package org.jetlinks.plugin.example.sdk;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.jetlinks.core.config.ConfigKey;
import org.jetlinks.core.device.DeviceOperator;
import org.jetlinks.core.metadata.ConfigMetadata;
import org.jetlinks.core.metadata.DefaultConfigMetadata;
import org.jetlinks.core.metadata.DeviceConfigScope;
import org.jetlinks.core.metadata.types.PasswordType;
import org.jetlinks.core.metadata.types.StringType;
import org.jetlinks.plugin.example.sdk.hc.HCNetSDK;
import org.jetlinks.plugin.example.sdk.hc.NetSDKDemo;
import org.jetlinks.plugin.internal.device.DeviceProduct;
import org.jetlinks.supports.official.JetLinksDeviceMetadata;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.StreamUtils;
import reactor.core.publisher.Mono;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

/**
 * 输入描述.
 *
 * @author zhangji 2023/3/9
 */
@Slf4j
public class PluginProductRS485 implements PluginProduct {
    private static String METADATA;

    private static final String DW_CHANNEL  = "dwChannel";
    private static final String FUNCTION_ID = "monthly_record";

    private static final ConfigMetadata deviceConfigMetadata = new DefaultConfigMetadata("设备配置", "设备接入配置")
            .add(DW_CHANNEL, "通道号", new StringType(), DeviceConfigScope.device);
    private static final ConfigMetadata productConfigMetadata = new DefaultConfigMetadata("产品配置", "设备接入配置")
            .add(DW_CHANNEL, "通道号", new StringType(), DeviceConfigScope.product);

    static {
        try (
                InputStream stream = new ClassPathResource(
                        "sdk-device-rs485-metadata.json", PluginProductRS485.class.getClassLoader())
                        .getInputStream()
        ) {
            // 从json文件中读取产品的默认物模型
            METADATA = StreamUtils.copyToString(stream, StandardCharsets.UTF_8);
        } catch (Throwable e) {
            PluginProductRS485.log.warn("load metadata failed", e);
        }
    }

    @Override
    public String getId() {
        return "sdk-product-001";
    }

    @Override
    public String getGroup() {
        return PluginProductGroup.RS485.name();
    }

    @Override
    public ConfigMetadata getProductConfigMetadata() {
        return productConfigMetadata;
    }

    @Override
    public ConfigMetadata getDeviceConfigMetadata() {
        return deviceConfigMetadata;
    }

    @Override
    public DeviceProduct getDeviceProduct() {
        DeviceProduct product = new DeviceProduct();
        product.setId(getId());
        product.setName("sdk产品-摄像头");
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

    @Override
    public boolean setSdkProperties(int userId,
                                    Map<String, Object> properties,
                                    NetSDKDemo sdk) {
        try {
            Map<String, Object> old = getSdkProperties(userId, sdk);
            old.putAll(properties);
            HCNetSDK.NET_DVR_ALARM_RS485CFG rs485Cfg = new HCNetSDK.NET_DVR_ALARM_RS485CFG();
            properties.computeIfPresent("dwSize", (key, value) -> rs485Cfg.dwSize = Integer.parseInt(value.toString()));
            properties.computeIfPresent("sDeviceName", (key, value) -> rs485Cfg.sDeviceName = value.toString().getBytes(StandardCharsets.UTF_8));
            properties.computeIfPresent("wDeviceType", (key, value) -> rs485Cfg.wDeviceType = Short.parseShort(value.toString()));
            properties.computeIfPresent("wDeviceProtocol", (key, value) -> rs485Cfg.wDeviceProtocol = Short.parseShort(value.toString()));
            properties.computeIfPresent("dwBaudRate", (key, value) -> rs485Cfg.dwBaudRate = Integer.parseInt(value.toString()));
            properties.computeIfPresent("byDataBit", (key, value) -> rs485Cfg.byDataBit = Byte.parseByte(value.toString()));
            properties.computeIfPresent("byStopBit", (key, value) -> rs485Cfg.byStopBit = Byte.parseByte(value.toString()));
            properties.computeIfPresent("byParity", (key, value) -> rs485Cfg.byParity = Byte.parseByte(value.toString()));
            properties.computeIfPresent("byFlowcontrol", (key, value) -> rs485Cfg.byFlowcontrol = Byte.parseByte(value.toString()));
            properties.computeIfPresent("byDuplex", (key, value) -> rs485Cfg.byDuplex = Byte.parseByte(value.toString()));
            properties.computeIfPresent("byWorkMode", (key, value) -> rs485Cfg.byWorkMode = Byte.parseByte(value.toString()));
            properties.computeIfPresent("byChannel", (key, value) -> rs485Cfg.byChannel = Byte.parseByte(value.toString()));
            properties.computeIfPresent("bySerialType", (key, value) -> rs485Cfg.bySerialType = Byte.parseByte(value.toString()));
            properties.computeIfPresent("byMode", (key, value) -> rs485Cfg.byMode = Byte.parseByte(value.toString()));
            properties.computeIfPresent("byOutputDataType", (key, value) -> rs485Cfg.byOutputDataType = Byte.parseByte(value.toString()));
            properties.computeIfPresent("byAddress", (key, value) -> rs485Cfg.byAddress = Byte.parseByte(value.toString()));

            return sdk.setRS485Cfg(userId, rs485Cfg);
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
            return
                    device.getSelfConfig(ConfigKey.of(DW_CHANNEL))
                            .map(Object::toString)
                            .map(Integer::valueOf)
                            .defaultIfEmpty(32)
                            .map(channel -> sdk.GetRecMonth(
                                    userId,
                                    (int) inputs.getOrDefault("wYear", LocalDate.now().getYear()),
                                    (int) inputs.getOrDefault("byMonth", LocalDate.now().getMonthValue()),
                                    channel))
                            .map(bytes -> {
                                int[] array = new int[bytes.length];
                                for (int i = 0; i < bytes.length; i++) {
                                    array[i] = bytes[i];
                                }
                                return array;
                            });
        }
        return Mono.empty();
    }
}
