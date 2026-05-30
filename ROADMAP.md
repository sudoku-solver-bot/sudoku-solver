# Sudoku Dojo — Roadmap

**Last updated:** 2026-05-29
**Repository:** [sudoku-solver-bot/sudoku-solver](https://github.com/sudoku-solver-bot/sudoku-solver)

---

## Current Phase: Improvement & Solver Parity (May 2026)

Core feature set shipped (309+ PRs merged). Focus is on TypeScript solver parity, bug fixes, and quality.

### Active Goals

- [ ] **TS Solver Parity (Epic #566)** — port remaining 5 Kotlin eliminators to TypeScript
- [ ] Fix hint accuracy — stop returning generic "Scanning" for hard puzzles (#603 → #610 + #611)
- [ ] Fix EmptyRectangle incorrect eliminations (#582 → #615 + #616)
- [ ] Optimize DeathBlossom performance (#583 → #617 + #618)
- [ ] Investigate missing dashboard/progress endpoints (#605 → #623)

### 🔴 High Priority

| Issue | Title | Category |
|-------|-------|----------|
| #610 | Add intermediate technique exhaustion to HintGenerator | Hint System |
| #615 | Diagnose EmptyRectangle eliminator incorrect eliminations | Solver Bug |
| #616 | Fix EmptyRectangle eliminator logic (blocked by #615) | Solver Bug |
| #619 | Port MutantFish candidate position enumeration to TS | Solver Parity |
| #620 | Complete MutantFish with cover set logic (blocked by #619) | Solver Parity |
| #621 | Port ALS detection helper to TypeScript | Solver Parity |
| #622 | Port ALS-XZ elimination logic (blocked by #621) | Solver Parity |

### 🟡 Medium Priority

| Issue | Title | Category |
|-------|-------|----------|
| #611 | Add puzzle validation to hint endpoint | Hint System |
| #617 | Profile DeathBlossom bottlenecks and add ALS caching | Performance |
| #618 | Add timeout guard to DeathBlossom + deep eliminators | Performance |
| #623 | Investigate dashboard/progress endpoint 404s | API Bug |
| #571 | Port SimpleColoringCandidateEliminator to TS | Solver Parity |
| #570 | Port UniqueRectanglesCandidateEliminator to TS | Solver Parity |
| #569 | Port ForcingChainsCandidateEliminator to TS | Solver Parity |
| #555 | Test systemd crash loop prevention after rate limiting | Infra |
| #585 | Close Kotlin ↔ TypeScript solver parity gap (umbrella) | Architecture |

### 🟢 Low Priority

| Issue | Title | Category |
|-------|-------|----------|
| #568 | Port XYZWingCandidateEliminator to TS | Solver Parity |
| #567 | Port XYWingCandidateEliminator to TS | Solver Parity |
| #445 | Evaluate pure TypeScript + CDN deployment (future) | Architecture |

### Open Bugs (awaiting plans)

| Issue | Title | Priority |
|-------|-------|----------|
| #603 | Hint API returns generic "Scanning" for some valid puzzles | 🔴 (plans: #610, #611) |
| #605 | Dashboard report and Progress endpoints return 404 | 🟡 (investigation: #623) |
| #582 | EmptyRectangleCandidateEliminator incorrect eliminations | 🔴 (plans: #615, #616) |
| #583 | DeathBlossomCandidateEliminator too slow | 🟡 (plans: #617, #618) |

### Recently Completed (May 2026)

- ✅ #609 Add difficulty field to tutorial responses
- ✅ #608 Fix 0-based coordinates in hint explanations
- ✅ #602 Slim repo-root AGENTS.md to thin dispatcher
- ✅ #601 Per-agent role definition files
- ✅ #599 UI/UX guidelines reference document
- ✅ #598 Treat '0' as empty cell in all display paths
- ✅ #589 Add value field to hint API response
- ✅ #588 Port FrankenFishCandidateEliminator to TS
- ✅ #584 Create shared multi-unit fish pattern helper
- ✅ #579 Add 7 advanced eliminators to defaults
- ✅ #578 Port DeathBlossomCandidateEliminator to TS
- ✅ #576 Remove remaining Render deployment references
- ✅ #564 Deploy script with atomic swap + smoke test
- ✅ #563 Staging environment support
- ✅ 309+ PRs merged total

---

## Project Overview

### Stack
- **Backend:** Kotlin 2.1 (Ktor) + JUnit 5
- **Frontend:** Vue 3 (Composition API) + Vite 5 + Vitest
- **TS Solver:** `packages/solver/` — standalone TypeScript solver package
- **Infrastructure:** Systemd service on VPS, GitHub Actions CI

### Architecture
- 17 Kotlin elimination algorithms (Naked Single → Death Blossom)
- 12+ TypeScript eliminators (porting in progress)
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
