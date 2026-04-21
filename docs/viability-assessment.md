# Platform Viability Assessment

> Generated: 2026-04-05
> Inputs: docs/README.md (Vision), docs/constraints.md (Operational Context), docs/scenarios/ (21 scenarios), docs/access-control-scenario.md (Cross-cutting)

## Executive Summary

The Datarun platform concept — a shared, configurable operational backbone for field-based organizations — holds up well under scrutiny. The 21 scenarios describe a coherent problem space, not a grab-bag. A small set of recurring primitives (structured records, identifiable subjects, responsibility bindings, temporal rhythms, state progression, hierarchical visibility) composes across the full scenario spectrum. The vision's five commitments are all load-bearing: every one is exercised by multiple scenarios, and the hardest constraint (offline-first) genuinely pervades the entire problem space rather than being a bolted-on concern.

There are no hard conflicts between scenarios. The tensions that exist — real-time reactivity vs. offline-first, configuration simplicity vs. expressive power, domain-agnosticism vs. domain-specific validation — are genuine design trade-offs, not fatal contradictions. Each is resolvable through well-understood patterns (eventual consistency, layered configuration, pluggable validation).

The recommendation is **CONDITIONAL GO**. The platform approach is sound for Phase 1 scenarios (00–14) plus the offline cross-cut (19). Three conditions apply: (1) the configuration boundary — what is "set up" vs. what requires development — must be the first architectural decision, as it determines the platform's actual expressive power; (2) scenario 12 (event-triggered actions) needs careful scoping to avoid becoming an unbounded rules engine; (3) scenarios 15, 16, and 18 should remain deferred — they introduce pressures (multi-tenant views, crisis-mode authority override, analytics-driven initiation) that would distort initial architecture if included prematurely.

---

## 1. Vision Guarantees Extracted

From `docs/README.md`, six distinct promises:

| # | Guarantee | Core claim |
|---|-----------|------------|
| V1 | **Works without connectivity** | Field work happens offline; sync reconciles independently recorded work |
| V2 | **Set up, not built** | New operational activities are configured, not developed |
| V3 | **Trustworthy records** | Every action traceable: who, what, when, role, context; records survive schema evolution |
| V4 | **One system, not many** | All operational work feels like the same platform with consistent patterns |
| V5 | **Grows without breaking** | Adding complexity doesn't require rethinking what exists |
| V6 | **Domain-agnostic** | Works across health, logistics, agriculture, humanitarian response (implicit in ambition) |

---

## 2. Component Abstraction Analysis

### 2a. Candidate Primitives

Across all 21 scenarios, the following recurring building blocks emerge:

| Primitive | Description | Scenarios that need it | Frequency |
|-----------|-------------|----------------------|-----------|
| **Structured Record** | A captured set of details conforming to a defined shape | 00, 01, 02, 05, 07, 08, 09, 10, 12, 13, 14, 20, 21 | 13/21 |
| **Identifiable Subject** | A persistent, recognizable real-world thing that records attach to | 01, 03, 05, 06, 08, 10, 13, 14, 20, 21 | 10/21 |
| **Responsibility Binding** | A link between a person and the things/areas they are accountable for | 02, 03, 04, 05, 07, 08, 09, 11, 14, 20, 21 | 11/21 |
| **Temporal Rhythm** | Expected periodicity — recurring obligations, deadlines, windows | 02, 05, 06, 09, 15, 20, 21 | 7/21 |
| **State Progression** | Work that moves through stages (open → reviewed → final; sent → received → confirmed) | 04, 07, 08, 11, 14, 16 | 6/21 |
| **Hierarchical Visibility** | Different people see different scopes based on their position | 03, 04, 05, 09, 11, 14, 15, 21 | 8/21 |
| **Review / Judgment** | One person assesses another's work and records a verdict | 04, 05, 11, 21 | 4/21 |
| **Transfer / Handoff** | Movement of things between parties with acknowledgment | 07, 14, 20 | 3/21 |
| **Condition-Based Trigger** | An observation or threshold causes a consequential action | 10, 12, 18 | 3/21 |
| **Cross-Reference / Link** | A connection between otherwise independent records or activities | 08, 13, 18 | 3/21 |
| **Schema / Shape Definition** | The expected structure of information to collect, which can evolve | 00, 06, 15 | 3/21 |
| **Offline Local State** | A complete working copy that operates independently of the server | 19 (cross-cutting, applies to most) | 1 explicit / ~14 implicit |

### 2b. Natural Groupings

**Group A — Identity and Registry** (subjects, their lifecycle, schema evolution)
- Primitives: Identifiable Subject, Schema/Shape Definition
- Core scenarios: 01, 06
- Supporting scenarios: 03, 05, 08, 10, 13, 14, 20

**Group B — Record and Capture** (the act of recording structured information)
- Primitives: Structured Record, Offline Local State
- Core scenarios: 00, 19
- Supporting scenarios: nearly all — this is the most pervasive group

**Group C — Responsibility and Authority** (who does what, who sees what)
- Primitives: Responsibility Binding, Hierarchical Visibility
- Core scenarios: 03, access control cross-cut
- Supporting scenarios: 02, 04, 05, 07, 09, 11, 14, 15, 20, 21

**Group D — Workflow and State** (work that progresses through stages)
- Primitives: State Progression, Review/Judgment, Transfer/Handoff
- Core scenarios: 04, 07, 08, 11, 14
- Supporting scenarios: 05, 16, 21

**Group E — Time and Scheduling** (rhythms, deadlines, windows)
- Primitives: Temporal Rhythm
- Core scenarios: 02, 09
- Supporting scenarios: 05, 06, 15, 20, 21

**Group F — Reactivity** (when something happens, something else follows)
- Primitives: Condition-Based Trigger, Cross-Reference/Link
- Core scenarios: 10, 12, 18
- Supporting scenarios: 08, 13

**Observation**: Groups A through E are tightly interwoven — most scenarios need primitives from 3–4 groups simultaneously. Group F (Reactivity) is more loosely coupled; scenarios 10, 12, and 18 can be served by a reactive layer that sits on top of the other five groups rather than being entangled with them. This is a positive architectural signal: the core is composable, and the reactive layer is separable.

---

## 3. Conflict Check

### Hard Conflicts

**None found.** No scenario requires something that would make another scenario impossible on the same platform.

### Tensions

**T1: Offline-first vs. real-time reactivity (V1 vs. scenarios 10, 12, 16)**

Scenarios 10 (dynamic targeting), 12 (event-triggered actions), and 16 (emergency response) all describe situations where awareness and response should be timely. But V1 guarantees that field work happens offline. A field worker recording a critical value (sc. 12) cannot trigger an immediate notification to a supervisor if neither is connected.

**Resolution**: This is an eventual-consistency tension, not a hard conflict. Triggers fire when information becomes centrally visible (on sync), not when it is recorded. The platform must make the delay explicit ("triggered 3 hours after recording, when sync occurred") and the trigger rules must be designed for eventual, not real-time, semantics. Scenario 16 (emergency) is the stress case — but it is already in Phase 2 (deferred), and the constraints document confirms that oversight views are eventually consistent.

**T2: Configuration simplicity vs. expressive power (V2 vs. scenarios 06b, 10, 12, 15, 18)**

V2 promises that operational activities are "set up, not built." But scenarios like 06b (evolving information shape), 10 (condition-based targeting), 12 (event-triggered rules), 15 (multi-audience views), and 18 (analytics-derived flows) require increasingly expressive configuration. At what point does "configuration" become a domain-specific programming language? If it becomes too expressive, the "set up" promise is hollow — it's still built, just in a different language.

**Resolution**: This is the most architecturally significant tension in the entire scenario set. It must be the first `/ade` decision. The resolution likely involves a layered configuration model: a simple layer for common patterns (Phase 1 core), a more expressive layer for complex rules (Phase 1 upper), and explicit escape hatches for things that genuinely require development (Phase 2). The key is that the simple layer must serve the majority of real deployments.

**T3: Domain-agnosticism vs. domain-specific validation (V6 vs. scenarios 08, 10, 12)**

The platform is domain-agnostic, but some scenarios implicitly require domain knowledge. Case management (08) needs to know what "resolved" means. Dynamic targeting (10) needs to evaluate domain-specific conditions. Event triggers (12) need to match domain-specific patterns. If validation and business rules are domain-specific, how does a domain-agnostic platform enforce them?

**Resolution**: The platform provides the mechanism (configurable rules, condition evaluation, state definitions) while the deploying organization provides the domain content (what conditions mean "resolved," what thresholds trigger action). This is the standard "engine vs. content" separation. It is achievable but makes T2 (configuration expressiveness) the critical constraint.

**T4: Trustworthy records vs. offline correction (V3 vs. scenario 00 "what makes this hard")**

V3 guarantees traceable, trustworthy records. But scenario 00's edge cases describe corrections made offline to records that may have already been corrected by someone else. If two corrections arrive out of order, or a correction is made to a record whose original version hasn't synced yet, the audit trail becomes complex. The "trustworthy" guarantee means the platform cannot silently merge or overwrite — it must surface the full history, which adds operational complexity.

**Resolution**: This is a genuine trade-off between simplicity and integrity. The resolution is to always preserve the full history (append-only semantics at the record level) and surface conflicts as explicit items requiring human judgment. This is well-understood in offline-first system design (CRDT-adjacent approaches, operation logs). It adds UI complexity but does not create architectural impossibility.

### False Conflicts (Resolved)

**FC1: Single system (V4) vs. multiple concurrent activities (scale constraint)**

At first glance, running multiple operational activities simultaneously (each with its own information shape, assignments, and oversight) seems to pull toward separate systems. But this is exactly what V4 promises to resolve: same concepts, same contracts, different configured content. The primitives identified in Section 2 are the same across activities — only the configuration differs. Not a conflict; it's the core value proposition.

**FC2: Grows without breaking (V5) vs. schema evolution (scenario 06b)**

Schema evolution seems like it could break existing work. But scenario 06b already describes the requirement: old records stay valid under the old shape, new records follow the new shape, both coexist. V5 is directly tested by 06b, and the scenario's "what makes this hard" section (offline users with stale schemas) describes the stress case, not a contradiction.

---

## 4. Gap Analysis

### 4a. Vision Guarantee Coverage

| Guarantee | Scenarios that exercise it | Stress points | Blind spots |
|-----------|---------------------------|---------------|-------------|
| **V1: Offline-first** | 19 (explicit), 00–14 (implicit — constraints doc confirms all field work is offline) | Scenario 19 "what makes this hard": conflicting offline records, stale state, ordering fragility. Access control offline enforcement. | Well-covered. No blind spot. |
| **V2: Set up, not built** | Not directly tested by any scenario — scenarios describe field operations, not the setup experience | T2 (configuration expressiveness) is the stress point. Scenarios 06b, 10, 12 push configuration complexity highest. | **Significant blind spot**: no scenario describes the administrator's experience of setting up an operational activity. This is deferred to architecture (per the pre-architecture gate), but it remains the guarantee with the least scenario coverage. |
| **V3: Trustworthy records** | 00 (basic capture + corrections), 04 (review trail), 07 (transfer confirmation), 08 (case history), 11 (multi-level approval trail), 14 (chain-of-custody traceability) | Scenario 00 "what makes this hard": offline corrections arriving out of order. Scenario 19: conflicting records from disconnected users. | Covered across many scenarios. The "correction" and "offline conflict" edge cases are the hardest tests. |
| **V4: One system** | 09 (coordinated campaign composing capture + assignment + oversight), 13 (cross-flow linking), 15 (multi-audience views of same data), 20-21 (composite real-world workflows) | Scenario 15 pushes hardest: the same underlying data must be viewable through multiple stakeholder lenses without duplicating it. | Covered. Composite scenarios 20-21 are the most direct validation. |
| **V5: Grows without breaking** | 06b (schema evolution), access control ("rules can grow over time") | 06b "what makes this hard": schema changes intersecting with offline work. Access control: adding finer-grained rules without rebuilding existing ones. | The guarantee is exercised but only for schema and access rule evolution. Whether *structural* growth (adding new scenario types, new workflow patterns) breaks existing deployments is not tested — it's an architecture-level concern. |
| **V6: Domain-agnostic** | 00–14 are written without domain language. 20-21 are health-domain composites. | The absence of a second-domain composite means domain-agnosticism is asserted by the core scenarios' neutrality, not validated by showing the same primitives serve two different domains. | **Minor blind spot**: a logistics or agriculture composite scenario would strengthen confidence, but the core scenarios' domain-purity is strong evidence on its own. |

### 4b. Scenario Alignment

| Scenario | Vision alignment | Platform tax | Uniqueness risk |
|----------|-----------------|-------------|----------------|
| 00 — Basic capture | Perfect fit. The simplest possible expression of the platform's core function. | None — this IS the platform's reason to exist. | None |
| 01 — Entity-linked capture | Perfect fit. Identity is a foundational platform primitive. | None | None |
| 02 — Periodic reporting | Strong fit. Time-based obligations are a universal operational pattern. | Low — the temporal rhythm primitive serves this directly. | None |
| 03 — Assignment | Strong fit. Responsibility binding is universal. | None | None |
| 04 — Review | Strong fit. Judgment/review is a common operational layer. | Low | None |
| 05 — Supervision visits | Strong fit. Composes from assignment + capture + temporal rhythm + review. | Low — clean composition of existing primitives. | None |
| 06 — Registry + schema evolution | Strong fit but architecturally heavy. Schema evolution is essential but the hardest "grows without breaking" test. | Medium — schema evolution adds significant infrastructure complexity. This is not avoidable; it's an inherent cost of V5. | None — schema evolution is used by many scenarios implicitly. |
| 07 — Distribution/handoff | Strong fit. Transfer with confirmation is a clean state-progression pattern. | Low | None |
| 08 — Case management | Strong fit. Long-running state with multiple interactions is a universal operational pattern. | Medium — case management requires richer state modeling than simple capture-and-review. But it composes from state progression + identity + responsibility. | None |
| 09 — Coordinated campaign | Strong fit. The platform's "one system" promise is tested here: assignment + capture + temporal rhythm + hierarchical visibility all at once. | Low — clean composition, no unique primitives needed. | None |
| 10 — Dynamic targeting | Moderate fit. Condition-based work emergence is a real operational need, but the expressiveness required for condition evaluation approaches T2 (configuration vs. programming). | Medium — the condition evaluation mechanism needs careful scoping. | **Watch**: the specific condition language is the T2 stress point. |
| 11 — Multi-step approval | Strong fit. Composes from review + state progression + hierarchical visibility. | Low | None |
| 12 — Event-triggered actions | Moderate fit. Reactive patterns are valuable but the rule engine risk is real (T2). | **Medium-high** — without scoping, this becomes an unbounded rules engine. Needs explicit boundaries on what can trigger what. | **Watch**: this is the scenario most likely to pull the platform toward over-engineering. |
| 13 — Cross-flow linking | Strong fit. Cross-referencing between activities is a natural platform capability. | Low — the cross-reference primitive is simple. | None |
| 14 — Multi-level distribution | Strong fit. Composes from handoff + hierarchical visibility + state progression. | Low | None |
| 15 — Cross-program overlays (Phase 2) | Moderate fit. Multi-audience views of the same data are a real need, but the "different timelines, different completeness criteria" aspect adds significant complexity. | Medium — view composition and stakeholder-specific filtering require a different kind of abstraction than capture and workflow. | Low — the underlying primitives are the same, it's the projection layer that's new. |
| 16 — Emergency response (Phase 2) | Weak fit for initial architecture. Crisis mode requires authority overrides, accelerated processes, and ad-hoc scope changes that break normal patterns. | **High** — accommodating crisis mode distorts the authority and workflow models if included from the start. | **Watch**: this is correctly deferred. |
| 18 — Analytics-derived flows (Phase 2) | Weak fit for initial architecture. Pattern detection and automated work initiation require an analytics/ML layer that is fundamentally different from the operational core. | **High** — analytics infrastructure is a separate concern. | **Watch**: correctly deferred. The feedback loop aspect is unique to this scenario. |
| 20 — CHV field work (composite) | Perfect fit. Validates that core primitives compose into a real workday. | None | N/A — validation scenario |
| 21 — Supervisor visit (composite) | Perfect fit. Validates the oversight layer. | None | N/A — validation scenario |

### 4c. Blind Spots and Missing Scenarios

**Blind spot 1: The setup/configuration experience (V2)**
No scenario describes the administrator who configures a new operational activity. This is the most architecturally consequential blind spot because V2 ("set up, not built") is a core promise. However, this is correctly identified as an architecture-phase concern in the pre-architecture gate, not a scenario-phase gap. The configuration boundary is where architecture exploration should begin.

**Blind spot 2: Data archival and retention**
Records accumulate (millions in a large deployment, per the constraints doc). No scenario addresses what happens to old data — archival, retention policies, or the operational experience of working with a system that contains years of accumulated records. This matters for performance (query over millions of records on a low-end phone) and compliance (retention mandates).

**Blind spot 3: User onboarding and role transitions**
The access control document mentions role changes ("people get promoted, transferred, or go on leave") but no scenario describes the operational experience of onboarding a new field worker, handing off responsibility during a transfer, or managing the transition when someone leaves. This affects how the responsibility binding primitive handles lifecycle events.

**Blind spot 4: Reporting and aggregation for decision-makers**
Scenarios describe capture, oversight, and coordination, but none describes the experience of a decision-maker who needs aggregated views: "how many cases were resolved this month across all districts?" Scenario 15 (multi-audience views) is the closest, but it's deferred to Phase 2. Aggregation may need to be a Phase 1 concern if coordinators rely on it for basic operational decisions.

**Not a blind spot**: Integration/interoperability is captured as a constraint, not a scenario. This is the right framing for Phase 1.

---

## 5. Viability Verdict

### Platform Coherence: 4/5

The scenarios form a strongly coherent platform. The 12 primitives identified in Section 2 compose cleanly across the Phase 1 core (00–14). Groups A through E (Identity, Record, Responsibility, Workflow, Time) are tightly interwoven in a way that strongly favors a unified platform over separate tools. Group F (Reactivity) is cleanly separable as a layer on top.

The reason this is 4 and not 5: scenarios 10 and 12 (condition-based targeting and event-triggered actions) push the configuration expressiveness boundary in a way that could fragment the platform's "set up, not built" promise if not carefully scoped. They are coherent with the rest but need tighter boundaries than the other scenarios.

### Vision-Reality Alignment: 4/5

Every vision guarantee is exercised by multiple scenarios. V1 (offline-first) is the most thoroughly tested — the constraints document and scenario 19's edge cases create a rigorous stress test. V3 (trustworthy records) is well-covered across capture, review, handoff, and approval scenarios. V4 (one system) is validated by the composite scenarios.

The reason this is 4 and not 5: V2 (set up, not built) is the least-tested guarantee. No scenario exercises it from the administrator's perspective. The promise is structural — it shapes what the platform IS — but the scenarios only describe what the platform DOES from the field perspective. This blind spot is known and deferred, but it remains the weakest link between vision and scenarios.

### Abstraction Feasibility: 4/5

The primitive set is small (12 candidates), the groupings are natural (6 groups), and the composition patterns are visible across scenarios. Most scenarios need 3–4 primitives from different groups, and no scenario requires a primitive that nothing else uses (no "one-off" machinery).

The reason this is 4 and not 5: Two composition challenges remain unresolved. First, the interaction between schema evolution (Group A) and offline local state (Group B) is the hardest abstraction problem — how does a device that has been offline for days handle a schema change that happened while it was disconnected? Second, the condition evaluation mechanism for scenarios 10 and 12 needs to be expressive enough to be useful but constrained enough to be configurable. These are solvable but non-trivial.

### Risk Flags

| # | Risk | Triggered by | Severity | Mitigation |
|---|------|-------------|----------|------------|
| R1 | **Configuration boundary collapse** — the line between "set up" and "built" is never clearly drawn, resulting in a configuration language that is effectively a programming language | Scenarios 06b, 10, 12; Tension T2 | **High** | Make this the first `/ade` decision. Define explicit tiers of configuration complexity. Accept that some things require development and provide clean extension points rather than trying to make everything configurable. |
| R2 | **Offline conflict resolution becomes unmanageable** — the append-only, surface-all-conflicts approach to offline reconciliation creates an overwhelming volume of conflicts that field supervisors cannot process | Scenario 19 "what makes this hard"; Tension T4 | **Medium** | Design conflict categories: auto-resolvable (last-write-wins for non-critical fields), human-required (conflicting decisions), and flagged (potential duplicates). Most conflicts should be auto-resolvable; only genuinely ambiguous ones should surface. |
| R3 | **Scenario 12 becomes a rules engine** — event-triggered actions, if unbounded, will attract feature requests that push it toward a general-purpose workflow engine, bloating the platform | Scenario 12; Tension T2 | **Medium** | Scope scenario 12 tightly: triggers fire on sync (not real-time), actions are limited to notifications and task creation (not arbitrary side effects), and the rule language is declarative, not Turing-complete. |
| R4 | **Domain-agnosticism is assumed, not proven** — all composite validation scenarios (20, 21) are from the health domain. The platform may have implicit health-domain assumptions baked into its primitives | Scenarios 20, 21; Guarantee V6 | **Low-medium** | Before finalizing primitive definitions during architecture, validate against at least one non-health domain (logistics distribution or agricultural extension). A second composite scenario is not required now, but a mental exercise against a different domain should happen during `/ade`. |
| R5 | **Scale on low-end devices** — millions of records, low-end Android phones, offline operation. The data volume that accumulates over years of deployment may exceed what local storage and query performance can handle on target devices | Constraints doc (scale + devices) | **Medium** | Architecture must address selective sync (devices only carry data relevant to their assignments, not the full dataset) and local data lifecycle (older records can be summarized or archived locally while remaining available centrally). This is a known pattern in offline-first systems but must be an explicit architectural concern. |

### Recommendation: CONDITIONAL GO

The platform approach is sound. Proceed to architecture exploration with the following conditions:

**Condition 1 — Configuration boundary first.** The first `/ade` session must address Tension T2: what is configurable, what requires development, and where the boundary sits. This is not one decision among many — it is THE decision that determines what the platform actually is.

**Condition 2 — Scope scenario 12 before architecture.** Event-triggered actions (scenario 12) must have explicit boundaries defined before it influences architectural decisions. Without scoping, it will pull the platform toward a general-purpose rules engine. Recommended boundaries: triggers fire on sync, actions limited to notifications and task creation, rule language is declarative.

**Condition 3 — Keep Phase 2 deferred.** Scenarios 15 (multi-audience views), 16 (emergency response), and 18 (analytics-derived flows) must remain deferred. Each introduces architectural pressures (view composition, authority override, analytics infrastructure) that would distort the Phase 1 core if accommodated from the start. The Phase 1 architecture should not actively prevent them, but should not be designed around them.

**What this means**: The Phase 1 scenario set (00–14, 19, access control), combined with the constraints document and the thickened foundational scenarios, provides sufficient input for architecture exploration. The primitive set is coherent, the vision is aligned, and the tensions are manageable. Begin with `/ade` on the configuration boundary, then proceed to domain composition and construct definitions.
