package org.jetlinks.plugin.internal.ai;

import org.jetlinks.plugin.core.Description;
import org.jetlinks.plugin.core.Plugin;
import org.jetlinks.plugin.core.PluginContext;
import org.jetlinks.plugin.core.PluginType;
import org.jetlinks.plugin.internal.InternalPluginType;
import org.jetlinks.plugin.internal.device.AbstractPluginDriver;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;

public abstract class AiPluginDriver extends AbstractPluginDriver {


    @Nonnull
    @Override
    public abstract Description getDescription();

    @Nonnull
    @Override
    public final PluginType getType() {
        return InternalPluginType.ai;
    }


    @Nonnull
    @Override
    public abstract Mono<? extends Plugin> createPlugin(@Nonnull String pluginId,
                                                        @Nonnull PluginContext context);


}
