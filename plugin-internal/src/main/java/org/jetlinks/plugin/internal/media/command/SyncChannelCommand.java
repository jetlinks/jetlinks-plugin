package org.jetlinks.plugin.internal.media.command;

import org.jetlinks.core.command.AbstractCommand;
import org.jetlinks.core.command.CommandUtils;
import org.jetlinks.core.metadata.FunctionMetadata;
import org.jetlinks.core.metadata.PropertyMetadata;
import org.jetlinks.core.metadata.SimpleFunctionMetadata;
import org.jetlinks.core.metadata.SimplePropertyMetadata;
import org.jetlinks.core.metadata.types.StringType;
import org.jetlinks.plugin.internal.media.MediaSdkChannel;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 更新通道.
 *
 * @author zhangji 2023/10/12
 */
public class SyncChannelCommand extends AbstractCommand<Flux<MediaSdkChannel>, SyncChannelCommand> {

    public static final String DEVICE_ID = "deviceId";

    public String deviceId() {
        return (String) readable().get(DEVICE_ID);
    }

    public static FunctionMetadata metadata() {
        return metadata(null);
    }

    public static FunctionMetadata metadata(List<PropertyMetadata> terms) {
        SimpleFunctionMetadata metadata = new SimpleFunctionMetadata();
        metadata.setId(CommandUtils.getCommandIdByType(SyncChannelCommand.class));
        metadata.setName("更新通道");

        if (terms == null) {
            terms = new ArrayList<>();
        }
        terms.add(SimplePropertyMetadata.of(DEVICE_ID, "设备ID", StringType.GLOBAL));
        metadata.setExpands(Collections.singletonMap("terms", terms));
        return metadata;
    }

}
