import { Coord } from './Coord'
import { SIZE, REGION_SIZE } from './Bitmask'

/**
 * Represents a group of coordinates (row, column, or 3×3 region).
 *
 * CoordGroups are used by the solver's constraint propagation to validate
 * that no group contains duplicate values or invalid cell arrangements.
 *
 * Equivalent to the Kotlin `CoordGroup` class.
 */
export class CoordGroup {
    /** The list of coordinates in this group. */
    readonly coords: readonly Coord[]

    constructor(coords: readonly Coord[]) {
        this.coords = coords
    }

    // ---------------------------------------------------------------------------
    // Pre-computed groups
    // ---------------------------------------------------------------------------

    private static _vertical?: readonly CoordGroup[]
    private static _horizontal?: readonly CoordGroup[]
    private static _region?: readonly CoordGroup[]
    private static _all?: readonly CoordGroup[]

    /** Column groups (9 groups, one per column). */
    static get vertical(): readonly CoordGroup[] {
        if (!CoordGroup._vertical) {
            CoordGroup._vertical = Object.freeze(
                Array.from({ length: SIZE }, (_, col) =>
                    new CoordGroup(Array.from({ length: SIZE }, (_, row) => Coord.all[row * SIZE + col]))
                )
            )
        }
        return CoordGroup._vertical
    }

    /** Row groups (9 groups, one per row). */
    static get horizontal(): readonly CoordGroup[] {
        if (!CoordGroup._horizontal) {
            CoordGroup._horizontal = Object.freeze(
                Array.from({ length: SIZE }, (_, row) =>
                    new CoordGroup(Array.from({ length: SIZE }, (_, col) => Coord.all[row * SIZE + col]))
                )
            )
        }
        return CoordGroup._horizontal
    }

    /** 3×3 region groups (9 groups). */
    static get region(): readonly CoordGroup[] {
        if (!CoordGroup._region) {
            CoordGroup._region = Object.freeze(
                Array.from({ length: SIZE }, (_, ri) => {
                    const regionRow = Math.floor(ri / REGION_SIZE)
                    const regionCol = ri % REGION_SIZE
                    const rowStart = regionRow * REGION_SIZE
                    const colStart = regionCol * REGION_SIZE
                    const coords: Coord[] = []
                    for (let r = rowStart; r < rowStart + REGION_SIZE; r++) {
                        for (let c = colStart; c < colStart + REGION_SIZE; c++) {
                            coords.push(Coord.all[r * SIZE + c])
                        }
                    }
                    return new CoordGroup(coords)
                })
            )
        }
        return CoordGroup._region
    }

    /** All 27 groups: vertical + horizontal + region. */
    static get all(): readonly CoordGroup[] {
        if (!CoordGroup._all) {
            CoordGroup._all = Object.freeze([
                ...CoordGroup.vertical,
                ...CoordGroup.horizontal,
                ...CoordGroup.region,
            ])
        }
        return CoordGroup._all
    }

    // ---------------------------------------------------------------------------
    // Factory methods
    // ---------------------------------------------------------------------------

    /** The column group containing `coord`. */
    static verticalOf(coord: Coord): CoordGroup {
        return CoordGroup.vertical[coord.col]
    }

    /** The row group containing `coord`. */
    static horizontalOf(coord: Coord): CoordGroup {
        return CoordGroup.horizontal[coord.row]
    }

    /** The 3×3 region group containing `coord`. */
    static regionOf(coord: Coord): CoordGroup {
        return CoordGroup.region[coord.region]
    }

    /** All three groups (vertical, horizontal, region) that contain `coord`. */
    static of(coord: Coord): readonly CoordGroup[] {
        return [CoordGroup.verticalOf(coord), CoordGroup.horizontalOf(coord), CoordGroup.regionOf(coord)]
    }

    /**
     * Creates a CoordGroup for a rectangular range of cells.
     *
     * @param rows Array of row indices (inclusive).
     * @param cols Array of column indices (inclusive).
     */
    static fromRanges(rows: readonly number[], cols: readonly number[]): CoordGroup {
        const coords: Coord[] = []
        for (const row of rows) {
            for (const col of cols) {
                coords.push(Coord.all[row * SIZE + col])
            }
        }
        return new CoordGroup(coords)
    }
}
