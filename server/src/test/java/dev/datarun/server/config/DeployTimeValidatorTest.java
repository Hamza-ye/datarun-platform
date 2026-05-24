package dev.datarun.server.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for DeployTimeValidator (IDR-018 §DtV).
 * Tests key DtV checks for expression rules.
 */
class DeployTimeValidatorTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private Shape testShape;

    @BeforeEach
    void setUp() {
        // Build a test shape with various field types
        ObjectNode schema = objectMapper.createObjectNode();
        ArrayNode fields = schema.putArray("fields");

        addField(fields, "name", "text", true);
        addField(fields, "age", "integer", false);
        addField(fields, "temperature", "decimal", false);
        addField(fields, "is_active", "boolean", false);
        addField(fields, "visit_date", "date", false);
        addField(fields, "status", "select", false, List.of("active", "closed", "partial"));
        addField(fields, "stockout_items", "multi_select", false, List.of("vaccines", "antimalarials", "bandages"));
        addField(fields, "notes", "narrative", false);

        schema.putNull("subject_binding");
        schema.putNull("uniqueness");

        testShape = new Shape("test_shape", 1, "active", "standard", schema, null);
    }

    @Test
    void validShowCondition_passes() {
        JsonNode expr = parse("""
                {"when": {"eq": ["payload.status", "active"]}}
                """);
        ExpressionRule rule = makeRule("status", "show_condition", expr, null);

        List<String> violations = makeValidator().validateRule(rule, testShape);

        assertTrue(violations.isEmpty(), "Expected no violations, got: " + violations);
    }

    @Test
    void invalidFieldReference_violation() {
        JsonNode expr = parse("""
                {"when": {"eq": ["payload.nonexistent_field", "active"]}}
                """);
        ExpressionRule rule = makeRule("status", "show_condition", expr, null);

        List<String> violations = makeValidator().validateRule(rule, testShape);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.contains("nonexistent_field") && v.contains("not found")));
    }

    @Test
    void orderingOperatorOnTextField_violation() {
        JsonNode expr = parse("""
                {"when": {"gt": ["payload.name", "abc"]}}
                """);
        ExpressionRule rule = makeRule("name", "show_condition", expr, null);

        List<String> violations = makeValidator().validateRule(rule, testShape);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.contains("Ordering operator") && v.contains("text")));
    }

    @Test
    void multiSelectWithEq_violation() {
        JsonNode expr = parse("""
                {"when": {"eq": ["payload.stockout_items", "vaccines"]}}
                """);
        ExpressionRule rule = makeRule("stockout_items", "show_condition", expr, null);

        List<String> violations = makeValidator().validateRule(rule, testShape);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.contains("multi_select") && v.contains("use 'in'")));
    }

    @Test
    void predicateCountExceedsThree_violation() {
        JsonNode expr = parse("""
                {"when": {"and": [
                    {"eq": ["payload.status", "active"]},
                    {"gt": ["payload.age", 5]},
                    {"lt": ["payload.age", 100]},
                    {"not_null": ["payload.notes"]}
                ]}}
                """);
        ExpressionRule rule = makeRule("name", "show_condition", expr, null);

        List<String> violations = makeValidator().validateRule(rule, testShape);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.contains("2-3 predicates")));
    }

    @Test
    void nestedLogicalOperator_violation() {
        JsonNode expr = parse("""
                {"when": {"and": [
                    {"or": [
                        {"eq": ["payload.status", "active"]},
                        {"eq": ["payload.status", "partial"]}
                    ]},
                    {"gt": ["payload.age", 5]}
                ]}}
                """);
        ExpressionRule rule = makeRule("name", "show_condition", expr, null);

        List<String> violations = makeValidator().validateRule(rule, testShape);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.contains("Nested logical")));
    }

    @Test
    void defaultExpressionTypeMismatch_violation() {
        // Comparison produces boolean but target field is 'text'
        JsonNode expr = parse("""
                {"value": {"gt": ["payload.age", 18]}}
                """);
        ExpressionRule rule = makeRule("name", "default", expr, null);

        List<String> violations = makeValidator().validateRule(rule, testShape);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v ->
                v.contains("produces boolean") && v.contains("text")));
    }

    @Test
    void defaultExpressionBooleanFieldMatch_passes() {
        // Comparison produces boolean, target field IS boolean → ok
        JsonNode expr = parse("""
                {"value": {"gt": ["payload.age", 18]}}
                """);
        ExpressionRule rule = makeRule("is_active", "default", expr, null);

        List<String> violations = makeValidator().validateRule(rule, testShape);

        assertTrue(violations.isEmpty(), "Expected no violations, got: " + violations);
    }

    @Test
    void defaultRefExpression_passes() {
        JsonNode expr = parse("""
                {"value": {"ref": "context.actor.scope_name"}}
                """);
        ExpressionRule rule = makeRule("name", "default", expr, null);

        List<String> violations = makeValidator().validateRule(rule, testShape);

        assertTrue(violations.isEmpty(), "Expected no violations, got: " + violations);
    }

    @Test
    void warningRule_validWithMessage_passes() {
        JsonNode expr = parse("""
                {"when": {"and": [
                    {"eq": ["payload.status", "active"]},
                    {"lt": ["payload.age", 5]}
                ]}}
                """);
        ExpressionRule rule = makeRule("name", "warning", expr, "Age seems too low");

        List<String> violations = makeValidator().validateRule(rule, testShape);

        assertTrue(violations.isEmpty(), "Expected no violations, got: " + violations);
    }

    @Test
    void unknownOperator_rejected() {
        JsonNode expr = parse("""
                {"when": {"contains": ["payload.name", "test"]}}
                """);
        ExpressionRule rule = makeRule("name", "show_condition", expr, null);

        List<String> violations = makeValidator().validateRule(rule, testShape);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.contains("Unknown operator")));
    }

    @Test
    void notWrapsLogical_violation() {
        JsonNode expr = parse("""
                {"when": {"not": {"and": [
                    {"eq": ["payload.status", "active"]},
                    {"gt": ["payload.age", 5]}
                ]}}}
                """);
        ExpressionRule rule = makeRule("name", "show_condition", expr, null);

        List<String> violations = makeValidator().validateRule(rule, testShape);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.contains("Nested logical")));
    }

    @Test
    void targetFieldNotInShape_violation() {
        JsonNode expr = parse("""
                {"when": {"eq": ["payload.status", "active"]}}
                """);
        ExpressionRule rule = makeRule("nonexistent", "show_condition", expr, null);

        List<String> violations = makeValidator().validateRule(rule, testShape);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.contains("does not exist")));
    }

    @Test
    void orderingOnIntegerField_passes() {
        JsonNode expr = parse("""
                {"when": {"gt": ["payload.age", 18]}}
                """);
        ExpressionRule rule = makeRule("age", "show_condition", expr, null);

        List<String> violations = makeValidator().validateRule(rule, testShape);

        assertTrue(violations.isEmpty(), "Expected no violations, got: " + violations);
    }

    @Test
    void orderingOnDecimalField_passes() {
        JsonNode expr = parse("""
                {"when": {"lte": ["payload.temperature", 37.5]}}
                """);
        ExpressionRule rule = makeRule("temperature", "show_condition", expr, null);

        List<String> violations = makeValidator().validateRule(rule, testShape);

        assertTrue(violations.isEmpty(), "Expected no violations, got: " + violations);
    }

    @Test
    void activityRoles_validActivityWorkActions_passes() {
        JsonNode roles = parse("""
                {
                  "field_worker": ["capture"],
                  "supervisor": ["capture", "review", "alert", "task_created", "task_completed"]
                }
                """);

        List<String> violations = DeployTimeValidator.validateActivityRoles(roles);

        assertTrue(violations.isEmpty(), "Expected no violations, got: " + violations);
    }

    @Test
    void activityRoles_assignmentChangedRejected() {
        JsonNode roles = parse("""
                {"supervisor": ["assignment_changed"]}
                """);

        List<String> violations = DeployTimeValidator.validateActivityRoles(roles);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.contains("Unknown action 'assignment_changed'")));
    }

    @Test
    void activityRoles_unknownAction_rejected() {
        JsonNode roles = parse("""
                {"field_worker": ["capture", "approve"]}
                """);

        List<String> violations = DeployTimeValidator.validateActivityRoles(roles);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.contains("Unknown action 'approve'")));
    }

    @Test
    void activityRoles_emptyActionList_rejected() {
        JsonNode roles = parse("""
                {"field_worker": []}
                """);

        List<String> violations = DeployTimeValidator.validateActivityRoles(roles);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.contains("non-empty action list")));
    }

    @Test
    void activityRoles_emptyRoleName_rejected() {
        JsonNode roles = parse("""
                {"": ["capture"]}
                """);

        List<String> violations = DeployTimeValidator.validateActivityRoles(roles);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.contains("role name must be non-empty")));
    }

    @Test
    void activityRoles_missingObject_rejected() {
        List<String> violations = DeployTimeValidator.validateActivityRoles(null);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.contains("'roles' object")));
    }

    @Test
    void patternRegistry_bundlesInitialDefinitionsAndEnablesOngoingProjection() {
        PatternRegistry registry = new PatternRegistry();

        assertTrue(registry.find("capture_with_review/v1").isPresent());
        assertTrue(registry.find("ongoing_resolution/v1").isPresent());
        assertTrue(registry.find("multi_step_approval/v1").isPresent());
        assertTrue(registry.find("transfer_with_acknowledgment/v1").isPresent());
        assertTrue(registry.find("ongoing_resolution/v1").orElseThrow().bindingEnabled());
    }

    @Test
    void activityPattern_validCaptureReviewBinding_passes() {
        JsonNode config = parse("""
                {
                  "shapes": ["facility_observation/v1", "facility_observation_review/v1"],
                  "roles": {
                    "field_worker": ["capture"],
                    "supervisor": ["review"]
                  },
                  "pattern": {
                    "subject": null,
                    "event": [
                      {
                        "ref": "capture_with_review/v1",
                        "composition": "event",
                        "shape_roles": {
                          "review_decision": ["facility_observation_review/v1"]
                        },
                        "activation_roles": {
                          "on_shapes": ["facility_observation/v1"]
                        },
                        "participant_roles": {
                          "capturer": ["field_worker"],
                          "reviewer": ["supervisor"]
                        },
                        "parameters": {}
                      }
                    ]
                  }
                }
                """);

        List<String> violations = validatePattern(config,
                "facility_observation/v1", "facility_observation_review/v1");

        assertTrue(violations.isEmpty(), "Expected no violations, got: " + violations);
    }

    @Test
    void activityPattern_unknownPatternRef_rejected() {
        JsonNode config = parse("""
                {
                  "roles": {"field_worker": ["capture"]},
                  "pattern": {
                    "subject": null,
                    "event": [
                      {
                        "ref": "unknown_pattern/v1",
                        "composition": "event",
                        "shape_roles": {},
                        "participant_roles": {},
                        "parameters": {}
                      }
                    ]
                  }
                }
                """);

        List<String> violations = validatePattern(config);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.contains("Unknown pattern ref 'unknown_pattern/v1'")));
    }

    @Test
    void activityPattern_twoSubjectBindings_rejected() {
        JsonNode config = parse("""
                {
                  "roles": {"sender": ["capture"], "receiver": ["capture"]},
                  "pattern": {
                    "subject": [
                      {"ref": "transfer_with_acknowledgment/v1", "composition": "subject"},
                      {"ref": "multi_step_approval/v1", "composition": "subject"}
                    ],
                    "event": []
                  }
                }
                """);

        List<String> violations = validatePattern(config);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.contains("at most one subject-level")));
    }

    @Test
    void activityPattern_duplicateTransitionBoundShapeOwnership_rejected() {
        JsonNode config = parse("""
                {
                  "roles": {
                    "field_worker": ["capture"],
                    "supervisor": ["review"]
                  },
                  "pattern": {
                    "subject": null,
                    "event": [
                      {
                        "ref": "capture_with_review/v1",
                        "composition": "event",
                        "shape_roles": {"review_decision": ["shared_review/v1"]},
                        "activation_roles": {"on_shapes": ["capture_a/v1"]},
                        "participant_roles": {
                          "capturer": ["field_worker"],
                          "reviewer": ["supervisor"]
                        },
                        "parameters": {}
                      },
                      {
                        "ref": "capture_with_review/v1",
                        "composition": "event",
                        "shape_roles": {"review_decision": ["shared_review/v1"]},
                        "activation_roles": {"on_shapes": ["capture_b/v1"]},
                        "participant_roles": {
                          "capturer": ["field_worker"],
                          "reviewer": ["supervisor"]
                        },
                        "parameters": {}
                      }
                    ]
                  }
                }
                """);

        List<String> violations = validatePattern(config,
                "shared_review/v1", "capture_a/v1", "capture_b/v1");

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.contains("Duplicate transition-bound shape ownership")));
    }

    @Test
    void activityPattern_missingRequiredShapeRole_rejected() {
        JsonNode config = parse("""
                {
                  "roles": {
                    "field_worker": ["capture"],
                    "supervisor": ["review"]
                  },
                  "pattern": {
                    "subject": null,
                    "event": [
                      {
                        "ref": "capture_with_review/v1",
                        "composition": "event",
                        "shape_roles": {},
                        "activation_roles": {"on_shapes": ["facility_observation/v1"]},
                        "participant_roles": {
                          "capturer": ["field_worker"],
                          "reviewer": ["supervisor"]
                        },
                        "parameters": {}
                      }
                    ]
                  }
                }
                """);

        List<String> violations = validatePattern(config, "facility_observation/v1");

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.contains("missing required shape_roles.review_decision")));
    }

    @Test
    void activityPattern_missingRequiredParticipantRole_rejected() {
        JsonNode config = parse("""
                {
                  "roles": {
                    "field_worker": ["capture"],
                    "supervisor": ["review"]
                  },
                  "pattern": {
                    "subject": null,
                    "event": [
                      {
                        "ref": "capture_with_review/v1",
                        "composition": "event",
                        "shape_roles": {"review_decision": ["review/v1"]},
                        "activation_roles": {"on_shapes": ["capture/v1"]},
                        "participant_roles": {
                          "capturer": ["field_worker"]
                        },
                        "parameters": {}
                      }
                    ]
                  }
                }
                """);

        List<String> violations = validatePattern(config, "capture/v1", "review/v1");

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.contains("missing required participant_roles.reviewer")));
    }

    @Test
    void activityPattern_compositionMismatch_rejected() {
        JsonNode config = parse("""
                {
                  "roles": {
                    "field_worker": ["capture"],
                    "supervisor": ["review"]
                  },
                  "pattern": {
                    "subject": {
                      "ref": "capture_with_review/v1",
                      "composition": "subject",
                      "shape_roles": {"review_decision": ["review/v1"]},
                      "activation_roles": {"on_shapes": ["capture/v1"]},
                      "participant_roles": {
                        "capturer": ["field_worker"],
                        "reviewer": ["supervisor"]
                      },
                      "parameters": {}
                    },
                    "event": []
                  }
                }
                """);

        List<String> violations = validatePattern(config, "capture/v1", "review/v1");

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.contains("does not match platform definition")));
    }

    @Test
    void activityPattern_participantRoleMustPermitTransitionAction() {
        JsonNode config = parse("""
                {
                  "roles": {
                    "field_worker": ["capture"],
                    "supervisor": ["capture"]
                  },
                  "pattern": {
                    "subject": null,
                    "event": [
                      {
                        "ref": "capture_with_review/v1",
                        "composition": "event",
                        "shape_roles": {"review_decision": ["review/v1"]},
                        "activation_roles": {"on_shapes": ["capture/v1"]},
                        "participant_roles": {
                          "capturer": ["field_worker"],
                          "reviewer": ["supervisor"]
                        },
                        "parameters": {}
                      }
                    ]
                  }
                }
                """);

        List<String> violations = validatePattern(config, "capture/v1", "review/v1");

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.contains("does not allow action 'review'")));
    }

    @Test
    void activityPattern_requiredParametersRejectedWhenMissing() {
        JsonNode config = parse("""
                {
                  "roles": {
                    "submitter": ["capture"],
                    "supervisor": ["review"]
                  },
                  "pattern": {
                    "subject": null,
                    "event": [
                      {
                        "ref": "multi_step_approval/v1",
                        "composition": "event",
                        "shape_roles": {
                          "submission": ["submission/v1"],
                          "level_decision": ["level_decision/v1"]
                        },
                        "participant_roles": {
                          "submitter": ["submitter"],
                          "level_1_reviewer": ["supervisor"],
                          "level_2_reviewer": ["supervisor"]
                        },
                        "parameters": {}
                      }
                    ]
                  }
                }
                """);

        List<String> violations = validatePattern(config, "submission/v1", "level_decision/v1");

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.contains("missing required parameters.levels")));
    }

    @Test
    void activityPattern_multiStepApprovalRequiresConfiguredLevelReviewers() {
        JsonNode config = parse("""
                {
                  "roles": {
                    "submitter": ["capture"],
                    "supervisor": ["review"]
                  },
                  "pattern": {
                    "subject": null,
                    "event": [
                      {
                        "ref": "multi_step_approval/v1",
                        "composition": "event",
                        "shape_roles": {
                          "submission": ["submission/v1"],
                          "level_decision": ["level_decision/v1"]
                        },
                        "participant_roles": {
                          "submitter": ["submitter"],
                          "level_1_reviewer": ["supervisor"]
                        },
                        "parameters": {"levels": 2}
                      }
                    ]
                  }
                }
                """);

        List<String> violations = validatePattern(config, "submission/v1", "level_decision/v1");

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.contains("missing required participant_roles.level_2_reviewer")));
    }

    @Test
    void activityPattern_transferSupervisorRequiredWhenDiscrepancyRolesMapped() {
        JsonNode config = parse("""
                {
                  "roles": {
                    "sender": ["capture"],
                    "receiver": ["capture"],
                    "supervisor": ["review"]
                  },
                  "pattern": {
                    "subject": {
                      "ref": "transfer_with_acknowledgment/v1",
                      "composition": "subject",
                      "shape_roles": {
                        "dispatch": ["dispatch/v1"],
                        "receipt": ["receipt/v1"],
                        "discrepancy_resolution": ["discrepancy_resolution/v1"]
                      },
                      "participant_roles": {
                        "sender": ["sender"],
                        "receiver": ["receiver"]
                      },
                      "parameters": {}
                    },
                    "event": []
                  }
                }
                """);

        List<String> violations = validatePattern(config,
                "dispatch/v1", "receipt/v1", "discrepancy_resolution/v1");

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.contains("missing required participant_roles.supervisor")));
    }

    @Test
    void activityPattern_eventReviewOverlayCanActivateOnSubjectOwnedShape() {
        JsonNode config = parse("""
                {
                  "roles": {
                    "sender": ["capture"],
                    "receiver": ["capture"],
                    "supervisor": ["review"]
                  },
                  "pattern": {
                    "subject": {
                      "ref": "transfer_with_acknowledgment/v1",
                      "composition": "subject",
                      "shape_roles": {
                        "dispatch": ["dispatch/v1"],
                        "receipt": ["receipt/v1"]
                      },
                      "participant_roles": {
                        "sender": ["sender"],
                        "receiver": ["receiver"]
                      },
                      "parameters": {}
                    },
                    "event": [
                      {
                        "ref": "capture_with_review/v1",
                        "composition": "event",
                        "shape_roles": {"review_decision": ["dispatch_review/v1"]},
                        "activation_roles": {"on_shapes": ["dispatch/v1"]},
                        "participant_roles": {
                          "capturer": ["sender"],
                          "reviewer": ["supervisor"]
                        },
                        "parameters": {}
                      }
                    ]
                  }
                }
                """);

        List<String> violations = validatePattern(config,
                "dispatch/v1", "receipt/v1", "dispatch_review/v1");

        assertTrue(violations.isEmpty(), "Expected no violations, got: " + violations);
    }

    @Test
    void activityPattern_validOngoingResolutionBinding_passesAfterBackfillLanded() {
        JsonNode config = parse("""
                {
                  "roles": {
                    "chv": ["capture"],
                    "supervisor": ["review"]
                  },
                  "pattern": {
                    "subject": {
                      "ref": "ongoing_resolution/v1",
                      "composition": "subject",
                      "shape_roles": {
                        "opening": ["opening/v1"],
                        "interaction": ["interaction/v1"],
                        "resolution": ["resolution/v1"],
                        "closure_review": ["closure_review/v1"]
                      },
                      "participant_roles": {
                        "assigned_worker": ["chv"],
                        "supervisor": ["supervisor"]
                      },
                      "parameters": {}
                    },
                    "event": []
                  }
                }
                """);

        List<String> violations = validatePattern(config,
                "opening/v1", "interaction/v1", "resolution/v1", "closure_review/v1");

        assertTrue(violations.isEmpty(), "Expected no violations, got: " + violations);
    }

    @Test
    void flagSeverityOverrides_validFlatMap_passes() {
        JsonNode overrides = parse("""
                {
                  "role_stale": "informational",
                  "temporal_authority_expired": "blocking"
                }
                """);

        List<String> violations = DeployTimeValidator.validateFlagSeverityOverrides(overrides);

        assertTrue(violations.isEmpty(), "Expected no violations, got: " + violations);
    }

    @Test
    void flagSeverityOverrides_unknownCategory_rejected() {
        JsonNode overrides = parse("""
                {"not_a_flag": "blocking"}
                """);

        List<String> violations = DeployTimeValidator.validateFlagSeverityOverrides(overrides);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.contains("Unknown flag category 'not_a_flag'")));
    }

    @Test
    void flagSeverityOverrides_invalidSeverity_rejected() {
        JsonNode overrides = parse("""
                {"role_stale": "urgent"}
                """);

        List<String> violations = DeployTimeValidator.validateFlagSeverityOverrides(overrides);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.contains("Invalid severity 'urgent'")));
    }

    @Test
    void flagSeverityOverrides_reservedCategory_rejected() {
        JsonNode overrides = parse("""
                {"reserved": "blocking"}
                """);

        List<String> violations = DeployTimeValidator.validateFlagSeverityOverrides(overrides);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.contains("Reserved flag category")));
    }

    @Test
    void flagSeverityOverrides_nestedPerActivity_rejected() {
        JsonNode overrides = parse("""
                {
                  "monitoring": {
                    "role_stale": "blocking"
                  }
                }
                """);

        List<String> violations = DeployTimeValidator.validateFlagSeverityOverrides(overrides);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.contains("nested/per-activity")));
    }

    @Test
    void shapeUniqueness_validDefinition_passes() {
        ObjectNode schema = testShape.schemaJson().deepCopy();
        schema.set("uniqueness", parse("""
                {
                  "scope": ["subject_ref", "activity_ref", "payload.status"],
                  "period": {"type": "calendar_week", "timezone": "deployment"},
                  "device_action": "warn"
                }
                """));

        List<String> violations = DeployTimeValidator.validateShapeUniqueness("test_shape/v1", schema);

        assertTrue(violations.isEmpty(), "Expected no violations, got: " + violations);
    }

    @Test
    void shapeUniqueness_unknownPayloadField_rejected() {
        ObjectNode schema = testShape.schemaJson().deepCopy();
        schema.set("uniqueness", parse("""
                {"scope": ["payload.not_a_field"], "device_action": "warn"}
                """));

        List<String> violations = DeployTimeValidator.validateShapeUniqueness("test_shape/v1", schema);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.contains("unknown payload field 'not_a_field'")));
    }

    @Test
    void shapeUniqueness_multiSelectPayloadField_rejected() {
        ObjectNode schema = testShape.schemaJson().deepCopy();
        schema.set("uniqueness", parse("""
                {"scope": ["payload.stockout_items"], "device_action": "warn"}
                """));

        List<String> violations = DeployTimeValidator.validateShapeUniqueness("test_shape/v1", schema);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.contains("non-scalar type 'multi_select'")));
    }

    @Test
    void shapeUniqueness_unsupportedPeriod_rejected() {
        ObjectNode schema = testShape.schemaJson().deepCopy();
        schema.set("uniqueness", parse("""
                {
                  "scope": ["subject_ref"],
                  "period": {"type": "rolling_7_days", "timezone": "deployment"}
                }
                """));

        List<String> violations = DeployTimeValidator.validateShapeUniqueness("test_shape/v1", schema);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.contains("rolling_7_days")));
    }

    @Test
    void shapeUniqueness_oldActionKey_rejected() {
        ObjectNode schema = testShape.schemaJson().deepCopy();
        schema.set("uniqueness", parse("""
                {"scope": ["subject_ref"], "action": "warn"}
                """));

        List<String> violations = DeployTimeValidator.validateShapeUniqueness("test_shape/v1", schema);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.contains("old key 'action'")));
    }

    @Test
    void shapeUniqueness_invalidDeviceAction_rejected() {
        ObjectNode schema = testShape.schemaJson().deepCopy();
        schema.set("uniqueness", parse("""
                {"scope": ["subject_ref"], "device_action": "block"}
                """));

        List<String> violations = DeployTimeValidator.validateShapeUniqueness("test_shape/v1", schema);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.contains("device_action 'block'")));
    }

    @Test
    void shapeUniqueness_expressionLikeCustomKey_rejected() {
        ObjectNode schema = testShape.schemaJson().deepCopy();
        schema.set("uniqueness", parse("""
                {"scope": ["subject_ref"], "when": {"eq": ["payload.status", "active"]}}
                """));

        List<String> violations = DeployTimeValidator.validateShapeUniqueness("test_shape/v1", schema);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.contains("key 'when' is not supported")));
    }

    // --- Helpers ---

    private void addField(ArrayNode fields, String name, String type, boolean required) {
        addField(fields, name, type, required, null);
    }

    private void addField(ArrayNode fields, String name, String type, boolean required, List<String> options) {
        ObjectNode field = objectMapper.createObjectNode();
        field.put("name", name);
        field.put("type", type);
        field.put("required", required);
        field.put("deprecated", false);
        field.put("display_order", fields.size() + 1);
        if (options != null) {
            ArrayNode optArray = field.putArray("options");
            options.forEach(optArray::add);
        }
        fields.add(field);
    }

    private JsonNode parse(String json) {
        try {
            return objectMapper.readTree(json);
        } catch (Exception e) {
            throw new RuntimeException("Bad test JSON", e);
        }
    }

    private ExpressionRule makeRule(String fieldName, String ruleType, JsonNode expression, String message) {
        return new ExpressionRule(
                UUID.randomUUID(), "test_activity", "test_shape/v1",
                fieldName, ruleType, expression, message, null);
    }

    private DeployTimeValidator makeValidator() {
        // For unit tests, we use a validator with null repositories
        // Only validateRule is used (doesn't need repos)
        return new DeployTimeValidator(null, null);
    }

    private List<String> validatePattern(JsonNode activityConfig, String... knownShapeRefs) {
        java.util.Set<String> known = new java.util.LinkedHashSet<>(java.util.Arrays.asList(knownShapeRefs));
        return DeployTimeValidator.validateActivityPatternBinding(
                "test_activity", activityConfig, known::contains, new PatternRegistry());
    }
}
