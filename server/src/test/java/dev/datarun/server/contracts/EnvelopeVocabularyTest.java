package dev.datarun.server.contracts;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.datarun.server.event.EnvelopeValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Phase 3e contract test: the envelope type vocabulary is closed.
 *
 * ADR-002 Addendum (2026-04-21) collapses identity and integrity events from
 * rogue envelope types into the 6 canonical process types, emitted under
 * dedicated shape names. This test guards that collapse: any attempt to
 * reintroduce the old type strings at the envelope level must be rejected.
 */
@SpringBootTest
@ActiveProfiles("test")
class EnvelopeVocabularyTest {

    @Autowired
    private EnvelopeValidator validator;

    @Autowired
    private ObjectMapper mapper;

    @ParameterizedTest
    @ValueSource(strings = {
            "conflict_detected",
            "conflict_resolved",
            "subjects_merged",
            "subject_split"
    })
    void oldShapeNameUsedAsType_rejected(String bannedType) throws Exception {
        String json = """
                {
                  "id": "11111111-1111-4111-8111-111111111111",
                  "type": "%s",
                  "shape_ref": "anything/v1",
                  "subject_ref": {"type":"subject","id":"22222222-2222-4222-8222-222222222222"},
                  "actor_ref":   {"type":"actor","id":"33333333-3333-4333-8333-333333333333"},
                  "device_id": "44444444-4444-4444-8444-444444444444",
                  "device_seq": 1,
                  "timestamp": "2026-04-21T10:00:00Z",
                  "payload": {}
                }
                """.formatted(bannedType);
        JsonNode envelope = mapper.readTree(json);
        List<String> errors = validator.validate(envelope);
        assertThat(errors)
                .as("Envelope must reject the drift type '%s' \u2014 it is a shape name, not a process type",
                        bannedType)
                .isNotEmpty();
    }

    @Test
    void canonicalTypes_accepted() throws Exception {
        for (String type : List.of("capture", "review", "alert", "task_created",
                "task_completed", "assignment_changed")) {
            String json = """
                    {
                      "id": "11111111-1111-4111-8111-111111111111",
                      "type": "%s",
                      "shape_ref": "basic_capture/v1",
                      "subject_ref": {"type":"subject","id":"22222222-2222-4222-8222-222222222222"},
                      "actor_ref":   {"type":"actor","id":"33333333-3333-4333-8333-333333333333"},
                      "device_id": "44444444-4444-4444-8444-444444444444",
                      "device_seq": 1,
                      "timestamp": "2026-04-21T10:00:00Z",
                      "payload": {}
                    }
                    """.formatted(type);
            List<String> errors = validator.validate(mapper.readTree(json));
            assertThat(errors)
                    .as("Canonical type '%s' must validate", type)
                    .isEmpty();
        }
    }
}
