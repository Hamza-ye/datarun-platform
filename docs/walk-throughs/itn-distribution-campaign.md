# Walk-Through: ITN Distribution Campaign

## Domain

Public health — insecticide-treated net (ITN) mass distribution in rural areas.

## Scenarios Exercised

S00 (structured capture), S01 (entity-linked capture), S03 (assignment), S06 (registry lifecycle), S07 (resource distribution), S09 (coordinated campaign), S14 (multi-level distribution), S22 (coordinated work across grouped locations)

---

## Context

A public health team conducts a time-bound campaign to distribute insecticide-treated nets across rural villages. Each village contains households that must be visited, assessed, and provided with nets based on need. Some households are known from previous campaigns; others are discovered during the current operation.

### Actors

* Distribution teams operating in the field
* Supervisors coordinating and adjusting field execution
* Storage operators managing ITN stock movement across supply points

### Objective

Ensure all households within targeted villages receive the appropriate number of ITNs, while maintaining accurate household records and tracking all distributed and remaining stock.

---

## Campaign Flow

### 1. Campaign Setup

A list of target villages is defined. For each village:

* A list of previously recorded households may be available
* Expected targets may exist (number of households to cover, number of ITNs to distribute)
* Some villages may have no prior records or targets

### 2. Assignment

Villages are assigned to distribution teams. Assignments may be changed by supervisors during the campaign.

### 3. Household-Level Activity

The work at each village may happen in separate passes by different actors. A registration team may visit first to enumerate and record households — capturing demographics, confirming locations, and updating existing records. A distribution team follows with the actual ITN allocation, working from the household list the registration pass produced.

Alternatively, a single team may handle both registration and distribution in one visit. Which approach is used depends on campaign design, team capacity, and logistics.

For each household:

* If already recorded: existing details are reviewed and updated
* If new: the household is registered and added to the dataset

During the visit, information is captured:

* Head of household
* Number of males and females
* Presence of internally displaced persons (if applicable)
* Number of ITNs given (during distribution pass)

### 4. Distribution

ITNs are allocated to each household based on defined criteria (e.g., household size). The number distributed is recorded per household.

### 5. Village Progress

Each village moves through observable states: not started → in progress → one of several terminal states.

A village may reach a terminal state in different ways:

* **Completed**: The team confirms all known and newly discovered households have been covered.
* **Interrupted**: Work was stopped before completion — access issues, security concerns, weather, or other reasons. A reason may be recorded. The village is closed for that team but may be reopened or reassigned.
* **Partially reassigned**: A team covers part of the village and marks their assignment as terminated. The remainder is assigned to another team, which picks up from where the first left off. The village itself is not complete until the second team finishes.

In all cases, the team makes the judgment call about their own status. Village-level completion is derived from whether all units have been handled, regardless of how many teams were involved.

### 6. Dynamic Changes During Execution

New villages may be identified and added. Villages may be cancelled or reassigned to other teams. These actions are performed by supervisors.

### 7. Target Monitoring

Progress may be evaluated against total households covered and total ITNs distributed. Some villages may not have predefined targets, requiring assessment based on actual coverage.

### 8. Supply Flow

Distribution teams collect ITNs from temporary storage points before fieldwork. After fieldwork, remaining ITNs are returned to the storage point. Storage points receive stock from higher-level warehouses and send stock back if needed. Each transfer is tracked with quantities, source and destination, and responsible actors.

---

## Outcomes

* All visited households are recorded or updated
* Each village has a clear completion status
* Total ITNs distributed are tracked and reconciled
* Remaining stock is accounted for
* The household dataset is expanded for future campaigns

---

## Architecture Walk-Through

This section maps the ITN campaign to the platform's architectural primitives and identifies pressure points.

### Primitive Mapping

| Campaign Concept | Primitive(s) | Notes |
|---|---|---|
| Household | Subject (type: household) | Entity-linked capture; registry grows during campaign |
| Village | Subject (type: village) | Parent subject; households are children via `parent_ref` |
| Household visit | Event (shape: household_visit) | Structured capture with ITN count, demographics |
| Village assignment | Assignment | Operator ↔ village binding; reassignable |
| ITN handoff | Event (shape: transfer) | Transfer-with-acknowledgment pattern (S07/S14) |
| Campaign | Configuration scope | Defines target villages, date window, shapes active |
| Village progress | L1 Projection | Derived from household-level events within village |

### Pressure Points

**1. Parent–child subject relationships (village → households)**

Villages and households are both subjects. A household belongs to a village. The architecture handles this via a `parent_ref` field in the subject payload — a convention, not a separate primitive. L1 projection rules can scope queries to "all subjects whose parent_ref = this village." No new construct needed.

**2. Location progress as derived state**

"Is this village done?" depends on aggregating household-level work. Two viable paths:

* **Path 1 — Projection aggregation**: An L1 rule counts distinct visited households vs. known households under a village and derives a progress state. Pure read-side; no new events needed.
* **Path 2 — Explicit location events**: Field operators (or the device) emit a `village_status` event when they believe a village is complete. This gives an auditable moment but requires the operator to make a judgment call.

Path 2 is recommended: it preserves the principle that humans make completion judgments, the event is auditable, and the projection can still compute coverage independently for oversight.

**3. Concurrent assignment to the same location**

Multiple distribution teams may be assigned to the same village, each working through households independently. Offline, neither team knows which households the other has visited. This maps directly to ADR-002's single-writer assumption: two actors creating events against the same subject_ref produces concurrent writes. The architecture handles this via accept-and-flag — both events are accepted, conflicts detected on sync.

Prevention is an operational concern, not an architectural one. Two approaches within the existing model:

* **Assignment-level partitioning**: Assign non-overlapping subsets of households to each team (e.g., by sub-area or household list). This uses the existing assignment primitive with a narrower scope.
* **Projection-based visibility**: After sync, L1 projections show "already visited by another actor" status. This doesn't prevent duplicates but surfaces them immediately for supervisor review.

Two additional deployer levers:

* **Online-only constraint for shared locations**: If connectivity permits, require operators working the same location to stay online so projections stay current and each sees the other's progress in near-real-time. This trades offline capability for coordination safety — acceptable in some deployments, unacceptable in others.
* **Configuration-level exclusivity**: A deployment rule such as "only one active assignment per location at a time" prevents the situation structurally. This is an L0 configuration constraint on the assignment primitive — no new mechanism needed.

For ITN distribution specifically, duplicate visits mean double-distribution — a concrete resource waste problem. The architecture handles the data reconciliation; the deployment configuration (how assignments are scoped and whether concurrency is allowed) determines how much duplication actually occurs.

**4. Campaign-level targets and aggregation**

Campaign-wide metrics (total ITNs distributed, total villages completed) aggregate across all village projections. This is an L2 concern — server-side aggregation over synced data. It lives outside the device boundary, which is correct: campaign dashboards are supervisor/coordinator views, not field-operator views.

### Verdict

The ITN campaign is fully modelable within the existing architecture. It composes subjects, events, assignments, projections, and transfer-with-acknowledgment without requiring new primitives. The "scoped iteration" pattern (iterate over units within a container) is a composition of existing primitives, not a fifth behavioral pattern.

Earliest phase where the full campaign is executable: **Phase 4** (workflow patterns: campaign as a coordinated effort with dynamic scope changes). Core data capture (household visits, ITN counts) works from **Phase 0**.
