```zsh
python3 docs/experiments/s00-event-spike/spike.py

============================================================
  STEP 1: Basic Capture
============================================================

A CHV visits a household and records an observation.

Event written:
  id:      e30e119e-ef1...
  type:    record_captured
  subject: d453f5e7-fc2...
  author:  chv-042
  time:    2026-04-08T09:00:00+00:00
  payload: {"temp": 38.5, "cough": true, "weight_kg": 12.3}

Events in store: 1
Projections:     0

Observation: the event is stored. No projection yet.
The event IS the data. Nothing else exists.

============================================================
  STEP 2: Projection — compute current state
============================================================

To show the record on screen, we project: apply all events → state.

  Current state:
    temp: 38.5
    cough: True
    weight_kg: 12.3

  Projection version: 1 event(s)
  Cached state matches computed: True

Observation: projection is a computation, not a lookup.
For 1 event, trivial. Question: how does this feel at 50 events?

============================================================
  STEP 3: Correction — append, don't overwrite
============================================================

The CHV realizes they misread the thermometer. They correct the temp.
The original event is NOT modified. A new event references it.

Correction event written:
  id:      3a3dcbda-ae4...
  type:    field_corrected
  subject: d453f5e7-fc2...
  ref:     e30e119e-ef1...
  author:  chv-042
  time:    2026-04-08T10:00:00+00:00
  payload: {"field": "temp", "was": 38.5, "now": 37.5, "reason": "misread thermometer"}

Original event payload: {"temp": 38.5, "cough": true, "weight_kg": 12.3}
Original temp value:    38.5  ← untouched

Projection after correction:
  Current state:
    temp: 37.5
    cough: True
    weight_kg: 12.3

Observation: append-only correction works. The original is preserved.
The projection shows the corrected value. Both events exist.
Events in store: 2

============================================================
  STEP 3b: Supervisor review
============================================================

A supervisor reviews and approves the corrected record.

Review event written:
  id:      087865c5-841...
  type:    review_completed
  subject: d453f5e7-fc2...
  ref:     3a3dcbda-ae4...
  author:  sup-007
  time:    2026-04-08T14:00:00+00:00
  payload: {"decision": "approved", "notes": null}

Projection after review:
  Current state:
    temp: 37.5
    cough: True
    weight_kg: 12.3
    _review_status: approved
    _reviewed_by: sup-007

Observation: review is just another event. No special machinery.
The projection now shows review status alongside the data.

============================================================
  STEP 4: Conflict — two offline corrections to the same field
============================================================

Two CHVs independently correct the weight (both were offline).
CHV-042 says weight was 13.0. CHV-088 says weight was 11.8.
Both reference the original capture event.

Correction A (chv-042):
  id:      8c797f2b-9ee...
  type:    field_corrected
  subject: d453f5e7-fc2...
  ref:     e30e119e-ef1...
  author:  chv-042
  time:    2026-04-08T11:00:00+00:00
  payload: {"field": "weight_kg", "was": 12.3, "now": 13.0, "reason": "re-weighed child"}

Correction B (chv-088):
  id:      0aa2f2b1-484...
  type:    field_corrected
  subject: d453f5e7-fc2...
  ref:     e30e119e-ef1...
  author:  chv-088
  time:    2026-04-08T11:15:00+00:00
  payload: {"field": "weight_kg", "was": 12.3, "now": 11.8, "reason": "scale was miscalibrated, re-measured"}

Projection after both corrections (naive — last event wins):
  Current state:
    temp: 37.5
    cough: True
    weight_kg: 11.8
    _review_status: approved
    _reviewed_by: sup-007

CONFLICTS DETECTED: 1
  Field: weight_kg
  Original event: e30e119e-ef1...
    chv-042: 12.3 → 13.0 (re-weighed child)
    chv-088: 12.3 → 11.8 (scale was miscalibrated, re-measured)

Observation: the conflict is DETECTABLE because both events exist.
The projection picked a winner (last-write by timestamp), but nothing
was lost. A conflict resolution event could supersede both.

KEY INSIGHT: With snapshots, you'd see two competing full records and
have to diff them field by field. With events, you see exactly which
field is contested and why each person changed it.

============================================================
  STEP 5: Replay — drop projections, rebuild from events
============================================================

Delete ALL projections. Rebuild from the event log only.
If the result matches, the event log is truly the single source of truth.

Projections after delete: 0
Events still in store:    5

Rebuilt projection:
  Current state:
    temp: 37.5
    cough: True
    weight_kg: 11.8
    _review_status: approved
    _reviewed_by: sup-007

Matches pre-delete projection: True
Events used:                   5

Observation: projections are truly derived. The event log alone is
sufficient to reconstruct all state. This is the escape hatch working.

============================================================
  FULL EVENT LOG
============================================================

Total events: 5

  [1] record_captured      by chv-042    ref=(none)           {"temp": 38.5, "cough": true, "weight_kg": 12.3}
  [2] field_corrected      by chv-042    ref=→e30e119e...     {"field": "temp", "was": 38.5, "now": 37.5, "reason": "misread thermometer"}
  [3] field_corrected      by chv-042    ref=→e30e119e...     {"field": "weight_kg", "was": 12.3, "now": 13.0, "reason": "re-weighed child"}
  [4] field_corrected      by chv-088    ref=→e30e119e...     {"field": "weight_kg", "was": 12.3, "now": 11.8, "reason": "scale was miscalibrated, re-measured"}
  [5] review_completed     by sup-007    ref=→3a3dcbda...     {"decision": "approved", "notes": null}


============================================================
  QUESTIONS TO SIT WITH
============================================================

  1. The projection is simple now (3 event types, direct field mapping).
     What happens when you add 10 event types? Does project() become
     a monster, or does it stay a clean switch/match?

  2. Conflict detection worked here because both corrections referenced
     the same original event and the same field. What if they didn't
     share a ref? What if the conflict is semantic, not structural?

  3. Timestamp ordering worked for 2 events from the same day.
     What if device clocks are wrong? What if events arrive at the
     server in a different order than they were created?

  4. The envelope had: id, type, subject_id, ref, payload, author,
     timestamp. Was anything missing? Was anything unnecessary?

  5. The projection rebuild was instant for 5 events. At what point
     does it become a problem? 100 events? 1000? Per subject, or
     across all subjects on the device?
```