# Planned Consumer Review Cards

Status: Draft review surface

This file gives Integration Review a surface for planned downstream atoms that do not yet have atom files. It prevents upstream foundation atoms from being accepted on hidden assumptions while avoiding premature downstream drafting.

These cards are not spec atoms, not implementation authority, and not accepted downstream contracts. When a planned atom is drafted, its atom file must reconcile or supersede the relevant card.

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

Integration Review question:

- Can `SPEC-005` draft identity-lineage behavior later while consuming foundation references as contracts only, without needing `SPEC-002`, `SPEC-003`, or `SPEC-004` to decide referent lifecycle ownership?

Outcome: Pending Integration Review.

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
- configuration authoring and deploy-time validation UX

Integration Review question:

- Can `SPEC-006` define configuration and validation surfaces later while consuming foundation `shape_ref` and `activity_ref` as narrow contracts, without needing foundation atoms to accept configuration packaging or schema tooling?

Outcome: Pending Integration Review.

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
- temporary authority, revocation, and offline grace policy
- sync delivery mechanics
- local purge/lifecycle rules for sensitive data

Integration Review question:

- Can `SPEC-007` draft authority reconstruction and sync delivery later while consuming foundation atoms as event, envelope, reference, and storage contracts only, without needing foundation atoms to decide authority policy or sync mechanics?

Outcome: Pending Integration Review.
