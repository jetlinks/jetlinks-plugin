package org.jetlinks.plugin.generator.core;


import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.Collection;
import java.util.function.Consumer;

/**
 * <pre>{@code
 *
 *   SdkGenerator
 *       .create("org.jetlinks:demo-sdk:1.0.0")
 *       .addSource("org.jetlinks.demo.DemoPlugin.java",source)
 *       .addResource("README.md",readme)
 *       .write(outputStream);
 *
 * }</pre>
 * <pre>{@code
 *  sdk.zip
 *    |--demo-sdk
 *    |----|---pom.xml
 *    |----|---src
 *    |----|----|---main
 *    |----|----|----|---java
 *    |----|----|----|----|---org.jetlinks.demo
 *    |----|----|----|----|----|---DemoPlugin.java
 *    |----|----|----|---resources
 *
 * }</pre>
 *
 * @author zhouhao
 * @since 1.0.2
 */
public interface SdkGenerator {

    static SdkGenerator create(String artifact) {
        Project project = new Project();
        project.withPattern(artifact);
        return new DefaultSdkGenerator(project);
    }

    SdkGenerator project(Consumer<Project> projectCustomizer);

    SdkGenerator addSource(String name, InputStream content);

    SdkGenerator addResource(String name, InputStream content);

    SdkGenerator addFile(String name, InputStream content);

    SdkGenerator addDependency(Dependency... dependencies);

    SdkGenerator addDependency(Collection<Dependency> dependencies);

    void write(OutputStream stream);

    void write(Path path);
}
