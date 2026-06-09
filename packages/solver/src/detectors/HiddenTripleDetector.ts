import type { Board } from '../Board'
import { CoordGroup } from '../CoordGroup'
import type { Hint, TechniqueDetector } from '../HintTypes'
import { Technique } from '../HintTypes'

/**
 * Detects hidden triples — three values that appear only in the same three cells within a group.
 *
 * When three candidate values in a group are confined to exactly the same three cells,
 * all other candidates can be eliminated from those three cells.
 */
export class HiddenTripleDetector implements TechniqueDetector {
    readonly technique = Technique.HIDDEN_TRIPLE

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
                if (cells.size >= 2 && cells.size <= 3) {
                    valueToCells.set(value, cells)
                }
            }

            const values = [...valueToCells.keys()]
            if (values.length < 3) continue
            for (let i = 0; i < values.length - 2; i++) {
                for (let j = i + 1; j < values.length - 1; j++) {
                    for (let k = j + 1; k < values.length; k++) {
                        const v1 = values[i]
                        const v2 = values[j]
                        const v3 = values[k]
                        const union = new Set([
                            ...valueToCells.get(v1)!,
                            ...valueToCells.get(v2)!,
                            ...valueToCells.get(v3)!
                        ])

                        if (union.size === 3) {
                            const cells = [...union]
                            for (const cell of cells) {
                                const extraCandidates = board.candidateValues(cell)
                                    .filter(v => v !== v1 && v !== v2 && v !== v3)
                                if (extraCandidates.length > 0) {
                                    return {
                                        coord: cell,
                                        value: extraCandidates[0],
                                        technique: Technique.HIDDEN_TRIPLE,
                                        explanation: `Values ${v1}, ${v2}, and ${v3} form a hidden triple. ` +
                                            `Other candidates can be eliminated from these cells.`
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
