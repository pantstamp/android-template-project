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

## Maintaining these skills

Skills hold **process**. Lessons from a retrospective go where they belong — project rules in
`CLAUDE.md`, mechanical checks in Konsist or lint, history in `RETRO.md` — and only a change to
*how a stage works* goes into a skill, written as a general step with no feature names, phase
numbers or counts. The test: would the sentence still be true and useful if that feature had
never existed?
