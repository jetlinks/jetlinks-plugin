package org.jetlinks.plugin.internal.device.command;

import org.jetlinks.core.command.AbstractCommand;
import org.jetlinks.core.command.CommandHandler;
import org.jetlinks.core.command.CommandUtils;
import org.jetlinks.core.metadata.ConfigMetadata;
import org.jetlinks.core.metadata.DataType;
import org.jetlinks.core.metadata.SimpleFunctionMetadata;
import org.jetlinks.core.metadata.SimplePropertyMetadata;
import org.jetlinks.core.metadata.types.ArrayType;
import org.jetlinks.core.metadata.types.ObjectType;
import org.jetlinks.core.metadata.types.StringType;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.Collections;
import java.util.function.Function;

/**
 * 获取设备配置定义命令
 *
 * @author zhouhao
 */
public class GetDeviceConfigMetadataCommand extends AbstractCommand<Mono<ConfigMetadata>, GetDeviceConfigMetadataCommand> {

    public GetDeviceConfigMetadataCommand withDeviceId(String deviceId) {
        return with("deviceId", deviceId);
    }

    public String getDeviceId() {
        return (String) readable().get("deviceId");
    }

    public static DataType createConfigMetadataType() {
        return new ObjectType()
            .addProperty("name", "配置名称", StringType.GLOBAL)
            .addProperty("description", "配置说明", StringType.GLOBAL)
            .addProperty("properties", "配置属性", new ArrayType()
                .elementType(new ObjectType()
                                 .addProperty("id", "属性ID", StringType.GLOBAL)
                                 .addProperty("name", "名称", StringType.GLOBAL)
                ));
    }

    public static CommandHandler<GetDeviceConfigMetadataCommand, Mono<ConfigMetadata>>
    createHandler(Function<GetDeviceConfigMetadataCommand, Mono<ConfigMetadata>> handler) {
        SimpleFunctionMetadata metadata = new SimpleFunctionMetadata();
        metadata.setId(CommandUtils.getCommandIdByType(GetDeviceConfigMetadataCommand.class));
        metadata.setName("获取设备配置定义");
        metadata.setInputs(Collections.singletonList(
            SimplePropertyMetadata.of("deviceId", "设备ID", StringType.GLOBAL)
        ));
        metadata.setOutput(createConfigMetadataType());
        return CommandHandler
            .of(metadata,
                (req, spt) -> handler.apply(req),
                GetDeviceConfigMetadataCommand::new);
    }
}
