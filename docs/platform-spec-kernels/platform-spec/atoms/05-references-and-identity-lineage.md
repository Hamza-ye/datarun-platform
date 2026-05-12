# References And Identity Lineage

Status: Draft
Owning boundary: Identity / Lineage
Primary owner: Architecture Steward

Source basis:

- `../../professional-baseline/04-architecture-baseline-v0.md`
- `../../professional-baseline/05-decision-gap-register.md`
- `../../professional-baseline/07-system-boundary-map.md`
- `../../professional-baseline/08-baseline-acceptance-check.md`
- `../../professional-baseline/09-identity-boundary-control.md`
- `../../professional-baseline/15-conflict-flag-offline-boundary-control.md`
- `../../professional-baseline/16-operational-constraints-boundary-control.md`
- `../../professional-baseline/17-authorization-visibility-boundary-control.md`
- `../../professional-baseline/18-envelope-shape-parametrization-boundary-control.md`
- `../../professional-baseline/19-envelope-shape-parametrization-definitions.md`

Depends on:

- `01-spec-governance.md`
- `02-glossary-and-core-definitions.md`
- `03-event-log-storage.md`
- `04-event-envelope-schema.md`
- `90-open-decisions.md`
- `91-rejected-paths.md`

Consumed by:

- `07-assignment-authority-and-sync.md`
- `09-projections-workflow-and-patterns.md`
- `10-conflict-flag-and-resolution.md`
- `12-reporting-aggregation-and-freshness.md`
- implementation designs for subject-lineage storage, lineage projections, conflict inputs, and read-side identity resolution

## Purpose

This atom defines the narrow Identity / Lineage contract for subject continuity, raw subject-reference preservation, alias projection, and corrective split behavior. It keeps reference contracts separate from referent lifecycle ownership so identity lineage does not absorb actor authority, assignment scope, process workflows, catalogs, descriptive attributes, reporting, or deployer configuration.

## Scope

This atom owns:

- subject identity continuity for referents that have subject-lineage semantics
- raw subject-reference preservation as accepted immutable event history
- read-side resolved subject references derived from lineage projection
- subject alias lineage for merge behavior
- corrective split behavior that preserves historical references
- lineage acyclicity for accepted subject-lineage operations
- subject-lineage facts needed by conflict, authorization, audit, and projection consumers
- Identity / Lineage-side constraints on merge/split and reference-resolution behavior

## Non-Scope

This atom does not own:

- final reference serialization, field placement, cardinality, or required emission sites
- new event-envelope fields, structural reference categories, or structural `type` values
- referent registration, referent attributes, deployer-defined catalogs, or descriptive subject profiles
- platform-bundled registration or identity shape inventory
- every referenceable entity or every typed reference lifecycle
- actor provisioning, authentication, account identity, group membership, or identity-provider claims
- assignment validity, access scope, sync scope, authority reconstruction, or authorization policy
- process lifecycle for shipments, campaigns, cases, reviews, transfer chains, or pending-match workflows
- workflow state progression, Pattern Registry inventory, product queues, or reporting surfaces
- conflict or flag lifecycle, resolution-event mapping, or general flag semantics
- deployer-authored duplicate-matching algorithms, catalog schemas, or platform logic
- local data lifecycle, retention, archival, import/export, or reporting freshness behavior

## Definitions

| Term | Meaning In This Atom | Must Not Mean |
|---|---|---|
| Subject-lineage referent | A referent whose identity continuity is governed by Identity / Lineage | Every record, actor, assignment, process, activity, catalog item, or workflow instance |
| Subject reference | A reference identifying what an event is about when the accepted reference contract or relevant shape requires subject aboutness | Ownership of every lifecycle, workflow, authority, catalog, or reporting behavior for that referent |
| Raw subject reference | The original subject reference written into an accepted event | A value rewritten after merge, split, aliasing, authorization review, or projection rebuild |
| Resolved subject reference | Projection-derived read-side interpretation of a raw subject reference after applying accepted subject-lineage facts | Replacement for the raw event reference or authorization target for historical events |
| Alias | A lineage relation that lets reads interpret a retired subject identity through a continuing subject identity | Mutation of historical events, account merge, group merge, assignment merge, or process matching |
| Merge | An online subject-lineage operation, where required by the baseline, that records alias-in-projection semantics between subject identities | Physical re-reference of historical events or lifecycle ownership over non-subject referents |
| Split | Corrective subject-lineage behavior that preserves frozen history under the source identity while separating later or corrected interpretation | Unmerge that rewrites event history or deletes prior identity facts |
| Lineage projection | Derived read model computed from accepted events and subject-lineage facts over the event subset available to the reader | Canonical event truth, mutable subject record truth, or globally complete state on every device |
| Read-side resolution | Lookup behavior that can present raw and resolved subject-reference facts for consumers | Structural validation, event mutation, authority grant, or conflict-resolution lifecycle |
| Identity-lineage fact | Accepted event or payload fact that contributes to subject continuity, alias, split, archive, or stale-reference interpretation where later shape work accepts it | Final platform-bundled shape inventory or a new envelope `type` |

## Invariants

- Accepted events remain append-only operational facts; Identity / Lineage must not rewrite, delete, redact, or mutate raw references inside historical events.
- Raw subject references remain available for audit, conflict detection, and original-subject authorization checks.
- Resolved subject references are projection-derived and read-side only.
- Authorization for historical events must be checked against the original subject reference written into the event, not a post-merge alias projection.
- Subject lineage applies only to referents with subject-lineage semantics. Actor references, assignment references, activity references, causal references, device identity, and process references keep their own owning boundaries.
- Client-generated event, subject, and record identifiers preserve offline creation; subject-lineage behavior must not require server-allocated identifiers for ordinary offline capture.
- Ordinary offline capture must not require central pre-registration of every referenceable entity.
- Merge, split, and corrective split operations are online-only where the accepted baseline requires server validation.
- Accepted subject-lineage operations must preserve acyclicity. Alias-cycle read-side behavior and resolution semantics remain an open decision and must not be silently defined here.
- Identity / Lineage may surface lineage facts or detector inputs, but Flag / Resolution owns flag lifecycle and accepted resolution effects.
- Identity / Lineage may expose subject-lineage projections, but projections and reports do not become source truth for identity.
- A device or server resolves references only over the event subset and lineage facts available to that context; this atom does not require complete global knowledge on every device.

## Contracts

### Inputs

- accepted immutable events from Event Log / Storage
- raw subject references and typed-reference values carried by the accepted envelope/reference contract or relevant shape
- event subsets delivered under Assignment / Authority / Sync constraints
- subject-lineage operation requests or accepted lineage facts for merge, split, corrective split, archive, or stale-reference interpretation where later shape work defines them
- original event references needed by authorization, conflict, audit, projection, and reporting consumers
- configuration or policy values only where a later accepted atom defines bounded subject-duplicate detection or presentation behavior

### Outputs

- subject identity projection over the available event subset
- alias lineage for accepted subject merges
- split/corrective lineage interpretation that preserves historical references
- raw and resolved subject-reference facts for read-side consumers
- lineage state facts, such as merged, split, archived, or stale-reference interpretation, where supported by accepted lineage facts
- detector inputs for stale, duplicate, or inconsistent subject-lineage cases, without owning flag lifecycle or domain matching policy
- read-only identity-lineage query/projection contract for downstream boundaries

### Boundary Crossings

| Crosses To | Through | Notes |
|---|---|---|
| Event Envelope / Schema | subject and typed references, raw reference values, identity-lineage shape needs where later accepted | Envelope owns reference field contracts and structural validation. Final serialization and active emission sites remain open. |
| Event Log / Storage | accepted immutable events and append-only lineage facts | Storage owns canonical event truth. Identity / Lineage derives projections and never rewrites historical references. |
| Assignment / Authority / Sync | original subject reference, actor/assignment/process references as inputs to authority projection | Authority and sync scope are assignment-derived. Identity / Lineage does not grant access or use resolved aliases as historical authorization targets. |
| Projection / Workflow State | unresolved or resolved subject-lineage facts consumed by process/workflow projections | Workflow owns process identity, pending-match behavior, workflow state, and product queues. |
| Flag / Resolution | lineage detector inputs and source facts for surfaced identity anomalies | Flag / Resolution owns flag identity, lifecycle, blocking behavior, and accepted resolution effects. |
| Configuration | bounded policy values if later accepted for duplicate-candidate rules or subject display | Configuration owns shape/catalog definitions and deployer parameterization. Identity / Lineage does not own deployer-defined catalogs or arbitrary matching logic. |
| Reporting / Aggregation | read-only lineage projections and raw/resolved reference facts | Reports consume derived identity views under access constraints; they do not become source truth. |
| Local Data Lifecycle | scoped event subsets and local availability of lineage facts | Local retain/remove behavior must not mutate central event history or identity lineage facts. |

## Allowed Extension Points

- Implementations may choose storage, indexing, and graph-projection strategies for lineage if raw event references and append-only event truth are preserved.
- Read models may display both raw and resolved subject references if they keep the distinction explicit.
- Later atoms may define platform-bundled subject-lineage shapes only through the platform-bundled shape inventory gap and without adding envelope `type` values.
- Later configuration work may define bounded duplicate-candidate policy values if it does not become deployer-authored lineage algorithms or arbitrary platform code.
- Product surfaces may choose labels for identity review or subject display if they map back to raw/resolved lineage facts and do not become canonical identity truth.

## Forbidden Couplings

- Do not rewrite historical event references after merge, split, archive, or correction.
- Do not treat every typed reference or every referenceable entity as subject-lineage ownership.
- Do not require central pre-registration before structurally valid ordinary offline capture.
- Do not encode referent registration, subject lifecycle, merge, split, conflict detection, or catalog membership as new envelope `type` values.
- Do not make Identity / Lineage own catalogs, descriptive attributes, process lifecycle, assignment lifecycle, workflow state, reporting, or configuration.
- Do not infer authority from resolved aliases, `actor_ref`, accounts, groups, identity-provider claims, tenant context, deployment context, or direct group/IdP authority.
- Do not add `tenant_id`, `deployment_id`, `user_id`, `group_id`, or immutable `authority_context` to identity-lineage behavior as authority fields.
- Do not use `status_changed`, `current_state`, or `pattern_ref` as an identity-lineage shortcut for workflow or process state.
- Do not treat device identity as actor identity.
- Do not make deployment, tenancy, account, or group context part of the subject-lineage graph without formal change control.
- Do not let deployers author identity algorithms, access-control logic, envelope fields, type values, or general platform code through identity configuration.
- Do not make identity anomaly detection own general conflict, flag, or resolution lifecycle.

## Open Gaps

| Gap | Owner / Route | Reopen Trigger |
|---|---|---|
| Final reference serialization and active emission sites | Event Envelope / Schema plus Identity / Lineage, Assignment / Authority / Sync, Configuration, Projection / Workflow State, and owning behavior atoms | A later atom or implementation needs canonical field names, placement, cardinality, or required emission sites for subject, causal, process, assignment, or other typed-reference values. |
| Referent registration, attributes, and catalogs | Event Envelope / Schema for reference contracts; Identity / Lineage for subject-continuity lifecycle; Configuration for shape/catalog definitions; Projection / Workflow State and Assignment / Authority / Sync for process, actor, and assignment lifecycles | A spec needs subject registration events, descriptive attribute mutation/projection, deployer-defined catalogs, platform-bundled registration shapes, or lifecycle ownership for non-subject referents. |
| Alias-cycle read-side behavior and resolution semantics | Identity / Lineage plus Flag / Resolution, with formal architecture decision if included | Identity or flag behavior needs to decide whether cycle-closing lineage facts are rejected, accepted-and-flagged, excluded from projection, or resolved through a specific event/process. |
| Platform-bundled registration or identity shapes | Owning behavior atoms plus Event Envelope / Schema and Configuration | A platform-owned identity, registration, merge, split, or stale-reference fact needs normative `shape_ref` inventory or payload schema. |
| Domain duplicate-detection and matching policy | Configuration plus Flag / Resolution, with Identity / Lineage as source-fact provider | A deployment needs domain-specific matching, duplicate-candidate thresholds, or automated resolution beyond platform-fixed subject-lineage facts. |
| User-facing identity resolution UX | Product/implementation design plus Identity / Lineage and Flag / Resolution | Implementation needs review screens, operator workflows, or display policy for raw/resolved subject references and identity anomalies. |

## Rejected Paths

- Server-allocated identifiers for offline event, subject, or record creation.
- Central pre-registration of every referenceable entity as a prerequisite for structurally valid offline capture.
- Rewriting historical event references to express identity evolution.
- Using post-merge alias projection as the authorization target for historical events.
- Treating actor, assignment, activity, causal, device, or process references as subject-lineage ownership.
- Making shipment, campaign, case, review, transfer, assignment, catalog, or descriptive-profile lifecycle a core Identity / Lineage feature.
- Making Identity / Lineage own general flag lifecycle, conflict-resolution lifecycle, or domain-specific matching policy.
- Treating descriptive attributes, catalog membership, deployer-defined referent shapes, or platform-bundled shape inventory as already accepted subject-lineage facts.
- Adding `tenant_id`, `deployment_id`, `authority_context`, `status_changed`, `current_state`, or `pattern_ref` as identity-lineage shortcuts.
- Using direct group/IdP authority, account identity, tenant context, or deployment context as identity-lineage authority.
- Treating device identity as actor identity.

## Implementation Implications

- Event processors must preserve raw subject references and make read-side resolution additive rather than mutating the event.
- Lineage projections should expose enough information for consumers to distinguish original and resolved subject references.
- Authorization, workflow, conflict, reporting, and local lifecycle designs should consume identity-lineage facts through read-only contracts and keep their own lifecycle responsibilities.
- Offline event creation can create client-generated subject and record identifiers, but merge/split validation may require online coordination where the accepted baseline requires it.
- Implementations should plan tests that prove merge/split behavior does not rewrite historical events and does not authorize historical events through resolved aliases.

## Review Checklist

- [ ] Source basis is accepted and cited.
- [ ] Owner and boundary are singular.
- [ ] Scope and non-scope are explicit.
- [ ] Contracts identify inputs, outputs, and boundary crossings.
- [ ] Open gaps are not closed accidentally.
- [ ] Forbidden couplings include the likely drift risks.
- [ ] No envelope field, type value, authority shortcut, or canonical projection state was added without change control.
- [ ] Product labels, role labels, and UI surfaces remain outside platform-core semantics.

Drafting Agent note:

- This is a draft only. It has not completed Challenge Review, Integration Review, Architecture Steward recommendation, or Decision Board / Project Owner approval.
- This draft intentionally carries forward unresolved reference serialization, referent registration/catalog, alias-cycle, and platform-bundled identity-shape gaps.
