package org.jetlinks.plugin.core;

import org.jetlinks.core.Wrapper;
import org.jetlinks.core.command.Command;
import org.jetlinks.core.command.CommandSupport;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;

public interface PluginDriver extends CommandSupport, Wrapper {

    @Nonnull
    Description getDescription();

    @Nonnull
    PluginType getType();

    @Nonnull
    Mono<? extends Plugin> createPlugin(@Nonnull String pluginId,
                                        @Nonnull PluginContext context);

    @Nonnull
    @Override
    default <R> R execute(@Nonnull Command<R> command) {
        throw new UnsupportedOperationException("unsupported command:" + command);
    }

    default Flux<DataBuffer> getResource(String name) {
        return DataBufferUtils
            .read(new ClassPathResource(name,
                                        this.getClass().getClassLoader()),
                  new DefaultDataBufferFactory(),
                  4096);
    }
}
