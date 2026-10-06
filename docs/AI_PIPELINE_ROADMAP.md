# AI pipeline roadmap — from this project to a reusable package

Status: **proposed.** Step 1 is in progress: [`docs/features/agent-pipeline/IMPROVEMENTS.md`](features/agent-pipeline/IMPROVEMENTS.md).

The pipeline (`.claude/skills/`, guide in [`AI_WORKFLOW.md`](AI_WORKFLOW.md)) was built for
this Android project. This roadmap covers three goals:

1. Separating the generic process from platform and project context.
2. Packaging the pipeline so other codebases (Android, Flutter, iOS or other platforms) can install it.
3. Versioning it, so a change that makes it worse can be detected and rolled back.

## Order

1. **Finish the agent improvements** in `IMPROVEMENTS.md`, in this repo.
2. **Write golden scenarios before refactoring** (see §3). They are the safety net for step 3.
3. **Split the layers inside this repo**: generic skills, project config, Android profile. Re-run
   the scenarios; Android must not get worse.
4. **Extract to a plugin and marketplace** with semver, and pin this repo to `v1.0.0`.
5. **Write a second profile** when a real Flutter or iOS project adopts it, not before. With
   one platform you can't yet tell what is really generic; the second one shows you.

---

## 1. Generic process vs platform and project context

The process (interview → outline gate → phased plan → checkpoints → gate → triage → retro) is
already generic. The specific context sits in a known set of places:

| What | Examples in the skills today | Layer |
|---|---|---|
| Commands | `./gradlew spotlessCheck :test:konsist:test test assembleDebug lint`, `spotlessApply`, the per-phase verification template | Project |
| Git and hosting | the `master` base branch, `feature/`/`fix/` prefixes, `gh` and the GitHub API, `@claude-review` | Project |
| Paths | `docs/features/{name}/` | Project |
| Enforcement tools | "a Konsist or lint rule" in retro routing, `warningsAsErrors` handling | Platform + project |
| Interview concerns | offline, lifecycle (rotation, process death), permissions, deep links | Platform |
| Plan checklist | `@Preview` per visual state, a resource added in the phase that first uses it, Room/Retrofit/Compose types leaking into modules | Platform |
| Residue patterns | `Log.d`, `println` | Platform |
| Rule review (gate Check 5) | CLAUDE.md's Flow, Compose and MVI sections | Project |

### Three layers

```
generic pipeline        → process: stages, checkpoints, gates, templates, routing rules
  + platform profile    → profiles/android.md, flutter.md, ios.md
  + project config      → .claude/pipeline.md in each repo, plus that repo's CLAUDE.md
```

- **Generic skills** describe *what* happens at each step and name an **extension point**
  wherever context is needed:
  - "run the **preflight** command from the pipeline config", not a gradle line
  - "cover the **platform concerns** from the profile", not an Android list
- **Platform profile**: a reference file per platform, holding:
  - interview concerns: Android process death ↔ iOS backgrounding and state restoration ↔
    Flutter widget lifecycle
  - plan checklist items: Compose previews ↔ SwiftUI previews ↔ golden tests
  - residue patterns: `Log.d` ↔ `print`/`NSLog` ↔ `debugPrint`
  - enforcement tools to suggest: Konsist ↔ SwiftLint custom rules ↔ `custom_lint`/`dart analyze`
- **Project config**: `.claude/pipeline.md`, holding:
  - base branch, branch prefixes and docs path
  - commands: preflight, format-fix, per-module test template, UI check
  - review mechanism
  - enforcement destinations (e.g. "architecture rules → `test/konsist`")
  - the CLAUDE.md sections the gate's rule review reads

  CLAUDE.md stays the project's coding rules; the config only says where things are.

### Rules that keep the generic pipeline from being worse

1. **Missing context is an error, not a fallback.** Every extension point is required. If
   `preflight` is not defined, the gate stops and says so instead of guessing a command.
2. **`/pipeline-init` bootstraps the config.** It reads the CI workflow, build files and
   CLAUDE.md, proposes a config, and the user approves it.
3. **Commands come from CI where possible.** The preflight command is currently written in three
   places (`build.yml`, `AI_WORKFLOW.md`, the gate skill). Hold it once.
4. **Profiles are allowed to be rich.** "Generic" applies to the process, not the knowledge; the
   Android profile keeps everything the skills know today.
5. **This repo is the reference consumer.** The golden scenarios must pass at least as well after
   the split as before it.

## 2. Packaging

**A Claude Code plugin**, distributed through a **marketplace** (a git repo such as
`pantstamp/ai-pipeline` with `.claude-plugin/marketplace.json`).

- The plugin holds `skills/`, `agents/`, templates and `profiles/`, under a
  `.claude-plugin/plugin.json` manifest.
- **Installing:**
  - `/plugin marketplace add …` then `/plugin install …`, or
  - better for a project: declare the marketplace and the enabled plugin in the project's
    committed `.claude/settings.json`, so anyone opening the repo is prompted to install it.
- **Local development:** `claude --plugin-dir ../ai-pipeline` loads a working copy without
  publishing.
- **Note:** plugin skills are namespaced (e.g. `/ai-pipeline:architect`); the docs must use
  those names.

| In the plugin (shared) | In each project (owned by it) |
|---|---|
| generic skills and agents | `.claude/pipeline.md` |
| `PLAN_TEMPLATE.md`, `REPORT_FORMAT.md` | `CLAUDE.md` |
| platform profiles | `docs/features/*` (SPEC, PLAN, RETRO) |
| `/pipeline-init` | CI and review workflows |
| a template for the review workflow | enforcement code (Konsist, lint, …) |

The `@claude-review` workflow is a CI file and can't live in a plugin. `/pipeline-init` offers
to install it from a template.

**Alternatives considered:**
- A git submodule or a copy-in script: no clean update or pin story.
- Agent Skills (`SKILL.md`) as a cross-tool format: worth keeping compatible with, but the plugin
  is the container for Claude Code.

**Verify before building:** the manifest and marketplace field names against the Claude Code
version in use.

## 3. Versioning and rollback

Rolling back is easy; **noticing that a change made things worse** is the hard part.

### Mechanics

- **One version for the whole pipeline**, not per skill. The stages share contracts: the gate
  reads the PLAN format, and the retro reads the gate report.
- **Semver in `plugin.json`, a git tag per release, and a CHANGELOG** saying per stage what
  changed and why.
- **Each project pins a version** through its marketplace entry's git ref. Rollback is moving the
  pin back.
- **Until extraction:** commits that touch the pipeline use the `chore(pipeline): …` prefix, and
  releases are tagged `pipeline-vX.Y.Z`. Roll back with `git checkout pipeline-vX.Y.Z -- .claude/`.

### Detecting regressions

1. **Stamp the version on every artifact.** SPEC, PLAN and RETRO headers record
   `pipeline: vX.Y.Z`. Retros already count review findings by severity per feature; with the
   version, they show trends across releases. This is lagging and noisy, but it is real-world data.
2. **Golden-scenario evals**, the leading indicator, run before tagging a release:
   - **Gate:** the movie-search branch before the PR #32 review fixes. How many of the known
     findings does the rule review (Check 5) catch?
   - **Architect:** the watched-movies SPEC. Does the plan record the blocking-DAO deviation,
     list a preview per state, and give each phase a verification command?
   - **Product owner:** a fixed raw requirement. Are error, empty and offline states covered, and
     are open questions recorded?
   - **Developer:** a small throwaway plan with a deliberately broken phase. Does it return
     `BLOCKED` instead of improvising?

   Model output varies between runs, so run each scenario several times and compare rates
   ("catches ≥4 of 6 findings in 3 of 3 runs"), not single results.
3. **Tooling to evaluate before building a harness:**
   - `claude plugin eval` (plugin eval suites)
   - the `skill-creator` skill (skill evals and benchmarks with variance analysis)
