import test from 'node:test';
import assert from 'node:assert/strict';
import {
  HostServiceError,
  createPluginContext,
  definePlugin,
  profiles,
  stream
} from '../dist/index.js';

test('profiles expose only capability metadata', () => {
  assert.deepEqual(profiles.standalone, {
    name: 'standalone',
    capabilities: ['plugin.lifecycle', 'plugin.command']
  });
  assert.deepEqual(profiles.device.capabilities, [
    'plugin.lifecycle',
    'plugin.command',
    'device.gateway'
  ]);
  assert.deepEqual(profiles.collector.capabilities, [
    'plugin.lifecycle',
    'plugin.command',
    'collector'
  ]);
});

test('the package exports the SPI surface', async () => {
  const api = await import('../dist/index.js');
  assert.equal(typeof api.profiles, 'object');
  assert.equal(typeof api.profiles.standalone.name, 'string');
});

test('ctx.service exposes a command proxy and preserves service target metadata', async () => {
  const requests = [];
  const transport = {
    async call(request) {
      requests.push(request);
      return {id: 'device-001'};
    },
    async *stream() {},
    async describe(serviceId) {
      return {id: serviceId, commands: []};
    }
  };
  const context = createPluginContext({
    pluginId: 'device-plugin',
    transport,
    requirements: {
      services: {'deviceService:device': ['QueryList']}
    }
  });

  const device = context.service('deviceService:device', {
    target: {tenantId: 'tenant-001'}
  });
  assert.deepEqual(await device.QueryList({where: 'id is device-001'}), {id: 'device-001'});
  assert.deepEqual(requests, [{
    serviceId: 'deviceService:device',
    commandId: 'QueryList',
    arguments: {where: 'id is device-001'},
    reference: {target: {tenantId: 'tenant-001'}}
  }]);
});

test('explicit call and stream share one service contract', async () => {
  const transport = {
    async call(request) {
      assert.equal(request.commandId, 'QueryById');
      return {id: request.arguments.id};
    },
    async *stream(request) {
      assert.equal(request.commandId, 'QueryList');
      yield {id: 'device-001'};
      yield {id: 'device-002'};
    },
    async describe(serviceId) {
      return {id: serviceId, commands: []};
    }
  };
  const context = createPluginContext({
    pluginId: 'device-plugin',
    transport,
    requirements: {services: {'deviceService:device': ['QueryById', 'QueryList']}}
  });
  const device = context.service('deviceService:device');

  assert.deepEqual(await device.call('QueryById', {id: 'device-001'}), {id: 'device-001'});
  const values = [];
  for await (const value of device.stream('QueryList', {})) values.push(value);
  assert.deepEqual(values, [
    {id: 'device-001'},
    {id: 'device-002'}
  ]);
});

test('service and command allowlists fail closed before transport invocation', async () => {
  let invoked = false;
  const transport = {
    async call() {
      invoked = true;
      return null;
    },
    async *stream() {},
    async describe(serviceId) {
      return {id: serviceId, commands: []};
    }
  };
  const context = createPluginContext({
    pluginId: 'standalone-plugin',
    transport,
    requirements: {services: {'deviceService:device': ['QueryList']}}
  });

  assert.throws(() => context.service('deviceService:gateway'), error =>
    error instanceof HostServiceError && error.code === 'service_not_declared');
  const device = context.service('deviceService:device');
  await assert.rejects(device.call('remove'), error =>
    error instanceof HostServiceError && error.code === 'command_not_declared');
  assert.equal(invoked, false);
});

test('definePlugin validates manifest service declarations and stream definitions', () => {
  const definition = definePlugin({
    manifest: {
      id: 'collector',
      name: 'Collector',
      type: 'collector',
      requires: {services: {'collectorService:channel': ['read']}}
    },
    commands: {
      read: stream(async function* () {
        yield {value: 1};
      })
    }
  });
  assert.equal(definition.manifest.requires.services['collectorService:channel'][0], 'read');
  assert.equal(definition.commands.read.mode, 'stream');
});

test('unary service calls observe AbortSignal cancellation', async () => {
  const controller = new AbortController();
  const transport = {
    call() {
      return new Promise(() => {});
    },
    async *stream() {},
    async describe(serviceId) {
      return {id: serviceId, commands: []};
    }
  };
  const context = createPluginContext({
    pluginId: 'device-plugin',
    transport,
    requirements: {services: {'deviceService:device': ['QueryList']}}
  });
  const pending = context.service('deviceService:device').QueryList({}, {signal: controller.signal});
  controller.abort();
  await assert.rejects(pending, error =>
    error instanceof HostServiceError && error.code === 'cancelled');
});
