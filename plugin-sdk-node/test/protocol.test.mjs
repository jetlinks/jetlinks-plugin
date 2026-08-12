import test from 'node:test';
import assert from 'node:assert/strict';
import {WireCodec, WireProtocolError, PROTOCOL_VERSION, ROUTES, DATA_MIME_TYPE, METADATA_MIME_TYPE} from '../dist/protocol.js';

test('wire constants match the Java protocol registry', () => {
  assert.equal(DATA_MIME_TYPE, 'application/json');
  assert.equal(METADATA_MIME_TYPE, 'text/plain');
  assert.equal(ROUTES.pluginStart, 'plugin.lifecycle.start');
  assert.equal(ROUTES.pluginCommand, 'plugin.command.execute');
  assert.equal(ROUTES.driverResource, 'driver.resource.get');
});

test('codec preserves protocol values and ignores unknown fields', () => {
  const codec = new WireCodec(4096);
  const value = codec.decode(codec.encode({version: PROTOCOL_VERSION, requestId: 'r1', unknown: true}));
  assert.equal(value.requestId, 'r1');
  assert.equal(value.unknown, true);
});

test('codec rejects oversized frames before parsing', () => {
  const codec = new WireCodec(1024);
  assert.throws(() => codec.encode({body: 'x'.repeat(2048)}), WireProtocolError);
});

test('codec enforces bounded metadata', () => {
  const codec = new WireCodec(4096);
  assert.throws(() => codec.request({
    interaction: 'REQUEST_RESPONSE',
    route: 'test',
    requestId: 'r1',
    deadlineEpochMillis: Date.now() + 1000,
    metadata: Object.fromEntries(Array.from({length: 33}, (_, index) => [`k${index}`, 'v']))
  }), WireProtocolError);
});
