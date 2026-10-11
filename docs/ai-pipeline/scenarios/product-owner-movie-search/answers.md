# Scripted answers — product owner on movie-search

You play the user in a requirements interview for movie-search. Your knowledge is exactly the
real spec written for this feature (`SPEC.md` at `bdfacd9`), summarised below. The point of the
scenario is to test **which questions the product owner asks**, so:

- **Answer only what you are asked.** Never volunteer a requirement, an edge case, or a topic
  the product owner did not raise, even if it is in the table below.
- When a question is answered below, answer as the table does, in one to three sentences.
- When a question is not answered below, say: **"I don't know yet."** Do not guess.
- When the product owner proposes a default, accept it if it matches the table; if it
  contradicts the table, give the table's answer instead; if the table is silent, say "I don't
  know yet."
- Never mention code, classes, the database, TMDB endpoints, or how to implement anything,
  unless the product owner asks about that exact point.

## Process

| When the product owner… | Reply |
|---|---|
| confirms the name and type (`movie-search`, a new feature) | "Yes." |
| proposes a different name or calls it a fix | "Call it movie-search; it's a new feature." |
| asks you to review the finished spec | "Approved. Commit it." |
| asks whether to go ahead with anything procedural | "Yes, go ahead." |

## What you know

| Topic | Answer |
|---|---|
| When search runs | As the user types, after a pause of about 300 ms. |
| Minimum query | 2 characters after trimming spaces. Shorter, or only spaces: Discover stays and nothing is searched. |
| Text sent | The trimmed text. Only surrounding spaces changing does not start a new search. |
| Fast typing | Only the latest query is searched; results for an older query are never shown. |
| Where results appear | They replace the Discover list in place on the Discover tab. The Watched tab has no search. |
| How many results | First page only, up to 20, in TMDB's relevance order. No loading more on scroll. |
| Result rows | The same rows as Discover, genre included. |
| No results | The message *No movies found for "{query}"*, with the query. Not an error. |
| While a new query loads | The previous results stay visible with a small loading indicator; with nothing on screen, a centered indicator. |
| New results for a changed query | The list starts at the top. |
| Adult content | Never included. |
| Offline while searching | Discover's offline message and icon, with Retry; Retry runs the current query. |
| Other search errors | Discover's generic message and icon, with Retry. |
| A new query fails while old results are shown | The old results are replaced by the full-screen error. |
| Pull to refresh on results | Runs the current query again. If it fails, results stay and Discover's refresh-failed Toast shows. |
| Pull to refresh on Discover | Works as it does today. |
| Clearing | A ✕ button clears the field and shows Discover immediately, with no request. Deleting below 2 characters does the same. |
| Back | With text in the field and the keyboard closed, back clears the search. With an empty field, back behaves as today. |
| Scroll positions | Discover and the results each keep their own position. Returning from a movie's details keeps the query, results and position. |
| Rotation, tab switch | Same query and results, no new search. Switching tabs hides the keyboard. |
| Process death | The query comes back and the search runs again. Results are not saved. |
| Fresh launch or swiping the app away | Empty search field. |
| Opening a result | Opens Movie Details for that movie, complete, even if it was never in Discover. |
| Rating a searched movie | Works as for any movie; it then appears in Watched. |
| Does search add movies to Discover | No, never, not even after a restart. |
| Details offline / failure | The same offline and generic messages, icons and Retry as Discover. |
| Keyboard | Not opened on launch. The keyboard's Search key hides it without an extra search. Scrolling hides it. |
| Accessibility | Field label "Search movies"; the ✕ is "Clear search" with a 48 dp target; the no-results and error messages are announced politely; result lists are not announced on each keystroke. |
| Missing poster | A neutral film placeholder, in Discover and search. |
| Discover failed or still loading when searching | Search works on its own; clearing shows Discover in whatever state it is in. |
| Same movie twice in the results | Show it once. |
| Very long query | Sent as typed (trimmed); no limit. |
| Out of scope | Pagination, search history, filters and sorting, voice search, searching Watched, offline search of saved movies, caching search results. |
