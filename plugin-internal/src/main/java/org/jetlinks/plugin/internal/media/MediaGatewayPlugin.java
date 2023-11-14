package org.jetlinks.plugin.internal.media;

import lombok.extern.slf4j.Slf4j;
import org.jetlinks.core.device.DeviceOperator;
import org.jetlinks.core.message.DeviceMessage;
import org.jetlinks.core.message.function.FunctionInvokeMessage;
import org.jetlinks.core.message.function.FunctionInvokeMessageReply;
import org.jetlinks.core.utils.Reactors;
import org.jetlinks.plugin.core.PluginContext;
import org.jetlinks.plugin.internal.device.DeviceGatewayPlugin;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

/**
 * 视频设备插件.
 *
 * @author zhangji 2023/10/12
 */
@Slf4j
public abstract class MediaGatewayPlugin extends DeviceGatewayPlugin {

    public MediaGatewayPlugin(String id, PluginContext context) {
        super(id, context);
    }

    @Override
    public Publisher<? extends DeviceMessage> execute(DeviceMessage message) {
        if (message instanceof FunctionInvokeMessage) {
            FunctionInvokeMessage function = ((FunctionInvokeMessage) message);
            String id = function.getFunctionId();
            return MediaSdkFunction
                    .valueOf(id)
                    .invoke(this, function)
                    .onErrorResume(err -> {
                        log.error("视频设备插件，执行功能错误", err);
                        return Mono.just(function.newReply().error(err));
                    });
        }
        return Mono.empty();
    }

    public abstract Mono<Byte> getDeviceState(DeviceOperator device);

    // 开始预览
    public Mono<MediaSdkStream> getRealplayStreamUrl(String deviceId, MediaSdkChannelConfig config) {
        return Mono.empty();
    }

    // 回放录像
    public Mono<MediaSdkStream> getPlaybackStreamUrl(String deviceId, MediaSdkDeviceControl control) {
        return Mono.empty();
    }

    // 查询预置位
    public Flux<MediaSdkPreset> queryPreset(String deviceId) {
        return Flux.empty();
    }

    // 设置预置位
    public Mono<Boolean> presetControl(String deviceId, MediaSdkDeviceControl control) {
        return Reactors.ALWAYS_FALSE;
    }

    // 云台控制
    public Mono<Boolean> ptzControl(String deviceId, MediaSdkDeviceControl control) {
        return Reactors.ALWAYS_FALSE;
    }

    // 开始录像/停止录像
    public Mono<Boolean> record(String deviceId, MediaSdkDeviceControl control) {
        return Reactors.ALWAYS_FALSE;
    }

    // 查询录像
    public Flux<MediaSdkRecord> queryRecord(String deviceId, MediaSdkDeviceControl control) {
        return Flux.empty();
    }
}
