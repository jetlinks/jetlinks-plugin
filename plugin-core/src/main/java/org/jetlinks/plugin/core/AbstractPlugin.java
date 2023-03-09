package org.jetlinks.plugin.core;

import lombok.extern.slf4j.Slf4j;
import org.jetlinks.core.command.AbstractCommandSupport;
import reactor.core.Disposable;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import java.util.function.BiConsumer;

@Slf4j
public abstract class AbstractPlugin extends AbstractCommandSupport implements Plugin {
    public static final AtomicReferenceFieldUpdater<AbstractPlugin, PluginState>
            STATE = AtomicReferenceFieldUpdater.newUpdater(AbstractPlugin.class, PluginState.class, "state");

    private final String id;

    private final PluginContext context;

    private volatile PluginState state = PluginState.stopped;

    private final List<BiConsumer<PluginState, PluginState>> stateListener = new CopyOnWriteArrayList<>();

    public AbstractPlugin(String id, PluginContext context) {
        this.id = id;
        this.context = context;
    }

    protected PluginContext context() {
        return context;
    }

    @Override
    public final String getId() {
        return id;
    }

    @Override
    public abstract PluginType getType();

    @Override
    public final PluginState getState() {
        return state;
    }

    @Override
    public final Mono<Void> start() {
        if (state == PluginState.starting || state == PluginState.running) {
            return Mono.empty();
        }
        changeState(PluginState.starting);
        return doStart()
                .doOnSuccess((ignore) -> changeState(PluginState.running))
                .doOnError(err -> changeState(PluginState.stopped));
    }

    protected Mono<Void> doStart() {
        return Mono.empty();
    }

    @Override
    public final Mono<Void> pause() {
        changeState(PluginState.paused);
        return doPause();
    }

    protected Mono<Void> doPause() {
        return Mono.empty();
    }

    @Override
    public final Mono<Void> shutdown() {
        changeState(PluginState.stopped);
        return doShutdown();
    }

    protected Mono<Void> doShutdown() {
        return Mono.empty();
    }


    protected PluginState changeState(PluginState state) {
        PluginState old = STATE.getAndSet(this, state);
        if (old != state) {
            fireStateChange(old, state);
        }
        return old;
    }


    @Override
    public final Disposable doOnSateChanged(BiConsumer<PluginState, PluginState> listener) {
        stateListener.add(listener);
        return () -> stateListener.remove(listener);
    }

    protected void fireStateChange(PluginState before, PluginState after) {
        log.debug("{} plugin [{}] state change from {} to {} ", getType().getId(), getId(), before, after);
        for (BiConsumer<PluginState, PluginState> listener : stateListener) {
            try {
                listener.accept(before, after);
            } catch (Throwable error) {
                log.warn("fire {} plugin [{}] state listener error", getType().getId(), getId(), error);
            }
        }
    }

}
