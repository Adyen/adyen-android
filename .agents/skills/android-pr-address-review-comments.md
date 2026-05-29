# android-pr-address-review-comments

Address review comments on a Pull Request for the Adyen Android SDK.

## Usage

Invoke this skill when you need to address reviewer feedback on a PR. Provide the PR number as input.

## Steps

### 1. Fetch the PR and review comments

```bash
# Get PR details
gh pr view <number> --json title,body,headRefName,baseRefName

# Get review comments with file paths
gh api repos/Adyen/adyen-android/pulls/<number>/comments \
  --jq '.[] | "ID: \(.id) | File: \(.path):\(.line) | Body: \(.body)"'

# Get general PR comments
gh pr view <number> --comments
```

### 2. Checkout the PR branch

```bash
gh pr checkout <number>
```

### 3. Summarize review comments

Present all review comments to the user in a structured format:

```
### Review Comments

1. **[file.kt:42]** — Reviewer's comment summary
2. **[file.kt:78]** — Reviewer's comment summary
3. **[General]** — Reviewer's general comment
```

Ask the user which comments to address. Some comments may need discussion rather than code changes — the user will decide.

### 4. Implement fixes

For each comment the user wants addressed:
- Make the requested code change
- Follow existing codebase patterns and conventions
- If a comment is unclear, ask the user for clarification before making changes

### 5. Commit the fixes

Group related fixes into logical commits — separate commits for logically distinct changes. Use the `android-commit` skill for each commit. The ticket number can be read from the PR body under `## Ticket Number`.

### 6. Push changes

```bash
git push
```

The PR will automatically update with the new commits.

### 7. Resolve review threads

After pushing, resolve the addressed review threads via the GitHub GraphQL API.

First, get the thread IDs:
```bash
gh api graphql -f query='query {
  repository(owner: "Adyen", name: "adyen-android") {
    pullRequest(number: <number>) {
      reviewThreads(first: 50) {
        nodes {
          id
          isResolved
          comments(first: 100) {
            nodes {
              id
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

Before resolving a thread, check if there is more than one comment in the thread (indicating an existing reply or discussion). If there is only the original comment (meaning we silently agreed with the feedback), add a thumbs up (👍) reaction to the first comment before resolving:

```bash
gh api graphql -f query='mutation {
  addReaction(input: {subjectId: "<comment-node-id>", content: THUMBS_UP}) {
    reaction { content }
  }
}'
```

If there is already a discussion (multiple comments), skip the reaction and just resolve.

Then resolve each addressed thread:
```bash
gh api graphql -f query='mutation {
  resolveReviewThread(input: {threadId: "<thread-id>"}) {
    thread { isResolved }
  }
}'
```

### 8. Report

Summarize what was done:
- Which comments were addressed
- What code changes were made
- Which threads were resolved
- Any comments that were left unresolved (and why)

## Important

- **Only resolve threads after the fix is implemented, committed, and pushed.** Never resolve a thread without making the change.
- **Do not resolve threads for comments the user chose to skip or disagree with.** Those should be discussed between the developer and reviewer.
- If you disagree with a review comment, do not implement it — flag it to the user for discussion.
- Always run pre-commit checks before committing.
- Never use `git add -A` or `git add .`.
