package org.jetlinks.plugin.internal.device;

import lombok.Getter;
import lombok.Setter;
import org.jetlinks.core.metadata.DeviceMetadata;

@Getter
@Setter
public class DeviceProduct {
    /**
     * 分类
     */
    private String group;

    /**
     * ID
     */
    private String id;

    /**
     * 名称
     */
    private String name;

    /**
     * 说明
     */
    private String description;

    /**
     * 物模型
     */
    private DeviceMetadata metadata;
}
