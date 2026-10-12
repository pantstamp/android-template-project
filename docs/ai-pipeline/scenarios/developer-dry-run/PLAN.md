# Implementation Plan: pipeline-dry-run

Small helpers for `:utils:random`, in three phases.

## References
- Spec: none

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
