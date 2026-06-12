# Scenario User-Fit Packet — S19: Offline-First Work and Sync

## 1. Scenario frame

**Scenario ID:** S19
**Scenario title:** Working Without Connectivity / Offline Capture and Sync
**Scenario role in platform learning:** Foundational offline-first constraint across all operational scenarios

**Source scenario summary:**
People in the field often work where connectivity is unreliable or unavailable. They still need to record observations, look up relevant information, make decisions, complete assigned work, and continue operations. When connectivity returns, what they did must become centrally visible and reconciled with what others may have done in the meantime.

**Why this scenario matters:**
Offline is not an edge case. It is a primary operating condition. A platform that works only when connected is not usable for the target deployments. S19 tests whether the platform can preserve field reality, support delayed visibility, and surface conflicts without making field workers resolve complex sync problems.

**What this scenario must not decide:**
This packet does not decide sync transport protocol, local database design, retry queues, conflict-resolution algorithms, storage partitioning, peer-to-peer sync, background job design, API shape, compression strategy, or device backup mechanics.

## 2. User and organization fit

### 2.1 Organization profile

**Organization type:** Field-operating organization whose primary work happens outside reliable connectivity.
**Operational domain:** Domain-neutral; applies to health, logistics, humanitarian response, agriculture, infrastructure, education, inspections, distribution, campaigns, and supervision.
**Scale:** Small field teams through national deployments with tens of thousands of workers and millions of records.
**Connectivity profile:** Field workers may be offline for hours, days, or longer. Supervisors may be intermittently connected. Coordinators may be online but still see delayed field reality.
**Operational maturity:** May currently use paper as the offline fallback, then manually re-enter or reconcile data later.
**Sensitivity pressure:** Offline devices may hold sensitive records; access and retention must be constrained by responsibility.
**Current workaround:** Paper forms, WhatsApp notes, spreadsheet copies, photos, local notebooks, duplicate re-entry, postponed work, or “sync when you reach town.”

### 2.2 Personas by operational surface

#### Persona A — Field worker

**Operational surface:** Field execution
**Real-world responsibility:** Do assigned work even without network access.
**Work context:** Often alone, mobile, time-constrained, using low-end Android, possibly in local language.
**Primary intent:** Capture or act immediately and trust that the work will not be lost.
**What they need to see or do:** Open assigned work, view relevant subjects, capture records, complete steps, correct mistakes where allowed, and see clear local save/sync status.
**What they need to trust:** Work saved offline still counts. The device has the right working set. Sync problems will not erase their work.
**What they should not need to understand:** Event IDs, device sequences, sync watermarks, conflict categories, projection rebuilds, access-scoped sync, or causality rules.

#### Persona B — Supervisor

**Operational surface:** Supervision / review
**Real-world responsibility:** Review work after it becomes visible, while understanding that field reality may be ahead of synced data.
**Work context:** Intermittently connected; may supervise teams that sync at different times.
**Primary intent:** Know what is latest visible, what may still be pending, and what needs review because of stale or conflicting offline work.
**What they need to see or do:** Review synced records, distinguish completed field work from centrally visible work, inspect stale/conflicting items, and follow up with workers.
**What they need to trust:** Delayed records are not silently discarded or overwritten.

#### Persona C — Coordinator / administrator

**Operational surface:** Coordination / administration
**Real-world responsibility:** Configure activities, assignments, and shape changes knowing they may not reach all devices immediately.
**Work context:** Usually online; manages rollout and monitors sync health.
**Primary intent:** Understand deployment state: who has synced, who is stale, what configuration is active, and which field work may arrive later under older assumptions.
**What they need to see or do:** Monitor sync freshness, rollout configuration changes, adjust assignments, and understand what delayed work may affect.
**What they need to trust:** Configuration and responsibility changes do not invalidate valid offline work.

#### Persona D — Reviewer / resolver

**Operational surface:** Review / exception handling
**Real-world responsibility:** Resolve conflicts, stale-authority cases, duplicate records, or invalid transitions created by disconnected work.
**Work context:** Usually online or better connected.
**Primary intent:** Make human judgments where the system cannot safely decide.
**What they need to see or do:** Compare records, see source context, know what happened when, identify who is designated to resolve, and avoid repeated conflicting resolutions.
**What they need to trust:** The platform preserved all relevant facts and did not silently choose a winner.

#### Persona E — Auditor / external reviewer

**Operational surface:** Audit / external review
**Real-world responsibility:** Reconstruct what happened, when it happened, when it synced, and under what authority.
**Work context:** Periodic or targeted.
**Primary intent:** Verify that offline work was not rewritten, hidden, or falsely ordered.
**What they need to trust:** The distinction between field-time and central-visibility-time is preserved.

## 3. User intent

### 3.1 Jobs to be done

**Field worker job:**
When I am offline, I need to keep working and save what I do, so that field reality is captured once and not lost because the network is absent.

**Supervisor job:**
When I review team work, I need to know what has synced, what may still be missing, and what conflicts need attention, so that I do not mistake central delay for field inactivity.

**Coordinator job:**
When I change assignments or configuration, I need to understand that devices may apply the change later, so that rollout does not invalidate work done under older local knowledge.

**Reviewer/resolver job:**
When offline work conflicts with other work, I need enough context to resolve the issue without asking the field worker to understand sync internals.

**Auditor job:**
When I inspect the history, I need to see when work happened and when it became visible, so that accountability is not distorted by sync timing.

### 3.2 Intent categories

* Continue work without connectivity
* Save locally with confidence
* Sync opportunistically
* Preserve field-time and central-visibility distinction
* Reconcile independently created work
* Surface conflicts without silent overwrite
* Preserve stale but real work
* Handle old configuration and old authority
* Show freshness and uncertainty to supervisors
* Avoid making field workers resolve system-level conflicts

### 3.3 Success from the user’s point of view

**Field worker success:**
“I can do the work now, save it, and trust it will sync later.”

**Supervisor success:**
“I know what I am seeing is the latest synced state, not necessarily all field reality.”

**Coordinator success:**
“I can change setup and assignments without breaking valid offline work.”

**Reviewer success:**
“I can resolve exceptions with enough context.”

**Organization success:**
“Offline work remains trustworthy, traceable, and reconcilable.”

## 4. Real-world journey

### 4.1 Normal offline capture path

1. Field worker syncs before leaving coverage.
2. Device receives assigned work, relevant subjects, active configuration, and local authority.
3. Worker goes offline.
4. Worker captures records or completes assigned work.
5. Device saves work locally.
6. Worker sees local saved state.
7. When connectivity returns, device syncs.
8. Records become centrally visible.
9. Supervisors see synced work and freshness indicators.

### 4.2 Long-disconnection path

1. Worker remains offline for days.
2. During that time, central state changes: new configuration, changed assignments, deactivated subjects, reviewed records, or new tasks.
3. Worker continues operating under last-known local state.
4. The longer the gap, the more likely stale assumptions become.
5. On sync, work is preserved.
6. Stale or conflicting conditions are surfaced for review rather than rejected silently.

### 4.3 Offline duplicate-record path

1. Two workers independently record information about the same subject while offline.
2. Neither knows about the other record.
3. Both records sync later.
4. They may differ in details, time, assessment, or decision.
5. The platform preserves both records.
6. The issue is surfaced as duplicate, conflict, or review-needed where appropriate.
7. A reviewer decides whether both are valid, one supersedes the other, or correction is needed.

### 4.4 Offline subject-staleness path

1. Worker has an old local subject list.
2. A subject is centrally deactivated, merged, split, reassigned, or updated.
3. Worker records against the subject identity available locally.
4. On sync, the event is accepted.
5. The platform detects that the reference may be stale or cross-lifecycle.
6. Projection or review surfaces the issue without rewriting the historical reference.

### 4.5 Offline authority-staleness path

1. Worker syncs and receives an assignment.
2. Worker goes offline.
3. Assignment is centrally revoked, contracted, or expired.
4. Worker continues under last-known authority.
5. On sync, the work is preserved but marked for stale-authority review where needed.
6. Reviewer decides whether the work remains valid, requires correction, or should be excluded from downstream action.

### 4.6 Offline shape-staleness path

1. Worker has configuration version v1.
2. Coordinator deploys configuration version v2.
3. Worker remains offline and continues using v1.
4. Worker syncs v1 records after v2 is already active centrally.
5. v1 records remain valid under v1.
6. New work after config sync uses v2.
7. Reports distinguish records created under different shape versions.

### 4.7 Offline review or decision path

1. Supervisor or reviewer is offline.
2. They review, approve, reject, or comment based on their local state.
3. Another reviewer or central actor may act during the same period.
4. On sync, the platform preserves both decisions.
5. If decisions conflict or violate a transition pattern, the issue is flagged.
6. Downstream action waits where flag severity requires it.

### 4.8 Central oversight path

1. Coordinator views operational dashboard.
2. Some workers have synced recently; others have not.
3. The view is incomplete by design.
4. Dashboard should show freshness, stale devices, pending sync, and unresolved issues.
5. Coordinator must not interpret “not visible centrally” as “not done in field.”

### 4.9 Device loss path

1. Worker captures work offline.
2. Device is lost, damaged, reset, or stolen before sync.
3. Unsynced work may be unrecoverable unless separate implementation or operational safeguards exist.
4. The platform/spec must distinguish guaranteed architecture behavior from operational risk.
5. Early deployments need clear support and mitigation practice.

## 5. Domain-neutral artifacts

| Artifact                           | Purpose                                                          | Created by               | Used by                               | Changes over time? | Trust requirement                               |
| ---------------------------------- | ---------------------------------------------------------------- | ------------------------ | ------------------------------------- | ------------------ | ----------------------------------------------- |
| Offline working set                | Data and configuration available on device while disconnected    | Sync/access process      | Field worker                          | Yes, on sync       | Must contain only authorized, relevant work     |
| Local saved record                 | Work captured before central sync                                | Field worker/device      | Field worker, sync process            | Yes until synced   | Must not disappear silently                     |
| Unsynced queue / pending work list | Shows what still needs sync                                      | Device/product surface   | Field worker, supervisor if visible   | Yes                | Must give confidence without exposing internals |
| Sync status indicator              | Shows whether work is synced, pending, failed, or blocked        | Device/sync surface      | Field worker                          | Yes                | Must be understandable                          |
| Last synced indicator              | Shows freshness of local or central view                         | Device/server projection | Field worker, supervisor, coordinator | Yes                | Must prevent false certainty                    |
| Central visibility timestamp       | Indicates when work became visible centrally                     | Sync/projection surface  | Supervisor, coordinator, auditor      | Yes                | Must be distinct from field-time                |
| Field-time timestamp               | Indicates when work was done according to device/human context   | Device/event record      | Supervisor, auditor                   | No, advisory       | Must not become sole structural ordering source |
| Stale-work flag                    | Marks work created under outdated subject/config/authority state | Runtime service          | Reviewer, supervisor                  | Yes                | Must preserve work while surfacing uncertainty  |
| Conflict review item               | Presents conflicting offline records or decisions                | Runtime/projection       | Reviewer/resolver                     | Yes                | Must support human judgment                     |
| Config freshness view              | Shows which version device/work used                             | Config/sync surface      | Coordinator, supervisor               | Yes                | Must avoid invalidating old-version work        |
| Assignment freshness view          | Shows whether authority may have changed since last sync         | Projection/sync surface  | Coordinator, reviewer                 | Yes                | Must support stale-authority review             |
| Sync health summary                | Shows who has not synced and for how long                        | Reporting projection     | Supervisor, coordinator               | Yes                | Must not treat absence as non-work              |
| Device incident note               | Records lost/damaged/replaced device handling                    | Operational support      | Coordinator/support/auditor           | Yes                | Must clarify what was and was not synced        |

## 6. Concrete domain exemplar

### Exemplar: Community field team working across remote villages

**Domain:** Health / community outreach
**Organization:** District program with community workers and supervisors
**Concrete artifact names:** Assigned household list, visit form, pending sync list, last synced status, stale-assignment review, supervisor sync dashboard
**Concrete user language:** “I visited households all day without network. The records are saved on my phone and will upload when I return to coverage.”

### Concrete journey

1. Community worker syncs at the district office.
2. Device receives assigned households, current visit forms, and relevant work.
3. Worker travels to remote villages with no connectivity.
4. Worker records visits, observations, and follow-up notes.
5. Another worker independently records about one household in a neighboring area.
6. Coordinator changes one worker’s assignment while both are offline.
7. Worker returns to coverage and syncs.
8. All records are accepted.
9. Some records are flagged because assignment or subject state changed while the worker was offline.
10. Supervisor sees the records, sync delay, and review items.
11. The organization can distinguish when work happened from when the office saw it.

### Where this example may mislead platform design

* It may imply health-specific behavior; the scenario is domain-neutral.
* It may imply the field worker should resolve conflicts; many conflicts require supervisor or coordinator judgment.
* It may imply every offline event can be automatically accepted into downstream workflow; flagged events may need gating.
* It may imply device loss is solved by architecture; device-loss mitigation is implementation and operational policy unless a formal architecture decision adds guarantees.
* It may imply offline sync is a UI feature only; offline pressure affects event, identity, access, configuration, workflow, reporting, and support.

## 7. Platform fit under current accepted architecture

| User need                                      | Likely settled platform vocabulary                 | Fit assessment                        |
| ---------------------------------------------- | -------------------------------------------------- | ------------------------------------- |
| Work without server connection                 | client-generated event IDs, local event creation   | Strong fit                            |
| Save record once and sync later                | immutable event as sync unit, idempotent sync      | Strong fit                            |
| Preserve record history                        | append-only event store, correction as new event   | Strong fit                            |
| Distinguish field-time from central visibility | timestamp advisory, sync metadata, projections     | Strong fit, product semantics open    |
| Detect stale work                              | `sync_watermark`, stale reference, accept-and-flag | Strong fit                            |
| Preserve stale work instead of rejecting       | accept-and-flag                                    | Strong fit                            |
| Handle out-of-order arrival                    | event sync order-independent; causal metadata      | Strong fit                            |
| Handle local authority while offline           | assignment-based access, sync scope = access scope | Strong fit                            |
| Reconcile stale authority                      | auth flags, accept-and-flag                        | Strong fit, review UX/spec open       |
| Handle old shape after config change           | `shape_ref`, shape version, atomic config delivery | Strong fit                            |
| Prevent downstream action before review        | detect-before-act, flagged-event exclusion         | Strong fit                            |
| Show current state after sync                  | projections/read models                            | Strong fit                            |
| Resolve manual conflicts                       | single-writer resolution, designated resolver      | Strong fit, resolver policy/spec open |
| Sync only authorized data                      | sync scope equals access scope                     | Strong fit                            |
| Device loss before sync                        | not an architecture guarantee; routed implementation/operational risk | Visible planning lane |

### 7.1 Current standing caveat

S19 has accepted runtime evidence through NW-025, and shared-device
actor-partition behavior is accepted through NW-055. That does not settle local
retention after access loss, device decommissioning, sealed-partition recovery,
token/session retention, local encryption, backup/recovery guarantees, or
no-local-retention/redacted views. Those route through NW-054/BAR-106 and
operational policy.

References to deactivated, merged, split, or reassigned subjects are stale
subject/linkage pressure. They do not authorize a full S06/entity lifecycle
implementation.

## 8. Fit assessment

### 8.1 Strong fit

The accepted architecture is strongly aligned with S19. It treats offline-created work as durable events, avoids stale-state rejection, preserves historical facts, carries causal metadata, and ties sync to access. This supports the core field promise: work can continue without connectivity.

### 8.2 Weak fit

User-facing offline confidence is not yet proven. The architecture can preserve and reconcile events, but field workers need simple signals: saved, pending sync, synced, needs review, failed, or no longer accessible. Supervisors need freshness semantics. Coordinators need rollout visibility.

### 8.3 Missing evidence

* How long are field users commonly offline in early deployments?
* What work must be possible offline beyond capture?
* Do field users need subject history offline, or only lookup lists?
* What minimum sync status language is understandable?
* How often do assignments change while devices are offline?
* How often do shape/config changes occur while devices are offline?
* What should happen when a worker loses a device before syncing?
* How much local history should remain after access scope contracts?
* Which conflicts are common and which are rare?
* Which offline decisions are safe to make locally?
* Which offline decisions should be advisory only until sync?
* How do supervisors currently interpret delayed reporting?
* What support procedure exists for sync failures?

### 8.4 Risk of false fit

The platform may be architecturally offline-first but still feel unreliable if users cannot tell whether work is saved, whether sync completed, or whether central staff have seen it. Offline success is partly architecture, partly product language, partly implementation reliability, and partly operational support.

## 9. Gap routing

### Gap 1 — Offline status language

**Short name:** Offline status vocabulary fit
**Classification:** Product/problem evidence gap
**Baseline-extension category:** Not applicable
**Current owner or likely decision path:** Product discovery / scenario thickening
**Baseline item affected:** User-facing offline confidence and sync-state language
**Why it is still open:** The architecture has sync metadata, but users need simple language for saved, pending, synced, failed, stale, and needs review.
**Closure path:** Product discovery / scenario thickening
**Evidence needed before closure:** Field interviews and usability testing with low-connectivity users.

### Gap 2 — Sync freshness semantics

**Short name:** Sync freshness semantics
**Classification:** Platform-spec detail gap
**Baseline-extension category:** Not applicable
**Current owner or likely decision path:** Platform-spec detailing
**Baseline item affected:** Reporting/oversight projections and central visibility
**Why it is still open:** Supervisors and coordinators need to distinguish field-time, sync-time, last-seen device state, and freshness of reports. Exact semantics are not specified here.
**Closure path:** Platform-spec detailing
**Evidence needed before closure:** Examples of supervision decisions affected by delayed sync.

### Gap 3 — Offline stale-authority review

**Short name:** Stale authority review behavior
**Classification:** Platform-spec detail gap
**Baseline-extension category:** Not applicable
**Current owner or likely decision path:** Platform-spec detailing, with operational policy support
**Baseline item affected:** Authorization staleness, accept-and-flag, detect-before-act
**Why it is still open:** The architecture accepts and flags stale-authority work, but does not define user-facing review, severity, or resolution paths.
**Closure path:** Platform-spec detailing
**Evidence needed before closure:** Real examples of work after revocation, assignment transfer, or temporary grant expiry.

### Gap 4 — Offline conflict review ergonomics

**Short name:** Offline conflict review ergonomics
**Classification:** Platform-spec detail gap
**Baseline-extension category:** Visible routed lane outside current baseline
**Current owner or likely decision path:** Platform-spec detailing
**Baseline item affected:** Conflict flag queue and resolver workflow
**Why it is still open:** Offline work creates duplicate, stale, concurrent, and transition conflicts. The architecture settles detection and resolution boundaries, but not reviewer-facing comparison and queue behavior.
**Closure path:** Platform-spec detailing
**Evidence needed before closure:** Examples of conflicts that require human judgment.

### Gap 5 — Long-disconnection operating policy

**Short name:** Long offline duration policy
**Classification:** Operational policy gap
**Baseline-extension category:** Not applicable
**Current owner or likely decision path:** Operational policy definition
**Baseline item affected:** Deployment support and risk handling, not event architecture
**Why it is still open:** The architecture accepts offline work, but organizations need procedures for workers offline for days or weeks, including check-in expectations and manual follow-up.
**Closure path:** Operational policy definition
**Evidence needed before closure:** Field deployment norms and acceptable offline windows.

### Gap 6 — Device loss before sync

**Short name:** Unsynced device loss handling
**Classification:** Implementation/tooling gap
**Baseline-extension category:** Not applicable
**Current owner or likely decision path:** Implementation/tooling design, with operational policy support; NW-054/BAR-106 if recovery, retention, encryption, or decommissioning guarantees are proposed
**Baseline item affected:** Local durability and support process, not event envelope
**Why it is still open:** Events are durable once stored and synced, but unsynced device loss creates practical risk. Exact backup, recovery, transfer, or support behavior needs routed implementation, security, and operational planning before implementation.
**Closure path:** Implementation/tooling design
**Evidence needed before closure:** Device-loss frequency, sensitivity level, and acceptable recovery guarantees.

### Gap 7 — Local data after scope contraction

**Short name:** Local retention after access loss
**Classification:** Platform-spec detail gap
**Baseline-extension category:** Not applicable
**Current owner or likely decision path:** NW-054/BAR-106 for retention/security behavior, with operational policy support
**Baseline item affected:** Selective-retain on scope contraction
**Why it is still open:** The architecture separates actor partitions and scope-filtered sync, but product behavior, user messaging, retention windows, expiry, and sensitive-data handling need the NW-054/BAR-106 planning lane.
**Closure path:** NW-054/BAR-106 decision route, then platform-spec detailing if approved
**Evidence needed before closure:** Examples of role transfer, offboarding, and sensitive local data.

### Gap 8 — Offline configuration rollout visibility

**Short name:** Config rollout visibility
**Classification:** Platform-spec detail gap
**Baseline-extension category:** Not applicable
**Current owner or likely decision path:** Platform-spec detailing
**Baseline item affected:** Atomic config delivery and old/new shape coexistence
**Why it is still open:** The architecture supports versioned config, but coordinators need visibility into which devices/users have received which version.
**Closure path:** Platform-spec detailing
**Evidence needed before closure:** Deployment examples where config changes reach devices at different times.

### Gap 9 — Priority sync and partial sync behavior

**Short name:** Priority sync behavior
**Classification:** Implementation/tooling gap
**Baseline-extension category:** Visible routed lane outside current baseline
**Current owner or likely decision path:** Implementation/tooling design
**Baseline item affected:** Sync transport and delivery mechanics under accepted sync/access boundaries
**Why it is still open:** Large deployments may need prioritization, pagination, backfill, and retry behavior. These are implementation mechanics unless they change sync/access authority.
**Closure path:** Implementation/tooling design
**Evidence needed before closure:** Bandwidth, device, and scale data from target deployments.

### Gap 10 — Offline decision boundaries

**Short name:** Offline decision authority boundaries
**Classification:** Platform-spec detail gap
**Baseline-extension category:** Not applicable
**Current owner or likely decision path:** Platform-spec detailing
**Baseline item affected:** Local command validation, accept-and-flag, workflow-state derivation
**Why it is still open:** Some offline decisions can be accepted and reconciled; others may need warning, review, or downstream gating. Exact user-facing behavior needs routed platform-spec and operational-policy work before implementation.
**Closure path:** Platform-spec detailing
**Evidence needed before closure:** Examples of offline approvals, referrals, corrections, task completions, and reviews.

## 10. Output decision

**Current packet status:** Ready to feed synthesis; unresolved surfaces remain visible planning lanes before implementation.

**Reason:**
The architecture supports offline-first work strongly, but user-facing offline confidence, sync freshness, stale-authority review, conflict-review ergonomics, device-loss handling, local retention, and rollout visibility need specification, implementation design, operational policy, or product evidence before becoming platform-spec.

## 11. Acceptance criteria for downstream platform-spec work

A routed Offline-first / S19 platform-spec section should be accepted only if it satisfies:

1. Field users can create and save work without connectivity.
2. Field users can understand whether work is saved locally, pending sync, synced, failed, or needs review.
3. Work created offline is not silently rejected because central state changed.
4. Field-time and central-visibility time are distinguishable.
5. Old-shape work remains valid under the shape version available to the device.
6. Offline work under stale authority is preserved and surfaced for review.
7. Duplicate or conflicting offline records are surfaced without silent overwrite.
8. Sync freshness is visible to supervisors and coordinators.
9. Devices receive only authorized data.
10. Local data after access loss follows selective-retain rules.
11. Manual-only conflicts are not auto-resolved.
12. Device loss before sync is addressed as an explicit implementation/operational risk.
13. S19 does not make S00 basic capture harder.
14. Offline behavior is explained in user language, not architecture vocabulary.
