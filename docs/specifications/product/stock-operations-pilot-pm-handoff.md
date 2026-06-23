# Stock Operations Pilot PM Handoff

Status: active PM handoff surface
Document type: product_handoff
Owner: product steward
Source: NW-149; PR #58 / NW-148 synthetic stock operations first-flow proof;
NW-150 stock operations pilot package skeleton; NW-151 subject-anchor hardening
Authority: derived planning surface only; does not add product behavior,
runtime implementation, production approval, architecture authority,
validation policy, contracts, schemas, BAR, CDL, or gap-register standing
Last reviewed: 2026-06-24
Supersedes: none
Related: `../../agent-working-surface/platform-next-work-backlog.md`;
`../../agent-working-surface/prompts/NW-149-stock-operations-pilot-goal-and-ordered-backlog.md`;
`../../../server/src/test/java/dev/datarun/server/e2e/SyntheticStockOperationsFirstFlowIntegrationTest.java`;
`../../agent-working-surface/validation-matrix.md`;
`../../implementation/module-interfaces.md`

## Product Goal

Datarun should run the first local/on-prem stock operations pilot in a lane not
currently covered by the legacy system. Field users capture stock operation
events on Datarun, sync them to the local/on-prem server, and supervisors
inspect scoped operational standing while the owner operates the environment
with explicit auth, data, support, backup/restore, and go/no-go gates.

This handoff does not approve real users, real stock records, production stock
truth, stock ledger correctness, production cutover, or controlled operational
use.

## first users/jobs

- Field stock worker: capture stocktake-line work in Datarun, including date,
  stock category, and quantity.
- Stock supervisor: inspect scoped stock operations standing and stocktake-line
  details for the work they are authorized to see.
- Deployment owner/operator: run the pilot locally/on-prem with explicit
  principal binding, backups, monitoring, secrets handling, and support path.
- Product/implementation owner: keep the first increment stocktake-line first,
  with receipt, issue, and disbursement only as later ordered backlog items.

## first usable increment

The first usable increment is a non-production stocktake-line pilot package
skeleton that turns the NW-148 test-only proof into reusable pilot
configuration/provisioning material. It keeps the event model flat:
`stocktake_line/v1` with stocktake date, stock category, and quantity, bound to
the `stock_operations` activity.

The increment is useful only when it can be provisioned locally, exercised with
synthetic or separately approved data, and inspected through scoped supervisor
visibility. It is not complete until mobile capture, local Keycloak/principal
binding, local operational preflight, and owner go/no-go gates are proven by
later backlog items.

### Architecture Assumptions

For this pilot handoff, `stocktake_line/v1` is deployer-authored shape
configuration and `stock_operations` is a deployer activity instance. They are
pilot configuration, not platform mechanisms, envelope changes, stock ledger
truth, workflow pattern definitions, or production approval.

Every Datarun event still requires the architecture-owned `subject_ref`
envelope field. The stock pilot mapping does not turn warehouse, item, catalog,
stocktake session, or ledger concepts into platform primitives.

For the current pilot proof, `stocktake_line/v1` events must reference a
provisioned pilot stock-scope subject through `event.subject_ref`. That subject
is the scoped operational anchor used by existing Datarun assignment,
location-path, sync, and scoped-report mechanics. In the first pilot mapping,
the intended real-world anchor is the physical stock-holding location or
storage point being counted, but this does not define a warehouse entity
lifecycle, inventory ledger, catalog/item model, stocktake session model,
process-ref subject, production stock truth, or new platform mechanism.

The package shape has `subject_binding = null`, so the subject reference must
come from capture, session, or operator context rather than from a
`stocktake_line/v1` payload field. If the current mobile/runtime path cannot
safely select or stamp that pilot stock-scope subject, the mobile stocktake
capture route must stop and identify the missing subject-selection surface
before mobile capture is accepted.

The first increment is flat capture plus scoped supervisor visibility.
Supervisor visibility means scoped read access; it does not select or require
`event.type = "review"`.

If later stock operations require human review, transfer acknowledgment,
multi-step approval, or discrepancy resolution, route a later slice explicitly
through the existing pattern contracts: `capture_with_review/v1`,
`transfer_with_acknowledgment/v1`, `multi_step_approval/v1`, or
`ongoing_resolution/v1`.

## Definition of Done

For each pilot slice, done means:

- user-visible behavior or executable runtime proof exists;
- focused tests pass;
- the full required gate for the touched surface passes;
- security/auth boundary is explicit;
- reliability/ops impact is explicit;
- no raw real data is used unless approved;
- status/backlog standing is updated;
- one next route is selected or the route is clearly parked.

## ordered Product Backlog

1. Stock operations pilot package skeleton. Create a reusable
   non-production/pilot config/provisioning package from the test-only
   stocktake proof. Accepted by NW-150.
2. Stock operations subject-anchor boundary and proof-oracle hardening. Make
   the `stocktake_line/v1` subject anchor explicit, keep it bounded to a
   provisioned pilot stock-scope subject, and harden the server proof so it
   does not rely on supervisor review authority. Accepted by NW-151.
3. Mobile stocktake capture smoke. Prove field-user mobile
   capture/offline/sync for `stocktake_line/v1`, or identify the exact missing
   mobile surface.
4. Supervisor stock operations view. Show stocktake line details, not only
   aggregate counts, using scoped authority.
5. Local Keycloak/principal-binding pilot path. Prove self-hosted Keycloak plus
   explicit Datarun principal bindings for pilot worker/supervisor users.
6. Local on-prem operational preflight. Prove backup/restore, monitoring,
   secrets, support path, and smoke evidence for the selected host.
7. Owner go/no-go for limited pilot. Select limited controlled use only after
   the evidence above exists.

## Security And Reliability Gates

- Real users/data, account import, submitted-record import/replay, production
  cutover, and controlled operational use remain blocked until the required
  owner approval route exists.
- Datarun authority remains explicit principal binding. IdP groups, claims,
  UI-selected actors, imported accounts, or request-authored actors do not
  grant Datarun authority.
- Pilot operation assumes local/on-prem hosting. Cross-border transfer, cloud
  hosting, managed external backup, external monitoring export, and remote
  support access to pilot data are not selected by default.
- Operational visibility must remain scoped before data display or aggregation.
  The pilot must not expand into broad reporting, import/export, queue/list
  review, batch automation, stock ledger correctness, or production stock truth.
- Reliability evidence must include the required focused test or runtime proof,
  the required full gate for any touched runtime surface, and explicit local
  backup/restore, monitoring, secrets, and support-path checks before go/no-go.
- Retention, offboarding, device purge, local encryption, tenant/control-plane,
  and new scope mechanisms remain separate routes unless explicitly selected.

## NW-148 Proof Evidence

PR #58 / NW-148 keeps
`SyntheticStockOperationsFirstFlowIntegrationTest` as valid runtime evidence.
The test proves a server-side synthetic `stocktake_line/v1` path through
config, auth/assignment, `/api/sync/push`, clean acceptance, and scoped
operational report visibility.

Specifically, the test publishes a flat `stocktake_line/v1` shape with
stocktake date, stock category, and quantity; publishes a `stock_operations`
activity; assigns a synthetic stock worker and scoped supervisor; fetches the
published config; submits two synthetic stocktake-line events through
authenticated `/api/sync/push`; verifies both events are accepted with zero
flags; and verifies `/web-admin/operational/report` shows the scoped `Stock
Operations` row with two clean source-work items.

This server-only test is evidence, not the whole pilot increment. It does not
prove mobile capture/offline UX, local Keycloak, on-prem operations,
backup/restore, support, real users/data, stock ledger correctness, production
stock truth, or production readiness.

## Next Implementation Route

Exactly one next implementation route is selected:

```text
NW-152 - Mobile stocktake capture smoke
```

NW-152 should prove mobile can select or stamp the provisioned pilot
stock-scope subject, capture a flat `stocktake_line/v1` item, retain it
offline or before sync, and push it through the existing authenticated sync
path. It must not decide a full stock domain model, reopen legacy migration,
repeatable sections, form importer, account import, real-data approval, review
workflow, stock ledger, or tenant/control-plane work.
