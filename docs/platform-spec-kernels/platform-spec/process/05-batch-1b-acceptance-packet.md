# Batch 1B Acceptance Packet

Status: Approved; status update authorized and recorded on 2026-05-12.

## Request

Approved promotion of these Batch 1B foundation atoms from `Draft` to `Accepted`:

- `SPEC-002` Glossary And Core Definitions
- `SPEC-003` Event Log And Storage
- `SPEC-004` Event Envelope And Schema

This is a narrow acceptance. It accepts only the foundation surface listed below and carries the Integration Review gaps forward.

## Recommendation

Architecture Steward recommendation: accept Batch 1B with explicit carried gaps.

Decision Board / Project Owner decision: approved on 2026-05-12.

No upstream atom edits are required before approval. No rejected path is reintroduced. No open decision in `SPEC-090` is silently closed.

## Accepted Surface

- `SPEC-002` is accepted as glossary vocabulary only.
- `SPEC-003` is accepted as append-only event-log truth with projections/read models derived from events.
- `SPEC-004` is accepted as the event-envelope structural contract, including forbidden envelope drift.

## Not Accepted By This Packet

- final reference serialization
- schema/versioning tooling
- platform-bundled shape inventory
- authority/sync policy details
- workflow/pattern inventory
- flag lifecycle and conflict semantics

## Integration Review Outcome

Verdict: Carry Explicit Gap.

Per-consumer outcomes:

| Consumer | Outcome |
|---|---|
| `SPEC-005` References And Identity Lineage | Carry explicit gap |
| `SPEC-006` Configuration And Parameterization | Carry explicit gap |
| `SPEC-007` Assignment, Authority, And Sync | Carry explicit gap |
| `SPEC-009` Projections, Workflow, And Patterns | Carry explicit gap |
| `SPEC-010` Conflict, Flag, And Resolution | Carry explicit gap |

## Gaps To Carry

- Reference serialization/emission sites; referent registration, attributes, and catalogs; alias-cycle read-side behavior.
- Formal envelope serialization; event schema/versioning tooling; projection compatibility across schema versions.
- Platform-bundled shape inventory; Pattern Registry inventory and schema.
- Configuration versioning, stale-configuration reconciliation, authoring, packaging, and deploy-time validation UX.
- Shared-device sessions; auditor/subject scope; cross-level visibility; permission/activity authority details; temporary authority, revocation, and offline grace policy; sync delivery mechanics; sensitive local purge/lifecycle.
- General flag semantics; flag identity, creation location, and resolution-event mapping; domain conflict automation outside workflow; source-chain traversal limits; auto-resolution authoring and monitoring.

## Approval Trace

Decision Board / Project Owner approval:

- approved Batch 1B promotion for `SPEC-002`, `SPEC-003`, and `SPEC-004`
- keep all carried gaps open
- authorized a status update commit changing the three atom headers and `atom-registry.yml` statuses from `draft` to `accepted`

Approval source: Project Owner instruction in the 2026-05-12 architecture-steward session.

Status update result:

- `SPEC-002`, `SPEC-003`, and `SPEC-004` are accepted implementation-facing spec authority only for the accepted surface in this packet.
- All carried gaps remain open and must be routed through their owning atoms or change control.

Referent registration, attributes, and catalogs hold-back check:

- Classification confirmed as `Hold-back / platform-spec design gap` in `SPEC-090`.
- This classification preserves the accepted baseline: typed references are envelope contracts, reference values are not referent lifecycle ownership, and new envelope fields/categories require formal change control.
- The accepted baseline and gap register do not close subject registration events, referent attributes, deployer-defined catalogs, or platform-bundled registration shapes. Keeping this area as a hold-back conforms to `08-baseline-acceptance-check.md` and `05-decision-gap-register.md`.
