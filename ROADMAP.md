# Sudoku Dojo — Roadmap

**Last updated:** 2026-06-30 (updated 09:01)
**Repository:** [sudoku-solver-bot/sudoku-solver](https://github.com/sudoku-solver-bot/sudoku-solver)

---

## Current Phase: Test Automation & Infrastructure Stabilization (June 2026)

TS solver at full parity. All 20 eliminators ported. HintGenerator, PuzzleGenerator, SolverWithSteps all in TypeScript. Focus shifted to test coverage, infrastructure hardening, and process automation.

### ✅ Completed (June 9–29)

#### Test Automation (from #678, #679, #680)
- ✅ **#741** — Difficulty generation tests (#699)
- ✅ **#742** — NumberBar + highlight priority + tutorial highlights + themes audit (#707, #708)
- ✅ **#740** — Accessibility modes audit (#706)
- ✅ **#739** — Keyboard navigation audit (#705)
- ✅ **#738** — Empty cell rendering + ARIA labels audit (#704)
- ✅ **#737** — Tutorial progress + saved puzzles tests (#712)
- ✅ **#736** — Preference persistence tests (#711)
- ✅ **#735** — Game state save/restore tests (#710)
- ✅ **#734** — localStorage key existence checks (#709)
- ✅ **#747** — API rate limiting test (#700)
- ✅ **#746** — HintGenerator test harness with tutorial verification (#743, #744, #745)

#### Infrastructure
- ✅ **#754** — Fix StartLimitIntervalSec in wrong systemd section
- ✅ **#751** — Normalize getHintForPuzzle response (client-first + server fallback)
- ✅ **#748** — Add SolverConfig + static convenience methods to SolverWithSteps
- ✅ **#749** — ADR-0008: Weekly Issue Board Audit (process established)
- ✅ **#752** — Agent session-launch repo scoping guard (architecture)

### 🟢 No Open Issues

The issue board is completely clean:
- **sudoku-solver-bot/sudoku-solver**: 0 open issues
- **novaclawhk/video-pipeline**: 1 open enhancement (see below)

---

## Project Overview

### Stack
- **Backend:** Kotlin 2.1 (Ktor) + JUnit 5 — `web/`, `solver/`, `board/` modules
- **Frontend:** Vue 3 (Composition API) + Vite 5 + Vitest — `web-ui/`
- **TS Solver:** `packages/solver/` — standalone TypeScript solver package (20 eliminators, HintGenerator, PuzzleGenerator, SolverWithSteps)
- **Infrastructure:** Systemd service on VPS, GitHub Actions CI
- **Servers:** Local `http://localhost:25321` | Production `https://sudoku-solver-r5y8.onrender.com`

### Test Coverage
- **71 frontend tests** (Vitest)
- **51 backend tests** (Kotlin/JUnit)
- **402 total test cases** across all suites
- HintGenerator tutorial verification, rate limiting, localStorage persistence, accessibility, keyboard nav all covered

### Key Links
- **Issues:** https://github.com/sudoku-solver-bot/sudoku-solver/issues
- **PRs:** https://github.com/sudoku-solver-bot/sudoku-solver/pulls
- **CI:** https://github.com/sudoku-solver-bot/sudoku-solver/actions

---

## Secondary Project: video-pipeline

**Repo:** `novaclawhk/video-pipeline` | **Stack:** Python + faster-whisper

### Open Issues

| # | Title | Priority | Status |
|---|-------|----------|--------|
| # | Title | Priority | Status |
|---|-------|----------|--------|
| #181 | 4-copy workspace consolidation (ADR-0004) | 🟡 Medium | Proposed — pending review |
| #177 | Comprehensive USER.md guide | 🟡 Medium | Plan #183 created, coder-assigned |
| #179 | DEVELOPER.md/DEVOPS.md missing from pip package | 🟡 Medium | Plan #182 created, coder-assigned |

### Recent Milestones
- ✅ **Task-queue scheduler epic (#154)** — All 6 PRs merged. Migrated from video-sequential to resource-aware task-queue dispatcher with dependency gating, resource capacity management, subprocess executor, and full integration into `process_slug()`.
- ✅ **Auto-resume after crash (#161)** — All 3 sub-plans shipped: transcribe checkpoint (#167), crash retry flag (#168), systemd PID tracking (#169). PRs #171, #172, #173 merged.
- ✅ **#175** — Download duration marker (PR #176 merged). ADR-0003 accepted.
- ✅ **#153** — Env-specific Playwright BDD test failures (documented and closed).
- ✅ **CLI overhaul (#134 epic)** — Full command groups (config, auth, system, videos) with CliRunner-based tests, deploy health checks, rollback, stale unit cleanup.
- ✅ **Old bugs closed** — #101 (duplicate workers), #97 (VAD filter), #143 (stale uvicorn), #142 (systemd unit tests) all fixed.

---

## Agent Responsibilities

| Agent | GitHub Role |
|-------|-------------|
| **Planner** | Maintains this roadmap. Creates `plan` and `chore` issues. |
| **Coder** | Picks up `plan` issues, creates PRs. |
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
| `epic` | Planner | Umbrella issue linking sub-issues |
| `chore` | Planner, Deployer | Maintenance, infrastructure |
| `priority:high/medium/low` | All | Triage level |
