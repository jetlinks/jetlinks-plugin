package org.jetlinks.plugin.internal.ai.command;

import io.swagger.v3.oas.annotations.media.Schema;
import org.hswebframework.web.bean.FastBeanCopier;
import org.jetlinks.core.command.AbstractConvertCommand;
import org.jetlinks.core.command.CommandMetadataResolver;
import org.jetlinks.core.metadata.FunctionMetadata;
import org.jetlinks.sdk.server.ai.model.AiModelInfo;
import org.jetlinks.sdk.server.ai.model.AiModelPortrait;
import reactor.core.publisher.Mono;


@Schema(title = "添加AI模型")
public class AddAiModelCommand extends AbstractConvertCommand<Mono<AiModelPortrait>, AddAiModelCommand> {

    @Schema(title = "模型信息")
    public AiModelInfo getModel() {
        Object val = this.readable().get("model");
        return val instanceof AiModelInfo ? ((AiModelInfo) val) : FastBeanCopier.copy(val, new AiModelInfo());
    }

    public void setModel(AiModelInfo model) {
        this.writable().put("model", model);
    }
    public static FunctionMetadata metadata() {
        return CommandMetadataResolver.resolve(AddAiModelCommand.class);
    }
}
