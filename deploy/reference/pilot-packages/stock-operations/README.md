# Stock Operations Pilot Package Skeleton

Status: non-production pilot package skeleton
Source: NW-150
Authority: package/provisioning material only; uses existing reviewed config,
principal-binding, and initial assignment bootstrap paths

This package turns the NW-148 synthetic stocktake-line proof into reusable
non-production/pilot material for a local/on-prem stock operations lane. It
does not select real users, real stock records, account import, submitted
record import/replay, production cutover, controlled operational use, stock
ledger correctness, stock truth, cloud hosting, cross-border transfer, managed
external backup, external monitoring export, or remote support access.

## Files

| File | Existing provisioning command | Purpose |
|---|---|---|
| `reviewed-config.json` | `config-publish` | Publishes `stocktake_line/v1`, `stock_operations`, pilot assignment-admin policy, and explicit web-admin command grants for the synthetic admin/supervisor actors. |
| `principal-bindings.synthetic.json` | `principal-bindings` | Binds three synthetic local IdP subjects to the package's fixed pilot actor UUIDs. |
| `assignment-bootstrap.synthetic-admin.json` | `assignment-bootstrap` | Creates the initial root-scoped `pilot_admin` assignment for the synthetic pilot admin actor. |
| `synthetic-assumptions.json` | none | Records the non-executable synthetic actor and assignment assumptions for worker/supervisor setup. |

Use the command boundary documented in
[`../../provisioning-inputs.md`](../../provisioning-inputs.md). Keep these files
as reviewed inputs; do not replace review with direct database mutation.

`reviewed-config.json` is a complete reviewed config snapshot for a clean or
isolated pilot/reference environment. The existing `config-publish` command
rejects omitted existing deployer shapes and activities, so this file is not a
patch and must not be blindly applied to an already configured deployment. For
an existing configured deployment, produce a freshly reviewed complete snapshot
that includes the existing accepted deployer shapes and activities plus this
stock operations skeleton.

## Synthetic Actors

| Role | Actor UUID | Principal subject |
|---|---|---|
| Pilot admin/operator | `15000000-0000-4000-8000-000000000001` | `stock-pilot-admin` |
| Field stock worker | `15000000-0000-4000-8000-000000000002` | `stock-pilot-worker` |
| Stock supervisor | `15000000-0000-4000-8000-000000000003` | `stock-pilot-supervisor` |

The package assumes a local/on-prem IdP issuer of
`https://idp.local.example/datarun-stock-pilot` for synthetic proof. Replace
that issuer and the provider subject values during a selected local
Keycloak/principal-binding route; IdP groups, claims, imported accounts, and
JWT `actor_id` claims remain non-authoritative.

## Assignment Setup

The current one-shot assignment command supports only the initial bootstrap
assignment. After applying the reviewed config and principal bindings, create
ordinary pilot assignments through the existing assignment-admin path using the
synthetic `pilot_admin` actor:

- assign `15000000-0000-4000-8000-000000000002` role `field_worker` for the
  selected warehouse geography and activity `stock_operations`;
- assign `15000000-0000-4000-8000-000000000003` role `supervisor` for the same
  selected warehouse geography and activity `stock_operations`.

The selected stock-scope subject used by stocktake events must be
pre-established before events are captured or synced. For this package,
pre-established means a stable subject UUID plus a `subject_locations` mapping
under the selected pilot geography, worker/supervisor assignments covering
that geography and activity `stock_operations`, and capture/session/operator
context that can stamp that subject into `subject_ref`. Scoped
worker/supervisor visibility depends on the event `subject_ref` resolving to
that subject's write-time location path.

`reviewed-config.json` grants assignment-admin create/end capability to
`pilot_admin`. It also grants `web_admin.access` and `web_admin.read_scoped` to
the synthetic supervisor actor so scoped operational standing can be inspected
through existing web-admin report surfaces. This is read-scoped visibility only;
the `stock_operations` activity role map is limited to field capture and the
package does not add a review workflow or emit `event.type = "review"`.

## Stocktake-Line Shape

The config package defines one flat deployer-authored shape:

```text
stocktake_line/v1
```

Fields:

- `stocktake_date`
- `stock_category`
- `quantity`

The shape has `subject_binding = null`. Because the envelope requires
`subject_ref`, capture/session/operator context must stamp the stocktake event
with a pre-established pilot stock-scope subject. For the first pilot mapping,
that subject represents the stock-holding location or storage point being
counted. This is package context only; it does not define warehouse lifecycle,
stock ledger correctness, item/catalog authority, stocktake session lifecycle,
production stock truth, `process` subject emission, or a new platform scope
mechanism.

The shape is standard-sensitivity synthetic/non-sensitive pilot material. Keep
real categories, real quantities, legacy submitted records, and imported
accounts out of this package unless a later route explicitly selects them with
Hamza's owner approval.
