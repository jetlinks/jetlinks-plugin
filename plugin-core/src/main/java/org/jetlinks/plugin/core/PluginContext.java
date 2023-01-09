package org.jetlinks.plugin.core;

import java.io.File;

/**
 * 插件运行上下文
 *
 * @author zhouhao
 * @since 1.0
 */
public interface PluginContext {

    /**
     * 插件注册中心,用于获取其他插件信息
     *
     * @return 插件注册中心
     */
    PluginRegistry registry();

    /**
     * 服务注册中心,用于获取其他服务信息,如:获取spring容器中的bean等
     *
     * @return 注册中心
     */
    ServiceRegistry services();

    /**
     * 插件运行环境变量信息
     *
     * @return 环境变量
     */
    PluginEnvironment environment();

    /**
     * 插件监控信息
     *
     * @return 监控信息
     */
    PluginMetrics metrics();

    /**
     * 调度器,用于执行定时任务等操作
     *
     * @return 调度器
     */
    PluginScheduler scheduler();

    /**
     * 插件工作路径,需要创建临时文件时,可以使用此路径
     *
     * @return workDir
     */
    File workDir();

}
