<!--
  ai-pipeline config template. Copy this file to your project as `.claude/pipeline.md` and replace
  every **Fill in:** value with your project's own (keep the backticks around commands and paths). Every
  name is required; `none` is allowed only where the third column says yes. Delete this comment.
  Each release's CHANGELOG says which names were added, so compare against the template of the
  version you install.
-->
# Pipeline config

The facts about this project that the pipeline skills and agents need. A name in double braces in
a skill or agent, such as `{{base}}`, stands for the value in this table.

Only facts go here — branches, paths, commands, tool names. How a stage works, and what is true of
every Android project, stays in the skills. `CLAUDE.md` stays the project's coding rules.

**Every value is required.** A stage that needs one that is missing or empty stops and says which;
it never guesses.

**`none` means the project has no such tool.** A step that uses it is skipped, and the stage's
report says so. `none` is not empty: an empty or missing value still stops the stage. Only names
marked in the table may be `none`.

**Write values as plain names and commands. Name a path only if it exists in this project.** A
project may check its AI-instruction files for paths that do not exist, and this file is one.

| Name | Value | May be `none` | Meaning |
|---|---|---|---|
| `{{base}}` | **Fill in:** the base branch, e.g. `master` or `main` | no | The branch features start from and PRs target |
| `{{feat-prefix}}` | **Fill in:** e.g. `feature/` | no | Branch prefix for a `feat` change |
| `{{fix-prefix}}` | **Fill in:** e.g. `fix/` | no | Branch prefix for a `fix` change |
| `{{retro-prefix}}` | **Fill in:** e.g. `docs/` | no | Branch prefix for a retrospective |
| `{{feature-docs}}` | **Fill in:** e.g. `docs/features/{feature-name}` | no | Folder holding a feature's `SPEC.md`, `PLAN.md` and `RETRO.md` |
| `{{hosting}}` | **Fill in:** GitHub, through the `gh` CLI — or — GitLab, through the `glab` CLI | no | The code host. Supported: GitHub (`gh`) and GitLab (`glab`); the skills have steps for each. Any other value means stop and say so |
| `{{issues}}` | **Fill in:** GitHub issues (`gh`), GitLab issues (`glab`), or `none` | yes | Where the product owner reads an issue: GitHub issues (`gh`), GitLab issues (`glab`), or `none` (the user pastes the text) |
| `{{pre-flight}}` | **Fill in:** the CI checks as one command, in CI's order, e.g. `./gradlew spotlessCheck test assembleDebug lint` | no | Mirrors CI: same tasks, same order. If the CI workflow changes, change this |
| `{{format-fix}}` | **Fill in:** e.g. `./gradlew spotlessApply`, or `none` | yes | Fixes the formatting step of `{{pre-flight}}` |
| `{{formatter}}` | **Fill in:** e.g. Spotless (ktlint), or `none` | yes | The formatting step of `{{pre-flight}}` |
| `{{verification}}` | **Fill in:** e.g. `./gradlew :{module}:testDebugUnitTest assembleDebug` (with flavours, the flavoured task) | no | A phase's verification command: one test task per module the phase touches |
| `{{architecture-rules}}` | **Fill in:** e.g. Konsist, ArchUnit, or `none` | yes | The tool enforcing architecture, layering and naming rules; it has a step in `{{pre-flight}}` |
| `{{architecture-rules-home}}` | **Fill in:** the module or folder of those rules, or `none` | yes | Where a new architecture rule is written |
| `{{rules}}` | **Fill in:** e.g. `CLAUDE.md`, or the files that hold the coding rules | no | The files holding the project's coding rules, read before planning or writing code |
| `{{rules-home}}` | **Fill in:** e.g. `CLAUDE.md` | no | Where a new coding rule is written |
| `{{rule-review}}` | **Fill in:** the sections or files of `{{rules}}` to review by hand, or `none` | yes | The sections or files of `{{rules}}` whose rules the toolchain cannot check |
| `{{lint}}` | **Fill in:** e.g. `./gradlew lint`; say whether warnings fail it — or `none` | yes | The lint task a phase adds when it touches UI or resources, and whether warnings fail it |
| `{{residue-extra}}` | **Fill in:** leftovers the project's tools do not flag, or `none` | yes | Residue the gate looks for beyond the skills' own list |
| `{{review}}` | **Fill in:** how an automated review is requested, e.g. comment `@claude-review` on the PR — or `none` | yes | How an automated review is requested (opt-in: it costs API credits) |
| `{{review-prompt}}` | **Fill in:** the file holding the reviewer's prompt, or `none` | yes | Where the automated reviewer's prompt is |

**Reading the pre-flight:** its output is long, so pipe it through `tail -n 60`, never redirect it
into a file. The pipe hides the exit code: read `BUILD SUCCESSFUL` or `BUILD FAILED` in the tail.
Gradle keeps the requested order, so the cheap checks fail first and the run stops at the first
failure.
