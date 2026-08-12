package org.jetlinks.plugin.external.runner;

import io.rsocket.transport.netty.server.CloseableChannel;

import java.net.InetSocketAddress;

/** Bound endpoint exposed by a Java runner. */
public final class ListeningEndpoint {
    private final CloseableChannel channel;

    public ListeningEndpoint(CloseableChannel channel) {
        this.channel = channel;
    }

    public CloseableChannel channel() {
        return channel;
    }

    public InetSocketAddress address() {
        return channel.address();
    }
}
