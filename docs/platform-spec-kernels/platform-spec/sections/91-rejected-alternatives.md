# 91 Rejected Alternatives

Status: Draft
Owning boundary: professional-baseline source authority
Primary owner: Challenge Reviewer

Source basis:

- `../../professional-baseline/04-architecture-baseline-v0.md`
- `../../professional-baseline/05-decision-gap-register.md`
- `../../professional-baseline/08-baseline-acceptance-check.md`
- `../../professional-baseline/20-platform-spec-outline.md`

Depends on:

- `00-specification-source-authority.md`
- `90-open-decisions-and-gap-register-citations.md`

Consumed by:

- all platform-spec section reviews
- change-control review
- engineering design review after accepted section coverage exists

## Purpose

This section consolidates rejected alternatives that first platform-spec sections and implementation designs must not reintroduce under new names. It preserves rejected paths; it does not add new rejections without review.

## Scope

This section owns:

- rejected alternatives from the accepted baseline and baseline acceptance check
- first-section rejected-path checks
- change-control triggers when implementation appears to need a rejected path

## Non-Scope

This section does not own:

- historical explanation of every alternative
- formal reopening of rejected paths
- implementation design
- product prioritization
- closure of open gaps

## Definitions

| Term | Meaning In This Section | Must Not Mean |
|---|---|---|
| Rejected alternative | A design direction excluded by accepted baseline or section review | Permanently impossible under formal change control |
| Forbidden encoding | Encoding behavior on the wrong platform axis | Naming preference |
| Reopen | Formal change-control process that challenges a rejection | Local implementation exception |

## Invariants

- Rejected alternatives remain rejected unless formally reopened through change control.
- Sections must cite the rejected alternatives relevant to their scope.
- Implementation designs must not reintroduce rejected alternatives under renamed tables, services, fields, or product objects.
- If a rejected alternative appears necessary, the correct response is change control, not local exception text.

## Contracts

### Inputs

- rejected paths from `04`
- accepted rejection list from `08`
- gap and hold-back constraints from `05`
- first-section review requirements from `20`

### Outputs

- consolidated rejected alternatives for first-section review
- change-control triggers for attempts to reintroduce rejected paths

### Boundary Crossings

| Crosses To | Through | Notes |
|---|---|---|
| All sections | relevant rejected-alternative subset | Sections cite only what they touch. |
| Change control | formal reopen trigger | Rejected alternatives can change only through explicit process. |
| Engineering design | design review checklist | Designs cannot rename and reintroduce rejected alternatives. |

## Rejected Alternatives

### Canonical Truth And Storage

- Mutable canonical records plus separate audit log.
- Snapshot-primary source-of-truth storage.
- Action-log-primary source-of-truth storage.
- Direct canonical projection patching.
- Treating projections, caches, reports, queues, dashboards, work items, or statuses as canonical operational truth.
- Deleting, redacting, or mutating accepted events without formal baseline reconsideration.

### Event Envelope, Schema, And References

- Adding envelope fields without formal change control.
- Adding structural event type values beyond the accepted six values.
- Treating envelope `type` as deployer-extensible domain taxonomy.
- Adding `status_changed`, `current_state`, or `pattern_ref` to the event envelope.
- Storing immutable `authority_context`.
- Adding `tenant_id`, `deployment_id`, `user_id`, or `group_id` as event-envelope authority fields.
- Using `device_time` for structural ordering.
- Requiring central pre-registration of every referenceable entity for structurally valid offline capture.

### Authority, Identity, And Sync

- Server-allocated identifiers for offline event, subject, or record creation.
- Rewriting historical event references to express identity evolution.
- Authorization through post-merge alias shortcuts.
- Treating actor, assignment, process, or activity references as subject-lineage lifecycle ownership.
- Treating account, group, identity-provider claim, tenant, or deployment as a direct authority source.
- Treating device identity as actor identity.
- Changing sync away from immutable, idempotent, append-only, event-scoped, access-filtered delivery without formal change control.

### Configuration, Workflow, And Product Surfaces

- Letting deployers author envelope fields, event type values, scope containment logic, state-machine mechanisms, access-control programs, arbitrary detector logic, or platform code.
- Treating `shape_ref` as workflow state, authority marker, product surface, online/offline class, role label, tenant identity, or deployment identity.
- Treating `activity_ref` as immutable authority context, pattern identity, tenant/deployment reference, or work-item identity.
- Treating product role labels as platform actor subclasses.
- Treating queues, review lists, dashboards, work items, oversight counts, stale labels, or returned-work views as canonical storage primitives.

### Conflict, Flags, And Operational Overreads

- Last-write-wins for operational conflicts requiring judgment.
- Invisible automatic merge where judgment is required.
- Rejecting invalid workflow transitions instead of accepting and flagging where the accepted baseline requires accept-and-flag.
- Treating workflow-specific ADR-005 flag behavior as general flag semantics.
- Treating conflict detection, flag lifecycle, offline operation, and detect-before-act as one universal subsystem.
- Treating offline default as every operation being offline-capable.
- Replacing event-log source of truth with snapshots or caches for performance.
- Treating retain-and-hide as sufficient sensitive-data handling.
- Treating interoperability compatibility as a Phase 1 real-time integration requirement.

## Allowed Extension Points

- A rejected alternative may be reopened only through formal change control.
- New rejected alternatives may be added after challenge review finds a drift path.
- Later sections may cite the subset relevant to their boundary.

## Forbidden Couplings

- Do not weaken a rejection by renaming it.
- Do not move a rejected behavior into implementation design.
- Do not use an open gap as a way to implement a rejected alternative.
- Do not treat a local exception as harmless when it changes a baseline invariant.

## Open Gaps

| Gap | Owner / Route | Reopen Trigger |
|---|---|---|
| Full source-line mapping for each rejected alternative | Challenge Reviewer | Before this section is promoted beyond Draft. |
| Additional rejected alternatives from later section reviews | Challenge Reviewer plus `05`/change control | A review identifies a drift path not listed here. |

## Implementation Implications

- Design reviews should check this section before approving event storage, envelope, reference, authority, configuration, workflow, flag, reporting, or local lifecycle work.
- If implementation appears to need a rejected alternative, create a change-control proposal before code depends on it.

## Review Checklist

- [ ] Rejected alternatives trace back to `04`, `08`, `05`, or reviewed assessed input.
- [ ] No open gap is treated as a rejection.
- [ ] No rejected alternative is softened into guidance only.
- [ ] No new behavior is introduced.
