package org.jetlinks.plugin.external.runner;

import org.jetlinks.plugin.core.PluginDriver;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

/**
 * Loads a single driver declared by the standard ServiceLoader file. Guessing a main class or
 * scanning arbitrary implementation classes is intentionally unsupported.
 */
public final class JarPluginDriverSource implements JavaPluginDriverSource {
    private final Path pluginJar;
    private volatile URLClassLoader classLoader;

    public JarPluginDriverSource(Path pluginJar) {
        this.pluginJar = pluginJar.toAbsolutePath().normalize();
    }

    @Override
    public Mono<LoadedPluginDriver> load() {
        return Mono.fromCallable(this::loadNow);
    }

    private LoadedPluginDriver loadNow() throws IOException {
        if (!Files.isRegularFile(pluginJar) || !Files.isReadable(pluginJar)) {
            throw new IOException("plugin jar is not a readable regular file: " + pluginJar);
        }
        URLClassLoader loader = new URLClassLoader(new URL[]{pluginJar.toUri().toURL()},
                                                   PluginDriver.class.getClassLoader());
        classLoader = loader;
        List<PluginDriver> drivers = new ArrayList<>();
        ServiceLoader.load(PluginDriver.class, loader).forEach(drivers::add);
        if (drivers.size() != 1) {
            closeLoader(loader);
            throw new IllegalStateException("plugin jar must declare exactly one PluginDriver, found " + drivers.size());
        }
        return new LoadedPluginDriver(drivers.get(0), loader);
    }

    @Override
    public void close() {
        URLClassLoader loader = classLoader;
        if (loader != null) {
            closeLoader(loader);
            classLoader = null;
        }
    }

    private static void closeLoader(URLClassLoader loader) {
        try {
            loader.close();
        } catch (IOException error) {
            throw new IllegalStateException("unable to close plugin classloader", error);
        }
    }
}
