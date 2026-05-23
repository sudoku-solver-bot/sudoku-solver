/**
 * Board-level constants shared across the solver.
 * Mirrors the Kotlin `BoardSettings` object.
 */
export const SIZE = 9
export const REGION_SIZE = 3
export const CELL_COUNT = SIZE * SIZE // 81

/** Symbol for display: index = value (0 = empty → '.', 1-9 = '1'-'9'). */
export const SYMBOLS: readonly string[] = ['.', '1', '2', '3', '4', '5', '6', '7', '8', '9']

/** Bitmask for the wildcard (all candidates possible). */
export const WILDCARD_PATTERN = (1 << SIZE) - 1 // 0b111111111 = 511
