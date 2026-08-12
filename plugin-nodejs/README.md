# @jetlinks/plugin-nodejs

Node.js 24+ SPI for JetLinks plugins. The package contains only transport-neutral plugin contracts;
the platform owns startup, authentication, resource limits and lifecycle supervision.

```ts
import type { PluginDriver } from '@jetlinks/plugin-nodejs';

export default function createDriver(): PluginDriver {
  return {
    description: {
      id: 'example',
      name: 'Example plugin',
      type: 'standalone',
      version: '1.0.0'
    },
    async createPlugin(pluginId, context) {
      return {
        id: pluginId,
        type: 'example',
        state: 'stopped',
        async start() {
          await context.monitor.event('plugin.started');
        },
        async pause() {},
        async shutdown() {},
        async execute(commandId, arguments_) {
          return { commandId, arguments_ };
        }
      };
    },
    async execute(commandId, arguments_) {
      return { commandId, arguments_ };
    }
  };
}
```

The same SPI is used for `standalone`, `device` and `collector` profiles. Runtime adapters are
deliberately not part of this package so that plugins can run in different deployment environments
without changing business code.
