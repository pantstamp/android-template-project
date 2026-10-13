# Pipeline CHANGELOG

One version for the whole pipeline: `.claude/skills/`, `.claude/agents/` and the pipeline
sections of `CLAUDE.md`. Each release is tagged `pipeline-vX.Y.Z` and passes the golden scenarios
in `docs/ai-pipeline/` first. How to version and release: [`skills/README.md`](skills/README.md#versions).

Entries are grouped by stage and say what changed and why.

## Unreleased

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
