package org.jetlinks.plugin.core.command;

import org.jetlinks.core.command.AbstractCommand;
import org.jetlinks.core.metadata.ConfigMetadata;
import reactor.core.publisher.Mono;

public class GetConfigMetadataCommand extends AbstractCommand<Mono<ConfigMetadata>,GetConfigMetadataCommand> {
}
