# Scenario User-Fit Packet — S01: Entity-Linked Capture

## 1. Scenario frame

**Scenario ID:** S01
**Scenario title:** Recording Information About a Specific Thing
**Scenario role in platform learning:** Foundational subject-linked capture scenario

**Source scenario summary:**
Someone records information about a recognizable real-world thing: a place, piece of equipment, household, facility, organizational unit, person, group, or other identifiable subject. The record is not just a standalone observation. It is tied to that specific thing so that, over time, the organization can see the history of records about it.

**Why this scenario matters:**
S01 is the bridge between S00 basic capture and S06 maintained registries. It tests whether a simple record can be linked to a known subject without turning basic capture into full registry management.

**What this scenario must not decide:**
This packet does not decide subject registry UI, matching algorithms, database schema, API design, search indexing, merge workflow, duplicate scoring, subject taxonomy, or operational governance policy.

## 2. User and organization fit

### 2.1 Organization profile

**Organization type:** Field-operating organization that repeatedly records information about known things.
**Operational domain:** Domain-neutral; could apply to facilities, water points, equipment, households, schools, farms, service points, warehouses, staff, community groups, or program participants.
**Scale:** Small team through national deployment.
**Connectivity profile:** Field workers may need to search, select, or create subject-linked records while offline. Supervisors and coordinators may see subject history only after sync.
**Operational maturity:** May currently use paper lists, spreadsheets, local knowledge, disconnected survey tools, or separate subject registries.
**Sensitivity pressure:** Depends on subject type. Equipment and location records may be routine; household or person-linked records may be elevated or restricted.
**Current workaround:** Workers often write the subject name manually, choose from an outdated list, create duplicate entries when they cannot find the right one, or rely on local memory.

### 2.2 Personas by operational surface

#### Persona A — Field recorder

**Operational surface:** Field execution
**Real-world responsibility:** Record information about the correct real-world thing.
**Work context:** Mobile, time-constrained, often offline, sometimes using partial or stale subject lists.
**Primary intent:** Find the right thing and record the observation once.
**What they need to see or do:** Search/select a subject, confirm enough details to avoid the wrong subject, record the required information, and continue if the subject is missing.
**What they need to trust:** They are recording against the right thing, and their work will remain attached to that thing after sync.
**What they should not need to understand:** `subject_ref`, raw-reference detection, alias projection, merge/split semantics, sync watermark, or identity conflict categories.
**Platform authority mapping:** Not assumed here. If access matters later, map through accepted `actor`, `assignment`, `role`, and `scope`.

#### Persona B — Supervisor or reviewer

**Operational surface:** Supervision / review
**Real-world responsibility:** Check whether subject-linked records are plausible, complete, duplicated, or attached to the wrong thing.
**Work context:** May review after field work syncs, sometimes with partial visibility across teams or areas.
**Primary intent:** See what has been recorded about a specific thing and catch obvious subject-linking problems.
**What they need to trust:** Subject history is not silently rewritten, and duplicate or ambiguous subject links are visible when they matter.
**What they should not need to understand:** Projection rebuild mechanics or lineage graph internals.

#### Persona C — Coordinator or administrator

**Operational surface:** Coordination / administration
**Real-world responsibility:** Set up capture that references known things without making field work complicated.
**Work context:** More likely online, responsible for subject lists, activities, assignments, and reporting expectations.
**Primary intent:** Let teams collect repeat observations about known subjects without rebuilding a custom system.
**What they need to trust:** The same subject can accumulate history across time and activities, and old records remain understandable if subject identity later changes.
**What they should not need to understand:** Event envelope internals, subject alias tables, or conflict detection sequence.

#### Persona D — Auditor or external reviewer

**Operational surface:** Audit / external review
**Real-world responsibility:** Verify that records about a thing are traceable and not retroactively manipulated.
**Work context:** Periodic or targeted review.
**Primary intent:** Reconstruct what was recorded about a subject and how identity ambiguity was handled.
**Platform authority mapping:** Auditor/query access is not assumed; route separately if needed.

## 3. User intent

### 3.1 Jobs to be done

**Field recorder job:**
When I record information about a known thing, I need to attach the record to the right thing, so that future users can see its history and I do not create duplicate or orphaned work.

**Supervisor job:**
When I review subject-linked records, I need to see what was recorded about a thing and whether any record looks duplicated, stale, or attached to the wrong subject.

**Coordinator job:**
When I set up a subject-linked activity, I need workers to use known subjects where possible, while still allowing work to continue when a subject is missing or ambiguous.

**Auditor job:**
When I inspect records about a subject, I need to see what happened over time without historical references being rewritten.

### 3.2 Intent categories

* Record about a known thing
* Find/select the right subject
* Preserve subject history
* Avoid duplicate subjects
* Continue when the subject is missing
* Correct wrong links without erasing history
* Review everything associated with a subject
* Keep old subject-linked records meaningful after identity changes

### 3.3 Success from the user’s point of view

**Field worker success:**
“I found the right thing, recorded against it, and did not need to understand registry mechanics.”

**Supervisor success:**
“I can see the subject’s history and spot records that may be duplicated or wrongly attached.”

**Coordinator success:**
“I can configure subject-linked capture without forcing full registry administration into every simple capture activity.”

**Organization success:**
“The organization has a trustworthy history of records about each known thing, even when identity is messy.”

## 4. Real-world journey

### 4.1 Normal path

1. Organization has a known set of subjects or an activity where subjects can be selected.
2. Field recorder starts a capture activity.
3. Field recorder searches for or selects the subject.
4. Field recorder confirms enough subject details to avoid selecting the wrong one.
5. Field recorder records the required information.
6. The record is saved immediately, including its subject link.
7. Later, users can view the subject’s history and see this record among other records about the same thing.

### 4.2 Missing-subject path

1. Field recorder attempts to find the subject.
2. The subject is not present in the local list.
3. The worker must still continue if the operational reality requires it.
4. The worker may create a candidate subject, use a temporary local entry, or record enough details for later matching.
5. On sync, the new or candidate subject may be reviewed.
6. If it duplicates an existing subject, the duplicate is surfaced for review rather than silently merged.

### 4.3 Wrong-subject path

1. A record is captured against the wrong subject.
2. The mistake is noticed later by the worker, supervisor, or coordinator.
3. A correction path is needed.
4. The original record remains traceable.
5. The corrected view should show the intended subject linkage, while preserving the fact that the original was recorded against the wrong subject.

### 4.4 Duplicate-subject path

1. Two people record about the same real-world thing under different subject identities.
2. Each record is valid as recorded.
3. Later, the organization notices the duplication.
4. A reviewer compares evidence.
5. If the identities are truly the same, the subject identities may be merged through a controlled process.
6. Historical records are not physically rewritten.
7. Current views may show a unified subject history through derived projection.

### 4.5 Ambiguous-identity path

1. A subject changes in the real world: a facility splits, a household moves, a piece of equipment is relabeled, or an organizational unit is reorganized.
2. Older records continue to point at the original identity.
3. Newer records may point at a successor or different identity.
4. Users need to understand the continuity or discontinuity without losing the historical record.
5. The platform must not pretend the identity question was always clean.

### 4.6 Offline path

1. Field recorder works offline with the last synced subject list.
2. Another actor may update, deactivate, merge, or duplicate the subject elsewhere.
3. Field recorder captures new records against the subject identity available locally.
4. On sync, the event is accepted.
5. Stale or conflicting subject conditions are surfaced for review if needed.
6. The subject history view must distinguish when work was done from when it became centrally visible.

## 5. Domain-neutral artifacts

| Artifact                            | Purpose                                                           | Created by                              | Used by                                        | Changes over time?                | Trust requirement                                            |
| ----------------------------------- | ----------------------------------------------------------------- | --------------------------------------- | ---------------------------------------------- | --------------------------------- | ------------------------------------------------------------ |
| Subject-linked record               | Records information about a specific known thing                  | Field recorder                          | Field worker, supervisor, coordinator, reports | No, the record remains historical | Must stay attached to the subject reference used at creation |
| Subject lookup list                 | Lets workers find known things                                    | Coordinator, import process, projection | Field worker                                   | Yes                               | Must be scoped, current enough, and usable offline           |
| Subject summary                     | Helps the worker confirm the right subject                        | Projection / registry view              | Field worker, supervisor                       | Yes                               | Must reduce wrong-subject selection                          |
| Subject history view                | Shows records associated with a subject over time                 | Projection / reporting view             | Supervisor, coordinator, auditor               | Yes                               | Must preserve historical meaning                             |
| Candidate subject                   | Lets work continue when the subject is missing                    | Field worker or import process          | Supervisor, coordinator                        | Yes, until confirmed or merged    | Must not silently become canonical without review            |
| Duplicate candidate marker          | Signals that two subjects may represent the same real-world thing | System or reviewer                      | Supervisor, coordinator                        | Yes, until resolved               | Must not force automatic merge                               |
| Wrong-link correction note          | Records that a record may have been attached to the wrong subject | Authorized actor                        | Supervisor, auditor, reporting                 | Yes                               | Must not erase the original subject reference                |
| Subject merge/split review artifact | Supports identity correction                                      | Supervisor/coordinator                  | Registry steward, auditor                      | Yes                               | Must preserve old references                                 |
| Subject-scoped report extract       | Summarizes records for one subject or group of subjects           | Reporting process                       | Coordinator, supervisor                        | Yes                               | Must show freshness and unresolved identity issues           |

## 6. Concrete domain exemplar

### Exemplar: Water-point inspection linked to a specific water point

**Domain:** WASH / infrastructure monitoring
**Organization:** NGO maintaining rural water infrastructure
**Concrete artifact names:** Water-point list, water-point condition record, duplicate water-point review, water-point history, repair-needed report
**Concrete user language:** “I inspected this specific pump and recorded its condition.”

### Concrete journey

1. A field worker arrives at a water point.
2. They search for the water point by name, code, village, or nearby landmark.
3. They confirm it is the correct water point.
4. They record:

   * whether water is flowing;
   * pump condition;
   * visible damage;
   * whether repair is needed;
   * optional note.
5. The record is saved while offline.
6. Later, the supervisor sees the record under that water point’s history.
7. Another worker may have created a duplicate water-point entry with a slightly different name.
8. The supervisor sees a duplicate candidate and reviews whether both entries represent the same real-world water point.
9. If they are the same, the duplicate can be resolved through controlled identity handling.
10. The original inspection record remains traceable.

### Where this example may mislead platform design

* It may imply that all subject-linked records are about physical infrastructure; they are not.
* It may understate sensitivity for person or household subjects.
* It may make duplicate detection look simple; real duplicates often require human judgment.
* It may encourage registry lifecycle design too early. S01 is about linked capture; full registry maintenance belongs to S06.
* It may encourage assuming the subject list is always complete. S01 must handle missing and stale subject lists.

## 7. Platform fit under current accepted architecture

| User need                          | Likely settled platform vocabulary                           | Fit assessment                     |
| ---------------------------------- | ------------------------------------------------------------ | ---------------------------------- |
| Link a record to a known thing     | `subject_ref`, typed identity reference                      | Strong fit                         |
| Keep the record durable            | append-only `event`, `capture`, `payload`                    | Strong fit                         |
| Keep subject history visible       | projection/read model                                        | Strong fit                         |
| Preserve old references            | original `subject_ref`, no physical re-reference             | Strong fit                         |
| Handle duplicate subjects          | subject lineage, duplicate identity flag, merge review       | Strong fit, spec details open      |
| Handle wrong or ambiguous identity | correction as new event, SubjectSplit where applicable       | Good fit, policy/spec details open |
| Continue offline                   | client-generated IDs, device causal metadata, sync watermark | Strong fit                         |
| Handle stale subject knowledge     | accept-and-flag, stale reference detection                   | Strong fit                         |
| Avoid silently resolving ambiguity | manual review for identity conflicts                         | Strong fit                         |
| Keep simple capture simple         | S00 simplicity baseline                                      | Critical guardrail                 |

## 8. Fit assessment

### 8.1 Strong fit

S01 fits the accepted architecture well. The platform already has a structural place for the subject a record is about, preserves historical references, allows offline-created events, and treats identity ambiguity as something to surface rather than silently overwrite.

### 8.2 Weak fit

The product surface is not yet proven. Field users may not think in terms of “subjects.” They may think in terms of “site,” “person,” “household,” “school,” “machine,” “pump,” “case,” “client,” “farm,” or “location.” The platform must support subject-linked capture without exposing architectural identity vocabulary as the primary user language.

### 8.3 Missing evidence

* What kinds of subjects do early deployments need to link records to?
* Are subjects mostly selected from a known list, scanned from a code, searched by name, selected by geography, or created in the field?
* How often are subject lists incomplete or stale?
* What minimum details help field workers avoid selecting the wrong subject?
* How do workers currently handle “I cannot find the thing”?
* Who is allowed to create a new subject while working?
* Who is allowed to correct a wrong subject link?
* How common are duplicate subjects?
* Which duplicate cases require human judgment?
* Do users need subject history in the field, or mainly after sync?
* Which subject categories are sensitive enough to constrain lookup visibility?

### 8.4 Risk of false fit

The architecture may support subject-linked records, but the product may still fail if lookup is slow, subject names are ambiguous, field users cannot identify the right subject offline, or duplicate review becomes too technical. S01 must not make field capture depend on full registry mastery.

## 9. Gap routing

### Gap 1 — Subject-linked capture setup path

**Short name:** Subject-linked capture setup
**Classification:** Platform-spec detail gap
**Baseline-extension category:** Not applicable
**Current owner or likely decision path:** Platform-spec detailing
**Baseline item affected:** Basic capture setup extended with subject selection/linking
**Why it is still open:** The architecture settles `subject_ref`, but not the coordinator-facing setup path for a capture activity that requires selecting a subject.
**Closure path:** Platform-spec detailing
**Evidence needed before closure:** Examples of subject-linked capture across at least two domains.

### Gap 2 — Subject lookup language and confirmation

**Short name:** Subject lookup fit
**Classification:** Product/problem evidence gap
**Baseline-extension category:** Not applicable
**Current owner or likely decision path:** Product discovery / scenario thickening
**Baseline item affected:** User-facing vocabulary and confirmation behavior for subject-linked capture
**Why it is still open:** Users may not understand or use “subject” language; they need domain-specific lookup and confirmation cues.
**Closure path:** Product discovery / scenario thickening
**Evidence needed before closure:** Field artifacts showing how workers identify the right thing today.

### Gap 3 — Missing subject while offline

**Short name:** Missing offline subject path
**Classification:** Platform-spec detail gap
**Baseline-extension category:** Not applicable
**Current owner or likely decision path:** Platform-spec detailing
**Baseline item affected:** Offline event creation, subject identity, duplicate detection, accept-and-flag
**Why it is still open:** The architecture permits client-generated IDs and accepts offline work, but the exact product behavior when the subject is missing locally is not settled.
**Closure path:** Platform-spec detailing
**Evidence needed before closure:** Real workflows where workers cannot find the subject but must continue.

### Gap 4 — Wrong subject link correction

**Short name:** Wrong subject link correction
**Classification:** Platform-spec detail gap
**Baseline-extension category:** Not applicable
**Current owner or likely decision path:** Platform-spec detailing, with operational policy support
**Baseline item affected:** Append-only correction, original subject reference, subject history projection
**Why it is still open:** The architecture preserves original references, but does not specify user-facing correction behavior when a record was attached to the wrong subject.
**Closure path:** Platform-spec detailing
**Evidence needed before closure:** Real examples of mistaken subject selection and correction practice.

### Gap 5 — Duplicate subject review for linked capture

**Short name:** Linked-capture duplicate subject review
**Classification:** Platform-spec detail gap
**Baseline-extension category:** Not applicable
**Current owner or likely decision path:** Platform-spec detailing
**Baseline item affected:** Subject identity conflict, merge/split review, projection-derived subject history
**Why it is still open:** Subject lineage is settled, but detection cues, reviewer comparison, and user-facing duplicate handling are not specified.
**Closure path:** Platform-spec detailing
**Evidence needed before closure:** Examples of duplicate subject patterns and how organizations resolve them.

### Gap 6 — Who may create or correct subject links

**Short name:** Subject-link authority policy
**Classification:** Operational policy gap
**Baseline-extension category:** Not applicable
**Current owner or likely decision path:** Operational policy definition, with platform-spec support
**Baseline item affected:** Assignment-based access and role-action table
**Why it is still open:** The architecture supports assignment-based access, but operational roles for creating new subjects, correcting links, and approving duplicate resolution are not defined.
**Closure path:** Operational policy definition
**Evidence needed before closure:** Organizational policy examples by subject sensitivity level.

### Gap 7 — Subject history visibility in field

**Short name:** Offline subject history visibility
**Classification:** Platform-spec detail gap
**Baseline-extension category:** Not applicable
**Current owner or likely decision path:** Platform-spec detailing
**Baseline item affected:** Sync scope equals access scope, selective sync, projection-derived subject history
**Why it is still open:** S01 needs subject history, but the exact minimum history needed on-device is not specified and may vary by role, sensitivity, and scale.
**Closure path:** Platform-spec detailing
**Evidence needed before closure:** Field tasks that require prior subject history to act correctly.

## 10. Output decision

**Current packet status:** Ready to feed synthesis; not ready to freeze platform-spec details.

**Reason:**
S01 has strong architecture fit, but the user-facing subject lookup path, missing-subject behavior, wrong-link correction, duplicate review, and subject-history visibility need validation before becoming platform-spec.

## 11. Acceptance criteria for downstream platform-spec work

A later S01 platform-spec section should be accepted only if it satisfies:

1. A field user can link a capture record to a known subject without understanding architecture vocabulary.
2. A field user can continue when the subject is missing or stale locally.
3. Subject-linked records remain durable and traceable.
4. Records preserve the subject reference used when they were created.
5. Subject history is visible through derived views without rewriting old events.
6. Duplicate subject candidates can be surfaced without automatic destructive merge.
7. Wrong subject links can be corrected without erasing the original record.
8. Offline records about a subject are accepted and reconciled later.
9. Subject visibility follows access scope.
10. Sensitive subject categories do not require field-level sensitivity or authority envelope fields.
11. S01 does not make S00 standalone capture harder.
12. Full registry lifecycle complexity remains in S06 unless explicitly needed.
