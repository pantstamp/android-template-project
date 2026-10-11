Feature name: movie-search

This is a replay of a historical branch. The branch under review is checked out, detached, at
`{{WORKTREE}}`. Your working directory is a different checkout of the same repository; use it
only for `CLAUDE.md`. Adjust your instructions as follows:

- Run every git command in the branch checkout: `git -C {{WORKTREE}} …`.
- Read the PLAN, SPEC and source files from `{{WORKTREE}}`, not from your working directory.
- Wherever your instructions say `master`, use `{{BASE}}`. It is a fixed commit: skip
  `git fetch`, and judge "behind master" against `{{BASE}}`.
- Use `CLAUDE.md` from your working directory, not `{{WORKTREE}}/CLAUDE.md`.
- Do not read `docs/features/movie-search/RETRO.md` in either checkout, or any commit that is
  not an ancestor of the branch checkout's `HEAD`.
- Run the Part 2 pre-flight in the branch checkout: `cd {{WORKTREE}} && ./gradlew …`.
