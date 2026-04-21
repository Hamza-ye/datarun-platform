# ADR-005 Exploration Guide: State Progression & Workflow

> **For what was decided**: read [ADR-005](../adrs/adr-005-state-progression.md).
> **For structural implications**: read [architecture/primitives.md](../architecture/primitives.md) (Trigger Engine, Pattern Registry, Command Validator), [architecture/patterns.md](../architecture/patterns.md) (4 patterns).
> **Below**: how we got there — section-by-section navigation into the raw explorations.

---

## Source Documents

| Doc | Lines | What it covers |
|-----|------:|----------------|
| [19](archive/19-adr5-session1-scoping.md) | 698 | Decision surface, event storm (S04/S08/S11/S07+S14), Pattern Registry emergence |
| [20](archive/20-adr5-session2-stress-test.md) | 559 | 6 questions stress-tested, irreversibility filter |
| [21](archive/21-adr5-session3-part1-structural-coherence.md) | 638 | 9 coherence checks (a–i), final primitives map, gate decision |

---

## doc 19: Scoping & Event Storm — Pattern Registry Emergence

- **§1**: 6 sub-decisions ADR-5 must make — state model, transition authority, offline transitions, pattern framework, auto-resolution, composition rules
- **§2**: Four scenario event storms through the workflow lens:
  - **S04** (Capture with Review): Discovers the review pattern — capture → review_requested → review_completed cycle. The simplest state machine.
  - **S08** (Case Management): Discovers long-running state machines with branching. Cases open, progress, get flagged, resume, close. Multiple parallel workflows on one subject.
  - **S11** (Multi-Step Approval): Discovers ordered step chains. Proves transitions need step metadata, not just status.
  - **S07+S14** (Transfer & Distribution): Discovers paired transitions (send/receive). Proves multi-hop state machines with handoff semantics.
- **§3**: Cross-storm synthesis — 8 positions emerge. The key insight: state machines are NOT arbitrary — they follow a small set of patterns.
- **§4**: Resolving Q2 — `status_changed` becomes the 7th event type. Why it's a separate type from `record_captured`, not a field on it.
- **§5**: **Pattern Registry emergence** — the central discovery. Patterns are parameterized state-machine skeletons: `capture_with_review`, `case_management`, `multi_step_approval`, `transfer_with_acknowledgment`. Deployers select a pattern and bind shapes/roles to it. The platform ships with fixed patterns; deployers don't author state machines.

**Unique value not in ADR**: The event storm acts showing how each pattern was discovered from scenarios (not designed top-down). The Q2 resolution reasoning — why `status_changed` is a type, not a field. The full pattern skeleton specifications before they were compressed into the ADR and [patterns.md](../architecture/patterns.md).

---

## doc 20: Stress Test — 6 Questions + Irreversibility Filter

Six questions stress-tested:

- **§Q3 (Pattern Composition Rules)**: Can two patterns compose on one subject? **Answer**: No — one pattern per activity-subject binding. Multiple activities can target the same subject, but each has its own state machine. Proves composition at the activity level, not pattern level.
- **§Q4 (Flag Cascade Behavior)**: What happens when a flag interrupts a state machine mid-transition? **Answer**: Flags suspend the state machine at the flagged step. Resolution resumes or rolls back. Proves flags and state machines interact through a "suspended" meta-state.
- **§Q5 (context.* Expression Scope)**: What entity data is available in form expressions? **Answer**: `payload.*` (current form), `entity.*` (subject projection), `context.*` (computed at form-open from projection). Proves the 3-scope expression model.
- **§Q6 (Auto-Resolution + State Machine)**: How do auto-resolution policies interact with state machines? **Answer**: Auto-resolution is a trigger (L3b deadline-check) that fires a resolution event. The state machine treats it like any other transition event. Proves auto-resolution doesn't need special state-machine integration.

Irreversibility filter (§5):
- **Envelope**: Zero new fields. ADR-5 adds no envelope changes — state is derived from existing event types.
- **Structural constraints**: Pattern definitions are platform-fixed (the 4 patterns ship with the platform). Deployers parameterize, not author.
- **Strategies**: Step sequencing, auto-resolution timing, flag-suspension behavior — all server-side, evolvable.

**Unique value not in ADR**: The Q3 composition analysis — why pattern-level composition was rejected (leads to combinatorial state explosion). The Q4 flag-suspension mechanism detail. The proof that ADR-5 adds zero envelope fields.

---

## doc 21: Structural Coherence — 9 Checks + Gate Decision

Nine coherence audits against all upstream ADRs:

- **§(a) ADR-1**: Append-only ✓ — transitions are events, not mutations. State machines are projections.
- **§(b) ADR-2**: Accept-and-flag ✓ — flagged events suspend state machines. Detect-before-act ✓ — conflict detection runs before triggers.
- **§(c) ADR-3**: Assignment-based access ✓ — transition authority requires assignment to the activity's scope. Sync scope ✓ — state machine state is part of the subject projection.
- **§(d) ADR-4**: **The critical integration check.** Four-layer gradient ✓ — patterns sit in Layer 4. Type vocabulary ✓ — `status_changed` is 7th type, fixed. Expression language ✓ — `context.*` scope adds entity projection access.
- **§(e)**: Internal coherence — Q1–Q6 compose without contradiction.
- **§(f)**: Primitives composition — Trigger Engine, Pattern Registry, Command Validator emerge as new primitives. Existing primitives (Event Store, Projection Engine, Conflict Detector) gain workflow-aware behaviors.
- **§(g)**: Envelope integrity — zero additions confirmed.
- **§(h)**: Anti-pattern check — all 6 anti-patterns (AP-1 through AP-6) verified as non-triggered.
- **§(i)**: Principle alignment — all 8 principles hold. P7 (simplest stays simple) explicitly verified: S00 is unaffected by workflow machinery.

**Unique value not in ADR**: The full ADR-4 integration check (§d, ~130 lines) proving patterns compose correctly with the four-layer gradient. The anti-pattern verification (§h) proving ADR-5 doesn't introduce Config-as-Code (AP-1) or Vocabulary Creep (AP-2). The final primitives map showing all 11 primitives in their complete form.
