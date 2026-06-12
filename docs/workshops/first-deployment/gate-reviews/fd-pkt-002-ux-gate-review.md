# FD-PKT-002 UX Gate Review

## Header

Role: UX owner / UX Architect for first-deployment gate review.

Date: 2026-06-13.

Gate status: `UNBLOCK WITH CONDITIONS` for Project Shepherd consolidation of product/spec/UX validation. This review does not authorize implementation.

Files reviewed:

- `AGENTS.md`
- `docs/status.md` Current Routing
- `docs/agent-working-surface/first-deployment-task-packet-router.md`
- `docs/workshops/first-deployment/README.md`
- `docs/workshops/first-deployment/stage-3-ux.md`
- `docs/workshops/first-deployment/stage-8-task-packet-backlog.md`
- `docs/workshops/first-deployment/task-packets/fd-pkt-002-candidate-1-product-spec-ux-validation.md`
- `docs/workshops/first-deployment/task-packets/fd-pkt-003-candidate-1-evidence-plan.md`
- `docs/workshops/first-deployment/task-packets/fd-pkt-004-candidate-1-mobile-offline-validation.md`
- `docs/workshops/first-deployment/task-packets/fd-pkt-005-candidate-1-view-model-contract-assessment.md`
- `docs/workshops/first-deployment/task-packets/fd-pkt-101-s06-entity-lifecycle-discovery.md`

Authority/source order: CDL, contracts, active status, BAR, and NW remain binding authority. Workshop and UX language may explain current behavior and validation copy, but it must not create authority, lifecycle truth, contracts, schema/API meaning, implementation scope, or product scope by itself.

## UX Gate Finding for FD-PKT-002

FD-PKT-002 is UX-acceptable as a product/spec and vocabulary validation packet. It is clear enough for Project Shepherd to consolidate into later implementation-packet inputs, provided those later packets remain bounded and continue to treat Candidate 1 implementation as blocked until FD-PKT-002 through FD-PKT-005 are gated and FD-PKT-101 resolves, promotes, splits, or explicitly excludes the S06 dependency.

The packet correctly frames Candidate 1 as an S01-compatible operational capture path: setup comprehension, assigned work, standalone capture, optional linked capture, unlinked/candidate capture, local save, sync states, append-only correction, latest synced interpretation, needs-review visibility, access-changed explanation, and shared-device wording only if claimed.

The UX risk is not missing structure; the risk is over-claiming. Later UI/copy work must not turn user-facing labels into platform vocabulary, event fields, lifecycle state, registry truth, access authority, production readiness, or shared contracts.

## Candidate 1 Vocabulary Decision

Decision: Candidate 1 vocabulary is acceptable for validation and later UI/copy drafting only as user-facing copy over existing backing sources. It is not accepted as platform terminology, contract vocabulary, event vocabulary, authority vocabulary, or lifecycle vocabulary.

Required wording and limits:

| Concept | Use this wording | Avoid or reject this implication |
|---|---|---|
| Known thing | `known thing` only as validation copy, or replace with a deployment-specific noun when Product/SME evidence supports it. | Do not imply maintained registry truth, active/current status, verification, lifecycle state, or source-of-known-set authority. |
| Linked | `linked record` or `record linked to an existing known thing`. | Do not imply verified, current, canonical, lifecycle-approved, or registry-maintained. |
| Unlinked | `unlinked record`. | Do not imply failed work, rejected work, deleted work, or automatic matching. |
| Candidate | `candidate for review` or `candidate evidence for review`. | Do not imply registry creation, candidate promotion, discovered-unit lifecycle, or automatic match/merge. |
| Saved locally | `saved locally` or `saved on this device`. | Do not imply server received it, supervisor can see it, approval, review, conflict-free status, retention guarantee, or recovery after device loss. |
| Waiting to sync | `waiting to sync`. | Do not imply server acceptance, approval, live truth, or durable workflow state. |
| Synced | `synced` only for completed server exchange for the resolved actor/scope. | Do not imply reviewed, approved, globally complete, conflict-free, report-complete, or live field truth. |
| Synced with issue | `synced with issue`. | Do not imply hard rejection, automatic resolution, direct flag mutation, review queue, resolver reassignment, or broad review authority. |
| Failed to sync | `failed to sync` plus `still saved on this device` where needed. | Do not imply data loss, server rejection, token refresh/logout, mobile login, sealed recovery, or guaranteed support recovery. |
| Latest synced | `latest synced` or `latest synced view`. | Do not imply live field reality, audit completeness, full reporting truth, or supervisor approval. |
| Needs review | `needs review`, `issue`, or `attention item`. | Do not imply hard rejection, production review queue, batch bypass, auto-resolution, resolver reassignment, or direct flag mutation. |
| Correction | `correction`, `update`, or `amended record`. | Do not imply erasing history, editing in place, replacement, cancellation, approval, conflict resolution, resolver reassignment, or auto-resolution. |
| Access changed | `access changed`, `access ended`, or `no longer assigned`. | Do not imply deletion, security erasure, device decommissioning, retention behavior, local hard rejection, or lifecycle truth. |
| Shared device, if claimed | `shared device` only with `current user`, `pending work`, `switch user`, and `saved work stays separate` language. | Do not imply cross-actor visibility, local actor authority, retention/security guarantee, sealed-partition recovery, local encryption, decommissioning, or support recovery. |

Persona and operational labels remain acting contexts only. `setup owner`, `field user`, `supervisor/reviewer`, `operator/support`, `auditor`, and `shared-device user` must always map to:

```txt
actor + active assignment + role + scope + time + activity/context
-> available actions and visible data
-> projected operational surface
```

## Required Walkthrough Evidence Before Implementation

Before any Candidate 1 implementation packet drafts UI/copy work, Project Shepherd should require recorded walkthrough evidence for:

- Setup comprehension: setup artifact, assigned work, publish handoff, responsibility model, and no architecture vocabulary.
- Standalone capture: assigned work to offline local save to later sync.
- Subject-linked capture: existing known thing selection and linked-record interpretation without lifecycle truth.
- Missing-known-thing capture: unlinked/candidate record that needs matching/review without registry creation.
- Failed sync recovery: preserved pending work, retry/support path, and no data-loss or server-rejection inference.
- Correction: appended correction/update with original history preserved.
- Supervisor freshness: latest synced view, timestamps, unresolved issue signals, and no live-truth or report-completeness inference.
- Needs-review: view-only versus allowed action, unresolved issue status, and exact designated-resolver limits.
- Access-changed/stale-access: advisory local warning or access-ended copy without deletion, retention, security, or mobile rejection claims.
- Shared-device A-to-B switch if claimed: current actor visibility, pending warning, drain-or-seal behavior, actor partition isolation, and safe resume or sealed state.
- Operator/support recovery: invalid token, setup/connect problem, sync failure, access ended, and pending work using constrained-deployment wording.

Each walkthrough must record acting context, authority mapping, scenario script, exact vocabulary tested, participant or SME feedback, comprehension risks, S06/successor-lane triggers, and pass/revise/route outcome.

## S06-Sensitive Copy Limits

S06-sensitive terms may be tested, but they must not become Candidate 1 truth. `known thing`, `candidate`, `unlinked`, `duplicate suspected`, `inactive`, `closed`, `moved`, `retired`, `verified`, `merged`, and `split` require S06-sensitive review before appearing in UI/copy work.

Allowed Candidate 1 copy: linked record, unlinked record, known thing as validation copy, candidate for review, missing known thing, needs review, duplicate suspected as review-oriented language, latest synced, saved locally, waiting to sync, synced, synced with issue, failed to sync, access changed, and correction/update as append-only.

Forbidden as Candidate 1 truth unless FD-PKT-101 promotes and routes S06: canonical known-thing registry creation, active/inactive/retired/closed/moved/verified lifecycle state, discovered-unit lifecycle, candidate promotion, registry stewardship workflow, duplicate handling workflow, merge/split UX, automatic matching, mobile-side lifecycle rejection, and maintained known-set source authority.

## Implementation Packet UX Conditions

Later implementation packets may draft UI/copy work only if they include:

- One bounded surface only, with exact files/contracts, accepted constructs reused, excluded successor lanes, forbidden work, targeted tests or evidence, manual walkthrough references, stop/report conditions, and commit boundary.
- A copy glossary using the required wording table above and marking each term as user-facing copy, not platform truth.
- A state-to-source checklist mapping every visible state to existing backing sources: local event store, pending push, sync metadata, flags/advisories, projections, assignment scope, config state, subject refs where available, and actor partitions.
- A walkthrough evidence reference for every state or label the implementation exposes.
- A claim-wording check proving no lifecycle, production-readiness, retention/security, reporting/export, production auth/mobile login, custom scope, conflict automation, resolver reassignment, auto-resolution, or data-loss wording entered Candidate 1.
- A persona/acting-context authority mapping for every setup, field, reviewer, support, auditor, or shared-device label.
- An S06 disposition from FD-PKT-101 naming whether S06 is excluded, promoted, split, or pre-release-gated for Candidate 1 honesty.

## Explicit Implementation Unblock Position: `UNBLOCK WITH CONDITIONS`

`UNBLOCK WITH CONDITIONS`.

FD-PKT-002 is unblocked for Project Shepherd consolidation and for later implementation-packet drafting as UX input. Candidate 1 implementation itself remains blocked. The unblock applies only if the conditions in this review are carried forward, FD-PKT-003/004/005 evidence gates remain active, and FD-PKT-101 resolves or explicitly excludes the S06 dependency before implementation dispatch.

## Stop/Route Conditions

Stop and route before implementation if:

- Candidate 1 copy or UI makes candidate/unlinked/missing-known-thing capture canonical registry truth.
- Users cannot understand candidate-only handling without maintained known things, lifecycle state, registry stewardship, duplicate stewardship, merge/split UX, or candidate promotion.
- A user-facing term becomes an event field, event type, scope mechanism, flag category, schema/contract field, durable workflow state, authority rule, lifecycle truth, shared API meaning, or stable shared view-model contract without routing.
- `latest synced`, `saved locally`, `failed to sync`, `needs review`, `correction`, or `access changed` implies live truth, server receipt, approval, rejection, data loss, deletion, retention/security, or history rewrite.
- Mobile warnings become authoritative rejection or local authority.
- Shared-device copy requires recovery, retention/security, local encryption, decommissioning, sealed-partition recovery, or cross-actor visibility.
- Persona or operational labels harden into identity categories, fixed modules, access rules, config namespaces, product-area boundaries, authority primitives, or implementation service boundaries.
- Production readiness, production auth/admin/mobile login, token lifecycle, retention/security, reporting/export/import, conflict automation, resolver reassignment, auto-resolution, custom/query scope, or ops readiness enters Candidate 1.
- Active routing, CDL/contracts/status/BAR/NW, packet inputs, or evidence conflict with this UX gate position.
