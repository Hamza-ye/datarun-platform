---
name: datarun-software-architect-boundary-reviewer
description: Review Datarun architecture-sensitive pressure only when evidence shows a stop trigger: CDL/contract semantics, stored truth, authority, sync/access scope, runtime partitioning, durable workflow state, deployer config power, reporting/audit breadth, retention/security, or architecture/gap routing.
---

# Datarun Software Architect Boundary Reviewer

Use this skill only when a concrete trigger fires. It is not the default route
for ordinary product, UI, copy, implementation, test, or documentation work.

## Purpose

Decide whether a change can stay in implementation/tooling/product/spec work or
must stop for an architecture/gap route. The reviewer protects the CDL and
accepted contracts without recreating broad steward protocol or blocking product
progress by tone alone.

The CDL is current architecture authority, not a sacred or frozen document.
Amending, splitting, superseding, or clarifying CDL standing is a routable
outcome when classification proves the current architecture record is wrong,
stale, underspecified, or in conflict with accepted evidence. Do not make CDL
change, suppression, or addition the first move; first classify the pressure and
choose the least disruptive route that preserves product progress.

## Required Inputs

1. `docs/status.md` Current Routing.
2. The user request, selected NW/task packet, PR, diff, or touched files.
3. Exact accepted product/platform spec or contract named by the touched
   surface.
4. CDL slices, architecture-decision-anchors, only for the concrete concern.
   Do not read the whole CDL/architecture records unless the selected task explicitly requires it.
5. `docs/agent-working-surface/decision-anchor-layer/gap-routing-playbook.md`
   only after a trigger is present or the task is explicitly architecture/gap
   work.

## Trigger Test

Route to this skill when the proposal, diff, or claim would add, remove, or
reinterpret any of these:

- canonical event envelope fields, event `type`, `shape_ref`, `subject_ref`,
  `actor_ref`, `activity_ref`, causal metadata, or platform payload contracts;
- stored event meaning, historical interpretation, immutable-event behavior,
  projection truth, identity aliasing, merge/split, or lifecycle truth;
- sync/access scope, subject-history behavior, assignment containment, new
  scope mechanisms, tenant/workspace partitioning, or runtime partitioning;
- actor authority, principal binding, IdP group/claim authority, request-body
  actor authority, resolver truth, or command capability authority;
- durable workflow-state authority, trigger execution, auto-resolution,
  resolver reassignment, pattern registry semantics, or flag resolvability;
- deployer configuration power beyond accepted setup/configuration: access
  logic, state machines, arbitrary code, scripts, recursive rules, runtime
  triggers, or custom platform vocabulary;
- local/offline compatibility promises: actor partitions, sealed partitions,
  cursor ownership, retention, expiry, purge, recovery, encryption, or
  cross-actor recovery;
- broad reporting, audit/history access, aggregate access bypass, import/export,
  legacy replay, registry import, production cutover, or security/retention
  promises.
- If the diff broadly touches both a platform-owned mechanism and a deployer-authored instance surface.

If none of these are affected, return the work to the selected implementation,
PM, review, or validation lane with a short "no architecture route" verdict.

## CDL Slice Map

Use the smallest relevant slice:

- event/envelope/storage: `CDL-001`, `CDL-006` through `CDL-021`;
- identity/merge/split/resolver: `CDL-022` through `CDL-029`;
- assignment/access/sync/authority: `CDL-030` through `CDL-037`, `CDL-055`;
- configuration/expression/package: `CDL-038` through `CDL-046`, `CDL-052`,
  `CDL-056`;
- workflow/pattern/trigger/auto-resolution: `CDL-047` through `CDL-054`;
- unknown topic: search the CDL JSON/CLI by the exact term from the diff before
  opening any broad architecture document.
- Mechanisms and instances: `CDL-005`

## Classification

Classify the pressure before proposing work:

- Product/problem evidence gap.
- Bug or implementation/tooling gap inside accepted boundaries.
- Platform/product spec detail gap.
- Architecture decision gap.
- Contract/schema/protocol change.
- Delivery-process issue.
- Inherited debt or progress-staleness risk.
- Quality/security/recovery issue.

Architecture is required only when the classification changes structural
contracts, accepted authority, stored truth, irreversible compatibility, or a
CDL-protected boundary. Otherwise choose the least disruptive route that returns
to product progress.

If the classification shows the CDL itself is the stale or incomplete surface,
recommend a bounded CDL-successor, reclassification, or clarification route
with acceptance evidence. If the issue can be handled by product/spec,
implementation/tooling, contract, or operations work inside accepted
boundaries, return it there instead.

## Outputs

Return:

- boundary verdict: no architecture route needed, product/spec route needed,
  implementation/tooling route OK, operational policy route needed, contract
  route needed, or architecture/gap route required;
- trigger evidence from status, diff, touched files, or accepted docs;
- CDL slices consulted, or why no CDL slice was needed;
- exact stop boundary and forbidden expansion;
- minimal next artifact or NW route, if any;
- executable acceptance evidence expected for the next route;
- return trigger to product/pilot progress.

## Must Not

- Do not run by default.
- Do not block ordinary UI/product copy, docs hygiene, or bounded implementation
  without a concrete trigger.
- Do not use "future decision", "not production", gap-register presence, or
  strong historical wording as a blocker by itself.
- Do not create architecture decisions directly.
- Do not treat CDL amendment, suppression, or addition as forbidden; route it
  only when classification shows it is the right smallest correction.
- Do not preserve implemented drift as architecture policy when CDL/contracts
  disagree; classify the conflict and propose the smallest correction.
- Do not select broad BAR/CDL/gap-register work unless the current request
  explicitly selects it.
