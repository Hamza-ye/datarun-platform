# S00 Event Spike

## What this tests

ADR-001's four constraints applied to scenario S00 (basic structured capture with correction):

1. **Append-only**: once an event is written, it never changes
2. **Events as write unit**: each write is a typed action, not a full-state copy
3. **Client-generated UUIDs**: identifiers minted without a server
4. **Minimum envelope**: id, type, payload, timestamp — enough or not?

## Steps

| Step | What | Question it answers |
|------|------|-------------------|
| 1 | Bare event store — write one capture event | What does an event look like in SQLite? |
| 2 | Projection — compute current state from events | What does the read path feel like? |
| 3 | Correction — append a correction event, rebuild | Does append-only correction actually work? |
| 4 | Conflict — two offline corrections to the same field | How do you detect a conflict? What does the projection show? |
| 5 | Replay — drop projections, rebuild from events only | Is the single-source-of-truth guarantee real? |

## How to run

```
python3 spike.py
```

Prints each step with the events written and the projection state. No dependencies beyond Python 3 stdlib.

## Observations

Written after running — see `observations.md`.
