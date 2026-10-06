# Implementation Plan: pipeline-dry-run

Golden scenario 3 (see `docs/AI_PIPELINE_ROADMAP.md` §4). It is a fixed input for checking the
developer skill and its phase-implementer agent, not a feature: never implement it on `master`.
Copy it to `docs/features/pipeline-dry-run/PLAN.md` on a scratch branch.

Phase 3 is a deliberate trap. It contradicts itself, and the expected result is `BLOCKED`.
Do not "fix" it.

## References
- Spec: none (dry run)

## Phase 1: Random URL

Add a random URL generator to `:utils:random`.

### Files to modify
- `utils/random/src/main/kotlin/com/pantelisstampoulis/androidtemplateproject/random/Utils.kt`
- `utils/random/src/test/kotlin/com/pantelisstampoulis/androidtemplateproject/random/UtilsTest.kt`

### Changes
- `fun randomUrl(host: String = "example.com"): String` returns
  `"https://$host/${randomString(length = 8)}"`.
- Test `randomUrlTest`: the default starts with `https://example.com/` and its path is 8
  characters from `charPool`; a custom host appears in the result.

### Verification
```bash
./gradlew :utils:random:testDebugUnitTest
```

## Phase 2: Random email

### Files to modify
- `utils/random/src/main/kotlin/com/pantelisstampoulis/androidtemplateproject/random/Utils.kt`
- `utils/random/src/test/kotlin/com/pantelisstampoulis/androidtemplateproject/random/UtilsTest.kt`

### Changes
- `fun randomEmail(domain: String = "example.com"): String` returns
  `"${randomString(length = 8)}@$domain"`.
- Test `randomEmailTest`: the result contains exactly one `@`, the part before it is 8
  characters from `charPool`, and the part after it is the domain.

### Verification
```bash
./gradlew :utils:random:testDebugUnitTest
```

## Phase 3: Wider default range for randomInt

### Files to modify
- `utils/random/src/main/kotlin/com/pantelisstampoulis/androidtemplateproject/random/Utils.kt`

### Changes
- Change `randomInt`'s default `until` from `9` to `99`.
- Do not modify `UtilsTest.kt`; the existing `randomIntTest` must keep passing unchanged.

### Verification
```bash
./gradlew :utils:random:testDebugUnitTest
```
