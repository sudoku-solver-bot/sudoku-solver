# ADR-0003: Deprecated Sudoku Techniques Policy

**Date:** 2026-05-31
**Status:** proposed
**Author:** Architect agent 🏗️

## Context

The Kotlin solver was ported from a reference implementation that included several sudoku techniques now considered deprecated by the sudoku community. The TypeScript solver is being brought to parity with the Kotlin solver, raising the question: should we implement deprecated techniques?

### Current State
- **EmptyRectangleCandidateEliminator** (#582) — has a known bug (incorrect eliminations), currently excluded from `defaultEliminators()`. The Kotlin solver has NO implementation of this technique — it's TypeScript-only.
- **SudokuWiki retired Empty Rectangles** in October 2023 in favor of the simpler "Rectangle Elimination" pattern. The wiki explicitly states: *"Empty Rectangles has been retired in favour of the simpler pattern Rectangle Elimination."*
- The algorithm is fundamentally different from what was initially understood — it's about the **absence** of candidates in a rectangular area, not L-shapes of presence.
- Several other techniques in the solver may also be deprecated or subsumed by simpler equivalents.

### The Problem
Deprecated techniques are often:
- More complex than their replacements
- Harder to implement correctly (reference implementations like Hodoku Java needed)
- Potentially confusing to users learning sudoku
- A maintenance burden with little practical value

## Decision

We will **not implement deprecated sudoku techniques** in the TypeScript solver unless they are required for backward compatibility with the Kotlin solver's output.

Specifically:
1. **Empty Rectangles** — skip. Use Rectangle Elimination or Grouped X-Cycles as the equivalent technique.
2. **Future technique ports** — before implementing any Kotlin technique in TypeScript, verify it hasn't been deprecated by the sudoku community (check SudokuWiki, Hodoku).
3. **Existing deprecated implementations** — evaluate on a case-by-case basis. If buggy and deprecated, remove rather than fix (see #582).

## Consequences

### Positive
- Smaller, simpler codebase — fewer eliminators to maintain
- Better user experience — users learn current, widely-recognized techniques
- Faster implementation — skip complex algorithms with simpler replacements
- Clearer architecture — each technique has one canonical implementation

### Negative
- Loss of parity with the Kotlin solver if it implements deprecated techniques
- Some users may expect to see specific technique names (e.g., "Empty Rectangle")
- Historical puzzles solved by the Kotlin solver may use different techniques

### Neutral
- The TypeScript solver becomes the reference implementation for technique selection
- Technique documentation may need updates to reflect the policy

## Alternatives Considered

| Alternative | Why rejected |
|-------------|-------------|
| **Implement all Kotlin techniques for parity** | Maintains deprecated code, increases complexity, confuses users |
| **Implement behind a feature flag** | Adds configuration complexity, still requires maintenance, rarely used |
| **Implement correctly anyway** | Significant effort for a deprecated technique with a simpler replacement |
| **Port only if user requests it** | Reactive approach; better to have a clear policy upfront |

## References

- SudokuWiki: Empty Rectangles retired (October 2023)
- Hodoku: Empty Rectangles implementation
- Issue #582: EmptyRectangleCandidateEliminator produces incorrect eliminations
- Issue #585: Kotlin ↔ TypeScript solver parity gap
