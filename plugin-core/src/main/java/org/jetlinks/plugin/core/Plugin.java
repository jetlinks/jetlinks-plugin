package org.jetlinks.plugin.core;

import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.ByteBuffer;
import java.util.function.BiConsumer;

/**
 * 插件实例接口
 *
 * @author zhouhao
 * @since 1.0.0
 */
public interface Plugin extends Wrapper {

    /**
     * @return 插件实例ID
     */
    String getId();

    /**
     * 插件类型,不同的插件类型支持的命令不同,如网络服务插件等.
     *
     * @return 插件类型
     */
    PluginType getType();

    /**
     * @return 插件状态
     */
    PluginState getState();

    /**
     * @return 启动插件
     */
    Mono<Void> start();

    /**
     * @return 暂停插件
     */
    Mono<Void> pause();

    /**
     * @return 停止插件
     */
    Mono<Void> shutdown();

    /**
     * 执行命令,通过命令来实现同步的操作。
     * 不同的插件类型支持的命令不同
     *
     * @return 执行结果
     * @see org.jetlinks.plugin.core.exception.IllegalPluginStateException
     */
    <R> R execute(PluginCommand<R> command);

    /**
     * 根据命令ID来创建一个命令实例,用于动态创建命令等操作,
     * 如果不支持此命令将返回{@link Mono#empty()}
     *
     * @param commandId 命令ID
     * @param <R>       命令返回类型
     * @return 命令
     */
    <R> Mono<PluginCommand<R>> createCommand(String commandId);

    /**
     * 获取支持的命令信息
     *
     * @return 命令信息
     */
    Flux<Description> getSupportCommands();

    /**
     * 监听插件状态变更,通过调用返回值{@link Disposable#dispose()}取消监听
     *
     * @param listener 监听器: &lt;之前的状态,最新的状态&gt;
     * @return Disposable
     */
    Disposable doOnSateChanged(BiConsumer<PluginState, PluginState> listener);

    /**
     * 监听停止事件
     *
     * @param listener 监听器
     * @return Disposable
     */
    default Disposable doOnStop(Disposable listener) {
        return doOnSateChanged((before, after) -> {
            if (after == PluginState.stopped) {
                listener.dispose();
            }
        });
    }

    /**
     * 获取插件资源,如文件等
     *
     * @param name 资源名称
     * @return 文件字节缓存流
     */
    default Flux<ByteBuffer> getResource(String name) {
        return Flux.empty();
    }

}
