# First Reference Deployment Policy

Status: accepted
Document type: operational_policy
Owner: Hamza
Source: NW-064 and
`docs/agent-working-surface/prompts/NW-064-define-first-reference-deployment-policy.md`
Amended by: NW-067 solo-owner operating-model gate; NW-163 local/on-prem
owner-operated production posture
Authority: operates within NW-063, BAR-001 through BAR-015, BAR-104,
DEC-EVENT-01, DEC-AUTH-02, DEC-AUTH-03, DEC-AUTH-05, DEC-CONFIG-03,
DEC-CONFIG-08, DEC-PROJECTION-01, DEC-BOUNDARY-01, IDR-027, IDR-028,
IDR-029, and IDR-030
Last reviewed: 2026-06-26
Supersedes: none
Related:
`docs/agent-working-surface/artifacts/NW-063-production-deployment-ops-hardening-map.md`,
`docs/operations/runbooks/production-deployment-runbook.md` (accepted under
NW-066), and
`docs/operations/rehearsals/production-deployment-rehearsal-plan.md` (accepted
under NW-066)

## 1. Acceptance Status

Hamza accepted this policy on 2026-06-13 for the provider-neutral synthetic
reference deployment and its repository-owned tooling, amended the operating
model on 2026-06-14 to support evidence-backed solo ownership for the
rehearsal and initial owner-operated production, and amended it through
NW-163 on 2026-06-26 to record that the SSH-operated lab server is the
selected pilot and initial owner-operated production host.

This policy distinguishes mandatory technical safety invariants, current
owner-selected facts, and provisional internal reliability targets. Mandatory
technical invariants include secret-safe handling, explicit principal binding,
TLS/network containment, immutable release evidence where a release is made,
and the Section 13 authority guardrails. Current owner-selected facts include
Hamza's solo-owner authority, the SSH-operated lab server as the selected
pilot and initial owner-operated production host, local VM/service separation,
and acknowledged same-host recovery risk.

The numerical operational values in this policy are internal reference targets
unless Hamza later selects them for a specific deployment promise or a concrete
external obligation requires them. They guide rehearsal and operational
improvement, but they do not create contractual SLA, fixed support hours,
24x7 response, retention, rotation, maintenance, backup, or cutover
prerequisites. They do not block feature work, authentication, pilot use, or
owner-approved initial production. Provider/region paperwork, an external
review body, a separate legal/compliance department, separate physical
infrastructure, or off-host backup is not a generic blocker. A blocker must
name the exact impossible action, the missing capability/input/access/dependency
or concrete external obligation, and the evidence.

No backup, restore, monitoring, rotation, release, or incident capability is
proven merely because it is required here. NW-065 must implement the necessary
tooling, NW-066 must describe the executable procedure, and NW-067 must
rehearse it.

CDL is architecture authority. Contracts control their declared technical
interfaces subject to CDL. Specifications, BAR, status, artifacts, code, and
tests are evidence of accepted standing or behavior; they cannot independently
create new architecture authority, prohibitions, or production blockers.

## 2. Scope And Deployment Class

This policy applies only to the first portable reference class selected by
NW-063:

- one Linux application host;
- one immutable Datarun server container;
- an external TLS reverse proxy, with the application port not publicly
  exposed;
- one durable PostgreSQL 16 service outside the application container
  lifecycle.

The SSH-operated lab server is selected for pilot and initial owner-operated
production. Logical separation can be satisfied by local VMs, containers, or
isolated services on that server when the named checks for the affected step
pass. The PostgreSQL service may be self-operated on that host or a local VM
if it meets the same access, backup, restore, monitoring, and evidence
requirements for the selected step.

This policy does not select Kubernetes, multi-host failover, zero-downtime
deployment, a cloud provider, managed identity, externally managed database,
or multi-customer control plane. Production web administration and mobile
OIDC login remain governed by their accepted platform/auth surfaces and the
selected NW-164 implementation route. This policy does not claim that the
current Dockerfile or compose files are production ready.

## 3. Plain-Language Roles

A role is a responsibility, not necessarily a job title. One person or small
team may hold several roles. The assignment must still be explicit so an
incident does not leave ownership ambiguous.

| Role | Plain-language responsibility |
|---|---|
| Deployment owner | Accountable for whether this deployment may exist and enter real production. Accepts this policy, funds the required controls, names the other owners, and accepts residual operational risk. |
| Service owner | Accountable for the running Datarun service, its service hours, support promise, SLO, planned maintenance, and user communication. |
| Application host owner | Maintains the Linux host, container runtime, firewall, operating-system updates, capacity, and access to the host. |
| DNS/TLS owner | Controls the public name, reverse proxy, certificates, certificate renewal, and the rule that only TLS traffic reaches the service. |
| Database owner | Operates PostgreSQL, database access, backups, point-in-time recovery where selected, restore execution, capacity, and database incident response. |
| Secrets owner | Controls the secret manager, access grants, rotation, emergency revocation, and response to suspected secret exposure. |
| Monitoring owner | Receives and maintains health, capacity, backup, certificate, and dependency alerts and ensures alerts reach a person who can act. |
| Release owner | Prepares the identified release artifact and deployment evidence. This role does not by itself grant approval to release. |
| Release approver | Decides whether a specific release may be deployed after checking evidence, maintenance timing, backup state, and recovery posture. |
| Incident commander | Coordinates a live incident, sets severity, assigns actions, authorizes communications, preserves evidence, and hands recovery decisions to the correct owner. |
| Support owner | Receives user reports, acknowledges them within the accepted support promise, and escalates operational or product issues. |
| Data/compliance owner | Classifies real deployment data, identifies jurisdiction, retention, privacy, residency, contractual, and breach-reporting obligations, and decides whether real data may be used. |
| Platform engineering | Implements and tests repository-owned deployment tooling without changing architecture, contracts, authority, or runtime semantics outside an authorized NW item. |

### 3.1 Small-Team Role Consolidation

One person may hold multiple or all roles when staffing requires it. Role
consolidation does not remove any control, evidence, or escalation duty.

The accepted initial operating model is solo owner-operated. Hamza may hold
deployment, approval, recovery, incident, support, and evidence roles for the
synthetic rehearsal and initial production. Separate named accounts may be
used for daily and privileged access, but another account owned by Hamza is
not an independent operator.

For every release, destructive recovery decision, privileged access grant,
principal-binding change, or configuration publication, the solo-owner record
must identify the exact artifact and change, automated verification, current
recovery point, rollback/restore/forward-fix posture, explicit go/no-go
decision, and retrospective review. AI-agent assistance may support execution
and review, but accountability and operational authority remain with Hamza.

Independent human continuity is explicitly unproven and must not be claimed.
Introduce a second authorized person when an organization, customer,
contract, regulator, insurer, or selected service/support promise requires
separation or coverage; when Hamza cannot meet the selected response and
recovery duties alone; or when another operator is intentionally onboarded.
Until such a trigger occurs, record the solo-owner bus-factor risk rather than
manufacturing second-person evidence.

No person may use role consolidation to infer application authority from host,
database, IdP, or cloud-administrator access. Platform actor, assignment,
principal-binding, and resolver authority remain governed by accepted platform
rules.

## 4. Policy Terms In Plain Language

| Term | Meaning in this policy |
|---|---|
| Production | An environment serving real users or holding real organizational data. A public test endpoint is also production if real data or reliance exists. |
| Synthetic rehearsal | A disposable or isolated exercise using invented, non-sensitive data and test identities to prove procedures. It is technical evidence, not permission to process real data. |
| RPO | Recovery point objective: the maximum amount of recent durable database history the owner is prepared to lose after a disaster, measured as time. |
| RTO | Recovery time objective: the maximum target time from disaster declaration until the service is restored to its agreed minimum usable state. |
| Backup | A recoverable copy or recovery point. For initial owner-operated local production, same-host recovery copies are accepted with the explicit limitation that they do not protect against total physical-host loss. A Docker volume alone is not a backup. |
| Point-in-time recovery (PITR) | Restoring the database to a selected time using a base backup plus transaction logs or a provider-equivalent feature. |
| Off-site | Stored outside the failure and access boundary of the live application host and primary database. It is a prioritized reliability improvement and may become required for a named disaster-recovery claim or external obligation, but it is not a generic feature, auth, pilot, or owner-operated production blocker. |
| SLO | Service level objective: the internal reliability target used to judge whether service quality is acceptable. It is not a contractual guarantee unless separately agreed. |
| Service hours | The hours during which users are told the service is intended to be available. |
| Support hours | The hours during which a person is committed to receive and respond to reports. These may be narrower than service hours. |
| Maintenance window | A pre-announced period in which an approved change may cause reduced availability. |
| Release artifact | The exact immutable container image identified by digest, plus its source commit and verification evidence. |
| Rollback | Replacing the application with a previously accepted image only when the current database schema and data remain compatible with it. |
| Database restore | Replacing or rebuilding the database from an accepted recovery point. It can discard post-recovery-point history and therefore requires explicit authority. |
| Forward fix | Correcting a failed deployment with a new tested change while preserving the current database history rather than reversing schema changes. |
| Disaster declaration | The recorded decision that normal repair is insufficient and the disaster-recovery path, including restore where needed, is active. |
| Evidence | Secret-safe records that show what was approved, executed, observed, and recovered: digests, timestamps, logs, checks, tickets, and rehearsal or incident records. |
| Least privilege | Giving a named person or workload only the access needed for its current duty, for no longer than needed. |

## 5. Ownership, Environments, And Access

Current owner-selected facts and technical safety invariants:

- Record Hamza as accountable deployment owner and operational contact for
  the initial owner-operated production environment unless a later concrete
  obligation requires another named party.
- Keep production separate from development and synthetic rehearsal by
  database, credentials, DNS name, secret namespace, monitoring destination,
  and local VM/container/service boundary where needed. A separate physical
  server is not required for this separation by default.
- Use a staging or rehearsal environment that matches the production
  deployment class but contains no real production data.
- Permit administrative access only to named individual accounts protected by
  multi-factor authentication. Do not use shared human credentials.
- Give workloads separate least-privilege database and secret identities.
- Require encrypted PostgreSQL connections with certificate verification
  appropriate to the selected provider or self-operated service.
- Log privileged access and changes. The 90-day access-review cadence and
  four-hour normal removal target are internal reference targets, not
  owner-selected service commitments or blockers. Disable access immediately
  for a suspected compromise.
- Do not expose the application HTTP port to the public network; public access
  terminates at the TLS reverse proxy.

Hamza holds the environment/account, application host, DNS/TLS, database,
secrets, monitoring, support, release, incident, and owner-decision roles for
the synthetic rehearsal and selected initial owner-operated production. The
reference tooling remains provider neutral and targets PostgreSQL 16 outside
the application container. The selected host is the SSH-operated lab server;
local VMs/services can satisfy logical separation. Provider/region wording
means the local host/operator, site/country boundary, maintenance-access
boundary, backup location, monitoring/log destination, and support access
path, not an unselected cloud provider. The 90-day access-review and four-hour
normal removal values remain internal reference targets.

## 6. Data Protection And Recovery

### 6.1 Recovery Objectives

Provisional internal recovery targets:

- RPO reference target: no more than 1 hour of committed PostgreSQL history.
- RTO reference target: restore the minimum usable server service within 8
  hours of disaster declaration.
- Measure both objectives during rehearsal and incidents. An unmeasured target
  is not evidence that the target can be met.

The 1-hour RPO and 8-hour RTO are provisional internal reliability targets.
They do not block feature work, authentication, pilot use, or owner-approved
initial production unless Hamza selects a specific recovery promise or a
concrete external obligation requires it. Hamza is the database restore
authority and disaster-declaration authority for the synthetic rehearsal and
initial owner-operated production.

### 6.2 Backup And Restore Controls

Technical safety invariants and internal recovery targets:

- Use transaction-log/PITR protection or a provider-equivalent capability when
  claiming the reference RPO.
- Create at least one recoverable base backup or provider snapshot when
  claiming restore readiness. A daily cadence is an internal reference target.
- Retain daily recovery points for 35 days and monthly recovery points for 12
  months only as internal reference targets unless Hamza selects a deployment
  promise or an identified obligation requires another retention rule.
- Encrypt backups in transit and at rest with keys outside the application
  container and ordinary operator credentials.
- For initial owner-operated local production, keep at least one recoverable
  same-host copy or recovery point and record that this does not protect
  against total physical-host loss. Add an off-host or protected separate
  failure/access boundary as a prioritized reliability improvement, and treat
  it as required only for a named disaster-recovery claim, external obligation,
  or selected higher-reliability route.
- Monitor backup completion and recovery-point freshness. A missed backup or
  stale recovery point is at least a severity-2 incident until corrected.
- Restore into an isolated clean environment at least quarterly and before
  first owner-operated production use when that use claims restore readiness.
  Verify schema history, event history,
  configuration packages, principal-binding audit/history, assignments, and
  representative auth/config/sync paths.
- Preserve failed restore evidence. Do not repair schema history by manual
  database edits.

Backup and PITR implementation should remain provider neutral. Encryption and
secret separation are technical safety invariants; the PITR, daily, 35-day,
12-month, and restore-test values are internal reference targets unless a
specific deployment claim or obligation requires them. Hamza is the
encryption/key owner, restore operator, and restore approver for the synthetic
rehearsal and initial owner-operated production. Same-host recovery is accepted
initially with total-host-loss risk acknowledged; it is not disaster recovery.
Off-host backup and separate physical infrastructure remain prioritized
reliability improvements, not feature, auth, pilot, or initial
owner-operated-production blockers unless a named claim or obligation requires
them.

Database restore is not ordinary application rollback. It may lose history
newer than the recovery point. The database owner executes it only after the
authorized restore decision is recorded, except when immediate action is
required to contain active corruption or compromise; emergency use must be
reviewed afterward by the deployment owner and data/compliance owner.

## 7. Secrets And Privileged Configuration

Technical safety invariants and internal rotation targets:

- Store database passwords, OIDC/JWKS configuration where confidential,
  private keys, tokens, and other credentials in a deployment-selected secret
  manager. Do not commit them, bake them into images, or retain them in
  compose files, shell history, screenshots, tickets, or rehearsal evidence.
- Grant secret access to named workloads and the minimum named operators.
- Rotate long-lived application and database credentials on an owner-selected
  cadence. A 90-day cadence is an internal reference target. Certificates must
  be renewed before expiry, and any credential must be revoked or rotated
  immediately after suspected exposure or owner departure.
- Alert at 30, 14, and 7 days before certificate or time-bounded credential
  expiry.
- Test rotation in the synthetic reference environment before first
  owner-operated production use that depends on those credentials when that
  use claims rotation readiness. Annual rehearsal is an internal reference
  target.
- Record secret identifiers, owners, creation/rotation dates, and verification
  results, but never secret values.
- Treat principal-binding manifests and deployment configuration as reviewed
  privileged inputs. Keep their change history and approval evidence without
  turning IdP groups, roles, JWT `actor_id`, or other claims into platform
  authority.

Secrets must stay outside source control, outside the application image, and
out of logs/evidence. They may be injected through restrictive host-mounted
files or another local mechanism that satisfies those constraints; no external,
paid, cloud, or provider-specific secret manager is required. Hamza is the
secrets owner and emergency revocation contact. The 90-day rotation,
expiry-alert, and annual rehearsal values are internal reference targets.
Immediate suspected-exposure revocation remains a safety invariant.

## 8. Monitoring, Capacity, SLO, And Support

### 8.1 Service And Reliability Targets

Provisional internal reliability targets:

- Availability reference target: 99.0% per calendar month, measured across all
  calendar time and including planned maintenance so maintenance cost remains
  visible.
- Service-hours reference target: 24 hours a day once Hamza selects
  owner-operated production use for the affected deployment.
- Staffed-support reference target: 08:00-18:00 in the deployment's local time,
  Monday through Friday excluding published holidays.
- Do not promise high availability, automatic failover, or zero downtime for
  the single-host reference class.

No contractual SLA, guaranteed availability, fixed support hours, or 24x7
response commitment is selected. The 99.0% availability, 24x7 service-hour,
and 08:00-18:00 `Asia/Aden` staffed-support values are internal reference
targets until Hamza explicitly selects a service promise for a concrete
deployment. No funded 24x7 severity-1 response roster exists, so 24x7 incident
response must not be advertised.

### 8.2 Required Signals And Thresholds

NW-065 must provide or integrate signals for application readiness, request
failure, host and database capacity, database connectivity, backup freshness,
certificate expiry, and OIDC/JWKS dependency failures.

Internal monitoring and capacity targets:

- Warn when CPU, memory, disk, or database connection use exceeds 70% of
  provisioned capacity for 15 minutes.
- Page or escalate when use exceeds 85% for 10 minutes, disk is projected to
  fill within 24 hours, the application is not ready for 5 minutes, repeated
  server errors materially affect users, the database is unavailable, backup
  freshness exceeds the selected or reference recovery target, or a certificate
  has less than 7 days remaining.
- Review capacity monthly and before a material user, data-volume, or
  configuration expansion.
- Every alert names an owner, response action, escalation path, and link to the
  applicable runbook. An alert with no responder is not an operational control.

The thresholds and durations above are internal reference targets. NW-065 may
expose Actuator/Prometheus-compatible health and metrics signals plus
structured standard-output logs. Hamza is the primary alert recipient for the
synthetic rehearsal and initial owner-operated production. A backup recipient
and final monitoring/logging destination are reliability improvements unless a
concrete support promise or external obligation makes them necessary for a
named action.

### 8.3 Incident Severity And Communication

| Severity | Plain-language trigger | Recommended initial response |
|---|---|---|
| Severity 1 - critical | Confirmed or suspected unauthorized data/authority exposure, event-history corruption, unrecoverable production data loss, or complete production outage with no safe workaround. | Acknowledge within 15 minutes if a 24x7 roster is accepted, appoint an incident commander, contain unsafe access, and update stakeholders at least hourly. |
| Severity 2 - major | Material degradation, database/provider outage, failed or stale backup, failed release affecting users, or recovery risk without confirmed data exposure. | Acknowledge within 1 support hour, assign an owner, and update stakeholders at least every 4 hours during active handling. |
| Severity 3 - minor | Limited defect or operational issue with a safe workaround and no material data, authority, or availability risk. | Acknowledge within 1 business day and route normal corrective work. |

Authority drift, cross-scope data exposure, secret leakage, event mutation, and
unsafe migration state are severity 1 even if availability appears healthy.

The severity definitions are an internal triage model. The acknowledgement and
update timings are internal reference targets, not an external support
commitment. Hamza is the incident commander, support contact, and escalation
authority for the synthetic rehearsal and initial owner-operated production.
Production-specific security/executive escalation and user communication
channels must be selected only when the affected deployment or external
obligation needs them.

## 9. Release, Migration, And Recovery Authority

Technical safety invariants and maintenance targets:

- Deploy only an immutable image identified by digest and source commit after
  required tests, clean image/resource inspection, vulnerability review, and
  environment preflight pass.
- Keep a two-hour maintenance window available when needed. This is not a
  requirement to perform weekly maintenance. Give at least 48 hours notice only
  when actual users will be affected and the work is not emergency security or
  recovery work.
- Require release-owner preparation and release-approver authorization. In the
  accepted solo-owner model, Hamza may hold both roles only with the evidence
  and retrospective review required by Section 3.1.
- Before a schema migration, confirm a successful recovery point consistent
  with the selected or reference recovery target and confirm that the database
  owner can access the restore procedure.
- Let Flyway perform only the supported forward migration path. Do not invent
  down migrations, edit Flyway history, or use undocumented manual SQL.
- Roll back only the application image, and only when tests or an explicit
  compatibility statement prove that the older image can safely use the
  current schema and data.
- If compatibility is not proven, stop traffic where needed and choose either
  an authorized database restore or a tested forward fix. Do not call an
  unproven image downgrade a rollback.
- Release approval never grants platform actor, assignment, resolver, or
  principal-binding authority.

Hamza is release owner, release approver, application rollback authority,
database restore authority, forward-fix authority, and the decision-maker
between restore and forward fix under the solo-owner model. Synthetic
rehearsal maintenance is scheduled by the rehearsal plan; a real deployment
may record an available two-hour window in `Asia/Aden` when planned
maintenance is needed. Notice applies only when actual users will be affected.

## 10. Evidence And Review

Evidence safety invariants and internal retention targets:

- Retain release approvals, image digests, deployment checks, privileged access
  changes, configuration and principal-binding approvals, rehearsal records,
  incident timelines, recovery decisions, and post-incident actions. A
  13-month retention period is an internal reference target unless Hamza
  selects another deployment retention rule or an identified obligation
  requires one.
- Retain security incident and legal-hold evidence longer when directed by the
  data/compliance owner.
- Keep evidence access-controlled, tamper-resistant where the selected
  platform supports it, and free of secret values and unnecessary personal or
  production record contents.
- Review this policy every 12 months as an internal target and after a
  severity-1 incident, failed restore, missed selected recovery target,
  provider or region change, material data reclassification, ownership change,
  or change to the reference deployment class.

The 13-month retention period and 12-month review cadence are internal
reference targets. Hamza is the evidence owner and legal-hold authority.
Sanitized summaries may be committed to this repository; raw logs, command
output, and other detailed evidence must remain outside the repository,
secret-safe, and access-controlled. That storage may be local; no cloud or
paid external evidence store is required.

## 11. Synthetic Rehearsal And Owner-Operated Production Selection

NW-067 synthetic rehearsal may prove that the selected image, PostgreSQL
recovery path, provisioning inputs, monitoring, rotation, incident response,
and solo cold-recovery procedure work with invented data in the reference
environment. It does not prove independent human continuity.

NW-163 selects the SSH-operated lab server for pilot and initial
owner-operated production. Hamza is the deployment owner, data/compliance
owner, approval authority, operational authority, support contact, and
decision-maker for that initial deployment. A written Hamza decision is enough
owner approval unless a specific applicable external obligation is identified.

Local VMs, containers, and isolated services on the selected server satisfy
logical separation for the initial deployment when the affected step's checks
pass. Same-host recovery copies are accepted initially with the explicit
limitation that total physical-host loss can still lose both service and
backup copies. Off-host backup and separate physical infrastructure remain
prioritized reliability improvements, not feature, auth, pilot, or initial
owner-operated production blockers.

Before real users or real organizational data are introduced, Hamza records
only the facts needed for that affected action:

- named legal organization and accountable deployment owner;
- deployment jurisdiction, local site/country boundary, maintenance-access
  boundary, backup location, monitoring/log destination, and support access
  path when relevant to the selected flow;
- data classification, including personal, sensitive, regulated, or
  organization-confidential categories;
- required retention, deletion, legal hold, breach notification, audit,
  contractual, regulator, customer, or other external obligations when one has
  been identified and shown to apply;
- user population, service/support promise, expected capacity, and field
  connectivity assumptions;
- accepted identity-provider and token acquisition path for the intended
  users;
- explicit acceptance of the solo-owner bus-factor risk, or the named second
  operator and coverage model when a Section 3.1 trigger applies;
- whether unresolved production web-admin authentication, mobile OAuth/OIDC
  login, or NW-054 device expiry/decommissioning/encryption questions block
  a named action in that deployment;
- any security, privacy, or compliance review required by the owning
  organization or another identified external obligation.

Missing provider/region paperwork, external review, separate departments, or
incomplete documentation does not block technical preparation, fresh local
OIDC login, principal binding, pilot feature work, or other independent
implementation. It blocks only the exact action that cannot proceed, and only
when the missing fact, capability, input, access, dependency, or obligation is
named with evidence.

Treat an unknown classification, jurisdiction, retention, user-login,
device-security, or breach obligation as a blocker only to the specific
real-data or cutover action that depends on it. Do not convert unknowns into a
global production prohibition or a reason to stop unrelated implementation.

Provider/region language is local/on-prem language for this route. It does
not require an unselected cloud provider, managed identity provider, managed
database, external monitoring destination, remote support provider, or
cross-border transfer. Any such option must be selected by a later owner
decision and must not become a default blocker.

## 12. Owner Acceptance Register

The deployment owner completed every row below. Future replacement values must
be at least as explicit as the baseline interpretation.

| Selection | Baseline interpretation | Owner selection |
|---|---|---|
| Deployment and service owners | Technical safety invariant: named accountability; roles may be combined explicitly. | Hamza holds both roles under the solo-owner model. |
| Host, DNS/TLS, database, secrets, monitoring owners | Technical safety invariant: one named accountable owner for each responsibility; add independent coverage when a Section 3.1 trigger applies. | Hamza holds all roles under the accepted solo-owner model; independent human continuity is unproven. |
| Release, incident, support, data/compliance owners | Technical safety invariant: named accountable owner for each function. | Hamza holds all roles under the solo-owner model. |
| Small-team approval model | Evidence-backed solo approval until a concrete separation or coverage trigger applies. | Hamza may self-approve rehearsal, pilot, and initial owner-operated production with the Section 3.1 record. |
| Hosting, PostgreSQL, region, environment boundaries | Current owner-selected fact: separate production and rehearsal identities, data, DNS, secrets, monitoring, and local service boundaries can be local/on-prem. | The SSH-operated lab server is selected as the pilot and initial owner-operated production host. Local VMs/services can satisfy logical separation. Provider/region means local host/operator and site boundary, not unselected cloud paperwork. |
| Access controls | Technical safety invariant: named individual accounts, no shared human credentials for privileged operations, encrypted PostgreSQL connections where used. Cadence/removal timings are internal reference targets. | Hamza owns access control under the solo-owner model; 90-day review and four-hour normal removal remain targets only. |
| RPO and RTO | Provisional internal reliability targets: RPO 1 hour and RTO 8 hours. | Not a contractual commitment, cutover gate, or feature/auth/pilot blocker unless Hamza selects that recovery promise or an external obligation requires it. |
| Backup and restore | Technical safety invariant: recoverable copy for claimed restore readiness and no secret exposure. PITR, daily copy, 35 daily/12 monthly points, and restore-test cadence are internal reference targets. | Same-host recovery copies are initially accepted with total-host-loss risk acknowledged; provider-neutral improvements remain prioritized, not blockers. |
| Backup encryption and off-site posture | Technical safety invariant: protect backup secrets/keys. Same-host recovery copies are accepted initially with total-host-loss risk acknowledged; off-host/separate-boundary copies are prioritized reliability improvements. | Current owner-selected risk posture after NW-163. |
| Disaster and restore authority | Named declarer, restore approver, and restore operator. | Hamza holds all three roles under the solo-owner model. |
| Secret storage and rotation | Technical safety invariant: no secrets in source control, images, logs, or evidence; immediate revocation after suspected exposure. Rotation cadence is an internal reference target. | Restrictive host-mounted files or another local mechanism outside source control and the application image is enough; no external or paid secret manager is required. |
| SLO and service/support hours | Provisional internal targets only: 99.0% monthly, 24x7 service-hours target, 08:00-18:00 local weekday support target. | No contractual SLA, guaranteed availability, fixed support hours, or 24x7 response commitment is selected. |
| Monitoring and capacity | Internal reference targets: 70% warning, 85% critical, backup/RPO, readiness, database, certificate, and provider alerts. | Signals are useful evidence; they do not block feature work, authentication, pilot use, or owner-approved initial production by themselves. |
| Incident model | Internal triage model: three severities and reference acknowledgement/update timings. | Hamza owns response under the solo-owner model; no external response commitment is selected. |
| Release and maintenance | Technical safety invariant: immutable digest and safe migration/rollback/restore authority. Maintenance windows and 48-hour notice are conditional targets. | A weekly window is available when needed, not required weekly. Notice applies only when actual users will be affected. |
| Rollback, restore, forward-fix decision | App rollback only with proven schema compatibility; otherwise authorized restore or tested forward fix. | Hamza holds this authority under the solo-owner model. |
| Evidence retention and policy review | Technical safety invariant: evidence must be secret-safe and outside the repository when raw. Retention and review cadence are internal reference targets. | A 13-month retention period and annual review are targets only. Raw evidence storage may be access-controlled local storage; no cloud storage is required. |
| Real-data compliance gate | Unknown classification or a specific legal/security obligation blocks only the affected real-data or cutover action. | Unselected real-data/cutover work remains unselected, while independent implementation may continue. |

Acceptance record:

- Deployment owner name: Hamza
- Deployment owner decision date: 2026-06-13
- Solo-owner amendment decision date: 2026-06-14
- Data/compliance owner name: Hamza
- Approved operating model and rationale: evidence-backed solo ownership is
  accepted for rehearsal and initial owner-operated production because no
  second operator exists. Independent human continuity remains an explicit
  residual risk and becomes mandatory only on a Section 3.1 trigger.
- Next scheduled review target: 2027-06-13, or earlier on a Section 10 trigger.

## 13. Mandatory Guardrails And Exceptions

This policy cannot:

- add, delete, rewrite, or reinterpret event history;
- change event envelope fields or types, contracts, sync watermarks, pull
  classes, access scope, assignment authority, resolver equality, or
  projection semantics;
- make IdP groups, roles, JWT `actor_id`, or other claims direct platform
  authority;
- replace explicit principal binding or its deployment-managed audited
  provisioning path;
- expose development admin surfaces, define production web-admin authority, or
  define mobile OAuth/OIDC login;
- claim device expiry, decommissioning, sealed-partition recovery, local
  encryption, redaction, or token/session retention closure reserved for
  NW-054/BAR-106;
- claim backup, restore, monitoring, alerting, rotation, rollback, or recovery
  works before implementation and rehearsal evidence exists.

An operational exception must name its owner, scope, reason, start and expiry
dates, compensating controls, evidence, and review authority. No exception may
waive the guardrails above or silently lower a mandatory safety invariant,
selected recovery promise, security, authority, or real-data approval boundary.
Such a request requires a new bounded NW route and, where applicable,
architecture or platform authority.

## 14. Successor And Acceptance Boundary

NW-064 is accepted with the Section 12 distinctions above. NW-065 may proceed
within this policy. NW-066 must translate only tested NW-065 tooling into
procedures. NW-067 may measure provisional recovery targets and exercise clean
install, restore, upgrade/failure, rotation, alert/incident, and solo cold
recovery without turning those reference targets into cutover gates.

Section 11 no longer creates a global production blocker. It requires the
specific owner facts and evidence needed for the affected real-data or cutover
action while allowing independent feature, auth, pilot, and local deployment
preparation to proceed.
