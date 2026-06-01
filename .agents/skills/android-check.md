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
  - **If the failure is a public API mismatch** (e.g., `apiCheck` task fails): This means `.api` dump files are out of date. Inform the developer that they need to run `./gradlew apiDump` (or `./gradlew :<module>:apiDump` if module-scoped) to regenerate the `.api` files, and include those files in their commit.

## Important

- Do not make any code changes. This skill is read-only verification.
- If a module name is provided, use it consistently for all Gradle commands.
