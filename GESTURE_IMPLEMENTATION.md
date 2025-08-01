# Gesture Implementation Guide for PotatoClient

## Overview

This document consolidates all gesture implementation details for PotatoClient. The system uses **interpreted gesture events only** - no raw pointer events are transmitted. All mouse/touch interactions are processed locally into high-level gestures before being sent as commands.

## Core Principles

1. **No Raw Events**: Mouse moves, clicks, and drags are NOT sent. Only interpreted gestures.
2. **NDC Coordinates**: All positions use Normalized Device Coordinates (-1 to 1 range).
3. **Zoom-Based Speed**: Pan gesture speed varies by camera zoom level.
4. **Debounced Commands**: Prevents overwhelming the server with rapid updates.
5. **Simplified Dragging**: ALL dragging is interpreted as camera rotation (pan gesture).
6. **No Swipe Detection**: Swipe gestures were intentionally removed to simplify the system. DO NOT reintroduce swipe detection - all drags should trigger pan gestures.

## Supported Gestures

### 1. Tap (Single Click)
**Action**: Rotate camera to point at tapped location  
**Command**: `rotary-goto-ndc`  
**Timing**: Must be < 300ms duration and < 20px movement

### 2. Double Tap (Double Click)
**Action**: Start computer vision tracking at location  
**Command**: `cv-start-track-ndc`  
**Timing**: Second tap within 300ms and within 10px of first tap

### 3. Pan (ANY Drag)
**Action**: Continuous camera rotation based on drag distance/direction  
**Commands**: `rotary-set-velocity` (periodic) and `rotary-halt` (on release)  
**Note**: Any drag > 20px triggers pan - no swipe detection needed

### 4. Mouse Wheel Zoom
**Action**: Change camera zoom level  
**Commands**: 
- Wheel Up: `heat-camera-next-zoom-table-pos` or `day-camera-next-zoom-table-pos`
- Wheel Down: `heat-camera-prev-zoom-table-pos` or `day-camera-prev-zoom-table-pos`

## Implementation Examples

### Kotlin: Gesture Detection and Event Generation

#### Mouse Wheel Zoom Handler
```kotlin
// In MouseEventHandler.kt
videoComponent.addMouseWheelListener { e ->
    // Convert wheel rotation to zoom commands
    when {
        e.wheelRotation < 0 -> {
            // Wheel up = zoom in
            val command = mapOf(
                "action" to if (streamType == StreamType.HEAT) 
                    "heat-camera-next-zoom-table-pos" 
                else 
                    "day-camera-next-zoom-table-pos",
                "params" to emptyMap<String, Any>()
            )
            callback.sendCommand(command)
        }
        e.wheelRotation > 0 -> {
            // Wheel down = zoom out
            val command = mapOf(
                "action" to if (streamType == StreamType.HEAT) 
                    "heat-camera-prev-zoom-table-pos" 
                else 
                    "day-camera-prev-zoom-table-pos",
                "params" to emptyMap<String, Any>()
            )
            callback.sendCommand(command)
        }
    }
}
```

#### Simplified Gesture Recognizer (No Swipe Detection)
```kotlin
// In GestureRecognizer.kt - Simplified without swipe
class GestureRecognizer(
    private val config: GestureConfig = GestureConfig(),
    private val onGesture: (GestureEvent) -> Unit
) {
    private val state = AtomicReference(GestureState.IDLE)
    private var startX: Int = 0
    private var startY: Int = 0
    private var startTime: Long = 0
    private var lastTapTime: Long = 0
    private var lastTapX: Int = 0
    private var lastTapY: Int = 0
    
    fun processMousePressed(x: Int, y: Int, button: Int, time: Long) {
        if (button != 1) return // Only handle left button
        
        startX = x
        startY = y
        startTime = time
        state.set(GestureState.PENDING)
    }
    
    fun processMouseDragged(x: Int, y: Int, time: Long) {
        val currentState = state.get()
        if (currentState == GestureState.IDLE) return
        
        val distance = sqrt(((x - startX) * (x - startX) + (y - startY) * (y - startY)).toDouble())
        
        if (currentState == GestureState.PENDING && distance > config.moveThreshold) {
            // Start pan gesture for ANY drag > threshold
            state.set(GestureState.PANNING)
            onGesture(GestureEvent.PanStart(startX, startY, time))
        } else if (currentState == GestureState.PANNING) {
            // Send pan updates
            val deltaX = x - startX
            val deltaY = y - startY
            onGesture(GestureEvent.PanMove(x, y, deltaX, deltaY, time))
        }
    }
    
    fun processMouseReleased(x: Int, y: Int, button: Int, time: Long) {
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
                        abs(y - lastTapY) < config.doubleTapTolerance) {
                        onGesture(GestureEvent.DoubleTap(x, y, time))
                        lastTapTime = 0 // Reset to prevent triple tap
                    } else {
                        // Single tap
                        onGesture(GestureEvent.Tap(x, y, time))
                        lastTapTime = time
                        lastTapX = x
                        lastTapY = y
                    }
                }
                // No swipe detection - any other release is just ignored
            }
            GestureState.PANNING -> {
                // End of pan gesture
                onGesture(GestureEvent.PanStop(x, y, time))
            }
            else -> {}
        }
        
        state.set(GestureState.IDLE)
    }
}

// Simplified GestureEvent without Swipe
sealed class GestureEvent {
    abstract val timestamp: Long
    
    data class Tap(val x: Int, val y: Int, override val timestamp: Long) : GestureEvent()
    data class DoubleTap(val x: Int, val y: Int, override val timestamp: Long) : GestureEvent()
    data class PanStart(val x: Int, val y: Int, override val timestamp: Long) : GestureEvent()
    data class PanMove(val x: Int, val y: Int, val deltaX: Int, val deltaY: Int, override val timestamp: Long) : GestureEvent()
    data class PanStop(val x: Int, val y: Int, override val timestamp: Long) : GestureEvent()
}
```

#### Gesture Event Generation
```kotlin
// In MouseEventHandler.kt - handleGesture method
private fun handleGesture(gesture: GestureEvent) {
    val canvasWidth = videoComponent.width
    val canvasHeight = videoComponent.height
    val aspectRatio = canvasWidth.toDouble() / canvasHeight.toDouble()
    
    // Create Transit event message
    val baseEvent = mutableMapOf(
        "type" to EventType.GESTURE.key,  // "gesture"
        "gesture-type" to when(gesture) {
            is GestureEvent.Tap -> EventType.TAP.key          // "tap"
            is GestureEvent.DoubleTap -> EventType.DOUBLE_TAP.key  // "doubletap"
            is GestureEvent.PanStart -> EventType.PAN_START.key    // "panstart"
            is GestureEvent.PanMove -> EventType.PAN_MOVE.key      // "panmove"
            is GestureEvent.PanStop -> EventType.PAN_STOP.key      // "panstop"
        },
        "timestamp" to gesture.timestamp,
        "canvas-width" to canvasWidth,
        "canvas-height" to canvasHeight,
        "aspect-ratio" to aspectRatio,
        "stream-type" to streamType.name.lowercase()
    )
    
    // Add gesture-specific data
    when (gesture) {
        is GestureEvent.Tap -> {
            baseEvent["x"] = gesture.x
            baseEvent["y"] = gesture.y
            baseEvent["ndc-x"] = pixelToNDC(gesture.x, canvasWidth)
            baseEvent["ndc-y"] = pixelToNDC(gesture.y, canvasHeight, true)
            
            // Send command immediately via request message
            sendRotaryGotoNDC(
                pixelToNDC(gesture.x, canvasWidth),
                pixelToNDC(gesture.y, canvasHeight, true)
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
                frameData?.timestamp
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
    }
    
    // Send gesture event via Transit
    callback.onGestureEvent(baseEvent)
}

// Helper function for NDC conversion
private fun pixelToNDC(value: Int, dimension: Int, invertY: Boolean = false): Double {
    val ndc = (value.toDouble() / dimension) * 2.0 - 1.0
    return if (invertY) -ndc else ndc
}
```

#### Command Sending via Request Messages
```kotlin
// Send commands through the video stream's request message system
private fun sendRotaryGotoNDC(ndcX: Double, ndcY: Double) {
    val command = mapOf(
        "action" to "rotary-goto-ndc",
        "channel" to streamType.name.lowercase(),
        "x" to ndcX,
        "y" to ndcY
    )
    
    // Send via TransitMessageProtocol as a request
    protocol.sendRequest("rotary-goto-ndc", command)
}

private fun sendCVStartTrackNDC(ndcX: Double, ndcY: Double, frameTimestamp: Long?) {
    val command = mutableMapOf(
        "action" to "cv-start-track-ndc",
        "channel" to streamType.name.lowercase(),
        "x" to ndcX,
        "y" to ndcY
    )
    
    frameTimestamp?.let {
        command["frame-timestamp"] = it
    }
    
    protocol.sendRequest("cv-start-track-ndc", command)
}

// PanController sends velocity commands periodically (every 120ms)
class PanController(
    private val onRotaryCommand: (azSpeed: Double, elSpeed: Double, azDir: RotaryDirection, elDir: RotaryDirection) -> Unit,
    private val onHaltCommand: () -> Unit,
    private val streamType: StreamType
) {
    private fun sendRotaryCommands() {
        val state = currentState.get()
        if (state.isInDeadZone) {
            onHaltCommand()
        } else {
            val azDir = if (lastNdcDeltaX >= 0) RotaryDirection.CLOCKWISE else RotaryDirection.COUNTER_CLOCKWISE
            val elDir = if (lastNdcDeltaY >= 0) RotaryDirection.CLOCKWISE else RotaryDirection.COUNTER_CLOCKWISE
            onRotaryCommand(state.azimuthSpeed, state.elevationSpeed, azDir, elDir)
        }
    }
}

// In MouseEventHandler - wire up PanController
private val panController = PanController(
    onRotaryCommand = { azSpeed, elSpeed, azDir, elDir ->
        val command = mapOf(
            "action" to "rotary-set-velocity",
            "azimuth-speed" to azSpeed,
            "elevation-speed" to elSpeed,
            "azimuth-direction" to azDir.name.lowercase().replace("_", "-"),
            "elevation-direction" to elDir.name.lowercase().replace("_", "-")
        )
        protocol.sendRequest("rotary-set-velocity", command)
    },
    onHaltCommand = {
        val command = mapOf("action" to "rotary-halt")
        protocol.sendRequest("rotary-halt", command)
    },
    streamType = streamType
)
```

### Clojure: Command Building in SimpleCommandBuilder

```kotlin
// In SimpleCommandBuilder.kt - Add zoom command builders
private fun buildHeatCameraNextZoomTablePos(): ByteArray {
    val zoom = JonSharedCmdHeatCamera.Zoom.newBuilder()
        .setNextZoomTablePos(JonSharedCmdHeatCamera.NextZoomTablePos.newBuilder().build())
        .build()
        
    val heatRoot = JonSharedCmdHeatCamera.Root.newBuilder()
        .setZoom(zoom)
        .build()
        
    return JonSharedCmd.Root.newBuilder()
        .setProtocolVersion(1)
        .setHeatCamera(heatRoot)
        .build()
        .toByteArray()
}

private fun buildHeatCameraPrevZoomTablePos(): ByteArray {
    val zoom = JonSharedCmdHeatCamera.Zoom.newBuilder()
        .setPrevZoomTablePos(JonSharedCmdHeatCamera.PrevZoomTablePos.newBuilder().build())
        .build()
        
    val heatRoot = JonSharedCmdHeatCamera.Root.newBuilder()
        .setZoom(zoom)
        .build()
        
    return JonSharedCmd.Root.newBuilder()
        .setProtocolVersion(1)
        .setHeatCamera(heatRoot)
        .build()
        .toByteArray()
}

private fun buildDayCameraNextZoomTablePos(): ByteArray {
    val zoom = JonSharedCmdDayCamera.Zoom.newBuilder()
        .setNextZoomTablePos(JonSharedCmdDayCamera.NextZoomTablePos.newBuilder().build())
        .build()
        
    val dayRoot = JonSharedCmdDayCamera.Root.newBuilder()
        .setZoom(zoom)
        .build()
        
    return JonSharedCmd.Root.newBuilder()
        .setProtocolVersion(1)
        .setDayCamera(dayRoot)
        .build()
        .toByteArray()
}

private fun buildDayCameraPrevZoomTablePos(): ByteArray {
    val zoom = JonSharedCmdDayCamera.Zoom.newBuilder()
        .setPrevZoomTablePos(JonSharedCmdDayCamera.PrevZoomTablePos.newBuilder().build())
        .build()
        
    val dayRoot = JonSharedCmdDayCamera.Root.newBuilder()
        .setZoom(zoom)
        .build()
        
    return JonSharedCmd.Root.newBuilder()
        .setProtocolVersion(1)
        .setDayCamera(dayRoot)
        .build()
        .toByteArray()
}

// Update the build function to handle zoom commands
fun build(transitCommand: Map<String, Any>): ByteArray? {
    return when (val action = transitCommand["action"] as? String) {
        "ping" -> buildPing()
        "rotary-halt" -> buildRotaryHalt()
        "rotary-goto-ndc" -> buildRotaryGotoNDC(transitCommand["params"] as? Map<*, *>)
        "rotary-set-velocity" -> buildRotarySetVelocity(transitCommand["params"] as? Map<*, *>)
        "cv-start-track-ndc" -> buildCVStartTrackNDC(transitCommand["params"] as? Map<*, *>)
        "heat-camera-next-zoom-table-pos" -> buildHeatCameraNextZoomTablePos()
        "heat-camera-prev-zoom-table-pos" -> buildHeatCameraPrevZoomTablePos()
        "day-camera-next-zoom-table-pos" -> buildDayCameraNextZoomTablePos()
        "day-camera-prev-zoom-table-pos" -> buildDayCameraPrevZoomTablePos()
        else -> {
            logger.warn("Unknown command action: $action")
            null
        }
    }
}
```

### Clojure: IPC Request Handler Update

```clojure
;; In ipc.clj - Update request handler to include zoom commands
(defmethod handle-message :request
  [_ stream-key payload]
  (let [action (:action payload)]
    ;; Gesture-based commands that need forwarding
    (if (contains? #{"rotary-set-velocity" "rotary-halt" "rotary-goto-ndc" 
                     "cv-start-track-ndc" "forward-command"
                     "heat-camera-next-zoom-table-pos" "heat-camera-prev-zoom-table-pos"
                     "day-camera-next-zoom-table-pos" "day-camera-prev-zoom-table-pos"} action)
      (let [subprocess-launcher (requiring-resolve 'potatoclient.transit.subprocess-launcher/send-message)
            transit-core (requiring-resolve 'potatoclient.transit.core/create-message)]
        (when (and subprocess-launcher transit-core)
          ;; Create command message with action and data from request
          (let [command-msg (@transit-core :command 
                              (merge {:action action} 
                                     (dissoc payload :action :process)))]
            (@subprocess-launcher :command command-msg)
            (logging/log-debug
              {:id ::forwarded-command
               :data {:stream stream-key
                      :action action
                      :command command-msg}
               :msg (str "Forwarded " action " command from " stream-key)}))))
      ;; Log unknown request types
      (logging/log-warn
        {:id ::unknown-request-type
         :data {:stream stream-key
                :request-type action
                :payload payload}
         :msg (str "Unknown request type: " action)}))))
```

## Configuration

### Gesture Timing (resources/config/gestures.edn)
```clojure
{:gesture-config
 {:move-threshold 20              ; pixels before pan starts
  :tap-long-press-threshold 300   ; ms for long press detection
  :double-tap-threshold 300       ; ms between taps for double-tap
  :pan-update-interval 120       ; ms between pan updates
  :double-tap-tolerance 10       ; pixel tolerance for double-tap
  :zoom-debounce-time 200}       ; ms debounce for wheel zoom
```

### Zoom-Based Speed Configuration
```clojure
:zoom-speed-config
{:heat [{:zoom-table-index 0
         :max-rotation-speed 0.1
         :min-rotation-speed 0.0001
         :ndc-threshold 0.5
         :dead-zone-radius 0.05
         :curve-steepness 4.0}
        ;; ... more zoom levels
        ]
 :day [{:zoom-table-index 0
        :max-rotation-speed 0.05
        ;; ... similar structure
        }]}
```

## Message Flow

### Event Message (for logging/debugging only)
```clojure
;; Transit envelope
{:msg-type :event
 :msg-id "unique-uuid"
 :timestamp 1234567890
 :payload {:type "gesture"
           :gesture-type "tap"
           :x 400
           :y 300
           :ndc-x 0.5
           :ndc-y -0.25
           :canvas-width 800
           :canvas-height 600
           :stream-type "heat"}}
```

### Request Message (for commands)
```clojure
;; Commands are sent as requests
{:msg-type :request
 :msg-id "unique-uuid"
 :timestamp 1234567890
 :payload {:action "rotary-goto-ndc"
           :channel "heat"
           :x 0.5
           :y -0.25}}
```

## Key Implementation Points

1. **No Navigation Events**: Remove all mouse-move, mouse-click, mouse-drag event forwarding
2. **Gesture-Only**: Only send interpreted gestures and their resulting commands
3. **Zoom via Wheel**: Mouse wheel directly triggers zoom commands, no intermediate events
4. **Local Processing**: All gesture detection happens in the video subprocess
5. **Command Forwarding**: Commands go through request messages to command subprocess
6. **Debouncing**: 
   - Pan: Commands sent every 120ms
   - Zoom: 200ms debounce on wheel events
7. **NDC Coordinates**: All positions normalized, Y-axis inverted for screen coords
8. **Aspect Ratio**: Pan X-delta multiplied by aspect ratio for consistent movement

## Benefits

- **Reduced Network Traffic**: Only high-level commands, not raw events
- **Consistent Behavior**: Same gestures work identically across platforms
- **Clean Protocol**: Clear separation between UI events and commands
- **Better Performance**: Local gesture processing reduces latency
- **Extensible**: Easy to add new gestures without protocol changes