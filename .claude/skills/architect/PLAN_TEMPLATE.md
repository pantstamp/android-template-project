# PLAN.md template

The plan must be **self-contained**: a developer or agent can implement it with no context
beyond this file and `CLAUDE.md`. Phase titles are the change's own steps — the developer
skill reuses them in commit messages and the PR description.

```markdown
# Implementation Plan: {Feature Name}

## Overview
What the change does and the approach, in a few sentences. List the phases in one line each.

## References
- Spec: `docs/features/{feature-name}/SPEC.md`
- Project conventions: `CLAUDE.md`

## Decisions
The forks agreed with the user at the outline stage, one line each with the reason.

## Observations on existing code
Omit if empty.
- **Deviations** — where this plan deliberately does not follow an existing pattern, and
  why. Anything not recorded here will be implemented as the existing pattern.
- **Notes** — problems spotted nearby that this plan does not touch, for separate triage.

## Phase 1: {what this phase delivers}

### Files to modify
- `path/to/ExistingFile.kt` — what changes

### Files to create
- `path/to/NewFile.kt`
  - Class: `ClassName`
  - Key methods: `methodName(params): ReturnType`
  - Implementation notes: ...

### Database migration
If applicable — SQL, version bump, exported schema.

### Previews to add
If the phase adds or changes a screen — one per visual state, with the `UiState` it renders.

### Tests
- `path/to/FooTest.kt`
  - `shouldDoXWhenY` — what it asserts

### Verification
```bash
./gradlew :module:testDebugUnitTest assembleDebug        # add lint if UI or res/ changed
```

## Phase 2: {what this phase delivers}
(same structure)

## Implementation Notes
Cross-cutting concerns, gotchas, and anything a later phase depends on.
```
