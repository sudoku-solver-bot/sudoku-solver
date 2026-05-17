# Sudoku Dojo — Roadmap

**Last updated:** 2026-05-17
**Repository:** [sudoku-solver-bot/sudoku-solver](https://github.com/sudoku-solver-bot/sudoku-solver)
**Live:** https://sudoku-solver-r5y8.onrender.com

---

## Current Phase: Polish & Bug Fix (May 2026)

After 304+ PRs merged and the full feature set shipped, focus is on quality and stability.

### Active Goals

- [ ] Fix all 🟠+ bugs filed by tester
- [ ] Quiz consistency audit — cross-belt duplicates, wrong answers, technique mismatches
- [ ] Hint quality improvement — technique-specific hints instead of generic "Scanning"
- [ ] Server stability — eliminate logback crash loop (PR #216 needs merge + deploy)

### In Progress

| Issue | Title | Priority | Owner |
|-------|-------|----------|-------|
| #318 | Fix local server crash loop (logback) | 🔴 high | coder |
| #319 | Add OpenAPI/Swagger docs | 🟡 medium | coder |
| #320 | Puzzle sharing via URL | 🟡 medium | coder |
| #321 | Keyboard shortcut help overlay | 🟢 low | coder |
| #24 | Branch protection config | 🟢 low | infra |

### Recently Completed

- ✅ 304+ PRs merged — full feature set (May 8)
- ✅ All known bugs closed (May 8)
- ✅ 20 tutorials with quizzes across 9 belt levels
- ✅ 20-language i18n support
- ✅ PWA with offline support
- ✅ Comprehensive E2E test suite

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
