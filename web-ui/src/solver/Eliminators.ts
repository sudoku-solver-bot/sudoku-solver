import type { Board } from './Board'
import { CoordGroup } from './CoordGroup'
import { bitCount, MASKS, WILDCARD_PATTERN } from './Bitmask'

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
