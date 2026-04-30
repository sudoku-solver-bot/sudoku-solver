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
2. Read `memory/sudoku-solver-roadmap.md` for current status
3. Read today's `memory/YYYY-MM-DD.md` if exists
4. Read the specific plan file if one is referenced

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
3. If no high-priority bugs, report "No tasks to implement" and stop
4. **Only implement bugs** — no new features or i18n (BUG SQUASH PHASE)

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

Branch naming:
- `feat/<description>` — New features
- `fix/<description>` — Bug fixes
- `refactor/<description>` — Refactoring
- `test/<description>` — Test additions
- `docs/<description>` — Documentation

```bash
git checkout -b feat/my-feature
```

### Step 5: Implement

- Read relevant code first
- Make focused, minimal changes
- Follow existing patterns in the codebase
- Add/update tests for any changed logic

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

1. **Never push directly to master** — always use branches and PRs
2. **One PR per task** — don't bundle unrelated changes
3. **Tests must pass** before creating PR
4. **Keep PRs small** — under 500 lines changed when possible
5. **Follow existing patterns** — match code style, naming, architecture
6. **Wait for review** — don't merge your own PRs unless explicitly told to
