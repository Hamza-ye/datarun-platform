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
 * Pins ADR invariants that are enforced structurally (by absence) rather than by an explicit
 * code path. Intentionally narrow: each test exists to make a future PR that violates the
 * invariant fail loudly.
 *
 * <h3>G-10 / C2-05 — ADR-002 §S7 "no {@code SubjectsUnmerged}"</h3>
 *
 * <p>Wrong-merge correction is via multi-successor {@code subject_split} (see Ship-2 W-4),
 * never via an inverse "unmerge" operation. The invariant is enforced because no
 * {@code subjects_unmerged.schema.json} exists in {@code contracts/shapes/} or the server's
 * bundled {@code schemas/shapes/}, so {@link dev.datarun.ship1.event.ShapePayloadValidator}
 * routes any {@code subjects_unmerged/*} shape_ref to its strict-unknown reject path
 * (HTTP 400 + {@code shape_unknown:} marker).
 *
 * <p>If a future PR adds such a schema, this test fails and forces a conversation with
 * ADR-002 §S7 before the invariant is broken.
 */
@SpringBootTest(classes = DatarunApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class AdrInvariantTest {

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
     * ADR-002 §S7 — pushing an event with {@code shape_ref = "subjects_unmerged/v1"} via the
     * real HTTP surface must be rejected by {@code ShapePayloadValidator} as an unknown shape.
     *
     * <p>Asserts:
     * <ul>
     *   <li>HTTP 400 with {@code error: validation_failed};</li>
     *   <li>response details name {@code subjects_unmerged/v1} and the {@code shape_unknown:} marker;</li>
     *   <li>no row written to {@code events} (rejection is pre-persist).</li>
     * </ul>
     */
    @Test
    void subjects_unmerged_shape_ref_is_rejected_as_unknown() throws Exception {
        bootstrap();
        String adminToken = issueAdminToken();
        UUID device = UUID.randomUUID();

        ObjectNode env = mapper.createObjectNode();
        env.put("id", UUID.randomUUID().toString());
        env.put("type", "capture");
        env.put("shape_ref", "subjects_unmerged/v1");
        env.put("activity_ref", "subjects_unmerged");
        ObjectNode sref = env.putObject("subject_ref");
        sref.put("type", "subject");
        sref.put("id", UUID.randomUUID().toString());
        ObjectNode aref = env.putObject("actor_ref");
        aref.put("type", "actor");
        aref.put("id", UUID.randomUUID().toString());
        env.put("device_id", device.toString());
        env.put("device_seq", 1);
        env.putNull("sync_watermark");
        env.put("timestamp", OffsetDateTime.now().toString());
        // Payload shape is irrelevant — the validator never gets to schema-check it because
        // no schema is registered for subjects_unmerged/* (the §S7 invariant).
        ObjectNode payload = env.putObject("payload");
        payload.put("source_id", UUID.randomUUID().toString());
        payload.put("predecessor_id", UUID.randomUUID().toString());

        int eventsBefore = countAllEvents();

        HttpResponse<String> resp = push(adminToken, pushBody(env));

        assertThat(resp.statusCode)
                .describedAs("ADR-002 §S7: subjects_unmerged is structurally rejected (no schema bundled)")
                .isEqualTo(400);

        JsonNode body = mapper.readTree(resp.body);
        assertThat(body.path("error").asText()).isEqualTo("validation_failed");
        String details = body.path("details").toString();
        assertThat(details)
                .describedAs("rejection details cite the unknown shape_ref by name")
                .contains("subjects_unmerged/v1")
                .contains("shape_unknown");

        assertThat(countAllEvents())
                .describedAs("rejected event must not be persisted (pre-write reject)")
                .isEqualTo(eventsBefore);
    }

    // =========================================================================== helpers
    private String url(String path) { return "http://localhost:" + port + path; }

    private void bootstrap() {
        http.exchange(url("/dev/bootstrap"), HttpMethod.POST, HttpEntity.EMPTY, String.class);
    }

    private String issueAdminToken() {
        String token = UUID.randomUUID().toString().replace("-", "");
        jdbc.update("INSERT INTO actor_tokens (token, actor_id) VALUES (?, ?)",
                token, UUID.randomUUID());
        return token;
    }

    private ObjectNode pushBody(ObjectNode... events) {
        ObjectNode body = mapper.createObjectNode();
        ArrayNode arr = body.putArray("events");
        for (ObjectNode e : events) arr.add(e);
        return body;
    }

    private HttpResponse<String> push(String token, ObjectNode body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + token);
        try {
            ResponseEntity<String> resp = http.exchange(url("/api/sync/push"), HttpMethod.POST,
                    new HttpEntity<>(body.toString(), headers), String.class);
            return new HttpResponse<>(resp.getStatusCode().value(), resp.getBody());
        } catch (HttpClientErrorException ex) {
            return new HttpResponse<>(ex.getStatusCode().value(), ex.getResponseBodyAsString());
        }
    }

    private int countAllEvents() {
        Integer n = jdbc.queryForObject("SELECT COUNT(*) FROM events", Integer.class);
        return n == null ? 0 : n;
    }

    private record HttpResponse<T>(int statusCode, T body) {}
}
