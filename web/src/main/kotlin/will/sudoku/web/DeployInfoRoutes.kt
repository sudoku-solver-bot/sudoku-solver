package will.sudoku.web

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import java.io.File
import java.util.Properties

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

@Serializable
data class VersionResponse(
    val version: String,
    val gitCommit: String?,
    val buildTimestamp: String?
)

/**
 * Build version info loaded from classpath version.properties (injected at build time by Gradle).
 * Falls back to null if properties are not available (e.g. during development without build).
 */
private val buildVersionInfo: Pair<String?, String?> by lazy {
    try {
        val props = Properties()
        val stream = Thread.currentThread().contextClassLoader
            .getResourceAsStream("version.properties")
        if (stream != null) {
            props.load(stream)
            val commit = props.getProperty("gitCommit")?.takeIf { it != "unknown" && it.isNotEmpty() }
            val time = props.getProperty("buildTime")?.takeIf { it.isNotEmpty() }
            Pair(commit, time)
        } else {
            Pair(null, null)
        }
    } catch (_: Exception) {
        Pair(null, null)
    }
}

/**
 * Read the git commit hash.
 * Priority: classpath version.properties > deploy commit file.
 */
internal fun readGitCommit(): String? {
    // Try classpath first (build-time injected)
    buildVersionInfo.first?.let { return it }

    // Fallback: deploy commit file
    return try {
        val commitFile = File(System.getenv("DEPLOY_COMMIT_FILE") ?: "/opt/sudoku/.deploy-commit")
        if (commitFile.isFile) commitFile.readText().trim().takeIf { it.isNotEmpty() } else null
    } catch (_: Exception) {
        null
    }
}

/**
 * Read build timestamp.
 * Priority: classpath version.properties > build timestamp file.
 */
internal fun readBuildTimestamp(): String? {
    // Try classpath first (build-time injected)
    buildVersionInfo.second?.let { return it }

    // Fallback: build timestamp file
    return try {
        val timestampFile = File(System.getenv("BUILD_TIMESTAMP_FILE") ?: "/opt/sudoku/.build-timestamp")
        if (timestampFile.isFile) timestampFile.readText().trim().takeIf { it.isNotEmpty() } else null
    } catch (_: Exception) {
        null
    }
}

fun Route.deployInfoRoutes() {
    get("/version") {
        call.respond(
            HttpStatusCode.OK,
            VersionResponse(
                version = "1.0.0",
                gitCommit = readGitCommit(),
                buildTimestamp = readBuildTimestamp()
            )
        )
    }

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
