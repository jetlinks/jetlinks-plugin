package org.jetlinks.plugin.core;

public interface PluginContext {

    PluginRegistry registry();

    ServiceRegistry services();

    PluginEnvironment environment();

    Metrics metrics();

}
