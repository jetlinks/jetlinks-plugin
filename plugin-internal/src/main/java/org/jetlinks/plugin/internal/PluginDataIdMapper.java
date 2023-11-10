package org.jetlinks.plugin.internal;

import org.jetlinks.core.device.DeviceThingType;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface PluginDataIdMapper {

    String TYPE_DEVICE = DeviceThingType.device.getId();
    String TYPE_PRODUCT = "product";


    /**
     * 根据插件中的数据id,获取JetLinks平台内部的id.
     *
     * @param type       类型,如: device,product
     * @param pluginId   插件ID
     * @param externalId 外部ID
     * @return 平台内部ID
     */
    Mono<String> getInternalId(String type, String pluginId, String externalId);

    /**
     * 根据平台内部的数据Id,获取插件中的数据ID
     *
     * @param type       类型,如: device,product
     * @param pluginId   插件ID
     * @param internalId 平台内部ID
     * @return 插件中的数据ID
     */
    Mono<String> getExternalId(String type, String pluginId, String internalId);

}
