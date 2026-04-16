package will.sudoku.web

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import will.sudoku.solver.Board
import will.sudoku.solver.BoardReader
import will.sudoku.solver.SimpleCandidateEliminator
import will.sudoku.solver.Coord
import java.time.LocalDate
import java.time.ZoneOffset

@Serializable
data class DailyChallenge(
    val date: String,
    val puzzle: String,
    val difficulty: String,
    val beltEmoji: String,
    val beltName: String,
    val candidates: Map<String, List<Int>>
)

@Serializable
data class DailyStats(
    val date: String,
    val solved: Boolean = false,
    val timeSeconds: Int = 0,
    val hintsUsed: Int = 0,
    val streak: Int = 0
)

fun Route.dailyChallengeRoutes() {
    // Deterministic puzzle generation based on date
    // Uses a curated set of puzzles rotated by day-of-year
    val dailyPuzzles = listOf(
        // Easy
        "2...7..38.....6.7.3...4.6....8.2.7..1.......6..7.3.4....4.8...9.6.4.....91..6...2" to "easy",
        "4......38..2..41....53..24..7.6.9..4.2.....7.6..7.3.9..57..83....39..4..24......9" to "medium",
        "1.....5694.2.....8.5...9.4....64.8.1....1....2.8.35....4.5...1.9.....4.2621.....5" to "hard",
        ".........9.46.7....768.41..3.97.1.8...8...3...5.3.87.2..75.261....4.32.8........." to "medium",
        ".1.9.36......8....9.....5.7..2.1.43....4.2....64.7.2..7.1.....5....3......56.1.2." to "hard",
        ".16..78.3...8......7...1.6..48...3..6.......2..9...65..6.9...2......2...9.46..51." to "medium",
        ".7...8.29..2.....4854.2......83742.............32617......9.6122.....4..13.6...7." to "hard",
        ".........231.9.....65..31....8924...1...5...6...1367....93..57.....1.843........." to "hard",
        // Expert
        ".345.....8.2.6.4..6....8.....39....4.5.....9.9....58.....3....8..1.4.6.5.....712." to "expert",
        "8..........36......7..9.2...5...7.......457.....1...3...1....68..85...1..9....4.." to "expert",
        ".....57.....9.1......38.....6...2...4.9..5...7..1....3.2.....8....4..3......1..9." to "expert",
        "000000004760010050090002081070050010000709000080030060240100070010090045900000000".replace('0', '.') to "expert",
        // More variety
        "100000569402000008050009040000640801000010000208035000040500010900000402621000005".replace('0', '.') to "hard",
        ".8..9..3..3.........2.6.1.8.2.8..5..8..9.7..6..4..5.7.5.3.4.9.........1..1..5..2." to "expert",
        "85...24.....1.............2.8...4.....1...7...9..3...5....6....372.9..8.........." to "expert"
    )

    val beltInfo = mapOf(
        "easy" to ("⬜" to "White Belt"),
        "medium" to ("🟠" to "Orange Belt"),
        "hard" to ("🟣" to "Purple Belt"),
        "expert" to ("⬛" to "Black Belt")
    )

    // GET /daily — get today's daily challenge
    get("/daily") {
        val today = LocalDate.now(ZoneOffset.UTC)
        val dayIndex = today.dayOfYear % dailyPuzzles.size
        val (puzzleStr, difficulty) = dailyPuzzles[dayIndex]
        val (emoji, name) = beltInfo[difficulty] ?: ("⬜" to "Daily")

        val board: Board = try {
            BoardReader.readBoard(puzzleStr)
        } catch (e: Exception) {
            call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Failed to load daily puzzle"))
            return@get
        }

        val eliminator = SimpleCandidateEliminator()
        eliminator.eliminate(board)

        val candidates = mutableMapOf<String, List<Int>>()
        for (coord in Coord.all) {
            if (!board.isConfirmed(coord)) {
                val values = board.candidateValues(coord).toList()
                if (values.isNotEmpty()) {
                    candidates[coord.index.toString()] = values
                }
            }
        }

        call.respond(DailyChallenge(
            date = today.toString(),
            puzzle = puzzleStr,
            difficulty = difficulty,
            beltEmoji = emoji,
            beltName = name,
            candidates = candidates
        ))
    }

    // GET /daily/{date} — get a specific date's challenge
    get("/daily/{date}") {
        val dateStr = call.parameters["date"] ?: ""
        val date = try { LocalDate.parse(dateStr) } catch (e: Exception) {
            call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid date format. Use YYYY-MM-DD"))
            return@get
        }

        val dayIndex = date.dayOfYear % dailyPuzzles.size
        val (puzzleStr, difficulty) = dailyPuzzles[dayIndex]
        val (emoji, name) = beltInfo[difficulty] ?: ("⬜" to "Daily")

        val board: Board = try {
            BoardReader.readBoard(puzzleStr)
        } catch (e: Exception) {
            call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Failed to load puzzle"))
            return@get
        }

        val eliminator = SimpleCandidateEliminator()
        eliminator.eliminate(board)

        val candidates = mutableMapOf<String, List<Int>>()
        for (coord in Coord.all) {
            if (!board.isConfirmed(coord)) {
                val values = board.candidateValues(coord).toList()
                if (values.isNotEmpty()) {
                    candidates[coord.index.toString()] = values
                }
            }
        }

        call.respond(DailyChallenge(
            date = date.toString(),
            puzzle = puzzleStr,
            difficulty = difficulty,
            beltEmoji = emoji,
            beltName = name,
            candidates = candidates
        ))
    }
}
