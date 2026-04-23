# ADR-008: Envelope Reference Fields — `subject_ref`, `actor_ref`, `activity_ref`

> Status: **Decided**
> Date: 2026-04-23
> Convergence round: 1 (Phase 2 ADR #3)
> Upstream: ADR-001 §S3, ADR-001 §S5, ADR-002 §S2, ADR-004 §S2, ADR-004 §S4
> Exploration: archive/02, archive/09 Q1/Q4, archive/13, archive/16, archive/17, archive/18 · [Phase 0.5 harvest — Group 2](../convergence/inventory/disputes-harvest.md)

---

## Context

The event envelope carries three reference fields that point at domain objects the event is *about*: `subject_ref`, `actor_ref`, `activity_ref`. All three are live, all three appear in every sync payload, and all three have been used in consumers across the platform.

The Phase 0 inventory surfaced these as DISPUTED:

- `subject-ref`: A-subagent read PRIMITIVE, C1 read CONTRACT, C2 read PRIMITIVE.
- `actor-ref`: A-subagent read CONTRACT, B-subagent read RESERVED, C1 read CONTRACT.
- `activity-ref`: A-subagent read CONTRACT, B-subagent read RESERVED, C1 read CONTRACT.

The Phase 0.5 archive harvest ([Group-2](../convergence/inventory/disputes-harvest.md)) resolved all three disputes against the same principle: **the reference is not the referent.** A subject, actor, or activity-instance is a domain object — classified elsewhere. An envelope reference field is a *contract* for how events point at that object. The two live in different rows of the ledger because they are different things.

The B-subagent's RESERVED reading of `actor_ref` and `activity_ref` was a subagent artifact with no archive evidence; the harvest found all three fields fully decided across ADR-001/002/004 and reinforced in archive entries 16, 17, and 18. The only residual open surface is the forward question of whether `Assignment` ever becomes its own reference type — noted and deferred, not settled here.

This ADR canonicalizes the three fields as CONTRACT, states the reference-vs-referent rule explicitly, and records the one forward-reference item.

---

## Decision

### S1: `subject_ref` is a CONTRACT envelope field

**Shape**: typed UUID — `{ type, id }`. `id` is a client-generated UUID (ADR-001 §S3). `type` is a closed enum naming which identity category the reference points at.

**Type enum (closed at four values)**: `subject`, `actor`, `process`, `assignment`. ADR-002 §S2 names the four identity categories; this ADR canonicalizes their appearance in the reference contract. The enum is closed in the same sense as the envelope `type` vocabulary closed by ADR-007 §S1: extension is architecture-grade, not an IDR.

**Reserved sub-position**: the value `process` is present in the enum but has no current emission site on the platform. It is **RESERVED** — reserved in the ledger sense — for future process/workflow-instance references. No code path currently emits or consumes `subject_ref.type = "process"`. Extension of the active set beyond `subject`, `actor`, `assignment` requires an ADR.

**Reference vs. referent**: `Subject`, `Actor`, `Process-identity`, and `Assignment` as domain objects are classified in their own ledger rows (PRIMITIVE, CONFIG, etc., as their own rationales dictate). `subject_ref` is the envelope-level contract by which events point at any of them. The two classifications are orthogonal.

### S2: `actor_ref` is a CONTRACT envelope field

**Shape**: two forms are admissible on the platform.

1. **Human actor**: client-generated UUID (ADR-001 §S3, ADR-002 §S2). No structural prefix.
2. **System actor**: the string form `system:{source_type}/{source_id}`. `source_type` is an evolvable vocabulary — platform-grown as new system event sources surface. `source_id` is a stable identifier within that source type.

Current `source_type` values established in prior ADRs include `trigger` (ADR-004 §S4) and `auto_resolution` (ADR-007 §S2). Growth of the `source_type` vocabulary is **not** architecture-grade; it is a platform-level convention. The *field* is decided (CONTRACT); the *set of system source types* is evolvable.

**Why not a closed enum.** `source_type` names the *kind* of system that authored the event. Closing it would require an ADR for every new trigger source, automated resolver, or reconciliation routine — that is an authority-cost mismatch with how the platform evolves. Keeping `source_type` open is consistent with ADR-001's stance that operational vocabulary grows while the envelope shape does not.

**Discriminator**: a consumer that needs to distinguish human vs. system authorship tests whether the ref starts with the literal prefix `system:`. This is the binding rule cited by ADR-007 §S3 (authorship-based routing of `conflict_resolved/v1`).

### S3: `activity_ref` is a CONTRACT envelope field

**Shape**: a deployer-chosen identifier matching `[a-z][a-z0-9_]*`, or null.

**Optional with auto-population**. The field is structurally optional in the envelope schema, but deployer-facing authoring pipelines auto-populate it from the activity context in which the event was captured. This produces the property that the field is "effectively mandatory for human-authored events without the schema enforcing it," per the archive record. Events authored outside an activity context (certain system events, lifecycle events) legitimately carry `null`.

**Why not a closed enum.** Activity identifiers are *deployer-chosen names* for deployer-assembled activity instances (ADR-004 §S9). The activity *instance* is CONFIG. The envelope-level field that references it is CONTRACT — platform commits to the field's presence, shape, and grammar; the *names* that flow through it are a deployer concern.

**Disambiguation role**: `activity_ref` is what prevents the 4-way envelope ambiguity called out in archive entry 17 — without it, events in multi-activity deployments cannot be attributed to the activity that produced them.

### S4: Reference is not referent (binding rule)

Every ledger row for a `*_ref` envelope field classifies the *field contract*, not the *referent*. The domain object the ref points at gets its own row and its own classification. The two classifications may legitimately differ:

| envelope field | classification | referent | referent classification (separate row) |
|---|---|---|---|
| `subject_ref` | CONTRACT | `Subject` | PRIMITIVE (ADR-002 §S2) |
| `actor_ref` | CONTRACT | `Actor` | PRIMITIVE (ADR-002 §S2) |
| `activity_ref` | CONTRACT | activity instance | CONFIG (L0 assembly, ADR-004 §S9) |
| `subject_ref.type = "process"` | RESERVED | process-identity | RESERVED — no emission site |

Any inventory classification that conflates the two is a category error and resolves to this rule.

---

## Forbidden patterns

**F-B1: Never classify a `*_ref` field and its referent in the same ledger row.** They are different things. The ref is an envelope contract; the referent is a domain object. Each gets its own row.

**F-B2: Never extend the `subject_ref.type` enum without an ADR.** The four-value enum (`subject`, `actor`, `process`, `assignment`) is architecture-grade. Adding a fifth type is a structural change to how events point at domain objects; it must go through a new ADR, not an IDR.

**F-B3: Never treat `actor_ref` `source_type` as a closed enum.** It is an evolvable platform vocabulary. New system source types (`trigger`, `auto_resolution`, and any successor) are added as the platform grows and do not require an ADR.

**F-B4: Never filter on envelope `type` to distinguish human vs. system authorship.** The discriminator is `actor_ref.startswith("system:")`. This restates ADR-007 §S3 with the exact field and prefix. Filtering on envelope `type` for authorship is an F-A3 violation (ADR-007) and the same mistake in a different dress.

---

## Rejected alternatives

**Alt-1: Classify `subject_ref` as PRIMITIVE.** Rejected. A primitive is a load-bearing domain object the platform is built on. `Subject` is the primitive; `subject_ref` is the field contract that points at it. Collapsing the two into one row loses the distinction between what an event *is about* (the primitive) and *how it points at what it is about* (the contract). The distinction is load-bearing — consumers compile against the contract shape, not against the primitive.

**Alt-2: Classify `actor_ref` and `activity_ref` as RESERVED.** Rejected. The B-subagent framing was a subagent artifact with no archive evidence. Both fields are fully live: `actor_ref` in every event, `activity_ref` in every event captured within an activity context. Classifying live fields as RESERVED would contradict their present-day use and mislead readers into thinking the fields are future surfaces rather than current contracts.

**Alt-3: Close the `actor_ref` `source_type` vocabulary.** Rejected per §S2 rationale — the authority cost of requiring an ADR for each new system source type does not match how the platform grows system-authored event sources. Leaving it open preserves the decided field contract without freezing the operational vocabulary inside it.

**Alt-4: Roll `assignment_ref` into this ADR as a new envelope field.** Rejected as premature. No archive material commits either way on whether Assignment evolves into its own envelope reference type, and the operational volume of assignment-targeting events has not forced the question. Deferred under the Forward-reference block below.

---

## Consequences

### Supersessions

**None.** ADR-001 §S3/§S5, ADR-002 §S2, and ADR-004 §S2/§S4 are re-cited, not superseded. This ADR names the canonical convergence-era cite for each field.

### Ledger updates (round 1)

| concept | was | becomes | settled-by | status |
|---|---|---|---|---|
| `subject-ref` | DISPUTED | CONTRACT | ADR-008 §S1 | PROPOSED |
| `actor-ref` | DISPUTED | CONTRACT | ADR-008 §S2 | PROPOSED |
| `activity-ref` | DISPUTED | CONTRACT | ADR-008 §S3 | PROPOSED |
| `process-identity` | RESERVED | RESERVED (re-cited) | ADR-008 §S1 | PROPOSED |

### Charter updates

Contracts table gains three rows: `subject_ref`, `actor_ref`, `activity_ref`. No new invariants.

### Forward reference

**Open question (explicit, registered)**: If `Assignment` evolves into a reference type with emission sites distinct from the current `subject_ref.type = "assignment"` channel, a structural decision surfaces — parameterize existing fields or add a dedicated `assignment_ref`. No archive material commits either way. A successor ADR is required before any such evolution lands.

This item is logged in [flagged-positions.md](../flagged-positions.md) under convergence round 1 so it cannot be silently deferred.

---

## Traceability

| Subject | Source |
|---|---|
| Typed UUID envelope references | ADR-001 §S3, §S5 (first decision); ADR-008 §S1 (canonical) |
| Four identity categories | ADR-002 §S2; ADR-008 §S1 canonicalizes the enum in the ref contract |
| `actor_ref` system form | ADR-004 §S4; archive entries 16, 18 |
| `activity_ref` optional-with-auto-populate | ADR-004 §S2; archive entry 17 (Attack 1 + optional-mandatory note) |
| Authorship discriminator (`system:` prefix) | ADR-007 §S3; restated here as F-B4 |
| `process` reserved in type enum | envelope schema contract (platform-level) |
