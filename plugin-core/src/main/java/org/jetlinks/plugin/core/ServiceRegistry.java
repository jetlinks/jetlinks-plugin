package org.jetlinks.plugin.core;

import reactor.core.publisher.Mono;

import java.util.List;

public interface ServiceRegistry {

    <T> Mono<T> getService(Class<T> type);

    <T> Mono<T> getService(Class<T> type, String name);

    <T> Mono<List<T>> getServices(Class<T> type);


}
