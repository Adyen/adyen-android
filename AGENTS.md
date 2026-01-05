# Agent Guidelines for Adyen Android SDK

This document outlines important patterns, practices, and rules to follow when working with the Adyen Android SDK codebase.

## Core Principles

### 1. Never Make Assumptions
**ALWAYS ask questions if you are uncertain about:**
- Code patterns or conventions
- Expected behavior
- Implementation details
- Migration strategies
- Whether a change is needed or correct

**Do not proceed with uncertain changes.** It's better to ask and get clarification than to implement incorrect solutions.

### 2. Always Work with a Plan

**CRITICAL: Create plan FIRST, implement AFTER approval:**
- **NEVER start implementation without a plan**
- First, create a comprehensive implementation plan document (e.g., `*_IMPLEMENTATION_PLAN.md`)
- Present the plan to the user for review and approval
- Only after the plan is approved, begin implementation
- Do not execute any code changes during the planning phase

**Before starting any task:**
- Ensure there is an implementation plan document (e.g., `*_IMPLEMENTATION_PLAN.md`)
- If no plan exists, guide the user to create one before proceeding with implementation
- Break down the work into clear, manageable phases
- Identify dependencies, risks, and testing strategy upfront

**When working on multi-phase tasks with a plan document:**
- Update the plan file as you complete tasks/phases
- Mark checkboxes as completed
- Add notes about any deviations or discoveries
- This helps if work needs to be continued later or by someone else
- **Do not commit plan files to the repository** as they are temporary working documents. They should only be committed manually if specifically needed.

**Commit workflow - CRITICAL:**
- **Complete one phase fully before moving to the next**
- After completing a phase:
  1. Run `./gradlew apiDump`
  2. Run `./gradlew check` (or module-specific check)
  3. Commit the phase with a descriptive message
  4. Only then proceed to the next phase
- Never accumulate multiple phases in a single commit
- Each commit should represent a logical, complete unit of work
- This ensures work can be reviewed incrementally and rolled back if needed

### 3. Test-Driven Development

**Write tests first, then make them green:**
- Before implementing new functionality, write the tests that define the expected behavior
- Follow existing test patterns in the codebase to understand our testing structure
- Search for similar tests to understand naming conventions and structure

**Test structure example:**
```kotlin
@Test
fun `when holder name is empty then validation fails`() {
    // GIVEN
    val input = ""
    
    // WHEN
    val result = validator.validate(input)
    
    // THEN
    assertFalse(result.isValid)
}
```

**Ensure test coverage before refactoring:**
- Always ensure there is adequate test coverage before making any internal refactors
- If tests don't exist, write them first to capture current behavior
- This ensures refactoring doesn't introduce regressions

**Pause and verify between big steps:**
- After completing significant changes, pause to run tests
- Ensure all tests that cover your code changes pass before proceeding
- Don't accumulate too many changes without verification

### 4. Always Run Checks Before Committing
After completing a phase of work and before each commit, **ALWAYS run these commands in order**:

```bash
./gradlew apiDump
./gradlew check
```

**Why these commands:**
- `apiDump` - Updates the public API files to capture any API changes
- `check` - Ensures all code compiles correctly, all tests pass, and no lint errors are introduced

**IMPORTANT: Include generated `.api` files in commits**
- After running `apiDump`, the command generates `.api` files in module `api/` directories (e.g., `core/api/core.api`)
- These files MUST be included in your commit along with the code changes
- They document the public API surface and are used for API compatibility checks

This ensures:
- All public API changes are documented
- API files are kept in sync with code changes
- All code compiles correctly
- All tests pass
- No lint errors are introduced
- The codebase remains in a healthy state

## Implementation Guidelines

### Public API Changes

**Default to internal visibility for all new code:**
- Classes, functions, properties should be `internal` unless they need to be public
- If a class/function is used across modules, use `@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)` annotation
- Only make things `public` if they are part of the public API
- This helps maintain a clean public API surface and prevents accidental exposure

**Example:**
```kotlin
// Default - internal visibility
internal class CardViewStateFactory { }

// Used across modules - restrict to library group
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
class SomeSharedClass { }

// Public API
class CardConfiguration { }
```

**When making public API changes:**

**Carefully review all changes:**
- Review each change carefully to ensure it's necessary and correct. If in doubt, ask questions.

**Never make breaking changes without discussion:**
- Breaking changes can happen through removed OR modified code
- **Do not proceed with breaking changes until discussed and confirmed with the user**
- Breaking changes should only be done in a major release
- Always ask during planning phase whether breaking changes are acceptable for the current work

**Ensure changes are necessary:**
- Even non-breaking changes can make the API more complex
- Try to find a solution without any public API changes
- If unsure whether something should be public or not, make it internal or annotate with `@RestrictTo`
- We can always make things public later, but making things internal causes a breaking change

**Maintain consistency:**
- Look for similar examples or use cases in the existing codebase. Whatever you work on will most probably have a similar implementation in the codebase.
- Follow established patterns and conventions
- Ask questions if uncertain about the correct approach

**Sealed classes vs abstract classes:**

When creating new classes that merchants might use with `when` expressions, prefer abstract classes over sealed classes to avoid breaking changes:

```kotlin
// ❌ AVOID - Adding new entries will break merchant code
sealed class DropInResult {
    class CancelledByUser : DropInResult()
    class Error(val reason: String?) : DropInResult()
    class Finished(val result: String) : DropInResult()
}

// Merchant code - breaks when we add new entries
val message = when (dropInResult) {
    is DropInResult.CancelledByUser -> "CancelledByUser"
    is DropInResult.Error -> "Error"
    is DropInResult.Finished -> "Finished"
    // No else clause - breaks if we add a new entry
}

// ✅ PREFER - Abstract classes with internal constructor
abstract class DropInResult internal constructor() {
    class CancelledByUser : DropInResult()
    class Error(val reason: String?) : DropInResult()
    class Finished(val result: String) : DropInResult()
}

// Merchant code - doesn't break when we add new entries
val message = when (dropInResult) {
    is DropInResult.CancelledByUser -> "CancelledByUser"
    is DropInResult.Error -> "Error"
    is DropInResult.Finished -> "Finished"
    else -> "else"
}
```

**For sealed classes that are safe to use:**
- If adding a new entry with no arguments to an existing sealed class, always make it a `class` and not an `object`
- This allows adding optional arguments in the future without a breaking change
- Example: `AddressConfiguration` is safe because merchants use it as a parameter, not in `when` expressions

```kotlin
// This is okay - merchants don't use when with it
sealed class AddressConfiguration : Parcelable {
    object None : AddressConfiguration()
    data class PostalCode() : AddressConfiguration()
    data class FullAddress() : AddressConfiguration()
    class Lookup : AddressConfiguration()
}

// Merchant usage - adding new entries doesn't affect this
setAddressConfiguration(AddressConfiguration.None)
```

**Note:** Changing an existing sealed class to abstract creates a breaking change. Use abstract classes for new code only.

### Layout and Styles (For XML)

**When making changes to layout or styles XML files:**

**Add styles to newly added views:**
- Always define styles for new views to allow merchant customization
- Follow existing style naming conventions

**Use correct colors and attributes:**
- Follow default Material colors to ensure compatibility
- Use attributes that exist in themes by default
- **Avoid:** `?android:attr/textColor` (may not be defined in merchant themes, causes crashes)
- **Prefer:** `?attr/colorOnSurface` (Material default for text colors, always available)
- Search for similar views in the codebase to find the correct attributes

**Maintain style hierarchy:**
- If introducing a style like `AdyenCheckout.Image.Logo.Large`, also declare parent styles:
  - `AdyenCheckout.Image.Logo`
  - `AdyenCheckout.Image`
- Parent styles can be empty but must exist
- Without proper hierarchy, merchants who override styles may encounter errors

**Example:**
```xml
<!-- All three must exist, even if parents are empty -->
<style name="AdyenCheckout.Image" />
<style name="AdyenCheckout.Image.Logo" parent="AdyenCheckout.Image" />
<style name="AdyenCheckout.Image.Logo.Large" parent="AdyenCheckout.Image.Logo">
    <item name="android:layout_width">48dp</item>
    <item name="android:layout_height">48dp</item>
</style>
```

### String Resources

**When making changes to strings:**

**Apply localized context in components:**
- Components must be displayed in the shopper locale, not device locale
- This includes texts, hints, errors, and all displayed string resources
- Search for `localizedContext` in the codebase for examples

**Add strings to styles, not views:**
- Define strings in styles to allow merchant customization
- In view XML files, use `tools:text` instead of `android:text`
- For programmatic strings, search for `localizedContext.getString` for examples

**Example:**
```xml
<!-- In layout XML -->
<TextView
    style="@style/AdyenCheckout.TextView.Title"
    tools:text="@string/checkout_card_holder_name_label" />
```

```kotlin
// In code - apply localized context
val localizedString = localizedContext.getString(R.string.checkout_card_holder_name_label)
```

### Adding New Modules

**When adding a new module:**

**Review Gradle configuration:**
- Determine if the module should be published (e.g., new payment method) or stay internal (e.g., test module)
- Compare new Gradle files with similar existing modules
- Ensure all necessary configuration is included

**For modules with external SDKs:**

Examples of modules with external SDKs: Twint, WeChat Pay, 3DS2, Cash App Pay, Google Pay

**If the module should be excluded by default in standalone components:**
- Add as `compileOnly` in Gradle files. You can always ask if this is necessary or not while planning.
- Add `dontwarn` rules to ProGuard to avoid R8 issues

**Example Gradle configuration:**
```gradle
// In dependent modules
compileOnly project(':external-sdk-module')
```

**Example ProGuard rule:**
```proguard
-dontwarn com.external.sdk.**
```

**Use safe access functions:**
- Surround code from external SDK modules with safe access functions
- Use `runCompileOnly` and `checkCompileOnly` to handle missing dependencies gracefully
- Search for existing examples in modules like Twint, WeChat Pay, or 3DS2

**Example:**
```kotlin
// Check if external SDK is available
if (checkCompileOnly("com.external.sdk.SomeClass")) {
    runCompileOnly {
        // Code using external SDK
        ExternalSDK.initialize()
    }
}
```

## Commit Guidelines

### Commit Message Format
All commits must follow this format:
```
Short commit message describing the change.

COSDK-XXX
```

**Important:**
- **ALWAYS ask the user for the ticket number** before making a commit
- For each task/plan, the ticket number should be the same. Remember the number once provided for the rest of the commits.
- Replace `XXX` with the actual ticket number (e.g., `COSDK-1234`)
- **Verify the commit message with the user** before executing the commit
- The first line should be a concise description of the change
- Leave a blank line between the description and the ticket reference

### Example Commit Messages
```
Add holder name localization keys and strings.

COSDK-1234
```

## Verification Checklist

Before considering work complete:
1. Implementation plan exists and is updated with progress
2. Tests written first and are passing (`./gradlew test`)
3. All checks pass (`./gradlew check`)
4. Code follows existing patterns in the codebase
5. If public API changed: Reviewed for breaking changes (discussed with user) and necessity
6. If layout/styles changed: Styles added, proper attributes used, hierarchy maintained
7. If strings changed: All translations present, localized context applied
8. If new module added: Gradle configuration reviewed, external SDKs handled properly
9. If refactoring: Test coverage exists for affected code
10. Added classes and functions have unit tests following given-when-then structure

## When in Doubt

1. **Search for similar patterns** in the existing codebase
2. **Ask questions** rather than making assumptions
3. **Run `./gradlew check`** to catch issues early
4. **Read compiler error messages carefully** - they often tell you exactly what's wrong
5. **Ask for the ticket number** before making any commits

## Resources
[Public Documentation](https://docs.adyen.com/online-payments/build-your-integration/?platform=Android)

---

**Remember:** The goal is to maintain consistency with the existing codebase and ensure all changes are correct and tested. Taking time to verify and ask questions prevents technical debt and bugs.
