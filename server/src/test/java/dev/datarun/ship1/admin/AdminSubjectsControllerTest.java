package dev.datarun.ship1.admin;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dev.datarun.ship1.DatarunApplication;
import dev.datarun.ship1.event.Event;
import dev.datarun.ship1.event.EventRepository;
import dev.datarun.ship1.event.ServerEmission;
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
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Controller-level tests for {@link AdminSubjectsController} merge/split (Ship-2 §6 commitments
 * 2/3/7). Covers: happy paths, §S9 pre-write rejections, structural error path, auth failure
 * passthroughs (401/403 from {@link dev.datarun.ship1.sync.CoordinatorAuthInterceptor}).
 */
@SpringBootTest(classes = DatarunApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class AdminSubjectsControllerTest {

    @Value("${local.server.port}")
    private int port;

    @Autowired private JdbcTemplate jdbc;
    @Autowired private ObjectMapper mapper;
    @Autowired private EventRepository events;

    private final RestTemplate http = new RestTemplate();

    private UUID coordinatorId;
    private String coordinatorToken;
    private UUID chvId;
    private String chvToken;

    @BeforeEach
    void cleanDbAndSeed() {
        jdbc.update("TRUNCATE events, actor_tokens, villages RESTART IDENTITY CASCADE");
        jdbc.update("ALTER SEQUENCE server_device_seq RESTART WITH 1");

        coordinatorId = UUID.randomUUID();
        coordinatorToken = "coord-" + UUID.randomUUID();
        jdbc.update("INSERT INTO actor_tokens (token, actor_id) VALUES (?, ?)", coordinatorToken, coordinatorId);
        emitAssignmentDirect(coordinatorId, "coordinator");

        chvId = UUID.randomUUID();
        chvToken = "chv-" + UUID.randomUUID();
        jdbc.update("INSERT INTO actor_tokens (token, actor_id) VALUES (?, ?)", chvToken, chvId);
        emitAssignmentDirect(chvId, "chv");
    }

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

    private String splitBody(UUID source, UUID... successors) {
        ObjectNode n = mapper.createObjectNode();
        n.put("source_id", source.toString());
        ArrayNode arr = n.putArray("successor_ids");
        for (UUID s : successors) arr.add(s.toString());
        return n.toString();
    }

    // ---------------------------------------------------------------- merge

    @Test
    void merge_happy_path_persists_event_with_server_authored_envelope() throws Exception {
        UUID surviving = UUID.randomUUID();
        UUID retired = UUID.randomUUID();
        HttpResp r = post("/admin/subjects/merge", coordinatorToken, mergeBody(surviving, retired));
        assertThat(r.status).isEqualTo(200);
        JsonNode resp = mapper.readTree(r.body);
        UUID eventId = UUID.fromString(resp.path("event_id").asText());
        assertThat(resp.path("shape_ref").asText()).isEqualTo("subjects_merged/v1");

        Event e = events.findById(eventId).orElseThrow();
        // Envelope: type=capture (F2 / vocabulary closed), server-authored device, null watermark.
        assertThat(e.type()).isEqualTo("capture");
        assertThat(e.shapeRef()).isEqualTo("subjects_merged/v1");
        assertThat(e.subjectId()).isEqualTo(surviving);
        // Actor: coordinator UUID, NOT system: prefixed (F-B4).
        assertThat(e.actorId()).isEqualTo(coordinatorId.toString());
        assertThat(e.actorId()).doesNotStartWith("system:");
        assertThat(e.deviceId()).isEqualTo(ServerEmission.SERVER_DEVICE_ID);
        assertThat(e.payload().path("surviving_id").asText()).isEqualTo(surviving.toString());
        assertThat(e.payload().path("retired_id").asText()).isEqualTo(retired.toString());
    }

    @Test
    void merge_rejects_archived_operand_with_400() {
        // Pre-archive `archivedSubject` by emitting a prior split against it.
        UUID archivedSubject = UUID.randomUUID();
        UUID s1 = UUID.randomUUID();
        UUID s2 = UUID.randomUUID();
        post("/admin/subjects/split", coordinatorToken, splitBody(archivedSubject, s1, s2));

        // Now try to merge into the archived subject.
        UUID other = UUID.randomUUID();
        HttpResp r = post("/admin/subjects/merge", coordinatorToken, mergeBody(archivedSubject, other));
        assertThat(r.status).isEqualTo(400);

        // And try to merge the archived subject as retired_id.
        HttpResp r2 = post("/admin/subjects/merge", coordinatorToken, mergeBody(other, archivedSubject));
        assertThat(r2.status).isEqualTo(400);
    }

    @Test
    void merge_rejects_self_merge() {
        UUID s = UUID.randomUUID();
        HttpResp r = post("/admin/subjects/merge", coordinatorToken, mergeBody(s, s));
        assertThat(r.status).isEqualTo(400);
    }

    @Test
    void merge_without_token_is_401() {
        HttpResp r = post("/admin/subjects/merge", null,
                mergeBody(UUID.randomUUID(), UUID.randomUUID()));
        assertThat(r.status).isEqualTo(401);
    }

    @Test
    void merge_with_chv_token_is_403() {
        HttpResp r = post("/admin/subjects/merge", chvToken,
                mergeBody(UUID.randomUUID(), UUID.randomUUID()));
        assertThat(r.status).isEqualTo(403);
    }

    // ---------------------------------------------------------------- split

    @Test
    void split_happy_path_with_two_successors() throws Exception {
        UUID source = UUID.randomUUID();
        UUID s1 = UUID.randomUUID();
        UUID s2 = UUID.randomUUID();
        HttpResp r = post("/admin/subjects/split", coordinatorToken, splitBody(source, s1, s2));
        assertThat(r.status).isEqualTo(200);
        JsonNode resp = mapper.readTree(r.body);
        UUID eventId = UUID.fromString(resp.path("event_id").asText());

        Event e = events.findById(eventId).orElseThrow();
        assertThat(e.type()).isEqualTo("capture");
        assertThat(e.shapeRef()).isEqualTo("subject_split/v1");
        assertThat(e.subjectId()).isEqualTo(source);
        assertThat(e.actorId()).isEqualTo(coordinatorId.toString());
        assertThat(e.deviceId()).isEqualTo(ServerEmission.SERVER_DEVICE_ID);

        JsonNode successors = e.payload().path("successor_ids");
        assertThat(successors.isArray()).isTrue();
        assertThat(successors.size()).isEqualTo(2);
    }

    @Test
    void split_rejects_archived_source() {
        UUID source = UUID.randomUUID();
        UUID s1 = UUID.randomUUID();
        UUID s2 = UUID.randomUUID();
        post("/admin/subjects/split", coordinatorToken, splitBody(source, s1, s2));

        // Re-split same source — now archived.
        HttpResp r = post("/admin/subjects/split", coordinatorToken,
                splitBody(source, UUID.randomUUID(), UUID.randomUUID()));
        assertThat(r.status).isEqualTo(400);
    }

    @Test
    void split_rejects_single_successor() {
        UUID source = UUID.randomUUID();
        HttpResp r = post("/admin/subjects/split", coordinatorToken, splitBody(source, UUID.randomUUID()));
        assertThat(r.status).isEqualTo(400);
    }

    @Test
    void split_without_token_is_401() {
        HttpResp r = post("/admin/subjects/split", null,
                splitBody(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID()));
        assertThat(r.status).isEqualTo(401);
    }

    @Test
    void split_with_chv_token_is_403() {
        HttpResp r = post("/admin/subjects/split", chvToken,
                splitBody(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID()));
        assertThat(r.status).isEqualTo(403);
    }
}
