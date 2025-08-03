package potatoclient.kotlin

import potatoclient.kotlin.gestures.FrameDataProvider
import potatoclient.kotlin.gestures.GestureConfig
import potatoclient.kotlin.gestures.GestureEvent
import potatoclient.kotlin.gestures.GestureRecognizer
import potatoclient.kotlin.gestures.PanController
import potatoclient.kotlin.gestures.RotaryDirection
import potatoclient.kotlin.gestures.StreamType
import potatoclient.java.transit.EventType
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
        fun onGestureEvent(event: Map<String, Any>)

        fun sendCommand(command: Map<String, Any>)
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
                        mapOf(
                            "action" to
                                if (streamType == StreamType.HEAT) {
                                    "heat-camera-next-zoom-table-pos"
                                } else {
                                    "day-camera-next-zoom-table-pos"
                                },
                            "params" to emptyMap<String, Any>(),
                        )
                    callback.sendCommand(command)
                }
                e.wheelRotation > 0 -> {
                    // Wheel down = zoom out
                    val command =
                        mapOf(
                            "action" to
                                if (streamType == StreamType.HEAT) {
                                    "heat-camera-prev-zoom-table-pos"
                                } else {
                                    "day-camera-prev-zoom-table-pos"
                                },
                            "params" to emptyMap<String, Any>(),
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
            mutableMapOf(
                "type" to EventType.GESTURE,
                "gesture-type" to gesture.getEventType(),
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

                // Send rotary-goto-ndc command
                sendRotaryGotoNDC(
                    pixelToNDC(gesture.x, canvasWidth),
                    pixelToNDC(gesture.y, canvasHeight, true),
                )
            }
            is GestureEvent.DoubleTap -> {
                baseEvent["x"] = gesture.x
                baseEvent["y"] = gesture.y
                baseEvent["ndc-x"] = pixelToNDC(gesture.x, canvasWidth)
                baseEvent["ndc-y"] = pixelToNDC(gesture.y, canvasHeight, true)

                // Get frame timestamp if available
                val frameData = frameDataProvider?.getFrameData()
                frameData?.let {
                    baseEvent["frame-timestamp"] = it.timestamp
                    baseEvent["frame-duration"] = it.duration
                }

                // Send CV tracking command
                sendCVStartTrackNDC(
                    pixelToNDC(gesture.x, canvasWidth),
                    pixelToNDC(gesture.y, canvasHeight, true),
                    frameData?.timestamp,
                )
            }
            is GestureEvent.PanStart -> {
                baseEvent["x"] = gesture.x
                baseEvent["y"] = gesture.y
                baseEvent["ndc-x"] = pixelToNDC(gesture.x, canvasWidth)
                baseEvent["ndc-y"] = pixelToNDC(gesture.y, canvasHeight, true)
                panController.startPan()
            }
            is GestureEvent.PanMove -> {
                baseEvent["x"] = gesture.x
                baseEvent["y"] = gesture.y
                baseEvent["delta-x"] = gesture.deltaX
                baseEvent["delta-y"] = gesture.deltaY

                // Apply aspect ratio adjustment to X delta
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
                panController.stopPan()
            }
            // No swipe handling - removed intentionally
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

    // Command sending methods
    private fun sendRotaryGotoNDC(
        ndcX: Double,
        ndcY: Double,
    ) {
        val command =
            mapOf(
                "action" to "rotary-goto-ndc",
                "params" to
                    mapOf(
                        "channel" to streamType.name.lowercase(),
                        "x" to ndcX,
                        "y" to ndcY,
                    ),
            )
        callback.sendCommand(command)
    }

    private fun sendCVStartTrackNDC(
        ndcX: Double,
        ndcY: Double,
        frameTimestamp: Long?,
    ) {
        val params =
            mutableMapOf(
                "channel" to streamType.name.lowercase(),
                "x" to ndcX,
                "y" to ndcY,
            )

        frameTimestamp?.let {
            params["frame-timestamp"] = it
        }

        val command =
            mapOf(
                "action" to "cv-start-track-ndc",
                "params" to params,
            )
        callback.sendCommand(command)
    }

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

    fun cleanup() {
        panController.shutdown()
        gestureRecognizer.reset()
    }
}
