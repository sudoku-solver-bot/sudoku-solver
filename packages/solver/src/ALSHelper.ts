import { Board } from './Board'
import { Coord } from './Coord'
import { CoordGroup } from './CoordGroup'

/**
 * Almost Locked Set (ALS): N cells with exactly N+1 candidates.
 *
 *ALSes are the building blocks for several advanced sudoku techniques
 * (ALS-XZ, Death Blossom, etc.). This module provides shared detection
 * and utility logic used by multiple eliminators.
 */
export interface ALS {
    cells: Coord[]
    candidates: Set<number>
    /** Pre-computed Set for O(1) membership checks. */
    cellsSet: Set<Coord>
}

/**
 * Find all Almost Locked Sets on the board.
 *
 * An ALS is a group of N cells (within the same row, column, or box)
 * that collectively contain exactly N+1 candidates.
 *
 * @param board   The board to scan
 * @param maxSize Maximum ALS size to search for (default 5)
 * @returns Array of all ALSes found across all groups
 */
export function findALSes(board: Board, maxSize: number = 5): ALS[] {
    const result: ALS[] = []

    for (const group of CoordGroup.all) {
        const unconfirmed = group.coords.filter(c => !board.isConfirmed(c))
        if (unconfirmed.length < 2) continue

        for (let size = 2; size <= Math.min(maxSize, unconfirmed.length); size++) {
            for (const combo of generateCombinations(unconfirmed, size)) {
                const allCands = new Set(combo.flatMap(c => board.candidateValues(c)))
                if (allCands.size === size + 1) {
                    result.push({
                        cells: combo,
                        candidates: allCands,
                        cellsSet: new Set(combo),
                    })
                }
            }
        }
    }

    return result
}

/**
 * Remove duplicate ALSes (same cell set, different order).
 */
export function deduplicateALS(alsList: ALS[]): ALS[] {
    const seen = new Set<string>()
    const result: ALS[] = []
    for (const als of alsList) {
        const key = als.cells.map(c => c.index).sort((a, b) => a - b).join(',')
        if (!seen.has(key)) {
            seen.add(key)
            result.push(als)
        }
    }
    return result
}

/**
 * Check whether two cells "see" each other (share a row, column, or box).
 */
export function seesEachOther(a: Coord, b: Coord): boolean {
    return (
        a.row === b.row ||
        a.col === b.col ||
        (Math.floor(a.row / 3) === Math.floor(b.row / 3) &&
            Math.floor(a.col / 3) === Math.floor(b.col / 3))
    )
}

/**
 * Generate all k-length combinations from a list.
 */
export function generateCombinations<T>(list: T[], k: number): T[][] {
    if (k === 0) return [[]]
    if (list.length === 0 || k > list.length) return []
    const result: T[][] = []
    for (let i = 0; i < list.length; i++) {
        for (const rest of generateCombinations(list.slice(i + 1), k - 1)) {
            result.push([list[i], ...rest])
        }
    }
    return result
}
