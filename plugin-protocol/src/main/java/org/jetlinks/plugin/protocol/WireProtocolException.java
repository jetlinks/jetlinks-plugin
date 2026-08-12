package org.jetlinks.plugin.protocol;

/** Protocol validation error with a stable machine-readable code. */
public class WireProtocolException extends RuntimeException {
    private final String code;

    public WireProtocolException(String code, String message) {
        super(message);
        this.code = code;
    }

    public WireProtocolException(String code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
