# ADR-009: Platform-Fixed Mechanism vs. Deployer-Configured Instance — Scope, Pattern, Activity

> Status: **Decided**
> Date: 2026-04-23
> Convergence round: 1 (Phase 2 ADR #4)
> Upstream: ADR-003 §S7, ADR-004 §S7, ADR-004 §S9, ADR-005 §S5
> Exploration: [Phase 0.5 harvest — Group 3](../convergence/inventory/disputes-harvest.md); [phase-1 topology §Bucket-C](../convergence/inventory/phase-1-topology.md)

---

## Context

Three concepts from the round 0 inventory remained DISPUTED after the first three ADRs of round 1 landed: `scope`, `pattern`, and `activity`. The Phase 0.5 harvest found that all three disputes share one structure — a platform-fixed **mechanism** (the shape of the thing, its closure, its semantics) paired with a deployer-configured **instance** (the named, parameterized occurrence that a deployer authors and ships in a configuration package).

A single classification is lossy by construction:

- Calling `scope` purely PRIMITIVE hides that deployers author concrete scope instances (a geographic tree, a subject list, an activity set).
- Calling `scope` purely CONFIG hides that the mechanism and the scope-type registry are platform-fixed and cannot be extended by deployers.
- The same tension repeats for `pattern` (closed pattern registry vs. deployer-authored activity that selects and parameterizes a pattern) and for `activity` (deployer-assembled L0 instance vs. the fact that `activity_ref` is a platform-level CONTRACT).

The rule that resolves the three has been **latent** in the decided corpus since ADR-003/004/005:

- ADR-003 §S7 closes scope types to a platform-fixed set while leaving scope *instances* to deployer assignment.
- ADR-004 §S7 restates the closure: *"Scope types are platform-fixed. Deployers select and compose scope types for each assignment but cannot define custom scope types or custom containment logic."*
- ADR-004 §S9 (four-layer configuration gradient) places activities at L0 assembly: deployers *compose* from platform-provided components.
- ADR-005 §S5 closes the pattern registry the same way: *"Patterns are a closed vocabulary with the same governance as event types — platform-fixed, deployer-referenced, not deployer-authored."*

ADR-009 **names** this rule, records it as a charter invariant, and applies it to settle the three disputed rows. No upstream ADR is superseded; each is re-cited.

---

## Decision

### S1: Duality rule (charter invariant)

**Classification rule.** When a platform concept exposes both a *mechanism* (a closed set, a grammar, a protocol, or a typed interface owned by the platform) and an *instance surface* (named, parameterized, and authored by deployers), the two are classified separately:

- **Mechanism → PRIMITIVE.** Closure, grammar, and semantics are platform-fixed. Extension is architecture-grade (new ADR, not an IDR).
- **Instance → CONFIG.** Deployers name, parameterize, and ship instances in the configuration package. Adding a new instance is a deployer action, not a platform evolution.

**Binding obligation.** Every ledger row for a concept that exhibits this duality must classify the mechanism and the instance surface in separate rows. A single row that conflates the two is a classification error and resolves to this rule.

**Scope of the rule.** This rule is general. It applies to every present and future concept on the platform that exhibits the mechanism/instance split. The three rows settled below are the round-1 applications; future rows exhibiting the same shape are decidable by reading this rule without a new ADR.

**Relationship to prior decisions.** This rule is not new. It is the unifying logic already present in ADR-003 §S7 (scope-type closure), ADR-004 §S7/§S9 (scope-type closure + four-layer gradient), and ADR-005 §S5 (pattern-registry closure). ADR-009 makes the rule explicit so that classification decisions outside the three concepts named below do not require a new ADR to rediscover it.

### S2: `scope` is PRIMITIVE

**Classification.** The `scope` concept — the mechanism by which the platform decides what data an actor may see and act on — is a platform-fixed PRIMITIVE. The scope-type registry (`geographic`, `subject_list`, `activity`) is closed at three values (ADR-003 §S7, ADR-004 §S7). The scope-containment test is platform code. Composition semantics (AND across non-null dimensions; null = unrestricted on that axis) are platform-fixed.

**Instance rows (unchanged).** The concrete scope instances a deployer authors remain CONFIG in their own rows: `geographic-scope`, `subject-list-scope` (and its synonym `subject-based-scope`), `scope-composition`, `scope-type`, `temporal-access-bounds`. These rows are not altered by this ADR.

**Why PRIMITIVE not CONFIG.** A mistake in scope mechanism is a data-leak vulnerability (ADR-004 §S7). Platform ownership of the mechanism is a security obligation, not a stylistic preference.

### S3: `pattern` is PRIMITIVE

**Classification.** The `pattern` concept — the mechanism by which the platform provides named workflow skeletons that deployers select and bind into activities — is a platform-fixed PRIMITIVE. The pattern registry is closed (ADR-005 §S5): patterns are deployer-*referenced*, not deployer-*authored*. Adding a pattern is a platform evolution.

**Instance rows (unchanged).** `pattern-role`, `pattern-composition-rule`, and `pattern-state` remain in their prior classifications (CONFIG, CONFIG, DERIVED respectively). A concrete activity that selects and parameterizes a pattern is an instance of activity (§S4), not a new pattern.

**Dedupe note.** The row `workflow-pattern` is a synonym of `pattern` (round 0 note). It is not promoted to its own ADR-level row; it remains CONFIG as a pointer to `pattern`, consistent with the synonym policy that `role-staleness`/`role-stale` followed in ADR-006.

### S4: `activity` is CONFIG

**Classification.** An `activity` — a deployer-assembled L0 unit composed of a selected pattern, one or more shape bindings, role-to-pattern-role mappings, and scope parameters — is CONFIG. It is the canonical L0 instance in the four-layer gradient (ADR-004 §S9): the thing a deployer authors, versions, and ships in the configuration package.

**Separation from `activity_ref`.** The envelope field `activity_ref` is CONTRACT (ADR-008 §S3) and stays CONTRACT. The envelope-field classification and the domain-object classification are orthogonal rows (ADR-008 §S4, reference-vs-referent rule). ADR-009 classifies the *instance*; ADR-008 classifies the *field that references the instance*.

**Why CONFIG not PRIMITIVE.** The platform does not ship activities. Deployers assemble them from platform-provided components (patterns, shapes, roles, scope types). An activity without deployer authorship does not exist on the platform. This is the definition of CONFIG in the ledger schema.

---

## Forbidden patterns

**F-C1: Never classify a mechanism as CONFIG or an instance as PRIMITIVE.** The mechanism/instance duality (§S1) is the binding rule. Collapsing either direction — treating a platform-fixed closure as a deployer knob, or treating a deployer-authored instance as a platform primitive — is a classification error. If a ledger row's classification appears to require one of these collapses, the concept is exhibiting the duality and must be split into two rows.

---

## Rejected alternatives

**Alt-1: Collapse `scope`, `pattern`, `activity` into the closest existing classifications without stating the duality rule.** Rejected. Three classification decisions taken in isolation solve the round-1 disputes but leave the latent rule unnamed; the next time a concept of the same shape surfaces, the same debate repeats. The disputes harvested from rounds 0 and 0.5 (nine DISPUTED rows, all sharing one structure) are evidence that the duality is a recurring shape on the platform, not a one-off.

**Alt-2: Scope the duality rule narrowly to `scope`, `pattern`, `activity` only.** Rejected. The rule is general by construction: its statement does not reference any of the three concepts. Narrowing the statement would produce a rule that requires amendment by new ADR every time a future concept exhibits the duality (role-action tables, flag-severity policies, pattern composition rules, future authorization dimensions). The authority cost of a narrow rule exceeds the authority cost of the general one.

**Alt-3: Partially supersede ADR-004.** Rejected. ADR-004 §S7 (scope-type closure) and §S9 (four-layer gradient) are each correct and each remain the canonical decision for what they decide. ADR-009's contribution is the explicit *rule* that makes the two sections consistent; it does not replace either. Re-citation is the correct relationship, consistent with the posture taken by ADR-006 and ADR-008.

**Alt-4: Defer the rule until a Phase 4 architecture rewrite.** Rejected. Phase 4 rewrites `architecture/` to conform to the charter. Deferring the duality rule would force Phase 4 to work from an implicit understanding of what PRIMITIVE and CONFIG mean in the presence of a closed mechanism — the exact ambiguity this ADR removes. The rule belongs in the charter before Phase 4 begins, not after.

---

## Consequences

### Supersessions

**None.** ADR-003 §S7, ADR-004 §S7, ADR-004 §S9, and ADR-005 §S5 are re-cited as the decision sites for their respective closures; ADR-009 names the unifying rule that connects them.

### Ledger updates (round 1)

| concept | was | becomes | settled-by | status |
|---|---|---|---|---|
| `scope` | DISPUTED | PRIMITIVE | ADR-009 §S2 | PROPOSED |
| `pattern` | DISPUTED | PRIMITIVE | ADR-009 §S3 | PROPOSED |
| `activity` | DISPUTED | CONFIG | ADR-009 §S4 | PROPOSED |

No new RESERVED rows. No new CONTRACT rows.

### Charter updates

- **Invariants** gain the duality rule as a new line (§S1).
- **Primitives** table gains `scope` and `pattern`.
- **Contracts** table is unchanged (ADR-008 already placed `activity_ref`).
- **Forbidden patterns** gains F-C1 at Phase 4 freeze.
- A concise statement of the duality rule is added to the cross-cutting rules section.

### Forward reference

**None.** The rule subsumes future concepts exhibiting the same duality; a new ADR is required only when a concept is claimed *not* to exhibit the duality (e.g., a mechanism that is itself deployer-authored).

### Round-1 completion

ADR-009 is the fourth and final ADR of Phase 2 round 1. With its landing:

- The nine DISPUTED rows surfaced by round 0 are all classified.
- The latent duality rule that explained the disputes is named and canonicalized.
- No round-1 ADR leaves a forward-reference that blocks round-1 closure.

Round-1 fixpoint check moves to the close-out commit (ledger rule 3 STABLE promotion for rows with no upstream churn).

---

## Traceability

| Subject | Source |
|---|---|
| Scope-type closure (3 types) | ADR-003 §S3, ADR-003 §S7, ADR-004 §S7 — first decisions; ADR-009 §S2 canonicalizes `scope` as PRIMITIVE |
| Four-layer gradient (activity at L0) | ADR-004 §S9 — first decision; ADR-009 §S4 canonicalizes `activity` as CONFIG |
| Pattern-registry closure | ADR-005 §S5 — first decision; ADR-009 §S3 canonicalizes `pattern` as PRIMITIVE |
| Reference-vs-referent orthogonality | ADR-008 §S4 — applied to `activity_ref` vs. `activity` in §S4 above |
| Duality rule (mechanism vs. instance) | ADR-009 §S1 — first explicit statement; implicit in ADR-003/004/005 above |
