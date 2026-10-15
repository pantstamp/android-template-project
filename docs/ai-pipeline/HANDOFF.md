# Handoff — after step 3 (project config), before step 4 (dry run on production)

Written 2026-10-15. **Start the next session with:** *"Read docs/ai-pipeline/HANDOFF.md and
continue."* Then read [`AI_PIPELINE_ROADMAP.md`](../AI_PIPELINE_ROADMAP.md) §1, §2 and §5.

## Before you start

- [ ] The step-3 PR (`chore/pipeline-split`) is merged, and `master` is tagged `pipeline-v2.0.0`
  on the merge commit.
- [ ] `git worktree list` shows only the main checkout, and `git branch | grep -- -eval-` is empty.
- [ ] `claude --version` is 2.1.280 or newer, and `claude -p "reply with ok" --model claude-opus-5-5`
  answers.
- [ ] The production Android project is cloned locally, and you know its base branch and CI.

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

1. **Step 4 — dry run on the production project** (roadmap §1):
   - Write its `.claude/pipeline.md` from its CI workflow, build files and `CLAUDE.md`. Every name
     in this repo's table needs a value.
   - Copy `.claude/skills/` and `.claude/agents/` onto a scratch branch there. Run
     `/pre-pr-checklist` on an existing feature branch, and `/architect` on a small real change if
     there is one.
   - List every place a skill still assumes this template. Likely candidates:
     - Konsist (does production have an architecture-rule tool at all?)
     - the CLAUDE.md section names `{{rule-review}}` points to
     - `warningsAsErrors`
     - the `@claude-review` workflow
   - Fix each one here as a `2.0.x` patch: a new name in the table if it is a fact, a skill change
     if it is process. Run the affected scenarios.
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
