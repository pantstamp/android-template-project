# Handoff — after the golden-scenario baseline (roadmap steps 2 → 3)

Written 2026-10-13, for a new session started after the baseline PR is merged.

**Start the session with:** *"Read docs/ai-pipeline/HANDOFF.md and continue."* Then read
[`AI_PIPELINE_ROADMAP.md`](../AI_PIPELINE_ROADMAP.md) §1 and §3, and [`README.md`](README.md) in
this folder.

## Before you start

- [ ] The version-stamp PR (`chore/pipeline-version-stamps`) is merged.
- [ ] `master` is checked out and pulled, and nothing is left over from earlier runs:
  `git worktree list` shows only the main checkout, and `git branch | grep -- -eval-` is empty.
- [ ] `claude --version` is **2.1.280 or newer**, and
  `claude -p "reply with ok" --model claude-opus-5-5` answers.

## Where things stand

The official baseline is taken: all four scenarios pass **3 of 3** through `eval.py`, on
`claude-opus-5-5`. Each scenario's `RESULTS.md` has the numbers, newest first.

| Scenario | Result | Bonus that never passes |
|---|---|---|
| `gate-movie-search` | 3 of 3 | — |
| `architect-movie-search` | 3 of 3 | B1, flagging the AC8 clear-and-retype scroll case |
| `developer-dry-run` | 3 of 3 | B1, a phase agent proving a test can fail |
| `product-owner-movie-search` | 3 of 3 | — (AC11, a cached result with no network call, is never covered) |

**What the baseline changed:**
- **Product owner skill:** the interview now covers repeated or equivalent input and late
  responses, asks about every gap it notices, and keeps undecided points out of the
  acceptance criteria. Before the change, 0 of 3; after it, 3 of 3.
- **Harness:** the developer scenario's prompt lists the command forms headless mode refuses.
  Git-graded runs also give the judge a `<tool-calls>` timeline of the session's own calls, with
  each spawned agent's ID.
- **Architect:** no change. E6 and E3 held in all three runs. Run 2 argued that a `StateFlow`
  query pipeline was safe because clearing happens in the collector. The judge passed it as
  borderline; watch for it.

## What is left, in order

1. ✅ **Tagged `pipeline-v1.0.0`** on `34baf3f`.
2. **Release `pipeline-v1.1.0`:** version stamps on SPEC, PLAN and RETRO headers, and
   `.claude/CHANGELOG.md`. Run the suite (at least the product owner and architect, whose
   templates changed), then follow the release steps in
   [`.claude/skills/README.md`](../../.claude/skills/README.md#versions).
3. **Roadmap step 3:** split the generic skills, the project config and the Android profile
   (roadmap §1). Re-run the full suite afterwards. It must stay at 3 of 3 everywhere; record each
   result at the top of the scenario's `RESULTS.md`.

## Things that bit us, and how they are handled now

| Problem | Where it is handled |
|---|---|
| `claude -p` 401 | `claude auth login`. The runner stops at the first one. |
| "does not support this model" | `claude update`. The runner stops at the first one. |
| The CLI's default model differs between machines | Models are pinned; each summary records the model that actually ran. |
| Under `dontAsk`, `cd`, `$(…)` and shell variables are refused | The gate and developer prompts say so. A run that retries a refused command unchanged stalls; watch the Denials column. |
| The judge cannot see which agent a message went to | Git-graded runs include a `<tool-calls>` timeline. |
| A seeded plan or snapshot that describes the test | `hidden_paths` and a neutral seed. |
| False `LEAK` from the runner's own `…/ai-pipeline-evals/…` path | The leak check strips the output directory. |
| Branch collisions between parallel runs | Branch names include the scenario. |

**Costs:** a full suite run (3 runs of each scenario) is about $85–90 at API prices: the architect
about $45, the product owner and developer about $20 each, the gate about $5. On a Claude
subscription this comes from the plan's allowance.

## Known limits of the suite

- **Judge consistency is unmeasured.** Each criterion is graded once per run. Grading a few
  outputs twice would show how much grades vary.
- **Three runs show a rate, not a guarantee.** P5 passed 4 earlier runs before failing 2 of 3.
