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

/**
 * NW-037 / FP-011 gates for principal-to-actor mapping and group/claim
 * non-authority. These tests use local HS256 JWTs to prove server-side
 * semantics without requiring a live Keycloak/JWKS deployment.
 */
@TestPropertySource(properties = {
        "datarun.auth.mode=jwt",
        "datarun.auth.jwt.issuer=https://issuer.test/datarun",
        "datarun.auth.jwt.audience=datarun-mobile",
        "datarun.auth.jwt.hmac-secret=01234567890123456789012345678901"
})
class ProductionAuthIntegrationTest extends AbstractIntegrationTest {

    private static final String ISSUER = "https://issuer.test/datarun";
    private static final String AUDIENCE = "datarun-mobile";
    private static final String SECRET = "01234567890123456789012345678901";

    private static final UUID ACTOR = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID OTHER_ACTOR = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final UUID DEVICE = UUID.fromString("33333333-3333-3333-3333-333333333333");
    private static final UUID OTHER_DEVICE = UUID.fromString("44444444-4444-4444-4444-444444444444");

    @Autowired private TestRestTemplate rest;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private AuthPrincipalBindingRepository bindingRepository;
    @LocalServerPort private int port;

    @BeforeEach
    void cleanDb() {
        jdbcTemplate.execute("DELETE FROM auth_principal_bindings");
        jdbcTemplate.execute("DELETE FROM actor_tokens");
        jdbcTemplate.execute("DELETE FROM subject_locations");
        jdbcTemplate.execute("DELETE FROM events");
        jdbcTemplate.execute("ALTER SEQUENCE events_sync_watermark_seq RESTART WITH 1");
        jdbcTemplate.execute("DELETE FROM device_sync_state");
    }

    @Test
    void authMeResolvesExplicitPrincipalBindingIgnoringGroupsAndClaims() throws Exception {
        bindingRepository.bind(ISSUER, "principal-admin", ACTOR);

        ResponseEntity<JsonNode> response = getAuthMe(jwt("principal-admin", adminClaims()));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().path("actor_id").asText()).isEqualTo(ACTOR.toString());
        assertThat(response.getBody().path("auth_source").asText()).isEqualTo("jwt-principal");
    }

    @Test
    void groupClaimsDoNotGrantPullOrAssignmentAuthority() throws Exception {
        bindingRepository.bind(ISSUER, "principal-no-assignment", ACTOR);
        insertCaptureEvent(UUID.randomUUID(), OTHER_ACTOR, OTHER_DEVICE, 1);
        String token = jwt("principal-no-assignment", adminClaims());

        ResponseEntity<JsonNode> pull = postJson("/api/sync/pull",
                Map.of("since_watermark", 0, "limit", 100), token);
        assertThat(pull.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(pull.getBody().path("events").size()).isZero();

        ResponseEntity<JsonNode> subjectHistory = postJson("/api/sync/subject-history",
                Map.of(
                        "subject_id", UUID.randomUUID().toString(),
                        "activity_ref", "case_review",
                        "cursor", 0,
                        "limit", 10),
                token);
        assertThat(subjectHistory.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(subjectHistory.getBody().path("error").asText())
                .isEqualTo("subject_history_not_authorized");

        ResponseEntity<JsonNode> assignment = postJson("/api/assignments",
                Map.of(
                        "target_actor_id", OTHER_ACTOR.toString(),
                        "role", "admin",
                        "valid_from", OffsetDateTime.now(ZoneOffset.UTC).minusDays(1).toString()),
                token);
        assertThat(assignment.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(assignment.getBody().path("error").asText())
                .contains("actor has no active assignments");
        assertThat(assignmentCreatedCount()).isZero();
    }

    @Test
    void productionPushRequiresMappedPrincipalAndPersistsNothingOnAuthFailures() throws Exception {
        Map<String, Object> event = buildEvent(UUID.randomUUID(), ACTOR, DEVICE, 1);
        String eventId = event.get("id").toString();

        HttpResponse<String> missing = postPushRaw(List.of(event), null);
        assertThat(missing.statusCode()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
        assertThat(storedEventCount(eventId)).isZero();

        HttpResponse<String> invalid = postPushRaw(List.of(event), "not-a-jwt");
        assertThat(invalid.statusCode()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
        assertThat(storedEventCount(eventId)).isZero();

        HttpResponse<String> unmapped =
                postPushRaw(List.of(event), jwt("unmapped-principal", adminClaims()));
        assertThat(unmapped.statusCode()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
        assertThat(storedEventCount(eventId)).isZero();
    }

    @Test
    void productionPushEnforcesActorRefBindingAndRejectsClientSystemAuthorship() throws Exception {
        bindingRepository.bind(ISSUER, "principal-worker", ACTOR);
        String token = jwt("principal-worker", Map.of());

        ResponseEntity<JsonNode> mismatch = postPush(List.of(
                buildEvent(UUID.randomUUID(), OTHER_ACTOR, DEVICE, 1)), token);
        assertThat(mismatch.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(mismatch.getBody().path("error").asText()).isEqualTo("actor_binding_failed");
        assertThat(mismatch.getBody().path("details").get(0).path("error").asText())
                .isEqualTo("actor_mismatch");
        assertThat(storedEventCount()).isZero();

        ResponseEntity<JsonNode> system = postPush(List.of(
                buildEvent(UUID.randomUUID(), "system:forged/client", DEVICE, 1)), token);
        assertThat(system.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(system.getBody().path("details").get(0).path("error").asText())
                .isEqualTo("client_system_actor");
        assertThat(storedEventCount()).isZero();

        ResponseEntity<JsonNode> accepted = postPush(List.of(
                buildEvent(UUID.randomUUID(), ACTOR, DEVICE, 1)), token);
        assertThat(accepted.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(accepted.getBody().path("accepted").asInt()).isEqualTo(1);
        assertThat(storedEventCount()).isEqualTo(1);
    }

    @Test
    void groupClaimsDoNotGrantCanonicalConflictResolution() throws Exception {
        bindingRepository.bind(ISSUER, "principal-admin-claim", ACTOR);
        String token = jwt("principal-admin-claim", adminClaims());
        UUID subjectId = UUID.randomUUID();
        UUID sourceEventId = insertCaptureEvent(subjectId, OTHER_ACTOR, OTHER_DEVICE, 1);
        UUID flagId = insertFlagWithResolver(sourceEventId, subjectId, OTHER_ACTOR);

        ResponseEntity<JsonNode> response = postJson(
                "/api/conflicts/" + flagId + "/resolve",
                Map.of("resolution", "accepted", "actor_id", OTHER_ACTOR.toString()),
                token);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(canonicalResolutionCount(flagId)).isZero();
        assertThat(unauthorizedResolutionFlagCount()).isEqualTo(1);
    }

    private ResponseEntity<JsonNode> getAuthMe(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        return rest.exchange("/api/auth/me", HttpMethod.GET,
                new HttpEntity<>(headers), JsonNode.class);
    }

    private ResponseEntity<JsonNode> postPush(List<?> events, String token) {
        Map<String, Object> request = new LinkedHashMap<>();
        request.put("events", events);
        request.put("device_id", DEVICE.toString());
        request.put("last_pull_watermark", 0);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (token != null) {
            headers.setBearerAuth(token);
        }
        return rest.exchange("/api/sync/push", HttpMethod.POST,
                new HttpEntity<>(request, headers), JsonNode.class);
    }

    private HttpResponse<String> postPushRaw(List<?> events, String token) throws Exception {
        Map<String, Object> request = new LinkedHashMap<>();
        request.put("events", events);
        request.put("device_id", DEVICE.toString());
        request.put("last_pull_watermark", 0);
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + port + "/api/sync/push"))
                .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .POST(HttpRequest.BodyPublishers.ofString(
                        objectMapper.writeValueAsString(request)));
        if (token != null) {
            builder.header("Authorization", "Bearer " + token);
        }
        HttpRequest httpRequest = builder.build();
        return HttpClient.newHttpClient().send(
                httpRequest, HttpResponse.BodyHandlers.ofString());
    }

    private ResponseEntity<JsonNode> postJson(String path, Map<String, Object> request, String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token);
        return rest.exchange(path, HttpMethod.POST,
                new HttpEntity<>(request, headers), JsonNode.class);
    }

    private Map<String, Object> buildEvent(UUID subjectId, UUID actorId, UUID deviceId, int seq) {
        return buildEvent(subjectId, actorId.toString(), deviceId, seq);
    }

    private Map<String, Object> buildEvent(UUID subjectId, String actorId, UUID deviceId, int seq) {
        Map<String, Object> event = new LinkedHashMap<>();
        event.put("id", UUID.randomUUID().toString());
        event.put("type", "capture");
        event.put("shape_ref", "basic_capture/v1");
        event.put("activity_ref", null);
        event.put("subject_ref", Map.of("type", "subject", "id", subjectId.toString()));
        event.put("actor_ref", Map.of("type", "actor", "id", actorId));
        event.put("device_id", deviceId.toString());
        event.put("device_seq", seq);
        event.put("sync_watermark", null);
        event.put("timestamp", OffsetDateTime.now(ZoneOffset.UTC).toString());
        event.put("payload", Map.of(
                "name", "Auth test",
                "category", "production",
                "notes", "principal binding",
                "date", "2026-06-04",
                "value", seq));
        return event;
    }

    private UUID insertCaptureEvent(UUID subjectId, UUID actorId, UUID deviceId, int seq) {
        UUID eventId = UUID.randomUUID();
        jdbcTemplate.update("""
                INSERT INTO events (id, type, shape_ref, activity_ref, subject_ref, actor_ref,
                                    device_id, device_seq, timestamp, payload)
                VALUES (?::uuid, 'capture', 'basic_capture/v1', NULL,
                        ?::jsonb, ?::jsonb, ?::uuid, ?, NOW()::timestamptz, ?::jsonb)
                """,
                eventId.toString(),
                "{\"type\":\"subject\",\"id\":\"" + subjectId + "\"}",
                "{\"type\":\"actor\",\"id\":\"" + actorId + "\"}",
                deviceId.toString(),
                seq,
                "{\"name\":\"Existing\",\"category\":\"production\",\"notes\":\"existing\"," +
                        "\"date\":\"2026-06-04\",\"value\":1}");
        return eventId;
    }

    private UUID insertFlagWithResolver(UUID sourceEventId, UUID subjectId, UUID resolverId) {
        UUID flagId = UUID.randomUUID();
        UUID serverDeviceId = jdbcTemplate.queryForObject(
                "SELECT device_id FROM server_identity LIMIT 1", UUID.class);
        long seq = jdbcTemplate.queryForObject("SELECT nextval('server_device_seq')", Long.class);
        jdbcTemplate.update("""
                INSERT INTO events (id, type, shape_ref, activity_ref, subject_ref, actor_ref,
                                    device_id, device_seq, timestamp, payload)
                VALUES (?::uuid, 'alert', 'conflict_detected/v1', NULL,
                        ?::jsonb, ?::jsonb, ?::uuid, ?, NOW()::timestamptz, ?::jsonb)
                """,
                flagId.toString(),
                "{\"type\":\"subject\",\"id\":\"" + subjectId + "\"}",
                "{\"type\":\"actor\",\"id\":\"system:conflict_detector/concurrent_state_change\"}",
                serverDeviceId.toString(),
                seq,
                "{\"source_event_id\":\"" + sourceEventId + "\"," +
                        "\"flag_category\":\"concurrent_state_change\"," +
                        "\"resolvability\":\"manual_only\"," +
                        "\"designated_resolver\":{\"type\":\"actor\",\"id\":\"" + resolverId + "\"}," +
                        "\"reason\":\"Production auth resolver test\"}");
        return flagId;
    }

    private int storedEventCount() {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM events", Integer.class);
        return count == null ? 0 : count;
    }

    private int storedEventCount(String eventId) {
        Integer count = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM events
                WHERE id = ?::uuid
                """, Integer.class, eventId);
        return count == null ? 0 : count;
    }

    private int assignmentCreatedCount() {
        Integer count = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM events
                WHERE shape_ref = 'assignment_created/v1'
                """, Integer.class);
        return count == null ? 0 : count;
    }

    private int canonicalResolutionCount(UUID flagId) {
        Integer count = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM events cr
                JOIN events cd ON cd.id::text = cr.payload->>'flag_event_id'
                WHERE cr.shape_ref = 'conflict_resolved/v1'
                  AND cr.payload->>'flag_event_id' = ?
                  AND cr.actor_ref->>'type' = cd.payload->'designated_resolver'->>'type'
                  AND cr.actor_ref->>'id' = cd.payload->'designated_resolver'->>'id'
                """, Integer.class, flagId.toString());
        return count == null ? 0 : count;
    }

    private int unauthorizedResolutionFlagCount() {
        Integer count = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM events
                WHERE shape_ref = 'conflict_detected/v1'
                  AND payload->>'flag_category' = 'scope_violation'
                """, Integer.class);
        return count == null ? 0 : count;
    }

    private Map<String, Object> adminClaims() {
        return Map.of(
                "groups", List.of("platform-admin", "national-supervisor"),
                "realm_access", Map.of("roles", List.of("admin")),
                "resource_access", Map.of("datarun", Map.of("roles", List.of("resolver"))),
                "actor_id", OTHER_ACTOR.toString());
    }

    private String jwt(String subject, Map<String, Object> extraClaims) throws Exception {
        Map<String, Object> header = Map.of("alg", "HS256", "typ", "JWT");
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("iss", ISSUER);
        payload.put("sub", subject);
        payload.put("aud", AUDIENCE);
        payload.put("exp", Instant.now().plusSeconds(3600).getEpochSecond());
        payload.putAll(extraClaims);

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
