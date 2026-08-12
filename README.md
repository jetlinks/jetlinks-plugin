# jetlinks-plugin

JetLinks 插件公开 SPI。

- `plugin-core`：Java 插件 `PluginDriver`、`Plugin`、上下文和生命周期契约。
- `plugin-nodejs`：Node.js 24+ 的等价 transport-neutral SPI。

进程、容器、RSocket、wire protocol、credential 和平台 host bridge 属于平台内部 runtime，
不作为插件开发者依赖发布。
