package org.jetlinks.plugin.internal.ai;

import com.google.common.collect.Maps;
import org.apache.commons.collections4.CollectionUtils;
import org.jetlinks.core.command.Command;
import org.jetlinks.core.command.CommandHandler;
import org.jetlinks.core.command.CommandSupport;
import org.jetlinks.core.command.CommandUtils;
import org.jetlinks.core.metadata.DataType;
import org.jetlinks.core.metadata.FunctionMetadata;
import org.jetlinks.core.metadata.PropertyMetadata;
import org.jetlinks.core.metadata.types.ObjectType;
import org.jetlinks.core.utils.MetadataUtils;
import org.jetlinks.plugin.core.AbstractPlugin;
import org.jetlinks.plugin.core.PluginContext;
import org.jetlinks.sdk.server.ai.cv.ImageRecognitionCommand;
import org.jetlinks.sdk.server.ai.cv.ObjectDetectionCommand;
import org.jetlinks.sdk.server.ai.cv.ObjectDetectionResult;
import org.springframework.core.ResolvableType;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * @author gyl
 * @since 2.3
 */
public abstract class AbstractAiPluginCommandSupport extends AbstractPlugin {

    protected static Map<String, List<PropertyMetadata>> OUTPUT_METADATA_CACHE = new ConcurrentHashMap<>();

    public AbstractAiPluginCommandSupport(String id, PluginContext context) {
        super(id, context);
    }


    /**
     * 注册自定义ai命令
     *
     * @param metadata                  ai命令模型解析
     * @param lightWeightProperties     轻量响应模型
     * @param flatProperties            平铺响应模型
     * @param flatLightWeightProperties 平铺轻量响应模型
     * @param executor                  执行逻辑
     * @param factory                   ai命令
     * @param <C>                       ai命令
     * @param <R>                       响应
     */
    @SuppressWarnings("all")
    <C extends Command<R>, R> void registerAiCommand(FunctionMetadata metadata,
                                                     List<PropertyMetadata> lightWeightProperties,
                                                     List<PropertyMetadata> flatProperties,
                                                     List<PropertyMetadata> flatLightWeightProperties,
                                                     BiFunction<C, CommandSupport, R> executor,
                                                     Supplier<C> factory) {
        C command = factory.get();
        String commandId = factory.get().getCommandId();
        OUTPUT_METADATA_CACHE.put(generateMetadataKey(commandId, AiOutputMetadataType.lightWeigh), lightWeightProperties);
        OUTPUT_METADATA_CACHE.put(generateMetadataKey(commandId, AiOutputMetadataType.flat), flatProperties);
        OUTPUT_METADATA_CACHE.put(generateMetadataKey(commandId, AiOutputMetadataType.lightWeighFlat), flatLightWeightProperties);
        registerHandler((Class<C>) command.getClass(),
                        CommandHandler.of(
                            metadata,
                            executor,
                            factory
                        ));
    }

    /**
     * 注册自定义ai命令，所有特殊响应默认使用轻量模型
     */
    <C extends Command<R>, R> void registerAiCommand(FunctionMetadata metadata,
                                                     List<PropertyMetadata> lightWeightProperties,
                                                     BiFunction<C, CommandSupport, R> executor,
                                                     Supplier<C> factory) {
        registerAiCommand(metadata, lightWeightProperties, lightWeightProperties, lightWeightProperties, executor, factory);
    }

    /**
     * 注册自定义ai命令，平铺轻量响应默认使用平铺模型
     */
    <C extends Command<R>, R> void registerAiCommand(FunctionMetadata metadata,
                                                     List<PropertyMetadata> lightWeightProperties,
                                                     List<PropertyMetadata> flatProperties,
                                                     BiFunction<C, CommandSupport, R> executor,
                                                     Supplier<C> factory) {
        registerAiCommand(metadata, lightWeightProperties, flatProperties, flatProperties, executor, factory);
    }

    /**
     * 注册目标检测命令
     *
     * @param executor 执行逻辑
     */
    void registerObjectDetectionCommand(BiFunction<ObjectDetectionCommand, CommandSupport, Flux<ObjectDetectionResult>> executor) {
        ObjectDetectionCommand command = new ObjectDetectionCommand();
        OUTPUT_METADATA_CACHE.put(generateMetadataKey(command.getCommandId(), AiOutputMetadataType.lightWeigh), getCommandOutputMetadata(ObjectDetectionCommand.class));
        List<PropertyMetadata> flatMetadata = getClassMetadata(ResolvableType.forClass(ObjectDetectionResult.FlatData.class));
        OUTPUT_METADATA_CACHE.put(generateMetadataKey(command.getCommandId(), AiOutputMetadataType.flat), flatMetadata);
        OUTPUT_METADATA_CACHE.put(generateMetadataKey(command.getCommandId(), AiOutputMetadataType.lightWeighFlat), flatMetadata);
        registerHandler(ObjectDetectionCommand.class,
                        CommandHandler.of(
                            ObjectDetectionCommand.metadata(),
                            executor,
                            ObjectDetectionCommand::new
                        ));
    }

    /**
     * 注册图像识别命令
     *
     * @param executor 执行逻辑
     */
    void registerImageRecognitionCommand(BiFunction<ImageRecognitionCommand, CommandSupport, Flux<ObjectDetectionResult>> executor) {
        ImageRecognitionCommand command = new ImageRecognitionCommand();
        OUTPUT_METADATA_CACHE.put(generateMetadataKey(command.getCommandId(), AiOutputMetadataType.lightWeigh), getCommandOutputMetadata(ImageRecognitionCommand.class));
        List<PropertyMetadata> flatMetadata = getClassMetadata(ResolvableType.forClass(ObjectDetectionResult.FlatData.class));
        OUTPUT_METADATA_CACHE.put(generateMetadataKey(command.getCommandId(), AiOutputMetadataType.flat), flatMetadata);
        OUTPUT_METADATA_CACHE.put(generateMetadataKey(command.getCommandId(), AiOutputMetadataType.lightWeighFlat), flatMetadata);
        registerHandler(ImageRecognitionCommand.class,
                        CommandHandler.of(
                            ImageRecognitionCommand.metadata(),
                            executor,
                            ImageRecognitionCommand::new
                        ));
    }

    /**
     * 获取命令的响应类型R为模型描述，扫描参数属性注释
     * <pre>{@code @Schema(title = "属性名")}</pre>
     */
    <T extends Command<?>> List<PropertyMetadata> getCommandOutputMetadata(Class<T> aclass) {
        ResolvableType responseDataType = CommandUtils.getCommandResponseDataType(aclass);
        return getClassMetadata(responseDataType);
    }

    /**
     * 扫描类中参数属性注释
     * <pre>{@code @Schema(title = "属性名")}</pre>
     */
    List<PropertyMetadata> getClassMetadata(ResolvableType responseDataType) {
        DataType dataType = MetadataUtils.parseType(responseDataType);
        if (dataType instanceof ObjectType) {
            return ((ObjectType) dataType).getProperties();
        }
        return Collections.emptyList();
    }

    static String generateMetadataKey(String commandId, AiOutputMetadataType type) {
        return String.join(":", commandId, type.name());
    }


    protected Mono<Map<String, List<PropertyMetadata>>> getOutputMetadata(Collection<String> commandId, AiOutputMetadataType type) {
        if (CollectionUtils.isEmpty(commandId)) {
            return Mono.fromRunnable(() -> Maps.filterKeys(OUTPUT_METADATA_CACHE, key -> key.contains(":" + type.name())));
        }

        return Mono.fromRunnable(() -> {
            Set<String> keys = commandId
                .stream()
                .map(c -> generateMetadataKey(c, type))
                .collect(Collectors.toSet());
            Maps.filterKeys(OUTPUT_METADATA_CACHE, keys::contains);
        });
    }
}
