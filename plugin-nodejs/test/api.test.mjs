import test from 'node:test';
import assert from 'node:assert/strict';
import { profiles } from '../dist/index.js';

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
