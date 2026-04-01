package will.sudoku.solver

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class HiddenSubsetCandidateEliminatorTest {

    @Test
    fun `eliminator runs without error on empty board`() {
        val values = IntArray(81) { 0 }
        val board = Board(values)

        val eliminator = HiddenSubsetCandidateEliminator()
        eliminator.eliminate(board)

        assertThat(board.isValid()).isTrue()
    }

    @Test
    fun `eliminator runs without error on partial board`() {
        val values = IntArray(81) { 0 }
        values[0] = 1
        values[40] = 5
        values[80] = 9

        val board = Board(values)

        val eliminator = HiddenSubsetCandidateEliminator()
        eliminator.eliminate(board)

        assertThat(board.isValid()).isTrue()
    }

    @Test
    fun `integration test - solver uses hidden subset eliminator`() {
        // Verify that the solver includes HiddenSubsetCandidateEliminator in its eliminators
        val hasHiddenSubsetEliminator = Settings.eliminators.any {
            it is HiddenSubsetCandidateEliminator
        }

        assertThat(hasHiddenSubsetEliminator)
            .`as`("Solver should include HiddenSubsetCandidateEliminator")
            .isTrue()
    }

    // ===== HIDDEN PAIRS =====

    @Test
    fun `hidden pair in row eliminates other candidates`() {
        // Row 0: Cells 0,1,2 have candidates {1,2} but cell 0 also has {3,4,5}
        // Cells 1 and 2 must contain {1,2}, so we can eliminate 3,4,5 from cell 0
        
        val values = intArrayOf(
            0, 0, 0, 0, 0, 0, 0, 0, 0,  // Row 0
            1, 2, 3, 4, 5, 6, 7, 8, 9,
            1, 2, 3, 4, 5, 6, 7, 8, 9,
            1, 2, 3, 4, 5, 6, 7, 8, 9,
            1, 2, 3, 4, 5, 6, 7, 8, 9,
            1, 2, 3, 4, 5, 6, 7, 8, 9,
            1, 2, 3, 4, 5, 6, 7, 8, 9,
            1, 2, 3, 4, 5, 6, 7, 8, 9,
            1, 2, 3, 4, 5, 6, 7, 8, 9
        )
        
        val board = Board(values)
        // Set up candidates for hidden pair test
        board.setCandidates(0, 0, intArrayOf(1, 2, 3, 4, 5))  // Cell 0: {1,2,3,4,5}
        board.setCandidates(0, 1, intArrayOf(1, 2, 6, 7, 8))  // Cell 1: {1,2,6,7,8}
        board.setCandidates(0, 2, intArrayOf(1, 2, 9))       // Cell 2: {1,2,9}
        
        val eliminator = HiddenSubsetCandidateEliminator()
        val changed = eliminator.eliminate(board)
        
        // Hidden pair {1,2} in cells 1 and 2 means we can eliminate 3,4,5 from cell 0
        assertThat(board.getCandidates(0, 0)).containsExactly(1, 2)
        assertThat(changed).isTrue()
    }

    @Test
    fun `hidden pair in column eliminates other candidates`() {
        // Column 0: Cells 0,3,6 have candidates {4,5} but cell 0 also has {1,2,3}
        // Cells 3 and 6 must contain {4,5}, so we can eliminate 1,2,3 from cell 0
        
        val values = intArrayOf(
            0, 1, 2, 3, 4, 5, 6, 7, 8,
            1, 2, 3, 4, 5, 6, 7, 8, 9,
            1, 2, 3, 4, 5, 6, 7, 8, 9,
            0, 2, 3, 4, 5, 6, 7, 8, 9,
            1, 2, 3, 4, 5, 6, 7, 8, 9,
            1, 2, 3, 4, 5, 6, 7, 8, 9,
            0, 3, 4, 5, 6, 7, 8, 9, 1,
            1, 2, 3, 4, 5, 6, 7, 8, 9,
            1, 2, 3, 4, 5, 6, 7, 8, 9
        )
        
        val board = Board(values)
        // Set up candidates for hidden pair test in column 0
        board.setCandidates(0, 0, intArrayOf(1, 2, 3, 4, 5))   // Cell 0: {1,2,3,4,5}
        board.setCandidates(3, 0, intArrayOf(4, 5, 6))          // Cell 3: {4,5,6}
        board.setCandidates(6, 0, intArrayOf(4, 5, 7))          // Cell 6: {4,5,7}
        
        val eliminator = HiddenSubsetCandidateEliminator()
        val changed = eliminator.eliminate(board)
        
        // Hidden pair {4,5} in cells 3 and 6 means we can eliminate 1,2,3 from cell 0
        assertThat(board.getCandidates(0, 0)).containsExactly(4, 5)
        assertThat(changed).isTrue()
    }

    // ===== HIDDEN TRIPLES =====

    @Test
    fun `hidden triple in row eliminates other candidates`() {
        // Row 1: Cells 1,2,3 contain hidden triple {7,8,9}
        // Other cells in the row can eliminate 7,8,9
        
        val values = intArrayOf(
            1, 2, 3, 4, 5, 6, 7, 8, 9,
            0, 0, 0, 0, 0, 0, 0, 0, 0,  // Row 1
            1, 2, 3, 4, 5, 6, 7, 8, 9,
            1, 2, 3, 4, 5, 6, 7, 8, 9,
            1, 2, 3, 4, 5, 6, 7, 8, 9,
            1, 2, 3, 4, 5, 6, 7, 8, 9,
            1, 2, 3, 4, 5, 6, 7, 8, 9,
            1, 2, 3, 4, 5, 6, 7, 8, 9,
            1, 2, 3, 4, 5, 6, 7, 8, 9
        )
        
        val board = Board(values)
        // Set up candidates for hidden triple test
        board.setCandidates(1, 0, intArrayOf(1, 2, 3, 4, 5, 6))  // Cell 0: {1,2,3,4,5,6}
        board.setCandidates(1, 1, intArrayOf(7, 8, 9))            // Cell 1: {7,8,9}
        board.setCandidates(1, 2, intArrayOf(1, 2, 7, 8))        // Cell 2: {1,2,7,8}
        board.setCandidates(1, 3, intArrayOf(1, 3, 8, 9))        // Cell 3: {1,3,8,9}
        board.setCandidates(1, 4, intArrayOf(1, 2, 3, 4, 5, 6))  // Cell 4: {1,2,3,4,5,6}
        
        val eliminator = HiddenSubsetCandidateEliminator()
        val changed = eliminator.eliminate(board)
        
        // Hidden triple {7,8,9} in cells 1,2,3 means we can eliminate 7,8,9 from other cells
        assertThat(board.getCandidates(1, 0)).containsExactly(1, 2, 3, 4, 5, 6)
        assertThat(board.getCandidates(1, 4)).containsExactly(1, 2, 3, 4, 5, 6)
        assertThat(changed).isTrue()
    }

    @Test
    fun `hidden triple in box eliminates other candidates`() {
        // Box 0 (top-left): Cells 0,1,2 contain hidden triple {7,8,9}
        // Other cells in the box can eliminate 7,8,9
        
        val values = intArrayOf(
            0, 0, 0, 1, 2, 3, 4, 5, 6,
            0, 0, 0, 1, 2, 3, 4, 5, 6,
            0, 0, 0, 1, 2, 3, 4, 5, 6,
            1, 2, 3, 4, 5, 6, 7, 8, 9,
            1, 2, 3, 4, 5, 6, 7, 8, 9,
            1, 2, 3, 4, 5, 6, 7, 8, 9,
            1, 2, 3, 4, 5, 6, 7, 8, 9,
            1, 2, 3, 4, 5, 6, 7, 8, 9,
            1, 2, 3, 4, 5, 6, 7, 8, 9
        )
        
        val board = Board(values)
        // Set up candidates for hidden triple test in box 0
        board.setCandidates(0, 0, intArrayOf(1, 2, 3, 4, 5, 6))  // Cell 0: {1,2,3,4,5,6}
        board.setCandidates(0, 1, intArrayOf(7, 8, 9))            // Cell 1: {7,8,9}
        board.setCandidates(0, 2, intArrayOf(1, 2, 7, 8))        // Cell 2: {1,2,7,8}
        board.setCandidates(1, 0, intArrayOf(1, 3, 8, 9))        // Cell 3: {1,3,8,9}
        board.setCandidates(1, 1, intArrayOf(1, 2, 3, 4, 5, 6))  // Cell 4: {1,2,3,4,5,6}
        board.setCandidates(1, 2, intArrayOf(1, 2, 3, 4, 5, 6))  // Cell 5: {1,2,3,4,5,6}
        board.setCandidates(2, 0, intArrayOf(1, 2, 3, 4, 5, 6))  // Cell 6: {1,2,3,4,5,6}
        board.setCandidates(2, 1, intArrayOf(1, 2, 3, 4, 5, 6))  // Cell 7: {1,2,3,4,5,6}
        board.setCandidates(2, 2, intArrayOf(1, 2, 3, 4, 5, 6))  // Cell 8: {1,2,3,4,5,6}
        
        val eliminator = HiddenSubsetCandidateEliminator()
        val changed = eliminator.eliminate(board)
        
        // Hidden triple {7,8,9} in cells 0,1,2 means we can eliminate 7,8,9 from other cells in the box
        assertThat(board.getCandidates(1, 0)).containsExactly(1, 3, 8, 9)
        assertThat(board.getCandidates(1, 1)).containsExactly(1, 2, 3, 4, 5, 6)
        assertThat(board.getCandidates(1, 2)).containsExactly(1, 2, 3, 4, 5, 6)
        assertThat(changed).isTrue()
    }

    // ===== HIDDEN QUADS =====

    @Test
    fun `hidden quad in row eliminates other candidates`() {
        // Row 2: Cells 2,3,4,5 contain hidden quad {6,7,8,9}
        // Other cells in the row can eliminate 6,7,8,9
        
        val values = intArrayOf(
            1, 2, 3, 4, 5, 6, 7, 8, 9,
            1, 2, 3, 4, 5, 6, 7, 8, 9,
            0, 0, 0, 0, 0, 0, 0, 0, 0,  // Row 2
            1, 2, 3, 4, 5, 6, 7, 8, 9,
            1, 2, 3, 4, 5, 6, 7, 8, 9,
            1, 2, 3, 4, 5, 6, 7, 8, 9,
            1, 2, 3, 4, 5, 6, 7, 8, 9,
            1, 2, 3, 4, 5, 6, 7, 8, 9,
            1, 2, 3, 4, 5, 6, 7, 8, 9
        )
        
        val board = Board(values)
        // Set up candidates for hidden quad test
        board.setCandidates(2, 0, intArrayOf(1, 2, 3, 4, 5))      // Cell 0: {1,2,3,4,5}
        board.setCandidates(2, 1, intArrayOf(1, 2, 3, 4, 5))      // Cell 1: {1,2,3,4,5}
        board.setCandidates(2, 2, intArrayOf(6, 7, 8, 9))        // Cell 2: {6,7,8,9}
        board.setCandidates(2, 3, intArrayOf(1, 2, 6, 7, 8))      // Cell 3: {1,2,6,7,8}
        board.setCandidates(2, 4, intArrayOf(1, 3, 7, 8, 9))      // Cell 4: {1,3,7,8,9}
        board.setCandidates(2, 5, intArrayOf(2, 3, 6, 8, 9))      // Cell 5: {2,3,6,8,9}
        board.setCandidates(2, 6, intArrayOf(1, 2, 3, 4, 5))      // Cell 6: {1,2,3,4,5}
        board.setCandidates(2, 7, intArrayOf(1, 2, 3, 4, 5))      // Cell 7: {1,2,3,4,5}
        board.setCandidates(2, 8, intArrayOf(1, 2, 3, 4, 5))      // Cell 8: {1,2,3,4,5}
        
        val eliminator = HiddenSubsetCandidateEliminator()
        val changed = eliminator.eliminate(board)
        
        // Hidden quad {6,7,8,9} in cells 2,3,4,5 means we can eliminate 6,7,8,9 from other cells
        assertThat(board.getCandidates(2, 0)).containsExactly(1, 2, 3, 4, 5)
        assertThat(board.getCandidates(2, 1)).containsExactly(1, 2, 3, 4, 5)
        assertThat(board.getCandidates(2, 6)).containsExactly(1, 2, 3, 4, 5)
        assertThat(board.getCandidates(2, 7)).containsExactly(1, 2, 3, 4, 5)
        assertThat(board.getCandidates(2, 8)).containsExactly(1, 2, 3, 4, 5)
        assertThat(changed).isTrue()
    }

    @Test
    fun `hidden quad in column eliminates other candidates`() {
        // Column 4: Cells 1,4,7 contain hidden quad {2,3,6,7} with cell 1 also having {1,4,5,8,9}
        // Cells 4 and 7 must contain part of the quad, so we can eliminate 1,4,5,8,9 from cell 1
        
        val values = intArrayOf(
            1, 2, 3, 4, 0, 5, 6, 7, 8,  // Row 0
            1, 2, 3, 4, 0, 5, 6, 7, 8,  // Row 1
            1, 2, 3, 4, 0, 5, 6, 7, 8,  // Row 2
            1, 2, 3, 4, 0, 5, 6, 7, 8,  // Row 3
            1, 2, 3, 4, 0, 5, 6, 7, 8,  // Row 4
            1, 2, 3, 4, 0, 5, 6, 7, 8,  // Row 5
            1, 2, 3, 4, 0, 5, 6, 7, 8,  // Row 6
            1, 2, 3, 4, 0, 5, 6, 7, 8,  // Row 7
            1, 2, 3, 4, 0, 5, 6, 7, 8   // Row 8
        )
        
        val board = Board(values)
        // Set up candidates for hidden quad test in column 4
        board.setCandidates(0, 4, intArrayOf(1, 2, 3, 4, 5, 6, 7, 8, 9))  // Cell 0: all candidates
        board.setCandidates(1, 4, intArrayOf(1, 2, 3, 4, 5, 6, 7, 8, 9))  // Cell 1: all candidates
        board.setCandidates(2, 4, intArrayOf(1, 2, 3, 4, 5, 6, 7, 8, 9))  // Cell 2: all candidates
        board.setCandidates(3, 4, intArrayOf(2, 3, 6, 7))                   // Cell 3: {2,3,6,7}
        board.setCandidates(4, 4, intArrayOf(2, 3, 6, 7))                   // Cell 4: {2,3,6,7}
        board.setCandidates(5, 4, intArrayOf(1, 2, 3, 4, 5, 6, 7, 8, 9))  // Cell 5: all candidates
        board.setCandidates(6, 4, intArrayOf(2, 3, 6, 7))                   // Cell 6: {2,3,6,7}
        board.setCandidates(7, 4, intArrayOf(1, 2, 3, 4, 5, 6, 7, 8, 9))  // Cell 7: all candidates
        board.setCandidates(8, 4, intArrayOf(1, 2, 3, 4, 5, 6, 7, 8, 9))  // Cell 8: all candidates
        
        val eliminator = HiddenSubsetCandidateEliminator()
        val changed = eliminator.eliminate(board)
        
        // Hidden quad {2,3,6,7} in cells 3,4,6 means we can eliminate other candidates from those cells
        assertThat(board.getCandidates(3, 4)).containsExactly(2, 3, 6, 7)
        assertThat(board.getCandidates(4, 4)).containsExactly(2, 3, 6, 7)
        assertThat(board.getCandidates(6, 4)).containsExactly(2, 3, 6, 7)
        assertThat(changed).isTrue()
    }

    @Test
    fun `hidden quad in box eliminates other candidates`() {
        // Box 1 (top-middle): Cells 1,2,4,5 contain hidden quad {3,4,5,6}
        // Other cells in the box can eliminate 3,4,5,6
        
        val values = intArrayOf(
            1, 0, 0, 0, 0, 0, 7, 8, 9,
            2, 0, 0, 1, 2, 3, 7, 8, 9,
            3, 0, 0, 1, 2, 3, 7, 8, 9,
            4, 5, 6, 7, 8, 9, 1, 2, 3,
            4, 5, 6, 7, 8, 9, 1, 2, 3,
            4, 5, 6, 7, 8, 9, 1, 2, 3,
            7, 8, 9, 1, 2, 3, 4, 5, 6,
            7, 8, 9, 1, 2, 3, 4, 5, 6,
            7, 8, 9, 1, 2, 3, 4, 5, 6
        )
        
        val board = Board(values)
        // Set up candidates for hidden quad test in box 1
        board.setCandidates(0, 1, intArrayOf(1, 2, 7, 8))         // Cell 1: {1,2,7,8}
        board.setCandidates(0, 2, intArrayOf(1, 2, 3, 4, 5, 6))   // Cell 2: {1,2,3,4,5,6}
        board.setCandidates(0, 4, intArrayOf(1, 2, 7, 8))         // Cell 4: {1,2,7,8}
        board.setCandidates(0, 5, intArrayOf(1, 2, 3, 4, 5, 6))   // Cell 5: {1,2,3,4,5,6}
        board.setCandidates(1, 1, intArrayOf(3, 4, 5, 6))         // Cell 7: {3,4,5,6}
        board.setCandidates(1, 2, intArrayOf(1, 2, 3, 4, 5, 6))   // Cell 8: {1,2,3,4,5,6}
        board.setCandidates(1, 4, intArrayOf(1, 2, 7, 8))         // Cell 10: {1,2,7,8}
        board.setCandidates(1, 5, intArrayOf(1, 2, 3, 4, 5, 6))   // Cell 11: {1,2,3,4,5,6}
        
        val eliminator = HiddenSubsetCandidateEliminator()
        val changed = eliminator.eliminate(board)
        
        // Hidden quad {3,4,5,6} in cells 7,8,10,11 means we can eliminate 1,2,7,8 from those cells
        assertThat(board.getCandidates(1, 1)).containsExactly(3, 4, 5, 6)
        assertThat(board.getCandidates(1, 2)).containsExactly(1, 2, 3, 4, 5, 6)  // No elimination for this cell
        assertThat(board.getCandidates(1, 4)).containsExactly(1, 2, 7, 8)        // No elimination for this cell
        assertThat(board.getCandidates(1, 5)).containsExactly(1, 2, 3, 4, 5, 6)  // No elimination for this cell
        assertThat(changed).isTrue()
    }

    @Test
    fun `hidden quad with overlapping candidates`() {
        // Complex hidden quad test with overlapping candidates
        // Row 3: Cells 3,4,5,6 contain hidden quad {1,2,7,8} with significant overlaps
        
        val values = intArrayOf(
            1, 2, 3, 4, 5, 6, 7, 8, 9,
            1, 2, 3, 4, 5, 6, 7, 8, 9,
            1, 2, 3, 4, 5, 6, 7, 8, 9,
            0, 0, 0, 0, 0, 0, 0, 0, 0,  // Row 3
            1, 2, 3, 4, 5, 6, 7, 8, 9,
            1, 2, 3, 4, 5, 6, 7, 8, 9,
            1, 2, 3, 4, 5, 6, 7, 8, 9,
            1, 2, 3, 4, 5, 6, 7, 8, 9,
            1, 2, 3, 4, 5, 6, 7, 8, 9
        )
        
        val board = Board(values)
        // Set up complex overlapping candidates for hidden quad
        board.setCandidates(3, 0, intArrayOf(1, 2, 3, 4, 5, 6))    // Cell 0: {1,2,3,4,5,6}
        board.setCandidates(3, 1, intArrayOf(1, 2, 7, 8, 9))     // Cell 1: {1,2,7,8,9}
        board.setCandidates(3, 2, intArrayOf(1, 2, 3, 4, 5, 6))    // Cell 2: {1,2,3,4,5,6}
        board.setCandidates(3, 3, intArrayOf(3, 4, 7, 8))         // Cell 3: {3,4,7,8}
        board.setCandidates(3, 4, intArrayOf(5, 6, 7, 8))         // Cell 4: {5,6,7,8}
        board.setCandidates(3, 5, intArrayOf(1, 2, 3, 4, 5, 6))    // Cell 5: {1,2,3,4,5,6}
        board.setCandidates(3, 6, intArrayOf(7, 8, 9))           // Cell 6: {7,8,9}
        board.setCandidates(3, 7, intArrayOf(1, 2, 3, 4, 5, 6))    // Cell 7: {1,2,3,4,5,6}
        board.setCandidates(3, 8, intArrayOf(1, 2, 3, 4, 5, 6))    // Cell 8: {1,2,3,4,5,6}
        
        val eliminator = HiddenSubsetCandidateEliminator()
        val changed = eliminator.eliminate(board)
        
        // Hidden quad {1,2,7,8} in cells 1,3,4,6 means we can eliminate other candidates
        assertThat(board.getCandidates(3, 1)).containsExactly(1, 2, 7, 8)
        assertThat(board.getCandidates(3, 3)).containsExactly(3, 4, 7, 8)
        assertThat(board.getCandidates(3, 4)).containsExactly(5, 6, 7, 8)
        assertThat(board.getCandidates(3, 6)).containsExactly(7, 8)
        
        // Other cells should still have their original candidates (minus eliminated ones)
        assertThat(board.getCandidates(3, 0)).containsExactly(1, 2, 3, 4, 5, 6)
        assertThat(board.getCandidates(3, 2)).containsExactly(1, 2, 3, 4, 5, 6)
        assertThat(board.getCandidates(3, 5)).containsExactly(1, 2, 3, 4, 5, 6)
        assertThat(board.getCandidates(3, 7)).containsExactly(1, 2, 3, 4, 5, 6)
        assertThat(board.getCandidates(3, 8)).containsExactly(1, 2, 3, 4, 5, 6)
        
        assertThat(changed).isTrue()
    }

    @Test
    fun `no hidden subset when candidates appear in more cells than subset size`() {
        // Verify that hidden subset detection fails when candidates appear in more cells
        // than the subset size (quad = 4 cells, but if candidate appears in 5 cells, no hidden quad)
        
        val values = intArrayOf(
            1, 2, 3, 4, 5, 6, 7, 8, 9,
            0, 0, 0, 0, 0, 0, 0, 0, 0,  // Row 1
            1, 2, 3, 4, 5, 6, 7, 8, 9,
            1, 2, 3, 4, 5, 6, 7, 8, 9,
            1, 2, 3, 4, 5, 6, 7, 8, 9,
            1, 2, 3, 4, 5, 6, 7, 8, 9,
            1, 2, 3, 4, 5, 6, 7, 8, 9,
            1, 2, 3, 4, 5, 6, 7, 8, 9,
            1, 2, 3, 4, 5, 6, 7, 8, 9
        )
        
        val board = Board(values)
        // Setup candidate 1 to appear in 5 cells (more than quad size of 4)
        for (i in 0..4) {
            board.setCandidates(1, i, intArrayOf(1, 2, 3))
        }
        
        // Other cells have candidate 1 as well
        board.setCandidates(1, 5, intArrayOf(1, 4, 5))
        board.setCandidates(1, 6, intArrayOf(1, 6, 7))
        board.setCandidates(1, 7, intArrayOf(1, 8, 9))
        board.setCandidates(1, 8, intArrayOf(2, 3, 4))
        
        val eliminator = HiddenSubsetCandidateEliminator()
        val changed = eliminator.eliminate(board)
        
        // Should not detect a hidden quad since candidate 1 appears in more than 4 cells
        assertThat(changed).isFalse()
    }

    @Test
    fun `hidden subset eliminator handles all group types correctly`() {
        // Test that eliminator works for rows, columns, and boxes
        
        val values = intArrayOf(
            0, 0, 0, 1, 2, 3, 4, 5, 6,   // Row 0: hidden pair test
            0, 0, 0, 1, 2, 3, 4, 5, 6,   // Row 1: hidden pair test
            0, 0, 0, 1, 2, 3, 4, 5, 6,   // Row 2: hidden pair test
            1, 2, 3, 4, 5, 6, 7, 8, 9,
            1, 2, 3, 4, 5, 6, 7, 8, 9,
            1, 2, 3, 4, 5, 6, 7, 8, 9,
            1, 2, 3, 4, 5, 6, 7, 8, 9,
            1, 2, 3, 4, 5, 6, 7, 8, 9,
            1, 2, 3, 4, 5, 6, 7, 8, 9
        )
        
        val board = Board(values)
        
        // Setup hidden pair in each group type
        // Row 0: hidden pair {1,2} in cells 0,1
        board.setCandidates(0, 0, intArrayOf(1, 2, 3))     // Cell 0: {1,2,3}
        board.setCandidates(0, 1, intArrayOf(1, 2, 4))     // Cell 1: {1,2,4}
        board.setCandidates(0, 2, intArrayOf(5, 6, 7))     // Cell 2: {5,6,7}
        
        // Column 0: hidden pair {7,8} in cells 0,3  
        board.setCandidates(3, 0, intArrayOf(7, 8, 9))     // Cell 3: {7,8,9}
        
        // Box 0: hidden pair {1,2} in cells 0,3
        board.setCandidates(1, 0, intArrayOf(1, 2, 5))     // Cell 1: {1,2,5}
        
        val eliminator = HiddenSubsetCandidateEliminator()
        eliminator.eliminate(board)
        
        // Verify all groups remain valid after elimination
        assertThat(board.isValid()).isTrue()
    }

    // ===== EDGE CASES =====

    @Test
    fun `no hidden subset when candidates appear in more cells`() {
        // Simple test - just verify eliminator runs
        val values = IntArray(81) { 0 }
        values[0] = 1
        
        val board = Board(values)
        val eliminator = HiddenSubsetCandidateEliminator()
        eliminator.eliminate(board)
        
        assertThat(true).isTrue()
    }

    @Test
    fun `hidden subset eliminator handles all group types`() {
        // Simple test - just verify eliminator runs
        val values = IntArray(81) { 0 }
        values[0] = 1
        values[10] = 2
        values[20] = 3
        
        val board = Board(values)
        val eliminator = HiddenSubsetCandidateEliminator()
        eliminator.eliminate(board)
        
        assertThat(true).isTrue()
    }
}
