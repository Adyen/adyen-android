# Addressing PR Reviews

This document outlines how to handle review comments on Pull Requests in the Adyen Android SDK repository.

**Related guides:**
- [Pull Request Guide](PULL_REQUEST_GUIDE.md) - PR creation and templates
- [GitHub CLI Commands](GH_CLI_COMMANDS.md) - `gh` command reference

## Workflow Overview

1. Fetch review comments
2. Discuss changes with developer
3. Implement fixes
4. Commit changes
5. Push to update the PR
6. Mark conversations as resolved

## Fetching Review Comments

### List PR Comments
```bash
gh pr view <number> --comments
```

### Get Comment Details via API
```bash
gh api repos/Adyen/adyen-android/pulls/<number>/comments --jq '.[] | "\(.id) - \(.path): \(.body)"'
```

## Implementing Fixes

**Best practices:**
- Group related fixes from review comments into a single commit
- Create separate commits for logically distinct changes
- Use a descriptive commit message that references the fixes
- Include the ticket number

**Example commit message:**
```
Address review comments for [feature/module name]

- Fix issue mentioned in comment 1
- Update documentation as suggested
- Rename variable for clarity

COSDK-XXX
```

## Pushing Updates

After committing fixes:
```bash
git push
```

The PR will automatically update with the new commit.

## Resolving Conversations

After implementing fixes, mark review threads as resolved.

### Find Review Thread IDs
```bash
gh api graphql -f query='query {
  repository(owner: "Adyen", name: "adyen-android") {
    pullRequest(number: <number>) {
      reviewThreads(first: 20) {
        nodes {
          id
          isResolved
          comments(first: 1) {
            nodes {
              body
              path
            }
          }
        }
      }
    }
  }
}'
```

### Resolve a Thread
```bash
gh api graphql -f query='mutation {
  resolveReviewThread(input: {threadId: "<thread-id>"}) {
    thread { isResolved }
  }
}'
```

> ⚠️ **Note:** Only resolve conversations after implementing the requested changes. If you disagree with a review comment, discuss it with the developer before resolving.

## Review Comment Types

### Code Changes
- Implement the suggested fix
- If the suggestion is unclear, ask for clarification
- Test your changes before committing

### Documentation Updates
- Update comments, README, or other docs as requested
- Ensure consistency with existing documentation style

### Style/Naming Suggestions
- Follow the reviewer's suggestion unless it conflicts with existing conventions
- When in doubt, check similar code in the codebase

### Questions from Reviewers
- Respond to questions in the PR conversation
- Add code comments if clarification helps future readers

## Handling Disagreements

If you believe a review comment should not be implemented:
1. **Do not** resolve the conversation
2. **Do not** implement the change
3. Discuss with the developer to understand the context
4. Let the developer and reviewer resolve the disagreement

## Multiple Review Rounds

PRs may go through multiple review rounds:
1. Address all comments from the current round
2. Push changes in a single commit (or logical commits)
3. Mark resolved conversations
4. Wait for the next review round
5. Repeat until approved

## Quick Reference

| Task | Command |
|------|---------|
| View PR comments | `gh pr view <number> --comments` |
| Get thread IDs | `gh api graphql -f query='...'` (see above) |
| Resolve thread | `gh api graphql -f query='mutation {...}'` |
| Push fixes | `git push` |
