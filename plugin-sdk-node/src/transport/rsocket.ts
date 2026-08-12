import { BufferEncoders, IdentitySerializers, RSocketClient } from 'rsocket-core';
import { Flowable } from 'rsocket-flowable';
import RSocketTcpClient from 'rsocket-tcp-client';
import type { ExternalPluginConnection } from '../api.js';
import { DATA_MIME_TYPE, METADATA_MIME_TYPE, WireCodec, type SetupMessage, type WireRequest, type WireResponse } from '../protocol.js';

function defaultExport(value: unknown): any {
  const module = value as { default?: unknown };
  const nested = module.default as { default?: unknown } | undefined;
  return nested?.default ?? module.default ?? value;
}

export interface RSocketTcpOptions {
  readonly host: string;
  readonly port: number;
  readonly setup: SetupMessage;
  readonly codec?: WireCodec;
}

export interface RSocketUnixOptions {
  readonly path: string;
  readonly setup: SetupMessage;
  readonly codec?: WireCodec;
}

function payload(codec: WireCodec, value: unknown): { data: Buffer } {
  return { data: codec.encode(value) };
}

function response(codec: WireCodec, value: { data: Buffer | Uint8Array }): WireResponse {
  return codec.decode<WireResponse>(value.data);
}

function abortable<T>(signal: AbortSignal | undefined, operation: (resolve: (value: T) => void, reject: (error: unknown) => void) => void): Promise<T> {
  return new Promise<T>((resolve, reject) => {
    if (signal?.aborted) {
      reject(signal.reason ?? new DOMException('The operation was aborted', 'AbortError'));
      return;
    }
    const abort = () => reject(signal?.reason ?? new DOMException('The operation was aborted', 'AbortError'));
    signal?.addEventListener('abort', abort, { once: true });
    operation(resolve, reject);
  });
}

class RSocketConnection implements ExternalPluginConnection {
  public readonly transport = 'rsocket';

  public constructor(private readonly socket: any, private readonly codec: WireCodec) {
  }

  public requestResponse(request: WireRequest, signal?: AbortSignal): Promise<WireResponse> {
    return abortable(signal, (resolve, reject) => {
      this.socket.requestResponse(payload(this.codec, request)).subscribe({
        onComplete: (value: { data: Buffer | Uint8Array }) => resolve(response(this.codec, value)),
        onError: reject
      });
    });
  }

  public requestStream(request: WireRequest, signal?: AbortSignal): AsyncIterable<WireResponse> {
    return this.stream(this.socket.requestStream(payload(this.codec, request)), signal);
  }

  public requestChannel(requests: AsyncIterable<WireRequest>, signal?: AbortSignal): AsyncIterable<WireResponse> {
    // The source is demand-driven by the RSocket subscriber. No requests are replayed.
    return this.stream(this.socket.requestChannel(this.channelPayloads(requests, signal)), signal, false);
  }

  public async close(): Promise<void> {
    this.socket.close();
  }

  private async *stream(flowable: any, signal?: AbortSignal, terminalOnComplete = true): AsyncIterable<WireResponse> {
    const queue: WireResponse[] = [];
    let complete = false;
    let failure: unknown;
    let wake: (() => void) | undefined;
    const subscription = await new Promise<any>((resolve) => {
      flowable.subscribe({
        onSubscribe: (value: any) => resolve(value),
        onNext: (value: { data: Buffer | Uint8Array }) => {
          queue.push(response(this.codec, value));
          wake?.();
        },
        onComplete: () => { complete = true; wake?.(); },
        onError: (error: unknown) => { failure = error; complete = true; wake?.(); }
      });
    });
    subscription.request(1);
    try {
      while (!complete || queue.length > 0) {
        if (signal?.aborted) {
          subscription.cancel();
          throw signal.reason ?? new DOMException('The operation was aborted', 'AbortError');
        }
        if (queue.length === 0) {
          await new Promise<void>(resolve => { wake = resolve; });
          wake = undefined;
          continue;
        }
        const next = queue.shift() as WireResponse;
        if (terminalOnComplete && next.complete) {
          complete = true;
          subscription.cancel();
        } else {
          subscription.request(1);
        }
        yield next;
      }
      if (failure !== undefined) {
        throw failure;
      }
    } finally {
      subscription.cancel();
    }
  }

  private channelPayloads(requests: AsyncIterable<WireRequest>, signal?: AbortSignal): any {
    return new Flowable((subscriber: any) => {
      let cancelled = false;
      let completed = false;
      let pulling = false;
      const iterator = requests[Symbol.asyncIterator]();
      const pull = async (count: number): Promise<void> => {
        if (cancelled || completed || pulling) {
          return;
        }
        pulling = true;
        try {
          for (let index = 0; index < count && !cancelled; index += 1) {
            if (signal?.aborted) {
              throw signal.reason ?? new DOMException('The operation was aborted', 'AbortError');
            }
            const next = await iterator.next();
            if (next.done) {
              completed = true;
              subscriber.onComplete();
              break;
            }
            subscriber.onNext(payload(this.codec, next.value));
          }
        } catch (error) {
          completed = true;
          subscriber.onError(error);
        } finally {
          pulling = false;
        }
      };
      subscriber.onSubscribe({
        request: (count: number) => { void pull(count); },
        cancel: () => {
          cancelled = true;
          void iterator.return?.();
        }
      });
    });
  }
}

export async function connectRSocket(options: RSocketTcpOptions): Promise<ExternalPluginConnection> {
  const codec = options.codec ?? new WireCodec(options.setup.maxFrameBytes);
  const transport = new (defaultExport(RSocketTcpClient))({ host: options.host, port: options.port }, BufferEncoders);
  return connectWithTransport(transport, options.setup, codec);
}

/** Connects through a Unix domain socket. The socket path is never placed in the wire payload. */
export async function connectRSocketUnix(options: RSocketUnixOptions): Promise<ExternalPluginConnection> {
  const codec = options.codec ?? new WireCodec(options.setup.maxFrameBytes);
  const transport = new (defaultExport(RSocketTcpClient))({ path: options.path }, BufferEncoders);
  return connectWithTransport(transport, options.setup, codec);
}

async function connectWithTransport(transport: unknown,
                                    setup: SetupMessage,
                                    codec: WireCodec): Promise<ExternalPluginConnection> {
  const client = new RSocketClient({
    transport,
    serializers: IdentitySerializers,
    setup: {
      dataMimeType: DATA_MIME_TYPE,
      metadataMimeType: METADATA_MIME_TYPE,
      keepAlive: 30000,
      lifetime: 90000,
      payload: payload(codec, setup)
    }
  } as any);
  const socket = await new Promise<any>((resolve, reject) => {
    client.connect().subscribe({ onComplete: resolve, onError: reject });
  });
  return new RSocketConnection(socket, codec);
}
