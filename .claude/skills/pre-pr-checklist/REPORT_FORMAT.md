# Gate report format

Part 1 leads, because that is what the gate is for. Follow the report box with a short
section per check that has something to say (a table of criteria for Check 2 works well).
On a partial re-run, mark the checks that were not run as `⏭️ skipped`.

For Check 5, give each finding its own entry:

```
FooViewModel.kt:57 — Flow and coroutines: "A state flag set before `launch` must be reset on
every exit path"
  Fails when: the save returns an error → `isSaving` stays true → the button never re-enables
  Fix: reset `isSaving` in `onError` as well as `onSuccess`
```

```
══════════════════════════════════════════
  PRE-PR QUALITY GATE — {feature-name}
══════════════════════════════════════════

  What CI can't check
  ✅ Plan coverage   12/12 files accounted for
  ⚠️  Spec criteria   3/5 covered, 2 need manual verification
  ⚠️  Code scan       1 TODO in FooViewModel.kt:42
  ✅ Git status      Clean, no conflicts
  ⚠️  Rule review     1 finding (Flow and coroutines)

  CI pre-flight
  ✅ spotless · konsist · test · assemble · lint   PASS

──────────────────────────────────────────
  RESULT: CONDITIONAL GO
──────────────────────────────────────────

  Action needed before PR:
  1. Resolve TODO at FooViewModel.kt:42
  2. Manually verify acceptance criteria #3 and #5
  3. Decide on rule review finding at FooViewModel.kt:57
══════════════════════════════════════════
```
