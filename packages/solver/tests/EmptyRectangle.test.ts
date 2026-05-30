import { describe, it, expect } from 'vitest'
import { Board } from '../src/Board'
import { Coord } from '../src/Coord'
import { MASKS, WILDCARD_PATTERN } from '../src/Bitmask'
import { EmptyRectangleCandidateEliminator } from '../src/Eliminators'

/**
 * Tests demonstrating incorrect eliminations in EmptyRectangleCandidateEliminator.
 *
 * Root cause: The eliminator makes eliminations that are NOT logically forced
 * by the Empty Rectangle pattern for single-row or single-column configurations.
 *
 * The current algorithm treats ANY box where a candidate is confined to a single
 * row (or single column) with ≤2 columns (≤2 rows) as an Empty Rectangle. It then
 * uses a strong link intersecting one of those columns/rows to eliminate from the
 * intersection of the strong link's other endpoint and the other column/row.
 *
 * However, this elimination is not logically forced:
 *
 *   ER: candidate X at (er, c1) and (er, c2) in box (single row, 2 columns)
 *   Strong link in column c1: (er, c1) = (otherR, c1)
 *   Code eliminates X from (otherR, c2)
 *
 * Proof that this is incorrect:
 *   If X is at (otherR, c2):
 *     - (er, c2) cannot have X (same column c2)
 *     - In the box, X must go at (er, c1) — the only remaining position
 *     - Column c1 has X at (er, c1), satisfying the strong link
 *     - (otherR, c1) cannot have X, but this is fine (strong link says X is at one end)
 *   → No contradiction. The elimination is not valid.
 *
 * Additionally, the code does NOT detect true L-shaped Empty Rectangles
 * (candidate confined to BOTH a row AND a column in the box), which is the
 * pattern described in standard sudoku technique references.
 */

describe('EmptyRectangleCandidateEliminator — incorrect eliminations', () => {
    /**
     * Helper: create a board with specific candidate set for a given value.
     * All other cells have all 9 candidates (WILDCARD_PATTERN).
     * Cells with the target candidate get WILDCARD_PATTERN | mask.
     */
    function createBoardWithCandidate(
        value: number,
        positions: [number, number][],
    ): Board {
        const mask = MASKS[value - 1]
        // Start with all cells having all candidates
        const patterns = new Int32Array(81)
        for (let i = 0; i < 81; i++) {
            patterns[i] = WILDCARD_PATTERN
        }
        // Override: only target cells have the specific candidate
        // (all others have all candidates EXCEPT the target value)
        for (let i = 0; i < 81; i++) {
            patterns[i] = WILDCARD_PATTERN & ~mask // all except target
        }
        for (const [r, c] of positions) {
            patterns[r * 9 + c] |= mask // add target candidate back
        }
        return new Board(patterns)
    }

    it('should NOT eliminate when ER is single-row with 2 cols and strong link intersects', () => {
        // Construct:
        // - In box 0, candidate 5 appears ONLY at (0,1) and (0,2)
        // - Column 1 has candidate 5 ONLY at (0,1) and (4,1) — strong link
        // - Cell (4,2) also has candidate 5 (the target for incorrect elimination)
        const value = 5
        const positions: [number, number][] = [
            [0, 1], [0, 2],  // Box 0, row 0: the ER pattern
            [4, 1],          // Column 1 strong link other end
            [4, 2],          // The potential target cell
        ]

        const board = createBoardWithCandidate(value, positions)
        const mask = MASKS[value - 1]

        // Verify setup
        expect(board.candidatePatterns[0 * 9 + 1] & mask).toBeTruthy()  // (0,1)
        expect(board.candidatePatterns[0 * 9 + 2] & mask).toBeTruthy()  // (0,2)
        expect(board.candidatePatterns[4 * 9 + 1] & mask).toBeTruthy()  // (4,1)
        expect(board.candidatePatterns[4 * 9 + 2] & mask).toBeTruthy()  // (4,2)

        const eliminator = new EmptyRectangleCandidateEliminator()
        const changed = eliminator.eliminate(board)

        // The eliminator should NOT remove candidate 5 from (4,2)
        // because placing 5 at (4,2) does not create a contradiction
        const stillHasCandidate = !!(board.candidatePatterns[4 * 9 + 2] & mask)
        expect(stillHasCandidate).toBe(true)
    })

    it('should NOT eliminate when ER is single-column with 2 rows and strong link intersects', () => {
        // Construct:
        // - In box 0, candidate 5 appears ONLY at (1,0) and (2,0)
        // - Row 1 has candidate 5 ONLY at (1,0) and (1,4) — strong link
        // - Cell (2,4) also has candidate 5 (the target for incorrect elimination)
        const value = 5
        const positions: [number, number][] = [
            [1, 0], [2, 0],  // Box 0, col 0: the ER pattern (column-direction)
            [1, 4],          // Row 1 strong link other end
            [2, 4],          // The potential target cell
        ]

        const board = createBoardWithCandidate(value, positions)
        const mask = MASKS[value - 1]

        // Verify setup
        expect(board.candidatePatterns[1 * 9 + 0] & mask).toBeTruthy()  // (1,0)
        expect(board.candidatePatterns[2 * 9 + 0] & mask).toBeTruthy()  // (2,0)
        expect(board.candidatePatterns[1 * 9 + 4] & mask).toBeTruthy()  // (1,4)
        expect(board.candidatePatterns[2 * 9 + 4] & mask).toBeTruthy()  // (2,4)

        const eliminator = new EmptyRectangleCandidateEliminator()
        const changed = eliminator.eliminate(board)

        // The eliminator should NOT remove candidate 5 from (2,4)
        const stillHasCandidate = !!(board.candidatePatterns[2 * 9 + 4] & mask)
        expect(stillHasCandidate).toBe(true)
    })

    it('should NOT eliminate for ER with 1 column (no second column to target)', () => {
        // When cols.size === 1, there's no "other" column to eliminate from.
        // This case is currently harmless but we should verify.
        const value = 5
        const positions: [number, number][] = [
            [0, 1],  // Box 0, row 0, single column
            [4, 1],  // Column 1 strong link
        ]

        const board = createBoardWithCandidate(value, positions)
        const eliminator = new EmptyRectangleCandidateEliminator()
        const changed = eliminator.eliminate(board)

        // No elimination should occur — the inner loop finds no other column
        expect(changed).toBe(false)
    })

    it('should detect true L-shaped Empty Rectangles (currently NOT detected)', () => {
        // A proper L-shaped Empty Rectangle:
        // - Candidate occupies row 0 (cols 1,2) AND column 0 (rows 1,2) in box 0
        // - This forms an L: (0,1), (0,2), (1,0), (2,0)
        // Current code does NOT detect this because rows.size=3 and cols.size=3
        const value = 5
        const positions: [number, number][] = [
            [0, 1], [0, 2],  // Row arm of L in box 0
            [1, 0], [2, 0],  // Column arm of L in box 0
            [5, 1],          // Column 1 strong link
        ]

        const board = createBoardWithCandidate(value, positions)
        const eliminator = new EmptyRectangleCandidateEliminator()
        const changed = eliminator.eliminate(board)

        // Current code does NOT eliminate anything because it doesn't detect
        // the L-shaped pattern (rows.size=3, cols.size=3 — neither equals 1)
        // This test documents the gap: true L-shaped ERs are not handled
        expect(changed).toBe(false)
    })
})
