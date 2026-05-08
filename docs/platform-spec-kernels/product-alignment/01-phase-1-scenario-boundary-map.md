# Phase 1 Scenario Boundary Map

Status: Session 1 product-alignment artifact

This document groups Phase 1 scenarios by product experience boundary and operational tension. It is not an architecture rewrite and not a platform-spec atom. It prepares later product/UX artifacts by asking: what does the platform need to feel like to users across the Phase 1 problem space?

## Source Basis

Primary domain inputs:

- `../../README.md`
- `../../constraints.md`
- `../../access-control-scenario.md`
- `../../behavioral_patterns.md`
- `../../principles.md`
- `../../viability-assessment.md`
- `../../scenarios/README.md`
- Phase 1 scenarios: `../../scenarios/00-basic-structured-capture.md` through `../../scenarios/14-multi-level-distribution.md`, plus `../../scenarios/22-coordinated-distribution-campaign-across-grouped-locations.md`
- Cross-cutting scenario: `../../scenarios/19-offline-capture-and-sync.md`

Guardrails:

- `../professional-baseline/04-architecture-baseline-v0.md`
- `../professional-baseline/05-decision-gap-register.md`
- `../professional-baseline/07-system-boundary-map.md`
- `../professional-baseline/09-identity-boundary-control.md`
- `../professional-baseline/15-conflict-flag-offline-boundary-control.md`
- `../professional-baseline/16-operational-constraints-boundary-control.md`
- `../professional-baseline/17-authorization-visibility-boundary-control.md`
- `../pre-operations/04-accepted-pre-atomization-decisions.md`

Vocabulary decisions belong in `06-product-vocabulary-alignment.md`.

## Session 1 Finding

The Phase 1 scenarios are not a feature backlog. They describe a coherent operational product surface:

- people record structured facts while often offline
- people work against known subjects, places, areas, units, resources, and situations
- people have assigned responsibilities and scoped visibility
- work waits, ages, moves, gets reviewed, gets handed off, and sometimes triggers follow-up
- supervisors and coordinators need progress, bottleneck, and exception views
- configuration must let organizations set up work without turning the product into a programming environment
- the same product language must make sense for simple capture, periodic reporting, review, case follow-up, and multi-level distribution

The main product risk is not that the scenarios fail to compose. They do compose. The risk is exposing the architecture too literally and making users think in terms of events, projections, sync scopes, flags, and pattern machinery instead of work, history, responsibility, review, exceptions, and progress.

## Product Boundary Groups

### 1. Capture And History

Scenarios:

- `00` Recording Structured Information
- `01` Recording Information About a Specific Thing
- `06b` When the Shape of Information Changes
- cross-cut by `19` Working Without Connectivity

Behavioral pressure:

- structured recording
- subject linkage
- shape definition and evolution
- append-only correction
- offline-first work

User-facing product question:

How does a field worker quickly record something, understand what shape of information is expected, correct it later without erasing history, and still trust that old and new versions remain understandable?

Product tension:

- Simple capture must stay simple.
- Versioning and correction must be present without making ordinary capture feel like record management.
- Offline users may keep working under old shapes, so the UI must avoid implying that central configuration is instantly global.

Likely product surfaces:

- capture screen
- draft/submission behavior
- local validation
- record history/timeline
- correction/amendment flow
- stale-shape or older-version indicators where needed

Guardrail routing:

- Event Log / Storage
- Event Envelope / Schema
- Configuration
- Local Data Lifecycle

Hold-backs:

- exact event schema/versioning tooling
- configuration authoring UX
- projection compatibility across schema versions
- product vocabulary for "record" versus baseline event

### 2. Subject Continuity And Registry Work

Scenarios:

- `01` Recording Information About a Specific Thing
- `06` Maintaining a Known Set of Things
- `13` When Separate Activities Are Related
- `22` Coordinated Work Across Grouped Locations

Behavioral pressure:

- subject linkage
- registry lifecycle
- deactivation without breaking history
- ambiguous identity
- cross-reference
- newly discovered units during execution

User-facing product question:

How does the product let users find, create, update, and understand the history of operational targets without exposing identity-lineage mechanics too early?

Product tension:

- Users need a stable "thing profile" experience.
- The platform must tolerate duplicates, splits, merges, deactivation, and offline discoveries.
- History must remain meaningful even when the current identity view changes.

Likely product surfaces:

- lookup/search
- subject or target profile
- create-new-while-offline flow
- deactivated/inactive visibility
- duplicate warning or resolution queue
- related work/history panel

Guardrail routing:

- Identity / Lineage
- Event Envelope / Schema
- Projection / Workflow State
- Flag / Resolution

Identity control warning:

`../professional-baseline/09-identity-boundary-control.md` is required guardrail material for this boundary. Product language such as "subject", "target", "unit", "thing", "case", "shipment", "campaign", or "work" must not imply that one broad Identity / Lineage subsystem owns every referent lifecycle. Identity / Lineage owns subject continuity where the referent has subject-lineage semantics. Actor authority, assignment scope, process state, campaign/shipment/case lifecycle, pending match, reporting identity views, and conflict-resolution lifecycle route elsewhere.

Hold-backs:

- alias-cycle behavior
- user-facing identity resolution UX
- duplicate-detection policy
- process identity and pending-match UX
- product vocabulary for "subject", "target", "unit", and "registry"

### 3. Responsibility, Authority, And Visibility

Scenarios:

- `03` Designated Responsibility
- `04` Supervisor Review
- `09` Coordinated Campaign
- `11` Multi-Step Approval
- `14` Multi-Level Distribution
- `22` Coordinated Work Across Grouped Locations
- cross-cut by `access-control-scenario.md`
- cross-cut by `19` Working Without Connectivity

Behavioral pressure:

- responsibility binding
- hierarchical visibility
- contextual authority
- temporary authority
- offline last-known authority
- assignment and reassignment

User-facing product question:

How does a user understand what work is theirs, what they are allowed to do, what they can only observe, and why something appears or disappears from their device?

Product tension:

- Authority must be contextual without making users read policy internals.
- Supervisors need broader visibility without turning visibility into uncontrolled action authority.
- Offline devices may enforce old authority while central assignments changed.
- Temporary grants and campaign assignments need a clear user experience, but exact grace behavior remains a gap.

Likely product surfaces:

- My Work / Today
- team view
- assignment view
- access-denied or read-only state
- reassignment handoff
- offline/stale-authority warning
- auditor or exception access surface if required

Guardrail routing:

- Assignment / Authority / Sync
- Configuration
- Reporting / Aggregation
- Local Data Lifecycle

Hold-backs:

- subject-based scope and auditor access
- shared-device actor scope
- temporary authority and offline revocation reconciliation
- permission table details
- cross-level distribution visibility
- group-managed authorization and IdP authority

### 4. Queues, Rhythm, And Due Work

Scenarios:

- `02` Regular, Recurring Reporting
- `03` Designated Responsibility
- `05` Periodic Visits and In-Person Assessment
- `09` Coordinated Campaign
- `10` Work That Depends on Changing Conditions
- `12` Event-Triggered Actions
- `22` Coordinated Work Across Grouped Locations

Behavioral pressure:

- temporal rhythm
- responsibility binding
- condition-triggered action
- expected work and missing work
- overdue and escalation

User-facing product question:

How does the product show what is due, missing, overdue, triggered, or waiting, and explain why each item needs attention?

Product tension:

- The same "needs attention" experience must cover scheduled reports, planned visits, campaign coverage gaps, triggered follow-up, and escalations.
- The product must explain pending work without exposing arbitrary trigger logic.
- Scenario 12 must not turn into a general rules-engine UX.

Likely product surfaces:

- Today / inbox
- due and overdue queues
- campaign progress list
- missing expected work view
- escalation list
- item explanation: why this is here

Guardrail routing:

- Projection / Workflow State
- Trigger / Reactivity
- Assignment / Authority / Sync
- Reporting / Aggregation

Hold-backs:

- exact Pattern Registry inventory
- formal Pattern Registry schema format
- notification/escalation delivery surface
- bounded trigger execution semantics
- reporting freshness surface

### 5. Review, Judgment, And Approval

Scenarios:

- `04` Supervisor Review
- `05` Periodic Visits and In-Person Assessment
- `11` Multi-Step Approval
- `21` is not Phase 1 authority here, but remains useful later as composite validation context

Behavioral pressure:

- review and judgment
- state progression
- hierarchical visibility
- aging and bottlenecks
- auditable decisions

User-facing product question:

How does a reviewer see what needs judgment, decide, return, reject, approve, escalate, and understand where work is stuck?

Product tension:

- Review decisions must feel like operational judgment, not low-level state transitions.
- Multi-step approval must show where work sits without making every user understand the whole internal workflow model.
- Invalid or stale decisions created offline may need surfacing without erasing the original action.

Likely product surfaces:

- review inbox
- review detail
- decision history
- returned-work flow
- approval-chain progress
- waiting-too-long indicators

Guardrail routing:

- Projection / Workflow State
- Assignment / Authority / Sync
- Flag / Resolution

Hold-backs:

- exact pattern inventory and schema
- source-chain traversal limits
- general flag semantics beyond closed workflow behavior
- temporary authority/revocation behavior if reviewers act offline

### 6. Transfer, Custody, And Discrepancy

Scenarios:

- `07` Handing Things Off and Confirming Receipt
- `14` Moving Things Through a Chain of Responsibility
- `22` Coordinated Work Across Grouped Locations

Behavioral pressure:

- transfer with acknowledgment
- state progression
- chain visibility
- partial/missing/disputed receipt
- related-but-separate supply and field work

User-facing product question:

How does the product show what was sent, received, disputed, still in transit, and connected to field execution without merging supply flow and field work into one confusing process?

Product tension:

- Single-hop transfer should remain simple.
- Multi-hop custody needs end-to-end traceability.
- Disputes must be visible and resolvable without rewriting handoff history.
- In scenario 22, supply flow and field work intersect but should remain separate activities linked by context.

Likely product surfaces:

- transfer creation
- receipt acknowledgement
- discrepancy/dispute flow
- chain view
- outstanding/in-transit list
- linked campaign supply context

Guardrail routing:

- Projection / Workflow State
- Flag / Resolution
- Reporting / Aggregation
- Assignment / Authority / Sync

Hold-backs:

- cross-level distribution visibility
- domain conflict automation outside workflow
- reporting and aggregation detail
- exact workflow pattern inventory

### 7. Long-Running Situations And Follow-Up

Scenarios:

- `08` Following Something Over Time Until It Is Resolved
- `10` Work That Depends on Changing Conditions
- `12` Event-Triggered Actions
- `13` When Separate Activities Are Related

Behavioral pressure:

- state progression
- responsibility shifts
- condition-triggered action
- cross-reference
- active/resolved/waiting-too-long visibility

User-facing product question:

How does the product keep an ongoing situation understandable across interactions, people, related activities, and changing conditions?

Product tension:

- Users need a coherent case/situation view, but the platform must remain domain-agnostic.
- Cross-links should provide context without forcing separate activities into one process.
- Triggered work should be understandable as "this now needs attention" without hiding its cause.

Likely product surfaces:

- active situation detail
- follow-up list
- related context panel
- responsibility handoff
- resolved/reopened history
- triggered-work explanation

Guardrail routing:

- Projection / Workflow State
- Trigger / Reactivity
- Assignment / Authority / Sync
- Flag / Resolution

Hold-backs:

- bounded trigger semantics
- exact workflow patterns for long-running state
- cross-flow link visibility and access behavior
- reporting over active/resolved work

### 8. Oversight, Progress, And Reporting

Scenarios:

- `02` Regular, Recurring Reporting
- `05` Periodic Visits and In-Person Assessment
- `09` Coordinated Campaign
- `11` Multi-Step Approval
- `14` Multi-Level Distribution
- `22` Coordinated Work Across Grouped Locations

Behavioral pressure:

- hierarchical visibility
- progress roll-up
- missing/late detection
- bottleneck visibility
- freshness awareness
- decision-maker views

User-facing product question:

How does a supervisor or coordinator see the state of work across people, areas, periods, reviews, and transfers while understanding that field state may be stale?

Product tension:

- Oversight must feel coherent across reporting, campaigns, reviews, transfers, and registry maintenance.
- Aggregate views cannot become canonical operational truth.
- Freshness must be visible enough to support decisions without overwhelming the screen.

Likely product surfaces:

- supervisor dashboard
- coordinator dashboard
- progress by area/location
- backlog and bottleneck views
- missing/late views
- freshness indicators
- exception list

Guardrail routing:

- Reporting / Aggregation
- Projection / Workflow State
- Assignment / Authority / Sync
- Event Log / Storage

Hold-backs:

- reporting and aggregation
- workflow-aware reporting
- freshness metadata
- access-constrained aggregate views
- cross-level visibility

### 9. Offline, Sync, And Reconciliation

Scenarios:

- `19` Working Without Connectivity
- applies across `00-14` and `22`

Behavioral pressure:

- work happens without connectivity
- local work later becomes centrally visible
- performed time differs from visible time
- stale local authority/configuration/subjects may exist
- conflicts may need human judgment

User-facing product question:

How does the product make offline work feel normal while still showing enough sync, freshness, and conflict information for trust?

Product tension:

- Offline should not feel like a degraded exception for field work.
- The UI must distinguish "done on device" from "visible to others" without requiring users to understand protocol mechanics.
- Conflict surfacing must be strong enough for trust but not so noisy that supervisors become a permanent cleanup queue.

Likely product surfaces:

- offline indicator
- pending upload count
- last synced timestamp
- sync result summary
- stale data warning where decisions depend on freshness
- conflict/exception queue
- local work history

Guardrail routing:

- Event Log / Storage
- Event Envelope / Schema
- Assignment / Authority / Sync
- Flag / Resolution
- Local Data Lifecycle

Hold-backs:

- sync delivery mechanics
- projection performance and caching
- general flag semantics
- local data lifecycle
- temporary authority/offline revocation reconciliation

## Scenario-To-Product-Boundary Matrix

| Scenario | Primary Product Boundary | Secondary Product Boundaries | Key UX Tension |
|---|---|---|---|
| `00` | Capture And History | Offline / Sync; Configuration | Keep simple capture simple while preserving correction/version history. |
| `01` | Subject Continuity And Registry Work | Capture And History; Offline / Sync | Show history around a thing without exposing identity ambiguity too early. |
| `02` | Queues, Rhythm, And Due Work | Oversight / Reporting; Responsibility | Missing work must be visible as a meaningful state, not just absent data. |
| `03` | Responsibility, Authority, And Visibility | Queues / Due Work; Oversight | Users need to know what is theirs and why without seeing policy internals. |
| `04` | Review, Judgment, And Approval | Responsibility; Queues; Flags | Waiting and decisions must be visible, auditable, and easy to act on. |
| `05` | Queues, Rhythm, And Due Work | Capture; Review; Oversight | Planned visits produce both records and follow-up obligations. |
| `06` | Subject Continuity And Registry Work | Capture; Configuration; Offline | Registry changes must not break historical work or offline work. |
| `06b` | Capture And History | Configuration; Offline | Old and new information shapes coexist without confusing users. |
| `07` | Transfer, Custody, And Discrepancy | Review; Flags; Oversight | Transfer is not complete until receipt or dispute is visible. |
| `08` | Long-Running Situations And Follow-Up | Responsibility; Cross-Reference; Review | Ongoing work must stay understandable across people and time. |
| `09` | Oversight, Progress, And Reporting | Responsibility; Queues; Offline | Coordinators need progress and gaps across many locations with stale field state. |
| `10` | Long-Running Situations And Follow-Up | Queues; Trigger / Reactivity | Changing conditions create work without becoming an opaque rule engine. |
| `11` | Review, Judgment, And Approval | Responsibility; Oversight | Approval chain status must be clear without exposing internal workflow machinery. |
| `12` | Queues, Rhythm, And Due Work | Trigger / Reactivity; Oversight | Responses and escalations must be bounded and explainable. |
| `13` | Long-Running Situations And Follow-Up | Subject Continuity; Oversight | Related activities need visible context without rigid coupling. |
| `14` | Transfer, Custody, And Discrepancy | Oversight; Responsibility | Chain visibility must show current location, outstanding handoffs, and discrepancies. |
| `19` | Offline, Sync, And Reconciliation | All boundaries | Offline work is normal; reconciliation must preserve trust. |
| `22` | Oversight, Progress, And Reporting | Capture; Subject Continuity; Transfer; Responsibility; Offline | Grouped-location work composes many patterns and needs coherent progress, reassignment, duplicate, and supply-flow UX. |

## Cross-Cutting Product Tensions

### T1. Domain-Agnostic Coherence Versus Natural Language

The product must use words that feel natural to users while staying domain-agnostic. A term that works for one domain may become wrong elsewhere. Later vocabulary work must separate:

- user-facing nouns
- architecture mechanisms
- deployer-configured labels
- domain examples

### T2. Simple Work Versus Hidden Platform Power

S00 must remain lightweight. Capture should not require users to understand workflow, flags, projections, sync internals, identity lineage, or pattern composition unless the selected activity needs those behaviors.

### T3. Offline Normality Versus Trust Signals

Field users should not feel that offline work is second-class. At the same time, supervisors and coordinators need enough freshness and reconciliation signals to trust what they see.

### T4. Progress Visibility Versus Canonical Truth

Users need progress dashboards and current-state views. The architecture baseline says these are projection-derived. Product design must therefore make current views useful while preserving access to history and not implying that summary state is the canonical record.

### T5. Configuration Promise Versus Setup Complexity

The domain promise is "set up, not built." The viability assessment already identified setup/configuration as the weakest scenario-tested area. Later product sessions must make the admin/configuration experience concrete enough to prove this promise without expanding configuration into programming.

### T6. Exceptions Versus Everyday Work

Flags, conflicts, stale authority, duplicate subjects, transfer disputes, and invalid transitions are essential for trust. They must not dominate ordinary workflows. The product needs progressive disclosure: normal work stays focused; exceptions surface when relevant.

### T7. Compositional Patterns Versus One-Off Screens

The behavioral patterns compose across scenarios. Product surfaces should also compose. If every scenario gets a bespoke screen model, the product will violate the "one system, not many" promise even if the architecture remains compositional.

## Immediate Implications For Later Sessions

Product experience principles should emphasize:

- one coherent operational system
- offline-first confidence
- explainable pending work
- progressive disclosure of complexity
- visible history and current interpretation
- scoped visibility and authority
- freshness-aware oversight
- configuration that feels bounded, not like programming

User-role work should separate:

- field worker
- supervisor / team lead
- coordinator / administrator
- auditor / external reviewer
- system actor only as architecture/product explanation where needed, not a normal user role

Vocabulary alignment must be treated as high risk because scratch product terms are useful but not baseline authority. In particular, later sessions must map:

- "record" to the accepted event/log/history model
- "template" to shape/configuration without weakening baseline `shape_ref`
- "work item" to projection/workflow/pattern behavior without inventing canonical work-item storage
- "activity" to deployer-configured activity and `activity_ref`
- "scope" to Assignment / Authority / Sync and Configuration boundaries
- "flag/conflict" to closed workflow flag behavior plus open general flag semantics
- "subject", "target", "unit", "case", "shipment", and "campaign" without collapsing referent vocabulary into one broad identity lifecycle owner

## Session 1 Output

The product-alignment track should proceed to `02-product-experience-principles.md` before vocabulary decisions or wireframe-level UX. The first priority is to stabilize how the product should feel across all Phase 1 boundaries, not to name every object.
