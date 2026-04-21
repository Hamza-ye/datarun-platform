# Annotation Conventions

> **Lifespan**: permanent. After Phase 4 freeze, applies to any future
> back-annotation of historical docs.

Exploration docs, scenarios, walk-throughs, checkpoints, experiments, and
prior phase specs are **archival**. Their bodies are never rewritten. They are
**annotated in place** using append-only markers so future readers see both
what was once considered AND what the considered thing became.

## The five annotation markers

Each marker is appended on its own line, immediately after the section/claim
it refers to. Use exactly the prefix shown.

### `>>> OPEN-Q: <text>`
Marks a forward-reference: a question, assumption, or pending classification
that depends on a future decision.

Example:
```
The envelope's `type` field can take any string the producer needs.
>>> OPEN-Q: should `type` be a closed vocabulary or open?
```

### `>>> CLOSED BY ADR-NNN: <decision>`
Resolves a prior `>>> OPEN-Q:` or settles a previously implicit assumption.

Example:
```
>>> OPEN-Q: should `type` be a closed vocabulary or open?
>>> CLOSED BY ADR-001-R: closed at 6 values [capture, review, alert, task_created, task_completed, assignment_changed].
```

### `>>> STALE — see ADR-NNN: <what was wrong>`
Marks a body claim that is no longer correct. The body itself is not edited.

Example:
```
We will model conflict resolution as a separate envelope `type`.
>>> STALE — see ADR-001-R: conflict_resolved is a shape, not a type. Envelope type vocabulary is closed.
```

### `>>> RECLASSIFIED BY ADR-NNN: <was X, now Y>`
A concept the doc treated as classification X has been reclassified as Y by a
later ADR. Used when the body's framing is wrong but the underlying concept
still exists.

Example:
```
The role-action binding is a primitive of the authorization model.
>>> RECLASSIFIED BY ADR-007: role-action is a CONFIG (per-deployment policy table), not a PRIMITIVE.
```

### `>>> ABSORBED INTO ADR-NNN`
The entire doc (or a specific section) has been fully absorbed into an ADR.
Use when no further annotation is meaningful — the doc's content lives on in
the ADR.

Example (top of an exploration doc):
```
>>> ABSORBED INTO ADR-006: this entire exploration is now reflected in ADR-006 §2–§4.
```

## Rules

1. **Append-only.** Never rewrite the body. Only append markers.
2. **Locality.** Markers go immediately after the claim/section they
   annotate, not at the top or bottom of the file.
3. **One marker per claim.** If a claim is later re-reclassified, the new
   marker is appended below the old one. History preserved.
4. **ADR cite required.** Every marker except `>>> OPEN-Q:` cites an ADR.
   `>>> OPEN-Q:` may stand alone; the drift gate counts these as open
   forward-refs.
5. **Phase 4 freeze precondition.** Every `>>> OPEN-Q:` in any doc has a
   matching `>>> CLOSED BY`, `>>> STALE`, `>>> RECLASSIFIED BY`, or
   `>>> ABSORBED INTO` annotation below it.
