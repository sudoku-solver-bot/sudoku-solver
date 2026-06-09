# Sudoku Dojo — UI/UX Guidelines

**Version:** 1.0  
**Date:** 2026-05-25  
**Status:** Accepted  
**Author:** Architect Agent  

---

## 1. Cell Representation

### Empty Cells
- **Visually blank** — empty cells must appear as empty spaces, never as `0`, `-`, or any placeholder character.
- The internal representation uses `.` (period) as the standard empty marker.
- The API wire format uses `0` for empty cells — this is a backend convention only and must never leak into the UI.
- When importing puzzles, both `.` and `0` should be accepted and normalized to internal `.` representation.

### Given (Pre-filled) Cells
- Displayed in **bold** or a distinct weight to distinguish from user-entered values.
- Not editable — `readonly` attribute set on the input.
- Visually distinct background or text color (e.g., darker text, subtle background tint).

### User-Entered Cells
- Normal weight text.
- Editable when selected.
- Should visually match given cells once entered (same font size, color).

### Error Cells
- Cells with conflicting values (duplicate in row/column/box) should show a clear error state:
  - Red text or red background tint
  - Optional shake animation on conflict detection
- Error state should update in real-time as the user types.

---

## 2. Cell Selection & Highlighting

### Selected Cell
- Clear border highlight (e.g., thick blue border, `box-shadow: inset 0 0 0 3px`).
- Background color change to indicate active focus.

### Same-Value Highlighting (When a Cell with a Known Value is Selected)
When the user selects a cell that contains a value (1-9):
1. **Highlight all other cells with the same value** across the entire grid.
2. Use a subtle background color (e.g., light blue `#e3f2fd` or light green) — distinct from the selected cell's highlight.
3. The selected cell itself should have a stronger highlight (e.g., bold border + slightly darker background).
4. This helps users quickly see where a number appears and identify missing positions.

**Example behavior (standard across sudoku.com, NYT Sudoku, Sudoku.com apps):**
- Select cell with value `5` → all other `5`s on the board get a light highlight.
- The selected cell gets a stronger highlight (border + background).
- Rows/columns/boxes that already contain the selected value are easier to scan.

### Peer Highlighting (Row/Column/Box)
When any cell is selected (empty or not):
- Highlight the **entire row, column, and 3×3 box** containing the selected cell.
- Use a very subtle background tint (e.g., `#f5f5f5` light gray).
- This helps users see the constraints that apply to the selected cell.

---

## 3. Pencil Mark (Candidate) Mode

### Entering Pencil Marks
- Toggle between "Normal" mode and "Pencil Mark" mode (button or keyboard shortcut).
- In Pencil Mark mode, typing a number adds/removes it as a candidate in the selected cell.
- Pencil marks display as a 3×3 mini-grid inside the cell (digits 1-9).

### Pencil Mark Highlighting
When a cell with pencil marks is selected:
1. **Highlight all cells that contain the same pencil mark(s).**
2. For each candidate digit in the selected cell, highlight all other cells that have that digit as a candidate.
3. Use subtle background tints per candidate (e.g., light colors from a palette).
4. This helps users spot hidden singles, naked pairs, and other techniques.

**Example behavior:**
- Select empty cell with candidates `{2, 5, 8}`:
  - All cells with candidate `2` get a light highlight.
  - All cells with candidate `5` get a different light highlight.
  - All cells with candidate `8` get another light highlight.
  - The selected cell gets a stronger highlight (border + background).

### Pencil Mark Removal
- When the user enters a confirmed value in a cell, automatically remove that digit from pencil marks in peer cells (same row/column/box).
- This is standard behavior across most sudoku apps.

---

## 4. Number Bar / Input Methods

### Number Bar
- Display digits 1-9 as clickable buttons below or beside the grid.
- Each button should show:
  - The digit itself.
  - Optionally, a count of how many of that digit are still missing (e.g., "5 (2 remaining").
- Disabled state for digits that are fully placed (all 9 instances on the board).
- Highlight the button matching the selected cell's value.

### Mobile Number Pad
- Same as number bar but optimized for touch input.
- Larger touch targets (minimum 44×44px per Apple HIG / Material Design).

### Keyboard Input
- Arrow keys for navigation.
- Number keys for value entry.
- Delete/Backspace to clear a cell.
- `P` or similar key to toggle pencil mark mode.

---

## 5. Color Coding

### Conflict Detection
- **Red** for conflicts (duplicate values in row/column/box).
- Applied to both the conflicting cell and the cell it conflicts with.

### Cell Colors (User-Assigned)
- Users can assign colors to cells for marking techniques or regions.
- Standard palette: yellow, green, blue, pink, orange, purple.
- Color applied as a subtle background tint, not obscuring the value.

### Error Prevention
- If the user enters a value that would create a conflict, optionally:
  - Show the conflict immediately (real-time validation).
  - Or prevent the entry (stricter mode).
- Real-time validation is preferred for learning apps.

---

## 6. Visual Design

### Grid Layout
- 9×9 grid with clear 3×3 box boundaries (thicker borders or spacing).
- Cells should be square (equal width and height).
- Responsive sizing: `clamp()` for font sizes on mobile.

### Typography
- Given values: bold, larger font (e.g., 20px+).
- User-entered values: same size, normal weight.
- Pencil marks: smaller font (e.g., 8-10px), positioned in a 3×3 grid within the cell.

### Dark Mode
- Invert colors appropriately (dark background, light text).
- Maintain contrast ratios (WCAG AA minimum 4.5:1 for text).
- Highlights should use lighter tints on dark backgrounds.

### Accessibility
- ARIA labels for each cell (e.g., "Row 1, Column 3: value 5").
- ARIA labels for empty cells (e.g., "Row 1, Column 3: empty, candidates 2, 5, 8").
- Keyboard navigation support.
- Focus indicators for screen readers.

---

## 7. State Management

### Undo/Redo
- Full undo/redo stack for all user actions (value entry, pencil mark changes, color changes).
- Keyboard shortcuts: Ctrl+Z / Ctrl+Shift+Z.

### Auto-Save
- Save puzzle state to localStorage periodically and on page unload.
- Resume from saved state on return.

### Timer
- Display elapsed time in MM:SS format.
- Pause when the puzzle is not in focus or when the user is in a menu.

---

## 8. References

### Industry Standard Behavior
The following behaviors are consistent across major sudoku implementations:

| Feature | sudoku.com | NYT Sudoku | Web Sudoku | Sudoku Dojo (proposed) |
|---------|-----------|------------|------------|----------------------|
| Empty cell display | Blank | Blank | Blank | Blank |
| Same-value highlight | ✅ | ✅ | ❌ | ✅ |
| Peer highlighting (row/col/box) | ✅ | ✅ | ❌ | ✅ |
| Pencil mark mode | ✅ | ✅ | ✅ (basic) | ✅ |
| Pencil mark highlighting | ✅ | ❌ | ❌ | ✅ |
| Conflict detection | ✅ | ✅ | ❌ | ✅ |
| Auto-remove pencil marks | ✅ | ✅ | ❌ | ✅ |
| Dark mode | ✅ | ✅ | ❌ | ✅ |

### Key Insight
The top two sudoku apps (sudoku.com, NYT Sudoku) both implement same-value highlighting and peer highlighting as core UX features. These are not optional polish — they are expected by users. The pencil mark highlighting is a differentiator that helps with learning and advanced techniques.

---

## 9. Implementation Priority

| Priority | Feature | Effort | Impact |
|----------|---------|--------|--------|
| P0 | Empty cells display as blank (no `0` visible) | Low | Critical — current bug |
| P0 | Same-value highlighting on selection | Low | High — expected UX |
| P0 | Peer highlighting (row/col/box) | Low | High — expected UX |
| P1 | Pencil mark mode toggle | Medium | High — core feature |
| P1 | Pencil mark highlighting | Medium | Medium — learning aid |
| P1 | Auto-remove pencil marks on value entry | Low | Medium — quality of life |
| P2 | Conflict detection (real-time) | Medium | Medium — learning aid |
| P2 | Number bar with remaining count | Low | Low — nice to have |
| P2 | Cell color coding | Medium | Low — power user feature |
| P3 | Dark mode highlights | Low | Low — accessibility |

---

*This guide should be referenced in all frontend development and code reviews. Any new UI components must comply with these guidelines.*
