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
import org.jetlinks.core.message.DeviceMessage;
import org.jetlinks.core.message.DeviceOfflineMessage;
import org.jetlinks.core.message.DeviceOnlineMessage;
import org.jetlinks.core.message.function.FunctionInvokeMessage;
import org.jetlinks.core.message.property.ReadPropertyMessage;
import org.jetlinks.core.message.property.ReportPropertyMessage;
import org.jetlinks.core.message.property.WritePropertyMessage;
import org.jetlinks.core.metadata.ConfigMetadata;
import org.jetlinks.core.metadata.DefaultConfigMetadata;
import org.jetlinks.core.metadata.DeviceConfigScope;
import org.jetlinks.core.metadata.types.PasswordType;
import org.jetlinks.core.metadata.types.StringType;
import org.jetlinks.plugin.core.PluginContext;
import org.jetlinks.plugin.example.sdk.hc.HCNetSDK;
import org.jetlinks.plugin.example.sdk.hc.NetSDKDemo;
import org.jetlinks.plugin.internal.device.DeviceGatewayPlugin;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 输入描述.
 *
 * @author zhangji 2023/3/6
 */
@Slf4j
public class SdkDevicePlugin extends DeviceGatewayPlugin {

    static final String IP          = "ip";
    static final String PORT        = "port";
    static final String USERNAME    = "user";
    static final String PASSWORD    = "psw";
    static final String USER_ID     = "user_id";
    static final String DW_CHANNEL  = "dwChannel";
    static final String FUNCTION_ID = "monthly_record";

    static final ConfigKey<Boolean> required = ConfigKey.of("required", "是否必填", Boolean.TYPE);

    static final ConfigMetadata deviceConfigMetadata = new DefaultConfigMetadata("设备配置", "设备接入配置")
            .add(DW_CHANNEL, "通道号", new StringType(), DeviceConfigScope.device)
            .add(USERNAME, "用户名", new StringType().expand(required.value(true)), DeviceConfigScope.device)
            .add(PASSWORD, "密码", new PasswordType().expand(required.value(true)), DeviceConfigScope.device);

    static final ConfigMetadata productConfigMetadata = new DefaultConfigMetadata("产品配置", "设备接入配置")
            .add(DW_CHANNEL, "通道号", new StringType(), DeviceConfigScope.product);

    private static final NetSDKDemo sdk = new NetSDKDemo();

    public SdkDevicePlugin(String id,
                           PluginContext context) {
        super(id, context);
        sdk.initMockSDKInstance();
    }

    @Override
    public Mono<ConfigMetadata> getDeviceConfigMetadata(String productId) {
        return Mono.just(deviceConfigMetadata);
    }

    @Override
    public Mono<ConfigMetadata> getProductConfigMetadata(String productId) {
        return Mono.just(productConfigMetadata);
    }

    private Mono<Void> pollState() {
        return getPlatformDevices()
                .flatMap(device -> this
                        .getUserId(device)
                        .map(userId -> Tuples.of(device.getId(), userId)))
                .buffer(100)
                .flatMap(list -> this
                        .getDeviceState(list)
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

    public Flux<DeviceInfo> getDeviceState(List<Tuple2<String, Integer>> device) {
        return Flux.fromIterable(device)
                .map(tp2 -> new DeviceInfo(
                        tp2.getT1(),
                        sdk.getDeviceStatus(tp2.getT2()),
                        getSdkProperties(tp2.getT2())));
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

    private Mono<Map<String, Object>> getSdkProperties(String deviceId) {
        return registry.getDevice(deviceId)
                .flatMap(this::getUserId)
                .map(this::getSdkProperties);
    }

    private Map<String, Object> getSdkProperties(Integer userId) {
        Map<String, Object> properties = new HashMap<>();
        HCNetSDK.NET_DVR_ALARM_RS485CFG rs485Cfg = sdk.getRS485Cfg(userId);
        properties.put("dwSize", rs485Cfg.dwSize);
        properties.put("sDeviceName", new String(rs485Cfg.sDeviceName));
        properties.put("wDeviceType", rs485Cfg.wDeviceType);
        properties.put("wDeviceProtocol", rs485Cfg.wDeviceProtocol);
        properties.put("dwBaudRate", rs485Cfg.dwBaudRate);
        properties.put("byDataBit", rs485Cfg.byDataBit);
        properties.put("byStopBit", rs485Cfg.byStopBit);
        properties.put("byParity", rs485Cfg.byParity);
        properties.put("byFlowcontrol", rs485Cfg.byFlowcontrol);
        properties.put("byDuplex", rs485Cfg.byDuplex);
        properties.put("byWorkMode", rs485Cfg.byWorkMode);
        properties.put("byChannel", rs485Cfg.byChannel);
        properties.put("bySerialType", rs485Cfg.bySerialType);
        properties.put("byMode", rs485Cfg.byMode);
        properties.put("byOutputDataType", rs485Cfg.byOutputDataType);
        properties.put("byAddress", rs485Cfg.byAddress);
        properties.put("byRes", rs485Cfg.byRes);
        return properties;
    }

    private Mono<Boolean> setSdkProperties(String deviceId,
                                           Map<String, Object> properties) {
        return registry.getDevice(deviceId)
                .flatMap(this::getUserId)
                .map(userId -> setSdkProperties(userId, properties));
    }

    private boolean setSdkProperties(int userId,
                                     Map<String, Object> properties) {
        try {
            HCNetSDK.NET_DVR_ALARM_RS485CFG rs485Cfg = new HCNetSDK.NET_DVR_ALARM_RS485CFG();
            rs485Cfg.dwSize = (int) properties.get("dwSize");
            rs485Cfg.sDeviceName = properties.get("sDeviceName").toString().getBytes(StandardCharsets.UTF_8);
            rs485Cfg.wDeviceType = (short) properties.get("wDeviceType");
            rs485Cfg.wDeviceProtocol = (short) properties.get("wDeviceProtocol");
            rs485Cfg.dwBaudRate = (int) properties.get("dwBaudRate");
            rs485Cfg.byDataBit = (byte) properties.get("byDataBit");
            rs485Cfg.byStopBit = (byte) properties.get("byStopBit");
            rs485Cfg.byParity = (byte) properties.get("byParity");
            rs485Cfg.byFlowcontrol = (byte) properties.get("byFlowcontrol");
            rs485Cfg.byDuplex = (byte) properties.get("byDuplex");
            rs485Cfg.byWorkMode = (byte) properties.get("byWorkMode");
            rs485Cfg.byChannel = (byte) properties.get("byChannel");
            rs485Cfg.bySerialType = (byte) properties.get("bySerialType");
            rs485Cfg.byMode = (byte) properties.get("byMode");
            rs485Cfg.byOutputDataType = (byte) properties.get("byOutputDataType");
            rs485Cfg.byAddress = (byte) properties.get("byAddress");
            rs485Cfg.byRes = (byte[]) properties.get("byRes");

            return sdk.setRS485Cfg(userId, rs485Cfg);
        } catch (Exception e) {
            log.error("set sdk properties error", e);
            return false;
        }
    }

    @Override
    public Publisher<? extends DeviceMessage> execute(DeviceMessage message) {

        // 读取属性
        if (message instanceof ReadPropertyMessage) {
            return this
                    .getSdkProperties(message.getDeviceId())
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
            String deviceId = functionInvokeMessage.getDeviceId();
            String function = functionInvokeMessage.getFunctionId();
            if (FUNCTION_ID.equals(function)) {
                return registry
                        .getDevice(deviceId)
                        .flatMap(device -> Mono
                                .zip(
                                        this.getUserId(device),
                                        device.getSelfConfig(ConfigKey.of(DW_CHANNEL))
                                                .map(Object::toString)
                                                .map(Integer::valueOf)
                                                .defaultIfEmpty(32)
                                )
                                .map(tp2 -> sdk.GetRecMonth(
                                        tp2.getT1(),
                                        functionInvokeMessage
                                                .getInput("wYear")
                                                .map(Object::toString)
                                                .map(Integer::valueOf)
                                                .orElse(LocalDate.now().getYear()),
                                        functionInvokeMessage.getInput("byMonth")
                                                .map(Object::toString)
                                                .map(Integer::valueOf)
                                                .orElse(LocalDate.now().getMonthValue()),
                                        tp2.getT2()))
                                .map(bytes -> {
                                    int[] array = new int[bytes.length];
                                    for (int i = 0; i < bytes.length; i++) {
                                        array[i] = bytes[i];
                                    }
                                    return array;
                                })
                                .map(result -> functionInvokeMessage.newReply().success(result)));
            }
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
