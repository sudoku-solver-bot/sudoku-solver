import type { Board } from '../Board'
import { CoordGroup } from '../CoordGroup'
import type { Hint, TechniqueDetector } from '../HintTypes'
import { Technique } from '../HintTypes'

/**
 * Detects naked pairs — two cells in a group with the same two candidates.
 *
 * When two cells in a group (row, column, or box) contain exactly the same
 * two candidate values, those values can be eliminated from all other cells
 * in that group.
 */
export class NakedPairDetector implements TechniqueDetector {
    readonly technique = Technique.NAKED_PAIR

    detect(board: Board): Hint | null {
        for (const coordGroup of CoordGroup.all) {
            const pairCells = coordGroup.coords.filter(coord =>
                !board.isConfirmed(coord) &&
                board.candidateValues(coord).length === 2
            )

            for (let i = 0; i < pairCells.length; i++) {
                for (let j = i + 1; j < pairCells.length; j++) {
                    const coord1 = pairCells[i]
                    const coord2 = pairCells[j]
                    const candidates1 = new Set(board.candidateValues(coord1))
                    const candidates2 = new Set(board.candidateValues(coord2))

                    if (candidates1.size === 2 && candidates2.size === 2 &&
                        candidates1.size === candidates2.size &&
                        [...candidates1].every(v => candidates2.has(v))) {
                        const values = [...candidates1]
                        return {
                            coord: coord1,
                            value: values[0],
                            technique: Technique.NAKED_PAIR,
                            explanation: `Cells (${coord1.row + 1},${coord1.col + 1}) and (${coord2.row + 1},${coord2.col + 1}) ` +
                                `form a naked pair with values ${values[0]} and ${values[1]}. ` +
                                `These values can be eliminated from other cells in this group.`
                        }
                    }
                }
            }
        }
        return null
    }
}
