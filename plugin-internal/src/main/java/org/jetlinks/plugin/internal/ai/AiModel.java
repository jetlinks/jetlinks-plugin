package org.jetlinks.plugin.internal.ai;

import org.jetlinks.sdk.server.ai.InternalTaskTarget;
import reactor.core.publisher.Mono;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

/**
 * AI模型信息
 */
public interface AiModel {

    /**
     * 获取模型ID
     *
     * @return 模型ID
     */
    String getId();

    /**
     * 模型名称
     *
     * @return 模型名称
     */
    String getName();

    /**
     * @return 模型提供商
     */
    String getProvider();

    /**
     * 获取模型的任务目标
     *
     * @return 任务目标
     * @see InternalTaskTarget
     */
    String getTarget();

    /**
     * 读取文件内容
     *
     * @return 文件内容
     */
    Mono<InputStream> readContent();

    /**
     * 写出模型文件到指定的输出流
     *
     * @param output 输出流
     * @return void
     */
    Mono<Void> writeContentTo(OutputStream output);

    /**
     * 获取其他信息
     */
    Map<String, Object> getOthers();
}
