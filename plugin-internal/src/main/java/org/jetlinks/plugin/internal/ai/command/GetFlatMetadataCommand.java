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
public class GetFlatMetadataCommand extends OperationByIdCommand<Mono<Map</*命令id*/String, List<PropertyMetadata>>>, GetFlatMetadataCommand> {

    public static CommandHandler<GetFlatMetadataCommand, Mono<Map</*命令id*/String, List<PropertyMetadata>>>>
    createHandler(Function<GetFlatMetadataCommand,Mono<Map</*命令id*/String, List<PropertyMetadata>>>> handler) {
        SimpleFunctionMetadata metadata = new SimpleFunctionMetadata();
        metadata.setId(CommandUtils.getCommandIdByType(GetFlatMetadataCommand.class));
        metadata.setName("获取ai输出平铺类型配置定义");
        metadata.setInputs(Lists.newArrayList(
            SimplePropertyMetadata.of("id", "命令Id，为空时获取所有", StringType.GLOBAL)
        ));
        return CommandHandler
            .of(metadata,
                (req, spt) -> handler.apply(req),
                GetFlatMetadataCommand::new);
    }

}
