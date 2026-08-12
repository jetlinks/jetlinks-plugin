package org.jetlinks.plugin.protocol;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class WireCodecTest {

    private final WireCodec codec = new WireCodec(4096);

    @Test
    void shouldRoundTripRequestWithUnknownField() throws Exception {
        String json = "{\"version\":\"1.0\",\"interaction\":\"REQUEST_RESPONSE\","
            + "\"route\":\"plugin.driver.describe\",\"requestId\":\"req-1\","
            + "\"deadlineEpochMillis\":4102444800000,\"futureField\":true}";

        WireRequest request = codec.decode(json.getBytes(java.nio.charset.StandardCharsets.UTF_8), WireRequest.class);

        assertEquals("plugin.driver.describe", request.getRoute());
        assertEquals("req-1", request.getRequestId());
    }

    @Test
    void shouldRejectExpiredShapeBeforeDispatch() throws Exception {
        WireRequest request = new WireRequest(
            WireInteraction.REQUEST_RESPONSE,
            "plugin.driver.describe",
            "req-1",
            4102444800000L,
            Collections.emptyMap(),
            new ObjectMapper().createObjectNode());

        byte[] bytes = codec.encode(request);
        assertEquals("plugin.driver.describe", codec.decode(bytes, WireRequest.class).getRoute());
    }

    @Test
    void shouldRejectOversizedMetadata() throws Exception {
        StringBuilder oversized = new StringBuilder(4097);
        for (int i = 0; i < 4097; i++) {
            oversized.append('x');
        }
        Map<String, String> metadata = new HashMap<>();
        metadata.put("x", oversized.toString());
        WireRequest request = new WireRequest(
            WireInteraction.REQUEST_RESPONSE,
            "plugin.driver.describe",
            "req-1",
            4102444800000L,
            metadata,
            new ObjectMapper().createObjectNode());

        assertThrows(WireProtocolException.class, () -> codec.encode(request));

        String json = "{\"version\":\"1.0\",\"interaction\":\"REQUEST_RESPONSE\","
            + "\"route\":\"plugin.driver.describe\",\"requestId\":\"req-1\","
            + "\"deadlineEpochMillis\":4102444800000,\"metadata\":{\"x\":\""
            + oversized + "\"}}";

        assertThrows(WireProtocolException.class,
                     () -> codec.decode(json.getBytes(java.nio.charset.StandardCharsets.UTF_8), WireRequest.class));
    }

    @Test
    void shouldRejectUnknownVersion() throws Exception {
        String json = "{\"version\":\"2.0\",\"runtimeId\":\"runtime-1\","
            + "\"driverId\":\"driver-1\",\"generation\":1}";

        assertThrows(WireProtocolException.class,
                     () -> codec.decode(json.getBytes(java.nio.charset.StandardCharsets.UTF_8), SetupMessage.class));
    }
}
