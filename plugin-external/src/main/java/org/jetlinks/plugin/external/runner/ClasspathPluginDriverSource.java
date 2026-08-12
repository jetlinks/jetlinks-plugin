package org.jetlinks.plugin.external.runner;

import org.jetlinks.plugin.core.PluginDriver;
import reactor.core.publisher.Mono;

import java.util.Objects;

/** Driver source for embedded tests or an application that already assembled its classpath. */
public final class ClasspathPluginDriverSource implements JavaPluginDriverSource {
    private final PluginDriver driver;

    public ClasspathPluginDriverSource(PluginDriver driver) {
        this.driver = Objects.requireNonNull(driver, "driver");
    }

    @Override
    public Mono<LoadedPluginDriver> load() {
        return Mono.just(new LoadedPluginDriver(driver, driver.getClass().getClassLoader()));
    }
}
