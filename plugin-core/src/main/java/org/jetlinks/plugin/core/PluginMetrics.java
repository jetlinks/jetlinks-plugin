package org.jetlinks.plugin.core;

public interface PluginMetrics {

    void count(String operation, int inc);

    void value(String operation, double value);

    void error(String operation, Throwable error);

}
