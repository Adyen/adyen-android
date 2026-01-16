# Pull Request Guide

This document outlines how to create and manage Pull Requests (PRs) in the Adyen Android SDK repository.

**Related guides:**
- [Branch Naming](BRANCH_NAMING.md) - Branch prefixes, base branches, chaining
- [GitHub CLI Commands](GH_CLI_COMMANDS.md) - `gh` command reference

## PR Title Conventions

**Format:** `[version prefix] - [Description]`

**Version prefix (nice to have):**
- `v5 -` for v5 branch PRs
- `v6 -` for v6/main branch PRs (until v6 is released)

**Examples:**
- `v5 - Google Pay - Issuer country config`
- `v6 - Card - State Migration`
- `v6 - Drop-in - Manage favorites screen`

The version prefix helps prioritize and differentiate between branches during active v6 development.

## PR Template

The PR template is located at `.github/pull_request_template.md`. Key sections:

### Description
- Include a short summary of changes
- For new features: attach screenshots or video if applicable
- For bug fixes: include reproduction path or link to related issue

### Checklist
```markdown
## Checklist <!-- Remove any line that's not applicable -->
- [ ] If applicable, make sure Breaking change label is added.
- [ ] Code is unit tested
- [ ] Changes are tested manually
- [ ] Related issues are linked
```

**Important:** Remove checklist items that don't apply to your PR.

### Ticket Number
Always include the ticket number after the checklist:
```
COSDK-XXX
```

The ticket number should match the one used in commits. If unknown, ask the developer.

### Release Notes
**Required for:** `feature/` and `fix/` branches only.

**Format:**
```markdown
## Release notes
### New
- For Google Pay, added the `allowedIssuerCountryCodes` and `blockedIssuerCountryCodes` configurations.

### Fixed
- Fixed an issue where components were recreated and lost their state.

### Improved
- For PIX, a QR code is now displayed and the copiable code is displayed above the copy button.

### Deprecated
- The following fields are now deprecated and have been moved to the new sdkData property:
  - `supportNativeRedirect` in `PaymentComponentData`
```

**Allowed section headers:** `Breaking changes`, `New`, `Fixed`, `Improved`, `Changed`, `Removed`, `Deprecated`

**Important:** Remove template comments when adding actual release notes to keep the PR clean.

## Creating PRs

### Always Create as Draft
All PRs should be created as **draft** initially. The developer will convert to ready for review after verification.

### PR Ownership
PRs should be opened in the name of the **developer/code editor**, not the AI agent.

### PR Chaining Process
When chaining PRs (see [Branch Naming](BRANCH_NAMING.md) for naming conventions):
1. Create first PR targeting `main` (or `v5`)
2. Create subsequent PRs targeting the previous PR's branch
3. After first PR merges, update subsequent PR bases to `main`

## Labels

### Automatic Labels
Labels are automatically assigned based on branch prefix:
- `Feature` - from `feature/` branches
- `Fix` - from `fix/` branches
- `Chore` - from `chore/` branches
- `Dependencies` - from `renovate/` branches

Size labels are also automatically added: `size:tiny`, `size:small`, `size:medium`, `size:large`, `size:huge`

### Manual Labels
**Breaking change** - Must be added manually if the PR contains breaking changes. Check the checklist item as a reminder.

### Reviewers
Do NOT manually assign reviewers or request reviews. GitHub handles this automatically via CODEOWNERS.

## PR Size Guidelines

- Keep PRs as small as possible for easier review
- Ideally, each phase of an implementation should have its own PR
- If a phase is too large, split into sub-phases with separate PRs
- No hard restrictions, but smaller PRs merge faster

## Description Best Practices

**For UI changes:**
- Always include screenshots or videos
- Use GitHub's image/video upload

**For bug fixes:**
- Link to related issues: `## Related issues\nhttps://github.com/Adyen/adyen-android/issues/XXXX`
- Describe the fix briefly

**For refactoring/chore:**
- Can be brief, just describe what was done
- No release notes needed

**Example - Feature with UI:**
```markdown
## Description
Implement UI of manage favorites screen. Removing a favorite is not implemented yet and will be done in the next PR.

<img width="1280" height="2856" alt="image" src="..." />

## Checklist
- [x] Changes are tested manually

COSDK-812
```

**Example - Bug Fix:**
```markdown
## Description
Store the `componentViewType` and recreate the view only when `componentViewType` has been changed.

## Related issues
https://github.com/Adyen/adyen-android/issues/2416

## Checklist
- [x] Changes are tested manually

COSDK-794

## Release notes
### Fixed
- Fixed an issue where components were recreated and lost their state when the app returned from the background.
```

## Release Strategy Overview

The repository follows this release flow:
1. **Feature branches** → merge to `main`
2. **Release branch** created from `main` (no direct pushes allowed)
3. **Release notes PR** opened on a `release_notes` branch
4. After release notes merge: publish to Maven, create GitHub release with tag
5. **Hotfixes** cherry-picked to release branch if needed

For `v5` maintenance:
- PRs merged to `v5` branch
- Similar release process but separate from main development

---

**Remember:** When in doubt, ask the developer for clarification before creating a PR.
