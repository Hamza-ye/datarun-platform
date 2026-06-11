# 011 — Core Architecture Decision Records

## Context Capsule

* Artifact: `011-core-architecture-decision-records.md`
* Pass: Pass 2 — Core Architecture Decision Records
* Status: Draft normalized decision-record artifact for project-source inclusion
* Mode: Normalization and decision-record writing only; no redesign, no reopening, no new architecture decisions.
* Current authority note:

  * These DEC records are derived operational anchors.
  * Current architecture authority remains the Canonical Decision Ledger.
  * Contracts govern crossed wire/process boundaries, and BAR/NW/IDR evidence remains validation input until folded by the catch-up waves.
  * `002` and `008` are recovery verification lineage for the original extraction pass.
* Recovery verification anchor:

  * `002-phase0-decision-register.md`
* Recovered architecture-map reference:

  * `008-authoritative-architecture-map.md`
* Consumes previous artifacts:

  * `009-decision-anchor-extraction-charter.md`
  * `010-candidate-architecture-decision-inventory.md`
* Input sources:

  * `002-phase0-decision-register.md`
  * `007-phase5-cross-lineage-vocabulary.md`
  * `008-authoritative-architecture-map.md`
  * `009-decision-anchor-extraction-charter.md`
  * `010-candidate-architecture-decision-inventory.md`
* Supporting lineage sources:

  * `003-phase1-adr2-identity-conflict-recovery.md`
  * `004-phase2-adr3-auth-sync-recovery.md`
  * `005-phase3-adr4-config-boundary-recovery.md`
  * `006-phase4-adr5-state-progression-recovery.md`
* Purpose:

  * Normalize candidate architecture decisions into stable architecture decision records that future vocabulary and gap-closure work can consume.
* Scope:

  * Merge duplicate candidates.
  * Split mixed-permanence candidates where needed.
  * Remove vocabulary-only candidates.
  * Remove implementation/tooling candidates.
  * Remove open fronts that are not settled architecture.
  * Assign final `DEC-*` identifiers.
  * State ownership, non-ownership, vocabulary anchors, negative boundaries, downstream consumers, and escalation triggers.
* Non-goals:

  * Do not create new architecture.
  * Do not close open fronts.
  * Do not write the vocabulary anchor map.
  * Do not write the gap routing playbook.
  * Do not define implementation mechanisms.
  * Do not define platform-spec details under the decisions.
  * Do not change accepted ADR sub-decisions.
* Settled outputs:

  * 36 normalized architecture decision records.
  * Final decision domains and permanence classes.
  * Candidate normalization summary.
  * Explicit exclusions carried forward.
* Rejected / excluded:

  * Standalone records for implementation concerns.
  * Standalone records for open fronts.
  * Standalone records for exact pattern inventory.
  * Standalone records for config authoring syntax.
  * Standalone records for database/API/service/queue/storage mechanics.
  * Standalone records for product/problem evidence.
* Deferred / open:

  * Vocabulary-to-decision anchor map.
  * Gap routing playbook.
  * Coherence audit.
  * Exact role-action artifact.
  * Auditor/query access.
  * Aggregate access semantics where they differ from event visibility.
  * Exact Pattern Registry inventory.
  * Pattern migration mechanics.
  * Reporting freshness semantics.
  * Retention/offboarding policy.
* Terms or decisions locked:

  * The decision IDs in this document are stable for future pass consumption unless the coherence audit finds a concrete mismatch.
* Next-pass handoff:

  * Pass 3 should produce `012-vocabulary-anchor-map.md`.
  * Pass 3 should map vocabulary terms to the decision IDs defined here.

---

## 1. Normalization Summary

Pass 1 produced 57 provisional candidates.

Pass 2 normalizes them into 36 decision records:

| Domain       | Final records | Normalization result                                                                                                                                   |
| ------------ | ------------: | ------------------------------------------------------------------------------------------------------------------------------------------------------ |
| `EVENT`      |             4 | Event source-of-truth, sync unit, envelope, and type vocabulary retained as separate structural boundaries.                                            |
| `IDENTITY`   |             4 | Typed identity, causal metadata, subject lineage, and identity-resolution order retained.                                                              |
| `CONFLICT`   |             4 | Accept-and-flag, detect-before-act, single-writer resolution, and flag model retained.                                                                 |
| `AUTH`       |             5 | Assignment access, sync=access, authority projection, scope containment, and scope contraction retained.                                               |
| `CONFIG`     |             8 | Shape/activity contracts, system actor protocol, config gradient, expression boundary, trigger boundary, config delivery, and bounded policy retained. |
| `WORKFLOW`   |             7 | Projection-derived state, Pattern Registry, composition, transition flags, context.*, source-chain, and auto-resolution retained.                      |
| `PROJECTION` |             2 | General projection boundary and reporting/analytics read-side boundary retained.                                                                       |
| `BOUNDARY`   |             2 | Negative boundary register and S00 simplicity baseline retained.                                                                                       |
| `GUARD`      |             0 | Folded into the domains whose invariants are guarded.                                                                                                  |
| **Total**    |        **36** | Within target range from the extraction charter.                                                                                                       |

### 1.1 Current CDL Anchor Overlay

This overlay patches the original recovery-pass source order. Each DEC remains a derived operational anchor; the Canonical Decision Ledger remains the current authority.

| DEC ID | Current CDL anchors | Notes |
|---|---|---|
| DEC-EVENT-01 | CDL-001, CDL-019 | Append-only event truth and immutable event write model. |
| DEC-EVENT-02 | CDL-019, CDL-020, CDL-021 | Typed immutable event as atomic write, idempotent sync, and client-minted identity unit. |
| DEC-EVENT-03 | CDL-006, CDL-010, CDL-011, CDL-012 | Eleven-field envelope, structural causal metadata, advisory device time, and no derived envelope fields. |
| DEC-EVENT-04 | CDL-007, CDL-013 | Closed envelope `type` vocabulary and `shape_ref` domain discrimination. |
| DEC-IDENTITY-01 | CDL-016, CDL-017, CDL-018, CDL-020 | Reference contracts, typed subject references, actor authorship, and client-generated IDs. |
| DEC-IDENTITY-02 | CDL-010, CDL-011, CDL-020 | Device-scoped causal metadata and advisory device time. |
| DEC-IDENTITY-03 | CDL-022, CDL-023, CDL-024, CDL-025, CDL-026, CDL-027 | Duplicate identity resolution, merge aliasing, split archival, no unmerge, acyclic lineage, and online-only merge/split. |
| DEC-IDENTITY-04 | CDL-002, CDL-029, CDL-033 | Projection order, raw-reference conflict detection, and authorization against original subject scope. |
| DEC-CONFLICT-01 | CDL-003, CDL-035 | Valid state-stale events are accepted and flagged; authorization staleness is surfaced. |
| DEC-CONFLICT-02 | CDL-004, CDL-029, CDL-048 | Detect-before-act, raw-reference detection, and transition violation flagging. |
| DEC-CONFLICT-03 | CDL-028, CDL-054 | Single-writer server-validated resolution and platform-classified resolvability. |
| DEC-CONFLICT-04 | CDL-014, CDL-015, CDL-048, CDL-054 | Platform-bundled flag shapes, deterministic flag identity, transition category, and resolvability. |
| DEC-AUTH-01 | CDL-030, CDL-055 | Assignment-based access and platform-fixed scope mechanism. |
| DEC-AUTH-02 | CDL-021, CDL-031 | Scope-filtered sync and sync/access equivalence. |
| DEC-AUTH-03 | CDL-030, CDL-032 | Assignment-derived access and authority not stored in the envelope. |
| DEC-AUTH-04 | CDL-034, CDL-055 | Scope-containment assignment creation and fixed scope mechanisms. |
| DEC-AUTH-05 | CDL-037 | Scope contraction is selective-retain/device policy, not canonical event mutation. |
| DEC-CONFIG-01 | CDL-008, CDL-013, CDL-014, CDL-039 | `shape_ref` contract, platform payload shapes, and deployer shape version coexistence. |
| DEC-CONFIG-02 | CDL-009, CDL-056 | Optional `activity_ref` contract and deployer activity configuration. |
| DEC-CONFIG-03 | CDL-018 | Human/system authorship contract. |
| DEC-CONFIG-04 | CDL-039, CDL-040 | Deployer shape model, version coexistence, and full-snapshot storage. |
| DEC-CONFIG-05 | CDL-005, CDL-038 | Mechanism/instance split and four-layer configuration gradient. |
| DEC-CONFIG-06 | CDL-043, CDL-052 | Bounded expression language and bounded `context.*` form context. |
| DEC-CONFIG-07 | CDL-042, CDL-044 | Server-only L3 policy and deploy-time complexity/dependency validation. |
| DEC-CONFIG-08 | CDL-041, CDL-042, CDL-044, CDL-046 | Atomic config packages, server-only policy, deploy-time validation, and sensitivity configuration. |
| DEC-WORKFLOW-01 | CDL-002, CDL-047 | Workflow state is projection-derived and rebuildable. |
| DEC-WORKFLOW-02 | CDL-049, CDL-056 | Platform-fixed Pattern Registry and deployer activity bindings. |
| DEC-WORKFLOW-03 | CDL-050 | Bounded pattern composition. |
| DEC-WORKFLOW-04 | CDL-004, CDL-048 | Detect-before-act and transition violation flagging. |
| DEC-WORKFLOW-05 | CDL-043, CDL-052 | Bounded expression language and closed `context.*` scope. |
| DEC-WORKFLOW-06 | CDL-051 | Source-only flagging and source-chain traversal. |
| DEC-WORKFLOW-07 | CDL-053, CDL-054 | Auto-resolution mechanism and platform resolvability classification. |
| DEC-PROJECTION-01 | CDL-002, CDL-047 | Rebuildable projections and workflow projection state. |
| DEC-PROJECTION-02 | CDL-002, CDL-030, CDL-031, CDL-036 | Reporting/read models derive from events and remain access/scope constrained. |
| DEC-BOUNDARY-01 | CDL-000, CDL-005, CDL-012 | Canonical surface rule, mechanism/instance split, and no derived envelope fields. |
| DEC-BOUNDARY-02 | CDL-000, CDL-005 | S00 simplicity is preserved as a routing guard under the canonical surface and mechanism/instance split. |

### 1.2 Accepted Extension Inputs To Fold During Catch-Up

These inputs are current validation/provenance for durable accepted behavior not fully represented in the original DEC corpus. They do not supersede the CDL; Wave 2 uses them to patch `011`, and later waves consume the patched `011`.

| Area | Source anchors | Current standing to fold | Target DEC area |
|---|---|---|---|
| Activity role-action model | IDR-021, IDR-023, NW-041 | Activity role-actions are `capture`, `review`, `alert`, `task_created`, and `task_completed`; `assignment_changed` is assignment administration, not an activity role-action. | DEC-AUTH-01, DEC-AUTH-03, DEC-CONFIG-08, DEC-WORKFLOW-04 |
| Multi-axis assignment containment | IDR-024 | Assignment create/end containment applies across geographic, subject-list, and activity axes with explicit bootstrap/root semantics. | DEC-AUTH-04 |
| Pattern definition contract and delivery | IDR-025, BAR-010, NW-031 | Platform pattern definitions are canonical contract artifacts delivered in atomic config packages under `pattern_definitions`. | DEC-CONFIG-08, DEC-WORKFLOW-02 |
| Resolver routing and canonical resolution | IDR-026, FP-009 | Active conflict categories have designated-resolver routing; canonical resolution is exact designated-resolver equality; unauthorized resolutions are accepted but not canonical. | DEC-CONFLICT-03, DEC-CONFLICT-04 |
| Production auth principal binding | IDR-027, IDR-028, BAR-104, NW-037, NW-038, NW-040 | Production auth resolves validated provider principals only through explicit active `(issuer, subject) -> actor_id` bindings; groups, roles, resource claims, and JWT `actor_id` are not platform authority. Binding provisioning is deployment-managed and audited. | DEC-CONFIG-03, DEC-AUTH-01, DEC-AUTH-03 |
| Assignment-admin command capability | IDR-029, NW-050 | `assignment_admin.create` and `assignment_admin.end` are platform-owned command capabilities outside `activities[*].roles`, evaluated from deployment-configured role-to-command policy plus same-assignment containment. | DEC-AUTH-04, DEC-CONFIG-08 |
| Shared-device actor partitions | IDR-030, NW-055 | A shared device has exactly one active server-resolved actor session; mutable local state, pending push, sync progress, subject-history cursors, token/session material, and config state are actor-partitioned. | DEC-AUTH-02, DEC-AUTH-05, DEC-PROJECTION-01 |
| Subject-history backfill | BAR-004, FP-005 | Subject-history backfill is a separate authorized repair surface with independent cursor pagination, per-page authorization, alias behavior, and no normal watermark mutation. | DEC-AUTH-02, DEC-AUTH-05, DEC-PROJECTION-01 |
| Platform payload contracts | BAR-005, FP-010 | Platform-owned payload schemas under `contracts/shapes/*.schema.json` are runtime contracts, not deployer shape rows and not activity-bindable form shapes. | DEC-CONFIG-01, DEC-CONFIG-04 |
| Config package schema hygiene | BAR-010, NW-034 | `contracts/config-package.schema.json` and `contracts/shape-format.schema.json` capture current wire/package hygiene while preserving forward-compatible unknown package keys. | DEC-CONFIG-04, DEC-CONFIG-08 |

Execution caveats:

* CDL-053/CDL-054 accept the auto-resolution mechanism class, but BAR-102 and IDR-026 keep runtime auto-resolution execution deferred until a successor policy/trigger slice.
* Resolver reassignment remains a future decision; IDR-026 preserves explicit no-human-route sentinel behavior rather than implicit reassignment.
* Online production binding-admin APIs, IdP group/claim authority, broad audit/history read surfaces, emergency override writes, new scope mechanisms, retention/decommissioning, and sealed-partition recovery remain future-decision routes unless separately promoted.

---

## 2. Candidate Disposition Summary

| Pass 1 candidate group                                   | Pass 2 disposition                                                                                                      |
| -------------------------------------------------------- | ----------------------------------------------------------------------------------------------------------------------- |
| `CAND-EVENT-01`, `CAND-EVENT-02`                         | Merged into `DEC-EVENT-01` and `DEC-EVENT-02`.                                                                          |
| `CAND-EVENT-03`, `CAND-EVENT-04`                         | Folded into `DEC-EVENT-02` and `DEC-EVENT-03`.                                                                          |
| `CAND-EVENT-05`                                          | Retained as `DEC-EVENT-04`.                                                                                             |
| `CAND-IDENTITY-01`, `CAND-IDENTITY-02`                   | Merged into `DEC-IDENTITY-01`.                                                                                          |
| `CAND-IDENTITY-03`, `CAND-IDENTITY-04`                   | Merged into `DEC-IDENTITY-02`.                                                                                          |
| `CAND-IDENTITY-05`, `CAND-IDENTITY-06`                   | Normalized into `DEC-IDENTITY-03`; online validation reflected in escalation/negative boundaries.                       |
| `CAND-CONFLICT-02`, `CAND-AUTH-04`, `CAND-PROJECTION-03` | Merged into `DEC-IDENTITY-04`.                                                                                          |
| `CAND-CONFLICT-01`                                       | Retained as `DEC-CONFLICT-01`; auth/workflow extensions referenced downstream.                                          |
| `CAND-CONFLICT-03`, `CAND-WORKFLOW-06`, `CAND-GUARD-01`  | Merged into `DEC-CONFLICT-02`.                                                                                          |
| `CAND-CONFLICT-04`                                       | Retained as `DEC-CONFLICT-03`.                                                                                          |
| `CAND-CONFLICT-05`, `CAND-CONFLICT-06`                   | Merged into `DEC-CONFLICT-04`.                                                                                          |
| `CAND-AUTH-01`                                           | Retained as `DEC-AUTH-01`.                                                                                              |
| `CAND-AUTH-02`                                           | Retained as `DEC-AUTH-02`.                                                                                              |
| `CAND-AUTH-03`, `CAND-PROJECTION-02`                     | Merged into `DEC-AUTH-03`.                                                                                              |
| `CAND-AUTH-05`                                           | Retained as `DEC-AUTH-04`.                                                                                              |
| `CAND-AUTH-06`, `CAND-AUTH-07`                           | Split and normalized; stale authority folds into flag model, selective-retain retained as `DEC-AUTH-05`.                |
| `CAND-CONFIG-01`                                         | Retained as `DEC-CONFIG-01`.                                                                                            |
| `CAND-CONFIG-02`                                         | Retained as `DEC-CONFIG-02`.                                                                                            |
| `CAND-CONFIG-03`                                         | Retained as `DEC-CONFIG-03`.                                                                                            |
| `CAND-CONFIG-04`                                         | Retained as `DEC-CONFIG-04`.                                                                                            |
| `CAND-CONFIG-05`                                         | Retained as `DEC-CONFIG-05`.                                                                                            |
| `CAND-CONFIG-06`                                         | Retained as `DEC-CONFIG-06`.                                                                                            |
| `CAND-CONFIG-07`                                         | Retained as `DEC-CONFIG-07`.                                                                                            |
| `CAND-CONFIG-08`, `CAND-GUARD-02`                        | Merged into `DEC-CONFIG-08`.                                                                                            |
| `CAND-CONFIG-09`                                         | Split: access rule remains `DEC-AUTH-01`; no deployer access logic belongs in `DEC-CONFIG-05` and negative boundaries.  |
| `CAND-CONFIG-10`, `CAND-CONFIG-11`, `CAND-CONFIG-12`     | Merged into bounded policy decisions under `DEC-CONFIG-05` and `DEC-CONFIG-08`; sensitivity handled by `DEC-CONFIG-08`. |
| `CAND-WORKFLOW-01`, `CAND-PROJECTION-04`                 | Merged into `DEC-WORKFLOW-01`.                                                                                          |
| `CAND-WORKFLOW-02`                                       | Folded into `DEC-EVENT-04` and negative boundaries.                                                                     |
| `CAND-WORKFLOW-03`                                       | Retained as `DEC-WORKFLOW-02`.                                                                                          |
| `CAND-WORKFLOW-04`                                       | Retained as `DEC-WORKFLOW-03`.                                                                                          |
| `CAND-WORKFLOW-05`, `CAND-WORKFLOW-06`                   | Merged into `DEC-WORKFLOW-04`.                                                                                          |
| `CAND-WORKFLOW-07`                                       | Retained as `DEC-WORKFLOW-05`.                                                                                          |
| `CAND-WORKFLOW-08`                                       | Retained as `DEC-WORKFLOW-06`.                                                                                          |
| `CAND-WORKFLOW-09`                                       | Split: resolvability part folded into `DEC-CONFLICT-04`; auto-resolution retained as `DEC-WORKFLOW-07`.                 |
| `CAND-PROJECTION-01`, `CAND-PROJECTION-05`               | Retained as `DEC-PROJECTION-01` and `DEC-PROJECTION-02`.                                                                |
| `CAND-GUARD-*`                                           | Folded into affected domains; no standalone `GUARD` records retained.                                                   |
| `CAND-BOUNDARY-01`, `CAND-BOUNDARY-03`                   | Retained as `DEC-BOUNDARY-01` and `DEC-BOUNDARY-02`.                                                                    |
| `CAND-BOUNDARY-02`, `CAND-BOUNDARY-04`                   | Deferred to gap routing playbook; used as governance rules, not core decision records.                                  |

---

# 3. Decision Records

---

## DEC-EVENT-01: Append-only event source of truth

Status: Settled
Permanence: Structural
Primary domain: `EVENT`
Related domains: `PROJECTION`, `CONFLICT`, `WORKFLOW`

Source anchor:

* ADR-001 S1
* ADR-001 S2
* `008` Core Architectural Closure
* `008` Primitive Taxonomy: Event Store

Decision:

All platform writes enter as append-only durable events. Current state and operational meaning are derived from those events; no mutable-in-place record is the source of truth.

Owns:

* Append-only write discipline.
* Event store as durable source of truth.
* Correction as a new event, not an in-place update.
* Rebuildability as an architectural expectation.

Does not own:

* Database schema.
* Projection materialization strategy.
* UI editing workflow.
* Operational correction policy.
* Exact correction event shapes.

Vocabulary anchored:

* `append-only`
* `event`
* `event store`
* `correction`
* `write-path discipline`
* `durable facts`

Negative boundaries:

* Mutable-in-place source records are rejected.
* Last-write-wins as a default truth model is rejected.
* Optional audit trail outside the data model is rejected.

Downstream consumers:

* Product/problem evidence: may describe correction and accountability pressure but cannot require mutable truth.
* Platform spec: defines correction behavior under append-only rules.
* Implementation/tooling: chooses event-store schema, indexing, storage, and replay mechanics.
* Operational policy: defines who may correct and review corrections.

Escalation triggers:

* Any proposal to update source records in place.
* Any proposal to make projections authoritative over events.
* Any proposal to erase or replace historical facts without explicit separate architecture decision.

Open follow-up:

* Regulatory erasure/redaction mechanisms remain a separate platform evolution front.

---

## DEC-EVENT-02: Typed immutable event as atomic write and sync unit

Status: Settled
Permanence: Structural
Primary domain: `EVENT`
Related domains: `AUTH`, `IDENTITY`, `CONFLICT`

Source anchor:

* ADR-001 S2
* ADR-001 S3
* ADR-001 S4
* `008` Final Event Contract
* `008` Storage Vocabulary

Decision:

The platform’s atomic write unit and sync unit is a typed immutable event with a client-generated UUID. Sync is idempotent, append-only, and order-independent at the event level.

Owns:

* Event as atomic write unit.
* Event as sync unit.
* Client-generated event identity.
* Idempotent append-only sync behavior.
* Order-independent sync acceptance.

Does not own:

* Sync scope; that belongs to authorization.
* Sync transport protocol.
* Pagination and batching.
* Conflict resolution policy.
* Server storage topology.

Vocabulary anchored:

* `typed immutable event`
* `client-generated UUID`
* `sync unit`
* `idempotent sync`
* `id`
* `event`

Negative boundaries:

* Syncing mutable snapshots as source truth is rejected.
* Server-assigned-only event identity is outside the accepted baseline.
* Full-state replacement sync is outside the accepted event-source baseline.

Downstream consumers:

* Product/problem evidence: can rely on offline-created records having durable event identity.
* Platform spec: defines event creation and sync acceptance rules.
* Implementation/tooling: designs idempotency keys, storage indexes, retry handling, and transport mechanics.
* Operational policy: defines acceptable sync windows and support procedures.

Escalation triggers:

* Any proposal to make a mutable record the sync unit.
* Any proposal to require server connectivity before event identity exists.
* Any proposal that makes event sync dependent on arrival order as truth.

Open follow-up:

* Priority sync, pagination, batching, and backfill remain implementation strategy.

---

## DEC-EVENT-03: Final event envelope field contract

Status: Settled
Permanence: Structural
Primary domain: `EVENT`
Related domains: `IDENTITY`, `AUTH`, `CONFIG`, `WORKFLOW`

Source anchor:

* ADR-001 S5
* ADR-002 S1/S2/S3/S4/S5
* ADR-004 S1/S2/S3/S4
* ADR-005 S4/S5
* `008` Final Event Contract

Decision:

The recovered event envelope has exactly the settled durable interpretation fields:

```txt
id
type
shape_ref
activity_ref
subject_ref
actor_ref
device_id
device_seq
sync_watermark
timestamp
payload
```

Authority, assignment references, pattern identity, current workflow state, transition validity, flag resolvability, and `context.*` values are not event-envelope fields.

Owns:

* Final envelope field set.
* Durable event interpretation boundary.
* Deliberate non-fields.
* Envelope-level escalation discipline.

Does not own:

* Payload schema content.
* Shape registry implementation.
* Activity configuration.
* Authority projection implementation.
* Workflow projection implementation.

Vocabulary anchored:

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

Negative boundaries:

* `authority_context` rejected.
* `assignment_ref` / `assignment_refs` rejected.
* `pattern_ref` rejected.
* `current_state` rejected.
* `transition_validity` rejected as envelope field.
* `resolvability` rejected as envelope field.
* `context.*` values rejected as stored event fields.

Downstream consumers:

* Product/problem evidence: may pressure missing context but must first check whether it belongs in payload, projection, config, or policy.
* Platform spec: defines field semantics under this contract.
* Implementation/tooling: serializes, validates, stores, indexes, and transports these fields.
* Operational policy: defines human rules around event creation and correction, not envelope expansion.

Escalation triggers:

* Any proposal to add an envelope field.
* Any proposal to move derived authority or workflow state into stored event truth.
* Any proposal to encode deployer-defined runtime behavior in envelope fields.

Open follow-up:

* Envelope extensions require formal architecture decision.

---

## DEC-EVENT-04: Platform-fixed event type vocabulary

Status: Settled
Permanence: Structural
Primary domain: `EVENT`
Related domains: `CONFIG`, `WORKFLOW`

Source anchor:

* ADR-004 S3
* ADR-005 status progression resolution
* `008` Event Type Vocabulary

Decision:

The event `type` field is platform-fixed, closed to deployers, append-only for platform evolution, and represents platform processing behavior rather than deployment-domain meaning.

The settled initial type set is:

```txt
capture
review
alert
task_created
task_completed
assignment_changed
```

Owns:

* Structural event type vocabulary.
* Processing-behavior meaning of `type`.
* Rule for adding future structural event types.

Does not own:

* Domain event names.
* Workflow state names.
* Shape names.
* Activity names.
* Pattern inventory.

Vocabulary anchored:

* `capture`
* `review`
* `alert`
* `task_created`
* `task_completed`
* `assignment_changed`
* `status_changed` rejected

Negative boundaries:

* Deployer-authored event types are rejected.
* Domain event names as structural `type` values are rejected.
* `status_changed` as ADR-005 structural type is rejected.

Downstream consumers:

* Product/problem evidence: may describe domain actions but must not convert them directly into event types.
* Platform spec: maps domain actions to event type + shape + activity + pattern.
* Implementation/tooling: enforces allowed type set and append-only platform evolution.
* Operational policy: no direct ownership.

Escalation triggers:

* Any proposal for a new structural event type.
* Any proposal that treats a domain action as a platform `type`.
* Any proposal to let deployers configure event type vocabulary.

Open follow-up:

* Future structural event types remain platform evolution only when new processing behavior is proven.

---

## DEC-IDENTITY-01: Typed identity references and identity categories

Status: Settled
Permanence: Structural
Primary domain: `IDENTITY`
Related domains: `EVENT`, `AUTH`, `WORKFLOW`

Source anchor:

* ADR-002 S2
* `007` Unified Identity Vocabulary
* `008` Identity Architecture

Decision:

All platform identity references use the shared `{type, id}` protocol. The settled identity categories are `subject`, `actor`, `process`, and `assignment`, each with distinct lifecycle semantics.

Owns:

* Shared identity reference shape.
* Four identity categories.
* Separation between identity and policy.
* Category-level semantic boundaries.

Does not own:

* Authorization semantics of assignment beyond identity category.
* Role-action table design.
* Subject matching algorithms.
* Actor lifecycle policy.
* Process workflow semantics.

Vocabulary anchored:

* `typed identity reference`
* `{type,id}`
* `subject`
* `actor`
* `process`
* `assignment`
* `subject_ref`
* `actor_ref`

Negative boundaries:

* Untyped UUID references are rejected.
* Treating actor identity as authority is rejected.
* Treating process identity as activity, pattern, campaign definition, or trigger process is rejected.
* Treating assignment as a role alone is rejected.

Downstream consumers:

* Product/problem evidence: can introduce subjects, actors, process chains, and responsibility relations in domain-neutral terms.
* Platform spec: defines registries and lifecycle behavior under the four categories.
* Implementation/tooling: chooses storage, lookup, and serialization mechanics.
* Operational policy: defines actor provisioning and assignment governance.

Escalation triggers:

* Any proposal for a fifth identity category.
* Any proposal to use untyped IDs across identity references.
* Any proposal to collapse actor, subject, process, or assignment semantics.

Open follow-up:

* Actor-as-subject visibility remains an underexplored sync/access front, not an identity-category change by default.

---

## DEC-IDENTITY-02: Causal metadata and hardware-bound device identity

Status: Settled
Permanence: Structural
Primary domain: `IDENTITY`
Related domains: `EVENT`, `CONFLICT`

Source anchor:

* ADR-002 S1
* ADR-002 S3
* ADR-002 S4
* ADR-002 S5
* `008` Causal Ordering Contract

Decision:

Events carry causal metadata for same-device ordering, staleness detection, and concurrency detection. `device_id` is hardware-bound, `device_seq` is durable and monotonic per device, `sync_watermark` is durable, and `timestamp` / `device_time` is advisory only.

Owns:

* Device causal namespace.
* Durable per-device sequence.
* Sync watermark as knowledge-state marker.
* Rejection of device-clock structural ordering.
* Hardware-bound device identity.

Does not own:

* Global total order.
* Human-facing timeline presentation.
* Sync transport mechanics.
* Device provisioning UI.
* Conflict policy beyond causal inputs.

Vocabulary anchored:

* `device_id`
* `device_sequence`
* `device_seq`
* `sync_watermark`
* `timestamp`
* `device_time`
* `causal ordering`
* `concurrency detection`
* `hardware-bound device identity`

Negative boundaries:

* Device-time structural ordering is rejected.
* Account-bound `device_id` is rejected.
* Global total ordering from offline devices is rejected.
* Reusing a device sequence namespace across hardware is rejected.

Downstream consumers:

* Product/problem evidence: may rely on distinction between when work happened and when it became centrally visible.
* Platform spec: defines causal comparison and stale-state detection behavior.
* Implementation/tooling: persists sequence and watermark reliably on device.
* Operational policy: defines device replacement and lost-device procedures.

Escalation triggers:

* Any proposal to trust device clock for structural ordering.
* Any proposal to bind device identity to user account instead of hardware.
* Any proposal to remove or weaken durable sequence/watermark requirements.

Open follow-up:

* Presentation ordering for user timelines remains platform-spec/implementation detail.

---

## DEC-IDENTITY-03: Subject lineage by merge aliasing and corrective split

Status: Settled
Permanence: Structural
Primary domain: `IDENTITY`
Related domains: `PROJECTION`, `CONFLICT`, `AUTH`

Source anchor:

* ADR-002 S6
* ADR-002 S7
* ADR-002 S8
* ADR-002 S9
* ADR-002 S10
* `008` Subject Lineage Contract

Decision:

Subject merge is alias-in-projection via `SubjectsMerged`, mapping `retired_id → surviving_id` without rewriting historical events. Wrong merges are corrected by `SubjectSplit`; the source is archived, historical events stay with the source, successors receive future events, and lineage remains acyclic.

Owns:

* Merge semantics.
* Split semantics.
* Lineage DAG invariants.
* Active/archived subject lifecycle boundary.
* No historical re-reference rule.

Does not own:

* Subject duplicate detection algorithm.
* Merge UI.
* Resolver assignment policy.
* Projection optimization.
* Subject registry storage mechanics.

Vocabulary anchored:

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

Negative boundaries:

* Physical re-reference after merge is rejected.
* `SubjectsUnmerged` is rejected.
* Offline merge/split is rejected.
* Archived source becoming active again is rejected.

Downstream consumers:

* Product/problem evidence: can model duplicate and split real-world subjects without rewriting history.
* Platform spec: defines merge/split validation and projection behavior.
* Implementation/tooling: implements lineage storage, transitive closure, and registry interfaces.
* Operational policy: defines who may merge/split and review lineage corrections.

Escalation triggers:

* Any proposal to rewrite historical event references after merge.
* Any proposal to add unmerge as symmetric reverse.
* Any proposal to permit offline merge/split.
* Any proposal to allow lineage cycles.

Open follow-up:

* Projection rebuild optimization after merge remains implementation strategy.

---

## DEC-IDENTITY-04: Identity resolution order across conflict, projection, and authorization

Status: Settled
Permanence: Structural
Primary domain: `IDENTITY`
Related domains: `CONFLICT`, `AUTH`, `PROJECTION`

Source anchor:

* ADR-002 S13
* ADR-003 S4
* `007` Identity Resolution Rule
* `008` Identity Resolution Flow

Decision:

Identity resolution follows this order:

```txt
Conflict detection uses raw references.
Projection may resolve aliases afterward.
Authorization uses original subject_ref where aliasing exists.
```

Owns:

* Raw-reference conflict detection.
* Projection-time alias application.
* Authorization against original `subject_ref`.
* Cross-domain identity resolution order.

Does not own:

* Subject matching logic.
* Projection storage mechanics.
* Access role-action table.
* Alias visualization UI.

Vocabulary anchored:

* `raw-reference detection`
* `alias mapping`
* `alias-respects-original-scope`
* `original subject_ref`
* `projection-time alias resolution`

Negative boundaries:

* Resolving aliases before conflict detection is rejected.
* Authorizing solely against surviving alias target is rejected where original scope matters.
* Rewriting old references to simplify authorization is rejected.

Downstream consumers:

* Product/problem evidence: can describe cross-scope merge pressure without inventing new authority fields.
* Platform spec: defines detection/projection/authorization ordering.
* Implementation/tooling: implements processing pipeline and projection dependencies.
* Operational policy: defines review procedure for alias-related flags.

Escalation triggers:

* Any proposal to evaluate conflict only after alias resolution.
* Any proposal to authorize only against surviving subject after merge.
* Any proposal to store rewritten subject refs as correction to merge.

Open follow-up:

* Cross-activity subject visibility after merge remains platform-spec detail unless it changes sync/access authority.

---

## DEC-CONFLICT-01: Accept-and-flag instead of stale-state rejection

Status: Settled
Permanence: Structural
Primary domain: `CONFLICT`
Related domains: `EVENT`, `AUTH`, `WORKFLOW`

Source anchor:

* ADR-002 S14
* ADR-003 S9
* ADR-005 S1
* ADR-005 S4
* `008` Conflict Detection and Resolution Model

Decision:

Events are accepted and flagged rather than rejected for state staleness, stale authority, stale references, or invalid workflow transitions. The event remains part of the durable record; anomaly handling happens through flags, resolution, and projection.

Owns:

* Accept-and-flag model.
* Rejection boundary for stale offline work.
* Event preservation under uncertainty.

Does not own:

* Severity policy.
* Resolver assignment practice.
* Exact flag queue UX.
* Domain-specific resolution policy.
* Auto-resolution details.

Vocabulary anchored:

* `accept-and-flag`
* `state staleness`
* `flag`
* `stale_reference`
* `transition_violation`
* `scope_violation`

Negative boundaries:

* Rejecting stale offline work as default is rejected.
* Silently overwriting conflicting work is rejected.
* Treating invalid workflow transition as event rejection is rejected.

Downstream consumers:

* Product/problem evidence: can model offline conflict and stale work as accepted historical reality.
* Platform spec: defines flag creation and resolution effects.
* Implementation/tooling: implements ingestion and flag persistence.
* Operational policy: defines review, escalation, and resolver practice.

Escalation triggers:

* Any proposal to reject offline events only because projected state changed.
* Any proposal to silently drop or overwrite stale events.
* Any proposal to treat flag resolution as historical deletion.

Open follow-up:

* Domain-specific conflict strategies remain bounded platform-spec/evolution work.

---

## DEC-CONFLICT-02: Detect-before-act

Status: Settled
Permanence: Strategy-protecting
Primary domain: `CONFLICT`
Related domains: `AUTH`, `CONFIG`, `WORKFLOW`, `PROJECTION`

Source anchor:

* ADR-002 S12
* ADR-003 S7
* ADR-005 S2
* `007` Detect-before-act consolidation
* `008` Conflict Detection Pipeline

Decision:

Conflict and flag detection run before downstream policy execution, trigger execution, and workflow state derivation. Unresolved flagged events do not drive downstream action unless the flag semantics explicitly allow it.

Owns:

* Detection-before-policy ordering.
* Detection-before-workflow-state ordering.
* Processing uncertainty boundary.
* Blocking semantics for unresolved flagged events.

Does not own:

* Exact ingestion pipeline implementation.
* Queue topology.
* UI warning presentation.
* Flag severity configuration values.
* Resolver assignment policy.

Vocabulary anchored:

* `detect-before-act`
* `flagged-event exclusion`
* `downstream policy execution`
* `unresolved flagged event`

Negative boundaries:

* Firing triggers before flag checks is rejected.
* Deriving workflow state from unresolved flagged events is rejected.
* Treating flag detection as an after-the-fact report only is rejected.

Downstream consumers:

* Product/problem evidence: can assume unresolved issues affect downstream visibility and reports.
* Platform spec: defines which flag states block which downstream behavior.
* Implementation/tooling: designs ingestion ordering and processing gates.
* Operational policy: defines escalation when flagged events block work.

Escalation triggers:

* Any proposal to execute downstream triggers before conflict/flag checks.
* Any proposal to include unresolved flagged events in workflow state truth.
* Any proposal to make detect-before-act configurable by deployers.

Open follow-up:

* Flag queue ergonomics and source-chain visualization remain platform evolution/spec work.

---

## DEC-CONFLICT-03: Single-writer conflict resolution

Status: Settled
Permanence: Strategy-protecting
Primary domain: `CONFLICT`
Related domains: `AUTH`, `WORKFLOW`

Source anchor:

* ADR-002 S11
* ADR-003 S6
* `008` Conflict Resolver boundary

Decision:

Every conflict instance has exactly one designated resolver. Resolution is canonical only through that resolver path, and conflict resolution requiring authority validation is online-only.

Owns:

* Single canonical resolver per conflict.
* `ConflictDetected` / `ConflictResolved` resolution model.
* Rejection of competing canonical resolutions.
* Online-only conflict resolution where server authority validation is required.

Does not own:

* Which role is assigned as resolver.
* Resolver workload management.
* Resolution UI.
* Domain-specific resolution criteria.
* Auto-resolution policy beyond eligible categories.

Vocabulary anchored:

* `ConflictDetected`
* `ConflictResolved`
* `designated resolver`
* `single-writer resolution`
* `online-only conflict resolution`

Negative boundaries:

* Multiple competing canonical resolutions are rejected.
* Offline authoritative conflict resolution is rejected where authority must be validated.
* Recursive meta-conflicts over resolution authority are rejected.

Downstream consumers:

* Product/problem evidence: can model review and resolution responsibility as single-owner.
* Platform spec: defines conflict lifecycle and resolver assignment hooks.
* Implementation/tooling: implements resolution locking and concurrency protection.
* Operational policy: defines resolver assignment and escalation rules.

Escalation triggers:

* Any proposal allowing multiple co-equal conflict resolutions.
* Any proposal allowing offline resolution of manual-only conflicts.
* Any proposal making resolver authority event-authored rather than assignment-derived.

Open follow-up:

* Resolver assignment practice is operational policy unless it changes authority architecture.

---

## DEC-CONFLICT-04: Flag dimensions and baseline category model

Status: Settled
Permanence: Strategy-protecting
Primary domain: `CONFLICT`
Related domains: `AUTH`, `CONFIG`, `WORKFLOW`

Source anchor:

* ADR-002 S11/S12/S14
* ADR-003 S7/S9
* ADR-004 S14
* ADR-005 S1/S2/S3/S7/S9
* `007` Flag dimensions
* `008` Flag category register

Decision:

Flags have independent dimensions: category, severity, resolvability, source event, and resolver. These dimensions must not be collapsed. The baseline flag/category model spans identity, concurrency, stale reference, authorization, domain uniqueness, and workflow transition validity.

Owns:

* Flag dimension separation.
* Baseline flag/category vocabulary.
* Distinction between severity and resolvability.
* Source event linkage.
* Resolver linkage.

Does not own:

* Exact severity labels.
* Flag queue UI.
* Resolution SLAs.
* Domain-specific conflict policy.
* Report treatment of unresolved flags.

Vocabulary anchored:

* `flag`
* `category`
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

Negative boundaries:

* Collapsing severity and resolvability is rejected.
* Deployer-configured resolvability classification is rejected.
* Auto-resolution of manual-only flags is rejected.
* Stored downstream flag propagation is rejected.

Downstream consumers:

* Product/problem evidence: can describe unresolved issues and review burden.
* Platform spec: defines flag lifecycle, severity mapping, report semantics, and source-chain behavior.
* Implementation/tooling: implements flag storage, indexing, queue projections, and source links.
* Operational policy: defines resolver assignment, review procedure, and escalation.

Escalation triggers:

* Any proposal to make manual-only categories auto-resolvable.
* Any proposal to collapse flag severity into flag resolvability.
* Any proposal to copy flags onto every downstream event as stored truth.

Open follow-up:

* Reporting semantics for unresolved flags remain platform-spec detail.
* Flag queue ergonomics remain platform evolution/spec work.

---

## DEC-AUTH-01: Assignment-based access

Status: Settled
Permanence: Structural
Primary domain: `AUTH`
Related domains: `IDENTITY`, `CONFIG`

Source anchor:

* ADR-003 S1
* ADR-004 S7
* `007` Authorization Vocabulary
* `008` Authorization Architecture

Decision:

Access is assignment-based. An actor is allowed to perform an action on a target when the actor has an active assignment whose scope contains the target and whose role permits the action.

Owns:

* Assignment as authorization grant.
* Actor + role + scope + time access model.
* Scope-containment test as core access check.
* Rejection of role-only access.

Does not own:

* Exact role-action table artifact.
* UI for assigning work.
* Storage schema for assignments.
* Auditor/query access extension.
* Aggregate access policy.

Vocabulary anchored:

* `assignment-based access`
* `scope-containment test`
* `active assignment`
* `role permits action`
* `assignment`

Negative boundaries:

* Generic RBAC alone is insufficient.
* Arbitrary deployer-authored access-control logic is rejected.
* Device-local authorization assertions as verified server facts are rejected.

Downstream consumers:

* Product/problem evidence: can describe responsibility and accountability through assignments.
* Platform spec: defines assignment lifecycle, role/action table, and target-context rules.
* Implementation/tooling: implements assignment storage, evaluation, and local checks.
* Operational policy: defines who may assign, transfer, revoke, and review authority.

Escalation triggers:

* Any proposal to authorize from actor role alone.
* Any proposal to let deployers author access logic.
* Any proposal to bypass scope containment.

Open follow-up:

* Exact role-action artifact remains platform-spec/tooling detail.

---

## DEC-AUTH-02: Sync scope equals access scope

Status: Settled
Permanence: Structural
Primary domain: `AUTH`
Related domains: `EVENT`, `CONFIG`, `PROJECTION`

Source anchor:

* ADR-003 S2
* `007` Authorization and Sync Vocabulary
* `008` Authorization and Sync Architecture

Decision:

Device sync scope equals actor access scope. A device receives exactly the data the actor is authorized to hold offline. Sync is not an independent data distribution mechanism.

Owns:

* Coupling of sync scope and access scope.
* Access-scoped offline data materialization.
* Rejection of full-dataset device sync.
* Rejection of sync independent from authorization.

Does not own:

* Sync batching.
* Priority sync.
* Pagination.
* Delta protocol.
* Auditor/query access exception design.
* Aggregate access design.

Vocabulary anchored:

* `sync scope = access scope`
* `sync scope`
* `access scope`
* `selective sync`
* `Sync Scope Resolver`

Negative boundaries:

* Sync independent of access is rejected.
* Full dataset on every device is rejected.
* Unscoped offline data delivery is rejected.

Downstream consumers:

* Product/problem evidence: can pressure auditor/reporting scenarios but must classify exceptions explicitly.
* Platform spec: defines what data belongs in scope for each assignment/activity.
* Implementation/tooling: designs selective sync, pagination, caching, and local retention mechanics.
* Operational policy: defines access grants and revocation procedures.

Escalation triggers:

* Any proposal to sync data an actor is not authorized to access.
* Any proposal for auditor/query access that bypasses sync=access.
* Any aggregate view that reveals out-of-scope event data without a settled access model.

Open follow-up:

* Auditor/query access is an architecture decision gap.
* Aggregate access may be architecture decision gap if it differs from event visibility.

---

## DEC-AUTH-03: Authority is projection-derived from assignment timeline

Status: Settled
Permanence: Structural
Primary domain: `AUTH`
Related domains: `EVENT`, `PROJECTION`, `CONFIG`

Source anchor:

* ADR-003 S3
* `007` Authority-as-projection consolidation
* `008` Authorization Vocabulary

Decision:

Authority is reconstructed from the assignment timeline and related event/config context. It is not stored as `authority_context`, `assignment_ref`, or assignment-reference lists in the event envelope.

Owns:

* Authority-as-projection.
* Assignment timeline as authority source.
* Rejection of event-authored authority context.
* No new envelope fields from authorization.

Does not own:

* Assignment storage schema.
* Role-action table artifact.
* Operational resolver assignment.
* Auditor/query access.
* Projection materialization strategy.

Vocabulary anchored:

* `authority-as-projection`
* `assignment timeline`
* `authority_context` rejected
* `assignment_ref` rejected
* `assignment_refs` rejected

Negative boundaries:

* Authority context in envelope is rejected.
* Single/variable assignment refs in envelope are rejected.
* Device-authored authority assertions as verified fact are rejected.

Downstream consumers:

* Product/problem evidence: can ask “under what authority?” but answer must derive from assignment timeline.
* Platform spec: defines authority reconstruction rules and query surfaces.
* Implementation/tooling: implements assignment timeline projections and validation.
* Operational policy: defines who grants, revokes, and reviews authority.

Escalation triggers:

* Any proposal to store authorization authority in every event.
* Any proposal to make assignment refs envelope fields.
* Any proposal to treat local device authority belief as verified server authority.

Open follow-up:

* Authority explanation UX is implementation/tooling unless it changes stored authority semantics.

---

## DEC-AUTH-04: Scope-containment invariant on assignment creation

Status: Settled
Permanence: Strategy-protecting
Primary domain: `AUTH`
Related domains: `CONFIG`, `BOUNDARY`

Source anchor:

* ADR-003 S5
* `008` Scope Resolver and Assignment Resolver boundaries

Decision:

A new assignment’s scope must be contained within the creating actor’s effective scope.

```txt
new_assignment.scope ⊆ creating_actor.effective_scope
```

Owns:

* Privilege escalation prevention on assignment creation.
* Server-side containment validation.
* Assignment creation invariant.

Does not own:

* Exact assignment UI.
* Role-action artifact shape.
* Emergency override policy.
* Delegation workflow details.
* Storage mechanics.

Vocabulary anchored:

* `scope-containment invariant`
* `privilege escalation prevention`
* `effective_scope`
* `scope_contains`

Negative boundaries:

* Lateral privilege escalation through assignment creation is rejected.
* Deployer-authored containment functions are rejected.
* Client-only assignment authority validation is rejected.

Downstream consumers:

* Product/problem evidence: can describe transfers/delegations that pressure the invariant.
* Platform spec: defines assignment creation validation and exception handling if any.
* Implementation/tooling: implements containment checks and assignment write guards.
* Operational policy: defines who may assign and what exception review is required.

Escalation triggers:

* Any proposal to create assignments outside creator scope.
* Any proposal for deployer-authored containment functions.
* Any proposal to authorize assignment creation from client-local belief alone.

Open follow-up:

* Emergency override policy is operational policy unless it changes containment architecture.

---

## DEC-AUTH-05: Selective-retain on scope contraction

Status: Settled
Permanence: Initial strategy
Primary domain: `AUTH`
Related domains: `CONFIG`, `PROJECTION`, `BOUNDARY`

Source anchor:

* ADR-003 S10
* `008` Authorization Vocabulary
* `008` Open Evolution Register

Decision:

Scope contraction uses selective-retain as the baseline strategy. Data handling after access loss must preserve accountability while preventing continued inappropriate local access.

Owns:

* Baseline scope-contraction data handling strategy.
* Rejection of retain-indefinitely.
* Rejection of retain-but-hide as sufficient for sensitive data.
* Distinction between event truth and local device retention.

Does not own:

* Exact local purge mechanics.
* Shared-device storage partitioning.
* Retention windows.
* Offboarding procedure.
* Regulatory erasure/redaction architecture.

Vocabulary anchored:

* `selective-retain`
* `scope contraction`
* local retention
* access loss

Negative boundaries:

* Retain-indefinitely after scope contraction is rejected.
* Retain-but-hide as sufficient for sensitive data is rejected.
* Scope contraction must not rewrite historical events.

Downstream consumers:

* Product/problem evidence: can pressure S24/S25 lifecycle, handoff, and offboarding cases.
* Platform spec: defines retained categories and visibility rules under accepted architecture.
* Implementation/tooling: implements local deletion, archival, partitioning, and device sync mechanics.
* Operational policy: defines retention windows, offboarding, and support procedures.

Escalation triggers:

* Any proposal to keep all prior scoped data locally after access loss.
* Any proposal to erase event history to satisfy access contraction.
* Any retention policy that changes event immutability or sync=access semantics.

Open follow-up:

* Data lifecycle and offboarding need operational policy and platform-spec detailing.
* Regulatory erasure/redaction remains separate platform evolution.

---

## DEC-CONFIG-01: Mandatory `shape_ref` historical schema contract

Status: Settled
Permanence: Structural
Primary domain: `CONFIG`
Related domains: `EVENT`, `PROJECTION`

Source anchor:

* ADR-004 S1
* `008` Shape Reference Contract

Decision:

Every event carries mandatory `shape_ref` in `{shape_name}/v{version}` format. It identifies the payload schema version used to interpret the event.

Owns:

* Mandatory schema reference in event envelope.
* Stable historical payload interpretation.
* Shape version reference format.
* Rejection of self-describing payloads as replacement.

Does not own:

* Shape authoring UI.
* Shape registry storage backend.
* Field-level business semantics.
* Form rendering.
* Shape migration tooling.

Vocabulary anchored:

* `shape_ref`
* `shape`
* `shape version`
* `shape registry`

Negative boundaries:

* Self-describing payloads replacing `shape_ref` are rejected.
* Events without shape references are outside accepted baseline.
* Silent reinterpretation of old payloads under new shape is rejected.

Downstream consumers:

* Product/problem evidence: can rely on old and new shapes remaining meaningful.
* Platform spec: defines shape validation and version compatibility rules.
* Implementation/tooling: implements schema registry, validation, and historical reads.
* Operational policy: defines review and approval for shape changes.

Escalation triggers:

* Any proposal to make `shape_ref` optional.
* Any proposal to embed schema in every event instead of referencing a shape version.
* Any proposal to reinterpret historical payloads without recorded shape version.

Open follow-up:

* Shape migration mechanics are platform-spec/tooling unless they change event interpretation.

---

## DEC-CONFIG-02: Optional `activity_ref` activity-instance contract

Status: Settled
Permanence: Structural
Primary domain: `CONFIG`
Related domains: `EVENT`, `WORKFLOW`, `AUTH`

Source anchor:

* ADR-004 S2
* ADR-005 S6
* `008` Activity Reference Contract

Decision:

`activity_ref` is optional and references an activity instance. It is a correlation/context field, not mandatory provenance, not authority context, and not pattern identity.

Owns:

* Activity instance reference semantics.
* Optionality of `activity_ref`.
* Cross-activity correlation boundary.
* Rejection of mandatory fabricated provenance.

Does not own:

* Activity definition schema.
* Activity template model.
* Pattern identity.
* Assignment authority.
* Activity setup workflow.

Vocabulary anchored:

* `activity_ref`
* `activity instance`
* `activity definition`
* activity scope type

Negative boundaries:

* Mandatory `activity_ref` for all events is rejected.
* `activity_ref` as authority context is rejected.
* `activity_ref` as `pattern_ref` substitute is rejected.

Downstream consumers:

* Product/problem evidence: can describe cross-activity operational contexts.
* Platform spec: defines when activity context is stamped and how activities relate.
* Implementation/tooling: implements activity selection, stamping, and filtering.
* Operational policy: defines activity naming and governance.

Escalation triggers:

* Any proposal to make `activity_ref` mandatory for all events.
* Any proposal to use `activity_ref` as authority proof.
* Any proposal to hide pattern identity in activity_ref semantics.

Open follow-up:

* Cross-activity cohort materialization and reporting remain platform-spec details unless they alter access/sync behavior.

---

## DEC-CONFIG-03: Auditable system actor identity

Status: Settled
Permanence: Strategy-protecting
Primary domain: `CONFIG`
Related domains: `EVENT`, `WORKFLOW`

Source anchor:

* ADR-004 S4
* ADR-005 S9
* `008` System Actor Contract

Decision:

System-authored events use auditable `actor_ref` values in the form:

```txt
system:{source_type}/{source_id}
```

Examples include:

```txt
system:trigger/{trigger_id}
system:auto_resolution/{policy_id}
```

Owns:

* System actor reference format.
* Auditable authorship for automation.
* Reuse of `actor_ref` for system events.

Does not own:

* Trigger configuration content.
* Auto-resolution policy details.
* System user accounts.
* Event payload semantics.
* Operational notification routing.

Vocabulary anchored:

* `system actor`
* `system:{source_type}/{source_id}`
* `system:trigger/{trigger_id}`
* `system:auto_resolution/{policy_id}`
* `trigger` source type
* `auto_resolution` source type

Negative boundaries:

* Anonymous automation writes are rejected.
* New `system_ref` envelope field is rejected.
* Full trigger configuration stored in events is rejected.

Downstream consumers:

* Product/problem evidence: can rely on system-generated work being attributable.
* Platform spec: defines system source types and allowed emitters.
* Implementation/tooling: implements actor serialization and audit display.
* Operational policy: defines review responsibilities for system-authored events.

Escalation triggers:

* Any proposal for unaudited system events.
* Any proposal to add separate system-authorship envelope fields.
* Any proposal to use system actor identity to bypass normal invariant checks.

Open follow-up:

* Exact system source-type registry can evolve as platform specification.

---

## DEC-CONFIG-04: Shape model and evolution boundary

Status: Settled
Permanence: Initial strategy
Primary domain: `CONFIG`
Related domains: `EVENT`, `PROJECTION`

Source anchor:

* ADR-004 S10
* `008` Shape Vocabulary

Decision:

Shapes are typed payload schemas. They are versioned, delta-authored, snapshot-stored, and evolve by additive/deprecation changes by default. Breaking changes are exceptional and explicit.

Owns:

* Shape as typed payload schema.
* Shape lifecycle baseline.
* Additive/deprecation default.
* Breaking-change recognition.
* Snapshot-stored runtime interpretation.

Does not own:

* Shape authoring syntax.
* Form builder UI.
* Registry database design.
* Migration tooling details.
* Domain-specific field taxonomy.

Vocabulary anchored:

* `shape`
* `shape definition`
* `shape evolution`
* `deprecation-only`
* `breaking change`
* `delta-authored`
* `snapshot-stored`

Negative boundaries:

* Shape as event type is rejected.
* Shape as workflow engine is rejected.
* Shape as arbitrary code is rejected.
* Silent breaking changes are rejected.

Downstream consumers:

* Product/problem evidence: can describe changing information needs.
* Platform spec: defines compatibility, validation, and versioning semantics.
* Implementation/tooling: builds authoring, registry, validation, and migration tools.
* Operational policy: defines approval/review for breaking changes.

Escalation triggers:

* Any proposal for unversioned shape changes affecting historical events.
* Any proposal to let shape definitions execute arbitrary code.
* Any proposal to silently change field meaning across versions.

Open follow-up:

* Exact shape authoring syntax remains implementation/tooling.

---

## DEC-CONFIG-05: Four-layer configuration gradient and code boundary

Status: Settled
Permanence: Strategy-protecting
Primary domain: `CONFIG`
Related domains: `AUTH`, `WORKFLOW`, `BOUNDARY`

Source anchor:

* ADR-004 S9
* ADR-004 S7
* ADR-005 S5
* `008` Configuration Boundary

Decision:

Configuration is bounded by a four-layer gradient:

```txt
L0 Assembly
L1 Shape
L2 Logic
L3 Policy
Code boundary
```

Deployers may select and parameterize platform-provided structures, but they may not author arbitrary code, access-control logic, event types, or state machines.

Owns:

* Configuration expressiveness ceiling.
* L0/L1/L2/L3 distinction.
* Side-effect boundary.
* L3-to-code escalation boundary.
* Rejection of deployer-authored primitives.

Does not own:

* Exact config file syntax.
* Configuration authoring UI.
* Internal compiler/interpreter implementation.
* Exact pattern skeleton inventory.
* Exact policy forms.

Vocabulary anchored:

* `L0 Assembly`
* `L1 Shape`
* `L2 Logic`
* `L3 Policy`
* `four-layer gradient`
* `L3→code boundary`
* `configuration has boundaries`
* `side effect`

Negative boundaries:

* Configuration as arbitrary code is rejected.
* Deployer-authored event types are rejected.
* Deployer-authored access logic is rejected.
* Deployer-authored state machines are rejected.
* Recursive/unbounded trigger chains are rejected.

Downstream consumers:

* Product/problem evidence: can pressure setup experience without making config arbitrary.
* Platform spec: defines artifacts and valid configuration surfaces under each layer.
* Implementation/tooling: builds authoring tools, validators, and interpreters.
* Operational policy: defines review and publishing process for configuration.

Escalation triggers:

* Any proposal to give deployers loops, functions, arbitrary code, custom access logic, or custom state machines.
* Any proposal to add side effects below L3.
* Any proposal to blur L3 policy and platform code without platform evolution.

Open follow-up:

* Setup lifecycle and config authoring syntax remain platform-spec/tooling gaps.

---

## DEC-CONFIG-06: Bounded expression language

Status: Settled
Permanence: Initial strategy
Primary domain: `CONFIG`
Related domains: `WORKFLOW`

Source anchor:

* ADR-004 S11
* ADR-005 S8
* `008` Expression Vocabulary

Decision:

The expression language is bounded: one expression language, two contexts, operators plus field references only, zero functions, no loops, no user-defined abstractions, no side effects. ADR-004 scopes are `payload.*`, `entity.*`, and `event.*`; ADR-005 adds closed pre-resolved `context.*`.

Owns:

* Expression-language boundary.
* Zero-function baseline.
* Field-reference scope model.
* No side-effect rule.
* Distinction between expression scope and dynamic query.

Does not own:

* Syntax details.
* Parser implementation.
* Authoring UI.
* Exact validation-message wording.
* Future platform-added context values.

Vocabulary anchored:

* `expression language`
* `payload.*`
* `entity.*`
* `event.*`
* `context.*`
* zero functions

Negative boundaries:

* Expressions as arbitrary programming language are rejected.
* Dynamic cross-entity/cross-subject queries through expressions are rejected.
* Side-effectful expressions are rejected.
* Deployer-defined `context.*` values are rejected.

Downstream consumers:

* Product/problem evidence: can describe warnings and conditional behavior.
* Platform spec: defines allowed operators, field refs, evaluation timing, and error handling.
* Implementation/tooling: implements expression parser, evaluator, editor, and tests.
* Operational policy: defines review procedure for risky logic if needed.

Escalation triggers:

* Any proposal to add functions, loops, dynamic queries, or side effects.
* Any proposal to let deployers define `context.*`.
* Any proposal to use expressions to create events below L3.

Open follow-up:

* Additional `context.*` values are platform evolution if closed/read-only/pre-resolved.

---

## DEC-CONFIG-07: Server-only bounded trigger architecture

Status: Settled
Permanence: Strategy-protecting
Primary domain: `CONFIG`
Related domains: `CONFLICT`, `WORKFLOW`

Source anchor:

* ADR-004 S5
* ADR-004 S12
* ADR-005 S9
* `008` Trigger Engine boundary

Decision:

Triggers execute server-only. The settled trigger model is bounded by event-reaction triggers and deadline-check triggers, non-recursive DAG behavior, and maximum path length 2.

Owns:

* Server-only trigger execution.
* Event-reaction trigger boundary.
* Deadline-check trigger boundary.
* Non-recursion and max path length.
* Rejection of device-side triggers.

Does not own:

* Queue topology.
* Scheduler implementation.
* Retry mechanics.
* Trigger authoring syntax.
* Notification delivery channels.

Vocabulary anchored:

* `server-only triggers`
* `event-reaction trigger (3a)`
* `deadline-check trigger (3b)`
* `trigger DAG`
* `max path length 2`
* `Trigger Engine`

Negative boundaries:

* Device-side triggers are rejected.
* Recursive triggers are rejected.
* Unbounded trigger chains are rejected.
* Trigger execution before flag detection is rejected.

Downstream consumers:

* Product/problem evidence: can describe event-triggered actions and deadlines.
* Platform spec: defines trigger conditions, allowed outputs, and execution semantics.
* Implementation/tooling: builds scheduler, queues, retries, and observability.
* Operational policy: defines review for automation and escalation behavior.

Escalation triggers:

* Any proposal to run triggers on device.
* Any proposal to allow recursive or unbounded trigger graphs.
* Any proposal to allow one trigger to emit arbitrary numbers of events.

Open follow-up:

* Trigger UX and authoring syntax remain tooling/spec detail.

---

## DEC-CONFIG-08: Atomic configuration delivery and bounded policy surface

Status: Settled
Permanence: Strategy-protecting
Primary domain: `CONFIG`
Related domains: `AUTH`, `WORKFLOW`, `BOUNDARY`

Source anchor:

* ADR-004 S6
* ADR-004 S8
* ADR-004 S13
* ADR-004 S14
* `008` Config Package Validator boundary

Decision:

Configuration is delivered atomically to devices, with at most current and previous-for-in-progress versions coexisting. The deployer policy surface is bounded to platform-defined parameters such as flag severity, domain uniqueness, scope composition, and shape/activity-level sensitivity.

Owns:

* Atomic config package delivery.
* Maximum two-version coexistence baseline.
* Config validation before delivery.
* Complexity budgets as baseline guardrails.
* Shape/activity-level sensitivity classification.
* Bounded deployer policy parameterization.

Does not own:

* Exact config authoring syntax.
* Deployment UI.
* Transport protocol.
* Device storage mechanics.
* Field-level sensitivity.
* Regulatory erasure/redaction mechanics.

Vocabulary anchored:

* `atomic config delivery`
* `config version`
* `complexity budgets`
* `flag severity`
* `domain_uniqueness_violation`
* `scope composition`
* `sensitivity classification`
* `standard`
* `elevated`
* `restricted`

Negative boundaries:

* Partial config delivery is rejected.
* Field-level sensitivity is rejected.
* Deployer-defined containment logic is rejected.
* Deployer-defined flag mechanism is rejected.
* Treating complexity budgets as arbitrary deployer bypasses is rejected.

Downstream consumers:

* Product/problem evidence: can describe setup, publishing, and version-skew scenarios.
* Platform spec: defines package contents, validation rules, version coexistence behavior, and policy parameters.
* Implementation/tooling: implements package build, validation, delivery, rollback, and device update.
* Operational policy: defines review/approval/publish process.

Escalation triggers:

* Any proposal to run partially inconsistent config.
* Any proposal to add field-level sensitivity.
* Any proposal to make budgets unbounded without architecture review.
* Any proposal to let deployers redefine policy mechanisms rather than parameters.

Open follow-up:

* Setup lifecycle is platform-spec detail.
* Regulatory controls remain platform evolution/policy unless they redefine immutability.

---

## DEC-WORKFLOW-01: Projection-derived workflow state

Status: Settled
Permanence: Strategy-protecting
Primary domain: `WORKFLOW`
Related domains: `EVENT`, `CONFLICT`, `PROJECTION`

Source anchor:

* ADR-005 S4
* `008` Workflow Architecture

Decision:

Workflow state is projection-derived from event stream, pattern definition, config version, and flag status.

```txt
current_state = f(event_stream, pattern_definition, config_version, flag_status)
```

State is not stored in events, not a mutable source record, and not a reason to reject offline writes.

Owns:

* Workflow state derivation rule.
* Rejection of stored `current_state`.
* State as projection, not event truth.
* Advisory Command Validator boundary.

Does not own:

* Exact pattern inventory.
* UI state display.
* Projection storage strategy.
* State label taxonomy.
* Domain-specific lifecycle semantics.

Vocabulary anchored:

* `projection-derived state machine`
* `current_state` rejected
* `Command Validator`
* `workflow state projection`
* `flag_status`

Negative boundaries:

* Stored current workflow state is rejected.
* State-machine enforcement by event rejection is rejected.
* `status_changed` as structural event type is rejected.
* Device-only transition authority is rejected.

Downstream consumers:

* Product/problem evidence: can describe lifecycle/state needs without requiring stored state.
* Platform spec: defines pattern state derivation and transition validity.
* Implementation/tooling: builds projections, validators, and state views.
* Operational policy: defines who may resolve state anomalies.

Escalation triggers:

* Any proposal to store current workflow state as event truth.
* Any proposal to reject offline events because local projected state is stale.
* Any proposal to add workflow state envelope fields.

Open follow-up:

* Pattern skeleton details remain platform-spec.

---

## DEC-WORKFLOW-02: Platform-fixed Pattern Registry

Status: Settled
Permanence: Strategy-protecting
Primary domain: `WORKFLOW`
Related domains: `CONFIG`, `PROJECTION`

Source anchor:

* ADR-005 S5
* `008` Workflow Vocabulary

Decision:

Workflow skeletons are platform-fixed patterns in the Pattern Registry. Deployers may select and parameterize patterns at L0, but may not author state machines.

Owns:

* Pattern Registry boundary.
* Platform-fixed workflow skeletons.
* Pattern parameterization boundary.
* Rejection of deployer-authored state machines.

Does not own:

* Exact initial pattern inventory.
* Pattern implementation internals.
* Pattern migration mechanics.
* Pattern authoring by deployers.
* UI workflow builder.

Vocabulary anchored:

* `Pattern Registry`
* `pattern`
* `state machine skeleton`
* `participant roles`
* `parameterization points`

Negative boundaries:

* Deployer-authored state machines are rejected.
* Treating example patterns as frozen architecture inventory is rejected.
* `pattern_ref` envelope field is rejected.

Downstream consumers:

* Product/problem evidence: can identify needed workflow patterns as pressure.
* Platform spec: defines exact pattern skeletons and slots.
* Implementation/tooling: implements pattern registry, validation, and projection logic.
* Operational policy: defines review of activity pattern selection.

Escalation triggers:

* Any proposal to let deployers author state machines.
* Any proposal to add `pattern_ref` to event envelope.
* Any proposal for a new pattern type that changes platform behavior rather than spec inventory.

Open follow-up:

* Exact Pattern Registry inventory is platform-spec/evolution.
* Pattern migration mechanics are platform evolution/implementation strategy.

---

## DEC-WORKFLOW-03: Pattern composition rules

Status: Settled
Permanence: Strategy-protecting
Primary domain: `WORKFLOW`
Related domains: `CONFIG`, `PROJECTION`

Source anchor:

* ADR-005 S6
* `008` Workflow Architecture

Decision:

Pattern composition is constrained by five rules:

1. One subject-level pattern per activity.
2. Event-level patterns compose freely.
3. Approval sub-flows embed.
4. Cross-activity linkage uses `activity_ref`.
5. Shape-to-pattern mapping is unique within an activity.

Owns:

* Subject-level vs event-level pattern distinction.
* Composition safety rules.
* Shape-to-pattern uniqueness within activity.
* Cross-activity linkage boundary.

Does not own:

* Exact pattern skeleton definitions.
* Cross-activity cohort materialization.
* UI flow composition.
* Reporting semantics.
* Payload reference design details.

Vocabulary anchored:

* `subject-level pattern`
* `event-level pattern`
* `composition rules`
* `approval sub-flow`
* `shape-to-pattern mapping`

Negative boundaries:

* Multiple competing subject-level state machines for one subject/activity are rejected.
* Hidden pattern spanning through envelope metadata is rejected.
* Ambiguous shape ownership by multiple patterns in one activity is rejected.

Downstream consumers:

* Product/problem evidence: can test whether scenarios compose from accepted pattern rules.
* Platform spec: defines pattern bindings, slots, and validation errors.
* Implementation/tooling: implements config validation and projection dispatch.
* Operational policy: no direct ownership unless review/approval procedures are added.

Escalation triggers:

* Any proposal for multiple subject-level lifecycle patterns in one activity.
* Any proposal to bypass activity_ref for hidden cross-activity coupling.
* Any proposal to let two patterns claim the same shape in one activity.

Open follow-up:

* Cross-activity cohort materialization is platform-spec detail unless it changes sync/access behavior.

---

## DEC-WORKFLOW-04: Transition violations and flagged-event exclusion

Status: Settled
Permanence: Strategy-protecting
Primary domain: `WORKFLOW`
Related domains: `CONFLICT`, `PROJECTION`

Source anchor:

* ADR-005 S1
* ADR-005 S2
* `008` Workflow Architecture

Decision:

Invalid workflow transitions create `transition_violation` flags. Unresolved flagged events remain visible in the timeline but are excluded from workflow state derivation until resolved.

Owns:

* `transition_violation` flag category.
* Flagged-event exclusion from workflow state.
* Timeline visibility of flagged events.
* Re-derivation after resolution.

Does not own:

* Exact resolution UI.
* State label taxonomy.
* Severity configuration.
* Auto-resolution policy details.
* Report counting semantics.

Vocabulary anchored:

* `transition_violation`
* `flagged-event exclusion`
* timeline visibility
* state derivation

Negative boundaries:

* Invalid transitions rejected as writes are rejected.
* Unresolved flagged events driving state truth are rejected.
* Hiding flagged events from timeline is rejected.

Downstream consumers:

* Product/problem evidence: can describe invalid/offline transitions without losing record truth.
* Platform spec: defines transition checking, exclusion, resolution effects, and reporting semantics.
* Implementation/tooling: implements projection filters and timeline display.
* Operational policy: defines manual review procedure for transition flags.

Escalation triggers:

* Any proposal to reject event writes for transition violations.
* Any proposal to include unresolved transition violations in state truth.
* Any proposal to hide unresolved flagged events from audit timeline.

Open follow-up:

* Report interpretation of flagged events is platform-spec detail.

---

## DEC-WORKFLOW-05: Closed pre-resolved `context.*` expression scope

Status: Settled
Permanence: Initial strategy
Primary domain: `WORKFLOW`
Related domains: `CONFIG`, `PROJECTION`

Source anchor:

* ADR-005 S8
* ADR-004 S11
* `008` Expression Vocabulary

Decision:

`context.*` is a closed, platform-fixed, pre-resolved, read-only expression scope. The settled baseline values are:

```txt
context.subject_state
context.subject_pattern
context.activity_stage
context.actor.role
context.actor.scope_name
context.days_since_last_event
context.event_count
```

Owns:

* Closed `context.*` vocabulary baseline.
* Pre-resolved read-only context boundary.
* No dynamic query rule.
* Platform-only extension rule.

Does not own:

* Exact expression syntax.
* Context resolution implementation.
* UI labels.
* Future additional platform-fixed values.
* Arbitrary derived analytics.

Vocabulary anchored:

* `context.*`
* `context.subject_state`
* `context.subject_pattern`
* `context.activity_stage`
* `context.actor.role`
* `context.actor.scope_name`
* `context.days_since_last_event`
* `context.event_count`

Negative boundaries:

* Deployer-defined `context.*` values are rejected.
* Dynamic cross-subject or cross-event form queries are rejected.
* Stored `context.*` event data is rejected.

Downstream consumers:

* Product/problem evidence: can ask for contextual form behavior under bounded values.
* Platform spec: defines context resolution timing and null/error behavior.
* Implementation/tooling: implements context projection and expression evaluation.
* Operational policy: no direct ownership.

Escalation triggers:

* Any proposal to let deployers define context values.
* Any proposal to add dynamic query behavior.
* Any proposal to store context values as event facts.

Open follow-up:

* Additional `context.*` values are platform evolution if closed/read-only/pre-resolved.

---

## DEC-WORKFLOW-06: Source-only flagging and source-chain traversal

Status: Settled
Permanence: Initial strategy
Primary domain: `WORKFLOW`
Related domains: `CONFLICT`, `PROJECTION`

Source anchor:

* ADR-005 S7
* `008` Workflow Vocabulary

Decision:

Only the root-cause source event receives the stored flag. Downstream effects are computed through source-chain traversal rather than stored downstream flag propagation.

Owns:

* Source-only flagging.
* Source-chain traversal.
* Rejection of copied downstream flags.
* Computed downstream warnings.

Does not own:

* Source-chain visualization UX.
* Report rollup semantics.
* Flag queue grouping.
* Storage indexes.
* Exact event reference field placement.

Vocabulary anchored:

* `source-only flagging`
* `source-chain traversal`
* `source_event_ref`
* downstream warning

Negative boundaries:

* Stored downstream flag propagation is rejected.
* Duplicating flags onto every downstream event is rejected.
* Treating computed warning as source truth is rejected.

Downstream consumers:

* Product/problem evidence: can describe downstream impact of unresolved upstream issues.
* Platform spec: defines source-chain semantics and warning rules.
* Implementation/tooling: implements traversal, indexing, and visualization.
* Operational policy: defines review and escalation for source-chain impact.

Escalation triggers:

* Any proposal to store propagated flags as facts on every downstream event.
* Any proposal to resolve downstream warnings independently from root cause.
* Any proposal to treat warning projection as source event truth.

Open follow-up:

* Richer source-chain visualization is platform evolution/tooling.
* Flag queue ergonomics remain open.

---

## DEC-WORKFLOW-07: Bounded auto-resolution for eligible flags

Status: Settled
Permanence: Strategy-protecting
Primary domain: `WORKFLOW`
Related domains: `CONFLICT`, `CONFIG`

Source anchor:

* ADR-005 S3
* ADR-005 S9
* `008` Auto-resolution boundary

Decision:

Auto-resolution is a bounded server-side L3b subtype. It applies only to `auto_eligible` flag categories and uses auditable system actor identity such as `system:auto_resolution/{policy_id}`. Manual-only flags cannot be auto-resolved.

Owns:

* Auto-resolution eligibility boundary.
* L3b server-side auto-resolution mechanism.
* Manual-only guardrail.
* System actor identity for auto-resolution.
* Loop-prevention requirement.

Does not own:

* Exact auto-resolution policies.
* Auto-resolution UI.
* Operational approval process.
* Manual resolution workflows.
* Future eligible categories.

Vocabulary anchored:

* `auto-resolution`
* `auto_eligible`
* `manual_only`
* `system:auto_resolution/{policy_id}`
* `auto_resolution` source type

Negative boundaries:

* Auto-resolution for manual-only flags is rejected.
* Auto-resolution as unbounded rule engine is rejected.
* Device-side auto-resolution is rejected.
* Recursive resolution loops are rejected.

Downstream consumers:

* Product/problem evidence: can identify repetitive low-risk flags as pressure.
* Platform spec: defines eligible categories, policies, loop guards, and resolution events.
* Implementation/tooling: implements policy execution, audit, and loop prevention.
* Operational policy: defines approval and monitoring for automated resolution.

Escalation triggers:

* Any proposal to auto-resolve identity conflicts, scope violations, or other manual-only categories.
* Any proposal to make auto-resolution deployer-authored arbitrary logic.
* Any proposal to run auto-resolution on-device.

Open follow-up:

* Additional auto-resolution policies are platform evolution within guardrails.

---

## DEC-PROJECTION-01: Projections are derived read models, not source truth

Status: Settled
Permanence: Structural
Primary domain: `PROJECTION`
Related domains: `EVENT`, `AUTH`, `WORKFLOW`

Source anchor:

* ADR-001 S2
* `007` Unified Architectural Closure
* `008` Projection/read-model primitives

Decision:

Projections derive meaning, current state, authority, workflow state, sync scope, flags, and reports from events plus configuration. Projections are rebuildable read models and are not source-of-truth facts.

Owns:

* Projection as derived/rebuildable.
* Projection non-authority over event truth.
* Read-model boundary.
* Rejection of hidden mutable side channels.

Does not own:

* Projection materialization strategy.
* Database tables.
* Caches.
* Indexes.
* UI views.
* Report semantics.

Vocabulary anchored:

* `projection`
* `read model`
* `rebuildable`
* `derived meaning`
* `current state`

Negative boundaries:

* Projection as source-of-truth fact is rejected.
* Hidden mutable state outside event store is rejected.
* Storing current state as authoritative record is rejected.

Downstream consumers:

* Product/problem evidence: can ask for current views but must preserve source event truth.
* Platform spec: defines projection semantics and consistency rules.
* Implementation/tooling: chooses materialization, indexing, invalidation, and replay strategy.
* Operational policy: defines how projections are used in reports and reviews.

Escalation triggers:

* Any proposal to make projection state authoritative over event stream.
* Any proposal to write state changes outside event store.
* Any proposal to make report output the source of truth.

Open follow-up:

* Projection optimization remains implementation/tooling.

---

## DEC-PROJECTION-02: Reporting and analytics projections are read-side summaries

Status: Settled
Permanence: Initial strategy
Primary domain: `PROJECTION`
Related domains: `AUTH`, `CONFLICT`, `BOUNDARY`

Source anchor:

* `008` Reporting/Analytics Projections
* ADR-001 S2
* ADR-003 S2
* ADR-005 S2/S7

Decision:

Reporting and analytics outputs are read-side summaries derived from events, projections, access scope, config, and flag state. They are not source-of-truth facts and do not bypass access/sync authority.

Owns:

* Reporting/analytics as projections.
* Report non-authority over event truth.
* Access-scoped reporting boundary.
* Flag-aware projection dependency.

Does not own:

* Exact report definitions.
* Aggregate access exceptions.
* Freshness semantics.
* Dashboard UI.
* Data warehouse implementation.
* External export mappings.

Vocabulary anchored:

* `reporting/analytics projections`
* `read-side summaries`
* `aggregate oversight`
* `flag-aware reporting`
* `freshness`

Negative boundaries:

* Report summaries as source truth are rejected.
* Aggregate views that bypass access rules are not settled.
* Hiding unresolved issues inside aggregates is outside accepted semantics until specified.

Downstream consumers:

* Product/problem evidence: can pressure reporting scenarios and aggregate oversight.
* Platform spec: defines freshness, completeness, unresolved flag treatment, and drilldown.
* Implementation/tooling: builds report storage, indexes, dashboards, and export.
* Operational policy: defines who may see which reports and how reports are used.

Escalation triggers:

* Any proposal for aggregate access that differs from underlying event access.
* Any proposal to treat aggregate output as source-of-truth correction.
* Any proposal to omit flag/freshness treatment from operational reports.

Open follow-up:

* Reporting freshness and aggregate access semantics remain gaps.
* Aggregate access may require architecture decision if it differs from event visibility.

---

## DEC-BOUNDARY-01: Negative boundary register remains active

Status: Settled
Permanence: Strategy-protecting
Primary domain: `BOUNDARY`
Related domains: `EVENT`, `IDENTITY`, `AUTH`, `CONFIG`, `WORKFLOW`

Source anchor:

* `008` Negative Boundary Register
* `007` Rejected alternatives consolidation

Decision:

Rejected alternatives remain active negative boundary evidence. They must not be reintroduced as platform capability, configuration capability, implementation shortcut, or vocabulary term without a formal architecture decision that explicitly revises the relevant baseline.

Owns:

* Negative-boundary preservation.
* Rejected-alternative visibility.
* Formal escalation requirement for reintroducing rejected paths.

Does not own:

* New decision process details.
* Gap routing examples.
* Implementation linting.
* Product discovery method.

Vocabulary anchored:

* `negative boundary`
* rejected alternative
* formal architecture decision
* baseline revision

Negative boundaries:

* Mutable-in-place records.
* Physical re-reference after merge.
* `SubjectsUnmerged`.
* Device-time structural ordering.
* Account-bound device identity.
* Authority context in envelope.
* Assignment refs in envelope.
* Sync independent of access.
* Deployer-authored event types.
* Deployer-authored access logic.
* Field-level sensitivity.
* Device-side triggers.
* Recursive triggers.
* Deployer-authored state machines.
* Stored `current_state`.
* `pattern_ref`.
* Stored downstream flag propagation.
* Auto-resolution of manual-only flags.

Downstream consumers:

* Product/problem evidence: can pressure rejected alternatives but must route them as architecture decision gaps.
* Platform spec: must not specify rejected alternatives under accepted boundaries.
* Implementation/tooling: must not implement rejected shortcuts.
* Operational policy: must not rely on rejected mechanisms.

Escalation triggers:

* Any proposed feature that matches a negative boundary.
* Any implementation shortcut that recreates a rejected design.
* Any vocabulary change that normalizes a rejected concept.

Open follow-up:

* Gap routing playbook should include negative-boundary checks.

---

## DEC-BOUNDARY-02: S00 simplicity baseline

Status: Settled
Permanence: Strategy-protecting
Primary domain: `BOUNDARY`
Related domains: `CONFIG`, `EVENT`, `AUTH`, `WORKFLOW`

Source anchor:

* `008` S00 Simplicity Check
* Platform scenario S00

Decision:

The simplest structured capture scenario must remain simple. Basic structured capture must not require custom event types, custom access-control code, custom triggers, deployer-authored state machines, `pattern_ref`, `authority_context`, field-level sensitivity, auto-resolution policy, or workflow flag propagation.

Owns:

* S00 simplicity guardrail.
* Complexity floor for the platform baseline.
* Rejection of requiring advanced machinery for basic capture.

Does not own:

* Exact S00 UI.
* Shape authoring UX.
* Basic deployment wizard.
* Product onboarding flow.
* Form renderer implementation.

Vocabulary anchored:

* `S00 simplicity baseline`
* `capture_only`
* simplest scenario
* basic structured capture

Negative boundaries:

* Custom event types for basic capture are rejected.
* Custom access code for basic capture is rejected.
* Custom triggers or state machines for basic capture are rejected.
* Workflow flag propagation for basic capture is rejected.

Downstream consumers:

* Product/problem evidence: validates that new scenario pressure does not overcomplicate the baseline.
* Platform spec: keeps minimal setup path small.
* Implementation/tooling: must provide a simple configured path.
* Operational policy: no direct ownership.

Escalation triggers:

* Any proposal that makes S00 require advanced configuration.
* Any proposal that makes basic capture depend on workflow machinery.
* Any proposal that makes simple setup require developer intervention.

Open follow-up:

* S00 acceptance criteria can be thickened as product/problem evidence and platform spec.

---

## 4. Explicitly Removed From Core Decision Records

The following Pass 1 candidates or candidate fragments were deliberately removed from core decision records and should be handled elsewhere.

| Removed item                                           | Reason                                                                               | Later route                                                               |
| ------------------------------------------------------ | ------------------------------------------------------------------------------------ | ------------------------------------------------------------------------- |
| Standalone `GUARD` domain records                      | Guard responsibilities are already owned by conflict/auth/config/workflow decisions. | Covered by relevant DEC records; gap playbook may reference guard checks. |
| Open evolution register as a decision record           | Open fronts are not settled architecture decisions.                                  | Gap routing playbook.                                                     |
| Implementation mechanisms boundary as decision record  | Important governance rule but better consumed in gap routing.                        | Gap routing playbook.                                                     |
| Exact Pattern Registry inventory                       | Explicitly open.                                                                     | Platform-spec detail or platform evolution.                               |
| Pattern migration mechanics                            | Explicitly open.                                                                     | Platform evolution / implementation tooling.                              |
| Auditor/query access                                   | Not settled and may affect sync=access.                                              | Architecture decision gap.                                                |
| Aggregate access if different from event access        | Not settled and may affect access authority.                                         | Architecture decision gap.                                                |
| Reporting freshness semantics                          | Needed, but under projection/reporting spec.                                         | Platform-spec detail gap.                                                 |
| Handoff package contents                               | Needed, but under accepted auth/sync/projection boundaries.                          | Platform-spec detail gap.                                                 |
| Setup approval lifecycle                               | Needed, but under config boundary.                                                   | Platform-spec detail gap.                                                 |
| Retention windows                                      | Human/legal/operations policy unless event immutability changes.                     | Operational policy gap.                                                   |
| Config authoring syntax                                | Tooling.                                                                             | Implementation/tooling gap.                                               |
| Database/API/queue/index/cache/local storage mechanics | Implementation.                                                                      | Implementation/tooling gap.                                               |
| SME validation and scenario thickening                 | Product evidence.                                                                    | Product/problem evidence gap.                                             |

---

## 5. Final Decision Index

| ID                | Title                                                                    | Domain       | Permanence          |
| ----------------- | ------------------------------------------------------------------------ | ------------ | ------------------- |
| DEC-EVENT-01      | Append-only event source of truth                                        | `EVENT`      | Structural          |
| DEC-EVENT-02      | Typed immutable event as atomic write and sync unit                      | `EVENT`      | Structural          |
| DEC-EVENT-03      | Final event envelope field contract                                      | `EVENT`      | Structural          |
| DEC-EVENT-04      | Platform-fixed event type vocabulary                                     | `EVENT`      | Structural          |
| DEC-IDENTITY-01   | Typed identity references and identity categories                        | `IDENTITY`   | Structural          |
| DEC-IDENTITY-02   | Causal metadata and hardware-bound device identity                       | `IDENTITY`   | Structural          |
| DEC-IDENTITY-03   | Subject lineage by merge aliasing and corrective split                   | `IDENTITY`   | Structural          |
| DEC-IDENTITY-04   | Identity resolution order across conflict, projection, and authorization | `IDENTITY`   | Structural          |
| DEC-CONFLICT-01   | Accept-and-flag instead of stale-state rejection                         | `CONFLICT`   | Structural          |
| DEC-CONFLICT-02   | Detect-before-act                                                        | `CONFLICT`   | Strategy-protecting |
| DEC-CONFLICT-03   | Single-writer conflict resolution                                        | `CONFLICT`   | Strategy-protecting |
| DEC-CONFLICT-04   | Flag dimensions and baseline category model                              | `CONFLICT`   | Strategy-protecting |
| DEC-AUTH-01       | Assignment-based access                                                  | `AUTH`       | Structural          |
| DEC-AUTH-02       | Sync scope equals access scope                                           | `AUTH`       | Structural          |
| DEC-AUTH-03       | Authority is projection-derived from assignment timeline                 | `AUTH`       | Structural          |
| DEC-AUTH-04       | Scope-containment invariant on assignment creation                       | `AUTH`       | Strategy-protecting |
| DEC-AUTH-05       | Selective-retain on scope contraction                                    | `AUTH`       | Initial strategy    |
| DEC-CONFIG-01     | Mandatory `shape_ref` historical schema contract                         | `CONFIG`     | Structural          |
| DEC-CONFIG-02     | Optional `activity_ref` activity-instance contract                       | `CONFIG`     | Structural          |
| DEC-CONFIG-03     | Auditable system actor identity                                          | `CONFIG`     | Strategy-protecting |
| DEC-CONFIG-04     | Shape model and evolution boundary                                       | `CONFIG`     | Initial strategy    |
| DEC-CONFIG-05     | Four-layer configuration gradient and code boundary                      | `CONFIG`     | Strategy-protecting |
| DEC-CONFIG-06     | Bounded expression language                                              | `CONFIG`     | Initial strategy    |
| DEC-CONFIG-07     | Server-only bounded trigger architecture                                 | `CONFIG`     | Strategy-protecting |
| DEC-CONFIG-08     | Atomic configuration delivery and bounded policy surface                 | `CONFIG`     | Strategy-protecting |
| DEC-WORKFLOW-01   | Projection-derived workflow state                                        | `WORKFLOW`   | Strategy-protecting |
| DEC-WORKFLOW-02   | Platform-fixed Pattern Registry                                          | `WORKFLOW`   | Strategy-protecting |
| DEC-WORKFLOW-03   | Pattern composition rules                                                | `WORKFLOW`   | Strategy-protecting |
| DEC-WORKFLOW-04   | Transition violations and flagged-event exclusion                        | `WORKFLOW`   | Strategy-protecting |
| DEC-WORKFLOW-05   | Closed pre-resolved `context.*` expression scope                         | `WORKFLOW`   | Initial strategy    |
| DEC-WORKFLOW-06   | Source-only flagging and source-chain traversal                          | `WORKFLOW`   | Initial strategy    |
| DEC-WORKFLOW-07   | Bounded auto-resolution for eligible flags                               | `WORKFLOW`   | Strategy-protecting |
| DEC-PROJECTION-01 | Projections are derived read models, not source truth                    | `PROJECTION` | Structural          |
| DEC-PROJECTION-02 | Reporting and analytics projections are read-side summaries              | `PROJECTION` | Initial strategy    |
| DEC-BOUNDARY-01   | Negative boundary register remains active                                | `BOUNDARY`   | Strategy-protecting |
| DEC-BOUNDARY-02   | S00 simplicity baseline                                                  | `BOUNDARY`   | Strategy-protecting |

---

## 6. Pass 2 Quality Gate Check

| Gate item from charter                            | Status     |
| ------------------------------------------------- | ---------- |
| Every record maps to `002` or `008`.              | Satisfied. |
| Every record has one primary domain.              | Satisfied. |
| Every record has a permanence class.              | Satisfied. |
| Every record states owns / does-not-own.          | Satisfied. |
| Every record lists vocabulary anchored.           | Satisfied. |
| Every record lists downstream consumers.          | Satisfied. |
| Every record lists escalation triggers.           | Satisfied. |
| Open fronts are not treated as settled decisions. | Satisfied. |
| Implementation/tooling entries removed.           | Satisfied. |
| S00 simplicity baseline preserved.                | Satisfied. |

---

## 7. Handoff Capsule

* This pass produced:

  * `011-core-architecture-decision-records.md`
  * 36 normalized architecture decision records
  * Final DEC IDs
  * Final domain assignments
  * Final permanence classes
  * Candidate disposition summary
  * Explicit exclusions
  * Decision index
* Stable items for next pass:

  * Final decision IDs.
  * Final decision titles.
  * Final decision ownership boundaries.
  * Final vocabulary lists per decision.
  * Negative boundaries per decision.
  * Escalation triggers per decision.
* Items not yet stable:

  * Vocabulary-to-decision map.
  * Primary/supporting decision mapping for each term.
  * Term collision table.
  * Gap routing playbook examples.
  * Coherence audit result.
* Required next input:

  * `002-phase0-decision-register.md`
  * `007-phase5-cross-lineage-vocabulary.md`
  * `008-authoritative-architecture-map.md`
  * `009-decision-anchor-extraction-charter.md`
  * `010-candidate-architecture-decision-inventory.md`
  * `011-core-architecture-decision-records.md`
* Known risks:

  * Some decision records intentionally span multiple ADRs; Pass 3 must preserve primary/supporting decision relationships.
  * Some vocabulary terms belong to multiple records; Pass 3 must assign one primary owner where possible.
  * `PROJECTION` terms may appear in event/auth/workflow decisions; Pass 3 must prevent duplicate ownership confusion.
  * `activity` remains a term collision: activity_ref field, activity instance, activity definition, and activity scope type.
  * `assignment` remains a term collision: identity category and authorization grant.
* Do not reinterpret:

  * These records do not create new architecture.
  * These records do not close open fronts.
  * These records do not define implementation mechanics.
  * These records do not override the CDL, contracts, current accepted baseline evidence, or the original `002`/`008` recovery lineage.
  * Removed items are not rejected as needs; they are routed out of core decision records.
* Next pass should start from:

  * `012-vocabulary-anchor-map.md`
  * Scope: map vocabulary terms to primary and supporting decision anchors.
  * Do not write gap routing playbook yet.
