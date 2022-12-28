package org.jetlinks.plugin.internal.things;

import org.jetlinks.core.message.ThingMessage;
import org.jetlinks.core.things.ThingAssociatedData;
import org.jetlinks.core.things.ThingInstance;
import reactor.core.publisher.Mono;

public interface PluginThingsManagerService {


    Mono<Void> handleMessage(ThingsManagerPlugin plugin,
                             ThingMessage message);

    Mono<Void> handleInstance(ThingsManagerPlugin plugin,
                              ThingInstance info);

    Mono<Void> handleData(ThingsManagerPlugin plugin,
                          ThingAssociatedData data);
}
