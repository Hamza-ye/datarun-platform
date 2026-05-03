# Baseline Acceptance Check

Status: Accepted sign-off for ADR-001 through ADR-005 baseline

This is the focused acceptance pass over:

- `../10-adr1-5-rest-state-closure-register.md`
- `04-architecture-baseline-v0.md`
- `05-decision-gap-register.md`

No ADR-006-R through ADR-009 content is used here.

## Acceptance Decision

The ADR-001 through ADR-005 baseline is accepted as stable enough to build from for:

- system boundary validation
- prioritized gap routing
- targeted ADR-006+ assessment
- platform-spec skeleton drafting

This does not make the baseline a final platform specification. It accepts it as the current engineering comparison surface.

## Accepted Closed Baseline Items

- Immutable event log source of truth.
- Stable event envelope baseline, including device-time advisory semantics.
- Client-generated identity and lineage preservation.
- Immutable event sync and access-scoped delivery.
- Authority as projection, with no stored immutable `authority_context`.
- Detect-before-act and accept-and-flag discipline.
- Platform-fixed structural event types and deployer-configured shapes.
- Bounded configuration evaluation and deployer policy surfaces.
- Scope-change local data strategy: additive expansion and selective-retain contraction.
- Projection-derived workflow state.
- ADR-005 workflow-specific flag lineage and auto-resolution boundary.
- Bounded `context.*` form-expression surface.

## Accepted Open Gaps

These gaps are accepted as explicit, not accidental. They are not blockers to accepting the baseline itself.

P1, before platform-spec skeleton or later-source use:

- ADR-006-R through ADR-009 assessment.
- General flag semantics if the platform spec needs more than ADR-005 workflow flag behavior.
- Exact Pattern Registry inventory.
- Formal Pattern Registry schema format.
- Source-chain traversal limits.
- Domain conflict automation outside workflow if non-workflow conflict behavior must be specified now.

P2, before core implementation planning or affected spec sections:

- Bounded context-expression execution details.
- Projection performance and caching.
- Event schema/versioning tooling and projection compatibility.
- Configuration authoring, deployment, validation, and migration UX.
- Auto-resolution authoring and monitoring.
- Sync delivery mechanics.
- Reporting and aggregation if reporting is included in the initial spec skeleton.

P3, policy/product-timed unless a first deployment needs them:

- Retention and archival.
- Setup experience, onboarding, and role transition details.
- Subject-based scope and auditor access.
- Shared-device actor scope.
- Assessment visibility.
- Sensitive-subject policy beyond shape/activity sensitivity.
- Grace-period policy.
- Permission table details.
- Cross-level distribution visibility.
- Domain-agnostic proof gap.
- Low-end device scale risk beyond the existing selective-sync/local-lifecycle baseline.

## Accepted Rejected Paths

- Mutable canonical records plus separate audit log.
- Snapshot-primary or action-log-primary source-of-truth storage.
- Server-allocated identifiers for offline event/subject/record creation.
- Last-write-wins and invisible automatic merge where judgment is required.
- Structural ordering by `device_time`.
- Stored immutable `authority_context`.
- Deployer-authored arbitrary access-control logic.
- Field-level sensitivity.
- Retain-and-hide as sufficient sensitive-data handling after scope contraction.
- `status_changed`, `current_state`, and `pattern_ref` as ADR-005 structural additions.
- Rejecting invalid workflow transitions instead of accepting and flagging.

## Suspicious Or Disputed Items

No baseline item is currently marked as disputed.

Items that need careful handling during the next passes:

- General flag semantics must not absorb ADR-005 workflow-specific flag behavior without classification.
- ADR-006-R may supersede ADR-006 only inside the ADR-006 revision lineage; it does not supersede ADR-001 through ADR-005.
- Domain conflict automation outside workflow must not be inferred from ADR-005 auto-resolution.
- Configuration must not become deployer-authored platform logic.
- Reporting, triggers, and local lifecycle must not redefine event-log source of truth, authority, or sync-scope semantics.

## Next Order

1. Validate `07-system-boundary-map.md` against the accepted baseline and accepted gaps.
2. Use the validated boundary map to route ADR-006-R through ADR-009 claims.
3. Draft the first platform-spec skeleton only after later-source claims are classified or explicitly deferred.
