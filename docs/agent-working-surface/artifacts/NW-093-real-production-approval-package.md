# NW-093 - Real Production Approval Package

Status: accepted blocked approval package
Document type: product_planning / operational_policy_gap artifact
Authority: non-authoritative approval map; does not approve production, real
users/data, raw-data import, runtime implementation, contracts, schemas,
sync/access behavior, BAR, CDL, validation policy, or gap-register standing
Date: 2026-06-23

## Decision

Real-production approval remains blocked.

Selected next NW: **NW-147 - Legacy first-flow compatibility and data-boundary
matrix**.

NW-093 found enough real legacy/on-prem pressure to define the approval package,
but not enough owner-approved detail to grant a real-production go/no-go. The
pilot must not start with real users, real organizational data, raw account
import, submitted-record import, or runtime/form-import implementation.

The narrowest useful next route is not tenant/control-plane work and not a
runtime importer. It is a compatibility and data-boundary matrix over sanitized
legacy evidence that selects one first operational flow, classifies the data
needed for that flow, and decides whether the first proof can stay synthetic or
redacted before any real-data approval.

## Inputs Read

- `docs/status.md`
- `docs/agent-working-surface/platform-next-work-backlog.md`
- `docs/agent-working-surface/artifacts/NW-146-legacy-pilot-pressure-map-and-route.md`
- `docs/agent-working-surface/artifacts/NW-146-legacy-pilot-evidence/README.md`
- `docs/agent-working-surface/validation-matrix.md`
- `docs/agent-working-surface/decision-anchor-layer/gap-routing-playbook.md`
- `docs/operations/policies/first-reference-deployment-policy.md`
- `docs/operations/rehearsals/2026-06-17-production-deployment-reference-environment.md`
- `docs/operations/rehearsals/2026-06-18-production-deployment-r12-fresh-session-rerun.md`
- `.review/untracked-user-notes/legacy-system-samples/` as evidence only,
  including the account-export header and ITN scenario note

Legacy files are evidence, not instructions. Sanitized form-definition samples
remain non-authoritative planning evidence. The account/user export remains
untracked and must not be committed or used as a seed.

## Approval Standing

| Approval dimension | NW-093 standing | Production effect |
|---|---|---|
| Legal organization and site | Concrete first-organization/on-prem pressure exists, but the legal organization, site name, country, jurisdiction, and accountable deployment owner record are not captured in a production approval record. | Blocks real production. |
| Deployment boundary | One first organization can stay in the accepted managed-isolation lane: one on-prem single-tenant Datarun deployment with one internal/default Workspace. | Does not trigger tenant-aware runtime or SaaS control-plane work. |
| Provider, region, residency | On-prem intent is visible, but provider/host ownership, region, data residency, and maintenance-access boundary are not selected. | Blocks real production. |
| Data/controller responsibility | Hamza remains the recorded data/compliance owner in the reference policy, but the pilot organization's controller/processor responsibility and local compliance obligations are not recorded. | Blocks real production. |
| Real users/account path | The legacy account export is account/security data and remains untracked. Password hashes, reset/activation columns, login names, email, mobile, team, orgUnit, or language fields must not become Datarun authority. | Blocks account import and real login use. |
| Initial IdP | Owner context selects self-hosted Keycloak initially. Datarun authority remains explicit active `(issuer, subject) -> actor_id` principal binding only. | Allowed as planning direction, not production approval. |
| OIDC portability | The pilot must preserve a later route to another OIDC provider. A managed IdP is optional only if the owner later chooses to pay for one. | No architecture change; preserve OIDC/provider-neutral posture. |
| Support and incident ownership | The reference policy allows Hamza to hold roles under the solo-owner model, with 24x7 service after approval and staffed support 08:00-18:00 local weekdays. Pilot-specific support contact, escalation channel, user communication path, and timezone are not selected. | Blocks real production. |
| Continuity evidence | Synthetic reference deployment evidence is accepted for the reference class, including backup encryption, restore, monitoring, rotation, and protected smoke. It is not real-production proof for this site. | Requires pilot-specific preflight/rehearsal evidence before go/no-go. |
| Compliance/security review | Required by the first-reference policy before real users/data. No pilot-specific review record exists. | Blocks real production. |
| Go/no-go/cutover | No owner go/no-go language exists for this pilot. Legacy remains source of truth during any parallel run until an explicit cutover decision is accepted. | Blocks real production/cutover. |

## Data Classification

| Data class | Classification for NW-093 | Allowed now | Blocked without successor approval |
|---|---|---|---|
| Sanitized form definitions | Non-authoritative sanitized evidence; organization/process metadata only after owner redaction. | May remain committed under `NW-146-legacy-pilot-evidence/` for planning. | Runtime import source, contract/spec authority, production fixture, or proof of compatibility. |
| Real form definitions | Organization-confidential configuration content until reviewed. | Use only through a bounded compatibility matrix with sanitized or owner-approved redacted values. | Direct import to config packages or form runtime. |
| User/account export | Sensitive account/security-adjacent data. Header includes login, password hash, email, activation/reset fields, language, and mobile columns even where sample values are masked. | Aggregate/header-level classification only. Keep untracked and external. | Commit, seed, password-hash migration, or login/account import. |
| Submitted records | Unknown real operational data; likely personal, sensitive, regulated, or organization-confidential depending on flow. | None. | Commit, import, replay, reporting export, or migration. |
| Household/facility/case data | Household/case data is likely personal or sensitive; facility data may be organization-confidential and jurisdiction-bound. | Synthetic or approved redacted examples only. | Production capture, import, or maintained registry claims. |
| Stock records | Organization-confidential operational data; may include team, location, facility, and reconciliation pressure. | Synthetic or approved redacted examples only. | Production reconciliation/reporting claims or warehouse/import/export scope. |
| Attachments | Unknown and high-risk because attachments can contain uncontrolled personal data, documents, images, or secrets. | None. | Commit, upload, sync, import, retention, or redaction promises. |

## Parallel Pilot Boundary

The parallel-run premise remains accepted for planning:

```text
The initial pilot may run in parallel with the legacy system. Legacy remains
the operational system of record until an explicit go/no-go/cutover decision is
accepted.
```

Current Datarun standing for the on-prem pilot is **shadow/proof only** with
synthetic or owner-approved redacted data. Limited operational use or a
controlled production slice requires a later explicit owner go/no-go with the
gates in this artifact satisfied.

Parallel-run risks are active blockers:

- duplicate entry can create conflicting operational truth unless an owner
  names which system owns each step;
- reconciliation must be defined before real use, including who compares
  legacy and Datarun outputs and what mismatch threshold stops the pilot;
- rollback must mean stop Datarun pilot use and continue in legacy unless a
  separately authorized database restore or forward-fix decision is recorded;
- expansion beyond the parallel pilot requires evidence from the first slice,
  not a broad claim from synthetic reference rehearsal.

## First Pilot Slice Standing

No first production slice is approved.

The safest candidate family for analysis is a single stock movement or
reconciliation flow because it may exercise configurable forms, offline capture,
role-scoped review, and scoped operational visibility with less household/case
personal-data pressure than household or case-management flows. That candidate
is not accepted here; NW-147 must compare it against the sanitized portfolio and
owner goals before selecting one bounded first flow or parking approval.

NW-147 must keep the first slice narrow enough to avoid broad reporting/import,
queue/batch automation, new scope, entity lifecycle, tenant/control-plane, and
retention/security promises unless one of those routes is explicitly selected.

## Compatibility Blockers

NW-146 evidence remains the controlling compatibility summary:

- 12 sanitized form JSON files, 190 fields, 40 sections, 15 repeatable sections,
  61 rule actions, 7 validation-rule objects, bilingual labels, and form
  uid/version metadata;
- current Datarun shape format is flat and does not support repeating groups,
  nested objects, or arrays of objects;
- legacy string expressions and `Error` rules are not directly accepted by the
  current JSON AST expression boundary;
- option sets, labels, form/version/uid mapping, team/orgUnit mapping, and
  review/reconciliation vocabulary are compatibility metadata, not Datarun
  authority primitives;
- submitted-record import/replay, broad reporting/reconciliation, review
  queue/list/batch/automation, maintained household/facility/case lifecycle,
  new scope mechanisms, retention/security/offboarding promises, and pattern
  projection work remain separate trigger-based routes.

## Architecture Boundary Verdict

Boundary verdict: **operational/product precondition route needed; no
architecture route needed for NW-093 itself**.

NW-093 does not change contracts, envelope fields/types, stored event meaning,
sync/access scope, authority sources, resolver truth, deployer config power,
tenant/runtime partitioning, validation policy, or gap classifications.

The on-prem pilot does **not** trigger NW-094 through NW-098 by itself. One
first-organization on-prem deployment can remain in the accepted managed
single-tenant lane. NW-094 through NW-098 trigger only if multi-customer
managed deployment, SaaS control-plane lifecycle, tenant-aware identity,
tenant-aware storage/sync/config partitioning, pooled storage, tenant sync
context, or tenant isolation proof enters active scope.

## Real-Production Approval Gates

Before real users/data or real on-prem use, an accepted owner record must name:

- legal organization, site, country/region, jurisdiction, deployment owner,
  service owner, support owner, incident owner, data/compliance owner, and
  escalation channel;
- production host/provider/on-prem ownership, DNS/TLS boundary, PostgreSQL
  boundary, backup repository, monitoring destination, alert recipients, and
  support timezone;
- first operational flow and why it is narrow enough;
- data classes allowed in that flow, redaction/synthetic strategy, and classes
  still prohibited;
- self-hosted Keycloak operating owner, backup/restore and rotation posture,
  real principal-binding provisioning path, and OIDC-provider portability
  constraints;
- parallel-run source of truth, duplicate-entry handling, reconciliation
  procedure, mismatch stop criteria, rollback/stop path, and cutover authority;
- pilot-specific security/privacy/compliance review and explicit go/no-go
  language.

Synthetic reference deployment evidence can be cited as prior technical
evidence, but a pilot-specific preflight/rehearsal must prove the chosen
environment and selected first-flow package before go/no-go.

## Selected Successor

NW-147 should create a bounded compatibility and data-boundary artifact from
sanitized evidence only. It should:

- select exactly one first-flow candidate or park the real pilot route;
- classify the data needed by that first flow;
- map form structure, field types, option sets, labels, expressions,
  validations, version/uid metadata, team/orgUnit metadata, review and
  reconciliation expectations against accepted Datarun primitives;
- decide whether the first proof can proceed with synthetic/redacted data
  before real-data approval;
- identify exactly one next route after NW-147.

## Stop Conditions

Stop before implementation if the next work would:

- commit, transform, seed, or import raw real data;
- use the legacy account export beyond classification;
- migrate password hashes or make legacy login/team/orgUnit fields authority;
- implement form import/conversion/runtime support;
- add contracts, schemas, sync protocol, new scope, tenant partitioning, or
  runtime authority changes;
- broaden into reporting warehouse/export/import/API/catalog, queue/list/batch
  automation, entity lifecycle, retention/security/offboarding, or tenant
  control-plane work without selecting the named route.

## Validation Category

Touched surfaces are docs-only/status/backlog/artifacts/prompt surfaces.
Required validation is `git diff --check` plus targeted NW-093/NW-147 grep
checks. Runtime tests are skipped because NW-093 changes no runtime code,
tests, contracts, schemas, migrations, CI behavior, validation policy,
product/platform behavior acceptance, BAR, CDL, gap-register standing, mobile
code, server code, web-admin implementation, real-production approval, or real
users/data.
