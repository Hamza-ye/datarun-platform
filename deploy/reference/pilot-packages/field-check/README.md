# Field Check M1.1 Package

Status: NW-171 local/on-prem journey provisioning material

This package supports the NW-171 M1.1 owner-operated local/on-prem
field-work closure proof. It adds one neutral `field_check` activity and one
`field_check_record/v1` shape while preserving the already-published stock
proof-fixture rows required by the complete reviewed-config snapshot command.

The package is not a product vocabulary decision, production cutover approval,
real-data import, legacy account import, submitted-record replay, retention or
security promise, S06 lifecycle route, tenant/control-plane route, or broad
reporting surface.

## Files

| File | Existing command/API | Purpose |
|---|---|---|
| `reviewed-config.json` | `config-publish` | Publishes the neutral field-check activity and preserves existing proof-fixture setup rows. |
| `assignment-create.field-worker.json` | `POST /api/assignments` | Creates the contained field-check responsibility for the bound proof actor through the accepted actor-bound assignment API. |

## Actor Boundary

NW-171 reuses the fresh local Keycloak principal accepted in NW-164 and
NW-165. The same server-resolved actor is used for setup owner, field user, and
owner/supervisor evidence in this bounded solo-owner proof:

```text
actor_id=15000000-0000-4000-8000-000000000001
issuer=https://auth.nmcpye.org/realms/datarun-local
```

This records a solo-owner/operator proof boundary. It does not claim separate
staff, production users, or a new actor authority model.
