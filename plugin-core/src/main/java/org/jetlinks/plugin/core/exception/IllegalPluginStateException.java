package org.jetlinks.plugin.core.exception;

import lombok.Getter;
import org.jetlinks.plugin.core.Plugin;
import org.jetlinks.plugin.core.PluginState;

@Getter
public class IllegalPluginStateException extends PluginException {

    private final PluginState state;

    public IllegalPluginStateException(Plugin plugin) {
        this(plugin, plugin.getState());
    }

    public IllegalPluginStateException(Plugin plugin, PluginState state) {
        super(plugin, null);
        this.state = state;
    }

    public IllegalPluginStateException(Plugin plugin, String message) {
        this(plugin, plugin.getState(), message);
    }

    public IllegalPluginStateException(Plugin plugin, PluginState state, String message) {
        super(plugin, message, null);
        this.state = state;
    }
}
