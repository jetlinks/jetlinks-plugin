package org.jetlinks.plugin.mock;

import com.google.common.collect.Maps;
import io.netty.util.internal.ThreadLocalRandom;
import org.apache.commons.collections4.CollectionUtils;
import org.hswebframework.web.api.crud.entity.PagerResult;
import org.hswebframework.web.api.crud.entity.QueryParamEntity;
import org.hswebframework.web.id.IDGenerator;
import org.jetlinks.core.command.CommandHandler;
import org.jetlinks.core.command.CommandUtils;
import org.jetlinks.core.device.DeviceOperator;
import org.jetlinks.core.device.DeviceState;
import org.jetlinks.core.message.DeviceMessage;
import org.jetlinks.core.message.Headers;
import org.jetlinks.core.message.property.ReportPropertyMessage;
import org.jetlinks.core.metadata.*;
import org.jetlinks.core.metadata.types.*;
import org.jetlinks.plugin.core.PluginContext;
import org.jetlinks.plugin.internal.device.DeviceGatewayPlugin;
import org.jetlinks.reactor.ql.utils.CastUtils;
import org.jetlinks.sdk.server.commons.cmd.QueryPagerCommand;
import org.jetlinks.sdk.server.device.DeviceInfo;
import org.jetlinks.sdk.server.device.cmd.QueryDevicePageCommand;
import reactor.core.Disposable;
import reactor.core.Disposables;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.*;

public class MockDevicePlugin extends DeviceGatewayPlugin {
    private final Disposable.Composite disposable = Disposables.composite();
    static final String MOCK_INTERVAL = "mockInterval";
    static final String MOCK_NUMBER = "mockNumber";
    private final Duration mockInterval;
    private final int mockNumber;

    public MockDevicePlugin(String id, PluginContext context) {
        super(id, context);
        mockInterval = Duration.ofSeconds(
            context.environment()
                   .getProperty(MOCK_INTERVAL)
                   .map(CastUtils::castNumber)
                   .orElse(5)
                   .intValue()
        );
        mockNumber = context
            .environment()
            .getProperty(MOCK_NUMBER)
            .map(CastUtils::castNumber)
            .orElse(1000)
            .intValue();

        registerHandler(
            CommandHandler.of(
                QueryPagerCommand.metadata(custom -> custom.setId(CommandUtils.getCommandIdByType(QueryDevicePageCommand.class))),
                (cmd, ignore) -> queryDevicePage(cmd),
                QueryDevicePageCommand::new
            )
        );
    }

    public Mono<PagerResult<DeviceInfo>> queryDevicePage(QueryDevicePageCommand cmd) {
        QueryParamEntity e = cmd.asQueryParam();

        List<DeviceInfo> data = new ArrayList<>(e.getPageSize());
        int offset = e.getPageIndex() * e.getPageSize() + 1;

        for (int i = 0; i < e.getPageSize(); i++) {
            DeviceInfo inf = new DeviceInfo();
            inf.setId("mock-device-" + (offset + i));
            inf.setName("模拟设备_" + (offset + i));
            inf.setState(org.jetlinks.sdk.server.device.DeviceState.online);

            data.add(inf);
        }

        return Mono.just(PagerResult.of(mockNumber, data, e));
    }

    @Override
    public Mono<ConfigMetadata> getProductConfigMetadata(String productId) {
        return Mono.just(
            new DefaultConfigMetadata("模拟配置", "")
                .add(Headers.keepOnlineTimeoutSeconds.getKey(), "心跳间隔", IntType.GLOBAL)
        );
    }

    protected Flux<DeviceMessage> createMessage(DeviceOperator device,
                                                DeviceMetadata metadata) {
        return createMockData(device, metadata.getProperties());
    }

    protected Flux<DeviceMessage> createMockData(DeviceOperator device,
                                                 List<PropertyMetadata> properties) {
        if (CollectionUtils.isEmpty(properties)) {
            return Flux.empty();
        }
        Map<String, Object> data = Maps.newHashMapWithExpectedSize(properties.size());
        for (PropertyMetadata property : properties) {
            String source = (String) property.getExpand("source").orElse(null);
            if (source == null || "device".equals(source)) {
                //属性来源不是设备的不模拟
                data.put(property.getId(), mockData(property.getValueType()));
            }
        }

        ReportPropertyMessage message = new ReportPropertyMessage();
        message.setProperties(data);
        message.setDeviceId(device.getDeviceId());
        return Flux.just(message);
    }


    @Override
    public Mono<Byte> getDeviceState(DeviceOperator device) {
        return Mono
            .delay(Duration.ofMillis(ThreadLocalRandom.current().nextInt(
                1, 50
            )))
            .thenReturn(DeviceState.online);
    }

    private Object mockData(DataType type) {
        if (type instanceof NumberType) {
            Number min = ((NumberType<?>) type).getMin(),
                max = ((NumberType<?>) type).getMax();
            return mockNumber(type,
                              min == null ? 0 : min,
                              max == null ? Short.MAX_VALUE : max);
        }

        if (type instanceof StringType) {
            return IDGenerator.RANDOM.generate();
        }
        if (type instanceof BooleanType) {
            return ThreadLocalRandom.current().nextBoolean();
        }
        if (type instanceof DateTimeType) {
            return System.currentTimeMillis();
        }
        if (type instanceof EnumType) {
            List<EnumType.Element> values = ((EnumType) type).getElements();
            return values.get(ThreadLocalRandom.current().nextInt(values.size()));
        }
        if (type instanceof ArrayType) {
            return Collections.singleton(mockData(((ArrayType) type).getElementType()));
        }
        if (type instanceof ObjectType) {
            Map<String, Object> data = new HashMap<>();
            for (PropertyMetadata property : ((ObjectType) type).getProperties()) {
                data.put(property.getId(), mockData(property.getValueType()));
            }
            return data;
        }
        // todo 更多文件类型
        return null;
    }

    private Number mockNumber(DataType type, Number min, Number max) {
        //  RandomDataGenerator random = new RandomDataGenerator();

        //todo 支持更多类型
        if (type instanceof IntType) {
            return ThreadLocalRandom
                .current()
                .nextInt(min.intValue(), max.intValue());
        }
        if (type instanceof ShortType) {
            return (short) ThreadLocalRandom
                .current()
                .nextInt(min.intValue(), max.intValue());
        }
        if (type instanceof LongType) {
            return (short) ThreadLocalRandom
                .current()
                .nextLong(min.intValue(), max.intValue());
        }

        return (float) ThreadLocalRandom
            .current()
            .nextDouble(min.doubleValue(), max.doubleValue());
    }

    protected Mono<Void> doReport() {
        return getPlatformDevices()
            .flatMap(device -> device
                         .getMetadata()
                         .flatMapMany(metadata -> createMessage(device, metadata)),
                     16)
            .flatMap(this::handleMessage)
            .then();
    }

    @Override
    protected Mono<Void> doShutdown() {
        disposable.dispose();
        return super.doShutdown();
    }

    @Override
    protected Mono<Void> doStart() {
        disposable.add(
            context()
                .scheduler()
                .interval("report-timer",
                          Mono.defer(this::doReport),
                          mockInterval
                )
        );

        return super.doStart();
    }


}
