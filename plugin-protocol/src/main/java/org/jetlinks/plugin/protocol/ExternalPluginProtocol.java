package org.jetlinks.plugin.protocol;

/**
 * Stable identifiers shared by all external plugin implementations.
 *
 * <p>This class is deliberately independent from Spring, RSocket and the platform runtime. The
 * same values are consumed by the Java and TypeScript SDKs and are therefore part of the wire
 * contract.</p>
 */
public final class ExternalPluginProtocol {

    public static final String VERSION = "1.0";
    public static final String DATA_MIME_TYPE = "application/json";
    public static final String METADATA_MIME_TYPE = "text/plain";
    public static final int DEFAULT_MAX_FRAME_BYTES = 1024 * 1024;
    public static final int MAX_METADATA_ENTRIES = 32;
    public static final int MAX_METADATA_VALUE_BYTES = 4096;

    public static final String ROUTE_SETUP = "plugin.runtime.setup";
    public static final String ROUTE_DESCRIBE = "plugin.driver.describe";
    public static final String ROUTE_CREATE = "plugin.driver.create";
    public static final String ROUTE_PLUGIN_START = "plugin.lifecycle.start";
    public static final String ROUTE_PLUGIN_PAUSE = "plugin.lifecycle.pause";
    public static final String ROUTE_PLUGIN_SHUTDOWN = "plugin.lifecycle.shutdown";
    public static final String ROUTE_PLUGIN_COMMAND = "plugin.command.execute";
    public static final String ROUTE_DRIVER_COMMAND = "driver.command.execute";
    public static final String ROUTE_DRIVER_RESOURCE = "driver.resource.get";
    public static final String ROUTE_RUNTIME_HEALTH = "runtime.health";
    public static final String ROUTE_RUNTIME_DRAIN = "runtime.drain";
    /** A platform command invoked by the external plugin through the bidirectional channel. */
    public static final String ROUTE_HOST_COMMAND = "host.command.execute";
    /** A bounded monitor event emitted by the external plugin to the platform. */
    public static final String ROUTE_HOST_MONITOR = "host.monitor.event";

    private ExternalPluginProtocol() {
    }
}
