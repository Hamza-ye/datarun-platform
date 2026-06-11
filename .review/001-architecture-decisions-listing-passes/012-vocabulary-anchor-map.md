# 012 — Vocabulary Anchor Map

## Context Capsule

* Artifact: `012-vocabulary-anchor-map.md`
* Pass: Pass 3 — Vocabulary Anchor Map
* Status: Draft vocabulary-anchor artifact for project-source inclusion
* Mode: Vocabulary anchoring only; no redesign, no reopening, no new architecture decisions.
* Current authority note:

  * This vocabulary map is downstream of `011`.
  * Current architecture authority remains the Canonical Decision Ledger.
  * Contracts govern crossed wire/process vocabulary, and BAR/NW/IDR evidence remains validation input until folded by the catch-up waves.
  * `002`, `007`, and `008` remain recovery lineage for the original extraction pass.
* Recovery verification anchor:

  * `002-phase0-decision-register.md`
* Recovered architecture-map reference:

  * `008-authoritative-architecture-map.md`
* Consumes previous artifacts:

  * `009-decision-anchor-extraction-charter.md`
  * `010-candidate-architecture-decision-inventory.md`
  * `011-core-architecture-decision-records.md`
* Input sources:

  * `002-phase0-decision-register.md`
  * `007-phase5-cross-lineage-vocabulary.md`
  * `008-authoritative-architecture-map.md`
  * `009-decision-anchor-extraction-charter.md`
  * `010-candidate-architecture-decision-inventory.md`
  * `011-core-architecture-decision-records.md`
* Supporting lineage sources:

  * `003-phase1-adr2-identity-conflict-recovery.md`
  * `004-phase2-adr3-auth-sync-recovery.md`
  * `005-phase3-adr4-config-boundary-recovery.md`
  * `006-phase4-adr5-state-progression-recovery.md`
* Purpose:

  * Map locked architecture vocabulary terms to the normalized decision records from `011`.
  * Identify the primary owning decision for each important term.
  * Identify supporting decisions where a term crosses domains.
  * Identify negative-boundary terms and rejected alternatives.
  * Identify vocabulary classification so later edits do not promote implementation, policy, or product/problem language into architecture.
* Scope:

  * Anchor terms already recovered in `007`, `008`, and `011`.
  * Preserve term-collision rules.
  * Preserve negative-boundary vocabulary.
  * Preserve implementation/tooling exclusions.
  * Provide a stable lookup layer for later vocabulary maintenance and gap routing.
* Non-goals:

  * Do not create new architecture terms.
  * Do not close open fronts.
  * Do not write the gap routing playbook.
  * Do not define platform-spec details.
  * Do not define implementation mechanisms.
  * Do not revise decision records from `011`.
  * Do not override the CDL, contracts, current accepted baseline evidence, `011`, or the original `002`/`007`/`008` recovery lineage.
* Settled outputs:

  * Vocabulary classification legend.
  * Decision-to-vocabulary map.
  * Alphabetical vocabulary anchor map.
  * Term-collision map.
  * Negative-boundary vocabulary map.
  * Implementation/tooling exclusion map.
  * Open-front vocabulary map.
* Rejected / excluded:

  * Treating implementation terms as architecture vocabulary.
  * Treating open fronts as settled vocabulary.
  * Treating rejected alternatives as available options.
  * Treating product/problem scenario terms as architecture unless already mapped to DEC anchors.
* Deferred / open:

  * Gap routing playbook.
  * Coherence audit.
  * Full source-line audit for each vocabulary term.
  * Any vocabulary edits required after coherence audit.
* Terms or decisions locked:

  * No new platform runtime term is introduced by this pass.
  * This document locks vocabulary-to-decision ownership for later work, subject to coherence audit.
* Count confirmation:

  * `011` has been corrected/audited to 36 normalized decision records.
  * This pass maps against the actual decision IDs present in `011`, including `DEC-WORKFLOW-07`.
  * No DEC ID is removed or renumbered by this correction.
* Next-pass handoff:

  * Pass 4 should produce `013-gap-routing-playbook.md`.
  * Pass 4 should consume this vocabulary map to route future gaps through affected vocabulary and owning decisions.

---

## 1. Pass Checkpoint

This pass follows the Pass 3 scope from `009-decision-anchor-extraction-charter.md`.

Required Pass 3 output:

```txt
locked vocabulary term
→ primary decision anchor
→ supporting decision anchors
→ classification
→ negative boundary if relevant
```

Quality gate:

```txt
every locked term maps to at least one decision
every important term has one primary owner decision
term collisions are explicitly handled
negative boundaries are linked to terms where relevant
implementation concerns remain marked as implementation concerns
```

---

## 2. Classification Legend

Use these classifications for vocabulary terms.

| Classification              | Meaning                                                                                                                                                                                  |
| --------------------------- | ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| Structural contract         | Stored-event or protocol-level commitment. Changing it likely requires migration, permanent dual semantics, historical reinterpretation, or deployed-device compatibility handling.      |
| Strategy-protecting service | Runtime rule, validator, or service boundary that protects accepted structural invariants. Internal implementation may evolve, but weakening the invariant requires architecture review. |
| Initial strategy            | Current baseline strategy that can evolve without changing historical events or breaking deployed devices.                                                                               |
| Projection/read model       | Rebuildable derived state from events, configuration, flags, and policy. Not source truth.                                                                                               |
| Configuration artifact      | Deployer-authored or deployer-parameterized artifact within platform-fixed bounds.                                                                                                       |
| Policy surface              | Bounded deployer or operational policy parameter; not arbitrary logic.                                                                                                                   |
| Negative boundary           | Rejected alternative or deliberately excluded path. Not available without formal architecture decision.                                                                                  |
| Implementation concern      | Build mechanism, storage/API/tooling/UI detail. Not architecture unless it changes a settled boundary.                                                                                   |
| Product/problem term        | Scenario/user/problem language. Not architecture unless converted into accepted decision vocabulary.                                                                                     |

### 2.1 Current Vocabulary Catch-Up Overlay

This overlay consumes the accepted-extension inputs now recorded in `011`. It does not replace the alphabetical maps below; it patches the current terms that future routing is most likely to need before the full table is re-normalized.

| Term | Primary decision | Supporting decisions | Classification | Accepted source | Routing note |
|---|---|---|---|---|---|
| activity role-action | DEC-AUTH-01 | DEC-CONFIG-08, DEC-WORKFLOW-04 | Structural contract | IDR-021, IDR-023, NW-041 | Activity work-action vocabulary is `capture`, `review`, `alert`, `task_created`, and `task_completed`. |
| `assignment_changed` exclusion | DEC-AUTH-03 | DEC-EVENT-04, DEC-CONFIG-08 | Negative boundary | IDR-023, NW-041 | `assignment_changed` remains assignment administration and must not be treated as an activity role-action. |
| `assignment_admin.create` | DEC-AUTH-04 | DEC-CONFIG-08 | Strategy-protecting service | IDR-029, NW-050 | Platform-owned assignment-admin command capability outside `activities[*].roles`. |
| `assignment_admin.end` | DEC-AUTH-04 | DEC-CONFIG-08 | Strategy-protecting service | IDR-029, NW-050 | Platform-owned assignment-admin command capability outside `activities[*].roles`. |
| `assignment_admin_capabilities` | DEC-CONFIG-08 | DEC-AUTH-04 | Policy surface | IDR-029, NW-050 | Deployment-configured role-to-command policy; not an envelope field, assignment payload field, or IdP claim. |
| principal binding | DEC-CONFIG-03 | DEC-AUTH-01, DEC-AUTH-03 | Strategy-protecting service | IDR-027, IDR-028, BAR-104 | Explicit `(issuer, subject) -> actor_id` mapping is the only production actor mapping from provider principals. |
| `auth_principal_bindings` | DEC-CONFIG-03 | DEC-AUTH-03 | Implementation concern | IDR-027, IDR-028, BAR-104 | Supporting lookup/projection rows for principal binding; not event or assignment authority. |
| OIDC/JWKS provider validation | DEC-CONFIG-03 | DEC-AUTH-03 | Strategy-protecting service | IDR-027, BAR-104, NW-038 | Validates issuer/audience/JWKS before explicit principal binding lookup. |
| JWT actor_id claim authority | DEC-BOUNDARY-01 | DEC-AUTH-03, DEC-CONFIG-03 | Negative boundary | IDR-027, IDR-028, BAR-104 | JWT `actor_id`, groups, roles, and resource claims are not platform authority. |
| platform payload schema | DEC-CONFIG-01 | DEC-CONFIG-04 | Structural contract | BAR-005, FP-010 | Platform-owned schemas under `contracts/shapes/*.schema.json`; not deployer-authored shape rows. |
| config package schema | DEC-CONFIG-08 | DEC-CONFIG-04 | Structural contract | BAR-010, NW-034 | Server-emitted/mobile-consumed package wire shape with tolerated unknown top-level keys. |
| shape-format schema | DEC-CONFIG-04 | DEC-CONFIG-01 | Structural contract | NW-034 | Deployer-authored form shape DSL schema; not a platform payload schema. |
| `pattern_definitions` | DEC-WORKFLOW-02 | DEC-CONFIG-08 | Configuration artifact | IDR-025, BAR-010 | Referenced platform pattern definitions delivered in atomic config packages. |
| `designated_resolver` | DEC-CONFLICT-03 | DEC-CONFLICT-04 | Strategy-protecting service | IDR-026, FP-009 | Required semantic resolver identity for emitted flags; canonical resolution requires exact resolver equality. |
| `resolver_unassigned` | DEC-CONFLICT-03 | DEC-CONFLICT-04 | Negative boundary | IDR-026 | Explicit sentinel for no-human-route cases; not fallback resolver authority and not reassignment. |
| subject-history backfill | DEC-AUTH-02 | DEC-AUTH-05, DEC-PROJECTION-01 | Strategy-protecting service | BAR-004, FP-005 | Separate authorized repair surface with independent cursor and no normal watermark mutation. |
| shared-device actor session | DEC-AUTH-02 | DEC-AUTH-05, DEC-PROJECTION-01 | Strategy-protecting service | IDR-030, NW-055 | Exactly one active server-resolved actor session on a shared device. |
| actor-local partition | DEC-AUTH-02 | DEC-AUTH-05, DEC-PROJECTION-01 | Strategy-protecting service | IDR-030, NW-055 | Mutable local events, pending push, projections, sync progress, cursors, token/session material, and config state are partitioned by actor. |
| retention/security successor route | DEC-AUTH-05 | DEC-BOUNDARY-01 | Policy surface | IDR-030, NW-054, BAR-106 | Expiry, decommissioning, sealed-partition recovery, encryption, and token/session retention remain separate future work. |
| auto-resolution execution | DEC-WORKFLOW-07 | DEC-CONFLICT-03, DEC-CONFLICT-04 | Policy surface | BAR-102, IDR-026 | Mechanism class is accepted by CDL-053/CDL-054, but runtime execution remains deferred to a successor policy/trigger slice. |

---

## 3. Ownership Rules

### 3.1 Primary owner

Each important vocabulary term should have one primary owning DEC record.

The primary owner is the decision that defines the term’s architecture boundary.

### 3.2 Supporting decisions

Supporting decisions are listed when a term is used by another domain but not owned there.

Example:

```txt
assignment
primary: DEC-IDENTITY-01
supporting: DEC-AUTH-01, DEC-AUTH-03
```

Reason:

* ADR-002 introduces `assignment` as an identity category.
* ADR-003 refines it as the authorization grant primitive.
* The identity decision owns the term origin; authorization decisions own access semantics.

### 3.3 Rejected terms

Rejected terms do not receive a normal owner. They map to:

```txt
primary: DEC-BOUNDARY-01
supporting: decision that rejects or replaces the term
classification: Negative boundary
```

Example:

```txt
authority_context
primary: DEC-BOUNDARY-01
supporting: DEC-EVENT-03, DEC-AUTH-03
classification: Negative boundary
```

### 3.4 Implementation terms

Implementation terms are not owned by DEC records as architecture vocabulary.

They map to:

```txt
primary: none
supporting: affected decision only for guardrail
classification: Implementation concern
route later through gap routing playbook
```

---

# 4. Decision-to-Vocabulary Map

This section provides the reverse lookup:

```txt
decision → vocabulary terms owned or primarily anchored
```

---

## 4.1 `EVENT` decisions

### DEC-EVENT-01 — Append-only event source of truth

Primary vocabulary:

* `append-only`
* `event`
* `event store`
* `correction`
* `write-path discipline`
* `durable facts`

Supporting vocabulary:

* `projection`
* `read model`
* `source of truth`
* `rebuildable`

Negative-boundary vocabulary:

* mutable-in-place records
* last-write-wins
* optional audit trail

---

### DEC-EVENT-02 — Typed immutable event as atomic write and sync unit

Primary vocabulary:

* `typed immutable event`
* `client-generated UUID`
* `sync unit`
* `idempotent sync`
* event `id`

Supporting vocabulary:

* `event`
* offline event creation
* order-independent sync

Negative-boundary vocabulary:

* mutable snapshot sync
* server-assigned-only event identity
* full-state replacement sync as source truth

---

### DEC-EVENT-03 — Final event envelope field contract

Primary vocabulary:

* `event envelope`
* `id`
* `type`
* `shape_ref`
* `activity_ref`
* `subject_ref`
* `actor_ref`
* `device_id`
* `device_seq`
* `sync_watermark`
* `timestamp`
* `payload`

Supporting vocabulary:

* durable interpretation fields
* envelope extensibility
* deliberate non-fields

Negative-boundary vocabulary:

* `authority_context`
* `assignment_ref`
* `assignment_refs`
* `pattern_ref`
* `current_state`
* `transition_validity`
* `resolvability`
* stored `context.*`

---

### DEC-EVENT-04 — Platform-fixed event type vocabulary

Primary vocabulary:

* event `type`
* `capture`
* `review`
* `alert`
* `task_created`
* `task_completed`
* `assignment_changed`

Supporting vocabulary:

* platform processing behavior
* domain meaning
* append-only type vocabulary

Negative-boundary vocabulary:

* deployer-authored event types
* domain event names as structural types
* `status_changed`

---

## 4.2 `IDENTITY` decisions

### DEC-IDENTITY-01 — Typed identity references and identity categories

Primary vocabulary:

* `typed identity reference`
* `{type,id}`
* `subject`
* `actor`
* `process`
* `assignment`
* `subject_ref`
* `actor_ref`

Supporting vocabulary:

* identity category
* identity discriminator

Negative-boundary vocabulary:

* untyped UUID reference
* actor identity as authority
* process identity as activity/pattern/campaign/trigger

---

### DEC-IDENTITY-02 — Causal metadata and hardware-bound device identity

Primary vocabulary:

* `device_id`
* `device_sequence`
* `device_seq`
* `sync_watermark`
* `timestamp`
* `device_time`
* `causal ordering`
* `concurrency detection`
* `hardware-bound device identity`

Supporting vocabulary:

* same-device order
* staleness detection
* knowledge-state marker

Negative-boundary vocabulary:

* device-time structural ordering
* account-bound device identity
* global total order from offline devices

---

### DEC-IDENTITY-03 — Subject lineage by merge aliasing and corrective split

Primary vocabulary:

* `SubjectsMerged`
* `SubjectSplit`
* `alias mapping`
* `retired_id`
* `surviving_id`
* `successor`
* `active`
* `archived`
* `lineage DAG`
* `corrective split`

Supporting vocabulary:

* transitive closure
* online-only merge/split
* source archived
* historical events frozen

Negative-boundary vocabulary:

* physical re-reference after merge
* `SubjectsUnmerged`
* offline merge/split
* lineage cycle

---

### DEC-IDENTITY-04 — Identity resolution order across conflict, projection, and authorization

Primary vocabulary:

* `raw-reference detection`
* `alias-respects-original-scope`
* `original subject_ref`
* `projection-time alias resolution`

Supporting vocabulary:

* `alias mapping`
* conflict-before-projection
* authorization-before-alias distortion

Negative-boundary vocabulary:

* alias resolution before conflict detection
* authorization solely against surviving alias target
* rewriting old references after merge

---

## 4.3 `CONFLICT` decisions

### DEC-CONFLICT-01 — Accept-and-flag instead of stale-state rejection

Primary vocabulary:

* `accept-and-flag`
* `state staleness`
* `flag`
* `stale_reference`
* `transition_violation`
* `scope_violation`

Supporting vocabulary:

* stale authority
* stale reference
* invalid transition
* event preservation under uncertainty

Negative-boundary vocabulary:

* stale offline event rejection
* silent overwrite
* invalid transition as write rejection

---

### DEC-CONFLICT-02 — Detect-before-act

Primary vocabulary:

* `detect-before-act`
* `flagged-event exclusion`
* `downstream policy execution`
* unresolved flagged event

Supporting vocabulary:

* trigger gating
* workflow-state gating
* processing uncertainty boundary

Negative-boundary vocabulary:

* trigger execution before flag detection
* workflow state derivation from unresolved flagged events
* flag detection as after-the-fact reporting only

---

### DEC-CONFLICT-03 — Single-writer conflict resolution

Primary vocabulary:

* `ConflictDetected`
* `ConflictResolved`
* `designated resolver`
* `single-writer resolution`
* `online-only conflict resolution`

Supporting vocabulary:

* canonical resolver
* conflict lifecycle
* resolver authority

Negative-boundary vocabulary:

* multiple co-equal conflict resolutions
* offline resolution of manual-only conflicts
* resolver authority stored in event envelope

---

### DEC-CONFLICT-04 — Flag dimensions and baseline category model

Primary vocabulary:

* `flag`
* flag `category`
* `severity`
* `flag resolvability`
* `auto_eligible`
* `manual_only`
* `source_event_ref`
* `resolver`
* `identity_conflict`
* `concurrent_state_change`
* `stale_reference`
* `scope_violation`
* `ScopeStaleFlag`
* `RoleStaleFlag`
* `TemporalAuthorityExpiredFlag`
* `domain_uniqueness_violation`
* `transition_violation`

Supporting vocabulary:

* source event
* resolver linkage
* flag queue
* category vs severity vs resolvability

Negative-boundary vocabulary:

* collapsing severity and resolvability
* deployer-configured resolvability
* auto-resolution of manual-only flags
* stored downstream flag propagation

---

## 4.4 `AUTH` decisions

### DEC-AUTH-01 — Assignment-based access

Primary vocabulary:

* `assignment-based access`
* `scope-containment test`
* `active assignment`
* `role permits action`
* `assignment` as authorization grant

Supporting vocabulary:

* actor + role + scope + time
* target context
* access_allowed

Negative-boundary vocabulary:

* role-only RBAC
* arbitrary ABAC as deployer-authored access logic
* device-local authorization assertion as verified server fact

---

### DEC-AUTH-02 — Sync scope equals access scope

Primary vocabulary:

* `sync scope = access scope`
* `sync scope`
* `access scope`
* `selective sync`
* `Sync Scope Resolver`

Supporting vocabulary:

* authorized offline data
* access-scoped materialization
* data delivery boundary

Negative-boundary vocabulary:

* sync independent of access
* full dataset on every device
* unscoped offline data delivery

---

### DEC-AUTH-03 — Authority is projection-derived from assignment timeline

Primary vocabulary:

* `authority-as-projection`
* `assignment timeline`

Supporting vocabulary:

* authority reconstruction
* assignment events
* role/action/scope policy

Negative-boundary vocabulary:

* `authority_context`
* `assignment_ref`
* `assignment_refs`
* event-authored authority
* device-authored authority assertion

---

### DEC-AUTH-04 — Scope-containment invariant on assignment creation

Primary vocabulary:

* `scope-containment invariant`
* `privilege escalation prevention`
* `effective_scope`
* `scope_contains`

Supporting vocabulary:

* assignment creation validation
* creator authority
* containment check

Negative-boundary vocabulary:

* lateral privilege escalation
* deployer-authored containment functions
* client-only assignment authority validation

---

### DEC-AUTH-05 — Selective-retain on scope contraction

Primary vocabulary:

* `selective-retain`
* `scope contraction`
* local retention
* access loss

Supporting vocabulary:

* scope expansion
* scope contraction data handling
* local device retention

Negative-boundary vocabulary:

* retain-indefinitely
* retain-but-hide as sufficient for sensitive data
* event rewriting after scope contraction

---

## 4.5 `CONFIG` decisions

### DEC-CONFIG-01 — Mandatory `shape_ref` historical schema contract

Primary vocabulary:

* `shape_ref`
* `shape`
* `shape version`
* `shape registry`

Supporting vocabulary:

* historical schema reference
* payload schema version
* shape validation

Negative-boundary vocabulary:

* self-describing payload replacing `shape_ref`
* events without shape references
* silent reinterpretation under new shape

---

### DEC-CONFIG-02 — Optional `activity_ref` activity-instance contract

Primary vocabulary:

* `activity_ref`
* `activity instance`
* activity scope type

Supporting vocabulary:

* activity definition
* activity context
* cross-activity correlation

Negative-boundary vocabulary:

* mandatory `activity_ref` for all events
* `activity_ref` as authority context
* `activity_ref` as hidden pattern reference

---

### DEC-CONFIG-03 — Auditable system actor identity

Primary vocabulary:

* `system actor`
* `system:{source_type}/{source_id}`
* `system:trigger/{trigger_id}`
* trigger source type
* `auto_resolution` source type

Supporting vocabulary:

* auditable automation
* system-authored event
* system source type

Negative-boundary vocabulary:

* anonymous automation writes
* `system_ref` envelope field
* full trigger configuration stored in events

---

### DEC-CONFIG-04 — Shape model and evolution boundary

Primary vocabulary:

* `shape`
* `shape definition`
* `shape evolution`
* `deprecation-only`
* `breaking change`
* delta-authored
* snapshot-stored

Supporting vocabulary:

* typed payload schema
* additive evolution
* historical read

Negative-boundary vocabulary:

* shape as event type
* shape as workflow engine
* shape as arbitrary code
* silent breaking change

---

### DEC-CONFIG-05 — Four-layer configuration gradient and code boundary

Primary vocabulary:

* `L0 Assembly`
* `L1 Shape`
* `L2 Logic`
* `L3 Policy`
* `four-layer gradient`
* `L3→code boundary`
* configuration has boundaries
* side effect

Supporting vocabulary:

* configuration expressiveness ceiling
* code boundary
* deployer parameterization

Negative-boundary vocabulary:

* configuration as arbitrary code
* deployer-authored event types
* deployer-authored access logic
* deployer-authored state machines
* recursive/unbounded trigger chains

---

### DEC-CONFIG-06 — Bounded expression language

Primary vocabulary:

* `expression language`
* `payload.*`
* `entity.*`
* `event.*`
* `context.*`
* zero functions

Supporting vocabulary:

* field reference
* operator
* expression scope
* form context
* trigger context

Negative-boundary vocabulary:

* expressions as programming language
* dynamic cross-entity queries
* side-effectful expressions
* deployer-defined `context.*`

---

### DEC-CONFIG-07 — Server-only bounded trigger architecture

Primary vocabulary:

* `server-only triggers`
* `event-reaction trigger (3a)`
* `deadline-check trigger (3b)`
* `trigger DAG`
* `max path length 2`
* `Trigger Engine`

Supporting vocabulary:

* trigger definition
* trigger source type
* deadline check
* event reaction

Negative-boundary vocabulary:

* device-side triggers
* recursive triggers
* unbounded trigger chain
* trigger execution before flag detection

---

### DEC-CONFIG-08 — Atomic configuration delivery and bounded policy surface

Primary vocabulary:

* `atomic config delivery`
* `config version`
* `config package`
* `Config Package Validator`
* `complexity budgets`
* `flag severity`
* `domain_uniqueness_violation`
* `scope composition`
* `sensitivity classification`
* `standard`
* `elevated`
* `restricted`
* `geographic`
* `subject_list`
* `activity` scope type

Supporting vocabulary:

* bounded policy surface
* deployer policy parameter
* shape/activity-level sensitivity
* fixed scope type

Negative-boundary vocabulary:

* partial config delivery
* field-level sensitivity
* deployer-defined containment logic
* deployer-defined flag mechanism
* unbounded complexity budgets

---

## 4.6 `WORKFLOW` decisions

### DEC-WORKFLOW-01 — Projection-derived workflow state

Primary vocabulary:

* `projection-derived state machine`
* `Command Validator`
* `workflow state projection`
* `flag_status`
* `current_state` rejected

Supporting vocabulary:

* state derivation
* pattern_definition
* config_version
* event_stream

Negative-boundary vocabulary:

* stored workflow state
* event rejection by state machine
* `status_changed` structural type
* device-only transition authority

---

### DEC-WORKFLOW-02 — Platform-fixed Pattern Registry

Primary vocabulary:

* `Pattern Registry`
* `pattern`
* `state machine skeleton`
* `participant roles`
* `parameterization points`

Supporting vocabulary:

* valid transition table
* projection specification
* platform workflow skeleton
* pattern binding

Negative-boundary vocabulary:

* deployer-authored state machine
* frozen scenario pattern examples as architecture inventory
* `pattern_ref` envelope field

---

### DEC-WORKFLOW-03 — Pattern composition rules

Primary vocabulary:

* `subject-level pattern`
* `event-level pattern`
* `composition rules`
* approval sub-flow
* shape-to-pattern mapping

Supporting vocabulary:

* one subject-level pattern per activity
* event-level pattern composition
* cross-activity linkage

Negative-boundary vocabulary:

* multiple competing subject-level lifecycle patterns in one activity
* hidden cross-activity coupling
* two patterns claiming the same shape in one activity

---

### DEC-WORKFLOW-04 — Transition violations and flagged-event exclusion

Primary vocabulary:

* `transition_violation`
* `flagged-event exclusion`
* timeline visibility
* state derivation

Supporting vocabulary:

* invalid workflow transition
* unresolved flagged event
* resolution effects

Negative-boundary vocabulary:

* transition violation as write rejection
* unresolved flagged event as state truth
* hiding flagged event from timeline

---

### DEC-WORKFLOW-05 — Closed pre-resolved `context.*` expression scope

Primary vocabulary:

* `context.*`
* `context.subject_state`
* `context.subject_pattern`
* `context.activity_stage`
* `context.actor.role`
* `context.actor.scope_name`
* `context.days_since_last_event`
* `context.event_count`

Supporting vocabulary:

* pre-resolved context
* read-only context
* local subject projection

Negative-boundary vocabulary:

* deployer-defined `context.*`
* dynamic query behavior
* stored context values as event facts

---

### DEC-WORKFLOW-06 — Source-only flagging and source-chain traversal

Primary vocabulary:

* `source-only flagging`
* `source-chain traversal`
* `source_event_ref`
* downstream warning

Supporting vocabulary:

* root-cause flag
* computed warning
* upstream flag state

Negative-boundary vocabulary:

* stored downstream flag propagation
* duplicated flags onto every downstream event
* warning projection as source truth

---

### DEC-WORKFLOW-07 — Bounded auto-resolution for eligible flags

Primary vocabulary:

* `auto-resolution`
* `auto_eligible`
* `manual_only`
* `system:auto_resolution/{policy_id}`
* `auto_resolution` source type

Supporting vocabulary:

* L3b auto-resolution
* loop prevention
* server-side policy

Negative-boundary vocabulary:

* auto-resolution of manual-only flags
* auto-resolution as unbounded rule engine
* device-side auto-resolution
* recursive resolution loops

---

## 4.7 `PROJECTION` decisions

### DEC-PROJECTION-01 — Projections are derived read models, not source truth

Primary vocabulary:

* `projection`
* `read model`
* `rebuildable`
* derived meaning
* current state

Supporting vocabulary:

* subject projection
* alias table projection
* assignment timeline projection
* workflow state projection
* sync scope projection
* flag queue projection
* source chain projection

Negative-boundary vocabulary:

* projection as source truth
* hidden mutable state outside event store
* stored current state as authoritative record

---

### DEC-PROJECTION-02 — Reporting and analytics projections are read-side summaries

Primary vocabulary:

* `reporting/analytics projections`
* read-side summaries
* aggregate oversight
* flag-aware reporting
* freshness

Supporting vocabulary:

* reporting projection
* analytics projection
* access-scoped reporting
* unresolved flag treatment

Negative-boundary vocabulary:

* report summary as source truth
* aggregate access bypassing access rules
* hiding unresolved issues inside aggregates without specification

---

## 4.8 `BOUNDARY` decisions

### DEC-BOUNDARY-01 — Negative boundary register remains active

Primary vocabulary:

* negative boundary
* rejected alternative
* formal architecture decision
* baseline revision

Supporting vocabulary:

* architecture escalation
* boundary preservation
* rejected path

Negative-boundary vocabulary:

* all rejected alternatives in Section 7 of this document

---

### DEC-BOUNDARY-02 — S00 simplicity baseline

Primary vocabulary:

* `S00 simplicity baseline`
* `capture_only`
* simplest scenario
* basic structured capture

Supporting vocabulary:

* minimum config
* S00 guardrail
* simple setup path

Negative-boundary vocabulary:

* custom event type for basic capture
* custom access code for basic capture
* custom trigger for basic capture
* deployer-authored state machine for basic capture
* workflow flag propagation for basic capture

---

# 5. Alphabetical Vocabulary Anchor Map

This section provides the main lookup table:

```txt
term → primary decision → supporting decisions → classification → note
```

---

## 5.1 A–C

| Term                            | Primary decision  | Supporting decisions               | Classification              | Note                                                                             |
| ------------------------------- | ----------------- | ---------------------------------- | --------------------------- | -------------------------------------------------------------------------------- |
| `access loss`                   | DEC-AUTH-05       | DEC-AUTH-02                        | Initial strategy            | Local data handling after actor loses access.                                    |
| `access scope`                  | DEC-AUTH-02       | DEC-AUTH-01                        | Structural contract         | Sync materializes access scope.                                                  |
| `active`                        | DEC-IDENTITY-03   | DEC-IDENTITY-01                    | Structural contract         | Subject lineage lifecycle state.                                                 |
| `active assignment`             | DEC-AUTH-01       | DEC-IDENTITY-01                    | Structural contract         | Assignment grant currently effective.                                            |
| `activity` scope type           | DEC-CONFIG-08     | DEC-CONFIG-02, DEC-AUTH-01         | Strategy-protecting service | Platform-fixed scope type. Disambiguate from activity_ref and activity instance. |
| `activity definition`           | DEC-CONFIG-02     | DEC-CONFIG-05                      | Configuration artifact      | L0 artifact; not event field.                                                    |
| `activity instance`             | DEC-CONFIG-02     | DEC-WORKFLOW-03                    | Structural contract         | Event field points to activity instance through activity_ref.                    |
| `activity_ref`                  | DEC-CONFIG-02     | DEC-EVENT-03, DEC-WORKFLOW-03      | Structural contract         | Optional event field; activity instance correlation.                             |
| `alert`                         | DEC-EVENT-04      | DEC-CONFIG-03                      | Structural contract         | Platform-fixed event type.                                                       |
| `alias mapping`                 | DEC-IDENTITY-03   | DEC-IDENTITY-04, DEC-PROJECTION-01 | Structural contract         | Merge projection mapping from retired to surviving subject.                      |
| `alias-respects-original-scope` | DEC-IDENTITY-04   | DEC-AUTH-01, DEC-AUTH-02           | Structural contract         | Authorization evaluates original subject_ref when aliasing exists.               |
| `append-only`                   | DEC-EVENT-01      | DEC-EVENT-02                       | Structural contract         | All writes append new events.                                                    |
| `archived`                      | DEC-IDENTITY-03   | DEC-IDENTITY-01                    | Structural contract         | Terminal subject lifecycle state after split.                                    |
| `assignment`                    | DEC-IDENTITY-01   | DEC-AUTH-01, DEC-AUTH-03           | Structural contract         | Identity category refined as authorization grant.                                |
| `assignment timeline`           | DEC-AUTH-03       | DEC-PROJECTION-01                  | Projection/read model       | Authority source; not envelope field.                                            |
| `assignment_changed`            | DEC-EVENT-04      | DEC-AUTH-03                        | Structural contract         | Platform-fixed event type for assignment/scope changes.                          |
| `atomic config delivery`        | DEC-CONFIG-08     | DEC-CONFIG-05                      | Strategy-protecting service | Devices receive coherent config package.                                         |
| `actor`                         | DEC-IDENTITY-01   | DEC-AUTH-01, DEC-CONFIG-03         | Structural contract         | Actor identity is not authority.                                                 |
| `actor_ref`                     | DEC-EVENT-03      | DEC-IDENTITY-01, DEC-CONFIG-03     | Structural contract         | Event envelope field for human or system actor.                                  |
| `authority-as-projection`       | DEC-AUTH-03       | DEC-PROJECTION-01                  | Structural contract         | Authority reconstructed, not event-authored.                                     |
| `authority_context`             | DEC-BOUNDARY-01   | DEC-EVENT-03, DEC-AUTH-03          | Negative boundary           | Rejected event-envelope field.                                                   |
| `auto_eligible`                 | DEC-CONFLICT-04   | DEC-WORKFLOW-07                    | Strategy-protecting service | Platform-level flag resolvability class.                                         |
| `auto_resolution` source type   | DEC-CONFIG-03     | DEC-WORKFLOW-07                    | Strategy-protecting service | System actor source type.                                                        |
| `auto-resolution`               | DEC-WORKFLOW-07   | DEC-CONFLICT-04, DEC-CONFIG-03     | Strategy-protecting service | Bounded L3b server-side eligible-flag resolution.                                |
| `basic structured capture`      | DEC-BOUNDARY-02   | DEC-EVENT-04, DEC-CONFIG-01        | Product/problem term        | Protected by S00 simplicity baseline.                                            |
| `breaking change`               | DEC-CONFIG-04     | DEC-CONFIG-01                      | Initial strategy            | Exceptional shape evolution path.                                                |
| `capture`                       | DEC-EVENT-04      | DEC-BOUNDARY-02                    | Structural contract         | Platform-fixed event type.                                                       |
| `capture_only`                  | DEC-BOUNDARY-02   | DEC-WORKFLOW-02                    | Configuration artifact      | Minimal pattern path for S00.                                                    |
| `category`                      | DEC-CONFLICT-04   | DEC-CONFLICT-01                    | Strategy-protecting service | Flag dimension.                                                                  |
| `causal ordering`               | DEC-IDENTITY-02   | DEC-CONFLICT-04                    | Structural contract         | Based on device_id, device_seq, sync_watermark.                                  |
| `client-generated UUID`         | DEC-EVENT-02      | DEC-IDENTITY-01                    | Structural contract         | Event ID and identity creation baseline.                                         |
| `Command Validator`             | DEC-WORKFLOW-01   | DEC-CONFLICT-02                    | Initial strategy            | Advisory validator; server creates flags.                                        |
| `complexity budgets`            | DEC-CONFIG-08     | DEC-CONFIG-05                      | Initial strategy            | Config validation guardrails.                                                    |
| `composition rules`             | DEC-WORKFLOW-03   | DEC-WORKFLOW-02                    | Strategy-protecting service | Pattern composition constraints.                                                 |
| `concurrency detection`         | DEC-IDENTITY-02   | DEC-CONFLICT-04                    | Structural contract         | Causal comparison input.                                                         |
| `concurrent_state_change`       | DEC-CONFLICT-04   | DEC-IDENTITY-02                    | Strategy-protecting service | Baseline conflict category.                                                      |
| `config package`                | DEC-CONFIG-08     | DEC-CONFIG-05                      | Configuration artifact      | Atomic bundle delivered to devices.                                              |
| `config version`                | DEC-CONFIG-08     | DEC-WORKFLOW-01                    | Strategy-protecting service | Versioned config interpretation.                                                 |
| `Config Package Validator`      | DEC-CONFIG-08     | DEC-CONFIG-05                      | Strategy-protecting service | Validates package before delivery.                                               |
| `configuration has boundaries`  | DEC-CONFIG-05     | DEC-BOUNDARY-02                    | Strategy-protecting service | Core config/code boundary.                                                       |
| `context.*`                     | DEC-WORKFLOW-05   | DEC-CONFIG-06                      | Initial strategy            | Closed, read-only, pre-resolved expression scope.                                |
| `context.activity_stage`        | DEC-WORKFLOW-05   | DEC-CONFIG-06                      | Initial strategy            | Pre-resolved context value.                                                      |
| `context.actor.role`            | DEC-WORKFLOW-05   | DEC-AUTH-01, DEC-CONFIG-06         | Initial strategy            | Pre-resolved context value from assignment.                                      |
| `context.actor.scope_name`      | DEC-WORKFLOW-05   | DEC-AUTH-01, DEC-CONFIG-06         | Initial strategy            | Pre-resolved context value from assignment/scope.                                |
| `context.days_since_last_event` | DEC-WORKFLOW-05   | DEC-PROJECTION-01                  | Initial strategy            | Pre-resolved context value from local projection.                                |
| `context.event_count`           | DEC-WORKFLOW-05   | DEC-PROJECTION-01                  | Initial strategy            | Pre-resolved context value from local projection.                                |
| `context.subject_pattern`       | DEC-WORKFLOW-05   | DEC-WORKFLOW-02                    | Initial strategy            | Pre-resolved context value.                                                      |
| `context.subject_state`         | DEC-WORKFLOW-05   | DEC-WORKFLOW-01                    | Initial strategy            | Pre-resolved context value.                                                      |
| `correction`                    | DEC-EVENT-01      | DEC-CONFLICT-03                    | Structural contract         | New event referencing/superseding prior event.                                   |
| `corrective split`              | DEC-IDENTITY-03   | DEC-CONFLICT-03                    | Structural contract         | Wrong merge correction through SubjectSplit.                                     |
| `current state`                 | DEC-PROJECTION-01 | DEC-WORKFLOW-01                    | Projection/read model       | Derived; not stored source truth.                                                |
| `current_state`                 | DEC-BOUNDARY-01   | DEC-EVENT-03, DEC-WORKFLOW-01      | Negative boundary           | Rejected stored event/envelope state.                                            |

---

## 5.2 D–I

| Term                             | Primary decision  | Supporting decisions                            | Classification              | Note                                                          |
| -------------------------------- | ----------------- | ----------------------------------------------- | --------------------------- | ------------------------------------------------------------- |
| `deadline-check trigger (3b)`    | DEC-CONFIG-07     | DEC-CONFIG-08                                   | Strategy-protecting service | Server-side scheduled/asynchronous trigger type.              |
| `deprecation-only`               | DEC-CONFIG-04     | DEC-CONFIG-01                                   | Initial strategy            | Default shape evolution path.                                 |
| `derived meaning`                | DEC-PROJECTION-01 | DEC-EVENT-01                                    | Projection/read model       | Projections derive meaning from events/config.                |
| `designated resolver`            | DEC-CONFLICT-03   | DEC-AUTH-03                                     | Strategy-protecting service | Exactly one resolver per conflict instance.                   |
| `detect-before-act`              | DEC-CONFLICT-02   | DEC-CONFLICT-01, DEC-CONFIG-07, DEC-WORKFLOW-04 | Strategy-protecting service | Detection precedes downstream execution and state derivation. |
| `device_id`                      | DEC-IDENTITY-02   | DEC-EVENT-03                                    | Structural contract         | Hardware-bound causal namespace.                              |
| `device_seq`                     | DEC-IDENTITY-02   | DEC-EVENT-03                                    | Structural contract         | Event envelope name for durable sequence.                     |
| `device_sequence`                | DEC-IDENTITY-02   | DEC-EVENT-03                                    | Structural contract         | ADR term for durable per-device sequence.                     |
| `device_time`                    | DEC-IDENTITY-02   | DEC-EVENT-03                                    | Structural contract         | Advisory only; not structural ordering.                       |
| `domain uniqueness`              | DEC-CONFIG-08     | DEC-CONFLICT-04                                 | Policy surface              | Deployer parameterized policy; produces flags.                |
| `domain_uniqueness_violation`    | DEC-CONFLICT-04   | DEC-CONFIG-08                                   | Strategy-protecting service | Baseline flag category from configured policy.                |
| `downstream policy execution`    | DEC-CONFLICT-02   | DEC-CONFIG-07                                   | Strategy-protecting service | Blocked/gated by detect-before-act.                           |
| downstream warning               | DEC-WORKFLOW-06   | DEC-CONFLICT-04                                 | Projection/read model       | Computed warning from source-chain traversal.                 |
| durable facts                    | DEC-EVENT-01      | DEC-EVENT-02                                    | Structural contract         | Event source-of-truth closure.                                |
| event `id`                       | DEC-EVENT-02      | DEC-EVENT-03                                    | Structural contract         | Client-generated UUID.                                        |
| event `type`                     | DEC-EVENT-04      | DEC-EVENT-03                                    | Structural contract         | Platform-fixed processing vocabulary.                         |
| `elevated`                       | DEC-CONFIG-08     | DEC-AUTH-05                                     | Policy surface              | Shape/activity-level sensitivity class.                       |
| `entity.*`                       | DEC-CONFIG-06     | DEC-PROJECTION-01                               | Initial strategy            | Expression scope over projected entity/subject values.        |
| `event`                          | DEC-EVENT-01      | DEC-EVENT-02, DEC-PROJECTION-01                 | Structural contract         | Durable immutable fact.                                       |
| `event store`                    | DEC-EVENT-01      | DEC-PROJECTION-01                               | Structural contract         | Append-only source store.                                     |
| `event envelope`                 | DEC-EVENT-03      | DEC-EVENT-02                                    | Structural contract         | Durable event interpretation fields.                          |
| `event-reaction trigger (3a)`    | DEC-CONFIG-07     | DEC-CONFLICT-02                                 | Strategy-protecting service | Server-side reaction during sync/ingestion.                   |
| `event.*`                        | DEC-CONFIG-06     | DEC-EVENT-03                                    | Initial strategy            | Expression scope over current event metadata.                 |
| `event-level pattern`            | DEC-WORKFLOW-03   | DEC-WORKFLOW-02                                 | Strategy-protecting service | Pattern over source event/sub-flow.                           |
| `expression language`            | DEC-CONFIG-06     | DEC-CONFIG-05                                   | Initial strategy            | Bounded expression system; not programming language.          |
| `flag`                           | DEC-CONFLICT-04   | DEC-CONFLICT-01                                 | Strategy-protecting service | Event-linked anomaly with dimensions.                         |
| flag `category`                  | DEC-CONFLICT-04   | DEC-CONFLICT-01                                 | Strategy-protecting service | Independent flag dimension.                                   |
| `flag resolvability`             | DEC-CONFLICT-04   | DEC-WORKFLOW-07                                 | Strategy-protecting service | Platform-level auto_eligible/manual_only classification.      |
| `flag_status`                    | DEC-WORKFLOW-01   | DEC-CONFLICT-04                                 | Projection/read model       | Input into workflow state projection.                         |
| `flagged-event exclusion`        | DEC-WORKFLOW-04   | DEC-CONFLICT-02                                 | Strategy-protecting service | Excludes unresolved flagged events from state derivation.     |
| flag-aware reporting             | DEC-PROJECTION-02 | DEC-CONFLICT-04                                 | Projection/read model       | Reports must account for unresolved flags.                    |
| `four-layer gradient`            | DEC-CONFIG-05     | DEC-CONFIG-08                                   | Strategy-protecting service | L0–L3 configuration boundary.                                 |
| `freshness`                      | DEC-PROJECTION-02 | DEC-IDENTITY-02                                 | Projection/read model       | Reporting/data-age semantics; detailed spec open.             |
| `geographic`                     | DEC-CONFIG-08     | DEC-AUTH-01                                     | Strategy-protecting service | Platform-fixed scope type.                                    |
| `hardware-bound device identity` | DEC-IDENTITY-02   | DEC-EVENT-03                                    | Structural contract         | Device identity not account-bound.                            |
| `idempotent sync`                | DEC-EVENT-02      | DEC-AUTH-02                                     | Structural contract         | Event sync idempotency.                                       |
| `identity_conflict`              | DEC-CONFLICT-04   | DEC-IDENTITY-03                                 | Strategy-protecting service | Duplicate identity conflict category.                         |

---

## 5.3 L–P

| Term                               | Primary decision  | Supporting decisions                       | Classification              | Note                                                                 |
| ---------------------------------- | ----------------- | ------------------------------------------ | --------------------------- | -------------------------------------------------------------------- |
| `L0 Assembly`                      | DEC-CONFIG-05     | DEC-WORKFLOW-02                            | Configuration artifact      | Activity and pattern assembly layer.                                 |
| `L1 Shape`                         | DEC-CONFIG-05     | DEC-CONFIG-04                              | Configuration artifact      | Shape definition layer.                                              |
| `L2 Logic`                         | DEC-CONFIG-05     | DEC-CONFIG-06                              | Configuration artifact      | Bounded logic layer.                                                 |
| `L3 Policy`                        | DEC-CONFIG-05     | DEC-CONFIG-07, DEC-WORKFLOW-07             | Policy surface              | Bounded server policy layer.                                         |
| `L3→code boundary`                 | DEC-CONFIG-05     | DEC-BOUNDARY-01                            | Strategy-protecting service | Escalation boundary into platform evolution.                         |
| lineage DAG                        | DEC-IDENTITY-03   | DEC-PROJECTION-01                          | Structural contract         | Acyclic subject lineage graph.                                       |
| local retention                    | DEC-AUTH-05       | DEC-AUTH-02                                | Initial strategy            | Local data handling after scope contraction.                         |
| `manual_only`                      | DEC-CONFLICT-04   | DEC-WORKFLOW-07                            | Strategy-protecting service | Human judgment required; no auto-resolution.                         |
| `max path length 2`                | DEC-CONFIG-07     | DEC-CONFLICT-02                            | Strategy-protecting service | Trigger DAG bound.                                                   |
| negative boundary                  | DEC-BOUNDARY-01   | all affected DEC records                   | Negative boundary           | Rejected path or deliberate exclusion.                               |
| `original subject_ref`             | DEC-IDENTITY-04   | DEC-AUTH-03                                | Structural contract         | Authorization and raw-reference anchor.                              |
| `pattern`                          | DEC-WORKFLOW-02   | DEC-WORKFLOW-03                            | Strategy-protecting service | Platform-fixed workflow skeleton.                                    |
| `pattern_ref`                      | DEC-BOUNDARY-01   | DEC-EVENT-03, DEC-WORKFLOW-02              | Negative boundary           | Rejected event-envelope field.                                       |
| `Pattern Registry`                 | DEC-WORKFLOW-02   | DEC-CONFIG-05                              | Strategy-protecting service | Platform-fixed skeleton registry.                                    |
| `payload`                          | DEC-EVENT-03      | DEC-CONFIG-01, DEC-CONFIG-04               | Structural contract         | Shape-validated event data.                                          |
| `payload.*`                        | DEC-CONFIG-06     | DEC-EVENT-03                               | Initial strategy            | Expression scope over event payload fields.                          |
| `privilege escalation prevention`  | DEC-AUTH-04       | DEC-AUTH-01                                | Strategy-protecting service | Assignment creation invariant.                                       |
| `process`                          | DEC-IDENTITY-01   | DEC-WORKFLOW-03                            | Structural contract         | Operational process identity; not activity/pattern/campaign/trigger. |
| `projection`                       | DEC-PROJECTION-01 | DEC-EVENT-01, DEC-AUTH-03, DEC-WORKFLOW-01 | Projection/read model       | Derived/rebuildable read model.                                      |
| `projection-derived state machine` | DEC-WORKFLOW-01   | DEC-PROJECTION-01                          | Strategy-protecting service | Workflow state derived from events/config/flags.                     |
| `projection-time alias resolution` | DEC-IDENTITY-04   | DEC-PROJECTION-01                          | Structural contract         | Alias application after raw-reference detection.                     |

---

## 5.4 R–S

| Term                                 | Primary decision  | Supporting decisions             | Classification              | Note                                                             |
| ------------------------------------ | ----------------- | -------------------------------- | --------------------------- | ---------------------------------------------------------------- |
| raw-reference detection              | DEC-IDENTITY-04   | DEC-CONFLICT-02                  | Structural contract         | Conflict detection uses original references.                     |
| read model                           | DEC-PROJECTION-01 | DEC-PROJECTION-02                | Projection/read model       | Derived, rebuildable, not source truth.                          |
| read-side summaries                  | DEC-PROJECTION-02 | DEC-PROJECTION-01                | Projection/read model       | Reporting/analytics output.                                      |
| rebuildable                          | DEC-PROJECTION-01 | DEC-EVENT-01                     | Projection/read model       | Projections can be recomputed from events/config.                |
| rejected alternative                 | DEC-BOUNDARY-01   | all affected DEC records         | Negative boundary           | Boundary evidence only.                                          |
| `restricted`                         | DEC-CONFIG-08     | DEC-AUTH-05                      | Policy surface              | Shape/activity-level sensitivity class.                          |
| resolver                             | DEC-CONFLICT-04   | DEC-CONFLICT-03                  | Strategy-protecting service | Flag dimension.                                                  |
| `retired_id`                         | DEC-IDENTITY-03   | DEC-PROJECTION-01                | Structural contract         | Subject ID retired by merge.                                     |
| `review`                             | DEC-EVENT-04      | DEC-WORKFLOW-03                  | Structural contract         | Platform-fixed event type.                                       |
| `RoleStaleFlag`                      | DEC-CONFLICT-04   | DEC-AUTH-01                      | Strategy-protecting service | Authorization flag category.                                     |
| role permits action                  | DEC-AUTH-01       | DEC-CONFIG-08                    | Structural contract         | Required access rule component; activity role-action model accepted by IDR-021/IDR-023. |
| `S00 simplicity baseline`            | DEC-BOUNDARY-02   | DEC-CONFIG-05, DEC-EVENT-04      | Strategy-protecting service | Keeps basic capture simple.                                      |
| `scope composition`                  | DEC-CONFIG-08     | DEC-AUTH-01                      | Policy surface              | Bounded policy parameter, platform-evaluated.                    |
| `scope contraction`                  | DEC-AUTH-05       | DEC-AUTH-02                      | Initial strategy            | Access loss and local data handling path.                        |
| `scope_contains`                     | DEC-AUTH-04       | DEC-AUTH-01                      | Strategy-protecting service | Containment validation function/interface.                       |
| `scope-containment invariant`        | DEC-AUTH-04       | DEC-AUTH-01                      | Strategy-protecting service | New assignment scope must fit creator effective scope.           |
| `scope-containment test`             | DEC-AUTH-01       | DEC-AUTH-04                      | Structural contract         | Core access check.                                               |
| `scope_violation`                    | DEC-CONFLICT-04   | DEC-AUTH-01                      | Strategy-protecting service | Authorization flag/category.                                     |
| `ScopeStaleFlag`                     | DEC-CONFLICT-04   | DEC-AUTH-05                      | Strategy-protecting service | Authorization stale-scope flag.                                  |
| `selective sync`                     | DEC-AUTH-02       | DEC-EVENT-02                     | Structural contract         | Access-scoped data delivery.                                     |
| `selective-retain`                   | DEC-AUTH-05       | DEC-AUTH-02                      | Initial strategy            | Scope-contraction handling baseline.                             |
| `sensitivity classification`         | DEC-CONFIG-08     | DEC-AUTH-05                      | Policy surface              | Shape/activity-level only.                                       |
| `server-only triggers`               | DEC-CONFIG-07     | DEC-CONFLICT-02                  | Strategy-protecting service | Trigger execution location.                                      |
| `shape`                              | DEC-CONFIG-01     | DEC-CONFIG-04                    | Structural contract         | Typed payload schema referenced by shape_ref.                    |
| `shape definition`                   | DEC-CONFIG-04     | DEC-CONFIG-01                    | Configuration artifact      | Versioned payload schema definition.                             |
| `shape evolution`                    | DEC-CONFIG-04     | DEC-CONFIG-01                    | Initial strategy            | Additive/deprecation default.                                    |
| `shape registry`                     | DEC-CONFIG-01     | DEC-CONFIG-04                    | Structural contract         | Registry for shape versions.                                     |
| `shape version`                      | DEC-CONFIG-01     | DEC-CONFIG-04                    | Structural contract         | Historical schema version.                                       |
| `shape_ref`                          | DEC-CONFIG-01     | DEC-EVENT-03, DEC-WORKFLOW-02    | Structural contract         | Mandatory event field.                                           |
| shape-to-pattern mapping             | DEC-WORKFLOW-03   | DEC-CONFIG-02, DEC-CONFIG-01     | Strategy-protecting service | Unique within activity.                                          |
| simplest scenario                    | DEC-BOUNDARY-02   | DEC-CONFIG-05                    | Product/problem term        | Protected by S00 guardrail.                                      |
| `single-writer resolution`           | DEC-CONFLICT-03   | DEC-AUTH-03                      | Strategy-protecting service | One canonical resolver path.                                     |
| `source_event_ref`                   | DEC-CONFLICT-04   | DEC-WORKFLOW-06                  | Strategy-protecting service | Source-event link used by flags/source-chain.                    |
| `source-chain traversal`             | DEC-WORKFLOW-06   | DEC-PROJECTION-01                | Initial strategy            | Projection walks source references.                              |
| `source-only flagging`               | DEC-WORKFLOW-06   | DEC-CONFLICT-04                  | Initial strategy            | Only root-cause source event is flagged.                         |
| `standard`                           | DEC-CONFIG-08     | DEC-AUTH-05                      | Policy surface              | Baseline sensitivity class.                                      |
| `state staleness`                    | DEC-CONFLICT-01   | DEC-IDENTITY-02                  | Structural contract         | Stale state leads to flag, not rejection.                        |
| state derivation                     | DEC-WORKFLOW-01   | DEC-WORKFLOW-04                  | Projection/read model       | Workflow state from event stream/config/flags.                   |
| state machine skeleton               | DEC-WORKFLOW-02   | DEC-WORKFLOW-01                  | Strategy-protecting service | Pattern Registry content.                                        |
| `status_changed`                     | DEC-BOUNDARY-01   | DEC-EVENT-04, DEC-WORKFLOW-01    | Negative boundary           | Rejected structural event type.                                  |
| `subject`                            | DEC-IDENTITY-01   | DEC-IDENTITY-03                  | Structural contract         | Real-world thing event is about.                                 |
| `subject_list`                       | DEC-CONFIG-08     | DEC-AUTH-01                      | Strategy-protecting service | Platform-fixed scope type.                                       |
| `subject_ref`                        | DEC-EVENT-03      | DEC-IDENTITY-01, DEC-IDENTITY-04 | Structural contract         | Event envelope field to subject identity.                        |
| `subject-level pattern`              | DEC-WORKFLOW-03   | DEC-WORKFLOW-02                  | Strategy-protecting service | One per subject/activity.                                        |
| `SubjectsMerged`                     | DEC-IDENTITY-03   | DEC-PROJECTION-01                | Structural contract         | Merge event mapping retired to surviving subject.                |
| `SubjectSplit`                       | DEC-IDENTITY-03   | DEC-CONFLICT-03                  | Structural contract         | Corrective split event.                                          |
| `successor`                          | DEC-IDENTITY-03   | DEC-IDENTITY-01                  | Structural contract         | New subject ID after split.                                      |
| `surviving_id`                       | DEC-IDENTITY-03   | DEC-PROJECTION-01                | Structural contract         | Active subject ID after merge.                                   |
| `sync scope`                         | DEC-AUTH-02       | DEC-AUTH-01                      | Structural contract         | Data actor/device receives.                                      |
| `sync scope = access scope`          | DEC-AUTH-02       | DEC-AUTH-01                      | Structural contract         | Core auth/sync coupling.                                         |
| `sync unit`                          | DEC-EVENT-02      | DEC-AUTH-02                      | Structural contract         | Immutable event.                                                 |
| `sync_watermark`                     | DEC-IDENTITY-02   | DEC-EVENT-03, DEC-CONFLICT-04    | Structural contract         | Last known server position at event creation.                    |
| `Sync Scope Resolver`                | DEC-AUTH-02       | DEC-PROJECTION-01                | Strategy-protecting service | Computes authorized data delivery.                               |
| `system actor`                       | DEC-CONFIG-03     | DEC-EVENT-03                     | Strategy-protecting service | Auditable system-authored event identity.                        |
| `system:auto_resolution/{policy_id}` | DEC-WORKFLOW-07   | DEC-CONFIG-03                    | Strategy-protecting service | Auto-resolution system actor ref.                                |
| `system:trigger/{trigger_id}`        | DEC-CONFIG-03     | DEC-CONFIG-07                    | Strategy-protecting service | Trigger system actor ref.                                        |
| `system:{source_type}/{source_id}`   | DEC-CONFIG-03     | DEC-EVENT-03                     | Strategy-protecting service | System actor format.                                             |

---

## 5.5 T–Z

| Term                           | Primary decision | Supporting decisions           | Classification              | Note                                      |
| ------------------------------ | ---------------- | ------------------------------ | --------------------------- | ----------------------------------------- |
| `task_completed`               | DEC-EVENT-04     | DEC-WORKFLOW-01                | Structural contract         | Platform-fixed event type.                |
| `task_created`                 | DEC-EVENT-04     | DEC-CONFIG-07, DEC-WORKFLOW-01 | Structural contract         | Platform-fixed event type.                |
| `TemporalAuthorityExpiredFlag` | DEC-CONFLICT-04  | DEC-AUTH-01                    | Strategy-protecting service | Authorization flag category.              |
| `timestamp`                    | DEC-IDENTITY-02  | DEC-EVENT-03                   | Structural contract         | Advisory device time / human-facing time. |
| timeline visibility            | DEC-WORKFLOW-04  | DEC-CONFLICT-01                | Strategy-protecting service | Flagged event remains visible.            |
| `transition_validity`          | DEC-BOUNDARY-01  | DEC-EVENT-03, DEC-WORKFLOW-04  | Negative boundary           | Computed, not envelope field.             |
| `transition_violation`         | DEC-WORKFLOW-04  | DEC-CONFLICT-04                | Strategy-protecting service | Workflow flag category.                   |
| `trigger DAG`                  | DEC-CONFIG-07    | DEC-CONFIG-08                  | Strategy-protecting service | Non-recursive bounded trigger graph.      |
| `Trigger Engine`               | DEC-CONFIG-07    | DEC-CONFLICT-02                | Strategy-protecting service | Server-side runtime service.              |
| trigger source type            | DEC-CONFIG-03    | DEC-CONFIG-07                  | Strategy-protecting service | System actor source type.                 |
| `typed identity reference`     | DEC-IDENTITY-01  | DEC-EVENT-03                   | Structural contract         | `{type,id}` identity pointer.             |
| `typed immutable event`        | DEC-EVENT-02     | DEC-EVENT-01                   | Structural contract         | Atomic write and sync unit.               |
| `{type,id}`                    | DEC-IDENTITY-01  | DEC-EVENT-03                   | Structural contract         | Shared identity reference shape.          |
| unresolved flagged event       | DEC-CONFLICT-02  | DEC-WORKFLOW-04                | Strategy-protecting service | Gated from downstream action/state.       |
| write-path discipline          | DEC-EVENT-01     | DEC-PROJECTION-01              | Structural contract         | Every state change enters event store.    |
| zero functions                 | DEC-CONFIG-06    | DEC-CONFIG-05                  | Initial strategy            | Expression language bound.                |

---

# 6. Term Collision Map

This section preserves known ambiguous terms and the required disambiguation.

---

## 6.1 `assignment`

| Usage                            | Meaning                                      | Primary decision |
| -------------------------------- | -------------------------------------------- | ---------------- |
| `assignment` identity category   | One of four typed identity categories.       | DEC-IDENTITY-01  |
| `assignment` authorization grant | Actor + role + scope + time access grant.    | DEC-AUTH-01      |
| `assignment timeline`            | Projection reconstructing authority history. | DEC-AUTH-03      |
| `assignment_changed`             | Event type for authority/scope changes.      | DEC-EVENT-04     |

Rule:

```txt
Use “assignment identity”, “assignment grant”, “assignment timeline”, or “assignment_changed event” when ambiguity matters.
```

---

## 6.2 `activity`

| Usage                  | Meaning                                                          | Primary decision |
| ---------------------- | ---------------------------------------------------------------- | ---------------- |
| `activity_ref` field   | Optional event-envelope field.                                   | DEC-CONFIG-02    |
| `activity instance`    | Deployer-configured operational unit referenced by activity_ref. | DEC-CONFIG-02    |
| `activity definition`  | L0 configuration artifact.                                       | DEC-CONFIG-05    |
| `activity` scope type  | Platform-fixed scope category for access containment.            | DEC-CONFIG-08    |
| cross-activity linkage | Pattern correlation using activity_ref and payload refs.         | DEC-WORKFLOW-03  |

Rule:

```txt
Use the full phrase:
activity_ref field
activity instance
activity definition
activity scope type
cross-activity linkage
```

---

## 6.3 `process`

| Usage                     | Meaning                                               | Primary decision |
| ------------------------- | ----------------------------------------------------- | ---------------- |
| `process` identity        | Operational process instance identity.                | DEC-IDENTITY-01  |
| activity/campaign/program | Configured operational context, not process identity. | DEC-CONFIG-02    |
| workflow pattern          | Platform workflow skeleton, not process identity.     | DEC-WORKFLOW-02  |
| trigger process           | Runtime mechanism, not process identity.              | DEC-CONFIG-07    |

Rule:

```txt
process identity = referenced operational process instance
activity = configured operational context
pattern = platform workflow skeleton
trigger = server-side automation mechanism
```

---

## 6.4 `type`

| Usage              | Meaning                               | Primary decision |
| ------------------ | ------------------------------------- | ---------------- |
| event `type`       | Platform-fixed processing vocabulary. | DEC-EVENT-04     |
| identity `type`    | Discriminator in `{type,id}`.         | DEC-IDENTITY-01  |
| field type         | Shape schema field type.              | DEC-CONFIG-04    |
| flag type/category | Flag category.                        | DEC-CONFLICT-04  |

Rule:

```txt
Never use bare “type” in platform/spec/implementation docs when ambiguity matters.
```

Use:

```txt
event type
identity type
field type
flag category
```

---

## 6.5 `state`

| Usage                                 | Meaning                                      | Primary decision  |
| ------------------------------------- | -------------------------------------------- | ----------------- |
| workflow state                        | Projection-derived from events/config/flags. | DEC-WORKFLOW-01   |
| current state read model              | Projection/read model, not source truth.     | DEC-PROJECTION-01 |
| stored `current_state`                | Rejected event/envelope field.               | DEC-BOUNDARY-01   |
| lifecycle state `active` / `archived` | Subject lineage validation vocabulary.       | DEC-IDENTITY-03   |
| state machine skeleton                | Pattern Registry artifact.                   | DEC-WORKFLOW-02   |

Rule:

```txt
Workflow state is projection-derived.
Subject lineage state is validation lifecycle vocabulary.
Neither is mutable stored source truth.
```

---

## 6.6 `pattern`

| Usage                    | Meaning                                                                    | Primary decision |
| ------------------------ | -------------------------------------------------------------------------- | ---------------- |
| `pattern`                | Platform-fixed workflow skeleton.                                          | DEC-WORKFLOW-02  |
| `Pattern Registry`       | Platform registry of workflow skeletons.                                   | DEC-WORKFLOW-02  |
| subject-level pattern    | Lifecycle state pattern for subject/activity.                              | DEC-WORKFLOW-03  |
| event-level pattern      | State pattern for source event/sub-flow.                                   | DEC-WORKFLOW-03  |
| scenario pattern example | Product/problem or platform-spec input, not frozen architecture inventory. | none             |

Rule:

```txt
Do not treat scenario pattern examples as closed architecture inventory.
Exact pattern skeletons belong to platform specification unless they change platform behavior.
```

---

## 6.7 `flag`

| Usage                | Meaning                                                  | Primary decision |
| -------------------- | -------------------------------------------------------- | ---------------- |
| flag mechanism       | Event-linked anomaly handling model.                     | DEC-CONFLICT-04  |
| flag category        | Type of anomaly.                                         | DEC-CONFLICT-04  |
| flag severity        | Policy surface.                                          | DEC-CONFIG-08    |
| flag resolvability   | Platform-level auto_eligible/manual_only classification. | DEC-CONFLICT-04  |
| resolver assignment  | Operational/authorization path for resolution.           | DEC-CONFLICT-03  |
| source-only flagging | Workflow/source-chain rule.                              | DEC-WORKFLOW-06  |

Rule:

```txt
flag category ≠ severity ≠ resolvability ≠ resolver assignment
```

---

## 6.8 `trigger`

| Usage                  | Meaning                                  | Primary decision |
| ---------------------- | ---------------------------------------- | ---------------- |
| trigger definition     | L3 configuration artifact.               | DEC-CONFIG-07    |
| Trigger Engine         | Server-side runtime service.             | DEC-CONFIG-07    |
| `trigger` source type  | System actor source type.                | DEC-CONFIG-03    |
| trigger-produced event | Normal event with system actor identity. | DEC-CONFIG-03    |

Rule:

```txt
Trigger-produced events are normal events.
They use normal event envelope fields and auditable system actor identity.
```

---

## 6.9 `context`

| Usage                 | Meaning                                         | Primary decision |
| --------------------- | ----------------------------------------------- | ---------------- |
| `context.*`           | Closed pre-resolved read-only expression scope. | DEC-WORKFLOW-05  |
| dynamic context query | Rejected.                                       | DEC-BOUNDARY-01  |
| stored context fields | Rejected.                                       | DEC-EVENT-03     |

Rule:

```txt
context.* is not a query escape hatch and is not stored event data.
```

---

## 6.10 `projection`

| Usage                      | Meaning                                | Primary decision  |
| -------------------------- | -------------------------------------- | ----------------- |
| projection/read model      | Rebuildable derived state.             | DEC-PROJECTION-01 |
| authority projection       | Assignment timeline-derived authority. | DEC-AUTH-03       |
| workflow state projection  | Pattern-derived workflow state.        | DEC-WORKFLOW-01   |
| reporting projection       | Read-side operational summary.         | DEC-PROJECTION-02 |
| projection as source truth | Rejected.                              | DEC-BOUNDARY-01   |

Rule:

```txt
Projection derives meaning.
Projection does not create source truth.
```

---

# 7. Negative-Boundary Vocabulary Map

Rejected terms and paths remain active boundary evidence.

| Rejected / excluded term or path              | Primary boundary decision | Replacement / accepted boundary                      | Supporting decision |
| --------------------------------------------- | ------------------------- | ---------------------------------------------------- | ------------------- |
| mutable-in-place records                      | DEC-BOUNDARY-01           | append-only events                                   | DEC-EVENT-01        |
| last-write-wins as truth                      | DEC-BOUNDARY-01           | accept-and-flag + resolution                         | DEC-CONFLICT-01     |
| physical re-reference after merge             | DEC-BOUNDARY-01           | alias-in-projection                                  | DEC-IDENTITY-03     |
| `SubjectsUnmerged`                            | DEC-BOUNDARY-01           | `SubjectSplit`                                       | DEC-IDENTITY-03     |
| device-time structural ordering               | DEC-BOUNDARY-01           | device_seq + sync_watermark; timestamp advisory      | DEC-IDENTITY-02     |
| account-bound device identity                 | DEC-BOUNDARY-01           | hardware-bound device identity                       | DEC-IDENTITY-02     |
| `authority_context` envelope field            | DEC-BOUNDARY-01           | authority-as-projection                              | DEC-AUTH-03         |
| `assignment_ref` envelope field               | DEC-BOUNDARY-01           | assignment timeline projection                       | DEC-AUTH-03         |
| `assignment_refs` envelope field              | DEC-BOUNDARY-01           | assignment timeline projection                       | DEC-AUTH-03         |
| sync independent of access                    | DEC-BOUNDARY-01           | sync scope equals access scope                       | DEC-AUTH-02         |
| deployer-authored event types                 | DEC-BOUNDARY-01           | platform-fixed event type vocabulary                 | DEC-EVENT-04        |
| self-describing payload replacing `shape_ref` | DEC-BOUNDARY-01           | mandatory `shape_ref`                                | DEC-CONFIG-01       |
| mandatory `activity_ref` for all events       | DEC-BOUNDARY-01           | optional activity_ref                                | DEC-CONFIG-02       |
| deployer-authored access logic                | DEC-BOUNDARY-01           | assignment-based access + platform-fixed containment | DEC-AUTH-01         |
| deployer-authored containment functions       | DEC-BOUNDARY-01           | fixed scope types + scope_contains                   | DEC-AUTH-04         |
| field-level sensitivity                       | DEC-BOUNDARY-01           | shape/activity-level sensitivity                     | DEC-CONFIG-08       |
| device-side triggers                          | DEC-BOUNDARY-01           | server-only triggers                                 | DEC-CONFIG-07       |
| recursive trigger chains                      | DEC-BOUNDARY-01           | bounded trigger DAG max path length 2                | DEC-CONFIG-07       |
| deployer-authored state machines              | DEC-BOUNDARY-01           | platform-fixed Pattern Registry                      | DEC-WORKFLOW-02     |
| stored `current_state`                        | DEC-BOUNDARY-01           | projection-derived state                             | DEC-WORKFLOW-01     |
| `status_changed` event type                   | DEC-BOUNDARY-01           | existing event types + shape/activity/pattern        | DEC-EVENT-04        |
| `pattern_ref` envelope field                  | DEC-BOUNDARY-01           | pattern derived from activity + shape mapping        | DEC-WORKFLOW-02     |
| dynamic `context.*` queries                   | DEC-BOUNDARY-01           | closed pre-resolved context values                   | DEC-WORKFLOW-05     |
| stored downstream flag propagation            | DEC-BOUNDARY-01           | source-only flagging + source-chain projection       | DEC-WORKFLOW-06     |
| auto-resolution for manual-only flags         | DEC-BOUNDARY-01           | auto-resolution only for auto_eligible flags         | DEC-WORKFLOW-07     |
| custom machinery for S00                      | DEC-BOUNDARY-02           | capture_only + shape + assignment baseline           | DEC-BOUNDARY-02     |

---

# 8. Implementation/Tooling Vocabulary Exclusion Map

These terms may appear in later engineering work, but they are not settled architecture vocabulary.

| Term / area                         | Classification         | Affected decision guardrail          | Routing note                                                             |
| ----------------------------------- | ---------------------- | ------------------------------------ | ------------------------------------------------------------------------ |
| database schema                     | Implementation concern | DEC-EVENT-01, DEC-PROJECTION-01      | Implementation/tooling gap unless it changes event/projection truth.     |
| table names                         | Implementation concern | DEC-EVENT-01                         | Implementation/tooling gap.                                              |
| indexes                             | Implementation concern | DEC-PROJECTION-01, DEC-PROJECTION-02 | Implementation/tooling gap.                                              |
| queues                              | Implementation concern | DEC-CONFIG-07                        | Implementation/tooling gap unless trigger semantics change.              |
| cache invalidation                  | Implementation concern | DEC-PROJECTION-01                    | Implementation/tooling gap.                                              |
| local storage mechanics             | Implementation concern | DEC-AUTH-05                          | Implementation/tooling gap unless retention/access contract changes.     |
| shared-device storage implementation detail | Implementation concern | DEC-AUTH-02, DEC-AUTH-05             | Implementation/tooling gap inside the accepted actor-partition boundary. |
| API shape                           | Implementation concern | DEC-EVENT-03, DEC-AUTH-01            | Implementation/tooling gap unless it adds/removes structural fields.     |
| SDK behavior                        | Implementation concern | multiple                             | Implementation/tooling gap unless it changes accepted runtime semantics. |
| authoring-file syntax               | Implementation concern | DEC-CONFIG-05, DEC-CONFIG-06         | Implementation/tooling gap.                                              |
| deployment tooling UI               | Implementation concern | DEC-CONFIG-08                        | Implementation/tooling gap.                                              |
| form renderer UI                    | Implementation concern | DEC-CONFIG-04, DEC-CONFIG-06         | Implementation/tooling gap.                                              |
| flag queue UX                       | Implementation concern | DEC-CONFLICT-04, DEC-WORKFLOW-06     | Platform-spec/tooling gap depending on semantics.                        |
| source-chain visualization          | Implementation concern | DEC-WORKFLOW-06                      | Tooling/platform-spec gap unless source-chain semantics change.          |
| report dashboard UI                 | Implementation concern | DEC-PROJECTION-02                    | Implementation/tooling gap.                                              |
| projection materialization strategy | Implementation concern | DEC-PROJECTION-01                    | Implementation/tooling gap.                                              |
| sync pagination                     | Implementation concern | DEC-EVENT-02, DEC-AUTH-02            | Implementation/tooling gap unless access/sync semantics change.          |
| sync batching                       | Implementation concern | DEC-EVENT-02, DEC-AUTH-02            | Implementation/tooling gap.                                              |
| transport protocol                  | Implementation concern | DEC-EVENT-02                         | Implementation/tooling gap.                                              |
| retry mechanics                     | Implementation concern | DEC-EVENT-02, DEC-CONFIG-07          | Implementation/tooling gap.                                              |

---

# 9. Open-Front Vocabulary Map

These terms identify known unresolved work. They are not rejected needs, but they are not settled architecture unless later routed and closed.

| Term / front                            | Current classification                                                                   | Affected decision anchors                       | Closure route                                                                                    |
| --------------------------------------- | ---------------------------------------------------------------------------------------- | ----------------------------------------------- | ------------------------------------------------------------------------------------------------ |
| exact Pattern Registry inventory        | Platform-spec detail gap / platform evolution                                            | DEC-WORKFLOW-02                                 | Define pattern skeletons under Pattern Registry; ADR only if platform behavior boundary changes. |
| pattern migration mechanics             | Platform evolution / implementation-tooling gap                                          | DEC-WORKFLOW-02, DEC-PROJECTION-01              | Platform evolution or engineering design depending on semantics.                                 |
| additional pattern types                | Platform evolution                                                                       | DEC-WORKFLOW-02                                 | Platform ships new pattern; deployers select/parameterize after platform support exists.         |
| additional `context.*` values           | Platform evolution                                                                       | DEC-WORKFLOW-05, DEC-CONFIG-06                  | Must be platform-fixed, read-only, pre-resolved, append-only.                                    |
| additional auto-resolution policies     | Platform evolution                                                                       | DEC-WORKFLOW-07, DEC-CONFLICT-04                | Must remain within auto_eligible and L3b guardrails.                                             |
| richer flag queue ergonomics            | Platform-spec detail / implementation-tooling gap                                        | DEC-CONFLICT-04, DEC-WORKFLOW-06                | Specify queue behavior and UX without changing flag semantics.                                   |
| domain conflict resolution strategies   | Platform-spec detail / platform evolution                                                | DEC-CONFLICT-04, DEC-WORKFLOW-07                | Cannot bypass manual-only categories.                                                            |
| auditor/query access                    | Architecture decision gap                                                                | DEC-AUTH-02, DEC-AUTH-03, DEC-PROJECTION-02     | Formal decision required if it alters sync=access or access authority.                           |
| aggregate access semantics              | Architecture decision gap if different from event access; otherwise platform-spec detail | DEC-AUTH-02, DEC-PROJECTION-02                  | Decide whether aggregates inherit event access or require new access model.                      |
| actor-as-subject delivery rule          | Underexplored front                                                                      | DEC-IDENTITY-01, DEC-AUTH-02                    | May be sync filter or scope evolution; decide only with formal boundary check.                   |
| future finer role-action granularity    | Platform-spec detail gap / architecture decision gap                                     | DEC-AUTH-01, DEC-CONFIG-08                      | Activity role-action baseline is accepted; future finer action vocabularies require boundary check. |
| config authoring syntax                 | Implementation/tooling gap                                                               | DEC-CONFIG-05, DEC-CONFIG-06                    | Engineering/tooling design.                                                                      |
| setup lifecycle                         | Platform-spec detail gap                                                                 | DEC-CONFIG-08                                   | Draft/validate/review/approve/publish under config package boundary.                             |
| reporting freshness semantics           | Platform-spec detail gap                                                                 | DEC-PROJECTION-02                               | Define freshness, completeness, unresolved flag treatment, drilldown.                            |
| handoff package contents                | Platform-spec detail gap                                                                 | DEC-AUTH-02, DEC-WORKFLOW-03, DEC-PROJECTION-01 | Define under access/sync/projection constraints.                                                 |
| retention windows                       | Operational policy gap                                                                   | DEC-AUTH-05                                     | Policy unless it changes event immutability or sync/access.                                      |
| offboarding procedure                   | Operational policy gap                                                                   | DEC-AUTH-05, DEC-AUTH-02                        | Policy unless it changes event/access architecture.                                              |
| regulatory encryption/redaction/erasure | Platform evolution / operational policy                                                  | DEC-EVENT-01, DEC-AUTH-05                       | Separate mechanisms; must not silently redefine immutability.                                    |
| multi-tenant naming                     | Platform evolution                                                                       | DEC-CONFIG-01, DEC-CONFIG-02                    | Preserve parse safety and historical interpretation.                                             |
| complexity budget changes               | Platform evolution                                                                       | DEC-CONFIG-08                                   | Validation evolution; historical events unchanged.                                               |

---

# 10. Vocabulary Maintenance Rules

Use these rules when editing future vocabulary, platform specs, or architecture notes.

## 10.1 Before changing a term

Check:

```txt
term
→ primary decision
→ supporting decisions
→ negative boundary
→ classification
```

Then classify the proposed change.

## 10.2 If the term is structural

Any change that affects stored events, durable interpretation, identity references, sync/access authority, or historical replay is an architecture decision gap.

Examples:

* event envelope fields;
* event type semantics;
* identity reference shape;
* causal metadata;
* shape_ref / activity_ref semantics;
* sync=access.

## 10.3 If the term is strategy-protecting

Internal implementation can evolve, but weakening the invariant requires architecture review.

Examples:

* detect-before-act;
* scope-containment invariant;
* server-only triggers;
* Pattern Registry boundary;
* manual_only auto-resolution exclusion.

## 10.4 If the term is initial strategy

Platform spec or implementation may detail it, but must preserve accepted constraints.

Examples:

* selective-retain;
* expression language baseline;
* source-only flagging;
* context.* baseline;
* complexity budgets.

## 10.5 If the term is implementation concern

Do not promote it into architecture unless it changes a settled boundary.

Examples:

* database tables;
* queues;
* APIs;
* local storage;
* config authoring syntax;
* SDK behavior;
* UI.

## 10.6 If the term is product/problem evidence

Do not convert it into architecture directly.

Route through:

```txt
Product/problem evidence gap
→ scenario thickening
→ platform-spec detail or architecture decision only if pressure remains
```

---

# 11. Pass 3 Quality Gate Check

| Gate item from charter                                            | Status                                                    |
| ----------------------------------------------------------------- | --------------------------------------------------------- |
| Every locked term maps to at least one decision.                  | Satisfied for terms recovered in `007`, `008`, and `011`. |
| Every important term has one primary owner decision.              | Satisfied.                                                |
| Term collisions are explicitly handled.                           | Satisfied.                                                |
| Negative boundaries are linked to terms where relevant.           | Satisfied.                                                |
| Implementation concerns remain marked as implementation concerns. | Satisfied.                                                |
| Open fronts are not treated as settled vocabulary.                | Satisfied.                                                |
| Product/problem terms are not promoted to architecture.           | Satisfied.                                                |
| `011` final decision count is confirmed as 36.                    | Satisfied.                                                |

---

# 12. Handoff Capsule

* This pass produced:

  * `012-vocabulary-anchor-map.md`
  * classification legend;
  * ownership rules;
  * decision-to-vocabulary map;
  * alphabetical vocabulary anchor map;
  * term-collision map;
  * negative-boundary vocabulary map;
  * implementation/tooling exclusion map;
  * open-front vocabulary map;
  * vocabulary maintenance rules.
* Stable items for next pass:

  * primary DEC owner for each important vocabulary term;
  * supporting DEC anchors for cross-domain terms;
  * negative-boundary terms and their accepted replacements;
  * implementation terms that must stay out of architecture;
  * open-front terms that must route through gaps.
* Items not yet stable:

  * final gap routing playbook;
  * known gap examples with closure paths;
  * coherence audit verdict;
  * full line-level source citation audit, if later needed.
* Required next input:

  * `002-phase0-decision-register.md`
  * `007-phase5-cross-lineage-vocabulary.md`
  * `008-authoritative-architecture-map.md`
  * `009-decision-anchor-extraction-charter.md`
  * `010-candidate-architecture-decision-inventory.md`
  * `011-core-architecture-decision-records.md`
  * `012-vocabulary-anchor-map.md`
* Known risks:

  * `assignment` remains dual-use: identity category and authorization grant.
  * `activity` remains multi-use: event field, instance, definition, scope type.
  * `type` remains overloaded: event type, identity type, field type, flag category.
  * `state` remains overloaded: projection state, lineage lifecycle state, state machine skeleton, rejected stored current_state.
  * `projection` appears broadly; future work must not treat projection output as source truth.
  * open-front vocabulary may be mistaken for accepted architecture if not routed through `013`.
* Do not reinterpret:

  * This artifact does not create new architecture.
  * This artifact does not close open fronts.
  * This artifact does not define implementation mechanics.
  * This artifact does not override the CDL, contracts, current accepted baseline evidence, `011`, or the original `002`/`007`/`008` recovery lineage.
  * This artifact maps vocabulary to decisions; it does not change those decisions.
* Next pass should start from:

  * `013-gap-routing-playbook.md`
  * Scope: classify and route future gaps using DEC anchors and vocabulary anchors.
  * Include known current gaps as validation examples.
  * Do not perform coherence audit yet.
