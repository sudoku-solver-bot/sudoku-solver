---
name: sudoku-coder
description: Implement coding tasks for the sudoku-solver project following plans from the planner agent. Follows standard git flow: create GitHub issue → feature branch → implement → test → create PR. Use when asked to implement a feature, fix a bug, or execute a plan for sudoku-solver.
---

# Sudoku Coder

Implementation agent for sudoku-solver. Follows plans and standard git flow.

## Project Setup

- **Repo:** `/home/claw1/repos/sudoku-solver`
- **Remotes:** `origin` = `sudoku-solver-bot/sudoku-solver`, `fork` = `novaclawhk/sudoku-solver`
- **Stack:** Kotlin 2.1 (Ktor) + Vue 3 + Vite
- **JDK:** 21
- **CI:** GitHub Actions (tests on push/PR)
- **Deploy:** Auto-deploys to Render on merge to master

## Context Loading (Before Every Task)

1. Read `CLAUDE.md` in repo root for architecture
2. Check for open GitHub issues: `gh issue list --repo sudoku-solver-bot/sudoku-solver --state open`
3. Read the specific plan issue if one is selected
4. **Check for in-progress work** — read `memory/coder-progress.md` if it exists (see Progress Tracking below)

## Priority: Fix Existing PRs First

Before picking up new work, **always check for pending PRs from novaclawhk**:

```bash
cd /home/claw1/repos/sudoku-solver

# Check our open PRs
gh pr list --repo sudoku-solver-bot/sudoku-solver --state open --author novaclawhk

# Check for merge conflicts
for pr in $(gh pr list --repo sudoku-solver-bot/sudoku-solver --state open --author novaclawhk --json number -q '.[].number'); do
  echo "PR #$pr:"
  gh pr view $pr --repo sudoku-solver-bot/sudoku-solver --json mergeable,statusCheckRollup,title
  gh pr checks $pr --repo sudoku-solver-bot/sudoku-solver 2>/dev/null || true
done
```

### If a PR has reviewer comments (not yet approved):
1. Checkout the branch: `gh pr checkout <NUMBER>`
2. Read the review comments:
   ```bash
   gh api repos/sudoku-solver-bot/sudoku-solver/pulls/<NUMBER>/reviews
   gh api repos/sudoku-solver-bot/sudoku-solver/pulls/<NUMBER>/comments
   gh pr view <NUMBER> --repo sudoku-solver-bot/sudoku-solver --comments
   ```
3. Address each comment:
   - ❌ Blocking issues → fix them
   - ⚠️ Suggestions → implement if reasonable
   - 💡 Nits → fix if quick, skip if not
4. Commit fixes: `git commit -m "fix: address review feedback for #<NUMBER>"`
5. Push: `git push`
6. Reply to the review comment explaining what was changed:
   ```bash
   gh pr comment <NUMBER> --repo sudoku-solver-bot/sudoku-solver --body "Addressed review feedback:
   - Fixed ❌ [issue] by [what you did]
   - Applied ⚠️ [suggestion] by [what you did]
   "
   ```
7. Verify CI passes

**Priority: Address reviewer comments BEFORE fixing merge conflicts or CI failures.**

### If a PR has merge conflicts:
1. Checkout the PR branch: `gh pr checkout <NUMBER>`
2. Rebase on master: `git rebase origin/master`
3. Resolve conflicts manually
4. Run tests: `./gradlew test`
5. Force push: `git push --force-with-lease`
6. Verify CI passes

### If a PR has failing CI:
1. Checkout the branch
2. Read the CI failure: `gh pr checks <NUMBER>`
3. Fix the issue
4. Commit and push
5. Verify CI passes

### If a PR is partially implemented:
1. Checkout the branch
2. Read the linked issue
3. Continue the implementation
4. Commit and push incremental progress
5. Verify CI passes

**Only pick up new issues after all existing PRs are clean (no conflicts, CI green, fully implemented).**

## Finding Work (GitHub Issues)

The coder picks up work from **open GitHub issues**, not from the roadmap.

### Step 1: Find the next task

```bash
cd /home/claw1/repos/sudoku-solver

# Priority order:
# 1. Plan issues (labeled 'plan') — these have implementation details
# 2. Bug issues (labeled 'bug') — fix directly if plan exists, or skip
# 3. Issues with priority:high first, then priority:medium, then priority:low

# Check for plan issues first (best candidates)
gh issue list --repo sudoku-solver-bot/sudoku-solver --state open --label plan

# Check high priority bugs
gh issue list --repo sudoku-solver-bot/sudoku-solver --state open --label "priority:high"

# Check all open issues
gh issue list --repo sudoku-solver-bot/sudoku-solver --state open

# Read the issue details
gh issue view <NUMBER> --repo sudoku-solver-bot/sudoku-solver
```

**How to pick:**
1. Look for `plan:` issues first — these have files to change, steps, and testing instructions
2. If no plans, look for `bug:` issues with `priority:high`
3. If no high-priority bugs, look for `bug:` issues with `priority:medium`
4. If no bugs at all, pick up the next available issue (features, docs, i18n, etc.) by priority
5. **Bug fixes take priority**, but when there are no bugs to fix, work on other open issues normally

### Step 2: Read the plan/bug carefully

```bash
# View full issue
gh issue view <NUMBER> --repo sudoku-solver-bot/sudoku-solver

# If it's a plan issue, it will have:
# - Files to Change
# - Implementation Steps
# - Testing instructions
# Follow those steps exactly.
```

## Git Flow (STRICT)

Every task follows this flow. No exceptions.

### Step 3: Sync Master

```bash
cd /home/claw1/repos/sudoku-solver
git checkout master
git pull --ff-only origin master
```

### Step 4: Create Feature Branch

Branch naming (default to `fix/` for bug-focused work):
- `fix/<description>` — Bug fixes (default)
- `feat/<description>` — New features
- `refactor/<description>` — Refactoring
- `test/<description>` — Test additions
- `docs/<description>` — Documentation

**Note:** Most work comes from GitHub issues. Use `fix/<description>` for any issue-driven work to keep things consistent.

```bash
git checkout -b feat/my-feature
```

### Step 5: Implement

- Read relevant code first
- Make focused, minimal changes
- Follow existing patterns in the codebase
- Add/update tests for any changed logic

**If the issue is large, break it into multiple PRs:**
1. Read the full plan/bug
2. Identify independent parts that can be shipped separately
3. Create a checklist comment on the issue:
   ```bash
   gh issue comment <NUMBER> --repo sudoku-solver-bot/sudoku-solver --body "Breaking into multiple PRs:
   - [ ] Part 1: <description>
   - [ ] Part 2: <description>
   - [ ] Part 3: <description>"
   ```
4. Implement and ship each part as its own PR
5. Reference the issue in each PR: "Part 1/N of #<NUMBER>"

**If a single PR is taking a long time, commit incrementally:**
1. Make a clean, working subset of changes
2. Run tests to confirm nothing is broken: `./gradlew test`
3. Build to confirm: `cd web-ui && npm run build && cd ..`
4. Commit: `git commit -m "fix: partial implementation of #<NUMBER> (step 1)"`
5. Push: `git push`
6. Continue with the next subset
7. Each intermediate commit should:
   - Compile and build without errors
   - Pass all existing tests
   - Not break any existing functionality
   - Be clean enough that it could ship as-is if needed

### Step 6: Verify Locally

```bash
# Run all tests
./gradlew test

# Build frontend
cd web-ui && npm run build && cd ..

# Build backend
./gradlew :web:installDist --no-daemon

# Quick smoke test
curl -sf http://localhost:25321/api/health | python3 -m json.tool
```

### Step 7: Commit

Conventional commits (reference the issue number):
```
fix: resolve logback crash from LayoutEncoder (#221)
bug: correct tutorial naked-single puzzle description (#225)
```

```bash
git add -A
git commit -m "fix: short description (Closes #<ISSUE_NUMBER>)"
```

### Step 8: Push and Create PR

```bash
# Push to fork (safer than origin)
git push -u fork fix/<description>

# Create PR referencing the issue
gh pr create --repo sudoku-solver-bot/sudoku-solver \
  --head novaclawhk:fix/<description> \
  --base master \
  --title "fix: short description" \
  --body "Closes #<ISSUE_NUMBER>

## Changes
- Change 1
- Change 2

## Testing
- [ ] All tests pass (\`./gradlew test\`)
- [ ] Frontend builds (\`npm run build\`)
- [ ] Manually tested on local deployment
"
```

### Step 9: Verify CI

```bash
gh pr checks <PR_NUMBER>
```

If CI fails, fix and push again.

## Key Architecture Notes

### Backend (Kotlin)
- Board = 81-element IntArray of 9-bit candidate bitmasks
- 17 CandidateEliminator implementations
- Solver = backtracking with MRV heuristic
- Ktor routes in `web/src/main/kotlin/will/sudoku/web/`
- Tests: `kotlin/src/test/` with `.question`/`.solution` file pairs

### Frontend (Vue 3)
- 14 components in `web-ui/src/`
- State: localStorage (no backend auth)
- Builds into `web/src/main/resources/static/`

### Adding a New Eliminator
1. Implement `CandidateEliminator` in `kotlin/src/main/`
2. Register in `Solver.kt`
3. Add `.question`/`.solution` test files
4. Add tutorial in `lessons.json`

### Adding a New API Endpoint
1. Create route file in `web/src/main/kotlin/will/sudoku/web/`
2. Register in `Application.kt`
3. Add frontend API call in `web-ui/src/`

### Adding a Frontend Component
1. Create `.vue` file in `web-ui/src/components/`
2. Import in parent component
3. Build: `cd web-ui && npm run build`

## Rules

1. **Fix existing PRs first** — merge conflicts, failing CI, partial implementations
2. **Never push directly to master** — always use branches and PRs
3. **One PR per task** — don't bundle unrelated changes
4. **Break large issues into multiple PRs** — ship incrementally
5. **Commit incrementally on long PRs** — each commit should build and pass tests
6. **Tests must pass** before pushing
7. **Keep PRs small** — under 500 lines changed when possible
8. **Follow existing patterns** — match code style, naming, architecture
9. **Wait for review** — don't merge your own PRs unless explicitly told to
