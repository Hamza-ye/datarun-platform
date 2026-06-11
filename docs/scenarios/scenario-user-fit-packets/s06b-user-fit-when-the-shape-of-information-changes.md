# Scenario User-Fit Packet — S06b: When the Shape of Information Changes

## 1. Scenario frame

**Scenario ID:** S06b
**Scenario title:** When the Shape of Information Changes
**Scenario role in platform learning:** Foundational configuration/versioning scenario

**Source scenario summary:**
The organization changes what information it expects to collect. New details become relevant, old details are retired, or the structure used to describe a known thing evolves. Work already captured or still in progress under the old shape must remain understandable, while new work follows the updated shape.

**Why this scenario matters:**
This scenario tests whether “set up, not built” can survive real operational change. A platform that supports only first-time setup is not enough. The organization must be able to adjust what it collects without invalidating prior records, breaking offline work, or forcing custom development.

**What this scenario must not decide:**
This packet does not decide exact config authoring syntax, database migrations, shape registry storage, UI diff tools, API design, report transformation mechanics, or arbitrary data-migration language.

## 2. User and organization fit

### 2.1 Organization profile

**Organization type:** Organization that runs repeated or long-lived operational activities where information needs change over time.
**Operational domain:** Domain-neutral; could apply to inspections, registries, service delivery, logistics, agriculture, humanitarian monitoring, education, infrastructure, or health.
**Scale:** Small program through national deployment.
**Connectivity profile:** Some workers may receive new configuration quickly; others may remain offline and continue using older configuration.
**Operational maturity:** Often starts with paper forms or spreadsheets, then changes forms as reporting, compliance, donor, regulatory, or operational needs evolve.
**Sensitivity pressure:** Shape changes may affect sensitivity when new fields collect more personal or restricted information.
**Current workaround:** Organizations often duplicate forms, add spreadsheet columns, rename fields informally, or tell workers through chat messages that “from now on, use this new version.”

### 2.2 Personas by operational surface

#### Persona A — Coordinator / setup owner

**Operational surface:** Coordination / administration
**Real-world responsibility:** Decide what information should be collected and when the new version should be used.
**Work context:** Usually online, responsible for operational setup and rollout.
**Primary intent:** Change the required information without commissioning a software project or breaking old records.
**What they need to see or do:** Add fields, retire fields, change labels, adjust required/optional status, define when the new shape becomes active, and understand the effect on in-progress work.
**What they need to trust:** The change will not make old records unreadable or cause field failure on devices.
**What they should not need to understand:** Event envelope internals, schema migration code, projection rebuild mechanics, or device config package internals.

#### Persona B — Field worker using old configuration

**Operational surface:** Field execution
**Real-world responsibility:** Continue working with the version currently available on the device.
**Work context:** Offline or intermittently connected.
**Primary intent:** Finish the work they started without being blocked by a change they have not received.
**What they need to see or do:** Keep entering records under the version available locally.
**What they need to trust:** Their work is valid if it followed the rules available at the time.
**What they should not need to understand:** Shape versioning, config package coexistence, or central deployment timing.

#### Persona C — Field worker after update

**Operational surface:** Field execution
**Real-world responsibility:** Use the newer information shape for new work after the device receives it.
**Work context:** Same field environment, but now operating under updated rules.
**Primary intent:** Understand what changed enough to capture correctly.
**What they need to see or do:** Recognize new required fields, retired fields, changed wording, and warnings.
**What they need to trust:** The device shows the right version for the work being started now.

#### Persona D — Supervisor / reviewer

**Operational surface:** Supervision / review
**Real-world responsibility:** Review records created under different versions without misjudging old records by new requirements.
**Work context:** Needs to compare, approve, correct, or report across old and new records.
**Primary intent:** Know which version a record used and whether it was valid under that version.
**What they need to trust:** Missing fields in old records are not necessarily errors.

#### Persona E — Reporting / oversight user

**Operational surface:** Coordination / oversight
**Real-world responsibility:** Interpret trends and summaries across records created under multiple shapes.
**Work context:** Looks at aggregate or longitudinal views.
**Primary intent:** Compare data over time without pretending every period collected the same fields.
**What they need to trust:** Reports show where fields differ by version or where values are not comparable.

## 3. User intent

### 3.1 Jobs to be done

**Coordinator job:**
When requirements change, I need to update what is collected, so that future work captures the right information, without breaking previous records or confusing field teams.

**Offline field worker job:**
When I am still working under the old setup, I need to complete and save my work, so that the organization does not lose valid field data because I had not synced yet.

**Supervisor job:**
When I review records from different versions, I need to know which version each record followed, so that I do not treat valid old records as incomplete new records.

**Reporting user job:**
When I compare records over time, I need to understand which fields existed when, so that summaries are honest about missing, retired, or newly introduced information.

### 3.2 Intent categories

* Adapt setup over time
* Preserve historical meaning
* Continue offline work
* Maintain trustworthy records
* Compare old and new records
* Avoid configuration becoming software development
* Prevent invalid setup from reaching field devices

### 3.3 Success from the user’s point of view

**Coordinator success:**
“I changed what we collect, and the rollout did not break old work or require a developer.”

**Field worker success:**
“I can keep working with what my device has, and my work still counts.”

**Supervisor success:**
“I can tell what was expected at the time the record was made.”

**Reporting success:**
“I can see cross-version data honestly, including fields that did not exist before.”

## 4. Real-world journey

### 4.1 Normal path

1. Organization starts collecting records under an initial information shape.
2. Coordinator identifies a needed change.
3. Coordinator updates the shape.
4. The updated shape is validated before deployment.
5. Devices receive the new configuration on sync.
6. New work uses the new shape.
7. Existing records remain tied to the shape version they were created under.
8. Supervisors and reports can distinguish old-version and new-version records.

### 4.2 Offline / old-version path

1. Field worker goes offline with the old configuration.
2. Coordinator deploys a new shape while the worker is offline.
3. Worker continues capturing records under the old shape.
4. Worker later reconnects.
5. The old-shape records sync successfully.
6. The platform keeps their original shape/version context.
7. New work on that device follows the updated shape after configuration sync.

### 4.3 In-progress work path

1. Worker starts a record or activity under the old shape.
2. A new shape is deployed before the work is completed.
3. The in-progress work remains finishable under the old shape.
4. New work starts under the new shape.
5. The user does not have to manually choose between technical versions unless operationally necessary.

### 4.4 Retired-field path

1. A field was previously collected.
2. The organization decides it is no longer needed.
3. New records stop asking for it.
4. Old records still show it.
5. Reports distinguish “not collected in this version” from “missing by mistake.”

### 4.5 New-required-field path

1. A new required field is added.
2. New records require it.
3. Old records do not retroactively become invalid.
4. Supervisors can see that the field was not part of the old shape.

### 4.6 Bulk-change path

1. Coordinator changes many entries or fields at once.
2. Some devices continue working from older assumptions.
3. Sync later produces a mix of old-shape and new-shape work.
4. Conflicts or stale assumptions are surfaced without discarding records.
5. The organization reviews any affected records or projections.

## 5. Domain-neutral artifacts

| Artifact                     | Purpose                                                                  | Created by                | Used by                                 | Changes over time?      | Trust requirement                            |
| ---------------------------- | ------------------------------------------------------------------------ | ------------------------- | --------------------------------------- | ----------------------- | -------------------------------------------- |
| Shape definition             | Defines expected fields, types, labels, and validation for a record type | Coordinator / setup owner | Field worker, supervisor, reports       | Yes, by version         | Must be versioned and validated              |
| Shape version                | Identifies which version governed a record                               | Platform/config process   | Everyone indirectly                     | Yes, new versions added | Must preserve historical interpretation      |
| Change note                  | Explains why a shape changed                                             | Coordinator               | Supervisor, auditor, future setup owner | Yes                     | Should support accountability and continuity |
| Old-version record           | Record created under prior shape                                         | Field worker              | Supervisor, reports, auditor            | No                      | Must remain valid under its own version      |
| New-version record           | Record created under updated shape                                       | Field worker              | Supervisor, reports, auditor            | No                      | Must follow updated requirements             |
| In-progress work marker      | Indicates work started under an older setup                              | Device/platform behavior  | Field worker, supervisor                | Temporary               | Must not force invalid conversion mid-work   |
| Cross-version report extract | Summarizes old and new records together                                  | Reporting projection      | Coordinator, reviewer                   | Yes                     | Must show version-related non-comparability  |
| Deprecated-field marker      | Shows a field is retained historically but no longer active              | Coordinator/platform spec | Setup owner, reports                    | Yes                     | Must prevent silent historical loss          |
| Validation failure notice    | Prevents invalid shape/config package deployment                         | Platform validation       | Coordinator                             | Temporary               | Must catch setup errors before field rollout |

## 6. Concrete domain exemplar

### Exemplar: Facility registry shape evolves from v1 to v2

**Domain:** Health/service delivery
**Organization:** Regional program maintaining a rural facility registry
**Concrete artifact names:** Facility profile v1, facility profile v2, facility verification form, deprecated field note, versioned facility report
**Concrete user language:** “We used to ask district and GPS. Now we need official facility code and service package.”

### Initial shape — facility_profile/v1

Fields:

* facility name
* district
* GPS location
* facility type
* active/inactive status
* contact person

### Updated shape — facility_profile/v2

Fields:

* facility name
* official facility code
* service package
* facility type
* active/inactive status
* verification date
* contact person

### Concrete journey

1. Coordinator originally deploys `facility_profile/v1`.
2. Field workers record and update facilities under v1.
3. Later, reporting requirements change.
4. Coordinator creates `facility_profile/v2`.
5. New field workers now collect official facility code and service package.
6. A worker who was offline still submits v1 facility updates after v2 is deployed.
7. The old records remain valid as v1 records.
8. Reports show which facilities have v2 verification and which only have older v1 details.
9. Supervisors can target follow-up verification without pretending v1 records were invalid.

### Where this example may mislead platform design

* It may imply health-specific registry logic. The scenario is domain-neutral.
* It may imply that every shape change is a migration. Many should be additive or deprecation-only.
* It may encourage arbitrary data transformation rules. That would cross into a higher-risk architecture area.
* It may imply field workers should see technical version names. Product language may need to hide or simplify versioning.
* It may understate reporting complexity when fields become semantically non-comparable.

## 7. Platform fit under current accepted architecture

| User need                              | Likely settled platform vocabulary                        | Fit assessment                                                           |
| -------------------------------------- | --------------------------------------------------------- | ------------------------------------------------------------------------ |
| Know which rules a record followed     | `shape_ref`, `shape version`                              | Strong fit                                                               |
| Keep old records understandable        | historical schema contract, shape registry                | Strong fit                                                               |
| Add new fields over time               | shape evolution, additive change                          | Strong fit                                                               |
| Retire fields without deleting history | deprecation-only default                                  | Strong fit                                                               |
| Continue offline work under old setup  | atomic config delivery, current + previous config version | Strong fit                                                               |
| Prevent broken setup rollout           | config package validation                                 | Strong fit                                                               |
| Avoid arbitrary scripts                | bounded configuration, L0-L3 gradient, no arbitrary code  | Strong fit                                                               |
| Compare old/new records in reports     | projection/read model, reporting projection               | Good fit, platform-spec details open                                     |
| Bulk update safely                     | accept-and-flag, detect-before-act, config validation     | Good fit, spec/tooling details open                                      |
| Perform complex migration              | breaking change handling                                  | Open; may require formal routing if it changes historical interpretation |

## 8. Fit assessment

### 8.1 Strong fit

S06b fits the architecture’s shape-version model directly. The accepted boundary already says records carry shape references, historical records stay meaningful under their referenced shape, additive change is normal, deprecation is the default removal path, and configuration delivery must avoid partial inconsistent rollout.

### 8.2 Weak fit

The user-facing shape-change lifecycle is not yet specified. Coordinators may not know when to add a field, deprecate a field, create a new shape, or start a new activity. Reports across versions can become misleading if version differences are hidden.

### 8.3 Missing evidence

* How often do target organizations change forms or registry fields?
* Do changes usually add fields, rename fields, remove fields, or restructure meaning?
* Who decides that a change is safe to deploy?
* How do organizations communicate form changes to field teams today?
* Do workers need to finish old work, or can old drafts be abandoned?
* How should reports show “field did not exist yet”?
* What language do coordinators use: form version, template, checklist, tool, register, dataset?
* Are “breaking changes” common, or can most real cases use additive/deprecation patterns?
* Do users need side-by-side old/new comparison?
* Should every shape change require a reason or approval?

### 8.4 Risk of false fit

The architecture may handle versioning correctly while the product still fails users. Coordinators may make unsafe changes because the tooling does not explain the consequences. Field workers may be confused if form wording changes without context. Supervisors may misread old records as incomplete. Reports may silently compare fields that are no longer semantically equivalent.

## 9. Gap routing

### Gap 1 — Shape change lifecycle guidance

**Short name:** Shape change lifecycle guidance
**Classification:** Platform-spec detail gap
**Baseline-extension category:** Not applicable
**Current owner or likely decision path:** Platform-spec detailing
**Baseline item affected:** Shape evolution boundary under `shape_ref` and shape versioning
**Why it is still open:** The architecture defines shape versioning and evolution boundaries, but not coordinator-facing rules for add, deprecate, rename, split, or new-shape decisions.
**Closure path:** Platform-spec detailing
**Evidence needed before closure:** Examples of real form/registry changes from target organizations.

### Gap 2 — Breaking-change decision rule

**Short name:** Breaking shape change rule
**Classification:** Platform-spec detail gap, escalating to architecture decision gap if transformation semantics change historical interpretation
**Baseline-extension category:** Not applicable unless a new platform migration behavior is proposed
**Current owner or likely decision path:** Platform-spec detailing first; formal architecture decision only if new platform migration semantics are required
**Baseline item affected:** Shape evolution boundary; historical interpretation under `shape_ref`
**Why it is still open:** The architecture permits exceptional breaking changes with explicit migration/platform handling, but does not specify the user-facing decision rule.
**Closure path:** Platform-spec detailing
**Evidence needed before closure:** Cases where additive/deprecation-only evolution is insufficient.

### Gap 3 — Cross-version reporting semantics

**Short name:** Cross-version reporting semantics
**Classification:** Platform-spec detail gap
**Baseline-extension category:** Not applicable
**Current owner or likely decision path:** Platform-spec detailing
**Baseline item affected:** Reporting/analytics projections and shape-version interpretation
**Why it is still open:** Reports must distinguish absent, not collected in this version, deprecated, renamed, and truly missing values. Exact semantics are not settled here.
**Closure path:** Platform-spec detailing
**Evidence needed before closure:** Reporting examples where old/new fields are compared.

### Gap 4 — Shape authoring and diff tooling

**Short name:** Shape diff and authoring tooling
**Classification:** Implementation/tooling gap
**Baseline-extension category:** Not applicable
**Current owner or likely decision path:** Implementation/tooling design
**Baseline item affected:** Configuration authoring under bounded shape model
**Why it is still open:** The architecture settles what shape evolution means, not whether authors use UI, YAML, spreadsheet import, JSON, visual builder, or diff review.
**Closure path:** Implementation/tooling design
**Evidence needed before closure:** Coordinator skill level, common artifact formats, and review needs.

### Gap 5 — In-progress old-shape work behavior

**Short name:** In-progress old-shape work completion
**Classification:** Platform-spec detail gap
**Baseline-extension category:** Not applicable
**Current owner or likely decision path:** Platform-spec detailing
**Baseline item affected:** Atomic config delivery and two-version device coexistence
**Why it is still open:** The architecture allows current plus previous configuration for in-progress work, but the exact product behavior for drafts, partial records, and started activities is not specified.
**Closure path:** Platform-spec detailing
**Evidence needed before closure:** Field workflows around drafts, partial capture, and delayed sync.

### Gap 6 — Shape change approval policy

**Short name:** Shape change approval policy
**Classification:** Operational policy gap
**Baseline-extension category:** Not applicable
**Current owner or likely decision path:** Operational policy definition, with platform-spec support
**Baseline item affected:** Configuration governance, not architecture boundary
**Why it is still open:** The platform can validate configuration mechanically, but organizations still need to decide who may approve risky changes.
**Closure path:** Operational policy definition
**Evidence needed before closure:** Organizational governance practice and sensitivity level by shape/activity.

## 10. Output decision

**Current packet status:** Needs SME validation first, then platform-spec detailing and implementation/tooling design.

**Reason:**
The architecture supports S06b strongly, but user-facing shape evolution rules, reporting semantics, approval practice, and authoring/diff tooling need evidence before they should be frozen.

## 11. Acceptance criteria for downstream platform-spec work

A later S06b platform-spec section should be accepted only if it satisfies:

1. A record is always interpretable under the shape version it was created with.
2. Old records do not become invalid because a new shape was deployed.
3. Offline old-shape work can sync after new-shape deployment.
4. In-progress work can complete under the version it started with where the platform permits it.
5. New work uses the newest applied configuration after sync.
6. Additive changes are straightforward.
7. Deprecation preserves historical readability.
8. Breaking changes are exceptional and routed through explicit rules.
9. Reports distinguish missing values from values not collected in that version.
10. Coordinators receive clear warnings before deploying risky changes.
11. Shape change tooling does not require arbitrary code.
12. S00 basic capture remains simple after shape versioning is introduced.
