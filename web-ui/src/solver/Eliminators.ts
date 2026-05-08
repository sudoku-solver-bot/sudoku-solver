import type { Board } from './Board'
import { Coord } from './Coord'
import { CoordGroup } from './CoordGroup'
import { bitCount, MASKS, SIZE, WILDCARD_PATTERN } from './Bitmask'

// ---------------------------------------------------------------------------
// CandidateEliminator interface
// ---------------------------------------------------------------------------

/**
 * A candidate elimination technique.
 *
 * Each eliminator modifies the board in-place, removing or confirming
 * candidates according to its logical rule. Returns true if any change
 * was made.
 */
export interface CandidateEliminator {
    /** Human-readable technique name (e.g., "Simple Elimination"). */
    readonly displayName: string

    /** Apply the elimination rule to the board. Returns true if changed. */
    eliminate(board: Board): boolean
}

// ---------------------------------------------------------------------------
// SimpleCandidateEliminator
// ---------------------------------------------------------------------------

/**
 * Removes candidates based on placed (confirmed) values in each group.
 *
 * For each confirmed cell in a row/column/box, erase that value from all
 * other cells in the same group. Repeats until the board stabilises.
 *
 * Equivalent to the Kotlin `SimpleCandidateEliminator`.
 */
export class SimpleCandidateEliminator implements CandidateEliminator {
    readonly displayName = 'Simple Elimination'

    eliminate(board: Board): boolean {
        let anyUpdate = false
        let stable: boolean
        do {
            stable = true
            for (const group of CoordGroup.all) {
                for (const coord of group.coords) {
                    if (board.isConfirmed(coord)) {
                        const pattern = board.candidatePattern(coord)
                        for (const peer of group.coords) {
                            if (coord !== peer) {
                                const updated = board.eraseCandidatePattern(peer, pattern)
                                if (updated) {
                                    anyUpdate = true
                                    stable = false
                                }
                            }
                        }
                    }
                }
            }
        } while (!stable)
        return anyUpdate
    }
}

// ---------------------------------------------------------------------------
// GroupCandidateEliminator (Naked Subsets)
// ---------------------------------------------------------------------------

/**
 * Finds naked subsets in each group.
 *
 * If N cells in a group share the same candidate pattern containing
 * exactly N candidates, those candidates cannot appear in any other
 * cell in that group. This covers naked singles, pairs, triples, etc.
 * Repeats until the board stabilises.
 *
 * Equivalent to the Kotlin `GroupCandidateEliminator`.
 */
export class GroupCandidateEliminator implements CandidateEliminator {
    readonly displayName = 'Naked Subset'

    eliminate(board: Board): boolean {
        let anyUpdate = false
        let stable: boolean
        do {
            stable = true
            for (const group of CoordGroup.all) {
                // Count occurrences of each candidate pattern in the group
                const countByPattern = new Map<number, number>()
                for (const coord of group.coords) {
                    const pat = board.candidatePattern(coord)
                    countByPattern.set(pat, (countByPattern.get(pat) ?? 0) + 1)
                }

                // Find patterns where bitCount == occurrence count (naked subset)
                for (const [pattern, count] of countByPattern) {
                    const bc = bitCount(pattern)
                    if (bc === count && bc !== 9) {
                        // Erase this pattern from all OTHER cells in the group
                        let updated = false
                        for (const coord of group.coords) {
                            if (board.candidatePattern(coord) !== pattern) {
                                if (board.eraseCandidatePattern(coord, pattern)) {
                                    updated = true
                                }
                            }
                        }
                        if (updated) {
                            anyUpdate = true
                            stable = false
                        }
                    }
                }
            }
        } while (!stable)
        return anyUpdate
    }
}

// ---------------------------------------------------------------------------
// ExclusionCandidateEliminator (Hidden Singles)
// ---------------------------------------------------------------------------

/**
 * Finds hidden singles in each group.
 *
 * If a candidate value appears in only one cell within a group, that cell
 * must hold that value. The cell is marked as confirmed.
 *
 * The `shortCircuitThreshold` skips groups that already have enough known
 * values (optimisation).
 *
 * Equivalent to the Kotlin `ExclusionCandidateEliminator`.
 */
export class ExclusionCandidateEliminator implements CandidateEliminator {
    readonly displayName = 'Hidden Single'

    private readonly shortCircuitThreshold: number

    constructor(shortCircuitThreshold = 8) {
        this.shortCircuitThreshold = shortCircuitThreshold
    }

    eliminate(board: Board): boolean {
        let anyUpdate = false
        let stable: boolean
        do {
            stable = true
            for (const group of CoordGroup.all) {
                // Collect known (confirmed) values in this group
                const knownValues = new Set<number>()
                for (const coord of group.coords) {
                    const v = board.value(coord)
                    if (v !== 0) knownValues.add(v)
                }

                // Short-circuit: if enough values are already known, skip
                if (knownValues.size >= this.shortCircuitThreshold) continue

                // Count occurrences of each candidate value across all cells
                const valueCount = new Map<number, number>()
                for (const coord of group.coords) {
                    const candidates = board.candidateValues(coord)
                    for (const v of candidates) {
                        valueCount.set(v, (valueCount.get(v) ?? 0) + 1)
                    }
                }

                // Find values that appear exactly once and aren't already placed
                for (const [value, count] of valueCount) {
                    if (count === 1 && !knownValues.has(value)) {
                        // Mark the cell containing this candidate as confirmed
                        for (const coord of group.coords) {
                            if (board.candidatePattern(coord) & MASKS[value - 1]) {
                                board.markValue(coord, value)
                                anyUpdate = true
                                stable = false
                                break
                            }
                        }
                    }
                }
            }
        } while (!stable)
        return anyUpdate
    }
}

// ---------------------------------------------------------------------------
// HiddenSubsetCandidateEliminator (Hidden Pairs, Triples, Quads)
// ---------------------------------------------------------------------------

/**
 * Finds hidden subsets within groups.
 *
 * A hidden subset occurs when N candidates appear in exactly N cells within a
 * group, even if those cells also contain other candidates. In this case, all
 * other candidates can be removed from those N cells.
 *
 * Equivalent to the Kotlin `HiddenSubsetCandidateEliminator`.
 */
export class HiddenSubsetCandidateEliminator implements CandidateEliminator {
    readonly displayName = 'Hidden Subset'

    eliminate(board: Board): boolean {
        let anyUpdate = false
        let stable: boolean
        do {
            stable = true
            for (const group of CoordGroup.all) {
                if (this._eliminateFromGroup(board, group)) {
                    anyUpdate = true
                    stable = false
                }
            }
        } while (!stable)
        return anyUpdate
    }

    private _eliminateFromGroup(board: Board, group: CoordGroup): boolean {
        const candidateToCells = new Map<number, Coord[]>()
        for (const coord of group.coords) {
            const candidates = board.candidateValues(coord)
            for (const c of candidates) {
                const list = candidateToCells.get(c)
                if (list) list.push(coord)
                else candidateToCells.set(c, [coord])
            }
        }

        const limited = new Map<number, Coord[]>()
        for (const [cand, cells] of candidateToCells) {
            if (cells.length >= 2 && cells.length <= 4) {
                limited.set(cand, cells)
            }
        }
        if (limited.size < 2) return false

        let anyUpdate = false
        for (const size of [2, 3, 4]) {
            if (this._checkSubset(board, limited, size)) {
                anyUpdate = true
            }
        }
        return anyUpdate
    }

    private _checkSubset(
        board: Board,
        candidateToCells: Map<number, Coord[]>,
        subsetSize: number,
    ): boolean {
        const eligible: number[] = []
        for (const [cand, cells] of candidateToCells) {
            if (cells.length <= subsetSize) {
                eligible.push(cand)
            }
        }
        if (eligible.length < subsetSize) return false

        let anyUpdate = false
        const combos = combinations(eligible, subsetSize)

        for (const combo of combos) {
            const cellsWithCandidates = new Set<Coord>()
            for (const cand of combo) {
                const cells = candidateToCells.get(cand)
                if (cells) {
                    for (const c of cells) cellsWithCandidates.add(c)
                }
            }

            if (cellsWithCandidates.size === subsetSize) {
                const comboMasks = new Set(combo.map(v => MASKS[v - 1]))

                for (const cell of cellsWithCandidates) {
                    const pat = board.candidatePattern(cell)
                    for (let i = 0; i < SIZE; i++) {
                        if ((pat & MASKS[i]) !== 0 && !comboMasks.has(MASKS[i])) {
                            if (board.eraseCandidateValue(cell, i + 1)) {
                                anyUpdate = true
                            }
                        }
                    }
                }
            }
        }
        return anyUpdate
    }
}

function combinations<T>(arr: T[], size: number): T[][] {
    if (size === 0) return [[]]
    if (size > arr.length) return []
    if (size === 1) return arr.map(item => [item])

    const result: T[][] = []
    for (let i = 0; i <= arr.length - size; i++) {
        const first = arr[i]
        const rest = arr.slice(i + 1)
        for (const combo of combinations(rest, size - 1)) {
            result.push([first, ...combo])
        }
    }
    return result
}

// ---------------------------------------------------------------------------
// PointingCandidateEliminator (Intersection: Region → Row/Col)
// ---------------------------------------------------------------------------

/**
 * Pointing: If in a region all candidates of a value are confined to one
 * row or column, remove that candidate from the rest of that row/column
 * outside the region.
 *
 * Example: In region 0, all '3' candidates are in row 0. This means cells
 * in row 0 that are NOT in region 0 cannot be '3' — remove '3' from them.
 */
export class PointingCandidateEliminator implements CandidateEliminator {
    readonly displayName = 'Pointing'

    eliminate(board: Board): boolean {
        let anyUpdate = false
        let stable: boolean
        do {
            stable = true
            for (const region of CoordGroup.region) {
                if (this._eliminateFromRegion(board, region)) {
                    anyUpdate = true
                    stable = false
                }
            }
        } while (!stable)
        return anyUpdate
    }

    private _eliminateFromRegion(board: Board, region: CoordGroup): boolean {
        let anyUpdate = false
        for (let value = 1; value <= SIZE; value++) {
            const mask = MASKS[value - 1]
            const cellsWithCandidate: Coord[] = []

            for (const coord of region.coords) {
                if (board.candidatePattern(coord) & mask) {
                    cellsWithCandidate.push(coord)
                }
            }

            // Need 2+ cells to form a pointing pattern
            if (cellsWithCandidate.length < 2) continue

            // Check if all cells share the same row
            const firstCoord = cellsWithCandidate[0]
            const sameRow = cellsWithCandidate.every(c => c.row === firstCoord.row)
            if (sameRow) {
                // Remove candidate from other cells in this row outside region
                const otherRowCells = CoordGroup.horizontal[firstCoord.row].coords
                    .filter(c => !region.coords.includes(c))
                for (const cell of otherRowCells) {
                    if (board.eraseCandidateValue(cell, value)) {
                        anyUpdate = true
                    }
                }
            }

            // Check if all cells share the same column
            const sameCol = cellsWithCandidate.every(c => c.col === firstCoord.col)
            if (sameCol) {
                // Remove candidate from other cells in this column outside region
                const otherColCells = CoordGroup.vertical[firstCoord.col].coords
                    .filter(c => !region.coords.includes(c))
                for (const cell of otherColCells) {
                    if (board.eraseCandidateValue(cell, value)) {
                        anyUpdate = true
                    }
                }
            }
        }
        return anyUpdate
    }
}

// ---------------------------------------------------------------------------
// ClaimingCandidateEliminator (Intersection: Row/Col → Region)
// ---------------------------------------------------------------------------

/**
 * Claiming: If in a row/column all candidates of a value are confined to one
 * region, remove that candidate from the rest of that region.
 *
 * Example: In row 0, all '5' candidates are in region 0. This means cells
 * in region 0 that are NOT in row 0 cannot be '5' — remove '5' from them.
 */
export class ClaimingCandidateEliminator implements CandidateEliminator {
    readonly displayName = 'Claiming'

    eliminate(board: Board): boolean {
        let anyUpdate = false
        let stable: boolean
        do {
            stable = true
            for (let i = 0; i < SIZE; i++) {
                if (this._eliminateFromLine(board, CoordGroup.horizontal[i])) {
                    anyUpdate = true
                    stable = false
                }
                if (this._eliminateFromLine(board, CoordGroup.vertical[i])) {
                    anyUpdate = true
                    stable = false
                }
            }
        } while (!stable)
        return anyUpdate
    }

    private _eliminateFromLine(board: Board, line: CoordGroup): boolean {
        let anyUpdate = false
        for (let value = 1; value <= SIZE; value++) {
            const mask = MASKS[value - 1]
            const cellsWithCandidate: Coord[] = []

            for (const coord of line.coords) {
                if (board.candidatePattern(coord) & mask) {
                    cellsWithCandidate.push(coord)
                }
            }

            if (cellsWithCandidate.length < 2) continue

            // Check if all cells with this candidate are in the same region
            const firstCell = cellsWithCandidate[0]
            const regionIndex = firstCell.region
            const allSameRegion = cellsWithCandidate.every(c => c.region === regionIndex)

            if (allSameRegion) {
                const region = CoordGroup.region[regionIndex]
                // Remove candidate from cells in this region NOT in this line
                const otherCells = region.coords.filter(c => !line.coords.includes(c))
                for (const cell of otherCells) {
                    if (board.eraseCandidateValue(cell, value)) {
                        anyUpdate = true
                    }
                }
            }
        }
        return anyUpdate
    }
}

// ---------------------------------------------------------------------------
// FishCandidateEliminator (X-Wing, Swordfish, Jellyfish)
// ---------------------------------------------------------------------------

/**
 * Generalized Fish pattern eliminator covering X-Wing (N=2), Swordfish
 * (N=3), and Jellyfish (N=4).
 *
 * For each candidate value, if N rows have that candidate confined to the
 * same N columns, eliminate the candidate from those N columns outside the
 * N rows (and vice versa for columns → rows).
 */
export class FishCandidateEliminator implements CandidateEliminator {
    readonly displayName = 'Fish'
    private readonly sizes: readonly number[]

    /**
     * @param sizes Fish sizes to search for (default: [2, 3, 4] for
     *   X-Wing, Swordfish, Jellyfish). Use [2] for X-Wing only, etc.
     */
    constructor(sizes: readonly number[] = [2, 3, 4]) {
        this.sizes = sizes
    }

    eliminate(board: Board): boolean {
        let anyUpdate = false
        let stable: boolean
        do {
            stable = true
            if (this._eliminateRows(board)) { anyUpdate = true; stable = false }
            if (this._eliminateCols(board)) { anyUpdate = true; stable = false }
        } while (!stable)
        return anyUpdate
    }

    private _eliminateRows(board: Board): boolean {
        let anyUpdate = false
        for (let value = 1; value <= SIZE; value++) {
            const mask = MASKS[value - 1]
            // For each row, list columns containing this candidate
            const rowToCols = new Map<number, number[]>()
            for (let r = 0; r < SIZE; r++) {
                const cols: number[] = []
                for (let c = 0; c < SIZE; c++) {
                    const coord = Coord.all[r * 9 + c]
                    if (board.candidatePattern(coord) & mask) {
                        cols.push(c)
                    }
                }
                if (cols.length >= 2) rowToCols.set(r, cols)
            }
            if (rowToCols.size < 2) continue

            const rows = [...rowToCols.keys()]
            for (const size of this.sizes) {
                if (size > rows.length) continue
                const rowCombos = combinations(rows, size)
                for (const fishRows of rowCombos) {
                    const unionCols = new Set<number>()
                    for (const r of fishRows) {
                        for (const c of rowToCols.get(r)!) unionCols.add(c)
                    }
                    if (unionCols.size !== size) continue
                    // Fish found: eliminate from unionCols outside fishRows
                    for (const c of unionCols) {
                        for (let r = 0; r < SIZE; r++) {
                            if (fishRows.includes(r)) continue
                            const coord = Coord.all[r * 9 + c]
                            if (board.eraseCandidateValue(coord, value)) {
                                anyUpdate = true
                            }
                        }
                    }
                }
            }
        }
        return anyUpdate
    }

    private _eliminateCols(board: Board): boolean {
        let anyUpdate = false
        for (let value = 1; value <= SIZE; value++) {
            const mask = MASKS[value - 1]
            const colToRows = new Map<number, number[]>()
            for (let c = 0; c < SIZE; c++) {
                const rows: number[] = []
                for (let r = 0; r < SIZE; r++) {
                    const coord = Coord.all[r * 9 + c]
                    if (board.candidatePattern(coord) & mask) {
                        rows.push(r)
                    }
                }
                if (rows.length >= 2) colToRows.set(c, rows)
            }
            if (colToRows.size < 2) continue

            const cols = [...colToRows.keys()]
            for (const size of this.sizes) {
                if (size > cols.length) continue
                const colCombos = combinations(cols, size)
                for (const fishCols of colCombos) {
                    const unionRows = new Set<number>()
                    for (const c of fishCols) {
                        for (const r of colToRows.get(c)!) unionRows.add(r)
                    }
                    if (unionRows.size !== size) continue
                    // Fish found: eliminate from unionRows outside fishCols
                    for (const r of unionRows) {
                        for (let c = 0; c < SIZE; c++) {
                            if (fishCols.includes(c)) continue
                            const coord = Coord.all[r * 9 + c]
                            if (board.eraseCandidateValue(coord, value)) {
                                anyUpdate = true
                            }
                        }
                    }
                }
            }
        }
        return anyUpdate
    }
}

// ---------------------------------------------------------------------------
