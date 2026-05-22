package dev.datarun.server.integrity;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Platform-owned flag catalog surface.
 *
 * <p>Severity can be overridden by deployment configuration. Resolvability is
 * fixed platform policy and must not be derived from severity.
 */
public final class FlagCatalog {

    public static final String SEVERITY_BLOCKING = "blocking";
    public static final String SEVERITY_INFORMATIONAL = "informational";
    public static final String RESERVED_CATEGORY = "reserved";

    private static final Map<String, String> DEFAULT_SEVERITIES = orderedMap(
            Map.entry("concurrent_state_change", SEVERITY_BLOCKING),
            Map.entry("stale_reference", SEVERITY_INFORMATIONAL),
            Map.entry("identity_conflict", SEVERITY_BLOCKING),
            Map.entry("scope_violation", SEVERITY_BLOCKING),
            Map.entry("temporal_authority_expired", SEVERITY_INFORMATIONAL),
            Map.entry("role_stale", SEVERITY_BLOCKING),
            Map.entry("domain_uniqueness_violation", SEVERITY_BLOCKING),
            Map.entry("transition_violation", SEVERITY_INFORMATIONAL)
    );

    private static final Map<String, String> RESOLVABILITY = orderedMap(
            Map.entry("concurrent_state_change", "manual_only"),
            Map.entry("stale_reference", "auto_eligible"),
            Map.entry("identity_conflict", "manual_only"),
            Map.entry("scope_violation", "manual_only"),
            Map.entry("temporal_authority_expired", "auto_eligible"),
            Map.entry("role_stale", "manual_only"),
            Map.entry("domain_uniqueness_violation", "manual_only"),
            Map.entry("transition_violation", "auto_eligible")
    );

    private static final Set<String> VALID_SEVERITIES = Set.of(
            SEVERITY_BLOCKING, SEVERITY_INFORMATIONAL);

    private FlagCatalog() {
    }

    @SafeVarargs
    private static Map<String, String> orderedMap(Map.Entry<String, String>... entries) {
        Map<String, String> map = new LinkedHashMap<>();
        for (Map.Entry<String, String> entry : entries) {
            map.put(entry.getKey(), entry.getValue());
        }
        return Collections.unmodifiableMap(map);
    }

    public static Map<String, String> defaultSeverities() {
        return DEFAULT_SEVERITIES;
    }

    public static List<String> categories() {
        return DEFAULT_SEVERITIES.keySet().stream().toList();
    }

    public static boolean isKnownCategory(String category) {
        return DEFAULT_SEVERITIES.containsKey(category);
    }

    public static boolean isValidSeverity(String severity) {
        return VALID_SEVERITIES.contains(severity);
    }

    public static String defaultSeverityFor(String category) {
        return Optional.ofNullable(DEFAULT_SEVERITIES.get(category))
                .orElseThrow(() -> new IllegalArgumentException("Unknown flag category '" + category + "'"));
    }

    public static String effectiveSeverityFor(String category, JsonNode overrides) {
        if (!isKnownCategory(category)) {
            throw new IllegalArgumentException("Unknown flag category '" + category + "'");
        }
        if (overrides != null && overrides.isObject()) {
            JsonNode override = overrides.get(category);
            if (override != null && override.isTextual() && isValidSeverity(override.asText())) {
                return override.asText();
            }
        }
        return defaultSeverityFor(category);
    }

    public static String resolvabilityFor(String category) {
        return Optional.ofNullable(RESOLVABILITY.get(category))
                .orElseThrow(() -> new IllegalArgumentException("Unknown flag category '" + category + "'"));
    }
}
