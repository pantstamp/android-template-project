# Contributing

This is a single-maintainer repository. The workflow below is written down anyway — partly so it
stays consistent over time, partly because a forked template should say how it expects to be worked
on.

---

## Branching model

**Trunk-based.** `master` is the only long-lived branch. Everything else is short-lived and merges
back through a pull request.

```
feature/watched-movies ──PR──► master ──tag──► v1.1.0
```

`master` is always releasable: it builds, its tests pass, and its architecture rules hold. That is
enforced by CI rather than by discipline.

### Why not git-flow?

This repository used to carry a `develop` branch alongside `master`. It was removed because it was
not doing anything: the two branches sat on the same commit for months, and every change cost two
pull requests — the second one reviewing a diff that had already been reviewed.

`develop` earns its place when several people integrate work before a scheduled release. With one
maintainer and continuous delivery, it is ceremony. Tags mark known-good states more precisely than
a second branch ever did.

If this template is adopted by a team that does run release trains, adding a release branch back is
a small change. The point is to choose it deliberately rather than inherit it.

### Branch naming

| Prefix | For |
|---|---|
| `feature/` | New functionality |
| `fix/` | Bug fixes |
| `chore/` | Build, tooling, dependencies, housekeeping |
| `docs/` | Documentation only |
| `spike/` | Exploration not intended to merge |

Spikes are allowed to stay unmerged. `chore/agp9-experiment` is one: it records what an AGP 9
migration would require and why it was not adopted.

---

## Day-to-day

```bash
git checkout master && git pull
git checkout -b feature/thing

# ... work ...

./gradlew spotlessApply          # format before committing
git commit -m "feat: add thing"
git push -u origin feature/thing

gh pr create --fill              # targets master by default
```

Then: wait for CI, **read your own diff on the PR page**, and merge when green.

Self-review is a habit rather than a rule here — GitHub will not let you approve your own pull
request, so requiring an approval would only ever be satisfied by an admin bypass. Reading the diff
in the PR view catches things that are invisible in an editor, so it is worth the two minutes.

### Before pushing

CI will run these, but running them locally is faster than waiting:

```bash
./gradlew spotlessCheck        # formatting
./gradlew assembleDebug        # compiles
./gradlew test                 # unit tests
./gradlew :test:konsist:test   # architecture rules
./gradlew lint                 # Android Lint
```

Requires **JDK 21** to run Gradle. The JDK used to compile is downloaded automatically by the
toolchain resolver, so no other JDK installation is needed.

### Commit messages

[Conventional Commits](https://www.conventionalcommits.org): `feat:`, `fix:`, `build:`, `ci:`,
`docs:`, `test:`, `refactor:`, `chore:`.

Explain **why** in the body when the reason is not obvious from the diff. Version bumps that look
routine but are not — anything held back on purpose — should say so, otherwise the next person to
read the version catalog will "fix" it.

---

## Continuous integration

`.github/workflows/build.yml` runs on every pull request and on every push to `master`:

| Step | Fails the build when |
|---|---|
| `spotlessCheck` | Formatting does not match ktlint config |
| `assembleDebug` | Compilation fails |
| `test` | Any unit test fails |
| `:test:konsist:test` | An architecture rule is violated |
| `lint` | Android Lint reports an error (`warningsAsErrors = true`) |

Test and lint reports are uploaded as artifacts, including on failure.

**`Build` is the required check.** A pull request cannot merge until it passes.

`TMDB_API_KEY` comes from repository secrets. A missing key produces an empty `BuildConfig` field
rather than a build failure, so CI still passes on forks without the secret.

### Claude PR Review

`.github/workflows/claude-review.yml` posts an automated review on each pull request. It stands in
for the second pair of eyes that a solo repository does not otherwise have; treat its findings as
suggestions.

It is **advisory and can never block a merge**, in two independent ways: it is not in the required
status checks, and its step is marked `continue-on-error` so an outage does not put a red cross on
a pull request whose build is green. The trade-off is that the check reports green even when the
review did not run — if you expected a review and no comment appeared, look at the workflow run.

It authenticates with `CLAUDE_CODE_OAUTH_TOKEN`, generated with `claude setup-token` and tied to a
Claude subscription, rather than metered API credits. If that secret is absent the step fails
quietly and the pull request is unaffected.

---

## Releases

`master` is tagged when it reaches a state worth returning to:

```bash
git checkout master && git pull
git tag -a v1.1.0 -m "Short description"
git push origin v1.1.0
```

Roughly [semver](https://semver.org): patch for fixes, minor for features or toolchain upgrades,
major for breaking changes to the template's structure.

---

## Architecture rules are enforced, not suggested

Before adding code, read [CLAUDE.md](CLAUDE.md) — it is the conventions reference (layering, MVI,
naming, DI, testing) and applies to human contributors as much as to AI assistants.

The `:test:konsist` module turns those conventions into tests. A pull request that puts a class in
the wrong layer, names it inconsistently, or gives a `RepositoryImpl` no corresponding test will
fail CI. If a rule is wrong, change the rule in the same PR and say why — do not work around it.

See also [ARCHITECTURE.md](docs/ARCHITECTURE.md) and [MODULARIZATION.md](docs/MODULARIZATION.md).

---

## Dependencies

All versions live in [`gradle/libs.versions.toml`](gradle/libs.versions.toml). Never hardcode a
version in a `build.gradle.kts`.

Some are held back deliberately. Before upgrading anything that looks merely out of date, check the
"Versions held back on purpose" section of the [README](README.md) and the `Pinned on purpose`
notes in [CLAUDE.md](CLAUDE.md). Those entries exist because the obvious upgrade breaks something
non-obvious.

---

## Feature planning

Larger features are specced and planned before implementation, under
`docs/features/{feature-name}/`:

- `SPEC.md` — user stories and acceptance criteria
- `PLAN.md` — implementation plan, broken into buildable phases

[`docs/features/watched-movies/`](docs/features/watched-movies/) is a worked example. The
`.claude/skills/` directory automates this flow, but the structure is useful regardless of whether
it is driven by an assistant.
