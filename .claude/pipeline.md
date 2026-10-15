# Pipeline config

The facts about this project that the pipeline skills and agents need. A name in double braces in
a skill or agent, such as `{{base}}`, stands for the value in this table.

Only facts go here — branches, paths, commands, tool names. How a stage works, and what is true of
every Android project, stays in the skills. `CLAUDE.md` stays the project's coding rules.

**Every value is required.** A stage that needs one that is missing or empty stops and says which;
it never guesses.

| Name | Value | Meaning |
|---|---|---|
| `{{base}}` | `master` | The branch features start from and PRs target |
| `{{feat-prefix}}` | `feature/` | Branch prefix for a `feat` change |
| `{{fix-prefix}}` | `fix/` | Branch prefix for a `fix` change |
| `{{retro-prefix}}` | `docs/` | Branch prefix for a retrospective |
| `{{feature-docs}}` | `docs/features/{feature-name}` | Folder holding a feature's `SPEC.md`, `PLAN.md` and `RETRO.md` |
| `{{hosting}}` | GitHub, through the `gh` CLI | The skills' issue, PR and review-comment commands are written for it; any other value means stop and say so |
| `{{pre-flight}}` | `./gradlew spotlessCheck :test:konsist:test test assembleDebug lint` | Mirrors CI (`.github/workflows/build.yml`): same tasks, same order. If the workflow changes, change this |
| `{{format-fix}}` | `./gradlew spotlessApply` | Fixes the formatting step of `{{pre-flight}}` |
| `{{formatter}}` | Spotless (ktlint) | The formatting step of `{{pre-flight}}`: `spotlessCheck` |
| `{{verification}}` | `./gradlew :{module}:testDebugUnitTest assembleDebug` | A phase's verification command: one test task per module the phase touches |
| `{{architecture-rules}}` | Konsist | The tool enforcing architecture, layering and naming rules; its step in `{{pre-flight}}` is `:test:konsist:test` |
| `{{architecture-rules-home}}` | `test/konsist` | Where a new architecture rule is written |
| `{{rule-review}}` | "Flow and coroutines", "Compose", "MVI pattern" | The `CLAUDE.md` sections whose rules the toolchain cannot check |
| `{{residue-extra}}` | unused imports — Spotless in this project does not flag them | Residue the gate looks for beyond the skills' own list |
| `{{review}}` | comment `@claude-review` on the PR | How an automated review is requested (opt-in: it costs API credits) |
| `{{review-prompt}}` | `.github/workflows/claude-review.yml` | Where the automated reviewer's prompt is |

**Reading the pre-flight:** its output is long, so pipe it through `tail -n 60`, never redirect it
into a file. The pipe hides the exit code: read `BUILD SUCCESSFUL` or `BUILD FAILED` in the tail.
Gradle keeps the requested order, so the cheap checks fail first and the run stops at the first
failure.
