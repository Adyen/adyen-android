---
name: android-public-api-change
description: Change the public API surface: visibility, breaking changes, and apiDump.
---

# android-public-api-change

Keep the public API surface of the Adyen Android SDK small, stable, and intentional.

## Usage

Invoke this skill as soon as work looks like it will touch the public API: adding a class, function, or property that is not `internal`, widening visibility, changing or removing a public signature, or when `apiCheck` fails during verification.

Every published module has `.api` dump files, so any change to the public surface shows up as a diff. An unexpected diff is the signal to come back here.

## Steps

### 1. Default to internal

Classes, functions, and properties are `internal` unless they are deliberately part of the public API.

```kotlin
// Default — internal visibility
internal class CardViewStateFactory

// Used across modules, not by merchants — restrict to the library group
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
class SomeSharedClass

// Public API — a deliberate decision
class CardConfiguration
```

If a type is only needed across our own modules, use `@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)` rather than making it public.

### 2. Prove the change is necessary

Making something public later is easy; making it internal again is a breaking change. So the bar is asymmetric.

- Look for an existing implementation of the same idea in the codebase first. Whatever you are working on most likely has a close precedent — follow it.
- Try to find a solution that needs no public API change at all.
- Even a non-breaking addition makes the API more complex. Additions are not free.
- If you are unsure whether something should be public, make it `internal` or annotate it with `@RestrictTo`.

Review each individual change in the `.api` diff and be able to justify it. If in doubt, ask.

### 3. Check for breaking changes

Breaking changes can come from removed **or** modified code — a changed parameter, return type, default value, or supertype all count.

Only the merchant-facing surface can break. `internal` and `@RestrictTo` declarations are excluded from the `.api` dumps (`nonPublicMarkers` in `config/gradle/apiValidator.gradle`), so changing or removing them is not a breaking change — which is exactly why step 1 defaults to them.

- **Do not proceed with a breaking change until it has been discussed and confirmed with the developer.**
- Breaking changes belong in a major release.
- Raise the question during planning: ask whether breaking changes are acceptable for the current work, rather than discovering it mid-implementation.

### 4. Choose a shape that can evolve

**Prefer abstract classes over sealed classes** for new types that merchants might use in a `when` expression. Adding an entry to a sealed class breaks exhaustive `when` blocks in merchant code.

```kotlin
// AVOID — adding an entry breaks merchant code
sealed class DropInResult {
    class CancelledByUser : DropInResult()
    class Error(val reason: String?) : DropInResult()
}

// PREFER — abstract class with an internal constructor.
// Merchants must write an `else` branch, so new entries are safe.
abstract class DropInResult internal constructor() {
    class CancelledByUser : DropInResult()
    class Error(val reason: String?) : DropInResult()
}
```

Sealed classes are fine when merchants pass them as parameters instead of matching on them — `AddressConfiguration` is an example. In that case, **add new entries as `class`, never `object`**, so optional arguments can be added later without breaking anything:

```kotlin
sealed class AddressConfiguration : Parcelable {
    object None : AddressConfiguration()
    data class PostalCode() : AddressConfiguration()
    class Lookup : AddressConfiguration()
}

// Merchant usage — passed as a parameter, so new entries do not affect it
setAddressConfiguration(AddressConfiguration.None)
```

Converting an existing sealed class to an abstract class is itself a breaking change. Use abstract classes for new code only.

### 5. Update the API dump

Once the API change is confirmed as intentional, regenerate the dump files and include them in the commit:

```bash
./gradlew apiDump              # whole project
./gradlew :<module>:apiDump    # single module
```

Do **not** run `apiDump` reflexively. Running it only after confirming intent is what allows `apiCheck` to catch accidental API changes during verification.

## Important

- An `apiCheck` failure is not a task to silence with `apiDump` — first work out whether the API change was intended.
- A public API change means the branch and PR are merchant-observable: use a `feature/` or `fix/` prefix, not `chore/`.
