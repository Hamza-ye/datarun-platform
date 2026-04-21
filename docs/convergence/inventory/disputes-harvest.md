# Phase 0 — Archive Harvest on the 9 Disputed Concepts

> **Purpose**: extract the reasoning left undocumented when ADRs narrowed to
> conclusions. This document feeds Phase 2 ADR drafting; every Phase 2 ADR on
> a disputed concept must cite the passages below as its starting point, not
> re-derive from architecture/ or phase specs.
>
> **Authority order** (per 2026-04-22 project-state correction):
> root docs > ADRs > `exploration/archive/` stress-tests > `architecture/` late consolidation.
>
> **Method**: three parallel subagents, one per concept-group, each read tiers
> 1–3 only. Architecture/ and phase specs deliberately excluded. Silence is
> reported as silence; reasoning is not fabricated.

---

## Cross-cutting finding — the PRIMITIVE ↔ CONFIG duality

All 9 disputes resolve to the **same structural shape**: each concept is
platform-fixed at the mechanism level and deployer-configured at the instance
level. Naming a concept as only PRIMITIVE loses the knob; naming it only
CONFIG loses the architecture.

| concept | platform-fixed mechanism | deployer-configured instance |
|---|---|---|
| `scope` | ADR-3 S1–S4 — sync=scope, assignment-based access, authority-as-projection | scope-type selection per role, scope-containment values, sensitive-subject lists |
| `pattern` | ADR-5 S4–S6 — state-as-projection, composition rules, closed pattern registry | which pattern an activity selects, parameters (levels/roles/deadlines) |
| `activity` | ADR-4 S2 — activity_ref envelope field, correlation semantics | the activity catalog itself (instances, configurations, sensitivity) |
| `flag` | ADR-002 S14 / Archive 09 Q12 — "never reject" protocol commitment | flag-category catalog, resolvability classification, per-flag severity |
| `conflict-detected` | ADR-002 S13 — CD on raw refs, single-writer resolution | (none — fully platform-fixed shape post-Addendum) |
| `accept-and-flag` | ADR-002 S14 — structural invariant | detection locale + sweep cadence (ADR-3 implementation choice) |
| `subject-ref` / `actor-ref` / `activity-ref` | envelope CONTRACT — typed UUID field format | the identities and activity instances the refs point to |

**Implication for Phase 2**: rather than 9 separate ADRs, one ADR
(*"Platform-fixed mechanism vs. deployer-configured instance — the disputed-concept
duality"*) probably settles the shared structural question, followed by
shorter per-concept ADRs that only say "how does this specific concept sit on
the duality." The 9 disputes may collapse to 2–4 Phase 2 ADRs.

**Caveat for the ledger**: the 11-label convergence vocabulary forces a
*single* classification per concept. The duality says a single label is
lossy. Phase 2 must either (a) accept the lossiness and pick the dominant
label per concept (recording the subordinate reading in `notes`) or (b)
extend the vocabulary with a compound label (e.g. `PRIMITIVE+CONFIG`). See
`convergence/protocol.md` for the decision mechanism.

---

## Group 1 — Integrity semantics

### `accept-and-flag`

**Dominant framing — INVARIANT** (with mechanism-level ALGORITHM details).

Key quotes:

- ADR-002 S14 — *"The platform NEVER rejects a validly-structured event based
  on subject state staleness. Events recorded against deactivated, merged,
  split, reclassified, or otherwise changed subjects are accepted and stored.
  State anomalies are surfaced as separate `ConflictDetected` events."*
- ADR-002 S14 (consequences) — *"This is the foundation of the accept-and-flag
  mechanism: every event that was physically recorded in the field is
  preserved. The platform's job is to surface anomalies, never to discard data."*
- Archive 09 Q12 (Bucket 1) — *"Orphaned events accepted-and-flagged — this is
  a protocol commitment that events are never rejected."*

**"X over Y" (rejected alternatives)** — archive 07 §A5:

> "Accept-and-flag creates a temporal window between event acceptance and
> flag creation. Any policies that fire on accepted events during this window
> create work based on potentially flagged events. No rollback mechanism
> exists for downstream consequences."

Rejection (reject-invalid-events) was rejected because it destroys the
offline-first promise: field workers capture; the platform cannot refuse
their history. The temporal-window cost is accepted; ADR-002 S12
(*"detect-before-act"*) mitigates by excluding flagged events from policy
triggers until resolved.

**Boundary clarification** — ADR-001 distinguishes *structural* validation
(envelope malformed → reject) from *state* validation (stale subject →
accept-and-flag). Archive 09: *"S14 addresses only state-based validation,
not structural validation."* This boundary is the single largest piece of
interaction detail that did not make it into architecture/.

**Phase 2 recommendation**: classify as **INVARIANT** (the "never reject"
property), with the detection procedure classified as ALGORITHM under
`conflict-detection`. Stop conflating the property with the procedure.

---

### `flag`

**Dominant framing — ambiguous in the archive, formalized in ADR-002 Addendum.**

Archive 05 (event storm) introduces `ConflictDetected` and
`DuplicateCandidateIdentified` as distinct domain events but does not
classify "flag" as a category. Archive 07 §A1 asks explicitly: *"There is
confusion about where flags are created."* Archive 09 resolves partially:

- Q12 (Bucket 1 — INVARIANT) — flags as protocol commitment
- A1 (Bucket 2 — strategy) — *"flag creation location is a projection/policy
  concern; the event schema is the same regardless of who emits ConflictDetected."*

The archive says flags-as-invariant is settled; flags-as-derived-events
(produced by projection/policy logic) is settled; what the archive **did not
predict** is ADR-002 Addendum (Phase 3e retrofit) formalizing the 4 flag
sub-types as platform-bundled *shapes*.

**Reconciliation across the classifications**:

- PRIMITIVE reading (C2): *the class of events called "flags"* is first-class.
- INVARIANT reading (C1): *the property "all integrity anomalies surface as
  flags, not as rejections"* always holds.
- DERIVED reading (B): *each flag instance* is produced by the Conflict
  Detector, not authored by an actor.

All three are true at different levels. The ledger must pick one.

**Phase 2 recommendation**: **INVARIANT** dominates — flags are the concrete
expression of accept-and-flag. The DERIVED aspect is captured by
`flag-catalog`, `flag-category`, `conflict-detector` (separate rows). The
PRIMITIVE reading is actually "integrity event" as a *category of envelope
`type`* which is already captured by `alert` and `type-vocabulary`.

---

### `conflict-detected`

**Dominant framing — CONTRACT (platform-bundled shape, post-Addendum).**

This is the concept whose *drift* motivated the Phase 3e retrofit. Archive
vs. post-Addendum evidence:

- Archive 05 — treats `ConflictDetected` as a domain event with no envelope
  discrimination (*"server-side matching rules identify two subjects that
  might be the same"*)
- Archive 07 §A1 — treats it as a strategy/config question (where does it run?)
- Archive 09 B2 (Bucket 1) — **locks it as event semantics**: *"Conflict
  detection operates on raw event references before alias resolution…
  determines what the ConflictDetected event records."*
- Phase 1–2 code — drifted into persisting `conflict_detected` as an envelope
  `type` value (not a shape)
- ADR-002 Addendum (2026-04-21) — *"Those four strings above are **shape
  names**, not envelope types. Each is now a **platform-bundled internal
  shape** with the envelope `type` assigned by semantic authorship."*

The 4-way classification dispute in Phase 0 is exactly the drift surface. The
Addendum corrected code; the docs still carry the pre-Addendum vocabulary in
places (which is why subagent C2 found PRIMITIVE framing and subagent C1
found CONFIG framing — those are the un-migrated prose layers).

**Rejected alternatives** — archive 07 §A1 compared three detection locales
(server-only, device-only, both). The server-only decision was locked in
archive 09 B2. No archive considered "conflict as separate table" vs. "conflict
as event with shape" — that design question was deferred to ADR-004 S3 and
resolved in the 3e Addendum.

**Phase 2 recommendation**: **CONTRACT** (shape-as-contract). Status: this is
already settled by ADR-002 Addendum + Phase 3e code, so this dispute should
resolve to STABLE in round 1 once the ledger row cites the Addendum.

---

## Group 2 — Envelope refs

### `subject-ref`

**Dominant framing — CONTRACT (field format), with PRIMITIVE *type* enum.**

The inventory dispute between A:PRIMITIVE and C1:CONTRACT is a category
error. `Subject` is PRIMITIVE (ADR-002 S2). `subject_ref` is the envelope
*field that references a subject* — a typed UUID format (ADR-001 + envelope
schema). The ref is not the primitive; it is the contract by which events
point at primitives.

Key passages:

- ADR-001 S5 — *"Subject association — events relate to subjects, but how
  subject identity works is ADR-2."* (ADR-1 commits to the field; ADR-2
  commits to the type taxonomy.)
- ADR-002 S2 — four typed identity categories: Subject, Actor, Process,
  Assignment. All carry `{type, id}` shape.
- Archive 09 — *"All identity references use the same typed UUID reference
  format."*

**RESERVED dimension**: the `subject_ref.type` enum includes `"process"`,
explicitly marked reserved in envelope schema (*"reserved for future
workflow-instance refs… No current emission site"*). Per Phase 3e audit B1
this is now documented in both envelope schema copies.

**Phase 2 recommendation**: **CONTRACT** for the field; keep `subject`
(separate row) as PRIMITIVE; add `process-identity` (separate row already
exists) as RESERVED. The dispute dissolves once we distinguish the
*referent* (primitive) from the *reference* (contract).

---

### `actor-ref`

**Dominant framing — CONTRACT (live, evolvable system-actor convention).**

Fully live in both human (UUID) and system (`system:{component}/{id}`)
forms. The B:RESERVED classification is a subagent misread — no form of
`actor_ref` is reserved.

Key passages:

- ADR-004 S4 — *"`actor_ref` format `system:{source_type}/{source_id}`.
  `source_type` starts with `trigger` and grows as new system sources emerge."*
- Archive 16 — *"The `actor_ref` field itself is committed (ADR-2). The
  VALUE format for system actors is less irreversible… strategy-protecting
  constraint (Tier 2)."*
- Archive 18 — *"Auto-resolution events use actor_ref format
  `system:auto_resolution/{policy_id}`."* (growing convention)

**Possible future surface** (not reserved, but note-worthy): if Assignment
becomes its own reference type, a structural design decision surfaces —
parameterize existing fields or add `assignment_ref`. No archive material
commits either way.

**Phase 2 recommendation**: **CONTRACT**. Drop RESERVED framing — it was a
subagent artifact, not archive evidence.

---

### `activity-ref`

**Dominant framing — CONTRACT (live, optional-with-auto-populate).**

Same story as `actor-ref` — subagent B:RESERVED was a misread. The field is
fully deployed (Phase 3d.1 threads it through mobile).

Key passages:

- ADR-004 S2 — *"Events may carry `activity_ref`: an optional field
  identifying which activity instance the event was captured within.
  Format: deployer-chosen identifier matching `[a-z][a-z0-9_]*`, or null."*
- Archive 17 (Attack 1) — *"Without `activity_ref`, 4-way ambiguity… No
  combination of envelope fields disambiguates."* (justifies the field)
- Archive 17 — *"Optional with auto-population from device context is the
  correct design… makes the field effectively mandatory for human-authored
  events without requiring the schema to enforce it."*

Archive does not discuss activity_ref versioning or activity hierarchies.

**Phase 2 recommendation**: **CONTRACT**. Drop RESERVED framing.

---

## Group 3 — Authorization / workflow

### `scope`

**Dominant framing — HYBRID (PRIMITIVE mechanism, CONFIG instances).**

The pivot happened in archive 12 (ADR-3 course correction):

> "ADR-003 has exactly ONE irreversible decision: what goes in the event
> envelope's authority_context. Everything else is sync protocol, projection
> architecture, device policy, or processing strategy. All of those are
> changeable after deployment without migrating stored events — they're
> evolvable."

Root-docs framing (authority order = highest) is PRIMITIVE:

- access-control-scenario.md §33 — *"Authority is contextual… may vary
  depending on which activity they're participating in, which area they're
  operating in, or which subjects they're working with."*
- principles.md P6 — *"Authority rules must be enforceable offline."*

ADR-004 S7 makes the knob explicit:

> "Scope types are platform-fixed. Deployers select and compose scope types
> for each assignment but cannot define custom scope types or custom
> containment logic… Three initial scope types: geographic, subject_list,
> activity."

**Phase 2 recommendation**: **PRIMITIVE** for `scope` (the model/concept);
keep `scope-type`, `scope-composition`, `geographic-scope`, `subject-based-
scope`, `query-based-scope`, `temporal-access-bounds` as CONFIG (already
classified this way — consistent). Dispute resolves: the primitive framing
captures the mechanism; the type enum captures the knob.

---

### `pattern`

**Dominant framing — PRIMITIVE (closed registry), with CONFIG selection.**

ADR-005 and archive 19 establish the closure:

- ADR-005 S5 — *"Patterns are a closed vocabulary with the same governance as
  event types — platform-fixed, deployer-referenced, not deployer-authored.
  Adding a pattern is a platform evolution, not a deployer action."*
- Archive 19 — *"The Pattern Registry: a set of platform-fixed workflow
  skeletons that deployers select and parameterize at Layer 0."*
- Archive 20 — *"The deployer does not see or author the state machine. They
  see: 'this is a case management activity where CHVs track malaria cases…'
  They select a pattern, map shapes to pattern roles, set deadlines, and deploy."*

The six named patterns (capture_only, capture_with_review,
entity_lifecycle, multi_step_approval, case_management, periodic_capture)
were **harvested from behavioral_patterns.md § atomic-patterns**, not
invented in ADR-005. The registry is open to platform-level growth but
closed to deployer authorship.

**Phase 2 recommendation**: **PRIMITIVE** for `pattern` itself (it names a
platform-fixed object class); keep individual pattern names
(`capture-only`, `case-management`, etc.) as PRIMITIVE too (they're
members of the closed registry); keep `pattern-role`,
`pattern-composition-rule`, `workflow-pattern` as CONFIG where they denote
deployer-facing knobs. This makes `pattern` consistent with
`pattern-registry` (already PRIMITIVE) — the dispute resolves.

---

### `activity`

**Dominant framing — HYBRID, leans CONFIG.**

Weakest case for PRIMITIVE of the three.

- ADR-004 S2 commits the *envelope field* `activity_ref` as PRIMITIVE
  machinery.
- ADR-004 S9 treats *activities themselves* as L0 assembly: *"The deployer-
  facing model: 'this is a case management activity where CHVs track cases…'
  … assembling from platform-provided components."*
- Archive 13 Q11 — *"How are 'activities' (a campaign, a routine program, a
  case management workflow) defined and how do events correlate to them?"*
  (framed as config question, not primitive question)

Unlike `scope` (where root-docs framing was explicitly PRIMITIVE) or
`pattern` (where the registry closure locks PRIMITIVE), an activity is a
deployer-bundled *assembly* of (shape + pattern + role + scope) named and
configured per deployment. The platform owns the assembly grammar, not the
assembled instances.

**Phase 2 recommendation**: **CONFIG** for `activity` (consistent with
`l0-assembly` which is PRIMITIVE — activity is what l0-assembly *produces*,
not the mechanism). Keep `activity-ref` separate as CONTRACT. The PRIMITIVE
reading from subagent A was framing activity-as-concept-category, which is
valid but dominated by the config-instance reading once you separate the
envelope field.

---

## Summary — expected dispute resolutions in Phase 2

| concept | recommended | reason |
|---|---|---|
| accept-and-flag | INVARIANT | "never reject" property; detection is separate algorithm |
| flag | INVARIANT | concrete expression of accept-and-flag |
| conflict-detected | CONTRACT | platform-bundled shape per ADR-002 Addendum — already settled |
| subject-ref | CONTRACT | field format; subject itself stays PRIMITIVE |
| actor-ref | CONTRACT | drop RESERVED — subagent artifact |
| activity-ref | CONTRACT | drop RESERVED — subagent artifact |
| scope | PRIMITIVE | model/concept; scope-types stay CONFIG |
| pattern | PRIMITIVE | closed registry; parameters stay CONFIG |
| activity | CONFIG | deployer-assembled instance; activity-ref stays CONTRACT |

These are *recommendations for the ADR author, not decisions*. Phase 2 ADR
drafts on each concept must verify the recommendation against the quoted
passages above.

---

## Method notes

- Architecture/ files were deliberately excluded from this harvest — per the
  2026-04-22 correction, they are late-consolidation artifacts whose readings
  may have drifted from the stress-test reasoning.
- Silence on a point is reported as silence (no fabricated reasoning).
- Citations use the form `file §section` or `file:lines`. All passages are
  quoted verbatim from the source.
- When the harvest group subagents found sources in conflict, the passage
  with the **lower-numbered** authority tier wins (root docs > ADRs > archive).
