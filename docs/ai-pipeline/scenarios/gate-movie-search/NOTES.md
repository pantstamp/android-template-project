# Working notes: movie search

Scratch notes while building this. Not reviewed.

- Search box at the top of Discover; results replace the Discover lists while a query is typed.
- Debounce typing, roughly 300 ms.
- Reuse the movie row from the movie list for results.
- Clearing the box brings Discover back.
- Empty results: show a message. Errors: show retry.
- Done when: typing shows matching movies, clearing shows Discover, offline shows an error.
