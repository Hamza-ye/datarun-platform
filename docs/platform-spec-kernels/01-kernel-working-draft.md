# Platform Specification Kernel Working Draft

Status: Iteration 1 in progress

This file stages atomic platform-specification kernels in one place until the approved source set reaches rest state. Sections may be rewritten, merged, split, or demoted during extraction. Final atomic files must not be created from these sections until the conflict checks and closure pass are complete.

## Draft Discipline

Each kernel section must remain technical and narrow. It should specify one platform fact, interface, invariant, interaction rule, open issue, rejected alternative, or conditional validity rule.

Do not organize sections by ADR number. ADRs and exploration files are source anchors only.

Do not treat alternatives as options unless approved sources leave them open. Rejected alternatives are guardrails.

Do not use unapproved sources or memory to fill gaps.

## Kernel Section Template

```markdown
## Kernel: [precise technical name]

Status: Candidate | Settled | Open | Conditional | Rejected | Superseded
Kind: primitive | contract | invariant | algorithm | interaction-rule | configuration-boundary | forbidden-interpretation | open-question | rejected-alternative | conditional-validity

Specification statement:

Source basis:

Closure basis:

Scope:

Non-goals:

Forbidden interpretations:

Open edges:

Platform specification note:
```

## Staged Kernels

## Kernel: Contextual Authority

Status: Settled
Kind: invariant

Specification statement:

Authority is contextual, not absolute. A person's ability to see or act depends on the combination of actor identity, role, operational context, activity, scope, and sometimes time. Possessing a role does not grant the same authority everywhere or for every activity.

Source basis:

- `docs/access-control-scenario.md` / `## The Reality`
- `docs/access-control-scenario.md` / `### What must hold true:` / `Authority is contextual, not absolute.`
- `docs/access-control-scenario.md` / `## Where this gets hard` / `Contextual authority that varies by step`

Closure basis:

Settled as a ground-truth access-control requirement. Not yet closed here as a concrete interface or storage model.

Scope:

Applies to visibility and action authority across operational activities, review steps, approval steps, areas, subjects, and responsibility contexts.

Non-goals:

This kernel does not decide the access-control data model, sync protocol, role vocabulary, policy language, or enforcement algorithm.

Forbidden interpretations:

- Do not treat role alone as sufficient authority.
- Do not treat authority as globally uniform for an actor.

Open edges:

Concrete representation and enforcement are left for later kernels sourced from exploration and ADRs.

Platform specification note:

The platform specification must describe authority as a contextual relation, not as a global actor property.

## Kernel: Access Scope Partitioning

Status: Settled
Kind: invariant

Specification statement:

People see and act only on information appropriate to their responsibilities. The same underlying information may be partitioned differently for different audiences without duplicating or redefining the information itself.

Source basis:

- `docs/access-control-scenario.md` / `### What must hold true:` / `People only see and act on what's appropriate to their role and context.`
- `docs/access-control-scenario.md` / `## What this results in:`

Closure basis:

Settled as a ground-truth access-control requirement. Not yet closed here as a concrete sync-scope or projection contract.

Scope:

Applies to field workers, supervisors, regional leads, auditors, cross-regional coordinators, and any actor whose visibility differs by responsibility or context.

Non-goals:

This kernel does not decide whether partitioning is enforced through assignment, scope projection, query filters, device sync filtering, or another mechanism.

Forbidden interpretations:

- Do not model every audience as a separate copy of the same operational information.
- Do not equate broader visibility with broader action authority.

Open edges:

Concrete partitioning mechanics remain to be extracted from later sources.

Platform specification note:

The platform specification should separate information visibility from permission to act.

## Kernel: Temporary Access Lifecycle

Status: Settled
Kind: interaction-rule

Specification statement:

Access may be temporary. Temporary grants must be created, take effect for their intended purpose or time window, end cleanly when the reason expires, and preserve the record of actions performed during the temporary authority period.

Source basis:

- `docs/access-control-scenario.md` / `### What must hold true:` / `Access can be temporary.`
- `docs/access-control-scenario.md` / `## Where this gets hard` / `Temporary authority under time pressure`

Closure basis:

Settled as a ground-truth access-control requirement. Not yet closed here as a concrete grant/revocation protocol.

Scope:

Applies to coverage, campaigns, emergency situations, temporary expanded access, and revocation after the temporary condition ends.

Non-goals:

This kernel does not decide whether temporary access is represented as assignment, policy, scope, activity configuration, or another contract.

Forbidden interpretations:

- Do not erase or reinterpret actions performed under temporary access after the access ends.
- Do not assume revocation is instantly known by disconnected devices.

Open edges:

Offline grant/revocation reconciliation and attribution mechanics remain to be closed by later kernels.

Platform specification note:

Temporary access should be specified as a lifecycle with audit consequences, not as a transient UI state.

## Kernel: Role And Responsibility Transition Preservation

Status: Settled
Kind: interaction-rule

Specification statement:

Changes in role or responsibility must not erase work, create orphaned responsibility, or lose attribution. Work in progress either remains attributable through a transition period or is handed off, while the record preserves who acted under which role at the time.

Source basis:

- `docs/access-control-scenario.md` / `### What must hold true:` / `Changes in role or responsibility are handled gracefully.`
- `docs/access-control-scenario.md` / `## What this results in:`

Closure basis:

Settled as a ground-truth access-control requirement. Not yet closed here as a concrete handoff, assignment, or audit model.

Scope:

Applies to promotion, transfer, leave, reassignment, handoff, and responsibility continuity.

Non-goals:

This kernel does not decide the transition workflow, reassignment event shape, or responsibility-binding primitive.

Forbidden interpretations:

- Do not let role changes make prior work disappear.
- Do not rewrite historical attribution after role or responsibility changes.

Open edges:

Concrete lifecycle representation remains to be extracted from later sources.

Platform specification note:

The platform specification must preserve historical authority context even when current responsibility changes.

## Kernel: Hierarchical Visibility With Exceptions

Status: Settled
Kind: interaction-rule

Specification statement:

Hierarchical visibility generally follows organizational structure, but exceptions must be supported without undermining the hierarchy. Supervisors and regional leads need inherited visibility, while auditors or cross-boundary coordinators may require visibility outside normal reporting lines.

Source basis:

- `docs/access-control-scenario.md` / `### What must hold true:` / `Hierarchical visibility follows organizational structure — with exceptions.`
- `docs/access-control-scenario.md` / `## Where this gets hard` / `Hierarchical visibility with exceptions at every level`

Closure basis:

Settled as a ground-truth access-control requirement. Not yet closed here as a concrete hierarchy, scope, or exception model.

Scope:

Applies to organizational hierarchy, regional visibility, supervisor oversight, auditor access, cross-regional coordination, and temporary cross-boundary roles.

Non-goals:

This kernel does not decide hierarchy representation, exception encoding, or containment logic.

Forbidden interpretations:

- Do not treat hierarchy as the only access path.
- Do not model exceptions by breaking or duplicating the hierarchy.

Open edges:

The approved mechanism for hierarchy and exception composition remains to be extracted from later sources.

Platform specification note:

The platform specification should treat hierarchy as a normal visibility path and exceptions as first-class access cases.

## Kernel: Access Rule Evolvability

Status: Settled
Kind: configuration-boundary

Specification statement:

Access rules must be able to grow from simple role-and-area rules into finer distinctions by activity, time window, or information sensitivity without requiring existing rules to be rebuilt.

Source basis:

- `docs/access-control-scenario.md` / `### What must hold true:` / `The rules can grow over time.`
- `docs/access-control-scenario.md` / `## What this results in:`

Closure basis:

Settled as a ground-truth access-control requirement. Not yet closed here as a specific configuration boundary or policy language.

Scope:

Applies to evolution from coarse access models to more nuanced rule sets while preserving existing deployments.

Non-goals:

This kernel does not decide whether field-level sensitivity is supported, how policy is represented, or where the configuration boundary lies.

Forbidden interpretations:

- Do not require rebuilding existing access rules to add finer distinctions.
- Do not infer unbounded deployer-authored access logic from this requirement.

Open edges:

The boundary between configurable access rules and platform-owned access mechanisms remains to be closed by later kernels.

Platform specification note:

The platform specification should express access-rule growth as bounded evolvability, not unlimited programmability.

## Kernel: Offline Access Divergence

Status: Settled
Kind: interaction-rule

Specification statement:

Access decisions must function while disconnected. A device may enforce the last-known rules while central rules have changed; local and central enforcement can temporarily disagree, and discrepancies must be reconciled when sync occurs.

Source basis:

- `docs/access-control-scenario.md` / `## Where this gets hard` / `Access decisions that must hold offline`

Closure basis:

Settled as a ground-truth access-control requirement. Not yet closed here as a conflict, flagging, rejection, or sync-scope rule.

Scope:

Applies to disconnected devices, role revocation, subject reassignment, temporary-grant expiry, local enforcement, central enforcement, and reconciliation on sync.

Non-goals:

This kernel does not decide whether offline-discrepant work is accepted, rejected, flagged, blocked locally, or resolved through policy.

Forbidden interpretations:

- Do not assume connected and disconnected enforcement always agree.
- Do not assume central revocation is immediately enforceable on disconnected devices.

Open edges:

The reconciliation behavior and anomaly surface remain to be extracted from later sources.

Platform specification note:

The platform specification must model offline access as potentially stale and reconcileable, not as an always-current central check.

## Kernel: Authority-Context Attribution

Status: Settled
Kind: invariant

Specification statement:

Every action must be attributable to a specific person acting in a specific role and context at a specific time.

Source basis:

- `docs/access-control-scenario.md` / `### What must hold true:` / `Changes in role or responsibility are handled gracefully.`
- `docs/access-control-scenario.md` / `## What this results in:`

Closure basis:

Settled as a ground-truth access-control requirement. Not yet closed here as a concrete event-envelope or projection contract.

Scope:

Applies to auditability of operational actions across role changes, temporary access, contextual authority, and disconnected work.

Non-goals:

This kernel does not decide which fields store authorship, whether authority context is stored or derived, or how audit reconstruction works.

Forbidden interpretations:

- Do not preserve only actor identity while losing role, context, or time.
- Do not update historical attribution to match current role or responsibility.

Open edges:

The storage and reconstruction model for authority context remains to be extracted from later sources.

Platform specification note:

The platform specification should require reconstructable authority context for each action.

## Pending Split Targets

Do not create final atomic files yet. Candidate future groups, to be validated after rest state:

- primitives
- contracts
- invariants
- algorithms
- configuration
- interactions
- forbidden-patterns
- open-questions
- rejected-alternatives
- conditional-validity
