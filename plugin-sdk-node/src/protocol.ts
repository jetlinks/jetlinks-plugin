export const DATA_MIME_TYPE = 'application/json';
export const METADATA_MIME_TYPE = 'text/plain';
export const PROTOCOL_VERSION = '1.0';
export const DEFAULT_MAX_FRAME_BYTES: number = 1024 * 1024;
export const MAX_METADATA_ENTRIES: number = 32;
export const MAX_METADATA_VALUE_BYTES: number = 4096;

export const ROUTES: {
  readonly describe: 'plugin.driver.describe';
  readonly create: 'plugin.driver.create';
  readonly driverCommand: 'driver.command.execute';
  readonly driverResource: 'driver.resource.get';
  readonly pluginStart: 'plugin.lifecycle.start';
  readonly pluginPause: 'plugin.lifecycle.pause';
  readonly pluginShutdown: 'plugin.lifecycle.shutdown';
  readonly pluginCommand: 'plugin.command.execute';
  readonly runtimeHealth: 'runtime.health';
  readonly runtimeDrain: 'runtime.drain';
  readonly hostCommand: 'host.command.execute';
  readonly hostMonitor: 'host.monitor.event';
} = {
  describe: 'plugin.driver.describe',
  create: 'plugin.driver.create',
  driverCommand: 'driver.command.execute',
  driverResource: 'driver.resource.get',
  pluginStart: 'plugin.lifecycle.start',
  pluginPause: 'plugin.lifecycle.pause',
  pluginShutdown: 'plugin.lifecycle.shutdown',
  pluginCommand: 'plugin.command.execute',
  runtimeHealth: 'runtime.health',
  runtimeDrain: 'runtime.drain',
  hostCommand: 'host.command.execute',
  hostMonitor: 'host.monitor.event'
} as const;

export type WireInteraction = 'REQUEST_RESPONSE' | 'REQUEST_STREAM' | 'REQUEST_CHANNEL';

export interface SetupMessage {
  readonly version: typeof PROTOCOL_VERSION;
  readonly runtimeId: string;
  readonly driverId: string;
  readonly generation: number;
  readonly sdkVersion: string;
  readonly credential?: string;
  readonly maxFrameBytes: number;
}

export interface WireRequest {
  readonly version: typeof PROTOCOL_VERSION;
  readonly interaction: WireInteraction;
  readonly route: string;
  readonly requestId: string;
  readonly deadlineEpochMillis: number;
  readonly metadata?: Readonly<Record<string, string>>;
  readonly body?: JsonValue;
}

export interface WireError {
  readonly code: string;
  readonly message: string;
  readonly details?: JsonValue;
}

export interface WireResponse {
  readonly version: typeof PROTOCOL_VERSION;
  readonly requestId: string;
  readonly success: boolean;
  readonly complete: boolean;
  readonly body?: JsonValue;
  readonly error?: WireError;
}

export type JsonPrimitive = string | number | boolean | null;
export type JsonValue = JsonPrimitive | JsonValue[] | { readonly [key: string]: JsonValue };

export class WireProtocolError extends Error {
  public readonly code: string;

  public constructor(code: string, message: string) {
    super(message);
    this.name = 'WireProtocolError';
    this.code = code;
  }
}

export class WireCodec {
  public constructor(public readonly maxFrameBytes: number = DEFAULT_MAX_FRAME_BYTES) {
    if (!Number.isSafeInteger(maxFrameBytes) || maxFrameBytes < 1024) {
      throw new RangeError('maxFrameBytes must be at least 1024');
    }
  }

  public encode(value: unknown): Buffer {
    let encoded: string;
    try {
      encoded = JSON.stringify(value);
    } catch (error) {
      throw new WireProtocolError('encode_failed', error instanceof Error ? error.message : 'unable to encode value');
    }
    const bytes = Buffer.byteLength(encoded, 'utf8');
    if (bytes > this.maxFrameBytes) {
      throw new WireProtocolError('frame_too_large', `encoded frame exceeds ${this.maxFrameBytes} bytes`);
    }
    return Buffer.from(encoded, 'utf8');
  }

  public decode<T>(payload: Uint8Array): T {
    if (payload.byteLength > this.maxFrameBytes) {
      throw new WireProtocolError('frame_too_large', `received frame exceeds ${this.maxFrameBytes} bytes`);
    }
    try {
      return JSON.parse(Buffer.from(payload).toString('utf8')) as T;
    } catch (error) {
      throw new WireProtocolError('decode_failed', error instanceof Error ? error.message : 'unable to decode frame');
    }
  }

  public request(input: Omit<WireRequest, 'version'>): WireRequest {
    if (!input.route || !input.requestId || !Number.isSafeInteger(input.deadlineEpochMillis)) {
      throw new WireProtocolError('request_invalid', 'route, requestId and deadlineEpochMillis are required');
    }
    this.validateRequest(input);
    return { ...input, version: PROTOCOL_VERSION };
  }

  public validateRequest(input: Omit<WireRequest, 'version'> | WireRequest): void {
    const metadata = input.metadata ?? {};
    const entries = Object.entries(metadata);
    if (entries.length > MAX_METADATA_ENTRIES) {
      throw new WireProtocolError('metadata_too_large', 'metadata entry count exceeds the protocol limit');
    }
    for (const [key, value] of entries) {
      if (Buffer.byteLength(key, 'utf8') > MAX_METADATA_VALUE_BYTES
        || typeof value !== 'string'
        || Buffer.byteLength(value, 'utf8') > MAX_METADATA_VALUE_BYTES) {
        throw new WireProtocolError('metadata_too_large', 'metadata key or value exceeds the protocol limit');
      }
    }
  }
}

export function assertResponse(response: WireResponse): WireResponse {
  if (!response.success) {
    const error = response.error ?? { code: 'remote_error', message: 'remote plugin request failed' };
    throw new WireProtocolError(error.code, error.message);
  }
  return response;
}
