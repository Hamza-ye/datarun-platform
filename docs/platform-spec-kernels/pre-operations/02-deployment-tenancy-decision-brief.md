# Deployment And Tenancy Decision Brief

Status: Stakeholder brief; accepted outcome recorded in `04-accepted-pre-atomization-decisions.md`

Audience: stakeholders and decision reviewers. This brief explains the tradeoff; it is not the agent-facing instruction surface.

## Decision

How should deployment and tenant context be handled during initial platform-spec atomization, especially for cloud and self-hosted futures?

## Why Now

`00-baseline-validation-and-full-stack-readiness.md` classified the multi-tenant deployment model as requiring a pre-atomization decision or hold-back. If this is left vague, atomization may accidentally add deployment or tenant identity to the event envelope, sync contract, configuration model, or reporting model.

## Baseline Constraints

- ADR-001 through ADR-005 protect the event envelope and immutable event identity.
- The accepted envelope does not include `tenant_id` or `deployment_id`.
- `constraints.md` requires support for jurisdictional variation, data residency, and structured exchange, but it does not define hosting architecture.
- `../professional-baseline/16-operational-constraints-boundary-control.md` says operational constraints do not decide storage, sync protocol mechanics, access-control implementation, reporting product model, external integration protocols, regulatory framework implementation, or module boundaries.
- `00-baseline-validation-and-full-stack-readiness.md` treats `Deployment / Tenancy` as a routing placeholder, not an accepted system boundary.

## Options Considered

| Option | Description | Assessment |
|---|---|---|
| A. Single-deployment runtime first | Initial atomization assumes one deployment context per runtime/database. Deployment context stays outside event envelopes. Cloud multi-tenancy is a later deployment architecture decision. | Recommended as initial guardrail. Smallest decision that protects envelope stability and keeps self-host simple. |
| B. Shared cloud multi-tenant runtime now | Atomization defines tenant isolation, tenant-scoped storage, tenant-aware sync, tenant-aware admin, and shared-runtime operations now. | Too broad for current stage. Likely to create platform invariants before stakeholder and ops needs are understood. |
| C. Tenant identity in every event envelope | Add tenant/deployment identity to events so every record is globally partitionable inside a shared event log. | Not acceptable without formal change control. It changes the envelope and risks redefining event identity. |
| D. Hybrid reserved field or optional metadata | Reserve tenant/deployment metadata in or near events but do not fully define behavior. | Risky. A reserved field tends to become implementation authority without a closed invariant. |

## Product Owner View

Stakeholder benefit:

- First deployments can be understood as one organization/deployment at a time.
- Self-hosted deployments remain easier to explain and operate.
- Data residency and jurisdictional concerns can be handled at deployment packaging and hosting boundaries before cross-tenant cloud operations exist.

Stakeholder harm:

- Cloud SaaS operators will not yet have a full shared-runtime multi-tenant model.
- Cross-tenant administration, benchmarking, managed hosting, and migration between deployments remain later work.

Adoption risk:

- Low for first self-hosted or single-organization deployments.
- Higher if the intended first commercial product is shared cloud SaaS across many organizations.

First deployment need:

- A clear deployment package and data boundary is more important than shared multi-tenancy.

## Architecture Steward View

Baseline impact:

- Option A preserves the current envelope and storage baseline.
- Options C and D touch envelope identity and require formal change control.

New invariants:

- Option A introduces only a guardrail: deployment context is outside the event envelope for initial atomization.
- It does not decide final cloud tenancy, tenant migration, or cross-tenant admin.

Coupling risk:

- Main risk is hidden coupling if sync, reporting, or configuration assume global cross-deployment visibility.
- Hold-back language must prevent deployment operations from redefining event identity or access scope.

Reversibility:

- Starting single-deployment runtime is reversible toward cloud multi-tenancy if storage/config/sync implementations keep deployment partitioning explicit outside event payloads.
- Adding tenant fields to immutable events is not easily reversible.

## Delivery Lead View

Implementation difficulty:

- Option A is significantly easier for a small delivery team using assisted drafting and implementation.
- It keeps initial deployment, backup, sync, admin, and reporting boundaries understandable.

Build order:

1. Specify initial single-deployment runtime guardrail.
2. Keep deployment packaging and configuration package identity visible as implementation/ops work.
3. Defer shared cloud multi-tenant runtime until self-host/single-deployment mechanics are proven.

Testing burden:

- Option A needs tests for no cross-deployment leakage only once multiple deployment contexts exist.
- Options B and C require broad isolation testing immediately.

Operational complexity:

- Option A keeps operational complexity low.
- Option B creates early operational burden around tenant isolation, migrations, observability, and support.

## Challenge Reviewer View

Hidden assumptions:

- "Single deployment" must be defined. It may mean one organization, one database, one runtime, one config namespace, or one legal data boundary.
- Self-hosted and cloud-managed single-tenant can share the same conceptual model if packaging is explicit.

Worst-case failure:

- The implementation accidentally hard-codes assumptions that make later cloud multi-tenancy impossible or unsafe.
- Reporting/admin tools accidentally read across deployments because deployment partitioning is not explicit in operational code.

What can be deferred:

- Shared-runtime SaaS tenancy.
- Cross-tenant admin.
- Tenant migration.
- Cross-deployment reporting.
- Managed hosting control plane.

What needs stakeholder or domain input:

- Whether first target deployment is self-hosted, managed single-tenant cloud, or shared cloud SaaS.
- Whether jurisdictional data residency is an immediate obligation.

## Proposed Decision

Initial atomization assumes one deployment context per runtime/database/configuration namespace. Deployment or tenant context stays outside the event envelope. Cloud multi-tenancy, cross-tenant administration, tenant migration, and shared-runtime hosting require a later dedicated architecture decision before implementation.

Outcome:

- Accepted as `PREOP-001` in `04-accepted-pre-atomization-decisions.md`.

## Explicit Deferrals

- shared cloud multi-tenant runtime
- cross-tenant admin/control plane
- tenant migration or split/merge
- cross-deployment reporting
- deployment packaging UX
- data residency implementation mechanics

## Reopen Trigger

Reopen this before implementation if the first target product must support multiple independent organizations inside one shared runtime or database.

Reopen this before atomizing deployment/ops specs if self-host packaging, data residency, or managed cloud operations require a stronger deployment identity contract.

## Required Follow-Up

- Carry deployment/tenancy as an explicit hold-back in the atomization plan.
- Do not add `tenant_id` or `deployment_id` to final event-envelope atoms without formal change control.
- If accepted, later implementation planning should define deployment partitioning outside event payloads.
