# Agent Guidelines for Adyen Android SDK

How to work in this repository. Detailed procedures live in skills under `.agents/skills/` — this file covers what applies to every task and points to the rest.

## The project

Adyen Android is a modular payments SDK: merchants integrate either Drop-in or individual payment method components, published as ~58 Gradle modules (see `settings.gradle`).

- `main` is **v6, in alpha** — the public API can still change, but it is released, so changes on `main` are already merchant-observable.
- `v5` is the stable maintenance branch. Only essential v5 work goes there.
- v6 is **Compose-first**. The XML-based v5 UI still lives in the repo, largely under `old/` packages.

For the architecture and the public surface, read [README.md](README.md), the v6 guides in [docs/v6/](docs/v6/README.md). Manual testing happens in [example-app/](example-app/README.md).

## Working agreement

**Plan before implementing.** For anything beyond a small, local change, present a plan and wait for approval before editing code. Break the work into phases and state the dependencies, risks, and testing strategy upfront. Multi-phase work gets a plan document (for example `*_IMPLEMENTATION_PLAN.md`) that you keep updated as phases complete — mark items done, note deviations. Plan documents are working artifacts: do not commit them unless asked.

**One phase, one commit.** Finish a phase, commit it with the `android-commit` skill, then start the next. Never accumulate several phases in one commit.

**Find the precedent first.** Whatever you are building almost certainly has a close analogue in the codebase. Locate it and match it, rather than introducing a second way of doing the same thing.

**Ask instead of assuming.** If the convention, the expected behavior, or whether the change is wanted at all is unclear, ask. Do not implement through uncertainty.

## Testing

- **Write the test first**, then make it pass. Before refactoring, make sure the affected code is already covered — if it is not, write those tests first to capture current behavior.
- New tests use JUnit 5 with Mockito-Kotlin, and Turbine for flows. A handful of older tests still use JUnit 4 or Robolectric; do not copy that as the default.
- Name tests `when <condition> then <expectation>` and structure the body as GIVEN / WHEN / THEN, following neighboring tests.
- Run checks through the `android-check` skill rather than calling Gradle directly, and run them as you go rather than accumulating unverified work.

## Rules that always apply

- New code is `internal` by default. Cross-module but not merchant-facing means `@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)`, not `public`.
- No breaking change lands without explicit agreement from the developer.
- Everything shown to a shopper uses the **shopper locale**, never the device locale.
- Never commit to `main` or `v5` — branch first.
- Never run `apiDump` just to make `apiCheck` pass. An unexpected `.api` diff means the API changed by accident.

## Task routing

| When the work involves | Use |
|------------------------|-----|
| Starting new work | `android-branch-create` |
| Compiling, linting, running tests | `android-check` |
| Committing a finished phase | `android-commit` |
| Opening a pull request | `android-pr-create` |
| Public API: visibility, signatures, `apiDump`, sealed vs abstract | `android-public-api-change` |
| Files under a module's `res/`: layouts, styles, strings | `android-ui-resources` |
| A new Gradle module, or an optional external SDK | `android-add-module` |
| Adding, updating, or removing an external library | `android-add-dependency` |
| Moving a v5 payment method to v6 | `android-migrate-payment-method-v6` |

Skills live in `.agents/skills/<name>/SKILL.md`. Not every skill might be mentioned above.

## Before calling work done

1. Checks pass for every affected module (`android-check`).
2. New classes and functions have unit tests, and the tests were written first.
3. The plan document, if there is one, reflects what was actually done.
4. The rules from every skill the work touched have been followed.
5. Changes match existing patterns, and each one is necessary.

## Resources

- [Public documentation](https://docs.adyen.com/online-payments/build-your-integration/?platform=Android)
- [Migration guide](MIGRATION.md)
