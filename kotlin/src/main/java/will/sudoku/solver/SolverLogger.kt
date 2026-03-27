package will.sudoku.solver

import mu.KotlinLogging
import org.slf4j.MDC
import kotlin.system.measureNanoTime

/**
 * Logging wrapper for solver operations with structured logging support.
 * Tracks solve times, backtracking counts, and progress metrics.
 */
class SolverLogger(private val loggerName: String = "Solver") {
    private val logger = KotlinLogging.logger(loggerName)
    
    /**
     * Log puzzle solve start with puzzle details
     */
    fun logSolveStart(puzzle: String, solverType: String) {
        logger.info {
            mapOf(
                "event" to "solve_start",
                "solver_type" to solverType,
                "puzzle_length" to puzzle.length,
                "puzzle_preview" to puzzle.take(20)
            ).let { data ->
                data.forEach { (key, value) -> MDC.put(key, value.toString()) }
                try { "Solving puzzle started" }
                finally { data.keys.forEach { MDC.remove(it) } }
            }
        }
    }
    
    /**
     * Log puzzle solve completion with timing
     */
    fun logSolveComplete(
        success: Boolean,
        solveTimeMs: Double,
        backtrackingCount: Int,
        stepsCount: Int?,
        solverType: String
    ) {
        logger.info {
            mapOf(
                "event" to "solve_complete",
                "success" to success,
                "solve_time_ms" to solveTimeMs,
                "backtracking_count" to backtrackingCount,
                "steps_count" to stepsCount,
                "solver_type" to solverType
            ).let { data ->
                data.forEach { (key, value) -> MDC.put(key, value?.toString() ?: "null") }
                try { "Solving puzzle completed" }
                finally { data.keys.forEach { MDC.remove(it) } }
            }
        }
    }
    
    /**
     * Log eliminator application
     */
    fun logEliminatorApplied(eliminatorName: String, cellsEliminated: Int) {
        logger.debug {
            mapOf(
                "event" to "eliminator_applied",
                "eliminator" to eliminatorName,
                "cells_eliminated" to cellsEliminated
            ).let { data ->
                data.forEach { (key, value) -> MDC.put(key, value.toString()) }
                try { "Eliminator applied" }
                finally { data.keys.forEach { MDC.remove(it) } }
            }
        }
    }
    
    /**
     * Log backtracking event
     */
    fun logBacktracking(depth: Int, cell: String, wrongValue: Int) {
        logger.debug {
            mapOf(
                "event" to "backtracking",
                "depth" to depth,
                "cell" to cell,
                "wrong_value" to wrongValue
            ).let { data ->
                data.forEach { (key, value) -> MDC.put(key, value.toString()) }
                try { "Backtracking" }
                finally { data.keys.forEach { MDC.remove(it) } }
            }
        }
    }
    
    /**
     * Log guess made during solving
     */
    fun logGuessMade(cell: String, value: Int, candidateCount: Int) {
        logger.debug {
            mapOf(
                "event" to "guess_made",
                "cell" to cell,
                "value" to value,
                "candidate_count" to candidateCount
            ).let { data ->
                data.forEach { (key, value) -> MDC.put(key, value.toString()) }
                try { "Guess made" }
                finally { data.keys.forEach { MDC.remove(it) } }
            }
        }
    }
    
    /**
     * Log propagation pass
     */
    fun logPropagationPass(cellsFilled: Int, candidatesEliminated: Int) {
        logger.debug {
            mapOf(
                "event" to "propagation_pass",
                "cells_filled" to cellsFilled,
                "candidates_eliminated" to candidatesEliminated
            ).let { data ->
                data.forEach { (key, value) -> MDC.put(key, value.toString()) }
                try { "Propagation pass completed" }
                finally { data.keys.forEach { MDC.remove(it) } }
            }
        }
    }
}

/**
 * Measure execution time and log it
 */
inline fun <T> measureAndLog(
    logger: SolverLogger,
    operation: String,
    block: () -> T
): T {
    val startTime = System.nanoTime()
    try {
        return block()
    } finally {
        val elapsedMs = (System.nanoTime() - startTime) / 1_000_000.0
        KotlinLogging.logger(logger.javaClass.name).debug {
            "Operation '$operation' completed in ${elapsedMs}ms"
        }
    }
}
