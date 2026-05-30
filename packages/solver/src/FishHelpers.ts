import type { Board } from './Board'
import { Coord } from './Coord'

// ---------------------------------------------------------------------------
// House — a row, column, or box (9-cell unit)
// ---------------------------------------------------------------------------

/** Represents a house: a row, column, or 3×3 box. */
export type House =
    | { kind: 'row'; index: number }
    | { kind: 'col'; index: number }
    | { kind: 'box'; index: number }

/** All 27 houses (9 rows + 9 columns + 9 boxes). */
export const allHouses: readonly House[] = (() => {
    const result: House[] = []
    for (let i = 0; i < 9; i++) {
        result.push({ kind: 'row', index: i })
        result.push({ kind: 'col', index: i })
        result.push({ kind: 'box', index: i })
    }
    return result
})()

/** Check whether a coordinate belongs to a house. */
export function houseContains(h: House, coord: Coord): boolean {
    switch (h.kind) {
        case 'row':
            return coord.row === h.index
        case 'col':
            return coord.col === h.index
        case 'box':
            return (
                Math.floor(coord.row / 3) === Math.floor(h.index / 3) &&
                Math.floor(coord.col / 3) === h.index % 3
            )
    }
}

/**
 * Return a stable string key for a house.
 * Example: "row:0", "col:3", "box:8".
 */
export function houseKey(h: House): string {
    return `${h.kind}:${h.index}`
}

// ---------------------------------------------------------------------------
// Candidate position helpers
// ---------------------------------------------------------------------------

/**
 * Find all coordinates where `value` is a candidate (not yet confirmed).
 * Returns the full Coord objects for ease of use.
 */
export function findCandidatePositions(board: Board, value: number): Coord[] {
    const result: Coord[] = []
    for (const coord of Coord.all) {
        if (!board.isConfirmed(coord) && board.candidateValues(coord).includes(value)) {
            result.push(coord)
        }
    }
    return result
}

/**
 * Find all houses that contain at least `minCount` positions among the
 * given coordinate list.
 */
export function findHousesWithCandidates(
    positions: readonly Coord[],
    minCount: number,
): House[] {
    return allHouses.filter((house) => {
        let count = 0
        for (const coord of positions) {
            if (houseContains(house, coord)) count++
            if (count >= minCount) return true
        }
        return false
    })
}

/**
 * Build a map from house key to the list of positions within that house.
 */
export function buildHousePositionMap(
    positions: readonly Coord[],
): Map<string, Coord[]> {
    const map = new Map<string, Coord[]>()
    for (const house of allHouses) {
        const inside = positions.filter((c) => houseContains(house, c))
        if (inside.length > 0) {
            map.set(houseKey(house), inside)
        }
    }
    return map
}

// ---------------------------------------------------------------------------
// Combinations — shared across fish eliminators
// ---------------------------------------------------------------------------

/** Generate all k-element combinations from `list`. */
export function generateCombinations<T>(list: readonly T[], k: number): T[][] {
    if (k === 0) return [[]]
    if (k > list.length) return []
    if (k === list.length) return [[...list]]

    const result: T[][] = []
    for (let i = 0; i < list.length; i++) {
        for (const rest of generateCombinations(list.slice(i + 1), k - 1)) {
            result.push([list[i], ...rest])
        }
    }
    return result
}
