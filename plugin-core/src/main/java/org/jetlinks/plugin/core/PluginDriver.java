package org.jetlinks.plugin.core;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
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

    default Flux<DataBuffer> getResource(String name) {
        return DataBufferUtils
                .read(new ClassPathResource(name,
                                            this.getClass().getClassLoader()),
                      new DefaultDataBufferFactory(),
                      4096);
    }
}
