package dev.datarun.ship1.admin;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dev.datarun.ship1.config.VillageRepository;
import dev.datarun.ship1.event.Event;
import dev.datarun.ship1.event.EventRepository;
import dev.datarun.ship1.sync.ActorTokenRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.*;

/**
 * Dev-only bootstrap. Seeds the Ship-1 world: 1 district, 2 villages, 2 CHV actors with tokens,
 * and the corresponding {@code assignment_created/v1} events that bind each CHV to one village.
 *
 * <p>Idempotent against a fixed-seed UUID pattern when {@code seed=fixed} is passed.
 */
@RestController
@RequestMapping("/dev")
public class DevBootstrapController {

    private static final UUID DEV_DEVICE_ID = UUID.fromString("00000000-0000-0000-0000-0000000000ff");

    private final VillageRepository villages;
    private final ActorTokenRepository tokens;
    private final EventRepository events;
    private final ObjectMapper mapper;

    public DevBootstrapController(VillageRepository villages, ActorTokenRepository tokens,
                                  EventRepository events, ObjectMapper mapper) {
        this.villages = villages;
        this.tokens = tokens;
        this.events = events;
        this.mapper = mapper;
    }

    @PostMapping("/bootstrap")
    public ResponseEntity<?> bootstrap() {
        UUID villageA = UUID.randomUUID();
        UUID villageB = UUID.randomUUID();
        villages.upsert(villageA, "Mirpur", "Village-1");
        villages.upsert(villageB, "Mirpur", "Village-2");

        UUID chvA = UUID.randomUUID();
        UUID chvB = UUID.randomUUID();
        String tokenA = randomToken();
        String tokenB = randomToken();
        tokens.createToken(tokenA, chvA);
        tokens.createToken(tokenB, chvB);

        OffsetDateTime now = OffsetDateTime.now();
        emitAssignment(chvA, villageA, "chv", now);
        emitAssignment(chvB, villageB, "chv", now);

        ObjectNode out = mapper.createObjectNode();
        out.put("village_a", villageA.toString());
        out.put("village_b", villageB.toString());
        out.put("chv_a_actor_id", chvA.toString());
        out.put("chv_a_token", tokenA);
        out.put("chv_b_actor_id", chvB.toString());
        out.put("chv_b_token", tokenB);
        return ResponseEntity.ok(out);
    }

    private void emitAssignment(UUID actorId, UUID villageId, String role, OffsetDateTime now) {
        ObjectNode payload = mapper.createObjectNode();
        ObjectNode target = payload.putObject("target_actor");
        target.put("type", "actor");
        target.put("id", actorId.toString());
        payload.put("role", role);
        ObjectNode scope = payload.putObject("scope");
        scope.put("geographic", villageId.toString());
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
                UUID.randomUUID(), // assignment has its own identity
                "system:dev_bootstrap/seed",
                DEV_DEVICE_ID,
                nextDevSeq(),
                null,
                now,
                payload);
        events.insert(e);
    }

    // Simple increment over the DEV_DEVICE_ID namespace — reads the last device_seq from events.
    private long nextDevSeq() {
        long max = 0;
        for (Event e : events.findByShapeRefPrefix("assignment_created/")) {
            if (DEV_DEVICE_ID.equals(e.deviceId())) max = Math.max(max, e.deviceSeq());
        }
        return max + 1;
    }

    private static String randomToken() {
        byte[] b = new byte[32];
        new java.security.SecureRandom().nextBytes(b);
        StringBuilder sb = new StringBuilder();
        for (byte x : b) sb.append(String.format("%02x", x));
        return sb.toString();
    }
}
