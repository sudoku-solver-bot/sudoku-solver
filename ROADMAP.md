# Sudoku Dojo — Roadmap

**Last updated:** 2026-06-08
**Repository:** [sudoku-solver-bot/sudoku-solver](https://github.com/sudoku-solver-bot/sudoku-solver)

---

## Current Phase: Client-Side TypeScript Migration (June 2026)

Stabilization complete. TS solver at full parity with Kotlin (20 eliminators). Now migrating remaining backend logic to client-side TypeScript for offline capability.

### Planned (from #666)

| Issue | Title | Priority | Effort | Status |
|-------|-------|----------|--------|--------|
| #669 | Port HintGenerator to TypeScript — core technique detection | High | 3-5 days | Next up |
| #670 | Port step-by-step solving to TypeScript | High | 2-3 days | |
| #671 | Port puzzle generation to TypeScript | Medium | 2-3 days | |
| #555 | Test systemd crash loop prevention | Medium | — | Ops task, blocked on VPS SSH |

### Completed This Sprint (June 7-8)

- ✅ **#674/#668** — **HintGenerator decomposition complete** — Removed dead code. HintGenerator: 1,208 → 507 LOC. 12 detectors in detectors/ package.
- ✅ **#673** — **HintGenerator decomposition part 1** — Extracted TechniqueDetector interface + 12 detectors.
- ✅ **#672/#667** — **Fish unification** — Extracted generic FishCandidateEliminator parameterized by size. Deleted XWingCandidateEliminator and SwordfishCandidateEliminator.
- ✅ **#653/#650** — **WWing board corruption** — Removed do-while(!stable) loop in WWingCandidateEliminator. Had caused cascading eliminations on partially-converged boards. PR: 187e55d.
- ✅ **#655/#652** — **MutantFish test timeout** — Added 15s per-test timeout. All 323 tests pass.
- ✅ **#651/#654** — **Multi-solution tutorials (12 total)** — Replaced all non-unique puzzles with validated unique-solution versions. All 20 tutorials now pass validation. PRs: 6b1d5f5, #657.
- ✅ **#445/#660** — **TS + CDN architectural evaluation** — Complete feasibility assessment (estimated 5-7 days). HintGenerator (1,208 LOC) is the largest porting risk. Deferred to future phase.
- ✅ **#666** — **Client-side TS migration plan** — Filed and decomposed by planner into #667-#671.

### 🟡 Current Sprint (June 8)

| Issue | Title | Priority | Est. |
|-------|-------|----------|------|
| #667 | Extract generic FishCandidateEliminator | 🔴 High | 1h |
| #668 | Decompose HintGenerator (1,208 LOC) | 🔴 High | 2-3h |

### 🟢 Backlog

| Issue | Title | Notes |
|-------|-------|-------|
| #666 | Client-side TS migration (umbrella) | 3 phases: hints, step-by-step, generation |
| #669 | Port HintGenerator to TS | Part of #666, Phase 1 |
| #670 | Port step-by-step to TS | Part of #666, Phase 2 |
| #671 | Port puzzle generation to TS | Part of #666, Phase 3 |
| #555 | Test systemd crash loop prevention | Ops task — needs VPS SSH access. Not actionable from planner. Systemd RestartSec already configured. |

### Recently Completed (May–June 2026)

- ✅ #649 Add DeathBlossom profiling instrumentation
- ✅ #648 Add ALS caching and early pruning to DeathBlossom
- ✅ #647 Profile DeathBlossom bottlenecks
- ✅ #646 ALS caching + early pruning plan
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
- **20 TypeScript eliminators** (full parity with Kotlin)
- REST API at `/api/v1/*` (client-side solving with server fallback)
- Step-by-step solving for educational use (server-only, porting planned)
- Hint generation (server-only, porting planned)
- Daily challenge removed (ADR-005)

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
