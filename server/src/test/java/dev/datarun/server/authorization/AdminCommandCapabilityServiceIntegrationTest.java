package dev.datarun.server.authorization;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.datarun.server.AbstractIntegrationTest;
import dev.datarun.server.config.AdminCommandCapabilityPolicy;
import dev.datarun.server.config.AssignmentAdminCapabilityPolicy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AdminCommandCapabilityServiceIntegrationTest extends AbstractIntegrationTest {

    private static final UUID ACTOR =
            UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID OTHER_ACTOR =
            UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final UUID FIXED_DEV_ADMIN_ACTOR = TEST_ACTOR_ID;

    @Autowired private AdminCommandCapabilityService adminCommandCapabilityService;
    @Autowired private AssignmentAdminCapabilityService assignmentAdminCapabilityService;
    @Autowired private ObjectMapper objectMapper;

    @BeforeEach
    void cleanPolicy() {
        jdbcTemplate.execute("DELETE FROM deployment_config");
    }

    @Test
    void absentPolicyDeniesSupportedCommands() {
        assertThat(adminCommandCapabilityService.actorGrants(
                ACTOR, AdminCommandCapabilityPolicy.WEB_ADMIN_ACCESS)).isFalse();
        assertThat(adminCommandCapabilityService.actorGrants(
                FIXED_DEV_ADMIN_ACTOR, AdminCommandCapabilityPolicy.WEB_ADMIN_ACCESS)).isFalse();
        assertThat(adminCommandCapabilityService.actorGrants(
                ACTOR, AdminCommandCapabilityPolicy.CONFIG_ADMIN_PUBLISH)).isFalse();
    }

    @Test
    void actorBoundPolicyGrantsOnlyExplicitActorAndCommand() throws Exception {
        List<String> violations = adminCommandCapabilityService.updatePolicy(policy("""
                {
                  "schema_version": 1,
                  "actors": {
                    "11111111-1111-1111-1111-111111111111": ["web_admin.access"],
                    "22222222-2222-2222-2222-222222222222": ["config_admin.publish"]
                  }
                }
                """), ACTOR);

        assertThat(violations).isEmpty();
        assertThat(adminCommandCapabilityService.actorGrants(
                ACTOR, AdminCommandCapabilityPolicy.WEB_ADMIN_ACCESS)).isTrue();
        assertThat(adminCommandCapabilityService.actorGrants(
                ACTOR, AdminCommandCapabilityPolicy.CONFIG_ADMIN_PUBLISH)).isFalse();
        assertThat(adminCommandCapabilityService.actorGrants(
                OTHER_ACTOR, AdminCommandCapabilityPolicy.CONFIG_ADMIN_PUBLISH)).isTrue();
        assertThat(adminCommandCapabilityService.actorGrants(
                OTHER_ACTOR, AdminCommandCapabilityPolicy.WEB_ADMIN_ACCESS)).isFalse();
        assertThat(adminCommandCapabilityService.actorGrants(
                FIXED_DEV_ADMIN_ACTOR, AdminCommandCapabilityPolicy.WEB_ADMIN_ACCESS)).isFalse();
    }

    @Test
    void malformedOrUnknownPolicyIsRejectedAndFailsClosedWhenStored()
            throws Exception {
        List<String> malformed = adminCommandCapabilityService.updatePolicy(policy("""
                {
                  "schema_version": 1,
                  "actors": []
                }
                """), ACTOR);
        assertThat(malformed)
                .contains("admin_command_capabilities.actors must be an object");
        assertThat(policyRowCount()).isZero();

        List<String> unknown = adminCommandCapabilityService.updatePolicy(policy("""
                {
                  "schema_version": 1,
                  "actors": {
                    "11111111-1111-1111-1111-111111111111": ["web_admin.access", "conflict.resolve"]
                  }
                }
                """), ACTOR);
        assertThat(unknown).anySatisfy(violation ->
                assertThat(violation).contains("Unknown admin command 'conflict.resolve'"));
        assertThat(policyRowCount()).isZero();

        insertRawPolicy("""
                {
                  "schema_version": 1,
                  "actors": {
                    "11111111-1111-1111-1111-111111111111": ["web_admin.access", "not_supported"]
                  }
                }
                """);
        assertThat(adminCommandCapabilityService.actorGrants(
                ACTOR, AdminCommandCapabilityPolicy.WEB_ADMIN_ACCESS)).isFalse();
        assertThatThrownBy(adminCommandCapabilityService::getValidatedPolicy)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid admin_command_capabilities");
    }

    @Test
    void webAdminAccessGrantsNoOtherAdminOrAssignmentAuthority() throws Exception {
        adminCommandCapabilityService.updatePolicy(policy("""
                {
                  "schema_version": 1,
                  "actors": {
                    "11111111-1111-1111-1111-111111111111": ["web_admin.access"]
                  }
                }
                """), ACTOR);

        assertThat(adminCommandCapabilityService.actorGrants(
                ACTOR, AdminCommandCapabilityPolicy.WEB_ADMIN_ACCESS)).isTrue();
        assertThat(adminCommandCapabilityService.actorGrants(
                ACTOR, AdminCommandCapabilityPolicy.WEB_ADMIN_READ_SCOPED)).isFalse();
        assertThat(adminCommandCapabilityService.actorGrants(
                ACTOR, AdminCommandCapabilityPolicy.CONFIG_ADMIN_AUTHOR)).isFalse();
        assertThat(adminCommandCapabilityService.actorGrants(
                ACTOR, AdminCommandCapabilityPolicy.CONFIG_ADMIN_VALIDATE)).isFalse();
        assertThat(adminCommandCapabilityService.actorGrants(
                ACTOR, AdminCommandCapabilityPolicy.CONFIG_ADMIN_READINESS_REVIEW)).isFalse();
        assertThat(adminCommandCapabilityService.actorGrants(
                ACTOR, AdminCommandCapabilityPolicy.CONFIG_ADMIN_APPROVE)).isFalse();
        assertThat(adminCommandCapabilityService.actorGrants(
                ACTOR, AdminCommandCapabilityPolicy.CONFIG_ADMIN_PUBLISH)).isFalse();
        assertThat(assignmentAdminCapabilityService.roleGrants(
                "admin", AssignmentAdminCapabilityPolicy.CREATE_COMMAND)).isFalse();
        assertThat(AdminCommandCapabilityPolicy.SUPPORTED_COMMANDS)
                .doesNotContain(AssignmentAdminCapabilityPolicy.CREATE_COMMAND,
                        AssignmentAdminCapabilityPolicy.END_COMMAND, "conflict.resolve",
                        "data.read");
        assertThatThrownBy(() -> adminCommandCapabilityService.actorGrants(
                ACTOR, "conflict.resolve"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unknown admin command");
    }

    private JsonNode policy(String json) throws Exception {
        return objectMapper.readTree(json);
    }

    private void insertRawPolicy(String policyJson) {
        jdbcTemplate.update("""
                INSERT INTO deployment_config (config_key, config_json, updated_by, updated_at)
                VALUES (?, ?::jsonb, ?::uuid, NOW())
                """,
                AdminCommandCapabilityPolicy.CONFIG_KEY,
                policyJson,
                ACTOR.toString());
    }

    private int policyRowCount() {
        Integer count = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM deployment_config
                WHERE config_key = ?
                """, Integer.class, AdminCommandCapabilityPolicy.CONFIG_KEY);
        return count == null ? 0 : count;
    }
}
