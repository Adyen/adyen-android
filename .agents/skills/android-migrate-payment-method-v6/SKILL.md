---
name: android-migrate-payment-method-v6
description: Plan and execute the migration of a v5 payment method to the v6 component architecture. Use when moving an existing payment method module to v6.
---

# android-migrate-payment-method-v6

Migrate a v5 payment method to the v6 architecture. This skill gives the order of work and the rules that are easy to miss. The shape of the code comes from the payment methods that are already migrated, which are always more current than any description here — read them rather than relying on this file for names or signatures.

## Usage

Invoke this skill when migrating an existing v5 payment method (e.g. `ideal`, `sepa`, `twint`) to v6. The input is the payment method / module name. The skill is **method-agnostic**: every payment method differs, so treat the steps as a checklist to adapt, not a rigid template.

> Throughout this skill, `X` stands for the payment method (e.g. `Ideal`), and `x`/`module` for its module.

## Find your references

No single migrated method covers every capability, and the set of migrated methods keeps growing. Find them before planning: every v6 payment method registers its factory with `PaymentMethodProvider.register` from an initializer, so searching for that call lists them all.

For each capability your method needs — no input fields, input fields, a stored variant, a secondary screen, an external SDK handoff — pick the closest migrated method and read it end to end, tests included. Match what it does rather than inventing a second way of doing the same thing.

For methods with input fields, also read the form state ADR under `ADR/`. It records how forms are modelled and why; the migrated form components show how it is done today.

## The v6 target architecture

Each v6 payment method is a small set of collaborators wired by a factory:

| Layer | Naming | Responsibility |
|-------|--------|----------------|
| Core details | `XDetails` (`core`) | The serializable `paymentMethod` body for `/payments`. |
| Params *(optional)* | `XComponentParams` + `XComponentParamsMapper` | Config derived from checkout params, the payment method and the merchant configuration. Skip it when values can be passed straight through. |
| State | `XComponentState`, `XIntent` and their factory, reducer, validator and post processor | Immutable state, changed only through intents. |
| State → payment | `XPaymentComponentState` | Maps component state to the payment request data. |
| View state | `XViewState` + `XViewStateProducer` | Maps component state to what the UI renders. |
| View | `XContent` (Compose) | Effects and flow collection in `XContent`, pure UI in private composables with previews. |
| Component | `XComponent` | Owns the state flow and exposes the method to Checkout. |
| Factory | `XFactory` | Builds the component from a `PaymentMethod`. |
| Registration | `XInitializer` + module `AndroidManifest.xml` | Registers the factory for each supported type. |
| Public API | `XConfiguration` + `CheckoutConfiguration` DSL | The only public surface; everything else is `internal`. |
| Stored variant *(optional)* | `StoredX…` | A parallel stack for stored payments, built by the same factory. |
| Secondary screen *(optional)* | — | Pickers and sheets shown outside the main content. |

## Before you start

1. **Read `AGENTS.md`.** This skill defers to it for the working agreement and testing rules, and to the skills it routes to: `android-public-api-change` (visibility, sealed vs abstract), `android-ui-resources` (styles and strings), and `android-add-module` (external SDK handling).
2. **Create a plan document first.** Per `AGENTS.md`, write `<METHOD>_V6_MIGRATION_PLAN.md`, get it approved, and do not start coding until then. Keep it updated as phases complete. Do not commit the plan file.
3. **Inventory the v5 sources.** List the existing v5 files (delegate, views, provider, configuration, tests) and map each onto the v6 collaborators above. Note what already exists — some methods are partially migrated — so you don't recreate it.
4. **Flag method-specific concerns** in the plan: external SDK handoff (`compileOnly` + `runCompileOnly`/`checkCompileOnly` + ProGuard `dontwarn`), availability pre-checks, action/redirect handling, and any events that originate outside composition and have to reach the UI. Also decide which **optional capabilities** apply: params/mapper, input fields, a stored variant, a secondary screen.
5. **Invoke `architecture-guardian`** when a phase introduces or changes public API, new abstractions, or module boundaries.
6. **Agree the commit/PR cadence with the developer.** Commits stay small (one per phase), but PRs need **not** map 1:1 to commits. Decide upfront whether to open a PR per phase or — the default — per cohesive **group** of phases (e.g. `old/` move; state + view-state + view; component + factory + registration). Cadence is a reviewer preference, so confirm it during planning.

## Working rhythm (applies to every step)

Follow this loop for each phase below:

1. **Tests first.** Write/move the tests for the layer before (or alongside) the implementation, using the given-when-then style. Every phase that adds a class adds its unit tests; the v5 tests for any code you move go with it.
2. **Implement** the layer, defaulting to `internal` visibility.
3. **Make it green**, then run the `android-check` skill scoped to the touched modules, plus `core` when core changed.
4. **Commit** that single phase via the `android-commit` skill (one logical change per commit, `COSDK-XXXX` ticket). Never bundle multiple phases.

## Steps

### 1. Branch (and chain)

Use the `android-branch-create` skill to create a `chore/` branch (base `main` during v6). For a multi-phase migration, build a **stack** with one draft PR per layer so reviewers can review incrementally, at the cadence agreed with the developer (see *Before you start*). Keep the same prefix and extend the name (e.g. `chore/v6-ideal-state`, `chore/v6-ideal-view`). The phase order below is already a valid dependency order, so it maps directly onto stack layers.

### 2. Preserve the v5 implementation (`old/` package)

- **Do this before adding any v6 code**, so v5 keeps working in parallel and all new v6 code lands in the clean namespace. Move the existing v5 delegate/views/provider/configuration into an `old/` package. **Move the matching v5 tests** into the corresponding `old/` test packages in the same commit.
- **Gate:** `:module:check`. Commit.

### 3. Core payment details + serializer registration

- Add `XDetails : PaymentMethodDetails` in `core` **if it does not already exist**, with a `PaymentMethodTypes` constant for each supported type.
- **Register every type** in `PaymentMethodDetails.getChildSerializer`. Skipping this falls back to `GenericDetails` and fails at runtime with a `ClassCastException`.
- **Tests:** serialize `XDetails` through the `PaymentMethodDetails` serializer for every supported type and assert the fields survive — that is exactly the regression the fallback causes.
- **Gate:** `:core:test`; `:core:apiCheck` (`XDetails` is public — follow `android-public-api-change` before running `apiDump`). Commit.

### 4. Params + mapper *(optional)*

- **Skip this phase if the method needs no derived config** — pass the values the state factory needs straight through.
- Otherwise add the params and their mapper.
- **Tests:** mapper unit tests covering defaults and overrides.
- **Gate:** `:module:test`. Commit.

### 5. State layer

- Add the component state, intents, state factory, reducer, validator, and the mapping to the payment component state.
- **For methods with input fields**, follow the form model the migrated form components use:
  - Derive an ordered form from the component state. The form alone decides which fields are shown, in what order, whether they are valid, where focus goes and which keyboard action each field gets. Nothing else — reducer, view state or UI — keeps its own copy of any of these.
  - Field state holds the value and the error, not focus. Focus decisions need validation to have run, so they belong to the post processor, which is the only writer of the focus request. The reducer only sets values.
  - An invalid submit shows every error and focuses the first invalid field instead of submitting.
- **Tests:** reducer (per intent), validator (valid/invalid), post processor (per focus decision), and the state→payment mapping.
- **Gate:** `:module:test`. Commit.

### 6. View-state layer

- Add the view state and its producer. Localize validation errors with the **shopper locale**.
- **For methods with input fields**, produce one renderable element per form element, in form order, so the UI shows exactly what the form contains.
- **Tests:** producer tests for each meaningful state → view-state mapping.
- **Gate:** `:module:test`. Commit.

### 7. View layer (Compose)

- Add `XContent`: a wrapper that collects the view-state flow and hosts effects/launchers, delegating to a **private pure-UI composable**.
- **Reuse the shared composables from the `ui` module** — scaffold, pay button, text fields, pickers. They already implement focus handling, keyboard actions and accessibility semantics; do not reimplement any of it.
- **For methods with input fields:**
  - Render the elements in a loop keyed by element id, so focus survives fields appearing, disappearing or moving.
  - Give every text field its Autofill content type, or explicitly none when no type describes the field.
- **If the method has a secondary screen**, add its secondary content composable, following the method that already has one.
- **Add `@Preview` composables for the meaningful UI cases**, not just one happy path — e.g. default/empty, loading, validation error, available vs unavailable, and any method-specific variants (light/dark via `uiMode`, RTL, different styles). Previews take the view state (or a small UI model) directly so each case is rendered in isolation.
- Follow the `android-ui-resources` skill and other payment methods for styles and strings.
- **Tests:** logic/UI tests where applicable. Focus cannot be tested on the JVM, so check focus order, keyboard actions, Autofill and screen reader output on a device.
- **Gate:** `:module:check`. Commit.

### 8. Component + factory + registration

- Add the component, wiring the state flow with every state collaborator from step 5, and deriving the view state from it. For input methods, `submit()` validates first and highlights errors instead of submitting when the state is invalid.
- **If the method needs a secondary screen**, implement the secondary screen contract the way the existing method does.
- **Wire analytics** so every event that fired on v5 still fires (submit, errors, render where applicable).
- Add the factory.
- Add a `@Keep` initializer that registers the factory for each supported type, and wire it into the module `AndroidManifest.xml` under the androidx-startup `InitializationProvider`.
- **Tests:** component tests like those of other components — loading transitions, validity/availability, secondary-screen events, and error handling.
- **Gate:** `:module:check`; `:module:apiCheck`. Commit.

### 9. Stored payment method variant *(if supported)*

- Add a parallel stored stack — component, state collaborators, view state and content — shaped like the regular one.
- Make the **single** factory implement both the regular and the stored factory interfaces. Registration then covers both; no extra registration is needed.
- If necessary create the UI for the stored variant.
- **Tests:** stored component tests (e.g. security-code input where applicable, submit, loading).
- **Gate:** `:module:check`; `:module:apiCheck`. Commit.

### 10. Public configuration / DSL

- Add/confirm the public `XConfiguration` and its `CheckoutConfiguration` DSL extension. Everything else stays `internal` or `@RestrictTo(LIBRARY_GROUP)`. Prefer abstract classes over sealed for merchant-facing `when` safety (see the `android-public-api-change` skill).
- **Gate:** if the public API changed intentionally, run `:module:apiDump` and commit the `.api` files. Commit.

### 11. Example-app wiring

- Add the method to the v6 example flow: the supported-types list and the DSL config, plus a host Activity/screen if needed.
- Verify on a device/emulator (real payment path).
- Commit.

### 12. Final verification & PR

- Run `android-check` for the module and `:core` (compile, lint, unit tests, `apiCheck`).
- Open or finalize the draft PR(s) via the `android-pr-create` skill, following the cadence agreed during planning (earlier phase groups may already have open PRs). Use a checklist covering: serializer registration, tests per layer, v5 preserved under `old/`, public API reviewed, styles/strings, stored variant + secondary screen (if applicable), example wiring, and the device checks from step 7.

## Important

- **Plan first, code after approval.** No implementation before the plan document is approved.
- **The migrated methods are the specification.** When this skill and the code disagree, the code wins — follow it, and update this skill.
- **Tests are part of every step** — created for new layers, moved with relocated v5 code. Never weaken or delete tests to make a phase pass.
- **Small commits; PRs at an agreed cadence.** One logical change per commit via `android-commit`. When review feedback lands on a lower layer, fix it on that branch and rebase the layers above.
- **Don't skip serializer registration** in `PaymentMethodDetails.getChildSerializer`.
- **Default to `internal`.** Only the configuration/DSL is public. Discuss any breaking change before proceeding.
- **Adapt per method.** Confirm which collaborators and optional capabilities — params/mapper, input fields, stored variant, secondary screen — your method actually needs rather than copying all of them.
