# 06: Maintaining a Known Set of Things

An organization keeps track of a recognized set of things — facilities, equipment, people, areas, or any other subjects it works with. These things are not static: new ones appear, existing ones change, and some eventually become inactive or irrelevant.

Changes to these things may need to be verified or approved before they take effect. It matters who changed what, when, and why. Some things need to be re-confirmed periodically to make sure the information about them is still accurate.

This results in:

* A maintained collection of known things and their current details
* A history of how each thing has changed over time
* Confidence that the current state of each thing is accurate and verified

---

# 06b: When the Shape of Information Changes

The kind of information being collected may change over time — new details become relevant, old ones are retired, or the way things are described evolves.

When this happens, work that's already in progress may still follow the old shape, while new work follows the updated one. Information recorded under the old shape must still be understandable and retrievable, even after the change.

This results in:

* The ability to evolve what information is collected without losing what was already recorded
* Clarity about which version of the expected information any given record was created under
* The ability to look across records from different periods, even when the expected details differed

---

## What makes this hard

When the shape of expected information changes, offline users may continue working under the old shape for days. Their work is valid — it followed the rules that were active on their device — but it arrives centrally after the rules have already changed. The system must accept both shapes and keep track of which rules each record was created under.

A thing in the registry may be deactivated or removed, but existing records, assignments, and ongoing work still reference it. Simply hiding the deactivated thing would break those references. The deactivation must be visible without making historical work that depended on it unintelligible.

Bulk changes — re-classifying many things at once, adding a new detail to an entire category, splitting one registry entry into several — create a large surface area for conflicts, particularly when they intersect with offline work that was based on the state before the change.