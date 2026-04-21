# Agent Lessons

Persistent learnings captured after corrections. Review at session start when relevant.

---

## Patterns to Avoid

### L1 — Do not treat envelope `type` as extensible (2026-04-21)

The envelope `type` vocabulary is **closed at 6 values** by ADR-4 S3. Four strings that look like types (`conflict_detected`, `conflict_resolved`, `subjects_merged`, `subject_split`) are **shape names**, not envelope types. The Phase 1/2 code drifted into using them as envelope types; the drift was caught at Phase 3d close-out and is being retrofitted in Phase 3e.

**Rules**:

- If a spec says "add a new event type", translate to "add a new shape" before writing code.
- Never filter integrity-event code on `type == "..."`. Always filter on `shape_ref` prefix.
- Authorship (human vs system actor) is carried by `actor_ref`, never by envelope `type`. The same shape can legitimately span multiple envelope types based on who authored it.
- When in doubt, read [ADR-002 Addendum](../adrs/adr-002-addendum-type-vocabulary.md) before writing code.

**How this drift got in**: Phase 1 and Phase 2 were implemented before ADR-4 S3 was ratified. ADR-4 did not audit Phase 1/2 code for type-vocabulary conformance. Lesson: when an ADR closes a vocabulary, run a grep audit across already-merged code the same day and file a retrofit phase if drift is found. Do not assume "future phases will notice."

## Patterns That Work

### L-W1 — Shape carries domain fact; type carries pipeline behavior; actor carries authorship

Three orthogonal axes. Keeping them orthogonal dissolves most "where should this discriminator live?" questions:

- **`shape_ref`**: what domain fact is this? (`conflict_resolved/v1`, `subjects_merged/v1`, any operator-defined shape)
- **`type`**: what processing pipeline? (`capture` / `review` / `alert` / `task_created` / `task_completed` / `assignment_changed`)
- **`actor_ref`**: who authored it? (human UUID, or `system:{component}/{id}` for system actors)

If you find yourself wanting to encode one axis on another, stop and re-read the architecture docs. The orthogonality is intentional.
