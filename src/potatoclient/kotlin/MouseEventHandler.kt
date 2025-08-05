package potatoclient.kotlin

import potatoclient.java.transit.EventType
import potatoclient.kotlin.events.CommandBuilder
import potatoclient.kotlin.gestures.FrameDataProvider
import potatoclient.kotlin.gestures.GestureConfig
import potatoclient.kotlin.gestures.GestureEvent
import potatoclient.kotlin.gestures.GestureRecognizer
import potatoclient.kotlin.gestures.PanController
import potatoclient.kotlin.gestures.RotaryDirection
import potatoclient.kotlin.gestures.StreamType
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
                    val command =
                        if (streamType == StreamType.HEAT) {
                            CommandBuilder.heatCameraNextZoom()
                        } else {
                            CommandBuilder.dayCameraNextZoom()
                        }
                    callback.sendCommand(command)
                }
                e.wheelRotation > 0 -> {
                    // Wheel down = zoom out
                    val command =
                        if (streamType == StreamType.HEAT) {
                            CommandBuilder.heatCameraPrevZoom()
                        } else {
                            CommandBuilder.dayCameraPrevZoom()
                        }
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
                TransitKeys.TYPE to EventType.GESTURE,
                TransitKeys.GESTURE_TYPE to gesture.getEventType(),
                TransitKeys.TIMESTAMP to gesture.timestamp,
                TransitKeys.CANVAS_WIDTH to canvasWidth,
                TransitKeys.CANVAS_HEIGHT to canvasHeight,
                TransitKeys.ASPECT_RATIO to aspectRatio,
                TransitKeys.STREAM_TYPE to streamType.toKeyword(),
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
        val command = CommandBuilder.rotaryGotoNDC(streamType, ndcX, ndcY)
        callback.sendCommand(command)
    }

    private fun sendCVStartTrackNDC(
        ndcX: Double,
        ndcY: Double,
        frameTimestamp: Long?,
    ) {
        val command = CommandBuilder.cvStartTrackNDC(streamType, ndcX, ndcY, frameTimestamp)
        callback.sendCommand(command)
    }

    private fun sendRotaryVelocityCommand(
        azSpeed: Double,
        elSpeed: Double,
        azDir: RotaryDirection,
        elDir: RotaryDirection,
    ) {
        val command = CommandBuilder.rotarySetVelocity(azSpeed, elSpeed, azDir, elDir)
        callback.sendCommand(command)
    }

    private fun sendRotaryHaltCommand() {
        val command = CommandBuilder.rotaryHalt()
        callback.sendCommand(command)
    }

    fun cleanup() {
        panController.shutdown()
        gestureRecognizer.reset()
    }
}
