# Sudoku Solver — Enhancement Roadmap

> Educational-focused plan for future development. Last updated: 2026-04-16

---

## Phase 1: UI Improvements — Candidate Display

**Goal:** Show pencil marks (possible values) in the grid, matching how real sudoku players think.

### What Other Apps Do

Standard candidate display across sudoku apps (SudokuWiki, Sudoku.com, Cracking the Cryptic, etc.):

```
┌───────┬───────┬───────┐
│ 1  2 3│ 4  5 6│ 7  8 9│   ← Each cell shows a mini 3×3 grid
│       │       │       │      of possible values 1-9
│       │       │       │
├───────┼───────┼───────┤
│ 5     │       │   3   │   ← Known values shown large in center
│       │       │       │
│       │       │       │
└───────┴───────┴───────┘
```

- **Known cells:** Large number, centered, bold
- **Unknown cells:** Small candidate numbers in a 3×3 sub-grid layout (position 1=1, position 2=2, etc.)
- **Highlighting:** Selected cell highlights its row/column/box peers in a lighter shade
- **Candidate toggling:** Click a cell → number pad toggles candidates on/off

### Tasks

- [ ] **1.1 Candidate Grid Rendering** (SudokuGrid.vue)
  - When cell is unknown, render a 3×3 mini-grid of pencil marks
  - Candidate `n` appears at position `(row=(n-1)/3, col=(n-1)%3)` within the cell
  - Auto-compute candidates from current board state
  - Toggle between "show candidates" and "hide candidates" mode

- [ ] **1.2 Candidate Editing**
  - Select an unknown cell → number pad toggles that candidate on/off
  - Auto-remove candidates when a number is placed
  - Option: manual vs auto candidates (like SudokuWiki's "Auto Clear")

- [ ] **1.3 Visual Highlighting**
  - When hovering/clicking a cell with candidate X, highlight all cells containing X
  - Highlight row/col/box of selected cell in light background
  - Color-code: given (blue), user-entered (black), wrong (red)

- [ ] **1.4 Backend API**
  - `GET /api/v1/solve/candidates` → return candidate sets for current board
  - Update solve endpoint to include candidates in response

---

## Phase 2: Interactive Tutorial System

**Goal:** Teach each elimination technique step by step, from simplest to hardest.

### Design Philosophy

Think of it like a karate belt system — white belt learns basic elimination, each new technique unlocks the next "belt". Kids should feel progression and achievement.

### Tutorial Structure (per technique)

Each tutorial lesson has:

1. **Introduction** — What is this technique? When do you use it?
2. **Example Board** — A carefully crafted puzzle that demonstrates the pattern
3. **Guided Steps:**
   - **Highlight** the relevant area (row/col/box/cells) in a distinct color
   - **Explain** in simple language what to look for
   - **Ask** the student to identify the elimination (interactive)
   - **Reveal** the answer with animation if they're stuck
4. **Practice** — 2-3 more puzzles using the same technique
5. **Quiz** — "Can you spot the pattern?" on a fresh board
6. **Badge** — Unlock a badge/achievement for completing

### Technique Order (simplest first)

| # | Technique | Belt Level | Key Concept |
|---|-----------|-----------|-------------|
| 1 | **Naked Single** | ⬜ White | Only one candidate left |
| 2 | **Hidden Single** | 🟡 Yellow | Number can only go in one place |
| 3 | **Naked Pair** | 🟠 Orange | Two cells, two same candidates |
| 4 | **Hidden Pair** | 🟠 Orange | Two numbers, same two cells |
| 5 | **Pointing Pair/Triple** | 🟢 Green | Box forces row/col elimination |
| 6 | **Box/Line Reduction** | 🟢 Green | Row/col forces box elimination |
| 7 | **Naked Triple** | 🔵 Blue | Three cells, three candidates |
| 8 | **Hidden Triple** | 🔵 Blue | Three numbers, same three cells |
| 9 | **X-Wing** | 🟣 Purple | Two rows, two columns pattern |
| 10 | **Swordfish** | 🟣 Purple | Three rows, three columns |
| 11 | **XY-Wing** | 🟤 Brown | Three-cell chain elimination |
| 12 | **XYZ-Wing** | 🟤 Brown | Three-cell with triple |
| 13 | **W-Wing** | ⬛ Black | Conjugate pair chain |
| 14 | **Simple Coloring** | ⬛ Black | Color chains of candidates |
| 15 | **Unique Rectangles** | ⬛ Black | Avoid deadly patterns |
| 16+ | Advanced (Forcing Chains, ALS-XZ, Fish, Death Blossom) | 🎓 Master | Expert techniques |

### Tasks

- [ ] **2.1 Tutorial Data Model** (backend)
  - `TutorialLesson` entity: id, technique, title, description, difficulty, belt color
  - `TutorialStep`: order, highlight coords, explanation text, interaction type
  - `ExamplePuzzle`: crafted board + expected candidate state
  - Store in JSON resource files (easy to author/edit)

- [ ] **2.2 Example Puzzle Authoring**
  - Create 2-3 hand-crafted puzzles per technique (30-45 puzzles total)
  - Each must showcase ONLY that technique (other techniques shouldn't shortcut it)
  - Validate: solver needs the target technique to make progress

- [ ] **2.3 Tutorial Engine** (backend)
  - Load tutorial by ID → return lesson + steps + example board
  - Track student progress (which lessons completed, quiz scores)
  - Hint system: given a board state, detect which technique to apply next

- [ ] **2.4 Tutorial UI** (frontend — new TutorialLesson.vue)
  - Split view: board on left, lesson content on right
  - Animated highlighting: pulse/glow effect on relevant cells
  - Step-by-step navigation (Next / Back / Show Me)
  - Interactive: "Which cell can be eliminated?" → click to answer
  - Progress bar and belt badges
  - Kid-friendly: large text, emoji, encouraging messages 🎉

- [ ] **2.5 Tutorial Routes** (web API)
  - `GET /api/v1/tutorials` → list all tutorials with progress
  - `GET /api/v1/tutorials/{id}` → get lesson details + steps
  - `GET /api/v1/tutorials/{id}/board` → get example puzzle with candidates
  - `POST /api/v1/tutorials/{id}/check` → validate student's answer
  - `GET /api/v1/tutorials/progress` → student's overall progress

---

## Phase 3: Progressive Learning & Gamification

**Goal:** Keep kids engaged with progression, achievements, and friendly competition.

### Tasks

- [ ] **3.1 Belt/Rank System**
  - Visual belt display on profile
  - Unlock next belt after completing all tutorials at current level
  - Optional: printable certificate for each belt

- [ ] **3.2 Daily Puzzle Challenge**
  - Curated puzzle of the day at different difficulty levels
  - Tracks: streak, time, hints used
  - Leaderboard (optional, can be personal-only)

- [ ] **3.3 Progress Dashboard** (enhance ParentTeacherDashboard)
  - Which techniques the student has mastered
  - Strengths and weaknesses (which techniques they use most/least)
  - Time spent, puzzles solved, accuracy
  - Parent view: see child's progress without playing for them

- [ ] **3.4 Accessibility**
  - High-contrast mode
  - Larger cell option for younger kids
  - Color-blind friendly palette
  - Keyboard navigation for all interactions

---

## Phase 4: Polish & Mobile

### Tasks

- [ ] **4.1 Responsive Design**
  - Mobile-first grid (touch targets ≥ 44px)
  - Swipe to navigate between tutorial steps
  - Bottom sheet for lesson content on mobile

- [ ] **4.2 Performance**
  - Lazy-load tutorials
  - Candidate computation on backend (avoid heavy JS)
  - Smooth animations (CSS transitions, not JS)

- [ ] **4.3 PWA / Offline**
  - Service worker for offline play
  - Cache completed tutorials and puzzles
  - Install prompt for home screen

---

## Priority Order

1. **Phase 1** (UI/Candidates) — most impactful, foundational for tutorials
2. **Phase 2** (Tutorials) — core educational value, start with techniques 1-5
3. **Phase 3** (Gamification) — engagement layer
4. **Phase 4** (Polish) — shipping quality

Each phase can be broken into individual PRs. Phase 2 should be done incrementally — start with the first 3-5 techniques and ship, then add more.

---

## Completion Status (2026-04-17)

### ✅ Phase 1: Candidate Display — COMPLETE
- [x] 1.1 Candidate Grid Rendering (3×3 mini-grid pencil marks)
- [x] 1.2 Candidate Editing (auto-refresh after cell update)
- [x] 1.3 Visual Highlighting (row/col/box peer highlighting, same-value highlighting)
- [x] 1.4 Backend API (`/api/v1/candidates`)

### ✅ Phase 2: Tutorial System — COMPLETE
- [x] 2.1 Tutorial Data Model (lessons.json with steps, highlights)
- [x] 2.2 Example Puzzle Authoring (15 curated puzzles from SudokuWiki)
- [x] 2.3 Tutorial Engine (backend routes for tutorials, boards, progress)
- [x] 2.4 Tutorial UI (TutorialMode + TutorialSelector with belt groups)
- [x] 2.5 Tutorial Routes (list, get, board, complete, progress)
- **15 tutorials across 7 belt levels** (White → Black)

### ✅ Phase 3: Gamification — COMPLETE
- [x] 3.1 Belt/Rank System (7 belts with emoji + color)
- [x] 3.2 Daily Puzzle Challenge (deterministic rotation, streak tracking)
- [x] 3.3 Progress Dashboard (stats, belt progress visualization)
- [x] 3.4 Accessibility (ARIA labels, keyboard navigation, focus indicators)

### ✅ Phase 4: Polish & Mobile — COMPLETE
- [x] 4.1 Responsive Design (mobile-first grid, touch targets)
- [x] 4.2 Performance (service worker caching, lazy candidates)
- [x] 4.3 PWA (vite-plugin-pwa, offline support, install prompt)
- [x] Splash screen for cold starts
- [x] Share results (Web Share API)
- [x] SVG-generated app icons

### Remaining Ideas (Future)
- [x] Swipe gestures for tutorial step navigation
- [x] High-contrast mode toggle (PR #110)
- [x] Color-blind friendly palette option (PR #110)
- [x] Printable certificates per belt (PR #114)
- [x] Leaderboard (opt-in) (PR #115)
- [x] Tutorial quiz mode "spot the pattern" (PR #113)
- [x] Practice puzzles 2-3 per technique (PR #113)
