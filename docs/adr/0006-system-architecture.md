# ADR-0006: System Architecture — Sudoku Dojo

**Date:** 2026-06-09
**Status:** accepted
**Author:** Architect Agent 🏗️

---

## Context

Sudoku Dojo has evolved from a simple solver into a multi-layer educational platform with 6 AI agents coordinating development. The architecture has grown organically across multiple sprints, and key design decisions are scattered across `CLAUDE.md`, `README.md`, GitHub issues, and ADRs. There is no single authoritative reference for the system's architecture, making it harder for agents (or new contributors) to understand boundaries, constraints, and rationale.

This ADR documents the current architecture as a baseline for the active client-side TypeScript migration (#666–#671).

## Decision

We will maintain this ADR as the canonical architecture reference for Sudoku Dojo. The system is organized as four modules with strict dependency boundaries, a dual solver engine (Kotlin + TypeScript), and a stateless client-side frontend.

---

### Module Architecture

| Module | Directory | Responsibility | Dependencies |
|--------|-----------|---------------|--------------|
| **Board** (`:board`) | `board/` | Pure data types — Board, Coord, CoordGroup, BoardReader | Zero |
| **Solver** (`:solver`) | `solver/` | 21 elimination algorithms, puzzle generation, hint generation, difficulty rating | `:board` |
| **Web** (`:web`) | `web/` | Ktor REST API, serves built Vue assets, rate limiting, CORS | `:board`, `:solver` |
| **Frontend** (`web-ui/`) | `web-ui/` | Vue 3 SPA, 32 components, PWA, client-side TS solver mirror | REST API only |

### Dependency Flow

```
web-ui ──REST JSON──► :web ──compile──► :solver ──compile──► :board
   │                                        │
   └── @sudoku-dojo/solver                  └── 21 eliminators
       (client-side mirror)                    (strategy pattern)
```

### Solver Engine

The solver engine is the core of the system. Two implementations exist:

**Kotlin** (`:solver`):
- 21 elimination algorithms (Naked Single → Death Blossom)
- Bitmask representation: 9-bit `Int` per cell in `IntArray[81]` (324 bytes per board)
- Strategy pattern: each eliminator is a self-contained class implementing `CandidateEliminator`
- Backtracking with MRV (Minimum Remaining Values) heuristic
- Additional services: `PuzzleGenerator`, `HintGenerator`, `DifficultyRater`, `SolverWithSteps`

**TypeScript** (`packages/solver/`):
- 20 eliminators — **full parity** with Kotlin for basic solving
- Same bitmask representation and strategy pattern
- Published as `@sudoku-dojo/solver` npm package
- Used client-side via dynamic import in `api.ts` with server fallback

### API Endpoints

| Endpoint | Method | Purpose | Client Available? |
|----------|--------|---------|-------------------|
| `/api/v1/solve` | POST | Solve a puzzle | ✅ (TS solver) |
| `/api/v1/hint` | POST | Get next hint | ❌ (server only) |
| `/api/v1/steps` | POST | Step-by-step solving | ❌ (server only) |
| `/api/v1/generate` | POST | Generate puzzle | ❌ (server only) |
| `/api/v1/validate` | POST | Validate puzzle | ✅ (TS solver) |
| `/api/v1/candidates` | POST | Get cell candidates | ✅ (TS solver) |
| `/api/v1/tutorials/*` | GET | Tutorial lessons | N/A (static JSON) |
| `/api/v1/difficulty` | GET | Difficulty rating | ❌ (server only) |

### Frontend Architecture

- **Vue 3** with Composition API + Vite 5
- **32 components** across: grid rendering, tutorial mode, quiz mode, practice mode, hints, settings, import/export
- **PWA** via `vite-plugin-pwa` with service worker
- **Client-side state** via `localStorage` (no auth, no database)
- **i18n**: 20 languages supported
- Builds to `web/src/main/resources/static/` and is served by Ktor

### Key Design Decisions

| Decision | Rationale |
|----------|-----------|
| Bitmask candidate representation | Fast bitwise ops, minimal memory (324 bytes/board) |
| Strategy pattern for eliminators | Self-contained techniques, composable chain, easy to add/remove |
| Backtracking with MRV | Most puzzles never need backtracking; MRV picks hardest cell first |
| Dual solver (Kotlin + TypeScript) | TS enables offline solving; Kotlin has full feature set |
| Client-side state via localStorage | Simplicity for educational tool — no auth/database overhead |
| Declarative tutorials as JSON | 20 lessons editable without code changes |
| Multi-stage Docker build | Small production image, no build tools in runtime |
| Daily challenge removed (ADR-005) | Required server persistence, not core to educational mission |

### Deployment

- **Runtime**: Systemd service on VPS (Hetzner)
- **CI**: GitHub Actions — Java CI (389 tests), Detekt (Kotlin lint), CodeQL (security)
- **Deploy**: Atomic swap script (`scripts/deploy.sh`) with smoke test
- **Docker**: Multi-stage build (Gradle → Alpine JRE)

### Agent Pipeline

Six AI agents coordinate via GitHub issues:

| Agent | Role | Primary Interface |
|-------|------|-------------------|
| **Architect** | Process improvements, coordination, ADRs | `architecture` issues |
| **Planner** | Implementation specs, decomposes work | `plan` issues |
| **Coder** | Implements features, creates PRs | PRs |
| **Tester** | Files bugs with repro steps | `bug` issues |
| **Reviewer** | Reviews PRs, merges after CI green | PR reviews |
| **Deployer** | Deploys on merge, reports failures | `chore` issues |

Communication: Direct messages via `sessions_send` + GitHub issues as coordination surface.

---

## Consequences

### Positive
- Clear module boundaries prevent coupling (solver never imports Ktor, web never does bitmask math)
- Dual solver enables progressive migration — client-first with server fallback
- Agent pipeline scales — each agent has clear responsibilities
- Declarative tutorials reduce code changes for content updates

### Negative
- Two solver implementations to maintain (Kotlin + TypeScript)
- Hint generation and step-by-step are still server-only — requires network for educational features
- No database limits features (no user accounts, no cross-device sync)

### Neutral
- TypeScript migration is actively reducing the Kotlin dependency
- Daily challenge removal simplified the backend but reduced engagement features

---

## Related ADRs

- ADR-0001: Record Architecture Decisions
- ADR-0002: GitHub as Integration Layer
- ADR-005: Drop Daily Challenge
- ADR-002: Feature Scope Reduction

## Related Issues

- #445: TS + CDN architectural evaluation
- #660: Feasibility assessment
- #666: Client-side TS migration plan
- #667–#671: Active migration sub-issues
