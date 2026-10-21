---
name: retrospective
description: >
  Runs a post-merge retrospective for a feature's pull request: reads the review comments, the
  diff, the fix commits, SPEC.md and PLAN.md, writes a RETRO.md comparing against earlier
  retros, and routes each lesson to the project's rules, an architecture or lint rule, a skill, or the review
  prompt. Use after a
  PR is merged when the user asks for a retro, retrospective, lessons learned, or to turn
  review findings into project rules.
---

# Post-Merge Retrospective

Mistakes repeat unless something changes. This turns a merged PR's review findings into
durable changes — project rules, mechanical checks, or process — and records what happened.

## Pipeline context

Read `.claude/pipeline.md` before you start. A name in double braces below, such as `{{base}}`,
stands for that name's value in its table.
If a value this skill uses is missing there, stop and tell the user which; do not
guess it.

## Inputs

- **Feature name** — locates `{{feature-docs}}/`
- **PR number**

If not given, infer from the branch or ask.

## Workflow

### 1. Branch

The feature branch is merged, so work on a new one:

```bash
git checkout {{base}} && git pull --ff-only
git checkout -b {{retro-prefix}}{feature-name}-retro
```

### 2. Gather

- Inline review comments — path, line, severity, suggestion, and whether a reply says it was
  fixed — and the review summary, the host's way (`{{hosting}}`):
  - **On GitHub**: `gh api repos/{owner}/{repo}/pulls/{n}/comments` and
    `gh api repos/{owner}/{repo}/issues/{n}/comments`
  - **On GitLab**: `glab api projects/:id/merge_requests/{n}/discussions --paginate --output ndjson`.
    A discussion whose first note has a `position` is an inline comment, with its replies; one
    without is the summary or another general comment.
- The PR diff and its commits; compare the original commit with any fix commits:
  - **On GitHub**: `gh pr diff {n}`
  - **On GitLab**: `glab mr diff {n} --raw`
- Timeline: comment times against the last commit and the merge — any comment newer than the
  last commit was merged unaddressed
- SPEC.md and PLAN.md, and the `pipeline:` version in PLAN.md's header: the release that
  built the feature. A plan written before versioning has none; write `unstamped`
- Earlier features' `RETRO.md` (in their `{{feature-docs}}`) — for the comparison and for repeats
- **Ask the user** what they found outside review (manual testing, Android Studio, lint or CI
  failures during implementation)

### 3. Analyse

Classify each finding: **fixed**, **skipped** (why — false positive, trade-off, deferred), or
**merged unaddressed**. For each fixed one, find the **root cause stage**: did the spec, the
plan, or the implementation introduce it? A plan-level defect is implemented faithfully, so
only the architect's self-checks could have caught it.

Look for patterns: the same mistake in several places, **a repeat of a lesson from an earlier
retro** (a lesson that never became a rule), framework-specific mistakes (Compose, coroutines,
Room) that suggest an outdated or incorrect pattern, and what the gate or reviewer should have
caught.

### 4. Route each lesson

For every lesson, pick exactly one destination:

| The lesson is… | It goes in… |
|---|---|
| A rule about the code | **`{{rules-home}}`** — the rule plus a one-line reason |
| Mechanically checkable | **A {{architecture-rules}} rule (in `{{architecture-rules-home}}`) or a lint rule** — prose gets violated, a rule can't be |
| About how a pipeline stage works | **The skill** — as a general process step |
| Something the automated reviewer should check | **The review prompt** in `{{review-prompt}}` |
| What happened and why | **RETRO.md only** |

**Test for anything going into `{{rules-home}}`, a skill or the review prompt: would it still be true and useful if this
feature had never existed?** If not, it is history — keep it in RETRO.md. Write rules as the
mechanism ("a condition that treats `null` and empty alike needs a test for each"), never as
the incident ("in feature X, phase 2 missed…"): no feature names, phase numbers, file names or
counts. A new {{architecture-rules}} or lint rule must be shown to fail on the code that prompted it.

### 5. Write RETRO.md

Save to `{{feature-docs}}/RETRO.md`:

```markdown
# Retrospective: {Feature Name}

PR: #{n} · Merged: {date} · pipeline: v{X.Y.Z from PLAN.md, or unstamped}

## Summary
What was built; findings by severity; how many fixed; how many merged unaddressed.

## Compared with earlier retros
The same metrics side by side (findings by severity, fixed before merge, merged unaddressed,
review waves), with each feature's pipeline version. Say which *kind* of finding changed, not
just the count, and whether it changed with a pipeline release.

## What went well
Correct patterns and decisions, with evidence from the diff.

## What the reviewer caught
Per finding: issue · file · root cause (and which stage introduced it) · fix · prevention.

## Issues found outside review

## Patterns to watch

## Recommended changes
### Coding rules (`{{rules-home}}`)
### {{architecture-rules}} / lint
### Skills
### Review prompt
Ready-to-paste text, each passing the test in step 4.

## Baseline for next time
The metrics table, and the target for the next feature.
```

### 6. Apply with approval

Present the recommended changes and ask which to apply. Apply the approved ones in the files
they belong to, commit them with RETRO.md, and open a docs PR.
