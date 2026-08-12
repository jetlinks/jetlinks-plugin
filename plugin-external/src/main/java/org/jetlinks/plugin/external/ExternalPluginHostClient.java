package org.jetlinks.plugin.external;

import io.netty.buffer.ByteBufUtil;
import io.rsocket.Payload;
import io.rsocket.RSocket;
import io.rsocket.util.DefaultPayload;
import org.jetlinks.core.monitor.Monitor;
import org.jetlinks.plugin.protocol.ExternalPluginProtocol;
import org.jetlinks.plugin.protocol.WireCodec;
import org.jetlinks.plugin.protocol.WireError;
import org.jetlinks.plugin.protocol.WireInteraction;
import org.jetlinks.plugin.protocol.WireRequest;
import org.jetlinks.plugin.protocol.WireResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Language-neutral host service facade exposed to a plugin through {@link ExternalPluginContext}.
 * It owns no socket lifecycle; the enclosing plugin server still owns the accepted connection.
 */
public final class ExternalPluginHostClient {
    private final RSocket socket;
    private final WireCodec codec;
    private final String sessionId;
    private final String driverId;
    private final long generation;
    private final String contextId;
    private final long requestTimeoutMillis;

    public ExternalPluginHostClient(RSocket socket,
                                    WireCodec codec,
                                    String sessionId,
                                    String driverId,
                                    long generation,
                                    String contextId,
                                    long requestTimeoutMillis) {
        this.socket = Objects.requireNonNull(socket, "socket");
        this.codec = Objects.requireNonNull(codec, "codec");
        this.sessionId = Objects.requireNonNull(sessionId, "sessionId");
        this.driverId = Objects.requireNonNull(driverId, "driverId");
        this.contextId = Objects.requireNonNull(contextId, "contextId");
        if (generation < 0 || requestTimeoutMillis <= 0) {
            throw new IllegalArgumentException("generation and requestTimeoutMillis are invalid");
        }
        this.generation = generation;
        this.requestTimeoutMillis = requestTimeoutMillis;
    }

    public Mono<Object> executeToMono(String serviceId,
                                      String commandId,
                                      Map<String, Object> arguments) {
        return socket.requestResponse(payload(request(WireInteraction.REQUEST_RESPONSE,
                                                       ExternalPluginProtocol.ROUTE_HOST_COMMAND,
                                                       command(serviceId, commandId, arguments))))
            .map(this::decode)
            .timeout(java.time.Duration.ofMillis(requestTimeoutMillis))
            .flatMap(this::requireSuccess)
            .map(response -> response.getBody() == null ? null : codec.treeToValue(response.getBody(), Object.class));
    }

    /** Returns a view fenced to a plugin context id while retaining the same accepted socket. */
    public ExternalPluginHostClient withContext(String pluginContextId) {
        return new ExternalPluginHostClient(socket,
                                            codec,
                                            sessionId,
                                            driverId,
                                            generation,
                                            require(pluginContextId, "contextId"),
                                            requestTimeoutMillis);
    }

    public Flux<Object> executeToFlux(String serviceId,
                                      String commandId,
                                      Map<String, Object> arguments) {
        return socket.requestStream(payload(request(WireInteraction.REQUEST_STREAM,
                                                     ExternalPluginProtocol.ROUTE_HOST_COMMAND,
                                                     command(serviceId, commandId, arguments))))
            .map(this::decode)
            .timeout(java.time.Duration.ofMillis(requestTimeoutMillis))
            .handle((response, sink) -> {
                if (!response.isSuccess()) sink.error(new ExternalPluginRemoteException(response.getError()));
                else if (!response.isComplete()) sink.next(response.getBody() == null
                    ? null
                    : codec.treeToValue(response.getBody(), Object.class));
            });
    }

    /** Records a bounded event/error through the platform monitor bridge. */
    public Mono<Void> monitorEvent(String name, Object payload, Throwable error) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("name", name);
        if (payload != null) body.put("payload", payload);
        if (error != null) body.put("error", error.getClass().getSimpleName());
        return socket.requestResponse(payload(request(WireInteraction.REQUEST_RESPONSE,
                                                       ExternalPluginProtocol.ROUTE_HOST_MONITOR,
                                                       body)))
            .map(this::decode)
            .timeout(java.time.Duration.ofMillis(requestTimeoutMillis))
            .flatMap(this::requireSuccess)
            .then();
    }

    public Mono<Void> onClose() {
        return socket.onClose();
    }

    private Map<String, Object> command(String serviceId, String commandId, Map<String, Object> arguments) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("serviceId", require(serviceId, "serviceId"));
        body.put("commandId", require(commandId, "commandId"));
        body.put("arguments", arguments == null ? Collections.emptyMap() : arguments);
        return body;
    }

    private WireRequest request(WireInteraction interaction, String route, Object body) {
        Map<String, String> metadata = new LinkedHashMap<>();
        metadata.put("sessionId", sessionId);
        metadata.put("driverId", driverId);
        metadata.put("contextId", contextId);
        metadata.put("generation", String.valueOf(generation));
        return new WireRequest(interaction,
                                route,
                                contextId + "-" + System.nanoTime(),
                                System.currentTimeMillis() + requestTimeoutMillis,
                                metadata,
                                codec.valueToTree(body));
    }

    private Payload payload(WireRequest request) {
        return DefaultPayload.create(codec.encode(request));
    }

    private WireResponse decode(Payload payload) {
        try {
            return codec.decode(ByteBufUtil.getBytes(payload.data()), WireResponse.class);
        } finally {
            payload.release();
        }
    }

    private Mono<WireResponse> requireSuccess(WireResponse response) {
        return response.isSuccess()
            ? Mono.just(response)
            : Mono.error(new ExternalPluginRemoteException(response.getError()));
    }

    private static String require(String value, String name) {
        if (value == null || value.trim().isEmpty()) throw new IllegalArgumentException(name + " is required");
        return value;
    }

    /** Adapts a host client into the existing plugin context without changing plugin-core. */
    public static final class ExternalPluginRemoteException extends RuntimeException {
        public ExternalPluginRemoteException(WireError error) {
            super(error == null ? "host command failed" : error.getMessage());
        }
    }
}
