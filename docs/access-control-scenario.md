# Access Control and Visibility

> Who can see what, do what, and under what circumstances?

This is not a numbered scenario — it's a cross-cutting concern that applies across most operational situations the platform supports.

---

## The Reality

In any operational environment, different people have different levels of visibility and authority. What someone can see and do depends on a combination of factors — who they are, what role they play, where they work, what activity they're involved in, and sometimes when.

These factors intersect. A person might have authority to act in one context but only observe in another. They might have access to certain subjects in their area but not in a neighboring one. Two people working in the same area on different activities might see entirely different things.

### What must hold true:

**People only see and act on what's appropriate to their role and context.**
A field worker sees the things relevant to their area and responsibilities. A supervisor sees what their team is doing. A regional lead sees across a broader scope. An auditor may cut across the normal hierarchy with special access.

**Authority is contextual, not absolute.**
Having a role doesn't mean having the same authority everywhere. A person's authority may vary depending on which activity they're participating in, which area they're operating in, or which subjects they're working with.

**Access can be temporary.**
People cover for each other. Campaigns grant time-limited authority. Emergency situations may expand access temporarily. When the reason for temporary access ends, the expanded access should end too. But everything done during that period remains on record.

**Changes in role or responsibility are handled gracefully.**
People get promoted, transferred, or go on leave. Work they were doing doesn't disappear when they move — it either stays with them through a transition period or is handed off to someone else. The record of who did what under which role at the time of action is preserved.

**Hierarchical visibility follows organizational structure — with exceptions.**
Generally, a supervisor can see what their reports are doing. A regional lead can see what supervisors in their region are doing. But this inheritance can be overridden — for example, a special-purpose auditor or a cross-regional coordinator who needs to see across normal boundaries.

**The rules can grow over time.**
An organization might start with simple rules: this role can do these things in this area. Over time, they might need finer distinctions — access that varies by activity, by time window, or by the sensitivity of specific information. Adding these finer distinctions should not require rethinking or rebuilding the rules that already exist.

---

## What this results in:

* People see and act on only what's relevant to their responsibilities
* The same underlying information can be appropriately partitioned for different audiences
* Temporary access grants and revocations are handled cleanly
* Role changes don't create gaps in responsibility or orphaned work
* The rules governing access can become more nuanced over time without breaking what already works
* Every action is attributable to a specific person acting in a specific role at a specific time

---

## Where this gets hard

The principles above interact with specific operational situations in ways that create real architectural pressure.

**Temporary authority under time pressure** (scenarios 09, 16): A coordinated campaign or emergency response may need to grant expanded authority to many people quickly, then revoke it cleanly when the situation ends. The grant and the revocation may happen while some of those people are offline — they may continue acting under expanded authority after it was centrally revoked, and that work still needs to be attributed correctly.

**Contextual authority that varies by step** (scenarios 04, 11): In review and multi-step approval, a person may have authority to act at one step but only observe at another. The same person may be a reviewer in one activity and a subject of review in another. Authority is not just role-based — it is role-plus-context, and the context changes at each step.

**Hierarchical visibility with exceptions at every level** (scenario 14): Multi-level distribution creates visibility expectations at each handoff point. A regional lead should see what left the warehouse, what arrived at each district, and what hasn't been accounted for. But exceptions — a special-purpose auditor who needs to see across regions, or a field worker who temporarily acts as a distribution point — cut across the normal hierarchy and must be accommodated without undermining it.

**Access decisions that must hold offline** (scenario 19): When a field worker is disconnected, the device must enforce access rules locally. But the rules may have changed centrally since the last sync — a person's role may have been revoked, a subject may have been reassigned, a temporary grant may have expired. The device enforces the last-known rules, and discrepancies are reconciled on sync. This means access control has two enforcement points (local and central) that may temporarily disagree.
