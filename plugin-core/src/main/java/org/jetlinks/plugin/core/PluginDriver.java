package org.jetlinks.plugin.core;

import org.jetlinks.core.Wrapper;
import org.jetlinks.core.command.Command;
import org.jetlinks.core.command.CommandSupport;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;

/**
 * 插件驱动,用于基于插件配置创建插件实例.
 *
 * @author zhouhao
 * @since 1.0
 */
public interface PluginDriver extends CommandSupport, Wrapper {

    /**
     * @return 插件描述
     */
    @Nonnull
    Description getDescription();

    /**
     * @return 插件类型
     */
    @Nonnull
    PluginType getType();

    /**
     * 创建插件实例
     *
     * @param pluginId 插件ID
     * @param context  插件上下文
     * @return 插件示例
     */
    @Nonnull
    Mono<? extends Plugin> createPlugin(@Nonnull String pluginId,
                                        @Nonnull PluginContext context);

    /**
     * 执行驱动命令
     *
     * @param command 命令
     * @param <R>     结果参数
     * @return 命令结果
     */
    @Nonnull
    @Override
    default <R> R execute(@Nonnull Command<R> command) {
        throw new UnsupportedOperationException("unsupported command:" + command);
    }

    /**
     * 获取插件内部资源
     *
     * @param name 名称
     * @return 资源数据
     */
    default Flux<DataBuffer> getResource(String name) {
        return DataBufferUtils
            .read(new ClassPathResource(name,
                                        this.getClass().getClassLoader()),
                  new DefaultDataBufferFactory(),
                  4096);
    }
}
