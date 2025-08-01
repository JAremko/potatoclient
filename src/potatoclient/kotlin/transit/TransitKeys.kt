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
}
