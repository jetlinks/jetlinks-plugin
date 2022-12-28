package org.jetlinks.plugin.core;

import lombok.AllArgsConstructor;

import java.util.function.Function;
import java.util.function.Supplier;

@AllArgsConstructor
class SimpleCommandHandler<R, C extends PluginCommand<R>> implements CommandHandler<R, C> {
    private final Supplier<Description> description;

    private final Function<C, R> executor;

    private final Supplier<C> commandBuilder;

    @Override
    public Description description() {
        return description.get();
    }

    @Override
    public R execute(C command) {
        return executor.apply(command);
    }

    @Override
    public C newCommand() {
        return commandBuilder.get();
    }
}
