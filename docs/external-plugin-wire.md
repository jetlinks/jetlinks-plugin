# External plugin wire contract

The first external-plugin protocol is transport-neutral JSON carried by RSocket. The same envelope
is used by Java and TypeScript implementations; platform code must not expose Java class names,
Spring types or Reactor types in the wire format.

## Interaction

Only `REQUEST_RESPONSE`, `REQUEST_STREAM` and `REQUEST_CHANNEL` are supported in version `1.0`.
RSocket demand and cancellation are authoritative for stream/channel interactions. A request also
contains an absolute UTC `deadlineEpochMillis`; receivers reject expired requests before invoking
plugin code. Side-effect commands are never automatically replayed after reconnect.

## Envelope

Requests contain `version`, `interaction`, `route`, `requestId`, `deadlineEpochMillis`, optional
bounded `metadata`, and an optional structured `body`. Responses contain `version`, `requestId`,
`success`, `complete`, and either `body` or a structured `error`. Unknown fields are ignored within
the same major version; unknown routes, interactions and capabilities fail closed.

## Lifecycle routes

`plugin.runtime.setup`, `plugin.driver.describe`, `plugin.driver.create`,
`plugin.lifecycle.start`, `plugin.lifecycle.pause`, `plugin.lifecycle.shutdown`,
`plugin.command.execute`, `driver.resource.get`, `runtime.health`, and `runtime.drain` are the
initial route registry. Route arguments remain JSON schema objects and are validated by each typed
SDK adapter before dispatch.

## Security and limits

Setup carries runtime/driver/generation identity and a credential reference. Credentials are never
written to logs, persisted plugin entities, command lines or environment variables. Implementations
must enforce negotiated frame/message limits, bounded metadata, generation fencing, and explicit
resource close on cancellation, disconnect and shutdown.
