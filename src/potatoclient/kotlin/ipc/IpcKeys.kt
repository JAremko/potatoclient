package potatoclient.kotlin.ipc

import com.cognitect.transit.Keyword
import com.cognitect.transit.TransitFactory

/**
 * Centralized definition of all Transit Keywords used in IPC communication.
 * These keywords match the protocol defined in CLAUDE.md.
 */
object IpcKeys {
    // Message envelope keys
    @JvmField val MSG_TYPE = TransitFactory.keyword("msg-type")
    @JvmField val MSG_ID = TransitFactory.keyword("msg-id")
    @JvmField val TIMESTAMP = TransitFactory.keyword("timestamp")
    @JvmField val PAYLOAD = TransitFactory.keyword("payload")
    
    // Message types
    @JvmField val EVENT = TransitFactory.keyword("event")
    @JvmField val COMMAND = TransitFactory.keyword("command")
    @JvmField val LOG = TransitFactory.keyword("log")
    @JvmField val METRIC = TransitFactory.keyword("metric")
    
    // Common keys
    @JvmField val TYPE = TransitFactory.keyword("type")
    @JvmField val ACTION = TransitFactory.keyword("action")
    @JvmField val STREAM_TYPE = TransitFactory.keyword("stream-type")
    @JvmField val STREAM_ID = TransitFactory.keyword("stream-id")
    @JvmField val PROCESS = TransitFactory.keyword("process")
    @JvmField val LEVEL = TransitFactory.keyword("level")
    @JvmField val MESSAGE = TransitFactory.keyword("message")
    @JvmField val DATA = TransitFactory.keyword("data")
    @JvmField val DETAILS = TransitFactory.keyword("details")
    
    // Event types
    @JvmField val GESTURE = TransitFactory.keyword("gesture")
    @JvmField val WINDOW = TransitFactory.keyword("window")
    @JvmField val CONNECTION = TransitFactory.keyword("connection")
    
    // Gesture types
    @JvmField val TAP = TransitFactory.keyword("tap")
    @JvmField val DOUBLE_TAP = TransitFactory.keyword("double-tap")
    @JvmField val PAN_START = TransitFactory.keyword("pan-start")
    @JvmField val PAN_MOVE = TransitFactory.keyword("pan-move")
    @JvmField val PAN_STOP = TransitFactory.keyword("pan-stop")
    @JvmField val WHEEL_UP = TransitFactory.keyword("wheel-up")
    @JvmField val WHEEL_DOWN = TransitFactory.keyword("wheel-down")
    
    // Window actions
    @JvmField val MINIMIZE = TransitFactory.keyword("minimize")
    @JvmField val MAXIMIZE = TransitFactory.keyword("maximize")
    @JvmField val RESTORE = TransitFactory.keyword("restore")
    @JvmField val RESIZE = TransitFactory.keyword("resize")
    @JvmField val FOCUS = TransitFactory.keyword("focus")
    @JvmField val BLUR = TransitFactory.keyword("blur")
    @JvmField val WINDOW_MOVE = TransitFactory.keyword("window-move")
    @JvmField val CLOSE_REQUEST = TransitFactory.keyword("close-request")
    
    // Connection actions
    @JvmField val CONNECTED = TransitFactory.keyword("connected")
    @JvmField val DISCONNECTED = TransitFactory.keyword("disconnected")
    @JvmField val TIMEOUT = TransitFactory.keyword("timeout")
    @JvmField val RECONNECTING = TransitFactory.keyword("reconnecting")
    @JvmField val CONNECTION_ERROR = TransitFactory.keyword("connection-error")
    
    
    // Stream types
    @JvmField val HEAT = TransitFactory.keyword("heat")
    @JvmField val DAY = TransitFactory.keyword("day")
    
    // Coordinate keys
    @JvmField val X = TransitFactory.keyword("x")
    @JvmField val Y = TransitFactory.keyword("y")
    @JvmField val NDC_X = TransitFactory.keyword("ndc-x")
    @JvmField val NDC_Y = TransitFactory.keyword("ndc-y")
    @JvmField val WIDTH = TransitFactory.keyword("width")
    @JvmField val HEIGHT = TransitFactory.keyword("height")
    @JvmField val DELTA_X = TransitFactory.keyword("delta-x")
    @JvmField val DELTA_Y = TransitFactory.keyword("delta-y")
    
    // Gesture-specific keys
    @JvmField val GESTURE_TYPE = TransitFactory.keyword("gesture-type")
    @JvmField val FRAME_TIMESTAMP = TransitFactory.keyword("frame-timestamp")
    
    
    // Log levels
    @JvmField val DEBUG = TransitFactory.keyword("debug")
    @JvmField val INFO = TransitFactory.keyword("info")
    @JvmField val WARN = TransitFactory.keyword("warn")
    @JvmField val ERROR = TransitFactory.keyword("error")
    
    /**
     * Helper function to create a keyword from a string.
     */
    @JvmStatic
    fun keyword(name: String): Keyword = TransitFactory.keyword(name)
    
    /**
     * Get stream type keyword from string.
     */
    @JvmStatic
    fun streamType(type: String): Keyword = when (type.lowercase()) {
        "heat" -> HEAT
        "day" -> DAY
        else -> keyword(type)
    }
    
    /**
     * Get log level keyword from string.
     */
    @JvmStatic
    fun logLevel(level: String): Keyword = when (level.uppercase()) {
        "DEBUG" -> DEBUG
        "INFO" -> INFO
        "WARN" -> WARN
        "ERROR" -> ERROR
        else -> INFO
    }
}