import type { Board } from '../Board'
import { CoordGroup } from '../CoordGroup'
import type { Hint, TechniqueDetector } from '../HintTypes'
import { Technique } from '../HintTypes'

/**
 * Detects naked triples — three cells in a group whose candidates are a subset of three values.
 *
 * When three cells in a group contain candidates that are all subsets of
 * the same three values, those values can be eliminated from all other cells
 * in that group.
 */
export class NakedTripleDetector implements TechniqueDetector {
    readonly technique = Technique.NAKED_TRIPLE

    detect(board: Board): Hint | null {
        for (const coordGroup of CoordGroup.all) {
            const unresolved = coordGroup.coords.filter(c =>
                !board.isConfirmed(c) &&
                board.candidateValues(c).length >= 2 &&
                board.candidateValues(c).length <= 3
            )

            if (unresolved.length < 3) continue

            for (let i = 0; i < unresolved.length - 2; i++) {
                for (let j = i + 1; j < unresolved.length - 1; j++) {
                    for (let k = j + 1; k < unresolved.length; k++) {
                        const candidates1 = new Set(board.candidateValues(unresolved[i]))
                        const candidates2 = new Set(board.candidateValues(unresolved[j]))
                        const candidates3 = new Set(board.candidateValues(unresolved[k]))
                        const union = new Set([...candidates1, ...candidates2, ...candidates3])

                        if (union.size === 3) {
                            for (const other of unresolved) {
                                if (other === unresolved[i] || other === unresolved[j] || other === unresolved[k]) continue
                                const otherCandidates = board.candidateValues(other)
                                const overlap = otherCandidates.filter(v => union.has(v))
                                if (overlap.length > 0) {
                                    return {
                                        coord: other,
                                        value: overlap[0],
                                        technique: Technique.NAKED_TRIPLE,
                                        explanation: `Cells (${unresolved[i].row + 1},${unresolved[i].col + 1}), ` +
                                            `(${unresolved[j].row + 1},${unresolved[j].col + 1}), and ` +
                                            `(${unresolved[k].row + 1},${unresolved[k].col + 1}) ` +
                                            `form a naked triple. ` +
                                            `Eliminate these values from other cells in this group.`
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
