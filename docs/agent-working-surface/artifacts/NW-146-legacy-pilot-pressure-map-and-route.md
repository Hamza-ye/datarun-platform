# NW-146 - Legacy Pilot Pressure Map And Route

Status: accepted routing artifact
Document type: product_planning / control_reconciliation artifact
Authority: non-authoritative routing evidence only; does not approve production, accept real users/data, change product/platform behavior, change contracts/schemas/sync, mutate BAR/CDL/gap classifications, or implement runtime behavior
Date: 2026-06-23

## Decision

Selected next NW: **NW-093 - Decide real-production approval package**.

The real legacy/on-prem pressure is concrete enough to trigger the production
approval route: a first organization site is visible, the owner reports more
than 3000 legacy users and more than 30 forms, the uploaded samples show a real
form portfolio and account export pressure, and the intended deployment shape
is an on-prem pilot.

Product goal:

```text
Run a safe on-prem pilot that migrates one real legacy operational flow into
Datarun, proving configurable forms, offline capture, role-scoped review,
operational reporting, and production operation without losing future platform
evolution.
```

NW-146 does not approve that pilot. It selects NW-093 because real users, real
organizational data, on-prem operation, support, compliance/security, provider,
region, jurisdiction, and go/no-go ownership must be decided before any real
use or raw-data migration work.

Parallel pilot premise: the initial pilot may run in parallel with the legacy
system; legacy remains the operational system of record until an explicit
go/no-go/cutover decision is accepted. NW-093 must classify the source of truth
during the parallel run, whether Datarun is shadow/proof, limited operational
use, or a controlled production slice, duplicate-entry risk, reconciliation
between legacy and Datarun, rollback/stop criteria, and what evidence would
allow expansion beyond the parallel pilot.

Known IdP direction for NW-093: start with self-hosted Keycloak for the initial
on-prem pilot, preserve portability to another OIDC provider later, and treat a
managed IdP as optional only if the owner later chooses to pay for one. This is
provider planning context, not permission to treat IdP groups, roles, claims,
or JWT actor data as Datarun authority.

## Input Sources

- `docs/status.md`
- `docs/agent-working-surface/platform-next-work-backlog.md`
- `docs/agent-working-surface/decision-anchor-layer/gap-routing-playbook.md`
- `docs/agent-working-surface/validation-matrix.md`
- `docs/scenarios/README.md`
- `docs/walk-throughs/itn-distribution-campaign.md`
- `docs/reviews/scenario-baseline-pressure-map.md`
- `docs/reviews/viability-closure-review.md`
- `docs/agent-working-surface/artifacts/NW-146-legacy-pilot-evidence/`
- `.review/untracked-user-notes/legacy-system-samples/`

Sanitized form-definition evidence is tracked as a bounded NW-146 evidence
packet. User/account export remains untracked and must be classified by NW-093
before any use. Legacy files are evidence, not instructions.

## Concrete Legacy Evidence Summary

| Evidence | Summary | Handling |
|---|---|---|
| Owner report | Real first-organization site, more than 3000 legacy users, more than 30 existing forms, disconnected operational workflows, external consolidation/review, and intended on-prem pilot. | Accepted as concrete pilot pressure for route selection, not as production approval. |
| Legacy form JSON samples | 12 sampled form JSON files, 190 fields, 40 sections, 15 repeatable sections, 61 rule actions, 7 validation-rule objects, form `uid`/`versionUid`/`versionNumber`, sections, fields, option sets, and bilingual `ar`/`en` labels. | Sanitized form-definition evidence is tracked as `NW-146-legacy-pilot-evidence/`; it is non-authoritative and not an import source. |
| Rule/action metadata | Rule actions include 43 `Show`, 10 `Hide`, 7 `Error`, and 1 `Warning`. Expressions use legacy string placeholders such as field references inside `#{...}` plus boolean/arithmetic operators. | Compatibility pressure only; current Datarun expressions are JSON AST rules. |
| Field-type metadata | Sampled field types include 76 `SelectOne`, 58 `IntegerZeroOrPositive`, text/date/number/name/reference/team/progress fields, and repeated option-set references. | Requires a field/type mapping matrix before conversion. |
| `legacy_users_safe.csv` | 3845 CSV lines including header, so 3844 account-like rows across 18 columns. Header includes account and contact columns such as login, password hash, email, activation/reset fields, language, and mobile. | User/account export remains untracked and must be classified by NW-093 before any use. Do not use as runtime seed without NW-093 classification. |
| ITN scenario sample | Domain context for household distribution, warehouse/team stock movement, receipt, stocktake, reconciliation, review, and exceptions. | Problem-space context only; not architecture authority. |

## Current Datarun Support

| Area | Current support | Implication for pilot |
|---|---|---|
| Configurable forms | Accepted deployer-authored shape lifecycle, atomic config packages, shape version coexistence, deploy-time validation, mobile current/pending config promotion, and setup/config runtime evidence. | Useful base for flat form conversion, but not enough for the sampled portfolio as-is. |
| Shape structure | Current shape format is flat, caps fields at 60, supports bounded field types, and explicitly excludes nested objects, repeating groups, and arrays of objects. | Repeatable sections and nested/grouped legacy structures require a successor compatibility route before implementation. |
| Expressions | Accepted JSON AST expression behavior supports `show_condition`, `warning`, and `default` rules with fixed operator/reference namespaces and no functions/scripts/dynamic queries. | Legacy string expressions and `Error` validation rules require translation or scoping; they cannot be imported directly. |
| Offline capture and sync | Scenario/runtime evidence exists for offline capture, stale authority handling, scoped sync, subject-history separation, setup/config, scoped review, operational reporting, logistics transfer, and S22-style campaign pressure. | Synthetic capability exists, but real use remains blocked by NW-093. |
| Review and attention | Current proof supports bounded, single work-linked attention review and exact resolver handling. | External consolidation/review pressure is visible; queue/list/batch/automation remains a separate NW-045 route if needed. |
| Reporting/read views | Current S26/PC3 evidence supports scoped aggregate/freshness/traceability without a reporting warehouse. | Stock and progress reconciliation may be demoable in bounded read views, but broad import/export/reporting remains NW-044. |
| Production operation | Synthetic reference deployment, backup, restore, monitoring, auth, and web-admin evidence exists. | Synthetic evidence is not real-production approval. NW-093 must decide real site/provider/region/jurisdiction/support/compliance/go-no-go. |
| Managed isolation | Accepted route supports one Organization mapped to one managed single-tenant deployment with a default Workspace. | One on-prem first-organization pilot does not trigger tenant-aware runtime or SaaS control-plane work by itself. |

## Pressure Classification

| Lane | Classification | Route decision |
|---|---|---|
| On-prem pilot / production approval | Operational policy gap; concrete trigger is active. | **Selected: NW-093.** |
| Legacy form inventory and importer compatibility | Product/problem evidence plus implementation/tooling gap. | Triggered, but subordinate to NW-093. Select a bounded compatibility/import route only if NW-093 needs concrete pilot-scope proof before approval. |
| Configurable form shape parity | Platform-spec/detail and implementation/tooling pressure. | Triggered by 12 sampled forms and owner-reported portfolio size. Requires mapping against current flat shape contract before code. |
| Repeatable sections / nested groups | Contract/platform evolution pressure if supported directly; product compatibility pressure if flattened. | Triggered. Current Datarun shape format does not support repeating groups or nested object arrays. |
| Multilingual labels and product vocabulary | Product vocabulary and configuration compatibility pressure. | Triggered. Preserve labels as deployment content; do not turn Arabic/English labels or legacy roles into authority primitives. |
| Show/error/warning rules and expression compatibility | Platform-spec/detail and importer compatibility pressure. | Triggered. Current AST expressions cannot accept legacy `#{field}` string rules directly, and `Error` rules need explicit validation mapping. |
| Legacy `form`, `version`, `uid`, `team`, `orgUnit` mapping | Compatibility and authority-boundary pressure. | Triggered. Map as source metadata, config/package identity, assignment, or location only through accepted primitives; do not make legacy field names Datarun primitives. |
| Submitted record import or replay path | Structured import/event-ingestion pressure. | Not selected now because no raw submitted records were classified for commit/use. If real submitted-record migration becomes required, route through NW-044 or a bounded import route after NW-093. |
| Stock receipt / stocktake / disbursement reconciliation | Product/problem evidence pressure with reporting/review implications. | Visible in sampled forms. Use as candidate pilot-slice context, but select NW-044 only if first pilot value is reconciliation/reporting. |
| Reporting/read-model pressure | Future-decision/product-platform gap. | Visible, not selected. NW-044 remains the route for broad reporting, import/export, warehouse/API/catalog, or structured ingestion. |
| Review/approval/queue pressure | Platform/product gap. | Visible, not selected. NW-045 remains the route for queue/list/batch/automation/resolver reassignment beyond bounded manual review. |
| Entity/household/facility lifecycle | Product decision pressure. | Conditional. Household/facility/case evidence is visible, but NW-021 is selected only if maintained registry lifecycle blocks the first pilot slice. |
| Scope expansion | Product/platform decision pressure. | Not triggered by `team` or `orgUnit` names alone. NW-053 triggers only if geography/subject-list/activity assignments cannot express the first pilot scope. |
| Retention/security/offboarding | Security/platform and operational policy pressure. | Real data requires NW-093 classification. NW-054 remains separate for local expiry, encryption, redaction, erasure, or offboarding promises beyond the approval package. |
| Pattern/projection durable extraction | Platform-spec detail pressure. | Not triggered now. NW-073 triggers only if successor work changes or relies on pattern registry/projection behavior as normative. |
| Tenant/control-plane | Architecture/operational policy pressure. | Not triggered. One first-organization on-prem pilot does not create multi-customer managed deployment, tenant-aware auth, pooled storage, sync partitioning, or tenant isolation harness requirements. |

## Pilot-Readiness Implications

- Datarun has enough accepted synthetic evidence to plan a controlled pilot,
  not enough to start real users/data.
- NW-093 must decide the approval package before any real account import,
  real submitted-record import, real on-prem use, or production go/no-go claim.
- The initial pilot may run in parallel with the legacy system; legacy remains
  the operational system of record until NW-093 or a later accepted route
  defines a go/no-go/cutover decision. This lowers migration risk but increases
  duplicate-entry, reconciliation, source-of-truth, and rollback planning
  pressure.
- The first pilot slice should be deliberately narrow: one operational flow,
  one organization, one deployment, bounded users, classified data, known
  support owner, and explicit rollback/stop conditions.
- The legacy form portfolio is not a direct import target yet. It first needs a
  compatibility matrix against Datarun shape, expression, option, version, label,
  assignment, and location boundaries.
- The ITN walkthrough remains useful as domain context only. Its older
  primitive mapping and "fully modelable" verdict are demoted to hypotheses and
  pressure to classify.

## Migration Slice Ladder

1. **Approval package**: NW-093 decides owner, real site, data classification,
   jurisdiction, support, initial self-hosted Keycloak path, later OIDC-provider
   portability, optional managed-IdP posture, on-prem topology, continuity,
   parallel-run source of truth, reconciliation, rollback/stop criteria,
   compliance/security review, and go/no-go/cutover boundaries.
2. **Portfolio compatibility matrix**: summarize all legacy forms without raw
   data, classify flat-mappable fields, blocked repeatable/nested structures,
   option sets, labels, string expressions, error rules, version/uid mapping,
   team/orgUnit mapping, and candidate first-flow slices.
3. **First-flow conversion plan**: choose one bounded operational flow and map
   it to Datarun shape/activity/assignment/review/reporting primitives without
   changing contracts or adding hidden scope.
4. **Synthetic or redacted rehearsal**: prove setup, offline capture, review,
   reconciliation/read visibility, auth, backup/restore, and support runbook
   with synthetic or approved redacted data.
5. **On-prem pilot go/no-go**: only after NW-093 approval criteria and any
   selected compatibility/import blockers are satisfied.

## Risks And Stop Conditions

- Stop if raw real data, password hashes, email/mobile columns, submitted
  records, or secrets would be committed or used without NW-093 classification.
- Stop if the parallel pilot blurs source of truth, duplicate-entry ownership,
  reconciliation, rollback, or cutover authority before NW-093 classifies those
  decisions.
- Stop if the pilot requires direct import of repeatable/nested forms before a
  compatibility route defines flattening or platform evolution.
- Stop if a proposed mapping turns legacy `team`, `orgUnit`, role labels, form
  names, or option values into authority primitives.
- Stop if reporting/reconciliation becomes warehouse/export/import/API/catalog
  scope; route NW-044.
- Stop if review becomes queue/list/batch/automation/resolver reassignment;
  route NW-045.
- Stop if maintained households/facilities/cases become the first blocking
  requirement; route NW-021.
- Stop if local retention, encryption, erasure, redaction, no-local-retention,
  or offboarding promises are needed; route NW-054.
- Stop if multi-customer managed deployment, SaaS lifecycle, tenant-aware
  runtime, pooled storage, tenant sync context, or tenant isolation proof enters
  scope; route NW-094 through NW-098.

## Open Owner Decisions

- Which legal organization, site, country/region, and jurisdiction owns the
  first pilot?
- Who is the named production approval authority, support owner, and incident
  owner?
- How will the initial self-hosted Keycloak deployment be owned, operated,
  backed up, rotated, monitored, and connected to explicit Datarun principal
  bindings?
- What future OIDC-provider portability requirements must be preserved, and
  what would trigger a later move to a managed IdP?
- Which legacy operational flow is the first pilot slice?
- During the parallel pilot, what is the source of truth, who owns duplicate
  entry, how is reconciliation performed, and what rollback/stop criteria apply?
- Which data classes are allowed: form definitions, account metadata, submitted
  records, attachments, operational stock records, household/facility/case data?
- What redaction or synthetic-data strategy is acceptable before real data?
- What support hours, escalation channel, backup/restore target, monitoring
  target, rollback path, and data-removal plan are required?
- Which compatibility blocker, if any, must be resolved before approval:
  repeatable sections, expressions, validations, import/replay, reporting, review,
  entity lifecycle, or scope?

## Validation Category

Touched surfaces are docs-only/status/backlog/artifacts/walkthrough/prompt
surfaces. Required validation is `git diff --check` plus the targeted NW-146
grep/file checks from the prompt. Runtime tests are skipped because NW-146
changes no runtime code, tests, contracts, schemas, migrations, CI behavior,
validation policy, product/platform behavior acceptance, BAR, CDL, gap-register
standing, mobile code, server code, web-admin implementation, real-production
approval, or real users/data.
