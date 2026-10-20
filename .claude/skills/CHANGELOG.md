# Pipeline CHANGELOG

One version for the whole pipeline: `.claude/skills/`, `.claude/agents/`, the shape of
`.claude/pipeline.md` and the pipeline sections of `CLAUDE.md`. Each release is tagged
`pipeline-vX.Y.Z` and passes the golden scenarios in `docs/ai-pipeline/` first. How to version and release: [`README.md`](README.md#versions).

Entries are grouped by stage and say what changed and why.

## Unreleased

### Pre-PR gate
- **Fetches the base and diffs against `origin/{{base}}`.** A stale local base put other merged
  work into the diff, so the rule review judged code that was not the branch's. If the fetch
  fails, the gate uses the local base and says so at the top. Check 4 reports how far the branch
  is behind, and a conflict with the base is NO GO.
- **A branch without SPEC.md or PLAN.md gets "not applicable" for Checks 1–2**, and the report's
  first line says the verdict covers code quality and CI only. In a dry run the gate used another
  document in the feature folder as a stand-in, which no contract covers and another run may not
  pick.
- **Skips a tool whose value is `none`**, and builds its pre-flight line from the steps that ran.

### All stages
- **The pipeline finds its own files wherever it is installed.** Skills and agents no longer open
  pipeline files by project-relative paths, which resolved only when the pipeline was copied into
  the project. The gate's report format is part of the `pre-pr-gate` agent;
  `PIPELINE_VERSION` and this CHANGELOG are in `skills/`, read from a skill's base directory; the
  architect opens `PLAN_TEMPLATE.md` from its own. The only project path left is
  `.claude/pipeline.md`. Installed machine-wide in a dry run, the gate fell back to a plain report
  layout because it could not find the format file.
- **The "Pipeline context" note reads correctly.** In 2.0.0 its example showed `{base}` with single
  braces, and the agents' copy said "a value a check uses uses". Every run read the table right
  anyway; the note now matches the names it describes.
- **`none` means the project has no such tool.** A step that uses it is skipped and the report
  says so; an empty or missing value still stops the stage. `pipeline.md`'s table marks which
  names may be `none`. A project without a formatter, an architecture-rule tool or an automated
  review had no defined way to say so.
- **`pipeline.md` names a path only if it exists in the project.** A project may check its
  AI-instruction files for paths that do not exist, and this file is one.

## 2.0.0 — 2026-10-15

All four golden scenarios pass 3 of 3 through `eval.py` on `claude-opus-5-5`, at `32f5662`. The
gate and product owner each needed a re-run after one failing run on a criterion with a history
of occasional misses (F5, P6); `pipeline-v1.1.0` was compared the same day.

### All stages
- **Project facts moved into `.claude/pipeline.md`.** The base branch and branch prefixes, the
  feature docs path, hosting, the pre-flight, format-fix and verification commands, the
  architecture-rule tool and where its rules live, the CLAUDE.md sections the rule review reads,
  and the review mechanism. Skills and agents name each one in double braces (`{{base}}`,
  `{{pre-flight}}`) and stop when a value is missing instead of guessing. Everything else keeps
  its 1.1.0 wording, the Android knowledge included. This is roadmap step 3: another Android
  project adopts the pipeline by writing its own `pipeline.md`, not by editing skills.
  Breaking: a project without `.claude/pipeline.md` cannot run any stage.
- **The pre-flight command is held once**, in the config. The gate agent and `AI_WORKFLOW.md`
  each had their own copy of `build.yml`'s task list.
- **Not done: a platform profile.** Moving the Android knowledge into one, or rewording it,
  regressed the product owner (6 of 9) and the architect (3 of 6) while 1.1.0, re-run the same
  day, passed 3 of 3. Deferred to the Flutter step; see the roadmap's §5.

## 1.1.0 — 2026-10-13

Product owner and architect pass 3 of 3 through `eval.py` on `claude-opus-5-5`; the gate and
developer did not change. Tagged on `07ceff3`.

### All stages
- **Artifacts record the pipeline version.** SPEC.md and PLAN.md headers carry
  `pipeline: vX.Y.Z`, read from `.claude/PIPELINE_VERSION`. Without it, a retro cannot tell
  whether a change in findings came with a pipeline release.

### Retrospective
- **RETRO.md records the version that built the feature**, taken from PLAN.md (`unstamped` for
  earlier plans). The comparison with earlier retros shows each feature's version, and whether
  a change in findings came with a release.

## 1.0.0 — 2026-10-13

The first release with a golden-scenario baseline: gate, architect, developer and product owner
each pass 3 of 3 through `eval.py` on `claude-opus-5-5`. Tagged on `34baf3f`.

### Product owner
- The interview covers repeated or equivalent input (the same value again, a change of only
  spaces or case) and late responses. It asks about every gap it notices, and records one as an
  open question only when the user doesn't know. Undecided points stay out of the acceptance
  criteria. The baseline's specs missed the unchanged-query case in 2 of 3 runs.

### Architect
- A design session the user leads: a briefing on the current state with a diagram, the user's
  own proposal first, a review of it one decision at a time, and only then the architect's
  design. The plan records target-state diagrams and a decision record with rejected
  alternatives.
- A ViewModel whose events start work gets an event × state table, with a test named in every
  cell where work is pending or running. "A test per state that matters" was read narrowly in
  every baseline run.

### Developer
- Each phase is implemented by a `phase-implementer` agent (Sonnet), which stops with BLOCKED
  rather than deviate from the plan. The skill re-runs the phase's verification itself, and
  sends feedback to the same agent after updating PLAN.md.

### Pre-PR gate
- Runs as an independent, read-only `pre-pr-gate` agent, with a review of the changed code
  against CLAUDE.md's judgement rules.
- Pipes the CI pre-flight through `tail` instead of redirecting it to a file.

### Earlier history
Changes before 1.0.0 are in `git log -- .claude`, and the agent-based pipeline in PR #35.
