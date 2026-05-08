package will.sudoku.web

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import java.io.File

/**
 * Lightweight deploy-info endpoint for monitoring and deploy verification.
 * Returns version, commit hash, and build timestamp without heavy system metrics.
 */
@Serializable
data class DeployInfoResponse(
    val version: String,
    val gitCommit: String?,
    val buildTimestamp: String?
)

/**
 * Read the deployed git commit hash from a file.
 * Path is configurable via DEPLOY_COMMIT_FILE env var (default: /opt/sudoku/.deploy-commit).
 * Returns null if the file is missing or unreadable (graceful degradation).
 */
internal fun readGitCommit(): String? {
    return try {
        val commitFile = File(System.getenv("DEPLOY_COMMIT_FILE") ?: "/opt/sudoku/.deploy-commit")
        if (commitFile.isFile) commitFile.readText().trim().takeIf { it.isNotEmpty() } else null
    } catch (_: Exception) {
        null
    }
}

/**
 * Read build timestamp from a file placed by the build pipeline.
 * Returns null if the file is missing or unreadable.
 */
internal fun readBuildTimestamp(): String? {
    return try {
        val timestampFile = File(System.getenv("BUILD_TIMESTAMP_FILE") ?: "/opt/sudoku/.build-timestamp")
        if (timestampFile.isFile) timestampFile.readText().trim().takeIf { it.isNotEmpty() } else null
    } catch (_: Exception) {
        null
    }
}

fun Route.deployInfoRoutes() {
    get("/deploy-info") {
        call.respond(
            HttpStatusCode.OK,
            DeployInfoResponse(
                version = "1.0.0",
                gitCommit = readGitCommit(),
                buildTimestamp = readBuildTimestamp()
            )
        )
    }
}
