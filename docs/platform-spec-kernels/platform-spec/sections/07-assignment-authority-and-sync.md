# Assignment, Authority, And Sync

Status: Draft
Owning boundary: Assignment / Authority / Sync
Primary owner: Architecture Steward

Source basis:

- `../../professional-baseline/04-architecture-baseline-v0.md`
- `../../professional-baseline/05-decision-gap-register.md`
- `../../professional-baseline/07-system-boundary-map.md`
- `../../professional-baseline/09-identity-boundary-control.md`
- `../../professional-baseline/15-conflict-flag-offline-boundary-control.md`
- `../../professional-baseline/16-operational-constraints-boundary-control.md`
- `../../professional-baseline/17-authorization-visibility-boundary-control.md`
- `../../professional-baseline/18-envelope-shape-parametrization-boundary-control.md`
- `../../professional-baseline/19-envelope-shape-parametrization-definitions.md`
- `../../pre-operations/04-accepted-pre-specification-decisions.md`

Depends on:

- `00-specification-source-authority.md`
- `01-core-definitions-and-boundary-vocabulary.md`
- `02-event-log-and-storage-model.md`
- `03-event-envelope-schema-and-references.md`
- `05-references-and-identity-lineage.md`
- `06-configuration-and-parametrization.md`
- `90-open-decisions-and-gap-register-citations.md`
- `91-rejected-alternatives.md`

Consumed by:

- `08-local-data-lifecycle.md`
- `09-projections-workflow-and-patterns.md`
- `10-conflict-flag-and-resolution.md`
- `11-trigger-reactivity.md`
- `12-reporting-aggregation-and-freshness.md`
- implementation designs for assignment projection, access filtering, authorization checks, sync filtering, stale-authority surfacing, and scope-change handoff

## Purpose

This section defines the narrow Assignment / Authority / Sync contract for assignment-derived access, access-scoped event sync, authority reconstruction, original-subject authorization, and scope-change handoff. It preserves immutable event history while preventing accounts, groups, external claims, role labels, activity context, tenant context, deployment context, or stored snapshots from becoming platform authority.

## Scope

This section owns:

- assignment-derived access as the source of effective event visibility and action authority
- sync scope as the same scope used for access filtering
- immutable event sync as idempotent, append-only, order-independent, and access-filtered delivery
- authority reconstruction as a projection over durable source facts and available sync knowledge
- authorization checks against the original subject reference written into the event
- additive scope expansion for newly visible event subsets
- baseline scope contraction as selective retain and local lifecycle handoff
- surfacing of stale, invalid, or insufficient authority for downstream review without mutating historical events
- Assignment / Authority / Sync-side constraints on authentication, device identity, role labels, activity context, and configured policy inputs

## Non-Scope

This section does not own:

- event-envelope fields, structural reference categories, structural `type` values, or envelope serialization
- immutable authority snapshots stored on events
- actor provisioning, authentication protocols, account schema, credential lifecycle, or identity-provider integration
- subject lineage, raw-reference preservation, alias projection, or referent lifecycle
- deployer-authored access-control programs, final permission tables, or arbitrary scope-containment logic
- workflow state, Pattern Registry inventory, product queues, task lifecycle, review lifecycle, or reporting surfaces
- flag lifecycle, resolution-event mapping, general authorization-flag semantics, or workflow-specific flag behavior
- sync transport, pagination, priority, bandwidth, retry, or operational delivery mechanics
- configuration authoring format, deploy-time validation UX, configuration package versioning, or stale-configuration reconciliation details
- local purge, secure deletion, retention, archival, or sensitive-data lifecycle rules
- final reference serialization, required reference emission sites, referent registration, referent attributes, or catalogs

## Definitions

| Term | Meaning In This Section | Must Not Mean |
|---|---|---|
| Assignment | Platform-recognized relation that binds an actor to scope, role/capacity, activity/context, time, or responsibility inputs used to derive access | External identity-provider claim, account group, role label, product persona, or stored event authority snapshot |
| Assignment timeline | Projection of assignment changes over time as available to the device or server doing authority reconstruction | Complete global knowledge on every device or mutable override of historical event facts |
| Access scope | Assignment-derived visibility or authority scope for events and derived effects | Independent entitlement model detached from assignment facts |
| Sync scope | The access scope used to filter immutable event delivery | Separate sync-only permission model |
| Authority projection | Derived authority reconstructed from actor reference, original subject or process references, assignment timeline, event creation context, relevant configuration inputs, and sync knowledge state | Stored immutable authority snapshot or event-envelope field |
| Original subject authorization | Authorization check against the original subject reference written into the accepted event | Authorization through a post-merge resolved subject reference |
| Event creation context | Durable event facts and bounded contextual inputs available when deciding whether an actor could create or act on an event | Complete permission proof stored in the event |
| Sync knowledge state | Events, assignments, configuration, lineage facts, and projections available to the sync or review point doing reconstruction | Guarantee that a disconnected device had complete current authority knowledge |
| Scope expansion | Later assignment-derived access to more events, subjects, processes, activities, or scopes | Rewrite of prior authorization results or bulk reclassification of old events |
| Scope contraction | Later assignment-derived loss of access to events, subjects, processes, activities, or scopes | Deletion or mutation of central canonical event history |
| Selective retain | Baseline scope-contraction strategy where an actor's own events can remain locally while other actors' events about out-of-scope subjects become local lifecycle candidates | Complete sensitive-data purge policy or proof that hide-only behavior is sufficient |
| Local lifecycle handoff | Transfer from Assignment / Authority / Sync to Local Data Lifecycle for device-side retain, remove, purge, archive, or summarize decisions | Authority section ownership of sensitive-data deletion or retention policy |
| Stale local authority | Local action taken under last-known assignments, configuration, or projections that later central knowledge may constrain or surface | Stored authorization guarantee or automatic rejection of the historical event |

## Invariants

- Access is assignment-derived.
- Sync scope is access scope.
- Sync distributes immutable events, not mutable records, projections, queues, or work items.
- Event sync is idempotent, append-only, order-independent, and access-filtered.
- Authority is reconstructed as projection-derived state from durable facts and available sync knowledge; it is not stored as immutable event authority.
- Authorization for historical events is checked against the original subject reference written into the event.
- Read-side subject alias projection may inform display and later interpretation, but it must not replace the historical authorization target.
- `actor_ref` records authorship and is an input to authority reconstruction; it is not a permission grant by itself.
- `activity_ref` is configured activity context and may be an input to authority reconstruction through assignment, role/action mapping, scope, and configuration mechanisms; it is not an authority grant by itself.
- Device identity and app-installation identity are sync and ordering inputs, not actor identity or authority.
- Authentication proves that a principal may act as an actor; assignment-derived authority decides what that actor may see or do.
- Account records, groups, external identity-provider claims, tenant context, deployment context, role labels, and configured labels are not direct event authority.
- Scope expansion is additive and may make more events available to the actor or device under the same immutable event-history rules.
- Scope contraction must preserve central canonical events and route local retain/remove/purge decisions to Local Data Lifecycle.
- Structurally valid events created under stale local authority remain immutable operational facts; late authority problems are surfaced for the owning review or flag path unless a later accepted operation class requires online-only rejection.
- This section consumes `SPEC-006` configuration inputs only as draft/open-gap context until Configuration is accepted; it does not close configuration versioning or stale-configuration reconciliation.

## Contracts

### Inputs

- accepted immutable events and event subsets from Event Log / Storage
- accepted envelope values from Event Envelope / Schema, including `actor_ref`, optional `activity_ref`, subject or typed references where required, device metadata, and sync concurrency metadata
- raw subject references and lineage projection facts from Identity / Lineage, with original subject references preserved for authorization checks
- assignment-change facts and assignment timeline inputs where accepted shapes or later platform-bundled facts define them
- bounded configuration inputs from Configuration, including role labels, role/action mappings, scope parameters, activity context, schedules, sensitivity classifications, and policy values
- local last-known assignments, configuration, scoped projections, and local session state for offline-with-constraints operation
- sync knowledge state available at the device, server, or review point performing reconstruction

### Outputs

- access-scoped event delivery decisions
- authority projection for available event subsets and assignment facts
- authorization check results for event creation, sync-time review, and downstream effect gating
- stale or invalid authority surfaced to Flag / Resolution or the owning review path where later accepted behavior defines that path
- additive event-subset availability after scope expansion
- selective-retain candidates and local lifecycle handoff records after scope contraction
- implementation-facing constraints that keep authority, sync, identity, configuration, workflow, reporting, and local lifecycle separate

### Boundary Crossings

| Crosses To | Through | Notes |
|---|---|---|
| Event Log / Storage | immutable event subset and append-only sync contract | Storage owns canonical event truth; this section filters delivery by access scope. |
| Event Envelope / Schema | `actor_ref`, optional `activity_ref`, original subject or typed references, device metadata, and sync concurrency metadata | Envelope owns field meanings. Assignment / Authority / Sync consumes them without adding fields or authority snapshots. |
| Identity / Lineage | original subject reference plus read-side lineage facts | Identity owns raw/resolved reference behavior. This section uses the original subject reference for historical authorization. |
| Configuration | role/action mappings, scope parameters, activity context, schedules, sensitivity classifications, and bounded policy values | Configuration supplies bounded inputs. This section owns authority reconstruction and does not accept arbitrary access-control logic. |
| Projection / Workflow State | detect-before-act gate and authority projection inputs for workflow effects | Workflow state remains projection-derived and must not run irreversible effects before relevant authorization checks. |
| Flag / Resolution | surfaced stale or invalid authority facts | Flag / Resolution owns lifecycle and resolution semantics; this section only supplies authorization anomaly source facts. |
| Trigger / Reactivity | detect-before-act gate for downstream triggers | Triggers must not fire irreversible effects before relevant access and authority checks run. |
| Reporting / Aggregation | access-scoped projection and report visibility constraints | Reports and aggregates consume access constraints; they do not become authority sources. |
| Local Data Lifecycle | scope-contraction retain/remove/purge handoff | Local lifecycle owns concrete device-side handling and sensitive-data rules. |

## Allowed Extension Points

- Implementations may choose projection storage, indexing, and recomputation strategies for authority if assignment-derived access, original-subject authorization, and immutable event history are preserved.
- Sync implementations may choose transport, pagination, batching, retry, priority, compression, and bandwidth strategies later under the sync delivery mechanics gap.
- Later accepted configuration work may define bounded role/action mappings, scope parameters, policy values, and activity/context authority details without becoming arbitrary access-control code.
- Later accepted sections may define platform-bundled assignment or authorization-review shapes if routed through the platform-bundled shape inventory and reference-serialization gaps.
- Product surfaces may label roles, review queues, assignments, and oversight views if those labels map back to assignments, scopes, capacities, configuration, or projections and do not become platform actor subclasses.
- Local lifecycle sections may define retain, remove, purge, archive, or summarize behavior after scope contraction without mutating central canonical events.

## Forbidden Couplings

- Do not store immutable authority snapshots on events.
- Do not store immutable `authority_context`.
- Do not add envelope fields or change envelope field meanings to carry authority.
- Do not add `tenant_id`, `deployment_id`, `user_id`, or `group_id` as event authority.
- Do not make account identity, group membership, external identity-provider claims, tenant context, deployment context, role labels, or configured labels direct authority sources.
- Do not make `actor_ref` alone sufficient for permission.
- Do not make `activity_ref` alone sufficient for permission.
- Do not use post-merge resolved subject references as the authorization target for historical events.
- Do not make sync scope a permission model separate from assignment-derived access scope.
- Do not make sync deliver mutable records, projection state, queue state, work items, or reporting rows as canonical truth.
- Do not define sync transport, pagination, priority, bandwidth, retry, or operational delivery mechanics in this section.
- Do not define final permission tables, fixed role taxonomies, fixed product personas, or auditor visibility exceptions in this section.
- Do not let deployers author access-control programs, scope-containment logic, platform code, envelope fields, structural type values, or field-level sensitivity mechanisms.
- Do not make configuration package changes retroactively invalidate structurally valid offline work by implication.
- Do not make local retain-and-hide sufficient for sensitive scope contraction.
- Do not make authority reconstruction own workflow state, flag lifecycle, reporting freshness, referent registration, referent attributes, catalogs, or local purge policy.

## Open Gaps

| Gap | Owner / Route | Reopen Trigger |
|---|---|---|
| Shared-device multi-actor sessions | Assignment / Authority / Sync plus Identity / Lineage and implementation design | Shared devices become required for the first deployment or local sessions must support multiple actors. |
| Auditor access and subject-based scope | Assignment / Authority / Sync with formal architecture decision if new scope semantics are required | Auditor or subject-based visibility is required by a deployment or product slice. |
| Assessment visibility | Assignment / Authority / Sync plus Reporting / Aggregation and operational policy | Assessment views require visibility beyond current assignment/scope mechanisms or become part of a product slice. |
| Cross-level distribution visibility | Assignment / Authority / Sync plus operational policy | A deployment needs cross-level access beyond existing scope mechanisms. |
| Permission table and activity/context authority details | Assignment / Authority / Sync plus Configuration | Concrete permission matrices, role/action bindings, or normative activity/context authority semantics must be implemented. |
| Temporary authority, revocation, and offline grace policy | Assignment / Authority / Sync plus Flag / Resolution and operational policy | Temporary grants, emergency cover, role handoff windows, late revocation, or grace periods are required. |
| Onboarding and role-transition details | Assignment / Authority / Sync plus Configuration, Local Data Lifecycle, and operational policy | Onboarding, responsibility transfer, actor setup, or role transition behavior must become normative. |
| Sync delivery mechanics | Assignment / Authority / Sync plus implementation tooling | Sync transport, pagination, priority, bandwidth handling, retries, or low-end-device delivery must be specified. |
| Local purge/lifecycle rules for sensitive data | Local Data Lifecycle plus operational policy | Sensitive deployment or scope contraction behavior must be implemented. |
| Configuration versioning and stale-configuration reconciliation | Configuration plus Event Envelope / Schema and Assignment / Authority / Sync | Stale local authority or configuration affects sync-time review, migration, or offline work created under older configuration. |
| Final reference serialization and active emission sites | Event Envelope / Schema plus Identity / Lineage, Assignment / Authority / Sync, Configuration, Projection / Workflow State, and owning behavior sections | Canonical field names, placement, cardinality, or required emission sites are needed for accepted references. |
| Referent registration, attributes, and catalogs where authority depends on referent interpretation | Event Envelope / Schema, Identity / Lineage, Configuration, Projection / Workflow State, and Assignment / Authority / Sync | Authority depends on subject registration events, descriptive attributes, deployer-defined catalogs, or lifecycle ownership for non-subject referents. |

## Rejected Paths

- Stored immutable authority snapshots on events.
- Stored immutable `authority_context`.
- `tenant_id`, `deployment_id`, `user_id`, or `group_id` as event authority.
- Account, group, external identity-provider claim, tenant, deployment, role label, or configured label as a direct authority source.
- Direct group or external-claim authority without assignment-derived mapping.
- Authorization through post-merge alias shortcuts.
- Treating `activity_ref` as immutable authority context, pattern identity, tenant/deployment reference, assignment authority, or work-item identity.
- Treating device identity as actor identity.
- Deployer-authored arbitrary access-control logic.
- Field-level sensitivity as a platform mechanism.
- Sync as a separate entitlement model from assignment-derived access.
- Syncing mutable record state, projections, queues, reports, or work items as canonical truth.
- Requiring complete global knowledge for ordinary offline capture.
- Rejecting structurally valid offline work solely because later central authority or configuration changed.
- Treating retain-and-hide as sufficient sensitive-data handling after scope contraction.

## Implementation Implications

- Authorization code should reconstruct authority from assignment, references, event context, configuration, and available sync knowledge instead of reading authority from the event envelope.
- Sync filtering should operate over immutable events and event subsets, with idempotent re-delivery tolerance and order-independent application.
- Projection and trigger code should call authority checks before irreversible downstream effects and should treat late authority failures as surfaced anomalies where later accepted behavior defines that path.
- Offline clients should enforce last-known authority from scoped local facts, while preserving enough event and reference context for later sync-time review.
- Scope expansion should add event availability without rewriting prior events or prior projection facts.
- Scope contraction should generate local lifecycle candidates and hand them to Local Data Lifecycle; it should not mutate central event history.
- Tests should prove that `actor_ref`, `activity_ref`, role labels, account data, group data, external claims, tenant context, deployment context, and resolved subject aliases do not independently authorize historical event access.

## Review Checklist

- [ ] Source basis is accepted and cited.
- [ ] Owner and boundary are singular.
- [ ] Scope and non-scope are explicit.
- [ ] Contracts identify inputs, outputs, and boundary crossings.
- [ ] Open gaps are not closed accidentally.
- [ ] Forbidden couplings include the likely drift risks.
- [ ] No envelope field, type value, authority shortcut, or canonical projection state was added without change control.
- [ ] Product labels, role labels, and UI surfaces remain outside platform-core semantics.

Drafting Agent note, 2026-05-12:

- This draft intentionally carries forward shared-device sessions, auditor and subject-based scope, assessment visibility, cross-level visibility, permission/activity authority details, temporary authority and offline grace policy, onboarding and role-transition details, sync delivery mechanics, sensitive local lifecycle, stale-configuration reconciliation, final reference serialization, and referent registration/catalog gaps.
- `SPEC-006` remains Draft. This section consumes `SPEC-006` only as draft/open-gap context and does not treat Configuration as accepted implementation authority.

Challenge Review reconciliation, 2026-05-12:

- Verdict before reconciliation: Needs Rework.
- Reconciled missing open-gap carry-forward by adding `Assessment visibility` and `Onboarding and role-transition details` to this section and `90-open-decisions-and-gap-register-citations.md`.
- No rejected path was reintroduced and no change-control trigger was found.

Integration Review, 2026-05-12:

- Verdict: Carry Explicit Gap.
- Planned downstream sections may consume SPEC-007 outputs as gates, source facts, and handoffs only.
- `SPEC-008` may consume scope-contraction handoff without inheriting purge, retention, archive, or sensitive lifecycle rules.
- `SPEC-009` may consume authority checks and detect-before-act gates without inheriting workflow state, Pattern Registry, queue, lifecycle, or final permission-table behavior.
- `SPEC-010` may consume stale or invalid authority source facts without inheriting flag lifecycle, resolution mapping, or general authorization-flag semantics.
- `SPEC-011` may consume authority gating without inheriting trigger timing, scheduling, or side-effect policy.
- `SPEC-012` may consume access-scoped visibility constraints without closing reporting freshness, auditor access, assessment visibility, or cross-level exceptions.

Architecture Steward recommendation, 2026-05-12:

- Recommendation: proceed as Draft with explicit carried gaps preserved.
- This recommendation does not promote SPEC-007 out of Draft.
- Future promotion still requires the formal acceptance workflow and an explicit status update in this section and `section-registry.yml`.
