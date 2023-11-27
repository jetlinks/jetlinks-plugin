package org.jetlinks.plugin.internal.rule;

import org.jetlinks.plugin.core.AbstractPlugin;
import org.jetlinks.plugin.core.PluginContext;
import org.jetlinks.plugin.core.PluginType;
import org.jetlinks.plugin.internal.InternalPluginType;
import org.jetlinks.rule.engine.api.task.ExecutionContext;
import reactor.core.Disposable;

public abstract class RuleEnginePlugin extends AbstractPlugin {

    public RuleEnginePlugin(String id, PluginContext context) {
        super(id, context);
    }

    @Override
    public PluginType getType() {
        return InternalPluginType.ruleEngine;
    }

    public abstract Disposable start(ExecutionContext context);


}
