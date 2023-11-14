package org.jetlinks.plugin.internal.media;

import lombok.Getter;
import lombok.Setter;
import org.jetlinks.core.message.function.FunctionParameter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 通道配置信息.
 *
 * @author zhangji 2023/10/19
 */
@Getter
@Setter
public class MediaSdkChannelConfig {

    // 编码格式，例如h264、MPEG-4、mpeg4等
    private String codec;

    // 通道号
    private String channel;

    // 码流类型
    private String subtype;

    private Map<String, Object> extraConfig;

    public Object getExtraConfig(String key) {
        if (extraConfig == null) {
            return null;
        }
        return extraConfig.get(key);
    }

    public Object getExtraConfig(String key, Object defaultValue) {
        if (extraConfig == null) {
            return defaultValue;
        }
        return extraConfig.getOrDefault(key, defaultValue);
    }

    public List<FunctionParameter> toFunctionInputs() {
        List<FunctionParameter> parameters = new ArrayList<>();
        parameters.add(new FunctionParameter("codec", codec));
        parameters.add(new FunctionParameter("channel", channel));
        parameters.add(new FunctionParameter("subtype", subtype));
        parameters.add(new FunctionParameter("extraConfig", extraConfig));
        return parameters;
    }

}
