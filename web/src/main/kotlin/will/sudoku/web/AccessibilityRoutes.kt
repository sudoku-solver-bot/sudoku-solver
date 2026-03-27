package will.sudoku.web

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
    val largeTargets: Boolean
)

fun Route.accessibilityRoutes() {
    post("/accessibility/settings") {
        val request = call.receive<AccessibilitySettingsRequest>()
        
        val fontSize = FontSize.valueOf(request.fontSize ?: "MEDIUM")
        val colorBlindMode = ColorBlindMode.valueOf(request.colorBlindMode ?: "NONE")
        
        call.respond(
            AccessibilitySettingsResponse(
                fontSize = fontSize.name,
                fontSizeScale = fontSize.scale,
                highContrast = request.highContrast ?: false,
                colorBlindMode = colorBlindMode.name,
                keyboardOnly = request.keyboardOnly ?: false,
                screenReader = request.screenReader ?: false,
                reduceMotion = request.reduceMotion ?: false,
                largeTargets = request.largeTargets ?: false
            )
        )
    }
    
    get("/accessibility/instructions") {
        call.respond(
            mapOf(
                "keyboardNavigation" to listOf(
                    "Arrow keys: Navigate grid",
                    "Number keys 1-9: Fill cell",
                    "Delete/Backspace: Clear cell",
                    "H: Get hint",
                    "R: Reset puzzle"
                ),
                "screenReaderTips" to listOf(
                    "Grid announced as table",
                    "Cell values announced",
                    "Hints announced automatically"
                )
            )
        )
    }
}
