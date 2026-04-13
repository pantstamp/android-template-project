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

### Stage 1 — Product Owner / BA

```
I want to implement a new feature. Here are the requirements:

Feature: User Profiles
Users should be able to create a profile with their name,
avatar, and favorite genres...

Run a BA session and produce a SPEC.md.
```

Claude will interview you one topic at a time, then save
`docs/features/user-profiles/SPEC.md`.

### Stage 2 — Architect

```
Create an implementation plan for the user-profiles feature.
```

Claude reads the SPEC.md, studies the codebase, proposes a phased plan,
discusses it with you, then saves `docs/features/user-profiles/PLAN.md`.

Tip: switch to Opus for this stage (`/model opus`) for deeper reasoning.

### Stage 3 — Developer

```
Implement the user-profiles feature.
```

Claude reads the PLAN.md, creates the feature branch, and implements
phase by phase — waiting for your approval between each phase. After
all phases pass, it creates the PR.

Tip: switch to Sonnet for this stage (`/model sonnet`) for faster coding.

### Quality Gate — Pre-PR Checklist

After all phases pass, before creating the PR:

```
Run pre-PR checks for the user-profiles feature.
```

Claude runs build, tests, lint, PLAN.md coverage, SPEC.md acceptance
criteria, and scans for TODOs and debug leftovers. Reports GO /
CONDITIONAL GO / NO GO. On GO, create the PR.

### Stage 4 — Automated Review

No skill needed — this is handled by your `claude-review.yml` GitHub
Actions workflow. It triggers automatically on PR creation.

### Triage review comments

After the automated review posts inline comments:

```
Fetch the inline review comments from PR #XX in this repo using gh api.
Walk me through each one. For each finding, review it and show me the
code and the suggestion, then wait for my decision before proceeding.
```

### Stage 5 — Post-Merge Retrospective

After merging the PR:

```
Run a retrospective for the user-profiles feature, PR #XX.
```

Claude reads the review comments, analyzes what went well and what went
wrong, produces `docs/features/user-profiles/RETRO.md`, and suggests
CLAUDE.md updates. Approve the updates and each feature makes the next
one better.

## Resuming work

If context gets heavy mid-implementation, clear and resume:

```
/clear
Read docs/features/user-profiles/PLAN.md and CLAUDE.md.
Phases 1 and 2 are complete. Continue with Phase 3.
```

This works because PLAN.md is self-contained.
