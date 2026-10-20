# AI-Assisted Android Development — Skills

Six skills for the feature pipeline. **The step-by-step guide is
[`docs/AI_WORKFLOW.md`](../../docs/AI_WORKFLOW.md)** — what to type at each stage, what to check
before moving on, and where the human checkpoints are. It is kept in one place on purpose so
two copies cannot drift.

| Stage | Skill | Produces |
|---|---|---|
| 1. Spec | `/product-owner` | the branch + `SPEC.md` (committed) |
| 2. Plan | `/architect` | `PLAN.md` (committed) |
| 3. Build | `/developer` | one commit per approved phase, then the PR |
| 4. Gate | `/pre-pr-checklist` | GO / CONDITIONAL GO / NO GO |
| 5. Review | comment `@claude-review` on the PR, then `/review-triage` | review fixes + thread replies |
| 6. Learn | `/retrospective` | `RETRO.md` + routed rule changes, on a docs branch |

The review itself is not a skill: it runs in GitHub Actions
(`.github/workflows/claude-review.yml`) and is opt-in, because it costs API credits.

Everything for one change lives in `docs/features/{feature-name}/` (`SPEC.md`, `PLAN.md`,
`RETRO.md`), on the change's own branch (`feature/…` or `fix/…`).

## Project config

The facts about this project that the skills and agents need — base branch and prefixes, the
feature docs path, hosting, the pre-flight and other commands, the architecture-rule tool, the
CLAUDE.md sections the gate's rule review reads, and the review mechanism — are in
[`../pipeline.md`](../pipeline.md). A skill names each one in double braces (`{{base}}`,
`{{pre-flight}}`) and reads the value from that table. **Every value is required**: a stage that
needs one that is missing stops and says so instead of guessing.

**Only facts go there.** How a stage works, and what is true of every Android project — lifecycle
and process death, previews, lint, Compose and Flow pitfalls — stays in the skills, in their own
words. Moving that knowledge out, or rewording it, made the golden scenarios worse (see
[`docs/AI_PIPELINE_ROADMAP.md`](../../docs/AI_PIPELINE_ROADMAP.md) §5). To change a command or a
branch, change the config, never a skill.

## Agents

Two skills hand bounded work to an agent in `../agents/`:

| Agent | Used by | Model | Why an agent |
|---|---|---|---|
| `phase-implementer` | `/developer`, once per phase | Sonnet | Keeps implementation's bulk out of the main conversation; every phase starts from PLAN.md alone |
| `pre-pr-gate` | `/pre-pr-checklist` | inherit | Judges the branch without the account of the session that wrote it; read-only |

**Skills own the conversation and the decisions; agents do bounded work and return a report.**
Every `/slash` entry point is a skill. An agent never asks the user anything, never commits, and
never makes a decision the user owns: where it would need one, it stops and reports, and the
skill takes it to the user. A stage that is a conversation (an interview, an outline to agree,
a finding to triage) stays in the skill.

A new or changed agent file is picked up when a session starts, so restart after editing one.

## Maintaining these skills

Skills hold **process**. Lessons from a retrospective go where they belong — project rules in
`CLAUDE.md`, mechanical checks in the architecture-rule tool or lint, history in `RETRO.md` —
and only a change to
*how a stage works* goes into a skill or agent, written as a general step with no feature names,
phase numbers or counts. The test: would the sentence still be true and useful if that feature
had never existed?

## Versions

The pipeline (`.claude/skills/`, `.claude/agents/`, the shape of `.claude/pipeline.md` and the
pipeline sections of `CLAUDE.md`) has one version, in [`PIPELINE_VERSION`](PIPELINE_VERSION).
SPEC, PLAN and RETRO headers record it as `pipeline: vX.Y.Z`.

- **Every change to the pipeline adds a line to the `Unreleased` section of
  [`CHANGELOG.md`](CHANGELOG.md)**, under its stage, saying what changed and why.
- **A release** runs the golden scenarios ([`docs/ai-pipeline/`](../../docs/ai-pipeline/README.md)).
  Then, in the release PR itself, it renames `Unreleased` to the version and date and sets
  `PIPELINE_VERSION` (once the plugin exists, `.claude-plugin/plugin.json`'s version with it), so
  the tagged commit carries both. After merge, tag the merge commit
  `pipeline-vX.Y.Z`. Use semver: major for a change to a contract between stages (the PLAN
  format the gate reads), minor for new behaviour, patch for a wording fix.
- **Roll back** with `git checkout pipeline-vX.Y.Z -- .claude/`.
