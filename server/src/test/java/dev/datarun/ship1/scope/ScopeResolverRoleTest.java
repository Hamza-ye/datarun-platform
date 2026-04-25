package dev.datarun.ship1.scope;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dev.datarun.ship1.event.Event;
import dev.datarun.ship1.event.EventRepository;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit test for {@link ScopeResolver#hasRoleAt(String, String, OffsetDateTime)} —
 * Ship-2 §6 commitment 1: coordinator recognition is a projection over
 * {@code assignment_created/v1} events, not a column or table.
 */
class ScopeResolverRoleTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void hasRoleAt_isFalseBeforeValidFrom_andTrueAfter() {
        EventRepository events = mock(EventRepository.class);
        ScopeResolver resolver = new ScopeResolver(events);

        UUID actorId = UUID.randomUUID();
        OffsetDateTime validFrom = OffsetDateTime.parse("2026-04-25T10:00:00Z");

        ObjectNode payload = mapper.createObjectNode();
        ObjectNode target = payload.putObject("target_actor");
        target.put("type", "actor");
        target.put("id", actorId.toString());
        payload.put("role", "coordinator");
        ObjectNode scope = payload.putObject("scope");
        scope.putNull("geographic");
        scope.putNull("subject_list");
        scope.putNull("activity");
        payload.put("valid_from", validFrom.toString());
        payload.putNull("valid_to");

        Event assignment = new Event(
                UUID.randomUUID(),
                "assignment_changed",
                "assignment_created/v1",
                null,
                "assignment",
                UUID.randomUUID(),
                "system:dev_bootstrap/seed",
                UUID.randomUUID(),
                1L,
                1L,
                validFrom,
                payload);

        when(events.findByShapeRefPrefix("assignment_created/")).thenReturn(List.of(assignment));

        assertThat(resolver.hasRoleAt(actorId.toString(), "coordinator", validFrom.minusSeconds(1)))
                .as("role check before valid_from is false")
                .isFalse();
        assertThat(resolver.hasRoleAt(actorId.toString(), "coordinator", validFrom.plusSeconds(1)))
                .as("role check after valid_from is true")
                .isTrue();
        assertThat(resolver.hasRoleAt(actorId.toString(), "chv", validFrom.plusSeconds(1)))
                .as("different role does not match")
                .isFalse();
        assertThat(resolver.hasRoleAt(UUID.randomUUID().toString(), "coordinator", validFrom.plusSeconds(1)))
                .as("different actor does not match")
                .isFalse();
    }
}
