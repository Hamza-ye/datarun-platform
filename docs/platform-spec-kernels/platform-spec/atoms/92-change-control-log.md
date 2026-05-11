# Change-Control Log

Status: Draft
Owning boundary: Cross-boundary process register
Primary owner: Architecture Steward

Source basis:

- `../../professional-baseline/02-change-control.md`
- `../../professional-baseline/04-architecture-baseline-v0.md`
- `../../professional-baseline/05-decision-gap-register.md`
- `../process/01-atomization-operating-plan.md`
- `01-spec-governance.md`

Depends on:

- `01-spec-governance.md`
- `90-open-decisions.md`
- `91-rejected-paths.md`

Consumed by:

- all future atom reviews
- decision board reviews
- implementation planning

## Purpose

This log records formal changes, disputes, accepted gap closures, and rejected baseline challenges that arise during platform-spec atomization.

## Scope

This log owns:

- accepted change-control outcomes
- links from changed claims to affected baseline items
- trace of why a change does not silently override prior closure

## Non-Scope

This log does not own:

- draft proposals before review
- ordinary typo or wording changes that do not affect meaning
- implementation ticket history
- product prioritization

## Invariants

- No accepted ADR-001 through ADR-005 baseline item is changed unless recorded here or in a later designated change-control artifact.
- Every accepted baseline-affecting change must name the affected baseline item, source claim, classification, decision owner, result, and rationale.
- This log starts empty because no platform-spec atomization change has yet modified the accepted baseline.

## Change-Control Entries

No accepted baseline changes have been recorded during platform-spec atomization yet.

## Entry Template

```md
## CC-YYYY-NNN: <Short Name>

Status: Proposed | Accepted | Rejected | Deferred | Valid Dispute | Superseded
Date:
Decision owner:
Affected baseline item:
Affected atom:
Source claim:
Classification:

Result:

Rationale:

Why this does not silently override prior closure:

Follow-up:
```

## Classification Values

Use the classifications from `../../professional-baseline/02-change-control.md`:

- consistent elaboration
- open-gap closure candidate
- deferred implementation or spec detail
- new unauthorized claim
- conflict with closed baseline
- valid dispute

## Contracts

### Inputs

- change-control proposals
- atom review findings
- decision board outcomes
- implementation pressure that challenges baseline behavior

### Outputs

- accepted or rejected change-control records
- affected atom updates
- open-decision register updates
- rejected-path register updates

### Boundary Crossings

| Crosses To | Through | Notes |
|---|---|---|
| Open Decisions | closure or deferral entries | Accepted closure may remove or revise hold-backs. |
| Rejected Paths | rejected or reopened paths | Reopening requires explicit record. |
| Behavior Atoms | affected atom updates | Atoms update only after change-control outcome is recorded. |

## Allowed Extension Points

- Additional metadata may be added if future review needs stronger traceability.
- Entries may link to commits, decision briefs, ADRs, or implementation designs.

## Forbidden Couplings

- Do not use this log to sneak in unreviewed behavior.
- Do not treat a draft proposal as accepted because it appears here.
- Do not record ordinary editorial changes as architecture changes.

## Open Gaps

| Gap | Owner / Route | Reopen Trigger |
|---|---|---|
| Whether change-control entries should receive stable IDs tied to commits | Architecture Steward / Delivery Lead | Before the first accepted baseline-affecting change. |
| Whether this log remains in platform-spec or moves to a broader decisions folder | Decision Board | If change-control volume grows beyond atomization work. |

## Review Checklist

- [ ] Log starts with no accepted baseline changes.
- [ ] Entry template captures all required fields.
- [ ] Classifications match accepted change-control language.
- [ ] No draft proposal is treated as accepted behavior.
