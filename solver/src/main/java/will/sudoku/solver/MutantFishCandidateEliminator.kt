package will.sudoku.solver

import will.sudoku.solver.Board.Companion.masks

/**
 * Mutant Fish Candidate Eliminator
 *
 * Detects Mutant Fish patterns — the most advanced fish pattern where both
 * the defining (base) sets and secondary (cover) sets can be a MIX of rows,
 * columns, and boxes.
 *
 * ## Fish Pattern Definition
 *
 * A fish of size N for candidate V has:
 * - N base sets (rows/columns/boxes) containing V positions
 * - N cover sets that also contain those same V positions
 * - Every V position in a cover set must also be in a base set
 * - Eliminate V from cover set positions NOT in any base set
 *
 * ## Why This Is Tricky
 *
 * Unlike basic fish (X-Wing, Swordfish) where bases are all rows and covers
 * are all columns, mutant fish allows e.g.:
 * - Base: row 0 + column 3 + box 5
 * - Cover: column 1 + row 7 + box 8
 *
 * ## Algorithm
 *
 * For each candidate value V and fish size N (2-4):
 * 1. Enumerate combinations of N base sets (from rows + columns + boxes)
 * 2. Find all V positions within those base sets
 * 3. Derive cover sets: which rows/cols/boxes contain these V positions
 * 4. Validate: exactly N cover sets, and every V in each cover set is in a base set
 * 5. Eliminate V from cover sets at positions outside all base sets
 *
 * ## Reference
 * https://www.sudopedia.org/wiki/Mutant_Fish
 */
class MutantFishCandidateEliminator : CandidateEliminator {

    // Represents a house: a row, column, or box
    sealed class House {
        abstract fun contains(coord: Coord): Boolean
        abstract val index: Int

        data class Row(override val index: Int) : House() {
            override fun contains(coord: Coord) = coord.row == index
        }
        data class Col(override val index: Int) : House() {
            override fun contains(coord: Coord) = coord.col == index
        }
        data class Box(override val index: Int) : House() {
            private val boxRow = index / 3
            private val boxCol = index % 3
            override fun contains(coord: Coord) =
                coord.row / 3 == boxRow && coord.col / 3 == boxCol
        }
    }

    // All 27 houses
    private val allHouses: List<House> = (0..8).flatMap { i ->
        listOf(House.Row(i), House.Col(i), House.Box(i))
    }

    override fun eliminate(board: Board): Boolean {
        var anyUpdate = false

        for (value in 1..9) {
            for (size in 2..4) {
                val changed = findMutantFish(board, value, size)
                if (changed) anyUpdate = true
            }
        }

        return anyUpdate
    }

    private fun findMutantFish(board: Board, value: Int, size: Int): Boolean {
        val mask = masks[value - 1]
        var anyUpdate = false

        // Find all positions with this candidate
        val allPositions = Coord.all.filter { coord ->
            !board.isConfirmed(coord) && (board.candidatePattern(coord) and mask) != 0
        }

        if (allPositions.size < size * 2) return false  // Need at least 2*size positions

        // Find houses that contain at least 2 positions with this candidate
        val candidateHouses = allHouses.filter { house ->
            allPositions.count { house.contains(it) } >= 2
        }

        if (candidateHouses.size < size) return false

        // Generate combinations of N houses to use as base sets
        val baseCombos = combinations(candidateHouses, size)

        for (baseHouses in baseCombos) {
            val result = tryFishPattern(board, value, mask, baseHouses, allPositions)
            if (result) anyUpdate = true
        }

        return anyUpdate
    }

    /**
     * Try a specific set of base houses as a fish pattern.
     */
    private fun tryFishPattern(
        board: Board,
        value: Int,
        mask: Int,
        baseHouses: List<House>,
        allPositions: List<Coord>
    ): Boolean {
        val baseSet = baseHouses.toSet()

        // Find V positions in base houses
        val fishPositions = allPositions.filter { coord ->
            baseSet.any { house -> house.contains(coord) }
        }

        if (fishPositions.isEmpty()) return false

        // Derive cover sets: all houses that contain at least one fish position
        val coverHouses = mutableSetOf<House>()
        for (house in allHouses) {
            if (fishPositions.any { house.contains(it) }) {
                coverHouses.add(house)
            }
        }

        // Remove base houses from cover (a house can be both base and cover,
        // but for counting purposes we need N additional cover sets)
        // Actually: in mutant fish, base and cover CAN overlap. The constraint
        // is that there are exactly N cover sets total (including overlaps).
        // The key constraint is: every V position in each cover house must also
        // be in a base house.

        if (coverHouses.size != baseHouses.size) return false

        // Validation: every V position in each cover house must also be in a base house.
        // If a cover house has V at a position NOT in any base house, then eliminating
        // from that cover house would be wrong.
        for (coverHouse in coverHouses) {
            val vPositionsInCover = allPositions.filter { coverHouse.contains(it) }
            val escaped = vPositionsInCover.any { coord ->
                baseSet.none { house -> house.contains(coord) }
            }
            if (escaped) return false
        }

        // Valid mutant fish! Eliminate V from cover house positions NOT in any base house.
        var anyUpdate = false
        for (coverHouse in coverHouses) {
            for (coord in Coord.all) {
                if (coverHouse.contains(coord) &&
                    !board.isConfirmed(coord) &&
                    (board.candidatePattern(coord) and mask) != 0 &&
                    baseSet.none { house -> house.contains(coord) }
                ) {
                    val erased = board.eraseCandidateValue(coord, value)
                    if (erased) anyUpdate = true
                }
            }
        }

        return anyUpdate
    }

    private fun <T> combinations(list: List<T>, k: Int): List<List<T>> {
        if (k == 0) return listOf(emptyList())
        if (k > list.size) return emptyList()
        if (k == list.size) return listOf(list)
        if (k == 1) return list.map { listOf(it) }

        val result = mutableListOf<List<T>>()
        fun combine(start: Int, current: List<T>) {
            if (current.size == k) {
                result.add(current)
                return
            }
            for (i in start until list.size) {
                combine(i + 1, current + list[i])
            }
        }
        combine(0, emptyList())
        return result
    }
}
