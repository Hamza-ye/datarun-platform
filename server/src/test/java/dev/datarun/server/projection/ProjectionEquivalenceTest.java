package dev.datarun.server.projection;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.datarun.server.AbstractIntegrationTest;
import dev.datarun.server.identity.AliasCache;
import dev.datarun.server.subject.SubjectProjection;
import dev.datarun.server.subject.SubjectSummary;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.io.InputStream;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Projection equivalence test (E7): server PE must produce byte-identical
 * canonical output to Dart PE given the same ordered event set.
 *
 * Loads shared fixture from contracts/fixtures/projection-equivalence.json.
 * Both Java and Dart tests compare to the same expected_output.
 */
class ProjectionEquivalenceTest extends AbstractIntegrationTest {

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private SubjectProjection subjectProjection;

    @Autowired
    private AliasCache aliasCache;

    @BeforeEach
    void cleanDb() {
        jdbc.execute("DELETE FROM events");
        jdbc.execute("DELETE FROM subject_aliases");
        jdbc.execute("DELETE FROM subject_lifecycle");
        jdbc.execute("ALTER SEQUENCE events_sync_watermark_seq RESTART WITH 1");
    }

    @Test
    void projectionEquivalence_sameFixture_producesExpectedOutput() throws Exception {
        // Load the shared fixture
        JsonNode fixture;
        try (InputStream is = getClass().getResourceAsStream("/fixtures/projection-equivalence.json")) {
            assertThat(is).as("Fixture file must exist on classpath").isNotNull();
            fixture = objectMapper.readTree(is);
        }

        // Insert events in order (with pre-assigned sync_watermarks)
        JsonNode events = fixture.get("events");
        for (JsonNode event : events) {
            insertEvent(event);
        }

        // Insert aliases
        JsonNode aliases = fixture.get("aliases");
        for (JsonNode alias : aliases) {
            insertAlias(alias);
        }

        // Refresh alias cache so PE picks them up
        aliasCache.refresh();

        // Run server PE
        List<SubjectSummary> subjects = subjectProjection.listSubjects();

        // Extract canonical output
        JsonNode expectedOutput = fixture.get("expected_output");
        JsonNode expectedSubjects = expectedOutput.get("subjects");

        assertThat(subjects).hasSize(expectedSubjects.size());

        for (int i = 0; i < expectedSubjects.size(); i++) {
            JsonNode expected = expectedSubjects.get(i);
            SubjectSummary actual = subjects.get(i);

            assertThat(actual.id())
                    .as("subject[%d].subject_id", i)
                    .isEqualTo(expected.get("subject_id").asText());

            assertThat(actual.eventCount())
                    .as("subject[%d].event_count", i)
                    .isEqualTo(expected.get("event_count").asInt());

            assertThat(actual.flagCount())
                    .as("subject[%d].flag_count", i)
                    .isEqualTo(expected.get("flag_count").asInt());

            // Compare timestamps — both should represent the same instant
            String expectedTs = expected.get("latest_timestamp").asText();
            OffsetDateTime expectedDt = OffsetDateTime.parse(expectedTs,
                    DateTimeFormatter.ISO_DATE_TIME.withZone(ZoneOffset.UTC));
            assertThat(actual.latestTimestamp().toInstant())
                    .as("subject[%d].latest_timestamp", i)
                    .isEqualTo(expectedDt.toInstant());
        }
    }

    private void insertEvent(JsonNode event) {
        UUID id = UUID.fromString(event.get("id").asText());
        String type = event.get("type").asText();
        String shapeRef = event.get("shape_ref").asText();
        String activityRef = event.has("activity_ref") && !event.get("activity_ref").isNull()
                ? event.get("activity_ref").asText() : null;
        JsonNode subjectRef = event.get("subject_ref");
        JsonNode actorRef = event.get("actor_ref");
        UUID deviceId = UUID.fromString(event.get("device_id").asText());
        int deviceSeq = event.get("device_seq").asInt();
        long syncWatermark = event.get("sync_watermark").asLong();
        OffsetDateTime timestamp = OffsetDateTime.parse(event.get("timestamp").asText(),
                DateTimeFormatter.ISO_DATE_TIME.withZone(ZoneOffset.UTC));
        JsonNode payload = event.get("payload");

        // Insert directly with pre-assigned sync_watermark (bypass sequence)
        jdbc.update("""
                INSERT INTO events (id, type, shape_ref, activity_ref, subject_ref, actor_ref,
                                    device_id, device_seq, sync_watermark, timestamp, payload)
                VALUES (?::uuid, ?, ?, ?, ?::jsonb, ?::jsonb, ?::uuid, ?, ?, ?::timestamptz, ?::jsonb)
                """,
                id.toString(), type, shapeRef, activityRef,
                subjectRef.toString(), actorRef.toString(),
                deviceId.toString(), deviceSeq, syncWatermark,
                timestamp.toString(), payload.toString());
    }

    private void insertAlias(JsonNode alias) {
        String retiredId = alias.get("retired_id").asText();
        String survivingId = alias.get("surviving_id").asText();
        jdbc.update("""
                INSERT INTO subject_aliases (retired_id, surviving_id, merged_at)
                VALUES (?::uuid, ?::uuid, NOW())
                ON CONFLICT (retired_id) DO NOTHING
                """, retiredId, survivingId);
    }
}
