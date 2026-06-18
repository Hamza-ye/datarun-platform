# NW-100 Agent Prompt: Add Admin Command Capability Provisioning Seed

You are working in `/home/hamza/datarun-platform`.

## Current Standing

NW-086 and NW-087 are accepted. `/web-admin` has a production browser
login/session boundary, and `/web-admin/shell` now requires `web_admin.access`
from the server-side `admin_command_capabilities` deployment policy.

That policy intentionally was not seeded by NW-087. Without a reviewed seed
path, the shell is correctly fail-closed but not operationally usable through
the accepted one-shot provisioning workflow.

NW-088 S23 config setup workflow should not start from an unseeded shell. This
slice closes only the reviewed seed path, then NW-088 can be promoted
separately.

## Goal

Extend the existing deployment-reviewed one-shot config publication input so it
can seed the server-side `admin_command_capabilities` policy.

Exit target:

```text
The reviewed `config-publish` provisioning manifest requires and applies
`admin_command_capabilities` transactionally alongside existing deployment
config rows. Exact reapply is idempotent; malformed or unknown command policy
fails closed without partial publication. The policy remains server-side
deployment config and is not delivered in config packages or made mobile-visible.
```

## Files To Read

Read only this packet by default:

1. `AGENTS.md`
2. `docs/status.md` Current Routing
3. `docs/commit-workflow.md`
4. `docs/agent-working-surface/platform-next-work-backlog.md` rows NW-087,
   NW-100, NW-088, and NW-093
5. `docs/specifications/platform/production-web-admin-authentication-and-authority.md`
6. `deploy/reference/provisioning-inputs.md`
7. `docs/operations/runbooks/production-deployment-runbook.md` section for
   reviewed configuration publication
8. Relevant source/tests:
   - `server/src/main/java/dev/datarun/server/ops/provisioning/ReviewedConfigProvisioner.java`
   - `server/src/main/java/dev/datarun/server/authorization/AdminCommandCapabilityService.java`
   - `server/src/main/java/dev/datarun/server/config/AdminCommandCapabilityPolicy.java`
   - `server/src/test/java/dev/datarun/server/ops/provisioning/OneShotProvisioningIntegrationTest.java`
   - `server/src/test/java/dev/datarun/server/authorization/AdminCommandCapabilityServiceIntegrationTest.java`

Do not open contracts unless you believe a process-boundary contract must
change. The expected route is no contract change.

## In Scope

- Add `admin_command_capabilities` to the reviewed config manifest shape.
- Require the field; unknown manifest fields should remain rejected.
- Validate with `AdminCommandCapabilityService` /
  `AdminCommandCapabilityPolicy`.
- Apply it transactionally with the existing reviewed config operation.
- Preserve idempotent exact reapply behavior.
- Update focused tests and provisioning documentation.

## Out Of Scope

Do not implement:

- online admin command capability APIs or UI;
- online principal-binding admin;
- config authoring/review/approve/publish browser workflow;
- assignment admin UX;
- mobile login/session or mobile config changes;
- tenant/workspace-aware auth, sync, storage, or config;
- config-package schema or mobile-visible command authority;
- database migrations unless an existing table cannot support the reviewed row;
- real-production approval.

## Required Behavior

- `admin_command_capabilities` stays in `deployment_config` under the existing
  NW-087 policy key.
- Missing, malformed, or unknown-command policy in reviewed config is rejected
  before publication succeeds.
- A failed reviewed config apply leaves no partial shapes, activities,
  expressions, deployment config changes, or config package publication.
- Exact reapply of the same reviewed config does not publish a new package and
  reports zero changed authoring rows.
- The fixed development admin actor is not implicitly seeded.
- IdP groups, roles, claims, JWT `actor_id`, request/UI actor IDs, assignment
  roles, and config package content remain non-authority for web/config admin
  commands.

## Expected Changed Surfaces

- `ReviewedConfigProvisioner`
- `OneShotProvisioningIntegrationTest`
- provisioning input/runbook docs
- status/backlog acceptance after verification

Do not change `contracts/`, mobile code, BAR, CDL, migrations, or real
production standing.

## Expected Tests

Add or update focused server tests proving:

- reviewed config applies `admin_command_capabilities`;
- exact reapply remains idempotent;
- invalid admin command policy rejects and rolls back;
- server policy evaluation still denies absent/malformed policy and grants only
  explicit actor+command entries.

Run focused Maven tests you add or touch. Run broader auth/config/provisioning
regression if shared provisioning/config code changes.

## Verification

Required before commit:

```bash
git diff --check
```

Also report:

- exact Maven test command(s) run;
- whether contracts, schemas, migrations, mobile files, BAR, CDL, and
  real-production operations evidence had no diff;
- that NW-088 remains separate.

## Stop Conditions

Stop and report before work that:

- makes `admin_command_capabilities` config-package content;
- derives command authority from IdP groups/roles/claims, JWT `actor_id`,
  request bodies, UI state, fixed dev actors, or assignment roles;
- adds online admin capability or principal-binding administration;
- changes contracts/schemas/envelopes, sync protocol, mobile auth/session,
  tenant/workspace context, storage isolation, retention/security, or real
  production approval;
- implements the S23 browser config workflow instead of only the seed path.

## Commit Flow

Use one route commit before implementation:

```text
docs(ops): route admin command provisioning seed
```

Use one implementation commit:

```text
feat(ops): seed admin command capabilities
```

Use the NW trailer:

```text
NW: NW-100
```

Do not mark NW-100 accepted in the implementation commit. After tests pass,
record acceptance separately with a status/backlog commit.
