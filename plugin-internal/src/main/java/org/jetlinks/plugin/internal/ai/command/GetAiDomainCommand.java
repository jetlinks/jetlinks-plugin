package org.jetlinks.plugin.internal.ai.command;

import io.swagger.v3.oas.annotations.media.Schema;
import org.jetlinks.core.command.AbstractCommand;
import org.jetlinks.core.command.CommandMetadataResolver;
import org.jetlinks.core.metadata.FunctionMetadata;
import org.jetlinks.sdk.server.ai.AiDomain;
import reactor.core.publisher.Mono;


@Schema(title = "获取支持的AI领域")
public class GetAiDomainCommand extends AbstractCommand<Mono<AiDomain>,GetAiDomainCommand> {

    public static FunctionMetadata metadata(){
        return CommandMetadataResolver.resolve(GetAiDomainCommand.class);
    }
}
