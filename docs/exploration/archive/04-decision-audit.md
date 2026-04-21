> **⚠️ SUPERSEDED** — This is a raw exploration document archived for reference. Decisions were finalized in **ADR-001**. For a navigated reading guide, see [guide-adr-001.md](../guide-adr-001.md).
# Decision Audit: ADR Scoping and Normalization

> Purpose: Ensure decisions captured in ADRs are complete, correctly scoped, and based on prior exploration — without gaps, overlaps, or premature locking.
>
> Inputs: ADR-001, exploration documents (architecture landscape, ADR-1 exploration, ADR-1 forward projection), all Phase 1 scenarios, principles, constraints, viability assessment, access control cross-cut.
>
> Status: Historical, all ADR001's open questions were sattled
>
> Created: 2026-04-09

---

## 1. Extracted Decision List From Exploration

Every decision — explicit or implicit — found across the three exploration documents.

### From Architecture Landscape

| # | Decision | Status | Where captured |
|---|----------|--------|----------------|
| L1 | Hybrid architecture: event-sourced storage + metadata-driven config + composable primitive vocabulary | Directional consensus, not committed | architecture-landscape.md §3 |
| L2 | ADR sequence: Offline Data Model → Identity/Conflict → Auth/Sync → Configuration → Workflow | Accepted as ordering | architecture-landscape.md §6 |
| L3 | Five hard constraints eliminate broad families (offline-first, configurable-not-coded, trustworthy records, scale, grows-without-breaking) | Validated | architecture-landscape.md §1 |
| L4 | Offline data model is root of dependency tree | Validated | architecture-landscape.md §5 |

### From ADR-1 Exploration

| # | Decision | Status | Where captured |
|---|----------|--------|----------------|
| E1 | S1: Records are append-only (forced by V3, P3, S00 edge) | Forced — no option space | exploration §S1 |
| E2 | S2: Three write-granularity options survive (snapshots, events, action log) | Explored, not decided in exploration | exploration §S2 |
| E3 | S3: Client-generated UUIDs (forced by V1, P1) | Forced — no option space | exploration §S3 |
| E4 | S4: Sync unit = immutable record, idempotent/append-only/order-independent | Follows from E1+E3 | exploration §S4 |
| E5 | S5: Conflict detection must surface, not silently resolve (forced by P5) | Forced — policy layer, model-independent | exploration §S5 |
| E6 | Events provide richer conflict context than snapshots (action vs. state) | Observation, not decision | exploration §S5 |
| E7 | Storage/performance is NOT a differentiator between options at expected scale | Validated | exploration §Device Storage |

### From Forward Projection

| # | Decision | Status | Where captured |
|---|----------|--------|----------------|
| F1 | Events (B) hold safest irreversibility position: B→C cheap, C→B risky, A→anything lossy | Analysis finding | forward-projection §Irreversibility |
| F2 | Action Log (C) converges toward event-sourcing under pressure (conflicts, sync, workflow) | Analysis finding | forward-projection §Narrowing |
| F3 | Snapshots (A) create structural ceiling at ADR-4/5 (workflows hard-coded, not configured) | Analysis finding | forward-projection §ADR-4, §ADR-5 |
| F4 | The B→C escape hatch requires the event log to be gap-free | Critical constraint on escape hatch viability | forward-projection §Where Option B could fail |
| F5 | Events enable two-tier sync (full streams for workers, summaries for supervisors) | ADR-3 possibility opened by ADR-1 choice | forward-projection §ADR-3 |
| F6 | Event type ownership (platform vs. config) draws the first line of configuration boundary | ADR-4 territory | forward-projection §ADR-4 |
| F7 | State machines emerge from projection rules; data and workflow are separate events | ADR-5 territory | forward-projection §ADR-5 |

---

## 2. ADR-001 Decision Breakdown

What ADR-001 actually decides, claim by claim.

### Correctly scoped to ADR-001

These decisions are properties of the storage primitive. They belong here.

| # | Decision | Justification |
|---|----------|---------------|
| A1 | All writes are append-only | Storage primitive semantics. Forced by V3/P3. |
| A2 | Atomic write unit = typed immutable event | Storage primitive choice. Core of ADR-1. |
| A3 | Event log is single source of truth | Distinguishes Events from Action Log. Core. |
| A4 | Projections are rebuildable from the event stream | Follows from A3. Defines the source-of-truth guarantee. |
| A5 | Client-generated UUIDs for all identifiers | Storage-level identity generation. Forced by V1/P1. |
| A6 | Sync unit = immutable event | Follows from A2. |
| A7 | Sync is idempotent, append-only, order-independent | Properties of syncing immutable events. |
| A8 | Envelope expresses: identity, type, payload, timestamp | Minimum envelope for the storage primitive. |
| A9 | Envelope is extensible for downstream ADRs | Structural openness. |
| A10 | Subject association, references, schema versioning, authorship, authority context are required but shaped downstream | Correct deferral of shape details. |

### Decided in ADR-001 but belongs elsewhere

These are commitments ADR-001 makes in its Consequences section that pre-decide downstream ADRs.

| # | Claim in ADR-001 | Should belong to | Problem |
|---|------------------|-----------------|---------|
| X1 | "Adding a new event type is platform evolution, not deployment configuration. This draws the first line of the configuration boundary." | **ADR-4** | This is THE configuration boundary question. ADR-4 hasn't been explored yet. It might be correct, but it's being committed without exploration. |
| X2 | "State machines emerge from projection rules applied to event sequences." | **ADR-5** | This forecloses alternative workflow models (explicit state machine primitives, state-transition tables). ADR-5 should decide this. |
| X3 | "Data and workflow actions are separate events that compose on the same subject." | **ADR-5** | Same issue. This constrains ADR-5's solution space. |
| X4 | "Two-tier sync is possible — full event streams for subjects a user works on, pre-computed projections for subjects they only oversee." | **ADR-3** | This previews a sync topology that ADR-3 should explore and decide. |
| X5 | "Projections (materialized views of current state) are maintained on-device and server for read performance." | **ADR-3** | Where projections live is a sync/device-strategy question. ADR-1 should say projections exist, not where they live. |
| X6 | "Authorization rules must be evaluable against locally projected state." | **ADR-3** | This constrains authorization design. True consequence of the event model, but the specific constraint should be stated in ADR-3, not assumed in ADR-1. |

### Missing from ADR-001 but required

| # | What's missing | Why it matters |
|---|---------------|---------------|
| M1 | **Write-path discipline rule**: the event store is always the sole write path. Projections/views are never written to independently. If a projection is wrong, the fix is to rebuild from events, not to patch the projection directly. | This is what makes the B→C escape hatch viable. Without it, the "single source of truth" guarantee is aspirational, not structural. The forward projection explicitly notes the escape hatch fails if the event log has gaps. The ADR must make this a hard rule, not an architectural aside. |
| M2 | **Projection rebuild scope**: whether "rebuildable" means from the full event stream globally, or from whatever events a device has locally. | ADR-1 says projections are "always rebuildable from the event stream." But a device may not have the full stream — it has a filtered subset (determined by ADR-3). Can a device rebuild its local projections from its local events? Or does rebuild require re-syncing? This affects offline resilience. ADR-1 should state the guarantee; ADR-3 decides the scope. |

---

## 3. Decision → ADR Mapping

### ADR-001: Offline Data Model

**Scope**: The storage primitive — what gets written, how, and what guarantees it provides.

**Should contain**: A1–A10, M1, M2.

**Should NOT contain**: X1–X6. These should be restated as "possibilities the event model opens" or removed entirely and addressed in their proper ADRs.

### ADR-002: Identity Model and Conflict Resolution

**Scope**: How subjects are identified, how duplicate real-world subjects are handled, how concurrent events are ordered, and what constitutes a conflict.

**Must decide**:
- Subject identity model: UUID-only? Composite keys? External identifiers?
- Subject lifecycle events: creation, merge, split, deactivation — what events represent these?
- Causal ordering mechanism: HLC, vector clocks, Lamport timestamps, or something simpler?
- Conflict definition: what pattern of events constitutes a conflict requiring resolution?
- Conflict resolution strategy: automatic (where provably safe), human triage (otherwise), or configurable?
- Event reference semantics: how an event structurally points to prior events (corrections, reviews, supersessions)

**Inputs from ADR-001**: A5 (client UUIDs), A2 (events carry references), A3/A4 (conflicts are visible in the event stream). The "What This Does NOT Decide" table in ADR-001 scopes this correctly.

### ADR-003: Authorization and Selective Sync

**Scope**: Who sees/does what, what data lives on which device, and how access rules interact with offline reality.

**Must decide**:
- Authorization model representation (role + scope + context)
- On-device enforcement of access rules against projected state
- Sync scope: what data flows to which device
- Sync topology: full events vs. summaries, one-tier vs. two-tier (X4 deferred here)
- Where projections live: device, server, or both (X5 deferred here)
- Projection rebuild scope: what "rebuildable" means per-device (M2 stated in ADR-1, resolved here)
- Stale access rule handling
- Evaluability constraint: authorization rules must work against local state (X6 deferred here)

### ADR-004: Configuration Paradigm and Boundary

**Scope**: What is configurable vs. what requires platform development. How configurations are defined, versioned, and propagated.

**Must decide**:
- Event type vocabulary ownership: platform vocabulary vs. deployment configuration (X1 deferred here)
- Shape/schema definition mechanics
- Configuration change propagation to devices
- Configuration versioning and coexistence (old config + new config on same device)
- The T2 boundary: where configuration stops and a programming language begins

### ADR-005: State Progression and Workflow

**Scope**: How work moves through stages and how workflow interacts with the event model.

**Must decide**:
- Whether state machines are projection-derived (X2) or explicit primitives or both
- Whether data and workflow events are separate types (X3) or unified
- Projection rules for state computation
- Multi-step, multi-actor workflow composition (S11, S14)
- Offline workflow conflict handling

---

## 4. Issues Detected

### Issue 1: ADR-001 pre-decides ADR-4 (configuration boundary)

**Severity: Medium.**

The statement "Adding a new event type is platform evolution, not deployment configuration" in ADR-001's Consequences section is an ADR-4 decision. It might be correct — the exploration's analysis of the T2 tension suggests a bounded event type set is wise — but ADR-4's exploration hasn't happened yet. If ADR-4 exploration reveals that some event types should be deployment-configurable (e.g., custom capture types), this ADR-001 statement would need to be walked back.

**Fix**: Restate as an observation about what the event model enables, not a commitment. "The event model supports a bounded vocabulary of event types. Whether specific types are platform-fixed or deployment-configurable is ADR-4's decision."

### Issue 2: ADR-001 pre-decides ADR-5 (workflow model)

**Severity: Medium.**

"State machines emerge from projection rules" and "data and workflow actions are separate events" are ADR-5 decisions stated in ADR-001's Consequences. The event model ENABLES these approaches, but doesn't REQUIRE them. ADR-5 might decide that some workflows need explicit state machine primitives alongside projection-derived state.

**Fix**: Restate as consequences the event model makes possible. "The event model makes it possible for state to be projection-derived and for data and workflow to be separate event types. ADR-5 will determine the specific state progression model."

### Issue 3: Escape hatch is aspirational, not operational

**Severity: High.**

This is the tension point flagged by the user. ADR-001 describes the B→C escape hatch as a risk acceptance note: "If projection complexity proves too complex, the escape hatch is adding application-maintained views." The forward projection document explicitly notes this only works if the event log is gap-free.

**The gap**: ADR-001 says the event log is the single source of truth (A3). But it doesn't state the DISCIPLINE RULE that makes this true in practice: no writes bypass the event store. Ever. This is not an architectural property — it's a development constraint. If a developer writes to a projection/view without writing to the event store (a natural shortcut under time pressure), the escape hatch silently closes.

**Fix**: Add a hard rule to S2 or as a standalone section:

> **Write-path discipline**: Every state change in the system enters through the event store. Projections and views are derived — maintained eagerly or lazily, but never written to independently. If a projection is corrupt or stale, the fix is rebuild from events, never direct patch. This discipline is what makes the event log the single source of truth in practice, not just in theory. It is also what keeps the escape hatch (adding application-maintained views if projection complexity proves excessive) viable: the escape hatch works only if the event log is gap-free from day one.

### Issue 4: Projection location is prematurely committed

**Severity: Low-Medium.**

"Projections are maintained on-device and server" in S2 pre-commits to a sync/device strategy that belongs in ADR-3. ADR-1 can say projections exist for read performance. Where they live — device, server, both, differently for different user tiers — is ADR-3's decision.

**Fix**: Change to "Projections (materialized views of current state) are maintained for read performance." Remove "on-device and server."

### Issue 5: "What This Does NOT Decide" table is incomplete

**Severity: Low.**

The table correctly lists 5 deferred concerns but misses:
- Where projections live (device/server/both) — ADR-3
- Sync topology (one-tier, two-tier, summary sync) — ADR-3
- Activity context / correlation metadata — ADR-4
- Whether event types are platform-fixed or configurable — ADR-4

**Fix**: Expand the table or add a note that the Consequences section describes possibilities, not commitments, for ADRs 2–5.

### Issue 6: Principles not updated

**Severity: Low (maintenance).**

ADR-001 confirms P1, P3, P5, P7. The principles.md file still shows all as "Hypothesis." This is cosmetic but creates drift between documents.

**Fix**: Update principles.md status for P1, P3, P5, P7 after ADR-001 changes are finalized.

---

## 5. Required Changes to ADR-001

Ordered by priority.

### Change 1: Add write-path discipline rule (Issue 3)

**Where**: S2, after "Projections are always rebuildable from the event stream."

**Add**: The write-path discipline paragraph from Issue 3 above. This operationalizes the escape hatch and makes "single source of truth" actionable rather than aspirational.

### Change 2: Soften Consequences section (Issues 1, 2)

**Where**: The four bullet points under "What is now constrained."

**ADR-4 bullet — change from**:
> "Adding a new event type is platform evolution, not deployment configuration. This draws the first line of the configuration boundary."

**To**:
> "Configuration defines data shapes per event type and selects which event types an activity uses. The boundary between platform-defined event types and deployment-configurable types is ADR-4's decision."

**ADR-5 bullet — change from**:
> "State transitions are events, not state mutations. State machines emerge from projection rules applied to event sequences. Data and workflow actions are separate events that compose on the same subject."

**To**:
> "The event model makes state-as-projection possible: state transitions can be events, and state machines can emerge from projection rules. The specific state progression model — including whether data and workflow events are separate types — is ADR-5's decision."

### Change 3: Remove projection location (Issue 4)

**Where**: S2, first sentence about projections.

**Change from**:
> "Projections (materialized views of current state) are maintained on-device and server for read performance."

**To**:
> "Projections (materialized views of current state) are maintained for read performance."

**Where**: ADR-3 Consequence bullet.

**Change from**:
> "Two-tier sync is possible — full event streams for subjects a user works on, pre-computed projections for subjects they only oversee."

**To**:
> "The event model enables differentiated sync strategies (e.g., full event streams vs. pre-computed projections). ADR-3 decides the sync topology."

### Change 4: Expand "What This Does NOT Decide" (Issue 5)

**Add rows**:

| Concern | Belongs to | Why deferred |
|---------|-----------|--------------|
| Where projections live (device, server, or both) | ADR-3 | Sync and device strategy |
| Sync topology (one-tier, two-tier, summary sync) | ADR-3 | Requires authorization model |
| Whether event types are platform-fixed or deployment-configurable | ADR-4 | Configuration boundary question |
| Activity context / correlation metadata in events | ADR-4 | Requires the activity model |

### Change 5: State projection rebuild scope (M2)

**Where**: S2, near the rebuild guarantee.

**Add after "the events win — the projection is discarded and recomputed"**:
> "What subset of events a device holds — and therefore what it can rebuild locally — is determined by ADR-3 (sync scope)."

---

## 6. Next ADRs to Create

### ADR-002: Identity Model and Conflict Resolution

**Purpose**: Define how subjects are identified across devices, how duplicate real-world subjects are detected and merged, how concurrent events are ordered, and what constitutes a conflict.

**Why next**: Directly downstream of ADR-001. Required by ADR-3 (authorization needs identity to define scope) and ADR-4 (configuration references subjects).

**Sub-decisions to explore**:

1. **Subject identity representation**: UUID-only? UUID + human-readable local IDs? Composite keys? How do external identifiers (national ID, facility code) relate to the platform's internal identity?
2. **Subject lifecycle events**: What events represent creation, merge, split, deactivation? How does a merge event affect the projection of both subjects' histories?
3. **Causal ordering mechanism**: HLC, vector clocks, Lamport timestamps, or simpler? What level of causal precision is needed vs. affordable on low-end devices?
4. **Conflict definition**: What event patterns constitute a conflict? Same subject + overlapping time window + overlapping fields? Or coarser? Does "conflict" mean "requires human resolution" or "requires visibility"?
5. **Resolution strategy**: Automatic resolution (where provably safe — additive, non-overlapping fields), human triage (otherwise), configurable policies? How is the resolution itself recorded?
6. **Event reference semantics**: How do correction, review, and supersession references work structurally? Is there a fixed reference vocabulary (corrects, reviews, supersedes) or is it open?

**Key scenarios**: S01 (duplicate identities, ambiguous identity), S06 (deactivated subjects, schema evolution), S08 (long-running cases with many interactions), S19 (offline conflicts, ordering), S06b (bulk changes intersecting offline work).

**Key principles**: P5 (conflict surfaced), P3 (append-only — merges don't rewrite).

### ADR-003: Authorization and Selective Sync

**Purpose**: Define the authorization model, sync scope, projection location strategy, and stale-access handling.

**Why**: Requires ADR-2 (identity defines "scope"). Determines what data each device carries and what each user can see/do.

**Absorbs from ADR-001**: X4 (two-tier sync), X5 (projection location), X6 (evaluability constraint), M2 (rebuild scope).

### ADR-004: Configuration Paradigm and Boundary

**Purpose**: Define what is configurable vs. platform-developed, how configurations are authored and propagated, and where the T2 boundary sits.

**Why**: Requires ADR-1 (storage primitive), ADR-2 (identity model), ADR-3 (authorization model). Most consequential for the platform's "set up, not built" promise.

**Absorbs from ADR-001**: X1 (event type vocabulary ownership).

### ADR-005: State Progression and Workflow

**Purpose**: Define how work moves through stages, whether state machines are projections or explicit primitives, and how workflow interacts with offline.

**Why**: Requires ADR-1 through ADR-4.

**Absorbs from ADR-001**: X2 (state machines from projections), X3 (data vs. workflow event separation).

---

## 7. What Must NOT Be Decided Yet

These are decisions where uncertainty remains too high, the required exploration hasn't happened, or the dependency chain hasn't been resolved.

| Decision | Why not yet | When |
|----------|------------|------|
| Specific event type vocabulary (capture, correct, review, transfer, etc.) | The boundary between platform types and configurable types is ADR-4 | After ADR-4 |
| Projection rules (how events compose into current state) | Depends on ADR-4 (configuration) and ADR-5 (workflow) | After ADR-4/5 |
| Authorization model details (RBAC, ABAC, hybrid) | ADR-3 exploration needed | ADR-3 |
| Configuration language syntax or format | ADR-4 exploration needed | ADR-4 |
| Sync protocol specifics | ADR-3 exploration needed | ADR-3 |
| Correlation / activity context in events | Requires the activity model from ADR-4 | ADR-4 |
| Whether state machines are projections, explicit primitives, or both | ADR-5 exploration needed | ADR-5 |
| Specific causal ordering implementation (HLC vs. vector clocks) | ADR-2 exploration needed | ADR-2 |
| Event envelope field schema | Requires ADR-2 (identity), ADR-3 (auth), ADR-4 (config) to shape it | After ADR-3 or ADR-4 |
| Projection caching / invalidation strategy on devices | Implementation concern; requires ADR-3 sync scope first | Post-ADR-3 |

---

## 8. Final Safety Check

**After applying the changes in Section 5, can we proceed to ADR-2 without missing anything?**

**Yes, with one caveat.**

The changes remove ADR-001's over-reach into ADR-4/5 territory, add the escape hatch discipline rule, and correctly scope what ADR-001 decides. After these changes, ADR-001 decides exactly one thing: the storage primitive and its guarantees. Everything else is opened as a possibility, not committed.

**The caveat**: ADR-001's status should remain **Draft** (as the user already set) until the changes in Section 5 are applied. Once applied, it can move to **Accepted** — it will be clean of downstream pre-commitments.

**Forward safety**: The ADR-002 through ADR-005 scope boundaries in Section 6 are clean — no overlaps, no gaps. Each ADR has a clear scope, clear inputs from prior ADRs, and clear sub-decisions. The absorptions (X1→ADR-4, X2/X3→ADR-5, X4/X5/X6→ADR-3) move every identified pre-commitment to its proper home.

**Remaining risk**: The principles.md file still shows all principles as "Hypothesis." After ADR-001 is finalized, P1, P3, P5, P7 should be updated to "Confirmed by ADR-001." This is maintenance, not a blocker for ADR-2.
