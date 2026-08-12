import type { JsonValue, WireRequest, WireResponse } from './protocol.js';

export type MaybePromise<T> = T | PromiseLike<T>;

export interface PluginDescription {
  readonly id: string;
  readonly name: string;
  readonly type: string;
  readonly description?: string;
  readonly version?: string;
  readonly others?: Readonly<Record<string, JsonValue>>;
}

export type PluginState = 'stopped' | 'running' | 'paused';

export interface PluginCommandContext {
  readonly signal?: AbortSignal;
  readonly metadata: Readonly<Record<string, string>>;
}

export interface PluginContext {
  readonly runtimeId: string;
  readonly driverId: string;
  readonly workDir?: string;
  readonly services: ReadonlyMap<string, unknown>;
  readonly monitor: Monitor;
  /** Bidirectional platform command facade; service ids are still platform-allowlisted. */
  readonly host: HostServiceClient;
}

export interface HostServiceClient {
  execute(serviceId: string, commandId: string, arguments_: JsonValue, signal?: AbortSignal): Promise<JsonValue>;
  stream(serviceId: string, commandId: string, arguments_: JsonValue, signal?: AbortSignal): AsyncIterable<JsonValue>;
  monitorEvent(name: string, payload?: JsonValue, error?: unknown, signal?: AbortSignal): Promise<void>;
}

export interface Monitor {
  event(name: string, payload?: JsonValue): MaybePromise<void>;
  error(name: string, error: unknown, payload?: JsonValue): MaybePromise<void>;
}

export interface Plugin {
  readonly id: string;
  readonly type: string;
  readonly state: PluginState;
  start(signal?: AbortSignal): Promise<void>;
  pause(signal?: AbortSignal): Promise<void>;
  shutdown(signal?: AbortSignal): Promise<void>;
  execute(commandId: string, arguments_: JsonValue, context: PluginCommandContext): MaybePromise<JsonValue | AsyncIterable<JsonValue>>;
}

export interface PluginDriver {
  readonly description: PluginDescription;
  createPlugin(pluginId: string, context: PluginContext): MaybePromise<Plugin>;
  execute(commandId: string, arguments_: JsonValue, context: PluginCommandContext): MaybePromise<JsonValue | AsyncIterable<JsonValue>>;
  resource?(name: string, signal?: AbortSignal): AsyncIterable<Uint8Array>;
}

export type PluginProfileName = 'standalone' | 'device' | 'collector';

export interface PluginProfile {
  readonly name: PluginProfileName;
  readonly capabilities: readonly string[];
}

export const profiles: Readonly<Record<PluginProfileName, PluginProfile>> = {
  standalone: { name: 'standalone', capabilities: ['plugin.lifecycle', 'plugin.command'] },
  device: { name: 'device', capabilities: ['plugin.lifecycle', 'plugin.command', 'device.gateway'] },
  collector: { name: 'collector', capabilities: ['plugin.lifecycle', 'plugin.command', 'collector'] }
};

export interface ExternalPluginConnection {
  readonly transport: string;
  requestResponse(request: WireRequest, signal?: AbortSignal): Promise<WireResponse>;
  requestStream(request: WireRequest, signal?: AbortSignal): AsyncIterable<WireResponse>;
  requestChannel(requests: AsyncIterable<WireRequest>, signal?: AbortSignal): AsyncIterable<WireResponse>;
  close(): Promise<void>;
}
