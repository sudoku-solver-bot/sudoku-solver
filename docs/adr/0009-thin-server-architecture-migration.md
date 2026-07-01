# ADR-0009: Thin-Server Architecture Migration

**Date:** 2026-06-26
**Status:** proposed
**Author:** Architect agent 🏗️

## Context

Sudoku Dojo's original architecture was a monolithic Ktor server that handled all solving logic, hint generation, and puzzle generation server-side. Every user interaction required an API call, meaning:

- **Latency**: Network round-trip for every solve, validate, and candidate computation
- **Offline impossible**: No functionality without internet connectivity
- **Server load**: All CPU-bound solving work ran on the server, scaling poorly
- **PWA hollow**: The PWA had a service worker but no meaningful offline capability

The thin-server principle states: **the client should do the heavy lifting; the server should be thin.** This is well-established in modern web architecture (e.g., Figma, Excalidraw, Photopea) but was not reflected in Sudoku Dojo's original design.

The Kotlin solver (21 eliminators, bitmask representation, backtracking with MRV) was ~5,000 LOC of pure computation with zero framework dependencies — making it an ideal candidate for porting to TypeScript.

## Decision

We will migrate Sudoku Dojo to a **thin-server architecture** in three phases:

### Phase 1: Client-Side Solver (✅ Complete)
Port the core solver engine from Kotlin to TypeScript, enabling client-side solving without API calls. The TypeScript solver:
- Implements 20 eliminators with behavioral parity to Kotlin
- Uses identical bitmask representation (`number[]` of 9-bit candidate masks)
- Exposes `solve()`, `validate()`, and `candidates()` as a public API
- Is published as `@sudoku-dojo/solver` — a standalone npm package with zero framework dependencies

### Phase 2: Extract as Standalone Package (✅ Complete)
Extract the solver from `web-ui/src/solver/` into `packages/solver/` as `@sudoku-dojo/solver`:
- Enables independent versioning, testing, and publishing
- Clear package boundary prevents solver from importing Vue or DOM APIs
- Can be used by other projects independently of Sudoku Dojo
- 200+ unit tests with Vitest, runnable in CI without a browser

### Phase 3: Progressive Server Reduction (🟡 In Progress)
Gradually move remaining server-side logic to the client where feasible:
- `solve` and `validate` already client-first with server fallback
- `candidates` already client-first
- `hint` (next hint) remains server-only — requires puzzle generation and step tracking
- `steps` (step-by-step) remains server-only — requires full solving pipeline
- Future: evaluate client-side hint generation and step tracking

### Client-First with Server Fallback
```typescript
// In api.ts — dynamic import with server fallback
async function solve(puzzle: string): Promise<Board> {
  try {
    const { solve } = await import('@sudoku-dojo/solver');
    return solve(puzzle); // client-side, zero latency
  } catch {
    return apiPost('/api/v1/solve', { puzzle }); // server fallback
  }
}
```

### What Stays on the Server
| Function | Reason |
|----------|--------|
| Hint generation (`/api/v1/hint`) | Requires full solving pipeline + step tracking |
| Step-by-step solving (`/api/v1/steps`) | Complex multi-step output, tutorial integration |
| Puzzle generation (`/api/v1/generate`) | Difficulty guarantees require server-side validation |
| Tutoring/difficulty display | Server-side computation remains reference implementation |

### What Moves to Client
| Function | Status |
|----------|--------|
| `solve()` | ✅ Client-first (TS solver) |
| `validate()` | ✅ Client-first (TS solver) |
| `candidates()` | ✅ Client-first (TS solver) |
| `hint()` | ❌ Server-only for now |
| `generate()` | ❌ Server-only for now |

## Consequences

### Positive
- **Zero-latency solving**: Client-side solve/validate completes in <100ms (vs 200-500ms API round-trip)
- **True offline support**: PWA becomes genuinely usable offline for core solving features
- **Reduced server load**: CPU-bound solving work shifts to client devices
- **Reusable package**: `@sudoku-dojo/solver` can power other projects (CLI tools, embedding in other apps, future CDN deployment)
- **Testable in isolation**: TypeScript solver has its own test suite (200+ tests) independent of the web UI
- **Clean architecture boundary**: Solver package has zero framework imports — pure computation

### Negative
- **Dual implementation maintenance**: Kotlin and TypeScript solvers must stay in sync
- **Hint/steps still server-only**: Educational features require network connectivity
- **Initial porting effort**: ~8-12 hours to port 20 eliminators with full parity
- **Client bundle size**: Solver package adds to the JS bundle (mitigated by dynamic import + tree shaking)

### Neutral
- The Kotlin solver remains the reference implementation and source of truth
- Future eliminator additions require dual implementation until Phase 3 is complete
- CDN deployment of the solver package is enabled but not yet pursued (evaluated in #445)

## Alternatives Considered

| Alternative | Why rejected |
|-------------|-------------|
| Keep solver server-only | Network latency, no offline support, contradicts PWA promise |
| Rewrite entire app as pure TypeScript SPA | Too large a scope; Kotlin backend has valuable features (hints, generation, tutorials) that are hard to port |
| Use WebAssembly to compile Kotlin solver | WASM tooling for Kotlin/JS adds complexity; TypeScript is more maintainable for the frontend team |
| Port ALL server features to client immediately | Hint generation and step-by-step are complex; incremental migration reduces risk |
| Keep solver in `web-ui/src/solver/` (no package extraction) | Tighter coupling to Vue, harder to test independently, can't version separately |

## Related

- Parent epic: #272 (port solver to client-side TypeScript)
- Implementation issues: #310–#317 (8-part breakdown)
- Package extraction: #444 (extract into @sudoku-dojo/solver)
- Architecture evaluation: #445 (pure TS + CDN evaluation)
- Solver parity: ADR-0010 (Client-Side Solver Parity & Fallback Strategy)
- System architecture: ADR-0006
