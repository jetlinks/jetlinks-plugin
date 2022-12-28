package org.jetlinks.plugin.core;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public abstract class AbstractPluginCommand<R> implements PluginCommand<R>, Serializable {
    private static final long serialVersionUID = 1L;

    private Map<String, Object> properties;

    @Override
    public String getId() {
        String name = getClass().getSimpleName();
        if (name.endsWith("Command")) {
            return name.substring(0, name.length() - 7);
        }
        return name;
    }

    @Override
    public final PluginCommand<R> with(String key, Object value) {
        writable().put(key, value);
        return this;
    }

    @Override
    public final PluginCommand<R> with(Map<String, Object> properties) {
        writable().putAll(properties);
        return this;
    }

    protected Map<String, Object> readable() {
        return properties == null ? Collections.emptyMap() : properties;
    }

    protected Map<String, Object> writable() {
        return properties == null ? properties = new HashMap<>() : properties;
    }

    @Override
    public abstract void validate();
}
