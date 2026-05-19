# Planned Consumer Review Cards

Status: Historical review evidence plus reusable card rule

This file preserves the Batch 1B planned-consumer review evidence and defines the rule for creating future planned-consumer cards. It is not a live backlog of downstream sections.

Planned-consumer cards are not platform-spec sections, not implementation authority, and not accepted downstream contracts. When a planned section is drafted, its section file supersedes any earlier card for that section.

## Current State

No active planned-consumer cards are currently maintained in this file.

The Batch 1B cards below are historical evidence for foundation acceptance. They should not be updated as if they were current downstream contracts. Future Integration Reviews should use drafted section files when they exist; create a new planned-consumer card only when an upstream section is being considered for acceptance and a downstream consumer is still `planned`.

## Use Rule

Use a planned-consumer review card only when all of these are true:

- an upstream section is being considered for acceptance
- an immediate downstream consumer is listed in the outline and mirrored in `section-registry.yml`
- the downstream consumer is still `planned` and has no section file
- Integration Review needs to check whether the upstream section is safe to accept

The card may identify required inputs, forbidden assumptions, carried gaps, and rework triggers. It must not define downstream behavior, close downstream gaps, or introduce implementation requirements.

## Review Outcomes

Each card must end with one of these outcomes:

- `Clear`: upstream section can be accepted without hidden downstream assumptions
- `Rework upstream`: upstream section must be narrowed, clarified, or moved back to draft
- `Carry explicit gap`: upstream section can be accepted only if a named gap stays visible
- `Escalate`: acceptance requires change control or Decision Board resolution

## Future Card Template

Use this compact template when a new planned-consumer card is actually needed:

```md
## <SPEC-ID> Planned Consumer Card

Consumer: `<SPEC-ID>` <title>

Upstream sections under review:

- `<SPEC-ID>` <title>

Consumption needs:

- <inputs or contracts the planned consumer needs>

Forbidden hidden assumptions:

- <assumption the upstream section must not make>

Carried gaps:

- <gap that must remain open>

Integration Review question:

- <question that decides whether the upstream section can proceed>

Outcome: Clear | Rework upstream | Carry explicit gap | Escalate.
```

## Historical Batch 1B Evidence

The Batch 1B foundation acceptance pass used planned-consumer cards to check whether planned downstream sections could later consume `SPEC-002`, `SPEC-003`, and `SPEC-004` without hidden assumptions.

| Consumer | Current superseding surface | Upstream foundation reviewed | Historical outcome | Carried gap surface |
|---|---|---|---|---|
| `04` Identity And Lineage | Candidate input `sections/05-references-and-identity-lineage.md` | `01`, `02`, `03` | Carry explicit gap | Reference serialization/emission sites; referent registration, attributes, and catalogs; alias-cycle read-side behavior. |
| `06` Configuration And Parameterization | Candidate input `sections/06-configuration-and-parametrization.md` | `01`, `03` | Carry explicit gap | Schema/versioning tooling; formal envelope serialization; platform-bundled shape inventory; Pattern Registry schema; configuration versioning and stale-configuration reconciliation; configuration authoring and deploy-time validation UX. |
| `05` Assignment, Authority, And Sync | Candidate input `sections/07-assignment-authority-and-sync.md` | `01`, `02`, `03` | Carry explicit gap | Shared-device sessions; auditor/subject scope; assessment visibility; cross-level visibility; permission/activity authority details; temporary authority, revocation, and offline grace; onboarding and role-transition details; sync delivery mechanics; sensitive local lifecycle. |
| `07` Projection, Workflow, And Pattern Registry | Planned section; historical Batch 1B card only | `01`, `02`, `03` | Carry explicit gap | Pattern Registry inventory/schema; projection compatibility across schema versions; source-chain traversal limits; platform-bundled shape inventory; configuration versioning and stale-configuration reconciliation. |
| `08` Flags, Conflict Surfacing, And Resolution | Planned section; historical Batch 1B card only | `01`, `02`, `03` | Carry explicit gap | General flag semantics; flag identity/creation/resolution mapping; platform-bundled integrity/identity/conflict shapes; alias-cycle behavior; domain conflict automation; source-chain traversal limits; auto-resolution authoring and monitoring; late authorization anomaly policy. |

Historical Batch 1B acceptance is recorded in `05-batch-1b-acceptance-packet.md`. Current platform-spec section drafting gaps are maintained in `../sections/90-open-decisions-and-gap-register-citations.md`.
