package org.jetlinks.plugin.example.device;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.jetlinks.core.message.DeviceMessage;
import org.jetlinks.core.message.DeviceOfflineMessage;
import org.jetlinks.core.message.DeviceOnlineMessage;
import org.jetlinks.core.message.property.ReadPropertyMessage;
import org.jetlinks.core.message.property.ReportPropertyMessage;
import org.jetlinks.core.message.property.WritePropertyMessage;
import org.jetlinks.core.metadata.ConfigMetadata;
import org.jetlinks.core.things.Thing;
import org.jetlinks.plugin.core.PluginContext;
import org.jetlinks.plugin.internal.device.DeviceGatewayPlugin;
import org.reactivestreams.Publisher;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Slf4j
class HttpApiDevicePlugin extends DeviceGatewayPlugin {

    static final String API_URL= "api_url";
    static final String ACCESS_KEY= "access_key";

    private final WebClient client;

    public HttpApiDevicePlugin(String id, PluginContext context) {
        super(id, context);

        //启动插件的配置
        String apiUrl = context
                .environment()
                .getProperty("api_url")
                .orElseThrow(() -> new IllegalArgumentException("plugin env 'api_url' is required"));
        String accessKey = context
                .environment()
                .getProperty("access_key")
                .orElseThrow(() -> new IllegalArgumentException("plugin env 'access_key' is required"));

        //http client
        client = context
                .services()
                .getService(WebClient.Builder.class)
                .map(WebClient.Builder::clone)
                .orElseGet(WebClient::builder)
                .baseUrl(apiUrl)
                // Authorization: Bearer {token}
                .defaultHeaders(headers -> headers.setBearerAuth(accessKey))
                .build();
    }

    @Override
    public Mono<ConfigMetadata> getDeviceConfigMetadata(String productId) {
        return super.getDeviceConfigMetadata(productId);
    }

    private Mono<Void> pollState() {
        return getPlatformDevices()
                .map(Thing::getId)
                .buffer(100)
                .flatMap(list -> this
                        .getDeviceState(list)
                        .flatMap(this::handleState)
                        .onErrorResume(err -> {
                            log.warn("check device state error", err);
                            return Mono.empty();
                        }))
                .then();
    }

    private Mono<Void> handleState(DeviceInfo deviceInfo) {
        //在线
        if (deviceInfo.online) {

            //属性上报
            if (MapUtils.isNotEmpty(deviceInfo.properties)) {
                ReportPropertyMessage msg = new ReportPropertyMessage();
                msg.setDeviceId(deviceInfo.id);
                msg.setProperties(deviceInfo.getProperties());
                return handleMessage(msg);
            }

            DeviceOnlineMessage message = new DeviceOnlineMessage();
            message.setDeviceId(deviceInfo.id);
            return handleMessage(message);
        } else {
            DeviceOfflineMessage message = new DeviceOfflineMessage();
            message.setDeviceId(deviceInfo.id);
            return handleMessage(message);
        }
    }

    public Flux<DeviceInfo> getDeviceState(List<String> device) {
        return client
                .post()
                .uri("/device/states")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(device)
                .exchangeToFlux(response -> response.bodyToFlux(DeviceInfo.class));
    }

    @Override
    public Publisher<? extends DeviceMessage> execute(DeviceMessage message) {

        // 读取属性
        if (message instanceof ReadPropertyMessage) {
            return this
                    .getDeviceState(Collections.singletonList(message.getDeviceId()))
                    .mapNotNull(DeviceInfo::getProperties)
                    .map(((ReadPropertyMessage) message).newReply()::success);
        }
        //修改属性
        else if (message instanceof WritePropertyMessage) {
            Map<String, Object> props = ((WritePropertyMessage) message).getProperties();

            return client
                    .post()
                    .uri("/device/{deviceId}/properties", message.getDeviceId())
                    .bodyValue(props)
                    .retrieve()
                    .bodyToMono(Void.class)
                    .thenReturn(((WritePropertyMessage) message).newReply().success(props))
                    .onErrorResume(err -> Mono.just(((WritePropertyMessage) message)
                                                            .newReply()
                                                            .error(err)));
        }

        return super.execute(message);
    }

    @Override
    protected Mono<Void> doStart() {

        //启动时定时拉取状态
        this.context()
            .scheduler()
            .interval("pull_device_state",
                      Mono.defer(this::pollState),
                      Duration.ofSeconds(10));

        return super.doStart();
    }


    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DeviceInfo {
        private String id;
        private boolean online;

        private Map<String, Object> properties;
    }
}
