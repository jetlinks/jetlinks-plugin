package org.jetlinks.plugin.internal.media;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.jetlinks.core.message.function.FunctionParameter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 视频设备控制指令.
 *
 * @author zhangji 2023/10/31
 */
@Getter
@Setter
public class MediaSdkDeviceControl {

    // 通道ID
    private String channelId;

    // 预置位操作
    private PresetOperation presetOperation;

    // 预置位ID
    private Integer presetIndex;

    /**
     * 云台控制命令：[方向，速度]
     * @see PtzCommand
     */
    private Map<String, Integer> ptzCommand;

    // 录像
    private Record record;

    private RecordQuery recordQuery;

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


    public boolean hasPresetCmd() {
        return presetOperation != null && presetIndex != null;
    }

    public boolean hasPtzCmd() {
        return ptzCommand != null;
    }

    public List<FunctionParameter> toFunctionInputs() {
        List<FunctionParameter> parameters = new ArrayList<>();
        parameters.add(new FunctionParameter("channelId", channelId));
        parameters.add(new FunctionParameter("presetOperation", presetOperation));
        parameters.add(new FunctionParameter("presetIndex", presetIndex));
        parameters.add(new FunctionParameter("ptzCommand", ptzCommand));
        parameters.add(new FunctionParameter("record", record));
        parameters.add(new FunctionParameter("recordQuery", recordQuery));
        parameters.add(new FunctionParameter("extraConfig", extraConfig)) ;
        return parameters;
    }

    // 预置位指令
    public enum PresetOperation {
        //设置预置位
        SET,
        //调用预置位
        CALL,
        //删除预置位
        DEL;
    }

    // 云台控制指令
    public enum PtzCommand {
        //上
        UP,
        //下
        DOWN,
        //左
        LEFT,
        //右
        RIGHT,
        //放大
        ZOOM_IN,
        //缩小
        ZOOM_OUT,
        //停止
        STOP;
    }

    public enum Record {
        START,
        STOP;
    }

    /**
     * 查询录像条件
     */
    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class RecordQuery {
        // 开始时间
        private LocalDateTime start;

        // 结束时间
        private LocalDateTime end;

        // 类型
        private String recordType;
    }

}
