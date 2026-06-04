package dev.datarun.server.authorization;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.sun.net.httpserver.HttpServer;
import dev.datarun.server.AbstractIntegrationTest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * NW-038 / FP-011 gates for OIDC/JWKS provider validation, explicit
 * principal-to-actor mapping, and group/claim non-authority.
 */
class ProductionAuthIntegrationTest extends AbstractIntegrationTest {

    private static final String ISSUER = "https://issuer.test/datarun";
    private static final String AUDIENCE = "datarun-mobile";
    private static final String KEY_ID = "datarun-oidc-test-key";
    private static final ObjectMapper STATIC_OBJECT_MAPPER = new ObjectMapper();

    private static final UUID ACTOR = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID OTHER_ACTOR = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final UUID DEVICE = UUID.fromString("33333333-3333-3333-3333-333333333333");
    private static final UUID OTHER_DEVICE = UUID.fromString("44444444-4444-4444-4444-444444444444");

    private static HttpServer jwksServer;
    private static RSAPrivateKey oidcPrivateKey;

    @Autowired private TestRestTemplate rest;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private AuthPrincipalBindingRepository bindingRepository;
    @LocalServerPort private int port;

    @DynamicPropertySource
    static void productionAuthProperties(DynamicPropertyRegistry registry) {
        ensureJwksServer();
        registry.add("datarun.auth.mode", () -> "oidc-jwks");
        registry.add("datarun.auth.oidc.issuer", () -> ISSUER);
        registry.add("datarun.auth.oidc.audience", () -> AUDIENCE);
        registry.add("datarun.auth.oidc.jwks-uri", ProductionAuthIntegrationTest::jwksUri);
    }

    @BeforeAll
    static void startJwks() {
        ensureJwksServer();
    }

    @AfterAll
    static void stopJwks() {
        if (jwksServer != null) {
            jwksServer.stop(0);
            jwksServer = null;
        }
    }

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

        ResponseEntity<JsonNode> response = getAuthMe(oidcJwt("principal-admin", adminClaims()));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().path("actor_id").asText()).isEqualTo(ACTOR.toString());
        assertThat(response.getBody().path("auth_source").asText()).isEqualTo("oidc-jwks-principal");
    }

    @Test
    void groupClaimsDoNotGrantPullOrAssignmentAuthority() throws Exception {
        bindingRepository.bind(ISSUER, "principal-no-assignment", ACTOR);
        insertCaptureEvent(UUID.randomUUID(), OTHER_ACTOR, OTHER_DEVICE, 1);
        String token = oidcJwt("principal-no-assignment", adminClaims());

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

        assertAuthFailurePersistsNothing(event, eventId, "not-a-jwt");
        assertAuthFailurePersistsNothing(event, eventId, badSignatureJwt());
        assertAuthFailurePersistsNothing(event, eventId, unknownKidJwt());
        assertAuthFailurePersistsNothing(event, eventId,
                oidcJwt("principal-worker", ISSUER + "/wrong", AUDIENCE,
                        KEY_ID, oidcPrivateKey, JWSAlgorithm.RS256,
                        Instant.now().plusSeconds(3600), null, Map.of()));
        assertAuthFailurePersistsNothing(event, eventId,
                oidcJwt("principal-worker", ISSUER, "wrong-audience",
                        KEY_ID, oidcPrivateKey, JWSAlgorithm.RS256,
                        Instant.now().plusSeconds(3600), null, Map.of()));
        assertAuthFailurePersistsNothing(event, eventId,
                oidcJwt("principal-worker", ISSUER, AUDIENCE,
                        KEY_ID, oidcPrivateKey, JWSAlgorithm.RS256,
                        Instant.now().minusSeconds(3600), null, Map.of()));
        assertAuthFailurePersistsNothing(event, eventId,
                oidcJwt("principal-worker", ISSUER, AUDIENCE,
                        KEY_ID, oidcPrivateKey, JWSAlgorithm.RS256,
                        Instant.now().plusSeconds(3600),
                        Instant.now().plusSeconds(3600), Map.of()));
        assertAuthFailurePersistsNothing(event, eventId, unsupportedAlgorithmJwt());

        HttpResponse<String> unmapped =
                postPushRaw(List.of(event), oidcJwt("unmapped-principal", adminClaims()));
        assertThat(unmapped.statusCode()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
        assertThat(storedEventCount(eventId)).isZero();
    }

    @Test
    void productionPushEnforcesActorRefBindingAndRejectsClientSystemAuthorship() throws Exception {
        bindingRepository.bind(ISSUER, "principal-worker", ACTOR);
        String token = oidcJwt("principal-worker", Map.of());

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
        String token = oidcJwt("principal-admin-claim", adminClaims());
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

    private static synchronized void ensureJwksServer() {
        if (jwksServer != null) {
            return;
        }
        try {
            KeyPair keyPair = generateRsaKeyPair();
            oidcPrivateKey = (RSAPrivateKey) keyPair.getPrivate();
            RSAKey publicJwk = new RSAKey.Builder((RSAPublicKey) keyPair.getPublic())
                    .keyID(KEY_ID)
                    .algorithm(JWSAlgorithm.RS256)
                    .build()
                    .toPublicJWK();
            byte[] jwks = STATIC_OBJECT_MAPPER.writeValueAsBytes(
                    Map.of("keys", List.of(publicJwk.toJSONObject())));

            jwksServer = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
            jwksServer.createContext("/jwks", exchange -> {
                exchange.getResponseHeaders().set("Content-Type", "application/json");
                exchange.sendResponseHeaders(200, jwks.length);
                exchange.getResponseBody().write(jwks);
                exchange.close();
            });
            jwksServer.start();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to start test JWKS server", e);
        }
    }

    private static KeyPair generateRsaKeyPair() throws Exception {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        return generator.generateKeyPair();
    }

    private static String jwksUri() {
        ensureJwksServer();
        return "http://127.0.0.1:" + jwksServer.getAddress().getPort() + "/jwks";
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

    private void assertAuthFailurePersistsNothing(
            Map<String, Object> event, String eventId, String token) throws Exception {
        HttpResponse<String> response = postPushRaw(List.of(event), token);
        assertThat(response.statusCode()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
        assertThat(storedEventCount(eventId)).isZero();
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

    private String oidcJwt(String subject, Map<String, Object> extraClaims) throws Exception {
        return oidcJwt(subject, ISSUER, AUDIENCE, KEY_ID, oidcPrivateKey, JWSAlgorithm.RS256,
                Instant.now().plusSeconds(3600), null, extraClaims);
    }

    private String oidcJwt(String subject,
                           String issuer,
                           String audience,
                           String keyId,
                           RSAPrivateKey privateKey,
                           JWSAlgorithm algorithm,
                           Instant expiresAt,
                           Instant notBefore,
                           Map<String, Object> extraClaims) throws Exception {
        JWTClaimsSet.Builder claims = new JWTClaimsSet.Builder()
                .issuer(issuer)
                .subject(subject)
                .audience(audience)
                .expirationTime(Date.from(expiresAt));
        if (notBefore != null) {
            claims.notBeforeTime(Date.from(notBefore));
        }
        extraClaims.forEach(claims::claim);

        SignedJWT jwt = new SignedJWT(
                new JWSHeader.Builder(algorithm)
                        .type(JOSEObjectType.JWT)
                        .keyID(keyId)
                        .build(),
                claims.build());
        jwt.sign(new RSASSASigner(privateKey));
        return jwt.serialize();
    }

    private String badSignatureJwt() throws Exception {
        KeyPair badKeyPair = generateRsaKeyPair();
        return oidcJwt("principal-worker", ISSUER, AUDIENCE, KEY_ID,
                (RSAPrivateKey) badKeyPair.getPrivate(), JWSAlgorithm.RS256,
                Instant.now().plusSeconds(3600), null, Map.of());
    }

    private String unknownKidJwt() throws Exception {
        KeyPair badKeyPair = generateRsaKeyPair();
        return oidcJwt("principal-worker", ISSUER, AUDIENCE, "unknown-key",
                (RSAPrivateKey) badKeyPair.getPrivate(), JWSAlgorithm.RS256,
                Instant.now().plusSeconds(3600), null, Map.of());
    }

    private String unsupportedAlgorithmJwt() throws Exception {
        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .issuer(ISSUER)
                .subject("principal-worker")
                .audience(AUDIENCE)
                .expirationTime(Date.from(Instant.now().plusSeconds(3600)))
                .build();
        SignedJWT jwt = new SignedJWT(
                new JWSHeader.Builder(JWSAlgorithm.HS256)
                        .type(JOSEObjectType.JWT)
                        .keyID(KEY_ID)
                        .build(),
                claims);
        jwt.sign(new MACSigner("01234567890123456789012345678901"));
        return jwt.serialize();
    }
}
