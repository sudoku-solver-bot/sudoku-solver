import type { Board } from './Board'
import { Coord } from './Coord'
import { CoordGroup } from './CoordGroup'
import { bitCount, maskToValues, MASKS, SIZE, WILDCARD_PATTERN } from './Bitmask'
import { ALS, findALSes, deduplicateALS, seesEachOther, generateCombinations } from './ALSHelper'
import {
    type House,
    allHouses,
    houseContains,
    houseKey,
    findCandidatePositions,
    findHousesWithCandidates,
    generateCombinations as generateCombinationsFH,
} from './FishHelpers'

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
// SkyscraperCandidateEliminator
// ---------------------------------------------------------------------------

/**
 * Skyscraper: Find two rows (or two columns) where a candidate appears in
 * exactly two cells each, with one aligned cell in the same column (for
 * rows) or row (for columns). The two non-aligned cells can "see" each
 * other's intersection — eliminate the candidate from any cell that sees
 * both non-aligned ends.
 */
export class SkyscraperCandidateEliminator implements CandidateEliminator {
    readonly displayName = 'Skyscraper'

    eliminate(board: Board): boolean {
        let anyUpdate = false
        let stable: boolean
        do {
            stable = true
            if (this._skyscraperRows(board)) { anyUpdate = true; stable = false }
            if (this._skyscraperCols(board)) { anyUpdate = true; stable = false }
        } while (!stable)
        return anyUpdate
    }

    /** Skyscraper across rows (shared column). */
    private _skyscraperRows(board: Board): boolean {
        let anyUpdate = false
        for (let value = 1; value <= SIZE; value++) {
            const mask = MASKS[value - 1]
            // Find rows where candidate appears in exactly 2 cells
            const rowPositions: Map<number, number[]> = new Map()
            for (let r = 0; r < SIZE; r++) {
                const cols: number[] = []
                for (let c = 0; c < SIZE; c++) {
                    if (board.candidatePattern(Coord.all[r * 9 + c]) & mask) {
                        cols.push(c)
                    }
                }
                if (cols.length === 2) rowPositions.set(r, cols)
            }
            if (rowPositions.size < 2) continue

            const rows = [...rowPositions.keys()]
            for (let i = 0; i < rows.length; i++) {
                for (let j = i + 1; j < rows.length; j++) {
                    const r1 = rows[i], r2 = rows[j]
                    const cols1 = rowPositions.get(r1)!
                    const cols2 = rowPositions.get(r2)!

                    // Find shared column
                    const shared = cols1.filter(c => cols2.includes(c))
                    if (shared.length !== 1) continue
                    const sc = shared[0]

                    // Non-shared columns
                    const c1 = cols1.find(c => c !== sc)!
                    const c2 = cols2.find(c => c !== sc)!

                    // Eliminate from cells that see both non-aligned ends
                    // i.e., cells in row r1, col c2 AND row r2, col c1
                    const targets = [
                        Coord.all[r1 * 9 + c2],
                        Coord.all[r2 * 9 + c1],
                    ]
                    for (const t of targets) {
                        if (board.eraseCandidateValue(t, value)) {
                            anyUpdate = true
                        }
                    }
                }
            }
        }
        return anyUpdate
    }

    /** Skyscraper across columns (shared row). */
    private _skyscraperCols(board: Board): boolean {
        let anyUpdate = false
        for (let value = 1; value <= SIZE; value++) {
            const mask = MASKS[value - 1]
            const colPositions: Map<number, number[]> = new Map()
            for (let c = 0; c < SIZE; c++) {
                const rows: number[] = []
                for (let r = 0; r < SIZE; r++) {
                    if (board.candidatePattern(Coord.all[r * 9 + c]) & mask) {
                        rows.push(r)
                    }
                }
                if (rows.length === 2) colPositions.set(c, rows)
            }
            if (colPositions.size < 2) continue

            const cols = [...colPositions.keys()]
            for (let i = 0; i < cols.length; i++) {
                for (let j = i + 1; j < cols.length; j++) {
                    const c1 = cols[i], c2 = cols[j]
                    const rows1 = colPositions.get(c1)!
                    const rows2 = colPositions.get(c2)!

                    const shared = rows1.filter(r => rows2.includes(r))
                    if (shared.length !== 1) continue
                    const sr = shared[0]

                    const r1 = rows1.find(r => r !== sr)!
                    const r2 = rows2.find(r => r !== sr)!

                    const targets = [
                        Coord.all[r1 * 9 + c2],
                        Coord.all[r2 * 9 + c1],
                    ]
                    for (const t of targets) {
                        if (board.eraseCandidateValue(t, value)) {
                            anyUpdate = true
                        }
                    }
                }
            }
        }
        return anyUpdate
    }
}

// ---------------------------------------------------------------------------
// TwoStringKiteCandidateEliminator
// ---------------------------------------------------------------------------

/**
 * 2-String Kite: A candidate forms two strong links (one in a row, one in
 * a column) connected by a box where the candidate appears in 2 cells
 * (different row & column). The remote ends of the chain eliminate the
 * candidate from their intersection.
 */
export class TwoStringKiteCandidateEliminator implements CandidateEliminator {
    readonly displayName = '2-String Kite'

    eliminate(board: Board): boolean {
        let anyUpdate = false
        let stable: boolean
        do {
            stable = true
            for (let value = 1; value <= SIZE; value++) {
                if (this._eliminateValue(board, value)) {
                    anyUpdate = true
                    stable = false
                }
            }
        } while (!stable)
        return anyUpdate
    }

    private _eliminateValue(board: Board, value: number): boolean {
        const mask = MASKS[value - 1]

        // Build row strong links: rows where candidate appears in exactly 2 cols
        const rowLinks: Map<number, [number, number]> = new Map()
        for (let r = 0; r < SIZE; r++) {
            const cols: number[] = []
            for (let c = 0; c < SIZE; c++) {
                if (board.candidatePattern(Coord.all[r * 9 + c]) & mask) cols.push(c)
            }
            if (cols.length === 2) rowLinks.set(r, [cols[0], cols[1]])
        }

        // Build column strong links: cols where candidate appears in exactly 2 rows
        const colLinks: Map<number, [number, number]> = new Map()
        for (let c = 0; c < SIZE; c++) {
            const rows: number[] = []
            for (let r = 0; r < SIZE; r++) {
                if (board.candidatePattern(Coord.all[r * 9 + c]) & mask) rows.push(r)
            }
            if (rows.length === 2) colLinks.set(c, [rows[0], rows[1]])
        }

        // Find 2-string kite: a box with 2 candidate cells, one forming
        // a row strong link, the other a column strong link
        let anyUpdate = false

        for (const region of CoordGroup.region) {
            // Find cells in this region with the candidate
            const regionCells: Coord[] = []
            for (const coord of region.coords) {
                if (board.candidatePattern(coord) & mask) regionCells.push(coord)
            }

            // Need exactly 2 cells, in different rows and columns
            if (regionCells.length !== 2) continue
            const a = regionCells[0], b = regionCells[1]
            if (a.row === b.row || a.col === b.col) continue

            // Try: cell A forms row strong link, cell B forms col strong link
            if (rowLinks.has(a.row) && colLinks.has(b.col)) {
                const [ac1, ac2] = rowLinks.get(a.row)!
                // The row link's other end (not a.col)
                const rowOtherCol = ac1 === a.col ? ac2 : ac1
                const [br1, br2] = colLinks.get(b.col)!
                // The column link's other end (not b.row)
                const colOtherRow = br1 === b.row ? br2 : br1
                // Eliminate from intersection of rowOther and colOther
                const target = Coord.all[colOtherRow * 9 + rowOtherCol]
                if (board.eraseCandidateValue(target, value)) anyUpdate = true
            }

            // Try: cell A forms col strong link, cell B forms row strong link
            if (colLinks.has(a.col) && rowLinks.has(b.row)) {
                const [ar1, ar2] = colLinks.get(a.col)!
                const colOtherRow = ar1 === a.row ? ar2 : ar1
                const [bc1, bc2] = rowLinks.get(b.row)!
                const rowOtherCol = bc1 === b.col ? bc2 : bc1
                const target = Coord.all[colOtherRow * 9 + rowOtherCol]
                if (board.eraseCandidateValue(target, value)) anyUpdate = true
            }
        }

        return anyUpdate
    }
}

// ---------------------------------------------------------------------------
// EmptyRectangleCandidateEliminator
// ---------------------------------------------------------------------------

/**
 * Empty Rectangle: In a 3×3 box, if a candidate's cells are confined to
 * one row + at most 2 columns (or one column + at most 2 rows), find a
 * strong link outside the box that intersects and eliminate the candidate
 * from the chain's remote intersection.
 */
export class EmptyRectangleCandidateEliminator implements CandidateEliminator {
    readonly displayName = 'Empty Rectangle'

    eliminate(board: Board): boolean {
        let anyUpdate = false
        let stable: boolean
        do {
            stable = true
            for (let value = 1; value <= SIZE; value++) {
                if (this._eliminateValue(board, value)) {
                    anyUpdate = true
                    stable = false
                }
            }
        } while (!stable)
        return anyUpdate
    }

    private _eliminateValue(board: Board, value: number): boolean {
        const mask = MASKS[value - 1]
        let anyUpdate = false

        // Build column strong links
        const colLinks: Map<number, [number, number]> = new Map()
        for (let c = 0; c < SIZE; c++) {
            const rows: number[] = []
            for (let r = 0; r < SIZE; r++) {
                if (board.candidatePattern(Coord.all[r * 9 + c]) & mask) rows.push(r)
            }
            if (rows.length === 2) colLinks.set(c, [rows[0], rows[1]])
        }

        // Build row strong links
        const rowLinks: Map<number, [number, number]> = new Map()
        for (let r = 0; r < SIZE; r++) {
            const cols: number[] = []
            for (let c = 0; c < SIZE; c++) {
                if (board.candidatePattern(Coord.all[r * 9 + c]) & mask) cols.push(c)
            }
            if (cols.length === 2) rowLinks.set(r, [cols[0], cols[1]])
        }

        for (const region of CoordGroup.region) {
            const cells = region.coords.filter(c => board.candidatePattern(c) & mask)
            if (cells.length < 2) continue

            // Find rows and columns occupied by candidate in this region
            const candidateRows = new Set(cells.map(c => c.row))
            const candidateCols = new Set(cells.map(c => c.col))

            // Need L-shaped pattern: candidate occupies both multiple rows AND
            // multiple columns within the box
            if (candidateRows.size < 2 || candidateCols.size < 2) continue

            // For each pair of candidate rows and columns in the box,
            // the ERI is at their intersection. Check for strong links.
            const candRowArr = Array.from(candidateRows)
            const candColArr = Array.from(candidateCols)

            for (const erRow of candRowArr) {
                for (const erCol of candColArr) {
                    // Check for strong link in erRow — one end must be in the box
                    const rowLink = rowLinks.get(erRow)
                    if (rowLink) {
                        const [r1, r2] = rowLink
                        const r1InBox = Coord.all[r1 * 9 + erCol].region === region.coords[0].region
                        const r2InBox = Coord.all[r2 * 9 + erCol].region === region.coords[0].region
                        if (r1InBox && !r2InBox) {
                            // r2 is outside — eliminate from (r2, erCol)
                            const target = Coord.all[r2 * 9 + erCol]
                            if (board.candidatePattern(target) & mask) {
                                board.eraseCandidateValue(target, value)
                                anyUpdate = true
                            }
                        } else if (r2InBox && !r1InBox) {
                            // r1 is outside — eliminate from (r1, erCol)
                            const target = Coord.all[r1 * 9 + erCol]
                            if (board.candidatePattern(target) & mask) {
                                board.eraseCandidateValue(target, value)
                                anyUpdate = true
                            }
                        }
                    }

                    // Check for strong link in erCol — one end must be in the box
                    const colLink = colLinks.get(erCol)
                    if (colLink) {
                        const [c1, c2] = colLink
                        const c1InBox = Coord.all[erRow * 9 + c1].region === region.coords[0].region
                        const c2InBox = Coord.all[erRow * 9 + c2].region === region.coords[0].region
                        if (c1InBox && !c2InBox) {
                            // c2 is outside — eliminate from (erRow, c2)
                            const target = Coord.all[erRow * 9 + c2]
                            if (board.candidatePattern(target) & mask) {
                                board.eraseCandidateValue(target, value)
                                anyUpdate = true
                            }
                        } else if (c2InBox && !c1InBox) {
                            // c1 is outside — eliminate from (erRow, c1)
                            const target = Coord.all[erRow * 9 + c1]
                            if (board.candidatePattern(target) & mask) {
                                board.eraseCandidateValue(target, value)
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
// WWingCandidateEliminator
// ---------------------------------------------------------------------------

/**
 * W-Wing: Two bi-value cells with the same {link, target} candidates are
 * connected through a strong link on the 'link' candidate, meaning any
 * cell seeing both bi-value cells cannot contain the 'target' candidate.
 *
 * Equivalent to the Kotlin `WWingCandidateEliminator`.
 */
export class WWingCandidateEliminator implements CandidateEliminator {
    readonly displayName = 'W-Wing'

    eliminate(board: Board): boolean {
        let anyUpdate = false
        let stable: boolean
        do {
            stable = true
            if (this._eliminateWings(board)) { anyUpdate = true; stable = false }
        } while (!stable)
        return anyUpdate
    }

    private _eliminateWings(board: Board): boolean {
        // Find all bi-value cells grouped by candidate pair
        const groups = new Map<string, Coord[]>()
        for (const coord of Coord.all) {
            if (board.isConfirmed(coord)) continue
            const pat = board.candidatePattern(coord)
            if (bitCount(pat) !== 2) continue
            const vals = maskToValues(pat).sort((a, b) => a - b)
            const key = `${vals[0]},${vals[1]}`
            const list = groups.get(key)
            if (list) { list.push(coord) } else { groups.set(key, [coord]) }
        }

        let anyUpdate = false
        for (const cells of groups.values()) {
            if (cells.length < 2) continue
            for (let i = 0; i < cells.length; i++) {
                for (let j = i + 1; j < cells.length; j++) {
                    const a = cells[i]
                    const b = cells[j]
                    const vals = board.candidateValues(a).sort((x, y) => x - y)
                    const link = vals[0]
                    const target = vals[1]

                    if (this._checkWWing(board, a, b, link, target) ||
                        this._checkWWing(board, a, b, target, link)) {
                        anyUpdate = true
                    }
                }
            }
        }
        return anyUpdate
    }

    private _checkWWing(board: Board, a: Coord, b: Coord, link: number, target: number): boolean {
        // Find cells with 'link' candidate that see cell a
        const linkCellsA = this._findLinkCells(board, a, link)
        const linkCellsB = this._findLinkCells(board, b, link)

        for (const la of linkCellsA) {
            for (const lb of linkCellsB) {
                if (la === lb) continue
                if (!this._seesEachOther(la, lb)) continue
                if (!this._isStrongLink(board, la, lb, link)) continue
                return this._eliminateCommonPeers(board, a, b, target)
            }
        }
        return false
    }

    private _findLinkCells(board: Board, cell: Coord, candidate: number): Coord[] {
        const mask = MASKS[candidate - 1]
        const result: Coord[] = []
        for (const coord of Coord.all) {
            if (coord === cell) continue
            if (!(board.candidatePattern(coord) & mask)) continue
            if (this._seesEachOther(coord, cell)) result.push(coord)
        }
        return result
    }

    private _seesEachOther(a: Coord, b: Coord): boolean {
        return a.row === b.row || a.col === b.col ||
            (Math.floor(a.row / 3) === Math.floor(b.row / 3) &&
             Math.floor(a.col / 3) === Math.floor(b.col / 3))
    }

    private _isStrongLink(board: Board, a: Coord, b: Coord, candidate: number): boolean {
        const mask = MASKS[candidate - 1]

        // Check shared units: row, col, region
        const units: CoordGroup[] = []
        if (a.row === b.row) units.push(CoordGroup.horizontal[a.row])
        if (a.col === b.col) units.push(CoordGroup.vertical[a.col])
        if (a.region === b.region) units.push(CoordGroup.region[a.region])

        for (const unit of units) {
            const cellsWithCandidate = unit.coords.filter(c =>
                !board.isConfirmed(c) && (board.candidatePattern(c) & mask)
            )
            if (cellsWithCandidate.length === 2 &&
                cellsWithCandidate.includes(a) && cellsWithCandidate.includes(b)) {
                return true
            }
        }
        return false
    }

    private _eliminateCommonPeers(board: Board, a: Coord, b: Coord, cand: number): boolean {
        let anyUpdate = false
        for (const coord of Coord.all) {
            if (coord === a || coord === b) continue
            if (board.isConfirmed(coord)) continue
            if (!(board.candidatePattern(coord) & MASKS[cand - 1])) continue
            if (this._seesEachOther(coord, a) && this._seesEachOther(coord, b)) {
                if (board.eraseCandidateValue(coord, cand)) anyUpdate = true
            }
        }
        return anyUpdate
    }
}

// ---------------------------------------------------------------------------
// XYWingCandidateEliminator
// ---------------------------------------------------------------------------

/**
 * XY-Wing finds 3 cells forming a pattern: a pivot cell with 2 candidates
 * (X,Y) and two wing cells — one with (X,Z) and one with (Y,Z) — where Z
 * can be eliminated from any cell that sees both wings.
 *
 * Reference: https://www.sudopedia.org/wiki/XY-Wing
 *
 * Equivalent to the Kotlin `XYWingCandidateEliminator`.
 */
export class XYWingCandidateEliminator implements CandidateEliminator {
    readonly displayName = 'XY-Wing'

    eliminate(board: Board): boolean {
        let anyUpdate = false

        // Find all cells with exactly 2 candidates (potential pivots)
        const pivots: Coord[] = []
        for (const coord of Coord.all) {
            if (!board.isConfirmed(coord) && bitCount(board.candidatePattern(coord)) === 2) {
                pivots.push(coord)
            }
        }

        for (const pivot of pivots) {
            const pivotCandidates = board.candidateValues(pivot)
            if (pivotCandidates.length !== 2) continue

            const x = pivotCandidates[0]
            const y = pivotCandidates[1]

            // Find wings with {X, Z} that see the pivot
            const wingsWithX = this._findWings(board, pivot, x)
            // Find wings with {Y, Z} that see the pivot
            const wingsWithY = this._findWings(board, pivot, y)

            // Try all combinations of wings
            for (const wing1 of wingsWithX) {
                for (const wing2 of wingsWithY) {
                    // Wing1 has {X, Z}, Wing2 has {Y, Z}
                    // Z is the candidate in both wings that's not X or Y
                    const wing1Candidates = board.candidateValues(wing1)
                    const wing2Candidates = board.candidateValues(wing2)

                    const z1 = wing1Candidates.find(c => c !== x)
                    const z2 = wing2Candidates.find(c => c !== y)

                    if (z1 == null || z2 == null || z1 !== z2) continue
                    const z = z1

                    // Check if wings see each other
                    if (!this._seesEachOther(wing1, wing2)) continue

                    // Find cells that see both wings and eliminate Z
                    if (this._eliminateFromCommonPeers(board, wing1, wing2, z)) {
                        anyUpdate = true
                    }
                }
            }
        }

        return anyUpdate
    }

    /**
     * Find cells with exactly 2 candidates that see the pivot and share
     * the specified candidate with the pivot.
     */
    private _findWings(board: Board, pivot: Coord, sharedCandidate: number): Coord[] {
        const result: Coord[] = []
        for (const coord of Coord.all) {
            if (coord === pivot) continue
            if (board.isConfirmed(coord)) continue
            const candidates = board.candidateValues(coord)
            if (candidates.length !== 2) continue
            if (!candidates.includes(sharedCandidate)) continue
            if (!this._seesEachOther(coord, pivot)) continue
            result.push(coord)
        }
        return result
    }

    private _seesEachOther(a: Coord, b: Coord): boolean {
        return (
            a.row === b.row ||
            a.col === b.col ||
            (Math.floor(a.row / 3) === Math.floor(b.row / 3) &&
                Math.floor(a.col / 3) === Math.floor(b.col / 3))
        )
    }

    /**
     * Eliminate a candidate from all cells that see both wing1 and wing2.
     */
    private _eliminateFromCommonPeers(
        board: Board,
        wing1: Coord,
        wing2: Coord,
        candidate: number,
    ): boolean {
        let anyUpdate = false
        for (const coord of Coord.all) {
            if (coord === wing1 || coord === wing2) continue
            if (board.isConfirmed(coord)) continue
            if (this._seesEachOther(coord, wing1) && this._seesEachOther(coord, wing2)) {
                if (board.eraseCandidateValue(coord, candidate)) anyUpdate = true
            }
        }
        return anyUpdate
    }
}

// ---------------------------------------------------------------------------
// XYZWingCandidateEliminator
// ---------------------------------------------------------------------------

/**
 * XYZ-Wing is an extension of XY-Wing where the pivot has 3 candidates
 * {X, Y, Z}. Wing 1 has {X, Z} and sees the pivot. Wing 2 has {Y, Z}
 * and sees the pivot. Z can be eliminated from any cell that sees all
 * three cells (pivot + both wings).
 *
 * Reference: https://www.sudopedia.org/wiki/XYZ-Wing
 *
 * Equivalent to the Kotlin `XYZWingCandidateEliminator`.
 */
export class XYZWingCandidateEliminator implements CandidateEliminator {
    readonly displayName = 'XYZ-Wing'

    eliminate(board: Board): boolean {
        let anyUpdate = false

        // Find all cells with exactly 3 candidates (potential pivots)
        const pivots: Coord[] = []
        for (const coord of Coord.all) {
            if (!board.isConfirmed(coord) && bitCount(board.candidatePattern(coord)) === 3) {
                pivots.push(coord)
            }
        }

        for (const pivot of pivots) {
            const pivotCandidates = board.candidateValues(pivot)
            if (pivotCandidates.length !== 3) continue

            // Try all combinations of which candidate is Z (the common one)
            for (let zi = 0; zi < 3; zi++) {
                const z = pivotCandidates[zi]
                const remaining = pivotCandidates.filter((_, i) => i !== zi)
                if (remaining.length !== 2) continue

                const x = remaining[0]
                const y = remaining[1]

                // Find wings with exactly {X, Z} that see the pivot
                const wingsWithX = this._findWings(board, pivot, x, z)
                // Find wings with exactly {Y, Z} that see the pivot
                const wingsWithY = this._findWings(board, pivot, y, z)

                // Try all combinations of wings
                for (const wing1 of wingsWithX) {
                    for (const wing2 of wingsWithY) {
                        // Eliminate Z from cells seeing all three
                        if (this._eliminateFromCommonPeers(board, pivot, wing1, wing2, z)) {
                            anyUpdate = true
                        }
                    }
                }
            }
        }

        return anyUpdate
    }

    /**
     * Find cells with exactly 2 candidates {c1, c2} that see the pivot.
     */
    private _findWings(board: Board, pivot: Coord, c1: number, c2: number): Coord[] {
        const result: Coord[] = []
        for (const coord of Coord.all) {
            if (coord === pivot) continue
            if (board.isConfirmed(coord)) continue
            const candidates = board.candidateValues(coord)
            if (candidates.length !== 2) continue
            if (candidates[0] !== c1 || candidates[1] !== c2) continue
            if (!this._seesEachOther(coord, pivot)) continue
            result.push(coord)
        }
        return result
    }

    private _seesEachOther(a: Coord, b: Coord): boolean {
        return (
            a.row === b.row ||
            a.col === b.col ||
            (Math.floor(a.row / 3) === Math.floor(b.row / 3) &&
                Math.floor(a.col / 3) === Math.floor(b.col / 3))
        )
    }

    /**
     * Eliminate a candidate from all cells that see all three (pivot, wing1, wing2).
     */
    private _eliminateFromCommonPeers(
        board: Board,
        pivot: Coord,
        wing1: Coord,
        wing2: Coord,
        candidate: number,
    ): boolean {
        let anyUpdate = false
        for (const coord of Coord.all) {
            if (coord === pivot || coord === wing1 || coord === wing2) continue
            if (board.isConfirmed(coord)) continue
            if (!board.candidateValues(coord).includes(candidate)) continue
            if (
                this._seesEachOther(coord, pivot) &&
                this._seesEachOther(coord, wing1) &&
                this._seesEachOther(coord, wing2)
            ) {
                if (board.eraseCandidateValue(coord, candidate)) anyUpdate = true
            }
        }
        return anyUpdate
    }
}

// ---------------------------------------------------------------------------
// UniqueRectanglesCandidateEliminator
// ---------------------------------------------------------------------------

/**
 * Unique Rectangles elimination technique, Type 1.
 *
 * Detects deadly rectangle patterns based on the uniqueness constraint:
 * a proper Sudoku puzzle has exactly one solution. A deadly pattern would
 * create multiple solutions, so candidates that force it can be eliminated.
 *
 * A deadly rectangle consists of 4 cells at (r1,c1), (r1,c2), (r2,c1),
 * (r2,c2) where all 4 contain the same 2 candidates {X, Y}. They must span
 * exactly 2 rows, 2 columns, and 2 different 3×3 boxes.
 *
 * Type 1 (Single Extra Candidate): If exactly one of the 4 cells has
 * additional candidates beyond {X, Y}, then X and Y can be eliminated from
 * that cell, leaving only the extra candidates.
 *
 * Reference:
 * - https://www.sudokuwiki.org/Unique_Rectangles (SudokuWiki)
 * - https://www.sudopedia.org/wiki/Unique_Rectangle
 *
 * Equivalent to the Kotlin `UniqueRectanglesCandidateEliminator`.
 */
export class UniqueRectanglesCandidateEliminator implements CandidateEliminator {
    readonly displayName = 'Unique Rectangles'

    eliminate(board: Board): boolean {
        let anyUpdate = false

        for (let r1 = 0; r1 < 8; r1++) {
            for (let r2 = r1 + 1; r2 < 9; r2++) {
                for (let c1 = 0; c1 < 8; c1++) {
                    for (let c2 = c1 + 1; c2 < 9; c2++) {
                        const corners: Coord[] = [
                            Coord.all[r1 * 9 + c1],
                            Coord.all[r1 * 9 + c2],
                            Coord.all[r2 * 9 + c1],
                            Coord.all[r2 * 9 + c2],
                        ]

                        const pattern = this._findDeadlyRectangle(board, corners)
                        if (pattern != null) {
                            if (this._applyType1(board, corners, pattern[0], pattern[1])) {
                                anyUpdate = true
                            }
                        }
                    }
                }
            }
        }

        return anyUpdate
    }

    private _findDeadlyRectangle(
        board: Board,
        corners: Coord[],
    ): [number, number] | null {
        for (const coord of corners) {
            if (board.isConfirmed(coord)) return null
        }

        const box1 = this._getBoxIndex(corners[0])
        const box2 = this._getBoxIndex(corners[1])
        const box3 = this._getBoxIndex(corners[2])
        const box4 = this._getBoxIndex(corners[3])

        if (box1 !== box3 || box2 !== box4) return null
        if (box1 === box2) return null

        const candidatesList = corners.map((c) => [
            ...maskToValues(board.candidatePattern(c)),
        ])

        const common = this._intersectAll(candidatesList)
        if (common.length < 2 || common.length !== 2) return null

        const [x, y] = common

        const cellsWithExtras = candidatesList.filter((c) => c.length > 2).length
        const cellsWithTwo = candidatesList.filter((c) => c.length === 2).length

        if (cellsWithExtras < 1 || cellsWithTwo < 3) return null

        return [x, y]
    }

    private _applyType1(
        board: Board,
        corners: Coord[],
        x: number,
        y: number,
    ): boolean {
        let anyUpdate = false
        const maskX = MASKS[x - 1]
        const maskY = MASKS[y - 1]

        const cellsWithExtras = corners.filter((coord) => {
            const pattern = board.candidatePattern(coord)
            return (
                (pattern & maskX) !== 0 &&
                (pattern & maskY) !== 0 &&
                bitCount(pattern) > 2
            )
        })

        if (cellsWithExtras.length === 1) {
            const target = cellsWithExtras[0]
            const erasedX = board.eraseCandidateValue(target, x)
            const erasedY = board.eraseCandidateValue(target, y)
            if (erasedX || erasedY) anyUpdate = true
        }

        return anyUpdate
    }

    private _getBoxIndex(coord: Coord): number {
        return Math.floor(coord.row / 3) * 3 + Math.floor(coord.col / 3)
    }

    private _intersectAll(lists: number[][]): number[] {
        if (lists.length === 0) return []
        let result = new Set(lists[0])
        for (let i = 1; i < lists.length; i++) {
            result = new Set(lists[i].filter((v) => result.has(v)))
        }
        return [...result]
    }
}


// ---------------------------------------------------------------------------
// ForcingChainsCandidateEliminator
// ---------------------------------------------------------------------------

export class ForcingChainsCandidateEliminator implements CandidateEliminator {
    readonly displayName = 'Forcing Chains'

    private static readonly MAX_CHAIN_DEPTH = 10

    eliminate(board: Board): boolean {
        let anyUpdate = false

        // Find all bi-value cells
        const biValueCells = Coord.all.filter((coord) => {
            if (board.isConfirmed(coord)) return false
            return bitCount(board.candidatePattern(coord)) === 2
        })

        for (const pivot of biValueCells) {
            const candidates = maskToValues(board.candidatePattern(pivot))
            if (candidates.length !== 2) continue

            const [x, y] = candidates

            // Explore both paths on copies of the board
            const boardX = board.copy()
            const boardY = board.copy()

            const resultX = this._exploreConsequences(boardX, pivot, x, 0)
            const resultY = this._exploreConsequences(boardY, pivot, y, 0)

            // Contradiction-based eliminations
            if (resultX.isContradiction) {
                if (board.eraseCandidateValue(pivot, x)) anyUpdate = true
                continue
            }

            if (resultY.isContradiction) {
                if (board.eraseCandidateValue(pivot, y)) anyUpdate = true
                continue
            }
        }

        return anyUpdate
    }

    /**
     * Explore consequences of setting a cell to a specific value.
     * Applies eliminations on the board copy for accurate propagation.
     */
    private _exploreConsequences(
        board: Board,
        startCoord: Coord,
        startValue: number,
        startDepth: number,
    ): ForcingChainResult {
        const confirmed = new Map<Coord, number>()
        const toProcess: Array<{ coord: Coord; value: number; depth: number }> = []

        toProcess.push({ coord: startCoord, value: startValue, depth: startDepth })

        while (toProcess.length > 0) {
            const { coord, value, depth } = toProcess.shift()!

            if (depth > ForcingChainsCandidateEliminator.MAX_CHAIN_DEPTH) continue

            // If this cell is already confirmed to a different value, contradiction
            if (board.isConfirmed(coord)) {
                const existingValue = board.value(coord)
                if (existingValue !== 0 && existingValue !== value) {
                    return { isContradiction: true, confirmedValues: confirmed }
                }
                if (existingValue === value) continue
            }

            // If this cell doesn't have this candidate, contradiction
            if ((board.candidatePattern(coord) & MASKS[value - 1]) === 0) {
                return { isContradiction: true, confirmedValues: confirmed }
            }

            // Apply: set the cell to this value
            board.markValue(coord, value)
            confirmed.set(coord, value)

            // Propagate: eliminate this value from all peers
            const peers = this._getPeers(coord)
            for (const peer of peers) {
                const erased = board.eraseCandidateValue(peer, value)
                if (erased) {
                    // Check if peer now has zero candidates — contradiction
                    if (board.candidatePattern(peer) === 0) {
                        return { isContradiction: true, confirmedValues: confirmed }
                    }
                    // If peer now has exactly one candidate, it's a forced value
                    if (
                        !confirmed.has(peer) &&
                        bitCount(board.candidatePattern(peer)) === 1
                    ) {
                        const forcedValue = board.value(peer)
                        if (forcedValue !== 0) {
                            toProcess.push({
                                coord: peer,
                                value: forcedValue,
                                depth: depth + 1,
                            })
                        }
                    }
                }
            }
        }

        return { isContradiction: false, confirmedValues: confirmed }
    }

    /**
     * Get all peers of a coordinate (cells sharing row, column, or box).
     */
    private _getPeers(coord: Coord): Coord[] {
        const peers: Coord[] = []
        const seen = new Set<number>()
        for (const group of CoordGroup.all) {
            if (group.coords.includes(coord)) {
                for (const c of group.coords) {
                    if (!seen.has(c.index)) {
                        seen.add(c.index)
                        peers.push(c)
                    }
                }
            }
        }
        return peers.filter((c) => c !== coord)
    }
}


// ---------------------------------------------------------------------------
// SimpleColoringCandidateEliminator
// ---------------------------------------------------------------------------

export class SimpleColoringCandidateEliminator implements CandidateEliminator {
    readonly displayName = 'Simple Coloring'

    private readonly UNCOLORED = 0
    private readonly COLOR_A = 1
    private readonly COLOR_B = 2

    eliminate(board: Board): boolean {
        let anyUpdate = false

        for (let candidate = 1; candidate <= 9; candidate++) {
            if (this._applySimpleColoring(board, candidate)) {
                anyUpdate = true
            }
        }

        return anyUpdate
    }

    private _applySimpleColoring(board: Board, candidate: number): boolean {
        let anyUpdate = false

        const { colors, contradictionCells } = this._buildColorChains(board, candidate)
        if (colors.size === 0) return false

        const colorA: Coord[] = []
        const colorB: Coord[] = []
        for (const [coord, color] of colors) {
            if (color === this.COLOR_A) colorA.push(coord)
            else if (color === this.COLOR_B) colorB.push(coord)
        }

        if (colorA.length === 0 || colorB.length === 0) return false

        // Rule 4: Contradiction detected — eliminate all cells of the
        // affected color.
        if (contradictionCells.size > 0) {
            const firstContradiction = contradictionCells.values().next().value
            if (firstContradiction != null) {
                const contradictionColor = colors.get(firstContradiction)
                const toEliminate =
                    contradictionColor === this.COLOR_A ? colorA : colorB
                for (const coord of toEliminate) {
                    if (
                        !board.isConfirmed(coord) &&
                        (board.candidatePattern(coord) & MASKS[candidate - 1]) !== 0
                    ) {
                        if (board.eraseCandidateValue(coord, candidate)) {
                            anyUpdate = true
                        }
                    }
                }
            }
            return anyUpdate
        }

        // Rule 2: Eliminate from cells that see both colors.
        // A cell seeing both Color A and Color B in the same unit means one
        // of those colors must be the solution, so this cell can't contain
        // the candidate.
        for (const coord of Coord.all) {
            if (board.isConfirmed(coord)) continue
            if (colors.has(coord)) continue
            if ((board.candidatePattern(coord) & MASKS[candidate - 1]) === 0) continue

            const seesColorA = colorA.some((c) => this._seesEachOther(coord, c))
            const seesColorB = colorB.some((c) => this._seesEachOther(coord, c))

            if (seesColorA && seesColorB) {
                if (board.eraseCandidateValue(coord, candidate)) {
                    anyUpdate = true
                }
            }
        }

        return anyUpdate
    }

    private _buildColorChains(
        board: Board,
        candidate: number,
    ): { colors: Map<Coord, number>; contradictionCells: Set<Coord> } {
        const candidateMask = MASKS[candidate - 1]

        // Find all unresolved cells with this candidate
        const candidateCells: Coord[] = []
        for (const coord of Coord.all) {
            if (
                !board.isConfirmed(coord) &&
                (board.candidatePattern(coord) & candidateMask) !== 0
            ) {
                candidateCells.push(coord)
            }
        }

        if (candidateCells.length === 0) {
            return { colors: new Map(), contradictionCells: new Set() }
        }

        const candidateSet = new Set(candidateCells)

        // Build adjacency list from conjugate pairs
        const adj = new Map<Coord, Coord[]>()
        for (const cell of candidateCells) {
            adj.set(cell, [])
        }

        const pairs = this._findConjugatePairs(board, candidate, candidateCells)
        for (const [c1, c2] of pairs) {
            adj.get(c1)!.push(c2)
            adj.get(c2)!.push(c1)
        }

        // BFS: find connected components and check bipartiteness
        const colors = new Map<Coord, number>()
        const contradictionCells = new Set<Coord>()
        const visited = new Set<Coord>()

        for (const start of candidateSet) {
            if (visited.has(start)) continue

            const component: Coord[] = []
            const queue: Coord[] = [start]
            colors.set(start, this.COLOR_A)
            let isBipartite = true

            while (queue.length > 0) {
                const current = queue.shift()!
                if (visited.has(current)) continue
                visited.add(current)
                component.push(current)

                const neighbors = adj.get(current) ?? []
                const currentColor = colors.get(current)!
                for (const neighbor of neighbors) {
                    if (!visited.has(neighbor)) {
                        colors.set(
                            neighbor,
                            currentColor === this.COLOR_A
                                ? this.COLOR_B
                                : this.COLOR_A,
                        )
                        queue.push(neighbor)
                    } else if (colors.get(neighbor) === currentColor) {
                        isBipartite = false
                    }
                }
            }

            if (!isBipartite) {
                for (const cell of component) {
                    contradictionCells.add(cell)
                }
            }
        }

        return { colors, contradictionCells }
    }

    private _findConjugatePairs(
        board: Board,
        candidate: number,
        candidateCells: Coord[],
    ): Array<[Coord, Coord]> {
        const pairs: Array<[Coord, Coord]> = []

        for (let row = 0; row < 9; row++) {
            const cellsInRow = candidateCells.filter((c) => c.row === row)
            if (cellsInRow.length === 2) {
                pairs.push([cellsInRow[0], cellsInRow[1]])
            }
        }

        for (let col = 0; col < 9; col++) {
            const cellsInCol = candidateCells.filter((c) => c.col === col)
            if (cellsInCol.length === 2) {
                pairs.push([cellsInCol[0], cellsInCol[1]])
            }
        }

        for (let regionRow = 0; regionRow < 3; regionRow++) {
            for (let regionCol = 0; regionCol < 3; regionCol++) {
                const cellsInRegion = candidateCells.filter(
                    (c) =>
                        Math.floor(c.row / 3) === regionRow &&
                        Math.floor(c.col / 3) === regionCol,
                )
                if (cellsInRegion.length === 2) {
                    pairs.push([cellsInRegion[0], cellsInRegion[1]])
                }
            }
        }

        return pairs
    }

    private _seesEachOther(a: Coord, b: Coord): boolean {
        return (
            a.row === b.row ||
            a.col === b.col ||
            (Math.floor(a.row / 3) === Math.floor(b.row / 3) &&
                Math.floor(a.col / 3) === Math.floor(b.col / 3))
        )
    }
}

// ---------------------------------------------------------------------------
// DeathBlossomCandidateEliminator
// ---------------------------------------------------------------------------

// ALS type imported from ALSHelper
/**
 * Death Blossom finds a stem cell with N candidates and N Almost Locked Sets
 * (petals). Each petal ALS contains one of the stem's candidates, and every
 * cell in the ALS with that candidate can see the stem. Candidates shared
 * between all petals can be eliminated from cells seeing all instances.
 *
 * Reference: https://www.sudopedia.org/wiki/Death_Blossom
 *
 * Equivalent to the Kotlin `DeathBlossomCandidateEliminator`.
 */
export class DeathBlossomCandidateEliminator implements CandidateEliminator {
    readonly displayName = 'Death Blossom'

    eliminate(board: Board): boolean {
        let anyUpdate = false

        const allALS = this._findAllALS(board)
        if (allALS.length === 0) return false

        for (const stem of Coord.all) {
            if (board.isConfirmed(stem)) continue
            const stemCandidates = new Set(board.candidateValues(stem))
            if (stemCandidates.size < 2) continue

            const petalsByCandidate = new Map<number, ALS[]>()

            for (const x of stemCandidates) {
                const eligible: ALS[] = []
                for (const als of allALS) {
                    if (!als.candidates.has(x)) continue
                    if (als.cells.includes(stem)) continue
                    const allXSeeStem = als.cells.every(cell =>
                        board.candidateValues(cell).includes(x) &&
                        this._seesEachOther(cell, stem)
                    )
                    if (allXSeeStem) eligible.push(als)
                }
                if (eligible.length > 0) petalsByCandidate.set(x, eligible)
            }

            if (petalsByCandidate.size < stemCandidates.size) continue

            const candidateList = [...stemCandidates]
            const combos = this._generatePetalCombinations(petalsByCandidate, candidateList)

            for (const petals of combos) {
                const allCells = petals.flatMap(p => p.cells)
                if (new Set(allCells).size !== allCells.length) continue

                const commonCandidates = new Set(
                    [...petals[0].candidates].filter(c =>
                        petals.every(p => p.candidates.has(c))
                    )
                )
                for (const sc of stemCandidates) commonCandidates.delete(sc)
                if (commonCandidates.size === 0) continue

                for (const z of commonCandidates) {
                    const zCellsPerALS = petals.map(als =>
                        als.cells.filter(c => board.candidateValues(c).includes(z))
                    )

                    for (const coord of Coord.all) {
                        if (coord === stem) continue
                        if (allCells.includes(coord)) continue
                        if (board.isConfirmed(coord)) continue
                        if (!board.candidateValues(coord).includes(z)) continue

                        const seesAllZ = zCellsPerALS.every(zCells =>
                            zCells.every(zc => this._seesEachOther(coord, zc))
                        )

                        if (seesAllZ) {
                            if (board.eraseCandidateValue(coord, z)) anyUpdate = true
                        }
                    }
                }
            }
        }

        return anyUpdate
    }

    private _findAllALS(board: Board): ALS[] {
        const result: ALS[] = []
        for (const group of CoordGroup.all) {
            const unconfirmed = group.coords.filter(c => !board.isConfirmed(c))
            for (let size = 2; size <= Math.min(4, unconfirmed.length); size++) {
                for (const combo of this._generateCombinations(unconfirmed, size)) {
                    const allCands = new Set(combo.flatMap(c => board.candidateValues(c)))
                    if (allCands.size === size + 1) {
                        result.push({ cells: combo, candidates: allCands })
                    }
                }
            }
        }
        return result
    }

    private _generatePetalCombinations(
        petalsByCandidate: Map<number, ALS[]>,
        candidates: number[]
    ): ALS[][] {
        if (candidates.length === 0) return [[]]

        const head = candidates[0]
        const tail = candidates.slice(1)
        const headALS = petalsByCandidate.get(head)
        if (!headALS) return []

        const restCombos = this._generatePetalCombinations(petalsByCandidate, tail)
        if (restCombos.length === 0 && tail.length > 0) return []

        const result: ALS[][] = []
        for (const als of headALS) {
            if (tail.length === 0) {
                result.push([als])
            } else {
                for (const rest of restCombos) {
                    result.push([als, ...rest])
                }
            }
        }
        return result
    }

    private _generateCombinations<T>(list: T[], k: number): T[][] {
        if (k === 0) return [[]]
        if (list.length === 0 || k > list.length) return []
        const result: T[][] = []
        for (let i = 0; i < list.length; i++) {
            for (const rest of this._generateCombinations(list.slice(i + 1), k - 1)) {
                result.push([list[i], ...rest])
            }
        }
        return result
    }

    private _seesEachOther(a: Coord, b: Coord): boolean {
        return a.row === b.row || a.col === b.col ||
            (Math.floor(a.row / 3) === Math.floor(b.row / 3) &&
             Math.floor(a.col / 3) === Math.floor(b.col / 3))
    }
}

// ---------------------------------------------------------------------------
// FrankenFishCandidateEliminator
// ---------------------------------------------------------------------------

/**
 * Franken Fish extends basic fish patterns by detecting sets of rows (or
 * columns) that share identical candidate column (or row) positions.
 *
 * If N rows each contain candidate V in exactly the same N columns, then V
 * can be eliminated from those N columns in all other rows. The symmetric
 * operation eliminates V from rows outside a matching set of columns.
 *
 * Supports fish sizes 2–4.
 *
 * Reference: https://www.sudopedia.org/wiki/Franken_Fish
 *
 * Equivalent to the Kotlin `FrankenFishCandidateEliminator`.
 */
export class FrankenFishCandidateEliminator implements CandidateEliminator {
    readonly displayName = 'Franken Fish'

    eliminate(board: Board): boolean {
        let anyUpdate = false

        for (let v = 1; v <= 9; v++) {
            if (this._eliminateRowFrankenFish(board, v)) anyUpdate = true
            if (this._eliminateColumnFrankenFish(board, v)) anyUpdate = true
        }

        return anyUpdate
    }

    // -----------------------------------------------------------------------
    // Row-based: find N rows whose candidate-V positions sit in the same N
    // columns → eliminate V from those columns in all other rows.
    // -----------------------------------------------------------------------
    private _eliminateRowFrankenFish(board: Board, v: number): boolean {
        let anyUpdate = false

        // Map: row → set of columns where candidate V appears
        const rowToColumns = new Map<number, Set<number>>()
        for (let row = 0; row < 9; row++) {
            const cols = new Set<number>()
            for (let col = 0; col < 9; col++) {
                const coord = Coord.all[row * 9 + col]
                if (
                    !board.isConfirmed(coord) &&
                    board.candidateValues(coord).includes(v)
                ) {
                    cols.add(col)
                }
            }
            if (cols.size >= 2 && cols.size <= 4) {
                rowToColumns.set(row, cols)
            }
        }

        // Group rows by their column sets (using a serialised key)
        const columnSetKey = (s: Set<number>) =>
            [...s].sort((a, b) => a - b).join(',')
        const groups = new Map<string, number[]>()
        for (const [row, cols] of rowToColumns) {
            const key = columnSetKey(cols)
            if (!groups.has(key)) groups.set(key, [])
            groups.get(key)!.push(row)
        }

        for (const [, rows] of groups) {
            if (rows.length < 2) continue

            // For each row that contributed, its column set size is the fish
            // size. Take the first row's column set as representative.
            const representativeRow = rows[0]
            const cols = rowToColumns.get(representativeRow)
            if (!cols) continue
            const n = cols.size
            if (rows.length < n) continue
            if (n < 2 || n > 4) continue

            const fishRows = new Set(rows.slice(0, n))

            for (let row = 0; row < 9; row++) {
                if (fishRows.has(row)) continue
                for (const col of cols) {
                    const coord = Coord.all[row * 9 + col]
                    if (!board.isConfirmed(coord)) {
                        if (board.eraseCandidateValue(coord, v)) anyUpdate = true
                    }
                }
            }
        }

        return anyUpdate
    }

    // -----------------------------------------------------------------------
    // Column-based: find N columns whose candidate-V positions sit in the
    // same N rows → eliminate V from those rows in all other columns.
    // -----------------------------------------------------------------------
    private _eliminateColumnFrankenFish(board: Board, v: number): boolean {
        let anyUpdate = false

        // Map: column → set of rows where candidate V appears
        const columnToRows = new Map<number, Set<number>>()
        for (let col = 0; col < 9; col++) {
            const rows = new Set<number>()
            for (let row = 0; row < 9; row++) {
                const coord = Coord.all[row * 9 + col]
                if (
                    !board.isConfirmed(coord) &&
                    board.candidateValues(coord).includes(v)
                ) {
                    rows.add(row)
                }
            }
            if (rows.size >= 2 && rows.size <= 4) {
                columnToRows.set(col, rows)
            }
        }

        // Group columns by their row sets
        const rowSetKey = (s: Set<number>) =>
            [...s].sort((a, b) => a - b).join(',')
        const groups = new Map<string, number[]>()
        for (const [col, rows] of columnToRows) {
            const key = rowSetKey(rows)
            if (!groups.has(key)) groups.set(key, [])
            groups.get(key)!.push(col)
        }

        for (const [, cols] of groups) {
            const representativeCol = cols[0]
            const rows = columnToRows.get(representativeCol)
            if (!rows) continue
            const n = rows.size
            if (cols.length < n) continue
            if (n < 2 || n > 4) continue

            const fishCols = new Set(cols.slice(0, n))

            for (let col = 0; col < 9; col++) {
                if (fishCols.has(col)) continue
                for (const row of rows) {
                    const coord = Coord.all[row * 9 + col]
                    if (!board.isConfirmed(coord)) {
                        if (board.eraseCandidateValue(coord, v)) anyUpdate = true
                    }
                }
            }
        }

        return anyUpdate
    }
}


// ---------------------------------------------------------------------------
// ALSXZCandidateEliminator
// ---------------------------------------------------------------------------

/**
 * ALS-XZ (Almost Locked Sets XZ) finds two Almost Locked Sets (N cells with
 * N+1 candidates) that share a "Restricted Common" (X). When X is restricted
 * (all cells with X in ALS1 see all cells with X in ALS2), the non-restricted
 * common candidate Z can be eliminated from any cell that sees all cells
 * containing Z in both ALSs.
 *
 * Reference: https://www.sudopedia.org/wiki/ALS-XZ
 *
 * Equivalent to the Kotlin `ALSXZCandidateEliminator`.
 */
export class ALSXZCandidateEliminator implements CandidateEliminator {
    readonly displayName = 'ALS-XZ'

    /**
     * Internal ALS representation: N cells with exactly N+1 candidates.
     */
    private static _Als(coords: Coord[], candidates: Set<number>) {
        const coordsSet = new Set(coords)
        return { coords, candidates, coordsSet }
    }

    eliminate(board: Board): boolean {
        let anyUpdate = false

        const allALS = this._findAllALS(board)
        const uniqueALS = this._deduplicateALS(allALS)

        for (let i = 0; i < uniqueALS.length; i++) {
            for (let j = i + 1; j < uniqueALS.length; j++) {
                const als1 = uniqueALS[i]
                const als2 = uniqueALS[j]

                // Skip overlapping ALSs
                if (als1.coords.some(c => als2.coordsSet.has(c))) continue

                const restrictedCommons = this._findRestrictedCommons(board, als1, als2)
                if (restrictedCommons.length === 0) continue

                const sharedCandidates = new Set(
                    [...als1.candidates].filter(c => als2.candidates.has(c))
                )
                const nonRestricted = [...sharedCandidates].filter(
                    c => !restrictedCommons.includes(c)
                )

                for (const z of nonRestricted) {
                    if (this._applyType1Elimination(board, als1, als2, z)) {
                        anyUpdate = true
                    }
                }
            }
        }

        return anyUpdate
    }

    private _findAllALS(board: Board): ReturnType<typeof ALSXZCandidateEliminator._Als>[] {
        const result: ReturnType<typeof ALSXZCandidateEliminator._Als>[] = []

        for (const group of CoordGroup.all) {
            const unconfirmed = group.coords.filter(c => !board.isConfirmed(c))
            if (unconfirmed.length < 2) continue

            for (
                let size = 2;
                size <= Math.min(5, unconfirmed.length);
                size++
            ) {
                for (const combo of this._generateCombinations(unconfirmed, size)) {
                    const allCands = new Set(
                        combo.flatMap(c => board.candidateValues(c))
                    )
                    if (allCands.size === size + 1) {
                        result.push(
                            ALSXZCandidateEliminator._Als(combo, allCands)
                        )
                    }
                }
            }
        }

        return result
    }

    private _deduplicateALS(
        alsList: ReturnType<typeof ALSXZCandidateEliminator._Als>[]
    ): ReturnType<typeof ALSXZCandidateEliminator._Als>[] {
        const seen = new Set<string>()
        const result: ReturnType<typeof ALSXZCandidateEliminator._Als>[] = []
        for (const als of alsList) {
            const key = als.coords.map(c => c.index).sort((a, b) => a - b).join(',')
            if (!seen.has(key)) {
                seen.add(key)
                result.push(als)
            }
        }
        return result
    }

    private _findRestrictedCommons(
        board: Board,
        als1: ReturnType<typeof ALSXZCandidateEliminator._Als>,
        als2: ReturnType<typeof ALSXZCandidateEliminator._Als>
    ): number[] {
        const shared = [...als1.candidates].filter(c => als2.candidates.has(c))
        const restricted: number[] = []

        for (const x of shared) {
            const cellsX1 = als1.coords.filter(c =>
                board.candidateValues(c).includes(x)
            )
            const cellsX2 = als2.coords.filter(c =>
                board.candidateValues(c).includes(x)
            )

            if (cellsX1.length === 0 || cellsX2.length === 0) continue

            const allSee = cellsX1.every(c1 =>
                cellsX2.every(c2 => this._seesEachOther(c1, c2))
            )

            if (allSee) restricted.push(x)
        }

        return restricted
    }

    private _applyType1Elimination(
        board: Board,
        als1: ReturnType<typeof ALSXZCandidateEliminator._Als>,
        als2: ReturnType<typeof ALSXZCandidateEliminator._Als>,
        z: number
    ): boolean {
        let anyUpdate = false

        const cellsZ1 = als1.coords.filter(c => board.candidateValues(c).includes(z))
        const cellsZ2 = als2.coords.filter(c => board.candidateValues(c).includes(z))

        if (cellsZ1.length === 0 || cellsZ2.length === 0) return false

        for (const coord of Coord.all) {
            if (als1.coordsSet.has(coord) || als2.coordsSet.has(coord)) continue
            if (board.isConfirmed(coord)) continue
            if (!board.candidateValues(coord).includes(z)) continue

            const seesAllZ =
                cellsZ1.every(zc => this._seesEachOther(coord, zc)) &&
                cellsZ2.every(zc => this._seesEachOther(coord, zc))

            if (seesAllZ) {
                if (board.eraseCandidateValue(coord, z)) anyUpdate = true
            }
        }

        return anyUpdate
    }

    private _generateCombinations<T>(list: T[], k: number): T[][] {
        if (k === 0) return [[]]
        if (list.length === 0 || k > list.length) return []
        const result: T[][] = []
        for (let i = 0; i < list.length; i++) {
            for (const rest of this._generateCombinations(list.slice(i + 1), k - 1)) {
                result.push([list[i], ...rest])
            }
        }
        return result
    }

    private _seesEachOther(a: Coord, b: Coord): boolean {
        return (
            a.row === b.row ||
            a.col === b.col ||
            (Math.floor(a.row / 3) === Math.floor(b.row / 3) &&
                Math.floor(a.col / 3) === Math.floor(b.col / 3))
        )
    }
}

// ---------------------------------------------------------------------------
// MutantFishCandidateEliminator
// ---------------------------------------------------------------------------

/**
 * Mutant Fish — the most advanced fish pattern where both base sets and
 * cover sets can be a MIX of rows, columns, and boxes.
 *
 * A fish of size N for candidate V has:
 * - N base sets (rows/cols/boxes) containing V positions
 * - N cover sets that also contain those V positions
 * - Every V position in a cover set must also be in a base set
 * - Eliminate V from cover-set positions NOT in any base set
 *
 * Reference: https://www.sudopedia.org/wiki/Mutant_Fish
 *
 * Equivalent to the Kotlin `MutantFishCandidateEliminator`.
 */
export class MutantFishCandidateEliminator implements CandidateEliminator {
    readonly displayName = 'Mutant Fish'

    eliminate(board: Board): boolean {
        let anyUpdate = false

        for (let v = 1; v <= SIZE; v++) {
            for (const size of [2, 3, 4]) {
                if (this._findMutantFish(board, v, size)) {
                    anyUpdate = true
                }
            }
        }

        return anyUpdate
    }

    /**
     * For a given candidate value and fish size, enumerate base-set
     * combinations and attempt to find valid mutant fish patterns.
     */
    private _findMutantFish(board: Board, value: number, size: number): boolean {
        const positions = findCandidatePositions(board, value)
        if (positions.length < size * 2) return false

        // Only consider houses with at least 2 candidate positions
        const candidateHouses = findHousesWithCandidates(positions, 2)
        if (candidateHouses.length < size) return false

        const baseCombos = generateCombinationsFH(candidateHouses, size)
        let anyUpdate = false

        for (const baseHouses of baseCombos) {
            if (this._tryFishPattern(board, value, baseHouses, positions)) {
                anyUpdate = true
            }
        }

        return anyUpdate
    }

    /**
     * Try a specific set of base houses as a mutant fish pattern.
     *
     * 1. Collect V positions in the base houses
     * 2. Derive cover sets: houses that contain those V positions
     * 3. Validate: exactly N cover sets, no V position escapes a base
     * 4. Eliminate V from cover positions outside all base sets
     */
    private _tryFishPattern(
        board: Board,
        value: number,
        baseHouses: readonly House[],
        allPositions: readonly Coord[],
    ): boolean {
        const baseSet = new Set(baseHouses.map(houseKey))

        // Find V positions in base houses
        const fishPositions = allPositions.filter((coord) =>
            baseHouses.some((h) => houseContains(h, coord)),
        )
        if (fishPositions.length === 0) return false

        // Derive cover sets: all houses that contain at least one fish position
        const coverHouses: House[] = []
        const seenCovers = new Set<string>()
        for (const house of allHouses) {
            const key = houseKey(house)
            if (seenCovers.has(key)) continue
            if (fishPositions.some((c) => houseContains(house, c))) {
                coverHouses.push(house)
                seenCovers.add(key)
            }
        }

        // Must have exactly N cover sets
        if (coverHouses.length !== baseHouses.length) return false

        // Validate: every V position in each cover house must be in a base house
        for (const coverHouse of coverHouses) {
            const vPositionsInCover = allPositions.filter((c) =>
                houseContains(coverHouse, c),
            )
            const escaped = vPositionsInCover.some((coord) =>
                baseHouses.every((h) => !houseContains(h, coord)),
            )
            if (escaped) return false
        }

        // Valid mutant fish — eliminate V from cover positions outside base sets
        let anyUpdate = false
        for (const coverHouse of coverHouses) {
            // Only iterate coords that are actually in this cover house
            for (const coord of coverHouse.coords) {
                if (
                    !board.isConfirmed(coord) &&
                    board.candidateValues(coord).includes(value) &&
                    baseHouses.every((h) => !houseContains(h, coord))
                ) {
                    if (board.eraseCandidateValue(coord, value)) {
                        anyUpdate = true
                    }
                }
            }
        }

        return anyUpdate
    }
}
