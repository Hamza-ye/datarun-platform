# NW-147 - Legacy First-Flow Compatibility And Data-Boundary Matrix

Status: accepted planning/control artifact
Document type: product_planning / operational_policy_gap artifact
Authority: non-authoritative compatibility and data-boundary matrix; does not
approve production, real users/data, raw-data import, form importer work,
runtime implementation, contracts, schemas, sync/access behavior, BAR, CDL,
validation policy, gap-register standing, or production cutover
Date: 2026-06-23

## Decision

Selected first-flow candidate: **stock operations starter slice using flat
Datarun events**, with the minimum event being a warehouse stocktake line from
`supply_wh_mids_stocktake_901_form.json`.

Optional only if already cheap within the same implementation slice:
receipt/issue line. Each legacy repeatable row maps to one Datarun event.

Selected next route: **NW-148 - Implement synthetic stock operations
first-flow vertical slice**.

This route is selected because the stocktake sample is the narrowest useful
stock/logistics candidate in the sanitized portfolio: it has one date field,
one stock category option-set field, one quantity field, no legacy string
rules, no `Error` validation rules, no household/case personal data, no
attachments, and no account data. Its main compatibility pressure is the
repeatable `invoiceDetails` section, but that does not require repeatable-
section platform support or a legacy form importer for the first slice. The
first executable proof should model every stocktake line as a flat Datarun
event. Multiple legacy rows become multiple Datarun events.

Do not claim full legacy form parity, importer support, stock ledger
correctness, or production readiness.

Real production remains blocked. Owner clarification: the first Datarun pilot
does not need to run as a shadow duplicate of an existing legacy flow. Legacy
can continue where it already runs, while Datarun runs in a new or
non-legacy-covered operational lane. Dual-entry reconciliation is therefore not
the default blocker. The owner premise remains local/on-prem with no
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

| Candidate | Compatibility fit | Data boundary and proof path | Operational-lane and stop boundary |
|---|---|---|---|
| **Warehouse stocktake (`supply_wh_mids_stocktake_901`) - selected** | Best flat fit if one stock line is one Datarun event. Source form has two sections, one repeatable `invoiceDetails` section, and three fields: date, stock category `SelectOne`, and zero-or-positive quantity. No string rules, no `Error` rules, and one option set. Legacy `uid`/version metadata can be preserved as source metadata only. | Organization-confidential stock snapshot data only: stock category, quantity, date, selected warehouse/site label if owner-approved, and synthetic operator identity. Synthetic data is enough for implementation/proof before real data. No household, case, account, submitted-record, attachment, or password data. | Datarun may run in a new/non-legacy-covered stock operations lane. Dual-entry reconciliation is not a default blocker. Stop if the slice claims stock ledger correctness, production stock truth, direct repeatable-section import, or full legacy form parity. |
| Health-facility receipt/return/disbursement (`supply_hf_mids_receipt_902`, `supply_hf_mids_return_904`, `supply_mids_disbursement_invoice_903`) | Good field-type fit but broader operational movement than stocktake. Each uses a repeatable `invoiceDetails` section. Receipt has date, voucher number, category, quantity; return adds a reference number; disbursement adds voucher date, received-by text, batch/lot, expiry date, and description. No rules or `Error` objects. | Organization-confidential stock movement and voucher data. Synthetic proof is enough before real data, but redacted proof would need owner approval because voucher, batch, expiry, and recipient text may identify real operations. | Optional only if already cheap in NW-148. Stop before stock ledger correctness, import/replay, or production movement authority. Not required for the minimum stocktake-line path. |
| ITN issue/team receipt/reconciliation (`itns_issue_form_wh_keeper`, `itns_team_receipt`, `itns_reconcile_team_disbursed_itns`) | Useful stock/reconciliation family, but less clean as first flow. Issue/team receipt have repeatable ledger sections and two legacy `Show` rules. Reconciliation has a repeatable `dataReview` section, a legacy `Team` field, issued/distributed/remaining/difference quantities, and cleanliness/compliance option sets. No `Error` objects in these samples. | Organization-confidential stock and team reconciliation data. Synthetic proof is enough before real data. Team/orgUnit labels must stay source metadata only unless later mapped through accepted Datarun assignment/location/principal-binding primitives. | Better as a follow-on after stocktake proof because it introduces team metadata and reconciliation vocabulary. Do not make reconciliation the default first-slice blocker. |
| Malaria supply consumption (`chv-malaria_supply_consumption`) | Small sample with one repeatable movement section and four fields: month, comment, commodity select, and quantity. No rules or `Error` objects. It is close to the stock family but has program consumption/reporting semantics. | May remain organization-confidential if synthetic; redacted real values could expose health-program consumption patterns. Synthetic proof is enough before real data, but it is less neutral than stocktake because it leans toward health reporting. | Not selected because warehouse stocktake is more neutral and simpler. |
| ITN household distribution (`itns_distribution_household`) | Poor first-flow fit. It has a repeatable household section, `Progress`, `Reference`, `FullName`, many household population fields, 17 rule actions including 6 `Error` rules and 1 `Warning`, and status/lifecycle vocabulary. | Household data is personal or sensitive. Synthetic data is allowed for exploration, but real/redacted proof needs stricter owner approval and likely legal/site/compliance facts. | This would likely trigger entity lifecycle, retention/security, and broader reconciliation pressure. Rejected for first flow. |
| CHV case, supervision, and health-facility visit (`chv_cases_register`, `chv_supervision`, `health_facility_visit`) | Poor first-flow fit. Samples include case, supervision, facility, disease/reporting, many fields, repeatable sections, 40 total rule actions across the family, case validation, and broad reporting pressure. | Likely personal, sensitive, regulated, or organization-confidential depending on flow. Synthetic-only before later approval; real/redacted use needs production approval and compliance/security decisions. | These candidates would drag in reporting, review, entity lifecycle, and retention/security pressure. Rejected for first flow. |

## Selected Flow Boundary

### Form Structure Fit

The selected stocktake form has a simple header/detail shape:

- non-repeatable `invoice` section with `DateOnly`;
- repeatable `invoiceDetails` section with `wh_category` and `wh_quantity`.

Current Datarun cannot represent the repeatable section directly, and NW-148
does not need it to. The implementation boundary is:

- one Datarun capture event represents one stocktake line;
- multiple legacy repeatable rows map to multiple Datarun events;
- the stocktake date is repeated on each line;
- stock category and quantity are flat fields;
- any legacy form `uid`, `versionUid`, `versionNumber`, section name, field
  name, option-set id, team, orgUnit, or form label remains source metadata only;
- no direct import of the repeatable legacy section is claimed.

If a later route requires one Datarun event to hold multiple stocktake lines as
nested objects or arrays, that is a compatibility blocker route, not part of
this selected first slice.

### Field Types, Option Sets, And Labels

Selected-field mapping for implementation/proof:

| Legacy field | Legacy type | Datarun-compatible rehearsal type | Notes |
|---|---|---|---|
| `DateOnly` | `Date` | `date` | Flat-compatible. |
| `wh_category` | `SelectOne` with option set `eIkndo6hF4q` | `select` | Option values are deployment content for the synthetic slice only, not Datarun authority primitives. |
| `wh_quantity` | `IntegerZeroOrPositive` | `integer` with minimum 0 | Flat-compatible. |

Bilingual labels can be preserved as deployment/product copy for the slice.
Labels must not become authority primitives, contract names, actor roles,
scope names, or database identifiers. The implementation should use product-safe
English labels and may retain Arabic labels as display copy if owner-approved.

### Expressions And Validation Rules

The selected stocktake sample has no legacy string expressions and no `Error`
validation objects. That is a major reason it is selected first.

The broader portfolio contains legacy `Show`, `Hide`, `Error`, and `Warning`
actions expressed as strings such as `#{field}` references. Those are not
accepted Datarun expression ASTs. NW-148 must not build or
accept a string-expression importer. If later first-flow work needs those
rules, route a specific compatibility blocker before implementation.

### Metadata And Authority Boundary

Allowed as source metadata for the selected implementation/proof:

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
| Synthetic stocktake rows | Allowed for NW-148. Preferred proof data. |
| Owner-approved redacted stock rows | Not needed for NW-148. Use only if a later owner-approved route states the redaction method and approval. |
| Real stock records | Still blocked before production approval and owner go/no-go. |
| Account/user export | Blocked; classification only. No seed data, password migration, or login import. |
| Submitted legacy records | Blocked. No import, replay, or reporting export. |
| Household/case/facility records | Out of selected first flow. |
| Attachments | Out of selected first flow. |

Synthetic proof is enough before real data because the selected question is
executable capability: can Datarun publish or test-provision a flat stocktake-
line shape/activity, accept a synthetic actor's stocktake-line work, and expose
that accepted event through existing scoped read/report surfaces?

## Operational Lane Control

The first Datarun pilot does not have to duplicate an existing legacy lane.
Legacy can keep running where it already runs. Datarun can run this synthetic
stock operations first-flow in a new or non-legacy-covered operational lane.

NW-148 is still synthetic and non-production. It may prove Datarun stocktake
capture and observation, but it must not claim real stock ledger correctness,
production stock truth, submitted-record migration, or cutover.

Dual-entry reconciliation is not a default blocker. It becomes relevant only if
an owner later chooses to run the same real stock operation in both systems or
compare Datarun output against an existing legacy worksheet.

Implementation/proof risks:

- two synthetic Datarun entries can represent the same stocktake line;
- category labels can drift from configured option values;
- late/offline sync can make the observed view stale;
- existing read/report surfaces may not expose the submitted synthetic event
  without additional scoped configuration or a narrow runtime fix.

Rollback/stop criteria:

- stop immediately if raw real data, account export data, submitted records,
  household/case data, or attachments enter scope;
- stop if the flow requires direct repeatable-section import before a
  compatibility blocker route;
- stop if a Datarun value is treated as production stock truth;
- stop if support access, monitoring export, backup export, remote access, or
  cross-border data movement is proposed without explicit owner approval;
- rollback means stop the synthetic Datarun proof path. It does not imply
  production database restore, cutover reversal, or forward-fix authority.

## Reporting And Review Pressure

The selected stock operations vertical slice may include only narrow proof
visibility:

- per-line captured value;
- scoped operational view or report observation using existing surfaces;
- explicit caveat that the view is synthetic and not production truth.

Broad reporting, aggregate drilldown, import/export, warehouse/API/catalog,
automated reconciliation, queue/list/batch review, or production dashboards
remain unselected. Route NW-044 if broad reporting/import/export becomes the
selected work. Route NW-045 if review/reconciliation becomes queue/list/batch,
automation, resolver reassignment, or conflict automation.

## Trigger Review

| Route | NW-147 verdict |
|---|---|
| NW-044 reporting/import/export | Not selected. Existing scoped read/report surfaces should be used if they are already enough for NW-148. Trigger only if broad reporting, structured import/export, warehouse/API/catalog, or aggregate semantics become active. |
| NW-045 queue/batch/automation | Not selected. No queue, batch, auto-resolution, or workflow automation is needed for the stock operations starter slice. |
| NW-053 new scope | Not selected. Stocktake rehearsal can stay within existing organization/deployment, activity, actor, assignment/location concepts. Team/orgUnit labels remain source metadata only. |
| NW-054 retention/security/offboarding | Not selected for synthetic implementation/proof. Real data, local encryption, erasure, redaction promises, or offboarding duties still route here if selected later. |
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
- lane ownership, duplicate-entry procedure if the selected real lane overlaps
  legacy, reconciliation owner only if comparison is selected, mismatch
  threshold if comparison is selected, stop criteria, rollback path, and cutover
  authority if cutover is selected;
- explicit owner go/no-go language.

## Selected Successor

Exactly one next route is selected:

**NW-148 - Implement synthetic stock operations first-flow vertical slice.**

The successor must implement or configure the smallest executable stock
operations vertical slice using existing Datarun capabilities. Minimum event:
warehouse stocktake line. Optional only if already cheap within the same slice:
receipt/issue line. Each legacy repeatable row maps to one Datarun event.

NW-148 must use synthetic data only, define a flat stocktake-line
shape/activity with existing shape format, use existing auth/assignment/config/
sync/read surfaces, and prove one end-to-end path: config exists or is test-
provisioned, a synthetic actor submits stocktake-line work, the event is
accepted, a scoped supervisor/operational view or report can observe it, and
validation evidence is recorded.

If no runtime code is needed, NW-148 should run a live/integration proof and
record it. If the existing runtime cannot execute the path, NW-148 must stop
and identify the exact missing implementation surface. It must not create
another planning artifact.

NW-148 must stop before real users/data, account import, submitted-record
import/replay, production cutover, direct repeatable-section support, legacy
form importer work, contracts/schemas/sync changes, broad reporting/import/
export expansion, queue/batch/automation, new scope, entity lifecycle,
retention/security promises, pattern/projection, tenant/control-plane work,
full legacy form parity, stock ledger correctness, or production readiness.

## Validation Category

Touched surfaces are docs-only/status/backlog/artifacts. Required validation is
`git diff --check` plus targeted NW-147/file/grep checks. Runtime tests are
skipped because NW-147 changes no runtime code, tests, contracts, schemas,
migrations, CI behavior, validation policy, product/platform behavior
acceptance, BAR, CDL, gap-register standing, mobile code, server code,
web-admin implementation, real-production approval, real users/data, or
form-import behavior.
