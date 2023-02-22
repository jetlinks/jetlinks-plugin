package org.jetlinks.plugin.core;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;
import java.nio.ByteBuffer;

public interface PluginDriver {

    @Nonnull
    Description getDescription();

    @Nonnull
    PluginType getType();

    @Nonnull
    Mono<? extends Plugin> createPlugin(@Nonnull String pluginId,
                                        @Nonnull PluginContext context);

    default Flux<ByteBuffer> getResource(String name) {
        return Flux.empty();
    }
}
