---
name: sudoku-reviewer
description: Review pull requests for the sudoku-solver project. Checks code quality, test coverage, merge conflicts, and CI status. Resolves merge conflicts. Use when asked to review a PR, check PR status, resolve conflicts, or approve/merge PRs.
---

# Sudoku Reviewer

PR review and merge management for sudoku-solver.

## Project Context

- **Repo:** `/home/claw1/repos/sudoku-solver`
- **Remotes:** `origin` = `sudoku-solver-bot/sudoku-solver`, `fork` = `novaclawhk/sudoku-solver`
- **CI:** GitHub Actions
- **Branch protection:** master has CI + review requirements

## Review Workflow

### Step 1: Fetch PR List

```bash
cd /home/claw1/repos/sudoku-solver
git fetch origin

# List open PRs
gh pr list --repo sudoku-solver-bot/sudoku-solver --state open

# Or get a specific PR
gh pr view <NUMBER> --repo sudoku-solver-bot/sudoku-solver
```

### Step 2: Checkout PR Locally

```bash
gh pr checkout <NUMBER> --repo sudoku-solver-bot/sudoku-solver
```

### Step 3: Review Checklist

Go through each item:

**Code Quality:**
- [ ] Code follows existing patterns and style
- [ ] No dead code, unused imports, or TODO hacks
- [ ] Meaningful variable/function names
- [ ] No hardcoded values that should be configurable
- [ ] Error handling is appropriate

**Correctness:**
- [ ] Logic matches the PR description and linked issue
- [ ] Edge cases handled (empty input, null, out of bounds)
- [ ] No regressions in existing behavior

**Tests:**
- [ ] New code has tests
- [ ] Existing tests still pass: `./gradlew test`
- [ ] Frontend builds: `cd web-ui && npm run build`

**Security:**
- [ ] No sensitive data exposed (API keys, tokens)
- [ ] Input validation on new endpoints
- [ ] No SQL injection or XSS vectors

**Performance:**
- [ ] No N+1 queries or unnecessary loops
- [ ] No blocking calls in async context

### Step 4: Run CI Checks

```bash
gh pr checks <NUMBER> --repo sudoku-solver-bot/sudoku-solver
```

### Step 5: Submit Review

```bash
# Approve
gh pr review <NUMBER> --repo sudoku-solver-bot/sudoku-solver --approve --body "LGTM! Clean implementation, good test coverage."

# Request changes
gh pr review <NUMBER> --repo sudoku-solver-bot/sudoku-solver --request-changes --body "Please fix:
1. Missing error handling in X
2. Test for edge case Y
"

# Comment only
gh pr review <NUMBER> --repo sudoku-solver-bot/sudoku-solver --comment --body "Nice work, just a few suggestions..."
```

## Resolving Merge Conflicts

### Step 1: Identify Conflicts

```bash
gh pr view <NUMBER> --repo sudoku-solver-bot/sudoku-solver --json mergeable,mergeStateStatus
```

If `mergeable: false` or `MERGE_CONFLICT`, proceed:

### Step 2: Resolve Locally

```bash
# Get latest master
cd /home/claw1/repos/sudoku-solver
git checkout master
git pull --ff-only origin master

# Checkout PR branch
gh pr checkout <NUMBER> --repo sudoku-solver-bot/sudoku-solver

# Merge master into the PR branch
git merge origin/master
```

This will show conflicting files. For each conflict:

```bash
# See conflicts
git diff --name-only --diff-filter=U

# For each conflicting file, resolve manually:
# 1. Open the file
# 2. Look for <<<<<<< HEAD ... ======= ... >>>>>>> markers
# 3. Choose the correct resolution (usually: keep both changes intelligently)
# 4. Remove the conflict markers
```

### Step 3: Verify and Push

```bash
# Run tests after resolving
./gradlew test

# Commit the merge resolution
git add -A
git commit -m "merge: resolve conflicts with master"

# Push back to PR
git push
```

### Step 4: Verify CI Passes

```bash
gh pr checks <NUMBER> --repo sudoku-solver-bot/sudoku-solver
```

## Merging a PR

Only after:
- CI passes (green)
- Review approved
- No merge conflicts

```bash
# Squash merge (preferred for clean history)
gh pr merge <NUMBER> --repo sudoku-solver-bot/sudoku-solver --squash --delete-branch

# If squash not desired, regular merge
gh pr merge <NUMBER> --repo sudoku-solver-bot/sudoku-solver --merge --delete-branch
```

After merge, confirm deployment:

```bash
# Trigger local deploy if needed
# (Remote auto-deploys to Render)
```

## Rules

1. **Never merge without CI green**
2. **Never merge your own PR** unless explicitly told to
3. **Always resolve conflicts on the PR branch**, not on master
4. **Run tests locally** after conflict resolution before pushing
5. **Be constructive** in reviews — explain why, not just what
6. **Use squash merge** by default for cleaner history
