package potatoclient.kotlin

import potatoclient.kotlin.gestures.FrameDataProvider
import potatoclient.kotlin.gestures.GestureConfig
import potatoclient.kotlin.gestures.GestureEvent
import potatoclient.kotlin.gestures.GestureRecognizer
import potatoclient.kotlin.gestures.PanController
import potatoclient.kotlin.gestures.RotaryDirection
import potatoclient.kotlin.gestures.StreamType
import potatoclient.transit.EventType
import java.awt.Component
import java.awt.Point
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.awt.event.MouseMotionAdapter
import java.awt.event.MouseWheelEvent
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.atomic.AtomicReference

class MouseEventHandler(
    private val videoComponent: Component,
    private val callback: EventCallback,
    private val eventFilter: EventFilter,
    private val eventThrottleExecutor: ScheduledExecutorService,
    private val streamType: StreamType = StreamType.HEAT, // Default to heat
    private val frameDataProvider: potatoclient.kotlin.gestures.FrameDataProvider? = null,
) {
    // Click caching for double-click detection
    private data class ClickInfo(
        val x: Int,
        val y: Int,
        val button: Int,
        val modifiers: Int,
        val timestamp: Long,
    ) {
        fun isDoubleClick(
            x: Int,
            y: Int,
            button: Int,
            timestamp: Long,
        ): Boolean {
            // Check if it's a double click (same button, close position, within time window)
            return this.button == button &&
                kotlin.math.abs(this.x - x) <= Constants.DOUBLE_CLICK_MAX_DISTANCE &&
                kotlin.math.abs(this.y - y) <= Constants.DOUBLE_CLICK_MAX_DISTANCE &&
                (timestamp - this.timestamp) <= Constants.DOUBLE_CLICK_WINDOW_MS
        }
    }

    interface EventCallback {
        fun onNavigationEvent(
            type: EventFilter.EventType,
            eventName: String,
            x: Int,
            y: Int,
            details: Map<String, Any>?,
        )

        // New methods for gesture events and commands
        fun onGestureEvent(event: Map<String, Any>)

        fun sendCommand(command: Map<String, Any>)
    }

    // State tracking
    private val lastClick = AtomicReference<ClickInfo?>()

    @Volatile private var pendingClickTask: ScheduledFuture<*>? = null

    @Volatile private var pendingMouseMoveTask: ScheduledFuture<*>? = null

    @Volatile private var pendingMouseDragTask: ScheduledFuture<*>? = null

    @Volatile private var lastMouseMoveEvent: MouseEvent? = null

    @Volatile private var lastMouseDragEvent: MouseEvent? = null
    private val lastMoveTime = AtomicLong(0)
    private val lastDragTime = AtomicLong(0)
    private val isDragging = AtomicBoolean(false)

    @Volatile private var dragOrigin: Point? = null

    // Gesture recognition components
    private val gestureConfig =
        GestureConfig(
            moveThreshold = 20,
            tapLongPressThreshold = 300,
            doubleTapThreshold = 300,
            swipeThreshold = 100,
            panUpdateInterval = 120,
            doubleTapTolerance = 10,
        )

    private val gestureRecognizer =
        GestureRecognizer(gestureConfig) { gesture ->
            handleGesture(gesture)
        }

    // Pan controller for continuous rotation
    private val panController =
        PanController(
            onRotaryCommand = { azSpeed, elSpeed, azDir, elDir ->
                sendRotaryVelocityCommand(azSpeed, elSpeed, azDir, elDir)
            },
            onHaltCommand = {
                sendRotaryHaltCommand()
            },
            streamType = streamType,
        )

    fun attachListeners() {
        videoComponent.addMouseListener(
            object : MouseAdapter() {
                override fun mouseClicked(e: MouseEvent) {
                    // Skip processing - we handle clicks on mouse release
                }

                override fun mousePressed(e: MouseEvent) {
                    // Feed to gesture recognizer
                    gestureRecognizer.processMousePressed(e.x, e.y, e.button, System.currentTimeMillis())

                    // Internal tracking only - filtered from output
                    val details =
                        mapOf(
                            KEY_BUTTON to e.button,
                            KEY_MODIFIERS to e.modifiersEx,
                        )
                    sendFilteredEvent(
                        EventFilter.EventType.MOUSE_PRESS,
                        "mouse-press",
                        e.x,
                        e.y,
                        details,
                    )
                }

                override fun mouseReleased(e: MouseEvent) {
                    // Feed to gesture recognizer
                    gestureRecognizer.processMouseReleased(e.x, e.y, e.button, System.currentTimeMillis())

                    // Handle drag end first
                    if (isDragging.compareAndSet(true, false)) {
                        val details =
                            mapOf(
                                KEY_BUTTON to e.button,
                                KEY_MODIFIERS to e.modifiersEx,
                            )
                        sendFilteredEvent(
                            EventFilter.EventType.MOUSE_DRAG_END,
                            "mouse-drag-end",
                            e.x,
                            e.y,
                            details,
                        )

                        // Clear drag origin
                        dragOrigin = null
                    } else {
                        // Process potential click/double-click
                        processClick(e)
                    }

                    // Internal tracking only - filtered from output
                    val details2 =
                        mapOf(
                            KEY_BUTTON to e.button,
                            KEY_MODIFIERS to e.modifiersEx,
                        )
                    sendFilteredEvent(
                        EventFilter.EventType.MOUSE_RELEASE,
                        "mouse-release",
                        e.x,
                        e.y,
                        details2,
                    )
                }

                override fun mouseEntered(e: MouseEvent) {
                    sendFilteredEvent(
                        EventFilter.EventType.MOUSE_ENTER,
                        "mouse-enter",
                        e.x,
                        e.y,
                        null,
                    )
                }

                override fun mouseExited(e: MouseEvent) {
                    sendFilteredEvent(
                        EventFilter.EventType.MOUSE_EXIT,
                        "mouse-exit",
                        e.x,
                        e.y,
                        null,
                    )
                }
            },
        )

        videoComponent.addMouseMotionListener(
            object : MouseMotionAdapter() {
                override fun mouseMoved(e: MouseEvent) {
                    lastMouseMoveEvent = e
                    val now = System.currentTimeMillis()
                    val lastTime = lastMoveTime.get()

                    if (now - lastTime >= Constants.MOUSE_EVENT_THROTTLE_MS) {
                        if (lastMoveTime.compareAndSet(lastTime, now)) {
                            val details = mapOf(KEY_MODIFIERS to e.modifiersEx)
                            sendFilteredEvent(
                                EventFilter.EventType.MOUSE_MOVE,
                                "mouse-move",
                                e.x,
                                e.y,
                                details,
                            )

                            // Cancel any pending task
                            pendingMouseMoveTask?.cancel(false)
                        }
                    } else {
                        // Schedule the event to be sent after throttle period
                        pendingMouseMoveTask?.cancel(false)

                        val delay = Constants.MOUSE_EVENT_THROTTLE_MS - (now - lastTime)
                        pendingMouseMoveTask =
                            eventThrottleExecutor.schedule({
                                lastMouseMoveEvent?.let { evt ->
                                    val details = mapOf(KEY_MODIFIERS to evt.modifiersEx)
                                    sendFilteredEvent(
                                        EventFilter.EventType.MOUSE_MOVE,
                                        "mouse-move",
                                        evt.x,
                                        evt.y,
                                        details,
                                    )
                                    lastMoveTime.set(System.currentTimeMillis())
                                }
                            }, delay, TimeUnit.MILLISECONDS)
                    }
                }

                override fun mouseDragged(e: MouseEvent) {
                    // Feed to gesture recognizer
                    gestureRecognizer.processMouseDragged(e.x, e.y, System.currentTimeMillis())

                    if (isDragging.compareAndSet(false, true)) {
                        // Store drag origin
                        dragOrigin = Point(e.x, e.y)

                        val details =
                            mapOf(
                                KEY_BUTTON to getButtonFromModifiers(e.modifiersEx),
                                KEY_MODIFIERS to e.modifiersEx,
                            )
                        sendFilteredEvent(
                            EventFilter.EventType.MOUSE_DRAG_START,
                            "mouse-drag-start",
                            e.x,
                            e.y,
                            details,
                        )
                    }

                    lastMouseDragEvent = e
                    val now = System.currentTimeMillis()
                    val lastTime = lastDragTime.get()

                    if (now - lastTime >= Constants.MOUSE_EVENT_THROTTLE_MS) {
                        if (lastDragTime.compareAndSet(lastTime, now)) {
                            sendDragEvent(e)

                            // Cancel any pending task
                            pendingMouseDragTask?.cancel(false)
                        }
                    } else {
                        // Schedule the event to be sent after throttle period
                        pendingMouseDragTask?.cancel(false)

                        val delay = Constants.MOUSE_EVENT_THROTTLE_MS - (now - lastTime)
                        pendingMouseDragTask =
                            eventThrottleExecutor.schedule({
                                lastMouseDragEvent?.let { evt ->
                                    sendDragEvent(evt)
                                    lastDragTime.set(System.currentTimeMillis())
                                }
                            }, delay, TimeUnit.MILLISECONDS)
                    }
                }

                private fun getButtonFromModifiers(modifiers: Int): Int =
                    when (
                        modifiers and (
                            MouseEvent.BUTTON1_DOWN_MASK or
                                MouseEvent.BUTTON2_DOWN_MASK or MouseEvent.BUTTON3_DOWN_MASK
                        )
                    ) {
                        MouseEvent.BUTTON1_DOWN_MASK -> 1
                        MouseEvent.BUTTON2_DOWN_MASK -> 2
                        MouseEvent.BUTTON3_DOWN_MASK -> 3
                        else -> 0
                    }
            },
        )

        videoComponent.addMouseWheelListener { e ->
            val details =
                mapOf(
                    KEY_WHEEL_ROTATION to e.wheelRotation,
                    KEY_SCROLL_AMOUNT to e.scrollAmount,
                    KEY_SCROLL_TYPE to if (e.scrollType == MouseWheelEvent.WHEEL_UNIT_SCROLL) "unit" else "block",
                    KEY_MODIFIERS to e.modifiersEx,
                )
            sendFilteredEvent(
                EventFilter.EventType.MOUSE_WHEEL,
                "mouse-wheel",
                e.x,
                e.y,
                details,
            )
        }
    }

    private fun processClick(e: MouseEvent) {
        val now = System.currentTimeMillis()
        val x = e.x
        val y = e.y
        val button = e.button
        val modifiers = e.modifiersEx

        val previous = lastClick.get()

        if (previous != null && previous.isDoubleClick(x, y, button, now)) {
            // Cancel pending single click
            pendingClickTask?.cancel(false)
            pendingClickTask = null

            // Send double-click immediately
            val details =
                mapOf(
                    KEY_BUTTON to button,
                    KEY_CLICK_COUNT to 2,
                    KEY_MODIFIERS to modifiers,
                )
            sendFilteredEvent(
                EventFilter.EventType.MOUSE_DOUBLE_CLICK,
                "mouse-double-click",
                x,
                y,
                details,
            )

            // Clear last click
            lastClick.set(null)
        } else {
            // Store this click
            val current = ClickInfo(x, y, button, modifiers, now)
            lastClick.set(current)

            // Schedule single click
            pendingClickTask =
                eventThrottleExecutor.schedule({
                    // If this click is still current, send it
                    if (lastClick.compareAndSet(current, null)) {
                        val details =
                            mapOf(
                                KEY_BUTTON to button,
                                KEY_CLICK_COUNT to 1,
                                KEY_MODIFIERS to modifiers,
                            )
                        sendFilteredEvent(
                            EventFilter.EventType.MOUSE_CLICK,
                            "mouse-click",
                            x,
                            y,
                            details,
                        )
                    }
                }, Constants.DOUBLE_CLICK_WINDOW_MS.toLong(), TimeUnit.MILLISECONDS)
        }
    }

    // Pre-allocated map keys to avoid string allocations
    private companion object {
        const val KEY_MODIFIERS = "modifiers"
        const val KEY_ORIGIN_X = "originX"
        const val KEY_ORIGIN_Y = "originY"
        const val KEY_DELTA_X = "deltaX"
        const val KEY_DELTA_Y = "deltaY"
        const val KEY_DISTANCE_PIXELS = "distancePixels"
        const val KEY_ORIGIN_NDC_X = "originNdcX"
        const val KEY_ORIGIN_NDC_Y = "originNdcY"
        const val KEY_DELTA_NDC_X = "deltaNdcX"
        const val KEY_DELTA_NDC_Y = "deltaNdcY"
        const val KEY_DISTANCE_NDC = "distanceNdc"
        const val KEY_BUTTON = "button"
        const val KEY_CLICK_COUNT = "clickCount"
        const val KEY_WHEEL_ROTATION = "wheelRotation"
        const val KEY_SCROLL_AMOUNT = "scrollAmount"
        const val KEY_SCROLL_TYPE = "scrollType"
    }

    // Pre-sized map for drag events (most complex case)
    private val dragEventDetails = HashMap<String, Any>(12)

    private fun sendDragEvent(e: MouseEvent) {
        // Reuse the pre-allocated map
        dragEventDetails.clear()
        dragEventDetails[KEY_MODIFIERS] = e.modifiersEx

        dragOrigin?.let { origin ->
            // Add origin point
            dragEventDetails[KEY_ORIGIN_X] = origin.x
            dragEventDetails[KEY_ORIGIN_Y] = origin.y

            // Calculate distance in pixels
            val deltaX = e.x - origin.x
            val deltaY = e.y - origin.y
            val distancePixels = kotlin.math.sqrt((deltaX * deltaX + deltaY * deltaY).toDouble())
            dragEventDetails[KEY_DELTA_X] = deltaX
            dragEventDetails[KEY_DELTA_Y] = deltaY
            dragEventDetails[KEY_DISTANCE_PIXELS] = distancePixels

            // Calculate NDC origin and distance
            val canvasWidth = videoComponent.width
            val canvasHeight = videoComponent.height
            if (canvasWidth > 0 && canvasHeight > 0) {
                val widthReciprocal = 2.0 / canvasWidth
                val heightReciprocal = 2.0 / canvasHeight

                val originNdcX = origin.x * widthReciprocal - 1.0
                val originNdcY = origin.y * heightReciprocal - 1.0
                val deltaNdcX = deltaX * widthReciprocal
                val deltaNdcY = deltaY * heightReciprocal
                val distanceNdc = kotlin.math.sqrt(deltaNdcX * deltaNdcX + deltaNdcY * deltaNdcY)

                dragEventDetails[KEY_ORIGIN_NDC_X] = originNdcX
                dragEventDetails[KEY_ORIGIN_NDC_Y] = originNdcY
                dragEventDetails[KEY_DELTA_NDC_X] = deltaNdcX
                dragEventDetails[KEY_DELTA_NDC_Y] = deltaNdcY
                dragEventDetails[KEY_DISTANCE_NDC] = distanceNdc
            }
        }

        sendFilteredEvent(
            EventFilter.EventType.MOUSE_DRAG,
            "mouse-drag",
            e.x,
            e.y,
            dragEventDetails,
        )
    }

    private fun sendFilteredEvent(
        type: EventFilter.EventType,
        eventName: String,
        x: Int,
        y: Int,
        details: Map<String, Any>?,
    ) {
        if (eventFilter.isUnfiltered(type)) {
            callback.onNavigationEvent(type, eventName, x, y, details)
        }
    }

    fun cleanup() {
        pendingClickTask?.cancel(false)
        pendingMouseMoveTask?.cancel(false)
        pendingMouseDragTask?.cancel(false)
        panController.shutdown()
        gestureRecognizer.reset()
    }

    // Gesture handling methods
    private fun handleGesture(gesture: GestureEvent) {
        val canvasWidth = videoComponent.width
        val canvasHeight = videoComponent.height

        // Calculate aspect ratio for pan gesture adjustment
        val aspectRatio = canvasWidth.toDouble() / canvasHeight.toDouble()

        val baseEvent =
            mutableMapOf(
                "type" to EventType.GESTURE.key,
                "gesture-type" to gesture.getEventType().key,
                "timestamp" to gesture.timestamp,
                "canvas-width" to canvasWidth,
                "canvas-height" to canvasHeight,
                "aspect-ratio" to aspectRatio,
                "stream-type" to streamType.name.lowercase(),
            )

        when (gesture) {
            is GestureEvent.Tap -> {
                baseEvent["x"] = gesture.x
                baseEvent["y"] = gesture.y
                baseEvent["ndc-x"] = pixelToNDC(gesture.x, canvasWidth)
                baseEvent["ndc-y"] = pixelToNDC(gesture.y, canvasHeight, true)
            }
            is GestureEvent.DoubleTap -> {
                baseEvent["x"] = gesture.x
                baseEvent["y"] = gesture.y
                baseEvent["ndc-x"] = pixelToNDC(gesture.x, canvasWidth)
                baseEvent["ndc-y"] = pixelToNDC(gesture.y, canvasHeight, true)
            }
            is GestureEvent.PanStart -> {
                baseEvent["x"] = gesture.x
                baseEvent["y"] = gesture.y
                baseEvent["ndc-x"] = pixelToNDC(gesture.x, canvasWidth)
                baseEvent["ndc-y"] = pixelToNDC(gesture.y, canvasHeight, true)
                // Start pan controller
                panController.startPan()
            }
            is GestureEvent.PanMove -> {
                baseEvent["x"] = gesture.x
                baseEvent["y"] = gesture.y
                baseEvent["delta-x"] = gesture.deltaX
                baseEvent["delta-y"] = gesture.deltaY
                // Important: Apply aspect ratio adjustment to X delta (matching web frontend)
                val ndcDeltaX = pixelToNDC(gesture.deltaX, canvasWidth, false)
                val ndcDeltaY = pixelToNDC(gesture.deltaY, canvasHeight, true)
                val adjustedNdcDeltaX = ndcDeltaX * aspectRatio
                baseEvent["ndc-delta-x"] = adjustedNdcDeltaX
                baseEvent["ndc-delta-y"] = ndcDeltaY

                // Update pan controller with current zoom level
                val currentZoomLevel = frameDataProvider?.getCurrentZoomLevel() ?: 0
                panController.updatePan(adjustedNdcDeltaX, ndcDeltaY, currentZoomLevel)
            }
            is GestureEvent.PanStop -> {
                baseEvent["x"] = gesture.x
                baseEvent["y"] = gesture.y
                // Stop pan controller
                panController.stopPan()
            }
            is GestureEvent.Swipe -> {
                baseEvent["direction"] = gesture.direction.name.lowercase()
                baseEvent["distance"] = gesture.distance
            }
        }

        // Add frame data if available
        frameDataProvider?.getFrameData()?.let { frameData ->
            baseEvent["frame-timestamp"] = frameData.timestamp
            baseEvent["frame-duration"] = frameData.duration
        }

        callback.onGestureEvent(baseEvent)
    }

    private fun pixelToNDC(
        value: Int,
        dimension: Int,
        invertY: Boolean = false,
    ): Double {
        val ndc = (value.toDouble() / dimension) * 2.0 - 1.0
        return if (invertY) -ndc else ndc
    }

    // Send commands via Transit
    private fun sendRotaryVelocityCommand(
        azSpeed: Double,
        elSpeed: Double,
        azDir: RotaryDirection,
        elDir: RotaryDirection,
    ) {
        val command =
            mapOf(
                "action" to "rotary-set-velocity",
                "params" to
                    mapOf(
                        "azimuth-speed" to azSpeed,
                        "elevation-speed" to elSpeed,
                        "azimuth-direction" to azDir.name.lowercase().replace("_", "-"),
                        "elevation-direction" to elDir.name.lowercase().replace("_", "-"),
                    ),
            )
        callback.sendCommand(command)
    }

    private fun sendRotaryHaltCommand() {
        val command =
            mapOf(
                "action" to "rotary-halt",
                "params" to emptyMap<String, Any>(),
            )
        callback.sendCommand(command)
    }
}
