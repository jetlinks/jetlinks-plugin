package org.jetlinks.plugin.core;

public interface PluginListener {

    void onStart(Plugin plugin);

    void onDestroy(Plugin plugin);

}
