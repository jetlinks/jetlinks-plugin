package org.jetlinks.plugin.core;

import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ServiceRegistryTest {

    @Test
    void shouldGetNamedServiceNow() {
        Object expected = new Object();
        ServiceRegistry registry = registry("deviceService:device", expected);

        assertSame(expected, registry.getServiceNow(Object.class, "deviceService:device"));
        assertThrows(UnsupportedOperationException.class,
                     () -> registry.getServiceNow(Object.class, "deviceService:gateway"));
    }

    private static ServiceRegistry registry(String name, Object service) {
        return new ServiceRegistry() {
            @Override
            public <T> Optional<T> getService(Class<T> type) {
                return Optional.empty();
            }

            @Override
            public <T> Optional<T> getService(Class<T> type, String serviceName) {
                if (!name.equals(serviceName) || !type.isInstance(service)) {
                    return Optional.empty();
                }
                return Optional.of(type.cast(service));
            }

            @Override
            public <T> List<T> getServices(Class<T> type) {
                return Collections.emptyList();
            }
        };
    }
}
