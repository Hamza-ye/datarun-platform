# Scenario User-Fit Synthesis — S00, S01, S06, S06b, Access Control, S19

## 1. Status

**Document type:** Product/problem evidence synthesis
**Input packets:**

* S00 — Recording Structured Information
* S01 — Entity-Linked Capture
* S06 — Maintaining a Known Set of Things
* S06b — When the Shape of Information Changes
* Access Control and Visibility
* S19 — Offline-First Work and Sync

**Purpose:**
Extract the shared user needs, domain-neutral artifacts, recurring product risks, repeated gaps, and revised platform-spec candidates from the six foundational user-fit packets.

**Boundary:**
This synthesis does not create architecture. It does not add event fields, event types, identity categories, access primitives, workflow primitives, storage models, APIs, UI commitments, sync protocols, or operational policies.

**Current use:**
This synthesis is the immediate input to Candidate 1 platform-spec drafting. Candidate 1 should now be reframed around basic usable deployment, not only standalone capture.

## 2. Foundation set

The six packets form the minimum product foundation for early usable deployments:

```txt
S00  record structured information
S01  attach records to known things
S06  maintain the known things over time
S06b change what is collected without breaking history
Access control decide who can see and act
S19  keep working offline and reconcile later
```

Together, they define the first coherent product surface:

```txt
authorized user
→ receives assigned working set
→ captures standalone or subject-linked records
→ can work offline
→ syncs later
→ old records remain meaningful
→ stale, duplicate, access, shape, or identity issues are surfaced
→ supervisors/coordinators see current derived views with freshness and unresolved issues
```

This is still not workflow-heavy. It is the baseline operational substrate.

## 3. Cross-scenario product thesis

The first platform product must not be “a form builder” only.

It must be a small operational system where:

1. A coordinator can define what needs to be recorded.
2. A coordinator can decide who is responsible for what.
3. A field user receives only the relevant work and records.
4. A field user can capture immediately, including offline.
5. A field user can attach a record to a known thing where needed.
6. A field user can continue when a known thing is missing or stale.
7. The platform preserves the original record, actor, subject link, shape version, and sync context.
8. The platform derives current views without rewriting history.
9. The platform surfaces stale or conflicting work for review.
10. Supervisors and coordinators can tell what is fresh, pending, unresolved, old-version, duplicated, or outside current responsibility.

## 4. Shared user intent

Across the six packets, users are not asking for architecture vocabulary.

They are asking for:

| User intent         | Product-language version                                           |
| ------------------- | ------------------------------------------------------------------ |
| Capture             | “Let me record this now.”                                          |
| Offline continuity  | “Do not stop me because there is no network.”                      |
| Subject linkage     | “This record is about this specific thing.”                        |
| Registry continuity | “Keep the list current without losing history.”                    |
| Shape evolution     | “Let us change what we collect without breaking old work.”         |
| Access control      | “Show people only what they are responsible for.”                  |
| Accountability      | “Show who did what, when, and under which responsibility.”         |
| Review              | “Show me what needs attention.”                                    |
| Freshness           | “Tell me how current this view is.”                                |
| Conflict handling   | “Do not silently overwrite; show the issue to the right reviewer.” |

## 5. Shared operational surfaces

| Surface                       | Shared need across the six packets                                                                                                               |
| ----------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------ |
| Field execution               | Immediate capture, subject lookup, local save, offline confidence, scoped working set, simple action availability.                               |
| Supervision / review          | Review records, subject history, stale/offline work, duplicate candidates, wrong links, stale authority, old-version records, unresolved issues. |
| Coordination / administration | Define capture setup, manage responsibility, maintain known things, deploy shape changes, monitor sync/config/access rollout.                    |
| Reporting / oversight         | See current derived views, freshness, unresolved flags, version differences, scoped summaries, and historical traceability.                      |
| Audit / external review       | Reconstruct original facts, corrections, subject identity history, assignment history, access context, and sync timing.                          |
| Support / operations          | Handle failed sync, device loss, long offline gaps, worker transfer, offboarding, and local data after access loss.                              |

## 6. Shared artifacts

| Artifact                            | Appears in                       | Purpose                                                                            |
| ----------------------------------- | -------------------------------- | ---------------------------------------------------------------------------------- |
| Structured record                   | S00, S01, S06, S06b, S19         | Durable fact of what was recorded, observed, updated, or done.                     |
| Subject-linked record               | S01, S06, S19                    | Record tied to a specific real-world thing.                                        |
| Information shape                   | S00, S06b, S19                   | Defines expected fields and validation for a record.                               |
| Shape version                       | S00, S06b, S19                   | Preserves which information rules governed a record.                               |
| Registry entry / known thing        | S01, S06, S06b, S19              | Persistent operational thing that accumulates history.                             |
| Subject lookup list                 | S01, S06, Access, S19            | Lets field users find known things within their allowed scope.                     |
| Candidate subject                   | S01, S06, S19                    | Lets work continue when the subject is missing or stale locally.                   |
| Duplicate candidate marker          | S01, S06, S19                    | Surfaces possible duplicate identity or duplicate capture.                         |
| Wrong-link correction note          | S01, S00, S06                    | Corrects a mistaken subject link without erasing the original.                     |
| Correction note / correction record | S00, S01, S06, S19               | Amends or supersedes prior information without in-place overwrite.                 |
| Assignment                          | Access, S19, S01, S06            | Grants contextual responsibility over role, scope, and time.                       |
| Role-action matrix                  | Access                           | Defines what actions a role may perform under platform-fixed access boundaries.    |
| Scope definition                    | Access, S19                      | Defines where or over what things/activity access applies.                         |
| Temporary access grant              | Access, S19                      | Allows bounded coverage, campaign, or emergency responsibility.                    |
| Assignment timeline                 | Access, S19                      | Reconstructs authority at the time of action.                                      |
| Offline working set                 | S19, Access, S01, S06            | Data and config available on device while disconnected.                            |
| Unsynced queue / pending work list  | S00, S19                         | Shows locally saved work not yet centrally visible.                                |
| Sync status indicator               | S19                              | Shows saved, pending, synced, failed, blocked, or needs review.                    |
| Last synced / freshness indicator   | S19, Access, Reporting           | Prevents central views from being mistaken for live field reality.                 |
| Stale-work flag                     | S00, S01, S06, Access, S19       | Marks stale subject, shape, authority, or state assumptions.                       |
| Conflict review item                | S00, S01, S06, S19               | Presents conflicts needing human judgment.                                         |
| Config freshness view               | S06b, S19                        | Shows which configuration or shape version applied.                                |
| Access-change notice                | Access, S19                      | Tells users when responsibility or visibility changed.                             |
| Scoped report                       | Access, S00, S01, S06, S06b, S19 | Summary constrained by access, freshness, and unresolved issues.                   |
| Audit trail                         | All six                          | Reconstructs who did what, when, about what, under which setup and responsibility. |

## 7. Repeated product-fit risks

### 7.1 Architecture fit does not prove user fit

The accepted architecture can represent the six foundations, but product failure can still occur if the user-facing surfaces are hard to understand.

High-risk surfaces:

* setup language;
* subject lookup;
* missing-subject path;
* sync status;
* stale authority review;
* access explanation;
* shape change warnings;
* duplicate review;
* correction behavior;
* reporting freshness.

### 7.2 Internal vocabulary may leak too early

The architecture vocabulary is useful internally but unsafe as primary product language.

Risky primary user terms:

* event;
* projection;
* `subject_ref`;
* `shape_ref`;
* assignment timeline;
* sync watermark;
* scope containment;
* stale reference;
* authority-as-projection;
* flag resolvability.

Safer user-language anchors:

* record;
* form/checklist;
* thing/site/person/household/equipment;
* assigned area;
* my team;
* saved on this device;
* waiting to sync;
* synced;
* needs review;
* old version;
* no longer active;
* duplicate candidate;
* access ended;
* last updated.

### 7.3 Offline is not just a sync feature

Offline touches every foundation:

* capture must work offline;
* subject lookup must work with stale local lists;
* access must be locally enforceable under last-known authority;
* shape changes may arrive late;
* registry changes may arrive late;
* decisions made offline may conflict with later central state;
* reports must show freshness;
* device loss remains an operational and implementation risk.

### 7.4 Access is not optional for early deployment

Basic capture is not deployable if every user sees everything.

Candidate 1 must include at least a minimal assignment/access frame:

```txt
who can capture
what they can see
where/which subjects they can work with
what happens when access changes while offline
```

It should not implement every access edge case, but it must not ignore access.

### 7.5 Entity-linked capture changes the meaning of “basic”

S00 standalone capture remains the simplicity baseline, but early deployments also need S01. A useful basic deployment must support both:

```txt
plain record
record about a known thing
```

The platform-spec must keep these separable. S01 must not force full registry administration into every simple capture path.

### 7.6 Shape evolution affects trust early

Even early deployments change forms, labels, required fields, and reporting definitions. If old records become confusing after a change, users lose trust quickly.

The product must distinguish:

* old-version record;
* new-version record;
* missing field;
* field not collected in that version;
* deprecated field;
* renamed/reworded field;
* non-comparable field.

### 7.7 Reporting can create false certainty

Supervisors and coordinators may treat visible data as complete data. The six packets repeatedly show this is unsafe.

Every early oversight surface needs freshness and uncertainty language:

```txt
latest synced view
last synced
pending work may exist
needs review
old version
stale assignment
unresolved duplicate
unresolved conflict
```

## 8. Repeated gaps by closure path

### 8.1 Product discovery / scenario thickening

| Gap                                | Why it repeats                                                                                              |
| ---------------------------------- | ----------------------------------------------------------------------------------------------------------- |
| User vocabulary fit                | S00, S01, Access, and S06b all show that users may not speak in platform terms.                             |
| Subject lookup language            | S01 and S06 depend on how workers identify the right thing.                                                 |
| Offline status language            | S19 and S00 depend on users trusting local save/sync state.                                                 |
| Access setup language              | Access and S19 require responsibility changes to be understandable.                                         |
| Shape-change practice              | S06b needs evidence on how organizations actually change forms and registry fields.                         |
| Registry operating model           | S06 needs evidence on whether registries come from imports, field creation, spreadsheets, or mixed sources. |
| Sync-delay interpretation          | Supervisors need evidence about how they currently interpret missing or delayed work.                       |
| Sensitivity categories in practice | Access and S06 need evidence about what data classes are standard, elevated, or restricted.                 |

### 8.2 Platform-spec detail gaps

| Gap                                 | Affected foundation           |
| ----------------------------------- | ----------------------------- |
| Basic capture setup lifecycle       | S00                           |
| Subject-linked capture setup        | S01                           |
| Missing subject while offline       | S01, S06, S19                 |
| Wrong subject link correction       | S01                           |
| Duplicate subject review            | S01, S06                      |
| Registry lifecycle vocabulary       | S06                           |
| Offline registry creation rules     | S06, S19                      |
| Shape change lifecycle guidance     | S06b                          |
| Cross-version reporting semantics   | S06b, S00, S19                |
| In-progress old-shape work behavior | S06b, S19                     |
| Role-action table artifact          | Access                        |
| Temporary access lifecycle          | Access, S19                   |
| Stale authority review              | Access, S19                   |
| Sync freshness semantics            | S19, Reporting                |
| Local data after scope contraction  | Access, S19                   |
| Offline decision boundaries         | S19, later workflow scenarios |
| Conflict review ergonomics          | S00, S01, S06, S19            |
| Reporting freshness semantics       | S19, Access, S06b             |

### 8.3 Implementation/tooling gaps

| Gap                                            | Why not architecture                                                                |
| ---------------------------------------------- | ----------------------------------------------------------------------------------- |
| Shape authoring and diff tooling               | Tooling for accepted shape/version boundary.                                        |
| Subject search and duplicate-candidate tooling | UI/search/matching behavior under accepted identity boundary.                       |
| Sync transport, retry, batching, pagination    | Mechanics under event-as-sync-unit and sync/access boundary.                        |
| Device local storage and durability            | Implementation detail unless it changes event or sync contracts.                    |
| Device loss mitigation                         | Implementation/support design unless a stronger architecture guarantee is proposed. |
| Config rollout dashboard                       | Tooling around accepted atomic config delivery.                                     |
| Projection materialization and indexes         | Implementation detail under derived projection boundary.                            |

### 8.4 Operational policy gaps

| Gap                                  | Why policy                                                         |
| ------------------------------------ | ------------------------------------------------------------------ |
| Who may correct records              | Organization must define authority and review practice.            |
| Who may create/correct subject links | Depends on sensitivity and governance.                             |
| Who may merge/split subjects         | Requires steward/resolver role and review policy.                  |
| Responsibility transfer / handoff    | Organization defines offboarding, leave, and transfer practice.    |
| Shape change approval policy         | Platform can validate mechanically; organization decides approval. |
| Long offline duration policy         | Deployment support rule, not event architecture.                   |
| Device loss procedure                | Support and accountability practice.                               |
| Retention windows after access loss  | Policy plus platform-spec support.                                 |

### 8.5 Architecture decision gaps

These must not be closed inside Candidate 1 unless a formal decision path is started.

| Gap                                       | Why architecture-sensitive                                                 |
| ----------------------------------------- | -------------------------------------------------------------------------- |
| Auditor/query access                      | May bypass assignment-derived access or sync/access equivalence.           |
| Aggregate access semantics                | May expose summaries beyond event/detail access.                           |
| Actor-as-subject delivery rule            | May grant access because actor is also subject, not because of assignment. |
| Field-level sensitivity                   | Rejected baseline; shape/activity sensitivity is the accepted boundary.    |
| New event envelope fields                 | Structural contract change.                                                |
| New event type vocabulary                 | Platform processing contract change.                                       |
| Device-side triggers                      | Rejected baseline.                                                         |
| Deployer-authored access logic            | Rejected baseline.                                                         |
| Deployer-authored state machines          | Rejected baseline.                                                         |
| Automatic resolution of manual-only flags | Rejected baseline.                                                         |

## 9. Revised platform-spec candidates

The first platform-spec candidates should now be revised from the earlier three-packet synthesis.

### Candidate 1 — Basic Operational Capture Setup Spec

**Why first:**
This is the smallest useful deployment surface. It must protect S00 simplicity while acknowledging that real early deployments need subject-linked capture, assignment-aware visibility, offline local save, and shape versioning.

**Primary inputs:** S00, S01, Access, S19
**Secondary inputs:** S06, S06b

**Scope:**

* create a basic capture activity;
* define a record shape;
* optionally require subject selection;
* define who can capture and who can view/review;
* deliver authorized working set to device;
* save work offline;
* sync later;
* show saved/pending/synced/needs-review status;
* preserve original record and corrections;
* preserve subject link as recorded;
* accept stale offline work and surface review items;
* record under the shape version available at capture time;
* show basic freshness in supervision views.

**Non-scope:**

* full registry lifecycle management;
* merge/split implementation details;
* full role-action matrix for every scenario;
* audit/query access beyond assignment-derived access;
* aggregate access divergence;
* advanced workflow patterns;
* event-triggered actions;
* recursive triggers;
* device-side triggers;
* complex reporting transformations.

### Candidate 2 — Registry Entry Lifecycle Spec

**Why second:**
S06 formalizes maintained known things. It should build on Candidate 1’s subject-linked capture, not be forced into Candidate 1.

**Primary inputs:** S06, S01
**Secondary inputs:** Access, S19, S06b

**Scope:**

* create or import known things;
* update details through append-only records;
* derive current registry view;
* support inactive/deactivated lifecycle states;
* surface duplicate candidates;
* support controlled merge/split review;
* preserve original references;
* handle offline-created candidate subjects;
* define verification/review states if needed under accepted workflow boundaries.

### Candidate 3 — Shape Evolution and Config Rollout Spec

**Why third:**
S06b turns setup from one-time definition into controlled evolution.

**Primary inputs:** S06b
**Secondary inputs:** S00, S01, S06, S19, Access

**Scope:**

* add fields;
* deprecate fields;
* rename/relabel fields;
* warn on risky changes;
* handle old-version records;
* handle in-progress old-shape work;
* show config rollout/freshness;
* define cross-version reporting semantics;
* define when breaking changes require formal routing.

### Candidate 4 — Assignment and Visibility Baseline Spec

**Why fourth:**
Access is needed in Candidate 1 but can initially be minimal. A fuller baseline spec should follow once the basic operational path is visible.

**Primary inputs:** Access Control, S19
**Secondary inputs:** S01, S06

**Scope:**

* roles;
* scopes;
* role-action table;
* assignment lifecycle;
* temporary access;
* access change communication;
* stale-authority review;
* local data after access loss;
* scoped visibility;
* sensitivity categories at shape/activity level.

### Candidate 5 — Offline Confidence and Reconciliation Spec

**Why fifth:**
Offline behavior appears in Candidate 1, but full reconciliation needs a dedicated spec because it crosses capture, subject linkage, access, shape evolution, conflict review, and support.

**Primary inputs:** S19
**Secondary inputs:** S00, S01, S06, S06b, Access

**Scope:**

* local saved state;
* pending sync;
* sync failure;
* freshness indicators;
* stale subject review;
* stale authority review;
* old-shape records;
* offline duplicate records;
* offline decision warnings;
* device loss handling boundaries;
* supervisor/coordinator sync health views.

### Candidate 6 — Review, Flag, and Correction Ergonomics Spec

**Why sixth:**
All six packets surface review pressure, but full review behavior should not bloat Candidate 1.

**Primary inputs:** S00, S01, S06, S19, Access
**Secondary inputs:** S06b

**Scope:**

* correction behavior;
* duplicate record review;
* duplicate subject review;
* stale-reference review;
* stale-authority review;
* wrong-link correction;
* conflict queue language;
* designated resolver UX;
* manual-only handling;
* source-chain visibility where relevant.

## 10. Revised Candidate 1 framing

Candidate 1 should no longer be called only “Basic Capture Setup Spec” if that causes standalone-capture tunnel vision.

Recommended title:

```txt
Candidate 1 — Basic Operational Capture Setup Spec
```

Recommended product framing:

```txt
A coordinator can set up a small operational capture activity.
A field user receives the assigned work, can capture with or without a subject link, can work offline, and can sync later.
A supervisor can see the latest synced view, including freshness and records needing attention.
```

Minimum path:

```txt
coordinator defines activity + shape
→ coordinator assigns responsibility
→ field user syncs authorized working set
→ field user captures standalone or subject-linked record
→ record saves offline if needed
→ record syncs later
→ supervisor sees record, freshness, and unresolved issues
→ correction/review can happen without erasing the original
```

Candidate 1 must include four simple variants:

### Variant A — Standalone capture

```txt
open assigned capture
→ fill required details
→ save
→ sync later
→ visible in scoped view
```

### Variant B — Subject-linked capture

```txt
open assigned capture
→ find/select known thing
→ confirm enough details
→ record observation
→ save
→ sync later
→ visible under subject history
```

### Variant C — Missing subject while offline

```txt
open assigned capture
→ cannot find subject locally
→ continue through candidate/new-subject path
→ sync later
→ duplicate/missing-subject review item appears if needed
```

### Variant D — Old config / stale access

```txt
user works under last synced setup
→ central config or assignment changes
→ user syncs old-work later
→ work is preserved
→ stale shape/access conditions are surfaced where needed
```

## 11. Candidate 1 acceptance gates

Candidate 1 should not be accepted unless it satisfies all of these:

1. S00 standalone capture remains simple.
2. S01 subject-linked capture is available without full registry lifecycle complexity.
3. Field users can work offline without understanding sync mechanics.
4. Records created offline are preserved and reconciled later.
5. Field users see understandable local save/sync status.
6. Capture can be scoped by assignment/access.
7. Device data delivery does not exceed access scope.
8. Old-shape work remains interpretable after shape evolution.
9. Subject references are preserved as originally recorded.
10. Missing-subject handling does not silently create canonical duplicates.
11. Wrong-link correction does not erase the original record.
12. Duplicate or conflicting work is surfaced, not silently overwritten.
13. Supervisors can see freshness and unresolved issues.
14. Coordinators are not forced to understand architecture vocabulary to set up basic capture.
15. Candidate 1 does not add event envelope fields.
16. Candidate 1 does not add deployer-authored event types.
17. Candidate 1 does not introduce deployer-authored access logic.
18. Candidate 1 does not introduce field-level sensitivity.
19. Candidate 1 does not introduce device-side triggers.
20. Candidate 1 does not treat projections or reports as source truth.

## 12. What Candidate 1 should deliberately defer

Candidate 1 should explicitly defer:

* full registry stewardship workflow;
* merge/split UX;
* formal auditor/query access;
* aggregate access divergence;
* advanced reporting;
* recurring reporting rhythms;
* supervisor visit patterns;
* review chains;
* multi-step approvals;
* event-triggered actions;
* full workflow pattern inventory;
* offline decision policy for complex approvals;
* peer-to-peer sync;
* device backup/recovery guarantees;
* media attachments;
* external system integration.

## 13. Current synthesis decision

**Status:** Ready to feed Candidate 1 drafting.

**Meaning:**
The six packets are sufficient to begin Candidate 1 platform-spec detailing, provided Candidate 1 is framed as basic operational capture under real deployment constraints, not as a pure form-capture feature.

**Main correction to prior plan:**
Candidate 1 must include minimal access and offline behavior from the start. It must also allow optional subject-linked capture. But it must not absorb full registry lifecycle, full access-control specification, full offline reconciliation mechanics, or full shape-evolution governance.

## 14. Recommended next move

Proceed to:

```txt
Candidate 1 — Basic Operational Capture Setup Spec
```

Suggested structure:

1. Purpose and non-purpose
2. User-facing baseline
3. Setup path
4. Capture path
5. Optional subject-linked path
6. Offline save/sync path
7. Minimal assignment/access path
8. Shape/version handling
9. Review/correction basics
10. Freshness and unresolved issue visibility
11. Explicit deferrals
12. Gap routing
13. Acceptance criteria
