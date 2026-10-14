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
| `prompt.md` | the prompt; `{{WORKTREE}}`, `{{BASE}}`, `{{BRANCH}}` and `{{SCRATCH}}` are filled in | yes |
| `criteria.md` | pass criteria, one `- [ID] (required\|bonus[, variant]) …` line each | **no** |
| `answers.md` | scripted answers for a multi-turn scenario, read by the simulated user | **no** |
| `RESULTS.md` | recorded baselines, newest first | **no** |

**Mode** says where the run starts:
- `outside` — in this repo, pointed at the snapshot worktree by the prompt. Used by the gate,
  which only reads git and files, so the current `CLAUDE.md` and agent are the ones loaded.
- `inside` — in the worktree, with today's `.claude/skills`, `.claude/agents` and `CLAUDE.md`
  committed onto the run's branch (see below). Used by the stages that write files.

Other `scenario.json` settings:
- **`seed_files`**: files copied from the scenario folder into the run's tree before it starts, such
  as a plan to execute. They are committed with the pipeline.
- **`hidden_paths`**: snapshot files that describe the scenario itself (this folder, the roadmap).
  They are deleted in that same commit. A seed or snapshot that tells the run what is being tested
  makes the result worthless. The first developer runs quoted the trap back from the plan's
  header.
- **`done_when_committed`** (a file) or **`done_when_commits`** (a count): when a multi-turn
  run has finished.
- **`grade_file`** or **`grade_git`**: what the judge reads besides the dialogue. That is either
  one committed file, or the run's commits, diff and status.
- **`judge_references`**: files only the judge reads, as `{"commit", "path", "label"}` read from
  git. The product owner's coverage bonus compares against the real spec this way.
- **`stamped`**: the `STAMP` check. The committed `grade_file` must have
  `pipeline: vX.Y.Z` under its title, matching `.claude/PIPELINE_VERSION`.
- **`read_only`** and **`nested_no_git`**: the `READONLY` and `NESTEDGIT` checks. `NESTEDGIT`
  fails a run in which an agent spawned by the session (a phase implementer) ran a git command
  that changes history or undoes edits.

`forbidden_paths` are files that would reveal the outcome (a feature's `RETRO.md`), matched by
their full repo path; `forbidden_patterns` are other strings (a later commit, `master`). The
run is denied reading forbidden paths, and any reading tool call that names either fails the
run as `LEAK`.

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

`claude -p` must be logged in and recent enough for the pinned model. If `claude -p "hi"` answers
with a 401, run `claude auth login`. If a run fails with "does not support this model", run
`claude update`. The pinned `claude-opus-5-5` needs Claude Code 2.1.280 or newer. The runner
stops at the first of either error instead of failing every run.

After a pipeline change, record the new numbers at the top of the scenario's `RESULTS.md`, with
the commit they were taken at.

## Status

The official baseline was taken on 2026-10-12 through `eval.py`, 3 runs per scenario, and all
four scenarios pass 3 of 3. Getting there took two harness fixes, which a developer re-run then
confirmed, and one skill change, which a product owner re-run then confirmed. Each `RESULTS.md`
has the details, newest first. Earlier entries there were taken in-session (subagents, with the
orchestrating session as the user). Prefer the `eval.py` numbers.

Models are pinned: `--model` and `--judge-model` default to `claude-opus-5-5`, and `--user-model`
to `claude-sonnet-5-5`. Every summary records the model that actually ran. Results taken with a
different model are not comparable.

| # | Scenario | Folder | State |
|---|---|---|---|
| 1 | Gate | [`gate-movie-search`](scenarios/gate-movie-search/) | PASS, 3 of 3 (`eval.py`, 2026-10-12) |
| 2 | Architect | [`architect-movie-search`](scenarios/architect-movie-search/) | PASS, 3 of 3 (`eval.py`, 2026-10-12) |
| 3 | Developer | [`developer-dry-run`](scenarios/developer-dry-run/) | PASS, 3 of 3 (`eval.py`, 2026-10-12) |
| 4 | Product owner | [`product-owner-movie-search`](scenarios/product-owner-movie-search/) | PASS, 3 of 3 (`eval.py`, 2026-10-12) |

A scenario that declares `answers` is multi-turn. `eval.py` starts a session, then loops: the
skill's last message and the dialogue so far go to a second model (`--user-model`, default
Sonnet) that replies from the scripted answers, and the reply is sent with `claude -p --resume`.
The loop ends when `done_when_committed` shows up in a commit, or after `max_turns`. The judge
then reads the dialogue and the committed `grade_file`.

A scenario with `branch` gets its own branch at the snapshot, named
`<branch>-eval-<run>`, because branch names are shared by every worktree of the repo. In
`inside` mode, today's pipeline is committed onto it as `eval: use the current pipeline`, so the
run starts from a clean tree. The branch is deleted with the worktree.
