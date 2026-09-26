---
name: architect
description: >
  Create a detailed technical implementation plan (PLAN.md) for a feature that has
  a completed SPEC.md. Analyzes the existing codebase, agrees the shape of the work
  with the user, then organizes changes into self-contained buildable phases that
  another developer or AI agent can execute without additional context.
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

The user owns the feature. The plan is a proposal until they agree to it, and the shape
of the work is agreed before any detail is written.

## Inputs

The user provides:
- **Feature name** — used to locate `docs/features/{feature-name}/SPEC.md`

If the user doesn't specify a feature name, check `docs/features/` for the most recent
SPEC.md and confirm with the user.

## Workflow

### 1. Read project context — and judge it

Read these files in order — each one informs the plan:

1. **CLAUDE.md** — project conventions, patterns, and constraints
2. **docs/features/{feature-name}/SPEC.md** — what we're building
3. **Architecture docs** — scan `docs/` for any architectural guidelines
4. **Existing code** — study the module structure, navigation graph, database setup,
   dependency injection configuration, and the area of the codebase most relevant
   to this feature

The goal is to understand existing patterns deeply so the plan follows them. Introducing
new patterns or libraries that aren't already in the project causes inconsistency and
maintenance burden — avoid it.

**But reading the code is also the moment to judge it.** "Follow the existing pattern"
propagates whatever is already wrong. `WatchedMovieDao.getWatchedMovieEntity()` was written
as a blocking query because `MovieDao.getMovieEntity()` already was one, so the same defect
shipped twice and survived for five months. Copying a defect is not consistency.

Sort what you notice into two piles:

- **Wrong, and this feature would copy it** → the plan must deviate deliberately and record
  why. This is not optional. The developer skill is told to follow surrounding patterns and
  to implement the plan exactly, so a deviation that isn't written down will be implemented
  as the old pattern.
- **Wrong, but unrelated to this feature** → an observation for the user to triage
  separately.

Do not fix anything at this stage, and do not widen the plan to absorb cleanup. Scope is
the user's call, and a feature plan that quietly becomes a refactor is the thing the phased
approach exists to prevent.

### 2. Agree the shape before writing the detail

Present an **outline** and wait for approval.

Keep it to a page, in plain language — someone who has not read the codebase should be able
to follow it. Two or three sentences per phase: what it builds, and how you will know it
works. No file paths, no method signatures.

What makes this step worth having is **the decisions, not the phase list**. Surface every
fork where there was a real choice:

- "Store the rating locally only, or sync it? The spec doesn't say."
- "This can hang off the existing repository or get its own. Here's the trade-off."
- "Phase 3 needs a schema migration. That's the one irreversible step — worth agreeing now."
- "The existing pattern here does X, which I think is wrong for this case. I plan to do Y
  instead. Do you agree?"

Then ask plainly: does this shape look right, and which way on the open questions?

**Why this comes before the detail.** A plan for a feature of ordinary size runs past a
thousand lines — the watched-movies spec was 156 lines and produced a 1,313-line plan.
Presenting that first and asking for feedback makes the review a rubber stamp, and a change
to the phasing at that point throws away everything written downstream of it. An outline is
something the user can actually disagree with.

If the feature is small enough that the outline and the plan are nearly the same document,
say so and keep this to a few lines. The gate exists to catch a wrong shape, not to add
ceremony.

### 3. Draft the plan

Structure the plan around these layers, in dependency order:

1. **Data Layer** — Room entities, DAOs, database migrations, data models
2. **Domain Layer** — Use cases, repository interfaces and implementations
3. **UI Layer** — ViewModels, Composables/screens, navigation changes
4. **Testing** — Unit tests, integration tests

For each layer, specify:
- Which **existing files** will be modified and what changes are needed
- What **new files** need to be created, with exact file paths
- **Class names, method signatures, and key implementation details**
- The **verification step** to confirm the phase is complete (build command or test).
  If the phase touches a composable or anything under `res/`, the command includes `lint`.
  The project runs lint with `warningsAsErrors`, and lint is the only check that sees
  Compose resource misuse. In discover-load-error-recovery, Phase 2's check was
  `testDebugUnitTest assembleDebug spotlessCheck`. A `LocalContext.current.getString()` it
  added went unnoticed until Phase 3's check ran lint, a phase later than it should have.
- For a phase that adds or changes a screen, the **`@Preview`s to add**: one per visual
  state the screen can render (loading, each error variant, empty, content), listed next to
  the composables. CI compiles previews but never renders them, so they are often the only
  way to see a state that can't be reached on a device. In discover-load-error-recovery the
  plan left them out, the empty state was never seen, and review had to ask for them.

#### Before presenting it, answer these

Concrete questions, not a compliance checklist. Most of Clean Architecture and dependency
inversion is already **Konsist-enforced** here — the layer dependencies, ViewModels taking
use cases rather than repositories, mappers implementing interfaces, `@Dao` functions being
suspend. Don't assert in prose that the plan honours a rule the build will check; the rules
run in verification and in CI. Spend the effort on what they can't check:

- **Is each new use case doing one thing?** One that fetches and also saves is two use
  cases wearing one name.
- **Does an interface grow past what its callers need?** Adding a fifth method to a
  repository because one caller wants it is worth a second look.
- **Does the plan introduce an abstraction with exactly one implementor that will never
  have another?** Prefer the concrete class until the second implementor exists. Two mapper
  interfaces were once added to `:architecture:mapper` for a single pair of classes in one
  module, and had to be moved back out.
- **Would a change of library reach into a module that shouldn't care?** A type belonging
  to Room, Retrofit or Compose named in `:architecture:mapper` or `:core:model` is the
  signal. `:architecture:mapper` holds only boundaries that survive swapping the library.
- **Does every phase leave the project compiling on its own?**
- **Does every phase that touches a composable or a resource run `lint`, and does every
  new screen state have a preview listed?**

If any answer is uncomfortable, change the plan before presenting it — or raise it as an
open question when you do.

### 4. Discuss with the user

Present the plan and explicitly ask for feedback before finalizing. This is a collaborative
step — the user may have context about upcoming changes, team preferences, or constraints
that aren't in the codebase.

Walk through it phase by phase in plain words first, then let them read the detail. If a
phase can't be described in two or three plain sentences, that is usually a sign the
phasing is wrong rather than a sign the explanation is hard.

Incorporate feedback and iterate until the user approves.

### 5. Save PLAN.md

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

## Observations on existing code
Omit this section if there is nothing to say. Two kinds of entry:

- **Deviations** — where this feature deliberately does not follow an existing pattern,
  and why. Anything not recorded here will be implemented as the existing pattern,
  because that is what the developer skill is told to do.
- **Notes** — problems spotted nearby that this feature does not touch, for the user to
  triage separately. Not work for this plan.

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
(Same structure as Phase 1, plus:)

### Previews to add
- `PreviewFooLoading`, `PreviewFooError`, `PreviewFooEmpty`, `PreviewFooContent` — one per
  visual state, with the `UiState` each one renders

### Verification
```bash
./gradlew assembleDebug lint
```

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

### 6. Confirm with the user

After saving, tell the user the file path and confirm they're ready to move to
implementation. Remind them they can use the developer skill to execute the plan
phase by phase.

## Key principles

- **Follow existing patterns — but not existing mistakes.** Study how the codebase does
  things today and replicate that approach. Where the existing approach is wrong for this
  feature, deviate and write down why (step 1). Silent deviation and silent copying are
  both failures; the difference is whether the reason is on the page.

- **No new libraries or patterns.** Unless the user explicitly approves, don't introduce
  dependencies or architectural patterns that aren't already in the project.

- **Agree the shape before the detail.** The outline is the user's decision point. A
  thousand-line plan presented for approval is not something anyone can meaningfully
  approve.

- **Each phase must be self-contained and buildable.** After completing any single phase,
  the project should compile. This means phases are ordered by dependency — data before
  domain before UI.

- **Be specific.** Vague instructions like "create a ViewModel" are not useful. Specify
  the class name, the state it holds, the methods it exposes, which use cases it calls,
  and how the UI observes it.

- **Report, don't repair.** Problems noticed outside the feature's scope are observations
  for the user, not phases in this plan.

- **Use Opus for this stage.** Architecture discussions benefit from deeper reasoning.
  Remind the user to switch models if they haven't: `/model opus`
