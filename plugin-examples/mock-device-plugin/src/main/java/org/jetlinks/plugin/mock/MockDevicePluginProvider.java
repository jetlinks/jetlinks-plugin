package org.jetlinks.plugin.mock;

import org.jetlinks.core.config.ConfigKey;
import org.jetlinks.core.metadata.DefaultConfigMetadata;
import org.jetlinks.core.metadata.types.IntType;
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

public class MockDevicePluginProvider implements DeviceGatewayPluginDriver {

    static Version version_1_0 = new Version(1, 0, 0, true);
    public static final ConfigKey<Boolean> required = ConfigKey.of("required", "是否必填", Boolean.TYPE);

    @Nonnull
    @Override
    public final Description getDescription() {
        return Description.of(
            "mock-device",
            "模拟设备数据",
            "",
            version_1_0,
            VersionRange.of(Version.platform_2_0, Version.platform_latest),
            //告诉平台,此插件需要的配置信息
            Collections.singletonMap(
                PLUGIN_CONFIG_METADATA,
                new DefaultConfigMetadata()
                    .add(MockDevicePlugin.MOCK_INTERVAL, "模拟频率(秒)", new IntType()
                        .min(1)
                        .expand(required.value(true)))
                    .add(MockDevicePlugin.MOCK_NUMBER, "模拟设备数量", new IntType()
                        .min(1)
                        .expand(required.value(true)))
            )
        );
    }

    @Nonnull
    @Override
    public Mono<? extends DeviceGatewayPlugin> createPlugin(@Nonnull String pluginId, @Nonnull PluginContext context) {
        return Mono.just(new MockDevicePlugin(pluginId, context));
    }


}
