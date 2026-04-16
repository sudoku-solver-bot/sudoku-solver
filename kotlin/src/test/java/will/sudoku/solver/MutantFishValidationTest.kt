package will.sudoku.solver

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import will.sudoku.solver.BoardReader.Companion.readBoard
import java.nio.file.Paths
import kotlin.io.path.readText

class MutantFishValidationTest {

    private fun loadPuzzle(name: String): Board {
        val dir = Paths.get(this::class.java.getResource("/solver/www.sudokuweb.org")!!.toURI())
        return readBoard(dir.resolve("$name.question").readText())
    }

    @Test
    fun `MutantFish does not corrupt g3`() {
        val puzzle = loadPuzzle("g3")
        SimpleCandidateEliminator().eliminate(puzzle)
        MutantFishCandidateEliminator().eliminate(puzzle)
        assertThat(puzzle.isValid()).isTrue()
    }

    @Test
    fun `MutantFish does not corrupt g4`() {
        val puzzle = loadPuzzle("g4")
        SimpleCandidateEliminator().eliminate(puzzle)
        MutantFishCandidateEliminator().eliminate(puzzle)
        assertThat(puzzle.isValid()).isTrue()
    }

    @Test
    fun `MutantFish does not produce empty candidates on g3`() {
        val puzzle = loadPuzzle("g3")
        SimpleCandidateEliminator().eliminate(puzzle)
        MutantFishCandidateEliminator().eliminate(puzzle)
        for (coord in Coord.all) {
            if (!puzzle.isConfirmed(coord)) {
                assertThat(puzzle.candidateValues(coord).toList())
                    .`as`("Cell (${coord.row},${coord.col}) should have candidates")
                    .isNotEmpty
            }
        }
    }

    @Test
    fun `MutantFish + Exclusion does not corrupt g3`() {
        val puzzle = loadPuzzle("g3")
        SimpleCandidateEliminator().eliminate(puzzle)
        ExclusionCandidateEliminator(9).eliminate(puzzle)
        MutantFishCandidateEliminator().eliminate(puzzle)
        assertThat(puzzle.isValid()).isTrue()
    }

    @Test
    fun `MutantFish + FrankenFish does not corrupt g3`() {
        val puzzle = loadPuzzle("g3")
        SimpleCandidateEliminator().eliminate(puzzle)
        ExclusionCandidateEliminator(9).eliminate(puzzle)
        FrankenFishCandidateEliminator().eliminate(puzzle)
        MutantFishCandidateEliminator().eliminate(puzzle)
        assertThat(puzzle.isValid()).isTrue()
    }

    @Test
    fun `solver solves g3 with MutantFish enabled`() {
        val puzzle = loadPuzzle("g3")
        val solver = Solver(SolverConfig(eliminators = SolverConfig.defaultEliminators()))
        val result = solver.solve(puzzle)
        assertThat(result).isNotNull
        assertThat(result!!.isSolved()).isTrue()
    }

    @Test
    fun `solver solves g4 with MutantFish enabled`() {
        val puzzle = loadPuzzle("g4")
        val solver = Solver(SolverConfig(eliminators = SolverConfig.defaultEliminators()))
        val result = solver.solve(puzzle)
        assertThat(result).isNotNull
        assertThat(result!!.isSolved()).isTrue()
    }

    @Test
    fun `all eliminators together solve g3`() {
        val puzzle = loadPuzzle("g3")
        val eliminators = SolverConfig.defaultEliminators()
        var changed = true
        while (changed) {
            changed = false
            for (elim in eliminators) {
                if (elim.eliminate(puzzle)) changed = true
            }
        }
        assertThat(puzzle.isValid()).isTrue()
        val solver = Solver(SolverConfig(eliminators = eliminators))
        val result = solver.solve(puzzle)
        assertThat(result).isNotNull
        assertThat(result!!.isSolved()).isTrue()
    }
}
