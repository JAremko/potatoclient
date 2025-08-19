package potatoclient.kotlin.gestures

import com.cognitect.transit.Keyword
import com.cognitect.transit.TransitFactory
import potatoclient.java.transit.EventType

// SwipeDirection removed - no swipe detection per design

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

    // Swipe removed intentionally - all drags should trigger pan

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
        }
    
    /**
     * Get the Transit Keyword for this gesture type
     */
    fun toKeyword(): Keyword =
        when (this) {
            is Tap -> TransitFactory.keyword("tap")
            is DoubleTap -> TransitFactory.keyword("double-tap")
            is PanStart -> TransitFactory.keyword("pan-start")
            is PanMove -> TransitFactory.keyword("pan-move")
            is PanStop -> TransitFactory.keyword("pan-stop")
        }
}

// Extension function for EventType to Keyword conversion
fun EventType.toKeyword(): Keyword =
    when (this) {
        EventType.TAP -> TransitFactory.keyword("tap")
        EventType.DOUBLE_TAP -> TransitFactory.keyword("double-tap")
        EventType.PAN_START -> TransitFactory.keyword("pan-start")
        EventType.PAN_MOVE -> TransitFactory.keyword("pan-move")
        EventType.PAN_STOP -> TransitFactory.keyword("pan-stop")
        else -> TransitFactory.keyword(this.name.lowercase().replace("_", "-"))
    }
