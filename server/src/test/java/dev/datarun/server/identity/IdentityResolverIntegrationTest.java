package dev.datarun.server.identity;

import com.fasterxml.jackson.databind.JsonNode;
import dev.datarun.server.AbstractIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Phase 1b quality gate tests — Identity Resolver.
 * Tests map 1:1 to the 8 quality gates in phase-1.md §3 (Phase 1b).
 */
class IdentityResolverIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private TestRestTemplate rest;

    @Autowired
    private JdbcTemplate jdbc;

    private static final UUID DEVICE_A = UUID.fromString("a1b2c3d4-e5f6-7890-abcd-ef1234567890");
    private static final UUID DEVICE_B = UUID.fromString("b2c3d4e5-f6a7-8901-bcde-f12345678901");
    private static final UUID ACTOR_ID = UUID.fromString("f47ac10b-58cc-4372-a567-0e02b2c3d479");

    @BeforeEach
    void cleanDb() {
        jdbc.execute("DELETE FROM actor_tokens");
        jdbc.execute("DELETE FROM subject_locations");
        jdbc.execute("DELETE FROM events");
        jdbc.execute("ALTER SEQUENCE events_sync_watermark_seq RESTART WITH 1");
        jdbc.execute("DELETE FROM device_sync_state");
        jdbc.execute("DELETE FROM subject_aliases");
        jdbc.execute("DELETE FROM subject_lifecycle");
        provisionTestToken();
    }

    // --- QG1: Merge A and B → alias created → GET /api/subjects shows one unified subject ---

    @Test
    void mergeSubjects_aliasCreated_unifiedInProjection() {
        UUID subjectA = UUID.randomUUID();
        UUID subjectB = UUID.randomUUID();

        // Create events for both subjects
        pushEvents(List.of(buildEvent(subjectA, DEVICE_A, 1)), DEVICE_A);
        pushEvents(List.of(buildEvent(subjectA, DEVICE_A, 2)), DEVICE_A);
        pushEvents(List.of(buildEvent(subjectB, DEVICE_B, 1)), DEVICE_B);

        // Before merge: 2 subjects
        var beforeMerge = rest.getForEntity("/api/subjects", JsonNode.class);
        assertThat(beforeMerge.getBody().get("subjects").size()).isEqualTo(2);

        // Merge B into A
        var mergeResponse = merge(subjectB, subjectA);
        assertThat(mergeResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(mergeResponse.getBody().get("surviving_id").asText()).isEqualTo(subjectA.toString());

        // After merge: 1 unified subject with events from both
        var afterMerge = rest.getForEntity("/api/subjects", JsonNode.class);
        JsonNode subjects = afterMerge.getBody().get("subjects");
        assertThat(subjects.size()).isEqualTo(1);
        assertThat(subjects.get(0).get("id").asText()).isEqualTo(subjectA.toString());
        // 3 domain events total (2 from A + 1 from B)
        assertThat(subjects.get(0).get("event_count").asInt()).isEqualTo(3);

        // GET events for surviving ID includes events from both
        var eventsResponse = rest.getForEntity("/api/subjects/" + subjectA + "/events", JsonNode.class);
        assertThat(eventsResponse.getBody().get("events").size()).isGreaterThanOrEqualTo(3);
    }

    // --- QG2: Merge A→B, then B→C → alias table: A→C, B→C → single-hop ---

    @Test
    void transitivemerge_singleHopAlias() {
        UUID subjectA = UUID.randomUUID();
        UUID subjectB = UUID.randomUUID();
        UUID subjectC = UUID.randomUUID();

        // Create events
        pushEvents(List.of(buildEvent(subjectA, DEVICE_A, 1)), DEVICE_A);
        pushEvents(List.of(buildEvent(subjectB, DEVICE_A, 2)), DEVICE_A);
        pushEvents(List.of(buildEvent(subjectC, DEVICE_A, 3)), DEVICE_A);

        // Merge A into B
        var merge1 = merge(subjectA, subjectB);
        assertThat(merge1.getStatusCode()).isEqualTo(HttpStatus.OK);

        // Merge B into C (transitive — A should now point to C)
        var merge2 = merge(subjectB, subjectC);
        assertThat(merge2.getStatusCode()).isEqualTo(HttpStatus.OK);

        // Verify alias table: A→C (single hop), B→C
        String aTarget = jdbc.queryForObject(
                "SELECT surviving_id::text FROM subject_aliases WHERE retired_id = ?::uuid",
                String.class, subjectA.toString());
        String bTarget = jdbc.queryForObject(
                "SELECT surviving_id::text FROM subject_aliases WHERE retired_id = ?::uuid",
                String.class, subjectB.toString());
        assertThat(aTarget).isEqualTo(subjectC.toString()); // Transitive: A→C not A→B
        assertThat(bTarget).isEqualTo(subjectC.toString()); // Direct: B→C

        // Unified subject list shows only C with all 3 events
        var subjectsResponse = rest.getForEntity("/api/subjects", JsonNode.class);
        JsonNode subjects = subjectsResponse.getBody().get("subjects");
        assertThat(subjects.size()).isEqualTo(1);
        assertThat(subjects.get(0).get("id").asText()).isEqualTo(subjectC.toString());
        assertThat(subjects.get(0).get("event_count").asInt()).isEqualTo(3);
    }

    // --- QG3: Split subject → source archived → successor created → historical events stay ---

    @Test
    void splitSubject_sourceArchived_historicalEventsStay() {
        UUID sourceId = UUID.randomUUID();

        // Create events for source
        pushEvents(List.of(buildEvent(sourceId, DEVICE_A, 1)), DEVICE_A);
        pushEvents(List.of(buildEvent(sourceId, DEVICE_A, 2)), DEVICE_A);

        // Split
        var splitResponse = split(sourceId);
        assertThat(splitResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        String successorId = splitResponse.getBody().get("successor_id").asText();
        assertThat(successorId).isNotEmpty();

        // Source is archived
        String state = jdbc.queryForObject(
                "SELECT state FROM subject_lifecycle WHERE subject_id = ?::uuid",
                String.class, sourceId.toString());
        assertThat(state).isEqualTo("archived");

        // Historical events still belong to source
        var sourceEvents = rest.getForEntity("/api/subjects/" + sourceId + "/events", JsonNode.class);
        int domainEventCount = 0;
        for (JsonNode e : sourceEvents.getBody().get("events")) {
            // Domain captures — excludes system-authored identity lifecycle (subject_split/v1 etc.).
            if ("basic_capture/v1".equals(e.get("shape_ref").asText())) domainEventCount++;
        }
        assertThat(domainEventCount).isEqualTo(2);

        // Successor has no events yet (it's a new subject)
        var successorEvents = rest.getForEntity("/api/subjects/" + successorId + "/events", JsonNode.class);
        assertThat(successorEvents.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    // --- QG4: Archived subject cannot be merge target ---

    @Test
    void archivedSubject_cannotBeMergeTarget() {
        UUID subjectA = UUID.randomUUID();
        UUID subjectB = UUID.randomUUID();
        UUID subjectC = UUID.randomUUID();

        pushEvents(List.of(buildEvent(subjectA, DEVICE_A, 1)), DEVICE_A);
        pushEvents(List.of(buildEvent(subjectB, DEVICE_A, 2)), DEVICE_A);
        pushEvents(List.of(buildEvent(subjectC, DEVICE_A, 3)), DEVICE_A);

        // Merge A into B (A becomes archived)
        merge(subjectA, subjectB);

        // Try to merge C into A (A is archived — should fail)
        var response = merge(subjectC, subjectA);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().get("error").asText()).isEqualTo("precondition_failed");

        // Try to merge A into C (A is archived — should fail as retired too)
        var response2 = merge(subjectA, subjectC);
        assertThat(response2.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response2.getBody().get("error").asText()).isEqualTo("precondition_failed");
    }

    // --- QG5: Archived subject cannot be split again ---

    @Test
    void archivedSubject_cannotBeSplitAgain() {
        UUID sourceId = UUID.randomUUID();

        pushEvents(List.of(buildEvent(sourceId, DEVICE_A, 1)), DEVICE_A);

        // Split once
        var split1 = split(sourceId);
        assertThat(split1.getStatusCode()).isEqualTo(HttpStatus.OK);

        // Try to split again — should fail (archived)
        var split2 = split(sourceId);
        assertThat(split2.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(split2.getBody().get("error").asText()).isEqualTo("precondition_failed");
    }

    // --- QG6: Event pushed for retired subject ID → stale_reference flag raised ---

    @Test
    void eventForRetiredSubject_staleReferenceFlag() {
        UUID subjectA = UUID.randomUUID();
        UUID subjectB = UUID.randomUUID();

        // Create initial events
        pushEvents(List.of(buildEvent(subjectA, DEVICE_A, 1)), DEVICE_A);
        pushEvents(List.of(buildEvent(subjectB, DEVICE_A, 2)), DEVICE_A);

        // Merge A into B (A is now retired)
        merge(subjectA, subjectB);

        // Push event referencing retired subject A
        var staleEvent = buildEvent(subjectA, DEVICE_B, 1);
        var response = pushEvents(List.of(staleEvent), DEVICE_B);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().get("accepted").asInt()).isEqualTo(1);
        assertThat(response.getBody().get("flags_raised").asInt()).isGreaterThanOrEqualTo(1);

        // Verify stale_reference flag exists
        Integer staleFlags = jdbc.queryForObject("""
                SELECT COUNT(*) FROM events
                WHERE shape_ref LIKE 'conflict_detected/%'
                  AND payload->>'flag_category' = 'stale_reference'
                  AND subject_ref->>'id' = ?
                """,
                Integer.class, subjectA.toString());
        assertThat(staleFlags).isGreaterThan(0);
    }

    // --- QG7: Contract C7 — merge creates alias → transitive closure → PE re-attributes ---

    @Test
    void contractC7_mergeCreatesAlias_peReattributes() {
        UUID subjectA = UUID.randomUUID();
        UUID subjectB = UUID.randomUUID();

        // Events for A
        pushEvents(List.of(
                buildEvent(subjectA, DEVICE_A, 1),
                buildEvent(subjectA, DEVICE_A, 2)), DEVICE_A);
        // Events for B
        pushEvents(List.of(buildEvent(subjectB, DEVICE_B, 1)), DEVICE_B);

        // Before merge: A has 2 events, B has 1
        var subjectsBefore = rest.getForEntity("/api/subjects", JsonNode.class);
        assertThat(subjectsBefore.getBody().get("subjects").size()).isEqualTo(2);

        // Merge A into B
        merge(subjectA, subjectB);

        // After merge: PE re-attributes A's events to B
        var subjectsAfter = rest.getForEntity("/api/subjects", JsonNode.class);
        JsonNode subjects = subjectsAfter.getBody().get("subjects");
        assertThat(subjects.size()).isEqualTo(1);
        // B now has 3 events (2 from A + 1 from B)
        assertThat(subjects.get(0).get("id").asText()).isEqualTo(subjectB.toString());
        assertThat(subjects.get(0).get("event_count").asInt()).isEqualTo(3);

        // GET events by canonical ID returns all events from alias chain
        var eventsResponse = rest.getForEntity("/api/subjects/" + subjectB + "/events", JsonNode.class);
        int domainEvents = 0;
        for (JsonNode e : eventsResponse.getBody().get("events")) {
            // Domain captures — excludes system-authored subjects_merged/v1.
            if ("basic_capture/v1".equals(e.get("shape_ref").asText())) domainEvents++;
        }
        assertThat(domainEvents).isEqualTo(3);
    }

    // --- QG8: Contract C4 — PE provides alias-resolved state to CD ---

    @Test
    void contractC4_peAliasResolved_cdUsesAliasState() {
        UUID subjectA = UUID.randomUUID();
        UUID subjectB = UUID.randomUUID();

        // Device A pushes to subject A
        pushEvents(List.of(buildEvent(subjectA, DEVICE_A, 1)), DEVICE_A);
        // Device A pushes to subject B
        pushEvents(List.of(buildEvent(subjectB, DEVICE_A, 2)), DEVICE_A);

        // Merge A into B
        merge(subjectA, subjectB);

        // Now Device B pushes to the RETIRED subject A (concurrent with merged state)
        // CD should use alias-resolved state: the event's subject is A (retired),
        // and the alias-resolved canonical is B.
        // The stale_reference flag should fire (retired ID).
        var staleEvent = buildEvent(subjectA, DEVICE_B, 1);
        var response = pushEvents(List.of(staleEvent), DEVICE_B);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().get("accepted").asInt()).isEqualTo(1);

        // At minimum, stale_reference should be raised (CD detects retired ID)
        Integer flags = jdbc.queryForObject("""
                SELECT COUNT(*) FROM events
                WHERE shape_ref LIKE 'conflict_detected/%'
                  AND payload->>'flag_category' = 'stale_reference'
                """, Integer.class);
        assertThat(flags).isGreaterThan(0);

        // The projection still shows B as a unified subject with all events
        var subjects = rest.getForEntity("/api/subjects", JsonNode.class);
        // Should have 1 subject (B, the canonical). A's events are re-attributed.
        // The stale event for retired A should also appear under B's alias chain.
        boolean foundCanonical = false;
        for (JsonNode s : subjects.getBody().get("subjects")) {
            if (s.get("id").asText().equals(subjectB.toString())) {
                foundCanonical = true;
            }
        }
        assertThat(foundCanonical).isTrue();
    }

    // --- Helpers ---

    private Map<String, Object> buildEvent(UUID subjectId, UUID deviceId, int seq) {
        Map<String, Object> event = new LinkedHashMap<>();
        event.put("id", UUID.randomUUID().toString());
        event.put("type", "capture");
        event.put("shape_ref", "basic_capture/v1");
        event.put("activity_ref", null);
        event.put("subject_ref", Map.of("type", "subject", "id", subjectId.toString()));
        event.put("actor_ref", Map.of("type", "actor", "id", ACTOR_ID.toString()));
        event.put("device_id", deviceId.toString());
        event.put("device_seq", seq);
        event.put("sync_watermark", null);
        event.put("timestamp", OffsetDateTime.now(ZoneOffset.UTC).toString());
        event.put("payload", Map.of(
                "name", "Site " + seq,
                "category", "urban",
                "notes", "Test event " + seq,
                "date", "2026-04-16",
                "value", seq * 10
        ));
        return event;
    }

    private ResponseEntity<JsonNode> pushEvents(List<?> events, UUID deviceId) {
        Map<String, Object> request = new LinkedHashMap<>();
        request.put("events", events);
        request.put("device_id", deviceId.toString());
        request.put("last_pull_watermark", 0);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);
        return rest.exchange("/api/sync/push", HttpMethod.POST, entity, JsonNode.class);
    }

    private ResponseEntity<JsonNode> merge(UUID retiredId, UUID survivingId) {
        Map<String, Object> request = Map.of(
                "retired_id", retiredId.toString(),
                "surviving_id", survivingId.toString(),
                "actor_id", ACTOR_ID.toString(),
                "reason", "Test merge"
        );
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);
        return rest.exchange("/api/identity/merge", HttpMethod.POST, entity, JsonNode.class);
    }

    private ResponseEntity<JsonNode> split(UUID sourceId) {
        Map<String, Object> request = Map.of(
                "source_id", sourceId.toString(),
                "actor_id", ACTOR_ID.toString(),
                "reason", "Test split"
        );
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);
        return rest.exchange("/api/identity/split", HttpMethod.POST, entity, JsonNode.class);
    }
}
