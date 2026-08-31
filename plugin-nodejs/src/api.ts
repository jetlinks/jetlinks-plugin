/** JSON-compatible value used at the platform/plugin boundary. */
export type JsonPrimitive = string | number | boolean | null;
export type JsonValue = JsonPrimitive | JsonValue[] | { readonly [key: string]: JsonValue };
export type JsonObject = { readonly [key: string]: JsonValue };

export type MaybePromise<T> = T | PromiseLike<T>;

export interface PluginManifest {
  readonly id: string;
  readonly name: string;
  readonly type: string;
  readonly description?: string;
  readonly version?: string;
  readonly requires?: PluginRequirements;
  readonly others?: Readonly<Record<string, JsonValue>>;
}

export interface PluginRequirements {
  /** Platform command services and the command ids this plugin is allowed to call. */
  readonly services?: Readonly<Record<string, readonly string[]>>;
  readonly capabilities?: readonly string[];
}

export type CallErrorCode =
  | 'service_not_declared'
  | 'command_not_declared'
  | 'service_not_allowed'
  | 'command_not_allowed'
  | 'service_unavailable'
  | 'invalid_arguments'
  | 'timeout'
  | 'cancelled'
  | 'remote_error'
  | 'stale_generation'
  | 'target_not_allowed'
  | 'host_command_failed';

export class HostServiceError extends Error {
  public readonly code: CallErrorCode;
  public readonly serviceId: string;
  public readonly commandId: string;
  public readonly retryable: boolean;
  public readonly details?: JsonValue;

  public constructor(options: {
    readonly code: CallErrorCode;
    readonly serviceId: string;
    readonly commandId?: string;
    readonly message?: string;
    readonly retryable?: boolean;
    readonly details?: JsonValue;
  }) {
    super(options.message ?? `${options.code}: ${options.serviceId}`);
    this.name = 'HostServiceError';
    this.code = options.code;
    this.serviceId = options.serviceId;
    this.commandId = options.commandId ?? '';
    this.retryable = options.retryable ?? false;
    if (options.details !== undefined) this.details = options.details;
  }
}

export interface CallOptions {
  readonly signal?: AbortSignal;
  readonly timeoutMs?: number;
  readonly metadata?: Readonly<Record<string, string>>;
}

export interface ServiceReferenceOptions {
  /** Dynamic resource scope. It is sent separately from serviceId and is authorization input. */
  readonly target?: JsonObject;
  readonly metadata?: Readonly<Record<string, string>>;
}

export interface DescribeOptions {
  readonly signal?: AbortSignal;
  readonly timeoutMs?: number;
}

export interface ServiceCommandDescription {
  readonly id: string;
  readonly mode: 'unary' | 'stream';
  readonly name?: string;
  readonly description?: string;
  readonly input?: JsonValue;
  readonly output?: JsonValue;
}

export interface ServiceDescription {
  readonly id: string;
  readonly name?: string;
  readonly description?: string;
  readonly version?: string;
  readonly commands: readonly ServiceCommandDescription[];
}

export interface ServiceCallRequest {
  readonly serviceId: string;
  readonly commandId: string;
  readonly arguments?: JsonValue;
  readonly reference?: ServiceReferenceOptions;
  readonly options?: CallOptions;
}

/**
 * Transport-neutral platform service bridge. RSocket and wire envelopes are implemented by the
 * platform runner and must not be implemented by plugin business code.
 */
export interface ServiceTransport {
  call(request: ServiceCallRequest): Promise<JsonValue>;
  stream(request: ServiceCallRequest): AsyncIterable<JsonValue>;
  describe(serviceId: string, options?: DescribeOptions): Promise<ServiceDescription>;
}

export interface ServiceCommand<I = JsonValue, O = JsonValue> {
  (arguments_?: I, options?: CallOptions): Promise<O>;
  call(arguments_?: I, options?: CallOptions): Promise<O>;
  stream(arguments_?: I, options?: CallOptions): AsyncIterable<O>;
}

export interface ServiceClient {
  readonly serviceId: string;
  readonly target?: JsonObject;
  call<O = JsonValue>(
    commandId: string,
    arguments_?: JsonValue,
    options?: CallOptions
  ): Promise<O>;
  stream<O = JsonValue>(
    commandId: string,
    arguments_?: JsonValue,
    options?: CallOptions
  ): AsyncIterable<O>;
  describe(options?: DescribeOptions): Promise<ServiceDescription>;
}

export type ServiceProxy<TContract = Record<string, ServiceCommand>> = ServiceClient & TContract;

export interface Monitor {
  event(name: string, payload?: JsonValue): MaybePromise<void>;
  error(name: string, error: unknown, payload?: JsonValue): MaybePromise<void>;
}

export interface Logger {
  debug(message: string, attributes?: Readonly<Record<string, JsonValue>>): void;
  info(message: string, attributes?: Readonly<Record<string, JsonValue>>): void;
  warn(message: string, attributes?: Readonly<Record<string, JsonValue>>): void;
  error(message: string, attributes?: Readonly<Record<string, JsonValue>>): void;
}

export interface PluginContext {
  readonly pluginId: string;
  readonly config: JsonValue;
  readonly signal: AbortSignal;
  service<TContract = Record<string, ServiceCommand>>(
    serviceId: string,
    options?: ServiceReferenceOptions
  ): ServiceProxy<TContract>;
  readonly monitor: Monitor;
  readonly logger: Logger;
}

export interface CommandContext extends PluginContext {
  readonly commandId: string;
}

export type UnaryCommandHandler<I = JsonValue, O = JsonValue> = (
  arguments_: I,
  context: CommandContext
) => MaybePromise<O>;

export type StreamCommandHandler<I = JsonValue, O = JsonValue> = (
  arguments_: I,
  context: CommandContext
) => AsyncIterable<O> | PromiseLike<AsyncIterable<O>>;

export interface CommandMetadata {
  readonly id?: string;
  readonly name?: string;
  readonly description?: string;
  readonly input?: JsonValue;
  readonly output?: JsonValue;
}

export interface UnaryCommandDefinition<I = JsonValue, O = JsonValue> {
  readonly mode: 'unary';
  readonly metadata: CommandMetadata;
  readonly handler: UnaryCommandHandler<I, O>;
}

export interface StreamCommandDefinition<I = JsonValue, O = JsonValue> {
  readonly mode: 'stream';
  readonly metadata: CommandMetadata;
  readonly handler: StreamCommandHandler<I, O>;
}

export type CommandDefinition = UnaryCommandDefinition | StreamCommandDefinition;
export type CommandEntry = CommandDefinition | UnaryCommandHandler;
export type CommandMap = Readonly<Record<string, CommandEntry>>;

export interface PluginLifecycle {
  start?(context: PluginContext): MaybePromise<void>;
  pause?(context: PluginContext): MaybePromise<void>;
  shutdown?(context: PluginContext): MaybePromise<void>;
}

export interface PluginImplementation {
  readonly commands?: CommandMap;
  readonly lifecycle?: PluginLifecycle;
}

export interface PluginDefinition extends PluginImplementation {
  readonly manifest: PluginManifest;
  create?(context: PluginContext): MaybePromise<PluginImplementation | void>;
}

/** Declares a unary command while keeping metadata separate from its business handler. */
export function command<I = JsonValue, O = JsonValue>(
  handler: UnaryCommandHandler<I, O>,
  metadata: CommandMetadata = {}
): UnaryCommandDefinition<I, O> {
  return { mode: 'unary', metadata, handler };
}

/** Declares a streaming command without exposing RSocket or reactive types. */
export function stream<I = JsonValue, O = JsonValue>(
  handler: StreamCommandHandler<I, O>,
  metadata: CommandMetadata = {}
): StreamCommandDefinition<I, O> {
  return { mode: 'stream', metadata, handler };
}

/**
 * Keeps plugin declarations as plain data so the platform runner can load them from any module
 * system. Validation is intentionally limited to stable boundary invariants.
 */
export function definePlugin(definition: PluginDefinition): PluginDefinition {
  validateManifest(definition.manifest);
  validateCommands(definition.commands, 'commands');
  return definition;
}

export interface PluginContextOptions {
  readonly pluginId: string;
  readonly config?: JsonValue;
  readonly signal?: AbortSignal;
  readonly transport: ServiceTransport;
  readonly requirements?: PluginRequirements;
  readonly monitor?: Monitor;
  readonly logger?: Logger;
}

/** Creates the transport-neutral context used by a platform runner and by local contract tests. */
export function createPluginContext(options: PluginContextOptions): PluginContext {
  const requirements = normalizeRequirements(options.requirements);
  const signal = options.signal ?? new AbortController().signal;
  const monitor = options.monitor ?? NOOP_MONITOR;
  const logger = options.logger ?? NOOP_LOGGER;
  const services = new Map<string, ServiceProxy>();

  return {
    pluginId: requireText(options.pluginId, 'pluginId'),
    config: options.config ?? null,
    signal,
    monitor,
    logger,
    service<TContract = Record<string, ServiceCommand>>(
      serviceId: string,
      reference: ServiceReferenceOptions = {}
    ): ServiceProxy<TContract> {
      const id = requireText(serviceId, 'serviceId');
      const declared = requirements.services?.[id];
      if (!declared) {
        throw serviceError('service_not_declared', id, '', 'service is not declared in plugin manifest');
      }
      const target = reference.target;
      const key = `${id}\u0000${JSON.stringify(target ?? null)}\u0000${JSON.stringify(reference.metadata ?? {})}`;
      const existing = services.get(key);
      if (existing) return existing as ServiceProxy<TContract>;
      const client = createServiceProxy(options.transport, id, reference, new Set(declared));
      services.set(key, client);
      return client as ServiceProxy<TContract>;
    }
  };
}

/** Creates a dynamic command proxy; explicit call/stream remain available as a no-Proxy fallback. */
export function createServiceProxy<TContract = Record<string, ServiceCommand>>(
  transport: ServiceTransport,
  serviceId: string,
  reference: ServiceReferenceOptions = {},
  allowedCommands?: ReadonlySet<string>
): ServiceProxy<TContract> {
  const id = requireText(serviceId, 'serviceId');
  const target = reference.target;
  const commandCache = new Map<string, ServiceCommand>();
  const client: ServiceClient = {
    serviceId: id,
    ...(target === undefined ? {} : {target}),
    call<O = JsonValue>(commandId: string, arguments_?: JsonValue, options?: CallOptions): Promise<O> {
      return invokeUnary(transport, id, commandId, arguments_, reference, options, allowedCommands) as Promise<O>;
    },
    stream<O = JsonValue>(commandId: string, arguments_?: JsonValue, options?: CallOptions): AsyncIterable<O> {
      return invokeStream(transport, id, commandId, arguments_, reference, options, allowedCommands) as AsyncIterable<O>;
    },
    describe(options?: DescribeOptions): Promise<ServiceDescription> {
      return transport.describe(id, options);
    }
  };

  return new Proxy(client as ServiceProxy<TContract>, {
    get(targetObject, property, receiver) {
      if (typeof property !== 'string' || property in targetObject) {
        return Reflect.get(targetObject, property, receiver);
      }
      let command = commandCache.get(property);
      if (!command) {
        const invoke = ((arguments_: JsonValue = {}, options?: CallOptions) =>
          targetObject.call(property, arguments_, options)) as ServiceCommand;
        invoke.call = invoke;
        invoke.stream = (arguments_: JsonValue = {}, options?: CallOptions) =>
          targetObject.stream(property, arguments_, options);
        command = invoke;
        commandCache.set(property, command);
      }
      return command;
    }
  });
}

async function invokeUnary(
  transport: ServiceTransport,
  serviceId: string,
  commandId: string,
  arguments_: JsonValue | undefined,
  reference: ServiceReferenceOptions,
  options: CallOptions | undefined,
  allowedCommands: ReadonlySet<string> | undefined
): Promise<JsonValue> {
  const command = requireText(commandId, 'commandId');
  assertAllowed(serviceId, command, allowedCommands);
  return withAbortTimeout(
    transport.call(createCallRequest(serviceId, command, arguments_, reference, options)),
    serviceId,
    command,
    options
  );
}

function invokeStream(
  transport: ServiceTransport,
  serviceId: string,
  commandId: string,
  arguments_: JsonValue | undefined,
  reference: ServiceReferenceOptions,
  options: CallOptions | undefined,
  allowedCommands: ReadonlySet<string> | undefined
): AsyncIterable<JsonValue> {
  const command = requireText(commandId, 'commandId');
  assertAllowed(serviceId, command, allowedCommands);
  return abortableStream(transport.stream(createCallRequest(serviceId, command, arguments_, reference, options)),
                         serviceId,
                         command,
                         options?.signal);
}

function createCallRequest(
  serviceId: string,
  commandId: string,
  arguments_: JsonValue | undefined,
  reference: ServiceReferenceOptions,
  options: CallOptions | undefined
): ServiceCallRequest {
  return {
    serviceId,
    commandId,
    ...(arguments_ === undefined ? {} : {arguments: arguments_}),
    ...(Object.keys(reference).length === 0 ? {} : {reference}),
    ...(options === undefined ? {} : {options})
  };
}

async function withAbortTimeout(
  promise: Promise<JsonValue>,
  serviceId: string,
  commandId: string,
  options?: CallOptions
): Promise<JsonValue> {
  const signal = options?.signal;
  if (signal?.aborted) throw serviceError('cancelled', serviceId, commandId, 'service call was cancelled');
  let timer: ReturnType<typeof setTimeout> | undefined;
  let abort: (() => void) | undefined;
  try {
    const guarded = new Promise<JsonValue>((resolve, reject) => {
      abort = () => reject(serviceError('cancelled', serviceId, commandId, 'service call was cancelled'));
      signal?.addEventListener('abort', abort, {once: true});
      if (options?.timeoutMs !== undefined) {
        if (!Number.isFinite(options.timeoutMs) || options.timeoutMs <= 0) {
          reject(serviceError('timeout', serviceId, commandId, 'timeoutMs must be positive'));
          return;
        }
        timer = setTimeout(() => reject(serviceError('timeout', serviceId, commandId, 'service call timed out')),
                           options.timeoutMs);
      }
      promise.then(resolve, reject);
    });
    return await guarded;
  } catch (error) {
    if (error instanceof HostServiceError) throw error;
    throw serviceError('remote_error', serviceId, commandId, error instanceof Error ? error.message : String(error), true);
  } finally {
    if (timer) clearTimeout(timer);
    if (abort) signal?.removeEventListener('abort', abort);
  }
}

async function* abortableStream(
  source: AsyncIterable<JsonValue>,
  serviceId: string,
  commandId: string,
  signal?: AbortSignal
): AsyncIterable<JsonValue> {
  if (signal?.aborted) throw serviceError('cancelled', serviceId, commandId, 'service stream was cancelled');
  const iterator = source[Symbol.asyncIterator]();
  const abort = () => iterator.return?.();
  signal?.addEventListener('abort', abort, {once: true});
  try {
    while (true) {
      if (signal?.aborted) throw serviceError('cancelled', serviceId, commandId, 'service stream was cancelled');
      const next = await iterator.next();
      if (next.done) return;
      yield next.value;
    }
  } catch (error) {
    if (error instanceof HostServiceError) throw error;
    throw serviceError('remote_error', serviceId, commandId, error instanceof Error ? error.message : String(error), true);
  } finally {
    signal?.removeEventListener('abort', abort);
    await iterator.return?.();
  }
}

function assertAllowed(serviceId: string, commandId: string, allowedCommands?: ReadonlySet<string>): void {
  if (allowedCommands && !allowedCommands.has(commandId)) {
    throw serviceError('command_not_declared', serviceId, commandId, 'command is not declared in plugin manifest');
  }
}

function serviceError(code: CallErrorCode, serviceId: string, commandId: string, message: string, retryable = false): HostServiceError {
  return new HostServiceError({code, serviceId, commandId, message, retryable});
}

function validateManifest(manifest: PluginManifest): void {
  requireText(manifest.id, 'manifest.id');
  requireText(manifest.name, 'manifest.name');
  requireText(manifest.type, 'manifest.type');
  normalizeRequirements(manifest.requires);
}

function validateCommands(commands: CommandMap | undefined, path: string): void {
  if (!commands) return;
  for (const [id, entry] of Object.entries(commands)) {
    requireText(id, `${path} command id`);
    if (typeof entry !== 'function' && (!entry || !entry.handler || !entry.mode)) {
      throw new TypeError(`${path}.${id} must be a function or command definition`);
    }
  }
}

function normalizeRequirements(requirements?: PluginRequirements): PluginRequirements {
  const services = requirements?.services;
  if (!services) return {services: {}};
  const normalized: Record<string, readonly string[]> = {};
  for (const [serviceId, commands] of Object.entries(services)) {
    requireText(serviceId, 'requires.services service id');
    if (!Array.isArray(commands) || commands.some(commandId => typeof commandId !== 'string' || !commandId.trim())) {
      throw new TypeError(`requires.services.${serviceId} must be a non-empty command id array`);
    }
    normalized[serviceId] = [...new Set(commands)];
  }
  return {...requirements, services: normalized};
}

function requireText(value: string, name: string): string {
  if (typeof value !== 'string' || !value.trim() || value.includes('\u0000')) {
    throw new TypeError(`${name} must not be blank`);
  }
  return value;
}

const NOOP_MONITOR: Monitor = {event() {}, error() {}};
const NOOP_LOGGER: Logger = {debug() {}, info() {}, warn() {}, error() {}};

export type PluginProfileName = 'standalone' | 'device' | 'collector';

export interface PluginProfile {
  readonly name: PluginProfileName;
  readonly capabilities: readonly string[];
}

export const profiles: Readonly<Record<PluginProfileName, PluginProfile>> = {
  standalone: {name: 'standalone', capabilities: ['plugin.lifecycle', 'plugin.command']},
  device: {name: 'device', capabilities: ['plugin.lifecycle', 'plugin.command', 'device.gateway']},
  collector: {name: 'collector', capabilities: ['plugin.lifecycle', 'plugin.command', 'collector']}
};
