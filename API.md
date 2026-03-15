# API Documentation

This document provides an overview of the Sudoku Solver API.

## Core Classes

### Board
Represents a Sudoku puzzle board with 81 cells using bitmask-based candidate representation.

```kotlin
// Create board from values
val board = Board(intArrayOf(5, 3, 0, ...))

// Check cell state
val isConfirmed = board.isConfirmed(Coord(0, 0))
val candidates = board.candidateValues(Coord(0, 0))

// Mark a value
board.markValue(Coord(0, 0), 5)

// Check board state
val isSolved = board.isSolved()
val isValid = board.isValid()
```

### Solver
Main solver class that uses constraint propagation and backtracking.

```kotlin
val solver = Solver()
val solution = solver.solve(board)
if (solution != null) {
    println("Solved!")
    println(solution)
}
```

### SolverWithMetrics
Extended solver that collects performance metrics.

```kotlin
val solver = SolverWithMetrics()
val result = solver.solveWithMetrics(board)

println(result.metrics)  // Print metrics
println(result.solvedBoard)  // Solved board or null
```

## Eliminators

### SimpleCandidateEliminator
Removes confirmed values from peer cells (same row, column, or region).

### GroupCandidateEliminator
Detects naked pairs, triples, and quads.

### ExclusionCandidateEliminator (Hidden Singles)
Finds values that can only go in one cell within a group.

### HiddenSubsetCandidateEliminator
Finds hidden pairs, triples, and quads.

### XWingCandidateEliminator
Detects X-Wing patterns for elimination across rows/columns.

### SwordfishCandidateEliminator
Extended X-Wing with 3 rows/columns.

### XYWingCandidateEliminator
Chain-based elimination using pivot and wings.

## Utility Classes

### DifficultyRater
Rates puzzle difficulty based on techniques required.

```kotlin
val rating = DifficultyRater.rate(metrics)
println(rating)  // "Medium (hidden singles)"
println(rating.level)  // MEDIUM
```

### HintGenerator
Generates hints for the next solving move.

```kotlin
val hint = HintGenerator.generate(board)
if (hint != null) {
    println("Look at (${hint.coord.row}, ${hint.coord.col})")
    println("Technique: ${hint.technique}")
    println("Explanation: ${hint.explanation}")
}
```

### PuzzleGenerator
Generates new Sudoku puzzles.

```kotlin
// Generate puzzle
val puzzle = PuzzleGenerator.generate(DifficultyRater.Level.MEDIUM)

// Generate with seed for reproducibility
val puzzle = PuzzleGenerator.generate(DifficultyRater.Level.EASY, seed = 42)
```

## Data Classes

### Coord
Represents a cell coordinate (row, column).

```kotlin
val coord = Coord(row = 4, col = 5)
val index = coord.index  // 4 * 9 + 5 = 41
```

### SolverMetrics
Collected metrics during solving.

```kotlin
data class SolverMetrics(
    val totalSolveTimeNanos: Long,
    val backtrackingCount: Int,
    val maxRecursionDepth: Int,
    val propagationPasses: Int,
    val cellsProcessed: Int,
    val eliminatorMetrics: Map<String, EliminatorMetrics>
)
```

### EliminatorMetrics
Metrics for a single eliminator.

```kotlin
data class EliminatorMetrics(
    val eliminations: Int,
    val passes: Int,
    val totalTimeNanos: Long
)
```

## Difficulty Levels

| Level | Description |
|-------|-------------|
| EASY | Simple elimination only |
| MEDIUM | Hidden singles required |
| HARD | Naked/hidden subsets required |
| EXPERT | X-Wing technique required |
| MASTER | Requires backtracking |
