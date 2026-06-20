# NW-118 PC1 Managed Lab Proof Boundary

Status: non-authoritative product-planning artifact
Document type: product_validation_artifact
Source: NW-118; NW-117 PC1 internal synthetic demo proof; NW-116 PC1 proof target decision; PC1 PM handoff; accepted PC1 product spec
Authority: owner-decision/planning artifact only; does not add product behavior, runtime behavior, validation policy, CI behavior, real-production approval, reporting scope, operational policy, or implementation standing
Last reviewed: 2026-06-21

## 1. Purpose

This artifact decides the bounded managed lab proof route after NW-117 passed
the internal synthetic demo proof.

PC1 remains:

```text
synthetic-demo-ready, not real-production-ready
```

NW-118 defines the lab boundary before any managed lab run. It does not run a
lab, use real users, use real organizational data, approve production, create
support commitments, or implement runtime behavior.

## 2. Source Standing

| Source | Standing used by NW-118 |
|---|---|
| PC1 product spec | PC1 can be demonstrated with synthetic or non-sensitive candidate evidence; real users or real organizational data require the separate real-production route. |
| PC1 PM handoff | Managed lab proof and real-use preparation are separate proof-target choices; real production requires NW-093. |
| NW-117 artifact | All 16 NW-111 sequences are `PASS` using the synthetic/non-sensitive `Example Organization` fixture; no friction was promoted. |
| NW-116 artifact | Managed lab proof was previously rejected only because the lab boundary was not selected yet; real-use preparation remains blocked behind NW-093. |
| Active status/backlog | NW-118 is the active process/control slice; NW-093 remains blocked until concrete real users/data, provider, region, jurisdiction, or support commitment appears. |
| Validation matrix | Product planning/docs-only work uses `git diff --check` plus targeted file/grep checks; runtime tests are skipped for docs-only planning. |

## 3. Owner Route Decision

Selected owner route:

```text
proceed to managed lab proof
```

The selected route is a synthetic/non-sensitive managed lab proof only. It is
not real-use preparation and is not real-production approval.

Rejected alternatives:

| Alternative | Decision | Reason |
|---|---|---|
| Select NW-093 real-use preparation | Not selected now | No concrete real users, real organizational data, provider, region, jurisdiction, support commitment, compliance/security review, continuity requirement, or go/no-go trigger is active. |
| Repeat the internal synthetic demo | Not selected | NW-117 already reviewed all 16 sequences as `PASS` with no recorded friction. |
| Park | Not selected | The internal proof is complete and a bounded synthetic managed lab proof is the next useful owner-review step. |

## 4. Managed Lab Boundary

The managed lab proof may proceed only inside this boundary:

| Boundary item | Selected value |
|---|---|
| Lab organization label | `Example Lab Organization` |
| Environment owner | Datarun deployment owner for the selected lab environment. |
| Operator/contact path | Owner-review lab operator through the repository/owner-review channel for the selected NW. |
| Data boundary | Synthetic/non-sensitive data only; no real users, no real organizational data, no customer data, and no production secrets. |
| Fixture/data source | NW-111/NW-117 neutral fixture labels, optionally renamed from `Example Organization` to `Example Lab Organization` for lab context without changing product/platform vocabulary. |
| Acceptance criteria | All 16 NW-111 sequences are demonstrated or explicitly marked `NOT_RUN` with rationale; no sequence uses real users/data; real-production standing remains blocked; friction is recorded as candidate follow-up pressure only. |
| Evidence owner | Product-validation / owner-review evidence agent for the selected lab proof packet. |
| Stop conditions | Stop before real users/data, real-production approval, reporting/export, retention/security promises, conflict workflow, entity lifecycle, tenant/control-plane work, contract/schema/sync changes, validation-policy/CI changes, BAR/CDL/gap-register changes, or runtime implementation. |

The lab proof may use an owner-managed lab environment only as a synthetic
demonstration target. It must not create a durable operations policy, provider
selection, support SLA, compliance/security approval, continuity promise, or
go/no-go production decision.

## 5. Evidence Required For The Lab Proof

The managed lab proof packet should record:

- lab organization label and environment owner;
- fixture labels and explicit synthetic/non-sensitive data confirmation;
- the 16 NW-111 sequence outcomes as `PASS`, `FRICTION`, `NOT_RUN`, or
  `OUT-OF-SCOPE`;
- notes tying each sequence to observed lab evidence or a not-run rationale;
- any friction as candidate follow-up pressure only;
- confirmation that no real users or real organizational data were used;
- confirmation that no real-production approval was granted;
- whether the owner should repeat the lab proof, select NW-093 for real-use
  preparation, select a bounded polish/follow-up route, or park.

## 6. Successor Route

Successor prompt created:

```text
docs/agent-working-surface/prompts/NW-119-run-pc1-managed-lab-proof.md
```

Selected successor:

```text
NW-119 - Run PC1 managed lab proof
```

Type: `product_validation / owner_review_evidence`

Priority: `P1`

Backlog status: `ready`

NW-119 should exit with one managed lab proof evidence packet:

```text
docs/agent-working-surface/artifacts/NW-119-pc1-managed-lab-proof.md
```

NW-119 remains product-validation / owner-review evidence unless the selected
task packet later supplies an allowed manual lab evidence path. It must not
implement runtime code or change product/platform behavior.

## 7. Real-Production Boundary

PC1 remains:

```text
synthetic-demo-ready, not real-production-ready
```

The selected managed lab route does not approve real users, real
organizational data, provider/region/jurisdiction choices, support commitment,
notification path, compliance/security review, continuity plan, or go/no-go
production ownership.

If the owner later chooses real-use preparation, NW-093 must be selected first.
NW-118 does not unblock or partially satisfy NW-093.

## 8. Stop Conditions Checked

No stop condition fired.

NW-118 did not require or select:

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

Docs-only product-planning / owner-decision artifact.

Runtime tests are skipped because NW-118 changes only working-surface
artifacts, prompt routing, backlog/status trace, and artifact indexing. It
changes no runtime code, tests, contracts, schemas, migrations, CI behavior,
validation policy, product spec, platform spec, BAR, CDL, gap register, or
mobile code.
