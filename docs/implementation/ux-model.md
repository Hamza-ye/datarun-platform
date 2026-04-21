# Field-User UX Model

> Structural UX architecture for the Datarun mobile app. Screen topology, navigation flow, component contracts, and progressive disclosure strategy. Every recommendation traces to an architectural constraint or a scenario requirement. Designed for Flutter implementation on low-end Android (8–16GB, 2G/intermittent 3G).

---

## 1. Screen Topology

### Decision: 3 screen types + 1 utility surface

| # | Screen | Purpose | Lifecycle |
|---|--------|---------|-----------|
| S1 | **Work List** | Subject-centric entry point. Shows all subjects in scope, grouped/filtered by activity and state. Landing screen. | Always alive — root of navigation stack |
| S2 | **Subject Detail** | Hub for a single subject. Timeline, current state, available actions. | Pushed onto stack from S1 |
| S3 | **Form** | Full-screen event creation. Shape-driven fields. Produces exactly one event. | Pushed onto stack from S1 or S2 |
| U1 | **Sync Panel** | Utility overlay (bottom sheet). Sync trigger, status, staleness. | Modal surface over any screen |

### Why 3, not 2 or 4

**Why not 2** (collapsing Subject Detail into Work List via tabs/expansion):
- Subject Detail must show a variable-length event timeline + multiple available actions + state badge + metadata. This does not fit as an expansion panel in a list, especially on 5" low-end screens. **STRUCTURAL** — derived from the event-sourced model where "current state" includes the full event history, not just a snapshot.
- Supervisors reviewing submissions need to see the capture event's content alongside the subject's history before making a judgment (S04). Inline expansion cannot hold this.

**Why not 4** (separate Inbox/Alerts screen):
- Alerts (Phase 4) are always about a subject. They surface as badges on subjects in the Work List and as timeline entries in Subject Detail. A separate Inbox screen would break V4 ("one coherent system") by creating a parallel navigation path. If volume warrants it in Phase 4+, a filter preset on Work List ("Show: Pending Alerts") handles this without adding a screen type. **RECOMMENDED** — an Inbox screen is a valid alternative if alert volume is extreme, but the filter approach preserves the 3-screen model.

**Why the Sync Panel is not a screen**:
- Sync is a utility action, not a content destination. It has no subject context, no event creation, no navigation depth. A bottom sheet or overlay keeps sync accessible from any screen without disrupting the navigation stack. **RECOMMENDED** — a dedicated settings screen is a valid alternative if sync gains complexity (e.g., selective sync per activity), but overkill for Phase 0–2.

### How both roles use the same 3 screens

| Flow | Field worker (S00) | Field worker (S08) | Supervisor (S04) |
|------|--------------------|--------------------|--------------------|
| Work List shows | Subjects in area, sorted by last visit | Active cases, grouped by state | Subjects with pending reviews, grouped by worker |
| Subject Detail shows | Event timeline, "Capture" action | Case timeline, state badge, state-appropriate actions | Latest capture + review decision action |
| Form produces | `capture` event | `capture` event (interaction/referral/resolution shape) | `review` event with decision payload |

Same 3 screen types. Different content, driven by activity configuration and pattern state. **STRUCTURAL** — this follows from type vocabulary being processing behavior (6 fixed types), with domain meaning in shapes [4-S3].

---

## 2. Navigation Flow

### Model: Stack-based, hub-and-spoke

```
┌─────────────┐
│  Work List   │ ← always root (index 0 in stack)
│     (S1)     │
└──────┬───────┘
       │ tap subject
       ▼
┌─────────────┐
│   Subject    │
│   Detail     │
│     (S2)     │
└──────┬───────┘
       │ tap action
       ▼
┌─────────────┐
│    Form      │
│     (S3)     │
└──────────────┘
```

**STRUCTURAL** — Flutter's `Navigator` stack is the natural fit. No bottom tabs, no drawer navigation. Rationale:
- No simultaneous multi-screen state to maintain (saves memory on low-end devices)
- Every form submission is about exactly one subject — there's no cross-subject action
- "Go back" is always unambiguous: pop the stack

### Entry points

| Entry | Target | When |
|-------|--------|------|
| App launch | S1 (Work List) | Always. No onboarding wizard in Phase 0. |
| "Add New" on Work List | S3 (Form) | Creating a new subject. `subject_ref` auto-generated (client UUID). Skips S2 because there's no history to show. |
| Tap subject on Work List | S2 (Subject Detail) | Viewing/acting on existing subject. |
| Tap action on Subject Detail | S3 (Form) | Performing an action on existing subject. |

### "Add New" shortcut flow

```
S1 (Work List) ──"Add New"──→ S3 (Form, new subject_ref)
                                      │
                                      │ save
                                      ▼
                               S1 (Work List) ← pop to root
```

The "Add New" flow pushes Form directly onto the stack with `skip_detail = true`. On save, the stack pops back to Work List, where the new subject now appears in the list. The user can then tap it to see Subject Detail. **RECOMMENDED** — an alternative is to push to an empty Subject Detail first, but this adds a pointless intermediate screen.

### Standard flow (existing subject)

```
S1 ──tap──→ S2 ──action──→ S3
                              │
                              │ save
                              ▼
                           S2 (refreshed) ← pop one
```

On save, Form pops back to Subject Detail, which refreshes its projection state to show the new event in the timeline and updated state badge. **STRUCTURAL** — Form outputs an event to the local Event Store; Subject Detail reads from the Projection Engine. The refresh is a projection re-derivation, not a data pass between screens.

### "Go back" behavior

| From | Back action | Result |
|------|-------------|--------|
| S3 (Form) — unsaved | System back / app bar back | Discard confirmation dialog, then pop |
| S3 (Form) — saved | (automatic) | Pop to S2 or S1 |
| S2 (Subject Detail) | System back / app bar back | Pop to S1 |
| S1 (Work List) | System back | Exit app (standard Android behavior) |
| U1 (Sync Panel) | Tap outside / swipe down / back | Dismiss overlay |

**STRUCTURAL** for discard confirmation — an unsaved form may contain significant data entry that can't be recovered (no draft persistence in Phase 0).

### Three scenarios through the same model

**S00: Basic capture (no workflow, no review)**
```
S1 → tap "Amina's Household" → S2 (timeline: 2 previous captures)
  → tap "Capture" → S3 (basic_capture/v1 form)
  → fill fields → save → S2 (now shows 3 captures)
  → back → S1

S1 → tap "Add New" → S3 (basic_capture/v1 form, new subject)
  → fill fields → save → S1 (new subject appears)
```

**S08: Case management (stateful, multi-step)**
```
S1 (grouped: opened=2, active=5, referred=1)
  → tap "Case #412 - Maria" → S2 (state: active, timeline: opening + 3 interactions)
    → actions shown: [Follow-up, Refer, Record Resolution]
    → tap "Follow-up" → S3 (interaction shape)
    → fill fields → save → S2 (state: still active, timeline: +1 interaction)
    → back → S1

S1 → tap "Add New Case" → S3 (opening shape, new subject)
  → fill fields → save → S1 (new case appears under "opened" group)
```

**S04: Supervisor review**
```
S1 (grouped: pending_review=12, reviewed=45)
  → tap "Amina's Household" → S2 (timeline: capture event pending review)
    → actions shown: [Review]
    → tap "Review" → S3 (review_decision shape, source_event_ref set)
    → select Accept/Return + notes → save → S2 (event now shows "accepted" badge)
    → back → S1 (pending_review count decremented)
```

Same navigation model in all three. The only differences are:
1. What the Work List groups by (activity + pattern config)
2. What actions Subject Detail offers (Action Resolver output)
3. Which shape the Form renders (determined by the selected action)

---

## 3. Screen Contracts

### S1: Work List

**Input** (what it reads to render):

| Input | Source | Phase 0 | Full |
|-------|--------|---------|------|
| `subject_list` | Projection Engine: all subjects in local store | All subjects (no scope filter) | Filtered by Scope Resolver output |
| `per_subject_summary` | Projection Engine: latest state per subject | `{subject_ref, latest_timestamp, capture_count}` | `+ {current_state, pending_action_count, flag_count, activity_ref}` |
| `activity_config` | Config consumer: current activity definition | Single hardcoded activity | Multiple activities from config package |
| `grouping_strategy` | Derived from pattern + activity config | None (flat list) | Group by state, by worker, by area |
| `sync_status` | Sync service: last sync time, pending count | `{last_sync, pending_event_count}` | Same |

**Output** (what it produces):

| Output | Type |
|--------|------|
| Navigation to S2 | `NavigateTo(SubjectDetail, subject_ref)` |
| Navigation to S3 (new subject) | `NavigateTo(Form, {subject_ref: new_uuid(), shape_ref, event_type, skip_detail: true})` |
| Sync trigger | `SyncRequested` → delegated to sync service |

**Content slots**:

```
┌──────────────────────────────────┐
│ [Activity Selector]  [Sync ●3]  │  ← header bar
├──────────────────────────────────┤
│ [Search / Filter bar]           │  ← OPEN: search by name, filter by state
├──────────────────────────────────┤
│ ┌─ Group Header ──────────────┐ │
│ │ Active (5)                  │ │  ← from grouping_strategy (Phase 4)
│ ├─────────────────────────────┤ │
│ │ ● Subject Name              │ │
│ │   Secondary info   [badge]  │ │  ← per_subject_summary
│ │ ● Subject Name              │ │
│ │   Secondary info   [badge]  │ │
│ └─────────────────────────────┘ │
│ ┌─ Group Header ──────────────┐ │
│ │ Pending Review (3)          │ │
│ │ ...                         │ │
│ └─────────────────────────────┘ │
├──────────────────────────────────┤
│        [+ Add New]               │  ← FAB or bottom action
└──────────────────────────────────┘
```

| Slot | Phase 0 | Phase 1 | Phase 2 | Phase 3 | Phase 4 |
|------|---------|---------|---------|---------|---------|
| Activity Selector | Hidden (one activity) | Hidden | Hidden | Visible if >1 activity | Same |
| Sync indicator | `●N` pending badge | Same | Same | Same | Same |
| Search bar | Name search only | Same | Same | Same | + state filter |
| Group headers | None (flat list) | Same | Same | Same | Grouped by pattern state |
| Subject card — primary | Subject name/label | Same | Same | Same | Same |
| Subject card — secondary | Last capture date | Same | Same | Same | + state label |
| Subject card — badge | None | Flag indicator (●) | Same | Same | + state badge + pending action count |
| Add New button | Always visible | Same | Same | Same | Visible per activity config |

**STRUCTURAL**: Activity Selector hidden when count=1 is a direct application of P7 ("the simplest scenario stays simple"). The slot exists but renders nothing.

---

### S2: Subject Detail

**Input**:

| Input | Source | Phase 0 | Full |
|-------|--------|---------|------|
| `subject_ref` | Navigation parameter | UUID | Same |
| `subject_projection` | Projection Engine: full state for this subject | `{subject_ref, events: [{id, type, timestamp, payload}]}` | `+ {current_state, flags, alias_info, pattern_state}` |
| `available_actions` | Action Resolver output | `[{label, event_type, shape_ref}]` — hardcoded: one "Capture" action | Computed from pattern transitions + role + state |
| `activity_config` | Config consumer | Single hardcoded activity | Full activity definition |

**Output**:

| Output | Type |
|--------|------|
| Navigation to S3 | `NavigateTo(Form, {subject_ref, shape_ref, event_type, source_event_ref?})` |

**Content slots**:

```
┌──────────────────────────────────┐
│ ← Back     Subject Name         │  ← app bar
├──────────────────────────────────┤
│ [State Badge]   [Flag Indicator] │  ← subject status header
│ Assigned to: Worker Name         │
│ Since: 2026-01-15                │
├──────────────────────────────────┤
│ ┌─ Event Timeline ─────────────┐ │
│ │ ▼ Mar 15 — Capture           │ │  ← most recent first
│ │   basic_capture/v1           │ │
│ │   [payload summary]          │ │
│ │   [review badge: accepted ✓] │ │  ← Phase 4: review status
│ │   [flag: ● conflict]         │ │  ← Phase 1: flag indicator
│ │                               │ │
│ │ ▼ Mar 01 — Capture           │ │
│ │   [payload summary]          │ │
│ └─────────────────────────────┘ │
├──────────────────────────────────┤
│ [Action 1] [Action 2] [Action 3]│  ← action bar (from Action Resolver)
└──────────────────────────────────┘
```

| Slot | Phase 0 | Phase 1 | Phase 2 | Phase 3 | Phase 4 |
|------|---------|---------|---------|---------|---------|
| State badge | Hidden | Hidden | Hidden | Hidden | Pattern state label + color |
| Flag indicator | Hidden | Visible: flag count per subject | Same | Same | Same |
| Assignment info | Hidden (single user) | Hidden | Visible: assigned worker | Same | Same |
| Event timeline | Flat event list with payload summary | + flag badges on events | Same | Same | + review decision badges, + transition markers |
| Event detail | Tap to expand payload | Same | Same | Same | + source-chain link for reviews |
| Action bar | One button: "Capture" | Same | Same | Same | Dynamic: transitions valid from current_state for current role |

**STRUCTURAL**: The action bar is the single point where available actions are determined. It always uses the Action Resolver's output. This guarantees V4 consistency — whether the activity has no pattern (S00) or a complex state machine (S08), actions are presented the same way.

---

### S3: Form

**Input**:

| Input | Source | Phase 0 | Full |
|-------|--------|---------|------|
| `shape_ref` | Navigation parameter | `basic_capture/v1` | Any valid shape_ref |
| `shape_definition` | Shape Registry (local config) | JSON field definitions | Same |
| `event_type` | Navigation parameter | `capture` | Any of 6 types |
| `subject_ref` | Navigation parameter | UUID (existing or new) | Same |
| `source_event_ref` | Navigation parameter (optional) | Not used | Set for `review` events |
| `activity_ref` | Derived from current activity context | `null` | Activity UUID |
| `actor_ref` | Current user identity | Hardcoded single user | From auth context |
| `context_values` | Expression Evaluator pre-resolution (slot) | Empty map | 7 `context.*` properties |
| `expression_rules` | Shape definition L2 expressions (slot) | None | Show/hide, defaults, warnings |
| `validation_warnings` | Command Validator output (slot) | None | Transition validity advisory |

**Output**:

| Output | Type |
|--------|------|
| Event written to local store | Full 11-field envelope event in SQLite |
| Navigation intent | `Pop` (back to previous screen) |

**Content slots**:

```
┌──────────────────────────────────┐
│ ← Cancel   [Form Title]   Save  │  ← app bar
├──────────────────────────────────┤
│ [Command Validator Warning]      │  ← Phase 4 slot: advisory banner
├──────────────────────────────────┤
│ ┌─ Field List ─────────────────┐ │
│ │ Field Label 1                │ │
│ │ [input widget]               │ │  ← widget type from shape field type
│ │ [field-level warning]        │ │  ← Phase 3 slot: expression warning
│ │                               │ │
│ │ Field Label 2                │ │
│ │ [input widget]               │ │
│ │                               │ │
│ │ [conditionally shown field]  │ │  ← Phase 3 slot: show/hide
│ │ ...                          │ │
│ └─────────────────────────────┘ │
├──────────────────────────────────┤
│ [Review Source Event Preview]    │  ← visible when event_type = review
└──────────────────────────────────┘
```

| Slot | Phase 0 | Phase 1 | Phase 2 | Phase 3 | Phase 4 |
|------|---------|---------|---------|---------|---------|
| Form title | Shape display name | Same | Same | Same | Same |
| Command Validator banner | Hidden | Hidden | Hidden | Hidden | Advisory warning if transition is invalid |
| Field list | All shape fields, always visible | Same | Same | Expression-driven show/hide + computed defaults | Same |
| Field warnings | Required-field validation only | Same | Same | + expression-driven conditional warnings | Same |
| Review source preview | Hidden | Hidden | Hidden | Hidden | Shows source event payload when `event_type = review` |
| Save action | Writes event, pops | Same | Same | Same | Same (warnings are advisory, never blocking on-device) |

**STRUCTURAL for non-blocking save**: The Command Validator is advisory [5-S4, ADR-5]. The device never blocks event creation. Flags are created server-side [SG-1]. This means the Form's save action ALWAYS succeeds locally, regardless of state validity. The user sees a warning but can proceed.

**STRUCTURAL for shape-driven fields**: Fields are NEVER hardcoded in Flutter widgets. The Form Engine reads the shape definition and renders fields dynamically. This is the core of V2 ("set up, not built") on the device side.

---

## 4. Component Architecture

### Component map

```
┌────────────────────────────────────────────────────────┐
│                    PRESENTATION                        │
│                                                        │
│  ┌──────────┐   ┌───────────────┐   ┌──────────────┐  │
│  │Work List  │   │Subject Detail │   │    Form      │  │
│  │  Screen   │   │    Screen     │   │   Screen     │  │
│  └─────┬─────┘   └──────┬────────┘   └──────┬───────┘  │
│        │                │                    │          │
├────────┼────────────────┼────────────────────┼──────────┤
│        │           MEDIATION                 │          │
│        │                │                    │          │
│  ┌─────▼─────┐   ┌─────▼──────┐   ┌────────▼───────┐  │
│  │   List    │   │   Action   │   │  Form Engine   │  │
│  │  Adapter  │   │  Resolver  │   │                │  │
│  └─────┬─────┘   └─────┬──────┘   └────────┬───────┘  │
│        │                │                    │          │
├────────┼────────────────┼────────────────────┼──────────┤
│        │              DATA                   │          │
│        │                │                    │          │
│  ┌─────▼────────────────▼────────────────────▼───────┐  │
│  │              Projection Engine                    │  │
│  │         (per-subject state derivation)             │  │
│  └───────────────────────┬───────────────────────────┘  │
│                          │                              │
│  ┌───────────────────────▼───────────────────────────┐  │
│  │               Event Store (SQLite)                │  │
│  └───────────────────────┬───────────────────────────┘  │
│                          │                              │
│  ┌───────────────────────▼───────────────────────────┐  │
│  │             Sync Service                          │  │
│  └───────────────────────────────────────────────────┘  │
│                                                        │
│  ┌───────────────────────────────────────────────────┐  │
│  │         Config Consumer (shapes, patterns)        │  │
│  └───────────────────────────────────────────────────┘  │
└────────────────────────────────────────────────────────┘
```

### 4.1 Form Engine

The form engine is the central widget. It transforms a shape definition into a renderable field list and produces a valid event.

**Pipeline**:

```
shape_definition ──→ FieldResolver ──→ field_list ──→ WidgetMapper ──→ widget_tree
                         │                                │
                    [ExpressionSlot]                  [ValidationSlot]
                    (Phase 3: show/hide,              (Phase 0: required fields
                     computed defaults)                Phase 3: + expression warnings)
                                                         │
                                                    ┌────▼────┐
                                                    │ payload │
                                                    └────┬────┘
                                                         │
                            ┌────────────────────────────▼──────────────────┐
                            │ EventAssembler                                │
                            │ {id, type, shape_ref, activity_ref,           │
                            │  subject_ref, actor_ref, device_id,           │
                            │  device_seq, timestamp, payload} → Event Store│
                            └───────────────────────────────────────────────┘
```

**Components**:

| Component | Responsibility | Phase 0 | Full |
|-----------|---------------|---------|------|
| **FieldResolver** | Reads shape definition, produces ordered field list. Applies expression-driven show/hide. | Identity pass-through (all fields visible) | Evaluates L2 expressions for visibility, defaults |
| **WidgetMapper** | Maps field type → Flutter widget. Pure function. | Supports: `text`, `number`, `date`, `select`, `boolean` | + `multi_select`, `gps`, `barcode`, `photo` (as shapes demand) |
| **ValidationSlot** | Validates field values against shape constraints. | Required-field check only | + type validation, range checks, expression-driven conditional warnings |
| **ExpressionSlot** | Evaluates L2 expressions. Receives field values + `context.*`. | No-op (returns empty) | Full expression evaluator with `payload.*`, `entity.*`, `context.*` |
| **CommandValidatorSlot** | Checks whether this event's state transition is valid. Advisory only. | No-op | Evaluates transition against pattern state machine. Produces warning, never blocks. |
| **EventAssembler** | Constructs 11-field envelope from form state + navigation params. Writes to Event Store. | Full envelope with Phase 0 fixed values | Same envelope, dynamic values |

**STRUCTURAL**: FieldResolver and EventAssembler are mandatory from Phase 0. ExpressionSlot and CommandValidatorSlot are no-ops that take `Map → Map` (identity function) and `Event → List<Warning>` (empty list) respectively. They become real in Phase 3 and Phase 4. The interface exists from day one; the implementation is swapped.

**OPEN**: Whether WidgetMapper uses a registry pattern (`Map<FieldType, WidgetBuilder>`) or a switch statement. Both are valid for the field count budget (≤60 per shape [4-S13]).

### 4.2 List Adapter

Transforms raw subject projections into a grouped, sorted, displayable list.

**Pipeline**:

```
subject_projections ──→ GroupingStrategy ──→ grouped_list ──→ CardRenderer ──→ widget_list
                              │                                    │
                         [activity_config]                   [SummaryExtractor]
                         [pattern_config]                    (what shows on each card)
```

**Components**:

| Component | Responsibility | Phase 0 | Full |
|-----------|---------------|---------|------|
| **GroupingStrategy** | Determines sort order and group boundaries. | Flat list, sorted by `subject_name` or `latest_timestamp` | Grouped by `current_state` from pattern. Sort within group by `pending_since`. |
| **CardRenderer** | Renders a single subject card. | Subject name + last capture timestamp | + state badge + flag count + pending action indicator |
| **SummaryExtractor** | Picks which projection fields to show on the card. | `{name, latest_timestamp}` | `{name, current_state, pending_count, flag_count, last_interaction}` |

**STRUCTURAL**: GroupingStrategy must be driven by activity/pattern config, not hardcoded. In Phase 0, the "no pattern" activity produces a flat sort. In Phase 4, `case_management` produces state-based groups. The List Adapter receives a strategy, it does not decide one.

**RECOMMENDED**: Use a single `ListView.builder` with section headers inserted as list items. No nested scrolling. Performant on low-end devices.

### 4.3 Action Resolver

Pure function that determines available actions for a subject. This is the critical component for V4 (one coherent system).

**Signature**:

```
ActionResolver(
  subject_projection,   // current state from PE
  activity_config,      // pattern + role mappings
  user_role             // current user's role in this activity
) → List<Action>

Action = {
  label: String,        // display text (from shape or pattern config)
  event_type: EventType,// one of 6 types
  shape_ref: String,    // which shape to load in Form
  source_event_ref: String?  // set for reviews (references the event being reviewed)
}
```

**Resolution logic by phase**:

| Phase | Logic |
|-------|-------|
| 0 | Return `[{label: "Capture", type: capture, shape_ref: "basic_capture/v1"}]`. Always. |
| 1–3 | Same as Phase 0 (no patterns active yet). |
| 4 (no pattern) | Return one action per shape mapped to this activity. |
| 4 (with pattern) | Read `current_state` from projection. Look up valid transitions from current state in pattern definition. Filter transitions by `user_role`. For each valid transition: resolve `shape_role` → `shape_ref` via activity's shape mapping. Return as action list. |
| 4 (capture_with_review, supervisor) | For subjects with events in `pending_review` state: add `{label: "Review", type: review, shape_ref: review_decision_shape, source_event_ref: pending_event_id}`. |

**STRUCTURAL**: The Action Resolver is the ONLY component that determines available actions. Screens never hardcode action buttons. This ensures that adding a new pattern or changing a state machine never requires screen changes — only Action Resolver logic changes.

### 4.4 Projection Engine (device-side)

Per-subject state derivation from local events. Everything the UI displays comes from here.

**Interface**:

```
// Phase 0
getSubjectList() → List<SubjectSummary>
getSubjectDetail(subject_ref) → SubjectProjection

// Phase 4 additions
getSubjectState(subject_ref, activity_ref) → PatternState
getPendingReviews(subject_ref) → List<PendingReview>
```

**STRUCTURAL**: Projections are derived from events [1-S2]. The PE re-derives on:
1. Event written locally (Form save)
2. Events received from sync (pull)

Re-derivation strategy is implementation-grade [IG-1]. Phase 0: full replay per subject. Escape hatch: materialized views if performance degrades.

### 4.5 Sync Service

Manages push/pull cycle. Invoked manually in Phase 0.

**Interface**:

```
SyncResult = {
  pushed_count: int,
  pulled_count: int,
  errors: List<SyncError>,
  new_watermark: int
}

triggerSync() → SyncResult
getSyncStatus() → {last_sync: DateTime, pending_count: int, is_online: bool}
```

**STRUCTURAL**: Sync is manual-trigger only in Phase 0. Background sync is a future addition. The sync service interface is the same regardless — callers don't care who triggered it.

---

## 5. Offline UX Patterns

### 5.1 Offline is the default state

The app assumes offline. Online is a bonus. No screen requires connectivity to render or function. Every screen reads from local SQLite.

**STRUCTURAL** — this is V1 ("works without connectivity"). Not a graceful degradation strategy. The app is designed for offline first; online features (sync) are additive.

### 5.2 Sync button and status

```
┌─────────────────────────┐
│ Sync ● 3 pending        │  ← tappable, always visible in Work List header
│ Last sync: 2h ago       │
└─────────────────────────┘
```

| State | Indicator | Sync button behavior |
|-------|-----------|---------------------|
| **Offline, events pending** | `● N pending` (orange badge). `Last sync: Xh ago` | Tappable → attempts connection → fails → shows "No connection" toast |
| **Offline, nothing pending** | `Last sync: Xh ago` (no badge) | Same behavior |
| **Online, events pending** | `● N pending` (orange badge) | Tappable → opens Sync Panel (U1) → push/pull → shows result |
| **Online, nothing pending** | `Last sync: just now` (no badge) | Tappable → pull only → shows "Up to date" or "N new events received" |
| **Syncing** | Spinner + `Syncing...` | Not tappable (debounce) |

**RECOMMENDED**: The pending count (`N`) is the count of events with `pushed = 0` in the local Event Store. This is a real-time local query, not a guess.

### 5.3 Event creation while offline

```
User fills form → taps Save
  → EventAssembler creates full 11-field envelope
  → Event written to SQLite (pushed = 0)
  → Projection Engine re-derives subject state
  → UI refreshes: new event in timeline, updated summary
  → Toast: "Saved. Will sync when online."
```

| Concern | Behavior |
|---------|----------|
| Save always works | Local SQLite write. No network check. **STRUCTURAL** [1-S1]. |
| Event has all fields | 11-field envelope assembled on-device. `sync_watermark` = NULL until sync. **STRUCTURAL** [1-S5, Phase 0 scope]. |
| UI reflects change immediately | Projection re-derived from local events. User sees updated timeline and state. |
| No "draft" state | Events are either saved (in Event Store) or discarded (cancel/back). No intermediate persistence. **RECOMMENDED** — drafts add complexity; if forms are long, consider per-field autosave in a future phase. |

### 5.4 Sync flow UX

Manual trigger from Sync Panel (U1):

```
┌─────────────────────────────────┐
│         Sync                  X │
├─────────────────────────────────┤
│                                 │
│  ● Push: 3 events to send      │
│  ○ Pull: checking for updates  │
│                                 │
│  [Sync Now]                     │
│                                 │
├─────────────────────────────────┤
│  Last sync: 2026-03-15 14:30   │
│  Device ID: a1b2...            │
└─────────────────────────────────┘
```

**Sync sequence**:

```
1. User taps [Sync Now]
2. Push phase:
   → Query: SELECT * FROM events WHERE pushed = 0
   → POST /api/sync/push {events: [...]}
   → Server responds: {accepted: 3, duplicates: 0}
   → Update local: SET pushed = 1 WHERE id IN (...)
   → UI: "● Push: 3 sent ✓"

3. Pull phase:
   → POST /api/sync/pull {since_watermark: W, limit: 100}
   → Server responds: {events: [...], latest_watermark: W'}
   → Insert new events into local SQLite (deduplicate by id)
   → Store W' as new watermark
   → Repeat until no more pages
   → PE re-derives projections for affected subjects
   → UI: "○ Pull: 12 received ✓"

4. Done:
   → Update last_sync timestamp
   → Dismiss Sync Panel (or show summary)
   → Work List and any open Subject Detail refresh
```

**Error handling**:

| Error | UX |
|-------|-----|
| No connectivity | Toast: "No connection. Try again later." Panel stays open. |
| Server error (5xx) | Toast: "Sync failed. Your data is safe locally." |
| Partial push (some accepted, some failed) | Show count: "2 of 3 sent. Retry for remaining." |
| Pull returns events for subjects not in scope | Ignore silently (Phase 0 has no scope; Phase 2 handles). |

**STRUCTURAL**: Events are never lost. A failed sync leaves local events untouched (`pushed` remains 0). The user can retry indefinitely. Idempotent sync [SY-2] means retrying is always safe.

### 5.5 Staleness indicators

| Indicator | Location | Phase 0 | Full |
|-----------|----------|---------|------|
| Last sync timestamp | Work List header, Sync Panel | `Last sync: 2h ago` or `Never synced` | Same |
| Pending event count | Work List header badge | `● N pending` (count of `pushed = 0` events) | Same |
| Per-subject staleness | Subject Detail | Not shown | **OPEN**: "Data as of last sync: Mar 15" if subject has server-origin events |

---

## 6. Progressive Disclosure Accommodations

The screen topology and component contracts are designed so that no phase requires restructuring screens. Every phase adds content to existing slots or activates previously no-op components.

### Phase 0 → Phase 1: Identity & Integrity

| Change | Screen affected | Type |
|--------|----------------|------|
| Flag badges on subjects | S1 Work List: badge slot on subject card | Activate badge slot (was hidden) |
| Flag badges on events | S2 Subject Detail: badge slot on timeline events | Activate badge slot (was hidden) |
| Conflict indicator on subject card | S1 Work List: SummaryExtractor returns `flag_count` | SummaryExtractor output gains a field |

**No screen restructuring.** Flag data flows through existing projection → SummaryExtractor → CardRenderer pipeline. The CardRenderer already has a badge slot; it was rendering nothing.

### Phase 1 → Phase 2: Authorization & Multi-Actor

| Change | Screen affected | Type |
|--------|----------------|------|
| Scope filtering activates | S1 Work List: `subject_list` query now returns scoped data | Invisible — PE returns fewer subjects |
| Assignment display | S2 Subject Detail: assignment info slot | Activate assignment slot (was hidden) |
| Multi-actor sync | Sync Service: scope-filtered pull | Internal sync logic change |

**No screen restructuring.** Scope filtering changes what the PE returns, not how the screen renders. The Subject Detail always had an assignment info slot; it was hidden because Phase 0/1 had a single user.

### Phase 2 → Phase 3: Configuration

| Change | Screen affected | Type |
|--------|----------------|------|
| Multiple activities | S1 Work List: Activity Selector becomes visible | Activate selector (was hidden) |
| Expression-driven show/hide | S3 Form: ExpressionSlot becomes real | Swap no-op → real implementation in FieldResolver |
| Computed defaults | S3 Form: ExpressionSlot | Same swap |
| Conditional warnings | S3 Form: ValidationSlot | Extend validation to include expression-driven warnings |
| `context.*` properties | S3 Form: `context_values` input | Populated by PE instead of empty map |

**No screen restructuring.** The Form Engine's ExpressionSlot was a no-op interface from Phase 0. Phase 3 provides a real implementation behind the same interface. The FieldResolver's pipeline doesn't change — it just gets non-trivial input from the ExpressionSlot.

### Phase 3 → Phase 4: Workflow & Policies

| Change | Screen affected | Type |
|--------|----------------|------|
| State badges on subjects | S1 Work List: badge slot shows `current_state` | SummaryExtractor output gains `current_state` |
| State-based grouping | S1 Work List: GroupingStrategy reads pattern config | GroupingStrategy receives non-trivial config |
| Dynamic action bar | S2 Subject Detail: actions from Action Resolver | Action Resolver logic becomes non-trivial (was hardcoded) |
| Command Validator warnings | S3 Form: CommandValidatorSlot becomes real | Swap no-op → real implementation |
| Review source preview | S3 Form: review preview slot | Activate slot (was hidden) |
| Transition history markers | S2 Subject Detail: timeline shows state transitions | Timeline event rendering adds transition markers |

**No screen restructuring.** The most significant Phase 4 activation is the Action Resolver going from a hardcoded single-action return to a pattern-aware transition evaluator. But the screens don't know the difference — they receive `List<Action>` and render buttons. Whether that list has 1 item or 5, the action bar works the same way.

### Verification: no screen type added in any phase

| Phase | Screens before | Screens after | New screen types |
|-------|:--------------:|:-------------:|:----------------:|
| 0 | 3 + U1 | 3 + U1 | 0 |
| 1 | 3 + U1 | 3 + U1 | 0 |
| 2 | 3 + U1 | 3 + U1 | 0 |
| 3 | 3 + U1 | 3 + U1 | 0 |
| 4 | 3 + U1 | 3 + U1 | 0 |

The progressive disclosure strategy holds: every phase adds capability without adding screens.

---

## 7. Data Flow Summary

```
┌─────────────────────────────────────────────────────┐
│                    USER                              │
│                                                     │
│    browses          views            fills           │
│    subjects         detail           form            │
│       │               │               │              │
│       ▼               ▼               ▼              │
│  ┌─────────┐   ┌───────────┐   ┌──────────┐        │
│  │  S1     │   │    S2     │   │   S3     │        │
│  │Work List│──▶│  Detail   │──▶│  Form    │        │
│  └────┬────┘   └─────┬─────┘   └────┬─────┘        │
│       │              │              │               │
│  reads│         reads│         writes│               │
│       ▼              ▼              ▼               │
│  ┌───────────────────────────────────────────────┐  │
│  │          Projection Engine                    │  │
│  │    (reads events, computes current state)     │  │
│  └───────────────────┬───────────────────────────┘  │
│                      │ reads                        │
│                      ▼                              │
│  ┌───────────────────────────────────────────────┐  │
│  │            Event Store (SQLite)               │  │
│  │  ┌─────────────────────────────────────────┐  │  │
│  │  │ id | type | shape_ref | subject_ref |.. │  │  │
│  │  │ id | type | shape_ref | subject_ref |.. │  │  │
│  │  └─────────────────────────────────────────┘  │  │
│  └───────────────────┬───────────────────────────┘  │
│                      │ push/pull                    │
│                      ▼                              │
│  ┌───────────────────────────────────────────────┐  │
│  │            Sync Service                       │  │
│  │    (manual trigger, push then pull)           │  │
│  └───────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────┘
```

**Key invariant**: Data flows in one direction for reads (Event Store → Projection Engine → Screens) and one direction for writes (Form → Event Store). Screens never write to projections. This is the device-side manifestation of write-path discipline [1-ST2].

---

## 8. Decision Register

Summary of all decisions in this document with their classification.

| # | Decision | Classification | Rationale |
|---|----------|---------------|-----------|
| D1 | 3 screen types (Work List, Subject Detail, Form) | STRUCTURAL | Event-sourced model requires timeline view (S2) separate from list (S1). Forms can be 60 fields (S3 must be full-screen). |
| D2 | Sync Panel is a utility overlay, not a screen | RECOMMENDED | Sync has no subject context. Bottom sheet is simpler. Could become a screen if sync gains complexity. |
| D3 | Stack-based navigation, no tabs/drawer | STRUCTURAL | Every user action is about one subject. No cross-subject lateral navigation needed. Memory-efficient on low-end devices. |
| D4 | "Add New" skips Subject Detail | RECOMMENDED | No history to show for a new subject. Could route through S2 if creation metadata is needed. |
| D5 | Work List is always root | STRUCTURAL | Landing screen must show all work without navigation. Root of hub-and-spoke. |
| D6 | Action bar driven exclusively by Action Resolver | STRUCTURAL | V4 consistency. Pattern changes never require screen changes. |
| D7 | Form Engine uses slot pattern for Expression/CommandValidator | STRUCTURAL | P5 (grows without breaking). Slots are no-ops in Phase 0, swapped with real implementations later. Interface stable. |
| D8 | No draft persistence | RECOMMENDED | Reduces complexity. Could add per-field autosave for very long forms. |
| D9 | Sync is push-then-pull, manual trigger | STRUCTURAL (Phase 0) | Implementation plan specifies manual sync. Background sync is a future addition. |
| D10 | Save always succeeds locally | STRUCTURAL | Command Validator is advisory [5-S4]. Flags are server-side [SG-1]. Device never blocks event creation. |
| D11 | Projection re-derivation on local write and sync pull | STRUCTURAL | Current state = f(events) [1-S2]. Any event change must update projections. |
| D12 | GroupingStrategy driven by activity/pattern config | STRUCTURAL | Different activities produce different list organizations. P7: no-pattern activity = flat list. |
| D13 | No separate alerts/inbox screen | RECOMMENDED | Alerts are subject-bound. Filter on Work List handles volume. Could add inbox if alert volume exceeds expectations. |
| D14 | Activity Selector hidden when count = 1 | STRUCTURAL | P7: the simplest scenario stays simple. S00 must not show UI for features it doesn't use. |
| D15 | FieldResolver reads shape definition, not hardcoded widgets | STRUCTURAL | V2 (set up, not built). Shape-driven forms are a core commitment. |
