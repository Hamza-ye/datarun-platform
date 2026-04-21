# Operational Context and Constraints

> What the platform must work within — the realities that bound every architectural decision.

This document describes the operational environment the platform will serve. These are boundary markers, not design choices. They constrain the solution space without prescribing what the solution looks like.

---

## Who Uses This

The people the platform serves fall into broad tiers based on how they work, not what domain they work in.

**Field-level workers** do the primary operational work — recording observations, delivering things, following up on cases, carrying out assigned tasks. They work in the field, often alone, often in areas with poor infrastructure. They typically use low-end Android phones. Their digital literacy ranges from basic to moderate. They may work in their local language. They are the highest-volume users.

**Supervisors and team leads** oversee field-level work — reviewing what was done, assessing quality, making decisions about what happens next. They may work partly in the field and partly from an office or meeting point. They use phones or tablets, sometimes laptops. Their literacy is moderate to high. They need to see across the work of multiple field workers.

**Coordinators and administrators** manage the operational setup — defining what information gets collected, who is responsible for what, how oversight works, what gets reported. They work from offices or regional hubs with more reliable infrastructure. They use laptops or desktops. They need to see across large scopes and make structural changes.

**Auditors and external reviewers** need periodic or targeted access to verify that work was done correctly, that records are trustworthy, and that operational rules were followed. Their access patterns cut across normal organizational boundaries.

---

## Connectivity

Connectivity is the single most pervasive operational constraint.

**Field level**: Frequently offline for hours to days. Connectivity, when available, is often 2G or intermittent 3G. Work cannot depend on a connection being present. Everything a field worker needs to do — capture, look up, decide — must be possible without any network.

**Supervisor level**: Intermittent connectivity. Supervisors may be online at a district hub but offline when visiting field sites. They sync when they can but cannot assume continuous access.

**Coordination level**: Generally reliable broadband. However, real-time access to field data is inherently limited by when field workers sync. Oversight views reflect the most recently synced state, not a live feed.

The platform must never require connectivity for primary field operations. Sync is how offline work becomes centrally visible, but the work itself happens independently of sync.

---

## Scale

These are order-of-magnitude ranges, not precise targets. They describe the operational envelope the platform must be designed to handle.

- **Users**: Tens of thousands of active field workers in a large deployment. Hundreds of supervisors. Tens of coordinators and administrators.
- **Records**: Millions of individual records across an active deployment. Growth is continuous — operational data accumulates, it is rarely deleted.
- **Organizational depth**: 3 to 6 levels of hierarchy (e.g., field worker → supervisor → district coordinator → regional lead → national). The number of levels varies by organization.
- **Concurrent activities**: An organization may run multiple operational activities simultaneously, each with its own information shape, assignments, and oversight rules — all within the same platform.

---

## Data Sensitivity

The platform handles information that ranges from routine to highly sensitive, depending on the domain and the deployment.

**Personal information**: Field operations often involve recording details about identifiable individuals — their health, their household, their location, their participation in programs. This information is subject to data protection requirements that vary by jurisdiction.

**Operational accountability**: Who did what, when, and under what authority must be preserved as a trustworthy record. This includes corrections and amendments — the original and the change must both be traceable.

**Jurisdictional boundaries**: Data may be subject to residency requirements (must be stored in-country), consent requirements (recorded subjects must have agreed), and audit requirements (records must be producible on request). The specific requirements vary, but the platform must be able to accommodate them without per-deployment custom development.

The platform does not need to enforce specific regulatory frameworks. It needs to provide the mechanisms — access control, audit trails, data partitioning — that allow deploying organizations to meet their own compliance obligations.

---

## Interoperability

The platform will not operate in isolation. Organizations using it will also use other systems — national health information systems, supply chain tools, reporting platforms, government databases.

**The constraint**: The platform must be capable of exchanging data with external systems. This means structured export and import of records, not necessarily real-time integration. The specific external systems vary by deployment and domain.

**What this is not**: This is not a scenario. It is a boundary condition. The platform does not need to solve interoperability in Phase 1, but its internal data model and record structure must not make future interoperability impossible.

---

## Responsiveness Expectations

Different tiers of work have different expectations for how quickly things happen.

**Capture**: Immediate. Recording information must feel instant, regardless of connectivity. There is no acceptable delay between a field worker deciding to record something and being able to do so.

**Sync**: Opportunistic. When connectivity is available, sync should complete within reasonable time windows — minutes, not hours. But sync timing is not under the platform's control; it depends on when connectivity appears.

**Oversight and reporting**: Eventually consistent. Supervisors and coordinators see the latest synced state. They accept that the view may be hours or days behind field reality. The platform must make the age of information visible — "last synced 3 hours ago" — so that decisions account for the delay.

**Configuration changes**: Propagated on next sync. When a coordinator changes what information is collected or who is assigned to what, the change reaches field devices on their next sync. Work in progress under the old configuration completes under the old rules; new work follows the new configuration.
