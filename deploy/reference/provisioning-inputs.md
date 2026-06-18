# One-Shot Provisioning Inputs

This implementation reference covers the deployment-managed NW-065 command
mode. It does not authorize a production change, replace input review, or
define the NW-066 deployment procedure.

## Invocation

Run the release image with the reference mounts and production configuration,
but without a web server:

```bash
docker compose -f deploy/reference/compose.yaml run --rm --no-deps server \
  --spring.main.web-application-type=none \
  --datarun.ops.command=COMMAND \
  --datarun.ops.input=/run/datarun/provisioning/INPUT.json \
  --datarun.ops.operator-id=OPERATOR_UUID \
  --datarun.ops.evidence-id=CHANGE_OR_EVIDENCE_ID
```

`COMMAND` is exactly `principal-bindings`, `config-publish`, or
`assignment-bootstrap`. The input must be a non-empty, valid UTF-8 regular
file at an absolute path, no larger than 10 MiB. The operator is an explicit
operator-evidence UUID; it does not grant platform actor or assignment
authority. The evidence identifier is 1-200 characters using letters, digits,
`.`, `_`, `:`, `/`, or `-`.

Success prints one JSON object and exits zero. Every success includes
`command`, `status`, `operator_id`, `evidence_id`, and `input_sha256`.
`input_sha256` fingerprints the exact file bytes, including whitespace and
line endings. Failure prints a secret-safe error object and exits nonzero.
Inputs, credentials, headers, and configuration values are not printed.

## Principal Bindings

This command delegates to the accepted audited principal-binding provisioner.
Each operation ID is immutable and idempotent; reusing an ID with different
operation content fails.

```json
{
  "manifest_version": "deployment-bindings/v1",
  "source": "change:CHG-123",
  "operations": [
    {
      "operation_id": "bind-field-worker-001",
      "issuer": "https://idp.example.org",
      "subject": "provider-subject",
      "actor_id": "11111111-1111-1111-1111-111111111111",
      "state": "active",
      "reason": "approved initial binding"
    }
  ]
}
```

`operations` must be non-empty. Every listed field is required. `state` is
`active` or `inactive`; one manifest cannot contain duplicate operation IDs or
multiple operations for the same issuer/subject pair. Provider claims,
groups, roles, and JWT `actor_id` values remain non-authoritative.

## Reviewed Config

This is a complete reviewed authoring snapshot, not a patch. Omission of an
existing deployer shape or activity fails. Shape versions are immutable and
contiguous. Exact reapplication does not create another config-package
version.

```json
{
  "schema_version": 1,
  "source": "change:CHG-124",
  "shapes": [
    {
      "name": "visit",
      "version": 1,
      "status": "active",
      "sensitivity": "standard",
      "schema_json": {
        "fields": [],
        "subject_binding": null,
        "uniqueness": null
      }
    }
  ],
  "activities": [
    {
      "name": "field_visit",
      "status": "active",
      "sensitivity": "standard",
      "config_json": {
        "shapes": ["visit/v1"],
        "roles": {"worker": ["capture"]}
      }
    }
  ],
  "expressions": [],
  "flag_severity_overrides": {},
  "assignment_admin_capabilities": {
    "schema_version": 1,
    "roles": {}
  },
  "admin_command_capabilities": {
    "schema_version": 1,
    "actors": {
      "11111111-1111-1111-1111-111111111111": [
        "web_admin.access",
        "config_admin.author",
        "config_admin.validate",
        "config_admin.readiness_review",
        "config_admin.approve",
        "config_admin.publish"
      ]
    }
  }
}
```

All eight top-level fields are required and unknown fields are rejected.
Shape and activity status is `active` or `deprecated`; sensitivity is
`standard`, `elevated`, or `restricted`. Expression entries require `id`,
`activity_ref`, `shape_ref`, `field_name`, `rule_type`, and `expression`;
`message` is optional. Existing shape, activity, expression, severity, pattern,
assignment-admin, and web/config admin command validators remain authoritative.
`admin_command_capabilities` seeds only server-side deployment config for
`/web-admin` and future config-admin handlers. It is not packaged to mobile
clients and is not IdP claim, assignment-role, request-body, or UI-selected
authority. The command publishes only after full deploy-time validation
succeeds transactionally.

## Initial Assignment Bootstrap

This command invokes only the accepted initial bootstrap path:

```json
{
  "schema_version": 1,
  "source": "change:CHG-125",
  "target_actor_id": "11111111-1111-1111-1111-111111111111",
  "role": "admin",
  "geographic_id": null,
  "subject_list": null,
  "activity_list": null,
  "valid_from": "2026-06-14T00:00:00Z",
  "valid_to": null
}
```

Unknown fields are rejected. `schema_version`, `source`, `target_actor_id`,
`role`, and `valid_from` are required. Optional scope fields are UUID/string
arrays when non-null and cannot be empty; `valid_to`, when present, must be
after `valid_from`. Exact reapplication returns the existing event ID.
Any drift or any different prior assignment state fails. The command creates
no general root authority, online API, direct SQL procedure, or rollback path.
