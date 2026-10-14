# Pipeline config

Where this project keeps the things the pipeline skills and agents need: branches, commands,
enforcement tools, the review mechanism. The skills hold the process, the platform profile holds
what is true of every project on the platform, and this file holds what is true of this one.
`CLAUDE.md` stays the project's coding rules; this file only says where things are.

**Every entry is required.** A skill or agent that needs an entry this file does not have stops
and says which one is missing. It never guesses a command or a branch.

## Platform profile

`android` — read [`profiles/android.md`](profiles/android.md).

## Git and hosting

- **Base branch:** `master`. Branches start from it, and PRs target it.
- **Branch prefixes:** `feature/` for a `feat` change, `fix/` for a `fix`, `docs/` for a
  retrospective.
- **Feature docs:** `docs/features/{feature-name}/` holds `SPEC.md`, `PLAN.md` and `RETRO.md`.
- **Hosting:** GitHub, through the `gh` CLI. The skills' issue, PR and review-comment commands
  are written for it.

## Commands

| Name | Command | Used for |
|---|---|---|
| Pre-flight | `./gradlew spotlessCheck :test:konsist:test test assembleDebug lint` | The gate's CI mirror, and before every push |
| Format fix | `./gradlew spotlessApply` | The only failure that needs no judgement call |
| Phase verification | `./gradlew :{module}:testDebugUnitTest assembleDebug` — one test task per module the phase touches | The command each PLAN.md phase ends with |
| UI check | `lint` | Appended to a phase's verification when the profile's UI-check condition holds |

**The pre-flight mirrors CI.** It has the same tasks in the same order as
`.github/workflows/build.yml`; if the workflow changes, change this line. Gradle keeps the
requested order, so the cheap checks fail first and the run stops at the first failure.

**Reading the result:** the output is long, so pipe it through `tail -n 60`. The pipe hides the
exit code: read `BUILD SUCCESSFUL` or `BUILD FAILED` in the tail. If the tail does not show the
cause, re-run only the failing task, or read the report file Gradle links to.

**Not covered by the pre-flight:** instrumented tests (`src/androidTest`). See `CLAUDE.md`.

## Enforcement

The checks the pre-flight runs, what each enforces, and what a failure calls for:

| Tool | Task | Enforces | On failure |
|---|---|---|---|
| Spotless | `spotlessCheck` | Formatting (ktlint) | Run the format fix. The run stopped here, so later tasks did not run. |
| Konsist | `:test:konsist:test` | Architecture, layering and naming rules | Change the code to match the rule. Changing a rule is a conversation with the user, not a silent edit. |
| Unit tests | `test` | Behaviour | Fix the code or the test, whichever is wrong. |
| Android Lint | `lint` | Android API, Compose and resource misuse, with `warningsAsErrors` | Read the HTML report the task links to and fix the cause. A baseline or suppression is the user's decision. |

**Where a new mechanical rule goes** (the retrospective's routing): an architecture, layering or
naming rule becomes a Konsist test in `test/konsist`. Anything Konsist cannot express goes to
Android Lint configuration. A new rule must be shown to fail on the code that prompted it.

## Review

- **Requested by** commenting `@claude-review` on the PR. It is opt-in, because it costs API
  credits.
- **Runs in** `.github/workflows/claude-review.yml`. The prompt in that file is where a lesson
  for the automated reviewer goes.

## Rule review

The `CLAUDE.md` sections whose rules the toolchain cannot check. The gate reviews the diff
against every bullet in them, and the architect holds the plan's code to them:

- **"Flow and coroutines"**
- **"Compose"**
- **"MVI pattern"**

## Residue the toolchain misses

Besides the profile's residue patterns, the gate looks for:

- **Unused imports** — Spotless in this project does not flag them.
