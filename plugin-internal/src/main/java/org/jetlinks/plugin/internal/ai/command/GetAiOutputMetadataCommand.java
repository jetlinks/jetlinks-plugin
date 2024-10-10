package org.jetlinks.plugin.internal.ai.command;

import com.google.common.collect.Lists;
import io.swagger.v3.oas.annotations.media.Schema;
import org.jetlinks.core.command.CommandHandler;
import org.jetlinks.core.command.CommandUtils;
import org.jetlinks.core.metadata.PropertyMetadata;
import org.jetlinks.core.metadata.SimpleFunctionMetadata;
import org.jetlinks.core.metadata.SimplePropertyMetadata;
import org.jetlinks.core.metadata.types.StringType;
import org.jetlinks.plugin.internal.ai.AiOutputMetadataType;
import org.jetlinks.sdk.server.commons.cmd.OperationByIdCommand;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * @author gyl
 * @since 2.3
 */
public class GetAiOutputMetadataCommand extends OperationByIdCommand<Mono<Map</*命令id*/String, List<PropertyMetadata>>>, GetAiOutputMetadataCommand> {


    @Schema(title = "输出类型")
    public AiOutputMetadataType getType() {
        return AiOutputMetadataType.valueOf((String) readable().get("type"));
    }

    public void setType(AiOutputMetadataType type) {
        writable().put("type", type.name());
    }

    public static CommandHandler<GetAiOutputMetadataCommand, Mono<Map</*命令id*/String, List<PropertyMetadata>>>>
    createHandler(Function<GetAiOutputMetadataCommand, Mono<Map</*命令id*/String, List<PropertyMetadata>>>> handler) {
        SimpleFunctionMetadata metadata = new SimpleFunctionMetadata();
        metadata.setId(CommandUtils.getCommandIdByType(GetAiOutputMetadataCommand.class));
        metadata.setName("获取ai输出指定类型配置定义");
        metadata.setInputs(Lists.newArrayList(
            SimplePropertyMetadata.of("id", "命令Id，为空时获取所有", StringType.GLOBAL),
            SimplePropertyMetadata.of("type", "输出类型", StringType.GLOBAL)

        ));
        return CommandHandler
            .of(metadata,
                (req, spt) -> handler.apply(req),
                GetAiOutputMetadataCommand::new);
    }

}
