/**
 * Pure data model for a Sudoku coordinate (row, col) with pre-computed
 * linear index and region.
 *
 * Equivalent to the Kotlin `Coord` class.
 */
export class Coord {
    /** 0-based row index (0-8). */
    readonly row: number
    /** 0-based column index (0-8). */
    readonly col: number
    /** Linear index: row * 9 + col (0-80). */
    readonly index: number
    /** Region index 0-8 (left-to-right, top-to-bottom). */
    readonly region: number

    private constructor(row: number, col: number) {
        this.row = row
        this.col = col
        this.index = row * 9 + col
        const regionRow = Math.floor(row / 3)
        const regionCol = Math.floor(col / 3)
        this.region = regionRow * 3 + regionCol
    }

    // ---------------------------------------------------------------------------
    // Pre-computed instances
    // ---------------------------------------------------------------------------
    private static _all?: readonly Coord[]

    /** All 81 coordinates, pre-computed and accessed by index (0-80). */
    static get all(): readonly Coord[] {
        if (!Coord._all) {
            const result: Coord[] = []
            for (let row = 0; row < 9; row++) {
                for (let col = 0; col < 9; col++) {
                    result[row * 9 + col] = new Coord(row, col)
                }
            }
            Coord._all = Object.freeze(result)
        }
        return Coord._all
    }
}
