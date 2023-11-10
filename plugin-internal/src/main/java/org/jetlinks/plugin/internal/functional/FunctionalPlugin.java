package org.jetlinks.plugin.internal.functional;

import io.swagger.v3.oas.annotations.media.Schema;
import org.jetlinks.core.command.Command;
import org.jetlinks.core.command.CommandHandler;
import org.jetlinks.plugin.core.AbstractPlugin;
import org.jetlinks.plugin.core.PluginContext;
import org.jetlinks.plugin.core.PluginType;
import org.jetlinks.supports.command.JavaBeanCommandSupport;

import java.util.Collections;

public abstract class FunctionalPlugin extends AbstractPlugin {

    @Override
    public abstract PluginType getType();

    public FunctionalPlugin(String id, PluginContext context) {
        super(id, context);
        registerCommand(this);
    }


    protected FunctionalService getService(String name) {
        return context()
            .services()
            .getService(FunctionalService.class,name)
            .orElseThrow(() -> new UnsupportedOperationException("Unsupported FunctionalService:" + name));
    }

    @SuppressWarnings("all")
    public void registerCommand(Object instance) {
        new JavaBeanCommandSupport(instance, method -> method.getAnnotation(Schema.class) != null)
            .getHandlers()
            .forEach(handler -> registerHandler(handler.getMetadata().getId(), (CommandHandler) handler));
    }

}
