package org.jetlinks.plugin.internal;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.hswebframework.web.dict.I18nEnumDict;
import org.jetlinks.plugin.core.PluginType;

@Getter
@AllArgsConstructor
public enum InternalPluginType implements PluginType, I18nEnumDict<String> {

    deviceGateway("设备接入网关"),
    thingsManager("物管理"),
    mediaGateway("视频设备接入网关");

    private final String name;

    @Override
    public String getId() {
        return name();
    }

    @Override
    public String getValue() {
        return getId();
    }

    @Override
    public String getText() {
        return getName();
    }
}
