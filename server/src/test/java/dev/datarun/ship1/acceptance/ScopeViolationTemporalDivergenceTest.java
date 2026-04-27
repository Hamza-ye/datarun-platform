package dev.datarun.ship1.acceptance;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dev.datarun.ship1.DatarunApplication;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Ship-3 closeout G-7' (re-aimed) — projection-time temporal-divergence test for
 * {@code scope_violation}. Drives the real server over HTTP per the Ship-1 walkthrough
 * convention (two simulated devices, JDBC token seed, assertions on {@code shape_ref}
 * and never on envelope {@code type}).
 *
 * <h2>Why this test exists</h2>
 *
 * FP-001 was originally written against a {@code role_stale} detector that lived in the
 * pre-convergence {@code dev.datarun.server} package and was NOT reinstated by Ship-1's
 * clean-slate rebuild under {@code dev.datarun.ship1}. {@code grep -r role_stale server/}
 * returns zero matches against current main; {@code ScopeResolver.hasRoleAt} (Ship-2) has
 * no production callers. FP-001 has therefore been carried forward as "needs a test"
 * against ghost code for three Ships. Closeout G-7's first dispatch surfaced this gap;
 * this re-dispatch resolves it by exercising the same projection-time-correctness
 * invariant against the substrate that DOES exist end-to-end:
 * {@link dev.datarun.ship1.scope.ScopeResolver#activeGeographicScopes(String, OffsetDateTime)}
 * powering {@code scope_violation} detection in
 * {@link dev.datarun.ship1.integrity.ConflictDetector}.
 *
 * <p>FP-001 retirement and a successor FP (aimed at {@code role_stale} when/if it lands)
 * are PM Wave-2 work and are not modified here. (Constraint: do not edit FP-001.)
 *
 * <h2>The forcing property</h2>
 *
 * The detector calls {@code scopes.activeGeographicScopes(actorId, capture.timestamp())}
 * — the temporal anchor is the capture's <em>event-time</em>. ADR-003 §S3 requires
 * authorization decisions to reconstruct from the event stream at the time the
 * authorized act occurred, not at the moment of evaluation. The access-control
 * scenario "Authority is contextual, not absolute" + "Access decisions that must hold
 * offline" formalize the same requirement from the user-experience side: an offline
 * device's capture must remain valid against the actor's scope at capture time even
 * if the server has since processed a reassignment.
 *
 * <p>Event C (below) is the forcing case: a regression to a request-time anchor —
 * whether by changing {@code capture.timestamp()} to {@code Instant.now()} or by
 * introducing a cached {@code currentScopes(actorId)} map keyed at evaluation time —
 * would flag Event C as {@code scope_violation}. A correct projection-time detector
 * resolves the actor's scope at T_C (when Event C was authored) and accepts cleanly.
 * <strong>This test cannot pass under any cache-based or request-time-anchored
 * implementation.</strong>
 *
 * <h2>Scenarios defended</h2>
 *
 * <ul>
 *   <li>S03 — user-based assignment (scope is per-actor, time-bounded).</li>
 *   <li>S19 — offline capture and sync (the forcing case is precisely an offline device
 *       whose capture predates a server-side reassignment).</li>
 * </ul>
 *
 * <h2>Sibling tests</h2>
 *
 * Same {@code @SpringBootTest} harness as {@link WalkthroughAcceptanceTest},
 * {@link Ship2WalkthroughAcceptanceTest}, {@link Ship3WalkthroughAcceptanceTest}.
 * {@code @BeforeEach} truncates {@code events}, {@code actor_tokens}, {@code villages}
 * and resets {@code server_device_seq}, identical to the existing walkthrough tests.
 *
 * <h2>Discriminator discipline (F-A2 / F-A4)</h2>
 *
 * Every flag-existence assertion below filters on {@code shape_ref = conflict_detected/v1}
 * and {@code payload.flag_category = scope_violation}. Envelope {@code type=alert} is
 * cross-checked but never used as the discriminator. No assertion branches on envelope
 * {@code type} alone.
 */
@SpringBootTest(classes = DatarunApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class ScopeViolationTemporalDivergenceTest {

    @Value("${local.server.port}")
    private int port;

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private ObjectMapper mapper;

    private final RestTemplate http = new RestTemplate();

    @BeforeEach
    void cleanDb() {
        jdbc.update("TRUNCATE events, actor_tokens, villages RESTART IDENTITY CASCADE");
        jdbc.update("ALTER SEQUENCE server_device_seq RESTART WITH 1");
    }

    /**
     * One consolidated walkthrough — Events A, B, C against a single reassignment timeline.
     * Step ordering matters (A predates the reassignment, B postdates it, C straddles it
     * in event-time vs request-time), so consolidating into one test keeps the timeline
     * and the assertions adjacent.
     *
     * <p>Wall-clock vs. event-time: every event in this test is pushed within the same
     * test-method JVM tick. The "request-time" anchor — were the detector to use one — would
     * therefore be roughly equal across all three pushes (≈ now), all post-T2. That is
     * exactly the regression Event C is designed to catch.
     */
    @Test
    void scope_violation_uses_event_time_not_request_time_under_actor_reassignment() throws Exception {
        // ---- Timeline (all in the past relative to the test's wall-clock now) -----------
        OffsetDateTime now = OffsetDateTime.now();
        OffsetDateTime t0 = now.minusHours(4);              // A→V1 begins
        OffsetDateTime t1 = now.minusHours(3);              // Event A authored (in scope at T1)
        OffsetDateTime tC = now.minusMinutes(150);          // Event C authored (T0 < T_C < T2; offline at the time)
        OffsetDateTime t2 = now.minusHours(2);              // A→V1 ends; A→V2 begins
        OffsetDateTime t3 = now.minusHours(1);              // Event B authored (out-of-scope at T3)

        // ---- World seed: villages, actor token, two assignment events --------------------
        UUID villageV1 = UUID.randomUUID();
        UUID villageV2 = UUID.randomUUID();
        seedVillage(villageV1, "Village-V1");
        seedVillage(villageV2, "Village-V2");

        UUID actorA = UUID.randomUUID();
        String tokenA = issueToken(actorA);
        // Admin/system token used as the bearer for all pushes — push path does not require
        // bearer-actor to match envelope actor_ref (matches the existing walkthrough convention).
        String adminToken = issueToken(UUID.randomUUID());

        UUID deviceA = UUID.randomUUID();      // actor A's regular device
        UUID deviceAdmin = UUID.randomUUID();  // device through which assignment events arrive

        // Assignment 1: A→V1 with a closed validity window [T0, T2). ScopeResolver only
        // processes the assignment_created/v1 shape and honors valid_to (see ScopeResolver
        // line 65: "if (validTo != null && !at.isBefore(validTo)) continue"). Encoding the
        // reassignment via valid_to on the original assignment_created/v1 is the only path
        // available to the resolver as of Ship-3 (assignment_ended/v1 is currently inert
        // in scope reconstruction; that is a forward-looking concern, not this test's).
        pushAssignment(adminToken, actorA, villageV1, deviceAdmin, 1, t0, t0, t2);
        // Assignment 2: A→V2 from T2 onward.
        pushAssignment(adminToken, actorA, villageV2, deviceAdmin, 2, t2, t2, null);

        // ---- Event A: capture at T1 in V1 ----
        // T0 < T1 < T2  →  A's scope at T1 includes V1  →  scope_violation MUST NOT fire.
        UUID subjA = UUID.randomUUID();
        ObjectNode evtA = householdCaptureV1(subjA, actorA, deviceA, 10, villageV1, "Khan household", 5, t1);
        HttpResponse<String> respA = push(adminToken, pushBody(evtA));
        assertThat(respA.statusCode).isEqualTo(200);
        assertThat(mapper.readTree(respA.body).path("accepted").asInt()).isEqualTo(1);
        assertThat(scopeViolationFlagsFor(evtA.path("id").asText()))
                .describedAs("Event A authored at T1 (within A's V1 binding [T0,T2)) — projection-time " +
                        "scope at T1 includes V1; scope_violation must not fire.")
                .isEmpty();

        // ---- Event B: capture at T3 in V1 ----
        // T3 > T2  →  A's scope at T3 is {V2}, NOT V1  →  scope_violation MUST fire.
        UUID subjB = UUID.randomUUID();
        ObjectNode evtB = householdCaptureV1(subjB, actorA, deviceA, 11, villageV1, "Sharif household", 4, t3);
        HttpResponse<String> respB = push(adminToken, pushBody(evtB));
        assertThat(respB.statusCode).isEqualTo(200);
        assertThat(mapper.readTree(respB.body).path("accepted").asInt())
                .describedAs("Accept-and-flag (ADR-006 §S1) — out-of-scope captures are NOT rejected.")
                .isEqualTo(1);
        JsonNode flagsB = scopeViolationFlagsFor(evtB.path("id").asText());
        assertThat(flagsB.size())
                .describedAs("Event B authored at T3 (after the reassignment) in V1 — projection-time " +
                        "scope at T3 excludes V1; scope_violation must fire exactly once.")
                .isEqualTo(1);
        // F-A2 / F-A4: discriminate on shape_ref + payload.flag_category, never on envelope type alone.
        assertThat(flagsB.get(0).path("shape_ref").asText()).isEqualTo("conflict_detected/v1");
        assertThat(flagsB.get(0).path("type").asText()).isEqualTo("alert");
        assertThat(flagsB.get(0).path("actor_id").asText())
                .isEqualTo("system:conflict_detector/scope_violation");
        assertThat(flagsB.get(0).path("payload").path("flag_category").asText()).isEqualTo("scope_violation");

        // ============================== Event C — FORCING PROPERTY ==============================
        // Event C is authored at T_C (T0 < T_C < T2) — that is, while A's V1 binding was still
        // active. The device was offline at T_C and only reaches the server now (wall-clock
        // post-T2, when A is no longer assigned to V1).
        //
        // FORCING PROPERTY — Event C honors A's scope at event-time (T_C, when A was assigned
        // to V1), not request-time (post-T2, when A is now assigned to V2). A regression to
        // request-time anchoring fails this assertion: any of the following implementations
        // would mis-flag Event C and break this test —
        //   1. scopes.activeGeographicScopes(actorId, Instant.now())   ← request-time anchor
        //   2. scopes.activeGeographicScopes(actorId, OffsetDateTime.now())
        //   3. a cached `currentScopes(actorId)` map populated at request handling
        //   4. any state cache that reflects the actor's "current" assignments
        //
        // The correct implementation — projection-time replay anchored at capture.timestamp() —
        // computes A's scope at T_C as {V1} and accepts cleanly. ADR-003 §S3 mandates exactly
        // this behavior; the access-control scenario's "authority is contextual, not absolute"
        // requirement is the user-facing form of the same property; S19 (offline capture and
        // sync) is the scenario that depends on it.
        UUID subjC = UUID.randomUUID();
        ObjectNode evtC = householdCaptureV1(subjC, actorA, deviceA, 12, villageV1, "Bukhari household", 6, tC);
        HttpResponse<String> respC = push(adminToken, pushBody(evtC));
        assertThat(respC.statusCode).isEqualTo(200);
        assertThat(mapper.readTree(respC.body).path("accepted").asInt()).isEqualTo(1);
        assertThat(scopeViolationFlagsFor(evtC.path("id").asText()))
                .describedAs("Event C authored at T_C (within A's V1 binding [T0,T2)) but pushed AFTER " +
                        "the reassignment — projection-time replay at T_C must include V1; " +
                        "request-time replay at now would not. scope_violation must not fire.")
                .isEmpty();
    }

    // ============================================================================== helpers

    private void seedVillage(UUID id, String name) {
        jdbc.update("INSERT INTO villages (id, district_name, name) VALUES (?::uuid, ?, ?)",
                id.toString(), "Mirpur", name);
    }

    private String issueToken(UUID actorId) {
        String token = UUID.randomUUID().toString().replace("-", "")
                + UUID.randomUUID().toString().replace("-", "");
        jdbc.update("INSERT INTO actor_tokens (token, actor_id) VALUES (?, ?::uuid)",
                token, actorId.toString());
        return token;
    }

    private void pushAssignment(String bearerToken, UUID targetActor, UUID villageId,
                                UUID deviceId, long deviceSeq,
                                OffsetDateTime eventTimestamp,
                                OffsetDateTime validFrom, OffsetDateTime validTo) {
        ObjectNode env = mapper.createObjectNode();
        env.put("id", UUID.randomUUID().toString());
        env.put("type", "assignment_changed");
        env.put("shape_ref", "assignment_created/v1");
        env.putNull("activity_ref");
        ObjectNode sref = env.putObject("subject_ref");
        sref.put("type", "assignment");
        sref.put("id", UUID.randomUUID().toString());
        ObjectNode aref = env.putObject("actor_ref");
        aref.put("type", "actor");
        aref.put("id", "system:test_seed/scope_violation_temporal_divergence");
        env.put("device_id", deviceId.toString());
        env.put("device_seq", deviceSeq);
        env.putNull("sync_watermark");
        env.put("timestamp", eventTimestamp.toString());

        ObjectNode payload = env.putObject("payload");
        ObjectNode target = payload.putObject("target_actor");
        target.put("type", "actor");
        target.put("id", targetActor.toString());
        payload.put("role", "chv");
        ObjectNode scope = payload.putObject("scope");
        scope.put("geographic", villageId.toString());
        scope.putNull("subject_list");
        scope.putNull("activity");
        payload.put("valid_from", validFrom.toString());
        if (validTo == null) payload.putNull("valid_to");
        else payload.put("valid_to", validTo.toString());

        HttpResponse<String> resp = push(bearerToken, pushBody(env));
        assertThat(resp.statusCode)
                .describedAs("assignment seed push must succeed: %s", resp.body)
                .isEqualTo(200);
    }

    private ObjectNode householdCaptureV1(UUID subjectId, UUID actorId, UUID deviceId,
                                          long deviceSeq, UUID villageId,
                                          String householdName, int householdSize,
                                          OffsetDateTime eventTimestamp) {
        ObjectNode env = mapper.createObjectNode();
        env.put("id", UUID.randomUUID().toString());
        env.put("type", "capture");
        env.put("shape_ref", "household_observation/v1");
        env.put("activity_ref", "household_observation");
        ObjectNode sref = env.putObject("subject_ref");
        sref.put("type", "subject");
        sref.put("id", subjectId.toString());
        ObjectNode aref = env.putObject("actor_ref");
        aref.put("type", "actor");
        aref.put("id", actorId.toString());
        env.put("device_id", deviceId.toString());
        env.put("device_seq", deviceSeq);
        env.putNull("sync_watermark");
        env.put("timestamp", eventTimestamp.toString());

        ObjectNode payload = env.putObject("payload");
        payload.put("household_name", householdName);
        payload.put("head_of_household_name", "HoH");
        payload.put("household_size", householdSize);
        payload.put("village_ref", villageId.toString());
        payload.putNull("latitude");
        payload.putNull("longitude");
        payload.putNull("visit_notes");
        return env;
    }

    private ObjectNode pushBody(ObjectNode... events) {
        ObjectNode body = mapper.createObjectNode();
        ArrayNode arr = body.putArray("events");
        for (ObjectNode e : events) arr.add(e);
        return body;
    }

    private HttpResponse<String> push(String bearerToken, ObjectNode body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + bearerToken);
        try {
            ResponseEntity<String> resp = http.exchange(
                    "http://localhost:" + port + "/api/sync/push", HttpMethod.POST,
                    new HttpEntity<>(body.toString(), headers), String.class);
            return new HttpResponse<>(resp.getStatusCode().value(), resp.getBody());
        } catch (HttpClientErrorException ex) {
            return new HttpResponse<>(ex.getStatusCode().value(), ex.getResponseBodyAsString());
        }
    }

    /** All scope_violation flag events whose payload.source_event_id equals the supplied id. */
    private JsonNode scopeViolationFlagsFor(String sourceEventId) throws Exception {
        var rows = jdbc.queryForList(
                "SELECT id, type, shape_ref, actor_id, payload::text AS payload " +
                        "FROM events WHERE shape_ref = 'conflict_detected/v1' " +
                        "AND payload->>'flag_category' = 'scope_violation' " +
                        "AND payload->>'source_event_id' = ? " +
                        "ORDER BY sync_watermark",
                sourceEventId);
        ArrayNode out = mapper.createArrayNode();
        for (var row : rows) {
            ObjectNode n = mapper.createObjectNode();
            n.put("id", row.get("id").toString());
            n.put("type", row.get("type").toString());
            n.put("shape_ref", row.get("shape_ref").toString());
            n.put("actor_id", row.get("actor_id").toString());
            n.set("payload", mapper.readTree(row.get("payload").toString()));
            out.add(n);
        }
        return out;
    }

    private record HttpResponse<T>(int statusCode, T body) {}
}
