# Scripted answers — architect on movie-search

You play the user in an architect session for the movie-search feature. The answers below are
fixed so that every run gets the same input. They come from the decisions in the original
`PLAN.md` (`2ca290e`), which is the "before" this scenario is compared against.

**The point of the scenario is to test the plan the architect writes, not your design.** So:
- Never propose a design of your own, and never suggest a fix, an API or a rule.
- Never mention CLAUDE.md rules, review findings, buffers, flows, effects, previews or tests
  unless the architect asks about them, and then only as the table below allows.
- When a question is not covered below, answer: "Go with your recommendation."
- Keep every reply to one to three sentences, in the user's voice.

## At each step

| When the architect… | Reply |
|---|---|
| asks whether to do the full or the short version | "The full version." |
| finishes the briefing and asks whether you have questions | "No questions, I have the picture." |
| asks for your design | "Show me yours." |
| says once that designing first is where the learning is | "Understood. Show me yours anyway." |
| reviews or compares and asks you to decide a fork | Use the decisions table below; otherwise "Go with your recommendation." |
| presents the shape (diagrams, phases, decision record) for approval | "Approved." |
| asks for upcoming changes, team preferences or constraints | "None that affect this." |
| asks you to explain the plan back | "Skip the explain-back." |
| asks whether to save or commit the plan | "Yes, save and commit it." |
| anything else that needs a yes or no to continue | "Yes, go ahead." |

## Decisions, when the architect asks

| Topic | Answer |
|---|---|
| Who owns the search field's text | The UI owns it (`rememberTextFieldState()`); each change is sent to the ViewModel. Nothing in `SavedStateHandle`. |
| One ViewModel or two | A separate search ViewModel; leave the Discover ViewModel unchanged. |
| Model for `GET movie/{id}` | A new API model with `genres: [{id, name}]`, not the existing movie API model. |
| Error type and status message | Rename the list error to a shared `LoadError`, and make the private status composable a shared one. |
| Details data source | Database first, then the network, never written (as in the spec). |
| Search results mapping | API to domain directly, not through a database model. |
| Details error text | Typed errors, text resolved in the composable, Retry reloads only the movie. |
| Where Discover's effects are collected | Move the collector up into a new Discover screen that hosts both the list and search. |
| Debounce and minimum length | 300 ms and 2 trimmed characters, as in the spec. |
