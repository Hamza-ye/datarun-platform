# Open Decisions And Hold-backs

Status: Draft
Owning boundary: Cross-boundary control register
Primary owner: Architecture Steward

Source basis:

- `../../professional-baseline/04-architecture-baseline-v0.md`
- `../../professional-baseline/05-decision-gap-register.md`
- `../../professional-baseline/07-system-boundary-map.md`
- `../../professional-baseline/09-identity-boundary-control.md`
- `../../professional-baseline/15-conflict-flag-offline-boundary-control.md`
- `../../professional-baseline/16-operational-constraints-boundary-control.md`
- `../../professional-baseline/17-authorization-visibility-boundary-control.md`
- `../../pre-operations/04-accepted-pre-atomization-decisions.md`
- `../../professional-baseline/18-envelope-shape-parametrization-boundary-control.md`
- `../../professional-baseline/19-envelope-shape-parametrization-definitions.md`

Depends on:

- `01-spec-governance.md`

Consumed by:

- all behavior atoms
- implementation planning
- change-control reviews

## Purpose

This register carries unresolved decisions and hold-backs into atomization so future specs do not accidentally close them. It is not a backlog and not an implementation plan.

## Scope

This register owns:

- visibility of open decisions
- explicit hold-backs
- routing for gaps to the correct boundary or decision process
- reopen triggers for deferred items
- atomization-relevant coverage for gaps named by current atoms, planned-consumer review cards, or next recommended work

## Non-Scope

This register does not own:

- closure of any listed gap
- implementation sequencing
- product prioritization
- new architecture decisions
- platform behavior

## Definitions

| Term | Meaning In This Register | Must Not Mean |
|---|---|---|
| Open decision | A question that needs formal architecture, platform-spec, operational policy, or implementation closure before it can be treated as settled | Accepted behavior |
| Hold-back | An unresolved area that atomization must avoid deciding accidentally | Unimportant work |
| Reopen trigger | A concrete condition that requires revisiting a deferred or held-back item | A vague "later" |

## Invariants

- Open decisions remain non-authoritative until closed through the correct route.
- Hold-backs must be cited by affected atoms when they touch the same area.
- A behavior atom may narrow around a hold-back only if it explicitly states what remains undecided.
- No atom may close an item in this register by implication.
- This register is not a full mirror of the professional-baseline gap register. It must carry every gap that current atomization work needs as a citable hold-back or open decision.

## Contracts

### Inputs

- accepted decision gap register
- boundary-control overlays
- accepted pre-atomization decisions
- new gap classifications discovered during atom drafting

### Outputs

- open-decision entries
- hold-back entries
- owner / route mapping
- reopen triggers

### Boundary Crossings

| Crosses To | Through | Notes |
|---|---|---|
| All behavior atoms | cited open-gap tables | Atoms must carry affected hold-backs forward. |
| Change-control log | accepted closure entries | Closure belongs in `92-change-control-log.md`, not silent edits here. |
| Decision Board | decision intake | Board-worthy items require separate decision records. |

## Open Decisions And Hold-backs

| Area | Status | Primary Route | Hold-back / Open Question | Reopen Trigger |
|---|---|---|---|---|
| Cloud multi-tenancy and shared-runtime hosting | Hold-back | Deployment / Tenancy routing surface | Initial atomization assumes one deployment context per runtime/database/configuration namespace. | First target requires multiple independent organizations in one runtime or database. |
| Deployment identity in event envelopes | Hold-back | Event Envelope / Schema plus Deployment / Tenancy | Do not add `tenant_id` or `deployment_id` to the event envelope. | Formal change-control proposal to change event identity or envelope partitioning. |
| External identity-provider authority | Hold-back | Assignment / Authority / Sync | IdP claims may authenticate a principal but must not directly grant event access. | First deployment requires IdP group or claim-driven access without assignment mapping. |
| Group-managed authorization | Hold-back | Assignment / Authority / Sync plus Configuration | Groups, if introduced later, are provisioning helpers or operational teams, not authority sources. | Proposed spec uses group membership as direct authority. |
| Referent registration, attributes, and catalogs | Hold-back / platform-spec design gap | Event Envelope / Schema for reference contracts; Identity / Lineage for subject-continuity lifecycle; Configuration for shape/catalog definitions; Projection / Workflow State and Assignment / Authority / Sync for process, actor, and assignment lifecycles | The ownership split is closed: typed references are envelope contracts, and referent lifecycle ownership follows the boundary that owns the referent's behavior. What remains open is the exact platform-spec treatment of subject registration events, descriptive subject-attribute mutation/projection, deployer-defined catalogs, and any platform-bundled fact shapes needed for registration. Do not require central pre-registration, treat every referenceable entity as subject lineage, encode registration as envelope `type`, route process or assignment lifecycle through Identity / Lineage, or turn deployer-defined attributes/catalogs into a platform-owned domain schema catalog by implication. | Before SPEC-004, SPEC-005, SPEC-006, or the configuration atom defines reference contracts, subject lineage, shape authoring, registration, referent attributes, catalogs, or lifecycle behavior that depends on this placement. |
| Operational actor vocabulary and operation-class routing | Platform-spec detail gap | Cross-boundary definitions plus Assignment / Authority / Sync, Configuration, and Projection / Workflow State | Operational labels such as field worker, supervisor, coordinator, auditor, reviewer, approver, sender, or receiver are scenario, product, deployment, or pattern-capacity vocabulary unless a later formal decision creates platform-owned actor subclasses. Specs must route behavior by operation class, assignments, capacities, scopes, activities, patterns, and projections rather than by persona label. | Before SPEC-002, SPEC-004, SPEC-007, or selected-slice atoms define role labels, actor classes, review/approval/setup language, operation classes, or platform-owned capacities. |
| Shared-device multi-actor sessions | Architecture decision gap | Assignment / Authority / Sync | Actor partitioning, session boundaries, and authorship/accountability are not closed. | Shared devices become required for initial deployment. |
| Auditor access and subject-based scope | Architecture decision gap | Assignment / Authority / Sync | Auditor and subject-based visibility exceptions remain unresolved. | Auditor or subject-based scope is required in first implementation slice. |
| Cross-level distribution visibility | Architecture decision gap / operational policy | Assignment / Authority / Sync | Visibility exceptions beyond existing scope semantics are not closed. | First deployment needs cross-level access beyond current scope mechanisms. |
| Temporary authority, revocation, and offline grace policy | Operational policy gap with architecture trigger | Assignment / Authority / Sync plus Flag / Resolution | Exact temporary grant expiry, handoff windows, stale local authority, and sync-time surfacing are not closed. | First platform spec must describe campaigns, emergency cover, temporary grants, or late revocation behavior. |
| General flag semantics beyond accepted workflow cases | Architecture decision gap | Flag / Resolution | ADR-005 closes workflow-specific flag interactions only. | A behavior atom requires non-workflow flag blocking, creation, lifecycle, or auto-resolution semantics. |
| Alias-cycle read-side behavior and resolution semantics | Architecture decision gap | Identity / Lineage plus Flag / Resolution | Cycle-closing alias handling is not accepted baseline behavior. | Identity atom needs to define cycle-closing event behavior or cyclic graph reads. |
| Domain conflict automation outside workflow | Architecture decision gap | Configuration plus Flag / Resolution | General domain conflict automation remains outside ADR-005 closure. | Platform spec must define non-workflow conflict automation. |
| Exact Pattern Registry inventory | Platform-spec detail gap | Projection / Workflow State | Pattern Registry mechanism is accepted; exact inventory is open. | Workflow atom needs a specific pattern skeleton as normative behavior. |
| Formal Pattern Registry schema | Platform-spec detail gap | Projection / Workflow State plus Configuration | Pattern schema format remains open. | Implementation needs serialized pattern definitions or validation tooling. |
| Source-chain traversal depth limits | Platform-spec detail gap | Flag / Resolution plus Projection / Workflow State | Source-only traversal is accepted for workflow cases; depth limits are open. | Workflow/flag atom needs normative traversal limits. |
| Sync delivery mechanics | Implementation/tooling gap | Assignment / Authority / Sync | Pagination, priority, bandwidth handling, transport details, and operational delivery mechanics are open. | Implementation design starts for sync transport or low-end device operation. |
| Local purge/lifecycle rules for sensitive data | Platform-spec / operational policy gap | Local Data Lifecycle | Retain-and-hide is not sufficient for sensitive deployments; exact purge rules are open. | Sensitive deployment or scope contraction behavior must be implemented. |
| Reporting freshness semantics | Platform-spec detail gap | Reporting / Aggregation | Oversight freshness must be disclosed where it matters; exact model is open. | Reporting atom or first product slice includes oversight metrics. |
| Retention and archival | Platform-spec / operational policy gap | Event Log / Storage plus Local Data Lifecycle | Central retention/archive policy is open. | Compliance, self-host, export, or storage-scale requirement needs retention behavior. |
| Structured import/export contracts | Platform-spec detail gap | Event Envelope / Schema plus Reporting / Aggregation | Structured exchange compatibility is required, but real-time integration and external schemas are not closed. | First deployment requires import/export or audit exchange. |
| Projection optimization and caching | Implementation/tooling gap | Event Log / Storage plus Projection / Workflow State | Projections are derived; optimization strategy is open. | Implementation performance design begins. |
| Event schema/versioning tooling | Implementation/tooling gap | Event Envelope / Schema plus Event Log / Storage and Configuration | Formal tooling, serialization details, migration handling, and validation mechanics are open. | Envelope atom moves from conceptual contract to implementation schema. |
| Projection compatibility across schema versions | Platform-spec / implementation tooling gap | Projection / Workflow State plus Event Envelope / Schema | Mixed shape or envelope schema versions must remain consumable without changing canonical event history. Exact projection merge and compatibility strategy is open. | Schema migration or mixed-version projection behavior must be specified. |
| Configuration authoring and deploy-time validation UX | Implementation/tooling gap | Configuration | Authoring format, packaging UX, and validator UX are open. | Admin/configuration surfaces are selected for implementation. |

## Allowed Extension Points

- New entries may be added when atom drafting exposes unresolved pressure.
- Entries must be added when a current atom, planned-consumer review card, or next recommended work item needs a citable hold-back that is only present in the professional-baseline gap register.
- Existing entries may be split if one hold-back contains separate architecture and implementation concerns.
- Entries may move to `92-change-control-log.md` only when a formal outcome is accepted.

## Forbidden Couplings

- Do not close a hold-back by editing an atom that depends on it.
- Do not remove a hold-back because it is inconvenient for implementation.
- Do not treat "not in first slice" as rejected.
- Do not treat "listed here" as permission to implement.

## Open Gaps

| Gap | Owner / Route | Reopen Trigger |
|---|---|---|
| Priority order for resolving listed gaps | Decision Board / Delivery Lead | Before first implementation planning from accepted atoms. |

## Rejected Paths

- Hiding unresolved decisions in prose.
- Treating product need as automatic architecture closure.
- Treating implementation convenience as a gap closure path.

## Implementation Implications

- Implementation tickets must not depend on an open item as if it were settled.
- If implementation requires one of these entries to be closed, the work should pause for the correct decision path.

## Review Checklist

- [ ] Every entry has a route.
- [ ] Every entry has a concrete reopen trigger.
- [ ] Entries do not introduce new behavior.
- [ ] Affected future atoms can cite the register.
- [ ] Every gap cited by current atom files or planned-consumer review cards is visible here or explicitly routed to a more specific accepted source.
