package org.jetlinks.plugin.core;

import java.util.function.Function;
import java.util.function.Supplier;

public interface CommandHandler<R, C extends PluginCommand<R>> {

    Description description();

    R execute(C command);

    C newCommand();


    static <R, C extends PluginCommand<R>> CommandHandler<R, C> of(Description description,
                                                                   Function<C, R> executor,
                                                                   Supplier<C> commandBuilder) {
        return new SimpleCommandHandler<>(() -> description, executor, commandBuilder);
    }

    static <R, C extends PluginCommand<R>> CommandHandler<R, C> of(Supplier<Description> description,
                                                                   Function<C, R> executor,
                                                                   Supplier<C> commandBuilder) {
        return new SimpleCommandHandler<>(description, executor, commandBuilder);
    }
}
