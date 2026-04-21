> **⚠️ SUPERSEDED** — This is a raw exploration document archived for reference. Decisions were finalized in **ADR-004**. For a navigated reading guide, see [guide-adr-004.md](../guide-adr-004.md).
# ADR-004 Session 1: Decision Surface, Prior Art, and Anti-Patterns

> Phase: 1 of 4 — Scoping & Prior Art
> Date: 2026-04-12
> Input: ADR-001 (storage), ADR-002 (identity), ADR-003 (authorization), constraints.md, viability-assessment.md, architecture-landscape.md
> Purpose: Define the full decision surface for ADR-4, learn from how comparable platforms drew the configuration boundary, and name the failure modes that will guide Sessions 2–4

---

## 1. What ADR-4 Must Decide

ADR-4 answers one question: **Where does the platform end and the deployment begin?**

Vision commitment V2 promises "set up, not built" — that new operational activities are configured by deploying organizations, not developed by platform engineers. Tension T2 (the highest-severity open tension in the project) asks: where does "configuration" become "programming"?

This is not a single binary decision. It is a boundary that runs through at least twelve sub-questions, accumulated from ADR-1, ADR-2, and ADR-3 deferrals.

### 1.1 The Full Decision Surface

Every item below was explicitly deferred from an upstream ADR. The "Nature" column distinguishes **boundary questions** (judgment about what deployers vs. developers own) from **technical questions** (mechanism choice that may be irreversible).

| # | Question | Source | Nature | Envelope? |
|---|----------|--------|--------|-----------|
| Q1 | **Event type vocabulary ownership** — Are event types platform-fixed (the platform defines "capture," "review," "transfer"), or deployment-configurable (deployers invent new event types)? | ADR-1 S2 deferral | Boundary | Yes — event `type` field is in every stored event |
| Q2 | **Data shape definition** — How are the payload schemas for each event type defined? What format? What expressiveness? | ADR-1 S5 deferral | Technical + Boundary | Indirectly — shapes determine what payloads look like |
| Q3 | **Schema versioning scheme** — How is the version of the payload schema recorded? Options: (A) version tag in envelope, (B) self-describing payload, (C) schema registry reference | ADR-1 envelope gap | Technical | **Yes — stored in every event forever** |
| Q4 | **Configuration versioning & on-device coexistence** — When a deployer changes a shape or adds an activity, how does the new configuration reach devices? What happens to work-in-progress under the old configuration? | Offline constraint | Technical | Partially — events may reference config version |
| Q5 | **T2 boundary line** — Where does "configuration" stop and "platform evolution" (code change) begin? What is the expressiveness ceiling for the configuration layer? | Core tension T2 | Boundary | No |
| Q6 | **S12 scoping (event-triggered actions)** — What subset of condition-based triggers can be expressed in configuration? What requires platform code? How do we prevent the trigger system from becoming a Turing-complete rules engine? | Viability Condition 2 | Boundary + Risk | No |
| Q7 | **Domain-specific conflict rule configuration** — Can deployers define custom rules for which event patterns constitute a conflict and how conflicts should be resolved? Or is the conflict model platform-fixed? | ADR-2 deferral | Boundary | No |
| Q8 | **Role-action permission tables** — How are role→action permission mappings defined and delivered to devices? | ADR-3 S1 deferral | Technical + Boundary | No |
| Q9 | **Per-flag-type severity configuration** — How does a deployer specify which flag types are blocking vs. informational (ADR-3 S7)? | ADR-3 S7 deferral | Boundary | No |
| Q10 | **Scope type extensibility** — Are scope types (geographic, subject-based, query-based) platform-fixed or deployment-configurable? | ADR-3 deferral | Boundary | No |
| Q11 | **Activity model & correlation metadata** — How are "activities" (a campaign, a routine program, a case management workflow) defined and how do events correlate to them? | ADR-1 exploration | Technical + Boundary | Possibly — events may carry activity reference |
| Q12 | **Sensitive-subject classification** — Can deployers mark subjects or data categories as sensitive, triggering stricter sync/retention behaviors? | ADR-3 deferral | Boundary | No |

### 1.2 Which Questions Touch the Envelope?

The irreversibility filter (proven in ADR-3) determines proportional exploration depth. Three questions potentially touch stored events:

- **Q1** (event type vocabulary): The `type` field is in every event (ADR-1 S5). Whether that field is drawn from a platform-fixed vocabulary or a deployment-defined one determines what values appear in stored events forever.
- **Q3** (schema versioning scheme): Whichever scheme is chosen, the version reference is stored in events. The format of that reference is irreversible.
- **Q11** (activity reference): If events carry an activity or correlation reference, that field is in the envelope.

Everything else (Q2, Q4–Q10, Q12) is configuration delivered to devices, server-side logic, or deployment policy — all changeable without data migration.

**Implication for Sessions 2–3**: Q1, Q3, and Q11 need the most rigorous exploration. Q5 and Q6 need the most careful judgment. The rest are important but evolvable.

---

## 2. Prior Art: Where Comparable Platforms Drew the Line

Six platforms operate in overlapping problem space. Four come from global-health field operations (DHIS2, CommCare, ODK, OpenSRP). Two come from enterprise operations (Salesforce, Odoo). Each drew a configuration boundary. Each teaches us something about where that boundary should — and should not — be placed.

### 2.1 DHIS2 — The Metadata-Driven Generalist

**Boundary location**: Almost everything is configuration. Data elements, programs, organization units, indicators, validation rules, program rules, user roles, sharing settings — all metadata objects. The Android app downloads metadata and interprets it at runtime. App code is identical across deployments.

**Configuration layers**:

| Layer | What | Expressiveness | Who |
|-------|------|---------------|-----|
| 0: Metadata objects | Data elements, org units, programs, categories | Declarative — name things, set types, assign relationships | Program manager with training |
| 1: Program Rules | Conditional form logic: hide/show fields, assign values, show warnings, create events | Expression-based — boolean conditions + 20+ functions (date math, z-scores, etc.) | Configuration specialist |
| 2: Indicators & Validation | Cross-element calculations, validation rules across data sets | Formula-based — arithmetic on data elements with period/org aggregation | Analytics specialist |
| 3: Custom applications | Anything beyond the above — custom workflows, external integrations, advanced analytics | Full code — JavaScript/React web apps via DHIS2 App Platform | Developer |

**Where the line is drawn**: Everything a deployer can express through metadata objects and program rules is "configuration." Everything else requires a custom DHIS2 app (code). The critical transition is **Layer 1 → Layer 3** — there is no intermediate step.

**What broke**:

1. **Configuration expertise became its own specialization.** The promise of "configured not built" was kept at the metadata level, but setting up a DHIS2 instance correctly requires understanding 50+ metadata object types, their relationships, and their interactions. The expertise shifted from "Java developer" to "DHIS2 configuration specialist" — a different skill, not a simpler one.

2. **Program Rules hit a ceiling at ~70% of workflows.** Rules are program-scoped — they cannot span two programs. A deployer who needs "when a patient is diagnosed in Program A, auto-enroll in Program B" must build a custom application. Deployers discover this limit *after* investing heavily in rule chains within Program Rules.

3. **The generic data model is flat.** The (DataElement, Period, OrgUnit, Value) tuple handles aggregate data well. Longitudinal case tracking required a fundamentally different model (Tracker) bolted on top. These two models coexist but don't compose — they have separate sync, separate conflict handling, and separate analytics. The configuration boundary for Tracker is different from the boundary for aggregate data, creating two parallel systems wearing one label.

4. **Schema evolution is implicit.** Metadata versioning tracks structure changes (hash-based version IDs, ATOMIC vs. BEST_EFFORT import). But captured data doesn't know which schema version it was captured under. Removing a data element from a program orphans all historical values — no migration path, no deprecation period. Devices sync full metadata packages; no incremental updates.

5. **No complexity budget.** Large programs (50+ data elements, 10+ rules) become unmaintainable. No tooling warns deployers they're exceeding practical limits. No dependency visualization, no refactoring support, no "you're approaching the boundary" signals.

**Lesson for Datarun**: The metadata-driven approach works at national scale — DHIS2 proves that. But the boundary must be **explicit, signposted, and graduated**. DHIS2's boundary is discovered by hitting walls. Datarun's boundary should be visible before you reach it. And the data model must compose across use cases (capture, case tracking, distribution) rather than requiring parallel subsystems.

### 2.2 CommCare — The Form-First Case Manager

**Boundary location**: Forms and cases are the core abstractions. Forms capture data through questionnaire flows; cases accumulate data across form submissions. Configuration is layered from visual builder through XLSForm to raw XPath/XML.

**Configuration layers**:

| Layer | What | Expressiveness | Who |
|-------|------|---------------|-----|
| 0: App Builder (Vellum) | Drag-and-drop forms — questions, groups, repeats, basic relevance | Visual — click-to-configure | Program staff |
| 1: XLSForm | Form definitions in spreadsheet format, case property mappings | Tabular — column-based logic | Technical program staff |
| 2: XPath expressions | Conditional logic, case database queries, cross-form calculations | Expression-based — XPath 1.0 with custom functions | Configuration specialist |
| 3: XML suite/profile | Menu structure, case list configuration, form linking, navigation | Structural — XML editing | Advanced configurator |
| 4: Custom code | Java plugins, custom validators, external integrations | Full code | Developer |

**Where the line is drawn**: The transition from "configuration" to "code" happens between Layer 3 (XML) and Layer 4 (Java). But the practical expressiveness ceiling is at **Layer 2** — most deployers can manage XPath until expressions exceed ~3 predicates or query depth exceeds ~3 case hierarchy levels.

**What broke**:

1. **Everything must be a form.** Distribution tracking, multi-level approvals, event-triggered actions — all must be expressed as "fill out a form." This works for data collection but becomes awkward for coordination and workflow. The form metaphor constrains what can be configured, not just how.

2. **Case properties are schemaless.** Properties accept any string value with no type enforcement. A property expected to be an integer can silently become text. After 2–3 years, apps with 50+ properties need external documentation, cleanup scripts, and custom backend validation — the configuration model can't maintain data quality on its own.

3. **Quantifiable complexity ceiling.** The community has identified concrete thresholds where configuration collapses:
   - Case types with 100+ custom properties → sluggish loading, unmaintainable
   - Nested conditionals deeper than 5 levels → invisible bugs
   - Case hierarchies deeper than 4 levels → query performance degrades
   - Fixture files larger than 10MB → slow app startup
   - XPath predicates with 3+ conditions → undebuggable

4. **Form changes are fragile.** Even small changes to form structure can require large adjustments to application configuration. Adding a field is safe; renaming a property orphans old data; removing a form leaves ghost submissions. The configuration model doesn't distinguish between additive and breaking changes.

5. **No configuration-as-code workflow.** App Builder is GUI-only. No version control for configuration, no diff capability, no rollback mechanism, no staging environment in the standard workflow. Configuration changes are made live.

**Lesson for Datarun**: CommCare's graduated layers (visual → spreadsheet → expression → XML → code) are the right structure — they let deployers start simple and grow. But the data model needs **schema enforcement from day one** (not schemaless-by-default), **additive vs. breaking change awareness** (configuration changes should be classified), and the configuration system should be **diffable and versionable** like code.

### 2.3 ODK — The Capture Purist

**Boundary location**: Forms are the only abstraction. Each form is self-contained, defined in XLSForm, captured independently. The recent Entities feature adds longitudinal tracking, but with severe constraints.

**Configuration layers**:

| Layer | What | Expressiveness | Who |
|-------|------|---------------|-----|
| 0: XLSForm | Form definition in spreadsheet format | Tabular — question types, skip logic, validation | Anyone with spreadsheet skills |
| 1: XPath expressions | Conditional logic, constraints, calculations | Expression-based — 60+ built-in functions | Trained data manager |
| 2: Entity lists | Shared data between forms (registration → follow-up) | Declarative — property definitions, save_to mappings | Trained data manager |
| 3: Custom development | External apps, API integrations, custom analytics | Full code — no intermediate step | Developer |

**Where the line is drawn**: Between Layer 2 (Entities) and Layer 3 (code). There is **no intermediate step** — you are either in XLSForm+Entities territory or you are writing code. ODK-X was created specifically to fill the gap, replacing XPath with JavaScript and adding two-way sync, but ODK-X is effectively a different platform.

**What broke**:

1. **Entity properties are strings-only and append-only.** Once an Entity property is created, it cannot be removed. Properties have no type enforcement. You cannot have numeric, date, or structured Entity properties. This forces data modeling gymnastics and accumulates schema cruft over time.

2. **No per-user data filtering at sync level.** All devices get all Entities. The only "filtering" is choice_filter in form logic — a UI concern, not a data access concern. For deployments with sensitive per-worker data, this is a showstopper.

3. **No workflow beyond form submission.** There is no state progression, no review step, no approval chain, no assignment model. A form submission is final. Review exists only as a server-side annotation that can gate Entity creation — but there is no configurable workflow.

4. **The ceiling is low and the wall is abrupt.** ODK is exceptionally good for its core use case (go to field, fill form, upload). But the moment you need anything beyond single-form capture — case management, multi-step workflow, inter-form coordination — you hit a hard wall with no escape hatch within the configuration layer.

**Lesson for Datarun**: ODK proves that **simplicity for the simple case is non-negotiable** — P7 is validated by ODK's success. But ODK also proves that **a tight ceiling without escape hatches drives organizations to other platforms entirely**. Datarun must keep the floor low (S00 should be as easy to configure as an ODK form) while raising the ceiling gradually (workflow, triggers, coordination should be configurable without code) and providing a clear path to code when the ceiling is reached.

### 2.4 OpenSRP — The Standards-First Task Engine

**Boundary location**: FHIR resources define everything. The data model, the task structure, the care plan logic — all expressed as FHIR resources synced to devices. Configuration means authoring FHIR resources.

**What broke**: FHIR is a health data standard. Using OpenSRP for non-health domains means either misusing health resources as generic containers (defeating the standards benefit) or building a parallel model (defeating the single-system commitment). "Set up, not built" becomes "set up by a FHIR specialist" — the configuration expertise required is high and domain-locked.

**Lesson for Datarun**: Domain-agnostic data models must not adopt domain-specific standards as internal representations. Interoperability is an export/mapping concern, not a storage concern. Configuration should not require specialist knowledge of an external standard.

### 2.5 Salesforce — The Enterprise Configuration Boundary

**Boundary location**: Declarative metadata defines data model and UI. Flow handles automation without code. Apex (Java-like) handles everything beyond. Governor limits enforce hard walls against runaway complexity. The platform has 20+ years of experience managing the configuration-to-code gradient, making it the most mature case study in existence.

**Configuration layers**:

| Layer | What | Expressiveness | Who |
|-------|------|---------------|-----|
| 0: Metadata & Fields | Custom objects, fields, field-level security, page layouts, record types, validation rules, formula fields | Declarative — name things, set types, set constraints | Administrator |
| 1: Automation without code | Workflow Rules (deprecated), Process Builder (deprecated), Flow (record-triggered, scheduled, screen, autolaunched), custom metadata types | Visual — click-to-configure branching, loops over collections, sub-flows | Advanced administrator |
| 2: Low-code + visual | Lightning Web Components (custom UI), Lightning page builder, Visualforce pages | Component assembly — drag-drop + some markup | Citizen developer |
| 3: Professional code | Apex triggers & classes, batch processing, custom REST/SOAP APIs, async processing | Full code — Java-like with governor limits | Developer |

**Where the line is drawn**: Between Layer 1 (Flow) and Layer 3 (Apex). Salesforce markets "Clicks Not Code" — the promise that Layers 0–1 cover most needs. In practice, the boundary holds for simple orgs (~30% of enterprises) but breaks for mid-market and enterprise orgs (60–95% require Apex). The break point is consistent: **bulk operations, multi-stage async processes, real-time integrations, and recursive logic** all require code.

**What broke**:

1. **Three overlapping automation systems created "org debt."** Salesforce shipped Workflow Rules, then Process Builder, then Flow — each more expressive than the last, but none replaced the prior ones immediately. Orgs ended up with the same object triggering automation in all three systems. Same-field updates from different systems caused conflicts. Testing required understanding three automation languages. Salesforce is now deprecating the first two and consolidating to Flow — but migration is years-long and error-prone. The lesson: **never ship overlapping automation systems**; provide exactly one mechanism per concern, or the consolidation cost exceeds the original development cost.

2. **"Flow Spaghetti" replaced code complexity with visual complexity.** Teams trying to avoid Apex built massive flows — 500+ elements, 10+ nested decision nodes, loops over thousands of records. These flows are harder to debug than equivalent Apex because there is no stack trace, no step debugger (until recently), and no diff/version control in the visual editor. Over-parameterized flows (30+ input variables for "reusability") became incomprehensible to anyone but the original author. The anti-pattern is clear: **visual configuration tools do not eliminate complexity, they just hide it behind a different interface**. When the configuration exceeds a complexity threshold, code is more maintainable.

3. **Governor limits are honest boundaries that shape architecture.** Per-transaction limits (100 SOQL queries, 150 DML statements, 10-second CPU time, 6MB heap) are hard walls enforced at runtime. These limits are not arbitrary — they prevent any single tenant from monopolizing shared resources. But they also **force developers to think architecturally**: bulkify operations, batch large jobs, avoid N+1 patterns. The limits shape the code rather than allowing any pattern. This is the healthiest boundary enforcement in any platform studied.

4. **Schema evolution is unversioned at the data level.** Field additions are safe (existing records get NULL). Field type changes often fail (Number → Text requires data conversion). Field deletions are destructive (data gone after 10-day recycle bin). Picklist value removal orphans existing records. Critically, **Salesforce does not version data by schema** — old records exist under old schema, new records under new schema, and readers must handle both. No automatic migration mechanism exists. This forces deployers to use the rename-migrate-retire pattern rather than direct schema modification.

5. **Trigger cascades create emergent behavior no one can predict.** An insert on Account fires Trigger 1, which updates Account, which fires Trigger 1 again (recursive), which fires Trigger 2, which fires Trigger 2 again — up to the recursion depth limit of 16. Orgs with 100+ automation rules across objects cannot make simple changes without risk of cascade failures. The fix (single handler per object + static recursion flag) is well-known but not enforced by the platform — it is organizational discipline, not architectural constraint.

6. **Configuration drift between environments is the default.** Production orgs get manual changes (admin clicks buttons). Later SFDX deployments overwrite those changes. The problem: **metadata is deployable, but data is not**. Custom Metadata Types exist as a hybrid (config-as-data that is deployable), but most configuration lives as undiffable metadata changes made through the UI.

**Lesson for Datarun**: Salesforce proves three things. First, governor limits (hard walls on resource consumption) are the healthiest form of boundary enforcement — they make the boundary architectural rather than Advisory. Datarun should consider hard limits on configuration complexity (max rules per shape, max trigger depth, max expression nesting). Second, exactly one automation mechanism per concern prevents overlapping-system debt. Third, the "clicks not code" promise breaks at ~60–70% coverage for non-trivial deployments — an honest boundary acknowledges this rather than pretending configuration covers everything. The configuration gradient hypothesis (Layer 0–3) should be designed with the explicit expectation that Layer 3 will be needed, and the escape to code should be clean rather than hidden.

### 2.6 Odoo — The Module-Based Extensibility Model

**Boundary location**: Python ORM models define data and logic. XML data files define views and access rules. Odoo Studio provides visual configuration for simple field additions and layout changes. The fundamental extensibility mechanism is module inheritance — any module can extend any model's fields, views, and behavior without modifying the original module's code.

**Configuration layers**:

| Layer | What | Expressiveness | Who |
|-------|------|---------------|-----|
| 0: Admin UI | User accounts, group permissions, company records, settings | Data entry — no schema definition | Administrator |
| 1: Studio (visual) | Custom fields, simple view modifications, app structure, field visibility | Visual — limited to field types and layout | Power user |
| 2: XML data files | Views (with XPath-based inheritance), access control lists, record rules, static data | Declarative — structural definition + domain expressions | Configuration specialist |
| 3: Python code | Model definitions, computed fields, server actions, automation logic, business rules, integrations | Full code — Python + ORM API | Developer |

**Where the line is drawn**: Between Layer 1 (Studio) and Layer 3 (Python). Layer 2 (XML) sits in between as a technically declarative but practically specialist-only tier. Studio hits its ceiling at business logic — it can add fields and rearrange layouts but cannot express conditional logic, computed fields, or workflow rules. The critical transition is **Studio → Python**, with XML as an intermediate that requires developer-adjacent skills.

**What broke**:

1. **Mixing Studio with code modules is the primary failure mode.** Studio-created custom fields exist as database records. Code module fields exist as Python class attributes. When both add fields to the same model, naming conflicts are silent (last-loaded module wins), upgrade paths become non-deterministic, and deactivating Studio fields can lose data. The lesson is identical to Salesforce's overlapping-automation problem: **two configuration authorities over the same domain create unpredictable interactions**.

2. **In-place model inheritance is powerful but creates invisible coupling.** Odoo's `_inherit` mechanism lets any module add fields to any model — `res.partner` (the core contact model) is extended by 30+ modules in a standard installation, each adding their own fields. This is architectural genius for extensibility but creates a hidden dependency graph. Breaking changes in module B's extension of model A cascade to module C's extension of model B. The pattern works when inheritance is **wide and shallow** (many small extensions) but degrades when it becomes **tall and deep** (chains of extensions extending extensions).

3. **Server actions blur the configuration/code boundary dangerously.** Odoo's `ir.actions.server` with `state='code'` embeds Python code as a text string in the database. This code is unversioned, untestable, invisible to static analysis, and bypasses the module upgrade pipeline. It exists because the declarative configuration layer cannot express the logic deployers need — but it creates a shadow codebase that accumulates debt. The lesson: **escape hatches to code must be explicit modules, not inline code strings**.

4. **Multi-company isolation is architecturally clean but enforced server-side only.** Every model supports a `company_id` field, and record rules enforce `[('company_id', 'in', company_ids)]`. This is the most elegant multi-tenant pattern in the studied platforms. But it is enforced entirely on the server — offline clients would need to replicate the domain expression evaluation engine locally. The security model assumes connectivity.

5. **No offline capability is an architectural choice, not a missing feature.** Odoo Mobile supports offline data capture but requires connectivity for server actions, record rule evaluation, related field access, scheduled actions, and search operations. This is not a gap — it reflects an architectural assumption (always-connected) that is incompatible with field operations on low-end devices with intermittent connectivity.

6. **Schema evolution depends on the upgrade pipeline.** Adding fields is safe (ORM creates columns automatically). Removing fields requires migration scripts. Type changes need explicit `pre_init_hook`/`post_init_hook` operations. Major version upgrades (e.g., 16 → 17) frequently break customizations because the migration path assumes controlled, sequential upgrades — not the fragmented version landscape of offline devices operating independently.

**Lesson for Datarun**: Odoo's most transferable pattern is the module inheritance model — extending behavior without modifying original code, keeping extensions wide and shallow. This maps directly to Datarun's need for per-deployment configuration that doesn't fork the platform. Odoo's three-layer security model (ACL → record rules → field-level visibility) is the right architecture for access control, but the enforcement must be local not server-side. The clearest warning is about **mixing configuration authorities** — if Datarun provides both visual tooling (Layer 0) and declarative configuration (Layer 1–2), they must operate through the same mechanism, not as parallel systems that can conflict.

### 2.7 Cross-Platform Synthesis

| Dimension | DHIS2 | CommCare | ODK | OpenSRP | Salesforce | Odoo | Datarun should... |
|-----------|-------|----------|-----|---------|------------|------|-------------------|
| **Boundary visibility** | Discovered by hitting walls | Partially graduated | Abrupt wall | Requires specialist | Graduated but breaks at ~60% | Studio ceiling, then code jump | Make boundary **explicit and signposted** |
| **Layers** | 3 (metadata → rules → code) | 5 (visual → spreadsheet → XPath → XML → code) | 3 (XLSForm → Entities → code) | 1 (FHIR resources or code) | 4 (metadata → Flow → LWC → Apex) | 4 (admin → Studio → XML → Python) | Have **3–4 layers** with clear transitions |
| **Schema enforcement** | Typed data elements | Schemaless properties | String-only entities | FHIR types | Typed metadata | ORM-typed fields | Enforce **typed schemas** from day one |
| **Schema evolution** | Implicit (orphaned data) | Fragile (breaking changes) | Append-only (no removal) | FHIR versioning | Unversioned data (rename-migrate-retire) | Upgrade pipeline (migration scripts) | Support **additive changes easily, breaking changes explicitly** |
| **Logic expressiveness** | Program Rules (~70%) | XPath (~80%) | XPath (~60%) | CQL/PlanDef | Flow (~60–70% of enterprise needs) | Server actions + computed fields (~70%) | Define **expression ceiling** clearly |
| **Cross-activity coordination** | No (program-scoped rules) | Limited (case sharing) | No | Limited (care plans) | Yes (cross-object Flow) | Yes (cross-model server actions) | Support **cross-activity references** natively |
| **Versioning** | Hash-based metadata | GUI-only (no version control) | Form versioning | FHIR versions | SFDX + Git (metadata only) | Module versioning + upgrade pipeline | Make configuration **diffable and versionable** |
| **Error at boundary** | Silent failure | Performance degradation | Hard stop | Integration failure | Governor limit exception (hard wall) | Server error or silent override | **Warn before boundary**, fail safely at boundary |
| **Automation overlap** | Single system (Program Rules) | Single system (XPath + case) | None (no automation) | None | Three systems → consolidation debt | Studio + code → conflict | **Exactly one mechanism** per concern |
| **Offline support** | Full offline (Android app) | Full offline (mobile app) | Full offline (Collect) | Partial offline (FHIR sync) | None (cloud-only) | Minimal (capture-only) | **Offline-first** with local rule evaluation |

---

## 3. Anti-Pattern Catalog

Five named failure modes that must guide every ADR-4 sub-decision, plus a sixth from the enterprise platform analysis. If a proposed configuration mechanism triggers any of these, it must be redesigned or explicitly bounded.

### AP-1: The Inner Platform Effect

**Definition**: The configuration layer becomes so expressive that it is effectively a general-purpose programming language, but one with worse tooling, no type system, no debugger, and no community compared to actual programming languages.

**How it manifests**: Configuration DSLs that gain variables, conditionals, loops, function definitions, and recursion. Each feature is added to solve a specific deployer request. The cumulative result is a language that is harder to use than Python but solves fewer problems.

**Precedent**: DHIS2's Program Rules approach the edge of this. CommCare's XPath expressions have crossed it for some deployments. Salesforce Flow is the canonical enterprise example — teams build 500+ element flows that are harder to debug than equivalent Apex code. Odoo's server actions with `state='code'` embed Python in database records, creating a shadow codebase that bypasses all code management practices.

**Test**: If a deployer's configuration requires more than 60 seconds to explain verbally, it has crossed the boundary. If configuration requires a debugger, it is programming. If configuration can express infinite loops, it is Turing-complete and the boundary has collapsed.

**Guard for ADR-4**: Define the expressiveness ceiling *before* designing the expression language. The ceiling is: **pure functions over known data, no loops, no side effects, no user-defined abstractions.**

### AP-2: Greenspun's Tenth Rule (Informal)

**Definition**: "Any sufficiently complicated configuration system contains an ad hoc, informally-specified, bug-ridden, slow implementation of half of a programming language."

**How it manifests**: The configuration language starts simple (key-value pairs, simple conditions). Deployer requests add "just one more feature" — date arithmetic, string manipulation, list operations, conditional assignment. Each addition is reasonable. The accumulation is a language that was never designed as one.

**Precedent**: ODK-X was created because ODK's XPath expressions could not express the workflows organizations needed. Instead of extending XPath incrementally, ODK-X replaced it with JavaScript — acknowledging that the right answer was a real language, not a configuration language pretending to be one.

**Test**: Count the number of built-in functions in the expression language. DHIS2 has ~25. CommCare has ~40. ODK has ~60. If Datarun's expression language exceeds 30 functions, question whether you're building a language rather than a configuration layer.

**Guard for ADR-4**: When the expression language's function count exceeds the threshold, that is the signal to provide a code escape hatch rather than adding more functions.

### AP-3: The Configuration Specialist Trap

**Definition**: The configuration model is technically "not programming" but requires such deep expertise that only trained specialists can use it. The promise of "set up, not built" is formally kept but practically broken — the deployment still requires hiring a specialist, just a different kind.

**How it manifests**: DHIS2 requires "DHIS2 configuration specialists" — trained operators who understand 50+ metadata object types and their interactions. CommCare requires "CommCare experts" who can write XPath expressions against case databases. The skill is not transferable across platforms, and the learning curve is 2–4 weeks for complex deployments.

**Precedent**: Every platform in the prior art set has created its own specialist class. The health information systems community has partially accepted this ("DHIS2 implementer" is a recognized role). But it contradicts V2's promise.

**Test**: Can a competent operations manager, given documentation, set up a new activity in less than one day? If not, the configuration model is too complex for V2's promise.

**Guard for ADR-4**: Design for **two tiers of deployer**: (1) an operations manager who assembles activities from pre-built components, and (2) a configuration specialist who creates new components. The operations manager's workflow must be visual and template-based. The specialist's workflow must be versionable and testable. Neither should need to write code.

### AP-4: The Schema Evolution Trap

**Definition**: The configuration model makes it easy to create initial configurations but impossible to evolve them safely. Schema changes break existing data, orphan historical records, or require manual migration.

**How it manifests**: DHIS2 orphans data when data elements are removed from programs. CommCare's schemaless case properties accumulate cruft — typos create permanent properties. ODK's Entity properties are append-only — once created, they can never be removed. In all three platforms, the first deployment works beautifully; the third schema revision creates a maintenance crisis.

**Precedent**: Universal across prior art. Schema evolution in offline-first systems is harder than in online systems because you cannot retroactively fix data on devices that haven't synced.

**Test**: Can a deployer add a field? Remove a field? Rename a field? Change a field's type? For each, what happens to: (a) data already captured under the old schema, (b) devices that haven't synced the new schema, (c) events in transit during the schema change? If any answer is "undefined behavior," the schema evolution model is incomplete.

**Guard for ADR-4**: Design schema evolution as a **first-class operation** with explicit change types (additive, deprecation, breaking), migration rules, and version coexistence guarantees. ADR-1 already provides the foundation: events are immutable and carry their shape version. ADR-4 must define the versioning scheme (Q3) and the coexistence rules (Q4).

### AP-5: The Trigger Escalation Trap

**Definition**: A condition-based trigger system starts with simple rules ("if value > threshold, flag it") and is incrementally extended until it becomes an unbounded workflow engine that can express arbitrary computation, fire cascading side effects, and create emergent behaviors that no one can predict.

**How it manifests**: S12 (event-triggered actions) is the entry point. First trigger: "notify supervisor when critical value recorded." Second: "create follow-up task when condition met." Third: "escalate when task not completed within 48 hours." Fourth: "auto-close case when all tasks complete." Fifth: "re-open case when new observation contradicts closure." By the fifth trigger, you have a reactive workflow engine.

**Precedent**: DHIS2's Program Rules can create events (CREATEEVENT action). CommCare's case management can auto-update properties. Salesforce Flows are the extreme case — a visual workflow engine that is functionally a programming language.

**Test**: Can a trigger fire another trigger? If yes, can the chain be unbounded? If yes, the system can loop. Even without explicit loops, cascading triggers create emergent behavior that is untestable and unpredictable at deployment scale.

**Guard for ADR-4**: Triggers must be **non-recursive** (a trigger cannot fire another trigger), **pure** (trigger evaluation has no side effects beyond creating a single pre-defined event type), and **bounded** (finite evaluation with explicit depth limit). Viability Condition 2 mandates this.

### AP-6: The Overlapping Authority Trap

**Definition**: Multiple configuration or automation mechanisms coexist over the same domain, creating conflicting behaviors, undebuggable interactions, and eventual consolidation debt that exceeds the original development cost.

**How it manifests**: A platform ships one automation system, then adds a more expressive one without deprecating the first. Eventually a third arrives. Each is reasonable in isolation. But when the same object triggers behavior in all three systems, the execution order is opaque, conflicts are undiagnosable, and migration between systems is a multi-year project.

**Precedent**: Salesforce's Workflow Rules + Process Builder + Flow is the canonical example — three automation systems firing on the same DML events, each with different expressiveness, different debugging tools, and different execution ordering. Salesforce spent years consolidating to Flow alone, and enterprises are still migrating. Odoo exhibits a milder version: Studio-created fields and code-module-created fields coexist on the same model, with naming conflicts resolved by load order rather than explicit precedence.

**Test**: Count the number of mechanisms that can modify behavior for a single event type. If >1 mechanism can write automation rules for the same trigger, they overlap. If their interaction is not formally specified (explicit ordering, explicit conflict resolution), overlapping authority will create debt.

**Guard for ADR-4**: For each configuration concern (schema, logic, triggers, access), provide **exactly one mechanism**. If an older mechanism is replaced by a newer one, deprecate and remove the old one — do not leave both active. Visual tooling (Layer 0) and specialist configuration (Layer 1–2) must produce the **same underlying artifacts**, not parallel structures that can conflict.

---

## 4. The Constraint Stack ADR-4 Inherits

ADR-4 does not start from a blank slate. Three upstream ADRs and the vision commitments constrain the configuration model:

### From ADR-1 (what must be configured)

| Constraint | Configuration implication |
|-----------|-------------------------|
| Events are immutable, typed, with minimum envelope (id, type, payload, timestamp) | Configuration must define **event types** and **payload schemas**. The `type` field is free text (Q1). The payload must conform to a **shape** (Q2). |
| Events carry a shape version (acknowledged in prose, not yet formalized) | Configuration must define a **versioning scheme** for shapes (Q3). |
| Projections are derived from events | Configuration may define **how projections are computed**, or this may be platform-fixed. |
| Sync unit is the immutable event | Configuration changes must be **sync-compatible** — new configuration cannot invalidate events already in transit (Q4). |

### From ADR-2 (what can be configured)

| Constraint | Configuration implication |
|-----------|-------------------------|
| Four typed identity categories (subject, actor, assignment, process) | Configuration must define identity types per deployment — but the four **categories** are platform-fixed. |
| Accept-and-flag conflict model | Conflict detection rules are platform-fixed. **Conflict resolution policies** may be deployment-configurable (Q7). |
| Detect-before-act guarantee | Which flag types are blocking vs. informational is deployment-configurable (Q9). The **mechanism** is platform-fixed. |

### From ADR-3 (what needs to be configured)

| Constraint | Configuration implication |
|-----------|-------------------------|
| Assignment-based access control | **Role definitions** and **role→action permission tables** must be configurable (Q8). |
| Sync scope = access scope | **Scope definitions** (geographic, subject-based, query-based) may be deployment-configurable (Q10). |
| Per-flag-type severity (blocking vs. informational) | Must be configurable per deployment (Q9). |
| Sensitive-subject classification | May be deployment-configurable (Q12). |

### From Vision & Constraints

| Commitment | ADR-4 implication |
|-----------|-------------------|
| V2: Set up, not built | The configuration boundary must enable operations managers to configure new activities without developer involvement for Phase 1 scenarios. |
| V5: Grows without breaking | Adding new activities or modifying existing ones must not break existing deployments. Configuration changes must be additive or explicitly versioned. |
| V6: Domain-agnostic | The configuration vocabulary must not contain domain-specific terms (no "patient," "facility," "treatment" baked into the platform). |
| P7: Simplest scenario stays simple | S00 (basic structured capture) must be trivially configurable — comparable to creating an ODK form. |
| Viability Condition 1 | The configuration boundary must be the first major architecture decision after identity/sync foundations. This is that decision. |
| Viability Condition 2 | S12 (event-triggered actions) must be strictly scoped. |

---

## 5. Decision Map: What's Permanent vs. What's Evolvable

Applying the irreversibility filter to the twelve questions:

### Potentially Irreversible (touches stored events)

| Question | What's stored | Why irreversible |
|----------|--------------|-----------------|
| **Q1**: Event type vocabulary | `type` field in every event | If platform-fixed, all events use a finite vocabulary. If deployment-configurable, events contain deployer-defined type strings. Migrating between these two models requires rewriting every event's type field. |
| **Q3**: Schema versioning scheme | Version reference in every event | Whether it's `shape_v: 3`, `_shape: "observation/v3"`, or `schema_ref: "obs-v3"` — the format is in every event forever. |
| **Q11**: Activity reference | Activity/correlation field in events (if added) | If events carry an activity reference, it's a new envelope field (via ADR-1 S5). If not, activity correlation is a projection concern. |

### Evolvable (sync protocol, server logic, or deployment policy)

| Question | Why evolvable |
|----------|--------------|
| **Q2**: Shape definition format | Shapes are configuration objects synced to devices. The format can change without affecting stored events (events carry the version reference, not the shape itself). |
| **Q4**: Configuration versioning | How configuration reaches devices is sync protocol. Can evolve without data migration. |
| **Q5**: T2 boundary line | The boundary is a development discipline decision. It constrains what the platform *offers*, not what is stored. |
| **Q6**: S12 trigger scoping | Trigger rules are configuration. Their expressiveness ceiling is a design choice that can be adjusted without data migration. |
| **Q7**: Conflict rule configuration | Resolution policies are server-side logic. |
| **Q8**: Role-action permission tables | Synced configuration. Format can change without affecting events. |
| **Q9**: Flag-type severity | Deployment policy. |
| **Q10**: Scope type extensibility | Server-side logic. |
| **Q12**: Sensitive-subject classification | Deployment policy. |

**Implication**: Session 3's stress test should focus adversarial depth on Q1, Q3, and Q11. The other questions need careful design but can be revised post-deployment.

---

## 6. The Configuration Gradient (Hypothesis for Session 2)

Based on prior art analysis across six platforms, a four-layer model emerges as the hypothesis for Sessions 2–4 to validate or revise. This revision incorporates three insights from the enterprise prior art that the initial hypothesis lacked.

### 6.0 Two Dimensions, Not One

The prior art reveals that configuration has **two independent dimensions**, not just "how expressive is this layer":

**Dimension A — Expressiveness**: How much logic can this layer express? (None → field logic → cross-field → cross-activity → Turing-complete)

**Dimension B — Vocabulary scope**: Is this layer parameterizing existing platform vocabulary, or extending it with new terms?

| | Parameterize existing | Extend vocabulary |
|---|---|---|
| **No logic** | Layer 0: "Use observation shape v3 for weekly CHV visits" | Layer 0+: "Create a new activity type called 'spot check'" |
| **Field logic** | Layer 2: "Hide BMI field if age < 2" | Layer 1: "Add a 'referral_urgency' field of type select" |
| **Cross-activity** | Layer 3: "When observation flags critical, create follow-up task" | Layer 3+: "Define a new trigger type: escalation" |

The original hypothesis captured Dimension A (expressiveness layers) but missed Dimension B. This matters because Odoo's biggest failure mode (Studio-created fields conflicting with code-module fields) is a Dimension B problem: two mechanisms both extending the vocabulary, producing conflicting artifacts. And Q1 (event type vocabulary ownership) is fundamentally a Dimension B question: can deployments extend the type vocabulary, or only parameterize platform-fixed types?

**Implication for Session 2**: Every time a scenario walk-through hits a configuration element, ask both questions: "What expressiveness layer?" AND "Is this parameterizing or extending?"

### 6.1 The Four Layers

### Layer 0: Assembly (Operations Manager)

**Who**: Coordinator or administrator with domain expertise, basic digital literacy.

**What**: Compose a new operational activity by selecting from pre-built components — which data shapes to use, who is assigned, what rhythm to follow, what review steps exist.

**How**: Template-based. Select a pattern ("periodic household visits with supervisor review"), parameterize it (shape, frequency, assignment scope), deploy.

**Expressiveness ceiling**: No logic, no conditions, no expressions. Pure assembly of named parts.

**Vocabulary scope**: Parameterize only. Layer 0 cannot create new event types, new shapes, or new trigger rules. It selects from what Layers 1–3 have defined.

**Prior art analog**: ODK's "create a new form from a template." DHIS2's "create a new program from a template." Salesforce's "create a custom object + page layout." Odoo Studio's "add a custom field."

**Complexity budget**:
- Max components per activity: 20 (shapes, assignments, review steps, schedules)
- Max parameterization depth: 2 levels (activity → shape → field defaults)
- Target time to configure S00-equivalent: < 30 minutes

### Layer 1: Shape Definition (Configuration Specialist)

**Who**: Technical program staff or configuration specialist.

**What**: Define the payload schema (data shape) for a new event type. What fields exist, what types they are, what validation constraints they have, what display order they follow.

**How**: Declarative schema. Field name, field type (text, number, date, select, geo, etc.), required/optional, validation rule (range, pattern), display hint.

**Expressiveness ceiling**: Field-level validation only. No cross-field logic. No computed fields. No external data references. Pure declaration.

**Vocabulary scope**: Extends vocabulary. Layer 1 creates new shapes that Layer 0 can then assemble. This is the primary extension point for deployers.

**Prior art analog**: XLSForm's survey sheet (without relevance or calculation columns). DHIS2's data element creation. Salesforce custom metadata types. Odoo XML model definitions.

**Complexity budget**:
- Max fields per shape: 60 (DHIS2 practical limit for maintainability is ~50; CommCare degrades past 100)
- Max validation rules per field: 3 (type constraint + range/pattern + required)
- Max shapes per deployment: no hard limit, but dependency graph must be acyclic
- Max select-option values per field: 100

### Layer 2: Logic (Configuration Specialist)

**Who**: Configuration specialist with expression training.

**What**: Add conditional behavior to shapes and activities. Skip logic (show/hide fields based on other fields), computed values (BMI from height and weight), conditional validation, dynamic defaults.

**How**: Pure functional expressions over the current event's payload. No side effects, no external queries, no case database access, no trigger firing.

**Expressiveness ceiling**: Pure functions of the current event's payload fields + entity attributes. No loops, no user-defined functions, no network access, no state mutation. Function count capped (target: ≤30 built-in functions covering math, string, date, comparison, boolean).

**Vocabulary scope**: Parameterize only. Layer 2 adds behavior to shapes defined in Layer 1. It cannot create new shapes, new event types, or new trigger rules. The artifacts it produces (expressions) attach to Layer 1 artifacts (shapes).

**Prior art analog**: DHIS2 Program Rules (but without CREATEEVENT), CommCare XPath Layer 2. Salesforce formula fields + validation rules (but not Flow). Odoo computed fields (but not server actions).

**Complexity budget**:
- Max expression nesting depth: 5 levels (CommCare's practical ceiling before bugs become invisible)
- Max expressions per shape: 30 (one per field is the natural limit; exceeding suggests the shape should split)
- Max expression length: 200 tokens (arbitrary but enforceable; expressions requiring more are likely crossing into programming)
- Max built-in functions: 30 (AP-2 guard — exceeding this signals language creep)
- Max data scope per expression: current event payload + current entity attributes (no cross-subject, no cross-event)

### Layer 3: Policy (Platform Specialist or Code)

**Who**: Platform specialist or developer.

**What**: Define cross-activity triggers (S12), custom conflict resolution rules (Q7), advanced scope types (Q10), schema migration rules.

**How**: Declarative policy definitions with two sub-types (revised after Session 2 walk-throughs):

**Layer 3a — Event-reaction policies**: "When event of type X with shape S meets condition Y, create event of type Z." Synchronous, evaluated at event ingestion. Can run on device or server. This is the familiar trigger model.

**Layer 3b — Deadline-based policies**: "When expected response event doesn't arrive within deadline D after trigger event, create escalation event." Asynchronous, **server-side only** — offline devices cannot reliably evaluate deadlines across all relevant events. Requires scheduled evaluation (at sync time or on a server schedule).

Both sub-types share the same constraints: non-recursive, bounded, produce at most one event per trigger per source event. The key differences are evaluation timing (synchronous vs. scheduled) and evaluation location (device-capable vs. server-only).

**Expressiveness ceiling**: Non-recursive triggers, finite evaluation, bounded depth. Anything beyond → code escape hatch. Payload mapping in trigger output is restricted to **static values and direct field references only** — no expressions, no lookups, no computed values (Session 2 finding: unrestricted payload derivation leads to AP-1).

**Vocabulary scope**: Can extend vocabulary (new trigger types, new scope types) but under strict review. Layer 3 extensions are the most dangerous because they compose with Layer 0–2 artifacts across activities.

**Prior art analog**: DHIS2's CREATEEVENT action (but bounded). CommCare's case auto-update rules (but declared, not buried in XPath). Salesforce Flow (but with Datarun's non-recursive constraint). Odoo automated actions (but scoped and without inline Python).

**Complexity budget**:
- Max triggers per event type: 5 (prevents trigger fan-out)
- Max trigger chain depth: 1 (non-recursive — AP-5 guard; a trigger cannot fire another trigger)
- Max condition complexity per trigger: 3 predicates (keeps trigger conditions human-readable)
- Max side-effect per trigger: 1 event creation of a pre-declared type (no arbitrary side effects)
- Total triggers per deployment: 50 (Salesforce-inspired; forces prioritization)
- **Max escalation depth: 2 levels** (deadline-check watching deadline-check output; Session 2 finding)
- **Deadline granularity: hours** (not minutes, not seconds — offline sync intervals make finer granularity meaningless)

### 6.2 The Explicit Boundary

| Transition | What changes | Signal to deployer |
|-----------|-------------|-------------------|
| Layer 0 → Layer 1 | Defining new shapes requires schema knowledge | "You're creating a new data structure" |
| Layer 1 → Layer 2 | Adding logic requires expression language | "You're writing rules — test them" |
| Layer 2 → Layer 3 | Cross-activity behavior requires policy knowledge | "This affects multiple activities — review carefully" |
| Layer 3 → Code | Turing-complete logic requires a developer | "This needs platform development — file a feature request or write an extension" |

Each transition should be **visible in the tooling**, not discovered by hitting a wall.

### 6.3 AP-6 Compliance: Unified Artifact Pipeline

The Overlapping Authority Trap (AP-6) requires that all layers produce artifacts through the **same pipeline**. Lessons from Salesforce (three automation systems) and Odoo (Studio vs. code modules):

**Rule**: Every configuration artifact — whether authored visually (Layer 0), in a schema editor (Layer 1), in an expression editor (Layer 2), or in a policy file (Layer 3) — must be stored in the **same format**, in the **same registry**, with the **same versioning mechanism**.

**What this means concretely**:
- A shape defined via visual builder and a shape defined via text editor produce the same artifact
- There is no "Studio shape" vs. "code shape" — there is one shape format
- Configuration changes at any layer go through the same validation, versioning, and deployment pipeline
- Conflict detection between layers is possible because all artifacts share a namespace

**What this does NOT mean**:
- All layers must use the same authoring tool (they can have different UIs)
- All layers must be equally easy (Layer 3 can require specialist training)
- All layers must be simultaneously editable (write locks per artifact are acceptable)

**Test for Session 2**: When sketching configuration artifacts for each scenario, verify that a Layer 0 artifact and a Layer 1 artifact for the same concern would be the same file format and could be diffed.

---

## 7. Session 2 Charter

Session 2 must ground this framework in concrete deployment stories. Five scenarios will be walked through the configuration lens:

| Scenario | Why selected | Configuration question it stresses |
|----------|-------------|-------------------------------------|
| **S00** (basic capture) | P7 — must remain trivially simple to configure | Does Layer 0 assembly suffice? How fast from zero to first capture? |
| **S06** (entity registry lifecycle) + **S06b** (schema evolution) | Schema evolution + configuration versioning (Q3, Q4) | What happens when a deployer changes a shape after data exists? |
| **S09** (coordinated campaign) | Activity model + correlation (Q11), multi-assignment | How is a campaign defined as configuration? What's the activity reference? |
| **S12** (event-triggered actions) | Trigger scoping (Q6), AP-5 risk | Can the triggers in this scenario be expressed in Layer 3 without becoming a rules engine? |
| **S20** (CHV field operations) | Role-action permissions (Q8), end-to-end deployer experience | What does the full configuration stack look like for a real composite workflow? |

For each scenario, Session 2 will produce:
1. A concrete configuration artifact sketch (what the deployer would actually author)
2. Which layer each configuration element belongs to
3. Where the configuration hits a wall (if it does)
4. What remains natural vs. what feels forced

---

## 8. Open Questions for Session 2

These are not decisions — they are the questions Session 2's scenario walk-throughs should resolve:

1. **Event type vocabulary**: Should event types be a platform-fixed vocabulary with deployment-configurable payloads (the "generic engine" approach)? Or should deployments define their own event types (the "open vocabulary" approach)? Or a hybrid (platform provides structural types, deployments define domain types)?

2. **Activity as a first-class concept**: Is "activity" (a campaign, a program, a routine workflow) a configuration-level concept that appears in the event envelope? Or is it purely a grouping concern in the configuration layer with no runtime representation?

3. **Shape authoring format**: What does a shape definition actually look like? JSON Schema? A custom format? A spreadsheet (following XLSForm precedent)? A visual builder?

4. **Expression language identity**: Should Datarun adopt an existing expression language (XPath subset, JSONPath, a Lua subset) or define its own? What are the trade-offs for each in terms of offline evaluation, function library, and boundary clarity?

5. **Configuration delivery model**: Is configuration a single atomic package synced to devices (DHIS2 model)? Or is it incrementally delivered (each change is a config event synced like data events)? What happens when a device has events captured under config version N but receives config version N+1 during sync?
