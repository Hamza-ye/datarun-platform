# Escape Hatch Register

Status: active routing register

Authority: `docs/architecture/adrs-decisions-canonical-ledger/canonical-decision-ledger.md`

This register preserves measured platform evolution paths for future agents. It is not architecture authority, and it does not authorize implementation by itself.

```text
Escape hatches are decided positions with stated revision triggers.
They are not deferred decisions.
They are not TODOs.
They are not permission for agents to implement the escape.
```

Use this file only to classify pressure, confirm that a trigger is being claimed with evidence, and route the work to the required successor decision or bounded implementation plan. If a trigger appears to have fired, stop ordinary implementation and produce evidence: metric, measurement method, deployment scope, current-position impact, and affected CDL rows.

## Register

### EH-001 Projection Engine -> B-to-C Materialized/Read Views

| Field | Value |
|---|---|
| id | EH-001 |
| name | Projection Engine -> B-to-C materialized/read views |
| status | `inactive_until_triggered` |
| current_position | Event log is canonical; projections are derived, rebuildable, and non-canonical. |
| trigger_metric | p95 projection rebuild for one subject greater than 200ms on a reference low-end device with representative subject history, after profiling and local indexing. |
| measurement_method | Profile subject rebuild on the reference device using representative histories; prove lower-impact local indexing/profiling work has already been applied. |
| allowed_change | Add application-maintained materialized/read views beside the event store for performance. |
| forbidden_change | Do not make views canonical; do not patch views as truth; do not bypass append-only event writes. |
| migration_impact | New read-model storage may be introduced, but it must be fully rebuildable from the event stream and repairable by replay. |
| decision_required | Successor implementation plan required if the trigger is measured; no envelope or event-meaning change is implied. |
| source_anchors | CDL-001, CDL-002; `docs/architecture/boundary.md` section 6; `docs/architecture/primitives.md` sections 1-2; `docs/exploration/22-platform-primitives-inventory.md` section 4.2; `docs/implementation/phases/phase-4.md` initial storage posture. |

### EH-002 Scope Resolver -> Persisted Authority Snapshot or Envelope Authority Context

| Field | Value |
|---|---|
| id | EH-002 |
| name | Scope Resolver -> persisted authority snapshot or envelope authority context |
| status | `inactive_until_triggered` |
| current_position | Authority is reconstructed from assignment events/projections; the envelope has no `authority_context` field. |
| trigger_metric | p95 authority reconstruction greater than 50ms/event after lower-impact optimization. |
| measurement_method | Profile authority reconstruction per event at scale and attribute the latency to authority reconstruction after trying assignment projection optimization, server-side assignment cache, indexed authority timeline table, and compact authority snapshot projection. |
| allowed_change | Add a persisted authority snapshot/read model first; consider envelope `authority_context` only if proven unavoidable. |
| forbidden_change | Do not add authority context for code convenience; do not let device-authored authority become canonical; do not add envelope fields without successor CDL authority. |
| migration_impact | Snapshot/read-model paths must remain derived and rebuildable; envelope-impacting paths would create permanent cross-version compatibility work. |
| decision_required | Architecture-grade successor decision required; envelope-impacting if `authority_context` is considered. |
| source_anchors | CDL-006, CDL-012, CDL-032; `docs/architecture/boundary.md` section 6; `docs/architecture/primitives.md` section 5; `docs/implementation/phases/phase-2.md` authority reconstruction risk table; `docs/exploration/22-platform-primitives-inventory.md` Scope Resolver row. |

### EH-003 Identity Resolver -> Actor-Scoped Ordering Metadata

| Field | Value |
|---|---|
| id | EH-003 |
| name | Identity Resolver -> actor-scoped ordering metadata |
| status | `inactive_until_triggered` |
| current_position | Current envelope has `actor_ref`, `device_id`, `device_seq`, and `sync_watermark`; it has no per-actor sequence field. |
| trigger_metric | More than 10% of `conflict_detected` events over 30 days in one production deployment are caused by the same human using multiple devices, and resolution accepts original intent without true conflict. |
| measurement_method | Analyze resolved conflict root causes over a 30-day deployment window; isolate same-actor cross-device ordering cases from genuine conflicts and other concurrency categories. |
| allowed_change | Prefer projection/server metadata for actor-scoped ordering; introduce an envelope field only if a successor decision proves it unavoidable. |
| forbidden_change | Do not reinterpret `device_seq` as actor-global; do not add a per-actor sequence field within ordinary implementation; do not weaken conflict evidence just to suppress flags. |
| migration_impact | Server/projection metadata would be derived and repairable; an envelope field would require permanent compatibility handling for events without that field. |
| decision_required | Successor architecture decision required if the trigger is measured, especially for any envelope field. |
| source_anchors | CDL-006, CDL-007, CDL-012; `docs/architecture/boundary.md` section 6; `docs/exploration/22-platform-primitives-inventory.md` Identity Resolver row; `docs/exploration/archive/09-adr2-phase3-classification-results.md` C4. |

### EH-004 Shape Registry -> Revisit Deprecation-Only Evolution

| Field | Value |
|---|---|
| id | EH-004 |
| name | Shape Registry -> revisit deprecation-only evolution |
| status | `inactive_until_triggered` |
| current_position | Old shape versions remain valid; additive/deprecation-first evolution is the default; breaking changes are exceptional. |
| trigger_metric | More than 3 deployments hit the field budget from deprecated accumulation. |
| measurement_method | Count deployments where deprecated field accumulation exhausts the shape field budget and cannot be addressed by additive/deprecation-only version coexistence. |
| allowed_change | Add migration tooling for deprecated field cleanup, with explicit validation and deployer acknowledgment. |
| forbidden_change | Do not rewrite historical events; do not reinterpret old payloads under new shapes; do not make old valid events invalid because a newer version exists. |
| migration_impact | Tooling may create operational cleanup paths, but historical event interpretation remains tied to the original `shape_ref`. |
| decision_required | Successor platform decision or bounded migration design required if the trigger is measured. |
| source_anchors | CDL-039; `docs/architecture/boundary.md` section 6; `docs/architecture/primitives.md` section 6; `docs/implementation/phases/phase-3.md` IG-7 row; `docs/exploration/archive/18-adr4-session3-part4-remaining-q-resolution.md` CF-1. |

### EH-005 Expression Evaluator -> Revisit L3 Expressiveness Ceiling

| Field | Value |
|---|---|
| id | EH-005 |
| name | Expression Evaluator -> revisit L3 expressiveness ceiling |
| status | `inactive_until_triggered` |
| current_position | Expressions are bounded and non-programmatic; deployers cannot author arbitrary code, functions, or hidden scripts. |
| trigger_metric | The same logical need appears in at least 3 deployments and can be safely expressed as a bounded platform-owned function. |
| measurement_method | Compare deployment requests for repeated logical need, prove it is not one-off domain code, and validate that the function can be budgeted, inspected, and tested across Java and Dart. |
| allowed_change | Add a limited platform-owned function vocabulary that is deploy-time validated, budgeted, and cross-platform tested. |
| forbidden_change | No deployer-authored arbitrary code; no inline scripts; no hidden programming language inside configuration; no unbounded dynamic queries or transformations. |
| migration_impact | Expression syntax and validator/runtime parity would need versioned cross-platform rollout; existing expressions must remain interpretable. |
| decision_required | Successor platform decision required before adding function vocabulary. |
| source_anchors | CDL-038, CDL-043; `docs/architecture/boundary.md` section 6; `docs/architecture/primitives.md` section 7; `docs/exploration/archive/13-adr4-session1-scoping.md` configuration/code boundary and expressiveness ceiling; `docs/exploration/22-platform-primitives-inventory.md` Expression Evaluator row. |

### EH-006 Pattern Registry -> Expand Platform-Fixed Pattern Inventory

| Field | Value |
|---|---|
| id | EH-006 |
| name | Pattern Registry -> expand platform-fixed pattern inventory |
| status | `inactive_until_triggered` |
| current_position | Deployers select and parameterize platform-fixed patterns; they do not author state machines. |
| trigger_metric | More than 2 deployments request custom state machines matching no existing pattern. |
| measurement_method | Classify deployment requests against existing pattern definitions; prove at least 3 requests need the same or clearly related platform-owned pattern gap rather than activity-level parameterization. |
| allowed_change | Add new platform-fixed pattern definitions and tests. |
| forbidden_change | Do not allow deployer-authored transition tables, state machines, or workflow engines in configuration; do not create mutable workflow state as truth. |
| migration_impact | New platform pattern definitions must be delivered through the established pattern registry/config package path and remain projection-derived. |
| decision_required | Successor platform decision or bounded pattern-addition plan required if the trigger is measured. |
| source_anchors | CDL-047, CDL-049; `docs/architecture/boundary.md` section 6; `docs/architecture/primitives.md` section 11; `docs/exploration/22-platform-primitives-inventory.md` Pattern Registry row; `docs/exploration/28-pattern-inventory-walkthrough.md`. |

## Future Extraction Seams Are Not Escape Hatches

Datarun is organized as a modular platform kernel with architectural capability areas, not as DDD bounded contexts by default. DDD may be applied at deployer-domain and future team-boundary layers. The initial deployment architecture is a disciplined modular monolith with explicit escape hatches for measured evolution.

Potential future extraction seams are advisory routing context only. They are not active work, they are not escape-hatch triggers, and they do not permit service extraction by ordinary implementation.

| Future seam | Extract only when |
|---|---|
| Sync service | Sync traffic or load scales independently from admin/config. |
| Projection workers | Rebuild/materialization load dominates request paths. |
| Config publishing service | Deployment config lifecycle becomes independently owned. |
| Reporting warehouse | Analytics requires a different storage/query model. |
| Identity/resolution service | Resolver workflow becomes operationally complex. |
| Pattern/policy engine | Workflow execution scales independently from capture/sync. |

Do not casually extract the event envelope, event store semantics, shape/version model, or flag semantics. Those are platform kernel contracts.
