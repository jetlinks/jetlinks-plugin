package org.jetlinks.plugin.core.command;

import io.swagger.v3.oas.annotations.media.Schema;
import org.jetlinks.core.command.AbstractCommand;
import org.jetlinks.core.command.CommandMetadataResolver;
import org.jetlinks.core.metadata.ConfigMetadata;
import org.jetlinks.core.metadata.FunctionMetadata;
import org.jetlinks.plugin.core.PluginDriver;
import reactor.core.publisher.Mono;

/**
 * 获取配置信息命令
 *
 * @see PluginDriver#getDescription()
 */
@Schema(title = "获取配置信息")
public class GetConfigMetadataCommand extends AbstractCommand<Mono<ConfigMetadata>, GetConfigMetadataCommand> {


    public static FunctionMetadata metadata(){
        return CommandMetadataResolver.resolve(GetConfigMetadataCommand.class);
    }

}
