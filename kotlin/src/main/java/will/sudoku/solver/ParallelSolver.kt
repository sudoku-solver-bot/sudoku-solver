package will.sudoku.solver

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlin.system.measureTimeMillis

/**
 * Parallel Sudoku Solver
 *
 * Solves multiple puzzles in parallel using Kotlin coroutines.
 * 
 * ## Usage
 * ```kotlin
 * val puzzles = listOf(puzzle1, puzzle2, puzzle3)
 * val solutions = ParallelSolver.solveBatch(puzzles)
 * 
 * // With progress callback
 * val solutions = ParallelSolver.solveBatch(puzzles) { completed, total ->
 *     println("Progress: $completed/$total")
 * }
 * ```
 */
object ParallelSolver {
    
    private val solver = Solver()
    
    /**
     * Solve multiple puzzles in parallel.
     * 
     * @param puzzles List of puzzle boards to solve
     * @param parallelism Number of parallel solvers (default: CPU cores)
     * @return List of solutions (null for unsolvable puzzles)
     */
    suspend fun solveBatch(
        puzzles: List<Board>,
        parallelism: Int = Runtime.getRuntime().availableProcessors()
    ): List<Board?> = coroutineScope {
        puzzles.mapIndexed { index, puzzle ->
            async(Dispatchers.Default) {
                solver.solve(puzzle)
            }
        }.awaitAll()
    }
    
    /**
     * Solve multiple puzzles in parallel with progress tracking.
     * 
     * @param puzzles List of puzzle boards to solve
     * @param parallelism Number of parallel solvers
     * @param onProgress Callback for progress updates (completed, total)
     * @return List of solutions with timing information
     */
    suspend fun solveBatchWithProgress(
        puzzles: List<Board>,
        parallelism: Int = Runtime.getRuntime().availableProcessors(),
        onProgress: ((completed: Int, total: Int) -> Unit)? = null
    ): ParallelBatchResult = coroutineScope {
        val results = mutableListOf<SolveResult>()
        var completed = 0
        
        val totalTimeMs = measureTimeMillis {
            val deferredResults = puzzles.mapIndexed { index, puzzle ->
                async(Dispatchers.Default) {
                    val solveTimeMs = measureTimeMillis {
                        solver.solve(puzzle)
                    }.let { solution ->
                        completed++
                        onProgress?.invoke(completed, puzzles.size)
                        SolveResult(
                            index = index,
                            solution = solution,
                            solveTimeMs = solveTimeMs,
                            success = solution != null
                        )
                    }
                }
            }
            
            results.addAll(deferredResults.awaitAll())
        }
        
        ParallelBatchResult(
            results = results,
            totalPuzzles = puzzles.size,
            solvedPuzzles = results.count { it.success },
            failedPuzzles = results.count { !it.success },
            totalTimeMs = totalTimeMs,
            averageTimeMs = if (results.isNotEmpty()) totalTimeMs / results.size else 0
        )
    }
    
    /**
     * Validate multiple puzzles in parallel.
     * 
     * @param puzzles List of puzzle strings to validate
     * @return List of validation results
     */
    suspend fun validateBatch(
        puzzles: List<String>
    ): List<ValidationResult> = coroutineScope {
        val validator = PuzzleValidator
        
        puzzles.mapIndexed { index, puzzle ->
            async(Dispatchers.Default) {
                val error = validator.validate(puzzle)
                ValidationResult(
                    index = index,
                    valid = error == null,
                    error = error
                )
            }
        }.awaitAll()
    }
    
    /**
     * Generate and solve puzzles in parallel (for benchmarking).
     * 
     * @param count Number of puzzles to generate and solve
     * @param difficulty Target difficulty
     * @return Benchmark results
     */
    suspend fun benchmarkSolve(
        count: Int,
        difficulty: DifficultyRater.Level = DifficultyRater.Level.MEDIUM
    ): BenchmarkResult = coroutineScope {
        // Generate puzzles
        val puzzles = PuzzleGenerator.generateBatch(count, difficulty)
        
        // Solve in parallel
        val startTime = System.currentTimeMillis()
        val results = solveBatch(puzzles)
        val endTime = System.currentTimeMillis()
        
        BenchmarkResult(
            totalPuzzles = count,
            solvedCount = results.count { it != null },
            failedCount = results.count { it == null },
            totalTimeMs = endTime - startTime,
            averageTimeMs = (endTime - startTime) / count
        )
    }
}

/**
 * Result of solving a single puzzle.
 */
data class SolveResult(
    val index: Int,
    val solution: Board?,
    val solveTimeMs: Long,
    val success: Boolean
)

/**
 * Result of a batch solve operation.
 */
data class ParallelBatchResult(
    val results: List<SolveResult>,
    val totalPuzzles: Int,
    val solvedPuzzles: Int,
    val failedPuzzles: Int,
    val totalTimeMs: Long,
    val averageTimeMs: Long
)

/**
 * Result of validating a puzzle.
 */
data class ValidationResult(
    val index: Int,
    val valid: Boolean,
    val error: String?
)

/**
 * Result of a benchmark run.
 */
data class BenchmarkResult(
    val totalPuzzles: Int,
    val solvedCount: Int,
    val failedCount: Int,
    val totalTimeMs: Long,
    val averageTimeMs: Long
)
