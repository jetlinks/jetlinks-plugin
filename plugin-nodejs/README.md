# @jetlinks/plugin-nodejs

Node.js 24+ transport-neutral SPI for JetLinks plugins. The platform owns startup,
authentication, RSocket, resource limits and lifecycle supervision. A plugin only declares its
manifest, business commands and the platform services it is allowed to call.

## Minimal plugin

```ts
import {command, definePlugin} from '@jetlinks/plugin-nodejs';

export default definePlugin({
  manifest: {
    id: 'example',
    name: 'Example plugin',
    type: 'standalone'
  },
  commands: {
    'system.health': command(async () => ({
      status: 'UP',
      uptime: process.uptime()
    }))
  }
});
```

## Calling platform services

Declare the service and command IDs in the manifest. The IDs are the platform command contract;
the SDK does not rename commands or expose platform Java classes.

```ts
import {definePlugin, type ServiceCommand} from '@jetlinks/plugin-nodejs';

interface DeviceService {
  QueryList: ServiceCommand<{where?: string}, {id: string}>;
  QueryById: ServiceCommand<{id: string}, {id: string} | undefined>;
}

export default definePlugin({
  manifest: {
    id: 'device-gateway',
    name: 'Device gateway',
    type: 'device',
    requires: {
      services: {
        'deviceService:device': ['QueryList', 'QueryById']
      }
    }
  },
  commands: {
    'gateway.lookup': async (input, ctx) => {
      const device = ctx.service<DeviceService>('deviceService:device');
      return device.QueryById({id: input.id});
    }
  }
});
```

Every service proxy also exposes the explicit, language-neutral form:

```ts
const device = ctx.service('deviceService:device');
const item = await device.call('QueryById', {id: 'device-001'});

for await (const item of device.QueryList.stream({where: 'productId is product-001'})) {
  console.log(item);
}
```

`device.QueryById(input)` is syntax sugar for `device.call('QueryById', input)`. Streaming commands
use `AsyncIterable`, not RSocket or Reactor types. Cancellation and deadlines are passed through
`AbortSignal` and `CallOptions`. Command IDs are the platform contract and are not renamed by the SDK.

Dynamic resource scope is separate from the service ID and participates in platform authorization:

```ts
const channel = ctx.service('collectorService:channel', {
  target: {channelId: 'channel-001'}
});
```

Java plugins reuse the existing service registry and platform `CommandSupport` contract:

```java
CommandSupport channel = context.services()
    .getServiceNow(
        CommandSupport.class,
        "collectorService:channel",
        Collections.singletonMap(
            "target",
            Collections.singletonMap("channelId", "channel-001")
        )
    );
```

Both languages preserve target as independent wire metadata rather than concatenating it into the
service id. Node's `ctx.service(...)` is the idiomatic Node API; Java keeps the existing
`PluginContext.services()` and `CommandSupport` API instead of defining a parallel service facade.

Undeclared services and commands fail closed before a transport request is made. The platform
performs the same allowlist, tenant/asset scope, generation, quota and audit checks at runtime.

The same SPI supports `standalone`, `device` and `collector` profiles. Runtime adapters are
deliberately not part of this package so plugin business code can run in attached processes or
containers without changing its implementation.
