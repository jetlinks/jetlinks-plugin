package org.jetlinks.plugin.protocol;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Collections;
import java.util.Map;

/**
 * Machine-readable error returned by an external plugin.
 *
 * <p>The message is diagnostic only. Callers must branch on {@link #getCode()} and may use the
 * bounded details map for structured remediation data.</p>
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public final class WireError {
    private String code;
    private String message;
    private boolean retryable;
    private Map<String, String> details = Collections.emptyMap();

    public WireError() {
    }

    public WireError(String code, String message, boolean retryable, Map<String, String> details) {
        this.code = code;
        this.message = message;
        this.retryable = retryable;
        this.details = details == null ? Collections.emptyMap() : details;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isRetryable() {
        return retryable;
    }

    public void setRetryable(boolean retryable) {
        this.retryable = retryable;
    }

    public Map<String, String> getDetails() {
        return details;
    }

    public void setDetails(Map<String, String> details) {
        this.details = details == null ? Collections.emptyMap() : details;
    }
}
