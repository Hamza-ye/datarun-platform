package dev.datarun.ship1.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dev.datarun.ship1.event.Event;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link SubjectAliasProjector}'s pure-function fold
 * (Ship-2 §6 commitment 5; ADR-002 §S6 — eager transitive closure,
 * order-independent under sync_watermark partial order).
 */
class SubjectAliasProjectorTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void simple_alias_A_to_B_resolves_to_B() {
        UUID a = UUID.randomUUID(), b = UUID.randomUUID();
        Map<UUID, UUID> map = SubjectAliasProjector.buildAliasMap(List.of(merge(a, b, 1L)));
        assertThat(map.get(a)).isEqualTo(b);
        assertThat(map).doesNotContainKey(b);
    }

    @Test
    void chain_A_to_B_to_C_resolves_A_to_C_single_hop() {
        UUID a = UUID.randomUUID(), b = UUID.randomUUID(), c = UUID.randomUUID();
        Map<UUID, UUID> map = SubjectAliasProjector.buildAliasMap(List.of(
                merge(a, b, 1L),
                merge(b, c, 2L)));
        // Eager closure: A→C directly (not A→B); §S6.
        assertThat(map.get(a)).isEqualTo(c);
        assertThat(map.get(b)).isEqualTo(c);
        assertThat(map).doesNotContainKey(c);
    }

    @Test
    void unmerged_id_resolves_to_itself_via_canonicalId() {
        // canonicalId is exercised through the closure map: an id never seen as
        // retired_id is not in the map at all → caller falls back to id.
        UUID a = UUID.randomUUID(), b = UUID.randomUUID(), unrelated = UUID.randomUUID();
        Map<UUID, UUID> map = SubjectAliasProjector.buildAliasMap(List.of(merge(a, b, 1L)));
        assertThat(map).doesNotContainKey(unrelated);
        // Mirror the public canonicalId semantic: getOrDefault(id, id).
        assertThat(map.getOrDefault(unrelated, unrelated)).isEqualTo(unrelated);
        assertThat(map.getOrDefault(b, b)).isEqualTo(b);
    }

    @Test
    void reversed_event_order_produces_identical_map() {
        UUID a = UUID.randomUUID(), b = UUID.randomUUID(), c = UUID.randomUUID();
        List<Event> ascending = List.of(merge(a, b, 1L), merge(b, c, 2L));
        List<Event> reversed = new ArrayList<>(ascending);
        Collections.reverse(reversed);

        Map<UUID, UUID> mapAsc = SubjectAliasProjector.buildAliasMap(ascending);
        Map<UUID, UUID> mapRev = SubjectAliasProjector.buildAliasMap(reversed);

        assertThat(mapRev).isEqualTo(mapAsc);
        assertThat(mapRev.get(a)).isEqualTo(c);
        assertThat(mapRev.get(b)).isEqualTo(c);
    }

    private Event merge(UUID retired, UUID surviving, long watermark) {
        ObjectNode payload = mapper.createObjectNode();
        payload.put("retired_id", retired.toString());
        payload.put("surviving_id", surviving.toString());
        return new Event(
                UUID.randomUUID(),
                "capture",
                "subjects_merged/v1",
                null,
                "subject",
                surviving,
                "system:test",
                UUID.fromString("00000000-0000-0000-0000-000000000001"),
                watermark,
                watermark,
                OffsetDateTime.now(),
                payload);
    }
}
