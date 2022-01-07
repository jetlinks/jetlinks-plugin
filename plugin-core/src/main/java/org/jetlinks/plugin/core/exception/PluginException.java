package org.jetlinks.plugin.core.exception;

import lombok.Getter;
import org.jetlinks.plugin.core.Plugin;

@Getter
public class PluginException extends RuntimeException {

    private final Plugin plugin;

    public PluginException(Plugin plugin, Throwable cause) {
        super(cause);
        this.plugin = plugin;
    }

    public PluginException(Plugin plugin, String message, Throwable cause) {
        super(message, cause);
        this.plugin = plugin;
    }

}
