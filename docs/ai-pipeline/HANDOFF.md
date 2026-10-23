# Handoff — step 5 (the plugin) in progress

Written 2026-10-21. **Start the next session with:** *"Read docs/ai-pipeline/HANDOFF.md and
continue."* Then read [`AI_PIPELINE_ROADMAP.md`](../AI_PIPELINE_ROADMAP.md) §2 (packaging) and §3
(versioning).

## Before you start

- [ ] The 2.1.0 PR is merged, and `pipeline-v2.1.0` is tagged on the merge commit.
- [ ] `git worktree list` shows only the main checkout, and `git branch | grep -- -eval-` is empty.
- [ ] `claude --version` is 2.1.280 or newer, and `claude -p "reply with ok" --model claude-opus-5-5`
  answers.
- [ ] `python3 docs/ai-pipeline/eval.py check` passes (free, no model call).

## Where things stand

**Steps 1–4 are done.** Step 4 was a dry run of `pipeline-v2.0.0` on a second, production Android
project, on another machine. It found eleven problems, and ten were fixed as
[`V2.1.0_FIXES.md`](V2.1.0_FIXES.md) describes. That is `pipeline-v2.1.0`:
- The pipeline finds its own files from any install: the report format is inside the gate agent,
  and `PIPELINE_VERSION` and the CHANGELOG are in `.claude/skills/`.
- The gate fetches its base, says "not applicable" without SPEC or PLAN, and skips tools set to
  `none`.
- New config names: `{{rules}}`, `{{rules-home}}`, `{{lint}}` and `{{issues}}`; `none` is defined.
- The architect resolves task names before writing a command; the phase implementer stops on a
  missing task.
- GitLab steps sit beside the GitHub ones in the developer, review-triage, retrospective and
  product-owner skills. **They are written, not yet run**: the production project is on GitLab,
  and step 6 is their first real use.
- `eval.py check`: four static checks on the pipeline's files, run before every suite run.
- New gate variants: `elsewhere`, `stale-base`, `no-spec` and `none-tools`.

**The release rule changed on 2026-10-20.** A release passes when no scenario is worse than the
previous release, run the same day. The released 2.0.0 scored 1 of 3 on the architect that day, so
"3 of 3 everywhere" had become a bar no version met. See the suite README, "Status".

**2.1.0's numbers:**
- gate 3 of 3 in all six variants
- developer 3 of 3
- architect 2 of 3, against 2.0.0's 1 of 3
- product owner 4 of 6, against 2.0.0's 3 of 3, recorded and accepted

## What is left, in order

1. **Step 5: package the pipeline as a plugin** (roadmap §2). **In progress on
   `chore/pipeline-3.0.0`** (2026-10-21):
   - **Done.** The three checks passed, plus four findings, all in roadmap §2 under "Verified in
     step 5". The biggest: a pin lives in a per-user registry, so editing `ref` in settings does
     nothing, and updating means `marketplace add <url>#<tag>` and then `plugin update`.
   - **Done.** `.claude/skills/` and `.claude/agents/` moved to `plugins/ai-pipeline/`, with
     `plugin.json` (still `2.1.0`) and the root `.claude-plugin/marketplace.json`. The spawn and
     hand-off names are prefixed `ai-pipeline:`, and so are the scenario prompts and the gate's
     `agent`. `eval.py` gives every run a frozen copy of the plugin through `--plugin-dir` (no more
     `--agents`), and its fifth static check compares `plugin.json` with `PIPELINE_VERSION`. The
     docs are updated.
   - **Checked, free or nearly:** `eval.py check` passes, and `claude plugin validate` passes. In
     a `setup` worktree, a haiku session loads exactly the six `ai-pipeline:` skills and both
     agents, with no bare duplicates.
   - **Done on 2026-10-23:**
     - `pipeline.template.md` ships in the plugin.
     - `eval.py check` has six checks; the new one keeps this repo's `pipeline.md` in step with the
       template.
     - The user guide is [`PLUGIN_GUIDE.pdf`](PLUGIN_GUIDE.pdf). Rebuild it with
       `make_plugin_guide.py`; never edit the PDF.
     - Plugin hooks were tested, and a config-check design was **deferred** with a trigger (roadmap
       §2, "Deferred").
   - **Left:**
     - **Plumbing runs, not the full suite** (decided 2026-10-23, suite README "Status"). One
       run each of the gate in its default and `elsewhere` variants and the developer, about
       $25–35. Optionally one product-owner run, for its STAMP check.
     - Then the release commit: `3.0.0` in `PIPELINE_VERSION` and `plugin.json`, the CHANGELOG
       heading, and this repo's `.claude/settings.json` pinned to `pipeline-v3.0.0`. The user chose
       on 2026-10-21 that this repo installs the released tag like any consumer, and develops with
       `--plugin-dir`.
     - After merge and tagging, install here with the roadmap §2 commands. That is the first real
       install from GitHub.
2. **Step 6: install on the production project** (GitLab), pinned to the tag:
   - commit its `pipeline.md`; it then runs under the project's own CI checks
   - take one real feature through the whole pipeline, ending with a retro
   - this is the first run of the GitLab steps, of rules kept outside `CLAUDE.md`, and of a
     flavoured project's verification commands
3. **Follow-ups for the skills themselves**, not for a release. Each costs a batch or two to look
   at:
   - **Architect, E6.** Both 2.0.0 and 2.1.0 now fail it in 1–2 runs of 3. The failing plans pass
     the query through a conflating `snapshotFlow` or `StateFlow` and ignore a query equal to the
     current one, so "matrix" → "" → "matrix" can be lost. The `StateFlow` rule is in `CLAUDE.md`,
     and the plans argue it does not apply.
   - **Product owner, P6.** A spec states a decision the user never made. This was 4 of 6 on
     2.1.0 against 3 of 3 on 2.0.0. If more batches confirm the gap, the first suspect is
     `{{rules}}` replacing the concrete `CLAUDE.md` in the skill.

**Decided, so not to be raised again:**
- No platform profile until a real Flutter project adopts the pipeline (roadmap §5).
- No retrospective scenario until the roadmap is complete. `review-triage` gets no scenario.
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
| A run reads this checkout's pipeline while you edit it | `full` gate runs read the agent and `pipeline.md` live; edit in a separate worktree until they finish. |
| A heredoc's `EOF` inside the text it writes cut the script short | Write long edit scripts to a file, or use a delimiter the text cannot contain. |
| `CLAUDE_CONFIG_DIR` to fake a machine-wide install loses the login | The `elsewhere` setting: a scratch project and `--agents` instead. |

**Costs:** a full suite run is about $90 at API prices (architect about $45–50, product owner
about $27, developer about $16, gate about $5 per variant). Step 3 cost about $500 in runs, 2.1.0
about $230 (two same-day comparisons included).

## Known limits of the suite

- **Three runs show a rate, not a guarantee.** P6, E6 and F1 each fail some runs on any version;
  a single miss is not a regression until a same-day comparison says so.
- **Judge consistency is unmeasured.**
- **The suite only tests this template project**, on GitHub. GitLab steps, rules kept outside
  `CLAUDE.md` and flavoured projects are first tested in step 6.
- **Without SPEC.md, the gate's rule review is weaker.** It found the Refresh-while-pending finding
  in 1 of 3 runs with no spec, against nearly every run with one.
- **Not blockers:** the architect's and developer's B1 bonuses rarely pass, and the product owner
  never covers AC11.
