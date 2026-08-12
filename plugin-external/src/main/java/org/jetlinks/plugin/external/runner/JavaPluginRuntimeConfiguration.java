package org.jetlinks.plugin.external.runner;

import org.jetlinks.plugin.protocol.ExternalPluginProtocol;

import java.time.Duration;
import java.nio.file.Path;
import java.util.Objects;

/** Immutable, bounded configuration for one runner generation. */
public final class JavaPluginRuntimeConfiguration {
    private final String host;
    private final int port;
    private final Path unixSocket;
    private final String runtimeId;
    private final String driverId;
    private final long generation;
    private final String credential;
    private final int maxFrameBytes;
    private final long maxResourceBytes;
    private final Duration drainTimeout;

    private JavaPluginRuntimeConfiguration(Builder builder) {
        this.host = builder.host;
        this.port = builder.port;
        this.unixSocket = builder.unixSocket;
        this.runtimeId = builder.runtimeId;
        this.driverId = builder.driverId;
        this.generation = builder.generation;
        this.credential = builder.credential;
        this.maxFrameBytes = builder.maxFrameBytes;
        this.maxResourceBytes = builder.maxResourceBytes;
        this.drainTimeout = builder.drainTimeout;
    }

    public static Builder builder() {
        return new Builder();
    }

    public String host() {
        return host;
    }

    public int port() {
        return port;
    }

    public Path unixSocket() {
        return unixSocket;
    }

    public String runtimeId() {
        return runtimeId;
    }

    public String driverId() {
        return driverId;
    }

    public long generation() {
        return generation;
    }

    public String credential() {
        return credential;
    }

    public int maxFrameBytes() {
        return maxFrameBytes;
    }

    public long maxResourceBytes() {
        return maxResourceBytes;
    }

    public Duration drainTimeout() {
        return drainTimeout;
    }

    public static final class Builder {
        private String host = "127.0.0.1";
        private int port = 0;
        private Path unixSocket;
        private String runtimeId = "runtime";
        private String driverId = "driver";
        private long generation;
        private String credential;
        private int maxFrameBytes = ExternalPluginProtocol.DEFAULT_MAX_FRAME_BYTES;
        private long maxResourceBytes = 16L * 1024 * 1024;
        private Duration drainTimeout = Duration.ofSeconds(10);

        public Builder host(String host) {
            this.host = Objects.requireNonNull(host, "host");
            return this;
        }

        public Builder port(int port) {
            this.port = port;
            return this;
        }

        public Builder unixSocket(Path unixSocket) {
            this.unixSocket = unixSocket == null ? null : unixSocket.toAbsolutePath().normalize();
            return this;
        }

        public Builder identity(String runtimeId, String driverId, long generation) {
            this.runtimeId = Objects.requireNonNull(runtimeId, "runtimeId");
            this.driverId = Objects.requireNonNull(driverId, "driverId");
            this.generation = generation;
            return this;
        }

        public Builder credential(String credential) {
            this.credential = credential;
            return this;
        }

        public Builder maxFrameBytes(int maxFrameBytes) {
            this.maxFrameBytes = maxFrameBytes;
            return this;
        }

        public Builder maxResourceBytes(long maxResourceBytes) {
            this.maxResourceBytes = maxResourceBytes;
            return this;
        }

        public Builder drainTimeout(Duration drainTimeout) {
            this.drainTimeout = Objects.requireNonNull(drainTimeout, "drainTimeout");
            return this;
        }

        public JavaPluginRuntimeConfiguration build() {
            if (port < 0 || port > 65535 || (unixSocket != null && unixSocket.toString().isEmpty())) {
                throw new IllegalArgumentException("port must be between 0 and 65535");
            }
            if (unixSocket != null && port != 0) {
                throw new IllegalArgumentException("unixSocket and port are mutually exclusive");
            }
            if (generation < 0 || maxFrameBytes < 1024 || maxResourceBytes <= 0 || drainTimeout.isNegative()) {
                throw new IllegalArgumentException("runtime limits are invalid");
            }
            return new JavaPluginRuntimeConfiguration(this);
        }
    }
}
