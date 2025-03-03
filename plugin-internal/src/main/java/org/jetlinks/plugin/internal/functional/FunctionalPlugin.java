package org.jetlinks.plugin.internal.functional;

import org.jetlinks.plugin.core.AbstractPlugin;
import org.jetlinks.plugin.core.PluginContext;
import org.jetlinks.plugin.core.PluginType;

public abstract class FunctionalPlugin extends AbstractPlugin {

    @Override
    public abstract PluginType getType();

    public FunctionalPlugin(String id, PluginContext context) {
        super(id, context);
    }

    protected FunctionalService getService(String name) {
        return context()
            .services()
            .getService(FunctionalService.class,name)
            .orElseThrow(() -> new UnsupportedOperationException("Unsupported FunctionalService:" + name));
    }

}
