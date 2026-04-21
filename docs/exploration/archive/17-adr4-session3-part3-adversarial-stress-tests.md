> **⚠️ SUPERSEDED** — This is a raw exploration document archived for reference. Decisions were finalized in **ADR-004**. For a navigated reading guide, see [guide-adr-004.md](../guide-adr-004.md).
# ADR-004 Session 3, Part 3: Adversarial Stress Tests

> Phase: 3.3 of 4 — Adversarial Stress Tests
> Date: 2026-04-12
> Input: Part 2 irreversibility filter (16-adr4-session3-part2-irreversibility-filter.md), Session 2 walk-throughs (14-adr4-session2-scenario-walkthrough.md)
> Purpose: Attempt to break the 3 Tier 1 (envelope-touching) positions with adversarial scenarios. If a position breaks, the fix is structural — discovered here, not in production. Light validation on 3 Tier 3 positions to confirm reasonableness.

---

## Scope (reframed by Part 2)

Part 2 found 4 envelope-level decisions out of 24 total positions. A4 (system actor format) classified as Tier 2 (strategy-protecting, not structural). Three Tier 1 positions receive full adversarial treatment:

| Attack | Target position | What we're trying to break |
|--------|----------------|---------------------------|
| Attack 1 | A2: `activity_ref` optionality | "Optional is wrong — it should be mandatory" |
| Attack 2 | A1: `shape_ref` versioning format | "The format can't handle breaking schema changes" |
| Attack 3 | A3: `type` vocabulary (6 types) | "6 types aren't enough — real deployments need more" |

Three Tier 3 positions receive light validation:

| Validation | Target | What we're confirming |
|------------|--------|----------------------|
| V1 | D3: Complexity budget (60 fields/shape) | Is the budget reasonable? |
| V2 | B5: Expression scope (one entity ref) | Is the scope sufficient? |
| V3 | B8: Trigger DAG max path 2 | Is the depth limit justified? |

---

## Attack 1: Multi-Activity Shape Collision

> "A deployment runs a vaccination campaign, routine immunization, and a school health screening — all using the same `vaccination_record` shape. A fourth activity, a nutrition survey, also captures the same shape at the same time. Can optional `activity_ref` handle this?"

### The scenario

An NGO operating in a district runs four concurrent activities:

| Activity | Shape | Time window | Assignment |
|----------|-------|-------------|------------|
| `routine_immunization` | `vaccination_record/v1` | Ongoing (no end date) | CHVs, geographic scope |
| `measles_campaign_2026` | `vaccination_record/v1` | May 1–21, 2026 | Campaign teams, geographic scope |
| `school_health_screening` | `vaccination_record/v1` | April 15–June 30, 2026 | School health teams, school-based scope |
| `nutrition_assessment` | `vaccination_record/v1` | June 2026 | CHVs, geographic scope |

Same shape, four activities. Two activities share the same actors (CHVs do both `routine_immunization` and `nutrition_assessment`). Three activities overlap in time. One uses a different scope type (school-based).

### The attack: what if `activity_ref` is null?

Without `activity_ref`, the only disambiguation signals are:

| Signal | Reliable? |
|--------|-----------|
| `shape_ref` | Identical across all four → useless |
| `actor_ref` | Same CHV does routine + nutrition → ambiguous for 2 activities |
| `timestamp` | Time windows overlap (May–June) → ambiguous for 3 activities |
| `subject_ref` | Same child could appear in all 4 activities → ambiguous |
| Assignment/scope data | Different scope types help (school vs. geographic), but this is projection-level inference, not event-level declaration |

**Result without `activity_ref`: 4-way ambiguity.** No combination of envelope fields disambiguates a vaccination event captured by a CHV in May for a child who appears in both routine and campaign lists. The projection engine has no reliable signal to attribute the event to the correct activity.

### The attack: what if `activity_ref` is mandatory?

Force every event to carry `activity_ref`. What breaks?

**S00 — basic household observation.** One activity (`household_survey`), one shape (`household_observation`), one role. There's no disambiguation need. The deployer must still set `activity_ref: "household_survey"` on every event. This is boilerplate — not harmful, but adds friction for the simplest use case.

**S06 — entity registry.** One activity (`facility_registry`), one unique shape (`facility`). Same: mandatory `activity_ref` is boilerplate. The shape alone suffices.

**Counter-argument: is boilerplate harmful?** Technically, a mandatory field with an obvious value is free on the device (auto-populated from the activity the user opened). The device knows which activity context the user is in — it presents the form within an activity shell. Auto-populating `activity_ref` from the activity context costs zero user friction and zero device logic beyond "stamp the current activity name on the event."

**Is there a case where auto-population fails?** Only if a device creates an event outside of any activity context — a "loose" event not attached to any activity. Does this happen?

- **Human events:** Always captured within an activity UI (the user opens an activity, fills a form). Activity context exists. Auto-population works.
- **System events (triggers):** Created by server-side triggers. The trigger knows its activity context (if any) from the source event. If the source event has `activity_ref`, the trigger output can inherit it. If the source event has null, the trigger output also has null. Consistent.
- **Import events:** Bulk-imported historical data. The importer may or may not know which activity the historical data belonged to. If it doesn't, `activity_ref: null` is the honest answer. Making it mandatory would force the importer to guess or fabricate — worse than null.

**The import case breaks mandatory.** Bulk imports of historical data (pre-platform) legitimately don't have an activity context. Forcing a value fabricates provenance.

### Verdict on Attack 1

**`activity_ref` SURVIVES as optional.** The 4-activity collision scenario proves it's essential when needed. The import scenario proves mandatory fabricates data when it shouldn't. Optional with auto-population from device context is the correct design:

| Situation | `activity_ref` value | Who sets it |
|-----------|---------------------|-------------|
| Event captured within an activity | Activity instance identifier | Device auto-populates from UI context |
| Event captured with no activity context | `null` | Default |
| Shape alone disambiguates | Present but technically redundant | Device auto-populates (harmless) |
| Shape-ambiguous (multiple activities, same shape) | **Required — disambiguation depends on it** | Device auto-populates from UI context |
| Imported historical data | `null` (honest: we don't know) | Import tool's default |
| Trigger output | Inherited from source event's `activity_ref` | Trigger engine copies |

**New insight from the attack:** The device UI always operates within an activity context (the user taps an activity to open a form). This means `activity_ref` is AUTO-POPULATED in practice — whenever the device has context (always, for human captures), it stamps the value. The optionality isn't "deployer decides whether to set it" — it's "the system stamps it when context exists, leaves null when it doesn't." This makes the field effectively mandatory for human-authored events without requiring the schema to enforce it.

**Structural consequence:** None. The envelope stays as-is (optional field, null default). The device behavior (auto-populate from activity context) is an implementation strategy, not an envelope constraint. A2 holds.

### Residual risk

**Risk:** A deployer creates two activities using the same shape and forgets that `activity_ref` matters. Both activities' events mix in projections and reports.

**Mitigation:** Deploy-time validation. When the configuration package contains two activities referencing the same shape, emit a warning: "Activities X and Y use the same shape. Events will be distinguished by activity_ref. Ensure activity context is available at capture time." This is a validation rule, not a structural change.

---

## Attack 2: Breaking Schema Change Under Committed Format

> "Shape v3 removes a required field that v1 and v2 had. 500 devices are offline with v1. 200 devices have v2. The deployer deploys v3. What happens?"

### The scenario in detail

Shape: `facility` (from S06 walk-through)

```
facility/v1 fields:
  - name          (text, required)
  - district      (select, required)
  - gps_location  (geo, required)

facility/v2 fields (additive):
  - name          (text, required)
  - district      (select, required)
  - gps_location  (geo, required)
  - has_solar_panel (boolean, optional)      # added
  - catchment_population (number, optional)  # added

facility/v3 fields (breaking):
  - name          (text, required)
  - location_code (text, required)           # NEW — replaces district + gps_location
  - has_solar_panel (boolean, optional)
  - catchment_population (number, optional)
```

v3 REMOVES `district` and `gps_location` (both required in v1/v2) and REPLACES them with `location_code`. This is a breaking change — captured data references fields that no longer exist in the latest schema.

### The attack: what does `shape_ref` format handle?

**Device state at v3 deployment:**

| Device group | Current config | Events already captured | Status |
|-------------|---------------|------------------------|--------|
| Group A (500 devices) | v1 | `shape_ref: "facility/v1"` on all events | Offline for 2+ weeks |
| Group B (200 devices) | v2 | `shape_ref: "facility/v2"` on events | Synced v2 last week |
| Group C (100 devices) | Will receive v3 at next sync | Will capture `shape_ref: "facility/v3"` | Online, sync upcoming |

**What the format guarantees:** Every event permanently declares which shape version was active when it was captured. The event store has events tagged `facility/v1`, `facility/v2`, and `facility/v3`. The shape registry retains all three versions. No confusion about which fields an event's payload should contain.

**What the format does NOT guarantee:** How the projection engine builds a unified view of a facility entity from events spanning three incompatible versions.

### Projection engine stress

A single facility, `facility_042`, has events from all three versions:

| Event # | `shape_ref` | Payload |
|---------|-------------|---------|
| 1 | `facility/v1` | `{name: "Clinic A", district: "North", gps_location: "-1.23, 36.82"}` |
| 2 | `facility/v2` | `{name: "Clinic A", district: "North", gps_location: "-1.23, 36.82", has_solar_panel: true}` |
| 3 | `facility/v3` | `{name: "Clinic A", location_code: "KE-NAI-001", has_solar_panel: true, catchment_population: 12000}` |

The projection engine needs to compute current state. But:

- Event 3 has no `district` or `gps_location`. Event 1 and 2 do.
- Event 1 and 2 have no `location_code`. Event 3 does.
- Does the current state have `district`? Or `location_code`? Or both?

### Three approaches to breaking changes

**Approach A: Forbid breaking changes entirely.**
Shape changes are append-only (add fields, deprecate fields, but never remove fields from future versions). v3 cannot remove `district` — it can only mark it `deprecated: true`, hiding it from new forms but keeping it in the schema.

- **Pro:** No cross-version projection problem. v3 events still carry `district: null` (deprecated but present). Unified view works identically to additive changes — null means "not captured at this version."
- **Con:** Shapes accumulate cruft forever. After many versions, a shape might have 40 active fields and 30 deprecated ones. The schema grows but never shrinks.
- **Assessment:** This is the same append-only principle as the event store (P3). Events never delete. Shapes never remove fields. Consistent philosophy. The cruft concern is bounded by the 60-field complexity budget — deployers who need radical restructuring create a NEW shape and a NEW activity, rather than contorting the old shape.

**Approach B: Allow breaking changes with a migration declaration.**
Session 2 proposed this: the deployer declares a migration when creating v3.

```
shape: facility
version: 3
based_on: facility/v2
breaking_changes:
  - removed: district
    migrated_to: location_code
    migration: "lookup_table"  # ← what IS this?
  - removed: gps_location
    migrated_to: location_code
    migration: "lookup_table"
```

What does `migration: "lookup_table"` mean operationally?

- The projection engine needs a function: given a `district` value and a `gps_location` value, produce a `location_code`. This is a DATA TRANSFORMATION — not a static mapping, not a simple rename.
- Who writes this function? The deployer? In what language? This is AP-1 territory — the configuration system is becoming a data-transformation platform.
- What about the DEVICE projection? Offline devices running v1 project facility entities from v1 events. When they sync and receive v3 + the migration, they need to reproject all historical facility events through the migration function. Can the device run arbitrary migration logic? On 8GB Android? For hundreds of entities?

**Assessment:** Migration declarations SOUND clean ("just declare the mapping") but REQUIRE either (a) a migration-expression language (AP-1) or (b) server-side-only migration execution (devices don't migrate, they receive migrated projections). Option (b) is viable but means devices running v1 offline can't project a facility entity consistently with the server's v3 understanding. The device shows `district: "North"` while the server shows `location_code: "KE-NAI-001"`. This inconsistency persists until the device syncs.

**Approach C: Breaking change = new shape.**
v3 isn't `facility/v3`. It's a new shape: `facility_v2` (a new shape, not a new version of the old one). The old `facility` shape is deprecated. New events use the new shape. Historical events retain the old shape. The projection engine maintains two separate streams.

- **Pro:** No cross-version projection problem. Each shape version stream is internally consistent.
- **Con:** The deployer now manages two shapes for the same real-world entity type. Entity projections must merge data from old-shape and new-shape events. The "merge" logic is the same problem as Approach B's migration — where does the transform live?
- **Assessment:** This doesn't solve the problem, it just relocates it from the shape registry to the projection layer.

### Verdict on Attack 2

**The format `shape_ref: "facility/v3"` handles the breaking change correctly at the event level.** Every event self-declares its version. The shape registry retains all versions. No event lose interpretability. The format is not the problem.

**The problem is the projection engine's cross-version merge strategy.** This is NOT an envelope concern — it's a platform implementation concern. The envelope's job is to ensure events are self-describing and version-tagged. It does that.

**However, the attack reveals a policy question: should the platform ALLOW breaking schema changes, or enforce deprecation-only?**

**Position: Deprecation-only (Approach A) is the default.** Breaking changes are extraordinary events that require an explicit deployer decision with server-side migration support. The detailed mechanism for breaking changes is an implementation concern — Session 3 should NOT attempt to design the migration system. What Session 3 CAN commit to:

| Decision | Classification | Rationale |
|----------|---------------|-----------|
| The default schema evolution model is additive-only with deprecation | Strategy-protecting constraint | Prevents unbounded projection complexity |
| Breaking changes require deployer acknowledgment and server-side migration | Strategy (implementation detail) | The mechanism is evolvable |
| `shape_ref` format handles both additive and breaking changes identically | Structural constraint (confirmed) | The format doesn't care what changed between versions |
| Devices see deprecated fields as `null` in new-version events; old-version events retain their values | Implementation strategy | Consistent with append-only philosophy |

**What survives this attack:** A1 (the format) is solid. The version integer in `shape_ref` is monotonic and unambiguous. The shape registry retains all versions. Events are self-describing across versions. The breaking-change policy question is orthogonal to the format — it's a B10 concern (change classification), which is Tier 3 (evolvable strategy). No envelope change needed.

### Residual risk

**Risk:** A deployer with 20 shape versions accumulated over 3 years has 15 deprecated fields and 12 active ones. The projection engine handles this, but the deployer tooling shows a cluttered schema.

**Mitigation:** Deployer tooling hides deprecated fields from the form builder by default. The shape registry shows "12 active fields, 15 legacy" with an expand button. This is UX, not architecture.

**Risk:** If breaking changes are allowed via server-side migration, and the migration logic is wrong, projections become inconsistent.

**Mitigation:** Server-side migration is a privileged operation with audit trail, rollback capability, and pre-migration validation (dry-run against historical events). This is operational procedure, not envelope design.

---

## Attack 3: Type Vocabulary Exhaustion

> "A deployer needs event types that don't exist in the 6-type vocabulary. Their deployment requires: case_opened, case_resolved, transfer, feedback, referral_accepted, stock_received. They need 6 new types. The platform has 6. Are we at 12 already?"

### The scenario

A health program combines:
- Community case management (S08): cases open, progress, resolve, transfer
- Resource distribution (S07): supplies transfer, acknowledge, reconcile
- Referral chains (S20+): refer, accept, complete
- Feedback collection: beneficiaries provide feedback on services

The deployer lists their "events":

| Deployer's event | What actually happens |
|-----------------|---------------------|
| "Case opened" | CHV records initial encounter with a new health issue |
| "Case resolved" | CHV records final visit, outcome documented |
| "Transfer" | Case responsibility moves from one CHV to another |
| "Feedback" | Beneficiary provides satisfaction rating |
| "Referral accepted" | Facility nurse confirms referral receipt |
| "Stock received" | CHV confirms supply delivery receipt |

### The attack: do these fit into 6 types?

Map each to the structural vocabulary:

| Deployer's event | Structural type | Shape | Why this mapping |
|-----------------|----------------|-------|------------------|
| Case opened | `capture` | `case_intake/v1` | Human records an observation (initial encounter). The shape carries diagnosis, symptoms, triage. |
| Case resolved | `capture` | `case_resolution/v1` | Human records an observation (final visit outcome). The shape carries resolution type, outcome. |
| Transfer | `assignment_changed` | N/A (envelope-level) | Authority over a subject changes. ADR-3's scope mechanism handles this. No shape-specific payload needed beyond what `assignment_changed` carries. |
| Feedback | `capture` | `service_feedback/v1` | Human records data (satisfaction, comments). Shape carries rating, text, service reference. |
| Referral accepted | `capture` | `referral_response/v1` | Human records an action (acknowledging a referral). Shape carries source referral ref, acceptance status. |
| Stock received | `capture` | `delivery_receipt/v1` | Human records an observation (received items). Shape carries item list, quantities, condition. |

**Result: 5 out of 6 map to `capture` with different shapes. 1 maps to `assignment_changed`.** Zero new structural types needed.

### Why does `capture` absorb so much?

Because `capture` means "a human records structured data about a subject." That's the foundational operation of field-based data collection — which is what this platform exists for. The DOMAIN meaning (case opened vs. feedback vs. stock received) lives in the shape, not in the structural type.

The structural type determines PROCESSING BEHAVIOR:
- `capture` → shape validation → projection pipeline → conflict detection
- `review` → source-event linking → review-status update in projection
- `alert` → target-role routing → notification pipeline
- `task_created` → deadline tracking → response watch
- `task_completed` → task resolution → deadline cancellation
- `assignment_changed` → scope recomputation → sync scope update

Different structural types exist because they require DIFFERENT PLATFORM PROCESSING, not because they represent different domain concepts. Adding `case_opened` as a structural type would mean: the platform processes "case opened" events differently from other captures. Does it? What different processing behavior does "case opened" trigger that `capture` + shape-based projection doesn't handle?

### Counter-attack: ADR-5 event types

Part 2 noted that ADR-5 (state progression) might need new structural types. Let's test the hardest candidates:

**`case_opened` — does it need unique processing?**

A case lifecycle has states: open → in-progress → referred → resolved → closed. State transitions are a projection concern — the projection engine derives current case state from the event sequence. The events are all `capture` events with different shapes (intake, follow-up, resolution). The projection engine doesn't need a special `case_opened` event type to know a case was opened — it sees the first `capture` with a `case_intake` shape for a new subject and projects: "case state = open."

**What if the projection engine DOES need a structural signal?** If projection logic needs to distinguish "this capture opens a case" from "this capture is a follow-up," the distinction lives in either:
- The shape (different shapes for intake vs. follow-up → projection routes on shape) — already works.
- A payload field (`interaction_type: "intake"` vs. `interaction_type: "follow_up"`) — deployer-configured, shape-level.
- The `activity_ref` context (different activities for different case phases) — already works.

None of these require a new structural type. The projection engine already routes on shape_ref and payload fields.

**`transfer` — does it need unique processing?**

A transfer moves a case from CHV A to CHV B. This IS different from a normal capture — it changes WHO is responsible for a subject. That's an `assignment_changed` event. No new type needed.

But wait — what if the transfer carries data? ("CHV A transfers with notes: patient allergic to ACTs, switch to alternative.") Transfer notes are payload data. Two options:
- `assignment_changed` with a payload shape (e.g., `transfer_notes/v1`). But `assignment_changed` today has no shape — it's an authority operation, not a data capture.
- Two events: `assignment_changed` (authority transfer) + `capture` with `case_transfer_notes/v1` (data). This keeps the structural types clean — authority changes are structural, data is captured.

**Position:** Two events for data-bearing transfers. This is honest: two things happened (authority changed + information was recorded). Merging them into one event would overload `assignment_changed` with payload semantics it doesn't have, or overload `capture` with authority semantics it doesn't have.

**`status_changed` — a general state-transition type?**

This is the strongest candidate for a new structural type. If ADR-5 introduces explicit state machines (case: open → resolved, approval: pending → approved → rejected), every state transition is structurally similar: "subject X moved from state A to state B." This might warrant different processing from `capture`:
- `capture` triggers projection updates based on payload content.
- `status_changed` triggers state-machine validation (is the transition legal?) and state-dependent behavior changes (what's allowed in the new state?).

**Assessment: This is a legitimate ADR-5 question.** If ADR-5 introduces state machines as a platform concept, `status_changed` as a 7th structural type is plausible. But:
- State transitions can also be modeled as `capture` events where the shape includes a `new_status` field, and the projection engine validates the transition via logic rules.
- The decision depends on whether state machines are a PLATFORM primitive (with platform-enforced transition rules) or a PROJECTION concern (derived from event sequences by configuration).

**This is exactly the kind of question Session 3 should NOT lock.** ADR-5 will explore state progression. The current 6-type vocabulary is sufficient for all ADR-4 scenarios. If ADR-5 determines that `status_changed` needs different processing behavior, it adds a 7th type. The append-only vocabulary commitment (from A3) makes this cheap.

### Verdict on Attack 3

**The 6-type vocabulary SURVIVES.** None of the deployer's 6 "needed" types require new structural types — they're all `capture` events with domain-specific shapes. The structural vocabulary is small because it represents PROCESSING BEHAVIOR, not DOMAIN MEANING.

**ADR-5 reservation:** `status_changed` is a plausible 7th type, contingent on ADR-5's state machine design. The append-only vocabulary commitment makes future addition cheap with no data migration. The current vocabulary is not "6 forever" — it's "6 to start, grow as proven necessary."

**The principle that holds:** A new structural event type is justified only when it requires DIFFERENT PLATFORM PROCESSING BEHAVIOR. If the only difference is domain meaning (captured by shape) or context (captured by activity_ref), it's `capture`.

### Residual risk

**Risk:** A deployer finds `capture` semantically overloaded — "everything is a capture" feels wrong even if it's structurally correct. Deployer experience suffers.

**Mitigation:** Deployer tooling presents events in domain terms ("Case opened"), not structural terms ("capture with case_intake/v1 shape"). The structural type is a platform concern; the deployer works with activities and shapes. The mapping between domain language and structural types is handled by the activity definition — the activity template says "when a user opens a case, they fill the case_intake form, which produces a capture event." The deployer never sees the word "capture" in normal workflow.

**Risk:** A future requirement genuinely requires different processing for a domain event, and the deployer assumes "just use capture with a different shape" without realizing the processing difference matters.

**Mitigation:** Platform evolution. When a processing distinction is proven necessary (via a scenario walk-through like this one), a new structural type is added. The append-only vocabulary means old events are unaffected. The cost of adding a type is code, not migration.

---

## Light Validation V1: Complexity Budget — 60 Fields Per Shape

> "A deployer creates a shape with 200 fields. Is 60 the right limit?"

### The reasoning chain

**Why does a limit exist?** To prevent:
1. Form UI degradation (scrolling through 200 fields on mobile)
2. Payload size inflation on constrained devices (8GB Android, SQLite storage)
3. Validation complexity (200 fields × N rules = combinatorial explosion)
4. Deployer comprehension failure (no one reviews a 200-field form)

**Why 60 specifically?** Session 1's spike (002-walk-through-scenarios-primitives.md) analyzed real-world CHV forms. The largest known form (a comprehensive household registration) has ~45 fields. Medical intake forms: 30–40. Quarterly stock reports: 15–20. The 95th percentile of real operational forms falls under 50 fields. 60 is a ceiling with headroom.

**What if a legitimate use case exceeds 60?** Split into sub-forms. A 120-field comprehensive assessment becomes: `assessment_demographics/v1` (20 fields) + `assessment_clinical/v1` (30 fields) + `assessment_environment/v1` (25 fields) + `assessment_supplies/v1` (20 fields). Four captures, four events, four shapes. Each is comprehensible. The activity orchestrates the sequence.

**Does splitting change the data model?** No. The subject is the same across all four events. The projection engine composes them. The deployer defines 4 shapes and 1 multi-stage activity.

**Could the limit be changed later?** Yes — it's a deploy-time validation rule (Tier 3, initial strategy). Raising it to 80 or lowering it to 40 requires a code change to the validator, not a data migration. No events are affected.

### Verdict

**60 is reasonable.** It exceeds real-world maxima with margin. It forces good form design (split large forms). It's evolvable. No deeper analysis warranted — this is exactly the kind of guard that should be set pragmatically and adjusted based on deployment experience.

---

## Light Validation V2: Expression Scope — One Entity Reference

> "A logic rule needs to reference attributes from 3 different entities at once. Is one-entity scope sufficient?"

### The reasoning chain

**Current position (Part 1, Check d):** Expressions can reference `payload.*` (current event's fields) and `entity.*` (the event's subject entity — one entity). They cannot reference attributes of OTHER entities.

**Why one entity?** The device has projection data for the event's subject (synced via ADR-3). Other entities may or may not be available locally. Referencing `other_entity.attribute` requires:
1. The device to have synced that entity's projection
2. The expression evaluator to resolve cross-entity references (a join operation)
3. Offline consistency guarantees for cross-entity reads

This is a query engine, not an expression evaluator. It's the slope to AP-1.

**The 3-entity scenario:** A patient encounter rule needs:
- `payload.diagnosis` (current event — always available)
- `entity.age_months` (patient — the subject, available)
- `facility.has_oxygen` (the facility where the CHV works — a different entity)

The rule: "If diagnosis is pneumonia AND patient is under 2 AND facility has no oxygen → warn: immediate referral to district hospital."

This is a legitimate clinical protocol. The deployer wants it. Does one-entity scope block it?

**Resolution without expanding scope:** The facility's `has_oxygen` status can be made available to the CHV's device as a **context attribute** — a projection-derived value for the actor's assigned facility, loaded at session start. The expression becomes:

```
when: "payload.diagnosis == 'pneumonia' AND entity.age_months < 2 AND context.facility_has_oxygen == false"
```

This adds a third data scope (`context.*`) containing actor-level and location-level attributes derived from projections. But importantly, this scope is:
- Pre-resolved at form-open time (not evaluated per-expression)
- Limited to the actor's own assignment context (not arbitrary entity queries)
- Read-only and static during the form session

Is `context.*` a new evaluation context or a scope expansion? It's a scope expansion within the existing `form` evaluation context. The evaluation context gains three reference pools:
- `payload.*` — current event's fields
- `entity.*` — subject entity's projected attributes
- `context.*` — actor's operational context (assigned facility attributes, scope-level facts)

**Is this acceptable?** It doesn't create a query engine (no joins, no arbitrary entity references). It requires the platform to pre-compute `context` attributes at form-open time, which is a projection engineering task. The expression evaluator stays trivial — it reads pre-resolved values, not live entity lookups.

**Position: One entity reference (`entity.*`) plus a pre-resolved context scope (`context.*`) is sufficient. No cross-entity dynamic references.** This is a strategy that can evolve (the set of available context attributes grows with the platform) without changing the expression evaluator's architecture.

### Verdict

**One-entity scope holds with a `context.*` extension.** The deployer gets access to actor-level contextual facts without the expression evaluator becoming a query engine. The `context` scope is pre-resolved, bounded, and static. This is a refinement to note for ADR writing, not a structural change.

**Caveat:** The `context.*` scope is a new idea surfaced by this stress test. It needs further validation in ADR-5 scenarios before committing. For now, it's noted as a plausible extension — not part of the ADR-4 commitment.

---

## Light Validation V3: Trigger DAG Max Path Length 2

> "A deployer configures a trigger chain of depth 3. Is the limit of 2 justified?"

### The reasoning chain

**Current position (Part 1, Check h):** The trigger dependency graph is a DAG with max path length 2 (3 trigger nodes). Example:
- Trigger A (event-reaction): stockout → create investigation_task
- Trigger B (deadline-check): investigation not completed in 48h → create regional_alert
- Trigger C (deadline-check): regional_alert not acknowledged in 72h → create national_escalation

**What would depth 3 enable?** A fourth trigger:
- Trigger D: national_escalation not acknowledged in 1 week → ???

What is "???" operationally? The escalation has already reached the national level. What comes after national? In organizational reality:
- Emergency protocol activation (beyond platform scope — phone calls, physical intervention)
- External notification (SMS to director, beyond platform event model)
- System shutdown of the distribution channel (extreme, would be code not config)

None of these are "create another event and wait for response." They're out-of-system actions. The platform's trigger chain is the in-system escalation ladder. When the ladder runs out, the response is human intervention outside the system.

**Why not set the limit higher "just in case"?** Each additional chain level multiplies latency:
- Level 1: fires at event ingestion → immediate
- Level 2: fires after deadline (e.g., 48h + sync latency) → ~48-72h
- Level 3: fires after second deadline (e.g., 72h + sync) → ~120-144h from original event
- Level 4: fires after third deadline → ~7+ days from original event

At depth 3 (a hypothetical level 4 trigger), you're creating an automated response to something that happened a WEEK ago. Automated responses with week-long delays are not useful — they're noise. The human escalation at level 3 (national) should have resolved the issue or declared it unresolvable within that timeframe.

**"Just in case" also increases validation complexity.** The deploy-time validator must check for cycles and depth violations in the trigger DAG. Deeper chains mean longer paths to validate, more edge cases in cycle detection, and more potential for deployer confusion ("which trigger watches which?"). The limit of 2 keeps the DAG visually inspectable — the deployer can trace any chain in their head.

### Verdict

**Max path length 2 is justified.** It covers the realistic escalation ladder (immediate → delayed → national). Deeper chains hit diminishing returns (week-long latency), out-of-system boundaries, and validation complexity. The limit can be raised later (it's a deploy-time validation rule, Tier 3) if a scenario proves that depth 3 serves a genuine need. No such scenario has emerged across 21 scenarios.

---

## Cross-Attack Synthesis

### What survived without modification

| Position | Attack | Outcome |
|----------|--------|---------|
| A1: `shape_ref` format | Breaking schema change (Attack 2) | **Survived.** Format handles breaking changes correctly. The projection engine's merge strategy is a separate concern. |
| A2: `activity_ref` optionality | 4-activity same-shape collision (Attack 1) | **Survived.** Optional is correct. Auto-populated from device context in practice. Import use case proves mandatory is wrong. |
| A3: `type` vocabulary (6 types) | 6 "new" domain types (Attack 3) | **Survived.** All mapped to existing structural types. Domain meaning lives in shapes, not types. |
| D3: 60 fields/shape | 200-field form (V1) | **Survived.** Budget exceeds real-world maxima. Split into sub-forms for larger needs. |
| B8: DAG max path 2 | Depth 3 chain (V3) | **Survived.** Deeper chains hit latency and out-of-system boundaries. |

### What gained new insight

| Position | Attack | Insight |
|----------|--------|---------|
| A2 | Attack 1 | Device auto-population means `activity_ref` is effectively mandatory for human captures, schema-optional for imports/edge cases. Deploy-time validation should warn on same-shape multi-activity configurations. |
| A1 | Attack 2 | Default evolution model should be deprecation-only (additive + deprecation). Breaking changes are an extraordinary operation with server-side migration support. This is a B10 strategy refinement, not an A1 change. |
| A3 | Attack 3 | `status_changed` is a plausible 7th type for ADR-5, contingent on state-machine design. The principle: new type justified only when it requires different platform processing behavior. |
| B5 | V2 | `context.*` scope (actor's operational context, pre-resolved) is a plausible extension for form-context expressions. Needs ADR-5 validation before commitment. |

### Envelope status after attacks

No envelope changes. The 11-field envelope from Part 2 survives all three Tier 1 attacks intact:

```
id              # ADR-1: UUID, client-generated
type            # ADR-1 + ADR-4: 6 structural types (append-only vocabulary)
shape_ref       # ADR-4: "{shape_name}/v{version}", mandatory, naming: [a-z][a-z0-9_]*
activity_ref    # ADR-4: optional, auto-populated by device, null for imports/edge cases
subject_ref     # ADR-2: typed identity reference
actor_ref       # ADR-2: who/what created this event
device_id       # ADR-2: hardware-bound device identifier
device_seq      # ADR-2: device-local sequence number
sync_watermark  # ADR-2: server-assigned ordering (null until synced)
timestamp       # ADR-1: device_time (advisory)
payload         # ADR-1: shape-conforming data
```

### Items carrying forward to Part 4

Part 4 (Q7/Q9/Q10/Q12 resolution) should address:

1. **Q7 — Breaking change policy (from Attack 2):** Default deprecation-only vs. server-side migration for breaking changes. This is a B10 refinement.
2. **`context.*` scope (from V2):** Should this be a Part 4 topic or deferred to ADR-5? Recommendation: defer — it emerged from a light validation, not a structural attack, and depends on ADR-5's state-progression model.
3. **`status_changed` type (from Attack 3):** Deferred to ADR-5. Noted as a growth path for the type vocabulary.
4. **Deploy-time validation warnings (from Attack 1):** Same-shape multi-activity configurations should trigger a deployer warning. This is a tooling concern for ADR writing, not an exploration question.
