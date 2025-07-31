package potatoclient.transit

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import potatoclient.transit.MessageKeys
import potatoclient.transit.MessageType
import potatoclient.transit.logDebug
import potatoclient.transit.logError
import potatoclient.transit.logInfo
import potatoclient.transit.logWarn
import java.io.PrintWriter
import java.io.StringWriter

/**
 * Message protocol for Transit subprocesses to communicate structured
 * information back to the parent process, similar to video stream protocol.
 *
 * Messages are sent as Transit maps with proper type information.
 */
class TransitMessageProtocol(
    private val processType: String, // "command" or "state"
    private val transitComm: TransitCommunicator,
) {
    // Track if we're in release mode
    private val isReleaseBuild =
        System.getProperty("potatoclient.release") != null ||
            System.getenv("POTATOCLIENT_RELEASE") != null

    /**
     * Send a log message to parent process
     */
    fun sendLog(
        level: String,
        message: String,
    ) {
        try {
            val logMsg =
                createMessage(
                    MessageType.LOG.key,
                    mapOf(
                        MessageKeys.LEVEL to level,
                        MessageKeys.MESSAGE to message,
                        MessageKeys.PROCESS to processType,
                    ),
                )
            // Use coroutine to send message
            kotlinx.coroutines.GlobalScope.launch {
                transitComm.sendMessage(logMsg)
            }

            // Also log to file via LoggingUtils
            when (level) {
                "ERROR" -> logError(message)
                "WARN" -> logWarn(message)
                "DEBUG" -> logDebug(message)
                else -> logInfo(message)
            }
        } catch (e: Exception) {
            // Fallback to stderr
            System.err.println("[$processType] [$level] $message")
        }
    }

    /**
     * Send an exception with stack trace to parent process
     */
    fun sendException(
        context: String,
        ex: Exception,
    ) {
        try {
            val stackTrace =
                StringWriter().use { sw ->
                    PrintWriter(sw).use { pw ->
                        ex.printStackTrace(pw)
                        sw.toString()
                    }
                }

            val errorMsg =
                createMessage(
                    MessageType.ERROR.key,
                    mapOf(
                        MessageKeys.CONTEXT to context,
                        MessageKeys.ERROR to ex.message,
                        MessageKeys.CLASS to ex.javaClass.name,
                        MessageKeys.STACK_TRACE to stackTrace,
                        MessageKeys.PROCESS to processType,
                    ) as Map<String, Any>,
                )
            kotlinx.coroutines.GlobalScope.launch {
                transitComm.sendMessage(errorMsg)
            }

            // Also log to file
            logError("$context: ${ex.message}", ex)
        } catch (e: Exception) {
            // Fallback
            System.err.println("[$processType] [ERROR] $context: ${ex.message}")
            ex.printStackTrace()
        }
    }

    /**
     * Send a debug message (only in non-release builds)
     */
    fun sendDebug(message: String) {
        if (!isReleaseBuild) {
            sendLog("DEBUG", message)
        }
    }

    /**
     * Send info message
     */
    fun sendInfo(message: String) {
        sendLog("INFO", message)
    }

    /**
     * Send warning message
     */
    fun sendWarn(message: String) {
        sendLog("WARN", message)
    }

    /**
     * Send error message
     */
    fun sendError(message: String) {
        sendLog("ERROR", message)
    }

    /**
     * Send a metric/statistic update
     */
    fun sendMetric(
        name: String,
        value: Any,
    ) {
        try {
            val metricMsg =
                createMessage(
                    MessageType.METRIC.key,
                    mapOf(
                        MessageKeys.NAME to name,
                        MessageKeys.VALUE to value,
                        MessageKeys.PROCESS to processType,
                    ),
                )
            kotlinx.coroutines.GlobalScope.launch {
                transitComm.sendMessage(metricMsg)
            }
        } catch (e: Exception) {
            logError("Failed to send metric $name: ${e.message}")
        }
    }

    /**
     * Send a status update
     */
    fun sendStatus(
        status: String,
        details: Map<String, Any> = emptyMap(),
    ) {
        try {
            val statusMsg =
                createMessage(
                    MessageType.STATUS.key,
                    mapOf(
                        MessageKeys.STATUS to status,
                        MessageKeys.PROCESS to processType,
                    ) + details,
                )
            kotlinx.coroutines.GlobalScope.launch {
                transitComm.sendMessage(statusMsg)
            }
        } catch (e: Exception) {
            logError("Failed to send status $status: ${e.message}")
        }
    }

    /**
     * Send a response message
     */
    fun sendResponse(
        action: String,
        details: Map<String, Any> = emptyMap(),
    ) {
        try {
            val responseMsg =
                createMessage(
                    MessageType.RESPONSE.key,
                    mapOf(
                        MessageKeys.ACTION to action,
                        MessageKeys.PROCESS to processType,
                    ) + details,
                )

            kotlinx.coroutines.GlobalScope.launch {
                transitComm.sendMessage(responseMsg)
            }
        } catch (e: Exception) {
            logError("Failed to send response $action: ${e.message}")
        }
    }

    /**
     * Send an event message
     */
    fun sendEvent(
        eventType: String,
        data: Map<String, Any>,
    ) {
        try {
            val eventMsg =
                createMessage(
                    MessageType.EVENT.key,
                    mapOf(
                        MessageKeys.TYPE to eventType,
                        MessageKeys.PROCESS to processType,
                    ) + data,
                )
            kotlinx.coroutines.GlobalScope.launch {
                transitComm.sendMessage(eventMsg)
            }
        } catch (e: Exception) {
            logError("Failed to send event $eventType: ${e.message}")
        }
    }

    /**
     * Send a navigation event (for video streams)
     */
    fun sendNavigationEvent(
        x: Int,
        y: Int,
        frameTimestamp: Long,
        frameDuration: Long,
        canvasWidth: Int,
        canvasHeight: Int,
        navType: String,
        ndcX: Double,
        ndcY: Double,
    ) {
        sendEvent(
            "navigation",
            mapOf(
                MessageKeys.X to x,
                MessageKeys.Y to y,
                MessageKeys.FRAME_TIMESTAMP to frameTimestamp,
                MessageKeys.FRAME_DURATION to frameDuration,
                MessageKeys.CANVAS_WIDTH to canvasWidth,
                MessageKeys.CANVAS_HEIGHT to canvasHeight,
                MessageKeys.NAV_TYPE to navType,
                MessageKeys.NDC_X to ndcX,
                MessageKeys.NDC_Y to ndcY,
            ),
        )
    }

    /**
     * Send a window event (for video streams)
     */
    fun sendWindowEvent(
        eventType: String,
        windowState: Int? = null,
        x: Int? = null,
        y: Int? = null,
        width: Int? = null,
        height: Int? = null,
    ) {
        val data = mutableMapOf<String, Any>(MessageKeys.TYPE to eventType)
        windowState?.let { data[MessageKeys.WINDOW_STATE] = it }
        x?.let { data[MessageKeys.X] = it }
        y?.let { data[MessageKeys.Y] = it }
        width?.let { data[MessageKeys.WIDTH] = it }
        height?.let { data[MessageKeys.HEIGHT] = it }

        sendEvent("window", data)
    }

    /**
     * Create a properly formatted Transit message
     */
    private fun createMessage(
        msgType: String,
        payload: Map<String, Any>,
    ): Map<String, Any> =
        mapOf(
            MessageKeys.MSG_TYPE to msgType,
            MessageKeys.MSG_ID to
                java.util.UUID
                    .randomUUID()
                    .toString(),
            MessageKeys.TIMESTAMP to System.currentTimeMillis(),
            MessageKeys.PAYLOAD to payload,
        )
}
