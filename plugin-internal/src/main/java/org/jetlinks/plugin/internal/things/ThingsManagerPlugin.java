package org.jetlinks.plugin.internal.things;

import org.jetlinks.core.message.ThingMessage;
import org.jetlinks.core.things.ThingAssociatedData;
import org.jetlinks.core.things.ThingInstance;
import org.jetlinks.core.things.ThingsRegistry;
import org.jetlinks.plugin.core.AbstractPlugin;
import org.jetlinks.plugin.core.PluginContext;
import org.jetlinks.plugin.core.PluginType;
import org.jetlinks.plugin.internal.InternalPluginType;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public abstract class ThingsManagerPlugin extends AbstractPlugin {

    private final PluginThingsManagerService managerService;

    protected final ThingsRegistry registry;

    public ThingsManagerPlugin(String id, PluginContext context) {
        super(id, context);

        managerService = context.services().getServiceNow(PluginThingsManagerService.class);

        registry = context.services().getServiceNow(ThingsRegistry.class);

    }

    public abstract Flux<ThingMessage> execute(ThingMessage message);

    protected final Mono<Void> handleMessage(ThingMessage message) {
        return managerService.handleMessage(this, message);
    }

    protected final Mono<Void> handleThingInstance(ThingInstance instance) {
        return managerService.handleInstance(this, instance);
    }

    protected final Mono<Void> handleThingData(ThingAssociatedData data) {
        return managerService.handleData(this, data);
    }

    @Override
    public final PluginType getType() {
        return InternalPluginType.thingsManager;
    }
}
