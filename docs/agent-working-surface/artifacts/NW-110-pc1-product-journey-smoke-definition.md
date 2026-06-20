# NW-110 PC1 Product Journey Smoke Definition

Status: non-authoritative product-validation artifact
Document type: product_validation_artifact
Source: NW-110; PC1 PM handoff; accepted PC1 product spec
Authority: planning/proof artifact only; does not add product behavior, runtime behavior, validation policy, CI behavior, real-production approval, or implementation standing
Last reviewed: 2026-06-20

## 1. Purpose

This artifact defines one bounded, domain-neutral synthetic smoke path for PM
and reviewer evaluation of the accepted Product Candidate 1 setup-to-field-loop
story.

It is planning and proof context only. It does not accept runtime behavior,
change product scope, approve real production, select a real domain, or
authorize work with real users or real organizational data.

## 2. Smoke Goal

A setup owner prepares and publishes a basic setup, an assignment coordinator
assigns responsibility, a field user gets assigned work, captures and corrects
an activity entry offline/online, syncs, and a supervisor/reviewer can see
latest synced/freshness/attention wording without implying reporting/export or
broad audit scope.

## 3. Synthetic Setup Fixture

Use one deliberately neutral fixture for the walkthrough:

| Fixture item | Neutral label |
|---|---|
| Organization | `Example Organization` |
| Setup | `Field Activity Setup` |
| Activity | `Site Visit` |
| Work item | `Assigned Visit` |
| Activity entry | `Visit Record` |
| Responsibility | `Assigned route / coverage` |
| Attention item | `Needs review` |

The fixture labels are product-proof labels only. They are not platform
vocabulary authority, contract terms, configuration schema requirements, or
domain commitments. Avoid domain-specific labels such as patient, commodity,
farmer, clinic, warehouse, shipment, CHV, or water point.

## 4. Actors

- Setup owner/config author
- Setup reviewer/approver/publisher
- Assignment coordinator
- Field user
- Supervisor/reviewer
- Deployment owner only as boundary/ops context

These actor labels describe product jobs in the smoke. They do not grant
authority or become runtime primitives; accepted server-resolved actor context,
assignment scope, command capability, resolver rules, and operations policy
remain the authority sources.

## 5. Preconditions

- PC1 product spec accepted.
- Web admin login/session accepted.
- Web admin command gate accepted.
- Setup/config workflow accepted.
- Assignment admin accepted.
- Mobile external login accepted.
- Mobile work/capture/sync/correction accepted in current slices.
- Real production remains blocked by NW-093.
- Analyzer remains known-red/non-blocking and is not a docs-only smoke gate.

## 6. Smoke Path Table

| Step | Actor | User-facing action | Expected observation | Existing accepted support | Evidence category | Stop boundary |
|---|---|---|---|---|---|---|
| 1. Enter Organization | Setup owner/config author | Opens the product surface for `Example Organization`. | The experience presents one Organization and no tenant or workspace selector. | Accepted PC1 one-Organization/default-Workspace product boundary. | PM walkthrough / product wording check. | Stop before tenant-aware runtime, pooled SaaS, workspace selection, or control-plane scope. |
| 2. Sign in to web admin | Setup owner/config author | Signs in to web admin. | A protected web-admin shell is available only after accepted browser session login and server-resolved actor context. | Accepted web admin login/session and command-gated shell standing. | PM walkthrough mapped to accepted web-admin standing. | Stop before IdP claims, generic admin role, request body actor IDs, or root bypass become authority. |
| 3. Prepare setup draft | Setup owner/config author | Creates or edits `Field Activity Setup` with `Site Visit` and `Visit Record` content. | A draft setup exists and is clearly not yet published to devices. | Accepted setup/config authoring workflow over current first vertical. | PM walkthrough / setup-state wording check. | Stop before new schema keys, scripts, triggers, dynamic queries, or config contract changes. |
| 4. Validate setup | Setup owner/config author | Runs validation on the setup candidate. | Validation results are shown as setup issues to fix or as a validated candidate, not as real-production approval. | Accepted setup validation workflow. | PM walkthrough / validation wording check. | Stop before validation policy, CI, config schema, or runtime event semantics change. |
| 5. Readiness review | Setup reviewer/approver/publisher | Performs readiness review for the validated candidate. | Review wording indicates operational readiness context only. | Accepted setup readiness-review step. | PM walkthrough / readiness wording check. | Stop before readiness review becomes workflow-state truth, reporting approval, or production approval. |
| 6. Approve setup | Setup reviewer/approver/publisher | Approves the exact validated setup candidate. | Approval binds to the validated candidate content and does not permit changed content to publish without revalidation. | Accepted approval-by-candidate workflow. | PM walkthrough / approval wording check. | Stop before mutable approvals, unchecked publish, or new authority source. |
| 7. Publish setup | Setup reviewer/approver/publisher | Publishes the approved `Field Activity Setup`. | Devices can later receive one published setup package for the Organization. | Accepted publish workflow and atomic setup/config package standing. | PM walkthrough / publish wording check. | Stop before partial package, per-device variant, tenant-scoped config, schema, or sync-protocol change. |
| 8. Assign responsibility | Assignment coordinator | Assigns the field user `Assigned route / coverage` for `Assigned Visit`. | Responsibility is visible as product language for assigned work and does not imply UI labels grant authority. | Accepted assignment admin create/end workflow and PC1 responsibility wording. | PM walkthrough / assignment wording check. | Stop before new scopes, online policy editing, command authority changes, or actor authority from UI. |
| 9. Field user signs in | Field user | Signs in on mobile. | The mobile session is tied to accepted server-resolved actor alignment. | Accepted mobile external login and actor/session standing. | PM walkthrough / login wording check. | Stop before real users/data, provider/region/support decisions, retention promises, or NW-093 scope. |
| 10. Field user gets work | Field user | Uses get-work/sync to refresh setup, assignments, and visible work. | `Assigned Visit` becomes visible when setup and responsibility are available. | Accepted mobile get-work, assignment-derived access, and sync standing. | PM walkthrough / get-work wording check. | Stop before broad audit pull, hidden sync scope, tenant sync context, or sync protocol change. |
| 11. Field user sees readiness/missing-state wording | Field user | Checks whether the device is ready to capture. | The user sees clear wording for ready-to-capture, missing setup/forms, missing assignment, retry, or syncing states as applicable. | Accepted PC1 mobile readiness and missing-state language. | PM walkthrough / state wording check. | Stop before advisory mobile wording becomes authoritative rejection or production connectivity guarantee. |
| 12. Field user captures activity entry | Field user | Captures a `Visit Record` for the assigned work, offline or online. | The entry is saved as configured field work without implying server acceptance while offline. | Accepted mobile capture, offline/online work, and append-only event standing. | PM walkthrough / capture state check. | Stop before event shape, envelope type, server authority, or validation semantics change. |
| 13. Field user sees saved-local / waiting-to-sync state | Field user | Reviews the just-captured entry before sync completes. | The entry remains visible as saved locally or waiting to sync. | Accepted saved-local, waiting-to-sync, syncing, synced, failed, and retry state vocabulary. | PM walkthrough / local-state wording check. | Stop before retention/security, no-local-retention, encryption, erasure, or decommissioning promises. |
| 14. Field user appends correction | Field user | Adds a correction to the `Visit Record`. | The correction is presented as a new append-only activity entry, not an overwrite. | Accepted correction and append-only product boundary. | PM walkthrough / correction wording check. | Stop before mutable records, correction-linkage metadata, or envelope/schema changes. |
| 15. Field user syncs | Field user | Syncs pending work when connectivity is available. | The user can see syncing, synced, failed, retry, and latest local pending-work state for the actor/device context. | Accepted mobile sync states and actor/device sync standing. | PM walkthrough / sync-state wording check. | Stop before sync protocol changes, broad historical pull, cross-actor local access, or production connectivity guarantees. |
| 16. Supervisor/reviewer sees latest synced/freshness/attention wording | Supervisor/reviewer | Reviews the latest synced `Visit Record`, freshness wording, and one narrow `Needs review` attention cue if present. | The view is narrow operational context, not reporting, export, analytics, warehouse, broad audit, or conflict-operations scope. | Accepted PC1 freshness/attention wording and PM handoff boundary; minimal operational view remains a successor candidate if current support is insufficient. | PM walkthrough / scoped oversight wording check. | Stop before dashboards, exports, broad read APIs, aggregate analytics, batch resolution, auto-resolution, resolver reassignment, or flag reporting. |

## 7. Expected Result

The smoke passes as a PM smoke when:

- PM can explain the PC1 loop end to end.
- Every step maps to accepted PC1 behavior or a clearly marked successor
  candidate.
- No step requires product scope expansion.
- No step requires real users/data.
- No step requires domain-specific vocabulary.

## 8. Known Gaps / Successor Candidates

These are candidate-only follow-ups. NW-110 does not create new backlog rows or
accept any of these routes:

- Product demo script / synthetic walkthrough.
- Mobile get-work/readiness/capture/correction polish if the walkthrough
  exposes friction.
- Setup/config structured-editor polish if JSON setup blocks PM proof.
- Assignment admin proof polish if responsibility language is unclear.
- Minimal scoped freshness/attention view if current accepted support is
  insufficient.
- Vocabulary validation packet after the neutral smoke exists.
- PC1 product journey smoke test/automation later, if selected.

## 9. Validation Category

- Docs-only/product-validation artifact.
- Use `git diff --check`.
- No runtime tests are required because no runtime code, tests, contracts, CI
  behavior, validation policy, or product scope changed.
- Future implementation rows must use
  `docs/agent-working-surface/validation-matrix.md`.

## 10. Stop Conditions

Stop before proceeding if the smoke path requires:

- real domain/pilot selection;
- real users/data;
- product-scope change;
- reporting/export;
- retention/security promises;
- entity lifecycle;
- conflict automation;
- tenant/control-plane work;
- contract/schema/sync changes;
- architecture/gap routing;
- runtime implementation.
