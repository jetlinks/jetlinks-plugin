package org.jetlinks.plugin.example.sdk;

import lombok.extern.slf4j.Slf4j;
import org.jetlinks.core.metadata.DefaultConfigMetadata;
import org.jetlinks.plugin.core.Description;
import org.jetlinks.plugin.core.PluginContext;
import org.jetlinks.plugin.core.Version;
import org.jetlinks.plugin.core.VersionRange;
import org.jetlinks.plugin.internal.device.DeviceGatewayPlugin;
import org.jetlinks.plugin.internal.device.DeviceProduct;
import org.jetlinks.plugin.internal.media.MediaGatewayPluginDriver;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;
import java.util.Collections;

import static org.jetlinks.plugin.example.sdk.SdkDevicePlugin.pluginProducts;

/**
 * 输入描述.
 *
 * @author zhangji 2023/3/6
 */
@Slf4j
public class SdkDevicePluginDriver extends MediaGatewayPluginDriver {

    static Version version_1_0 = new Version(1, 0, 0, true);

    @Nonnull
    @Override
    public final Description getDescription() {
        return Description.of(
                "mock-sdk",
                "模拟通过SDK接入",
                "基于海康威视设备SDK的参数获取与配置demo实现。作为展示JetLinks 2.0的插件接入能力的示例，与实际场景逻辑可能有所不同。",
                version_1_0,
                VersionRange.of(Version.platform_2_0, Version.platform_latest),
                //告诉平台,此插件需要的配置信息
                Collections.singletonMap(
                        PLUGIN_CONFIG_METADATA,
                        new DefaultConfigMetadata()
                )
        );
    }

    @Nonnull
    @Override
    public Mono<? extends DeviceGatewayPlugin> createPlugin(@Nonnull String pluginId,
                                                            @Nonnull PluginContext context) {
        return Mono.just(new SdkDevicePlugin(pluginId, context));
    }

    @Override
    public Flux<DeviceProduct> getSupportProducts() {
        return Flux
                .concat(
                        super.getSupportProducts(),
                        Flux
                                .fromIterable(pluginProducts.values())
                                .map(PluginProduct::getDeviceProduct)
                );
    }

}
