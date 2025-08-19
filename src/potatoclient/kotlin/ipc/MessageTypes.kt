package potatoclient.kotlin.ipc

/**
 * Message types and keys for JSON-based IPC communication
 */
object MessageTypes {
    // Message type field
    const val MSG_TYPE = "msg-type"
    const val TIMESTAMP = "timestamp"
    
    // Core message types
    const val COMMAND = "command"
    const val EVENT = "event"
    const val LOG = "log"
    const val METRIC = "metric"
    const val ERROR = "error"
}

/**
 * Event types for various system events
 */
object EventTypes {
    const val TYPE = "type"
    
    // Main event types
    const val GESTURE = "gesture"
    const val WINDOW = "window"
    const val FRAME = "frame"
    const val CONNECTION = "connection"
    
    // Gesture subtypes (for gesture-type field)
    const val GESTURE_TYPE = "gesture-type"
    const val TAP = "tap"
    const val DOUBLE_TAP = "double-tap"
    const val PAN_START = "pan-start"
    const val PAN_MOVE = "pan-move"
    const val PAN_STOP = "pan-stop"
    
    // Window actions (for action field)
    const val ACTION = "action"
    const val MINIMIZE = "minimize"
    const val MAXIMIZE = "maximize"
    const val RESTORE = "restore"
    const val RESIZE = "resize"
    const val CLOSE_REQUEST = "close-request"
    const val FOCUS = "focus"
    const val BLUR = "blur"
    const val WINDOW_MOVE = "window-move"
}

/**
 * Common message keys
 */
object MessageKeys {
    // Coordinate fields
    const val X = "x"
    const val Y = "y"
    const val NDC_X = "ndc-x"
    const val NDC_Y = "ndc-y"
    const val WIDTH = "width"
    const val HEIGHT = "height"
    const val DELTA_X = "delta-x"
    const val DELTA_Y = "delta-y"
    
    // Stream identification
    const val STREAM_TYPE = "stream-type"
    const val STREAM_HEAT = "heat"
    const val STREAM_DAY = "day"
    
    // Command fields
    const val ACTION = "action"
    const val AZIMUTH_SPEED = "azimuth-speed"
    const val ELEVATION_SPEED = "elevation-speed"
    const val AZIMUTH_DIRECTION = "azimuth-direction"
    
    // Log fields
    const val LEVEL = "level"
    const val MESSAGE = "message"
    const val PROCESS = "process"
    const val DATA = "data"
    
    // Error fields
    const val ERROR = "error"
    const val DETAILS = "details"
    const val STACK_TRACE = "stack-trace"
}