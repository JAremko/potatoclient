package potatoclient.kotlin.gestures

// EventFilter not needed in GestureRecognizer
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.atomic.AtomicReference
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit
import kotlin.math.abs
import kotlin.math.sqrt

enum class GestureState {
    IDLE,
    PENDING,
    PANNING,
}

data class GestureConfig(
    val moveThreshold: Int = 20, // pixels before gesture starts (from gestures.json)
    val tapLongPressThreshold: Long = 300, // ms for long press
    val doubleTapThreshold: Long = 300, // ms between taps
    val panUpdateInterval: Long = 120, // ms between pan updates
    val doubleTapTolerance: Int = 10, // pixels tolerance for double tap position
    val scrollDebounceInterval: Long = 50, // ms between scroll events (accumulates scroll amounts)
)

class GestureRecognizer(
    private val config: GestureConfig = GestureConfig(),
    private val onGesture: (GestureEvent) -> Unit,
    private val frameDataProvider: FrameDataProvider
) {
    private val state = AtomicReference(GestureState.IDLE)
    private var startX: Int = 0
    private var startY: Int = 0
    private var startTime: Long = 0
    private var lastTapTime: Long = 0
    private var lastTapX: Int = 0
    private var lastTapY: Int = 0

    // Thread-safe tracking for pan gesture updates
    private val lastPanUpdate = AtomicLong(0)
    private var panStartNotified = false
    
    // Thread-safe tracking for scroll debouncing
    private val lastScrollUpdate = AtomicLong(0)
    private val pendingScrollUp = AtomicInteger(0)
    private val pendingScrollDown = AtomicInteger(0)
    private val lastScrollX = AtomicInteger(0)
    private val lastScrollY = AtomicInteger(0)
    
    // Timer for flushing pending scroll events
    private val scrollFlushExecutor: ScheduledExecutorService = Executors.newSingleThreadScheduledExecutor { r ->
        Thread(r, "GestureRecognizer-ScrollFlush").apply { isDaemon = true }
    }
    private var scrollFlushTask: ScheduledFuture<*>? = null

    fun processMousePressed(
        x: Int,
        y: Int,
        button: Int,
        time: Long,
    ) {
        if (button != 1) return // Only handle left button

        startX = x
        startY = y
        startTime = time
        state.set(GestureState.PENDING)
    }

    fun processMouseDragged(
        x: Int,
        y: Int,
        time: Long,
    ) {
        val currentState = state.get()
        if (currentState == GestureState.IDLE) return

        val distance = sqrt(((x - startX) * (x - startX) + (y - startY) * (y - startY)).toDouble())

        if (currentState == GestureState.PENDING && distance > config.moveThreshold) {
            // Start pan gesture
            state.set(GestureState.PANNING)
            panStartNotified = true
            lastPanUpdate.set(time)
            onGesture(GestureEvent.PanStart(startX, startY, time, frameDataProvider.getCurrentFrameTimestamp()))
        } else if (currentState == GestureState.PANNING) {
            // Throttle pan updates to config.panUpdateInterval
            val lastUpdate = lastPanUpdate.get()
            if (time - lastUpdate >= config.panUpdateInterval) {
                // Calculate deltas from start position (not incremental)
                val deltaX = x - startX
                val deltaY = y - startY
                onGesture(GestureEvent.PanMove(x, y, deltaX, deltaY, time, frameDataProvider.getCurrentFrameTimestamp()))
                lastPanUpdate.set(time)
            }
        }
    }

    fun processMouseReleased(
        x: Int,
        y: Int,
        button: Int,
        time: Long,
    ) {
        if (button != 1) return

        val currentState = state.get()
        val elapsedTime = time - startTime
        val distance = sqrt(((x - startX) * (x - startX) + (y - startY) * (y - startY)).toDouble())

        when (currentState) {
            GestureState.PENDING -> {
                // Only process as tap if movement is minimal and duration is short
                if (distance <= config.moveThreshold && elapsedTime < config.tapLongPressThreshold) {
                    // Check for double tap
                    if (time - lastTapTime < config.doubleTapThreshold &&
                        abs(x - lastTapX) < config.doubleTapTolerance &&
                        abs(y - lastTapY) < config.doubleTapTolerance
                    ) {
                        onGesture(GestureEvent.DoubleTap(x, y, time, frameDataProvider.getCurrentFrameTimestamp()))
                        lastTapTime = 0 // Reset to prevent triple tap
                    } else {
                        // Single tap
                        onGesture(GestureEvent.Tap(x, y, time, frameDataProvider.getCurrentFrameTimestamp()))
                        lastTapTime = time
                        lastTapX = x
                        lastTapY = y
                    }
                }
                // No swipe detection - any other release is just ignored
            }
            GestureState.PANNING -> {
                onGesture(GestureEvent.PanStop(x, y, time, frameDataProvider.getCurrentFrameTimestamp()))
            }
            else -> {}
        }

        state.set(GestureState.IDLE)
        panStartNotified = false
    }

    fun processMouseWheel(
        x: Int,
        y: Int,
        rotation: Int,
        time: Long
    ) {
        // Update last scroll position
        lastScrollX.set(x)
        lastScrollY.set(y)
        
        // Accumulate scroll amounts
        if (rotation < 0) {
            pendingScrollUp.addAndGet(abs(rotation))
        } else if (rotation > 0) {
            pendingScrollDown.addAndGet(rotation)
        }
        
        // Cancel any existing flush task
        scrollFlushTask?.cancel(false)
        
        // Schedule a new flush task
        scrollFlushTask = scrollFlushExecutor.schedule({
            flushPendingScrollEvents(System.currentTimeMillis())
        }, config.scrollDebounceInterval, TimeUnit.MILLISECONDS)
    }
    
    private fun flushPendingScrollEvents(time: Long) {
        val frameTimestamp = frameDataProvider.getCurrentFrameTimestamp()
        val x = lastScrollX.get()
        val y = lastScrollY.get()
        
        // Send accumulated scroll up
        val scrollUp = pendingScrollUp.getAndSet(0)
        if (scrollUp > 0) {
            onGesture(GestureEvent.WheelUp(x, y, scrollUp, time, frameTimestamp))
        }
        
        // Send accumulated scroll down
        val scrollDown = pendingScrollDown.getAndSet(0)
        if (scrollDown > 0) {
            onGesture(GestureEvent.WheelDown(x, y, scrollDown, time, frameTimestamp))
        }
        
        lastScrollUpdate.set(time)
    }
    
    fun reset() {
        state.set(GestureState.IDLE)
        panStartNotified = false
        lastPanUpdate.set(0)
        
        // Cancel any pending flush task
        scrollFlushTask?.cancel(false)
        
        // Flush any pending scroll events before reset
        if (pendingScrollUp.get() > 0 || pendingScrollDown.get() > 0) {
            flushPendingScrollEvents(System.currentTimeMillis())
        }
        
        // Reset scroll tracking
        lastScrollUpdate.set(0)
        pendingScrollUp.set(0)
        pendingScrollDown.set(0)
        lastScrollX.set(0)
        lastScrollY.set(0)
    }
    
    fun cleanup() {
        reset()
        scrollFlushExecutor.shutdown()
        try {
            if (!scrollFlushExecutor.awaitTermination(100, TimeUnit.MILLISECONDS)) {
                scrollFlushExecutor.shutdownNow()
            }
        } catch (e: InterruptedException) {
            scrollFlushExecutor.shutdownNow()
            Thread.currentThread().interrupt()
        }
    }
}
