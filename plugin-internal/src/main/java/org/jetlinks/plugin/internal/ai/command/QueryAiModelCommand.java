package org.jetlinks.plugin.internal.ai.command;

import io.swagger.v3.oas.annotations.media.Schema;
import org.jetlinks.core.command.AbstractCommand;
import org.jetlinks.core.command.AbstractConvertCommand;
import org.jetlinks.core.command.CommandMetadataResolver;
import org.jetlinks.core.metadata.FunctionMetadata;
import org.jetlinks.sdk.server.ai.model.AiModelPortrait;
import reactor.core.publisher.Flux;

@Schema(title = "查询AI模型")
public class QueryAiModelCommand extends AbstractCommand<Flux<AiModelPortrait>, QueryAiModelCommand> {

    public static FunctionMetadata metadata() {
        return CommandMetadataResolver.resolve(QueryAiModelCommand.class);
    }

    @Schema(title = "模型id", description = "为null时返回所有模型")
    public String getModuleId() {
        return ((String) readable().get("moduleId"));
    }

    public void setModuleId(String moduleId) {
        writable().put("moduleId", moduleId);
    }

}
