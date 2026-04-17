# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

**Sudoku Dojo** — An educational Sudoku platform with 17 solving algorithms, 20 interactive tutorials, daily challenges, and belt-rank progression. Built with Kotlin (backend) + Vue 3 (frontend).

**Live site:** https://sudoku-solver-r5y8.onrender.com

## Build System

Multi-module Gradle project:
- `kotlin` — Solver engine (17 elimination algorithms)
- `web` — Ktor web server (REST API)
- `web-ui` — Vue 3 + Vite frontend (builds into `web/src/main/resources/static/`)

### Common Commands

```bash
# Build everything
./gradlew build

# Run all tests (389 tests)
./gradlew test

# Run single test class
./gradlew test --tests will.sudoku.solver.SolverTest

# Run backend locally (serves frontend too)
./gradlew :web:run

# Frontend dev server (hot reload)
cd web-ui && npm run dev

# Build frontend for production
cd web-ui && npm run build

# Run JMH benchmarks (manual trigger only)
./gradlew :kotlin:jmh
```

## CI/CD

GitHub Actions:
- **Java CI** — Runs on push/PR to `master` (389 tests)
- **Detekt** — Kotlin static analysis on push to `master` + weekly
- **JMH** — Manual dispatch only

JDK 21 required. Auto-deploys to Render on merge to `master`.

## Architecture

### Backend

#### Core Solver (`kotlin/src/main/java/will/sudoku/solver/`)

The `Board` class maintains state as an 81-element `IntArray` of candidate patterns:
- Each cell has a 9-bit integer (bitmask) representing possible values 1-9
- Bit `i` (0-indexed) = value `i+1`
- Confirmed cell = single bit set; unresolved = multiple bits set

**17 Eliminators** (all implement `CandidateEliminator` interface):

| Class | Technique |
|-------|-----------|
| `SimpleCandidateEliminator` | Remove known values from peers |
| `GroupCandidateEliminator` | Naked subsets (pairs, triples, quads) |
| `ExclusionCandidateEliminator` | Hidden singles |
| `HiddenSubsetCandidateEliminator` | Hidden pairs/triples/quads |
| `XWingCandidateEliminator` | X-Wing pattern |
| `SwordfishCandidateEliminator` | Swordfish (3-row/col) |
| `XYWingCandidateEliminator` | XY-Wing (Y-Wing) |
| `XYZWingCandidateEliminator` | XYZ-Wing |
| `WWingCandidateEliminator` | W-Wing |
| `SimpleColoringCandidateEliminator` | Simple coloring chains |
| `UniqueRectanglesCandidateEliminator` | Unique rectangle avoidance |
| `ALSXZCandidateEliminator` | Almost Locked Sets - XZ |
| `FrankenFishCandidateEliminator` | Franken Fish |
| `MutantFishCandidateEliminator` | Mutant Fish |
| `DeathBlossomCandidateEliminator` | Death Blossom |
| `ForcingChainsCandidateEliminator` | Forcing chains |

**Solver algorithm:** Backtracking with MRV heuristic. Applies all eliminators until stable, then picks cell with fewest candidates to branch on.

#### Web Server (`web/src/main/kotlin/will/sudoku/web/`)

Ktor REST API with routes:
- `SolveRoutes` — Solve puzzles
- `CandidateRoutes` — Get candidate sets
- `HintRoutes` — Teaching hints
- `GenerateRoutes` — Puzzle generation
- `DailyChallengeRoutes` — Daily puzzle
- `TutorialRoutes` — 20 tutorials from `lessons.json`
- `DashboardRoutes` — Stats and progress
- `HealthRoutes` — Health check (JVM memory, uptime)
- `StepByStepRoutes` — Step-by-step solving
- `ProgressRoutes` — Student progress tracking

#### Tutorials (`web/src/main/resources/tutorials/lessons.json`)

20 lessons across 7 belt levels:
- White (2): Naked Single, Hidden Single
- Yellow (1): Hidden Single
- Orange (2): Naked Pair, Hidden Pair
- Green (2): Pointing Pair, Box/Line Reduction
- Blue (2): Naked Triple, Hidden Triple
- Purple (2): X-Wing, Swordfish
- Brown (2): XY-Wing, XYZ-Wing
- Black (3): Unique Rectangle, Simple Coloring, W-Wing
- Master (5): ALS-XZ, Franken Fish, Mutant Fish, Death Blossom, Forcing Chains

### Frontend (`web-ui/src/`)

Vue 3 SPA with 14 components:
- `App.vue` — Root with navigation
- `SudokuGrid.vue` — 9×9 grid with candidates, highlighting
- `TutorialMode.vue` — Guided lesson player
- `TutorialSelector.vue` — Belt-grouped tutorial list
- `Dashboard.vue` — Stats and progress
- `DailyChallenge.vue` — Daily puzzle with streaks
- `Settings.vue` — Color-blind, high contrast, dark mode
- `ControlPanel.vue`, `MobileNumberPad.vue`, `HintModal.vue` — Controls
- `ResultDisplay.vue`, `ToastNotification.vue`, `ProgressIndicator.vue` — Feedback

**State:** localStorage for progress, settings, streaks (no backend auth)

### Coordinate System

- `Coord(row: Int, col: Int)` — Zero-indexed (0-8)
- `Coord.all` — Pre-computed array of all 81 coordinates
- `CoordGroup` — Rows, columns, 3×3 regions

### Board Format

Text format with optional visual separators:
- `.` or `0` = empty, `1-9` = value
- `!` = column separator, `-` = row separator

Parsed by `BoardReader.readBoard()`.

### Test Structure

Tests in `kotlin/src/test/`:
- `kotlin/src/test/resources/solver/` — `.question` / `.solution` file pairs
- `SolverTest` auto-discovers paired files
- Individual eliminator tests (e.g., `XWingCandidateEliminatorTest`)
- Total: 389 tests, 0 failures

## Branch Strategy

- **master** — Stable, auto-deploys to Render
- Branch protection: CI required, reviews required, linear history, admin enforcement

## Adding a New Feature

### New Elimination Technique
1. Implement `CandidateEliminator` in `kotlin/src/main/`
2. Add to eliminator chain in `Solver.kt`
3. Add tests with `.question`/`.solution` files
4. Create tutorial in `lessons.json`
5. Add frontend tutorial content

### New Frontend Component
1. Create `.vue` file in `web-ui/src/components/`
2. Import in `App.vue` or parent component
3. Add API calls in `api.js` if needed
4. Build: `cd web-ui && npm run build`

### New API Endpoint
1. Create route file in `web/src/main/kotlin/will/sudoku/web/`
2. Register in `Application.kt`
3. Add corresponding frontend API call in `api.js`
