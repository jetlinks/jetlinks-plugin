package org.jetlinks.plugin.protocol;

/**
 * RSocket interaction selected by a route. Fire-and-forget and resume are intentionally absent in
 * the first protocol version because their delivery and recovery semantics are not required by the
 * platform plugin lifecycle.
 */
public enum WireInteraction {
    REQUEST_RESPONSE,
    REQUEST_STREAM,
    REQUEST_CHANNEL
}
