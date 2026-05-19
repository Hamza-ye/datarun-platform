# Baseline Stabilization Plan

Status: Completed operating plan

This plan defines the order for stabilizing the ADR-001 through ADR-005 baseline and its gaps before any later work changes the baseline.

## Goal

Make the current baseline usable by engineering without hiding open decisions, reopening settled decisions, or continuing broad extraction.

The stabilized state should answer:

- what is decided now
- what is explicitly not decided
- what work closes each gap
- which responsibility area owns each mechanism or gap
- what later ADR claims may be assessed
- what must not be reopened without formal change control

## Stabilization Order

### Step 1: Baseline Acceptance Check

Review `04-architecture-baseline-v0.md` against `../10-adr1-5-rest-state-closure-register.md`.

Outcome:

- mark the baseline as accepted, or
- record exact baseline items needing correction

Do not read ADR-006-R through ADR-009 in this step.

Acceptance criteria:

- every major closure-register settled item is represented
- rejected paths are listed
- open/deferred items are not stated as decisions
- ADR-006-R through ADR-009 are not used as closure sources

### Step 2: Gap Closure Path Assignment

Review `05-decision-gap-register.md` and assign each gap a closure path.

Allowed closure paths:

- formal architecture decision
- platform-spec detailing
- implementation/tooling design
- operational policy definition
- later-source assessment
- no action until product need appears

Outcome:

- every gap has a closure path
- no gap is sent to broad exploration by default

### Step 3: Gap Priority Triage

Rank gaps by what they block.

Priority levels:

- P0: blocks architecture baseline acceptance
- P1: blocks affected future specification/detail work
- P2: blocks implementation planning for a core subsystem
- P3: product/operations detail that can wait

Outcome:

- a short ordered list of next gap work
- no parallel reopening of unrelated decisions

### Step 4: Architecture Responsibility Map Validation

Validate `07-system-boundary-map.md` against the accepted baseline and gap register.

Outcome:

- settled mechanisms have owning responsibility areas
- every gap has one primary responsibility owner
- later claims have a routing surface before classification

### Step 5: Targeted Later-Source Assessment

Assess ADR-006-R through ADR-009 only against gaps or explicit baseline disputes.

Allowed outcomes:

- consistent elaboration of a settled responsibility area
- open-gap closure candidate for a named owner gap
- deferred implementation/spec detail
- new unauthorized claim
- conflict with closed baseline
- valid dispute requiring formal reopen

Outcome:

- later claims are classified, not absorbed
- baseline remains unchanged unless a formal change is accepted

### Step 6: Removed Outline Attempt

Historical plan step: after baseline acceptance and gap triage, draft the first platform specification outline.

Current standing: the produced outline has been removed and the `../platform-spec/` workspace is frozen.

Inputs:

- accepted `04-architecture-baseline-v0.md`
- triaged `05-decision-gap-register.md`
- validated `07-system-boundary-map.md`
- `02-change-control.md`

Do not generate the outline directly from ADRs or exploration files.

## What Not To Do

- Do not run another broad extraction pass over all staging files.
- Do not treat every gap as needing exploration.
- Do not read ADR-006-R through ADR-009 as automatic authority.
- Do not split detailed specification sections until the baseline and gaps are stable.
- Do not patch large staging files unless a concrete extraction error is found.

## Professional Team Practice

A production team at this stage would stabilize the baseline first, then move through gaps by closure path:

1. Confirm the accepted architecture baseline.
2. Decide which open items actually block engineering.
3. Route settled mechanisms and gaps through architecture responsibility areas.
4. Route platform-spec details through the gap register or change control before any section work exists.
5. Turn implementation/tooling items into design docs or tickets.
6. Turn policy gaps into product/operations decisions.
7. Use later ADRs only as assessed inputs, not as superseding truth.

## Completed Stabilization Outcome

This stabilization sequence has been completed through the professional-baseline records:

1. `08-baseline-acceptance-check.md` accepted the ADR-001 through ADR-005 baseline.
2. `07-system-boundary-map.md` was validated, corrected where needed, and reframed as an architecture responsibility map.
3. ADR-006-R through ADR-009 were assessed against the stabilized gaps and responsibility areas in `10` through `13`.
4. Historical pattern-inventory material was assessed in `14`.
5. Assessment records in `15` through `19` captured cross-cutting risks and routed durable content into `05` and `07`.

There is no active platform-spec drafting path from this plan. Use `10` through `19` only as traceable assessed inputs when `05` or change control needs their routed findings.
