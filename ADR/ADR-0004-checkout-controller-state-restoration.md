# Restoring a CheckoutController after process death

| Field         | Value        |
|---------------|--------------|
| Author(s)     | Oscar Spruit |
| Status        | In progress  |
| Creation date | 2026-10-07   |

## Abstract

A `CheckoutController` must survive process death: a shopper who is sent to a bank app or browser for a redirect, or
who waits for an await action, can return to an app that Android killed in the meantime. The first steps are merged:
the controller requires a `SavedStateHandle`, saves the phase of its flow, guards against a double payment, and
restores actions. While reviewing the redirect support, we found that restoring is triggered by every new controller
for the same payment method on the same handle, not only after process death. The SDK cannot tell whether the
merchant creates that controller to continue the payment or to start a new one.

This ADR compares the ways of deciding when a flow is restored, grouped into automatic and explicit solutions. It
proposes making restoring explicit with two functions as the base: `controller.saveState()` returns the state of the
payment in progress as a `String`, and `CheckoutController.restore(state, callbacks, coroutineScope)` creates a
controller from it after process death. The merchant stores the state wherever their host saves state, so the same
API works from a view model, an Activity, Compose, the cross-platform wrappers and Drop-in, and the SDK has nothing to
guess. Convenience helpers, such as an SDK-managed `SavedStateHandle` integration or a composable, can be built on top
later. This removes the `savedStateHandle` parameter merged earlier, which is a breaking change to the alpha API.

## Motivation

### What is merged

| PR    | Change                                                                                                                                                                                              |
|-------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| #3098 | `CheckoutController(...)` requires a `savedStateHandle`. Breaking change in the v6 alpha.                                                                                                           |
| #3099 | The flow saves its phase (`Input`, `Submitted`, `HandlingAction(action)`) per payment method. A flow that is restored in `Submitted` reports "payment outcome unknown" instead of submitting again. |
| #3102 | A flow that is restored in `HandlingAction` rebuilds its action component without launching it again (`RestorableActionComponent`). Components that cannot resume fail once and clear the state.    |
| #3103 | 3DS2 drops its restoration remnants. The 3DS2 SDK cannot resume a challenge after process death.                                                                                                    |

Open: #3107 makes redirect actions resumable. #3111 is a candidate fix for the problem below (solution B).

### The problem: the SDK has to guess the merchant's intent

Restoring currently happens inside the flow, when the controller is created. So any controller for the same payment
method on the same `SavedStateHandle` restores the saved phase, including in the same process. Two situations look
identical to the SDK:

- **Continue the payment.** The view model was rebuilt after process death, or the merchant rebuilt the controller
  for another reason.
- **Start a new payment.** The shopper left a redirect without finishing it, and the merchant creates a new controller
  to retry, for example after switching payment methods and back.

On `main`, the retry reports "The 'redirect' action cannot be resumed" once. With #3107, the retry restores the
abandoned redirect and waits for a return that never comes, with submitting blocked: the form is frozen until the old
redirect completes or the view model is cleared.

### Facts that constrain the solution

- **"Don't keep activities" is not process death.** It destroys activities but keeps the process alive, so
  singletons, statics and objects held by other SDKs survive. Android never destroys a single activity to free memory;
  it kills the process. Real process death is tested with `adb shell am kill <package>` while the app is in the
  background, or with the "No background processes" developer option.
- **3DS2 cannot be resumed.** The 3DS2 SDK keeps its transaction in memory only. When `ChallengeActivity` is recreated
  in a new process, it compares the saved process id and finishes without delivering a result.
- **The return intent can arrive before the controller exists.** A merchant who fetches payment methods again after
  process death creates the controller asynchronously, while the redirect return is delivered immediately. The SDK
  does not buffer returns (D7 in the implementation plan), so the merchant must hold on to the intent.
- **Sessions need the original session.** A context fetched again is a new session, but `/details` must go to the
  session the payment was made in.
- **iOS does not restore anything.** The iOS SDK has no persistence. A pending redirect lives in a process-wide
  `RedirectListener` that holds one handler, and the redirect that was launched last receives the return. When iOS
  terminates the app, the return is dropped. While the app is running, a new component is always a new flow.
- **Drop-in needs a single owner for its flows first.** Its controllers live in screen view models. On an action, the
  owning screen is popped, so after process death the owner no longer exists. The payment flow registry plan moves all
  flows to `DropInViewModel`. Drop-in always has the `CheckoutContext` synchronously (from its launching intent) and
  knows when it restores (`DropInNavigator.didRestoreState`).

### Requirements

1. Never submit a payment twice because of a restore.
2. Resume redirect and await actions after process death, including repeated process death during one payment.
3. A new payment after an abandoned flow must start fresh, and not freeze.
4. Do not save card data. Keep the saved state small.
5. Work with several controllers per view model (for example a card form and a Google Pay button).
6. Support Drop-in and the Flutter and React Native wrappers.
7. Keep public API alignment with iOS where possible, and call out Android-only API explicitly.
8. When a merchant integrates incorrectly, the failure should be visible rather than silent.

## Solutions

Solutions A to E restore automatically and differ in how the SDK decides what counts as a restore. Solutions F to K
let the merchant state the intent.

### A. Restore on every controller creation (current `main`)

Every controller restores the saved phase for its payment method and handle.

**Pros:**

- No merchant code needed to resume.
- Already merged.

**Cons:**

- Fails requirement 3: a retry restores the abandoned flow (a failure on `main`, a frozen form with #3107).

### B. Restore once per handle instance (#3111)

A process-wide, weakly held registry remembers the payment methods restored per `SavedStateHandle` instance. A handle
rebuilt from saved state (process death, or a recreated view model) is a new instance, so the first flow on it
restores. A later flow for the same payment method on the same instance clears the state and starts fresh.

**Pros:**

- No merchant code needed to resume, and no public API change.
- Covers repeated process death: the registry dies with the process, and the phase stays in the handle.
- Matches the iOS convention while the app is running: a new controller is a new flow.

**Cons:**

- Still a guess. It fails silently if the merchant creates the controller outside the view model, for example in
  `Activity.onCreate`: a rotation then creates a second controller on the same handle, which clears a pending action.
- A second controller created while the first one's payment request is in flight clears the `Submitted` marker, which
  weakens requirement 1 if the process then dies.

### C. Automatic restore, ended by cancelling the controller's `coroutineScope`

The flow lives as long as the scope passed to the controller. Cancelling the scope clears the saved state.

**Pros:**

- The merchant expresses "this flow is over" with an existing parameter. No signature change.
- Matches the Drop-in registry plan, which already ends a flow by cancelling its child scope.

**Cons:**

- A scope is shared, passed on and cancelled for unrelated reasons. Treating cancellation as "end the payment" is a
  hidden side effect.
- Merchants who use `viewModelScope` for every controller must switch to a scope per controller.
- If the merchant forgets, retries still restore the abandoned flow.

### D. Restore only across a process restart (process token)

The phase is saved with a token that is unique per process. A phase written in the current process is not restored.

**Pros:**

- Small and local, no public API change.

**Cons:**

- Still a guess, with the same misuse case as B.
- A view model recreated in the same process no longer restores.

### E. Unique payment method IDs

Replace the payment method type in the saved state key with a unique ID per payment method entry (planned separately).

**Pros:**

- Separates two controllers for different entries of the same type, for example two cards.

**Cons:**

- Does not solve the problem: a retry creates a controller for the same entry, so it has the same ID. An ID that
  changes per controller would break restoring after process death, unless the merchant keeps the ID.

### F. Explicit end: `controller.cancel()`

Automatic restore stays. The merchant calls `cancel()` to end a flow, which clears its saved state.

**Pros:**

- The merchant states the intent explicitly.

**Cons:**

- New public API, and if the merchant forgets to call it, retries still freeze.

### G. Explicit resume on an existing controller: `controller.restore(): Boolean`

The SDK saves the payment method and phase in the handle, but does not restore by itself. The merchant creates the
controller as usual and calls `restore()` in the view model's creation path. The return value says whether an action
is pending.

**Pros:**

- No guessing: only `restore()` resumes, any other controller starts fresh.
- Additive public API.
- The return value tells the merchant to show `CheckoutAction(...)`, so they no longer keep their own flag.

**Cons:**

- The merchant must keep the `CheckoutContext` and the selected payment method to rebuild the same controller.
- The early return problem remains for merchants who fetch again.

### H. SDK rebuilds the controller: `CheckoutController.restore(savedStateHandle, callbacks, scope)`

Like G, but the SDK also saves the `CheckoutContext` and the payment method, and rebuilds the controller itself.

**Pros:**

- Least merchant code: one call in `init`.
- Solves the early return problem: the controller exists synchronously.

**Cons:**

- The SDK saves the whole `CheckoutContext`, including the payment methods response. Large, and the merchant's data.
  Rejected for that reason.

### I. Merchant passes the context: `CheckoutController.restore(context, callbacks, scope, savedStateHandle)`

The SDK saves only what it creates: the payment method, the phase and the session values (ID, data, result). The
merchant passes a context, fetched again or kept by themselves. `restore(...)` resolves the saved payment method in that
context and returns the rebuilt controller and its payment method, or `null`. One payment in progress per view model is
saved in a single slot, which a new submit overwrites and completion clears. Creating a controller does not touch it.

**Pros:**

- No guessing, and the SDK does not save the `CheckoutContext`.
- Additive public API. Keeps the merged `savedStateHandle` parameter.
- Sessions work with a context fetched again, because the restored controller uses the saved session values.
- Works with several controllers: the merchant builds the others fresh from the same context.
- Fits Drop-in well: it always has the context synchronously, and the registry has a defined place to call it.

**Cons:**

- The early return problem remains for merchants who fetch again: they must hold on to the return intent until
  `restore(...)` has run.
- A flow abandoned before any new submit stays restorable until it completes. For a redirect that is arguably correct,
  because the payment is still pending at the bank.

### J. Merchant-owned state: `controller.saveState()` and `CheckoutController.restore(state, callbacks, scope)`

The controller produces a `String` with everything it needs to continue: its resolved payment method, the
configuration, the checkout attempt ID, the public key, the session values and the phase. The merchant stores it
wherever they want, and creates the controller from it after process death. The `savedStateHandle` parameter is
removed. A `String` follows Braintree's pending request and PayPal's `instanceState` (see [Prior art](#prior-art)).

```kotlin
fun CheckoutController.saveState(): String?

fun CheckoutController.Companion.restore(
    state: String,
    callbacks: AdvancedCheckoutCallbacks, // plus the sessions and action-only overloads
    coroutineScope: CoroutineScope,
): CheckoutController?
```

Two rules keep it explicit:

- **Restoring is creating.** There is no `restoreState()` on an existing controller, which would need rules for being
  called after `handleReturn`, after a submit, twice, or with a `target` or `context` that does not match the state.
  `restore(...)` needs no context, because the state is self-contained, so the controller is rebuilt synchronously,
  before any return arrives.
- **`null` means "nothing to resume".** `saveState()` returns `null` when no payment is in progress, so the merchant
  can save every controller without thinking about which one is paying. `restore(...)` returns `null` for a state it
  cannot use: an unknown version after an SDK update, an expired state, or an action that cannot resume (3DS2). The
  merchant then starts fresh.

The save hook is the one each host already has for saving state at the right moment:

| Host | Save | Restore |
|---|---|---|
| View model | `savedStateHandle.setSavedStateProvider(KEY) { bundleOf(STATE to controller.saveState()) }` | In `init`, from `savedStateHandle.get<Bundle>(KEY)` |
| Activity | `onSaveInstanceState(outState)` | In `onCreate`, from `savedInstanceState` |
| Compose | A `rememberSaveable` `Saver` that calls `controller.saveState()` | When the saveable value is restored |
| Flutter, React Native | The `String` can be passed to the Dart or JS layer | From the Dart or JS layer |
| Drop-in | A saved state provider for the action flow, in `DropInViewModel`'s handle | When the action screen is restored without a flow |

**The host decides what survives a configuration change.** The API restores what was saved, but it cannot keep the live
controller alive. In a view model, or in Compose with `retain`, the controller survives a rotation. When the controller
lives in an Activity or in plain `remember`, a rotation rebuilds it from the saved state, so it behaves like process
death: a payment request in flight ends as "payment outcome unknown", a 3DS2 challenge fails, and await polling starts
over. A redirect is not affected. The documentation must tell merchants to host the controller in something that
survives configuration changes.

**Pros:**

- Most explicit: a controller only resumes if the merchant created it from a state they saved, and a late or repeated
  restore is impossible.
- One concept for every host: view model, Activity, Compose, the wrappers and Drop-in.
- Solves the early return problem: the controller is rebuilt synchronously, with nothing to fetch.
- Sessions work, because the session values are inside the state.
- Small: only this controller's payment method, not the whole payment methods response. No card data.
- Works with several controllers: each one saves its own state, and only the one with a payment in progress returns
  a value.
- Explicit results for "nothing to resume", like PayPal's `null` and Braintree's `NoResult`.
- Helpers can be added later on top of the same two functions, for example a `SavedStateHandle` extension or a Compose
  `Saver`, without adding a second concept. K is such a helper.
- The pattern the closest payment SDKs moved to in their latest major versions: Braintree Android v5 and the PayPal
  Android SDK.

**Cons:**

- Breaking change again: removes the `savedStateHandle` parameter merged in #3098.
- The merchant has to add the save hook. Forgetting it means nothing is restored. Calling `saveState()` outside a save
  hook, for example right after `onAction`, can produce a stale state.
- A host that does not survive configuration changes turns every rotation into a restore, with the consequences above.
- A snapshot can be stale if the payment finishes in the background after Android saved state. Every solution has this
  limitation, because Android only persists state at save time.
- If a merchant stores the state on disk, they take on versioning across SDK updates. `restore(...)` returns `null`
  for a version it does not know.

### K. SDK-saved state, explicit restore: `CheckoutController.restore(savedStateHandle, callbacks, scope)`

The state of J (this controller's resolved payment method, the configuration, the checkout attempt ID, the public key,
the session values and the phase), but saved by the SDK instead of the merchant. The controller registers it with
`savedStateHandle.setSavedStateProvider(...)` while a payment is in progress (`Submitted` or `HandlingAction`), so
Android pulls it at the moment it saves state. The merchant restores explicitly, in the view model's creation path:

```kotlin
val controller = CheckoutController.restore(savedStateHandle, callbacks, viewModelScope)
```

`restore(...)` rebuilds the controller synchronously from the saved state, or returns `null` when no payment was in
progress. It consumes the saved state, so a second call, or a call later in the same process, returns `null`. Any
other controller starts fresh.

**One saved payment per handle.** The saved state belongs to the payment that was started last on the handle (submitted,
or launched as an action-only flow), until it completes or fails:

- Starting a payment registers the controller as the provider. Android allows one provider per key, so the payment
  started last replaces the previous one.
- Completing or failing removes the provider, but only if the controller is still the current owner, so an older
  payment finishing never clears a newer one.
- Creating a controller, or showing an idle one, does not touch the saved state.

**Pros:**

- No guessing: only `restore(...)` resumes, and the rule for what is saved does not depend on why a controller was
  created.
- Least room for merchant mistakes: the merchant passes the handle, as today, and makes one call in `init`. The SDK
  decides when to save, so the moment cannot be wrong. Forgetting `restore(...)` gives a fresh form, which is visible.
- Solves the early return problem: the controller is rebuilt synchronously, with nothing to fetch.
- Sessions work, because the session values are inside the state.
- The merchant no longer keeps a "show action" flag: a non-null result means a payment is in progress.
- Not a breaking change: keeps the merged `savedStateHandle` parameter and adds `restore(...)`.
- No per-payment-method keys and no registry: the state carries its own payment method, so several controllers of the
  same type are no problem.
- Fits on top of J: it uses the same state, so it can be offered as a helper built on `saveState()` and `restore(...)`.

**Cons:**

- The SDK saves this controller's payment method and configuration. Not the whole `CheckoutContext` (see H), but more
  than only the phase.
- Tied to `SavedStateHandle`. Storing the state elsewhere needs the later `saveState()` addition.
- Only one payment in progress per view model is saved. If two payments run at once, the older one loses its guard
  against a double payment when the process dies. Starting two payments at once is not a supported flow.
- The registered provider keeps the controller that owns the state in memory until a newer payment takes over or it
  finishes. In practice those controllers already live on `viewModelScope` until the view model is cleared.

### Comparison

| Criterion                              | A   | B         | C         | D         | F         | G     | I         | J              | K |
|----------------------------------------|-----|-----------|-----------|-----------|-----------|-------|-----------|----------------|---|
| SDK guesses the intent                 | Yes | Yes       | No        | Yes       | No        | No    | No        | No             | No |
| Retry after abandoned flow (req. 3)    | ✗   | ✓         | If cancelled | ✓      | If cancelled | ✓  | ✓         | ✓              | ✓ |
| Resume after process death (req. 2)    | ✓   | ✓         | ✓         | ✓         | ✓         | Merchant calls | Merchant calls | Merchant restores | Merchant restores |
| Merchant code to resume                | None | None     | Scope per controller | None | `cancel()` | Keep context and payment method, call `restore()` | Call `restore(context, ...)` | A save hook and `restore(state, ...)` | Call `restore(...)` in `init` |
| Early return (D7)                      | Merchant | Merchant | Merchant | Merchant | Merchant | Merchant | Merchant when fetching again | Solved | Solved |
| Sessions with a new context            | With Phase 6 | With Phase 6 | With Phase 6 | With Phase 6 | With Phase 6 | With Phase 6 | ✓ | ✓          | ✓ |
| Misuse is visible (req. 8)             | No  | No        | No        | No        | No        | Yes   | Yes       | Mostly         | Yes |
| Public API change                      | None | None     | KDoc      | None      | Additive  | Additive | Additive | Breaking      | Additive |
| Saves the `CheckoutContext`            | No  | No        | No        | No        | No        | No    | No        | Partly (own payment method only) | Partly (own payment method only) |

E and H are left out: E does not solve the problem, and H was rejected because it saves the whole context. K saves
only the subset of J's state that belongs to the controller with the payment in progress. "With
Phase 6" means the planned change that saves the session values in the handle, which then also has to include the
session ID.

## Proposed solution

**J** is proposed as the base: `controller.saveState(): String?` and
`CheckoutController.restore(state, callbacks, coroutineScope): CheckoutController?`.

The two most important criteria are ease of integration (merchants cannot easily make mistakes) and an explicit SDK
(no assumptions, and no edge cases beyond what it promises):

- **Explicit:** a controller only resumes if the merchant created it from a state they saved. Restoring is creating,
  so a late, repeated or mismatching restore is impossible, and `null` explicitly means "nothing to resume".
- **The same concept everywhere:** one pair of functions for a view model, an Activity, Compose, the Flutter and React
  Native wrappers, and Drop-in. Each host saves the `String` with the hook it already has for saving state.
- **No hidden timing rules:** the controller is rebuilt synchronously from a self-contained state, so the early return
  problem and sessions are solved without extra rules.
- **A proper base:** the convenience of K, or of a `rememberCheckoutController` composable, can be built on top of
  these two functions later, without introducing a second concept. The other way around, K would tie the base to
  `SavedStateHandle`.
- **Prior art:** the payment SDKs closest to this problem moved to the same pattern in their latest major versions:
  Braintree Android v5 and the PayPal Android SDK (see [Prior art](#prior-art)).

Its costs are accepted and handled by documentation:

- It is a breaking change to the alpha API: the `savedStateHandle` parameter merged in #3098 is removed.
- The merchant adds a save hook. The documentation shows the one-line hook per host.
- The controller must live in something that survives configuration changes (a view model, or `retain` in Compose).
  Otherwise every rotation is a restore.

The alternatives:

- **K (`restore(savedStateHandle, ...)`)** leaves the least room for merchant mistakes and is additive, but ties
  restoring to `SavedStateHandle`. It is the first candidate for a helper on top of J.
- **I (`restore(context, ...)`)** is additive and saves the least (payment method, phase and session values), but leaves
  the early return to merchants who fetch the context again.

I, J and K share the same internal model: a flow state of payment method, phase and session values. They only differ in
who stores it and what the merchant passes in. Drop-in can use J internally: after the payment flow registry, it saves
the action flow's state in `DropInViewModel`'s handle and restores it when the action screen is restored without a flow.

With J:

- #3111 (solution B) is closed. Its first commit, which simulates process death in tests with a rebuilt handle, is
  kept.
- #3107 is unchanged: the redirect component does not care who decides to restore.
- The flows stop restoring when they are created. Only `restore(...)` restores.
- The flows keep their phase in memory, and `saveState()` takes a snapshot. `CheckoutFlowStateStore` and its
  per-payment-method keys (#3099) no longer write to a `SavedStateHandle`.
- The `savedStateHandle` parameter is removed from the `CheckoutController(...)` functions, and from the internal
  action component plumbing (Phase 5b of the implementation plan).

## Final decision

Pending discussion.

## Concerns and follow-up actions

- **Breaking change (J).** Needs explicit agreement, and a migration note for anyone already on the alpha.
- **When to save (J).** Document the save hook per host (`setSavedStateProvider`, `onSaveInstanceState`, a
  `rememberSaveable` `Saver`).
- **Helpers on top of J.** Decide which helpers to offer and when: K (an SDK-managed `SavedStateHandle` integration),
  and a `rememberCheckoutController` composable built on `rememberSaveable` and `retain`.
- **Host lifetime (J).** Document that the controller must be hosted in something that survives configuration changes
  (a view model, or `retain` in Compose), otherwise every rotation is a restore.
- **3DS2 on restore (J).** Decide whether `restore(...)` returns `null` for a 3DS2 state, or a controller that reports
  the failure through `onFailure`.
- **If K is offered as a helper.** Document that only the payment started last is saved per handle, and that its
  `restore(...)` consumes the saved state.
- **Early return (I).** Document that merchants who fetch again must hold on to the return intent. A follow-up could
  add a static `Checkout.handleReturn(intent)` that holds a return until a controller is restored, in line with iOS's
  `Checkout.handleReturn(url:)`. That is new public API and needs an alignment check.
- **Public API alignment.** Both explicit solutions add Android-only API, because iOS has nothing to restore. Run the
  API alignment check on the chosen shape, and tell the iOS team that iOS drops a redirect return after termination.
- **Drop-in.** Depends on the payment flow registry. Once Drop-in restores, the registry plan's D3 (finish with
  `Failed` when the action flow is missing) only applies to actions that cannot resume, like 3DS2.
- **Wrappers.** Flutter and React Native host several components in one view model, and their Dart or JS layer restarts
  after process death. Each wrapper decides how much it supports.
- **When to clear the saved state.** The open question from the implementation plan (D8): which failures end a flow.
  Firebase Auth only returns a pending result once and only when it is recent, to prevent false positives. An expiry
  on the saved state would answer part of this question.
- **Result for "nothing to resume" (J).** `saveState()` and `restore(...)` return `null` (see J). Still to decide: how
  a restored controller reports that the shopper came back without finishing, like Braintree's `NoResult`.
- **Native redirect.** Found during review: v6 sends `supportNativeRedirect = true` but only registers a factory for
  `redirect`, so a `nativeRedirect` action fails as unsupported. Separate issue.

## Details

### Prior art

How other SDKs that send the user to a browser or another app handle process death, grouped by pattern.

| Pattern | SDK | How it works | Matches |
|---|---|---|---|
| SDK restores automatically, keyed by where the object is created | AndroidX Activity Result API | Pending results are saved in the registry's saved state, and delivered to whoever registers again with the same key before `STARTED`. In Compose the key comes from the position in composition. | A, B |
| | Stripe `PaymentSheet.FlowController`, `EmbeddedPaymentElement` | Managed internally with the SDK's own activities and `SavedStateHandle`. Merchants must create the object in `onCreate`, or call `rememberEmbeddedPaymentElement` "unconditionally as part of the initialization path". The `CreateIntentCallback` is stored statically to survive process death. | A, B |
| | Adyen v5 | Components live in a view model keyed by the activity or fragment, with an optional `key` on every provider for several instances. Created in `onCreate`, restored automatically. | A, B |
| SDK saves, the app asks for a pending result | Firebase Auth | After a Custom Tab sign-in interrupted by process death, the app calls `getPendingAuthResult()` on start. It returns a result only once, and only for recent sign-ins "to help prevent false positive sign-ins". | G, I |
| App owns a state and passes it back | Braintree Android v5 | `PayPalLauncher.launch()` returns `PayPalPendingRequest.Started(pendingRequestString)`, which the merchant must persist. On return the merchant calls `handleReturnToApp(pendingRequest, intent)`, which returns `Success`, `NoResult` or `Failure`. Replaced v4's SDK-managed flow. | J |
| | PayPal Android SDK | Moved from `finishStart(intent, authState)` to a `client.instanceState` property and `client.restore(instanceState: String)`. For process death: persist `instanceState` and call `restore()` on recreation. `finishStart(intent)` returns `null` when there is no matching flow in this process. | J |
| | AppAuth-Android | `AuthState` serializes to JSON (`jsonSerializeString()`), and the app stores it wherever it wants. | J |
| | Stripe `EmbeddedPaymentElement` | Exposes a `Parcelable` `state` to configure an element instantly from another activity's state. Not for process death, but the same shape. | J |
| No restore, reconcile with the server | Google Play Billing | `onPurchasesUpdated` is only delivered while the app runs. The app calls `queryPurchasesAsync()` when it connects or comes to the foreground; the server is the source of truth. | Baseline |
| | Adyen iOS | A return after termination is dropped; the merchant checks the payment status on their server. | Baseline |

Observations:

- **Automatic restoring needs control over where the object is created.** The AndroidX API, Stripe and Adyen v5 all
  require creation in a fixed place, unconditionally. v6 lets merchants create controllers in their own view model, so
  A and B would need the same rules without being able to enforce them. Stripe's history of process death fixes shows
  the maintenance cost ([#4841](https://github.com/stripe/stripe-android/pull/4841),
  [#7771](https://github.com/stripe/stripe-android/pull/7771), [#8668](https://github.com/stripe/stripe-android/pull/8668)).
- **The closest payment SDKs moved to merchant-owned state.** Braintree (v5) and PayPal both replaced SDK-managed state
  for their redirect and app switch flows with a state the merchant keeps and passes back.
- **They use a `String`.** It does not depend on where it is stored, so it works with a `Bundle`, disk, or a wrapper's
  Dart or JS side.
- **They distinguish "nothing to resume" from "came back without finishing".** PayPal returns `null` for the first and
  Braintree returns `NoResult` for the second.
- **The state and the return are handed over together.** Braintree's `handleReturnToApp(pendingRequest, intent)` gets
  both at once, so the order in which they arrive does not matter. In J, rebuilding synchronously in `init` gives the
  same guarantee.

Sources: [Braintree v5 migration guide](https://developer.paypal.com/braintree/docs/guides/client-sdk/migration/android/v5/),
[Braintree `PayPalPendingRequest`](https://braintree.github.io/braintree_android/PayPal/com.braintreepayments.api.paypal/-pay-pal-pending-request/index.html),
[PayPal Android troubleshooting](https://developer.paypal.com/sdk/android/troubleshooting),
[PayPal Android releases](https://github.com/paypal/paypal-android/releases),
[Firebase `getPendingAuthResult`](https://firebase.google.com/docs/reference/android/com/google/firebase/auth/FirebaseAuth),
[AppAuth-Android README](https://github.com/openid/AppAuth-Android/blob/master/README.md),
[Stripe `EmbeddedPaymentElement`](https://stripe.dev/stripe-android/paymentsheet/com.stripe.android.paymentelement/-embedded-payment-element/index.html),
[Play Billing integration](https://developer.android.com/google/play/billing/integrate).

### Scenarios used to evaluate the solutions

| Scenario | A | B | I | J | K |
|---|---|---|---|---|---|
| Process death during a redirect, then return through the deep link | Resumes | Resumes | Resumes if the controller is restored before the return | Resumes | Resumes |
| Repeated process death during an await action | Resumes each time | Resumes each time | Resumes each time | Resumes each time, if saved each time | Resumes each time |
| Retry with the same payment method after leaving a redirect | Fails once (`main`), frozen form (#3107) | Fresh | Fresh | Fresh | Fresh |
| Controller created in `Activity.onCreate`, then a rotation | Resumes | Pending action cleared silently | Fresh, visible | Fresh, visible | Fresh, visible |
| Second controller while the first is waiting for `/payments` | "Outcome unknown" failure | Fresh, `Submitted` marker cleared | Fresh, marker kept | Fresh, first controller's state unaffected | Fresh; only the newer payment is saved once it is submitted |
| Card form and Google Pay button in one view model | Works | Works | Works, others built fresh | Works, each saved separately | Works, the one in progress is saved |

### Sketches

Solution I:

```kotlin
init {
    viewModelScope.launch {
        val context = setUpCheckout() // fetch and Checkout.setup(...), or a context the merchant kept
        val restored = CheckoutController.restore(context, callbacks, viewModelScope, savedStateHandle)
        controller = restored?.controller ?: createController(context, defaultTarget)
        showAction = restored != null
    }
}
```

Solution J, from a view model:

```kotlin
init {
    val savedState = savedStateHandle.get<Bundle>(CHECKOUT_KEY)?.getString(STATE_KEY)
    val restored = savedState?.let { CheckoutController.restore(it, callbacks, viewModelScope) }
    if (restored != null) {
        controller = restored
        showAction = true
    } else {
        viewModelScope.launch { setUpCheckout() }
    }
    savedStateHandle.setSavedStateProvider(CHECKOUT_KEY) { bundleOf(STATE_KEY to controller?.saveState()) }
}
```

Solution J, from an Activity (a rotation is a restore here, see J):

```kotlin
override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    controller = savedInstanceState?.getString(STATE_KEY)?.let { CheckoutController.restore(it, callbacks, lifecycleScope) }
        ?: CheckoutController(target, context, callbacks, lifecycleScope)
}

override fun onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)
    outState.putString(STATE_KEY, controller.saveState())
}
```

Solution J, from Compose:

```kotlin
val controller = retain { /* restore from the saveable state, or create a new controller */ }
rememberSaveable(saver = Saver(save = { controller.saveState() }, restore = { it })) { null }
```

Solution K:

```kotlin
init {
    val restored = CheckoutController.restore(savedStateHandle, callbacks, viewModelScope)
    if (restored != null) {
        controller = restored
        showAction = true
    } else {
        viewModelScope.launch { setUpCheckout() } // creates controllers with savedStateHandle, as today
    }
}
```

### Several controllers (K)

| Situation | What is saved | After process death |
|---|---|---|
| Card form and Google Pay button, the shopper pays with card (redirect) | Card | `restore(...)` returns the card controller. The merchant builds Google Pay fresh from their context later. |
| Two controllers of the same payment method type | The one that started the payment | No key is needed: the state contains its own resolved payment method. |
| The shopper abandons a card redirect, then pays with Google Pay | Google Pay (started last) | Google Pay resumes. The abandoned card flow is no longer saved. |
| The shopper abandons a card redirect, retries the card, but has not submitted yet | Still the first card payment | That redirect resumes. It is still pending at the bank, and the merchant can choose not to call `restore(...)`. The retry takes over once it submits. |
| Controllers in different view models | One state per handle | Independent of each other. |
| Drop-in (flows owned by the registry, one handle) | The flow in the action slot | Fits the registry's "one action at a time" rule. |

### Drop-in fit

| | B | I | J | K |
|---|---|---|---|---|
| Where state is stored | A handle per screen view model | `DropInViewModel`'s handle, by the SDK | `DropInViewModel`'s handle, through a saved state provider for the action flow | `DropInViewModel`'s handle, by the SDK, for the flow in the action slot |
| When it restores | Implicitly, when a controller is created | When the action screen is restored and its slot is empty | Same moment | When the action screen is restored and its slot is empty |
| Context | Available | Synchronously from the launching intent | Inside the state (Drop-in does not need it) | Inside the state |
| Without the registry | Relies on the nav3 store behavior the registry plan rejects | The owning screen is gone | The owning screen is gone | The owning screen is gone |
