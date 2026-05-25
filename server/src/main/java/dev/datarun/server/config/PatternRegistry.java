package dev.datarun.server.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Platform-bundled workflow pattern definitions loaded from contracts/patterns.
 *
 * Deployer config may bind shapes, participant roles, and parameters to these
 * refs; it must not define states or transition tables.
 */
@Component
public class PatternRegistry {

    private static final String RESOURCE_PATTERN = "classpath*:patterns/*.json";

    private final ObjectMapper objectMapper;
    private final Map<String, PatternDefinition> definitions;

    @Autowired
    public PatternRegistry(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.definitions = loadDefinitions(objectMapper);
    }

    public PatternRegistry() {
        this(new ObjectMapper());
    }

    public Optional<PatternDefinition> find(String ref) {
        return Optional.ofNullable(definitions.get(ref));
    }

    public Collection<PatternDefinition> definitions() {
        return definitions.values();
    }

    public ObjectNode packageDefinitions(Collection<String> refs) {
        ObjectNode root = objectMapper.createObjectNode();
        root.put("schema_version", 1);
        ObjectNode defs = objectMapper.createObjectNode();
        refs.stream().sorted().forEach(ref -> find(ref).ifPresent(definition ->
                defs.set(ref, definition.definitionJson().deepCopy())));
        root.set("definitions", defs);
        return root;
    }

    private static Map<String, PatternDefinition> loadDefinitions(ObjectMapper objectMapper) {
        try {
            Resource[] resources = new PathMatchingResourcePatternResolver(
                    PatternRegistry.class.getClassLoader()).getResources(RESOURCE_PATTERN);
            Arrays.sort(resources, Comparator.comparing(Resource::getFilename,
                    Comparator.nullsLast(String::compareTo)));
            LinkedHashMap<String, PatternDefinition> defs = new LinkedHashMap<>();
            for (Resource resource : resources) {
                JsonNode json;
                try (InputStream input = resource.getInputStream()) {
                    json = objectMapper.readTree(input);
                }
                PatternDefinition definition = parseDefinition(json);
                if (defs.put(definition.ref(), definition) != null) {
                    throw new IllegalStateException("Duplicate pattern definition ref: " + definition.ref());
                }
            }
            if (defs.isEmpty()) {
                throw new IllegalStateException("No pattern definitions found on classpath at " + RESOURCE_PATTERN);
            }
            return Collections.unmodifiableMap(defs);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load pattern definitions", e);
        }
    }

    private static PatternDefinition parseDefinition(JsonNode json) {
        return new PatternDefinition(
                json.path("ref").asText(),
                textSet(json.get("allowed_compositions")),
                json.path("binding_enabled").asBoolean(false),
                textSet(json.at("/shape_roles/required")),
                textSet(json.at("/shape_roles/optional")),
                textSet(json.at("/shape_roles/transition_bound")),
                roleMap(json.get("platform_shape_roles")),
                textSet(json.at("/activation_roles/required")),
                textSet(json.at("/activation_roles/optional")),
                textSet(json.at("/participant_roles/required")),
                textSet(json.at("/participant_roles/optional")),
                actionRequirements(json.at("/participant_roles/action_requirements")),
                textSet(json.at("/parameters/required")),
                textSet(json.at("/parameters/optional")),
                json.at("/semantics/level_based_approval").asBoolean(false),
                json.at("/semantics/transfer_supervisor_conditional").asBoolean(false),
                json.deepCopy());
    }

    private static Set<String> textSet(JsonNode node) {
        if (node == null || !node.isArray()) {
            return Set.of();
        }
        LinkedHashSet<String> result = new LinkedHashSet<>();
        for (JsonNode item : node) {
            if (item.isTextual()) {
                result.add(item.asText());
            }
        }
        return Collections.unmodifiableSet(result);
    }

    private static Map<String, Set<String>> actionRequirements(JsonNode node) {
        if (node == null || !node.isObject()) {
            return Map.of();
        }
        LinkedHashMap<String, Set<String>> result = new LinkedHashMap<>();
        node.fields().forEachRemaining(entry ->
                result.put(entry.getKey(), textSet(entry.getValue())));
        return Collections.unmodifiableMap(result);
    }

    private static Map<String, Set<String>> roleMap(JsonNode node) {
        if (node == null || !node.isObject()) {
            return Map.of();
        }
        LinkedHashMap<String, Set<String>> result = new LinkedHashMap<>();
        node.fields().forEachRemaining(entry ->
                result.put(entry.getKey(), textSet(entry.getValue())));
        return Collections.unmodifiableMap(result);
    }

    public record PatternDefinition(
            String ref,
            Set<String> allowedCompositions,
            boolean bindingEnabled,
            Set<String> requiredShapeRoles,
            Set<String> optionalShapeRoles,
            Set<String> transitionBoundShapeRoles,
            Map<String, Set<String>> platformShapeRoles,
            Set<String> requiredActivationRoleLists,
            Set<String> optionalActivationRoleLists,
            Set<String> requiredParticipantRoles,
            Set<String> optionalParticipantRoles,
            Map<String, Set<String>> participantActionRequirements,
            Set<String> requiredParameters,
            Set<String> optionalParameters,
            boolean levelBasedApproval,
            boolean transferSupervisorConditional,
            JsonNode definitionJson
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
