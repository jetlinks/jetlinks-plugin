package org.jetlinks.plugin.core;


import java.io.Serializable;
import java.util.Map;

public interface PluginCommand<R> extends Serializable {

    PluginCommand<R> with(String key, Object value);

    PluginCommand<R> with(Map<String, Object> properties);

    void validate();
}
