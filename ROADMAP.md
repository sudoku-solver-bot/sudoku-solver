# Sudoku Dojo — Roadmap

**Last updated:** 2026-06-09
**Repository:** [sudoku-solver-bot/sudoku-solver](https://github.com/sudoku-solver-bot/sudoku-solver)

---

## Current Phase: Client-Side TypeScript Migration (June 2026)

Stabilization complete. TS solver at full parity with Kotlin (20 eliminators). Now migrating remaining backend logic to client-side TypeScript for offline capability.

### Planned (from #666)

| Issue | Title | Priority | Effort | Status |
|-------|-------|----------|--------|--------|
| #669 | Port HintGenerator to TypeScript | High | ~3h total | ✅ Closed — sub-issues #682-#689 |
| #670 | Port step-by-step solving to TypeScript | High | ~1h total | Broken into #693-#696 |
| #671 | Port puzzle generation to TypeScript | Medium | ~50 min total | Broken into #690-#692 |
| #678 | Automate API test suite | Medium | ~1h total | Broken into #697-#701 |
| #679 | Automate UI/UX source code audit | Medium | ~60 min total | Broken into #704-#708 |
| #680 | Automate localStorage persistence tests | Medium | ~45 min total | Broken into #709-#712 |
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

### 🟡 Current Sprint (June 8-9) — Active Work

| Issue | Title | Priority | Est. | Status |
|-------|-------|----------|------|--------|
| #682 | HintGenerator core infrastructure + basic detectors | 🔴 High | ~20 min | ✅ Closed — PR #702 |
| #683 | PointingPair + BoxLineReduction detectors | 🔴 High | ~15 min | ✅ Closed — PR #703 |
| #684 | Subset detectors (4) | 🔴 High | ~20 min | ✅ Closed — PR #714 |
| #685 | Fish + wing detectors (4) | 🔴 High | ~20 min | ✅ Closed — PR #714 |
| #686 | Eliminator-based techniques (8) | 🔴 High | ~25 min | Blocked on #682 |
| #687 | Wire HintGenerator to api.ts | 🔴 High | ~15 min | Blocked on #682-#686 |
| #688 | Verify against 20 tutorials | 🔴 High | ~20 min | Blocked on #687 |
| #689 | Update HintModal.vue | 🟡 Medium | ~15 min | Blocked on #687 |
| #690 | Port DifficultyRater to TS | 🟡 Medium | ~15 min | ✅ Closed — PR #717 |
| #691 | Port PuzzleGenerator to TS | 🟡 Medium | ~20 min | Blocked on #690 |
| #692 | Wire PuzzleGenerator to API | 🟡 Medium | ~15 min | Blocked on #691 |
| #693 | Port SolvingProgress/SolvingStep types | 🔴 High | ~10 min | ✅ Closed — PR #717 |
| #694 | Port StepRecorder to TS | 🔴 High | ~20 min | Blocked on #693 |
| #695 | Port SolverWithSteps to TS | 🔴 High | ~10 min | Blocked on #694 |
| #696 | Wire SolverWithSteps to API | 🔴 High | ~15 min | Blocked on #695 |

### 🟢 Backlog — Test Automation

| Issue | Title | Notes |
|-------|-------|-------|
| #697 | Tutorial validation tests (20) | Part of #678 |
| #698 | Quiz validation tests (20) | Part of #678 |
| #699 | Difficulty generation tests (9) | Part of #678 |
| #700 | Rate limiting test | Part of #678 |
| #701 | Endpoint error handling tests | Part of #678 |
| #704 | Empty cell rendering + ARIA labels audit | Part of #679 |
| #705 | Keyboard navigation audit | Part of #679 |
| #706 | Dark/colorblind/contrast modes audit | Part of #679 |
| #707 | Number bar + highlight priority audit | Part of #679 |
| #708 | Tutorial highlights + themes audit | Part of #679 |
| #709 | localStorage key existence checks | Part of #680 |
| #710 | Game state save/restore tests | Part of #680 |
| #711 | Preference persistence tests | Part of #680 |
| #712 | Tutorial progress + saved puzzles tests | Part of #680 |
| #555 | Test systemd crash loop prevention | Ops task, blocked on VPS SSH |

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
