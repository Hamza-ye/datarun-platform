# 23: Setting Up a New Operational Activity

A coordinator needs to start a new kind of field activity without commissioning a new software project.

They decide what information workers should collect, which people are responsible for the work, which places or subjects are in scope, what simple warnings should appear during entry, and what should be reviewed before the activity is considered ready.

Once the setup is approved, workers receive the new activity during their normal preparation or sync process. They can start using it in the field, including when they later lose connectivity.

Some workers may already have work in progress under the previous setup. That work should remain understandable and finishable under the rules that existed when it began, while new work follows the new setup.

This results in:

* A new operational activity becoming available without custom development
* Field workers receiving the right information and responsibility boundaries
* Invalid or incomplete setup being caught before field workers depend on it
* Existing in-progress work remaining meaningful after the setup changes

---

## What makes this hard

Setup mistakes can cause field failure later. If a required question is missing, a responsibility is unclear, or a referenced part of the activity does not exist, the problem should be caught before the setup reaches field workers.

Different workers may receive the new setup at different times. A worker who has not connected recently may still be using the previous version, while another worker has already moved to the new one. Both versions need to produce records that remain interpretable.

Simple warnings are valuable, but they must not turn setup into software development. Coordinators should be able to express common checks without needing scripts, custom logic, or hidden processing rules.

Changes to setup affect future work, not the meaning of historical records. A record created under an older setup should remain understandable even after the activity changes.
