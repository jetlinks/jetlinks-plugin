package org.jetlinks.plugin.protocol;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;

/** Response envelope. Stream responses set {@code complete=false} until their final item. */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public final class WireResponse {
    private String version = ExternalPluginProtocol.VERSION;
    private String requestId;
    private boolean success;
    private boolean complete = true;
    private JsonNode body;
    private WireError error;

    public WireResponse() {
    }

    public static WireResponse success(String requestId, JsonNode body, boolean complete) {
        WireResponse response = new WireResponse();
        response.requestId = requestId;
        response.success = true;
        response.complete = complete;
        response.body = body;
        return response;
    }

    public static WireResponse failure(String requestId, WireError error) {
        WireResponse response = new WireResponse();
        response.requestId = requestId;
        response.success = false;
        response.error = error;
        return response;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public boolean isComplete() {
        return complete;
    }

    public void setComplete(boolean complete) {
        this.complete = complete;
    }

    public JsonNode getBody() {
        return body;
    }

    public void setBody(JsonNode body) {
        this.body = body;
    }

    public WireError getError() {
        return error;
    }

    public void setError(WireError error) {
        this.error = error;
    }
}
