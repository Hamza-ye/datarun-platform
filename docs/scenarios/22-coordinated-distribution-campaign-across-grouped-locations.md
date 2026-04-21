# 22: Coordinated Work Across Grouped Locations

An organization plans a time-bound effort that requires visiting a set of locations, and within each location, reaching individual units — households, facilities, sites, or any other operational targets. Some units are already known from previous work; others are discovered during execution.

Field operators are assigned to locations and work through the units one by one. For each unit, they capture structured information and perform whatever operational action is needed — recording details, delivering items, making assessments. New units found during the work become part of the record for future use.

Progress is tracked at the location level: has work started, is it ongoing, is it complete. Completion depends on whether all known and newly discovered units have been handled. If work is incomplete, follow-up visits happen within the campaign period.

The scope of work may change during execution. New locations may be added, existing ones cancelled or reassigned to different operators. These changes are controlled by supervisory roles.

Some efforts involve distributing physical items alongside data collection. Items flow through a supply chain — from central storage to intermediate points to field operators — and each handoff is tracked separately. Operators receive items before fieldwork and return unused items afterward. The supply flow and the field work are related but tracked as separate activities.

This results in:

* A record of every unit visited (existing and newly discovered) and what was done there
* Clear progress status for each location
* Traceability of any distributed items from source to destination
* An expanded dataset that improves future operations

---

## What makes this hard

The set of units to visit is not fixed at the start. New units appear during execution, and each one needs the same level of attention and recording as pre-existing ones. Any system that assumes a fixed target list will under-count or miss coverage.

Location progress depends on what happened at the unit level — work done by a different person, on different subjects, at different times. Knowing whether a location is "done" requires looking across all unit-level work within it, not just at the location itself.

The supply flow and the field work intersect but follow different rhythms. Items move through multiple handoffs before reaching the field. Each handoff involves different actors with different responsibilities. Reconciling what was sent, what was received, and what was distributed to units requires tracking both flows and connecting them.

Reassignment during execution means that work started by one operator may be continued by another. The second operator needs to see what was already done — which units were visited, what was captured, what remains — without losing continuity.

Multiple operators may be assigned to the same location at the same time, each working through units independently. Without connectivity, neither knows which units the other has already visited. This creates the possibility of duplicate work — the same unit visited twice, with potentially different observations recorded. Prevention depends on how units are divided between operators before they go offline; detection and reconciliation happen only after they sync.

Offline work is pervasive. Operators visit units in areas with no connectivity, capturing data and distributing items. When they sync, their work may overlap with changes made centrally — new locations added, assignments changed, targets updated. The work they did offline under the old state must be reconciled with the new state without losing anything.
