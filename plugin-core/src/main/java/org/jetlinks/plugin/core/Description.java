package org.jetlinks.plugin.core;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Map;

@Getter
@Setter
@AllArgsConstructor(staticName = "of")
@NoArgsConstructor
public class Description implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 驱动内部ID
     */
    private String id;

    /**
     * 驱动名称
     */
    private String name;

    /**
     * 驱动描述
     */
    private String description;

    /**
     * 版本号
     */
    private Version version;

    /**
     * 支持的平台版本范围
     */
    private VersionRange platformVersion;

    /**
     * 其他信息
     */
    private Map<String, Object> others;


}
