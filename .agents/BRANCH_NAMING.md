# Branch Naming Conventions

This document outlines branch naming conventions and base branch selection for the Adyen Android SDK repository.

## Branch Prefixes

Branch names determine automatic labeling and release notes requirements:

| Prefix | Auto Label | Release Notes Required |
|--------|------------|------------------------|
| `feature/` | Feature | ✅ Yes |
| `fix/` | Fix | ✅ Yes |
| `chore/` | Chore | ❌ No |
| `renovate/` | Dependencies | ❌ No |

**Examples:**
- `feature/googlepay-issuer-country-config`
- `fix/3ds2-cancel-button`
- `chore/v6-sdk-data-implementation`

## Base Branches

| Branch | Purpose |
|--------|---------|
| `main` | Current development branch (v6) |
| `v5` | Previous release, maintenance only |

**Guidelines:**
- Most work targets `main`
- PRs to `v5` only for essential fixes or features required for v5 maintenance
- If unsure, ask the developer or check the base branch of an existing related branch
- For an already created branch, check its base branch to understand the target

## Branch Chaining

When a task requires multiple branches/PRs (e.g., large features split into phases):

**Naming convention:**
- Keep the first prefix the same
- Use descriptive endings (1-3 words)
- **Do NOT use numbers**

**Example chain:**
```
chore/drop-in-payment-method-list
chore/drop-in-payment-method-list-default
chore/drop-in-payment-method-list-ui
chore/drop-in-payment-method-list-stored
chore/drop-in-payment-method-list-stored-manage
```

**When to chain:**
- Large features that benefit from incremental review
- Each phase of an implementation plan
- When sub-phases are too big for a single PR

## Version Prefix in Branch Names

Including version in the branch name is optional but helpful:
- `chore/v6-sdk-data-implementation`
- `fix/v5-component-being-recreated-on-lifecycle`

This helps identify which version the work is for at a glance.
