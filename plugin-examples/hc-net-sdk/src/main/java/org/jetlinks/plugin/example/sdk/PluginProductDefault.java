package org.jetlinks.plugin.example.sdk;

import com.alibaba.fastjson.JSON;
import org.jetlinks.core.device.DeviceOperator;
import org.jetlinks.core.metadata.ConfigMetadata;
import org.jetlinks.plugin.example.sdk.hc.HCNetSDK;
import org.jetlinks.plugin.example.sdk.hc.NetSDKDemo;
import org.jetlinks.plugin.internal.device.DeviceProduct;
import org.jetlinks.plugin.internal.media.MediaGatewayPluginDriver;
import org.jetlinks.supports.official.JetLinksDeviceMetadata;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.StreamUtils;
import reactor.core.publisher.Mono;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * 输入描述.
 *
 * @author zhangji 2023/10/16
 */
public class PluginProductDefault implements PluginProduct {

    @Override
    public String getId() {
        return MediaGatewayPluginDriver.product.getId();
    }

    @Override
    public String getGroup() {
        return PluginProductGroup.PTZ.name();
    }

    @Override
    public ConfigMetadata getProductConfigMetadata() {
        return null;
    }

    @Override
    public ConfigMetadata getDeviceConfigMetadata() {
        return null;
    }

    @Override
    public DeviceProduct getDeviceProduct() {
        return MediaGatewayPluginDriver.product;
    }

    @Override
    public Map<String, Object> getSdkProperties(Integer userId,
                                                NetSDKDemo sdk) {
        return new HashMap<>();
    }

    @Override
    public boolean setSdkProperties(int userId,
                                    Map<String, Object> properties,
                                    NetSDKDemo sdk) {
        return false;
    }

    @Override
    public Mono<Object> invokeFunction(int userId,
                                       String function,
                                       DeviceOperator device,
                                       Map<String, Object> inputs,
                                       NetSDKDemo sdk) {
        return Mono.empty();
    }
}
