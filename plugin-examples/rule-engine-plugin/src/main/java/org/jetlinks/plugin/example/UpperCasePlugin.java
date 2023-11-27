package org.jetlinks.plugin.example;

import com.google.common.collect.Maps;
import org.jetlinks.plugin.core.PluginContext;
import org.jetlinks.plugin.internal.rule.FunctionRuleEnginePlugin;
import org.jetlinks.rule.engine.api.RuleData;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;

import java.util.Map;

public class UpperCasePlugin extends FunctionRuleEnginePlugin {
    private final String prefix;
    private final String suffix;

    public UpperCasePlugin(String id, PluginContext context) {
        super(id, context);
        prefix = context
            .environment()
            .getProperty("prefix")
            .orElse("");

        suffix = context
            .environment()
            .getProperty("suffix")
            .orElse("");
    }

    @Override
    protected Publisher<Object> apply(RuleData source, Map<String, Object> data) {
        context().monitor()
                 .logger()
                 .debug("处理数据:{}", data);
        return Mono.just(
            Maps.transformEntries(data, (k, v) -> prefix + String
                .valueOf(v)
                .toUpperCase() + suffix)
        );
    }


}
