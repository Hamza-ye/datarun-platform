# Agent Lessons

> **LEGACY AGENT NOTES.**
> Do not use this file as authority for platform-spec atomization. The current
> atomization path starts at
> [`../platform-spec-kernels/professional-baseline/README.md`](../platform-spec-kernels/professional-baseline/README.md).
> Keep only lessons that do not conflict with that professional baseline.
>
Persistent learnings captured after corrections. Review at session start when relevant.

---

## Patterns to Avoid

### L1 — Do not treat envelope `type` as extensible (2026-04-21)

The envelope `type` vocabulary is **closed at 6 values** by ADR-4 S3. Four strings that look like types (`conflict_detected`, `conflict_resolved`, `subjects_merged`, `subject_split`) are **shape names**, not envelope types. The Phase 1/2 code drifted into using them as envelope types; the drift was caught at Phase 3d close-out and is being retrofitted in Phase 3e.

**Rules**:

- If a spec says "add a new event type", translate to "add a new shape" before writing code.
- Never filter integrity-event code on `type == "..."`. Always filter on `shape_ref` prefix.
- Authorship (human vs system actor) is carried by `actor_ref`, never by envelope `type`. The same shape can legitimately span multiple envelope types based on who authored it.
- When in doubt during platform-spec work, read
  [`../platform-spec-kernels/professional-baseline/11-adr007-envelope-type-assessment.md`](../platform-spec-kernels/professional-baseline/11-adr007-envelope-type-assessment.md)
  and
  [`../platform-spec-kernels/professional-baseline/12-adr008-reference-fields-assessment.md`](../platform-spec-kernels/professional-baseline/12-adr008-reference-fields-assessment.md)
  before writing spec language.

**How this drift got in**: Phase 1 and Phase 2 were implemented before ADR-4 S3 was ratified. ADR-4 did not audit Phase 1/2 code for type-vocabulary conformance. Lesson: when an ADR closes a vocabulary, run a grep audit across already-merged code the same day and file a retrofit phase if drift is found. Do not assume "future phases will notice."

### L2 — Silent deferral is a forbidden pattern (2026-04-21)

When you observe a position that is "correct today but could drift under future work" — or when you defer verification of a claim because the current phase is narrow — record it in the current owning gap/control surface, not in memory.

For platform-spec atomization, the owning surface is
[`../platform-spec-kernels/professional-baseline/05-decision-gap-register.md`](../platform-spec-kernels/professional-baseline/05-decision-gap-register.md)
or the relevant control overlay. The legacy flagged-position register is not an
atomization authority source.

**Rules**:

- **Rule R-1 (no silent deferral)**: Add or update a professional-baseline gap/control entry before closing the atomization pass. "I'll remember" is not a mechanism.
- **Rule R-2 (gates are verifiable)**: Every hold-back names a verifiable closure route — not "we believe this is fine."
- **Rule R-3 (status changes with evidence)**: Moving an item to closed/completed requires citing the artifact that made the gate pass.
- **Rule R-4 (consult before atomization)**: Read the professional-baseline README, baseline, gap register, boundary map, and control overlays before drafting a spec atom.
- **Rule R-5 (classify, don't delete)**: If later material absorbs a gap, classify it and preserve the source boundary. History matters for traceability.

The professional-baseline gap/control set is the counter-mechanism to cross-session memory loss during atomization. Treat it as load-bearing.

## Patterns That Work

### L-W1 — Shape carries domain fact; type carries pipeline behavior; actor carries authorship

Three orthogonal axes. Keeping them orthogonal dissolves most "where should this discriminator live?" questions:

- **`shape_ref`**: what domain fact is this? (`conflict_resolved/v1`, `subjects_merged/v1`, any operator-defined shape)
- **`type`**: what processing pipeline? (`capture` / `review` / `alert` / `task_created` / `task_completed` / `assignment_changed`)
- **`actor_ref`**: who authored it? (human UUID, or `system:{component}/{id}` for system actors)

If you find yourself wanting to encode one axis on another, stop and re-read the
professional-baseline boundary map and the ADR-007 through ADR-009 assessments.
The orthogonality is intentional.
