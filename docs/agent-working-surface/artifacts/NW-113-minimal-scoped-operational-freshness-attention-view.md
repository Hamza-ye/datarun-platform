# NW-113 Minimal Scoped Operational Freshness/Attention View

Status: non-authoritative product-validation artifact
Document type: product_validation_artifact
Source: NW-113; NW-112 demo review; NW-111 synthetic demo walkthrough; NW-110 smoke definition; PC1 PM handoff; accepted PC1 product spec
Authority: planning/boundary artifact only; does not add product behavior, runtime behavior, validation policy, CI behavior, real-production approval, reporting scope, conflict operations, or implementation standing
Last reviewed: 2026-06-20

## 1. Purpose

This artifact defines the smallest supervisor/reviewer observation needed for
the Product Candidate 1 synthetic demo final beat:

- latest synced work;
- freshness wording;
- one narrow attention cue if present.

It is product-validation and product-planning only. It does not implement a
view, accept runtime behavior, approve real production, broaden reporting, or
create conflict-review authority.

## 2. Source Standing

| Source | Standing used by NW-113 |
|---|---|
| PC1 product spec | PC1 is a one-Organization operational loop. Optional review/attention must stay successor-gated and must not become reporting, export, broad audit, conflict automation, tenant work, or real-production approval. |
| PC1 PM handoff | The PC1 goal includes supervisors seeing latest synced work with freshness and unresolved attention items, but freshness/latest-synced wording must stay a scoped operational view rather than a reporting product. |
| NW-110 smoke definition | The final smoke step is a supervisor/reviewer review of latest synced work, freshness wording, and one `Needs review` cue if present. |
| NW-111 walkthrough | Sequence 16 is the only step where the current demo still needs a minimal view, single attention route, or deferral decision. |
| NW-112 review | Sequences 1-15 are clear; sequence 16 is friction because the final supervisor/reviewer beat needs a bounded non-reporting proof boundary before implementation. |

The accepted standing is enough to describe the product boundary. It is not
enough to claim that the final supervisor/reviewer beat is already demoable as
a concrete PC1 observation.

## 3. Selected Boundary

Decision:

```text
One separate implementation successor should be selected later.
```

Selected successor:

```text
NW-114 - Implement minimal operational freshness/attention view
```

Why this is the selected boundary:

- Current accepted standing is not enough for the synthetic demo final beat
  because it does not yet provide one concrete supervisor/reviewer observation
  that ties latest synced work, freshness wording, and the optional attention
  cue together.
- Deferring the final beat would leave the only NW-112 friction unresolved and
  would weaken the PC1 setup-to-sync proof.
- A separate implementation successor can keep the work narrow and testable
  without mixing runtime implementation into NW-113.

## 4. Smallest PC1 Observation

The smallest acceptable observation answers exactly three reviewer questions:

1. What is the latest synced work I can inspect for this PC1 scope?
2. How fresh is that synced work in product-safe wording?
3. Does that same work carry one narrow `Needs review` cue, if present?

Required visible elements:

| Element | Minimal meaning | Must not imply |
|---|---|---|
| Latest synced work | One latest synced activity entry or work item visible to the supervisor/reviewer for the accepted PC1 scope, with enough context to recognize the synthetic `Assigned Visit` / `Visit Record` beat. | Full history, broad audit, all-subject access, aggregate reporting, or cross-actor completeness. |
| Freshness wording | A product phrase tied to the visible work or narrow view, such as last successful sync timing, no synced work yet, or retry/failure context when already available from accepted state. | Production connectivity SLA, proof that all devices are current, retention guarantee, or server-wide reporting freshness. |
| Narrow attention cue | One read-only `Needs review` cue on the visible work if an unresolved attention item already exists. Absence of the cue is acceptable when no such item is present. | Conflict queue, flag report, batch review, resolver reassignment, auto-resolution, or resolution workflow. |

The demo final beat is satisfied when a supervisor/reviewer can see the latest
synced `Visit Record`, understand its freshness, and notice one `Needs review`
cue if present, without leaving the narrow operational context.

## 5. Non-Goals

NW-113 and its successor boundary do not include:

- dashboards, exports, imports, warehouses, aggregate analytics, or reporting
  product scope;
- broad read APIs, broad audit/history, support-operator inspection, or
  subject-history/audit pull;
- conflict operations, flag reporting, batch review, resolver reassignment,
  auto-resolution, or resolution UI;
- retention/security/offboarding promises;
- entity lifecycle, known-set, duplicate, merge, split, or registry workflows;
- tenant/control-plane work, tenant selectors, workspace selectors, or pooled
  SaaS behavior;
- contract, schema, envelope, sync protocol, authority-source, or durable
  workflow-state changes;
- real domain selection, real users, real organizational data, or
  real-production approval;
- product-spec, PM-handoff, validation-policy, CI, BAR, CDL, or gap-register
  changes.

## 6. Successor Boundary

NW-114 should implement only the selected minimal observation. It should prefer
existing accepted web-admin/session/command-gate and scoped-read standing, and
must stop if the implementation would require a new authority source, new sync
scope, new contract, broad reporting route, or conflict-review productization.

NW-114 may show a read-only attention cue when existing accepted data already
contains one. It must not add resolution actions, batch operations, flag
reports, or resolver changes.

## 7. Validation Category

Docs-only product-validation artifact.

Runtime tests are skipped for NW-113 because this artifact changes only
working-surface artifacts, trace, backlog, status, and an optional successor
prompt. It changes no runtime code, tests, contracts, schemas, migrations, CI
behavior, validation policy, product scope, platform spec, BAR, CDL, or gap
register.

## 8. Stop Conditions

Stop and route before implementation or acceptance if the route requires:

- real domain or pilot selection;
- real users or real organizational data;
- product-scope change;
- runtime implementation inside NW-113;
- reporting dashboards, exports, imports, warehouses, analytics, or broad read
  APIs;
- retention/security promises;
- entity lifecycle;
- conflict automation, batch review, resolver reassignment, auto-resolution, or
  flag reporting;
- tenant/control-plane work;
- contract, schema, envelope, authority-source, or sync changes;
- architecture/gap routing.
