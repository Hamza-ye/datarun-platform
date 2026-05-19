# Baseline Workflow

This workflow describes what a production engineering team would do at the current platform-spec-kernels stage.

## Operating Position

The large staging files are evidence, not the artifact engineers should build from. The closure register is a baseline map, not the final platform specification. The next work should produce a usable architecture baseline and a controlled gap register.

## Work Order

### 1. Freeze The Current Baseline

Declare ADR-001 through ADR-005 as the current accepted baseline for extraction purposes.

Meaning:

- The baseline can be challenged later, but only through explicit change control.
- ADR-006-R through ADR-009 cannot silently revise it.
- Implementation planning can reference it as the current decision surface.

Primary reference:

- `../10-adr1-5-rest-state-closure-register.md`

### 2. Produce Architecture Baseline v0

Create one implementation-facing architecture baseline document from the closure register.

It should cover only:

- storage model
- event envelope
- identity and references
- conflict and stale-event handling
- authorization and sync
- configuration boundary
- projection and workflow
- flags only where already closed by ADR-001 through ADR-005
- open decisions and known deferred details

It should not include:

- ADR history
- exploration narrative
- broad rationale
- unresolved ADR-006-R through ADR-009 claims
- implementation design not supported by the baseline

### 3. Produce Decision Gap Register

Create a short list of items that remain unresolved after ADR-001 through ADR-005.

Each gap must be classified as one of:

- architecture decision gap
- platform-spec detail gap
- implementation/tooling gap
- operational policy gap
- later-source assessment gap

### 4. Define Change Control

Before assessing ADR-006-R through ADR-009, define how a new claim can affect the baseline.

Allowed outcomes:

- consistent elaboration
- valid closure of an explicit open gap
- deferred implementation/spec detail
- new unauthorized claim
- conflict with closed baseline
- valid dispute requiring formal reopen

### 5. Assess ADR-006-R Through ADR-009 Against Gaps

Read later ADRs only through the gap register and change-control categories.

Do not treat them as a fresh source sequence that automatically closes platform behavior.

### 6. Do Not Persist A Separate Outline Artifact

Do not retain a standalone platform specification outline artifact.

Any future outline-like synthesis should be generated from the accepted baseline, gap register, and responsibility map for the specific review task, not kept as a parallel source.

## Stop Conditions

Stop and record a conflict if:

- a later claim contradicts a closed baseline item
- the closure register and staging evidence disagree
- an item cannot be classified without reopening a decision
- a proposed kernel depends on forbidden sources

## Success Criteria

This workflow is succeeding when an engineer can read the baseline and gap register without reading the full extraction archive and understand:

- what is decided
- what is not decided
- what is rejected
- what can be implemented now
- what needs formal decision work before implementation
