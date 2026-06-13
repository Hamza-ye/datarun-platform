# Gap Routing Playbook

Status: active routing surface

This is the active working-surface version of the `013` routing layer. Use it before creating new decisions, platform specs, implementation prompts, policies, or product/problem artifacts.

## Core Routing Rule

```txt
pressure or proposal
-> affected vocabulary
-> owning DEC anchor
-> negative boundaries
-> classification
-> closure path
-> bounded artifact
```

Do not implement directly from pressure. Route first.

## Vocabulary Guardrails

Use vocabulary as a routing aid, not a new authority surface.

| Term or path | Anchor | Classification | Route |
|---|---|---|---|
| activity role-action | DEC-AUTH-01, DEC-CONFIG-08, DEC-WORKFLOW-04 | Structural contract | Current activity work-action vocabulary is `capture`, `review`, `alert`, `task_created`, and `task_completed`. |
| `assignment_changed` as activity action | DEC-AUTH-03, DEC-EVENT-04 | Negative boundary | `assignment_changed` remains assignment administration, not activity work. |
| `assignment_admin.create` / `assignment_admin.end` | DEC-AUTH-04, DEC-CONFIG-08 | Strategy-protecting service | Platform-owned command capabilities outside `activities[*].roles`. |
| principal binding | DEC-CONFIG-03, DEC-AUTH-03 | Strategy-protecting service | Production auth maps provider principals only through explicit `(issuer, subject) -> actor_id` bindings. |
| IdP group/claim/JWT `actor_id` authority | DEC-BOUNDARY-01, DEC-AUTH-03 | Negative boundary | Not platform authority without successor decision. |
| operational/persona labels | DEC-BOUNDARY-01, DEC-AUTH-01, DEC-AUTH-03 | Product/UX vocabulary | Labels such as coordinator/setup owner, field user, supervisor/reviewer, operator/admin, support role, and auditor are acting-context lenses only. They must not become actor identity categories, authority primitives, fixed product modules, config namespaces, or implementation boundaries. Describe authority through actor + active assignment + role + scope + time + activity/context. |
| S01 subject-linked capture vs S06/entity lifecycle | DEC-IDENTITY-01, DEC-IDENTITY-03, DEC-PROJECTION-01, DEC-CONFLICT-01, DEC-AUTH-01 | Product/spec routing boundary | S01-compatible work may link records to known subjects and carry unpromoted missing-known-thing/candidate artifacts. S06/entity lifecycle covers maintained known things, discovered-unit lifecycle, active/inactive/retired truth, registry stewardship, and merge/split UX; it remains BAR-105/S06 successor work before implementation. |
| platform payload schema | DEC-CONFIG-01, DEC-CONFIG-04 | Structural contract | `contracts/shapes/*.schema.json` are platform payload contracts, not deployer shape rows. |
| config package schema | DEC-CONFIG-08, DEC-CONFIG-04 | Structural contract | Server-emitted/mobile-consumed wire package contract; unknown top-level keys remain tolerated. |
| `pattern_definitions` | DEC-WORKFLOW-02, DEC-CONFIG-08 | Configuration artifact | Referenced platform pattern definitions delivered in atomic config packages. |
| `designated_resolver` | DEC-CONFLICT-03, DEC-CONFLICT-04 | Strategy-protecting service | Canonical resolution requires exact designated-resolver equality. |
| `resolver_unassigned` | DEC-CONFLICT-03 | Negative boundary | Explicit no-human-route sentinel, not fallback authority or reassignment. |
| subject-history backfill | DEC-AUTH-02, DEC-AUTH-05, DEC-PROJECTION-01 | Strategy-protecting service | Separate authorized repair surface with independent cursor and no normal watermark mutation. |
| shared-device actor session | DEC-AUTH-02, DEC-AUTH-05, DEC-PROJECTION-01 | Strategy-protecting service | Exactly one active server-resolved actor session; mutable local state is actor-partitioned. |
| `context.*` | DEC-CONFIG-06, DEC-WORKFLOW-05 | Initial strategy / closed platform vocabulary | Current accepted refs are the seven IDR-018 form-context properties. Unknown `context.*` refs are deploy-time invalid; additions require platform evolution and must remain platform-fixed, read-only, pre-resolved, and non-query-like. |
| auto-resolution execution | DEC-WORKFLOW-07, DEC-CONFLICT-03 | Policy surface | Mechanism class is accepted, but runtime execution is deferred to successor policy/trigger work. |

Term collisions to keep explicit:

* `assignment`: identity category, authorization grant, assignment timeline, or `assignment_changed` event.
* `activity`: `activity_ref`, activity definition, activity scope, or activity instance.
* `role`: assignment role, activity action role, IdP role claim, product job title, or persona label.
* `type`: envelope type, identity type, field type, or flag category.
* `state`: projection state, identity lifecycle, pattern state, or rejected stored `current_state`.

## Allowed Classifications

| Classification | Use when | Closure path |
|---|---|---|
| Product/problem evidence gap | User journey, operational behavior, deployment archetype, or acceptance pressure is not understood enough to specify. | Product discovery or scenario thickening. |
| Architecture decision gap | The proposal changes structural contracts, event semantics, identity references, sync/access behavior, authorization authority, workflow state truth, trigger behavior, or deployer configuration boundaries. | Formal architecture decision / CDL-successor decision. |
| Platform-spec detail gap | The decision boundary is accepted, but exact product/platform behavior needs specification. | Platform-spec detail artifact. |
| Implementation/tooling gap | The work concerns storage, APIs, UI, build mechanics, test harnesses, or tooling without changing accepted semantics. | Engineering design, implementation prompt, or spike. |
| Operational policy gap | The work defines human process, support, retention, governance, rollout, or review policy without changing platform semantics. | Operational policy artifact. |

## Artifact And Closure Trace

The classification determines the durable output. Prompts and NW rows route
work; they do not replace the output.

| Classification | Durable output | Acceptance trace |
|---|---|---|
| Product/problem evidence gap | Scenario, user-fit/research note, validation result, or bounded routing artifact. | NW exit links the evidence and states what remains unvalidated. |
| Architecture decision gap | CDL successor decision, or an IDR only within delegated architecture bounds. | NW exit links the decision and names contracts/BAR/DEC routes affected. |
| Platform-spec detail gap | `docs/specifications/platform/`; use `docs/specifications/product/` when the accepted behavior is user-visible. Use `artifacts/` only while the result remains non-binding exploration. | NW exit links the indexed specification, acceptance criteria, and implementation successor if needed. |
| Implementation/tooling gap | Bounded prompt plus code, tests, contracts, and implementation-boundary updates actually changed by the work. | NW exit records commit/runtime evidence and any residual route. |
| Operational policy gap | `docs/operations/policies/` for choices, `docs/operations/runbooks/` for procedures, and `docs/operations/rehearsals/` for plans/evidence. Use `artifacts/` only for the non-binding map that selects them. | NW exit links the indexed policy/procedure and rehearsal evidence; stronger readiness claims require executed evidence. |

For every promoted NW item:

```txt
pressure and authority
-> backlog row
-> bounded prompt when non-trivial
-> correctly classified durable output
-> verification/evidence
-> backlog acceptance or explicit deferral
-> fold-forward update only to the active surfaces materially changed
```

If analysis selects implementation, create a successor row. Do not mutate an
exploration row into implementation or leave the recommendation only inside a
prompt. If the durable output has no established home, the analysis must name
and justify one before implementation begins. Follow
`docs/documentation-organization.md` for naming, headers, indexes, lifecycle,
and supersession.

## Architecture Escalation Triggers

Escalate to architecture when a proposal:

* adds or implies new envelope fields or event `type` values;
* changes stored event meaning or historical interpretation;
* adds durable workflow-state authority;
* changes sync/access scope or creates new scope mechanisms;
* rewrites normal sync watermarks or turns live sync into historical pull;
* moves authority, state, resolver truth, or actor identity into a new durable source;
* treats IdP groups, claims, roles, or JWT `actor_id` as platform authority;
* lets deployer config author access logic, containment functions, state machines, arbitrary code, or device-side triggers;
* implements runtime auto-resolution, resolver reassignment, emergency override semantics, or broad audit/history access without a successor decision;
* weakens deploy-time validation, config-package atomicity, or the mechanism/instance split.

## Stable Routing Rules

### Irreversibility Filter

Ask:

1. Does this change stored truth or historical interpretation?
2. Does this create permanent compatibility or dual semantics?
3. Does this move platform authority into a new source?

If yes, route to architecture. If no, classify as platform-spec, implementation/tooling, operational policy, or product/problem evidence.

### Configuration Guardrails

Allowed configuration work stays inside platform-fixed mechanisms and deployer-authored instances. Reject or escalate config that becomes arbitrary code, access-control logic, resolver authority, state-machine authoring, device-side trigger execution, or unvalidated dependency cascade.

Configuration Anti-pattern catalog:
The configuration boundary exists to support “set up, not built” without creating a second programming platform inside deployment config.

| Anti-pattern | Smell | Review response |
|---|---|---|
| Config-as-code | Deployer config needs loops, functions, scripts, recursion, or custom execution order. | Route to platform mechanism or reject. |
| Vocabulary creep | Deployer asks for new event types, action types, scope types, or state names as ordinary config. | Keep structural vocabulary platform-owned. |
| Implicit coupling | One config artifact silently depends on another without declared reference. | Require explicit dependency and deploy-time validation. |
| Version coupling | Old events become invalid when config changes. | Preserve versioned interpretation or migrate explicitly. |
| Ghost dependencies | Broken config is discovered on device or at runtime. | Fail deploy-time validation. |
| Complexity blind spots | Large shapes/rules/triggers become unreviewable but still deploy. | Enforce budgets and warnings. |
| Inner-platform effect | Activity or pattern config becomes a deployer-authored processing pipeline. | Stop and route to platform evolution. |

### Device/Server Evaluation Contract

Server evaluation is authoritative for acceptance, conflict detection, resolver authority, assignment administration, production auth, and L3 policy.

Device evaluation is advisory for UX and projection repair. Device logic may warn, hide, stage, or project, but must not become source authority or reject structurally valid policy/state anomalies that the platform should accept and flag.

### Escape Hatch Rule

Use `docs/agent-working-surface/escape-hatch-register.md` only when a documented B-to-C escape hatch has been explicitly activated. Pressure alone does not activate an escape hatch.

## Do-Not-Promote List

Do not promote these into authority without the named route:

* UI vocabulary;
* scenario labels;
* implementation table/API names;
* IdP claims or groups;
* mobile-selected actor identity;
* config authoring syntax;
* local retention mechanics;
* reporting aggregates;
* broad audit/history access;
* emergency override semantics.

## Implementation Prompt Checklist

Before implementation, the task packet should state:

* goal;
* files to read, capped and justified;
* authority and guardrails;
* forbidden work;
* expected boundary;
* targeted tests;
* commit role/sequence and acceptance boundary;
* stop-and-report conditions.

## Test Seed Backlog

When a route changes behavior, prefer tests that pin:

* envelope vocabulary and schema parity;
* projection rebuildability/equivalence;
* identity merge/split/alias ordering;
* scope-filtered sync and subject-history separation;
* assignment containment and command capability;
* config package contract and deploy-time validation;
* pattern projection and transition-violation behavior;
* resolver equality and unauthorized-resolution handling.

## Known Gap Register

last-reviewed: 2026-06-13

| Gap ID | Gap | Status | Classification | Current route |
|---|---|---|---|---|
| GAP-AUTH-01 | Auditor/query access | Partially constrained; broad forms deferred | Architecture decision gap | Simple current-scope auditor visibility remains ordinary assignment/config posture. Broad audit/history read surfaces, redacted/no-local-retention views, dynamic auditor scope, and emergency/special write bypasses require concrete product/security decision before implementation. |
| GAP-PROJECTION-01 | Aggregate access semantics | Open | Architecture decision gap if aggregates bypass event access; platform-spec detail if they inherit event access | Reporting/aggregate work should route through NW-044 or a bounded reporting spec; aggregate visibility outside event-level access requires formal decision. |
| GAP-AUTH-02 | Actor-as-subject delivery rule | Open/future decision | Architecture decision gap | Route with subject/query/custom-scope pressure; do not add a non-assignment sync dimension without formal decision. |
| GAP-AUTH-03 | Activity role-action table artifact | Accepted baseline for current coarse action model | Reference-only / future finer granularity route | Current model is accepted; only future finer role-action granularity remains a route. |
| GAP-WORKFLOW-01 | Pattern Registry inventory | Partially accepted | Platform-spec detail gap | Canonical contract and delivery are accepted. Additional platform pattern definitions route as platform-spec/platform-evolution work under Pattern Registry boundaries. |
| GAP-WORKFLOW-02 | Pattern migration mechanics | Open | Platform-spec detail gap / implementation tooling gap | Specify migration only when concrete compatibility pressure appears; do not create durable workflow-state tables. |
| GAP-WORKFLOW-03 | Additional `context.*` values | Current baseline fixed by NW-057; future additions open | Architecture decision gap or platform-spec detail gap | Current refs are closed to the seven IDR-018 properties and unknown `context.*` refs are deploy-time invalid. New values must stay platform-fixed, read-only, pre-resolved, and bounded; architecture review required if expression authority changes. |
| GAP-WORKFLOW-04 | Additional auto-resolution policies | Deferred | Architecture decision gap / policy surface | Route through BAR-102/NW-045. Preserve exact designated-resolver equality and avoid direct flag mutation. |
| GAP-CONFLICT-01 | Flag queue ergonomics | Open | Platform-spec detail gap / implementation tooling gap | UX may improve queue behavior without changing flag semantics, resolver authority, or canonical resolution. |
| GAP-CONFLICT-02 | Domain conflict automation and batch resolution | Future decision | Architecture decision gap / platform-spec detail gap | Route through NW-045. Batch/automation must emit per-flag resolution events and preserve resolver equality. |
| GAP-CONFLICT-03 | Resolver-steward eligibility policy | Open; current runtime has role-name fallback heuristic | Platform-spec detail gap; architecture decision gap if a new resolver capability or authority primitive is introduced | IDR-026 accepts nearest eligible human steward and exact resolver equality. Current code first uses configured review action where possible, then falls back to role-name substrings such as admin/supervisor/coordinator/reviewer/manager/lead/resolver. Future conflict/admin UX or production hardening must not treat that fallback as product-ready authority; define explicit steward eligibility or route a successor decision before changing resolver authority. |
| GAP-CONFIG-01 | Config authoring syntax | Open | Implementation/tooling gap | Tooling format may evolve if it preserves config-package and shape-format contracts. |
| GAP-CONFIG-02 | Setup lifecycle for new operational activity | Open | Platform-spec detail gap / operational policy gap | Define draft/validate/review/approve/publish workflow under config-package boundaries. |
| GAP-PROJECTION-02 | Reporting freshness semantics | Open | Platform-spec detail gap | Define freshness, completeness, unresolved-flag handling, and drilldown under access constraints. |
| GAP-SYNC-01 | Handoff package contents | Open | Platform-spec detail gap | Define handoff contents under subject-history, sync, projection, and actor-partition constraints. |
| GAP-RETENTION-01 | Retention windows | Future decision | Operational policy gap / architecture decision gap if event/sync semantics change | Route through NW-054. Do not delete server event history or rewrite normal sync watermarks. |
| GAP-RETENTION-02 | Worker offboarding / exit procedure | Future decision | Operational policy gap | Route with retention/security policy; preserve assignment-derived access and shared-device sealing rules. |
| GAP-RETENTION-03 | Regulatory encryption/redaction/erasure | Future decision | Architecture decision gap / operational policy gap | Separate local retention/security from immutable server event truth; deletion/redaction requires formal authority. |
| GAP-PRODUCT-01 | Multi-tenant naming strategy | Open | Product/problem evidence gap / platform-spec detail gap | Requires concrete deployment archetype pressure before specification. |
| GAP-CONFIG-03 | Complexity budget changes | Open | Platform-spec detail gap / architecture decision gap if guardrails weaken | Adjust only with validation evidence; weakening deploy-time guardrails requires architecture review. |
| GAP-AUTH-04 | Cross-activity cohort materialization | Open/future decision | Architecture decision gap | Any subject/query/custom scope beyond accepted assignment axes requires formal decision. |
| GAP-AUTH-05 | Cross-activity subject access for a second actor | Open/future decision | Architecture decision gap | Do not bypass assignment-derived access or create hidden sync scope. |
| GAP-SCENARIO-01 | Scenario phasing and product/spec follow-through | S00/S19/S21/S22/S23/S26/S27 accepted as runtime probes | Product/problem evidence gap / platform-spec detail gap | Accepted probes NW-025/S19, NW-026/S00, NW-029/S21, NW-030/S27, NW-032/S23, NW-033/S26, and NW-042/S22 provide evidence but do not add new primitives. Product/spec work should cite exact NW evidence and preserve no-new-primitive constraints. |
