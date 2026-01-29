# GitHub CLI Commands Reference

This document provides commonly used `gh` CLI commands for the Adyen Android SDK repository.

## Important

**CRITICAL:** Every `gh` command must be verified by the developer before execution. Never auto-run GitHub commands.

## Creating PRs

The PR template is located at `.github/pull_request_template.md`. See [Pull Request Guide](PULL_REQUEST_GUIDE.md) for template details.

### Create a Draft PR (always)
```bash
gh pr create --draft --base main --title "v6 - Feature description" --fill
```
This opens an editor pre-filled with the PR template. Edit and save to create the PR.

### Create PR Targeting Another Branch (for chaining)
```bash
gh pr create --draft --base feature/parent-branch --title "v6 - Child feature" --body "..."
```

## Viewing PRs

### List Recent Merged PRs
```bash
# List recent PRs merged to main
gh pr list --state merged --base main --limit 10 --json number,title,headRefName

# List recent PRs merged to v5
gh pr list --state merged --base v5 --limit 10 --json number,title,headRefName
```

### View a Specific PR
```bash
# View PR details
gh pr view 2424 --json body,title,headRefName

# View PR in browser
gh pr view 2424 --web
```

### Check PR Status
```bash
gh pr status
```

### View Your Draft PRs
```bash
gh pr list --author @me --draft
```

### View All Your PRs
```bash
gh pr list --author @me
```

## Managing PRs

### Update PR Title
```bash
gh pr edit 2424 --title "v6 - Updated title"
```

### Update PR Body
```bash
gh pr edit 2424 --body "New body content"
```

### Add Labels
```bash
gh pr edit 2424 --add-label "Breaking change"
```

### Mark PR Ready for Review
> ⚠️ **Note:** This should only be done by developers, not agents. We manually review the PR before taking it out of draft.

```bash
gh pr ready 2424
```

## Branch Operations

### Check Out a PR Locally
```bash
gh pr checkout 2424
```

### View PR Diff
```bash
gh pr diff 2424
```

## Searching

### Search PRs by Title
```bash
gh pr list --search "sdk data" --state all
```

### Search Merged PRs with Specific Label
```bash
gh pr list --state merged --label "Feature" --limit 20
```

## Useful Combinations

### Get PR Reference for Description Writing
```bash
# See how similar PRs were written
gh pr list --state merged --base main --limit 20 --json number,title,headRefName | jq -r '.[] | "PR #\(.number): \(.title) | Branch: \(.headRefName)"'
```

### View Full PR Body
```bash
gh pr view 2424 --json body | jq -r '.body'
```
