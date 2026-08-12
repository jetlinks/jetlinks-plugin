import { assertResponse, ROUTES, WireCodec, type JsonValue, type SetupMessage, type WireRequest, type WireResponse } from './protocol.js';
import type { ExternalPluginConnection, HostServiceClient, Plugin, PluginCommandContext, PluginContext, PluginDescription } from './api.js';

function abortError(signal: AbortSignal): Error {
  return signal.reason instanceof Error ? signal.reason : new DOMException('The operation was aborted', 'AbortError');
}

function deadline(timeoutMs: number): number {
  return Date.now() + timeoutMs;
}

const noopHost: HostServiceClient = {
  execute: async () => { throw new Error('host services are unavailable on the platform-side client context'); },
  stream: async function* () { throw new Error('host services are unavailable on the platform-side client context'); },
  monitorEvent: async () => { throw new Error('host services are unavailable on the platform-side client context'); }
};

export interface ExternalPluginClientOptions {
  readonly runtimeId: string;
  readonly driverId: string;
  readonly generation: number;
  readonly credential?: string;
  readonly requestTimeoutMs?: number;
  readonly codec?: WireCodec;
}

export class ExternalPluginClient {
  private readonly timeoutMs: number;
  private readonly codec: WireCodec;
  private readonly context: PluginContext;

  public constructor(private readonly connection: ExternalPluginConnection,
                     private readonly options: ExternalPluginClientOptions) {
    this.timeoutMs = options.requestTimeoutMs ?? 10000;
    this.codec = options.codec ?? new WireCodec();
    this.context = {
      runtimeId: options.runtimeId,
      driverId: options.driverId,
      services: new Map(),
      monitor: { event: async () => undefined, error: async () => undefined },
      host: noopHost
    };
  }

  public async describe(signal?: AbortSignal): Promise<PluginDescription> {
    const response = await this.requestResponse(ROUTES.describe, undefined, signal);
    const body = assertResponse(response).body;
    if (!body || typeof body !== 'object' || Array.isArray(body)) {
      throw new Error('external plugin describe response is invalid');
    }
    return body as unknown as PluginDescription;
  }

  public async createPlugin(pluginId: string, signal?: AbortSignal): Promise<Plugin> {
    const response = await this.requestResponse(ROUTES.create, { pluginId }, signal);
    assertResponse(response);
    return new RemotePlugin(this, pluginId, 'external');
  }

  public async runtimeHealth(signal?: AbortSignal): Promise<JsonValue> {
    const response = await this.requestResponse(ROUTES.runtimeHealth, undefined, signal);
    return assertResponse(response).body ?? null;
  }

  public async runtimeDrain(signal?: AbortSignal): Promise<JsonValue> {
    const response = await this.requestResponse(ROUTES.runtimeDrain, undefined, signal);
    return assertResponse(response).body ?? null;
  }

  public async close(): Promise<void> {
    await this.connection.close();
  }

  public async requestResponse(route: string, body?: JsonValue, signal?: AbortSignal): Promise<WireResponse> {
    const request = this.request('REQUEST_RESPONSE', route, body);
    return this.connection.requestResponse(request, signal);
  }

  public requestStream(route: string, body?: JsonValue, signal?: AbortSignal): AsyncIterable<WireResponse> {
    return this.connection.requestStream(this.request('REQUEST_STREAM', route, body), signal);
  }

  public requestChannel(route: string,
                        bodies: AsyncIterable<JsonValue>,
                        signal?: AbortSignal): AsyncIterable<WireResponse> {
    const requests = (async function* (client: ExternalPluginClient): AsyncIterable<WireRequest> {
      for await (const body of bodies) {
        yield client.request('REQUEST_CHANNEL', route, body);
      }
    })(this);
    return this.connection.requestChannel(requests, signal);
  }

  public async *resource(name: string, signal?: AbortSignal): AsyncIterable<Uint8Array> {
    for await (const response of this.requestStream(ROUTES.driverResource, { name }, signal)) {
      const value = assertResponse(response);
      if (response.complete) return;
      const body = value.body as { dataBase64?: unknown } | undefined;
      if (!body || typeof body.dataBase64 !== 'string') throw new Error('resource chunk is invalid');
      yield Buffer.from(body.dataBase64, 'base64');
    }
  }

  public contextValue(): PluginContext {
    return this.context;
  }

  public codecValue(): WireCodec {
    return this.codec;
  }

  private request(interaction: WireRequest['interaction'], route: string, body?: JsonValue): WireRequest {
    return this.codec.request({
      interaction,
      route,
      requestId: `${this.options.driverId}-${crypto.randomUUID()}`,
      deadlineEpochMillis: deadline(this.timeoutMs),
      ...(body === undefined ? {} : { body })
    });
  }
}

class RemotePlugin implements Plugin {
  public state: Plugin['state'] = 'stopped';

  public constructor(private readonly client: ExternalPluginClient,
                     public readonly id: string,
                     public readonly type: string) {
  }

  public async start(signal?: AbortSignal): Promise<void> {
    await this.lifecycle(ROUTES.pluginStart, 'running', signal);
  }

  public async pause(signal?: AbortSignal): Promise<void> {
    await this.lifecycle(ROUTES.pluginPause, 'paused', signal);
  }

  public async shutdown(signal?: AbortSignal): Promise<void> {
    await this.lifecycle(ROUTES.pluginShutdown, 'stopped', signal);
  }

  public async *execute(commandId: string, arguments_: JsonValue, context: PluginCommandContext): AsyncIterable<JsonValue> {
    for await (const response of this.client.requestStream(ROUTES.pluginCommand,
      { pluginId: this.id, commandId, arguments: arguments_ }, context.signal)) {
      const value = assertResponse(response);
      if (!response.complete) {
        yield value.body ?? null;
      }
    }
  }

  private async lifecycle(route: string, state: Plugin['state'], signal?: AbortSignal): Promise<void> {
    const response = await this.client.requestResponse(route, { pluginId: this.id }, signal);
    assertResponse(response);
    this.state = state;
  }
}

export function setupMessage(options: ExternalPluginClientOptions,
                             sdkVersion: string,
                             codec: WireCodec = options.codec ?? new WireCodec()): SetupMessage {
  return {
    version: '1.0',
    runtimeId: options.runtimeId,
    driverId: options.driverId,
    generation: options.generation,
    sdkVersion,
    ...(options.credential === undefined ? {} : { credential: options.credential }),
    maxFrameBytes: codec.maxFrameBytes
  };
}
