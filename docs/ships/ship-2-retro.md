# Ship-2 Retro — Registry Lifecycle & Merge/Split (S06)

> **Tag target**: `ship-2`
> **Opened**: 2026-04-25
> **Closed (implementation)**: 2026-04-26
> **Spec**: [`ship-2.md`](ship-2.md)
> **Parent**: [`ship-1b-retro.md`](ship-1b-retro.md) — `ship-1` and `ship-1b` tags do not move.
> **Scenarios shipped**: [S06](../scenarios/06-entity-registry-lifecycle.md) (identity half — registry lifecycle + merge/split) under [S19](../scenarios/19-offline-capture-and-sync.md).

---

## 1. Walkthroughs that passed

Three new walkthroughs added in `Ship2WalkthroughAcceptanceTest` (sibling of `WalkthroughAcceptanceTest`, Ship-1 precedent). All three pass against the real HTTP surface; Ship-1's five prior walkthroughs are untouched and remain green. Suite total: **33/33** (`./mvnw test`, `Tests run: 33, Failures: 0, Errors: 0`).

| W | Method | ADR §S exercised | Commit |
|---|---|---|---|
| W-3 | [`walkthrough_W3_reactive_merge_after_identity_conflict`](../../server/src/test/java/dev/datarun/ship1/acceptance/Ship2WalkthroughAcceptanceTest.java) | ADR-002 §S6 (alias-in-projection, eager closure), §S13 (raw refs) | `fe6cb28` |
| W-4 | [`walkthrough_W4_wrong_merge_correction_via_multi_successor_split`](../../server/src/test/java/dev/datarun/ship1/acceptance/Ship2WalkthroughAcceptanceTest.java) | ADR-002 §S7 (no `SubjectsUnmerged`, corrective split), §S8 (split freezes history); arity edit on `subject_split.schema.json` | `fe6cb28` |
| W-5 | [`walkthrough_W5_lineage_dag_enforcement`](../../server/src/test/java/dev/datarun/ship1/acceptance/Ship2WalkthroughAcceptanceTest.java) | ADR-002 §S9 (DAG by construction); pre-write 4xx, distinguished from ADR-002 §S14 accept-and-flag | `fe6cb28` |

Setup strategy chosen: **Strategy B (single-CHV, clean)** per spec §6.4 — no `scope_violation` noise to filter. Documented in test class comment.

---

## 2. §7 retro criteria — evidence

Every numbered criterion from spec §7 mechanically verified against HEAD.

| # | Criterion | Status | Evidence |
|---|---|---|---|
| 1 | W-3 + W-4 + W-5 pass via HTTP, mirroring `WalkthroughAcceptanceTest` | ✅ | `./mvnw test` → `Tests run: 33, Failures: 0, Errors: 0`. Sibling class `Ship2WalkthroughAcceptanceTest` (commit `fe6cb28`); Ship-1's 5 walkthroughs (W-0 + W-1 + W-2 + M1 + M2) untouched and green. |
| 2 | Every Ship-2 commit cites at least one of `S06` / `S19` | ✅ | `git log --oneline ship-1b..HEAD` — every subject line carries `S06` (one `chore: ignore android gradle build output` `a966cce` is build-tooling hygiene, no scenario; one `chore(orchestrator): …` `a54e60b` is meta-work, no `(ship-2)` scope by design). All Ship-2 scope-tagged commits cite S06. |
| 3 | **FP-002 gate met (option a)**: no `subject_lifecycle` table; no source reads it as state | ✅ | `git diff ship-1b..HEAD -- server/src/main/resources/db/migration/` → 0 lines (migration tree untouched). `git grep -l subject_lifecycle server/` → 0 matches. Read paths project on demand: [`SubjectAliasProjector`](../../server/src/main/java/dev/datarun/ship1/admin/SubjectAliasProjector.java) (commit `17461d9`) and [`SubjectLifecycleProjector`](../../server/src/main/java/dev/datarun/ship1/admin/SubjectLifecycleProjector.java) replay events per request — no cache, the [`ScopeResolver`](../../server/src/main/java/dev/datarun/ship1/scope/ScopeResolver.java) precedent. |
| 4 | **Contract diff scoped**: envelope unchanged; only `subject_split.schema.json` arity edit; **FP-007 gate met** | ✅ | `git diff --stat ship-1b..HEAD -- contracts/envelope.schema.json` → 0 lines. `git diff --stat ship-1b..HEAD -- contracts/shapes/` → only `subject_split.schema.json` (12 ins / 6 del; commits `f7f0e8a` + `48049e2`). `bash scripts/check-convergence.sh` → **PASS**, `[4/4] … shape trees byte-identical`. Drift gate landed at `5cbb183`. |
| 5 | Coordinator actor present + token gates merge/split + `actor_ref` is human UUID, never `system:` | ✅ | `DevBootstrapController` seeds the coordinator actor + token + `assignment_created/v1` with `role="coordinator"` (commits `da661d7`, `691dc91`). `CoordinatorAuthInterceptor` (commit `3b92e50`) gates `/admin/subjects/**`. [`AdminSubjectsController.java:163`](../../server/src/main/java/dev/datarun/ship1/admin/AdminSubjectsController.java#L163) writes `actor_id = <coordinator UUID>` from the bearer token. The W-3/W-4 SQL assertions check `actor_id` equals the coordinator actor UUID, never the `system:` form (F-B4). |
| 6 | Envelope `type=capture` on every persisted merge/split event | ✅ | The only literal `"capture"` in the merge/split write path is [`AdminSubjectsController.java:163`](../../server/src/main/java/dev/datarun/ship1/admin/AdminSubjectsController.java#L163) (`grep -n '"capture"' …` returns exactly that line). W-3 / W-4 / W-5 each assert `splitRow.get("type") == "capture"` and `mergeRow.get("type") == "capture"` directly from `events`. No envelope-`type` vocabulary extension (F2); no integrity logic branches on `type` (F-A2). |
| 7 | Server-authored envelope fields per ADR-002 §S1/§S4/§S5 | ✅ | [`ServerEmission.java:20`](../../server/src/main/java/dev/datarun/ship1/event/ServerEmission.java#L20) defines the single reserved `SERVER_DEVICE_ID = 00000000-0000-0000-0000-000000000001`. Both consumers share it: [`ConflictDetector.java:40`](../../server/src/main/java/dev/datarun/ship1/integrity/ConflictDetector.java#L40) (Ship-1) and [`AdminSubjectsController.java:50`](../../server/src/main/java/dev/datarun/ship1/admin/AdminSubjectsController.java#L50) (Ship-2). `device_seq` drawn from `server_device_seq` (`ServerEmission#nextServerDeviceSeq`). W-4 SQL asserts `device_id = 00000000-0000-0000-0000-000000000001`. Refactor commit `8ca6675`. |
| 8 | No new ADR drafted unless R1–R6 / §3.2 triggered one | ✅ | No ADR drafted. R1–R6 assessment §3 below; §3.2 §4 below. None promoted to architectural. |
| 9 | Ledger rows for `subject_lifecycle`, `alias_projection`, `coordinator` | ✅ | Three new rows in [`docs/convergence/concept-ledger.md`](../convergence/concept-ledger.md): `subject-lifecycle` (DERIVED, ADR-001 §S2; cache declined per FP-002 (a)), `alias-projection` (DERIVED, ADR-002 §S6), `coordinator` (CONFIG, ADR-009 §S1). Ledger close-out summary updated; counts `STABLE 251 → 254`, total `269 → 272`. |
| 10 | Charter regenerated; drift gate PASS | ✅ | [`docs/charter.md`](../charter.md) §Status regenerated from ledger. `bash scripts/check-convergence.sh` exit 0; checks 1–4 all PASS. |
| 11 | Retro note filed | ✅ | This file. Covers walkthroughs (§1), criteria (§2), R1–R6 + §3.2 (§3 + §4), implementation-grade choices (§5), FPs (§6), ledger deltas (§7), Ship-3 handoff (§8), OQ-3 status (§9), cosmetic notes (§10). |
| 12 | `ship-2` tag applied; `ship-1` / `ship-1b` unmoved | ✅ | `git tag ship-2` applied locally at retro close. Tag list verification: `git tag --sort=-creatordate | head` shows `ship-2` ahead of `ship-1b` ahead of `ship-1`. Origin reconciliation deferred to user decision; tag is local-only. |

---

## 3. §3.1 risk assessment — R1–R6

| Risk | Observed? | Why / evidence |
|---|---|---|
| **R1** — Alias projection correctness under eager transitive closure | **Exercised at single-hop scale** | W-3 explicitly merges `S_X → S_Y`, then `S_Y → S_Z`, and asserts the canonical resolution `S_X → S_Z` is single-hop (eager closure per ADR-002 §S6). [`SubjectAliasProjector`](../../server/src/main/java/dev/datarun/ship1/admin/SubjectAliasProjector.java) walks the closure on every read (no cache). Not stressed under concurrent merges (Ship-2 runs single-coordinator on a single JVM); R1's contradiction-on-rebuild case is not constructible in this slice. Carries forward to the first multi-resolver Ship (likely Ship-5). **No supersession.** |
| **R2** — Lineage DAG enforcement under race | **Not observed** | Spec §3.1 was downgraded mid-session (commit `beae77d`, Spec-conformance review): §S10 online-only-server-validated-transaction is the intended guard, and Ship-2's single-coordinator on single-JVM means the in-practice guard is single-writer + Postgres MVCC + PK/UNIQUE. R2 carries forward to Ship-5 multi-resolver. **No supersession.** |
| **R3** — Post-archive accept-and-flag discipline | **Not observable in slice** | Ship-1's CHV flow generates fresh `subject_id` per capture and never references existing subjects by UUID. The flow that would reference an archived UUID does not exist until Ship-3 / Ship-4 introduces subject pick-lists. The `stale_reference` flag category remains in the catalog (charter §Flag catalog #2) and is unemittted in Ship-2. **No supersession.** |
| **R4** — Projection rebuild cost | **Not observed** | FP-002 (a) declines the cache; there is no rebuild procedure to time, and Ship-2's fixture (3 walkthroughs × ~15 events each) is below any rebuild-cost threshold. Read paths re-project per request. R4 reopens if a future Ship adopts the cache (escape hatch B→C remains available). **No supersession.** |
| **R5** — Subject-lifecycle storage discipline (FP-002) | **Closed at spec time, gate met at retro** | FP-002 RESOLVED option (a). See §6 below + spec §7 criterion 3. **No supersession.** |
| **R6** — Conflict Detector alias-blindness | **Not stressed** | ADR-002 §S13 governs how the detector treats events that reference *retired* IDs (raw refs, not alias-resolved). Ship-1's `identity_conflict` heuristic is name+village (no UUID reference), so alias resolution is irrelevant to detection in this slice. Same trigger Ship as R3. **No supersession.** |

No ADR-002 §S moved from STABLE to DISPUTED. No ADR drafted.

---

## 4. §3.2 domain-realism observations

Spec §3.2 named four watch-categories pre-build. Honest reporting:

- **Coordinator workflow ergonomics** — *not observed in this slice.* The merge/split endpoints are scripted via HTTP from the test class; no human coordinator operated them, and no UI was built (per §6.5). The first real-coordinator workflow is a Ship-5 concern (judgment / approvals UX).
- **Operator confusion between "duplicate registration" and "subject evolution"** — *not observed in this slice.* Ship-2 has only one trigger path (CHV duplicate-name capture → `identity_conflict` flag → coordinator merge). "Subject evolution" as a distinct platform response does not exist until Ship-3 (shape evolution) or Ship-4 (case management). The confusion surface is empty.
- **Merge/split audit trail completeness** — *partially observable.* W-3 and W-4 demonstrate that `subjects_merged/v1` and `subject_split/v1` events carry `actor_id = coordinator UUID` and `device_id = SERVER_DEVICE_ID`, so "who" and "from which surface" are reconstructable from the event. **Not** captured: free-text "why" on the merge/split request body (the endpoints accept only `surviving_id`/`retired_id` or `source_id`/`successor_ids`). This is intentional per §6.5 (no audit-reason field promised) but is an observation Ship-3/4/5 should weigh when shape evolution / case management lands. Not an FP today; recorded here as a domain note.
- **Post-corrective-split coordinator workflow** — *empty-successor invariant confirmed mechanically.* W-4 asserts that immediately after split, `yPrime` and `xPrime` have zero captures attributed to them; the original captures stay with `S_X` and (archived) `S_Y`. This is the correct outcome under ADR-002 §S8 + immutability. **What a coordinator does next** in operational reality (bootstrap-capture, UI affordance pointing to archived-source history) is unbuilt and untested in Ship-2. Same forwarding as FP-006: lands when the device flow first references existing subjects by UUID. Not promoted to a new FP — FP-006 already covers it.

No new FPs opened from §3.2.

---

## 5. Implementation-grade choices made during build (Frame-2)

Choices the spec deliberately deferred to retro. Each is **explicitly not architectural** — Ship-3 may revisit any of them.

### 5.1 Coordinator endpoints under `/admin/subjects/**` (vs. parallel `/coordinator/...`)

Endpoints landed at `POST /admin/subjects/merge` and `POST /admin/subjects/split` (commit `dce4130`). Reasoning: the coordinator surface is administrative (server-addressed, online-only, bearer-token authed) and the existing `/admin/**` namespace already houses Ship-1's Thymeleaf admin. A parallel `/coordinator/...` namespace would have introduced a second auth interceptor and a second Spring `@Controller` for one slice. Future-compat: if Ship-5's `designated_resolver` lands as a non-coordinator role, the namespace is wide enough to absorb it without rename.

### 5.2 Bootstrap strategy: Strategy B (single-CHV, clean) for walkthroughs

Spec §6.4 offered Strategy A (cross-CHV, dual-flag) and Strategy B (single-CHV, clean). Picked B at test authorship time (commit `fe6cb28`). Reasoning: W-3/W-4/W-5 assert on merge/split semantics, not on `scope_violation` ⊕ `identity_conflict` co-emission. Strategy A would have introduced an incidental `scope_violation` flag that every walkthrough then has to filter past, costing assertion clarity for zero added coverage. Strategy A is exercised by Ship-1's W-1/W-2 already.

### 5.3 HTTP surface for alias resolution: `GET /admin/subjects/{id}/canonical` (Shape A)

`SubjectAliasCanonicalEndpointTest` exercises the explicit Shape-A endpoint that returns the surviving subject's UUID for a given (possibly retired) subject UUID. Considered: Shape B (a query-flag like `?resolve_alias=true` on existing `/admin/events` reads that quietly rewrites `subject_id` references to canonical). Picked A. Reasoning: ADR-002 §S13 is "Conflict Detector uses raw references; alias resolution happens only in the projection layer **after** detection". A query-flag rewrite would couple alias resolution into every read path's URL surface and risk a future caller forgetting the flag and getting the *wrong* answer (raw when canonical was wanted, or vice versa). Shape A keeps "raw, not rewritten" mechanically — `/admin/events` continues to return events with their original `subject_id`; callers who want canonical resolution opt in by URL.

### 5.4 `ServerEmission` extraction (commit `8ca6675`)

Ship-1's `ConflictDetector` had inlined the `SERVER_DEVICE_ID` constant and the `server_device_seq` advance. Ship-2's `AdminSubjectsController` would have copied both. Refactored ahead of the `AdminSubjectsController` commit (`dce4130` follows `8ca6675`) to give both consumers one source. The single reserved server-device UUID and the durable Postgres sequence are now provably shared (criterion 7 evidence). No second reserved UUID introduced; no parallel sequence.

### 5.5 `actor_tokens.kind` — not introduced

The coordinator actor's token sits in the same `actor_tokens` table as CHV tokens (commit `da661d7`). No `kind` discriminator column. Authority is reconstructed by `ScopeResolver.hasRoleAt(actorId, role, instant)` projecting `assignment_created/v1` events with `role="coordinator"` (commit `ecf3ece`). Reasoning: introducing a column on `actor_tokens` would have been the second authority surface in the system; ADR-009 §S1 + ship-2 §6 commitment 1 commit to authority-as-projection. Future-compat: Ship-5 `designated_resolver` adds scope dimensions on the assignment row, never a column on the token row.

### 5.6 `subject_split` empty-array rejection: pre-validation, not accept-and-flag

The schema arity edit (`successor_ids: array, minItems: 2`, commit `48049e2`) makes empty arrays a structural-validation reject. Considered: accept the empty-successor split and emit a `conflict_detected/v1` flag. Rejected per ADR-002 §S9 (DAG by construction is a pre-write rule, not a state-anomaly rule) and the W-5 distinction in spec §6.4 (4xx structural error, no event written). The W-4 test explicitly asserts `eventsBefore == eventsAfter` on the empty-array attempt.

---

## 6. FPs touched

| FP | Status at close | Action |
|---|---|---|
| **FP-001** — `role_stale` / scope-from-cache | OPEN | No Ship-2 interaction (S06 is identity, not authority). Next re-evaluation at the first role-action enforcement Ship (likely Ship-5). |
| **FP-002** — `subject_lifecycle` read-discipline | **RESOLVED 2026-04-26 (option a)** | Resolution log entry added to [`flagged-positions.md`](../flagged-positions.md). Three-part gate met: (1) no migration touch (`git diff` empty), (2) no source matches (`git grep` empty), (3) reads project from events on demand via `SubjectAliasProjector` (commit `17461d9`) and `ScopeResolver.hasRoleAt` (commit `ecf3ece`). |
| **FP-004** — `assignment_ref` envelope field | OPEN | No Ship-2 interaction. |
| **FP-005** — Corrections surface unassigned | OPEN | No Ship-2 interaction. Ship-2 reconciles two subjects (merge); corrections reconcile two captures of the same subject — distinct concern. |
| **FP-006** — S7↔S8 attribution under corrective split | OPEN (status check 2026-04-26) | Empty-successor invariant confirmed in W-4 mechanically (yPrime / xPrime have zero captures; original captures stay attributed to `S_X` and archived `S_Y`). The offline-capture-against-archived-source path is structurally unconstructible in Ship-1's CHV flow, so the seam is not exercised. Carries to Ship-3 (if shape evolution introduces UUID-referenced flows) or Ship-4 (case management). Resolution log entry dated 2026-04-26 added to [`flagged-positions.md`](../flagged-positions.md). |
| **FP-007** — Contract↔server-resource shape drift | **RESOLVED 2026-04-26** | Resolution log entry added. Drift-gate check 4 landed at `5cbb183` (ahead of the schema arity edits at `f7f0e8a` / `48049e2`); gate observed PASS at Ship-2 close (`bash scripts/check-convergence.sh` → exit 0, `[4/4] … shape trees byte-identical`). |
| **FP-008** — `conflict_detected` lacks root_cause trace | OPEN per OQ-5 path (c) (status check 2026-04-26) | Ship-2 emitted no `conflict_detected/v1` events whose `source_event_id` differs from the trigger event. Early flag corpus stays trace-clean. Resolution log entry dated 2026-04-26 added. |

No new FPs opened. RFS-1 (naïve identity heuristic) was operationally stress-tested by Ship-2's setup and the merge correction path held — name+village false-positives are correctable by coordinator merge with no false-negatives surfacing in the slice.

---

## 7. Ledger deltas

Three new rows added in [`docs/convergence/concept-ledger.md`](../convergence/concept-ledger.md). Counts: **STABLE 251 → 254, total 269 → 272**. No pre-existing row changed classification (no history-line mutations elsewhere). Ship-2 close-out summary appended to the round-2 epilogue.

| Concept | Classification | Settled-by | Status | Notes |
|---|---|---|---|---|
| `subject-lifecycle` | DERIVED | ADR-001 §S2 | STABLE | **Cache intentionally declined** per FP-002 (a). Replays `subjects_merged/v1` / `subject_split/v1` on demand (`ScopeResolver` precedent). No `subject_lifecycle` table. ADR-001 §S2 escape hatch B→C remains available if a future Ship surfaces read cost. The closest conventional ledger status is DERIVED (no "DECLINED-CACHE" slot exists); the cache-declination decision is recorded in the row's note. |
| `alias-projection` | DERIVED | ADR-002 §S6 | STABLE | First materialisation in live flow. `SubjectAliasProjector` rebuilds eagerly per request from events; eager transitive closure (A→B, B→C ⇒ A→C single-hop). Distinct from the abstract `merge-alias-projection` strategy row (ALGORITHM, pre-existing). |
| `coordinator` | CONFIG | ADR-009 §S1 | STABLE | Deployer-authored actor instance (one human UUID + bearer token, seeded by `DevBootstrapController`). Recognition is the existing `assignment-changed` PRIMITIVE (`role="coordinator"`) — **no parallel mechanism row** per ADR-009 §S1 duality (F-C1). Future-compat: Ship-5 `designated_resolver` lands as scope dimensions on this row. |

History entries: each new row carries a `Ship-2 (2026-04-26): CREATED` line in the `history` column per ledger rule 1.

---

## 8. Hand-off to Ship-3

Recorded in [`ship-2.md` §8](ship-2.md#8-hand-off-to-ship-3). Highlights:

- **Live R-N risks**: R1 exercised at single-hop, not under concurrency; R2 / R3 / R6 unchanged; R4 not observable; R5 RESOLVED.
- **FP-002** RESOLVED, **FP-007** RESOLVED. **FP-006** + **FP-008** carry; **FP-001** + **FP-004** + **FP-005** unchanged.
- **Alias-projection rebuild procedure**: read-time replay, no persistent cache. Ship-3's shape-evolution work inherits this pattern.
- **Multi-successor split arity** (`successor_ids: array, minItems: 2`) is now contract; Ship-3 must not re-narrow.
- **Coordinator authority via projection** is the model; Ship-5 extensions add scope dimensions on the assignment row, never a parallel surface.

Ship-3 next action per spec: **draft Ship-3 spec (S06b shape evolution)**. R-4 sweep required: FP-001, FP-004, FP-005, FP-006, FP-008 — at least three of these (FP-006 / FP-008 most prominently) plausibly become live in Ship-3.

---

## 9. OQ-3 status

Spec §9 OQ-3 (broken-link cleanup side-quest) is **RESOLVED 2026-04-26**, fixed at commit `31c6bf9` ahead of any code change in the Ship-2 build cycle. Spec §9 amended in this same retro authorship sequence to mark RESOLVED with the commit cite. The broken link survives in the tagged `ship-1` / `ship-1b` commits (tags do not move); `main` is correct from `31c6bf9` forward.

---

## 10. Cosmetic notes

Not fix-obligations; recorded for the next Ship that touches the relevant code.

- **`SubjectAliasProjector#aliasChainLength` docstring nit.** The method walks raw alias edges to count hops; the docstring asserts "not chain-walked at query time" which is correct only for `canonicalId` (the single-hop method, post-eager-closure). Either scope the doc claim to `canonicalId` or reimplement `aliasChainLength` over the closure. Surfaced during the spec-conformance re-review; not promoted to FP because eager closure makes the divergence semantically invisible to callers (both methods agree on "is this an alias?"). Ship-3 can pick it up if it touches the projector.
- **`Ship Orchestrator` skill carries a `chore(orchestrator): ...` commit (`a54e60b`)** outside Ship-2's `(ship-2)` scope. By design (it's meta-work on the orchestrator's per-Ship loop, not S06 work). Recorded so a `git log --oneline` reader does not mistake it for a missing scenario cite.

---

## 11. Session journal — mechanics

- **Build**: `./mvnw test` → `Tests run: 33, Failures: 0, Errors: 0` against the running test database (Postgres 16 on `localhost:15432`).
- **Drift gate**: `bash scripts/check-convergence.sh` → exit 0 on first run after the retro + ledger updates landed. Check 4 (shape parity, FP-007's gate) verified byte-identical after the schema arity edits in lock-step.
- **Commit hygiene**: a single sweep commit (`871a6b8`) was split into three at retro start (`a54e60b` orchestrator skill, `beae77d` spec amendments, `fe6cb28` walkthrough tests) so the Ship-2 commit log carries one concern per commit.
- **Tag**: `git tag ship-2` applied locally; `ship-1` / `ship-1b` unmoved. Origin reconciliation is deferred to user decision.
