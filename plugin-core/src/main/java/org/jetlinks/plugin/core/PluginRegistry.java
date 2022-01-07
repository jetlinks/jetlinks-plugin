package org.jetlinks.plugin.core;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface PluginRegistry {

    Flux<Description> getProviders();

    Mono<Plugin> getPlugin(String type, String pluginId);

    default Mono<Plugin> getPlugin(PluginType type, String pluginId) {
        return getPlugin(type.getId(), pluginId);
    }

    Flux<Plugin> getPlugins(String type);

    default Flux<Plugin> getPlugins(PluginType type) {
        return getPlugins(type.getId());
    }

    Flux<Plugin> getPlugins();


}
