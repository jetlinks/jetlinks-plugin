package org.jetlinks.plugin.internal;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetlinks.core.monitor.logger.Logger;
import org.jetlinks.core.monitor.logger.Slf4jLogger;
import org.jetlinks.core.monitor.metrics.Metrics;
import org.jetlinks.core.monitor.tracer.Tracer;
import org.jetlinks.core.utils.ConverterUtils;
import org.jetlinks.plugin.core.*;
import reactor.core.Disposable;
import reactor.core.Disposables;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.File;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class StaticPluginContext implements ServiceRegistry, PluginContext, PluginRegistry, PluginScheduler, PluginEnvironment {
    private final Map<String, Object> services = new HashMap<>();
    private final Map<String, Object> configs = new HashMap<>();

    public StaticPluginContext withService(Object service) {
        services.put(service.getClass().getSimpleName(), service);
        return this;
    }

    public StaticPluginContext withService(String name, Object service) {
        services.put(name, service);
        return this;
    }

    public StaticPluginContext withConfig(String key, Object value) {
        configs.put(key, value);
        return this;
    }

    @Override
    public PluginRegistry registry() {
        return this;
    }

    @Override
    public ServiceRegistry services() {
        return this;
    }

    @Override
    public PluginEnvironment environment() {
        return this;
    }


    @Override
    public PluginMetrics metrics() {
        return new PluginMetrics() {
            @Override
            public Logger logger() {
                return new Slf4jLogger(log);
            }

            @Override
            public Tracer tracer() {
                return Tracer.noop();
            }


            @Override
            public Metrics metrics() {
                return Metrics.noop();
            }
        };
    }

    @Override
    public PluginScheduler scheduler() {
        return this;
    }

    @Override
    public File workDir() {
        return new File("target");
    }

    @Override
    public Mono<Plugin> getPlugin(String type, String pluginId) {
        return Mono.empty();
    }

    @Override
    public Flux<Plugin> getPlugins(String type) {
        return Flux.empty();
    }

    @Override
    public Flux<Plugin> getPlugins() {
        return Flux.empty();
    }

    @Override
    public Disposable interval(String name, Mono<Void> job, String cronExpression, boolean singleton) {
        return Disposables.disposed();
    }

    @Override
    public Disposable interval(String name, Mono<Void> job, Duration interval, boolean singleton) {
        return Flux.interval(interval)
                   .onBackpressureDrop()
                   .concatMap(ignore -> job)
                   .subscribe();
    }

    @Override
    public Disposable delay(Mono<Void> job, Duration interval) {
        return Mono
            .delay(interval)
            .then(job)
            .subscribe();
    }

    @Override
    public Optional<String> getProperty(String key) {
        return Optional.ofNullable(configs.get(key))
                       .map(String::valueOf);
    }

    @Override
    public <T> Optional<T> getProperty(String key, Class<T> type) {
        return Optional.ofNullable(configs.get(key))
                       .map(e -> ConverterUtils.convert(e, type));
    }

    @Override
    public Map<String, Object> getProperties() {
        return configs;
    }

    @Override
    public <T> Optional<T> getService(Class<T> type) {
        return services
            .values()
            .stream()
            .filter(type::isInstance)
            .findFirst()
            .map(type::cast);
    }

    @Override
    public <T> Optional<T> getService(Class<T> type, String name) {
        return Optional.ofNullable(services.get(name))
                       .map(type::cast);
    }

    @Override
    public <T> List<T> getServices(Class<T> type) {
        return services
            .values()
            .stream()
            .filter(type::isInstance)
            .map(type::cast)
            .collect(Collectors.toList());
    }
}
