/**
 * Client-side Sudoku solver API.
 *
 * Re-exports all solver modules and provides convenience functions
 * for the most common operations: solve, validate, getCandidates.
 *
 * Usage:
 * ```ts
 * import { solve, validate, getCandidates } from '@/solver'
 *
 * const result = solve('53..7....6..195....98....6.8...6...34..8.3..17...2...6.6....28....419..5....8..79')
 * if (result) console.log(result)
 * ```
 */

// ---------------------------------------------------------------------------
// Public API
// ---------------------------------------------------------------------------

import { BoardReader } from './BoardReader'
import { Board } from './Board'
import { Coord } from './Coord'
import { Solver, SolverConfig } from './Solver'
import type { CandidateEliminator } from './Eliminators'

/**
 * Solve a Sudoku puzzle.
 *
 * @param puzzle 81-character puzzle string. Supports '.' and '0' for empty cells.
 * @param eliminators Optional custom eliminators. Uses default 3 core eliminators if omitted.
 * @returns Solved puzzle string (81 chars), or null if no solution exists.
 * @throws If the puzzle string is not 81 characters.
 */
export function solve(
    puzzle: string,
    eliminators?: readonly CandidateEliminator[],
): string | null {
    const board = BoardReader.fromString(puzzle, Board)
    const config = eliminators ? new SolverConfig(eliminators) : undefined
    const solver = new Solver(config)
    const result = solver.solve(board)
    if (!result) return null

    // Convert solved board back to 81-char string
    return BoardReader.boardToString(result)
}

/**
 * Validate a puzzle: check if it's solvable and has exactly one solution.
 *
 * @param puzzle 81-character puzzle string.
 * @returns Object with `valid` flag and optional `error` message.
 */
export function validate(puzzle: string): { valid: boolean; error?: string } {
    if (puzzle.replace(/\s/g, '').length !== 81) {
        return { valid: false, error: 'Puzzle must be exactly 81 characters' }
    }

    for (const ch of puzzle.replace(/\s/g, '')) {
        if (ch !== '.' && ch !== '0' && !(ch >= '1' && ch <= '9')) {
            return { valid: false, error: `Invalid character: '${ch}'` }
        }
    }

    try {
        const board = BoardReader.fromString(puzzle, Board)
        if (!board.isValid()) {
            return { valid: false, error: 'Puzzle has contradictions' }
        }

        const solver = new Solver()
        const solution = solver.solve(board)
        if (!solution) {
            return { valid: false, error: 'No solution found' }
        }

        return { valid: true }
    } catch (e) {
        return { valid: false, error: e instanceof Error ? e.message : String(e) }
    }
}

/**
 * Get all candidate values for a specific cell.
 *
 * @param puzzle 81-character puzzle string.
 * @param row 0-based row index (0-8).
 * @param col 0-based column index (0-8).
 * @returns Array of candidate values (1-9), or empty array if cell is confirmed.
 */
export function getCandidates(
    puzzle: string,
    row: number,
    col: number,
): number[] {
    const board = BoardReader.fromString(puzzle, Board)

    // Apply eliminators to narrow candidates
    const solver = new Solver()
    solver.solve(board) // This mutates the board but also narrows candidates

    const coord = Coord.all[row * 9 + col]
    return board.candidateValues(coord)
}

// ---------------------------------------------------------------------------
// Re-exports
// ---------------------------------------------------------------------------

export { BoardReader } from './BoardReader'
export { Board } from './Board'
export { Coord } from './Coord'
export { CoordGroup } from './CoordGroup'
export { Solver, SolverConfig, NoOpListener } from './Solver'
export type { SolvingListener, Elimination } from './Solver'
export {
    SimpleCandidateEliminator,
    GroupCandidateEliminator,
    ExclusionCandidateEliminator,
} from './Eliminators'
export type { CandidateEliminator } from './Eliminators'
export { SIZE, REGION_SIZE, SYMBOLS, MASKS, WILDCARD_PATTERN, bitCount } from './Bitmask'
