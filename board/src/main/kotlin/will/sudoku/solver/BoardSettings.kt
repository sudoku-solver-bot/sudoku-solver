package will.sudoku.solver

import kotlin.math.sqrt

/**
 * Board dimensions and display settings.
 *
 * These are fixed for standard 9x9 Sudoku but are kept configurable
 * for potential future variants.
 */
object BoardSettings {
    const val size: Int = 9
    val regionSize: Int = sqrt(size.toDouble()).toInt()
    val symbols: CharArray = charArrayOf('.', '1', '2', '3', '4', '5', '6', '7', '8', '9')

    init {
        require(regionSize * regionSize == size) { "given size [$size] cannot be properly sqrt into another integer" }
    }
}
