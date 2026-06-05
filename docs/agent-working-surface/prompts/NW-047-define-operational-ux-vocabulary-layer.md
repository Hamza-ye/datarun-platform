# NW-047 Agent Prompt: Define Operational UX Vocabulary And Product Layering Boundary

You are working in `/home/hamza/datarun-platform`.

## Goal

Create a bounded operational UX/product layering companion before any serious operational UI implementation.

Exit target:

```text
Future UI, reporting, workflow, and scenario explorations have a clear product-facing vocabulary and layering boundary that keeps core platform architecture, deployer/domain configuration, operational UX concepts, and concrete screens separate.
```

This is a stewardship/design-control slice. It should produce guidance and routing guardrails, not runtime code, UI screens, schemas, APIs, CDL changes, or accepted product capabilities.

## Files To Read

Read only this packet by default:

1. `AGENTS.md`
2. `docs/status.md` Current Routing section only.
3. `docs/agent-working-surface/README.md`
4. `docs/behavioral_patterns.md`
5. `docs/agent-working-surface/platform-next-work-backlog.md`
   - Read NW-041 through NW-047.
6. `docs/checkpoints/checkpoint-2026-06-04-gap-baseline-assessment.md`
   - Read the gap table, exploration list, S22 addendum, guardrails, and march orders.
7. `docs/agent-working-surface/architecture-rationale-and-routing-companion.md`
   - Read only the anti-pattern, routing, and change-classification sections relevant to UI/product vocabulary leakage.
8. `docs/reviews/scenario-baseline-pressure-map-protocol.md`
9. `docs/scenarios/22-coordinated-distribution-campaign-across-grouped-locations.md`
10. `docs/scenarios/26-operational-reporting-and-aggregate-oversight.md`
11. `docs/scenarios/27-logistics-distribution-composite.md`
12. `docs/walk-throughs/itn-distribution-campaign.md`
13. `docs/implementation/module-interfaces.md`
    - Read `Event Store`, `Projection Engine`, `Conflict Detector`, `Scope Resolver`, `Config Packager`, `Pattern Registry`, `Trigger Engine`, and `Command Validator`.
14. Use `scripts/query_cdl.py` only for CDL entries needed to check wording around event truth, accept-and-flag, projections, mechanism/instance split, configuration delivery, mobile advisory authority, flags, scope, and workflow patterns.

Do not read broad architecture/history docs unless a directly read source routes you there.

## Expected Artifact

Produce a concise companion or companion update. Prefer a new active working-surface document unless current routing clearly points to an existing file:

```text
docs/agent-working-surface/operational-ux-layering-companion.md
```

The artifact should include:

1. A direct assessment of why operational UX/product vocabulary is a real architecture boundary.
2. A layered model separating:
   - core platform architecture;
   - domain/workflow configuration;
   - operational UX/product vocabulary;
   - concrete UI screens and interactions.
3. A minimal operational UX construct set, with mapping columns:
   - product-facing concept;
   - what the user should understand;
   - current core backing constructs;
   - examples of domain labels;
   - forbidden interpretation.
4. A short anti-pattern list for future agents.
5. A checklist future gap explorations must apply before routing product/UI-facing work.
6. A routing rule for when a future concern needs:
   - companion-only guidance;
   - a probe;
   - a future decision;
   - an IDR;
   - a CDL/BAR-level change.
7. A clear statement that this companion is not architecture authority and cannot promote deferred capabilities.

Use examples from simple monthly reporting, S22 coordinated distribution, S26 reporting, and S27 logistics only as examples. Do not let any example become platform vocabulary by accident.

## Recommended Minimal Vocabulary To Evaluate

Start from this candidate set, revise only if the repo evidence requires it:

| Candidate UX concept | Intended product meaning | Guardrail |
|---|---|---|
| Work item | A thing a person can act on or inspect. | Not a new event type, assignment type, or task engine. |
| Activity entry | A user-authored record of work performed. | Backed by existing event/activity shape semantics; not internal envelope vocabulary. |
| Assignment | User-visible responsibility/route/queue membership. | Administration authority remains outside activity role-actions. |
| Progress | Derived status of a subject, location, campaign, or work set. | Read-side interpretation unless a human-authored status event exists. |
| Pending review | A record needing human inspection or resolution. | Must not imply auto-resolution or resolver reassignment. |
| Attention item | A flag, warning, stale condition, or unresolved conflict visible to a user. | Server flags remain canonical; mobile warnings remain advisory. |
| Blocked item | Work that cannot proceed operationally without human action or missing context. | Not automatic trigger execution unless a future decision adds it. |
| Change | A user-facing explanation that something was added, reassigned, resolved, superseded, or corrected. | Backed by events/projections, not mutable audit text. |
| Handoff | Continuity of responsibility or work context across actors/devices. | Use assignment history and subject-history; no new sync scope by default. |
| Report view | A scoped read-side aggregation with freshness and drill-back. | Not a reporting warehouse/API unless separately routed. |

## Forbidden Work

- Do not implement UI screens, widgets, mobile flows, backend APIs, schemas, config-package keys, report endpoints, or view-model contracts.
- Do not edit runtime code or tests unless a narrow documentation test already exists and must be updated.
- Do not change CDL text, accept BAR rows, or create an IDR unless the analysis concludes a companion is insufficient; if so, route the need instead of writing the IDR in this slice.
- Do not introduce product vocabulary that overrides core terms such as event, scope, resolver, projection, flag category, pattern, or actor.
- Do not promote entity lifecycle, triggers, auto-resolution, resolver reassignment, new scope mechanisms, reporting warehouse/API, import/export, IdP claims, or mobile authoritative rejection.
- Do not use ITN/campaign/logistics/domain terms as platform concepts.
- Do not make UX vocabulary an authority source for synchronization, conflict detection, assignment command authorization, resolver equality, or projection correctness.

## Verification

This is documentation/routing work. Run:

```bash
git diff --check
```

If you update Markdown tables heavily, also inspect the rendered/table source enough to catch broken row structure.

## Backlog And Status Updates

If the companion lands:

- Mark NW-047 `accepted`.
- Update `docs/status.md` Current Routing to state that operational UI implementation should use the companion before design/code.
- Update `docs/agent-working-surface/README.md` only if the companion becomes part of the default working-surface source order for UI/product slices.
- Do not mark NW-042 through NW-046 accepted or superseded.

If the work discovers that a real server/mobile view-model contract, config display schema, or new operational command authority is needed:

- Leave NW-047 `in_review` or `blocked`.
- Add a bounded successor row for the needed IDR/probe/contract decision.
- Do not smuggle the decision into a companion.

## Commit Boundary

Use one commit if the slice lands cleanly:

```text
docs(ux): define operational product vocabulary boundary
```

The commit should include the companion and tightly related routing updates only.

## Stop And Report

Stop and report if:

- the companion cannot keep UX vocabulary non-authoritative;
- future UI work appears to require a stable runtime view-model/API/config contract before screens can be designed responsibly;
- the analysis needs to modify CDL/IDR/BAR authority rather than routing a decision;
- operational UX concepts cannot be mapped without promoting deferred platform capabilities.
