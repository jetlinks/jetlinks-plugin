package org.jetlinks.plugin.external.runner;

import org.jetlinks.plugin.core.PluginDriver;
import reactor.core.publisher.Mono;

/** Loads exactly one native Java plugin driver for a runtime generation. */
public interface JavaPluginDriverSource extends AutoCloseable {
    Mono<LoadedPluginDriver> load();

    @Override
    default void close() {
    }

    /** Loaded driver and the classloader that owns its plugin dependencies. */
    final class LoadedPluginDriver implements AutoCloseable {
        private final PluginDriver driver;
        private final ClassLoader classLoader;

        public LoadedPluginDriver(PluginDriver driver, ClassLoader classLoader) {
            this.driver = driver;
            this.classLoader = classLoader;
        }

        public PluginDriver driver() {
            return driver;
        }

        public ClassLoader classLoader() {
            return classLoader;
        }

        @Override
        public void close() {
            if (classLoader instanceof AutoCloseable) {
                try {
                    ((AutoCloseable) classLoader).close();
                } catch (Exception error) {
                    throw new IllegalStateException("unable to close plugin classloader", error);
                }
            }
        }
    }
}
