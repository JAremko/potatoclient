package potatoclient.kotlin.transit

import java.io.File
import java.io.FileWriter
import java.io.PrintWriter
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.atomic.AtomicReference

/**
 * Simple logging utility for Kotlin subprocesses
 * Creates individual log files for each subprocess type
 */
object LoggingUtils {
    private val logWriter = AtomicReference<PrintWriter?>()
    val isReleaseBuild =
        System.getProperty("potatoclient.release") != null ||
            System.getenv("POTATOCLIENT_RELEASE") != null

    fun initializeLogging(processName: String) {
        if (isReleaseBuild) {
            // No file logging in release builds
            return
        }

        try {
            val logsDir = File("logs")
            if (!logsDir.exists()) {
                logsDir.mkdirs()
            }

            val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"))
            val logFile = File(logsDir, "$processName-$timestamp.log")

            val writer = PrintWriter(FileWriter(logFile, true), true)
            logWriter.set(writer)

            // Clean up old logs for this process type
            cleanupOldLogs(logsDir, processName, 10)

            log("INFO", "Logging initialized for $processName subprocess")
        } catch (e: Exception) {
            System.err.println("Failed to initialize logging: ${e.message}")
        }
    }

    fun log(
        level: String,
        message: String,
    ) {
        val timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        val logMessage = "[$timestamp] [$level] $message"

        // In development, write to log file
        val writer = logWriter.get()
        if (writer != null) {
            writer.println(logMessage)
        } else if (level == "ERROR") {
            // Only write errors to stderr if no log file is available
            System.err.println(logMessage)
        }
        // Don't write non-error messages to stdout as it interferes with Transit
    }

    fun logError(
        message: String,
        throwable: Throwable? = null,
    ) {
        log("ERROR", message)
        throwable?.let {
            val writer = logWriter.get()
            if (writer != null) {
                it.printStackTrace(writer)
            }
            it.printStackTrace(System.err)
        }
    }

    fun close() {
        logWriter.getAndSet(null)?.close()
    }

    private fun cleanupOldLogs(
        logsDir: File,
        processPrefix: String,
        maxFiles: Int,
    ) {
        try {
            val logFiles =
                logsDir.listFiles { file ->
                    file.isFile &&
                        file.name.startsWith(processPrefix) &&
                        file.name.endsWith(".log")
                } ?: return

            if (logFiles.size <= maxFiles) return

            // Sort by last modified time (newest first)
            logFiles.sortByDescending { it.lastModified() }

            // Delete old files
            logFiles.drop(maxFiles).forEach { file ->
                if (file.delete()) {
                    // Log to file only, not stdout
                    log("DEBUG", "Deleted old log file: ${file.name}")
                }
            }
        } catch (e: Exception) {
            System.err.println("Failed to cleanup old logs: ${e.message}")
        }
    }
}

// Extension functions for convenient logging
fun logInfo(message: String) = LoggingUtils.log("INFO", message)

fun logWarn(message: String) = LoggingUtils.log("WARN", message)

fun logError(
    message: String,
    throwable: Throwable? = null,
) = LoggingUtils.logError(message, throwable)

fun logDebug(message: String) {
    if (!LoggingUtils.isReleaseBuild) {
        LoggingUtils.log("DEBUG", message)
    }
}
