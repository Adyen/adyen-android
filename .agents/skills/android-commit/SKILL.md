---
name: android-commit
description: Create a commit with pre-commit checks and conventions.
---

# android-commit

Create a verified, properly formatted commit for the Adyen Android SDK.

## Usage

Invoke this skill after completing a phase of work and you are ready to commit. This skill runs pre-commit checks, then creates the commit following project conventions.

## Steps

### 1. Verify branch

Check the current branch with `git rev-parse --abbrev-ref HEAD`. If the current branch is `main` or `v5`, **stop immediately** and warn the user. Never commit directly to `main` or `v5`. Suggest using the `android-branch-create` skill to create a feature/fix/chore branch first.

Also ensure the branch is up to date with its upstream branch. If behind, pull the latest changes before proceeding.

### 2. Run pre-commit checks

Use the `android-check` skill (`.agents/skills/android-check.md`) to run verification checks. Scope the checks to the modules that have changes.

**If checks fail:** Do not proceed to committing.

### 3. Review changes

Run `git status` and `git diff` to understand what will be committed. Present a brief summary of changed files to the user.

### 4. Ask for ticket number

Ask the user for the ticket number (format: `COSDK-XXXX`). If the user has already provided a ticket number earlier in the session, reuse it — if necessary, confirm with the user that the same ticket applies.

### 5. Stage files

Stage only the specific files that were modified and are necessary for the commit. **Never use `git add -A` or `git add .`**. Use `git add <file1> <file2> ...` for individual files.

Include any `.api` files if they were updated (e.g., after running `apiDump` for intentional public API changes).

### 6. Security scan

Run `git diff --cached` to review the staged diff for:
- API keys, tokens, or secrets
- Credentials or passwords
- `.env` values or sensitive configuration

**If anything sensitive is found:** Stop immediately and warn the user. Do not commit.

### 7. Compose commit message

Format:
```
Short imperative description of the change.

COSDK-XXXX
```

- First line: concise summary of what was done (imperative mood)
- Blank line
- Ticket number

### 8. Confirm with user

Show the user:
- The list of staged files
- The proposed commit message

Ask for approval before executing the commit. If the user wants changes, adjust and re-confirm.

### 9. Execute commit

Run `git commit -m "..."` with the approved message.

Confirm the commit was created successfully by running `git log --oneline -1`.

## Important

- Never skip pre-commit checks.
- Never use `git add -A` or `git add .`.
- Always ask for ticket number if not already known.
- Always confirm the commit message with the user before executing.
- Stop immediately if secrets are detected in the diff.
