# 017 - Systematic Catch-Up And Alignment Plan

## Context Capsule

* Artifact: `017-systematic-catchup-and-alignment-plan.md`
* Status: Planning artifact for stewardship-layer stabilization
* Mode: Alignment plan only; no architecture redesign, no catch-up edits applied here.
* Inputs:
  * `009-decision-anchor-extraction-charter.md`
  * `011-core-architecture-decision-records.md`
  * `012-vocabulary-anchor-map.md`
  * `013-gap-routing-playbook.md`
  * `014-architecture-decision-coherence-audit.md`
  * `015-decision-anchor-correction-patch.md`
  * `016-stewardship-layer-assessment.md`
  * `docs/architecture/adrs-decisions-canonical-ledger/`
  * `docs/agent-working-surface/`
  * `docs/decisions/`
  * `contracts/`
* Purpose:
  * Define the smallest validated sequence that turns the decision-anchor package into the stable future stewardship surface.
  * Avoid broad churn by patching upstream anchors before downstream vocabulary/routing.
  * Fold accepted IDR outcomes into the new layer, then freeze the old IDR-first workflow as historical/provenance.
  * Split `013`, merge the useful role of `architecture-rationale-and-routing-companion.md` into the stable router, and retire the companion as an active surface.
* Non-goals:
  * Do not rewrite architecture.
  * Do not close open gaps.
  * Do not create new platform capabilities.
  * Do not update active repo routers until the new layer passes validation.

---

## 1. Stabilization Principle

The new layer should become the single operational stewardship front door, but not by making `.review` prose authoritative.

The stable end state is:

```txt
CDL or successor architecture decisions
  = architecture authority

decision-anchor layer
  = preferred consumable operational layer
  = DEC lookup, vocabulary ownership, routing, negative boundaries, catch-up status

contracts
  = implementation-facing wire/process boundary contracts

active implementation/runtime evidence
  = folded into the decision-anchor layer when durable
  = raw BAR/NW/IDR artifacts retained as provenance/reference
```

During catch-up, current BAR/NW/IDR files remain validation sources.

After catch-up and final audit, they should stop being default agent surfaces unless a task specifically needs historical evidence.

---

## 2. Dependency Order From `009`

The charter's dependency chain is directional. Do not patch downstream files first.

```txt
009 extraction charter
  defines method, domains, templates, quality gates

010 candidate inventory
  upstream candidate source, not final truth

011 core architecture decision records
  stable DEC anchor corpus
  upstream of vocabulary and routing

012 vocabulary anchor map
  term ownership and term collisions
  downstream of 011
  upstream of routing

013 gap routing playbook
  operational routing surface
  downstream of 011 and 012

014 coherence audit
  verifies 009-013

015 correction patch
  correction provenance for 011-013

016 assessment
  adoption verdict and catch-up findings

017 plan
  sequencing plan for validated stabilization
```

Implication:

```txt
First patch 009/011 source-order and DEC anchors.
Then patch 012 vocabulary.
Then split and merge 013.
Then retire or demote old surfaces.
Then update active routers.
```

---

## 3. Target File Roles

### 3.1 Package Entry Point

Add a small package index before changing active repo routers.

Suggested file:

```txt
.review/001-architecture-decisions-listing-passes/000-decision-anchor-layer-index.md
```

Role:

* low-token entry point;
* source-order statement;
* artifact map;
* "what to read for which work" routing;
* warning that CDL/successor decisions remain architecture authority.

This file should be the future single pointer from `AGENTS.md`, `docs/status.md`, and `docs/agent-working-surface/README.md` after validation.

### 3.2 `009` Extraction Charter

Final role:

* method and quality-gate authority for the package;
* not a current decision source.

Required patch:

* update source hierarchy from `002/008`-primary to "CDL/successor authority with `002/008` as recovery verification lineage";
* preserve the artifact dependency order;
* add IDR/BAR/NW catch-up as a later stabilization extension of the original pass chain.

### 3.3 `010` Candidate Inventory

Final role:

* historical candidate inventory.

Required patch:

* likely none beyond package index labeling.

Do not keep maintaining it after freeze.

### 3.4 `011` Core Decision Records

Final role:

* stable DEC anchor corpus consumed by all future routing.

Required patch:

* add CDL anchors to every DEC;
* fold accepted durable IDR outcomes into the relevant DEC records or add bounded "accepted extension" records where the old DEC set lacks a place;
* separate architecture mechanism from current implementation status.

Expected accepted catch-up areas:

* production principal-to-actor binding and group/claim non-authority;
* production principal-binding administration route;
* activity role-action mapping and `assignment_changed` exclusion;
* assignment-admin command capability;
* subject-history backfill as separate authorized repair surface;
* platform payload schemas as runtime contracts;
* config-package schema and pattern-definition delivery;
* shared-device actor-session partitioning;
* auto-resolution architecture accepted but execution deferred;
* resolver reassignment future-decision.

### 3.5 `012` Vocabulary Anchor Map

Final role:

* stable vocabulary ownership and collision map.

Required patch:

* consume patched `011`;
* add accepted current vocabulary from IDR/BAR/NW catch-up;
* remove stale "open" labels where the surface is now accepted;
* preserve open-front and implementation/tooling classification where still true.

### 3.6 `013` Stable Routing Playbook

Final role:

* single future routing surface for architecture-sensitive work.

Required structural change:

```txt
013-gap-routing-playbook.md
  stable routing rules only
  includes:
    source order
    routing workflow
    irreversibility filter
    allowed classifications
    closure paths
    escalation triggers
    gap template
    before-implementation checklist
    do-not-promote reminders
    implementation prompt template

013-known-gap-routing-register.md
  volatile known-gap and open-front examples
  includes:
    current known gaps
    accepted/deferred/future-decision status
    BAR/NW/IDR source anchors
    last-reviewed date
```

Stable `013` should absorb the active roles of:

```txt
docs/agent-working-surface/architecture-rationale-and-routing-companion.md
```

Transferred companion roles:

* authority/use rule;
* decision-routing workflow;
* route definitions;
* routing checklist;
* irreversibility filter;
* common classifications;
* configuration anti-pattern guardrails;
* config lifecycle and dependency/cascade rules where they are general routing rules;
* device/server evaluation contract;
* rationale cards as compressed routing cards;
* architecture test seed backlog;
* escape-hatch/platform-evolution routing table;
* do-not-promote reminders;
* implementation-prompt checklist.

Not transferred as active maintenance:

* source-to-card historical map;
* detailed exploration provenance;
* old table of contents;
* duplicate wording already present in `011`/`012`;
* any rationale that cannot be anchored to CDL/DEC.

### 3.7 `014`, `015`, `016`, `017`

Final role:

* provenance and validation trail.

Required patch:

* no heavy edits.
* future final audit can supersede them as active guidance.

### 3.8 Old Working-Surface Companion

Final role after merge:

```txt
retired reference/provenance
```

Required patch:

* replace active content with a short retirement notice pointing to stable `013`;
* keep a pointer to the last full version through git history or move to a reference/archive path only if repository practice prefers visible archives.

Do not leave both `013` and the companion active.

---

## 4. Future Work Routing After Freeze

The IDR style of work should be folded into the new layer and then frozen as the default future workflow.

Existing IDRs:

* remain provenance;
* are validation inputs for catch-up;
* are not deleted;
* should not remain the first place future agents route new decisions.

Future closure paths should be selected by stable `013`:

| Work kind | Future artifact route |
|---|---|
| Changes architecture authority, structural contracts, negative boundaries, or irreversible platform semantics | formal architecture decision / CDL-successor decision |
| Defines behavior under accepted DEC boundaries | platform-spec detail artifact |
| Selects implementation mechanics under accepted DEC/spec boundaries | implementation design or bounded implementation prompt |
| Defines human process, support, review, retention, governance | operational policy artifact |
| Thickens real-world pressure before deciding/specifying | product/problem evidence artifact |
| Tracks known open/deferred/future-decision fronts | `013-known-gap-routing-register.md` |

Rule:

```txt
Do not create a new IDR by default.
Route through 013 first.
Only use an IDR-like note if 013 explicitly routes the work to that artifact class,
or if the project intentionally keeps "implementation decision record" as a provenance format
for implementation-local decisions.
```

If the project keeps IDR as a provenance format, its role should be:

```txt
implementation decision record
not architecture decision source
not default routing surface
not competing backlog/status register
```

---

## 5. Baseline Placement

The stable layer needs one place for baseline standing, not many.

Recommended transition:

### During Catch-Up

Use current sources as validation inputs:

```txt
BAR = accepted/runtime evidence source
NW backlog = accepted/deferred/future-decision routing source
IDRs = decision provenance source
contracts = runtime contract source
```

### After Catch-Up

Move durable baseline facts into:

```txt
011 = accepted architecture/decision anchors and accepted extensions
012 = vocabulary owned by those anchors
013 = routing rules and closure paths
013-known-gap-routing-register = open/deferred/future-decision status
```

Then demote:

```txt
BAR/NW/IDR working-surface files
  = historical evidence/provenance/reference
```

Do not demote BAR/NW/IDRs until every accepted row needed for future routing is represented in `011`, `012`, or `013-known-gap-routing-register`.

This avoids maintaining:

```txt
CDL + BAR + NW + IDR + companion + 013
```

as parallel active surfaces.

The intended active set after freeze should be:

```txt
000 package index
011 core decision records
012 vocabulary map
013 routing playbook
013 known-gap register
contracts/
CDL/successor authority
```

---

## 6. Patch Waves

### Wave 0 - Validation Baseline

Goal:

* confirm current staged/untracked state;
* confirm no hidden source-order drift;
* preserve current artifacts before patching.

Edits:

* none, except this plan.

Checks:

```bash
git status --short
rg -n "Status: active working surface|Authority:|Source Order" docs/agent-working-surface docs/status.md AGENTS.md
```

Exit:

* every later wave has a bounded target and stop condition.

### Wave 1 - Add Package Index And Source-Order Normalization

Goal:

* create a stable front door for the package;
* make source authority explicit before any content catch-up.

Patch:

* add `000-decision-anchor-layer-index.md`;
* patch source-order statements in `009`, `011`, `012`, `013`, `014`, `015`;
* do not yet update active repo routers.

Validation:

```bash
rg -n "CDL|canonical-decision-ledger|Baseline Acceptance|derived consumable|recovery verification" .review/001-architecture-decisions-listing-passes/000-decision-anchor-layer-index.md .review/001-architecture-decisions-listing-passes/009-decision-anchor-extraction-charter.md .review/001-architecture-decisions-listing-passes/011-core-architecture-decision-records.md .review/001-architecture-decisions-listing-passes/013-gap-routing-playbook.md
```

Stop if:

* any file claims the new layer overrides CDL or contracts.

### Wave 2 - Upstream DEC Catch-Up

Goal:

* patch `011` before downstream vocabulary/routing.

Patch:

* add CDL anchors to every DEC;
* add accepted-extension sections for durable post-ADR decisions;
* add current-status caveats for deferred execution/future decisions.

Catch-up set:

* IDR-021/023 activity role-action model;
* IDR-024 multi-axis containment if not already fully represented;
* IDR-025 pattern definition delivery;
* IDR-026 resolver routing and exact canonical resolution;
* IDR-027/028/BAR-104 production auth;
* IDR-029/NW-050 assignment-admin command capability;
* IDR-030/NW-055 shared-device session lifecycle;
* BAR-004 subject-history backfill;
* BAR-005 platform payload contracts;
* BAR-010/NW-034 config package/schema hygiene.

Validation:

```bash
rg -n "CDL-[0-9]{3}" .review/001-architecture-decisions-listing-passes/011-core-architecture-decision-records.md
rg -n "IDR-02[1-9]|IDR-030|BAR-004|BAR-005|BAR-010|BAR-104|NW-050|NW-055" .review/001-architecture-decisions-listing-passes/011-core-architecture-decision-records.md
rg -n "auto-resolution execution|resolver reassignment|future_decision|deferred" .review/001-architecture-decisions-listing-passes/011-core-architecture-decision-records.md
```

Stop if:

* a catch-up patch adds new envelope fields/types;
* it treats runtime evidence as new architecture;
* it makes deferred execution active.

### Wave 3 - Vocabulary Catch-Up

Goal:

* patch `012` after `011` is stable.

Patch:

* add vocabulary for accepted extensions;
* update stale open-front labels;
* preserve term collision boundaries.

Validation:

```bash
rg -n "principal binding|OIDC|JWT|assignment_admin|shared-device|subject-history|platform payload|config package schema|role-action" .review/001-architecture-decisions-listing-passes/012-vocabulary-anchor-map.md
rg -n "exact role-action artifact open|shared-device storage partitioning  \\| Implementation concern" .review/001-architecture-decisions-listing-passes/012-vocabulary-anchor-map.md
```

Stop if:

* implementation terms become architecture vocabulary without DEC/CDL anchor.

### Wave 4 - Split `013` And Merge Companion Roles

Goal:

* make `013` the one stable operational router;
* move volatile known gaps into a separate register;
* absorb useful companion routing roles;
* prepare companion retirement.

Patch:

* keep `013-gap-routing-playbook.md` for stable rules;
* create `013-known-gap-routing-register.md`;
* move current section 8 known gaps to the register and patch them against current BAR/NW/IDR status;
* merge companion sections into stable `013` where they are general routing rules;
* do not duplicate long exploration provenance.

Validation:

```bash
rg -n "irreversibility|mechanism|instance|config-as-code|device/server|do not promote|escape hatch|implementation prompt" .review/001-architecture-decisions-listing-passes/013-gap-routing-playbook.md
rg -n "GAP-|last-reviewed|BAR-|NW-|IDR-" .review/001-architecture-decisions-listing-passes/013-known-gap-routing-register.md
rg -n "Exact role-action tables are not defined|ADR-003 settles the access model but defers role-action" .review/001-architecture-decisions-listing-passes/013-known-gap-routing-register.md
```

Stop if:

* the stable router contains volatile current-status rows that will churn often;
* the known-gap register closes gaps silently.

### Wave 5 - Retire Superseded Active Surfaces

Goal:

* remove parallel maintenance surfaces after the new layer is validated.

Patch:

* retire `docs/agent-working-surface/architecture-rationale-and-routing-companion.md` by replacing active guidance with a pointer to stable `013`;
* mark BAR/NW/IDRs as provenance only after their durable facts are folded into the new layer;
* update `docs/agent-working-surface/README.md`, `docs/status.md`, and `AGENTS.md` to point to the package index.

Validation:

```bash
rg -n "architecture-rationale-and-routing-companion.md" AGENTS.md docs/status.md docs/agent-working-surface/README.md .review/001-architecture-decisions-listing-passes
rg -n "Source Order|Current Routing|Default implementer" AGENTS.md docs/status.md docs/agent-working-surface/README.md
```

Stop if:

* active routers still require agents to maintain both the companion and `013`;
* BAR/NW are retired before their needed facts are represented in the new layer.

### Wave 6 - Final Coherence Audit

Goal:

* produce the validation checkpoint that allows the new layer to be used as the stable surface.

Patch:

* add final audit artifact, likely `018-stewardship-layer-freeze-audit.md`;
* record checks and residual known risks.

Validation:

```bash
rg -n "35 normalized|35 decision|WORKFLOW\\s*\\|\\s*6" .review/001-architecture-decisions-listing-passes
rg -n "Platform evolution" .review/001-architecture-decisions-listing-passes/013-gap-routing-playbook.md .review/001-architecture-decisions-listing-passes/013-known-gap-routing-register.md
rg -n "new envelope|new event type|authority_context|deployer-authored access|deployer-authored state" .review/001-architecture-decisions-listing-passes
```

Exit:

* new layer can become active default stewardship surface.

---

## 7. Churn Control Rules

Use these rules across every wave.

* Patch upstream before downstream.
* Patch one artifact role per commit/slice.
* Preserve old material as reference until final freeze.
* Do not update active routers until the new layer has passed final audit.
* Do not keep duplicate active surfaces after final audit.
* Do not rewrite prose for style.
* Do not collapse validation evidence into architecture decisions.
* Do not promote open fronts while moving text.
* Prefer pointers over copying long rationale.
* Every moved role must have an explicit new home.

---

## 8. Stop Conditions

Stop and surface drift if any patch would:

* replace CDL as architecture authority without a formal source-order decision;
* add or imply new envelope fields or event `type` values;
* make general trigger execution, auto-resolution execution, or resolver reassignment active;
* make IdP groups/claims/JWT `actor_id` direct platform authority;
* introduce new scope mechanisms;
* turn subject-history into broad audit/history pull;
* make mobile local advisory behavior authoritative;
* turn implementation storage/API details into architecture;
* retire BAR/NW/IDR facts before they are represented in the new layer;
* leave `013` and the rationale companion both active after the merge.

---

## 9. Recommended Next Slice

The next implementation slice should be Wave 1 only:

```txt
Add package index and normalize source-order wording.
```

Files to patch:

```txt
.review/001-architecture-decisions-listing-passes/000-decision-anchor-layer-index.md
.review/001-architecture-decisions-listing-passes/009-decision-anchor-extraction-charter.md
.review/001-architecture-decisions-listing-passes/011-core-architecture-decision-records.md
.review/001-architecture-decisions-listing-passes/012-vocabulary-anchor-map.md
.review/001-architecture-decisions-listing-passes/013-gap-routing-playbook.md
.review/001-architecture-decisions-listing-passes/014-architecture-decision-coherence-audit.md
.review/001-architecture-decisions-listing-passes/015-decision-anchor-correction-patch.md
```

Do not patch active repo routers in that slice.

Reason:

```txt
Source-order clarity must be correct before content catch-up begins.
```
