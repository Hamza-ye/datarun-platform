# Pattern Inventory Walkthrough Assessment

Status: Historical/pre-convergence assessment against accepted ADR-001 through ADR-005 baseline

This document assesses `../../exploration/28-pattern-inventory-walkthrough.md` through the accepted baseline, validated boundary map, and later-source classification guardrails. The walkthrough is not authority for platform behavior. It is useful candidate material for the `Exact Pattern Registry Inventory` and `Formal Pattern Registry Schema Format` gaps.

## Source Basis

Assessment inputs:

- `04-architecture-baseline-v0.md`
- `05-decision-gap-register.md`
- `07-system-boundary-map.md`
- `08-baseline-acceptance-check.md`
- `13-adr009-duality-rule-assessment.md`
- `../10-adr1-5-rest-state-closure-register.md`
- `../../adrs/adr-005-state-progression.md`, only to verify Pattern Registry and composition closure
- `../../exploration/28-pattern-inventory-walkthrough.md`

Not used as authority:

- The walkthrough's references to `architecture/primitives.md`, `architecture/contracts.md`, `architecture/cross-cutting.md`, implementation-era contract IDs, or prior architecture component names.
- The walkthrough's self-description as "specification-grade" or as resolving inventory. Under the current baseline, it can only be a candidate closure input.

## Assessment Scope

ADR-005 closes the Pattern Registry mechanism and composition model but explicitly leaves exact pattern inventory, pattern skeletons, and formal pattern schema format open. ADR-009 then clarifies the classification split:

- Pattern mechanism: platform-fixed, Projection / Workflow State boundary.
- Pattern bindings and activity instances: deployer-configured, Configuration boundary.

The walkthrough mostly proposes candidate inventory and schema detail. It should not be read as implementation authority or as already-accepted platform specification.

Participant-role language in this assessment means workflow capacity inside a pattern, such as capturer, reviewer, approver, sender, or receiver. It must not be read as a fixed product persona, actor subclass, permission shortcut, or service boundary. Concrete deployments bind those capacities to configured roles, assignments, scopes, and operation classes.

## Boundary Routing

| Claim Area | Primary Boundary | Secondary Boundaries | Reason |
|---|---|---|---|
| Pattern Registry inventory candidate | Projection / Workflow State | Configuration | Pattern mechanism and skeletons are platform-owned; deployers bind instances through configuration. |
| pattern schema conventions | Projection / Workflow State | Configuration; Event Envelope / Schema | Transition tables consume event `type`, shape roles, and activity context without adding envelope fields. |
| pattern parameterization | Configuration | Projection / Workflow State; Assignment / Authority / Sync | Deployer-provided shape, role, deadline, and numeric values configure instances of platform-owned patterns. |
| pattern composition validation | Projection / Workflow State | Configuration | ADR-005 composition rules are platform-owned and enforced through deploy-time validation. |
| no-pattern activity | Configuration | Projection / Workflow State | Activity can exist without selecting a workflow pattern; projection does not derive workflow state. |
| entity_lifecycle candidate | Projection / Workflow State | Configuration | Potential new platform-owned pattern, not a parameterization of `case_management` if accepted. |

## Claim Classification

| Walkthrough Claim | Classification | Assessment |
|---|---|---|
| Defines normalized pattern fields: states, transitions, roles, projections, parameters, composition type | Open-gap closure candidate | Useful candidate schema surface for the `Formal Pattern Registry Schema Format` gap. Needs rewriting without implementation-era contract IDs, with ADR-009 mechanism/instance split, and with participant roles treated as capacities/bindings rather than actor classes. |
| Transition tuple `(current_state, event.type, shape_role) -> next_state` | Open-gap closure candidate | Compatible with ADR-005 and ADR-007 if `event.type` remains the six-value processing axis and `shape_role` maps to `shape_ref` through configuration. |
| Eliminates implicit transitions | Open-gap closure candidate | Useful spec discipline. Not baseline by itself, but likely safe if included in formal pattern schema. |
| Subject-level vs event-level composition types | Consistent elaboration plus pattern-schema candidate | Directly elaborates ADR-005 composition rules. Exact state keys are spec/implementation detail, not architecture baseline. |
| Shape binding split into transition-bound and activation-bound | Open-gap closure candidate | Valuable refinement for Rule 5. It prevents overlay review patterns from falsely conflicting with host pattern shapes. Needs formal review before acceptance. |
| Pattern projections: auto-maintained, pattern-specific, deployer-configured | Open-gap closure candidate with ADR-009 caution | Useful taxonomy, but must be split carefully: platform-fixed pattern projections belong to Projection / Workflow State; deployer projection rules belong to Configuration. |
| Pattern parameterization at L0 | Consistent elaboration | Compatible with ADR-004 gradient and ADR-009. Deployer maps shapes, roles, deadlines, and numeric values; deployer does not author state machines. |
| S00 requires no pattern / `pattern: none` | Consistent elaboration | Compatible with ADR-005's simplicity proof. Safe as candidate spec language: Pattern Registry is opt-in for activities that need workflow state. |
| `capture_with_review` inventory entry | Open-gap closure candidate | Strong candidate for initial inventory because ADR-005 names it as an existence proof. Exact states/projections/overlay behavior need formal pattern-spec review. |
| `case_management` inventory entry | Open-gap closure candidate | Strong candidate for initial inventory. Watch the claim that unknown shapes in an activity produce no flag; this must not weaken ADR-005 `transition_violation` semantics for shapes that are transition-bound to the pattern. |
| `multi_step_approval` inventory entry | Open-gap closure candidate | Strong candidate. Dual subject-level/event-level mode is useful but should be treated as schema/detail until accepted. |
| `transfer_with_acknowledgment` inventory entry | Open-gap closure candidate | Strong candidate. Multi-level distribution as multiple related pattern instances is compatible with ADR-009 and avoids creating a broad distribution super-pattern. |
| Composition validation against ADR-005 Rules 1-5 | Consistent elaboration plus deploy-time validation candidate | Useful as validation examples. Exact validator checks remain implementation/tooling or formal schema detail. |
| `entity_lifecycle` cannot be parameterized from `case_management` | Open-gap closure candidate | The reasoning is plausible: cyclical verification differs structurally from case closure. Treat `entity_lifecycle` as a candidate future pattern, not accepted inventory. |
| `entity_lifecycle` may be added later with no data migration | Deferred platform evolution candidate | Compatible with Pattern Registry append-only evolution if no envelope fields or type values change. It still requires platform decision/spec work before shipping. |
| "Patterns are config, not events" wording | Needs narrowing | Under ADR-009, pattern mechanism is platform-fixed; deployer pattern bindings are configuration. Do not carry forward wording that classifies the mechanism as config. |
| Uses implementation contract IDs such as C15/C18/C20 | Not carry-forward | Those IDs come from pre-convergence contract extraction and should not be used as authority in atomized specs. |

## Candidate Inventory Result

The walkthrough supports the following candidate inventory classification:

| Pattern | Assessment Result | Notes |
|---|---|---|
| `capture_with_review` | Candidate initial inventory | Event-level review flow; overlay mode requires activation-bound shape distinction. |
| `case_management` | Candidate initial inventory | Subject-level lifecycle; exact optional shape-role behavior needs spec review. |
| `multi_step_approval` | Candidate initial inventory | Candidate dual-mode pattern; mode declaration belongs in formal schema. |
| `transfer_with_acknowledgment` | Candidate initial inventory | Subject-level transfer lifecycle; multi-level distribution should be composition over related subjects, not a broader pattern. |
| `entity_lifecycle` | Candidate future pattern | Do not merge into `case_management`; decide separately if early deployments need registry lifecycle behavior. |
| no-pattern activity | Candidate configuration option | Valid activity configuration with no workflow pattern. |

## Safe Carry-Forward Candidates

The following material is safe to carry forward into pattern atomization as candidate language:

- Pattern inventory should start from the four ADR-005 existence-proof patterns, not from arbitrary workflow invention.
- Pattern definitions should use abstract shape roles mapped to concrete `shape_ref` values by deployer configuration.
- Pattern participant names should describe capacities in a behavior skeleton, not fixed platform role classes.
- Pattern definitions should distinguish subject-level and event-level state derivation.
- Pattern definitions should state roles, transitions, state markers, projections, and parameterization points.
- Pattern Registry should support activities with no selected workflow pattern.
- Pattern composition validation should include shape-to-pattern uniqueness, but activation-bound shape references may need separate treatment from transition-bound ownership.
- Multi-level distribution should be modeled through composed transfer instances and payload/reference links, not through a broad special-case mechanism.

## Items Not Safe To Absorb Yet

- The walkthrough's claim that it resolves initial inventory.
- Exact state names, transition tables, projection names, and parameter lists as final spec.
- Exact formal schema format for pattern definitions.
- Implementation-specific state table names or projection-engine algorithms.
- Implementation contract IDs from pre-convergence architecture docs.
- `entity_lifecycle` as accepted initial inventory.
- Any wording that makes patterns deployer-authored config rather than platform-fixed mechanisms selected and parameterized by deployers.
- Any wording that turns capturer, reviewer, approver, sender, receiver, supervisor, coordinator, or auditor labels into platform-owned actor subclasses.

## Baseline Impact

No ADR-001 through ADR-005 baseline item should be changed by this assessment alone.

No new gap is required. The walkthrough is candidate material for existing P1 gaps:

- `Exact Pattern Registry Inventory`
- `Formal Pattern Registry Schema Format`

It also provides supporting candidate detail for existing P2/P3 areas:

- Configuration authoring and deployment UX
- Projection performance and caching
- Reporting and aggregation, if pattern-specific projections feed reporting later

## Recommended Next Step

Use this assessment to draft a Pattern Registry atomization plan, not the final pattern spec. The first atomization pass should split:

- platform-fixed Pattern Registry mechanism
- pattern definition schema
- candidate initial inventory
- deployer activity bindings and parameterization
- composition validation rules
- explicit hold-backs such as `entity_lifecycle`, exact schema serialization, and reporting aggregation
