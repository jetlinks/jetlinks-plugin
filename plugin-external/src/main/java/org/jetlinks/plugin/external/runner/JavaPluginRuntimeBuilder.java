package org.jetlinks.plugin.external.runner;

import org.jetlinks.plugin.core.PluginContext;
import org.jetlinks.plugin.core.PluginDriver;
import org.jetlinks.plugin.internal.StaticPluginContext;

import java.util.Objects;

/** Fluent builder for an embedded Java runner or the fixed command-line runner. */
public final class JavaPluginRuntimeBuilder {
    private PluginDriver driver;
    private JavaPluginDriverSource driverSource;
    private JavaPluginRuntimeConfiguration configuration = JavaPluginRuntimeConfiguration.builder().build();
    private PluginContext context = new StaticPluginContext();

    JavaPluginRuntimeBuilder() {
    }

    public JavaPluginRuntimeBuilder driver(PluginDriver driver) {
        if (driverSource != null) {
            throw new IllegalStateException("driverSource has already been configured");
        }
        this.driver = Objects.requireNonNull(driver, "driver");
        return this;
    }

    public JavaPluginRuntimeBuilder driverSource(JavaPluginDriverSource source) {
        if (driver != null) {
            throw new IllegalStateException("driver has already been configured");
        }
        this.driverSource = Objects.requireNonNull(source, "source");
        return this;
    }

    public JavaPluginRuntimeBuilder configuration(JavaPluginRuntimeConfiguration configuration) {
        this.configuration = Objects.requireNonNull(configuration, "configuration");
        return this;
    }

    public JavaPluginRuntimeBuilder context(PluginContext context) {
        this.context = Objects.requireNonNull(context, "context");
        return this;
    }

    public JavaPluginRuntime build() {
        if (driver == null && driverSource == null) {
            throw new IllegalStateException("driver or driverSource is required");
        }
        JavaPluginDriverSource source = driverSource == null
            ? new ClasspathPluginDriverSource(driver)
            : driverSource;
        return new JavaPluginRuntime(source, configuration, context);
    }
}
