# @jetlinks/external-plugin-sdk

Node.js 24+ SDK for JetLinks external plugins. The public API is transport-neutral: plugin code
uses `Promise`, `AsyncIterable`, `AbortSignal`, JSON values and `Uint8Array`; RSocket is only the
first runtime binding.

## Run a plugin

```ts
import {
  ExternalPluginServer,
  profiles,
} from '@jetlinks/external-plugin-sdk';
import { readFileSync } from 'node:fs';

const driver = {
  description: {
    id: 'example',
    name: 'Example plugin',
    type: profiles.standalone.name,
  },
  async createPlugin(id) {
    return {
      id,
      type: 'example',
      state: 'stopped',
      async start() {},
      async pause() {},
      async shutdown() {},
      async execute(commandId, args) {
        return {commandId, args};
      },
    };
  },
  async execute(commandId, args) {
    return {commandId, args};
  },
};

const server = new ExternalPluginServer(driver, {
  host: '127.0.0.1',
  port: 7000,
  runtimeId: process.env.JETLINKS_RUNTIME_ID,
  driverId: process.env.JETLINKS_DRIVER_ID,
  generation: Number(process.env.JETLINKS_GENERATION),
  credential: readFileSync('/run/secrets/jetlinks-plugin-token', 'utf8').trim(),
});
await server.start();
process.once('SIGTERM', async () => {
  server.beginDrain();
  await server.close();
});
```

For a local Unix domain socket, replace `port` with `unixSocket: '/run/jetlinks/plugin.sock'`.
The server removes only that socket path during startup/shutdown; it never accepts both TCP and
Unix addresses in one runtime.

`device` and `collector` are capability profiles (`profiles.device`, `profiles.collector`) that
use the same driver and lifecycle contract. Their typed platform adapters are added in the cloud
integration milestones; the SDK does not embed platform entities or manager dependencies.

The server supports setup authentication, request-response, request-stream, request-channel,
resource streaming, bounded frames/resources, cancellation and demand-driven delivery. Commands
are never replayed after a disconnect. `connectRSocket` uses TCP and `connectRSocketUnix` uses a
Node Unix domain socket; both return the same transport-neutral `ExternalPluginConnection`.
