package org.jetlinks.plugin.protocol;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.Collections;
import java.util.Map;

/**
 * Request envelope used by request-response, request-stream and request-channel interactions.
 *
 * <p>{@code deadlineEpochMillis} is an absolute UTC deadline. A receiver must reject an expired
 * request before invoking plugin code; this prevents a delayed command from becoming a new side
 * effect after a connection has been replaced.</p>
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public final class WireRequest {
    private String version = ExternalPluginProtocol.VERSION;
    private WireInteraction interaction;
    private String route;
    private String requestId;
    private long deadlineEpochMillis;
    private Map<String, String> metadata = Collections.emptyMap();
    private JsonNode body;

    public WireRequest() {
    }

    public WireRequest(WireInteraction interaction,
                       String route,
                       String requestId,
                       long deadlineEpochMillis,
                       Map<String, String> metadata,
                       JsonNode body) {
        this.interaction = interaction;
        this.route = route;
        this.requestId = requestId;
        this.deadlineEpochMillis = deadlineEpochMillis;
        this.metadata = metadata == null ? Collections.emptyMap() : metadata;
        this.body = body;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public WireInteraction getInteraction() {
        return interaction;
    }

    public void setInteraction(WireInteraction interaction) {
        this.interaction = interaction;
    }

    public String getRoute() {
        return route;
    }

    public void setRoute(String route) {
        this.route = route;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public long getDeadlineEpochMillis() {
        return deadlineEpochMillis;
    }

    public void setDeadlineEpochMillis(long deadlineEpochMillis) {
        this.deadlineEpochMillis = deadlineEpochMillis;
    }

    public Map<String, String> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, String> metadata) {
        this.metadata = metadata == null ? Collections.emptyMap() : metadata;
    }

    public JsonNode getBody() {
        return body;
    }

    public void setBody(JsonNode body) {
        this.body = body;
    }
}
