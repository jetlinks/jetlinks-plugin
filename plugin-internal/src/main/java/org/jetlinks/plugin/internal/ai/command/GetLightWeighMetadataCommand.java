package org.jetlinks.plugin.internal.ai.command;

import com.google.common.collect.Lists;
import org.jetlinks.core.command.CommandHandler;
import org.jetlinks.core.command.CommandUtils;
import org.jetlinks.core.metadata.PropertyMetadata;
import org.jetlinks.core.metadata.SimpleFunctionMetadata;
import org.jetlinks.core.metadata.SimplePropertyMetadata;
import org.jetlinks.core.metadata.types.StringType;
import org.jetlinks.sdk.server.commons.cmd.OperationByIdCommand;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * @author gyl
 * @since 2.3
 */
public class GetLightWeighMetadataCommand extends OperationByIdCommand<Mono<Map</*命令id*/String, List<PropertyMetadata>>>, GetLightWeighMetadataCommand> {

    public static CommandHandler<GetLightWeighMetadataCommand, Mono<Map</*命令id*/String, List<PropertyMetadata>>>>
    createHandler(Function<GetLightWeighMetadataCommand,Mono<Map</*命令id*/String, List<PropertyMetadata>>>> handler) {
        SimpleFunctionMetadata metadata = new SimpleFunctionMetadata();
        metadata.setId(CommandUtils.getCommandIdByType(GetLightWeighMetadataCommand.class));
        metadata.setName("获取ai输出轻量配置定义");
        metadata.setInputs(Lists.newArrayList(
            SimplePropertyMetadata.of("id", "命令Id，为空时获取所有", StringType.GLOBAL)
        ));
        return CommandHandler
            .of(metadata,
                (req, spt) -> handler.apply(req),
                GetLightWeighMetadataCommand::new);
    }

}
