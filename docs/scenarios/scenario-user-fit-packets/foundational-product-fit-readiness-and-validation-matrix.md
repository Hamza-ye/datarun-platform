# Foundational Product-Fit Readiness and Validation Matrix

## 1. Status

**Document type:** Product-fit readiness and validation gate
**Input sources:**

* [s00-user-fit-recording-structured-information.md](s00-user-fit-recording-structured-information.md)
* [s01-user-fit-entity-linked-capture.md](s01-user-fit-entity-linked-capture.md)
* [s06-user-fit-maintaining-a-known-set-of-things.md](s06-user-fit-maintaining-a-known-set-of-things.md)
* [s06b-user-fit-when-the-shape-of-information-changes.md](s06b-user-fit-when-the-shape-of-information-changes.md)
* [access-control-focused-user-fit-packet.md](access-control-focused-user-fit-packet.md)
* [s19-user-fit-offline-first-work-and-sync.md](s19-user-fit-offline-first-work-and-sync.md)
* [scenario-user-fit-synthesis-across-s00-s01-s06-s06b-access-control-S19.md](scenario-user-fit-synthesis-across-s00-s01-s06-s06b-access-control-S19.md)
* Architecture reference layer:

  * `008-authoritative-architecture-map.md`
  * `architecture-decision-records.md`
  * `architecture-vocabulary-anchor-map.md`
  * `gap-routing-playbook.md`
  * `known-gaps-register.md`
* Routing-only evolution context:

  * `escape-hatch-register.md`

**Purpose:**
Prevent the platform from treating architecture compatibility as product validation. This matrix separates:

```txt
works under accepted architecture
≠
fits field users, setup owners, supervisors, and deployment reality
```

**Current use:**
Immediate gate before drafting:

```txt
Candidate 1 — Basic Operational Capture Setup Spec
```

**Boundary:**
This document does not create architecture, platform specification, implementation design, UI design, sync protocol, database shape, or operational policy. It classifies product-fit readiness and routes unresolved work.

## 2. Core rule

A capability is not product-ready just because it maps to accepted architecture.

### 2.1 Architecture fit means

The capability can be expressed without violating settled architecture boundaries, such as:

* append-only events;
* event as atomic write/sync unit;
* final event envelope;
* typed identity references;
* `subject_ref`;
* `shape_ref`;
* optional `activity_ref`;
* assignment-based access;
* sync scope equals access scope;
* authority as projection;
* accept-and-flag;
* detect-before-act;
* projection-derived state;
* bounded configuration;
* no deployer-authored event types;
* no deployer-authored access logic;
* no field-level sensitivity;
* no device-side triggers.

### 2.2 Product fit means

Target users can recognize, understand, perform, and trust the work.

A product-fit claim requires evidence that:

* the user recognizes the task;
* the user language matches the product surface;
* the setup owner can configure the behavior without developer thinking;
* the field worker can complete the flow under realistic conditions;
* the supervisor can interpret the result;
* the user can distinguish saved, pending, synced, stale, corrected, duplicate, or needs-review states;
* the organization can operate the policy around the behavior;
* the behavior does not make S00 harder.

### 2.3 Default decision rule

```txt
If architecture fit is strong but user evidence is thin:
  do not promote the behavior to full platform spec.
  include only a conservative baseline if Candidate 1 needs it.
  route the remaining uncertainty as product discovery or platform-spec detail.
```

## 3. Readiness classification

| Readiness status               | Meaning                                                                                                                            | Candidate 1 handling                                               |
| ------------------------------ | ---------------------------------------------------------------------------------------------------------------------------------- | ------------------------------------------------------------------ |
| Ready for platform-spec        | Architecture boundary is settled and user behavior is basic enough to specify now.                                                 | Include in Candidate 1.                                            |
| Spec with validation caveat    | A conservative baseline can be specified, but wording, UX, or operating assumptions still need validation.                         | Include minimally and mark validation questions.                   |
| Needs user validation first    | The product behavior depends on field evidence; specifying now risks wrong abstraction or overbuilt configuration.                 | Do not freeze detailed behavior in Candidate 1.                    |
| Operational policy needed      | The issue depends on organizational authority, review practice, escalation, retention, support, or governance.                     | Do not solve through platform behavior alone.                      |
| Platform-spec detail gap       | Precise platform behavior is needed under accepted architecture, but the detail is not yet written.                                | Include only if Candidate 1 needs a minimal rule. Otherwise defer. |
| Implementation/tooling gap     | The issue is about UI, storage, sync mechanics, authoring tools, dashboards, APIs, local storage, indexing, or engineering design. | Do not make it architecture or product evidence.                   |
| Architecture decision required | Closing the issue would change a settled boundary or cross a negative boundary.                                                    | Exclude from Candidate 1 unless formal decision path starts.       |
| Escape-hatch trigger required  | A measured evolution path exists but is inactive until metric evidence is produced.                                                | Do not implement. Record only as future routing context.           |

## 4. Evidence confidence

| Evidence status           | Meaning                                                                                           |
| ------------------------- | ------------------------------------------------------------------------------------------------- |
| Strong packet evidence    | Repeated across packets and grounded in core user intent.                                         |
| Plausible but unvalidated | Consistent with packets, but not yet checked with users/SMEs.                                     |
| Thin evidence             | Mentioned as pressure, but actual journey, vocabulary, or operating practice is not thick enough. |
| Missing evidence          | No adequate field/user evidence yet.                                                              |
| Architecture-routed       | Cannot be resolved as product evidence alone because it may change accepted architecture.         |
| Policy-routed             | Requires organizational decision, not only product behavior.                                      |
| Tooling-routed            | Requires implementation or authoring/tooling design.                                              |
| Escape-hatch-routed       | Requires measured trigger evidence before any successor plan.                                     |

## 5. Product-fit matrix

| Product surface                          | User intent                                                           | Architecture fit                                         | Product-fit assumption                                              | Evidence status                           | Readiness                                                 | Overbuild risk                                  | Candidate 1 decision                                   | Validation question                                                                              | Closure path                                                                 |
| ---------------------------------------- | --------------------------------------------------------------------- | -------------------------------------------------------- | ------------------------------------------------------------------- | ----------------------------------------- | --------------------------------------------------------- | ----------------------------------------------- | ------------------------------------------------------ | ------------------------------------------------------------------------------------------------ | ---------------------------------------------------------------------------- |
| Basic standalone capture                 | Record known details immediately.                                     | Strong.                                                  | Field worker understands a simple capture surface.                  | Strong packet evidence.                   | Ready for platform-spec.                                  | Low.                                            | Include.                                               | What do users call the thing they are filling: form, record, checklist, report, visit note?      | Platform-spec detailing.                                                     |
| Required/optional field behavior         | Capture complete enough information.                                  | Strong under shape model.                                | Coordinators can define required fields without overbuilding logic. | Plausible but unvalidated.                | Spec with validation caveat.                              | Form-builder sprawl.                            | Include minimal baseline.                              | Which required fields are operationally mandatory vs nice-to-have?                               | Platform-spec detailing + product discovery.                                 |
| Local save while offline                 | Trust that work is not lost.                                          | Strong.                                                  | Field workers trust a local saved state if clearly shown.           | Strong need, wording unvalidated.         | Spec with validation caveat.                              | Overexplaining sync internals.                  | Include minimally.                                     | What wording makes users trust that saved offline work still counts?                             | Product discovery / scenario thickening.                                     |
| Sync status language                     | Know whether work is saved, pending, synced, failed, or needs review. | Strong architecture support.                             | Users distinguish local save from central visibility.               | Thin evidence.                            | Needs user validation first.                              | Building too many technical statuses.           | Include only basic statuses.                           | Do users understand “synced,” “uploaded,” “submitted,” “received,” and “approved” differently?   | Product discovery / scenario thickening.                                     |
| Field-time vs central visibility         | Understand when work happened vs when office saw it.                  | Strong.                                                  | Supervisors need freshness cues.                                    | Strong packet evidence, thin UI evidence. | Spec with validation caveat.                              | Treating dashboard as live truth.               | Include minimal freshness marker.                      | What decisions become unsafe when records are delayed?                                           | Platform-spec detailing + product discovery.                                 |
| Optional subject-linked capture          | Record about a specific known thing.                                  | Strong via `subject_ref`.                                | Field workers can identify the correct thing.                       | Strong need, lookup evidence thin.        | Spec with validation caveat.                              | Forcing full registry into simple capture.      | Include optional path only.                            | How do workers identify the correct thing today?                                                 | Platform-spec detailing + product discovery.                                 |
| Subject lookup wording                   | Find/select the right known thing.                                    | Strong architecture support.                             | Users can search by name/code/location/details.                     | Thin evidence.                            | Needs user validation first.                              | Overbuilding search/matching.                   | Do not freeze detailed UX.                             | What labels, codes, landmarks, or local names identify the subject?                              | Product discovery / scenario thickening.                                     |
| Subject confirmation cues                | Avoid wrong subject selection.                                        | Compatible.                                              | A small set of visible details reduces wrong selection.             | Missing/thin evidence.                    | Needs user validation first.                              | Too many fields in lookup cards.                | Defer detailed behavior.                               | What minimum details distinguish two similar subjects?                                           | Product discovery / scenario thickening.                                     |
| Missing subject while offline            | Continue when the thing is not locally available.                     | Compatible with offline IDs and accept-and-flag.         | Candidate/new-subject path is acceptable.                           | Plausible but unvalidated.                | Spec with validation caveat.                              | Silently creating canonical duplicates.         | Include conservative candidate path.                   | What do workers do today when the thing is missing from the list?                                | Platform-spec detailing + product discovery.                                 |
| Candidate subject review                 | Prevent candidate entries becoming bad registry truth.                | Strong under subject identity boundaries.                | Supervisors/coordinators can review candidates later.               | Thin evidence.                            | Platform-spec detail gap.                                 | Building registry governance too early.         | Include only if needed by missing-subject path.        | Who reviews candidate entries, and how soon?                                                     | Platform-spec detailing + operational policy definition.                     |
| Duplicate subject review                 | Resolve possibly same real-world thing.                               | Strong lineage boundary; product behavior open.          | Reviewers can compare evidence.                                     | Thin evidence.                            | Needs user validation first.                              | Matching algorithm or merge UX too early.       | Defer detailed review UX.                              | Which duplicate cases are obvious, and which need human judgment?                                | Product discovery / scenario thickening, then platform-spec detailing.       |
| Wrong subject link correction            | Correct a record attached to wrong thing.                             | Compatible with append-only correction.                  | Users understand correction without erasing original.               | Plausible but unvalidated.                | Spec with validation caveat.                              | Hidden re-reference or mutable edit behavior.   | Include correction concept, not full UX.               | Does “correct” mean amend, replace, cancel, reassign, or supersede?                              | Platform-spec detailing + operational policy definition.                     |
| Basic correction of record values        | Fix mistake without losing original.                                  | Strong.                                                  | Supervisor/field worker can understand original vs corrected.       | Strong need, policy thin.                 | Spec with validation caveat.                              | Building full version-control UI.               | Include minimal correction behavior.                   | Who may correct, and is a reason required?                                                       | Platform-spec detailing + operational policy definition.                     |
| Basic assignment/scoped access           | See/do only assigned work.                                            | Strong.                                                  | Early deployments can start with simple scope.                      | Strong packet evidence.                   | Ready for platform-spec baseline.                         | Ignoring access entirely.                       | Include minimally.                                     | What is the first deployment’s simplest responsibility unit: area, activity, subject list, team? | Platform-spec detailing.                                                     |
| Role-action table                        | Define what each role can do.                                         | Accepted as needed, exact artifact open.                 | A small initial role-action set is enough.                          | Thin evidence.                            | Platform-spec detail gap.                                 | Premature complete permission matrix.           | Include minimal baseline only.                         | Which actions must be controlled separately on day one?                                          | Platform-spec detailing.                                                     |
| Access setup language                    | Help coordinator define responsibility.                               | Architecture compatible.                                 | Coordinators think in duties/coverage, not architecture terms.      | Thin evidence.                            | Needs user validation first.                              | Exposing assignment/scope jargon.               | Use plain labels, defer detailed setup UX.             | What artifacts do organizations use today to assign work?                                        | Product discovery / scenario thickening.                                     |
| Temporary access                         | Cover leave, campaigns, emergency expansion.                          | Compatible via time-bounded assignment.                  | Temporary grants are common enough to matter.                       | Plausible but unvalidated.                | Operational policy needed + platform-spec detail gap.     | Overbuilding full temporary-access lifecycle.   | Defer detailed lifecycle.                              | Who grants temporary access and when does it expire?                                             | Operational policy definition + platform-spec detailing.                     |
| Stale authority review                   | Handle work after assignment changed offline.                         | Strong via accept-and-flag.                              | Reviewers can judge stale-authority cases.                          | Thin product evidence.                    | Spec with validation caveat.                              | Making field users resolve authority conflicts. | Mark as review item; defer full UX.                    | What should happen if a worker acts after transfer or expiry?                                    | Platform-spec detailing + operational policy definition.                     |
| Local data after access loss             | Handle data on device after scope contraction.                        | Accepted initial strategy.                               | Retention/removal can be specified later.                           | Policy thin.                              | Platform-spec detail gap + operational policy needed.     | Over-retention or accidental exposure.          | Defer beyond minimal safety statement.                 | How long may old local data remain after responsibility changes?                                 | Platform-spec detailing + operational policy definition.                     |
| Shape version preservation               | Keep old records interpretable.                                       | Strong via `shape_ref`.                                  | Users need old/new distinction.                                     | Strong packet evidence.                   | Ready for platform-spec.                                  | Low if kept technical internally.               | Include.                                               | What user wording should represent “old version”?                                                | Platform-spec detailing + product discovery.                                 |
| In-progress old-shape work               | Let work started under old setup finish.                              | Strong architecture support.                             | Field users should not choose technical versions.                   | Plausible but unvalidated.                | Spec with validation caveat.                              | Version-choice complexity in UI.                | Include conservative rule.                             | When should old in-progress work be completed vs abandoned?                                      | Platform-spec detailing.                                                     |
| Shape-change authoring flow              | Change what is collected.                                             | Architecture bounded, tooling open.                      | Coordinators can add/deprecate fields safely.                       | Thin evidence.                            | Needs user validation first + implementation/tooling gap. | Overbuilding config builder.                    | Defer detailed authoring.                              | How do organizations change forms today?                                                         | Product discovery / scenario thickening, then implementation/tooling design. |
| Shape-change warning                     | Prevent risky changes.                                                | Compatible under bounded config.                         | Coordinators understand warnings.                                   | Thin evidence.                            | Needs user validation first.                              | Too many technical warnings.                    | Include only basic “may affect old/new work” warning.  | Which changes are actually risky to coordinators?                                                | Product discovery / scenario thickening.                                     |
| Cross-version reporting distinction      | Avoid treating “not collected” as “missing.”                          | Compatible.                                              | Reporting users need version-aware meaning.                         | Strong need, semantics open.              | Platform-spec detail gap.                                 | Reporting engine too early.                     | Include basic distinction only.                        | Which fields are compared across old/new records?                                                | Platform-spec detailing.                                                     |
| Registry lifecycle vocabulary            | Mark active, inactive, retired, closed, etc.                          | Strong subject lifecycle support.                        | User terms vary by domain.                                          | Thin evidence.                            | Needs user validation first.                              | Freezing generic lifecycle labels too early.    | Defer detailed vocabulary.                             | What do users call inactive/no-longer-valid things?                                              | Product discovery / scenario thickening.                                     |
| Registry stewardship                     | Maintain known set over time.                                         | Strong architecture support.                             | Early deployments need a lightweight path.                          | Plausible but unvalidated.                | Platform-spec detail gap.                                 | Full MDM-style registry too early.              | Defer to Candidate 2.                                  | Is the registry imported, field-created, spreadsheet-maintained, or mixed?                       | Platform-spec detailing + product discovery.                                 |
| Duplicate record review                  | Handle two records about same thing/action.                           | Strong conflict boundary.                                | Reviewers can decide whether both are valid.                        | Thin evidence.                            | Platform-spec detail gap.                                 | Full conflict console too early.                | Include only “needs review” surface.                   | What duplicate records are common in real field work?                                            | Platform-spec detailing + product discovery.                                 |
| Conflict review ergonomics               | Let resolver compare and decide.                                      | Strong conflict boundary; UX open.                       | Single reviewer can understand context.                             | Thin evidence.                            | Platform-spec detail gap.                                 | Building advanced flag queue too early.         | Defer full ergonomics.                                 | What information does a resolver need to decide?                                                 | Platform-spec detailing.                                                     |
| Reporting freshness                      | Prevent false certainty in dashboards.                                | Compatible with projections.                             | Supervisors can act on freshness labels.                            | Strong need, product wording thin.        | Spec with validation caveat.                              | Live-dashboard illusion.                        | Include minimal freshness marker.                      | What freshness threshold changes decisions?                                                      | Platform-spec detailing + product discovery.                                 |
| Scoped reports                           | Show only allowed data.                                               | Compatible if inherited from access.                     | Reports can initially inherit detail access.                        | Plausible.                                | Spec with validation caveat.                              | Accidentally deciding aggregate access.         | Include only access-inherited reports.                 | Do managers need summaries beyond details they can inspect?                                      | Platform-spec detailing; architecture decision if divergence needed.         |
| Auditor/query access                     | Inspect across normal hierarchy.                                      | Not settled if beyond assignment-derived access.         | Auditors may need cross-cutting access.                             | Architecture-routed.                      | Architecture decision required.                           | Bypassing access/sync equivalence.              | Exclude from Candidate 1.                              | Can audit access be modeled as time-bounded scoped assignment?                                   | Formal architecture decision if not.                                         |
| Aggregate access beyond detail access    | See summaries without detail records.                                 | Not settled.                                             | Managers may need broad summaries.                                  | Architecture-routed.                      | Architecture decision required if divergent.              | Leaking restricted data through aggregates.     | Exclude from Candidate 1 unless inherited access only. | Must aggregates always inherit event/detail access?                                              | Formal architecture decision or platform-spec detailing.                     |
| Actor-as-subject delivery                | User sees records about themselves.                                   | Not settled.                                             | Some domains may require it.                                        | Architecture-routed.                      | Architecture decision required.                           | Actor identity becomes authority.               | Exclude from Candidate 1.                              | Does being the subject create access independent of assignment?                                  | Formal architecture decision.                                                |
| Device loss before sync                  | Handle lost unsynced work.                                            | Not architecture-guaranteed.                             | Organizations need mitigation.                                      | Tooling/policy evidence thin.             | Implementation/tooling gap + operational policy needed.   | Claiming impossible durability guarantee.       | Defer; name as risk.                                   | How often are devices lost before sync?                                                          | Implementation/tooling design + operational policy definition.               |
| Config authoring syntax                  | Let coordinator define setup.                                         | Implementation/tooling under bounded config.             | A tool can be designed later.                                       | Thin evidence.                            | Implementation/tooling gap.                               | Premature DSL or form-builder surface.          | Exclude detailed syntax.                               | Do coordinators start from spreadsheet, template, previous activity, or blank setup?             | Implementation/tooling design after product discovery.                       |
| Projection/materialized view performance | Keep derived views usable.                                            | Escape-hatch-routed if performance trigger measured.     | Performance may become issue later.                                 | No trigger evidence.                      | Escape-hatch trigger required.                            | Implementing read views as truth.               | Do not include as product-fit requirement.             | Has the measured p95 projection trigger fired?                                                   | Escape-hatch routing only.                                                   |
| Authority snapshot performance           | Speed authority reconstruction.                                       | Escape-hatch-routed if metric trigger fires.             | Performance may become issue later.                                 | No trigger evidence.                      | Escape-hatch trigger required.                            | Adding authority context for convenience.       | Do not include.                                        | Has authority reconstruction exceeded the measured threshold after lower-impact optimization?    | Escape-hatch routing; formal decision if envelope impact.                    |
| Expanded expression functions            | More config expressiveness.                                           | Escape-hatch-routed if repeated deployment need appears. | May be requested later.                                             | No trigger evidence.                      | Escape-hatch trigger required.                            | Turning config into code.                       | Exclude.                                               | Has the same bounded logical need appeared across deployments?                                   | Escape-hatch routing; successor platform decision.                           |
| Expanded pattern inventory               | Add new platform-fixed workflow skeletons.                            | Escape-hatch-routed if repeated deployment need appears. | Future workflows may require it.                                    | No trigger evidence.                      | Escape-hatch trigger required.                            | Deployer-authored state machines.               | Exclude.                                               | Do repeated deployments request the same unsupported pattern?                                    | Escape-hatch routing; successor decision or pattern plan.                    |

## 6. Candidate 1 readiness summary

### 6.1 Include in Candidate 1

Candidate 1 may include these as platform-spec behavior:

* basic standalone capture;
* record shape and shape version preservation;
* immediate local save;
* basic pending/synced status;
* basic offline event creation and later sync;
* minimal assignment-scoped access;
* optional subject-linked capture;
* conservative missing-subject candidate path;
* basic correction concept;
* basic freshness marker;
* surfaced unresolved issue state.

### 6.2 Include minimally with validation caveat

Candidate 1 may include conservative language, but must mark validation needs for:

* sync status wording;
* subject lookup wording;
* subject confirmation cues;
* missing subject while offline;
* wrong subject link correction;
* stale authority review;
* in-progress old-shape work;
* basic cross-version reporting distinction;
* role-action table baseline;
* reporting freshness language.

### 6.3 Defer from Candidate 1

Candidate 1 should explicitly defer:

* full registry stewardship;
* full duplicate subject review UX;
* merge/split UX;
* advanced conflict review queue;
* temporary access lifecycle;
* detailed role-action matrix;
* access-change communication design;
* shape authoring and diff tooling;
* full cross-version reporting semantics;
* local retention after access loss;
* device-loss recovery design;
* config authoring syntax;
* advanced reporting and dashboard UI.

### 6.4 Exclude unless formal architecture decision starts

Candidate 1 must exclude:

* auditor/query access beyond assignment-derived access;
* aggregate visibility beyond underlying detail/event access;
* actor-as-subject delivery as independent access rule;
* field-level sensitivity;
* deployer-authored event types;
* deployer-authored access logic;
* deployer-authored state machines;
* device-side triggers;
* new event envelope fields;
* stored current workflow state;
* automatic resolution of manual-only flags.

### 6.5 Do not activate from escape hatches

Candidate 1 must not activate escape-hatch paths unless trigger evidence exists.

Do not use Candidate 1 to introduce:

* canonical materialized views;
* envelope authority context;
* actor-scoped ordering metadata;
* historical event rewrites for shape cleanup;
* new expression-function vocabulary;
* new pattern inventory;
* service extraction.

Escape hatches are routing context only.

## 7. Research checklist

### 7.1 Vocabulary

* What do field users call a record?
* What do coordinators call the setup artifact: form, checklist, activity, register, campaign, workflow, report?
* What do users call a known thing: site, facility, household, case, person, pump, asset, location, client, group?
* What do users call a correction: edit, fix, amend, replace, cancel, update, resubmit?
* What wording communicates “needs review” without implying failure?
* What wording communicates “old version” without exposing `shape_ref`?

### 7.2 Setup flow

* Who sets up a new activity today?
* What do they start from: paper form, spreadsheet, prior campaign, donor template, government form, or local SOP?
* Who approves setup before field use?
* How often does setup change after launch?
* What parts of setup are stable across activities?
* What setup work currently requires a technical person?
* What setup mistakes are common?

### 7.3 Field capture

* What is the smallest useful capture flow?
* How many fields are typical in early deployments?
* What validation do field users expect immediately?
* Which validation errors should block saving?
* Which issues should save and be reviewed later?
* How do users handle partial information?
* What makes a capture flow feel too heavy?

### 7.4 Subject lookup and linked capture

* How does a worker identify the right subject today?
* Are subjects selected by name, code, location, QR/barcode, map, list, or local knowledge?
* What minimum details prevent wrong selection?
* What happens when two subjects look similar?
* What happens when the subject is missing?
* Who may create a candidate subject?
* Does the worker need subject history in the field?

### 7.5 Registry maintenance

* Where does the initial registry come from?
* Who is allowed to add entries?
* Who is allowed to update entries?
* Which changes require review?
* What lifecycle terms do users already use?
* How are inactive, closed, retired, moved, replaced, merged, or split things handled today?
* How are duplicates discovered today?
* Who resolves duplicates?

### 7.6 Corrections

* Who may correct a record?
* Can the original actor correct their own record?
* Can a supervisor correct another person’s record?
* Is a correction reason required?
* Can corrections happen offline?
* What should the field worker see after a correction?
* What should a supervisor or auditor see?

### 7.7 Duplicates and conflicts

* Which duplicates are common?
* Which duplicates are harmless?
* Which duplicates block downstream action?
* Which conflicts require human judgment?
* Who is the correct resolver?
* What information does the resolver need?
* What should be hidden from field workers to avoid burdening them?

### 7.8 Offline confidence

* How long are users commonly offline?
* What must work offline beyond capture?
* What local status words are understood?
* Do users distinguish saved locally, uploaded, submitted, received, reviewed, and approved?
* What should happen when sync fails?
* How do workers recover from failed sync today?
* What support path exists for lost devices?

### 7.9 Access and responsibility

* What is the simplest first responsibility model?
* Is access mostly geographic, subject-list based, activity based, team based, or mixed?
* What are the first roles needed?
* Which actions differ by role?
* Who grants access?
* Who revokes access?
* How often do responsibilities change?
* How do users learn that responsibility changed?
* How is temporary coverage handled today?

### 7.10 Shape change and versioning

* How often do forms or registry fields change?
* Who requests changes?
* Who approves changes?
* Are old forms still accepted after a change?
* How do field teams learn about changes?
* What does “missing” mean when a field did not exist yet?
* Which field changes affect reporting comparability?
* Are breaking changes common or exceptional?

### 7.11 Freshness and reporting

* Do supervisors understand that central views are latest synced state, not live field reality?
* What freshness indicator is useful?
* What freshness delay is acceptable?
* Which decisions are unsafe on stale data?
* Should reports show pending/unresolved issue counts?
* How do reports distinguish not collected, missing, stale, duplicate, corrected, and needs review?

### 7.12 Sensitivity

* Which data categories are standard, elevated, or restricted?
* Which subject categories are sensitive?
* Which activities are sensitive?
* Which users may see sensitive subjects or records?
* Are sensitivity rules stable enough for shape/activity-level classification?
* Is there evidence that field-level sensitivity is required?
* What compliance obligations are deployment-specific rather than platform-general?

## 8. Candidate 1 gates derived from this matrix

Candidate 1 may proceed if it states:

1. Which product surfaces are included now.
2. Which surfaces are included only minimally.
3. Which surfaces are deferred.
4. Which surfaces require product validation before detailed specification.
5. Which surfaces require operational policy.
6. Which surfaces require implementation/tooling design.
7. Which surfaces require formal architecture decision.
8. Which escape hatches are explicitly inactive.
9. How S00 simplicity is preserved.
10. How user language is separated from architecture vocabulary.

Candidate 1 should not proceed if it:

* treats all product surfaces as already validated;
* hides validation caveats;
* folds full registry lifecycle into basic capture;
* treats access as optional;
* treats offline as only a sync transport problem;
* introduces config builder complexity before evidence;
* uses architecture terms as primary user terms;
* activates escape hatches without trigger evidence;
* crosses negative boundaries.

## 9. Initial gap candidates to add or cross-reference

The following are not automatically new architecture gaps. They are product-fit or platform-spec gates that should be added to, or cross-referenced from, `known-gaps-register.md` only if no existing gap already covers them.

| Short name                               | Classification               | Baseline-extension category | Likely closure path                     | Notes                                                                   |
| ---------------------------------------- | ---------------------------- | --------------------------- | --------------------------------------- | ----------------------------------------------------------------------- |
| Foundational user vocabulary fit         | Product/problem evidence gap | Not applicable              | Product discovery / scenario thickening | Covers record/form/subject/access/sync language.                        |
| Basic setup flow validation              | Product/problem evidence gap | Not applicable              | Product discovery / scenario thickening | Prevents overbuilding setup/config authoring.                           |
| Offline confidence language              | Product/problem evidence gap | Not applicable              | Product discovery / scenario thickening | Covers saved/pending/synced/failed/needs-review wording.                |
| Subject lookup validation                | Product/problem evidence gap | Not applicable              | Product discovery / scenario thickening | Covers how workers identify the right subject.                          |
| Correction language and authority        | Operational policy gap       | Not applicable              | Operational policy definition           | Platform spec can support correction, but organization decides who/why. |
| Duplicate review evidence                | Product/problem evidence gap | Not applicable              | Product discovery / scenario thickening | Needed before detailed duplicate review UX.                             |
| Candidate 1 minimal role-action baseline | Platform-spec detail gap     | Not applicable              | Platform-spec detailing                 | May cross-reference existing role-action table gap.                     |
| Reporting freshness baseline             | Platform-spec detail gap     | Not applicable              | Platform-spec detailing                 | May cross-reference reporting freshness semantics.                      |
| Shape-change user warning language       | Product/problem evidence gap | Not applicable              | Product discovery / scenario thickening | Needed before authoring UX.                                             |
| Device-loss operating model              | Operational policy gap       | Not applicable              | Operational policy definition           | Implementation support may also be needed.                              |

## 10. Output decision

**Status:** Ready to feed Candidate 1 drafting.

**Decision:**
Candidate 1 can begin, but it must be drafted as a constrained platform-spec baseline with visible product-fit caveats. It must not pretend the high-risk user-facing surfaces have been validated.

**Candidate 1 title remains:**

```txt
Candidate 1 — Basic Operational Capture Setup Spec
```

**But Candidate 1 must include a readiness preface:**

```txt
This specification includes only the minimal product surfaces needed for early usable deployment.
Detailed user-facing behavior remains gated by the Product-Fit Readiness and Validation Matrix.
Where product evidence is thin, the spec uses conservative baseline behavior and records validation questions instead of expanding configuration power.
```
