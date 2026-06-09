import type { Board } from '../Board'
import { Coord } from '../Coord'
import type { Hint, TechniqueDetector } from '../HintTypes'
import { Technique } from '../HintTypes'

/**
 * Detects pointing pairs/triples — candidates restricted to one row or column within a box.
 *
 * When a candidate value in a box is confined to a single row (or column),
 * that value can be eliminated from the rest of that row (or column) outside the box.
 */
export class PointingPairDetector implements TechniqueDetector {
    readonly technique = Technique.POINTING_PAIR

    detect(board: Board): Hint | null {
        for (let boxRow = 0; boxRow < 3; boxRow++) {
            for (let boxCol = 0; boxCol < 3; boxCol++) {
                for (let value = 1; value <= 9; value++) {
                    const cells: Coord[] = []
                    for (let r = boxRow * 3; r < boxRow * 3 + 3; r++) {
                        for (let c = boxCol * 3; c < boxCol * 3 + 3; c++) {
                            const coord = Coord.all[r * 9 + c]
                            if (!board.isConfirmed(coord) &&
                                board.candidateValues(coord).includes(value)) {
                                cells.push(coord)
                            }
                        }
                    }
                    if (cells.length >= 2 && cells.length <= 3) {
                        // Check if all cells are in the same row
                        const rows = new Set(cells.map(c => c.row))
                        if (rows.size === 1) {
                            const row = cells[0].row
                            for (let col = 0; col < 9; col++) {
                                if (Math.floor(col / 3) === boxCol) continue
                                const coord = Coord.all[row * 9 + col]
                                if (!board.isConfirmed(coord) &&
                                    board.candidateValues(coord).includes(value)) {
                                    return {
                                        coord,
                                        value,
                                        technique: Technique.POINTING_PAIR,
                                        explanation: `Value ${value} in box (${boxRow + 1},${boxCol + 1}) ` +
                                            `is restricted to row ${row + 1}. ` +
                                            `Eliminate ${value} from row ${row + 1} in other boxes.`
                                    }
                                }
                            }
                        }
                        // Check if all cells are in the same column
                        const cols = new Set(cells.map(c => c.col))
                        if (cols.size === 1) {
                            const col = cells[0].col
                            for (let row = 0; row < 9; row++) {
                                if (Math.floor(row / 3) === boxRow) continue
                                const coord = Coord.all[row * 9 + col]
                                if (!board.isConfirmed(coord) &&
                                    board.candidateValues(coord).includes(value)) {
                                    return {
                                        coord,
                                        value,
                                        technique: Technique.POINTING_PAIR,
                                        explanation: `Value ${value} in box (${boxRow + 1},${boxCol + 1}) ` +
                                            `is restricted to column ${col + 1}. ` +
                                            `Eliminate ${value} from column ${col + 1} in other boxes.`
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return null
    }
}
