package potatoclient.kotlin.transit

import com.cognitect.transit.Keyword
import com.cognitect.transit.TransitFactory
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import potatoclient.java.transit.EventType
import potatoclient.java.transit.MessageType
import potatoclient.kotlin.transit.logDebug
import potatoclient.kotlin.transit.logError
import potatoclient.kotlin.transit.logInfo
import potatoclient.kotlin.transit.logWarn
import java.io.PrintWriter
import java.io.StringWriter

/**
 * Message protocol for Transit subprocesses to communicate structured
 * information back to the parent process, similar to video stream protocol.
 *
 * Messages are sent as Transit maps with proper type information.
 */
class TransitMessageProtocol(
    val processType: String, // "command" or "state" - made public for MessageBuilder
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
                    MessageType.LOG,
                    mapOf(
                        TransitKeys.LEVEL to level,
                        TransitKeys.MESSAGE to message,
                        TransitKeys.PROCESS to processType,
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
                    MessageType.ERROR,
                    mapOf(
                        TransitKeys.CONTEXT to context,
                        TransitKeys.ERROR to (ex.message ?: "Unknown error"),
                        TransitKeys.CLASS to ex.javaClass.name,
                        TransitKeys.STACK_TRACE to stackTrace,
                        TransitKeys.PROCESS to processType,
                    ),
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
                    MessageType.METRIC,
                    mapOf(
                        TransitKeys.NAME to name,
                        TransitKeys.VALUE to value,
                        TransitKeys.PROCESS to processType,
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
        status: Keyword,
        details: Map<Any, Any> = emptyMap(),
    ) {
        try {
            val statusMsg =
                createMessage(
                    MessageType.STATUS,
                    mapOf(
                        TransitKeys.STATUS to status,
                        TransitKeys.PROCESS to processType,
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
                    MessageType.RESPONSE,
                    mapOf(
                        TransitKeys.ACTION to action,
                        TransitKeys.PROCESS to processType,
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
        eventType: EventType,
        data: Map<Any, Any>,
    ) {
        try {
            val eventMsg =
                createMessage(
                    MessageType.EVENT,
                    mapOf(
                        TransitKeys.TYPE to eventType.keyword, // Use keyword instead of enum
                        TransitKeys.PROCESS to processType,
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
            EventType.NAVIGATION,
            mapOf(
                TransitKeys.X to x,
                TransitKeys.Y to y,
                TransitKeys.FRAME_TIMESTAMP to frameTimestamp,
                TransitKeys.FRAME_DURATION to frameDuration,
                TransitKeys.CANVAS_WIDTH to canvasWidth,
                TransitKeys.CANVAS_HEIGHT to canvasHeight,
                TransitKeys.NAV_TYPE to navType,
                TransitKeys.NDC_X to ndcX,
                TransitKeys.NDC_Y to ndcY,
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
        val data = mutableMapOf<Any, Any>(TransitKeys.TYPE to eventType)
        windowState?.let { data[TransitKeys.WINDOW_STATE] = it }
        x?.let { data[TransitKeys.X] = it }
        y?.let { data[TransitKeys.Y] = it }
        width?.let { data[TransitKeys.WIDTH] = it }
        height?.let { data[TransitKeys.HEIGHT] = it }

        sendEvent(EventType.WINDOW, data)
    }

    /**
     * Send a request message (for forwarding commands from video streams)
     */
    fun sendRequest(
        action: String,
        data: Map<String, Any>,
    ) {
        try {
            val requestMsg =
                createMessage(
                    MessageType.REQUEST,
                    mapOf(
                        TransitKeys.ACTION to action,
                        TransitKeys.PROCESS to processType,
                    ) + data,
                )
            kotlinx.coroutines.GlobalScope.launch {
                transitComm.sendMessage(requestMsg)
            }
        } catch (e: Exception) {
            logError("Failed to send request $action: ${e.message}")
        }
    }

    /**
     * Send a command message (for video stream commands in new nested format)
     */
    fun sendCommand(command: Map<Any, Any>) {
        try {
            val commandMsg =
                createMessage(
                    MessageType.COMMAND,
                    command, // Command is already in the correct nested format
                )
            kotlinx.coroutines.GlobalScope.launch {
                transitComm.sendMessage(commandMsg)
            }
        } catch (e: Exception) {
            logError("Failed to send command: ${e.message}")
        }
    }

    /**
     * Create a properly formatted Transit message
     */
    fun createMessage(
        msgType: MessageType,
        payload: Map<Any, Any>,
    ): Map<Any, Any> =
        mapOf(
            TransitKeys.MSG_TYPE to msgType.key,
            TransitKeys.MSG_ID to
                java.util.UUID
                    .randomUUID()
                    .toString(),
            TransitKeys.TIMESTAMP to System.currentTimeMillis(),
            TransitKeys.PAYLOAD to payload,
        )

    /**
     * Get a MessageBuilder for creating standardized messages
     */
    fun messageBuilder(): MessageBuilder = MessageBuilder(this)
}

/**
 * Builder class for creating standardized Transit messages.
 * Provides a fluent API for constructing messages with proper structure.
 */
class MessageBuilder(
    private val protocol: TransitMessageProtocol,
) {
    fun command(
        action: String,
        params: Map<String, Any> = emptyMap(),
    ) = protocol.createMessage(
        MessageType.COMMAND,
        mapOf(
            TransitKeys.ACTION to action,
            TransitKeys.PARAMS to params,
        ),
    )

    fun response(
        action: String,
        status: String,
        data: Map<Any, Any> = emptyMap(),
    ) = protocol.createMessage(
        MessageType.RESPONSE,
        mapOf(
            TransitKeys.ACTION to action,
            TransitKeys.STATUS to status,
            TransitKeys.DATA to data,
        ),
    )

    fun request(
        action: String,
        data: Map<Any, Any> = emptyMap(),
    ) = protocol.createMessage(
        MessageType.REQUEST,
        mapOf(
            TransitKeys.ACTION to action,
            TransitKeys.DATA to data,
        ) + mapOf(TransitKeys.PROCESS to protocol.processType),
    )

    fun event(
        eventType: EventType,
        data: Map<Any, Any>,
    ) = protocol.createMessage(
        MessageType.EVENT,
        mapOf(TransitKeys.TYPE to eventType.key) + data,
    )

    fun gestureEvent(
        gestureType: EventType,
        timestamp: Long,
        canvasWidth: Int,
        canvasHeight: Int,
        aspectRatio: Double,
        streamType: String,
        additionalData: Map<Any, Any> = emptyMap(),
    ) = event(
        EventType.GESTURE,
        mapOf(
            TransitKeys.TYPE to gestureType.key,
            TransitKeys.TIMESTAMP to timestamp,
            TransitKeys.CANVAS_WIDTH to canvasWidth,
            TransitKeys.CANVAS_HEIGHT to canvasHeight,
            TransitKeys.ASPECT_RATIO to aspectRatio,
            TransitKeys.STREAM_TYPE to streamType,
        ) + additionalData,
    )

    fun navigationEvent(
        navType: EventType,
        x: Int,
        y: Int,
        ndcX: Double,
        ndcY: Double,
        frameTimestamp: Long,
        frameDuration: Long,
        canvasWidth: Int,
        canvasHeight: Int,
    ) = event(
        EventType.NAVIGATION,
        mapOf(
            TransitKeys.NAV_TYPE to navType.key,
            TransitKeys.X to x,
            TransitKeys.Y to y,
            TransitKeys.NDC_X to ndcX,
            TransitKeys.NDC_Y to ndcY,
            TransitKeys.FRAME_TIMESTAMP to frameTimestamp,
            TransitKeys.FRAME_DURATION to frameDuration,
            TransitKeys.CANVAS_WIDTH to canvasWidth,
            TransitKeys.CANVAS_HEIGHT to canvasHeight,
        ),
    )

    fun windowEvent(
        windowType: EventType,
        windowState: Int? = null,
        x: Int? = null,
        y: Int? = null,
        width: Int? = null,
        height: Int? = null,
    ): Map<Any, Any> {
        val data = mutableMapOf<Any, Any>(TransitKeys.TYPE to windowType.key)
        windowState?.let { data[TransitKeys.WINDOW_STATE] = it }
        x?.let { data[TransitKeys.X] = it }
        y?.let { data[TransitKeys.Y] = it }
        width?.let { data[TransitKeys.WIDTH] = it }
        height?.let { data[TransitKeys.HEIGHT] = it }

        return event(EventType.WINDOW, data)
    }

    fun log(
        level: String,
        message: String,
    ) = protocol.createMessage(
        MessageType.LOG,
        mapOf(
            TransitKeys.LEVEL to level,
            TransitKeys.MESSAGE to message,
            TransitKeys.PROCESS to protocol.processType,
        ),
    )

    fun error(
        context: String,
        error: String,
        exception: Exception? = null,
    ): Map<Any, Any> {
        val payload =
            mutableMapOf<Any, Any>(
                TransitKeys.CONTEXT to context,
                TransitKeys.ERROR to error,
                TransitKeys.PROCESS to protocol.processType,
            )

        exception?.let { ex ->
            payload[TransitKeys.CLASS] = ex.javaClass.name
            payload[TransitKeys.STACK_TRACE] = ex.stackTraceToString()
        }

        return protocol.createMessage(MessageType.ERROR, payload)
    }

    fun metric(
        name: String,
        value: Any,
    ) = protocol.createMessage(
        MessageType.METRIC,
        mapOf(
            TransitKeys.NAME to name,
            TransitKeys.VALUE to value,
            TransitKeys.PROCESS to protocol.processType,
        ),
    )

    fun status(
        status: String,
        details: Map<String, Any> = emptyMap(),
    ) = protocol.createMessage(
        MessageType.STATUS,
        mapOf(
            TransitKeys.STATUS to status,
            TransitKeys.PROCESS to protocol.processType,
        ) + details,
    )
}
