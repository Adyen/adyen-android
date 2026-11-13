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

This ADR proposes making `CheckoutContext` a **live object** that owns the session and runs at most one payment at a
time:

- **Saving and restoring happen on the context.** Internally, the context produces a small, versioned `String` with the
  session state and the state of the payment in progress, which it pulls from the components that own it. Merchants only
  get extension functions that take a `SavedStateHandle`: `context.saveState(savedStateHandle)` and
  `Checkout.restore(savedStateHandle)`.
- **One flow for merchants, without checks.** `Checkout.restore(savedStateHandle)` synchronously returns the saved state,
  or null. `Checkout.setup(...)` takes that optional state: it continues the saved session when there is one, otherwise
  it sets up the new one. The merchant then creates controllers from the context as always.
- **Restoring is explicit.** Only a context set up with a saved state carries a payment in progress. Each controller
  created from it checks whether its target is the one that was in progress: if so it resumes, once, otherwise it
  starts fresh.
- **Drop-in and components share the session.** Every context of a session is a view on one session state, kept in sync
  in the process and merged by revision across process death.
- **Partial payments** can be added later without breaking the public API, the way iOS does it: the SDK handles the
  order internally and the context updates itself.

This is a breaking change to the alpha API: the `savedStateHandle` parameter of `CheckoutController(...)` is removed, and
`CheckoutContext` is no longer `Parcelable`.

## Motivation

### Scope

- Only the sessions and action-only flows. The advanced flow is being removed.
- Only controller state is restored: the phase of the payment and what it needs to continue. Input and UI state are not
  restored. If they ever are, that happens in the Compose layer.
- Action-only is not available in Drop-in.

### What has to be saved

| State                                                                | Scope      | Where it lives today                          |
|----------------------------------------------------------------------|------------|-----------------------------------------------|
| `CheckoutFlowPhase` (`Input`, `Submitted`, `HandlingAction(action)`) | Controller | `CheckoutFlowStateStore` → `SavedStateHandle` |
| Action component data (`paymentData`, `nativeRedirectData`)          | Controller | `PaymentDataRepository` → `SavedStateHandle`  |
| Latest `sessionData`                                                 | Session    | `DefaultSessionStateStore`, memory only       |
| `CheckoutContext` and `CheckoutTarget`                               | Controller | Merchant                                      |

`sessionData` is scoped to the session, while the phase is scoped to the controller. Drop-in also updates `sessionData`
outside any controller, when a stored payment method is removed. A future partial payment adds an open order, which is
also scoped to the session.

### Constraints

- **Saved state is small and shared.** Android passes saved state through a binder buffer of about 1 MB, and recommends
  keeping it under about 50 KB. The merchant's own state uses the same budget for their activity. Strings are stored as
  UTF-16, so twice their character count.
- **Reference sizes** from `example-app/src/androidTest/assets`: `sessionData` is 2,081 characters (about 4 KB in a
  `Parcel`). The setup response is 8.5 KB of JSON, of which 3.5 KB is payment methods (30 payment methods, no issuer
  lists). Real responses are larger. Payment methods must not be saved.
- **The launch intent of an activity is not part of the saved-state budget.** The system keeps it with the activity and
  delivers it again after process death. It only has to fit through the binder call at launch.
- **An activity's saved state is its own.** Drop-in's bundle is sent separately from the merchant's activity.
- **Callbacks can't cross activities.** Lambdas can't be put in an intent, so callbacks only exist in the merchant's
  process.
- **Partial payments must be addable later without breaking the public API.**

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
```

**Pros:**

- No new public API. It matches how `sessionData` is already keyed in `SessionStateStore`.

**Cons:**

- Forces a `ViewModel`. This doesn't work for every integration type, Flutter or React Native.
- The action-only flow has no session ID, so it needs a second keying scheme.
- At most one controller per session.
- Restoring is still implicit. Going back mid-payment and picking another payment method in the same session resumes the
  old `HandlingAction`.
- The merchant still has to keep the context and target. `CheckoutTarget` isn't `Parcelable`, and the context contains
  the payment methods. If they create a new session after process death, as the example app does, the restore silently
  misses.

### Option 2 — `controller.saveState()` and restore from `SavedState`

The controller exposes an opaque, `Parcelable` `SavedState`. A separate factory function creates a controller from it.
The merchant fully owns the lifecycle of the state.

```kotlin
fun CheckoutController.saveState(): CheckoutController.SavedState

fun CheckoutController(
    savedState: CheckoutController.SavedState.Sessions,
    callbacks: SessionCheckoutCallbacks,
    coroutineScope: CoroutineScope,
): CheckoutController
```

**Pros:**

- Platform-agnostic: no `ViewModel`, `SavedStateHandle` or disk.
- Restoring is explicit, so the SDK never continues a state the merchant didn't ask for.
- Easy to test: a save, restore and continue round trip is a plain unit test.

**Cons:**

- Session state ends up in a controller's state. Each `SavedState` holds its own copy of `sessionData`, so with several
  controllers for one session, restoring an older state could overwrite the newer value.
- An open order has no controller. With a partial payment, the shopper may be on the payment method list without any
  controller, so restoring needs a second, async path through setup. The merchant has to know which path to take.
- Following v5, a partial payment would end the controller and hand the order to the merchant, who calls setup a second
  time. iOS hides the order from its public API, and merchants shouldn't have to call setup twice.
- Saving the context or target with it either exceeds the size budget, or leaves the merchant to keep them.
- The context is a value, so it can't collect state from components. Component state would have to be pushed into a
  global store whenever it changes.

### Option 3 — a key, with the state written to disk by the SDK

The factory functions take a key instead of a `SavedStateHandle`. The SDK writes the state, including the context and
target, to disk under that key, and a new factory function restores a controller from just the key.

```kotlin
fun CheckoutController(
    key: String,
    target: CheckoutTarget,
    context: CheckoutContext.Sessions,
    callbacks: SessionCheckoutCallbacks,
    coroutineScope: CoroutineScope,
): CheckoutController

// null when nothing is stored for key, or when the stored state can't be read
fun CheckoutController(key: String, callbacks: CheckoutCallbacks, coroutineScope: CoroutineScope): CheckoutController?
```

**Pros:**

- The merchant only manages keys.
- Doesn't depend on a `ViewModel` or `Bundle`, and survives an app restart.

**Cons:**

- Stale state gets worse. Saved instance state is cleared when the task is removed, but a file on disk outlives app
  restarts. Best-effort cleanup is the stale state problem on a longer timescale.
- Payment data is stored at rest. `paymentData`, `sessionData` and the context would be written outside the
  system-managed saved state, which raises encryption, retention and compliance questions.
- Needs an Android `Context` for file access, and adds failure modes: the file is gone, corrupt, or from an older SDK
  version.
- No equivalent on iOS, so the public API can't align across platforms.

### Option 4 — a live `CheckoutContext` that owns the session

`CheckoutContext` becomes a live object that owns the session state and at most one payment in progress. Controllers are
still created per payment, with their own callbacks. Saving and restoring happen on the context, which collects the
state of the payment in progress from its components. See [Proposed solution](#proposed-solution).

**Pros:**

- One restore path for every case, including an open order, without checks in merchant code.
- The session state has a single owner, so several controllers and Drop-in share the latest `sessionData`.
- State is collected from the components that own it, so payment components can add their own state later.
- Only a few KB are saved. The payment methods and the context are never saved.
- Partial payments can be added later without breaking the API, and without a second setup call.
- Matches iOS, whose `Checkout` is the same kind of live object.

**Cons:**

- Breaking change to the alpha API: the `savedStateHandle` parameter is removed and `CheckoutContext` is no longer
  `Parcelable`. `DropInResultContract.Input` parcels the context today.
- Public save and restore only work with a `SavedStateHandle`, so Activity-only integrations and the wrappers need a view
  model on the native side, until the `String` functions are exposed.
- Setup is async, so the merchant has to hold a redirect return until the controller exists.

### Comparison

|                                         | Option 1        | Option 2          | Option 3          | Option 4                 |
|-----------------------------------------|-----------------|-------------------|-------------------|--------------------------|
| Works without a `ViewModel`             | No              | Yes               | Yes               | Not yet (`SavedStateHandle` only) |
| Restoring is explicit                   | No              | Yes               | Yes               | Yes                      |
| Merchant code without checks            | No              | No                | No                | Yes                      |
| Multiple controllers                    | One per session | Merchant keys     | Merchant keys     | Each controller checks the context |
| Session state shared with Drop-in       | No              | Copies per controller | Copies per key | One owner per session   |
| Open order after process death          | No              | Second restore path | Second restore path | Same path            |
| Size of the saved state                 | Small, but the merchant keeps the context | Small or large | Disk | A few KB          |
| Payment data at rest outside the system | No              | No                | Yes               | No                       |
| Aligns with iOS                         | No              | Partly            | No                | Yes, except save and restore |

## Proposed solution

**Option 4.** A live `CheckoutContext` owns the session and at most one payment in progress, and is saved and restored
through `SavedStateHandle` extensions.

### Decisions

| Topic | Decision |
|---|---|
| Owner of session state | The live `CheckoutContext`, no global store per controller |
| Payments per context | One at a time |
| Where state is saved | On the context, pulled from the payment in progress |
| Public save and restore API | Only extension functions that take a `SavedStateHandle`. The `String` based functions are internal |
| Restore | Sync `Checkout.restore(savedStateHandle)` returns the saved state. `Checkout.setup` takes it as an optional parameter. The merchant needs no `if` |
| Saved state usage | Single-use. Removed from the handle when a setup with it succeeds |
| Restoring several controllers | Each controller checks the context: the target that was in progress resumes, any other starts fresh |
| Saved state format | Opaque, versioned `String`, internal |
| Callbacks | Per controller. Drop-in uses `DropInService` for merchant hooks |
| Action-only | One action per context, passed to setup |
| Redirect return | Stays `controller.handleReturn(intent)`. The SDK doesn't buffer returns |
| Every action component | Implements `RestorableActionComponent` |
| Partial payments | Later, handled internally like iOS, without a public order type |
| Drop-in launch | The context's data part and session state go in the launch intent |

### Public API sketch

Names and exact signatures are not final.

```kotlin
object Checkout {
    /**
     * Continues the session of [savedState] when it is not null. Otherwise calls [sessionResponse] and sets up that
     * session. [sessionResponse] is only called when it is needed, so no session is created for nothing.
     */
    suspend fun setup(
        configuration: CheckoutConfiguration,
        savedState: CheckoutSavedState?,
        sessionResponse: suspend () -> SessionResponse,
    ): Result<CheckoutContext.Sessions>

    // A context handles exactly one action. [action] is only called when there is no saved state to continue
    suspend fun setup(
        configuration: CheckoutConfiguration,
        savedState: CheckoutSavedState?,
        action: suspend () -> Action,
    ): Result<CheckoutContext.ActionOnly>

    // Unchanged, for merchants who don't restore
    suspend fun setup(sessionResponse: SessionResponse, configuration: CheckoutConfiguration):
        Result<CheckoutContext.Sessions>
    suspend fun setup(action: Action, configuration: CheckoutConfiguration): Result<CheckoutContext.ActionOnly>
}

/**
 * Opaque. Can be used for one successful setup.
 */
class CheckoutSavedState internal constructor(/* the versioned String, and the handle it came from */)

/**
 * Reads the state the SDK saved in [savedStateHandle]. Synchronous, no network. Null when nothing was saved.
 */
fun Checkout.restore(savedStateHandle: SavedStateHandle): CheckoutSavedState?

/**
 * Saves this context in [savedStateHandle] whenever Android saves state. Call it once for every context, new or
 * restored.
 */
fun CheckoutContext.saveState(savedStateHandle: SavedStateHandle)

sealed interface CheckoutContext {

    class Sessions : CheckoutContext {
        val paymentMethods: StateFlow<List<PaymentMethod>>
        val storedPaymentMethods: StateFlow<List<StoredPaymentMethod>>
        val amount: StateFlow<Amount?>

        // Only set on a context set up with a saved state, until its controller is created
        val paymentInProgress: PaymentInProgress?
    }

    class ActionOnly : CheckoutContext
}

fun CheckoutController(
    target: CheckoutTarget,
    context: CheckoutContext.Sessions,
    callbacks: SessionCheckoutCallbacks,
    coroutineScope: CoroutineScope,
): CheckoutController

fun CheckoutController(
    context: CheckoutContext.ActionOnly,
    callbacks: ActionOnlyCheckoutCallbacks,
    coroutineScope: CoroutineScope,
): CheckoutController

DropIn.start(launcher, context: CheckoutContext.Sessions, serviceClass, theme)
```

Internally, and for Drop-in through `@RestrictTo(LIBRARY_GROUP)`, the base functions work on the `String`:

```kotlin
fun CheckoutContext.saveStateString(): String?   // null when there is nothing to continue
fun Checkout.restore(savedState: String): CheckoutSavedState?   // null for an unknown version
```

The extensions use an SDK-owned key in the handle, so they don't clash with the merchant's own keys. Exposing the base
functions later, for example for the cross-platform wrappers or for storing the state elsewhere, is an addition and
doesn't break anything. Doing it the other way around would.

Rules for the saved state:

- **Single-use.** After a setup with it succeeds, the state counts as used and its entry is removed from the handle. A
  later `Checkout.restore` in the same view model returns null, and a second setup with the same object ignores it.
  Without this, a new checkout in the same view model would resume an old payment.
- **Kept until setup succeeds.** `Checkout.restore` only reads the entry. A process death while setup is still running
  loses nothing, and a failed setup can be retried with the same object. Setup errors say so: retrying without the state
  loses the payment in progress.
- **The saved session wins.** With a saved state, setup continues that session and doesn't call the `sessionResponse`
  or `action` lambda.
- **Wrong flow.** A state of the action-only flow passed to the sessions setup, or the other way around, is treated as
  nothing to continue. Setup logs it and uses the lambda.

The `savedStateHandle` parameter of the `CheckoutController(...)` functions is removed. `CheckoutController.handleReturn`
stays as it is: a return is always passed to a specific controller, so the SDK never buffers returns and can't deliver
one to the wrong controller.

### Merchant usage

#### Sessions, from a view model

```kotlin
class CheckoutViewModel(private val savedStateHandle: SavedStateHandle) : ViewModel() {

    private var context: CheckoutContext.Sessions? = null
    private var controller: CheckoutController? = null

    // A return can arrive before setup has finished, so it is kept until the controller exists
    private var pendingReturn: Intent? = null

    init {
        // Sync, no network. Null when nothing was saved
        val savedState = Checkout.restore(savedStateHandle)

        viewModelScope.launch {
            // Continues the saved session if there is one. Otherwise fetches and sets up a new one
            val result = Checkout.setup(configuration, savedState) { fetchSessionResponse() }
            // Identical from here on, whether the session was continued or new
            when (result) {
                is Checkout.Result.Success -> {
                    val context = result.checkoutContext
                    this@CheckoutViewModel.context = context
                    // Android pulls the state when it saves, so the merchant doesn't have to time anything
                    context.saveState(savedStateHandle)
                    // The payment method that was paying resumes, any other one starts fresh
                    val target = context.paymentInProgress?.target ?: defaultTarget
                    controller = CheckoutController(target, context, callbacks, viewModelScope).also { controller ->
                        pendingReturn?.let(controller::handleReturn)
                        pendingReturn = null
                    }
                }
                is Checkout.Result.Error -> onSetupFailed(result.error)
            }
        }
    }

    // Called from the activity's onCreate (launch intent) and onNewIntent
    fun onReturn(intent: Intent) {
        controller?.handleReturn(intent) ?: run { pendingReturn = intent }
    }
}
```

The merchant writes no `if` for restoring. Whether a payment resumes is decided in setup and in the controller.

Setup is async, so after process death the redirect return can arrive before the controller exists. Keeping it until
then is the merchant's responsibility, like any other intent their activity receives.

#### Sessions, several controllers

A merchant who shows a card form and a Google Pay button creates both controllers again, as they would without a
restore. Each controller looks at the context: if its target is the one that was in progress, it resumes that payment.
Otherwise it is a completely fresh controller.

```kotlin
// After setup, in the same way whether the session was continued or new
cardController = CheckoutController(CheckoutTarget.PaymentMethod("scheme"), context, cardCallbacks, viewModelScope)
googlePayController = CheckoutController(CheckoutTarget.PaymentMethod("googlepay"), context, googlePayCallbacks, viewModelScope)
// If the card payment was handling a redirect, cardController resumes it and googlePayController starts fresh
```

Only one controller can match, because a context has at most one payment in progress, and it can only be taken once.

#### Action-only

```kotlin
val savedState = Checkout.restore(savedStateHandle)

// The lambda gives the action from the merchant's own /payments call. It isn't called when a saved action continues
val result = Checkout.setup(configuration, savedState) { action }

// Identical whether the context was continued or new: a continued context resumes its action, a new one handles it
val context = result.checkoutContext
context.saveState(savedStateHandle)
val controller = CheckoutController(context, callbacks, viewModelScope)
```

- The context is bound to its action, so restoring needs no matching: the continued context already contains the
  action, and the merchant doesn't need to have kept it. The equality check in `ActionOnlyCheckoutFlow` goes away.
- A new action means a new context through setup.
- Once its action has ended, the context is finished. Creating a controller from it, or a second controller while the
  first one is still handling the action, fails visibly.

#### Partial payments (later)

```kotlin
// No second setup call. The same context now has the remaining amount and its payment methods
giftCard { onPartialPayment = { showPaymentMethods(context.paymentMethods.value) } }
val nextController = CheckoutController(nextTarget, context, callbacks, viewModelScope)
```

### Alignment with iOS

iOS won't add state restoration. Its `Checkout` is the equivalent of the live context.

| Topic | iOS | Android | Reason for the difference |
|---|---|---|---|
| Setup result | Live `Checkout` | Live `CheckoutContext` | Aligned in behavior. Name to decide |
| Creating components | `checkout.createPaymentComponent(for:)`, `checkout.createDropIn()` | `CheckoutController(target, context, ...)`, `DropIn.start(launcher, context)` | Existing Android shape |
| Callbacks | At setup | Per controller | The context crosses into Drop-in's activity, callbacks can't |
| Drop-in merchant hooks | Setup callbacks | `DropInService` | Must survive process death |
| Action-only | `setup(configuration:callbacks:)`, then `handle(action:)`, several actions per checkout | Action passed to setup, then `CheckoutController(context, ...)`, one action per context | The context's lifecycle and saved state are bound to one action. Callbacks: same reason as above |
| Payments in progress | One per checkout (`pendingPaymentComponent`, Drop-in navigation lock) | One per context | Aligned |
| Partial payments | Internal, Drop-in only | Later, internal | Aligned |
| Order type | Internal | Internal | Aligned |
| Redirect return | Static `Checkout.handleReturn(url:)`, routed to the active redirect | `controller.handleReturn(intent)` | Explicit routing: no buffering, and a return can't reach the wrong controller |
| Save and restore | None | `context.saveState(savedStateHandle)`, `Checkout.restore(savedStateHandle)`, `setup(configuration, savedState) { ... }` | Android-only, because of process death |

## Final decision

Pending discussion.

## Concerns and follow-up actions

### Open questions

1. **Naming.** `CheckoutContext` stays as the name of the live object, or aligns with iOS's `Checkout`.
2. **Is `paymentInProgress` still needed?** Controllers decide themselves whether they resume, so the merchant only
   needs it to pick which screen to show. If it stays: what it exposes besides the target, and whether it is cleared
   when its controller is created or when the payment ends.
3. **Calling `/sessions/{id}/setup` when continuing a saved session.** Always calling it gives a complete context, for
   example to retry after a failed action, but fails offline. Skipping it without an open order avoids that, but leaves
   the context without payment methods.
4. **Second payment while one is in progress.** For example a Google Pay action still pending while Drop-in opens. Reject
   the second submit as a visible failure, or document it as unsupported.
5. **When a context ends.** When its session completes, or when the merchant drops it.
6. **`CheckoutContext` no longer `Parcelable`.** Merchants who parcel it today break. Acceptable in the alpha.
7. **Several contexts in one `SavedStateHandle`.** The extensions use one SDK-owned key, so one view model can save one
   context. An optional `key` parameter would allow more.
8. **Integrations without a `SavedStateHandle`.** A `SavedStateHandle` comes from a view model, so Activity-only
   integrations and the Flutter and React Native wrappers need a view model on the native side, or the base functions
   exposed later. Exposing them later is an addition.
9. **Lambda or value for the new session.** The lambda (`setup(configuration, savedState) { fetchSessionResponse() }`)
   avoids creating a session that isn't used. A `setup(sessionResponse, configuration, savedState)` overload is simpler
   to read, but makes the merchant call their server's `/sessions` even when a saved session continues. Offer only the
   lambda, or both.
10. **Setup taking the `SavedStateHandle` directly.** `setup(configuration, savedStateHandle) { ... }` would be a single
    call that also registers saving, but would make every setup depend on `SavedStateHandle`. Keeping `restore` separate
    keeps setup independent of where the state comes from.

### To verify with the backend

- Is `/sessions/{id}/setup` allowed while an action is pending?
- Does setup with an order accept a `sessionData` that is older than the latest? If an older `sessionData` is rejected
  or harmless, the revision merge could be simpler.
- Does setup with an order return the remaining amount, or does it have to be saved?

### Follow-up actions

- **Breaking change.** Needs explicit agreement, and a migration note for anyone already on the alpha.
- **Await timeout is silent.** When `StatusRepository.poll` reaches its limit, it emits
  `Result.failure("Max polling time exceeded.")` and cancels. `AwaitComponent.onStatus` only logs failures
  (`// TODO - Throw exception?`), so the shopper keeps seeing the waiting screen. A restore after the deadline would hit
  this right away. Fix before making await restorable.
- **Redirect return before the controller exists.** The docs and the example app must show holding the return until the
  controller exists. The example app's `V6SessionsViewModel.onNewIntent` currently drops a return when
  `checkoutController` is null.
- **Public API alignment.** Run the API alignment check on the final shape, and share the differences in the alignment
  table with the iOS team.

## Details

### What is saved

The internal `saveStateString()` returns null when there is nothing to continue. The `SavedStateHandle` extension then
stores nothing.

| Situation | Contents |
|---|---|
| `Input`, no order | Nothing |
| `Input`, order open (later) | Session ID, `sessionData`, order (`pspReference`, `orderData`, maybe `remainingAmount`) |
| `Submitted` | Session ID, `sessionData`, phase marker (plus the order, if open) |
| `HandlingAction` | Session ID, `sessionData`, target, action, checkout attempt ID, component state (plus the order, if open) |
| Action-only, `HandlingAction` | Action, checkout attempt ID, component state |

Also saved when needed: the session's `shopperLocale`, if the configuration has none.

Not saved, because it comes from the `CheckoutConfiguration` the merchant passes to `Checkout.setup`: environment,
client key, locale, analytics level and component configurations. Not saved because it is fetched again: payment methods,
public key.

A payment can't return to `Input` after an action: in `FullCheckoutFlow`, an action ends in `onComplete` or
`onFailure`. So a restored payment only needs to finish its action, and needs no payment component. `CheckoutPaymentMethod`
already renders nothing without one. A payment restored in `Submitted` reports "outcome unknown", as
`FullCheckoutFlow.failWithUnknownOutcome` already does.

`sessionData` changing in `Input` without an order doesn't matter: the merchant can create a new session. The
`onBeforeSubmit` patch happens right before `/payments`, so it is already saved under `Submitted`.

Shape, about 3 to 6 KB while a payment is in progress:

```json
{
  "version": 1,
  "session": { "id": "...", "sessionData": "...", "order": null, "revision": 4 },
  "payment": {
    "target": "...",
    "phase": "HandlingAction",
    "action": { "...": "..." },
    "checkoutAttemptId": "...",
    "actionComponent": { "startedAt": 1762959600000 },
    "paymentComponent": null
  }
}
```

### How state is collected

State is pulled from its owner at save time, like Android's `SavedStateRegistry` providers, and not pushed into a store
whenever it changes.

- **Components** (payment and action) get an internal contract: `saveState(): JSONObject?`. Their factory receives that
  state again on restore.
- **The flow** of the payment in progress combines its phase, action and component state.
- **The context** adds the session state and returns one versioned string.

### How a controller updates the context

A controller doesn't update the context through public API. `CheckoutControllerFactory` gives the controller's internals
two internal handles that the context owns.

**Session state, written on every response.** Today `SessionComponentRequestDispatcher` writes `sessionData` to the
global `DefaultSessionStateStore`. Instead, it writes to the context's session state, one instance per session ID,
shared by every context of that session:

```kotlin
internal class SessionState(initial: SessionSnapshot) {
    private val _snapshot = MutableStateFlow(initial)
    val snapshot: StateFlow<SessionSnapshot> = _snapshot

    fun update(transform: (SessionSnapshot) -> SessionSnapshot) {
        _snapshot.update { current -> transform(current).copy(revision = current.revision + 1) }
    }
}

// In SessionComponentRequestDispatcher, instead of sessionStateStore.setSessionData(...)
sessionState.update { it.copy(sessionData = response.sessionData) }
```

This is pushed right away, because the next call needs it and can come from another controller or from Drop-in. The
context's public `paymentMethods`, `storedPaymentMethods` and `amount` are mapped from the snapshot.

**The payment in progress, claimed and pulled.** The context has one slot:

```kotlin
internal class PaymentSlot {
    private var owner: PaymentOwner? = null           // the flow of the payment in progress
    private var restored: RestoredPayment? = null     // set by setup with a saved state, until its controller takes it

    fun claim(owner: PaymentOwner): Boolean           // false while another payment is in progress
    fun release(owner: PaymentOwner)                  // no-op unless owner is the current one
    fun takeRestored(target: CheckoutTarget): RestoredPayment?  // null for any other target, or once taken
    fun saveState(): JSONObject? = owner?.saveState() ?: restored?.toJson()
}
```

| Moment | Flow | Slot |
|---|---|---|
| Controller created | `takeRestored(target)`. A match resumes in the saved phase and becomes the owner, anything else starts fresh | Restored payment taken, or unchanged |
| Submit | `claim(this)`. If that fails, the payment fails visibly | Owner set |
| Action, polling, waiting for a return | Keeps its own phase, action and component state | Unchanged |
| Completion, failure, `SubmitResult.Retry` | `release(this)` | Empty |
| Controller's `coroutineScope` cancelled | `release` through `coroutineContext.job.invokeOnCompletion` | Empty |
| Android saves state | `saveState()` combines phase, action and the components' `saveState()` | Asked |

- In `Input` nothing has claimed the slot, so nothing is saved.
- A finished payment can't clear a newer one, because `release` only acts for the current owner.
- Until its controller is created, the restored payment is saved unchanged, so a second process death loses nothing.
- `saveState()` runs on the main thread. Flow and component state are only changed on the main thread, where the
  components already run, so no locks are needed.

### Restoring an action

1. `Checkout.restore` reads the string from the handle. `Checkout.setup` with that state calls `/sessions/{id}/setup`
   if needed, and attaches the payment in progress to the context.
2. `CheckoutController(target, context, ...)` for that target takes it, once, and builds the flow in the saved phase.
3. `ActionHandler.restoreAction(action)` → `ActionComponentProvider.get(action, ...)` → `component.restoreAction(state)`.
4. The merchant passes the redirect return to the resumed controller with `controller.handleReturn(intent)`.

Per component:

- **Redirect:** `initState()` takes `paymentData` or `nativeRedirectData` from the action and waits for `handleReturn`.
- **Await:** polls `action.paymentData` without opening the URL again, for the rest of `DEFAULT_MAX_POLLING_DURATION`.
- **3DS2:** `restoreAction()` reports "cannot be resumed", because the 3DS2 SDK only keeps the transaction in memory.

### Rule for components

A component can only restore what it can rebuild from the `Action`, the `CheckoutConfiguration` and its own saved state.
Anything it only builds up in memory is lost on process death. Component state must stay small and must not contain card
data.

### Time-based state (await)

Save a start time, not a remaining time: Android saves at an uncontrolled moment, so a remaining time would already be
outdated.

```kotlin
override fun restoreAction(state: JSONObject?) {
    val paymentData = action.paymentData ?: return emitError(GenericError("Payment data should not be null"))
    val startedAt = state?.optLong(STARTED_AT) ?: clock.now()
    // Clamped, so that a changed device clock can't give a negative or longer duration
    val remaining = (DEFAULT_MAX_POLLING_DURATION - (clock.now() - startedAt)).coerceIn(0, DEFAULT_MAX_POLLING_DURATION)
    startStatusPolling(paymentData, remaining)
}
```

- Use wall-clock time behind an injectable clock. `SystemClock.elapsedRealtime()` resets on reboot, which would break
  the state if the base functions are ever exposed for storing it on disk.
- `StatusRepository.poll` sends a request straight away and only checks the time limit after the first response, so a
  restore after the deadline still gets one final status.
- Polling restarts with the fast delay. That is harmless.

### Session state shared between components and Drop-in

Use case: a merchant shows a Google Pay button and a "more payment methods" button that opens Drop-in. The shopper opens
Drop-in, cancels, and pays with Google Pay.

Session state is shared, the payment in progress is owned:

| Shared per session | Why |
|---|---|
| Latest `sessionData` | Every call needs the newest value |
| Open order and remaining amount (later) | A partial payment in Drop-in changes what Google Pay charges |
| Changes to the payment methods | Drop-in can remove a stored payment method |
| Completed | A session that Drop-in finished can't be paid again |
| Revision | Decides which copy is newest when merging |

How it stays in sync:

- **In the process:** every live context is a view on one store keyed by session ID, like `DefaultSessionStateStore`
  today, extended to the rows above.
- **Durable copies:** the merchant's saved state (`context.saveState(savedStateHandle)`), Drop-in's launch intent and its
  own saved state, and Drop-in's result, which carries the session state internally. `DropInResultContract.parseResult`
  runs in the merchant's process and merges it.
- **Merging:** whenever a copy enters the store (launch, restore, result), the higher revision wins.
- **Cleanup:** state is only removed when a session completes or expires (`expiresAt`), never when the last context goes
  away. Otherwise a merchant could restore an outdated copy after Drop-in finished.

Scenario with process death while Drop-in is open:

1. The merchant's activity saved revision 1 when Drop-in opened. Drop-in saves revision 2 when the app goes to the
   background.
2. The new process recreates Drop-in first, which puts revision 2 in the store.
3. The shopper cancels. The merchant's activity is recreated and setup with its saved state brings revision 1. The store
   keeps 2.
4. The Drop-in result brings revision 2 as well. Google Pay continues with the latest state.

### Drop-in

Drop-in sustains itself from its launch intent and its own saved state, without depending on objects in the merchant's
process.

- The live context has an internal `Parcelable` data part: setup response with payment methods, configuration, checkout
  attempt ID and public key. This is what `DropInResultContract.Input` carries today.
- `DropIn.start(launcher, context)` puts the data part and the current session state in the launch intent. The merchant
  API stays the same.
- `DropInViewModel` builds its own live context from the intent, and saves it in its handle with the same
  `context.saveState(savedStateHandle)`, next to the back stack.
- After process death, the data comes from the intent and the session state from the handle, so the context is rebuilt
  without a network call. With an open order, Drop-in calls setup itself and shows a loading state.
- The internal function that builds a context from data and saved state is `@RestrictTo(LIBRARY_GROUP)`.
- Controllers live in `DropInViewModel` (the planned payment flow registry). That is where the payment in progress is
  pulled from.
- `DropInService` stays, as the place for merchant hooks in Drop-in, starting with `onBeforeSubmit` (see the TODO in
  `DropInControllerProvider`). Android can start it again after process death.

Rejected: handing the live context to Drop-in in memory, because Drop-in would depend on the merchant's process objects.
Also rejected: Drop-in calling setup itself from a `SessionResponse`, because merchants could no longer inspect the
payment methods before launching Drop-in, and setup would run twice.

### Partial payments (later)

- The SDK handles the order internally. After a partial payment, the session calls setup again with the order and the
  context updates its payment methods and amount. iOS does the same in `Session.performSubmit`.
- No public order type. iOS made `PartialPaymentOrder` internal in #2828.
- The payment methods and amount are accessors on the live context, so they can start changing without an API change.
- Partial payments can start in Drop-in only, with a visible error for components, as iOS does with
  `PartialPaymentError.notSupportedForComponent`. Supporting components later is an addition.
- A partial payment callback, if any, goes in the payment method's callbacks block (gift card, meal voucher). A partial
  payment without a handler fails visibly through `onFailure`, checked when it happens (ADR-0005).
- Result types are not sealed, or allow new subtypes, so a partial payment variant doesn't break exhaustive `when`s.

### Internal changes

- `CheckoutContext` becomes live, with an internal `Parcelable` data part. `DropInResultContract.Input` carries the data
  part and the session state.
- `DefaultSessionStateStore` becomes the shared session state: `sessionData`, order, payment method changes, completed,
  revision. Its TODOs about memory-only state and never clearing sessions are resolved.
- `CheckoutControllerFactory` takes the payment in progress from a context set up with a saved state, once, and builds
  the flow in its phase. `FullCheckoutFlow` and `ActionOnlyCheckoutFlow` get their phase from it, instead of reading
  `CheckoutFlowStateStore` when they are created.
- The `CheckoutSavedState` keeps a reference to the handle it was read from, so that a successful setup can remove the
  entry.
- `CheckoutFlowStateStore` no longer writes to a `SavedStateHandle`.
- `ActionComponentProvider.get` and `ActionFactory.create` lose their `savedStateHandle` parameter. `AwaitComponent`
  reads `paymentData` from the action, and `PaymentDataRepository` is removed from v6.
- Every action component implements `RestorableActionComponent`. The check in `ActionHandler.restoreAction` for
  components that don't is removed.
- Payment and action components get an internal `saveState(): JSONObject?` contract.

### Convenience layers considered

Both could sit on top of the proposed solution later. Neither replaces it.

- **`SavedStateRegistryOwner` and a key.** `SavedStateHandle` uses this underneath, and Activities, Fragments and nav
  back stack entries implement it, so it would remove the `ViewModel` requirement. Still Android-only, and a plain
  `FlutterActivity` doesn't implement it.
- **Compose: `rememberSaveable` with a `Saver`.** Saved state is dropped when the composable leaves the composition,
  which fits "the shopper goes back mid-payment". The controller's coroutine scope usually belongs in a `ViewModel`, so
  this only suits simpler integrations.
