package org.jetlinks.plugin.internal.ai.command;

import io.swagger.v3.oas.annotations.media.Schema;
import org.jetlinks.core.command.AbstractConvertCommand;
import org.jetlinks.core.command.CommandMetadataResolver;
import org.jetlinks.core.metadata.FunctionMetadata;
import org.jetlinks.sdk.server.ai.model.AiModelPortrait;
import reactor.core.publisher.Flux;

@Schema(title = "查询ai模型，指定id，则查指定模型，反之则查全部")
public class QueryAiModelCommand extends AbstractConvertCommand<Flux<AiModelPortrait>, QueryAiModelCommand> {

    public static FunctionMetadata metadata() {
        return CommandMetadataResolver.resolve(QueryAiModelCommand.class);
    }

    @Schema(title = "模型id")
    public String getModuleId() {
        return ((String) readable().get("moduleId"));
    }

    public void setModuleId(String moduleId) {
        writable().put("moduleId", moduleId);
    }

}
