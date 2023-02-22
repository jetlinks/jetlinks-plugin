package org.jetlinks.plugin.internal.device;

import org.jetlinks.core.device.DeviceOperator;
import org.jetlinks.core.device.DeviceProductOperator;
import org.jetlinks.core.device.DeviceRegistry;
import org.jetlinks.core.message.DeviceMessage;
import org.jetlinks.core.metadata.ConfigMetadata;
import org.jetlinks.plugin.core.AbstractPlugin;
import org.jetlinks.plugin.core.PluginContext;
import org.jetlinks.plugin.core.PluginScheduler;
import org.jetlinks.plugin.core.PluginType;
import org.jetlinks.plugin.internal.InternalPluginType;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;

/**
 * 设备接入网关插件,用于自定义设备接入,如: 定时{@link PluginScheduler#interval(String, Mono, Duration)}主动采集设备数据等场景.
 *
 * @author zhouhao
 * @since 1.0
 */
public abstract class DeviceGatewayPlugin extends AbstractPlugin {

    private final PluginDeviceGatewayService gatewayService;
    private final PluginDeviceManager deviceManager;

    protected final DeviceRegistry registry;

    public DeviceGatewayPlugin(String id, PluginContext context) {
        super(id, context);

        gatewayService = context
                .services()
                .getServiceNow(PluginDeviceGatewayService.class);

        registry = context
                .services()
                .getServiceNow(DeviceRegistry.class);

        deviceManager = context
                .services()
                .getServiceNow(PluginDeviceManager.class);

    }

    public Publisher<? extends DeviceMessage> execute(DeviceMessage message) {
        return Flux.empty();
    }


    /**
     * 执行物模型指令,并返回执行结果.
     *
     * @param productId 产品ID {@link DeviceProduct#getId()}
     * @param device    设备信息
     * @param message   指令
     * @return 执行结果
     */
    public Publisher<? extends DeviceMessage>  execute(String productId,
                                       DeviceOperator device,
                                       DeviceMessage message) {
        return execute(message);
    }

    /**
     * 获取产品配置的元数据信息,在产品详情界面会根据此信息来展示拓展信息输入框.
     * <p>
     * 保存后可通过{@link DeviceOperator#getConfig(String)}或者{@link DeviceOperator#getSelfConfig(String)}获取配置值
     *
     * @param productId 产品ID
     * @return ConfigMetadata
     */

    public Mono<ConfigMetadata> getProductConfigMetadata(String productId) {
        return Mono.empty();
    }

    /**
     * 获取设备配置的元数据信息,在设备详情界面会根据此信息来展示拓展信息输入框.
     * <p>
     * 保存后可通过{@link DeviceOperator#getConfig(String)}或者{@link DeviceOperator#getSelfConfig(String)}获取配置值
     *
     * @param productId 产品ID
     * @return ConfigMetadata
     */
    public Mono<ConfigMetadata> getDeviceConfigMetadata(String productId) {
        return Mono.empty();
    }

    /**
     * 处理解析后的设备消息,用于将第三方系统或者sdk的消息转为平台统一消息格式后,将消息传递给平台.
     * 请使用平台内置的消息实现,请勿自己实现{@link DeviceMessage}接口.
     *
     * @param message 消息
     * @return 异步处理结果
     * @see org.jetlinks.core.message.DeviceRegisterMessage
     * @see org.jetlinks.core.message.property.ReportPropertyMessage
     */
    protected final Mono<Void> handleMessage(DeviceMessage message) {
        return gatewayService.handleMessage(this, message);
    }

    /**
     * 获取设备的状态,返回empty表示不支持获取.
     *
     * @param device 设备操作接口
     * @return 异步设备状态
     * @see org.jetlinks.core.device.DeviceState
     */
    public Mono<Byte> getDeviceState(DeviceOperator device) {
        return Mono.empty();
    }

    /**
     * 扫描某个产品下的设备
     *
     * @param productId 插件定义的产品ID
     * @return 设备列表
     */
    public Flux<Device> scanDevices(String productId) {
        return Flux.empty();
    }

    /**
     * 获取平台中的设备列表
     *
     * @return 设备列表
     */
    protected final Flux<DeviceOperator> getPlatformDevices() {
        return deviceManager.getDevices(this);
    }

    /**
     * 获取平台中指定产品ID(插件中定义的产品ID)的设备列表
     *
     * @return 设备列表
     * @see DeviceProduct#getId()
     */
    protected final Flux<DeviceOperator> getPlatformDevices(String productId) {
        return deviceManager.getDevices(this, productId);
    }

    /**
     * 当平台的设备注册(保存)时调用
     *
     * @param device 设备操作接口
     * @return 异步结果
     */
    public Mono<Void> doOnDeviceRegister(DeviceOperator device) {
        return Mono.empty();
    }

    /**
     * 当平台的产品注册(保存)时调用
     *
     * @param product 产品操作接口
     * @return 异步结果
     */
    public Mono<Void> doOnProductRegister(DeviceProductOperator product) {
        return Mono.empty();
    }

    @Override
    public final PluginType getType() {
        return InternalPluginType.deviceGateway;
    }
}
