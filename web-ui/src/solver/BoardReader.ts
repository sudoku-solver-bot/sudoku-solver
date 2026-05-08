import { WILDCARD_PATTERN, valueToMask } from './Bitmask'
import type { Board } from './Board'

/**
 * Parses puzzle strings in various formats into a Board.
 *
 * Supported formats:
 * - 81-character string: '1', '2', ..., '9' = given values; '.' or '0' = empty
 * - 81-element number array: 1-9 = given values; 0 = empty
 * - Multiple lines (newlines are stripped)
 *
 * Equivalent to the Kotlin `Board(values: IntArray)` factory.
 */
export class BoardReader {
    /**
     * Parse an 81-character string into a Board.
     *
     * @param input 81-character puzzle string. Supports '.' and '0' for empty cells.
     * @param BoardClass The Board constructor (avoids circular dependency).
     * @throws If input length is not 81.
     */
    static fromString(input: string, BoardClass: { fromValues(values: readonly number[]): Board }): Board {
        // Strip whitespace and handle multi-line input
        const cleaned = input.replace(/\s/g, '')
        if (cleaned.length !== 81) {
            throw new Error(`Invalid puzzle: expected 81 characters, got ${cleaned.length}`)
        }

        const values: number[] = []
        for (let i = 0; i < 81; i++) {
            const ch = cleaned[i]
            if (ch === '.' || ch === '0') {
                values.push(0)
            } else {
                const n = parseInt(ch, 10)
                if (isNaN(n) || n < 1 || n > 9) {
                    throw new Error(`Invalid character at position ${i}: '${ch}'`)
                }
                values.push(n)
            }
        }

        return BoardClass.fromValues(values)
    }

    /**
     * Parse a puzzle string and return the raw Int32Array of candidate patterns
     * (without creating a Board). Useful when you need the patterns array directly.
     */
    static toPatterns(input: string): Int32Array {
        const cleaned = input.replace(/\s/g, '')
        if (cleaned.length !== 81) {
            throw new Error(`Invalid puzzle: expected 81 characters, got ${cleaned.length}`)
        }

        const patterns = new Int32Array(81)
        for (let i = 0; i < 81; i++) {
            const ch = cleaned[i]
            if (ch === '.' || ch === '0') {
                patterns[i] = WILDCARD_PATTERN
            } else {
                const n = parseInt(ch, 10)
                if (isNaN(n) || n < 1 || n > 9) {
                    throw new Error(`Invalid character at position ${i}: '${ch}'`)
                }
                patterns[i] = valueToMask(n)
            }
        }
        return patterns
    }
}
