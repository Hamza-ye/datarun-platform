# NW-124 PC2 Synthetic Walkthrough Proof

Status: non-authoritative product-validation / owner-review evidence artifact
Document type: product_validation_artifact / owner_review_evidence
Source: NW-124; accepted NW-123; accepted NW-122 / PR #38; PC2 PM handoff; accepted NW-072 conflict/flag and attention-query boundary
Authority: evidence/routing only; does not add product behavior, runtime behavior, validation policy, CI behavior, real-production approval, reporting scope, conflict operations, or implementation standing
Last reviewed: 2026-06-21

## 1. Purpose

This artifact records the PC2 synthetic proof packet selected by NW-123 after
NW-122 implemented the `Single Work-Linked Attention Review` loop.

It walks the PC2 owner-review journey from one visible `Needs review` cue to
one attention review page and one manual decision. It classifies each proof
beat as `PASS`, `FRICTION`, `NOT_RUN`, or `OUT-OF-SCOPE`, states whether
runtime/manual inspection actually happened, and selects exactly one next
route.

This is product-validation / owner-review evidence only. It does not implement
runtime code, approve real users/data, broaden PC2 into reporting or queue
operations, or change accepted product/platform scope.

## 2. Evidence Mode

Runtime/manual UI inspection performed: **No**.

Live browser/manual click-through classification: **NOT_RUN**.

Reason: no accepted low-friction runtime session path was available for this
proof. The available local and lab paths did not produce a clean manual browser
walkthrough surface before this artifact:

- `datarun-app.lab` was not reachable from the current environment.
- The default local Docker database path had stale Flyway checksum drift for
  migration V3.
- An isolated local compose rebuild was not completed during exploration.
- The accepted web-admin browser path depends on OIDC login plus explicit
  principal binding; no documented local synthetic browser login/provisioning
  path was selected for this NW.

Evidence basis: accepted PR #38 / NW-122 implementation and validation
evidence, accepted NW-123 standing, and accepted product/platform boundaries.

Classification rule used below:

- `PASS` means the accepted implementation and validation evidence supports
  the beat for synthetic owner-review proof.
- `NOT_RUN` means live browser/manual runtime inspection did not happen.
- `OUT-OF-SCOPE` means the beat would require work explicitly excluded from
  PC2 or routed to another NW/gap.

## 3. Sources Used

- `docs/status.md`
- `docs/specifications/product/product-candidate-2-pm-handoff.md`
- `docs/specifications/platform/conflict-flag-resolution-and-attention-query-boundary.md`
- `docs/agent-working-surface/artifacts/NW-123-pc2-post-nw122-demo-standing-and-successor-selection.md`
- `docs/agent-working-surface/platform-next-work-backlog.md`
- PR #38 / NW-122 validation evidence:
  - `docker compose -f docker-compose.test.yml up -d test-db` passed.
  - `git diff --check` passed.
  - `./mvnw -Dtest=WebAdminOperationalViewIntegrationTest,ConflictResolutionIntegrationTest test`
    passed 32 tests, 0 failures, 0 errors, 0 skipped in 41.694 s.
  - `./mvnw test` passed 401 tests, 0 failures, 0 errors, 0 skipped in
    01:22 min.

## 4. Synthetic Fixture

Fixture label: `Example Organization` / `Example Field Activity`.

Selected example: one S21-like scoped supervisor/reviewer attention item
attached to one visible work record.

The proof intentionally uses one work-linked attention item only:

- one organization-like synthetic context;
- one scoped source work item;
- one visible `Needs review` cue;
- one attention review page;
- one exact designated reviewer;
- one manual decision.

S27-style logistics pressure remains useful product context, but this proof
does not select a second item or broaden the review into queue/list/multi-item
workflow.

## 5. Proof Beat Walkthrough

| # | Beat | Classification | Evidence / note |
|---|---|---|---|
| 1 | A scoped reviewer opens the operational surface and sees at most one visible source work item with a `Needs review` cue. | PASS | NW-122 accepts `/web-admin/operational` as the server-rendered scoped operational page and keeps the cue one-item only. |
| 2 | The cue is work-linked, not a broad conflict queue or reporting drilldown. | PASS | NW-122 links the cue to one server-rendered attention review item attached to the visible scoped work. Queue/list/multi-item review remains unselected. |
| 3 | The reviewer opens one attention review page from the cue. | PASS | NW-122 added the `/web-admin/operational/attention` review page and re-applies accepted assignment scope before returning attention details. |
| 4 | The review page shows product-safe source-work and attention context. | PASS | NW-122 renders bounded product copy, avoids raw actor UUIDs, and avoids raw detector reasons in the PC2 product UI. |
| 5 | The page makes resolver standing understandable without broadening authority. | PASS | Exact stored `designated_resolver` equality is the accepted authority for the opened item. `resolver_unassigned` is blocked/not currently resolvable with no fallback, override, or reassignment path. |
| 6 | The reviewer makes one manual decision on the opened item. | PASS | Accepted evidence covers POST resolution bound to the opened attention item through an opaque session token, re-querying the exact unresolved visible flag before mutation. |
| 7 | The decision uses only the web-admin session actor, not browser/body actor data. | PASS | NW-122 uses the server-resolved session actor and ignores body/UI actor spoofing. |
| 8 | Resolution preserves append-only source work and stores resolution separately. | PASS | NW-122 calls the existing append-only `ConflictResolutionService.resolve(...)`; source work is not rewritten. |
| 9 | After resolution, the item no longer counts as unresolved attention. | PASS | NW-122 treats a flag as unresolved only when no canonical exact-resolver `conflict_resolved/v1` exists. |
| 10 | A live browser/manual runtime click-through was performed during NW-124. | NOT_RUN | Runtime/manual inspection was not performed; this artifact is based on accepted PR #38 implementation and validation evidence. |
| 11 | The proof expands into list review, queue triage, reporting/import/export, batch workflow, automation, reassignment, or resolver eligibility broadening. | OUT-OF-SCOPE | These routes remain explicitly excluded from PC2 proof and routed to existing NW/gap surfaces if concrete pressure appears. |

## 6. Boundary Checks

No stop condition fired.

NW-124 does not use or approve:

- real users, real organizational data, customer data, or production secrets;
- real-production readiness or go/no-go approval;
- reporting dashboards, imports, exports, warehouses, analytics,
  completeness, broad read APIs, or drilldown;
- queue/list/multi-item review, filters, batch workflow, broad conflict
  console, resolver reassignment, or auto-resolution;
- resolver eligibility broadening beyond exact stored `designated_resolver`
  equality for the opened item;
- tenant/control-plane work;
- contract, schema, envelope, sync, validation-policy, CI, BAR, CDL, or
  gap-register changes;
- runtime implementation or runtime tests.

Deferred concerns remain routed to existing surfaces:

- real users/data: NW-093;
- reporting/import/export: NW-044 and projection gap surfaces;
- queue/list/multi-item review: GAP-CONFLICT-01 successor pressure only;
- automation/batch/resolver reassignment: NW-045;
- resolver eligibility broadening: GAP-CONFLICT-03 successor pressure only;
- tenant/control-plane: NW-094 through NW-098.

## 7. Resulting PC2 Standing

PC2 remains:

```text
synthetic-demo-ready, not real-production-ready
```

NW-124 adds owner-review proof evidence over accepted implementation/test
evidence, with live runtime/manual inspection explicitly `NOT_RUN`.

PC2 is not real-production-ready. Real users or real organizational data still
require NW-093 first.

## 8. Selected Next Route

Selected next route:

```text
park PC2 proof route
```

Reason: the accepted NW-122 surface is sufficient for synthetic owner-review
evidence, NW-124 records the manual-runtime limitation instead of burying it,
and no concrete small polish, NW-093 real-use trigger, reporting route,
queue/list route, automation route, resolver-eligibility route, or
tenant/control-plane route is selected.

No successor prompt is added. Future PC2 work must be selected separately from
the PM handoff/backlog. If an owner requires a live browser walkthrough later,
that should be selected as a separate bounded environment/manual-proof route,
not folded silently into this evidence artifact.

## 9. Validation Category

Docs-only product-validation / owner-review evidence.

Runtime tests are skipped because NW-124 changes only working-surface evidence,
status/backlog trace, and artifact indexing. It changes no runtime code, tests,
contracts, schemas, migrations, CI behavior, validation policy, product spec,
platform spec, BAR, CDL, gap register, mobile code, or server/web-admin
implementation.

## 10. Review Notes For ChatGPT

- Review verdict for NW-124 should verify that live runtime/manual inspection
  is explicitly marked `NOT_RUN` and not implied.
- Blocking runtime issue claim: none. The proof uses accepted PR #38 evidence
  rather than making a new runtime inspection claim.
- Selected next route: park PC2 proof route.
- Boundaries to preserve: no real users/data without NW-093; no queue/list or
  multi-item review unless selected through GAP-CONFLICT-01; no
  reporting/import/export without NW-044; no automation, batch, or resolver
  reassignment without NW-045; no resolver eligibility broadening without
  GAP-CONFLICT-03 successor; no tenant/control-plane work without NW-094
  through NW-098.
