package potatoclient.kotlin.gestures

/**
 * Simplified gesture events that include frame timestamp data.
 * These are sent directly to Clojure for interpretation.
 */
sealed class GestureEvent {
    abstract val x: Int
    abstract val y: Int
    abstract val timestamp: Long
    abstract val frameTimestamp: Long

    data class Tap(
        override val x: Int,
        override val y: Int,
        override val timestamp: Long,
        override val frameTimestamp: Long
    ) : GestureEvent()
    
    data class DoubleTap(
        override val x: Int,
        override val y: Int,
        override val timestamp: Long,
        override val frameTimestamp: Long
    ) : GestureEvent()
    
    data class PanStart(
        override val x: Int,
        override val y: Int,
        override val timestamp: Long,
        override val frameTimestamp: Long
    ) : GestureEvent()
    
    data class PanMove(
        override val x: Int,
        override val y: Int,
        val deltaX: Int,
        val deltaY: Int,
        override val timestamp: Long,
        override val frameTimestamp: Long
    ) : GestureEvent()
    
    data class PanStop(
        override val x: Int,
        override val y: Int,
        override val timestamp: Long,
        override val frameTimestamp: Long
    ) : GestureEvent()
    
    data class WheelUp(
        override val x: Int,
        override val y: Int,
        val scrollAmount: Int,
        override val timestamp: Long,
        override val frameTimestamp: Long
    ) : GestureEvent()
    
    data class WheelDown(
        override val x: Int,
        override val y: Int,
        val scrollAmount: Int,
        override val timestamp: Long,
        override val frameTimestamp: Long
    ) : GestureEvent()
}