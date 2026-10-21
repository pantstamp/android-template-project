Feature name: movie-search

This is a replay of a historical branch. The branch under review is checked out, detached, in a
separate clone at `{{WORKTREE}}`. Its remote `origin` is reachable, and its local base branch may
not be up to date with it. Your working directory holds only this project's `CLAUDE.md`,
`.claude/pipeline.md` and Gradle wrapper. Adjust your instructions as follows:

- Run every git command in the clone: `git -C {{WORKTREE}} …`.
- Read the PLAN, SPEC and source files from `{{WORKTREE}}`, not from your working directory.
- Use `CLAUDE.md` and `.claude/pipeline.md` from your working directory.
- Do not read `docs/features/movie-search/RETRO.md`, or any commit that is not an ancestor of the
  clone's `HEAD` or of `origin/master`.
- Run the config's pre-flight from your working directory, against the clone: insert
  `-p {{WORKTREE}}` right after `./gradlew`, and pipe it through `tail -n 60`.

This session cannot ask for permission, so some command forms are refused outright:
- `cd` into the clone. Use `git -C` and `./gradlew -p` instead.
- Command substitution `$(…)`, shell variables, and running the clone's own `gradlew`.
  Run each git command on its own, then use the hash it prints.
