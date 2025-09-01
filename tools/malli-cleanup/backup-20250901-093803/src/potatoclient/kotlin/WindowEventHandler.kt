package potatoclient.kotlin

import potatoclient.kotlin.ipc.IpcClient
import java.awt.Dimension
import java.awt.Frame
import java.awt.Point
import java.awt.event.ComponentAdapter
import java.awt.event.ComponentEvent
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.atomic.AtomicReference
import javax.swing.JFrame
import kotlin.system.exitProcess

/**
 * Unified window event handler that sends events via IPC.
 * Handles all window events with proper throttling for rapid-fire events.
 */
class WindowEventHandler(
    private val frame: JFrame,
    private val ipcClient: IpcClient,
    throttleMs: Long = 100L, // Default 100ms throttle for resize/move events
    private val onShutdown: (() -> Unit)? = null, // Optional shutdown callback
) {
    // Throttling state
    private val resizeThrottler = EventThrottler(throttleMs)
    private val moveThrottler = EventThrottler(throttleMs)

    // Track window state for relative data
    @Volatile private var lastSize = Dimension(frame.width, frame.height)

    @Volatile private var lastLocation = Point(frame.x, frame.y)

    @Volatile private var lastState = frame.extendedState

    private val throttleExecutor: ScheduledExecutorService =
        Executors.newSingleThreadScheduledExecutor { r ->
            Thread(r, "WindowEventThrottle").apply { isDaemon = true }
        }

    fun attachListeners() {
        // Window event listener
        frame.addWindowListener(
            object : WindowAdapter() {
                override fun windowOpened(e: WindowEvent) {
                    ipcClient.sendWindowEvent("focus")
                }

                override fun windowClosing(e: WindowEvent) {
                    // Clean shutdown when window is closed
                    cleanup()
                    onShutdown?.invoke() // Call custom shutdown handler if provided
                    ipcClient.shutdown()
                    exitProcess(0)
                }

                override fun windowClosed(e: WindowEvent) {
                    // Additional cleanup if needed
                    cleanup()
                }

                override fun windowIconified(e: WindowEvent) {
                    ipcClient.sendWindowEvent("minimize")
                }

                override fun windowDeiconified(e: WindowEvent) {
                    ipcClient.sendWindowEvent("restore")
                }

                override fun windowActivated(e: WindowEvent) {
                    ipcClient.sendWindowEvent("focus")
                }

                override fun windowDeactivated(e: WindowEvent) {
                    ipcClient.sendWindowEvent("blur")
                }
            },
        )

        // Window state listener for maximize events
        frame.addWindowStateListener { e ->
            val oldState = e.oldState
            val newState = e.newState

            if (oldState != newState) {
                when {
                    // Maximized
                    (newState and Frame.MAXIMIZED_BOTH) == Frame.MAXIMIZED_BOTH &&
                        (oldState and Frame.MAXIMIZED_BOTH) != Frame.MAXIMIZED_BOTH -> {
                        ipcClient.sendWindowEvent("maximize")
                    }
                    // Restored from maximized
                    (oldState and Frame.MAXIMIZED_BOTH) == Frame.MAXIMIZED_BOTH &&
                        (newState and Frame.MAXIMIZED_BOTH) == 0 -> {
                        ipcClient.sendWindowEvent("restore")
                    }
                }
                lastState = newState
            }
        }

        // Component listener for resize and move
        frame.addComponentListener(
            object : ComponentAdapter() {
                override fun componentResized(e: ComponentEvent) {
                    val newSize = frame.size
                    val oldSize = lastSize

                    // Only send if actually changed
                    if (newSize.width != oldSize.width || newSize.height != oldSize.height) {
                        // Throttle resize events
                        resizeThrottler.throttle {
                            ipcClient.sendWindowEvent(
                                "resize",
                                width = newSize.width,
                                height = newSize.height,
                                deltaX = newSize.width - oldSize.width,
                                deltaY = newSize.height - oldSize.height,
                            )
                            lastSize = Dimension(newSize)
                        }
                    }
                }

                override fun componentMoved(e: ComponentEvent) {
                    val newLocation = frame.location
                    val oldLocation = lastLocation

                    // Only send if actually moved
                    if (newLocation.x != oldLocation.x || newLocation.y != oldLocation.y) {
                        // Throttle move events
                        moveThrottler.throttle {
                            ipcClient.sendWindowEvent(
                                "window-move",
                                x = newLocation.x,
                                y = newLocation.y,
                                deltaX = newLocation.x - oldLocation.x,
                                deltaY = newLocation.y - oldLocation.y,
                            )
                            lastLocation = Point(newLocation)
                        }
                    }
                }
            },
        )
    }

    fun cleanup() {
        resizeThrottler.cleanup()
        moveThrottler.cleanup()
        throttleExecutor.shutdown()
        try {
            if (!throttleExecutor.awaitTermination(100, TimeUnit.MILLISECONDS)) {
                throttleExecutor.shutdownNow()
            }
        } catch (_: InterruptedException) {
            throttleExecutor.shutdownNow()
        }
    }

    /**
     * Inner class to handle event throttling
     */
    private inner class EventThrottler(
        private val throttleMs: Long,
    ) {
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
                val oldTask =
                    pendingTask.getAndSet(
                        throttleExecutor.schedule({
                            lastEventTime.set(System.currentTimeMillis())
                            action()
                        }, delay, TimeUnit.MILLISECONDS),
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
