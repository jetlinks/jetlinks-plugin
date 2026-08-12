/** JSON-compatible value used at the platform/plugin boundary. */
export type JsonPrimitive = string | number | boolean | null;
export type JsonValue = JsonPrimitive | JsonValue[] | { readonly [key: string]: JsonValue };

export type MaybePromise<T> = T | PromiseLike<T>;

export interface Disposable {
  dispose(): void;
}

export interface PluginDescription {
  readonly id: string;
  readonly name: string;
  readonly type: string;
  readonly description?: string;
  readonly version?: string;
  readonly others?: Readonly<Record<string, JsonValue>>;
}

export type PluginState = 'stopped' | 'starting' | 'running' | 'paused';

export interface PluginCommandContext {
  readonly signal?: AbortSignal;
  readonly metadata: Readonly<Record<string, string>>;
}

/** Platform capabilities exposed to a plugin without coupling it to a transport. */
export interface HostServiceClient {
  execute(serviceId: string,
          commandId: string,
          arguments_: JsonValue,
          signal?: AbortSignal): Promise<JsonValue>;
  stream(serviceId: string,
         commandId: string,
         arguments_: JsonValue,
         signal?: AbortSignal): AsyncIterable<JsonValue>;
  monitorEvent(name: string,
               payload?: JsonValue,
               error?: unknown,
               signal?: AbortSignal): Promise<void>;
}

export interface Monitor {
  event(name: string, payload?: JsonValue): MaybePromise<void>;
  error(name: string, error: unknown, payload?: JsonValue): MaybePromise<void>;
}

export interface PluginEnvironment {
  getProperty(key: string): string | undefined;
  getProperties(): Readonly<Record<string, JsonValue>>;
}

export interface ServiceRegistry {
  getService<T = unknown>(serviceId: string): T | undefined;
}

export interface PluginScheduler {
  schedule(name: string,
           task: () => MaybePromise<void>,
           options: { readonly intervalMs?: number; readonly singleton?: boolean }): Disposable;
  cancel(name: string): void;
}

export interface PluginContext {
  readonly runtimeId: string;
  readonly driverId: string;
  readonly workDir?: string;
  readonly services: ServiceRegistry;
  readonly environment: PluginEnvironment;
  readonly scheduler: PluginScheduler;
  readonly monitor: Monitor;
  readonly host: HostServiceClient;
}

export interface Plugin {
  readonly id: string;
  readonly type: string;
  readonly state: PluginState;
  start(signal?: AbortSignal): MaybePromise<void>;
  pause(signal?: AbortSignal): MaybePromise<void>;
  shutdown(signal?: AbortSignal): MaybePromise<void>;
  execute(commandId: string,
          arguments_: JsonValue,
          context: PluginCommandContext): MaybePromise<JsonValue | AsyncIterable<JsonValue>>;
}

export interface PluginDriver {
  readonly description: PluginDescription;
  createPlugin(pluginId: string, context: PluginContext): MaybePromise<Plugin>;
  execute(commandId: string,
          arguments_: JsonValue,
          context: PluginCommandContext): MaybePromise<JsonValue | AsyncIterable<JsonValue>>;
  resource?(name: string,
            signal?: AbortSignal): MaybePromise<Uint8Array | AsyncIterable<Uint8Array>>;
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
