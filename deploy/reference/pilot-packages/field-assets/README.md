# Field Assets M2.2 Setup/Seed Package

Status: NW-178 lifecycle-neutral setup/provisioning hardening

This package supports the NW-178 bounded setup path for a small synthetic or
owner-approved `field_asset` set without lifecycle, registry, promotion,
rejection, duplicate, merge/split, import/export, or new `subject_ref`
behavior.

The setup path uses only accepted mechanisms:

1. publish a configured `asset_check/v1` shape with
   `subject_binding = "field_asset"`;
2. bootstrap one setup owner assignment through the existing one-shot
   assignment bootstrap path;
3. run the package-scoped `field-assets-seed` command, which creates or
   reuses the package geography, exact `subject_locations`, setup-owner-created
   field/reviewer assignments, and append-only seed capture events;
4. assign field users with accepted geography, activity, and `subject_list`
   scope so mobile lookup receives only authorized seeded assets.

The seed file is a synthetic pilot fixture used by tests and controlled setup
evidence. It is not a registry import/export format, bulk loader, lifecycle
state model, duplicate workflow, or source of assignment scope authority.

## Files

| File | Existing command/API | Purpose |
|---|---|---|
| `reviewed-config.json` | `config-publish` | Publishes `asset_check/v1` and `field_asset_inspection` with configured `field_asset` binding. |
| `assignment-bootstrap.setup-owner.json` | `assignment-bootstrap` | Creates the initial scoped setup-owner assignment that may create narrower pilot assignments. |
| `seeded-field-assets.synthetic.json` | `field-assets-seed` | Defines two stable synthetic asset subjects, their accepted location binding, and the narrow field/reviewer/out-of-scope assignments used to prove scoped lookup and no broad browsing. |

## Invocation Order

Run the files in this package in order:

1. `config-publish` with `reviewed-config.json`.
2. `assignment-bootstrap` with `assignment-bootstrap.setup-owner.json`.
3. `field-assets-seed` with `seeded-field-assets.synthetic.json`.

`field-assets-seed` is exact-reapply idempotent. If the package geography,
subject-location mapping, setup-owner-created assignments, or seed capture
events already exist with the same meaning, the command reports them as reused.
If any of those records exist with different content, the command fails instead
of updating them.

Recovery from a failed seed is operational, not lifecycle behavior: fix the
reviewed input and rerun before field rehearsal, or restart from a clean pilot
environment. The command does not delete, edit, promote, reject, merge, split,
or export asset records.

## Boundary

`field_asset` remains configured product vocabulary over existing subject
identity. Seeded assets become lookup choices only because their append-only
seed events sync through accepted assignment scope. Candidate evidence remains
unpromoted and does not add a `field_asset` binding or become lookup truth.
