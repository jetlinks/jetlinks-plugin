package org.jetlinks.plugin.internal.device;

import org.jetlinks.core.device.DeviceOperator;
import org.jetlinks.core.device.DeviceRegistry;
import org.jetlinks.core.message.DeviceMessage;
import org.jetlinks.core.metadata.ConfigMetadata;
import org.jetlinks.plugin.core.*;
import org.jetlinks.plugin.internal.InternalPluginType;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collections;

public abstract class DeviceGatewayPlugin extends AbstractPlugin {

    private final PluginDeviceGatewayService gatewayService;

    protected final DeviceRegistry registry;

    public DeviceGatewayPlugin(String id, PluginContext context) {
        super(id, context);

        gatewayService = context.services().getServiceNow(PluginDeviceGatewayService.class);

        registry = context.services().getServiceNow(DeviceRegistry.class);

        registerCommand(DeviceMessageSendCommand.ID,
                        CommandHandler.of(
                                () -> Description.of(DeviceMessageSendCommand.ID, "设备消息发送", "向设备发送指令", Collections.emptyMap()),
                                cmd -> execute(cmd.getMessage()),
                                DeviceMessageSendCommand::new));

    }

    public abstract Flux<DeviceMessage> execute(DeviceMessage message);

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
     * 获取设备配置的元数据信息,在设备或者产品详情界面会根据此信息来展示拓展信息输入框.
     * <p>
     * 保持后可通过{@link DeviceOperator#getConfig(String)}或者{@link DeviceOperator#getSelfConfig(String)}获取配置值
     *
     * @return ConfigMetadata
     */
    protected Mono<ConfigMetadata> getDeviceConfigMetadata() {
        return Mono.empty();
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
     * 获取此插件支持的产品信息,用于一个插件支持接入多个设备时
     *
     * @return void
     */
    public abstract Flux<DeviceProduct> getSupportProducts();


    @Override
    public final PluginType getType() {
        return InternalPluginType.deviceGateway;
    }
}
