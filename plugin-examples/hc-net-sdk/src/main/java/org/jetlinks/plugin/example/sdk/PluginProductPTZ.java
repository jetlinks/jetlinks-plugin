package org.jetlinks.plugin.example.sdk;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.jetlinks.core.device.DeviceOperator;
import org.jetlinks.core.metadata.ConfigMetadata;
import org.jetlinks.plugin.example.sdk.hc.HCNetSDK;
import org.jetlinks.plugin.example.sdk.hc.NetSDKDemo;
import org.jetlinks.plugin.internal.device.DeviceProduct;
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
 * @author zhangji 2023/3/9
 */
@Slf4j
public class PluginProductPTZ implements PluginProduct {
    private static String METADATA;

    private static final String USERNAME    = "user";
    private static final String PASSWORD    = "psw";
    private static final String FUNCTION_ID = "regular_inspection";

    static {
        try (
                InputStream stream = new ClassPathResource(
                        "sdk-device-senser-metadata.json", PluginProductPTZ.class.getClassLoader())
                        .getInputStream()
        ) {
            // 从json文件中读取产品的默认物模型
            METADATA = StreamUtils.copyToString(stream, StandardCharsets.UTF_8);
        } catch (Throwable e) {
            PluginProductPTZ.log.warn("load metadata failed", e);
        }
    }

    @Override
    public String getId() {
        return "sdk-product-002";
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
        DeviceProduct product = new DeviceProduct();
        product.setId(getId());
        product.setName("sdk产品-球机");
        product.setGroup(getGroup());
        product.setDescription("sdk产品。以插件的方式接入");
        JetLinksDeviceMetadata metadata = new JetLinksDeviceMetadata(JSON.parseObject(METADATA));
        product.setMetadata(metadata);
        return product;
    }

    @Override
    public Map<String, Object> getSdkProperties(Integer userId,
                                                NetSDKDemo sdk) {
        Map<String, Object> properties = new HashMap<>();
        HCNetSDK.NET_DVR_PTZPOS ptzcfg = sdk.GetPTZcfg(userId);
        properties.put("wPanPos", ptzcfg.wPanPos);
        properties.put("wTiltPos", ptzcfg.wTiltPos);
        properties.put("wZoomPos", ptzcfg.wZoomPos);
        return properties;
    }

    @Override
    public boolean setSdkProperties(int userId,
                                    Map<String, Object> properties,
                                    NetSDKDemo sdk) {
        try {
            Map<String, Object> old = getSdkProperties(userId, sdk);
            old.putAll(properties);
            HCNetSDK.NET_DVR_PTZPOS ptzcfg = new HCNetSDK.NET_DVR_PTZPOS();
            old.computeIfPresent("wPanPos", (key, value) -> ptzcfg.wPanPos = Short.parseShort(value.toString()));
            old.computeIfPresent("wTiltPos", (key, value) -> ptzcfg.wTiltPos = Short.parseShort(value.toString()));
            old.computeIfPresent("wZoomPos", (key, value) -> ptzcfg.wZoomPos = Short.parseShort(value.toString()));

            return sdk.SetPTZcfg(userId, ptzcfg);
        } catch (Exception e) {
            log.error("set sdk properties error", e);
            return false;
        }
    }

    @Override
    public Mono<Object> invokeFunction(int userId,
                                       String function,
                                       DeviceOperator device,
                                       Map<String, Object> inputs,
                                       NetSDKDemo sdk) {
        if (FUNCTION_ID.equals(function)) {
            sdk.regularInspection();
            return Mono.just("success");
        }
        return Mono.empty();
    }
}
