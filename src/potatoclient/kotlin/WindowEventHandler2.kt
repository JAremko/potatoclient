package potatoclient.kotlin

import com.cognitect.transit.Keyword
import potatoclient.kotlin.transit.TransitKeys
import java.awt.Dimension
import java.awt.Frame
import java.awt.Point
import java.awt.event.*
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.atomic.AtomicReference
import javax.swing.JFrame

/**
 * Enhanced window event handler that sends all window events with Transit Keywords
 * and proper throttling for rapid-fire events like resize.
 */
class WindowEventHandler2(
    private val frame: JFrame,
    private val callback: EventCallback,
    private val throttleMs: Long = 100L  // Default 100ms throttle for resize events
) {
    interface EventCallback {
        fun onWindowEvent(event: Map<Any, Any>)
    }

    // Throttling state for resize events
    private val resizeThrottler = EventThrottler(throttleMs)
    private val moveThrottler = EventThrottler(throttleMs)
    
    // Track window state for relative data
    @Volatile private var lastSize = Dimension(frame.width, frame.height)
    @Volatile private var lastLocation = Point(frame.x, frame.y)
    @Volatile private var lastState = frame.extendedState
    
    private val throttleExecutor: ScheduledExecutorService = Executors.newSingleThreadScheduledExecutor { r ->
        Thread(r, "WindowEventThrottle").apply { isDaemon = true }
    }

    fun attachListeners() {
        // Window event listener
        frame.addWindowListener(object : WindowAdapter() {
            override fun windowOpened(e: WindowEvent) {
                sendWindowEvent(TransitKeys.WINDOW, TransitKeys.FOCUS, mapOf(
                    TransitKeys.TIMESTAMP to System.currentTimeMillis()
                ))
            }

            override fun windowClosing(e: WindowEvent) {
                sendWindowEvent(TransitKeys.WINDOW, TransitKeys.CLOSE_REQUEST, mapOf(
                    TransitKeys.TIMESTAMP to System.currentTimeMillis()
                ))
            }

            override fun windowClosed(e: WindowEvent) {
                // Clean up resources
                cleanup()
            }

            override fun windowIconified(e: WindowEvent) {
                sendWindowEvent(TransitKeys.WINDOW, TransitKeys.MINIMIZE, mapOf(
                    TransitKeys.TIMESTAMP to System.currentTimeMillis()
                ))
            }

            override fun windowDeiconified(e: WindowEvent) {
                sendWindowEvent(TransitKeys.WINDOW, TransitKeys.RESTORE, mapOf(
                    TransitKeys.TIMESTAMP to System.currentTimeMillis()
                ))
            }

            override fun windowActivated(e: WindowEvent) {
                sendWindowEvent(TransitKeys.WINDOW, TransitKeys.FOCUS, mapOf(
                    TransitKeys.TIMESTAMP to System.currentTimeMillis()
                ))
            }

            override fun windowDeactivated(e: WindowEvent) {
                sendWindowEvent(TransitKeys.WINDOW, TransitKeys.BLUR, mapOf(
                    TransitKeys.TIMESTAMP to System.currentTimeMillis()
                ))
            }
        })

        // Window state listener for maximize events
        frame.addWindowStateListener { e ->
            val oldState = e.oldState
            val newState = e.newState
            
            if (oldState != newState) {
                when {
                    // Maximized
                    (newState and Frame.MAXIMIZED_BOTH) == Frame.MAXIMIZED_BOTH &&
                    (oldState and Frame.MAXIMIZED_BOTH) != Frame.MAXIMIZED_BOTH -> {
                        sendWindowEvent(TransitKeys.WINDOW, TransitKeys.MAXIMIZE, mapOf(
                            TransitKeys.TIMESTAMP to System.currentTimeMillis(),
                            TransitKeys.STATE to getStateKeyword(newState)
                        ))
                    }
                    // Restored from maximized
                    (oldState and Frame.MAXIMIZED_BOTH) == Frame.MAXIMIZED_BOTH &&
                    (newState and Frame.MAXIMIZED_BOTH) == 0 -> {
                        sendWindowEvent(TransitKeys.WINDOW, TransitKeys.RESTORE, mapOf(
                            TransitKeys.TIMESTAMP to System.currentTimeMillis(),
                            TransitKeys.STATE to getStateKeyword(newState)
                        ))
                    }
                }
                lastState = newState
            }
        }

        // Component listener for resize and move
        frame.addComponentListener(object : ComponentAdapter() {
            override fun componentResized(e: ComponentEvent) {
                val newSize = frame.size
                val oldSize = lastSize
                
                // Only send if actually changed
                if (newSize.width != oldSize.width || newSize.height != oldSize.height) {
                    val eventData: Map<Any, Any> = mapOf(
                        TransitKeys.WIDTH to newSize.width,
                        TransitKeys.HEIGHT to newSize.height,
                        TransitKeys.DELTA_X to (newSize.width - oldSize.width),
                        TransitKeys.DELTA_Y to (newSize.height - oldSize.height),
                        TransitKeys.TIMESTAMP to System.currentTimeMillis()
                    )
                    
                    // Throttle resize events
                    resizeThrottler.throttle {
                        sendWindowEvent(TransitKeys.WINDOW, TransitKeys.RESIZE, eventData)
                        lastSize = Dimension(newSize)
                    }
                }
            }

            override fun componentMoved(e: ComponentEvent) {
                val newLocation = frame.location
                val oldLocation = lastLocation
                
                // Only send if actually moved
                if (newLocation.x != oldLocation.x || newLocation.y != oldLocation.y) {
                    val eventData: Map<Any, Any> = mapOf(
                        TransitKeys.X to newLocation.x,
                        TransitKeys.Y to newLocation.y,
                        TransitKeys.DELTA_X to (newLocation.x - oldLocation.x),
                        TransitKeys.DELTA_Y to (newLocation.y - oldLocation.y),
                        TransitKeys.TIMESTAMP to System.currentTimeMillis()
                    )
                    
                    // Throttle move events
                    moveThrottler.throttle {
                        sendWindowEvent(TransitKeys.WINDOW, TransitKeys.WINDOW_MOVE, eventData)
                        lastLocation = Point(newLocation)
                    }
                }
            }
        })
    }

    private fun sendWindowEvent(eventType: Keyword, action: Keyword, data: Map<Any, Any>) {
        val event = mutableMapOf<Any, Any>(
            TransitKeys.MSG_TYPE to TransitKeys.EVENT,
            TransitKeys.TYPE to eventType,
            TransitKeys.ACTION to action,
            TransitKeys.TIMESTAMP to System.currentTimeMillis()
        )
        event.putAll(data)
        
        callback.onWindowEvent(event)
    }

    private fun getStateKeyword(state: Int): Keyword {
        return when {
            (state and Frame.MAXIMIZED_BOTH) == Frame.MAXIMIZED_BOTH -> TransitKeys.MAXIMIZE
            (state and Frame.ICONIFIED) == Frame.ICONIFIED -> TransitKeys.MINIMIZE
            else -> TransitKeys.RESTORE
        }
    }

    fun cleanup() {
        resizeThrottler.cleanup()
        moveThrottler.cleanup()
        throttleExecutor.shutdown()
        try {
            if (!throttleExecutor.awaitTermination(100, TimeUnit.MILLISECONDS)) {
                throttleExecutor.shutdownNow()
            }
        } catch (e: InterruptedException) {
            throttleExecutor.shutdownNow()
        }
    }

    /**
     * Inner class to handle event throttling
     */
    private inner class EventThrottler(private val throttleMs: Long) {
        private val lastEventTime = AtomicLong(0)
        private val pendingTask = AtomicReference<ScheduledFuture<*>?>(null)
        
        fun throttle(action: () -> Unit) {
            val now = System.currentTimeMillis()
            val last = lastEventTime.get()
            
            if (now - last >= throttleMs) {
                // Execute immediately if enough time has passed
                if (lastEventTime.compareAndSet(last, now)) {
                    action()
                    // Cancel any pending task
                    pendingTask.getAndSet(null)?.cancel(false)
                }
            } else {
                // Schedule for later
                val delay = throttleMs - (now - last)
                val oldTask = pendingTask.getAndSet(
                    throttleExecutor.schedule({
                        lastEventTime.set(System.currentTimeMillis())
                        action()
                    }, delay, TimeUnit.MILLISECONDS)
                )
                // Cancel old task if any
                oldTask?.cancel(false)
            }
        }
        
        fun cleanup() {
            pendingTask.getAndSet(null)?.cancel(false)
        }
    }
}