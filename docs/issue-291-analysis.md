# Issue #291 Analysis: Tutorial Example Puzzles Too Sparse

**Date:** 2026-05-06
**Status:** Needs puzzle crafting — not a quick fix

## Problem
17/20 tutorial examplePuzzle values are too sparse. Hidden Single can always find a move before the taught technique is needed.

## Testing Results (with PR #290's applyHiddenSinglesUntilStable)
```
❌ naked-single      | expected=Naked Single      | hint=Hidden Single
✅ hidden-single     | expected=Hidden Single     | hint=Hidden Single
❌ naked-pair        | expected=Naked Pair        | hint=Hidden Single
❌ hidden-pair       | expected=Hidden Pair       | hint=Naked Pair
❌ pointing-pair     | expected=Pointing Pair     | hint=Hidden Single
❌ box-line-reduction| expected=Box/Line Reduction| hint=Hidden Single
❌ naked-triple      | expected=Naked Triple      | hint=Hidden Single
❌ hidden-triple     | expected=Hidden Triple     | hint=Hidden Single
❌ x-wing            | expected=X-Wing            | hint=Hidden Single
❌ swordfish         | expected=Swordfish         | hint=Hidden Single
❌ xy-wing           | expected=XY-Wing           | hint=Hidden Single
❌ xyz-wing          | expected=XYZ-Wing          | hint=Hidden Single
❌ unique-rectangle  | expected=Unique Rectangle  | hint=Hidden Single
❌ simple-coloring   | expected=Simple Coloring   | hint=Pointing Pair
❌ w-wing            | expected=W-Wing            | hint=Hidden Single
❌ als-xz            | expected=ALS-XZ            | hint=Hidden Single
❌ franken-fish      | expected=Franken Fish      | hint=Hidden Single
❌ mutant-fish       | expected=Mutant Fish       | hint=Hidden Single
❌ death-blossom     | expected=Death Blossom     | hint=Hidden Single
❌ forcing-chains    | expected=Forcing Chains    | hint=Hidden Single
```

## Deeper Problem
Even adding givens (making puzzles less sparse) doesn't help — the puzzles simply don't contain the taught technique at the right state. After exhausting hidden singles, the hint API returns "Scanning" (no technique found).

SudokuWiki X-Wing puzzles also fail the same way.

## What's Needed
Each tutorial needs a carefully crafted puzzle where:
1. Simple techniques (Naked Single) are exhausted
2. Hidden Singles are exhausted
3. The taught technique is the next required move
4. The puzzle state matches the tutorial's steps/highlighted cells

## Approach Options
1. **Craft puzzles manually:** Use a solver to find states where each technique is available and peel back to givens
2. **Search online:** Find verified technique-demonstration puzzles from Sudoku teaching sites
3. **Generate programmatically:** Take full solutions, remove cells, test against hint API

## Additional Finding
- `TeachingHintProvider.getHint()` doesn't run basic elimination before checking for Naked Singles
- This may explain why Naked Single tutorials also fail
- May need a separate fix for `TeachingHintProvider`

## Recommended Next Steps
1. Create a puzzle crafting tool or script
2. Focus on lower-belt tutorials first (users encounter these first)
3. Each puzzle needs individual validation against the hint API
