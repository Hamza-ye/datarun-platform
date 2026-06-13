# First Reference Deployment Policy

Status: in_review
Document type: operational_policy
Owner: Deployment owner (named selection pending)
Source: NW-064 and
`docs/agent-working-surface/prompts/NW-064-define-first-reference-deployment-policy.md`
Authority: operates within NW-063, BAR-001 through BAR-015, BAR-104,
DEC-EVENT-01, DEC-AUTH-02, DEC-AUTH-03, DEC-AUTH-05, DEC-CONFIG-03,
DEC-CONFIG-08, DEC-PROJECTION-01, DEC-BOUNDARY-01, IDR-027, IDR-028,
IDR-029, and IDR-030
Last reviewed: 2026-06-13
Supersedes: none
Related:
`docs/agent-working-surface/artifacts/NW-063-production-deployment-ops-hardening-map.md`,
`docs/operations/runbooks/production-deployment-runbook.md` (planned by
NW-066), and
`docs/operations/rehearsals/production-deployment-rehearsal-plan.md` (planned
by NW-066)

## 1. Proposal Status

This document proposes the operating choices for the first reference
deployment. It is not accepted policy yet.

Every value labelled **Recommended default** is a conservative starting point,
not an approved commitment. Every corresponding selection remains:

> **TBD - owner acceptance required**

The deployment owner must name the responsible people or teams, accept or
replace every required value in Section 12, and record any approved exception
before NW-064 can become `accepted`.

No backup, restore, monitoring, rotation, release, or incident capability is
proven merely because it is required here. NW-065 must implement the necessary
tooling, NW-066 must describe the executable procedure, and NW-067 must
rehearse it.

## 2. Scope And Deployment Class

This policy applies only to the first portable reference class selected by
NW-063:

- one Linux application host;
- one immutable Datarun server container;
- an external TLS reverse proxy, with the application port not publicly
  exposed;
- one durable external PostgreSQL 16 service outside the application
  container lifecycle.

The PostgreSQL service may be managed or self-operated if it meets the same
access, backup, restore, monitoring, and evidence requirements.

This policy does not select Kubernetes, multi-host failover, zero-downtime
deployment, a cloud provider, production web administration, or mobile
OAuth/OIDC login. It does not claim that the current Dockerfile or compose
files are production ready.

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

**Recommended default:** use a second authorized person to review production
releases, destructive recovery decisions, privileged access grants, and
principal-binding or configuration changes.

**TBD - owner acceptance required:** name the consolidated role assignments
and decide whether a one-person self-approval exception is allowed. If allowed,
each use must record why no second reviewer was available, the exact artifact
and change, automated verification results, backup/restore readiness, and the
deployment owner's retrospective review.

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
| Backup | A recoverable copy or provider recovery point kept separately from the live application host. A Docker volume alone is not a disaster backup. |
| Point-in-time recovery (PITR) | Restoring the database to a selected time using a base backup plus transaction logs or a provider-equivalent feature. |
| Off-site | Stored outside the failure and access boundary of the live application host and primary database. Prefer a separate account or protected backup boundary. |
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

**Recommended defaults:**

- Name one accountable deployment owner and one operational contact for every
  role in Section 3 before a production environment is created.
- Keep production separate from development and synthetic rehearsal by
  account or project, database, credentials, DNS name, secret namespace, and
  monitoring destination.
- Use a staging or rehearsal environment that matches the production
  deployment class but contains no real production data.
- Permit administrative access only to named individual accounts protected by
  multi-factor authentication. Do not use shared human credentials.
- Give workloads separate least-privilege database and secret identities.
- Require encrypted PostgreSQL connections with certificate verification
  appropriate to the selected provider or self-operated service.
- Log privileged access and changes. Review active human and workload access
  every 90 days and immediately after role changes or an incident.
- Disable access within four hours of an approved removal request and
  immediately for a suspected compromise.
- Do not expose the application HTTP port to the public network; public access
  terminates at the TLS reverse proxy.

**TBD - owner acceptance required:** name the environment/account owner,
application host owner, DNS/TLS owner, database owner, secrets owner, and
monitoring owner; identify the hosting and PostgreSQL provider or self-operated
boundary; select the production region and maintenance access path; accept or
replace the 90-day access review and four-hour removal targets.

## 6. Data Protection And Recovery

### 6.1 Recovery Objectives

**Recommended defaults:**

- RPO: no more than 1 hour of committed PostgreSQL history.
- RTO: restore the minimum usable server service within 8 hours of disaster
  declaration.
- Measure both objectives during rehearsal and incidents. An unmeasured target
  is not evidence that the target can be met.

**TBD - owner acceptance required:** accept or replace the RPO and RTO, name
the database restore authority, and name who may declare a disaster.

### 6.2 Backup And Restore Controls

**Recommended defaults:**

- Use continuous transaction-log/PITR protection or a provider-equivalent
  capability consistent with the accepted RPO.
- Create at least one daily recoverable base backup or provider snapshot.
- Retain daily recovery points for 35 days and monthly recovery points for 12
  months, subject to the data/compliance owner's real-data decision.
- Encrypt backups in transit and at rest with keys outside the application
  container and ordinary operator credentials.
- Keep at least one protected off-site or separate-account copy that a
  compromise of the application host cannot delete.
- Monitor backup completion and recovery-point freshness. A missed backup or
  stale recovery point is at least a severity-2 incident until corrected.
- Restore into an isolated clean environment at least quarterly and before
  first production approval. Verify schema history, event history,
  configuration packages, principal-binding audit/history, assignments, and
  representative auth/config/sync paths.
- Preserve failed restore evidence. Do not repair schema history by manual
  database edits.

**TBD - owner acceptance required:** select the backup/PITR service, schedules,
35-day and 12-month retention values, encryption/key owner, off-site boundary,
quarterly restore cadence, restore operator, restore approver, and any legal
hold or deletion constraints.

Database restore is not ordinary application rollback. It may lose history
newer than the recovery point. The database owner executes it only after the
authorized restore decision is recorded, except when immediate action is
required to contain active corruption or compromise; emergency use must be
reviewed afterward by the deployment owner and data/compliance owner.

## 7. Secrets And Privileged Configuration

**Recommended defaults:**

- Store database passwords, OIDC/JWKS configuration where confidential,
  private keys, tokens, and other credentials in a deployment-selected secret
  manager. Do not commit them, bake them into images, or retain them in
  compose files, shell history, screenshots, tickets, or rehearsal evidence.
- Grant secret access to named workloads and the minimum named operators.
- Rotate long-lived application and database credentials every 90 days,
  certificates before expiry, and any credential immediately after suspected
  exposure or owner departure.
- Alert at 30, 14, and 7 days before certificate or time-bounded credential
  expiry.
- Test rotation in the synthetic reference environment before first
  production approval and at least annually thereafter.
- Record secret identifiers, owners, creation/rotation dates, and verification
  results, but never secret values.
- Treat principal-binding manifests and deployment configuration as reviewed
  privileged inputs. Keep their change history and approval evidence without
  turning IdP groups, roles, JWT `actor_id`, or other claims into platform
  authority.

**TBD - owner acceptance required:** select the secret manager and key
boundary; name the secrets owner; accept or replace the 90-day rotation,
expiry-alert, and annual rehearsal intervals; select the emergency revocation
contact and maximum response time.

## 8. Monitoring, Capacity, SLO, And Support

### 8.1 Service And Reliability Targets

**Recommended defaults:**

- Availability SLO: 99.0% per calendar month, measured across all calendar
  time and including planned maintenance so maintenance cost remains visible.
- Service hours: 24 hours a day once real production approval is granted.
- Staffed support hours: 08:00-18:00 in the deployment's local time, Monday
  through Friday excluding published holidays.
- Do not promise high availability, automatic failover, or zero downtime for
  the single-host reference class.

**TBD - owner acceptance required:** select the availability SLO, service
hours, support hours, local time zone, holiday calendar, and whether a funded
24x7 severity-1 response roster exists. Do not advertise 24x7 incident response
unless named people have accepted the roster.

### 8.2 Required Signals And Thresholds

NW-065 must provide or integrate signals for application readiness, request
failure, host and database capacity, database connectivity, backup freshness,
certificate expiry, and OIDC/JWKS dependency failures.

**Recommended defaults:**

- Warn when CPU, memory, disk, or database connection use exceeds 70% of
  provisioned capacity for 15 minutes.
- Page or escalate when use exceeds 85% for 10 minutes, disk is projected to
  fill within 24 hours, the application is not ready for 5 minutes, repeated
  server errors materially affect users, the database is unavailable, backup
  freshness exceeds the accepted RPO, or a certificate has less than 7 days
  remaining.
- Review capacity monthly and before a material user, data-volume, or
  configuration expansion.
- Every alert names an owner, response action, escalation path, and link to the
  applicable runbook. An alert with no responder is not an operational control.

**TBD - owner acceptance required:** accept or replace the thresholds and
durations, select the monitoring/logging destination, and name the primary and
backup alert recipients.

### 8.3 Incident Severity And Communication

| Severity | Plain-language trigger | Recommended initial response |
|---|---|---|
| Severity 1 - critical | Confirmed or suspected unauthorized data/authority exposure, event-history corruption, unrecoverable production data loss, or complete production outage with no safe workaround. | Acknowledge within 15 minutes if a 24x7 roster is accepted, appoint an incident commander, contain unsafe access, and update stakeholders at least hourly. |
| Severity 2 - major | Material degradation, database/provider outage, failed or stale backup, failed release affecting users, or recovery risk without confirmed data exposure. | Acknowledge within 1 support hour, assign an owner, and update stakeholders at least every 4 hours during active handling. |
| Severity 3 - minor | Limited defect or operational issue with a safe workaround and no material data, authority, or availability risk. | Acknowledge within 1 business day and route normal corrective work. |

Authority drift, cross-scope data exposure, secret leakage, event mutation, and
unsafe migration state are severity 1 even if availability appears healthy.

**TBD - owner acceptance required:** accept or replace severity definitions,
acknowledgement targets, update cadence, incident commander roster, support
contact, executive/security escalation, and user communication channel.

## 9. Release, Migration, And Recovery Authority

**Recommended defaults:**

- Deploy only an immutable image identified by digest and source commit after
  required tests, clean image/resource inspection, vulnerability review, and
  environment preflight pass.
- Use one scheduled two-hour maintenance window per week, with at least 48
  hours notice for user-visible production maintenance. Emergency security or
  recovery work may occur outside the window with incident records.
- Require release-owner preparation and release-approver authorization. Use a
  second person where available; Section 3.1 governs any one-person exception.
- Before a schema migration, confirm a successful recovery point no older than
  the accepted RPO and confirm that the database owner can access the restore
  procedure.
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

**TBD - owner acceptance required:** select the maintenance window and notice,
name the release owner and approver, decide whether the one-person exception is
allowed, name application rollback authority, name database restore authority,
name forward-fix authority, and define who decides between restore and forward
fix during an incident.

## 10. Evidence And Review

**Recommended defaults:**

- Retain release approvals, image digests, deployment checks, privileged access
  changes, configuration and principal-binding approvals, rehearsal records,
  incident timelines, recovery decisions, and post-incident actions for 13
  months.
- Retain security incident and legal-hold evidence longer when directed by the
  data/compliance owner.
- Keep evidence access-controlled, tamper-resistant where the selected
  platform supports it, and free of secret values and unnecessary personal or
  production record contents.
- Review this policy every 12 months and after a severity-1 incident, failed
  restore, missed RPO/RTO, provider or region change, material data
  reclassification, ownership change, or change to the reference deployment
  class.

**TBD - owner acceptance required:** accept or replace the 13-month evidence
retention and 12-month review cadence; name the evidence repository, evidence
owner, approved readers, and legal-hold authority.

## 11. Synthetic Rehearsal Versus Real Production Approval

NW-067 synthetic rehearsal may prove that the selected image, PostgreSQL
recovery path, provisioning inputs, monitoring, rotation, incident response,
and operator handoff work with invented data in the reference environment.

A successful synthetic rehearsal does **not** approve a real production
deployment. Before real users or real organizational data are introduced, the
deployment owner and data/compliance owner must separately record:

- named legal organization and accountable deployment owner;
- deployment jurisdiction, region, and data residency constraints;
- data classification, including personal, sensitive, regulated, or
  organization-confidential categories;
- required retention, deletion, legal hold, breach notification, audit, and
  contractual controls;
- user population, service/support promise, expected capacity, and field
  connectivity assumptions;
- accepted identity-provider and token acquisition path for the intended
  users;
- whether unresolved production web-admin authentication, mobile OAuth/OIDC
  login, or NW-054 device expiry/decommissioning/encryption questions block
  that deployment;
- completed security, privacy, and compliance review required by the owning
  organization.

Until that approval exists, use synthetic, non-sensitive data and test
identities only.

**Recommended default:** treat any unknown classification, jurisdiction,
retention, user-login, device-security, or breach obligation as a blocker to
real production, not as an implied acceptance.

**TBD - owner acceptance required:** name the data/compliance owner, intended
jurisdictions, initial data classification, required organizational reviews,
and the exact real-production approval authority.

## 12. Owner Acceptance Register

The deployment owner must complete every row. A replacement value must be at
least as explicit as the recommended default.

| Selection | Recommended default | Owner selection |
|---|---|---|
| Deployment and service owners | Named accountable people; roles may be combined explicitly. | **TBD - owner acceptance required** |
| Host, DNS/TLS, database, secrets, monitoring owners | One named primary and backup contact for each responsibility. | **TBD - owner acceptance required** |
| Release, incident, support, data/compliance owners | Named people with reachable escalation paths. | **TBD - owner acceptance required** |
| Small-team approval model | Second-person review where available; documented owner-reviewed exception otherwise. | **TBD - owner acceptance required** |
| Hosting, PostgreSQL, region, environment boundaries | Separate production and rehearsal identities, data, DNS, secrets, and monitoring. | **TBD - owner acceptance required** |
| Access controls | Named MFA accounts, no shared human credentials, encrypted PostgreSQL connections, 90-day review, four-hour normal removal target. | **TBD - owner acceptance required** |
| RPO and RTO | RPO 1 hour; RTO 8 hours. | **TBD - owner acceptance required** |
| Backup and restore | Continuous PITR/equivalent, daily backup, 35 daily and 12 monthly points, quarterly restore test. | **TBD - owner acceptance required** |
| Backup encryption and off-site posture | Encryption in transit/at rest and a protected separate failure/access boundary. | **TBD - owner acceptance required** |
| Disaster and restore authority | Named declarer, restore approver, and restore operator. | **TBD - owner acceptance required** |
| Secret storage and rotation | Selected secret manager; 90-day long-lived credential rotation; immediate emergency revocation. | **TBD - owner acceptance required** |
| SLO and service/support hours | 99.0% monthly; 24x7 service; 08:00-18:00 local weekday support; no implied 24x7 responder. | **TBD - owner acceptance required** |
| Monitoring and capacity | 70% warning, 85% critical, backup/RPO, readiness, database, certificate, and provider alerts. | **TBD - owner acceptance required** |
| Incident model | Three severities; severity-1 authority/data events; explicit acknowledgement and communication targets. | **TBD - owner acceptance required** |
| Release and maintenance | Immutable digest; weekly two-hour window; 48-hour notice; pre-migration recovery point. | **TBD - owner acceptance required** |
| Rollback, restore, forward-fix decision | App rollback only with proven schema compatibility; otherwise authorized restore or tested forward fix. | **TBD - owner acceptance required** |
| Evidence retention and policy review | 13 months; annual and event-triggered review. | **TBD - owner acceptance required** |
| Real-data compliance gate | Unknown classification or legal/security obligation blocks real production. | **TBD - owner acceptance required** |

Acceptance record:

- Deployment owner name: **TBD - owner acceptance required**
- Deployment owner decision date: **TBD - owner acceptance required**
- Data/compliance owner name: **TBD - owner acceptance required**
- Approved exceptions and rationale: **TBD - owner acceptance required**
- Next scheduled review: **TBD - owner acceptance required**

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
waive the guardrails above or silently lower an accepted RPO/RTO, security,
authority, or real-data approval boundary. Such a request requires a new
bounded NW route and, where applicable, architecture or platform authority.

## 14. Successor And Acceptance Boundary

NW-064 remains `in_review` while any Section 12 selection is unanswered.
NW-065 remains blocked until this policy is owner-accepted.

After owner acceptance:

1. update this document to `accepted` with the selected values and named roles;
2. record the NW-064 acceptance separately without claiming implemented
   operational capability;
3. route the accepted values into NW-065 tooling and NW-066 procedures;
4. require NW-067 to measure RPO/RTO and exercise clean install, restore,
   upgrade/failure, rotation, alert/incident, and operator handoff.

Real production remains separately blocked by Section 11 even after NW-064
acceptance and a successful synthetic rehearsal.
