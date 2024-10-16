package org.jetlinks.plugin.tcp;

import lombok.SneakyThrows;
import org.jetlinks.core.device.*;
import org.jetlinks.core.message.DeviceMessage;
import org.jetlinks.core.message.property.ReadPropertyMessage;
import org.jetlinks.core.utils.Reactors;
import org.jetlinks.plugin.internal.device.DeviceGatewayPlugin;
import org.jetlinks.plugin.internal.device.PluginDeviceGatewayService;
import org.jetlinks.plugin.internal.device.PluginDeviceManager;
import org.jetlinks.plugin.internal.StaticPluginContext;
import org.jetlinks.plugin.internal.device.TcpDeviceClientPlugin;
import org.jetlinks.supports.test.InMemoryDeviceRegistry;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.netty.DisposableServer;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

class ExampleTcpDevicePluginTest {


    @Test
    @SneakyThrows
    void test() {

        String deviceId = "tcp-test-1";
        AtomicInteger serverReceived = new AtomicInteger();
        DisposableServer server = ExampleTcpServer.start(serverReceived);

        //注册模拟产品和设备
        DeviceRegistry registry = new InMemoryDeviceRegistry();
        registry.register(ProductInfo.builder()
                                     .id("test")
                                     .metadata("{}")
                                     .build())
                .block();

        DeviceOperator operator = registry
            .register(
                DeviceInfo
                    .builder()
                    .id(deviceId)
                    .productId("test")
                    .build()
                    .addConfig(TcpDeviceClientPlugin.HOST, "127.0.0.1")
                    .addConfig(TcpDeviceClientPlugin.PORT, server.port())
            )
            .block();
        Assertions.assertNotNull(operator);

        Sinks.Many<DeviceMessage> msg = Sinks.many().multicast().onBackpressureBuffer();
        //模拟服务
        PluginDeviceGatewayService gatewayService = (plugin, message) -> {
            msg.emitNext(message, Reactors.emitFailureHandler());
            return Mono.empty();
        };
        PluginDeviceManager manager = new PluginDeviceManager() {
            @Override
            public Flux<DeviceOperator> getDevices(DeviceGatewayPlugin plugin) {
                return Flux.just(operator);
            }

            @Override
            public Flux<DeviceOperator> getDevices(DeviceGatewayPlugin plugin, String productId) {
                return Flux.just(operator);
            }
        };

        //创建插件
        StaticPluginContext pluginContext = new StaticPluginContext();
        pluginContext.withService(registry)
                     .withService(gatewayService)
                     .withService("pluginDeviceManager", manager);
        ExampleTcpDevicePlugin plugin = new ExampleTcpDevicePlugin(
            "test",
            pluginContext
        );
        plugin.start().block();

        ReadPropertyMessage message = new ReadPropertyMessage();
        message.setDeviceId(deviceId);

        //下发消息
       Mono.zip(
               Mono.fromDirect(plugin.execute(message)),
               Mono.fromDirect(plugin.execute(message))
           )
            .as(StepVerifier::create)
            .expectComplete()
            .verify();

        Thread.sleep(200);
        //服务端接收到了数据
        Assertions.assertNotEquals(0, serverReceived.get());

        //平台接收到了数据
        msg.asFlux()
           .take(3)
           .doOnNext(System.out::println)
           .timeout(Duration.ofSeconds(5))
           .then()
           .as(StepVerifier::create)
           .expectComplete()
           .verify();

    }

}