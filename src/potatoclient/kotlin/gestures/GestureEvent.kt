package potatoclient.kotlin.gestures

import potatoclient.transit.EventType

enum class SwipeDirection {
    UP,
    DOWN,
    LEFT,
    RIGHT,
}

sealed class GestureEvent {
    abstract val timestamp: Long

    data class Tap(
        val x: Int,
        val y: Int,
        override val timestamp: Long,
    ) : GestureEvent()

    data class DoubleTap(
        val x: Int,
        val y: Int,
        override val timestamp: Long,
    ) : GestureEvent()

    data class PanStart(
        val x: Int,
        val y: Int,
        override val timestamp: Long,
    ) : GestureEvent()

    data class PanMove(
        val x: Int,
        val y: Int,
        val deltaX: Int,
        val deltaY: Int,
        override val timestamp: Long,
    ) : GestureEvent()

    data class PanStop(
        val x: Int,
        val y: Int,
        override val timestamp: Long,
    ) : GestureEvent()

    data class Swipe(
        val direction: SwipeDirection,
        val distance: Int,
        override val timestamp: Long,
    ) : GestureEvent()

    /**
     * Get the EventType enum value for this gesture
     */
    fun getEventType(): EventType =
        when (this) {
            is Tap -> EventType.TAP
            is DoubleTap -> EventType.DOUBLE_TAP
            is PanStart -> EventType.PAN_START
            is PanMove -> EventType.PAN_MOVE
            is PanStop -> EventType.PAN_STOP
            is Swipe -> EventType.SWIPE
        }
}
