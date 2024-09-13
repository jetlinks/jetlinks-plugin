package org.jetlinks.plugin.internal.ai;

import lombok.AllArgsConstructor;
import org.jetlinks.core.command.CommandHandler;
import org.jetlinks.core.command.CommandSupport;
import org.jetlinks.plugin.core.AbstractPlugin;
import org.jetlinks.plugin.core.PluginContext;
import org.jetlinks.plugin.internal.ai.command.AddAiModelCommand;
import org.jetlinks.plugin.internal.ai.command.GetAiDomainCommand;
import org.jetlinks.plugin.internal.ai.command.RemoveAiModelCommand;
import org.jetlinks.sdk.server.SdkServices;
import org.jetlinks.sdk.server.ai.AiCommandSupports;
import org.jetlinks.sdk.server.ai.AiDomain;
import org.jetlinks.sdk.server.ai.cv.ImageRecognitionCommand;
import org.jetlinks.sdk.server.ai.model.AiModelInfo;
import org.jetlinks.sdk.server.ai.model.AiModelPortrait;
import org.jetlinks.sdk.server.commons.cmd.QueryListCommand;
import org.jetlinks.sdk.server.file.DownloadFileCommand;
import org.jetlinks.sdk.server.utils.ConverterUtils;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Map;

/**
 * AI插件,提供AI相关能力.
 *
 * @author zhouhao
 * @see org.jetlinks.sdk.server.ai.cv.ObjectDetectionCommand
 * @see org.jetlinks.sdk.server.ai.cv.PushMediaStreamCommand
 * @see ImageRecognitionCommand
 * @since 1.0.3
 */
public abstract class AiPlugin extends AbstractPlugin {

    protected final CommandSupport modelManager;
    protected final CommandSupport fileService;

    public AiPlugin(String id, PluginContext context) {
        super(id, context);
        this.modelManager = getService(SdkServices.aiService + ":" + AiCommandSupports.modelManager);
        this.fileService = getService(SdkServices.fileService);

        //平台会通过命令方式获取相关信息
        registerHandler(GetAiDomainCommand.class,
                        CommandHandler.of(
                            GetAiDomainCommand.metadata(),
                            (ignore, that) -> Mono.just(getDomain()),
                            GetAiDomainCommand::new
                        ));

        registerHandler(AddAiModelCommand.class,
                        CommandHandler.of(
                            AddAiModelCommand.metadata(),
                            (cmd, that) -> addModel(cmd.getModel()),
                            AddAiModelCommand::new
                        ));

        registerHandler(RemoveAiModelCommand.class,
                        CommandHandler.of(
                            RemoveAiModelCommand.metadata(),
                            (cmd, that) -> removeModel(cmd.getId()),
                            RemoveAiModelCommand::new
                        ));

    }

    /**
     * 获取插件适用的AI领域
     *
     * @return AiDomain
     */
    public abstract AiDomain getDomain();

    /**
     * 添加模型
     *
     * @return 解析后的模型画像
     */
    public abstract Mono<AiModelPortrait> addModel(AiModelInfo model);

    /**
     * 移除模型
     */
    public abstract Mono<Void> removeModel(String modelId);

    /**
     * 获取平台提供命令的服务
     *
     * @param service 服务ID
     * @return CommandSupport
     * @see SdkServices
     */
    protected CommandSupport getService(String service) {
        return context()
            .services()
            .getService(CommandSupport.class, service)
            .orElseThrow(() -> new UnsupportedOperationException(service + " not found"));
    }

    /**
     * 获取模型信息
     *
     * @param id 模型ID
     * @return 模型信息
     */
    protected final Flux<AiModel> getModels(Collection<String> id) {
        //查询模型信息
        return modelManager
            .execute(
                QueryListCommand
                    .of(AiModelInfo.class)
                    .dsl(query -> query.in("id", id)))
            .map(AiModelImpl::new);
    }


    @AllArgsConstructor
    private class AiModelImpl implements AiModel {
        private final AiModelInfo info;

        @Override
        public String getId() {
            return info.getId();
        }

        @Override
        public String getName() {
            return info.getName();
        }

        @Override
        public String getProvider() {
            return info.getProvider() == null
                ? null
                : info.getProvider().getValue();
        }

        @Override
        public String getTarget() {
            return info.getTarget() == null
                ? null
                : info.getTarget().getValue();
        }

        @Override
        public Mono<InputStream> readContent() {
            return fileService
                .execute(new DownloadFileCommand().setUrl(info.getFileUrl()))
                .map(ConverterUtils::convertDataBuffer)
                .as(DataBufferUtils::join)
                .map(DataBuffer::asInputStream);
        }

        @Override
        public Mono<Void> writeContentTo(OutputStream output) {
            return DataBufferUtils
                .write(
                    fileService
                        .execute(new DownloadFileCommand().setUrl(info.getFileUrl()))
                        .map(ConverterUtils::convertDataBuffer),
                    output
                )
                .doOnNext(DataBufferUtils::release)
                .then();
        }

        @Override
        public Map<String, Object> getOthers() {
            return info.getOthers();
        }
    }

}
