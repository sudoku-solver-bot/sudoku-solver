package will.sudoku.web

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import will.sudoku.solver.*

@Serializable
data class SaveStateRequest(
    val sessionId: String,
    val puzzle: String,
    val row: Int? = null,
    val col: Int? = null,
    val oldValue: Int? = null,
    val newValue: Int? = null,
    val moveType: String? = null
)

@Serializable
data class UndoRedoRequest(
    val sessionId: String,
    val currentPuzzle: String
)

@Serializable
data class UndoRedoResponse(
    val puzzle: String?,
    val success: Boolean,
    val message: String,
    val undoCount: Int,
    val redoCount: Int
)

@Serializable
data class HistoryResponse(
    val undoCount: Int,
    val redoCount: Int,
    val canUndo: Boolean,
    val canRedo: Boolean
)

// Session-based storage (in production, use proper session management)
private val sessionManagers = mutableMapOf<String, UndoRedoManager>()

fun Route.undoRedoRoutes() {
    
    post("/api/v1/undo-redo/save") {
        val request = call.receive<SaveStateRequest>()
        
        // Get or create manager for session
        val manager = sessionManagers.getOrPut(request.sessionId) { UndoRedoManager() }
        
        // Create move if provided
        val move = if (request.row != null && request.col != null && 
                       request.oldValue != null && request.newValue != null) {
            Move(
                row = request.row,
                col = request.col,
                oldValue = request.oldValue,
                newValue = request.newValue,
                type = MoveType.valueOf(request.moveType ?: "PLACE")
            )
        } else null
        
        // Save state
        val state = PuzzleState(
            puzzle = request.puzzle,
            move = move
        )
        manager.saveState(state)
        
        call.respond(
            UndoRedoResponse(
                puzzle = null,
                success = true,
                message = "State saved",
                undoCount = manager.undoCount(),
                redoCount = manager.redoCount()
            )
        )
    }
    
    post("/api/v1/undo-redo/undo") {
        val request = call.receive<UndoRedoRequest>()
        
        val manager = sessionManagers[request.sessionId]
        if (manager == null) {
            return@post call.respond(
                HttpStatusCode.NotFound,
                mapOf("error" to "Session not found")
            )
        }
        
        val currentState = PuzzleState(request.currentPuzzle)
        val previousState = manager.undo(currentState)
        
        if (previousState == null) {
            return@post call.respond(
                UndoRedoResponse(
                    puzzle = null,
                    success = false,
                    message = "No moves to undo",
                    undoCount = 0,
                    redoCount = manager.redoCount()
                )
            )
        }
        
        call.respond(
            UndoRedoResponse(
                puzzle = previousState.puzzle,
                success = true,
                message = "Undone successfully",
                undoCount = manager.undoCount(),
                redoCount = manager.redoCount()
            )
        )
    }
    
    post("/api/v1/undo-redo/redo") {
        val request = call.receive<UndoRedoRequest>()
        
        val manager = sessionManagers[request.sessionId]
        if (manager == null) {
            return@post call.respond(
                HttpStatusCode.NotFound,
                mapOf("error" to "Session not found")
            )
        }
        
        val nextState = manager.redo()
        
        if (nextState == null) {
            return@post call.respond(
                UndoRedoResponse(
                    puzzle = null,
                    success = false,
                    message = "No moves to redo",
                    undoCount = manager.undoCount(),
                    redoCount = 0
                )
            )
        }
        
        call.respond(
            UndoRedoResponse(
                puzzle = nextState.puzzle,
                success = true,
                message = "Redone successfully",
                undoCount = manager.undoCount(),
                redoCount = manager.redoCount()
            )
        )
    }
    
    get("/api/v1/undo-redo/history/{sessionId}") {
        val sessionId = call.parameters["sessionId"] ?: return@get call.respond(
            HttpStatusCode.BadRequest,
            mapOf("error" to "Missing sessionId")
        )
        
        val manager = sessionManagers[sessionId]
        if (manager == null) {
            return@get call.respond(
                HttpStatusCode.NotFound,
                mapOf("error" to "Session not found")
            )
        }
        
        val history = manager.getHistory()
        call.respond(
            HistoryResponse(
                undoCount = history.undoCount,
                redoCount = history.redoCount,
                canUndo = history.canUndo,
                canRedo = history.canRedo
            )
        )
    }
    
    delete("/api/v1/undo-redo/clear/{sessionId}") {
        val sessionId = call.parameters["sessionId"] ?: return@delete call.respond(
            HttpStatusCode.BadRequest,
            mapOf("error" to "Missing sessionId")
        )
        
        val manager = sessionManagers[sessionId]
        if (manager == null) {
            return@delete call.respond(
                HttpStatusCode.NotFound,
                mapOf("error" to "Session not found")
            )
        }
        
        manager.clear()
        call.respond(mapOf("message" to "History cleared"))
    }
}
