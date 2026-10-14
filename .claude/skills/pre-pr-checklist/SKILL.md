---
name: pre-pr-checklist
description: >
  Runs a pre-PR quality gate on a feature branch: checks that the PLAN.md was followed, that
  each SPEC.md acceptance criterion is covered, that no debug leftovers or TODOs are in the
  diff, and that the changed code follows CLAUDE.md's judgement rules, then runs a local mirror
  of CI and reports GO, CONDITIONAL GO or NO GO. Use when the user asks whether a branch is
  ready for a PR, or for a pre-PR check, quality gate or final check.
---

# Pre-PR Quality Gate

CI checks that the code is correct. This gate checks that it is **the code that was agreed**.

The checks themselves run in the **`pre-pr-gate` agent** (`.claude/agents/pre-pr-gate.md`),
not in this conversation. The conversation that wrote the code is the one most likely to judge
it generously; the agent starts with no account of what was built and only reads the branch. It
is read-only, so it reports problems and cannot fix its way to GO. This skill runs it, shows
the result, and handles everything that needs the user.

## Inputs

- **Feature name** — locates the feature's folder in the config's feature docs path. If not
  given, infer it from the branch name (the branch prefix dropped: `feature/user-profiles` →
  `user-profiles`) and confirm.

## Pipeline context

Read `.claude/pipeline.md` (the project config) before step 1; the agent reads it too. This skill
uses its **feature docs** path, **branch prefixes**, **format fix** command and **enforcement**
table, and the profile's **manual checks**. If one is missing, stop and tell the user which; do
not guess it.

## Workflow

### 1. Run the gate

Spawn the `pre-pr-gate` agent in the foreground and wait for its report. Give it the feature
name and nothing else — no summary of the work, no list of what you expect it to find. Its
independence is the point.

### 2. Show the report

Show the report as the agent returned it. Don't soften or re-grade it: if you disagree with an
item, say so separately and say why.

### 3. Follow up

- **GO** → offer to continue with the developer skill's PR step.
- **CONDITIONAL GO** → list the items. Unverified criteria and rule review findings need a
  decision from the user, not an automatic fix: take them one at a time, show the code and the
  finding, and ask whether to fix, verify manually, or dismiss. Then fix what was agreed.
- **NO GO** → show the errors and offer fixes within the limits below.

Limits on fixes: each pre-flight failure gets the response the config's **enforcement** table
gives for its tool, and nothing faster. In general:
- **Formatting** — run the config's format fix. The only failure that needs no judgment call.
- **A rule check** (architecture, lint) — fix the code to match the rule. Changing a rule, or
  adding a baseline or suppression, is a conversation with the user, not a silent edit.

When the user reports a manual check (one of the profile's manual checks), record it in the next
commit as a `Verified-manually:` trailer, as the developer skill does. The gate reads those
from git.

### 4. Re-run

Spawn the agent again after fixes. After a NO GO, re-run the whole gate. After a CONDITIONAL
GO, name the checks to re-run (e.g. "Check 3 and Check 5") — the agent runs only those.
