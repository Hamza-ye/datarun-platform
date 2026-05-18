# 00 Specification Source Authority

Status: Draft
Owning boundary: professional-baseline source authority
Primary owner: Architecture Steward

Source basis:

- `../../professional-baseline/02-change-control.md`
- `../../professional-baseline/03-artifact-definitions.md`
- `../../professional-baseline/04-architecture-baseline-v0.md`
- `../../professional-baseline/05-decision-gap-register.md`
- `../../professional-baseline/07-system-boundary-map.md`
- `../../professional-baseline/20-platform-spec-outline.md`

Depends on:

- accepted ADR-001 through ADR-005 architecture baseline
- canonical decision gap register
- architecture responsibility map
- platform-spec outline

Consumed by:

- all platform-spec sections
- section review and acceptance
- engineering design, tickets, and tests after section acceptance

## Purpose

This section defines which documents can create implementation-facing platform specification authority and how draft section material becomes accepted. It keeps source hierarchy, open-decision handling, and change control explicit before any behavior section is accepted.

## Scope

This section owns:

- platform-spec source hierarchy
- section status meanings
- section acceptance requirements
- open-decision citation rule
- change-control trigger routing
- rules for using assessed inputs and candidate section material

## Non-Scope

This section does not own:

- platform runtime behavior
- implementation design, tickets, or tests
- product priority or UX alignment decisions
- closure of any gap in `05-decision-gap-register.md`
- acceptance of any behavior section

## Definitions

| Term | Meaning In This Section | Must Not Mean |
|---|---|---|
| Accepted architecture baseline | ADR-001 through ADR-005 as represented by `04-architecture-baseline-v0.md` | Later ADR prose, implementation state, product alignment, or draft section text |
| Canonical gap register | `05-decision-gap-register.md` | A duplicate list in a section, dashboard, or implementation backlog |
| Architecture responsibility map | `07-system-boundary-map.md`, used for responsibility routing | Runtime module map, team ownership, or service decomposition |
| Platform-spec outline | `20-platform-spec-outline.md` | Accepted platform specification or implementation design |
| Specification section | A reviewed platform-spec unit with one primary owner and explicit gaps | Exploratory draft material or an implementation ticket |
| Accepted specification section | A section approved through review and change-control checks | A file that exists, a prior draft acceptance note, or a registry status alone |
| Assessed input | Classified source material already routed through the baseline/spec path | Independent authority over ADR-001 through ADR-005 |

## Invariants

- `04-architecture-baseline-v0.md` is the accepted architecture baseline.
- `05-decision-gap-register.md` is the single canonical open-gap and open-decision register.
- `07-system-boundary-map.md` routes responsibility; it does not add behavior.
- `20-platform-spec-outline.md` routes section structure and first-section blockers; it is not the platform specification itself.
- ADR-006-R through ADR-009 are assessed inputs only. They do not supersede ADR-001 through ADR-005 automatically.
- Product-alignment material is not source authority unless separately assessed into this baseline/spec path.
- Existing `../sections/` files are candidate section material until reviewed against `05`, `07`, and `20`.
- A section must cite `05` whenever it touches an open gap. A section must not silently close a gap.
- Engineering design, implementation tickets, and tests may depend on accepted specification sections, not draft sections.

## Contracts

### Inputs

- accepted architecture baseline
- canonical gap register
- architecture responsibility map
- platform-spec outline
- classified assessed inputs where cited by the outline or affected gap
- candidate section material under this workspace

### Outputs

- source hierarchy for all platform-spec sections
- acceptance rules for section status
- change-control triggers for claims that alter baseline semantics
- citation rules for open gaps and rejected alternatives

### Boundary Crossings

| Crosses To | Through | Notes |
|---|---|---|
| All sections | source hierarchy and acceptance rule | Behavior sections consume source authority; this section does not define their behavior. |
| `05-decision-gap-register.md` | open-decision citation | Gaps remain canonical in `05`; sections cite and classify their impact. |
| `07-system-boundary-map.md` | responsibility owner requirement | Every behavior section names one primary owner from the map. |
| Change control | baseline-change trigger | Claims that change accepted architecture require formal closure before section acceptance. |
| Engineering design | accepted-section citation | Design work may cite accepted sections only. |

## Allowed Extension Points

- New sections may be added if routed through `20` or a formally accepted outline update.
- Section status may change only after review records the source basis, gap handling, rejected-path check, and change-control result.
- Candidate section text may be reused after reconciliation with `04`, `05`, `07`, and `20`.

## Forbidden Couplings

- Do not treat draft sections as implementation authority.
- Do not treat the section registry as architecture authority.
- Do not use product alignment, implementation convenience, or prior acceptance notes to bypass `05`.
- Do not create a second open-gap register inside a section.
- Do not close a gap through wording in a behavior section without formal closure.
- Do not use assessed ADR-006-R through ADR-009 material as automatic baseline authority.

## Open Gaps

| Gap | Owner / Route | Reopen Trigger |
|---|---|---|
| None for source hierarchy | `05-decision-gap-register.md` remains canonical | A future source wants to alter baseline semantics or section acceptance rules. |

## Rejected Paths

- Building directly from ADR prose, exploration prose, product-alignment prose, or implementation plans.
- Treating existing candidate section files as accepted specification.
- Treating the old section workspace as a governance layer.
- Treating implementation readiness as implied by the architecture baseline alone.

## Implementation Implications

- Implementation planning must wait for accepted platform-spec coverage for the behavior it needs.
- Even append-to-log work needs accepted source authority, vocabulary, event-log, envelope/schema, open-decision, and rejected-alternative coverage.
- If implementation needs behavior that remains a `05` gap, the gap must be closed, constrained, or explicitly held back before code depends on it.

## Review Checklist

- [ ] Source basis is accepted and cited.
- [ ] Scope and non-scope are explicit.
- [ ] No platform behavior is introduced.
- [ ] `05` remains the canonical gap register.
- [ ] `07` remains routing only.
- [ ] `20` remains outline only.
- [ ] Candidate sections are not treated as accepted platform specification.
