---
name: android-migrate-payment-method-v6
description: Plan and execute the migration of a v5 payment method to the v6 component architecture.
---

# android-migrate-payment-method-v6

Migrate a v5 payment method to the v6 architecture, using the Google Pay migration as the canonical reference. This skill produces an ordered, test-driven execution plan and drives the migration phase by phase, with small commits and stacked PRs.

## Usage

Invoke this skill when migrating an existing v5 payment method (e.g. `ideal`, `sepa`, `twint`) to v6. The input is the payment method / module name. The skill is **method-agnostic**: every payment method differs (Google Pay, for instance, has no input fields and hands off to an external SDK), so treat the steps as a checklist to adapt, not a rigid template.

> Throughout this skill, `X` stands for the payment method (e.g. `Ideal`), and `x`/`module` for its module.

## The v6 target architecture

Each v6 payment method is a small set of collaborators wired by a factory. No single module is a complete reference — use all three migrated methods: **Google Pay** (external SDK, no input fields), **MBWay** (input fields + secondary screen, no params/mapper), and **Card** (input fields + stored variant):

| Layer | Type (reference) | Responsibility |
|-------|------------------|----------------|
| Core details | `XDetails : PaymentMethodDetails` (`core`) | Serializable `paymentMethod` body for `/payments`. **Must be registered** in `PaymentMethodDetails.getChildSerializer` and have a `PaymentMethodTypes` constant. |
| Params *(optional)* | `XComponentParams` + `XComponentParamsMapper` | Map `CheckoutParams` + `PaymentMethod` + config → typed params. Only when the method derives config; simple methods pass `CheckoutParams` values straight through (MBWay has no params/mapper). |
| State | `XComponentState`, `XComponentStateFactory`, `XIntent` (sealed), `XComponentStateReducer`, `XComponentStateValidator` | Immutable state + intent-driven reducer + validity. |
| State → payment | `XPaymentComponentState`, `toPaymentComponentState()` ext | Map component state → `PaymentComponentData<XDetails>`. |
| View state | `XViewState` + `XViewStateProducer` | Map component state → UI model. |
| View | `XContent` (Compose) + pure-UI sub-composables | `XContent` holds effects/flow collection; a private composable holds pure UI + `@Preview`. |
| Component | `XComponent : PaymentComponent` | Owns `ComponentStateFlow`, derives `viewState`, exposes `eventFlow`, `Content()`, `submit()`, `setLoading()`. |
| Factory | `XFactory : PaymentComponentFactory<XComponent>` | Builds the component from a `PaymentMethod`. Also implements `StoredPaymentComponentFactory<StoredXComponent>` when the method supports stored payments (see Card). |
| Registration | `XInitializer : Initializer<Unit>` + module `AndroidManifest.xml` | Registers the factory via `PaymentMethodProvider.register(txVariant, factory)` for each supported type. |
| Public API | `XConfiguration` + `CheckoutConfiguration.x { }` DSL | The only public surface; everything else is `internal`. |
| Stored variant *(if supported)* | `StoredXComponent` + parallel stored state/view-state/reducer/validator/intent + `StoredXContent` | Separate stack for stored payments, built by the same factory (see Card). |
| Secondary screen *(optional)* | `SecondaryScreenComponent` → `SecondaryContent(identifier, modifier)` + `PaymentComponentEvent.SecondaryScreen`/`CloseSecondaryScreen` | For pickers/bottom sheets, e.g. MBWay's country-code picker. |

## Before you start

1. **Read `AGENTS.md`.** This skill defers to it for visibility/API rules, sealed-vs-abstract, styles/strings, external-SDK handling, and TDD.
2. **Create a plan document first.** Per `AGENTS.md`, write `<METHOD>_V6_MIGRATION_PLAN.md`, get it approved, and do not start coding until then. Keep it updated as phases complete. Do not commit the plan file.
3. **Inventory the v5 sources.** List the existing v5 files (delegate, views, provider, configuration, tests) and map each onto the v6 collaborators above. Note what already exists (some methods are partially migrated — e.g. Google Pay's `XDetails` already existed) so you don't recreate it.
4. **Flag method-specific concerns** in the plan: external SDK handoff (`compileOnly` + `runCompileOnly`/`checkCompileOnly` + ProGuard `dontwarn`), availability pre-checks, action/redirect handling, and any bridging of out-of-composition events into the Composable (see Google Pay's `viewEventChannel`). Also decide which **optional capabilities** apply: a params/mapper, **input fields** (validation + error display), a **stored variant**, and a **secondary screen** (pickers/sheets).
5. **Invoke `architecture-guardian`** when a phase introduces or changes public API, new abstractions, or module boundaries.
6. **Agree the commit/PR cadence with the developer.** Commits stay small (one per phase), but PRs need **not** map 1:1 to commits. Decide upfront whether to open a PR per phase or — the default — per cohesive **group** of phases (e.g. `old/` move; state + view-state + view; component + factory + registration). Cadence is a reviewer preference, so confirm it during planning.

## Working rhythm (applies to every step)

Follow this loop for each phase below:

1. **Tests first.** Write/move the tests for the layer before (or alongside) the implementation, using the given-when-then style. Every phase that adds a class adds its unit tests; the v5 tests for any code you move go with it.
2. **Implement** the layer, defaulting to `internal` visibility.
3. **Make it green**, then run the `android-check` skill scoped to the touched module(s) (`./gradlew :<module>:check`, plus `:core:check` when core changed).
4. **Commit** that single phase via the `android-commit` skill (one logical change per commit, `COSDK-XXXX` ticket). Never bundle multiple phases.

## Steps

### 1. Branch (and chain)

Use the `android-branch-create` skill to create a `chore/` branch (base `main` during v6). For a multi-phase migration, **chain branches** and open a **stacked draft PR** per branch so reviewers can review incrementally, at the cadence agreed with the developer (see *Before you start* — default is one branch/PR per cohesive group of phases, not per commit). Keep the same prefix and extend the name (e.g. `chore/v6-ideal-state`, `chore/v6-ideal-view`).

### 2. Preserve the v5 implementation (`old/` package)

- **Do this before adding any v6 code**, so v5 keeps working in parallel and all new v6 code lands in the clean namespace. Move the existing v5 delegate/views/provider/configuration into an `old/` package. **Move the matching v5 tests** into the corresponding `old/` test packages in the same commit.
- **Gate:** `:module:check`. Commit.

### 3. Core payment details + serializer registration

- Add `XDetails : PaymentMethodDetails` in `core` **if it does not already exist**.
- Add/confirm the `PaymentMethodTypes` constant(s), then **register the type(s)** in `PaymentMethodDetails.getChildSerializer` → `XDetails.SERIALIZER`. Skipping this causes a runtime `ClassCastException` (it falls back to `GenericDetails`).
- **Tests:** add a serializer test that serializes `XDetails` through `PaymentMethodDetails.SERIALIZER` for every supported type, asserting the fields survive (this is the exact regression the fallback causes).
- **Gate:** `:core:test`; `:core:apiCheck` (`XDetails` is public — run `:core:apiDump` only if the API change is intentional and commit the `.api` file). Commit.

### 4. Params + mapper *(optional)*

- **Skip this phase if the method needs no derived config** — pass the required `CheckoutParams` values (e.g. `shopperLocale`) straight to the state factory, as MBWay does.
- Otherwise add `XComponentParams` and `XComponentParamsMapper` (`CheckoutParams` + `PaymentMethod` + config → params).
- **Tests:** mapper unit tests covering defaults and overrides.
- **Gate:** `:module:test`. Commit.

### 5. State layer

- Add `XComponentState`, `XComponentStateFactory` (initial state), `XIntent` (sealed updates), `XComponentStateReducer` (intent → state), `XComponentStateValidator`, `XPaymentComponentState`, and the `toPaymentComponentState()` extension.
- **For methods with input fields:** model each field with the shared `TextInputViewState` (value + focus + error), add update/focus intents, and a `HighlightValidationErrors` intent that `submit()` dispatches when the state is invalid (see MBWay/Card).
- **Tests:** reducer (per intent), validator (valid/invalid), and the state→payment mapping.
- **Gate:** `:module:test`. Commit.

### 6. View-state layer

- Add `XViewState` and `XViewStateProducer` (component state → UI model); map validation results to localized error text using the shopper locale.
- **Tests:** producer tests for each meaningful state → view-state mapping.
- **Gate:** `:module:test`. Commit.

### 7. View layer (Compose)

- Add `XContent`: a wrapper that collects the view-state flow and hosts effects/launchers, delegating to a **private pure-UI composable**. **Reuse shared composables from the `ui` module** (`ComponentScaffold`, `PayButton`, input fields, `ValuePickerField`) instead of building from scratch.
- **If the method has a secondary screen**, add its `SecondaryContent` composable (e.g. `XSecondaryContent`) to render the component's `SecondaryScreenComponent.SecondaryContent()` slot (see MBWay's picker).
- **Add `@Preview` composables for the meaningful UI cases**, not just one happy path — e.g. default/empty, loading, validation error, available vs unavailable, and any method-specific variants (light/dark via `uiMode`, RTL, different styles). Previews take the `ViewState` (or a small UI model) directly so each case is rendered in isolation.
- Follow `AGENTS.md` and other payment methods for styles and strings.
- **Tests:** logic/UI tests where applicable.
- **Gate:** `:module:check`. Commit.

### 8. Component + factory + registration

- Add `XComponent : PaymentComponent` wiring `ComponentStateFlow(initialState, reducer, validator)`, `viewState(producer)`, `eventFlow`, `Content()`, `submit()`, `setLoading()`, `requiresUserInteraction()`. For input methods, `submit()` validates first and dispatches `HighlightValidationErrors` when invalid instead of emitting `Submit` (see MBWay).
- **If the method needs a secondary screen**, also implement `SecondaryScreenComponent` (`SecondaryContent()`) and emit `PaymentComponentEvent.SecondaryScreen`/`CloseSecondaryScreen` to open/close it.
- **Wire analytics** via `AnalyticsManager`/`GenericEvents` (submit and error events, render where applicable and other analytics events which were already firing on v5).
- Add `XFactory : PaymentComponentFactory<XComponent>`.
- Add `XInitializer : Initializer<Unit>` (`@Keep`) that registers the factory via `PaymentMethodProvider.register(txVariant, factory)` for each supported type, and wire it into the module `AndroidManifest.xml` under the androidx-startup `InitializationProvider`.
- **Tests:** component tests like it is done for other components, loading transitions, validity/availability, secondary-screen events, and error handling.
- **Gate:** `:module:check`; `:module:apiCheck`. Commit.

### 9. Stored payment method variant *(if supported)*

- If the method supports stored payments, add a parallel stored stack: `StoredXComponent`, `StoredXComponentState` + `StoredXComponentStateFactory`/`Reducer`/`Validator`, `StoredXIntent`, `StoredXViewState` + `StoredXViewStateProducer`, and `StoredXContent`.
- Make the **single** `XFactory` implement **both** `PaymentComponentFactory<XComponent>` and `StoredPaymentComponentFactory<StoredXComponent>` (two `create()` overloads). `PaymentMethodProvider.register` registers it under both the regular and stored maps automatically — no extra registration needed (see `CardFactory`).
- If necessary create the UI for the stored variant.
- **Tests:** stored component tests (e.g. security-code/CVC input where applicable, submit, loading).
- **Gate:** `:module:check`; `:module:apiCheck`. Commit.

### 10. Public configuration / DSL

- Add/confirm the public `XConfiguration` and the `CheckoutConfiguration.x { }` DSL extension. Everything else stays `internal` or `@RestrictTo(LIBRARY_GROUP)`. Prefer abstract classes over sealed for merchant-facing `when` safety (see `AGENTS.md`).
- **Gate:** if the public API changed intentionally, run `:module:apiDump` and commit the `.api` files. Commit.

### 11. Example-app wiring

- Add the method to the v6 example flow: the supported-types list and the `CheckoutConfiguration.x { }` config, plus a host Activity/screen if needed.
- Verify on a device/emulator (real payment path).
- Commit.

### 12. Final verification & PR

- Run `android-check` for the module and `:core` (compile, lint, unit tests, `apiCheck`).
- Open or finalize the draft PR(s) via the `android-pr-create` skill, following the cadence agreed during planning (earlier phase groups may already have open PRs). Use a checklist covering: serializer registration, tests per layer, v5 preserved under `old/`, public API reviewed, styles/strings, stored variant + secondary screen (if applicable), and example wiring.

## Important

- **Plan first, code after approval.** No implementation before the plan document is approved.
- **Tests are part of every step** — created for new layers, moved with relocated v5 code. Never weaken or delete tests to make a phase pass.
- **Small commits; PRs at an agreed cadence.** One logical change per commit via `android-commit`. PRs do **not** have to be per commit — group cohesive phases into stacked draft PRs (`android-branch-create` + `android-pr-create`), and agree the per-phase vs per-group cadence with the developer during planning.
- **Don't skip serializer registration** in `PaymentMethodDetails.getChildSerializer`.
- **Default to `internal`.** Only the configuration/DSL is public. Discuss any breaking change before proceeding.
- **Adapt per method.** No single reference is complete: Google Pay (external SDK, no fields), MBWay (input + secondary screen, no params/mapper), Card (input + stored). Confirm which collaborators and optional capabilities — params/mapper, input/validation, stored variant, secondary screen — your method actually needs rather than copying all of them.
