# Production Deployment And Operations Hardening Map

Status: accepted routing artifact

Source: NW-063

Date: 2026-06-13

Amended: 2026-06-14 by the owner-accepted NW-067 solo-owner gate amendment.
That policy amendment supersedes this artifact's original second-operator
handoff assumption while preserving all technical and evidence controls.

Authority: non-binding analysis under BAR-001 through BAR-015, BAR-104,
DEC-EVENT-01, DEC-AUTH-02, DEC-AUTH-03, DEC-AUTH-05, DEC-CONFIG-03,
DEC-CONFIG-08, DEC-PROJECTION-01, DEC-BOUNDARY-01, IDR-027, IDR-028,
IDR-029, and IDR-030.

## 1. Recommendation

The first reference target should be:

> One Linux application host running one immutable Datarun server container
> behind an external TLS reverse proxy, connected to an externally operated
> durable PostgreSQL 16 service.

The PostgreSQL service may be managed or self-operated, but it must be outside
the application container lifecycle and have independently tested backup,
restore, monitoring, and access controls. The reference remains provider
neutral and does not require Kubernetes.

This is the smallest honest target because it matches the current Java/Docker
packaging model, avoids introducing an orchestrator the repository does not
support, and leaves database durability with a service that can be backed up
and restored independently. It is a reference deployment class, not a claim
that the current Dockerfile or compose file is production ready.

The target can be selected without fixing deployment-specific RPO, RTO,
compliance, or staffing values. Those values block an executable production
runbook and rehearsal acceptance, not selection of the portable reference
class.

### Blocking Finding

A clean build that mirrors the current `server/` Docker context skips all Maven
resource directories under `../contracts`. The resulting JAR contains no
`patterns/*.json`, `pattern-definition.schema.json`, or platform payload
schemas under `shapes/`. `PatternRegistry` fails startup when no pattern
definitions exist, and `PlatformPayloadContractValidator` also requires the
shape resources.

Therefore:

- the current Dockerfile is not a verified deployable server image from a
  clean build context;
- `docker-compose.yml` is development scaffolding and is not valid production
  evidence;
- image/resource packaging must be corrected and tested before the reference
  runbook can be written as executable.

This corrects the broader NW-056 statement that Docker artifacts make the
server runnable: the source tree is locally Maven-runnable, but the clean
server-only image build omits required root contract resources.

## 2. Current Asset Inventory

| Surface | Current asset | Proven capability | Missing production procedure or control | Classification | Route |
|---|---|---|---|---|---|
| Server image | Multi-stage Temurin 17 Alpine `server/Dockerfile` | Maven can package the application; local root-context builds include contract resources. | Clean Docker context omits root contract resources; no non-root user, image provenance, health check, immutable tag/digest policy, or graceful lifecycle evidence. | Implementation/tooling | NW-065. |
| Runtime configuration | Environment-backed `application.properties` | Database, port, timezone, auth mode, OIDC/JWKS, and binding manifest settings exist. | Defaults are development-permissive: `dev-token`, default database credentials, and unauthenticated dev push enabled; no fail-closed production profile or environment validation. | Implementation/tooling | NW-065. |
| PostgreSQL | PostgreSQL JDBC, Flyway V1-V10, dev/test compose services | Runtime integration tests exercise PostgreSQL and Flyway migrations. | No supported production provisioning profile, TLS requirement, connection/capacity policy, backup schedule, restore procedure, replication posture, or major-version support statement. | Operational policy plus implementation/tooling | NW-064 selects policy; NW-065 tools the target. |
| TLS, DNS, proxy | None in repository | Server listens on configured HTTP port. | No TLS termination, DNS ownership, certificate renewal, proxy header policy, request-size/time-out policy, or internal port exposure rule. | Operational policy plus implementation/tooling | NW-064 and NW-065. |
| Secrets | Environment variables | Runtime can consume database and auth-provider settings. | No secret-store choice, least-privilege ownership, rotation cadence, leak response, startup validation, or prevention of secret material in compose/env evidence. | Operational policy plus implementation/tooling | NW-064 and NW-065. |
| OIDC/JWKS | `oidc-jwks` validator with issuer, audience, and JWKS URI | BAR-104 proves provider JWT validation and explicit principal binding. | No provider setup checklist, discovery/connectivity preflight, signing-key rotation drill, outage behavior, or operator-owned client/token flow. Mobile login and production web admin auth remain separate decisions. | Operational policy plus implementation/tooling | NW-064/NW-065 for server setup only; separate auth lanes for login/admin. |
| Principal bindings | Startup manifest runner and audited provisioner | NW-040 proves validation, idempotency, serialization, create/rotate/deactivate/rebind, and append-only audit. | No manifest template, review/approval policy, dry-run command, secret-safe mount convention, audit query, or rotation/recovery procedure. | Operational policy plus implementation/tooling | NW-064 and NW-065. |
| Config validation/publication | `DeployTimeValidator`, `ConfigPackager`, config package storage and delivery | BAR-010 proves validation, atomic publication, versions, and mobile promotion. | Publication is available through the development admin controller or direct service use; there is no production operator command/import workflow, approval process, or retained publication evidence. | Operational policy plus implementation/tooling | NW-064 and NW-065. GAP-CONFIG-02 remains visible. |
| Assignment bootstrap | `createInitialBootstrapAssignment(...)` | Tests prove one-time no-prior-assignment bootstrap semantics. | No production operator entry point invokes it; the dev bootstrap controller writes a different dev-only root assignment path. | Implementation/tooling | NW-065. Do not expose the dev controller. |
| Schema migration | Flyway runs at application startup | Tests repeatedly initialize current V1-V10 schema. | No preflight, migration backup, compatibility matrix, lock/failure response, deployment sequencing, or tested upgrade path. Flyway undo is not implemented. | Operational policy plus implementation/tooling | NW-064 and NW-065. |
| Rollback | Container image can in principle be replaced | No tested production rollback capability. | Application rollback is allowed only when schema compatibility is proven. Database rollback means restore of a consistent backup/PITR target; otherwise use forward fix. Do not invent down migrations. | Operational policy | NW-064, then runbook NW-066. |
| Backup/restore/DR | None in repository | PostgreSQL is the durable server store. | RPO/RTO, retention, encryption, off-site copy, restore validation, PITR, disaster ownership, and evidence are undefined and untested. | Operational policy | NW-064, then NW-066/NW-067. |
| Health/logs/metrics | Framework default logs only | Process startup and HTTP behavior are testable. | No Actuator dependency, liveness/readiness contract, metrics endpoint, structured log policy, correlation fields, alert rules, capacity thresholds, or SLO. | Operational policy plus implementation/tooling | NW-064 and NW-065. |
| Incident/support | None | Existing tests identify technical invariants. | No severity model, on-call/support owner, escalation, communication, evidence retention, recovery authority, or post-incident route. | Operational policy | NW-064. |
| Field-device onboarding | `/api/auth/me`, config delivery, sync, actor partitions | Accepted auth, config, sync, and shared-device kernel behavior exists. | No production token acquisition UX, device enrollment checklist, sync smoke test, connectivity troubleshooting, or device-loss recovery promise. | Operational procedure plus separate future decisions | NW-066 for current bearer-token smoke test; mobile login/admin auth and NW-054 remain separate. |
| CI/release | GitHub Actions server tests against PostgreSQL | Server code/contracts are tested on push/PR paths. | CI does not build, inspect, start, or vulnerability-scan the server image; no release artifact, signing, SBOM, promotion, or deployment approval path. | Operational policy plus implementation/tooling | NW-064 and NW-065. |

## 3. Reference Deployment Options

| Option | Prerequisites | Operator burden and recovery | Portability and observability | Unsupported assumptions | Required repository work |
|---|---|---|---|---|---|
| Single application host, external TLS, durable external PostgreSQL | Linux/container runtime; DNS; reverse proxy; PostgreSQL 16 endpoint; secret store; backup service. | Lowest new platform burden. App recovery is image redeploy; database recovery is provider/self-operated restore. Host remains an application availability boundary. | High portability. Standard container/log/metrics integration is sufficient. | Does not provide automatic multi-host failover or zero downtime. External PostgreSQL operations must be real, not a named Docker volume treated as DR. | Fix image resources; harden image/profile; add health/metrics; add deployment/provisioning tooling; write and rehearse runbook. |
| Kubernetes | Cluster, ingress, cert management, registry, secrets, storage/database operator or external DB, monitoring stack, skilled operators. | Highest burden. Can add rescheduling and rollout controls, but database and migration recovery still require explicit design. | Portable only across Kubernetes-compatible environments; significantly larger observability and policy surface. | No manifests, charts, probes, resource sizing, disruption policy, or Kubernetes operations evidence exist. Orchestration does not create application correctness or backup safety. | All single-host work plus charts/manifests, cluster policy, probes, resource limits, rollout strategy, and cluster rehearsal. |
| Managed application runtime and managed database | Selected provider, app/container service, managed PostgreSQL, managed TLS/secrets/logging, provider IAM. | Potentially low day-two burden but high provider dependency. Recovery quality depends on selected service tier and tested export/restore. | Lowest portability unless the app remains a plain OCI image and provider features stay optional. Observability is provider-specific. | No provider, region, compliance profile, cost envelope, or service constraints are selected. Some managed runtimes constrain startup jobs, filesystem mounts, or long-lived connections. | Fix image/resources and runtime profile first; then add provider-specific deployment adapter and evidence. |

### Selection

Select the single application host with external TLS and durable external
PostgreSQL as the first reference. Kubernetes and a fully managed application
runtime remain unselected alternatives. A deployment may later map the same
image and policies to those targets, but NW-063 creates no work for them.

## 4. Decision And Classification Register

| Unresolved item | Classification | Owner | Input needed | Durable output | Blocks first rehearsal |
|---|---|---|---|---|---|
| Hosting/account/network owner, DNS, TLS, database operator, and environment boundaries | Operational policy | Deployment owner | Named roles, environments, provider constraints, maintenance access. | `docs/operations/policies/first-reference-deployment-policy.md` | Yes. |
| RPO, RTO, backup retention, encryption, restore authority, and disaster declaration | Operational policy | Deployment owner/data owner | Data criticality, tolerated loss/downtime, legal and cost constraints. | Same policy | Yes. |
| Secret storage, access, rotation, revocation, and evidence handling | Operational policy | Deployment owner/security owner | Available secret manager and incident requirements. | Same policy | Yes. |
| SLO, capacity thresholds, alert ownership, incident severity, support escalation, and evidence retention | Operational policy | Service owner | User hours, support staffing, expected load, communication obligations. | Same policy | Yes. |
| Release approval, maintenance windows, migration backup, forward-fix versus restore authority | Operational policy | Service owner/database owner | Change-control expectations and downtime tolerance. | Same policy | Yes. |
| Clean image includes all canonical contract resources | Implementation/tooling | Platform engineering | Root build context and artifact packaging approach. | Code, Docker/build assets, image test evidence under NW-065. | Yes. |
| Fail-closed production configuration and startup validation | Implementation/tooling | Platform engineering | Required environment list and secret injection mechanism from NW-064. | Code/configuration/tests under NW-065. | Yes. |
| Health/readiness/metrics and release-image verification | Implementation/tooling | Platform engineering/operations | Policy thresholds and chosen monitoring adapter. | Code, tests, CI/deployment assets under NW-065. | Yes. |
| Production-safe config publication and initial assignment bootstrap | Implementation/tooling | Platform engineering | Reviewed config source, bootstrap actor/role/scope, external operator identity/evidence. | One-shot deployment tooling and tests under NW-065. | Yes. |
| Supported compatibility envelope for app image, PostgreSQL major, schema version, proxy, and provisioning inputs | Platform-spec detail | Platform engineering | Results from NW-065 implementation and upgrade tests. | A stable compatibility section in the indexed runbook or a successor `docs/specifications/platform/` document if behavior extends beyond procedure. | Yes. |
| First real deployment compliance and data-classification constraints | Product/problem evidence | Deployment/product owner | Jurisdiction, organization policy, data categories, field conditions. | Scenario/user-fit or deployment validation note; policy references accepted facts. | No for synthetic rehearsal; yes for real production approval. |
| Mobile OAuth/OIDC acquisition and refresh/logout lifecycle | Product/platform decision outside this lane | Product/auth owner | Selected mobile login flow and provider behavior. | Separate auth decision/implementation route. | No for bearer-token technical rehearsal; yes for turnkey mobile production. |
| Production web admin authentication and admin authority | Architecture/product decision outside this lane | Auth architecture/product owner | Admin journeys, command authority, audit and provider integration. | Separate decision route. | No for deployment-managed provisioning; yes for production web admin use. |
| Device expiry, decommissioning, sealed recovery, local encryption, redaction, and token retention | NW-054/BAR-106 | Security/platform owner | Security and retention decision. | NW-054 durable decision outputs. | No for server deployment rehearsal; yes for stronger device-security claims. |

No new architecture decision is required for the selected reference target.
Escalate only if implementation proposes new authority, contracts, event
semantics, sync/access behavior, configuration boundaries, or data-erasure
semantics.

## 5. Runbook Outline

NW-066 must produce one indexed
`docs/operations/runbooks/production-deployment-runbook.md` covering:

1. Supported target, versions, owners, and linked policy.
2. Release artifact identity, checksum/signature, contract-resource inspection,
   and environment prerequisites.
3. DNS, TLS reverse proxy, firewall, PostgreSQL TLS, database role, and secret
   injection.
4. Environment validation with explicit rejection of development auth/defaults.
5. Clean database creation, Flyway validation/migration, and migration evidence.
6. OIDC/JWKS connectivity and issuer/audience verification.
7. Principal-binding manifest validation, application, idempotency check, and
   audit query.
8. Config import/validation/publication and retained package-version evidence.
9. One-time initial assignment bootstrap and subsequent command-capability
   verification.
10. Field-device `/api/auth/me`, config, push/pull, and pending/freshness smoke
    test using non-sensitive rehearsal data.
11. Logs, health, metrics, alerts, dashboards, capacity checks, and SLO signals.
12. Backup creation, restore into a clean environment, integrity checks, and
    RPO/RTO measurement.
13. Application/schema upgrade, compatibility check, maintenance decision,
    and post-upgrade smoke test.
14. Failed startup or migration response, application rollback eligibility,
    database restore criteria, and forward-fix path.
15. Database credential, binding manifest, provider signing key/JWKS, and
    application secret rotation.
16. Incident triage, evidence capture, escalation, communication, and solo
    cold recovery.
17. Cleanup, access revocation, evidence retention, and follow-up NW routing.

Every section must state prerequisites, exact procedure, expected observables,
stop conditions, recovery posture, and retained evidence.

## 6. Rehearsal Matrix

| Rehearsal | Prerequisites and procedure shape | Expected result and retained evidence | Failure stop condition | Cleanup |
|---|---|---|---|---|
| Clean install | Approved policy, release image, empty database, DNS/TLS test name. Execute environment preflight, database create, migration, app start. | Image digest, preflight output, Flyway version/history, healthy endpoint, TLS result, startup logs. | Missing contract resource, dev auth/default secret, migration error, unhealthy app, or public direct app port. | Destroy synthetic environment or retain as upgrade source. |
| Initial migration | Empty PostgreSQL 16 database and migration backup marker. Run only the supported migration path. | V1-V10 applied once; schema history clean; app starts. | Validation checksum mismatch, partial failure, unexpected DDL, or lock timeout. | Preserve failed DB snapshot; do not edit schema manually. |
| TLS/provider/secrets | Valid certificate, provider test tenant, secret store entries. Validate TLS chain, issuer/audience/JWKS, and secret access. | External TLS only; provider token validation succeeds; secrets absent from logs/evidence. | Plaintext exposure, wrong issuer/audience, unverifiable JWKS, or leaked secret. | Revoke test credentials and remove temporary DNS. |
| Binding manifest | Reviewed synthetic actor/principal manifest. Validate, apply, reapply, and query audit. | One active binding, idempotent reapply, append-only audit evidence. | Partial apply, ambiguous mapping, unaudited change, or group/claim authority. | Deactivate rehearsal binding through a reviewed manifest. |
| Config and assignment bootstrap | Reviewed bounded config and initial actor/scope. Run production tooling, not `/admin`. | Valid package version published; one initial assignment event exists; later admin command checks use accepted capability and containment. | Dev controller required, invalid config publishes, duplicate bootstrap, or authority inferred from request/provider claims. | End synthetic assignments through authorized path where appropriate. |
| Device sync smoke | Rehearsal bearer token bound to assigned actor and test device. Run auth-me, config, local capture, push, pull. | Server-resolved actor matches binding; config version arrives; append-only event syncs; freshness/pending behavior is honest. | Actor mismatch, unscoped data, watermark rewrite, or data loss. | Revoke token/binding and remove synthetic data only by environment teardown. |
| Backup and clean restore | Known dataset, policy-approved backup method, second empty environment. Back up, restore, start same image, inspect counts and smoke paths. | Restore completes within measured RTO and to expected RPO; events, bindings, config, assignments, and schema history are consistent. | Incomplete event history, invalid schema history, missing audit/config data, or exceeded accepted limits. | Destroy restored rehearsal environment after evidence retention. |
| Application/schema upgrade | Previous accepted image/database, new candidate, pre-upgrade backup. Run preflight, migrate, deploy, and smoke tests. | Upgrade completes; old events/config remain interpretable; current auth/sync/config paths work. | Migration failure, incompatible rollback assumption, projection/contract drift, or failed smoke test. | Follow policy: eligible app rollback, database restore, or forward fix. |
| Failed deployment/migration | Controlled bad config/image or interrupted migration in disposable environment. Exercise detection and escalation. | Failure is detected before traffic; evidence identifies stage; no manual schema mutation occurs. | Operators cannot identify safe stop state or recovery authority. | Restore disposable DB or rebuild environment. |
| Credential/JWKS rotation | Two valid credentials/keys and reviewed rotation sequence. Rotate database/app secret or provider signing key without changing platform authority. | New credential/key accepted; old one rejected at intended boundary; binding remains explicit. | Outage exceeds policy, old secret remains active unexpectedly, or claims become authority. | Revoke rehearsal credentials and retain timing/evidence. |
| Alert and incident triage | Working health/metrics/log pipeline and injected app/database/provider failure. | Alert reaches owner; runbook locates cause, captures evidence, escalates, and records recovery decision. | No alert, false healthy state, secret leakage, or unclear owner. | Remove inject, confirm recovery, close rehearsal incident. |
| Solo cold recovery | Hamza closes the active session and starts a fresh privileged session using only indexed policy/runbook, approved access, execution record, and evidence index. | Hamza reconstructs state from retained evidence, executes smoke checks, identifies the recovery point, and names stop/escalation authority without hidden session state. | Undocumented knowledge, inaccessible approved access, hidden prior-session state, or unclear recovery authority is required. | Close the temporary session, retain evidence, and record runbook corrections. |

NW-067 must create the reusable rehearsal plan plus a dated rehearsal record.
A pass requires clean install, restore, upgrade, failure response, rotation,
alert, and solo cold-recovery evidence. Partial execution cannot strengthen the
production readiness claim or prove independent human continuity.

## 7. Successor Sequence

| ID | Outcome | Primary classification | Durable output/evidence | Status and dependency | Stop condition |
|---|---|---|---|---|---|
| NW-064 | Define first reference deployment operations policy | Operational policy | `docs/operations/policies/first-reference-deployment-policy.md`, indexed and owner-accepted. | `ready`; depends on NW-063. | Stop if RPO/RTO, ownership, secret handling, support, or release authority cannot be assigned. |
| NW-065 | Implement reference deployment and provisioning tooling | Implementation/tooling | Clean image and resource test; fail-closed production profile; health/metrics; reference deployment assets; safe binding/config/assignment provisioning; focused and full evidence. | `blocked` until NW-064 accepted. | Stop if tooling requires new authority, online admin APIs, contract changes, or unsupported rollback claims. |
| NW-066 | Write the production deployment runbook | Operational procedure | `docs/operations/runbooks/production-deployment-runbook.md` and `docs/operations/rehearsals/production-deployment-rehearsal-plan.md`, indexed and reviewed against actual tooling. | `blocked` until NW-064 and NW-065 accepted. | Stop if any required step depends on dev admin surfaces, undocumented manual database writes, or unimplemented recovery. |
| NW-067 | Execute production deployment and recovery rehearsal | Operational rehearsal | `docs/operations/rehearsals/YYYY-MM-DD-production-deployment-reference-environment.md` plus retained command/log/timing evidence and runbook corrections. | `blocked` until NW-066 accepted. | Stop and fail the rehearsal on contract-resource omission, unsafe migration state, failed restore, authority drift, secret leakage, or unmet accepted RPO/RTO. |

The stronger claim after NW-067 is limited to:

> The named reference deployment is repeatably deployable and recoverable under
> the accepted policy and rehearsed procedure.

It does not imply production web admin auth, mobile OAuth/OIDC login, device
retention/security closure, reporting, multi-host availability, Kubernetes
support, or turnkey product readiness.

## 8. Direct Answers

1. The smallest honest reference is one application host, external TLS, and
   durable external PostgreSQL.
2. Hosting ownership, RPO/RTO, backup retention, secrets, monitoring/SLO,
   support, incident, release, and evidence policies must be accepted first.
3. Current OIDC/JWKS validation, explicit principal-binding application,
   Flyway forward migration, config validation/package semantics, assignment
   event semantics, and bearer-bound sync can be reused unchanged.
4. Image contract-resource packaging, production defaults, health/metrics,
   release-image verification, config publication tooling, and assignment
   bootstrap tooling require implementation.
5. RPO/RTO, secret manager and rotation, support hours, SLO/alerts, ownership,
   maintenance windows, and recovery authority are deployment choices, not
   platform semantics.
6. A stronger claim requires a clean build/install, tested backup restore,
   tested upgrade/failure path, rotation, alert, handoff, measured RPO/RTO,
   retained evidence, and no unresolved blocking rehearsal failure.
7. Device expiry/decommissioning/encryption routes to NW-054. Production web
   admin auth and mobile provider login remain separate auth decisions.

## 9. Verification Ledger

### Files inspected

- Active routing, backlog, BAR, decision anchors, gap playbook, NW-056,
  first-deployment summary/checkpoint, documentation and commit standards.
- `server/Dockerfile`, both compose files, `server/pom.xml`,
  `application.properties`, server CI, migrations V1-V10.
- `PatternRegistry`, `PlatformPayloadContractValidator`,
  `PrincipalBindingManifestProvisioner`, `PrincipalBindingProvisioningRunner`,
  `AuthProperties`, `AssignmentService`, `ConfigPackager`, config/admin
  controllers, and `WebConfig`.

### Commands and observations

- `rg`, `sed`, `find`, and `git` inspection commands were used to trace assets,
  authority boundaries, environment properties, and bootstrap entry points.
- A clean isolated copy containing only the Dockerfile-visible server inputs
  ran `./mvnw package -DskipTests -B --no-transfer-progress`.
  Maven reported all `../contracts` resource directories missing, built
  successfully, and the JAR inspection found none of the required pattern or
  platform-shape resources.
- A direct Docker build was started to verify the image path, but stopped while
  downloading the large JDK base layer. The isolated clean Maven build proves
  the relevant Docker-context resource omission without changing the repo.
- No runtime test suite was rerun because NW-063 changes documentation and
  routing only.

### Assumptions

- PostgreSQL 16 is the first supported rehearsal major because CI and current
  dev/test assets use PostgreSQL 16 variants. NW-065/NW-066 must make the
  compatibility claim explicit from executed evidence.
- The first rehearsal uses synthetic/non-sensitive data and a technical bearer
  token path. It does not claim mobile OAuth/OIDC product readiness.
- Managed PostgreSQL is preferred operationally when available, but the
  reference runbook must remain valid for a self-operated durable PostgreSQL
  service that meets the same accepted policy and rehearsal evidence.

### Drift found

- NW-056's general Docker deployability statement is too strong for a clean
  `server/` image build because required root contract resources are omitted.
- Historical implementation planning mentions Kubernetes for production, but
  no active manifests, accepted target, or operations evidence exists.
- Config publication and initial assignment bootstrap have accepted internal
  semantics but no production operator entry point.

### Unresolved decisions

- NW-064 must set concrete policy values and owners.
- NW-065 must prove the corrected image and provisioning tooling.
- NW-067 must measure whether the accepted RPO/RTO and operational controls are
  actually met.
