package org.jetlinks.plugin.tcp;

import org.jetlinks.core.message.property.ReadPropertyMessage;
import org.jetlinks.plugin.core.PluginContext;
import org.jetlinks.plugin.internal.device.TcpDeviceClientPlugin;
import reactor.core.publisher.Mono;

import java.time.Duration;

class ExampleTcpDevicePlugin extends TcpDeviceClientPlugin {

    public ExampleTcpDevicePlugin(String id, PluginContext context) {
        super(id, context);

    }

    @Override
    protected Mono<TcpDeviceClient> createClient(String deviceId) {
        //todo 根据配置来使用json或者其他 ？
        return Mono.just(new JsonTcpDeviceClient(deviceId, this));
    }


    //拉取已经建立连接的设备的数据
    protected Mono<Void> pollData() {
        return clientCache
            .values()
            .flatMap(client -> {
                ReadPropertyMessage msg = new ReadPropertyMessage();
                msg.setDeviceId(client.deviceId);
                return client
                    .downstream(msg)
                    .onErrorResume(err -> {
                        context().monitor().logger()
                                 .warn("拉取设备[{}]数据失败", client.deviceId, err);
                        return Mono.empty();
                    });
            })
            .then();
    }

    //初始化平台的所有设备连接
    protected Mono<Void> initConnections() {
        return getPlatformDevices()
            .flatMap(device -> getOrCreateClient(device.getDeviceId()))
            .then();
    }

    //定时拉取设备数据
    protected void init() {
        context()
            .scheduler()
            .interval("poll-data",
                      Mono.defer(this::pollData),
                      Duration.ofSeconds(10)
            );
    }

    @Override
    protected Mono<Void> doStart() {
        init();
        return initConnections();
    }



}
