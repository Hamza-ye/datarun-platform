---
description: "Create a Datarun Platform checkpoint snapshot: current standing, accepted baseline, open work, risks, and next march orders."
name: "Project Checkpoint"
argument-hint: "Optional focus, e.g. 'post NW-008', 'before new phase', 'returning after time away'"
agent: "agent"
---

# /checkpoint — Datarun Platform Standing Snapshot

You are creating a recurring checkpoint for the Datarun Platform project.

A checkpoint is not architecture authority, a viability assessment, or a full audit. It is a dated snapshot that lets the project owner return later and understand:

```text
where the platform stood,
which assumptions were accepted,
which risks were known,
what changed since the previous checkpoint,
and what should happen next.
```

## Context Rules

Use the current post-Phase-4 working surface. Do not reconstruct truth from old document chronology.

Read, in this order:

1. `AGENTS.md`
2. `docs/status.md` Current Routing section only.
3. `docs/agent-working-surface/README.md`
4. `docs/agent-working-surface/baseline-acceptance-register.md`
5. `docs/agent-working-surface/platform-next-work-backlog.md`
6. `docs/agent-working-surface/escape-hatch-register.md`
7. `docs/checklists/architecture-routing-checklist.md`
8. `docs/reviews/viability-closure-review.md`
9. `docs/scenarios/README.md`
10. The most recent checkpoint in `docs/checkpoints/`.
11. `git status --short`, `git log --oneline -8`, and focused diffs for recent commits named by the checkpoint trigger.

Open CDL rows, IDRs, contracts, scenario files, or code only when the working surface routes you there or when recent changes need verification. Use `scripts/query_cdl.py` or the CDL README/JSON catalog instead of loading the full CDL.

## Output Location

Save the checkpoint as:

```text
docs/checkpoints/checkpoint-[YYYY-MM-DD]-[short-topic].md
```

Use lowercase words separated by hyphens for `[short-topic]`, for example:

```text
docs/checkpoints/checkpoint-2026-06-01-post-phase-4-stabilization.md
```

If a checkpoint for the same topic already exists, use `-b`, `-c`, and so on.

## Output Format

Produce Markdown with this structure.

### 1. Bearing

Purpose: a 30-second re-entry point.

Include:

- **Anchor commit**: current implementation/status commit the checkpoint is based on.
- **Phase**: current phase or stabilization mode.
- **Momentum**: `ADVANCING`, `STALLED`, or `BLOCKED`, with one sentence.
- **Last milestone**: most recent meaningful completion.
- **Horizon**: next natural stopping point.

Then add a short narrative of 2-4 sentences.

### 2. Standing Baseline

Purpose: current accepted/candidate/deferred state.

Summarize:

- Accepted BAR rows.
- Remaining baseline candidates.
- Deferred/future-decision rows that must not be promoted by implication.
- Current active backlog rows by priority.

Use compact tables. Do not paste the full BAR or backlog.

### 3. Recent Movement

Purpose: show what changed since the previous checkpoint.

Include:

- Recent commits table: `Commit | Meaning | Evidence`.
- Important fixes or verification results.
- Any removed/superseded surfaces.

### 4. Architecture Guardrails

Purpose: preserve the important assumptions that governed recent work.

Include:

- Current source order.
- Non-negotiable guardrails.
- Escape-hatch status summary.
- FP-011/auth boundary reminder.

Reference CDL/IDR rows by ID or file path; do not quote large sections.

### 5. Risk Pulse

Purpose: changes only.

Use two tables:

```text
New or elevated risks
Resolved or de-risked items
```

For each risk, include severity, trigger, mitigation, and whether it needs a backlog row.

### 6. Scenario And Product Pressure

Purpose: keep product reality visible without turning scenarios into implementation authority.

Summarize:

- New/thickened scenarios.
- Which pressures are current baseline, verification-needed, deferred, or future-decision.
- Any wording or routing corrections made since the last checkpoint.

### 7. Verification Ledger

Purpose: make test evidence recoverable.

List recent targeted verification commands and what they accepted. Include failed/blocked verification only when it influenced decisions.

### 8. March Orders

Purpose: the next concrete actions.

Provide 3-5 items, ordered by leverage. Each item must include:

```text
Why now:
Expected artifact:
Scope:
Stop condition:
```

Tie each item to a BAR/NW row where possible.

## Output Rules

1. Be concrete. A checkpoint is useful only if it names files, rows, commits, and evidence.
2. Keep it readable. This is a snapshot, not a full audit.
3. Do not mark anything accepted unless the BAR already accepts it or the checkpoint work verified it.
4. Do not promote deferred/future-decision surfaces.
5. Record low-severity review findings if they can affect future assumptions.
6. If the working tree is dirty, include a clear overlay section before march orders.
7. Run `git diff --check` after writing or editing checkpoint files.
