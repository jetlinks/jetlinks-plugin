package org.jetlinks.plugin.core;


import java.util.List;
import java.util.Optional;

public interface ServiceRegistry {

    default <T> T getServiceNow(Class<T> type) {
        return getService(type)
                .orElseThrow(() -> new UnsupportedOperationException("unsupported service:" + type.getSimpleName()));
    }


    <T> Optional<T> getService(Class<T> type);

    <T> Optional<T> getService(Class<T> type, String name);

    <T> List<T> getServices(Class<T> type);


}
