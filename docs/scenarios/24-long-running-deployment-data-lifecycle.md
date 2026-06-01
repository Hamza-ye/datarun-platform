# 24: Long-Running Deployment Data Lifecycle

An organization has been using the platform for years. Devices and central records contain a long history of work across people, places, activities, and subjects.

Workers need current information for the work assigned to them now. Supervisors and auditors may need older history for review, handoff, or investigation. Coordinators need the system to remain usable even as records accumulate.

Responsibility changes over time. A worker may lose access to an area or subject, while still needing a traceable record of what they personally did earlier. Sensitive information that is no longer needed on a device should not remain there merely because it is hidden from the screen.

This results in:

* Active devices carrying the information needed for current work
* Older records remaining centrally traceable
* A clear distinction between current working data, retained personal work history, and audit history
* Sensitive out-of-scope information being handled intentionally

---

## What makes this hard

Operational history grows continuously. A low-end device cannot safely carry every record from a large deployment forever, but deleting the wrong information can break accountability or handoff.

Access changes do not erase the past. If a worker loses responsibility for a subject, their earlier work still happened and may need to remain attributable to them. At the same time, other people's sensitive records about that subject may no longer belong on the worker's device.

Audit needs are broader than daily work needs. An auditor may need to reconstruct history that normal field sync would not deliver to a worker. That reconstruction should be explicit and authorized, not an accidental side effect of ordinary field access.

Retention policies vary by sensitivity and deployment context. Routine operational records, personal records, and highly sensitive records may require different local handling.
