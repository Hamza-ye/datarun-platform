package dev.datarun.server.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * REST API for config delivery (IDR-019).
 * GET /api/sync/config — returns latest config package.
 */
@RestController
@RequestMapping("/api/sync/config")
public class ConfigApiController {

    private final ConfigPackager configPackager;

    public ConfigApiController(ConfigPackager configPackager) {
        this.configPackager = configPackager;
    }

    /**
     * Config endpoint (IDR-019). Returns full package or 304 if client is up-to-date.
     */
    @GetMapping
    public ResponseEntity<?> getConfig(
            @RequestHeader(value = "If-None-Match", required = false) String ifNoneMatch) {

        Optional<ConfigPackager.ConfigPackage> latest = configPackager.getLatest();
        if (latest.isEmpty()) {
            return ResponseEntity.ok(Map.of(
                    "version", 0,
                    "shapes", Map.of(),
                    "activities", Map.of(),
                    "expressions", Map.of(),
                    "flag_severity_overrides", Map.of(),
                    "sensitivity_classifications", Map.of("shapes", Map.of(), "activities", Map.of())
            ));
        }

        ConfigPackager.ConfigPackage pkg = latest.get();

        // ETag/304 support (strip quotes per HTTP spec — clients may send '"1"' or '1')
        String etag = String.valueOf(pkg.version());
        if (ifNoneMatch != null && etag.equals(ifNoneMatch.replace("\"", ""))) {
            return ResponseEntity.status(304).build();
        }

        return ResponseEntity.ok()
                .eTag(etag)
                .body(pkg.packageJson());
    }
}
