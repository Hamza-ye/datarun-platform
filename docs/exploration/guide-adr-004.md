# ADR-004 Exploration Guide: Configuration Boundary

> **For what was decided**: read [ADR-004](../adrs/adr-004-configuration-boundary.md).
> **For structural implications**: read [architecture/primitives.md](../architecture/primitives.md) (Shape Registry, Expression Evaluator, Deploy-time Validator, Config Packager, Pattern Registry), [architecture/contracts.md](../architecture/contracts.md) (C5, C11–C14, C19–C20).
> **Below**: how we got there — section-by-section navigation into the raw explorations.

---

## Source Documents

| Doc | Lines | What it covers |
|-----|------:|----------------|
| [13](archive/13-adr4-session1-scoping.md) | 561 | Decision surface, prior art, anti-patterns, four-layer gradient hypothesis |
| [14](archive/14-adr4-session2-scenario-walkthrough.md) | 1157 | 5 scenario walkthroughs (S00, S06, S09, S12, S20), full config stack |
| [15](archive/15-adr4-session3-part1-structural-coherence.md) | 923 | 6 structural coherence checks + addendum layer integrity checks |
| [16](archive/16-adr4-session3-part2-irreversibility-filter.md) | 469 | Position inventory, irreversibility classification, envelope impact |
| [17](archive/17-adr4-session3-part3-adversarial-stress-tests.md) | 505 | 3 full attacks + 3 light validations on envelope-touching positions |
| [18](archive/18-adr4-session3-part4-remaining-q-resolution.md) | 594 | 4 remaining questions resolved (Q7, Q9, Q10, Q12), final synthesis |

This is the largest exploration cluster (6 docs, ~4,200 lines). The four-layer gradient is the defining output.

---

## doc 13: Scoping — The Decision Surface

- **§1**: 8 sub-decisions ADR-4 must make — shape lifecycle, type vocabulary, expression boundary, pattern framework, activity composition, config delivery, budgets, deployer experience
- **§2**: Prior art deep-dive — DHIS2 metadata, CommCare XLSForm, ODK XForms, OpenSRP FHIR, Salesforce metadata+flow. Where each drew the configuration line and what broke.
- **§3**: Anti-pattern catalog — 6 named anti-patterns (Config-as-Code, Vocabulary Creep, Implicit Coupling, Version Coupling, Ghost Dependencies, Complexity Blind Spots). Used as a test suite throughout the cluster.
- **§4**: The constraint stack ADR-4 inherits from ADR-1/2/3 — 12 hard constraints that bound the configuration space
- **§5**: Permanent vs. evolvable decision map — first-pass irreversibility classification
- **§6**: **The four-layer gradient hypothesis** — the central insight. Layer 1 (shapes), Layer 2 (expressions), Layer 3 (triggers), Layer 4 (patterns). Each layer adds power; each has a hard budget.

**Unique value not in ADR**: The anti-pattern catalog — 6 named failure modes with examples from prior art. Referenced throughout the cluster as proof tests. The prior art analysis of where DHIS2/CommCare/Salesforce drew their configuration lines and what happened (§2 is 200+ lines of competitive intelligence not in the ADR).

---

## doc 14: Scenario Walkthroughs — The Longest Exploration Doc

5 scenarios fully walked through the configuration model, act by act:

- **§WT-1 (S00)**: Basic capture — shape definition, form rendering, event envelope composition. Proves Layer 1 works for the simple case.
- **§WT-2 (S06/S06b)**: Registry lifecycle + schema evolution — shape versioning, deprecation-only evolution, at-most-2-active-versions rule. Discovers the "old events under old shape remain valid" invariant.
- **§WT-3 (S09)**: Coordinated campaign — activity composition (shape + assignment + temporal scope). Discovers Layer 2 expressions for form-level computed fields.
- **§WT-4 (S12)**: Event-triggered actions — **the hardest walkthrough**. Discovers Layer 3 (triggers: L3a event-reaction, L3b deadline-check). Discovers the trigger DAG depth limit.
- **§WT-5 (S20)**: CHV field operations — end-to-end deployer experience composing shapes, activities, assignments, and expressions for a real workflow.
- **§6**: Cross-scenario synthesis — 15 positions emerge from the walkthroughs
- **§7**: Full configuration stack — the end-to-end deployer experience from shape authoring to device delivery
- **§8**: Primitives map update — Shape Registry, Expression Evaluator, Deploy-time Validator, Config Packager emerge as new primitives

**Unique value not in ADR**: The full walkthrough sequences (1,100+ lines) — step-by-step through each scenario showing how the deployer authors configuration, how the server validates, how the device receives and interprets it. The S12 trigger walkthrough that forced the L3a/L3b distinction. The deployer experience narrative (§7) that isn't in the ADR.

---

## doc 15: Structural Coherence — 6+6 Checks

Six primary coherence audits:

- **§(a)**: Artifact lifecycle — do shapes, activities, triggers all share one lifecycle model? **Yes, with nuance** — deprecation-only, at-most-2 active versions.
- **§(b)**: Dependency graph — acyclic? Consistent cascade rules? **Yes** — shapes are leaves, activities reference shapes, triggers reference activities.
- **§(c)**: Device vs. server evaluation — one consistent contract? **No** — server evaluates L3 (triggers), device evaluates L2 (form expressions). The split is intentional and clean.
- **§(d)**: Expression language — one language or many? **One** — same grammar, different available scopes per context.
- **§(e)**: Pattern framework — coherent concept or fuzzy? **Coherent** — patterns are parameterized state-machine skeletons that generate activities+triggers.
- **§(f)**: Envelope composition — complete, redundant, or gapped? Verifies ADR-4 adds exactly `shape_ref` to the envelope.

Addendum (§Addendum): 6 additional layer-integrity checks verifying anti-patterns AP-1 through AP-6 are not violated.

**Unique value not in ADR**: The detailed reasoning behind the device/server evaluation split (§c). The expression language unification argument (§d). The anti-pattern boundary checks proving none of the 6 named anti-patterns are triggered.

---

## doc 16: Irreversibility Filter — What's Actually Permanent

- **§Inventory**: All 40+ ADR-4 positions cataloged in one table
- **§Filter**: Each position classified — Category A (envelope-touching, 1 position: `shape_ref`), B (protocol-level, ~5), C (server-side, ~25), D (policy, ~10)
- **§Bulk Classification**: Categories B–D don't need full stress testing — they're evolvable
- **§Envelope Impact**: ADR-4 adds exactly ONE new envelope field: `shape_ref` (shape name + version). This is the only irreversible addition.
- **§Stress Test Scope**: Only Category A gets full adversarial testing. B–D get light validation.

**Unique value not in ADR**: The explicit enumeration of all 40+ positions and their irreversibility classification — which the ADR distills into sub-decisions but doesn't show the classification reasoning for each.

---

## doc 17: Adversarial Stress Tests — 3 Attacks + 3 Light Validations

- **Attack 1 (Multi-Activity Shape Collision)**: Two activities reuse the same shape with different expression contexts. Proves shapes must be activity-independent; expressions bind at the activity level.
- **Attack 2 (Breaking Schema Change)**: Tests whether deprecation-only evolution can be circumvented. Proves the invariant holds — no migration path exists by design.
- **Attack 3 (Type Vocabulary Exhaustion)**: Tests whether the 7 fixed event types can be exhausted. Proves they can't — the types are semantic categories, not per-use-case enumeration.
- **V1 (Complexity Budget)**: 60-field-per-shape budget — validated as sufficient for all scenarios without hitting performance limits.
- **V2 (Expression Scope)**: One entity reference per expression — validates the 3-predicate ceiling.
- **V3 (Trigger DAG)**: Max path length 2 — validates DAG depth limit prevents infinite trigger chains.

**Unique value not in ADR**: The attack sequences — especially Attack 1's discovery of why expressions bind at activity level (not shape level). The concrete validation of the hard budgets (60 fields, 3 predicates, DAG depth 2).

---

## doc 18: Remaining Questions — Q7, Q9, Q10, Q12

- **§Q7**: Domain-specific conflict rule configuration — can deployers configure what constitutes a conflict? **Answer**: Yes, constrained to pattern-level conflict definitions with the 9-category flag catalog as the vocabulary.
- **§Q9**: Per-flag-type severity configuration — can deployers adjust flag severity? **Answer**: Yes, within the auto-resolution policy framework. Severity affects resolution priority, not detection.
- **§Q10**: Scope type extensibility — can deployers define new scope types beyond the initial 3? **Answer**: No. 3 scope types are platform-fixed (geographic, organizational, programmatic). Custom scopes are dangerous for the authorization invariants.
- **§Q12**: Sensitive-subject classification — how do deployers mark subjects as sensitive? **Answer**: Via shape-level metadata that flows to sync scope computation.
- **§Carry-Forward**: Integrates all Part 3 findings back into the position inventory.
- **§Final Synthesis**: The complete ADR-4 decision surface after all 6 sessions.

**Unique value not in ADR**: The Q10 reasoning for why scope types are not extensible (§Q10 has ~100 lines of analysis). The sensitive-subject classification design that feeds into ADR-003's sync scope.
