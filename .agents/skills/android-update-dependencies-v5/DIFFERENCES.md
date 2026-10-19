# Deliberate differences between v5 and main

Everything in the build system should be identical to `main` **except** what is listed here. If you find a difference that is not on this list, port it from `main` rather than justifying it. If it should not be ported, add it here with the reason.

Remove an entry as soon as its difference is resolved. A stale entry is worse than a missing one, because it causes the next reader to preserve something that no longer needs preserving.

The *Introduced* column records the PR that added the entry to this file, which is not necessarily when the difference itself first appeared. Commit hashes are omitted because the branch is rebased.

## Versions and build system

| Difference | Why | Introduced |
| --- | --- | --- |
| `min-sdk` is lower than main's | v5 is still being released and cannot raise it. Every version ceiling below follows from this; if `min-sdk` changes, re-derive them all | #2886 |
| Versions capped by that ceiling | See step 3 of the skill. Derive them from the AAR manifests each time; the set changes as AndroidX publishes | #2886 |
| Versions where `main` is ahead | v5 must not exceed `main`, and does not chase it. Usually the release-age lag below | #2886 |
| Kotlin held below `main`'s language version | Each newer one raises the AGP and Kotlin versions merchants need; moving it is a user decision | #3133 |
| Majors of dependencies that ship to merchants not taken | Rule 4 of the skill | #3133 |
| Catalog entries used only by v6 absent, along with their release-notes list entries | Not used on v5. `material3` stays `[excluded]` there, as only the example app uses it | #2886 |
| `compose-material` has no explicit version | v5 lets the Compose BOM pin material3; `main` pins it directly | #2886 |
| `settings.gradle` lacks `address`, `authentication`, `core`, `ui` | v6-only modules | #2886 |
| The root Dokka aggregation lacks the same four modules | Follows from the above | #2886 |
| `config/detekt/detekt.yml` lacks the Compose relaxations | Compose-motivated configuration is not ported to v5, which has no first-class Compose support | #2886 |
| `config/module/` absent | **Never port it.** The scaffolding is not compatible with v5 | #2886 |
| `.agents/` holds only this skill and `DIFFERENCES.md` | General agent guidance is maintained on `main` and read from there. Do not copy `AGENTS.md` or the other skills across; where they state a value that differs on v5, they would mislead | #2886 |
| `renovate.json`, `.github/ISSUE_TEMPLATE/` and `.github/pull_request_template.md` absent | GitHub and Renovate only read them from `main`, whose `renovate.json` also configures v5's updates | #3133 |
| `[plugins] kotlin = { id = "kotlin" }` | Left on the legacy id deliberately; `main` uses `org.jetbrains.kotlin.jvm` | #2886 |
| `DokkaConventionPlugin` points source links at `tree/v5` | `main` points at `tree/main`. Without this every source link in v5's published documentation resolves to v6 code | #2886 |
| `JavaConventionPlugin` compiles Android modules for Java 11 | Keeps merchant-facing bytecode on 11; tooling follows `jvm-target` (17) as on `main` | #3133 |
| Per-module `build.gradle` and `consumer-rules.pro` files differ throughout | v6 moved packages under `.old`, added the `address`, `authentication`, `core` and `ui` modules and adopted Compose across components, so module dependencies, namespaces and keep rules diverge. Expect around twenty files to differ for this reason alone. This covers only differences that follow from the refactor or serve source that exists on one branch only — anything else in those files is still drift, and must be ported or listed here | #2886 |
| `card` keeps `card-scanning` as `compileOnly` | `main`'s `implementation` belongs to v6's scanner-by-default (COSDK-1195); here it would add card scanning and Play Services Wallet to every card integration | #3133 |
| `compile-sdk` stays at 36 | `main` raised it for lifecycle 2.11, which `min-sdk` rules out; same for the `compose-ui` dependency it added to `components-compose` | #3133 |
| `main`'s lint configuration not applied | It only exempts v6's `old/` code and layouts, so on v5 every warning would fail the build | #3133 |

### Release-age lag

`main`'s `renovate.json` sets `minimumReleaseAge: 14 days`, so each quarterly PR holds back anything published in the fortnight before it was cut; Renovate lists those under *Pending* in the PR body. `main` updates monthly and picks them up sooner, so v5 will always trail on a handful of dependencies.

This is expected and self-correcting — the next quarterly run takes them. Do not chase them individually, and do not lower `minimumReleaseAge` to avoid it.

## Workflows

| Difference | Why | Introduced |
| --- | --- | --- |
| No `merge_group` triggers, no `github.event_name == 'pull_request'` guards, and simpler concurrency groups | v5 does not use merge queues. `main` needs the `pull_request.number \|\| sha` fallback because `head_ref` is empty in a merge group | #2886 |
| `check_v5.yml` in place of `check_main.yml`, and `v5` branch triggers throughout | Branch identity | #2886 |
| `validate_dependencies_for_release_notes.py` compares against `origin/v5` | Branch identity | #3133 |
| No prerelease handling — `create_github_release` has no `prerelease` input, `generate_version_name` has no prerelease outputs and uses `version_name.sh` rather than `version_info.sh` and `version_name_from_branch.sh`, `finalize_release` has no `version_info` job or `omitPrereleaseDuringUpdate`, and `update_release_notes` sets `draft: true` | v5 ships stable releases only; prereleases are a v6 concept | #2886 |
| `update_verification_metadata.yml` has no `push` trigger | Step 7 regenerates the metadata once, when versions are final. On `main` the trigger serves single-dependency branches nobody touches. Kept dispatchable | #2956 |
| `release_nightly_app.yml` absent | v5 has no nightly builds | #2886 |
| `release_acceptance_app.yml` has no `workflow_call` block | That block exists on `main` only so `release_nightly_app.yml` can call it, and v5 does not have that workflow | #2886 |
| `run_ui_tests.yml` absent | v5 is a maintenance branch and does not run instrumentation tests | #2886 |
| `stale_issues.yml` absent | Scheduled workflows run only from the default branch, so a copy here would never execute. Issues are repository-wide and managed from `main` | #2886 |
| `release_snapshot.yml` absent | v5 does not publish snapshots | #2886 |
| `label_pr_size.yml` absent | v5 PRs are not size-labelled | #2886 |
| `deprecate_releases.yml`, `.github/scripts/deprecate-releases.js` and `package.json` absent | The script works purely against the GitHub Releases API and does not read the ref it runs from, so v5 releases are deprecated by running that workflow from `main` | #2886 |
| `ADR/`, `docs/v6/` and `MIGRATION.md` absent | Documentation for v6 | #2886 |
