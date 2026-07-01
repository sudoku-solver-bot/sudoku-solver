# ADR-0010: Client-Side Solver Parity & Fallback Strategy

**Date:** 2026-06-26
**Status:** proposed
**Author:** Architect agent 🏗️

## Context

ADR-0009 established the thin-server architecture migration, resulting in two solver implementations: the original Kotlin solver (reference, 21 eliminators) and the TypeScript solver (`@sudoku-dojo/solver`, 20 eliminators). Both are active in production — the TypeScript solver handles solve/validate/candidates client-side, and the Kotlin solver handles hint generation, step-by-step solving, and puzzle generation server-side.

This dual-implementation architecture creates specific risks:
- **Divergence**: Kotlin and TypeScript solvers could produce different results for the same puzzle
- **Incomplete parity**: TypeScript solver has 20 eliminators vs Kotlin's 21 (Missing: `DeathBlossomCandidateEliminator`)
- **Fallback reliability**: When the client-side solver fails or is unavailable, the server fallback must work correctly
- **Testing gaps**: Without systematic parity testing, behavioral differences go undetected

We need a defined strategy for maintaining parity, testing both implementations, and handling fallback.

## Decision

### 1. Parity Level: "Solving Parity"

We define **solving parity** as: for any valid Sudoku puzzle, both solvers produce identical final solutions. We do NOT require identical intermediate steps or eliminator application order — only that the end result matches.

Specifically:
- **In scope**: Same solved board, same validation result, same candidate lists
- **Out of scope**: Identical step ordering, identical hint selection, identical elimination logging

### 2. Testing Strategy

**Unit tests (TypeScript solver, `packages/solver/tests/`)**:
- Each eliminator tested independently with known puzzle positions
- Core data types (Coord, CoordGroup, Board, Bitmask) tested for correctness
- BoardReader tested for format parsing edge cases
- 200+ tests, run via Vitest in CI

**Parity tests (`SolverParity.test.ts`)**:
- A corpus of 50+ puzzles (easy through extreme) solved by both implementations
- Each puzzle is solved by the Kotlin solver first (source of truth), then the TS solver
- Assertions: same final board, same solution, same validation result
- Run in CI on every PR that touches the solver

**Kotlin solver remains the reference implementation**:
- When behavior differs, Kotlin is the source of truth
- The TypeScript solver must be updated to match Kotlin behavior
- Exceptions require explicit documentation and approval

### 3. Fallback Strategy

```typescript
// Client-first with transparent server fallback
async function solveWithFallback(puzzle: string): Promise<Board> {
  try {
    // 1. Try client-side solver (dynamic import, code-split)
    const { solve } = await import('@sudoku-dojo/solver');
    return solve(puzzle);
  } catch (error) {
    // 2. Fall back to server API
    console.warn('Client solver unavailable, falling back to server', error);
    const response = await fetch('/api/v1/solve', {
      method: 'POST',
      body: JSON.stringify({ puzzle }),
    });
    if (!response.ok) throw new Error('Server solve failed');
    return response.json();
  }
}
```

Fallback triggers:
| Condition | Action |
|-----------|--------|
| Dynamic import fails (network/CDN error) | Server fallback |
| Client solver throws exception | Server fallback |
| Client solver returns invalid result (self-check) | Server fallback |
| Server returns error | Show user-facing error |
| Both fail | Show "Unable to solve" error |

### 4. Eliminator Gap Tracking

The TypeScript solver has 20 of 21 eliminators. The gap (`DeathBlossomCandidateEliminator`) is documented as a known limitation:
- Death Blossom applies to <0.1% of puzzles (extreme difficulty only)
- Puzzles requiring Death Blossom will fall back to the Kotlin server solver
- The gap is tracked in issue #445 (TS + CDN evaluation)
- No new eliminator gaps should be introduced without an ADR update

### 5. Versioning & Release

The `@sudoku-dojo/solver` package:
- Uses semantic versioning (MAJOR.MINOR.PATCH)
- MAJOR bump for API-breaking changes (e.g., Board constructor signature change)
- MINOR bump for new eliminators or features
- PATCH bump for bug fixes (behavioral corrections)
- Published to GitHub Packages (private registry)
- Version is pinned in the web-ui's `package.json`

## Consequences

### Positive
- **Defined correctness**: Solving parity is a measurable, testable standard
- **Automated detection**: Parity tests catch divergence before it reaches production
- **Resilient UX**: Client-first with transparent fallback means users rarely see errors
- **Maintainable gap**: Single known eliminator gap, tracked and documented
- **Independent versioning**: Solver package can be updated without touching the web UI

### Negative
- **Ongoing maintenance burden**: Two implementations means twice the work for new features
- **Parity test corpus maintenance**: New puzzle types require new test cases
- **Fallback complexity**: Error handling adds code paths that need testing
- **Package publishing overhead**: Each solver change requires a package publish cycle

### Neutral
- The Kotlin solver retains features the TypeScript solver doesn't have (hints, steps, generation)
- Future architectural decisions (ADR-0009 Phase 3) may reduce the implementation gap
- Death Blossom gap is acceptable for now given its rarity in practice

## Alternatives Considered

| Alternative | Why rejected |
|-------------|-------------|
| Require identical step ordering (full parity) | Eliminator ordering differs by design; TS uses different traversal. Would require complete algorithm rewrite for marginal benefit |
| Drop Kotlin solver entirely | Hint generation, step-by-step, and puzzle generation are not yet ported to TS. Server is still needed for educational features |
| No fallback — always use server | Defeats purpose of thin-server migration; reintroduces latency |
| Auto-generated solver from shared specification | No suitable code generation tool exists for bitmask-based constraint solving; manual port with parity tests is more practical |
| Monkey-patch TS solver to match Kotlin step-by-step | Fragile; any Kotlin change would break the TS mirror. Solving parity is sufficient |

## Related

- ADR-0009: Thin-Server Architecture Migration
- ADR-0006: System Architecture (documents dual solver as current state)
- Issue #272: Port solver to client-side TypeScript (parent epic)
- Issue #444: Extract TypeScript solver into @sudoku-dojo/solver
- Issue #445: Evaluate pure TypeScript + CDN deployment
- Parity test file: `packages/solver/tests/SolverParity.test.ts`
