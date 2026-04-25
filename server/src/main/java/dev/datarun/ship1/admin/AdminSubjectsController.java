package dev.datarun.ship1.admin;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Coordinator-only merge/split endpoints (Ship-2 §6 commitment 2). Registered behind
 * {@code CoordinatorAuthInterceptor} on {@code /admin/subjects/**}.
 *
 * <p>Skeleton at Task 2 — bodies are stubs returning 200. Task 5 fills in the §S9 pre-write
 * validation and the server-authored envelope emission per §6 commitment 7.
 */
@RestController
@RequestMapping("/admin/subjects")
public class AdminSubjectsController {

    @PostMapping("/merge")
    public ResponseEntity<?> merge(@RequestBody(required = false) JsonNode body) {
        return ResponseEntity.ok(Map.of("status", "stub"));
    }

    @PostMapping("/split")
    public ResponseEntity<?> split(@RequestBody(required = false) JsonNode body) {
        return ResponseEntity.ok(Map.of("status", "stub"));
    }
}
