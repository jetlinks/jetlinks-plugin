package org.jetlinks.plugin.internal.device;

import org.jetlinks.plugin.core.PluginContext;
import org.jetlinks.plugin.core.PluginType;
import org.jetlinks.plugin.internal.InternalPluginDriver;
import org.jetlinks.plugin.internal.InternalPluginType;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;

public interface DeviceGatewayPluginDriver extends InternalPluginDriver {

    /**
     * 获取此驱动支持的产品信息
     *
     * @return void
     */
    default Flux<DeviceProduct> getSupportProducts() {
        return Flux.empty();
    }

    @Nonnull
    @Override
    Mono<? extends DeviceGatewayPlugin> createPlugin(@Nonnull String pluginId,
                                                     @Nonnull PluginContext context);

    @Nonnull
    @Override
    default PluginType getType() {
        return InternalPluginType.deviceGateway;
    }
}
