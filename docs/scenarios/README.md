# Scenarios

## Purpose

These scenarios describe **real-world situations** that the platform must be able to support. They are written from the perspective of the people and organizations doing the work — not from the perspective of the system.

Each scenario describes what happens, what results from it, and why it matters — without prescribing how the platform should implement it. This separation is deliberate: the scenarios define the **problem space**, while architecture, domain decomposition, and construct definitions belong in separate analysis layers.

---

## Ordering Rationale

The scenarios follow a natural progression of operational complexity:

**Recording things** → **Recurring obligations** → **Oversight and judgment** → **Coordination across people and places** → **Reactive and emergent work**

This progression supports incremental reasoning — each scenario adds real-world complexity without jumping to architectural conclusions.

---

## Index

| # | Title | What it describes |
|---|-------|-------------------|
| [00](00-basic-structured-capture.md) | Recording Structured Information | Someone captures a known set of details about something |
| [01](01-entity-linked-capture.md) | Recording Information About a Specific Thing | Records tied to an identifiable real-world subject |
| [02](02-periodic-reporting.md) | Regular, Recurring Reporting | Information expected on a recurring rhythm |
| [03](03-user-based-assignment.md) | Designated Responsibility | Specific people accountable for specific things |
| [04](04-supervisor-review.md) | Review and Judgment by Another Person | Work reviewed before it's considered final |
| [05](05-supervision-audit-visits.md) | Periodic Visits and In-Person Assessment | Planned visits to observe and assess work |
| [06](06-entity-registry-lifecycle.md) | Maintaining a Known Set of Things / Evolving Information Shape | Tracking things over time; changing what's collected |
| [07](07-resource-distribution.md) | Handing Things Off and Confirming Receipt | Transfers between parties with acknowledgment |
| [08](08-case-management.md) | Following Something Over Time Until Resolution | Ongoing situations requiring multiple interactions |
| [09](09-coordinated-campaign.md) | A Planned, Time-Bound Effort Across Many Places | Coordinated work across locations within a window |
| [10](10-dynamic-targeting.md) | Work That Depends on Changing Conditions | Action driven by observed conditions, not fixed plans |
| [11](11-multi-step-approval.md) | Work Passing Through Multiple Levels of Judgment | Sequential review by multiple people |
| [12](12-event-triggered-actions.md) | When Something Happens, Something Else Should Follow | Consequential responses to observations |
| [13](13-cross-flow-linking.md) | When Separate Activities Are Related | Connections between independent activities |
| [14](14-multi-level-distribution.md) | Moving Things Through a Chain of Responsibility | Multi-level movement with handoffs and tracing |
| [15](15-cross-program-overlays.md) | The Same Work Serving Multiple Audiences | Multiple stakeholders viewing the same work differently |
| [16](16-emergency-rapid-response.md) | Responding to an Unexpected Crisis | Rapid mobilization under evolving, urgent conditions |
| [18](18-advanced-analytics-derived-flows.md) | When Patterns in Past Work Trigger New Work | Analysis-driven initiation of new efforts |
| [19](19-offline-capture-and-sync.md) | Working Without Connectivity | Doing meaningful work offline and reconciling later |
| [20](20-chv-field-operations.md) | Community Health Volunteer Field Work | A CHV's day-to-day work in the community |
| [21](21-chv-supervisor-operations.md) | Supervisor Visit and Assessment | A supervisor visiting a volunteer to review their work |
| [22](22-coordinated-distribution-campaign-across-grouped-locations.md) | Coordinated Work Across Grouped Locations | Visiting units within locations during a time-bound effort with supply flow |

---

## Phasing

### Phase 1 — Core
Scenarios 00–14, 22. These describe a set of operational situations that share enough common ground that a unified platform approach clearly makes sense.

### Phase 2 — Extensions (Deferred)
Scenarios 15, 16, 18. These describe situations that are compatible with the core but introduce specialized operational demands that should not drive initial decisions.

### Cross-Cutting
Scenario 19 (working without connectivity) is not a separate operational situation — it's a constraint that applies to many of the scenarios above. It describes the reality that the people doing the work in scenarios 00–14 often do so without reliable connectivity.

### Composite Real-World Scenarios
Scenarios 20 and 21 describe complete, real-world operational contexts (a CHV's field day, a supervisor visit) that naturally touch multiple scenarios from the core set. They exist to validate that the platform's eventual design can support a coherent real-world workflow, not just individual isolated capabilities.

---

## Architectural Significance

Not all Phase 1 scenarios carry equal weight for architecture. Some force foundational decisions that constrain everything built on top of them. Others compose from those foundations and validate that the constraints work in combination.

### Foundational

These scenarios force decisions that shape the platform's core structure. They must be addressed first in architecture exploration.

- **00 — Basic structured capture**: The simplicity baseline. Whatever the platform's core recording primitive is, it must handle this cleanly.
- **01 — Entity-linked capture**: Introduces identity — tying records to recognizable, persistent subjects. Forces decisions about how things are identified and how identity survives ambiguity.
- **06 — Registry lifecycle + schema evolution**: Introduces mutability — things change, the shape of information changes, and both must be handled without breaking existing records.
- **19 — Offline capture and sync**: The hardest constraint. Everything that works online must also work offline, and reconciliation on reconnect must handle conflicts, ordering, and stale state.
- **Access control** (cross-cutting): Authority and visibility intersect every scenario. The rules must be locally enforceable (offline) and eventually consistent (on sync).

### Structurally important

These scenarios test specific patterns that architecture must accommodate but do not individually force foundational choices.

- **02 — Periodic reporting**: Time as a structural dimension — recurring obligations, cadence, deadlines.
- **03 — Assignment**: Responsibility — binding specific people to specific work.
- **04 — Review**: Judgment — one person's work subject to another's assessment.
- **07 — Distribution**: Handoffs — transfer of things between parties with acknowledgment.
- **08 — Case management**: Long-running state — something tracked across multiple interactions until resolution.

### Compositional

These scenarios compose from the above. They validate that foundational and structural decisions work in combination.

- **05, 09, 10, 11, 12, 13, 14**: Progressively complex compositions of recording, assignment, oversight, coordination, and reactive patterns.
- **22**: Composes subjects (parent–child), assignment, structured capture, transfer-with-acknowledgment, and L1 projections into a scoped-iteration pattern. See [ITN walk-through](../walk-throughs/itn-distribution-campaign.md) for a concrete domain example.

---

## Pre-Architecture Gate

The pre-architecture phase is complete when all of the following hold:

1. The [constraints document](../constraints.md) exists and covers target users, connectivity profiles, scale ranges, data sensitivity, interoperability boundaries, and responsiveness expectations.
2. Foundational scenarios (00, 01, 06, 19) include "what makes this hard" sections with domain-pure edge cases and failure modes.
3. The [access control document](../access-control-scenario.md) grounds its principles against specific scenario tensions.
4. No known contradictions exist between scenarios, constraints, and vision — or contradictions are explicitly documented as tensions to resolve during architecture.
5. `/viability` has been run against the complete foundation and returned GO or CONDITIONAL GO.

### Governing principles

These principles govern the gate and the transition into architecture exploration:

- **Phase-gate discipline**: Do not advance into architecture until the criteria above are met. Evidence, not optimism.
- **Evidence over claims**: Completeness is demonstrated by specific content (thickened scenarios, grounded access control), not by assertions that the work is "done."
- **Context continuity**: Everything produced in this phase — constraints, scenario tensions, access control grounding — carries forward as explicit input to `/viability` and `/ade`. No phase transition drops context.

---

## What These Scenarios Are Not

These scenarios deliberately avoid:

* **Naming platform constructs** — no "entities", "submissions", "forms", "state machines", "schemas", or "flows"
* **Decomposing into capabilities** — no "what this introduces" sections that pre-decide what the platform needs to provide
* **Prescribing architecture** — no sync protocols, conflict resolution strategies, or notification channels

That decomposition happens in the **decision exploration** and **construct definition** layers, informed by these scenarios but not contained within them.
