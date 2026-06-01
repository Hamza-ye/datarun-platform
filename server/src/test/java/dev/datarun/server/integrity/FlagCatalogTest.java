package dev.datarun.server.integrity;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dev.datarun.server.contracts.PlatformPayloadContractValidator;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FlagCatalogTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final PlatformPayloadContractValidator platformPayloadValidator =
            new PlatformPayloadContractValidator(objectMapper);

    @Test
    void catalogExposesOnlyActiveCategoriesAndFixedResolvability() {
        assertThat(FlagCatalog.categories()).containsExactly(
                "concurrent_state_change",
                "stale_reference",
                "identity_conflict",
                "scope_violation",
                "temporal_authority_expired",
                "role_stale",
                "domain_uniqueness_violation",
                "transition_violation");
        assertThat(FlagCatalog.categories()).doesNotContain(FlagCatalog.RESERVED_CATEGORY);
        assertThat(FlagCatalog.defaultSeverities().keySet())
                .containsExactlyElementsOf(FlagCatalog.categories());

        Map<String, String> expectedResolvability = Map.of(
                "concurrent_state_change", "manual_only",
                "stale_reference", "auto_eligible",
                "identity_conflict", "manual_only",
                "scope_violation", "manual_only",
                "temporal_authority_expired", "auto_eligible",
                "role_stale", "manual_only",
                "domain_uniqueness_violation", "manual_only",
                "transition_violation", "auto_eligible");
        expectedResolvability.forEach((category, resolvability) ->
                assertThat(FlagCatalog.resolvabilityFor(category)).isEqualTo(resolvability));

        assertThatThrownBy(() -> FlagCatalog.resolvabilityFor(FlagCatalog.RESERVED_CATEGORY))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void conflictDetectedContractRequiresCategoryAndDesignatedResolver() {
        ObjectNode missingFlagCategory = conflictDetectedPayload();
        missingFlagCategory.remove("flag_category");
        assertThat(platformPayloadValidator.validate("conflict_detected/v1", missingFlagCategory))
                .anySatisfy(error -> assertThat(error).contains("flag_category"));

        ObjectNode missingResolver = conflictDetectedPayload();
        missingResolver.remove("designated_resolver");
        assertThat(platformPayloadValidator.validate("conflict_detected/v1", missingResolver))
                .anySatisfy(error -> assertThat(error).contains("designated_resolver"));
    }

    @Test
    void effectiveSeverity_usesOverrideOrDefault() throws Exception {
        JsonNode overrides = objectMapper.readTree("""
                {"temporal_authority_expired": "blocking"}
                """);

        assertThat(FlagCatalog.effectiveSeverityFor("temporal_authority_expired", overrides))
                .isEqualTo("blocking");
        assertThat(FlagCatalog.effectiveSeverityFor("role_stale", overrides))
                .isEqualTo("blocking");
        assertThat(FlagCatalog.effectiveSeverityFor("stale_reference", overrides))
                .isEqualTo("informational");
    }

    @Test
    void severityDoesNotChangeFixedResolvability() throws Exception {
        JsonNode overrides = objectMapper.readTree("""
                {
                  "temporal_authority_expired": "blocking",
                  "identity_conflict": "informational"
                }
                """);

        assertThat(FlagCatalog.effectiveSeverityFor("temporal_authority_expired", overrides))
                .isEqualTo("blocking");
        assertThat(FlagCatalog.resolvabilityFor("temporal_authority_expired"))
                .isEqualTo("auto_eligible");

        assertThat(FlagCatalog.effectiveSeverityFor("identity_conflict", overrides))
                .isEqualTo("informational");
        assertThat(FlagCatalog.resolvabilityFor("identity_conflict"))
                .isEqualTo("manual_only");
    }

    private ObjectNode conflictDetectedPayload() {
        ObjectNode payload = objectMapper.createObjectNode();
        payload.put("source_event_id", UUID.randomUUID().toString());
        payload.put("flag_category", "scope_violation");
        payload.put("resolvability", "manual_only");
        ObjectNode resolver = objectMapper.createObjectNode();
        resolver.put("type", "actor");
        resolver.put("id", UUID.randomUUID().toString());
        payload.set("designated_resolver", resolver);
        return payload;
    }
}
