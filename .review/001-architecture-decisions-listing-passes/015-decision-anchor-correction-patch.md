# 015 — Decision Anchor Correction Patch

## Context Capsule

* Artifact: `015-decision-anchor-correction-patch.md`
* Pass: Correction Patch after Pass 5 audit
* Status: Patch instruction artifact for local file correction
* Mode: Editorial/routing-consistency correction only; no redesign, no reopening, no new architecture decisions.
* Current authority note:

  * This correction patch repaired the original recovery-pass chain.
  * Current architecture authority remains the Canonical Decision Ledger.
  * Contracts govern crossed wire/process boundaries, and BAR/NW/IDR evidence remains validation input until folded by the catch-up waves.
  * `002` and `008` remain recovery lineage, not current authority over the CDL.
* Recovery verification anchor:

  * `002-phase0-decision-register.md`
* Recovered architecture-map reference:

  * `008-authoritative-architecture-map.md`
* Consumes previous artifacts:

  * `009-decision-anchor-extraction-charter.md`
  * `010-candidate-architecture-decision-inventory.md`
  * `011-core-architecture-decision-records.md`
  * `012-vocabulary-anchor-map.md`
  * `013-gap-routing-playbook.md`
  * `014-architecture-decision-coherence-audit.md`
* Purpose:

  * Provide exact correction instructions for the local assistant agent that has file-system access.
  * Correct artifact consistency issues identified by `014`.
  * Preserve the audit trail without changing architecture.
* Scope:

  * Patch `011`, `012`, and `013`.
  * Do not add, remove, or rename DEC records.
  * Do not change accepted architecture decisions.
  * Do not change source hierarchy.
  * Do not alter gap routing substance except classification-field hygiene and closure-path normalization.
* Non-goals:

  * Do not rewrite `014`.
  * Do not create new DEC records.
  * Do not close known gaps.
  * Do not define platform-spec details.
  * Do not perform a full line-level citation audit.
* Patch verdict:

  * Apply this patch before treating the decision-anchor layer as frozen.

---

## 1. Correction Summary

Apply three corrections:

```txt
PATCH-001: Correct 011 decision count from 35 to 36.
PATCH-002: Update 012 and 013 notes that carried the 011 count mismatch.
PATCH-003: Normalize 013 gap classifications and closure paths.
```

These corrections are editorial/routing consistency fixes.

They are not architecture decisions.

---

## 2. PATCH-001 — Correct `011` Decision Count

File:

```txt
011-core-architecture-decision-records.md
```

### 2.1 Replace all summary claims of 35 records

Find:

```txt
35 normalized architecture decision records
```

Replace with:

```txt
36 normalized architecture decision records
```

Find:

```txt
Pass 2 normalizes them into 35 decision records:
```

Replace with:

```txt
Pass 2 normalizes them into 36 decision records:
```

Find:

```txt
35 decision records
```

Replace with:

```txt
36 decision records
```

Only apply this where the phrase refers to the final normalized record count.

Do not change references to the charter’s expected range of approximately 25–35 unless the phrase is explicitly claiming the final count.

### 2.2 Correct normalization summary table

Find the `WORKFLOW` row in the normalization summary.

Current row should be corrected from:

```txt
WORKFLOW | 6 | Projection-derived state, Pattern Registry, composition, transition flags, source-chain, and auto-resolution retained.
```

to:

```txt
WORKFLOW | 7 | Projection-derived state, Pattern Registry, composition, transition flags, context.*, source-chain, and auto-resolution retained.
```

Find the total row.

Replace:

```txt
Total | 35
```

with:

```txt
Total | 36
```

### 2.3 Confirm actual decision index count

Do not remove or renumber:

```txt
DEC-WORKFLOW-07: Bounded auto-resolution for eligible flags
```

The correct stable decision count is:

| Domain       | Final records |
| ------------ | ------------: |
| `EVENT`      |             4 |
| `IDENTITY`   |             4 |
| `CONFLICT`   |             4 |
| `AUTH`       |             5 |
| `CONFIG`     |             8 |
| `WORKFLOW`   |             7 |
| `PROJECTION` |             2 |
| `BOUNDARY`   |             2 |
| `GUARD`      |             0 |
| **Total**    |        **36** |

### 2.4 Verification command / check

After patching, verify:

```txt
Count all headings matching:
## DEC-
```

Expected result:

```txt
36
```

Also verify:

```txt
DEC-WORKFLOW-07
```

still exists.

---

## 3. PATCH-002 — Update Count-Mismatch Notes in `012`

File:

```txt
012-vocabulary-anchor-map.md
```

### 3.1 Replace Context Capsule count note

Find the Count note that says:

```txt
011 says it produced 35 normalized records, but its final decision index includes DEC-WORKFLOW-07.
This pass maps against the actual decision IDs present in 011, including DEC-WORKFLOW-07.
The count mismatch is carried to 014-architecture-decision-coherence-audit.md; this pass does not silently remove or renumber any decision.
```

Replace with:

```txt
Count confirmation:
  011 has been corrected/audited to 36 normalized decision records.
  This pass maps against the actual decision IDs present in 011, including DEC-WORKFLOW-07.
  No DEC ID is removed or renumbered by this correction.
```

### 3.2 Replace quality gate row

Find:

```txt
Count mismatch in 011 is preserved for audit. | Satisfied.
```

Replace with:

```txt
011 final decision count is confirmed as 36. | Satisfied.
```

### 3.3 Replace handoff item

Find any handoff item equivalent to:

```txt
final count correction for 011 final decision total
```

Replace with:

```txt
full line-level source citation audit, if later needed
```

### 3.4 Verification checks

After patching, search `012` for:

```txt
35
count mismatch
```

Expected:

* No remaining live claim that `011` has a count mismatch.
* References to the historical audit issue may remain only if explicitly framed as resolved.

---

## 4. PATCH-003 — Update Count-Mismatch Notes in `013`

File:

```txt
013-gap-routing-playbook.md
```

### 4.1 Replace Context Capsule carried issue

Find the Known carried issue that says:

```txt
011 states 35 records but includes DEC-WORKFLOW-07; 012 mapped against actual IDs and carried the mismatch to audit.
This playbook does not correct the count. It preserves the issue for 014-architecture-decision-coherence-audit.md.
```

Replace with:

```txt
Count confirmation:
  011 has been corrected/audited to 36 normalized decision records.
  012 and 013 map against the actual DEC ID set, including DEC-WORKFLOW-07.
  No DEC ID is removed or renumbered by this correction.
```

### 4.2 Replace deferred/open count wording

Find any deferred/open item equivalent to:

```txt
Final correction of the 011 count mismatch.
```

Replace with:

```txt
Full line-level source citation audit, if later needed.
```

### 4.3 Replace quality gate wording

Find:

```txt
Count mismatch from 011 preserved for audit. | Satisfied.
```

Replace with:

```txt
011 final decision count is confirmed as 36. | Satisfied.
```

### 4.4 Replace handoff audit target

Find:

```txt
011 claims 35 records but includes DEC-WORKFLOW-07; verify count and correction path.
```

Replace with:

```txt
Verify 011 final count remains 36 and DEC-WORKFLOW-07 remains present.
```

---

## 5. PATCH-004 — Normalize `013` Gap Classification Fields

File:

```txt
013-gap-routing-playbook.md
```

### 5.1 Update the gap record template

Find:

```md
## GAP-<AREA>-<NN>: <Short name>

Classification:
Current owner or likely decision path:
Affected vocabulary:
Affected DEC anchors:
Baseline item affected:
Why it is still open:
Negative boundaries checked:
Architecture escalation trigger:
Closure path:
Required output:
Notes:
```

Replace with:

```md
## GAP-<AREA>-<NN>: <Short name>

Classification:
Baseline-extension category:
Current owner or likely decision path:
Affected vocabulary:
Affected DEC anchors:
Baseline item affected:
Why it is still open:
Negative boundaries checked:
Architecture escalation trigger:
Closure path:
Closure subtype:
Required output:
Notes:
```

### 5.2 Add field meanings

In the field-meaning table, add:

| Field                       | Meaning                                                                                                                                                                                            |
| --------------------------- | -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| Baseline-extension category | One of: `Platform evolution that does not violate accepted decisions`, `Expansion of an explicitly open front`, `Work on a front that is still underexplored or not settled`, or `Not applicable`. |
| Closure subtype             | Optional note such as `platform evolution memo`, `platform-spec detail`, `engineering spike`, or `policy document`; must not replace the approved closure path.                                    |

### 5.3 Preserve allowed gap classifications

Keep the allowed gap classifications exactly as:

```txt
Architecture decision gap
Platform-spec detail gap
Implementation/tooling gap
Operational policy gap
Product/problem evidence gap
```

Do not add `Platform evolution`, `open front`, or `underexplored front` to the allowed gap-classification list.

---

## 6. PATCH-005 — Normalize Known Gap Records in `013`

For every known-gap record in Section 8, ensure:

```txt
Classification:
```

contains exactly one of the five allowed gap classifications.

Add immediately after it:

```txt
Baseline-extension category:
```

Use this table.

| Gap section                                           | Correct `Classification`     | Correct `Baseline-extension category`                       |
| ----------------------------------------------------- | ---------------------------- | ----------------------------------------------------------- |
| 8.1 Auditor/query access                              | Architecture decision gap    | Work on a front that is still underexplored or not settled  |
| 8.2 Aggregate access semantics                        | Architecture decision gap    | Work on a front that is still underexplored or not settled  |
| 8.3 Actor-as-subject delivery rule                    | Architecture decision gap    | Work on a front that is still underexplored or not settled  |
| 8.4 Exact role-action table artifact                  | Platform-spec detail gap     | Not applicable                                              |
| 8.5 Exact Pattern Registry inventory                  | Platform-spec detail gap     | Expansion of an explicitly open front                       |
| 8.6 Pattern migration mechanics                       | Implementation/tooling gap   | Expansion of an explicitly open front                       |
| 8.7 Additional `context.*` values                     | Architecture decision gap    | Platform evolution that does not violate accepted decisions |
| 8.8 Additional auto-resolution policies               | Platform-spec detail gap     | Platform evolution that does not violate accepted decisions |
| 8.9 Flag queue ergonomics                             | Platform-spec detail gap     | Expansion of an explicitly open front                       |
| 8.10 Domain conflict resolution strategies            | Platform-spec detail gap     | Expansion of an explicitly open front                       |
| 8.11 Config authoring syntax                          | Implementation/tooling gap   | Not applicable                                              |
| 8.12 Setup lifecycle for new operational activity     | Platform-spec detail gap     | Not applicable                                              |
| 8.13 Reporting freshness semantics                    | Platform-spec detail gap     | Not applicable                                              |
| 8.14 Handoff package contents                         | Platform-spec detail gap     | Not applicable                                              |
| 8.15 Retention windows                                | Operational policy gap       | Not applicable                                              |
| 8.16 Worker offboarding / exit procedure              | Operational policy gap       | Not applicable                                              |
| 8.17 Regulatory encryption/redaction/erasure          | Architecture decision gap    | Platform evolution that does not violate accepted decisions |
| 8.18 Multi-tenant naming strategy                     | Architecture decision gap    | Platform evolution that does not violate accepted decisions |
| 8.19 Complexity budget changes                        | Platform-spec detail gap     | Platform evolution that does not violate accepted decisions |
| 8.20 Cross-activity cohort materialization            | Platform-spec detail gap     | Expansion of an explicitly open front                       |
| 8.21 Cross-activity subject access for a second actor | Platform-spec detail gap     | Expansion of an explicitly open front                       |
| 8.22 Scenario phasing for S23–S27                     | Product/problem evidence gap | Work on a front that is still underexplored or not settled  |

### 6.1 Important interpretation rule

The primary classification is the starting route.

It does not remove escalation triggers.

Example:

```txt
Cross-activity cohort materialization
Classification: Platform-spec detail gap
Baseline-extension category: Expansion of an explicitly open front
Architecture escalation trigger:
  If cohort materialization gives access outside assignment-derived scope,
  changes sync=access, introduces dynamic cross-event queries as authority,
  or requires new stored event fields.
```

---

## 7. PATCH-006 — Normalize Closure Paths in `013`

Where any known-gap entry uses:

```txt
Closure path: Platform evolution
Closure path: Platform evolution note
Closure path: Platform evolution / implementation-tooling gap
```

replace with one approved closure path plus optional subtype.

Approved closure paths:

```txt
Formal architecture decision
Platform-spec detailing
Implementation/tooling design
Operational policy definition
Product discovery / scenario thickening
```

Use this mapping:

| Gap                                     | Closure path                  | Closure subtype                                                                        |
| --------------------------------------- | ----------------------------- | -------------------------------------------------------------------------------------- |
| Additional `context.*` values           | Formal architecture decision  | Platform evolution memo for closed platform-fixed context vocabulary                   |
| Additional auto-resolution policies     | Platform-spec detailing       | Platform evolution detail within accepted auto_eligible/L3b guardrails                 |
| Regulatory encryption/redaction/erasure | Formal architecture decision  | Platform evolution memo plus operational/legal policy                                  |
| Multi-tenant naming strategy            | Formal architecture decision  | Platform evolution memo for durable naming semantics                                   |
| Complexity budget changes               | Platform-spec detailing       | Platform evolution detail within existing config-boundary guardrails                   |
| Pattern migration mechanics             | Implementation/tooling design | Architecture review only if historical interpretation or stored state semantics change |
| Exact Pattern Registry inventory        | Platform-spec detailing       | Platform evolution only for new platform behavior                                      |
| Domain conflict resolution strategies   | Platform-spec detailing       | Platform evolution only if new policy machinery is needed                              |

If an entry already uses an approved closure path, leave it unless it mixes several values in the same field.

---

## 8. PATCH-007 — Verification Checklist for Local Assistant Agent

After applying patches, run these checks.

### 8.1 Decision count check

In `011`:

```txt
count headings matching "## DEC-"
```

Expected:

```txt
36
```

### 8.2 Count mismatch check

Search in `011`, `012`, and `013`:

```txt
35 normalized
count mismatch
claims 35
```

Expected:

* No live unresolved mismatch language.
* Historical references only allowed in `014`.

### 8.3 Classification check

Search in `013`:

```txt
Classification: Platform evolution
Classification: Architecture decision gap /
Classification: Platform-spec detail gap /
Classification: Implementation/tooling gap,
Classification: Platform evolution /
```

Expected:

```txt
0 matches
```

Exceptions:

* These phrases may appear in explanatory prose only if not used as the `Classification:` field.

### 8.4 Baseline-extension category check

Search in `013`:

```txt
Baseline-extension category:
```

Expected:

```txt
22 known-gap records should each have one Baseline-extension category.
```

### 8.5 Closure path check

Search in `013`:

```txt
Closure path:
```

Each value should be one of:

```txt
Formal architecture decision
Platform-spec detailing
Implementation/tooling design
Operational policy definition
Product discovery / scenario thickening
```

Optional extra detail should appear under:

```txt
Closure subtype:
```

### 8.6 DEC ID integrity check

Search all of `011`, `012`, and `013` for DEC IDs.

Expected:

* Every referenced DEC ID exists in `011`.
* No DEC ID is removed.
* `DEC-WORKFLOW-07` remains present.
* No new DEC ID is introduced.

---

## 9. What Not To Change

Do not change:

* `002-phase0-decision-register.md`
* `003-phase1-adr2-identity-conflict-recovery.md`
* `004-phase2-adr3-auth-sync-recovery.md`
* `005-phase3-adr4-config-boundary-recovery.md`
* `006-phase4-adr5-state-progression-recovery.md`
* `007-phase5-cross-lineage-vocabulary.md`
* `008-authoritative-architecture-map.md`
* `009-decision-anchor-extraction-charter.md`
* `010-candidate-architecture-decision-inventory.md`
* `014-architecture-decision-coherence-audit.md`

Do not change any DEC ID.

Do not delete `DEC-WORKFLOW-07`.

Do not convert `Platform evolution` into a sixth gap classification.

Do not close any known gap.

Do not change architecture decisions.

---

## 10. After Patch Status

After this patch is applied, the expected status is:

```txt
Decision-anchor layer corrected and usable.
```

The chain becomes:

```txt
009 charter
010 candidate inventory
011 corrected 36 DEC records
012 vocabulary map updated to 36
013 gap routing playbook with clean classification fields
014 audit record preserved
015 correction patch record
```

The decision-anchor layer remains subordinate to current authority:

```txt
docs/architecture/adrs-decisions-canonical-ledger/canonical-decision-ledger.md
contracts/ where crossed
current accepted baseline evidence during catch-up
```

`002-phase0-decision-register.md` and `008-authoritative-architecture-map.md` remain recovery verification lineage for the original extraction pass.

---

## 11. Handoff Capsule

* This patch produced:

  * Exact correction instructions for `011`, `012`, and `013`.
  * Stable final count: 36 DEC records.
  * Classification / Baseline-extension category separation for `013`.
  * Closure path normalization instructions.
  * Verification checklist for the local assistant agent.
* Stable items after applying patch:

  * `011` final DEC count is 36.
  * `012` maps to the 36-record set.
  * `013` uses only five allowed gap classifications.
  * `Platform evolution` is no longer a gap classification; it is a baseline-extension category.
  * `014` remains the audit record.
* Items not changed:

  * Accepted ADR decisions.
  * Event envelope.
  * DEC IDs.
  * Vocabulary ownership.
  * Gap routing substance.
  * Known gap open/closed status.
* Required local-agent action:

  * Apply patch to `011`, `012`, and `013`.
  * Run verification checks in Section 8.
  * Report any failed check before making interpretive edits.
* Do not reinterpret:

  * This is an editorial/routing-consistency patch.
  * This is not architecture evolution.
  * This does not close gaps.
  * This does not supersede the CDL, contracts, current accepted baseline evidence, or the original `002`/`008` recovery lineage.
