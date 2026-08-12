package org.jetlinks.plugin.protocol;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * JSON codec for the external plugin wire envelopes.
 *
 * <p>Unknown fields are ignored for minor-version forward compatibility, while required fields,
 * size limits and metadata limits are validated before a message reaches plugin code. The codec is
 * stateless and safe to share between connections.</p>
 */
public final class WireCodec {
    private final ObjectMapper mapper;
    private final int maxMessageBytes;

    public WireCodec() {
        this(ExternalPluginProtocol.DEFAULT_MAX_FRAME_BYTES);
    }

    public WireCodec(int maxMessageBytes) {
        if (maxMessageBytes < 1024) {
            throw new IllegalArgumentException("maxMessageBytes must be at least 1024");
        }
        this.maxMessageBytes = maxMessageBytes;
        this.mapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true);
    }

    /**
     * Returns the maximum encoded message size enforced by this codec.
     */
    public int maxMessageBytes() {
        return maxMessageBytes;
    }

    public byte[] encode(Object value) {
        try {
            validate(value);
            byte[] bytes = mapper.writeValueAsBytes(value);
            checkSize(bytes.length);
            return bytes;
        } catch (JsonProcessingException e) {
            throw new WireProtocolException("encode_failed", "unable to encode wire message", e);
        }
    }

    public <T> T decode(byte[] bytes, Class<T> type) {
        if (bytes == null) {
            throw new WireProtocolException("message_missing", "wire message is missing");
        }
        checkSize(bytes.length);
        try {
            T value = mapper.readValue(bytes, type);
            validate(value);
            return value;
        } catch (WireProtocolException e) {
            throw e;
        } catch (Exception e) {
            throw new WireProtocolException("decode_failed", "unable to decode wire message", e);
        }
    }

    public JsonNode readTree(byte[] bytes) {
        checkSize(bytes == null ? 0 : bytes.length);
        try {
            return mapper.readTree(bytes);
        } catch (Exception e) {
            throw new WireProtocolException("decode_failed", "unable to decode wire message", e);
        }
    }

    public JsonNode valueToTree(Object value) {
        try {
            return mapper.valueToTree(value);
        } catch (IllegalArgumentException e) {
            throw new WireProtocolException("encode_failed", "unable to encode wire body", e);
        }
    }

    public <T> T treeToValue(JsonNode node, Class<T> type) {
        try {
            return mapper.treeToValue(node, type);
        } catch (Exception e) {
            throw new WireProtocolException("body_invalid", "unable to decode wire body", e);
        }
    }

    private void validate(Object value) {
        if (value instanceof WireRequest) {
            WireRequest request = (WireRequest) value;
            requireVersion(request.getVersion());
            requireText(request.getRoute(), "route");
            requireText(request.getRequestId(), "requestId");
            if (request.getInteraction() == null) {
                throw new WireProtocolException("interaction_missing", "interaction is required");
            }
            validateMetadata(request.getMetadata());
            if (request.getDeadlineEpochMillis() <= 0) {
                throw new WireProtocolException("deadline_invalid", "deadlineEpochMillis must be positive");
            }
        } else if (value instanceof WireResponse) {
            WireResponse response = (WireResponse) value;
            requireVersion(response.getVersion());
            requireText(response.getRequestId(), "requestId");
            if (!response.isSuccess() && response.getError() == null) {
                throw new WireProtocolException("error_missing", "error is required for failed response");
            }
        } else if (value instanceof SetupMessage) {
            SetupMessage setup = (SetupMessage) value;
            requireVersion(setup.getVersion());
            requireText(setup.getRuntimeId(), "runtimeId");
            requireText(setup.getDriverId(), "driverId");
            if (setup.getGeneration() < 0) {
                throw new WireProtocolException("generation_invalid", "generation must not be negative");
            }
            if (setup.getMaxFrameBytes() < 1024 || setup.getMaxFrameBytes() > maxMessageBytes) {
                throw new WireProtocolException("frame_size_invalid", "maxFrameBytes is outside the negotiated range");
            }
        }
    }

    private void validateMetadata(Map<String, String> metadata) {
        if (metadata == null || metadata.size() > ExternalPluginProtocol.MAX_METADATA_ENTRIES) {
            throw new WireProtocolException("metadata_too_large", "metadata entry count exceeds the protocol limit");
        }
        metadata.forEach((key, value) -> {
            if (key == null || key.isEmpty() || value == null
                || value.getBytes(StandardCharsets.UTF_8).length > ExternalPluginProtocol.MAX_METADATA_VALUE_BYTES) {
                throw new WireProtocolException("metadata_invalid", "metadata contains an invalid value");
            }
        });
    }

    private void requireVersion(String version) {
        if (!ExternalPluginProtocol.VERSION.equals(version)) {
            throw new WireProtocolException("version_unsupported", "unsupported protocol version: " + version);
        }
    }

    private void requireText(String value, String field) {
        if (value == null || value.trim().isEmpty()) {
            throw new WireProtocolException(field + "_missing", field + " is required");
        }
    }

    private void checkSize(int size) {
        if (size == 0 || size > maxMessageBytes) {
            throw new WireProtocolException("message_too_large", "wire message size is outside the configured limit");
        }
    }
}
