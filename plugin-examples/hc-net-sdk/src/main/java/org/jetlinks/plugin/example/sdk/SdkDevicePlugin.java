package org.jetlinks.plugin.example.sdk;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.hswebframework.web.exception.BusinessException;
import org.jetlinks.core.Value;
import org.jetlinks.core.config.ConfigKey;
import org.jetlinks.core.device.DeviceOperator;
import org.jetlinks.core.device.DeviceProductOperator;
import org.jetlinks.core.message.DeviceMessage;
import org.jetlinks.core.message.DeviceOfflineMessage;
import org.jetlinks.core.message.DeviceOnlineMessage;
import org.jetlinks.core.message.function.FunctionInvokeMessage;
import org.jetlinks.core.message.function.FunctionParameter;
import org.jetlinks.core.message.property.ReadPropertyMessage;
import org.jetlinks.core.message.property.ReportPropertyMessage;
import org.jetlinks.core.message.property.WritePropertyMessage;
import org.jetlinks.core.metadata.ConfigMetadata;
import org.jetlinks.core.metadata.DefaultConfigMetadata;
import org.jetlinks.core.metadata.DeviceConfigScope;
import org.jetlinks.core.metadata.types.PasswordType;
import org.jetlinks.core.metadata.types.StringType;
import org.jetlinks.plugin.core.PluginContext;
import org.jetlinks.plugin.example.sdk.hc.NetSDKDemo;
import org.jetlinks.plugin.internal.device.DeviceGatewayPlugin;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 输入描述.
 *
 * @author zhangji 2023/3/6
 */
@Slf4j
public class SdkDevicePlugin extends DeviceGatewayPlugin {

    static final String IP       = "ip";
    static final String PORT     = "port";
    static final String USERNAME = "user";
    static final String PASSWORD = "psw";
    static final String USER_ID  = "user_id";

    public static final ConfigKey<Boolean> required = ConfigKey.of("required", "是否必填", Boolean.TYPE);
    private static final NetSDKDemo sdk = new NetSDKDemo();
    public static final Map<String, PluginProduct> pluginProducts = new HashMap<>();

    static {
        PluginProductRS485 media = new PluginProductRS485();
        pluginProducts.put(media.getId(), media);
        PluginProductPTZ senser = new PluginProductPTZ();
        pluginProducts.put(senser.getId(), senser);
    }


    public SdkDevicePlugin(String id,
                           PluginContext context) {
        super(id, context);
        sdk.initMockSDKInstance();
    }

    @Override
    public Mono<ConfigMetadata> getDeviceConfigMetadata(String productId) {
        return Mono
                .justOrEmpty(pluginProducts.get(productId))
                .mapNotNull(PluginProduct::getDeviceConfigMetadata)
                .map(configMetadata -> ((DefaultConfigMetadata) configMetadata)
                        .add(USERNAME, "用户名", new StringType().expand(required.value(true)), DeviceConfigScope.device)
                        .add(PASSWORD, "密码", new PasswordType().expand(required.value(true)), DeviceConfigScope.device))
                .defaultIfEmpty(new DefaultConfigMetadata()
                        .add(USERNAME, "用户名", new StringType().expand(required.value(true)), DeviceConfigScope.device)
                        .add(PASSWORD, "密码", new PasswordType().expand(required.value(true)), DeviceConfigScope.device))
                .cast(ConfigMetadata.class);
    }

    @Override
    public Mono<ConfigMetadata> getProductConfigMetadata(String productId) {
        return Mono
                .justOrEmpty(pluginProducts.get(productId))
                .mapNotNull(PluginProduct::getProductConfigMetadata);
    }

    @Override
    public Mono<Byte> getDeviceState(DeviceOperator device) {
        return this
                .getUserId(device)
                .map(sdk::getDeviceStatus)
                .map(status -> status ? (byte) 1 : (byte) 0);
    }

    private Mono<Void> pollState() {
        return getPlatformDevices()
                .buffer(100)
                .flatMap(list -> Flux.fromIterable(list)
                        .flatMap(this::getDeviceInfo)
                        .flatMap(this::handleState)
                        .onErrorResume(err -> {
                            log.warn("check device state error", err);
                            return Mono.empty();
                        }))
                .then();
    }

    private Mono<Void> handleState(DeviceInfo deviceInfo) {
        //在线
        if (deviceInfo.online) {
            //属性上报
            if (MapUtils.isNotEmpty(deviceInfo.properties)) {
                ReportPropertyMessage msg = new ReportPropertyMessage();
                msg.setDeviceId(deviceInfo.id);
                msg.setProperties(deviceInfo.getProperties());
                return handleMessage(msg);
            }

            DeviceOnlineMessage message = new DeviceOnlineMessage();
            message.setDeviceId(deviceInfo.id);
            return handleMessage(message);
        } else {
            DeviceOfflineMessage message = new DeviceOfflineMessage();
            message.setDeviceId(deviceInfo.id);
            return handleMessage(message);
        }
    }

    public Mono<DeviceInfo> getDeviceInfo(DeviceOperator device) {
        return Mono
                .zip(
                        Mono.just(device.getId()),
                        this.getUserId(device),
                        this.getPluginProduct(device)
                )
                .map(tp3 -> new DeviceInfo(
                        tp3.getT1(),
                        sdk.getDeviceStatus(tp3.getT2()),
                        tp3.getT3().getSdkProperties(tp3.getT2(), sdk)))
                .switchIfEmpty(Mono.error(() -> new BusinessException("plugin device info not exist")));
    }

    /**
     * 获取已登录的设备用户ID
     *
     * @param device 设备操作
     * @return 用户ID
     */
    private Mono<Integer> getUserId(DeviceOperator device) {
        return device
                .getSelfConfig(USER_ID)
                .map(Value::asInt)
                .switchIfEmpty(device.getSelfConfigs(IP, PORT, USERNAME, PASSWORD)
                        // 不存在用户ID，则发起登录
                        .map(values -> sdk.Login_V40(
                                values.getString(IP, ""),
                                values.getNumber(PORT, 0).shortValue(),
                                values.getString(USERNAME, ""),
                                values.getString(PASSWORD, "")))
                        .flatMap(userId -> device.setConfig(USER_ID, userId).thenReturn(userId)));
    }

    private Mono<Boolean> setSdkProperties(String deviceId,
                                           Map<String, Object> properties) {
        return registry.getDevice(deviceId)
                .flatMap(device -> Mono
                        .zip(
                                this.getUserId(device),
                                this.getPluginProduct(device)
                        ))
                .map(tp2 -> tp2.getT2().setSdkProperties(tp2.getT1(), properties, sdk));

    }

    private Mono<Object> invokeFunction(FunctionInvokeMessage message) {
        return registry.getDevice(message.getDeviceId())
                .flatMap(device -> Mono
                        .zip(
                                Mono.just(device),
                                this.getUserId(device),
                                this.getPluginProduct(device)
                        ))
                .flatMap(tp3 -> tp3
                        .getT3()
                        .invokeFunction(
                                tp3.getT2(),
                                message.getFunctionId(),
                                tp3.getT1(),
                                message.getInputs().stream()
                                        .collect(Collectors.toMap(FunctionParameter::getName, FunctionParameter::getValue)),
                                sdk));
    }

    /**
     * 获取插件内部的产品信息
     * @param device 设备操作
     * @return 插件内部产品
     */
    private Mono<PluginProduct> getPluginProduct(DeviceOperator device) {
        return device
                .getProduct()
                // 内部产品ID
                .map(DeviceProductOperator::getId)
                .doOnNext(id -> log.info("plugin id: {}", id))
                .mapNotNull(pluginProducts::get);
    }

    @Override
    public Publisher<? extends DeviceMessage> execute(DeviceMessage message) {

        // 读取属性
        if (message instanceof ReadPropertyMessage) {
            return registry
                    .getDevice(message.getDeviceId())
                    .flatMap(this::getDeviceInfo)
                    .map(DeviceInfo::getProperties)
                    .map(((ReadPropertyMessage) message).newReply()::success);
        }
        //修改属性
        else if (message instanceof WritePropertyMessage) {
            Map<String, Object> props = ((WritePropertyMessage) message).getProperties();
            return this
                    .setSdkProperties(message.getDeviceId(), props)
                    .map(success -> success ? ((WritePropertyMessage) message).newReply().success(props) :
                            ((WritePropertyMessage) message).newReply().error(new BusinessException("error")))
                    .onErrorResume(err -> Mono.just(((WritePropertyMessage) message)
                            .newReply()
                            .error(err)));
            // 调用功能
        } else if (message instanceof FunctionInvokeMessage) {
            FunctionInvokeMessage functionInvokeMessage = ((FunctionInvokeMessage) message);
            return this
                    .invokeFunction(functionInvokeMessage)
                    .map(result -> functionInvokeMessage.newReply().success(result));
        }

        return super.execute(message);
    }

    @Override
    protected Mono<Void> doStart() {

        //启动时定时拉取状态
        this.context()
                .scheduler()
                .interval("pull_device_state",
                        Mono.defer(this::pollState),
                        Duration.ofSeconds(10));

        return super.doStart();
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DeviceInfo {
        private String  id;
        private boolean online;

        private Map<String, Object> properties;
    }
}
