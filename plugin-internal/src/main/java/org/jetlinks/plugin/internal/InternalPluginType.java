package org.jetlinks.plugin.internal;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.hswebframework.web.dict.I18nEnumDict;
import org.jetlinks.core.command.Command;
import org.jetlinks.plugin.core.PluginContext;
import org.jetlinks.plugin.core.PluginType;

@Getter
@AllArgsConstructor
public enum InternalPluginType implements PluginType, I18nEnumDict<String> {

    deviceGateway("设备接入网关"),
    thingsManager("物管理"),
    ruleEngine("规则引擎"),

    /**
     * 独立运行的插件,不依托平台其他功能独立运行的插件.
     * 通过命令{@link org.jetlinks.plugin.core.Plugin#execute(Command)}来提供对外能力.
     * 通过{@link PluginContext#services()}来访问平台能力.
     *
     * @see org.jetlinks.plugin.internal.functional.FunctionalPlugin
     */
    standalone("独立运行");

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
