# Platform Specification Kernel Working Draft

Status: Iteration 9 in progress

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

## Kernel: Tiered Operator Contexts

Status: Settled
Kind: conditional-validity

Specification statement:

The platform serves different operator tiers whose operating conditions differ materially: field-level workers, supervisors and team leads, coordinators and administrators, and auditors or external reviewers. Platform behavior and interfaces must remain valid across these tiers without assuming that all users have the same devices, connectivity, literacy, authority, or access patterns.

Source basis:

- `docs/constraints.md` / `## Who Uses This`

Closure basis:

Settled as an operational constraint. Not yet closed as specific personas, role vocabulary, UI surfaces, or permission model.

Scope:

Applies to primary field operations, supervision, coordination, administration, audit, and external review access.

Non-goals:

This kernel does not decide product roles, access-control roles, UI layouts, or workflow responsibilities.

Forbidden interpretations:

- Do not design as if coordinators' infrastructure and field workers' infrastructure are equivalent.
- Do not assume auditors follow the normal organizational hierarchy.

Open edges:

Concrete product surfaces and role contracts remain to be extracted from later sources.

Platform specification note:

The platform specification should distinguish operational tier constraints from architectural actor or role primitives.

## Kernel: Offline Primary Field Operations

Status: Settled
Kind: invariant

Specification statement:

Primary field operations must not require connectivity. Field workers must be able to capture, look up, and decide while offline; sync is the mechanism by which offline work becomes centrally visible, not a prerequisite for doing the work.

Source basis:

- `docs/constraints.md` / `## Connectivity`

Closure basis:

Settled as an operational constraint. Not yet closed as a storage model, sync protocol, or offline feature boundary.

Scope:

Applies to field-level capture, lookup, decisions, assigned work, and local device operation under poor or absent connectivity.

Non-goals:

This kernel does not decide which operations are primary field operations, which operations require coordinator connectivity, or how local state is stored.

Forbidden interpretations:

- Do not require a network roundtrip for primary field work.
- Do not treat offline mode as an exceptional degraded path for field users.

Open edges:

The event, projection, sync, and conflict mechanics remain to be extracted from exploration and ADRs.

Platform specification note:

The platform specification should make offline-capable field work a baseline operating invariant.

## Kernel: Synced-State Oversight

Status: Settled
Kind: interaction-rule

Specification statement:

Oversight and coordination views reflect the most recently synced state, not a live feed of field reality. The platform must make information freshness visible so supervisors and coordinators can account for sync delay.

Source basis:

- `docs/constraints.md` / `## Connectivity`
- `docs/constraints.md` / `## Responsiveness Expectations` / `Oversight and reporting`

Closure basis:

Settled as an operational constraint. Not yet closed as a freshness field, projection contract, or reporting interface.

Scope:

Applies to supervisor views, coordinator views, reporting, oversight decisions, and centrally visible field data.

Non-goals:

This kernel does not decide freshness representation, reporting model, or whether freshness is per record, per device, per scope, or per view.

Forbidden interpretations:

- Do not present centrally visible field state as guaranteed live.
- Do not hide sync age where delayed visibility affects decisions.

Open edges:

Concrete freshness metadata and projection semantics remain to be extracted from later sources.

Platform specification note:

The platform specification should explicitly describe oversight state as eventually synced and freshness-aware.

## Kernel: Large Deployment Scale Envelope

Status: Settled
Kind: conditional-validity

Specification statement:

The platform must be designed within a large-deployment envelope: tens of thousands of active field workers, hundreds of supervisors, tens of coordinators and administrators, millions of records, three to six organizational levels, and multiple concurrent activities.

Source basis:

- `docs/constraints.md` / `## Scale`

Closure basis:

Settled as an operational constraint. The numeric ranges are order-of-magnitude design bounds, not precise performance targets.

Scope:

Applies to architecture sizing, data model choices, sync assumptions, organizational hierarchy, and multi-activity support.

Non-goals:

This kernel does not define exact throughput, storage quotas, index strategy, service-level objectives, or device-local data limits.

Forbidden interpretations:

- Do not optimize only for pilot-scale deployments.
- Do not treat the numeric ranges as exact contractual maxima.

Open edges:

Concrete performance budgets and storage/sync strategies remain to be extracted or specified later.

Platform specification note:

The platform specification should preserve these ranges as the design envelope for implementation choices.

## Kernel: Continuous Record Accumulation

Status: Settled
Kind: invariant

Specification statement:

Operational data accumulates continuously and is rarely deleted. The platform must handle long-lived record growth while preserving accountability and traceability.

Source basis:

- `docs/constraints.md` / `## Scale`
- `docs/constraints.md` / `## Data Sensitivity` / `Operational accountability`

Closure basis:

Settled as an operational constraint. Not yet closed as a retention, archival, deletion, or local lifecycle policy.

Scope:

Applies to accumulated records, corrections, amendments, audit production, and long-running deployments.

Non-goals:

This kernel does not decide whether records are physically deleted, archived, compacted, summarized, retained indefinitely, or locally evicted.

Forbidden interpretations:

- Do not assume operational data is short-lived.
- Do not sacrifice traceability for convenience.

Open edges:

Retention, archival, local data lifecycle, and deletion semantics remain unresolved by this source.

Platform specification note:

The platform specification should separate continuous accumulation and traceability requirements from concrete storage lifecycle policies.

## Kernel: Concurrent Activities In One Platform

Status: Settled
Kind: invariant

Specification statement:

An organization may run multiple operational activities simultaneously within the same platform, with each activity having its own information shape, assignments, and oversight rules.

Source basis:

- `docs/constraints.md` / `## Scale` / `Concurrent activities`

Closure basis:

Settled as an operational constraint. Not yet closed as an activity model, configuration package, or event reference contract.

Scope:

Applies to multi-activity deployments, activity-specific shapes, activity-specific assignments, and activity-specific oversight.

Non-goals:

This kernel does not decide how activities are identified, versioned, referenced, configured, or separated in sync and access control.

Forbidden interpretations:

- Do not assume one deployment equals one operational activity.
- Do not solve concurrent activities by requiring separate platform instances.

Open edges:

The activity contract, configuration model, and cross-activity interaction rules remain to be extracted from later sources.

Platform specification note:

The platform specification should treat concurrent activities as a baseline deployment condition.

## Kernel: Compliance Mechanism Boundary

Status: Settled
Kind: configuration-boundary

Specification statement:

The platform does not need to enforce specific regulatory frameworks directly. It must provide mechanisms such as access control, audit trails, and data partitioning that allow deploying organizations to meet jurisdiction-specific compliance obligations without per-deployment custom development.

Source basis:

- `docs/constraints.md` / `## Data Sensitivity`

Closure basis:

Settled as an operational constraint and boundary. Not yet closed as concrete compliance configuration, policy, or audit-export interfaces.

Scope:

Applies to personal information, operational accountability, residency requirements, consent requirements, audit requirements, and jurisdictional variation.

Non-goals:

This kernel does not commit the platform to implement any named regulatory framework as built-in domain logic.

Forbidden interpretations:

- Do not bake one jurisdiction's compliance rules into the core platform.
- Do not require per-deployment custom development for ordinary compliance accommodation.

Open edges:

Concrete mechanisms for residency, consent, audit production, partitioning, and policy configuration remain to be specified later.

Platform specification note:

The platform specification should define compliance support as platform mechanisms plus deployer/jurisdiction configuration, not as hard-coded regulatory content.

## Kernel: Interoperability Compatibility

Status: Settled
Kind: conditional-validity

Specification statement:

The platform must remain capable of exchanging structured records with external systems. Real-time integration is not required by this constraint, but the internal data model and record structure must not make future structured import/export impossible.

Source basis:

- `docs/constraints.md` / `## Interoperability`

Closure basis:

Settled as an operational boundary condition. Explicitly not a Phase 1 implementation requirement in this source.

Scope:

Applies to structured export, structured import, future integration with external systems, and internal record-model choices that affect interoperability.

Non-goals:

This kernel does not decide specific external systems, integration protocols, real-time APIs, data standards, or Phase 1 delivery scope.

Forbidden interpretations:

- Do not design internal records in a way that prevents future structured exchange.
- Do not infer a requirement for real-time external integration from this source.

Open edges:

Concrete import/export contracts and integration timing remain open.

Platform specification note:

The platform specification should preserve interoperability compatibility as a design constraint even where integration is deferred.

## Kernel: Responsiveness By Work Tier

Status: Settled
Kind: conditional-validity

Specification statement:

Responsiveness expectations differ by work type: capture must feel immediate regardless of connectivity; sync is opportunistic and should complete in reasonable windows when connectivity exists; oversight and reporting are eventually consistent; configuration changes propagate on next sync.

Source basis:

- `docs/constraints.md` / `## Responsiveness Expectations`

Closure basis:

Settled as an operational constraint. The source expresses qualitative expectations, not exact latency service levels.

Scope:

Applies to capture, sync, oversight, reporting, and configuration propagation.

Non-goals:

This kernel does not define precise latency budgets, retry algorithms, sync batching, or UI loading behavior.

Forbidden interpretations:

- Do not let sync availability delay local capture.
- Do not treat oversight/reporting as real-time unless another approved source explicitly closes that requirement.

Open edges:

Precise latency, batching, and freshness contracts remain to be specified later.

Platform specification note:

The platform specification should state responsiveness expectations by work type rather than as one global latency target.

## Kernel: Configuration Changes Propagate On Sync

Status: Settled
Kind: interaction-rule

Specification statement:

Configuration changes reach field devices on their next sync. Work in progress under the old configuration completes under the old rules, while new work follows the new configuration.

Source basis:

- `docs/constraints.md` / `## Responsiveness Expectations` / `Configuration changes`

Closure basis:

Settled as an operational constraint. Not yet closed as a configuration versioning, package delivery, or migration protocol.

Scope:

Applies to changes in information collected, assignments, oversight rules, field-device propagation, and work-in-progress behavior.

Non-goals:

This kernel does not decide config package shape, version references, schema migration, assignment-change events, or conflict handling for stale configuration.

Forbidden interpretations:

- Do not require field devices to receive configuration changes immediately.
- Do not force in-progress work under old configuration to retroactively follow new rules.

Open edges:

Configuration versioning and stale-config reconciliation remain to be extracted from later sources.

Platform specification note:

The platform specification should treat configuration propagation as sync-mediated and version-sensitive.

## Kernel: Common Operational Substrate

Status: Settled
Kind: invariant

Specification statement:

The platform exists to provide a shared operational substrate for collecting information, coordinating work, tracking progress, and maintaining accountability across people, places, and time. It should absorb common operational foundations that organizations otherwise rebuild for each initiative.

Source basis:

- `docs/README.md` / `## What This Project Is`
- `docs/README.md` / `## Ambition`

Closure basis:

Settled as a vision requirement. Not yet closed as a concrete primitive set, service boundary, data model, or implementation architecture.

Scope:

Applies to field operations across information capture, responsibility assignment, review, progress tracking, coordination, oversight, and traceability.

Non-goals:

This kernel does not decide which primitives implement the substrate or how the platform is decomposed internally.

Forbidden interpretations:

- Do not reduce the platform to a single-purpose domain application.
- Do not treat every initiative as requiring bespoke foundational software.

Open edges:

The concrete reusable primitives, contracts, and interactions remain to be extracted from scenarios, exploration, and ADRs.

Platform specification note:

The platform specification should start from the operational substrate role before naming technical constructs.

## Kernel: Setup Not Built

Status: Settled
Kind: configuration-boundary

Specification statement:

Operational activities should be set up by describing what information is collected, who is responsible, what rhythms and oversight structures apply, and what should happen under relevant conditions. Standing up a new operational activity should not require rebuilding software foundations.

Source basis:

- `docs/README.md` / `## Vision`
- `docs/README.md` / `### Core Commitments` / `Set up, not built`

Closure basis:

Settled as a vision requirement. The concrete configuration boundary remains open until later approved sources close it.

Scope:

Applies to operational activity setup, information needs, responsibility, cadence, oversight, and condition-driven behavior.

Non-goals:

This kernel does not imply unlimited configurability, deployer-authored programming, or a specific configuration language.

Forbidden interpretations:

- Do not equate setup with per-deployment software development.
- Do not infer an unbounded rules engine or fully programmable platform from this vision statement.

Open edges:

The boundary between deployer configuration and platform evolution remains to be extracted from viability, exploration, and ADRs.

Platform specification note:

The platform specification should treat setup as a bounded capability whose technical boundary must be specified explicitly.

## Kernel: Coherent Single-System Experience

Status: Settled
Kind: invariant

Specification statement:

The platform must feel like one coherent system across different operational work types. Recording observations, reviewing work, tracking distribution, following up on cases, and similar activities should use consistent concepts, contracts, and ways of seeing what happened and what remains pending.

Source basis:

- `docs/README.md` / `## Vision`
- `docs/README.md` / `### Core Commitments` / `One system, not many`

Closure basis:

Settled as a vision requirement. Not yet closed as a concrete vocabulary, primitive set, UI model, or contract set.

Scope:

Applies across simple capture, reporting, review, distribution, case follow-up, oversight, and complex operational campaigns.

Non-goals:

This kernel does not require all activities to share identical screens, workflows, data shapes, or policies.

Forbidden interpretations:

- Do not model each operational activity as an isolated product with unrelated concepts.
- Do not force uniformity where the source requires coherent consistency.

Open edges:

The common concepts and contracts that create coherence remain to be extracted from later sources.

Platform specification note:

The platform specification should distinguish coherence of platform concepts from sameness of activity-specific configuration.

## Kernel: Operational Adaptability Without Rebuild

Status: Settled
Kind: invariant

Specification statement:

The platform must allow operational needs to evolve without rebuilding existing foundations. New information needs, responsibilities, oversight rules, and added complexity should be introduced without rethinking or breaking existing work.

Source basis:

- `docs/README.md` / `## Vision`
- `docs/README.md` / `### Core Commitments` / `Grows without breaking`

Closure basis:

Settled as a vision requirement. Not yet closed as schema evolution, configuration versioning, migration, compatibility, or primitive-extension mechanics.

Scope:

Applies to evolving information collection, responsibility, oversight, coordination patterns, and growth from simple to more complex operational work.

Non-goals:

This kernel does not decide that every possible future need must be deployer-configurable or backward-compatible without platform evolution.

Forbidden interpretations:

- Do not require existing deployments to be rebuilt when adding ordinary operational complexity.
- Do not treat growth as permission to mutate historical records or erase old meanings.

Open edges:

Concrete evolution mechanisms remain to be extracted from scenarios, principles, exploration, and ADRs.

Platform specification note:

The platform specification should define how stable foundations and evolvable setup coexist.

## Kernel: Domain-Agnostic Field Operations

Status: Settled
Kind: conditional-validity

Specification statement:

The platform ambition spans field operations in multiple domains, including health, logistics, agriculture, humanitarian response, and other operational settings. The platform must model common operational behaviors without hard-coding one domain's concepts as the core.

Source basis:

- `docs/README.md` / `## Ambition`

Closure basis:

Settled as an ambition constraint. Concrete proof across domains remains evidence-driven and must come from scenarios, viability assessment, exploration, and later decisions.

Scope:

Applies to core platform concepts, naming, contracts, and reusable behaviors.

Non-goals:

This kernel does not require all domain-specific validation or content to be platform-generic.

Forbidden interpretations:

- Do not hard-code health, logistics, agriculture, or humanitarian concepts as platform primitives solely because examples mention them.
- Do not treat domain-agnosticism as absence of domain-specific deployer content.

Open edges:

The boundary between domain-agnostic mechanisms and domain-specific configured content remains to be extracted from later approved sources.

Platform specification note:

The platform specification should keep core terminology operational and domain-neutral unless an approved source closes a domain-specific concept as platform-owned.

## Kernel: Scenario Problem-Space Boundary

Status: Settled
Kind: forbidden-interpretation

Specification statement:

Scenario files define real-world problem-space situations from the perspective of people and organizations doing the work. They do not name platform constructs, decompose capabilities, prescribe architecture, define sync protocols, choose conflict strategies, or imply implementation paths.

Source basis:

- `docs/scenarios/README.md` / `## Purpose`
- `docs/scenarios/README.md` / `## What These Scenarios Are Not`

Closure basis:

Settled as an extraction and interpretation rule for all scenario-derived kernels.

Scope:

Applies to every file under `docs/scenarios/`, including foundational, structural, compositional, deferred, cross-cutting, and composite scenarios.

Non-goals:

This kernel does not extract any platform primitive or interface from a scenario by itself.

Forbidden interpretations:

- Do not convert scenario prose directly into platform constructs.
- Do not infer an implementation mechanism from a scenario unless a later approved exploration or ADR closes it.
- Do not treat scenario ordering as an implementation plan.

Open edges:

Concrete constructs and decisions remain to be extracted from exploration and ADR sources after scenario evidence is captured.

Platform specification note:

The platform specification should use scenarios as requirement evidence and acceptance context, not as technical design authority.

## Kernel: Operational Complexity Progression

Status: Settled
Kind: conditional-validity

Specification statement:

The scenario set is ordered by increasing operational complexity: recording things, recurring obligations, oversight and judgment, coordination across people and places, and reactive or emergent work. This order supports reasoning over the problem space but does not prescribe architecture or implementation order.

Source basis:

- `docs/scenarios/README.md` / `## Ordering Rationale`

Closure basis:

Settled as scenario-index context.

Scope:

Applies to scenario reading order and extraction sequencing.

Non-goals:

This kernel does not decide platform phases, delivery order, implementation milestones, or technical dependencies.

Forbidden interpretations:

- Do not treat scenario ordering as proof that later scenarios depend technically on earlier scenarios.
- Do not use ordering to promote constructs before later decision sources close them.

Open edges:

Actual architecture dependencies remain to be extracted from exploration and ADRs.

Platform specification note:

The platform specification may use this progression to explain requirement coverage, not to define system layering.

## Kernel: Scenario Phase Boundary

Status: Settled
Kind: conditional-validity

Specification statement:

Scenarios 00 through 14 and 22 form the Phase 1 core problem set. Scenarios 15, 16, and 18 are compatible extensions but deferred as initial architecture drivers. Scenario 19 is cross-cutting offline reality, not a separate operational situation. Scenarios 20 and 21 are composite real-world validation contexts.

Source basis:

- `docs/scenarios/README.md` / `## Phasing`

Closure basis:

Settled as scenario-scope classification. Not settled as a technical release plan.

Scope:

Applies to scenario evidence weighting during extraction.

Non-goals:

This kernel does not decide platform feature phases, implementation sequence, or ADR scope by itself.

Forbidden interpretations:

- Do not let deferred scenarios 15, 16, and 18 drive initial core architecture without later closure.
- Do not ignore scenario 19 simply because it is cross-cutting rather than numbered into the core sequence.
- Do not treat composite scenarios 20 and 21 as new primitive sources without checking whether they compose earlier requirements.

Open edges:

Whether deferred or composite scenario pressures become closed platform rules depends on later viability, exploration, and ADR sources.

Platform specification note:

The platform specification should distinguish core, deferred, cross-cutting, and composite scenario evidence when explaining requirement coverage.

## Kernel: Foundational Scenario Evidence Set

Status: Settled
Kind: conditional-validity

Specification statement:

Scenarios 00, 01, 06, and 19 plus the access-control cross-cut form the foundational evidence set for later architecture exploration. They are foundational because they expose simplicity, identity, mutability, shape evolution, offline reconciliation, authority, and visibility pressures.

Source basis:

- `docs/scenarios/README.md` / `## Architectural Significance` / `Foundational`

Closure basis:

Settled as scenario evidence priority. Not settled as architecture or construct definition.

Scope:

Applies to extraction order, conflict checks, and later closure review.

Non-goals:

This kernel does not decide the technical solutions for recording, identity, schema evolution, sync, conflict, access control, or visibility.

Forbidden interpretations:

- Do not skip foundational scenario pressures when judging later platform kernels.
- Do not convert the "foundational" label into an architecture layer without later decision evidence.

Open edges:

The technical closure of these pressures remains to be extracted from exploration and ADRs.

Platform specification note:

The platform specification should be traceable back to these foundational scenario pressures where it defines core platform contracts.

## Kernel: Structured Capture Baseline

Status: Settled
Kind: invariant

Specification statement:

The platform must support recording a known set of details about something observed, done, or received. The expected information shape is known before capture, the completed record is kept for later reference, and users must be able to look up what was recorded.

Source basis:

- `docs/scenarios/00-basic-structured-capture.md`
- Probe cross-check: `docs/behavioral_patterns.md` identifies this as Structured Recording (P01), but that document is not formally processed yet.

Closure basis:

Settled as an S00 domain requirement. Not yet closed as a form model, event model, schema model, or database representation.

Scope:

Applies to the simplest recording workflow: known details, completion, future lookup, and confidence that required details were captured.

Non-goals:

This kernel does not decide whether records are events, submissions, forms, rows, documents, or another construct.

Forbidden interpretations:

- Do not require advanced workflow, identity, review, or coordination machinery for the simplest capture case.
- Do not infer a platform construct name from the scenario wording.

Open edges:

The technical recording primitive and validation contract remain to be extracted from exploration and ADRs.

Platform specification note:

The platform specification should preserve S00 as the minimum viable expression of recording work and use it as a simplicity check for later contracts.

## Kernel: Coexisting Record Shapes

Status: Settled
Kind: conditional-validity

Specification statement:

When the expected set of details changes, records captured under the old shape and records captured under the updated shape must both remain valid and retrievable.

Source basis:

- `docs/scenarios/00-basic-structured-capture.md` / `## What makes this hard`
- Probe cross-check: `docs/behavioral_patterns.md` identifies this as Shape Definition and Evolution (P11), but that document is not formally processed yet.

Closure basis:

Settled as an S00 domain requirement. Not yet closed as a schema-versioning mechanism or projection rule.

Scope:

Applies to old and new expected information shapes coexisting after capture has already occurred.

Non-goals:

This kernel does not decide whether old records are migrated, transformed, interpreted through versioned schemas, or handled through another mechanism.

Forbidden interpretations:

- Do not invalidate existing records merely because the expected details changed later.
- Do not require all historical records to look identical to current records.

Open edges:

The shape/version contract and read behavior remain to be extracted from later sources.

Platform specification note:

The platform specification should state that recorded information remains valid under the shape active when it was captured, once later sources close the technical mechanism.

## Kernel: Duplicate Independent Capture Pressure

Status: Settled
Kind: open-question

Specification statement:

Different people may independently record information about the same real-world occurrence or thing, producing duplicate records that are not identical. The platform must account for this pressure without assuming users know about each other's work at capture time.

Source basis:

- `docs/scenarios/00-basic-structured-capture.md` / `## What makes this hard`

Closure basis:

Settled as an S00 domain pressure. The platform response is open at this source level and must be closed by later exploration and ADRs.

Scope:

Applies to duplicate capture of the same event or subject, with differing details, times, or actors.

Non-goals:

This kernel does not decide deduplication, identity resolution, conflict detection, merge behavior, or whether duplicates are automatically or manually resolved.

Forbidden interpretations:

- Do not assume duplicate records are always identical.
- Do not assume duplicate prevention is always possible at capture time.

Open edges:

Identity, duplicate detection, conflict classification, and reconciliation behavior remain to be extracted from later sources.

Platform specification note:

The platform specification should include duplicate independent capture as a pressure on identity and reconciliation contracts, not as a scenario-level solution.

## Kernel: Traceable Record Correction

Status: Settled
Kind: invariant

Specification statement:

A completed record may need correction, and the correction must be traceable: who changed what, when, and why. The original must not be erased. Offline corrections add ordering pressure because originals and corrections may arrive centrally out of order or after another correction has already occurred.

Source basis:

- `docs/scenarios/00-basic-structured-capture.md` / `## What makes this hard`

Closure basis:

Settled as an S00 domain requirement. Not yet closed as append-only storage, correction event shape, audit model, or conflict rule.

Scope:

Applies to post-completion correction, correction attribution, preservation of the original, offline correction arrival order, and multiple corrections.

Non-goals:

This kernel does not decide the technical correction primitive, storage mutability rule, or resolution algorithm.

Forbidden interpretations:

- Do not erase the original record when correcting it.
- Do not lose correction attribution.
- Do not assume central arrival order equals the order in which work was done.

Open edges:

The storage and correction semantics remain to be extracted from exploration and ADRs.

Platform specification note:

The platform specification should make correction traceability a baseline audit requirement and later bind it to the decided write model.

## Kernel: Subject-Linked Record History

Status: Settled
Kind: invariant

Specification statement:

The platform must support records tied to recognizable, real-world subjects such as places, equipment, organizational units, people, or other identifiable things. Over time, it must be possible to review what has been recorded about a subject, when, and by whom.

Source basis:

- `docs/scenarios/01-entity-linked-capture.md`
- Probe cross-check: `docs/behavioral_patterns.md` identifies this as Subject Linkage (P02), but that document is not formally processed yet.

Closure basis:

Settled as an S01 domain requirement. Not yet closed as a subject primitive, reference shape, identity key, or registry model.

Scope:

Applies to recognizable subjects and the accumulated history of observations or reports associated with them.

Non-goals:

This kernel does not decide what counts as a platform subject, how subjects are identified, or how records reference subjects.

Forbidden interpretations:

- Do not treat subject-linked history as merely a current-state lookup.
- Do not infer a concrete identity representation from the scenario wording.

Open edges:

The subject identity model and reference contract remain to be extracted from exploration and ADRs.

Platform specification note:

The platform specification should preserve subject-linked history as a core requirement before introducing technical identity constructs.

## Kernel: Duplicate Subject Identity Pressure

Status: Settled
Kind: open-question

Specification statement:

The same real-world subject may be recorded under different identities by different people. These duplicate identities can accumulate over time and may not be obvious at the moment of recording.

Source basis:

- `docs/scenarios/01-entity-linked-capture.md` / `## What makes this hard`

Closure basis:

Settled as an S01 domain pressure. The platform response is open at this source level and must be closed by later exploration and ADRs.

Scope:

Applies to duplicate representations of facilities, people, equipment, organizational units, or other real-world subjects.

Non-goals:

This kernel does not decide duplicate detection, merge policy, aliasing, canonical identity, user review, or automated matching.

Forbidden interpretations:

- Do not assume real-world subjects are always recorded under one identity.
- Do not assume duplicate identity is always detectable at capture time.

Open edges:

Duplicate detection and identity resolution behavior remain to be extracted from later sources.

Platform specification note:

The platform specification should treat duplicate subject identity separately from duplicate record capture, even though both may interact.

## Kernel: Identity Ambiguity Over Time

Status: Settled
Kind: conditional-validity

Specification statement:

A subject's identity may become ambiguous or change over time through split, merge, reassignment, relabeling, or similar domain events. Records created before the change and records created after the change must both remain meaningful.

Source basis:

- `docs/scenarios/01-entity-linked-capture.md` / `## What makes this hard`
- Probe cross-check: `docs/behavioral_patterns.md` identifies mutable subject linkage as a variation of P02, but that document is not formally processed yet.

Closure basis:

Settled as an S01 domain pressure. Not yet closed as an identity lifecycle model.

Scope:

Applies to facilities splitting, households merging, equipment reassignment or relabeling, and any comparable subject-identity change.

Non-goals:

This kernel does not decide whether historical references are rewritten, aliased, frozen, migrated, split, merged, or projected.

Forbidden interpretations:

- Do not make historical records unintelligible after identity changes.
- Do not assume identity changes only affect future records.

Open edges:

Identity lifecycle semantics remain to be extracted from exploration and ADRs.

Platform specification note:

The platform specification should require meaningful historical and future subject references across identity changes, then bind that requirement to the decided identity model.

## Kernel: Subject History Ordering Under Sync

Status: Settled
Kind: open-question

Specification statement:

Records about a subject may be created offline by multiple people and arrive centrally out of order. The apparent central history of a subject can differ from the order in which observations actually happened.

Source basis:

- `docs/scenarios/01-entity-linked-capture.md` / `## What makes this hard`

Closure basis:

Settled as an S01 domain pressure. The ordering model is open at this source level.

Scope:

Applies to subject-linked observations and reports created offline, synced later, and interleaved with records from other actors.

Non-goals:

This kernel does not decide timestamps, causal ordering, event sequence fields, conflict detection, or projection ordering.

Forbidden interpretations:

- Do not equate central arrival order with actual work order.
- Do not assume subject history is simple append-by-arrival when offline work exists.

Open edges:

Ordering, causality, and projection semantics remain to be extracted from later sources.

Platform specification note:

The platform specification should distinguish observed time, sync arrival, and subject-history interpretation once later sources close the technical model.

## Kernel: Behavioral Pattern Boundary

Status: Settled
Kind: forbidden-interpretation

Specification statement:

The behavioral patterns extracted from scenarios name recurring behaviors the platform must support. They are not platform constructs, database tables, technology choices, implementation mechanisms, or architecture primitives.

Source basis:

- `docs/behavioral_patterns.md` / `## 1. Purpose`
- `docs/scenarios/README.md` / `## What These Scenarios Are Not`

Closure basis:

Settled as an interpretation rule for scenario and behavioral-pattern extraction.

Scope:

Applies to all scenario-derived and behavioral-pattern-derived kernels.

Non-goals:

This kernel does not decide the platform primitive vocabulary or architecture decomposition.

Forbidden interpretations:

- Do not convert a behavioral pattern name directly into a platform primitive.
- Do not treat the behavioral pattern catalog as a database or interface design.
- Do not treat the pattern decomposition as implementation scope.

Open edges:

Technical closure remains to be extracted from viability, exploration, and ADR sources.

Platform specification note:

The platform specification may use behavioral pattern names as requirement categories only where later sources do not require a stricter technical term.

## Kernel: Structured Recording Behavior

Status: Settled
Kind: invariant

Specification statement:

Someone captures a predefined set of details about something observed, done, or received. The structure exists before capture, and the resulting record remains available for later reference.

Source basis:

- `docs/behavioral_patterns.md` / `P01 — Structured Recording`
- Scenario evidence: S00, S01, S05, S20, S21

Closure basis:

Settled as a behavioral requirement. Not closed as a technical recording primitive.

Scope:

Applies to structured capture across standalone records, subject-linked records, visits, field work, and assessments.

Non-goals:

Does not decide form, event, document, schema, or storage representation.

Forbidden interpretations:

- Do not require unrelated workflow machinery for the basic recording behavior.
- Do not treat "record" as a technical storage decision at this stage.

Open edges:

Recording contract and storage model remain open until exploration/ADR closure.

Platform specification note:

Use as the behavioral baseline for any technical recording contract.

## Kernel: Subject Linkage Behavior

Status: Settled
Kind: invariant

Specification statement:

A record may be tied to a recognizable, persistent real-world subject whose identity survives across multiple interactions over time, even when that identity is stable, changing, ambiguous, or resolved later.

Source basis:

- `docs/behavioral_patterns.md` / `P02 — Subject Linkage`
- Scenario evidence: S01, S06, S08, S13, S20

Closure basis:

Settled as a behavioral requirement. Not closed as a subject primitive, reference contract, or identity lifecycle model.

Scope:

Applies to people, places, households, equipment, organizational units, cases, and comparable operational subjects.

Non-goals:

Does not decide identifiers, aliasing, merge/split, registry structure, or reference fields.

Forbidden interpretations:

- Do not assume subject identity is always stable.
- Do not assume subject linkage requires one specific technical identity model.

Open edges:

Identity and reference mechanics remain open until exploration/ADR closure.

Platform specification note:

Use as the behavioral basis for later subject, reference, and identity semantics.

## Kernel: Temporal Rhythm Behavior

Status: Settled
Kind: invariant

Specification statement:

Some work is expected on a predictable rhythm. A missing expected occurrence is meaningful, not merely absence of data.

Source basis:

- `docs/behavioral_patterns.md` / `P03 — Temporal Rhythm`
- Scenario evidence: S02, S05, S06, S09

Closure basis:

Settled as a behavioral requirement. Not closed as a scheduling, deadline, task, or projection mechanism.

Scope:

Applies to recurring reports, planned visits, registry reconfirmation, campaign windows, and follow-up expectations.

Non-goals:

Does not decide calendars, task generation, reminders, timers, or rule evaluation.

Forbidden interpretations:

- Do not treat missing work as indistinguishable from no expected work.
- Do not assume all rhythms are fixed calendar schedules.

Open edges:

Temporal representation and enforcement remain open until later sources close them.

Platform specification note:

Use to justify explicit representation of expected work and gaps once the technical model is closed.

## Kernel: Responsibility Binding Behavior

Status: Settled
Kind: invariant

Specification statement:

A specific person may be accountable for a specific scope of work, making it clear who should have done something and whether they did.

Source basis:

- `docs/behavioral_patterns.md` / `P04 — Responsibility Binding`
- Scenario evidence: S03, S09, S14, S20, S21

Closure basis:

Settled as a behavioral requirement. Not closed as an assignment model, access model, or actor-scope contract.

Scope:

Applies to explicit assignments, role-based responsibility, geographic responsibility, subject-set responsibility, work-type responsibility, and time-windowed responsibility.

Non-goals:

Does not decide assignment events, role vocabulary, scope fields, or authorization mechanics.

Forbidden interpretations:

- Do not model accountability without named responsible actors or actor groups where the scenario requires specificity.
- Do not equate responsibility with visibility or authority in every context.

Open edges:

Responsibility, access, and sync mechanics remain open until later sources close them.

Platform specification note:

Use as behavioral evidence for later assignment and authority contracts.

## Kernel: Hierarchical Visibility Behavior

Status: Settled
Kind: invariant

Specification statement:

Different people see different scopes of information and work based on organizational, geographic, programmatic, or exceptional visibility relationships. Seeing more is not the same as being authorized to do more.

Source basis:

- `docs/behavioral_patterns.md` / `P05 — Hierarchical Visibility`
- Scenario evidence: S03, S04, S05, S09, S11, S14, S15, S21

Closure basis:

Settled as a behavioral requirement. Not closed as a hierarchy model, scope model, or access-control implementation.

Scope:

Applies to supervision, regional oversight, campaign monitoring, multi-level distribution, approvals, cross-program overlays, and auditor-like exceptions.

Non-goals:

Does not decide containment logic, sync scope, read permissions, write permissions, or exception encoding.

Forbidden interpretations:

- Do not collapse visibility and action authority.
- Do not assume hierarchy has no exceptions.

Open edges:

Visibility and authority mechanics remain open until later sources close them.

Platform specification note:

Use as behavioral evidence for separating visibility from authority in later technical contracts.

## Kernel: Review And Judgment Behavior

Status: Settled
Kind: invariant

Specification statement:

Work completed by one person may pass through another person's assessment before it is final. The assessment may approve, reject, return, question, or escalate the work, and the outcome is part of the record.

Source basis:

- `docs/behavioral_patterns.md` / `P06 — Review and Judgment`
- Scenario evidence: S04, S05, S11, S21

Closure basis:

Settled as a behavioral requirement. Not closed as a workflow, state machine, review event, or approval protocol.

Scope:

Applies to supervisor review, audit visits, multi-step approval, and supervisor assessment.

Non-goals:

Does not decide review states, transition rules, reviewer assignment, or finalization mechanics.

Forbidden interpretations:

- Do not lose who reviewed what, when, and with what decision.
- Do not assume every review is single-level or same-role.

Open edges:

Review and state progression mechanics remain open until later sources close them.

Platform specification note:

Use as behavioral evidence for auditable review semantics.

## Kernel: Transfer With Acknowledgment Behavior

Status: Settled
Kind: invariant

Specification statement:

Something may move from one party to another, and the receiver confirms receipt, disputes quantity or condition, or notes a discrepancy. The transfer is not behaviorally complete until acknowledgment or discrepancy is recorded.

Source basis:

- `docs/behavioral_patterns.md` / `P07 — Transfer with Acknowledgment`
- Scenario evidence: S07, S14, S20, S22

Closure basis:

Settled as a behavioral requirement. Not closed as inventory, logistics, transaction, or workflow mechanism.

Scope:

Applies to goods, supplies, resources, authority, responsibility, and related handoff behaviors.

Non-goals:

Does not decide inventory accounting, transfer event shape, chain-of-custody model, or discrepancy resolution.

Forbidden interpretations:

- Do not treat send as equivalent to received.
- Do not erase discrepancies from the transfer history.

Open edges:

Transfer representation and reconciliation remain open until later sources close them.

Platform specification note:

Use as behavioral evidence for handoff and acknowledgment contracts.

## Kernel: State Progression Behavior

Status: Settled
Kind: invariant

Specification statement:

Work may move through meaningful stages. Earlier stages constrain later stages, progress is recorded, and current status matters for what can happen next.

Source basis:

- `docs/behavioral_patterns.md` / `P08 — State Progression`
- Scenario evidence: S04, S07, S08, S09, S11, S12, S14, S20, S22

Closure basis:

Settled as a behavioral requirement. Not closed as stored status, state machine, projection, or workflow engine.

Scope:

Applies to review, transfer, case management, campaigns, approvals, event-triggered responses, and multi-level distribution.

Non-goals:

Does not decide whether state is stored, derived, configured, or enforced by a platform mechanism.

Forbidden interpretations:

- Do not assume all progression is linear.
- Do not assume current state can be understood without history where the behavior requires progression.

Open edges:

State representation and transition semantics remain open until later sources close them.

Platform specification note:

Use as behavioral evidence for later state and workflow decisions without naming their implementation.

## Kernel: Condition-Triggered Action Behavior

Status: Settled
Kind: conditional-validity

Specification statement:

Observed conditions, thresholds, patterns, gaps, or elapsed expectations may determine that something needs attention. The resulting action may be notification, assignment, escalation, or new work, and previous actions may influence future needs.

Source basis:

- `docs/behavioral_patterns.md` / `P09 — Condition-Triggered Action`
- Scenario evidence: S10, S12, S18

Closure basis:

Settled as a behavioral requirement, with S18 representing deferred extension pressure. Not closed as a trigger engine or rules language.

Scope:

Applies to dynamic targeting, event-triggered responses, escalation, analytics-derived flows, and feedback loops.

Non-goals:

Does not decide trigger timing, real-time semantics, rule expressiveness, notification channels, or automated work creation.

Forbidden interpretations:

- Do not assume all triggered action is real-time.
- Do not infer an unbounded rules engine from the behavior.

Open edges:

Trigger boundaries and mechanism remain open until viability, exploration, and ADR sources close them.

Platform specification note:

Use as behavioral evidence for reactive work while preserving configuration-boundary caution.

## Kernel: Cross-Reference Behavior

Status: Settled
Kind: invariant

Specification statement:

Separate activities may be related because understanding one requires context from another. The connection should be visible without forcing independent activities into one rigid process.

Source basis:

- `docs/behavioral_patterns.md` / `P10 — Cross-Reference`
- Scenario evidence: S08, S13, S18, S22

Closure basis:

Settled as a behavioral requirement. Not closed as reference fields, links, joins, projections, or relationship contracts.

Scope:

Applies to case context, supply-to-campaign context, prior audit findings, analytics-derived relationships, and related but independent flows.

Non-goals:

Does not decide link representation, referential integrity, activity identity, or coupling rules.

Forbidden interpretations:

- Do not force related activities into a single workflow solely because they are connected.
- Do not hide meaningful operational relationships between otherwise independent activities.

Open edges:

Cross-reference representation and interaction rules remain open until later sources close them.

Platform specification note:

Use as behavioral evidence for explicit but non-rigid linking semantics.

## Kernel: Shape Evolution Behavior

Status: Settled
Kind: conditional-validity

Specification statement:

The expected structure of information may change over time. Old records remain valid under the shape that was active when captured, while new work may follow the updated shape.

Source basis:

- `docs/behavioral_patterns.md` / `P11 — Shape Definition and Evolution`
- Scenario evidence: S00, S06

Closure basis:

Settled as a behavioral requirement. Not closed as schema versioning, shape registry, migration, or validation mechanism.

Scope:

Applies to fixed and evolving expected information structures, old/new record coexistence, and records from different periods.

Non-goals:

Does not decide whether shape changes are migrated, versioned, projected, or read through compatibility layers.

Forbidden interpretations:

- Do not invalidate older records because the expected structure changed.
- Do not require all old and new records to share the same current shape.

Open edges:

Shape/version mechanics remain open until later sources close them.

Platform specification note:

Use as behavioral evidence for later configuration and record-shape contracts.

## Kernel: Offline-First Work Behavior

Status: Settled
Kind: invariant

Specification statement:

Meaningful work happens without connectivity. Records, decisions, and state progression may be created locally and later reconciled with shared state when connectivity returns.

Source basis:

- `docs/behavioral_patterns.md` / `P12 — Offline-First Work`
- Scenario evidence: S19 and cross-cutting pressure across field scenarios, including S22

Closure basis:

Settled as a behavioral requirement. Not closed as sync protocol, local storage, conflict detection, or reconciliation model.

Scope:

Applies to field capture, decisions, stale local state, conflicting offline work, sync arrival, and cross-user reconciliation.

Non-goals:

Does not decide offline feature subset, conflict policy, data replication strategy, or user-visible sync behavior.

Forbidden interpretations:

- Do not require connectivity for field work.
- Do not assume local and central state are always current with each other.
- Do not equate sync arrival time with work time.

Open edges:

Offline storage, sync, ordering, and reconciliation mechanics remain open until later sources close them.

Platform specification note:

Use as behavioral evidence for offline-first architecture decisions without preselecting those decisions.

## Kernel: Behavioral Composition Without New Patterns

Status: Settled
Kind: conditional-validity

Specification statement:

The Phase 1 scenario set decomposes into the behavioral pattern catalog without requiring new patterns, and composite scenarios decompose into the same atomic behavioral patterns as their constituent scenarios.

Source basis:

- `docs/behavioral_patterns.md` / `## 4. Scenario Decomposition Table`
- `docs/behavioral_patterns.md` / `## 6. Validation`
- Scenario evidence: S00-S14, S19, S20, S21, with S22 providing an additional composite core scenario in the scenario index.

Closure basis:

Settled as behavioral validation. Not closed as architectural primitive sufficiency.

Scope:

Applies to behavioral decomposition and cross-scenario reuse.

Non-goals:

Does not prove any particular architecture, primitive set, implementation phase, or platform module boundary.

Forbidden interpretations:

- Do not treat "no new behavioral pattern" as "no new technical mechanism."
- Do not treat composite scenario decomposition as evidence that every implementation detail is already closed.

Open edges:

Technical sufficiency and architecture closure remain to be established by viability, exploration, and ADR sources.

Platform specification note:

Use as a domain-level sufficiency claim for behavioral coverage, not as a final technical specification.

## Kernel: Conditional Platform Viability

Status: Settled
Kind: conditional-validity

Specification statement:

The platform approach is viable as a conditional go: the Phase 1 scenario set, offline cross-cut, access-control concern, constraints, and vision form a coherent enough problem space to proceed into architecture exploration, provided specific scope and boundary conditions are respected.

Source basis:

- `docs/viability-assessment.md` / `## Executive Summary`
- `docs/viability-assessment.md` / `### Recommendation: CONDITIONAL GO`

Closure basis:

Settled as a pre-architecture viability verdict. Not settled as final architecture.

Scope:

Applies to the decision to proceed from domain/scenario analysis into architecture exploration.

Non-goals:

Does not decide technical architecture, primitives, implementation sequence, or platform specification content.

Forbidden interpretations:

- Do not treat conditional go as unconditional architecture approval.
- Do not treat the viability assessment as final ADR closure.

Open edges:

The named conditions and risks must be closed or bounded by later exploration and ADRs.

Platform specification note:

The platform specification should inherit the viability conditions as historical constraints only where later sources confirm or refine them.

## Kernel: No Hard Scenario Conflicts

Status: Settled
Kind: conditional-validity

Specification statement:

The approved scenario set contains no hard conflicts that make a unified platform impossible. The observed conflicts are design trade-offs and tensions, not contradictions that invalidate the platform concept.

Source basis:

- `docs/viability-assessment.md` / `## 3. Conflict Check`

Closure basis:

Settled as a viability finding over the scenario and domain-ground-truth set.

Scope:

Applies to scenario coherence and the decision to continue architecture exploration.

Non-goals:

Does not prove any particular technical solution or eliminate implementation risk.

Forbidden interpretations:

- Do not treat absence of hard conflicts as proof that every scenario should drive initial architecture equally.
- Do not ignore documented tensions because no hard conflict was found.

Open edges:

Each tension still requires technical closure or explicit deferral.

Platform specification note:

Use as context for why the platform can be specified as one coherent system, while preserving documented tensions.

## Kernel: Configuration Boundary Collapse Risk

Status: Settled
Kind: configuration-boundary

Specification statement:

The line between "set up" and "built" is the most architecturally significant viability risk. If configuration becomes too expressive, it collapses into a programming language and undermines the platform promise.

Source basis:

- `docs/viability-assessment.md` / `T2: Configuration simplicity vs. expressive power`
- `docs/viability-assessment.md` / `Risk Flags` / `R1`
- `docs/viability-assessment.md` / `Condition 1 — Configuration boundary first`

Closure basis:

Settled as a high-severity viability risk and architecture-entry condition. Not closed as the final configuration model.

Scope:

Applies to shape evolution, dynamic targeting, event-triggered actions, multi-audience views, analytics-derived flows, and any future deployer-authored logic.

Non-goals:

Does not decide the layers, syntax, rule language, admin UI, extension model, or deployer capabilities.

Forbidden interpretations:

- Do not infer unlimited deployer-authored logic from "set up, not built."
- Do not hide software development inside a configuration language.

Open edges:

The concrete configuration boundary remains to be closed by exploration and ADRs.

Platform specification note:

The platform specification must make the configuration boundary explicit and should preserve this risk as the reason the boundary exists.

## Kernel: Offline Reactivity Eventual-Consistency Tension

Status: Settled
Kind: conditional-validity

Specification statement:

Reactive behavior and timely response must be reconciled with offline-first work. In the viability assessment, triggers and oversight reactions are framed as eventually consistent: they can occur when information becomes centrally visible, not necessarily when the field observation happened.

Source basis:

- `docs/viability-assessment.md` / `T1: Offline-first vs. real-time reactivity`

Closure basis:

Settled as a viability tension and direction. Not closed as a trigger mechanism or timing contract.

Scope:

Applies to dynamic targeting, event-triggered actions, emergency response pressure, oversight delay, and sync-mediated visibility.

Non-goals:

Does not decide trigger timing, notification channels, sync protocol, or whether later ADRs preserve this exact mechanism.

Forbidden interpretations:

- Do not assume field-recorded events can always trigger immediate central action.
- Do not design reactive behavior as inherently real-time unless later authoritative sources close that.

Open edges:

Reactive semantics and sync/freshness contracts remain to be closed by later sources.

Platform specification note:

The platform specification should treat real-time reactivity as constrained by offline-first operation unless later decisions introduce bounded exceptions.

## Kernel: Domain Mechanism Content Separation

Status: Settled
Kind: configuration-boundary

Specification statement:

Domain-agnosticism is viable if the platform provides mechanisms while deploying organizations provide domain content: what conditions mean, which thresholds matter, what "resolved" means, and which validations apply.

Source basis:

- `docs/viability-assessment.md` / `T3: Domain-agnosticism vs. domain-specific validation`

Closure basis:

Settled as a viability direction. Not closed as a specific configuration, validation, or rule model.

Scope:

Applies to case management, dynamic targeting, event-triggered actions, domain-specific validation, and business rules.

Non-goals:

Does not decide rule expressiveness, validation language, domain content schema, or which rules require code.

Forbidden interpretations:

- Do not hard-code domain content into platform mechanisms.
- Do not claim domain-agnosticism requires the platform to know no domain-specific content at deployment time.

Open edges:

The mechanism/content boundary remains to be closed by later sources.

Platform specification note:

The platform specification should separate platform-owned mechanism from deployer-provided domain content.

## Kernel: Trustworthy Records Offline Correction Tension

Status: Settled
Kind: conditional-validity

Specification statement:

Traceable records and offline correction create a real trade-off: preserving full history and surfacing conflicts adds operational complexity, but silent overwrite or erasure would violate the trustworthy-records promise.

Source basis:

- `docs/viability-assessment.md` / `T4: Trustworthy records vs. offline correction`
- `docs/scenarios/00-basic-structured-capture.md` / `## What makes this hard`

Closure basis:

Settled as a viability tension. Not closed as append-only storage, event sourcing, or conflict-resolution policy.

Scope:

Applies to corrections, offline arrival order, conflicting corrections, trustworthy records, and human judgment pressure.

Non-goals:

Does not decide storage mutability, conflict categories, flagging, or resolution workflow.

Forbidden interpretations:

- Do not silently merge or overwrite where traceability is required.
- Do not minimize the operational complexity introduced by full-history preservation.

Open edges:

Write model and conflict behavior remain to be closed by exploration and ADRs.

Platform specification note:

The platform specification should preserve this tension as the motivation for whatever record-history and conflict semantics are later closed.

## Kernel: Phase 2 Deferral Guardrail

Status: Settled
Kind: conditional-validity

Specification statement:

Scenarios 15, 16, and 18 are compatible with the platform ambition but should remain deferred as initial architecture drivers because they introduce view-composition, crisis-authority, and analytics-infrastructure pressures that can distort the Phase 1 core.

Source basis:

- `docs/viability-assessment.md` / `Executive Summary`
- `docs/viability-assessment.md` / `Condition 3 — Keep Phase 2 deferred`
- `docs/scenarios/README.md` / `## Phasing`

Closure basis:

Settled as a viability guardrail. Not settled as permanent exclusion or final platform scope.

Scope:

Applies to multi-audience views, emergency response, analytics-derived flows, authority override, and analytics-driven initiation.

Non-goals:

Does not decide that deferred scenarios are invalid, out of scope forever, or unsupported by future platform evolution.

Forbidden interpretations:

- Do not let deferred scenarios drive initial core architecture.
- Do not design the Phase 1 core around crisis mode or analytics infrastructure.
- Do not actively block future compatibility unless later sources close that trade-off.

Open edges:

Future support and exact compatibility constraints remain to be closed by later sources.

Platform specification note:

The platform specification should distinguish core constraints from deferred extension pressures.

## Kernel: Setup Experience Blind Spot

Status: Open
Kind: open-question

Specification statement:

The setup/configuration experience is the least-tested core promise in the scenario set. Scenarios describe field operations, not the administrator's experience of setting up an operational activity.

Source basis:

- `docs/viability-assessment.md` / `Vision Guarantee Coverage` / `V2`
- `docs/viability-assessment.md` / `Blind spot 1: The setup/configuration experience`

Closure basis:

Open as a viability blind spot.

Scope:

Applies to administrator setup, activity configuration, setup usability, and the credibility of "set up, not built."

Non-goals:

Does not decide the admin UI, configuration layers, or authoring workflow.

Forbidden interpretations:

- Do not treat setup UX as already scenario-proven.
- Do not let field-operation scenarios alone prove the setup promise.

Open edges:

Setup experience evidence and configuration authoring semantics remain open.

Platform specification note:

The platform specification should avoid overstating setup usability until later sources close the boundary and authoring model.

## Kernel: Retention And Archival Blind Spot

Status: Open
Kind: open-question

Specification statement:

The domain ground truth establishes large, accumulating records, but no scenario directly addresses archival, retention policy, or the operational experience of years of accumulated data.

Source basis:

- `docs/viability-assessment.md` / `Blind spot 2: Data archival and retention`
- `docs/constraints.md` / `## Scale`

Closure basis:

Open as a viability blind spot.

Scope:

Applies to old records, retention policy, archival, compliance, performance, local storage, and long-running deployments.

Non-goals:

Does not decide deletion, summarization, local eviction, central retention, or archive access.

Forbidden interpretations:

- Do not assume data lifecycle is solved by the scenarios.
- Do not ignore accumulated-data pressure on low-end devices.

Open edges:

Retention and archival mechanics remain open.

Platform specification note:

The platform specification should mark data lifecycle as unresolved unless later decisions close it.

## Kernel: Onboarding And Role Transition Blind Spot

Status: Open
Kind: open-question

Specification statement:

User onboarding and role-transition operations are not covered by a dedicated scenario, even though access-control requirements mention promotion, transfer, leave, coverage, and responsibility handoff.

Source basis:

- `docs/viability-assessment.md` / `Blind spot 3: User onboarding and role transitions`
- `docs/access-control-scenario.md`

Closure basis:

Open as a viability blind spot.

Scope:

Applies to onboarding, transfer, leave, role changes, responsibility handoff, and lifecycle events for workers.

Non-goals:

Does not decide actor lifecycle, assignment lifecycle, HR integration, or handoff workflow.

Forbidden interpretations:

- Do not assume role lifecycle UX is scenario-proven.
- Do not erase responsibility continuity when roles change.

Open edges:

Actor and responsibility lifecycle mechanics remain open.

Platform specification note:

The platform specification should avoid overclaiming role lifecycle coverage until later closure.

## Kernel: Reporting Aggregation Blind Spot

Status: Open
Kind: open-question

Specification statement:

The scenarios cover capture, oversight, and coordination, but do not directly cover decision-maker aggregation such as cross-district totals or monthly resolved-case counts. Scenario 15 is related but deferred.

Source basis:

- `docs/viability-assessment.md` / `Blind spot 4: Reporting and aggregation for decision-makers`

Closure basis:

Open as a viability blind spot.

Scope:

Applies to aggregate reporting, decision-maker views, summaries, and cross-scope metrics.

Non-goals:

Does not decide analytics, dashboards, materialized views, reporting schema, or Phase 1 inclusion.

Forbidden interpretations:

- Do not assume detailed aggregation requirements are covered by capture scenarios.
- Do not silently pull deferred multi-audience view complexity into the core.

Open edges:

Aggregation scope and reporting semantics remain open.

Platform specification note:

The platform specification should distinguish operational oversight from analytical aggregation unless later sources close them together.

## Kernel: Domain-Agnosticism Proof Gap

Status: Open
Kind: open-question

Specification statement:

Core scenarios are domain-pure, but composite validation examples are health-oriented. Domain-agnosticism is plausible but not fully proven by a second non-health composite scenario in the ground-truth set.

Source basis:

- `docs/viability-assessment.md` / `Vision Guarantee Coverage` / `V6`
- `docs/viability-assessment.md` / `Risk Flags` / `R4`

Closure basis:

Open as a low-medium viability proof gap.

Scope:

Applies to domain-neutral core terminology, cross-domain validation, and risk of implicit health-domain assumptions.

Non-goals:

Does not reject the platform's domain-agnostic ambition.

Forbidden interpretations:

- Do not bake health-domain assumptions into platform core because composite examples are health-oriented.
- Do not treat absence of a second-domain composite as a hard contradiction.

Open edges:

Additional non-health validation remains useful for confidence.

Platform specification note:

The platform specification should keep core terms domain-neutral and cite domain-specific examples only as examples.

## Kernel: Low-End Device Scale Risk

Status: Settled
Kind: conditional-validity

Specification statement:

Scale on low-end devices is a medium viability risk: millions of records, low-end Android phones, offline operation, and years of accumulated data can exceed local storage or query performance if not addressed explicitly.

Source basis:

- `docs/viability-assessment.md` / `Risk Flags` / `R5`
- `docs/constraints.md` / `## Scale`

Closure basis:

Settled as a viability risk. Not closed as a selective-sync, local lifecycle, archive, or performance design.

Scope:

Applies to local storage, query performance, selective data availability, accumulated records, and field-device constraints.

Non-goals:

Does not decide local database design, sync filtering, archive strategy, or performance budgets.

Forbidden interpretations:

- Do not assume every device can hold the full deployment dataset.
- Do not ignore local lifecycle just because central records can accumulate.

Open edges:

Selective sync, local lifecycle, and performance design remain to be closed by later sources.

Platform specification note:

The platform specification should include low-end device and scale constraints wherever it defines local data availability.

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
