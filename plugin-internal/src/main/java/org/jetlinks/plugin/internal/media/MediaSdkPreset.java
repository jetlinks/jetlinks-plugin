package org.jetlinks.plugin.internal.media;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hswebframework.web.bean.FastBeanCopier;

import java.util.HashMap;
import java.util.Map;

/**
 * 预置位.
 *
 * @author zhangji 2023/10/20
 */
@Getter
@Setter
@NoArgsConstructor
public class MediaSdkPreset {

    private String id;

    private String name;

    private Map<String, Object> others;

    public MediaSdkPreset(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public Map<String,Object> toMap(){
        return FastBeanCopier.copy(this, new HashMap<>());
    }
}
