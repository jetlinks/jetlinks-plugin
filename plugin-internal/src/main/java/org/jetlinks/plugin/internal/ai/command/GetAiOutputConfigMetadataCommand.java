package org.jetlinks.plugin.internal.ai.command;

import com.google.common.collect.Lists;
import org.jetlinks.core.command.AbstractCommand;
import org.jetlinks.core.command.CommandHandler;
import org.jetlinks.core.command.CommandUtils;
import org.jetlinks.core.metadata.DataType;
import org.jetlinks.core.metadata.PropertyMetadata;
import org.jetlinks.core.metadata.SimpleFunctionMetadata;
import org.jetlinks.core.metadata.SimplePropertyMetadata;
import org.jetlinks.core.metadata.types.ArrayType;
import org.jetlinks.core.metadata.types.ObjectType;
import org.jetlinks.core.metadata.types.StringType;
import org.jetlinks.plugin.internal.device.command.GetDeviceConfigMetadataCommand;
import org.jetlinks.sdk.server.ai.AiOutput;
import reactor.core.publisher.Flux;

import java.util.function.Function;

/**
 * 获取ai输出配置定义命令
 *
 * @author zhouhao
 */
public class GetAiOutputConfigMetadataCommand extends AbstractCommand<Flux<PropertyMetadata>, GetAiOutputConfigMetadataCommand> {

    public GetAiOutputConfigMetadataCommand withCommandId(String commandId) {
        return with("commandId", commandId);
    }

    public String getCommandId() {
        return (String) readable().get("commandId");
    }


    public GetAiOutputConfigMetadataCommand withType(Type type) {
        return with("type", type.name());
    }

    public Type getType() {
        return Type.valueOf((String) readable().get("type"));
    }

    public static CommandHandler<GetAiOutputConfigMetadataCommand, Flux<PropertyMetadata>>
    createHandler(Function<GetAiOutputConfigMetadataCommand, Flux<PropertyMetadata>> handler) {
        SimpleFunctionMetadata metadata = new SimpleFunctionMetadata();
        metadata.setId(CommandUtils.getCommandIdByType(GetAiOutputConfigMetadataCommand.class));
        metadata.setName("获取ai输出各类型配置定义");
        metadata.setInputs(Lists.newArrayList(
            SimplePropertyMetadata.of("commandId", "命令Id", StringType.GLOBAL),
            SimplePropertyMetadata.of("type", "输出类型", StringType.GLOBAL)
        ));
        return CommandHandler
            .of(metadata,
                (req, spt) -> handler.apply(req),
                GetAiOutputConfigMetadataCommand::new);
    }


    public enum Type {
        /**
         * @see AiOutput#flat()
         */
        flat,
        /**
         * @see AiOutput#lightWeighFlat()
         */
        lightWeighFlat,
        /**
         * @see AiOutput#toLightWeighMap()
         */
        lightWeigh;
    }

}
