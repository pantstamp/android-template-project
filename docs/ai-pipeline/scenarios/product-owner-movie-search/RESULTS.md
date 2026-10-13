# Results — product owner on movie-search

Newest first. Each entry records the pipeline commit it was taken at.

## 2026-10-12 — after the repeated-input change, at `f22f175` (3 runs)

**PASS: 3 of 3 runs passed every required criterion**, all bonuses included, on
`claude-opus-5-5`. P5 went from 1 of 3 to 3 of 3, and P8 from 2 of 3 to 3 of 3.

`f22f175` changed three lines of the skill. The edge cases the interview covers now include
repeated or equivalent input and late responses. Every gap the product owner notices must be
asked about, and becomes an open question only when the user doesn't know. An undecided point
goes under Open Questions, never into an acceptance criterion.

Every run now states that a change of only surrounding spaces starts no new search, and that a
late response for an older query is never shown. The interviews got a little longer, 12–13
exchanges against 10, and recorded more open questions (10, 15 and 18) because the product owner
asked more. P6 still passed in all three runs: every one of those questions was an "I don't know
yet". AC11 (a cached result loads with no network call) is still missing from all three.

| Criterion | Level | Run 1 | Run 2 | Run 3 |
|---|---|---|---|---|
| P1–P4 | required | ✅ | ✅ | ✅ |
| P5 repeated or identical queries | required | ✅ | ✅ | ✅ |
| P6 open questions recorded, no unapproved decision | required | ✅ | ✅ | ✅ |
| P7 out of scope listed | required | ✅ | ✅ | ✅ |
| P8 every criterion testable | required | ✅ | ✅ | ✅ |
| COMMITTED, LEAK | required | ✅ | ✅ | ✅ |
| B1, B2 | bonus | ✅ | ✅ | ✅ |
| B3 ≥ 20 of the real spec's 30 ACs | bonus | ✅ 26 | ✅ 27 | ✅ 27 |

| Run | Duration | Exchanges | Agent turns | Output tokens | Cost | Denials |
|---|---|---|---|---|---|---|
| 1 | 299 s | 12 | 24 | 15,977 | $7.45 | 1 |
| 2 | 323 s | 13 | 21 | 16,532 | $8.87 | 0 |
| 3 | 344 s | 12 | 28 | 17,773 | $8.51 | 1 |

## 2026-10-12 — official `eval.py` baseline, at `7acd123` (3 runs)

**FAIL: 0 of 3 runs passed every required criterion**, on `claude-opus-5-5`. P6, the failure
these runs were meant to re-check, passed all three. The failures are two others, and both are
real.

- **P5 failed in runs 1 and 3. The interview never asked whether an unchanged query searches
  again.** The scripted user had the answer ready: changing only surrounding spaces starts no new
  search. Run 3 never raised the case. Run 1 noticed it but filed it as Open Question 6 without
  asking. Run 2 proposed it, and the user confirmed it. P5 had passed all 4 earlier runs, so this
  is variance in what the interview covers, not a new defect. The skill's edge-case prompt
  ("empty states, boundary values, concurrent operations") does not mention repeated or
  equivalent input.
- **P8 failed in run 2.** AC-5.5 reads "What a refresh that returns no results shows is an open
  question (OQ-2)." An undecided point was written as a checkbox criterion that cannot be
  tested.

B3 dropped AC5 (spaces only) in runs 1 and 3, the same gap as P5. AC11 (a cached result loads with
no network call) was missing in all three runs.

| Criterion | Level | Run 1 | Run 2 | Run 3 |
|---|---|---|---|---|
| P1 empty results | required | ✅ | ✅ | ✅ |
| P2 offline and other errors, with retry | required | ✅ | ✅ | ✅ |
| P3 rotation and process death | required | ✅ | ✅ | ✅ |
| P4 clearing the query | required | ✅ | ✅ | ✅ |
| P5 repeated or identical queries | required | ❌ | ✅ | ❌ |
| P6 open questions recorded, no unapproved decision | required | ✅ | ✅ | ✅ |
| P7 out of scope listed | required | ✅ | ✅ | ✅ |
| P8 every criterion testable | required | ✅ | ❌ | ✅ |
| COMMITTED, LEAK | required | ✅ | ✅ | ✅ |
| B1 Details opens a movie never in Discover | bonus | ✅ | ✅ | ✅ |
| B2 search never adds to Discover | bonus | ✅ | ✅ | ✅ |
| B3 ≥ 20 of the real spec's 30 ACs | bonus | ✅ 27 | ✅ 26 | ✅ 25 |

| Run | Duration | Exchanges | Agent turns | Output tokens | Cost | Denials |
|---|---|---|---|---|---|---|
| 1 | 257 s | 10 | 19 | 14,671 | $5.96 | 0 |
| 2 | 272 s | 10 | 21 | 14,890 | $6.46 | 1 |
| 3 | 295 s | 10 | 28 | 16,625 | $8.32 | 1 |

## 2026-10-12 — first `eval.py run` end to end, at `e4e40cf` (1 run)

**PASS: 1 of 1.** P1–P8, `LEAK` and `COMMITTED` passed on `claude-opus-5-5`, with all three
bonuses. 294 s, 10 exchanges with the simulated user, 26 agent turns, $7.28 API-equivalent.

- **The simulated user (`claude-sonnet-5-5`) kept to its script.** It answered what was asked,
  rejected the proposal to cache search results in the Discover table, and said "I don't know yet"
  six times. All six became open questions.
- **B3 was first graded as an estimate.** The runner's judge never sees the real spec. With
  `judge_references` added, a re-grade of the same output counted about 27 of 30. The missing
  three were AC8 (scroll to top), AC26 (Discover's pull-to-refresh unchanged) and AC30 (an empty
  field on a fresh launch).
- **The run's first attempt failed in setup.** It and the architect run started in the same second
  with the same branch prefix, so they got the same branch name. Branch names now include the
  scenario.
- One denial: the agent's own `$(find …)`, which it rewrote.

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
