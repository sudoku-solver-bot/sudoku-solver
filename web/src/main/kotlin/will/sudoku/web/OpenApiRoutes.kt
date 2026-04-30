package will.sudoku.web

import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.http.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.*

/**
 * Auto-generated OpenAPI 3.0.3 specification for the Sudoku Solver API.
 * Served at /api/v1/openapi.json and /api/v1/docs (Swagger UI).
 */
fun Route.openapiRoutes() {
    get("/openapi.json") {
        call.respondText(
            contentType = ContentType.Application.Json,
            text = generateOpenApiSpec()
        )
    }

    // Swagger UI - lightweight inline HTML
    get("/docs") {
        call.respondText(
            contentType = ContentType.Text.Html,
            text = swaggerUiHtml
        )
    }
}

@Serializable
data class ApiEndpoint(
    val method: String,
    val path: String,
    val summary: String,
    val description: String
)

/** All documented API endpoints */
val apiEndpoints = listOf(
    ApiEndpoint("GET", "/health", "Health check", "Returns system health status with JVM metrics"),
    ApiEndpoint("POST", "/solve", "Solve puzzle", "Solve a sudoku puzzle with optional metrics"),
    ApiEndpoint("POST", "/hint", "Get hint", "Get a teaching hint for the current puzzle state"),
    ApiEndpoint("POST", "/generate", "Generate puzzle", "Generate a new sudoku puzzle by difficulty"),
    ApiEndpoint("GET", "/generate", "Generate puzzle (GET)", "Generate a puzzle via query parameters"),
    ApiEndpoint("POST", "/validate", "Validate puzzle", "Validate a puzzle and optionally check solution uniqueness"),
    ApiEndpoint("POST", "/step-by-step", "Step-by-step solve", "Solve a puzzle with detailed step-by-step explanation"),
    ApiEndpoint("POST", "/candidates", "Get candidates", "Get all pencil mark candidates for empty cells"),
    ApiEndpoint("GET", "/tutorials", "List tutorials", "Get available tutorial lessons grouped by belt level"),
    ApiEndpoint("GET", "/daily", "Daily challenge", "Get today's daily challenge puzzle"),
    ApiEndpoint("POST", "/daily/stats", "Submit daily stats", "Submit completion stats for the daily challenge"),
    ApiEndpoint("POST", "/dashboard/report", "Dashboard report", "Get student dashboard with progress"),
    ApiEndpoint("POST", "/celebration", "Celebration", "Get celebration details for an achievement"),
    ApiEndpoint("POST", "/progress", "Get progress", "Get user progress (XP, level, puzzles solved)"),
    ApiEndpoint("POST", "/generate/difficulty", "Generate by difficulty/age", "Generate a puzzle appropriate for a given age or difficulty"),
    ApiEndpoint("POST", "/user-testing/participant", "Create test participant", "Register a user testing participant"),
    ApiEndpoint("POST", "/user-testing/session", "Create test session", "Start a user testing session"),
    ApiEndpoint("POST", "/user-testing/survey", "Submit survey", "Submit post-test survey responses"),
)

private val prettyJson = Json { prettyPrint = true }

private fun generateOpenApiSpec(): String {
    val spec = buildJsonObject {
        put("openapi", "3.0.3")
        put("info", buildJsonObject {
            put("title", "Sudoku Solver API")
            put("description", "A comprehensive Sudoku solver, teacher, and puzzle generator API. " +
                "Solve puzzles, get hints, generate puzzles by difficulty, track progress, and more.")
            put("version", "1.0.0")
            put("contact", buildJsonObject {
                put("name", "Sudoku Solver")
                put("url", "https://github.com/sudoku-solver-bot/sudoku-solver")
            })
            put("license", buildJsonObject {
                put("name", "MIT")
                put("url", "https://opensource.org/licenses/MIT")
            })
        })
        put("servers", buildJsonArray {
            add(buildJsonObject {
                put("url", "https://sudoku-solver-r5y8.onrender.com")
                put("description", "Production")
            })
            add(buildJsonObject {
                put("url", "http://localhost:8080")
                put("description", "Local development")
            })
        })
        put("tags", buildJsonArray {
            add(buildJsonObject { put("name", "Solving"); put("description", "Puzzle solving endpoints") })
            add(buildJsonObject { put("name", "Generation"); put("description", "Puzzle generation endpoints") })
            add(buildJsonObject { put("name", "Teaching"); put("description", "Hints, tutorials, and educational features") })
            add(buildJsonObject { put("name", "Daily Challenge"); put("description", "Daily puzzle challenges") })
            add(buildJsonObject { put("name", "Progress"); put("description", "User progress and gamification") })
            add(buildJsonObject { put("name", "System"); put("description", "Health check and system info") })
        })
        put("paths", buildPaths())
        put("components", buildComponents())
    }
    return prettyJson.encodeToString(JsonObject.serializer(), spec)
}

private fun buildPaths(): JsonObject = buildJsonObject {
    // /health
    put("/health", buildJsonObject {
        put("get", buildOperation("System", "Health check",
            "Returns system health status including JVM memory, threads, uptime, and OS info. " +
                "Status can be OK, DEGRADED (>80% heap), or WARNING (>90% heap).",
            "200", "HealthResponse"))
    })

    // /solve
    put("/solve", buildJsonObject {
        put("post", buildOperation("Solving", "Solve a Sudoku puzzle",
            "Solve a sudoku puzzle provided as an 81-character string (0 or . for empty cells). " +
                "Optionally include solving metrics like time, backtracking count, and difficulty rating.",
            "200", "SolveResponse", "SolveRequest",
            errors = listOf("400")))
    })

    // /hint
    put("/hint", buildJsonObject {
        put("post", buildOperation("Teaching", "Get a teaching hint",
            "Get a contextual hint for the current puzzle state. Returns the technique name, " +
                "cell coordinates, explanation, and teaching points.",
            "200", "HintResponse", "HintRequest",
            errors = listOf("400")))
    })

    // /generate
    put("/generate", buildJsonObject {
        put("post", buildOperation("Generation", "Generate a puzzle",
            "Generate a new sudoku puzzle by difficulty level. Supports EASY, MEDIUM, HARD, EXPERT, and MASTER. " +
                "Optionally provide a seed for reproducible generation.",
            "200", "GenerateResponse", "GenerateRequest",
            errors = listOf("400")))
        put("get", buildOperation("Generation", "Generate a puzzle (GET)",
            "Generate a puzzle via query parameters. Same as POST but convenient for quick testing.",
            "200", "GenerateResponse",
            params = listOf(
                Triple("difficulty", "query", "Difficulty level (EASY, MEDIUM, HARD, EXPERT, MASTER)"),
                Triple("seed", "query", "Random seed for reproducible generation")
            ),
            errors = listOf("400")))
    })

    // /validate
    put("/validate", buildJsonObject {
        put("post", buildOperation("Solving", "Validate a puzzle",
            "Validate a sudoku puzzle for correctness and optionally check for unique solution.",
            "200", "ValidateResponse", "ValidateRequest",
            errors = listOf("400")))
    })

    // /step-by-step
    put("/step-by-step", buildJsonObject {
        put("post", buildOperation("Solving", "Step-by-step solution",
            "Solve a puzzle with detailed step-by-step explanation including technique used at each step.",
            "200", "SolveStepsResponse", "SolveStepsRequest",
            errors = listOf("400")))
    })

    // /candidates
    put("/candidates", buildJsonObject {
        put("post", buildOperation("Solving", "Get pencil mark candidates",
            "Get all possible candidate values (pencil marks) for each empty cell in the puzzle.",
            "200", "CandidatesResponse", "CandidatesRequest",
            errors = listOf("400")))
    })

    // /tutorials
    put("/tutorials", buildJsonObject {
        put("get", buildOperation("Teaching", "List tutorial lessons",
            "Get all available tutorial lessons grouped by belt level (white through black belt)."))
    })

    // /daily
    put("/daily", buildJsonObject {
        put("get", buildOperation("Daily Challenge", "Get daily challenge",
            "Get today's daily challenge puzzle. Changes at midnight UTC. Includes difficulty, belt level, and candidates."))
    })

    // /daily/stats
    put("/daily/stats", buildJsonObject {
        put("post", buildOperation("Daily Challenge", "Submit daily stats",
            "Submit completion statistics for the daily challenge.",
            "200", "DailyStatsResponse", "DailyStatsRequest"))
    })

    // /dashboard/report
    put("/dashboard/report", buildJsonObject {
        put("post", buildOperation("Progress", "Get dashboard report",
            "Get a student's dashboard with puzzles solved, average time, streak, and level.",
            "200", "DashboardResponse", "DashboardRequest"))
    })

    // /celebration
    put("/celebration", buildJsonObject {
        put("post", buildOperation("Progress", "Get celebration",
            "Get celebration details (confetti type, sound, message) for an achievement.",
            "200", "CelebrationResponse", "CelebrationRequest"))
    })

    // /progress
    put("/progress", buildJsonObject {
        put("post", buildOperation("Progress", "Get user progress",
            "Get user progress including level, XP, and puzzles solved.",
            "200", "ProgressResponse", "ProgressRequest"))
    })

    // /generate/difficulty
    put("/generate/difficulty", buildJsonObject {
        put("post", buildOperation("Generation", "Generate by age/difficulty",
            "Generate a puzzle appropriate for a specific age or difficulty level. " +
                "Uses age-to-difficulty mapping for educational purposes.",
            "200", "GenerateDifficultyResponse", "GenerateDifficultyRequest",
            errors = listOf("400")))
    })
}

private fun buildOperation(
    tag: String,
    summary: String,
    description: String,
    successCode: String = "200",
    successRef: String? = null,
    requestBodyRef: String? = null,
    params: List<Triple<String, String, String>> = emptyList(),
    errors: List<String> = emptyList()
): JsonObject = buildJsonObject {
    put("tags", buildJsonArray { add(tag) })
    put("summary", summary)
    put("description", description)
    if (params.isNotEmpty()) {
        put("parameters", buildJsonArray {
            params.forEach { (name, `in`, desc) ->
                add(buildJsonObject {
                    put("name", name)
                    put("in", `in`)
                    put("required", false)
                    put("schema", buildJsonObject { put("type", "string") })
                    put("description", desc)
                })
            }
        })
    }
    if (requestBodyRef != null) {
        put("requestBody", buildJsonObject {
            put("required", true)
            put("content", buildJsonObject {
                put("application/json", buildJsonObject {
                    put("schema", buildJsonObject {
                        put("\$ref", "#/components/schemas/$requestBodyRef")
                    })
                })
            })
        })
    }
    put("responses", buildJsonObject {
        if (successRef != null) {
            put(successCode, buildJsonObject {
                put("description", "Successful response")
                put("content", buildJsonObject {
                    put("application/json", buildJsonObject {
                        put("schema", buildJsonObject {
                            put("\$ref", "#/components/schemas/$successRef")
                        })
                    })
                })
            })
        } else {
            put(successCode, buildJsonObject {
                put("description", "Successful response")
            })
        }
        errors.forEach { code ->
            put(code, buildJsonObject {
                put("description", "Bad request")
                put("content", buildJsonObject {
                    put("application/json", buildJsonObject {
                        put("schema", buildJsonObject {
                            put("\$ref", "#/components/schemas/ErrorResponse")
                        })
                    })
                })
            })
        }
    })
}

private fun buildComponents(): JsonObject = buildJsonObject {
    put("schemas", buildJsonObject {
        // Solve
        schema("SolveRequest", mapOf(
            "puzzle" to "string (81 chars, 0 or . for empty)" to true,
            "includeMetrics" to "boolean (default: false)" to false
        ))
        schema("SolveResponse", mapOf(
            "solved" to "boolean" to true,
            "solution" to "string (81 chars) or null" to false,
            "metrics" to "SolverMetricsResponse or null" to false,
            "error" to "string or null" to false
        ))
        schema("SolverMetricsResponse", mapOf(
            "solveTimeMs" to "number" to true,
            "backtrackingCount" to "integer" to true,
            "maxRecursionDepth" to "integer" to true,
            "propagationPasses" to "integer" to true,
            "cellsProcessed" to "integer" to true,
            "difficulty" to "string (EASY/MEDIUM/HARD/EXPERT/MASTER)" to false,
            "techniquesUsed" to "array of strings" to false
        ))

        // Hint
        schema("HintRequest", mapOf(
            "puzzle" to "string (81 chars)" to true
        ))
        schema("HintResponse", mapOf(
            "type" to "string" to true,
            "cell" to "CellCoordinate or null" to false,
            "technique" to "string" to true,
            "explanation" to "string" to true,
            "teachingPoints" to "array of strings" to true
        ))
        schema("CellCoordinate", mapOf(
            "row" to "integer (0-8)" to true,
            "col" to "integer (0-8)" to true
        ))

        // Generate
        schema("GenerateRequest", mapOf(
            "difficulty" to "string (EASY/MEDIUM/HARD/EXPERT/MASTER, default: MEDIUM)" to false,
            "seed" to "integer or null" to false
        ))
        schema("GenerateResponse", mapOf(
            "puzzle" to "string (81 chars)" to true,
            "difficulty" to "string" to true,
            "error" to "string or null" to false
        ))

        // Validate
        schema("ValidateRequest", mapOf(
            "puzzle" to "string (81 chars)" to true,
            "checkUniqueness" to "boolean (default: true)" to false
        ))
        schema("ValidateResponse", mapOf(
            "valid" to "boolean" to true,
            "uniqueSolution" to "boolean or null" to false,
            "solutionCount" to "integer or null" to false,
            "errors" to "array of ValidationErrorData" to false,
            "error" to "string or null" to false
        ))

        // Step-by-step
        schema("SolveStepsRequest", mapOf(
            "puzzle" to "string (81 chars)" to true
        ))
        schema("SolveStepsResponse", mapOf(
            "solved" to "boolean" to true,
            "solution" to "string or null" to false,
            "steps" to "array of StepResponse" to true,
            "totalSteps" to "integer" to true,
            "solveTimeMs" to "number or null" to false,
            "error" to "string or null" to false
        ))

        // Candidates
        schema("CandidatesRequest", mapOf(
            "puzzle" to "string (81 chars)" to true
        ))
        schema("CandidatesResponse", mapOf(
            "candidates" to "object (cell key -> array of ints)" to true,
            "error" to "string or null" to false
        ))

        // Dashboard
        schema("DashboardRequest", mapOf(
            "studentId" to "string" to true
        ))
        schema("DashboardResponse", mapOf(
            "studentId" to "string" to true,
            "studentName" to "string" to true,
            "puzzlesSolved" to "integer" to true,
            "averageTimeSeconds" to "integer" to true,
            "streak" to "integer" to true,
            "level" to "integer" to true
        ))

        // Celebration
        schema("CelebrationRequest", mapOf(
            "solved" to "boolean" to false,
            "perfect" to "boolean" to false,
            "fast" to "boolean" to false
        ))
        schema("CelebrationResponse", mapOf(
            "type" to "string" to true,
            "intensity" to "string" to true,
            "duration" to "integer" to true,
            "sound" to "string or null" to false,
            "message" to "string" to true,
            "emoji" to "array of strings" to true
        ))

        // Progress
        schema("ProgressRequest", mapOf(
            "userId" to "string" to true
        ))
        schema("ProgressResponse", mapOf(
            "userId" to "string" to true,
            "level" to "integer" to true,
            "xp" to "integer" to true,
            "puzzlesSolved" to "integer" to true
        ))

        // Generate by difficulty
        schema("GenerateDifficultyRequest", mapOf(
            "difficulty" to "string or null" to false,
            "age" to "integer or null" to false
        ))
        schema("GenerateDifficultyResponse", mapOf(
            "puzzle" to "string" to true,
            "difficulty" to "string" to true,
            "targetAge" to "string" to true
        ))

        // Daily
        schema("DailyStatsRequest", mapOf(
            "date" to "string (YYYY-MM-DD)" to true,
            "solved" to "boolean" to false,
            "timeSeconds" to "integer" to false,
            "hintsUsed" to "integer" to false
        ))

        // Health
        schema("HealthResponse", mapOf(
            "status" to "string (OK/DEGRADED/WARNING)" to true,
            "version" to "string" to true,
            "timestamp" to "integer (epoch ms)" to true,
            "uptime" to "UptimeInfo or null" to false,
            "jvm" to "JvmInfo or null" to false,
            "system" to "SystemInfo or null" to false
        ))

        // Error
        schema("ErrorResponse", mapOf(
            "error" to "string" to true
        ))
    })
}

private fun JsonObjectBuilder.schema(name: String, fields: Map<Pair<String, String>, Boolean>) {
    put(name, buildJsonObject {
        put("type", "object")
        put("properties", buildJsonObject {
            fields.forEach { (desc, required) ->
                val (fieldName, typeDesc) = desc
                put(fieldName, buildJsonObject {
                    put("description", typeDesc)
                })
            }
        })
        val reqFields = fields.filter { it.value }.keys.map { it.first }
        if (reqFields.isNotEmpty()) {
            put("required", buildJsonArray { reqFields.forEach { add(it) } })
        }
    })
}

private val swaggerUiHtml = """
<!DOCTYPE html>
<html>
<head>
    <title>Sudoku Solver API - Swagger UI</title>
    <meta charset="utf-8"/>
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <link rel="stylesheet" href="https://unpkg.com/swagger-ui-dist@5/swagger-ui.css">
    <style>
        body { margin: 0; }
        .swagger-ui .topbar { display: none; }
    </style>
</head>
<body>
    <div id="swagger-ui"></div>
    <script src="https://unpkg.com/swagger-ui-dist@5/swagger-ui-bundle.js"></script>
    <script>
        SwaggerUIBundle({
            url: "openapi.json",
            dom_id: '#swagger-ui',
            presets: [
                SwaggerUIBundle.presets.apis,
                SwaggerUIBundle.SwaggerUIStandalonePreset
            ],
            layout: "BaseLayout"
        });
    </script>
</body>
</html>
""".trimIndent()
