package dev.datarun.server.config;

import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Platform-bundled workflow pattern definitions.
 *
 * Deployer config may bind shapes, participant roles, and parameters to these
 * refs; it must not define states or transition tables.
 */
@Component
public class PatternRegistry {

    private static final String SUBJECT = "subject";
    private static final String EVENT = "event";

    private final Map<String, PatternDefinition> definitions;

    public PatternRegistry() {
        LinkedHashMap<String, PatternDefinition> defs = new LinkedHashMap<>();
        register(defs, new PatternDefinition(
                "capture_with_review/v1",
                Set.of(EVENT),
                true,
                Set.of("review_decision"),
                Set.of(),
                Set.of("review_decision"),
                Set.of("on_shapes"),
                Set.of(),
                Set.of("capturer", "reviewer"),
                Set.of(),
                Map.of(
                        "capturer", Set.of("capture"),
                        "reviewer", Set.of("review")
                ),
                Set.of(),
                Set.of("review_deadline"),
                false,
                false));

        register(defs, new PatternDefinition(
                "ongoing_resolution/v1",
                Set.of(SUBJECT),
                false,
                Set.of("opening", "interaction", "resolution", "closure_review"),
                Set.of("referral", "reopening", "transfer", "general_review"),
                Set.of("opening", "interaction", "referral", "resolution",
                        "closure_review", "reopening", "transfer", "general_review"),
                Set.of(),
                Set.of(),
                Set.of("assigned_worker", "supervisor"),
                Set.of(),
                Map.of(
                        "assigned_worker", Set.of("capture"),
                        "supervisor", Set.of("review")
                ),
                Set.of(),
                Set.of("follow_up_interval", "overdue_threshold", "resolution_target"),
                false,
                false));

        register(defs, new PatternDefinition(
                "multi_step_approval/v1",
                Set.of(SUBJECT, EVENT),
                true,
                Set.of("submission", "level_decision"),
                Set.of(),
                Set.of("submission", "level_decision"),
                Set.of(),
                Set.of(),
                Set.of("submitter"),
                Set.of(),
                Map.of("submitter", Set.of("capture")),
                Set.of("levels"),
                Set.of("review_deadline"),
                true,
                false));

        register(defs, new PatternDefinition(
                "transfer_with_acknowledgment/v1",
                Set.of(SUBJECT),
                true,
                Set.of("dispatch", "receipt"),
                Set.of("discrepancy_report", "discrepancy_resolution"),
                Set.of("dispatch", "receipt", "discrepancy_report", "discrepancy_resolution"),
                Set.of(),
                Set.of(),
                Set.of("sender", "receiver"),
                Set.of("supervisor"),
                Map.of(
                        "sender", Set.of("capture"),
                        "receiver", Set.of("capture"),
                        "supervisor", Set.of("review")
                ),
                Set.of(),
                Set.of("receipt_deadline", "resolution_deadline"),
                false,
                true));

        this.definitions = Collections.unmodifiableMap(defs);
    }

    public Optional<PatternDefinition> find(String ref) {
        return Optional.ofNullable(definitions.get(ref));
    }

    public Collection<PatternDefinition> definitions() {
        return definitions.values();
    }

    private void register(Map<String, PatternDefinition> defs, PatternDefinition definition) {
        defs.put(definition.ref(), definition);
    }

    public record PatternDefinition(
            String ref,
            Set<String> allowedCompositions,
            boolean bindingEnabled,
            Set<String> requiredShapeRoles,
            Set<String> optionalShapeRoles,
            Set<String> transitionBoundShapeRoles,
            Set<String> requiredActivationRoleLists,
            Set<String> optionalActivationRoleLists,
            Set<String> requiredParticipantRoles,
            Set<String> optionalParticipantRoles,
            Map<String, Set<String>> participantActionRequirements,
            Set<String> requiredParameters,
            Set<String> optionalParameters,
            boolean levelBasedApproval,
            boolean transferSupervisorConditional
    ) {
        public Set<String> allShapeRoles() {
            return union(requiredShapeRoles, optionalShapeRoles);
        }

        public Set<String> allActivationRoleLists() {
            return union(requiredActivationRoleLists, optionalActivationRoleLists);
        }

        public Set<String> allFixedParticipantRoles() {
            return union(requiredParticipantRoles, optionalParticipantRoles);
        }

        public Set<String> allParameters() {
            return union(requiredParameters, optionalParameters);
        }

        private static Set<String> union(Set<String> first, Set<String> second) {
            if (first.isEmpty()) return second;
            if (second.isEmpty()) return first;
            java.util.LinkedHashSet<String> result = new java.util.LinkedHashSet<>(first);
            result.addAll(second);
            return Collections.unmodifiableSet(result);
        }
    }
}
