import test from 'node:test';
import assert from 'node:assert/strict';
import net from 'node:net';
import os from 'node:os';
import path from 'node:path';
import { rm } from 'node:fs/promises';
import {
  ExternalPluginClient,
  ExternalPluginServer,
  ROUTES,
  connectRSocket,
  connectRSocketUnix,
  profiles,
  setupMessage,
  WireCodec
} from '../dist/index.js';

async function freePort() {
  const probe = net.createServer();
  await new Promise((resolve, reject) => probe.listen(0, '127.0.0.1', error => error ? reject(error) : resolve()));
  const address = probe.address();
  const port = address.port;
  await new Promise(resolve => probe.close(resolve));
  return port;
}

test('Node SDK server and client complete the lifecycle and stream command contract', async () => {
  const port = await freePort();
  let state = 'stopped';
  const driver = {
    description: { id: 'node-driver', name: 'Node driver', type: profiles.standalone.name },
    async createPlugin(pluginId) {
      return {
        id: pluginId,
        type: 'node-plugin',
        get state() { return state; },
        async start() { state = 'running'; },
        async pause() { state = 'paused'; },
        async shutdown() { state = 'stopped'; },
        async execute(commandId, args) {
          if (commandId !== 'echo') throw new Error('unknown command');
          return (async function* () {
            yield { value: args.value };
            yield { value: 'last' };
          })();
        }
      };
    },
    async execute() { return { ok: true }; },
    async *resource(name) {
      assert.equal(name, 'manifest.json');
      yield new Uint8Array([1, 2, 3]);
    }
  };
  const server = new ExternalPluginServer(driver, {
    port,
    runtimeId: 'runtime-1',
    driverId: 'node-driver',
    generation: 1,
    credential: 'secret',
    onError: error => console.error('server error', error)
  });
  await server.start();

  const options = { runtimeId: 'runtime-1', driverId: 'node-driver', generation: 1, credential: 'secret' };
  const connection = await connectRSocket({ host: '127.0.0.1', port, setup: setupMessage(options, 'test', new WireCodec()) });
  const client = new ExternalPluginClient(connection, options);
  try {
    assert.equal((await client.describe()).id, 'node-driver');
    const chunks = [];
    for await (const chunk of client.resource('manifest.json')) chunks.push(...chunk);
    assert.deepEqual(chunks, [1, 2, 3]);
    const plugin = await client.createPlugin('plugin-1');
    await plugin.start();
    const channelBodies = (async function* () {
      yield { pluginId: 'plugin-1' };
      yield { pluginId: 'plugin-1' };
    })();
    const channelResponses = [];
    for await (const response of client.requestChannel(ROUTES.pluginPause, channelBodies)) channelResponses.push(response);
    assert.equal(channelResponses.length, 2);
    const values = [];
    for await (const item of plugin.execute('echo', { value: 'ok' }, { metadata: {} })) values.push(item);
    assert.deepEqual(values, [{ value: 'ok' }, { value: 'last' }]);
    await plugin.shutdown();
    assert.deepEqual(await client.runtimeHealth(), { ready: true });
    assert.deepEqual(await client.runtimeDrain(), { draining: true });
    assert.deepEqual(await client.runtimeHealth(), { ready: false });
  } finally {
    await client.close();
    await server.close();
  }
});

test('Node SDK server accepts a Unix domain socket endpoint', async () => {
  const socket = path.join(os.tmpdir(), `jetlinks-plugin-${process.pid}-${Date.now()}.sock`);
  const driver = {
    description: { id: 'node-unix-driver', name: 'Node Unix driver', type: profiles.standalone.name },
    async createPlugin(id) {
      return {
        id,
        type: 'node-plugin',
        state: 'stopped',
        async start() {},
        async pause() {},
        async shutdown() {},
        async execute(_commandId, args) { return args; }
      };
    },
    async execute(_commandId, args) { return args; }
  };
  const server = new ExternalPluginServer(driver, {
    unixSocket: socket,
    runtimeId: 'runtime-unix',
    driverId: 'node-unix-driver',
    generation: 1,
    credential: 'secret'
  });
  await server.start();
  const options = { runtimeId: 'runtime-unix', driverId: 'node-unix-driver', generation: 1, credential: 'secret' };
  const connection = await connectRSocketUnix({ path: socket, setup: setupMessage(options, 'test', new WireCodec()) });
  const client = new ExternalPluginClient(connection, options);
  try {
    assert.equal((await client.describe()).id, 'node-unix-driver');
  } finally {
    await client.close();
    await server.close();
    await rm(socket, { force: true });
  }
});
