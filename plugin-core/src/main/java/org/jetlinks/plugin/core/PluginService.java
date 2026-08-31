package org.jetlinks.plugin.core;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.Map;

/**
 * Transport-neutral view of one platform command service.
 *
 * <p>The service id is the platform command contract (for example
 * {@code deviceService:device}). Implementations may be backed by a local command support or by
 * the platform host bridge; plugin code must not depend on either implementation.</p>
 *
 * @author zhouhao
 * @since 1.0
 */
public interface PluginService {

    /**
     * @return canonical platform service id
     */
    String getServiceId();

    /**
     * Executes a request-response command.
     *
     * @param commandId command id declared by the plugin manifest
     * @param arguments JSON-compatible command arguments
     * @return one command result
     */
    Mono<Object> call(String commandId, Map<String, Object> arguments);

    /**
     * Executes a streaming command. Backpressure and cancellation are preserved by the runtime.
     *
     * @param commandId command id declared by the plugin manifest
     * @param arguments JSON-compatible command arguments
     * @return command result stream
     */
    Flux<Object> stream(String commandId, Map<String, Object> arguments);

    /**
     * Returns the JSON-compatible command metadata exposed by the platform.
     * Implementations that do not have a remote service description may keep the default
     * unsupported result; external runtimes implement this through the host bridge.
     *
     * @return service description, including the allowlisted commands
     * @since 1.0
     */
    default Mono<Map<String, Object>> describe() {
        return Mono.error(new UnsupportedOperationException("service description is unavailable: " + getServiceId()));
    }

    /**
     * Creates a reference with an independent resource target. The target is part of authorization
     * context and must not be concatenated into the service id.
     *
     * @param target dynamic resource target
     * @return target-scoped service reference
     */
    default PluginService target(Map<String, Object> target) {
        throw new UnsupportedOperationException("target-scoped platform service is unavailable: " + getServiceId());
    }

    default Mono<Object> call(String commandId) {
        return call(commandId, Collections.emptyMap());
    }

    default Flux<Object> stream(String commandId) {
        return stream(commandId, Collections.emptyMap());
    }
}
