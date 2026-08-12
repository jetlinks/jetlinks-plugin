package org.jetlinks.plugin.external;

import io.rsocket.transport.netty.client.TcpClientTransport;
import io.rsocket.transport.netty.server.TcpServerTransport;
import org.jetlinks.core.command.Command;
import org.jetlinks.plugin.core.Description;
import org.jetlinks.plugin.core.Plugin;
import org.jetlinks.plugin.core.PluginContext;
import org.jetlinks.plugin.core.PluginDriver;
import org.jetlinks.plugin.core.PluginState;
import org.jetlinks.plugin.core.PluginType;
import org.jetlinks.plugin.internal.StaticPluginContext;
import org.jetlinks.plugin.protocol.ExternalPluginProtocol;
import org.jetlinks.plugin.protocol.SetupMessage;
import org.jetlinks.plugin.protocol.WireCodec;
import org.jetlinks.plugin.protocol.WireInteraction;
import org.jetlinks.plugin.protocol.WireRequest;
import org.jetlinks.plugin.protocol.WireResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExternalPluginServerTest {
    private ExternalPluginServer server;
    private ExternalPluginClient client;

    @AfterEach
    void close() {
        if (client != null) {
            client.close();
        }
        if (server != null) {
            server.shutdown().block(Duration.ofSeconds(5));
        }
    }

    @Test
    void shouldServeLifecycleCommandsAndStreamsOverTcp() {
        WireCodec codec = new WireCodec();
        server = ExternalPluginServer
            .builder(new TestDriver(), new StaticPluginContext())
            .identity("runtime-1", "driver-1", 3)
            .credential("secret")
            .build();
        server.bind(TcpServerTransport.create("127.0.0.1", 0)).block(Duration.ofSeconds(5));

        SetupMessage setup = setup("secret");
        client = ExternalPluginClient
            .connect(TcpClientTransport.create("127.0.0.1", server.channel().address().getPort()), setup, codec)
            .block(Duration.ofSeconds(5));

        WireResponse describe = client.requestResponse(request(
                codec, WireInteraction.REQUEST_RESPONSE, ExternalPluginProtocol.ROUTE_DESCRIBE, "describe", null))
            .block(Duration.ofSeconds(5));
        assertTrue(describe.isSuccess());
        assertEquals("driver-1", describe.getBody().get("id").asText());

        WireResponse created = client.requestResponse(request(
                codec, WireInteraction.REQUEST_RESPONSE, ExternalPluginProtocol.ROUTE_CREATE, "create",
                new ExternalPluginServer.CreateCall("plugin-1")))
            .block(Duration.ofSeconds(5));
        assertTrue(created.isSuccess());

        WireResponse started = client.requestResponse(request(
                codec, WireInteraction.REQUEST_RESPONSE, ExternalPluginProtocol.ROUTE_PLUGIN_START, "start",
                new ExternalPluginServer.LifecycleCall("plugin-1")))
            .block(Duration.ofSeconds(5));
        assertTrue(started.isSuccess());

        StepVerifier.create(client.requestStream(request(
                codec, WireInteraction.REQUEST_STREAM, ExternalPluginProtocol.ROUTE_PLUGIN_COMMAND, "stream",
                new ExternalPluginServer.CommandCall("plugin-1", "echo", Collections.singletonMap("value", "ok")))))
            .assertNext(item -> assertEquals("ok", item.getBody().get("value").asText()))
            .assertNext(item -> assertTrue(item.isComplete()))
            .verifyComplete();

        WireResponse stopped = client.requestResponse(request(
                codec, WireInteraction.REQUEST_RESPONSE, ExternalPluginProtocol.ROUTE_PLUGIN_SHUTDOWN, "stop",
                new ExternalPluginServer.LifecycleCall("plugin-1")))
            .block(Duration.ofSeconds(5));
        assertTrue(stopped.isSuccess());
    }

    @Test
    void shouldRejectInvalidCredentialBeforeServingRequests() {
        server = ExternalPluginServer
            .builder(new TestDriver(), new StaticPluginContext())
            .identity("runtime-1", "driver-1", 3)
            .credential("secret")
            .build();
        server.bind(TcpServerTransport.create("127.0.0.1", 0)).block(Duration.ofSeconds(5));

        client = ExternalPluginClient
            .connect(TcpClientTransport.create("127.0.0.1", server.channel().address().getPort()), setup("wrong"), null)
            .block(Duration.ofSeconds(5));
        StepVerifier.create(client.requestResponse(request(
                new WireCodec(), WireInteraction.REQUEST_RESPONSE, ExternalPluginProtocol.ROUTE_DESCRIBE, "rejected", null)))
            .expectError()
            .verify(Duration.ofSeconds(5));
    }

    @Test
    void shouldAllowPluginToInvokeFencedHostService() {
        WireCodec codec = new WireCodec();
        server = ExternalPluginServer
            .builder(new TestDriver(true), new StaticPluginContext())
            .identity("runtime-1", "driver-1", 3)
            .credential("secret")
            .build();
        server.bind(TcpServerTransport.create("127.0.0.1", 0)).block(Duration.ofSeconds(5));

        org.jetlinks.plugin.external.ExternalPluginResponder responder = new org.jetlinks.plugin.external.ExternalPluginResponder() {
            @Override
            public Mono<WireResponse> requestResponse(WireRequest request) {
                assertEquals("runtime-1", request.getMetadata().get("sessionId"));
                assertEquals("plugin-1", request.getMetadata().get("contextId"));
                assertEquals("3", request.getMetadata().get("generation"));
                return Mono.just(WireResponse.success(request.getRequestId(), codec.valueToTree(Collections.singletonMap("ok", true)), true));
            }

            @Override
            public Flux<WireResponse> requestStream(WireRequest request) {
                return Flux.error(new UnsupportedOperationException("not used"));
            }

            @Override
            public Flux<WireResponse> requestChannel(org.reactivestreams.Publisher<WireRequest> requests) {
                return Flux.error(new UnsupportedOperationException("not used"));
            }
        };
        client = ExternalPluginClient
            .connect(TcpClientTransport.create("127.0.0.1", server.channel().address().getPort()),
                     setup("secret"),
                     codec,
                     responder)
            .block(Duration.ofSeconds(5));

        assertTrue(client.requestResponse(request(codec,
                                                   WireInteraction.REQUEST_RESPONSE,
                                                   ExternalPluginProtocol.ROUTE_CREATE,
                                                   "create-host",
                                                   new ExternalPluginServer.CreateCall("plugin-1")))
                          .block(Duration.ofSeconds(5))
                          .isSuccess());
        assertTrue(client.requestResponse(request(codec,
                                                   WireInteraction.REQUEST_RESPONSE,
                                                   ExternalPluginProtocol.ROUTE_PLUGIN_START,
                                                   "start-host",
                                                   new ExternalPluginServer.LifecycleCall("plugin-1")))
                          .block(Duration.ofSeconds(5))
                          .isSuccess());
    }

    private static SetupMessage setup(String credential) {
        SetupMessage setup = new SetupMessage();
        setup.setRuntimeId("runtime-1");
        setup.setDriverId("driver-1");
        setup.setGeneration(3);
        setup.setCredential(credential);
        setup.setSdkVersion("test");
        return setup;
    }

    private static WireRequest request(WireCodec codec,
                                       WireInteraction interaction,
                                       String route,
                                       String requestId,
                                       Object body) {
        return new WireRequest(interaction,
                                route,
                                requestId,
                                System.currentTimeMillis() + 10_000,
                                Collections.emptyMap(),
                                body == null ? null : codec.valueToTree(body));
    }

    private static final class TestDriver implements PluginDriver {
        private final boolean callHost;
        private final PluginType type = new PluginType() {
            @Override
            public String getId() {
                return "test";
            }

            @Override
            public String getName() {
                return "Test";
            }
        };

        private TestDriver() {
            this(false);
        }

        private TestDriver(boolean callHost) {
            this.callHost = callHost;
        }

        @Override
        public Description getDescription() {
            return Description.of("driver-1", "Test Driver", "test", null, null, Collections.emptyMap());
        }

        @Override
        public PluginType getType() {
            return type;
        }

        @Override
        public Mono<? extends Plugin> createPlugin(String pluginId, PluginContext context) {
            return Mono.just(new TestPlugin(pluginId, type, context, callHost));
        }

        @Override
        public <R> R execute(Command<R> command) {
            throw new UnsupportedOperationException("unsupported");
        }

        @Override
        public Flux<Object> executeToFlux(String commandId, Map<String, Object> arguments) {
            return Flux.just(Collections.singletonMap("value", arguments.get("value")));
        }

        @Override
        public boolean isWrapperFor(Class<?> type) {
            return false;
        }

        @Override
        public <T> T unwrap(Class<T> type) {
            throw new IllegalArgumentException("not wrapped");
        }

        @Override
        public Flux<DataBuffer> getResource(String name) {
            return Flux.just(DefaultDataBufferFactory.sharedInstance.wrap(new byte[]{1, 2, 3}));
        }
    }

    private static final class TestPlugin implements Plugin {
        private final String id;
        private final PluginType type;
        private final PluginContext context;
        private final boolean callHost;
        private final AtomicReference<PluginState> state = new AtomicReference<>(PluginState.stopped);
        private final List<java.util.function.BiConsumer<PluginState, PluginState>> listeners = new CopyOnWriteArrayList<>();

        private TestPlugin(String id, PluginType type, PluginContext context, boolean callHost) {
            this.id = id;
            this.type = type;
            this.context = context;
            this.callHost = callHost;
        }

        @Override
        public String getId() {
            return id;
        }

        @Override
        public PluginType getType() {
            return type;
        }

        @Override
        public PluginState getState() {
            return state.get();
        }

        @Override
        public Mono<Void> start() {
            if (!callHost) {
                change(PluginState.running);
                return Mono.empty();
            }
            return context.services()
                .getService(ExternalPluginHostClient.class)
                .orElseThrow(() -> new IllegalStateException("host client is missing"))
                .executeToMono("test.service", "echo", Collections.singletonMap("value", "ok"))
                .doOnSuccess(ignore -> change(PluginState.running))
                .then();
        }

        @Override
        public Mono<Void> pause() {
            change(PluginState.paused);
            return Mono.empty();
        }

        @Override
        public Mono<Void> shutdown() {
            change(PluginState.stopped);
            return Mono.empty();
        }

        @Override
        public Disposable doOnSateChanged(java.util.function.BiConsumer<PluginState, PluginState> listener) {
            listeners.add(listener);
            return () -> listeners.remove(listener);
        }

        @Override
        public <R> R execute(Command<R> command) {
            throw new UnsupportedOperationException("unsupported");
        }

        @Override
        public Flux<Object> executeToFlux(String commandId, Map<String, Object> arguments) {
            return "echo".equals(commandId)
                ? Flux.just(Collections.singletonMap("value", arguments.get("value")))
                : Flux.error(new IllegalArgumentException("unknown command"));
        }

        @Override
        public boolean isWrapperFor(Class<?> type) {
            return false;
        }

        @Override
        public <T> T unwrap(Class<T> type) {
            throw new IllegalArgumentException("not wrapped");
        }

        private void change(PluginState next) {
            PluginState previous = state.getAndSet(next);
            listeners.forEach(listener -> listener.accept(previous, next));
        }
    }
}
