# ADR-002: Feature Scope Reduction for Phase 1

**Date:** 2026-05-23  
**Status:** Accepted  
**Author:** Architect Agent  
**Decided by:** William  

---

## 1. Context

The sudoku-solver project has accumulated significant feature surface area — daily challenges, leaderboards, 20-language i18n, achievement systems, belt certificates, user testing scaffolding, onboarding tours, and a dashboard with hardcoded stub data. 

While these are good features in isolation, the project lacks solid foundations: the tutorial system (the primary learning path) needs attention, core solver UX needs polish, and the codebase carries ~4,900 lines of dead/misplaced code in the solver module (per ADR-001). Spreading effort across too many features dilutes focus and increases bug surface.

**Decision:** Strip non-essential features to concentrate on tutorial and core sudoku functionality first. Reintroduce removed features in later phases once the foundation is solid.

---

## 2. Decision

### Features REMOVED in Phase 1

| Feature | Frontend | Backend | Approx LOC Removed |
|---------|----------|---------|-------------------|
| **Daily Challenge** | `DailyChallenge.vue` (520), `DailyChallenge.test.ts` | `DailyChallengeRoutes.kt` (143) | ~663 + tests |
| **Leaderboard** | `Leaderboard.vue` (502), `Leaderboard.test.ts` | None (stub only) | ~502 + tests |
| **Multi-language (i18n)** | `i18n.ts` (2,656) — 20 languages | None | ~2,656 |
| **Achievements** | `Achievements.vue` (305) | None | ~305 |
| **Belt Certificate** | `BeltCertificate.vue` (309), `certificate-image.ts` | None | ~600+ |
| **User Testing** | `UserTestingParticipation.vue` (583), `UserTestingSurvey.vue` (839) | Dead code in solver module | ~1,422 + ~1,127 dead |
| **Onboarding Tour** | `OnboardingTour.vue` (228) | None | ~228 |
| **Stats Page** | `StatsPage.vue` (449) | None | ~449 |
| **Dashboard** | `Dashboard.vue` (464) | `DashboardSystem.kt` (stub, ~33) | ~500 |
| **Legacy API routes** | — | Duplicate route block in `Application.kt` | ~50 (config) |

**Total estimated removal: ~7,500+ lines**

### Features KEPT (Core)

| Feature | Files | Purpose |
|---------|-------|---------|
| **Tutorial Mode** | `TutorialMode.vue`, `TutorialSelector.vue`, `TutorialRoutes.kt` (367) | 🎯 Primary learning path — the main focus |
| **Practice Mode** | `PracticeMode.vue` | Core play loop |
| **Solve / Validate / Hint / StepByStep** | `SolveRoutes.kt`, `HintRoutes.kt`, `ValidateRoutes.kt`, `StepByStepRoutes.kt`, `CandidateRoutes.kt` | Core solver functionality |
| **Quiz Mode** | `QuizMode.vue` | Learning assessment |
| **SudokuGrid + Controls** | `SudokuGrid.vue`, `ControlPanel.vue`, `NumberBar.vue`, `MobileNumberPad.vue` | Core UI |
| **Settings** | `Settings.vue` | Basic usability |
| **Saved Puzzles** | `SavedPuzzles.vue` | Basic persistence |
| **Import Puzzle** | `ImportPuzzle.vue` | Useful utility |
| **Confetti Celebration** | `ConfettiCelebration.vue` | Reward feedback for learning |
| **Help / About** | `HelpPage.vue`, `AboutPage.vue` | Basic reference |
| **Progress Indicator** | `ProgressIndicator.vue` | Simple, useful |
| **Generate / Difficulty** | `GenerateRoutes.kt`, `DifficultyRoutes.kt` | Puzzle generation |
| **PWA basics** | Offline indicator, install prompt | Basic UX |

---

## 3. Consequences

### Positive
- **Focused development** — tutorial and core solver get full attention
- **Smaller bug surface** — fewer features = fewer edge cases
- **Cleaner codebase** — removing dead code and stubs reduces confusion
- **Faster builds** — less code to compile and test
- **Clearer roadmap** — Phase 2 can reintroduce features with real implementations

### Negative
- **Feature gap on deploy** — users lose daily challenge, leaderboard, multi-language
- **i18n removal is hard to reverse** — translation strings will need re-addition
- **Some users may expect these features** — need clear communication

### Mitigations
- Document removed features in README with "planned for Phase 2" tags
- Keep i18n.ts structure as a commented template for easy re-addition
- Daily Challenge and Leaderboard need real backend persistence anyway — removal is honest

---

## 4. Implementation Order

| Step | Task | Scope |
|------|------|-------|
| 1 | Remove i18n.ts, hardcode English strings | Frontend only |
| 2 | Remove DailyChallenge (frontend + backend routes + tests) | Full stack |
| 3 | Remove Leaderboard (frontend + tests) | Frontend only |
| 4 | Remove Achievements, BeltCertificate, StatsPage | Frontend only |
| 5 | Remove UserTesting (frontend + dead backend code) | Full stack |
| 6 | Remove OnboardingTour, Dashboard | Frontend + backend stubs |
| 7 | Remove legacy `/api` route block from Application.kt | Backend only |
| 8 | Clean up App.vue navigation (remove tabs/links to removed features) | Frontend |
| 9 | Update tests (remove tests for deleted features) | Tests |
| 10 | Verify build + all remaining tests pass | CI |

---

## 5. Reintroduction Criteria (Phase 2+)

Features should only be re-added when:
1. Core tutorial + solver UX is stable and bug-free
2. Real backend persistence exists (not hardcoded stubs)
3. Automated tests cover the feature's routes/components
4. The feature has been re-evaluated against current priorities
