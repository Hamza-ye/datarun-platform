# Datarun Charter — Currently Decided State

> **Start every session here.** This is the single source of truth for "what
> is settled." Generated from `docs/convergence/concept-ledger.md`. Every
> claim cites the ADR that decided it. The drift gate
> (`scripts/check-convergence.sh`) fails any claim missing a cite or pointing
> to a superseded ADR.
>
> **Session-start reads (in order)**: this file → [convergence/concept-ledger.md](convergence/concept-ledger.md) → `docs/ships/ship-N.md` (current Ship spec, if one is in flight). `CLAUDE.md` is consulted for the codebase map only; its phase tracking is historical.
>
> **Lifespan**: permanent. Convergence Phase 2 closed (2026-04-23). Forward
> changes arrive via Ship retros that supersede ADRs per
> [convergence/supersede-rules.md](convergence/supersede-rules.md); charter is
> regenerated, never hand-edited.

## Status

**Convergence phase**: 2 — **CLOSED** (2026-04-23). Protocol dormant; re-enters only if a Ship retro triggers a cross-cutting ADR round.
**Forward cadence**: scenario-driven Ships (see *Rhythm* section).
**Next action**: draft `docs/ships/ship-1.md` (Cluster A+B — offline structured capture under assigned scope).
**Last ADR landed**: ADR-009 (platform-fixed mechanism vs. deployer-configured instance).
**Ledger state**: 251 STABLE / 7 DEFERRED / 11 OBSOLETE / 0 OPEN / 269 total. Drift gate: PASS.

---

## Invariants

> One line per invariant. Each cites the ADR that established it.

- **Accept-and-flag.** A validly-structured event is never rejected for state-based reasons; state anomalies surface as flag events, never as rejections or modifications. ([ADR-006 §S1](adrs/adr-006-flag-semantics.md))
- **Flag as canonical anomaly surface (event stream).** Flags are the canonical representation of state anomalies on the event stream; no parallel anomaly-record surface exists or is permitted on the event stream. Scoped to representation and emission — does not govern non-event-stream surfaces (telemetry, metrics, operational logs). ([ADR-006 §S2](adrs/adr-006-flag-semantics.md))
- **Server-side flag creation (default).** Flags are created server-side during sync processing; device-side creation is additively evolvable. ([ADR-006 §S4](adrs/adr-006-flag-semantics.md))
- **Envelope type vocabulary is closed at six values.** Allowed: `capture`, `review`, `alert`, `task_created`, `task_completed`, `assignment_changed`. Extension is architecture-grade. `type` answers *which pipeline*; `shape_ref` answers *what fact*; `actor_ref` answers *who authored*. ([ADR-007 §S1](adrs/adr-007-envelope-type-closure.md); first-decision cite [ADR-004 §S3](adrs/adr-004-configuration-boundary.md))
- **Platform-fixed mechanism vs. deployer-configured instance (duality rule).** When a platform concept exposes both a closed mechanism (set, grammar, protocol, or typed interface owned by the platform) and a parameterized instance surface (named and authored by deployers), the two are classified in separate ledger rows: the mechanism PRIMITIVE, the instance CONFIG. Conflating them is a classification error. ([ADR-009 §S1](adrs/adr-009-platform-fixed-vs-deployer-configured.md))

---

## Primitives

> Load-bearing concepts the platform is built on. Each row points to its
> contract definition and owning ADR.

| primitive | contract | settled-by |
|---|---|---|
| `scope` (platform-fixed authorization mechanism; scope-type registry closed at 3 values) | authorization mechanism | [ADR-009 §S2](adrs/adr-009-platform-fixed-vs-deployer-configured.md); first-decision cites [ADR-003 §S7](adrs/adr-003-authorization-sync.md), [ADR-004 §S7](adrs/adr-004-configuration-boundary.md) |
| `pattern` (platform-fixed workflow skeleton registry; deployer-referenced, not deployer-authored) | pattern registry | [ADR-009 §S3](adrs/adr-009-platform-fixed-vs-deployer-configured.md); first-decision cite [ADR-005 §S5](adrs/adr-005-state-progression.md) |

---

## Contracts

> Shared formats and interfaces.

| contract | location | settled-by |
|---|---|---|
| `subject_ref` (envelope field; typed UUID with 4-value type enum, `process` reserved) | envelope schema | [ADR-008 §S1](adrs/adr-008-envelope-reference-fields.md) |
| `actor_ref` (envelope field; human UUID or `system:{source_type}/{source_id}`) | envelope schema | [ADR-008 §S2](adrs/adr-008-envelope-reference-fields.md) |
| `activity_ref` (envelope field; optional deployer-chosen identifier `[a-z][a-z0-9_]*`) | envelope schema | [ADR-008 §S3](adrs/adr-008-envelope-reference-fields.md) |
| `conflict_detected/v1` (platform-bundled shape; integrity flag) | `contracts/shapes/` | [ADR-007 §S2](adrs/adr-007-envelope-type-closure.md) |
| `conflict_resolved/v1` (platform-bundled shape; spans `type=review` human, `type=capture` system) | `contracts/shapes/` | [ADR-007 §S2](adrs/adr-007-envelope-type-closure.md) |
| `subjects_merged/v1` (platform-bundled shape; `type=capture`) | `contracts/shapes/` | [ADR-007 §S2](adrs/adr-007-envelope-type-closure.md) |
| `subject_split/v1` (platform-bundled shape; `type=capture`) | `contracts/shapes/` | [ADR-007 §S2](adrs/adr-007-envelope-type-closure.md) |

---

## Cross-cutting rules

> Rules that span multiple primitives. Absorbed from the former
> `docs/architecture/cross-cutting.md` (deleted 2026-04-23). Content here is
> cite-backed; implementation detail that was in that file is either covered
> by an ADR or deferred until the Ship that needs it.

### Envelope (11 fields, closed)

The envelope is the universal contract between every component. Every reader reads it; only the write path (Event Store) writes it.

| Field | Type | Presence | Settled-by |
|---|---|---|---|
| `id` | UUID | Mandatory | [ADR-001 §S3, §S5](adrs/adr-001-offline-data-model.md) |
| `type` | enum(6) | Mandatory | [ADR-007 §S1](adrs/adr-007-envelope-type-closure.md); first-decision [ADR-004 §S3](adrs/adr-004-configuration-boundary.md) |
| `shape_ref` | `{name}/v{N}` | Mandatory | [ADR-004 §S1](adrs/adr-004-configuration-boundary.md) |
| `activity_ref` | `[a-z][a-z0-9_]*` | Optional | [ADR-008 §S3](adrs/adr-008-envelope-reference-fields.md); first-decision [ADR-004 §S2](adrs/adr-004-configuration-boundary.md) |
| `subject_ref` | `{type, id}` (4-value type enum, `process` reserved) | Mandatory | [ADR-008 §S1](adrs/adr-008-envelope-reference-fields.md) |
| `actor_ref` | human UUID or `system:{source_type}/{source_id}` | Mandatory | [ADR-008 §S2](adrs/adr-008-envelope-reference-fields.md) |
| `device_id` | UUID | Mandatory | [ADR-002 §S5](adrs/adr-002-identity-conflict.md) |
| `device_seq` | integer | Mandatory | [ADR-002 §S1](adrs/adr-002-identity-conflict.md) |
| `sync_watermark` | version | Mandatory | [ADR-002 §S1](adrs/adr-002-identity-conflict.md) |
| `timestamp` | datetime (advisory only) | Mandatory | [ADR-002 §S3](adrs/adr-002-identity-conflict.md) |
| `payload` | object (validated against `shape_ref`) | Mandatory | [ADR-001 §S5](adrs/adr-001-offline-data-model.md) |

Envelope extension is architecture-grade. The six envelope `type` values (`capture`, `review`, `alert`, `task_created`, `task_completed`, `assignment_changed`) are closed per [ADR-007 §S1](adrs/adr-007-envelope-type-closure.md). `type` answers *which pipeline*; `shape_ref` answers *what fact*; `actor_ref` answers *who authored*.

### Accept-and-flag lifecycle

Anomalies surface as flag events alongside the accepted event — never as rejections or modifications. The pipeline is a closed loop: Event Store → Conflict Detector (detect) → Projection Engine (exclude flagged events from state derivation) → Trigger Engine (auto-resolution or manual path) → Event Store (resolution persisted) → Projection Engine (state re-derived). Settled by [ADR-006 §S1, §S2, §S4](adrs/adr-006-flag-semantics.md).

### Detect-before-act

Flagged events do not (a) trigger policies, (b) advance state machines, or (c) authorize downstream work. The Conflict Detector is the sole provider of this guarantee. Settled by [ADR-006 §S1](adrs/adr-006-flag-semantics.md); authorization-blocking vs. informational mode is deployer-configurable per [ADR-003 §S7](adrs/adr-003-authorization-sync.md).

### Four-layer configuration gradient

Deployer-authored configuration maps to the platform across four layers, ordered by side effects (not complexity):

| Layer | Name | Side effects |
|---|---|---|
| L0 | Assembly | None — structural wiring (pattern selection, scope composition, flag severity, sensitivity) |
| L1 | Shape | None — schema and data mapping |
| L2 | Logic | Form-scoped only — no persistent records (show/hide, defaults, warnings) |
| L3 | Policy | System-scoped — creates persistent events (L3a event-reaction, L3b deadline-check, auto-resolution) |

Beyond L3 is **platform evolution** (new types, new patterns, new `context.*` properties) — not a workaround. The boundary is visible, not discovered by hitting walls. Settled by [ADR-004 §S9](adrs/adr-004-configuration-boundary.md); the L3 ceiling is where the platform's fitness will be stressed first (viability assessment Risk R1, R3 — revisited at the Ship that first touches the reactive layer).

### Sync contract

Sync is not a primitive — it is eight guarantees decomposed from Event Store, Scope Resolver, Conflict Detector, and Config Packager. Events are the sync unit; sync is idempotent, append-only, order-independent, and scope-filtered. Conflict detection precedes policy execution on synced events. Configuration is delivered atomically; at most 2 versions coexist on-device. Merge/split/resolution events sync like any other event, filtered by scope. Settled by [ADR-001 §S4](adrs/adr-001-offline-data-model.md), [ADR-002 §S1](adrs/adr-002-identity-conflict.md), [ADR-003 §S2](adrs/adr-003-authorization-sync.md), [ADR-004 §S6](adrs/adr-004-configuration-boundary.md), [ADR-006 §S1](adrs/adr-006-flag-semantics.md).

### Flag catalog (9 categories)

| # | Category | Source | Resolvability |
|---|---|---|---|
| 1 | `identity_conflict` | ADR-002 | `manual_only` |
| 2 | `stale_reference` | ADR-002 | `auto_eligible` |
| 3 | `concurrent_state_change` | ADR-002 | `manual_only` |
| 4 | `scope_violation` | ADR-003 | `manual_only` |
| 5 | `role_stale` | ADR-003 | `manual_only` |
| 6 | `temporal_authority_expired` | ADR-003 | `auto_eligible` |
| 7 | `domain_uniqueness_violation` | ADR-004 | `manual_only` |
| 8 | `transition_violation` | ADR-005 | `auto_eligible` |
| 9 | *(reserved — catalog is append-only)* | — | — |

Resolvability is platform-defined per [ADR-006 §S4](adrs/adr-006-flag-semantics.md). Severity (blocking vs. informational) is deployer-configurable per [ADR-004 §S14](adrs/adr-004-configuration-boundary.md) — but the precise severity/uniqueness policy surface is an open item (ledger: `sensitive-subject-classification` DEFERRED; will be pinned when the Ship that needs it defines the requirement).

### Aggregation interface

Cross-subject aggregation (dashboards, campaign progress, reporting) is classified as **outside the architecture**. The architecture declares interface points only: aggregation reads from projections (never directly from events), inherits flagged-event-exclusion, and is write-path-forbidden. What aggregation looks like to deployers is not constrained by any ADR. Multiple valid models are permitted.

---

## Forbidden patterns

> Carries forward from ADR-007/008/009 bodies. Each cite is the ADR whose
> decision makes the pattern forbidden. The pre-convergence list in
> `CLAUDE.md` (F1–F15) is historical.

### F-A series — envelope `type` discipline (ADR-007)

- **F-A1.** Never add a value to the envelope `type` enum. Add a new shape instead. ([ADR-007 §S1](adrs/adr-007-envelope-type-closure.md))
- **F-A2.** Never filter code on `type == "<specific_string>"` for domain discrimination. Filter on `shape_ref`. ([ADR-007 §S3](adrs/adr-007-envelope-type-closure.md))
- **F-A3.** Never use envelope `type` to encode authorship. Authorship is in `actor_ref`. The same shape can ride different types depending on authoring actor (human → `review`, system → `capture`). ([ADR-007 §S2, §S3](adrs/adr-007-envelope-type-closure.md))
- **F-A4.** Never key code on a system event's `type`. If you are about to write `if (type in ["conflict_detected", "conflict_resolved", "subjects_merged", "subject_split"])`, stop — those are shape names. Use `shape_ref`. ([ADR-007 §S2, §S3](adrs/adr-007-envelope-type-closure.md))
- **F-A5.** Never split a shape across envelope types without recording the authorship rule in the same table form as [ADR-007 §S2](adrs/adr-007-envelope-type-closure.md). Leaving authorship implicit is how the Phase 1/2 drift started.

### F-B series — envelope reference fields (ADR-008)

- **F-B1.** Never classify a `*_ref` field and its referent in the same ledger row. The ref is an envelope contract; the referent is a domain object. ([ADR-008 §S1](adrs/adr-008-envelope-reference-fields.md))
- **F-B2.** Never extend the `subject_ref.type` enum without an ADR. The four-value enum (`subject`, `actor`, `process`, `assignment`) is architecture-grade. ([ADR-008 §S1](adrs/adr-008-envelope-reference-fields.md))
- **F-B3.** Never treat `actor_ref` `source_type` as a closed enum. It is an evolvable platform vocabulary. ([ADR-008 §S2](adrs/adr-008-envelope-reference-fields.md))
- **F-B4.** Never filter on envelope `type` to distinguish human vs. system authorship. The discriminator is `actor_ref.startswith("system:")`. ([ADR-008 §S2](adrs/adr-008-envelope-reference-fields.md))

### F-C series — classification discipline (ADR-009)

- **F-C1.** Never classify a mechanism as CONFIG or an instance as PRIMITIVE. If a ledger row's classification appears to require one of these collapses, the concept is exhibiting the duality and must be split into two rows. ([ADR-009 §S1](adrs/adr-009-platform-fixed-vs-deployer-configured.md))

---

## Rhythm

> How forward work happens now that Phase 2 is closed. This section governs
> AI-assisted shipping and is the primary anti-drift mechanism.

**Unit of work**: the **Ship**. A Ship delivers one or more scenarios from [docs/scenarios/](scenarios/). Ships are scenario-driven, not developer-milestone-driven.

**Per-Ship loop**:

1. **Ship spec** (`docs/ships/ship-N.md`) — written before any code. Declares: scenarios delivered, ADRs exercised (§S cites), ADRs at risk of supersession with the specific position that would break and under what observation, ledger concepts touched with expected status changes, out-of-scope assertion, retro criteria.
2. **Slice** — thinnest vertical that implements the scenarios end-to-end. No horizontal work.
3. **Build** — every commit cites the scenario ID advanced (`feat(ship-N): S0X — …`). Commits without a scenario cite are red flags visible in `git log`.
4. **Scenario acceptance** — scripted walkthrough that follows the scenario prose. Walkthrough is the acceptance criterion, not unit tests alone.
5. **Retro + convergence pass** — produces, in order: any ADR position changed → new ADR (supersede per [convergence/supersede-rules.md](convergence/supersede-rules.md)); ledger rows updated in the same commit; charter **regenerated** (never hand-edited); flagged-positions entries closed or created; Ship tag (`ship-N`).

**Hard rule**: no code work begins until the Ship spec is written and all cited ADRs are Decided. If a scenario requires a position the ADRs do not cover, that triggers an ADR round — spec waits.

**Session-survival mechanism** (how this plan outlives any single session):

- **Drift gate is mechanical** — `scripts/check-convergence.sh` refuses commits where charter claims lack Decided-ADR cites. An agent cannot silently introduce stale content.
- **Charter is the session-start read** — this header names the read order. Repo memory ([/memories/repo/datarun-architecture.md](../)) reinforces it.
- **CLAUDE.md is codebase-map-only** — phase tracking, test counts, and status fields in CLAUDE.md are historical. Current status lives here and in `docs/ships/README.md`.
- **Ship spec is the single in-flight work doc** — a mid-session agent reads one file to know full scope of current work.
- **Retro regenerates, never edits** — the charter is never hand-patched at retro; it is rebuilt from ADR + ledger cites. Any manual charter edit bypasses the gate and is a red flag.

**Out-of-scope for Ships (absorbed as properties or deferred)**:

- S19 (offline capture and sync) — a constraint every Ship inherits, not a Ship itself.
- S13 (cross-flow linking) — lands opportunistically in the Ship that first has two activities coexisting.
- S05, S20, S21 — composite scenarios; serve as acceptance tests for the Ships that contain their parts, not as independent Ships.
- S15, S16, S18 — Phase-2 per [viability-assessment.md](viability-assessment.md); deliberately deferred to avoid distorting initial architecture.

---

## Pointers

- Concept ledger (classification source of truth): [convergence/concept-ledger.md](convergence/concept-ledger.md)
- ADRs (9, all Decided): [adrs/](adrs/)
- Scenarios: [scenarios/](scenarios/)
- Deferred / flagged items: [flagged-positions.md](flagged-positions.md)
- Convergence protocol (dormant): [convergence/protocol.md](convergence/protocol.md)
- Supersede rules: [convergence/supersede-rules.md](convergence/supersede-rules.md)
- Drift gate: [scripts/check-convergence.sh](../scripts/check-convergence.sh)
- Codebase map (stale tracking, keep for paths only): [CLAUDE.md](../CLAUDE.md)
- Absorbed / deleted (2026-04-23): former `docs/architecture/{README,contracts,boundary,cross-cutting}.md`. `primitives.md` retained pending Ship-2 rewrite (contradicts ADR-009 §S1; `patterns.md` retained pending Ship-3 relocation to `specification/`).
