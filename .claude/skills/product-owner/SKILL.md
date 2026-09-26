---
name: product-owner
description: >
  Interviews the user one topic at a time to turn raw requirements or a GitHub issue into a
  SPEC.md with user stories, acceptance criteria, edge cases and out-of-scope items, on a new
  feature or fix branch. Use when the user wants to spec, scope or define a feature or bug fix,
  write user stories or acceptance criteria, or start work on a GitHub issue.
---

# Product Owner / Business Analyst

Runs a requirements interview and produces a SPEC.md. Ambiguities are cheapest to resolve
here, before a plan or code depends on them.

## Inputs

- **Raw requirements** — free-form text, or a **GitHub issue** number
- **Feature name** — a short kebab-case identifier (e.g. `user-profiles`). If not given,
  infer one and confirm it.
- **Type** — `feat` (new behaviour) or `fix` (a bug). Infer it (an issue labelled `bug` is a
  `fix`) and confirm it together with the name.

## Workflow

### 1. Set up the branch

Every later stage (architect, developer) works on the branch created here, so SPEC.md and
PLAN.md are committed rather than left untracked in whatever checkout happens to be active.

```bash
git status --porcelain          # must be empty
git checkout master && git pull --ff-only
git checkout -b {prefix}/{feature-name}   # prefix: feature for feat, fix for fix
```

If the working tree is dirty or another branch has unmerged work, stop and ask instead of
carrying changes along. If the branch already exists, check it out and continue from what is
on it.

### 2. Read context

- `CLAUDE.md` — conventions and constraints that affect feasibility
- Existing specs in `docs/features/` — avoid contradicting them
- **If the input is a GitHub issue**: `gh issue view {n} --comments`. Then read the code the
  issue names and **verify its root-cause claims**. Issues describe symptoms well and causes
  partially; the code usually shows more causes than the issue lists, and each one is a
  requirement the spec must cover. Report what you confirmed and what you found beyond it.

Tell the user you'll interview them one topic at a time, then write SPEC.md.

### 3. Find the gaps

Before asking anything, identify:
- Ambiguous terms or undefined behaviour
- Missing unhappy paths
- Implicit assumptions about platform behaviour
- Android concerns: offline, lifecycle (rotation, process death), permissions, deep links,
  back navigation, accessibility

### 4. Interview — one topic per message

For each topic:
- State what is unclear or what you are assuming
- Propose a default with its reason ("I'd suggest X because Y — does that work?")
- Wait for the answer before the next topic

Cover what applies:
- **Core behaviour** — triggers and outcomes
- **Edge cases** — empty states, boundary values, concurrent operations
- **Error handling** — network failures, database errors, invalid input
- **Lifecycle** — rotation, process death, back navigation
- **Offline** — what works offline, what is cached, sync strategy
- **Accessibility** — content descriptions, touch targets, screen reader behaviour
- **Data** — storage limits, migration, data loss
- **UI/UX open questions** — naming, visual treatment, interaction patterns

### 5. Write and commit SPEC.md

Save to `docs/features/{feature-name}/SPEC.md`:

```markdown
# Feature: {Feature Name}

## Overview
One paragraph: what this does and why. For a fix, include the verified root cause.

## User Stories
- **As a** [user type], **I want** [action], **so that** [benefit]
- **Acceptance Criteria:**
  - [ ] Criterion (observable and checkable — not "loads quickly")

## Edge Cases
Each edge case and how it is handled.

## Error Handling
Network errors, database failures, invalid state.

## Out of Scope
What this does NOT include.

## Open Questions (if any)
Undecided points, with the options discussed.
```

Ask the user to review it. Apply requested changes. Once they approve, commit it:

```bash
git add docs/features/{feature-name}/SPEC.md
git commit -m "docs({feature-name}): add spec"
```

Do not push; the developer stage pushes the branch with the PR.

## Example

**User:** "Users should be able to mark movies as watched and see them in a new tab."

1. Confirm name and type: `watched-movies`, `feat` → branch `feature/watched-movies`
2. Read CLAUDE.md and existing specs
3. Ask: "Can a rating be changed later from the Watched tab, or is it read-only there?"
4. Continue one topic at a time until the gaps are covered
5. Save the spec, get approval, commit it on the branch
