---
name: android-branch-create
description: Create a branch with correct prefix and base.
---

# android-branch-create

Create a new branch for the Adyen Android SDK following project naming conventions.

## Usage

Invoke this skill when starting new work that requires a branch. The skill determines the correct prefix, base branch, and name, then creates and checks out the branch.

## Steps

### 1. Determine branch type

Ask the user what type of work this is:

| Prefix | When to use |
|--------|-------------|
| `feature/` | New functionality or enhancements |
| `fix/` | Bug fixes |
| `chore/` | Internal changes, refactoring, tooling, maintenance |

> **Note:** The prefix determines whether release notes are required — `feature/` and `fix/` require them, `chore/` and `renovate/` do not. Because the first alpha has been released, merchants can already observe changes made on `main`, so choose the prefix based on whether the change is merchant-observable:
>
> - **Public API change** (any `.api` file is updated) → `feature/` or `fix/`
> - **Behavioral change** without an API change — a different error code, a callback where there used to be a crash, changed defaults or validation → `feature/` or `fix/`
> - **Neither** — internal refactoring, tests, tooling, documentation → `chore/`
>
> If it is unclear whether a change is merchant-observable, ask the user before choosing the prefix.

### 2. Determine base branch

- Default: `main` (current development branch, v6)
- Use `v5` only for essential fixes or features required for v5 maintenance
- If this is part of a branch chain (phased work), the base should be the parent branch from the previous phase
- If unsure, ask the user

### 3. Compose branch name

**Format:** `<prefix>/<descriptive-name>`

Rules:
- Use 1-3 descriptive words separated by hyphens
- Do **not** use numbers in the descriptive part of the name (except for version prefixes)
- Including a version prefix is optional but helpful (e.g., `chore/v6-sdk-data-implementation`)

**For branch chaining** (multi-phase work):
- Keep the same prefix as the first branch in the chain
- Extend the name with descriptive suffixes

Example chain:
```
chore/drop-in-payment-method-list
chore/drop-in-payment-method-list-default
chore/drop-in-payment-method-list-ui
chore/drop-in-payment-method-list-stored
```

### 4. Confirm with user

Present the proposed branch name and base branch. Ask for approval before creating.

### 5. Create and checkout

```bash
git checkout <base-branch>
git pull
git checkout -b <branch-name>
git push --set-upstream origin <branch-name>
```

Confirm the branch was created by running `git rev-parse --abbrev-ref HEAD`.

## Important

- Never create a branch without confirming the name and base with the user.
- Always pull the latest base branch before creating the new branch.
