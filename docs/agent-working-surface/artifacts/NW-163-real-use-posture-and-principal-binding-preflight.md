# NW-163 Real-Use Posture And Principal-Binding Preflight

Status: accepted
Date: 2026-06-26
Type: non-authoritative routing / implementation-preflight note
Source: owner command for NW-163; NW-155 slice 6; NW-158 behavior-slice gate

## Standing

NW-163 corrects active wording that made unselected work read like a global
prohibition. Repository documents are evidence of standing, not legislation.
They do not create separate departments, external approvers, cloud hosting,
cross-border transfer, or a legacy account-import prerequisite.

Hamza is the initial owner, operator, developer, administrator, support
contact, and decision-maker. A written Hamza decision is enough owner approval
unless a specific applicable external obligation is identified. Application
roles such as worker, supervisor, and reviewer do not imply separate employees.

## Wording Corrected

| Surface | Correction |
|---|---|
| `docs/status.md` Current Routing | Replaced the broad "blocked until selected" posture with a current real-use posture: unselected work is not selected, but independent technical preparation can continue. Any blocker must name the exact affected action and evidence. |
| `docs/status.md` Recommended next move | Replaced the NW-163 docs-only successor with NW-164 as the selected bounded implementation route for fresh local OIDC/Keycloak user access and explicit principal binding. |
| `docs/agent-working-surface/platform-next-work-backlog.md` status definition | Tightened `blocked` so it applies only when a specific technical capability is absent, Hamza stopped the work, or a concrete external obligation applies. |
| `docs/agent-working-surface/platform-next-work-backlog.md` Active Work Index and backlog rows | Marked NW-163 accepted and added NW-164 as the next selected implementation route. NW-159 remains a candidate only for future blocked-by-default wording recurrence. |
| `deploy/reference/README.md` | Clarified that companion services can run locally/on-prem on Hamza's existing server as isolated services or VMs when validated, and that optional external/cloud/provider topology is not a prerequisite. |
| `deploy/reference/provisioning-inputs.md` | Changed the example binding reason from approval ceremony language to owner-selected binding language. |
| `deploy/reference/pilot-packages/stock-operations/README.md` | Replaced "separately approved" local Keycloak wording with "selected" route wording, and changed package language from approving real use to not selecting it. |

Historical artifacts that say "blocked" remain dated evidence. Current routing
uses the NW-163 rule: say exactly which action cannot proceed, why it is
impossible, and what evidence supports that statement.

## Owner And Hosting Facts

- The current pilot and initial production deployment target is the
  SSH-operated lab server Hamza uses for operations. In this context "lab
  server" means the intended production host for the selected local/on-prem
  deployment, not a separate test-only environment.
- When logical separation is needed, use local VMs or isolated services on that
  server before treating a separate physical host, cloud service, managed
  identity provider, managed database, external monitoring service, remote
  support platform, or paid backup service as necessary.
- Logging, monitoring, identity, database, application, and support facilities
  default to local/self-hosted deployment unless a later route selects
  otherwise.
- Initial backups may remain on the same physical server. That protects
  against some application, database, and VM failures, but it is not disaster
  recovery for total host loss.
- Moving services and backups to separate physical infrastructure is a future
  reliability improvement, not a prerequisite for feature implementation,
  technical preparation, pilot use, or initial owner-approved production use.
- Hamza provides support and administration directly. No separate support
  organization, office, or remote support provider is required.
- The domain and required subdomains are available, including `app.nmcpye.org`
  and other `*.nmcpye.org` names. `www.nmcpye.org` is unavailable.

## Current Implementation Evidence

| Capability | Evidence |
|---|---|
| OIDC/JWKS bearer validation exists | `server/src/main/java/dev/datarun/server/authorization/OidcJwksTokenValidator.java` validates issuer, audience, JWKS signature, subject, expiry, not-before, and login nonce. |
| Auth mode can use OIDC/JWKS | `server/src/main/resources/application.properties` exposes `datarun.auth.mode=oidc-jwks`, issuer, audience, and JWKS URI settings. |
| Explicit principal binding exists | `AuthenticatedActorResolver` resolves OIDC/JWKS principals only through `AuthPrincipalBindingRepository.resolveActor(issuer, subject)` before returning an actor. |
| Binding storage and operation audit exist | Migrations V8/V9 create `auth_principal_bindings` and `auth_principal_binding_operations`; `PrincipalBindingManifestProvisioner` supports active/inactive operations, idempotency, rebind, and audit. |
| One-shot provisioning exists | `OneShotProvisioningService` supports the `principal-bindings` command with operator/evidence identity and input hashing. |
| Web-admin OIDC login exists | `WebAdminSessionController` and `WebAdminSessionService` implement authorization-code login, callback, server session, CSRF-protected probe, logout, and session revalidation. |
| Mobile fresh OIDC activation exists | Accepted NW-085/NW-101 evidence and mobile auth tests cover OIDC/PKCE handoff followed by `/api/auth/me` actor activation. |
| Synthetic pilot principal-binding package exists | `deploy/reference/pilot-packages/stock-operations/principal-bindings.synthetic.json` maps three synthetic local IdP subjects to fixed pilot actor UUIDs. |

Test evidence includes `ProductionAuthIntegrationTest`,
`WebAdminSessionBoundaryTest`, `OneShotProvisioningIntegrationTest`,
`StockOperationsPilotPackageIntegrationTest`, and mobile OIDC/auth service
tests. Those tests show mapped OIDC principals become actors, groups/claims and
JWT `actor_id` are not authority, actor mismatch is rejected, existing
sessions are denied when bindings change, and provisioning is idempotent and
audited.

## Fresh Local OIDC Path

Fresh local OIDC provisioning can enable users without legacy account import:

1. Run a local/self-hosted OIDC provider such as Keycloak on the existing
   server or a local VM/service.
2. Create a local realm/client/user set for the selected pilot users.
3. Configure Datarun with the local issuer, audience, JWKS URI, web-admin
   authorization/token/redirect URIs, and client credentials.
4. Apply reviewed principal-binding input mapping each local provider
   `(issuer, subject)` to the intended Datarun `actor_id`.
5. Apply the already accepted config/admin capability and assignment setup
   needed for those actors.
6. Validate that `/web-admin/login` and mobile OIDC plus `/api/auth/me` resolve
   the bound actor and do not use IdP claims, groups, roles, or JWT `actor_id`
   as authority.

## Missing Concrete Implementation

The missing next implementation is not the auth mechanism. It is a concrete
local Keycloak/fresh-user preflight package:

- local Keycloak realm/client/user setup material or equivalent self-hosted
  OIDC setup;
- Datarun runtime and web-admin/mobile settings for that local issuer;
- reviewed principal-binding manifest using the local issuer and real local
  subject values for fresh users;
- a focused preflight/smoke proving a bound fresh local principal resolves to
  the expected actor through accepted auth paths.

No code/test evidence shows that legacy account import, submitted-record
import/replay, cloud hosting, cross-border transfer, managed identity, separate
departments, or a generic approval package is required for that next step.

## Legacy Account Import

Legacy account import is a separate feature. It remains unselected in NW-163
and is not needed for fresh local OIDC users. Legacy password/hash migration,
legacy account CSV handling, and imported account binding should not enter
NW-164 unless Hamza selects that feature in a separate route.

## Risks And Future Options

| Topic | Standing |
|---|---|
| Real users/data | Not selected by NW-163. A later Hamza decision can select a narrow first use directly; if an external obligation is identified, name it and the affected action. |
| Production cutover | Not selected by NW-163 or NW-164. Technical preparation and local auth preflight can proceed independently. |
| Same-host backups | Allowed for initial use with the limitation recorded: they do not protect against total physical-host loss. |
| Separate infrastructure | Future reliability improvement, not a prerequisite unless capacity, security, recovery, or incompatibility evidence shows the existing server/local VM cannot satisfy a named operation. |
| Managed/cloud identity | Optional future route. Do not make it a blocker for local Keycloak/fresh-user preparation. |
| Cross-border transfer | Not selected. Local/on-prem deployment remains the default. |
| Gap register/future decisions | Routable when triggered, not forbidden by default. |

## Selected Next Route

NW-164 is selected as the smallest executable implementation route.

Behavior slice: slice 6, explicit principal binding/login path.
Fixture role: local OIDC users and stock-pilot package values are pilot
fixtures, not product authority.
Accepted boundary: NW-070 production auth/principal binding, NW-079/NW-086
web-admin OIDC session boundary, NW-085/NW-101 mobile OIDC activation, and
existing one-shot provisioning.
Non-goals: legacy account import, submitted-record import/replay, real-data
cutover, production cutover, cloud/cross-border/managed-provider dependency,
IdP claim/group authority, CDL, BAR, gap register, contracts, accepted specs,
broad reporting/import-export, stock ledger correctness, and review workflow.
Validation gate: prove a fresh local provider subject is mapped through
explicit binding and resolves to the intended actor through the accepted auth
paths.

No owner question is required before NW-164. Existing owner facts are enough to
scope the next implementation.
