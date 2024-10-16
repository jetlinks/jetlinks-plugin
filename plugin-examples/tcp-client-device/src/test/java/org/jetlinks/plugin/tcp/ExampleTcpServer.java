package org.jetlinks.plugin.tcp;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.json.JsonObjectDecoder;
import org.jetlinks.core.message.property.ReportPropertyMessage;
import org.jetlinks.sdk.server.utils.ObjectMappers;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.netty.DisposableServer;
import reactor.netty.tcp.TcpServer;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicInteger;

public class ExampleTcpServer {

    public static DisposableServer start(AtomicInteger serverReceived) {
        return TcpServer
            .create()
            .doOnConnection(conn -> {
                //json方式解析
                conn.addHandlerFirst(new JsonObjectDecoder());

            })
            .handle((in, out) -> Flux
                .merge(
                    //接收来自客户端的消息
                    in
                        .receiveObject()
                        .cast(ByteBuf.class)
                        .flatMap(buf -> {
                            serverReceived.incrementAndGet();
                            System.out.println("from client:" + buf.toString(StandardCharsets.UTF_8));
                            return Mono.empty();
                        })
                        .then(),
                    //定时推送消息给客户端
                    Flux.interval(Duration.ofSeconds(1))
                        .map(i -> {
                            ReportPropertyMessage msg = new ReportPropertyMessage();
                            msg.setProperties(Collections.singletonMap("temp", i));
                            return Unpooled.wrappedBuffer(ObjectMappers.toJsonBytes(msg.toJson()));
                        })
                        .as(out::send)
                ))
            .bindNow();

    }
}
