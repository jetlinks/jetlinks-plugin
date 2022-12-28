package org.jetlinks.plugin.internal.device;

import org.jetlinks.core.device.DeviceRegistry;
import org.jetlinks.core.message.DeviceMessage;
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

    protected final Mono<Void> handleMessage(DeviceMessage message) {
        return gatewayService.handleMessage(this, message);
    }

    /**
     * 获取设备的状态,返回empty表示不支持获取.
     *
     * @param deviceId 设备ID
     * @return 异步设备状态
     * @see org.jetlinks.core.device.DeviceState
     */
    public Mono<Byte> getDeviceState(String deviceId) {
        return Mono.empty();
    }


    @Override
    public final PluginType getType() {
        return InternalPluginType.deviceGateway;
    }
}
