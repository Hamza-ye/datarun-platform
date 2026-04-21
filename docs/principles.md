# Working Principles

> These are beliefs that guide all decisions. They are explicitly provisional — each is a hypothesis until validated through constraint decisions.

This document captures what we believe matters, derived from the Vision, Constraints, and Behavioral Patterns. Each principle will be tested as we make architectural decisions. Principles that survive decision-making become validated; those that don't get revised or retired.

---

## Status

| Principle | Status | Last tested |
|-----------|--------|-------------|
| P1: Offline is the default | Confirmed | ADR-001, ADR-002, ADR-005 |
| P2: Configuration has boundaries | Confirmed | ADR-004, ADR-005 |
| P3: Records are append-only | Confirmed | ADR-001, ADR-002, ADR-005 |
| P4: Patterns compose; exceptions don't | Confirmed | ADR-001, ADR-002, ADR-003, ADR-005 |
| P5: Conflict is surfaced, not silently resolved | Confirmed | ADR-001, ADR-002, ADR-005 |
| P6: Authority is always contextual and auditable | Confirmed | ADR-003, ADR-005 |
| P7: The simplest scenario stays simple | Confirmed | ADR-001, ADR-002, ADR-004, ADR-005 |

---

## The Principles

### P1: Offline is the default, not the exception
*Status: Confirmed by ADR-001, ADR-002, ADR-005*

The platform assumes disconnection. Every behavior that works online must work offline. Connectivity is for synchronization, not operation. If a design requires connectivity to function, it's wrong.

**Derived from**: V1 (Works without connectivity), S19, Constraints doc (field-level connectivity)

**Confirmed by**: ADR-001 (events are created locally, synced when connectivity returns — no operation requires a network roundtrip), ADR-002 (causal ordering uses device-local sequence numbers; merge/split are online-only but restricted to coordinator-level operations with reliable connectivity — field workers never need connectivity), ADR-005 (state derived from local projection; `context.*` pre-resolved from local data; Command Validator advisory — never blocks offline capture; transition violations flagged, never rejected).

---

### P2: Configuration has boundaries
*Status: Confirmed by ADR-004, ADR-005*

"Set up, not built" does not mean "infinitely configurable." The platform provides a fixed set of powerful primitives. Configuration combines and parameterizes those primitives. When a need can't be met by configuration, it's either out of scope or requires platform evolution — not a workaround.

**Derived from**: V2 (Set up, not built), T2 tension (configuration vs. expressiveness), R1 risk (configuration boundary collapse)

**Confirmed by**: ADR-004 established a four-layer configuration gradient (L0 Assembly → L1 Shape → L2 Logic → L3 Policy) with an explicit boundary at L3→code. Hard complexity budgets prevent configuration from becoming programming. The expressiveness ceiling is visible and signposted, not discovered by hitting walls. S00 requires exactly 2 configuration artifacts (one shape, one activity) — V2 validated. ADR-005 extended: patterns are platform-fixed (deployers select and parameterize, not author state machines), `context.*` properties are platform-fixed (deployers reference, not define), auto-resolution targets platform-classified flag types only.

---

### P3: Records are append-only; history is sacred
*Status: Confirmed by ADR-001, ADR-002, ADR-005*

Nothing is deleted or overwritten. Corrections append; they don't replace. The full history — who did what, when, under what shape, under what authority — is always recoverable. Convenience never justifies losing provenance.

**Derived from**: V3 (Trustworthy records), S00 edge cases (corrections), S06 (schema evolution)

**Confirmed by**: ADR-001 (all writes are append-only — structural, not policy), ADR-002 (merge creates alias events, never rewrites references; split freezes history; SubjectsUnmerged rejected as structurally unsound; events are never rejected for state staleness), ADR-005 (invalid transitions flagged, never rejected; derived state exists only in projections, never stored in events; auto-resolution produces standard `ConflictResolved` events — auditable records, not silent corrections).

---

### P4: Patterns compose; the platform evolves through composition, not modification
*Status: Confirmed by ADR-001, ADR-002, ADR-003, ADR-005*

The platform supports a small number of behavioral patterns. Real-world complexity is handled by composing patterns, not by adding exceptions. If a scenario requires a one-off behavior that doesn't fit any pattern, the answer is "no" — not a special case.

New behaviors emerge from combining existing primitives. New primitives can be added to the vocabulary without changing existing compositions — existing compositions continue to work because they only reference primitives they know about. This is how the platform grows without breaking: evolution happens at the edges, not at the core.

**Derived from**: V4 (One system), V5 (Grows without breaking), Viability assessment Section 2 (primitives compose across scenarios)

**Confirmed by**: ADR-003 introduced no new structural mechanisms — authorization reuses the accept-and-flag model from ADR-002 (S9), extends detect-before-act to all flag types (S7), and derives authority from the existing assignment timeline rather than adding a parallel authority structure. The pattern held: composition of existing primitives (events, projections, flags) rather than new mechanisms. ADR-005 formalized five composition rules governing how workflow patterns interact (subject-level vs. event-level, cross-activity linking via `activity_ref`, shape-to-pattern uniqueness). Validated against three multi-pattern scenarios: case management + approval, campaign + distribution, entity lifecycle + review.

---

### P5: Conflict is surfaced, not silently resolved
*Status: Confirmed by ADR-001, ADR-002, ADR-005*

When independent work creates conflicts (offline edits, concurrent decisions, schema mismatches), the platform surfaces them for human resolution. Automatic conflict resolution is only acceptable when the resolution is provably correct and reversible.

**Derived from**: V3 (Trustworthy records), S19 edge cases (offline conflicts), T4 tension (trustworthy records vs. offline correction)

**Confirmed by**: ADR-001 (concurrent events on the same subject are structurally visible in the event stream; resolution produces its own event), ADR-002 (accept-and-flag mechanism — every anomaly produces a ConflictDetected event with a designated single-writer resolver; concurrent state changes always require human resolution; auto-resolution is a deployable strategy, not a structural default), ADR-005 (transition violations are a new flag category — surfaced, not silently swallowed; source-chain traversal surfaces upstream flags on downstream events; auto-resolution produces traceable events with system actor identity and platform-enforced resolvability classification prevents auto-resolution of security-relevant flags).

---

### P6: Authority is always contextual and auditable
*Status: Confirmed by ADR-003, ADR-005*

What someone can see and do depends on who they are, what role they hold, what scope they're operating in, and what moment in time. Every action records not just *who* but *as what* and *where*. Authority rules must be enforceable offline.

**Derived from**: Access Control doc, S04/S11 (review authority varies by step), S19 (offline authority enforcement)

**Confirmed by**: ADR-003 established assignment-based access control (S1) where authority derives from temporal actor→scope bindings. Sync scope equals access scope (S2) — offline enforcement is structural, not advisory. Authority-as-projection (S3) means the full audit chain (event → actor → assignments at that moment → scope → authorization decision → flag if anomalous) is always reconstructible. The alias-respects-original-scope rule (S4) ensures authority context is never retroactively altered by identity merges. ADR-005 extended: pattern role declarations are explicit in activity configuration; auto-resolution events carry `system:auto_resolution/{policy_id}` actor identity — every state transition, flag, and resolution is attributable.

---

### P7: The simplest scenario stays simple
*Status: Confirmed by ADR-001, ADR-002, ADR-004, ADR-005*

S00 (basic structured capture) is the litmus test. If the platform makes S00 complicated — requiring users to understand concepts they don't need — the abstraction is wrong. Complexity is opt-in, not mandatory.

**Derived from**: V5 (Grows without breaking), Viability assessment (S00 as simplicity baseline)

**Confirmed by**: ADR-001 (S00 is one event with a data payload — no projections needed for standalone captures), ADR-002 (S00 adds three envelope fields — device_id, device_sequence, sync_watermark — one aggregate validates the write, one sync step, no alias resolution, no flags, no conflict handling for the happy path), ADR-004 (S00 requires 2 configuration artifacts — one shape definition and one activity definition, both L0/L1 — no logic rules, no triggers, no code), ADR-005 (S00 unchanged — no patterns, no state machines, no composition rules, no flags, no auto-resolution; still exactly 2 configuration artifacts; the entire workflow apparatus adds zero weight to scenarios that don't use it).

---

## How Principles Get Tested

Each constraint decision will:

1. **Reference** which principles guided the decision
2. **Note** whether the decision **confirms**, **refines**, or **challenges** each referenced principle
3. **Trigger revision** if a principle is challenged — before proceeding to the next decision

All seven principles have been confirmed through five ADRs without requiring revision. ADR-005 completed the confirmation cycle — every principle has been tested by at least two ADRs, and all survived without refinement. The principles are now validated working constraints, not provisional hypotheses.

---

## Revision History

| Date | Change |
| --- | --- |
| 2026-04-13 | P2 confirmed by ADR-004 |
| 2026-04-12 | P4 confirmed by ADR-003 (composition, no new mechanisms). P6 confirmed by ADR-003 (assignment-based, audit chain, offline enforcement). |
| 2026-04-11 | P4 refined: renamed to "Patterns compose; the platform evolves through composition, not modification." Added explicit statement that new primitives don't change existing compositions. Captures evolvability intent without adding a separate extensibility principle. |
| 2026-04-10 | P1, P3, P5, P7 updated to Confirmed by ADR-001 and ADR-002. Confirmation evidence added to each principle section. |
| 2026-04-08 | Initial working principles derived from Vision, Constraints, and Patterns |