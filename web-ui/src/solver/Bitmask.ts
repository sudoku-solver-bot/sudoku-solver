/** Board size (9×9 standard Sudoku). */
export const SIZE = 9

/** Region size (3×3 subgrid). */
export const REGION_SIZE = 3

/** Total cell count. */
export const CELL_COUNT = SIZE * SIZE

/** Display symbols: index 0 = '.', 1-9 = '1'-'9'. */
export const SYMBOLS: readonly string[] = ['.', '1', '2', '3', '4', '5', '6', '7', '8', '9']

/**
 * Bit masks for individual values 1-9.
 *
 * masks[0] = 0b000000001 (value 1)
 * masks[1] = 0b000000010 (value 2)
 * ...
 * masks[8] = 0b100000000 (value 9)
 */
export const MASKS: readonly number[] = Object.freeze(
    Array.from({ length: SIZE }, (_, i) => 1 << i)
)

/** Bitmask with all 9 bits set (= 511, wildcard / empty cell). */
export const WILDCARD_PATTERN = (1 << SIZE) - 1

// ---------------------------------------------------------------------------
// Bit operations
// ---------------------------------------------------------------------------

/**
 * Count the number of set bits (popcount) in a 32-bit integer.
 * Uses the SWAR (SIMD Within A Register) technique for efficiency.
 */
export function bitCount(n: number): number {
    n = n - ((n >>> 1) & 0x55555555)
    n = (n & 0x33333333) + ((n >>> 2) & 0x33333333)
    n = (n + (n >>> 4)) & 0x0f0f0f0f
    return (n * 0x01010101) >>> 24
}

/** Test whether a specific value (1-9) is present in a bitmask. */
export function hasValue(mask: number, value: number): boolean {
    return (mask & MASKS[value - 1]) !== 0
}

/** Get the lowest set value (1-9) from a bitmask, or 0 if mask is empty. */
export function lowestValue(mask: number): number {
    if (mask === 0) return 0
    // Count trailing zeros (CTZ)
    let c = 0
    while ((mask & 1) === 0) { mask >>>= 1; c++ }
    return c + 1
}

/**
 * Extract all candidate values (1-9) from a bitmask.
 * Returns a sorted array of values.
 */
export function maskToValues(mask: number): number[] {
    const result: number[] = []
    for (let i = 0; i < SIZE; i++) {
        if (mask & MASKS[i]) result.push(i + 1)
    }
    return result
}

/**
 * Convert a value (1-9) to its singleton bitmask.
 * Returns 0 for value 0.
 */
export function valueToMask(value: number): number {
    return value === 0 ? 0 : MASKS[value - 1]
}
