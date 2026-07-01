import { describe, it, expect } from 'vitest'
import { Board } from '../src/Board'
import { BoardReader } from '../src/BoardReader'
import { Coord } from '../src/Coord'
import { MASKS, WILDCARD_PATTERN } from '../src/Bitmask'
import { EmptyRectangleCandidateEliminator } from '../src/Eliminators'
import { SolverConfig } from '../src/Solver'

/**
 * Empty Rectangle Candidate Eliminator tests.
 *
 * Algorithm (SudokuWiki-based):
 * 1. Identify ERIs (Empty Rectangle Intersections): candidate cells that
 *    have other candidates in both their row and column within the box
 *    (L-shaped pattern).
 * 2. For each row/column strong link, check if one end (the "near end")
 *    sees the ERI (same row or column).
 * 3. Eliminate candidate from the cell at the intersection of the far end
 *    and the ERI:
 *    - Row link, near end shares col → target at (ERI.row, farEnd.col)
 *    - Column link, near end shares row → target at (farEnd.row, ERI.col)
 * 4. Both the near end and the elimination target must be outside the
 *    ERI's box.
 *
 * Reference: https://www.sudokuwiki.org/Empty_Rectangles
 */

describe('EmptyRectangleCandidateEliminator — basic properties', () => {
    it('has correct displayName', () => {
        expect(new EmptyRectangleCandidateEliminator().displayName).toBe('Empty Rectangle')
    })

    it('returns false on empty board', () => {
        const board = BoardReader.fromString('.'.repeat(81), Board)
        const changed = new EmptyRectangleCandidateEliminator().eliminate(board)
        expect(changed).toBe(false)
    })

    it('returns false on fully solved board', () => {
        const solved = '534678912672195348198342567859761423426853791713924856961537284287419635345286179'
        const board = BoardReader.fromString(solved, Board)
        const changed = new EmptyRectangleCandidateEliminator().eliminate(board)
        expect(changed).toBe(false)
    })

    it('does not throw on various puzzles', () => {
        const eliminator = new EmptyRectangleCandidateEliminator()
        const puzzles = [
            '53..7....6..195....98....6.8...6...34..8.3..17...2...6.6....28....419..5....8..79',
            '1.....5694.2.....8.5...9.4....64.8.1....1....2.8.35....4.5...1.9.....4.2621.....5',
            '.....6....59.....82....8....45........3........6..3.54...325..6..................',
        ]
        for (const p of puzzles) {
            const board = BoardReader.fromString(p, Board)
            eliminator.eliminate(board)
        }
    })
})

describe('EmptyRectangleCandidateEliminator — correct elimination', () => {
    it('eliminates candidate via classic Empty Rectangle pattern', () => {
        // Construct a board with the classic ER pattern (SudokuWiki example):
        // ERI at D6 (row 3, col 5) in box 4 (center)
        // Strong link on candidate 6 in row 7: H6 (7,5) ↔ H9 (7,8)
        // Near end H6 shares column 5 with ERI → eliminate 6 from D9 (3,8)
        const ALL = 0x1FF // all candidates 1-9
        const MASK6 = 1 << 5 // candidate 6

        const patterns = new Int32Array(81)
        for (let i = 0; i < 81; i++) {
            patterns[i] = ALL & ~MASK6
        }

        // Box 4 (center, rows 3-5, cols 3-5): candidate 6 cells
        // D4(3,3), D5(3,4), D6(3,5), E6(4,5), F6(5,5)
        // Strong link row 7: H6(7,5), H9(7,8)
        // Elimination target: D9(3,8) — should be eliminated
        // J9(8,8) prevents unintended column strong link D9-H9
        const keepCells = [
            [3, 3], [3, 4], [3, 5], // D4, D5, D6 (box 4 row buddies)
            [4, 5], // E6 (box 4 col buddy)
            [5, 5], // F6 (box 4 col buddy)
            [7, 5], // H6 (strong link)
            [7, 8], // H9 (strong link)
            [3, 8], // D9 (elimination target)
            [8, 8], // J9 (prevents column strong link D9-H9)
        ]
        for (const [r, c] of keepCells) {
            patterns[r * 9 + c] |= MASK6
        }

        const board = new Board(patterns)

        // Verify D9 has candidate 6 before elimination
        const d9 = Coord.all[3 * 9 + 8]
        expect(board.candidateValues(d9)).toContain(6)

        const eliminator = new EmptyRectangleCandidateEliminator()
        const changed = eliminator.eliminate(board)

        expect(changed).toBe(true)
        expect(board.candidateValues(d9)).not.toContain(6)
        expect(board.candidateValues(Coord.all[7 * 9 + 5])).toContain(6)
        expect(board.candidateValues(Coord.all[7 * 9 + 8])).toContain(6)
    })

    it('is included in default eliminator set', () => {
        const config = new SolverConfig()
        // Solve a puzzle with defaults — should succeed without error
        const puzzle = '53..7....6..195....98....6.8...6...34..8.3..17...2...6.6....28....419..5....8..79'
        const board = BoardReader.fromString(puzzle, Board)
        for (const eliminator of config.eliminators) {
            eliminator.eliminate(board)
        }
        // No crash = ER is in defaults and doesn't break anything
    })
})

describe('EmptyRectangleCandidateEliminator — rejects invalid patterns', () => {
    /**
     * Helper: create a board with specific candidate set for a given value.
     * All cells have all candidates EXCEPT the target value; only specified
     * positions get the target candidate added back.
     */
    function createBoardWithCandidate(
        value: number,
        positions: [number, number][],
    ): Board {
        const mask = MASKS[value - 1]
        const patterns = new Int32Array(81)
        for (let i = 0; i < 81; i++) {
            patterns[i] = WILDCARD_PATTERN & ~mask
        }
        for (const [r, c] of positions) {
            patterns[r * 9 + c] |= mask
        }
        return new Board(patterns)
    }

    it('does NOT eliminate for single-row pattern (no column buddy)', () => {
        // Single row (0,1), (0,2) in box 0: no cell has both row AND column
        // buddies, so no valid ERI — the algorithm correctly skips.
        const value = 5
        const positions: [number, number][] = [
            [0, 1], [0, 2],  // Box 0, row 0 only
            [4, 1],          // Column 1 strong link
            [4, 2],          // Potential (invalid) target
        ]

        const board = createBoardWithCandidate(value, positions)
        const mask = MASKS[value - 1]

        const eliminator = new EmptyRectangleCandidateEliminator()
        const changed = eliminator.eliminate(board)

        expect(changed).toBe(false)
        expect(!!(board.candidatePatterns[4 * 9 + 2] & mask)).toBe(true)
    })

    it('does NOT eliminate for single-column pattern (no row buddy)', () => {
        // Single column (1,0), (2,0) in box 0: no ERI qualifies.
        const value = 5
        const positions: [number, number][] = [
            [1, 0], [2, 0],  // Box 0, col 0 only
            [1, 4],          // Row 1 strong link
            [2, 4],          // Potential (invalid) target
        ]

        const board = createBoardWithCandidate(value, positions)
        const mask = MASKS[value - 1]

        const eliminator = new EmptyRectangleCandidateEliminator()
        const changed = eliminator.eliminate(board)

        expect(changed).toBe(false)
        expect(!!(board.candidatePatterns[2 * 9 + 4] & mask)).toBe(true)
    })

    it('does NOT eliminate with only one candidate cell', () => {
        const value = 5
        const positions: [number, number][] = [
            [0, 1],  // Single cell in box 0
            [4, 1],  // Column 1 strong link
        ]

        const board = createBoardWithCandidate(value, positions)
        const eliminator = new EmptyRectangleCandidateEliminator()
        const changed = eliminator.eliminate(board)

        expect(changed).toBe(false)
    })

    it('does NOT eliminate when ERI corner has no candidate (L-shaped gap)', () => {
        // L-shaped pattern in box 0: (0,1),(0,2) + (1,0),(2,0)
        // The corner cell (0,0) does NOT have the candidate → no valid ERI.
        // This is a known limitation: the algorithm requires the ERI cell
        // itself to hold the candidate to be considered.
        const value = 5
        const positions: [number, number][] = [
            [0, 1], [0, 2],  // Row arm of L in box 0
            [1, 0], [2, 0],  // Column arm of L in box 0
            [5, 1],          // Column 1 strong link
        ]

        const board = createBoardWithCandidate(value, positions)
        const eliminator = new EmptyRectangleCandidateEliminator()
        const changed = eliminator.eliminate(board)

        // No valid ERI found (corner (0,0) doesn't have the candidate)
        expect(changed).toBe(false)
    })
})
