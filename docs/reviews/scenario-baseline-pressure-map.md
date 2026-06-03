# Scenario-To-Baseline Pressure Map

Status: review/map for NW-023

Authority: none. This document does not change architecture, contracts, BAR status, or backlog status.

## 2026-06-03 Execution Update

The first-wave probe set recommended by this map has now executed:

| Probe | Backlog row | Current standing |
|---|---|---|
| S19 - offline/stale authority | NW-025 | `accepted`; runtime probe proves stale structurally valid work is persisted and flagged while live sync and subject-history remain separate. |
| S00 - structured capture | NW-026 | `accepted`; runtime probe proves append-only correction, idempotent replay, server-watermark pull ordering, and existing concurrency flag mechanics. |
| S21 - supervisor review | NW-029 | `accepted`; runtime probe proves scoped supervisor visibility, `capture_with_review/v1` state, unresolved/non-designated flagged review exclusion, and exact resolver re-inclusion. |
| S27 - logistics transfer | NW-030 | `accepted`; runtime probe proves non-health `transfer_with_acknowledgment/v1` state, scoped sync, manual discrepancy review, exact resolver handling, and out-of-order accept-and-flag behavior. |

The original map remains useful as pressure/provenance. It should no longer be read as saying S00/S19/S21/S23/S27 are unexecuted or gated. BAR-010 config package delivery and NW-032/S23 setup-config runtime evidence are now accepted; S26 reporting remains a ready-with-constraints later candidate when explicitly selected.

## 1. Purpose And Method

This map triages scenario pressure against the current post-Phase-4 baseline, deferred/future-decision surfaces, implementation-facing contracts, and near-term verification work. Scenarios remain problem-space inputs; they are not implementation specifications and do not prove runtime evidence by themselves.

Method:

- Read the active working-surface packet: `AGENTS.md`, the current routing section of `docs/status.md`, `docs/agent-working-surface/README.md`, this review protocol, scenario index, access-control scenario, BAR, backlog, viability closure review, and behavioral pattern catalog.
- Read each relevant scenario file individually: S00-S16, S18, and S19-S27.
- Slice CDL only where scenario rows needed authority clarification, especially CDL-006, CDL-010, CDL-030, CDL-031, CDL-037, CDL-038, CDL-042, CDL-043, CDL-047, CDL-048, CDL-054, and CDL-055.
- Check the process-boundary contracts touched by selected probes: envelope, sync, flag catalog, platform payload shapes, pattern definition, bundled patterns, and shared fixture surfaces.

Evidence levels are intentionally separated:

- Scenario rows start at `scenario_pressure`.
- Architecture support comes from CDL rows only.
- Contract support is `contract_defined` when a contract surface names the shape, protocol, flag, or pattern boundary.
- BAR evidence is current baseline standing, not runtime scenario evidence.
- Recommended probes target future `runtime_scenario_evidence`; this map does not claim that evidence exists.

Pressure labels used below: `accepted_baseline_pressure`, `candidate_baseline_pressure`, `deferred_surface_pressure`, `future_decision_pressure`, `contract_pressure`, `runtime_probe_candidate`, and `documentation_only_pressure`.

## 2. Scenario Triage Table

| Scenario | Class | Primary pressure | BAR rows | Contracts touched | Deferred/future surfaces | Probe viability | Probe value | Notes |
|---|---|---|---|---|---|---|---|---|
| S00 - Recording Structured Information | `simple_baseline` | Basic capture, shape interpretation, append-only correction, duplicate/concurrent capture | BAR-001, BAR-002, BAR-005, BAR-010, BAR-013 | envelope; sync; shape validation; flag catalog | none if correction stays event-based | `ready_now` | high | `accepted_baseline_pressure`, `candidate_baseline_pressure`, `contract_pressure`, `runtime_probe_candidate`; simplest probe for trustworthy records without needing workflow or deferred behavior. |
| S01 - Recording Information About a Specific Thing | `simple_baseline` | Subject linkage, subject history, duplicate identity, ambiguous identity | BAR-003, BAR-004, BAR-009, BAR-014 | envelope; sync subject-history; identity shapes; fixtures | BAR-105 if it becomes full registry lifecycle | `ready_with_constraints` | high | `candidate_baseline_pressure`, `contract_pressure`; good identity/projection pressure if limited to merge/split and alias projection, not registry lifecycle. |
| S02 - Regular, Recurring Reporting | `baseline_composite` | Cadence, expected reporting gaps, responsibility by period | BAR-007, BAR-010, BAR-011, BAR-012, BAR-014 | envelope; config package; expressions; patterns/fixtures if modeled as pattern state | BAR-101 for automatic missed-period response | `verification_needed_first` | medium | `candidate_baseline_pressure`, `deferred_surface_pressure`; useful after config/pattern verification, but automatic gap action must remain out of scope. |
| S03 - Designated Responsibility | `simple_baseline` | Actor-to-scope accountability and assignment-derived access | BAR-003, BAR-007 | sync; assignment_created/v1; assignment_ended/v1 | BAR-108 if it asks for custom scope logic | `ready_now` | high | `accepted_baseline_pressure`, `contract_pressure`, `runtime_probe_candidate`; clean probe for active assignment, scoped pull, and actor-bound command authority. |
| S04 - Review and Judgment by Another Person | `baseline_composite` | Review decision, pending state, reviewer authority, flag visibility | BAR-006, BAR-007, BAR-012, BAR-013, BAR-014 | flag catalog; capture_with_review/v1; pattern-definition; fixtures | BAR-101 for overdue review automation; BAR-103 for resolver reassignment | `ready_with_constraints` | high | `candidate_baseline_pressure`, `contract_pressure`, `runtime_probe_candidate`; viable if review is human-authored and overdue handling is reported, not automatically triggered. |
| S05 - Periodic Visits and In-Person Assessment | `baseline_composite` | Planned visits, structured assessment, late/missed visits, follow-up visibility | BAR-007, BAR-010, BAR-011, BAR-012, BAR-013, BAR-014 | envelope; config package; expressions; capture_with_review/v1; flag catalog | BAR-101 for automatic late/missed-visit responses | `verification_needed_first` | medium | `candidate_baseline_pressure`, `deferred_surface_pressure`; good later composition of S02 and S04 after NW-009/NW-010. |
| S06 - Maintaining a Known Set of Things / Shape Evolution | `future_decision_pressure` | Registry lifecycle, deactivation, bulk changes, mutable subject set, shape evolution | BAR-009, BAR-010, BAR-015; BAR-105 | identity shapes; envelope; sync; config package | BAR-105; BAR-107 if solved by new envelope fields/types | `defer` | low | `deferred_surface_pressure`, `future_decision_pressure`, `documentation_only_pressure`; shape-version coexistence is viable, but entity lifecycle remains deferred. |
| S07 - Handing Things Off and Confirming Receipt | `baseline_composite` | Transfer, receipt, discrepancy, single or chained handoff | BAR-006, BAR-007, BAR-012, BAR-013, BAR-014 | transfer_with_acknowledgment/v1; flag catalog; pattern fixtures | BAR-102 if discrepancy is auto-resolved | `ready_with_constraints` | high | `candidate_baseline_pressure`, `contract_pressure`, `runtime_probe_candidate`; strong pattern-state probe if discrepancy resolution stays manual. |
| S08 - Following Something Over Time Until It Is Resolved | `baseline_composite` | Long-running active situation, interactions, transfers, closure review | BAR-006, BAR-007, BAR-009, BAR-012, BAR-013, BAR-014 | ongoing_resolution/v1; assignment shapes; flag catalog; fixtures | BAR-103 if resolver reassignment is required; BAR-105 if subject lifecycle is required | `verification_needed_first` | medium | `candidate_baseline_pressure`, `contract_pressure`; valuable after pattern/projection verification, but not a first probe because it combines many axes. |
| S09 - Planned, Time-Bound Effort Across Many Places | `baseline_composite` | Campaign planning, assignments, progress visibility, staged work | BAR-003, BAR-007, BAR-010, BAR-011, BAR-012, BAR-014 | sync; assignment shapes; config package; patterns/fixtures | BAR-101 for automatic gap intervention; BAR-108 for custom campaign scope mechanisms | `ready_with_constraints` | medium | `accepted_baseline_pressure`, `candidate_baseline_pressure`; viable as scoped assignment/progress reporting if automatic intervention is excluded. |
| S10 - Work That Depends on Changing Conditions | `future_decision_pressure` | Condition-derived targeting and changing attention set | BAR-011, BAR-012, BAR-014; BAR-101 | expressions; pattern projections; fixtures | BAR-101; BAR-108 if dynamic scope is introduced | `successor_decision_required` | low | `deferred_surface_pressure`, `future_decision_pressure`, `documentation_only_pressure`; condition detection can inform reporting, but generated work requires successor trigger policy. |
| S11 - Multi-Step Approval | `baseline_composite` | Sequential review, dynamic reviewer authority, approval chain traceability | BAR-006, BAR-007, BAR-012, BAR-013, BAR-014 | multi_step_approval/v1; flag catalog; pattern-definition; fixtures | BAR-101 for overdue escalation; BAR-103 for resolver reassignment | `verification_needed_first` | medium | `candidate_baseline_pressure`, `contract_pressure`; useful after pattern-state projection acceptance because it stresses level-based approval. |
| S12 - Event-Triggered Actions | `future_decision_pressure` | Consequential response to observation, delayed response under offline sync, escalation | BAR-006, BAR-012, BAR-013, BAR-014; BAR-101 | flag catalog; pattern projections; sync | BAR-101; BAR-102 if response cleanup is automatic | `successor_decision_required` | medium | `deferred_surface_pressure`, `future_decision_pressure`, `documentation_only_pressure`; S12 remains active scenario pressure, but general trigger execution is deferred and future execution is server-side L3 only under CDL-042. |
| S13 - Cross-Flow Linking | `baseline_composite` | Independent activities connected by shared subject, activity, or payload reference | BAR-009, BAR-010, BAR-014 | envelope activity_ref/subject_ref; deployer shapes; fixtures | BAR-107 if solved by a new structural reference field | `verification_needed_first` | medium | `candidate_baseline_pressure`, `contract_pressure`; viable only when links live in payload/config/projection, not as new envelope fields. |
| S14 - Multi-Level Distribution | `baseline_composite` | Multi-hop custody, traceability, partial handoffs, hierarchical visibility | BAR-003, BAR-006, BAR-007, BAR-012, BAR-013, BAR-014 | sync; assignment shapes; transfer_with_acknowledgment/v1; flag catalog | BAR-102 if discrepancies are auto-resolved; BAR-108 if custom hierarchy scope is requested | `ready_with_constraints` | high | `accepted_baseline_pressure`, `candidate_baseline_pressure`, `contract_pressure`; overlaps S27 but with broader hierarchy pressure. |
| S15 - Cross-Program Overlays | `phase2_pressure` | Multiple stakeholder lenses over shared records and aggregate views | BAR-007, BAR-014; potential BAR-106, BAR-108 | sync/access scopes; reporting/projection fixtures if added later | BAR-106; BAR-108; NW-018 deferred | `defer` | low | `deferred_surface_pressure`, `documentation_only_pressure`; keep as deferred extension and do not promote multi-audience views into current baseline. |
| S16 - Emergency Rapid Response | `phase2_pressure` | Urgent temporary authority, fast communication, evolving crisis scope | BAR-007; potential BAR-101, BAR-104, BAR-108 | sync/access scopes; assignment shapes | BAR-101; BAR-104; BAR-108; NW-019 deferred | `defer` | low | `deferred_surface_pressure`, `future_decision_pressure`, `documentation_only_pressure`; emergency override and production identity-provider authority are not current implementation scope. |
| S18 - Advanced Analytics-Derived Flows | `phase2_pressure` | Analysis-derived work initiation and feedback into future detection | BAR-011, BAR-012, BAR-014; BAR-101 | expressions/projections only as reporting inputs; no current analytics contract | BAR-101; possible reporting warehouse future seam; NW-020 deferred | `successor_decision_required` | low | `future_decision_pressure`, `documentation_only_pressure`; analytics-derived initiation is future platform evolution, not ordinary trigger/config behavior. |
| S19 - Working Without Connectivity | `baseline_composite` | Offline capture, stale central state, reconciliation, causal ordering | BAR-002, BAR-003, BAR-004, BAR-007, BAR-008, BAR-013, BAR-014 | envelope; sync; subject-history; assignment shapes; flag catalog; fixtures | BAR-106 if sensitive local purge policy becomes field-level security; BAR-108 for custom scope | `ready_with_constraints` | high | `accepted_baseline_pressure`, `candidate_baseline_pressure`, `contract_pressure`, `runtime_probe_candidate`; best cross-cutting probe for offline-first baseline without adding new mechanisms. |
| S20 - CHV Field Operations | `baseline_composite` | Field capture, subject linkage, supplies, continuous responsibility | BAR-001, BAR-002, BAR-003, BAR-007, BAR-009, BAR-012, BAR-013, BAR-014 | envelope; sync; identity shapes; assignment shapes; transfer/ongoing patterns | BAR-105 if patient/facility lifecycle is promoted | `ready_with_constraints` | medium | `candidate_baseline_pressure`; good domain composite, but S21 gives a sharper review/authority probe and S27 gives cleaner non-health distribution. |
| S21 - CHV Supervisor Operations | `baseline_composite` | Supervisor assessment, review authority, point-in-time judgment, scoped visibility | BAR-006, BAR-007, BAR-012, BAR-013, BAR-014 | capture_with_review/v1; flag catalog; sync; pattern fixtures | BAR-101 for overdue visit/review automation; BAR-104 for production auth claims | `ready_with_constraints` | high | `candidate_baseline_pressure`, `contract_pressure`, `runtime_probe_candidate`; strong assignment/review probe if it stays human-reviewed and assignment-scoped. |
| S22 - Coordinated Distribution Campaign Across Grouped Locations | `baseline_composite` | Campaign plus unit-level work, reassignment, supply flow, offline duplicate units | BAR-003, BAR-004, BAR-006, BAR-007, BAR-012, BAR-013, BAR-014 | sync; subject-history; assignment shapes; transfer pattern; flag catalog | BAR-101 for automatic progress follow-up; BAR-105 for newly discovered unit lifecycle | `ready_with_constraints` | high | `accepted_baseline_pressure`, `candidate_baseline_pressure`; high scenario value, but initial probe should constrain discovered units to capture/linkage rather than full registry lifecycle. |
| S23 - Configure New Operational Activity | `baseline_composite` | Setup without custom development, deploy-time validation, version coexistence, bounded warnings | BAR-010, BAR-011, BAR-012, BAR-014 | config package specs; pattern-definition; patterns; expression fixtures | EH-005 if expression functions are needed; BAR-101 for L3 policy execution | `accepted_runtime_probe` | high | `accepted_baseline_pressure`, `contract_pressure`; NW-032 accepted bounded setup/config runtime evidence because setup mistakes must fail before devices depend on them. |
| S24 - Long-Running Deployment Data Lifecycle | `baseline_composite` | Sync versus retention, audit reconstruction, scope contraction, local purge | BAR-003, BAR-004, BAR-007, BAR-008, BAR-014; BAR-106 | sync; subject-history; retention/mobile store behavior; fixtures | BAR-106; reporting/audit surfaces beyond normal field sync | `ready_with_constraints` | medium | `candidate_baseline_pressure`, `future_decision_pressure`; selective retention can be probed now, but field-level sensitivity/encryption/redaction needs successor decision. |
| S25 - Worker Onboarding, Transfer, Leave, and Exit | `baseline_composite` | Assignment changes, stale offline authority, handoff history, exit retention | BAR-003, BAR-004, BAR-006, BAR-007, BAR-008, BAR-014 | sync; subject-history; assignment shapes; flag catalog | BAR-104 for production provider authority; BAR-106 for sensitive exit purge detail | `ready_with_constraints` | high | `accepted_baseline_pressure`, `candidate_baseline_pressure`, `contract_pressure`; strong authority/stale-work probe after S19, provided identity-provider groups/claims are not treated as authority. |
| S26 - Operational Reporting and Aggregate Oversight | `baseline_composite` | Aggregate freshness, unresolved flag treatment, scoped views, drill-back traceability | BAR-006, BAR-007, BAR-012, BAR-013, BAR-014 | flag catalog; sync/access scopes; projection fixtures | reporting warehouse future seam if separate analytics storage is required | `ready_with_constraints` | medium | `accepted_baseline_pressure`; good reporting probe after projection/integrity verification, with freshness and exclusion semantics visible. |
| S27 - Logistics Distribution Composite | `baseline_composite` | Non-health custody chain, partial receipt, discrepancy review, domain-agnostic pattern proof | BAR-006, BAR-007, BAR-012, BAR-013, BAR-014 | transfer_with_acknowledgment/v1; flag catalog; assignment shapes; pattern fixtures | BAR-102 if discrepancy cleanup is automatic; BAR-108 for custom custody scope | `ready_with_constraints` | high | `candidate_baseline_pressure`, `contract_pressure`, `runtime_probe_candidate`; best near-term non-health proof if discrepancy review remains manual and pattern-derived. |

## 3. Recommended 2-4 Scenario Probes

| Rank | Scenario/probe | Why this one | BAR rows pressured | Forbidden scope | Expected evidence |
|---|---|---|---|---|---|
| 1 | S00 - Structured capture with offline correction and duplicate detection | Establishes the simplest end-to-end trustworthy-record baseline before composite probes. | BAR-001, BAR-002, BAR-005, BAR-010, BAR-013 | No new envelope fields/types; no trigger, workflow, or entity lifecycle requirement. | `runtime_scenario_evidence` for valid envelope/payload acceptance, append-only correction, idempotent sync, and any duplicate/concurrency flag behavior. |
| 2 | S19 - Offline capture, stale assignment, and scoped reconciliation | Directly pressures the core offline-first promise and accepted sync/authority baseline while exposing mobile/projection candidate risk. | BAR-002, BAR-003, BAR-004, BAR-007, BAR-008, BAR-013, BAR-014 | No production OIDC/JWT/Keycloak authority, no new scope mechanisms, no field-level sensitivity design beyond current retention policy. | `runtime_scenario_evidence` for push/pull ordering, subject-history isolation, assignment-scoped pull, stale-authority flags, and selective retention behavior. |
| 3 | S21 - Supervisor visit and assessment with scoped review | Gives the assignment/responsibility/review probe with clear real-world actors and a bounded review pattern. | BAR-006, BAR-007, BAR-012, BAR-013, BAR-014 | No general trigger execution for late visits, no auto-resolution, no resolver reassignment, no production auth claims. | `runtime_scenario_evidence` for capture_with_review projection, reviewer authority, unresolved-flag exclusion, and scoped visibility. |
| 4 | S27 - Logistics distribution with partial receipt and discrepancy review | Provides the non-health/domain-agnostic proof while reusing the transfer pattern and manual discrepancy review. | BAR-006, BAR-007, BAR-012, BAR-013, BAR-014 | No auto-resolution of discrepancies, no new envelope fields/types, no custom scope/custody mechanism. | `runtime_scenario_evidence` for transfer_with_acknowledgment states, discrepancy flags/resolution by exact designated resolver, scoped sync, and projection equivalence. |

These probes deliberately avoid S12/S18 trigger-derived work, S06 entity lifecycle, S15/S16 deferred extensions, and any authority path that depends on production identity-provider claims.

## 4. Selected Scenario Walkthroughs

### Probe 1 - S00 Structured Capture With Offline Correction

**Scenario**

| Field | Value |
|---|---|
| id | S00 |
| title | Recording Structured Information |
| real_world_goal | A field worker records a known set of details, later corrects or amends the record, and the platform preserves both original and correction without losing traceability. |
| scenario_class | `simple_baseline` |
| primary_pressure | Event envelope closure, payload shape interpretation, append-only persistence, idempotent sync, and duplicate/concurrency flagging. |
| candidate_probe_value | high |

**Actors**

| Role | Responsibility | Current authority model | Expected scope |
|---|---|---|---|
| Field worker | Capture the original structured record and any correction while offline or online. | Assignment-derived authority when scoped work is required; otherwise actor attribution through envelope. | Assigned activity/location/subject, if the activity is scoped. |
| Supervisor or data steward | Review questionable duplicate/correction flags if emitted. | Human resolver only when designated by flag routing. | Scope that contains the source record or configured steward role. |

**Subjects or real-world things**

| Description | Identity risk | Location or scope | Lifecycle pressure |
|---|---|---|---|
| A site, household, equipment item, or report target. | Duplicate captures may point to the same real-world thing with different payload details. | Optional geographic/activity scope. | None for the probe; do not model creation/deactivation as S06 lifecycle. |

**Offline windows**

| Actor | Duration or condition | Stale risks |
|---|---|---|
| Field worker | Hours or days without connectivity. | Correction may arrive after another correction or duplicate record; central ordering follows sync watermark, while device sequence preserves local order. |

**Activities and config**

| Activity or work | Shapes or payloads | Expressions | Pattern binding | Config pressure |
|---|---|---|---|---|
| Basic structured capture | A deployer-defined capture shape such as `site_survey/v1`, with optional later `site_survey/v2`. | L2 warnings only if needed; no functions or scripts. | None required. | Shape version coexistence and deploy-time validation can be included if BAR-010 is under test. |

**Expected events**

| Type | shape_ref | subject_ref | actor_ref | activity_ref | Notes |
|---|---|---|---|---|---|
| `capture` | deployer capture shape, for example `site_survey/v1` | Stable subject ref or activity-level subject | Field worker | Optional activity ref | Original record; must use existing 11-field envelope. |
| `capture` | deployer correction/amendment shape or same capture shape with correction semantics | Same subject | Field worker | Same activity | Correction is another event, not mutation of the original event. |
| `alert` | `conflict_detected/v1` | Source event subject | System actor | Same or null | Only if duplicate/concurrent/domain uniqueness detector finds a configured anomaly. |
| `review` | `conflict_resolved/v1` | Flag/source subject | Designated resolver | Same or null | Optional manual resolution by exact resolver. |

**Expected flags**

| Category | Reason | Severity | Resolvability | Resolver route |
|---|---|---|---|---|
| `concurrent_state_change` | Another device changed the same subject after the worker's last known watermark. | blocking | `manual_only` | Source-event human resolver per flag catalog. |
| `domain_uniqueness_violation` | A configured uniqueness rule detects duplicate business identity. | blocking | `manual_only` | Domain/activity data steward. |

**Sync, projection, and retention**

| Area | Expectation |
|---|---|
| live_sync_expectation | Push is idempotent by event id; pull is ordered by sync watermark; invalid structure fails before persistence. |
| subject_history_expectation | Not required unless the activity later becomes subject-history sensitive. |
| projection_expectation | Current view is derived from events; unresolved flagged source events are excluded from authoritative projection where applicable. |
| local_retention_expectation | The worker's own events remain retained for provenance under current policy. |

**Deferred or future surfaces**

| Surface | Reason | Route |
|---|---|---|
| New envelope fields/types | The current envelope already carries shape, subject, actor, device, causality, time, and payload. | BAR-107 successor decision only. |
| S06/entity lifecycle | This probe records about a thing; it does not maintain the registry of things. | BAR-105/NW-021 if product explicitly selects it. |

### Probe 2 - S19 Offline Capture, Stale Assignment, And Scoped Reconciliation

**Scenario**

| Field | Value |
|---|---|
| id | S19 |
| title | Working Without Connectivity |
| real_world_goal | Field work continues offline, then reconnects against central state that may have changed while preserving what happened and surfacing conflicts. |
| scenario_class | `baseline_composite` |
| primary_pressure | Offline event creation, sync ordering, assignment-scoped pull, stale authority detection, subject-history backfill, and retention boundaries. |
| candidate_probe_value | high |

**Actors**

| Role | Responsibility | Current authority model | Expected scope |
|---|---|---|---|
| Field worker | Capture assigned work offline using last-known configuration and assignments. | Active assignment projection known at device; server re-evaluates current authority on sync. | Geographic, subject-list, and activity axes as configured. |
| Supervisor | Changes assignment or reviews stale/conflicting work after sync. | Assignment-derived supervisor/steward role; exact designated resolver for flags. | Scope containing worker, subject, and activity. |
| Server integrity pipeline | Accept structurally valid events, then flag anomalies. | Platform code, not deployer config. | Evaluates persisted events and current projections. |

**Subjects or real-world things**

| Description | Identity risk | Location or scope | Lifecycle pressure |
|---|---|---|---|
| A subject assigned to the worker before going offline. | Duplicate or conflicting records may arrive from another device. | Initially in worker scope, then possibly reassigned or removed centrally. | None; do not deactivate or mutate the subject registry in this probe. |

**Offline windows**

| Actor | Duration or condition | Stale risks |
|---|---|---|
| Field worker | Works offline after last pull watermark. | Assignment may end, role may change, or another actor may record against the same subject. |
| Supervisor or coordinator | Online while field worker is offline. | Central assignment changes may not reach the device until later sync. |

**Activities and config**

| Activity or work | Shapes or payloads | Expressions | Pattern binding | Config pressure |
|---|---|---|---|---|
| Scoped field capture | Deployer capture shapes plus platform assignment shapes. | Optional L2 form checks only. | Optional, but the first probe should not depend on complex workflow. | Config version may differ between devices; old work remains interpretable. |

**Expected events**

| Type | shape_ref | subject_ref | actor_ref | activity_ref | Notes |
|---|---|---|---|---|---|
| `assignment_changed` | `assignment_created/v1` | Assignment ref | Supervisor/coordinator | null or assignment activity | Grants initial worker scope. |
| `capture` | deployer capture shape | Assigned subject | Field worker | Activity under assignment | Created offline with device sequence and stale/old sync watermark. |
| `assignment_changed` | `assignment_ended/v1` | Assignment ref | Supervisor/coordinator | null or assignment activity | Ends or narrows authority while worker is offline. |
| `alert` | `conflict_detected/v1` | Source event subject | System actor | Source activity | Emitted after persistence if stale authority, role, scope, or concurrency anomaly is found. |

**Expected flags**

| Category | Reason | Severity | Resolvability | Resolver route |
|---|---|---|---|---|
| `temporal_authority_expired` | Work was done after a previously valid assignment expired centrally. | informational | `auto_eligible` | Human steward unless an explicitly active future auto policy designates a system actor. |
| `role_stale` | Work used a role/action pairing no longer valid at server evaluation. | blocking | `manual_only` | Activity supervisor or role-authority resolver. |
| `scope_violation` | Current assignment scope does not contain the subject/activity. | blocking | `manual_only` | Nearest common human steward. |
| `concurrent_state_change` | Another device changed the subject after the worker's last known watermark. | blocking | `manual_only` | Source-event human resolver. |

**Sync, projection, and retention**

| Area | Expectation |
|---|---|
| live_sync_expectation | Normal pull remains actor-scoped and watermark-based; push accepts structurally valid events before detector flags. |
| subject_history_expectation | Subject-history backfill uses its own cursor and must not mutate normal live-sync watermark. |
| projection_expectation | Authority and state are projections; unresolved flagged source events are excluded from relevant derived state. |
| local_retention_expectation | Own events remain for provenance; other actors' now-out-of-scope events are subject to selective retention policy. |

**Deferred or future surfaces**

| Surface | Reason | Route |
|---|---|---|
| Production OIDC/JWT/Keycloak authority | This probe uses current actor/assignment authority, not provider groups or claims as authority. | BAR-104/FP-011 successor auth decision. |
| New scope mechanisms | CDL-055 closes current scope mechanisms to geographic, subject_list, and activity. | BAR-108 successor decision. |
| Field-level sensitivity | Retention can be tested at current policy level, but encryption/redaction semantics are future. | BAR-106 successor security/platform decision. |

### Probe 3 - S21 Supervisor Visit And Scoped Review

**Scenario**

| Field | Value |
|---|---|
| id | S21 |
| title | Supervisor Visit and Assessment |
| real_world_goal | A supervisor visits a CHV, reviews recent work, records structured assessment and notes, and questionable work remains visible for follow-up. |
| scenario_class | `baseline_composite` |
| primary_pressure | Review authority, capture_with_review projection, scoped visibility, flag exclusion, and offline supervisor work. |
| candidate_probe_value | high |

**Actors**

| Role | Responsibility | Current authority model | Expected scope |
|---|---|---|---|
| CHV/field worker | Create the work being assessed. | Assignment-derived field role. | Assigned community, subjects, and activities. |
| Supervisor | Review CHV work and record assessment. | Assignment-derived reviewer/supervisor role; server-side role-action check. | Supervisor scope includes the CHV's work and review activity. |
| Resolver/steward | Resolve any flagged review or source events. | Exact designated resolver only. | Scope determined by flag routing. |

**Subjects or real-world things**

| Description | Identity risk | Location or scope | Lifecycle pressure |
|---|---|---|---|
| CHV work records, related subjects, and point-in-time supervisor visit. | Review may reference records that changed or synced late. | Supervisor's team/geography/activity scope. | None for the probe; CHV onboarding/exiting belongs to S25. |

**Offline windows**

| Actor | Duration or condition | Stale risks |
|---|---|---|
| Supervisor | Conducts visit offline and syncs later. | CHV may sync additional work, or supervisor assignment may change before review arrives centrally. |
| CHV | May have unsynced work during supervisor visit. | Supervisor's point-in-time assessment can be based on incomplete local view. |

**Activities and config**

| Activity or work | Shapes or payloads | Expressions | Pattern binding | Config pressure |
|---|---|---|---|---|
| Supervisor assessment | Assessment capture shape plus review decision shape. | L2 completeness/warning checks only. | `capture_with_review/v1` for source work or assessment review, depending on activity design. | Pattern binding must be platform-owned definition, deployer-selected and validated. |

**Expected events**

| Type | shape_ref | subject_ref | actor_ref | activity_ref | Notes |
|---|---|---|---|---|---|
| `capture` | CHV work shape | Work subject | CHV | CHV activity | Source work under review. |
| `capture` | supervisor assessment shape | CHV, work subject, or visit subject by activity design | Supervisor | Supervisor visit activity | Structured observation and notes. |
| `review` | review decision shape | Source event/subject per pattern composition | Supervisor | Review activity | Accept/return/question decision under `capture_with_review/v1`. |
| `alert` | `conflict_detected/v1` | Source event subject | System actor | Source activity | Transition, authority, or concurrency flag if rules are violated. |

**Expected flags**

| Category | Reason | Severity | Resolvability | Resolver route |
|---|---|---|---|---|
| `transition_violation` | Review arrives without an active pending-review instance or invalid transition order. | informational | `auto_eligible` | Workflow/activity supervisor unless future auto policy designates system actor. |
| `scope_violation` | Supervisor reviews work outside current assignment scope. | blocking | `manual_only` | Nearest common human steward. |
| `role_stale` | Supervisor role/action authority changed before server evaluation. | blocking | `manual_only` | Activity supervisor/role resolver. |
| `concurrent_state_change` | Source work changed after supervisor's last known watermark. | blocking | `manual_only` | Source-event human resolver. |

**Sync, projection, and retention**

| Area | Expectation |
|---|---|
| live_sync_expectation | Supervisor receives only scoped CHV/source data; offline review pushes later and is evaluated centrally. |
| subject_history_expectation | If supervisor is newly assigned to the CHV/subject, subject-history backfill supplies authorized past activity history without rewriting live watermark. |
| projection_expectation | Review state is derived from pattern rules; invalid transitions produce flags rather than rejected events. |
| local_retention_expectation | Supervisor device retention follows current scope contraction policy if review authority ends later. |

**Deferred or future surfaces**

| Surface | Reason | Route |
|---|---|---|
| General trigger execution | Late review reminders or escalation are not needed for the probe. | BAR-101; future server-side L3 policy per CDL-042. |
| Auto-resolution | `auto_eligible` flag categories are not automatically resolved by this probe. | BAR-102 successor slice. |
| Resolver reassignment | The probe uses exact designated resolver semantics only. | BAR-103 successor decision. |

### Probe 4 - S27 Logistics Distribution With Discrepancy Review

**Scenario**

| Field | Value |
|---|---|
| id | S27 |
| title | Logistics Distribution Across Multiple Handoffs |
| real_world_goal | Supplies move from central warehouse to districts and field teams; each handoff is confirmed, discrepancies remain visible, and review does not rely on health-domain concepts. |
| scenario_class | `baseline_composite` |
| primary_pressure | Transfer-with-acknowledgment pattern, partial receipt, discrepancy review, scoped sync, and domain-agnostic projection. |
| candidate_probe_value | high |

**Actors**

| Role | Responsibility | Current authority model | Expected scope |
|---|---|---|---|
| Warehouse sender | Record dispatch from central custody. | Assignment-derived sender role with capture permission. | Warehouse/district/activity scope. |
| District receiver | Confirm receipt or report discrepancy. | Assignment-derived receiver role with capture permission. | District supply subject/activity scope. |
| Field team | Continue downstream distribution from confirmed received quantities. | Assignment-derived receiver/sender role for next leg. | Field team or delivery-point scope. |
| Supervisor/steward | Review discrepancies. | Human resolver or supervisor role; exact designated resolver for flags. | Scope containing transfer subject and discrepancy activity. |

**Subjects or real-world things**

| Description | Identity risk | Location or scope | Lifecycle pressure |
|---|---|---|---|
| Supply batch, dispatch, custody leg, or delivery point depending on activity design. | Duplicate dispatch identifiers or overlapping receipts may occur after offline sync. | Warehouse, district, field team, and delivery point scopes. | None; supplies are tracked through events, not a new registry lifecycle mechanism. |

**Offline windows**

| Actor | Duration or condition | Stale risks |
|---|---|---|
| District receiver | Receives or partially receives while offline. | Dispatch may have been amended, split, or already disputed centrally. |
| Field team | Continues downstream distribution after local receipt. | Unresolved discrepancy may exist centrally before field team syncs. |

**Activities and config**

| Activity or work | Shapes or payloads | Expressions | Pattern binding | Config pressure |
|---|---|---|---|---|
| Multi-hop logistics transfer | Deployer shapes bound as dispatch, receipt, discrepancy_report, discrepancy_resolution. | L2 quantity warnings only; no scripts or computed custody engine. | `transfer_with_acknowledgment/v1`. | Pattern binding and role-action mapping must be deploy-time validated. |

**Expected events**

| Type | shape_ref | subject_ref | actor_ref | activity_ref | Notes |
|---|---|---|---|---|---|
| `assignment_changed` | `assignment_created/v1` | Assignment ref | Coordinator/supervisor | null or activity | Grants sender/receiver/supervisor roles within current scope mechanisms. |
| `capture` | dispatch shape | Transfer/custody subject | Warehouse sender | Logistics activity | Starts `in_transit` state. |
| `capture` | receipt shape | Same transfer/custody subject | District receiver | Logistics activity | Moves to `received` or `partial_receipt` based on discrepancy branch. |
| `capture` | discrepancy_report shape | Same transfer/custody subject | Receiver or steward | Logistics activity | Moves to `disputed` when needed. |
| `review` | discrepancy_resolution shape | Same transfer/custody subject | Supervisor/steward | Logistics activity | Manual review moves to `resolved` when valid. |
| `alert` | `conflict_detected/v1` | Source event subject | System actor | Logistics activity | Raised for invalid transitions, unauthorized actions, or configured uniqueness conflicts. |

**Expected flags**

| Category | Reason | Severity | Resolvability | Resolver route |
|---|---|---|---|---|
| `transition_violation` | Receipt or resolution arrives out of order under pattern state. | informational | `auto_eligible` | Workflow/activity supervisor unless future auto policy designates system actor. |
| `scope_violation` | Actor records a transfer leg outside assignment scope. | blocking | `manual_only` | Nearest common human steward. |
| `domain_uniqueness_violation` | Configured dispatch/batch identifier is duplicated. | blocking | `manual_only` | Logistics/activity data steward. |
| `concurrent_state_change` | Two devices record overlapping transfer-state changes without seeing each other. | blocking | `manual_only` | Source-event human resolver. |

**Sync, projection, and retention**

| Area | Expectation |
|---|---|
| live_sync_expectation | Each actor pulls only assigned custody/activity scope; pushed handoff events are ordered by server watermark after sync. |
| subject_history_expectation | If a receiver is newly assigned to an existing transfer subject, subject-history backfill may repair the pattern projection for that subject/activity. |
| projection_expectation | Pattern state derives from `transfer_with_acknowledgment/v1`; unresolved flagged events are excluded from state derivation. |
| local_retention_expectation | Own dispatch/receipt events remain for provenance; out-of-scope other-party events follow selective retention policy. |

**Deferred or future surfaces**

| Surface | Reason | Route |
|---|---|---|
| Auto-resolution | Discrepancy review remains human/manual in the probe. | BAR-102 successor policy/trigger slice. |
| New scope mechanisms | Custody scope must fit current geographic, subject_list, and activity axes. | BAR-108 successor decision if insufficient. |
| New envelope fields/types | Transfer semantics live in shape_ref, payload, pattern binding, and projection. | BAR-107 successor decision only. |

## 5. Deferred/Future Pressure Register

| Scenario | Pressure | Route | Why not now |
|---|---|---|---|
| S06, S22 | Maintaining a known set of things, deactivation, discovered-unit lifecycle, and bulk registry changes. | BAR-105; NW-021 successor product/platform decision. | Entity lifecycle remains deferred; probes may record about subjects but must not promote registry lifecycle. |
| S10, S12, S18 | Condition/event/analytics-derived work initiation and escalation. | BAR-101; CDL-042; NW-017 keeps S12 pressure visible; NW-020 for S18. | General trigger execution is deferred; future trigger execution is server-side L3 policy, not device-side or deployer-scripted behavior. |
| S07, S14, S21, S27 | Desire to automatically clear benign discrepancies or transition issues. | BAR-102 successor policy/trigger slice. | Auto-resolution is deferred even for `auto_eligible` categories; current probes use human/exact-resolver resolution only. |
| S04, S08, S11, S21 | Reassigning who resolves a flag or changing resolver authority after emission. | BAR-103 successor platform decision. | Resolver reassignment needs audit semantics and migration rules; current flag resolution requires exact designated resolver. |
| S16, S25 | Emergency authority override or production identity-provider group/claim authority. | BAR-104; FP-011; NW-019 for emergency mini-brief. | Production OIDC/JWT/Keycloak integration and group/claim authority are future decisions; assignments remain the authority source now. |
| S15, S24 | Multi-audience views, local sensitivity handling, redaction/encryption, and sensitivity-specific local purge beyond current retention policy. | BAR-106; NW-018 for S15 context. | Field-level sensitivity/encryption/redaction is future-decision work; do not treat UI hiding as sufficient protection. |
| S13, S27, any composite probe | Request for extra structural references, workflow state, pattern refs, or scope/sensitivity metadata in events. | BAR-107; CDL-006 successor decision. | New envelope fields or event types are architecture-grade changes; current semantics must use existing envelope fields plus payload/config/projection. |
| S09, S14, S16, S22, S25, S27 | Custom campaign, emergency, custody, or temporary access logic beyond current scope axes. | BAR-108; CDL-055 successor decision. | New scope mechanisms or deployer-defined containment scripts are security-sensitive platform evolution. |
| S23 | Configuration requests that need loops, functions, hidden scripts, dynamic queries, or custom code. | CDL-038, CDL-043; EH-005 if repeated measured need appears. | Configuration must stay bounded; expression function growth needs successor platform decision. |
| S26 | Aggregate reporting that needs a separate reporting warehouse or analytics storage model. | Escape-hatch future extraction seam; no active trigger claimed. | Current probe should use projection/reporting semantics and freshness labels, not create a new analytics subsystem. |

## 6. Original Test And Backlog Recommendations

Current status: the S00, S19, S21, and S27 runtime-probe recommendations below have been executed through NW-026, NW-025, NW-029, and NW-030 respectively. NW-009 and NW-010 are also accepted. The table is retained as the original NW-023 route, not as an open task list.

| Recommendation | Type | Priority | Depends on | Exit condition |
|---|---|---|---|---|
| Keep NW-009 and NW-010 ahead of broad composite scenario acceptance. | baseline_acceptance | P1 | BAR-008, BAR-009, BAR-011, BAR-012, BAR-013, BAR-014 | Identity, retention, expression, pattern-state, transition/domain uniqueness, projection equivalence, and mobile tests attach fresh evidence. |
| Add a focused S00 runtime probe after or alongside append-only/event-store verification. | scenario_runtime_probe | P1 | BAR-001 accepted; BAR-002/BAR-005/BAR-010/BAR-013 verification surface | End-to-end capture/correction/duplicate path produces accepted events and expected flags without mutation or contract changes. |
| Add a constrained S19 runtime probe for offline/stale authority/scoped reconciliation. | scenario_runtime_probe | P1 | BAR-003, BAR-004, BAR-007 accepted; BAR-008 and BAR-014 verification | Offline push/pull, subject-history isolation, stale-authority flags, and selective retention behavior are exercised together. |
| Add an S21 review probe after pattern/projection baseline verification. | scenario_runtime_probe | P2 | NW-009 and NW-010, especially BAR-012/BAR-013/BAR-014 | Scoped supervisor review produces expected pattern state, flags invalid review/state cases, and preserves exact resolver semantics. |
| Add an S27 non-health transfer probe as the first domain-agnostic composite. | scenario_runtime_probe | P2 | NW-009 and NW-010, especially transfer pattern and projection equivalence | Multi-hop logistics transfer reaches expected states, discrepancy review remains manual, and scoped sync/projection work without health assumptions. |
| Treat S23 and S26 as next-wave probes, not first-wave blockers. | backlog_followup | P2 | NW-009/NW-010 plus config verification | Setup/config and reporting/freshness probes are scoped after core pattern/projection/mobile candidate risks are resolved. |
| Keep S06, S15, S16, and S18 out of runtime-probe scope until selected by successor work. | deferral_control | P4 | BAR-101, BAR-104, BAR-105, BAR-106, BAR-108 or related successor decisions | Deferred extension rows remain problem-space pressure only; no BAR row is promoted by scenario wording. |

## 7. Safe Progress Call

Current execution status:

- The original near-term set plus S23 setup/config evidence is complete: S00, S19, S21, S23, and S27 are accepted in the backlog with runtime evidence.
- NW-009 and NW-010 are accepted, so S21/S27 are no longer blocked on projection/mobile evidence.
- BAR-010 config package delivery is accepted, so all BAR-001 through BAR-015 baseline rows are accepted.

Original call:

- Use S00 and a constrained S19 as first scenario-grade probe designs, because they pressure accepted sync/envelope/authority baseline without requiring deferred capabilities.
- Prepare S21 and S27 probe designs in parallel with NW-009/NW-010, but run/accept them only after pattern/projection/mobile candidate verification is green enough to avoid confusing baseline gaps with scenario failures.

Original NW-009/NW-010 gate (satisfied as of 2026-06-03):

- Anything that claims pattern-state projection, projection equivalence, expression parity, mobile selective retention, or unresolved-flag exclusion as baseline behavior should wait for those verification rows or attach fresh equivalent evidence.
- S23 setup/config has landed as NW-032. S26 reporting/aggregate oversight remains the later ready-with-constraints probe when explicitly selected.

Needs successor decisions:

- General trigger execution remains deferred under BAR-101 and constrained to server-side L3 policy by CDL-042.
- Auto-resolution remains deferred under BAR-102, even when a flag category is `auto_eligible`.
- Resolver reassignment remains BAR-103 future-decision work.
- Production OIDC/JWT/Keycloak authority and group/claim authority remain BAR-104/FP-011 future-decision work.
- Entity lifecycle remains deferred under BAR-105.
- Field-level sensitivity/encryption/redaction remains BAR-106 future-decision work.
- New envelope fields or event types remain BAR-107 future-decision work.
- New scope mechanisms remain BAR-108 future-decision work.

Safe recommendation as of 2026-06-03: treat S00/S19/S21/S23/S27 as accepted scenario evidence, not as pending work. S26 reporting/aggregate oversight remains a later ready-with-constraints movement if explicitly selected. Deferred/future-decision surfaces above remain unpromoted.
