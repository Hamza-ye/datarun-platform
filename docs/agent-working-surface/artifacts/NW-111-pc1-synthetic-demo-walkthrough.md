# NW-111 PC1 Synthetic Demo Walkthrough

Status: non-authoritative product-validation artifact
Document type: product_validation_artifact
Source: NW-111; NW-110 smoke definition; PC1 PM handoff; accepted PC1 product spec
Authority: planning/demo artifact only; does not add product behavior, runtime behavior, validation policy, CI behavior, real-production approval, or implementation standing
Last reviewed: 2026-06-20

## 1. Purpose

This artifact turns the NW-110 smoke definition into a step-by-step synthetic
demo script for the Product Candidate 1 setup-to-sync story.

It is for PM and reviewer comprehension only. It does not authorize
implementation, approve real production, select a real domain, add product
scope, or create validation policy.

## 2. Demo Fixture

Use the exact domain-neutral fixture from NW-110:

| Fixture item | Neutral label |
|---|---|
| Organization | `Example Organization` |
| Setup | `Field Activity Setup` |
| Activity | `Site Visit` |
| Work item | `Assigned Visit` |
| Activity entry | `Visit Record` |
| Responsibility | `Assigned route / coverage` |
| Attention item | `Needs review` |

These labels are product-proof labels only. They are not platform vocabulary
authority, contract terms, schema requirements, or domain commitments.

## 3. Demo Script

| Sequence | Actor | Demo action | What the reviewer should observe | Accepted support / source | If this fails, candidate follow-up |
|---|---|---|---|---|---|
| 1. Enter Organization | Setup owner/config author | Open the product surface for `Example Organization`. | The first-view product context is one Organization, with no tenant or workspace selector. | Accepted PC1 one-Organization/default-Workspace boundary; NW-110 step 1. | Organization-entry vocabulary or navigation polish. |
| 2. Sign in to web admin | Setup owner/config author | Sign in to web admin. | The web-admin surface appears only after the accepted browser session login and server-resolved actor context. | Accepted web-admin login/session and command-gated shell standing; NW-110 step 2. | Web-admin entry wording or sign-in demo setup notes. |
| 3. Prepare setup draft | Setup owner/config author | Create or edit `Field Activity Setup` with `Site Visit` and `Visit Record` content. | The setup is visibly a draft and is not yet delivered to field devices. | Accepted setup/config first vertical; PC1 setup journey; NW-110 step 3. | Setup/config structured-editor polish. |
| 4. Validate setup | Setup owner/config author | Run validation on the setup candidate. | Validation results are framed as setup issues to fix or as a validated candidate, not production approval. | Accepted setup validation workflow; NW-110 step 4. | Setup validation wording or example-data polish. |
| 5. Readiness review | Setup reviewer/approver/publisher | Perform readiness review for the validated candidate. | Readiness wording reads as operational review context only. | Accepted PC1 readiness-review product state; NW-110 step 5. | Readiness review wording polish. |
| 6. Approve setup | Setup reviewer/approver/publisher | Approve the exact validated setup candidate. | Approval binds to the validated content; changed content would need validation and approval again. | Accepted approval-by-candidate behavior; PC1 setup states; NW-110 step 6. | Approval-state wording polish. |
| 7. Publish setup | Setup reviewer/approver/publisher | Publish the approved `Field Activity Setup`. | The published setup is one atomic package that devices can later receive for the Organization. | Accepted publish workflow and config-package standing; NW-110 step 7. | Publish-state wording or demo setup notes. |
| 8. Assign responsibility | Assignment coordinator | Assign the field user `Assigned route / coverage` for `Assigned Visit`. | Responsibility language explains assigned work without implying UI labels grant authority. | Accepted assignment admin create/end workflow; PC1 responsibility language; NW-110 step 8. | Assignment proof polish. |
| 9. Field user signs in | Field user | Sign in on mobile. | The mobile session is tied to accepted server-resolved actor alignment. | Accepted mobile external login and actor/session standing; NW-110 step 9. | Mobile sign-in wording or demo environment notes. |
| 10. Field user gets work | Field user | Use get-work/sync to refresh setup, responsibilities, and visible work. | `Assigned Visit` appears when setup and responsibility are available. | Accepted mobile get-work, assignment-derived access, and sync standing; NW-110 step 10. | Mobile get-work/readiness polish. |
| 11. Field user sees readiness/missing states | Field user | Check whether the device is ready to capture. | The device clearly distinguishes ready-to-capture, missing setup/forms, missing assignment, retry, and syncing states where applicable. | Accepted PC1 mobile readiness and missing-state language; NW-110 step 11. | Mobile readiness/missing-state polish. |
| 12. Field user captures activity entry | Field user | Capture a `Visit Record` for the assigned work, offline or online. | The entry is saved as configured field work without implying server acceptance while offline. | Accepted mobile capture, offline/online work, and append-only event standing; NW-110 step 12. | Mobile capture polish. |
| 13. Field user sees saved-local/waiting-to-sync | Field user | Review the just-captured entry before sync completes. | The entry remains visible as saved locally or waiting to sync. | Accepted saved-local, waiting-to-sync, syncing, synced, failed, and retry state vocabulary; NW-110 step 13. | Mobile local/sync-state polish. |
| 14. Field user appends correction | Field user | Add a correction to the `Visit Record`. | The correction is presented as a new append-only entry, not an overwrite. | Accepted correction and append-only product boundary; NW-110 step 14. | Mobile correction polish. |
| 15. Field user syncs | Field user | Sync pending work when connectivity is available. | The user can see syncing, synced, failed, retry, and latest local pending-work state for the actor/device context. | Accepted mobile sync states and actor/device sync standing; NW-110 step 15. | Mobile sync presentation polish or later smoke automation. |
| 16. Supervisor/reviewer sees latest synced/freshness/attention wording | Supervisor/reviewer | Review the latest synced `Visit Record`, freshness wording, and one narrow `Needs review` cue if present. | The view is narrow operational context, not reporting, export, analytics, broad audit, or conflict-operations scope. | Accepted PC1 freshness/attention wording and PM handoff boundary; NW-110 step 16. | Minimal freshness/attention view, single attention route, or vocabulary validation packet. |

## 4. Demo Pass Criteria

The demo passes if:

- a reviewer can follow the PC1 loop end to end;
- the neutral vocabulary is understandable;
- no step depends on real users or real organizational data;
- no step depends on reporting/export, retention/security, entity lifecycle,
  conflict automation, tenant/control-plane work, or real-production approval;
- any friction is recorded as candidate follow-up, not fixed inside this NW.

## 5. Candidate Follow-Up Capture

| Observed friction | Candidate route | Evidence needed | Stop boundary |
|---|---|---|---|
| JSON setup editing blocks reviewer comprehension. | Setup/config structured-editor polish. | Walkthrough note showing which setup step blocked comprehension. | No config schema, expression, package, validation-policy, or runtime behavior change. |
| Field readiness, capture, correction, or sync states are hard to follow. | Mobile readiness/capture/correction polish. | Reviewer note tied to the exact mobile sequence and expected observation. | No sync protocol, event semantics, authority, login, or retention/security change. |
| Responsibility/assignment proof is unclear. | Assignment proof polish. | Coordinator/reviewer note showing which responsibility wording or step failed. | No new scopes, command authority, online policy editing, or UI actor authority. |
| Latest synced, freshness, or `Needs review` wording is not demonstrable. | Minimal freshness/attention view. | Walkthrough note showing the smallest missing reviewer observation. | No dashboard, export, warehouse, broad read API, batch review, or conflict automation. |
| Neutral labels still confuse the PC1 story. | Vocabulary validation packet. | Reviewer notes on unclear terms and acceptable replacements. | No platform vocabulary, contract, schema, or product-scope rewrite. |
| Manual walkthrough becomes repetitive after the path is understood. | Smoke automation later. | Accepted script plus stable demo fixture and expected observations. | No CI/validation-policy change without a selected automation NW. |

## 6. Validation Category

Docs-only/product-validation artifact.

Runtime tests are skipped because no runtime code, tests, contracts, CI
behavior, validation policy, or product scope changed.

## 7. Stop Conditions

Stop if the demo requires:

- real domain or real pilot selection;
- real users/data;
- product-scope change;
- runtime implementation;
- reporting/export;
- retention/security promises;
- entity lifecycle;
- conflict automation;
- tenant/control-plane;
- contract/schema/sync changes;
- architecture/gap routing.
