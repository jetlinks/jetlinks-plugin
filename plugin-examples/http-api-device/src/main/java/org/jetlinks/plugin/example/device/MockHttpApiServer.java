package org.jetlinks.plugin.example.device;

import org.hswebframework.ezorm.core.param.Term;
import org.hswebframework.web.api.crud.entity.QueryParamEntity;
import org.jetlinks.core.device.DeviceInfo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

@SpringBootApplication
public class MockHttpApiServer {

    public static void main(String[] args) {
        SpringApplication.run(MockHttpApiServer.class, args);
    }

    @RestController
    static class DeviceController {

        private final Map<String, HttpApiDevicePlugin.DeviceInfo> cache = new ConcurrentHashMap<>();

        public DeviceController() {
            cache.put("device_1", new HttpApiDevicePlugin.DeviceInfo("device_1", true, null));
            cache.put("device_2", new HttpApiDevicePlugin.DeviceInfo("device_2", true, null));
            cache.put("device_3", new HttpApiDevicePlugin.DeviceInfo("device_3", true, null));
        }

        @PostMapping("/device/{deviceId}/properties")
        private Mono<Void> updateState(@PathVariable String deviceId,
                                       @RequestBody Mono<Map<String, Object>> properties) {
            return properties
                    .doOnNext(prop -> cache
                            .compute(deviceId, (key, info) -> {
                                if (info == null) {
                                    return new HttpApiDevicePlugin.DeviceInfo(key, true, prop);
                                } else {
                                    Map<String, Object> merge = new HashMap<>();
                                    if (info.getProperties() != null) {
                                        merge.putAll(info.getProperties());
                                    }
                                    merge.putAll(prop);
                                    info.setProperties(merge);
                                    return info;
                                }
                            }))
                    .then();
        }


        //修改状态信息
        @PostMapping("/device/set-states")
        private Mono<Void> setState(@RequestBody Flux<HttpApiDevicePlugin.DeviceInfo> info) {
            return info
                    .doOnNext(device -> {
                        if (null != device.getId()) {
                            cache.put(device.getId(), device);
                        }
                    })
                    .then();
        }

        //获取状态信息
        @PostMapping("/device/states")
        public Flux<HttpApiDevicePlugin.DeviceInfo> getDeviceStates(@RequestBody Mono<List<String>> devices) {
            return devices
                    .flatMapIterable(Function.identity())
                    .map(id -> cache.getOrDefault(id, new HttpApiDevicePlugin.DeviceInfo(id, true, null)));
        }

        //获取设备
        @PostMapping("/device/_query")
        public Flux<HttpApiDevicePlugin.DeviceInfo> query(@RequestBody QueryParamEntity query) {
            String deviceId = query
                    .getTerms()
                    .stream()
                    .filter(term -> "id".equals(term.getColumn()))
                    .map(Term::getValue)
                    .map(Object::toString)
                    .findAny()
                    .orElse(null);

            return StringUtils.hasText(deviceId) ? Flux.just(cache.get(deviceId)) : Flux.fromIterable(cache.values());
        }
    }
}
