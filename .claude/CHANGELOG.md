# Pipeline CHANGELOG

One version for the whole pipeline: `.claude/skills/`, `.claude/agents/`, `.claude/profiles/`,
`.claude/pipeline.md` and the pipeline sections of `CLAUDE.md`. Each release is tagged `pipeline-vX.Y.Z` and passes the golden scenarios
in `docs/ai-pipeline/` first. How to version and release: [`skills/README.md`](skills/README.md#versions).

Entries are grouped by stage and say what changed and why.

## Unreleased

### All stages
- **The skills and agents are generic; platform and project context moved out.** Commands,
  branches, the docs path, hosting, enforcement tools, the review mechanism and the rule-review
  sections are in the project config, `.claude/pipeline.md`. Interview concerns, the plan
  checklist, residue patterns and framework mistake areas are in the platform profile,
  `.claude/profiles/android.md`. Each stage reads both first, and stops on a missing entry
  instead of guessing. This is roadmap step 3: the process can now be reused on another platform
  or project by writing a profile and a config, not by editing skills. Breaking: a project
  without `.claude/pipeline.md` cannot run any stage.
- **The pre-flight command is held once**, in the config. The gate agent and `AI_WORKFLOW.md`
  each had their own copy of `build.yml`'s task list.

### Product owner
- Every edge case and platform concern the user decided or approved also gets an acceptance
  criterion. After the split, one of three specs recorded the agreed rotation and process-death
  behaviour only under Edge Cases, where the architect and the gate never look.
- When an answer accepts only part of a proposal, the rest is not approved: the interview asks
  about it or records it as an open question. One run turned the unmentioned half of two
  proposals into acceptance criteria.

### Architect
- A plan that argues a CLAUDE.md rule does not apply must walk the rule's own failure sequence
  through the plan's code, including state the event handler changes first. One of three plans
  after the split kept a `StateFlow` query pipeline on a steady-state argument while its handler
  cleared results synchronously; the baseline had passed the same argument as borderline.

### Retrospective
- Lessons can be routed to the platform profile, and a skill sentence that names a platform's
  tools fails the routing test: it belongs in the profile or `CLAUDE.md`.

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
