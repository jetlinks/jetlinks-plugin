package org.jetlinks.plugin.tcp;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelOption;
import io.netty.handler.codec.json.JsonObjectDecoder;
import org.jetlinks.core.message.DeviceMessage;
import org.jetlinks.core.message.DeviceOfflineMessage;
import org.jetlinks.core.message.DeviceOnlineMessage;
import org.jetlinks.core.message.MessageType;
import org.jetlinks.plugin.internal.device.TcpDeviceClientPlugin;
import org.jetlinks.sdk.server.utils.ObjectMappers;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.netty.Connection;
import reactor.netty.tcp.TcpClient;

import java.util.HashMap;
import java.util.Map;

class JsonTcpDeviceClient extends TcpDeviceClientPlugin.TcpDeviceClient {

    public JsonTcpDeviceClient(String deviceId, TcpDeviceClientPlugin parent) {
        super(deviceId, parent);
        tryReconnect();
    }

    //处理平台下发的消息
    @Override
    public Mono<Void> downstream(DeviceMessage message) {
        return connect()
            .flatMap(conn -> conn
                .outbound()
                //直接转为json发送
                .send(Mono.just(Unpooled.wrappedBuffer(
                    ObjectMappers.toJsonBytes(message.toJson())
                )))
                .then()
            );
    }

    @Override
    protected void initConnection(Connection connection) {
        //断开连接则认为离线
        connection.onDispose(() -> super
            .upstream(new DeviceOfflineMessage())
            .subscribe());

        //建立连接则认为上线
        upstream(new DeviceOnlineMessage())
            .thenMany(
                connection
                    //json,根据实际情况设置netty codec
                    .addHandlerFirst(new JsonObjectDecoder())
                    .inbound()
                    .receiveObject()
                    .cast(ByteBuf.class)
                    .concatMap(this::handleUpstream)
            )
            .subscribe();

    }


    //处理来自设备的消息
    private Mono<Void> handleUpstream(ByteBuf buf) {
        try {

            //array
            if (buf.getByte(0) == '[') {
                return Flux
                    .fromIterable(ObjectMappers.parseJsonArray(new ByteBufInputStream(buf, false), HashMap.class))
                    .concatMap(this::handleMessage)
                    .then();
            }
            //object
            else {
                return handleMessage(
                    ObjectMappers
                        .parseJson(new ByteBufInputStream(buf, false),
                                   HashMap.class));
            }

        } finally {
            // ReferenceCountUtil.release(buf);
        }
    }


    @SuppressWarnings("all")
    private Mono<Void> handleMessage(Map<?, ?> data) {
        return MessageType
            .<DeviceMessage>convertMessage((Map<String, Object>) data)
            .map(super::upstream)
            .orElse(Mono.empty());
    }

    @Override
    protected TcpClient initClient(TcpClient client) {
        return client
            .option(ChannelOption.TCP_NODELAY, true)
            .option(ChannelOption.SO_KEEPALIVE, true)
            .option(ChannelOption.SO_TIMEOUT, 1000)
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000);
    }
}