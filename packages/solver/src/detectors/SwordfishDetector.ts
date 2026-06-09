import type { Board } from '../Board'
import { Coord } from '../Coord'
import { FishCandidateEliminator } from '../Eliminators'
import type { Hint, TechniqueDetector } from '../HintTypes'
import { Technique } from '../HintTypes'

/**
 * Detects Swordfish patterns — candidate in 3 rows restricted to same 3 columns (or vice versa).
 *
 * Uses the existing FishCandidateEliminator(3) to find eliminations,
 * then reports the first affected cell as a hint.
 */
export class SwordfishDetector implements TechniqueDetector {
    readonly technique = Technique.SWORDFISH

    detect(board: Board): Hint | null {
        const testBoard = board.copy()
        const eliminator = new FishCandidateEliminator([3])
        const changed = eliminator.eliminate(testBoard)
        if (!changed) return null

        // Find a cell that was affected
        for (const coord of Coord.all) {
            if (board.isConfirmed(coord) || testBoard.isConfirmed(coord)) continue
            const before = board.candidateValues(coord)
            const after = testBoard.candidateValues(coord)
            const eliminated = before.filter(v => !after.includes(v))
            if (eliminated.length > 0) {
                return {
                    coord,
                    value: eliminated[0],
                    technique: Technique.SWORDFISH,
                    explanation: `Swordfish pattern allows eliminating ${eliminated.join(', ')} from cell (${coord.row + 1},${coord.col + 1}).`
                }
            }
        }
        return null
    }
}
