package org.jetlinks.plugin.external;

import org.jetlinks.core.monitor.Monitor;
import org.jetlinks.plugin.core.PluginContext;
import org.jetlinks.plugin.core.PluginEnvironment;
import org.jetlinks.plugin.core.PluginMetrics;
import org.jetlinks.plugin.core.PluginRegistry;
import org.jetlinks.plugin.core.PluginScheduler;
import org.jetlinks.plugin.core.ServiceRegistry;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Plugin context wrapper that adds the typed host client as a named service. Existing local
 * services remain delegated to the runner context; no platform implementation type crosses the
 * process boundary.
 */
public final class ExternalPluginContext implements PluginContext {
    public static final String HOST_SERVICE_NAME = "external.host";

    private final PluginContext delegate;
    private final ExternalPluginHostClient host;
    private final ServiceRegistry services;

    private ExternalPluginContext(PluginContext delegate, ExternalPluginHostClient host) {
        this.delegate = delegate;
        this.host = host;
        this.services = new HostServiceRegistry(delegate.services(), host);
    }

    public static PluginContext withHost(PluginContext delegate, ExternalPluginHostClient host) {
        return new ExternalPluginContext(delegate, host);
    }

    public ExternalPluginHostClient host() {
        return host;
    }

    @Override public PluginRegistry registry() { return delegate.registry(); }
    @Override public ServiceRegistry services() { return services; }
    @Override public PluginEnvironment environment() { return delegate.environment(); }
    @Override public PluginMetrics metrics() { return delegate.metrics(); }
    @Override public Monitor monitor() { return delegate.monitor(); }
    @Override public PluginScheduler scheduler() { return delegate.scheduler(); }
    @Override public File workDir() { return delegate.workDir(); }

    private static final class HostServiceRegistry implements ServiceRegistry {
        private final ServiceRegistry delegate;
        private final ExternalPluginHostClient host;

        private HostServiceRegistry(ServiceRegistry delegate, ExternalPluginHostClient host) {
            this.delegate = delegate;
            this.host = host;
        }

        @Override
        public <T> Optional<T> getService(Class<T> type) {
            if (type.isInstance(host)) return Optional.of(type.cast(host));
            return delegate.getService(type);
        }

        @Override
        public <T> Optional<T> getService(Class<T> type, String name) {
            if (HOST_SERVICE_NAME.equals(name) && type.isInstance(host)) return Optional.of(type.cast(host));
            return delegate.getService(type, name);
        }

        @Override
        public <T> List<T> getServices(Class<T> type) {
            List<T> result = new ArrayList<>();
            if (type.isInstance(host)) result.add(type.cast(host));
            result.addAll(delegate.getServices(type));
            return result;
        }
    }
}
