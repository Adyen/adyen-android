---
name: android-add-dependency
description: Add, remove or update an external dependency in the SDK. Use before touching gradle/libs.versions.toml, including when the change is a side effect of a larger task.
---

# android-add-dependency

Decide whether to add, change, or remove a dependency in the Adyen Android SDK, then apply it without breaking release notes generation or dependency verification.

## Usage

Invoke this skill **before** touching `gradle/libs.versions.toml` — as soon as it looks like work will need a new library or plugin, a different version or artifact, or the removal of an existing one. This applies even when the dependency change is a side effect of a larger task, and even when the developer named the dependency themselves.

Renovate handles routine version bumps. This skill is for dependency changes made by hand.

## Steps

### 1. Stop and get approval for the dependency itself

**Do not edit any file yet.** This is a decision, not a mechanical step. We are an SDK: every dependency we take is imposed on every merchant that integrates us, and every one we drop or re-point can break them. The default answer to a new dependency is no.

**First, try to avoid it:**

- Does the Android platform, AndroidX, or something already in `gradle/libs.versions.toml` cover this?
- Is the part we actually need small enough to implement ourselves? For published modules, prefer a small internal implementation over a new dependency.
- Does it already reach us transitively? Declaring an existing transitive dependency explicitly is a much smaller step than taking a genuinely new artifact.

**Then present the impact.** Answer every line — "none" is a valid answer, guessing is not:

> **Impact of `<group:name:version>`**
> - **Scope** — which modules, and which configuration (`api`, `implementation`, `compileOnly`, `testImplementation`, `androidTestImplementation`)
> - **Merchants** — does it end up in merchant builds? Cover version conflicts with libraries merchants commonly use, `minSdk` (ours is 23), download size and method count, and the transitive artifacts it drags in
> - **Public API** — do its types appear in our public API? If so its version becomes part of our contract and later upgrades turn into breaking changes. Prefer keeping it out of signatures and off `api`
> - **R8 / ProGuard** — consumer rules or `dontwarn` needed? If it is an optional external SDK, does it need the `compileOnly` + `runCompileOnly` treatment described in the `android-add-module` skill (`.agents/skills/android-add-module/SKILL.md`)?
> - **Release notes** — `[included]` or `[excluded]`, and why (step 4 works out the exact entry)
> - **Build and CI** — verification metadata churn, build and CI time, added Renovate surface
> - **Tests** — impact on the test suite, if any
> - **Maintenance and supply chain** — license, release cadence, last release, maintainer, and whether the version is at least 14 days old (matching the `minimumReleaseAge` Renovate uses in `renovate.json`)
> - **Alternatives considered** — what you rejected, and why this is the better option

For a **change**, also state what moves: the size of the version jump, breaking changes in the new version, and any coordinate or artifact swap.

For a **removal**, also state whether anything still uses it, whether its types were exposed in our public API, and whether merchants may be resolving it through us. Removing a dependency merchants rely on transitively is a breaking change — follow the `android-public-api-change` skill (`.agents/skills/android-public-api-change/SKILL.md`).

**Ask for explicit approval and wait for an answer.** This holds even when the developer already asked for this exact dependency; they should see the impact before it lands. If they have clearly already decided, keep the summary short, but still show it.

### 2. Choose the version and declaration

- Never use a dynamic version (`+`, `latest.release`).
- If the artifact is covered by a BoM that is already declared (e.g. `compose-bom`), declare it without a version — see `compose-ui-main` in `gradle/libs.versions.toml`.
- Use the narrowest configuration that works. `implementation` over `api`, and test configurations over production ones.

### 3. Update `gradle/libs.versions.toml`

- Add the version to `[versions]` under the matching comment group, and the entry to `[libraries]` or `[plugins]`.
- Follow the ordering of the group you are adding to.
- Omit `version.ref` when the version comes from a BoM.

### 4. Update `.github/release_notes_dependency_list.toml`

Every dependency must appear in `[included]` or `[excluded]`. This is enforced: `scripts/validate_dependencies_for_release_notes.py` runs on every PR through `.github/workflows/validate_dependencies.yml` and fails the build for unlisted dependencies.

**Derive the id.** The key is not the alias. It is derived from the entry:

| Entry has | Id to use |
|-----------|-----------|
| `group` and `name` | `<group>:<name>` |
| `module` | the `module` value |
| neither (plugins) | the `id` value |

**Choose the section.** Use `[included]` only when both are true:

1. The dependency is merchant-relevant, meaning either:
   - It ships to merchants — declared in a published SDK module as `api`, `implementation`, or `compileOnly`, so it resolves in a merchant's build. Compare `com.squareup.okhttp3:okhttp` (in `core` and `checkout-core`, included) against `com.squareup.okhttp3:logging-interceptor` (example app only, excluded).
   - Or it is part of the toolchain merchants have to be compatible with. This applies to the Android Gradle Plugin and Kotlin only — every other build, CI, and code quality tool is excluded.
2. No existing `[included]` entry already represents it. A BoM covers its artifacts, or a sibling from the same release train is already listed. The Kotlin artifacts and plugins are all represented by the single `kotlin` entry, and `com.android.application`/`com.android.library` by `com.android.tools.build:gradle`.

Everything else goes in `[excluded]`: test-only, example-app-only, lint, `build-logic`, CI, and code quality tooling.

> Do not use the comment groups in `libs.versions.toml` to decide. `# Production libraries` also holds example-app and tooling dependencies such as `leak-canary`, `retrofit-main`, and `ktlint-cli`. Find where the alias is actually used, and in which configuration.

**Write the entry.**

`[excluded]` values are an empty string. Add a comment when the reason is not obvious — in particular when a production dependency is excluded because another entry covers it:

```toml
# This dependency is already defined by another
"androidx.navigation3:navigation3-ui" = ""
```

`[included]` values are a markdown link. `{}` is replaced with the new version when release notes are generated:

```toml
"androidx.startup:startup-runtime" = "[AndroidX Startup](https://developer.android.com/jetpack/androidx/releases/startup#{})"
```

- Use a version-anchored URL so the entry deep-links to the notes for that release. Follow a neighbouring entry from the same vendor:
  - AndroidX: `https://developer.android.com/jetpack/androidx/releases/<artifact>#{}`
  - GitHub releases: `https://github.com/<org>/<repo>/releases/tag/{}` — check whether the project prefixes its tags with `v`.
- If the project publishes no per-version anchor, use a stable changelog or docs URL and leave out `{}`. See the OkHttp and TWINT entries.
- Verify the formatted URL resolves for the version you are adding. A misplaced `{}` produces a dead link in published release notes.
- Use a human-readable display name in the style of the surrounding entries.
- Keep the list alphabetically ordered by id.

### 5. Confirm the entry with the developer

This is the second confirmation, and it is about the entry rather than the dependency. Always ask, even when the choice looks obvious. Show:

- The id, and the section you propose
- Why that section applies
- For `[included]`, the link template and the URL it produces for the version being added

Do not write the entry before you get an answer. If you cannot work out a good link, ask instead of guessing.

### 6. Update verification metadata

Dependency verification is enabled in `gradle/verification-metadata.xml`, so a new artifact fails the build until its checksum is recorded:

```bash
./gradlew --write-verification-metadata sha256 resolveDependencies assembleDebug
```

`assembleDebug` is included so dynamic dependencies such as `aapt2` resolve. Review the diff — it should only add entries for the new dependency and its transitives — and include the file in the commit.

### 7. Verify

- Run the same validation CI runs: `python3 scripts/validate_dependencies_for_release_notes.py`. It compares against the merge base with `origin/main`, so run it on your branch.
- Use the `android-check` skill (`.agents/skills/android-check/SKILL.md`) to run compile, lint, and unit tests.

## Modifying and removing dependencies

- **Changed coordinates** on an existing alias (`group`, `name`, or `module`): update the id in `.github/release_notes_dependency_list.toml` too. PR validation only inspects newly added *aliases*, so a coordinate change passes CI and instead breaks release notes generation later with `Dependency not recognized`.
- **Removed dependency**: remove its entry from `[included]` or `[excluded]` in the same change, unless the same id is still declared by another alias.
- **Version-only bumps**: no change to the dependency list is needed. Step 1 still applies for a major version jump, since that is a merchant-visible change.

## Important

- Never add, change, or remove a dependency without approval from the developer first. Present the impact before making any change, including when they named the dependency themselves.
- Taking a new dependency in a published module is a decision about our SDK's contract with merchants, not an implementation detail.
- Never change `gradle/libs.versions.toml` without updating `.github/release_notes_dependency_list.toml` in the same change.
- Always have the developer confirm the `[included]`/`[excluded]` choice and the link before writing it.
- Never guess a release notes URL, a license, or a maintenance status. Look it up, or say you could not determine it.
- `[included]` entries are published to merchants in the release notes. Treat them as merchant-facing content.
