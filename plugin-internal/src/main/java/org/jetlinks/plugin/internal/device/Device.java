package org.jetlinks.plugin.internal.device;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class Device {
    private String id;

    private String productId;

    private String name;

    private String address;

    private String description;

    private Map<String, Object> others;
}
