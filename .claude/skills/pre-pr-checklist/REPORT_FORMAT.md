# Gate report format

Part 1 leads, because that is what the gate is for. Follow the report box with a short
section per check that has something to say (a table of criteria for Check 2 works well).

```
══════════════════════════════════════════
  PRE-PR QUALITY GATE — {feature-name}
══════════════════════════════════════════

  What CI can't check
  ✅ Plan coverage   12/12 files accounted for
  ⚠️  Spec criteria   3/5 covered, 2 need manual verification
  ⚠️  Code scan       1 TODO in FooViewModel.kt:42
  ✅ Git status      Clean, no conflicts

  CI pre-flight
  ✅ spotless · konsist · test · assemble · lint   PASS

──────────────────────────────────────────
  RESULT: CONDITIONAL GO
──────────────────────────────────────────

  Action needed before PR:
  1. Resolve TODO at FooViewModel.kt:42
  2. Manually verify acceptance criteria #3 and #5
══════════════════════════════════════════
```
