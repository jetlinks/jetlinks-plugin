package org.jetlinks.plugin.external.runner;

import org.jetlinks.core.Wrapper;
import org.jetlinks.core.command.Command;
import org.jetlinks.plugin.core.Description;
import org.jetlinks.plugin.core.Plugin;
import org.jetlinks.plugin.core.PluginContext;
import org.jetlinks.plugin.core.PluginDriver;
import org.jetlinks.plugin.core.PluginType;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JarPluginDriverSourceTest {
    private static final String SERVICE = "META-INF/services/org.jetlinks.plugin.core.PluginDriver";
    private static final String DRIVER = TestDriver.class.getName();
    private static final String DRIVER_TWO = TestDriverTwo.class.getName();

    @Test
    void loadsExactlyOneServiceProviderAndClosesItsClassloader() throws Exception {
        Path jar = createJar(DRIVER);
        JarPluginDriverSource source = new JarPluginDriverSource(jar);
        JavaPluginDriverSource.LoadedPluginDriver loaded = source.load().block();
        assertEquals(DRIVER, loaded.driver().getClass().getName());
        loaded.close();
        source.close();
        Files.delete(jar);
    }

    @Test
    void rejectsNoProvider() throws Exception {
        Path jar = createJar("");
        try {
            assertThrows(IllegalStateException.class, () -> new JarPluginDriverSource(jar).load().block());
        } finally {
            Files.deleteIfExists(jar);
        }
    }

    @Test
    void rejectsMultipleProviders() throws Exception {
        Path jar = createJar(DRIVER + "\n" + DRIVER_TWO + "\n");
        try {
            assertThrows(IllegalStateException.class, () -> new JarPluginDriverSource(jar).load().block());
        } finally {
            Files.deleteIfExists(jar);
        }
    }

    private static Path createJar(String services) throws Exception {
        Path jar = Files.createTempFile("external-plugin", ".jar");
        try (OutputStream output = Files.newOutputStream(jar);
             JarOutputStream archive = new JarOutputStream(output)) {
            archive.putNextEntry(new JarEntry(SERVICE));
            archive.write(services.getBytes(StandardCharsets.UTF_8));
            archive.closeEntry();
        }
        return jar;
    }

    public static class TestDriver implements PluginDriver {
        private final PluginType type = new PluginType() {
            @Override
            public String getId() {
                return "test";
            }

            @Override
            public String getName() {
                return "Test";
            }
        };

        @Override
        public Description getDescription() {
            return Description.of("test", "Test", "test", null, null, Collections.emptyMap());
        }

        @Override
        public PluginType getType() {
            return type;
        }

        @Override
        public Mono<? extends Plugin> createPlugin(String pluginId, PluginContext context) {
            return Mono.error(new UnsupportedOperationException());
        }

        @Override
        public <R> R execute(Command<R> command) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean isWrapperFor(Class<?> type) {
            return false;
        }

        @Override
        public <T> T unwrap(Class<T> type) {
            throw new IllegalArgumentException("not wrapped");
        }
    }

    public static final class TestDriverTwo extends TestDriver {
    }
}
