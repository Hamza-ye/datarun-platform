# ADR-003 Exploration Guide: Authorization & Selective Sync

> **For what was decided**: read [ADR-003](../adrs/adr-003-authorization-sync.md) — 7 sub-decisions (S1–S7).
> **For structural implications**: read [architecture/primitives.md](../architecture/primitives.md) (Scope Resolver), [architecture/cross-cutting.md](../architecture/cross-cutting.md) (accept-and-flag extensions).
> **Below**: how we got there — section-by-section navigation into the raw explorations.

---

## Source Documents

| Doc | Lines | What it covers |
|-----|------:|----------------|
| [10](archive/10-adr3-phase1-policy-scenarios.md) | 684 | 6 scenario groups walked through act-by-act with policy enforcement |
| [11](archive/11-adr3-phase2-stress-test.md) | 822 | 5 mechanisms, 20 findings, 4 combination scenarios |
| [12](archive/12-adr3-course-correction.md) | 244 | Irreversibility filter — most of stress test was overkill |

---

## doc 10: Policy Enforcement Scenarios

Six scenario groups tested, each with multi-act walkthroughs:

- **§2 — S20+S21 (Simplicity Anchor)**: CHV records offline → looks up history → supervisor reviews → both sync. Proves the authorization model must not over-complicate the basic offline workflow.
- **§3 — S03 (Designated Responsibility)**: Assignment, offline reassignment, events in old scope, scope handoff to new worker. Discovers the "accept events from old scope, flag for review" pattern.
- **§4 — S09 (Coordinated Campaign)**: Time-windowed campaign grants, offline expiry, post-expiry events. Discovers temporal authority grants as separate from standing assignments.
- **§5 — S14 (Multi-Level Distribution)**: Multi-hop transfers across authorization boundaries. Discovers cross-scope event visibility requirements.

Key synthesis:
- **13 Hot Spots** identified for stress testing — one per act where authorization interacts with offline/identity in non-obvious ways
- **Assignment model** emerged as the central abstraction — temporal binding of actor to scope

**Unique value not in ADR**: The detailed per-act walkthroughs showing how each hot spot was discovered. The progression from simple (S20) to complex (S14) that proves the model scales. The 13 hot spots that became the stress test's attack surface.

---

## doc 11: Stress Test — 5 Mechanisms, 20 Findings

Five mechanisms tested (A–E), with combination scenarios:

### Mechanism A: Access Rule Representation (A1–A5)
- **A1**: Assignment model can't express all 13 hot spots without extensions — needs temporal grants, campaign scoping
- **A3**: **Critical** — misconfigured coordinator can escalate scope beyond their authority. Proves server-side scope-containment invariant needed (became [3-S5])
- **A5**: Device-side role enforcement is bypassable on rooted Android — proves enforcement must be server-side, device is advisory

### Mechanism B: Sync Scope Determination (B1–B5)
- **B3**: First-sync bandwidth on 2G is prohibitive without mitigation — proves priority-based sync needed
- **B4**: **Critical** — data removal on scope contraction. All three options (purge/retain-hide/retain-indefinitely) have problems. Proves selective-retain with crash-safe purge needed

### Mechanism C: Stale Rule Reconciliation (C1–C5)
- **C2**: **Critical** — RoleStaleFlag on clinical actions needs pre-policy interception, not post-flag review. Extends detect-before-act to authorization (became [3-S7])
- **C5**: Accept-and-flag insufficient for sensitive-subject data exposure — proves data-sensitivity classification needed

### Mechanism D: Projection Location (D1–D4)
- **D1**: Supervisor projection rebuild is prohibitive without incremental updates
- **D4**: Offline supervisor assessment on stale data — operationally dangerous but mechanically sound

### Mechanism E: Authority Context in Envelope (E1–E5)
- **E1–E2**: Envelope size manageable but variable-length assignment refs need schema commitment
- **E3**: Authority context is an assertion, not verified fact — this is correct but must be explicit

### Combination Scenarios (α–δ)
- **α**: Assignment transfer + identity merge + offline worker — compound authorization/identity conflict
- **β**: Campaign expiry + device sharing + offline observations
- **δ**: Auditor access + cross-hierarchy scope — tests read-only authority model

**Unique value not in ADR**: The 20 individual attack paths. The combination scenarios that stress multiple mechanisms simultaneously. The finding that data-sensitivity classification is needed for scope contraction (B4/C5). The proof that device-side enforcement is advisory at best (A5).

---

## doc 12: Course Correction — The Irreversibility Filter

This is the most important document in the ADR-003 cluster. It discovered that most of the stress test was overkill because authorization is mostly server-side logic:

- **§Irreversibility Filter**: Only ONE thing touches the event envelope — authority_context. Everything else is sync protocol, projection, or policy — all changeable.
- **§Envelope Question**: Three options for authority_context in envelope. Option (c) — no authority_context in envelope, derive on sync — was chosen. **This means ADR-003 adds zero new envelope fields.** The least irreversible ADR.
- **§Stress Test Impact**: Reclassifies all 20 findings:
  - 4 become ADR-003 constraints (scope-containment, alias-respects-scope, resolution-online-only, detect-before-act extension)
  - Most become strategies (server-side, evolvable)
  - Several are overcalls — legitimate mechanisms that don't need constraint-level locking
- **§Evolvability Analysis**: Each of the 7 sub-decisions checked for evolvability — all except S4 (alias scope rule) can evolve without migration

**Unique value not in ADR**: The irreversibility filter reasoning — why ADR-003 commits less than ADR-001/002. The explicit overcall identification. The decision to NOT put authority_context in the envelope (and why that's the right call even though it makes sync-time computation slightly harder).
