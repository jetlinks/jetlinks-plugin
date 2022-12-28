package org.jetlinks.plugin.internal.device;

import org.jetlinks.core.message.DeviceMessage;
import reactor.core.publisher.Mono;

public interface PluginDeviceGatewayService {


    Mono<Void> handleMessage(DeviceGatewayPlugin plugin,
                             DeviceMessage message);

}
