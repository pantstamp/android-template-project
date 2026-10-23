# Handoff — after 3.0.0, before step 6 (the production project)

Written 2026-10-23. **Start the next session with:** *"Read docs/ai-pipeline/HANDOFF.md and
continue."* Then read [`AI_PIPELINE_ROADMAP.md`](../AI_PIPELINE_ROADMAP.md) §2 (packaging) and the
user guide [`PLUGIN_GUIDE.pdf`](PLUGIN_GUIDE.pdf) (built by `make_plugin_guide.py`; never edit the PDF).

## Before you start

- [ ] The 3.0.0 PR is merged, and `pipeline-v3.0.0` is tagged on the merge commit and pushed
  (`git ls-remote --tags origin pipeline-v3.0.0`).
- [ ] This repo has the released plugin installed: `claude plugin list` shows
  `ai-pipeline@ai-pipeline` at 3.0.0. If not, run the install commands in roadmap §2 here.
- [ ] `git worktree list` shows only the main checkout, and `git branch | grep -- -eval-` is empty.
- [ ] `claude -p "reply with ok" --model claude-opus-5-5` answers, and `python3 docs/ai-pipeline/eval.py check` passes.

## Where things stand

**Steps 1–5 are done.** 3.0.0 is the pipeline as a Claude Code plugin:
- **Where it lives.** The source is `plugins/ai-pipeline/` (skills, agents, `pipeline.template.md`,
  `PIPELINE_VERSION`, the CHANGELOG). The repo root's `.claude-plugin/marketplace.json` lists it.
  A release is a `pipeline-vX.Y.Z` tag.
- **Names.** Skills are `/ai-pipeline:<skill>` and agents `ai-pipeline:<agent>`; no skill's process
  changed.
- **This repo is a consumer too.** `.claude/settings.json` pins the released tag. Develop with
  `claude --plugin-dir ./plugins/ai-pipeline`, which loads the working copy over the installed one.
- **The suite.** `eval.py` gives every run a frozen copy of the plugin through `--plugin-dir`.
  `eval.py check` has six static checks, including that `plugin.json` matches `PIPELINE_VERSION`
  and that this repo's `pipeline.md` matches the template.
- **Released on a new, packaging-only rule** (suite README, "Status"): one passing run of each
  scenario the move could break. Gate (`full`, `elsewhere`), developer and product owner passed
  1 of 1. The architect was not run.

**What step 5 verified about plugins** is in roadmap §2, "Verified in step 5". Two points matter
for step 6:
- **Editing `ref` in `settings.json` does nothing.** The pin lives in a per-user registry. To take
  or roll back a version, run `marketplace add "<url>#<tag>"`, then `plugin update`.
- **One machine has one pin per marketplace name.** Two projects on one machine that pin different
  tags interfere with each other.

## What is left, in order

1. **Step 6: install on the production project** (GitLab, another machine), following
   `PLUGIN_GUIDE.pdf` §5:
   - delete the step-4 copies of the skills and agents (`~/.claude/skills/`, `~/.claude/agents/`
     or the project's `.claude/`)
   - compare its `.claude/pipeline.md` with `pipeline.template.md`. It probably lacks the names
     2.1.0 added: `{{rules}}`, `{{rules-home}}`, `{{lint}}` and `{{issues}}`.
   - run the two install commands, and commit `pipeline.md` and `settings.json`. They then run under
     the project's own CI checks.
   - take one real feature through the whole pipeline, ending with a retro
   - this is the first run of the GitLab steps, of rules kept outside `CLAUDE.md`, of a flavoured
     project's verification commands, and of an install from GitHub on another machine
2. **Follow-ups for the skills themselves**, not for a release. Each costs a batch or two to look
   at:
   - **Architect, E6.** Both 2.0.0 and 2.1.0 fail it in 1–2 runs of 3. The failing plans pass
     the query through a conflating `snapshotFlow` or `StateFlow` and ignore a query equal to the
     current one, so "matrix" → "" → "matrix" can be lost. The `StateFlow` rule is in `CLAUDE.md`,
     and the plans argue it does not apply.
   - **Product owner, P6.** A spec states a decision the user never made. This was 4 of 6 on
     2.1.0 against 3 of 3 on 2.0.0, and passed the single 3.0.0 run. If more batches confirm the
     gap, the first suspect is `{{rules}}` replacing the concrete `CLAUDE.md` in the skill.
3. **Deferred, with a trigger:** a deterministic config check (a script, a `SessionStart` hook and
   `/ai-pipeline:doctor`). Build it when a second project or another developer installs the plugin,
   or when step 6 hits a missing value the skills did not stop on (roadmap §2, "Deferred").

**Decided, so not to be raised again:**
- No platform profile until a real Flutter project adopts the pipeline (roadmap §5).
- No retrospective scenario until the roadmap is complete. `review-triage` gets no scenario.
- The full suite is not needed for a packaging-only release (suite README, "Status").
- The gate does not stop early on an already-merged branch ([`V2.1.0_FIXES.md`](V2.1.0_FIXES.md),
  entry 3).

**Another project's dry run** follows [`STEP4_GUIDE.md`](STEP4_GUIDE.md), written for reuse.

## Things that bit us, and how they are handled now

| Problem | Where it is handled |
|---|---|
| `claude -p` 401, or "does not support this model" | `claude auth login` / `claude update`. The runner stops at the first one. |
| Under `dontAsk`, `cd`, `$(…)` and shell variables are refused | The gate and developer prompts say so. The gate still loses 1–3 calls a run to `W=…; git -C $W`; harmless. |
| A developer run that commits a plan change on its own ended early | `done_when_subjects`: a run ends when a commit names each phase. |
| One failing run in three on P6, E6 or F1 | Compare with the previous release the same day before touching a skill (suite README, Status). |
| A same-day comparison checkout has no `local.properties` | Copy only its `sdk.dir` line; the root file holds the API key. |
| A forbidden pattern matched a legitimate command (`--all` in `./gradlew tasks --all`) | Forbidden patterns name the exact git forms (`log --all`, `branch -a` …), never a bare flag. |
| A run reads this checkout's pipeline while you edit it | Since 3.0.0 each run loads a frozen copy of the plugin; only `pipeline.md` and `CLAUDE.md` are still read live by `outside` runs. |
| A heredoc's `EOF` inside the text it writes cut the script short | Write long edit scripts to a file, or use a delimiter the text cannot contain. |
| `CLAUDE_CONFIG_DIR` to fake a machine-wide install loses the login | The `elsewhere` setting: a scratch project, and the plugin through `--plugin-dir`. |
| A forbidden pattern `ai-pipeline` matched the plugin's own agent names | `eval.py` removes the `ai-pipeline:` namespace before matching (3.0.0). |
| Editing `ref` in a project's settings did not move its install | The pin lives in a per-user registry: `marketplace add "<url>#<tag>"`, then `plugin update` (roadmap §2). |

**Costs:** a full suite run is about $90–150 at API prices (architect about $45–50, developer
about $16, gate about $5 per variant; the product owner varies with its dialogue, $27 for three
runs on 2.1.0 and $27 for a single 25-exchange run on 3.0.0). The 3.0.0 plumbing check cost $36. Step 3 cost about $500 in runs, 2.1.0
about $230 (two same-day comparisons included).

## Known limits of the suite

- **Three runs show a rate, not a guarantee.** P6, E6 and F1 each fail some runs on any version;
  a single miss is not a regression until a same-day comparison says so.
- **Judge consistency is unmeasured.**
- **The suite only tests this template project**, on GitHub, through `--plugin-dir`; no run installs from a marketplace. GitLab steps, rules kept outside
  `CLAUDE.md` and flavoured projects are first tested in step 6.
- **Without SPEC.md, the gate's rule review is weaker.** It found the Refresh-while-pending finding
  in 1 of 3 runs with no spec, against nearly every run with one.
- **Not blockers:** the architect's and developer's B1 bonuses rarely pass, and the product owner
  never covers AC11.
