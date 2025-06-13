package org.jetlinks.plugin.internal.rule;

import org.jetlinks.core.trace.FluxTracer;
import org.jetlinks.core.utils.Reactors;
import org.jetlinks.plugin.core.PluginContext;
import org.jetlinks.rule.engine.api.RuleConstants;
import org.jetlinks.rule.engine.api.RuleData;
import org.jetlinks.rule.engine.api.task.ExecutionContext;
import org.reactivestreams.Publisher;
import reactor.core.Disposable;

import java.util.Map;

public abstract class FunctionRuleEnginePlugin extends RuleEnginePlugin {
    public FunctionRuleEnginePlugin(String id, PluginContext context) {
        super(id, context);
    }

    @Override
    public final Disposable start(ExecutionContext context) {
        FluxTracer<Boolean> tracer = context()
            .monitor()
            .tracer()
            .traceFlux("/apply");
        return context
            .getInput()
            .accept(data -> data
                .dataToMap()
                .concatMap(map -> apply(data, map))
                .concatMap(val -> {
                    RuleData ruleData = context.newRuleData(data, val);
                    return context
                        .fireEvent(RuleConstants.Event.result, ruleData)
                        .then(context.getOutput().write(ruleData));
                })
                .as(tracer)
                // 捕获错误
                .onErrorResume(err -> context.onError(err, data))
                .then(Reactors.ALWAYS_TRUE));
    }

    protected abstract Publisher<Object> apply(RuleData source, Map<String, Object> data);
}
