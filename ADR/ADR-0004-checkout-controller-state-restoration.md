# Restoring a CheckoutController after process death

| Field         | Value        |
|---------------|--------------|
| Author(s)     | Oscar Spruit |
| Status        | In progress  |
| Creation date | 2026-10-07   |

## Abstract

Passing a `SavedStateHandle` to the `CheckoutController` factory functions turned out not to be enough:

- Several controllers can share one `SavedStateHandle`, so the state has to be keyed per controller.
- Restoring is implicit: creating a controller means "create, or restore if something is saved under this key". A
  shopper who goes back mid-payment can leave a phase behind that the next controller continues.
- `SavedStateHandle` forces a `ViewModel`, which doesn't fit every integration, Flutter or React Native.

Recommendation: **option 2**. The controller exposes an opaque `SavedState`, and a separate factory function restores a
controller from it. The merchant decides where to keep the state and when to drop it.

## Motivation

### Scope

- Only the sessions and action-only flows. The advanced flow is being removed.
- Only the controller state is restored: the `CheckoutFlowPhase` and the state it needs to continue, such as the latest
  `sessionData`. Input and UI state are not restored. If they ever are, that happens in the Compose layer.

### What has to be saved

| State                                                                | Scope      | Where it lives today                          |
|----------------------------------------------------------------------|------------|-----------------------------------------------|
| `CheckoutFlowPhase` (`Input`, `Submitted`, `HandlingAction(action)`) | Controller | `CheckoutFlowStateStore` → `SavedStateHandle` |
| Action component data (`paymentData`, `nativeRedirectData`)          | Controller | `PaymentDataRepository` → `SavedStateHandle`  |
| Latest `sessionData`                                                 | Session    | `DefaultSessionStateStore`, memory only       |
| `CheckoutContext` and `CheckoutTarget`                               | Controller | Merchant                                      |

`sessionData` can change while the controller is still in `Input`, for example when `onBeforeSubmit` returns patched
`sessionData` or a refused `/payments` call returns new `sessionData`. So the state is worth saving in every phase, not
only while an action is being handled.

`sessionData` is scoped to the session, while the phase is scoped to the controller. Drop-in also updates
`sessionData` outside any controller, when a stored payment method is removed.

## Solutions

### Option 1 — `SavedStateHandle`, keyed by session ID

The factory functions keep their current shape. Internally the state is keyed by the session ID, so there is at most one
controller per session. To restore, the merchant keeps the `CheckoutContext` and `CheckoutTarget` and creates the
controller again with the same `SavedStateHandle`.

```kotlin
fun CheckoutController(
    target: CheckoutTarget,
    context: CheckoutContext.Sessions,
    callbacks: SessionCheckoutCallbacks,
    coroutineScope: CoroutineScope,
    savedStateHandle: SavedStateHandle,
): CheckoutController

fun CheckoutController(
    context: CheckoutContext.ActionOnly,
    callbacks: ActionOnlyCheckoutCallbacks,
    coroutineScope: CoroutineScope,
    savedStateHandle: SavedStateHandle,
): CheckoutController
```

Usage:

```kotlin
class CheckoutViewModel(private val savedStateHandle: SavedStateHandle) : ViewModel() {

    private var controller: CheckoutController? = null

    init {
        // The merchant has to keep the context and target themselves to be able to restore. CheckoutTarget isn't
        // Parcelable, so they keep what they need to rebuild it.
        val context = savedStateHandle.get<CheckoutContext.Sessions>(CONTEXT_KEY)
        val paymentMethodType = savedStateHandle.get<String>(PAYMENT_METHOD_TYPE_KEY)

        if (context != null && paymentMethodType != null) {
            // Restores implicitly: the SDK finds the saved phase in savedStateHandle under the session ID
            controller = createController(context, CheckoutTarget.PaymentMethod(paymentMethodType))
        } else {
            viewModelScope.launch { setupCheckout() }
        }
    }

    private suspend fun setupCheckout() {
        val sessionResponse = fetchSessionResponse() // The merchant's own /sessions call
        when (val result = Checkout.setup(sessionResponse, configuration)) {
            is Checkout.Result.Success -> {
                savedStateHandle[CONTEXT_KEY] = result.checkoutContext
                savedStateHandle[PAYMENT_METHOD_TYPE_KEY] = PAYMENT_METHOD_TYPE
                controller = createController(result.checkoutContext, CheckoutTarget.PaymentMethod(PAYMENT_METHOD_TYPE))
            }
            is Checkout.Result.Error -> onSetupFailed(result.error)
        }
    }

    // Also used for a new flow. If a phase is still saved for this session, it is continued.
    private fun createController(context: CheckoutContext.Sessions, target: CheckoutTarget) = CheckoutController(
        target = target,
        context = context,
        callbacks = callbacks,
        coroutineScope = viewModelScope,
        savedStateHandle = savedStateHandle,
    )
}
```

There is no explicit "is there saved state?" check. The merchant can only check whether they kept a context, and
whether a phase is continued is decided inside the SDK.

**Pros:**

- No new public API. It matches how `sessionData` is already keyed in `SessionStateStore`.

**Cons:**

- Forces a `ViewModel`. This doesn't work for every integration type, Flutter or React Native.
- The action-only flow has no session ID, so it needs a second keying scheme.
- At most one controller per session.
- Restoring is still implicit. Going back mid-payment and picking another payment method in the same session resumes the
  old `HandlingAction`.
- The merchant still has to keep the context and target. If they create a new session after process death, as the
  example app does, the restore silently misses.

### Option 2 — `saveState()` and restore from `SavedState`

The controller exposes an opaque, `Parcelable` `SavedState`. A separate factory function creates a controller from it.
The merchant fully owns the lifecycle of the state and what is used to keep it.

```kotlin
class CheckoutController {

    /**
     * Always returns a state, because `sessionData` can change in every phase.
     */
    fun saveState(): SavedState

    sealed interface SavedState : Parcelable {
        class Sessions internal constructor(/* phase, sessionData, action data, context, target */) : SavedState
        class ActionOnly internal constructor(/* phase, action data, context */) : SavedState
    }
}

fun CheckoutController(
    savedState: CheckoutController.SavedState.Sessions,
    callbacks: SessionCheckoutCallbacks,
    coroutineScope: CoroutineScope,
): CheckoutController

fun CheckoutController(
    savedState: CheckoutController.SavedState.ActionOnly,
    callbacks: ActionOnlyCheckoutCallbacks,
    coroutineScope: CoroutineScope,
): CheckoutController
```

The existing factory functions no longer take a `SavedStateHandle` and always start a new flow.

Usage with a `ViewModel`:

```kotlin
class CheckoutViewModel(private val savedStateHandle: SavedStateHandle) : ViewModel() {

    private var controller: CheckoutController? = null

    init {
        val savedState = savedStateHandle.get<Bundle>(KEY)
            ?.let { BundleCompat.getParcelable(it, STATE, CheckoutController.SavedState.Sessions::class.java) }

        if (savedState != null) {
            // Contains the context and target, so no new session or setup is needed
            controller = CheckoutController(savedState, callbacks, viewModelScope)
        } else {
            viewModelScope.launch { setupCheckout() }
        }

        // Called at the moment the state is saved, so the merchant doesn't have to time saveState() calls
        savedStateHandle.setSavedStateProvider(KEY) {
            bundleOf(STATE to controller?.saveState())
        }
    }

    private suspend fun setupCheckout() {
        val sessionResponse = fetchSessionResponse() // The merchant's own /sessions call
        when (val result = Checkout.setup(sessionResponse, configuration)) {
            is Checkout.Result.Success -> {
                controller = CheckoutController(
                    target = CheckoutTarget.PaymentMethod(PAYMENT_METHOD_TYPE),
                    context = result.checkoutContext,
                    callbacks = callbacks,
                    coroutineScope = viewModelScope,
                )
            }
            is Checkout.Result.Error -> onSetupFailed(result.error)
        }
    }
}
```

Usage without a `ViewModel` (Activity, Fragment, the Flutter and React Native native code):

```kotlin
override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    val savedState = savedInstanceState
        ?.let { BundleCompat.getParcelable(it, STATE, CheckoutController.SavedState.Sessions::class.java) }

    if (savedState != null) {
        controller = CheckoutController(savedState, callbacks, lifecycleScope)
    } else {
        lifecycleScope.launch { setupCheckout() } // Same as in the ViewModel, with lifecycleScope
    }
}

override fun onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)
    outState.putParcelable(STATE, controller?.saveState())
}
```

If `SavedState` stays lean (see [Concerns and follow-up actions](#concerns-and-follow-up-actions)), the restore branch
also needs the context and target. The merchant then has to keep those next to the state, as in option 1.

**Pros:**

- Platform-agnostic: no `ViewModel`, `SavedStateHandle` or disk.
- Restoring is explicit, so the SDK never continues a state the merchant didn't ask for. This removes the stale state
  problem.
- The merchant does the keying, so multiple controllers are not the SDK's concern.
- Flutter and React Native can keep the state on the native side, so no Dart or JS API is needed at first.
- The shape maps to iOS (`Codable`) if it is ever needed there.
- Easiest to test: a save, restore and continue round trip is a plain unit test.
- Drop-in can use the same mechanism instead of its own session data restore.
- Convenience layers can be added on top later. See [Alternatives](#alternatives).

**Cons:**

- More work for the merchant than passing a `SavedStateHandle`.
- Each `SavedState` holds its own copy of the session-scoped `sessionData`. With several controllers for one session,
  restoring an older state could overwrite the store with outdated data. The docs have to say that only the active
  controller's state should be saved and restored.
- If the context is included, the state could get close to the saved state size limit
  (`TransactionTooLargeException`). See [Concerns and follow-up actions](#concerns-and-follow-up-actions).

### Option 3 — a key, with the state written to disk by the SDK

The factory functions take a key instead of a `SavedStateHandle`. The SDK writes the state, including the context and
target, to disk under that key. A new factory function restores a controller from just the key. The SDK manages the
lifecycle of the state on a best-effort basis.

```kotlin
fun CheckoutController(
    key: String,
    target: CheckoutTarget,
    context: CheckoutContext.Sessions,
    callbacks: SessionCheckoutCallbacks,
    coroutineScope: CoroutineScope,
): CheckoutController

fun CheckoutController(
    key: String,
    context: CheckoutContext.ActionOnly,
    callbacks: ActionOnlyCheckoutCallbacks,
    coroutineScope: CoroutineScope,
): CheckoutController

/**
 * @return null when nothing is stored for [key], or when the stored state can't be read.
 */
fun CheckoutController(
    key: String,
    callbacks: CheckoutCallbacks,
    coroutineScope: CoroutineScope,
): CheckoutController?
```

Usage:

```kotlin
class CheckoutViewModel : ViewModel() {

    private var controller: CheckoutController? = null

    init {
        // Reads the state stored on disk under KEY, including the context and target
        val restoredController = CheckoutController(KEY, callbacks, viewModelScope)

        if (restoredController != null) {
            controller = restoredController
        } else {
            viewModelScope.launch { setupCheckout() }
        }
    }

    private suspend fun setupCheckout() {
        val sessionResponse = fetchSessionResponse() // The merchant's own /sessions call
        when (val result = Checkout.setup(sessionResponse, configuration)) {
            is Checkout.Result.Success -> {
                // Overwrites whatever is still stored under KEY
                controller = CheckoutController(
                    key = KEY,
                    target = CheckoutTarget.PaymentMethod(PAYMENT_METHOD_TYPE),
                    context = result.checkoutContext,
                    callbacks = callbacks,
                    coroutineScope = viewModelScope,
                )
            }
            is Checkout.Result.Error -> onSetupFailed(result.error)
        }
    }
}
```

The ViewModel isn't required here. The same code works with any coroutine scope. Because the state is on disk, the
restore branch also runs after an app restart. The merchant has to pick keys that don't continue a payment the shopper
already left behind.

**Pros:**

- The merchant only manages keys.
- Doesn't depend on a `ViewModel` or `Bundle`, and survives an app restart.

**Cons:**

- Stale state gets worse. Saved instance state is cleared when the task is removed, but a file on disk outlives app
  restarts. Best-effort cleanup is the stale state problem on a longer timescale.
- Payment data is stored at rest. `paymentData`, `sessionData` and the context would be written outside the
  system-managed saved state, which raises encryption, retention and compliance questions.
- Needs an Android `Context` for file access.
- Adds failure modes: the key exists but the file is gone, corrupt, or from an older SDK version.
- The callbacks and scope still have to be passed again, and their type has to be checked against the stored flow at
  runtime.
- No equivalent on iOS, so the public API can't align across platforms.

### Comparison

|                                         | Option 1        | Option 2          | Option 3          |
|-----------------------------------------|-----------------|-------------------|-------------------|
| Works without a `ViewModel`             | No              | Yes               | Yes               |
| Works for Flutter and React Native      | No              | Yes (native side) | Yes               |
| Multiple controllers                    | One per session | Merchant keys     | Merchant keys     |
| Stale state after going back            | Yes             | No                | Worse (on disk)   |
| Who owns the state's lifecycle          | `ViewModel`     | Merchant          | SDK, best effort  |
| Merchant keeps context and target       | Yes             | No, if included   | No                |
| Payment data at rest outside the system | No              | No                | Yes               |
| Aligns with iOS                         | No              | Yes               | No                |
| Testability                             | Needs fakes     | Plain round trip  | Needs file system |

## Proposed solution

**Option 2**: `controller.saveState()` returns an opaque `SavedState`, and a `CheckoutController(savedState, callbacks,
coroutineScope)` factory function restores a controller from it. The existing factory functions no longer take a
`SavedStateHandle` and always start a new flow.

- Restoring is explicit, so the SDK never continues a state the merchant didn't ask for.
- It doesn't depend on a `ViewModel`, `SavedStateHandle` or disk, so it works for every integration type, Flutter and
  React Native.
- The merchant owns the lifecycle of the state, and keys it themselves when they have several controllers.
- Convenience layers, like the ones in [Alternatives](#alternatives), can be added on top later.

## Final decision

Pending discussion.

## Concerns and follow-up actions

1. Does `SavedState` include the `CheckoutContext` and `CheckoutTarget`?
    - **Self-contained:** restoring takes only the saved state. Merchants can't get it wrong, for example by creating a
      new session after process death. The cost is size: measure a realistic payment methods response and configuration
      against the saved state size limit.
    - **Lean:** restoring also takes the context and target. The state stays small, but recreating the context is easy
      to get wrong.
2. What happens when a controller is restored in `Submitted`? The result of the `/payments` call is unknown. The
   closest precedent is a non-restorable action, which fails with "check the payment status on your server".
3. Does Drop-in move to this mechanism for its session data restore?

## Details

### Alternatives

These are convenience layers that could sit on top of option 2. Neither replaces it.

#### `SavedStateRegistryOwner` and a key

`SavedStateHandle` uses this underneath. Activities, Fragments and nav back stack entries implement it, so it removes
the `ViewModel` requirement.

```kotlin
fun CheckoutController(
    target: CheckoutTarget,
    context: CheckoutContext.Sessions,
    callbacks: SessionCheckoutCallbacks,
    coroutineScope: CoroutineScope,
    savedStateRegistryOwner: SavedStateRegistryOwner,
    key: String,
): CheckoutController
```

Still Android-only, a plain `FlutterActivity` doesn't implement it, and restoring is still implicit.

#### Compose: `rememberSaveable` with a `Saver`

v6 is Compose-first. Saved state is dropped when the composable leaves the composition, which fits "the shopper goes
back mid-payment" exactly.

```kotlin
@Composable
fun rememberCheckoutController(
    target: CheckoutTarget,
    context: CheckoutContext.Sessions,
    callbacks: SessionCheckoutCallbacks,
): CheckoutController
```

The controller's coroutine scope usually belongs in a `ViewModel`, not in the composition, so this only suits simpler
integrations.
