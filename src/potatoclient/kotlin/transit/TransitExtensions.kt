package potatoclient.kotlin.transit

import com.cognitect.transit.Keyword

/**
 * Kotlin extension properties for cleaner access to Transit maps with keyword keys.
 * These extensions make working with Transit's keyword-based maps more idiomatic in Kotlin.
 */
val Map<*, *>.msgType: Any?
    get() = this[TransitKeys.MSG_TYPE]

val Map<*, *>.msgId: String?
    get() = this[TransitKeys.MSG_ID] as? String

val Map<*, *>.timestamp: Long?
    get() = this[TransitKeys.TIMESTAMP] as? Long

val Map<*, *>.payload: Map<*, *>?
    get() = this[TransitKeys.PAYLOAD] as? Map<*, *>

// Payload extensions
val Map<*, *>.action: String?
    get() = this[TransitKeys.ACTION] as? String

val Map<*, *>.params: Map<*, *>?
    get() = this[TransitKeys.PARAMS] as? Map<*, *>

val Map<*, *>.status: String?
    get() = this[TransitKeys.STATUS] as? String

val Map<*, *>.channel: String?
    get() = this[TransitKeys.CHANNEL] as? String

val Map<*, *>.error: String?
    get() = this[TransitKeys.ERROR] as? String

val Map<*, *>.message: String?
    get() = this[TransitKeys.MESSAGE] as? String

val Map<*, *>.level: String?
    get() = this[TransitKeys.LEVEL] as? String

val Map<*, *>.process: String?
    get() = this[TransitKeys.PROCESS] as? String

// State-specific extensions
val Map<*, *>.system: Map<*, *>?
    get() = this[TransitKeys.SYSTEM] as? Map<*, *>

val Map<*, *>.batteryLevel: Number?
    get() = this[TransitKeys.BATTERY_LEVEL] as? Number

val Map<*, *>.hasData: Boolean?
    get() = this[TransitKeys.HAS_DATA] as? Boolean

val Map<*, *>.protoReceived: Boolean?
    get() = this[TransitKeys.PROTO_RECEIVED] as? Boolean

// Stats extensions
val Map<*, *>.received: Number?
    get() = this[TransitKeys.RECEIVED] as? Number

val Map<*, *>.sent: Number?
    get() = this[TransitKeys.SENT] as? Number

val Map<*, *>.wsConnected: Boolean?
    get() = this[TransitKeys.WS_CONNECTED] as? Boolean

val Map<*, *>.rateHz: Number?
    get() = this[TransitKeys.RATE_HZ] as? Number

// Coordinate extensions (for gestures/commands)
val Map<*, *>.x: Number?
    get() = this[TransitKeys.X] as? Number

val Map<*, *>.y: Number?
    get() = this[TransitKeys.Y] as? Number

val Map<*, *>.ndcX: Number?
    get() = this[TransitKeys.NDC_X] as? Number

val Map<*, *>.ndcY: Number?
    get() = this[TransitKeys.NDC_Y] as? Number

// Velocity command extensions
val Map<*, *>.azimuthSpeed: Number?
    get() = this[TransitKeys.AZIMUTH_SPEED] as? Number

val Map<*, *>.elevationSpeed: Number?
    get() = this[TransitKeys.ELEVATION_SPEED] as? Number

val Map<*, *>.azimuthDirection: String?
    get() = this[TransitKeys.AZIMUTH_DIRECTION] as? String

val Map<*, *>.elevationDirection: String?
    get() = this[TransitKeys.ELEVATION_DIRECTION] as? String

// Frame-related extensions
val Map<*, *>.frameTimestamp: Long?
    get() = this[TransitKeys.FRAME_TIMESTAMP] as? Long

val Map<*, *>.frameDuration: Long?
    get() = this[TransitKeys.FRAME_DURATION] as? Long

// Helper function to check if a message is of a specific type
fun Map<*, *>.isMessageType(type: Keyword): Boolean = this.msgType == type

// Helper to safely get nested values
inline fun <reified T> Map<*, *>.getNestedValue(vararg keys: Keyword): T? {
    var current: Any? = this
    for (key in keys) {
        current = (current as? Map<*, *>)?.get(key) ?: return null
    }
    return current as? T
}
