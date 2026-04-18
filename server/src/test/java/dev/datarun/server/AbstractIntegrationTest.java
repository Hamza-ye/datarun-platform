package dev.datarun.server;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.util.UUID;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public abstract class AbstractIntegrationTest {

    /**
     * Default test actor ID used across test classes.
     */
    protected static final UUID TEST_ACTOR_ID = UUID.fromString("f47ac10b-58cc-4372-a567-0e02b2c3d479");

    /**
     * Fixed test token for the default actor. Provisioned by {@link #provisionTestToken()}.
     */
    protected static final String TEST_TOKEN = "test_token_for_default_actor_00000000000000000000000000000000";

    @Autowired
    protected JdbcTemplate jdbcTemplate;

    /**
     * Provision a test actor token. Call this in @BeforeEach after cleaning the DB.
     * Idempotent — uses ON CONFLICT DO NOTHING.
     * Also provisions a root-scope assignment so pull returns all events.
     */
    protected void provisionTestToken() {
        jdbcTemplate.update("""
                INSERT INTO actor_tokens (token, actor_id, revoked)
                VALUES (?, ?::uuid, FALSE)
                ON CONFLICT (token) DO NOTHING
                """, TEST_TOKEN, TEST_ACTOR_ID.toString());

        // Create a root-scope assignment so scoped pull sees all events.
        // Uses sync_watermark = 0 so it doesn't appear in pull results (pull uses > watermark).
        UUID serverDeviceId = jdbcTemplate.queryForObject(
                "SELECT device_id FROM server_identity LIMIT 1", UUID.class);
        long seq = jdbcTemplate.queryForObject(
                "SELECT nextval('server_device_seq')", Long.class);
        UUID assignmentId = UUID.fromString("00000000-0000-0000-0000-aaaaaaaaaaaa");
        UUID eventId = UUID.fromString("00000000-0000-0000-0000-bbbbbbbbbbbb");

        jdbcTemplate.update("""
                INSERT INTO events (id, type, shape_ref, activity_ref, subject_ref, actor_ref,
                                    device_id, device_seq, sync_watermark, timestamp, payload)
                VALUES (?::uuid, 'assignment_changed', 'assignment_created/v1', NULL,
                        ?::jsonb, ?::jsonb, ?::uuid, ?, 0, NOW()::timestamptz, ?::jsonb)
                ON CONFLICT (id) DO NOTHING
                """,
                eventId.toString(),
                "{\"type\":\"assignment\",\"id\":\"" + assignmentId + "\"}",
                "{\"type\":\"actor\",\"id\":\"" + TEST_ACTOR_ID + "\"}",
                serverDeviceId.toString(),
                seq,
                "{\"target_actor\":{\"type\":\"actor\",\"id\":\"" + TEST_ACTOR_ID + "\"}," +
                "\"role\":\"admin\",\"scope\":{\"geographic\":null,\"subject_list\":null,\"activity\":null}," +
                "\"valid_from\":\"2026-01-01T00:00:00Z\",\"valid_to\":null}");
    }

    /**
     * Get HTTP headers with the test actor's Bearer token.
     */
    protected HttpHeaders authHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + TEST_TOKEN);
        headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);
        return headers;
    }
}
