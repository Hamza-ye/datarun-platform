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

- `START-HERE.md`: short orientation guide for humans and agents. It routes work without becoming architecture authority.
- `atom-registry.yml`: compact machine-readable lookup for atom status, owner, boundary, batch, dependencies, and next work. Source-basis paths in the registry are rooted at `docs/platform-spec-kernels/`.
- `process/01-atomization-operating-plan.md`: the multi-session professional process for decomposition, drafting, review, and change control.
- `process/02-spec-atom-template.md`: the required template for every future platform spec atom.
- `process/03-atom-inventory-and-writing-order.md`: the initial decomposition map and iterative writing order.
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

At the start of each platform-spec session, agents must read `START-HERE.md`, then `atom-registry.yml`, then only the operating-plan, atom, dependency, and source-basis files needed for the selected work.

If an atom path, status, owner role, boundary, batch, dependency, or blocking relationship changes, update `atom-registry.yml` in the same commit.

## Current Next Step

Use `START-HERE.md` and `atom-registry.yml` to run the next session:

1. read the start guide and registry
2. confirm `SPEC-002` and `SPEC-003` remain draft foundation atoms
3. run a focused design session for `atoms/04-event-envelope-schema.md`
4. update the registry only if atom metadata changes
