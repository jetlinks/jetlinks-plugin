package org.jetlinks.plugin.external;

import io.netty.buffer.ByteBufUtil;
import io.netty.channel.unix.DomainSocketAddress;
import io.rsocket.ConnectionSetupPayload;
import io.rsocket.Payload;
import io.rsocket.RSocket;
import io.rsocket.SocketAcceptor;
import io.rsocket.core.RSocketServer;
import io.rsocket.transport.ServerTransport;
import io.rsocket.transport.netty.server.CloseableChannel;
import io.rsocket.util.DefaultPayload;
import org.jetlinks.plugin.core.Description;
import org.jetlinks.plugin.core.Plugin;
import org.jetlinks.plugin.core.PluginContext;
import org.jetlinks.plugin.core.PluginDriver;
import org.jetlinks.plugin.protocol.ExternalPluginProtocol;
import org.jetlinks.plugin.protocol.WireCodec;
import org.jetlinks.plugin.protocol.WireError;
import org.jetlinks.plugin.protocol.WireInteraction;
import org.jetlinks.plugin.protocol.WireProtocolException;
import org.jetlinks.plugin.protocol.WireRequest;
import org.jetlinks.plugin.protocol.WireResponse;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.netty.tcp.TcpServer;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.Base64;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * RSocket server used by an external plugin process.
 *
 * <p>The server owns plugin instances created through the supplied driver. Request handling is
 * demand-driven and all inbound payloads are released immediately after decoding.</p>
 */
public final class ExternalPluginServer implements Disposable {
    private final PluginDriver driver;
    private final PluginContext context;
    private final WireCodec codec;
    private final String runtimeId;
    private final String driverId;
    private final long generation;
    private final byte[] credential;
    private final long maxResourceBytes;
    private final Map<String, Plugin> plugins = new ConcurrentHashMap<>();
    private final java.util.concurrent.atomic.AtomicBoolean draining = new java.util.concurrent.atomic.AtomicBoolean();
    private final java.util.concurrent.atomic.AtomicReference<Mono<Void>> shutdownSignal = new java.util.concurrent.atomic.AtomicReference<>();
    private volatile CloseableChannel channel;

    private ExternalPluginServer(Builder builder) {
        this.driver = builder.driver;
        this.context = builder.context;
        this.codec = builder.codec;
        this.runtimeId = builder.runtimeId;
        this.driverId = builder.driverId;
        this.generation = builder.generation;
        this.credential = builder.credential == null
            ? null
            : builder.credential.getBytes(StandardCharsets.UTF_8);
        this.maxResourceBytes = builder.maxResourceBytes;
    }

    public static Builder builder(PluginDriver driver, PluginContext context) {
        return new Builder(driver, context);
    }

    public Mono<ExternalPluginServer> bind(ServerTransport<CloseableChannel> transport) {
        Objects.requireNonNull(transport, "transport");
        return RSocketServer
            .create()
            .acceptor((setup, peer) -> accept(setup, peer))
            .fragment(codec.maxMessageBytes())
            .bind(transport)
            .doOnNext(bound -> channel = bound)
            .thenReturn(this);
    }

    /**
     * Binds the server to a Unix domain socket. The caller owns stale-socket cleanup so that a
     * runtime cannot accidentally remove a path outside its deployment directory.
     */
    public Mono<ExternalPluginServer> bindUnix(Path path) {
        Objects.requireNonNull(path, "path");
        Path normalized = path.toAbsolutePath().normalize();
        if (normalized.toString().isEmpty()) {
            return Mono.error(new IllegalArgumentException("unix socket path must not be empty"));
        }
        return bind(io.rsocket.transport.netty.server.TcpServerTransport.create(
            TcpServer.create().bindAddress(() -> new DomainSocketAddress(normalized.toString()))));
    }

    public Mono<Void> shutdown() {
        Mono<Void> current = shutdownSignal.get();
        if (current != null) {
            return current;
        }
        Mono<Void> created = Flux.fromIterable(plugins.values())
            .concatMap(Plugin::shutdown)
            .then(Mono.defer(() -> {
                CloseableChannel currentChannel = channel;
                if (currentChannel == null || currentChannel.isDisposed()) {
                    return Mono.empty();
                }
                currentChannel.dispose();
                return currentChannel.onClose();
            }))
            .cache();
        if (shutdownSignal.compareAndSet(null, created)) {
            return created;
        }
        return shutdownSignal.get();
    }

    /** Rejects new plugin instances while allowing existing calls to drain. */
    public void beginDrain() {
        draining.set(true);
    }

    public CloseableChannel channel() {
        return channel;
    }

    @Override
    public void dispose() {
        shutdown().subscribe(
            null,
            error -> System.getLogger(ExternalPluginServer.class.getName())
                .log(System.Logger.Level.WARNING, "external plugin shutdown failed", error));
    }

    @Override
    public boolean isDisposed() {
        CloseableChannel current = channel;
        return current != null && current.isDisposed();
    }

    private Mono<RSocket> accept(ConnectionSetupPayload setup, RSocket peer) {
        if (!ExternalPluginProtocol.DATA_MIME_TYPE.equals(setup.dataMimeType())) {
            return Mono.error(new WireProtocolException("data_mime_unsupported", "unsupported data mime type"));
        }
        SetupData setupData;
        // RSocket owns the setup payload lifecycle; releasing it here causes a double release
        // when setup authentication rejects the connection.
        setupData = new SetupData(codec.decode(ByteBufUtil.getBytes(setup.data()), org.jetlinks.plugin.protocol.SetupMessage.class));
        if (!runtimeId.equals(setupData.message.getRuntimeId())
            || !driverId.equals(setupData.message.getDriverId())
            || generation != setupData.message.getGeneration()
            || !matchesCredential(setupData.message.getCredential())) {
            return Mono.error(new WireProtocolException("setup_rejected", "setup identity or credential is invalid"));
        }
        return Mono.just(new RequestResponder(peer));
    }

    private boolean matchesCredential(String actual) {
        if (credential == null) {
            return actual == null || actual.isEmpty();
        }
        return actual != null && MessageDigest.isEqual(credential, actual.getBytes(StandardCharsets.UTF_8));
    }

    private WireResponse decodeResponse(Payload payload) {
        try {
            return codec.decode(ByteBufUtil.getBytes(payload.data()), WireResponse.class);
        } finally {
            payload.release();
        }
    }

    private WireRequest decodeRequest(Payload payload) {
        try {
            return codec.decode(ByteBufUtil.getBytes(payload.data()), WireRequest.class);
        } finally {
            payload.release();
        }
    }

    private Mono<WireResponse> executeResponse(WireRequest request, PluginContext requestContext) {
        return checkDeadline(request)
            .then(dispatch(request, requestContext))
            .timeout(remaining(request.getDeadlineEpochMillis()))
            .map(result -> WireResponse.success(request.getRequestId(), codec.valueToTree(result), true))
            .onErrorResume(error -> Mono.just(WireResponse.failure(request.getRequestId(), toWireError(error))));
    }

    private Flux<WireResponse> executeStream(WireRequest request) {
        return checkDeadline(request)
            .thenMany(dispatchStream(request))
            .timeout(remaining(request.getDeadlineEpochMillis()))
            .map(result -> WireResponse.success(request.getRequestId(), codec.valueToTree(result), false))
            .concatWith(Mono.just(WireResponse.success(request.getRequestId(), null, true)))
            .onErrorResume(error -> Flux.just(WireResponse.failure(request.getRequestId(), toWireError(error))));
    }

    private Mono<Object> dispatch(WireRequest request, PluginContext requestContext) {
        switch (request.getRoute()) {
            case ExternalPluginProtocol.ROUTE_DESCRIBE:
                return Mono.just(toDescription());
            case ExternalPluginProtocol.ROUTE_CREATE:
                return createPlugin(request, requestContext);
            case ExternalPluginProtocol.ROUTE_PLUGIN_START:
                return lifecycle(request, Plugin::start);
            case ExternalPluginProtocol.ROUTE_PLUGIN_PAUSE:
                return lifecycle(request, Plugin::pause);
            case ExternalPluginProtocol.ROUTE_PLUGIN_SHUTDOWN:
                return lifecycle(request, Plugin::shutdown)
                    .doOnSuccess(ignore -> removePlugin(request));
            case ExternalPluginProtocol.ROUTE_RUNTIME_HEALTH:
                return Mono.just(Collections.singletonMap("ready", !draining.get()));
            case ExternalPluginProtocol.ROUTE_RUNTIME_DRAIN:
                // Drain is an acknowledgement boundary, not a transport close. The platform
                // must receive this response before it shuts down plugin instances and socket.
                beginDrain();
                return Mono.just(Collections.singletonMap("draining", Boolean.TRUE));
            default:
                return Mono.error(new WireProtocolException("route_unsupported", "unsupported request route: " + request.getRoute()));
        }
    }

    private Flux<Object> dispatchStream(WireRequest request) {
        if (ExternalPluginProtocol.ROUTE_DRIVER_COMMAND.equals(request.getRoute())) {
            CommandCall call = codec.treeToValue(request.getBody(), CommandCall.class);
            return driver.executeToFlux(call.commandId, call.arguments == null
                ? Collections.emptyMap()
                : call.arguments);
        }
        if (ExternalPluginProtocol.ROUTE_PLUGIN_COMMAND.equals(request.getRoute())) {
            CommandCall call = codec.treeToValue(request.getBody(), CommandCall.class);
            Plugin target = plugin(call.pluginId);
            return target.executeToFlux(call.commandId, call.arguments == null
                ? Collections.emptyMap()
                : call.arguments);
        }
        if (ExternalPluginProtocol.ROUTE_DRIVER_RESOURCE.equals(request.getRoute())) {
            ResourceCall call = codec.treeToValue(request.getBody(), ResourceCall.class);
            validateResourceName(call.name);
            AtomicLong total = new AtomicLong();
            return driver.getResource(call.name).map(buffer -> toResourceChunk(buffer, total));
        }
        return Flux.error(new WireProtocolException("interaction_unsupported", "route requires request-response: " + request.getRoute()));
    }

    private Mono<Object> createPlugin(WireRequest request, PluginContext requestContext) {
        if (draining.get()) {
            return Mono.error(new WireProtocolException("runtime_draining", "runtime is draining"));
        }
        CreateCall call = codec.treeToValue(request.getBody(), CreateCall.class);
        if (call.pluginId == null || call.pluginId.trim().isEmpty()) {
            return Mono.error(new WireProtocolException("plugin_id_missing", "pluginId is required"));
        }
        if (plugins.containsKey(call.pluginId)) {
            return Mono.error(new WireProtocolException("plugin_exists", "plugin already exists"));
        }
        return driver.createPlugin(call.pluginId, requestContext)
            .doOnNext(plugin -> plugins.put(call.pluginId, plugin))
            .map(plugin -> Collections.singletonMap("pluginId", plugin.getId()));
    }

    private Mono<Object> lifecycle(WireRequest request,
                                   java.util.function.Function<Plugin, Mono<Void>> action) {
        LifecycleCall call = codec.treeToValue(request.getBody(), LifecycleCall.class);
        return action.apply(plugin(call.pluginId)).thenReturn(Collections.singletonMap("pluginId", call.pluginId));
    }

    private Plugin plugin(String id) {
        Plugin plugin = plugins.get(id);
        if (plugin == null) {
            throw new WireProtocolException("plugin_not_found", "plugin does not exist: " + id);
        }
        return plugin;
    }

    private void removePlugin(WireRequest request) {
        LifecycleCall call = codec.treeToValue(request.getBody(), LifecycleCall.class);
        plugins.remove(call.pluginId);
    }

    private Mono<Void> checkDeadline(WireRequest request) {
        long remaining = request.getDeadlineEpochMillis() - System.currentTimeMillis();
        return remaining <= 0
            ? Mono.error(new WireProtocolException("deadline_exceeded", "request deadline has expired"))
            : Mono.empty();
    }

    private Map<String, Object> toDescription() {
        Description description = driver.getDescription();
        Map<String, Object> result = new java.util.LinkedHashMap<>();
        result.put("id", description.getId());
        result.put("name", description.getName());
        result.put("description", description.getDescription());
        result.put("version", description.getVersion() == null ? null : description.getVersion().toString());
        result.put("type", driver.getType().getId());
        result.put("typeName", driver.getType().getName());
        result.put("others", description.getOthers());
        return result;
    }

    private ResourceChunk toResourceChunk(DataBuffer buffer, AtomicLong total) {
        try {
            int size = buffer.readableByteCount();
            long current = total.addAndGet(size);
            if (current > maxResourceBytes) {
                throw new WireProtocolException("resource_too_large", "resource exceeds the configured limit");
            }
            byte[] bytes = new byte[size];
            buffer.read(bytes);
            return new ResourceChunk(Base64.getEncoder().encodeToString(bytes), current);
        } finally {
            DataBufferUtils.release(buffer);
        }
    }

    private void validateResourceName(String name) {
        if (name == null || name.isEmpty() || name.indexOf('\0') >= 0 || name.contains("..")
            || name.startsWith("/") || name.startsWith("\\")) {
            throw new WireProtocolException("resource_name_invalid", "resource name is invalid");
        }
    }

    private WireError toWireError(Throwable error) {
        Throwable cause = error instanceof java.util.concurrent.CompletionException && error.getCause() != null
            ? error.getCause()
            : error;
        if (cause instanceof WireProtocolException) {
            WireProtocolException protocolError = (WireProtocolException) cause;
            return new WireError(protocolError.getCode(), protocolError.getMessage(), false, Collections.emptyMap());
        }
        if (cause instanceof java.util.concurrent.TimeoutException) {
            return new WireError("deadline_exceeded", "request deadline has expired", false, Collections.emptyMap());
        }
        return new WireError("plugin_execution_failed", cause.getClass().getSimpleName(), false, Collections.emptyMap());
    }

    private Duration remaining(long deadlineEpochMillis) {
        long millis = deadlineEpochMillis - System.currentTimeMillis();
        return Duration.ofMillis(Math.max(1, millis));
    }

    private final class RequestResponder implements RSocket {
        private final RSocket peer;
        private final ExternalPluginHostClient host;
        private final PluginContext requestContext;

        private RequestResponder(RSocket peer) {
            this.peer = peer;
            this.host = new ExternalPluginHostClient(
                peer,
                codec,
                runtimeId,
                driverId,
                generation,
                "driver",
                10_000);
            this.requestContext = ExternalPluginContext.withHost(context, host);
        }

        @Override
        public Mono<Payload> requestResponse(Payload payload) {
            WireRequest request = decodeRequest(payload);
            if (request.getInteraction() != WireInteraction.REQUEST_RESPONSE) {
                return Mono.just(DefaultPayload.create(codec.encode(WireResponse.failure(
                    request.getRequestId(), new WireError("interaction_mismatch", "interaction does not match request type", false, Collections.emptyMap())))));
            }
            return executeResponse(request, contextFor(request)).map(response -> DefaultPayload.create(codec.encode(response)));
        }

        @Override
        public Flux<Payload> requestStream(Payload payload) {
            WireRequest request = decodeRequest(payload);
            return executeStream(request).map(response -> DefaultPayload.create(codec.encode(response)));
        }

        @Override
        public Flux<Payload> requestChannel(org.reactivestreams.Publisher<Payload> payloads) {
            return Flux.from(payloads)
                .map(ExternalPluginServer.this::decodeRequest)
                .doOnNext(request -> {
                    if (request.getInteraction() != WireInteraction.REQUEST_CHANNEL) {
                        throw new WireProtocolException("interaction_mismatch", "request channel requires REQUEST_CHANNEL interaction");
                    }
                })
                .concatMap(request -> ExternalPluginProtocol.ROUTE_DRIVER_COMMAND.equals(request.getRoute())
                    ? executeStream(request)
                        .map(response -> DefaultPayload.create(codec.encode(response)))
                    : executeResponse(request, contextFor(request))
                        .map(response -> DefaultPayload.create(codec.encode(response))));
        }

        @Override
        public Mono<Void> onClose() {
            return peer.onClose();
        }

        private PluginContext contextFor(WireRequest request) {
            if (!ExternalPluginProtocol.ROUTE_CREATE.equals(request.getRoute())) {
                return requestContext;
            }
            CreateCall call = codec.treeToValue(request.getBody(), CreateCall.class);
            if (call.pluginId == null || call.pluginId.trim().isEmpty()) {
                return requestContext;
            }
            return ExternalPluginContext.withHost(context, host.withContext(call.pluginId));
        }
    }

    public static final class Builder {
        private final PluginDriver driver;
        private final PluginContext context;
        private WireCodec codec = new WireCodec();
        private String runtimeId = "runtime";
        private String driverId = "driver";
        private long generation;
        private String credential;
        private long maxResourceBytes = 16L * 1024 * 1024;

        private Builder(PluginDriver driver, PluginContext context) {
            this.driver = Objects.requireNonNull(driver, "driver");
            this.context = Objects.requireNonNull(context, "context");
        }

        public Builder codec(WireCodec codec) {
            this.codec = Objects.requireNonNull(codec, "codec");
            return this;
        }

        public Builder identity(String runtimeId, String driverId, long generation) {
            this.runtimeId = Objects.requireNonNull(runtimeId, "runtimeId");
            this.driverId = Objects.requireNonNull(driverId, "driverId");
            this.generation = generation;
            return this;
        }

        public Builder credential(String credential) {
            this.credential = credential;
            return this;
        }

        public Builder maxResourceBytes(long maxResourceBytes) {
            if (maxResourceBytes <= 0) {
                throw new IllegalArgumentException("maxResourceBytes must be positive");
            }
            this.maxResourceBytes = maxResourceBytes;
            return this;
        }

        public ExternalPluginServer build() {
            return new ExternalPluginServer(this);
        }
    }

    private static final class SetupData {
        private final org.jetlinks.plugin.protocol.SetupMessage message;

        private SetupData(org.jetlinks.plugin.protocol.SetupMessage message) {
            this.message = message;
        }
    }

    public static final class CreateCall {
        public String pluginId;
        public Map<String, Object> configuration;

        public CreateCall() {
        }

        public CreateCall(String pluginId) {
            this.pluginId = pluginId;
        }
    }

    public static final class LifecycleCall {
        public String pluginId;

        public LifecycleCall() {
        }

        public LifecycleCall(String pluginId) {
            this.pluginId = pluginId;
        }
    }

    public static final class CommandCall {
        public String pluginId;
        public String commandId;
        public Map<String, Object> arguments;

        public CommandCall() {
        }

        public CommandCall(String pluginId, String commandId, Map<String, ?> arguments) {
            this.pluginId = pluginId;
            this.commandId = commandId;
            this.arguments = arguments == null ? null : new java.util.LinkedHashMap<>(arguments);
        }
    }

    public static final class ResourceCall {
        public String name;

        public ResourceCall() {
        }

        public ResourceCall(String name) {
            this.name = name;
        }
    }

    public static final class ResourceChunk {
        public final String dataBase64;
        public final long totalBytes;

        public ResourceChunk(String dataBase64, long totalBytes) {
            this.dataBase64 = dataBase64;
            this.totalBytes = totalBytes;
        }
    }
}
