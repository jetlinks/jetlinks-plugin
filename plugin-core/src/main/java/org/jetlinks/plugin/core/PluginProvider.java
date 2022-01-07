package org.jetlinks.plugin.core;

import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;

public interface PluginProvider {

    @Nonnull
    Description getDescription();

    @Nonnull
    Mono<Plugin> createPlugin(@Nonnull String pluginId,
                              @Nonnull PluginContext context);

}
