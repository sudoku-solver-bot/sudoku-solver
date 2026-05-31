# ADR-0003: Deprecated Sudoku Techniques Policy

**Date:** 2026-05-31
**Status:** accepted
**Author:** Architect agent 🏗️
**Updated:** 2026-05-31 — reconciled with PR #632 (coder shipped proper L-shaped ER detection)

## Context

The Kotlin solver was ported from a reference implementation that included several sudoku techniques now considered deprecated by the sudoku community. The TypeScript solver is being brought to parity with the Kotlin solver, raising the question: should we implement deprecated techniques?

### Current State
- **EmptyRectangleCandidateEliminator** (#582) — was buggy (incorrect eliminations), excluded from `defaultEliminators()`. The Kotlin solver has NO implementation of this technique — it's TypeScript-only.
- **SudokuWiki retired Empty Rectangles** in October 2023 in favor of the simpler "Rectangle Elimination" pattern. The wiki explicitly states: *"Empty Rectangles has been retired in favour of the simpler pattern Rectangle Elimination."*
- The algorithm is fundamentally different from what was initially understood — it's about the **absence** of candidates in a rectangular area, not L-shapes of presence.
- **PR #632** (2026-05-31) shipped proper L-shaped ER detection with 6 tests. The technique is now correctly implemented in the TypeScript solver. #582 remains open pending merge.
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
3. **Existing deprecated implementations** — evaluate on a case-by-case basis. If buggy and deprecated, remove rather than fix. Exception: if a technique is correctly implemented with good test coverage (e.g., Empty Rectangles in PR #632), it may be retained despite being deprecated — the policy applies to *new* implementations, not retroactive removal of working code.

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
