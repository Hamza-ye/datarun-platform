> **⚠️ ARCHIVED** — This document describes the exploration methodology used during ADR development. It is not specific to any ADR. For the current exploration directory layout, see the [README](../README.md).
# ADR Exploration Framework

> Reusable methodology for exploring and committing architectural decisions.
> Designed to prevent premature locking, cross-ADR contamination, and scope creep.

---

## Working Principle

To manage this safely:

- Upstream ADRs should define constraints, not over-specified solutions
- Downstream exploration must explicitly declare assumptions
- Assumptions must be validated or replaced before being promoted into ADRs
- Decisions should be committed at the last responsible moment
- ADRs should remain revisable until their dependent decisions stabilize

## The Problem This Solves

ADRs in this project are not isolated — they form a network of constraints.

Example (simplified influence flow):

```
ADR-1 (Offline Data Model)
  → influences ADR-2 (Identity & Conflict)
      → influences ADR-3 (Auth & Sync)
          → influences ADR-4 (Configuration)
              → influences ADR-5 (Workflow)
```

Each ADR constrains the design space of the next, but does not fully determine it.

Two critical failure modes must be avoided:

1. **Premature locking**
   Writing an ADR too early, before understanding its downstream impact, leads to over-commitment.  
   When issues are discovered later, this forces costly revisions or pushes complexity into downstream ADRs as workarounds.

2. **Assumption contamination**
   Exploring downstream concerns requires assumptions about upstream decisions.  
   If these assumptions are not made explicit, they silently propagate into explorations and later become unintended constraints in ADRs.

---

## Two-Pass Process

### Pass 1: Explore in Dependency Order

Explore each ADR in sequence. Each exploration takes the previous exploration's *direction* as input — not a committed decision.

```
Explore ADR-2 ──→ Explore ADR-3 ──→ Explore ADR-4 ──→ Explore ADR-5
     ↑                  ↑                  ↑                  ↑
  ADR-1 (committed)  ADR-2 direction    ADR-2/3 direction  ADR-1/4 direction
```

**Rules for Pass 1:**

1. **No ADRs are written during Pass 1.** Only exploration documents.
2. **Each exploration explicitly states its upstream assumptions.** A section titled "Upstream Assumptions" lists what the exploration assumes about prior ADRs and what changes if those assumptions are wrong.
3. **Each exploration narrows — it does not decide.** The output is: options eliminated, options surviving, the pivotal question, and a directional lean with stated confidence.
4. **Explorations can flag upstream problems.** If exploring ADR-4 reveals that ADR-2's directional lean creates an issue, it is flagged — not silently absorbed. Flags are tagged `[BLOCKING]` (the lean in this document is conditional on the flag's outcome) or `[INFORMATIONAL]` (surface at audit; lean stands). A `[BLOCKING]` flag does not halt exploration — but the lean it affects must be explicitly marked conditional, stating what direction it takes if the flag resolves against the assumption.
5. **Downstream Consequences describe constraints, not decisions.** "This direction forces ADR-3 to address X" is a constraint. "ADR-3 should use RBAC" is a decision for ADR-3's exploration — it does not belong in Downstream Consequences. Use: "opens," "forces," "requires ADR-N to address." Avoid: "should," "will use," "determines."

**Exploration Document Structure:**

```markdown
# ADR-N Exploration: [Title]

## Upstream Assumptions
<!-- Tag each input: committed (from a written ADR) or assumed (from a prior exploration lean). -->
- ADR-X **committed**: [what the ADR decided — treat as a hard constraint]
- ADR-X **leans toward**: [directional lean from a prior exploration — treat as an assumption]
  - If this lean changes to [alternative]: [what changes in this exploration]

## What Must Be Decided
- Sub-decision list (atomic, scoped)

## Constraint Filter
- What the constraints/principles force (no option space)
- What remains open

## Options That Survive
- Option analysis with scenario stress tests

## The Pivotal Question
- What the real choice reduces to

## Directional Lean
- Where the analysis points, with stated confidence level (High / Medium / Low — see definition below)
- What would change the lean
<!-- If any upstream input is Medium or Low confidence, state what this lean becomes if that assumption fails. -->

## Downstream Consequences
<!-- Describe constraints this direction creates for downstream ADRs — not decisions for them.
     Use: "opens," "forces," "requires ADR-N to address." Avoid: "should," "will use," "determines." -->
- How this direction constrains the next ADR(s)

## Flags
<!-- Tag each flag [BLOCKING] or [INFORMATIONAL].
     BLOCKING: this exploration's lean is conditional on the flag's outcome.
               State: "Resolved if [condition]. If not, lean changes to [alternative]."
     INFORMATIONAL: surface at the audit; does not affect this exploration's lean. -->
- Issues detected with upstream assumptions
- Decisions that seem misplaced (belong in a different ADR)
- Unresolved tensions
```

### Confidence Levels

Every directional lean must declare one of three levels:

| Level | Meaning | Implication for downstream explorations |
|-------|---------|----------------------------------------|
| **High** | The analysis strongly converges. Changing it would require a fundamental shift in constraints or project goals. | Downstream may treat this lean as near-constraint but must still tag it as assumed. |
| **Medium** | The analysis favors one direction, but a plausible alternative exists. One new flag or scenario could shift it. | Downstream must explicitly state the contingency if this lean fails. |
| **Low** | The analysis is genuinely uncertain. The lean is a working hypothesis. | Downstream must treat this as a fork: analyze both paths and note where they diverge. |

### Irreversibility Filter (before stress testing)

Before stress-testing an ADR's findings, apply the irreversibility boundary test to every Phase 1 finding:

**"If we commit to this and later discover it's wrong, can we change it without migrating stored events or breaking deployed devices?"**

Classify each finding into one of three categories:

| Category | Test | Stress-test depth |
|----------|------|-------------------|
| **Envelope-touching** | Changes what is stored in every event, permanently | Full adversarial stress test |
| **Protocol/server-side** | Server logic that can evolve without data migration | Light validation — confirm the approach is sound, don't attack-path it |
| **Policy/configuration** | Deployment-configurable behavior | Document initial position, skip stress test |

Not all ADRs have the same constraint surface. ADR-002 had ~8 envelope-level decisions; ADR-003 had ~1. The exploration ceremony must scale to the actual constraint surface — applying uniform depth to every ADR inflates evolvable strategies into permanent-feeling commitments.

The filter output feeds the stress test scope: only envelope-touching findings get the full adversarial treatment. Everything else is documented as strategy or deferred.

### Pass 1 → Pass 2 Gate: Decision Audit

After all explorations are complete, run a decision audit. This audit is written as a standalone document in `docs/exploration/` (not inline in individual explorations and not in this framework file). It is the formal gate between Pass 1 and Pass 2. The audit covers:

1. **Extract all decisions** (explicit and implicit) from all explorations
2. **Check upstream assumptions** — do they hold across the full chain?
3. **Detect scope bleed** — decisions in one exploration that belong in another ADR
4. **Detect gaps** — decisions required by the chain that no exploration covers
5. **Verify the dependency order** — does the assumed sequence still hold?
6. **Produce a final decision map** — which decisions land in which ADR

**Gate criteria to proceed to Pass 2:**

- [ ] Every upstream assumption is validated or addressed
- [ ] Every decision is assigned to exactly one ADR
- [ ] No ADR contains decisions that depend on a later ADR
- [ ] No exploration flags remain unresolved
- [ ] The directional lean of each exploration is stable (not contradicted by downstream explorations)

### Pass 2: Write ADRs in Dependency Order

Write each ADR in sequence. Each ADR takes committed decisions from prior ADRs as input — not assumptions or directional leans.

```
Write ADR-1 (finalize) ──→ Write ADR-2 ──→ Write ADR-3 ──→ Write ADR-4 ──→ Write ADR-5
         ↑                       ↑               ↑               ↑               ↑
    Audit results           ADR-1 committed   ADR-1/2        ADR-1/2/3       ADR-1/2/3/4
                                              committed       committed       committed
```

**Rules for Pass 2:**

1. **Each ADR references only committed prior ADRs** — never explorations, never directional leans.
2. **Each ADR's Consequences section describes what it constrains downstream** — not what it decides for downstream ADRs. Use language like "enables," "makes possible," "constrains to" — never "decides" or "determines."
3. **Each ADR includes a "What This Does NOT Decide" section** — explicit about scope boundaries.
4. **Status progression**: Draft → Accepted (after review). Not "Accepted" during writing.
5. **After each ADR is written**, verify that its downstream consequences don't contradict any exploration's directional lean. If they do, the downstream exploration must be revisited before writing that ADR.
6. **The ADR commits decisions; the exploration documents the journey.** Rationale chains ("Forced by…"), evolvability analysis, scenario walkthroughs, and stress test narratives stay in the exploration. The ADR links to the exploration (header) and provides a Traceability table (sub-decision → classification → key forcing inputs). Context references upstream ADRs and what they deferred — it does not re-narrate the exploration phases.
7. **Sub-decisions are classified by permanence.** Use three tiers: *structural constraints* (change requires data migration), *strategy-protecting constraints* (server-side logic guarding a structural invariant — the invariant is permanent, the implementation can evolve), and *initial strategies* (evolvable, documented under "Strategy" headings). The irreversibility filter from exploration determines the tier.

---

## Principles for Scoping

- **One ADR = one coherent decision boundary.** If a decision can be made independently of the ADR's other decisions, it might belong elsewhere.
- **Consequences describe constraints, not decisions.** "Subject identity is UUID-based" constrains ADR-2. "Identity merge is stream-linking" is ADR-2's decision — it doesn't belong in ADR-1's consequences.
- **Prefer deferring over locking.** When uncertain whether a decision belongs here or downstream, defer it. A deferred decision can be pulled up; a premature decision must be walked back.
- **The "What This Does NOT Decide" table is a first-class section**, not an afterthought. It is the explicit contract between this ADR and downstream ADRs.

### Evolvability Check (V5 structural test)

Every sub-decision in an ADR should be checked against three questions:

1. Can a new variant be added without changing stored data or existing protocols?
2. Can the behavior be changed without migrating what's already deployed?
3. Can a deploying organization customize this without a platform code change?

"Yes" is the normal case and needs no special documentation. **When the answer is "no"**: document what is permanently locked and why the locking is intentional. Permanence is expected for some decisions — the purpose of this check is to ensure it's deliberate, not accidental.

These checks are performed during exploration, not in the ADR itself. The ADR records the classification result (structural constraint / strategy-protecting / initial strategy) and links to the exploration for the analysis. If a sub-decision's evolvability status is surprising or contentious, the exploration should document the reasoning in detail.

---

## File Naming Convention

Exploration files in `docs/exploration/` are numbered sequentially in creation order:

```
NN-slug.md    ← NN is the next available number; use a descriptive slug
```

The gate audit document is a dedicated file in this directory (e.g. `09-final-decision-audit.md`). The Pass 1 → Pass 2 gate is executed there — not inline in individual explorations and not in this framework file.

The current ADR sequence, each ADR's status, and per-ADR open questions are tracked in the latest audit file and project state document — not here.

ADR files in `docs/adrs/` follow `adr-NNN-slug.md`.
