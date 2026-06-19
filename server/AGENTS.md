# Server Agent Instructions

Use this file for server setup and validation details when touching `server/`
or server-owned behavior.

## Test Database

Local integration tests expect the Postgres test database:

```bash
cd /home/hamza/datarun-platform
docker compose -f docker-compose.test.yml up -d test-db
```

CI uses a GitHub Actions Postgres service instead of this local Compose step.

## Maven Commands

Focused test from `server/`:

```bash
./mvnw -Dtest=<RelevantTestClass> test
```

Full server test gate from `server/`:

```bash
./mvnw test
```

Server CI runs from `server/`:

```bash
./mvnw verify --batch-mode --no-transfer-progress
```

See `.github/workflows/server-ci.yml` for the exact CI job.

## Image Verification

Server packaging, release, Dockerfile, runtime image, or ops changes must also
consider:

```bash
scripts/verify-server-image.sh
```

The CI workflow runs this from the repository root after Maven verify.

## Evidence

Report command, cwd, result, test count/duration when available, and any skipped
gate rationale. Keep focused tests tied to the touched behavior, then run the
full gate when server behavior, contracts, auth, sync, admin, or packaging
surfaces changed.

Use `docs/agent-working-surface/validation-matrix.md` for touched-surface gates
and acceptance evidence format.
