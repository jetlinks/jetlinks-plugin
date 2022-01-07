package org.jetlinks.plugin.core;

import java.time.Duration;

public interface Metrics {

    void count(String operation, int inc);

    void value(String operation, double value);

    void error(String operation, Throwable error);

}
