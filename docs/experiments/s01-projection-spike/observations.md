# Projection Scaling Spike — Observations

> Run date: 2026-04-14
> Status: **Directionally validated — full confirmation during implementation on target device.**

---

## 1. What was tested

Combined projection fold as a device would compute it on form-open: entity state + workflow state + active flags + context.* properties.

### Capabilities exercised

| # | Capability | Architecture ref | In spike? | Notes |
|---|-----------|-----------------|-----------|-------|
| 1 | Entity state merge (`update` payloads) | [1-S2] | Yes | Payload fields merged into entity projection |
| 2 | Flagged event exclusion | [5-S2] | Yes | `flagged_events: Set[str]` — set-lookup per event, excluded from state derivation |
| 3 | Pattern-based state derivation | [5-S4], [C15] | Yes | Real state machine skeletons with named states, valid transitions keyed by `(state, type, shape_prefix)` |
| 4 | Multi-version shape routing | [C11], [4-S1] | Yes | 14 shape versions across 2 shape version generations |
| 5 | Alias resolution | [2-S6], [C7] | Yes | Eager transitive closure, single-hop lookup (3 aliases per subject) |
| 6 | Authority reconstruction | [3-S3], [C6] | Yes | Assignment-timeline scan (2 reassignments per subject) |
| 7 | Source-chain traversal | [5-S7] | Yes | `activity_ref` checked against known event set |
| 8 | Flag raise/resolve counting | [E3] | Yes | Active flag tracking through fold |
| 9 | `context.*` pre-resolution | [5-S8] | Yes | 7 platform-fixed properties computed post-fold |
| 10 | Realistic payload size | [4-S13] | Yes | 10–25 fields per event (avg ~13 fields, ~330 bytes) |

### Patterns tested

| Pattern | States | Transitions | Events tested at |
|---------|--------|-------------|-----------------|
| `case_management` | 4 (registered → active → monitoring → closed) | 6 | 100, 200, 500 |
| `capture_with_review` | 5 (open → under_review → accepted \| rejected → corrected) | 5 | 100 |

### Event composition

Events use the 6 platform-fixed types (`capture`, `review`, `alert`, `task_created`, `task_completed`, `assignment_changed`) with realistic distribution. Case management: ~50% followup captures, ~15% monitoring, ~10% alerts, ~10% tasks, ~8% reviews. Multiple actors per subject (primary, reviewer, supervisor). System actors for trigger-generated events.

---

## 2. Results

### Timing

| Scenario | Events | Mean | Median | P95 | P99 | µs/event |
|----------|--------|------|--------|-----|-----|----------|
| Case Management | 100 | 0.252 ms | 0.206 ms | 0.552 ms | 0.684 ms | 2.5 |
| Case Management | 200 | 0.456 ms | 0.415 ms | 0.765 ms | 1.123 ms | 2.3 |
| Case Management | 500 | 1.218 ms | 1.040 ms | 2.223 ms | 3.049 ms | 2.4 |
| Capture with Review | 100 | 0.208 ms | 0.174 ms | 0.395 ms | 0.591 ms | 2.1 |

5,000 iterations per scenario, 100-iteration warm-up. Python 3 on dev machine.

### Linearity

| Ratio | Measured | Ideal |
|-------|----------|-------|
| 200/100 | 1.81x | 2.0x |
| 500/100 | 4.83x | 5.0x |

Growth is linear. No super-linear blowup from any capability (set lookups, pattern matching, authority scanning). The slight sub-linearity at 200 events is expected (fixed overhead amortized over more events).

### Per-event cost

Stable at 2.1–2.5 µs/event across all scenarios and event counts. This includes:
- 1 alias resolution (hash lookup)
- 1 flagged-event set membership test
- 1 shape version dispatch
- 1 pattern state-machine evaluation (transition lookup)
- 1 payload merge (dict.update)
- 1 source-chain check
- 1 authority reconstruction (on assignment_changed events only)

### Projection output (500-event case management)

- Entity fields: 20 (accumulated from payload merges)
- Workflow state: `closed` (terminal state reached)
- Events excluded by flags: 44 (8.8%)
- Transition violations: 12 (expected — random event ordering doesn't always produce valid sequences)
- Shape versions seen: 10 (across v1/v2 of multiple shapes)
- context.* properties: all 7 computed

---

## 3. Assessment

### What this confirms

1. **Algorithmic cost is O(n) linear** — per-event cost is constant regardless of stream length.
2. **All 10 projection capabilities compose without blowup** — flagged-event exclusion, pattern routing, alias resolution, authority scanning, and context.* computation each add constant per-event overhead.
3. **Worst-case Python P99 (3.049 ms at 500 events) is 65x below the 200ms threshold** — even in the slowest measured language.
4. **Projected JVM P99 (~0.1–0.3 ms)** leaves a ~600–2000x margin against the 200ms threshold on comparable hardware.
5. **Payload size (~330 bytes/event, ~13 fields) is within the 60-field budget** without measurable GC pressure in the fold.

### What this does NOT confirm

1. **Absolute timing on target hardware** — Python on a dev machine ≠ Kotlin/JVM on an 8–16GB Android phone. The 200ms threshold is an implementation-phase test.
2. **Memory pressure at scale** — 500 events × 330 bytes ≈ 165KB of payload data. Modest, but the real test is JVM object overhead and GC behavior on Android.
3. **Concurrent projection** — the spike tests single-subject fold. A device might compute projections for multiple subjects during sync; contention is an implementation concern.
4. **Incremental projection** — the spike tests full rebuild. If implementation adds event-at-a-time incremental projection, the fold is even cheaper (amortized to a single event).

### Verdict

**Directionally validated.** The algorithmic cost of the combined projection fold is comfortably within budget at all tested scales (100–500 events). Growth is linear. No capability adds super-linear cost. The B→C escape hatch (materialized views) is not needed for algorithmic reasons — it remains available for constant-factor issues discovered during implementation on the target device.

The spike can be marked as **closed for algorithmic validation**. Full confirmation moves to implementation phase: Kotlin/JVM on Android, real device profiling, 200ms threshold test.

---

## 4. Capability coverage vs. previous spike

| Capability | Previous spike | Enhanced spike | Change |
|-----------|---------------|----------------|--------|
| Entity state merge | ✅ 2–3 fields | ✅ 10–25 fields | Realistic payloads |
| Workflow state transitions | ✅ Partial (string set) | ✅ Real state machine | Pattern-defined transitions |
| Flag raise/resolve | ✅ | ✅ | Unchanged |
| Source-chain traversal | ✅ Lightweight | ✅ Lightweight | Unchanged |
| Flagged event exclusion | ❌ | ✅ | NEW — set-lookup per event |
| Pattern-based state derivation | ❌ | ✅ | NEW — shape→pattern routing |
| Multi-version shape routing | ❌ | ✅ | NEW — version dispatch |
| Alias resolution | ❌ | ✅ | NEW — identity lookup |
| Authority reconstruction | ❌ | ✅ | NEW — assignment-timeline scan |
| `context.*` pre-resolution | ❌ | ✅ | NEW — 7 properties post-fold |
| Realistic payload size | ❌ (2–3 fields) | ✅ (10–25 fields) | NEW — ~330 bytes/event |
| Event counts | 100 only | 100, 200, 500 | Extended range |
| Event types | Non-standard | 6 platform-fixed types | Corrected vocabulary |

Previous spike coverage: 4/10 capabilities. Enhanced: 10/10.

---

## 5. Implications for implementation

1. **No preemptive materialized views needed** — start with pure event fold. Add views only if profiling on Android shows constant-factor problems.
2. **Incremental projection is an optimization, not a necessity** — full rebuild at 500 events takes ~1.2ms in Python. On JVM this will be sub-millisecond. Incremental mode is a nice-to-have for UX smoothness, not a correctness requirement.
3. **Pattern evaluation is cheap** — the state machine lookup (iterate transitions, match on state + type + shape prefix) is negligible cost. No need for precompiled transition tables unless profiling demands it.
4. **Alias resolution is negligible** — single hash lookup. The eager transitive closure strategy [2-S6] pays off here.
5. **Authority reconstruction scales with assignment count, not event count** — the timeline scan is bounded by the number of assignments (typically 2–5 per subject lifetime), not the event stream length.
