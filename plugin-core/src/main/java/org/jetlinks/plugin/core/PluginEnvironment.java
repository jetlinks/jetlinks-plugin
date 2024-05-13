package org.jetlinks.plugin.core;

import java.util.Map;
import java.util.Optional;

/**
 * 查询运行环境,用于获取配置信息等.
 *
 * @author zhouhao
 * @since 1.0
 */
public interface PluginEnvironment {

    /**
     * 获取配置信息
     *
     * @param key KEY
     * @return 配置值
     * @see Optional
     */
    Optional<String> getProperty(String key);

    /**
     * 获取配置信息为指定的类型
     *
     * @param key  key
     * @param type 类型
     * @param <T>  T
     * @return 配置值
     */
    <T> Optional<T> getProperty(String key, Class<T> type);

    /**
     * @return 获取全部配置信息
     */
    Map<String, Object> getProperties();


}
