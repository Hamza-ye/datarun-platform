# 90 Open Decisions And Gap Register Citations

Status: Draft
Owning boundary: professional-baseline source authority
Primary owner: Architecture Steward

Source basis:

- `../../professional-baseline/05-decision-gap-register.md`
- `../../professional-baseline/07-system-boundary-map.md`
- `../../professional-baseline/20-platform-spec-outline.md`

Depends on:

- `00-specification-source-authority.md`

Consumed by:

- all platform-spec sections
- section review and acceptance
- engineering design only after affected sections are accepted

## Purpose

This section gives platform-spec sections a stable way to cite unresolved decisions without duplicating or replacing `05-decision-gap-register.md`. Its job is visibility and routing, not closure.

## Scope

This section owns:

- citation rules for `05` gaps
- first-section blocker and constraint classification
- gap handling categories used by platform-spec sections
- reminders that sections must not silently close gaps

## Non-Scope

This section does not own:

- canonical gap ownership
- closure of any gap
- implementation sequencing
- product prioritization
- new architecture decisions

## Definitions

| Term | Meaning In This Section | Must Not Mean |
|---|---|---|
| Blocker | A gap that must close or be explicitly held back before the affected claim can be accepted | Reason to silently draft around the gap |
| Constraint | A gap that allows drafting but requires limits, citations, and forbidden overreads | Accepted final behavior |
| Explicit deferral | A scoped statement that the section does not cover the gap | Rejection of the gap |
| Implementation/tooling item | Detail needed before engineering can build safely | Architecture closure by implementation |
| Operational policy item | Deployment or operations decision under accepted mechanisms | Platform mechanism change |
| Product validation item | Product pressure requiring separate assessment | Baseline authority |

## Invariants

- `05-decision-gap-register.md` is the canonical open-gap and open-decision register.
- This section may cite, classify, and route `05` gaps for platform-spec use, but it must not replace `05`.
- Every platform-spec section must cite `05` when it touches an open gap.
- A section may proceed around a gap only by naming the constraint, blocker, or deferral explicitly.
- Implementation tickets/tests must not treat a blocker or deferred gap as settled behavior.

## Contracts

### Inputs

- canonical gaps, owners, closure paths, and reopen triggers from `05`
- responsibility routing from `07`
- first-section blocker assessment from `20`

### Outputs

- citable gap-handling categories
- first-section blocker/constraint map
- review prompts for affected sections

### Boundary Crossings

| Crosses To | Through | Notes |
|---|---|---|
| All sections | open-gap tables and citations | Sections cite only the gaps they touch. |
| `05-decision-gap-register.md` | canonical ownership and closure path | `05` remains canonical. |
| Engineering design | accepted-section gap status | Design may proceed only where gaps are closed, constrained, or explicitly held back. |

## First-Section Gap Citations

| Gap From `05` | Applies To | Required Handling |
|---|---|---|
| Envelope type, shape ref, references, and parametrization boundary | `01`, `03`, `91` | Constraint. Preserve axis separation and do not add fields, type values, actor subclasses, or product classes. |
| Event schema and versioning tooling | `03` | Blocker for implementation-ready append; constraint for conceptual envelope drafting. |
| Final reference serialization and active emission sites | `03` | Blocker for implementation-ready append if canonical reference names, placement, cardinality, or emission sites are needed. |
| Process reference and process lifecycle semantics | `01`, `03` | Explicit deferral unless process lifecycle or active process-reference emission is included. |
| Structured import/export compatibility | `02`, `03`, `91` | Explicit deferral unless external exchange is included. |
| Projection performance and caching | `02` | Constraint. Do not specify cache/rebuild strategy as architecture. |
| Low-end device scale and offline performance | `02` | Constraint. Do not weaken event-log truth for performance pressure. |
| Retention and archival | `02`, `91` | Constraint; blocker only if deletion, redaction, archive policy, or canonical-history mutation is specified. |
| Operational actor vocabulary and operation-class routing | `01`, `91` | Constraint. Role labels remain product/deployer vocabulary unless formally changed. |
| Exact Pattern Registry inventory and formal schema | `01` | Explicit deferral. `01` may define `pattern` only as a term. |
| General flag semantics and domain conflict automation outside workflow | `91` | Hold back for later flag/conflict sections. |
| Alias-cycle enforcement and resolution semantics | `91` | Hold back for later identity/flag sections. |
| Subject-based scope, auditor access, shared-device actor scope, temporary authority, and authorization visibility details | `03`, `91` | Hold back unless actor/session/reference behavior is over-defined. |

## Allowed Extension Points

- Add citations when a new draft section touches a `05` gap.
- Split a citation if one gap has separate blocker, constraint, policy, and tooling effects.
- Move a gap out of this section only after `05` records formal closure or accepted deferral.

## Forbidden Couplings

- Do not copy `05` into this section as a second register.
- Do not close a gap because a section found convenient wording.
- Do not turn implementation tooling needs into architecture decisions.
- Do not treat product validation pressure as baseline authority.

## Open Gaps

| Gap | Owner / Route | Reopen Trigger |
|---|---|---|
| All listed gaps | `05-decision-gap-register.md` | Any affected section needs to accept behavior touching the gap. |

## Rejected Paths

- Hiding unresolved decisions in prose.
- Treating "out of first slice" as rejected.
- Treating "listed here" as permission to implement.

## Implementation Implications

- Engineering work must cite accepted sections and their gap handling.
- If implementation needs a blocker to be resolved, work routes to the closure path in `05`.
- Append-to-log implementation is blocked until `03` has enough accepted schema/version/reference detail or an explicit first-slice hold-back.

## Review Checklist

- [ ] Every cited gap exists in `05`.
- [ ] No gap ownership is duplicated here.
- [ ] Blockers, constraints, deferrals, tooling items, policy items, and product validation items are distinguishable.
- [ ] First-section implementation-readiness blockers are visible.
