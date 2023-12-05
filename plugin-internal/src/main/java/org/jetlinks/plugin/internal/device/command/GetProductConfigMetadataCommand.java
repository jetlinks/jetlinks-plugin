package org.jetlinks.plugin.internal.device.command;

import org.jetlinks.core.command.AbstractCommand;
import org.jetlinks.core.command.CommandHandler;
import org.jetlinks.core.command.CommandUtils;
import org.jetlinks.core.metadata.ConfigMetadata;
import org.jetlinks.core.metadata.SimpleFunctionMetadata;
import org.jetlinks.core.metadata.SimplePropertyMetadata;
import org.jetlinks.core.metadata.types.ArrayType;
import org.jetlinks.core.metadata.types.ObjectType;
import org.jetlinks.core.metadata.types.StringType;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.function.Function;

/**
 * 获取产品配置定义命令
 *
 * @author zhouhao
 */
public class GetProductConfigMetadataCommand extends AbstractCommand<Mono<ConfigMetadata>, GetProductConfigMetadataCommand> {

    public GetProductConfigMetadataCommand withProductId(String productId) {
        return with("productId", productId);
    }

    public String getProductId() {
        return (String) readable().get("productId");
    }

    public static CommandHandler<GetProductConfigMetadataCommand, Mono<ConfigMetadata>>
    createHandler(Function<GetProductConfigMetadataCommand, Mono<ConfigMetadata>> handler) {
        SimpleFunctionMetadata metadata = new SimpleFunctionMetadata();
        metadata.setId(CommandUtils.getCommandIdByType(GetProductConfigMetadataCommand.class));
        metadata.setName("获取产品配置定义");
        metadata.setInputs(Collections.singletonList(
            SimplePropertyMetadata.of("productId", "产品ID", StringType.GLOBAL)
        ));
        metadata.setOutput(GetDeviceConfigMetadataCommand.createConfigMetadataType());
        return CommandHandler
            .of(metadata,
                (req, spt) -> handler.apply(req),
                GetProductConfigMetadataCommand::new);
    }

}
