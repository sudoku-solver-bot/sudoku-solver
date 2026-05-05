package will.sudoku.solver

import will.sudoku.solver.BoardSettings.regionSize
import will.sudoku.solver.BoardSettings.size
import will.sudoku.solver.BoardSettings.symbols
import java.lang.System.lineSeparator

/**
 * Represents a Sudoku puzzle board with 81 cells arranged in a 9×9 grid.
 *
 * The board uses a bitmask-based candidate representation to efficiently track possible values
 * for each cell. Each cell's candidate pattern is a 9-bit integer where bit `i` (0-indexed)
 * corresponds to value `i+1`. A confirmed cell has exactly one bit set. An empty/unknown
 * cell has all bits set.
 *
 * ## Bitmask Representation
 * The board uses bit manipulation to represent candidate patterns efficiently:
 * - `0b000000001` - only value 1 is possible (bit 0)
 * - `0b000000010` - only value 2 is possible (bit 1)
 * - `0b000000100` - only value 3 is possible (bit 2)
 * - And so on up to `0b100000000` - only value 9 is possible (bit 8)
 * - `0b111111111` - all values 1-9 are possible (empty cell, wildcard)
 *
 * ## Cell States
 * - **Confirmed cell**: Exactly one candidate value (single bit set)
 * - **Unresolved cell**: Multiple candidate values (multiple bits set)
 * - **Empty cell**: No values (all bits set, wildcard)
 *
 * ## Coordinate System
 * Cells are indexed 0-80 using a [row, col] coordinate system:
 * - `Coord(row, col)` - Represents a cell position
 * - Linear index: `index = row * 9 + col`
 * - Rows: 0-8, Columns: 0-8
 *
 * ## Thread Safety
 * This class is designed for use in a single-threaded backtracking solver.
 * The `copy()` method creates a new board instance for safe backtracking.
 *
 * ## Example
 * ```kotlin
 * // Create an empty board
 * val board = Board()
 *
 * // Get candidate values for a cell
 * val candidates = board.candidateValues(coord) // Returns IntArray of possible values
 *
 * // Check if a cell is confirmed
 * if (board.isConfirmed(coord)) { ... }
 *
 * // Mark a cell value
 * board.markValue(coord, 5)
 *
 * // Erase a candidate value
 * board.eraseCandidateValue(coord, 5)
 * ```
 *
 * @param candidatePatterns IntArray of 81 candidate patterns (one per cell).
 *                         Each pattern is a 9-bit integer representing possible values.
 * @throws IllegalArgumentException if candidatePatterns size is not 81.
 */
class Board private constructor(val candidatePatterns: IntArray) {
    /**
     * Initializes a new Sudoku board with the given candidate patterns.
     *
     * @param candidatePatterns IntArray of 81 candidate patterns, one per cell.
     *                             Each pattern is a 9-bit integer representing possible values.
     * @throws IllegalArgumentException if array size is not exactly 81.
     */
    init {
        require(candidatePatterns.size == size * size) {
            "Expected $size*$size board (81 cells), but got ${candidatePatterns.size} patterns"
        }
    }

    companion object {
        // candidate masks. it helps to extract the candidate values from candidate pattern
        val masks = (0 until size).map { 1 shl it }.toIntArray()

        // constructs a Board with known values
        @JvmStatic
        operator fun invoke(values: IntArray): Board {
            val candidatePatternForWildcard = (1 shl size) - 1
            val candidatePatterns = values.map {
                when (it) {
                    // if value is 0, it can be anything
                    0 -> candidatePatternForWildcard

                    // if value is 1, the candidate pattern in binary format should be 0b000000001
                    // if value is 4, the candidate pattern in binary format should be 0b000000100
                    else -> masks[it - 1]
                }
            }.toIntArray()
            return Board(candidatePatterns)
        }
    }

    //
    // methods for equals, hashCode and copy ---------------------
    //
    override fun equals(other: Any?): Boolean {
        return (other is Board) && candidatePatterns.contentEquals(other.candidatePatterns)
    }

    override fun hashCode(): Int {
        return candidatePatterns.contentHashCode()
    }

    fun copy(): Board {
        return Board(candidatePatterns.copyOf())
    }

    //
    // methods for status checking -------------------------------
    //
    fun symbolAt(coord: Coord): Char {
        return symbols[value(coord)]
    }

    fun isConfirmed(coord: Coord): Boolean {
        return candidatePattern(coord).countOneBits() == 1
    }

    fun isValid(): Boolean {
        return Coord.all.none { candidatePatterns[it.index] == 0 } &&
                CoordGroup.all.none { group ->
                    group.coords
                        .filter { isConfirmed(it) }
                        .groupingBy { candidatePattern(it) }
                        .eachCount()
                        .any { it.value > 1 }
                }
    }

    fun isSolved(): Boolean {
        return Coord.all.all { isConfirmed(it) }
    }

    //
    // methods for printing boards -------------------------------
    //
    override fun toString(): String {
        fun line(row: Int): String {
            return (0 until regionSize).map { region ->
                (0 until regionSize).map { regionCol ->
                    symbolAt(Coord(row, region * regionSize + regionCol))
                }.joinToString(separator = "")
            }.joinToString(separator = "!") + lineSeparator()
        }

        val regionSegment = CharArray(regionSize) { '-' }.joinToString(separator = "")

        val sepLine = (0 until regionSize)
            .joinToString(separator = "!") { regionSegment } + lineSeparator()

        return (0 until regionSize).joinToString(separator = sepLine) { region ->
            (0 until regionSize).joinToString(separator = "") { regionRow ->
                line(region * regionSize + regionRow)
            }
        }
    }

    //
    // methods for manipulating candidate patterns ---------------
    //

    // return the raw candidate pattern
    fun candidatePattern(coord: Coord): Int {
        return candidatePatterns[coord.index]
    }

    // return the candidate pattern as a list of value
    fun candidateValues(coord: Coord): IntArray {
        val candidate = candidatePattern(coord)
        return (0 until size).filter {
            candidate and masks[it] > 0
        }.map { it + 1 }.toIntArray()
    }

    // erase candidate pattern
    // if the pattern is updated, return true
    fun eraseCandidatePattern(coord: Coord, candidatePattern: Int): Boolean {
        val old = candidatePatterns[coord.index]
        candidatePatterns[coord.index] = candidatePatterns[coord.index] and (candidatePattern.inv())
        return (old != candidatePatterns[coord.index])
    }

    // erase candidate value
    // if the pattern is updated, return true
    fun eraseCandidateValue(coord: Coord, value: Int): Boolean {
        return eraseCandidatePattern(coord, masks[value - 1])
    }

    //
    // methods for manipulating confirmed values --------------
    //

    // get confirmed value
    fun value(coord: Coord): Int {
        return (1..size).firstOrNull { candidatePatterns[coord.index] == masks[it - 1] } ?: 0
    }

    // mark confirmed value
    fun markValue(coord: Coord, value: Int) {
        candidatePatterns[coord.index] = masks[value - 1]
    }

    //
    // methods for getting unsolved coord ---------------------
    // this method should be handled by another delegated class for move selection later
    fun unresolvedCoord(): Coord? {
        return Coord.all.filterNot { isConfirmed(it) }
            .minByOrNull { candidatePattern(it).countOneBits() }
    }

    /**
     * Count total candidates across all cells. Used for before/after comparison
     * when tracking eliminator effects.
     */
    fun countTotalCandidates(): Int {
        return Coord.all.sumOf { candidatePattern(it).countOneBits() }
    }

}
