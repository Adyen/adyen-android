# android-pr-create

Create a draft Pull Request for the Adyen Android SDK following project conventions.

## Usage

Invoke this skill when you are ready to open a PR for the current branch. The skill generates the full PR body, shows it for user approval, and creates the PR as a draft.

## Steps

### 1. Gather context

Collect the following information:

```bash
git rev-parse --abbrev-ref HEAD              # current branch name
git log --oneline <base-branch>..HEAD        # commits on this branch (base is main, v5, or parent branch)
git diff <base-branch> --stat                # files changed summary
```

Determine from the branch name:
- **Branch type**: `feature/`, `fix/`, `chore/`, or `renovate/`
- **Whether release notes are required**: Yes for `feature/` and `fix/`, No for `chore/` and `renovate/`

### 2. Determine base branch

- Default base: `main`
- If the branch name contains `v5` or the user specifies, use `v5`
- If this is part of a PR chain, ask the user for the parent branch name and use that as base

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
- To find links for completed phases, search for related PRs:
  ```bash
  gh pr list --state all --base <base-branch> --search "<search term>" --json number,title,url
  ```
- If there is an implementation plan file (`*_IMPLEMENTATION_PLAN.md`), use it to identify the phases
- If the work is not phased, omit this section entirely

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

After approval, create as a **draft**:

```bash
gh pr create --draft --base <base-branch> --title "<title>" --body "<body>"
```

Show the PR URL to the user after creation.

## Important

- Always create PRs as draft. Never create a non-draft PR.
- PRs should be opened in the name of the developer, not the AI agent.
- Never assign reviewers — GitHub CODEOWNERS handles this automatically.
- Remove all template comments from the final PR body.
- If release notes are required but you are unsure what to write, ask the user.
- Do not add labels manually — they are assigned automatically based on branch prefix. Exception: if the PR contains breaking changes, remind the user to add the `Breaking change` label.
