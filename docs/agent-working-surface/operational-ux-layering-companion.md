# Operational UX Layering Companion

Status: active working-surface companion

Authority: none. This companion does not change the canonical decision ledger, the Baseline Acceptance Register, contracts, runtime behavior, schemas, APIs, screen requirements, or accepted platform capabilities. It is a product and UX vocabulary guardrail for future UI, reporting, workflow, and scenario explorations.

Use this file before product/UI-facing work to translate operational needs into user-facing language without promoting that language into platform architecture.

## 1. Boundary Assessment

Operational UX vocabulary is a real architecture boundary because the product must name work the way people experience it, while the platform must preserve event-led, projection-derived, accept-and-flag architecture.

The scenario and behavioral-pattern evidence shows recurring user needs: structured recording, subject linkage, temporal rhythm, responsibility binding, hierarchical visibility, review and judgment, transfer with acknowledgment, state progression, condition-triggered action, cross-reference, shape evolution, and offline-first work. Those behaviors are real product pressure, but `docs/behavioral_patterns.md` explicitly treats them as behavioral inputs, not architecture constructs.

That distinction matters in UI work. A screen label such as "pending review" or "blocked item" can help a coordinator decide what to inspect next. The same words become unsafe if an agent treats them as a new event type, mutable workflow status, sync scope, resolver rule, trigger engine, report API, or deployer-authored state machine.

Product language is therefore a presentation and reasoning layer. It may describe what a user sees, understands, filters, or acts on. It must not become a new source of truth.

## 2. Layered Model

| Layer | Owns | Examples | Must not own |
|---|---|---|---|
| Core platform architecture | Platform invariants, contracts, mechanisms, closed vocabularies, authority boundaries, and runtime semantics. | Event envelope, event `type`, `shape_ref`, append-only event store, projections, flags, scope containment, platform pattern registry, sync protocol. | Domain labels, screen labels, campaign-specific terms, product copy, or deployer business language. |
| Domain/workflow configuration | Deployer-authored instances using platform mechanisms. | Shapes, activities, role-action bindings, severity overrides within platform rules, assignment instances, pattern bindings, geography trees, subject lists. | New event types, scope logic, pattern mechanisms, expression languages, resolver rules, or custom processing pipelines. |
| Operational UX/product vocabulary | Human-facing concepts that make current platform constructs understandable and actionable. | Work item, activity entry, progress, pending review, attention item, handoff, report view. | Runtime authority, stored truth, contract schemas, sync filters, conflict semantics, assignment command authority, projection correctness. |
| Concrete UI screens and interactions | Specific layouts, labels, controls, workflows, navigation, filters, and affordances for a product surface. | A review queue, supervisor dashboard, distribution handoff screen, report drill-back interaction, freshness indicator. | Platform capability claims, accepted BAR status, API contracts, storage models, or architecture decisions. |

Layer rule: move down the table only with explicit routing. A useful UI label can stay companion-only. A reusable interaction may need a probe. A durable runtime contract, new authority behavior, or process-boundary shape needs a future decision, IDR, CDL row, BAR row, or contract update as routed by the working surface.

## 3. Minimal Operational UX Construct Set

The concepts below are allowed product vocabulary for future design exploration. They are not platform primitives.

| Product-facing concept | What the user should understand | Current core backing constructs | Example domain labels | Forbidden interpretation |
|---|---|---|---|---|
| Work item | Something a person can act on, inspect, continue, or resolve. | Events, subject summaries, assignment-derived access, projections, pattern state, flags. | Monthly report, household visit, village visit, dispatch, receipt, discrepancy. | Not a new event type, assignment type, task table, task engine, or sync scope. |
| Activity entry | A user-authored record of work performed or observed. | Structurally valid event with payload under a `shape_ref`, optional `activity_ref`, `actor_ref`, `subject_ref`, and event time metadata. | Report submission, visit record, stock handoff note, supervisor review. | Not internal envelope vocabulary exposed as product language; not mutable form state after submission. |
| Assignment | User-visible responsibility, route, workload, or queue membership. | Assignment events, Scope Resolver, assignment-derived authorization and sync scope. | Route, village responsibility, district review coverage, warehouse custody responsibility. | Not an activity role-action; not IdP group authority; not UI-only permission; not request-body actor authority. |
| Progress | Derived understanding of how far a subject, location, campaign, transfer, review, or work set has moved. | Rebuildable projections over events, platform pattern definitions, optional human-authored status events where configured. | Month complete, village in progress, transfer received, campaign coverage. | Not canonical mutable workflow status; not a `status_changed` event type; not a reason to reject structurally valid offline work. |
| Pending review | A record, flag, discrepancy, or transition that needs human inspection or resolution. | Server-emitted flags, resolver routing, review events, pattern/review projections. | Supervisor review, discrepancy review, duplicate visit review, stale assignment review. | Does not imply auto-resolution, resolver reassignment, batch bypass, or non-designated resolution authority. |
| Attention item | A flag, warning, stale condition, unresolved discrepancy, or downstream indicator visible to a user. | Canonical server flags, source-only flag cascade indicators, mobile advisory warnings, projections. | Duplicate household, stale role, out-of-order receipt, delayed report, upstream flagged input. | Mobile warnings are not canonical flags; downstream indicators are not propagated root flags. |
| Blocked item | Work that operationally cannot proceed cleanly without human action, missing context, or accepted resolution. | Detect-before-act semantics, unresolved flag exclusion from state/policy participation, assignment/scope/projection context. | Review needed before close, unresolved stock discrepancy, stale authority needs supervisor decision. | Not automatic trigger execution; not automatic rejection; not a durable platform blocked-state column. |
| Change | A user-facing explanation that something was added, corrected, reassigned, superseded, received, resolved, or rejected. | Append-only events, corrections, assignment changes, review/resolution events, projection rebuild. | Reassignment, correction, accepted resolution, returned stock, updated report. | Not mutable audit text; not direct projection patching; not overwriting prior records. |
| Handoff | Continuity of responsibility, custody, or work context across actors, assignments, devices, or locations. | Assignment history, subject history/backfill, transfer-with-acknowledgment pattern, sync within current access scope. | Reassigned village, stock custody transfer, supervisor takeover, next team continuation. | Not a new scope mechanism; not normal sync watermark rewrite; not broad historical pull by default. |
| Report view | A scoped read-side aggregation with visible freshness, uncertainty, and drill-back. | Rebuildable projections, event timestamps, projected latest timestamps, sync watermarks, scoped sync, flag source links, subject timelines. | Monthly reporting dashboard, campaign progress, district oversight, logistics status board. | Not a reporting warehouse, export pipeline, import API, canonical aggregate table, or new process-boundary report contract. |

## 4. Example Translations

Examples are illustrative only. Domain terms must remain domain labels, not platform vocabulary.

| Example pressure | Product wording that is safe | Core translation | Deferred edge to avoid |
|---|---|---|---|
| Simple monthly reporting | "Report view" with submitted, missing, stale, and pending review counts. | Scoped projection over report events, timestamps, assignment scope, and flags. | Reporting warehouse, export/import contract, canonical aggregate table. |
| S22 coordinated grouped-location work | "Campaign progress", "work item", "handoff", and "attention item". | Configured activities, assignments, subject-linked captures, transfer pattern events, subject history, source-only flags, read-side aggregation. | Full discovered-unit lifecycle, automatic follow-up triggers, custom campaign scope. |
| S26 operational reporting | "Report view" with freshness, unresolved issue treatment, and drill-back. | Projection metadata, sync watermarks, unresolved flag counts, event timelines, scoped access. | General reporting API, interop/export system, mutable aggregate truth. |
| S27 logistics distribution | "Handoff", "progress", "pending review", and "attention item". | `transfer_with_acknowledgment/v1` pattern, dispatch/receipt/discrepancy events, transition flags, exact resolver handling. | Custom custody scope, auto-resolution, resolver reassignment, hard-coded logistics platform semantics. |

## 5. Anti-Patterns

| Anti-pattern | Smell | Required response |
|---|---|---|
| Product term becomes structural vocabulary | A UI label is proposed as an envelope `type`, event field, scope type, flag category, pattern mechanism, or activity action. | Stop and route as platform evolution or reject. |
| Screen state becomes truth | A dashboard status, queue state, or progress badge is stored as canonical current state. | Reframe as event append plus rebuildable projection, or route a future decision. |
| Operational queue bypasses resolver authority | Batch review, pending match, or pending review UI resolves without exact designated resolver semantics. | Route to conflict/admin UX exploration or successor decision. |
| Domain label becomes platform concept | Campaign, village, household, stock, route, or logistics term appears in core contracts or generic platform APIs. | Keep it as deployer/domain configuration or screen copy. |
| UI hides uncertainty | Reports show one count without freshness, unresolved flag treatment, or drill-back. | Make freshness, exclusion, and traceability visible in product design. |
| Device warning becomes rejection | Mobile command validation blocks structurally valid state/policy anomalies. | Keep the warning advisory and rely on server accept-and-flag. |
| Configuration becomes product programming | A product request adds loops, scripts, custom traversal, custom scope logic, or deployer state machines. | Route to platform mechanism decision or reject. |
| Companion used as authority | An agent cites this file to accept a BAR row, change a contract, or implement a deferred capability. | Stop. Use CDL, BAR, contracts, IDRs, and routed successor work. |

## 6. Future Exploration Checklist

Before routing product/UI-facing work, answer these questions:

| Check | Required answer |
|---|---|
| What user problem is being named? | State the operational decision or action the user needs, without naming new platform primitives. |
| Which layer owns it? | Classify core architecture, domain/workflow configuration, operational UX vocabulary, or concrete UI. |
| What current constructs back it? | Name events, projections, assignments, scope, flags, pattern state, config package content, or subject history as applicable. |
| Is a product term trying to become authority? | If yes, stop and route to a decision path. |
| Does it cross a process-boundary contract? | If yes, route contract/schema/protocol work explicitly. |
| Does it change sync or access? | If yes, treat it as security-sensitive platform work, not UI wording. |
| Does it change state participation or flag handling? | If yes, preserve detect-before-act or route a successor decision. |
| Does it need a stable server/mobile view-model or API before screens can be designed responsibly? | If yes, route that contract/probe first instead of designing screens against guesses. |
| Does it promote a deferred surface? | Entity lifecycle, triggers, auto-resolution, resolver reassignment, new scope, reporting warehouse/API, import/export, IdP claim authority, and mobile authoritative rejection require successor routing. |
| What evidence level is being claimed? | Keep scenario pressure, architecture support, contract definition, code evidence, targeted test evidence, runtime scenario evidence, and accepted baseline status separate. |

## 7. Routing Rule

| Route | Use when | Output |
|---|---|---|
| Companion-only guidance | The work only needs vocabulary, screen-copy guardrails, example translation, or product-facing terminology over existing constructs. | Update this companion or cite it in a bounded prompt. |
| Probe | The work can be exercised with current constructs but needs scenario/runtime evidence, UX evidence, or design confidence before implementation. | Scenario review, runtime probe, UX/product exploration, or narrow test plan. |
| Future decision | The product need is real but may require new platform behavior, new command authority, new reporting/import/export boundary, entity lifecycle, triggers, auto-resolution, resolver reassignment, or new scope semantics. | Bounded path comparison and successor recommendation. |
| IDR | The implementation path needs a concrete decision within an already authorized architecture boundary. | IDR with scope, alternatives, selected path, guardrails, tests, and deferrals. |
| CDL/BAR-level change | The work changes canonical contracts, invariants, process-boundary schemas/protocols, accepted baseline status, or platform authority. | CDL successor decision, contract updates, BAR acceptance evidence, and targeted tests as routed. |

Default escalation rule: if a product/UI concern would make multiple independently shipped components agree on a new shape or meaning, treat it as contract or architecture work before screen design.

## 8. Non-Authority Statement

This companion cannot promote deferred capabilities. It does not authorize entity lifecycle, trigger execution, auto-resolution, resolver reassignment, new scope mechanisms, reporting warehouse/API, structured import/export, IdP groups/claims as authority, mobile authoritative rejection, new envelope fields, new event types, or new platform pattern mechanisms.

Future agents may use this file to choose safer language and routing. They must use the CDL for architecture authority, the BAR for accepted baseline status, contracts for process-boundary shapes, and routed IDRs or successor decisions for implementation authority.
