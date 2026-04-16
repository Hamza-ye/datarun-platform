package dev.datarun.server.sync;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.datarun.server.event.EnvelopeValidator;
import dev.datarun.server.event.Event;
import dev.datarun.server.event.EventRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/sync")
public class SyncController {

    private final EventRepository eventRepository;
    private final EnvelopeValidator envelopeValidator;
    private final ObjectMapper objectMapper;

    public SyncController(EventRepository eventRepository,
                          EnvelopeValidator envelopeValidator,
                          ObjectMapper objectMapper) {
        this.eventRepository = eventRepository;
        this.envelopeValidator = envelopeValidator;
        this.objectMapper = objectMapper;
    }

    @PostMapping("/push")
    public ResponseEntity<?> push(@RequestBody PushRequest request) {
        if (request.events() == null || request.events().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "empty_batch"));
        }

        // Validate all events against envelope schema first
        List<Map<String, Object>> validationErrors = new ArrayList<>();
        for (int i = 0; i < request.events().size(); i++) {
            JsonNode eventNode = objectMapper.valueToTree(request.events().get(i));
            List<String> errors = envelopeValidator.validate(eventNode);
            if (!errors.isEmpty()) {
                validationErrors.add(Map.of(
                        "index", i,
                        "errors", errors
                ));
            }
        }

        if (!validationErrors.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "validation_failed", "details", validationErrors));
        }

        // All valid — persist with deduplication
        int accepted = 0;
        int duplicates = 0;
        for (Event event : request.events()) {
            if (eventRepository.insert(event)) {
                accepted++;
            } else {
                duplicates++;
            }
        }

        return ResponseEntity.ok(Map.of("accepted", accepted, "duplicates", duplicates));
    }

    @PostMapping("/pull")
    public ResponseEntity<?> pull(@RequestBody PullRequest request) {
        if (request.sinceWatermark() == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "invalid_watermark"));
        }

        int limit = request.limit() != null ? request.limit() : 100;
        if (limit < 1) limit = 1;
        if (limit > 1000) limit = 1000;

        List<Event> events = eventRepository.findSince(request.sinceWatermark(), limit);
        long latestWatermark = events.isEmpty()
                ? request.sinceWatermark()
                : events.get(events.size() - 1).syncWatermark();

        return ResponseEntity.ok(Map.of(
                "events", events,
                "latest_watermark", latestWatermark
        ));
    }

    public record PushRequest(List<Event> events) {
    }

    public record PullRequest(
            @com.fasterxml.jackson.annotation.JsonProperty("since_watermark") Long sinceWatermark,
            Integer limit
    ) {
    }
}
