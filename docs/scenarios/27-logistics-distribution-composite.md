# 27: Logistics Distribution Across Multiple Handoffs

A logistics team distributes supplies from a central warehouse to districts, then to field teams, and finally to local delivery points.

Each handoff needs confirmation. The sender records what left their custody, the receiver confirms what arrived, and discrepancies are followed up. Partial delivery is common: some items arrive late, damaged, or in a different quantity than expected.

Field teams continue distribution after receiving supplies. Supervisors need to know which items are still in transit, which have been received, which have been distributed, and where discrepancies remain unresolved.

This results in:

* Traceability from warehouse dispatch through field delivery
* Confirmation at every handoff
* Visibility into partial receipts and discrepancies
* Supervisory review when quantities or custody records do not match
* A complete operational history without relying on health-domain concepts

---

## What makes this hard

Supply movement is a chain of responsibility. Each step depends on the previous step, but people at different levels may work offline or sync at different times.

Discrepancies are not always simple errors. A district may receive less than expected because stock was split, damaged, delayed, or misrecorded. The record should preserve what each party observed rather than silently choosing one side.

Work can proceed while a discrepancy is being reviewed. Some downstream distribution may continue using confirmed received quantities, while the unresolved difference remains visible for oversight.

The same general coordination patterns must work without health-specific assumptions. The subjects are supplies, dispatches, teams, and delivery points rather than patients, facilities, or cases.
