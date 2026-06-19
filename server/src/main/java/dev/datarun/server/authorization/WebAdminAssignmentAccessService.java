package dev.datarun.server.authorization;

import dev.datarun.server.config.AssignmentAdminCapabilityPolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class WebAdminAssignmentAccessService {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(WebAdminAssignmentAccessService.class);

    private final ScopeResolver scopeResolver;
    private final AssignmentAdminCapabilityService assignmentAdminCapabilityService;

    public WebAdminAssignmentAccessService(
            ScopeResolver scopeResolver,
            AssignmentAdminCapabilityService assignmentAdminCapabilityService) {
        this.scopeResolver = scopeResolver;
        this.assignmentAdminCapabilityService = assignmentAdminCapabilityService;
    }

    public boolean hasAnyAssignmentAdminCommand(UUID actorId) {
        return hasCommand(actorId, AssignmentAdminCapabilityPolicy.CREATE_COMMAND)
                || hasCommand(actorId, AssignmentAdminCapabilityPolicy.END_COMMAND);
    }

    public boolean hasCommand(UUID actorId, String command) {
        if (actorId == null) {
            return false;
        }
        try {
            return scopeResolver.getActiveAssignments(actorId).stream()
                    .anyMatch(assignment -> roleGrants(assignment.role(), command));
        } catch (RuntimeException e) {
            LOGGER.warn("event=web_admin_assignment_command_visibility_failed actor_id={} command={} reason={}",
                    actorId, command, e.getMessage());
            return false;
        }
    }

    public boolean roleGrants(String role, String command) {
        try {
            return assignmentAdminCapabilityService.roleGrants(role, command);
        } catch (RuntimeException e) {
            LOGGER.warn("event=web_admin_assignment_role_visibility_failed role={} command={} reason={}",
                    role, command, e.getMessage());
            return false;
        }
    }
}
