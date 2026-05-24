# Sudoku Dojo — User Guide

## Getting Started

### What is Sudoku Dojo?

Sudoku Dojo is an educational Sudoku platform that teaches you solving techniques through interactive tutorials, belt-rank progression, and practice puzzles. Unlike regular Sudoku apps, it doesn't just check your answers — it teaches you *how* to solve puzzles step by step.

### How to Access

**Web App:** Open your browser and navigate to the Sudoku Dojo URL.

**Install as App (PWA):**
- **Chrome/Edge:** Click the install icon (⬇️) in the address bar, or go to Menu → "Install Sudoku Dojo"
- **Safari (iOS):** Tap the Share button (⬆️), then "Add to Home Screen"
- **Android:** Tap the menu (⋮) in your browser, then "Add to Home Screen"

Once installed, Sudoku Dojo works like a native app — launch it from your home screen.

### First Puzzle Walkthrough

1. **Open Sudoku Dojo** — you'll see the main grid with some numbers pre-filled
2. **Select a cell** — tap any empty cell to select it
3. **Enter a number** — use the number pad (bottom) or keyboard (1-9) to fill in your answer
4. **Use hints** — stuck? Tap the 💡 hint button for a guided next step
5. **Check your work** — the app validates your puzzle as you go
6. **Complete the puzzle** — fill all 81 cells correctly to win!

### What's Next?

Once you're comfortable with basic solving, explore:
- **Tutorials** — learn 21+ advanced techniques with guided lessons
- **Practice Mode** — solve puzzles that require specific techniques
- **Belt Progression** — unlock belts as you master techniques

---

## Game Modes

### Free Play

Generate a new puzzle by selecting a difficulty level:

| Difficulty | Givens | Description |
|-----------|--------|-------------|
| **Easy** | 36-45 | Perfect for beginners |
| **Medium** | 30-35 | A gentle challenge |
| **Hard** | 26-29 | Requires some technique |
| **Expert** | 22-25 | Advanced strategies needed |
| **Master** | 17-21 | For the truly dedicated |

Tap **Generate** to create a new puzzle, then solve it at your own pace.

### Import

Have a puzzle from a book, newspaper, or website? Import it:

1. Tap the **Import** button
2. Paste an 81-character puzzle string (use `0` or `.` for empty cells)
   - Example: `000000030002090500080706004900054006...`
3. Tap **Load** to start solving

### Practice Mode

Practice specific techniques with curated puzzle sets:

1. Open the **Tutorial** menu
2. Select a technique you've learned
3. Choose **Practice** to get puzzles that require that technique
4. Solve puzzles filtered to challenge that specific skill

Practice puzzles are grouped by belt level — complete tutorials first to unlock practice sets.

---

## Solving Interface

### Grid Controls

- **Select a cell** — tap any empty or filled cell
- **Enter a number** — tap a number on the pad (bottom) or press 1-9 on keyboard
- **Clear a cell** — select it and press Delete/Backspace, or tap the ✕ button
- **Navigate** — use arrow keys or swipe to move between cells

### Pencil Marks

Pencil marks let you note possible candidates in a cell:

1. Select a cell
2. Toggle pencil mode (✏️ button or keyboard shortcut)
3. Enter numbers — they appear as small notes instead of final answers
4. Toggle off to return to normal entry mode

### Hints

Stuck? Tap the 💡 **Hint** button for a guided next step:

- The hint highlights relevant cells and explains the technique to use
- Follow the hint to learn how the technique works in context
- Hints are part of the learning experience — use them freely!

### Error Checking

The app validates your puzzle as you go:
- **Conflict highlighting** — cells with duplicate numbers in a row/column/box are flagged
- **Completion detection** — the app knows when you've solved the puzzle
- No time penalty for mistakes — this is a learning tool, not a race

### Timer

A timer tracks how long you've been solving. It's informational only — no pressure!

### Keyboard Shortcuts

| Key | Action |
|-----|--------|
| `↑ ↓ ← →` | Navigate between cells |
| `1-9` | Enter a number |
| `Delete` / `⌫` | Clear cell |
| `Ctrl+Z` | Undo |
| `Ctrl+Y` | Redo |
| `?` | Show keyboard shortcuts |
| `Esc` | Deselect cell |

---

## Tutorial System

### Belt Progression

Sudoku Dojo uses a belt-rank system to track your progress. Each belt represents a level of mastery:

| Belt | Techniques | Difficulty |
|------|-----------|------------|
| ⬜ **White** | Naked Single | Beginner |
| 🟡 **Yellow** | Hidden Single | Beginner |
| 🟠 **Orange** | Naked Pair, Hidden Pair | Intermediate |
| 🟢 **Green** | Pointing Pair/Triple, Box/Line Reduction | Intermediate |
| 🔵 **Blue** | Naked Triple, Hidden Triple | Intermediate |
| 🟣 **Purple** | X-Wing, Swordfish | Advanced |
| 🟤 **Brown** | XY-Wing, XYZ-Wing | Advanced |
| ⬛ **Black** | Unique Rectangle, Simple Coloring, W-Wing | Advanced |
| 🏆 **Master** | ALS-XZ, Franken Fish, Mutant Fish, Death Blossom, Forcing Chains | Expert |

Complete all tutorials in a belt to unlock the next one.

### How Tutorials Work

Each tutorial has three parts:

1. **Concept** — learn what the technique is and when to use it
2. **Example Puzzle** — watch the technique applied step-by-step on a real puzzle
3. **Practice** — solve puzzles that require this technique to reinforce your learning

### Technique Catalog

The app teaches 21+ techniques organized by belt level. Each technique is a specific pattern or strategy that helps you eliminate candidates and solve cells. As you progress through the belts, you'll learn increasingly powerful strategies.

### Quizzes

After completing a belt's tutorials, take the belt quiz to test your knowledge:
- Multiple-choice questions about the techniques you've learned
- Pass the quiz to officially unlock the next belt
- Retake quizzes anytime to refresh your knowledge
