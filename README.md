# Sudoku Dojo — Learn & Play

An educational Sudoku platform with 17 solving algorithms, interactive tutorials, daily challenges, and belt-rank progression. Built with Kotlin + Vue 3.

**Live:** [sudoku-solver-r5y8.onrender.com](https://sudoku-solver-r5y8.onrender.com)

---

## What It Does

Sudoku Dojo teaches Sudoku solving techniques step-by-step, from basic elimination to advanced logic chains. Think of it as a karate dojo — each technique earns you a belt, from ⬜ White to 🎓 Master.

### Core Features

- **20 Interactive Tutorials** across 7 belt levels (White → Master)
- **17 Solving Algorithms** (see below)
- **Step-by-step guided lessons** with highlighted cells and explanations
- **Daily Challenge** — new puzzle every day with streak tracking
- **Dashboard** — stats, belt progress, and solving history
- **Candidate Display** — pencil marks shown as mini 3×3 grids
- **PWA Support** — installable, works offline
- **Accessibility** — color-blind mode, high contrast, ARIA labels, keyboard navigation
- **Share Results** — Web Share API for daily challenge completion

---

## Solving Techniques

The solver implements 17 elimination algorithms, ordered by difficulty:

| # | Technique | Belt | Key Concept |
|---|-----------|------|-------------|
| 1 | **Simple Elimination** | ⬜ White | Remove known values from peers |
| 2 | **Naked Single** | ⬜ White | Only one candidate left |
| 3 | **Hidden Single** | 🟡 Yellow | Value can only go in one place |
| 4 | **Naked Pair** | 🟠 Orange | Two cells, two same candidates |
| 5 | **Hidden Pair** | 🟠 Orange | Two numbers, same two cells |
| 6 | **Pointing Pair/Triple** | 🟢 Green | Box forces row/col elimination |
| 7 | **Box/Line Reduction** | 🟢 Green | Row/col forces box elimination |
| 8 | **Naked Triple** | 🔵 Blue | Three cells, three candidates |
| 9 | **Hidden Triple** | 🔵 Blue | Three numbers, same three cells |
| 10 | **X-Wing** | 🟣 Purple | Two rows, two columns pattern |
| 11 | **Swordfish** | 🟣 Purple | Three rows, three columns |
| 12 | **XY-Wing** | 🟤 Brown | Three-cell chain elimination |
| 13 | **XYZ-Wing** | 🟤 Brown | Three-cell with triple |
| 14 | **W-Wing** | ⬛ Black | Conjugate pair chain |
| 15 | **Simple Coloring** | ⬛ Black | Color chains of candidates |
| 16 | **Unique Rectangles** | ⬛ Black | Avoid deadly patterns |
| 17 | **ALS-XZ** | 🎓 Master | Almost Locked Sets with restricted common |
| 18 | **Franken Fish** | 🎓 Master | Fish using boxes as base/cover |
| 19 | **Mutant Fish** | 🎓 Master | Mixed house types in base + cover |
| 20 | **Death Blossom** | 🎓 Master | Stem + petal ALS elimination |
| 21 | **Forcing Chains** | 🎓 Master | What-if chain contradictions |

---

## Architecture

### Backend — Kotlin + Ktor

- **Solver Engine** (`kotlin/`) — Bitmask-based candidate representation with 17 eliminators and backtracking (MRV heuristic)
- **Web API** (`web/`) — Ktor REST API serving puzzles, solutions, tutorials, hints, daily challenges
- **Docker** — Multi-stage build (Gradle build + Alpine JRE runtime)

### Frontend — Vue 3 + Vite

- **Single-page app** (`web-ui/`) — Responsive, mobile-first design
- **Components:** SudokuGrid, TutorialMode, TutorialSelector, Dashboard, DailyChallenge, Settings
- **PWA:** Service worker caching via vite-plugin-pwa

### Project Structure

```
sudoku-solver/
├── kotlin/                    # Kotlin solver engine
│   ├── src/main/              # 17 eliminators + solver + puzzle generator
│   └── src/test/              # 389 tests, 0 failures
├── web/                       # Ktor web server
│   └── src/main/              # REST API routes + static resources
│       └── resources/tutorials/  # lessons.json (20 tutorials)
├── web-ui/                    # Vue 3 frontend
│   └── src/components/        # 14 Vue components
├── Dockerfile                 # Multi-stage Docker build
└── docs/                      # Roadmap, benchmarks, puzzle library
```

---

## API Endpoints

| Endpoint | Description |
|----------|-------------|
| `POST /api/solve` | Solve a puzzle |
| `POST /api/v1/candidates` | Get candidate sets for a board |
| `GET /api/hint` | Get a teaching hint for current board state |
| `GET /api/generate?difficulty=medium` | Generate a random puzzle |
| `GET /api/daily` | Today's daily challenge puzzle |
| `GET /api/v1/tutorials` | List all tutorials |
| `GET /api/v1/tutorials/{id}` | Get tutorial lesson + steps |
| `GET /api/v1/tutorials/{id}/board` | Get example puzzle for tutorial |
| `POST /api/v1/tutorials/{id}/complete` | Mark tutorial as completed |
| `GET /api/v1/progress` | Student progress overview |
| `GET /api/health` | Health check (uptime, JVM memory, threads) |
| `GET /api/validate` | Validate a puzzle solution |

---

## Getting Started

### Prerequisites

- **JDK 21+**
- **Node.js 18+** (for frontend build)
- **Docker** (optional, for containerized deployment)

### Run Locally

```bash
# Build and run backend (includes frontend assets)
./gradlew :web:run

# Or run frontend dev server separately
cd web-ui && npm install && npm run dev
```

### Run Tests

```bash
# All 389 tests
./gradlew test

# Single test class
./gradlew test --tests will.sudoku.solver.SolverTest
```

### Docker

```bash
docker build -t sudoku-dojo .
docker run -p 10000:10000 sudoku-dojo
```

---

## Deployment

Deployed on [Render](https://render.com) (free tier):

- **Branch:** `master` (auto-deploy on push)
- **Runtime:** Docker
- **Region:** Oregon
- **Health Check:** `/api/health`
- **Dashboard:** [dashboard.render.com](https://dashboard.render.com)

---

## Contributing

### Development Workflow

1. Create a feature branch from `master`
2. Make changes + add tests
3. Run `./gradlew test` to verify
4. Open a pull request
5. CI must pass (Java CI workflow, 389 tests)

### Branch Protection

- ✅ CI status checks required
- ✅ PR reviews required
- ✅ Linear history (squash merge)
- ✅ Admin enforcement
- ✅ Force pushes disabled

### Adding a New Elimination Technique

1. Implement `CandidateEliminator` interface in `kotlin/src/main/`
2. Register in `Solver.kt` eliminator chain
3. Add tests with `.question` / `.solution` files
4. Create a tutorial lesson in `lessons.json`
5. Add frontend support (tutorial steps, highlights)

### Pre-commit Hooks

This project uses [pre-commit](https://pre-commit.com/):

```bash
pip install pre-commit && pre-commit install
```

Checks: Kotlin (ktlint), JavaScript/Vue (ESLint), Dockerfiles (hadolint), YAML/JSON/XML syntax, security (private keys, AWS credentials), trailing whitespace, large files.

---

## Stats

- **109 PRs merged** (G1-G10 game/teaching features + UI/UX enhancements)
- **389 tests**, 0 failures
- **20 tutorials**, 7 belt levels
- **17 elimination algorithms**
- **CI:** GitHub Actions (Java CI + Detekt + JMH benchmarks)

## License

This project is open source. See repository for license details.
