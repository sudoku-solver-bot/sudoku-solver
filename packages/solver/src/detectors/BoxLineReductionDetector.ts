import type { Board } from '../Board'
import { Coord } from '../Coord'
import type { Hint, TechniqueDetector } from '../HintTypes'
import { Technique } from '../HintTypes'

/**
 * Detects box/line reductions — candidates restricted to one box in a row or column.
 *
 * When a candidate value in a row (or column) is confined to a single box,
 * that value can be eliminated from the rest of that box.
 */
export class BoxLineReductionDetector implements TechniqueDetector {
    readonly technique = Technique.BOX_LINE_REDUCTION

    detect(board: Board): Hint | null {
        // Check rows for box/line reduction
        for (let row = 0; row < 9; row++) {
            for (let value = 1; value <= 9; value++) {
                const cells: Coord[] = []
                for (let col = 0; col < 9; col++) {
                    const coord = Coord.all[row * 9 + col]
                    if (!board.isConfirmed(coord) &&
                        board.candidateValues(coord).includes(value)) {
                        cells.push(coord)
                    }
                }
                if (cells.length >= 2 && cells.length <= 3) {
                    const boxes = new Set(cells.map(c => `${Math.floor(c.row / 3)},${Math.floor(c.col / 3)}`))
                    if (boxes.size === 1) {
                        const [boxRow, boxCol] = boxes.values().next().value!.split(',').map(Number)
                        for (let r = boxRow * 3; r < boxRow * 3 + 3; r++) {
                            if (r === row) continue
                            for (let c = boxCol * 3; c < boxCol * 3 + 3; c++) {
                                const coord = Coord.all[r * 9 + c]
                                if (!board.isConfirmed(coord) &&
                                    board.candidateValues(coord).includes(value)) {
                                    return {
                                        coord,
                                        value,
                                        technique: Technique.BOX_LINE_REDUCTION,
                                        explanation: `Value ${value} in row ${row + 1} ` +
                                            `is restricted to box (${boxRow + 1},${boxCol + 1}). ` +
                                            `Eliminate ${value} from other rows in this box.`
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Check columns for box/line reduction
        for (let col = 0; col < 9; col++) {
            for (let value = 1; value <= 9; value++) {
                const cells: Coord[] = []
                for (let row = 0; row < 9; row++) {
                    const coord = Coord.all[row * 9 + col]
                    if (!board.isConfirmed(coord) &&
                        board.candidateValues(coord).includes(value)) {
                        cells.push(coord)
                    }
                }
                if (cells.length >= 2 && cells.length <= 3) {
                    const boxes = new Set(cells.map(c => `${Math.floor(c.row / 3)},${Math.floor(c.col / 3)}`))
                    if (boxes.size === 1) {
                        const [boxRow, boxCol] = boxes.values().next().value!.split(',').map(Number)
                        for (let c = boxCol * 3; c < boxCol * 3 + 3; c++) {
                            if (c === col) continue
                            for (let r = boxRow * 3; r < boxRow * 3 + 3; r++) {
                                const coord = Coord.all[r * 9 + c]
                                if (!board.isConfirmed(coord) &&
                                    board.candidateValues(coord).includes(value)) {
                                    return {
                                        coord,
                                        value,
                                        technique: Technique.BOX_LINE_REDUCTION,
                                        explanation: `Value ${value} in column ${col + 1} ` +
                                            `is restricted to box (${boxRow + 1},${boxCol + 1}). ` +
                                            `Eliminate ${value} from other columns in this box.`
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
