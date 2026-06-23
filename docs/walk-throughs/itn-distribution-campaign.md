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

## Product And Compatibility Pressure

This section records hypotheses and pressure to classify. It is not
architecture authority, does not define platform primitives, and does not prove
the full campaign is currently executable.

### Candidate Mappings To Classify

| Campaign Concept | Candidate Datarun lens | Pressure to classify |
|---|---|---|
| Household | Subject-linked capture or entity-lifecycle route | Newly discovered households may trigger NW-021 if maintained registry lifecycle blocks the pilot slice. |
| Village | Location, subject-linked capture, or reporting lens | Do not assume parent/child subject semantics without a selected lifecycle/scope route. |
| Household visit | Configured capture activity | Likely compatible when flattened into current shape fields and accepted expression rules. |
| Village assignment | Assignment-derived responsibility | Must stay inside accepted geography, subject-list, activity, role, and time scope axes unless NW-053 is selected. |
| ITN handoff | Transfer/distribution workflow context | Current S27 evidence supports bounded transfer pressure; reconciliation/reporting may still trigger NW-044. |
| Campaign | Deployment plan or configured activity bundle | Must not become a new config scope primitive without a selected platform route. |
| Village progress | Read-side/projection/reporting view | Use as reporting pressure; route NW-044 if broad aggregation, warehouse/export, or import is required. |

### Pressure Points

**1. Parent–child subject relationships (village → households)**

Villages and households may need a relationship, but this walkthrough no longer
claims that `parent_ref` or a subject hierarchy is accepted platform behavior
for the campaign. Treat the relationship as entity/scope pressure. If the pilot
requires maintained household or village lifecycle, route through NW-021 before
implementation.

**2. Location progress as derived state**

"Is this village done?" depends on aggregating household-level work and field
judgment. Candidate paths remain hypotheses:

* **Read-side aggregation**: count visited/covered households and exceptions
  under the selected scope. This is reporting pressure and may route NW-044.
* **Field-authored status capture**: a team records its completion,
  interruption, reassignment, or cancellation judgment as configured capture
  content. This must not introduce new envelope types or workflow truth without
  a selected route.

No path is accepted by this walkthrough. A pilot slice must choose the smallest
safe representation and prove it against current contracts or route a successor.

**3. Concurrent assignment to the same location**

Multiple distribution teams may work the same village or overlapping household
set. Offline work can create duplicate visits, double distribution, stale
authority, and inconsistent observations. Current Datarun evidence supports
accept-and-flag behavior for scoped offline work, but this walkthrough does not
claim all ITN duplicate and stock reconciliation cases are solved.

Candidate controls to classify:

* **Assignment partitioning**: assign non-overlapping geography, subject lists,
  or activities when current scope axes are sufficient.
* **Operational visibility**: surface already-handled, duplicate, stale, or
  unresolved work after sync using accepted read-side behavior.
* **Review/reconciliation**: keep manual review bounded; route NW-045 if queue,
  batch, automation, or resolver reassignment is required.

Additional deployment choices are policy pressure, not architecture decisions:

* **Online-only constraint for shared locations**: possible operational posture
  when connectivity permits, not an offline-first platform guarantee.
* **Exclusive assignment policy**: possible setup/operator rule if expressible
  inside accepted assignment behavior; otherwise route before implementation.

For ITN distribution specifically, duplicate visits may mean double-distribution
and stock loss. That makes reconciliation a pilot risk, not proof that the full
campaign is already modelable.

**4. Campaign-level targets and aggregation**

Campaign-wide metrics such as total ITNs distributed, villages completed,
stock issued, stock returned, and unresolved discrepancies are reporting
pressure. Current S26/PC3 evidence supports bounded scoped read views, but broad
reporting, import/export, warehouse/API/catalog, or structured ingestion must
route through NW-044.

### Current Verdict

The ITN campaign is a strong domain example for a legacy/on-prem pilot, but the
full campaign is not accepted here as fully modelable or executable. The
walkthrough is now problem-space evidence for NW-146/NW-093 routing:

* flat household or stock capture may be a candidate first slice;
* repeatable legacy forms, household/facility lifecycle, reconciliation
  reporting, and review queues may require successor routes;
* real users, real data, and on-prem operation require NW-093 before use.
