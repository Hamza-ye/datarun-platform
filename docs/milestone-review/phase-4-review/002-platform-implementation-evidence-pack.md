# Platform Implementation Evidence Pack

> Status: **implementation evidence pack — not architecture authority**  
> Architecture authority: `canonical-decision-ledger.md`  
> Generated for: scenario walkthroughs and implementation gap analysis
>
> Date: 2026-05-29

## 1. How to read this file

This file consolidates the currently uploaded phase specifications, IDRs, and implementation-alignment review into one compact review surface.

It is designed to answer questions such as:

```text
Can the documented implementation support this real-world scenario?
Which pieces are architecturally required?
Which pieces are documented as implemented?
Which pieces are only planned or decision-level?
Which pieces still need code inspection or runtime verification?
```

This file must not be treated as proof that the code works. It records what the available documents claim, decide, test, or defer.

### Authority rule

- `canonical-decision-ledger.md` is the architecture authority.
- This evidence pack is implementation evidence.
- Phase files and IDRs are implementation/planning evidence.
- A claim marked `claimed_implemented` or `tested_in_docs` still requires source-code or runtime verification before being treated as operational proof.

### Claim language rule

When using this file:

```text
Say: "The docs claim this was implemented."
Do not say: "The platform definitely implements this" unless code/runtime evidence is also available.
```

---

## 2. Evidence levels

| Level | Meaning | Trust boundary |
|---|---|---|
| `architectural` | Required by `canonical-decision-ledger.md`. | Architecture truth, not implementation proof. |
| `implementation_decided` | An IDR records an implementation decision. | Design intent / implementation decision. |
| `planned` | A phase spec says the capability should be built. | Plan, not completion. |
| `claimed_implemented` | A phase/IDR says the capability landed or deliverables shipped. | Documentation claim. |
| `tested_in_docs` | Quality gates, tests, or counts are documented as passing. | Test claim, not independently verified here. |
| `needs_code_verification` | The uploaded docs are insufficient to prove actual code behavior. | Requires code inspection. |
| `needs_runtime_verification` | The behavior must be exercised end-to-end with data. | Requires running scenario/test. |
| `scenario_verified` | A later scenario walkthrough has verified fit. | Not assigned in this pack; to be added after scenario reviews. |
| `known_gap` | Explicitly deferred, missing, or contradicted by the docs. | Do not assume available. |

---

## 3. Source files covered

### Architecture authority

```text
docs/architecture/adrs-decisions-canonical-ledger/canonical-decision-ledger.md, and README.md for index
```

### Review/support artifacts

```text
docs/reviews/post-phase-4-review/001-implementation-decision-alignment-review.md
docs/reviews/post-phase-4-review/002-platform-implementation-evidence-pack.md
docs/implementation/module-interfaces.md
```

### Phase files

at: `docs/implementation/phases/`

```text
phase-0.md
phase-1.md
phase-2.md
phase-3.md
phase-3d.md
phase-3e.md
phase-4.md
```

### Implementation decision records

at: `docs/decisions/`. indexed at: `docs/decisions/INDEX.md`

```text
idr-001-test-infrastructure.md
idr-002-pg-idkit-dev.md
idr-003-snake-case-json.md
idr-004-networknt-validator.md
idr-005-ci-github-actions.md
idr-006-thymeleaf-admin.md
idr-007-concurrency-detection.md
idr-008-server-event-producer.md
idr-009-alias-table.md
idr-010-conflict-detection-intercept.md
idr-011-identity-conflict-scope.md
idr-012-sqflite-memory-path.md
idr-013-assignment-payload.md
idr-014-materialized-path-locations.md
idr-015-scope-filtered-sync-query.md
idr-016-actor-token-table.md
idr-017-shape-storage.md
idr-018-expression-grammar.md
idr-019-config-package.md
idr-020-pattern-state-machine-representation.md
idr-021-role-action-enforcement-model.md
idr-022-flag-severity-and-domain-uniqueness.md
idr-023-role-action-domain-boundary-and-assignment-administration.md
idr-024-multi-axis-assignment-containment.md
idr-025-pattern-definition-contract-and-delivery.md
idr-026-conflict-resolver-routing-and-single-writer-resolution.md
```


---

## 4. Executive evidence summary

The uploaded documents describe a platform built in phases:

| Phase | Main claim | Evidence level |
|---|---|---|
| Phase 0 | Offline capture → local store → sync → server persists → projection/admin visibility. | `claimed_implemented`, `tested_in_docs` |
| Phase 1 | Identity and integrity: conflict detection, alias projection, merge/split, flagged-event exclusion. | `claimed_implemented`, `tested_in_docs` |
| Phase 2 | Authorization and multi-actor sync: assignment-based access, scope-filtered pull, auth flags, selective retention. | `claimed_implemented`, `tested_in_docs` |
| Phase 3 | Configuration: shapes, expressions, deploy-time validation, config package, at-most-two device versions. | Mixed: `claimed_implemented`, with some quality gates in docs not all marked `[x]` in the excerpted phase plan. |
| Phase 3d | Close-out: `activity_ref`, sensitivity package surface, `context.*` resolver tests. | `claimed_implemented` / debt-closure claim |
| Phase 3e | Retrofit: closed six-value `type`, platform-bundled internal shapes, `shape_ref` filters. | `claimed_implemented`, quality gates specified |
| Phase 4 | Role-action, severity, domain uniqueness, pattern registry, pattern-state projection, backfill, resolver routing, transition violations. | `claimed_implemented`, `tested_in_docs` according to Phase 4 completion audit |

The implementation alignment review found **no fatal architectural drift** but identified follow-up items:

```text
- events.location_path needs an explicit historical immutability boundary.
- resolver routing must be runtime-enforced for every new flag.
```

---

## 5. Capability status matrix

| Capability | Status from docs | Confidence from docs | Needs code verification | Needs runtime scenario |
|---|---|---:|:---:|:---:|
| Event envelope validation | `claimed_implemented`, `tested_in_docs` | High | Yes | Yes |
| Append-only event store | `claimed_implemented`, `tested_in_docs` | High | Yes | Yes |
| Basic push/pull sync | `claimed_implemented`, `tested_in_docs` | High | Yes | Yes |
| Mobile offline capture | `claimed_implemented`, `tested_in_docs` | High | Yes | Yes |
| Basic projection engine | `claimed_implemented`, `tested_in_docs` | High | Yes | Yes |
| Identity merge/split | `claimed_implemented`, `tested_in_docs` | Medium | Yes | Yes |
| Alias projection | `claimed_implemented`, `tested_in_docs` | Medium | Yes | Yes |
| Conflict detection pipeline | `claimed_implemented`, `tested_in_docs` | Medium | Yes | Yes |
| Flag lifecycle | `claimed_implemented`, `tested_in_docs` | Medium | Yes | Yes |
| Resolver routing | `claimed_implemented` in Phase 4 / IDR-026 | Medium | Yes | Yes |
| Assignment model | `claimed_implemented`, `tested_in_docs` | Medium/High | Yes | Yes |
| Scope-filtered sync | `claimed_implemented`, `tested_in_docs` | Medium | Yes | Yes |
| Selective retain / purge | `claimed_implemented`, `tested_in_docs` | Medium | Yes | Yes |
| Role-action enforcement | `claimed_implemented` in Phase 4 | Medium | Yes | Yes |
| Shape registry/storage | `implementation_decided`, `claimed_implemented` | Medium | Yes | Yes |
| Expression evaluator | `implementation_decided`, `claimed_implemented` | Medium | Yes | Yes |
| Config package delivery | `implementation_decided`, `claimed_implemented` | Medium | Yes | Yes |
| Sensitivity surface | `claimed_implemented` as config/package surface | Medium | Yes | Yes |
| Flag severity config | `claimed_implemented` in Phase 4 | Medium | Yes | Yes |
| Domain uniqueness | `claimed_implemented` in Phase 4 | Medium | Yes | Yes |
| Pattern registry | `claimed_implemented` in Phase 4 | Medium | Yes | Yes |
| Pattern definition delivery | `claimed_implemented`, quality gates documented | Medium | Yes | Yes |
| Pattern-state projection | `claimed_implemented`, quality gates documented | Medium | Yes | Yes |
| Transition violation detection | `claimed_implemented`, quality gates documented | Medium | Yes | Yes |
| Subject-history backfill | `claimed_implemented`, quality gates documented | Medium | Yes | Yes |
| General trigger engine | Ambiguous/stale summary vs Phase 4 scope; do not assume. | Low | Yes | Yes |
| Auto-resolution execution | `known_gap` unless later source proves otherwise. | Low | Yes | Yes |
| Production authentication/OIDC | `known_gap` / not proven. | Low | Yes | Yes |
| Entity lifecycle / S06 support | `known_gap`; S06 explicitly deferred in Phase 4. | Low | Yes | Yes |

---

## 6. Capability evidence cards

### 6.1 Event store and envelope validation

```yaml
id: event_store_and_envelope
status_from_docs: claimed_implemented
evidence_level:
  - architectural
  - claimed_implemented
  - tested_in_docs
confidence_from_docs: high
canonical_constraints:
  - immutable append-only operational event stream
  - projections derived and rebuildable
  - eleven conceptual envelope fields
  - closed six-value type vocabulary
implementation_evidence:
  - phase-0 claims server PostgreSQL event table and mobile SQLite event table
  - phase-0 quality gates claim malformed envelopes rejected and accepted events validate against envelope schema
  - phase-3e claims schema retrofit to exactly six type values
implementation_claims:
  - server push deduplicates by event id
  - server assigns watermarks
  - mobile creates complete 11-field events offline
  - contracts/envelope.schema.json exists as the validation surface
known_risks:
  - docs do not prove current schema files still match code
  - two envelope schema copies can diverge unless parity test remains active
verification_questions:
  - Does the current server reject malformed envelopes before insert?
  - Does the current server reject illegal type values such as conflict_detected?
  - Are events immutable after insert at database and application layers?
  - Does mobile still emit every required envelope field?
scenario_checks:
  - create event offline
  - sync duplicate event twice
  - attempt malformed envelope
  - attempt illegal type value
```

### 6.2 Basic push/pull sync

```yaml
id: basic_sync
status_from_docs: claimed_implemented
evidence_level:
  - claimed_implemented
  - tested_in_docs
confidence_from_docs: high
canonical_constraints:
  - idempotent, order-independent exchange
  - event stream remains canonical
implementation_evidence:
  - phase-0 quality gates claim push/pull watermarks, duplicate push idempotency, and real PostgreSQL integration tests
implementation_claims:
  - POST /api/sync/push accepts event arrays, deduplicates, assigns watermarks
  - POST /api/sync/pull returns events since watermark, paginated
  - mobile push/pull updates local watermarks and pushed status
known_risks:
  - phase docs prove intent/test claims, not current runtime behavior
verification_questions:
  - Does pull ordering remain stable under pagination?
  - Do duplicate pushes return stable responses?
  - Does mobile recover from partial push/pull failures?
scenario_checks:
  - offline capture on device A, sync, pull to device B
  - interrupted sync retry
  - large event set pagination
```

### 6.3 Projection engine

```yaml
id: projection_engine
status_from_docs: claimed_implemented
evidence_level:
  - architectural
  - claimed_implemented
  - tested_in_docs
confidence_from_docs: medium_high
canonical_constraints:
  - projections are derived and rebuildable
  - unresolved flagged events excluded from authoritative state derivation
  - event stream wins over projections
implementation_evidence:
  - phase-0 claims minimal per-subject projection
  - phase-1 claims alias resolution and flagged-event exclusion
  - phase-4 claims pattern-state projection and server/mobile fixture parity
implementation_claims:
  - subject list/current state derived from events
  - event timelines include flagged events
  - unresolved flagged events excluded from current state and pattern state
  - accepted resolutions re-derive state including source event
known_risks:
  - projection behavior is scenario-sensitive and must be tested against realistic data
  - performance optimizations must not become canonical truth
verification_questions:
  - Does flagged-event exclusion work consistently across subject projection and pattern projection?
  - Does resolving a flag re-include or keep excluding the source event correctly?
  - Do server/mobile projections match on shared fixtures?
scenario_checks:
  - flagged event visible in timeline but absent from current state
  - resolve as accepted and rebuild projection
  - resolve as rejected and confirm current state unchanged
```

### 6.4 Identity resolution and alias projection

```yaml
id: identity_resolution
status_from_docs: claimed_implemented
evidence_level:
  - implementation_decided
  - claimed_implemented
  - tested_in_docs
confidence_from_docs: medium
canonical_constraints:
  - merge/split append events, never rewrite historical events
  - alias projection is rebuildable
  - stale references are detected before alias normalization
implementation_evidence:
  - idr-009 decides materialized subject_aliases as rebuildable projection with eager transitive closure
  - phase-1 claims merge/split events, alias resolution, stale_reference detection, and quality gates
implementation_claims:
  - subjects_merged/v1 emitted as type=capture shape_ref=subjects_merged/v1
  - subject_split/v1 emitted as type=capture shape_ref=subject_split/v1
  - alias lookup is single-hop
  - historical events remain under original references
known_risks:
  - lifecycle projection and advisory lock behavior need code verification
  - split semantics are complex under real-world mistaken merges
verification_questions:
  - Does merge preserve historical event subject_ref values?
  - Does stale_reference detection inspect raw refs before alias projection?
  - Does alias table rebuild from events produce identical rows?
  - Do concurrent merges serialize correctly?
scenario_checks:
  - duplicate subject merge
  - stale offline event after merge
  - wrong merge followed by split
  - transitive merge A→B→C
```

### 6.5 Conflict detection and flag lifecycle

```yaml
id: conflict_detection_and_flags
status_from_docs: claimed_implemented
evidence_level:
  - architectural
  - implementation_decided
  - claimed_implemented
  - tested_in_docs
confidence_from_docs: medium
canonical_constraints:
  - accept-and-flag
  - flags are event-stream anomaly representation
  - detect-before-act
  - source-only flagging
implementation_evidence:
  - idr-010 decides same-request, two-transaction conflict detection
  - phase-1 claims concurrent_state_change, stale_reference, identity_conflict pipeline
  - phase-2 claims authorization flags
  - idr-022 claims domain_uniqueness_violation
  - phase-4 claims transition_violation
implementation_claims:
  - Tx1 persists events; Tx2 evaluates and persists flags
  - detector failure does not roll back event persistence
  - sweep job can repair missed flags
  - deterministic flag identity avoids duplicate flags
known_risks:
  - multiple flags on one event require resolver convergence
verification_questions:
  - If detector fails, is event persisted and later swept?
  - Do repeated detections produce no duplicate flag events?
  - Does every flag use type=alert and shape_ref=conflict_detected/v1?
  - Does each new flag carry designated_resolver?
scenario_checks:
  - concurrent edits from two devices
  - detector failure then sweep
  - duplicate domain event
  - invalid workflow transition
```

### 6.6 Resolver routing and single-writer resolution

```yaml
id: resolver_routing
status_from_docs: claimed_implemented
confidence_from_docs: medium
evidence_level:
  - implementation_decided
  - claimed_implemented
  - needs_code_verification
canonical_constraints:
  - exactly one canonical resolver per flag
  - conflict resolution online-only
  - non-designated authors cannot create canonical resolution
implementation_evidence:
  - idr-026 defines designated_resolver requirement, resolver_unassigned sentinel, and exact resolver equality
  - phase-4 sequencing claims runtime resolver designation and single-writer enforcement landed
implementation_claims:
  - new conflict_detected/v1 flags include designated_resolver
  - detector author is separate from resolver
  - auto_eligible does not imply system-owned
  - request body actor ids are not authority
known_risks:
  - JSON schema requirement may lag semantic requirement
  - legacy flags without designated_resolver must not become canonically resolvable by accident
  - resolver reassignment is not landed unless a later source proves it
verification_questions:
  - Does the API reject conflict_resolved/v1 authored by a non-designated actor?
  - Does a body-supplied actor ID fail to impersonate resolver authority?
  - How are resolver_unassigned flags displayed and blocked from clearing?
scenario_checks:
  - scope_violation routed to nearest eligible steward
  - domain_uniqueness routed to common data steward
  - non-resolver attempts resolution
```

### 6.7 Assignment and authorization model

```yaml
id: authorization_assignments_scope
status_from_docs: claimed_implemented
evidence_level:
  - implementation_decided
  - claimed_implemented
  - tested_in_docs
confidence_from_docs: medium_high
canonical_constraints:
  - assignment-based access
  - sync scope equals access scope
  - authority reconstructed from assignment events
  - no authority_context envelope field
implementation_evidence:
  - idr-013 defines assignment_created/v1 and assignment_ended/v1 payloads
  - phase-2 claims assignment service, scope resolver, token auth, scope-filtered sync quality gates
  - idr-024 hardens assignment containment across geographic, subject_list, activity axes
implementation_claims:
  - assignment identity is envelope subject_ref.id with type=assignment
  - AND within assignment axes, OR across assignments
  - assignment events for pulling actor are always synced
  - creator cannot grant broader scope than they possess
known_risks:
  - production authentication/OIDC is not proven
  - bootstrap/root provisioning behavior is security-sensitive
  - assignment end authority must be verified at runtime
verification_questions:
  - Can an actor create a broader assignment by using null on one axis?
  - Are empty arrays rejected or treated as granting no values rather than unrestricted?
  - Does ending an assignment require target-assignment authority?
  - Are actor IDs bound to authenticated request context, not body fields?
scenario_checks:
  - supervisor assigns field worker to district/activity
  - field worker loses scope while offline
  - reassignment mid-campaign
  - subject-list-only assignment
```

### 6.8 Scope-filtered sync and selective retention

```yaml
id: scope_filtered_sync
status_from_docs: claimed_implemented
evidence_level:
  - implementation_decided
  - claimed_implemented
  - tested_in_docs
confidence_from_docs: medium
canonical_constraints:
  - sync scope equals access scope
  - no sync-all-hide-later
  - normal live sync remains request-time scoped
implementation_evidence:
  - idr-015 decides denormalized events.location_path for pull filtering
  - phase-2 claims C10 exact authorized pull tests
  - phase-4 claims subject-history backfill is separate from normal live sync
implementation_claims:
  - geographic filtering at SQL level
  - activity and subject_list filtering applied with AND/OR assignment semantics
  - own assignment events always included
  - mobile purges out-of-scope non-own events on scope contraction and keeps own events
known_risks:
  - events.location_path immutability under location reparenting needs explicit verification
  - normal live sync and subject-history backfill must not be conflated
  - sensitivity enforcement beyond packaging needs runtime proof
verification_questions:
  - Does pull exclude unauthorized data for all three scope axes?
  - Does a supervisor receive the correct team data without receiving unrelated scopes?
  - Does selective-retain keep own events and purge others when scope contracts?
  - Does subject-history backfill require request-time authorization per page?
scenario_checks:
  - field worker restricted to village
  - supervisor over district
  - reassignment from one activity to another
  - offline work after scope contraction
```

### 6.9 Shape registry and shape storage

```yaml
id: configuration_shapes
status_from_docs: claimed_implemented
confidence_from_docs: medium
evidence_level:
  - implementation_decided
  - claimed_implemented
canonical_constraints:
  - mandatory shape_ref
  - versioned shape snapshots
  - all historical shape versions remain interpretable
  - deployment config cannot redefine platform mechanisms
implementation_evidence:
  - idr-017 decides shapes table keyed by name/version, standalone JSONB snapshots, sensitivity, subject_binding
  - phase-3 claims deployer-authored shapes, deprecation-only evolution, shape version coexistence
implementation_claims:
  - field vocabulary fixed in IDR-017
  - expressions external to shapes
  - activities table stores config_json
  - shape versions are full snapshots, not deltas at runtime
known_risks:
  - exact field vocabulary is implementation/platform-spec, not architecture proof
  - migration/breaking-change tooling not proven
verification_questions:
  - Can old shape versions still render/project after a new version is published?
  - Are deprecated fields hidden for new entry but preserved in old events?
  - Is subject_binding enforced consistently when creating envelope subject_ref?
scenario_checks:
  - publish v1, capture, publish v2, capture v2, project both
  - deprecated shape still visible for old event timeline
```

### 6.10 Expression evaluator and `context.*`

```yaml
id: expression_evaluator
status_from_docs: claimed_implemented
confidence_from_docs: medium
evidence_level:
  - implementation_decided
  - claimed_implemented
canonical_constraints:
  - bounded expression language
  - zero functions
  - form context and trigger context separated
  - context.* is pre-resolved and read-only
implementation_evidence:
  - idr-018 defines JSON AST, comparison/logical/ref nodes, null handling, namespace strictness
  - phase-3 claims form and trigger contexts wired, with triggers not firing until Phase 4
  - phase-3d closes context resolver test debt for event_count, days_since_last_event, actor.role, actor.scope_name
implementation_claims:
  - server and mobile evaluators use shared JSON fixtures
  - form expressions use payload/entity/context
  - trigger expressions use event only
known_risks:
  - dynamic query creep must remain blocked
  - trigger-context execution is separate from expression parsing
verification_questions:
  - Does DtV reject functions, nested logic, unknown namespaces, and cross-context refs?
  - Do server and mobile evaluators return identical results for fixtures?
  - Are context values static during form fill?
scenario_checks:
  - warning based on payload + entity
  - default based on context.days_since_last_event
  - expression referencing null context property
```

### 6.11 Config package delivery

```yaml
id: config_package_delivery
status_from_docs: claimed_implemented
confidence_from_docs: medium
evidence_level:
  - implementation_decided
  - claimed_implemented
canonical_constraints:
  - config delivered atomically
  - at most two config versions on device
  - deploy-time validation gates package publication
implementation_evidence:
  - idr-019 defines GET /api/sync/config, monotonic package version, full snapshot package_json, current/pending two-slot device model
  - phase-3 claims config publishing pipeline and at-most-two coexistence
implementation_claims:
  - sync pull response includes config_version
  - device downloads config separately if needed
  - unknown top-level keys ignored for forward compatibility
  - sensitivity_classifications and flag_severity_overrides are package keys
known_risks:
  - phase-3 quality gates in the plan excerpt are not all marked [x], though later phase docs imply completion
  - atomicity must be runtime tested around interrupted downloads and in-progress forms
verification_questions:
  - Does device complete an in-progress v1 form before promoting v2?
  - Can a partial config package be observed by runtime code?
  - Does bad config fail publish before devices see it?
scenario_checks:
  - publish new shape while worker has open form
  - failed config download retry
  - mobile receives package with future unknown key
```

### 6.12 Sensitivity surface

```yaml
id: sensitivity_surface
status_from_docs: claimed_implemented_as_surface
confidence_from_docs: medium
evidence_level:
  - claimed_implemented
  - needs_runtime_verification
canonical_constraints:
  - sensitivity is shape/activity-level config, not envelope field
  - no field-level sensitivity in current platform
implementation_evidence:
  - phase-3d claims sensitivity_classifications emitted by ConfigPackager from shapes.sensitivity and activities.sensitivity, device parses/stores, no enforcement in Phase 3d
  - idr-017 and idr-019 carry sensitivity fields/package surface
implementation_claims:
  - levels standard/elevated/restricted represented in config package
known_risks:
  - enforcement behavior is not proven by surface presence
  - field-level sensitivity/encryption/redaction are deferred platform-evolution areas
verification_questions:
  - Does sync filtering enforce sensitivity restrictions, or is this only packaged metadata?
  - Does device retention treat restricted data differently under scope contraction?
  - Does admin/export/audit behavior use sensitivity?
scenario_checks:
  - restricted shape captured by worker
  - supervisor lacking sensitive-access attempts sync/view
```

### 6.13 Flag severity and resolvability

```yaml
id: flag_severity_and_resolvability
status_from_docs: claimed_implemented
confidence_from_docs: medium
evidence_level:
  - implementation_decided
  - claimed_implemented
canonical_constraints:
  - severity separate from resolvability
  - manual_only vs auto_eligible is platform-owned
  - deployers cannot make manual-only flags auto-resolvable
implementation_evidence:
  - idr-022 defines flat deployment-wide flag_severity_overrides with blocking/informational
  - phase-4 claims flag severity landed and tests prove severity does not change resolvability
implementation_claims:
  - missing severity entries use platform defaults
  - per-activity severity rejected in Phase 4
  - unresolved flagged source events remain excluded from authoritative projections regardless of severity
known_risks:
  - canonical ledger amendment pending for two active categories
  - runtime behavior of blocking vs informational must be scenario-tested
verification_questions:
  - Does changing severity leave resolvability unchanged?
  - Does blocking prevent dependent workflow decisions while preserving event visibility?
  - Does informational keep audit/review visibility?
scenario_checks:
  - blocking domain uniqueness conflict
  - informational transition violation
  - manual-only flag with informational severity
```

### 6.14 Domain uniqueness

```yaml
id: domain_uniqueness
status_from_docs: claimed_implemented
confidence_from_docs: medium
evidence_level:
  - implementation_decided
  - claimed_implemented
canonical_constraints:
  - shape-declared uniqueness
  - server authoritative, device advisory
  - accept-and-flag for violations
implementation_evidence:
  - idr-022 activates shapes[*].uniqueness with scope, optional period, device_action
  - phase-4 claims server-side domain_uniqueness_violation detector landed
implementation_claims:
  - device may warn from local data but server remains authoritative
  - duplicate event is accepted and flagged, not rejected
  - flag emits type=alert, shape_ref=conflict_detected/v1, flag_category=domain_uniqueness_violation
  - flag targets incoming conflicting event, not every duplicate
known_risks:
  - uniqueness semantics require domain scenario testing
  - sensitive uniqueness keys may need hashing/redaction behavior verified
verification_questions:
  - Does server detect duplicates missed by offline device?
  - Are unresolved flagged duplicates excluded from authoritative projection?
  - Does resolving accepted/rejected behave correctly?
scenario_checks:
  - one visit per household per week
  - duplicate submitted offline from two devices
  - accepted exception
```

### 6.15 Pattern registry and pattern definition delivery

```yaml
id: pattern_registry_delivery
status_from_docs: claimed_implemented
confidence_from_docs: medium
evidence_level:
  - implementation_decided
  - claimed_implemented
  - tested_in_docs
canonical_constraints:
  - pattern mechanism platform-fixed
  - deployers bind/parameterize; they do not author transitions
  - pattern definitions delivered atomically with config
implementation_evidence:
  - idr-020 defines platform-bundled pattern specs and activity pattern binding set
  - idr-025 defines contracts/patterns and pattern_definitions in config package
  - phase-4 quality gates claim pattern contract validation and mobile package preservation
implementation_claims:
  - pattern refs versioned name/vN
  - semantic changes require new pattern ref version
  - mobile reads pattern definitions from config package rather than hardcoded registry
known_risks:
  - exact production pattern behavior depends on pattern definition files not included here unless present in scenario source set
  - deploy-time validation must reject duplicate shape ownership and invalid roles
verification_questions:
  - Are pattern definitions executable and equal across server/mobile?
  - Does config package include every referenced pattern definition?
  - Does mobile preserve definitions from active config slot?
scenario_checks:
  - capture_with_review binding
  - multi_step_approval binding
  - transfer_with_acknowledgment binding
```

### 6.16 Pattern-state projection and transition violation

```yaml
id: pattern_state_projection
status_from_docs: claimed_implemented
confidence_from_docs: medium
evidence_level:
  - implementation_decided
  - claimed_implemented
  - tested_in_docs
canonical_constraints:
  - workflow state projection-derived
  - no workflow_state/current_state/pattern_ref envelope fields
  - invalid transitions accepted and flagged
  - unresolved flagged events do not advance pattern state
implementation_evidence:
  - idr-020 defines pattern state identity and transition evaluation
  - phase-4 claims server/mobile pattern-state projection, transition_violation detector, and no new envelope type/field
implementation_claims:
  - subject-level state key is subject_ref + activity_ref + binding.ref
  - event-level state key is source_event_id + binding.ref
  - transition_violation emits type=alert, shape_ref=conflict_detected/v1
  - resolving flag as accepted makes source eligible again, but legal transitions still govern advancement
known_risks:
  - S06/entity_lifecycle explicitly deferred
  - no durable workflow-state authority table should be assumed
  - real pattern fit depends on scenario-specific shape roles and transitions
verification_questions:
  - Does an out-of-order valid offline event flag rather than reject?
  - Does the pattern state exclude unresolved flagged events?
  - Does state re-derive correctly after resolution?
  - Does ongoing_resolution rely on subject-history backfill rather than normal sync?
scenario_checks:
  - capture → review → completion
  - transition before required prerequisite
  - mid-campaign reassignment with ongoing_resolution
```

### 6.17 Role-action enforcement

```yaml
id: role_action_enforcement
status_from_docs: claimed_implemented
confidence_from_docs: medium
evidence_level:
  - implementation_decided
  - claimed_implemented
canonical_constraints:
  - device checks advisory only
  - server is authoritative
  - accept-and-flag for unauthorized offline work
  - assignment_changed not authorized through activities[*].roles
implementation_evidence:
  - idr-021 defines activity-scoped role-action permissions and role_stale semantics
  - idr-023 excludes assignment_changed from activity role maps
  - phase-4 claims role-action enforcement landed
implementation_claims:
  - allowed action vocabulary: capture, review, alert, task_created, task_completed
  - role_stale means action-authority mismatch, not any role-label change
  - horizon authority and current authority both checked
known_risks:
  - assignment lifecycle must stay outside role-action enforcement
verification_questions:
  - Does DtV reject assignment_changed in roles?
  - Does server flag disallowed offline work rather than rejecting it?
  - Are device-side hidden/disabled actions advisory only?
scenario_checks:
  - worker attempts review while offline
  - role changed while worker offline
  - supervisor review after reassignment
```

### 6.18 Subject-history backfill

```yaml
id: subject_history_backfill
status_from_docs: claimed_implemented
confidence_from_docs: medium
evidence_level:
  - claimed_implemented
  - tested_in_docs
canonical_constraints:
  - normal live sync remains request-time scoped
  - backfill is separate from audit/historical pull
  - request-time authorization per page
implementation_evidence:
  - phase-4 FP-005 boundary and gates claim subject-history backfill decided/tested before ongoing_resolution enabled
implementation_claims:
  - independent cursor pagination
  - does not lower normal sync watermark
  - alias handling and activity filtering covered by tests
  - audit/historical pull remains out of normal live sync
known_risks:
  - backfill is highly scenario-sensitive
  - can become a hidden sync-all channel if boundaries erode
verification_questions:
  - Does every backfill page re-check current authorization?
  - Does backfill include only history needed by subject-level projections?
  - Does normal sync remain unchanged?
scenario_checks:
  - ongoing_resolution after reassignment
  - supervisor needs prior subject history to compute state
  - actor loses authority mid-backfill
```

### 6.19 Mobile runtime and advisory validation

```yaml
id: mobile_runtime
status_from_docs: claimed_implemented
confidence_from_docs: medium_high
evidence_level:
  - claimed_implemented
  - tested_in_docs
canonical_constraints:
  - offline-first capture
  - local checks advisory where server is authority
  - no device-side canonical authorization
implementation_evidence:
  - phase-0 claims offline capture, local store, sync, form rendering
  - phase-2 claims actor token, local assignments, selective retention
  - phase-3 claims config store and expression evaluator
  - phase-4 claims mobile advisory validators and pattern definition storage
implementation_claims:
  - mobile stores config current/pending
  - mobile evaluates form expressions
  - mobile performs advisory role-action/uniqueness/pattern warnings
  - mobile purges out-of-scope non-own events after scope contraction
known_risks:
  - device checks are not security authority
  - offline scenario complexity requires real device/emulator tests
verification_questions:
  - Can mobile operate with stale config and later reconcile safely?
  - Does local projection match server after sync?
  - Does device avoid creating canonical flags locally unless future platform decision enables it?
scenario_checks:
  - long offline window
  - stale assignment/config
  - local warning missed remote duplicate
```

### 6.20 Admin and operator surfaces

```yaml
id: admin_operator_surfaces
status_from_docs: claimed_implemented_for_dev_and_operations
confidence_from_docs: medium_low
evidence_level:
  - claimed_implemented
  - needs_code_verification
canonical_constraints:
  - admin UI must call application/command surfaces, not bypass decisions
  - production auth not proven unless source/code proves it
implementation_evidence:
  - phase-0 claims minimal admin visibility
  - phase-1 claims flag badges and resolution UI
  - phase-2 claims assignment/location admin templates
  - phase-3 claims config authoring UI
  - phase-4 states HTML admin assignment forms are development-only until production admin/root actor binding exists
implementation_claims:
  - admins can see subject/event timelines, flags, assignments, config authoring surfaces
known_risks:
  - production authentication and authorization for admin surfaces are not proven
  - dev-only forms must not become production authority semantics
verification_questions:
  - Are admin commands bound to authenticated actor context?
  - Can admin UI bypass resolver or assignment containment rules?
  - Are production and development modes separated?
scenario_checks:
  - coordinator resolves flag
  - supervisor creates assignment
  - unauthorized admin attempt
```

---

## 7. Cross-capability flows

### Flow A — Basic offline capture

```text
mobile form → event envelope → local event store → sync push → server event store → projection → admin subject/event view
```

Evidence: Phase 0 claims this loop is implemented and tested.  
Verification: run with a fresh shape, malformed envelope, duplicate push, and server/mobile projection comparison.

### Flow B — Concurrent offline edits

```text
device A and B capture same subject while disconnected → both sync → conflict detector compares knowledge horizon → concurrent_state_change flag → flagged event visible but excluded → resolver resolves → projection re-derives
```

Evidence: Phase 1/IDR conflict pipeline.  
Verification: check per-event horizon, deterministic flag ID, resolver route, and projection re-derivation.

### Flow C — Merge and stale reference

```text
subject A and B duplicate → merge A into B → alias projection A→B → offline event still references A → raw ref checked before alias → stale_reference flag → resolver/auto policy or human resolution
```

Evidence: IDR-009 and Phase 1.  
Verification: confirm no historical rewrite, alias table rebuild, raw-ref detection.

### Flow D — Scope-filtered sync and reassignment

```text
actor has assignment scope → pull returns authorized events only → assignment changes → next pull changes event set → device retains own events and purges out-of-scope non-own events → auth flags handle stale offline pushes
```

Evidence: Phase 2, IDR-013/015/024, Phase 4 reassignment gates.  
Verification: run with geographic + activity + subject_list axes and mid-campaign reassignment.

### Flow E — Config publish and dynamic capture

```text
deployer authors shape/activity/expressions → deploy-time validation → config package snapshot → device receives pending config → promotes at safe boundary → worker captures using dynamic form → server validates payload by shape_ref
```

Evidence: Phase 3, IDR-017/018/019, Phase 3d.  
Verification: run v1/v2 coexistence, invalid config rejection, in-progress form switch.

### Flow F — Domain uniqueness

```text
shape declares uniqueness → device may warn from local data → server checks authoritative event store during push → duplicate accepted and flagged → resolver accepts/rejects → projection re-derives
```

Evidence: IDR-022 and Phase 4.  
Verification: run duplicate missed by offline device.

### Flow G — Pattern workflow

```text
activity binds platform pattern → config package includes pattern definitions → events project pattern state → invalid transition accepted and flagged → unresolved flag excludes state advancement → resolution re-derives state
```

Evidence: IDR-020/025 and Phase 4.  
Verification: run valid and invalid transition, server/mobile projection parity, flag resolution.

---

## 8. Known weak spots and do-not-assume list

These items must not be assumed available or correct merely because the architecture supports them.

| Item                                                                  | Current evidence status                                                                                                  | How to verify or handle                                                                  |
| --------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------ | ---------------------------------------------------------------------------------------- |
| Production authentication/OIDC                                        | Not proven. Phase 4 warns not to take production auth unless explicitly in scope.                                        | Inspect auth code/config and run authz tests.                                            |
| Auto-resolution execution                                             | Not landed according to Phase 4 completion notes, despite earlier phase summary wording.                                 | Treat as missing unless later source/code proves it.                                     |
| Resolver reassignment                                                 | Not landed unless later source proves it.                                                                                | Do not design scenarios depending on reassignment.                                       |
| Durable workflow-state table                                          | Not landed and should not be assumed; workflow state is projection-derived.                                              | Use projection/read model only.                                                          |
| S06 / entity_lifecycle                                                | Explicitly deferred in Phase 4.                                                                                          | Scenario requiring entity_lifecycle is not currently supported unless new source exists. |
| General trigger engine                                                | Earlier summaries mention triggers; Phase 4 scope excludes general trigger/auto-resolution unless scheduled by addendum. | Verify code before assuming.                                                             |
| Deterministic flag UUID algorithm                                     | Canonical/implementation wording mismatch.                                                                               | Decide whether to amend CDL-015 or migrate code.                                         |
| `events.location_path` historical immutability                        | Needs explicit implementation/documentation boundary.                                                                    | Verify no reparenting rewrites historical events.                                        |
| Admin UI production authority                                         | Development/admin surfaces exist, but production auth binding is not proven.                                             | Run security review before production use.                                               |
| Code still matches docs                                               | Not proven by this pack.                                                                                                 | Inspect code/tests or run scenarios.                                                     |

---

## 9. Runtime verification checklist

Use this checklist before treating the platform as ready for a real deployment workflow.

### Eventing

- [ ] Illegal envelope type values are rejected.
- [ ] Missing required envelope fields are rejected.
- [ ] Duplicate push is idempotent.
- [ ] Existing events cannot be updated/deleted through application paths.
- [ ] Server and contracts envelope schemas are equivalent.

### Sync

- [ ] Pull returns only events after watermark and respects pagination.
- [ ] Pull filters by actor scope across geographic, subject_list, and activity axes.
- [ ] Own assignment events are included even if ordinary scope filters would exclude them.
- [ ] Subject-history backfill does not alter normal sync watermark.
- [ ] Backfill rechecks authorization per page.

### Projection

- [ ] Flagged events visible in timeline but excluded from current state.
- [ ] Resolution accepted/rejected changes state inclusion correctly.
- [ ] Alias projection rebuilds from identity events.
- [ ] Pattern-state projection matches server/mobile shared fixtures.

### Identity

- [ ] Merge emits type=capture + shape_ref=subjects_merged/v1.
- [ ] Split emits type=capture + shape_ref=subject_split/v1.
- [ ] No historical subject_ref rewrite after merge/split.
- [ ] Stale reference detection reads raw ref before aliasing.

### Authorization

- [ ] Assignment creation containment checks all three axes.
- [ ] Null axis means unrestricted only for that axis, not root authority.
- [ ] Assignment ending requires target-assignment authority.
- [ ] Request-body actor IDs cannot grant command authority.
- [ ] Role-action rejects assignment_changed in activities[*].roles.

### Configuration

- [ ] Invalid shape/expression/config is rejected before publish.
- [ ] Device never observes partial config.
- [ ] At most two config versions coexist on device.
- [ ] In-progress form completes under old config.
- [ ] Old shape versions remain valid.

### Workflow and flags

- [ ] Domain uniqueness violation is accepted and flagged.
- [ ] Transition violation is accepted and flagged.
- [ ] No new envelope type/field introduced by workflow.
- [ ] Every new flag has designated_resolver.
- [ ] Non-designated resolver cannot canonically resolve.
- [ ] Severity does not alter resolvability.

### Mobile

- [ ] Offline capture works without network.
- [ ] Device advisory checks do not create canonical authority.
- [ ] Stale config/assignment scenarios reconcile safely.
- [ ] Selective-retain purges only allowed data and keeps own events.

### Admin/operator

- [ ] Admin commands are authenticated and bound to actor context.
- [ ] Admin UI cannot bypass assignment containment or resolver routing.
- [ ] Dev-only admin paths are disabled or protected in production.

---

## 10. Scenario walkthrough protocol

For every selected real-world scenario, use this structure.

### Scenario input extraction

```yaml
scenario:
  name:
  real_world_goal:
  architectural_pre_reading:
  actors:
    - actor_id_or_role:
      responsibility:
      expected_scope:
  subjects:
    - subject_type:
      identity_risk:
      location_or_scope:
  offline_windows:
    - actor:
      duration:
      expected_stale_risks:
  activities:
    - activity_ref:
      shapes:
      pattern_binding:
  expected_events:
    - type:
      shape_ref:
      subject_ref:
      actor_ref:
      activity_ref:
  expected_flags:
    - category:
      reason:
      severity:
      resolvability:
      resolver_route:
```

### Review steps

1. Identify actors, assignments, authority relationships, and access control intersection.
2. Identify subjects, lifecycle transitions, and real-world identity risks.
3. Identify activities and whether `activity_ref` is known or honestly null.
4. Identify shapes needed and whether they fit current shape vocabulary.
5. Identify all events emitted, with legal `type` and `shape_ref`.
6. Identify sync expectations for every actor/device.
7. Identify stale/offline windows and expected flags.
8. Identify resolver route for each flag.
9. Identify current-state and timeline projections.
10. Identify workflow/pattern state expectations.
11. Identify mobile behavior: form, local projection, advisory validation, sync/purge.
12. Identify admin/operator behavior.
13. Classify each step with evidence levels.
14. Produce scenario verdict.

### Scenario verdict rubric

| Verdict | Meaning |
|---|---|
| `runs_with_documented_implementation` | Docs claim all required capabilities landed and tests/gates exist; still needs runtime run for certainty. |
| `runs_with_assumptions` | Likely supported, but one or more behaviors depend on assumptions not directly evidenced. |
| `architecturally_supported_but_implementation_unproven` | Canonical ledger supports it, but implementation evidence is insufficient. |
| `partially_supported` | Some steps are supported; missing/deferred capability blocks full scenario. |
| `not_supported` | Scenario requires a capability explicitly absent/deferred or contradicts canonical constraints. |
| `requires_code_or_runtime_verification` | Documents are too weak or conflicting to answer. |

### Output format for scenario review

```md
# Scenario Fit Review — <scenario name>

## Verdict

## Required capabilities

## Step-by-step walkthrough

| Step | Required behavior | Evidence level | Supported? | Notes |
|---|---|---|---|---|

## Expected events

## Expected flags and resolver routes

## Expected sync/projection behavior

## Missing or weakly evidenced behavior

## Runtime tests to run

## Final risk assessment
```

---

## 11. Suggested source set after this pack

For scenario walkthrough work, keep the project source set small:

```text
docs/architecture/adrs-decisions-canonical-ledger/canonical-decision-ledger.md
001-platform-implementation-evidence-pack.md
selected scenario files
docs/behavioral_patterns.md
docs/access-control-scenario.md
docs/principles.md
```

Optional, only when needed:

```text
001-implementation-decision-alignment-review.md
module-boundary-design.md
```

Do not keep the full phase/IDR set active if the goal is scenario review; it increases surface area and invites source-layer confusion.

---

## 12. Maintenance notes

This file should be updated when:

- a scenario walkthrough proves or disproves a capability;
- code inspection finds docs are stale;
- runtime tests expose a gap;
- production authentication/admin authority is implemented and verified;
- auto-resolution or general trigger execution is implemented;
- entity_lifecycle/S06 support is implemented.

Suggested update discipline:

```text
Do not silently promote `claimed_implemented` to `scenario_verified`.
Only scenario walkthroughs, code inspection, or runtime tests may increase confidence.
```
