# Sudoku Dojo — Roadmap

**Last updated:** 2026-06-29
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
- **novaclawhk/video-pipeline**: 2 open bugs (see below)

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
| 101 | Duplicate pipeline workers for same slug | 🔴 High | Triage done — needs plan |
| 97  | VAD filter stops JAV transcription early | 🔴 High | Triage done — needs plan |

Both bugs have detailed analysis with root causes and suggested fixes but need `plan` issues and coder assignment.

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
