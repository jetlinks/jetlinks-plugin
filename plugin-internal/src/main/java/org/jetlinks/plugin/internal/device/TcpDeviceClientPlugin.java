package org.jetlinks.plugin.internal.device;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.hswebframework.web.exception.ValidationException;
import org.jetlinks.core.cache.ReactiveCacheContainer;
import org.jetlinks.core.config.ConfigKey;
import org.jetlinks.core.device.DeviceOperator;
import org.jetlinks.core.device.DeviceState;
import org.jetlinks.core.device.DeviceThingType;
import org.jetlinks.core.enums.ErrorCode;
import org.jetlinks.core.exception.DeviceOperationException;
import org.jetlinks.core.message.DeviceMessage;
import org.jetlinks.core.message.DisconnectDeviceMessage;
import org.jetlinks.core.metadata.ConfigMetadata;
import org.jetlinks.core.metadata.DefaultConfigMetadata;
import org.jetlinks.core.metadata.types.IntType;
import org.jetlinks.core.metadata.types.StringType;
import org.jetlinks.core.monitor.logger.Logger;
import org.jetlinks.core.monitor.tracer.Tracer;
import org.jetlinks.core.utils.Reactors;
import org.jetlinks.plugin.core.PluginContext;
import org.reactivestreams.Publisher;
import reactor.core.Disposable;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.netty.Connection;
import reactor.netty.tcp.TcpClient;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

@Getter
@Setter
public abstract class TcpDeviceClientPlugin extends DeviceGatewayPlugin {

    public final static ConfigKey<String> HOST = ConfigKey.of("host", "主机地址", String.class);
    public final static ConfigKey<Integer> PORT = ConfigKey.of("port", "端口", Integer.class);

    protected final ReactiveCacheContainer<String, TcpDeviceClient> clientCache =
        ReactiveCacheContainer.create();


    //短链接,不重试
    private boolean sortConnection;

    private int maxRetryTimes = 32;

    public TcpDeviceClientPlugin(String id, PluginContext context) {
        super(id, context);
    }

    //声明在设备详情页面中需要填写的配置
    @Override
    public Mono<ConfigMetadata> getDeviceConfigMetadata(String deviceId) {
        return Mono.just(
            new DefaultConfigMetadata("TCP连接配置", "")
                .add(HOST.getKey(), HOST.getName(), StringType.GLOBAL)
                .add(PORT.getKey(), PORT.getName(), IntType.GLOBAL)
        );
    }

    @Override
    public Mono<Void> doOnDeviceRegister(DeviceOperator device) {
        return this
            .getOrCreateClient(device.getDeviceId())
            .then();
    }

    @Override
    public Publisher<? extends DeviceMessage> execute(DeviceMessage message) {
        if (message instanceof DisconnectDeviceMessage) {
            clientCache.remove(message.getDeviceId());
            return Mono.just(((DisconnectDeviceMessage) message).newReply().success());
        }
        return this
            .getOrCreateClient(message.getDeviceId())
            .flatMap(client -> client.downstream(message))
            .then(Mono.empty());
    }

    protected Mono<TcpDeviceClient> getOrCreateClient(String deviceId) {
        return clientCache
            .computeIfAbsent(deviceId, this::createClient);
    }

    protected abstract Mono<TcpDeviceClient> createClient(String deviceId);

    @Override
    public Mono<Byte> getDeviceState(DeviceOperator device) {

        TcpDeviceClient client = clientCache.getNow(device.getDeviceId());
        if (client == null || client.isDisposed()) {
            return Mono.just(DeviceState.offline);
        }
        return Mono.just(DeviceState.online);
    }

    @Override
    protected Mono<Void> doShutdown() {
        clientCache.clear();
        return super.doShutdown();
    }

    @RequiredArgsConstructor
    public abstract static class TcpDeviceClient implements Disposable {
        @SuppressWarnings("all")
        static final AtomicReferenceFieldUpdater<TcpDeviceClient, Sinks.One>
            CONNECTING = AtomicReferenceFieldUpdater.newUpdater(TcpDeviceClient.class, Sinks.One.class, "connecting");

        public final String deviceId;
        protected final TcpDeviceClientPlugin parent;
        private volatile Connection connected;
        private volatile Sinks.One<Connection> connecting;
        private volatile Disposable connectionDisposable;
        private int retryTimes;
        private boolean disposed;
        private Throwable lastError;

        protected Logger logger() {
            return parent.context().monitor().logger();
        }

        protected Tracer tracer() {
            return parent.context().monitor().tracer();
        }

        protected final Mono<Connection> connect() {
            if (connected != null && !connected.isDisposed()) {
                return Mono.just(connected);
            }

            Sinks.One<Connection> connecting = Sinks.one();
            if (CONNECTING.compareAndSet(this, null, connecting)) {
                if (null != this.connectionDisposable) {
                    this.connectionDisposable.dispose();
                }

                @SuppressWarnings("all")
                Disposable connectionDisposable = connectNow()
                    .as(tracer().traceMono("connect"))
                    .subscribe(
                        c -> {
                            logger().debug("连接设备[{}]成功", deviceId);
                            connecting.emitValue(c, Reactors.emitFailureHandler());
                            CONNECTING.compareAndSet(this, connecting, null);
                        },
                        (err) -> {
                            logger().warn("连接设备[{}]失败", deviceId, err);
                            connecting.emitError(lastError = err, Reactors.emitFailureHandler());
                            CONNECTING.compareAndSet(this, connecting, null);
                            tryReconnect();
                        },
                        () -> {
                            connecting.emitEmpty(Reactors.emitFailureHandler());
                            if (CONNECTING.compareAndSet(this, connecting, null)) {
                                logger().warn("连接设备[{}]失败,未正确配置host,port?", deviceId);
                            }
                            tryReconnect();
                        });
                this.connectionDisposable = connectionDisposable;
                return connecting.asMono();
            }
            return this.connecting.asMono();

        }

        private Mono<Connection> connectNow() {
            return parent
                .registry
                .getDevice(deviceId)
                .flatMap(device -> device
                    .getSelfConfigs(HOST, PORT)
                    .filter(values -> values.size() == 2)
                    .flatMap(values -> connection(
                        values
                            .getValue(HOST)
                            .orElseThrow(() -> new ValidationException("host cant not be empty")),
                        values
                            .getValue(PORT)
                            .orElseThrow(() -> new ValidationException("port cant not be null")))));
        }

        private synchronized void connected(Connection connected) {
            retryTimes = 0;
            if (this.connected != null) {
                this.connected.dispose();
            }
            if (this.isDisposed()) {
                connected.dispose();
                return;
            }
            this.connected = connected;
            initConnection(this.connected);
        }

        protected abstract void initConnection(Connection connection);

        protected void tryReconnect() {
            //短连接不重试
            if (this.isDisposed() || parent.isSortConnection()) {
                return;
            }
            if (parent.getMaxRetryTimes() > 0
                && retryTimes++ >= parent.getMaxRetryTimes()) {
                return;
            }
            Mono.delay(Duration.ofMillis(retryTimes * 100L))
                .then(Mono.defer(this::connect))
                .subscribe();
        }

        public abstract Mono<Void> downstream(DeviceMessage message);

        public final Mono<Void> upstream(DeviceMessage message) {
            if (message.getDeviceId() == null) {
                message.thingId(DeviceThingType.device.getId(), deviceId);
            }
            parent.context()
                  .monitor()
                  .logger()
                  .debug("设备上报消息:{}", message);
            return parent
                .handleMessage(message);
        }

        protected Mono<Connection> connection(String host, int port) {
            logger().debug("start connect device {} tcp server {}:{}", deviceId, host, port);
            return this
                .initClient(
                    TcpClient
                        .create()
                        .host(host)
                        .port(port)
                )
                .doOnConnected(this::connected)
                .doOnDisconnected(connection -> tryReconnect())
                .connect()
                .cast(Connection.class);
        }

        protected abstract TcpClient initClient(TcpClient client);

        @Override
        public boolean isDisposed() {
            return disposed;
        }

        @Override
        public void dispose() {
            disposed = true;
            if (null != connected) {
                connected.dispose();
            }
            if (connectionDisposable != null) {
                connectionDisposable.dispose();
            }
            if (connecting != null) {
                connecting.tryEmitError(new DeviceOperationException(ErrorCode.CONNECTION_LOST));
            }
        }
    }
}
