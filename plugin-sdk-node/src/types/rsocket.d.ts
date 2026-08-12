declare module 'rsocket-core' {
  export const IdentitySerializers: unknown;
  export const BufferEncoders: unknown;
  export class RSocketClient {
    public constructor(options: unknown);
    public connect(): { subscribe(observer: { onComplete(value: unknown): void; onError(error: unknown): void }): void };
  }
  export class RSocketServer {
    public constructor(options: unknown);
    public start(): void;
    public stop(): void;
  }
}

declare module 'rsocket-tcp-client' {
  export default class RSocketTcpClient {
    public constructor(options: { host?: string; port?: number; path?: string });
  }
}

declare module 'rsocket-flowable' {
  export const Flowable: {
    new <T = unknown>(source: (subscriber: unknown) => void, max?: number): unknown;
  };
  export default class Flowable<T = unknown> {
    public constructor(source: (subscriber: unknown) => void, max?: number);
    public subscribe(subscriber: unknown): void;
  }
}

declare module 'rsocket-tcp-server' {
  export default class RSocketTcpServer {
    public constructor(options: { host: string; port: number; serverFactory?: (onConnection: (socket: unknown) => void) => unknown });
    public start(): unknown;
    public stop(): void;
  }
}
