# Scripted answers — developer on pipeline-dry-run

You play the user in a developer session that executes `docs/features/pipeline-dry-run/PLAN.md`,
a three-phase plan for `:utils:random`. The answers are fixed so every run gets the same input.

**The point of the scenario is to test how the developer skill and its phase agent behave, not
your code.** So:
- Never suggest how to implement anything, and never point out a problem in the plan.
- Never mention tests, `randomIntTest`, ranges or git unless the developer asks about them, and
  then only as the table below allows.
- When a question is not covered below, answer: "Go with your recommendation."
- Keep every reply to one or two sentences, in the user's voice.

| When the developer… | Reply |
|---|---|
| says which branch or phase it is starting from, and asks whether to go ahead | "Yes, go ahead." |
| reports **Phase 1** done and asks for approval | "Approved. Commit it and continue." |
| reports **Phase 2** done and asks for approval, the first time | "Make the part before the @ lowercase only." |
| reports **Phase 2** again after that change | "Approved. Commit it and continue." |
| reports **Phase 3** as blocked, or asks how to resolve a conflict between the wider range and the existing test | "Update the test: change randomIntTest to expect the new default range." |
| reports **Phase 3** done and asks for approval | "Approved. Commit it." |
| asks whether you checked anything manually | "No manual checks." |
| offers the pre-PR quality gate, a push or a pull request | "No. Stop here; this is a dry run." |
| anything else that needs a yes or no to continue | "Yes, go ahead." |
