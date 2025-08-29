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
    
    // Timer for tap grace period (to detect double-tap)
    private val tapExecutor: ScheduledExecutorService = Executors.newSingleThreadScheduledExecutor { r ->
        Thread(r, "GestureRecognizer-TapGrace").apply { isDaemon = true }
    }
    private var tapGraceTask: ScheduledFuture<*>? = null
    private var pendingTap: GestureEvent.Tap? = null

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
            // Cancel any pending tap since we're starting a pan
            tapGraceTask?.cancel(false)
            tapGraceTask = null
            pendingTap = null
            
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
                    val currentTap = GestureEvent.Tap(x, y, time, frameDataProvider.getCurrentFrameTimestamp())
                    
                    // Check if this could be a double tap
                    val pending = pendingTap
                    if (pending != null &&
                        time - pending.timestamp < config.doubleTapThreshold &&
                        abs(x - pending.x) < config.doubleTapTolerance &&
                        abs(y - pending.y) < config.doubleTapTolerance
                    ) {
                        // Cancel the pending single tap timer
                        tapGraceTask?.cancel(false)
                        tapGraceTask = null
                        pendingTap = null
                        
                        // Send double tap immediately
                        onGesture(GestureEvent.DoubleTap(x, y, time, frameDataProvider.getCurrentFrameTimestamp()))
                        lastTapTime = 0 // Reset to prevent triple tap
                        lastTapX = x
                        lastTapY = y
                    } else {
                        // Store this tap and wait for grace period
                        pendingTap = currentTap
                        lastTapTime = time
                        lastTapX = x
                        lastTapY = y
                        
                        // Cancel any existing grace timer
                        tapGraceTask?.cancel(false)
                        
                        // Start grace period timer
                        tapGraceTask = tapExecutor.schedule({
                            // Grace period expired, send the single tap
                            val tap = pendingTap
                            if (tap != null) {
                                onGesture(tap)
                                pendingTap = null
                            }
                        }, config.doubleTapThreshold.toLong(), TimeUnit.MILLISECONDS)
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
        
        // Cancel tap grace timer and send pending tap if any
        tapGraceTask?.cancel(false)
        tapGraceTask = null
        val tap = pendingTap
        if (tap != null) {
            onGesture(tap)
            pendingTap = null
        }
    }
    
    fun cleanup() {
        reset()
        
        // Shutdown scroll flush executor
        scrollFlushExecutor.shutdown()
        try {
            if (!scrollFlushExecutor.awaitTermination(100, TimeUnit.MILLISECONDS)) {
                scrollFlushExecutor.shutdownNow()
            }
        } catch (e: InterruptedException) {
            scrollFlushExecutor.shutdownNow()
            Thread.currentThread().interrupt()
        }
        
        // Shutdown tap grace executor
        tapExecutor.shutdown()
        try {
            if (!tapExecutor.awaitTermination(100, TimeUnit.MILLISECONDS)) {
                tapExecutor.shutdownNow()
            }
        } catch (e: InterruptedException) {
            tapExecutor.shutdownNow()
            Thread.currentThread().interrupt()
        }
    }
}
