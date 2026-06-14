# Provider-Neutral Reference Deployment Assets

These assets implement the NW-063/NW-064 deployment class: one immutable
Datarun server container on one Linux host, an external TLS reverse proxy, and
an external durable PostgreSQL 16 service. They do not bundle a proxy,
database, backup system, provider adapter, or production approval.

## Inputs

Set these non-secret environment variables before rendering or starting the
Compose project:

| Variable | Requirement |
|---|---|
| `DATARUN_IMAGE` | Registry/repository image reference ending in `@sha256:` plus 64 lowercase hex characters. Mutable tags are rejected. |
| `DATARUN_IMAGE_SOURCE` | Expected HTTPS value of the image's `org.opencontainers.image.source` label. |
| `DATARUN_IMAGE_REVISION` | Expected full 40-character source commit from the image's `org.opencontainers.image.revision` label. |
| `DATARUN_RUNTIME_CONFIG_DIR` | Absolute host directory populated by the selected external secret/configuration mechanism. |
| `DATARUN_PROVISIONING_DIR` | Absolute host directory containing reviewed provisioning input. |
| `DATARUN_TRUST_DIR` | Absolute host directory containing the PostgreSQL trust root. |

Optional non-secret limits are `DATARUN_APP_HOST_PORT` (default `18080`),
`DATARUN_MANAGEMENT_HOST_PORT` (`18081`), `DATARUN_CPU_LIMIT` (`1.0`),
`DATARUN_MEMORY_LIMIT` (`1024m`), `DATARUN_PIDS_LIMIT` (`256`), and
`DATARUN_STOP_GRACE_PERIOD` (`45s`). The stop grace period must exceed the
application's default 30-second graceful shutdown phase.

The runtime directory is a Spring config tree. It must contain one scalar per
file under these exact filenames:

```text
datarun.auth.mode
datarun.auth.oidc.issuer
datarun.auth.oidc.audience
datarun.auth.oidc.jwks-uri
datarun.auth.principal-bindings.applied-by
datarun.auth.principal-bindings.manifest
spring.datasource.url
spring.datasource.username
spring.datasource.password
```

`datarun.auth.mode` must contain `oidc-jwks`.
`datarun.auth.principal-bindings.manifest` must contain the fixed container
path `/run/datarun/provisioning/principal-bindings.json`. The corresponding
reviewed manifest must exist in `DATARUN_PROVISIONING_DIR` as
`principal-bindings.json`.

For deployment-managed principal-binding, reviewed-config, and initial
assignment commands, see
[`provisioning-inputs.md`](provisioning-inputs.md). It defines the non-web
invocation boundary, exact input shapes, and evidence output; it is an
implementation reference, not the NW-066 production runbook.

The JDBC URL must select PostgreSQL TLS verification with
`sslmode=verify-full` and
`sslrootcert=/run/datarun/trust/postgresql-root.crt`; that certificate must
exist in `DATARUN_TRUST_DIR`. Keep the username and password in their separate
config-tree files, not in the URL.

No secret files or value templates belong in this directory or in Compose
environment variables. The external population mechanism must make every
mounted file readable by container UID/GID `10001:10001` without granting
unnecessary host access.

## Boundary And Output

`compose.yaml` creates only the server service. It runs the production profile
with a read-only root filesystem, a bounded temporary filesystem, all Linux
capabilities dropped, `no-new-privileges`, explicit CPU/memory/PID limits,
restart behavior, and a graceful SIGTERM window.

Both container listeners are published only on host `127.0.0.1`. The external
TLS proxy may forward public HTTPS traffic to the application host port. It
must not forward the management port. Host firewalling must still reject
direct public access, and the proxy remains responsible for certificates,
request policy, and forwarding only the intended application traffic.
Monitoring on the host may scrape the loopback management port.

## Validation

Run:

```bash
deploy/reference/preflight.sh
```

The full check validates inputs without printing their values, renders
Compose, verifies the local digest-selected image's OCI source/revision and
non-root user, and confirms that UID/GID `10001:10001` can read the mounted
files. `--config-only` omits image inspection and is suitable only for static
Compose validation; it is not release evidence.

## Stop Conditions

Stop rather than start the service when preflight fails, the image is not
identified by digest and matching source labels, a required mounted file is
missing or unreadable, PostgreSQL TLS verification is absent, either host
listener is not loopback-only, or development/default credentials are needed.

Also stop if the deployment requires a bundled database/proxy, provider
adapter, Kubernetes, manual database mutation, development admin surfaces,
new authority semantics, or a claim that backup, restore, monitoring,
rotation, login, or real-production approval has already been proven. Those
are outside these implementation assets and remain subject to NW-066/NW-067
or separately routed work.
