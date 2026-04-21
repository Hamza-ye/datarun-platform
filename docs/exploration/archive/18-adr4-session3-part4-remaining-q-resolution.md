> **⚠️ SUPERSEDED** — This is a raw exploration document archived for reference. Decisions were finalized in **ADR-004**. For a navigated reading guide, see [guide-adr-004.md](../guide-adr-004.md).
# ADR-004 Session 3, Part 4: Remaining Question Resolution

> Phase: 3.4 of 4 — Remaining Q Resolution + Session 3 Synthesis
> Date: 2026-04-12
> Input: Parts 1–3, Session 2 walk-throughs (14-adr4-session2-scenario-walkthrough.md), ADR-002 (conflict model), ADR-003 (authorization/sync), relevant scenarios (S08, S15, S19)
> Purpose: Resolve Q7, Q9, Q10, Q12 — the four sub-questions not exercised by Session 2 walk-throughs. Integrate carry-forward insights from Part 3. Produce a full Session 3 synthesis with final position inventory.

---

## Scope

Session 2 §6.5 identified four questions that no selected scenario exercised:

| Question | Topic | Why deferred from Session 2 |
|----------|-------|---------------------------|
| Q7 | Domain-specific conflict rule configuration | No scenario involved custom conflict resolution |
| Q9 | Per-flag-type severity configuration | No scenario needed fine-grained flag configuration |
| Q10 | Scope type extensibility | All scenarios used geographic scoping |
| Q12 | Sensitive-subject classification | No scenario involved sensitive data categorization |

All four were classified as non-envelope by Session 1 §1.2 and confirmed as Tier 3 (initial strategies) by Part 2's irreversibility filter. None touch stored events. None require full adversarial stress testing.

**Method:** For each question, walk through the relevant scenario(s), identify where the decision sits in the configuration gradient, form a position, and classify.

Additionally, Part 3 carried forward four items that need integration:

| Item | Source | Nature |
|------|--------|--------|
| B10 refinement: default deprecation-only schema evolution | Attack 2 | Strategy refinement |
| `context.*` scope as expression extension | V2 light validation | Noted, defer decision |
| `status_changed` as plausible 7th type | Attack 3 | Noted, defer to ADR-5 |
| Deploy-time validation warnings for same-shape multi-activity | Attack 1 | Tooling concern |

---

## Q7: Domain-Specific Conflict Rule Configuration

### What ADR-2 already decided

ADR-2 established the **conflict detection mechanism** — not the rules:

| Commitment | Source | Classification |
|-----------|--------|---------------|
| Accept-and-flag: events never rejected, anomalies surfaced | ADR-2 S14 | Structural constraint |
| Single-writer resolution: one designated resolver per conflict | ADR-2 S11 | Strategy-protecting constraint |
| Detect-before-act: flagged events don't trigger policies until resolved | ADR-2 S12 | Strategy-protecting constraint |
| Four structural conflict types: concurrent state change, stale reference, duplicate identity, cross-lifecycle | ADR-2 Phase 2 | Structural constraint |

ADR-2 explicitly deferred to ADR-4: *"Which specific conditions constitute domain-level conflicts (beyond structural ones) is a deployment-configured business rule, not a platform constant."*

### The question restated

Can deployers define custom rules for **what event patterns constitute a domain conflict** and **how domain conflicts should be resolved**?

Examples:
- "Two weight measurements for the same child on the same day" — domain conflict or valid?
- "A supply distribution exceeding available stock" — constraint violation or allowed?
- "A follow-up visit recorded before the intake visit syncs" — temporal anomaly or normal offline work?

### Walking through S08 (case management) and S19 (offline capture)

**S08 — Following something over time until resolved:**

A case has multiple interaction events from multiple workers. Domain conflicts include:
- Two workers independently recording a resolution for the same case (both offline). Structurally: concurrent state change on the same subject by different actors. ADR-2's structural conflict detector catches this — both events are accepted, a `ConflictDetected` event is created, a single resolver is designated.
- A worker recording a follow-up visit referencing assessment data that was captured by someone else who hasn't synced yet. The worker's event arrives first. Structurally: the follow-up references stale state. ADR-2's stale-reference detection catches this when the assessment arrives later.

**What ADR-2 doesn't catch that S08 needs:**
- "This case has been inactive for 90 days — should it auto-close or alert the supervisor?" This isn't a conflict — it's a deadline-based policy. Already handled by Layer 3b triggers.
- "A worker marked the case resolved but the last recorded vital sign was abnormal." This is a **clinical protocol rule**, not a conflict detection rule. It's a validation concern — should the platform prevent or warn against resolution when indicators are abnormal?

**S19 — Working without connectivity:**

Two workers offline, both recording about the same household. Worker A records a visit on Monday. Worker B, unaware, records a visit on Tuesday. Both sync on Wednesday. Is this a conflict?

Structurally: two `capture` events with the same `subject_ref` by different `actor_ref` values in a short time window. ADR-2's conflict detector does NOT flag this automatically — these are two valid captures by authorized actors. No structural invariant is violated (no concurrent state mutation — both are independent observations).

But the DEPLOYMENT may consider this a domain conflict: "only one visit per household per week per activity." This is a **business rule** about acceptable patterns, not a structural invariant.

### Three categories of conflict-related rules

| Category | Example | Who defines | Where evaluated | Already handled? |
|----------|---------|------------|-----------------|-----------------|
| **Structural conflicts** | Concurrent state change, duplicate identity, stale reference | Platform (ADR-2) | Server, at sync | Yes — ADR-2 |
| **Domain uniqueness rules** | "One visit per household per week" | Deployer | Server, at sync + device, at capture | No — this is Q7 |
| **Clinical/business validation** | "Don't resolve case with abnormal vitals" | Deployer | Device, at form submission (L2 logic rules) | Partially — L2 handles validation warnings, but can't enforce cross-event constraints |

**The key insight:** Q7 is really two sub-questions:

**Q7a: Can deployers define domain uniqueness constraints?**
Example: "One `household_visit` capture per subject per activity per calendar week."

This is a constraint that the platform evaluates ACROSS events — it requires projecting the event history for a subject and checking whether a new event violates a deployer-defined uniqueness rule. It's NOT a form-level validation (L2) because it requires access to historical event data, not just the current form's fields.

**Q7b: Can deployers define domain-specific conflict resolution strategies?**
Example: "When two conflicting case resolutions exist, prefer the one from the senior role."

This extends ADR-2's single-writer resolution with deployer-defined precedence rules. Instead of "designate one resolver," the deployer configures "auto-resolve based on role seniority."

### Position on Q7a: Domain uniqueness constraints

**Where in the gradient:** This is a **server-side validation rule** — it requires cross-event data that the device may not have completely (other workers' events haven't synced). On-device, the constraint can be checked against locally known events (optimistic check, may miss events from other offline workers). On the server, it's checked authoritatively at sync.

**Configuration mechanism:** A uniqueness rule is a simple declaration:

```
uniqueness:
  shape: household_visit
  scope: [subject_ref, activity_ref]
  period: calendar_week
  action: warn  # or: block
```

This says: "For a given subject and activity, at most one `household_visit` per calendar week. If violated, issue a warning (or block the event from triggering policies)."

**Where does this sit?** It's a **Layer 1 concern** — it's part of the shape definition. A shape declares its own uniqueness semantics. The validation engine (partially settled primitive) evaluates it. No expressions needed. No trigger logic. Pure declarative constraint.

**Classification:** Initial strategy (Tier 3). The configuration format is evolvable. The concept ("shapes can declare uniqueness constraints") is the position; the exact YAML format is implementation detail.

**Device vs. server behavior:**
- Device: checks uniqueness against locally synced events. If offline for weeks, may miss violations. Issues optimistic warning.
- Server: authoritative check at sync. If violation detected, emits a `ConflictDetected` event using ADR-2's existing mechanism. The domain uniqueness violation is just another flag type.

**This reuses ADR-2's infrastructure.** Domain uniqueness violations produce the same `ConflictDetected` event as structural conflicts. The flag carries a type that distinguishes it (e.g., `domain_uniqueness_violation` vs. `concurrent_state_change`). The resolution workflow is identical.

### Position on Q7b: Domain-specific conflict resolution strategies

**Short answer: defer to ADR-5.**

Auto-resolution strategies require:
1. A precedence model (role seniority, timestamp recency, etc.)
2. A resolution action (prefer one, merge payloads, escalate to human)
3. Integration with state machines (ADR-5's concern)

This is a workflow question, not a configuration boundary question. ADR-2 provides the mechanism (single-writer resolution, explicit resolution events). ADR-5 will determine whether resolution can be automated and how.

**What ADR-4 commits to:** The conflict detection infrastructure accepts new flag types from deployer-defined rules. The resolution strategy remains what ADR-2 established (human, single-writer) until ADR-5 explores automation.

### Q7 Summary

| Sub-question | Position | Classification | Layer |
|-------------|----------|---------------|-------|
| Q7a: Domain uniqueness constraints | Shapes declare uniqueness rules (scope, period, action). Evaluated on device (optimistic) and server (authoritative). Violations produce ADR-2 conflict flags. | Initial strategy (Tier 3) | L1 (shape definition) |
| Q7b: Domain conflict resolution strategies | Defer to ADR-5 (workflow/state progression). ADR-4 provides the flag types; ADR-5 provides the resolution automation. | Deferred | — |

---

## Q9: Per-Flag-Type Severity Configuration

### What ADR-3 already decided

ADR-3 S7 established: *"Which flag types are blocking vs. informational is deployment-configurable."* It provided initial defaults:

| Flag type | Default severity | Rationale |
|-----------|-----------------|-----------|
| `ScopeStaleFlag` | Informational | Scope staleness is common in offline scenarios; blocking would halt too much work |
| `RoleStaleFlag` | Blocking (for capability-restricted actions) | A trainee acting as a qualified worker is a safety concern |
| `TemporalAuthorityExpiredFlag` | Informational | Expired temporal authority is an assignment concern, not a safety concern |

ADR-2's structural conflict types also have implicit severity:
| Flag type | Default severity | Rationale |
|-----------|-----------------|-----------|
| Concurrent state change | Blocking | Two contradictory state mutations require human resolution |
| Stale reference | Informational | Common in offline scenarios; usually self-resolves on sync |
| Duplicate identity | Blocking | Requires merge/split decision (ADR-2) |
| Cross-lifecycle | Informational | Event references a completed/archived entity; data is valid, context is stale |

### The question restated

How does a deployer override these defaults? Can a deployment say "ScopeStaleFlag should be blocking in our context" or "stale reference should auto-resolve"?

### Walking through S19 (offline capture)

S19 describes extended disconnection — days or weeks offline. In this context:

- A worker captures 50 household visits over 2 weeks offline. During that time, 3 households have been reassigned to a different worker. When the original worker syncs, 3 events have `ScopeStaleFlag` — the worker's scope no longer includes those households.

**If `ScopeStaleFlag` is blocking:** 3 events are frozen. Their data doesn't flow into projections, reports, or trigger policies until a supervisor resolves each flag. The supervisor reviews: "Worker A captured valid data about Household X before the reassignment. Accept the data." Three resolution events. This is correct but labor-intensive for a common offline scenario.

**If `ScopeStaleFlag` is informational:** 3 events flow into projections and reports immediately. A flag is visible in the audit trail. The supervisor can review at their convenience but the data is not blocked.

**Different deployments have different risk tolerances.** A nutrition survey (low stakes) tolerates informational scope staleness. A pharmaceutical distribution (high stakes, regulatory) may require blocking scope staleness because stale-scope events could indicate unauthorized access to controlled substances.

### Where in the gradient

Flag severity configuration is a **Layer 0 concern** — part of the deployment's activity configuration. It's a simple key-value mapping:

```
flag_severity:
  scope_stale: informational       # default: informational
  role_stale: blocking             # default: blocking
  temporal_expired: informational  # default: informational
  concurrent_state: blocking       # default: blocking
  stale_reference: informational   # default: informational
  duplicate_identity: blocking     # default: blocking
  cross_lifecycle: informational   # default: informational
  domain_uniqueness: warn          # from Q7a, default: warn
```

**No expressions needed. No logic. Pure parameterization.** This is the simplest form of configuration — the platform defines the vocabulary of flag types and their possible severities (blocking, informational). The deployer selects severity per flag type. The platform provides sensible defaults.

**Scope of configuration:** Per-deployment (not per-activity). Flag severity is a deployment-wide policy because the detect-before-act mechanism (ADR-2 S12) operates at the event level, not the activity level. A flagged event is flagged regardless of which activity produced it.

**Could it be per-activity?** Theoretically, yes: "blocking for pharmaceutical distribution, informational for nutrition survey." But this creates a problem: a single event belongs to one activity (via `activity_ref`). If two activities share a shape and a subject appears in both, the flag severity depends on which activity the event was captured under. This is workable but adds complexity without a demonstrated need. Per-deployment is simpler and sufficient for the scenarios examined.

**Growth path:** If per-activity severity is later needed, the configuration format extends from a flat map to a map-of-maps. No structural change. No data migration.

### Q9 Summary

| Position | Classification | Layer |
|----------|---------------|-------|
| Per-deployment flag severity configuration. Platform defines flag types and possible severities (blocking/informational). Deployer overrides defaults. | Initial strategy (Tier 3) | L0 (deployment parameters) |
| Default severities defined by ADR-2/ADR-3 remain as platform defaults | Platform default | — |
| Per-activity severity is a growth path, not initial commitment | Deferred | — |

---

## Q10: Scope Type Extensibility

### What ADR-3 already decided

ADR-3 S1 established: *"Assignment-based access control. The access check is a single scope-containment test: `actor.assignment.scope ⊇ subject.location`."*

ADR-3's deferred items include:
- *"Subject-based scope (case management): New assignment configuration, not structural change."*
- Assessment visibility ("about me" dimension): sync filter configuration.

All Session 2 walk-throughs used **geographic scope** — the actor is assigned to a geographic area, subjects are located in that area, scope-containment is a spatial hierarchy check.

### The question restated

Are scope types (geographic, subject-based, program-based) **platform-fixed** or **deployment-configurable**? Can a deployer invent a new scope type?

### Walking through S08 (case management) and S15 (cross-program overlays)

**S08 — Case management:**

A case worker is assigned specific cases, not a geographic area. Their scope is: "the 15 cases assigned to me." A new case is assigned → scope expands. A case is transferred → scope contracts. The scope-containment test isn't `actor.scope ⊇ subject.location` — it's `subject.id ∈ actor.assigned_subjects`.

This is a **different scope type** from geographic. The containment test has the same shape (is the subject within the actor's authorized set?) but the SET is defined differently (explicit list vs. spatial hierarchy).

**S15 — Cross-program overlays:**

Multiple stakeholders view the same data through different lenses. A health ministry sees facility-scoped data. A finance ministry sees commodity-scoped data. A donor sees geography-scoped data. The scope type varies BY STAKEHOLDER within the same deployment.

This means a single deployment may use multiple scope types simultaneously.

### Analysis: platform-fixed vs. deployment-configurable

**Option A: Platform-fixed scope types.**

The platform ships with a fixed set of scope types:

| Scope type | Containment test | Assignment format |
|-----------|-----------------|-------------------|
| `geographic` | `subject.location` within `actor.scope.area` (hierarchy: country → region → district → facility → community) | Actor assigned to area node(s) |
| `subject_list` | `subject.id` in `actor.scope.subjects` | Actor assigned specific subject IDs |
| `program` | `event.activity_ref` in `actor.scope.activities` | Actor assigned to specific activities |

Adding a new scope type requires a platform update (code change). But critically, adding a new scope type does NOT require data migration — the scope type is part of the assignment configuration (server-side), not the event envelope. Events don't carry scope type information; scope evaluation happens during sync scope computation and authorization checks.

**Option B: Deployment-configurable scope types.**

The deployer defines custom scope types with custom containment logic. This requires:
1. A scope definition language (how to express "entity X is within scope Y")
2. A containment evaluation engine that runs deployer-defined rules
3. A sync scope computation engine that evaluates custom rules

This is a rules engine for access control — a direct path to AP-1/AP-2. Access control rules-engine complexity is one of the most dangerous forms of configuration creep because bugs in access control are security vulnerabilities, not just data quality issues.

**Option C: Platform-fixed scope types, composable.**

The platform ships with a fixed set of scope types (as in Option A), but an assignment can combine multiple scope types:

```
assignment:
  actor: chv_001
  scope:
    geographic: district_north      # sees subjects in this area
    subject_list: [case_042, case_087]  # plus explicitly assigned cases
    activities: [routine_immunization]  # limited to this activity
```

The containment test becomes: subject satisfies ALL scope dimensions that are non-null. This is composable (multiple dimensions) without being configurable (no custom logic).

### Position on Q10

**Platform-fixed scope types, composable (Option C).**

Rationale:
1. **Security-critical logic should not be deployer-written.** Scope evaluation determines what data each device receives. A bug in custom scope logic is a data leak. Platform-fixed scope types are tested, audited, and hardened.
2. **The known scope types cover the scenarios.** Geographic (S00–S07, S09, S12, S20), subject-list (S08), and program/activity (S15) handle all 21 scenarios. No scenario requires a scope type that can't be expressed as a combination of these three.
3. **New scope types are a platform evolution, not deployment configuration.** If a future deployment needs "commodity-scoped access" (a scope type not in the initial set), the platform adds it. This is a code change, not a data migration — scope types are evaluated in server-side sync computation, not stored in events.
4. **Composability handles multi-dimensional access.** S15's "different stakeholders, different lenses" maps to different assignments with different scope dimension combinations, not custom scope types.

**Initial scope type vocabulary:**

| Scope type | Containment test | When used |
|-----------|-----------------|-----------|
| `geographic` | Subject's location within actor's geographic assignment (hierarchy containment) | Default for field workers, supervisors |
| `subject_list` | Subject's ID in actor's explicit assignment list | Case management, specific-subject access |
| `activity` | Event's `activity_ref` in actor's permitted activities | Program-specific access restriction |

**Composition rules:**
- An assignment MAY have multiple scope dimensions.
- All non-null dimensions must pass for access to be granted (AND composition).
- A null dimension means "unrestricted on this dimension."
- Example: `{geographic: district_north, activity: null}` = all activities in district_north. `{geographic: null, subject_list: [case_042]}` = case_042 regardless of geography.

**Growth path:** New scope types (e.g., `commodity`, `facility_type`) are added as platform evolution. The composition mechanism stays the same. No event migration needed.

### Q10 Summary

| Position | Classification | Layer |
|----------|---------------|-------|
| Platform-fixed scope types: geographic, subject_list, activity | Initial strategy (Tier 3) | Platform implementation |
| Composable: assignments combine multiple scope dimensions with AND logic | Initial strategy (Tier 3) | Platform implementation |
| Deployers cannot define custom scope types or custom containment logic | Strategy-protecting constraint | Protects security invariant |
| New scope types are platform evolution (code change, no migration) | Growth path | — |

---

## Q12: Sensitive-Subject Classification

### What ADR-3 already decided

ADR-3 S10 (selective-retain on scope contraction) distinguished:
- *"Non-sensitive data: retain-all acceptable."*
- *"Sensitive personal data: selective purge recommended (crash-safe, journaled)."*
- *"Retain-but-hide: not recommended for sensitive data (physically accessible on rooted devices)."*

ADR-3 deferred: *"Sensitive-subject classification: ADR-4. Deployment-specific. Sync scope (S2) can incorporate sensitivity as a filter."*

### The question restated

Can deployers mark subjects or data categories as sensitive, triggering stricter sync/retention behaviors? What does "sensitive" mean operationally?

### Analysis

**What "sensitive" means in practice:**

In field operations, sensitivity typically comes from:
1. **Subject identity sensitivity** — HIV patients, GBV survivors, mental health cases. The subject's IDENTITY is sensitive, not just their data.
2. **Data category sensitivity** — Financial data, personnel evaluations, investigation findings. The DATA is sensitive regardless of subject.
3. **Regulatory classification** — Personal health information (PHI), personally identifiable information (PII). External regulation dictates handling.

**What stricter behavior looks like:**

| Behavior | When sensitivity is set | Without sensitivity |
|----------|----------------------|-------------------|
| Sync scope | Additional filter: only actors with explicit sensitive-access role | Standard scope-containment |
| Device retention | Selective purge on scope contraction | Retain-all acceptable |
| Audit logging | Enhanced logging (who accessed when) | Standard event audit trail |
| Display | Masked/redacted in list views, full in detail view | Standard display |

### Where in the gradient

Sensitivity classification is a **Layer 0/L1 concern** — it's a property of the shape or the activity, declared at configuration time.

**Option A: Shape-level sensitivity.**

A shape is marked as sensitive. All events with that `shape_ref` inherit the sensitivity classification.

```
shape: hiv_screening
version: 1
sensitivity: high
fields:
  - name: result
    type: select
    options: [positive, negative, inconclusive]
```

**Pro:** Simple, consistent. Every `hiv_screening` event is sensitive, period.
**Con:** Coarse-grained. What if only `result: positive` is sensitive?

**Option B: Activity-level sensitivity.**

An activity is marked as sensitive. All events captured within that activity inherit the classification.

```
activity: gbv_response
sensitivity: high
shapes: [gbv_intake, gbv_follow_up, gbv_referral]
```

**Pro:** Groups related shapes under one sensitivity umbrella. Matches how programs are structured (all GBV work is sensitive, not just specific fields).
**Con:** Same coarseness concern.

**Option C: Field-level sensitivity.**

Individual fields within a shape are marked as sensitive.

```
shape: patient_encounter
version: 1
fields:
  - name: chief_complaint
    type: text
    sensitivity: standard
  - name: hiv_test_result
    type: select
    sensitivity: high
```

**Pro:** Fine-grained. Only the sensitive field triggers stricter handling.
**Con:** Significantly more complex. The sync layer must understand field-level sensitivity (partial event redaction?). The retention layer must selectively purge fields within events. This breaks the event immutability model — you can't partially purge an immutable event.

**Assessment of Option C:** Field-level sensitivity is incompatible with the append-only event model (ADR-1). Events are immutable atoms. You can't redact a field from a stored event without violating immutability. The only options are: purge the ENTIRE event (data loss) or encrypt the sensitive field at capture (key-based access control). Encryption adds envelope-level complexity — and we've already confirmed no new envelope fields.

### Position on Q12

**Shape-level or activity-level sensitivity classification (deployer's choice), NOT field-level.**

| Decision | Position | Classification |
|----------|----------|---------------|
| Sensitivity is a property of shapes and/or activities | Initial strategy (Tier 3) | L0/L1 configuration |
| Platform provides sensitivity levels: `standard` (default), `elevated`, `restricted` | Initial strategy (Tier 3) | L0 vocabulary |
| Sensitivity affects: sync scope filtering (additional role requirement), device retention policy (purge vs. retain), audit level | Initial strategy (Tier 3) | Platform behavior per level |
| Field-level sensitivity is NOT supported | Strategy-protecting constraint | Protects event immutability (ADR-1) |
| Sensitivity is NOT stored in the event envelope | Structural constraint (confirms no change) | Same envelope |

**How it works operationally:**

1. **At configuration time:** Deployer marks shapes or activities with a sensitivity level.
2. **At sync time:** Server evaluates sync scope with an additional dimension: does the actor have a role that grants access to this sensitivity level? This composes with Q10's scope types — sensitivity is another AND dimension, not a separate scope type.
3. **At retention time:** When an actor's scope contracts, events referencing sensitive shapes/activities are candidates for purge (not just removal from projection). Non-sensitive events use retain-all.
4. **At display time:** Events with elevated/restricted sensitivity shapes can have display-level masking in list views. This is a UI concern, not a data concern.

**What this does NOT solve:** Regulatory compliance (GDPR right to erasure, HIPAA de-identification). These are system-level concerns that require purpose-built data lifecycle management. Sensitivity classification provides the metadata to IDENTIFY what's sensitive; the compliance mechanism itself is platform evolution, not configuration.

**Growth path:** If regulatory requirements later demand field-level handling, the platform can add event-level encryption where the payload is encrypted with a key bound to sensitivity level. This is a major platform feature, not a configuration concern. The sensitivity classification provides the foundation (which events need encryption) without committing to the mechanism.

### Q12 Summary

| Position | Classification | Layer |
|----------|---------------|-------|
| Shape-level and activity-level sensitivity classification | Initial strategy (Tier 3) | L0/L1 |
| Three levels: standard, elevated, restricted | Initial strategy (Tier 3) | L0 vocabulary |
| Sensitivity affects sync scope, retention policy, audit level | Initial strategy (Tier 3) | Platform behavior |
| No field-level sensitivity (protects event immutability) | Strategy-protecting constraint | — |
| No new envelope fields from sensitivity | Structural (confirms no change) | — |

---

## Part 3 Carry-Forward Integration

### CF-1: B10 Refinement — Default Deprecation-Only Schema Evolution

Part 3 Attack 2 (breaking schema changes) concluded:

> "The default schema evolution model is additive-only with deprecation. Breaking changes require deployer acknowledgment and server-side migration."

**Integration into Part 4:** This is a B10 strategy refinement. It doesn't create a new position — it sharpens the existing one:

| B10 (original) | B10 (refined) |
|----------------|---------------|
| Change classification: additive / deprecation / breaking | Change classification: additive / deprecation / breaking. **Default: deprecation-only.** Breaking changes are an acknowledged exceptional operation requiring server-side validation and deployer confirmation. |

**Classification stays:** Initial strategy (Tier 3). The mechanism for handling breaking changes is implementation detail. The principle ("deprecation is the default evolution path") is the position.

### CF-2: `context.*` Scope Extension

Part 3 V2 (expression scope) surfaced `context.*` as a plausible third data scope for form-context expressions — pre-resolved actor-level contextual facts (e.g., assigned facility attributes).

**Decision: Defer to ADR-5.** Rationale:
1. `context.*` emerged from a light validation, not a structural attack.
2. Its utility depends on ADR-5's state-progression model (case context, workflow context).
3. The expression evaluator's architecture doesn't change — `context.*` is a pre-resolved scope, not a dynamic query.
4. Noting it here ensures it's not lost. It will be exercised in ADR-5's scenario walk-throughs.

### CF-3: `status_changed` as Plausible 7th Type

Part 3 Attack 3 noted that `status_changed` is a legitimate candidate for a 7th structural event type if ADR-5 introduces state machines as a platform primitive.

**Decision: Defer to ADR-5.** The current 6-type vocabulary handles all ADR-4 scenarios. The append-only type vocabulary commitment (A3) makes future addition cheap. ADR-5 will determine whether state transitions require different platform processing behavior.

### CF-4: Deploy-Time Validation Warnings

Part 3 Attack 1 identified that deployer configurations with multiple activities using the same shape should trigger a warning: *"Events will be distinguished by activity_ref. Ensure activity context is available at capture time."*

**Integration:** This is a tooling concern, not an architecture position. It belongs in the ADR-4 writing as a deployer-facing guard. No exploration needed — the rule is straightforward.

Combined with Q7a (domain uniqueness constraints), a pattern emerges: **deploy-time validation is a distinct platform capability** that checks configuration artifacts against known pitfalls before deployment. This isn't a new position — it's an observation that the complexity budgets (D3), uniqueness constraints (Q7a), and same-shape-multi-activity warnings are all instances of deploy-time validation.

---

## Session 3 Complete Synthesis

### All positions after Session 3 (Parts 1–4)

**Tier 1 — Structural constraints (envelope-touching, irreversible):**

| # | Position | Status after Session 3 |
|---|----------|----------------------|
| A1 | `shape_ref`: mandatory, `{shape_name}/v{version}`, naming `[a-z][a-z0-9_]*` | **Confirmed** — survived Attack 2 (breaking changes). Format handles all evolution scenarios. |
| A2 | `activity_ref`: optional, deployer-chosen `[a-z][a-z0-9_]*`, references instance | **Confirmed** — survived Attack 1 (multi-activity collision). Auto-populated by device. |
| A3 | `type`: platform-fixed, append-only, initial 6 types | **Confirmed** — survived Attack 3 (vocabulary exhaustion). Domain meaning lives in shapes. |

**Tier 2 — Strategy-protecting constraints:**

| # | Position | Status after Session 3 |
|---|----------|----------------------|
| A4 | System actor: `system:{source_type}/{source_id}` | **Confirmed** — convention, not schema |
| B3 | Triggers server-only | **Confirmed** — Part 1 revision (3a moved from device to server) |
| B11 | Config as atomic package | **Confirmed** — no challenge |
| A1-naming | Shape names: `[a-z][a-z0-9_]*` | **Confirmed** — protects parse safety |
| Q10-security | No deployer-defined scope types or containment logic | **New** — protects access control integrity |
| Q12-immutability | No field-level sensitivity (protects event immutability) | **New** — protects ADR-1 append-only model |

**Tier 3 — Initial strategies (evolvable without migration):**

| # | Position | Status after Session 3 |
|---|----------|----------------------|
| B1 | Four-layer gradient (L0–L3) | **Confirmed** — all Qs map to layers |
| B2 | L3 split: 3a event-reaction, 3b deadline-check | **Confirmed** |
| B4 | One expression language, two contexts | **Confirmed** — `context.*` deferred to ADR-5 |
| B5 | Zero functions in expressions | **Confirmed** — V2 light validation held |
| B6 | Payload mapping: static + field refs only | **Confirmed** |
| B7 | Projection rules: lookup tables | **Confirmed** |
| B8 | DAG max path 2 | **Confirmed** — V3 justified the limit |
| B9 | Shapes as deltas/snapshots | **Confirmed** |
| B10 | Change classification + default deprecation-only | **Refined** — Attack 2 sharpened default |
| B12 | Device holds max 2 config versions | **Confirmed** |
| C1 | Patterns: platform-fixed, parameterized | **Confirmed** |
| C2 | Three platform capabilities | **Confirmed** |
| C3 | Max 2 escalation levels | **Confirmed** |
| C4 | Campaign as composition | **Confirmed** |
| D1 | Boundary at L3 → code | **Confirmed** |
| D2 | Side effect = persistent record | **Confirmed** |
| D3 | Complexity budgets (60 fields, etc.) | **Confirmed** — V1 validated |
| D4 | Capability elevation principle | **Confirmed** |
| Q7a | Domain uniqueness: shape-declared, server-authoritative | **New** — extends ADR-2 conflict model |
| Q9 | Flag severity: per-deployment, deployer-overridable defaults | **New** — implements ADR-3 S7 deferral |
| Q10-types | Scope types: geographic, subject_list, activity (composable AND) | **New** — implements ADR-3 scope deferral |
| Q12-levels | Sensitivity: shape/activity-level, 3 levels, affects sync/retention/audit | **New** — implements ADR-3 sensitivity deferral |

**Deferred to ADR-5:**

| Item | Reason |
|------|--------|
| Q7b: Domain conflict resolution strategies | Workflow/state machine concern |
| `context.*` expression scope | Depends on state-progression model |
| `status_changed` type | Depends on whether state machines are a platform primitive |

### Envelope after Session 3 — UNCHANGED

```
id              # ADR-1: UUID, client-generated
type            # ADR-1 + ADR-4: 6 structural types (append-only vocabulary)
shape_ref       # ADR-4: "{shape_name}/v{version}", mandatory
activity_ref    # ADR-4: optional, auto-populated by device
subject_ref     # ADR-2: typed identity reference
actor_ref       # ADR-2: who/what created this event
device_id       # ADR-2: hardware-bound device identifier
device_seq      # ADR-2: device-local sequence number
sync_watermark  # ADR-2: server-assigned ordering
timestamp       # ADR-1: device_time (advisory)
payload         # ADR-1: shape-conforming data
```

11 fields. No additions from Q7, Q9, Q10, or Q12. All four questions resolved through configuration, not envelope changes.

### Items resolved vs. remaining

| Session 1 Question | Status | Resolution |
|-------------------|--------|------------|
| Q1: Event type vocabulary | ✅ Resolved | Platform-fixed, 6 types, append-only (A3) |
| Q2: Data shape definition | ✅ Resolved | Shape definitions at L1, typed fields (B9, D3) |
| Q3: Schema versioning | ✅ Resolved | `shape_ref` in envelope (A1) |
| Q4: Config versioning & coexistence | ✅ Resolved | Atomic package, max 2 versions (B11, B12) |
| Q5: T2 boundary line | ✅ Resolved | L3 → code (D1) |
| Q6: S12 scoping | ✅ Resolved | L3a/3b, complexity budgets, DAG limits (B2, B8, D3) |
| Q7: Conflict rule config | ✅ Partially resolved | Q7a: domain uniqueness at L1. Q7b: deferred to ADR-5. |
| Q8: Role-action permissions | ✅ Resolved | L0 activity parameters (Session 2 §6.3) |
| Q9: Flag severity config | ✅ Resolved | Per-deployment override at L0 |
| Q10: Scope type extensibility | ✅ Resolved | Platform-fixed, composable (geographic + subject_list + activity) |
| Q11: Activity model | ✅ Resolved | `activity_ref` optional (A2), activity definitions at L0 |
| Q12: Sensitive-subject classification | ✅ Resolved | Shape/activity-level sensitivity at L0/L1 |

**All 12 questions resolved or partially resolved (with clear deferral).** Session 3 is complete.

### Session 3 readiness for Session 4

Session 3 accomplished:
1. **Part 1:** Verified structural coherence — all five Session 2 solutions compose into one system.
2. **Part 2:** Applied irreversibility filter — 4 of 24 positions touch the envelope. Scoped Part 3 proportionally.
3. **Part 3:** Adversarially attacked all 3 Tier 1 positions — all survived without modification.
4. **Part 4:** Resolved remaining Qs — all map cleanly to the configuration gradient, none require envelope changes.

**Session 4 charter:**
1. Write ADR-004 — consolidate all positions, classify per irreversibility tier, document decisions.
2. S00 simplicity validation — verify P7 (the simplest scenario stays simple under ADR-4's commitments).
3. Readiness audit — confirm all 12 questions are addressed, no orphaned concerns.
