package will.sudoku.solver

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import will.sudoku.solver.BoardReader.Companion.readBoard

/**
 * Validate ALS-XZ eliminator produces correct results on known puzzles.
 */
class ALSXZValidationTest {

    private fun loadPuzzle(name: String): Board {
        val dir = java.nio.file.Paths.get(
            this::class.java.getResource("/solver/www.sudokuweb.org")!!.toURI()
        )
        return readBoard(java.nio.file.Files.readString(dir.resolve("$name.question")))
    }

    @Test
    fun `ALS-XZ does not corrupt g3`() {
        val puzzle = loadPuzzle("g3")
        SimpleCandidateEliminator().eliminate(puzzle)

        val before = puzzle.copy()
        ALSXZCandidateEliminator().eliminate(puzzle)

        assertThat(puzzle.isValid()).isTrue()
    }

    @Test
    fun `ALS-XZ does not corrupt g4`() {
        val puzzle = loadPuzzle("g4")
        SimpleCandidateEliminator().eliminate(puzzle)

        ALSXZCandidateEliminator().eliminate(puzzle)

        assertThat(puzzle.isValid()).isTrue()
    }

    @Test
    fun `ALS-XZ solver still solves g3 with ALSXZ enabled`() {
        val puzzle = loadPuzzle("g3")
        val solver = Solver(SolverConfig(
            eliminators = SolverConfig.defaultEliminators()
        ))
        val result = solver.solve(puzzle)
        assertThat(result).isNotNull
        assertThat(result!!.isSolved()).isTrue()
    }

    @Test
    fun `ALS-XZ solver still solves g4 with ALSXZ enabled`() {
        val puzzle = loadPuzzle("g4")
        val solver = Solver(SolverConfig(
            eliminators = SolverConfig.defaultEliminators()
        ))
        val result = solver.solve(puzzle)
        assertThat(result).isNotNull
        assertThat(result!!.isSolved()).isTrue()
    }

    @Test
    fun `ALS-XZ does not produce empty candidate cells`() {
        val puzzle = loadPuzzle("g3")
        SimpleCandidateEliminator().eliminate(puzzle)

        ALSXZCandidateEliminator().eliminate(puzzle)

        for (coord in Coord.all) {
            if (!puzzle.isConfirmed(coord)) {
                assertThat(puzzle.candidateValues(coord).toList())
                    .`as`("Cell (${coord.row},${coord.col}) should have candidates")
                    .isNotEmpty
            }
        }
    }

    @Test
    fun `ALS-XZ works with other eliminators without corruption`() {
        val puzzle = loadPuzzle("g3")
        val eliminators = listOf(
            SimpleCandidateEliminator(),
            GroupCandidateEliminator(),
            HiddenSubsetCandidateEliminator(),
            ExclusionCandidateEliminator(9),
            XWingCandidateEliminator(),
            SwordfishCandidateEliminator(),
            XYWingCandidateEliminator(),
            XYZWingCandidateEliminator(),
            WWingCandidateEliminator(),
            SimpleColoringCandidateEliminator(),
            UniqueRectanglesCandidateEliminator(),
            ForcingChainsCandidateEliminator(),
            ALSXZCandidateEliminator(),
            FrankenFishCandidateEliminator()
        )

        // Run all eliminators iteratively
        var changed = true
        while (changed) {
            changed = false
            for (elim in eliminators) {
                if (elim.eliminate(puzzle)) changed = true
            }
        }

        assertThat(puzzle.isValid()).isTrue()

        // Solver should still be able to solve it
        val solver = Solver(SolverConfig(eliminators = eliminators))
        val result = solver.solve(puzzle)
        assertThat(result).isNotNull
        assertThat(result!!.isSolved()).isTrue()
    }
}
