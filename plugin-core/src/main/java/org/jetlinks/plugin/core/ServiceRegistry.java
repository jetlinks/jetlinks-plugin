package org.jetlinks.plugin.core;


import java.util.List;
import java.util.Optional;

public interface ServiceRegistry {

    <T> Optional<T> getService(Class<T> type);

    <T> Optional<T> getService(Class<T> type, String name);

    <T> List<T> getServices(Class<T> type);


}
