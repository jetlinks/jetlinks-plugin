package org.jetlinks.plugin.example.sdk;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.jetlinks.core.metadata.DefaultConfigMetadata;
import org.jetlinks.core.metadata.types.IntType;
import org.jetlinks.core.metadata.types.StringType;
import org.jetlinks.plugin.core.Description;
import org.jetlinks.plugin.core.PluginContext;
import org.jetlinks.plugin.core.Version;
import org.jetlinks.plugin.core.VersionRange;
import org.jetlinks.plugin.internal.device.DeviceGatewayPlugin;
import org.jetlinks.plugin.internal.device.DeviceGatewayPluginDriver;
import org.jetlinks.plugin.internal.device.DeviceProduct;
import org.jetlinks.supports.official.JetLinksDeviceMetadata;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.StreamUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

/**
 * 输入描述.
 *
 * @author zhangji 2023/3/6
 */
@Slf4j
public class SdkDevicePluginDriver implements DeviceGatewayPluginDriver {

    static Version version_1_0 = new Version(1, 0, 0, true);

    static final DeviceProduct product;

    public static String METADATA;

    static {

        try (
                InputStream stream = new ClassPathResource(
                        "sdk-device-metadata.json", SdkDevicePluginDriver.class.getClassLoader())
                        .getInputStream()
        ) {
            // 从json文件中读取产品的默认物模型
            METADATA = StreamUtils.copyToString(stream, StandardCharsets.UTF_8);
        } catch (Throwable e) {
            SdkDevicePluginDriver.log.warn("load metadata failed", e);
        }
    }

    static {
        product = new DeviceProduct();
        product.setId("sdk-product-001");
        product.setName("sdk产品001");
        product.setDescription("sdk产品。以插件的方式接入");
        JetLinksDeviceMetadata metadata = new JetLinksDeviceMetadata(JSON.parseObject(METADATA));
        product.setMetadata(metadata);
    }

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
                                .add(SdkDevicePlugin.IP, "设备ip", StringType.GLOBAL)
                                .add(SdkDevicePlugin.PORT, "设备端口", IntType.GLOBAL)
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
        return Flux.just(product);
    }

}
