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
`CLAUDE.md`, mechanical checks in Konsist or lint, history in `RETRO.md` — and only a change to
*how a stage works* goes into a skill or agent, written as a general step with no feature names,
phase numbers or counts. The test: would the sentence still be true and useful if that feature
had never existed?
