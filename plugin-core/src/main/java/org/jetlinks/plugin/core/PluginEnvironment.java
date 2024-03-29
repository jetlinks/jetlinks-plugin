package org.jetlinks.plugin.core;

import java.util.Map;
import java.util.Optional;

public interface PluginEnvironment {

    Optional<String> getProperty(String key);

    <T> Optional<T> getProperty(String key, Class<T> type);

    Map<String,Object> getProperties();


}
