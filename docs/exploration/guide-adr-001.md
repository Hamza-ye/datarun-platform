# ADR-001 Exploration Guide: Offline Data Model

> **For what was decided**: read [ADR-001](../adrs/adr-001-offline-data-model.md).
> **For structural implications**: read [architecture/primitives.md](../architecture/primitives.md) (Event Store, Projection Engine).
> **Below**: how we got there — section-by-section navigation into the raw explorations.

---

## Source Documents

| Doc | Lines | What it covers |
|-----|------:|----------------|
| [01](archive/01-architecture-landscape.md) | 368 | Prior art survey, architecture families, coupling analysis |
| [02](archive/02-adr1-offline-data-model.md) | 432 | Three options (snapshots / events / action log), scenario stress tests |
| [03](archive/03-adr1-forward-projection.md) | 278 | How each option shapes ADRs 2–5, irreversibility analysis |
| [04](archive/04-decision-audit.md) | 360 | Scoping audit — what ADR-001 was over-deciding, what was missing |

---

## doc 01: Architecture Landscape

- **§1**: Constraint filter — what the five hard constraints eliminate (thin clients, mutable state, full-dataset sync, fixed vocabularies)
- **§2**: Prior art deep-dives — DHIS2, CommCare, ODK, OpenSRP. Where each hit walls and what Datarun does differently
- **§3**: Three surviving architecture families (Metadata Engine, Event-Sourced, Composable Primitives) — comparison matrix + the hybrid insight
- **§4**: Primitive realization matrix — how all 12 viability primitives map to each family
- **§5**: Coupling analysis — why offline data model is the root of the dependency tree
- **§6**: Decision sequence rationale — why ADR-1→2→3→4→5 in that order

**Unique value not in ADR**: The prior art analysis (§2) — detailed lessons from DHIS2's metadata walls, CommCare's form-as-everything ceiling, ODK's lack of case management, OpenSRP's FHIR lock-in. None of this is in the ADR; it's the competitive context that informed the hybrid direction.

---

## doc 02: ADR-1 Exploration

- **§S1**: Why append-only is forced (not a choice) — traces to V3, P3, S00
- **§S2**: The real choice — three options with concrete JSON examples showing how capture, correction, and review look in each
- **§Scenarios**: S00, S01, S06b, S04/S11, S07/S14, S08, S19 run through all three options. Verdict per scenario
- **§Device Storage**: Storage/performance budget analysis — proves neither is a differentiator at expected scale
- **§Commitments**: What each option locks you into long-term (document versioning vs. operational timeline vs. dual-representation)
- **§Honest Narrowing**: The pivotal question — "Is single source of truth a requirement, or is traceability + performance sufficient?"

**Unique value not in ADR**: The per-scenario comparison tables (§Scenarios) showing exactly where snapshots strain and where events/action-log diverge. The storage budget math (§Device Storage) that proves storage is a non-factor. The "what you're signing up for" characterizations that frame each option as a long-term trajectory.

---

## doc 03: Forward Projection

- **§ADR-2**: Identity merge under each option — snapshots = content-rewriting (hard), events = stream-linking (clean)
- **§ADR-3**: Sync under each option — snapshots = one-size-fits-all (CommCare scaling wall), events = two-tier sync possible
- **§ADR-4**: Configuration under each option — snapshots = simple but low ceiling, events = higher complexity but clearer boundary
- **§ADR-5**: Workflow under each option — snapshots fuse data+action (modeling wall), events separate them (natural fit)
- **§Irreversibility Gradient**: B→C cheap, C→B risky, A→anything lossy. Events hold the safest position.
- **§Failure Modes**: Where each option could fail and what the escape hatch is

**Unique value not in ADR**: The irreversibility gradient (§Irreversibility) — the asymmetry argument that made events the winner. The failure mode analysis (§Where B/C/A could fail) — honest about projection complexity risk and the B→C escape hatch. The "Action Log converges toward event-sourcing under pressure" finding — why the pragmatic middle ground isn't actually cheaper.

---

## doc 04: Decision Audit

- **§1**: Every decision extracted from docs 01-03 — 7+7+4 = 18 total, with status tracking
- **§2**: Scoping analysis — 10 correctly in ADR-001, 6 that were over-reaching into ADR-3/4/5, 2 missing
- **§4**: Six issues detected — the escape hatch was aspirational (Issue 3, severity High), ADR-001 pre-decided ADR-4/5 (Issues 1-2)
- **§5**: Exact text changes applied to ADR-001 — write-path discipline rule, softened consequences, expanded deferral table
- **§7**: What must NOT be decided yet — 10 decisions with timing

**Unique value not in ADR**: The specific over-reach corrections (§4-§5) — what ADR-001 originally said about ADR-4/5 and why it was walked back. The "write-path discipline" rule origin story — it wasn't in the first draft; the audit added it as the mechanism that makes the escape hatch viable, not aspirational.
