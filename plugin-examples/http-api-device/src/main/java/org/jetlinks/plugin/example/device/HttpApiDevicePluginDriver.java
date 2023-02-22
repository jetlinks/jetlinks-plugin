package org.jetlinks.plugin.example.device;

import org.jetlinks.core.metadata.DefaultConfigMetadata;
import org.jetlinks.core.metadata.types.StringType;
import org.jetlinks.plugin.core.Description;
import org.jetlinks.plugin.core.PluginContext;
import org.jetlinks.plugin.core.Version;
import org.jetlinks.plugin.core.VersionRange;
import org.jetlinks.plugin.internal.device.DeviceGatewayPlugin;
import org.jetlinks.plugin.internal.device.DeviceGatewayPluginDriver;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;
import java.util.Collections;

public class HttpApiDevicePluginDriver implements DeviceGatewayPluginDriver {

    static Version version_1_0 = new Version(1, 0, 0, true);

    @Nonnull
    @Override
    public final Description getDescription() {
        return Description.of(
                "mock-http-api",
                "模拟HTTP-API接入",
                "",
                version_1_0,
                VersionRange.of(Version.platform_2_0, Version.platform_latest),
                //告诉平台,此插件需要的配置信息
                Collections.singletonMap(
                        PLUGIN_CONFIG_METADATA,
                        new DefaultConfigMetadata()
                                .add(HttpApiDevicePlugin.API_URL, "API地址", StringType.GLOBAL)
                                .add(HttpApiDevicePlugin.ACCESS_KEY, "认证密钥", StringType.GLOBAL)
                )
        );
    }

    @Nonnull
    @Override
    public Mono<? extends DeviceGatewayPlugin> createPlugin(@Nonnull String pluginId,
                                                            @Nonnull PluginContext context) {
        return Mono.just(new HttpApiDevicePlugin(pluginId, context));
    }
}
