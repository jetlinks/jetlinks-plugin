package org.jetlinks.plugin.tcp;

import org.jetlinks.core.metadata.DefaultConfigMetadata;
import org.jetlinks.core.metadata.types.BooleanType;
import org.jetlinks.plugin.core.Description;
import org.jetlinks.plugin.core.PluginContext;
import org.jetlinks.plugin.core.Version;
import org.jetlinks.plugin.core.VersionRange;
import org.jetlinks.plugin.internal.device.DeviceGatewayPlugin;
import org.jetlinks.plugin.internal.device.DeviceGatewayPluginDriver;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;
import java.util.Collections;

public class ExampleTcpDevicePluginDriver implements DeviceGatewayPluginDriver {
    static Version version_1_0 = new Version(1, 0, 0, true);

    @Nonnull
    @Override
    public Mono<? extends DeviceGatewayPlugin> createPlugin(@Nonnull String pluginId, @Nonnull PluginContext context) {

        ExampleTcpDevicePlugin plugin = new ExampleTcpDevicePlugin(pluginId, context);

        context.environment()
               .getProperty("sortConnection", Boolean.class)
               .ifPresent(plugin::setSortConnection);

        context.environment()
               .getProperty("maxRetryTimes", Integer.class)
               .ifPresent(plugin::setMaxRetryTimes);

        return Mono.just(plugin);
    }

    @Nonnull
    @Override
    public Description getDescription() {
        return Description.of(
            "tcp-client-device",
            "TCP客户端请求设备数据",
            "",
            version_1_0,
            VersionRange.of(Version.platform_2_0, Version.platform_latest),
            //告诉平台,此插件需要的配置信息
            Collections.singletonMap(
                PLUGIN_CONFIG_METADATA,
                new DefaultConfigMetadata()
                    .add("sortConnection", "是否短链接", BooleanType.GLOBAL)
                    .add("maxRetryTimes", "最大重试次数", BooleanType.GLOBAL)
            )
        );
    }
}
