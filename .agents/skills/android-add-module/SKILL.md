---
name: android-add-module
description: Add a new Gradle module and wire in optional external SDKs.
---

# android-add-module

Add a new Gradle module to the Adyen Android SDK with the right publishing and dependency setup.

## Usage

Invoke this skill when creating a new module (for example a new payment method or an internal helper module), or when an existing module needs to depend on an optional external SDK.

## Steps

### 1. Decide whether the module is published

- **Published** — anything merchants integrate, such as a new payment method. It uses the `checkout.android.library` convention plugin plus a `checkoutPublishing { id, name, description }` block, and gets an `api/<module>.api` dump file. Compare with `card/build.gradle`.
- **Not published** — internal tooling and test support. These skip both: `test-core` uses the plain `android.library` plugin, and `lint` is a Kotlin JVM module.

Copy the Gradle setup from the existing module closest to what you are adding, then diff the two files and account for every difference. Add the module to `settings.gradle`, keeping the list alphabetical.

### 2. Register a published module elsewhere

A published module is not done when its own `build.gradle` is right — two lists outside it have to know about it:

- **Documentation.** Add `dokka(project(':<module>'))` to the `dependencies` block at the bottom of the root `build.gradle`. Every published module has an entry there, so a missing one means the module is silently absent from the generated API documentation.
- **Drop-in.** If merchants reach the payment method through Drop-in, add `api project(':<module>')` to `drop-in/build.gradle` so it ships with Drop-in. Shared infrastructure such as `core`, `ui-core` and `components-core`, and action modules such as `redirect` and `await`, arrive transitively and are not listed.

### 3. Handle optional external SDKs with compileOnly

Some modules wrap a third-party SDK: 3DS2, Twint, WeChat Pay, Cash App Pay, Google Pay, card scanning. Merchants who use standalone components should not be forced to pull these in, so the dependent module declares them as `compileOnly` and tolerates their absence at runtime.

Ask during planning whether the new module needs this treatment — it is not automatic.

```gradle
// In the dependent module, e.g. action-core/build.gradle
compileOnly project(':3ds2')
```

Because the classes are absent at runtime, R8 warns about the missing references. Suppress that in the dependent module's `consumer-rules.pro` so merchant builds stay clean:

```proguard
-dontwarn com.adyen.checkout.adyen3ds2.**
```

### 4. Guard every call into an optional SDK

Never touch a `compileOnly` type directly. Wrap the call so a missing class degrades gracefully instead of throwing `NoClassDefFoundError`:

```kotlin
import com.adyen.checkout.core.common.helper.runCompileOnly

val sdkVersion = runCompileOnly { ThreeDS2Service.INSTANCE.sdkVersion }.getOrNull()
```

The optional type must not appear outside the `runCompileOnly` block — not in a signature, a field, or a local variable — or the JVM resolves it before the guard runs. See `card/src/main/java/com/adyen/checkout/card/internal/util/CardScannerWrapper.kt` for the pattern and the reason.

For a boolean availability check, use `checkCompileOnly`.

### 5. Verify

Run the `android-check` skill (`.agents/skills/android-check/SKILL.md`) for the new module and for every module that depends on it.

For an optional SDK, also verify the fallback path: the dependent module must compile and behave sensibly when the optional module is absent.

## Important

- A new published module is a public API addition — apply the `android-public-api-change` skill (`.agents/skills/android-public-api-change/SKILL.md`) to whatever it exposes.
- A new external library, as opposed to a new project module, goes through the `android-add-dependency` skill (`.agents/skills/android-add-dependency/SKILL.md`) first.
