package org.jetlinks.plugin.internal;

import org.jetlinks.plugin.core.Description;
import org.jetlinks.plugin.core.PluginDriver;
import org.jetlinks.plugin.internal.device.DeviceGatewayPluginDriver;

import javax.annotation.Nonnull;

public interface InternalPluginDriver extends PluginDriver {

    /**
     * 插件配置元数据key,用于在{@link Description#getOthers()}中定义创建插件所需的配置信息.
     * 用于前端根据配置动态生成配置表单.
     * @see org.jetlinks.core.metadata.ConfigMetadata
     * @see Description#getOthers()
     */
    String PLUGIN_CONFIG_METADATA = "configMetadata";

    /**
     * @return Description
     * @see DeviceGatewayPluginDriver#PLUGIN_CONFIG_METADATA
     */
    @Nonnull
    @Override
    Description getDescription();

}
