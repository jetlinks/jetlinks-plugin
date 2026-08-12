package org.jetlinks.plugin.external;

import io.netty.buffer.ByteBufUtil;
import io.netty.channel.unix.DomainSocketAddress;
import io.rsocket.Payload;
import io.rsocket.RSocket;
import io.rsocket.SocketAcceptor;
import io.rsocket.core.RSocketConnector;
import io.rsocket.transport.ClientTransport;
import io.rsocket.util.DefaultPayload;
import org.jetlinks.plugin.protocol.ExternalPluginProtocol;
import org.jetlinks.plugin.protocol.SetupMessage;
import org.jetlinks.plugin.protocol.WireCodec;
import org.jetlinks.plugin.protocol.WireRequest;
import org.jetlinks.plugin.protocol.WireResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import org.reactivestreams.Publisher;
import reactor.netty.tcp.TcpClient;

import java.time.Duration;
import java.nio.file.Path;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Non-blocking client for the external plugin wire contract.
 *
 * <p>The client preserves RSocket demand and cancellation. It does not replay requests after a
 * disconnect because plugin commands may have side effects.</p>
 */
public final class ExternalPluginClient implements AutoCloseable {
    private final RSocket socket;
    private final WireCodec codec;
    private final AtomicBoolean closed = new AtomicBoolean();

    private ExternalPluginClient(RSocket socket, WireCodec codec) {
        this.socket = socket;
        this.codec = codec;
    }

    public static Mono<ExternalPluginClient> connect(ClientTransport transport,
                                                     SetupMessage setup,
                                                     WireCodec codec) {
        return connect(transport, setup, codec, null);
    }

    /**
     * Connects with an optional host responder.  A null responder keeps the original one-way
     * client behavior; when present, plugin-initiated request-response/stream/channel calls are
     * decoded and delegated without changing the public plugin command API.
     */
    public static Mono<ExternalPluginClient> connect(ClientTransport transport,
                                                     SetupMessage setup,
                                                     WireCodec codec,
                                                     ExternalPluginResponder responder) {
        Objects.requireNonNull(transport, "transport");
        Objects.requireNonNull(setup, "setup");
        WireCodec actualCodec = codec == null ? new WireCodec(setup.getMaxFrameBytes()) : codec;
        Payload setupPayload = DefaultPayload.create(actualCodec.encode(setup));
        RSocketConnector connector = RSocketConnector
            .create()
            .dataMimeType(ExternalPluginProtocol.DATA_MIME_TYPE)
            .metadataMimeType(ExternalPluginProtocol.METADATA_MIME_TYPE)
            .fragment(setup.getMaxFrameBytes())
            .setupPayload(setupPayload);
        if (responder != null) {
            connector = connector.acceptor((setupPayloadValue, peer) -> Mono.just(
                new HostResponderSocket(peer, actualCodec, responder)));
        }
        return connector.connect(transport)
            .map(socket -> new ExternalPluginClient(socket, actualCodec));
    }

    /**
     * Connects to a Unix domain socket. The address stays in the transport layer and is never
     * encoded into a setup message or a command payload.
     */
    public static Mono<ExternalPluginClient> connectUnix(Path path,
                                                         SetupMessage setup,
                                                         WireCodec codec) {
        return connectUnix(path, setup, codec, null);
    }

    public static Mono<ExternalPluginClient> connectUnix(Path path,
                                                         SetupMessage setup,
                                                         WireCodec codec,
                                                         ExternalPluginResponder responder) {
        Objects.requireNonNull(path, "path");
        Path normalized = path.toAbsolutePath().normalize();
        if (normalized.toString().isEmpty()) {
            return Mono.error(new IllegalArgumentException("unix socket path must not be empty"));
        }
        ClientTransport transport = io.rsocket.transport.netty.client.TcpClientTransport.create(
            TcpClient.create().remoteAddress(() -> new DomainSocketAddress(normalized.toString())));
        return connect(transport, setup, codec, responder);
    }

    public Mono<WireResponse> requestResponse(WireRequest request) {
        return ensureOpen()
            .then(Mono.defer(() -> socket
                .requestResponse(toPayload(request))
                .map(this::decodeResponse)
                .timeout(remaining(request.getDeadlineEpochMillis()))));
    }

    public Flux<WireResponse> requestStream(WireRequest request) {
        return Flux.defer(() -> ensureOpen()
            .thenMany(socket
                .requestStream(toPayload(request))
                .map(this::decodeResponse)
                .timeout(remaining(request.getDeadlineEpochMillis()))));
    }

    public Flux<WireResponse> requestChannel(org.reactivestreams.Publisher<WireRequest> requests) {
        return Flux.defer(() -> ensureOpen()
            .thenMany(socket.requestChannel(
                Flux.from(requests)
                    .map(request -> {
                        if (request.getInteraction() != org.jetlinks.plugin.protocol.WireInteraction.REQUEST_CHANNEL) {
                            throw new IllegalArgumentException("request channel requires REQUEST_CHANNEL interaction");
                        }
                        return toPayload(request);
                    })))
            .map(this::decodeResponse));
    }

    public Mono<Void> onClose() {
        return socket.onClose();
    }

    private static final class HostResponderSocket implements RSocket {
        private final RSocket peer;
        private final WireCodec codec;
        private final ExternalPluginResponder responder;

        private HostResponderSocket(RSocket peer,
                                    WireCodec codec,
                                    ExternalPluginResponder responder) {
            this.peer = peer;
            this.codec = codec;
            this.responder = responder;
        }

        @Override
        public Mono<Payload> requestResponse(Payload payload) {
            WireRequest request = decode(payload);
            return responder.requestResponse(request)
                .map(response -> DefaultPayload.create(codec.encode(response)));
        }

        @Override
        public Flux<Payload> requestStream(Payload payload) {
            WireRequest request = decode(payload);
            return responder.requestStream(request)
                .map(response -> DefaultPayload.create(codec.encode(response)));
        }

        @Override
        public Flux<Payload> requestChannel(Publisher<Payload> payloads) {
            return Flux.from(payloads)
                .map(this::decode)
                .transform(responder::requestChannel)
                .map(response -> DefaultPayload.create(codec.encode(response)));
        }

        @Override
        public Mono<Void> onClose() {
            return peer.onClose();
        }

        private WireRequest decode(Payload payload) {
            try {
                return codec.decode(ByteBufUtil.getBytes(payload.data()), WireRequest.class);
            } finally {
                payload.release();
            }
        }
    }

    @Override
    public void close() {
        if (closed.compareAndSet(false, true)) {
            socket.dispose();
        }
    }

    private Mono<Void> ensureOpen() {
        return closed.get()
            ? Mono.error(new IllegalStateException("external plugin client is closed"))
            : Mono.empty();
    }

    private Payload toPayload(WireRequest request) {
        return DefaultPayload.create(codec.encode(request));
    }

    private WireResponse decodeResponse(Payload payload) {
        try {
            return codec.decode(ByteBufUtil.getBytes(payload.data()), WireResponse.class);
        } finally {
            payload.release();
        }
    }

    private Duration remaining(long deadlineEpochMillis) {
        long millis = deadlineEpochMillis - System.currentTimeMillis();
        if (millis <= 0) {
            return Duration.ofMillis(1);
        }
        return Duration.ofMillis(millis);
    }
}
