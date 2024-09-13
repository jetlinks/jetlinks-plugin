package org.jetlinks.plugin.internal.ai.command;

import io.swagger.v3.oas.annotations.media.Schema;
import org.jetlinks.core.command.CommandUtils;
import org.jetlinks.core.metadata.FunctionMetadata;
import org.jetlinks.core.metadata.SimpleFunctionMetadata;
import org.jetlinks.core.metadata.SimplePropertyMetadata;
import org.jetlinks.core.metadata.types.StringType;
import org.jetlinks.sdk.server.ai.model.AiModelOperationResults;
import org.jetlinks.sdk.server.commons.cmd.OperationByIdCommand;
import reactor.core.publisher.Mono;

import java.util.Collections;


@Schema(title = "移除AI模型")
public class RemoveAiModelCommand extends OperationByIdCommand<Mono<AiModelOperationResults>, RemoveAiModelCommand> {

    public static FunctionMetadata metadata(){
        SimpleFunctionMetadata metadata = new SimpleFunctionMetadata();
        //RemoveAiModel
        metadata.setId(CommandUtils.getCommandIdByType(RemoveAiModelCommand.class));
        metadata.setName("移除AI模型");
        metadata.setInputs(Collections.singletonList(SimplePropertyMetadata.of("id", "Id", StringType.GLOBAL)));
        return metadata;
    }
}
