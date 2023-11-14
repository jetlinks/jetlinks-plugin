package org.jetlinks.plugin.internal.media;

import lombok.Getter;
import lombok.Setter;
import org.hswebframework.web.bean.FastBeanCopier;

import java.util.HashMap;
import java.util.Map;

/**
 * 视频录像.
 *
 * @author zhangji 2023/11/2
 */
@Getter
@Setter
public class MediaSdkRecord {
    private String name;

    private String deviceId;

    private String channelId;

    private String streamId;

    private Long startTime;

    private Long endTime;

    private String filePath;

    private String type;

    private Long fileSize;

    private String recorderId;

    private String secrecy;

    private Map<String,Object> others;

    public Map<String,Object> toMap(){
        return FastBeanCopier.copy(this, new HashMap<>());
    }
}
