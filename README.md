# Sudoku Dojo — High-Level Design & Architecture

An **educational** Sudoku platform that teaches 21 solving techniques through interactive tutorials, belt-rank progression, and daily challenges. Target audience: learners mastering Sudoku logic + contributors extending the system.

**Live:** [sudoku-solver-r5y8.onrender.com](https://sudoku-solver-r5y8.onrender.com)

---

## Module Map

Three Gradle modules with strict boundaries:

| Module | Directory | Responsibility | Boundary |
|--------|-----------|---------------|----------|
| **Solver Engine** (`:kotlin`) | `kotlin/src/main/java/will/sudoku/solver/` | Pure solving logic — board representation, 21 elimination algorithms, puzzle generation, difficulty rating, hint generation | No HTTP, no I/O, no framework dependencies. Input: `Board`, output: `Board`/metrics/steps. |
| **Web Server** (`:web`) | `web/src/main/kotlin/will/sudoku/web/` | Ktor REST API — routes, request/response serialization, rate limiting, CORS, serves built Vue assets | Depends on `:kotlin` module only. Each route file owns one domain. No business logic duplication. |
| **Frontend** (`web-ui/`) | `web-ui/src/` | Vue 3 SPA — grid UI, tutorials, dashboard, settings, PWA. Includes a client-side TypeScript solver mirror | Builds to `web/src/main/resources/static/`. Communicates exclusively via REST API. Owns all client state (localStorage). |

### Directory Layout

```
sudoku-solver/
├── kotlin/                          # Solver engine (:kotlin)
│   └── src/main/java/will/sudoku/solver/
│       ├── Board.kt                 # 81-cell IntArray bitmask representation
│       ├── BoardReader.kt           # Text → Board parser
│       ├── Coord.kt / CoordGroup.kt # Coordinate system + house groups
│       ├── Solver.kt                # Backtracking solver with MRV heuristic
│       ├── SolverConfig.kt          # Configurable eliminator chain
│       ├── SolverWithMetrics.kt     # Instrumented solver (step count, timings)
│       ├── SolverWithSteps.kt       # Step-recording solver for tutorials
│       ├── CandidateEliminator.kt   # Strategy interface
│       ├── *CandidateEliminator.kt  # 21 technique implementations
│       ├── PuzzleGenerator.kt       # Puzzle generation (fill diagonals → remove cells)
│       ├── PuzzleValidator.kt       # Uniqueness + validity checks
│       ├── DifficultyRater.kt       # Rates puzzles by required technique depth
│       ├── HintGenerator.kt         # Produces TeachingHint for next logical move
│       ├── TeachingHint.kt          # Hint data model
│       ├── DifficultyLevel.kt       # 8 difficulty levels (EASY → EVIL)
│       ├── StepType.kt              # Enum of elimination step types
│       ├── SolvingStep.kt           # Recorded elimination step (for playback)
│       ├── SolvingProgress.kt       # Intermediate solver state
│       ├── StepRecorder.kt          # Collects SolvingSteps during solve
│       ├── SolvingListener.kt       # Observer interface for solver events
│       ├── MetricsCollector.kt      # Aggregates solver performance metrics
│       ├── SolverMetrics.kt         # Metrics data model
│       └── SolverLogger.kt          # Structured solver logging
├── web/                             # Ktor web server (:web)
│   └── src/main/kotlin/will/sudoku/web/
│       ├── Application.kt           # Ktor module setup + route registration
│       ├── SolveRoutes.kt           # POST /api/v1/solve
│       ├── HintRoutes.kt            # POST /api/v1/hint
│       ├── GenerateRoutes.kt        # POST /api/v1/generate
│       ├── ValidateRoutes.kt        # GET  /api/v1/validate
│       ├── CandidateRoutes.kt       # GET  /api/v1/candidates
│       ├── StepByStepRoutes.kt      # POST /api/v1/steps
│       ├── TutorialRoutes.kt        # GET  /api/v1/tutorials/*
│       ├── DailyChallengeRoutes.kt  # GET  /api/v1/daily
│       ├── DifficultyRoutes.kt      # GET  /api/v1/difficulty
│       ├── HealthRoutes.kt          # GET  /api/v1/health
│       ├── DeployInfoRoutes.kt      # GET  /api/v1/deploy-info, /version
│       ├── PuzzleEncoder.kt         # Board ↔ 81-char string encoding
│       ├── PuzzleValidator.kt       # Request validation utilities
│       ├── RequestLogging.kt        # Ktor plugin for request logging
│       └── VersionInfo.kt           # Build-time version constants
│   └── src/main/resources/tutorials/
│       └── lessons.json             # 20 tutorial lessons (declarative)
├── web-ui/                          # Vue 3 frontend
│   └── src/
│       ├── components/              # 32 Vue components (SudokuGrid, TutorialMode, etc.)
│       ├── solver/                  # TypeScript solver mirror (Board, Eliminators, Solver)
│       ├── api.ts                   # REST API client
│       ├── stats-tracker.ts         # localStorage progress tracking
│       └── main.ts                  # Vue app entry point
├── docs/                            # Supplementary docs
│   ├── adr/                         # Architecture Decision Records (ADR-0001, ADR-0002)
│   ├── BENCHMARKING.md              # JMH benchmark methodology
│   └── PUZZLE_LIBRARY.md            # Test puzzle format reference
├── .github/workflows/               # CI: Java CI, Detekt, CodeQL, JMH (manual)
├── Dockerfile                       # Multi-stage: Gradle build → Alpine JRE runtime
└── gradle/                          # Gradle wrapper (JDK 25)
```

---

## Module Boundaries

### What NOT to do
- **Solver never imports Ktor** or any web framework classes. `Board` does not know about HTTP.
- **Web server never performs raw bitmask math** — delegates to solver module via `Board` API.
- **Frontend never talks to solver directly** — always through REST endpoints.
- **Frontend has no backend auth or server-side sessions** — state is client-side `localStorage`.

### Dependency Graph
```
web-ui  ──REST JSON──►  :web  ──compile dep──►  :kotlin
 (Vue 3)               (Ktor)                  (pure JVM)
    │                      │
    ├── TypeScript solver   ├── serves static/
    │   (client-side mirror)│   (built Vue assets)
    │                        │
    └── localStorage state   └── rate-limited API
```

---

## Key Design Decisions

| Decision | Rationale | Reference |
|----------|-----------|-----------|
| **Bitmask candidate representation** — 9-bit `Int` per cell in `IntArray[81]` | Fast bitwise operations, minimal memory (324 bytes per board), enables efficient eliminator chaining | `Board.kt`, `Coord.kt` |
| **Strategy pattern for eliminators** — `CandidateEliminator` interface | Each technique is self-contained; `SolverConfig` composes the chain; easy to add/remove/reorder | `CandidateEliminator.kt`, `SolverConfig.kt` |
| **Backtracking with MRV heuristic** | Picks cell with fewest candidates first; most puzzles never need backtracking | `Solver.kt` |
| **Dual API versioning** — `/api/v1/*` + deprecated `/api/*` | Clean versioning with backward compatibility during migration; `X-API-Warn` header on legacy routes | `Application.kt` |
| **Ktor + Netty** | Lightweight, coroutine-native, no servlet container overhead | `web/build.gradle.kts` |
| **Vue 3 + Vite SPA** | Fast HMR, tree-shaking, native ESM; PWA via `vite-plugin-pwa` | `web-ui/package.json` |
| **Multi-stage Docker build** — Gradle build layer → Alpine JRE runtime layer | Small production image, no build tools in runtime | `Dockerfile` |
| **Client-side state via localStorage** | No auth, no database — simplicity for an educational tool | `web-ui/src/stats-tracker.ts` |
| **Tutorial content as JSON** — `lessons.json` | Declarative, editable without code changes; 20 lessons across 9 belt levels | `web/src/main/resources/tutorials/lessons.json` |
| **Puzzle generation: fill diagonal boxes first** | Diagonal 3×3 boxes are independent — enables faster valid board generation | `PuzzleGenerator.kt` |
| **Difficulty rating via technique scoring** | Rates puzzle difficulty by which eliminator techniques are required to solve it | `DifficultyRater.kt` |
| **Client-side TypeScript solver mirror** (`web-ui/src/solver/`) | Enables offline solving, candidate display, and validation without API calls | `Board.ts`, `Solver.ts`, `Eliminators.ts` |
| **PWA with service worker** | Installable app, offline capability, app-like experience | `vite-plugin-pwa` in `web-ui/` |

---

## Architecture Diagram

```
┌────────────────────┐          REST JSON/HTTP         ┌─────────────────────┐
│    Vue 3 SPA       │ ◄────────────────────────────► │    Ktor Server      │
│    (web-ui/)        │                                │    (:web)            │
│                     │  POST /api/v1/solve             │                      │
│  ┌───────────────┐  │  POST /api/v1/hint              │  Application.kt      │
│  │ TypeScript    │  │  POST /api/v1/generate          │  (module setup)      │
│  │ Solver Mirror │  │  POST /api/v1/validate          │                      │
│  └───────────────┘  │  POST /api/v1/candidates        │  ┌────────────────┐  │
│                     │  POST /api/v1/steps             │  │ Route Files    │  │
│  ┌───────────────┐  │  GET  /api/v1/tutorials/*      │  │ (one per       │  │
│  │ 32 Components │  │  GET  /api/v1/daily             │  │  domain)       │  │
│  └───────────────┘  │  GET  /api/v1/difficulty        │  └────────────────┘  │
│                     │  GET  /api/v1/health             │                      │
│  ┌───────────────┐  │  GET  /api/v1/version           │  ┌────────────────┐  │
│  │ PWA + SW     │  │  GET  /api/v1/deploy-info        │  │ static/        │  │
│  └───────────────┘  │                                  │  │ (built Vue     │  │
│                     │                                   │  │  assets)       │  │
│  ┌───────────────┐  │                                   │  └────────────────┘  │
│  │ localStorage │  │                                   └──────────┬──────────┘
│  │ (state)      │  │                                              │
│  └───────────────┘  │                                  compile project(":kotlin")
└────────────────────┘                                              │
                                                         ┌──────────▼──────────┐
                                                         │  Solver Engine      │
                                                         │  (:kotlin)           │
                                                         │                      │
                                                         │  ┌───────────────┐  │
                                                         │  │ Board         │  │
                                                         │  │ (IntArray[81] │  │
                                                         │  │  bitmasks)    │  │
                                                         │  └───────────────┘  │
                                                         │                      │
                                                         │  ┌───────────────┐  │
                                                         │  │ 21 Eliminators│  │
                                                         │  │ (strategy     │  │
                                                         │  │  pattern)     │  │
                                                         │  └───────────────┘  │
                                                         │                      │
                                                         │  ┌───────────────┐  │
                                                         │  │ Solver        │  │
                                                         │  │ (backtrack +  │  │
                                                         │  │  MRV)         │  │
                                                         │  └───────────────┘  │
                                                         │                      │
                                                         │  ┌───────────────┐  │
                                                         │  │ Generator     │  │
                                                         │  │ Rater, Hint   │  │
                                                         │  └───────────────┘  │
                                                         └──────────────────────┘
```

---

## Data Flow

### Solve
Frontend sends 81-char board string → API deserializes to `SolveRequest` → `BoardReader.readBoard()` → `Solver.solve()` applies 21 eliminators + MRV backtracking → solved `Board` → encoded back to 81-char string → JSON response with metrics.

### Hint
Frontend sends current board + player state → API → `HintGenerator.generateHint()` finds the next applicable logical technique → returns `TeachingHint` with technique name, description, highlighted cells, and educational content.

### Tutorial
Frontend fetches lesson via `GET /api/v1/tutorials/{id}` → `TutorialRoutes` reads `lessons.json` → returns lesson with puzzle, explanation, and step list → `TutorialMode.vue` plays through step-by-step solving with highlighted cells and technique explanations.

### Daily Challenge
Server generates puzzle deterministically (seed = date) → frontend fetches via `GET /api/v1/daily` → `DailyChallenge.vue` displays puzzle + tracks streak count in `localStorage`.

---

## API Endpoints (all under `/api/v1/` or deprecated `/api/`)

| Method | Path | Description | Route File |
|--------|------|-------------|------------|
| `POST` | `/api/v1/solve` | Solve a puzzle | `SolveRoutes.kt` |
| `POST` | `/api/v1/hint` | Get a teaching hint | `HintRoutes.kt` |
| `POST` | `/api/v1/generate` | Generate puzzle by difficulty | `GenerateRoutes.kt` |
| `GET` | `/api/v1/validate` | Validate a puzzle/solution | `ValidateRoutes.kt` |
| `GET` | `/api/v1/candidates` | Get candidate sets per cell | `CandidateRoutes.kt` |
| `POST` | `/api/v1/steps` | Step-by-step solving trace | `StepByStepRoutes.kt` |
| `GET` | `/api/v1/tutorials` | List all tutorials | `TutorialRoutes.kt` |
| `GET` | `/api/v1/tutorials/{id}` | Get tutorial lesson + steps | `TutorialRoutes.kt` |
| `GET` | `/api/v1/daily` | Today's daily challenge | `DailyChallengeRoutes.kt` |
| `GET` | `/api/v1/daily/{date}` | Daily challenge for date | `DailyChallengeRoutes.kt` |
| `GET` | `/api/v1/difficulty` | Difficulty levels info | `DifficultyRoutes.kt` |
| `GET` | `/api/v1/health` | Health check (memory, uptime, git commit) | `HealthRoutes.kt` |
| `GET` | `/api/v1/version` | Version + build info (lightweight) | `DeployInfoRoutes.kt` |
| `GET` | `/api/v1/deploy-info` | Deploy info (version, commit, timestamp) | `DeployInfoRoutes.kt` |

Legacy `/api/*` paths redirect to `/api/v1/*` with `X-API-Warn` header.

---

## Solving Techniques (21 Eliminators)

| # | Technique | Belt | Key Concept | Class |
|---|-----------|------|-------------|-------|
| 1 | Simple Elimination | ⬜ White | Remove known values from peers | `SimpleCandidateEliminator` |
| 2 | Naked Single | ⬜ White | Only one candidate left | (SimpleEliminator covers) |
| 3 | Hidden Single | 🟡 Yellow | Value can only go in one place | `ExclusionCandidateEliminator` |
| 4 | Naked Pair/Triple/Quad | 🟠 Orange | N cells, N same candidates | `GroupCandidateEliminator` |
| 5 | Hidden Pair/Triple/Quad | 🟠 Orange | N numbers, same N cells | `HiddenSubsetCandidateEliminator` |
| 6 | Pointing Pair/Triple | 🟢 Green | Box forces row/col elimination | `GroupCandidateEliminator` |
| 7 | Box/Line Reduction | 🟢 Green | Row/col forces box elimination | `GroupCandidateEliminator` |
| 8–9 | Naked Triple / Hidden Triple | 🔵 Blue | Three-cell subsets | `GroupCandidateEliminator` / `HiddenSubsetCandidateEliminator` |
| 10 | X-Wing | 🟣 Purple | Two rows, two columns | `XWingCandidateEliminator` |
| 11 | Swordfish | 🟣 Purple | Three rows, three columns | `SwordfishCandidateEliminator` |
| 12 | XY-Wing | 🟤 Brown | Three-cell chain | `XYWingCandidateEliminator` |
| 13 | XYZ-Wing | 🟤 Brown | Three-cell with triple | `XYZWingCandidateEliminator` |
| 14 | W-Wing | ⬛ Black | Conjugate pair chain | `WWingCandidateEliminator` |
| 15 | Simple Coloring | ⬛ Black | Color chains of candidates | `SimpleColoringCandidateEliminator` |
| 16 | Unique Rectangles | ⬛ Black | Avoid deadly patterns | `UniqueRectanglesCandidateEliminator` |
| 17 | ALS-XZ | 🎓 Master | Almost Locked Sets | `ALSXZCandidateEliminator` |
| 18 | Franken Fish | 🎓 Master | Fish using boxes as base/cover | `FrankenFishCandidateEliminator` |
| 19 | Mutant Fish | 🎓 Master | Mixed house types in base + cover | `MutantFishCandidateEliminator` |
| 20 | Death Blossom | 🎓 Master | Stem + petal ALS elimination | `DeathBlossomCandidateEliminator` |
| 21 | Forcing Chains | 🎓 Master | What-if chain contradictions | `ForcingChainsCandidateEliminator` |

---

## Tutorial System (20 lessons, 9 belts)

8 technique belts + 1 Master belt, each building on the previous:

| Belt | Lessons | Techniques |
|------|---------|------------|
| ⬜ White (2) | Naked Single, Hidden Single | Basic elimination |
| 🟡 Yellow (1) | Hidden Single | Advanced singles |
| 🟠 Orange (2) | Naked Pair, Hidden Pair | Subset logic |
| 🟢 Green (2) | Pointing Pair, Box/Line Reduction | Box-line interactions |
| 🔵 Blue (2) | Naked Triple, Hidden Triple | Multi-cell subsets |
| 🟣 Purple (2) | X-Wing, Swordfish | Fish patterns |
| 🟤 Brown (2) | XY-Wing, XYZ-Wing | Wing patterns |
| ⬛ Black (3) | Unique Rectangle, Simple Coloring, W-Wing | Advanced chains |
| 🎓 Master (5) | ALS-XZ, Franken Fish, Mutant Fish, Death Blossom, Forcing Chains | Expert techniques |

Content defined declaratively in `web/src/main/resources/tutorials/lessons.json`.

---

## Quick Start

### Prerequisites
- **JDK 25** (toolchain enforced in build.gradle.kts)
- **Node.js 18+** (for frontend build)

### Build & Test
```bash
# All tests
./gradlew test

# Frontend lint
cd web-ui && npm run lint:ci

# Frontend build (outputs to web/src/main/resources/static/)
cd web-ui && npm run build

# Build deployable distribution
./gradlew :web:installDist
```

### Run Locally
```bash
# Backend + served frontend
./gradlew :web:run     # → http://localhost:25321

# Frontend dev server (hot reload)
cd web-ui && npm install && npm run dev
```

### Docker
```bash
docker build -t sudoku-dojo .
docker run -p 10000:10000 sudoku-dojo
```

---

## CI/CD

- **GitHub Actions** — Java CI (tests), Detekt (static analysis), CodeQL (security), JMH (manual benchmarks)
- **Auto-deploy** to Render on merge to `master`
- **Branch protection** — CI required, reviews required, linear history, admin enforcement

---

## References

| Doc | Purpose |
|-----|---------|
| `CLAUDE.md` | AI coding agent instructions — full architecture details, coordinate system, board format |
| `docs/adr/` | Architecture Decision Records — why key decisions were made |
| `docs/BENCHMARKING.md` | JMH benchmark methodology and results |
| `docs/PUZZLE_LIBRARY.md` | Test puzzle format (`.question`/`.solution` file pairs) |
| `docs/sonarcloud-setup.md` | SonarCloud static analysis configuration |
