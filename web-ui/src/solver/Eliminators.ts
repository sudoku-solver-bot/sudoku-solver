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
