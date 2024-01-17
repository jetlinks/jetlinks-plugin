package org.jetlinks.plugin.generator.core;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.joox.Match;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.StreamUtils;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.function.Consumer;

import static org.joox.JOOX.$;

@Slf4j
@AllArgsConstructor
class DefaultSdkGenerator implements SdkGenerator {

    private final Project project;
    private final FileTree root = new FileTree();
    private final FileTree sources = root.newPath("src/main/java");
    private final FileTree resources = root.newPath("src/main/resources");
    private final Set<Dependency> dependencies = new LinkedHashSet<>();

    @Override
    public SdkGenerator project(Consumer<Project> projectCustomizer) {
        projectCustomizer.accept(project);
        return this;
    }

    @Override
    public SdkGenerator addSource(String name, InputStream content) {
        sources.addFile(name.replace(".", "/") + ".java", content);
        return this;
    }

    @Override
    public SdkGenerator addResource(String name, InputStream content) {
        resources.addFile(name, content);
        return this;
    }

    @Override
    public SdkGenerator addFile(String name, InputStream content) {
        root.addFile(name, content);
        return this;
    }

    @Override
    public SdkGenerator addDependency(Dependency... dependencies) {
        return addDependency(Arrays.asList(dependencies));
    }

    @Override
    public SdkGenerator addDependency(Collection<Dependency> dependencies) {
        this.dependencies.addAll(dependencies);
        return this;
    }

    @Override
    @SneakyThrows
    public void write(OutputStream stream) {

        generatePom();

        try (ZipArchiveOutputStream zip = new ZipArchiveOutputStream(stream)) {
            root.write(zip);
        }
    }

    @Override
    public void write(Path path) {
        if (path.toFile().isFile()) {
            throw new IllegalArgumentException(path + " is not a directory");
        }
        boolean ignore = path.toFile().mkdirs();

        generatePom();
        try {
            root.write(path);
        } finally {
            root.close();
        }
    }

    @SneakyThrows
    private void generatePom() {
        try (InputStream resource = new ClassPathResource("plugin-generator/template/pom.xml").getInputStream()) {
            String pomXml = StreamUtils.copyToString(resource, StandardCharsets.UTF_8);
            if (!dependencies.isEmpty()) {
                Match xml = $(pomXml);

                xml.find("project > artifactId").text(project.getArtifactId());
                xml.find("project > groupId").text(project.getGroupId());
                xml.find("project > version").text(project.getVersion());

                if (project.getName() != null) {
                    xml.find("project > name").text(project.getName());
                }
                if (project.getDescription() != null) {
                    xml.find("project > description").text(project.getDescription());
                }

                Match dependenciesXml = xml.find("dependencies");
                for (Dependency dependency : dependencies) {
                    dependenciesXml = dependenciesXml.append(dependency.toXml());
                }
                ByteArrayOutputStream out = new ByteArrayOutputStream();

                Result result = new StreamResult(out);
                Source source = new DOMSource(xml.document());

                TransformerFactory factory = TransformerFactory.newInstance();
                Transformer transformer = factory.newTransformer();

                transformer.setOutputProperty(javax.xml.transform.OutputKeys.INDENT, "yes");
                transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

                transformer.transform(source, result);
                root.addFile("pom.xml", new ByteArrayInputStream(out.toByteArray()));
            } else {
                root.addFile("pom.xml", new ByteArrayInputStream(pomXml.getBytes(StandardCharsets.UTF_8)));
            }
        }
    }

    @Getter
    @EqualsAndHashCode(of = "name")
    private static class FileTree implements Closeable {
        private Set<FileTree> next;

        private String name;
        private boolean file;
        private InputStream content;

        private void write(ZipArchiveOutputStream stream) {
            write(null, stream);
        }

        @SneakyThrows
        private void write(String parent, ZipArchiveOutputStream stream) {
            if (name != null) {
                String entryName = parent == null ? name : parent + "/" + name;
                if (file && content != null) {
                    ZipArchiveEntry entry = new ZipArchiveEntry(entryName);
                    stream.putArchiveEntry(entry);
                    StreamUtils.copy(content, stream);
                    stream.closeArchiveEntry();
                }
                if (next != null) {
                    for (FileTree fileTree : next) {
                        fileTree.write(entryName, stream);
                    }
                }
            } else {
                if (next != null) {
                    for (FileTree fileTree : next) {
                        fileTree.write(stream);
                    }
                }
            }


        }

        @SneakyThrows
        public void write(Path path) {
            Path thisPath = name == null ? path : Paths.get(path.toString(), name);
            if (file && content != null) {
                Files.copy(content, thisPath, StandardCopyOption.REPLACE_EXISTING);
            }
            if (!file && next != null) {
                boolean ignore = thisPath.toFile().mkdirs();
                for (FileTree fileTree : next) {
                    fileTree.write(thisPath);
                }
            }

        }

        public FileTree newPath(String name) {
            return addFile(name, null);
        }

        public FileTree addFile(String name, InputStream content) {
            String[] arr = name.split("[/\\\\]", 2);
            if (next == null) {
                next = new LinkedHashSet<>();
            }
            FileTree tree = new FileTree();
            tree.name = arr[0];
            next.add(tree);
            if (arr.length == 1) {
                tree.file = content != null;
                tree.content = content;
                return tree;
            } else {
                return tree.addFile(arr[1], content);
            }
        }

        @Override
        public void close() {
            if (null != content) {
                try {
                    content.close();
                } catch (IOException ignore) {

                }
            }
            if (next != null) {
                next.forEach(FileTree::close);
            }
        }
    }
}
