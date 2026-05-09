# Spec Atom Template

Status: Template

Use this template for every platform specification atom created under this folder.

Do not delete sections because they feel empty. If a section does not apply, write `None` and explain why. Empty sections are where coupling usually hides.

```md
# <Atom Name>

Status: Draft | Accepted | Deferred | Hold-back | Rejected
Owning boundary: <one boundary from the system boundary map>
Primary owner: <role or review owner>
Source basis:
- <accepted baseline or control document>

Depends on:
- <accepted atom or guardrail>

Consumed by:
- <known downstream atoms or implementation areas>

## Purpose

<One short paragraph explaining why this atom exists.>

## Scope

This atom owns:

- <owned behavior or contract>

## Non-Scope

This atom does not own:

- <explicitly excluded lifecycle, behavior, or vocabulary>

## Definitions

| Term | Meaning In This Atom | Must Not Mean |
|---|---|---|
| <term> | <definition> | <forbidden overread> |

## Invariants

- <rule that must always hold>

## Contracts

### Inputs

- <events, references, configuration, projections, local state, or sync state consumed>

### Outputs

- <events, references, projections, decisions, validation results, or handoff artifacts emitted>

### Boundary Crossings

| Crosses To | Through | Notes |
|---|---|---|
| <boundary> | <event/reference/projection/config contract> | <constraint> |

## Allowed Extension Points

- <what deployers, implementation, or later platform evolution may vary>

## Forbidden Couplings

- <what must not be encoded here>

## Open Gaps

| Gap | Owner / Route | Reopen Trigger |
|---|---|---|
| <gap> | <decision gap, atom, implementation design, or operational policy> | <trigger> |

## Rejected Paths

- <rejected design path that reviewers should catch>

## Implementation Implications

- <engineering consequences that follow from this atom without prescribing full implementation design>

## Review Checklist

- [ ] Source basis is accepted and cited.
- [ ] Owner and boundary are singular.
- [ ] Scope and non-scope are explicit.
- [ ] Contracts identify inputs, outputs, and boundary crossings.
- [ ] Open gaps are not closed accidentally.
- [ ] Forbidden couplings include the likely drift risks.
- [ ] No envelope field, type value, authority shortcut, or canonical projection state was added without change control.
- [ ] Product labels, role labels, and UI surfaces remain outside platform-core semantics.
```

## Required Boundary Names

Use the boundary names from the accepted boundary map unless a formal decision adds another routing surface:

- Event Log / Storage
- Event Envelope / Schema
- Identity / Lineage
- Assignment / Authority / Sync
- Configuration
- Projection / Workflow State
- Flag / Resolution
- Trigger / Reactivity
- Reporting / Aggregation
- Local Data Lifecycle

`Deployment / Tenancy` may be used only as a hold-back routing surface under accepted pre-atomization guardrails. It is not yet a settled implementation boundary.

## Required Status Meanings

| Status | Meaning |
|---|---|
| Draft | Work in progress; not implementation authority. |
| Accepted | Current implementation-facing spec authority, subject to change control. |
| Deferred | Safe to postpone; reopen trigger must be named. |
| Hold-back | Do not decide now; prevent accidental closure. |
| Rejected | Do not use without formal reopen. |
