---
name: android-check
description: Run compile, lint, and unit test checks.
---

# android-check

Run verification checks for the Adyen Android SDK.

## Usage

Invoke this skill to verify the project compiles, tests pass, and lint is clean.

**Optional parameter:** A module name to scope checks to a single module (e.g., `card`, `drop-in`, `checkout-core`). If not provided, checks run for the entire project.

## Steps

### 1. Run check

Run the full check task (compile, lint, unit tests):

- **Module-scoped:** `./gradlew :<module>:check`
- **Full project:** `./gradlew check`

### 2. Report results

- **On success:** Confirm all checks passed.
- **On failure:** Parse the Gradle output and provide a concise summary of what failed (compilation errors, test failures, lint violations). Include file paths and line numbers when available. Do not dump the entire Gradle log.
  - **If the failure is a public API mismatch** (e.g., `apiCheck` task fails): the public API changed. Report *what* changed, and do not run `apiDump` to silence it — an unintended API change is a real failure. Follow the `android-public-api-change` skill (`.agents/skills/android-public-api-change/SKILL.md`) to confirm the change is intended before the dump files are regenerated.

## Important

- Do not make any code changes. This skill is read-only verification.
- If a module name is provided, use it consistently for all Gradle commands.
