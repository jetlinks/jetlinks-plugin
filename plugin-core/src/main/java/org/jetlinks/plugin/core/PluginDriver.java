package org.jetlinks.plugin.core;

import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;

public interface PluginDriver {

    @Nonnull
    Description getDescription();

    @Nonnull
    PluginType getType();

    @Nonnull
    Mono<Plugin> createPlugin(@Nonnull String pluginId,
                              @Nonnull PluginContext context);

}
