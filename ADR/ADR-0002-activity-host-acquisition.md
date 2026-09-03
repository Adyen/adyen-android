# Activity host acquisition for components

| Field         | Value        |
|---------------|--------------|
| Author(s)     | Oscar Spruit |
| Status        | Accepted     |
| Creation date | 2026-09-01   |

## Abstract

Some payment and action components need an Android `Activity` to do their work: launching Custom Tabs, registering an
`ActivityResultLauncher`, or handing an `Activity` to a third-party SDK. In v6 these components obtain that `Activity`
from the Compose tree, inside their `Content` composable. This ADR evaluates four ways of acquiring an activity host and
proposes keeping the current composition-based acquisition, while making its failure mode explicit. Adopting SDK-owned
activities is proposed per payment method, only when a concrete case requires it, rather than as an SDK-wide
architecture. Letting merchants hide the contents of our composables was considered and is deferred, pending a separate
decision on whether every action must always show UI.

## Motivation

Five capabilities in the SDK cannot be served by an application `Context` alone:

| Capability                  | Consumers                     | Requires                                       |
|-----------------------------|-------------------------------|------------------------------------------------|
| Launching Custom Tabs       | redirect, await, QR code      | `Activity` for correct task and theming        |
| `ActivityResultLauncher`    | Google Pay                    | `ActivityResultRegistry`                       |
| Third-party SDK entry point | 3DS2 challenge, Twint         | `Activity`, sometimes before `STARTED`         |
| Deep link return            | redirect, WeChat Pay          | an activity with a matching `<intent-filter>`  |
| Result callback class name  | WeChat Pay                    | a known `Activity` class name                  |

Today each of these is satisfied from inside composition. `RedirectComponent` sends a `RedirectViewEvent` through a
buffered channel that is only collected by `redirectEvent(..)` inside its `Content` composable. `GooglePayComponent`
does the same with `GooglePayViewEvent` and `GooglePayContent`, which owns the
`rememberLauncherForActivityResult(TaskResultContracts.GetPaymentDataResult())`. `AuthenticationComponent` reads
`LocalActivity.current` in `AuthenticationEventEffect`.

This works, but it has consequences worth recording:

- **The contract is implicit.** An action only progresses if the merchant renders our composable in response to
  `CheckoutRoute.Action`. Because the channels are buffered the event is not lost, but if the merchant never renders it,
  the action never runs and the SDK reports no error. The shopper sees a screen where nothing happens.
- **Components cannot be tested without Compose.** The launch path only exists in a composable, so covering it needs a
  Compose or Robolectric test rather than a plain JUnit 5 test.
- **Three bespoke channel bridges** exist purely to move a request from a non-composable caller into composition.
- **Some capabilities have no composition at all.** A redirect renders nothing, so `Content` exists only as a side-effect
  host.
- **Nothing outside composition can read the theme.** `CheckoutTheme` is a composable parameter, which is why
  `CustomTabsLauncher` still carries a `TODO` for Custom Tabs style customization.

Two upcoming pieces of work make the question concrete rather than theoretical: migrating Twint to v6, whose SDK must be
constructed before its host reaches `STARTED`, and giving WeChat Pay a `callbackClassName` that does not borrow the
merchant's activity class.

## Solutions

### A. Composition-based acquisition (current)

Components acquire the host from the Compose tree inside `Content`, bridging non-composable callers through buffered
channels.

**Pros:**

- Already implemented and working for every supported payment method.
- Requires nothing from the merchant beyond rendering the composables they already render.
- Naturally guarantees the app is in the foreground when a third-party UI is launched, because composition implies a
  resumed window.
- No process-global mutable state, no extra activities, no manifest entries.
- Correct by construction for anything that has real UI. The Google Pay button, the 3DS2 challenge screen, await, QR code
  and voucher screens must be composed before they can be interacted with, so the host is guaranteed to be present.

**Cons:**

- Silent stall if the merchant does not render the composable for an action.
- Requires a Compose test to cover a launch path.
- Buffered-channel bridges are boilerplate that exists only to cross the composition boundary.
- `Content` for a redirect renders nothing, which makes the composable's purpose non-obvious.
- No host, and therefore no theme, available outside composition.

### B. Explicit host binding by the merchant

The merchant passes the host to the SDK, for example `CheckoutController.bind(activity)` or a
`rememberCheckoutController(..)` composable.

**Pros:**

- The requirement is explicit and documented in the type system.
- Registration can happen before `STARTED`, satisfying every third-party SDK constraint.
- No process-global state.

**Cons:**

- A `CheckoutController` may legitimately be created in a `ViewModel`, where no `Activity` is available. Forcing the
  merchant to plumb one in is poor developer experience.
- Inconsistent with the rest of v6: `Checkout.setup(..)` and the `CheckoutController(..)` factories deliberately take no
  `Context`.
- Moves the failure from silent to a crash, when trying to launch an unattached launcher.
- Adds public API surface for something the SDK can determine itself.

### C. Implicit host acquisition via an activity tracker

A process-wide `Application.ActivityLifecycleCallbacks` tracker, registered from `CheckoutCoreInitializer` next to
`ApplicationContextHolder`, weakly holds the currently resumed activity. Components ask the tracker for a host when they
need one.

**Pros:**

- No public API and no merchant obligation.
- Works from a `ViewModel`, matching how a `CheckoutController` is actually created.
- Decouples the launch path from composition, so components become unit-testable.
- Gating acquisition on `RESUMED` preserves the foreground guarantee that composition currently provides implicitly.

**Cons:**

- Process-global mutable state holding an `Activity`. A step up in risk from `ApplicationContextHolder`, which holds an
  application `Context`.
- Ambiguous in multi-window or when several activities are resumed.
- Fails for a host that is not a `ComponentActivity`.
- Needs a reset hook for tests, and careful weak referencing to avoid leaks.
- Does not by itself solve deep link return or `callbackClassName`.

### D. SDK-owned activities

Every capability that needs an `Activity` is moved inside an activity the SDK declares and owns. The five capabilities
collapse into one: start our activity. Results return either through an `ActivityResultContract` or through a
process-global bridge.

**Pros:**

- Removes the pre-`STARTED` problem entirely. An activity we own registers in its own `onCreate`, which is trivially
  before `STARTED`. This is the only option that solves Twint without a headless fragment.
- The SDK can own the redirect `<intent-filter>`, so merchants no longer declare one, override `onNewIntent`, or call
  `CheckoutController.handleReturn(..)`.
- Gives WeChat Pay a `callbackClassName` we control.
- An activity context is always available, so Custom Tabs theming and task placement are correct.
- Industry-proven.

**Cons:**

- Four or five new activities, each with manifest entries, argument and result types, and its own process-death handling.
- Arguments must be `Parcelable`, or a process-global holder is needed to carry non-parcelable values. Google Pay's
  contract input is a `Task<PaymentData>`, which is not `Parcelable`.
- If the SDK owns the redirect `<intent-filter>`, we will need a default `returnUrl`. This opens up the same issues
  as we are currently having with drop-in.

### Cross-cutting: capability seam

Independently of A to D, components could depend on narrow capability interfaces such as `UrlLauncher` or
`ActivityResultRegistrar` rather than on a host type or on composition.

**Pros:**

- Components become unit-testable with fakes, no Compose or Robolectric needed.
- The acquisition strategy becomes swappable. Moving from A to C or D later changes one class instead of every payment
  method.
- Removes the three buffered-channel bridges.

**Cons:**

- New internal abstraction to learn.
- No merchant-visible benefit on its own.

## Proposed solution

Keep **A**, with two additions, and explicitly do not adopt **D** as an SDK-wide architecture.

1. **Make the failure explicit.** When an action needs a host and none appears, log an error and emit a `CheckoutError`
   instead of stalling indefinitely. Simplicity that fails loudly is acceptable; simplicity that hangs is not.
2. **Introduce the capability seam opportunistically**, when a component is being modified for another reason. This is
   cheap insurance: it buys testability now and reduces a future move to C or D to a single-class change.

A third addition was considered, letting merchants render our composable with its contents hidden so they can draw their
own UI in its place. It is deferred, see the follow-up actions below.

The reasoning:

- The current design is correct by construction for every capability that has real UI, which is most of the surface.
  Google Pay is the clearest example: `submit()` can only be reached by clicking a button rendered inside `Content`, so
  the host is guaranteed present.
- Redirect is the only case where `Content` renders nothing, and the buffered channel means it still works whenever the
  merchant follows the documented flow. The residual risk is the failure mode, which addition 1 fixes directly.
- Most integrations use drop-in, where `DropInActivity` always provides a host. The problem is largely limited to
  standalone components.
- D's cost is concentrated entirely in the SDK.
- **D is decomposable.** If Twint's v6 migration needs an activity, it gets one activity in the Twint module. If
  merchants ask for a redirect without manifest changes, redirect gets a trampoline. Neither requires a unified host
  abstraction, so there is no cliff that forces adopting all of D at once.
- D has no merchant-visible benefit over A for Google Pay. A merchant can already supply their own button today:
  `GooglePayViewStateProducer` returns a null button view state when `showSubmitButton` is false, so `GooglePayContent`
  draws nothing while still hosting the launcher, and `controller.submit()` keeps working.

## Final decision

**Accepted, with additions 1 and 2.** The team agreed that A remains the right trade-off: it is already implemented,
requires nothing from the merchant beyond rendering the composables they already render, and is correct by construction
for every capability that has real UI. D is not adopted SDK-wide, but stays available per payment method if a concrete
case demands it.

**Addition 3 is out of scope for now.** The team is still deciding whether every action should always show our UI. Until
that is settled no flag is added. The analysis is kept in the details below so the follow-up does not have to start from
scratch.

## Concerns and follow-up actions

- **Twint's v6 migration is the most likely trigger for revisiting D.** The v5 implementation relies on
  `Twint(this, ::onTwintResult)` being a field initializer of `TwintActionFragment`, which works because the pre-`STARTED`
  constraint is relative to the fragment's own lifecycle. If v6 has no place for a fragment, Twint needs either a
  headless fragment of its own or a Twint-owned activity.
- **WeChat Pay's `callbackClassName`** should point at an SDK-declared activity rather than the merchant's class. This is
  a manifest concern and can be solved locally without adopting D.
- **Custom Tabs theming remains unresolved.** `CustomTabsLauncher` carries a `TODO` and currently applies no colors. Any
  fix needs the theme to be reachable outside composition, which today it is not.
- **Process death during an action is unhandled** in every option. `ActionHandler` constructs components with a throwaway
  `SavedStateHandle` and a `TODO`. Component state has to become restorable before any host strategy can survive process
  death, so this is a prerequisite rather than an alternative.
- **Reopen D if** support data shows merchants misconfiguring the intent filter or `handleReturn`, merchants ask for a
  redirect without manifest changes, or non-Compose v6 integrations become a goal, which would remove the composition
  host entirely.
- **Addition 3, hiding composable contents, needs a decision and is the main follow-up.** The open question is whether
  every action must always show our UI. If the answer is no, the mechanism worked out in the details below applies, and
  it is scoped to `CheckoutAction` only: payment methods do not need it. Three sub-questions to settle at the same time:
  which actions may hide their UI, given that QR code and voucher must not; whether a flag that is a no-op for redirect
  and 3DS2 is worth exposing at all; and whether the theme must keep being applied to a hidden composable, since
  `AuthenticationEventEffect` derives the 3DS2 `UiCustomization` from `CheckoutThemeProvider`.
- **Addition 3 would not remove the host contract.** Merchants would still have to render the composable, hidden or not.
  Addition 1 is what catches those who do not, so it stands on its own and does not depend on the deferred decision.

## Details

### Why Twint's constraint is weaker than it appears

`Fragment.registerForActivityResult` throws only when the fragment's own state is past `CREATED`. `TwintActionFragment`
constructs `Twint(this, ::onTwintResult)` in a field initializer, so the constraint is satisfied relative to the
fragment, not to the merchant's activity. A freshly instantiated fragment therefore provides a new registration window at
any moment, which is why v5 works without the merchant binding anything.

### Two mechanisms for starting an SDK-owned activity

Should D be adopted for a specific payment method, the two mechanisms are not interchangeable:

| Need                                          | Mechanism                                   | Why                                                                            |
|-----------------------------------------------|---------------------------------------------|--------------------------------------------------------------------------------|
| Contract-based result (Google Pay)            | `ActivityResultRegistry` from a host         | The contract already exists and its input is not `Parcelable`                   |
| Deep link return (redirect, WeChat Pay)       | SDK-owned activity plus a result bridge      | The return arrives via `<intent-filter>`, not `onActivityResult`                |
| Third-party SDK needing an `Activity` (Twint) | SDK-owned activity                           | A fresh activity is trivially before `STARTED`                                  |

`ActivityResultRegistry` has two `register` overloads. The one without a `LifecycleOwner` has no lifecycle restriction,
so it can be called outside composition, at the cost of manual unregistration.

### Hiding composable contents

This section records the analysis for addition 3. It is **deferred, not accepted**, and is kept so the follow-up does not
have to start from scratch.

**Scope: only `CheckoutAction` would need a flag.** Payment methods do not. Google Pay, the one payment method that
looked like it needed one, is already covered by `showSubmitButton`, and no other payment method has a reason to hide the
UI the shopper has to interact with.

**The flag cannot mean "do not compose `Content`".** For two of the three v6 actions, `Content` is nothing but effects:
`RedirectComponent.Content` only calls `redirectEvent(..)`, and `AuthenticationComponent.Content` only calls
`AuthenticationEventEffect(..)`. Skipping them means the redirect never launches and the 3DS2 challenge never starts.
`AwaitComponent` is the only action that mixes both, calling `redirectEvent(..)` and then `AwaitContent(modifier)`.

The mechanism that fits is therefore to pass the flag into `Content` and let each component decide what it means:

```kotlin
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
interface ActionComponent : EventComponent<ActionComponentEvent> {
    @Composable
    fun Content(modifier: Modifier, showContent: Boolean)

    fun handleAction()
}
```

| Component                 | Behaviour                                                                       |
|---------------------------|---------------------------------------------------------------------------------|
| `RedirectComponent`       | Ignores the flag. No visuals to hide, and the effect must always run.            |
| `AuthenticationComponent` | Ignores the flag, for the same reason.                                           |
| `AwaitComponent`          | Always runs `redirectEvent(..)`; draws `AwaitContent(modifier)` only when set.    |
| QR code, voucher          | Ignore the flag. Their UI is how the shopper pays, so it must not be hideable.    |

Putting the decision inside each component enforces the QR code and voucher rule structurally, rather than through an
exclusion list in `CheckoutAction` that someone has to remember to update.

Note that this leaves the flag affecting exactly one component today, `AwaitComponent`. For redirect and 3DS2 a merchant
can already render their own UI, because our `Content` draws nothing for those actions. Whether that is worth a public
flag is part of the deferred decision.

An earlier draft proposed splitting every component into an always-composed `Effects()` and a conditionally-composed
`Content(modifier)`. That is more invasive than the scope above needs and has been dropped. If launch paths ever need to
be testable without Compose, that is addition 2's job.

Three shapes were considered for the public API:

- **A parameter on the existing composable**, `CheckoutAction(controller, showContent = false)`. One API, no risk of a
  merchant rendering both a visible and a hidden variant, and effects are guaranteed to run. The drawback is a composable
  named after what it draws that can be configured to draw nothing, and a `modifier` that becomes meaningless when
  contents are hidden.
- **A separate effects-only composable**, for example `CheckoutActionEffects(controller)`. The intent is clearer at the
  call site, but there are two APIs to keep in sync and a merchant could render both, registering twice.
- **A custom modifier**, for example `Modifier.hideCheckoutContent()`, measuring the content but never placing it.
  Tempting because composition is independent of placement, so the effects would keep running without splitting
  `Content` at all. Rejected because it depends on every component threading its `modifier` through to a single root,
  which `RedirectComponent.Content` does not do today, so it would silently do nothing for the component that motivates
  this the most. It is also not truly hidden without `clearAndSetSemantics`, leaving content in the accessibility tree
  and its fields focusable, it is undiscoverable from the composable signature, it pollutes `Modifier` across the
  merchant's whole app, and it uses a modifier as a behaviour switch, which is contrary to Compose API conventions.

**Preferred shape, should addition 3 be adopted: the parameter, named `showContent`.** It matches the requirement as
stated, render our composable and hide its contents, and keeps the behaviour switch where behaviour switches belong.

For reference, a Google Pay integration with a merchant-supplied button already works today and needs none of the above:

```kotlin
// With showSubmitButton = false our composable draws nothing, but still hosts the Google Pay launcher
CheckoutPaymentMethod(controller)
MyOwnGooglePayButton(onClick = { controller.submit() })
```

### Existing precedents in this repository

Both halves of D already exist in the codebase and can be reused if a specific case needs them:

- `SharedChallengeStatusHandler` in the `3ds2` module is a process-global singleton with a queued-result handoff,
  bridging the 3DS2 SDK's challenge result back into the delegate.
- `DropInRedirectHandlingActivity` is a trampoline activity, and `drop-in`'s `AndroidManifest.xml` declares the
  `${checkoutRedirectScheme}://${applicationId}` intent filter using the placeholder defined in `drop-in/build.gradle`
  from `rootProject.ext.checkoutRedirectScheme`.
