# Orchestrator Skill Assessment — Pre-Ship-4

> **Audit date**: 2026-04-29
> **Auditor role**: Skill Reviewer (structural assessment)
> **Subject**: `.github/agents/Ship Orchestrator - datarun-platform.agent.md`
> **Corroborating evidence**: [gap-report-ships-1-3.md](gap-report-ships-1-3.md) | Ship retros 1–3 | `docs/flagged-positions.md` | `docs/charter.md` §Rhythm | `docs/convergence/concept-ledger.md`

---

## Executive Summary

The orchestrator skill is **structurally sound**. Its four decision frames, hard rules, and pressure-test protocol are well-designed and demonstrably effective: the gap report found **zero silent architectural drift** across three Ships and one closeout. The drift that did accumulate — 16 open FPs, two Ship-4 blockers (FP-014, FP-018), and the FP-001 silent-deferral failure — traces to specific gaps in the skill's secondary mechanisms (deferral tracking, closure granularity, and conformance timing), not to a fundamental design flaw.

The skill should be **patched in targeted areas** before Ship-4, not redesigned. Seven findings follow, with specific recommendations.

---

## FINDING 1: Deferral Tracking Has No Active Surfacing Mechanism

**Risk level**: HIGH

**Description**: The skill instructs agents to consult `docs/flagged-positions.md` (R-4) and to never silently defer (R-1), but provides no **active mechanism** for ensuring deferred items are re-surfaced at the right time. The FP register is a passive artifact — it depends on an agent reading it, understanding which FPs are relevant, and acting on the `Blocks:` field. The skill's "canonical sources — re-read every turn" list (line 41–53) includes `docs/flagged-positions.md` but does not enforce any FP-scan step in the per-Ship loop. The loop is: spec → slice → build → conformance review → walkthrough → retro + drift gate + tag. There is no "FP re-evaluation pass" as a named step.

**Evidence**:
- FP-001 survived three Ship retros (Ship-1, Ship-1b, Ship-2) with "silently re-aimed" language before the Ship-3 closeout caught it (Ship-3 retro §13.1, finding 1). The skill's R-1 rule forbids this, but the mechanism to detect it was absent — it took a cross-cutting system review to surface it.
- FP-014 (scope-eval pull-class temporal anchor) was latent from Ship-1 retro §3.3 but was not flagged as an FP until Ship-3 closeout Wave 2-A (FP-014 resolution log: "lay un-FP'd for 3 Ships"). The skill's "Ship spec — slim shape" template (line 96–105) includes "§5 FP consultation" as a pre-fill, but the consultation is for *existing* FPs — it does not check whether retro observations should have *become* FPs.
- FP-018 (`assignment_ended/v1` consumption) was latent since Ship-1 but only surfaced at Ship-3 closeout. The shape existed and validated from day one; no Ship's FP sweep caught that `ScopeResolver` ignored it.

**Recommendation**: Add a named step to the per-Ship loop between "retro" and "tag": **FP promotion sweep**. The step's rule:

> Every §3 retro observation classified as "carries forward" or "lands in Ship-N" MUST either (a) map to an existing FP whose `Blocks:` field names the carry-forward Ship, or (b) be promoted to a new FP entry before the retro is filed. An observation that carries forward without an FP entry is an R-1 violation by construction.

Add this as **H10** in the hard rules.

---

## FINDING 2: Closure Criteria (Frame 3) Lack Granularity on §S Parity

**Risk level**: MEDIUM

**Description**: Frame 3 (Closure check, line 85–87) requires: "commits carry scenario cites (H2), walkthroughs pass, FPs with new entries written, any new ADR followed by ledger/charter regen, drift gate PASS, retro filed with ADR risks assessed and handoff written, tag applied." This is a checklist of *process* gates but does not include a **substance** gate for §S–implementation parity. Rule R-7 was adopted at Ship-3 closeout *because* Frame 3 alone was insufficient — `field_count_budget` was marked STABLE while §S13 had no HTTP runtime enforcement, and FP-001 was carried against discarded code.

Frame 3 as written permits a Ship to close with:
- A ledger row promoted to STABLE for a §S that is `decided-unexercised`.
- A walkthrough that passes via the wrong strategy (conformity drift layer 2 in the skill's own taxonomy, line 11).

The skill *describes* the conformity drift risk (line 11–12, line 23–24) and prescribes the spec-conformance review (line 207–246) to catch it. But Frame 3 doesn't name the conformance review outcome as a closure prerequisite. A coding agent following Frame 3 literally could skip it.

**Evidence**:
- Ship-3 retro §13.4 adopted R-7 specifically because Frame 3 did not catch these failures at the originating Ship's retro.
- Gap report §Cross-Ship notes: "Closeout-as-phase not yet ritualized" (Low severity) — this is the symptom of Frame 3 being under-specified.

**Recommendation**: Amend Frame 3 to include:

> - Spec-conformance review dispatched and all findings routed (matches / drift / missing / partial per §Spec-conformance dispatch).
> - No concept-ledger row promoted STABLE→STABLE for a §S that is `decided-unexercised` (R-7).
> - FP promotion sweep completed (H10 per Finding 1).

---

## FINDING 3: Slice Selection Logic Is Sound But Has a Composite-Scenario Blind Spot

**Risk level**: MEDIUM

**Description**: Frame 1 (In scope for current Ship?, line 69–74) defaults to "no" and requires: (a) named in Ship's scenarios, (b) not in §6 exclusions, (c) introduces no new contract/primitive. This is tight and correctly conservative. H9 (line 36, added during Ship-3) addresses composite scenarios (S05/S20/S21) requiring explicit declaration of which bullets are exercised.

The blind spot is **latent cross-cutting work that is not scenario-named but becomes load-bearing within the slice**. FP-018 is the exemplar: `assignment_ended/v1` consumption is not a scenario concern — no scenario says "end an assignment." It is infrastructure that S03 (user-based assignment) implicitly depends on but that Ship-1's S03 slice did not exercise. Frame 1's test (a) — "named in Ship's scenarios" — correctly excludes it from Ship-1's scope. But no frame asks: "does this Ship's slice depend on infrastructure that no prior Ship has exercised?"

The skill's §3.1 risk enumeration in the spec template partially covers this (structural risks = §S under real load), but the risk enumeration is about *ADR positions*, not about *code paths that exist but have zero callers*.

**Evidence**:
- FP-018: `ScopeResolver` reads only `assignment_created/*` events; `assignment_ended/v1` is validated, stored, but never consumed. Three Ships passed without catching this because no scenario named "end assignment" and the risk enumeration checked §S positions, not code-path coverage.
- FP-001's substrate drift: the `role_stale` detector cited in FP-001 was discarded at Ship-1 rebuild; three retros re-aimed the FP without re-verifying the substrate existed.

**Recommendation**: Add a lightweight **substrate check** to the spec-conformance review dispatch (line 207–246). Append to the reviewer's prompt:

> Additionally: for each §S position the spec claims this Ship exercises, verify the code path exists and has at least one caller. Report any "exists but zero callers" findings as `substrate_gap`.

This is cheap (one grep per §S) and would have caught both FP-018 and FP-001's substrate drift at Ship-1.

---

## FINDING 4: Spec-Conformance Review Timing Permits Conformity Drift to Survive Build

**Risk level**: MEDIUM

**Description**: The skill prescribes spec-conformance review "after build close, before walkthrough authorship" (line 209). This is architecturally correct — finding is separate from fix, and the review is a second pair of eyes. However, the review runs *once per Ship*, at the end. Conformity drift introduced in an early commit can compound across later commits within the same Ship's build cycle. By the time the review runs, the routing of findings (drift → Ship-internal commit, line 218–223) may require rework that crosses multiple files.

In practice, Ships 1–3 were small enough that this was manageable. Ship-4 is projected to be larger (S04 + S08, two FP blockers to resolve, first ADR-005 §S exercise). The single-pass conformance review may be insufficient.

**Evidence**:
- Ship-3 retro §4 DR-2 / §13.2 G-2: the v2-current detection asymmetry was caught at conformance review time and required a post-tag closeout wave to address. Had the review run mid-build (after the `ConflictDetector` was touched or confirmed untouched), the asymmetry would have been surfaced earlier.
- Ship-3 closeout was 22 commits across 4 waves — a substantial correction phase that the skill's original rhythm did not anticipate.

**Recommendation**: For Ships that touch more than one ADR cluster (the skill's own over-size signal, line 117), prescribe a **mid-build conformance checkpoint**. This is not a full review — it is a quick "§6 commitments touched so far: which are matches, which are in-progress, any drift?" The full review still runs at build close.

---

## FINDING 5: Agent Confusion Surface — Orchestrator vs. Coding Agent Boundary

**Risk level**: LOW

**Description**: The skill's orchestrator/coding-agent boundary table (line 176–183) is clear in principle: the orchestrator reads and decides, the coding agent executes. The confusion surface is in the **handoff prompt template** (line 189–204). The template says "Task: [one-paragraph, in-scope-by-construction]" and "Out of scope: [explicit exclusions]." But it does not carry:
- The FP entries that block the task (R-4 requires consulting them, but the handoff doesn't embed them).
- The §S positions the task exercises (the coding agent must know which §S it is proving).
- The Frame 3 closure conditions the coding agent must satisfy before reporting "done."

A coding agent receiving only the handoff prompt has enough context to *build* but not enough to *self-check* against the governance framework. The orchestrator is supposed to be present for the self-check, but in a multi-session build (as Ships 1–3 were), the orchestrator may not be in the same session as the coding agent's final commits.

**Evidence**:
- Ship-2 retro §10: a `chore(orchestrator): ...` commit (`a54e60b`) landed outside Ship-2's scope tag, noted as "meta-work." The coding agent did not know this was scope-external because the handoff didn't carry scope boundaries.
- Ship-3 retro §10: two `chore(...)` commits without scenario cites, permitted by exemption but requiring after-the-fact justification.

**Recommendation**: Extend the handoff prompt template to include three additional fields:

```
FP gates: [FPs whose Blocks: field names this task — must be resolved or re-deferred]
§S proving: [ADR §S positions this task exercises — coding agent must verify exercised-met]
Closure: [subset of Frame 3 conditions the coding agent reports against]
```

---

## FINDING 6: The Drift Gate Script Scope Is Narrower Than the Skill Assumes

**Risk level**: LOW

**Description**: The skill references `scripts/check-convergence.sh` as the drift gate (H5, line 31: "Drift gate must PASS before any commit touching charter/ledger"). The skill's Frame 3 includes "drift gate PASS" as a closure prerequisite. But the script's actual scope is limited to four checks: charter/ledger cite discipline (checks 1–3) and shape-tree byte-identity (check 4). It does NOT check:
- §S–implementation parity (R-7) — caught by Ship-3 closeout, not by the script.
- Behavioral schema regression — tracked as FP-016.
- FP register consistency (e.g., FP citing discarded code).

The skill's language ("drift gate PASS") could lead an agent to treat the script's PASS as evidence that no drift exists. Ship-3 closeout §13.5 clarified the script's scope in a header comment, but the skill itself still refers to "drift gate" without qualifying its limitations.

**Evidence**:
- Ship-3 closeout §13.5 explicitly documents: "Behavioral schema regression and §S–implementation parity are NOT enforced by the script."
- Gap report §Convergence Verdict notes "Closeout-as-phase not yet ritualized" — the script alone is insufficient as a closure gate.

**Recommendation**: In the skill's Hard Rules section, amend H5:

> **H5.** Drift gate (`scripts/check-convergence.sh`) must PASS before any commit touching charter/ledger. Note: the script enforces cite-discipline and shape-tree parity only. §S–implementation parity (R-7), behavioral schema regression (FP-016 gate), and FP register substrate validity are verified by the spec-conformance review and FP promotion sweep, not by the script.

---

## FINDING 7: Recovery Protocol Does Not Re-Bootstrap FP State

**Risk level**: LOW

**Description**: The "Recovery — I lost track" section (line 252–255) prescribes: read charter §Status → ships/README.md → latest retro → git history. State in 5 bullets: active Ship, last closure, open items, blocking FPs, next action. This is sufficient for *Ship-level* recovery but does not ensure the agent re-reads the FP register's resolution logs. An agent recovering mid-closeout (as happened in Ship-3 Wave 2-B) would not know which FPs have partially-completed resolution logs without reading the full register.

**Evidence**:
- Ship-3 closeout §13.8 records three self-corrections in dispatches where the recovering agent cited stale or incorrect FP substrates. The recovery protocol's "read charter §Status" step would have shown FP-001 as OPEN, but not that its substrate was discarded.

**Recommendation**: Add to the recovery protocol step 1:

> For any FP whose `Blocks:` field names the active Ship or whose resolution log has entries within the last Ship's date range, read the full FP entry (not just the Status line).

---

## FINDING 8: Role Separation — The Builder Should Not Grade the Build

**Risk level**: HIGH

**Description**: The skill's original two-role model (orchestrator + coding agent) assigns both spec authorship and retro authorship to the coding agent. The agent who implements the build also evaluates whether the build met its criteria. This is the "grading your own exam" anti-pattern.

The spec-conformance review — already dispatched to an independent Code Reviewer — is the one mechanism that *does* separate finding from fixing. Ship-3's closeout proves its value: the coding agent's retro (§1–§12) declared all criteria met, but the independent system review found three retro-blocking findings. The conformance review is the template; the retro and spec §6 authorship should follow the same separation.

**Evidence**:
- Ship-3 retro §13.1: three retro-blocking findings survived the coding agent's self-authored retro and were caught only by the independent system review.
- Ship-4's §6 sub-decisions were already produced by an architect agent (`docs/ships/ship-4-section-6-draft.md`, cited in the gap report) — the practice evolved toward separation before the skill formalized it.
- The skill's own principle (line 210): "a reviewer that also fixes is a reviewer that rationalises" applies equally to a builder that also evaluates.

**Recommendation**: Formalize a four-role dispatch model:

| Role | Spec authorship | Retro authorship |
|---|---|---|
| **Orchestrator** | Mechanical pre-fills (§1, §4, §5, §6.5, §7) | Merge + pressure-test + FP sweep |
| **Software Architect** | §2 ADR §S analysis, §3.1 structural risks, §6 sub-decisions | — |
| **Coding Agent** | — (receives locked spec) | Factual sections (§1 what shipped, §3/§5 choices, §7 ledger, §8 journal) |
| **Code Reviewer** | — | Evaluative sections (§2 criteria evidence, §4 domain observations, §6 FP assessment) |

The seam is natural: the coding agent knows what was *tried* (§3 implementation choices, §8 session journal) — only the builder has this context. The Code Reviewer knows what was *achieved* (§2 criteria met, §4 observations independent of builder bias) — only an independent eye has this property. The architect knows what *should* be built (§6 sub-decisions, §3.1 risks) — this requires system-level judgment the coding agent may not bring.

---

## Verdict

**The skill should be patched before Ship 4, but not redesigned.**

The gap report's own conclusion — "zero silent architectural drift" across three Ships — is evidence that the skill's core design (four frames, hard rules, pressure-test protocol, spec-conformance review) works. The drift that accumulated is **deferral-tracking drift** (FP-001, FP-014, FP-018), **closure-granularity drift** (`field_count_budget` STABLE without enforcement), and **evaluation-independence drift** (coding agent's retro missing findings that independent review caught). All three trace to specific gaps in the skill's secondary mechanisms, not to its primary architecture.

The skill's three-layer drift taxonomy (protocol / conformity / domain) is the right framework. The problem is that layer 1 (protocol drift) has the strongest mechanical enforcement (drift gate script, H1–H9), while layers 2 and 3 rely on human-agent judgment with no mechanical backstop. The findings above add targeted mechanical backstops:

| Finding | Patch | Effort |
|---|---|---|
| F1 — Deferral tracking | H10 (FP promotion sweep as named step) | 1 paragraph in skill |
| F2 — Closure granularity | Amend Frame 3 with R-7 + conformance + H10 | 9-item numbered list |
| F3 — Substrate blind spot | Amend conformance review prompt | 2 sentences in template |
| F4 — Mid-build checkpoint | Conditional mid-build review for multi-cluster Ships | 1 paragraph in skill |
| F5 — Handoff enrichment | Extend handoff template with 3 fields | 3 lines in template |
| F6 — Drift gate scope | Amend H5 with scope qualifier | 1 sentence in skill |
| F7 — Recovery FP bootstrap | Amend recovery step 1 | 1 sentence in skill |
| F8 — Role separation | Four-role dispatch model with spec §6 + retro split | New section replacing 2-role table |

All eight patches have been applied to the skill file. The changes are structural but minimal — the skill's core architecture (four decision frames, hard rules, pressure-test protocol, canonical sources) is unchanged. What changed is: who writes what (four roles instead of two), when evaluation happens (H10 FP sweep + mid-build checkpoint), and what the closure gate checks for (Frame 3 expanded from prose to 9-item checklist).

**The drift had causes the skill could have prevented but did not cause.** The skill's design is correct; its coverage had gaps at the deferral-to-surfacing seam and the builder-evaluator boundary. The eight patches close those gaps. Ship-4 can proceed with the patched skill.

---

## Appendix: §Rhythm Assessment

The charter's §Rhythm section (charter.md line 170–201) and the skill's per-Ship loop are aligned. The five-step loop (spec → slice → build → acceptance → retro + tag) matches the charter's description. The charter adds R-7 as a per-Ship retro deliverable; the skill should mirror this (covered by Finding 2). The charter's "Session-survival mechanism" correctly identifies the drift gate as mechanical, the charter as the session-start read, and the Ship spec as the single in-flight work doc — all consistent with the skill's "canonical sources" section.

The one divergence: the charter says "retro **regenerates**, never edits" (line 194), referring to the charter itself. The skill's Frame 3 says "charter regen" as a closure step. These are aligned. No gap.

The §Rhythm's "Out-of-scope for Ships" list (S19 as constraint, S05/S20/S21 as composites, S15/S16/S18 as Phase-2 deferred) is consistent with the skill's H9 composite-scenario coverage rule. The skill's H9 was added during Ship-3 to prevent silent composite-scenario coverage — an appropriate mid-build correction that the §Rhythm section absorbed.

**§Rhythm verdict**: sound. The skill and the charter's §Rhythm are mutually consistent. No patch needed beyond what the seven findings already cover.
