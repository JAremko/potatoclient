package potatoclient.kotlin.ipc

import com.cognitect.transit.Keyword
import com.cognitect.transit.TransitFactory

/**
 * Centralized definition of all Transit Keywords used in IPC communication.
 * These keywords match the protocol defined in CLAUDE.md.
 */
object IpcKeys {
    // Message envelope keys
    @JvmField val MSG_TYPE: Keyword = TransitFactory.keyword("msg-type")

    // MSG_ID removed - not needed for our IPC protocol
    // @JvmField val MSG_ID: Keyword = TransitFactory.keyword("msg-id")

    @JvmField val TIMESTAMP: Keyword = TransitFactory.keyword("timestamp")

    @JvmField val PAYLOAD: Keyword = TransitFactory.keyword("payload")

    // Message types
    @JvmField val EVENT: Keyword = TransitFactory.keyword("event")

    @JvmField val COMMAND: Keyword = TransitFactory.keyword("command")

    @JvmField val LOG: Keyword = TransitFactory.keyword("log")

    @JvmField val METRIC: Keyword = TransitFactory.keyword("metric")

    // Common keys
    @JvmField val TYPE: Keyword = TransitFactory.keyword("type")

    @JvmField val ACTION: Keyword = TransitFactory.keyword("action")

    @JvmField val STREAM_TYPE: Keyword = TransitFactory.keyword("stream-type")

    @JvmField val STREAM_ID: Keyword = TransitFactory.keyword("stream-id")

    @JvmField val PROCESS: Keyword = TransitFactory.keyword("process")

    @JvmField val LEVEL: Keyword = TransitFactory.keyword("level")

    @JvmField val MESSAGE: Keyword = TransitFactory.keyword("message")

    @JvmField val DATA: Keyword = TransitFactory.keyword("data")

    @JvmField val DETAILS: Keyword = TransitFactory.keyword("details")

    // Event types
    @JvmField val GESTURE: Keyword = TransitFactory.keyword("gesture")

    @JvmField val WINDOW: Keyword = TransitFactory.keyword("window")

    @JvmField val CONNECTION: Keyword = TransitFactory.keyword("connection")

    // Gesture types
    @JvmField val TAP: Keyword = TransitFactory.keyword("tap")

    @JvmField val DOUBLE_TAP: Keyword = TransitFactory.keyword("double-tap")

    @JvmField val PAN_START: Keyword = TransitFactory.keyword("pan-start")

    @JvmField val PAN_MOVE: Keyword = TransitFactory.keyword("pan-move")

    @JvmField val PAN_STOP: Keyword = TransitFactory.keyword("pan-stop")

    @JvmField val WHEEL_UP: Keyword = TransitFactory.keyword("wheel-up")

    @JvmField val WHEEL_DOWN: Keyword = TransitFactory.keyword("wheel-down")

    // Window actions
    @JvmField val MINIMIZE: Keyword = TransitFactory.keyword("minimize")

    @JvmField val MAXIMIZE: Keyword = TransitFactory.keyword("maximize")

    @JvmField val RESTORE: Keyword = TransitFactory.keyword("restore")

    @JvmField val RESIZE: Keyword = TransitFactory.keyword("resize")

    @JvmField val FOCUS: Keyword = TransitFactory.keyword("focus")

    @JvmField val BLUR: Keyword = TransitFactory.keyword("blur")

    @JvmField val WINDOW_MOVE: Keyword = TransitFactory.keyword("window-move")

    @JvmField val CLOSE_REQUEST: Keyword = TransitFactory.keyword("close-request")

    // Connection actions
    @JvmField val CONNECTED: Keyword = TransitFactory.keyword("connected")

    @JvmField val DISCONNECTED: Keyword = TransitFactory.keyword("disconnected")

    @JvmField val TIMEOUT: Keyword = TransitFactory.keyword("timeout")

    @JvmField val RECONNECTING: Keyword = TransitFactory.keyword("reconnecting")

    @JvmField val CONNECTION_ERROR: Keyword = TransitFactory.keyword("connection-error")

    // Stream types
    @JvmField val HEAT: Keyword = TransitFactory.keyword("heat")

    @JvmField val DAY: Keyword = TransitFactory.keyword("day")

    // Coordinate keys
    @JvmField val X: Keyword = TransitFactory.keyword("x")

    @JvmField val Y: Keyword = TransitFactory.keyword("y")

    @JvmField val NDC_X: Keyword = TransitFactory.keyword("ndc-x")

    @JvmField val NDC_Y: Keyword = TransitFactory.keyword("ndc-y")

    @JvmField val WIDTH: Keyword = TransitFactory.keyword("width")

    @JvmField val HEIGHT: Keyword = TransitFactory.keyword("height")

    @JvmField val DELTA_X: Keyword = TransitFactory.keyword("delta-x")

    @JvmField val DELTA_Y: Keyword = TransitFactory.keyword("delta-y")

    // Gesture-specific keys
    @JvmField val GESTURE_TYPE: Keyword = TransitFactory.keyword("gesture-type")

    @JvmField val FRAME_TIMESTAMP: Keyword = TransitFactory.keyword("frame-timestamp")

    @JvmField val SCROLL_AMOUNT: Keyword = TransitFactory.keyword("scroll-amount")

    // Log levels
    @JvmField val DEBUG: Keyword = TransitFactory.keyword("debug")

    @JvmField val INFO: Keyword = TransitFactory.keyword("info")

    @JvmField val WARN: Keyword = TransitFactory.keyword("warn")

    @JvmField val ERROR: Keyword = TransitFactory.keyword("error")

    /**
     * Helper function to create a keyword from a string.
     */
    @JvmStatic
    fun keyword(name: String): Keyword = TransitFactory.keyword(name)

    /**
     * Get stream type keyword from string.
     */
    @JvmStatic
    fun streamType(type: String): Keyword =
        when (type.lowercase()) {
            "heat" -> HEAT
            "day" -> DAY
            else -> keyword(type)
        }

    /**
     * Get log level keyword from string.
     */
    @JvmStatic
    fun logLevel(level: String): Keyword =
        when (level.uppercase()) {
            "DEBUG" -> DEBUG
            "INFO" -> INFO
            "WARN" -> WARN
            "ERROR" -> ERROR
            else -> INFO
        }
}
