Status: accepted
Document type: rehearsal_record
Owner: Hamza
Source: NW-078; `docs/agent-working-surface/prompts/NW-078-install-monitoring-alert-adapter.md`
Authority: operates within `docs/operations/policies/first-reference-deployment-policy.md`, `docs/operations/runbooks/production-deployment-runbook.md`, and `docs/operations/rehearsals/production-deployment-rehearsal-plan.md`
Last reviewed: 2026-06-17
Supersedes: none
Related: `docs/operations/rehearsals/2026-06-17-production-deployment-reference-environment.md`; raw evidence at `datarun-app.lab:/opt/datarun-lab/evidence/NW-078-2026-06-17` and `keycloak.lab:/opt/datarun-lab/evidence/NW-078-2026-06-17`

# 2026-06-17 Monitoring Alert Adapter Evidence

## Result

Final result: `accepted`.

NW-078 installed and exercised a lab-only monitoring and alert delivery adapter
for the NW-067 reference environment. The app host runs Prometheus,
blackbox-exporter, and node-exporter with app management and monitoring
listeners bound to loopback. The ops host runs Alertmanager and a
Hamza-approved local synthetic webhook recipient for rehearsal alert delivery.

The adapter represents readiness, database connectivity, JWKS dependency,
backup freshness/encryption, certificate expiry, and capacity signals. A final
annotated inject pass delivered all six synthetic alert groups to the webhook
recipient with owner/runbook labels and response, escalation, and runbook
annotations. After cleanup, Prometheus and Alertmanager both reported zero
active alerts, and the webhook evidence contains resolved notifications for the
six synthetic groups.

This record does not accept NW-067, prove fresh-session R12, approve a real
production alert recipient, add Grafana, prove independent human continuity, or
approve real production.

## Environment

Raw evidence roots:

- `datarun-app.lab:/opt/datarun-lab/evidence/NW-078-2026-06-17`
- `keycloak.lab:/opt/datarun-lab/evidence/NW-078-2026-06-17`

App host: `datarun-app.lab` (`192.168.1.213`). Ops host:
`keycloak.lab` / `monitoring.lab` (`192.168.1.217`). Database signal target:
DB1 at `192.168.1.214:5432`.

Monitoring images:

- `prom/prometheus@sha256:69f5241418838263316593f7274a304b095c40bcf22e57272865da91bd60a8ac`
- `prom/blackbox-exporter@sha256:e753ff9f3fc458d02cca5eddab5a77e1c175eee484a8925ac7d524f04366c2fc`
- `quay.io/prometheus/node-exporter@sha256:0f422f62c15f154af8d8572b23d623aebfb10cec73a5c654d18f911f3f9df241`
- `prom/alertmanager@sha256:af26fbe4dd1886ac0efd7bd55cd9027da262e105b137a376522b7c14c3626e4a`

## Evidence Summary

| Check | Result | Evidence |
|---|---|---|
| Monitoring install | Pass | `app-monitoring-containers-after-clear-annotated-final.txt`, `ops-monitoring-containers-after-clear-annotated-final.txt`, `app-monitoring-image-identities-final.txt`, and `ops-monitoring-image-identities-final.txt` show the selected monitoring containers and digest-pinned image identities. |
| Config validation | Pass | `promtool-check-config-final.txt`, `blackbox-check-config-final.txt`, and `alertmanager-check-config-final.txt` validate the final Prometheus, blackbox, and Alertmanager configuration. |
| Real signal coverage | Pass | `prometheus-targets-after-clear-annotated-final.json`, `probe-success-after-clear-annotated-final.json`, `backup-age-after-clear-final.json`, and `db-real-probe-success-annotated-final.json` record readiness, JWKS, DB1 TCP, TLS certificate, backup freshness/encryption, and capacity signal coverage after cleanup. |
| Annotated synthetic firing | Pass | `prometheus-alerts-during-inject-annotated-final.json` and `prometheus-alerts-during-inject-annotated-final-summary.txt` show exactly six synthetic alerts firing: readiness, database, JWKS, backup freshness, certificate expiry, and capacity. Each final firing alert includes `response_action` and `escalation_path` annotations. |
| Alert delivery | Pass | `alertmanager-alerts-during-inject-annotated-final.json`, `webhook-events-during-inject-annotated-final.jsonl`, and `webhook-events-during-inject-annotated-final-summary.txt` show the six synthetic alert groups delivered to the approved local synthetic webhook recipient with owner/runbook labels and response/escalation annotations. |
| Resolution and cleanup | Pass | `prometheus-alerts-after-clear-annotated-final-summary.txt` and `alertmanager-alerts-after-clear-annotated-final-summary.txt` both report zero active alerts. `webhook-events-after-clear-annotated-final-summary.txt` records resolved notifications for all six synthetic alert groups. |
| Evidence hygiene | Pass | `secret-scan-final-app.txt` and `secret-scan-final-ops.txt` report no retained token, password, or private-key pattern matches in the NW-078 evidence roots. |

## Deviations

- Hamza approved a local synthetic webhook sink on the ops host as the
  rehearsal recipient. This proves delivery mechanics for NW-067 R11 but does
  not select a real production notification destination.
- The DB connectivity probe was switched from `datarun-db1.lab:5432` to fixed
  DB1 rehearsal IP `192.168.1.214:5432` after the app-host probe path showed
  lab-name resolution/probe instability. The final DB1 probe returned
  `probe_success=1`.
- A transient real `DatarunJwksDown` notification was observed during the
  annotated rerun and resolved before final cleanup. The final quiet-state
  evidence shows zero active Prometheus and Alertmanager alerts.
- Grafana was not installed. NW-078 accepts the Prometheus/blackbox/node-exporter
  plus Alertmanager/webhook path as the concrete rehearsal adapter.

## Cleanup State

- Synthetic HTTP, TCP, TLS, backup freshness, and capacity inject targets were
  removed from the final monitoring target and textfile metric files.
- The temporary expiring certificate, key, `openssl s_server` process, and PID
  file were removed after alert delivery and resolution evidence.
- Prometheus, blackbox-exporter, node-exporter, Alertmanager, and the synthetic
  webhook sink remain running for inspection and for the final NW-067 R12
  attempt.
- Raw evidence remains under the app and ops NW-078 evidence roots.

## Follow-Up Work

- NW-067 can now route R11 through this accepted adapter, but NW-067 remains
  partial until a genuinely fresh privileged Hamza session executes R12 solo
  cold recovery.
- Before real production, replace the local synthetic webhook recipient with
  the selected production alert destination and access controls.
