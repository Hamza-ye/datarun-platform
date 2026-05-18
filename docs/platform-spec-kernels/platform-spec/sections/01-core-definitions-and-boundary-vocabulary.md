# 01 Core Definitions And Boundary Vocabulary

Status: Draft
Owning boundary: professional-baseline source authority
Primary owner: Architecture Steward

Source basis:

- `../../professional-baseline/04-architecture-baseline-v0.md`
- `../../professional-baseline/05-decision-gap-register.md`
- `../../professional-baseline/07-system-boundary-map.md`
- `../../professional-baseline/19-envelope-shape-parametrization-definitions.md`
- `../../professional-baseline/20-platform-spec-outline.md`

Depends on:

- `00-specification-source-authority.md`
- `90-open-decisions-and-gap-register-citations.md`
- `91-rejected-alternatives.md`

Consumed by:

- all behavior sections
- engineering design vocabulary after this section is accepted

## Purpose

This section defines cross-boundary vocabulary so later platform-spec sections can use stable terms without re-deciding ownership, envelope shape, authority, projection, workflow, or configuration semantics.

## Scope

This section owns:

- citable definitions for first platform-spec sections
- separation between event truth, envelope contracts, references, configuration, projections, assignments, product labels, and operation classes
- forbidden meanings for drift-prone terms
- explicit citations to `05` where a term names an open surface

## Non-Scope

This section does not own:

- final event-envelope serialization
- final bundled shape inventory
- Pattern Registry inventory or schema
- identity merge/split behavior
- assignment, authorization, sync, or local lifecycle behavior
- process lifecycle or active process-reference emission
- flag lifecycle, conflict resolution, or reporting freshness behavior
- product personas or UI wording
- closure of any `05` gap

## Definitions

| Term | Meaning In This Section | Must Not Mean |
|---|---|---|
| Event | An immutable operational fact accepted through the event-store path | Mutable record, projection row, product task card, or workflow state |
| Event log | Append-only canonical store of accepted events | Audit sidecar for mutable records, cache, queue, or dashboard store |
| Operational fact | A valid accepted event preserved as history | Proof that downstream interpretation is clean, authorized, or conflict-free |
| Event envelope | Platform-owned contract carried by each event for storage, sync, routing, attribution, and interpretation | Deployer-authored record wrapper |
| Envelope `type` | Platform-owned processing-pipeline discriminator | Domain fact, lifecycle state, role, product surface, authority, tenant, or deployment marker |
| Payload | Shape-conforming fact body carried by an event | Envelope contract, authority snapshot, or canonical workflow state |
| `shape_ref` | Payload schema and version reference | Workflow state, authority marker, role label, product surface, tenant identity, or deployment identity |
| Shape | Typed payload schema identified by `shape_ref` | Envelope `type`, workflow pattern, actor class, access rule, or final bundled-shape inventory |
| Platform-bundled shape | Narrow platform-owned fact shape accepted by a later owning section | General platform-owned business schema catalog |
| `actor_ref` | Authorship reference for the human or system actor that authored the event | Permission grant, role class, account identity, or device identity |
| `subject_ref` | Reference to what an event is about where a subject/reference contract requires it | Ownership of all lifecycle, workflow, authority, or reporting behavior for that referent |
| `activity_ref` | Optional configured activity-instance context | Pattern reference, authority snapshot, tenant/deployment identity, or work-item identity |
| Reference | Contract that lets one boundary point to a referent owned elsewhere | Lifecycle ownership of the referent |
| Device identity | Device or app-installation namespace for device metadata and sync/order signals | Actor identity or authority source |
| Projection | Derived state or read model computed from events and relevant configuration/patterns | Canonical truth or directly patchable state |
| Pattern | Platform-fixed workflow skeleton selected and parameterized by deployers | Deployer-authored state-machine code or product queue |
| Activity | Deployer-configured operational instance binding shapes, patterns, roles, scopes, schedules, thresholds, or policy values | Platform actor class or envelope type |
| Deployer parameterization | Selecting values inside platform-owned mechanisms | Deployer-authored platform code, envelope fields, event types, scope logic, or access-control programs |
| Assignment | Platform-recognized relation used to derive authority and sync scope | External IdP claim, product role label, or immutable authority snapshot |
| Authority projection | Reconstructed authority from actor, references, assignment timeline, event context, and sync knowledge | Stored immutable `authority_context` |
| Sync scope | Assignment-derived access scope used to filter event delivery | Separate sync-only entitlement model |
| Accept-and-flag | Validly structured work is accepted while state/authority/workflow/identity/configured-domain anomalies are surfaced by owning boundaries | Accepting malformed envelopes or bypassing structural validation |
| Operation class | Architecture lens for offline-capable, online/coordination-required, offline-with-constraints, or configuration/control-plane work | Product persona or hierarchy label |
| Product label | UI, scenario, deployment, or role wording mapped to platform mechanisms | Platform actor subclass or architecture boundary |
| Hold-back | Known unresolved area a section must not decide accidentally | Permission to implement or ignore the issue |

## Invariants

- Definitions clarify accepted baseline vocabulary; they do not add behavior.
- Event log truth, envelope processing, payload shape, references, projection state, assignment-derived authority, and deployer configuration remain separate axes.
- Product labels, role labels, queues, dashboards, work items, and UI statuses do not become platform classes or canonical storage primitives by being named here.
- Terms that touch open gaps must cite `05` in affected behavior sections.
- This section may define `pattern` as a term, but it must not define Pattern Registry inventory or schema.
- This section may define reference vocabulary, but it must not close final reference serialization, active emission sites, or process lifecycle behavior.

## Contracts

### Inputs

- accepted baseline vocabulary from `04`
- gap ownership and hold-backs from `05`
- responsibility routing from `07`
- candidate definitions from `19`
- first-section blockers from `20`

### Outputs

- cross-boundary term meanings
- forbidden meanings for drift-prone terms
- vocabulary dependency surface for later sections

### Boundary Crossings

| Crosses To | Through | Notes |
|---|---|---|
| Event Log / Storage | event, event log, operational fact, projection | Storage owns behavior; this section owns shared words. |
| Event Envelope / Schema | envelope, type, payload, references, device metadata | Envelope section owns field contract and validation behavior. |
| Assignment / Authority / Sync | assignment, authority projection, sync scope, operation class | Authority/sync behavior remains later section work. |
| Configuration | activity, shape, deployer parameterization | Configuration owns bounded deployer variation. |
| Projection / Workflow State | projection, pattern | Pattern inventory/schema remain open in `05`. |

## Allowed Extension Points

- Later sections may add boundary-specific terms if they preserve these forbidden meanings.
- Product and deployer wording may vary by deployment if it maps back to platform-owned mechanisms.
- Platform-bundled shapes may be defined later only by owning behavior sections.

## Forbidden Couplings

- Do not use `type` as a domain taxonomy.
- Do not use `shape_ref` as authority, workflow state, product surface, role, tenant, or deployment identity.
- Do not infer authority from `actor_ref`, account, group, identity-provider claim, tenant, deployment, or product role label.
- Do not treat projections, queues, reports, dashboards, work items, or statuses as canonical truth.
- Do not treat deployer parameterization as deployer-authored platform logic.
- Do not use glossary wording to close a `05` gap.

## Open Gaps

| Gap | Owner / Route | Reopen Trigger |
|---|---|---|
| Envelope type, shape ref, references, and parametrization boundary | `05`; Event Envelope / Schema | A definition is used to add fields, type values, actor subclasses, or product classes. |
| Operational actor vocabulary and operation-class routing | `05`; Assignment / Authority / Sync plus Configuration and Projection / Workflow State | A section defines role labels, actor classes, review/approval/setup language, or operation classes. |
| Exact Pattern Registry inventory and formal schema | `05`; Projection / Workflow State | A section needs normative pattern skeletons or serialized schema. |
| Final reference serialization and active emission sites | `05`; Event Envelope / Schema plus owning behavior sections | A section needs canonical field names, placement, cardinality, or required emission sites. |
| Process reference and process lifecycle semantics | `05`; Projection / Workflow State plus Event Envelope / Schema | A section defines process lifecycle or active process-reference emission. |

## Rejected Paths

- Product personas as platform actor subclasses.
- Queues, dashboards, work items, or review lists as canonical storage.
- `type`, `shape_ref`, references, activity, pattern, projection, assignment, configuration, and product vocabulary as interchangeable axes.
- Definitions that close open decisions without change control.

## Implementation Implications

- Implementation names may be more specific, but they must not invert these ownership boundaries.
- Engineering designs should cite accepted terms only after this section is accepted.
- If a design needs a term to carry behavior not defined here, route it to the owning section or `05`.

## Review Checklist

- [ ] Source basis is accepted and cited.
- [ ] Definitions do not add behavior.
- [ ] Open gaps are cited rather than closed.
- [ ] No envelope field, type value, actor subclass, authority shortcut, or canonical projection state is introduced.
- [ ] Product labels and UI surfaces remain outside platform-core semantics.
