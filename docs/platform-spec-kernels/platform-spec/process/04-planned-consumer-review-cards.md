# Planned Consumer Review Cards

Status: Draft review surface

This file gives Integration Review a surface for planned downstream atoms that do not yet have atom files. It prevents upstream foundation atoms from being accepted on hidden assumptions while avoiding premature downstream drafting.

These cards are not spec atoms, not implementation authority, and not accepted downstream contracts. When a planned atom is drafted, its atom file must reconcile or supersede the relevant card.

The Batch 1B foundation acceptance pass used three card groups:

- Batch 2 immediate consumers: `SPEC-005`, `SPEC-006`, and `SPEC-007`.
- Direct registry consumer with projection/workflow risk surface: `SPEC-009`, because workflow state and pattern behavior directly consume foundation vocabulary, event truth, and envelope/shape separation.
- Direct registry consumer with conflict/flag risk surface: `SPEC-010`, because conflict/flag behavior directly depends on the foundation split between structural validation, append-only fact preservation, and later anomaly surfacing.

## Use Rule

Use a planned-consumer review card only when all of these are true:

- an upstream atom is being considered for acceptance
- an immediate downstream consumer is listed in `atom-registry.yml`
- the downstream consumer is still `planned` and has no atom file
- Integration Review needs to check whether the upstream atom is safe to accept

The card may identify required inputs, forbidden assumptions, carried gaps, and rework triggers. It must not define downstream behavior, close downstream gaps, or introduce implementation requirements.

## Review Outcomes

Each card must end with one of these outcomes:

- `Clear`: upstream atom can be accepted without hidden downstream assumptions
- `Rework upstream`: upstream atom must be narrowed, clarified, or moved back to draft
- `Carry explicit gap`: upstream atom can be accepted only if a named gap stays visible
- `Escalate`: acceptance requires change control or Decision Board resolution

## SPEC-005 Planned Consumer Card

Consumer: `SPEC-005` References And Identity Lineage

Supersession note: this planned-consumer card was used for Batch 1B foundation acceptance and is superseded by accepted `atoms/05-references-and-identity-lineage.md`. It remains here as review evidence, not as current downstream authority.

Foundation upstream atoms under review:

- `SPEC-002` Glossary And Core Definitions
- `SPEC-003` Event Log And Storage
- `SPEC-004` Event Envelope And Schema

Consumption needs:

- stable meanings for raw reference, resolved reference, subject reference, typed reference, actor reference, causal reference, and event identity
- assurance that historical event references are preserved in append-only event truth
- envelope wording that allows subject and typed references where required without making every referent an Identity / Lineage lifecycle subject

Forbidden hidden assumptions:

- central pre-registration of every referenceable entity before offline capture
- treating every referenceable entity as subject-lineage ownership
- rewriting historical event references after identity evolution
- using post-merge alias projection as the authorization target for historical events
- making Identity / Lineage own assignment, authority, process lifecycle, workflow, reporting, catalogs, or deployer-defined descriptive attributes

Carried gaps:

- final reference serialization and active emission sites
- referent registration, attributes, and catalogs
- alias-cycle read-side behavior and resolution semantics

Historical Integration Review question:

- Can `SPEC-005` draft identity-lineage behavior later while consuming foundation references as contracts only, without needing `SPEC-002`, `SPEC-003`, or `SPEC-004` to decide referent lifecycle ownership?

Outcome: Carry explicit gap.

## SPEC-006 Planned Consumer Card

Consumer: `SPEC-006` Configuration And Parameterization

Foundation upstream atoms under review:

- `SPEC-002` Glossary And Core Definitions
- `SPEC-004` Event Envelope And Schema

Consumption needs:

- stable meaning and format for `shape_ref`
- stable distinction between envelope `type`, payload shape, activity context, pattern, projection, and product/deployer labels
- stable optional `activity_ref` semantics that preserve configured activity context without making it authority, workflow state, or product queue identity
- deployer parameterization definition that keeps deployer variation inside platform-owned mechanisms

Forbidden hidden assumptions:

- deployers can define envelope fields or structural `type` values
- deployers can author arbitrary platform code, access-control logic, state-machine mechanisms, or detector logic
- `shape_ref` can encode workflow state, authority, product surface, role, tenant, deployment, or online/offline status
- `activity_ref` can encode authority context, pattern identity, tenant/deployment identity, or work-item identity
- platform-bundled shapes are a general platform-owned domain schema catalog

Carried gaps:

- event schema/versioning tooling
- formal envelope serialization details
- platform-bundled shape inventory
- formal Pattern Registry schema
- configuration versioning and stale-configuration reconciliation
- configuration authoring and deploy-time validation UX

Integration Review question:

- Can `SPEC-006` define configuration and validation surfaces later while consuming foundation `shape_ref` and `activity_ref` as narrow contracts, without needing foundation atoms to accept configuration packaging or schema tooling?

Outcome: Carry explicit gap.

## SPEC-007 Planned Consumer Card

Consumer: `SPEC-007` Assignment, Authority, And Sync

Foundation upstream atoms under review:

- `SPEC-002` Glossary And Core Definitions
- `SPEC-003` Event Log And Storage
- `SPEC-004` Event Envelope And Schema

Intermediate planned dependencies to reconcile later:

- `SPEC-005` References And Identity Lineage
- `SPEC-006` Configuration And Parameterization

Consumption needs:

- stable meanings for assignment, access scope, sync scope, authority projection, original subject authorization, actor reference, activity reference, subject or typed references, device identity, and event subset
- append-only event truth and projection-derived current state
- envelope metadata for authorship, activity context, device identity, intra-device ordering, and cross-device concurrency detection without storing authority snapshots

Forbidden hidden assumptions:

- `actor_ref`, account identity, group membership, IdP claim, tenant, or deployment is a direct authority source
- event envelopes store immutable `authority_context`
- sync scope is an entitlement model separate from assignment-derived access scope
- `device_id` is actor identity
- `activity_ref` grants authority
- ordinary offline capture requires complete global knowledge
- sync transport, pagination, priority, and bandwidth mechanics are already decided by foundation atoms

Carried gaps:

- shared-device multi-actor sessions
- auditor access and subject-based scope
- cross-level distribution visibility
- permission table and activity/context authority details
- temporary authority, revocation, and offline grace policy
- sync delivery mechanics
- local purge/lifecycle rules for sensitive data

Integration Review question:

- Can `SPEC-007` draft authority reconstruction and sync delivery later while consuming foundation atoms as event, envelope, reference, and storage contracts only, without needing foundation atoms to decide authority policy or sync mechanics?

Outcome: Carry explicit gap.

## SPEC-009 Planned Consumer Card

Consumer: `SPEC-009` Projections, Workflow, And Patterns

Foundation upstream atoms under review:

- `SPEC-002` Glossary And Core Definitions
- `SPEC-003` Event Log And Storage
- `SPEC-004` Event Envelope And Schema

Intermediate planned dependencies to reconcile later:

- `SPEC-005` References And Identity Lineage
- `SPEC-006` Configuration And Parameterization
- `SPEC-007` Assignment, Authority, And Sync

Consumption needs:

- stable meanings for projection, read model, workflow state, pattern, Pattern Registry, participant capacity, source event, source chain, and event subset
- append-only event truth where workflow state is rebuilt from immutable events plus relevant configuration and pattern definitions
- envelope `type`, `shape_ref`, payload, references, and timestamps as projection inputs without making workflow state an envelope field
- stable distinction between `type=review`, review payload shapes, review patterns, review queues, and reviewer labels

Forbidden hidden assumptions:

- `current_state`, `pattern_ref`, `status_changed`, queue state, review status, or work-item identity is canonical event/envelope state
- exact Pattern Registry inventory, pattern skeletons, or formal pattern schema are decided by foundation atoms
- invalid workflow transitions are structurally rejected where the accepted baseline requires accept-and-flag
- product queues, review lists, returned-work views, oversight counts, pending labels, or stale labels are storage primitives
- source-chain traversal depth or source-only cascade beyond ADR-005 workflow cases is already accepted as general flag behavior
- `shape_ref`, `activity_ref`, role labels, or product surfaces can encode workflow state or pattern identity

Carried gaps:

- exact Pattern Registry inventory
- formal Pattern Registry schema
- projection compatibility across schema versions
- source-chain traversal depth limits
- platform-bundled shape inventory where workflow or review behavior needs a platform-owned fact shape
- configuration versioning and stale-configuration reconciliation

Integration Review question:

- Can `SPEC-009` draft projection, workflow, and pattern behavior later while consuming `SPEC-002`, `SPEC-003`, and `SPEC-004` as vocabulary, append-only storage, and structural-envelope contracts only, without requiring foundation atoms to decide pattern inventory, workflow state storage, product queue semantics, or configuration-version reconciliation?

Outcome: Carry explicit gap.

## SPEC-010 Planned Consumer Card

Consumer: `SPEC-010` Conflict, Flag, And Resolution

Foundation upstream atoms under review:

- `SPEC-002` Glossary And Core Definitions
- `SPEC-003` Event Log And Storage
- `SPEC-004` Event Envelope And Schema

Intermediate planned dependencies to reconcile later:

- `SPEC-005` References And Identity Lineage
- `SPEC-006` Configuration And Parameterization
- `SPEC-007` Assignment, Authority, And Sync
- `SPEC-009` Projections, Workflow, And Patterns

Consumption needs:

- stable distinction between structural validation and accept-and-flag anomaly handling
- append-only event truth where flags and resolutions affect interpretation/projection, not historical event identity
- envelope `type` as processing vocabulary, with identity/integrity/conflict facts routed through shapes, payload, references, projections, and later flag contracts
- source-event and source-chain vocabulary that permits causal/source links where required by shape or owning boundary without adding a universal envelope source field
- `actor_ref` and system actor convention available for later eligible auto-resolution attribution without deciding general resolution-event mapping

Forbidden hidden assumptions:

- `SPEC-004` already decides general flag lifecycle, flag identity, detector ownership, flag creation location, or resolution-event type mapping
- conflict detected, conflict resolved, subjects merged, subject split, cycle violation, or transition state becomes an envelope `type`
- state, authority, workflow, identity-lineage, configured-domain, or reporting anomalies become structural envelope invalidity
- malformed envelopes or payloads are accepted under accept-and-flag
- every detector runs before event persistence or every ordinary offline capture has complete global knowledge
- source-chain traversal depth, source-only cascade beyond closed workflow cases, or server-created flag default is already accepted
- flagged or unresolved events can create irreversible downstream work before relevant checks run

Carried gaps:

- general flag semantics beyond accepted workflow cases
- flag event identity, creation location, and resolution-event type mapping
- platform-bundled integrity/identity/conflict shape inventory
- alias-cycle read-side behavior and resolution semantics
- domain conflict automation outside workflow
- source-chain traversal depth limits
- auto-resolution authoring and monitoring
- temporary authority, revocation, and offline grace policy where late authorization anomalies may surface as flags

Integration Review question:

- Can `SPEC-010` draft conflict, flag, and resolution behavior later while consuming `SPEC-002`, `SPEC-003`, and `SPEC-004` as vocabulary, append-only storage, and structural-envelope contracts only, without requiring foundation atoms to decide detector ownership, flag lifecycle, conflict shape inventory, or resolution mapping?

Outcome: Carry explicit gap.
