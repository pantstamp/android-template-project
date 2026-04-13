---
name: retrospective
description: >
  Run a post-merge retrospective for a completed feature. Reads the PR review
  comments, analyzes what Claude got right and wrong during implementation,
  and produces a RETRO.md with lessons learned and actionable improvements
  for CLAUDE.md.
  Use this skill after merging a feature PR, or when the user mentions
  "retrospective", "retro", "lessons learned", "what went wrong", "post-mortem",
  "what should we improve", "update CLAUDE.md from review", or "what did the
  reviewer find". Also trigger when the user wants to analyze review findings
  across multiple features to spot patterns.
---

# Post-Merge Retrospective

This skill produces a brief lessons-learned document after a feature is merged.
It matters because the patterns Claude gets wrong tend to repeat — if Claude
generated a non-suspend DAO method once, it'll do it again on the next feature
unless something changes. The retrospective captures these patterns and turns
them into concrete CLAUDE.md improvements, so each feature makes the next one
better.

## Inputs

The user provides:
- **Feature name** — used to locate `docs/features/{feature-name}/`
- **PR number** — used to fetch review comments from GitHub

If not specified, infer from the current branch or ask.

## Workflow

### 1. Gather data

Read these sources to build the full picture:

**a) PR review comments:**
```bash
gh api repos/{owner}/{repo}/pulls/{pr_number}/comments
```

Extract each comment's: file path, line, body (which includes severity and
the suggested fix).

**b) The PR diff — what actually changed:**
```bash
gh pr diff {pr_number}
```

**c) The feature's SPEC.md and PLAN.md** from `docs/features/{feature-name}/`
to understand what was intended.

**d) Any fix commits** — if the user triaged review comments and pushed fixes,
compare the original implementation to the fixes to understand what changed.

### 2. Analyze

Categorize each review finding into one of these buckets:

- **Caught and fixed** — reviewer found it, user fixed it. This is the most
  valuable category because it reveals what Claude's implementation stage
  gets wrong.
- **Caught but skipped** — reviewer found it, user decided not to fix. Worth
  noting why — was it a false positive? A style preference? A known tradeoff?
- **Not caught** — issues the user found during manual testing or code review
  in Android Studio that the automated reviewer missed. Ask the user if there
  were any of these.

For the "caught and fixed" category, look for patterns:
- Same type of mistake across multiple files (e.g., missing error handling)
- Mistakes that could have been prevented by a CLAUDE.md rule
- Mistakes that the pre-PR checklist should have caught
- Framework-specific issues (Compose, Coroutines, Room) that suggest Claude
  used an outdated or incorrect pattern

### 3. Produce RETRO.md

Save to:
```
docs/features/{feature-name}/RETRO.md
```

### RETRO.md structure

```markdown
# Retrospective: {Feature Name}

PR: #{pr_number}
Date: {date}

## Summary
One paragraph — what was built, how many review findings, how many fixed.

## What went well
Things Claude got right — patterns followed correctly, good architectural
decisions, clean code areas.

## What the reviewer caught

### Critical / Major (fixed)
For each:
- **Issue**: what was wrong
- **File**: where
- **Root cause**: why Claude generated it this way
- **Fix applied**: what changed
- **Prevention**: how to avoid this next time

### Minor / Suggestions (fixed or skipped)
Brief list with outcome (fixed / skipped / deferred).

## Issues found outside review
Anything the user found during manual testing or Android Studio review
that the automated reviewer missed.

## Patterns to watch
Recurring themes across this feature's findings. Examples:
- "Claude defaults to OnConflictStrategy.ABORT instead of REPLACE"
- "Claude forgets to make DAO write methods suspend"
- "Missing error handling on repository methods"

## Recommended CLAUDE.md updates
Specific lines to add to CLAUDE.md based on this retro. Format as
ready-to-copy text:

> When writing Room DAOs, always use `suspend` for write operations
> and return `Flow<T>` for read operations. Use
> `OnConflictStrategy.REPLACE` unless there's an explicit reason not to.

## Recommended review checklist updates
If the reviewer missed something that it should check for, note it here.
```

### 4. Suggest CLAUDE.md updates

After saving RETRO.md, present the recommended CLAUDE.md additions and ask
the user if they want to apply them. If yes, read the current CLAUDE.md,
find the appropriate section, and add the new rules.

This is the key step that closes the loop — review findings become
project rules that prevent the same mistakes on future features.

### 5. Cross-feature patterns (optional)

If there are multiple RETRO.md files in `docs/features/`, offer to scan
them for cross-feature patterns:

"You now have retros for {N} features. Want me to look across them for
recurring patterns? This can surface systemic issues worth addressing
in CLAUDE.md."

Read all RETRO.md files, identify findings that appear in 2+ features,
and present them as high-confidence CLAUDE.md additions.

## When to run this

The natural moment is right after merging the PR:

1. Merge the PR on GitHub
2. Back in Claude Code terminal:
   ```
   Run a retrospective for the user-profiles feature, PR #XX.
   ```
3. Review the RETRO.md
4. Approve or edit the CLAUDE.md updates
5. Commit both files

Over time, this builds a knowledge base in `docs/features/` that makes
CLAUDE.md increasingly precise, which makes each subsequent feature's
implementation and review cleaner.
