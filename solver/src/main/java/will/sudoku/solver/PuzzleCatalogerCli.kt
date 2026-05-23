package will.sudoku.solver

/**
 * CLI entry point for PuzzleCataloger.
 *
 * Usage:
 *   ./gradlew :kotlin:run --args="--count 100 --difficulty EXPERT --technique X_WING"
 *   ./gradlew :kotlin:run --args="--count 1000 --difficulty MASTER"
 *   ./gradlew :kotlin:run --args="--count 50 --technique SIMPLE_COLORING --require"
 */
fun main(args: Array<String>) {
    var count = 100
    var difficulty = DifficultyRater.Level.EXPERT
    var targetTechnique: String? = null
    var requireTechnique = false
    var startSeed = 1L

    // Parse args
    var i = 0
    while (i < args.size) {
        when (args[i]) {
            "--count" -> { count = args[++i].toIntOrNull() ?: count }
            "--difficulty" -> {
                difficulty = when (args[++i].uppercase()) {
                    "EASY" -> DifficultyRater.Level.EASY
                    "MEDIUM" -> DifficultyRater.Level.MEDIUM
                    "HARD" -> DifficultyRater.Level.HARD
                    "EXPERT" -> DifficultyRater.Level.EXPERT
                    "MASTER" -> DifficultyRater.Level.MASTER
                    else -> difficulty
                }
            }
            "--technique" -> { targetTechnique = args[++i] }
            "--require" -> { requireTechnique = true }
            "--seed" -> { startSeed = args[++i].toLongOrNull() ?: startSeed }
            "--help" -> {
                println("""
                    PuzzleCataloger — batch-generate and analyze sudoku puzzles
                    
                    Usage: PuzzleCataloger [options]
                    
                    Options:
                      --count N          Number of puzzles to generate (default: 100)
                      --difficulty LEVEL  EASY, MEDIUM, HARD, EXPERT, MASTER (default: EXPERT)
                      --technique NAME    Target technique to find (default: all)
                      --require           Only return puzzles that REQUIRE the technique
                      --seed N            Starting seed (default: 1)
                      --help              Show this help
                    
                    Technique names (use any):
                      X_WING, SWORDFISH, XY_WING, SIMPLE_COLORING, UNIQUE_RECTANGLES,
                      NAKED_PAIR, NAKED_TRIPLE, HIDDEN_SINGLE, HIDDEN_PAIR, HIDDEN_TRIPLE,
                      FORCING_CHAINS, DEATH_BLOSSOM, etc.
                    
                    Examples:
                      Find 1000 puzzles using X-Wing:
                        --count 1000 --technique X_WING --difficulty EXPERT
                    
                      Find 50 puzzles requiring Simple Coloring:
                        --count 50 --technique SIMPLE_COLORING --require --difficulty MASTER
                    
                      Catalog ALL techniques across 1000 MASTER puzzles:
                        --count 1000 --difficulty MASTER
                """.trimIndent())
                return
            }
        }
        i++
    }

    println("PuzzleCataloger")
    println("─".repeat(50))
    println("Count: $count, Difficulty: $difficulty")
    if (targetTechnique != null) {
        println("Target: $targetTechnique")
        println("Required: $requireTechnique")
    } else {
        println("Target: ALL techniques")
    }
    println()

    val startTime = System.currentTimeMillis()
    val result = PuzzleCataloger.catalog(
        count = count,
        difficulty = difficulty,
        targetTechnique = targetTechnique,
        requireTechnique = requireTechnique,
        startSeed = startSeed
    )
    val elapsed = System.currentTimeMillis() - startTime

    println()
    println("Results (${elapsed}ms)")
    println("─".repeat(50))
    println("Generated: ${result.totalGenerated}")
    println("Solved: ${result.totalSolved}")
    println("Unmatched: ${result.unmatched}")
    println()

    if (targetTechnique != null) {
        val matchingKey = result.byTechnique.keys.find { it.equals(targetTechnique, ignoreCase = true) }
        val entries = matchingKey?.let { result.byTechnique[it] } ?: emptyList()
        println("Found ${entries.size} puzzles using $targetTechnique")
        if (requireTechnique && entries.isEmpty()) {
            println("  (No puzzles REQUIRE this technique — solver can work around it)")
        }
        entries.take(10).forEachIndexed { idx, entry ->
            println("  ${idx + 1}. ${entry.puzzle} — ${entry.techniques.size} techniques, first use at step ${entry.firstUseStep}")
        }
        if (entries.size > 10) {
            println("  ... and ${entries.size - 10} more")
        }

        // Output best puzzle
        if (entries.isNotEmpty()) {
            val best = entries.minBy { if (it.firstUseStep > 0) it.firstUseStep else Int.MAX_VALUE }
            println()
            println("Best puzzle (earliest technique use at step ${best.firstUseStep}):")
            println("  Puzzle: ${best.puzzle}")
            println("  Techniques: ${best.techniques}")
        }
    } else {
        println("Technique distribution:")
        result.byTechnique.entries
            .sortedByDescending { it.value.size }
            .forEach { (technique, entries) ->
                println("  $technique: ${entries.size} puzzles")
            }
    }
}
