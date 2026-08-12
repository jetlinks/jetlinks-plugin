package org.jetlinks.plugin.external.runner;

import java.nio.file.Path;
import java.time.Duration;

/** Fixed command-line entry point used by process and container runtime providers. */
public final class JavaPluginRunner {
    private JavaPluginRunner() {
    }

    public static void main(String[] args) {
        Arguments options = Arguments.parse(args);
        JavaPluginRuntime runtime = JavaPluginRuntime.builder()
            .driverSource(new JarPluginDriverSource(options.plugin))
            .configuration(JavaPluginRuntimeConfiguration.builder()
                              .host(options.host)
                              .port(options.port)
                              .unixSocket(options.unixSocket)
                              .identity(options.runtimeId, options.driverId, options.generation)
                              .credential(options.credential)
                              .build())
            .build();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> runtime.close()
            .block(Duration.ofSeconds(10)), "jetlinks-plugin-runner-shutdown"));
        runtime.start().block(Duration.ofSeconds(30));
        runtime.onClose().block();
    }

    static final class Arguments {
        private Path plugin;
        private String host = "0.0.0.0";
        private int port = 7000;
        private Path unixSocket;
        private String runtimeId = "runtime";
        private String driverId = "driver";
        private long generation;
        private String credential;

        static Arguments parse(String[] args) {
            Arguments result = new Arguments();
            for (int i = 0; i < args.length; i++) {
                String option = args[i];
                String value = i + 1 < args.length ? args[++i] : null;
                switch (option) {
                    case "--plugin":
                        result.plugin = Path.of(require(option, value));
                        break;
                    case "--host":
                        result.host = require(option, value);
                        break;
                    case "--port":
                        result.port = Integer.parseInt(require(option, value));
                        break;
                    case "--unix-socket":
                        result.unixSocket = Path.of(require(option, value));
                        break;
                    case "--runtime-id":
                        result.runtimeId = require(option, value);
                        break;
                    case "--driver-id":
                        result.driverId = require(option, value);
                        break;
                    case "--generation":
                        result.generation = Long.parseLong(require(option, value));
                        break;
                    case "--credential-file":
                        result.credential = readCredentialFile(Path.of(require(option, value)));
                        break;
                    case "--credential":
                        throw new IllegalArgumentException("--credential is not supported; use --credential-file");
                    default:
                        throw new IllegalArgumentException("unsupported option: " + option);
                }
            }
            if (result.plugin == null) {
                throw new IllegalArgumentException("--plugin is required");
            }
            return result;
        }

        private static String readCredentialFile(Path path) {
            try {
                return CredentialFileReader.read(path);
            } catch (Exception error) {
                throw new IllegalArgumentException("unable to read credential file", error);
            }
        }

        String credential() {
            return credential;
        }

        private static String require(String option, String value) {
            if (value == null || value.trim().isEmpty()) {
                throw new IllegalArgumentException(option + " requires a value");
            }
            return value;
        }
    }
}
