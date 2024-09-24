package org.jetlinks.plugin.internal.ai.command;

import io.swagger.v3.oas.annotations.media.Schema;
import org.jetlinks.core.command.AbstractConvertCommand;
import org.jetlinks.core.command.CommandMetadataResolver;
import org.jetlinks.core.metadata.FunctionMetadata;
import reactor.core.publisher.Mono;

@Schema(title = "停止任务")
public class StopAiTaskCommand extends AbstractConvertCommand<Mono<Void>, StopAiTaskCommand> {

    public static FunctionMetadata metadata() {
        return CommandMetadataResolver.resolve(StopAiTaskCommand.class);
    }

    @Schema(title = "模型id")
    public String getModuleId() {
        return ((String) readable().get("moduleId"));
    }

    public void setModuleId(String moduleId) {
        writable().put("moduleId", moduleId);
    }

    @Schema(title = "任务id")
    public String getTaskId() {
        return ((String) readable().get("taskId"));
    }

    public void setTaskId(String taskId) {
        writable().put("taskId", taskId);
    }
}
