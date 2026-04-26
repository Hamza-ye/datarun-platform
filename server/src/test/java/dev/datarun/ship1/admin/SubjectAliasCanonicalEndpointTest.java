package dev.datarun.ship1.admin;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dev.datarun.ship1.DatarunApplication;
import dev.datarun.ship1.event.Event;
import dev.datarun.ship1.event.EventRepository;
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
 * Controller-level test for {@code GET /admin/subjects/{id}/canonical}
 * (Ship-2 §6 commitment 5; ADR-002 §S6 — eager transitive closure exposed at the
 * HTTP boundary). Exercises the W-3 narrative shape — does not run the full W-3
 * walkthrough (deferred to the next dispatch).
 */
@SpringBootTest(classes = DatarunApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class SubjectAliasCanonicalEndpointTest {

    @Value("${local.server.port}")
    private int port;

    @Autowired private JdbcTemplate jdbc;
    @Autowired private ObjectMapper mapper;
    @Autowired private EventRepository events;

    private final RestTemplate http = new RestTemplate();

    private UUID coordinatorId;
    private String coordinatorToken;

    @BeforeEach
    void cleanDbAndSeed() {
        jdbc.update("TRUNCATE events, actor_tokens, villages RESTART IDENTITY CASCADE");
        jdbc.update("ALTER SEQUENCE server_device_seq RESTART WITH 1");

        coordinatorId = UUID.randomUUID();
        coordinatorToken = "coord-" + UUID.randomUUID();
        jdbc.update("INSERT INTO actor_tokens (token, actor_id) VALUES (?, ?)", coordinatorToken, coordinatorId);
        emitAssignmentDirect(coordinatorId, "coordinator");
    }

    @Test
    void canonical_returns_self_when_id_is_not_retired() throws Exception {
        UUID s = UUID.randomUUID();
        HttpResp r = get("/admin/subjects/" + s + "/canonical", coordinatorToken);
        assertThat(r.status).isEqualTo(200);
        JsonNode body = mapper.readTree(r.body);
        assertThat(body.path("requested_id").asText()).isEqualTo(s.toString());
        assertThat(body.path("canonical_id").asText()).isEqualTo(s.toString());
        assertThat(body.path("alias_chain_length").asInt()).isEqualTo(0);
    }

    @Test
    void canonical_resolves_retired_to_surviving_after_merge() throws Exception {
        UUID surviving = UUID.randomUUID();
        UUID retired = UUID.randomUUID();
        // Coordinator merges retired -> surviving.
        post("/admin/subjects/merge", coordinatorToken, mergeBody(surviving, retired));

        HttpResp r = get("/admin/subjects/" + retired + "/canonical", coordinatorToken);
        assertThat(r.status).isEqualTo(200);
        JsonNode body = mapper.readTree(r.body);
        assertThat(body.path("requested_id").asText()).isEqualTo(retired.toString());
        assertThat(body.path("canonical_id").asText()).isEqualTo(surviving.toString());
        assertThat(body.path("alias_chain_length").asInt()).isEqualTo(1);

        // The historical merge event is unchanged: subject_id is the surviving id (per
        // emit() convention) and payload still carries the raw retired_id (W-3:
        // "raw, not rewritten").
        Event mergeEvent = events.findByShapeRefPrefix("subjects_merged/").get(0);
        assertThat(mergeEvent.payload().path("retired_id").asText()).isEqualTo(retired.toString());
        assertThat(mergeEvent.payload().path("surviving_id").asText()).isEqualTo(surviving.toString());
    }

    @Test
    void canonical_resolves_chain_single_hop_after_two_merges() throws Exception {
        // S_X → S_Y, then S_Y → S_Z. Eager closure: S_X resolves directly to S_Z.
        UUID sx = UUID.randomUUID();
        UUID sy = UUID.randomUUID();
        UUID sz = UUID.randomUUID();
        post("/admin/subjects/merge", coordinatorToken, mergeBody(sy, sx));   // X retired into Y
        post("/admin/subjects/merge", coordinatorToken, mergeBody(sz, sy));   // Y retired into Z

        HttpResp r = get("/admin/subjects/" + sx + "/canonical", coordinatorToken);
        assertThat(r.status).isEqualTo(200);
        JsonNode body = mapper.readTree(r.body);
        assertThat(body.path("canonical_id").asText())
                .as("S_X must resolve to S_Z directly (single hop), not to S_Y")
                .isEqualTo(sz.toString());
        // alias_chain_length reports the number of merge edges traversed: X→Y→Z = 2 hops.
        // The "single hop" guarantee is on lookup latency / consumer behaviour, not on
        // the underlying chain length — consumers do not chain-walk; the projector does.
        assertThat(body.path("alias_chain_length").asInt()).isEqualTo(2);
    }

    @Test
    void canonical_rejects_non_uuid_with_400() {
        HttpResp r = get("/admin/subjects/not-a-uuid/canonical", coordinatorToken);
        assertThat(r.status).isEqualTo(400);
    }

    @Test
    void canonical_requires_coordinator_token() {
        UUID s = UUID.randomUUID();
        HttpResp r = get("/admin/subjects/" + s + "/canonical", null);
        // 401 from CoordinatorAuthInterceptor (missing bearer).
        assertThat(r.status).isIn(401, 403);
    }

    // ----------------------------------------------------------------- helpers

    private void emitAssignmentDirect(UUID actorId, String role) {
        ObjectNode payload = mapper.createObjectNode();
        ObjectNode target = payload.putObject("target_actor");
        target.put("type", "actor");
        target.put("id", actorId.toString());
        payload.put("role", role);
        ObjectNode scope = payload.putObject("scope");
        scope.putNull("geographic");
        scope.putNull("subject_list");
        scope.putNull("activity");
        OffsetDateTime now = OffsetDateTime.now().minusMinutes(1);
        payload.put("valid_from", now.toString());
        payload.putNull("valid_to");

        Event e = new Event(
                UUID.randomUUID(),
                "assignment_changed",
                "assignment_created/v1",
                null,
                "assignment",
                UUID.randomUUID(),
                "system:test/seed",
                UUID.fromString("00000000-0000-0000-0000-0000000000ff"),
                System.nanoTime() & 0x7fffffff,
                null,
                now,
                payload);
        events.insert(e);
    }

    private record HttpResp(int status, String body) {}

    private HttpResp get(String path, String token) {
        HttpHeaders headers = new HttpHeaders();
        if (token != null) headers.set("Authorization", "Bearer " + token);
        try {
            ResponseEntity<String> resp = http.exchange(
                    "http://localhost:" + port + path, HttpMethod.GET,
                    new HttpEntity<>(headers), String.class);
            return new HttpResp(resp.getStatusCode().value(), resp.getBody());
        } catch (HttpClientErrorException ex) {
            return new HttpResp(ex.getStatusCode().value(), ex.getResponseBodyAsString());
        }
    }

    private HttpResp post(String path, String token, String body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (token != null) headers.set("Authorization", "Bearer " + token);
        try {
            ResponseEntity<String> resp = http.exchange(
                    "http://localhost:" + port + path, HttpMethod.POST,
                    new HttpEntity<>(body, headers), String.class);
            return new HttpResp(resp.getStatusCode().value(), resp.getBody());
        } catch (HttpClientErrorException ex) {
            return new HttpResp(ex.getStatusCode().value(), ex.getResponseBodyAsString());
        }
    }

    private String mergeBody(UUID surviving, UUID retired) {
        ObjectNode n = mapper.createObjectNode();
        n.put("surviving_id", surviving.toString());
        n.put("retired_id", retired.toString());
        return n.toString();
    }
}
