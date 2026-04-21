> **⚠️ SUPERSEDED** — This is a raw exploration document archived for reference. Decisions were finalized in **ADR-003**. For a navigated reading guide, see [guide-adr-003.md](../guide-adr-003.md).
# ADR-003 Phase 2: Authorization & Sync Stress Test

> **Phase**: 2 of 3 — Adversarial Stress Test
> **Method**: Threat-modeling methodology applied to architectural mechanisms
> **Date**: 2026-04-11
> **Input**: Phase 1 policy enforcement scenarios (`10-adr3-phase1-policy-scenarios.md`)
> **Author**: Security Engineer

---

## 1. Mechanisms Tested

Phase 1 proposed five mechanisms. This document treats each as guilty until proven innocent.

| Mechanism | Phase 1 Claim | Core Risk |
|-----------|--------------|-----------|
| **A: Assignment-Based Access Control** | Every access rule reduces to a single scope-containment + role check against active Assignments | Insufficient expressiveness; privilege escalation via assignment manipulation; concurrent assignment conflicts |
| **B: Sync Scope Determination** | Sync scope = access scope; server computes the delta at sync time | Scale collapse; bandwidth failure on scope expansion; data exposure during scope contraction |
| **C: Stale Rule Reconciliation** | Authorization staleness is handled by the same accept-and-flag mechanism as identity staleness | Flag volume collapse; policy-fire-before-flag on role violation; data sensitivity vs. immutability |
| **D: Projection Location (Tiered)** | Field workers: device-local; supervisors: hybrid; coordinators: server | Stale supervisor assessments; projection rebuild cost; double-lag visibility at coordinator tier |
| **E: Authority Context in Event Envelope** | Every event carries `authority_context` with assignment_ref(s) and optional process_ref | Envelope bloat; variable-length lists in immutable schema; stale assertions vs. provable facts |

**Pre-test stance**: Phase 1 is optimistic about the uniformity of accept-and-flag across all staleness types. The security threat model adds a concern Phase 1 did not: some staleness types (sensitive-subject access, role degradation affecting clinical validity) have downstream harm beyond a flag in a queue. The stress test will probe whether "always accept, always flag, let a human decide" is architecturally safe for all staleness categories or whether a subset requires pre-sync interception.

---

## 2. Mechanism A: Access Rule Representation

### Finding A1: The Assignment Model Cannot Express All 13 Phase 1 Hot Spots Without Extensions

**Attack path**: Show that the single-check model (`actor.assignment.scope ⊇ subject.location AND actor.assignment.role permits action`) is insufficient for at least one of the 13 hot spots, requiring a structural extension to the assignment primitive.

**Setup**: Walk through each hot spot against the minimal assignment model.

| HS | Hot Spot | Does assignment-based model handle it? | Gap / Extension required |
|----|---------|---------------------------------------|-------------------------|
| HS-1 | Subject history completeness on reassignment | **YES** — scope-based sync delivers all events for in-scope subjects | None structural; operational policy needed for initial sync budget |
| HS-2 | Assessment visibility to assessed CHV | **NO** — "about me as subject" is a second sync dimension orthogonal to geographic scope | Requires a second assignment type: **actor-as-subject scope** — "I can see events whose subject_ref references my own actor_id." Not expressible as geographic containment. |
| HS-3 | Data removal on scope narrowing | **NO** — the assignment model says what to deliver, not what to remove from devices already holding data | Requires a **data lifecycle policy** attached to assignments — independent of the access control check |
| HS-4 | Supervisor projection computation location | **PARTIALLY** — scope defines what data exists; location of computation is a separate concern | Requires a **device capability tier** attribute on the actor, not expressed in the assignment |
| HS-5 | Stale-scope flag severity | **YES** — handled by the flag taxonomy, not by the assignment model itself | None |
| HS-6 | Scope transition atomicity | **NO** — the assignment model creates events (AssignmentEnded + AssignmentCreated) but cannot guarantee their delivery atomicity | Requires a **sync protocol guarantee**: assignment transition events and initial scope data must be bundled in a single sync payload unit |
| HS-7 | Scope composition (standing + campaign) | **STRAINS** — union of two assignments' scopes is expressible, but which assignment_ref goes in the event envelope is ambiguous when both scopes cover the subject | Requires a **tagging rule**: when both assignments cover the subject, the event carries the more-specific (narrower) assignment reference. This rule must be explicit. |
| HS-8 | Temporal access enforcement | **YES** — the assignment model has temporal bounds; enforcement is server-side on sync | None structural; acknowledged limitation |
| HS-9 | Grace period for expired authority | **YES** — auto-resolution policy (Mechanism C strategy) handles this | None |
| HS-10 | Cross-level visibility boundaries | **STRAINS** — the distribution event granularity (one event vs. per-district events) determines whether districts see each other's allocations; the assignment model alone doesn't decide this | Requires a **visibility scope** attribute on Process events: "this process event is readable by actors in scope X" |
| HS-11 | Upward visibility aggregation | **NO** — the assignment model determines who has authority, not how data is aggregated across levels | Requires a **projection tier rule** independent of the assignment model |
| HS-12 | Scope-crossing merge | **BREAKS** — when alias resolution maps a subject outside the current worker's scope, the assignment model has no resolution. The worker's events reference a subject they were authorized to access; the alias points to one they weren't. | Requires an **alias-respects-original-scope** rule: the event's authorization is evaluated against the original subject_id's scope at the time of creation, not the surviving subject_id's scope. |
| HS-13 | Orphaned conflicts | **STRAINS** — resolver designation is derived from scope hierarchy; reassignment of the resolver changes the derived designation | Requires an **explicit re-designation** event (ConflictResolverReassigned) rather than automatic re-derivation |

**Critical gap — HS-2**: The assignment model is geographic-primary. "Events about me as subject" (supervisor assessments of CHVs, case management follow-ups targeting a specific individual) requires a second access dimension: **actor-as-subject visibility**. This cannot be expressed as geographic containment. Example: CHV-A is geographically in Zone 3. The supervisor creates an assessment whose subject_ref is CHV-A's actor_id. CHV-A's sync scope (geographic) does not include "events that reference my own actor_id." Under the current model, CHV-A never receives their own assessments.

**Critical gap — HS-12**: Scope-crossing merge is an authorization-correctness failure, not just an operational inconvenience. Worker creates events about Subject S1 (authorized — in their scope). S1 merges into S2 (outside their scope). After alias resolution, the worker's events project into S2's stream — a stream the worker was never authorized to contribute to. The flag exists (StaleReferenceFlag from ADR-002), but the scope-crossing dimension is not represented in the flag metadata. The resolver may approve the events without realizing they've been projected into an unauthorized scope.

**Verdict**: **STRAINS** on HS-7, HS-10, HS-13. **BREAKS** on HS-2, HS-3, HS-6, HS-11, HS-12.

The assignment model is correct as a foundation but requires five structural extensions before it can serve as a complete authorization model. These are not edge cases — HS-2 affects the core supervisor-CHV relationship; HS-12 affects any deployment that performs subject merges.

**Required additions**:
1. **Actor-as-subject visibility**: A second assignment dimension granting an actor read access to events where they are the referenced subject.
2. **Data lifecycle policy**: Attached to assignments; governs what happens to data on device when assignment ends.
3. **Sync atomicity guarantee**: Assignment transition + initial scope data delivered as a single atomic sync unit.
4. **HS-7 tagging rule**: When multiple assignments cover the same subject, the event carries the narrower (more specific) assignment_ref.
5. **Alias-respects-original-scope rule**: Authorization of an event is evaluated against the subject's scope at the time of event creation, not the post-merge surviving scope.

---

### Finding A2: Assignment Scale Is Manageable, But Role Hierarchy Enforcement Has a Gap

**Setup**: Compute the minimal assignment set for S20+S21 and evaluate server-side evaluation cost at scale.

**Minimal assignment set per actor tier:**

| Actor | Assignments needed | Typical count |
|-------|------------------|---------------|
| CHV (field_worker) | 1 standing geographic assignment | 1 |
| CHV enrolled in 1 campaign | 1 standing + 1 campaign | 2 |
| Supervisor | 1 standing geographic assignment covering their CHV set | 1 |
| District coordinator | 1 standing assignment per district | 1 |
| Coordinator running a campaign | 1 standing + 1 campaign process ownership | 2 |

**Scale calculation**: 10,000 workers × avg 1.5 assignments = 15,000 active assignments at any time. Each assignment is an event (~500 bytes): 15,000 × 500 bytes = 7.5 MB of assignment state on the server. This is trivial.

**Server evaluation at sync**: For each sync, the server evaluates: "What is the current sync scope for this device?" This requires: (1) load actor's active assignments (1 DB query), (2) resolve geographic scope to subject IDs (1 index scan), (3) compute event delta since sync_watermark (1 range scan per subject). At 1,000 concurrent syncs: 1,000 × (1 + N_subjects + N_subjects) queries, where N_subjects ≈ 50. That's 1,000 × 101 = 101,000 DB operations. On a properly indexed server, this is 10–30 seconds of aggregate DB time — borderline but feasible with connection pooling and read replicas.

**Role hierarchy enforcement gap**: The assignment model stores role as an attribute of the Assignment event: `{actor_id, scope, role: "field_worker", ...}`. There is no platform-level role hierarchy defined. The model states that "a field_worker cannot create supervisor assessments" — but who enforces this? The device-local check compares the actor's role from their local assignment event against a rule encoded... where? In the form definition? In platform code?

Walk-through of the gap:
1. CHV-A's assignment event says `role: "field_worker"`.
2. CHV-A opens an assessment form (somehow obtained — perhaps shared by another worker or present from a prior supervisor role).
3. The device checks: "does actor's role permit this action type?" But the permitted actions per role are not in the assignment event — they're in the platform's role definition.
4. If role definitions are delivered as configuration (ADR-004 territory), they must be on-device and current. If they're hard-coded, CHV-A on an older app version may have stale role definitions.

**Verdict**: **HOLDS** for scale. **STRAINS** for role hierarchy enforcement — the mechanism works if role permission tables are delivered as sync'd configuration, but this dependency on ADR-004 is not stated explicitly, creating a hidden coupling.

---

### Finding A3: A Misconfigured Coordinator Can Escalate Scope Beyond Their Authority

**Attack path**: Coordinator C1 (authorized for District A) creates an assignment granting Worker W access to subjects in District B (outside C1's authority).

**Walk-through**:
1. Coordinator C1 has assignment: `{actor: C1, scope: {district: "A"}, role: "coordinator"}`.
2. C1 issues command: `CreateAssignment{actor: W, scope: {district: "B"}, role: "field_worker", created_by: C1}`.
3. The server receives this command. **What validates it?**

Phase 1 states: "Coordinators can only assign workers within their own scope — a district coordinator cannot assign workers into a different district." But the mechanism for enforcing this is not specified.

**Server-side validation required**: The server must check `scope_of_new_assignment ⊆ scope_of_creating_coordinator` before writing the AssignmentCreated event. If this check is absent or bypassed, C1 has granted access to District B without authorization.

**The gap is structural**: The server-side validation is described as a consequence of the model, but the assignment-based model as proposed has no self-enforcing property here. The Assignment event does not carry a reference to the creating coordinator's scope. A malicious or misconfigured coordinator creates a valid-looking assignment event that the sync engine will dutifully deliver to Worker W's device, giving W access to District B's subjects.

**Worst case**: A compromised coordinator account (credentials stolen, insider threat) issues assignments that span the entire national scope. Every worker who receives such an assignment gets national scope on their device. The sync engine faithfully delivers all events for all subjects in the national scope. Data exposure is massive and silent until the assignment is discovered and revoked.

**Mitigation required**: The `AssignmentCreated` command must be validated server-side with an explicit check: `new_assignment.scope ⊆ creating_actor.assignment.scope`. This is not optional — without it, the authorization model has a lateral privilege escalation vector at the coordinator tier. The validation must be part of the aggregate's invariants (like ADR-002's acyclicity constraints for merges), not an application-layer convention.

**Verdict**: **BREAKS** — the assignment model as described does not prevent a coordinator from creating out-of-scope assignments. A mandatory server-side scope-containment invariant on AssignmentCreated is required.

---

### Finding A4: Concurrent Conflicting Assignments Lack a Winner Rule

**Attack path**: Two coordinators (C1 and C2) simultaneously create conflicting assignments for Worker W.

**Walk-through**:
1. C1 and C2 both have authority over District A (unusual but possible during leadership transition).
2. At 10:00 AM, C1 creates: `AssignmentCreated{actor: W, scope: Zone_3_of_A, role: field_worker}` (server state version V200).
3. At 10:01 AM, C2 creates: `AssignmentCreated{actor: W, scope: Zone_7_of_A, role: field_worker}` (server state version V201).

Both are valid. Worker W now has two active assignments — both are field_worker roles, different scopes. This is actually fine if assignments compose (Phase 1 EQ-3: effective scope = union of all active assignments). Worker W gets access to both Zone 3 and Zone 7. No conflict.

**The real conflict case**: C1 creates an assignment with `role: field_worker`. C2 simultaneously creates an assignment for the same worker with `role: supervisor`. Now Worker W has two simultaneous roles. The scope check for "can this actor create a supervisor assessment" would pass because one of their assignments grants supervisor role.

**Is this resolvable by the assignment model?** Only if assignments are role-exclusive — a worker can hold at most one active role at a time. But Phase 1 EQ-3 says "multiple simultaneous assignments" are explicitly supported (campaign overlay). If roles can stack, the privilege escalation is by design. If roles cannot stack, the invariant must be: "an actor may have at most one active assignment per role tier, and role tiers must be compatible."

**Verdict**: **STRAINS** — the model works for geographic scope composition but is undefined for concurrent conflicting role grants. A role-incompatibility invariant is needed to prevent unintended privilege accumulation.

---

### Finding A5: Device-Side Role Enforcement Is Bypassable on Rooted Android

**Attack path**: CHV-A's device is rooted. They modify the local assignment event to change their role from `field_worker` to `supervisor`. The device-local check now passes for supervisor-only actions.

**Walk-through**:
1. The Assignment event is stored in local SQLite (or equivalent). On a rooted device, this is readable and writable.
2. CHV-A modifies: `role: "field_worker"` → `role: "supervisor"` in the local assignment record.
3. CHV-A opens a supervisor assessment form. The device-local check passes.
4. CHV-A creates a supervisor assessment event. The event's authority_context carries the assignment_ref — pointing to the (now-modified) assignment.
5. On sync, the server receives the assessment event and the authority_context's assignment_ref.

**Server-side detection**: The server looks up the assignment by its UUID (the assignment_ref). The server's copy of the assignment still says `role: field_worker`. The event's claimed role (implied by the form type: "supervisor assessment") conflicts with the assignment's actual role. This is detectable: the server sees an action that requires supervisor role from an actor whose stored assignment is field_worker.

**But detection requires the server to check role-action compatibility for EVERY incoming event.** Does the current model require this? The Conflict Detector (ADR-002 S12) runs before policies. But the Conflict Detector is specified to detect concurrent state changes and stale references — not role-action mismatches. A `RoleViolationFlag` would need to be a new conflict type.

**More subtle attack**: CHV-A doesn't modify the assignment. Instead, they modify the local UI code (or abuse a debug interface) to submit a supervisor assessment event whose `type` field says `observation_recorded` instead of `supervisor_assessment`. The event type bypasses the role check. The server receives it as an observation. The data (supervisor assessment content) is preserved but misclassified. This is not an authorization failure — it's a data integrity failure — but the outcome may be similar: unqualified workers performing actions they shouldn't.

**Verdict**: **STRAINS** — device-side enforcement is the last line of defense but not the only line. The server must perform role-action compatibility checks on every incoming event, independent of the device's claims. This is an additional server-side validation that the current model does not explicitly specify.

---

## 3. Mechanism B: Sync Scope Determination

### Finding B1: Server-Side Sync Computation Scales Conditionally

**Attack path**: 1,000 workers simultaneously syncing after returning to connectivity; demonstrate whether the server can sustain this.

**Computation model for one sync**:
1. Load actor's active assignments: 1 query → returns 1–2 rows
2. Resolve assignments to geographic scope → subject list: 1 index scan → returns ~50 subject IDs
3. For each subject, find events since sync_watermark: 50 range queries
4. Serialize and stream events to device

**Per-sync DB operations**: ~52 queries. At 1,000 concurrent syncs: 52,000 queries.
- Modern PostgreSQL with connection pooling: ~5,000 queries/second sustained → 10–11 seconds to process the batch
- With read replicas and proper indexing: feasible

**But the sync_watermark is a server-state version counter, not a timestamp.** If the server processes events at high volume (10,000 workers × 10 events/day = 100,000 events/day → ~1 event/second), the watermark increments rapidly. A worker offline for 7 days has a watermark 700,000 positions behind. The delta scan for 50 subjects over 700,000 watermark positions may scan millions of index rows.

**Concrete worst case**: Worker offline 30 days. 10,000 workers × 10 events/day × 30 days = 3,000,000 events generated in the system during that period. The 50 subjects in scope have received perhaps 200 events each = 10,000 events to transfer. At 1KB per event: 10 MB per sync. On 2G (20–40 kbps effective): 33–67 minutes per sync.

This is not a sync computation problem — it's a **bandwidth problem**. The server can compute the delta quickly; the bottleneck is transfer speed to the device on 2G.

**Verdict**: **HOLDS** for computation at scale. **STRAINS** for bandwidth on extended offline periods — the model is correct but the user experience for workers returning after long disconnection is severely degraded. A **priority sync** strategy (recent events and assignment changes first; backfill historical events in background) is essential but not specified in the model.

---

### Finding B2: Mass Reassignment Creates an Assignment Event Storm

**Setup**: Coordinator restructures District D, reassigning 200 subjects from Area A to Area B. This affects 20 CHVs in Area A (lose subjects) and 15 CHVs in Area B (gain subjects).

**Event generation**:
- For each of the 200 subjects: an `AssignmentChanged` or equivalent event to reflect the new geographic categorization: 200 events
- For each of the 20 Area A CHVs: their effective sync scope changes (the 200 subjects leave their scope): no new assignment events needed if scope is derived from geography, but the next sync must deliver the updated state
- For each of the 15 Area B CHVs: their scope expands; they must receive all events for the 200 subjects (200 subjects × ~50 events = 10,000 events)

**On next sync for Area B CHVs**: Each of 15 CHVs receives 10,000 events → 10MB each at 1KB/event. On 2G: 25–50 minutes per CHW. 15 CHWs, but syncs are staggered: total bandwidth burden is per-CHW, not aggregate.

**On next sync for Area A CHWs**: The 200 subjects are no longer in scope. The server stops delivering new events for those subjects. The CHW already has the historical events — per B4 (HS-3), what happens to them is a policy decision, not a protocol decision. But the CHW's device receives no notification that their scope has narrowed — only absence of new data for those subjects. If HS-3 is "retain and hide," the CHW may notice their subject list has shrunk. If it's "purge," the device must detect the scope change proactively.

**The gap**: The sync protocol currently delivers events but has no mechanism for the server to say "you no longer have access to subjects X, Y, Z — remove them." The server stops sending new events for those subjects, but doesn't issue a purge command. This means HS-3 option (a) (purge) cannot be implemented purely from the sync protocol as described. Purge requires either: (a) a server-initiated "remove these subjects from your device" message — a new protocol message type not currently in the model, or (b) the device to detect scope changes by comparing received assignments against prior assignments and inferring which subjects have left scope.

**Verdict**: **STRAINS** — mass reassignment is operationally feasible but reveals that scope contraction cannot be enforced by the current sync protocol. Scope expansion (delivery of new events) is handled naturally; scope contraction (removal of data) requires a new protocol mechanism.

---

### Finding B3: First-Sync Bandwidth for New Workers Is Prohibitive on 2G Without Mitigation

**Setup**: New CHW assigned to a village with 200 subjects and 3 years of history. Average 50 events per subject per year = 150 events per subject total = 30,000 events.

**Transfer cost**: 30,000 events × 1KB = 30 MB. On 2G (20–40 kbps effective): 100–200 minutes (1.5–3.5 hours). On 3G (200–400 kbps): 10–20 minutes. In practice, 2G connectivity windows may be 5–15 minutes before dropping.

**What happens on an interrupted first sync?**
- The worker has a partial event set
- Their projections are incomplete — some subjects appear with gaps
- Their sync_watermark is set to the last successfully received watermark
- On next sync, they resume from that watermark
- But the subjects they received no events for (due to interruption) have a watermark of 0 — the server will try to resend all their events again from the beginning

This means interrupted first-syncs may never complete on 2G — each sync attempt retransfers the same events because the per-subject watermark is not tracked, only the global watermark.

**Verdict**: **BREAKS** for first-sync on 2G without mitigation. The model needs explicit support for: (a) priority sync (deliver assignment events + recent events first; backfill historical events), (b) per-subject watermarks or a subject-specific "delivered through" marker, or (c) device-side acknowledgment of partial sync that allows resumption. Without one of these, new CHWs in 2G areas may never achieve a complete initial sync.

---

### Finding B4: Data Removal on Scope Contraction — None of the Three Options Is Safe

**Setup**: Worker W is reassigned 3 times over 2 years. Walk through each option for what remains on device.

**Option (a) — Purge on scope contraction**:
- Year 1: W in Zone 3. Device holds 200 subjects × 50 events = 10,000 events.
- End of Year 1: Reassigned to Zone 5. Purge Zone 3 data from device.
- Purge risks: (i) crash mid-purge → partial data, some Zone 3 subjects purged, others not; (ii) events W created about Zone 3 subjects are purged — W loses access to their own work history; (iii) a stolen device after purge has no sensitive data for Zone 3 patients. **Risk** if stolen before purge: all Zone 3 patient data exposed.
- **Critical**: Purge of W's own events (which exist on the server) does not harm data integrity. But W's event history is gone locally. If W needs to reference prior work (e.g., "I treated this patient last year"), they cannot without connectivity. This breaks the offline continuity of care.

**Option (b) — Retain but hide**:
- The data is present on the device. On Android, the app's SQLite database is protected by app sandboxing but accessible on rooted devices.
- A stolen, rooted device exposes 3 years of patient data across every zone W has ever worked in.
- The "hide" enforcement is UI-only — there is no platform-level enforcement that the app will actually hide the data consistently across all code paths. A debugging interface or a modified APK could bypass this.
- **Verdict**: Unacceptable for sensitive health data. This option is only safe for non-sensitive operational data.

**Option (c) — Retain indefinitely**:
- After 3 reassignments, the device has data for Zones 3, 5, and 7. As a field worker, W now has broader data access than their current scope grants.
- A stolen device exposes data for all zones. Over time, the device becomes a comprehensive data store — the worst outcome for data minimization.
- **Verdict**: Unacceptable.

**The uncomfortable truth**: For sensitive personal health information, the only safe option is (a) purge — but purge requires solving the crash-safety and work-history-continuity problems. The other two options leave sensitive data on physically vulnerable devices in field environments where theft and loss are common.

**A fourth option not in Phase 1**: **Selective retain** — W's own events (which reference W as author) are retained. Events authored by others about now-out-of-scope subjects are purged. This gives W continuity of their own work history while minimizing exposure. It requires the purge logic to distinguish events by author, which is computable from the event envelope.

**Verdict**: **BREAKS** as a binary choice among the three options. A fourth option (selective retain: keep own events, purge others') is needed for sensitive health data. The mechanism is structurally sound but the policy space is underspecified and the "retain but hide" option must be explicitly ruled out for sensitive data.

---

### Finding B5: Reassignment History Gap Is Operationally Harmful

**Setup**: CHW-A assigned to Village X. A sync transfer moves responsibility to CHW-B. CHW-A has 20 unsynced events.

**Walk-through**:
1. Coordinator creates `AssignmentEnded{actor: CHW-A, scope: Village_X}` and `AssignmentCreated{actor: CHW-B, scope: Village_X}`.
2. CHW-B syncs. Server delivers all events for Village X subjects that are on the server. CHW-A's 20 offline events are NOT yet on the server.
3. CHW-B's projection is missing CHW-A's 20 events. Example: CHW-A administered a treatment to Patient P but hasn't synced. CHW-B's projection shows Patient P has no recent treatment.
4. CHW-B visits Patient P, sees no record of treatment, and administers a duplicate treatment. This is a **clinical safety failure** — duplicate medication administration.
5. CHW-A eventually syncs. Their 20 events arrive, including the treatment record for Patient P. The server now has two treatment records for Patient P for the same day.

**Detection**: ADR-002's Conflict Detector would flag this as a concurrent state change — two treatments for the same patient in the same window. But the conflict is detected AFTER the second treatment has already been physically administered. The flag cannot undo the clinical action.

**Is this a mechanism failure?** The mechanism (accept-and-flag) is working correctly. But the operational consequence of the gap — a clinical action performed on incomplete data — is not a flag-and-resolve situation. It is a **patient safety event**. The platform cannot guarantee that CHW-B sees complete data at the time of handoff, only that discrepancies are eventually surfaced.

**This is an inherent consequence of offline-first**: The design accepts that supervisors see eventually consistent data (C.6). The same applies to peer-to-peer handoffs. The mechanism is correct but the operational risk must be documented explicitly, and the UI must make data staleness highly visible at handoff time.

**Verdict**: **HOLDS** mechanically — the model correctly handles the gap through detect-and-flag. **STRAINS** operationally — for clinical data, the gap between CHW-A's last sync and the handoff is a patient safety risk that the model must surface to deployers as a limitation, not just a flag in a queue.

---

## 4. Mechanism C: Stale Rule Reconciliation

### Finding C1: Flag Volume Under Mass Reassignment Is Unresolvable Without Automated Batch Resolution

**Setup**: District coordinator restructures 200 workers. 150 are offline. Over 3 days, they create 10 events each = 1,500 ScopeStaleFlag events on sync.

**Walk-through of resolution burden**:
- 1,500 flags, each requiring a resolver decision
- Each flag requires: reading the event, understanding the context, determining whether the data is valid, and marking resolved
- At 2 minutes per flag (optimistic for a coordinator): 3,000 minutes = 50 hours of review time
- The restructuring coordinator is now 50 hours behind on their primary responsibilities

**Is batch resolution viable?** Phase 1 proposed it. The question is what rule allows batch resolution without individual review. For `ScopeStaleFlag` from a mass reassignment:

The meta-condition that enables safe batch resolution: "All ScopeStaleFlags where the triggering_event (the AssignmentEnded event) has the same event_id and the worker's events were created before the worker could reasonably have known of the change (i.e., before they synced the assignment change event)."

This is computationally determinable: the `sync_watermark` on the worker's stale-scope events is older than the sync_watermark of the AssignmentEnded event. This establishes that the worker demonstrably could not have known their scope had changed. All such flags can be auto-resolved as "valid under previous scope."

**Verdict**: **HOLDS** — but only if the batch auto-resolution rule is implemented. Without it, the model is operationally dead on mass reassignment. The rule is: "ScopeStaleFlags where the event's sync_watermark is less than the watermark of the triggering AssignmentEnded event are auto-resolvable as 'valid under prior scope.'" This must be a platform-level auto-resolution policy, not a deployment configuration — the logic depends on watermark comparison which is always available.

---

### Finding C2: RoleStaleFlag on Clinical Actions Requires Pre-Policy Interception, Not Post-Flag Review

**Setup**: Worker's role changed from `field_worker` (can administer treatments) to `trainee` (observe only). Worker (offline) administers 3 treatments and creates treatment events. Sync delivers RoleStaleFlag on all 3. Downstream supply-deduction policy has already fired.

**Walk-through**:
1. Worker creates `TreatmentAdministered{patient: P1, medication: M1, dose: 5mg}` at 10:00. Event carries `device_seq: 47, sync_watermark: V150`.
2. Worker creates two more treatment events.
3. Worker syncs. Server processes events.
4. **ADR-002 S12**: "Conflict detection before policy execution." The Conflict Detector evaluates incoming events before policies fire. BUT — does the `RoleStaleFlag` qualify as a "conflict" that triggers S12's detect-before-act guarantee?

The current ADR-002 S12 specification says "Events flagged by the Conflict Detector do not trigger policy execution until the flag is resolved." But S12 was designed for **identity** conflicts (ConflictDetected type). The `RoleStaleFlag` is a new flag type proposed by Phase 1. Does the detect-before-act guarantee extend to authorization flags?

**If it does**: The supply-deduction policy does not fire on the 3 treatment events until a coordinator reviews the RoleStaleFalg and approves. This is the correct outcome — no supply is deducted for treatments that may be invalid.

**If it does not**: The policy fires immediately. Supply is deducted. The treatment is recorded. A `RoleStaleFlag` exists in the queue. A coordinator eventually reviews it and may decide the treatments were invalid — but the supply has already been deducted and the patient records already reflect the treatments. Correction requires additional events (SupplyDeductionCorrected, TreatmentVoided) that create downstream complexity.

**The fix**: The detect-before-act guarantee (ADR-002 S12) must be **explicitly extended to all flag types, including authorization flags**. This is a constraint-level decision that must be captured in ADR-003. It is not optional — without it, RoleStaleFlag events create a window where invalid clinical actions propagate through the system before review.

**Verdict**: **BREAKS** if the detect-before-act guarantee is not extended to authorization flags. The current ADR-002 S12 text is scoped to identity conflicts only. ADR-003 must extend it.

---

### Finding C3: Triple Staleness on One Event Creates a Multi-Resolver Deadlock

**Setup**: S19 Act 3 — role change (3a) + scope narrowing (3b) + subject merge (3c) happen simultaneously. Worker creates one event that triggers three different flags.

**Walk-through**:
- Event E1: `ObservationRecorded{subject: S1_retired, actor: W, assignment: old_Zone3_assignment, role_at_time: field_worker}`
- On sync, three flags generated:
  - Flag F1 (RoleStaleFlag): Worker's role changed to trainee. Resolver: the coordinator who changed the role.
  - Flag F2 (ScopeStaleFlag): Worker's scope no longer includes S1's original location. Resolver: the supervisor of the new scope holder.
  - Flag F3 (StaleReferenceFlag): S1 is retired, merged into S2. Resolver: derived from S2's scope hierarchy.

**The deadlock scenario**:
- F1's resolver (Coordinator C1) resolves: "Treatment is invalid — worker was a trainee." Resolution: reject E1's clinical implications.
- F2's resolver (Supervisor S2) resolves independently: "Data from Zone 3 is valid — worker was in Zone 3 when they captured it." Resolution: accept E1's scope attribution.
- F3's resolver (Coordinator C3 who oversees S2) resolves: "Alias resolves correctly — E1 is about S2." Resolution: accept E1 under S2's stream.

Now F1 has rejected E1 (clinical implications invalid) but F2 and F3 have accepted E1 (data attribution valid). The event is simultaneously rejected and accepted. The projection for S2 includes E1's data (via alias, per F2/F3 resolution) but downstream clinical policies should not fire on it (per F1 resolution).

**Is this resolvable?** Only if the three flag types are handled by a single coordinating resolver or if the resolution hierarchy is clear: clinical validity (F1) takes precedence over data attribution (F2, F3). But Phase 1 proposed that resolver designation is derived from scope hierarchy — and F1, F2, F3 have different scope scopes. There is no defined cross-flag coordination.

**Verdict**: **BREAKS** — triple staleness on a single event requires a multi-resolver coordination mechanism that Phase 1 does not address. At minimum: (a) all flags on a single event should be bundled and assigned to a single resolver who has authority across all three concern types (the coordinator who has the broadest scope covering all three issues), or (b) the flag resolution order must be defined (clinical validity first; if rejected, other flags are auto-resolved as moot; if accepted, other flags are resolved independently).

---

### Finding C4: Meta-Flag Chain Is Bounded But Operationally Damaging

**Setup**: Designated resolver R's scope changes while conflict F1 is pending. R resolves F1 under stale authority. The resolution event is itself flagged (UnauthorizedResolution).

**Walk-through**:
1. F1 is created. Resolver designated: R (Supervisor of Zone 3).
2. R is reassigned to Zone 7. A `ConflictResolverReassigned` event is needed — but Phase 1 only proposes this as a response; it's not part of the model.
3. R (unaware of their reassignment, offline) resolves F1: `ConflictResolved{conflict_id: F1, resolved_by: R, resolution: accept}`.
4. R syncs. The server detects that R no longer has authority over Zone 3 (their scope changed). The ConflictResolved event is flagged: `UnauthorizedResolutionFlag{original_conflict: F1, unauthorized_resolution: by R}`.
5. A meta-flag F1' is generated. Who resolves F1'? The current holder of Zone 3's supervisor role (R's replacement, R2). R2 must review both F1 (the original data conflict) and R's resolution decision.

**Does this recurse?** If R2 is also offline and reassigned while F1' is pending: F1'' could be generated. But ADR-002 S10 (merge/split online-only) establishes a precedent: some operations must be online-only precisely to prevent cascading offline conflicts. A similar constraint could apply here: **conflict resolution is online-only**. This would terminate the chain at depth 1 — R could not resolve F1 while offline, so the meta-flag situation never arises.

**Verdict**: **STRAINS** — the chain is bounded if conflict resolution is restricted to online-only operation. This is not stated in Phase 1 but is a necessary constraint. Making conflict resolution online-only is consistent with the coordinator tier's reliable connectivity (C.3) and with the operational reality that conflict resolution is a deliberate administrative act, not a field activity.

---

### Finding C5: Accept-and-Flag Is Insufficient for Sensitive-Subject Data Exposure

**Setup**: Worker W's scope is narrowed to remove HIV-positive patients (subject type: highly sensitive). W is offline. W continues recording observations about HIV patients. Events are accepted on sync.

**The core tension**: ADR-002 S14 says "events are NEVER rejected for state staleness." This principle was designed to prevent data loss from field workers' valid-but-stale work. But it creates an irreconcilable conflict with data protection regulations in many jurisdictions:

- GDPR Article 5: personal data must be processed with appropriate security
- Many national health data laws: unauthorized access to sensitive health categories (HIV status, mental health) requires specific breach reporting

When W's events about HIV patients are accepted and stored, the platform has received data that W was not authorized to collect. The flag exists, but:
1. The data is already in the system — immutable, permanent
2. The data was transmitted from W's device to the server (the transmission itself may constitute a breach under some regulations)
3. Even if the flag is resolved as "reject," the events remain in the store (P3 — append-only)

**Accept-and-flag means the sensitive data is permanently stored, regardless of resolution outcome.**

**This is not a mechanism failure — it is a design constraint collision**: P3 (append-only) and ADR-002 S14 (never reject) were designed to prevent operational data loss. But for data that should never have been collected (unauthorized sensitive-subject observations), permanent storage of the unauthorized record may itself be a compliance violation.

**Resolution options**:
1. **Accept as a known limitation**: The platform documents that it cannot prevent the physical recording of sensitive data on-device or its transmission on sync. Deployers must implement compensating controls (device encryption, scope management to prevent offline exposure in the first place).
2. **Sensitive-subject classification**: Subjects can be flagged as "high sensitivity." For high-sensitivity subjects, the accept-and-flag window is shortened by requiring more frequent syncs (or by implementing a technical control: the sync protocol refuses high-sensitivity events from actors without active authorization — an exception to S14).
3. **Physical data minimization**: High-sensitivity subjects are never delivered to devices where the authorized scope might expire — i.e., they are only synced when the assignment is verified active.

**Verdict**: **BREAKS** for deployments with sensitive data under regulatory requirements. Accept-and-flag is an operationally pragmatic model but is incompatible with "the data should never have been recorded in the first place" situations. A sensitive-subject classification with modified sync rules for high-sensitivity data is required as a deployment option.

---

## 5. Mechanism D: Projection Location Strategy

### Finding D1: Supervisor Projection Rebuild Is Prohibitive Without Incremental Updates

**Setup**: Supervisor has 15 CHWs. Each CHW covers 50 subjects. Each subject has 80 events. Supervisor syncs daily, receiving ~150 new events from 15 CHWs × 10 events/day.

**Projection rebuild on a tablet**:
- 15 CHWs × 50 subjects = 750 subjects in supervisor's scope
- Total event count: 750 subjects × 80 events = 60,000 events
- ADR-001 S00 spike: "projection rebuild was instant for 5 events." Estimated ~1ms per event for simple projections.
- 60,000 events × 1ms = 60 seconds for a full rebuild from scratch

**With incremental updates (only new events since last sync)**:
- 150 new events per sync × 1ms = 0.15 seconds
- This is the correct operational model — incremental projection updates, not full rebuild

**The gap**: ADR-001 S2 says "projections are always rebuildable from the event stream." The rebuild guarantee implies full rebuild capability. But the performance requirement (screen loads must feel instant) requires incremental updates as the operational path, with full rebuild only as a recovery mechanism.

If the supervisor's tablet crashes and loses projection state, a full rebuild from 60,000 events takes 60 seconds. This is borderline acceptable (one-time cost on first use after failure). The model must specify: **full rebuild is a recovery operation, not a normal operation**. Incremental update is the normal path.

**Additional concern — raw events vs. server summaries**: If the supervisor receives server-computed summaries (Phase 1 Q3), they cannot do offline drill-down into individual CHW events without raw events on the device. The hybrid model requires both. On a tablet with 16GB storage: 60,000 events × 1KB = 60 MB — trivially small. But this grows: after 3 years, 750 subjects × 300 events = 225,000 events = 225 MB. Still manageable on a tablet.

**Verdict**: **HOLDS** with incremental updates. Full rebuild is an acceptable recovery cost (60 seconds). The model must explicitly state that incremental update is the normal operational path.

---

### Finding D2: Server-Computed Summary Freshness Creates Double-Lag Visibility

**Setup**: CHW-A creates events. CHW-A syncs at 6 PM. Supervisor S syncs at 7 AM next day.

**Staleness chain**:
1. CHW-A's events from 6 PM are on the server.
2. Supervisor's 7 AM sync: server summary includes CHW-A's 6 PM events. Summary is current as of the sync moment.
3. Between 6 PM and 7 AM, CHW-A continued working offline and created 10 more events. These are NOT in the summary.
4. Supervisor sees: "CHW-A last activity: 6 PM yesterday. 10 observations recorded." But CHW-A actually recorded 20 observations (10 more since 6 PM).

**The supervisor cannot distinguish**:
- "No activity recorded" = CHW-A did not work
- "No activity visible" = CHW-A worked but hasn't synced

**Without distinguishing these**, the supervisor may incorrectly escalate or take corrective action against a CHW who is actively working but simply offline.

**Required mitigation**: The summary must include a **last-known-sync timestamp** per CHW, allowing the supervisor to see: "CHW-A last synced at 6 PM yesterday. Activity since then is unknown." This is visible staleness, which constraints.md (C.6) requires: "oversight views reflect the most recently synced state... The platform must make the age of information visible."

**Verdict**: **HOLDS** — the model is consistent with C.6's eventual consistency requirement. But the UI must surface per-CHW last-sync time explicitly, and the server summary must include this metadata. Without it, the model holds but the deployment is operationally misleading.

---

### Finding D3: Coordinator Projection Under Batch Merge Has Consistency Window

**Setup**: A batch merge of 200 subjects creates ~200 SubjectsMerged events. The coordinator's server-side projection must be updated.

**Walk-through**:
- Server processes 200 SubjectsMerged events sequentially. Each triggers an alias table update and projection rebuild for the surviving subject.
- If the coordinator queries the server mid-rebuild (at event 100 of 200): 100 subjects have been merged; 100 have not. The coordinator sees a partially-merged world.
- Example: Subject S1 (merged at event 50) shows unified history from both S1 and S2. Subject S3 (not yet merged at event 100) still shows separate histories.

**Is this a consistency violation?** Under eventual consistency (C.6), yes — this is accepted. But the coordinator cannot know which subjects are in the "merged" state and which are in the "pre-merge" state during the rebuild window. The projection has no "rebuild in progress" marker.

**Required mitigation**: The server projection must expose a "last_fully_consistent_watermark" — the highest watermark at which all projections are complete. Queries against a watermark higher than this indicate partial consistency. The coordinator UI should show: "Projection is being updated; some subjects may show stale state."

**Verdict**: **STRAINS** — eventual consistency during batch merge is by design, but the model needs a consistency marker in projections so that coordinators can interpret partial states correctly.

---

### Finding D4: Offline Supervisor Assessment on Stale Data Is Operationally Dangerous but Mechanically Sound

**Setup**: Supervisor S visits CHW-A in the field. Both offline. Supervisor's last sync: 2 days ago. CHW-A has 30 unsynced events.

**Walk-through**:
1. Supervisor reviews CHW-A's work from the 2-day-old projection. 30 events are invisible.
2. Supervisor creates `AssessmentRecorded{subject: CHW-A, gaps_identified: ["3 patients with no follow-up"], score: 2/5}`.
3. CHW-A's 30 offline events include follow-ups for exactly the 3 patients the supervisor flagged.
4. Supervisor syncs. Their assessment event arrives at the server. CHW-A's 30 events also arrive.
5. The server processes CHW-A's events first (they have an earlier sync_watermark). The projection for CHW-A now shows the follow-ups.
6. The supervisor's assessment (with lower score for missing follow-ups) is now visibly based on incorrect data.

**Is this a flag situation?** The assessment event is not flagged — it was created under valid authority with current (as of sync) data. The staleness is not a rule violation; it's an operational reality. The assessment is accurately recording what the supervisor saw at the time of the visit.

**Should the assessment be retroactively corrected?** No — the supervisor saw what they saw. The assessment is a point-in-time observation (correct per ADR-002's design). But the system should surface: "This assessment was created based on projection state from [2 days ago]; [30] events have since arrived that were not visible at assessment time."

**Verdict**: **HOLDS** — this is operating as designed. The model correctly accepts the assessment and does not retroactively flag it. The operational risk (assessments based on stale data) is inherent to offline-first and must be surfaced via projection staleness metadata in the UI, which is already required by C.6.

---

## 6. Mechanism E: Authority Context in Event Envelope

### Finding E1: Envelope Size Is Manageable But Storage Projection Requires Monitoring

**Setup**: Compute envelope size for minimum and maximum cases.

**Minimum case (S20 Act 1 — CHW records observation under standing assignment)**:
```
ADR-001 fields:    ~200 bytes (id, type, payload, timestamp)
ADR-002 fields:    ~80 bytes (device_id: 16B UUID, device_sequence: 8B int, 
                               sync_watermark: 8B, subject_ref: 20B, actor_ref: 20B)
ADR-003 fields:    ~24 bytes (1× assignment_ref: 20B typed UUID + 4B overhead)
Total envelope:    ~304 bytes + payload (~200–500 bytes)
Total event size:  ~500–800 bytes
```

**Maximum case (campaign worker with standing + campaign assignments + process reference)**:
```
ADR-001 fields:    ~200 bytes
ADR-002 fields:    ~80 bytes
ADR-003 fields:    ~60 bytes (2× assignment_refs: 40B + process_ref: 20B)
Total envelope:    ~340 bytes + payload
Total event size:  ~540–840 bytes
```

**Storage projection**:
- 10,000 workers × 10 events/day × 365 days = 36,500,000 events/year
- Average event size: 700 bytes
- Annual storage: ~25.5 GB per year
- After 5 years: ~128 GB — manageable on modern server storage

**Per-device storage**: 50 subjects × 100 events × 700 bytes = 3.5 MB. Even after 3 years of accumulation at 50 events/subject/year: 50 × 300 × 700 bytes = 10.5 MB. Well within low-end Android storage budgets.

**Verdict**: **HOLDS** — the authority_context adds modest overhead (24–60 bytes per event) with no storage or bandwidth crisis at projected scale.

---

### Finding E2: Variable-Length Assignment Reference List Requires Schema Commitment

**The problem**: HS-7 — when a worker operates under both standing and campaign assignments, how many assignment_refs are in the event?

**Option 1 — Single ref (nearest/narrower assignment)**: The event carries one assignment_ref. When both assignments cover the subject, the campaign assignment wins (it's more specific). Simple, fixed-size envelope field.
- **Loss**: The event does not record that the worker was also operating under their standing assignment at the time. If the campaign is later invalidated, the event has no standing assignment reference to fall back on.
- **Recovery**: The server can derive the standing assignment from the actor's assignment history at event creation time. This is computable from the actor's timeline.

**Option 2 — Multiple refs (variable-length list)**: The event carries all active assignment_refs that cover the subject.
- **Benefit**: Complete authority provenance in the event itself — no server-side derivation needed.
- **Risk**: The event envelope has a variable-length field. ADR-001 S5 commits to an extensible envelope, but variable-length lists in an immutable, versioned schema create forward-compatibility complexity. Adding a 3rd assignment type later means events with 3 refs that older projection code doesn't handle.
- **ADR-001 S5 states**: "The envelope is designed to be extended by future ADRs." But extension of a fixed-schema field is different from adding a variable-length list. This is a breaking envelope change.

**Verdict**: **STRAINS** — neither option is clean. The single-ref option loses provenance; the multi-ref option complicates the immutable envelope schema. A practical resolution: the event carries a single `primary_assignment_ref` (the most-specific active assignment) plus an optional `secondary_assignment_ref` (the standing assignment if a campaign is primary). This limits the maximum number of refs to 2 — bounded, not variable-length — covering all known use cases without an unbounded list.

This is a constraint-level decision (it affects the immutable envelope) and must be captured in ADR-003.

---

### Finding E3: Authority Context Is an Assertion, Not a Verified Fact — and That's Correct but Must Be Explicit

**Walk-through**:
1. Device creates event with `authority_context: {assignment_ref: UUID-of-standing-assignment}`.
2. The assignment referenced by UUID-of-standing-assignment was ended 2 days ago (server-side).
3. The device doesn't know this — it has the stale assignment in local state.
4. The event's authority_context asserts: "I was operating under assignment UUID-XYZ."
5. On sync, the server looks up UUID-XYZ. The assignment exists (it's an immutable event) but is no longer active. The device's assertion is evaluable and falsifiable.

**What the authority_context must NOT be**: A claim that the assignment was active at the time of the event. The server cannot verify this without knowing the device's knowledge state at the time of creation — which is approximated by the sync_watermark. The correct interpretation: "the device believed this assignment was active when creating this event, based on its state as of its last sync (sync_watermark V150)."

**Explicit statement required in ADR-003**: "The `authority_context` is a device assertion — an honest record of what authorization the device believed was in effect at capture time. It is not a server-verified fact. The server validates authority_context on sync by checking whether the referenced assignments were active at a time consistent with the event's sync_watermark."

This distinction matters for auditors: the authority_context establishes intent and belief, not verified authorization. Combined with the flag (ScopeStaleFlag, RoleStaleFlag), the audit trail records both "what the device claimed" and "what the server determined."

**Verdict**: **HOLDS** — the mechanism is correct if explicitly framed as an assertion with server-side validation on sync. The ambiguity in Phase 1 (is it an assertion or a fact?) must be resolved in ADR-003.

---

### Finding E4: Retroactive Authority Context Correction Must Use Resolution Events, Not New Assignment Events

**Setup**: Event E1 has `authority_context: {assignment_ref: campaign_assignment_UUID}`. A coordinator resolves the resulting flag with: "this event should be attributed to standing assignment, not campaign." How is this expressed?

**Walk-through**:
- E1 is immutable. Its authority_context cannot be changed.
- A resolution event `FlagResolved{flag_id: F1, resolution: "attribute_to_standing_assignment", standing_assignment_ref: UUID}` captures the attribution decision.
- The projection for E1's subject must incorporate this resolution: when building the subject's history for campaign reporting, E1 is excluded. For standing reporting, E1 is included.

**The gap**: The projection engine must know to consult resolution events when building projections. Currently, projections are built from domain events (ObservationRecorded, etc.) and identity events (SubjectsMerged). Adding "consult resolution events for attribution overrides" adds a new class of projection input.

**Is this expressible within the current projection model?** Yes — resolution events are regular events in the store. The projection function adds a rule: "if a FlagResolved event with `attribution_override` type exists for this event_id, use the overridden attribution." This is a projection logic extension, not a structural change.

**Verdict**: **HOLDS** — retroactive attribution is mechanically correct using resolution events. The projection must be extended to handle attribution overrides, but this is a code-level concern, not a structural constraint.

---

### Finding E5: System-Generated Events Need a "System Actor" Identity

**Setup**: ConflictDetected, SubjectsMerged, SubjectSplit events are generated by the server. What is their authority_context?

**Walk-through**:
- These events do not have a human actor performing an action. The authority is the platform itself, acting in response to sync processing.
- If `authority_context` is mandatory (required field), system events must populate it with something.
- Option 1: `authority_context: {type: "system", process: "sync_conflict_detection"}` — a special non-actor authority
- Option 2: The coordinator who triggered the operation (for SubjectsMerged/Split) or null for ConflictDetected (no human initiator)
- Option 3: `authority_context` is optional — present for human-initiated events, absent for system events

**The envelope design implication**: If the field is optional, every system that reads events must handle the absent case. Optional fields in an immutable event schema are acceptable (they have a default interpretation: "no human authority context") but create code paths that must be consistently handled.

**Recommended resolution**: `authority_context` has three valid states: (a) human-initiated with assignment ref(s), (b) human-initiated by coordinator with no active assignment for the system operation (rare, but possible), (c) system-generated — represented by a reserved `{type: "platform", id: "platform-system"}` actor identity that exists in every deployment. This makes the field always present and always parseable, without requiring a special null-handling path.

**Verdict**: **STRAINS** — the authority_context mechanism needs a defined "platform actor" identity for system-generated events. This is a minor structural addition that must be defined in ADR-003.

---

## 7. Combination Scenarios

### Combo α: Assignment Transfer + Identity Merge + Offline Worker

**Setup recap**: CHW-A assigned to Village X. Subject S1 in Village X. While CHW-A offline: (1) CHW-A reassigned to Village Z; (2) S1 merged into S2 (Village Y — outside both X and Z). CHW-A creates 5 observations against S1.

**Sync walk-through**:

**Step 1 — Server receives 5 events from CHW-A**. Each event: `{subject_ref: {type:subject, id:S1}, authority_context: {assignment_ref: Village_X_assignment_UUID}, sync_watermark: V150}`.

**Step 2 — Conflict Detector runs first (ADR-002 S12)**. For each event:
- S1 is retired (merged into S2). The event references a retired subject → `StaleReferenceFlag` (ADR-002 type, already defined). **Flag 1 generated** (one per event = 5 flags, but per ADR-002 A3 strategy, these should batch under a single root cause: same subject merge event).
- The Village_X_assignment_UUID has ended. The actor's current scope is Village_Z. → `ScopeStaleFlag`. **Flag 2 generated** (batchable across 5 events).

**Step 3 — Alias resolution (ADR-002 S6)**. S1 aliases to S2. S2 is in Village Y. CHW-A's current scope (Village Z) does not include Village Y. This is a **scope-crossing alias** (Finding A1, HS-12).

- Under the "alias-respects-original-scope" rule (required by Finding A1): authorization is evaluated against S1's original scope (Village X). CHW-A was authorized for Village X at event creation time (V150 < V_merge). The events are valid under their original scope.
- Without this rule: the alias maps events into S2's stream. S2 is in Village Y. CHW-A has never been authorized for Village Y. The server would need to flag these as additionally scope-violated in Village Y's context.

**Step 4 — How many flags total, and who resolves each?**

Without the alias-respects-original-scope rule: potentially 3 flag types per event × 5 events = 15 flags, with resolvers spanning Village X supervisor, Village Y coordinator, and the role hierarchy. This is unresolvable in practice.

With the alias-respects-original-scope rule: 2 flag types (StaleReference + ScopeStale), both batchable to 1 resolution decision each. The resolver for both is the coordinator who has scope over both Village X (old scope) and S2's final resting location (Village Y) — i.e., the district coordinator. 2 flags, 1 resolver, clear resolution path.

**Step 5 — What does the projection show?**

S2's projection includes CHW-A's 5 events (via alias). The projection for S2 shows these events with a staleness annotation. The Village Y supervisor (S2's current scope) sees events from a CHW they don't supervise, flagged for review.

**Interaction not visible from individual mechanism analysis**: The alias resolution triggers a visibility transfer — CHW-A's events appear in Village Y's supervisor's view without Village Y supervisor having any assignment relationship with CHW-A. This is operationally unexpected and potentially confusing. The Village Y supervisor sees flagged events from an unknown CHW and must review them without context about why those events are in their queue.

**Verdict**: The alias-respects-original-scope rule is essential to prevent this combo from creating a multi-resolver deadlock and cross-scope projection pollution. Without it, this combo **BREAKS** the resolution model. With it, it **HOLDS** with 2 batchable flags.

---

### Combo β: Campaign Expiry + Device Sharing + Offline Observations

**Setup recap**: CHW-A and CHW-B share one Android phone. Campaign C expired. CHW-A creates 3 events, logs out. CHW-B logs in, creates 5 events.

**Walk-through**:

**Step 1 — Device state after CHW-A's session**: The device holds Campaign C data (subjects, forms, assignment events for CHW-A). CHW-A's 3 events are in local storage with `actor_ref: {type:actor, id:CHW-A-UUID}`.

**Step 2 — CHW-B logs in**: What data is accessible to CHW-B?

Under "sync scope = access scope": CHW-B's sync scope is CHW-B's assignments. CHW-A's data is technically CHW-A's sync scope data. But both are on the same device. The device-local access control must isolate per-actor data.

**Does the current model enforce per-actor isolation on a shared device?** Phase 1 EQ-5 surfaced this as an open question. The answer reveals a gap: the sync model delivers data per actor (sync is per-device, but the server determines scope based on who is authenticated). If CHW-B authenticates and syncs, the server delivers CHW-B's scope. But CHW-A's previously-delivered data is still in local storage.

**Can CHW-B see CHW-A's observations?** If the device's local database is actor-partitioned (CHW-A's events in partition A, CHW-B's events in partition B), then CHW-B's app session reads only from partition B. CHW-A's data is invisible to CHW-B in the app. But on a rooted device, both partitions are readable.

**If the device is not actor-partitioned** (all events in one table, differentiated by actor_ref): CHW-B's session queries show events where `subject_ref` matches subjects in CHW-B's scope. If subject S1 is in CHW-A's scope (but not CHW-B's), CHW-A's events about S1 are in the database but would be filtered out by the scope query. However, an insufficiently restrictive query could return CHW-A's events to CHW-B's session.

**Finding**: The model must specify actor-partitioned local storage for shared-device scenarios. This is a device-side architectural requirement, not expressed in the current mechanism.

**Step 3 — CHW-B creates 5 campaign events after campaign expiry**: The campaign's `valid_until` has passed. The device has the campaign assignment event for CHW-B (delivered when CHW-B last synced). The assignment's temporal bound is visible on-device. The device should warn CHW-B: "Campaign C ended [date]. These observations will be flagged for review." But the warning cannot block capture (I4 — offline operation preserved).

**Step 4 — Sync**: Who syncs? If the shared phone is used by both CHW-A and CHW-B sequentially, sync happens under each actor's credentials separately. The server receives: CHW-A's 3 events (actor_ref: CHW-A-UUID, device_id: shared_phone_UUID) and CHW-B's 5 events (actor_ref: CHW-B-UUID, device_id: shared_phone_UUID).

**The device_id is the same for both actors**. ADR-002 S5 says `device_id` identifies a physical device, not a user account. The device sequence counter is per-device. Both CHW-A's and CHW-B's events increment the same device sequence counter.

**A sequence ordering collision**: CHW-A creates events with device_seq 1, 2, 3. CHW-B (on the same device) creates events with device_seq 4, 5, 6, 7, 8. The combined sequence for the shared device is monotonically increasing — no collision. The server can distinguish CHW-A's events from CHW-B's via actor_ref. **Causal ordering is preserved.**

**Step 5 — Campaign expiry flags**: CHW-B's 5 events carry `authority_context: {assignment_ref: Campaign_C_assignment_UUID, process_ref: Campaign_C_UUID}`. The campaign's `valid_until` was before these events' device_time (advisory). On sync, the server checks: Campaign C ended at watermark V_end. CHW-B's sync_watermark on these events is V_before_end. CHW-B's events were created against a watermark that predates the campaign end — but only if CHW-B had synced after Campaign C started and before it ended. If CHW-B never synced during the campaign, their watermark may be older than the campaign start.

**Interaction not visible from isolated analysis**: Device sharing means the device's sync_watermark is shared between actors. If CHW-A synced and advanced the watermark, CHW-B's events on the same device carry CHW-A's sync_watermark. CHW-B's events appear to have been created with more recent server knowledge than CHW-B actually had. The server might determine CHW-B's events "should have known" about the campaign expiry (their watermark is post-expiry) when in fact CHW-B logged in after CHW-A synced and inherited the newer watermark.

**This is a causal ordering corruption specific to shared devices.** The sync_watermark on an event is supposed to represent "what server state did this device know about when this event was created?" On a shared device, the watermark represents "what state did this device know about at its last sync, regardless of which actor performed that sync." The two actors on the same device share a knowledge-state namespace, which is incorrect for causal analysis.

**Verdict**: **BREAKS** on two dimensions: (a) per-actor data isolation on shared devices is unspecified and requires actor-partitioned local storage; (b) shared device_id creates causal ordering corruption when actors share a sync_watermark. For (b), the fix requires either: per-actor sync sessions (each actor maintains their own watermark) or explicit documentation that shared devices produce unreliable causal ordering for multi-actor scenarios.

---

### Combo γ: Stale Supervisor + Reassigned CHW

**Setup recap**: Supervisor S oversees 10 CHWs including CHW-A. Yesterday, CHW-A reassigned to a different district. Supervisor S's device is 1 day stale. Supervisor S visits CHW-A in the field (both offline) and creates an assessment.

**Walk-through**:

**Step 1 — Supervisor S's local state**: The device has the old assignment event for CHW-A (within S's scope). The new assignment (to different district) has not been delivered. From S's device perspective, CHW-A is still in S's scope.

**Step 2 — Assessment creation**: Supervisor S creates `AssessmentRecorded{subject_ref: {type:actor, id:CHW-A-UUID}, assessment_data: {...}, score: 4/5, authority_context: {assignment_ref: S_District_A_assignment_UUID}}`. This references CHW-A (actor as subject — HS-2 dimension) and S's district assignment.

**Step 3 — CHW-A's new district supervisor (S2) creates a concurrent assessment**: S2 is online and has the current assignment data. S2 creates a competing assessment of CHW-A on the same day.

**Step 4 — Supervisor S syncs**: The server receives S's assessment. The server checks: does S have authority over CHW-A? CHW-A's assignment now points to District B. S is authorized for District A. S has no authority over CHW-A.

**Flag generated**: `ScopeStaleFlag{event: assessment_UUID, actor: S, intended_subject: CHW-A-UUID, S_scope: District_A, CHW-A_current_scope: District_B}`.

**Who resolves this flag?** The coordinator who oversees both District A and District B (the regional coordinator). They must decide: is S's assessment valid as a contemporaneous field observation? Or should it be discarded because S had no authority?

**The operational reality**: S's assessment was made in good faith, in person, based on direct observation. It may contain valuable information (S saw CHW-A's work in the field). Discarding it wastes the information. But accepting it without flagging creates an authorization record where District A supervisor has authority over District B CHWs.

**Preferred resolution**: S's assessment is accepted and attributed as "informal observation under prior scope" — valid as data, not valid as official supervisor assessment. The projection for CHW-A includes S's assessment with a "scope-stale" annotation. S2's assessment (from the correct supervisor) takes precedence for official supervision metrics.

**New interaction from this combo**: Two assessments for CHW-A on the same day, from different supervisors. The ADR-002 Conflict Detector would flag concurrent state changes on CHW-A as a subject. But supervisor assessments are not state changes — they're additive observations. The conflict model needs to distinguish between "concurrent conflicting state changes" (traditional conflict) and "multiple assessments of the same subject in the same period" (valid concurrent observations, no conflict). This distinction was not surfaced in Phase 1 or ADR-002.

**Verdict**: **STRAINS** — the mechanism handles the stale-scope flag correctly, but the concurrent-assessments scenario reveals a gap in the conflict taxonomy. Multiple assessments of the same subject (especially supervisor assessments of CHW performance) are not conflicts and should not be flagged as such. A "concurrent additive observations" exemption (from ADR-002's conflict taxonomy) must be explicitly extended to actor-as-subject assessments.

---

### Combo δ: Auditor Access + Cross-Hierarchy Scope + Data Sensitivity

**Setup**: External auditor needs temporary access to review all operations in District D — 3 zones, 50 CHWs, 2,500 subjects.

**Step 1 — Expressing auditor's assignment**:

Can Phase 1's assignment model express "access to all subjects in District D, read-only, for 30 days"?

- Scope: geographic (District D node in the hierarchy) — **YES**, expressible
- Role: `auditor` — **YES**, a new role type, not yet defined in the model
- Duration: 30 days (`valid_from`, `valid_until`) — **YES**, temporal bounds exist
- Permissions: read-only — **STRAINS**: the current assignment model grants access to "perform actions based on role." But "read-only" is not a geographic scope — it's a capability restriction within a scope. The role must encode "can observe but cannot create subject events." Whether this is a role attribute or a separate capability field is not specified.

**For query-based scope** (auditor needs "all subjects with flag type X across any geography"): The assignment model **BREAKS** here. Geographic containment cannot express "any subject whose projection matches condition C." This requires a query-scope type, which is not part of Phase 1's geographic-primary model.

**Step 2 — Sync vs. server-only access**:

Does the auditor sync data to a device? 2,500 subjects × 80 events = 200,000 events × 1KB = 200 MB. On a laptop with broadband (auditors operate with reliable connectivity — C.3 extension): feasible. On a tablet on 3G: 30–60 minutes for initial sync.

For an auditor with a 30-day window, syncing 200 MB on first use is acceptable. But the auditor retaining that data after the 30-day period is a data protection concern.

**Step 3 — Data removal after access period**:

Temporal enforcement is server-side only (HS-8). After 30 days, the auditor's assignment expires. On the auditor's next sync, the server stops delivering new events. But the 200 MB already on the auditor's device is not purged — this is HS-3 applied to an auditor context. For an auditor with a laptop (better security controls than a field phone), option (c) retain indefinitely is less risky, but still creates a GDPR compliance issue if the data residency period has passed.

**Verdict for temporal data removal**: A **device-side data expiry** mechanism is required for time-limited access grants. When the assignment's `valid_until` is reached (detectable on-device from assignment event metadata + device_time advisory check), the device should trigger a purge of the auditor's data. This is the one case where device-side temporal enforcement is necessary despite device_time being advisory — because the alternative (data retained indefinitely) is worse than a potentially slightly early/late purge due to clock drift.

**Step 4 — Can the auditor create events?**:

An "audit finding" event: `AuditFindingRecorded{subject: Zone_3_UUID, finding: "incomplete records", authority_context: {assignment_ref: auditor_assignment_UUID}}`. The assignment grants `role: auditor`. If the platform supports `AuditFindingRecorded` as an event type, the auditor can create it under their assignment.

**Step 5 — Does the assignment model handle this without a special case?**

No. The auditor requires: (a) a new role type (`auditor`) not in the current model; (b) a read-only capability within geographic scope; (c) potential query-based scope for cross-geography access; (d) device-side temporal data expiry. These are 4 structural additions. The auditor is not a degenerate case of the existing model — it is a distinct access pattern requiring explicit modeling.

**Verdict**: **BREAKS** as a pure extension of the existing model. The assignment model handles auditors' geographic scope and temporal bounds, but cannot express read-only capability, query-based scope, or device-side data expiry. Auditor access requires: (a) a `capability` field on assignments (`read_only`, `read_write`, etc.); (b) query-scope as a first-class scope type alongside geographic scope; (c) device-side data expiry for time-limited assignments. These are constraint-level additions to the model.

---

## 8. Invariant Survival Report

| Invariant | Mechanism A | Mechanism B | Mechanism C | Mechanism D | Mechanism E |
|-----------|-------------|-------------|-------------|-------------|-------------|
| **I1: No Data Loss** | **HOLDS** — events accepted under stale assignments are never dropped; ScopeStaleFlag preserves them | **HOLDS** — sync scope determines what is delivered, not what is rejected; stale-scope events still sync to server | **HOLDS** — accept-and-flag is the mechanism; no flag type discards events | **HOLDS** — projection failures do not affect event store | **HOLDS** — authority_context is a field on immutable events; its presence/absence does not affect event acceptance |
| **I2: No Silent Permission Elevation** | **BREAKS** — A3 shows coordinator can create out-of-scope assignments without server-side scope-containment invariant; A4 shows concurrent conflicting role grants can accumulate unintended privileges | **HOLDS** — sync scope is computed server-side; the server cannot be tricked into delivering out-of-scope data IF the scope computation is correct and server-side assignment validation (A3 fix) is implemented | **HOLDS** — flags expose rather than hide elevated access | **HOLDS** — projection location does not affect authorization | **STRAINS** — E5 shows system-generated events with no authority_context could be exploited as events that bypass authority validation if parsers treat absent authority_context as "any authority" |
| **I3: No Silent Permission Persistence** | **STRAINS** — B4 shows that data on devices is not automatically removed when scope narrows; the device "remembers" data from prior scopes indefinitely under options (b) and (c) | **BREAKS** — the sync protocol has no mechanism to actively remove data from devices on scope contraction; silence (no new events delivered) is not purge | **HOLDS** — flags surface staleness, enabling active remediation | **STRAINS** — server-computed summaries stop including out-of-scope data, but cached device summaries may persist until next sync | **HOLDS** — authority_context doesn't affect data persistence |
| **I4: Offline Operation Preserved** | **HOLDS** — device-local checks are soft (warning) not hard (block); all captures proceed | **HOLDS** — sync scope doesn't block capture | **HOLDS** — accept-and-flag explicitly accepts all events | **HOLDS** — projection failures don't block capture | **HOLDS** — authority_context is populated at capture time without network |
| **I5: Audit Trail Complete** | **STRAINS** — A1 shows HS-12 (scope-crossing alias) doesn't propagate scope context to flag metadata without the alias-respects-original-scope rule; without it, the audit trail loses the context that the event was authorized under S1's original scope | **HOLDS** — sync event delivery creates server-side log | **HOLDS** — flag events are themselves immutable records; the combination of the event, the flag, and the resolution creates a complete trace | **STRAINS** — D2 shows supervisor sees double-lag staleness but the "based on data as of" timestamp may not be explicit in the assessment event | **HOLDS** — authority_context + flags create full audit trace per E3 |
| **I6: Simplicity Preserved** | **HOLDS for S20+S21 happy path** — CHW operates under one assignment; scope check is invisible in the UI; zero authorization interactions in a normal day | **HOLDS** — sync is background; CHW doesn't interact with scope determination | **HOLDS** — no flags in the happy path (no assignment changes while CHW is actively working) | **HOLDS** — device-local projection is seamless | **HOLDS** — authority_context is populated automatically; CHW never sees it |

### Critical invariant failures requiring ADR-003 constraints:

**I2 BREAKS on Mechanism A (A3)**: Server-side scope-containment invariant on AssignmentCreated is mandatory.

**I3 BREAKS on Mechanism B (B4)**: A data lifecycle policy (purge mechanism with scope-contraction awareness) must be defined and enforced at sync time.

---

## 9. Assumptions Surfaced

The following Phase 1 assumptions were found to be incorrect or underspecified under stress:

| # | Phase 1 Assumption | Stress Test Finding | Impact |
|---|-------------------|---------------------|--------|
| **P1** | "Assignment-based access control is a single check" | A1 shows 5 structural extensions needed (actor-as-subject scope, lifecycle policy, sync atomicity guarantee, HS-7 tagging rule, alias-respects-original-scope) | HIGH — the model is correct as a foundation but incomplete as a complete authorization system |
| **P2** | "Sync scope = access scope is sufficient for field workers" | B3 shows first-sync on 2G is prohibitive (1.5–3.5 hours) without priority sync; B4 shows scope contraction requires an active purge mechanism not present in the model | HIGH — bandwidth and purge are structural requirements |
| **P3** | "Authorization staleness is handled uniformly by accept-and-flag" | C2 shows RoleStaleFlag on clinical actions requires pre-policy interception (extension of ADR-002 S12); C5 shows accept-and-flag is incompatible with regulatory requirements for sensitive subjects | HIGH — the uniform model breaks for two important categories |
| **P4** | "Coordinator scope validation is a consequence of the model" | A3 shows the model has no self-enforcing scope-containment on AssignmentCreated; this is a privilege escalation vector | CRITICAL — this is a security vulnerability, not just an operational risk |
| **P5** | "Shared devices are a minor edge case (EQ-5)" | Combo β shows shared devices cause causal ordering corruption (shared sync_watermark) and require actor-partitioned local storage | HIGH — device sharing is common in target deployments; this is not a minor edge case |
| **P6** | "Auditor access is a special case that can wait" | Combo δ shows auditor access requires 4 structural additions (capability field, query-based scope, device-side expiry, auditor role type) that cannot be bolted on later without envelope changes | MEDIUM — deferring auditor access may create irrecoverable lock-in if the envelope is finalized without capability fields |
| **P7** | "RoleStaleFlag and ScopeStaleFlag are symmetric to StaleReferenceFlag" | C3 shows triple staleness creates a multi-resolver deadlock; the three flag types need a coordination mechanism that StaleReferenceFlag (identity only) does not require | MEDIUM — the flag model is more complex than Phase 1 represented |
| **P8** | "Temporal enforcement via server-only is sufficient" | Combo δ shows time-limited access grants (auditor case) require device-side data expiry, not just server-side stoppage of new events | MEDIUM — for time-limited access, server-side enforcement prevents new data delivery but cannot remove data already on the device |

---

## 10. Missing Operational Paths

The following paths were discovered during stress testing that Phase 1 did not address:

### M1: Reassignment During an Active Case (S08 Context)
A CHW is the responsible actor for an ongoing case (S08 — following something over time until resolved). The CHW is reassigned to a different geographic area. The case subject is in the old area. Who continues the case? The assignment model's scope-based authorization would remove the CHW's access to the case subject. But case responsibility (from S08) is actor-to-subject, not geographic. These two assignment types conflict — geographic scope assignment vs. case responsibility assignment.

### M2: Cross-CHW Subject Referral
CHW-A creates an event referencing CHW-B as the intended next actor for a subject ("refer to CHW-B for follow-up"). CHW-B's device must receive this referral — but the referral is an event in CHW-A's scope, not CHW-B's scope. CHW-B has no assignment relationship with the subject at the time of referral. The referral must create a temporary scope expansion or a "pending assignment" state that the sync protocol must handle.

### M3: Coordinator Offline During Critical Assignment Creation
All coordinator-level operations are described as online-only (C.3 — reliable broadband). But C.3 says "Generally reliable broadband" — not guaranteed. A coordinator who is creating an emergency campaign assignment while traveling (3G connectivity) may lose connectivity mid-operation. An incomplete assignment creation could leave workers in an ambiguous scope state. The model needs a transactional guarantee for assignment creation: either the AssignmentCreated event is committed (and syncs to workers) or it does not exist.

### M4: Worker Accumulates Multiple Expired Campaign Assignments
Over 2 years, a worker participates in 10 campaigns. Each adds an assignment event. On sync, the server delivers all 10 campaign assignments (most with `valid_until` in the past). The device accumulates expired assignment events. On scope evaluation ("what is this actor's current scope?"), the device must filter for active assignments only. An implementation bug that fails to filter expired assignments could grant access to subjects from past campaigns. The model must explicitly specify that scope evaluation always checks `valid_from ≤ current_time ≤ valid_until` and that `device_time` being advisory means this check is approximate. The authoritative check is server-side.

### M5: Supervisor Assessment of a CHW Who Has Been Deactivated
A CHW leaves the organization. Their actor record is deactivated (analogous to subject deactivation in ADR-002). A supervisor, still offline with stale state, creates an assessment of the deactivated CHW. The assessment subject_ref points to a deactivated actor identity. Is this an identity-stale event (handled by ADR-002 StaleReferenceFlag)? Or is it an authorization-stale event (handled by RoleStaleFlag)? The flag taxonomy does not cover "event targeting a deactivated actor-as-subject."

---

## 11. Hot Spot Resolutions

| HS | Phase 1 Description | Stress Test Result | Resolution Status |
|----|--------------------|--------------------|-------------------|
| **HS-1** | Subject history completeness on reassignment | B5: Gap is real and operationally harmful for clinical handoffs. The gap is bounded by the predecessor CHW's sync frequency. | **NARROWED** — accept as an inherent offline-first limitation; require UI to show "data as of last sync" prominently at handoff time; recommend deployers establish sync-before-handoff workflows. |
| **HS-2** | Assessment visibility to assessed CHW | A1: Confirmed that actor-as-subject scope is a second dimension not expressible in geographic assignment model. | **RESOLVED as requires extension** — a second assignment type (actor-as-subject visibility grant) must be added; alternatively, assessments whose subject_ref is an actor_id are always synced to that actor's device (a platform-level rule for actor-as-subject events). |
| **HS-3** | Data removal on scope narrowing | B4: None of the three options is safe. Option (b) retain-but-hide is unacceptable for sensitive health data. Option (c) retain indefinitely violates data minimization. Option (a) purge is required but needs crash-safety and selective-retain (own events) refinement. | **RESOLVED with conditions** — purge is the required behavior; selective retain (keep actor's own events, purge others') is the recommended implementation; purge must be crash-safe (journaled, atomic). |
| **HS-4** | Projection computation location for supervisors | D1: Device-local projection is feasible for supervisors (60 seconds full rebuild on a tablet, 0.15 seconds incremental). Server-computed summaries are useful for aggregate dashboards. | **RESOLVED** — hybrid: raw events sync to supervisor's device for drill-down and offline review visits; server-computed summaries sync for aggregate dashboard. The tablet's storage and compute are sufficient. |
| **HS-5** | Stale-scope flag severity | C1, C2: Informational for geographic scope staleness (C.7 — work in progress continues under old rules). Not informational for role staleness on clinical actions (requires pre-policy interception). | **NARROWED to two categories** — ScopeStaleFlag is informational (auto-resolvable per C1 rule); RoleStaleFlag on capability-restricted action types blocks policy execution (extension of ADR-002 S12). |
| **HS-6** | Scope transition atomicity | B2: Confirmed the gap. Assignment change events and initial scope data are not guaranteed to arrive atomically. | **RESOLVED as requires protocol extension** — the sync protocol must bundle: (a) assignment transition events, (b) the first batch of new-scope subject data in a single sync payload unit; partial delivery must be resumable with the assignment events always delivered first. |
| **HS-7** | Scope composition (standing + campaign) | A1, E2: Union scope is correct; the event carries the narrower (more specific) assignment_ref as `primary_assignment_ref` + the standing assignment as `secondary_assignment_ref` — a bounded 2-ref structure, not a variable-length list. | **RESOLVED** — max 2 assignment refs in authority_context (primary = most specific active assignment, secondary = standing assignment if primary is a campaign assignment). |
| **HS-8** | Temporal access enforcement (device_time advisory) | C1, Combo δ: Server-side enforcement is correct for ongoing operations. Device-side data expiry (purge) is required for time-limited access grants (auditor case) despite device_time being advisory — because the alternative (data retained post-expiry) is worse than approximate enforcement. | **RESOLVED with nuance** — temporal enforcement: server-side for access control (correct); device-side for data removal on time-limited grants (required, advisory precision accepted). |
| **HS-9** | Grace period for expired authority | C1: Auto-resolution rule is sufficient (events with sync_watermark before the campaign-end watermark are auto-accepted as "validly created before the worker could know"). No explicit grace period time window needed — the watermark comparison is more precise. | **RESOLVED** — watermark-based auto-resolution replaces time-window grace period. |
| **HS-10** | Cross-level visibility in distribution | Combo δ (partially): Per-district events (one event per district allocation) prevent cross-district visibility leakage but create event multiplication (5 events for 5 districts vs. 1 event for all). | **NARROWED** — recommend per-district allocation events with a parent distribution process event for aggregate tracking. This is a configuration-layer decision (ADR-004), not a structural ADR-003 decision. |
| **HS-11** | Upward visibility aggregation | D1, D2: Coordinator tier uses server-computed projections (reliable connectivity). Supervisor tier uses hybrid. Field tier uses device-local. | **RESOLVED** — tiered model confirmed; see Q3 analysis. |
| **HS-12** | Scope-crossing merge | A1, Combo α: This is the most dangerous hot spot. The alias-respects-original-scope rule is required. Without it, scope-crossing merges create silent cross-scope data projection and multi-resolver deadlocks. | **RESOLVED as requires structural rule** — alias resolution must preserve the original scope context for authorization evaluation. The StaleReferenceFlag must include the original subject's scope at event creation time in its metadata. |
| **HS-13** | Orphaned conflicts (resolver reassigned) | C4: Bounded if conflict resolution is online-only. Meta-flag depth is max 1 if the resolution actor must be online and their authority is verified at resolution time. | **RESOLVED with constraint** — conflict resolution must be online-only (analogous to ADR-002 S10 for merge/split). This eliminates the meta-flag chain beyond depth 1. |

---

## Summary: What ADR-003 Must Decide as Constraints (Bucket 1 Candidates)

The following findings have irreversibility properties (affect the event envelope, the sync protocol structure, or server-side invariants that cannot be changed without data migration):

| Constraint Candidate | Source | Why Irreversible |
|---------------------|--------|-----------------|
| **Authority context max 2 refs**: events carry `primary_assignment_ref` + optional `secondary_assignment_ref`; no variable-length list | E2, HS-7 | Envelope schema; events are immutable |
| **Server-side scope-containment invariant on AssignmentCreated**: new assignment scope must be contained within the creating coordinator's scope | A3 | Security invariant; must be enforced from day one or privilege escalation is baked in |
| **Detect-before-act extended to authorization flags**: RoleStaleFlag and ScopeStaleFlag block policy execution (extending ADR-002 S12) | C2 | Changes sync processing order; all implementations must follow the same order |
| **Alias-respects-original-scope rule**: authorization evaluation for an event uses the original subject's scope at event creation time, not the post-merge surviving scope | A1, HS-12, Combo α | Affects how all future conflict detectors evaluate scope-crossing events |
| **Conflict resolution is online-only** | C4, HS-13 | Protocol constraint; must be enforced by the sync implementation |
| **System events use a reserved platform actor identity** | E5 | Envelope schema; every system-generated event must be parseable with the same authority_context structure |
| **Sensitive-subject classification as a first-class subject attribute** | C5 | Affects sync scope computation for high-sensitivity subjects; cannot be retrofitted without reprocessing all existing subject events |
| **Actor-partitioned local storage on shared devices** | Combo β | Device-side architectural requirement; determines SQLite schema design |

The following findings are strategies (Bucket 2 candidates) — correct directions that can be evolved in code without data migration:

- Selective retain purge strategy (own events kept, others purged on scope contraction)
- Priority sync for initial scope expansion (recent events first, historical backfill)
- Watermark-based auto-resolution for ScopeStaleFlag
- Per-actor sync sessions on shared devices
- Incremental projection updates as normal operational path (full rebuild as recovery)
- Batch flag resolution with root-cause grouping
