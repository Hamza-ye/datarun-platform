package dev.datarun.ship1.admin;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.datarun.ship1.DatarunApplication;
import dev.datarun.ship1.event.Event;
import dev.datarun.ship1.event.EventRepository;
import dev.datarun.ship1.event.ShapePayloadValidator;
import dev.datarun.ship1.scope.ScopeResolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestTemplate;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test for Ship-2 §6 commitment 1: dev_bootstrap seeds a coordinator actor +
 * bearer token and emits ONE {@code assignment_created/v1} event with {@code role="coordinator"}
 * and all-null scope dimensions. ScopeResolver's projection then reports the actor as a
 * coordinator at {@code now}.
 */
@SpringBootTest(classes = DatarunApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class DevBootstrapCoordinatorTest {

    @Value("${local.server.port}")
    private int port;

    @Autowired private JdbcTemplate jdbc;
    @Autowired private ObjectMapper mapper;
    @Autowired private EventRepository events;
    @Autowired private ScopeResolver scopes;
    @Autowired private ShapePayloadValidator shapeValidator;

    private final RestTemplate http = new RestTemplate();

    @BeforeEach
    void cleanDb() {
        jdbc.update("TRUNCATE events, actor_tokens, villages RESTART IDENTITY CASCADE");
        jdbc.update("ALTER SEQUENCE server_device_seq RESTART WITH 1");
    }

    @Test
    void bootstrap_seeds_coordinator_and_emits_role_assignment() throws Exception {
        ResponseEntity<String> resp = http.exchange(
                "http://localhost:" + port + "/dev/bootstrap",
                HttpMethod.POST, HttpEntity.EMPTY, String.class);
        assertThat(resp.getStatusCode().value()).isEqualTo(200);
        JsonNode body = mapper.readTree(resp.getBody());

        // Response carries coordinator id + token.
        UUID coordinatorId = UUID.fromString(body.path("coordinator_actor_id").asText());
        String coordinatorToken = body.path("coordinator_token").asText();
        assertThat(coordinatorToken).hasSize(64); // 32 bytes hex

        // Token resolves to coordinator actor id.
        UUID resolved = jdbc.queryForObject(
                "SELECT actor_id FROM actor_tokens WHERE token = ?", UUID.class, coordinatorToken);
        assertThat(resolved).isEqualTo(coordinatorId);

        // Exactly one assignment_created/v1 event for the coordinator with role=coordinator
        // and all-null scope dimensions.
        List<Event> all = events.findByShapeRefPrefix("assignment_created/");
        long count = all.stream()
                .filter(e -> coordinatorId.toString().equals(e.payload().path("target_actor").path("id").asText()))
                .count();
        assertThat(count).isEqualTo(1);

        Event coordEvent = all.stream()
                .filter(e -> coordinatorId.toString().equals(e.payload().path("target_actor").path("id").asText()))
                .findFirst().orElseThrow();
        JsonNode payload = coordEvent.payload();
        assertThat(payload.path("role").asText()).isEqualTo("coordinator");
        assertThat(payload.path("scope").path("geographic").isNull()).isTrue();
        assertThat(payload.path("scope").path("subject_list").isNull()).isTrue();
        assertThat(payload.path("scope").path("activity").isNull()).isTrue();

        // §6 commitment 1: payload must validate against the platform-bundled
        // assignment_created/v1 schema (the schema already permits all-null scope).
        List<String> errors = shapeValidator.validate("assignment_created/v1", payload);
        assertThat(errors).as("all-null-scope coordinator assignment payload validates").isEmpty();

        // ScopeResolver projection reports the actor as a coordinator at now.
        assertThat(scopes.hasRoleAt(coordinatorId.toString(), "coordinator", OffsetDateTime.now()))
                .isTrue();
    }
}
