# Scenario User-Fit Packet — S00: Recording Structured Information

## 1. Scenario frame

**Scenario ID:** S00
**Scenario title:** Recording Structured Information
**Scenario role in platform learning:** Foundational simplicity baseline

**Source scenario summary:**
Someone needs to record a known set of details about something they observed, did, checked, received, or reported. The expected information is known ahead of time. The user fills it in, and the record is kept for later lookup, review, correction, or reporting.

**Why this scenario matters:**
This is the minimum useful platform behavior. If the platform cannot make this scenario simple, later scenarios will inherit unnecessary complexity.

**What this scenario must not decide:**
This packet does not decide UI screens, database tables, API design, service boundaries, sync protocol mechanics, exact form-builder syntax, report layout, or operational staffing policy.

## 2. User and organization fit

### 2.1 Organization profile

**Organization type:** Field-operating organization that needs structured records from distributed workers.
**Operational domain:** Domain-neutral; could apply to health, logistics, agriculture, infrastructure, humanitarian response, education, inspections, or routine monitoring.
**Scale:** Small team through national deployment.
**Connectivity profile:** Field execution may be offline for hours or days. Coordination may be online more often.
**Operational maturity:** May currently use paper forms, spreadsheets, messaging apps, disconnected survey tools, or a bespoke system.
**Sensitivity pressure:** Ranges from routine to restricted, depending on what is recorded.
**Current workaround:** People collect information using paper, spreadsheets, forms, chat messages, or single-purpose data collection tools, then someone later reconciles, cleans, and reports it.

### 2.2 Personas by operational surface

#### Persona A — Field recorder

**Operational surface:** Field execution
**Real-world responsibility:** Capture information accurately at the point of work.
**Work context:** Often mobile, time-constrained, sometimes offline, possibly using a low-end Android device.
**Primary intent:** Record what happened or what was observed without needing to understand the platform’s internals.
**What they need to see or do:** Open the right record shape, fill required details, save confidently, correct mistakes when allowed.
**What they need to trust:** The record will not disappear if offline; the system will preserve when and by whom it was recorded.
**What they should not need to understand:** Event envelopes, shape versioning, sync watermarks, projection rebuilding, conflict categories, or architecture vocabulary.
**Platform authority mapping:** Not assumed here. If access matters later, map through accepted `actor`, `assignment`, `role`, and `scope`.

#### Persona B — Supervisor or reviewer

**Operational surface:** Supervision / review
**Real-world responsibility:** Check whether records are complete, plausible, timely, and usable.
**Work context:** May be online or intermittently offline; needs visibility into submitted work.
**Primary intent:** Understand what was recorded, identify issues, and request correction or follow-up where needed.
**What they need to trust:** They can distinguish original records from corrections and can see unresolved anomalies.
**What they should not need to understand:** Storage mechanics, sync ordering mechanics, or database history.

#### Persona C — Coordinator or administrator

**Operational surface:** Coordination / administration
**Real-world responsibility:** Define what information should be collected and monitor whether records are coming in.
**Work context:** More likely to have reliable connectivity and larger-screen access.
**Primary intent:** Set up a simple data capture activity without custom software development.
**What they need to trust:** Changes to what is collected will not invalidate historical records.
**What they should not need to understand:** Event-source implementation, internal schema migration mechanics, or device storage details.

## 3. User intent

### 3.1 Jobs to be done

**Field recorder job:**
When I observe, inspect, receive, count, or report something, I need to record the required details immediately, so that the organization has a trustworthy record, without waiting for connectivity or doing duplicate paperwork.

**Supervisor job:**
When records are submitted, I need to see what was recorded and whether anything looks incomplete, duplicated, stale, or corrected, so that I can follow up without losing the original record.

**Coordinator job:**
When the organization needs a new structured record, I need to define what should be collected, so that field teams can start using it without new application development.

### 3.2 Intent categories

* Record something
* Preserve accountability
* Look up what was recorded
* Correct a mistake without erasing history
* Adapt the expected information over time
* Reconcile offline work later

### 3.3 Success from the user’s point of view

**Field worker success:**
“I captured the record once, it saved, and I know it will sync later.”

**Supervisor success:**
“I can see the record, who made it, when it was made, whether it was corrected, and whether it needs attention.”

**Coordinator success:**
“I can define a simple information shape and deploy it without building a custom app.”

**Organization success:**
“The organization has a durable, traceable body of records that remains meaningful as forms and requirements evolve.”

## 4. Real-world journey

### 4.1 Normal path

1. Coordinator defines the known details that should be recorded.
2. Field recorder opens the relevant capture surface.
3. Field recorder enters the required details.
4. The record is saved immediately.
5. The record becomes visible for later lookup, review, reporting, or correction.
6. If online, it syncs soon. If offline, it waits until sync is possible.

### 4.2 Offline / stale path

1. Field recorder works while disconnected.
2. The device still allows record creation.
3. The record is saved locally.
4. The organization may change the expected information shape before the device reconnects.
5. On sync, the old-shape record is still accepted because it was valid under the device’s active configuration.
6. Any stale, duplicate, or conflicting condition is surfaced for review rather than silently rejected or overwritten.

### 4.3 Correction / exception path

1. A submitted record is found to contain an error.
2. A correction is made as a new accountable action.
3. The original remains traceable.
4. The corrected view is available through derived views or reports.
5. The system preserves who changed what, when, and why, if policy requires a reason.

### 4.4 Longitudinal path

Over time, many records accumulate. Some are created under older information shapes, some under newer ones. The organization still needs to search, compare, report, and audit across the full history without pretending old records were created under new requirements.

## 5. Domain-neutral artifacts

| Artifact                            | Purpose                                                                            | Created by                      | Used by                                        | Changes over time?              | Trust requirement                                        |
| ----------------------------------- | ---------------------------------------------------------------------------------- | ------------------------------- | ---------------------------------------------- | ------------------------------- | -------------------------------------------------------- |
| Structured record                   | Captures the required details of an observation, action, report, receipt, or check | Field recorder or system actor  | Field worker, supervisor, coordinator, reports | No, the fact remains stable     | Must remain durable and traceable                        |
| Information shape                   | Defines what details are expected for a type of record                             | Coordinator / administrator     | Field recorder, validation, reports            | Yes, by version                 | Old records must remain interpretable                    |
| Draft or unsynced local record      | Allows work to continue without connectivity                                       | Field recorder                  | Field recorder, sync process                   | Yes, until finalized/synced     | Must not be lost silently                                |
| Correction note or correction event | Records that a prior record was amended or superseded                              | Authorized actor                | Supervisor, auditor, reporting, or Field recorder if authorized                 | Yes, corrections may accumulate | Must not erase the original                              |
| Review or issue marker              | Shows that a record needs attention                                                | Supervisor or system process    | Supervisor, coordinator                        | Yes, resolved or escalated      | Must not hide the underlying record                      |
| Report extract                      | Summarizes captured records                                                        | Coordinator / reporting process | Coordinators, managers, external reviewers     | Yes, as records sync            | Must show freshness and unresolved issues where relevant |

## 6. Concrete domain exemplar

### Exemplar: Water-point condition observation

**Domain:** WASH / infrastructure monitoring
**Organization:** NGO maintaining rural water points
**Concrete artifact names:** Water-point condition form, pump inspection note, repair-needed flag, monthly condition extract
**Concrete user language:** “I checked the pump and recorded whether it works, what is broken, and whether repair is needed.”

### Concrete journey

1. A field worker visits a water point.
2. They open the water-point condition observation.
3. They record:

   * water point ID or selected site;
   * whether water is flowing;
   * pump condition;
   * visible damage;
   * estimated urgency;
   * free-text note;
   * optional photo if supported later.
4. The worker saves the observation offline.
5. The supervisor later sees the observation after sync.
6. If the field worker made a mistake, a correction is recorded without deleting the original.

### Where this example may mislead platform design

* It may make S00 look like infrastructure inspection only; it is not.
* It may encourage assuming all records are tied to a persistent subject. S00 can be plain structured capture; entity-linked capture belongs more directly to S01.
* It may encourage workflow assumptions such as automatic repair task creation. That belongs to later scenarios, not S00.
* It may encourage photo/media handling decisions, which are not settled by S00.

## 7. Platform fit under current accepted architecture

| User need                                 | Likely settled platform vocabulary                                       | Fit assessment                                 |
| ----------------------------------------- | ------------------------------------------------------------------------ | ---------------------------------------------- |
| Save a record immediately                 | `event`, `capture`, `payload`                                            | Strong fit                                     |
| Keep record immutable and traceable       | `append-only`, `event store`, `correction`                               | Strong fit                                     |
| Define expected fields                    | `shape`, `shape_ref`, `shape version`                                    | Strong fit                                     |
| Work offline                              | client-generated event `id`, `device_id`, `device_seq`, `sync_watermark` | Strong fit                                     |
| Sync later without order-dependent truth  | event as sync unit, idempotent sync                                      | Strong fit                                     |
| Interpret old records after shape changes | versioned `shape_ref`, shape evolution                                   | Strong fit                                     |
| Correct without erasing                   | correction as new event                                                  | Strong fit, platform-spec details still needed |
| Detect duplicates or stale records        | `accept-and-flag`, `detect-before-act`                                   | Good fit, exact UX/spec open                   |
| Show current view                         | `projection`, `read model`                                               | Strong fit                                     |
| Keep basic capture simple                 | `S00 simplicity baseline`, `capture_only`                                | Critical guardrail                             |

## 8. Fit assessment

### 8.1 Strong fit

S00 fits the current architecture well. The accepted architecture directly supports append-only capture, offline event creation, versioned shape interpretation, sync as immutable events, and derived read models.

### 8.2 Weak fit

The user-facing simplicity is not yet proven. The architecture can support simple capture, but that does not prove that coordinators can configure it simply or that field workers will experience it as simple.

### 8.3 Missing evidence

* What is the minimum setup a coordinator expects for a new capture activity?
* How much field validation is useful before it becomes confusing?
* Do users think in terms of “forms,” “records,” “reports,” “checks,” or domain-specific words?
* How often do corrections happen in real deployments?
* Who is allowed to correct a record?
* Do workers need draft states, or is “save later/sync later” enough?
* What local language or low-literacy constraints affect capture?
* What is the expected handling of media attachments, if any?
* What should happen when two users submit similar records about the same thing?

### 8.4 Risk of false fit

The platform reasoning says S00 is simple, but the actual setup experience may become complex if coordinators must understand shapes, activities, roles, sync, conflict handling, or versioning before creating a basic capture surface.

## 9. Gap routing

### Gap 1 — Simple setup experience for basic capture

**Short name:** Basic capture setup lifecycle
**Classification:** Platform-spec detail gap
**Baseline-extension category:** Not applicable
**Current owner or likely decision path:** Platform-spec detailing
**Baseline item affected:** S00 simplicity baseline; shape/activity setup under bounded configuration
**Why it is still open:** The architecture says simple capture must remain simple, but does not define the coordinator-facing setup sequence.
**Closure path:** Platform-spec detailing
**Evidence needed before closure:** SME review of how non-technical coordinators describe a new data collection need.

### Gap 2 — User language for “record,” “form,” and “shape”

**Short name:** Capture vocabulary fit
**Classification:** Product/problem evidence gap
**Baseline-extension category:** Not applicable
**Current owner or likely decision path:** Product discovery / scenario thickening
**Baseline item affected:** Product language around S00 and configuration surfaces
**Why it is still open:** The architecture uses `shape` and `event`, but users may understand “form,” “checklist,” “record,” “report,” or domain-specific terms.
**Closure path:** Product discovery / scenario thickening
**Evidence needed before closure:** Interviews or artifact review from target organizations.

### Gap 3 — Correction behavior for simple capture

**Short name:** Basic capture correction policy
**Classification:** Platform-spec detail gap
**Baseline-extension category:** Not applicable
**Current owner or likely decision path:** Platform specification plus operational policy if permissions are involved
**Baseline item affected:** Append-only correction behavior
**Why it is still open:** The architecture preserves corrections as new facts, but does not specify the user-facing correction flow, required reason, or review behavior.
**Closure path:** Platform-spec detailing
**Evidence needed before closure:** Examples of real correction practices from field organizations.

### Gap 4 — Duplicate simple records

**Short name:** Duplicate capture handling
**Classification:** Platform-spec detail gap
**Baseline-extension category:** Not applicable
**Current owner or likely decision path:** Platform specification
**Baseline item affected:** Accept-and-flag and duplicate/stale review behavior
**Why it is still open:** S00 notes duplicate or similar records, but exact detection, display, and resolution behavior are not specified here.
**Closure path:** Platform-spec detailing
**Evidence needed before closure:** Examples of when duplicate records are harmless, useful, or problematic.

## 10. Output decision

**Current packet status:** Needs SME validation first, then platform-spec detailing.

**Reason:**
The architecture supports the scenario, but the user-fit evidence is not yet strong enough to specify the setup experience, correction behavior, vocabulary, or duplicate handling with confidence.

## 11. Acceptance criteria for downstream platform-spec work

A later S00 platform-spec section should be accepted only if it satisfies:

1. A coordinator can define a basic capture activity without custom code.
2. A field worker can create and save a record offline.
3. The record remains durable and traceable after sync.
4. A record created under an older shape remains interpretable.
5. Corrections do not erase originals.
6. Duplicate or stale records are surfaced without silent overwrite.
7. Basic capture does not require custom event types, custom access logic, custom triggers, deployer-authored state machines, or workflow flag propagation.
8. The user-facing language can be understood without architecture vocabulary.
