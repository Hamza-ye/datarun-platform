# NW-116 PC1 Proof Target Decision

Status: non-authoritative product-planning artifact
Document type: product_validation_artifact
Source: NW-116; NW-115 PC1 post-NW-114 demo standing; PC1 PM handoff; accepted PC1 product spec
Authority: owner-decision/planning artifact only; does not add product behavior, runtime behavior, validation policy, CI behavior, real-production approval, reporting scope, operational policy, or implementation standing
Last reviewed: 2026-06-21

## 1. Purpose

This artifact chooses the next Product Candidate 1 proof target after NW-115
marked PC1:

```text
synthetic-demo-ready, not real-production-ready
```

It selects exactly one proof target and preserves the real-production boundary.
It does not implement product work, approve real use, add reporting, or change
accepted product/platform scope.

## 2. Source Standing

| Source | Standing used by NW-116 |
|---|---|
| PC1 product spec | PC1 can be demonstrated with synthetic or non-sensitive candidate evidence; real users or real organizational data require the separate real-production route. |
| PC1 PM handoff | The first proof-target decision chooses between internal synthetic demo, managed lab proof, or real-use preparation; real production requires NW-093. |
| NW-115 artifact | All 16 NW-111 demo sequences are `CLEAR`; PC1 is synthetic-demo-ready, not real-production-ready; NW-044 remains unselected. |
| Active backlog/status | NW-116 is the active process/control slice; NW-093 remains blocked until concrete real users/data, provider, region, jurisdiction, or support commitment appears. |
| Validation matrix | Product planning/docs-only work uses `git diff --check` plus targeted file/grep checks; runtime tests are skipped for docs-only planning. |

## 3. Selected Proof Target

Selected proof target:

```text
internal synthetic demo
```

Why this is the selected target:

- It is the smallest useful proof after NW-115 because the full 16-sequence
  PC1 synthetic demo is now clear.
- It uses only synthetic/non-sensitive evidence and the accepted neutral
  `Example Organization` fixture.
- It can produce owner-review evidence without selecting a lab operator,
  external organization, provider, region, support path, compliance/security
  review, or go/no-go production owner.
- It keeps managed-lab and real-use choices reversible until an owner supplies
  concrete target context.

## 4. Rejected Proof Targets

| Proof target | Decision | Reason |
|---|---|---|
| Managed lab proof | Not selected now | No managed lab organization, environment owner, operator, support path, data boundary, or lab acceptance criteria have been selected. Choosing a lab proof now would create operational assumptions that are not present in the prompt or current status. |
| Real-use preparation through NW-093 | Not selected now | No concrete real users, real organizational data, provider, region, jurisdiction, support commitment, compliance/security review, or go/no-go trigger is active. NW-093 remains blocked and must be selected before real-use preparation. |
| Explicit park | Not selected | NW-115 found PC1 synthetic-demo-ready. Parking would discard the immediate value of turning the cleared script into owner-review evidence. |

## 5. Required Evidence For Selected Target

The internal synthetic demo target requires one bounded evidence packet that:

- uses only synthetic or non-sensitive data;
- reuses the NW-111 neutral fixture unless a future owner explicitly selects
  domain vocabulary;
- walks all 16 NW-111 sequences;
- records pass/fail/not-run standing for each sequence;
- records any observed friction as follow-up pressure, not automatic
  implementation scope;
- confirms that the demo does not use real users or real organizational data;
- confirms that the demo does not claim real-production readiness;
- preserves the NW-115 freshness boundary: NW-114 operational freshness is
  sufficient for PC1 demo proof, while `GAP-PROJECTION-02` remains open for
  reporting freshness/completeness/drilldown.

## 6. Successor Route

Successor prompt created:

```text
docs/agent-working-surface/prompts/NW-117-run-pc1-internal-synthetic-demo-proof.md
```

Selected successor:

```text
NW-117 - Run PC1 internal synthetic demo proof
```

Type: `product_validation / owner_review_evidence`

Priority: `P1`

Backlog status: `ready`

NW-117 should exit with one internal synthetic demo evidence packet:

```text
docs/agent-working-surface/artifacts/NW-117-pc1-internal-synthetic-demo-proof.md
```

NW-117 remains docs/product-validation unless the selected task packet later
requires manual environment evidence. It must not implement runtime code or
change product/platform behavior.

## 7. Real-Production Boundary

PC1 remains:

```text
synthetic-demo-ready, not real-production-ready
```

The selected internal synthetic demo target does not approve real users, real
organizational data, provider/region/jurisdiction choices, support commitment,
notification path, compliance/security review, continuity plan, or go/no-go
production ownership.

If the owner later chooses real-use preparation, NW-093 must be selected first.
NW-116 does not unblock or partially satisfy NW-093.

## 8. Stop Conditions Checked

No stop condition fired.

NW-116 did not select or require:

- real users or real organizational data;
- real-production approval;
- reporting dashboards, exports, imports, warehouses, analytics, broad read
  APIs, completeness semantics, or drilldown;
- retention/security/offboarding promises;
- entity lifecycle;
- conflict automation, batch review, resolver reassignment, auto-resolution,
  flag reporting, or conflict workflow;
- tenant/control-plane work;
- contract, schema, envelope, authority-source, sync, validation-policy, CI,
  BAR, CDL, or gap-register changes;
- runtime implementation, mobile code, or server/web-admin implementation.

## 9. Validation Category

Docs-only product-planning/owner-decision artifact.

Runtime tests are skipped because NW-116 changes only working-surface
artifacts, prompt routing, backlog/status trace, and artifact indexing. It
changes no runtime code, tests, contracts, schemas, migrations, CI behavior,
validation policy, product spec, platform spec, BAR, CDL, gap register, or
mobile code.
