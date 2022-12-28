package org.jetlinks.plugin.core;

import java.io.File;
import java.util.Map;

public interface PluginContext {

    PluginRegistry registry();

    ServiceRegistry services();

    PluginEnvironment environment();

    PluginMetrics metrics();

    PluginScheduler scheduler();

    File workDir();

    Map<String,Object> configuration();
}
