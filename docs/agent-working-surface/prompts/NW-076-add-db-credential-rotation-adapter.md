# NW-076 Agent Prompt: Add DB Credential Rotation Adapter

You are Worker NW-076 in `/home/hamza/datarun-platform`.

You are not alone in the codebase or lab. Do not revert unrelated repository
changes. Do not mutate remote state until the read-only precheck is complete,
the recovery path is explicit, and Hamza has approved the rotation window.

## Goal

Close the NW-067 database-credential rotation blocker by adding and exercising
a reviewed two-generation synthetic database credential rotation procedure for
the reference environment.

## Current Gap

NW-067 R10 is blocked because Section 15 of the production runbook names the
database credential rotation requirement, but no reviewed two-generation
secret adapter exists. The reference deployment already expects a config-tree
runtime mount with separate `spring.datasource.username` and
`spring.datasource.password` files, PostgreSQL TLS verification, and
loopback-only application listeners. The missing piece is the operational
procedure that creates a replacement least-privilege synthetic credential,
switches the mounted secret safely, proves service behavior, revokes the old
generation, and retains secret-safe evidence.

## Read

1. `AGENTS.md`
2. `docs/status.md` Current Routing
3. `docs/commit-workflow.md`
4. `docs/operations/policies/first-reference-deployment-policy.md`
   Section 7
5. `docs/operations/runbooks/production-deployment-runbook.md` Sections 3,
   5, 6, 7, 10, and 15
6. `docs/operations/rehearsals/production-deployment-rehearsal-plan.md`
7. `docs/operations/rehearsals/2026-06-17-production-deployment-reference-environment.md`
8. `docs/agent-working-surface/platform-next-work-backlog.md` NW-076 row
9. `deploy/reference/README.md`
10. `deploy/reference/compose.yaml`
11. `deploy/reference/preflight.sh`

## Safest Execution Route

Use synthetic generations without weakening the primary lab credential.

1. Capture read-only posture for the current app container, runtime config
   mount shape, PostgreSQL roles/grants, and `pg_hba.conf` access boundary.
2. If the running service currently uses the primary lab runtime role, first
   switch to a synthetic generation-1 role with equivalent narrow grants while
   keeping the primary credential available as the recovery path.
3. Rotate from synthetic generation 1 to synthetic generation 2 through the
   mounted secret mechanism, using a new runtime config directory or secret
   version rather than editing the live file in place.
4. Recreate or restart only the Datarun server container, run preflight and
   readiness/auth/config/sync smoke, then revoke generation 1.
5. Prove generation-1 rejection with `psql` using the old generation only
   after generation 2 is healthy.

Do not use operator SQL against application data, events, Flyway history, or
principal-binding tables except for read-only evidence queries already covered
by the runbook.

## Read-Only Remote Precheck

Run these from a network that can reach the lab. Keep outputs in the evidence
root and redact only secret values, not structural facts.

```bash
ssh -o BatchMode=yes nmcp@192.168.1.213 \
  'set -euo pipefail
   container="$(docker ps \
     --filter label=com.docker.compose.project=datarun-reference \
     --filter label=com.docker.compose.service=server \
     --format "{{.Names}}" | head -n1)"
   test -n "$container"
   docker inspect "$container" \
     --format "name={{.Name}} image={{.Image}} status={{.State.Status}}"
   docker inspect "$container" --format "{{json .Mounts}}" |
     jq -r ".[] | [.Source,.Destination,.RW] | @tsv"'
```

```bash
ssh -o BatchMode=yes nmcp@192.168.1.214 \
  'set -euo pipefail
   sudo -u postgres psql -X --set ON_ERROR_STOP=1 --no-align --tuples-only \
     --command="SELECT rolname, rolcanlogin, rolvaliduntil, rolsuper, rolcreatedb, rolcreaterole, rolreplication, rolbypassrls FROM pg_roles WHERE rolname LIKE '\''datarun%'\'' ORDER BY rolname;"
   sudo -u postgres psql -X --set ON_ERROR_STOP=1 --no-align --tuples-only \
     --command="SELECT datname, pg_catalog.pg_get_userbyid(datdba) FROM pg_database WHERE datname LIKE '\''datarun%'\'' ORDER BY datname;"
   sudo awk '\''!/^[[:space:]]*(#|$)/ { print FILENAME ":" NR ":" $0 }'\'' \
     /etc/postgresql/*/main/pg_hba.conf'
```

If PostgreSQL is containerized or managed differently in the lab, adapt only
the inspection path; keep the same outputs: role posture, database ownership,
grant posture, and host-based access rules.

## Adapter To Create

Create the lab-only adapter outside git unless the procedure becomes reusable
operator documentation:

```text
/opt/datarun-lab/adapters/nw076-db-credential-rotation.sh
/opt/datarun-lab/adapters/sql/nw076-create-runtime-role.sql
/opt/datarun-lab/adapters/sql/nw076-revoke-runtime-role.sql
```

The adapter must prompt for generated passwords with silent input or read them
from the approved secret manager. It must not take passwords in command-line
arguments, environment variables, committed files, or evidence files.

The SQL adapter should do the equivalent of the following, with role/database
names passed as `psql` variables and grants copied from the inspected current
runtime role:

```sql
CREATE ROLE :"new_role" LOGIN PASSWORD :'new_password' VALID UNTIL :'valid_until';
GRANT CONNECT ON DATABASE :"database_name" TO :"new_role";
GRANT USAGE ON SCHEMA public TO :"new_role";
GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA public TO :"new_role";
GRANT USAGE, SELECT, UPDATE ON ALL SEQUENCES IN SCHEMA public TO :"new_role";
```

Do not grant `SUPERUSER`, `CREATEDB`, `CREATEROLE`, `REPLICATION`, or
`BYPASSRLS`. If the existing runtime role has broader privileges only because
startup migrations run through that role, record that drift and stop for owner
approval before cloning it.

If `pg_hba.conf` is scoped by role, add only narrow `hostssl` entries for the
two synthetic runtime roles, the target database, and the app-host CIDR; reload
PostgreSQL after the reviewed change. Do not add broad `all all` rules.

## Commands To Run

Use the accepted runbook environment variables. Keep `set -x` disabled.

```bash
export DATARUN_EVIDENCE_DIR=/opt/datarun-lab/evidence/NW-076-$(date -u +%Y%m%dT%H%M%SZ)
install -d -m 0750 "$DATARUN_EVIDENCE_DIR"

deploy/reference/preflight.sh \
  | tee "$DATARUN_EVIDENCE_DIR/preflight-before-db-rotation.txt"
curl --fail --silent --show-error \
  "http://127.0.0.1:${DATARUN_MANAGEMENT_HOST_PORT:-18081}/actuator/health/readiness" \
  | tee "$DATARUN_EVIDENCE_DIR/readiness-before-db-rotation.json"
```

Create generation 1 and generation 2 through the adapter. Build a new runtime
config directory for the active generation and preserve all non-database
config-tree files unchanged:

```bash
/opt/datarun-lab/adapters/nw076-db-credential-rotation.sh prepare-generation \
  --database datarun \
  --role datarun_runtime_nw076_g2 \
  --current-runtime-config "$DATARUN_RUNTIME_CONFIG_DIR" \
  --next-runtime-config /opt/datarun-lab/runtime-config-nw076-g2 \
  --evidence-dir "$DATARUN_EVIDENCE_DIR"

export DATARUN_RUNTIME_CONFIG_DIR=/opt/datarun-lab/runtime-config-nw076-g2
deploy/reference/preflight.sh \
  | tee "$DATARUN_EVIDENCE_DIR/preflight-after-db-secret-switch.txt"
docker compose -f deploy/reference/compose.yaml up -d --force-recreate server
```

Run the accepted smoke checks from runbook Section 10 using the already
reviewed auth curl configs and provisioning fixtures:

```bash
curl --config "$DATARUN_AUTH_CURL_CONFIG" --fail --silent --show-error \
  "$DATARUN_PUBLIC_BASE_URL/api/auth/me" \
  | tee "$DATARUN_EVIDENCE_DIR/db-rotation-auth-me.json"
curl --config "$DATARUN_AUTH_CURL_CONFIG" --fail --silent --show-error \
  "$DATARUN_PUBLIC_BASE_URL/api/sync/config" \
  --output "$DATARUN_EVIDENCE_DIR/db-rotation-config.json"
curl --config "$DATARUN_AUTH_CURL_CONFIG" --fail --silent --show-error \
  --header 'Content-Type: application/json' \
  --data-binary @"$DATARUN_PROVISIONING_DIR/smoke-pull-request.json" \
  "$DATARUN_PUBLIC_BASE_URL/api/sync/pull" \
  | tee "$DATARUN_EVIDENCE_DIR/db-rotation-pull.json"
```

After generation 2 passes, revoke generation 1 and prove rejection:

```bash
/opt/datarun-lab/adapters/nw076-db-credential-rotation.sh revoke-generation \
  --role datarun_runtime_nw076_g1 \
  --evidence-dir "$DATARUN_EVIDENCE_DIR"

/opt/datarun-lab/adapters/nw076-db-credential-rotation.sh prove-rejected \
  --role datarun_runtime_nw076_g1 \
  --database datarun \
  --evidence-dir "$DATARUN_EVIDENCE_DIR"
```

The rejection proof should record the PostgreSQL error class or `psql` exit
status only. It must not print the rejected password.

## Evidence To Retain

Retain outside git:

- read-only app container mount metadata and runtime config filenames/modes;
- DB role posture, grant posture, and `pg_hba.conf` narrow-rule evidence;
- generated credential identifiers, owners, creation timestamps, and
  rotation timestamps, but no values;
- preflight before and after the secret switch;
- readiness, `/api/auth/me`, `/api/sync/config`, and authorized pull smoke;
- generation-1 revocation and rejection proof;
- outage duration, recovery path, cleanup state, and any residual risk.

## Repo Docs To Patch

Patch only docs unless a separate implementation task is approved.

- Update `docs/operations/runbooks/production-deployment-runbook.md` Section
  15 only if this creates a reusable operator procedure beyond the existing
  generic text.
- Create a dated record under `docs/operations/rehearsals/` only after an
  actual exercise exists and summarize secret-safe evidence paths.
- Update `docs/agent-working-surface/platform-next-work-backlog.md`,
  `docs/status.md`, and `docs/operations/README.md` only when NW-076 standing
  changes.

## Acceptance

NW-076 can be accepted only when evidence shows:

- a generation-2 synthetic database credential is created with least privilege;
- the mounted secret is updated through the approved mechanism;
- the application restarts or reloads safely and passes readiness, auth,
  config, and sync smoke;
- the generation-1 credential is revoked or disabled;
- old-credential rejection is proven at the intended boundary;
- outage timing and recovery path are recorded;
- no credential value is committed or retained in evidence.

## Guardrails

- Do not rotate real production credentials.
- Do not weaken PostgreSQL TLS verification, privileges, or `pg_hba.conf`
  posture to make the test pass.
- Do not leave both generations active unless the procedure explicitly records
  the overlap window and revokes generation 1 before acceptance.
- Do not edit application data, Flyway history, or accepted platform authority.

## Verification

Run `git diff --check` for repository changes. Verify the service is healthy
after rotation and that the old credential fails. Verify evidence contains only
secret identifiers and hashes, not secret values.

## Commit Flow

Runbook or routing changes use:

```text
docs(ops): route database credential rotation

NW: NW-076
```

Executed evidence uses:

```text
test(ops): prove database credential rotation

NW: NW-076
```

## Stop And Report

Stop if access would be lost without a recovery path, the primary lab runtime
credential would be weakened before synthetic fallback exists, the old
generation cannot be revoked, proof requires exposing a password, PostgreSQL
TLS or `pg_hba.conf` would be broadened, smoke fails after the switch, or the
only path depends on undocumented manual database repair.
