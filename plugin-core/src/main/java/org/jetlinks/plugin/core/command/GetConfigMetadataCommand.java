package org.jetlinks.plugin.core.command;

import org.jetlinks.core.command.AbstractCommand;
import org.jetlinks.core.metadata.ConfigMetadata;
import org.jetlinks.plugin.core.PluginDriver;
import reactor.core.publisher.Mono;

/**
 * 获取配置信息命令
 *
 * @see PluginDriver#getDescription()
 */
public class GetConfigMetadataCommand extends AbstractCommand<Mono<ConfigMetadata>, GetConfigMetadataCommand> {
}
