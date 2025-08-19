package potatoclient.kotlin

import potatoclient.java.transit.EventType
import potatoclient.kotlin.gestures.FrameDataProvider
import potatoclient.kotlin.gestures.GestureConfig
import potatoclient.kotlin.gestures.GestureEvent
import potatoclient.kotlin.gestures.GestureRecognizer
import potatoclient.kotlin.gestures.PanController
import potatoclient.kotlin.gestures.RotaryDirection
import potatoclient.kotlin.gestures.StreamType
import potatoclient.kotlin.gestures.toKeyword
import potatoclient.kotlin.transit.TransitKeys
import java.awt.Component
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.awt.event.MouseMotionAdapter

class MouseEventHandler(
    private val videoComponent: Component,
    private val callback: EventCallback,
    private val streamType: StreamType = StreamType.HEAT,
    private val frameDataProvider: FrameDataProvider? = null,
) {
    interface EventCallback {
        // Only gesture events and commands
        fun onGestureEvent(event: Map<Any, Any>)

        fun sendCommand(command: Map<Any, Any>)
    }

    // Gesture recognition components
    private val gestureConfig =
        GestureConfig(
            moveThreshold = 20,
            tapLongPressThreshold = 300,
            doubleTapThreshold = 300,
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
                override fun mousePressed(e: MouseEvent) {
                    gestureRecognizer.processMousePressed(e.x, e.y, e.button, System.currentTimeMillis())
                }

                override fun mouseReleased(e: MouseEvent) {
                    gestureRecognizer.processMouseReleased(e.x, e.y, e.button, System.currentTimeMillis())
                }
            },
        )

        videoComponent.addMouseMotionListener(
            object : MouseMotionAdapter() {
                override fun mouseDragged(e: MouseEvent) {
                    gestureRecognizer.processMouseDragged(e.x, e.y, System.currentTimeMillis())
                }
            },
        )

        // Mouse wheel for zoom
        videoComponent.addMouseWheelListener { e ->
            when {
                e.wheelRotation < 0 -> {
                    // Wheel up = zoom in
                    val command: Map<Any, Any> = mapOf(
                        TransitKeys.MSG_TYPE to TransitKeys.COMMAND,
                        TransitKeys.ACTION to if (streamType == StreamType.HEAT) TransitKeys.HEAT_CAMERA_NEXT_ZOOM else TransitKeys.DAY_CAMERA_NEXT_ZOOM
                    )
                    callback.sendCommand(command)
                }
                e.wheelRotation > 0 -> {
                    // Wheel down = zoom out
                    val command: Map<Any, Any> = mapOf(
                        TransitKeys.MSG_TYPE to TransitKeys.COMMAND,
                        TransitKeys.ACTION to if (streamType == StreamType.HEAT) TransitKeys.HEAT_CAMERA_PREV_ZOOM else TransitKeys.DAY_CAMERA_PREV_ZOOM
                    )
                    callback.sendCommand(command)
                }
            }
        }
    }

    private fun handleGesture(gesture: GestureEvent) {
        val canvasWidth = videoComponent.width
        val canvasHeight = videoComponent.height
        val aspectRatio = canvasWidth.toDouble() / canvasHeight.toDouble()

        val baseEvent =
            mutableMapOf<Any, Any>(
                TransitKeys.MSG_TYPE to TransitKeys.EVENT,
                TransitKeys.TYPE to TransitKeys.GESTURE,
                TransitKeys.GESTURE_TYPE to gesture.getEventType().toKeyword(),
                TransitKeys.TIMESTAMP to gesture.timestamp,
                TransitKeys.CANVAS_WIDTH to canvasWidth,
                TransitKeys.CANVAS_HEIGHT to canvasHeight,
                TransitKeys.ASPECT_RATIO to aspectRatio,
                TransitKeys.STREAM_TYPE to if (streamType == StreamType.HEAT) TransitKeys.HEAT else TransitKeys.DAY
            )

        when (gesture) {
            is GestureEvent.Tap -> {
                baseEvent[TransitKeys.X] = gesture.x
                baseEvent[TransitKeys.Y] = gesture.y
                baseEvent[TransitKeys.NDC_X] = pixelToNDC(gesture.x, canvasWidth)
                baseEvent[TransitKeys.NDC_Y] = pixelToNDC(gesture.y, canvasHeight, true)

                // Send rotary-goto-ndc command
                sendRotaryGotoNDC(
                    pixelToNDC(gesture.x, canvasWidth),
                    pixelToNDC(gesture.y, canvasHeight, true),
                )
            }
            is GestureEvent.DoubleTap -> {
                baseEvent[TransitKeys.X] = gesture.x
                baseEvent[TransitKeys.Y] = gesture.y
                baseEvent[TransitKeys.NDC_X] = pixelToNDC(gesture.x, canvasWidth)
                baseEvent[TransitKeys.NDC_Y] = pixelToNDC(gesture.y, canvasHeight, true)

                // Get frame timestamp if available
                val frameData = frameDataProvider?.getFrameData()
                frameData?.let {
                    baseEvent[TransitKeys.FRAME_TIMESTAMP] = it.timestamp
                    baseEvent[TransitKeys.FRAME_DURATION] = it.duration
                }

                // Send CV tracking command
                sendCVStartTrackNDC(
                    pixelToNDC(gesture.x, canvasWidth),
                    pixelToNDC(gesture.y, canvasHeight, true),
                    frameData?.timestamp,
                )
            }
            is GestureEvent.PanStart -> {
                baseEvent[TransitKeys.X] = gesture.x
                baseEvent[TransitKeys.Y] = gesture.y
                baseEvent[TransitKeys.NDC_X] = pixelToNDC(gesture.x, canvasWidth)
                baseEvent[TransitKeys.NDC_Y] = pixelToNDC(gesture.y, canvasHeight, true)
                panController.startPan()
            }
            is GestureEvent.PanMove -> {
                baseEvent[TransitKeys.X] = gesture.x
                baseEvent[TransitKeys.Y] = gesture.y
                baseEvent[TransitKeys.DELTA_X] = gesture.deltaX
                baseEvent[TransitKeys.DELTA_Y] = gesture.deltaY

                // Apply aspect ratio adjustment to X delta
                val ndcDeltaX = pixelToNDC(gesture.deltaX, canvasWidth, false)
                val ndcDeltaY = pixelToNDC(gesture.deltaY, canvasHeight, true)
                val adjustedNdcDeltaX = ndcDeltaX * aspectRatio

                baseEvent[TransitKeys.NDC_DELTA_X] = adjustedNdcDeltaX
                baseEvent[TransitKeys.NDC_DELTA_Y] = ndcDeltaY

                // Update pan controller with current zoom level
                val currentZoomLevel = frameDataProvider?.getCurrentZoomLevel() ?: 0
                panController.updatePan(adjustedNdcDeltaX, ndcDeltaY, currentZoomLevel)
            }
            is GestureEvent.PanStop -> {
                baseEvent[TransitKeys.X] = gesture.x
                baseEvent[TransitKeys.Y] = gesture.y
                panController.stopPan()
            }
            // No swipe handling - removed intentionally
        }

        // Add frame data if available
        frameDataProvider?.getFrameData()?.let { frameData ->
            baseEvent[TransitKeys.FRAME_TIMESTAMP] = frameData.timestamp
            baseEvent[TransitKeys.FRAME_DURATION] = frameData.duration
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

    // Command sending methods
    private fun sendRotaryGotoNDC(
        ndcX: Double,
        ndcY: Double,
    ) {
        val command: Map<Any, Any> = mapOf(
            TransitKeys.MSG_TYPE to TransitKeys.COMMAND,
            TransitKeys.ACTION to TransitKeys.ROTARY_GOTO_NDC,
            TransitKeys.STREAM_TYPE to if (streamType == StreamType.HEAT) TransitKeys.HEAT else TransitKeys.DAY,
            TransitKeys.NDC_X to ndcX,
            TransitKeys.NDC_Y to ndcY
        )
        callback.sendCommand(command)
    }

    private fun sendCVStartTrackNDC(
        ndcX: Double,
        ndcY: Double,
        frameTimestamp: Long?,
    ) {
        val command: MutableMap<Any, Any> = mutableMapOf(
            TransitKeys.MSG_TYPE to TransitKeys.COMMAND,
            TransitKeys.ACTION to TransitKeys.CV_START_TRACK_NDC,
            TransitKeys.STREAM_TYPE to if (streamType == StreamType.HEAT) TransitKeys.HEAT else TransitKeys.DAY,
            TransitKeys.NDC_X to ndcX,
            TransitKeys.NDC_Y to ndcY
        )
        frameTimestamp?.let { command[TransitKeys.FRAME_TIMESTAMP] = it }
        callback.sendCommand(command as Map<Any, Any>)
    }

    private fun sendRotaryVelocityCommand(
        azSpeed: Double,
        elSpeed: Double,
        azDir: RotaryDirection,
        elDir: RotaryDirection,
    ) {
        val command: Map<Any, Any> = mapOf(
            TransitKeys.MSG_TYPE to TransitKeys.COMMAND,
            TransitKeys.ACTION to TransitKeys.ROTARY_SET_VELOCITY,
            TransitKeys.AZIMUTH_SPEED to azSpeed,
            TransitKeys.ELEVATION_SPEED to elSpeed,
            TransitKeys.AZIMUTH_DIRECTION to if (azDir == RotaryDirection.CLOCKWISE) TransitKeys.CLOCKWISE else TransitKeys.COUNTER_CLOCKWISE,
            TransitKeys.ELEVATION_DIRECTION to if (elDir == RotaryDirection.CLOCKWISE) TransitKeys.CLOCKWISE else TransitKeys.COUNTER_CLOCKWISE
        )
        callback.sendCommand(command)
    }

    private fun sendRotaryHaltCommand() {
        val command: Map<Any, Any> = mapOf(
            TransitKeys.MSG_TYPE to TransitKeys.COMMAND,
            TransitKeys.ACTION to TransitKeys.ROTARY_HALT
        )
        callback.sendCommand(command)
    }

    fun cleanup() {
        panController.shutdown()
        gestureRecognizer.reset()
    }

    /**
     * Companion object containing NDC conversion utilities.
     * Merged from the Java NDCConverter class.
     * 
     * NDC coordinates range from -1 to 1 in both axes, with (0,0) at the center.
     * Y-axis is inverted so that positive Y is up (matching OpenGL conventions).
     */
    companion object {
        data class NDCPoint(val x: Double, val y: Double)
        data class PixelPoint(val x: Int, val y: Int)

        /**
         * Convert pixel coordinates to NDC coordinates.
         * 
         * @param x Pixel X coordinate (0 to width-1)
         * @param y Pixel Y coordinate (0 to height-1)
         * @param width Canvas width in pixels
         * @param height Canvas height in pixels
         * @return NDC coordinates where x,y are in range [-1, 1]
         */
        @JvmStatic
        fun pixelToNDC(x: Int, y: Int, width: Int, height: Int): NDCPoint {
            require(width > 0 && height > 0) { "Width and height must be positive" }
            
            var ndcX = (x.toDouble() / width) * 2.0 - 1.0
            var ndcY = -((y.toDouble() / height) * 2.0 - 1.0) // Invert Y
            
            // Clamp to valid NDC range to handle edge cases
            ndcX = ndcX.coerceIn(-1.0, 1.0)
            ndcY = ndcY.coerceIn(-1.0, 1.0)
            
            return NDCPoint(ndcX, ndcY)
        }

        /**
         * Convert NDC coordinates to pixel coordinates.
         * 
         * @param ndcX NDC X coordinate (-1 to 1)
         * @param ndcY NDC Y coordinate (-1 to 1)
         * @param width Canvas width in pixels
         * @param height Canvas height in pixels
         * @return Pixel coordinates
         */
        @JvmStatic
        fun ndcToPixel(ndcX: Double, ndcY: Double, width: Int, height: Int): PixelPoint {
            require(width > 0 && height > 0) { "Width and height must be positive" }
            
            // Clamp NDC coordinates to valid range
            val clampedX = ndcX.coerceIn(-1.0, 1.0)
            val clampedY = ndcY.coerceIn(-1.0, 1.0)
            
            var x = Math.round((clampedX + 1.0) / 2.0 * width).toInt()
            var y = Math.round((-clampedY + 1.0) / 2.0 * height).toInt() // Invert Y back
            
            // Clamp to valid pixel range
            x = x.coerceIn(0, width - 1)
            y = y.coerceIn(0, height - 1)
            
            return PixelPoint(x, y)
        }

        /**
         * Convert pixel delta to NDC delta (for pan gestures).
         * Does not invert Y axis as this is a relative movement.
         * 
         * @param deltaX Pixel delta X
         * @param deltaY Pixel delta Y
         * @param width Canvas width in pixels
         * @param height Canvas height in pixels
         * @return NDC delta values
         */
        @JvmStatic
        fun pixelDeltaToNDC(deltaX: Int, deltaY: Int, width: Int, height: Int): NDCPoint {
            require(width > 0 && height > 0) { "Width and height must be positive" }
            
            val ndcDeltaX = (deltaX.toDouble() / width) * 2.0
            val ndcDeltaY = -(deltaY.toDouble() / height) * 2.0 // Still invert for consistency
            
            return NDCPoint(ndcDeltaX, ndcDeltaY)
        }

        /**
         * Apply aspect ratio correction to NDC X coordinate.
         * This is useful for maintaining circular motion in non-square viewports.
         * 
         * @param ndcX NDC X coordinate
         * @param aspectRatio Width/Height ratio
         * @return Aspect-corrected NDC X
         */
        @JvmStatic
        fun applyAspectRatio(ndcX: Double, aspectRatio: Double): Double {
            return ndcX * aspectRatio
        }

        /**
         * Check if pixel coordinates are within canvas bounds.
         * 
         * @param x Pixel X coordinate
         * @param y Pixel Y coordinate
         * @param width Canvas width
         * @param height Canvas height
         * @return true if coordinates are within bounds
         */
        @JvmStatic
        fun isInBounds(x: Int, y: Int, width: Int, height: Int): Boolean {
            return x >= 0 && x < width && y >= 0 && y < height
        }
    }
}
