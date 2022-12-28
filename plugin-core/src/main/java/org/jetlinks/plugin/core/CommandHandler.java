package org.jetlinks.plugin.core;

public interface CommandHandler<R,C extends PluginCommand<R>> {

    Description description();

    R execute(C command);

    C newCommand();
}
