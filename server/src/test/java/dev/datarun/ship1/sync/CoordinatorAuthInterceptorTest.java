package dev.datarun.ship1.sync;

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
 * Integration test for {@link CoordinatorAuthInterceptor} on {@code /admin/subjects/**}.
 *
 * <p>Exercises Ship-2 §6 commitment 1 + commitment 2:
 * <ul>
 *   <li>401 on missing/unknown token</li>
 *   <li>403 on a valid CHV token whose actor lacks the {@code coordinator} role</li>
 *   <li>200 on a valid coordinator token (role established by an
 *       {@code assignment_created/v1} event with role={@code "coordinator"})</li>
 * </ul>
 */
@SpringBootTest(classes = DatarunApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class CoordinatorAuthInterceptorTest {

    @Value("${local.server.port}")
    private int port;

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private EventRepository events;

    @Autowired
    private ObjectMapper mapper;

    private final RestTemplate http = new RestTemplate();

    @BeforeEach
    void cleanDb() {
        jdbc.update("TRUNCATE events, actor_tokens, villages RESTART IDENTITY CASCADE");
        jdbc.update("ALTER SEQUENCE server_device_seq RESTART WITH 1");
    }

    private String url(String path) { return "http://localhost:" + port + path; }

    private int post(String path, String token) {
        return post(path, token, "{}");
    }

    private int post(String path, String token, String body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (token != null) headers.set("Authorization", "Bearer " + token);
        try {
            ResponseEntity<String> resp = http.exchange(
                    url(path), HttpMethod.POST, new HttpEntity<>(body, headers), String.class);
            return resp.getStatusCode().value();
        } catch (HttpClientErrorException ex) {
            return ex.getStatusCode().value();
        }
    }

    private void seedToken(String token, UUID actorId) {
        jdbc.update("INSERT INTO actor_tokens (token, actor_id) VALUES (?, ?)", token, actorId);
    }

    private void emitAssignment(UUID actorId, String role, OffsetDateTime now) {
        ObjectNode payload = mapper.createObjectNode();
        ObjectNode target = payload.putObject("target_actor");
        target.put("type", "actor");
        target.put("id", actorId.toString());
        payload.put("role", role);
        ObjectNode scope = payload.putObject("scope");
        scope.putNull("geographic");
        scope.putNull("subject_list");
        scope.putNull("activity");
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

    @Test
    void missing_authorization_header_is_401() {
        assertThat(post("/admin/subjects/merge", null)).isEqualTo(401);
    }

    @Test
    void unknown_token_is_401() {
        assertThat(post("/admin/subjects/merge", "deadbeef")).isEqualTo(401);
    }

    @Test
    void chv_token_without_coordinator_role_is_403() {
        UUID chv = UUID.randomUUID();
        String token = "chv-token-" + UUID.randomUUID();
        seedToken(token, chv);
        emitAssignment(chv, "chv", OffsetDateTime.now().minusMinutes(1));

        assertThat(post("/admin/subjects/merge", token)).isEqualTo(403);
    }

    @Test
    void coordinator_token_passes_through_to_200() {
        UUID coord = UUID.randomUUID();
        String token = "coord-token-" + UUID.randomUUID();
        seedToken(token, coord);
        emitAssignment(coord, "coordinator", OffsetDateTime.now().minusMinutes(1));

        // Valid merge body — interceptor allows through; controller emits the event and returns 200.
        String body = "{\"surviving_id\":\"" + UUID.randomUUID() + "\",\"retired_id\":\"" + UUID.randomUUID() + "\"}";
        assertThat(post("/admin/subjects/merge", token, body)).isEqualTo(200);
    }
}
