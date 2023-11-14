package org.jetlinks.plugin.internal.media;

import org.jetlinks.core.message.function.FunctionInvokeMessage;
import org.jetlinks.core.message.function.FunctionInvokeMessageReply;
import reactor.core.publisher.Mono;

/**
 * 视频设备方法.
 *
 * @author zhangji 2023/10/20
 */
public enum MediaSdkFunction {
    //预览地址
    GetRealplayStreamUrl() {
        @Override
        public Mono<FunctionInvokeMessageReply> invoke(MediaGatewayPlugin plugin, FunctionInvokeMessage message) {
            MediaSdkChannelConfig config = message.inputsToBean(MediaSdkChannelConfig.class);
            return plugin
                    .getRealplayStreamUrl(message.getDeviceId(), config)
                    .map(stream -> message.newReply().output(stream));
        }
    },
    //回放地址
    GetPlaybackStreamUrl() {
        @Override
        public Mono<FunctionInvokeMessageReply> invoke(MediaGatewayPlugin plugin, FunctionInvokeMessage message) {
            MediaSdkDeviceControl control = message.inputsToBean(MediaSdkDeviceControl.class);
            return plugin
                    .getPlaybackStreamUrl(message.getDeviceId(), control)
                    .map(stream -> message.newReply().output(stream));
        }
    },
    //查询预置位
    QueryPreset() {
        @Override
        public Mono<FunctionInvokeMessageReply> invoke(MediaGatewayPlugin plugin, FunctionInvokeMessage message) {
            return plugin
                    .queryPreset(message.getDeviceId())
                    .collectList()
                    .map(preset -> message.newReply().output(preset));
        }
    },
    //预置位操作
    Preset() {
        @Override
        public Mono<FunctionInvokeMessageReply> invoke(MediaGatewayPlugin plugin, FunctionInvokeMessage message) {
            MediaSdkDeviceControl control = message.inputsToBean(MediaSdkDeviceControl.class);
            return plugin
                    .presetControl(message.getDeviceId(), control)
                    .map(success -> message.newReply().success(success));
        }
    },
    //云台控制
    PTZ {
        @Override
        public Mono<FunctionInvokeMessageReply> invoke(MediaGatewayPlugin plugin, FunctionInvokeMessage message) {
            MediaSdkDeviceControl control = message.inputsToBean(MediaSdkDeviceControl.class);
            return plugin
                    .ptzControl(message.getDeviceId(), control)
                    .map(success -> message.newReply().success(success));
        }
    },
    //看守位控制
    HomePosition{
        @Override
        public Mono<FunctionInvokeMessageReply> invoke(MediaGatewayPlugin plugin, FunctionInvokeMessage message) {
            return Mono.empty();
        }
    },
    //开始录像
    StartRecord {
        @Override
        public Mono<FunctionInvokeMessageReply> invoke(MediaGatewayPlugin plugin, FunctionInvokeMessage message) {
            MediaSdkDeviceControl control = message.inputsToBean(MediaSdkDeviceControl.class);
            return plugin
                    .record(message.getDeviceId(), control)
                    .map(success -> message.newReply().success(success));
        }
    },
    //停止录像
    StopRecord {
        @Override
        public Mono<FunctionInvokeMessageReply> invoke(MediaGatewayPlugin plugin, FunctionInvokeMessage message) {
            MediaSdkDeviceControl control = message.inputsToBean(MediaSdkDeviceControl.class);
            return plugin
                    .record(message.getDeviceId(), control)
                    .map(success -> message.newReply().success(success));
        }
    },
    //查询录像文件
    QueryRecordList{
        @Override
        public Mono<FunctionInvokeMessageReply> invoke(MediaGatewayPlugin plugin, FunctionInvokeMessage message) {
            MediaSdkDeviceControl control = message.inputsToBean(MediaSdkDeviceControl.class);
            return plugin
                    .queryRecord(message.getDeviceId(), control)
                    .collectList()
                    .map(record -> message.newReply().success(record));
        }
    },
    //订阅报警
    SubscribeAlarm{
        @Override
        public Mono<FunctionInvokeMessageReply> invoke(MediaGatewayPlugin plugin, FunctionInvokeMessage message) {
            return Mono.empty();
        }
    };

    public abstract Mono<FunctionInvokeMessageReply> invoke(MediaGatewayPlugin plugin, FunctionInvokeMessage message);
}