package potatoclient.kotlin.events

import potatoclient.kotlin.gestures.RotaryDirection
import potatoclient.kotlin.gestures.StreamType
import potatoclient.kotlin.transit.TransitKeys

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
    ): Map<Any, Any> =
        mapOf(
            TransitKeys.ROTARY to
                mapOf(
                    TransitKeys.GOTO_NDC to
                        mapOf(
                            TransitKeys.CHANNEL to streamType.toKeyword(),
                            TransitKeys.X to ndcX,
                            TransitKeys.Y to ndcY,
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
    ): Map<Any, Any> {
        val params =
            mutableMapOf<Any, Any>(
                TransitKeys.CHANNEL to streamType.toKeyword(),
                TransitKeys.X to ndcX,
                TransitKeys.Y to ndcY,
            )
        frameTimestamp?.let { params[TransitKeys.FRAME_TIME] = it }

        return mapOf(
            TransitKeys.CV to
                mapOf(
                    TransitKeys.START_TRACK_NDC to params,
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
    ): Map<Any, Any> =
        mapOf(
            TransitKeys.ROTARY to
                mapOf(
                    TransitKeys.SET_VELOCITY to
                        mapOf(
                            TransitKeys.AZIMUTH_SPEED to azSpeed,
                            TransitKeys.ELEVATION_SPEED to elSpeed,
                            TransitKeys.AZIMUTH_DIRECTION to azDir.toKeyword(),
                            TransitKeys.ELEVATION_DIRECTION to elDir.toKeyword(),
                        ),
                ),
        )

    /**
     * Create a rotary halt command
     * Old: {"action": "rotary-halt", "params": {}}
     * New: {"rotary": {"halt": {}}}
     */
    fun rotaryHalt(): Map<Any, Any> =
        mapOf(
            TransitKeys.ROTARY to
                mapOf(
                    TransitKeys.HALT to emptyMap<Any, Any>(),
                ),
        )

    /**
     * Create a heat camera zoom command
     * Old: {"action": "heat-camera-next-zoom-table-pos", "params": {}}
     * New: {"heat-camera": {"next-zoom-table-pos": {}}}
     */
    fun heatCameraNextZoom(): Map<Any, Any> =
        mapOf(
            TransitKeys.HEAT_CAMERA to
                mapOf(
                    TransitKeys.NEXT_ZOOM_TABLE_POS to emptyMap<Any, Any>(),
                ),
        )

    /**
     * Create a heat camera zoom out command
     * New: {"heat-camera": {"prev-zoom-table-pos": {}}}
     */
    fun heatCameraPrevZoom(): Map<Any, Any> =
        mapOf(
            TransitKeys.HEAT_CAMERA to
                mapOf(
                    TransitKeys.PREV_ZOOM_TABLE_POS to emptyMap<Any, Any>(),
                ),
        )

    /**
     * Create a day camera zoom command
     * New: {"day-camera": {"next-zoom-table-pos": {}}}
     */
    fun dayCameraNextZoom(): Map<Any, Any> =
        mapOf(
            TransitKeys.DAY_CAMERA to
                mapOf(
                    TransitKeys.NEXT_ZOOM_TABLE_POS to emptyMap<Any, Any>(),
                ),
        )

    /**
     * Create a day camera zoom out command
     * New: {"day-camera": {"prev-zoom-table-pos": {}}}
     */
    fun dayCameraPrevZoom(): Map<Any, Any> =
        mapOf(
            TransitKeys.DAY_CAMERA to
                mapOf(
                    TransitKeys.PREV_ZOOM_TABLE_POS to emptyMap<Any, Any>(),
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
