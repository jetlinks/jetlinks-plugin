import { timingSafeEqual } from 'node:crypto';
import { unlink } from 'node:fs/promises';
import net from 'node:net';
import { BufferEncoders, IdentitySerializers, RSocketServer } from 'rsocket-core';
import { Flowable } from 'rsocket-flowable';
import RSocketTcpServer from 'rsocket-tcp-server';
import type { HostServiceClient, Plugin, PluginCommandContext, PluginContext, PluginDriver, PluginState } from './api.js';
import { ROUTES, WireCodec, WireProtocolError, type JsonValue, type SetupMessage, type WireRequest, type WireResponse } from './protocol.js';

function defaultExport(value: unknown): any {
  const module = value as { default?: unknown };
  const nested = module.default as { default?: unknown } | undefined;
  return nested?.default ?? module.default ?? value;
}

export interface ExternalPluginServerOptions {
  readonly host?: string;
  readonly port?: number;
  readonly unixSocket?: string;
  readonly runtimeId: string;
  readonly driverId: string;
  readonly generation: number;
  readonly credential?: string;
  readonly sdkVersion?: string;
  readonly codec?: WireCodec;
  readonly onError?: (error: unknown) => void;
  readonly maxResourceBytes?: number;
}

function success(requestId: string, body: JsonValue | undefined, complete: boolean): WireResponse {
  return { version: '1.0', requestId, success: true, complete, ...(body === undefined ? {} : { body }) };
}

function failure(requestId: string, error: unknown): WireResponse {
  const message = error instanceof Error ? error.message : 'plugin execution failed';
  const code = error instanceof WireProtocolError ? error.code : 'plugin_execution_failed';
  return {
    version: '1.0',
    requestId,
    success: false,
    complete: true,
    error: { code, message }
  };
}

function asJson(value: unknown): JsonValue {
  return value === undefined ? null : value as JsonValue;
}

function asArguments(request: WireRequest): JsonValue {
  return request.body && typeof request.body === 'object' && !Array.isArray(request.body)
    ? ((request.body as Record<string, JsonValue>).arguments ?? {})
    : {};
}

function toAsyncIterable(value: JsonValue | AsyncIterable<JsonValue>): AsyncIterable<JsonValue> {
  if (value && typeof value === 'object' && Symbol.asyncIterator in value) {
    return value as AsyncIterable<JsonValue>;
  }
  return (async function* (): AsyncIterable<JsonValue> { yield value as JsonValue; })();
}

function flowableFromAsyncIterable<T>(source: AsyncIterable<T>): any {
  return new Flowable((subscriber: any) => {
    let cancelled = false;
    let completed = false;
    let pulling = false;
    const iterator = source[Symbol.asyncIterator]();
    const pull = async (count: number): Promise<void> => {
      if (cancelled || completed || pulling) return;
      pulling = true;
      try {
        for (let index = 0; index < count && !cancelled; index += 1) {
          const next = await iterator.next();
          if (next.done) {
            completed = true;
            subscriber.onComplete();
            break;
          }
          subscriber.onNext(next.value);
        }
      } catch (error) {
        subscriber.onError(error);
        completed = true;
      } finally {
        pulling = false;
      }
    };
    subscriber.onSubscribe({
      request: (count: number) => { void pull(count); },
      cancel: () => { cancelled = true; void iterator.return?.(); }
    });
  });
}

function singleFromPromise<T>(work: () => Promise<T>): any {
  return {
    subscribe(subscriber: any): void {
      let cancelled = false;
      subscriber.onSubscribe(() => { cancelled = true; });
      void work().then(value => {
        if (!cancelled) subscriber.onComplete(value);
      }, error => {
        if (!cancelled) subscriber.onError(error);
      });
    }
  };
}

async function* flowableToAsyncIterable<T>(source: any): AsyncIterable<T> {
  const queue: T[] = [];
  let complete = false;
  let failure: unknown;
  let wake: (() => void) | undefined;
  const subscription = await new Promise<any>(resolve => {
    source.subscribe({
      onSubscribe: (value: any) => resolve(value),
      onNext: (value: T) => { queue.push(value); wake?.(); },
      onComplete: () => { complete = true; wake?.(); },
      onError: (error: unknown) => { failure = error; complete = true; wake?.(); }
    });
  });
  subscription.request(1);
  try {
    while (!complete || queue.length > 0) {
      if (queue.length === 0) {
        await new Promise<void>(resolve => { wake = resolve; });
        wake = undefined;
        continue;
      }
      const next = queue.shift() as T;
      subscription.request(1);
      yield next;
    }
    if (failure !== undefined) throw failure;
  } finally {
    subscription.cancel();
  }
}

class ServerHostClient implements HostServiceClient {
  public constructor(private readonly socket: any,
                     private readonly codec: WireCodec,
                     private readonly options: ExternalPluginServerOptions,
                     private readonly contextId: string) {}

  public async execute(serviceId: string, commandId: string, arguments_: JsonValue, signal?: AbortSignal): Promise<JsonValue> {
    const response = await this.first(this.socket.requestResponse({ data: this.codec.encode(this.request('REQUEST_RESPONSE', ROUTES.hostCommand, {
      serviceId, commandId, arguments: arguments_
    })) }), signal);
    this.assert(response);
    return response.body ?? null;
  }

  public async *stream(serviceId: string, commandId: string, arguments_: JsonValue, signal?: AbortSignal): AsyncIterable<JsonValue> {
    const source = this.socket.requestStream({ data: this.codec.encode(this.request('REQUEST_STREAM', ROUTES.hostCommand, {
      serviceId, commandId, arguments: arguments_
    })) });
    for await (const payload of flowableToAsyncIterable<{ data: Buffer | Uint8Array }>(source)) {
      if (signal?.aborted) throw signal.reason ?? new DOMException('The operation was aborted', 'AbortError');
      const response = this.codec.decode<WireResponse>(payload.data);
      this.assert(response);
      if (response.complete) return;
      yield response.body ?? null;
    }
  }

  public async monitorEvent(name: string, payload?: JsonValue, error?: unknown, signal?: AbortSignal): Promise<void> {
    const response = await this.first(this.socket.requestResponse({ data: this.codec.encode(this.request('REQUEST_RESPONSE', ROUTES.hostMonitor, {
      name,
      ...(payload === undefined ? {} : { payload }),
      ...(error === undefined ? {} : { error: error instanceof Error ? error.name : String(error) })
    })) }), signal);
    this.assert(response);
  }

  private request(interaction: WireRequest['interaction'], route: string, body: JsonValue): WireRequest {
    return this.codec.request({
      interaction,
      route,
      requestId: `${this.options.driverId}-${crypto.randomUUID()}`,
      deadlineEpochMillis: Date.now() + 10000,
      metadata: {
        sessionId: this.options.runtimeId,
        driverId: this.options.driverId,
        contextId: this.contextId,
        generation: String(this.options.generation)
      },
      body
    });
  }

  private async first(flowable: any, signal?: AbortSignal): Promise<WireResponse> {
    for await (const payload of flowableToAsyncIterable<{ data: Buffer | Uint8Array }>(flowable)) {
      if (signal?.aborted) throw signal.reason ?? new DOMException('The operation was aborted', 'AbortError');
      return this.codec.decode<WireResponse>(payload.data);
    }
    throw new Error('host response was empty');
  }

  private assert(response: WireResponse): void {
    if (!response.success) {
      throw new WireProtocolError(response.error?.code ?? 'host_error', response.error?.message ?? 'host request failed');
    }
  }
}

export class ExternalPluginServer {
  private readonly codec: WireCodec;
  private readonly plugins = new Map<string, Plugin>();
  private readonly context: PluginContext;
  private readonly credential: Buffer | undefined;
  private readonly maxResourceBytes: number;
  private server?: any;
  private startSignal: Promise<void> | undefined;
  private started = false;
  private draining = false;

  public constructor(private readonly driver: PluginDriver,
                     private readonly options: ExternalPluginServerOptions) {
    if ((options.port === undefined) === (options.unixSocket === undefined)) {
      throw new Error('exactly one of port or unixSocket must be configured');
    }
    if (options.unixSocket !== undefined && options.unixSocket.trim().length === 0) {
      throw new Error('unixSocket must not be empty');
    }
    this.codec = options.codec ?? new WireCodec();
    this.maxResourceBytes = options.maxResourceBytes ?? 16 * 1024 * 1024;
    if (!Number.isSafeInteger(this.maxResourceBytes) || this.maxResourceBytes <= 0) {
      throw new RangeError('maxResourceBytes must be positive');
    }
    this.credential = options.credential === undefined ? undefined : Buffer.from(options.credential, 'utf8');
    this.context = {
      runtimeId: options.runtimeId,
      driverId: options.driverId,
      services: new Map(),
      monitor: { event: async () => undefined, error: async () => undefined },
      host: {
        execute: async () => { throw new Error('host services are unavailable before connection setup'); },
        stream: async function* () { throw new Error('host services are unavailable before connection setup'); },
        monitorEvent: async () => { throw new Error('host services are unavailable before connection setup'); }
      }
    };
  }

  public start(): Promise<void> {
    if (this.started && this.startSignal) return this.startSignal;
    this.started = true;
    let resolveReady: () => void = () => undefined;
    let rejectReady: (error: unknown) => void = () => undefined;
    this.startSignal = new Promise<void>((resolve, reject) => {
      resolveReady = resolve;
      rejectReady = reject;
    });
    const transport = new (defaultExport(RSocketTcpServer))({
      host: this.options.unixSocket === undefined ? (this.options.host ?? '127.0.0.1') : undefined,
      port: this.options.unixSocket ?? this.options.port,
      serverFactory: (onConnection: (socket: net.Socket) => void) => {
        const server = net.createServer(onConnection);
        server.once('listening', resolveReady);
        server.once('error', rejectReady);
        return server;
      }
    }, BufferEncoders);
    this.server = new (RSocketServer as any)({
      transport,
      serializers: IdentitySerializers,
      getRequestHandler: (socket: unknown, setupPayload: { data: Buffer | Uint8Array }) => {
        this.verifySetup(this.codec.decode<SetupMessage>(setupPayload.data));
        return {
          requestResponse: (payload: { data: Buffer | Uint8Array }) => singleFromPromise(async () => {
            const request = this.decodeRequest(payload.data);
            return { data: this.codec.encode(await this.handleResponse(request, socket)) };
          }),
          requestStream: (payload: { data: Buffer | Uint8Array }) => {
            const request = this.decodeRequest(payload.data);
            return flowableFromAsyncIterable(this.handleStream(request));
          },
          requestChannel: (payloads: any) => flowableFromAsyncIterable(this.handleChannel(payloads))
        };
      },
      errorHandler: (error: unknown) => this.options.onError?.(error)
    });
    const start = async (): Promise<void> => {
      if (this.options.unixSocket !== undefined) {
        await unlink(this.options.unixSocket).catch(error => {
          const code = (error as NodeJS.ErrnoException).code;
          if (code !== 'ENOENT') throw error;
        });
      }
      this.server.start();
      await this.startSignal;
    };
    return start();
  }

  public beginDrain(): void {
    this.draining = true;
  }

  public async close(): Promise<void> {
    if (!this.started) return;
    this.beginDrain();
    const results = await Promise.allSettled([...this.plugins.values()].map(plugin => plugin.shutdown()));
    this.plugins.clear();
    this.server?.stop();
    this.server = undefined;
    this.started = false;
    this.startSignal = undefined;
    if (this.options.unixSocket !== undefined) {
      await unlink(this.options.unixSocket).catch(error => {
        const code = (error as NodeJS.ErrnoException).code;
        if (code !== 'ENOENT') throw error;
      });
    }
    const failures = results.filter((result): result is PromiseRejectedResult => result.status === 'rejected');
    if (failures.length > 0) {
      throw new AggregateError(failures.map(failure => failure.reason), 'one or more plugins failed to shut down');
    }
  }

  private verifySetup(setup: SetupMessage): void {
    if (setup.runtimeId !== this.options.runtimeId
      || setup.driverId !== this.options.driverId
      || setup.generation !== this.options.generation
      || !this.matchesCredential(setup.credential)) {
      throw new Error('setup identity or credential is invalid');
    }
  }

  private matchesCredential(actual: string | undefined): boolean {
    if (this.credential === undefined) return actual === undefined || actual === '';
    if (actual === undefined) return false;
    const candidate = Buffer.from(actual, 'utf8');
    return candidate.length === this.credential.length && timingSafeEqual(candidate, this.credential);
  }

  private async handleResponse(request: WireRequest, socket?: unknown): Promise<WireResponse> {
    try {
      this.checkDeadline(request);
      if (request.interaction !== 'REQUEST_RESPONSE' && request.interaction !== 'REQUEST_CHANNEL') {
        throw new Error('interaction mismatch');
      }
      switch (request.route) {
        case ROUTES.describe:
          return success(request.requestId, this.driver.description as unknown as JsonValue, true);
        case ROUTES.create:
          return success(request.requestId, await this.create(request, socket), true);
        case ROUTES.pluginStart:
          return success(request.requestId, await this.lifecycle(request, 'running'), true);
        case ROUTES.pluginPause:
          return success(request.requestId, await this.lifecycle(request, 'paused'), true);
        case ROUTES.pluginShutdown:
          return success(request.requestId, await this.lifecycle(request, 'stopped'), true);
        case ROUTES.runtimeHealth:
          return success(request.requestId, { ready: !this.draining }, true);
        case ROUTES.runtimeDrain:
          this.beginDrain();
          return success(request.requestId, { draining: true }, true);
        default:
          throw new Error(`unsupported request route: ${request.route}`);
      }
    } catch (error) {
      return failure(request.requestId, error);
    }
  }

  private async *handleStream(request: WireRequest): AsyncIterable<{ data: Buffer }> {
    try {
      this.checkDeadline(request);
      if (request.interaction !== 'REQUEST_STREAM') throw new Error('interaction mismatch');
      const body = request.body as Record<string, JsonValue> | undefined;
      if (request.route === ROUTES.driverResource) {
        yield* this.resourceStream(request, body);
        return;
      }
      const pluginId = typeof body?.pluginId === 'string' ? body.pluginId : undefined;
      const commandId = typeof body?.commandId === 'string' ? body.commandId : '';
      const target = request.route === ROUTES.driverCommand ? this.driver : this.plugins.get(pluginId ?? '');
      if (!target) throw new Error(`plugin does not exist: ${pluginId}`);
      const result = target === this.driver
        ? await this.driver.execute(commandId, asArguments(request), this.commandContext(request))
        : await (target as Plugin).execute(commandId, asArguments(request), this.commandContext(request));
      for await (const item of toAsyncIterable(result)) {
        yield { data: this.codec.encode(success(request.requestId, asJson(item), false)) };
      }
      yield { data: this.codec.encode(success(request.requestId, undefined, true)) };
    } catch (error) {
      yield { data: this.codec.encode(failure(request.requestId, error)) };
    }
  }

  private async *resourceStream(request: WireRequest, body: Record<string, JsonValue> | undefined): AsyncIterable<{ data: Buffer }> {
    const name = typeof body?.name === 'string' ? body.name : '';
    if (!name || name.includes('..') || name.includes('\0') || name.startsWith('/') || name.startsWith('\\')) {
      throw new Error('resource name is invalid');
    }
    if (!this.driver.resource) throw new Error('resource is not supported');
    let total = 0;
    for await (const chunk of this.driver.resource(name)) {
      total += chunk.byteLength;
      if (total > this.maxResourceBytes) throw new Error('resource exceeds the configured limit');
      yield { data: this.codec.encode(success(request.requestId, {
        dataBase64: Buffer.from(chunk).toString('base64'),
        totalBytes: total
      }, false)) };
    }
    yield { data: this.codec.encode(success(request.requestId, undefined, true)) };
  }

  private async *handleChannel(payloads: any): AsyncIterable<{ data: Buffer }> {
    for await (const payload of flowableToAsyncIterable<{ data: Buffer }>(payloads)) {
      const request = this.decodeRequest(payload.data);
      if (request.route === ROUTES.driverCommand || request.route === ROUTES.pluginCommand) {
        try {
          this.checkDeadline(request);
          const body = request.body as Record<string, JsonValue> | undefined;
          const pluginId = typeof body?.pluginId === 'string' ? body.pluginId : undefined;
          const commandId = typeof body?.commandId === 'string' ? body.commandId : '';
          const target = request.route === ROUTES.driverCommand
            ? this.driver
            : this.plugins.get(pluginId ?? '');
          if (!target) throw new Error(`plugin does not exist: ${pluginId}`);
          const result = target === this.driver
            ? await this.driver.execute(commandId, asArguments(request), this.commandContext(request))
            : await (target as Plugin).execute(commandId, asArguments(request), this.commandContext(request));
          for await (const item of toAsyncIterable(result)) {
            yield { data: this.codec.encode(success(request.requestId, asJson(item), false)) };
          }
          yield { data: this.codec.encode(success(request.requestId, undefined, true)) };
        } catch (error) {
          yield { data: this.codec.encode(failure(request.requestId, error)) };
        }
      } else {
        yield { data: this.codec.encode(await this.handleResponse(request)) };
      }
    }
  }

  private async create(request: WireRequest, socket?: unknown): Promise<JsonValue> {
    if (this.draining) throw new Error('runtime is draining');
    const body = request.body as Record<string, JsonValue> | undefined;
    const pluginId = typeof body?.pluginId === 'string' ? body.pluginId : '';
    if (!pluginId || this.plugins.has(pluginId)) throw new Error('plugin id is missing or already exists');
    const plugin = await this.driver.createPlugin(pluginId, this.contextFor(socket, pluginId));
    this.plugins.set(pluginId, plugin);
    return { pluginId };
  }

  private async lifecycle(request: WireRequest, state: PluginState): Promise<JsonValue> {
    const body = request.body as Record<string, JsonValue> | undefined;
    const pluginId = typeof body?.pluginId === 'string' ? body.pluginId : '';
    const plugin = this.plugins.get(pluginId);
    if (!plugin) throw new Error(`plugin does not exist: ${pluginId}`);
    if (state === 'running') await plugin.start();
    else if (state === 'paused') await plugin.pause();
    else {
      await plugin.shutdown();
      this.plugins.delete(pluginId);
    }
    return { pluginId };
  }

  private commandContext(request: WireRequest): PluginCommandContext {
    return { metadata: request.metadata ?? {} };
  }

  private contextFor(socket: unknown, pluginId: string): PluginContext {
    if (!socket) return this.context;
    return { ...this.context, host: new ServerHostClient(socket, this.codec, this.options, pluginId) };
  }

  private decodeRequest(payload: Uint8Array): WireRequest {
    const request = this.codec.decode<WireRequest>(payload);
    this.codec.validateRequest(request);
    return request;
  }

  private checkDeadline(request: WireRequest): void {
    if (request.deadlineEpochMillis <= Date.now()) {
      throw new WireProtocolError('deadline_exceeded', 'request deadline has expired');
    }
  }
}
