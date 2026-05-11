# Platform Specification Atomization Workspace

Status: Planning workspace

This folder is the working area for turning the accepted architecture baseline into small, reviewable, implementation-facing platform specification atoms.

It is not an evidence archive and it is not a replacement for the accepted baseline. Final spec atoms must be drafted from the accepted baseline, the decision gap register, the boundary map, and the boundary-control overlays named below.

## Goal

Turn the accepted baseline into small, reviewable, implementation-facing specs where every atom has:

- one owner
- one primary boundary
- explicit contracts
- explicit forbidden couplings
- known open gaps
- a controlled writing and review order

## Source Authority

Primary guardrails:

- `../professional-baseline/04-architecture-baseline-v0.md`
- `../professional-baseline/05-decision-gap-register.md`
- `../professional-baseline/07-system-boundary-map.md`
- `../professional-baseline/09-identity-boundary-control.md`
- `../professional-baseline/15-conflict-flag-offline-boundary-control.md`
- `../professional-baseline/16-operational-constraints-boundary-control.md`
- `../professional-baseline/17-authorization-visibility-boundary-control.md`
- `../pre-operations/04-accepted-pre-atomization-decisions.md`
- `../professional-baseline/18-envelope-shape-parametrization-boundary-control.md`
- `../professional-baseline/19-envelope-shape-parametrization-definitions.md`

Supporting process references:

- `../professional-baseline/02-change-control.md`
- `../professional-baseline/03-artifact-definitions.md`
- `../pre-operations/01-decision-board-operating-model.md`

## Folder Contents

- `atom-registry.yml`: compact machine-readable lookup for atom status, owner, boundary, batch, dependencies, and next work. Source-basis paths in the registry are rooted at `docs/platform-spec-kernels/`.
- `process/01-atomization-operating-plan.md`: the multi-session professional process for decomposition, drafting, review, and change control.
- `process/02-spec-atom-template.md`: the required template for every future platform spec atom.
- `process/03-atom-inventory-and-writing-order.md`: the initial decomposition map and iterative writing order.
- `process/04-planned-consumer-review-cards.md`: non-authoritative integration-review cards for checking whether planned downstream atoms can consume upstream drafts without hidden assumptions.
- `atoms/`: draft and accepted platform specification atoms.

## Non-Goals

This workspace must not:

- create platform behavior directly from ADR prose or exploration prose
- close gaps without the change-control process
- add event-envelope fields, type values, authority shortcuts, tenant/user/group authority, or canonical projection state
- turn product surfaces, role labels, queues, or workflow labels into platform classes
- collapse multiple boundaries into one convenience subsystem
- turn the registry into a second source of architecture authority

## Agent Start Rule

At the start of each platform-spec session, agents must read `atom-registry.yml`, then `process/01-atomization-operating-plan.md`, then only the atom, dependency, and source-basis files needed for the selected work.

If an atom path, status, owner role, boundary, batch, dependency, blocking relationship, or source basis changes, update `atom-registry.yml` in the same commit.

## Current Next Step

Use `atom-registry.yml` and `process/01-atomization-operating-plan.md` to run the next session:

1. read the registry and operating plan
2. reconcile the control foundation: `SPEC-001`, `SPEC-090`, and `SPEC-091`
3. prepare the foundation acceptance gate for `SPEC-002`, `SPEC-003`, and `SPEC-004`
4. use `process/04-planned-consumer-review-cards.md` for planned-consumer Integration Review of `SPEC-005`, `SPEC-006`, `SPEC-007`, and the direct `SPEC-009` projection/workflow and `SPEC-010` conflict/flag risk surfaces
5. update the registry in the same commit as any atom status or metadata change
