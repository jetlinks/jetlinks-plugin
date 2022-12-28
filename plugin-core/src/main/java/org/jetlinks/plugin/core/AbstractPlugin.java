package org.jetlinks.plugin.core;

import lombok.extern.slf4j.Slf4j;
import org.jetlinks.plugin.core.exception.PluginException;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

@Slf4j
public abstract class AbstractPlugin implements Plugin {
    public static final AtomicReferenceFieldUpdater<AbstractPlugin, PluginState>
            STATE = AtomicReferenceFieldUpdater.newUpdater(AbstractPlugin.class, PluginState.class, "state");

    private final String id;

    private final PluginContext context;

    private volatile PluginState state = PluginState.stopped;

    private final Map<String, CommandHandler<?, PluginCommand<?>>> handlers = new ConcurrentHashMap<>();

    private final Map<String, Supplier<Flux<ByteBuffer>>> resources = new ConcurrentHashMap<>();

    private final List<BiConsumer<PluginState, PluginState>> stateListener = new CopyOnWriteArrayList<>();

    public AbstractPlugin(String id, PluginContext context) {
        this.id = id;
        this.context = context;
    }

    protected void registerResource(String name, Supplier<Flux<ByteBuffer>> supplier) {
        resources.put(name, supplier);
    }

    @SuppressWarnings("all")
    protected <R,C extends PluginCommand<R>> void registerCommand(String id, CommandHandler<R, C> commandHandler) {
        handlers.put(id, (CommandHandler)commandHandler);
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

    private Mono<Void> doStart() {
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
    public Mono<Void> shutdown() {
        changeState(PluginState.stopped);
        return doShutdown();
    }

    protected Mono<Void> doShutdown() {
        return Mono.empty();
    }

    @Override
    @SuppressWarnings("all")
    public final <R> R execute(PluginCommand<R> command) {
        CommandHandler<?, PluginCommand<?>> handler = handlers.get(command.getId());

        if (handler == null) {
            return executeUnsupportedCommand(command);
        }
        try {
            return (R) handler.execute(command);
        } catch (Throwable error) {
            throw new PluginException(this, error);
        }

    }

    private <R> R executeUnsupportedCommand(PluginCommand<R> command) {
        throw unsupportedCommandError(command.getId());
    }

    @Override
    @SuppressWarnings("all")
    public final <R> Mono<PluginCommand<R>> createCommand(String commandId) {
        CommandHandler<?, PluginCommand<?>> handler = handlers.get(commandId);
        if (handler == null) {
            return Mono.error(unsupportedCommandError(commandId));
        }
        return Mono.just((PluginCommand) handler.newCommand());
    }

    protected PluginException unsupportedCommandError(String commandId) {
        return new PluginException(this, new UnsupportedOperationException("unsupported command:" + commandId));
    }

    @Override
    public Flux<Description> getSupportCommands() {
        return Flux
                .fromIterable(handlers.values())
                .mapNotNull(CommandHandler::description);
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

    @Override
    public final Flux<ByteBuffer> getResource(String name) {
        return resources.getOrDefault(name, Flux::empty).get();
    }
}
