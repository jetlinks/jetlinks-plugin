package org.jetlinks.plugin.example.sdk;

import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.hswebframework.web.api.crud.entity.PagerResult;
import org.hswebframework.web.api.crud.entity.QueryParamEntity;
import org.hswebframework.web.exception.BusinessException;
import org.jetlinks.core.Value;
import org.jetlinks.core.command.CommandHandler;
import org.jetlinks.core.device.DeviceOperator;
import org.jetlinks.core.device.DeviceProductOperator;
import org.jetlinks.core.message.DeviceMessage;
import org.jetlinks.core.message.DeviceOfflineMessage;
import org.jetlinks.core.message.DeviceOnlineMessage;
import org.jetlinks.core.message.property.ReadPropertyMessage;
import org.jetlinks.core.message.property.ReportPropertyMessage;
import org.jetlinks.core.message.property.WritePropertyMessage;
import org.jetlinks.core.metadata.ConfigMetadata;
import org.jetlinks.core.metadata.DefaultConfigMetadata;
import org.jetlinks.core.metadata.SimplePropertyMetadata;
import org.jetlinks.core.metadata.types.IntType;
import org.jetlinks.core.metadata.types.PasswordType;
import org.jetlinks.core.metadata.types.StringType;
import org.jetlinks.plugin.core.PluginContext;
import org.jetlinks.plugin.example.sdk.hc.HCNetSDK;
import org.jetlinks.plugin.example.sdk.hc.NetSDKDemo;
import org.jetlinks.plugin.internal.device.Device;
import org.jetlinks.plugin.internal.device.command.QueryDeviceListCommand;
import org.jetlinks.plugin.internal.device.command.QueryDevicePageCommand;
import org.jetlinks.plugin.internal.media.*;
import org.jetlinks.plugin.internal.media.command.SyncChannelCommand;
import org.reactivestreams.Publisher;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import sun.nio.cs.ext.GBK;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.jetlinks.plugin.example.sdk.PluginProduct.required;

/**
 * 输入描述.
 *
 * @author zhangji 2023/3/6
 */
@Slf4j
public class SdkDevicePlugin extends MediaGatewayPlugin {

    static final String IP = "ip";
    static final String LOGIN_PORT = "login_port";
    static final String RTSP_PORT = "rtsp_port";
    static final String USERNAME = "user";
    static final String PASSWORD = "pwd";
    static final String USER_ID = "user_id";
    static final String CHARSET = "charset";
    static final String CHANNEL_NUM = "channel_num";
    static final String LAST_PTZ_COMMAND = "last_ptz_command";

    private static final NetSDKDemo sdk = new NetSDKDemo();
    public static final Map<String, PluginProduct> pluginProducts = new HashMap<>();

    static {
        PluginProductRS485 media = new PluginProductRS485();
        pluginProducts.put(media.getId(), media);
        PluginProductPTZ senser = new PluginProductPTZ();
        pluginProducts.put(senser.getId(), senser);
        PluginProductDefault productDefault = new PluginProductDefault();
        pluginProducts.put(productDefault.getId(), productDefault);
    }


    public SdkDevicePlugin(String id,
                           PluginContext context) {
        super(id, context);
        sdk.initSDKInstance();

        // 同步通道
        registerHandler(SyncChannelCommand.class,
                        CommandHandler
                                .of(SyncChannelCommand.metadata(),
                                    (cmd, self) -> syncChannel(cmd),
                                    SyncChannelCommand::new)
        );

        // 分页查询设备
        registerHandler(QueryDevicePageCommand.class,
                        CommandHandler
                                .of(QueryDevicePageCommand.metadata(
                                        SimplePropertyMetadata.of("id", "ID", StringType.GLOBAL)),
                                    (cmd, self) -> queryDevice(cmd),
                                    QueryDevicePageCommand::new)
        );

        // 查询设备列表
        registerHandler(QueryDeviceListCommand.class,
                        CommandHandler
                                .of(QueryDeviceListCommand.metadata(
                                        SimplePropertyMetadata.of("id", "ID", StringType.GLOBAL)),
                                    (cmd, self) -> queryDevice(cmd),
                                    QueryDeviceListCommand::new)
        );

    }

    private Flux<MediaSdkChannel> syncChannel(SyncChannelCommand cmd) {
        return registry
                .getDevice(cmd.deviceId())
                .flatMap(device -> device.getConfigs(USER_ID, CHANNEL_NUM, USERNAME, PASSWORD, IP, LOGIN_PORT))
                .flatMap(values -> Mono.fromSupplier(() -> {
                    Integer userId = values.getValue(USER_ID).map(Value::asInt).orElse(0);
                    Integer channelNum = values.getValue(CHANNEL_NUM).map(Value::asInt).orElse(1);
                    String username = values.getValue(USERNAME).map(Value::asString).orElse(null);
                    String password = values.getValue(PASSWORD).map(Value::asString).orElse(null);
                    String ip = values.getValue(IP).map(Value::asString).orElse(null);
                    Integer port = values.getValue(LOGIN_PORT).map(Value::asInt).orElse(0);

                    return doQueryChannel(cmd.deviceId(), userId, channelNum, username, password, ip, port);
                }))
                .flatMapMany(Flux::fromIterable);
    }

    private List<MediaSdkChannel> doQueryChannel(String deviceId,
                                                 Integer userId,
                                                 Integer channelNum,
                                                 String username,
                                                 String password,
                                                 String ip,
                                                 Integer port) {
        HCNetSDK.NET_DVR_ACCESS_DEVICE_CHANNEL_INFO info = sdk.queryChannel(
                userId, channelNum, username, password, ip, port.shortValue()
        );
        List<MediaSdkChannel> channelList = new ArrayList<>();

        for (int i = 0; i <= info.dwTotalChannelNum; i++) {
            int index = i + 1;
            System.out.println("" + index + "号通道是否已接入：" + info.byChannel[i]);
            MediaSdkChannel channel = new MediaSdkChannel();
            channel.setId(String.valueOf(index));
            channel.setName(String.valueOf(index));
            channel.setDeviceId(deviceId);
            channelList.add(channel);
        }
        return channelList;
    }

    private Mono<PagerResult<Device>> queryDevice(QueryDevicePageCommand cmd) {
        QueryParamEntity query = cmd.toQueryParam();
        List<Device> deviceList = new ArrayList<>();
        Device device = new Device();
        device.setId("test");
        device.setName("test");
        deviceList.add(device);
        return Mono.just(PagerResult.of(deviceList.size(), deviceList, query));
    }

    private Flux<Device> queryDevice(QueryDeviceListCommand cmd) {
        QueryParamEntity query = cmd.toQueryParam();
        List<Device> deviceList = new ArrayList<>();
        Device device = new Device();
        device.setId("test");
        device.setName("test");
        deviceList.add(device);
        return Flux.fromIterable(deviceList);
    }

    @Override
    public Mono<ConfigMetadata> getDeviceConfigMetadata(String productId) {
        return Mono
                .justOrEmpty(pluginProducts.get(productId))
                .mapNotNull(PluginProduct::getDeviceConfigMetadata)
                .defaultIfEmpty(new DefaultConfigMetadata()
                                        .add(IP, "设备ip", new StringType().expand(required.value(true)))
                                        .add(LOGIN_PORT, "设备登录端口", new IntType().expand(required.value(true)))
                                        .add(RTSP_PORT, "设备rtsp端口", new IntType())
                                        .add(USERNAME, "用户名", new StringType().expand(required.value(true)))
                                        .add(PASSWORD, "密码", new PasswordType().expand(required.value(true))))
                .cast(ConfigMetadata.class);
    }

    @Override
    public Mono<ConfigMetadata> getProductConfigMetadata(String productId) {
        return Mono
                .justOrEmpty(pluginProducts.get(productId))
                .mapNotNull(PluginProduct::getProductConfigMetadata);
    }

    @Override
    public Mono<Byte> getDeviceState(DeviceOperator device) {
        return this
                .getUser(device)
                .map(sdk::getDeviceStatus)
                .map(status -> status ? (byte) 1 : (byte) -1);
    }

    private Mono<Void> pollState() {
        return getPlatformDevices()
                .buffer(100)
                .flatMap(list -> Flux.fromIterable(list)
                                     .flatMap(this::getDeviceInfo)
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

    public Mono<DeviceInfo> getDeviceInfo(DeviceOperator device) {
        return Mono
                .zip(
                        Mono.just(device.getId()),
                        this.getUser(device),
                        this.getPluginProduct(device)
                )
                .map(tp3 -> new DeviceInfo(
                        tp3.getT1(),
                        sdk.getDeviceStatus(tp3.getT2()),
                        tp3.getT3().getSdkProperties(tp3.getT2(), sdk)))
                .switchIfEmpty(Mono.error(() -> new BusinessException("plugin device info not exist")));
    }

    /**
     * 获取已登录的设备用户ID
     *
     * @param device 设备操作
     * @return 用户ID
     */
    private Mono<Integer> getUser(DeviceOperator device) {
        return device
                .getSelfConfig(USER_ID)
                .map(Value::asInt)
                .filter(sdk::getDeviceStatus)
                .onErrorResume(err -> {
                    log.error("sdk登录错误", err);
                    return Mono.error(err);
                })
                .switchIfEmpty(device.getSelfConfigs(IP, LOGIN_PORT, USERNAME, PASSWORD)
                                     .onErrorResume(err -> {
                                         log.error("sdk登录错误", err);
                                         return Mono.error(err);
                                     })
                                     // 不存在用户ID，则发起登录
                                     .map(values -> sdk.Login_V40(
                                             values.getString(IP, ""),
                                             values.getNumber(LOGIN_PORT, 0).shortValue(),
                                             values.getString(USERNAME, ""),
                                             values.getString(PASSWORD, "")))
                                     .flatMap(tp2 -> {
                                         log.info("[media sdk] userId：{}", tp2.getT1());
                                         Map<String, Object> config = new HashMap<>();
                                         config.put(USER_ID, tp2.getT1());
                                         config.put(CHARSET, tp2.getT2().byCharEncodeType);
                                         config.put(CHANNEL_NUM, tp2.getT2().struDeviceV30.byChanNum);
                                         return device.setConfigs(config).thenReturn(tp2.getT1());
                                     }));
    }

    private Mono<Boolean> setSdkProperties(String deviceId,
                                           Map<String, Object> properties) {
        return registry.getDevice(deviceId)
                       .flatMap(device -> Mono
                               .zip(
                                       this.getUser(device),
                                       this.getPluginProduct(device)
                               ))
                       .map(tp2 -> tp2.getT2().setSdkProperties(tp2.getT1(), properties, sdk));

    }

    /**
     * 获取插件内部的产品信息
     *
     * @param device 设备操作
     * @return 插件内部产品
     */
    private Mono<PluginProduct> getPluginProduct(DeviceOperator device) {
        return device
                .getProduct()
                // 内部产品ID
                .map(DeviceProductOperator::getId)
                .doOnNext(id -> log.info("plugin id: {}", id))
                .mapNotNull(pluginProducts::get);
    }

    @Override
    public Publisher<? extends DeviceMessage> execute(DeviceMessage message) {

        // 读取属性
        if (message instanceof ReadPropertyMessage) {
            return registry
                    .getDevice(message.getDeviceId())
                    .flatMap(this::getDeviceInfo)
                    .map(DeviceInfo::getProperties)
                    .map(((ReadPropertyMessage) message).newReply()::success);
        }
        //修改属性
        else if (message instanceof WritePropertyMessage) {
            Map<String, Object> props = ((WritePropertyMessage) message).getProperties();
            return this
                    .setSdkProperties(message.getDeviceId(), props)
                    .map(success -> success ? ((WritePropertyMessage) message).newReply().success(props) :
                            ((WritePropertyMessage) message).newReply().error(new BusinessException("error")))
                    .onErrorResume(err -> Mono.just(((WritePropertyMessage) message)
                                                            .newReply()
                                                            .error(err)));
        }

        return super.execute(message);
    }

    @Override
    public Mono<MediaSdkStream> getRealplayStreamUrl(String deviceId, MediaSdkChannelConfig config) {
        return registry
                .getDevice(deviceId)
                .flatMap(device -> Mono.zip(
                        getUser(device),
                        device.getSelfConfigs(IP, RTSP_PORT, USERNAME, PASSWORD)
                ))
                .map(tp2 -> {
                    // 地址格式：rtsp://{username}:{password}@{ip}:{port}/{codec}/{channel}/{subtype}/av_stream
                    String codec = StringUtils.hasText(config.getCodec()) ? config.getCodec() : "h264";
                    String channel = StringUtils.hasText(config.getChannel()) ? config.getCodec() : "1";
                    String subtype = StringUtils.hasText(config.getSubtype()) ? config.getCodec() : "main";
                    String url = "rtsp://" +
                            tp2.getT2().getString(USERNAME, "") +
                            ":" +
                            tp2.getT2().getString(PASSWORD, "") +
                            "@" +
                            tp2.getT2().getString(IP, "") +
                            ":" +
                            tp2.getT2().getString(RTSP_PORT, String.valueOf(sdk.queryRtspPort(tp2.getT1()))) +
                            "/" +
                            codec +
                            "/" +
                            channel +
                            "/" +
                            subtype +
                            "/av_stream";
                    return new MediaSdkStream(url);
                });
    }

    @Override
    public Mono<MediaSdkStream> getPlaybackStreamUrl(String deviceId, MediaSdkDeviceControl control) {
        if (control.getRecordQuery() == null) {
            return Mono.error(new BusinessException("recordQuery参数不能为空"));
        }
        return registry
                .getDevice(deviceId)
                .flatMap(device -> Mono.zip(
                        getUser(device),
                        device.getSelfConfigs(IP, RTSP_PORT, USERNAME, PASSWORD)
                ))
                .map(tp2 -> {
                    // TODO: 2023/11/14 时间查询条件未生效？
                    // 地址格式：rtsp://username:password@ip:port/Streaming/tracks/{channel}?starttime={starttime}&endtime={endtime}
                    String url = "rtsp://" +
                            tp2.getT2().getString(USERNAME, "") +
                            ":" +
                            tp2.getT2().getString(PASSWORD, "") +
                            "@" +
                            tp2.getT2().getString(IP, "") +
                            ":" +
                            tp2.getT2().getString(RTSP_PORT, String.valueOf(sdk.queryRtspPort(tp2.getT1()))) +
                            "/Streaming/tracks/" +
                            control.getChannelId();
                    LocalDateTime start = control.getRecordQuery().getStart();
                    if (start != null) {
                        String starttime = String.format("%d%d%dT%d%d%d",
                                                         start.getYear(),
                                                         start.getMonthValue(),
                                                         start.getDayOfMonth(),
                                                         start.getHour(),
                                                         start.getMinute(),
                                                         start.getSecond());
                        url = url + "?starttime=" + starttime;
                    }
                    LocalDateTime end = control.getRecordQuery().getEnd();
                    if (end != null) {
                        String endtime =  String.format("%d%d%dT%d%d%d",
                                                        end.getYear(),
                                                        end.getMonthValue(),
                                                        end.getDayOfMonth(),
                                                        end.getHour(),
                                                        end.getMinute(),
                                                        end.getSecond());
                        if (start != null) {
                            url = url + "&endtime=" + endtime;
                        } else {
                            url = url + "?endtime=" + endtime;
                        }
                    }

                    return new MediaSdkStream(url);
                });
    }

    @Override
    public Flux<MediaSdkPreset> queryPreset(String deviceId) {
        return registry
                .getDevice(deviceId)
                .flatMap(device -> Mono
                        .zip(
                                getUser(device),
                                device.getConfig(CHARSET).map(value -> value.as(Byte.class))
                        ))
                .flatMapMany(tp2 -> Flux.fromIterable(sdk.queryPreset(tp2.getT1()))
                                        .map(data -> buildPreset(data, getCharset(tp2.getT2()))));
    }

    @SneakyThrows
    private MediaSdkPreset buildPreset(HCNetSDK.NET_DVR_PRESET_NAME data, Charset charset) {
        MediaSdkPreset preset = new MediaSdkPreset(
                String.valueOf(data.wPresetNum), new String(data.byName, charset).trim()
        );
        Map<String, Object> others = new HashMap<>();
        others.put("wPanPos", data.wPanPos);
        others.put("wTiltPos", data.wTiltPos);
        others.put("wZoomPos", data.wZoomPos);
        preset.setOthers(others);
        return preset;
    }

    private Charset getCharset(Byte charset) {
        switch (charset) {
            case 0:
                return Charset.defaultCharset();
            case 1:
                return Charset.forName("GB2312");
            case 2:
                return new GBK();
            case 3:
                return Charset.forName("BIG5");
            case 6:
                return StandardCharsets.UTF_8;
            case 7:
                return StandardCharsets.ISO_8859_1;
            default:
                return Charset.defaultCharset();
        }
    }

    @Override
    public Mono<Boolean> presetControl(String deviceId, MediaSdkDeviceControl control) {
        return registry
                .getDevice(deviceId)
                .flatMap(this::getUser)
                .flatMap(userId -> Mono.fromSupplier(() -> sdk.presetControl(userId, control)));
    }

    @Override
    public Mono<Boolean> ptzControl(String deviceId, MediaSdkDeviceControl control) {
        return registry
                .getDevice(deviceId)
                .flatMap(device -> Mono
                        .zip(
                                this.getUser(device),
                                device
                                        .getConfig(LAST_PTZ_COMMAND)
                                        .map(Value::asInt)
                                        .defaultIfEmpty(-1)
                        )
                        .flatMap(tp2 -> Mono.fromSupplier(() -> sdk.ptzControl(tp2.getT1(), control, tp2.getT2())))
                        .flatMap(ptzCommand -> {
                            if (ptzCommand.isSuccess()) {
                                return device
                                        .setConfig(LAST_PTZ_COMMAND, ptzCommand.getCommand())
                                        .thenReturn(ptzCommand.isSuccess());
                            }
                            return Mono.just(ptzCommand.isSuccess());
                        }));
    }

    @Override
    public Mono<Boolean> record(String deviceId,  MediaSdkDeviceControl control) {
        return registry
                .getDevice(deviceId)
                .flatMap(this::getUser)
                .flatMap(userId -> Mono.fromSupplier(() -> {
                    int channel = Integer.parseInt(control.getChannelId());
                    if (control.getRecord() == MediaSdkDeviceControl.Record.START) {
                        return sdk.startRecord(userId, channel);
                    } else {
                        return sdk.stopRecord(userId, channel);
                    }
                }));
    }

    @Override
    public Flux<MediaSdkRecord> queryRecord(String deviceId, MediaSdkDeviceControl control) {
        return registry
                .getDevice(deviceId)
                .flatMap(device -> Mono
                        .zip(
                                getUser(device),
                                device.getConfig(CHARSET).map(value -> value.as(Byte.class))
                        ))
                .flatMapMany(tp2 -> Mono
                        .fromSupplier(() -> sdk.queryRecord(
                                tp2.getT1(),
                                Integer.parseInt(control.getChannelId()),
                                control.getRecordQuery().getStart(),
                                control.getRecordQuery().getEnd())
                        )
                        .flatMapMany(Flux::fromIterable)
                        .map(data -> {
                            MediaSdkRecord record = new MediaSdkRecord();
                            record.setDeviceId(deviceId);
                            record.setChannelId(control.getChannelId());
                            record.setName(new String(data.sFileName, getCharset(tp2.getT2())).trim());
                            record.setStartTime(data.struStartTime.toTimestamp());
                            record.setEndTime(data.struStopTime.toTimestamp());
                            record.setFileSize((long) data.dwFileSize);
                            return record;
                        }));
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
