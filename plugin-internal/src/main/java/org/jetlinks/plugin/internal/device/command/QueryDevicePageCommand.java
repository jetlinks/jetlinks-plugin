package org.jetlinks.plugin.internal.device.command;

import org.hswebframework.web.api.crud.entity.QueryParamEntity;
import org.hswebframework.web.bean.FastBeanCopier;
import org.jetlinks.core.command.AbstractCommand;
import org.jetlinks.core.command.CommandUtils;
import org.jetlinks.core.metadata.FunctionMetadata;
import org.jetlinks.core.metadata.PropertyMetadata;
import org.jetlinks.core.metadata.SimpleFunctionMetadata;
import org.jetlinks.plugin.internal.device.Device;
import org.hswebframework.web.api.crud.entity.PagerResult;
import org.jetlinks.sdk.server.commons.cmd.QueryPagerCommand;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * 分页查询设备命令
 *
 * @author zhouhao
 * @since 1.0
 */
public class QueryDevicePageCommand extends QueryPagerCommand<Device> {

    public String productId() {
        return (String) readable().get("productId");
    }

    //转换为通用查询条件
    public QueryParamEntity toQueryParam() {
        return asQueryParam();
    }

    public static FunctionMetadata metadata(PropertyMetadata... terms) {
        return metadata(Arrays.asList(terms));
    }

    public static FunctionMetadata metadata(List<PropertyMetadata> terms) {
        SimpleFunctionMetadata metadata = new SimpleFunctionMetadata();
        metadata.setId(CommandUtils.getCommandIdByType(QueryDevicePageCommand.class));

        metadata.setName("分页查询设备列表");

        metadata.setExpands(Collections.singletonMap("terms", terms));

        //todo 其他属性设置?

        return metadata;
    }

}
