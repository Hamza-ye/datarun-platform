> **Superseded.** The primitive mappings in this document were revised through ADR-3, ADR-4, and ADR-5. The current primitives inventory is in [architecture/primitives.md](../architecture/primitives.md). The scenario walk-throughs below remain valid as event-level traces.

## Walk-through: S04 (Review and Judgment by Another Person)

### The story as user actions

1. CHV captures a record (fills in observation details, saves)
2. The record enters a "waiting for review" state
3. The supervisor sees it in their pending-review queue
4. Supervisor opens it, examines, decides: accept / return for correction / flag concern
5. The decision is recorded — who, when, what they decided
6. If returned: CHV sees it back, makes correction, resubmits → back to step 3
7. Delays in review are visible (how long has this been waiting?)

### Pointing at existing primitives

| User action | Primitive | How |
|---|---|---|
| CHV captures a record | Event Store | `record_captured` event — same as S00 |
| Record enters "waiting for review" | Projection Engine | The projection of this subject now shows status=pending_review. No new event needed — the *absence* of a review event after a capture event implies "waiting" |
| Supervisor sees pending-review queue | **??? — no home yet** | Something must query: "all subjects where the projection shows pending_review and the assigned reviewer is this supervisor." This is a *query across projections filtered by role.* |
| Supervisor reviews and decides | Event Store | `review_completed` event with decision payload — the spike already has this |
| The decision is part of the record | Projection Engine | Projection now includes `_review_status`, `_reviewed_by` — spike already demonstrated this |
| If returned, CHV sees it back | **??? — same gap** | CHV needs to see "subjects returned to me for correction." Same cross-projection query filtered by role + status |
| Delay visibility | **??? — no home yet** | "How long since the capture event with no review event?" This is a temporal query across subjects |

### What emerged: new needs

**Need 1: Cross-subject queries filtered by actor and status.**

The projection engine computes state *per subject*. But the supervisor needs to see *all subjects in a certain state assigned to them*. This requires:

- Either: an index over projections (a secondary read model: "all subjects where status=X and reviewer=Y")
- Or: the projection engine maintains per-actor views alongside per-subject views

This isn't a new primitive — it's a **capability the projection engine needs**: the ability to maintain indexed views, not just per-subject state. The invariant is still "derived from events." But the projection is now more than a simple `subject_id → state` map.

**Need 2: Assignment — who reviews what.**

When the CHV captures a record, how does the system know which supervisor should review it? The scenario says "work done by one person is reviewed by another." This implies:

- There's a relationship: CHV → supervisor
- Or: there's a rule: "records captured in zone X go to supervisor Y"

This is an **assignment** concept. It's not an event. It's not a projection. It's *configuration* — a rule that exists before any data is captured. The spike has no home for this. Is it a primitive (it enforces its own invariant: "every record must have an assigned reviewer") or is it part of the configuration system (ADR-4)?

**Candidate: Assignment Resolver.** Given a subject and an action context, determines who is responsible for the next step. Its inputs are configuration rules + the subject's projected state. Its invariant: assignment rules are always evaluable on-device without network.

Status: **candidate** — might be a primitive, might be a capability of the configuration interpreter. ADR-3 (who sees what) and ADR-4 (how activities are configured) will settle this.

**Need 3: Status as an emergent property vs. explicit tracking.**

In the spike, "pending review" is implicit — there's a capture event but no review event. That works. But the scenario says "delays are noticeable" — someone needs to query "how long has this been pending?" That requires knowing *when* the status changed to pending. For "pending_review," the timestamp is the capture event's timestamp. But for "returned for correction," the timestamp is the review event's timestamp. The projection engine can compute this, but it needs to track *when each status began*, not just *what the current status is*.

This is a projection concern — not a new primitive. But it shapes how projections are structured: they need state-change timestamps, not just current values.

### Primitives map after S04

| Primitive | Invariant | Status |
|---|---|---|
| Event Store | Events immutable, IDs unique, sole write path | **Settled** |
| Projection Engine | Current state = f(events), rebuildable | **Settled** — but now we know it needs indexed views and state-change timestamps |
| Subject Identity | Each real-world subject has one canonical ID | **Open** (ADR-2) |
| Conflict Detector | ??? | **Candidate** (ADR-2) |
| Scope Resolver | ??? — what events belong on which device | **Open** (ADR-3) |
| Shape Registry | ??? — payload schemas per event type | **Open** (ADR-4) |
| Assignment Resolver | Assignment rules evaluable on-device | **Candidate** — might be ADR-3 or ADR-4 |

---

## Walk-through: S08 (Case Management — following something over time)

### The story as user actions

1. A problem is identified — someone creates a "case" (a subject that needs ongoing attention)
2. Over days/weeks, multiple people interact with it: visits, decisions, notes, referrals
3. Responsibility shifts between people at different points
4. Each interaction adds to the record
5. The case stays "active" until explicitly resolved
6. At any point, anyone involved can see the full history
7. Overdue/waiting-too-long cases are visible

### Pointing at existing primitives

| User action | Primitive | How |
|---|---|---|
| Identify a problem (create a case) | Event Store | `case_opened` event. The subject_id is the case. |
| Record an interaction (visit, note) | Event Store | `interaction_recorded` event with subject_id = case ID. |
| Make a decision | Event Store | `decision_made` event referencing the case. |
| Transfer responsibility | Event Store | `responsibility_transferred` event — from actor A to actor B |
| View full history | Event Store directly | Read all events for this subject_id — the spike's `get_events_for_subject` already does this |
| View current state | Projection Engine | Project all events → current status, current responsible person, last interaction date, etc. |
| Case is resolved | Event Store | `case_resolved` event |
| See overdue cases | **??? — same gap as S04** | Cross-subject query: "active cases where last interaction > N days ago" |

### What emerged: new needs

**Need 4: A subject that accumulates many events over a long time.**

In S00, a subject had 5 events (capture, correction, review). In S08, a case might have 50-100 events over months: multiple visits, notes, decisions, transfers, partial resolutions.

This stresses the projection engine differently. The `project()` function in the spike walks all events sequentially. At 100 events, is that still fast enough on a low-end Android device? Probably yes for one subject. But what about 200 active cases × 50 events each = 10,000 events to project? That's where the S00 spike's question 5 becomes real.

This isn't a new primitive — it's a **scaling property of the projection engine**. The response is probably: materialized projections that update incrementally (process only new events since last projection), not full rebuild every time. The invariant doesn't change ("projection = f(events)"), but the implementation strategy does.

This is a concrete candidate for a follow-up spike experiment: create a subject with 100 events, measure projection rebuild time, then implement incremental update and measure again.

**Need 5: Responsibility transfer as a first-class concept.**

In S04, assignment was static: CHV captures, supervisor reviews, relationship is preconfigured. In S08, responsibility *moves*: person A identifies the case, person B handles the first follow-up, person A takes it back, person C gets involved for a specialist decision.

Each transfer is an event (`responsibility_transferred`). The projection tracks "who is currently responsible." But who is *allowed* to transfer? What are the rules? Can any involved person transfer to anyone, or are there constraints?

This reveals that the **Assignment Resolver** candidate from S04 isn't just "who reviews this" — it's "who is responsible for this subject right now, and who can change that." It computes assignment based on events (transfers) and rules (configuration). The events are in the event store. The rules are configuration. The resolver sits between them.

Stronger signal now that this is a real primitive, not just a capability.

**Need 6: Subject lifecycle phases.**

A case has phases: opened → active → resolved (and possibly: reopened → active → resolved again). Different rules apply in different phases — you can't add interactions to a resolved case without reopening it first.

The projection engine can compute the current phase from events. But who *enforces* "you can't add an interaction to a resolved case"? Is that:

- A validation rule in the write path (before writing the event, check the projection)?
- A projection-layer concern (the projection rejects events that violate phase rules)?
- A separate primitive (a state machine that governs what events are valid given the current state)?

This is the question ADR-5 (workflow/state progression) must answer. But we now see it concretely: the event store is append-only and accepts anything. *Something* must sit between the user action and the event store to enforce "this action is not valid right now." Where does that live?

**Candidate: Command Validator.** Given a proposed action and the subject's current projected state, determines whether the action is allowed. Enforces lifecycle rules, phase constraints, authorization. Sits before the event store — the gatekeeper.

This is a significant finding. The spike's `write_event()` is unconditional — it writes anything. Real systems can't do that. Something must validate before writing. That something needs access to the current projection (to know the subject's state) and to configuration (to know the rules). It's a primitive because it enforces an invariant: **no event is written that violates the subject's lifecycle rules.**

### Primitives map after S08

| Primitive | Invariant | Status | Emerged from |
|---|---|---|---|
| Event Store | Events immutable, IDs unique, sole write path | **Settled** | ADR-1 + spike |
| Projection Engine | Current state = f(events), rebuildable; needs indexed views, state-change timestamps, incremental updates | **Settled** (core invariant) / **Open** (capabilities) | ADR-1 + spike + S04 + S08 |
| Subject Identity | Each real-world subject has one canonical ID | **Open** (ADR-2) | ADR-1 |
| Conflict Detector | ??? | **Candidate** (ADR-2) | ADR-1 exploration |
| Command Validator | No event is written that violates lifecycle rules / authorization | **Candidate** — needs ADR-3 (auth) + ADR-5 (lifecycle) | **S08** |
| Assignment Resolver | Current responsibility is always computable on-device | **Candidate** — might be part of projection, might be standalone | S04 + **S08 strengthened it** |
| Scope Resolver | Determines what events belong on which device | **Open** (ADR-3) | ADR-1 exploration |
| Shape Registry | Defines payload schemas per event type | **Open** (ADR-4) | ADR-1 exploration |

---

## What the walk-throughs proved

1. **The event store and projection engine held.** Every data-write action in both scenarios mapped to "write an event." Every data-read action mapped to "query a projection." The core decomposition from ADR-1 is load-bearing.

2. **Two new candidate primitives emerged:**
   - **Command Validator** (from S08) — the gatekeeper between user intent and the event store
   - **Assignment Resolver** (from S04, strengthened by S08) — computes "who is responsible" from events + configuration rules

3. **The projection engine needs more than the spike showed.** Indexed cross-subject queries, state-change timestamps, incremental updates. The invariant holds but the capabilities list grew.

4. **Nobody new appeared for the event store.** It didn't gain new responsibilities. It stayed lean: accept events, store immutably, serve by subject. Good sign — the core primitive is stable.

---

## Part 2: The proven methodologies for your current state

You asked me to name the systematic approaches and when they apply. Here's what maps to your project's exact situation — a conceptual architecture with constraints but no implementation:

### Where you are now

You have: vision, scenarios, constraints, one committed ADR, one spike, and a primitives map with 8 entries (2 settled, 2 candidate, 4 open). You don't have: code, a team, an implementation, or users.

### The three methods that apply, in order

**1. Domain-Driven Design — Tactical Design (Evans, 2003)**

*What it is:* Identify aggregates (primitives that enforce consistency boundaries), entities (things with identity over time), value objects (things defined by their attributes), and domain events (things that happened).

*Why it applies now:* You're doing exactly this — the primitives map IS an aggregate map. The walk-throughs are aggregate discovery. "Subject" is an entity. "Event envelope" is a value object. The scenario events (`record_captured`, `review_completed`) are domain events.

*What to read:* Evans' "Domain-Driven Design" Part II (chapters 5-6: Entities, Value Objects, Aggregates, Repositories). Skip Part III (large-scale structure) — you're not there yet. ~60 pages. Alternatively, Vaughn Vernon's "Implementing Domain-Driven Design" chapters 7 (Aggregates) and 8 (Domain Events) — more practical, more modern.

*When to apply:* **Now, through the ADR-2 and ADR-3 explorations.** Each ADR exploration is aggregate discovery for its domain area.

**2. Event Storming (Brandolini, 2013)**

*What it is:* A workshop technique where you map domain events on a timeline, then discover commands (what triggers the event), actors (who performs the command), aggregates (what validates and emits the event), read models (what projections people need), and policies (rules that trigger further commands when events occur).

*Why it applies now:* Your scenario walk-throughs are a solo version of event storming. The walk-throughs above discovered commands ("transfer responsibility"), aggregates (Command Validator), read models (indexed projections), and policies (review assignment rules). Event storming gives this a systematic structure instead of ad-hoc discovery.

*What to read:* Alberto Brandolini's "Introducing EventStorming" — specifically the "Big Picture" and "Process Modeling" chapters. Or the free summary at eventstorming.com. The core technique takes 30 minutes to learn.

*When to apply:* **Before each ADR exploration, as the discovery method.** Instead of starting an ADR exploration by listing "what must be decided" in the abstract, start by event-storming the scenarios that stress-test that ADR. The events, commands, aggregates, and policies that emerge from the storming become the sub-decisions of the exploration.

*For a solo practitioner:* Use colored sticky notes or a digital board. Orange = event. Blue = command. Yellow = actor. Pink = aggregate. Purple = policy. Walk one scenario at a time. The primitives map is the output.

**3. Decisions Under Uncertainty — "Last Responsible Moment" (Poppendieck, 2003) + "Real Options" (Matts/Maassen, 2007)**

*What it is:* A decision-making framework: commit to irreversible decisions as late as possible, but create options that let you defer without blocking progress. A decision should be made when the cost of *not deciding* exceeds the value of *keeping the option open*.

*Why it applies now:* This is the framework for your "ADR vs. strategy" question. You already intuited it — the boundary test (stored state = constraint, interpretation logic = strategy) is a practical implementation of last-responsible-moment thinking. But the formal framework gives you the language to defend decisions: "We committed to events because the cost of migrating stored data later exceeds the value of keeping the option open. We deferred conflict resolution policies because the cost of not deciding is zero — the events store every conflict, and we can change the resolution policy without data migration."

*What to read:* Mary and Tom Poppendieck's "Lean Software Development" chapter 3 (Decide As Late As Possible). Chris Matts' "Real Options" paper (short, available free online). Both < 1 hour of reading.

*When to apply:* **Every time you feel the tension between committing and deferring.** Before you write anything into an ADR, ask: "What is the cost of not deciding this now? What option am I closing?" If the cost of deferring is low and the option is valuable, defer. If the cost of deferring is growing (other decisions are blocked), commit.

### The sequence for your next two weeks

| Step | Method | Output |
|---|---|---|
| Event-storm S04, S07, S08 against the primitives map | Event Storming | Updated primitives map, concrete questions for ADR-2 |
| ADR-2 exploration using the framework template | DDD tactical design for identity + conflict | ADR-2 directional lean, primitives map grows |
| Spike: subject with 100 events, projection rebuild, incremental update | Prototype | Grounded answer to "does projection scale?" |
| ADR-2 → ADR decision using boundary test | Last Responsible Moment | ADR-2 written (constraints only, strategies labeled) |
| Repeat for ADR-3 | Same cycle | Sync and auth primitives settled |

Each step grounds the next. You never go more than one ADR ahead of concrete experience. The primitives map grows with each cycle. The ball of mud is avoided because every primitive earns its place through a stated invariant and a scenario that required it.