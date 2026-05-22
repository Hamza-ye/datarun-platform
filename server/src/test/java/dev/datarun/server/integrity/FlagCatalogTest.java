package dev.datarun.server.integrity;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FlagCatalogTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

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
}
