# Sudoku Dojo — Roadmap

**Last updated:** 2026-05-18
**Repository:** [sudoku-solver-bot/sudoku-solver](https://github.com/sudoku-solver-bot/sudoku-solver)
**Live:** https://sudoku-solver-r5y8.onrender.com

---

## Current Phase: Polish & Bug Fix (May 2026)

After 304+ PRs merged and the full feature set shipped, focus is on quality and stability.

### Active Goals

- [ ] **Architecture Debt Cleanup (Epic #434)** — remove 6,000+ LOC dead code, add integration tests, build-time validation
- [ ] Fix all 🔴 high-priority bugs (quiz data, practice puzzles, hint accuracy)
- [ ] Quiz consistency — add options/correctAnswer to all belt quizzes, fix cross-belt duplicates
- [ ] Tutorial quality — fix broken example puzzles, validate lesson data at build time
- [ ] Server reliability — deploy race condition fix, systemd rate limiting

### Top Priority Plans (🔴 high)

| Issue | Title | Category |
|-------|-------|----------|
| #433 | Build-time validation for quiz data | Architecture |
| #432 | Build-time validation for tutorial lesson data | Architecture |
| #431 | Ktor integration tests (DailyChallenge, Solve, Validate) | Architecture |
| #430 | Ktor integration tests (TutorialRoutes) | Architecture |
| #428 | Delete com.sudoku.testing package (1,127 LOC) | Architecture |
| #427 | Delete 9 dead solver classes (4,877 LOC) | Architecture |
| #408 | Fix deploy race condition — atomic artifact swap | Infra |
| #407 | Add systemd rate limiting to prevent crash loops | Infra |
| #406 | Add options + correctAnswer to advanced belt quizzes | Quiz |
| #405 | Add options + correctAnswer to intermediate belt quizzes | Quiz |
| #404 | Add options + correctAnswer to beginner belt quizzes | Quiz |
| #403 | Fix 3 advanced tutorial example puzzles | Tutorial |
| #402 | Fix 3 intermediate tutorial example puzzles | Tutorial |
| #399 | Expose gitCommit in health endpoint | Infra |

### Open Bugs

| Issue | Title | Priority |
|-------|-------|----------|
| #418 | Remote serving stale quiz data from initial commit | 🟡 |
| #388 | All belt-level quiz questions have empty/missing explanations | 🟡 |
| #375 | 15 of 34 practice puzzles broken | 🔴 |
| #374 | Daily challenge crashes for certain dates | 🔴 |
| #373 | Celebration and Undo-Redo routes return 404 | 🔴 |
| #367 | Quiz questions reuse same puzzle across different belts | 🟡 |
| #366 | Quiz answerCell points to already-filled cells | 🔴 |

### Recently Completed

- ✅ #318 Fix local server crash loop (May 17)
- ✅ #319 OpenAPI/Swagger docs (May 17)
- ✅ #320 Puzzle sharing via URL (May 17)
- ✅ #321 Keyboard shortcut help overlay (May 17)
- ✅ #24 Branch protection config (May 17)
- ✅ #414 Unit tests for tutorial/quiz validation (May 17)
- ✅ #401 Build-time version injection via Gradle (May 17)
- ✅ #372 Tutorial completion 500 serialization fix (May 17)
- ✅ #394 Quiz missing options/correctAnswer (May 13)
- ✅ #396 6 tutorial example puzzles solvable without taught technique (May 13)
- ✅ 304+ PRs merged — full feature set (May 8)
- ✅ 20 tutorials with quizzes across 9 belt levels
- ✅ 20-language i18n support
- ✅ PWA with offline support

---

## Next Phase: Performance & Polish (TBD)

- Performance benchmarks and optimization
- Visual regression test suite
- Student progress analytics
- Advanced hint explanations with diagrams

---

## Project Overview

### Stack
- **Backend:** Kotlin 2.1 (Ktor) + JUnit 5
- **Frontend:** Vue 3 (Composition API) + Vite 5 + Vitest
- **Infrastructure:** Render.com, GitHub Actions CI
- **Tests:** 389 tests, 0 failures

### Architecture
- 17 elimination algorithms from Naked Single to Death Blossom
- REST API at `/api/v1/*`
- Step-by-step solving for educational use
- Daily challenge with streak tracking

### Key Links
- **Issues:** https://github.com/sudoku-solver-bot/sudoku-solver/issues
- **PRs:** https://github.com/sudoku-solver-bot/sudoku-solver/pulls
- **CI:** https://github.com/sudoku-solver-bot/sudoku-solver/actions

---

## Agent Responsibilities

| Agent | GitHub Role |
|-------|-------------|
| **Planner** | Maintains this roadmap. Creates `plan` and `chore` issues. |
| **Coder** | Picks up `plan` + `bug` issues, creates PRs. |
| **Tester** | Creates `bug` issues with repro steps. Closes bugs after verifying fix. |
| **Reviewer** | Reviews PRs, merges after CI green + approval. |
| **Deployer** | Deploys on merge, reports failures as `chore` issues. |
| **Architect** | Creates `architecture` issues for process improvements. |

---

## Issue Labels

| Label | Used By | Meaning |
|-------|---------|---------|
| `plan` | Planner | Implementation spec ready for coder |
| `bug` | Tester | Reproducible defect with severity |
| `architecture` | Architect | Process/coordination improvement |
| `chore` | Planner, Deployer | Maintenance, infrastructure |
| `priority:high/medium/low` | All | Triage level |
