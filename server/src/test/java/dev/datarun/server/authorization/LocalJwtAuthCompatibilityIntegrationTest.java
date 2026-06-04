package dev.datarun.server.authorization;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.datarun.server.AbstractIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.TestPropertySource;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

@TestPropertySource(properties = {
        "datarun.auth.mode=jwt",
        "datarun.auth.jwt.issuer=https://issuer.test/datarun",
        "datarun.auth.jwt.audience=datarun-mobile",
        "datarun.auth.jwt.hmac-secret=01234567890123456789012345678901"
})
class LocalJwtAuthCompatibilityIntegrationTest extends AbstractIntegrationTest {

    private static final String ISSUER = "https://issuer.test/datarun";
    private static final String AUDIENCE = "datarun-mobile";
    private static final String SECRET = "01234567890123456789012345678901";
    private static final UUID ACTOR = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID DEVICE = UUID.fromString("33333333-3333-3333-3333-333333333333");

    @Autowired private TestRestTemplate rest;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private PrincipalBindingManifestProvisioner provisioner;
    @LocalServerPort private int port;

    @BeforeEach
    void cleanDb() {
        jdbcTemplate.execute("DELETE FROM auth_principal_binding_operations");
        jdbcTemplate.execute("DELETE FROM auth_principal_bindings");
        jdbcTemplate.execute("DELETE FROM actor_tokens");
        jdbcTemplate.execute("DELETE FROM subject_locations");
        jdbcTemplate.execute("DELETE FROM events");
        jdbcTemplate.execute("ALTER SEQUENCE events_sync_watermark_seq RESTART WITH 1");
        jdbcTemplate.execute("DELETE FROM device_sync_state");
    }

    @Test
    void localJwtModeStillResolvesExplicitBinding() throws Exception {
        provisionBinding("local-jwt-binding", "local-principal", ACTOR);

        ResponseEntity<JsonNode> response = getAuthMe(jwt("local-principal"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().path("actor_id").asText()).isEqualTo(ACTOR.toString());
        assertThat(response.getBody().path("auth_source").asText()).isEqualTo("jwt-principal");
    }

    @Test
    void localJwtModeStillRejectsUnmappedPushBeforePersistence() throws Exception {
        Map<String, Object> event = buildEvent(UUID.randomUUID());

        HttpResponse<String> response = postPushRaw(List.of(event), jwt("unmapped-principal"));

        assertThat(response.statusCode()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
        assertThat(storedEventCount()).isZero();
    }

    private void provisionBinding(String operationId, String subject, UUID actorId) throws Exception {
        Map<String, Object> operation = new LinkedHashMap<>();
        operation.put("operation_id", operationId);
        operation.put("issuer", ISSUER);
        operation.put("subject", subject);
        operation.put("actor_id", actorId.toString());
        operation.put("state", "active");
        operation.put("reason", "local jwt compatibility binding");
        Map<String, Object> manifest = new LinkedHashMap<>();
        manifest.put("manifest_version", "local-jwt-test/v1");
        manifest.put("source", "test:local-jwt");
        manifest.put("operations", List.of(operation));
        provisioner.applyManifestJson(
                objectMapper.writeValueAsString(manifest),
                "system:local-jwt-test");
    }

    private ResponseEntity<JsonNode> getAuthMe(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        return rest.exchange("/api/auth/me", HttpMethod.GET,
                new HttpEntity<>(headers), JsonNode.class);
    }

    private HttpResponse<String> postPushRaw(List<?> events, String token) throws Exception {
        Map<String, Object> request = new LinkedHashMap<>();
        request.put("events", events);
        request.put("device_id", DEVICE.toString());
        request.put("last_pull_watermark", 0);
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + port + "/api/sync/push"))
                .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .header("Authorization", "Bearer " + token)
                .POST(HttpRequest.BodyPublishers.ofString(
                        objectMapper.writeValueAsString(request)))
                .build();
        return HttpClient.newHttpClient().send(
                httpRequest, HttpResponse.BodyHandlers.ofString());
    }

    private Map<String, Object> buildEvent(UUID subjectId) {
        Map<String, Object> event = new LinkedHashMap<>();
        event.put("id", UUID.randomUUID().toString());
        event.put("type", "capture");
        event.put("shape_ref", "basic_capture/v1");
        event.put("activity_ref", null);
        event.put("subject_ref", Map.of("type", "subject", "id", subjectId.toString()));
        event.put("actor_ref", Map.of("type", "actor", "id", ACTOR.toString()));
        event.put("device_id", DEVICE.toString());
        event.put("device_seq", 1);
        event.put("sync_watermark", null);
        event.put("timestamp", OffsetDateTime.now(ZoneOffset.UTC).toString());
        event.put("payload", Map.of(
                "name", "Local JWT auth test",
                "category", "production",
                "notes", "principal binding",
                "date", "2026-06-04",
                "value", 1));
        return event;
    }

    private int storedEventCount() {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM events", Integer.class);
        return count == null ? 0 : count;
    }

    private String jwt(String subject) throws Exception {
        Map<String, Object> header = Map.of("alg", "HS256", "typ", "JWT");
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("iss", ISSUER);
        payload.put("sub", subject);
        payload.put("aud", AUDIENCE);
        payload.put("exp", Instant.now().plusSeconds(3600).getEpochSecond());

        String signingInput = encode(objectMapper.writeValueAsBytes(header))
                + "." + encode(objectMapper.writeValueAsBytes(payload));
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(SECRET.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        return signingInput + "." + encode(mac.doFinal(signingInput.getBytes(StandardCharsets.UTF_8)));
    }

    private String encode(byte[] value) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(value);
    }
}
