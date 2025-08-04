package potatoclient.kotlin.events

import potatoclient.kotlin.gestures.RotaryDirection
import potatoclient.kotlin.gestures.StreamType

/**
 * Builder for creating Transit commands in the new nested format.
 *
 * This replaces the old action/params format with the new structure
 * that matches the protobuf command hierarchy.
 */
object CommandBuilder {
    /**
     * Create a rotary goto NDC command
     * Old: {"action": "rotary-goto-ndc", "params": {...}}
     * New: {"rotary": {"goto-ndc": {"channel": "heat", "x": 0.5, "y": -0.3}}}
     */
    fun rotaryGotoNDC(
        streamType: StreamType,
        ndcX: Double,
        ndcY: Double,
    ): Map<String, Any> =
        mapOf(
            "rotary" to
                mapOf(
                    "goto-ndc" to
                        mapOf(
                            "channel" to streamType.toKeyword(),
                            "x" to ndcX,
                            "y" to ndcY,
                        ),
                ),
        )

    /**
     * Create a CV start track NDC command
     * Old: {"action": "cv-start-track-ndc", "params": {...}}
     * New: {"cv": {"start-track-ndc": {"channel": "heat", "x": 0.5, "y": -0.3, "frame-time": 12345}}}
     */
    fun cvStartTrackNDC(
        streamType: StreamType,
        ndcX: Double,
        ndcY: Double,
        frameTimestamp: Long? = null,
    ): Map<String, Any> {
        val params =
            mutableMapOf(
                "channel" to streamType.toKeyword(),
                "x" to ndcX,
                "y" to ndcY,
            )
        frameTimestamp?.let { params["frame-time"] = it }

        return mapOf(
            "cv" to
                mapOf(
                    "start-track-ndc" to params,
                ),
        )
    }

    /**
     * Create a rotary set velocity command
     * Old: {"action": "rotary-set-velocity", "params": {...}}
     * New: {"rotary": {"set-velocity": {"azimuth-speed": 0.5, ...}}}
     */
    fun rotarySetVelocity(
        azSpeed: Double,
        elSpeed: Double,
        azDir: RotaryDirection,
        elDir: RotaryDirection,
    ): Map<String, Any> =
        mapOf(
            "rotary" to
                mapOf(
                    "set-velocity" to
                        mapOf(
                            "azimuth-speed" to azSpeed,
                            "elevation-speed" to elSpeed,
                            "azimuth-direction" to azDir.toKeyword(),
                            "elevation-direction" to elDir.toKeyword(),
                        ),
                ),
        )

    /**
     * Create a rotary halt command
     * Old: {"action": "rotary-halt", "params": {}}
     * New: {"rotary": {"halt": {}}}
     */
    fun rotaryHalt(): Map<String, Any> =
        mapOf(
            "rotary" to
                mapOf(
                    "halt" to emptyMap<String, Any>(),
                ),
        )

    /**
     * Create a heat camera zoom command
     * Old: {"action": "heat-camera-next-zoom-table-pos", "params": {}}
     * New: {"heat-camera": {"next-zoom-table-pos": {}}}
     */
    fun heatCameraNextZoom(): Map<String, Any> =
        mapOf(
            "heat-camera" to
                mapOf(
                    "next-zoom-table-pos" to emptyMap<String, Any>(),
                ),
        )

    /**
     * Create a heat camera zoom out command
     * New: {"heat-camera": {"prev-zoom-table-pos": {}}}
     */
    fun heatCameraPrevZoom(): Map<String, Any> =
        mapOf(
            "heat-camera" to
                mapOf(
                    "prev-zoom-table-pos" to emptyMap<String, Any>(),
                ),
        )

    /**
     * Create a day camera zoom command
     * New: {"day-camera": {"next-zoom-table-pos": {}}}
     */
    fun dayCameraNextZoom(): Map<String, Any> =
        mapOf(
            "day-camera" to
                mapOf(
                    "next-zoom-table-pos" to emptyMap<String, Any>(),
                ),
        )

    /**
     * Create a day camera zoom out command
     * New: {"day-camera": {"prev-zoom-table-pos": {}}}
     */
    fun dayCameraPrevZoom(): Map<String, Any> =
        mapOf(
            "day-camera" to
                mapOf(
                    "prev-zoom-table-pos" to emptyMap<String, Any>(),
                ),
        )
}

/**
 * Extension functions for converting enums to Transit keywords
 */
fun StreamType.toKeyword(): String =
    when (this) {
        StreamType.HEAT -> "heat"
        StreamType.DAY -> "day"
    }

fun RotaryDirection.toKeyword(): String =
    when (this) {
        RotaryDirection.CLOCKWISE -> "clockwise"
        RotaryDirection.COUNTER_CLOCKWISE -> "counter-clockwise"
    }
