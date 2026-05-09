# Envelope, Shape, And Parametrization Boundary Control

Status: Atomization-ready extraction and control overlay

This document records the focused ADR-004 lineage for `shape_ref`, envelope `type`, platform-fixed structure, and deployer parametrization. It is not a new architecture decision and does not supersede any ADR.

The purpose is to prevent a recurring drift pattern: treating operational facts, workflow labels, role labels, or product surfaces as new envelope types, platform classes, or core architectural boundaries when the accepted model routes them through shapes, references, patterns, configuration, projections, and assignments.

## Source Basis

Lineage sources, used as historical evidence only:

- `../../exploration/archive/14-adr4-session2-scenario-walkthrough.md`
- `../../exploration/archive/15-adr4-session3-part1-structural-coherence.md`
- `../../exploration/archive/16-adr4-session3-part2-irreversibility-filter.md`
- `../../exploration/archive/17-adr4-session3-part3-adversarial-stress-tests.md`
- `../../exploration/archive/18-adr4-session3-part4-remaining-q-resolution.md`

Accepted closure sources:

- `../../adrs/adr-004-configuration-boundary.md`
- `../../adrs/adr-005-state-progression.md`
- `04-architecture-baseline-v0.md`
- `05-decision-gap-register.md`
- `07-system-boundary-map.md`

Later assessment guardrails, used only as classified assessment material:

- `11-adr007-envelope-type-assessment.md`
- `12-adr008-reference-fields-assessment.md`
- `13-adr009-duality-rule-assessment.md`
- `14-pattern-inventory-walkthrough-assessment.md`

Related product controls:

- `../product-alignment/09-first-vertical-slice.md`
- `../product-alignment/10-atomization-readiness-from-product.md`
- `../product-alignment/11-alignment-closeout.md`

## Extraction Pass Result

### Pass 1: Session 2 introduced the split

Session 2 tested configuration through scenario walkthroughs. The important finding was not a domain taxonomy. It was the orthogonal split:

| Axis | Extracted pressure | Direction found |
|---|---|---|
| Envelope `type` | How the platform routes event processing | Platform-fixed structural vocabulary |
| `shape_ref` | Which payload schema and fact version an event carries | Mandatory shape/version reference |
| `activity_ref` | Which operational activity produced the event | Optional activity-instance reference |
| Configuration | How deployers assemble operations | Bounded parameterization over platform mechanisms |

The campaign walkthrough forced `activity_ref`: the same `shape_ref` can appear in multiple concurrent activities, so activity attribution cannot be inferred reliably from shape, actor, subject, or time.

### Pass 2: Structural coherence narrowed the model

Session 3 Part 1 found two lifecycle models:

- event-coupled: shapes, because events permanently carry `shape_ref`
- config-package: activities, logic rules, triggers, projection rules, and campaigns, because they are deployed as configuration and affect future behavior

It also clarified that:

- `type` and `shape_ref` are not redundant
- target routing belongs in payload/configuration/projection, not a new envelope field
- source-event links belong in payload where the relevant shape needs them, not a universal envelope field
- patterns are platform-fixed skeletons that deployers select and parameterize
- participant-role words such as capturer, reviewer, coordinator, sender, or receiver describe capacities inside a pattern, not platform actor subclasses

### Pass 3: Irreversibility filtering identified the hard surface

Session 3 Part 2 classified three envelope-touching positions as Tier 1 structural constraints:

| Position | What became hard |
|---|---|
| `shape_ref` | Mandatory envelope field, `{shape_name}/v{version}`, constrained shape names, integer versions |
| `activity_ref` | Optional envelope field, deployer-chosen activity instance identifier, null allowed |
| `type` | Platform-fixed, append-only structural event type vocabulary |

Most configuration architecture was not Tier 1. It remained evolvable strategy or strategy-protecting constraint.

### Pass 4: Adversarial stress preserved the line

Session 3 Part 3 attacked the model and did not require envelope changes:

- Same-shape multi-activity pressure confirmed `activity_ref`, but kept it optional because imports and unknown provenance need null.
- Breaking shape changes did not break `shape_ref`; they exposed projection and migration policy as separate implementation/spec questions.
- Requested domain event names such as case opened, feedback, referral accepted, and stock received mapped to `capture` plus domain-specific shapes unless a distinct platform processing behavior was proven.
- `status_changed` remained a possible ADR-005 question, not an ADR-004 addition.

The key rule emerged here: a new envelope `type` is justified only by different platform processing behavior. Domain meaning, lifecycle meaning, activity context, and role context do not justify new envelope type values by themselves.

### Pass 5: ADR-004 closed the decision

ADR-004 correctly closed the architecture:

- S1: every event carries `shape_ref`
- S2: events may carry optional `activity_ref`
- S3: `type` is a six-value platform-fixed processing vocabulary
- S9: deployers work through a four-layer configuration gradient
- S10: shapes are typed, versioned payload schemas
- S14: deployer policy values sit over platform-owned mechanisms

ADR-004 also explicitly deferred state progression, `status_changed`, domain conflict resolution automation, pattern inventory, projection merge strategy, migration tooling, schema tooling, authoring format, and validator UX.

### Pass 6: ADR-005 reinforced the closure

ADR-005 added no envelope fields and no structural event types. It rejected `status_changed` for the explored workflow surface because state transition significance was expressible through shape plus pattern, with workflow state derived in projections.

ADR-005 also closed the Pattern Registry mechanism as platform-fixed and deployer-parameterized, while leaving exact pattern inventory and formal schema as platform-spec gaps.

## Closure Assessment

ADR-004 closed the core decision correctly. The weakness was not the decision itself. The weakness was that later readers had to assemble the interpretation from several places:

- ADR-004 S3 for `type`
- ADR-004 S1/S10 for `shape_ref`
- ADR-004 S2 for `activity_ref`
- ADR-005 S5/S6 for patterns
- later assessments for `type` vs. `shape_ref`, reference vs. referent, and mechanism vs. instance

That spread makes the line easy to misread during atomization. In particular, the word `review` appears at multiple layers:

- `type=review`: envelope processing path for a human judgment event
- `capture_with_review`: pattern skeleton for submit/review/return/accept flow
- review queue: projection/product surface
- reviewer or supervisor: configured capacity or operational label

Only the first is an envelope type. Only the second is a platform-fixed pattern mechanism. The others are projections, product translations, configuration bindings, or operational labels.

## Drift Risks Found

The current baseline and product-alignment edits mostly preserve the line. The risk is not that the architecture has already hard-coded the wrong model. The risk is that atomization agents may collapse the axes unless the control is explicit.

High-risk misreads:

- Treating event `type` as a domain-event taxonomy.
- Treating `review` as one core subsystem or reviewer class because it appears as an envelope type, pattern behavior, queue, and role label.
- Treating `shape_ref` as a workflow or authority marker instead of a payload schema/version reference.
- Treating `activity_ref` as authorization, pattern identity, tenant/deployment identity, or immutable authority context.
- Treating platform-fixed mechanisms as deployer-authored config.
- Treating deployer labels such as supervisor, coordinator, auditor, reviewer, or field worker as platform-owned actor subclasses.
- Treating product queues or work items as canonical storage primitives.

## Clean Model

The atomization-safe model is four orthogonal axes:

| Axis | Owns | May vary by deployer? | Extension path |
|---|---|---|---|
| Envelope `type` | Platform processing pipeline | No | Architecture-grade/platform code change |
| `shape_ref` | Payload fact schema and version | Yes, through shape registry rules; some shapes may be platform-bundled | Shape addition/versioning, or platform evolution for bundled shapes |
| References | Subject, actor, activity, device, causal identity contracts | Referent values vary; field contracts do not | Architecture-grade for new envelope fields/categories |
| Configuration / parameterization | Activities, assignments, pattern bindings, roles, scopes, thresholds, severities, deadlines, policy values | Yes within bounded mechanisms | Deployer configuration; platform evolution for new mechanisms |

The boundary is behavioral, not persona-based:

- offline-capable operation
- online or coordination-required operation
- offline-with-constraints operation that may need warnings, constrained authority, or sync-time reconciliation
- configuration/control-plane operation

Operational role labels can describe who commonly performs an operation. They do not define the operation's architectural class.

## Control Rules

### C1: Type Is Processing Behavior

Use an envelope `type` only for platform processing behavior. A proposed type addition must answer:

1. What different pipeline handles this event?
2. Why can shape, payload, activity context, actor identity, pattern state, or projection logic not express the difference?
3. Which existing type cannot process it without inspecting domain payload?
4. What permanent parser/projection obligation is created?

If those answers are not clear, do not add a type.

### C2: Shape Ref Is Fact Schema

Use `shape_ref` to identify what payload fact is carried and which version of its schema applies.

Do not use `shape_ref` to encode:

- who authored the event
- who may see it
- which product surface displays it
- whether it is online-only
- current workflow state
- operational role labels
- tenant/deployment identity

Those belong to references, assignments, configuration, projections, sync scope, or product translation.

### C3: Activity Ref Is Context, Not Authority Snapshot

Use `activity_ref` to preserve the activity instance context that produced an event.

Do not use `activity_ref` as:

- immutable `authority_context`
- pattern reference
- deployment or tenant reference
- substitute for assignment-derived access
- product queue identity

Authority remains projection-derived from actor, assignment timeline, scope, activity/context, subject references, and sync knowledge state.

### C4: Review Must Stay Layered

When a spec mentions review, classify which layer is meant:

| Review usage | Correct layer |
|---|---|
| `type=review` | Event Envelope / Schema processing vocabulary |
| Review decision payload | `shape_ref` fact schema |
| `capture_with_review` | Pattern Registry mechanism |
| Review queue/status | Projection / Workflow State and product surface |
| Reviewer/supervisor label | Configuration binding, assignment, role, or operational lens |

Do not turn the word `review` into a fixed actor class, service boundary, canonical work item, or extra type value.

### C5: Platform-Fixed And Deployer-Configured Must Be Split

Every atom that mentions a platform-fixed mechanism and deployer-authored instances must split them.

Examples:

- Pattern Registry mechanism: platform-fixed.
- Activity selecting `capture_with_review`: deployer configuration.
- Scope containment semantics: platform-fixed.
- Concrete scope assignments: deployer configuration.
- Structural event types: platform-fixed.
- Shape names and versions: deployer-defined unless platform-bundled.

### C6: Product Surfaces Are Not Canonical Storage

Queues, assigned work, review lists, oversight counts, stale/pending labels, and returned-work views are projection/product surfaces unless a formal decision says otherwise.

They must not create:

- canonical `WorkItem` storage
- role-specific services
- new envelope fields
- mutable record truth competing with events
- broad reporting or aggregation closure

## What Must Never Be Encoded As A New Envelope Type

Do not encode these as envelope `type` values:

- domain facts such as case opened, case resolved, feedback, stock received, referral accepted, inspection completed
- identity or integrity facts such as conflict detected, conflict resolved, subjects merged, subject split
- workflow states such as submitted, pending, approved, returned, resolved, closed, reopened
- product surfaces such as queue item, work item, dashboard item, review item
- role labels such as supervisor action, coordinator action, auditor action, field-worker action
- activity or campaign labels such as campaign capture, routine capture, setup event
- sync/display states such as pending sync, synced, stale, local-only
- escalation levels when the platform processing remains `alert`

Use shapes, payload fields, activity context, pattern definitions, assignments, projections, or product translation instead.

## What Belongs In Schema Or Configuration Instead

| Need | Route |
|---|---|
| Record a domain fact | `type=capture` with domain `shape_ref` |
| Record human judgment on a prior event | `type=review` with review/resolution shape |
| Record system anomaly or notification | `type=alert` with alert/conflict shape |
| Record task creation or completion | `task_created` / `task_completed` plus task/response shapes |
| Distinguish campaign vs. routine work | `activity_ref` plus activity configuration |
| Distinguish intake vs. follow-up vs. resolution | shape roles, concrete shapes, payload fields, and pattern definition |
| Drive review queue/status | Pattern Registry and projection-derived state |
| Bind reviewer/capturer/approver roles | activity configuration, role-action mappings, assignments, scopes |
| Express visibility/authority | assignment-derived access and sync scope, not envelope type |
| Express operational labels | product vocabulary or deployer labels mapped to roles/assignments |

## Baseline And Gap Register Comparison

The accepted baseline already contains the correct architecture line:

- `04-architecture-baseline-v0.md` preserves the six structural event types, `shape_ref`, optional `activity_ref`, bounded configuration, Pattern Registry, and no `status_changed`.
- `05-decision-gap-register.md` correctly keeps Pattern Registry inventory/schema open and now includes operational actor vocabulary routing.
- `07-system-boundary-map.md` routes Event Envelope / Schema separately from Configuration, Assignment / Authority / Sync, and Projection / Workflow State.
- `11`, `12`, and `13` later-source assessments provide a useful triad: type is not domain fact; reference is not referent; mechanism is not instance.
- `14-pattern-inventory-walkthrough-assessment.md` correctly keeps pattern inventory/schema candidate-only.

No ADR wording needs deletion or rewrite. The needed correction is this explicit atomization-facing control surface plus the companion definition file.

## Atomization Acceptance Checks

Before accepting an atom involving events, shapes, patterns, review, assignments, or operational roles, answer:

1. Which axis is being used: `type`, `shape_ref`, reference field, projection, pattern, configuration, or product label?
2. If `type`, what distinct platform processing behavior is required?
3. If `shape_ref`, what payload fact and version does it identify?
4. If pattern, what platform-fixed skeleton is selected, and what remains deployer parameterization?
5. If role label, which assignment, scope, activity, policy value, or pattern capacity binds it?
6. If queue/status/work item, which projection derives it, and why is it not canonical storage?
7. What operation class applies: offline-capable, online/coordination-required, offline-with-constraints, or configuration/control-plane?
8. Which gap is touched but not closed?
9. Would this atom add an envelope field, type value, actor subclass, or service boundary? If yes, change control is required.

## Baseline Impact

This overlay does not change ADR-001 through ADR-005 baseline behavior.

It makes one platform-spec detail gap explicit: atomization needs a concise, citable definition of envelope type, `shape_ref`, references, pattern mechanism, deployer parameterization, and prohibited encodings. That definition is provided in `19-envelope-shape-parametrization-definitions.md`.

## Recommended Next Step

Use this overlay and `19-envelope-shape-parametrization-definitions.md` before drafting event-envelope, review, Pattern Registry, selected-slice, or authorization/sync atoms.

The selected slice may proceed only if the review loop is framed as a narrow use of the existing type/shape/pattern/configuration axes, not as a new core model.
