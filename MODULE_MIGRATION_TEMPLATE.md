# Module Migration Template

This template outlines the implementation plan for migrating a payment method module to the new component architecture, following the patterns established in the MBWay module.

> **Prerequisites**: This plan assumes scaffold files already exist in the module (empty class files with TODO comments).

---

## Phase 1: Gradle Setup
**Goal**: Add required dependencies for the new implementation

**Files to modify**:
- [ ] `{module}/build.gradle`
  - Add `libs.plugins.kotlin.compose` plugin
  - Add `libs.plugins.kotlin.serialization` plugin
  - Add `api project(':core')` dependency
  - Add `libs.androidx.startup` dependency

**Reference**: `mbway/build.gradle`

**Verification**:
```bash
./gradlew :{module}:compileDebugKotlin
./gradlew :{module}:check
./gradlew apiDump
```

**Commit**: "Add required Gradle dependencies for {Module}. COSDK-XXX"

---

## Phase 2: Create PaymentMethod in Core Module
**Goal**: Create the new PaymentMethod class in the core module

**Files to create**:
- [ ] `core/src/main/java/com/adyen/checkout/core/components/paymentmethod/{Module}PaymentMethod.kt`
  - Data class with `val` properties (not `var`)
  - Properties: `type`, `checkoutAttemptId`, plus payment-method-specific fields
  - Include `SERIALIZER` companion object
  - `PAYMENT_METHOD_TYPE` constant

**Files to modify**:
- [ ] `core/src/main/java/com/adyen/checkout/core/components/paymentmethod/PaymentMethodDetails.kt`
  - Add {Module}PaymentMethod to `getChildSerializer` when block

**Reference**: 
- `core/.../MBWayPaymentMethod.kt` (new pattern)
- `components-core/.../{Module}PaymentMethod.kt` (old implementation for field reference)

**Verification**:
```bash
./gradlew :core:compileDebugKotlin
./gradlew :core:check
./gradlew apiDump
```

**Commit**: "Add {Module}PaymentMethod to core module. COSDK-XXX"

---

## Phase 3: Create Configuration
**Goal**: Create the new configuration class following the new pattern

**Files to create**:
- [ ] `{module}/src/main/java/com/adyen/checkout/{module}/{Module}Configuration.kt`
  - `{Module}Configuration` data class implementing `Configuration`
  - `{Module}ConfigurationBuilder` class (internal constructor)
  - `CheckoutConfiguration.{module}()` extension function
  - `CheckoutConfiguration.get{Module}Configuration()` internal function

**Reference**: `card/src/main/java/com/adyen/checkout/card/CardConfiguration.kt`

**Verification**:
```bash
./gradlew :{module}:compileDebugKotlin
./gradlew :{module}:check
./gradlew apiDump
```

**Commit**: "Implement {Module}Configuration. COSDK-XXX"

---

## Phase 4: Create Public Navigation Keys
**Goal**: Create public navigation keys for the module

**Files to create**:
- [ ] `{module}/src/main/java/com/adyen/checkout/{module}/{Module}NavigationKeys.kt`
  - `{Module}MainNavigationKey` data object implementing `CheckoutNavigationKey`

**Reference**: `mbway/src/main/java/com/adyen/checkout/mbway/MBWayNavigationKeys.kt`

**Verification**:
```bash
./gradlew :{module}:compileDebugKotlin
./gradlew :{module}:check
./gradlew apiDump
```

**Commit**: "Add {Module} public navigation keys. COSDK-XXX"

---

## Phase 5: Implement State Classes
**Goal**: Create the state management classes

**Files to implement**:
- [ ] `{module}/src/main/java/com/adyen/checkout/{module}/internal/ui/state/{Module}Intent.kt`
  - Sealed interface extending `ComponentStateIntent`
  - Intents: `Update{Field}`, `Update{Field}Focus`, `UpdateLoading`, `HighlightValidationErrors`

- [ ] `{module}/src/main/java/com/adyen/checkout/{module}/internal/ui/state/{Module}ComponentState.kt`
  - Data class implementing `ComponentState`
  - Properties: payment-method-specific `TextInputComponentState` fields, `isLoading: Boolean`

- [ ] `{module}/src/main/java/com/adyen/checkout/{module}/internal/ui/state/{Module}ViewState.kt`
  - Data class implementing `ViewState`
  - Properties: payment-method-specific `TextInputViewState` fields, `isLoading: Boolean`

- [ ] `{module}/src/main/java/com/adyen/checkout/{module}/internal/ui/state/{Module}PaymentComponentState.kt`
  - Data class implementing `PaymentComponentState<{Module}PaymentMethod>`
  - Properties: `data: PaymentComponentData<{Module}PaymentMethod>`, `isValid: Boolean`

**Reference**: MBWay state classes

**Verification**:
```bash
./gradlew :{module}:compileDebugKotlin
./gradlew :{module}:check
./gradlew apiDump
```

**Commit**: "Implement {Module} state classes. COSDK-XXX"

---

## Phase 6: Implement State Infrastructure + Validation Logic
**Goal**: Create the state management infrastructure with full validation logic

**Files to implement**:
- [ ] `{module}/src/main/java/com/adyen/checkout/{module}/internal/ui/state/{Module}ComponentStateFactory.kt`
  - Class implementing `ComponentStateFactory<{Module}ComponentState>`
  - `createInitialState()` method returning initial state with empty fields

- [ ] `{module}/src/main/java/com/adyen/checkout/{module}/internal/ui/state/{Module}ComponentStateReducer.kt`
  - Class implementing `ComponentStateReducer<{Module}ComponentState, {Module}Intent>`
  - `reduce()` method handling all intents

- [ ] `{module}/src/main/java/com/adyen/checkout/{module}/internal/ui/state/{Module}ComponentStateValidator.kt`
  - Class implementing `ComponentStateValidator<{Module}ComponentState>`
  - `validate()` and `isValid()` methods
  - **Migrate validation logic from old implementation** (e.g., `old/internal/ui/model/{Module}OutputData.kt`)

- [ ] `{module}/src/main/java/com/adyen/checkout/{module}/internal/ui/state/{Module}ViewStateProducer.kt`
  - Class implementing `ViewStateProducer<{Module}ComponentState, {Module}ViewState>`
  - `produce()` method

**Core module changes for localization** (if validation errors need new keys):
- [ ] `core/.../CheckoutLocalizationKey.kt` - Add new localization key
- [ ] `core/.../DefaultLocalizationSource.kt` - Add mapping to string resource
- [ ] `core/src/main/res/values/strings.xml` - Add string resource (copy from module if exists)

**Reference**: 
- MBWay state infrastructure classes
- `old/internal/ui/model/{Module}OutputData.kt` for validation logic

**Verification**:
```bash
./gradlew :{module}:compileDebugKotlin
./gradlew :core:compileDebugKotlin
./gradlew :{module}:check
./gradlew :core:check
./gradlew apiDump
```

**Commit**: "Implement {Module} state infrastructure with validation. COSDK-XXX"

---

## Phase 7: Create State Extension
**Goal**: Create extension to convert component state to payment state

**Files to create**:
- [ ] `{module}/src/main/java/com/adyen/checkout/{module}/internal/ui/state/{Module}ComponentStateExt.kt`
  - `{Module}ComponentState.toPaymentComponentState()` extension function
  - **Migrate state conversion logic from old implementation** (e.g., `old/internal/ui/Default{Module}Delegate.kt` - `createComponentState()`)

**Reference**: `mbway/.../MBWayComponentStateExt.kt`

**Verification**:
```bash
./gradlew :{module}:compileDebugKotlin
./gradlew :{module}:check
./gradlew apiDump
```

**Commit**: "Add {Module}ComponentState extension for payment state conversion. COSDK-XXX"

---

## Phase 8: Create Internal Navigation Keys
**Goal**: Create internal navigation keys for component navigation

**Files to create**:
- [ ] `{module}/src/main/java/com/adyen/checkout/{module}/internal/ui/{Module}NavKeys.kt`
  - `{Module}NavKey` data object implementing `NavKey`

**Reference**: `mbway/internal/ui/MBWayNavKeys.kt`

**Verification**:
```bash
./gradlew :{module}:compileDebugKotlin
./gradlew :{module}:check
./gradlew apiDump
```

**Commit**: "Add {Module} internal navigation keys. COSDK-XXX"

---

## Phase 9: Implement Component + Composable Placeholder
**Goal**: Create the main component class with full submit logic

**Files to implement**:
- [ ] `{module}/src/main/java/com/adyen/checkout/{module}/internal/ui/{Module}Component.kt`
  - Class implementing `PaymentComponent<{Module}PaymentComponentState>`
  - Constructor parameters: componentParams, analyticsManager, componentStateValidator, componentStateFactory, componentStateReducer, viewStateProducer, coroutineScope
  - Properties: navigation, navigationStartingPoint, eventChannel, eventFlow, componentState, viewState
  - Methods: `submit()`, `setLoading()`, `onIntent()`
  - Composable: `MainScreen()` - calls `{Module}Component()` composable from view package
  - **Migrate submit logic from old implementation** (e.g., `old/internal/ui/Default{Module}Delegate.kt` - `onSubmit()`)

- [ ] `{module}/src/main/java/com/adyen/checkout/{module}/internal/ui/view/{Module}Component.kt` (composable)
  - Add parameters: `viewState: {Module}ViewState`, `onSubmitClick: () -> Unit`, `onIntent: ({Module}Intent) -> Unit`
  - Leave implementation as TODO placeholder

**Reference**: `mbway/internal/ui/MBWayComponent.kt`

**Verification**:
```bash
./gradlew :{module}:compileDebugKotlin
./gradlew :{module}:check
./gradlew apiDump
```

**Commit**: "Implement {Module}Component with submit logic. COSDK-XXX"

---

## Phase 10: Implement Factory
**Goal**: Create the factory for component creation

**Files to implement**:
- [ ] `{module}/src/main/java/com/adyen/checkout/{module}/internal/ui/{Module}Factory.kt`
  - Class implementing `PaymentMethodFactory<{Module}PaymentComponentState, {Module}Component>`
  - `create()` method that instantiates all dependencies and {Module}Component

**⚠️ IMPORTANT**: Follow `MBWayFactory` pattern exactly:
- Access `componentParams` from `componentParamsBundle.commonComponentParams`
- Do NOT add `supportedPaymentMethods` property (not part of `PaymentMethodFactory` interface)

**Reference**: `mbway/internal/ui/MBWayFactory.kt`

**Verification**:
```bash
./gradlew :{module}:compileDebugKotlin
./gradlew :{module}:check
./gradlew apiDump
```

**Commit**: "Implement {Module}Factory. COSDK-XXX"

---

## Phase 11: Implement Initializer & AndroidManifest
**Goal**: Create the startup initializer to register the factory and configure manifest

**Files to implement**:
- [ ] `{module}/src/main/java/com/adyen/checkout/{module}/internal/{Module}Initializer.kt`
  - Class implementing `Initializer<Unit>`
  - `@Keep` annotation
  - `create()` method registering `{Module}Factory` with `PaymentMethodProvider`
  - `dependencies()` returning empty list

**Files to modify**:
- [ ] `{module}/src/main/AndroidManifest.xml`
  - Add startup provider entry for `{Module}Initializer`

**Reference**: `mbway/internal/MBWayInitializer.kt` and `mbway/src/main/AndroidManifest.xml`

**Verification**:
```bash
./gradlew :{module}:compileDebugKotlin
./gradlew :{module}:check
./gradlew apiDump
```

**Commit**: "Implement {Module}Initializer and update AndroidManifest. COSDK-XXX"

---

## Phase 12: Final Verification
**Goal**: Ensure everything compiles and passes checks

**Actions**:
- [ ] Run `./gradlew apiDump`
- [ ] Run `./gradlew :{module}:check`
- [ ] Verify no public API changes are unintentional

**Commit**: (only if fixes needed) "Fix {Module} compilation issues. COSDK-XXX"

---

## Summary: Files Changed

### Core Module
| File | Action |
|------|--------|
| `core/.../paymentmethod/{Module}PaymentMethod.kt` | Create |
| `core/.../paymentmethod/PaymentMethodDetails.kt` | Modify (add to serializer) |
| `core/.../CheckoutLocalizationKey.kt` | Modify (add keys if needed) |
| `core/.../DefaultLocalizationSource.kt` | Modify (add mappings if needed) |
| `core/src/main/res/values/strings.xml` | Modify (add strings if needed) |

### Module (Public API)
| File | Action |
|------|--------|
| `{module}/build.gradle` | Modify |
| `{module}/{Module}Configuration.kt` | Create |
| `{module}/{Module}NavigationKeys.kt` | Create |

### Module (Internal)
| File | Action |
|------|--------|
| `{module}/internal/{Module}Initializer.kt` | Implement |
| `{module}/internal/ui/{Module}Component.kt` | Implement |
| `{module}/internal/ui/{Module}Factory.kt` | Implement |
| `{module}/internal/ui/{Module}NavKeys.kt` | Create |
| `{module}/internal/ui/state/{Module}Intent.kt` | Implement |
| `{module}/internal/ui/state/{Module}ComponentState.kt` | Implement |
| `{module}/internal/ui/state/{Module}ViewState.kt` | Implement |
| `{module}/internal/ui/state/{Module}PaymentComponentState.kt` | Implement |
| `{module}/internal/ui/state/{Module}ComponentStateFactory.kt` | Implement |
| `{module}/internal/ui/state/{Module}ComponentStateReducer.kt` | Implement |
| `{module}/internal/ui/state/{Module}ComponentStateValidator.kt` | Implement |
| `{module}/internal/ui/state/{Module}ViewStateProducer.kt` | Implement |
| `{module}/internal/ui/state/{Module}ComponentStateExt.kt` | Create |
| `{module}/internal/ui/view/{Module}Component.kt` | Implement (placeholder) |
| `{module}/src/main/AndroidManifest.xml` | Modify |

---

## Common Gotchas

1. **Factory pattern**: Always follow `MBWayFactory` exactly. Access `componentParams` via `componentParamsBundle.commonComponentParams`, not by constructing it directly.

2. **Visibility**: All new classes should be `internal` unless they need to be public. Use `@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)` for cross-module internal usage.

3. **Validation strings**: If adding new validation error messages, add the localization key and string to the `core` module, not the payment method module.

4. **Serialization**: Use `@Serializable` annotation and create proper serializer companion object in PaymentMethod class.

5. **API dump**: Always run `./gradlew apiDump` before committing to capture public API changes.

---

## Notes

- Each phase should be a separate commit
- Run verification commands after each phase before committing
- Follow existing code style and patterns from MBWay
- UI implementation is handled separately (see UI_MIGRATION_PLAN template)
