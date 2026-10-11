# Results — product owner on movie-search

Newest first. Each entry records the pipeline commit it was taken at.

## 2026-10-11 — first baseline, at `a9ee13b` (3 runs)

**WARN: 2 of 3 runs passed every required criterion.** Run 1 failed P6: its spec states two
decisions the user never made.

| Criterion | Level | Run 1 | Run 2 | Run 3 |
|---|---|---|---|---|
| P1 empty results | required | ✅ | ✅ | ✅ |
| P2 offline and other errors, with retry | required | ✅ | ✅ | ✅ |
| P3 rotation and process death | required | ✅ | ✅ | ✅ |
| P4 clearing the query | required | ✅ | ✅ | ✅ |
| P5 repeated or identical queries | required | ✅ | ✅ | ✅ |
| P6 open questions recorded, no unapproved decision | required | ❌ | ✅ | ✅ |
| P7 out of scope listed | required | ✅ | ✅ | ✅ |
| P8 every criterion testable | required | ✅ | ✅ | ✅ |
| COMMITTED, LEAK | required | ✅ | ✅ | ✅ |
| B1 Details opens a movie never in Discover | bonus | ✅ | ✅ | ✅ |
| B2 search never adds to Discover | bonus | ✅ | ✅ | ✅ |
| B3 ≥ 20 of the real spec's 30 ACs | bonus | ✅ 23 | ✅ 28 | ✅ 28 |

| Run | Spec | Open questions | Agent tokens |
|---|---|---|---|
| 1 | 277 lines, 7 user stories | 6 | 109k |
| 2 | 300 lines, 7 user stories | 5 | 110k |
| 3 | 269 lines, 8 user stories | 4 | 106k |

**How it was run.** As for scenarios 2 and 3, `claude -p` returned 401 inside the desktop
session:
- **The runs:** each was a `general-purpose` subagent following the worktree's
  `.claude/skills/product-owner/SKILL.md`, at `c3a9626` with today's pipeline committed.
- **The user:** the orchestrating session played the user from `answers.md`, a summary of what the
  real spec (`bdfacd9`) decided.
- **Grading:** a judge agent read the criteria, the committed spec, the interview and (for B3)
  the real spec.

**Orchestration problems in run 1.** These are a confound, and the reason not to read too much into
its result:
- The orchestrating session once sent an unscripted message ("waiting for your next question").
- Several replies queued while run 1 was mid-turn. It then saw answers to questions it had not yet
  asked, and asked for them to be confirmed.

Neither problem gave it information outside the script. Its P6 failure, two invented decisions,
is in content the user never discussed, not in the confused topics.

**Findings.**
- **Every run found the code-level gaps that a one-line requirement hides.** Each read the code
  before asking anything, and found without prompting both problems the real spec found:
  - Movie Details reads only the local table, so a searched movie can't open;
  - that table is the Discover list, so storing results there would show them in Discover.

  The product owner's "read the code" step works even when the input is not an issue.
- **The proposed defaults were often wrong in the same direction.** Before the user corrected
  them, runs proposed:
  - a 400 ms debounce;
  - a 1-character minimum;
  - no pull-to-refresh on results;
  - clearing old results while a new query loads;
  - caching search results to make Details work (run 3);
  - back leaving the screen instead of clearing the search (run 3).

  The interview format caught every one, because the user answered. A user who accepts defaults
  would have shipped them.
- **The open questions are honest.** Every "I don't know yet" became an open question in all three
  specs. Run 2 also flagged a case nobody had discussed: a refresh that returns no results.
- **Run 1's P6 failure.** Its spec says a result opened while offline "must still show completely",
  and makes the placeholder decorative for TalkBack. Neither was asked. Its review step listed five
  self-filled points for the user to check, but not these two.
- **Coverage gaps shared across runs:**
  - all three missed AC8 (scroll to the top on new results);
  - two missed AC30 (empty field on a fresh launch).

  Nobody asked about either. Run 1, the most disrupted interview, also never asked about Details'
  error states or adult content.
- **Cost:** about 106–110k agent tokens per run, over 8–14 turns.
