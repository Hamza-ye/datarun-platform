# Scenario User-Fit Packet — Access Control and Visibility

## 1. Scenario frame

**Scenario ID:** Cross-cutting
**Scenario title:** Access Control and Visibility
**Scenario role in platform learning:** Foundational authority, visibility, and responsibility scenario

**Source scenario summary:**
In operational environments, different people need different levels of visibility and authority. What someone can see or do depends on who they are, what role they play, where they work, what activity they are involved in, which subjects they are responsible for, and sometimes when the authority is active.

**Why this scenario matters:**
A platform is not deployable if everyone sees everything or if authority is global and static. Early deployments need a safe, understandable access model that supports everyday responsibilities, supervision, temporary coverage, role changes, and offline work without exposing architecture mechanics to users.

**What this scenario must not decide:**
This packet does not decide exact permission tables, access-policy syntax, database row security, API authorization implementation, local storage partitioning, UI screens, audit export format, or organization-specific governance rules.

## 2. User and organization fit

### 2.1 Organization profile

**Organization type:** Field-operating organization with multiple roles, locations, activities, and responsibility boundaries.
**Operational domain:** Domain-neutral; applies across health, logistics, agriculture, humanitarian response, infrastructure, education, inspections, and other operations.
**Scale:** Small teams through national deployments with many organizational levels.
**Connectivity profile:** Field users may work offline while assignments, responsibilities, or role permissions change centrally.
**Operational maturity:** May start with simple geographic responsibility and grow into activity-specific, temporary, subject-list, campaign, or audit access.
**Sensitivity pressure:** Ranges from routine operational data to restricted personal or sensitive program information.
**Current workaround:** Organizations often rely on spreadsheets, shared passwords, messaging groups, manual exports, broad admin accounts, or local judgment about “who should see this.”

### 2.2 Personas by operational surface

#### Persona A — Field worker

**Operational surface:** Field execution
**Real-world responsibility:** Do assigned work in a defined area, subject set, activity, or time window.
**Work context:** Often offline, using a low-end Android phone, with limited visibility into central changes.
**Primary intent:** See only the work and records needed to do the job, and act without being blocked by connectivity.
**What they need to see or do:** Open assigned activities, view relevant subjects, capture records, complete assigned tasks, and know when something is outside their responsibility.
**What they need to trust:** The device contains the right working set and will not expose unrelated data.
**What they should not need to understand:** Assignment timelines, sync scope, authority projections, scope-containment tests, stale-authority flags, or access conflict categories.
**Platform authority mapping:** Later mapped through accepted `actor`, `assignment`, `role`, `scope`, and time-bound authority.

#### Persona B — Supervisor or team lead

**Operational surface:** Supervision / review
**Real-world responsibility:** Oversee work performed by a team or within an assigned scope.
**Work context:** Partly field, partly office or hub; intermittently connected.
**Primary intent:** See enough to manage team work, review quality, and follow up on problems.
**What they need to see or do:** View submitted records, see worker progress, review issues, compare related records, and act on assigned review responsibilities.
**What they need to trust:** They are seeing the relevant team or area, not an incomplete or overbroad view.
**What they should not need to understand:** Internal sync partitioning, access-scoped projection logic, or device retention mechanics.

#### Persona C — Coordinator or administrator

**Operational surface:** Coordination / administration
**Real-world responsibility:** Define who is responsible for what, where, and when.
**Work context:** More likely online, using larger-screen devices.
**Primary intent:** Assign responsibilities safely and adjust them as operations change.
**What they need to see or do:** Create assignments, change roles, grant temporary access, revoke access, transfer responsibility, and understand who can currently see or do what.
**What they need to trust:** Assignment changes do not create privilege escalation, orphaned work, or hidden data exposure.
**What they should not need to understand:** Event-envelope exclusions, projection rebuilds, or low-level sync mechanics.

#### Persona D — Regional lead or higher-level manager

**Operational surface:** Oversight / coordination
**Real-world responsibility:** See across broader areas, teams, or activities.
**Work context:** More online, responsible for aggregate progress and exceptions.
**Primary intent:** Understand operational status across a broader scope without manually collecting reports.
**What they need to see or do:** View scoped summaries, identify gaps, follow escalation paths, and inspect details where authorized.
**What they need to trust:** Aggregate visibility does not hide unresolved issues or expose data beyond allowed scope.

#### Persona E — Auditor or external reviewer

**Operational surface:** Audit / external review
**Real-world responsibility:** Verify that work was done correctly and that access rules were followed.
**Work context:** Periodic, targeted, or time-limited; may cut across normal organizational hierarchy.
**Primary intent:** Inspect records, responsibility history, and authority at the time of action.
**What they need to trust:** The platform can show who did what, under which role, in which context, without relying only on current permissions.
**Platform authority mapping:** Not assumed as settled. Auditor/query access must be routed separately if it exceeds assignment-derived access.

#### Persona F — Temporary cover / campaign worker

**Operational surface:** Field execution / emergency or campaign work
**Real-world responsibility:** Act outside ordinary standing responsibility for a limited reason and period.
**Work context:** Often under time pressure; may be offline before the temporary grant or revocation syncs.
**Primary intent:** Receive temporary authority clearly, act during the allowed window, and stop seeing/doing that work when the reason ends.
**What they need to trust:** Work done during valid temporary access remains attributable after access ends.

## 3. User intent

### 3.1 Jobs to be done

**Field worker job:**
When I open the platform, I need to see the work, subjects, and records relevant to my responsibility, so that I can do my job without seeing unrelated or sensitive information.

**Supervisor job:**
When I manage a team, I need visibility into their work and authority to review or act where appropriate, so that I can maintain quality and accountability.

**Coordinator job:**
When responsibilities change, I need to grant, change, or revoke access safely, so that work continues without exposing data or losing accountability.

**Regional lead job:**
When I oversee a broader area, I need scoped visibility into progress and exceptions, so that I can coordinate without becoming a system administrator for every detail.

**Auditor job:**
When I review past work, I need to know whether the actor had authority at the time of action, so that I can judge compliance and traceability.

**Temporary worker job:**
When I cover for someone or join a campaign, I need temporary access to the relevant work, so that I can act without permanent expansion of authority.

### 3.2 Intent categories

* See only relevant work
* Act only where responsible
* Preserve accountability
* Support supervision
* Support temporary access
* Support role changes
* Support hierarchy with exceptions
* Work offline under last-known authority
* Reconcile stale authority later
* Avoid overexposure of sensitive information
* Keep simple access understandable

### 3.3 Success from the user’s point of view

**Field worker success:**
“I see what I need for my area or assignment, and I do not see unrelated work.”

**Supervisor success:**
“I can see and review my team’s work without asking for manual exports.”

**Coordinator success:**
“I can change responsibility without creating a hidden mess.”

**Auditor success:**
“I can reconstruct whether the person acted under the right authority at the time.”

**Organization success:**
“People have enough access to work, but not more than they should.”

## 4. Real-world journey

### 4.1 Simple standing assignment path

1. Coordinator assigns a field worker to a defined area, subject set, or activity.
2. The field worker receives the relevant work and records on sync.
3. The worker captures or updates records within that responsibility.
4. Supervisor sees the worker’s submitted work within the supervisor’s own scope.
5. Coordinator can later inspect responsibility and progress.

### 4.2 Context-specific authority path

1. A person has one role in one activity and another role in a different activity.
2. In one context they may capture records.
3. In another context they may only view or review.
4. The platform must avoid treating the person’s role as global.
5. The user experience should make the current context clear enough that the actor knows what they can do.

### 4.3 Temporary coverage path

1. A worker is absent or a campaign starts.
2. Coordinator grants another actor temporary access.
3. The temporary actor syncs and receives the relevant working set.
4. The actor performs work during the coverage period.
5. The temporary grant ends.
6. New work should no longer be allowed under that temporary access.
7. Work done during the valid period remains on record and attributable.

### 4.4 Role or responsibility change path

1. A worker is promoted, transferred, or leaves the program.
2. Their old responsibilities must not disappear.
3. Some work may be handed off to another actor.
4. Some historical records remain attributed to the original actor.
5. The new actor needs enough context to continue.
6. The organization needs to know who was responsible before, during, and after the change.

### 4.5 Hierarchical visibility path

1. Field worker sees assigned work.
2. Supervisor sees work across their team.
3. Regional lead sees work across multiple supervisors.
4. National coordinator sees broader operational status.
5. Exceptions may exist: cross-regional coordinator, temporary campaign lead, or auditor.
6. Exceptions must not collapse the whole model into “admin sees everything.”

### 4.6 Offline stale-authority path

1. Field worker syncs and receives their current authority and working set.
2. The worker goes offline.
3. Centrally, the worker’s assignment is revoked, contracted, or changed.
4. The worker continues acting under last-known authority.
5. The worker later reconnects.
6. The work is preserved but may be flagged as stale-authority or out-of-date responsibility.
7. Reviewers need to decide whether the work remains valid, requires correction, or should be excluded from downstream action.

### 4.7 Sensitive visibility path

1. Organization has records or subjects with elevated sensitivity.
2. Some actors may see routine records but not sensitive categories.
3. Supervisors or auditors may need broader visibility under controlled conditions.
4. The access model must support meaningful partitioning without field-level sensitivity becoming arbitrary per-field access logic.
5. Users need simple language: “you do not have access to this record,” not architecture terms.

### 4.8 Aggregate oversight path

1. A manager wants to see progress across a broad scope.
2. The manager may not need every underlying detail.
3. Reports must avoid leaking sensitive or unauthorized data.
4. If aggregate visibility differs from detailed visibility, that becomes an explicit unresolved access question.
5. The product must make freshness and unresolved issues visible.

## 5. Domain-neutral artifacts

| Artifact                      | Purpose                                                     | Created by                                          | Used by                                     | Changes over time?              | Trust requirement                                    |
| ----------------------------- | ----------------------------------------------------------- | --------------------------------------------------- | ------------------------------------------- | ------------------------------- | ---------------------------------------------------- |
| Actor profile                 | Identifies a person or system actor                         | Coordinator/admin or provisioning process           | All operational surfaces                    | Yes                             | Must separate identity from authority                |
| Assignment                    | Grants responsibility over a role, scope, and time window   | Coordinator/admin or authorized actor               | Field worker, supervisor, sync/access views | Yes                             | Must be traceable and bounded                        |
| Role-action matrix            | Defines what actions a role may perform                     | Platform/spec/config surface                        | Coordinator, validation, review             | Yes                             | Must stay bounded and understandable                 |
| Scope definition              | Defines where or over what subjects/activity access applies | Coordinator/admin within platform-fixed scope types | Field worker, supervisor, coordinator       | Yes                             | Must avoid arbitrary custom containment logic        |
| Temporary access grant        | Gives time-limited responsibility                           | Coordinator/admin                                   | Temporary worker, campaign lead, reviewer   | Yes                             | Must end cleanly and preserve historical attribution |
| Assignment timeline           | Reconstructs authority at a point in time                   | Projection/read model                               | Auditor, coordinator, supervisor            | Yes                             | Must not be stored as event-envelope truth           |
| Visibility view               | Shows what a person can see                                 | Projection/read model                               | Field worker, supervisor, coordinator       | Yes                             | Must match authorized data delivery                  |
| Action availability indicator | Shows whether a user can perform an action in context       | Product surface                                     | Field worker, reviewer, supervisor          | Yes                             | Must not require understanding internal rules        |
| Access-change notice          | Explains that responsibility changed                        | Coordinator/system/product surface                  | Field worker, supervisor                    | Yes                             | Must be clear and timely where possible              |
| Stale-authority flag          | Marks work done under outdated local authority              | Platform/runtime service                            | Supervisor, coordinator, auditor            | Yes, until resolved or accepted | Must preserve the work while surfacing uncertainty   |
| Audit trail                   | Shows who did what under which responsibility               | Derived from events and assignments                 | Auditor, coordinator                        | Yes                             | Must be reconstructable from durable facts           |
| Scoped report                 | Shows summary within allowed access                         | Reporting projection                                | Supervisor, regional lead, coordinator      | Yes                             | Must not bypass access boundaries                    |

## 6. Concrete domain exemplar

### Exemplar: District health outreach program

**Domain:** Health/service delivery
**Organization:** District program with community workers, supervisors, district coordinators, regional leads, and external auditors
**Concrete artifact names:** Worker assignment list, village coverage map, temporary campaign roster, supervisor review queue, audit access request
**Concrete user language:** “This worker covers these villages this month,” “this supervisor reviews this team,” “this auditor can inspect campaign records for two weeks.”

### Concrete journey

1. Coordinator assigns community workers to villages.
2. Each worker syncs and receives the relevant households, facilities, or activity forms for their assigned villages.
3. Supervisor sees work from the workers they supervise.
4. A vaccination campaign starts.
5. Some workers receive temporary access to additional villages.
6. One worker goes offline before the campaign access is revoked.
7. The worker captures records under the last-known campaign access.
8. On sync, the records remain traceable but may require stale-authority review.
9. An external auditor later receives targeted access to inspect campaign records.
10. The audit path must not silently bypass the normal access model.

### Where this example may mislead platform design

* It may imply health-specific roles; the scenario is domain-neutral.
* It may imply strict hierarchy is always enough; exceptions are common.
* It may imply auditors are settled in the baseline; auditor/query access remains a visible routed lane if it exceeds assignment-derived access.
* It may imply field-level sensitivity; accepted boundaries keep sensitivity at shape/activity level.
* It may imply online-only authority; field work must continue offline under last-known access, then reconcile later.

## 7. Platform fit under current accepted architecture

| User need                                         | Likely settled platform vocabulary                          | Fit assessment                                   |
| ------------------------------------------------- | ----------------------------------------------------------- | ------------------------------------------------ |
| Actor identity remains stable across role changes | `actor`, typed identity reference                           | Strong fit                                       |
| Responsibility is contextual                      | `assignment`, role, scope, time                             | Strong fit                                       |
| Access depends on scope containment               | assignment-based access, scope-containment test             | Strong fit                                       |
| Device only receives authorized data              | sync scope = access scope                                   | Strong fit                                       |
| Authority can be reconstructed later              | authority-as-projection, assignment timeline                | Strong fit                                       |
| Avoid authority stored in every event             | no `authority_context`, no `assignment_ref` envelope field  | Strong fit                                       |
| Temporary access                                  | time-bounded assignments                                    | Good fit, platform-spec details open             |
| Role changes                                      | assignment timeline and append-only changes                 | Good fit, operational policy open                |
| Offline stale authority                           | accept-and-flag, authorization flags                        | Strong fit                                       |
| Hierarchical visibility                           | scoped assignments and role-action rules                    | Good fit, exact role-action table open           |
| Auditor access                                    | routed decision lane if beyond assignment-derived access    | Not baseline-accepted                            |
| Aggregate visibility                              | routed decision lane if aggregate access differs from detail access | Not baseline-accepted                    |
| Actor-as-subject visibility                       | routed decision lane                                        | Not baseline-accepted                            |
| Per-field sensitive visibility                    | rejected as baseline                                        | Not allowed without formal architecture decision |

### 7.1 Current standing caveat

Accepted standing supports assignment-derived visibility, scope-filtered sync,
production principal-to-actor binding, assignment-admin create/end command
capabilities, and shared-device actor partitions. It does not support authority
from IdP groups/claims, request-body actor IDs, UI-selected actors, or
activity-role promotion of assignment-admin commands.

Simple current-scope auditor visibility can be modeled as ordinary assignments.
Broad audit/history reads, cross-axis query access, emergency/special write
bypasses, custom/query scope, aggregate access beyond detail access, and
no-local-retention/redacted views remain visible routed lanes that need
decision, planning, evidence, and change control before implementation.

## 8. Fit assessment

### 8.1 Strong fit

The accepted architecture supports the core access pressure: authority is contextual, assignment-based, time-aware, projection-derived, and coupled to sync. This matches the operational need that users see only the data they are allowed to hold, especially offline.

### 8.2 Weak fit

The product and platform-spec surfaces are not yet proven. Users need simple operational language such as “assigned area,” “my team,” “temporary coverage,” “review access,” and “audit access.” They should not need to understand assignment timelines, scope containment, or authority projections.

### 8.3 Missing evidence

* What are the first deployment’s simplest roles?
* Which actions must be controlled separately?
* Do early deployments start with geographic scope, subject-list scope, activity scope, or a combination?
* How often is temporary access needed?
* How long can temporary access remain active?
* Who is allowed to grant temporary access?
* How often do users change roles or transfer areas?
* What happens to work in progress during transfer?
* Do supervisors need detail records, summaries, or both?
* What audit access is required in real deployments?
* Can auditors operate through normal assignments, or do they require a separate access model?
* Do aggregate reports inherit event-level access, or can they show broader summaries?
* How sensitive are early subject categories?
* What should the user see when access is revoked but offline work still exists locally?

### 8.4 Risk of false fit

The architecture may be correct while the product still feels confusing. If users cannot understand why a record appears, why an action is disabled, or why a temporary assignment ended, they will treat the system as unreliable. If coordinators cannot see the effects of access changes before deployment, they may overgrant access or create orphaned work.

## 9. Gap routing

### Gap 1 — Exact role-action table

**Short name:** Role-action table
**Classification:** Platform-spec detail gap
**Baseline-extension category:** Not applicable
**Current owner or likely decision path:** Platform-spec detailing
**Baseline item affected:** Assignment-based access under platform-fixed access logic
**Why it is still open:** The architecture settles role-scoped assignment access for activity work actions, but not the exact artifact that defines which actions each role can perform. Assignment-admin create/end is a separate platform-owned command-capability boundary under IDR-029/NW-050.
**Closure path:** Platform-spec detailing
**Evidence needed before closure:** Early deployment role/action examples from field workers, supervisors, coordinators, and auditors.

### Gap 2 — Access setup language

**Short name:** Access setup vocabulary fit
**Classification:** Product/problem evidence gap
**Baseline-extension category:** Not applicable
**Current owner or likely decision path:** Product discovery / scenario thickening
**Baseline item affected:** User-facing language for assignments, roles, scopes, and temporary grants
**Why it is still open:** Architecture vocabulary is not necessarily user vocabulary. Users may speak in terms of teams, areas, programs, coverage, permissions, duties, responsibilities, or access.
**Closure path:** Product discovery / scenario thickening
**Evidence needed before closure:** Real examples of responsibility assignment artifacts from target organizations.

### Gap 3 — Temporary access lifecycle

**Short name:** Temporary access lifecycle
**Classification:** Platform-spec detail gap
**Baseline-extension category:** Not applicable
**Current owner or likely decision path:** Platform-spec detailing, with operational policy support
**Baseline item affected:** Assignment lifecycle, time-bounded authority, stale-authority flags
**Why it is still open:** The architecture supports time-bounded authority, but user-facing grant, expiry, revocation, notification, and stale-offline handling are not specified.
**Closure path:** Platform-spec detailing
**Evidence needed before closure:** Examples of coverage, campaign, and emergency access.

### Gap 4 — Role/responsibility transfer

**Short name:** Responsibility transfer and handoff
**Classification:** Operational policy gap
**Baseline-extension category:** Not applicable
**Current owner or likely decision path:** Operational policy definition, with platform-spec support
**Baseline item affected:** Assignment timeline and work continuity
**Why it is still open:** The platform can preserve assignment changes, but the organization must decide who hands off work, when, and what context transfers.
**Closure path:** Operational policy definition
**Evidence needed before closure:** Transfer, leave, promotion, and offboarding practices from real deployments.

### Gap 5 — Auditor/query access

**Short name:** Auditor/query access
**Classification:** Architecture decision gap
**Baseline-extension category:** Visible routed lane; not baseline-accepted
**Current owner or likely decision path:** NW-051/NW-053-style successor decision with product/security evidence support
**Baseline item affected:** Assignment-based access and sync/access equivalence
**Why it is still open:** Auditor visibility may cut across normal hierarchy. If it can be represented as assignment-derived access, platform-spec may suffice. If it requires query-scope authority or access beyond assignment-derived data, it needs a formal architecture decision.
**Closure path:** Ordinary time-bounded assignments if sufficient; successor decision if broad audit/history or query scope is needed
**Evidence needed before closure:** Concrete audit access needs and whether they can be represented by time-bounded scoped assignments.

### Gap 6 — Aggregate access semantics

**Short name:** Aggregate visibility boundary
**Classification:** Architecture decision gap if aggregate visibility exceeds event/detail access; otherwise platform-spec detail gap
**Baseline-extension category:** Visible routed lane; not baseline-accepted
**Current owner or likely decision path:** NW-044/formal reporting decision or platform-spec detailing depending on chosen visibility rule
**Baseline item affected:** Reporting projections and sync scope equals access scope
**Why it is still open:** Aggregate summaries beyond inspectable detail data need explicit product/security evidence and routing before implementation.
**Closure path:** NW-044/formal decision if aggregate access diverges or needs durable report APIs/export; platform-spec detailing if it inherits event-level access.
**Evidence needed before closure:** Oversight reporting examples by role and sensitivity level.

### Gap 7 — Actor-as-subject visibility

**Short name:** Actor-as-subject access
**Classification:** Architecture decision gap
**Baseline-extension category:** Visible routed lane; not baseline-accepted
**Current owner or likely decision path:** Formal architecture decision after product/problem evidence
**Baseline item affected:** Actor/subject separation and assignment-based access
**Why it is still open:** Some records may be about the actor personally. Any access grant based on being the subject, rather than assignment-derived access, needs explicit routing before implementation.
**Closure path:** Formal architecture decision
**Evidence needed before closure:** Real cases where workers need access to records about themselves.

### Gap 8 — Stale authority review behavior

**Short name:** Stale authority review
**Classification:** Platform-spec detail gap
**Baseline-extension category:** Not applicable
**Current owner or likely decision path:** Platform-spec detailing
**Baseline item affected:** Accept-and-flag for authorization staleness
**Why it is still open:** The architecture accepts and flags stale-authority events, but does not specify the user-facing review, severity, or resolution path.
**Closure path:** Platform-spec detailing
**Evidence needed before closure:** Examples of work performed after revocation, scope contraction, temporary grant expiry, or role change.

### Gap 9 — Access change communication

**Short name:** Access change communication
**Classification:** Product/problem evidence gap
**Baseline-extension category:** Not applicable
**Current owner or likely decision path:** Product discovery / scenario thickening
**Baseline item affected:** User confidence and operational continuity
**Why it is still open:** The architecture can change access on sync, but users need understandable signals about what changed and what they can now do.
**Closure path:** Product discovery / scenario thickening
**Evidence needed before closure:** Field evidence on how workers currently learn about responsibility changes.

### Gap 10 — Sensitive category operating model

**Short name:** Sensitive category operating model
**Classification:** Platform-spec detail gap, escalating to architecture decision gap if field-level sensitivity is required
**Baseline-extension category:** Not applicable unless regulatory controls exceed current baseline
**Current owner or likely decision path:** Platform-spec detailing under shape/activity sensitivity; NW-054/BAR-106 and formal routing if field-level controls, redaction, encryption, or no-local-retention views are required
**Baseline item affected:** Shape/activity-level sensitivity classification
**Why it is still open:** Sensitivity is supported at shape/activity level, but real deployments may require specific operating guidance for standard, elevated, and restricted categories.
**Closure path:** Platform-spec detailing for shape/activity sensitivity; NW-054/BAR-106 or formal decision for field-level/redaction/retention behavior
**Evidence needed before closure:** Sensitivity categories and access examples from early deployments.

## 10. Output decision

**Current packet status:** Ready to feed synthesis; unresolved access surfaces remain visible planning lanes before implementation.

**Reason:**
The access architecture is strongly aligned with the operational need, but role-action artifacts, temporary access lifecycle, stale-authority review, access setup language, auditor access, aggregate visibility, and actor-as-subject delivery need more evidence or formal routing before specification.

## 11. Acceptance criteria for downstream platform-spec work

A routed Access Control and Visibility platform-spec section should be accepted only if it satisfies:

1. A user’s identity is separate from their authority.
2. Authority is contextual by role, scope, activity, and time.
3. A field device receives only data the actor is authorized to hold.
4. Access changes are traceable over time.
5. Temporary access can be granted and ended without losing historical attribution.
6. Role or responsibility changes do not orphan work.
7. Offline work under stale authority is preserved and surfaced for review.
8. Users can understand access outcomes without architecture vocabulary.
9. Coordinators can see the effect of assignment changes before or after deployment.
10. Auditor access is not assumed beyond the accepted assignment model unless formally decided.
11. Aggregate reporting does not bypass access rules without formal decision.
12. Field-level sensitivity is not introduced through product or spec language without formal architecture decision.
13. Basic S00 capture remains simple under the access model.
