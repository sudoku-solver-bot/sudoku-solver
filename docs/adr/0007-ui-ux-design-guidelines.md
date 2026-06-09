# ADR-0007: UI/UX Design Guidelines for Sudoku Grid

**Date:** 2026-06-09
**Status:** accepted
**Author:** Architect Agent 🏗️

---

## Context

Sudoku Dojo's frontend has grown to 32 Vue components with multiple interaction modes (normal, tutorial, quiz, practice). Without formalized UI guidelines, components risk inconsistent behavior — different highlight colors, conflicting cell states, or non-standard interactions. The top sudoku apps (sudoku.com, NYT Sudoku) have established user expectations for grid interaction that we must match.

## Decision

We adopt `docs/UI-GUIDELINES.md` as the canonical UI/UX reference for all frontend development and code reviews. All new and modified components must comply.

### Key Standards

**Cell States:**
| State | Visual Treatment |
|-------|-----------------|
| Empty | Blank — never show `0`, `-`, or placeholder |
| Given (pre-filled) | Bold, not editable, subtle background tint |
| User-entered | Normal weight, editable |
| Error/conflict | Red text or background, shake animation |

**Highlighting (in priority order):**
1. **Selected cell** — thick blue border + background
2. **Same-value** — all cells with same digit get light highlight
3. **Peer highlighting** — row, column, and 3×3 box get subtle gray tint
4. **Pencil mark highlighting** — matching candidates highlighted across grid
5. **Tutorial highlights** — blue, green, red, yellow (system-defined)

**Pencil Marks:**
- 3×3 mini-grid inside cell (digits 1–9)
- Toggle between Normal and Pencil Mark mode
- Auto-remove from peers when a value is confirmed

**Number Bar:**
- Digits 1–9 as clickable buttons
- Show remaining count per digit
- Disable fully-placed digits

**Accessibility:**
- ARIA labels for every cell (row, column, value, or "empty, candidates X, Y, Z")
- Keyboard navigation (arrow keys, number keys, Delete, P for pencil mode)
- Dark mode with WCAG AA contrast (4.5:1 minimum)
- Colorblind and high-contrast modes supported

**Color Palette:**
- Brand: purple gradient `#667eea` → `#764ba2`
- Highlights: blue, green, red, yellow
- Conflicts: red
- Cell colors: user-assignable (yellow, green, blue, pink, orange, purple)

## Consequences

### Positive
- Consistent UX across all 32 components
- Matches industry-standard behavior (sudoku.com, NYT Sudoku)
- Clear reference for code reviews — "does this comply with UI-GUIDELINES.md?"
- Accessibility baked in from the start

### Negative
- Slightly more upfront work for new components
- Must keep guidelines in sync with implementation

### Neutral
- Guidelines are a living document — should be updated as new patterns emerge

## Alternatives Considered

| Alternative | Why rejected |
|-------------|-------------|
| Each component defines its own style | Inconsistent UX, no shared mental model |
| Follow one competitor's design exactly | Limits innovation; our tutorial system is unique |
| No formal guidelines | Already led to drift — this ADR fixes that |

## Related

- `docs/UI-GUIDELINES.md` — the full specification
- `SudokuGrid.vue` — primary implementation reference
- ADR-0006: System Architecture
