package org.jetlinks.plugin.internal.media;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 通道信息.
 *
 * @author zhangji 2023/10/12
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MediaSdkChannel {

    @Schema(description = "通道ID")
    private String id;

    @Schema(description = "名称")
    private String name;

    @Schema(description = "设备ID")
    private String deviceId;

    @Schema(description = "厂商")
    private String manufacturer;

    @Schema(description = "型号")
    private String model;

}
