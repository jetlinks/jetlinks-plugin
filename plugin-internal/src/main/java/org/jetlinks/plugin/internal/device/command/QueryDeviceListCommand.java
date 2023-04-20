package org.jetlinks.plugin.internal.device.command;

import org.hswebframework.web.api.crud.entity.QueryParamEntity;
import org.hswebframework.web.bean.FastBeanCopier;
import org.jetlinks.core.command.AbstractCommand;
import org.jetlinks.core.command.CommandUtils;
import org.jetlinks.core.metadata.FunctionMetadata;
import org.jetlinks.core.metadata.PropertyMetadata;
import org.jetlinks.core.metadata.SimpleFunctionMetadata;
import org.jetlinks.plugin.internal.device.Device;
import reactor.core.publisher.Flux;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * 查询设备列表命令
 *
 * @author zhouhao
 * @since 1.0
 */
public class QueryDeviceListCommand extends AbstractCommand<Flux<Device>, QueryDeviceListCommand> {

    public String productId() {
        return (String) readable().get("productId");
    }

    //转换为通用查询条件
    public QueryParamEntity toQueryParam() {
        return FastBeanCopier.copy(readable(), new QueryParamEntity());
    }

    public static FunctionMetadata metadata(PropertyMetadata... terms) {
        return metadata(Arrays.asList(terms));
    }

    public static FunctionMetadata metadata(List<PropertyMetadata> terms) {
        SimpleFunctionMetadata metadata = new SimpleFunctionMetadata();
        metadata.setId(CommandUtils.getCommandIdByType(QueryDeviceListCommand.class));

        metadata.setName("查询设备列表");

        metadata.setExpands(Collections.singletonMap("terms", terms));

        return metadata;
    }

}
