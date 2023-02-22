package org.jetlinks.plugin.internal.device;

import org.jetlinks.core.device.DeviceOperator;
import reactor.core.publisher.Flux;

public interface PluginDeviceManager {

    Flux<DeviceOperator> getDevices(DeviceGatewayPlugin plugin);

    Flux<DeviceOperator> getDevices(DeviceGatewayPlugin plugin, String productId);

}
