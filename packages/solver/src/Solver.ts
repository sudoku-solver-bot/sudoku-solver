import type { Board } from './Board'
import type { CandidateEliminator } from './Eliminators'
import {
    SimpleCandidateEliminator,
    GroupCandidateEliminator,
    HiddenSubsetCandidateEliminator,
    ExclusionCandidateEliminator,
    PointingCandidateEliminator,
    ClaimingCandidateEliminator,
    FishCandidateEliminator,
    SkyscraperCandidateEliminator,
    TwoStringKiteCandidateEliminator,
    WWingCandidateEliminator,
    XYWingCandidateEliminator,
    XYZWingCandidateEliminator,
    UniqueRectanglesCandidateEliminator,
    SimpleColoringCandidateEliminator,
    ForcingChainsCandidateEliminator,
    ALSXZCandidateEliminator,
    DeathBlossomCandidateEliminator,
} from './Eliminators'
import { Coord } from './Coord'

// ---------------------------------------------------------------------------
// SolvingListener (minimal)
// ---------------------------------------------------------------------------

/** Callback interface for observing the solving process. */
export interface SolvingListener {
    onPropagationPassStarted?(): void
    onEliminatorApplied?(techniqueName: string, totalEliminated: number): void
    onCandidatesEliminated?(techniqueName: string, eliminations: Elimination[]): void
    onGuessMade?(coord: Coord, firstCandidate: number, totalCandidates: number): void
    onCellFilled?(coord: Coord, value: number, explanation: string): void
    onBacktracking?(): void
    onSolveComplete?(solved: boolean, timeNanos: number, backtracks: number): void
}

/** Tracks a set of candidate values removed from a specific cell. */
export interface Elimination {
    coord: Coord
    eliminatedValues: number[]
}

/** No-op listener for when observation is not needed. */
export const NoOpListener: SolvingListener = {}

// ---------------------------------------------------------------------------
// SolverConfig
// ---------------------------------------------------------------------------

/** Configuration for the Sudoku solver. */
export class SolverConfig {
    readonly eliminators: readonly CandidateEliminator[]
    readonly deepEliminators: readonly CandidateEliminator[]
    readonly maxRecursionDepth: number

    constructor(
        eliminators?: readonly CandidateEliminator[],
        maxRecursionDepth?: number,
        deepEliminators?: readonly CandidateEliminator[],
    ) {
        this.eliminators = eliminators ?? defaultEliminators()
        this.deepEliminators = deepEliminators ?? defaultDeepEliminators()
        this.maxRecursionDepth = maxRecursionDepth ?? 1000
    }

    /** Factory: only the 3 core eliminators (no advanced techniques). */
    static basic(): SolverConfig {
        return new SolverConfig(
            [
                new SimpleCandidateEliminator(),
                new GroupCandidateEliminator(),
                new ExclusionCandidateEliminator(9),
            ],
            undefined,
            [],
        )
    }
}

/**
 * Default eliminators: all 11 production-ready TS eliminators.
 *
 * Excluded:
 *   EmptyRectangle — makes incorrect eliminations (known bug, breaks solver)
 *   DeathBlossom  — too slow for default set (combinatorial explosion)
 */
function defaultEliminators(): readonly CandidateEliminator[] {
    return [
        new SimpleCandidateEliminator(),
        new GroupCandidateEliminator(),
        new HiddenSubsetCandidateEliminator(),
        new ExclusionCandidateEliminator(9),
        new PointingCandidateEliminator(),
        new ClaimingCandidateEliminator(),
        new FishCandidateEliminator(),
        new SkyscraperCandidateEliminator(),
        new TwoStringKiteCandidateEliminator(),
        new WWingCandidateEliminator(),
        new XYWingCandidateEliminator(),
        new XYZWingCandidateEliminator(),
        new UniqueRectanglesCandidateEliminator(),
        new SimpleColoringCandidateEliminator(),
        new ForcingChainsCandidateEliminator(),
        new ALSXZCandidateEliminator(),
    ]
}

/**
 * Deep eliminators: computationally expensive techniques that only run after
 * simpler eliminators stall. These use internal timeouts to prevent excessive
 * runtime on hard puzzles.
 */
export function defaultDeepEliminators(): readonly CandidateEliminator[] {
    return [
        new DeathBlossomCandidateEliminator(),
    ]
}

// ---------------------------------------------------------------------------
// Solver
// ---------------------------------------------------------------------------

/**
 * Main Sudoku solver using constraint propagation and backtracking.
 *
 * ## Strategy
 * 1. **Constraint propagation**: Apply all configured eliminators iteratively
 *    until no more progress can be made.
 * 2. **Backtracking**: When propagation stalls, pick the cell with the fewest
 *    candidates (MRV heuristic) and try each value recursively.
 *
 * Equivalent to the Kotlin `Solver` class.
 */
export class Solver {
    private readonly config: SolverConfig

    constructor(config?: SolverConfig) {
        this.config = config ?? new SolverConfig()
    }

    /**
     * Solve a puzzle. Returns the solved board or null if no solution exists.
     */
    solve(board: Board, listener: SolvingListener = NoOpListener): Board | null {
        return this.solveInternal(board, 0, listener)
    }

    /**
     * Internal recursive solver with depth tracking.
     *
     * @param board The puzzle board to solve (will be mutated in-place).
     * @param depth Current recursion depth.
     * @param listener Optional observer.
     */
    private solveInternal(board: Board, depth: number, listener: SolvingListener): Board | null {
        // Safety: prevent infinite recursion
        if (depth > this.config.maxRecursionDepth) return null
        if (!board.isValid()) return null
        if (board.isSolved()) return board

        // Phase 1: Constraint propagation — apply all eliminators until stable
        // Phase 1a: Run default (lightweight) eliminators first
        // Phase 1b: When defaults stall, run deep eliminators (expensive, timed)
        //           and return to defaults if they make progress
        listener.onPropagationPassStarted?.()

        let anyProgress = true
        while (anyProgress) {
            anyProgress = false
            // Step 1: Run standard (lightweight) eliminators
            anyProgress = this._runEliminators(this.config.eliminators, board, listener) || anyProgress

            // Step 2: If standards stalled, try deep eliminators
            if (!anyProgress && this.config.deepEliminators.length > 0) {
                anyProgress = this._runEliminators(this.config.deepEliminators, board, listener)
            }
        }

        if (!board.isValid()) return null
        if (board.isSolved()) return board

        // Phase 2: Backtracking with MRV (Minimum Remaining Values) heuristic
        const unresolvedCoord = board.unresolvedCoord()
        if (!unresolvedCoord) return null

        const candidates = board.candidateValues(unresolvedCoord)

        // Multi-candidate = guess point
        if (candidates.length > 1) {
            listener.onGuessMade?.(unresolvedCoord, candidates[0], candidates.length)
        }

        for (const candidateValue of candidates) {
            const newBoard = board.copy()
            newBoard.markValue(unresolvedCoord, candidateValue)

            const explanation =
                candidates.length === 1
                    ? `Naked Single at (${unresolvedCoord.row + 1}, ${unresolvedCoord.col + 1}) — only candidate is ${candidateValue}`
                    : `Guess (try ${candidateValue} among ${candidates.length} candidates) at (${unresolvedCoord.row + 1}, ${unresolvedCoord.col + 1})`

            listener.onCellFilled?.(unresolvedCoord, candidateValue, explanation)

            const result = this.solveInternal(newBoard, depth + 1, listener)
            if (result !== null) {
                return result
            }

            // Backtrack
            listener.onBacktracking?.()
        }

        return null
    }

    /**
     * Run a set of eliminators once each. Returns true if any made progress.
     */
    private _runEliminators(
        eliminators: readonly CandidateEliminator[],
        board: Board,
        listener: SolvingListener,
    ): boolean {
        let anyProgress = false
        for (const eliminator of eliminators) {
            const beforeState = new Map<Coord, Set<number>>()
            for (const c of Coord.all) {
                if (!board.isConfirmed(c)) {
                    beforeState.set(c, new Set(board.candidateValues(c)))
                }
            }

            const changed = eliminator.eliminate(board)
            if (changed) {
                const eliminations: Elimination[] = []
                for (const [coord, beforeValues] of beforeState) {
                    if (!board.isConfirmed(coord)) {
                        const afterValues = new Set(board.candidateValues(coord))
                        const removed = [...beforeValues].filter((v) => !afterValues.has(v))
                        if (removed.length > 0) {
                            eliminations.push({ coord, eliminatedValues: removed })
                        }
                    } else {
                        const confirmedValue = board.value(coord)
                        const removed = [...beforeValues].filter((v) => v !== confirmedValue)
                        if (removed.length > 0) {
                            eliminations.push({ coord, eliminatedValues: removed })
                        }
                    }
                }

                const totalEliminated = eliminations.reduce(
                    (sum, e) => sum + e.eliminatedValues.length,
                    0,
                )
                if (totalEliminated > 0) {
                    listener.onEliminatorApplied?.(eliminator.displayName, totalEliminated)
                    listener.onCandidatesEliminated?.(eliminator.displayName, eliminations)
                }
                anyProgress = true
            }
        }
        return anyProgress
    }
}
