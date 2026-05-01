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

**⚠️ CRITICAL: Only approve if the PR does what it claims.**
If you spot ANY issues, add a comment — do NOT approve.

First, read the PR description and linked issue to understand what the PR claims to do.

**Does it do what it claims?**
- [ ] PR description matches the actual changes
- [ ] Linked issue is actually addressed
- [ ] All claimed features/fixes are present in the diff
- [ ] No unrelated changes snuck in

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

### Step 3b: If Issues Found

**Add a comment to the PR with specific, actionable feedback:**

```bash
# Comment with inline issues
gh pr comment <NUMBER> --repo sudoku-solver-bot/sudoku-solver --body "## Review Findings

### ❌ Issue 1: [description]
File: \`path/to/file.ext:123\`
Problem: [what's wrong]
Suggestion: [how to fix]

### ⚠️ Issue 2: [description]
..."
```

**Or request changes formally:**

```bash
gh pr review <NUMBER> --repo sudoku-solver-bot/sudoku-solver --request-changes --body "Please fix:
1. [Issue 1]
2. [Issue 2]
"
```

**Guidelines:**
- Be specific: reference file names and line numbers
- Be constructive: explain the problem AND suggest a fix
- Categorize: ❌ blocking (must fix), ⚠️ suggestion (should fix), 💡 nit (optional)
- If the PR doesn't actually fix the linked issue → ❌ blocking, explain what's missing

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

1. **Only approve if the PR does what it claims** — verify against PR description and linked issue
2. **Add comments for ANY issues found** — don't silently approve and hope for the best
3. **Never merge without CI green**
4. **Never merge your own PR** unless explicitly told to
5. **Always resolve conflicts on the PR branch**, not on master
6. **Run tests locally** after conflict resolution before pushing
7. **Be constructive** in reviews — explain why, not just what
8. **Use squash merge** by default for cleaner history
9. **If the PR doesn't fix the linked issue, request changes** with a clear explanation
