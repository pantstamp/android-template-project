# Golden scenarios

A suite that says whether a change to the pipeline (`.claude/skills/`, `.claude/agents/`, the
pipeline sections of `CLAUDE.md`) made it worse. It is step 2 of
[`AI_PIPELINE_ROADMAP.md`](../AI_PIPELINE_ROADMAP.md); §4 there is the brief.

Each scenario replays a **historical snapshot whose outcome is already known**, with the
**current** pipeline files, and grades the result against what actually happened.

Run it before tagging a pipeline release (`pipeline-vX.Y.Z`), not on every change: a full run of
one scenario costs minutes and well over 100k tokens.

## Decisions

| Decision | Choice | Why |
|---|---|---|
| Tooling | `eval.py`, a script around `claude -p` | `claude plugin eval` needs a newer CLI and a plugin manifest, runs each case in an empty workspace, and cannot answer a skill's questions mid-run. Revisit at roadmap step 4, when the skills are a plugin. |
| Runs per scenario | 3 | Model output varies. 3 of 3 passes; 2 of 3 is `WARN`, a reason to look; less is `FAIL`. |
| Order | gate → architect → developer → product owner | Cheapest and best-known baseline first; the architect next, because PR #32's defects started in the plan. |
| Location | `scenarios/<name>/`, one folder each | Moves into the plugin repo at step 4. |

## A scenario folder

| File | Holds | Seen by the run? |
|---|---|---|
| `scenario.json` | snapshot and base commits, mode, agent, tools, runs, variants | — |
| `prompt.md` | the prompt; `{{WORKTREE}}` and `{{BASE}}` are filled in | yes |
| `criteria.md` | pass criteria, one `- [ID] (required\|bonus[, variant]) …` line each | **no** |
| `RESULTS.md` | recorded baselines, newest first | **no** |

**Mode** says where the run starts:
- `outside` — in this repo, pointed at the snapshot worktree by the prompt. Used by the gate,
  which only reads git and files, so the current `CLAUDE.md` and agent are the ones loaded.
- `inside` — in the worktree, with today's `.claude/skills`, `.claude/agents` and `CLAUDE.md`
  copied in, uncommitted. Used by the stages that write files.

`forbidden_paths` are files that would reveal the outcome (a feature's `RETRO.md`). The run is
denied reading them, and any tool call that names one fails the run as `LEAK`.

## Running

```bash
python3 docs/ai-pipeline/eval.py list
python3 docs/ai-pipeline/eval.py run gate-movie-search                    # 3 full runs
python3 docs/ai-pipeline/eval.py run gate-movie-search --variant check5   # rule review only
```

Each run gets its own worktree, so runs never share Gradle state. Results go to
`$TMPDIR/ai-pipeline-evals/<scenario>/<timestamp>-<variant>/`: per run the prompt, the full
transcript, the output and `grade.json`, and a `summary.md` for the set. Worktrees are removed
afterwards unless `--keep` is passed.

A judge model grades the output against `criteria.md`; deterministic checks (`LEAK`) are added
by the runner. To grade an output produced some other way, for example by spawning the agent
from an interactive session:

```bash
python3 docs/ai-pipeline/eval.py setup gate-movie-search <dir>     # worktree + prompt, no model call
python3 docs/ai-pipeline/eval.py grade gate-movie-search <output.md>
```

`claude -p` must be logged in: if `claude -p "hi"` answers with a 401, run `claude auth login`
first.

After a pipeline change, record the new numbers at the top of the scenario's `RESULTS.md`, with
the commit they were taken at.

## Status

| # | Scenario | Folder | State |
|---|---|---|---|
| 1 | Gate | [`gate-movie-search`](scenarios/gate-movie-search/) | runnable; baseline in `RESULTS.md` |
| 2 | Architect | — | next; needs a simulated user for the scripted answers |
| 3 | Developer | [`developer-dry-run`](scenarios/developer-dry-run/) | plan only; needs scripted answers |
| 4 | Product owner | — | not started |

Scenarios 2–4 need answers given mid-run. `eval.py` refuses a scenario that declares `answers`
until it can drive one: the plan is a loop of `claude -p --resume`, with a second model playing
the user from the scripted answers.
