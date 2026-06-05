# NW-056 Agent Prompt: Product Standing And Production Readiness Map

You are working in `/home/hamza/datarun-platform`.

## Goal

Create a product-facing standing map for Datarun after the accepted post-Phase-4 platform baseline.

Exit target:

```text
Datarun has an actionable product-readiness artifact that separates accepted platform/kernel capability, runnable scenario evidence, production deployment readiness, web admin/config UX readiness, mobile UX readiness, login/provider readiness, and deferred/future-decision surfaces.
```

This is analysis and routing work. Do not implement UI, mobile login, Keycloak, deployment automation, APIs, schemas, or runtime behavior in this slice.

## Core Questions

Answer these directly:

1. What is Datarun today as a product, not only as a platform kernel?
2. Which features and scenarios can be run end to end with current code, and at what evidence level?
3. Is a real production deployment possible now, and under what operational constraints?
4. Can work start now on web admin/config UX, mobile UX/vocabulary, mobile login, provider/Keycloak integration, and product polish?
5. What must be routed first so product/UI work does not smuggle in new authority, scope, state, report, or identity-provider semantics?

## Files To Read

Read only this packet by default:

1. `AGENTS.md`
2. `docs/status.md` Current Routing section only.
3. `docs/agent-working-surface/README.md`
4. `docs/agent-working-surface/baseline-acceptance-register.md`
5. `docs/agent-working-surface/platform-next-work-backlog.md`
   - Read rows NW-044 through NW-056, plus any row named by Current Routing.
6. `docs/agent-working-surface/operational-ux-layering-companion.md`
7. `docs/implementation/module-interfaces.md`
   - Read `Authority role`, `Event Store`, `Projection Engine`, `Conflict Detector`, `Scope Resolver`, `Assignment Admin Capability Policy`, `Authenticated Actor Resolver`, `Sync Surfaces`, `Shape Registry`, `Config Packager`, `Mobile Actor Session And Local Store`, `Trigger Engine`, and `Command Validator`.
8. Product intent and pressure:
   - `docs/README.md`
   - `docs/constraints.md`
   - `docs/scenarios/README.md`
   - `docs/access-control-scenario.md`
9. Scenario evidence sources:
   - `docs/scenarios/00-basic-structured-capture.md`
   - `docs/scenarios/19-offline-capture-and-sync.md`
   - `docs/scenarios/21-chv-supervisor-operations.md`
   - `docs/scenarios/22-coordinated-distribution-campaign-across-grouped-locations.md`
   - `docs/scenarios/23-configure-new-operational-activity.md`
   - `docs/scenarios/24-long-running-deployment-data-lifecycle.md`
   - `docs/scenarios/25-worker-onboarding-transfer-and-exit.md`
   - `docs/scenarios/26-operational-reporting-and-aggregate-oversight.md`
   - `docs/scenarios/27-logistics-distribution-composite.md`
10. Contracts:
    - `contracts/sync-protocol.md`
    - `contracts/config-package.schema.json`
    - `contracts/shape-format.schema.json`
    - `contracts/flag-catalog.md`
    - `contracts/pattern-definition.schema.json`
11. Decisions:
    - `docs/decisions/idr-027-production-auth-principal-actor-binding.md`
    - `docs/decisions/idr-028-production-principal-binding-administration.md`
    - `docs/decisions/idr-029-assignment-admin-command-capability.md`
    - `docs/decisions/idr-030-shared-device-session-lifecycle.md`
12. Focused code inspection:
    - `server/src/main/java/dev/datarun/server/admin/AdminController.java`
    - `server/src/main/java/dev/datarun/server/config/ConfigAdminController.java`
    - `server/src/main/java/dev/datarun/server/authorization/WebConfig.java`
    - `server/src/main/java/dev/datarun/server/authorization/ActorTokenInterceptor.java`
    - `server/src/main/resources/application.properties`
    - `docker-compose.yml`
    - `server/Dockerfile`
    - `mobile/lib/presentation/screens/setup_screen.dart`
    - `mobile/lib/presentation/screens/work_list_screen.dart`
    - `mobile/lib/presentation/screens/form_screen.dart`
    - `mobile/lib/presentation/widgets/sync_panel.dart`
    - `mobile/lib/data/device_identity.dart`
    - `mobile/lib/data/sync_service.dart`
13. Use `scripts/query_cdl.py` only for the exact CDL rows needed to verify authority, event truth, auth, sync, config, scope, projection, retention, and workflow claims.

Do not read broad architecture/history docs unless a directly read source routes you there.

## Required Analysis Format

Create:

```text
docs/agent-working-surface/artifacts/NW-056-product-standing-and-production-readiness-map.md
```

The artifact must use this structure.

### 1. Executive Product Standing

State the current product standing in 5-8 bullets using clear status terms:

- `production_kernel_accepted`
- `scenario_runtime_evidenced`
- `operator_deployable_with_constraints`
- `product_surface_partial`
- `blocked_by_future_decision`
- `not_started`

Define the terms if you introduce any others.

### 2. Capability Matrix

Use a compact table:

```text
Capability | User-facing meaning | Current backing | Evidence | Product readiness | Missing before productization | Route
```

Cover at least:

- structured capture and correction;
- offline sync and scope-filtered pull;
- subject history and handoff context;
- assignment create/end and assignment-admin command capability;
- conflict/flag review and designated resolver behavior;
- deployer shapes, activities, expressions, severity, config publishing, and config delivery;
- platform workflow patterns and projection;
- reporting/aggregate oversight;
- production OIDC/JWT/Keycloak authority;
- principal-binding provisioning;
- shared-device actor sessions;
- mobile setup, sync, work list, form capture, advisory warnings;
- web admin subject/flag/assignment/location/config surfaces;
- deployment packaging and operations.

### 3. Runnable Scenario Map

For each scenario with accepted runtime evidence, state what can run today and what remains product/UI work:

- S00 structured capture;
- S19 offline/stale authority;
- S21 supervisor review;
- S22 coordinated distribution campaign;
- S23 setup/config;
- S26 reporting/aggregate oversight;
- S27 logistics transfer.

Also classify S24/S25 as current pressure for retention/security and worker lifecycle UX, without claiming implementation beyond accepted rows.

### 4. Production Deployment Readiness

Answer whether a real production deployment is possible now.

Separate:

- platform/kernel readiness;
- deployment manifest/provisioning readiness;
- admin/operator readiness;
- IdP/Keycloak integration readiness;
- mobile login/session readiness;
- data retention/security readiness;
- observability/backup/upgrade/readiness;
- support/runbook readiness.

Use `yes`, `yes_with_constraints`, `no`, or `needs_decision`, with concrete constraints.

### 5. UI/UX Start Decision

For each product surface, answer `start_now`, `start_after_NW_056_output`, `needs_decision_first`, or `defer`.

Include:

- web admin/config UX;
- production admin authentication for web admin;
- online production principal-binding admin API/UI;
- mobile login via OIDC/Keycloak;
- mobile shared-device actor switch UX;
- mobile operational vocabulary and navigation polish;
- reporting/dashboard UX;
- conflict review/batch handling UX;
- device retention/sealed partition recovery UX.

### 6. Vocabulary And Information Architecture

Apply `operational-ux-layering-companion.md`.

Map product terms to safe backing constructs. Explicitly prevent product terms such as campaign, handoff, progress, pending review, blocked, route, worker, provider, Keycloak group, or admin from becoming platform authority.

### 7. Product Gaps And Successor Routes

Produce 5-10 recommended successor rows. For each:

```text
Title:
Type:
Priority:
Depends on:
Expected artifact:
Why now:
Stop condition:
```

Only add backlog rows/prompts for successors that are clearly ready. Otherwise list recommendations inside the artifact.

### 8. Verification Ledger

Record:

- source files inspected;
- commands run;
- test evidence reused from BAR/NW rows;
- tests not rerun and why;
- any drift or uncertainty.

## Guardrails

- Do not mark a product surface production-ready just because the underlying kernel is accepted.
- Do not demote accepted BAR rows because UI/product polish is missing.
- Do not claim Keycloak groups, roles, resource claims, or JWT `actor_id` as authority.
- Do not turn mobile login UX into actor authority; the server-resolved actor remains authoritative.
- Do not turn web admin labels into event types, scope types, flag categories, workflow state, or config mechanisms.
- Do not promote online principal-binding admin APIs, IdP group/claim authority, reporting warehouse/API, import/export, field-level sensitivity/encryption/redaction, sealed-partition recovery, entity lifecycle, trigger execution, auto-resolution, resolver reassignment, new scope mechanisms, new envelope fields, or new event types.
- Do not use `docker-compose.yml` as proof of production hardening.
- Do not treat the development HTML admin console as production-admin authentication.

## Backlog And Status Updates

If the artifact lands cleanly:

- Mark NW-056 `accepted`.
- Update this backlog row with the artifact path and any accepted successor rows.
- Update `docs/status.md` Current Routing only if the artifact materially changes the recommended next slice.

If the analysis discovers that product work is blocked by a missing security/platform decision:

- Leave the blocked successor as `blocked` or `future_decision`.
- Name the exact missing decision and affected BAR/CDL rows.

## Verification

Run:

```bash
git diff --check
```

Inspect any Markdown tables you add.

## Commit Boundary

Use one commit if the slice lands cleanly:

```text
docs(product): map product standing and production readiness
```

## Stop And Report

Stop if the requested product analysis requires changing runtime behavior, adding a process-boundary contract, selecting a new authority model, or claiming production readiness that the current BAR/backlog evidence does not support.
