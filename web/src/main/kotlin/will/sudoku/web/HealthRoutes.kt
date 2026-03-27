package will.sudoku.web

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import java.lang.management.ManagementFactory
import java.lang.management.MemoryMXBean
import java.lang.management.RuntimeMXBean

@Serializable
data class HealthResponse(
    val status: String,
    val version: String,
    val timestamp: Long = System.currentTimeMillis(),
    val uptime: UptimeInfo? = null,
    val jvm: JvmInfo? = null,
    val system: SystemInfo? = null
)

@Serializable
data class UptimeInfo(
    val milliseconds: Long,
    val humanReadable: String
)

@Serializable
data class JvmInfo(
    val memory: MemoryInfo,
    val threads: ThreadInfo,
    val javaVersion: String,
    val javaVendor: String
)

@Serializable
data class MemoryInfo(
    val heapUsedMB: Long,
    val heapMaxMB: Long,
    val heapUsedPercent: Double,
    val nonHeapUsedMB: Long,
    val freeMemoryMB: Long,
    val totalMemoryMB: Long
)

@Serializable
data class ThreadInfo(
    val threadCount: Int,
    val peakThreadCount: Int,
    val daemonThreadCount: Int
)

@Serializable
data class SystemInfo(
    val osName: String,
    val osVersion: String,
    val osArch: String,
    val availableProcessors: Int,
    val userTimezone: String
)

fun Route.healthRoutes() {
    val runtimeBean: RuntimeMXBean = ManagementFactory.getRuntimeMXBean()
    val memoryBean: MemoryMXBean = ManagementFactory.getMemoryMXBean()
    val threadBean = ManagementFactory.getThreadMXBean()
    val startTime = System.currentTimeMillis()

    get("/health") {
        val currentTime = System.currentTimeMillis()
        val uptimeMs = currentTime - startTime

        // Memory metrics
        val heapUsage = memoryBean.heapMemoryUsage
        val nonHeapUsage = memoryBean.nonHeapMemoryUsage
        val runtime = Runtime.getRuntime()

        val memoryInfo = MemoryInfo(
            heapUsedMB = heapUsage.used / (1024 * 1024),
            heapMaxMB = heapUsage.max / (1024 * 1024),
            heapUsedPercent = (heapUsage.used.toDouble() / heapUsage.max.toDouble()) * 100,
            nonHeapUsedMB = nonHeapUsage.used / (1024 * 1024),
            freeMemoryMB = runtime.freeMemory() / (1024 * 1024),
            totalMemoryMB = runtime.totalMemory() / (1024 * 1024)
        )

        val threadInfo = ThreadInfo(
            threadCount = threadBean.threadCount,
            peakThreadCount = threadBean.peakThreadCount,
            daemonThreadCount = threadBean.daemonThreadCount
        )

        val jvmInfo = JvmInfo(
            memory = memoryInfo,
            threads = threadInfo,
            javaVersion = System.getProperty("java.version", "unknown"),
            javaVendor = System.getProperty("java.vendor", "unknown")
        )

        val systemInfo = SystemInfo(
            osName = System.getProperty("os.name", "unknown"),
            osVersion = System.getProperty("os.version", "unknown"),
            osArch = System.getProperty("os.arch", "unknown"),
            availableProcessors = runtime.availableProcessors(),
            userTimezone = System.getProperty("user.timezone", "unknown")
        )

        val uptimeInfo = UptimeInfo(
            milliseconds = uptimeMs,
            humanReadable = formatUptime(uptimeMs)
        )

        // Determine health status based on memory usage
        val status = when {
            memoryInfo.heapUsedPercent > 90 -> "WARNING"
            memoryInfo.heapUsedPercent > 80 -> "DEGRADED"
            else -> "OK"
        }

        call.respond(
            HttpStatusCode.OK,
            HealthResponse(
                status = status,
                version = "1.0.0",
                uptime = uptimeInfo,
                jvm = jvmInfo,
                system = systemInfo
            )
        )
    }
}

private fun formatUptime(ms: Long): String {
    val seconds = ms / 1000
    val minutes = seconds / 60
    val hours = minutes / 60
    val days = hours / 24

    return when {
        days > 0 -> "${days}d ${hours % 24}h ${minutes % 60}m"
        hours > 0 -> "${hours}h ${minutes % 60}m ${seconds % 60}s"
        minutes > 0 -> "${minutes}m ${seconds % 60}s"
        else -> "${seconds}s"
    }
}
