# Authorization Visibility Boundary Control

Status: Assessed boundary-routing input already routed into the baseline/gap path

This document records how `../../access-control-scenario.md` creates authorization and visibility pressure without silently rewriting ADR-003 or becoming a deployer-authored access-control system. Durable authority and routing belong in `05` and `07`.

The core rule is: the access-control scenario states what must remain possible across roles, context, hierarchy, temporary authority, and offline operation. ADR-001 through ADR-005 define the accepted mechanisms currently allowed to satisfy that pressure. Anything not closed by those mechanisms must route through the gap register and change control before it affects specification language.

## Source Basis

Primary inputs:

- `../00-extraction-state.md`
- `../10-adr1-5-rest-state-closure-register.md`
- `02-change-control.md`
- `04-architecture-baseline-v0.md`
- `05-decision-gap-register.md`
- `07-system-boundary-map.md`
- `09-identity-boundary-control.md`
- `15-conflict-flag-offline-boundary-control.md`
- `16-operational-constraints-boundary-control.md`
- `../pre-operations/04-accepted-pre-specification-decisions.md`
- `../../access-control-scenario.md`

Lineage context:

- `../00-extraction-state.md` Iteration 1 processed `../../access-control-scenario.md` inside the approved ground-truth source boundary.

## Authority Rule

`../../access-control-scenario.md` is authoritative for operational pressure:

- visibility and authority depend on actor, role, place, activity, subject, and time
- authority is contextual rather than absolute
- access can be temporary
- role and responsibility changes must preserve attribution
- hierarchy-based visibility has exceptions
- access rules grow more nuanced over time
- offline devices may enforce last-known rules while central authority has already changed

It is not authority for:

- adding event-envelope fields
- storing immutable `authority_context`
- making user groups, identity-provider claims, or tenant fields direct authority sources
- deployer-authored arbitrary access-control logic
- field-level sensitivity
- changing sync from event-scoped, append-only, idempotent delivery
- changing projection-derived authority into stored canonical authority
- rejecting validly structured offline work solely because later central authority changed

## Baseline Reading

The accepted baseline already closes these mechanisms:

- access is assignment-derived
- sync scope is access scope
- authority is projection-derived
- authorization is reconstructed from actor, subject/process references, assignment timeline, event creation context, and sync knowledge state
- authorization checks use the original subject reference written into the event
- no immutable `authority_context` is stored in the event envelope
- scope expansion is additive
- scope contraction uses selective retain as ADR-003's initial strategy
- sensitive deployments require stronger local lifecycle handling than retain-and-hide

The access-control scenario should therefore constrain future controlled work by naming pressure and gaps. It should not be used to reopen those closed rules unless a claim passes `02-change-control.md`.

## Pressure-To-Responsibility Mapping

| Scenario Pressure | Accepted Baseline Mechanism | Primary Responsibility Area | Open / Deferred Routing |
|---|---|---|---|
| role and context determine visibility/action | assignment-derived access; bounded roles, scopes, activities, and policy choices | Assignment / Authority / Sync | permission table details; activity/context authority details |
| same role has different authority in different activities or areas | authority projection from actor, assignment timeline, references, and event context | Assignment / Authority / Sync | subject-based scope; assessment visibility; cross-level visibility |
| temporary authority and cover | assignments and role/scope projections over time | Assignment / Authority / Sync | temporary authority, revocation, and offline grace reconciliation |
| emergency or campaign authority expansion | additive scope expansion; sync-visible assignment/config changes | Assignment / Authority / Sync | bulk grant/revoke workflow; stale local authority handling |
| role change or handoff | immutable event authorship plus projection-derived authority | Assignment / Authority / Sync | onboarding, role transition, and handoff policy |
| hierarchical visibility with exceptions | platform-fixed scope mechanisms plus deployer-configured scope instances | Assignment / Authority / Sync; Configuration | auditor access; cross-level distribution visibility |
| auditor or cross-regional visibility | explicit gap for subject-based scope and auditor access | Assignment / Authority / Sync | formal decision if new scope semantics are required |
| offline local enforcement may differ from central authority | local last-known state plus sync-time reconciliation | Assignment / Authority / Sync; Flag / Resolution | grace-period policy; authorization anomaly surfacing |

## Classification Rules

Use these classifications during later review:

| Claim Type | Classification | Allowed Handling |
|---|---|---|
| accounts authenticate, actors author, assignments authorize | consistent elaboration | Carry forward from `../pre-operations/04-accepted-pre-specification-decisions.md`. |
| role, scope, activity, time, and subject/context all influence authority | consistent elaboration plus future specification detail | Specify as inputs to the authority projection without adding envelope fields or stored authority snapshots. |
| concrete permission tables, role names, and deployer policy values | future specification detail or operational policy | Keep bounded by platform-owned mechanisms and configuration limits. |
| product role labels used as UI lenses or scenario shorthand | product clarification plus future specification detail | Preserve as operating-context language; do not create fixed actor classes or direct authority shortcuts. |
| auditor access, subject-based scope, or cross-level visibility exceptions | open-gap closure candidate | Close through the named gap if expressible with existing scope mechanisms; require formal decision for new scope semantics. |
| temporary grants, revocation, and offline grace behavior | open-gap closure candidate | Route through the temporary authority/offline reconciliation gap before authorization/sync specification if needed now. |
| user group or identity-provider claim directly grants data access | conflict or new unauthorized claim | Reject unless formal change control changes the baseline. |
| authority snapshot stored on each event | conflict with closed baseline | Reject unless formal reopen accepts stored `authority_context`. |
| field-level sensitivity | conflict with closed baseline | Reject unless formal reopen changes ADR-004. |
| arbitrary deployer access-control code | conflict with closed baseline | Reject unless formal reopen changes the configuration boundary. |

## Offline Authorization Effect

Offline authorization creates two enforcement points that may temporarily disagree:

- local device enforcement over last-known assignments, configuration, scoped projections, and local session state
- server/sync enforcement over later central knowledge, assignment changes, revocations, identity changes, and received events

If this material is later reused:

- local enforcement must be described as last-known and scoped, not globally authoritative
- sync-time authorization must preserve immutable event history and attribution
- late authorization problems should surface as anomalies or flags unless the operation class is explicitly online-only or structurally invalid
- downstream policy, trigger, workflow, or irreversible effects must respect detect-before-act at the boundary that owns those effects
- local purge or removal after scope contraction must route through Local Data Lifecycle and must not mutate central canonical events

Do not use offline disagreement as a reason to store immutable authority snapshots. The baseline rejects that path; authority remains reconstructed from durable facts.

## Mechanism / Instance Split

Scope and authority have both platform-owned and deployer-configured parts:

- platform-owned mechanism: assignment-derived access, fixed scope containment semantics, sync scope as access scope, authority projection inputs, and original-reference authorization checks
- deployer-configured instances: concrete roles, assigned actors, areas, teams, activities, schedules, thresholds, sensitivity classifications, and policy values within bounded configuration

Future controlled specification work must not mix these into one "access-control config language." Deployer configuration supplies instances and policy values. The platform owns the authority mechanism and its limits.

Operational actor labels sit on the instance/product side of this split. `Supervisor`, `Coordinator`, `Auditor`, `Reviewer`, and similar names may be deployer labels, scenario shorthand, or UI lenses; they are not platform-owned actor subclasses unless a later formal decision creates such a mechanism. Future authorization material should name the authority inputs and operation class it requires rather than relying on a persona label.

## Future Readiness Checks

Before accepting authorization, sync, reporting, trigger, workflow, or local-lifecycle behavior from this material:

1. Is the claim a closed ADR-001 through ADR-005 rule, a pre-operations accepted decision, an access-control pressure, or an open gap?
2. Does it preserve assignment-derived access and sync scope as access scope?
3. Does it keep authentication, actor authorship, assignment authority, and device identity separate?
4. Does it require a new scope type, auditor path, subject-based scope, or cross-level visibility rule?
5. Does it define temporary authority, revocation, or grace-period behavior beyond existing assignment projection?
6. Does it change how sync/projection handles work created under stale local authority?
7. Does it add an envelope field, stored authority snapshot, field-level sensitivity, or arbitrary deployer access logic?
8. Does reporting or aggregation respect access/sync scope rather than becoming an authority shortcut?
9. Does local lifecycle preserve immutable central history while handling sensitive scope contraction?

## Baseline Impact

No ADR-001 through ADR-005 baseline item should be changed by this assessment.

This assessment added one gap-register clarification: temporary authority, revocation, and offline grace reconciliation should be explicit before authorization/sync behavior is accepted if the work needs to describe temporary grants or late revocation behavior.

## Historical Follow-Up Note

Use the routed findings through `05` and `07`, with `../pre-operations/04-accepted-pre-specification-decisions.md` as assessed source material where relevant.

Hold back unless explicitly needed:

- new scope types
- auditor access semantics
- subject-based scope
- cross-level distribution visibility
- group-managed authorization
- external identity-provider authority
- shared-device multi-actor sessions
- full temporary-grant workflow
- exact offline grace-period policy
