# Spec Governance And Source Authority

Status: Draft
Owning boundary: Cross-boundary process
Primary owner: Architecture Steward

Source basis:

- `../atom-registry.yml`
- `../../professional-baseline/02-change-control.md`
- `../../professional-baseline/03-artifact-definitions.md`
- `../../professional-baseline/04-architecture-baseline-v0.md`
- `../../professional-baseline/05-decision-gap-register.md`
- `../../professional-baseline/07-system-boundary-map.md`
- `../../pre-operations/01-decision-board-operating-model.md`
- `../../pre-operations/04-accepted-pre-atomization-decisions.md`
- `../process/01-atomization-operating-plan.md`

Depends on:

- accepted ADR-001 through ADR-005 baseline
- accepted pre-atomization decisions
- platform-spec atomization operating plan

Consumed by:

- every platform specification atom
- future implementation designs that map to platform-spec atoms
- change-control reviews

## Purpose

This atom defines how platform specification atoms become implementation-facing authority. It protects the accepted baseline from drift by making source hierarchy, document status, review outcomes, and change-control triggers explicit.

## Scope

This atom owns:

- source hierarchy for platform-spec atomization
- registry use and update rules
- atom status meanings
- acceptance and review process
- change-control trigger routing
- commit role trace convention
- rules for using evidence, product alignment, and implementation pressure

## Non-Scope

This atom does not own:

- event storage behavior
- event-envelope shape or field semantics
- identity, assignment, sync, workflow, conflict, reporting, or local lifecycle behavior
- product priority decisions
- final implementation design
- closure of any open architecture, platform-spec, operational policy, or implementation gap

## Definitions

| Term | Meaning In This Atom | Must Not Mean |
|---|---|---|
| Accepted baseline | ADR-001 through ADR-005 closure as represented by the professional baseline documents | All ADR prose, later ADR claims, or implementation plans |
| Spec atom | A small implementation-facing specification unit with one owner, one boundary, contracts, forbidden couplings, and open gaps | A product feature brief or implementation module |
| Atom registry | A compact machine-readable index of atom status, owner, boundary, batch, dependencies, and blocking relationships | Architecture authority |
| Evidence archive | Source-specific extraction and lineage material used to verify disputes | Direct implementation authority |
| Hold-back | A known unresolved area that must not be accidentally decided by atomization | A forgotten backlog item or implicit approval |
| Change control | The required process for claims that alter or challenge the accepted baseline | Informal editing or convenient reinterpretation |

## Invariants

- ADR-001 through ADR-005 remain the accepted platform baseline until explicitly changed through change control.
- Later ADRs, product artifacts, implementation pressure, and AI-generated drafts do not supersede the accepted baseline automatically.
- Every platform-spec atom must identify one primary owning boundary or explicitly declare itself cross-boundary process/control material.
- Every unresolved gap must remain visible until accepted, deferred with a trigger, rejected, or formally changed.
- No spec atom may silently add envelope fields, type values, authority sources, actor subclasses, canonical projection state, or deployer-authored platform logic.
- The atom registry is a lookup layer. Atom files remain canonical.
- Registry changes must be committed with atom changes when status, path, owner role, boundary, batch, dependencies, or blocking relationships change.

## Contracts

### Inputs

- accepted professional baseline
- decision gap register
- system boundary map
- boundary-control overlays
- accepted pre-atomization decisions
- atomization operating plan
- new pressure from product, implementation, review, or later analysis
- atom registry entries

### Outputs

- accepted, draft, deferred, hold-back, or rejected atom status
- open-decision entries
- rejected-path entries
- change-control log entries
- commit-role trace lines for relevant commits
- registry updates when atom metadata changes

### Boundary Crossings

| Crosses To | Through | Notes |
|---|---|---|
| All specification atoms | source hierarchy and atom template | Atoms consume governance; governance does not define their domain behavior. |
| Atom registry | metadata update rule | Registry accelerates lookup but does not replace atom content. |
| Decision Gap Register | open-decision routing | Gaps can be carried forward, but not closed here. |
| Change Control | change-control triggers and log entries | Baseline changes require explicit records. |
| Implementation Design | accepted atom status | Implementation designs may consume accepted atoms, not draft claims as architecture. |

## Source Hierarchy

Use sources in this order:

1. accepted professional baseline and boundary-control overlays
2. accepted pre-atomization decisions
3. decision gap register
4. change-control classifications for later claims
5. evidence archive only for verification or disputes

Do not draft final spec atoms directly from ADR narrative, exploration narrative, product-alignment prose, or implementation plans.

## Registry Procedure

At the start of a platform-spec session:

1. Read `../atom-registry.yml`.
2. Select the requested atom or the next recommended atom.
3. Read the selected atom, its listed dependencies, and only the source-basis files needed for the work.
4. Update `../atom-registry.yml` in the same commit if atom status, path, owner role, boundary, batch, dependencies, or blocking relationships change.

If the registry and atom file disagree, pause and reconcile them before continuing. The atom file remains canonical for content and rationale.

Registry `source_basis` paths are rooted at `docs/platform-spec-kernels/`.

## Atom Statuses

| Status | Meaning |
|---|---|
| Draft | Work in progress; not implementation authority. |
| Accepted | Current implementation-facing spec authority, subject to change control. |
| Deferred | Safe to postpone; reopen trigger must be named. |
| Hold-back | Do not decide now; prevent accidental closure. |
| Rejected | Do not use without formal reopen. |

## Acceptance Process

Before an atom can be accepted:

- source basis must be named
- owner and boundary must be singular, unless the atom is process/control material
- scope and non-scope must be explicit
- contracts must identify inputs, outputs, and boundary crossings
- open gaps must remain visible
- forbidden couplings must name likely drift risks
- rejected paths must not be reintroduced
- change-control triggers must be checked

## Change-Control Triggers

Stop and require formal change control if a proposed atom:

- adds event-envelope fields
- changes event-envelope field meaning
- adds structural event type values
- stores immutable `authority_context`
- adds `tenant_id`, `deployment_id`, `user_id`, or `group_id` to event authority
- makes account, group, identity-provider claim, tenant, or deployment fields direct authority sources
- makes field-level sensitivity a platform mechanism
- lets deployers author access-control logic or platform code
- makes mutable records, snapshots, queues, work items, or projections canonical truth
- introduces last-write-wins or invisible merge for operational conflicts requiring judgment
- changes assignment-derived access or sync scope as access scope
- changes the platform/deployer responsibility split

## Commit Trace Convention

Atomization commits should keep the project's existing conventional subject style and add a role line in the commit body.

Subject format:

```text
docs(spec): <short action>
```

Body format:

```text
Role: <Architecture Steward | Drafting Agent | Challenge Reviewer | Delivery Lead | Product Owner>
Trace: <baseline, atom, decision, or hold-back touched>
```

The commit role records responsibility for the change. It does not change atom status and does not make draft material accepted platform behavior.

## Allowed Extension Points

- New spec atoms may be added when routed through the inventory and template.
- New hold-backs may be added when a risk is discovered.
- New rejected paths may be added when review finds a drift pattern.
- New change-control entries may be added when a claim is accepted, rejected, disputed, or formally reopened.

## Forbidden Couplings

- Do not use governance to create platform behavior.
- Do not use commit roles as approval authority.
- Do not treat draft atoms as implementation authority.
- Do not treat the atom registry as a second architecture source of truth.
- Do not use product alignment or implementation convenience to bypass change control.
- Do not collapse stakeholder priority, architecture authority, and delivery sequencing into one decision.

## Open Gaps

| Gap | Owner / Route | Reopen Trigger |
|---|---|---|
| Final acceptance authority for individual atoms | Decision board / project owner | Before marking the first behavior atom as Accepted. |
| Whether atom acceptance requires human external review | Decision board | Before implementation begins from accepted atoms. |
| How implementation designs will cite accepted atoms | Delivery Lead / implementation planning | Before the first implementation design is drafted from these specs. |
| Whether registry validation should be automated | Delivery Lead / implementation tooling | Once atom count or agent concurrency makes manual checking unreliable. |

## Rejected Paths

- Building directly from the evidence archive.
- Treating ADR-006-R through ADR-009 as automatic baseline authority.
- Treating implementation plans as architecture closure.
- Treating product role labels or UI surfaces as platform boundaries.

## Implementation Implications

- Implementation work should map to accepted atoms.
- Draft atoms may inform exploration but must not be treated as stable contracts.
- Any implementation pressure that needs a new invariant must route through change control before code relies on it.

## Review Checklist

- [ ] Source basis is accepted and cited.
- [ ] Governance does not define domain behavior.
- [ ] Change-control triggers match the accepted baseline.
- [ ] Atom statuses are clear.
- [ ] Commit role convention is trace-only, not approval authority.
- [ ] Open gaps remain visible.
