package potatoclient.kotlin.gestures

sealed class GestureEvent {
    abstract val x: Int
    abstract val y: Int
    abstract val timestamp: Long

    data class Tap(
        override val x: Int,
        override val y: Int,
        override val timestamp: Long
    ) : GestureEvent()

    data class DoubleTap(
        override val x: Int,
        override val y: Int,
        override val timestamp: Long
    ) : GestureEvent()

    data class PanStart(
        override val x: Int,
        override val y: Int,
        override val timestamp: Long
    ) : GestureEvent()

    data class PanMove(
        override val x: Int,
        override val y: Int,
        val deltaX: Int,
        val deltaY: Int,
        override val timestamp: Long
    ) : GestureEvent()

    data class PanStop(
        override val x: Int,
        override val y: Int,
        override val timestamp: Long
    ) : GestureEvent()
}