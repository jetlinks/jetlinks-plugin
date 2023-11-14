package org.jetlinks.plugin.internal.media;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.jetlinks.core.config.ConfigKey;
import org.jetlinks.core.metadata.ConfigMetadata;
import org.jetlinks.core.metadata.DefaultConfigMetadata;
import org.jetlinks.core.metadata.types.EnumType;
import org.jetlinks.core.metadata.types.PasswordType;
import org.jetlinks.plugin.core.PluginType;
import org.jetlinks.plugin.internal.InternalPluginType;
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

/**
 * 输入描述.
 *
 * @author zhangji 2023/10/12
 */
@Slf4j
public abstract class MediaGatewayPluginDriver implements DeviceGatewayPluginDriver {

    public static DeviceProduct product;

    static {
        try (
                InputStream stream = new ClassPathResource(
                        "media-sdk-metadata.json", MediaGatewayPluginDriver.class.getClassLoader())
                        .getInputStream()
        ) {
            // 从json文件中读取产品的默认物模型
            String METADATA = StreamUtils.copyToString(stream, StandardCharsets.UTF_8);
            product = new DeviceProduct();
            product.setId("media-sdk");
            product.setName("默认sdk视频设备");
            product.setDescription("以插件的方式接入的视频设备");
            JetLinksDeviceMetadata metadata = new JetLinksDeviceMetadata(JSON.parseObject(METADATA));
            product.setMetadata(metadata);
        } catch (Throwable e) {
            log.warn("load metadata failed", e);
        }

    }

    @Override
    public Flux<DeviceProduct> getSupportProducts() {
        return Mono.justOrEmpty(product).flux();
    }


    @Nonnull
    @Override
    public PluginType getType() {
        return InternalPluginType.mediaGateway;
    }

}
