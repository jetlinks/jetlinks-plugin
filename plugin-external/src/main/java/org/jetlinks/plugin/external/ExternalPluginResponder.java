package org.jetlinks.plugin.external;

import org.jetlinks.plugin.protocol.WireRequest;
import org.jetlinks.plugin.protocol.WireResponse;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Host-side responder used when an external plugin invokes a platform service over the same
 * bidirectional RSocket connection.  The SDK deliberately exposes only wire envelopes; platform
 * adapters decide which routes are allowed and how they map to local services.
 */
public interface ExternalPluginResponder {

    Mono<WireResponse> requestResponse(WireRequest request);

    Flux<WireResponse> requestStream(WireRequest request);

    Flux<WireResponse> requestChannel(Publisher<WireRequest> requests);
}
