import type { Board } from '../Board'
import { CoordGroup } from '../CoordGroup'
import type { Hint, TechniqueDetector } from '../HintTypes'
import { Technique } from '../HintTypes'

/**
 * Detects hidden pairs — two values that appear only in the same two cells within a group.
 *
 * When two candidate values in a group are confined to exactly the same two cells,
 * all other candidates can be eliminated from those two cells.
 */
export class HiddenPairDetector implements TechniqueDetector {
    readonly technique = Technique.HIDDEN_PAIR

    detect(board: Board): Hint | null {
        for (const coordGroup of CoordGroup.all) {
            const valueToCells = new Map<number, Set<typeof coordGroup.coords[0]>>()
            for (let value = 1; value <= 9; value++) {
                const cells = new Set<typeof coordGroup.coords[0]>()
                for (const coord of coordGroup.coords) {
                    if (!board.isConfirmed(coord) &&
                        board.candidateValues(coord).includes(value)) {
                        cells.add(coord)
                    }
                }
                if (cells.size === 2) {
                    valueToCells.set(value, cells)
                }
            }

            const values = [...valueToCells.keys()]
            for (let i = 0; i < values.length; i++) {
                for (let j = i + 1; j < values.length; j++) {
                    const v1 = values[i]
                    const v2 = values[j]
                    const cells1 = valueToCells.get(v1)!
                    const cells2 = valueToCells.get(v2)!

                    if (cells1.size === 2 && cells2.size === 2 &&
                        [...cells1].every(c => cells2.has(c)) &&
                        [...cells2].every(c => cells1.has(c))) {
                        const cells = [...cells1]
                        const extraCandidates1 = board.candidateValues(cells[0]).filter(v => v !== v1 && v !== v2)
                        const extraCandidates2 = board.candidateValues(cells[1]).filter(v => v !== v1 && v !== v2)

                        if (extraCandidates1.length > 0) {
                            return {
                                coord: cells[0],
                                value: extraCandidates1[0],
                                technique: Technique.HIDDEN_PAIR,
                                explanation: `Values ${v1} and ${v2} form a hidden pair in cells ` +
                                    `(${cells[0].row + 1},${cells[0].col + 1}) and ` +
                                    `(${cells[1].row + 1},${cells[1].col + 1}). ` +
                                    `Other candidates can be eliminated from these cells.`
                            }
                        }
                        if (extraCandidates2.length > 0) {
                            return {
                                coord: cells[1],
                                value: extraCandidates2[0],
                                technique: Technique.HIDDEN_PAIR,
                                explanation: `Values ${v1} and ${v2} form a hidden pair in cells ` +
                                    `(${cells[0].row + 1},${cells[0].col + 1}) and ` +
                                    `(${cells[1].row + 1},${cells[1].col + 1}). ` +
                                    `Other candidates can be eliminated from these cells.`
                            }
                        }
                    }
                }
            }
        }
        return null
    }
}
