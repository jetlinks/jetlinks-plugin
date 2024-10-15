package org.jetlinks.plugin.internal.device;

import org.jetlinks.core.cache.ReactiveCacheContainer;
import org.jetlinks.core.device.DeviceOperator;
import org.jetlinks.core.device.DeviceState;
import org.jetlinks.core.metadata.ConfigMetadata;
import org.jetlinks.core.metadata.DefaultConfigMetadata;
import org.jetlinks.core.metadata.types.IntType;
import org.jetlinks.core.metadata.types.StringType;
import org.jetlinks.plugin.core.PluginContext;
import reactor.core.Disposable;
import reactor.core.publisher.Mono;
import reactor.netty.tcp.TcpClient;

public class TcpDeviceClientPlugin extends DeviceGatewayPlugin {

    private final ReactiveCacheContainer<String, TcpDeviceClient> clientCache =
        ReactiveCacheContainer.create();

    public TcpDeviceClientPlugin(String id, PluginContext context) {
        super(id, context);
    }

    @Override
    public Mono<ConfigMetadata> getDeviceConfigMetadata(String deviceId) {
        return Mono.just(
            new DefaultConfigMetadata("TCP连接配置", "")
                .add("host", "主机地址", StringType.GLOBAL)
                .add("port", "端口", IntType.GLOBAL)
        );
    }

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

    protected static class TcpDeviceClient implements Disposable {
        private String deviceId;

        private TcpClient client;

        @Override
        public void dispose() {

        }
    }
}
