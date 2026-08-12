package org.jetlinks.plugin.external.runner;

import io.rsocket.transport.netty.server.TcpServerTransport;
import org.jetlinks.plugin.external.ExternalPluginServer;
import org.jetlinks.plugin.protocol.WireCodec;
import org.jetlinks.plugin.core.PluginContext;
import reactor.core.Disposable;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Owns one driver generation, listener, plugin instances and classloader. A runtime is not a hot
 * reload container: replacing a generation is the platform deployment manager's responsibility.
 */
public final class JavaPluginRuntime implements Disposable {
    private final JavaPluginDriverSource source;
    private final JavaPluginRuntimeConfiguration configuration;
    private final PluginContext context;
    private final AtomicBoolean started = new AtomicBoolean();
    private final AtomicBoolean closed = new AtomicBoolean();
    private final AtomicReference<ExternalPluginServer> server = new AtomicReference<>();
    private final AtomicReference<JavaPluginDriverSource.LoadedPluginDriver> loaded = new AtomicReference<>();
    private final AtomicReference<Mono<Void>> closeSignal = new AtomicReference<>();

    JavaPluginRuntime(JavaPluginDriverSource source,
                      JavaPluginRuntimeConfiguration configuration,
                      PluginContext context) {
        this.source = Objects.requireNonNull(source, "source");
        this.configuration = Objects.requireNonNull(configuration, "configuration");
        this.context = Objects.requireNonNull(context, "context");
    }

    public static JavaPluginRuntimeBuilder builder() {
        return new JavaPluginRuntimeBuilder();
    }

    public Mono<ListeningEndpoint> start() {
        if (!started.compareAndSet(false, true)) {
            ExternalPluginServer current = server.get();
            return current == null
                ? Mono.error(new IllegalStateException("runtime start is already in progress"))
                : Mono.just(new ListeningEndpoint(current.channel()));
        }
        return source.load()
            .flatMap(loadedDriver -> {
                loaded.set(loadedDriver);
                WireCodec codec = new WireCodec(configuration.maxFrameBytes());
                ExternalPluginServer pluginServer = ExternalPluginServer
                    .builder(loadedDriver.driver(), context)
                    .codec(codec)
                    .identity(configuration.runtimeId(), configuration.driverId(), configuration.generation())
                    .credential(configuration.credential())
                    .maxResourceBytes(configuration.maxResourceBytes())
                    .build();
                server.set(pluginServer);
                Mono<ExternalPluginServer> bind = configuration.unixSocket() == null
                    ? pluginServer.bind(TcpServerTransport.create(configuration.host(), configuration.port()))
                    : bindUnix(pluginServer);
                return bind
                    .map(ignore -> new ListeningEndpoint(pluginServer.channel()))
                    .onErrorResume(error -> close().then(Mono.error(error)));
            });
    }

    private Mono<ExternalPluginServer> bindUnix(ExternalPluginServer pluginServer) {
        try {
            if (Files.exists(configuration.unixSocket(), LinkOption.NOFOLLOW_LINKS)
                && (Files.isRegularFile(configuration.unixSocket(), LinkOption.NOFOLLOW_LINKS)
                || Files.isDirectory(configuration.unixSocket(), LinkOption.NOFOLLOW_LINKS)
                || Files.isSymbolicLink(configuration.unixSocket()))) {
                return Mono.error(new IllegalStateException("unix socket path is not a socket"));
            }
            Files.deleteIfExists(configuration.unixSocket());
        } catch (Exception error) {
            return Mono.error(new IllegalStateException("unable to remove stale unix socket", error));
        }
        return pluginServer.bindUnix(configuration.unixSocket());
    }

    public Mono<Void> drain(Duration timeout) {
        ExternalPluginServer current = server.get();
        if (current == null) {
            return Mono.empty();
        }
        current.beginDrain();
        return current.shutdown().timeout(timeout == null ? configuration.drainTimeout() : timeout);
    }

    public Mono<Void> close() {
        Mono<Void> current = closeSignal.get();
        if (current != null) {
            return current;
        }
        Mono<Void> created = Mono.defer(() -> {
            closed.set(true);
            ExternalPluginServer currentServer = server.get();
            Mono<Void> serverClose = currentServer == null ? Mono.empty() : currentServer.shutdown();
            return serverClose.doFinally(ignore -> {
                JavaPluginDriverSource.LoadedPluginDriver currentLoaded = loaded.getAndSet(null);
                if (currentLoaded != null) {
                    currentLoaded.close();
                }
                source.close();
            });
        }).cache();
        if (closeSignal.compareAndSet(null, created)) {
            return created;
        }
        return closeSignal.get();
    }

    public Mono<Void> onClose() {
        return closeSignal.get() == null ? Mono.never() : closeSignal.get();
    }

    @Override
    public void dispose() {
        close().subscribe(null, error -> System.getLogger(JavaPluginRuntime.class.getName())
            .log(System.Logger.Level.WARNING, "java plugin runtime close failed", error));
    }

}
