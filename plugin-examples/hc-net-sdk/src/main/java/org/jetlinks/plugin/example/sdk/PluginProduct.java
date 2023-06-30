package org.jetlinks.plugin.example.sdk;

import org.jetlinks.core.config.ConfigKey;
import org.jetlinks.core.device.DeviceOperator;
import org.jetlinks.core.metadata.ConfigMetadata;
import org.jetlinks.plugin.example.sdk.hc.NetSDKDemo;
import org.jetlinks.plugin.internal.device.DeviceProduct;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * 输入描述.
 *
 * @author zhangji 2023/3/9
 */
public interface PluginProduct {

    String USERNAME    = "user";
    String PASSWORD    = "psw";
    ConfigKey<Boolean> required = ConfigKey.of("required", "是否必填", Boolean.TYPE);

    String getId();

    String getGroup();

    ConfigMetadata getProductConfigMetadata();

    ConfigMetadata getDeviceConfigMetadata();

    DeviceProduct getDeviceProduct();

    Map<String, Object> getSdkProperties(Integer userId,
                                         NetSDKDemo sdk);

    boolean setSdkProperties(int userId,
                             Map<String, Object> properties,
                             NetSDKDemo sdk);

    Mono<Object> invokeFunction(int userId,
                                String function,
                                DeviceOperator device,
                                Map<String, Object> inputs,
                                NetSDKDemo sdk);

}
