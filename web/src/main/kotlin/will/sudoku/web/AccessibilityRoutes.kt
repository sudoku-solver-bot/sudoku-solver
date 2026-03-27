package will.sudoku.web

import io.github.smiley4.ktorswaggerui.dsl.routing.get
import io.github.smiley4.ktorswaggerui.dsl.routing.post
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import will.sudoku.solver.*

@Serializable
data class AccessibilitySettingsRequest(
    val fontSize: String? = null,
    val highContrast: Boolean? = null,
    val colorBlindMode: String? = null,
    val keyboardOnly: Boolean? = null,
    val screenReader: Boolean? = null,
    val reduceMotion: Boolean? = null,
    val largeTargets: Boolean? = null
)

@Serializable
data class AccessibilitySettingsResponse(
    val fontSize: String,
    val fontSizeScale: Double,
    val highContrast: Boolean,
    val colorBlindMode: String,
    val keyboardOnly: Boolean,
    val screenReader: Boolean,
    val reduceMotion: Boolean,
    val largeTargets: Boolean,
    val colorScheme: ColorSchemeResponse
)

@Serializable
data class ColorSchemeResponse(
    val given: String,
    val filled: String,
    val error: String,
    val highlight: String
)

@Serializable
data class AccessiblePuzzleResponse(
    val grid: List<List<AccessibleCellResponse>>,
    val description: String,
    val difficulty: String,
    val estimatedTime: String,
    val keyboardInstructions: List<String>
)

@Serializable
data class AccessibleCellResponse(
    val row: Int,
    val col: Int,
    val value: Int?,
    val isGiven: Boolean,
    val candidates: List<Int>,
    val ariaLabel: String,
    val screenReaderText: String
)

fun Route.accessibilityRoutes() {

    post("/accessibility/settings", {
        tags = listOf("Accessibility")
        description = "Update accessibility settings and get color scheme"
        request {
            body<AccessibilitySettingsRequest> {
                example("sample") {
                    value = AccessibilitySettingsRequest(
                        fontSize = "LARGE",
                        highContrast = true,
                        colorBlindMode = "DEUTERANOPIA",
                        screenReader = true
                    )
                }
            }
        }
        response {
            HttpStatusCode.OK to {
                description = "Updated settings with color scheme"
                body<AccessibilitySettingsResponse>()
            }
        }
    }) {
        val request = call.receive<AccessibilitySettingsRequest>()
        
        val fontSize = FontSize.values().find { 
            it.name == (request.fontSize ?: "MEDIUM") 
        } ?: FontSize.MEDIUM
        
        val colorBlindMode = ColorBlindMode.values().find {
            it.name == (request.colorBlindMode ?: "NONE")
        } ?: ColorBlindMode.NONE
        
        val colorScheme = AccessibilityHelper.getColorScheme(colorBlindMode)
        
        call.respond(
            AccessibilitySettingsResponse(
                fontSize = fontSize.displayName,
                fontSizeScale = fontSize.scale,
                highContrast = request.highContrast ?: false,
                colorBlindMode = colorBlindMode.displayName,
                keyboardOnly = request.keyboardOnly ?: false,
                screenReader = request.screenReader ?: false,
                reduceMotion = request.reduceMotion ?: false,
                largeTargets = request.largeTargets ?: false,
                colorScheme = ColorSchemeResponse(
                    given = colorScheme.given,
                    filled = colorScheme.filled,
                    error = colorScheme.error,
                    highlight = colorScheme.highlight
                )
            )
        )
    }
    
    get("/accessibility/instructions", {
        tags = listOf("Accessibility")
        description = "Get keyboard navigation instructions"
        response {
            HttpStatusCode.OK to {
                description = "Keyboard instructions"
            }
        }
    }) {
        val instructions = AccessibilityHelper.getKeyboardInstructions()
        
        call.respond(
            mapOf(
                "keyboardInstructions" to instructions,
                "message" to "Use these keyboard shortcuts to navigate and play"
            )
        )
    }
    
    post("/accessibility/puzzle", {
        tags = listOf("Accessibility")
        description = "Get puzzle in accessible format with screen reader support"
        request {
            body<Map<String, String>> {
                example("sample") {
                    mapOf(
                        "puzzle" to "530070000600195000...",
                        "difficulty" to "EASY"
                    )
                }
            }
        }
        response {
            HttpStatusCode.OK to {
                description = "Accessible puzzle format"
                body<AccessiblePuzzleResponse>()
            }
        }
    }) {
        val request = call.receive<Map<String, String>>()
        val puzzleString = request["puzzle"] ?: return@post call.respond(
            HttpStatusCode.BadRequest,
            mapOf("error" to "Missing puzzle")
        )
        
        val difficultyName = request["difficulty"] ?: "MEDIUM"
        val difficulty = DifficultyLevel.fromString(difficultyName) ?: DifficultyLevel.MEDIUM
        
        val board = BoardReader.read(puzzleString)
        val accessiblePuzzle = AccessibilityHelper.toAccessiblePuzzle(board, difficulty)
        
        call.respond(
            AccessiblePuzzleResponse(
                grid = accessiblePuzzle.grid.map { row ->
                    row.map { cell ->
                        AccessibleCellResponse(
                            row = cell.row,
                            col = cell.col,
                            value = cell.value,
                            isGiven = cell.isGiven,
                            candidates = cell.candidates.toList(),
                            ariaLabel = cell.ariaLabel,
                            screenReaderText = cell.toScreenReaderText()
                        )
                    }
                },
                description = accessiblePuzzle.description,
                difficulty = accessiblePuzzle.difficulty,
                estimatedTime = accessiblePuzzle.estimatedTime,
                keyboardInstructions = AccessibilityHelper.getKeyboardInstructions()
            )
        )
    }
    
    get("/accessibility/options", {
        tags = listOf("Accessibility")
        description = "Get all available accessibility options"
        response {
            HttpStatusCode.OK to {
                description = "List of accessibility options"
            }
        }
    }) {
        call.respond(
            mapOf(
                "fontSizes" to FontSize.values().map { 
                    mapOf("name" to it.name, "displayName" to it.displayName, "scale" to it.scale)
                },
                "colorBlindModes" to ColorBlindMode.values().map {
                    mapOf("name" to it.name, "displayName" to it.displayName)
                },
                "features" to listOf(
                    mapOf(
                        "name" to "highContrast",
                        "description" to "Increase contrast for better visibility"
                    ),
                    mapOf(
                        "name" to "keyboardOnly",
                        "description" to "Full keyboard navigation support"
                    ),
                    mapOf(
                        "name" to "screenReader",
                        "description" to "Optimized for screen readers"
                    ),
                    mapOf(
                        "name" to "reduceMotion",
                        "description" to "Reduce animations and transitions"
                    ),
                    mapOf(
                        "name" to "largeTargets",
                        "description" to "Larger touch/click targets"
                    )
                )
            )
        )
    }
}
