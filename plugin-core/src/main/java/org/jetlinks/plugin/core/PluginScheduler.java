package org.jetlinks.plugin.core;

import reactor.core.Disposable;
import reactor.core.publisher.Mono;

import java.time.Duration;

public interface PluginScheduler {

    default Disposable interval(Mono<Void> job, Duration interval) {
        return interval(job, interval, true);
    }

    Disposable interval(Mono<Void> job, Duration interval, boolean singleton);

    Disposable delay(Mono<Void> job, Duration interval);

}
