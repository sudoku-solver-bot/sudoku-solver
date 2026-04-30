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

## Git Flow (STRICT)

Every task follows this flow. No exceptions.

### Step 1: Sync Master

```bash
cd /home/claw1/repos/sudoku-solver
git checkout master
git pull --ff-only origin master
```

### Step 2: Create GitHub Issue (if not exists)

```bash
gh issue create \
  --title "feat: Short description" \
  --body "## What
Description of what to implement.

## Why
Why this matters.

## Acceptance Criteria
- [ ] Criterion 1
- [ ] Criterion 2
"
```

### Step 3: Create Feature Branch

Branch naming:
- `feat/<description>` — New features
- `fix/<description>` — Bug fixes
- `refactor/<description>` — Refactoring
- `test/<description>` — Test additions
- `docs/<description>` — Documentation

```bash
git checkout -b feat/my-feature
```

### Step 4: Implement

- Read relevant code first
- Make focused, minimal changes
- Follow existing patterns in the codebase
- Add/update tests for any changed logic

### Step 5: Verify Locally

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

### Step 6: Commit

Conventional commits:
```
feat: add XYZ-wing tutorial content
fix: correct cell selection offset on mobile
test: add E2E tests for daily challenge flow
refactor: extract shared number pad component
docs: update API documentation for hint endpoint
```

```bash
git add -A
git commit -m "feat: short description"
```

### Step 7: Push and Create PR

```bash
# Push to fork (safer than origin)
git push -u fork feat/my-feature

# Create PR against origin/master
gh pr create \
  --repo sudoku-solver-bot/sudoku-solver \
  --head novaclawhk:feat/my-feature \
  --base master \
  --title "feat: short description" \
  --body "Closes #XX

## Changes
- Change 1
- Change 2

## Testing
- [ ] All tests pass (\`./gradlew test\`)
- [ ] Frontend builds (\`npm run build\`)
- [ ] Manually tested on local deployment
"
```

### Step 8: Verify CI

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
