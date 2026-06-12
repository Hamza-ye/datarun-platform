# Scenario User-Fit Packet — S06: Maintaining a Known Set of Things

## 1. Scenario frame

**Scenario ID:** S06
**Scenario title:** Maintaining a Known Set of Things
**Scenario role in platform learning:** Foundational identity and registry scenario

**Source scenario summary:**
An organization keeps track of a recognized set of real-world things: facilities, equipment, people, locations, areas, households, groups, or organizational units. These things are not static. New ones appear, details change, some become inactive, some are duplicated, and some need periodic verification.

**Why this scenario matters:**
This scenario tests whether the platform can support persistent operational subjects over time, not just isolated records. Many downstream scenarios depend on this: recurring reporting, assignment, visits, case follow-up, distribution, coordination, review, and reporting.

**What this scenario must not decide:**
This packet does not decide database schema, registry UI, duplicate-matching algorithm, API shape, storage model, exact merge workflow, exact subject type taxonomy, or operational governance policy.

## 2. User and organization fit

### 2.1 Organization profile

**Organization type:** Organization that maintains a working registry of things it operates on, monitors, supports, supplies, inspects, or reports about.
**Operational domain:** Domain-neutral; could apply to facilities, water points, households, farms, schools, equipment, stock locations, community groups, staff, service points, or program participants.
**Scale:** From hundreds to millions of subjects.
**Connectivity profile:** Registry updates may happen offline in the field; validation and duplicate review may require online or supervisor/coordinator access.
**Operational maturity:** Could begin from paper lists, spreadsheets, legacy databases, imported lists, local field knowledge, or fragmented program-specific registries.
**Sensitivity pressure:** Depends on subject type. Facility/equipment registries may be routine; household/person registries may be elevated or restricted.
**Current workaround:** Organizations often maintain lists in spreadsheets or separate systems, then field teams carry outdated copies or create local duplicates when they cannot find the right entry.

### 2.2 Personas by operational surface

#### Persona A — Field maintainer

**Operational surface:** Field execution
**Real-world responsibility:** Identify, update, verify, or add entries while doing operational work.
**Work context:** Mobile, sometimes offline, often working from local knowledge and partial lists.
**Primary intent:** Find the right thing, confirm its details, or record that it has changed.
**What they need to see or do:** Search/select a known thing, create a new one when genuinely missing, update observed details, mark verification status, record inactive/moved/changed conditions where allowed.
**What they need to trust:** They are updating the right thing and their offline changes will not disappear.
**What they should not need to understand:** Subject lineage, alias projection, stale-reference flags, merge/split semantics, or registry projection internals.
**Platform authority mapping:** Not assumed here. If needed, map through accepted `actor`, `assignment`, `role`, and `scope`.

#### Persona B — Supervisor / registry reviewer

**Operational surface:** Supervision / review
**Real-world responsibility:** Check whether field updates are valid, identify duplicates, approve or reject sensitive changes, and resolve ambiguous entries.
**Work context:** Intermittently connected; may need side-by-side comparison of old/new details and duplicate candidates.
**Primary intent:** Keep the registry usable without destroying history.
**What they need to trust:** The system shows who changed what, when, from which prior state, and whether the change conflicts with other work.
**What they should not need to understand:** Physical event storage or projection rebuild mechanics.

#### Persona C — Coordinator / registry steward

**Operational surface:** Coordination / administration
**Real-world responsibility:** Define which types of things the organization tracks and which details matter.
**Work context:** More likely online, responsible for data quality and operational readiness.
**Primary intent:** Maintain a trustworthy current list while preserving the history needed for audit, reporting, and continuity.
**What they need to trust:** Registry evolution will not invalidate existing work or silently lose references.
**What they should not need to understand:** Low-level sync mechanics, event envelope internals, or implementation storage design.

#### Persona D — Auditor / external reviewer

**Operational surface:** Audit / external review
**Real-world responsibility:** Verify that registry changes were justified and traceable.
**Work context:** Periodic or targeted review.
**Primary intent:** See the chain of changes without relying only on the current registry view.
**What they need to trust:** The current view is derived from preserved historical facts, not overwritten records.
**Platform authority mapping:** Auditor/query access is not assumed; route separately if needed.

## 3. User intent

### 3.1 Jobs to be done

**Field maintainer job:**
When I encounter a real-world thing the organization works with, I need to find or update its record, so that future work uses the correct current information, without losing what was known before.

**Supervisor job:**
When registry changes are submitted, I need to verify whether they are accurate, duplicated, stale, or conflicting, so that the organization does not act on bad identity or outdated details.

**Coordinator job:**
When the organization’s working set changes, I need the registry to stay current and trustworthy, so that assignments, reporting, visits, and operations use the right subjects.

**Auditor/reviewer job:**
When I inspect registry history, I need to see how a current entry came to be, so that I can judge accountability and data quality.

### 3.2 Intent categories

* Maintain a known thing
* Record changes over time
* Preserve accountability
* Resolve ambiguity
* Verify or review something
* Adapt setup over time
* Support future assignment, reporting, visits, and workflow

### 3.3 Success from the user’s point of view

**Field maintainer success:**
“I found the right entry or created a new one only when needed, and my update is saved even offline.”

**Supervisor success:**
“I can see whether this change is valid, duplicated, stale, or needs review.”

**Coordinator success:**
“The registry stays usable as the organization changes, without becoming a mess of duplicates and hidden edits.”

**Auditor success:**
“I can reconstruct who changed the registry and why.”

## 4. Real-world journey

### 4.1 Normal path

1. Organization starts with a known set of subjects.
2. Coordinator defines which details matter for those subjects.
3. Field maintainer searches for a subject during work.
4. Field maintainer confirms, updates, or adds details.
5. The update is saved as an accountable record.
6. A projection or current view shows the latest known details.
7. Supervisor or coordinator reviews changes where required.

### 4.2 Offline / stale path

1. Field maintainer works offline with the last synced registry.
2. The subject’s details may have changed centrally since last sync.
3. The field maintainer updates based on local observation.
4. Another user may update the same subject while the first device is offline.
5. On sync, both updates are preserved.
6. Stale or conflicting conditions are surfaced rather than silently overwritten.
7. The current view is derived after conflict/flag handling.

### 4.3 Duplicate / ambiguity path

1. Field maintainer cannot find an existing subject.
2. They create a new entry or record a candidate new entry.
3. Later, the system or reviewer identifies that it may duplicate an existing subject.
4. The duplicate is reviewed.
5. If it is truly the same thing, a merge path can alias the duplicate into the surviving identity.
6. Historical records remain attached to the IDs they originally referenced.
7. Projection can show a unified current view after aliasing.

### 4.4 Deactivation / lifecycle path

1. A subject becomes inactive, closed, moved, retired, replaced, split, or no longer operationally relevant.
2. The organization records that lifecycle change.
3. Existing historical work remains intelligible.
4. New work should avoid the inactive subject unless explicitly allowed.
5. Offline stale work against the old state may still arrive and should be accepted and flagged where needed.

### 4.5 Longitudinal path

Over months or years, the registry accumulates additions, corrections, verifications, deactivations, duplicates, merges, splits, and shape changes. The organization needs both a current operational view and a traceable history.

## 5. Domain-neutral artifacts

| Artifact                    | Purpose                                               | Created by                                    | Used by                           | Changes over time?            | Trust requirement                                        |
| --------------------------- | ----------------------------------------------------- | --------------------------------------------- | --------------------------------- | ----------------------------- | -------------------------------------------------------- |
| Registry entry              | Represents a known real-world thing                   | Field maintainer, coordinator, import process | Field teams, supervisors, reports | Yes                           | Must preserve identity and history                       |
| Registry update record      | Records a change to known details                     | Field maintainer or authorized actor          | Supervisors, projections, reports | No, each update is historical | Must show who changed what and when                      |
| Verification note           | Confirms the entry is still accurate                  | Field maintainer or supervisor                | Coordinator, reports              | Yes                           | Must distinguish verified from merely present            |
| Duplicate candidate marker  | Indicates two entries may represent the same thing    | System or reviewer                            | Supervisor, coordinator           | Yes, until resolved           | Must not merge automatically without valid authority     |
| Merge/split review artifact | Supports identity correction                          | Supervisor/coordinator                        | Registry steward, auditor         | Yes                           | Must preserve original references                        |
| Inactive/deactivated marker | Shows subject is no longer active for new work        | Field maintainer, supervisor, coordinator     | Field teams, reports              | Yes                           | Must not break historical references                     |
| Registry extract            | Operational list for lookup, assignment, or reporting | System projection/report                      | Field worker, coordinator         | Yes                           | Must show freshness and unresolved issues where relevant |

## 6. Concrete domain exemplar

### Exemplar: Rural facility registry

**Domain:** Health / service delivery
**Organization:** Regional health program maintaining a list of clinics and service points
**Concrete artifact names:** Facility registry, facility update form, verification visit note, duplicate facility review, inactive facility marker
**Concrete user language:** “This clinic changed name,” “this facility closed,” “this facility is actually the same as another one,” “this site needs verification.”

### Concrete journey

1. A coordinator imports or creates an initial list of facilities.
2. A field worker visits a facility and notices the name or service availability has changed.
3. The worker updates the facility details offline.
4. Another supervisor updates the same facility centrally before the worker syncs.
5. On sync, both updates are preserved.
6. If the updates conflict, the record is flagged for review.
7. A supervisor decides which details should appear in the current facility view.
8. Historical observations remain tied to the subject identity used when they were created.

### Where this example may mislead platform design

* It may imply that all registries are facilities; they are not.
* It may understate sensitivity for person/household registries.
* It may make merge/split look like a routine field action; accepted architecture makes merge/split online-only and server-validated.
* It may encourage exact duplicate-matching algorithm design too early.
* It may imply registry UI design, which is a routed product/spec lane rather than a decision made here.

## 7. Platform fit under current accepted architecture

| User need                                   | Likely settled platform vocabulary                            | Fit assessment                           |
| ------------------------------------------- | ------------------------------------------------------------- | ---------------------------------------- |
| Represent a known real-world thing          | `subject`, `subject_ref`, typed identity reference            | Strong fit                               |
| Record changes over time                    | append-only `event`, `capture`, `payload`                     | Strong fit                               |
| Show current known details                  | `projection`, `read model`, current state as derived          | Strong fit                               |
| Preserve historical references              | no physical re-reference, original `subject_ref`              | Strong fit                               |
| Handle duplicates                           | `SubjectsMerged`, alias mapping, raw-reference detection      | Strong fit, spec details open            |
| Correct wrong merges/splits                 | `SubjectSplit`, archived source, successors                   | Strong fit, operational policy/spec open |
| Work offline                                | client-generated IDs, device causal metadata, accept-and-flag | Strong fit                               |
| Handle stale changes                        | `sync_watermark`, `stale_reference`, accept-and-flag          | Strong fit                               |
| Restrict who sees or edits registry entries | assignment-based access, sync scope = access scope            | Strong fit, role-action spec open        |
| Keep registry current without mutable truth | append-only events + projection                               | Strong fit                               |
| Deactivate without breaking history         | ordinary configured capture if domain-local; platform entity lifecycle otherwise | Near-future route through S06/BAR-105 before implementation |

### 7.1 Current standing caveat

Current platform standing supports subject-linked capture, preserved
`subject_ref` history, accepted subject-history backfill, and lineage-aware
identity handling. It does not make generic registry lifecycle a settled
product surface. S06/entity lifecycle is deferred from the current baseline so
the platform can stabilize surrounding slices first; it may still be needed for
early deployments and should stay in the near-future product-deployment lane.

Use this packet to gather product evidence and route a successor decision. Do
not use it to implement canonical inactive/active lifecycle state,
discovered-unit lifecycle, registry import/export, merge/split UX, or broad
audit/history reads without a bounded S06/BAR-105 successor route.

## 8. Fit assessment

### 8.1 Strong fit

S06 fits the accepted architecture as product pressure. The architecture
already distinguishes durable events from derived current views, treats
subjects as typed identity references, preserves historical references, and
supports lineage evidence. That support is not the same as an accepted generic
registry lifecycle product.

### 8.2 Weak fit

The exact user-facing registry model is not yet specified. Users may not think in terms of “subject,” “identity,” “merge,” or “projection.” They may expect a simple list with edit history, verification status, and review flows.

### 8.3 Missing evidence

* What kinds of things do target organizations maintain as registries?
* Are registry entries created centrally, imported, field-created, or all three?
* Who is allowed to add a new entry?
* Who is allowed to edit key identity fields?
* Which changes need review?
* How common are duplicates?
* What does “inactive” mean operationally?
* Do users need merge/split, or only duplicate review and deactivation in early deployments?
* What current artifacts do organizations use: spreadsheets, paper lists, government registries, local ledgers, WhatsApp lists?
* How sensitive are different subject categories?
* How should field users behave when they cannot find a subject offline?

### 8.4 Risk of false fit

The architecture supports subject identity and lineage, but real organizations may need simpler operational language: “list,” “site,” “household,” “facility,” “equipment,” “record,” “inactive,” “needs verification.” If the product exposes architectural language too directly, S06 may feel technical even if the primitives are correct.

## 9. Gap routing

### Gap 1 — Registry setup and lifecycle language

**Short name:** Registry setup language
**Classification:** Product/problem evidence gap
**Baseline-extension category:** Not applicable
**Current owner or likely decision path:** Product discovery / scenario thickening
**Baseline item affected:** Product vocabulary around subject registries
**Why it is still open:** The architecture settles `subject`, but users may describe registries through domain terms such as facility, household, site, asset, school, group, or equipment.
**Closure path:** Product discovery / scenario thickening
**Evidence needed before closure:** Artifact review and SME interviews across at least two domains.

### Gap 2 — Registry entry lifecycle states

**Short name:** Registry lifecycle state vocabulary
**Classification:** Platform-spec detail gap
**Baseline-extension category:** Not applicable
**Current owner or likely decision path:** Product discovery, then S06/BAR-105 successor route before implementation
**Baseline item affected:** Subject lifecycle projection under accepted subject identity boundaries
**Why it is still open:** The architecture has identity and lineage support, but ordinary operational lifecycle states such as inactive, closed, moved, pending verification, or duplicate candidate are not accepted baseline behavior.
**Closure path:** Product/platform decision route, then platform-spec detailing if approved
**Evidence needed before closure:** Real examples of subject deactivation, reactivation, verification, and closure.

### Gap 3 — Duplicate review workflow

**Short name:** Duplicate registry entry handling
**Classification:** Platform-spec detail gap
**Baseline-extension category:** Not applicable
**Current owner or likely decision path:** Platform-spec detailing for candidate evidence; S06/BAR-105 successor route before productizing merge/split UX
**Baseline item affected:** Subject merge/split and conflict resolution boundaries
**Why it is still open:** Merge/split architecture is settled, but user-facing candidate detection, review, evidence comparison, and resolution workflow are not specified.
**Closure path:** Platform-spec detailing
**Evidence needed before closure:** Examples of duplicate handling in real registries.

### Gap 4 — Who may edit registry identity fields

**Short name:** Registry edit authority
**Classification:** Operational policy gap
**Baseline-extension category:** Not applicable
**Current owner or likely decision path:** Operational policy definition, with S06/BAR-105 successor routing if the policy requires platform lifecycle behavior
**Baseline item affected:** Assignment-based access and role-action table
**Why it is still open:** The architecture supports assignment-based access but does not define which operational roles may create, edit, deactivate, merge, or split registry entries.
**Closure path:** Operational policy definition
**Evidence needed before closure:** Organizational governance practices and risk classification by subject type.

### Gap 5 — Field-created subjects while offline

**Short name:** Offline registry creation rules
**Classification:** Platform-spec detail gap
**Baseline-extension category:** Not applicable
**Current owner or likely decision path:** Product discovery and platform-spec detailing only for candidate capture; S06/BAR-105 successor route for canonical subject creation lifecycle
**Baseline item affected:** Client-generated identity, subject registry, duplicate detection, accept-and-flag
**Why it is still open:** The architecture permits client-generated IDs and offline capture, but the exact rules for creating candidate subjects, promoting them, and governing later review are not specified.
**Closure path:** Platform-spec detailing for unpromoted candidate capture; successor route for canonical registry lifecycle
**Evidence needed before closure:** Field workflows where the user cannot find a subject but must continue working.

## 10. Output decision

**Current packet status:** Near-future product-deployment lane; needs SME validation and successor routing before any registry-lifecycle implementation.

**Reason:**
The architecture supports the scenario pressure, and early deployments may need
registry lifecycle. Current standing keeps S06/entity lifecycle outside the
accepted baseline until the surrounding slices are stable and the BAR-105/S06
route is promoted. Real-world registry lifecycle, user vocabulary, authority
model, duplicate-handling practices, and promotion rules need validation and
routed decision work before platform-spec behavior is frozen.

## 11. Acceptance criteria for downstream platform-spec work

A routed S06/BAR-105 platform-spec section should be accepted only if it satisfies:

1. Users can distinguish “current registry view” from preserved history without seeing architecture vocabulary.
2. A field user can find and update a known subject offline.
3. A field user has a safe path when the subject is missing offline.
4. Updates do not overwrite historical facts.
5. Old records remain tied to the subject identity originally referenced.
6. Duplicate candidates can be reviewed without automatic destructive merge.
7. Merge and split behavior preserves historical references.
8. Inactive/deactivated subjects do not break historical work, after a routed
   lifecycle decision if the product needs platform lifecycle state.
9. Registry changes can be scoped by assignment/access rules.
10. Sensitive registry categories can be handled without field-level sensitivity or envelope authority fields.
11. The basic S00 capture path is not made harder by adding registry support.
