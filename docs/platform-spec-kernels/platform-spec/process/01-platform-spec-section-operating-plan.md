# Platform specification section drafting Operating Plan

Status: Candidate process input

This document defines the multi-session process for turning the accepted platform baseline into implementation-facing specification sections without creating mud, drift, premature coupling, or irreversible decisions.

## North Star

The platform spec must preserve append-only operational truth, offline-first capture, assignment-scoped authority, projection-derived state, bounded deployer parameterization, and visible conflict handling.

Every platform-spec section must make the platform easier to implement without weakening those constraints.

## Operating Rule

Platform specification section drafting is a source-authority discipline before it is a writing process.

No section is accepted until it has:

- one primary owner
- one primary boundary
- explicit non-scope
- explicit contracts
- explicit forbidden couplings
- known open gaps
- source basis linked to accepted sources and routed constraints
- review against change-control triggers
- Challenge Review and Integration Review evidence

## Source Hierarchy

Use sources in this order:

1. accepted architecture baseline: `04-architecture-baseline-v0.md`
2. canonical decision gap register: `05-decision-gap-register.md`
3. architecture responsibility map: `07-system-boundary-map.md`
4. platform-spec outline: `20-platform-spec-outline.md`
5. assessed inputs only where routed through the outline or an affected `05` gap
6. evidence archive only for verification or disputes

Do not draft final platform-spec sections directly from ADR narrative, exploration narrative, product-alignment prose, or implementation plans.

## Agent Start Procedure

At the start of every platform-spec session:

1. Read `section-registry.yml`.
2. Identify the requested section, next recommended section, or affected hold-back.
3. Read this operating plan whenever the session drafts, reviews, changes section status, changes dependencies, touches an open gap, records a rejected path, or handles change control.
4. Read only the selected section, its registry-listed dependencies, and the cited source-basis files needed for the work.
5. Update `section-registry.yml` in the same commit if section status, path, owner role, boundary, batch, dependencies, blocking relationships, or source basis change.

The registry is a fast lookup layer. It is not architecture authority. If the registry and section file disagree, pause and reconcile them before continuing.

Registry `source_basis` paths are rooted at `docs/platform-spec-kernels/`.

## Commit Role Convention

Commit history is a traceability surface for this work. Platform specification section drafting commits should keep the project's existing conventional subject style and add a role line in the commit body.

Subject format:

```text
docs(spec): <short action>
```

Commit body format:

```text
Role: <Architecture Steward | Drafting Agent | Challenge Reviewer | Integration Reviewer | Delivery Lead | Product Owner>
Trace: <baseline, section, decision, or hold-back touched>
```

Use the role that best describes the responsibility of the change:

| Role | Use When The Commit Mainly |
|---|---|
| Architecture Steward | Preserves or clarifies architecture boundaries, invariants, or change-control routing. |
| Drafting Agent | Drafts specs, templates, inventories, or process artifacts under accepted sources and routed constraints. |
| Challenge Reviewer | Records review findings, rejected paths, disputes, or coupling risks. |
| Integration Reviewer | Checks whether upstream sections can be consumed by immediate downstream sections without circular ownership or hidden assumptions. |
| Delivery Lead | Sequences implementation-facing work, delivery constraints, or build order. |
| Product Owner | Records stakeholder priority, accepted deferral, or product-impact decision. |

Do not use the commit role to bypass document status. A commit can record a draft, hold-back, or rejected path without making it accepted platform behavior.

## Role Discipline

Roles are operating constraints, not labels for tone. A person or agent may switch roles in one chat session, but the switch must be explicit and the new role must use the prior role's output as input rather than silently rewriting it.

Use one active role per work unit:

- Drafting Agent drafts scoped section text from the selected source basis and does not accept sections, close gaps, or decide change control.
- Challenge Reviewer records findings, coupling risks, rejected-path matches, and pause triggers; it does not rewrite findings into approval.
- Integration Reviewer checks boundary crossings, dependency direction, and downstream consumption; it does not draft downstream behavior to make an upstream section look complete.
- Architecture Steward routes findings, reconciles source authority, classifies decision pressure, and prepares status recommendations; it does not use stewardship as a shortcut for acceptance.
- Delivery Lead sequences implementation-facing work and transition artifacts; it does not treat draft sections as implementation authority.
- Product Owner states priority, deployment pressure, and product impact; it does not convert product need into architecture closure.

Commits should normally represent one dominant role. If a commit contains reconciliation after review, the trace must name the review or gap being reconciled.

## Core Anti-Mud Rules

The process must keep these axes separate:

- event truth vs projection state
- envelope processing type vs payload fact shape
- reference category vs referent lifecycle ownership
- actor/authentication identity vs assignment-derived authority
- role label vs platform actor class
- activity context vs authority snapshot
- workflow pattern vs product queue
- conflict detection vs flag lifecycle
- offline capture vs global correctness
- deployer parameterization vs deployer-authored platform logic
- deployment/account/group context vs event-envelope authority

If a section blurs one of these axes, stop and re-route the claim before drafting continues.

## Session Cadence

Run platform-spec section drafting as repeated architecture sessions.

### Session 0: Intake And Source Check

Inputs:

- accepted source list
- current decision gap register
- existing section inventory
- any new implementation or product pressure

Actions:

1. confirm no source authority changed
2. classify new pressure through change control
3. update hold-backs if needed
4. select the next section batch

Exit criteria:

- the batch has no unresolved owner or boundary ambiguity
- all required pre-specification decisions are accepted or explicitly held back

### Session 1: Section Scoping

Actions:

1. name the section
2. assign one primary boundary
3. list accepted source basis and routed constraints
4. define scope and non-scope
5. list adjacent sections and boundary crossings
6. identify open gaps that must remain open

Exit criteria:

- scope can be explained without product personas or implementation modules
- non-scope prevents the most likely coupling errors

### Session 2: Contract Drafting

Actions:

1. write invariants
2. define inputs and outputs
3. define event, reference, projection, configuration, or sync contracts
4. define allowed extension points
5. define forbidden couplings

Exit criteria:

- a future implementation can tell what it may consume and what it may emit
- deployer variation is separated from platform-owned mechanisms
- no open gap has been silently closed

### Session 3: Boundary Review

Review against these questions:

1. Did the section add or reinterpret an event-envelope field?
2. Did it add a structural event type?
3. Did it make a projection canonical?
4. Did it turn a role label into a platform actor class?
5. Did it make accounts, groups, identity-provider claims, tenant context, or deployment context an authority source?
6. Did it store immutable authority context?
7. Did it let deployers author platform logic?
8. Did it assume global knowledge for ordinary offline capture?
9. Did it silently resolve conflicts or stale authority?
10. Did it absorb another boundary's lifecycle?

Exit criteria:

- any "yes" answer is either removed, routed to another section, or escalated through change control

### Session 4: Integration Review

Actions:

1. check all boundary crossings
2. remove circular ownership
3. confirm all dependencies point backward to accepted platform-spec sections or accepted professional-baseline sources
4. check immediate downstream consumers, including planned sections, for hidden assumptions
5. use `process/04-planned-consumer-review-cards.md` when a downstream consumer does not yet have a section file
6. update the section inventory
7. update open decisions and rejected paths

Exit criteria:

- the section can be accepted without forcing another section to accept hidden behavior
- planned downstream sections have an explicit non-authoritative consumer review surface, or the upstream section remains draft

### Session 5: Acceptance Or Rework

Allowed outcomes:

- accepted as current platform-spec section
- accepted as draft with named open gaps
- deferred
- hold-back
- needs spike
- requires formal ADR/change
- rejected

Avoid vague outcomes such as "later" without a reopen trigger.

Acceptance requires:

- completed Challenge Review
- completed Integration Review
- Architecture Steward recommendation
- Decision Board / Project Owner approval for the status change
- section status and registry status updated in the same commit

Draft sections may be read for planning context, but implementation designs cite accepted sections only.

## Writing Batches

Write sections in small batches. A batch should contain enough context to be coherent, but not enough to hide coupling.

Recommended batch size:

- 2 to 4 sections for foundation work
- 1 to 2 sections for high-risk boundary work
- 1 section for anything touching envelope, authority, sync, tenancy, identity, or configuration semantics

## Initial Milestones

### Milestone A: Planning Scaffold

Acceptance target:

- platform-spec section drafting operating plan exists
- section template exists
- section inventory and writing order exists
- no final platform behavior is introduced

### Milestone B: Foundation Sections

Draft in order:

1. specification control
2. glossary and core definitions
3. event log and storage
4. event envelope and schema

Acceptance target:

- stable source hierarchy
- stable language for event truth, envelope type, shape ref, activity ref, actor ref, subject ref, projections, and parameterization
- foundation acceptance happens as a batch after planned consumers `SPEC-005`, `SPEC-006`, and `SPEC-007`, plus direct registry consumers `SPEC-009` for the projection/workflow risk surface and `SPEC-010` for the conflict/flag risk surface, are checked through Integration Review

### Milestone C: Core Boundary Sections

Draft in order:

1. references and identity lineage
2. configuration and parameterization
3. assignment, authority, and sync

Acceptance target:

- identity does not own authority, process lifecycle, workflow, or reporting
- configuration does not become arbitrary deployer code
- authority remains assignment-derived and projection-derived

### Milestone D: Derived Behavior Sections

Draft in order:

1. projections, workflow, and patterns
2. conflict, flag, and resolution
3. trigger and reactivity

Acceptance target:

- workflow state remains derived
- conflict detection and flag lifecycle remain separate
- downstream effects respect detect-before-act

### Milestone E: Operational Surface Sections

Draft conditionally:

1. local data lifecycle
2. reporting, aggregation, and freshness
3. audit export and interoperability
4. retention and archival
5. deployment and operations hold-backs

Acceptance target:

- operational needs remain routed without rewriting core invariants

## Change-Control Triggers

Stop and require formal change control if a proposed section:

- adds event-envelope fields
- changes event-envelope field meaning
- adds structural event type values
- stores immutable `authority_context`
- adds `tenant_id`, `deployment_id`, `user_id`, or `group_id` to event authority
- makes external identity-provider claims direct authority sources
- makes field-level sensitivity a platform mechanism
- lets deployers author access-control logic or platform code
- makes mutable records, snapshots, queues, or projections canonical truth
- introduces last-write-wins or invisible merge for operational conflicts requiring judgment
- changes assignment-derived access or sync scope as access scope
- changes the platform/deployer responsibility split

## Acceptance Checklist

Before a section is accepted:

- source basis is named
- owner and boundary are singular
- scope and non-scope are explicit
- contracts are testable enough for implementation design
- open gaps remain visible
- forbidden couplings are concrete
- rejected paths are not reintroduced
- downstream sections know how to consume it
- immediate planned downstream sections have been checked through section files or planned-consumer review cards
- accepted status has a Decision Board / Project Owner approval record
- no product alignment language is needed to understand the section

## Working Principle

Prefer narrow, boring sections that compose through explicit contracts over broad sections that feel complete but hide ownership.
