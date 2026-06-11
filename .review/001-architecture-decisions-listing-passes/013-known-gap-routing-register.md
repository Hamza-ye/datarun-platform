# 013 Known Gap Routing Register

## Context Capsule

* Artifact: `013-known-gap-routing-register.md`
* Status: Active known-gap register for the decision-anchor layer after Wave 4 split
* Mode: Routing/status register only; no redesign, no reopening, no new architecture decisions.
* Source:

  * split from `013-gap-routing-playbook.md` Section 8;
  * reconciled against `011` accepted-extension inputs and `012` vocabulary catch-up overlay;
  * validated against current BAR/NW/IDR standing from `docs/status.md`.
* last-reviewed: 2026-06-11
* Authority note:

  * This register does not override the CDL, contracts, BAR, NW backlog, IDRs, or `013`.
  * It is the active known-gap lookup for future routing.
  * Detailed historical examples embedded in `013` are legacy recovery-pass detail until physically pruned.

---

## 1. Register Rules

Use this file for current known-gap status. Use `013-gap-routing-playbook.md` for stable routing rules.

Every row must keep these distinctions separate:

* `Classification`: one of the allowed `013` gap classifications.
* `Status`: accepted, deferred, future-decision, partially accepted, or reference-only.
* `Route`: the next artifact type or backlog route.
* `Sources`: provenance/evidence inputs, not competing authority.

Do not add a new gap here when an existing row covers the pressure. Update the existing row with a new source and last-reviewed date.

---

## 2. Current Known Gaps

| Gap ID | Gap | Status | Classification | Sources | Current route |
|---|---|---|---|---|---|
| GAP-AUTH-01 | Auditor/query access | Partially constrained; broad forms deferred | Architecture decision gap | NW-049, NW-051, BAR-104, BAR-106, BAR-108 | Simple current-scope auditor visibility remains ordinary assignment/config posture. Broad audit/history read surfaces, redacted/no-local-retention views, dynamic auditor scope, and emergency/special write bypasses require a concrete product/security decision before implementation. |
| GAP-PROJECTION-01 | Aggregate access semantics | Open | Architecture decision gap if aggregates bypass event access; platform-spec detail if they inherit event access | NW-044, NW-047, CDL-030, CDL-031 | Reporting/aggregate work should route through NW-044 or a bounded reporting spec; any aggregate visibility outside event-level access requires formal decision. |
| GAP-AUTH-02 | Actor-as-subject delivery rule | Open/future decision | Architecture decision gap | NW-049, NW-053 route, BAR-108 | Route with subject/query/custom-scope pressure; do not add a non-assignment sync dimension without formal decision. |
| GAP-AUTH-03 | Activity role-action table artifact | Accepted baseline for current coarse action model | Reference-only / future finer granularity route | IDR-021, IDR-023, BAR-010, NW-041 | Current model is accepted: `capture`, `review`, `alert`, `task_created`, `task_completed`; `assignment_changed` excluded. Only future finer role-action granularity remains a route. |
| GAP-WORKFLOW-01 | Pattern Registry inventory | Partially accepted | Platform-spec detail gap | IDR-025, BAR-010, contracts/patterns | Canonical contract and delivery are accepted. Additional platform pattern definitions route as platform-spec/platform-evolution work under Pattern Registry boundaries. |
| GAP-WORKFLOW-02 | Pattern migration mechanics | Open | Platform-spec detail gap / implementation tooling gap | IDR-025, BAR-010 | Specify migration only when concrete compatibility pressure appears; do not create durable workflow-state tables. |
| GAP-WORKFLOW-03 | Additional `context.*` values | Open | Architecture decision gap or platform-spec detail gap depending on semantics | CDL-052, DEC-WORKFLOW-05 | New values must stay platform-fixed, read-only, pre-resolved, and bounded; architecture review required if the value changes expression authority. |
| GAP-WORKFLOW-04 | Additional auto-resolution policies | Deferred | Architecture decision gap / policy surface | CDL-053, CDL-054, BAR-102, IDR-026, NW-045 | Mechanism class exists, but runtime execution is deferred. Preserve exact designated-resolver equality and avoid direct flag mutation. |
| GAP-CONFLICT-01 | Flag queue ergonomics | Open | Platform-spec detail gap / implementation tooling gap | IDR-026, NW-047 | UX may improve queue behavior without changing flag semantics, resolver authority, or canonical resolution. |
| GAP-CONFLICT-02 | Domain conflict automation and batch resolution | Future decision | Architecture decision gap / platform-spec detail gap | CDL-045, CDL-053, CDL-054, IDR-026, NW-045 | Route through NW-045. Batch/automation must emit per-flag resolution events and preserve resolver equality. |
| GAP-CONFIG-01 | Config authoring syntax | Open | Implementation/tooling gap | CDL-038, CDL-043, NW-034 | Tooling format may evolve if it preserves config-package and shape-format contracts. |
| GAP-CONFIG-02 | Setup lifecycle for new operational activity | Open | Platform-spec detail gap / operational policy gap | BAR-010, NW-047 | Define draft/validate/review/approve/publish workflow under config-package boundaries. |
| GAP-PROJECTION-02 | Reporting freshness semantics | Open | Platform-spec detail gap | NW-044, NW-047 | Define freshness, completeness, unresolved-flag handling, and drilldown under access constraints. |
| GAP-SYNC-01 | Handoff package contents | Open | Platform-spec detail gap | IDR-030, BAR-004, NW-055 | Define handoff contents under subject-history, sync, projection, and actor-partition constraints. |
| GAP-RETENTION-01 | Retention windows | Future decision | Operational policy gap / architecture decision gap if it changes event/sync semantics | BAR-106, NW-054 | Route through NW-054. Do not delete server event history or rewrite normal sync watermarks. |
| GAP-RETENTION-02 | Worker offboarding / exit procedure | Future decision | Operational policy gap | BAR-106, NW-054 | Route with retention/security policy; preserve assignment-derived access and shared-device sealing rules. |
| GAP-RETENTION-03 | Regulatory encryption/redaction/erasure | Future decision | Architecture decision gap / operational policy gap | BAR-106, NW-054 | Separate local retention/security from immutable server event truth; deletion/redaction requires formal authority. |
| GAP-PRODUCT-01 | Multi-tenant naming strategy | Open | Product/problem evidence gap / platform-spec detail gap | NW-047 | Requires concrete deployment archetype pressure before specification. |
| GAP-CONFIG-03 | Complexity budget changes | Open | Platform-spec detail gap / architecture decision gap if guardrails weaken | CDL-044 | Adjust only with validation evidence; weakening deploy-time guardrails requires architecture review. |
| GAP-AUTH-04 | Cross-activity cohort materialization | Open/future decision | Architecture decision gap | NW-053 route, BAR-108 | Any subject/query/custom scope beyond accepted assignment axes requires formal decision. |
| GAP-AUTH-05 | Cross-activity subject access for a second actor | Open/future decision | Architecture decision gap | NW-053 route, BAR-108 | Do not bypass assignment-derived access or create hidden sync scope. |
| GAP-SCENARIO-01 | Scenario phasing for S23-S27 | Partially accepted as runtime probes | Product/problem evidence gap / platform-spec detail gap | NW-032/S23, NW-033/S26, NW-030/S27, NW-042/S22 | Accepted probes provide evidence but do not add new primitives. Product/spec work should cite the exact NW row and preserve no-new-primitive constraints. |

---

## 3. Deferred/Future-Decision Guardrails

These fronts must not be implemented from this register alone:

* broad audit/history read surfaces;
* emergency or special write bypasses;
* new scope mechanisms;
* subject/query/custom scope;
* runtime auto-resolution execution;
* resolver reassignment;
* online production binding-admin APIs;
* IdP group/claim authority;
* mobile authoritative rejection;
* retention/decommissioning/sealed-partition recovery;
* server event deletion/redaction.

Each requires the route named above plus a bounded implementation prompt before code changes.
