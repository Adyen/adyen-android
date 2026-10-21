---
name: android-code-review
description: Review a PR, or the current branch, against SDK conventions and report prioritized findings. Optionally post them as a pending GitHub review.
---

# android-code-review

Review code changes in the Adyen Android SDK the way the team reviews each other: grounded in the codebase, focused on behavior, and phrased as concrete, verifiable findings.

## Usage

Invoke this skill to review someone else's pull request, or to self-review your branch before opening one.

**Optional parameter:** A PR number or URL. If provided, the skill reviews that PR. If not, it reviews the current branch against its base.

The review is read-only. It ends with a report in the chat; in PR mode, selected findings can then be posted as a pending review that the developer submits themselves.

## Steps

### 1. Determine the target

**PR mode** (a number or URL was given):
- Fetch the PR metadata (title, body, author, base and head branch, head SHA), the diff, and the list of changed files.
- Fetch the existing review threads, from humans and bots (e.g. `gemini-code-assist`), with their resolved state. These are used in step 5 to avoid repeating what was already raised.
- Do **not** check out the PR branch — the working tree may hold the developer's own work. Fetch the head of the pull request and read the full files directly from that commit.

**Branch mode** (no number given):
- Determine the base the same way as the `android-pr-create` skill: `main` by default, `v5` for v5 work, or the branch directly below in a stack.
- Review the changes the branch adds on top of the merge base. If the working tree has uncommitted changes, include them and say so in the report.

In both modes, a layer in a stack is reviewed against the layer below it — never re-review lower layers.

### 2. Understand the intent

Before judging the code, establish what it is supposed to do:

- Read the PR description, commit messages, and the ticket number (`COSDK-XXXX`) if present.
- If the work is phased, read the plan document (`*_IMPLEMENTATION_PLAN.md`) or the PR's `### Progress` section. Work that is explicitly deferred to a later phase is not a finding — at most a question.
- Decide whether the touched code is v5 or v6, using the rules in `AGENTS.md`. New code that extends v5 on `main` is a finding unless it is an essential fix.
- Note the size. More than ~500 changed lines means the PR should probably be split into a stack.

Summarize the intent in one or two sentences. If you cannot, that is itself the first question for the author.

### 3. Read beyond the diff

A diff hides most of what matters. For every non-trivial change:

- Read the whole file, not just the hunks.
- Find the callers of changed functions and the other paths that do the same job. A check enforced in one path (e.g. a factory) and skipped in another (e.g. a reducer) is a common bug.
- Find the closest precedent in the codebase. The change should match it, not introduce a second way of doing the same thing.
- Search for the same logic elsewhere. If it now exists in a third place, suggest moving it to a shared place (a follow-up is fine).
- Check `ADR/` for decisions that apply to the area.

### 4. Review against the checklist

Work through the categories that apply to the diff.

#### Correctness and behavior

- Logic errors, unhandled states, and edge cases: empty and null values, loading and failed states, repeated or concurrent calls, cancellation.
- Exhaustive `when` over sealed types rather than casts or `else` branches, so a future case fails to compile instead of being silently ignored.
- State that must survive configuration changes and process death.
- Failure paths: when a request fails, what does the shopper see, and can they recover?

#### Shopper-facing input and UI

- Input arrives by typing, pasting, **and** autofill. Check input transformations against all three: `maxLength` reverts the whole change instead of truncating, pasted text can contain line breaks, tabs, and other invisible characters.
- A change to one field can invalidate another's existing value (e.g. a country change shrinks the postal code max length).
- Everything shown to the shopper uses the shopper locale, never the device locale.
- Changes under `res/` follow the `android-ui-resources` skill. New or changed copy may need confirmation from design — ask rather than assume.

#### Architecture and consistency

- New abstractions, interfaces, or generic type parameters with a single consumer. Ask whether a second consumer is planned before accepting them.
- Names that describe the effect, not the trigger (e.g. `Reset` rather than `Prefill` when the action resets the form).
- No new imports from v5 core modules (`checkout-core`, `components-core`, `ui-core`, `action-core`, `sessions-core`) in v6 code.

#### Public API

Any change to `.api` files, or to non-`internal` declarations, is checked against the `android-public-api-change` skill:

- New code is `internal` by default; cross-module-only code uses `@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)`.
- Each line of the `.api` diff is intentional and justified.
- Breaking changes have explicit agreement and need the `Breaking change` label.
- Public behavior and naming are aligned with iOS and Web. Flag suspected cross-platform discrepancies, including ones that look like a bug on the other platform.
- A merchant-observable change on a `chore/` branch means the prefix is wrong and release notes are missing.

#### Tests

- New classes and functions have unit tests, using JUnit 5, Mockito-Kotlin, and Turbine for flows. New JUnit 4 or Robolectric tests are a finding.
- Test names follow `when <condition> then <expectation>`, and bodies are structured as GIVEN / WHEN / THEN.
- Every state and branch is covered, not just the happy path (e.g. the loading state, every case with special handling plus the fallback).
- Tests that would pass for the wrong reason: an input that equals the default or fallback value proves nothing.

#### Dependencies, build, and CI

- Changes to `gradle/libs.versions.toml` follow the `android-add-dependency` skill: release notes dependency list and verification metadata are updated.
- Workflow changes do not expose tokens or secrets to code they do not need to trust (e.g. credentials persisted by `actions/checkout` while third-party build code runs).
- No secrets, keys, or credentials anywhere in the diff.

#### PR hygiene (PR mode)

- Title, ticket number, checklist, and release notes follow the `android-pr-create` skill.
- Deferred work is marked with a `TODO` and a ticket, not left as a silent gap.

### 5. Filter and classify findings

Keep only findings that survive these filters:

- **Verified.** You read the code that proves it. Anything you cannot confirm from the code becomes a question, not a claim.
- **Concrete.** Describe the scenario that breaks, as a short reproduction when possible ("select US, type `12345-6789`, switch to NL — backspace no longer works").
- **New.** Not already raised in an existing thread, and not explicitly deferred by the plan.
- **Not automated.** Skip formatting and style that detekt, ktlint, and Android lint already enforce in CI.

Classify each finding:

- **Blocking** — bugs, broken behavior, unintended or unagreed public API changes, security issues, new logic without tests.
- **Should fix** — inconsistencies with existing patterns, duplication, misleading names, test gaps.
- **Question** — intent, design, or cross-platform behavior that the code alone cannot answer.
- **Nit** — optional polish. Keep these few.

Prefer a short list of high-confidence findings over an exhaustive one. Do not pad the review with praise or restate what the code does.

### 6. Report

Present the review in this format, omitting empty sections:

```
## Review: <PR #123 — title | branch-name>

<One or two sentences: what the change does and the overall assessment.>

### Blocking
1. **`address/src/main/java/.../AddressTextField.kt:33`** — Problem, the concrete scenario, and a suggested fix.

### Should fix
2. ...

### Questions
3. ...

### Nits
4. ...

Not reviewed: <anything skipped, and why>
```

Number findings continuously across sections, so they can be referred to by number.

In branch mode, the review ends here. Offer to address findings, but do not change code unless the developer asks.

### 7. Post to GitHub (PR mode, optional)

Ask the developer which findings to post, by number, and whether to adjust any wording. Post nothing without an explicit selection.

Post the selection as **one pending review**:

- Omit `event`, so the review stays pending and is visible only to the developer until they submit it on GitHub.
- Set `commit_id` to the head SHA that was reviewed.
- Inline comments use `path`, `line`, and `side` (`RIGHT` for added or unchanged lines, `LEFT` for removed lines), plus `start_line` for a range. Only lines inside the diff hunks can carry an inline comment.
- Findings without a valid diff line go into the review body.
- GitHub allows one pending review per user per PR. If one already exists, tell the developer instead of creating another.

Comments are posted under the developer's name. Write them the way the team does: collaborative and specific ("What do you think of…", "Should we…"), with severity labels, and without mentioning that they were generated.

Afterwards, share the link to the PR's files tab so the developer can review and submit the comments.

## Important

- The review is read-only: never edit code, commit, push, or check out branches.
- Never submit, approve, or request changes on a PR. Only create a pending review, and only after the developer has selected the findings.
- Do not run Gradle checks — CI runs them. If a finding depends on a check result, suggest the `android-check` skill.
- When unsure whether something is a problem, ask it as a question instead of asserting it.
