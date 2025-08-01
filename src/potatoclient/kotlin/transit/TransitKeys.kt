package potatoclient.kotlin.transit

import com.cognitect.transit.Keyword
import com.cognitect.transit.impl.KeywordImpl

/**
 * Pre-created Transit keyword instances for efficient map access.
 * Using constants avoids creating new KeywordImpl instances on each access.
 */
object TransitKeys {
    // Message envelope keys
    val MSG_TYPE: Keyword = KeywordImpl("msg-type")
    val MSG_ID: Keyword = KeywordImpl("msg-id")
    val TIMESTAMP: Keyword = KeywordImpl("timestamp")
    val PAYLOAD: Keyword = KeywordImpl("payload")

    // Common payload keys
    val ACTION: Keyword = KeywordImpl("action")
    val PARAMS: Keyword = KeywordImpl("params")
    val STATUS: Keyword = KeywordImpl("status")
    val CHANNEL: Keyword = KeywordImpl("channel")
    val ERROR: Keyword = KeywordImpl("error")
    val MESSAGE: Keyword = KeywordImpl("message")
    val LEVEL: Keyword = KeywordImpl("level")
    val PROCESS: Keyword = KeywordImpl("process")

    // State-specific keys
    val SYSTEM: Keyword = KeywordImpl("system")
    val BATTERY_LEVEL: Keyword = KeywordImpl("battery-level")
    val HAS_DATA: Keyword = KeywordImpl("has-data")
    val PROTO_RECEIVED: Keyword = KeywordImpl("proto-received")

    // Stats keys
    val RECEIVED: Keyword = KeywordImpl("received")
    val SENT: Keyword = KeywordImpl("sent")
    val WS_CONNECTED: Keyword = KeywordImpl("ws-connected")
    val RATE_HZ: Keyword = KeywordImpl("rate-hz")

    // Coordinate keys (for gestures)
    val X: Keyword = KeywordImpl("x")
    val Y: Keyword = KeywordImpl("y")
    val NDC_X: Keyword = KeywordImpl("ndc-x")
    val NDC_Y: Keyword = KeywordImpl("ndc-y")

    // Velocity command keys
    val AZIMUTH_SPEED: Keyword = KeywordImpl("azimuth-speed")
    val ELEVATION_SPEED: Keyword = KeywordImpl("elevation-speed")
    val AZIMUTH_DIRECTION: Keyword = KeywordImpl("azimuth-direction")
    val ELEVATION_DIRECTION: Keyword = KeywordImpl("elevation-direction")

    // Frame-related keys
    val FRAME_TIMESTAMP: Keyword = KeywordImpl("frame-timestamp")
    val FRAME_DURATION: Keyword = KeywordImpl("frame-duration")
}
