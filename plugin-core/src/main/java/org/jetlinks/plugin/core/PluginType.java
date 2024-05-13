package org.jetlinks.plugin.core;

/**
 * 插件类型,用于定义插件用途.
 *
 * @author zhouhao
 * @since 1.0
 */
public interface PluginType {

    /**
     * @return 类型标识
     */
    String getId();

    /**
     * @return 类型名称
     */
    String getName();

}
