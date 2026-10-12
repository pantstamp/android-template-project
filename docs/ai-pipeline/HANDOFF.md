# Handoff — golden scenarios (roadmap step 2)

Written 2026-10-12, for a new session started after the open PRs are merged.

**Start the session with:** *"Read docs/ai-pipeline/HANDOFF.md and continue step 2."* Then read
[`AI_PIPELINE_ROADMAP.md`](../AI_PIPELINE_ROADMAP.md) §4 and [`README.md`](README.md) in this
folder.

## Before you start

- [ ] [#37](https://github.com/pantstamp/android-template-project/pull/37), `fix(mvi)`
  `ObserveEffects` through `rememberUpdatedState`, is merged.
- [ ] [#38](https://github.com/pantstamp/android-template-project/pull/38), this suite, is merged.
- [ ] `master` is checked out and pulled, and nothing is left over from earlier runs:
  `git worktree list` shows only the main checkout, and `git branch | grep -- -eval-` is empty.
- [ ] The CLI works, and is new enough for the pinned model:
  - `claude --version` is **2.1.280 or newer**;
  - `claude -p "reply with ok" --model claude-opus-5-5` answers.

## Where step 2 stands

All four scenarios are built and have in-session baselines. Each has also run **once** end to end
through `eval.py`:

| Scenario | In-session baseline | First `eval.py` run |
|---|---|---|
| `gate-movie-search` | 3 of 3 | PASS |
| `architect-movie-search` | 2 of 3 (E3 failed) | **FAIL, E6 only** |
| `developer-dry-run` | 3 of 3 | PASS |
| `product-owner-movie-search` | 2 of 3 (P6 failed; run 1 had a confound) | PASS, all bonuses |

Each scenario's `RESULTS.md` has the details, newest first.

**Pipeline changes the suite drove, already merged with #38:**
- **Gate:** pipes the pre-flight through `tail` instead of redirecting it to a file.
- **Architect:** plans a ViewModel's event tests as an event × state table, with a test in every
  busy cell.

## What is left, in order

1. **Take the official baseline: 3 runs of each scenario through `eval.py`.** About $85–90 at
   API prices, taken from the subscription allowance. Run them one at a time or in parallel; branch
   names no longer collide.

   ```bash
   python3 docs/ai-pipeline/eval.py run gate-movie-search
   python3 docs/ai-pipeline/eval.py run developer-dry-run
   python3 docs/ai-pipeline/eval.py run product-owner-movie-search
   python3 docs/ai-pipeline/eval.py run architect-movie-search
   ```

   Record each summary at the top of the scenario's `RESULTS.md`, with the commit it ran at and the
   model. The summary shows both.
2. **Decide on the architect's E6.** In the first `eval.py` run, the plan fed its query pipeline from
   a `MutableStateFlow` through `distinctUntilChanged`, so clearing and retyping the same query
   searched nothing. That is the retro's defect A, against a CLAUDE.md rule, which none of the six
   in-session runs got wrong. If it fails again in the 3-run baseline, add a dedicated architect
   self-check line for "a `StateFlow` that feeds work through `distinctUntilChanged`". Then re-run
   the architect scenario and compare.
3. **Check whether #37 fixed the architect's E3 failure.** Only one of the earlier failing runs
   passed a callback straight to `ObserveEffects`. With the helper fixed, E3 should now hold. Confirm
   it in the 3-run baseline.
4. **Re-check the product owner's P6** in the clean 3-run baseline before changing the skill. The one
   failure (two decisions the user never made) came from the run whose interview was disrupted.
5. **Tag the release:** `pipeline-v1.0.0` on the commit the baseline was taken at, following the
   roadmap's versioning plan. Not done yet:
   - stamping `pipeline: vX.Y.Z` on the SPEC, PLAN and RETRO headers;
   - starting a pipeline CHANGELOG.
6. **Then step 3 of the roadmap:** split the generic skills, the project config and the Android
   profile. Re-run the suite afterwards; it must not get worse.

## Things that bit us, and how they are handled now

| Problem | Where it is handled |
|---|---|
| `claude -p` 401 | `claude auth login`. The runner stops at the first one. |
| "does not support this model" | `claude update`. The runner stops at the first one. |
| The CLI's default model differs between machines | Models are pinned: `claude-opus-5-5` for the stage and judge, `claude-sonnet-5-5` for the simulated user. Each summary records the model that actually ran. |
| Under `dontAsk`, a `cd` outside the working directory and `$(…)` are refused, even when allowed | The gate prompt asks for `git -C` and `./gradlew -p <worktree>`. Watch the Denials column. |
| A seeded plan or snapshot that describes the test | `hidden_paths` and a neutral seed. The first developer runs quoted the trap back. |
| False `LEAK` from the runner's own `…/ai-pipeline-evals/…` path | The leak check strips the output directory. |
| The judge could not see the real spec | `judge_references`. |
| Branch collisions between parallel runs | Branch names include the scenario. |

**Costs:** each summary prints an API-equivalent cost. On a Claude subscription this is drawn from
the plan's allowance, not billed per token, unless extra usage is enabled. `claude auth status`
shows how the CLI is logged in.

## Known limits of the suite

- **Judge consistency is unmeasured.** Each criterion is graded once per run. The same
  `ObserveEffects` pattern once passed and once failed E3. Grading a few outputs twice would show
  how much grades vary.
- **The in-session baselines** were taken by subagents, with the orchestrating session as the user.
  Prefer the `eval.py` numbers from here on.
