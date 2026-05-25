# AGENTS.md — Sudoku Dojo

**Sudoku Dojo** — an interactive sudoku learning app with tutorials, practice puzzles, and technique-based progressive learning.

## Stack

- **Backend:** Kotlin 2.1 + Ktor (REST API)
- **Frontend:** Vue 3 + TypeScript + Vite (SPA, PWA)
- **Solver:** Kotlin solver engine + TypeScript reimplementation for offline use

## Architecture

```
board/       — Sudoku board data model and basic operations
solver/      — Kotlin solver engine (techniques, eliminators)
web/         — Ktor HTTP server, API routes, static asset serving
web-ui/      — Vue 3 frontend (components, solver, API client)
docs/        — ADRs, user guide, agent profiles, UI guidelines
docs/agents/ — Per-agent role files (coder, reviewer, tester, etc.)
```

## Key Conventions

- **Empty cells:** Internal `.` (period). API wire format may use `0`. UI must display as **blank** — never render `.` or `0` in the grid.
- **Pencil marks:** Stored as `Record<string, number[]>` (cell index → candidate digits).
- **Cell selection:** Selected cell gets strong border highlight. Peer row/col/box gets subtle background tint. Same-value cells get consistent highlight.
- **Conflict detection:** Duplicate values in any peer unit (row/col/box) render in red.
- **API paths:** `/api/v1/` prefix for all REST endpoints.

## Before You Touch Frontend Code

Read **[docs/UI-GUIDELINES.md](docs/UI-GUIDELINES.md)** — the authoritative UI/UX reference. Covers cell rendering, selection highlighting, pencil marks, color coding, dark mode, and accessibility.

## Agent Roles

See **[docs/agents/](docs/agents/)** for per-agent skill files and profiles.
Architect → Planner → Coder → Reviewer → Tester → Deployer.
