package org.jetlinks.plugin.generator.core;

import lombok.SneakyThrows;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.jetlinks.plugin.generator.core.Dependency;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.io.ByteArrayInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.joox.JOOX.$;
import static org.junit.jupiter.api.Assertions.*;

class DefaultSdkGeneratorTest {

    @Test
    @SneakyThrows
    void testZip() {

        Path file = Paths.get("./target/sdk.zip");

        SdkGenerator
            .create("org.jetlinks:test-sdk:1.0.0")
            .addDependency(Dependency.of("org.jetlinks:jetlinks-core:1.2.2-SNAPSHOT"))
            .addSource("org.jetlinks.sdk.Test", new ByteArrayInputStream("test".getBytes()))
            .write(Files.newOutputStream(file));

        try (ZipArchiveInputStream input = new ZipArchiveInputStream(Files.newInputStream(file))) {
            ZipArchiveEntry entry;
            do {
                entry = input.getNextEntry();
                System.out.println(entry);
            } while (entry != null);
        }


    }

    @Test
    @SneakyThrows
    void testGeneratePom() {
        SdkGenerator
            .create("org.jetlinks:test-sdk:1.0.0")
            .addDependency(Dependency.of("org.jetlinks:jetlinks-core:1.2.2-SNAPSHOT"))
            .addDependency(Dependency.of("org.jetlinks:jetlinks-supports:1.2.2-SNAPSHOT"))
            .write(Paths.get("./target/sdk"));

        Flux.fromIterable(
                $(Paths.get("./target/sdk/pom.xml"))
                    .find("dependency"))
            .doOnNext(e -> {
                System.out.println($(e).toString());
            })
            .filter(element -> "jetlinks-core"
                .equals(element.getElementsByTagName("artifactId").item(0).getTextContent()))
            .as(StepVerifier::create)
            .expectNextCount(1)
            .verifyComplete();

    }

    @Test
    @SneakyThrows
    void testGenerateSource() {
        byte[] code = "test".getBytes();

        Path source = Paths.get("./target/testGenerateSource");
        SdkGenerator
            .create("org.jetlinks:testGenerateSource:1.0.0")
            .addSource("org.jetlinks.plugin.Test", new ByteArrayInputStream(code))
            .write(source);

        Path file = Paths.get(source.toString(), "src/main/java/org/jetlinks/plugin/Test.java");
        assertTrue(file.toFile().isFile());
        assertArrayEquals(code, Files.readAllBytes(file));

    }
}