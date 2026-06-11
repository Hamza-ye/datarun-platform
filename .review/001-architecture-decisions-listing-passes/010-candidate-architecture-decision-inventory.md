# 010 — Candidate Architecture Decision Inventory

## Context Capsule

* Artifact: `010-candidate-architecture-decision-inventory.md`
* Pass: Pass 1 — Candidate Architecture Decision Inventory
* Status: Draft candidate inventory for project-source inclusion
* Mode: Inventory only; no normalization, no final decision records, no gap routing, no redesign, no reopening.
* Primary anchor:

  * `002-phase0-decision-register.md`
* Top-level architecture reference:

  * `008-authoritative-architecture-map.md`
* Consumes previous artifact:

  * `009-decision-anchor-extraction-charter.md`
* Input sources:

  * `002-phase0-decision-register.md`
  * `007-phase5-cross-lineage-vocabulary.md`
  * `008-authoritative-architecture-map.md`
  * `009-decision-anchor-extraction-charter.md`
* Supporting lineage sources:

  * `003-phase1-adr2-identity-conflict-recovery.md`
  * `004-phase2-adr3-auth-sync-recovery.md`
  * `005-phase3-adr4-config-boundary-recovery.md`
  * `006-phase4-adr5-state-progression-recovery.md`
* Purpose:

  * Inventory candidate architecture decision anchors that may become normalized decision records in the next pass.
* Scope:

  * List candidate decision anchors.
  * Assign each candidate one proposed primary domain.
  * Record source anchors.
  * Record proposed permanence.
  * Record vocabulary affected.
  * Record possible overlaps and normalization risks.
* Non-goals:

  * Do not finalize decision records.
  * Do not decide final granularity.
  * Do not route gaps.
  * Do not write vocabulary anchor map.
  * Do not settle open fronts.
  * Do not introduce architecture beyond accepted ADR anchors.
* Settled outputs:

  * Candidate inventory only.
  * No candidate is final.
  * Candidate IDs are provisional.
* Rejected / excluded:

  * One decision per ADR.
  * One decision per ADR sub-decision.
  * Treating open fronts as settled.
  * Treating implementation mechanisms as architecture.
  * Treating product/problem scenario pressure as architecture unless already converted by accepted ADRs.
* Deferred / open:

  * Candidate merging.
  * Candidate splitting.
  * Candidate removal.
  * Final DEC IDs.
  * Final decision records.
  * Vocabulary anchor map.
  * Gap routing playbook.
  * Coherence audit.
* Terms or decisions locked:

  * None. This pass locks no final architecture decision.
* Next-pass handoff:

  * Pass 2 should normalize this inventory into `011-core-architecture-decision-records.md`.
  * Pass 2 may merge, split, rename, remove, or re-domain any candidate.

---

## 1. Pass Checkpoint

This pass follows `009-decision-anchor-extraction-charter.md`.

The required Pass 1 output is:

```txt
candidate DEC list
domain
candidate title
source ADR anchors
permanence
included vocabulary
possible overlaps
```

This document is therefore deliberately provisional.

Candidate IDs use this form:

```txt
CAND-<DOMAIN>-<NN>
```

These are not final `DEC-*` identifiers.

---

## 2. Inventory Rules Used

A candidate appears here only when it satisfies at least one of:

1. It maps to accepted ADR sub-decisions in `002-phase0-decision-register.md`.
2. It appears in `008-authoritative-architecture-map.md` as settled architecture derived from accepted ADRs.
3. It appears in `007-phase5-cross-lineage-vocabulary.md` as consolidated architecture vocabulary traceable to accepted ADRs.

A candidate may still be removed in Pass 2 if it is:

* vocabulary-only;
* too broad;
* too narrow;
* implementation/tooling;
* platform-spec detail;
* operational policy;
* product/problem evidence;
* an open front rather than a settled decision.

---

## 3. Candidate Inventory Summary

| Domain       | Candidate count | Notes                                                                                                |
| ------------ | --------------: | ---------------------------------------------------------------------------------------------------- |
| `EVENT`      |               5 | Source-of-truth, write unit, event envelope, event type boundary.                                    |
| `IDENTITY`   |               6 | Typed references, identity categories, causal metadata, subject lineage.                             |
| `CONFLICT`   |               6 | Accept-and-flag, detect-before-act, conflict resolution, flag dimensions.                            |
| `AUTH`       |               7 | Assignment access, sync/access coupling, authority projection, containment, stale auth.              |
| `CONFIG`     |              11 | Shape/activity, config gradient, expressions, triggers, scope/sensitivity, budgets.                  |
| `WORKFLOW`   |               9 | Projection-derived state, Pattern Registry, composition, state flags, source-chain, auto-resolution. |
| `PROJECTION` |               5 | Read-model derivation boundaries that may normalize into other domains.                              |
| `GUARD`      |               4 | Runtime invariant guards, likely overlaps with service-level candidates.                             |
| `BOUNDARY`   |               4 | Negative boundaries, open evolution, S00 simplicity, implementation boundary.                        |
| **Total**    |          **57** | Expected to reduce in Pass 2.                                                                        |

The high count is intentional. Pass 1 inventories candidates before normalization.

---

## 4. Candidate Decisions — `EVENT`

### CAND-EVENT-01 — Append-only durable facts

| Field               | Value                                                                                         |
| ------------------- | --------------------------------------------------------------------------------------------- |
| Proposed domain     | `EVENT`                                                                                       |
| Proposed permanence | Structural                                                                                    |
| Source anchors      | ADR-001 S1, ADR-001 S2; `008` Core Architectural Closure                                      |
| Candidate decision  | All writes enter as append-only durable facts; no mutable-in-place source-of-truth records.   |
| Vocabulary affected | `append-only`, `event`, `event store`, `correction`, `write-path discipline`, `durable facts` |
| Possible overlaps   | May merge with CAND-EVENT-02 or CAND-PROJECTION-01.                                           |
| Normalization risk  | Could be too broad if it combines append-only storage with projection derivation.             |

### CAND-EVENT-02 — Typed immutable event as atomic write and sync unit

| Field               | Value                                                                                                                     |
| ------------------- | ------------------------------------------------------------------------------------------------------------------------- |
| Proposed domain     | `EVENT`                                                                                                                   |
| Proposed permanence | Structural                                                                                                                |
| Source anchors      | ADR-001 S2, ADR-001 S4                                                                                                    |
| Candidate decision  | The atomic write unit and sync unit is the typed immutable event; sync is idempotent, append-only, and order-independent. |
| Vocabulary affected | `event`, `sync unit`, `idempotent sync`, `typed immutable event`                                                          |
| Possible overlaps   | May merge with CAND-EVENT-01.                                                                                             |
| Normalization risk  | Sync scope belongs under `AUTH`; sync unit belongs here. Keep distinction.                                                |

### CAND-EVENT-03 — Client-generated event identity

| Field               | Value                                                                                          |
| ------------------- | ---------------------------------------------------------------------------------------------- |
| Proposed domain     | `EVENT`                                                                                        |
| Proposed permanence | Structural                                                                                     |
| Source anchors      | ADR-001 S3                                                                                     |
| Candidate decision  | Event IDs are client-generated UUIDs.                                                          |
| Vocabulary affected | `client-generated UUID`, `id`                                                                  |
| Possible overlaps   | May merge into CAND-EVENT-04 final envelope.                                                   |
| Normalization risk  | May be too narrow as a standalone final record unless tied to offline identity/write contract. |

### CAND-EVENT-04 — Final event envelope field contract

| Field               | Value                                                                                                                                                        |
| ------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------ |
| Proposed domain     | `EVENT`                                                                                                                                                      |
| Proposed permanence | Structural                                                                                                                                                   |
| Source anchors      | ADR-001 S5; ADR-002 S1/S2/S3/S4/S5; ADR-004 S1/S2/S3/S4; `008` Final Event Contract                                                                          |
| Candidate decision  | The recovered event envelope contains exactly the settled durable interpretation fields and excludes authority, workflow state, pattern, and context fields. |
| Vocabulary affected | `event envelope`, `id`, `type`, `shape_ref`, `activity_ref`, `subject_ref`, `actor_ref`, `device_id`, `device_seq`, `sync_watermark`, `timestamp`, `payload` |
| Possible overlaps   | Overlaps with shape, activity, identity, causal metadata, system actor, and event type candidates.                                                           |
| Normalization risk  | Could become a summary decision that references field-specific decisions rather than owning all field semantics.                                             |

### CAND-EVENT-05 — Platform-fixed event type vocabulary

| Field               | Value                                                                                                                                                   |
| ------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------- |
| Proposed domain     | `EVENT`                                                                                                                                                 |
| Proposed permanence | Structural                                                                                                                                              |
| Source anchors      | ADR-004 S3; ADR-005 status_changed resolution; `008` Event Type Vocabulary                                                                              |
| Candidate decision  | Event `type` is platform-fixed, closed to deployers, append-only for platform evolution, and represents processing behavior rather than domain meaning. |
| Vocabulary affected | `capture`, `review`, `alert`, `task_created`, `task_completed`, `assignment_changed`, `status_changed` rejected                                         |
| Possible overlaps   | May overlap with CAND-WORKFLOW-02 because `status_changed` was resolved in workflow.                                                                    |
| Normalization risk  | Should probably remain distinct because event type semantics are structural.                                                                            |

---

## 5. Candidate Decisions — `IDENTITY`

### CAND-IDENTITY-01 — Typed identity reference protocol

| Field               | Value                                                               |
| ------------------- | ------------------------------------------------------------------- |
| Proposed domain     | `IDENTITY`                                                          |
| Proposed permanence | Structural                                                          |
| Source anchors      | ADR-002 S2; `008` Identity Architecture                             |
| Candidate decision  | All identity references use `{type, id}` and are not untyped UUIDs. |
| Vocabulary affected | `typed identity reference`, `{type,id}`, `subject_ref`, `actor_ref` |
| Possible overlaps   | May merge with CAND-IDENTITY-02.                                    |
| Normalization risk  | Might be too narrow unless paired with the category set.            |

### CAND-IDENTITY-02 — Four identity categories with separate lifecycles

| Field               | Value                                                                                                                               |
| ------------------- | ----------------------------------------------------------------------------------------------------------------------------------- |
| Proposed domain     | `IDENTITY`                                                                                                                          |
| Proposed permanence | Structural                                                                                                                          |
| Source anchors      | ADR-002 S2; Phase 1 recovery; `008` Identity Architecture                                                                           |
| Candidate decision  | The platform recognizes four identity categories: subject, actor, process, and assignment, each with different lifecycle semantics. |
| Vocabulary affected | `subject`, `actor`, `process`, `assignment`                                                                                         |
| Possible overlaps   | May merge with CAND-IDENTITY-01. Assignment also overlaps with `AUTH`.                                                              |
| Normalization risk  | Assignment has dual role: identity category and authorization grant. Pass 2 must preserve both meanings.                            |

### CAND-IDENTITY-03 — Causal metadata contract

| Field               | Value                                                                                                                                  |
| ------------------- | -------------------------------------------------------------------------------------------------------------------------------------- |
| Proposed domain     | `IDENTITY`                                                                                                                             |
| Proposed permanence | Structural                                                                                                                             |
| Source anchors      | ADR-002 S1, S3, S4                                                                                                                     |
| Candidate decision  | Events carry causal metadata for same-device order, staleness detection, and concurrency detection; device time is advisory only.      |
| Vocabulary affected | `device_id`, `device_sequence`, `device_seq`, `sync_watermark`, `device_time`, `timestamp`, `causal ordering`, `concurrency detection` |
| Possible overlaps   | May split between causal ordering and advisory timestamp.                                                                              |
| Normalization risk  | `timestamp` is envelope field, but device-time trust boundary is identity/conflict-related.                                            |

### CAND-IDENTITY-04 — Hardware-bound device identity and durable sequence namespace

| Field               | Value                                                                                                                                            |
| ------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------ |
| Proposed domain     | `IDENTITY`                                                                                                                                       |
| Proposed permanence | Structural                                                                                                                                       |
| Source anchors      | ADR-002 S4, S5                                                                                                                                   |
| Candidate decision  | `device_id` is hardware-bound, not user-bound; `(device_id, device_sequence)` is globally unique and sequence/watermark persistence is required. |
| Vocabulary affected | `hardware-bound device identity`, `device_id`, `device_sequence`, `sync_watermark`                                                               |
| Possible overlaps   | May merge with CAND-IDENTITY-03.                                                                                                                 |
| Normalization risk  | Could be a sub-boundary under causal metadata.                                                                                                   |

### CAND-IDENTITY-05 — Subject merge as alias-in-projection

| Field               | Value                                                                                                                |
| ------------------- | -------------------------------------------------------------------------------------------------------------------- |
| Proposed domain     | `IDENTITY`                                                                                                           |
| Proposed permanence | Structural                                                                                                           |
| Source anchors      | ADR-002 S6, S13                                                                                                      |
| Candidate decision  | Subject merge maps `retired_id → surviving_id` through projection aliasing; historical events are not re-referenced. |
| Vocabulary affected | `SubjectsMerged`, `alias mapping`, `retired_id`, `surviving_id`, `transitive closure`                                |
| Possible overlaps   | Overlaps with CAND-PROJECTION-03 and CAND-CONFLICT-02.                                                               |
| Normalization risk  | Must preserve the difference between lineage operation and alias projection.                                         |

### CAND-IDENTITY-06 — Subject split and lineage DAG constraints

| Field               | Value                                                                                                                                                                       |
| ------------------- | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| Proposed domain     | `IDENTITY`                                                                                                                                                                  |
| Proposed permanence | Structural                                                                                                                                                                  |
| Source anchors      | ADR-002 S7, S8, S9, S10                                                                                                                                                     |
| Candidate decision  | Wrong merges are corrected through `SubjectSplit`; source is archived, successors receive new events, lineage is acyclic, and merge/split are online-only server-validated. |
| Vocabulary affected | `SubjectSplit`, `corrective split`, `archived`, `successor`, `lineage DAG`, `active`, `online-only operations`                                                              |
| Possible overlaps   | Could split online-only validation into `GUARD`.                                                                                                                            |
| Normalization risk  | Combines structural lineage semantics with strategy-protecting validation location. Pass 2 may split.                                                                       |

---

## 6. Candidate Decisions — `CONFLICT`

### CAND-CONFLICT-01 — Accept-and-flag

| Field               | Value                                                                                                                       |
| ------------------- | --------------------------------------------------------------------------------------------------------------------------- |
| Proposed domain     | `CONFLICT`                                                                                                                  |
| Proposed permanence | Structural                                                                                                                  |
| Source anchors      | ADR-002 S14; ADR-003 S9; ADR-005 S1/S4                                                                                      |
| Candidate decision  | Events are accepted and flagged rather than rejected for state staleness, stale authority, or invalid workflow transitions. |
| Vocabulary affected | `accept-and-flag`, `state staleness`, `flag`, `transition_violation`                                                        |
| Possible overlaps   | Overlaps with CAND-WORKFLOW-01 and CAND-AUTH-06.                                                                            |
| Normalization risk  | Base accept-and-flag may stay under conflict; specific extensions may stay under auth/workflow.                             |

### CAND-CONFLICT-02 — Raw-reference detection before alias projection

| Field               | Value                                                                                            |
| ------------------- | ------------------------------------------------------------------------------------------------ |
| Proposed domain     | `CONFLICT`                                                                                       |
| Proposed permanence | Structural                                                                                       |
| Source anchors      | ADR-002 S13; ADR-003 S4                                                                          |
| Candidate decision  | Conflict detection uses original raw subject references before projection-time alias resolution. |
| Vocabulary affected | `raw-reference detection`, `alias mapping`, `original subject_ref`                               |
| Possible overlaps   | Overlaps with CAND-IDENTITY-05 and CAND-AUTH-04.                                                 |
| Normalization risk  | May merge into a broader identity-resolution decision.                                           |

### CAND-CONFLICT-03 — Detect-before-act

| Field               | Value                                                                                                    |
| ------------------- | -------------------------------------------------------------------------------------------------------- |
| Proposed domain     | `CONFLICT`                                                                                               |
| Proposed permanence | Strategy-protecting                                                                                      |
| Source anchors      | ADR-002 S12; ADR-003 S7; ADR-005 S2                                                                      |
| Candidate decision  | Conflict and flag detection run before downstream policy execution and before workflow state derivation. |
| Vocabulary affected | `detect-before-act`, `flagged-event exclusion`, `downstream policy execution`                            |
| Possible overlaps   | Overlaps with `GUARD` and `WORKFLOW`.                                                                    |
| Normalization risk  | Could become a central guard decision rather than conflict-only.                                         |

### CAND-CONFLICT-04 — Single-writer conflict resolution

| Field               | Value                                                                                                                        |
| ------------------- | ---------------------------------------------------------------------------------------------------------------------------- |
| Proposed domain     | `CONFLICT`                                                                                                                   |
| Proposed permanence | Strategy-protecting                                                                                                          |
| Source anchors      | ADR-002 S11; ADR-003 S6                                                                                                      |
| Candidate decision  | Every conflict designates exactly one resolver; conflict resolution is online-only where required by authority validation.   |
| Vocabulary affected | `ConflictDetected`, `ConflictResolved`, `designated resolver`, `single-writer resolution`, `online-only conflict resolution` |
| Possible overlaps   | Resolver authority overlaps with `AUTH`.                                                                                     |
| Normalization risk  | May split canonical single-writer resolution from online authority validation.                                               |

### CAND-CONFLICT-05 — Flag dimension model

| Field               | Value                                                                                                                                     |
| ------------------- | ----------------------------------------------------------------------------------------------------------------------------------------- |
| Proposed domain     | `CONFLICT`                                                                                                                                |
| Proposed permanence | Strategy-protecting                                                                                                                       |
| Source anchors      | ADR-002 S11/S12; ADR-003 S7/S9; ADR-004 S14; ADR-005 S1/S2/S3/S7/S9; `007` flag dimensions                                                |
| Candidate decision  | Flags have independent dimensions: category, severity, resolvability, source event, and resolver. These dimensions must not be collapsed. |
| Vocabulary affected | `flag`, `category`, `severity`, `flag resolvability`, `source_event_ref`, `resolver`                                                      |
| Possible overlaps   | Overlaps with CAND-WORKFLOW-09 and CAND-CONFIG-11/12.                                                                                     |
| Normalization risk  | May be a consolidation decision rather than primary architecture decision.                                                                |

### CAND-CONFLICT-06 — Conflict and flag category register

| Field               | Value                                                                                                                                                                                                          |
| ------------------- | -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| Proposed domain     | `CONFLICT`                                                                                                                                                                                                     |
| Proposed permanence | Initial strategy / Strategy-protecting mixed                                                                                                                                                                   |
| Source anchors      | ADR-002 conflict categories; ADR-003 auth flags; ADR-004 domain uniqueness; ADR-005 transition violation                                                                                                       |
| Candidate decision  | The platform recognizes a settled baseline set of conflict/flag categories across identity, causality, authorization, domain uniqueness, and workflow transition validity.                                     |
| Vocabulary affected | `identity_conflict`, `concurrent_state_change`, `stale_reference`, `scope_violation`, `ScopeStaleFlag`, `RoleStaleFlag`, `TemporalAuthorityExpiredFlag`, `domain_uniqueness_violation`, `transition_violation` |
| Possible overlaps   | May split by source ADR or merge into flag model.                                                                                                                                                              |
| Normalization risk  | Mixed permanence. Some categories are structural/strategy-protecting; severity and exact handling may be policy.                                                                                               |

---

## 7. Candidate Decisions — `AUTH`

### CAND-AUTH-01 — Assignment-based access rule

| Field               | Value                                                                                           |
| ------------------- | ----------------------------------------------------------------------------------------------- |
| Proposed domain     | `AUTH`                                                                                          |
| Proposed permanence | Structural                                                                                      |
| Source anchors      | ADR-003 S1                                                                                      |
| Candidate decision  | Access is determined by active assignment, scope containment, and role permission.              |
| Vocabulary affected | `assignment-based access`, `scope-containment test`, `role permits action`, `active assignment` |
| Possible overlaps   | Role-action table artifact is open and must not be settled here.                                |
| Normalization risk  | Must not turn into a full role-action table design.                                             |

### CAND-AUTH-02 — Sync scope equals access scope

| Field               | Value                                                                                   |
| ------------------- | --------------------------------------------------------------------------------------- |
| Proposed domain     | `AUTH`                                                                                  |
| Proposed permanence | Structural                                                                              |
| Source anchors      | ADR-003 S2                                                                              |
| Candidate decision  | Device sync scope equals actor access scope; sync is not independent data distribution. |
| Vocabulary affected | `sync scope = access scope`, `sync scope`, `access scope`, `selective sync`             |
| Possible overlaps   | Interacts with auditor/query access and aggregate access open fronts.                   |
| Normalization risk  | Must preserve auditor/query access as open, not settled exception.                      |

### CAND-AUTH-03 — Authority is projection-derived, not event-authored

| Field               | Value                                                                                                                                                            |
| ------------------- | ---------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| Proposed domain     | `AUTH`                                                                                                                                                           |
| Proposed permanence | Structural                                                                                                                                                       |
| Source anchors      | ADR-003 S3                                                                                                                                                       |
| Candidate decision  | Authority is reconstructed from assignment timeline and related context; it is not stored as `authority_context` or assignment references in the event envelope. |
| Vocabulary affected | `authority-as-projection`, `assignment timeline`, `authority_context` rejected, `assignment_ref` rejected                                                        |
| Possible overlaps   | Overlaps with CAND-EVENT-04 and CAND-PROJECTION-02.                                                                                                              |
| Normalization risk  | Must not imply authority is unknowable; it is reconstructable from projection.                                                                                   |

### CAND-AUTH-04 — Alias-respects-original-scope

| Field               | Value                                                                                                                |
| ------------------- | -------------------------------------------------------------------------------------------------------------------- |
| Proposed domain     | `AUTH`                                                                                                               |
| Proposed permanence | Structural                                                                                                           |
| Source anchors      | ADR-003 S4                                                                                                           |
| Candidate decision  | Authorization evaluates original `subject_ref` where aliasing exists, not only the alias-resolved surviving subject. |
| Vocabulary affected | `alias-respects-original-scope`, `original subject_ref`, `alias mapping`                                             |
| Possible overlaps   | Overlaps with raw-reference detection and subject merge candidates.                                                  |
| Normalization risk  | Could merge into identity resolution interaction rule.                                                               |

### CAND-AUTH-05 — Scope-containment invariant on assignment creation

| Field               | Value                                                                               |
| ------------------- | ----------------------------------------------------------------------------------- |
| Proposed domain     | `AUTH`                                                                              |
| Proposed permanence | Strategy-protecting                                                                 |
| Source anchors      | ADR-003 S5                                                                          |
| Candidate decision  | New assignment scope must be contained within the creating actor’s effective scope. |
| Vocabulary affected | `scope-containment invariant`, `privilege escalation prevention`, `effective_scope` |
| Possible overlaps   | Runtime guard / assignment resolver.                                                |
| Normalization risk  | May remain as its own strategy-protecting guard.                                    |

### CAND-AUTH-06 — Authorization staleness uses accept-and-flag

| Field               | Value                                                                                                     |
| ------------------- | --------------------------------------------------------------------------------------------------------- |
| Proposed domain     | `AUTH`                                                                                                    |
| Proposed permanence | Initial strategy                                                                                          |
| Source anchors      | ADR-003 S7, S9                                                                                            |
| Candidate decision  | Offline work under stale local authority is accepted and flagged using authorization-specific flag types. |
| Vocabulary affected | `ScopeStaleFlag`, `RoleStaleFlag`, `TemporalAuthorityExpiredFlag`, `scope_violation`, `accept-and-flag`   |
| Possible overlaps   | Overlaps with CAND-CONFLICT-01 and CAND-CONFLICT-06.                                                      |
| Normalization risk  | May become part of broader flag model.                                                                    |

### CAND-AUTH-07 — Selective-retain on scope contraction

| Field               | Value                                                                                                                 |
| ------------------- | --------------------------------------------------------------------------------------------------------------------- |
| Proposed domain     | `AUTH`                                                                                                                |
| Proposed permanence | Initial strategy                                                                                                      |
| Source anchors      | ADR-003 S10                                                                                                           |
| Candidate decision  | Scope contraction uses selective-retain rather than retaining all data indefinitely or hiding out-of-scope data only. |
| Vocabulary affected | `selective-retain`, `scope contraction`                                                                               |
| Possible overlaps   | May overlap with implementation/security policy and S24/S25 lifecycle gaps.                                           |
| Normalization risk  | Need to keep exact purge/storage mechanics out of architecture.                                                       |

---

## 8. Candidate Decisions — `CONFIG`

### CAND-CONFIG-01 — Mandatory `shape_ref` contract

| Field               | Value                                                                                                             |
| ------------------- | ----------------------------------------------------------------------------------------------------------------- |
| Proposed domain     | `CONFIG`                                                                                                          |
| Proposed permanence | Structural                                                                                                        |
| Source anchors      | ADR-004 S1                                                                                                        |
| Candidate decision  | Every event carries mandatory `shape_ref` in `{shape_name}/v{version}` format to identify payload schema version. |
| Vocabulary affected | `shape_ref`, `shape`, `shape version`, `shape registry`                                                           |
| Possible overlaps   | May relate to final event envelope candidate.                                                                     |
| Normalization risk  | Should likely remain separate because it owns historical payload interpretation.                                  |

### CAND-CONFIG-02 — Optional `activity_ref` activity-instance contract

| Field               | Value                                                                                                                                   |
| ------------------- | --------------------------------------------------------------------------------------------------------------------------------------- |
| Proposed domain     | `CONFIG`                                                                                                                                |
| Proposed permanence | Structural                                                                                                                              |
| Source anchors      | ADR-004 S2                                                                                                                              |
| Candidate decision  | `activity_ref` is optional and references an activity instance; it is not mandatory provenance, authority context, or pattern identity. |
| Vocabulary affected | `activity_ref`, `activity instance`, `activity definition`                                                                              |
| Possible overlaps   | Cross-activity workflow linking uses `activity_ref`; overlaps with CAND-WORKFLOW-04.                                                    |
| Normalization risk  | Must disambiguate activity instance, activity scope type, and activity definition.                                                      |

### CAND-CONFIG-03 — System actor identity protocol

| Field               | Value                                                                                                                   |
| ------------------- | ----------------------------------------------------------------------------------------------------------------------- |
| Proposed domain     | `CONFIG`                                                                                                                |
| Proposed permanence | Strategy-protecting                                                                                                     |
| Source anchors      | ADR-004 S4; ADR-005 S9                                                                                                  |
| Candidate decision  | System-authored events use auditable `actor_ref` values such as `system:{source_type}/{source_id}`.                     |
| Vocabulary affected | `system actor`, `system:{source_type}/{source_id}`, `system:trigger/{trigger_id}`, `system:auto_resolution/{policy_id}` |
| Possible overlaps   | Could move to `EVENT` because it affects `actor_ref`; could move to `WORKFLOW` for auto-resolution actor.               |
| Normalization risk  | Need one owner for system actor protocol.                                                                               |

### CAND-CONFIG-04 — Shape model and evolution boundary

| Field               | Value                                                                                                                                                                  |
| ------------------- | ---------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| Proposed domain     | `CONFIG`                                                                                                                                                               |
| Proposed permanence | Initial strategy                                                                                                                                                       |
| Source anchors      | ADR-004 S10                                                                                                                                                            |
| Candidate decision  | Shapes are versioned typed payload schemas, delta-authored, snapshot-stored, with additive/deprecation evolution by default and explicit handling for breaking change. |
| Vocabulary affected | `shape definition`, `shape evolution`, `deprecation-only`, `breaking change`, `snapshot-stored`, `delta-authored`                                                      |
| Possible overlaps   | Overlaps with CAND-CONFIG-01.                                                                                                                                          |
| Normalization risk  | Some shape semantics may be structural through `shape_ref`; authoring/storage strategy is more evolvable.                                                              |

### CAND-CONFIG-05 — Four-layer configuration gradient

| Field               | Value                                                                                             |
| ------------------- | ------------------------------------------------------------------------------------------------- |
| Proposed domain     | `CONFIG`                                                                                          |
| Proposed permanence | Initial strategy / Strategy-protecting mixed                                                      |
| Source anchors      | ADR-004 S9                                                                                        |
| Candidate decision  | Configuration is bounded by L0 Assembly, L1 Shape, L2 Logic, L3 Policy, and the L3→Code boundary. |
| Vocabulary affected | `L0 Assembly`, `L1 Shape`, `L2 Logic`, `L3 Policy`, `four-layer gradient`, `L3→code boundary`     |
| Possible overlaps   | Related to expression, trigger, pattern, and policy candidates.                                   |
| Normalization risk  | Could be umbrella decision with sub-decisions under it.                                           |

### CAND-CONFIG-06 — Expression language boundary

| Field               | Value                                                                                                                                                            |
| ------------------- | ---------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| Proposed domain     | `CONFIG`                                                                                                                                                         |
| Proposed permanence | Initial strategy                                                                                                                                                 |
| Source anchors      | ADR-004 S11; ADR-005 S8                                                                                                                                          |
| Candidate decision  | The expression language is bounded: one language, field references and operators only, zero functions, with fixed scopes and later closed `context.*` extension. |
| Vocabulary affected | `expression language`, `payload.*`, `entity.*`, `event.*`, `context.*`, zero functions                                                                           |
| Possible overlaps   | CAND-WORKFLOW-07 owns `context.*`; this candidate owns general expression boundary.                                                                              |
| Normalization risk  | May split ADR-004 expression scope from ADR-005 context scope.                                                                                                   |

### CAND-CONFIG-07 — Server-only bounded trigger architecture

| Field               | Value                                                                                                                               |
| ------------------- | ----------------------------------------------------------------------------------------------------------------------------------- |
| Proposed domain     | `CONFIG`                                                                                                                            |
| Proposed permanence | Strategy-protecting / Initial strategy mixed                                                                                        |
| Source anchors      | ADR-004 S5, S12                                                                                                                     |
| Candidate decision  | Triggers execute server-only and are bounded as event-reaction or deadline-check triggers with non-recursive DAG max path length 2. |
| Vocabulary affected | `server-only triggers`, `event-reaction trigger (3a)`, `deadline-check trigger (3b)`, `trigger DAG`, `max path length 2`            |
| Possible overlaps   | Runtime guard and workflow auto-resolution.                                                                                         |
| Normalization risk  | Server-only may be strategy-protecting; 3a/3b model and budgets may be initial strategy.                                            |

### CAND-CONFIG-08 — Atomic configuration delivery and version coexistence

| Field               | Value                                                                                                     |
| ------------------- | --------------------------------------------------------------------------------------------------------- |
| Proposed domain     | `CONFIG`                                                                                                  |
| Proposed permanence | Strategy-protecting                                                                                       |
| Source anchors      | ADR-004 S6                                                                                                |
| Candidate decision  | Configuration is delivered atomically; devices run at most current and previous-for-in-progress versions. |
| Vocabulary affected | `atomic config delivery`, `config version`, version coexistence                                           |
| Possible overlaps   | S23 setup lifecycle is platform-spec detail, not this decision.                                           |
| Normalization risk  | Exact delivery protocol is implementation/tooling and must stay excluded.                                 |

### CAND-CONFIG-09 — Platform-fixed scope types and no deployer-authored access logic

| Field               | Value                                                                                                                  |
| ------------------- | ---------------------------------------------------------------------------------------------------------------------- |
| Proposed domain     | `CONFIG`                                                                                                               |
| Proposed permanence | Strategy-protecting                                                                                                    |
| Source anchors      | ADR-004 S7; ADR-003 S1/S5                                                                                              |
| Candidate decision  | Scope types and containment are platform-fixed; deployers cannot author access-control logic or containment functions. |
| Vocabulary affected | `geographic`, `subject_list`, `activity`, `scope composition`, no deployer-authored access logic                       |
| Possible overlaps   | Strong overlap with `AUTH`.                                                                                            |
| Normalization risk  | Pass 2 must decide whether scope type vocabulary belongs under `CONFIG` while access rule remains under `AUTH`.        |

### CAND-CONFIG-10 — Shape/activity-level sensitivity boundary

| Field               | Value                                                                                                                  |
| ------------------- | ---------------------------------------------------------------------------------------------------------------------- |
| Proposed domain     | `CONFIG`                                                                                                               |
| Proposed permanence | Strategy-protecting                                                                                                    |
| Source anchors      | ADR-004 S8, S14                                                                                                        |
| Candidate decision  | Sensitivity classification is at shape/activity level; field-level sensitivity is rejected from the accepted baseline. |
| Vocabulary affected | `sensitivity classification`, `standard`, `elevated`, `restricted`, field-level sensitivity rejected                   |
| Possible overlaps   | Retention and regulatory controls are open evolution/policy, not settled here.                                         |
| Normalization risk  | Exact handling of restricted data is not decided by this candidate.                                                    |

### CAND-CONFIG-11 — Complexity budgets

| Field               | Value                                                                                                                             |
| ------------------- | --------------------------------------------------------------------------------------------------------------------------------- |
| Proposed domain     | `CONFIG`                                                                                                                          |
| Proposed permanence | Initial strategy                                                                                                                  |
| Source anchors      | ADR-004 S13                                                                                                                       |
| Candidate decision  | Baseline complexity budgets constrain shape size, predicate count, trigger count, deployment trigger count, and escalation depth. |
| Vocabulary affected | `complexity budgets`                                                                                                              |
| Possible overlaps   | Could be part of four-layer gradient.                                                                                             |
| Normalization risk  | Budget values may evolve without event migration; do not over-freeze them.                                                        |

### CAND-CONFIG-12 — Deployer-parameterized policy surface

| Field               | Value                                                                                                                                                                                                                  |
| ------------------- | ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| Proposed domain     | `CONFIG`                                                                                                                                                                                                               |
| Proposed permanence | Initial strategy                                                                                                                                                                                                       |
| Source anchors      | ADR-004 S14; ADR-005 S3/S9                                                                                                                                                                                             |
| Candidate decision  | Deployers may parameterize bounded policies such as flag severity, domain uniqueness, scope composition, sensitivity levels, and eligible auto-resolution policy parameters, but may not redefine platform mechanisms. |
| Vocabulary affected | `flag severity`, `domain_uniqueness_violation`, `scope composition`, `sensitivity levels`, `auto-resolution policy`                                                                                                    |
| Possible overlaps   | Overlaps with conflict flag model and workflow auto-resolution.                                                                                                                                                        |
| Normalization risk  | Could be too broad. Pass 2 may split policy surface by category.                                                                                                                                                       |

---

## 9. Candidate Decisions — `WORKFLOW`

### CAND-WORKFLOW-01 — Projection-derived workflow state

| Field               | Value                                                                                                                                                             |
| ------------------- | ----------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| Proposed domain     | `WORKFLOW`                                                                                                                                                        |
| Proposed permanence | Initial strategy                                                                                                                                                  |
| Source anchors      | ADR-005 S4                                                                                                                                                        |
| Candidate decision  | Workflow state is derived from event stream, pattern definition, config version, and flag status; it is not stored in events and not used to reject offline work. |
| Vocabulary affected | `projection-derived state machine`, `current_state`, `Command Validator`, state projection                                                                        |
| Possible overlaps   | Overlaps with CAND-PROJECTION-01 and CAND-CONFLICT-01.                                                                                                            |
| Normalization risk  | The “not stored” part may be structural negative boundary even if pattern strategy evolves.                                                                       |

### CAND-WORKFLOW-02 — No `status_changed` structural event type

| Field               | Value                                                                                                                                        |
| ------------------- | -------------------------------------------------------------------------------------------------------------------------------------------- |
| Proposed domain     | `WORKFLOW`                                                                                                                                   |
| Proposed permanence | Structural negative boundary / Event overlap                                                                                                 |
| Source anchors      | ADR-005 status_changed evaluation; ADR-004 S3                                                                                                |
| Candidate decision  | State progression is expressed through existing event types, shapes, and patterns; `status_changed` is not added as a structural event type. |
| Vocabulary affected | `status_changed` rejected, `capture`, `review`, `task_created`, `task_completed`                                                             |
| Possible overlaps   | Strong overlap with CAND-EVENT-05.                                                                                                           |
| Normalization risk  | May become a negative boundary under event type vocabulary instead of its own decision.                                                      |

### CAND-WORKFLOW-03 — Platform-fixed Pattern Registry

| Field               | Value                                                                                                                                              |
| ------------------- | -------------------------------------------------------------------------------------------------------------------------------------------------- |
| Proposed domain     | `WORKFLOW`                                                                                                                                         |
| Proposed permanence | Initial strategy / Strategy-protecting mixed                                                                                                       |
| Source anchors      | ADR-005 S5                                                                                                                                         |
| Candidate decision  | Workflow skeletons are platform-fixed patterns selected and parameterized by deployers at L0; deployers do not author state machines.              |
| Vocabulary affected | `Pattern Registry`, `pattern`, `state machine skeleton`, `participant roles`, `parameterization points`, deployer-authored state machines rejected |
| Possible overlaps   | Exact pattern inventory is open platform spec/evolution.                                                                                           |
| Normalization risk  | Must not freeze example pattern inventory.                                                                                                         |

### CAND-WORKFLOW-04 — Pattern composition rules

| Field               | Value                                                                                                                                                                                                                                                           |
| ------------------- | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| Proposed domain     | `WORKFLOW`                                                                                                                                                                                                                                                      |
| Proposed permanence | Initial strategy / Strategy-protecting mixed                                                                                                                                                                                                                    |
| Source anchors      | ADR-005 S6                                                                                                                                                                                                                                                      |
| Candidate decision  | Pattern composition is constrained by five rules: one subject-level pattern per activity, event-level patterns compose freely, approval sub-flows embed, cross-activity linking uses `activity_ref`, and shape-to-pattern mapping is unique within an activity. |
| Vocabulary affected | `subject-level pattern`, `event-level pattern`, `composition rules`, `activity_ref`, shape-to-pattern mapping                                                                                                                                                   |
| Possible overlaps   | Cross-activity linking overlaps with activity_ref.                                                                                                                                                                                                              |
| Normalization risk  | Could split into subject-level/event-level distinction plus composition validation.                                                                                                                                                                             |

### CAND-WORKFLOW-05 — `transition_violation` flag category

| Field               | Value                                                                                                       |
| ------------------- | ----------------------------------------------------------------------------------------------------------- |
| Proposed domain     | `WORKFLOW`                                                                                                  |
| Proposed permanence | Strategy-protecting                                                                                         |
| Source anchors      | ADR-005 S1                                                                                                  |
| Candidate decision  | Invalid workflow transitions are surfaced through `transition_violation` flags rather than event rejection. |
| Vocabulary affected | `transition_violation`, transition validity, flag                                                           |
| Possible overlaps   | Overlaps with accept-and-flag and flagged-event exclusion.                                                  |
| Normalization risk  | May merge into workflow flag handling.                                                                      |

### CAND-WORKFLOW-06 — Flagged-event exclusion from state derivation

| Field               | Value                                                                                                 |
| ------------------- | ----------------------------------------------------------------------------------------------------- |
| Proposed domain     | `WORKFLOW`                                                                                            |
| Proposed permanence | Strategy-protecting                                                                                   |
| Source anchors      | ADR-005 S2                                                                                            |
| Candidate decision  | Unresolved flagged events remain visible in timeline but are excluded from workflow state derivation. |
| Vocabulary affected | `flagged-event exclusion`, timeline visibility, state derivation                                      |
| Possible overlaps   | Overlaps with detect-before-act.                                                                      |
| Normalization risk  | Could be part of CAND-CONFLICT-03 if detect-before-act becomes central guard.                         |

### CAND-WORKFLOW-07 — Closed pre-resolved `context.*` scope

| Field               | Value                                                                                                                                                                                  |
| ------------------- | -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| Proposed domain     | `WORKFLOW`                                                                                                                                                                             |
| Proposed permanence | Initial strategy                                                                                                                                                                       |
| Source anchors      | ADR-005 S8                                                                                                                                                                             |
| Candidate decision  | `context.*` is a closed, platform-fixed, pre-resolved, read-only expression scope with seven settled values.                                                                           |
| Vocabulary affected | `context.subject_state`, `context.subject_pattern`, `context.activity_stage`, `context.actor.role`, `context.actor.scope_name`, `context.days_since_last_event`, `context.event_count` |
| Possible overlaps   | Overlaps with expression language boundary.                                                                                                                                            |
| Normalization risk  | Additional `context.*` values are platform evolution, not deployer configuration.                                                                                                      |

### CAND-WORKFLOW-08 — Source-only flagging and source-chain traversal

| Field               | Value                                                                                                                      |
| ------------------- | -------------------------------------------------------------------------------------------------------------------------- |
| Proposed domain     | `WORKFLOW`                                                                                                                 |
| Proposed permanence | Initial strategy                                                                                                           |
| Source anchors      | ADR-005 S7                                                                                                                 |
| Candidate decision  | Only the root-cause source event receives the stored flag; downstream impacts are computed through source-chain traversal. |
| Vocabulary affected | `source-only flagging`, `source-chain traversal`, `source_event_ref`, stored downstream flag propagation rejected          |
| Possible overlaps   | Overlaps with projection/read-model semantics.                                                                             |
| Normalization risk  | Visualization and flag queue ergonomics are open evolution, not settled architecture.                                      |

### CAND-WORKFLOW-09 — Flag resolvability and bounded auto-resolution

| Field               | Value                                                                                                                                                           |
| ------------------- | --------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| Proposed domain     | `WORKFLOW`                                                                                                                                                      |
| Proposed permanence | Strategy-protecting / Initial strategy mixed                                                                                                                    |
| Source anchors      | ADR-005 S3, S9                                                                                                                                                  |
| Candidate decision  | Flag categories have platform-level resolvability classification; auto-resolution applies only to `auto_eligible` flags through bounded L3b server-side policy. |
| Vocabulary affected | `auto_eligible`, `manual_only`, `flag resolvability`, `auto-resolution`, `system:auto_resolution/{policy_id}`                                                   |
| Possible overlaps   | Overlaps with conflict flag dimensions and config policy surface.                                                                                               |
| Normalization risk  | May split into resolvability classification and auto-resolution mechanism.                                                                                      |

---

## 10. Candidate Decisions — `PROJECTION`

### CAND-PROJECTION-01 — Projections derive meaning and current state

| Field               | Value                                                                                                                                          |
| ------------------- | ---------------------------------------------------------------------------------------------------------------------------------------------- |
| Proposed domain     | `PROJECTION`                                                                                                                                   |
| Proposed permanence | Structural / foundational invariant                                                                                                            |
| Source anchors      | ADR-001 S2; `007` unified closure; `008` Core Architectural Closure                                                                            |
| Candidate decision  | Projections derive current meaning and state from durable events and configuration; projections are rebuildable and not source-of-truth facts. |
| Vocabulary affected | `projection`, `read model`, `current state`, `rebuildable`, `derived meaning`                                                                  |
| Possible overlaps   | Overlaps with CAND-EVENT-01 and CAND-WORKFLOW-01.                                                                                              |
| Normalization risk  | Could become part of central invariant rather than standalone record.                                                                          |

### CAND-PROJECTION-02 — Assignment timeline projection

| Field               | Value                                                                                                                                                |
| ------------------- | ---------------------------------------------------------------------------------------------------------------------------------------------------- |
| Proposed domain     | `PROJECTION`                                                                                                                                         |
| Proposed permanence | Structural / Strategy-protecting mixed                                                                                                               |
| Source anchors      | ADR-003 S3; ADR-003 S1/S2                                                                                                                            |
| Candidate decision  | Assignment timeline projection reconstructs actor authority over time and supports authorization validation without storing authority in each event. |
| Vocabulary affected | `assignment timeline`, `authority-as-projection`, `assignment_changed`                                                                               |
| Possible overlaps   | Strong overlap with CAND-AUTH-03.                                                                                                                    |
| Normalization risk  | Likely folds into authority-as-projection decision.                                                                                                  |

### CAND-PROJECTION-03 — Alias table projection

| Field               | Value                                                                            |
| ------------------- | -------------------------------------------------------------------------------- |
| Proposed domain     | `PROJECTION`                                                                     |
| Proposed permanence | Structural                                                                       |
| Source anchors      | ADR-002 S6, S13                                                                  |
| Candidate decision  | Alias mappings are applied in projection after raw-reference conflict detection. |
| Vocabulary affected | `alias table`, `alias mapping`, `transitive closure`, `raw-reference detection`  |
| Possible overlaps   | Strong overlap with identity merge and raw-reference detection.                  |
| Normalization risk  | May be absorbed into identity-resolution interaction rule.                       |

### CAND-PROJECTION-04 — Workflow state projection

| Field               | Value                                                                                                     |
| ------------------- | --------------------------------------------------------------------------------------------------------- |
| Proposed domain     | `PROJECTION`                                                                                              |
| Proposed permanence | Initial strategy                                                                                          |
| Source anchors      | ADR-005 S4, S2                                                                                            |
| Candidate decision  | Workflow state projection derives state from events, pattern definition, config version, and flag status. |
| Vocabulary affected | `workflow state projection`, `projection-derived state machine`, `flag_status`                            |
| Possible overlaps   | Strong overlap with CAND-WORKFLOW-01.                                                                     |
| Normalization risk  | Likely folds into workflow domain.                                                                        |

### CAND-PROJECTION-05 — Reporting and analytics projections are read-side summaries

| Field               | Value                                                                                   |
| ------------------- | --------------------------------------------------------------------------------------- |
| Proposed domain     | `PROJECTION`                                                                            |
| Proposed permanence | Initial strategy / Boundary                                                             |
| Source anchors      | `008` Projection/read-model primitives                                                  |
| Candidate decision  | Reporting and analytics projections are read-side summaries, not source-of-truth facts. |
| Vocabulary affected | `reporting/analytics projections`, `read-side summaries`, source-of-truth facts         |
| Possible overlaps   | Report freshness and aggregate semantics are platform-spec gaps, not settled here.      |
| Normalization risk  | May be too thin unless paired with projection source-of-truth boundary.                 |

---

## 11. Candidate Decisions — `GUARD`

### CAND-GUARD-01 — Runtime services guard invariants before downstream action

| Field               | Value                                                                                                                                          |
| ------------------- | ---------------------------------------------------------------------------------------------------------------------------------------------- |
| Proposed domain     | `GUARD`                                                                                                                                        |
| Proposed permanence | Strategy-protecting                                                                                                                            |
| Source anchors      | `007` unified closure; `008` Core Architectural Closure; ADR-002 S12; ADR-003 S7; ADR-004 S5/S6/S7/S8; ADR-005 S2/S9                           |
| Candidate decision  | Runtime services guard accepted invariants before downstream action, policy execution, projection derivation, trigger emission, or resolution. |
| Vocabulary affected | `runtime service`, `guard invariants`, `detect-before-act`, `Config Package Validator`, `Trigger Engine`, `Auto-resolution Engine`             |
| Possible overlaps   | Umbrella over many service-specific candidates.                                                                                                |
| Normalization risk  | Might be retained as a central invariant or removed as too broad.                                                                              |

### CAND-GUARD-02 — Config package validation before delivery

| Field               | Value                                                                                                                                           |
| ------------------- | ----------------------------------------------------------------------------------------------------------------------------------------------- |
| Proposed domain     | `GUARD`                                                                                                                                         |
| Proposed permanence | Strategy-protecting                                                                                                                             |
| Source anchors      | ADR-004 S6, S7, S8, S12, S13; `008` Strategy-protecting runtime services                                                                        |
| Candidate decision  | Configuration packages must be validated for dependency, budget, version, scope, trigger, sensitivity, and pattern constraints before delivery. |
| Vocabulary affected | `Config Package Validator`, `atomic config delivery`, `complexity budgets`, `server-only triggers`                                              |
| Possible overlaps   | CAND-CONFIG-08 and CAND-CONFIG-11.                                                                                                              |
| Normalization risk  | Could be implementation/tooling unless limited to architecture boundary.                                                                        |

### CAND-GUARD-03 — Scope and sync resolvers preserve authorization boundaries

| Field               | Value                                                                                                                               |
| ------------------- | ----------------------------------------------------------------------------------------------------------------------------------- |
| Proposed domain     | `GUARD`                                                                                                                             |
| Proposed permanence | Strategy-protecting                                                                                                                 |
| Source anchors      | ADR-003 S1/S2/S5; ADR-004 S7                                                                                                        |
| Candidate decision  | Scope and sync resolvers preserve assignment-based access, platform-fixed containment, and sync=access before data reaches devices. |
| Vocabulary affected | `Scope Resolver`, `Sync Scope Resolver`, `scope-containment test`, `sync scope = access scope`                                      |
| Possible overlaps   | AUTH candidates.                                                                                                                    |
| Normalization risk  | Likely folds into auth decisions unless service boundary is needed.                                                                 |

### CAND-GUARD-04 — Command Validator is advisory; server creates transition flags

| Field               | Value                                                                                                                                           |
| ------------------- | ----------------------------------------------------------------------------------------------------------------------------------------------- |
| Proposed domain     | `GUARD`                                                                                                                                         |
| Proposed permanence | Initial strategy                                                                                                                                |
| Source anchors      | ADR-005 S4                                                                                                                                      |
| Candidate decision  | The Command Validator may warn on-device, but transition validity is ultimately handled by server-side flag generation and projection behavior. |
| Vocabulary affected | `Command Validator`, `transition_violation`, advisory validator                                                                                 |
| Possible overlaps   | CAND-WORKFLOW-01 and CAND-WORKFLOW-05.                                                                                                          |
| Normalization risk  | Could be too implementation-adjacent unless kept as advisory/non-rejection boundary.                                                            |

---

## 12. Candidate Decisions — `BOUNDARY`

### CAND-BOUNDARY-01 — Negative boundary register as architecture guard

| Field               | Value                                                                                                                                                    |
| ------------------- | -------------------------------------------------------------------------------------------------------------------------------------------------------- |
| Proposed domain     | `BOUNDARY`                                                                                                                                               |
| Proposed permanence | Strategy-protecting                                                                                                                                      |
| Source anchors      | `008` Negative Boundary Register; `007` rejected alternatives                                                                                            |
| Candidate decision  | Rejected alternatives remain active negative boundary evidence and must not be reintroduced as platform capability without formal architecture decision. |
| Vocabulary affected | `negative boundary`, rejected alternatives, no deployer-authored event types, no authority_context, no current_state, no pattern_ref                     |
| Possible overlaps   | Every domain has local negative boundaries.                                                                                                              |
| Normalization risk  | Could become meta-governance rather than architecture decision.                                                                                          |

### CAND-BOUNDARY-02 — Open evolution register remains open

| Field               | Value                                                                                                                     |
| ------------------- | ------------------------------------------------------------------------------------------------------------------------- |
| Proposed domain     | `BOUNDARY`                                                                                                                |
| Proposed permanence | Initial strategy / Governance                                                                                             |
| Source anchors      | `008` Open Evolution Register                                                                                             |
| Candidate decision  | Open fronts are tracked explicitly and must not be treated as settled architecture until closed through the correct path. |
| Vocabulary affected | `open evolution`, `open front`, platform evolution, underexplored front                                                   |
| Possible overlaps   | Gap routing playbook.                                                                                                     |
| Normalization risk  | More likely belongs in gap routing playbook than decision records.                                                        |

### CAND-BOUNDARY-03 — S00 simplicity baseline

| Field               | Value                                                                                                                                                                                                                                                         |
| ------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| Proposed domain     | `BOUNDARY`                                                                                                                                                                                                                                                    |
| Proposed permanence | Strategy-protecting                                                                                                                                                                                                                                           |
| Source anchors      | `008` S00 Simplicity Check; product scenario S00                                                                                                                                                                                                              |
| Candidate decision  | The simplest structured capture scenario must remain simple and must not require custom event types, access code, triggers, state-machine authoring, pattern refs, authority context, field-level sensitivity, auto-resolution, or workflow flag propagation. |
| Vocabulary affected | `S00 simplicity baseline`, simplest scenario, capture_only                                                                                                                                                                                                    |
| Possible overlaps   | Product/problem evidence and config boundary.                                                                                                                                                                                                                 |
| Normalization risk  | Could become a guardrail rather than decision record.                                                                                                                                                                                                         |

### CAND-BOUNDARY-04 — Implementation mechanisms are not architecture unless mapped to primitive boundary

| Field               | Value                                                                                                                                                                                                |
| ------------------- | ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| Proposed domain     | `BOUNDARY`                                                                                                                                                                                           |
| Proposed permanence | Strategy-protecting / Governance                                                                                                                                                                     |
| Source anchors      | `008` Reading Rules and Implementation Guardrails; `009` Exclusion Rules                                                                                                                             |
| Candidate decision  | Database schemas, APIs, service boundaries, queues, indexes, caches, UI, local storage mechanics, and tooling syntax are not architecture unless explicitly mapped to accepted primitive boundaries. |
| Vocabulary affected | `implementation concern`, `implementation/tooling gap`, `service boundary`, `database schema`, `API shape`                                                                                           |
| Possible overlaps   | Gap routing playbook.                                                                                                                                                                                |
| Normalization risk  | May belong in playbook rather than core decision records.                                                                                                                                            |

---

## 13. Explicitly Excluded From Candidate Decisions

These are not candidate architecture decisions in this pass.

| Excluded item                                    | Reason                                                                              | Likely route later                                                           |
| ------------------------------------------------ | ----------------------------------------------------------------------------------- | ---------------------------------------------------------------------------- |
| Exact Pattern Registry inventory                 | Explicitly open platform-spec/evolution front.                                      | Platform-spec detail gap or platform evolution.                              |
| Pattern skeleton definitions                     | Spec detail under accepted Pattern Registry boundary.                               | Platform-spec detail gap.                                                    |
| Pattern migration mechanics                      | Open evolution / implementation strategy.                                           | Platform evolution or implementation/tooling gap.                            |
| Auditor/query access                             | Underexplored/not settled; may affect sync=access.                                  | Architecture decision gap.                                                   |
| Aggregate access rules beyond event access       | May alter access/sync semantics or leak scoped data.                                | Architecture decision gap or platform-spec detail gap depending on boundary. |
| Report freshness semantics                       | Behavior under projections; not settled architecture.                               | Platform-spec detail gap.                                                    |
| Flag-aware aggregate reporting semantics         | Behavior under projections; not settled architecture.                               | Platform-spec detail gap.                                                    |
| Handoff package contents                         | Spec detail under auth/sync/projection boundaries.                                  | Platform-spec detail gap.                                                    |
| Setup approval lifecycle                         | Product/spec behavior under config boundary.                                        | Platform-spec detail gap.                                                    |
| Retention windows                                | Human/regulatory policy unless it changes event/source-truth boundary.              | Operational policy gap.                                                      |
| Worker exit/offboarding procedure                | Operational process unless it changes sync/access or device-retention architecture. | Operational policy gap.                                                      |
| Shared-device storage partitioning               | Implementation/security design.                                                     | Implementation/tooling gap.                                                  |
| Role-action table artifact shape                 | Open artifact shape; access primitive settled, exact artifact not.                  | Platform-spec detail gap or implementation/tooling gap.                      |
| Config authoring syntax                          | Explicit implementation/tooling concern.                                            | Implementation/tooling gap.                                                  |
| Database schema / indexes / queues / APIs / SDKs | Implementation mechanics.                                                           | Implementation/tooling gap.                                                  |
| SME validation / scenario thickening             | Product/problem evidence, not architecture.                                         | Product/problem evidence gap.                                                |

---

## 14. Candidate Overlap Hotspots for Pass 2

Pass 2 should pay special attention to these overlap areas.

### 14.1 Event source-of-truth vs projection derivation

Candidates involved:

* CAND-EVENT-01
* CAND-EVENT-02
* CAND-PROJECTION-01
* CAND-WORKFLOW-01

Likely normalization question:

```txt
Should the central invariant become one decision, or should event source-of-truth and projection derivation remain separate decisions?
```

### 14.2 Event envelope vs field-specific decisions

Candidates involved:

* CAND-EVENT-04
* CAND-EVENT-05
* CAND-CONFIG-01
* CAND-CONFIG-02
* CAND-IDENTITY-03
* CAND-CONFIG-03

Likely normalization question:

```txt
Should final envelope be a summary decision that references field-owner decisions, or should it own all field semantics?
```

### 14.3 Assignment as identity vs authorization grant

Candidates involved:

* CAND-IDENTITY-02
* CAND-AUTH-01
* CAND-AUTH-03
* CAND-PROJECTION-02

Likely normalization question:

```txt
How do we preserve the term collision without duplicating the decision?
```

### 14.4 Raw-reference / alias / authorization interaction

Candidates involved:

* CAND-IDENTITY-05
* CAND-CONFLICT-02
* CAND-AUTH-04
* CAND-PROJECTION-03

Likely normalization question:

```txt
Should this become one cross-domain identity-resolution interaction decision?
```

### 14.5 Detect-before-act as conflict rule vs runtime guard

Candidates involved:

* CAND-CONFLICT-03
* CAND-GUARD-01
* CAND-WORKFLOW-06
* CAND-CONFIG-07

Likely normalization question:

```txt
Should detect-before-act remain under CONFLICT, or become the central GUARD decision?
```

### 14.6 Configuration gradient vs sub-boundaries

Candidates involved:

* CAND-CONFIG-05
* CAND-CONFIG-06
* CAND-CONFIG-07
* CAND-CONFIG-11
* CAND-CONFIG-12

Likely normalization question:

```txt
Should the gradient be one high-level decision plus specific child records, or only specific boundary decisions?
```

### 14.7 Pattern Registry vs exact pattern inventory

Candidates involved:

* CAND-WORKFLOW-03
* CAND-WORKFLOW-04
* excluded Pattern Registry inventory

Likely normalization question:

```txt
How do we anchor the pattern boundary without freezing the skeleton inventory?
```

### 14.8 Boundary/governance records

Candidates involved:

* CAND-BOUNDARY-01
* CAND-BOUNDARY-02
* CAND-BOUNDARY-03
* CAND-BOUNDARY-04

Likely normalization question:

```txt
Which of these belong in final decision records, and which belong only in the gap routing playbook?
```

---

## 15. Pass 1 Quality Gate Check

| Gate item from charter                           | Status                                               |
| ------------------------------------------------ | ---------------------------------------------------- |
| Every candidate has a source anchor.             | Satisfied at candidate level.                        |
| Every candidate has one proposed primary domain. | Satisfied.                                           |
| Every candidate has proposed permanence.         | Satisfied, with mixed-permanence notes where needed. |
| Every candidate lists vocabulary affected.       | Satisfied.                                           |
| Overlaps are explicitly marked.                  | Satisfied.                                           |
| No candidate is presented as final.              | Satisfied.                                           |

---

## 16. Handoff Capsule

* This pass produced:

  * Candidate architecture decision inventory.
  * 57 provisional candidate anchors.
  * Candidate source anchors.
  * Proposed domains.
  * Proposed permanence classifications.
  * Vocabulary affected per candidate.
  * Overlap and normalization-risk notes.
  * Excluded non-candidate items.
  * Overlap hotspots for Pass 2.
* Stable items for next pass:

  * Candidate list.
  * Explicit exclusions.
  * Overlap hotspots.
  * Pass 1 quality gate result.
* Items not yet stable:

  * Final DEC IDs.
  * Final decision count.
  * Final decision names.
  * Final decision domains.
  * Final permanence classifications where mixed.
  * Owns / does-not-own boundaries.
  * Escalation triggers.
  * Downstream consumer mapping.
* Required next input:

  * `002-phase0-decision-register.md`
  * `007-phase5-cross-lineage-vocabulary.md`
  * `008-authoritative-architecture-map.md`
  * `009-decision-anchor-extraction-charter.md`
  * `010-candidate-architecture-decision-inventory.md`
* Known risks:

  * Too many candidates if field-level and envelope-level decisions are both retained.
  * Duplicate treatment of projections across `EVENT`, `WORKFLOW`, and `PROJECTION`.
  * Duplicate treatment of authorization across `IDENTITY`, `AUTH`, and `PROJECTION`.
  * Mixed permanence candidates may need splitting.
  * Boundary/governance candidates may belong in the gap routing playbook rather than final records.
* Do not reinterpret:

  * Candidate IDs are not final decision IDs.
  * Candidate presence does not mean the item is accepted as a standalone decision record.
  * Excluded items remain excluded from settled architecture unless later closed through the correct path.
  * This pass does not route gaps.
* Next pass should start from:

  * `011-core-architecture-decision-records.md`
  * Scope: normalize candidate granularity and write final decision records.
  * Pass 2 may merge, split, remove, or rename any candidate.
