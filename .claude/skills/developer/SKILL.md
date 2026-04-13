---
name: developer
description: >
  Implement a feature phase by phase following a PLAN.md, with human approval gates
  between phases. Handles the full cycle from branch creation through implementation,
  verification, and PR creation.
  Use this skill when the user wants to implement, build, or code a planned feature —
  including phrases like "implement this", "start coding", "build the feature",
  "execute the plan", "start phase 1", "continue implementation", "create the PR",
  or when the user has a PLAN.md and wants to begin or continue development.
  Also trigger when the user asks to resume implementation from a specific phase.
---

# Software Developer

This skill implements a feature by following a PLAN.md phase by phase, with human
checkpoints between each phase. The phased approach matters because it catches
problems early — a data layer bug found after building the UI costs much more to
fix than one caught right after the data layer phase.

## Inputs

The user provides:
- **Feature name** — used to locate `docs/features/{feature-name}/PLAN.md`
- **Starting phase** (optional) — if resuming, which phase to start from

If the user doesn't specify a feature name, check `docs/features/` for the most
recent PLAN.md and confirm.

## Workflow

### 1. Read context and set up

Read these files before writing any code:
1. **CLAUDE.md** — project conventions and coding standards
2. **docs/features/{feature-name}/PLAN.md** — the implementation plan
3. **docs/features/{feature-name}/SPEC.md** — the feature spec (for context on *why*)

Then set up the working branch:
```bash
git checkout -b feature/{feature-name}
```

If the branch already exists (resuming work), just check it out:
```bash
git checkout feature/{feature-name}
```

Tell the user which branch you're on and which phase you're starting with.

### 2. Implement phase by phase

For each phase in PLAN.md:

**a) Implement exactly what the plan specifies.**
- No additions, no deviations, no "while I'm here" improvements
- Follow existing code patterns from CLAUDE.md and the surrounding codebase
- If something in the plan seems wrong, stop and ask rather than improvising

**b) Run the verification step specified in the plan.**
- Typically `./gradlew assembleDebug` for build verification
- Or `./gradlew testDebugUnitTest` for test verification
- Report the result — pass or fail, with error details if it failed

**c) Report what you did.**
Keep the summary concise:
- Files created / modified (list them)
- Key implementation decisions (if any)
- Verification result (pass / fail)

**d) Wait for approval.**
Do NOT proceed to the next phase until the user says to continue.
This is the human checkpoint — they may want to review in Android Studio,
run the app, or ask questions before proceeding.

### 3. Handle phase failures

If the verification step fails:
- Read the error carefully
- Fix the issue
- Re-run verification
- Report the fix and the new result

If the fix isn't obvious, show the error to the user and discuss before
attempting a fix.

### 4. Context management

Implementation across 4 phases can fill up the context window. Watch for signs
of degradation (slower responses, less precise output) and proactively manage:

- **Between phases:** Use `/compact` with a focus note:
  ```
  /compact focus on PLAN.md progress, current phase, and list of modified files
  ```

- **If context is very full:** Use `/clear` and restart with:
  ```
  Read docs/features/{feature-name}/PLAN.md and CLAUDE.md.
  We are implementing the {feature-name} feature.
  Phases 1 through {N} are complete. Continue with Phase {N+1}.
  ```

  This works because PLAN.md is self-contained — Claude can pick up at any
  phase without needing the prior conversation.

### 5. Pre-PR quality gate (recommended)

After all phases are complete, suggest running the pre-PR checklist:

"All phases are done. Want me to run the pre-PR quality gate before
creating the PR? It checks build, tests, lint, plan coverage, and
scans for common mistakes."

If the user agrees, hand off to the **pre-pr-checklist** skill.
If they skip it, proceed directly to PR creation.

### 6. PR creation (after quality gate or user approval)

Once the pre-PR checklist passes (or the user skips it):

**a) Final verification** (skip if the pre-PR checklist already ran these):
```bash
./gradlew assembleDebug
./gradlew testDebugUnitTest
./gradlew ktlintFormat
```

**b) Commit:**
```bash
git add -A
git commit -m "feat({feature-name}): {concise description}

- Phase 1: Data layer — {summary}
- Phase 2: Domain layer — {summary}
- Phase 3: UI layer — {summary}
- Phase 4: Testing — {summary}

Spec: docs/features/{feature-name}/SPEC.md
Plan: docs/features/{feature-name}/PLAN.md"
```

Use conventional commits format. If there were multiple commits during development,
that's fine — no need to squash.

**c) Push and create PR:**
```bash
git push -u origin feature/{feature-name}
gh pr create --base develop --title "feat: {Feature Title}" --body "$(cat <<'EOF'
## Summary
{One paragraph: what this PR implements and why}

## Changes by layer

### Data Layer
- {key changes}

### Domain Layer
- {key changes}

### UI Layer
- {key changes}

### Testing
- {tests added and what they cover}

## References
- Spec: `docs/features/{feature-name}/SPEC.md`
- Plan: `docs/features/{feature-name}/PLAN.md`

## Checklist
- [ ] Code review
- [ ] Manual testing on device
- [ ] UI matches expected behavior
EOF
)"
```

Do NOT merge the PR — just create it. The automated review workflow will
pick it up from here.

### 7. Confirm completion

Tell the user:
- The PR number and link
- That the automated code review will run via GitHub Actions
- They can triage review comments later using:
  ```
  Fetch the inline review comments from PR #{number} in this repo using gh api.
  Walk me through each one. For each finding, review it and show me the code
  and the suggestion, then wait for my decision before proceeding.
  ```

## Key principles

- **Stick to the plan.** The plan was reviewed and approved in the architecture stage.
  Deviating during implementation introduces unreviewed design decisions.

- **One phase at a time.** Never jump ahead. The human checkpoint between phases is
  where the user catches problems while they're still cheap to fix.

- **Use Sonnet for implementation.** It's the best coding model and faster than Opus.
  Remind the user to switch if they're still on Opus: `/model sonnet`

- **Report honestly.** If a test fails, say so. If something in the plan doesn't work
  as expected, surface it. The user needs accurate information to make decisions.

- **Keep commits clean.** Use conventional commits, include the feature name, and
  reference the spec and plan.
