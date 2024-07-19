package org.jetlinks.plugin.internal.device;

import org.jetlinks.core.command.AbstractCommandSupport;
import org.jetlinks.core.command.CommandHandler;
import org.jetlinks.core.metadata.ConfigMetadata;
import org.jetlinks.plugin.core.command.GetConfigMetadataCommand;
import org.jetlinks.plugin.internal.InternalPluginDriver;
import reactor.core.publisher.Mono;

import java.util.Map;

public abstract class AbstractPluginDriver extends AbstractCommandSupport implements InternalPluginDriver {

    public AbstractPluginDriver() {
        registerHandler(
            CommandHandler.of(
                GetConfigMetadataCommand::metadata,
                (cmd, ignore) -> getConfigMetadata(cmd),
                GetConfigMetadataCommand::new
            )
        );
    }

    protected Mono<ConfigMetadata> getConfigMetadata(GetConfigMetadataCommand cmd) {
        Map<String, Object> others = getDescription().getOthers();
        if (others == null) {
            return Mono.empty();
        }
        return Mono.justOrEmpty((ConfigMetadata) others.get(InternalPluginDriver.PLUGIN_CONFIG_METADATA));
    }
}
