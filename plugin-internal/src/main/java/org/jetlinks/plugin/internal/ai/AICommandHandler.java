package org.jetlinks.plugin.internal.ai;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jetlinks.core.command.Command;
import org.jetlinks.core.command.CommandHandler;
import org.jetlinks.core.command.CommandSupport;
import org.jetlinks.core.command.CommandUtils;
import org.jetlinks.core.metadata.DataType;
import org.jetlinks.core.metadata.FunctionMetadata;
import org.jetlinks.core.metadata.PropertyMetadata;
import org.jetlinks.core.metadata.types.ObjectType;
import org.jetlinks.core.utils.MetadataUtils;
import org.jetlinks.sdk.server.ai.AiOutput;
import org.jetlinks.sdk.server.ai.cv.ImageRecognitionCommand;
import org.jetlinks.sdk.server.ai.cv.ObjectDetectionCommand;
import org.jetlinks.sdk.server.ai.cv.ObjectDetectionResult;
import org.springframework.core.ResolvableType;
import reactor.core.publisher.Flux;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Supplier;

/**
 * @author gyl
 * @since 2.3
 */
@AllArgsConstructor
public class AICommandHandler<C extends Command<R>, R> implements CommandHandler<C, R> {

    private final Supplier<FunctionMetadata> description;

    private final BiFunction<C, CommandSupport, R> handler;

    private final Supplier<C> commandBuilder;

    /**
     * @see AiOutput#toLightWeighMap()
     */
    @Getter
    private final Collection<PropertyMetadata> lightWeightProperties;

    /**
     * @see AiOutput#flat()
     */
    @Getter
    private final Collection<PropertyMetadata> flatProperties;

    /**
     * @see AiOutput#lightWeighFlat()
     */
    @Getter
    private final Collection<PropertyMetadata> flatLightWeightProperties;


    /**
     * 注册自定义ai命令
     *
     * @param metadata                  ai命令模型解析
     * @param executor                  执行逻辑
     * @param commandBuilder            ai命令
     * @param lightWeightProperties     轻量响应模型
     * @param flatProperties            平铺响应模型
     * @param flatLightWeightProperties 平铺轻量响应模型
     * @param <C>                       ai命令
     * @param <R>                       响应
     */
    @SuppressWarnings("all")
    static <R, C extends Command<R>> AICommandHandler<C, R> of(Supplier<FunctionMetadata> metadata,
                                                               BiFunction<C, CommandSupport, R> executor,
                                                               Supplier<C> commandBuilder,
                                                               AiOutputMetadataBuilder lightWeightProperties,
                                                               AiOutputMetadataBuilder flatProperties,
                                                               AiOutputMetadataBuilder flatLightWeightProperties) {
        Class<C> commandClass = (Class<C>) commandBuilder.get().getClass();
        return new AICommandHandler<>(metadata, executor, commandBuilder,
                                      lightWeightProperties.generateMetadata(commandClass),
                                      flatProperties.generateMetadata(commandClass),
                                      flatLightWeightProperties.generateMetadata(commandClass));
    }

    /**
     * 注册自定义ai命令，所有特殊响应默认使用轻量模型
     */
    @SuppressWarnings("all")
    static <R, C extends Command<R>> AICommandHandler<C, R> of(Supplier<FunctionMetadata> metadata,
                                                               BiFunction<C, CommandSupport, R> executor,
                                                               Supplier<C> commandBuilder,
                                                               AiOutputMetadataBuilder lightWeightProperties,
                                                               AiOutputMetadataBuilder flatProperties) {
        Class<C> commandClass = (Class<C>) commandBuilder.get().getClass();
        Collection<PropertyMetadata> flatPropertyMetadata = flatProperties.generateMetadata(commandClass);
        return of(metadata, executor, commandBuilder,
                  lightWeightProperties.generateMetadata(commandClass),
                  flatPropertyMetadata,
                  flatPropertyMetadata);
    }

    /**
     * 注册自定义ai命令，平铺轻量响应默认使用平铺模型
     */
    @SuppressWarnings("all")
    static <R, C extends Command<R>> AICommandHandler<C, R> of(Supplier<FunctionMetadata> metadata,
                                                               BiFunction<C, CommandSupport, R> executor,
                                                               Supplier<C> commandBuilder,
                                                               AiOutputMetadataBuilder lightWeightProperties) {
        Class<C> commandClass = (Class<C>) commandBuilder.get().getClass();
        Collection<PropertyMetadata> PropertyMetadata = lightWeightProperties.generateMetadata(commandClass);
        return of(metadata, executor, commandBuilder,
                  PropertyMetadata,
                  PropertyMetadata,
                  PropertyMetadata);
    }

    static <R, C extends Command<R>> AICommandHandler<C, R> of(Supplier<FunctionMetadata> metadata,
                                                               BiFunction<C, CommandSupport, R> executor,
                                                               Supplier<C> commandBuilder,
                                                               Collection<PropertyMetadata> lightWeightProperties,
                                                               Collection<PropertyMetadata> flatProperties,
                                                               Collection<PropertyMetadata> flatLightWeightProperties) {
        return new AICommandHandler<>(metadata, executor, commandBuilder, lightWeightProperties, flatProperties, flatLightWeightProperties);
    }

    static <R, C extends Command<R>> AICommandHandler<C, R> of(Supplier<FunctionMetadata> metadata,
                                                               BiFunction<C, CommandSupport, R> executor,
                                                               Supplier<C> commandBuilder,
                                                               Collection<PropertyMetadata> lightWeightProperties,
                                                               Collection<PropertyMetadata> flatProperties) {
        return new AICommandHandler<>(metadata, executor, commandBuilder, lightWeightProperties, flatProperties, flatProperties);
    }

    static <R, C extends Command<R>> AICommandHandler<C, R> of(Supplier<FunctionMetadata> metadata,
                                                               BiFunction<C, CommandSupport, R> executor,
                                                               Supplier<C> commandBuilder,
                                                               Collection<PropertyMetadata> properties) {
        return new AICommandHandler<>(metadata, executor, commandBuilder, properties, properties, properties);
    }


    /**
     * 注册目标检测命令
     *
     * @param executor 执行逻辑
     */
    static AICommandHandler<ObjectDetectionCommand, Flux<ObjectDetectionResult>> ofObjectDetectionCommand(BiFunction<ObjectDetectionCommand, CommandSupport, Flux<ObjectDetectionResult>> executor) {
        List<PropertyMetadata> flatMetadata = getClassMetadata(ResolvableType.forClass(ObjectDetectionResult.FlatData.class));
        return new AICommandHandler<>(ObjectDetectionCommand::metadata,
                                      executor,
                                      ObjectDetectionCommand::new,
                                      getClassMetadata(ResolvableType.forClass(ObjectDetectionResult.class)),
                                      flatMetadata,
                                      flatMetadata);
    }

    /**
     * 注册图像识别命令
     *
     * @param executor 执行逻辑
     */
    static AICommandHandler<ImageRecognitionCommand, Flux<ObjectDetectionResult>> ofImageRecognitionCommand(BiFunction<ImageRecognitionCommand, CommandSupport, Flux<ObjectDetectionResult>> executor) {
        List<PropertyMetadata> flatMetadata = getClassMetadata(ResolvableType.forClass(ObjectDetectionResult.FlatData.class));
        return new AICommandHandler<>(ImageRecognitionCommand::metadata,
                                      executor,
                                      ImageRecognitionCommand::new,
                                      getClassMetadata(ResolvableType.forClass(ObjectDetectionResult.class)),
                                      flatMetadata,
                                      flatMetadata);
    }

    public Collection<PropertyMetadata> getAiOutputPropertyMetadata(AiOutputMetadataType type) {
        switch (type) {
            case flat:
                return flatProperties;
            case lightWeighFlat:
                return flatLightWeightProperties;
            case lightWeigh:
                return lightWeightProperties;
        }
        return Collections.emptyList();
    }

    @Override
    public R handle(@Nonnull C command, @Nonnull CommandSupport support) {
        return handler.apply(command, support);
    }

    @Nonnull
    @Override
    public C createCommand() {
        return commandBuilder.get();
    }

    @Override
    public FunctionMetadata getMetadata() {
        return description.get();
    }


    /**
     * 获取命令的响应类型R为模型描述，扫描参数属性注释
     * <pre>{@code @Schema(title = "属性名")}</pre>
     */
    protected static <T extends Command<?>> List<PropertyMetadata> getCommandOutputMetadata(Class<T> aclass) {
        ResolvableType responseDataType = CommandUtils.getCommandResponseDataType(aclass);
        return getClassMetadata(responseDataType);
    }

    /**
     * 扫描类中参数属性注释
     * <pre>{@code @Schema(title = "属性名")}</pre>
     */
    protected static List<PropertyMetadata> getClassMetadata(ResolvableType responseDataType) {
        DataType dataType = MetadataUtils.parseType(responseDataType);
        if (dataType instanceof ObjectType) {
            return ((ObjectType) dataType).getProperties();
        }
        return Collections.emptyList();
    }


}
