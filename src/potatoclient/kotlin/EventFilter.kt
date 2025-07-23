package potatoclient.kotlin

import java.util.EnumSet

class EventFilter {
    enum class EventType {
        MOUSE_CLICK,
        MOUSE_DOUBLE_CLICK,
        MOUSE_DRAG_START,
        MOUSE_DRAG,
        MOUSE_DRAG_END,
        MOUSE_MOVE,
        MOUSE_WHEEL,
        MOUSE_ENTER,
        MOUSE_EXIT,
        MOUSE_PRESS,
        MOUSE_RELEASE,
        WINDOW_OPENED,
        WINDOW_CLOSING,
        WINDOW_CLOSED,
        WINDOW_MINIMIZED,
        WINDOW_RESTORED,
        WINDOW_MAXIMIZED,
        WINDOW_UNMAXIMIZED,
        WINDOW_FOCUSED,
        WINDOW_UNFOCUSED,
        WINDOW_MOVED,
        WINDOW_RESIZED,
    }

    companion object {
        // Default filtered events (configurable)
        private val DEFAULT_FILTERED_EVENTS =
            EnumSet.of(
                EventType.MOUSE_MOVE,
                EventType.MOUSE_PRESS,
                EventType.MOUSE_RELEASE,
                EventType.WINDOW_MOVED,
                EventType.WINDOW_RESIZED,
                EventType.WINDOW_OPENED,
                EventType.WINDOW_MINIMIZED,
                EventType.WINDOW_RESTORED,
                EventType.WINDOW_MAXIMIZED,
                EventType.WINDOW_UNMAXIMIZED,
                EventType.WINDOW_FOCUSED,
                EventType.WINDOW_UNFOCUSED,
            )
    }

    private val filteredEvents: EnumSet<EventType> = EnumSet.copyOf(DEFAULT_FILTERED_EVENTS)

    fun isUnfiltered(type: EventType): Boolean = !filteredEvents.contains(type)
}
