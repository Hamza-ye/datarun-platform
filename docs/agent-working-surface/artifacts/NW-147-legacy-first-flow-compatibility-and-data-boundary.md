# NW-147 - Legacy First-Flow Compatibility And Data-Boundary Matrix

Status: accepted planning/control artifact
Document type: product_planning / operational_policy_gap artifact
Authority: non-authoritative compatibility and data-boundary matrix; does not
approve production, real users/data, raw-data import, form importer work,
runtime implementation, contracts, schemas, sync/access behavior, BAR, CDL,
validation policy, gap-register standing, or production cutover
Date: 2026-06-23

## Decision

Selected first-flow candidate: **warehouse stocktake from
`supply_wh_mids_stocktake_901_form.json`**, limited to a flat, synthetic or
owner-approved redacted one-line stocktake rehearsal.

Selected next route: **synthetic/redacted first-flow rehearsal package** for
the selected warehouse stocktake slice.

This route is selected because the stocktake sample is the narrowest useful
stock/logistics candidate in the sanitized portfolio: it has one date field,
one stock category option-set field, one quantity field, no legacy string
rules, no `Error` validation rules, no household/case personal data, no
attachments, and no account data. Its main compatibility pressure is the
repeatable `invoiceDetails` section, which is outside the current flat Datarun
shape boundary. The first rehearsal must therefore prove only a bounded flat
projection of one stock line per event. It must not claim direct repeatable
section import or full form parity.

Real production remains blocked. Legacy remains the operational system of
record during any parallel run until an explicit owner go/no-go/cutover
decision is accepted. The owner premise remains local/on-prem with no
cross-border transfer, cloud hosting, managed external backup, external
monitoring export, or remote support access to pilot data selected by default.

## Inputs Read

- `docs/status.md`
- `docs/agent-working-surface/platform-next-work-backlog.md`
- `docs/agent-working-surface/artifacts/NW-093-real-production-approval-package.md`
- `docs/agent-working-surface/artifacts/NW-146-legacy-pilot-pressure-map-and-route.md`
- `docs/agent-working-surface/artifacts/NW-146-legacy-pilot-evidence/README.md`
- Sanitized form samples under
  `docs/agent-working-surface/artifacts/NW-146-legacy-pilot-evidence/`
- `docs/agent-working-surface/validation-matrix.md`
- `docs/agent-working-surface/decision-anchor-layer/gap-routing-playbook.md`
- `docs/operations/policies/first-reference-deployment-policy.md`
- `docs/specifications/platform/configuration-package-and-shapes.md`
- `docs/specifications/platform/expression-language.md`
- `contracts/shape-format.schema.json`

No raw submitted records were read or used. The user/account export was not
used except as the already-classified sensitive account/security-adjacent
pressure recorded by NW-093.

## Evidence Boundary

The NW-146 evidence packet contains 12 sanitized form-definition JSON files
with 190 fields, 40 sections, 15 repeatable sections, 61 rule actions, 7
validation-rule objects, bilingual labels, option-set references, and
form `uid`/`versionUid`/`versionNumber` metadata. It is evidence only. It is
not an import source, contract, runtime fixture, product specification, or
production approval record.

Current Datarun deployer-authored form shapes are flat. Nested objects,
repeating groups, and arrays of objects are outside the accepted shape format;
the only array-valued form field type is bounded `multi_select`. Accepted form
expressions are pure JSON AST rules, not legacy `#{field}` string expressions.

## Candidate Matrix

| Candidate | Compatibility fit | Data boundary and proof path | Parallel-run and stop boundary |
|---|---|---|---|
| **Warehouse stocktake (`supply_wh_mids_stocktake_901`) - selected** | Best flat fit if the rehearsal uses one stock line per Datarun event. Source form has two sections, one repeatable `invoiceDetails` section, and three fields: date, stock category `SelectOne`, and zero-or-positive quantity. No string rules, no `Error` rules, and one option set. Legacy `uid`/version metadata can be preserved as source metadata only. | Organization-confidential stock snapshot data only: stock category, quantity, date, selected warehouse/site label if owner-approved, and synthetic operator identity. Synthetic data is enough before real data; owner-approved redacted data may be used only after the rehearsal packet says exactly what is redacted. No household, case, account, submitted-record, attachment, or password data. | Legacy remains source of truth. Duplicate-entry risk is bounded to duplicate stock counts for the same date/category; rehearsal reconciliation compares Datarun stocktake lines to the legacy/source worksheet and stops on any mismatch. Rollback means stop using Datarun and continue in legacy; no database restore or cutover is implied. |
| Health-facility receipt/return/disbursement (`supply_hf_mids_receipt_902`, `supply_hf_mids_return_904`, `supply_mids_disbursement_invoice_903`) | Good field-type fit but broader operational movement than stocktake. Each uses a repeatable `invoiceDetails` section. Receipt has date, voucher number, category, quantity; return adds a reference number; disbursement adds voucher date, received-by text, batch/lot, expiry date, and description. No rules or `Error` objects. | Organization-confidential stock movement and voucher data. Synthetic proof is enough before real data, but redacted proof would need owner approval because voucher, batch, expiry, and recipient text may identify real operations. | Legacy remains source of truth. Duplicate-entry risk is higher than stocktake because a receipt/disbursement can be mistaken for operational movement. Needs owner-named movement owner and reconciliation procedure before real use. Not selected because stocktake proves the boundary with fewer workflow and cutover risks. |
| ITN issue/team receipt/reconciliation (`itns_issue_form_wh_keeper`, `itns_team_receipt`, `itns_reconcile_team_disbursed_itns`) | Useful stock/reconciliation family, but less clean as first flow. Issue/team receipt have repeatable ledger sections and two legacy `Show` rules. Reconciliation has a repeatable `dataReview` section, a legacy `Team` field, issued/distributed/remaining/difference quantities, and cleanliness/compliance option sets. No `Error` objects in these samples. | Organization-confidential stock and team reconciliation data. Synthetic proof is enough before real data. Team/orgUnit labels must stay source metadata only unless later mapped through accepted Datarun assignment/location/principal-binding primitives. | Legacy remains source of truth. Duplicate-entry and team accountability pressure are meaningful. Reconciliation owner/procedure is central, but this is broader than a first flat stocktake because it introduces team metadata and review vocabulary. Better as a follow-on after stocktake proof. |
| Malaria supply consumption (`chv-malaria_supply_consumption`) | Small sample with one repeatable movement section and four fields: month, comment, commodity select, and quantity. No rules or `Error` objects. It is close to the stock family but has program consumption/reporting semantics. | May remain organization-confidential if synthetic; redacted real values could expose health-program consumption patterns. Synthetic proof is enough before real data, but it is less neutral than stocktake because it leans toward health reporting. | Legacy remains source of truth. Duplicate-entry risk is consumption/reporting drift. Not selected because warehouse stocktake is more neutral and simpler. |
| ITN household distribution (`itns_distribution_household`) | Poor first-flow fit. It has a repeatable household section, `Progress`, `Reference`, `FullName`, many household population fields, 17 rule actions including 6 `Error` rules and 1 `Warning`, and status/lifecycle vocabulary. | Household data is personal or sensitive. Synthetic data is allowed for exploration, but real/redacted proof needs stricter owner approval and likely legal/site/compliance facts. | Legacy remains source of truth. Duplicate-entry risk affects household/campaign status. This would likely trigger entity lifecycle, retention/security, and broader reconciliation pressure. Rejected for first flow. |
| CHV case, supervision, and health-facility visit (`chv_cases_register`, `chv_supervision`, `health_facility_visit`) | Poor first-flow fit. Samples include case, supervision, facility, disease/reporting, many fields, repeatable sections, 40 total rule actions across the family, case validation, and broad reporting pressure. | Likely personal, sensitive, regulated, or organization-confidential depending on flow. Synthetic-only before later approval; real/redacted use needs production approval and compliance/security decisions. | Legacy remains source of truth. Duplicate-entry risk affects case/facility program records and supervision standing. These candidates would drag in reporting, review, entity lifecycle, and retention/security pressure. Rejected for first flow. |

## Selected Flow Boundary

### Form Structure Fit

The selected stocktake form has a simple header/detail shape:

- non-repeatable `invoice` section with `DateOnly`;
- repeatable `invoiceDetails` section with `wh_category` and `wh_quantity`.

Current Datarun cannot represent the repeatable section directly. The rehearsal
boundary is therefore:

- one Datarun capture event represents one stocktake line;
- the stocktake date is repeated on each line;
- stock category and quantity are flat fields;
- any legacy form `uid`, `versionUid`, `versionNumber`, section name, field
  name, option-set id, team, orgUnit, or form label remains source metadata only;
- no direct import of the repeatable legacy section is claimed.

If a later route requires one Datarun event to hold multiple stocktake lines as
nested objects or arrays, that is a compatibility blocker route, not part of
this selected rehearsal.

### Field Types, Option Sets, And Labels

Selected-field mapping for rehearsal planning:

| Legacy field | Legacy type | Datarun-compatible rehearsal type | Notes |
|---|---|---|---|
| `DateOnly` | `Date` | `date` | Flat-compatible. |
| `wh_category` | `SelectOne` with option set `eIkndo6hF4q` | `select` | Option values are deployment content for rehearsal only, not Datarun authority primitives. |
| `wh_quantity` | `IntegerZeroOrPositive` | `integer` with minimum 0 | Flat-compatible. |

Bilingual labels can be preserved as deployment/product copy for the rehearsal.
Labels must not become authority primitives, contract names, actor roles,
scope names, or database identifiers. The rehearsal should use product-safe
English labels and may retain Arabic labels as display copy if owner-approved.

### Expressions And Validation Rules

The selected stocktake sample has no legacy string expressions and no `Error`
validation objects. That is a major reason it is selected first.

The broader portfolio contains legacy `Show`, `Hide`, `Error`, and `Warning`
actions expressed as strings such as `#{field}` references. Those are not
accepted Datarun expression ASTs. The stocktake rehearsal must not build or
accept a string-expression importer. If later first-flow work needs those
rules, route a specific compatibility blocker before implementation.

### Metadata And Authority Boundary

Allowed as source metadata for the selected rehearsal:

- legacy form file name;
- legacy form name and labels;
- legacy `uid`, `versionUid`, and `versionNumber`;
- legacy section and field names;
- legacy option-set id;
- owner-approved source site/warehouse label.

Not allowed as Datarun authority primitives:

- legacy form ids as event type or envelope fields;
- legacy team/orgUnit labels as assignment, scope, actor, tenant, or principal
  authority;
- option values as platform vocabulary;
- labels as role/action authority;
- user/account export data as login seed data.

Any actual production authority must continue to use accepted Datarun
primitives: authenticated actor, active principal binding, assignment, location,
activity, role/action, and scoped sync/read behavior.

## Data Boundary

| Data class | Selected flow standing |
|---|---|
| Sanitized form definition | Allowed evidence. Not an importer input or contract. |
| Synthetic stocktake rows | Allowed for the next rehearsal route. Preferred proof data. |
| Owner-approved redacted stock rows | Allowed only if the rehearsal packet states the redaction method and owner approval. |
| Real stock records | Still blocked before production approval and owner go/no-go. |
| Account/user export | Blocked; classification only. No seed data, password migration, or login import. |
| Submitted legacy records | Blocked. No import, replay, or reporting export. |
| Household/case/facility records | Out of selected first flow. |
| Attachments | Out of selected first flow. |

Synthetic or owner-approved redacted proof is enough before real data because
the selected question is compatibility and operational rehearsal: can a flat
Datarun stocktake line be captured, reviewed/observed, reconciled against a
source worksheet, and stopped safely without changing source-of-truth status?

## Parallel-Run Control

Source of truth: **legacy system**.

The selected stocktake rehearsal may only be shadow/proof. During any later
real parallel run, Datarun stocktake entries must not update stock ownership,
issue stock, receive stock, correct legacy counts, or become operational truth
until an explicit go/no-go/cutover decision is accepted.

Duplicate-entry risk:

- same operator can enter the same date/category quantity in both systems;
- two Datarun entries can represent the same stocktake line;
- category labels can drift from source option values;
- late/offline sync can make a rehearsal view appear stale.

Reconciliation owner/procedure for the next rehearsal route:

- name one rehearsal reconciliation owner;
- prepare a synthetic or owner-approved redacted source worksheet with stock
  date, category, and quantity;
- capture matching flat Datarun stocktake-line events;
- compare by stock date plus category;
- classify every line as match, missing-in-Datarun, extra-in-Datarun, or
  value-mismatch;
- stop the rehearsal on any unexplained mismatch;
- record that legacy remains source of truth.

Rollback/stop criteria:

- stop immediately if raw real data, account export data, submitted records,
  household/case data, or attachments enter scope;
- stop if the flow requires direct repeatable-section import before a
  compatibility blocker route;
- stop if a Datarun value is treated as operational stock truth;
- stop if reconciliation ownership, mismatch handling, or source-of-truth
  language is ambiguous;
- stop if support access, monitoring export, backup export, remote access, or
  cross-border data movement is proposed without explicit owner approval;
- rollback means cease Datarun pilot use and continue in legacy. It does not
  imply production database restore, cutover reversal, or forward-fix authority.

## Reporting And Review Pressure

The selected stocktake rehearsal may include only narrow proof visibility:

- per-line captured value;
- simple reconciliation worksheet comparison;
- explicit caveat that the view is synthetic/redacted and not production truth.

Broad reporting, aggregate drilldown, import/export, warehouse/API/catalog,
automated reconciliation, queue/list/batch review, or production dashboards
remain unselected. Route NW-044 if broad reporting/import/export becomes the
selected work. Route NW-045 if reconciliation becomes queue/list/batch,
automation, resolver reassignment, or conflict automation.

## Trigger Review

| Route | NW-147 verdict |
|---|---|
| NW-044 reporting/import/export | Not selected. Manual synthetic/redacted worksheet reconciliation is enough for the next route. Trigger only if broad reporting, structured import/export, warehouse/API/catalog, or aggregate semantics become active. |
| NW-045 queue/batch/automation | Not selected. No queue, batch, auto-resolution, or workflow automation is needed for stocktake rehearsal. |
| NW-053 new scope | Not selected. Stocktake rehearsal can stay within existing organization/deployment, activity, actor, assignment/location concepts. Team/orgUnit labels remain source metadata only. |
| NW-054 retention/security/offboarding | Not selected for synthetic/redacted rehearsal. Real data, local encryption, erasure, redaction promises, or offboarding duties still route here if selected later. |
| NW-021 entity lifecycle | Not selected. Stock categories and stocktake lines are not accepted as maintained entity lifecycle truth. |
| NW-073 pattern extraction | Not selected. No pattern registry/projection behavior is needed. |
| NW-094 through NW-098 tenant/control-plane | Not selected. One local/on-prem first-organization pilot does not trigger managed SaaS control plane or tenant-aware runtime/storage/sync work. |

## Production Facts Still Needed

The selected matrix does not remove NW-093 production blockers. Before real
users/data, real stock records, controlled operational use, or cutover, an
accepted owner record still needs:

- legal organization, site, country/local boundary, jurisdiction, and
  accountable deployment owner;
- on-prem host/operator, DNS/TLS boundary, PostgreSQL boundary, backup
  location, monitoring/log destination, support access path, and confirmation
  that no cross-border transfer is selected by default;
- data/controller responsibility and local compliance/security review;
- allowed real data classes for the stocktake flow and prohibited data classes;
- self-hosted Keycloak operating owner, backup/restore and rotation posture,
  principal-binding provisioning path, and OIDC-provider portability;
- pilot support owner, escalation channel, support timezone, incident owner,
  and user communication path;
- pilot-specific continuity/preflight evidence for the chosen on-prem host;
- source-of-truth wording, duplicate-entry procedure, reconciliation owner,
  mismatch threshold, stop criteria, rollback path, and cutover authority;
- explicit owner go/no-go language.

## Selected Successor

Exactly one next route is selected:

**NW-148 - Synthetic/redacted warehouse stocktake first-flow rehearsal
package.**

The successor should create a proof package, not runtime code. It should define
the synthetic or owner-approved redacted stocktake data set, flat one-line
shape/activity mapping, source worksheet, rehearsal operator, reconciliation
owner, expected observations, mismatch stop criteria, rollback/stop wording,
and evidence format. It must preserve the local/on-prem and no cross-border
premise, keep legacy as source of truth, and stop before real users/data,
account import, submitted-record import/replay, production cutover, direct
repeatable-section import, contracts/schemas/sync changes, reporting/import/
export expansion, queue/batch/automation, new scope, entity lifecycle,
retention/security promises, pattern/projection, or tenant/control-plane work.

## Validation Category

Touched surfaces are docs-only/status/backlog/artifacts. Required validation is
`git diff --check` plus targeted NW-147/file/grep checks. Runtime tests are
skipped because NW-147 changes no runtime code, tests, contracts, schemas,
migrations, CI behavior, validation policy, product/platform behavior
acceptance, BAR, CDL, gap-register standing, mobile code, server code,
web-admin implementation, real-production approval, real users/data, or
form-import behavior.
