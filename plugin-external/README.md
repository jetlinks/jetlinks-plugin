# Java external plugin runtime

`plugin-external` provides the Java RSocket binding and the fixed `JavaPluginRunner` entry point.
The runner loads exactly one `PluginDriver` from `META-INF/services` in the supplied JAR.

```sh
java -cp plugin-external.jar org.jetlinks.plugin.external.runner.JavaPluginRunner \
  --plugin /opt/jetlinks/plugins/example.jar \
  --host 0.0.0.0 --port 7000 \
  --runtime-id runtime-1 --driver-id example --generation 1 \
  --credential-file /run/secrets/jetlinks-plugin-token
```

Credentials are read from a regular file with no group/other POSIX permissions, are bounded to
4 KiB, and are not accepted as command-line arguments. Shutdown drains the RSocket server and
closes the plugin classloader. A JAR must expose exactly one `PluginDriver`; zero or multiple
providers fail before the listener is opened.
