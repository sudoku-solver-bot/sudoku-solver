import { Coord } from './Coord'
import { CoordGroup } from './CoordGroup'
import { SIZE, SYMBOLS, WILDCARD_PATTERN } from './BoardSettings'

/**
 * Bit masks for individual values 1-9.
 *
 * masks[0] = 0b000000001 (value 1)
 * masks[1] = 0b000000010 (value 2)
 * ...
 * masks[8] = 0b100000000 (value 9)
 */
const MASKS: readonly number[] = Object.freeze(Array.from({ length: SIZE }, (_, i) => 1 << i))

/**
 * Represents a Sudoku puzzle board with 81 cells arranged in a 9×9 grid.
 *
 * Uses a bitmask-based candidate representation:
 * - Each cell's candidate pattern is a 9-bit integer.
 * - Bit i (0-indexed) corresponds to value i+1.
 * - A confirmed cell has exactly one bit set.
 * - An empty/unknown cell has all 9 bits set (WILDCARD_PATTERN = 511).
 *
 * Equivalent to the Kotlin `Board` class.
 */
export class Board {
    /** 81 candidate patterns, one per cell. */
    readonly candidatePatterns: Int32Array

    /**
     * @param candidatePatterns 81-element array of bitmask patterns.
     * @throws If array length is not 81.
     */
    constructor(candidatePatterns: Int32Array | number[] | Uint16Array) {
        if (candidatePatterns.length !== 81) {
            throw new Error(`Expected 81 cells, got ${candidatePatterns.length}`)
        }
        this.candidatePatterns =
            candidatePatterns instanceof Int32Array
                ? candidatePatterns
                : new Int32Array(candidatePatterns)
    }

    // ---------------------------------------------------------------------------
    // Construction helpers
    // ---------------------------------------------------------------------------

    /** Create a board from an 81-element array of placed values (0 = empty). */
    static fromValues(values: readonly number[]): Board {
        const patterns = new Int32Array(81)
        for (let i = 0; i < 81; i++) {
            const v = values[i]
            patterns[i] = v === 0 ? WILDCARD_PATTERN : MASKS[v - 1]
        }
        return new Board(patterns)
    }

    /** Create an empty board (all cells are wildcards). */
    static empty(): Board {
        return new Board(new Int32Array(81).fill(WILDCARD_PATTERN))
    }

    // ---------------------------------------------------------------------------
    // Equality & copy
    // ---------------------------------------------------------------------------

    equals(other: Board): boolean {
        for (let i = 0; i < 81; i++) {
            if (this.candidatePatterns[i] !== other.candidatePatterns[i]) return false
        }
        return true
    }

    copy(): Board {
        return new Board(new Int32Array(this.candidatePatterns))
    }

    // ---------------------------------------------------------------------------
    // Status checking
    // ---------------------------------------------------------------------------

    /** Display symbol for a cell ('.' for empty, '1'-'9' for confirmed). */
    symbolAt(coord: Coord): string {
        return SYMBOLS[this.value(coord)]
    }

    /** True if exactly one candidate value remains. */
    isConfirmed(coord: Coord): boolean {
        return bitCount(this.candidatePatterns[coord.index]) === 1
    }

    /** True if the board is valid (no contradictions). */
    isValid(): boolean {
        // No cell should have zero candidates.
        for (let i = 0; i < 81; i++) {
            if (this.candidatePatterns[i] === 0) return false
        }
        // No group should contain duplicate confirmed values.
        for (const group of CoordGroup.all) {
            const seen = new Set<number>()
            for (const c of group.coords) {
                if (this.isConfirmed(c)) {
                    const pat = this.candidatePatterns[c.index]
                    if (seen.has(pat)) return false
                    seen.add(pat)
                }
            }
        }
        return true
    }

    /** True if all 81 cells are confirmed. */
    isSolved(): boolean {
        for (let i = 0; i < 81; i++) {
            if (bitCount(this.candidatePatterns[i]) !== 1) return false
        }
        return true
    }

    // ---------------------------------------------------------------------------
    // Candidate pattern access
    // ---------------------------------------------------------------------------

    /** Raw bitmask pattern for a cell. */
    candidatePattern(coord: Coord): number {
        return this.candidatePatterns[coord.index]
    }

    /** Array of possible values (1-9) for a cell. */
    candidateValues(coord: Coord): number[] {
        const pat = this.candidatePatterns[coord.index]
        const result: number[] = []
        for (let i = 0; i < SIZE; i++) {
            if (pat & MASKS[i]) result.push(i + 1)
        }
        return result
    }

    // ---------------------------------------------------------------------------
    // Candidate manipulation
    // ---------------------------------------------------------------------------

    /** Erase a candidate pattern from a cell. Returns true if changed. */
    eraseCandidatePattern(coord: Coord, pattern: number): boolean {
        const idx = coord.index
        const old = this.candidatePatterns[idx]
        this.candidatePatterns[idx] = old & ~pattern
        return old !== this.candidatePatterns[idx]
    }

    /** Erase candidate value (1-9) from a cell. Returns true if changed. */
    eraseCandidateValue(coord: Coord, value: number): boolean {
        return this.eraseCandidatePattern(coord, MASKS[value - 1])
    }

    // ---------------------------------------------------------------------------
    // Confirmed value manipulation
    // ---------------------------------------------------------------------------

    /** Get the confirmed value (1-9), or 0 if not confirmed. */
    value(coord: Coord): number {
        const pat = this.candidatePatterns[coord.index]
        for (let i = 0; i < SIZE; i++) {
            if (pat === MASKS[i]) return i + 1
        }
        return 0
    }

    /** Set a confirmed value (1-9) for a cell. */
    markValue(coord: Coord, value: number): void {
        this.candidatePatterns[coord.index] = MASKS[value - 1]
    }

    // ---------------------------------------------------------------------------
    // Solver helpers
    // ---------------------------------------------------------------------------

    /**
     * Find the unresolved cell with the fewest candidates (MRV heuristic).
     * Returns undefined if solved.
     */
    unresolvedCoord(): Coord | undefined {
        let best: Coord | undefined
        let bestCount = SIZE + 1
        for (const c of Coord.all) {
            if (this.isConfirmed(c)) continue
            const count = bitCount(this.candidatePatterns[c.index])
            if (count < bestCount) {
                bestCount = count
                best = c
                if (count === 2) break // cannot get better than 2 candidates
            }
        }
        return best
    }

    /** Total number of candidates across all cells. */
    countTotalCandidates(): number {
        let total = 0
        for (let i = 0; i < 81; i++) {
            total += bitCount(this.candidatePatterns[i])
        }
        return total
    }

    // ---------------------------------------------------------------------------
    // Display
    // ---------------------------------------------------------------------------

    toString(): string {
        const lines: string[] = []
        for (let regionRow = 0; regionRow < 3; regionRow++) {
            for (let rowInRegion = 0; rowInRegion < 3; rowInRegion++) {
                const row = regionRow * 3 + rowInRegion
                const parts: string[] = []
                for (let regionCol = 0; regionCol < 3; regionCol++) {
                    const cells: string[] = []
                    for (let colInRegion = 0; colInRegion < 3; colInRegion++) {
                        const col = regionCol * 3 + colInRegion
                        cells.push(this.symbolAt(Coord.all[row * 9 + col]))
                    }
                    parts.push(cells.join(''))
                }
                lines.push(parts.join('|'))
            }
            if (regionRow < 2) lines.push('---+---+---')
        }
        return lines.join('\n')
    }
}

// ---------------------------------------------------------------------------
// Helpers
// ---------------------------------------------------------------------------

/** Count set bits (popcount) in a 32-bit integer. */
function bitCount(n: number): number {
    n = n - ((n >>> 1) & 0x55555555)
    n = (n & 0x33333333) + ((n >>> 2) & 0x33333333)
    n = (n + (n >>> 4)) & 0x0f0f0f0f
    return (n * 0x01010101) >>> 24
}
