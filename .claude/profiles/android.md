# Platform profile: Android

What the pipeline knows about Android, true of any Android project. The project's own commands
and tools are in `.claude/pipeline.md`; its coding rules are in `CLAUDE.md`.

Each section is an extension point a skill or agent names. They are required: a skill that
needs a section missing here stops and says so.

## Interview concerns

The product owner uses both lists. Keep them short: every topic added here lengthens every
interview, and the golden scenario got worse when they grew.

### Gaps to look for

Before the interview: offline, lifecycle (rotation, process death), permissions, deep links,
back navigation, accessibility.

### Topics to cover

Where they apply:
- **Lifecycle** — rotation, process death, back navigation
- **Offline** — what works offline, what is cached, sync strategy
- **Accessibility** — content descriptions, touch targets, screen reader behaviour

## Plan checklist

The architect applies each item to every phase, and checks the plan against it before
presenting it.

- **UI check condition.** A phase that touches a composable or anything under `res/` adds the
  config's UI check (lint) to its verification. Lint is the only check that sees Compose and
  resource misuse.
- **Resources arrive with their first use.** Add each resource in the phase that first uses it:
  an unused resource is itself a lint error, so a resource added early fails that phase.
- **A `@Preview` per visual state.** A phase that adds or changes a screen lists one preview per
  state it can render: loading, each error variant, empty, content. CI compiles previews but
  never renders them, so a preview is often the only way to see a state that is hard to reach on
  a device.
- **Library types stay in their module.** A Room, Retrofit or Compose type named in a module that
  holds domain models or stable contracts means a change of library would reach into a module
  that should not care.

## Hard-to-reach UI states

For a UI criterion that is hard to reach on a device (an empty or rare error state), suggest a
`@Preview` of that state before any throwaway code change.

## Manual checks

What the user checks at a checkpoint, and records as `Verified-manually:`: behaviour on a device
or emulator, and previews in Android Studio. When checking on a device or emulator yourself,
confirm the precondition is on screen (screenshot or log) before acting. An action taken while a
spinner is still showing tests the spinner.

## Residue patterns

Debug output that compiles and passes every check: `println`, `print`, `System.out`,
`System.err`, `printStackTrace()`, and `Log.d` / `Log.v` / `Log.i` added for debugging.

## Framework-specific mistakes

Areas where a repeated mistake usually means an outdated or incorrect pattern, which the
retrospective looks for: Compose (recomposition, effects, state hoisting), coroutines and Flow
(cancellation, collection, buffering), Room (blocking queries, flows that never complete,
migrations).
