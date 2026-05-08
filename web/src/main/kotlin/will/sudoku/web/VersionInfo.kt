package will.sudoku.web

import java.io.InputStream
import java.util.Properties

/**
 * Provides build-time version information from version.properties on the classpath.
 *
 * version.properties is generated at build time by Gradle (processResources task)
 * and contains:
 *   - gitCommit: short commit hash (or "unknown" if git is unavailable)
 *   - buildTime: epoch millis when the build ran
 */
object VersionInfo {
    private val props: Properties by lazy {
        val p = Properties()
        try {
            val stream: InputStream? = javaClass.classLoader.getResourceAsStream("version.properties")
            if (stream != null) {
                p.load(stream)
                stream.close()
            }
        } catch (_: Exception) {
            // Properties remain empty
        }
        p
    }

    /** Short git commit hash at build time, or null if unavailable. */
    val gitCommit: String?
        get() {
            val v = props.getProperty("gitCommit")
            return if (v.isNullOrBlank() || v == "unknown") null else v
        }

    /** Build timestamp (epoch millis), or null if unavailable. */
    val buildTime: Long?
        get() = props.getProperty("buildTime")?.toLongOrNull()
}
