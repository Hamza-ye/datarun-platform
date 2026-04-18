package dev.datarun.server.authorization;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Location hierarchy admin API. CRUD for static reference data (IDR-014).
 */
@RestController
@RequestMapping("/api/locations")
public class LocationController {

    private final LocationRepository locationRepository;
    private final SubjectLocationRepository subjectLocationRepository;

    public LocationController(LocationRepository locationRepository,
                              SubjectLocationRepository subjectLocationRepository) {
        this.locationRepository = locationRepository;
        this.subjectLocationRepository = subjectLocationRepository;
    }

    @PostMapping
    public ResponseEntity<?> createLocation(@RequestBody CreateLocationRequest request) {
        if (request.name() == null || request.level() == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "missing required fields: name, level"));
        }

        UUID id = request.id() != null ? request.id() : UUID.randomUUID();
        try {
            locationRepository.insert(id, request.name(), request.parentId(), request.level());
            Location created = locationRepository.findById(id);
            return ResponseEntity.ok(created);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/tree")
    public ResponseEntity<?> getTree() {
        List<Location> all = locationRepository.findAll();
        return ResponseEntity.ok(Map.of("locations", all));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getLocation(@PathVariable UUID id) {
        Location location = locationRepository.findById(id);
        if (location == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(location);
    }

    @PostMapping("/subjects")
    public ResponseEntity<?> assignSubjectLocation(@RequestBody AssignSubjectLocationRequest request) {
        if (request.subjectId() == null || request.locationId() == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "missing required fields: subject_id, location_id"));
        }

        Location location = locationRepository.findById(request.locationId());
        if (location == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "location not found: " + request.locationId()));
        }

        subjectLocationRepository.upsert(request.subjectId(), request.locationId(), location.path());
        return ResponseEntity.ok(Map.of("subject_id", request.subjectId(), "location_path", location.path()));
    }

    public record CreateLocationRequest(
            UUID id,
            String name,
            @JsonProperty("parent_id") UUID parentId,
            String level
    ) {}

    public record AssignSubjectLocationRequest(
            @JsonProperty("subject_id") UUID subjectId,
            @JsonProperty("location_id") UUID locationId
    ) {}
}
