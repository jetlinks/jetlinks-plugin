package org.jetlinks.plugin.core;

import org.jetlinks.core.command.CommandSupport;
import reactor.core.Disposable;
import reactor.core.publisher.Mono;

import java.util.function.BiConsumer;

/**
 * 插件实例接口
 *
 * @author zhouhao
 * @since 1.0.0
 */
public interface Plugin extends CommandSupport {

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


}
