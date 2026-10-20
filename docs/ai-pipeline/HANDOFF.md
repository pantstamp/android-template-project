# Handoff — after step 3 (project config), before step 4 (dry run on production)

Written 2026-10-15, updated 2026-10-20. **Start the next session with:** *"Read
docs/ai-pipeline/HANDOFF.md and V2.1.0_FIXES.md, and build pipeline-v2.1.0."* If the production project is on another machine, see
the next section for how to start instead. Then read
[`AI_PIPELINE_ROADMAP.md`](../AI_PIPELINE_ROADMAP.md) §1, §2 and §5.

## Before you start

- [x] The step-3 PR (#43) is merged, and `pipeline-v2.0.0` is tagged on the merge commit
  (`8282a53`).
- [ ] `git worktree list` shows only the main checkout, and `git branch | grep -- -eval-` is empty.
- [ ] `claude --version` is 2.1.280 or newer, and `claude -p "reply with ok" --model claude-opus-5-5`
  answers.
- [ ] The production Android project is cloned, and you know its base branch and CI.

## Running step 4 on another laptop

The user's step-by-step checklist for this phase is [`STEP4_GUIDE.md`](STEP4_GUIDE.md). Follow
its checkpoints, so they can check each one.

The production project may live on a different machine and in a different repository. That works:
this repo is public, so the other machine can fetch everything step 4 needs. The session that wrote
this file does not travel with it, so this file is the whole brief.

**On that machine you need:**
- Claude Code 2.1.280 or newer, logged in
- the Android toolchain (the JDK the project's CI uses, the SDK), so the pre-flight can run
- `gh` logged in with an account that can push to this repo, for the fixes

**Set it up** with this repo cloned next to the production project, and one session that sees both:

```bash
git clone https://github.com/pantstamp/android-template-project.git ~/src/android-template-project
```

```bash
cd ~/src/production-app && claude --add-dir ~/src/android-template-project
```

Then say: *"Read `~/src/android-template-project/docs/ai-pipeline/HANDOFF.md` and run step 4 on
this project."* (Adjust both paths to where the projects actually are.)

**How the steps change** (the guide's 4.2 and part 6 have the commands and checks):
1. **Install from the newest tag**, not from this repo's `master`: machine-wide (`~/.claude/skills`,
   `~/.claude/agents`, after backing up any skill or agent of the same name) or into the project's
   working tree, uncommitted. Machine-wide is the closer rehearsal of the plugin. Check first
   whether the project already has a `.claude/` folder; nothing of its own may be overwritten.
2. **Keep it local.** Nothing from the pipeline is committed or pushed in the project. Add the new
   project paths and `docs/features/` to its `.git/info/exclude` (local-only, never committed),
   leaving out any path it already tracks. Lines there are relative to the git root: if the
   build root is a subfolder, prefix them with it. `/product-owner` and `/architect`, if run,
   write SPEC.md and PLAN.md without committing them. At the end, remove exactly what the install
   added and the exclude lines, and restore any backups.
3. **Write the project's own `.claude/pipeline.md`**, show it to the user before using it, and run
   any check the project's CI makes on AI-instruction or docs files against it.
4. **Fixes go to this repo**, as a branch and a PR from that machine. Releases are tagged here, as
   before.
5. **The golden scenarios run on either machine**: they need only this repo and the Android SDK.
   Copy only the `sdk.dir` line into its `local.properties` (and the TMDB key, if the machine has
   one).

**Production code must not leak into this repo.** It is public. Everything written back here, from
skill fixes to CHANGELOG lines, `RESULTS.md` notes and this file, describes the mechanism, never
production's code:
- **Fine:** "a project without an architecture-rule tool"
- **Not fine:** production's class names, file paths, rules or snippets

That is the retrospective's rule ("no feature names, file names"), and it matters more here because
the code is not this repo's to publish. Confirm with the user that running Claude Code on
production's code is allowed before starting.

## Where things stand

**The goal changed on 2026-10-15:** first a second, production Android project, then Flutter. The
roadmap is re-planned around it.

**Step 3 is done as `pipeline-v2.0.0`.** The skills and agents are `pipeline-v1.1.0`'s text with
only project facts replaced by named values, such as `{{base}}`, `{{pre-flight}}` and
`{{architecture-rules}}`. Their values are in [`.claude/pipeline.md`](../../.claude/pipeline.md).
All four scenarios pass 3 of 3 (gate and product owner on a re-run).

**A first attempt at step 3 was dropped.** It moved the Android knowledge into a platform profile,
and the product owner and architect regressed while 1.1.0, re-run the same day, did not. The roadmap's
§5 and each `RESULTS.md` have the numbers. The lesson: facts can move to a config; knowledge and
wording stay in the skill.

## What is left, in order

1. **Step 4 — dry run on the production project** (roadmap §1; on another machine, see above):
   - **Status (2026-10-20):** dry run done on the production project; the gate ran on one branch.
     The agreed fixes, and how to check each, are in [`V2.1.0_FIXES.md`](V2.1.0_FIXES.md). Build
     them as one `2.1.0` (not `2.0.x`), then run the scenarios it lists.
   - Write its `.claude/pipeline.md` from its CI workflow, build files and `CLAUDE.md`. Every name
     in this repo's table needs a value.
   - Copy the pipeline from the `pipeline-v2.0.0` tag into its working tree, uncommitted and listed
     in its `.git/info/exclude`: nothing of the pipeline is committed or pushed there. Run
     `/pre-pr-checklist` on an existing feature branch. That branch has no SPEC.md or PLAN.md, so
     Checks 1 and 2 report that, which is expected; judge the rest. Optionally run
     `/product-owner` then `/architect` on a small real change, stopping after the plan and
     committing neither SPEC.md nor PLAN.md.
   - List every place a skill still assumes this template. Likely candidates:
     - Konsist (does production have an architecture-rule tool at all?)
     - the CLAUDE.md section names `{{rule-review}}` points to
     - `warningsAsErrors`
     - the `@claude-review` workflow
   - Fix each one here: a new name in the table if it is a fact, a skill change if it is process.
     Run the affected scenarios. (The first dry run's fixes became `2.1.0`, not a patch.)
   - **Building 2.1.0** (from 2026-10-20, branch `chore/pipeline-2.1.0`), in four slices, each
     followed by the scenarios it touches: (a) entries 1, 10, 11 and the `eval.py` static checks;
     (b) the gate's behaviour, 2, 4, 7; (c) the new names in the architect and developer, 5, 6, 8;
     (d) GitLab and `{{issues}}`, 9 (the production project is on GitLab). Released together.
2. **Step 5 — package as a plugin** (roadmap §2). Verify first:
   - the `subagent_type` string for a plugin agent (`ai-pipeline:pre-pr-gate`?)
   - that a pinned tag is not refetched by auto-update
3. **Step 6 — install on production**, pinned to the tag, and take one real feature through the
   whole pipeline, ending with a retro.

**Decided, so not to be raised again:**
- No platform profile until a real Flutter project adopts the pipeline (roadmap §5).
- No retrospective scenario until the roadmap is complete. `review-triage` gets no scenario.

## Things that bit us, and how they are handled now

| Problem | Where it is handled |
|---|---|
| `claude -p` 401, or "does not support this model" | `claude auth login` / `claude update`. The runner stops at the first one. |
| Under `dontAsk`, `cd`, `$(…)` and shell variables are refused | The gate and developer prompts say so. The gate still loses 1–3 calls a run to `W=…; git -C $W`; harmless. |
| A developer run that commits a plan change on its own ended early | `done_when_subjects`: a run ends when a commit names each phase. |
| One failing run in three on P6 or F5 | Compare with the previous release the same day before touching a skill (suite README, Status). |
| A same-day comparison checkout has no `local.properties` | Copy only its `sdk.dir` line; the root file holds the API key. |

**Costs:** a full suite run is about $90 at API prices (architect about $45–50, product owner
about $27, developer about $16, gate about $5). Step 3 cost about $500 in runs, most of it on the
dropped first attempt.

## Known limits of the suite

- **Three runs show a rate, not a guarantee.** P6 and F5 each fail about one run in ten or more;
  a single miss is not a regression until a same-day comparison says so.
- **Judge consistency is unmeasured.**
- **The suite only tests this template project.** Production quality shows on the first real
  feature there.
- **Not blockers:** the architect's and developer's B1 bonuses rarely pass, and the product owner
  never covers AC11.
