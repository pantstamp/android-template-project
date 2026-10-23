/ai-pipeline:developer pipeline-dry-run

This is a dry run of the pipeline on a scratch branch. You are on `{{BRANCH}}`, which holds the
plan at `docs/features/pipeline-dry-run/PLAN.md`; it is this feature's branch. Stay on it: do
not switch, create, push or fetch branches, and do not open a pull request.

When you need an answer from me, end your message with the question and wait.

This session cannot ask for permission, so some command forms are refused outright, for you
and for any agent you start: `cd`, command substitution `$(…)`, and shell variables such as
`$?` or `${PIPESTATUS[0]}`. Run each command plain, from the working directory; Gradle's own
output says whether it passed.
