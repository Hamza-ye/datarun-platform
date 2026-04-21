> **⚠️ SUPERSEDED** — This is a raw exploration document archived for reference. Decisions were finalized in **ADR-003**. For a navigated reading guide, see [guide-adr-003.md](../guide-adr-003.md).
# ADR-003 Phase 1: Policy Enforcement Scenario Analysis

> Phase: 1 of 3 — Policy Enforcement Scenarios
> Method: Scenario-driven policy analysis (adapted from Brandolini event storming)
> Date: 2026-04-10
> Input scenarios: S20, S21, S03, S09, S14, S19

---

## 1. Upstream Assumptions

### From ADR-001 (committed)

| # | Assumption | Verified against |
|---|-----------|-----------------|
| A1.1 | All writes are append-only immutable events | ADR-001 S1 ✓ |
| A1.2 | Identifiers are client-generated UUIDs | ADR-001 S3 ✓ |
| A1.3 | The sync unit is the immutable event | ADR-001 S4 ✓ |
| A1.4 | Projections are derived from events and rebuildable | ADR-001 S2 ✓ |
| A1.5 | The event log is the single source of truth | ADR-001 S2 ✓ (strategy-protecting-constraint — write-path discipline) |
| A1.6 | Event envelope is extensible — ADR-3 can add fields (e.g., authority context) without invalidating existing events | ADR-001 S5 ✓ |

### From ADR-002 (committed)

| # | Assumption | Verified against |
|---|-----------|-----------------|
| A2.1 | Four typed identity categories: Subject, Actor, Process, Assignment | ADR-002 S2 ✓ |
| A2.2 | Causal ordering via `device_sequence` + `sync_watermark` | ADR-002 S1 ✓ |
| A2.3 | Events carry typed identity references: `{type, id}` | ADR-002 S2 ✓ |
| A2.4 | Merge = alias in projection, never re-reference | ADR-002 S6 ✓ |
| A2.5 | Split freezes history, source is archived | ADR-002 S8 ✓ |
| A2.6 | Accept-and-flag conflict model — events are NEVER rejected for state staleness | ADR-002 S14 ✓ |
| A2.7 | Single-writer conflict resolution: every conflict has exactly one designated resolver | ADR-002 S11 ✓ |
| A2.8 | Merge and split are online-only operations | ADR-002 S10 ✓ |
| A2.9 | `device_time` is advisory only | ADR-002 S3 ✓ |
| A2.10 | `device_id` is hardware-bound, not user-bound | ADR-002 S5 ✓ |
| A2.11 | Conflict detection happens before policy execution on sync (server-side) | ADR-002 S12 ✓ |
| A2.12 | Conflict detection uses raw references, not alias-resolved ones | ADR-002 S13 ✓ |

**Items explicitly deferred from ADR-002 to ADR-003:**

- Who can resolve which conflict types (authorization for conflict resolution) — ADR-002 S11
- What data syncs to which device (sync scope for merge/split events) — ADR-002 S6, S10
- Sync topology (one-tier, two-tier, summary sync)
- Where projections live (device, server, or both)

### From constraints.md

| # | Assumption | Verified |
|---|-----------|----------|
| C.1 | Field workers: frequently offline hours-to-days, low-end Android, 2G/3G | ✓ |
| C.2 | Supervisors: intermittent connectivity, phones/tablets | ✓ |
| C.3 | Coordinators: reliable broadband, laptops/desktops | ✓ |
| C.4 | Capture must be instant regardless of connectivity | ✓ |
| C.5 | Sync is opportunistic, minutes not hours | ✓ |
| C.6 | Oversight is eventually consistent | ✓ |
| C.7 | Configuration changes propagated on next sync — work in progress under old config completes under old rules | ✓ |
| C.8 | Tens of thousands of field workers, hundreds of supervisors, tens of coordinators | ✓ |
| C.9 | Millions of records, 3–6 levels of organizational hierarchy | ✓ |
| C.10 | Platform provides mechanisms (access control, audit trails, data partitioning) — deploying orgs apply specific policies | ✓ |

**Correction**: None. All upstream assumptions verified against the actual files.

### Critical constraint interaction for this analysis

C.7 is the constraint most directly relevant to ADR-003: *"Work in progress under the old configuration completes under the old rules."* This applies to authorization rules — if a scope changes centrally while a worker is offline, their in-progress work under the old scope is valid. Combined with A2.6 (events never rejected for state staleness), this creates the foundational tension: **offline work is always accepted, even under stale authorization; discrepancies are surfaced post-sync.**

---

## 2. Scenario Group 1: S20 + S21 — Simplicity Anchor

**Why this is the anchor**: S20 (CHV field work) and S21 (supervisor visit) represent the everyday operational cycle that tens of thousands of users perform daily. If the authorization model adds ceremony to this case, it will be rejected by users and deploying organizations alike. This is the S00 of authorization — the litmus test for P7 (simplest scenario stays simple).

### Act 1: CHV records observation during household visit (offline)

| Question | Answer |
|----------|--------|
| **Actor** | CHV (field worker), operating under a standing assignment binding them to a geographic area (e.g., Village X). Role: field worker. |
| **Data required on device** | (1) Subject registry for their assigned area — subjects they may visit. (2) Reference data — observation forms, treatment protocols, supply lists. (3) Their own prior events against these subjects — for continuity of care. (4) Their active assignment record — binds this actor to this scope. |
| **Access rule evaluated** | "Actor has an active Assignment of type `field_worker` whose scope includes the subject's geographic area." The rule is a check: does the actor's assignment scope contain the subject being acted upon? |
| **Rule enforcement point** | **Device-local only at capture time.** The device holds the assignment and the subject registry. The UI presents only in-scope subjects. At sync, the server validates the assignment was active at the event's `device_time` (advisory) or `device_sequence` position. Mismatch → accept and flag. |
| **Stale rule scenario** | CHV was reassigned to Village Y yesterday, but device still shows Village X assignment. CHV records an observation about a subject in Village X. On sync: event is accepted (A2.6), flagged as "recorded under stale assignment scope." The flag is informational, not blocking — the observation data is valuable regardless of assignment state. |
| **Sync scope implication** | The CHV's device must contain: all subjects in their assigned geographic scope, the CHV's own prior events for those subjects, the CHV's active assignment record, and reference data (forms, protocols). This is a **scope-filtered subset** of the total data. |

### Act 2: CHV looks up subject history to inform current visit (offline, reading projection)

| Question | Answer |
|----------|--------|
| **Actor** | Same CHV as Act 1. |
| **Data required on device** | Local projection of the subject's event stream — prior observations, treatments, status. Only events that have been synced to this device (authored by this CHV, or authored by others and delivered via sync because the subject is in scope). |
| **Access rule evaluated** | Same as Act 1 — the subject is in the CHV's assigned scope. No additional rule for reading vs. writing. At this tier, the actor who captures data also reads it. |
| **Rule enforcement point** | **Device-local only.** The projection is built from events already on the device. If the device has the events, the CHV can see them. No runtime authorization check beyond "this subject is in my local registry." |
| **Stale rule scenario** | If another CHV was previously assigned to this subject (before reassignment), their historical events may or may not be on this device. If they synced before the reassignment and the subject was transferred as part of scope handoff, the events should be present. If not — the CHV sees an incomplete history. This is a **sync scope completeness** question, not an authorization question. |
| **Sync scope implication** | When a subject is in a CHV's scope, the device needs **all events** for that subject — not just the CHV's own. This means subject-scoped sync, not actor-scoped sync. The CHV sees a subject's full history (within their scope), including events from prior assignees. |

🔴 **Hot Spot HS-1**: **Subject history completeness on reassignment.** When subject responsibility transfers from CHV-A to CHV-B, must CHV-B's device receive all of CHV-A's historical events for those subjects? If yes, the first sync after a scope expansion could be large. If no, the new CHV works with an incomplete picture. The answer depends on whether "subject history" is operationally necessary or just nice-to-have.

### Act 3: Supervisor visits CHV in the field, reviews recent work (may be offline)

| Question | Answer |
|----------|--------|
| **Actor** | Supervisor, operating under a standing assignment binding them to a set of CHVs (or equivalently, to the geographic areas those CHVs cover). Role: supervisor. |
| **Data required on device** | (1) The CHV's recent events — observations, treatments — for the supervisor to review. (2) The subjects those events reference — enough context to understand what the CHV did. (3) The supervisor's own assignment record. (4) Assessment forms. |
| **Access rule evaluated** | "Actor has an active Assignment of type `supervisor` whose scope includes the CHV being visited." Scope can be expressed as: supervisor is assigned to a set of CHVs (actor-based), or to a geographic area that contains the CHV's area (geography-based). |
| **Rule enforcement point** | **Device-local** for the review itself. The supervisor's device must already contain the CHV's recent events (received via sync). If the supervisor is offline during the visit, they review what they have. Server validates on sync that the supervisor had authority over this CHV at the time of the assessment. |
| **Stale rule scenario** | (a) Supervisor's assignment changed — a CHV was moved out of their oversight. If the supervisor assesses the CHV after the reassignment (but before syncing), the assessment is accepted and flagged. (b) The CHV's data on the supervisor's device is stale — the CHV created new events that haven't synced yet. The supervisor reviews what they have. This is not an authorization issue — it's an eventual consistency reality. |
| **Sync scope implication** | The supervisor's device needs: events from **all CHVs in their scope**, the subjects referenced by those events, and assessment/review forms. This is a **wider scope** than any single CHV's device. The supervisor's device is a superset of its assigned CHVs' data — at least the events, probably not the full reference data. |

### Act 4: Supervisor documents assessment of CHV's work (may be offline)

| Question | Answer |
|----------|--------|
| **Actor** | Same supervisor. Creating a new event: an assessment/review of the CHV's work. |
| **Data required on device** | Same as Act 3, plus: the assessment form/template. The event references the CHV (as a Subject of type Actor — the CHV is the thing being assessed) and the CHV's events being reviewed. |
| **Access rule evaluated** | Same scope rule as Act 3: supervisor has assignment covering this CHV. Additionally: the actor must have role `supervisor` — a CHV cannot create supervisor assessments. This is a **role + scope** check. |
| **Rule enforcement point** | **Device-local** at capture time (role and scope checked against local state). **Server** on sync (validates role and scope were active at event creation time). |
| **Stale rule scenario** | Supervisor was demoted to field worker role while offline but continues to create supervisor assessments. On sync: events are accepted (A2.6), flagged as "created under stale role." This is structurally identical to the stale-scope case. |
| **Sync scope implication** | The supervisor's assessment event must sync to: (a) the server, (b) potentially to the CHV's device (so the CHV can see their assessment), (c) to upstream coordinators. This is a **write that fans out** — the event originates on the supervisor's device but is relevant to multiple downstream consumers. |

🔴 **Hot Spot HS-2**: **Assessment visibility to the assessed CHV.** Should the CHV's device receive their supervisor's assessment events? If yes, the CHV's sync scope includes "events about me as a subject" — not just "events about subjects in my geographic scope." This introduces a second sync dimension: geographic scope (what I work on) + actor scope (what's about me).

### Act 5: CHV syncs at end of day (connectivity window)

| Question | Answer |
|----------|--------|
| **Actor** | CHV, reaching a connectivity point. |
| **Data required on device** | N/A — this is a sync action, not a capture action. |
| **Access rule evaluated** | Sync itself is not an authorization action. The sync protocol determines **what events flow** based on the CHV's current assignment scope. However: if the CHV's assignment changed since last sync, the sync response must reflect the new scope — potentially delivering new subjects and removing (or just stopping delivery of) old ones. |
| **Rule enforcement point** | **Server-side.** The server evaluates the CHV's current assignment scope, determines the delta (new events in scope since last sync_watermark), and sends them. The server also receives the CHV's outbound events, runs conflict detection (ADR-002 S12), and flags any that were created under stale scope. |
| **Stale rule scenario** | CHV's scope was narrowed while offline. On sync: (a) outbound events for out-of-scope subjects are accepted and flagged; (b) inbound sync no longer includes events for subjects outside the new scope; (c) question: does the device **keep** the subjects it already has from the old scope, or does it purge them? |
| **Sync scope implication** | Sync is the enforcement point where stale scope becomes consequential. The sync protocol must: (1) deliver scope changes (assignment events), (2) adjust the inbound event filter to the current scope, (3) accept outbound events regardless of scope validity (A2.6), (4) decide whether to actively remove data for subjects no longer in scope. |

🔴 **Hot Spot HS-3**: **Data removal on scope narrowing.** When a CHV's scope shrinks (reassigned to a smaller area), should the device purge subjects and events that are no longer in scope? Options: (a) purge immediately — reduces device storage, enforces data minimization, but the CHV loses access to their own historical work; (b) retain but hide — data stays on device but UI removes access, eventually purged; (c) retain indefinitely — simplest technically, worst for data sensitivity. Each has security, privacy, and usability trade-offs.

### Act 6: Supervisor syncs and sees aggregated view of multiple CHVs' work

| Question | Answer |
|----------|--------|
| **Actor** | Supervisor, syncing to get a cross-CHV view. |
| **Data required on device** | Aggregated projections across all supervised CHVs: how many visits each performed, completion rates, overdue follow-ups, supply levels. Individual events are needed for drill-down. |
| **Access rule evaluated** | Supervisor's scope must include all the CHVs whose work they're viewing. The aggregation is a projection — it is computed from events, and the scope filter determines which events feed it. |
| **Rule enforcement point** | **Split.** If projections are built locally on the supervisor's device: the scope is implicitly enforced by what events were synced to the device (sync scope = access scope for local projections). If projections are server-computed and delivered as summaries: the server enforces scope when computing the summary, and the supervisor's device displays a pre-authorized view. |
| **Stale rule scenario** | A CHV was removed from the supervisor's oversight. On next sync, the server stops including that CHV's events in the supervisor's sync payload. But the supervisor's local projection still includes the old CHV's data until recomputed. For server-computed summaries, the view updates immediately on sync. |
| **Sync scope implication** | **This is the critical act for projection location.** At the supervisor tier, local projection from raw events is feasible but computationally expensive (all events from all CHVs). Server-computed summaries are lighter to transfer but require the server to know the supervisor's exact scope and to pre-compute views. A hybrid is also possible: raw events sync for drill-down, server-computed summaries sync for dashboard views. |

🔴 **Hot Spot HS-4**: **Projection computation location for supervisors.** The supervisor needs both detail (individual CHV events for review visits) and aggregate (dashboard of all CHVs' progress). Raw events give detail but require device-side computation. Server summaries give aggregate but lose drill-down. This is a fundamental sync topology question that affects bandwidth, device capability, and data freshness.

### Simplicity Verdict

**The authorization model itself does NOT add unacceptable complexity to S20+S21.** The access rule is structurally simple: `actor.assignment.scope ⊇ subject.location`. This is one lookup. The complication is not in the rule evaluation — it is in three surrounding concerns:

1. **Sync scope determination** — what data reaches which device (HS-1, HS-3, HS-4)
2. **Stale state handling** — not different from ADR-002's accept-and-flag; authorization staleness is structurally identical to identity staleness (event accepted, anomaly flagged)
3. **Projection location** — where aggregated views are computed (HS-4)

The authorization check itself is lightweight and device-local. If ADR-003 keeps it that way — a scope containment check at capture time, server validation on sync — P7 is preserved. The complexity is in sync topology for supervisors, not in the auth model for CHVs.

---

## 3. Scenario Group 2: S03 — Designated Responsibility

### Act 1: Coordinator assigns a worker to a geographic area / set of subjects

| Question | Answer |
|----------|--------|
| **Actor** | Coordinator, operating from an office with reliable connectivity. |
| **Data required on device** | Coordinator's device/dashboard: the organizational hierarchy, available workers, geographic areas, current assignments. This is administrative data, not field data. |
| **Access rule evaluated** | "Actor has role `coordinator` with authority over the organizational unit that contains the target area." Coordinators can only assign workers within their own scope — a district coordinator cannot assign workers into a different district. |
| **Rule enforcement point** | **Server-side.** Coordinators have reliable connectivity (C.3). Assignment creation is a command that produces an `AssignmentCreated` event. The server validates the coordinator's authority to create this assignment. |
| **Stale rule scenario** | Minimal — coordinators operate online. The risk is two coordinators simultaneously creating conflicting assignments for the same area. This is a single-writer question: who owns assignment authority for a given scope? |
| **Sync scope implication** | Assignment events must flow **downstream** to the affected worker's device on next sync. The assignment event is what tells the worker's device "you are now responsible for area X." |

### Act 2: Worker operates within assigned scope (offline)

| Question | Answer |
|----------|--------|
| **Actor** | Field worker, operating under the assignment created in Act 1. |
| **Data required on device** | The assignment record (received on last sync), subjects within the assigned scope, reference data. Identical to S20 Act 1. |
| **Access rule evaluated** | Same as S20 Act 1: `actor.assignment.scope ⊇ subject.location`. |
| **Rule enforcement point** | **Device-local.** |
| **Stale rule scenario** | Not stale yet — the worker has their current assignment. |
| **Sync scope implication** | Standard field-worker sync scope: assignment + subjects in scope + own events + reference data. |

### Act 3: Coordinator reassigns worker to a different scope while worker is offline

| Question | Answer |
|----------|--------|
| **Actor** | Coordinator, creating a new assignment event: `AssignmentEnded` for old scope + `AssignmentCreated` for new scope. (Or a single `AssignmentChanged` event — the representation is TBD, but the semantic is: old scope deactivated, new scope activated.) |
| **Data required on device** | Coordinator's administrative view. |
| **Access rule evaluated** | Coordinator's authority over both the old and new scope areas. |
| **Rule enforcement point** | **Server-side.** |
| **Stale rule scenario** | N/A for the coordinator. The tension is entirely on the worker's side — they don't know about this yet. |
| **Sync scope implication** | The assignment change events are queued for delivery to the worker's device on next sync. Until then, the worker operates under the old assignment. This is the designed behavior per C.7: "Work in progress under the old configuration completes under the old rules." |

### Act 4: Worker (offline, unaware of reassignment) records observations in old scope

| Question | Answer |
|----------|--------|
| **Actor** | Field worker, still operating under the old assignment. |
| **Data required on device** | Same as Act 2 — device state is unchanged. |
| **Access rule evaluated** | Device evaluates against the old (now stale) assignment. The check passes — the device doesn't know the assignment has changed. |
| **Rule enforcement point** | **Device-local passes; server will flag on sync.** The device-local check is correct against its local state. The server-side check on sync will detect that the event was created against a scope the worker was no longer assigned to at server-time. |
| **Stale rule scenario** | **This IS the stale rule scenario.** The event is valid work — the worker spent effort collecting this data. The data is about real subjects in a real area. The only anomaly is that the worker was no longer the designated responsible person. |
| **Sync scope implication** | The event must be accepted (A2.6). The question is what flag type to generate. Options: (a) `ScopeStaleFlag` — informational, meaning "this event was recorded under a stale assignment, review for attribution"; (b) `UnauthorizedEventFlag` — stronger, implying a policy violation. The correct framing matters: this is NOT unauthorized access. This is authorized work under stale state, exactly analogous to ADR-002's identity staleness. |

🔴 **Hot Spot HS-5**: **Stale-scope flag severity.** Is an event recorded under a stale assignment (a) an informational note (the data is fine, just attribute it to the right person), (b) a soft conflict (someone should review whether this data is valid), or (c) a hard conflict (the event cannot be processed until resolved)? The answer determines workflow: informational notes don't block downstream policies; hard conflicts do. C.7 ("work in progress under old rules completes under old rules") argues strongly for (a).

### Act 5: Worker syncs — events in a scope they're no longer assigned to

| Question | Answer |
|----------|--------|
| **Actor** | Field worker, syncing. |
| **Data required on device** | N/A — sync action. |
| **Access rule evaluated** | Server evaluates all incoming events against the worker's **current** assignment scope. Events outside the current scope are accepted and flagged per HS-5. |
| **Rule enforcement point** | **Server-side.** |
| **Stale rule scenario** | The sync response delivers: (a) the new assignment events (ending old scope, starting new scope), (b) subjects and events for the new scope, (c) per HS-3, potentially phases out data for the old scope. The worker's device transitions to the new scope state. |
| **Sync scope implication** | **Sync must deliver scope transition atomically.** The worker needs to go from "old scope state" to "new scope state" in one sync. If the assignment change arrives without the new scope's subjects, the worker has an assignment to an empty scope. If the new subjects arrive without the assignment change, the worker has subjects they can't explain. The assignment event and the initial data for the new scope should be delivered together — or the device must handle partial transition gracefully. |

🔴 **Hot Spot HS-6**: **Scope transition atomicity on sync.** When a worker's scope changes, the sync must deliver: (1) assignment change event, (2) subject data for new scope, (3) potentially withdraw data for old scope. If sync is interrupted mid-transition (connectivity drops), the device could be in an inconsistent state. The sync protocol must handle partial delivery: either the assignment change is idempotent and the device re-requests missing data on next sync, or the transition is an explicit multi-step handshake.

### Act 6: New worker assigned to old scope syncs — do they get the previous worker's events?

| Question | Answer |
|----------|--------|
| **Actor** | New field worker, assigned to the area the first worker left. |
| **Data required on device** | Subjects in the assigned area + **all events for those subjects**, regardless of who authored them. |
| **Access rule evaluated** | New worker's assignment scope includes these subjects. Authorization is scope-based, not authorship-based — the new worker can see all data about subjects in their scope, even if created by a different worker. |
| **Rule enforcement point** | **Server-side** (sync scope determination). The server includes all events for in-scope subjects in the sync payload, filtering by scope, not by author. |
| **Stale rule scenario** | The previous worker's events from Act 4 (recorded under stale scope) may or may not be flagged. If flagged, the new worker sees flagged events in the subject's history. If the flag is resolved (attributed to this scope), they see clean data. |
| **Sync scope implication** | **Confirms subject-scoped sync, not actor-scoped sync.** The sync payload for a worker contains all events for subjects in their scope, regardless of authorship. This is essential for continuity — the new worker needs the full subject history to continue the work. |

### Designated Responsibility Summary

The core pattern is clear: **assignment is scope-granting.** An `Assignment` event (ADR-002 typed identity) binds an actor to a scope. The scope determines two things simultaneously:

1. **Authorization scope** — what the actor can do (capture events about subjects within the scope)
2. **Sync scope** — what data the device receives (events for subjects within the scope)

Authorization scope ≤ sync scope is possible (device has data the user can only read, not write). Sync scope ≤ authorization scope is problematic (user is authorized for data the device doesn't have — they'd need to be online to act).

**Key finding**: Stale assignment scope is structurally identical to the stale identity state handled by ADR-002 S14. The same accept-and-flag mechanism applies. C.7 explicitly blesses this: in-progress work under old rules completes under old rules.

---

## 4. Scenario Group 3: S09 — Coordinated Campaign

### Act 1: Coordinator creates a campaign with a defined time window and scope

| Question | Answer |
|----------|--------|
| **Actor** | Coordinator, online. |
| **Data required on device** | Administrative view: available workers, geographic areas, campaign templates. |
| **Access rule evaluated** | Coordinator has authority to create campaigns within their organizational scope. |
| **Rule enforcement point** | **Server-side.** Campaign creation is an administrative action that produces a Process (ADR-002 typed identity — transient, scoped to a workflow instance). |
| **Stale rule scenario** | Minimal — coordinators operate online. |
| **Sync scope implication** | The campaign definition (a Process event with temporal bounds and geographic scope) must be distributed to all workers who will participate. This is a **broadcast within scope** — potentially hundreds of workers. |

### Act 2: Workers receive campaign-scoped grants (additional authority beyond standing assignment)

| Question | Answer |
|----------|--------|
| **Actor** | Workers, receiving campaign participation assignments on sync. |
| **Data required on device** | Campaign definition (Process event), campaign-specific assignment (an Assignment event binding this actor to the campaign's scope for the campaign's duration), campaign-specific subjects and reference data (e.g., additional households to visit beyond standing assignment). |
| **Access rule evaluated** | The campaign assignment is an **additive scope grant** — the worker's effective scope becomes `standing_assignment.scope ∪ campaign_assignment.scope` for the campaign's duration. Two assignments, composable. |
| **Rule enforcement point** | **Device-local** — the device evaluates both assignments. A subject is in scope if it's in either the standing scope or the campaign scope. |
| **Stale rule scenario** | The campaign assignment hasn't synced yet — the worker doesn't know about the campaign. They continue with standing work only. This is a **delivery latency** issue, not an authorization issue. |
| **Sync scope implication** | Campaign participation triggers a **scope expansion on sync**: the worker's device receives subjects that are in the campaign scope but not in their standing scope. This expansion may be temporary — subjects received for the campaign may need to be removed after the campaign ends. This links back to HS-3 (data removal on scope change). |

🔴 **Hot Spot HS-7**: **Scope composition model.** When a worker has multiple active assignments (standing + campaign), is the effective scope (a) a union (any assignment grants access), (b) a tagged union (access is tagged with which assignment granted it), or (c) independent (each assignment has its own scope check)? Option (b) preserves provenance — "this event was created under campaign authority" vs. "this event was created under standing authority" — which matters for campaign reporting and post-campaign cleanup.

### Act 3: Workers perform campaign work (offline, under campaign authority)

| Question | Answer |
|----------|--------|
| **Actor** | Field worker, offline, performing campaign-specific tasks (e.g., distributing nets, administering vaccines) for subjects in the campaign scope. |
| **Data required on device** | Campaign subjects, campaign forms, campaign assignment. The work is structurally identical to standing work (Act 1 of S20) — same event structure, same capture flow. |
| **Access rule evaluated** | `actor.effective_scope ⊇ subject.location`, where effective scope includes the campaign grant. If the subject is in the campaign scope, the check passes. |
| **Rule enforcement point** | **Device-local.** |
| **Stale rule scenario** | Not stale yet — campaign is active, assignment is current. |
| **Sync scope implication** | Events carry the campaign Process reference (ADR-002 typed identity: `{type: process, id: campaign_uuid}`). This tag is what allows the server to attribute the event to the campaign for progress tracking and reporting. |

**Key finding**: The event envelope must carry **which authority the event was created under** — not just who and where, but under which assignment. ADR-001 S5 anticipated this: "every event records the authority under which it was performed (role, scope, or equivalent), but the authorization model is ADR-3." The authority context in the envelope is the union of: actor reference, assignment reference(s), and optionally process reference.

### Act 4: Campaign time window expires while workers are offline

| Question | Answer |
|----------|--------|
| **Actor** | No direct actor — the campaign's temporal bound has passed. The coordinator may issue a `CampaignEnded` event, or the expiration may be implicit (the campaign definition carried a `valid_until` timestamp). |
| **Data required on device** | The device has the campaign definition with its temporal bounds. |
| **Access rule evaluated** | The device **could** check `device_time > campaign.valid_until` — but ADR-002 S3 says `device_time` is advisory only. If the device clock is wrong, the check is meaningless. The device **cannot reliably know** whether the campaign has expired without syncing. |
| **Rule enforcement point** | 🔴 **This is the core tension of temporal access.** The device cannot enforce expiration reliably because: (a) `device_time` is advisory (ADR-002 S3), (b) the coordinator may extend the campaign while the worker is offline, (c) the worker has no way to distinguish "campaign expired" from "I haven't synced the extension yet." |
| **Stale rule scenario** | This IS the stale rule scenario. The campaign ended, but the worker doesn't know. |
| **Sync scope implication** | The device must have the campaign's temporal bounds for display purposes ("campaign runs until March 15"), but enforcement of expiration must happen **server-side on sync**, not device-side. |

🔴 **Hot Spot HS-8**: **Temporal access enforcement.** Can the device enforce time-bound access? If `device_time` is advisory, the answer is: **no, not reliably.** The device can provide a best-effort check (warn the user "this campaign may have expired"), but the authoritative check must be server-side. This means: events created after a campaign expires but before the worker syncs are accepted (A2.6) and flagged. The flag is "created under expired campaign authority." This is structurally identical to Act 4 of S03 (stale scope) — the same accept-and-flag mechanism applies.

### Act 5: Workers (offline) continue recording under expired campaign authority

| Question | Answer |
|----------|--------|
| **Actor** | Field worker, offline, still performing campaign work. |
| **Data required on device** | Same as Act 3. The device state is unchanged — the worker doesn't know the campaign expired. |
| **Access rule evaluated** | Device-local check passes — the campaign assignment is still active in local state. |
| **Rule enforcement point** | **Device-local passes (stale); server will flag on sync.** |
| **Stale rule scenario** | Worker records 15 observations under expired campaign authority. All 15 are accepted on sync, all 15 are flagged. A coordinator (or auto-resolution policy) decides: were these observations valuable? Should they be attributed to the campaign or to standing work? |
| **Sync scope implication** | The events carry the campaign process reference. The server can distinguish "campaign work recorded after expiration" from "standing work" by checking the process reference against the campaign's temporal bounds. |

### Act 6: Workers sync — events created under expired campaign authority arrive at server

| Question | Answer |
|----------|--------|
| **Actor** | Field worker, syncing. Server processing incoming events. |
| **Data required on device** | N/A — sync action. |
| **Access rule evaluated** | Server checks each event's authority context: the campaign reference, the worker's campaign assignment, and the campaign's temporal bounds. Events where `event.sync_watermark < campaign_end_watermark` are within the campaign window (the worker hadn't seen the end event). Events where the worker demonstrably could not have known about the expiration are flagged with lower severity. |
| **Rule enforcement point** | **Server-side.** |
| **Stale rule scenario** | The sync response delivers: (a) the `CampaignEnded` event (or equivalent), (b) the end of the campaign assignment for this worker. On the next capture, the device-local check will no longer pass for campaign-scope subjects. |
| **Sync scope implication** | Post-campaign, the worker's sync scope contracts back to their standing assignment only. Campaign-specific subjects that are outside the standing scope should eventually be removed from the device (per HS-3). Campaign events the worker authored remain in the server's record — they are immutable. |

🔴 **Hot Spot HS-9**: **Grace period for expired authority.** Should there be an explicit grace period for expired campaign authority? Example: "Events recorded within 48 hours of campaign expiration are automatically attributed to the campaign without flagging." This reduces flag noise but introduces a configurable parameter. Alternative: always flag, let auto-resolution policies handle the grace period. The second option is more consistent with P5 (conflict is surfaced) and avoids encoding temporal policy into the platform.

### Coordinated Campaign Summary

Campaign authorization is **additive temporal scope** — a time-bound extension of a worker's standing scope. Three findings:

1. **Authority context in event envelope**: Every event must record which assignment(s) authorized it. For standing work, this is the standing assignment. For campaign work, this is the campaign assignment. The event carries `{type: assignment, id: campaign_assignment_uuid}` in its authority context.

2. **Temporal enforcement is server-side only**: The device cannot reliably enforce expiration (ADR-002 S3 — device_time is advisory). The device provides best-effort warnings. Authoritative temporal validation happens on sync.

3. **Accept-and-flag is uniform**: Expired campaign authority is handled identically to stale scope authority (S03 Act 4) — the same mechanism, the same flag type structure, the same resolution workflow. This confirms that authorization staleness is a single concern, not a collection of special cases.

---

## 5. Scenario Group 4: S14 — Multi-Level Distribution

### Act 1: Central warehouse initiates distribution to 5 districts

| Question | Answer |
|----------|--------|
| **Actor** | Warehouse coordinator, operating from central location with reliable connectivity. Creating a Process (ADR-002: transient, scoped to this distribution cycle) and shipment events. |
| **Data required on device** | Administrative view: district inventory, distribution plan, target quantities. |
| **Access rule evaluated** | Coordinator has authority to create distributions within their scope (the entire district set under their coordination). |
| **Rule enforcement point** | **Server-side.** Central operations are online. |
| **Stale rule scenario** | Minimal — online operation. |
| **Sync scope implication** | The distribution process and initial shipment events must propagate to district-level actors. Each district receives only the shipment events relevant to them — not the full distribution across all 5 districts. |

🔴 **Hot Spot HS-10**: **Cross-level visibility boundaries.** Does the warehouse coordinator's distribution event contain per-district quantities? If yes, and if district hubs sync the same event, each district hub can see the quantities allocated to other districts. This may be acceptable (transparency) or problematic (competitive sensitivity between districts, or simply unnecessary data on low-bandwidth connections). Options: (a) one event, all districts see everything; (b) per-district events, each district sees only their allocation; (c) one event with per-district visibility filtering in the projection. Option (b) is cleanest for sync scope but creates 5 events where 1 would suffice.

### Act 2: District hub receives and confirms receipt

| Question | Answer |
|----------|--------|
| **Actor** | District supervisor/coordinator, at district hub (intermittent connectivity). |
| **Data required on device** | The shipment event for their district, expected quantities, confirmation form. |
| **Access rule evaluated** | "Actor has assignment to this district hub with role `district_coordinator` or `supply_manager`." Receipt confirmation requires supply management authority, not just regular supervision authority. This is a **role-specific permission** within a geographic scope. |
| **Rule enforcement point** | **Device-local** for the confirmation event. **Server-side** on sync for validation. |
| **Stale rule scenario** | District coordinator was replaced while a shipment was in transit. The new coordinator confirms receipt. This is clean — the assignment is current. The old coordinator confirming receipt after their assignment ended: accepted and flagged (same as S03 Act 4). |
| **Sync scope implication** | The district hub's device needs: shipment events sent to this district, the distribution process event (for context), and prior shipments for historical reference. It does NOT need shipment events for other districts. **Sync scope is district-scoped.** |

### Act 3: District hub distributes to 20 community points

| Question | Answer |
|----------|--------|
| **Actor** | District coordinator, creating sub-distribution events — one per community delivery point. |
| **Data required on device** | List of community delivery points in the district, target quantities per point, available stock (from Act 2 receipt). |
| **Access rule evaluated** | District coordinator has authority over all community points within their district. The sub-distribution is creating Process events (or extending the parent Process with child Process references) scoped to each community point. |
| **Rule enforcement point** | **Device-local** (may be offline at community point). The district coordinator's scope includes all community points in the district. |
| **Stale rule scenario** | A community point was reassigned to a different district. The district coordinator (offline) distributes to a community point that's no longer in their scope. Accepted and flagged on sync. |
| **Sync scope implication** | The sub-distribution events must reach each community point's assigned worker on sync. Each community worker receives only the sub-distribution event for their point. |

### Act 4: Community health worker confirms receipt at their level

| Question | Answer |
|----------|--------|
| **Actor** | CHV or community-level worker, confirming receipt of supplies at their community point. |
| **Data required on device** | The sub-distribution event for their community point, expected quantities. |
| **Access rule evaluated** | "Actor has assignment to this community point." Standard scope check — identical to S20 Act 1. |
| **Rule enforcement point** | **Device-local.** |
| **Stale rule scenario** | Same as S03 — reassignment during transit. |
| **Sync scope implication** | Community worker's device needs: their sub-distribution event, their own prior receipt events. Does NOT need the parent distribution event or other community points' events. |

### Visibility Analysis Across Levels

| Level | Can see... | Cannot see... | Why |
|-------|-----------|---------------|-----|
| **Warehouse coordinator** | All 5 district allocations, all district confirmations, all community confirmations (aggregated) | Individual community-level event details (unless drilling down) | Geographic scope spans all districts; aggregation for manageability |
| **District coordinator** | Their district's allocation, their 20 community sub-distributions, community confirmations | Other districts' allocations or community data | Geographic scope limited to their district |
| **Community worker** | Their community point's sub-distribution, their own confirmation | Other community points' data, district-level totals | Geographic scope limited to their community |

🔴 **Hot Spot HS-11**: **Upward visibility aggregation.** The warehouse coordinator needs aggregated visibility across 5 districts × 20 communities = 100 data points. Options: (a) raw events from all 100 points sync to the coordinator's device — works but heavy on bandwidth; (b) district-level summaries computed by each district hub and synced upward — lighter transfer, but requires computation at the district level; (c) server-computed aggregation — the coordinator queries the server for aggregated views. Option (c) is most natural for coordinators (who have reliable connectivity per C.3). This reinforces HS-4: **projection computation location** is tier-dependent.

### Multi-Level Distribution Summary

Distribution reveals a **hierarchical scope model** where:

1. Each level sees only its immediate operational context by default (its inbound shipments, its outbound distributions, its confirmations).
2. Upward visibility is through **aggregated projections**, not raw events from lower levels.
3. Scope is strictly geographic/organizational — a district coordinator sees all community points in their district, not in other districts.
4. The authorization rule at each level is the same: `actor.assignment.scope ⊇ entity.location`. The scope just gets wider at higher levels.

**Key finding for sync topology**: Multi-level distribution argues for **tiered sync** — field workers sync raw events to a server, supervisors and coordinators read server-computed projections (or selectively sync the events they need for drill-down). This is consistent with constraints.md: coordinators have reliable broadband and do not need offline access to granular field data.

---

## 6. Scenario Group 5: S19 — Offline Capture (Auth Lens)

### Act 1: Worker syncs and receives current access rules + data for their scope

| Question | Answer |
|----------|--------|
| **Actor** | Field worker, at a connectivity point. |
| **Data required on device** | After sync, the device has: (1) current assignment(s) with scope, (2) all subjects within scope, (3) all events for in-scope subjects, (4) reference data (forms, protocols). This represents the worker's **authorization snapshot** — the complete set of access rules and data applicable to their role and scope as of this sync. |
| **Access rule evaluated** | No access rule is evaluated — this is the moment the rules are delivered. |
| **Rule enforcement point** | **Server-side** — the server determines what to send based on the worker's current assignments. |
| **Stale rule scenario** | Not applicable — this is the freshest state the worker will have until next sync. |
| **Sync scope implication** | The sync payload is the **materialization of the access policy** for this device. Whatever the server sends defines what the device can work with. The assignment events on the device are the local copy of the access rules. |

**Key finding**: **The sync payload IS the access control mechanism for offline operation.** The device doesn't evaluate authorization rules against an abstract policy engine. It works with the data it has. The data it has is determined by the server's scope filter at sync time. Sync scope = de facto authorization scope while offline. Authorization "enforcement" on-device is really just "the device only has data for in-scope subjects."

### Act 2: Worker goes offline for 3 days

| Question | Answer |
|----------|--------|
| **Actor** | Field worker, operating offline. |
| **Data required on device** | Everything received in Act 1. No changes possible — no sync. |
| **Access rule evaluated** | Device-local scope checks against local state. All subjects in local registry are accessible. |
| **Rule enforcement point** | **Device-local only.** No server involvement possible. |
| **Stale rule scenario** | Not stale yet — but the staleness clock is ticking. State divergence between device and server grows with every hour. |
| **Sync scope implication** | The device is a snapshot that ages. No sync scope changes possible. |

### Act 3: During 3 days — role changed, scope narrowed, subject merged outside worker's scope

Three things happen centrally while the worker is offline:

**3a. Role change**: Worker's role is changed from `field_worker` to `trainee` (reduced permissions — can observe but not treat).

| Question | Answer |
|----------|--------|
| **Impact on offline worker** | None until sync. The device doesn't know about the role change. The worker continues with full field_worker permissions. |
| **On sync** | Events created under the old role are accepted (A2.6). If `trainee` cannot create treatment events, any treatment events from the offline period are flagged as "created under stale role authority." The data is preserved. |

**3b. Scope narrowing**: Worker's assignment scope is reduced from "all of Village X" to "Zone 3 of Village X only."

| Question | Answer |
|----------|--------|
| **Impact on offline worker** | None until sync. The worker continues visiting all zones. |
| **On sync** | Events about subjects in Zones 1, 2, 4 are accepted and flagged as "out of current scope." The flag resolution determines: are these events attributed to the worker (who did the work) or to the worker now responsible for those zones? |

**3c. Subject merge involving an out-of-scope subject**: Subject A (in worker's scope) is merged with Subject B (outside worker's scope) while the worker is offline. The surviving ID is Subject B's UUID.

| Question | Answer |
|----------|--------|
| **Impact on offline worker** | The worker still sees Subject A in their local registry. They record events against Subject A's UUID. |
| **On sync** | The merge event syncs to the worker's device (because Subject A is in their scope). The alias table updates: A → B. The worker's events referencing Subject A are projected through the alias to Subject B. But Subject B may be outside the worker's scope. This creates a **scope-crossing alias** — the worker's events now point to a subject they are not authorized to access. |

🔴 **Hot Spot HS-12**: **Scope-crossing merge.** When a merge causes a subject to "leave" a worker's scope (because the surviving ID is in a different scope), what happens? Options: (a) the worker's events follow the alias — they are now associated with an out-of-scope subject, and the worker loses visibility into their own work; (b) the worker's scope expands to include the surviving subject — but this is an authorization change triggered by an identity operation, which conflates two concerns; (c) the alias is applied in projection only at the server level — the worker's device continues to show Subject A in their local view, and the merge is reflected only in server-side cross-scope projections. Option (c) preserves the worker's operational view while allowing the server to maintain the unified subject record.

### Act 4: Worker (offline, stale rules) records events against deauthorized subjects

| Question | Answer |
|----------|--------|
| **Actor** | Worker, offline, unaware of role change (3a), scope narrowing (3b), or subject merge (3c). |
| **Data required on device** | Unchanged from Act 1 — device is a stale snapshot. |
| **Access rule evaluated** | Device-local checks pass against stale state. |
| **Rule enforcement point** | **Device-local passes (stale).** |
| **Stale rule scenario** | The worker has recorded events that are "wrong" from the server's current perspective in three different ways: wrong role (3a), wrong scope (3b), and against a merged subject (3c). Each of these is structurally handled by the same mechanism. |
| **Sync scope implication** | All events are accepted (A2.6). Each type of staleness produces a flag with different metadata but the same structural pattern. |

### Act 5: Worker syncs — what happens?

The critical question: **is authorization staleness handled by the same accept-and-flag mechanism as identity staleness (ADR-002 S14)?**

**Answer: Yes, with the same mechanism but different flag types.**

| Staleness type | ADR-002 analog | Flag type | Flag metadata | Resolution |
|----------------|----------------|-----------|---------------|------------|
| Stale role (3a) | Event referencing deactivated subject | `RoleStaleFlag` | `{event_id, actor_id, role_at_creation, role_at_sync}` | Coordinator reviews: is the event valid under the new role? If not, the event stands as a record but downstream policies don't fire. |
| Stale scope (3b) | Event in wrong scope | `ScopeStaleFlag` | `{event_id, actor_id, scope_at_creation, scope_at_sync, subject_id}` | Coordinator reviews attribution. Data is valid; responsibility attribution may change. |
| Stale identity (3c) | Subject merged — ADR-002 S6 | `StaleReferenceFlag` (already defined in ADR-002) | `{event_id, subject_id_referenced, surviving_id}` | Alias resolves in projection. If surviving subject is cross-scope, flag for supervisor review (HS-12). |

**Why the same mechanism works**: All three staleness types share the same structural properties:
1. The event was valid at the time of creation (device state was consistent)
2. The event cannot be rejected (A2.6, P3)
3. Someone must review the discrepancy
4. The resolution is itself an event

The only difference is the **severity**: identity staleness (ADR-002) is usually resolvable mechanically (alias in projection). Role and scope staleness have operational implications (should this treatment have happened? was the right person responsible?). But the mechanism — append a flag event, designate a resolver, surface for review — is identical.

**Key insight**: Authorization staleness is **NOT** a separate mechanism from identity staleness. It is the same accept-and-flag mechanism with an expanded taxonomy of flag types. ADR-003 adds flag types (RoleStaleFlag, ScopeStaleFlag, TemporalAuthorityExpiredFlag); it does not add a new conflict-handling mechanism.

---

## 7. Cross-Cutting Synthesis

### Q1: Scope Representation

**How is "scope" represented in the authorization model?**

The scenarios reveal a **geographic-primary, composable** scope model:

| Scope type | Where discovered | How expressed |
|------------|-----------------|---------------|
| **Geographic** | S20 (CHV area), S21 (supervisor region), S14 (warehouse → district → community) | Organizational hierarchy: each level maps to a geographic area. Assignment binds actor to a node in this hierarchy. |
| **Subject-based** | S20 Act 2 (specific subjects in scope through geographic containment) | Not primary — subjects are in scope because they are *within* a geographic area, not because they are individually assigned. |
| **Activity-based** | S09 (campaign scope) | Additive temporal overlay — the campaign defines an additional scope (geographic or subject-list) active for a time window. |
| **Composable** | S09 Act 2 (standing + campaign), S14 (hierarchy nesting) | Effective scope = union of all active assignments' scopes. Each event records which assignment authorized it. |

**Finding**: The primary scope axis is **geographic hierarchy** (village → zone → district → region → national). Assignments bind actors to nodes in this hierarchy. A worker assigned to "Village X" is authorized for all subjects geolocated within Village X. A supervisor assigned to "District Y" is authorized for all workers and subjects within District Y's constituent villages.

Campaign scope is an **additive overlay** — it temporarily expands a worker's effective scope to include additional geographic areas or specific subject lists. The overlay is time-bounded and tagged, so events created under it can be distinguished from standing work.

Subject-based scope (assign specific subjects, not areas) **may be needed** for specialized cases (e.g., case management — S08 — where a case worker follows a specific individual across geographic boundaries). This did not surface in the Phase 1 scenarios analyzed but should be stress-tested in Phase 2.

**Cannot determine yet — needs Phase 2 stress test**: Whether subject-based scope is a first-class primitive or a degenerate case of geographic scope with area = {specific subject's location}.

### Q2: Sync Scope vs. Access Scope

**Does a device receive exactly what the user is authorized to see, more, or less?**

The scenarios reveal a **sync scope = access scope** model with one qualification:

| Option | Trade-off | What scenarios revealed |
|--------|-----------|------------------------|
| **Sync scope = access scope** | Clean, simple, no UI-level authorization needed. But: scope changes require data addition/removal on sync. | S20 (CHV gets exactly their subjects), S03 (scope change = sync scope change), S14 (each level gets exactly their events). **This is the dominant pattern.** |
| **Sync scope > access scope** (more data, UI restricts) | Enables pre-fetching, smoother scope transitions. But: data on device is accessible to anyone with physical access. On low-end Android with potential device sharing, this is a security risk. | Not supported by any scenario. Violates data minimization. |
| **Sync scope < access scope** (less data, fetch on demand) | Reduces bandwidth. But: user can't work offline on data they're authorized for but don't have. Violates C.4 (capture must be instant regardless of connectivity). | Contradicts P1 (offline is the default). Not viable for field workers. |

**Finding**: **Sync scope = access scope for field workers.** The device receives exactly the data the worker is authorized to act on. No more (security/privacy), no less (offline operability).

**Qualification for supervisors and coordinators**: Supervisors may receive a **hybrid** — raw events for subjects they directly interact with (review visits), plus server-computed summaries for their aggregate view (HS-4). This is not "more than authorized" — it's a different representation of authorized data. Coordinators likely don't sync data to their devices at all — they query the server directly, relying on their reliable connectivity (C.3).

**Cannot determine yet — needs Phase 2 stress test**: The exact boundary between "raw events synced to supervisor device" and "server-computed summary synced as a projection" depends on the supervisor's offline requirements. If supervisors must do review visits while offline (S21), they need raw events for the specific CHVs they're visiting. If their aggregate view can wait until they're online, server-computed summaries suffice for the dashboard.

### Q3: Projection Location

**Where do projections live?**

The scenarios reveal a **tier-dependent hybrid** model:

| Tier | Projection source | What scenarios revealed | Rationale |
|------|-------------------|------------------------|-----------|
| **Field workers** | Device-local projections from raw events | S20 Acts 1–2: CHV builds subject history view from local events. Simple projection (latest state of a subject from its event stream). | Low-end devices can handle per-subject projections. The subject set is small (1 village). ADR-001's escape hatch (add application-maintained views) is available if projection complexity proves too high. |
| **Supervisors** | Hybrid: raw events for drill-down + server-computed summaries for aggregation | S21 Act 6: supervisor needs both individual CHV data (for visit review) and aggregate view (for oversight dashboard). | Supervisor devices are more capable (tablets, C.2) but raw events from 10–20 CHVs may be too much for projection on-device. Summaries are lighter to transfer and compute. |
| **Coordinators** | Server-computed projections, accessed online | S14 Act 1: warehouse coordinator needs cross-district aggregate. S09 Act 1: campaign coordinator needs cross-area progress. | Coordinators have reliable broadband (C.3). No offline requirement for aggregate views. Querying the server is natural. |

**Finding**: Projections are not one-size-fits-all — they are tiered:

1. **Field tier**: device-local projection from raw events. Projection scope = sync scope = assignment scope. Simple, self-contained.
2. **Supervisor tier**: device holds raw events for direct reports (synced), plus receives pre-computed summary projections from the server on sync. Supervisor can do offline review visits using raw events; dashboard view uses server-computed summaries.
3. **Coordinator tier**: no device-side projection. Coordinators query server-side projections through a web interface. The server maintains projections from all events in the coordinator's scope.

This implies a **two-tier sync topology**: field workers ↔ server (raw events), supervisors ↔ server (raw events + summary projections), coordinators → server (queries only). This is NOT a relay topology (field worker does not sync through supervisor). Every device syncs directly with the server; the distinction is what gets synced.

### Q4: Temporal Access

**How are time-bound access grants represented?**

From S09 (coordinated campaign) analysis:

1. **Campaign (time-bound scope) is a Process** (ADR-002 typed identity — transient, scoped to a workflow). The Process event carries temporal bounds: `{valid_from, valid_until}`.

2. **Campaign participation is an Assignment** (ADR-002 typed identity — temporal, binds actor to scope). The Assignment event references the Process and carries its own temporal bounds, which may be a subset of the campaign's bounds.

3. **Temporal enforcement is server-side only.** The device cannot reliably determine whether a temporal bound has passed because `device_time` is advisory (ADR-002 S3). The device provides best-effort warnings ("this campaign was scheduled to end on date X"). Authoritative enforcement happens on sync.

4. **Expired-authority events are accepted and flagged.** The flag type is `TemporalAuthorityExpiredFlag` with metadata: `{event_id, assignment_id, process_id, expiry_bound, event_device_time}`.

**How does the device know a grant has expired if it hasn't synced?** It doesn't, reliably. The device can check `device_time > assignment.valid_until`, but this check is advisory. The platform should surface this as a UI warning, not a hard block. The hard block is server-side: on sync, events created after the assignment's `valid_until` (determined by the server's knowledge of when the assignment was ended, not by device_time) are flagged.

**Determination**: Time-bound access grants are **events** — specifically, Assignment events with temporal bounds. They sync to devices like any other event. Expiration is enforced server-side on sync, not device-side at capture time. This is consistent with C.7 ("work in progress under old configuration completes under old rules") and A2.6 (events never rejected for state staleness).

### Q5: Conflict Resolver Designation

**Who designates the resolver? Is it role-based? Scope-based?**

ADR-002 S11 states: "The designated resolver is determined by the conflict type and the subject's current assignment context (e.g., the assigned supervisor for that subject's scope)."

From the scenarios:

| Conflict type | Designated resolver | Rationale from scenarios |
|---------------|-------------------|--------------------------|
| **Stale scope** (S03 Act 4) | The supervisor or coordinator responsible for the area where the event was recorded | They have visibility into both the old and new assignment — they can determine whether the event data should be attributed to the new assignee or left with the original worker. |
| **Stale role** (S19 Act 3a) | The coordinator who changed the role | They made the role change decision and can determine whether the in-flight work is valid under the new role. |
| **Expired campaign authority** (S09 Act 5) | The campaign coordinator | They own the campaign process and can determine whether post-expiration work should count. |
| **Cross-scope merge** (S19 Act 3c, HS-12) | The coordinator whose scope spans both the old and new subject locations | Only someone with visibility into both scopes can resolve the attribution. |
| **Data conflict** (ADR-002 — concurrent state changes) | The supervisor of the subject's geographic scope | Standing rule — the person with oversight of the subject area. |

**Pattern**: The resolver is the **nearest actor in the hierarchy whose scope encompasses all parties to the conflict**. For a stale-scope event in Village X, the resolver is the district supervisor who oversees Village X. For a cross-scope merge spanning two districts, the resolver is the regional coordinator.

**What happens if the designated resolver's role changes while a conflict is pending?** This is a real risk:

1. If the resolver is reassigned (moved to a different district), the conflict needs re-designation. This is an administrative event: `ConflictResolverReassigned`.
2. If the resolver's role is downgraded (supervisor → field worker), they can no longer resolve. Same: re-designation required.
3. The platform must track pending conflicts per resolver. When the resolver's assignment changes, any pending conflicts must be re-routed to the new holder of that scope.

🔴 **Hot Spot HS-13**: **Orphaned conflicts.** What happens when a conflict's designated resolver leaves without resolving it? If the reassignment happens while the resolver is offline, they may resolve the conflict under stale authority. The resolution is then itself under stale authority — flagged as an unauthorized resolution (ADR-002 S11: "Resolution events from other actors are accepted but flagged as unauthorized"). This creates a meta-flag: a flag on a flag resolution. This is bounded — it can't recurse infinitely because the re-designation is an online-only coordinator operation — but it is operationally messy.

**Is conflict resolver designation an authorization rule like any other, or a special case?** It is an authorization rule — specifically, an **assignment-derived permission**. The same scope model that grants "CHV can observe subjects in Village X" also grants "District Supervisor can resolve conflicts about subjects in District Y." The resolver designation is not a separate system — it's a derived consequence of the hierarchy scope model. The resolver for a conflict about Subject S is: the actor whose assignment scope is the narrowest scope containing Subject S's location at role ≥ supervisor.

---

## 8. Hot Spot Inventory

| # | Hot Spot | Scenario | Severity | Cross-references |
|---|---------|----------|----------|-----------------|
| HS-1 | Subject history completeness on reassignment — must the new assignee receive all prior events? | S20 Act 2 | Medium | Affects sync payload size, continuity of care. Interacts with HS-3 (data removal). |
| HS-2 | Assessment visibility to the assessed CHV — does the CHV receive supervisor assessment events about them? | S21 Act 4 | Low | Introduces second sync dimension (geographic scope + "about me" scope). May complicate sync scope model. |
| HS-3 | Data removal on scope narrowing — purge, hide, or retain subjects no longer in scope? | S20 Act 5 | High | Security, privacy, device storage. Interacts with HS-1 (history completeness). Must be decided in ADR-003. |
| HS-4 | Projection computation location for supervisors — raw events vs. server summaries vs. hybrid | S21 Act 6 | High | Determines sync topology. Interacts with Q3. Directly affects bandwidth and device capability requirements. |
| HS-5 | Stale-scope flag severity — informational, soft conflict, or hard conflict? | S03 Act 4 | Medium | Determines whether downstream policies fire on stale-scope events. C.7 argues for informational. |
| HS-6 | Scope transition atomicity on sync — must assignment change + new data arrive together? | S03 Act 5 | Medium | Protocol design concern. Partial sync during scope transition could leave device in inconsistent state. |
| HS-7 | Scope composition model — union, tagged union, or independent assignments? | S09 Act 2 | High | Affects event envelope design (authority context), campaign reporting, post-campaign cleanup. |
| HS-8 | Temporal access enforcement — device can't reliably enforce time bounds (device_time is advisory) | S09 Act 4 | High | Fundamental: device-side temporal enforcement is impossible. Server-side only. Design must accommodate this. |
| HS-9 | Grace period for expired authority — auto-accept within window, or always flag? | S09 Act 6 | Low | Policy-level question. Can be a configurable strategy rather than a structural decision. |
| HS-10 | Cross-level visibility boundaries in distribution — can districts see each other's allocations? | S14 Act 1 | Medium | Affects event granularity (one event per distribution vs. one per district). Interacts with sync scope filtering. |
| HS-11 | Upward visibility aggregation — raw events from all levels, or per-level summaries synced upward? | S14 Act 4 | High | Directly determines sync topology for supervisors and coordinators. Reinforces Q3. |
| HS-12 | Scope-crossing merge — when a merge moves a subject outside a worker's scope | S19 Act 3c | Medium | Identity × authorization interaction. The alias mechanism (ADR-002) must account for scope boundaries. |
| HS-13 | Orphaned conflicts — resolver's role changes while conflicts are pending | Q5 synthesis | Medium | Meta-flags (flags on flag resolutions under stale authority). Bounded but operationally messy. |

### Severity rationale

- **High** (HS-3, HS-4, HS-7, HS-8, HS-11): These must be resolved in ADR-003. They affect the event envelope, sync protocol structure, or fundamental enforcement model. Deferring them creates unresolvable ambiguity for downstream ADRs.
- **Medium** (HS-1, HS-5, HS-6, HS-10, HS-12, HS-13): These should be addressed in ADR-003 but could be resolved in Phase 2 stress testing. They are tensions that have plausible default resolutions.
- **Low** (HS-2, HS-9): These are policy-level or deployment-configurable. They do not affect the structural authorization model.

---

## 9. Emergent Questions

These questions were **not in the prompt** but emerged during scenario analysis:

### EQ-1: Authority Context in the Event Envelope

ADR-001 S5 anticipated: "every event records the authority under which it was performed." The scenarios reveal what this must contain:

- **Actor reference**: `{type: actor, id: uuid}` — who performed the action (already in ADR-002)
- **Assignment reference**: `{type: assignment, id: uuid}` — which assignment authorized this action (standing or campaign)
- **Process reference** (optional): `{type: process, id: uuid}` — if the action was part of a campaign or distribution process

This is not a simple `role` field. It is a **compound authority context** that traces the action to a specific grant. The event envelope gains an `authority_context` section that ADR-003 must define.

### EQ-2: Sync Scope as the Primary Offline Authorization Mechanism

The analysis revealed that for offline operation, **sync scope IS the access control mechanism.** The device doesn't evaluate access rules against a policy engine — it works with the data it has, and it only has data the server determined it should have. This means:

- "Device-local access control" is mostly a UI concern (what to show), not a policy enforcement concern
- The hard access control decision is made **at sync time by the server** when computing the sync payload
- The event envelope's authority context is for **auditability and post-sync validation**, not for device-local gatekeeping

This simplifies the device-side architecture dramatically: the device does not need a policy engine. It needs a scope check ("is this subject in my local registry?") and a role check ("am I a field worker or supervisor?"). Both are derived from locally synced assignment events.

### EQ-3: The Assignment as the Atomic Unit of Authorization

Across all scenarios, the **Assignment** (ADR-002 typed identity) emerged as the atomic unit of authorization. An assignment binds:

- An **actor** to a **scope** (geographic hierarchy node or subject list)
- With a **role** (field_worker, supervisor, coordinator, auditor)
- For a **duration** (standing or time-bound)
- Under a **process** (standing operations or specific campaign)

Every access rule reduces to: "Does the actor have an active assignment whose scope contains the target entity and whose role permits the intended action?" This is a single check. The assignment event is the portable representation of this grant.

This suggests that the authorization model is not RBAC (roles are necessary but insufficient — they need scope) and not ABAC (attributes are too generic — the assignment is a structured, typed grant). It is **assignment-based access control** — a scope-bound, role-qualified, temporally-bounded grant materialized as an event.

### EQ-4: Asymmetry Between Scope Expansion and Scope Contraction

Scope expansion (new campaign, reassignment to a larger area) is **additive** — the sync delivers new data, and the device grows. Scope contraction (campaign ends, reassigned to a smaller area, role downgrade) is **subtractive** — data that was on the device may need to be removed or hidden.

Additive is easy. Subtractive is hard. (This is the same asymmetry as ADR-002's merge vs. unmerge.) The sync protocol must handle both, but contraction introduces HS-3 (data removal) which has security, privacy, and operational implications that expansion does not.

### EQ-5: Device-Sharing and Actor vs. Device Identity

ADR-002 S5 says `device_id` is hardware-bound, and a single actor may use multiple devices. The inverse is also operationally real: **multiple actors may share a single device** (common in resource-constrained deployments — a shared phone at a health post). When Worker A logs in, the device should show Worker A's scope. When Worker B logs in, it should show Worker B's scope. This means:

- Sync scope is **actor-scoped**, not device-scoped — the device stores data for the currently-logged-in actor(s)
- If the device stores data for multiple actors simultaneously, it must enforce per-actor visibility locally
- If the device purges data on actor switch, it must re-sync on each login (expensive on 2G)

This was not explored in any scenario but is a real deployment constraint that ADR-003 must address.

### EQ-6: Auditor Access as a Scope Exception

The access control scenario document mentions auditors: "their access patterns cut across normal organizational boundaries." Auditors don't fit the geographic hierarchy model — they get temporary, cross-cutting access to specific data for audit purposes. This is a different kind of assignment: not geographic, not campaign-scoped, but **query-scoped** (access to events matching certain criteria across organizational boundaries).

This is the least constrained access pattern and the hardest to model within the assignment framework. It was not stressed by any Phase 1 scenario. It should be added to Phase 2 stress testing.
