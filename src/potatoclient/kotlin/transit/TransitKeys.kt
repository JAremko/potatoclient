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
    val COMMAND_TYPE: Keyword = TransitFactory.keyword("command-type")
    val ROTARY: Keyword = TransitFactory.keyword("rotary")
    val CV: Keyword = TransitFactory.keyword("cv")
    val HEAT_CAMERA: Keyword = TransitFactory.keyword("heat-camera")
    val DAY_CAMERA: Keyword = TransitFactory.keyword("day-camera")
    val GUI: Keyword = TransitFactory.keyword("gui")
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
    
    // Additional keys from MessageKeys.java (camelCase versions)
    val STREAM_ID: Keyword = TransitFactory.keyword("streamId")
}
