# NW-145 Post-PC2 Live Proof Standing And Next Route

Status: non-authoritative product-validation / control-reconciliation artifact

Date: 2026-06-23

## Classification

`READY`

## PC2 live proof standing

NW-144 accepted the PC2 replacement live browser proof as `PASS`.

Standing after NW-144:

- The retained isolated PC2 stack stayed on source revision
  `75880aafd346d06d4439b037b29d0d193a02f7ec`.
- Compose project remained `datarun-pc2-nw125`.
- App port remained `127.0.0.1:28080`.
- Synthetic OIDC provider remained `172.17.0.1:28090`.
- Synthetic actor remained
  `33333333-3333-4333-8333-333333333333`.
- Browser proof used the required two-port tunnel.
- Synthetic OIDC login/session worked.
- Authenticated `/web-admin/operational` showed one synthetic-actor
  `Needs review` cue before decision.
- `/web-admin/operational/attention` opened and showed exact designated
  reviewer standing.
- The real attention-review form submitted exactly one owner-approved
  `Accept` decision.
- Accepted resolution event `cf0e6b52-06cb-4134-a548-6f1c29cc6956` cleared
  replacement flag `14414414-4144-4144-9144-144144144144`.
- Authenticated operational after-state showed zero synthetic-actor
  `Needs review` cues.

No blocker remains for PC2 Single Work-Linked Attention Review proof.

## R12 continuity before and after

NW-144 inspected R12 read-only before and after PC2 work. R12 remained
unchanged:

- R12 app container stayed healthy on image digest
  `sha256:5f246547e1292092b133540a86efe230fd9f9bacc1f73bccc83e8644e4fb82e2`.
- R12 source revision stayed
  `757d6c8d386f760693157c3e1388c877efdf6a0e`.
- R12 version stayed `nw067-candidate`.
- R12 readiness stayed HTTP 200 with `{"status":"UP"}`.
- R12 `/api/auth/me` without token stayed HTTP 401.
- R12 `/web-admin/operational` stayed HTTP 404.
- R12 evidence root and runtime config remained present.
- DB1 and ops evidence roots remained present.
- R12 Keycloak and Alertmanager remained up.

NW-145 did not re-inspect or mutate R12.

## Residual Isolated-PC2 Lab State

Residual PC2 lab state after NW-144:

- Replacement source `14414414-3144-4144-9144-144144144144`, replacement flag
  `14414414-4144-4144-9144-144144144144`, and accepted resolution
  `cf0e6b52-06cb-4134-a548-6f1c29cc6956` remain in the isolated PC2 DB.
- One unrelated unassigned detector flag remains:
  `3ebd24d2-24a6-3732-904d-247ba4f9fad4`.
- That flag is category `concurrent_state_change`, source event
  `14114114-3141-4141-9141-141141141141`, designated resolver
  `system:resolver_unassigned/concurrent_state_change`.
- The flag is against old consumed NW-141 source work, not the NW-144
  replacement source.
- It did not appear as a synthetic-actor `/web-admin/operational`
  `Needs review` cue.

Classification:

`parked lab residue`

Rationale: the residual flag is isolated PC2 lab state created during proof
fixture history. It is not a current PC2 Single Work-Linked Attention Review
blocker and is not safely clearable inside the synthetic actor route because
its designated resolver is a `resolver_unassigned` sentinel. No cleanup route
is selected. Future resolver-unassigned, review-queue, reassignment, or
automation pressure remains routed through existing conflict routes such as
NW-045 and the accepted NW-072 boundary if it becomes product pressure.

## Attached Legacy Pilot Task Routing

Hamza provided a proposed legacy-system/on-prem pilot pressure-map prompt and
explicitly said it is not for immediate execution. The proposal is cleanly
routable, but its ID must not reuse `NW-143` because NW-143 is already accepted
for consumed PC2 fixture reconciliation.

Lane selection:

- Selected lane: `PM Product Planner`.
- Secondary control concern: use the lane selector and gap-routing inputs
  inside the prompt; stop before architecture/spec/runtime changes.
- Why: the work is a product/control-plane intake and route-selection task for
  a real on-prem pilot pressure, not implementation.

Known untracked evidence location:

`.review/untracked-user-notes/legacy-system-samples/`

Filenames observed without reading or committing raw contents:

- `chv-malaria_supply_consumption_form.json`
- `chv_cases_register_form.json`
- `chv_supervision_form.json`
- `health_facility_visit_form.json`
- `itns-distribution-example-health-sector.md`
- `itns_distribution_household_form.json`
- `itns_issue_form_wh_keeper_form.json`
- `itns_reconcile_team_disbursed_itns_form.json`
- `itns_team_receipt_form.json`
- `legacy_users_safe.csv`
- `supply_hf_mids_receipt_902_form.json`
- `supply_hf_mids_return_904_form.json`
- `supply_mids_disbursement_invoice_903_form.json`
- `supply_wh_mids_stocktake_901_form.json`

Clean routing correction:

- Use `NW-146`, not `NW-143`.
- Keep the proposed task as a bounded pressure-map and route-selection NW.
- Do not execute import, implementation, schema, contract, runtime, or
  production-approval work inside that route.
- Do not commit raw real production data unless classification explicitly finds
  a file safe. Prefer summarized or redacted evidence.

## Selected Next Route

Exactly one next route is selected:

`NW-146 - Legacy pilot intake, form portfolio pressure map, and on-prem route selection`

## Validation

- `git diff --check` passed.
- `rg "NW-145" docs/status.md docs/agent-working-surface/platform-next-work-backlog.md` passed.
- `test -f docs/agent-working-surface/artifacts/NW-145-post-pc2-live-proof-standing-and-next-route.md` passed.
- `grep -n "PC2 live proof standing" docs/agent-working-surface/artifacts/NW-145-post-pc2-live-proof-standing-and-next-route.md` passed.
- Runtime automated tests skipped because NW-145 changed no runtime code,
  tests, contracts, schemas, migrations, CI behavior, validation policy,
  product or platform specs, BAR, CDL, gap register, mobile code, or
  server/web-admin implementation.
