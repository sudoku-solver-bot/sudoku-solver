# Sudoku Dojo — Roadmap

**Last updated:** 2026-06-06
**Repository:** [sudoku-solver-bot/sudoku-solver](https://github.com/sudoku-solver-bot/sudoku-solver)

---

## Current Phase: Performance & Polish (June 2026)

Core feature set shipped (320+ PRs merged). **TS solver parity achieved!** Epic #566 and umbrella #585 closed. Focus is on DeathBlossom performance optimization and polish.

### Active Goals

- ✅ **TS Solver Parity (Epic #566)** — all 9 eliminators ported 🎉
- [ ] Optimize DeathBlossom performance (#583 → #617 + #618)

### 🟡 Medium Priority

_None — all medium-priority issues resolved._

### 🟢 Low Priority

| Issue | Title | Category |
|-------|-------|----------|
| #555 | Test systemd crash loop prevention after rate limiting | Infra |
| #445 | Evaluate pure TypeScript + CDN deployment (future) | Architecture |

### Open Bugs

| Issue | Title | Priority |
|-------|-------|----------|
| #583 | DeathBlossomCandidateEliminator too slow (TS side) | 🟡 (plan: #617) |

### Recently Completed (May–June 2026)

- ✅ #649 Add DeathBlossom profiling instrumentation
- ✅ #645 Add timeout guard to DeathBlossom + deepEliminators set
- ✅ #644 Port ForcingChainsCandidateEliminator to TS
- ✅ #639 Port SimpleColoringCandidateEliminator to TS
- ✅ #585, #566 — parity umbrella + epic closed
- ✅ #640 Port UniqueRectanglesCandidateEliminator to TS
- ✅ #638 Optimize MutantFish elimination loop (cover set perf)
- ✅ #637 Port XYZWingCandidateEliminator to TS (v2)
- ✅ #636 Add cross-data integrity validation
- ✅ #635 Extend validateJsonData with quiz and practice validation
- ✅ #631 Port XYWingCandidateEliminator to TS (Closes #567)
- ✅ #628 Add shared ALS detection helper
- ✅ #627 Port MutantFish candidate position enumeration to TS
- ✅ #626 Fix EmptyRectangle eliminator logic
- ✅ #625 Remove DeathBlossom from Kotlin default eliminators
- ✅ #624 Diagnose EmptyRectangle eliminator (failing tests)
- ✅ #614 Add intermediate technique exhaustion to HintGenerator
- ✅ #613 Add puzzle validation to hint endpoint
- ✅ #612 Use 1-based coordinates in hint explanations
- ✅ #607 Fix Dashboard/Progress endpoints (404s)
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
- ✅ 315+ PRs merged total

---

## Project Overview

### Stack
- **Backend:** Kotlin 2.1 (Ktor) + JUnit 5
- **Frontend:** Vue 3 (Composition API) + Vite 5 + Vitest
- **TS Solver:** `packages/solver/` — standalone TypeScript solver package
- **Infrastructure:** Systemd service on VPS, GitHub Actions CI

### Architecture
- 17 Kotlin elimination algorithms (Naked Single → Death Blossom)
- 16+ TypeScript eliminators (porting in progress)
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
