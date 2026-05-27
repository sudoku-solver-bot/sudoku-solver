# Sudoku Dojo

Kotlin 2.1 (Ktor) backend + Vue 3 (TypeScript) frontend. Interactive sudoku learning app.

## Before You Start
- Read **[docs/UI-GUIDELINES.md](docs/UI-GUIDELINES.md)** — authoritative UI/UX reference
- Read your role file in **[docs/agents/](docs/agents/)** — responsibilities, inputs, outputs, constraints

## Agent Roles

| Role | File | Responsibility |
|------|------|---------------|
| 🏗️ Architect | [docs/agents/architect.md](docs/agents/architect.md) | Roadmap, ADRs, direction |
| 📋 Planner | [docs/agents/planner.md](docs/agents/planner.md) | Decompose issues, triage |
| 💻 Coder | [docs/agents/coder.md](docs/agents/coder.md) | Implement features, fix bugs |
| 🧪 Tester | [docs/agents/tester.md](docs/agents/tester.md) | QA, bug reports |
| 👀 Reviewer | [docs/agents/reviewer.md](docs/agents/reviewer.md) | Code review, PR approval |
| 🚀 Deployer | [docs/agents/deployer.md](docs/agents/deployer.md) | Deployment, monitoring |

## Key Conventions
- Empty cells: internal `.`, API `0`, UI shows **blank** (never `0` or `.`)
- Pencil marks: `Record<string, number[]>` (cell index → candidate digits)
- Stack: Kotlin 2.1 + Ktor → Vue 3 + Vite (SPA/PWA)
