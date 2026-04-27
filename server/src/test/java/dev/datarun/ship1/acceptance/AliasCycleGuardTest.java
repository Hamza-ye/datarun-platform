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
 * Ship-3 closeout Wave 3 step (b) — push-path alias-cycle guard acceptance.
 * Implementation of {@code docs/architecture/cycle-guard-contract.md} §7
 * (test contract); authority {@link dev.datarun.ship1.integrity.CycleGuard}
 * and ADR-006-R §S5. Closes FP-019.
 *
 * <h2>Why this test exists</h2>
 *
 * Reviewer findings SC-07 + C2-03 ([`docs/reviews/system/architect.md`]) identified
 * that two coordinators acting on offline-divergent views of the alias graph could
 * each commit a {@code subjects_merged/v1} or {@code subject_split/v1} event that,
 * taken jointly, closes a cycle. Once the cycle-closing event is persisted,
 * ADR-001 §S1 (append-only) forbids retraction; identity resolution that traverses
 * the cycle is undefined. ADR-006-R §S5 commits the platform to surfacing
 * cycle-closure as a flag ({@code flag_category = "cycle_violation"}, {@code manual_only})
 * preserving accept-and-flag (ADR-006-R §S1). This test pair is the gate for FP-019.
 *
 * <h2>The two cases</h2>
 *
 * <ol>
 *   <li><b>Cross-batch</b> ({@link #cycleGuard_singleEventPush_persistedGraphCloses}) —
 *       persisted graph contains edge A→B; a separate push introduces edge B→A which
 *       closes a cycle against the persisted graph. Demonstrates the guard reads the
 *       persisted alias graph at request time.</li>
 *   <li><b>Intra-batch</b> ({@link #cycleGuard_twoEventBatch_inFlightCloses}) —
 *       FORCING PROPERTY (see below). Single push of two merges that, considered
 *       independently against the persisted graph alone, neither closes a cycle.
 *       Together they do — and the contract's batch-serial semantics catch it.</li>
 * </ol>
 *
 * <h2>Forcing property (Test B)</h2>
 *
 * A per-event guard that reads only the persisted graph (committed events only)
 * misses the intra-batch case. The two merges, considered against persisted-only
 * state, each pass independently — but together they close a cycle. The contract's
 * batch-serial semantics include earlier-in-batch already-accepted edges; this
 * test asserts that batch-relative inclusion is honored. A regression to
 * per-event-against-persisted-only fails the assertion.
 *
 * <h2>Discriminator discipline (F-A2 / F-A4)</h2>
 *
 * Every flag-existence assertion below filters on {@code shape_ref = conflict_detected/v1}
 * and {@code payload.flag_category = cycle_violation}. Envelope {@code type=alert} is
 * cross-checked but never used as the discriminator. No assertion branches on envelope
 * {@code type} alone.
 */
@SpringBootTest(classes = DatarunApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class AliasCycleGuardTest {

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
     * Test A — cross-batch: persisted graph closes against a fresh push. Per
     * cycle-guard contract §7.1.
     */
    @Test
    void cycleGuard_singleEventPush_persistedGraphCloses() throws Exception {
        String token = issueToken(UUID.randomUUID());
        UUID device = UUID.randomUUID();

        UUID a = UUID.randomUUID();
        UUID b = UUID.randomUUID();

        // Push 1: edge A → B (retired=A, surviving=B).
        ObjectNode merge1 = mergeEvent(a, b, device, 1);
        HttpResponse<String> resp1 = push(token, pushBody(merge1));
        assertThat(resp1.statusCode).isEqualTo(200);
        JsonNode r1 = mapper.readTree(resp1.body);
        assertThat(r1.path("accepted").asInt()).isEqualTo(1);
        assertThat(r1.path("flags_raised").asInt())
                .describedAs("first edge against an empty graph never closes a cycle")
                .isEqualTo(0);

        // Push 2: edge B → A — closes A → B → A against the persisted graph.
        ObjectNode merge2 = mergeEvent(b, a, device, 2);
        HttpResponse<String> resp2 = push(token, pushBody(merge2));
        assertThat(resp2.statusCode).isEqualTo(200);
        JsonNode r2 = mapper.readTree(resp2.body);
        assertThat(r2.path("accepted").asInt())
                .describedAs("alias events are accepted (F-D1, accept-and-flag) — never rejected")
                .isEqualTo(1);
        assertThat(r2.path("flags_raised").asInt())
                .describedAs("cross-batch cycle closure must emit exactly one cycle_violation flag")
                .isEqualTo(1);

        // Flag exists with the contract-§5 envelope + payload shape, sourced at push-2's event.
        JsonNode flags = cycleViolationFlagsFor(merge2.path("id").asText());
        assertThat(flags.size()).isEqualTo(1);
        JsonNode flag = flags.get(0);
        assertThat(flag.path("shape_ref").asText()).isEqualTo("conflict_detected/v1");
        assertThat(flag.path("type").asText()).isEqualTo("alert");
        assertThat(flag.path("actor_id").asText()).isEqualTo("system:cycle_guard/cycle_violation");
        assertThat(flag.path("payload").path("flag_category").asText()).isEqualTo("cycle_violation");
        assertThat(flag.path("payload").path("resolvability").asText()).isEqualTo("manual_only");

        // Canonical cycle_path per contract §4.3 — [to, intermediate..., from, to].
        // For new edge B→A (from=B, to=A) against persisted A→B: DFS from A → B → match from
        // → return [A, B, A].
        JsonNode cyclePath = flag.path("payload").path("cycle_path");
        assertThat(cyclePath.isArray()).isTrue();
        assertThat(cyclePath.size()).isEqualTo(3);
        assertThat(cyclePath.get(0).asText()).isEqualTo(a.toString());
        assertThat(cyclePath.get(1).asText()).isEqualTo(b.toString());
        assertThat(cyclePath.get(2).asText()).isEqualTo(a.toString());

        assertThat(flag.path("payload").path("source_event_id").asText())
                .isEqualTo(merge2.path("id").asText());
    }

    /**
     * Test B — intra-batch FORCING PROPERTY: two-merge batch where neither edge
     * alone closes against the persisted graph but together they do. Per
     * cycle-guard contract §7.2.
     *
     * <p>FORCING PROPERTY — A per-event guard that reads only the persisted graph
     * (committed events only) misses this cycle. The two merges, considered against
     * persisted-only state, each pass independently — but together they close a
     * cycle. The contract's batch-serial semantics include earlier-in-batch
     * already-accepted edges; this test asserts that batch-relative inclusion is
     * honored. A regression to per-event-against-persisted-only fails this
     * assertion.
     */
    @Test
    void cycleGuard_twoEventBatch_inFlightCloses() throws Exception {
        String token = issueToken(UUID.randomUUID());
        UUID device = UUID.randomUUID();

        UUID a = UUID.randomUUID();
        UUID b = UUID.randomUUID();

        // Single push, two events in array order:
        //   Event 1: retired=A, surviving=B   → edge A → B
        //   Event 2: retired=B, surviving=A   → edge B → A (closes cycle through event 1's edge)
        // FORCING PROPERTY: against the persisted graph alone (empty, then still empty after
        // event 1 because nothing has committed yet), event 2 would NOT close a cycle. Only
        // batch-serial semantics — which include event 1's already-accepted in-flight edge in
        // the union graph for event 2's check — surface the cycle. A naive pre-persist guard
        // that reads only the persisted graph passes Test A but fails this assertion.
        ObjectNode merge1 = mergeEvent(a, b, device, 1);
        ObjectNode merge2 = mergeEvent(b, a, device, 2);
        HttpResponse<String> resp = push(token, pushBody(merge1, merge2));

        assertThat(resp.statusCode).isEqualTo(200);
        JsonNode parsed = mapper.readTree(resp.body);
        assertThat(parsed.path("accepted").asInt())
                .describedAs("both alias events accepted (accept-and-flag preserved across the batch)")
                .isEqualTo(2);
        assertThat(parsed.path("flags_raised").asInt())
                .describedAs("exactly one cycle_violation — emitted on event 2 (the cycle-closing edge), " +
                        "NOT on event 1 (which on its own is a clean merge)")
                .isEqualTo(1);

        // Flag must reference event 2 — the cycle-closing edge — and not event 1.
        JsonNode flagsOnEvent1 = cycleViolationFlagsFor(merge1.path("id").asText());
        assertThat(flagsOnEvent1.size())
                .describedAs("event 1 edge A→B does not close a cycle on its own; no flag")
                .isEqualTo(0);

        JsonNode flagsOnEvent2 = cycleViolationFlagsFor(merge2.path("id").asText());
        assertThat(flagsOnEvent2.size()).isEqualTo(1);
        JsonNode flag = flagsOnEvent2.get(0);
        assertThat(flag.path("shape_ref").asText()).isEqualTo("conflict_detected/v1");
        assertThat(flag.path("type").asText()).isEqualTo("alert");
        assertThat(flag.path("actor_id").asText()).isEqualTo("system:cycle_guard/cycle_violation");
        assertThat(flag.path("payload").path("flag_category").asText()).isEqualTo("cycle_violation");
        assertThat(flag.path("payload").path("source_event_id").asText())
                .isEqualTo(merge2.path("id").asText());

        // Canonical cycle_path per contract §4.3: [A, B, A] — same shape as Test A.
        JsonNode cyclePath = flag.path("payload").path("cycle_path");
        assertThat(cyclePath.isArray()).isTrue();
        assertThat(cyclePath.size()).isEqualTo(3);
        assertThat(cyclePath.get(0).asText()).isEqualTo(a.toString());
        assertThat(cyclePath.get(1).asText()).isEqualTo(b.toString());
        assertThat(cyclePath.get(2).asText()).isEqualTo(a.toString());
    }

    // ============================================================================== helpers

    private String issueToken(UUID actorId) {
        String token = UUID.randomUUID().toString().replace("-", "")
                + UUID.randomUUID().toString().replace("-", "");
        jdbc.update("INSERT INTO actor_tokens (token, actor_id) VALUES (?, ?::uuid)",
                token, actorId.toString());
        return token;
    }

    private ObjectNode mergeEvent(UUID retiredId, UUID survivingId, UUID deviceId, long deviceSeq) {
        ObjectNode env = mapper.createObjectNode();
        env.put("id", UUID.randomUUID().toString());
        // Per envelope schema + ADR-002 Addendum: subjects_merged is a SHAPE; envelope type
        // for alias-lifecycle events is "capture".
        env.put("type", "capture");
        env.put("shape_ref", "subjects_merged/v1");
        env.putNull("activity_ref");
        ObjectNode sref = env.putObject("subject_ref");
        sref.put("type", "subject");
        sref.put("id", survivingId.toString());
        ObjectNode aref = env.putObject("actor_ref");
        aref.put("type", "actor");
        aref.put("id", "system:test_seed/alias_cycle_guard");
        env.put("device_id", deviceId.toString());
        env.put("device_seq", deviceSeq);
        env.putNull("sync_watermark");
        env.put("timestamp", OffsetDateTime.now().toString());

        ObjectNode payload = env.putObject("payload");
        payload.put("retired_id", retiredId.toString());
        payload.put("surviving_id", survivingId.toString());
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

    /** All cycle_violation flag events whose payload.source_event_id equals the supplied id. */
    private JsonNode cycleViolationFlagsFor(String sourceEventId) throws Exception {
        var rows = jdbc.queryForList(
                "SELECT id, type, shape_ref, actor_id, payload::text AS payload " +
                        "FROM events WHERE shape_ref = 'conflict_detected/v1' " +
                        "AND payload->>'flag_category' = 'cycle_violation' " +
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
