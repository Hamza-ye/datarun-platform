package dev.datarun.server.projection;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.datarun.server.AbstractIntegrationTest;
import dev.datarun.server.config.PatternRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class PatternStateProjectionTest extends AbstractIntegrationTest {

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PatternStateProjection patternStateProjection;

    @Autowired
    private PatternRegistry patternRegistry;

    @BeforeEach
    void cleanDb() {
        jdbc.execute("DELETE FROM actor_tokens");
        jdbc.execute("DELETE FROM subject_locations");
        jdbc.execute("DELETE FROM events");
        jdbc.execute("DELETE FROM activities");
        jdbc.execute("ALTER SEQUENCE events_sync_watermark_seq RESTART WITH 1");
    }

    @Test
    void projectCurrent_enabledPatternBindings_matchSharedFixture() throws Exception {
        JsonNode fixture;
        try (InputStream is = getClass().getResourceAsStream("/fixtures/pattern-state-projection.json")) {
            assertThat(is).as("Fixture file must exist on classpath").isNotNull();
            fixture = objectMapper.readTree(is);
        }

        JsonNode activities = fixture.at("/config_package/activities");
        activities.fields().forEachRemaining(entry ->
                jdbc.update("""
                        INSERT INTO activities (name, config_json, status, sensitivity)
                        VALUES (?, ?::jsonb, 'active', 'standard')
                        """,
                        entry.getKey(), entry.getValue().toString()));

        for (JsonNode event : fixture.get("events")) {
            insertEvent(event);
        }

        OffsetDateTime asOf = OffsetDateTime.parse(fixture.get("as_of").asText(),
                DateTimeFormatter.ISO_DATE_TIME.withZone(ZoneOffset.UTC));
        JsonNode actual = objectMapper.readTree(patternStateProjection.projectCurrent(asOf).toString());
        JsonNode expected = fixture.at("/expected_output/pattern_states");

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void serverFixtureMirror_matchesRootContractFixture() throws Exception {
        Path contractFixture = Paths.get("..", "contracts", "fixtures", "pattern-state-projection.json")
                .toAbsolutePath().normalize();
        Path serverFixture = Paths.get("src", "test", "resources", "fixtures", "pattern-state-projection.json")
                .toAbsolutePath().normalize();

        assertThat(contractFixture).exists();
        assertThat(serverFixture).exists();
        assertThat(normalize(Files.readString(serverFixture)))
                .isEqualTo(normalize(Files.readString(contractFixture)));
    }

    @Test
    void sharedFixturePatternDefinitions_matchRegistryPackageOutput() throws Exception {
        JsonNode fixture;
        try (InputStream is = getClass().getResourceAsStream("/fixtures/pattern-state-projection.json")) {
            assertThat(is).as("Fixture file must exist on classpath").isNotNull();
            fixture = objectMapper.readTree(is);
        }

        Set<String> refs = collectPatternRefs(fixture.at("/config_package/activities"));

        assertThat(fixture.at("/config_package/pattern_definitions"))
                .isEqualTo(patternRegistry.packageDefinitions(refs));
    }

    private void insertEvent(JsonNode event) {
        UUID id = UUID.fromString(event.get("id").asText());
        String activityRef = event.has("activity_ref") && !event.get("activity_ref").isNull()
                ? event.get("activity_ref").asText() : null;
        OffsetDateTime timestamp = OffsetDateTime.parse(event.get("timestamp").asText(),
                DateTimeFormatter.ISO_DATE_TIME.withZone(ZoneOffset.UTC));

        jdbc.update("""
                INSERT INTO events (id, type, shape_ref, activity_ref, subject_ref, actor_ref,
                                    device_id, device_seq, sync_watermark, timestamp, payload)
                VALUES (?::uuid, ?, ?, ?, ?::jsonb, ?::jsonb, ?::uuid, ?, ?, ?::timestamptz, ?::jsonb)
                """,
                id.toString(),
                event.get("type").asText(),
                event.get("shape_ref").asText(),
                activityRef,
                event.get("subject_ref").toString(),
                event.get("actor_ref").toString(),
                event.get("device_id").asText(),
                event.get("device_seq").asInt(),
                event.get("sync_watermark").asLong(),
                timestamp.toString(),
                event.get("payload").toString());
    }

    private Set<String> collectPatternRefs(JsonNode activities) {
        Set<String> refs = new LinkedHashSet<>();
        activities.fields().forEachRemaining(entry -> {
            JsonNode pattern = entry.getValue().get("pattern");
            if (pattern == null || !pattern.isObject()) {
                return;
            }
            collectPatternRef(pattern.get("subject"), refs);
            JsonNode eventBindings = pattern.get("event");
            if (eventBindings != null && eventBindings.isArray()) {
                for (JsonNode binding : eventBindings) {
                    collectPatternRef(binding, refs);
                }
            }
        });
        return refs;
    }

    private void collectPatternRef(JsonNode binding, Set<String> refs) {
        if (binding == null || !binding.isObject()) {
            return;
        }
        JsonNode ref = binding.get("ref");
        if (ref != null && ref.isTextual()) {
            refs.add(ref.asText());
        }
    }

    private static String normalize(String s) {
        while (s.endsWith("\n") || s.endsWith("\r")) {
            s = s.substring(0, s.length() - 1);
        }
        return s;
    }
}
