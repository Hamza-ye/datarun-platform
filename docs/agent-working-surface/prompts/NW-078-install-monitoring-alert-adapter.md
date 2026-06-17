# NW-078 Agent Prompt: Install Monitoring And Alert Adapter

You are working in `/home/hamza/datarun-platform`.

## Goal

Close the NW-067 R11 alert-delivery blocker by installing or selecting a
concrete monitoring/logging/alert adapter for the synthetic reference
environment and exercising the required alert injects.

This is an operational adapter for the reference lab only. Do not claim
real-production monitoring coverage; final production destination, backup
recipient, provider integration, and independent continuity remain undecided.

## Read

1. `AGENTS.md`
2. `docs/status.md` Current Routing
3. `docs/commit-workflow.md`
4. `docs/operations/policies/first-reference-deployment-policy.md`
   Sections 8, 11, 13, and 14
5. `docs/operations/runbooks/production-deployment-runbook.md` Section 11
6. `docs/operations/rehearsals/production-deployment-rehearsal-plan.md`
7. `docs/operations/rehearsals/2026-06-17-production-deployment-reference-environment.md`
8. `docs/agent-working-surface/platform-next-work-backlog.md` NW-078 row

## Current Lab Gap

Read-only inspection on 2026-06-17 found:

- App host: `datarun-app.lab` / `192.168.1.213`, hostname
  `vm-datarun-app.lab.nmcpye`.
- App containers: `datarun-reference-server-1` is healthy and publishes only
  `127.0.0.1:18080->8080` and `127.0.0.1:18081->8081`;
  `datarun-local-registry` publishes `127.0.0.1:5000->5000`.
- App images available locally:
  `localhost:5000/datarun/server@sha256:5f246547e1292092b133540a86efe230fd9f9bacc1f73bccc83e8644e4fb82e2`,
  previous rehearsal images, and `registry:2`.
- App management checks currently return readiness `{"status":"UP"}` and
  Prometheus-compatible metrics including Hikari/JDBC, JVM, disk, HTTP,
  process, system CPU, and logback counters.
- App host capacity at inspection: `/` and `/opt/datarun-lab` about 7% used,
  2 CPUs, about 3.8 GiB memory with about 3.0 GiB available.
- Ops host: `keycloak.lab` / `192.168.1.217`, hostname
  `vm-datarun-ops-01.lab.nmcpye`.
- Ops containers: only `datarun-keycloak` is running, exposing host `443` to
  container `8443`.
- Ops images available locally: only
  `quay.io/keycloak/keycloak@sha256:5fdbf2dbb5897cc34e82de49d13e23db011f9925089dbc555fc095f2c8bc1dac`.
- Ops files under `/opt/datarun-lab` contain Keycloak config, pki material,
  and secret files. Secret values were not inspected.
- Prior R11 evidence says the run is blocked because no
  Prometheus/Alertmanager/Grafana or equivalent alert-recipient adapter was
  present.

Important boundary: the app management port is intentionally loopback-only on
the app host. A stack running only on the ops VM cannot scrape
`127.0.0.1:18081` without a tunnel or breaking the accepted listener boundary.
If the local workspace cannot resolve `*.lab` names, use the documented IPs
with SSH host-key aliases, for example:
`ssh -o BatchMode=yes -o HostKeyAlias=datarun-app.lab nmcp@192.168.1.213`.

## Minimum Safe Adapter

Use a two-part synthetic lab stack:

- App host monitoring agent: Prometheus, blackbox-exporter, and node-exporter
  on `datarun-app.lab`. Prometheus must run on the app host so it can scrape
  `http://127.0.0.1:18081/actuator/prometheus` and probe
  `http://127.0.0.1:18081/actuator/health/readiness` without exposing the
  management port.
- Ops host alert/dash adapter: Alertmanager and optional Grafana on
  `keycloak.lab`. Alertmanager must deliver to Hamza or an explicitly approved
  synthetic recipient. A local webhook-only sink is useful for debugging but
  does not satisfy alert delivery unless Hamza accepts it as the synthetic
  recipient for this rehearsal.

Required image inputs must be selected and recorded by digest before start:

```bash
export PROMETHEUS_IMAGE='docker.io/prom/prometheus@sha256:<reviewed-digest>'
export ALERTMANAGER_IMAGE='docker.io/prom/alertmanager@sha256:<reviewed-digest>'
export BLACKBOX_IMAGE='docker.io/prom/blackbox-exporter@sha256:<reviewed-digest>'
export NODE_EXPORTER_IMAGE='quay.io/prometheus/node-exporter@sha256:<reviewed-digest>'
export GRAFANA_IMAGE='docker.io/grafana/grafana@sha256:<reviewed-digest>'
```

Stop if digest-pinned images cannot be obtained or loaded. Do not use mutable
`latest` tags as acceptance evidence.

## Files To Create

Create these lab-only files. Do not commit rendered files containing alert
recipient secrets.

```text
/opt/datarun-lab/monitoring/
  app-compose.yaml
  ops-compose.yaml
  prometheus/prometheus.yml
  prometheus/rules/datarun-reference.rules.yml
  blackbox/blackbox.yml
  targets/http.yml
  targets/tcp.yml
  textfile/datarun-backup.prom
  textfile/datarun-rehearsal-inject.prom
  alertmanager/alertmanager.yml.template
  alertmanager/alertmanager.yml        # rendered secret-bearing file
  grafana/provisioning/datasources/prometheus.yml
```

App host setup skeleton:

```bash
ssh -o BatchMode=yes nmcp@datarun-app.lab
sudo install -d -m 0750 -o root -g sudo \
  /opt/datarun-lab/monitoring/prometheus/rules \
  /opt/datarun-lab/monitoring/blackbox \
  /opt/datarun-lab/monitoring/targets \
  /opt/datarun-lab/monitoring/textfile
```

Ops host setup skeleton:

```bash
ssh -o BatchMode=yes nmcp@keycloak.lab
sudo install -d -m 0750 -o root -g sudo \
  /opt/datarun-lab/monitoring/alertmanager \
  /opt/datarun-lab/monitoring/grafana/provisioning/datasources
sudo install -d -m 0700 -o root -g root \
  /opt/datarun-lab/monitoring/secrets
```

## Prometheus Targets

Generate the real target files on the app host. Read the JWKS URI from the
protected runtime config with operator privileges; do not hard-code a realm
path, and do not print secret file contents.

```bash
sudo sh -c 'jwks_uri=$(tr -d "\r\n" </opt/datarun-lab/runtime-config/datarun.auth.oidc.jwks-uri)
cat >/opt/datarun-lab/monitoring/targets/http.yml <<EOF
- targets:
  - http://127.0.0.1:18081/actuator/health/readiness
  labels:
    check: readiness
    owner: hamza
    runbook: production-deployment-runbook-section-11
- targets:
  - ${jwks_uri}
  labels:
    check: jwks
    owner: hamza
    runbook: production-deployment-runbook-section-11
- targets:
  - https://datarun-rehearsal.lab
  - https://keycloak.lab
  labels:
    check: certificate
    owner: hamza
    runbook: production-deployment-runbook-section-11
EOF
cat >/opt/datarun-lab/monitoring/targets/tcp.yml <<EOF
- targets:
  - datarun-db1.lab:5432
  labels:
    check: database
    owner: hamza
    runbook: production-deployment-runbook-section-11
EOF'
```

## Alert Rules

The rule file must cover these real signals:

- readiness: blackbox probe of the management readiness endpoint, critical
  after 5 minutes down;
- database: TCP probe to `datarun-db1.lab:5432`, critical on failure;
- JWKS: blackbox HTTP probe of the configured JWKS URI, critical on failure;
- backup freshness: `time() - datarun_backup_last_success_timestamp_seconds`
  greater than 3600 seconds, and backup encryption proof metric not equal to 1;
- certificate: warn below 30 and 14 days, critical below 7 days;
- capacity: CPU, memory, disk, and DB connection use over 70% for 15 minutes
  and over 85% for 10 minutes; disk projected to fill within 24 hours;
- server errors: repeated server error or logback error counters over the
  selected review window.

Also include separate `inject="true"` rules for safe synthetic proof using
`datarun_rehearsal_*` textfile metrics. Synthetic rules prove alert routing;
they do not prove the underlying production condition occurred.

## Compose Constraints

App host compose constraints:

- Prometheus must be able to scrape app loopback management.
- The app management port must remain unpublished beyond `127.0.0.1`.
- Prometheus web UI may bind only to the lab interface or loopback; if exposed
  to Grafana on the ops host, restrict it to lab/operator access.
- Node-exporter must use a textfile collector directory at
  `/opt/datarun-lab/monitoring/textfile`.

Ops host compose constraints:

- Alertmanager must be reachable from app-host Prometheus.
- The recipient URL, SMTP password, or equivalent delivery secret must live
  only under `/opt/datarun-lab/monitoring/secrets` or another protected secret
  path.
- Grafana is optional for pass if Prometheus/Alertmanager evidence provides
  dashboard-equivalent data, but include it if the rehearsal wants a visual
  dashboard record.

## Injects

Use bounded injects only. Do not stop PostgreSQL, Keycloak, TLS proxy, or the
app host to prove alert routing.

1. Readiness inject: add a temporary blackbox target
   `http://127.0.0.1:65535/actuator/health/readiness` with labels
   `check="readiness", inject="true"`, wait for the accepted 5-minute alert,
   then remove it and verify resolved.
2. Database inject: add a temporary TCP blackbox target
   `datarun-db1.lab:1` with labels `check="database", inject="true"`, wait for
   alert delivery, then remove it and verify resolved.
3. JWKS inject: add a temporary HTTP target derived from the configured JWKS
   host but using a non-existent path, labelled `check="jwks", inject="true"`.
   A 404 or probe failure must alert without changing Keycloak.
4. Backup inject: write an old synthetic timestamp to
   `datarun_rehearsal_backup_last_success_timestamp_seconds{inject="true"}`.
   Keep the real backup metric separate. This does not clear NW-075 backup
   encryption/PITR proof.
5. Certificate inject: create an ephemeral 6-day self-signed cert and serve it
   with `openssl s_server` on an unused lab-only port; probe it with a
   blackbox module that records expiry while tolerating the synthetic issuer.
   Remove the process and files after evidence capture.
6. Capacity inject: write synthetic textfile metrics such as
   `datarun_rehearsal_capacity_ratio{resource="cpu",inject="true"} 0.86`.
   Do not run unbounded CPU, memory, disk-fill, or connection-exhaustion tests
   against the reference service.

## Commands To Run

Before start, capture current state:

```bash
ssh -o BatchMode=yes nmcp@datarun-app.lab docker ps -a
ssh -o BatchMode=yes nmcp@datarun-app.lab docker image ls -a --digests
ssh -o BatchMode=yes nmcp@datarun-app.lab \
  curl --fail --silent --show-error http://127.0.0.1:18081/actuator/health/readiness
ssh -o BatchMode=yes nmcp@datarun-app.lab \
  curl --fail --silent --show-error http://127.0.0.1:18081/actuator/prometheus \
  > "$DATARUN_EVIDENCE_DIR/app-prometheus-before.txt"
ssh -o BatchMode=yes nmcp@keycloak.lab docker ps -a
ssh -o BatchMode=yes nmcp@keycloak.lab docker image ls -a --digests
```

Validate configs before start:

```bash
ssh -o BatchMode=yes nmcp@datarun-app.lab \
  docker run --rm --entrypoint promtool \
  -v /opt/datarun-lab/monitoring/prometheus:/etc/prometheus:ro \
  "$PROMETHEUS_IMAGE" check config /etc/prometheus/prometheus.yml
ssh -o BatchMode=yes nmcp@datarun-app.lab \
  docker run --rm -v /opt/datarun-lab/monitoring/blackbox:/etc/blackbox:ro \
  "$BLACKBOX_IMAGE" --config.check --config.file=/etc/blackbox/blackbox.yml
```

Start the adapter:

```bash
ssh -o BatchMode=yes nmcp@keycloak.lab \
  docker compose -f /opt/datarun-lab/monitoring/ops-compose.yaml up -d
ssh -o BatchMode=yes nmcp@datarun-app.lab \
  docker compose -f /opt/datarun-lab/monitoring/app-compose.yaml up -d
```

Verify scrape and alert path:

```bash
ssh -o BatchMode=yes nmcp@datarun-app.lab \
  curl --fail --silent --show-error http://127.0.0.1:19090/-/ready
ssh -o BatchMode=yes nmcp@keycloak.lab \
  curl --fail --silent --show-error http://127.0.0.1:19093/-/ready
ssh -o BatchMode=yes nmcp@datarun-app.lab \
  curl --fail --silent --show-error 'http://127.0.0.1:19090/api/v1/targets'
ssh -o BatchMode=yes nmcp@keycloak.lab \
  curl --fail --silent --show-error 'http://127.0.0.1:19093/api/v2/alerts'
```

Record alert delivery by preserving the Alertmanager alert JSON, the
recipient-side received notification metadata, acknowledgement time, triage
note, and recovery decision. Redact recipient secrets and token-bearing URLs.

## Acceptance

NW-078 can be accepted only when evidence shows:

- app readiness/liveness and Prometheus-compatible metrics are scraped without
  exposing the management endpoint publicly;
- database connectivity, JWKS dependency, backup freshness, certificate, and
  capacity signals are represented by the selected adapter;
- readiness, DB/JWKS, backup freshness, certificate, and capacity injects
  deliver alerts to Hamza or the approved synthetic recipient at accepted
  thresholds;
- every alert names owner, response action, escalation path, and runbook link;
- incident timeline, triage, evidence, recovery decision, cleanup, and alert
  resolution are recorded;
- telemetry does not contain secrets, tokens, private keys, raw passwords, or
  bearer URLs.

## Evidence To Retain

Use `/opt/datarun-lab/evidence/NW-078-YYYY-MM-DD` or a successor evidence root.
Retain:

- host/container/image state before and after;
- digest-pinned monitoring image identities;
- non-secret config templates, config hashes, and `promtool`/blackbox config
  validation output;
- Prometheus targets, rules, active alerts, resolved alerts, and selected query
  results;
- Alertmanager delivered-alert JSON and recipient acknowledgement metadata;
- timeline with inject start, fire time, delivery time, acknowledgement,
  triage, recovery decision, and resolution;
- server logs and telemetry secret-scan result;
- cleanup evidence showing synthetic inject files/processes removed.

Do not retain rendered Alertmanager config if it contains webhook URLs,
passwords, tokens, or recipient secrets. Retain a SHA-256 hash and a redacted
copy instead.

## Stop And Report

Stop if:

- no Hamza or approved synthetic recipient is configured;
- alerts cannot be delivered or acknowledged;
- a required signal is absent or falsely healthy;
- the app management endpoint becomes reachable outside the app host loopback;
- telemetry or evidence leaks secrets;
- a pass would require mutable monitoring images, weakening accepted
  thresholds, destructive injects, DB/Keycloak mutation, or manual schema
  repair;
- backup freshness depends on NW-075 evidence that has not been produced;
- the only monitoring path would claim real-production coverage.

## Repo Docs To Patch

- This prompt is the bounded execution packet.
- Patch `docs/operations/runbooks/production-deployment-runbook.md` Section 11
  only after the selected adapter is actually proven and should become the
  reusable operator path.
- Create a dated rehearsal/evidence record only after alert delivery and
  triage actually occur.
- Update `docs/status.md` and the NW backlog only after NW-078 acceptance
  evidence exists.
- Do not change contracts, code, architecture decisions, auth authority, or
  production approval standing for this slice.

## Commit Flow

Runbook or routing changes use:

```text
docs(ops): route monitoring alert adapter

NW: NW-078
```

Executed evidence uses:

```text
test(ops): prove monitoring alert delivery

NW: NW-078
```
