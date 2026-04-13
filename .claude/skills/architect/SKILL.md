---
name: architect
description: >
  Create a detailed technical implementation plan (PLAN.md) for a feature that has
  a completed SPEC.md. Analyzes the existing codebase, proposes changes organized
  into self-contained buildable phases, and produces a plan that another developer
  or AI agent can execute without additional context.
  Use this skill when the user wants to plan, architect, or design the implementation
  of a feature — including phrases like "create a plan", "architecture", "technical design",
  "implementation plan", "how should we build this", "plan this feature", or when the user
  has a SPEC.md and wants to move to the implementation planning stage.
  Also trigger when the user asks to break a feature into phases or create a PLAN.md.
---

# Software Architect

This skill produces a self-contained implementation plan from a feature specification.
The plan is detailed enough that a developer (human or AI) can execute it phase by phase
without needing to ask clarifying questions. This matters because vague plans lead to
wrong assumptions, architectural drift, and rework.

## Inputs

The user provides:
- **Feature name** — used to locate `docs/features/{feature-name}/SPEC.md`

If the user doesn't specify a feature name, check `docs/features/` for the most recent
SPEC.md and confirm with the user.

## Workflow

### 1. Read project context

Read these files in order — each one informs the plan:

1. **CLAUDE.md** — project conventions, patterns, and constraints
2. **docs/features/{feature-name}/SPEC.md** — what we're building
3. **Architecture docs** — scan `docs/` for any architectural guidelines
4. **Existing code** — study the module structure, navigation graph, database setup,
   dependency injection configuration, and the area of the codebase most relevant
   to this feature

The goal is to understand existing patterns deeply so the plan follows them exactly.
Introducing new patterns or libraries that aren't already in the project causes
inconsistency and maintenance burden — avoid it.

### 2. Draft the plan

Structure the plan around these layers, in dependency order:

1. **Data Layer** — Room entities, DAOs, database migrations, data models
2. **Domain Layer** — Use cases, repository interfaces and implementations
3. **UI Layer** — ViewModels, Composables/screens, navigation changes
4. **Testing** — Unit tests, integration tests

For each layer, specify:
- Which **existing files** will be modified and what changes are needed
- What **new files** need to be created, with exact file paths
- **Class names, method signatures, and key implementation details**
- The **verification step** to confirm the phase is complete (build command or test)

### 3. Discuss with the user

Present the plan and explicitly ask for feedback before finalizing. This is a
collaborative step — the user may have context about upcoming changes, team
preferences, or constraints that aren't in the codebase.

Common things to discuss:
- "I noticed the project uses pattern X for Y — I'll follow the same approach here."
- "There's a choice between approach A and B for this part — here's the tradeoff."
- "This phase depends on Z — should we handle that differently?"

Incorporate feedback and iterate until the user approves.

### 4. Save PLAN.md

Save the finalized plan to:

```
docs/features/{feature-name}/PLAN.md
```

### PLAN.md structure

The plan must be **self-contained** — a developer or AI agent should be able to pick it
up and implement the feature without any additional context beyond the project's own
CLAUDE.md.

```markdown
# Implementation Plan: {Feature Name}

## Overview
Brief summary of the feature and high-level approach.

## References
- Spec: `docs/features/{feature-name}/SPEC.md`
- Project conventions: `CLAUDE.md`

## Phase 1: Data Layer

### Files to modify
- `path/to/ExistingFile.kt` — description of changes

### Files to create
- `path/to/NewFile.kt`
  - Class: `ClassName`
  - Key methods: `methodName(params): ReturnType`
  - Implementation notes: ...

### Database migration
If applicable — migration SQL, version bump, etc.

### Verification
```bash
./gradlew assembleDebug
```

## Phase 2: Domain Layer
(Same structure as Phase 1)

## Phase 3: UI Layer
(Same structure as Phase 1)

## Phase 4: Testing

### Unit tests to create
- `path/to/TestFile.kt`
  - Test cases: what each test verifies

### Verification
```bash
./gradlew testDebugUnitTest
```

## Implementation Notes
Any cross-cutting concerns, gotchas, or decisions worth documenting.
```

### 5. Confirm with the user

After saving, tell the user the file path and confirm they're ready to move to
implementation. Remind them they can use the developer skill to execute the plan
phase by phase.

## Key principles

- **Follow existing patterns exactly.** Study how the codebase does things today and
  replicate that approach. If the project uses `UseCase` classes with `invoke()` operators,
  do the same. If repositories return `Flow<T>`, follow suit.

- **No new libraries or patterns.** Unless the user explicitly approves, don't introduce
  dependencies or architectural patterns that aren't already in the project.

- **Each phase must be self-contained and buildable.** After completing any single phase,
  the project should compile. This means phases are ordered by dependency — data before
  domain before UI.

- **Be specific.** Vague instructions like "create a ViewModel" are not useful. Specify
  the class name, the state it holds, the methods it exposes, which use cases it calls,
  and how the UI observes it.

- **Use Opus for this stage.** Architecture discussions benefit from deeper reasoning.
  Remind the user to switch models if they haven't: `/model opus`
