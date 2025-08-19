package potatoclient.kotlin.transit

import com.cognitect.transit.Keyword
import com.cognitect.transit.TransitFactory

/**
 * Pre-created Transit keyword instances for efficient map access.
 * Using constants avoids creating new keyword instances on each access.
 */
object TransitKeys {
    // Message envelope keys
    val MSG_TYPE: Keyword = TransitFactory.keyword("msg-type")
    val MSG_ID: Keyword = TransitFactory.keyword("msg-id")
    val TIMESTAMP: Keyword = TransitFactory.keyword("timestamp")
    val PAYLOAD: Keyword = TransitFactory.keyword("payload")

    // Common payload keys
    val ACTION: Keyword = TransitFactory.keyword("action")
    val PARAMS: Keyword = TransitFactory.keyword("params")
    val STATUS: Keyword = TransitFactory.keyword("status")
    val CHANNEL: Keyword = TransitFactory.keyword("channel")
    val ERROR: Keyword = TransitFactory.keyword("error")
    val MESSAGE: Keyword = TransitFactory.keyword("message")
    val LEVEL: Keyword = TransitFactory.keyword("level")
    val PROCESS: Keyword = TransitFactory.keyword("process")

    // State-specific keys
    val SYSTEM: Keyword = TransitFactory.keyword("system")
    val BATTERY_LEVEL: Keyword = TransitFactory.keyword("battery-level")
    val HAS_DATA: Keyword = TransitFactory.keyword("has-data")
    val PROTO_RECEIVED: Keyword = TransitFactory.keyword("proto-received")

    // Stats keys
    val RECEIVED: Keyword = TransitFactory.keyword("received")
    val SENT: Keyword = TransitFactory.keyword("sent")
    val WS_CONNECTED: Keyword = TransitFactory.keyword("ws-connected")
    val RATE_HZ: Keyword = TransitFactory.keyword("rate-hz")

    // Coordinate keys (for gestures)
    val X: Keyword = TransitFactory.keyword("x")
    val Y: Keyword = TransitFactory.keyword("y")
    val NDC_X: Keyword = TransitFactory.keyword("ndc-x")
    val NDC_Y: Keyword = TransitFactory.keyword("ndc-y")

    // Velocity command keys
    val AZIMUTH_SPEED: Keyword = TransitFactory.keyword("azimuth-speed")
    val ELEVATION_SPEED: Keyword = TransitFactory.keyword("elevation-speed")
    val AZIMUTH_DIRECTION: Keyword = TransitFactory.keyword("azimuth-direction")
    val ELEVATION_DIRECTION: Keyword = TransitFactory.keyword("elevation-direction")

    // Frame-related keys
    val FRAME_TIMESTAMP: Keyword = TransitFactory.keyword("frame-timestamp")
    val FRAME_DURATION: Keyword = TransitFactory.keyword("frame-duration")

    // Metric keys
    val NAME: Keyword = TransitFactory.keyword("name")
    val VALUE: Keyword = TransitFactory.keyword("value")
    val CONTEXT: Keyword = TransitFactory.keyword("context")

    // Event type key
    val TYPE: Keyword = TransitFactory.keyword("type")
    val GESTURE: Keyword = TransitFactory.keyword("gesture")
    
    // Additional keys from MessageKeys.java
    val DATA: Keyword = TransitFactory.keyword("data")
    val CLASS: Keyword = TransitFactory.keyword("class")
    val STACK_TRACE: Keyword = TransitFactory.keyword("stack-trace")
    val WIDTH: Keyword = TransitFactory.keyword("width")
    val HEIGHT: Keyword = TransitFactory.keyword("height")
    val CANVAS_WIDTH: Keyword = TransitFactory.keyword("canvas-width")
    val CANVAS_HEIGHT: Keyword = TransitFactory.keyword("canvas-height")
    val WINDOW_STATE: Keyword = TransitFactory.keyword("window-state")
    val NAV_TYPE: Keyword = TransitFactory.keyword("nav-type")
    val DETAILS: Keyword = TransitFactory.keyword("details")
    val ASPECT_RATIO: Keyword = TransitFactory.keyword("aspect-ratio")
    val STREAM_TYPE: Keyword = TransitFactory.keyword("stream-type")
    val GESTURE_TYPE: Keyword = TransitFactory.keyword("gesture-type")
    val STATE: Keyword = TransitFactory.keyword("state")
    
    // Command-specific keys
    val COMMAND: Keyword = TransitFactory.keyword("command")
    val EVENT: Keyword = TransitFactory.keyword("event")
    val LOG: Keyword = TransitFactory.keyword("log")
    val METRIC: Keyword = TransitFactory.keyword("metric")
    val ROTARY: Keyword = TransitFactory.keyword("rotary")
    val CV: Keyword = TransitFactory.keyword("cv")
    val HEAT: Keyword = TransitFactory.keyword("heat")
    val DAY: Keyword = TransitFactory.keyword("day")
    val HEAT_CAMERA: Keyword = TransitFactory.keyword("heat-camera")
    val DAY_CAMERA: Keyword = TransitFactory.keyword("day-camera")
    val HEAT_CAMERA_NEXT_ZOOM: Keyword = TransitFactory.keyword("heat-camera-next-zoom")
    val HEAT_CAMERA_PREV_ZOOM: Keyword = TransitFactory.keyword("heat-camera-prev-zoom")
    val DAY_CAMERA_NEXT_ZOOM: Keyword = TransitFactory.keyword("day-camera-next-zoom")
    val DAY_CAMERA_PREV_ZOOM: Keyword = TransitFactory.keyword("day-camera-prev-zoom")
    val GUI: Keyword = TransitFactory.keyword("gui")
    val ROTARY_GOTO_NDC: Keyword = TransitFactory.keyword("rotary-goto-ndc")
    val ROTARY_SET_VELOCITY: Keyword = TransitFactory.keyword("rotary-set-velocity")
    val ROTARY_HALT: Keyword = TransitFactory.keyword("rotary-halt")
    val CV_START_TRACK_NDC: Keyword = TransitFactory.keyword("cv-start-track-ndc")
    val GOTO_NDC: Keyword = TransitFactory.keyword("goto-ndc")
    val START_TRACK_NDC: Keyword = TransitFactory.keyword("start-track-ndc")
    val STOP_TRACK: Keyword = TransitFactory.keyword("stop-track")
    val FRAME_TIME: Keyword = TransitFactory.keyword("frame-time")
    val VELOCITY: Keyword = TransitFactory.keyword("velocity")
    val SET_VELOCITY: Keyword = TransitFactory.keyword("set-velocity")
    val HALT: Keyword = TransitFactory.keyword("halt")
    val STOP: Keyword = TransitFactory.keyword("stop")
    val MODE: Keyword = TransitFactory.keyword("mode")
    val NEXT_ZOOM_TABLE_POS: Keyword = TransitFactory.keyword("next-zoom-table-pos")
    val PREV_ZOOM_TABLE_POS: Keyword = TransitFactory.keyword("prev-zoom-table-pos")
    val SET_FOCUS_MODE: Keyword = TransitFactory.keyword("set-focus-mode")
    val SELECT_VIDEO_CHANNEL: Keyword = TransitFactory.keyword("select-video-channel")
    val DELTA_X: Keyword = TransitFactory.keyword("delta-x")
    val DELTA_Y: Keyword = TransitFactory.keyword("delta-y")
    val NDC_DELTA_X: Keyword = TransitFactory.keyword("ndc-delta-x")
    val NDC_DELTA_Y: Keyword = TransitFactory.keyword("ndc-delta-y")
    val CLOCKWISE: Keyword = TransitFactory.keyword("clockwise")
    val COUNTER_CLOCKWISE: Keyword = TransitFactory.keyword("counter-clockwise")
    
    // Response type keywords
    val PONG: Keyword = TransitFactory.keyword("pong")
    val ACK: Keyword = TransitFactory.keyword("ack")
    // ERROR already defined above
    
    // Status keywords
    val STATUS_STARTING: Keyword = TransitFactory.keyword("starting")
    val STATUS_TEST_MODE_READY: Keyword = TransitFactory.keyword("test-mode-ready")
    val STATUS_SENT: Keyword = TransitFactory.keyword("sent")
    val STATUS_STOPPED: Keyword = TransitFactory.keyword("stopped")
    val STATUS_SHUTTING_DOWN: Keyword = TransitFactory.keyword("shutting-down")
    val STATUS_TEST_MODE_STOPPED: Keyword = TransitFactory.keyword("test-mode-stopped")
    
    // Common field keywords (removing duplicates already defined above)
    val TEST_MODE: Keyword = TransitFactory.keyword("test-mode")
    val ORIGINAL_MSG_ID: Keyword = TransitFactory.keyword("original-msg-id")
    val PROTO_TYPE: Keyword = TransitFactory.keyword("proto-type")
    val SIZE: Keyword = TransitFactory.keyword("size")
    val RATE_LIMIT_HZ: Keyword = TransitFactory.keyword("rate-limit-hz")
    
    // Window event keys
    val WINDOW: Keyword = TransitFactory.keyword("window")
    val WINDOW_MOVE: Keyword = TransitFactory.keyword("window-move")
    val MINIMIZE: Keyword = TransitFactory.keyword("minimize")
    val MAXIMIZE: Keyword = TransitFactory.keyword("maximize")
    val RESTORE: Keyword = TransitFactory.keyword("restore")
    val RESIZE: Keyword = TransitFactory.keyword("resize")
    val CLOSE_REQUEST: Keyword = TransitFactory.keyword("close-request")
    val FOCUS: Keyword = TransitFactory.keyword("focus")
    val BLUR: Keyword = TransitFactory.keyword("blur")
    
    // Connection event keys
    val CONNECTION: Keyword = TransitFactory.keyword("connection")
    val CONNECTED: Keyword = TransitFactory.keyword("connected")
    val DISCONNECTED: Keyword = TransitFactory.keyword("disconnected")
    val RECONNECTING: Keyword = TransitFactory.keyword("reconnecting")
    val TIMEOUT: Keyword = TransitFactory.keyword("timeout")
    val CONNECTION_ERROR: Keyword = TransitFactory.keyword("connection-error")
    
    // Additional keys from MessageKeys.java (camelCase versions)
    val STREAM_ID: Keyword = TransitFactory.keyword("streamId")
}
