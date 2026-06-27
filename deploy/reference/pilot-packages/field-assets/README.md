# Field Assets M2.2 Setup/Seed Package

Status: NW-174 lifecycle-neutral setup/seed proof material

This package supports the NW-174 proof that initial known `field_asset`
subjects can enter the pilot without lifecycle, registry, promotion, rejection,
duplicate, merge/split, import/export, or new `subject_ref` behavior.

The setup path uses only accepted mechanisms:

1. publish a configured `asset_check/v1` shape with
   `subject_binding = "field_asset"`;
2. bootstrap one setup owner assignment through the existing one-shot
   assignment bootstrap path;
3. map each stable asset subject id to accepted geographic scope through
   `subject_locations`;
4. seed each asset as an append-only capture event through normal sync push;
5. assign field users with accepted geography, activity, and `subject_list`
   scope so mobile lookup receives only authorized seeded assets.

The seed file is a synthetic pilot fixture used by tests and manual setup
evidence. It is not a registry import/export format, bulk loader, lifecycle
state model, duplicate workflow, or source of assignment scope authority.

## Files

| File | Existing command/API | Purpose |
|---|---|---|
| `reviewed-config.json` | `config-publish` | Publishes `asset_check/v1` and `field_asset_inspection` with configured `field_asset` binding. |
| `assignment-bootstrap.setup-owner.json` | `assignment-bootstrap` | Creates the initial scoped setup-owner assignment that may create narrower pilot assignments. |
| `seeded-field-assets.synthetic.json` | normal `/api/sync/push` plus `subject_locations` setup | Defines two stable synthetic asset subjects used to prove scoped lookup and no broad browsing. |

## Boundary

`field_asset` remains configured product vocabulary over existing subject
identity. Seeded assets become lookup choices only because their append-only
seed events sync through accepted assignment scope. Candidate evidence remains
unpromoted and does not add a `field_asset` binding or become lookup truth.
