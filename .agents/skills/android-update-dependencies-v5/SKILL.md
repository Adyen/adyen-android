---
name: android-update-dependencies-v5
description: Work a Renovate dependency update PR on the v5 branch.
---

# android-update-dependencies-v5

Bring a Renovate "All v5 dependencies" PR to a mergeable state, keeping v5 as close to `main` as possible while preserving the differences that are deliberate.

## Usage

Invoke this skill when working a Renovate dependency PR whose base is `v5`. Renovate opens one every three months.

**Only applies to `v5` and branches based on it.** Check with `git merge-base --is-ancestor origin/v5 HEAD`. If you are on `main`, ignore this file.

The deliberate differences between the branches are recorded in `DIFFERENCES.md` next to this file. Read it before changing anything, and keep it current — see step 9.

**The two files divide cleanly: this one describes the process, `DIFFERENCES.md` holds the list.** Never name an individual difference here, however convenient it seems mid-step. A specific written into a step then has to be maintained in two places, and this is the copy that gets forgotten — leaving a step that names one difference where several now exist, which reads as an exhaustive list and silently reverts the rest. Point at `DIFFERENCES.md` and verify against it instead.

### General guidance lives on main

Commit, branch, PR and check conventions are maintained on `main` only and are not duplicated here. Read them from git rather than checking `main` out:

```
git fetch origin main
git ls-tree -r origin/main --name-only | rg '^\.agents/|^AGENTS.md'
git show origin/main:<path>
```

Enumerate them dynamically — a hardcoded list will go stale. Where any of them states a value that differs on v5, such as `minSdk`, take v5's value from the repository rather than the one written there.

### Read the previous update for precedent

```
gh pr list --base v5 --label Dependencies --state merged --limit 3
```

Its commits show the shape of the work. Treat them as **precedent, not policy** — `DIFFERENCES.md` is the only authority on what is deliberate.

## The three rules

1. **v5 must never exceed `main`.** `main` owns and validates the toolchain and build system. Anything Renovate raises above `main` comes back down to `main`'s value. Trailing `main` is fine and often expected; see the release-age note in `DIFFERENCES.md`.
2. **v5 must keep building at its `min-sdk`.** Read the value from `gradle/libs.versions.toml`; never assume it.
3. **v5 must not diverge on its own.** Every change is either ported from `main` unchanged, or a deliberate divergence that is written down. If something cannot be ported as-is, stop and say so plainly, explain why, and get the user's decision before adapting it. Record the outcome in `DIFFERENCES.md`. Never invent a v5-only solution silently.

Apply rule 1 first, then rule 2. Matching `main` pulls in versions that need a higher `minSdk`, so the ceiling has to be applied afterwards.

## Steps

### 1. Sync build logic and toolchain from main

**Unconditional.** Do this even when v5 builds fine and nothing is failing. A green build is not evidence of alignment, and drift is only ever cheaper to pay down early.

```
git fetch origin main
git checkout origin/main -- build-logic/
```

That copy is wholesale, so it reverts every deliberate difference inside `build-logic/`. Re-apply the ones `DIFFERENCES.md` lists, then check the result against it:

```
git diff origin/main HEAD -- build-logic/
```

Every line this prints must match an entry in `DIFFERENCES.md`, and every `build-logic/` entry there must appear in this diff. Treat a mismatch in either direction as a problem: an unlisted change is drift, and a listed one that is missing was just reverted by the copy. Nothing in the build fails either way, so this diff is the only thing that catches it.

Then copy `main`'s toolchain versions into the catalog: AGP, Gradle, Kotlin, KSP, Dokka, detekt, ktlint, kover, sonarqube, binary-compatibility-validator, hilt.

**Scope: everything that configures the build rather than implementing the product.** That is the root and per-module `build.gradle` and `build.gradle.kts`, `settings.gradle`, `gradle.properties`, `gradle/` including the catalog and wrapper, `build-logic/`, `config/`, `scripts/`, `.github/`, `.editorconfig`, and any other `.gradle`, `.kts`, `.properties`, `.toml`, `.sh`, `.py`, `.yml` or dotfile outside `src/`.

**Also look for files that exist on `main` and not on v5 at all.** A new script, workflow or convention plugin will not show up in a diff of shared files:

```
comm -23 <(git ls-tree -r origin/main --name-only | sort) <(git ls-tree -r HEAD --name-only | sort)
```

Review every build-related entry: port it, or add it to `DIFFERENCES.md` with the reason. Run the reverse direction too, to catch anything v5 has that `main` does not.

Cross-check the result against `DIFFERENCES.md`, and be careful how: a filename can appear there as part of the *reason* for some other entry while having no entry of its own, so grepping for it gives a false pass. Confirm each path is genuinely listed as a difference in its own right.

Anything not in `DIFFERENCES.md` must match `main`. If you find a difference that is not listed, port it — do not justify it.

**Before porting a workflow, check it can actually run on v5.** Scheduled workflows only run from the default branch, and a reusable workflow with no caller is dead weight.

### 2. Downgrade anything above main

```
git diff origin/main HEAD -- gradle/libs.versions.toml settings.gradle gradle/wrapper/
git diff origin/main HEAD -I'^[[:space:]]*uses: ' -- .github/workflows
```

Use `git diff origin/main HEAD`, not `origin/main...HEAD`. The three-dot form compares against the merge base, which is far behind both branches and will mislead you badly.

**`.github/workflows/` and `settings.gradle` are in scope, not just the catalog.** GitHub Action versions are easy to miss because they do not live in the version catalog. Take `main`'s exact digest and version comment so the pins match what `main` runs.

The `-I` flag hides hunks whose every changed line matches, which is the easiest way to separate real differences from pinned-version churn. It takes a POSIX regex: `\s` silently matches nothing, use `[[:space:]]`.

### 3. Pin anything that breaches min-sdk

Derive the ceilings; never copy a stored table. For every Android artifact the PR bumps, read `minSdkVersion` out of the published AAR and compare it against the project's `min-sdk`:

```
https://dl.google.com/dl/android/maven2/<group-path>/<name>/<version>/<name>-<version>.aar
```

Fall back to `https://repo1.maven.org/maven2/...`, then `unzip -p <aar> AndroidManifest.xml | grep minSdkVersion`.

- KMP artifacts have no plain AAR — use the `-android` suffix, for example `ui-android` or `material3-android`.
- For the Compose BOM, fetch its POM to see what it pins, then check those artifacts.
- Check first-order transitives too. Gradle resolves the highest version in the graph, so a compliant direct dependency can still drag in a non-compliant one.
- A ceiling is not reliably "one minor below `main`". Walk versions down until the manifest satisfies `min-sdk`; do not assume.

If `min-sdk` ever changes, every ceiling has to be re-derived.

### 4. Fix toolchain fallout

Compile and configuration failures caused by the new toolchain. This is the step that needs judgement; the rest is mechanical.

Prefer `main`'s solution over inventing one. If `main` has already crossed the same toolchain boundary, it has solved the problem — find out how before debugging from scratch. Inventing a v5-only fix is a rule 3 violation.

### 5. Regenerate the API dump

```
./gradlew apiDump
```

Run it every time, whether or not Kotlin moved.

A Kotlin major produces a large dump diff that is **expected and not a real API change**, driven by codegen rather than by source. Known categories: parcelize emitting `CREATOR` as an anonymous object instead of a named `$Creator` class, parcelize marking `describeContents` and `writeToParcel` final, interface methods with default bodies becoming real JVM default methods under `-Xjvm-default=all-compatibility`, and `get$default` bridges appearing for interface methods with default arguments.

Before accepting a large dump diff, confirm nothing actually left the API surface:

- no `public static final field CREATOR` removed
- every member that became `final` belongs to a `final class`, so nobody could have overridden it
- `$DefaultImpls` classes retained — that is what keeps already-compiled merchant code linking
- no change to any method's parameters or return type
- no class removed other than generated `$Creator` helpers

Account for every removed line. If something does not fit a known codegen category, it is a real API change and needs a decision, not a dump.

### 6. Fix tests and the example app

### 7. Regenerate dependency verification metadata

Do this **last**, once versions are final, as its own commit.

```
./gradlew --write-verification-metadata sha256 resolveDependencies assembleDebug --refresh-dependencies
```

`--refresh-dependencies` matters. The writer only records what the build actually resolves, so with a warm cache it silently misses artifacts and the failure surfaces later as a verification error on a cold CI runner.

### 8. Verify

```
./gradlew apiCheck
./gradlew check
./gradlew dokkaGeneratePublicationHtml
python3 scripts/validate_dependencies_for_release_notes.py
```

The release-notes validator only checks *added* dependencies, so stale entries for removed ones are harmless.

Then re-run the rule-1 comparison from step 2 and confirm nothing on v5 exceeds `main`.

If you touched secrets or reusable workflows, cross-check them: every secret a reusable workflow references must be declared under `on.workflow_call.secrets`, and every caller must pass exactly what the callee declares. A reference that resolves to an empty string fails silently — an empty `cache-encryption-key`, for instance, simply disables configuration-cache reuse rather than erroring.

### 9. Update this skill and DIFFERENCES.md

Before reporting, fold anything durable back in:

- **A new deliberate difference** — add it to `DIFFERENCES.md` with its reason and the PR it was introduced in. It belongs there and nowhere else: do not also write it into a step above, even if that is the step it will bite.
- **A difference that is now resolved** — remove the entry. A stale entry is worse than a missing one, because the next reader will preserve something that no longer needs preserving.
- **The process was wrong or incomplete** — fix this file, so the same gap is not rediscovered next cycle.

Only durable rules and decisions belong in either file. Version numbers, ceilings and accounts of a particular quarter belong in the commits.

### 10. Report and get approval

Present a final report to the user and **wait for explicit approval before the PR is considered ready**:

- the state of the differences before this PR and after it
- what was **aligned** with `main`, and why it was safe
- what was **skipped**, and why
- what was **modified** rather than ported as-is, with the reasoning and the user decision behind each — see rule 3
- anything that could not be verified

Ask for a final review. Do not treat the work as finished until the user confirms.

## Commits and PR

Follow `main`'s commit and PR skills. Specific to this work:

- One commit per concern, in the order of the steps above. Do not mix the sync, the downgrades, the ceiling pins and the API dump.
- Explain anything that looks removable. A future maintainer deletes what they cannot explain, so the reasoning belongs in the commit message rather than in a chat log.
- **Never edit Renovate's PR description, and do not add anything to the PR body.** Renovate regenerates it whenever a rebase is triggered, and it carries a `renovate-debug` block. Its version table describes what was *available*, not what landed.
- Anything worth keeping belongs in a commit message, or in `DIFFERENCES.md` if it is a decision.
