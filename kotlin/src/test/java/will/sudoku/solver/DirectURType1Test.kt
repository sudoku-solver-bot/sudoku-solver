package will.sudoku.solver

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

/**
 * Directly test UR Type 1 by creating a board with a known UR pattern.
 */
class DirectURType1Test {

    private fun boxIdx(r: Int, c: Int): Int = (r / 3) * 3 + c / 3

    @Test
    fun `UR Type 1 fires on board with deadly rectangle and one extra candidate cell`() {
        // Build a grid where UR corners are at (0,0), (0,3), (1,0), (1,3)
        // in boxes 0 and 1.
        // Need: 3 cells with exactly {2,9} and 1 cell with {2,9,5} (or similar)

        // Key insight: to give (1,3) an extra candidate, I need 5 to NOT be eliminated
        // from (1,3) by row/col/box constraints.
        // - Row 1 should not have 5 elsewhere → remove 5 from row 1
        // - Col 3 should not have 5 elsewhere → remove 5 from col 3
        // - Box 1 should not have 5 elsewhere → remove 5 from box 1

        // Build rows 0-2:
        // R0: 2, 3, 4, 9, 5, 6, 7, 8, 1  → has 5 at col 4
        // R1: 9, 6, 7, _, 8, 1, 3, 4, _  → NO 5! Missing {2,5} at cols 3,8
        // R2: 5, 8, 1, 4, _, 7, 6, 9, 2  → has 5 at col 0. Remove col 4 value too.

        // Wait, R2C4 is in box 1. If we remove it, box 1 might be missing 5.
        // But (0,4)=5 is in box 1. So box 1 already has 5.
        // For (1,3) to have 5 as candidate: box 1 should not have 5? No!
        // SimpleCandidateEliminator removes candidates that appear as given values in the same unit.
        // So if 5 appears anywhere in box 1, it's eliminated from (1,3).
        // (0,4)=5 is in box 1 → 5 is eliminated from (1,3)!

        // I need to remove 5 from box 1 entirely. But then box 1 would be invalid...
        // Unless (1,3) IS where 5 goes. But (1,3) is an empty cell.

        // Actually for UR Type 1: we want (1,3) to have candidates {2,5,9}.
        // 5 must NOT appear in row 1, col 3, or box 1 (as a given).
        // But if 5 doesn't appear in box 1 as a given, it's fine — 5 can be a candidate.

        // R0: 2, 3, 4, 9, _, 6, 7, 8, 1  → col 4 removed (was 5) → box 1 now missing 5
        // R1: 9, 6, 7, _, 8, 1, 3, 4, _  → missing {2,5} → (1,3) candidates include 2,5
        // R2: 5, 8, 1, 4, _, 7, 6, 9, 2  → col 4 removed → box 1 missing 5 and more

        // Hmm but if box 1 has no 5 as given, then all empty cells in box 1 have 5 as candidate.
        // That's fine for UR — we just need (1,3) to have {2,5,9}.

        // Let me redesign:
        // R0: 2, 3, 4, 9, _, 6, 7, 8, 1  → remove (0,4), was 5
        // R1: 9, 6, 7, _, _, 1, 3, 4, _  → remove (1,3) and (1,4) and (1,8)
        // R2: _, 8, 1, 4, 3, 7, 6, 9, 2  → remove (2,0), was 5

        // Box 1 givens: (0,3)=9,(0,4)=0,(0,5)=6,(1,3)=0,(1,4)=0,(1,5)=1,(2,3)=4,(2,4)=3,(2,5)=7
        // = {1,3,4,6,7,9} → missing {2,5,8}
        // (1,3) candidates = row1_missing ∩ col3_missing ∩ box1_missing
        // row1 has: 9,6,7,_,_,1,3,4,_ → {1,3,4,6,7,9} → missing {2,5,8}
        // col3 has: 9,_,4,... → need to check col 3 values below row 2

        // This is getting very complex. Let me try a different approach:
        // Remove (0,4) from givens to make 5 a candidate in box 1.

        val partial = intArrayOf(
            2, 3, 4, 9, 0, 6, 7, 8, 1,  // (0,4) removed to allow 5 as candidate in box 1
            9, 6, 7, 0, 8, 1, 3, 4, 5,  // (1,3) removed (was 2). Row has {1,3,4,5,6,7,8,9}, missing {2}
            5, 8, 1, 4, 3, 7, 6, 9, 2,  // complete
            0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0
        )

        // Check row 1: 9,6,7,_,8,1,3,4,5 → has 5! So (1,3) can't have 5 as candidate.
        // Row 1 missing = {2}. So (1,3) can only be 2.
        // That's a hidden single — UR won't fire.

        // I need row 1 to NOT have 5. Let me swap 5 to a different position:
        // R1: 9, 6, 7, _, 8, 1, 3, 5, 4  → 5 at col 7
        // But then R1 = {1,3,4,5,6,7,8,9}, missing {2} → still only {2} for (1,3)

        // The problem: if row 1 has 8 givens and only (1,3) is empty, it's determined.
        // I need more empty cells in row 1.

        // R1: 9, 6, 7, _, _, 1, 3, 4, _  → missing {2,5,8}
        // (1,3) candidates = {2,5,8} ∩ col3_missing ∩ box1_missing

        // Box 1: (0,3)=9,(0,4)=0,(0,5)=6,(1,3)=0,(1,4)=0,(1,5)=1,(2,3)=4,(2,4)=3,(2,5)=7
        // Given: {1,3,4,6,7,9} → missing {2,5,8}
        // Col 3: 9,_,4,... (need values from solver for rows 3-8)
        // (1,3) candidates = {2,5,8} ∩ col3_missing ∩ {2,5,8} = col3_missing ∩ {2,5,8}

        // For UR we need (1,3) to have {2,9} + something. But row 1 missing is {2,5,8}, not {2,9}.
        // 9 is already in row 1 at col 0! So (1,3) can't have 9.

        // I need to reorganize. Let me put 9 somewhere else in row 1.
        // R0: 2, 3, 4, 9, 0, 6, 7, 8, 1
        // R1: _, 6, 7, _, 8, 1, 3, 4, 5  → missing {2,9}. (1,0) and (1,3) need {2,9}
        // But (1,0)=_ and (1,3)=_ both need {2,9}!

        // R2: 5, 8, 1, 4, 3, 7, 6, 9, 2
        // Box 0: (0,0)=2,(0,1)=3,(0,2)=4,(1,0)=_,(1,1)=6,(1,2)=7,(2,0)=5,(2,1)=8,(2,2)=1
        //   = {1,2,3,4,5,6,7,8} → missing {9}
        // Box 1: (0,3)=9,(0,4)=0,(0,5)=6,(1,3)=_,(1,4)=8,(1,5)=1,(2,3)=4,(2,4)=3,(2,5)=7
        //   = {1,3,4,6,7,8,9} → missing {2,5}

        // (1,0) candidates = row_missing{2,9} ∩ col0_missing ∩ box0_missing{9}
        //   = {2,9} ∩ col0_missing ∩ {9} = {9} if col0 allows 9
        //   → (1,0) = 9 (hidden single from box)

        // (1,3) candidates = row_missing{2,9} ∩ col3_missing ∩ box1_missing{2,5}
        //   = {2,9} ∩ col3_missing ∩ {2,5} = {2} if 9 not in col3_missing
        //   → (1,3) = 2 (hidden single)

        // Still hidden singles. The grid is too constrained with 77 clues.
        // I need MUCH fewer clues to create ambiguity.

        // New approach: start with a sparser puzzle
        // Only provide givens that DON'T constrain the UR corners too much

        // Actually, let me try removing ALL cells in rows 0-1 and cols 0,3 except the UR corners
        // plus remove cells in row 2 and cols 0,3

        val partial2 = intArrayOf(
            0, 3, 4, 0, 0, 6, 7, 8, 1,  // R0: remove cols 0,3,4
            0, 6, 7, 0, 8, 1, 3, 4, 5,  // R1: remove cols 0,3
            5, 8, 1, 4, 3, 7, 6, 9, 2,  // R2: complete
            0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0
        )

        val board2 = Board.invoke(partial2)
        SimpleCandidateEliminator().eliminate(board2)

        val c00 = board2.candidateValues(Coord(0, 0)).toList().sorted()
        val c03 = board2.candidateValues(Coord(0, 3)).toList().sorted()
        val c04 = board2.candidateValues(Coord(0, 4)).toList().sorted()
        val c10 = board2.candidateValues(Coord(1, 0)).toList().sorted()
        val c13 = board2.candidateValues(Coord(1, 3)).toList().sorted()

        // After simple elimination:
        // (0,0): row has {1,3,4,6,7,8} → missing {2,5,9}. Col 0 has {5}. Box 0 has {1,3,4,5,6,7,8} → missing {2,9}
        //   = {2,5,9} ∩ col0 ∩ {2,9} = {2,9} (if col 0 doesn't eliminate 2 or 9)
        // Col 0: row2=5, rows3-8 empty. So col 0 only has 5. Candidates: {1,2,3,4,6,7,8,9}
        // (0,0) = {2,5,9} ∩ {1,2,3,4,6,7,8,9} ∩ {2,9} = {2,9} ✓

        // (0,3): row has {1,3,4,6,7,8} → missing {2,5,9}. Col 3 has {4}. Box 1 has {1,3,4,6,7,8,9} → missing {2,5}
        //   = {2,5,9} ∩ col3_candidates ∩ {2,5} = {2,5} ∩ col3_candidates
        // Col 3: row2=4, rows3-8 empty. Col 3 only has 4. So candidates include {2,5}
        // (0,3) = {2,5}

        // (1,0): row has {1,3,4,5,6,7,8} → missing {2,9}. Col 0 same as above. Box 0 same.
        // (1,0) = {2,9} ✓

        // (1,3): row has {1,3,4,5,6,7,8} → missing {2,9}. Col 3 same as above. Box 1 has {1,3,4,6,7,8,9} → missing {2,5}
        // (1,3) = {2,9} ∩ col3 ∩ {2,5} = {2} (since 9 is not in box 1's missing!)
        // Wait: box 1 missing = {1..9} - box1_givens = {1..9} - {6,8,1,3,4,7,6,9,2} wait
        // Box 1 givens: (0,3)=0,(0,4)=0,(0,5)=6,(1,3)=0,(1,4)=8,(1,5)=1,(2,3)=4,(2,4)=3,(2,5)=7
        // = {1,3,4,6,7,8} → missing {2,5,9}
        // (1,3) = {2,9} ∩ col3 ∩ {2,5,9} = {2,9}

        // So: (0,0)={2,9}, (0,3)={2,5}, (1,0)={2,9}, (1,3)={2,9}
        // Common candidates: {2,9} ∩ {2,5} ∩ {2,9} ∩ {2,9} = {2} ← only 1!
        // Not enough for UR (need 2 common)

        // The problem: (0,3) has {2,5} instead of {2,9} because 9 is in box 1
        // via (2,3)=4? No... box 1 doesn't have 9 at (2,3). Let me recheck.
        // Box 1: rows 0-2, cols 3-5.
        // R0: (0,3)=0,(0,4)=0,(0,5)=6
        // R1: (1,3)=0,(1,4)=8,(1,5)=1
        // R2: (2,3)=4,(2,4)=3,(2,5)=7
        // Givens in box 1: {1,3,4,6,7,8} → missing {2,5,9}
        // So (0,3) should have candidates from {2,5,9} ∩ row0_missing ∩ col3_missing
        // Row 0 missing = {2,5,9} (has {1,3,4,6,7,8})
        // Col 3 has {4} → col3_missing = {1,2,3,5,6,7,8,9}
        // (0,3) = {2,5,9} ∩ {1,2,3,5,6,7,8,9} ∩ {2,5,9} = {2,5,9} ← has 3 candidates!

        // (1,3): row1 missing = {2,9} (has {1,3,4,5,6,7,8})
        // (1,3) = {2,9} ∩ col3_missing ∩ box1_missing = {2,9} ∩ {1,2,3,5,6,7,8,9} ∩ {2,5,9} = {2,9}
        // So (1,3) = {2,9} ← 2 candidates

        // (0,0) = {2,9}, (0,3) = {2,5,9}, (1,0) = {2,9}, (1,3) = {2,9}
        // Common = {2,9} ∩ {2,5,9} ∩ {2,9} ∩ {2,9} = {2,9}
        // 3 cells have exactly {2,9}: (0,0), (1,0), (1,3)
        // 1 cell has {2,5,9}: (0,3) ← this is the cell with extras!
        // UR Type 1: eliminate 2 and 9 from (0,3), leaving {5}

        // Verify box check:
        assertThat(boxIdx(0, 0)).isEqualTo(0) // box 0
        assertThat(boxIdx(0, 3)).isEqualTo(1) // box 1
        assertThat(boxIdx(1, 0)).isEqualTo(0) // box 0 (same as (0,0))
        assertThat(boxIdx(1, 3)).isEqualTo(1) // box 1 (same as (0,3))

        // Run UR eliminator
        val urElim = UniqueRectanglesCandidateEliminator()
        val changed = urElim.eliminate(board2)

        assertThat(changed)
            .`as`(
                "UR should fire. (0,0)=$c00, (0,3)=$c03, (1,0)=$c10, (1,3)=$c13"
            )
            .isTrue()

        // Verify that 2 and 9 were eliminated from (0,3)
        if (changed) {
            val c03After = board2.candidateValues(Coord(0, 3)).toList().sorted()
            assertThat(c03After).`as`("After UR, (0,3) should only have {5}").containsExactly(5)
        }
    }
}
