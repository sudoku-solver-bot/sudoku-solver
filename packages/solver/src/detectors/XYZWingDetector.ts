import type { Board } from '../Board'
import { Coord } from '../Coord'
import { XYZWingCandidateEliminator } from '../Eliminators'
import type { Hint, TechniqueDetector } from '../HintTypes'
import { Technique } from '../HintTypes'

/**
 * Detects XYZ-Wing patterns — pivot {X,Y,Z}, Wing1 {X,Z}, Wing2 {Y,Z}, eliminate Z from cells seeing all three.
 *
 * Uses the existing XYZWingCandidateEliminator to find eliminations,
 * then reports the first affected cell as a hint.
 */
export class XYZWingDetector implements TechniqueDetector {
    readonly technique = Technique.XYZ_WING

    detect(board: Board): Hint | null {
        const testBoard = board.copy()
        const eliminator = new XYZWingCandidateEliminator()
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
                    technique: Technique.XYZ_WING,
                    explanation: `XYZ-Wing pattern allows eliminating ${eliminated.join(', ')} from cell (${coord.row + 1},${coord.col + 1}).`
                }
            }
        }
        return null
    }
}
