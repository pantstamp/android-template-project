# AI-Assisted Android Development — Skills

Five reusable skills for the AI agent pipeline:

```
Stage 1: Product Owner/BA  →  Stage 2: Architect  →  Stage 3: Developer
     (product-owner skill)    (architect skill)     (developer skill)
                                                          ↓
                                                    Quality Gate
                                                 (pre-pr-checklist skill)
                                                          ↓
                              Stage 5: Retro     ←  Stage 4: Reviewer
                           (retrospective skill)    (GitHub Actions)
```

## Installation

Copy the skill folders into your project's `.claude/skills/` directory:

```bash
mkdir -p .claude/skills
cp -r product-owner .claude/skills/
cp -r architect .claude/skills/
cp -r developer .claude/skills/
cp -r pre-pr-checklist .claude/skills/
cp -r retrospective .claude/skills/
```

Claude Code automatically picks up skills from `.claude/skills/` when you launch it
from the project root.

## Feature file structure

Each feature gets its own directory under `docs/features/`:

```
docs/
└── features/
    ├── watched-movies/
    │   ├── SPEC.md      ← produced by product-owner skill
    │   ├── PLAN.md      ← produced by architect skill
    │   └── RETRO.md     ← produced by retrospective skill
    ├── user-profiles/
    │   ├── SPEC.md
    │   ├── PLAN.md
    │   └── RETRO.md
    └── ...
```

This keeps a history of decisions per feature. No more overwriting files in the
project root.

## Usage

**The step-by-step guide for this project is [`docs/AI_WORKFLOW.md`](../../docs/AI_WORKFLOW.md)** —
what to type at each stage, what to check before moving on, and where the human
checkpoints are. It is kept in one place on purpose; duplicating the walkthrough here
would leave two copies to drift apart.

At a glance:

| Stage | Skill | Produces |
|---|---|---|
| 1 | `/product-owner` | `SPEC.md` |
| 2 | `/architect` | `PLAN.md` |
| 3 | `/developer` | code, on `feature/{feature-name}` |
| 4 | `/pre-pr-checklist` | GO / NO GO |
| 5 | comment `@claude-review` on the PR | inline review comments |
| 6 | `/retrospective` | `RETRO.md` + CLAUDE.md additions |

Stage 5 is not a skill. It runs in GitHub Actions
(`.github/workflows/claude-review.yml`) and is **opt-in**: reviews cost API credits, so
nothing happens on push — comment `@claude-review` on the pull request to request one.

## Resuming work

If context gets heavy mid-implementation, clear and resume:

```
/clear
Read docs/features/user-profiles/PLAN.md and CLAUDE.md.
Phases 1 and 2 are complete. Continue with Phase 3.
```

This works because PLAN.md is self-contained.
