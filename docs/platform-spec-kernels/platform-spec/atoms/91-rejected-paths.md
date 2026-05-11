# Rejected Paths

Status: Draft
Owning boundary: Cross-boundary control register
Primary owner: Challenge Reviewer

Source basis:

- `../../professional-baseline/04-architecture-baseline-v0.md`
- `../../professional-baseline/07-system-boundary-map.md`
- `../../professional-baseline/09-identity-boundary-control.md`
- `../../professional-baseline/15-conflict-flag-offline-boundary-control.md`
- `../../professional-baseline/16-operational-constraints-boundary-control.md`
- `../../professional-baseline/17-authorization-visibility-boundary-control.md`
- `../../pre-operations/04-accepted-pre-atomization-decisions.md`
- `../../professional-baseline/18-envelope-shape-parametrization-boundary-control.md`
- `../../professional-baseline/19-envelope-shape-parametrization-definitions.md`

Depends on:

- `01-spec-governance.md`

Consumed by:

- every atom review
- change-control review
- implementation design review

## Purpose

This register consolidates rejected architecture and atomization paths so future specs and implementation designs can catch drift early.

## Scope

This register owns:

- rejected paths from the accepted baseline
- forbidden encodings from boundary-control overlays
- review prompts for future atoms and implementation designs

## Non-Scope

This register does not own:

- explanation of every historical alternative
- formal reopening of rejected paths
- implementation design
- product feature prioritization

## Definitions

| Term | Meaning In This Register | Must Not Mean |
|---|---|---|
| Rejected path | A design direction the accepted baseline or overlay says not to use | Permanently impossible under any future formal change |
| Forbidden encoding | A way of representing behavior in the wrong architectural axis | A naming preference |
| Reopen | A formal change-control process that challenges an accepted rejection | Informal exception |

## Invariants

- Rejected paths remain rejected unless formally reopened through change control.
- Atoms must cite relevant rejected paths in their own rejected-path sections.
- Implementation designs must not reintroduce rejected paths under different names.

## Contracts

### Inputs

- accepted baseline rejected paths
- boundary-control forbidden coupling lists
- future review findings

### Outputs

- consolidated rejected-path list
- review checklist inputs for future atoms
- change-control triggers when a rejected path is proposed

### Boundary Crossings

| Crosses To | Through | Notes |
|---|---|---|
| All atoms | rejected-path sections and review checklist | Atoms carry only the relevant subset. |
| Change Control | formal reopen trigger | Rejected paths can change only through explicit process. |
| Implementation Design | design review checklist | Implementation cannot rename and reintroduce a rejected path. |

## Rejected Paths

### Canonical Truth And Storage

- Mutable canonical records plus separate audit log.
- Snapshot-primary source-of-truth storage.
- Action-log-primary source-of-truth storage.
- Direct canonical projection patching.
- Treating projections, caches, reports, queues, or dashboards as canonical operational truth.
- Deleting, redacting, or mutating canonical events without formal baseline reconsideration.

### Event Envelope And Schema

- Adding envelope fields without formal change control.
- Changing envelope field meaning without formal change control.
- Structural ordering by `device_time`.
- Stored immutable `authority_context`.
- `status_changed` as a structural event type.
- `current_state` as canonical event state.
- `pattern_ref` as an event-envelope structural reference.
- `tenant_id`, `deployment_id`, `user_id`, or `group_id` as event-envelope authority fields.
- Deployer policy fields becoming envelope fields.

### Envelope Type Misuse

Do not encode these as envelope `type` values:

- domain facts such as case opened, case resolved, feedback, stock received, referral accepted, or inspection completed
- identity or integrity facts such as conflict detected, conflict resolved, subjects merged, or subject split
- workflow states such as submitted, pending, approved, returned, resolved, closed, or reopened
- product surfaces such as queue item, work item, dashboard item, or review item
- role labels such as supervisor action, coordinator action, auditor action, or field-worker action
- activity labels such as campaign event, campaign capture, routine capture, or setup event
- sync/display states such as pending sync, synced, stale, or local-only
- escalation levels when the platform processing remains `alert`

### Identity And References

- Server-allocated identifiers for offline event, subject, or record creation.
- Central pre-registration of referenceable entities as a prerequisite for structurally valid offline capture.
- Rewriting historical event references to express identity evolution.
- Using post-merge alias projection as the authorization target for historical events.
- Treating actor, assignment, or process references as subject-lineage ownership.
- Making shipment, campaign, case, review, or transfer matching a core subject-lineage feature.
- Making identity own general flag or conflict-resolution lifecycle.
- Treating descriptive attributes, catalog membership, or deployer-defined referent shapes as subject-lineage facts.
- Treating device identity as actor identity.

### Authorization, Sync, And Tenancy

- Making account, group, identity-provider claim, tenant, or deployment fields direct authority sources.
- Deployer-authored arbitrary access-control logic.
- Field-level sensitivity as a platform mechanism.
- Stored authority snapshots on events.
- Authorization through post-merge alias shortcuts.
- Changing sync away from immutable, idempotent, append-only, event-scoped, access-filtered delivery without formal change control.
- Treating sync scope as an independent entitlement model separate from assignment-derived access scope.

### Configuration And Parameterization

- Treating deployer configuration as arbitrary platform code.
- Letting deployers author envelope fields, event type values, scope containment logic, state-machine mechanisms, access-control programs, or arbitrary detector logic.
- Treating deployer-defined attributes, catalogs, or shapes as a platform-owned domain schema catalog unless a later atom accepts a narrow platform-bundled shape.
- Treating `shape_ref` as workflow state, authority marker, product surface, online/offline class, role label, tenant identity, or deployment identity.
- Treating `activity_ref` as immutable authority context, pattern identity, tenant/deployment reference, or work-item identity.

### Workflow, Flags, And Conflict Handling

- Rejecting invalid workflow transitions instead of accepting and flagging where the accepted baseline requires accept-and-flag.
- Last-write-wins for operational conflicts requiring judgment.
- Invisible automatic merge where judgment is required.
- Treating workflow-specific ADR-005 flag behavior as general flag semantics.
- Treating conflict detection, flag lifecycle, offline operation, and detect-before-act as one universal subsystem.
- Treating one unified flag catalog as accepted baseline behavior.
- Treating `cycle_violation` as an accepted baseline flag category.
- Treating request-time temporal anchoring as a general detector rule.
- Treating server-side flag creation as a permanent invariant for all flag categories.
- Extending source-only cascade beyond ADR-005 workflow cases as general flag semantics.
- Letting flagged or unresolved events create irreversible downstream work before relevant checks run.
- Auto-resolution for non-workflow or security-relevant conflicts without formal classification.

### Product Surface And Role Drift

- Role labels as platform actor subclasses.
- Review as one fixed subsystem because it appears as event type, pattern behavior, queue, and role label.
- Reviewer, supervisor, coordinator, auditor, or field worker as fixed platform classes.
- Queues, assigned work, review lists, oversight counts, stale labels, pending labels, returned-work views, or dashboard items as canonical storage primitives.
- Product vocabulary as source of architecture closure.

### Operational Constraint Overreads

- Treating offline default as "every operation is offline-capable."
- Requiring complete global knowledge for ordinary capture.
- Treating delayed central visibility as a platform defect.
- Replacing event-log source of truth with snapshots or caches for performance.
- Treating retain-and-hide as sufficient sensitive-data handling.
- Hard-coding one regulatory framework into the core platform.
- Treating interoperability compatibility as a Phase 1 real-time integration requirement.

## Allowed Extension Points

- A rejected path may be formally reopened through change control.
- New rejected paths may be added after challenge review.
- Individual atoms may cite only the relevant subset of this register.

## Forbidden Couplings

- Do not weaken a rejection by renaming it.
- Do not move a rejected behavior into implementation design.
- Do not treat an exception as local if it changes a baseline invariant.

## Open Gaps

| Gap | Owner / Route | Reopen Trigger |
|---|---|---|
| Full mapping from every rejected path to source line references | Challenge Reviewer | Before marking this atom Accepted. |
| Whether rejected paths should be grouped by atom once all foundation atoms are drafted | Architecture Steward | After Batch 1 foundation atoms are complete. |

## Implementation Implications

- Design reviews should check this register before approving implementation modules.
- If implementation appears to need a rejected path, it should produce a change-control proposal, not a workaround.

## Review Checklist

- [ ] Rejected paths match accepted baseline and overlays.
- [ ] No rejected path is softened into guidance only.
- [ ] No new behavior is introduced.
- [ ] Future atoms can cite relevant sections directly.
