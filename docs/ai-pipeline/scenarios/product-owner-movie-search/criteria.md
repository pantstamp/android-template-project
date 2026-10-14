# Pass criteria — product owner on movie-search

Graded against the committed `SPEC.md` and the interview dialogue. The run never sees this file.

The run starts from a one-line requirement — *"Users should be able to search movies by title
from the Discover tab"* — at `c3a9626`, before the real spec existed. The scripted user knows
exactly what the real spec (`bdfacd9`) decided, answers only what is asked, and says "I don't
know yet" otherwise. So the spec can cover a topic only if the product owner asked about it:
coverage measures the interview.

Each line is `- [ID] (level) text`. `required` criteria decide the pass; `bonus` criteria are
reported but never fail a run. An acceptance criterion counts when it is a checkable statement
of behaviour (given / when / then, or equivalent), not a goal.

## What a one-line requirement leaves out

- [P1] (required) Empty results: an acceptance criterion for a query with no matches, saying
  what the user sees.
- [P2] (required) Offline and other errors: acceptance criteria for a search that fails offline
  and for other failures, each with a way to retry.
- [P3] (required) Rotation and process death: acceptance criteria for what survives a rotation
  and what happens after the process is killed in the background.
- [P4] (required) Clearing the query: an acceptance criterion for clearing the search and what
  is shown afterwards.
- [P5] (required) Repeated or identical queries: the spec defines what happens when the query
  does not meaningfully change (the same text again, only surrounding spaces) or when results
  for an older query arrive late.

## The spec's discipline

- [P6] (required) Open questions are recorded rather than assumed: every point the user
  answered "I don't know yet" appears as an open question (or is explicitly decided by the user
  later), and the spec states no decision the user did not make or approve.
- [P7] (required) Out-of-scope items are listed.
- [P8] (required) Every acceptance criterion is testable as written: observable, with no
  "quickly", "smoothly" or "user-friendly" without a measure.

The runner adds `COMMITTED` (`SPEC.md` committed on the branch), `STAMP` (`pipeline: vX.Y.Z`
under the title, matching `.claude/PIPELINE_VERSION`) and `LEAK` (no `master`, remote, issue or
later commit reached for) itself.

## Bonus

- [B1] (bonus) The spec covers how a search result opens in Movie Details when that movie was
  never in the Discover list (the real spec's AC10). Code reading shows Details reads only the
  local table, so this fails today.
- [B2] (bonus) The spec says search must not add movies to the Discover list (AC13/AC21). Code
  reading shows that list *is* the local table.
- [B3] (bonus) Coverage relative to the real spec: of its 30 acceptance criteria, how many have
  an equivalent. Report the count in the evidence; pass at 20 or more.
