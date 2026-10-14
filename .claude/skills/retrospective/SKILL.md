---
name: retrospective
description: >
  Runs a post-merge retrospective for a feature's pull request: reads the review comments, the
  diff, the fix commits, SPEC.md and PLAN.md, writes a RETRO.md comparing against earlier
  retros, and routes each lesson to CLAUDE.md, a mechanical rule, a skill, or the review
  prompt. Use after a PR is merged when the user asks for a retro, retrospective, lessons learned, or to turn
  review findings into project rules.
---

# Post-Merge Retrospective

Mistakes repeat unless something changes. This turns a merged PR's review findings into
durable changes — project rules, mechanical checks, or process — and records what happened.

## Inputs

- **Feature name** — locates the feature's folder in the config's feature docs path
- **PR number**

If not given, infer from the branch or ask.

## Pipeline context

Read `.claude/pipeline.md` (the project config) and the platform profile it names before
step 1. This skill uses the config's **base branch** (`{base}` below), **branch prefixes**,
**feature docs** path (`{feature docs}`), **hosting**, **enforcement** tools and where a new
rule goes, and **review** mechanism, and the profile's **framework-specific mistakes**. If one is
missing, stop and tell the user which; do not guess it.

## Workflow

### 1. Branch

The feature branch is merged, so work on a new one:

```bash
git checkout {base} && git pull --ff-only
git checkout -b {docs prefix}{feature-name}-retro
```

### 2. Gather

- Inline review comments: `gh api repos/{owner}/{repo}/pulls/{n}/comments` — path, line,
  severity, suggestion, and whether a reply says it was fixed
- The review summary: `gh api repos/{owner}/{repo}/issues/{n}/comments`
- The PR diff (`gh pr diff {n}`) and its commits; compare the original commit with any fix
  commits
- Timeline: comment times against the last commit and the merge — any comment newer than the
  last commit was merged unaddressed
- SPEC.md and PLAN.md, and the `pipeline:` version in PLAN.md's header: the release that
  built the feature. A plan written before versioning has none; write `unstamped`
- Earlier `RETRO.md` files in the feature docs path — for the comparison and for repeats
- **Ask the user** what they found outside review (manual testing, the IDE, failures of the
  config's enforcement tools or CI during implementation)

### 3. Analyse

Classify each finding: **fixed**, **skipped** (why — false positive, trade-off, deferred), or
**merged unaddressed**. For each fixed one, find the **root cause stage**: did the spec, the
plan, or the implementation introduce it? A plan-level defect is implemented faithfully, so
only the architect's self-checks could have caught it.

Look for patterns: the same mistake in several places, **a repeat of a lesson from an earlier
retro** (a lesson that never became a rule), mistakes in the profile's
**framework-specific** areas that suggest an outdated or incorrect pattern, and what the gate or
reviewer should have caught.

### 4. Route each lesson

For every lesson, pick exactly one destination:

| The lesson is… | It goes in… |
|---|---|
| A rule about the code | **CLAUDE.md** — the rule plus a one-line reason |
| Mechanically checkable | **A mechanical rule**, where the config says new rules go — prose gets violated, a rule can't be |
| True of every project on this platform | **The platform profile** — an interview concern, plan checklist item or residue pattern |
| About how a pipeline stage works | **The skill** — as a general process step |
| Something the automated reviewer should check | **The review prompt**, where the config's review section says it is |
| What happened and why | **RETRO.md only** |

**Test for anything going into CLAUDE.md, the profile, a skill or the review prompt: would it
still be true and useful if this feature had never existed?** A skill must also stay true on
another platform: a lesson that names a platform's tools or APIs belongs in the profile or in
CLAUDE.md. If not, it is history — keep it in RETRO.md. Write rules as the
mechanism ("a condition that treats `null` and empty alike needs a test for each"), never as
the incident ("in feature X, phase 2 missed…"): no feature names, phase numbers, file names or
counts. A new mechanical rule must be shown to fail on the code that prompted it.

### 5. Write RETRO.md

Save to `{feature docs}/RETRO.md`:

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
### CLAUDE.md
### Mechanical rules
### Platform profile
### Skills
### Review prompt
Ready-to-paste text, each passing the test in step 4.

## Baseline for next time
The metrics table, and the target for the next feature.
```

### 6. Apply with approval

Present the recommended changes and ask which to apply. Apply the approved ones in the files
they belong to, commit them with RETRO.md, and open a docs PR.
