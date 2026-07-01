# Sudoku Dojo — Roadmap

**Last updated:** 2026-07-01 (updated 20:00 HKT)
**Repository:** [sudoku-solver-bot/sudoku-solver](https://github.com/sudoku-solver-bot/sudoku-solver)

---

## Current Phase: IMPROVEMENT PHASE (July 2026)

✅ **Phase 3 — Thin-Server Architecture Migration complete.** All ADR-0009 and ADR-0010 sub-issues shipped. Hint generator, step-by-step solving, solver parity corpus, and CI enforcement all delivered. TS solver at full parity with 20 eliminators. 0 open PRs. Production live.

### ✅ Completed

#### Phase 3 — Thin-Server Migration (ADR-0009, ADR-0010)
- ✅ **#757** — Port hint generator to TypeScript (PR #767 merged)
- ✅ **#758** — Port step-by-step solving to TypeScript (PR #768 merged)
- ✅ **#759** — Expand SolverParity corpus 5→50+ puzzles (PR #766 merged)
- ✅ **#761** — Add CI solver parity workflow + README badge (PR #765 merged)
- ✅ **#756** — ADR-0010 closed (all sub-issues shipped)
- ✅ **#755** — ADR-0009 closed (all sub-issues shipped)

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

#### Bug Fixes
- ✅ **#763** — Fix EmptyRectangleCandidateEliminator (incorrect ERI detection via heuristic → correct 2×2 empty rectangle check). Re-enabled in defaults.

#### Infrastructure
- ✅ **#754** — Fix StartLimitIntervalSec in wrong systemd section
- ✅ **#751** — Normalize getHintForPuzzle response (client-first + server fallback)
- ✅ **#748** — Add SolverConfig + static convenience methods to SolverWithSteps
- ✅ **#749** — ADR-0008: Weekly Issue Board Audit (process established)
- ✅ **#752** — Agent session-launch repo scoping guard (architecture)

### 🔵 Sudoku-solver Issues

**0 open issues** — all shipped and closed.

**novaclawhk/video-pipeline**: 7 open issues (see below)

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
| #228 | Move data layer dashboard/ → service/ | 🟡 Medium | Assigned to coder — in progress |
| #229 | Move API routes to service/ | 🟡 Medium | Assigned to coder — in progress |
| #230 | Fix imports (dashboard → service) | 🟡 Medium | Assigned to coder — in progress |
| #231 | Refactor web.py (remove direct DB) | 🔴 High | Assigned to coder — in progress |
| #232 | Clean up dashboard main.py | 🟡 Medium | Assigned to coder — in progress |
| #233 | Update pyproject.toml Phase 2 | 🟡 Medium | Assigned to coder — in progress |
| #205 | ADR-0006 Consistent project structure (tracker) | 🟡 Medium | Architecture — Phases 2-4 pending |

### Recent Milestones
- ✅ **ADR-0006 Phase 1 complete (Jun 30)** — All 6 sub-issues shipped: module renames (dashboard → dashboard/, pipeline_service/ → service/, pipeline/ → processor/), pyproject.toml consolidation, deploy path updates, test reorganization. 11 PRs merged in 2 hours.
- 🔄 **ADR-0006 Phase 2 in progress (Jun 30)** — Decouple dashboard from service. 6 sub-issues created: #228 (data layer move), #229 (API routes), #230 (imports), #231 (web.py refactor), #232 (main.py cleanup), #233 (pyproject update). All assigned to coder.
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
