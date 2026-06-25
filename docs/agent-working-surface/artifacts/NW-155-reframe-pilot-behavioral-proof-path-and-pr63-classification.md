# NW-155 - Reframe Pilot Behavioral Proof Path And PR #63 Classification

Status: route note
Date: 2026-06-25
Owner: product/engineering steward
Authority: routing and classification only; does not accept new runtime behavior

## Goal

Define the pilot target in platform behavior terms, not domain terms, before
continuing implementation. PR #63 remains draft-paused until this classification
is reviewed.

## Behavior Slices

| Slice | Needed pilot behavior | Accepted landed evidence |
|---|---|---|
| 1. configured structured capture | A deployer-configured activity/shape can drive structured capture without new platform primitives. | NW-148 server proof publishes configured shape/activity and accepts structured events. NW-150 packages the fixture config under `deploy/reference/pilot-packages/stock-operations/`. NW-152 mobile smoke reads that package through existing mobile config storage. |
| 2. offline local pending work | A field device can save work locally before successful sync, with actor-local pending state. | NW-059 and NW-060 accepted generic mobile sync/pending-state presentation and capture handoff. NW-152 proves the fixture work is stored pending locally before sync. |
| 3. authenticated sync | Pending work reaches the server through the existing authenticated sync path. | NW-037/NW-038/NW-040/NW-070 accept authenticated actor/principal-binding behavior. NW-148 proves authenticated `/api/sync/push` accepts the synthetic server path. NW-152 proves mobile `/api/auth/me` refresh plus sync push for the fixture path. |
| 4. assignment-scoped visibility | Supervisor/operator visibility is constrained by accepted assignment scope and web-admin read authority. | NW-129 accepts the scoped operational report boundary. NW-148 proves a scoped report aggregate for the fixture path. NW-151 hardens the subject-anchor and write-time location proof. NW-154 contains broad subject/timeline helper reads. |
| 5. scoped evidence inspection | A scoped reader can inspect enough current evidence to support pilot proof without broad reporting, export, subject timeline browsing, or domain workflow authority. | Accepted evidence currently covers scoped aggregate standing and limited trace context through NW-129/NW-131, plus raw broad-read containment through NW-154. Field-level configured-work detail inspection is not yet accepted as product/report behavior. |
| 6. explicit principal binding / login path | Pilot actors must resolve through explicit principal binding and an accepted login/auth path, not IdP claims, roles, request actors, or local-only writable actors. | NW-037/NW-038/NW-040/NW-070 accept principal-binding/authenticated-actor behavior. NW-085 accepts mobile login/token lifecycle behavior. NW-086/NW-087/NW-100 accept web-admin session and command authority behavior. NW-150 contains synthetic binding inputs only. |
| 7. local/on-prem operational preflight | A local/on-prem pilot can be checked without approving real users/data, production cutover, cloud transfer, remote support access, or controlled operational use. | NW-093 records the blocked real-production approval package and local/on-prem constraint. NW-150 records non-production local/on-prem fixture assumptions. No full local/on-prem pilot preflight is accepted yet. |

## What PR #63 Proves If Kept

PR #63 proves only this bounded behavior: the existing
`/web-admin/operational/report` surface can render a read-only, assignment-scoped
detail section for visible configured work, using configured activity/shape/field
context where present and without stock-specific reusable controller, template,
service, or model names.

The proof is limited to scoped configured-work detail visibility. It does not
prove the whole pilot path, real production readiness, login/provider setup,
local/on-prem preflight, broad reporting, export/import, review workflow,
warehouse/catalog/session behavior, or production data truth.

## Classification

Classification: B - too broad for the accepted report boundary as currently
selected.

Rationale: PR #63 is not a stock-specific platform UI concept and is not a pure
local hard-code workaround. However, it adds a generic "render configured fields
on the operational report" capability. That is a product/reporting behavior
decision, not an implementation detail of the pilot proof. PR #63 becomes A
only if "scoped evidence inspection" is explicitly selected as the behavior slice
and the accepted boundary says configured field details belong on this report.

It should be treated as C only if review decides the field-detail section exists
solely to make the fixture visible and no generic scoped evidence inspection
behavior is selected.

## Stop / Route Decision

- Keep PR #63 draft-paused; do not merge.
- Do not start the explicit principal binding / login path or local/on-prem
  preflight implementation from the old NW-155 route.
- Do not use "render all configured fields in the report" as the proof mechanism
  unless scoped evidence inspection is first selected as product/report behavior.
- Next decision is owner/steward classification of PR #63:
  - keep and narrow it under an accepted scoped evidence inspection slice;
  - park or revert the runtime implementation and keep only the route note;
  - replace it with a different evidence-inspection proof that stays inside the
    accepted report boundary.

No implementation successor is selected by this note.
