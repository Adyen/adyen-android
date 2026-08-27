---
name: android-pr-create
description: Create a PR with title, body, and checklist.
---

# android-pr-create

Create a Pull Request for the Adyen Android SDK following project conventions.

## Usage

Invoke this skill when you are ready to open a PR for the current branch. The skill generates the full PR body, shows it for user approval, and creates the PR.

## Steps

### 1. Gather context

Collect the current branch name, the commits it adds on top of its base (`main`, `v5`, or the parent branch in a stack), and a summary of the files they change.

Determine from the branch name:
- **Branch type**: `feature/`, `fix/`, `chore/`, or `renovate/`
- **Whether release notes are required**: Yes for `feature/` and `fix/`, No for `chore/` and `renovate/`

Then verify the prefix actually matches the change. Inspect the diff for public API changes (updated `.api` files) and for behavioral changes a merchant could observe — a different error code, a callback where there used to be a crash, changed defaults or validation. If the branch is `chore/` or `renovate/` but the diff contains either, the prefix is probably wrong and release notes are likely required. Tell the user and ask whether to rename the branch or proceed anyway. Never omit release notes silently.

### 2. Determine base branch

- Default base: `main`
- If the branch name contains `v5` or the user specifies, use `v5`
- If this branch is a layer in a stack, the base is the branch **directly below it**, not the trunk. For a chain built with plain git, ask the user for the parent branch name.

### 3. Compose PR title

Format: `[version prefix] - [Description]`

- Use `v6 -` prefix for `main`-based branches (until v6 is released)
- Use `v5 -` prefix for `v5`-based branches
- Description should be concise and match what the PR does

Examples:
- `v6 - Card - State Migration`
- `v5 - Google Pay - Issuer country config`
- `v6 - Drop-in - Manage favorites screen`

### 4. Compose PR body

Use the template at `.github/pull_request_template.md` as the base structure. Fill in each section:

#### Description section

Write a clear summary of what the PR does based on the commits and diff. For UI changes, remind the user to attach screenshots or video.

#### Progress section (for multi-phase work)

If the current work is part of a phased implementation plan, include a `### Progress` section inside the description. Format:

```
### Progress

✅ Phase 1 — [short description](link-to-PR)
✅ Phase 2 — [short description](link-to-PR)
➡️ **Phase 3 — short description (this PR)**
Phase 4 — short description
Phase 5 — short description
```

Rules:
- ✅ for completed phases — include a link to the PR
- ➡️ **bold** for the current phase (this PR)
- Plain text (no emoji) for future phases
- To find links for completed phases, search the repository's open and merged PRs on the same base branch
- If there is an implementation plan file (`*_IMPLEMENTATION_PLAN.md`), use it to identify the phases
- If the work is not phased, omit this section entirely

Keep this section for stacked PRs too — the stack shows branch order, but not the plan behind it.

#### Checklist section

Include the checklist from the template. Remove items that do not apply. Check items that are done:

```markdown
## Checklist
- [ ] If applicable, make sure Breaking change label is added.
- [x] Code is unit tested
- [x] Changes are tested manually
- [ ] Aligned public API changes with other platforms (if applicable)
- [ ] Related issues are linked
```

#### Ticket Number section

```markdown
## Ticket Number
COSDK-XXXX
```

Use the ticket number from the commits or ask the user if not found.

#### Release notes section

**Only include for `feature/` and `fix/` branches.** For `chore/` and `renovate/` branches, omit the release notes section entirely.

For feature/fix branches, generate release notes under the appropriate headers:

```markdown
## Release notes
### New
- Description of new feature.

### Fixed
- Description of fix.
```

Allowed headers: `Breaking changes`, `New`, `Fixed`, `Improved`, `Changed`, `Removed`, `Deprecated`

Remove all template comments (`[//]: #`) from the final output.

### 5. Show for approval

Present the complete PR to the user:
- Title
- Base branch
- Full body

Ask for approval. If the user wants changes, adjust and re-present.

### 6. Create the PR

After approval, open the PR against the base branch determined above, as a draft unless the user wants it ready for review.

For a stack, PRs are opened and linked per layer in one go, covering **every** branch that does not have a PR yet. The generated titles and bodies do not follow the conventions above, so correct them afterwards.

Show the PR URL(s) to the user after creation.

### Merging a stack

Worth telling the user when a stack is opened: stacks merge **bottom-up**, so merging a PR also merges every unmerged PR below it.

## Important

- PRs should be opened in the name of the developer, not the AI agent.
- Never assign reviewers — GitHub CODEOWNERS handles this automatically.
- Remove all template comments from the final PR body.
- If release notes are required but you are unsure what to write, ask the user.
- Do not add labels manually — they are assigned automatically based on branch prefix. Exception: if the PR contains breaking changes, remind the user to add the `Breaking change` label.
