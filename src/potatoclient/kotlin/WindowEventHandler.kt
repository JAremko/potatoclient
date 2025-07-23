package potatoclient.kotlin

import java.awt.Dimension
import java.awt.Frame
import java.awt.Point
import java.awt.event.ComponentAdapter
import java.awt.event.ComponentEvent
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicLong
import javax.swing.JFrame

class WindowEventHandler(
    private val frame: JFrame,
    private val callback: EventCallback,
    private val eventFilter: EventFilter,
    private val eventThrottleExecutor: ScheduledExecutorService,
) {
    interface EventCallback {
        fun onWindowEvent(
            type: EventFilter.EventType,
            eventName: String,
            details: Map<String, Any>?,
        )
    }

    // Window state tracking
    @Volatile private var lastWindowLocation: Point? = frame.location

    @Volatile private var lastWindowSize: Dimension? = frame.size
    private val lastWindowEventTime = AtomicLong(0)

    // Throttled event tasks
    @Volatile private var pendingWindowResizeTask: ScheduledFuture<*>? = null

    @Volatile private var pendingWindowMoveTask: ScheduledFuture<*>? = null

    @Volatile private var lastResizeEvent: ComponentEvent? = null

    @Volatile private var lastMoveEvent: ComponentEvent? = null

    fun attachListeners() {
        // Add comprehensive window listeners
        frame.addWindowListener(
            object : WindowAdapter() {
                // Removed windowClosing - handled by FrameManager

                override fun windowOpened(e: WindowEvent) {
                    sendFilteredWindowEvent(EventFilter.EventType.WINDOW_OPENED, "opened", null)
                }

                override fun windowIconified(e: WindowEvent) {
                    sendFilteredWindowEvent(EventFilter.EventType.WINDOW_MINIMIZED, "minimized", null)
                }

                override fun windowDeiconified(e: WindowEvent) {
                    sendFilteredWindowEvent(EventFilter.EventType.WINDOW_RESTORED, "restored", null)
                }

                override fun windowActivated(e: WindowEvent) {
                    sendFilteredWindowEvent(EventFilter.EventType.WINDOW_FOCUSED, "focused", null)
                }

                override fun windowDeactivated(e: WindowEvent) {
                    sendFilteredWindowEvent(EventFilter.EventType.WINDOW_UNFOCUSED, "unfocused", null)
                }
            },
        )

        // Window state listener for maximize/normal
        frame.addWindowStateListener { e ->
            val oldState = e.oldState
            val newState = e.newState

            if (oldState != newState) {
                val details =
                    mutableMapOf<String, Any>(
                        "oldState" to getStateString(oldState),
                        "newState" to getStateString(newState),
                    )

                if ((newState and Frame.MAXIMIZED_BOTH) == Frame.MAXIMIZED_BOTH) {
                    sendFilteredWindowEvent(EventFilter.EventType.WINDOW_MAXIMIZED, "maximized", details)
                } else if ((oldState and Frame.MAXIMIZED_BOTH) == Frame.MAXIMIZED_BOTH &&
                    (newState and Frame.MAXIMIZED_BOTH) == 0
                ) {
                    sendFilteredWindowEvent(EventFilter.EventType.WINDOW_UNMAXIMIZED, "unmaximized", details)
                }
            }
        }

        // Component listener for resize and move
        frame.addComponentListener(
            object : ComponentAdapter() {
                override fun componentResized(e: ComponentEvent) {
                    lastResizeEvent = e
                    scheduleWindowEvent(
                        task = {
                            lastResizeEvent?.let {
                                val newSize = frame.size
                                if (newSize != lastWindowSize) {
                                    val details =
                                        mutableMapOf<String, Any>(
                                            "width" to newSize.width,
                                            "height" to newSize.height,
                                        )
                                    lastWindowSize?.let {
                                        details["oldWidth"] = it.width
                                        details["oldHeight"] = it.height
                                    }
                                    sendFilteredWindowEvent(EventFilter.EventType.WINDOW_RESIZED, "resized", details)
                                    lastWindowSize = newSize
                                }
                            }
                        },
                        currentTask = pendingWindowResizeTask,
                        taskSetter = { pendingWindowResizeTask = it },
                    )
                }

                override fun componentMoved(e: ComponentEvent) {
                    lastMoveEvent = e
                    scheduleWindowEvent(
                        task = {
                            lastMoveEvent?.let {
                                val newLocation = frame.location
                                if (newLocation != lastWindowLocation) {
                                    val details =
                                        mutableMapOf<String, Any>(
                                            "x" to newLocation.x,
                                            "y" to newLocation.y,
                                        )
                                    lastWindowLocation?.let {
                                        details["oldX"] = it.x
                                        details["oldY"] = it.y
                                    }
                                    sendFilteredWindowEvent(EventFilter.EventType.WINDOW_MOVED, "moved", details)
                                    lastWindowLocation = newLocation
                                }
                            }
                        },
                        currentTask = pendingWindowMoveTask,
                        taskSetter = { pendingWindowMoveTask = it },
                    )
                }
            },
        )
    }

    private fun scheduleWindowEvent(
        task: () -> Unit,
        currentTask: ScheduledFuture<*>?,
        taskSetter: (ScheduledFuture<*>?) -> Unit,
    ) {
        val now = System.currentTimeMillis()
        val lastTime = lastWindowEventTime.get()

        if (now - lastTime >= Constants.WINDOW_EVENT_THROTTLE_MS) {
            if (lastWindowEventTime.compareAndSet(lastTime, now)) {
                task()
                // Cancel any pending task
                currentTask?.cancel(false)
            }
        } else {
            // Cancel existing task and schedule new one
            currentTask?.cancel(false)

            val delay = Constants.WINDOW_EVENT_THROTTLE_MS - (now - lastTime)
            val newTask =
                eventThrottleExecutor.schedule({
                    task()
                    lastWindowEventTime.set(System.currentTimeMillis())
                }, delay, TimeUnit.MILLISECONDS)
            taskSetter(newTask)
        }
    }

    private fun getStateString(state: Int): String =
        buildString {
            append("normal,")
            if ((state and Frame.ICONIFIED) == Frame.ICONIFIED) append("minimized,")
            if ((state and Frame.MAXIMIZED_HORIZ) == Frame.MAXIMIZED_HORIZ) append("maximized_horizontal,")
            if ((state and Frame.MAXIMIZED_VERT) == Frame.MAXIMIZED_VERT) append("maximized_vertical,")
            if ((state and Frame.MAXIMIZED_BOTH) == Frame.MAXIMIZED_BOTH) append("maximized,")
        }.removeSuffix(",")

    private fun sendFilteredWindowEvent(
        type: EventFilter.EventType,
        eventName: String,
        details: Map<String, Any>?,
    ) {
        if (eventFilter.isUnfiltered(type)) {
            callback.onWindowEvent(type, eventName, details)
        }
    }

    fun cleanup() {
        pendingWindowResizeTask?.cancel(false)
        pendingWindowMoveTask?.cancel(false)
    }
}
