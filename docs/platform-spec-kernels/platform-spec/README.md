# Platform Specification Section Workspace

Status: Candidate section input workspace

This folder contains candidate platform-spec section material.

It is not an evidence archive, not a process authority, and not a replacement for the accepted baseline or the platform-spec outline. Existing section files must be reviewed against `../professional-baseline/20-platform-spec-outline.md` before any section is treated as normative.

## Goal

Turn the accepted baseline into reviewable, implementation-facing platform-spec sections where every section has:

- one owner
- one primary boundary
- explicit contracts
- explicit forbidden couplings
- known open gaps
- a clear writing and review order

## Source Authority

Primary source path:

- `../professional-baseline/04-architecture-baseline-v0.md`
- `../professional-baseline/05-decision-gap-register.md`
- `../professional-baseline/07-system-boundary-map.md`
- `../professional-baseline/20-platform-spec-outline.md`

Assessed source material may be cited only where the outline or `05` routes it into an affected section. Existing `sections/` files remain draft/candidate material until reviewed against the primary source path.

Supporting process references:

- `../professional-baseline/02-change-control.md`
- `../professional-baseline/03-artifact-definitions.md`
- `../pre-operations/01-decision-board-operating-model.md`

## Folder Contents

- `section-registry.yml`: non-authoritative manifest for outline-aligned section paths, draft/planned labels, and candidate inputs. It owns no architecture, gap closure, section acceptance, implementation readiness, or remote tracking state. Source-basis paths in the registry are rooted at `docs/platform-spec-kernels/`.
- `process/01-platform-spec-section-operating-plan.md`: the multi-session professional process for decomposition, drafting, review, and change control.
- `process/02-platform-spec-section-template.md`: the required template for every future platform-spec section.
- `process/03-section-inventory-and-writing-order.md`: the initial decomposition map and iterative writing order.
- `process/04-planned-consumer-review-cards.md`: non-authoritative integration-review cards for checking whether planned downstream sections can consume upstream drafts without hidden assumptions.
- `process/05-batch-1b-acceptance-packet.md`: historical approval trace from the previous section workflow; it is review evidence, not current acceptance authority.
- `sections/`: candidate and draft platform specification sections.

## Non-Goals

This workspace must not:

- create platform behavior directly from ADR prose or exploration prose
- close gaps without the change-control process
- add event-envelope fields, type values, authority shortcuts, tenant/user/group authority, or canonical projection state
- turn product surfaces, role labels, queues, or workflow labels into platform classes
- collapse multiple boundaries into one convenience subsystem
- turn the registry into a second source of architecture authority

## Agent Start Rule

At the start of each platform-spec section session, agents must read `../professional-baseline/20-platform-spec-outline.md`, then use `section-registry.yml` only as a manifest for locating candidate files and source-basis pointers.

If a section path, manifest status label, owner, batch, candidate input, or source-basis pointer changes, update `section-registry.yml` in the same commit. If the manifest disagrees with `04`, `05`, `07`, `20`, or an accepted section, treat the manifest as stale.

## Current Next Step

Do not continue from the old Batch 2 sequence by default.

First review `../professional-baseline/20-platform-spec-outline.md`, identify which `05-decision-gap-register.md` gaps block the first platform-spec sections, then classify the existing `sections/` files as candidate input, reusable text, stale process material, or material that must be rewritten.
