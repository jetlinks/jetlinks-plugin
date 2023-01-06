package org.jetlinks.plugin.internal.device;

import lombok.Getter;
import lombok.Setter;
import org.jetlinks.core.metadata.DeviceMetadata;

@Getter
@Setter
public class DeviceProduct {
    private String id;

    private String name;

    private String description;

    private DeviceMetadata metadata;
}
