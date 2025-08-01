# Gesture Implementation Plan for PotatoClient Desktop

## Overview

This plan outlines the implementation of a gesture recognition system for the PotatoClient desktop application, replacing basic mouse events with sophisticated gesture detection similar to the web frontend. The implementation will focus on single-point gestures (no multi-touch) and leverage the existing Transit-based architecture.

## Reference Documentation

This implementation is based on the research and analysis in [`NDC_GESTURES_IMPLEMENTATION.md`](NDC_GESTURES_IMPLEMENTATION.md), which documents the web frontend's gesture system. Key concepts we're adapting:

- **NDC Coordinate System**: All positions normalized to -1 to 1 range
- **Gesture Types**: Tap, double-tap, pan, swipe (no pinch zoom for desktop)
- **Timing Configurations**: From `gestures.json` and `zoom_table_rotary_touch.json`
- **Command Mappings**: Gesture → protobuf command conversions

## Architecture Design

### Gesture Recognition Flow

```
Mouse Events (JFrame/Canvas)
    ↓
GestureRecognizer (Kotlin) - New Component
    ↓
Gesture Events (Transit Messages)
    ↓
Gesture Handler (Clojure) - New Component
    ↓
Transit Commands
    ↓
Command Subprocess → Server
```

## Phase 1: Kotlin Gesture Recognition Infrastructure

### 1.1 Create Gesture Recognition Components

**New File: `kotlin-subprocesses/src/main/kotlin/potatoclient/video/gestures/GestureRecognizer.kt`**

```kotlin
package potatoclient.video.gestures

import potatoclient.video.events.EventFilter
import java.util.concurrent.atomic.AtomicReference
import java.util.concurrent.atomic.AtomicLong
import kotlin.math.abs
import kotlin.math.sqrt
import kotlin.math.atan2
import kotlin.math.PI

enum class GestureState {
    IDLE,
    PENDING,
    PANNING,
    DRAGGING
}

data class GestureConfig(
    val moveThreshold: Int = 20,          // pixels before gesture starts (from gestures.json)
    val tapLongPressThreshold: Long = 300, // ms for long press
    val doubleTapThreshold: Long = 300,    // ms between taps
    val swipeThreshold: Int = 100,         // pixels for swipe
    val panUpdateInterval: Long = 120,     // ms between pan updates
    val doubleTapTolerance: Int = 10      // pixels tolerance for double tap position
)

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
    
    // Thread-safe tracking for pan gesture updates
    private val lastPanUpdate = AtomicLong(0)
    private var panStartNotified = false
    
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
            // Start pan gesture
            state.set(GestureState.PANNING)
            panStartNotified = true
            lastPanUpdate.set(time)
            onGesture(GestureEvent.PanStart(startX, startY, time))
        } else if (currentState == GestureState.PANNING) {
            // Throttle pan updates to config.panUpdateInterval
            val lastUpdate = lastPanUpdate.get()
            if (time - lastUpdate >= config.panUpdateInterval) {
                // Calculate deltas from start position (not incremental)
                val deltaX = x - startX
                val deltaY = y - startY
                onGesture(GestureEvent.PanMove(x, y, deltaX, deltaY, time))
                lastPanUpdate.set(time)
            }
        }
    }
    
    fun processMouseReleased(x: Int, y: Int, button: Int, time: Long) {
        if (button != 1) return
        
        val currentState = state.get()
        val elapsedTime = time - startTime
        val distance = sqrt(((x - startX) * (x - startX) + (y - startY) * (y - startY)).toDouble())
        
        when (currentState) {
            GestureState.PENDING -> {
                if (distance <= config.moveThreshold && elapsedTime < config.tapLongPressThreshold) {
                    // Check for double tap
                    if (time - lastTapTime < config.doubleTapThreshold &&
                        abs(x - lastTapX) < config.doubleTapTolerance && 
                        abs(y - lastTapY) < config.doubleTapTolerance) {
                        onGesture(GestureEvent.DoubleTap(x, y, time))
                        lastTapTime = 0
                    } else {
                        // Single tap
                        onGesture(GestureEvent.Tap(x, y, time))
                        lastTapTime = time
                        lastTapX = x
                        lastTapY = y
                    }
                } else if (distance > config.swipeThreshold && elapsedTime < config.tapLongPressThreshold) {
                    // Swipe gesture
                    val direction = calculateSwipeDirection(startX, startY, x, y)
                    onGesture(GestureEvent.Swipe(direction, distance.toInt(), time))
                }
            }
            GestureState.PANNING -> {
                onGesture(GestureEvent.PanStop(x, y, time))
            }
            else -> {}
        }
        
        state.set(GestureState.IDLE)
        panStartNotified = false
    }
    
    private fun calculateSwipeDirection(x1: Int, y1: Int, x2: Int, y2: Int): SwipeDirection {
        val dx = x2 - x1
        val dy = y2 - y1
        val angle = atan2(dy.toDouble(), dx.toDouble())
        
        // Convert angle to direction (matching web frontend logic)
        return when {
            angle > -PI/4 && angle <= PI/4 -> SwipeDirection.RIGHT
            angle > PI/4 && angle <= 3*PI/4 -> SwipeDirection.DOWN
            angle > 3*PI/4 || angle <= -3*PI/4 -> SwipeDirection.LEFT
            else -> SwipeDirection.UP
        }
    }
    
    fun reset() {
        state.set(GestureState.IDLE)
        panStartNotified = false
        lastPanUpdate.set(0)
    }
}
```

### 1.2 Define Gesture Events

**New File: `kotlin-subprocesses/src/main/kotlin/potatoclient/video/gestures/GestureEvent.kt`**

```kotlin
package potatoclient.video.gestures

enum class SwipeDirection {
    UP, DOWN, LEFT, RIGHT
}

sealed class GestureEvent {
    abstract val timestamp: Long
    
    data class Tap(val x: Int, val y: Int, override val timestamp: Long) : GestureEvent()
    data class DoubleTap(val x: Int, val y: Int, override val timestamp: Long) : GestureEvent()
    data class PanStart(val x: Int, val y: Int, override val timestamp: Long) : GestureEvent()
    data class PanMove(val x: Int, val y: Int, val deltaX: Int, val deltaY: Int, override val timestamp: Long) : GestureEvent()
    data class PanStop(val x: Int, val y: Int, override val timestamp: Long) : GestureEvent()
    data class Swipe(val direction: SwipeDirection, val distance: Int, override val timestamp: Long) : GestureEvent()
}
```

### 1.3 Create Pan Controller for Desktop

Based on the web frontend's [`RotaryPanController`](examples/web/frontend/ts/components/lit/jonStreamGUI/rotaryPanController.ts), we need a similar component:

**New File: `kotlin-subprocesses/src/main/kotlin/potatoclient/video/gestures/PanController.kt`**

```kotlin
package potatoclient.video.gestures

import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt

data class PanState(
    val azimuthSpeed: Double = 0.0,
    val elevationSpeed: Double = 0.0,
    val isInDeadZone: Boolean = true
)

class PanController(
    private val onRotaryCommand: (azSpeed: Double, elSpeed: Double, azDir: RotaryDirection, elDir: RotaryDirection) -> Unit,
    private val onHaltCommand: () -> Unit,
    private val streamType: StreamType
) {
    companion object {
        const val UPDATE_INTERVAL = 120L // ms, matching web frontend
        
        // Load configuration from resources
        fun loadSpeedConfigs(): Map<StreamType, List<SpeedConfig>> {
            // In production, load from resources/config/gestures.edn
            // For now, return defaults matching zoom_table_rotary_touch.json
            return mapOf(
                StreamType.HEAT to listOf(
                    SpeedConfig(0.1, 0.0001, 0.5, 0.05, 4.0),   // zoom 0
                    SpeedConfig(0.25, 0.0001, 0.5, 0.05, 4.0),  // zoom 1
                    SpeedConfig(0.5, 0.0001, 0.5, 0.05, 4.0),   // zoom 2
                    SpeedConfig(1.0, 0.0001, 0.5, 0.05, 4.0)    // zoom 3+
                ),
                StreamType.DAY to listOf(
                    SpeedConfig(0.05, 0.0001, 0.5, 0.05, 4.0),  // zoom 0
                    SpeedConfig(0.15, 0.0001, 0.5, 0.05, 4.0),  // zoom 1
                    SpeedConfig(0.5, 0.0001, 0.5, 0.05, 4.0),   // zoom 2
                    SpeedConfig(1.0, 0.0001, 0.5, 0.05, 4.0)    // zoom 3+
                )
            )
        }
    }
    
    private val executor: ScheduledExecutorService = Executors.newSingleThreadScheduledExecutor()
    private var updateTask: ScheduledFuture<*>? = null
    private val currentState = AtomicReference(PanState())
    private var speedConfig: SpeedConfig = SpeedConfig()
    private val speedConfigs = loadSpeedConfigs()
    
    // Track NDC deltas for direction calculation
    private var lastNdcDeltaX: Double = 0.0
    private var lastNdcDeltaY: Double = 0.0
    
    fun startPan() {
        currentState.set(PanState())
        startPeriodicUpdate()
    }
    
    fun updatePan(ndcDeltaX: Double, ndcDeltaY: Double, zoomLevel: Int) {
        // Update speed config based on zoom level
        speedConfig = getSpeedConfigForZoom(zoomLevel)
        
        // Store deltas for direction calculation
        lastNdcDeltaX = ndcDeltaX
        lastNdcDeltaY = ndcDeltaY
        
        val magnitude = sqrt(ndcDeltaX * ndcDeltaX + ndcDeltaY * ndcDeltaY)
        val isInDeadZone = magnitude <= speedConfig.deadZoneRadius
        
        if (!isInDeadZone) {
            val (azSpeed, elSpeed) = calculateRotationSpeeds(ndcDeltaX, ndcDeltaY)
            currentState.set(PanState(azSpeed, elSpeed, false))
        } else {
            currentState.set(PanState(0.0, 0.0, true))
        }
    }
    
    fun stopPan() {
        stopPeriodicUpdate()
        onHaltCommand()
    }
    
    private fun calculateRotationSpeeds(ndcDeltaX: Double, ndcDeltaY: Double): Pair<Double, Double> {
        val magnitude = sqrt(ndcDeltaX * ndcDeltaX + ndcDeltaY * ndcDeltaY)
        
        // Normalize directions
        val normalizedDeltaX = ndcDeltaX / magnitude
        val normalizedDeltaY = ndcDeltaY / magnitude
        
        // Apply dead zone
        val adjustedMagnitude = (magnitude - speedConfig.deadZoneRadius).coerceAtLeast(0.0)
        val maxMagnitude = speedConfig.ndcThreshold - speedConfig.deadZoneRadius
        val normalizedMagnitude = (adjustedMagnitude / maxMagnitude).coerceAtMost(1.0)
        
        // Apply curve interpolation (matching web frontend)
        val curvedMagnitude = normalizedMagnitude.pow(speedConfig.curveSteepness)
        
        // Calculate speed
        val speed = speedConfig.minRotationSpeed + 
                   (speedConfig.maxRotationSpeed - speedConfig.minRotationSpeed) * curvedMagnitude
        
        return Pair(
            abs(normalizedDeltaX * speed).coerceAtLeast(speedConfig.minRotationSpeed),
            abs(normalizedDeltaY * speed).coerceAtLeast(speedConfig.minRotationSpeed)
        )
    }
    
    private fun startPeriodicUpdate() {
        updateTask?.cancel(false)
        updateTask = executor.scheduleAtFixedRate({
            sendRotaryCommands()
        }, 0, UPDATE_INTERVAL, TimeUnit.MILLISECONDS)
    }
    
    private fun stopPeriodicUpdate() {
        updateTask?.cancel(false)
        updateTask = null
    }
    
    private fun sendRotaryCommands() {
        val state = currentState.get()
        if (state.isInDeadZone) {
            onHaltCommand()
        } else {
            // Calculate directions based on NDC delta signs
            val azDir = if (lastNdcDeltaX >= 0) {
                RotaryDirection.CLOCKWISE
            } else {
                RotaryDirection.COUNTER_CLOCKWISE
            }
            
            val elDir = if (lastNdcDeltaY >= 0) {
                RotaryDirection.CLOCKWISE
            } else {
                RotaryDirection.COUNTER_CLOCKWISE
            }
            
            onRotaryCommand(state.azimuthSpeed, state.elevationSpeed, azDir, elDir)
        }
    }
    
    private fun getSpeedConfigForZoom(zoomLevel: Int): SpeedConfig {
        val configs = speedConfigs[streamType] ?: return SpeedConfig()
        return configs.getOrNull(zoomLevel) ?: configs.lastOrNull() ?: SpeedConfig()
    }
    
    fun shutdown() {
        stopPeriodicUpdate()
        executor.shutdown()
    }
}

data class SpeedConfig(
    val maxRotationSpeed: Double = 1.0,
    val minRotationSpeed: Double = 0.0001,
    val ndcThreshold: Double = 0.5,
    val deadZoneRadius: Double = 0.05,
    val curveSteepness: Double = 4.0
)
```

### 1.4 Integrate with MouseEventHandler

**Modify: `kotlin-subprocesses/src/main/kotlin/potatoclient/video/events/MouseEventHandler.kt`**

Add gesture recognition:

```kotlin
class MouseEventHandler(
    private val canvas: Component,
    private val streamType: StreamType,
    private val frameDataProvider: FrameDataProvider,
    private val eventSender: (Map<String, Any>) -> Unit
) {
    // Load gesture config from Transit message or use defaults
    private val gestureConfig = GestureConfig(
        moveThreshold = 20,
        tapLongPressThreshold = 300,
        doubleTapThreshold = 300,
        swipeThreshold = 100,
        panUpdateInterval = 120,
        doubleTapTolerance = 10
    )
    
    private val gestureRecognizer = GestureRecognizer(gestureConfig) { gesture ->
        handleGesture(gesture)
    }
    
    // Pan controller for continuous rotation
    private val panController = PanController(
        onRotaryCommand = { azSpeed, elSpeed, azDir, elDir ->
            sendRotaryVelocityCommand(azSpeed, elSpeed, azDir, elDir)
        },
        onHaltCommand = {
            sendRotaryHaltCommand()
        },
        streamType = streamType
    )
    
    // Modify existing mouse event handlers to feed gesture recognizer
    override fun mousePressed(e: MouseEvent) {
        super.mousePressed(e)
        gestureRecognizer.processMousePressed(e.x, e.y, e.button, System.currentTimeMillis())
    }
    
    override fun mouseDragged(e: MouseEvent) {
        super.mouseDragged(e)
        gestureRecognizer.processMouseDragged(e.x, e.y, System.currentTimeMillis())
    }
    
    override fun mouseReleased(e: MouseEvent) {
        super.mouseReleased(e)
        gestureRecognizer.processMouseReleased(e.x, e.y, e.button, System.currentTimeMillis())
    }
    
    private fun handleGesture(gesture: GestureEvent) {
        val frameData = frameDataProvider.getFrameData()
        val canvasWidth = canvas.width
        val canvasHeight = canvas.height
        
        // Calculate aspect ratio for pan gesture adjustment
        val aspectRatio = canvasWidth.toDouble() / canvasHeight.toDouble()
        
        val baseEvent = mutableMapOf(
            "type" to "gesture",
            "gesture-type" to gesture.javaClass.simpleName.lowercase(),
            "timestamp" to gesture.timestamp,
            "canvas-width" to canvasWidth,
            "canvas-height" to canvasHeight,
            "aspect-ratio" to aspectRatio
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
                val currentZoomLevel = getCurrentZoomLevel() // From state or frame data
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
        
        frameData?.let {
            baseEvent["frame-timestamp"] = it.timestamp
            baseEvent["frame-duration"] = it.duration
        }
        
        eventSender(baseEvent)
    }
    
    private fun pixelToNDC(value: Int, dimension: Int, invertY: Boolean = false): Double {
        val ndc = (value.toDouble() / dimension) * 2.0 - 1.0
        return if (invertY) -ndc else ndc
    }
    
    // Send commands via Transit
    private fun sendRotaryVelocityCommand(azSpeed: Double, elSpeed: Double, 
                                         azDir: RotaryDirection, elDir: RotaryDirection) {
        val command = mapOf(
            "action" to "rotary-set-velocity",
            "params" to mapOf(
                "azimuth-speed" to azSpeed,
                "elevation-speed" to elSpeed,
                "azimuth-direction" to azDir.name.lowercase(),
                "elevation-direction" to elDir.name.lowercase()
            )
        )
        // Send via existing Transit infrastructure
        sendCommand(command)
    }
    
    private fun sendRotaryHaltCommand() {
        val command = mapOf(
            "action" to "rotary-halt",
            "params" to emptyMap<String, Any>()
        )
        sendCommand(command)
    }
    
    // Cleanup on disconnect
    fun cleanup() {
        panController.shutdown()
        gestureRecognizer.reset()
    }
}

// Rotary direction enum matching protobuf
enum class RotaryDirection {
    CLOCKWISE,
    COUNTER_CLOCKWISE
}
```

## Phase 2: Clojure Gesture Handling

### 2.1 Update Specs for Gestures

**Modify: `src/potatoclient/specs.clj`**

Add gesture-related specs:

```clojure
;; Gesture event types
(def gesture-type
  [:enum "tap" "doubletap" "panstart" "panmove" "panstop" "swipe"])

(def swipe-direction
  [:enum "up" "down" "left" "right"])

(def gesture-event
  [:map
   [:type [:= "gesture"]]
   [:gesture-type gesture-type]
   [:timestamp int?]
   [:canvas-width int?]
   [:canvas-height int?]
   [:stream-type {:optional true} string?]
   [:x {:optional true} int?]
   [:y {:optional true} int?]
   [:ndc-x {:optional true} number?]
   [:ndc-y {:optional true} number?]
   [:delta-x {:optional true} int?]
   [:delta-y {:optional true} int?]
   [:ndc-delta-x {:optional true} number?]
   [:ndc-delta-y {:optional true} number?]
   [:direction {:optional true} swipe-direction]
   [:distance {:optional true} int?]
   [:frame-timestamp {:optional true} int?]
   [:frame-duration {:optional true} int?]])

(def speed-config
  [:map
   [:max-rotation-speed number?]
   [:min-rotation-speed number?]
   [:ndc-threshold number?]
   [:dead-zone-radius number?]
   [:curve-steepness number?]])
```

### 2.2 Create Gesture Handler

**New File: `src/potatoclient/gestures/handler.clj`**

```clojure
(ns potatoclient.gestures.handler
  (:require [com.fulcrologic.guardrails.malli.core :refer [>defn =>]]
            [potatoclient.transit.commands :as commands]
            [potatoclient.transit.app-db :as app-db]
            [potatoclient.specs :as specs]
            [potatoclient.logging :as logging]
            [potatoclient.process :as process]))

(>defn handle-tap-gesture
  "Handle single tap gesture - rotate camera to NDC position"
  [{:keys [ndc-x ndc-y stream-type] :as gesture}]
  [::specs/gesture-event => nil?]
  (logging/log-info "Tap gesture" {:ndc-x ndc-x :ndc-y ndc-y :stream stream-type})
  (let [channel (if (= stream-type "heat") :heat :day)]
    (process/send-command :command (commands/rotary-goto-ndc channel ndc-x ndc-y)))
  nil)

(>defn handle-double-tap-gesture
  "Handle double tap gesture - start CV tracking at NDC position"
  [{:keys [ndc-x ndc-y stream-type frame-timestamp] :as gesture}]
  [::specs/gesture-event => nil?]
  (logging/log-info "Double-tap gesture" {:ndc-x ndc-x :ndc-y ndc-y :stream stream-type})
  (let [channel (if (= stream-type "heat") :heat :day)]
    (process/send-command :command 
      (commands/cv-start-track-ndc channel ndc-x ndc-y frame-timestamp)))
  nil)

(>defn handle-pan-start-gesture
  "Handle pan gesture start"
  [{:keys [ndc-x ndc-y] :as gesture}]
  [::specs/gesture-event => nil?]
  (logging/log-debug "Pan start" {:ndc-x ndc-x :ndc-y ndc-y})
  (app-db/update-app-db [:gestures :pan] 
    {:active true
     :start-x ndc-x
     :start-y ndc-y
     :last-update (System/currentTimeMillis)})
  nil)

(>defn handle-pan-move-gesture
  "Handle pan gesture movement - send rotary velocity commands"
  [{:keys [ndc-delta-x ndc-delta-y stream-type] :as gesture}]
  [::specs/gesture-event => nil?]
  (let [pan-state (app-db/get-in-app-db [:gestures :pan])
        now (System/currentTimeMillis)]
    (when (and (:active pan-state)
               (> (- now (:last-update pan-state)) 100)) ; Throttle to ~10Hz
      (let [;; Get current zoom level for the active stream
            zoom-key (if (= stream-type "heat") :heat-zoom :day-zoom)
            zoom-level (app-db/get-in-app-db [:camera zoom-key] 0)
            speed-config (get-speed-config-for-zoom stream-type zoom-level)
            [az-speed el-speed] (calculate-rotation-speeds 
                                  ndc-delta-x ndc-delta-y speed-config)
            ;; Determine rotation directions based on delta signs
            az-direction (if (pos? ndc-delta-x) :clockwise :counter-clockwise)
            el-direction (if (pos? ndc-delta-y) :clockwise :counter-clockwise)]
        (process/send-command :command 
          (commands/rotary-set-velocity az-speed el-speed az-direction el-direction))
        (app-db/update-app-db [:gestures :pan :last-update] now))))
  nil)

(>defn handle-pan-stop-gesture
  "Handle pan gesture stop"
  [gesture]
  [::specs/gesture-event => nil?]
  (logging/log-debug "Pan stop")
  (process/send-command :command (commands/rotary-halt))
  (app-db/update-app-db [:gestures :pan] {:active false})
  nil)

(>defn handle-swipe-gesture
  "Handle swipe gesture"
  [{:keys [direction] :as gesture}]
  [::specs/gesture-event => nil?]
  (logging/log-info "Swipe gesture" {:direction direction})
  ;; Can be used for UI navigation or other actions
  nil)

(>defn handle-gesture-event
  "Main gesture event dispatcher"
  [event]
  [::specs/stream-event => nil?]
  (let [gesture-type (keyword (:gesture-type event))]
    (case gesture-type
      :tap (handle-tap-gesture event)
      :doubletap (handle-double-tap-gesture event)
      :panstart (handle-pan-start-gesture event)
      :panmove (handle-pan-move-gesture event)
      :panstop (handle-pan-stop-gesture event)
      :swipe (handle-swipe-gesture event)
      (logging/log-warn "Unknown gesture type" {:type gesture-type})))
  nil)

;; Helper functions

(>defn- get-speed-config-for-zoom
  "Get rotation speed configuration based on zoom level and stream type"
  [stream-type zoom-level]
  [string? int? => ::specs/speed-config]
  ;; Load from configuration matching web frontend zoom_table_rotary_touch.json
  (let [config (config/get-gesture-config)
        zoom-configs (get-in config [:zoom-speed-config 
                                    (if (= stream-type "heat") :heat :day)])
        zoom-config (or (first (filter #(= (:index %) zoom-level) zoom-configs))
                       ;; Default fallback
                       {:max-rotation-speed 1.0
                        :min-rotation-speed 0.0001
                        :ndc-threshold 0.5
                        :dead-zone-radius 0.05
                        :curve-steepness 4.0})]
    (select-keys zoom-config [:max-rotation-speed :min-rotation-speed 
                             :ndc-threshold :dead-zone-radius :curve-steepness])))

(>defn- calculate-rotation-speeds
  "Calculate rotation speeds from NDC deltas"
  [ndc-delta-x ndc-delta-y config]
  [number? number? ::specs/speed-config => [:tuple number? number?]]
  (let [magnitude (Math/sqrt (+ (* ndc-delta-x ndc-delta-x) 
                                (* ndc-delta-y ndc-delta-y)))
        dead-zone (:dead-zone-radius config)]
    (if (<= magnitude dead-zone)
      [0.0 0.0]
      (let [adjusted-magnitude (max 0 (- magnitude dead-zone))
            max-magnitude (- (:ndc-threshold config) dead-zone)
            normalized-magnitude (min 1.0 (/ adjusted-magnitude max-magnitude))
            curved-magnitude (Math/pow normalized-magnitude (:curve-steepness config))
            speed (+ (:min-rotation-speed config)
                    (* (- (:max-rotation-speed config) (:min-rotation-speed config))
                       curved-magnitude))]
        [(* (Math/abs ndc-delta-x) speed)
         (* (Math/abs ndc-delta-y) speed)]))))
```

### 2.2 Update Event Stream Handler

**Modify: `src/potatoclient/events/stream.clj`**

```clojure
(ns potatoclient.events.stream
  (:require [potatoclient.gestures.handler :as gestures]
            ;; ... other requires
            ))

(>defn handle-stream-event
  [subprocess-key event]
  [::specs/subprocess-key ::specs/stream-event => nil?]
  (let [event-type (:type event)]
    (case event-type
      "gesture" (gestures/handle-gesture-event 
                  (assoc event :stream-type (name subprocess-key)))
      ;; ... existing event handlers
      )))
```

### 2.3 Add Gesture Configuration

**New File: `resources/config/gestures.edn`**

```clojure
{:gesture-config
 {:move-threshold 20              ; pixels before pan starts
  :tap-long-press-threshold 300   ; ms for long press detection
  :double-tap-threshold 300       ; ms between taps for double-tap
  :swipe-threshold 100           ; pixels for swipe recognition
  :pan-update-interval 120       ; ms between pan command updates
  :double-tap-tolerance 10}      ; pixel tolerance for double-tap position
  
 ;; Zoom-based speed configurations matching web frontend
 :zoom-speed-config
 {:heat [{:index 0
          :max-rotation-speed 0.1
          :min-rotation-speed 0.0001
          :ndc-threshold 0.5
          :dead-zone-radius 0.05
          :curve-steepness 4.0}
         {:index 1
          :max-rotation-speed 0.25
          :min-rotation-speed 0.0001
          :ndc-threshold 0.5
          :dead-zone-radius 0.05
          :curve-steepness 4.0}
         {:index 2
          :max-rotation-speed 0.5
          :min-rotation-speed 0.0001
          :ndc-threshold 0.5
          :dead-zone-radius 0.05
          :curve-steepness 4.0}
         {:index 3
          :max-rotation-speed 1.0
          :min-rotation-speed 0.0001
          :ndc-threshold 0.5
          :dead-zone-radius 0.05
          :curve-steepness 4.0}
         {:index 4
          :max-rotation-speed 1.0
          :min-rotation-speed 0.0001
          :ndc-threshold 0.5
          :dead-zone-radius 0.05
          :curve-steepness 4.0}]
  :day [{:index 0
         :max-rotation-speed 0.05
         :min-rotation-speed 0.0001
         :ndc-threshold 0.5
         :dead-zone-radius 0.05
         :curve-steepness 4.0}
        {:index 1
         :max-rotation-speed 0.15
         :min-rotation-speed 0.0001
         :ndc-threshold 0.5
         :dead-zone-radius 0.05
         :curve-steepness 4.0}
        {:index 2
         :max-rotation-speed 0.5
         :min-rotation-speed 0.0001
         :ndc-threshold 0.5
         :dead-zone-radius 0.05
         :curve-steepness 4.0}
        {:index 3
         :max-rotation-speed 1.0
         :min-rotation-speed 0.0001
         :ndc-threshold 0.5
         :dead-zone-radius 0.05
         :curve-steepness 4.0}
        {:index 4
         :max-rotation-speed 1.0
         :min-rotation-speed 0.0001
         :ndc-threshold 0.5
         :dead-zone-radius 0.05
         :curve-steepness 4.0}]}}
```

### 2.4 Add Gesture Configuration Loader

**New File: `src/potatoclient/gestures/config.clj`**

```clojure
(ns potatoclient.gestures.config
  (:require [com.fulcrologic.guardrails.malli.core :refer [>defn =>]]
            [clojure.edn :as edn]
            [clojure.java.io :as io]
            [potatoclient.specs :as specs]
            [potatoclient.logging :as logging]))

(def ^:private gesture-config-atom (atom nil))

(>defn load-gesture-config!
  "Load gesture configuration from resources"
  []
  [=> nil?]
  (try
    (if-let [resource (io/resource "config/gestures.edn")]
      (let [config (edn/read-string (slurp resource))]
        (reset! gesture-config-atom config)
        (logging/log-info "Loaded gesture configuration"))
      (logging/log-warn "Gesture config not found, using defaults"))
    (catch Exception e
      (logging/log-error "Failed to load gesture config" {:error e})))
  nil)

(>defn get-gesture-config
  "Get current gesture configuration"
  []
  [=> (? map?)]
  (or @gesture-config-atom
      ;; Default configuration
      {:gesture-config
       {:move-threshold 20
        :tap-long-press-threshold 300
        :double-tap-threshold 300
        :swipe-threshold 100
        :pan-update-interval 120
        :double-tap-tolerance 10}
       :zoom-speed-config {}}))
```

## Phase 3: Add Missing Transit Commands

### 3.1 Update Transit Commands

**Modify: `src/potatoclient/transit/commands.clj`**

Add new commands if missing:

```clojure
(>defn rotary-goto-ndc
  "Command to rotate camera to NDC position"
  [channel ndc-x ndc-y]
  [::specs/channel-type number? number? => ::specs/command]
  {:action "rotary-goto-ndc"
   :params {:channel (name channel)
            :x ndc-x
            :y ndc-y}})

(>defn cv-start-track-ndc
  "Start CV tracking at NDC position"
  [channel ndc-x ndc-y frame-timestamp]
  [::specs/channel-type number? number? (? int?) => ::specs/command]
  {:action "cv-start-track-ndc"
   :params {:channel (name channel)
            :x ndc-x
            :y ndc-y
            :frame-timestamp frame-timestamp}})

(>defn rotary-set-velocity
  "Set rotary platform velocity with directions"
  [azimuth-speed elevation-speed azimuth-direction elevation-direction]
  [number? number? ::specs/rotary-direction ::specs/rotary-direction => ::specs/command]
  {:action "rotary-set-velocity"
   :params {:azimuth-speed azimuth-speed
            :elevation-speed elevation-speed
            :azimuth-direction (name azimuth-direction)
            :elevation-direction (name elevation-direction)}})

(>defn rotary-halt
  "Stop all rotary movement"
  []
  [=> ::specs/command]
  {:action "rotary-halt"
   :params {}})
```

## Phase 4: Update Kotlin Command Processing

### 4.1 Handle New Commands in CommandSubprocess

**Modify: `kotlin-subprocesses/src/main/kotlin/potatoclient/CommandSubprocess.kt`**

Add handlers for new gesture-based commands. Note that these need to create proper protobuf messages following the structure from the web frontend:

```kotlin
// Based on web frontend command structure from NDC_GESTURES_IMPLEMENTATION.md
private fun sendRotaryGotoNDC(channel: JonGuiDataVideoChannel, x: Float, y: Float) {
    val rotateToNdc = JonSharedCmd.RotaryPlatform.RotateToNDC.newBuilder()
        .setChannel(channel)
        .setX(x)
        .setY(y)
        .build()
        
    val rotaryRoot = JonSharedCmd.RotaryPlatform.Root.newBuilder()
        .setRotateToNdc(rotateToNdc)
        .build()
        
    val root = JonSharedCmd.Root.newBuilder()
        .setProtocolVersion(1)
        .setRotary(rotaryRoot)
        .build()
        
    sendProtobufMessage(root)
}

private fun sendCVStartTrackNDC(channel: JonGuiDataVideoChannel, x: Float, y: Float, frameTimestamp: Long?) {
    val builder = JonSharedCmd.CV.StartTrackNDC.newBuilder()
        .setChannel(channel)
        .setX(x)
        .setY(y)
        
    // Add frame timestamp if available
    frameTimestamp?.let { builder.setFrameTime(it) }
    
    val cvRoot = JonSharedCmd.CV.Root.newBuilder()
        .setStartTrackNdc(builder.build())
        .build()
        
    val root = JonSharedCmd.Root.newBuilder()
        .setProtocolVersion(1)
        .setCv(cvRoot)
        .build()
        
    sendProtobufMessage(root)
}

private fun sendRotaryHalt() {
    val haltMsg = JonSharedCmd.RotaryPlatform.Halt.newBuilder().build()
    
    val rotaryRoot = JonSharedCmd.RotaryPlatform.Root.newBuilder()
        .setHalt(haltMsg)
        .build()
        
    val root = JonSharedCmd.Root.newBuilder()
        .setProtocolVersion(1)
        .setRotary(rotaryRoot)
        .build()
        
    sendProtobufMessage(root)
}

private fun sendRotarySetVelocity(azSpeed: Float, elSpeed: Float, azDir: JonGuiDataRotaryDirection, elDir: JonGuiDataRotaryDirection) {
    // Create both axis commands
    val azimuthRotate = JonSharedCmd.RotaryPlatform.RotateAzimuth.newBuilder()
        .setSpeed(azSpeed)
        .setDirection(azDir)
        .build()
        
    val elevationRotate = JonSharedCmd.RotaryPlatform.RotateElevation.newBuilder()
        .setSpeed(elSpeed)
        .setDirection(elDir)
        .build()
        
    val azimuthMsg = JonSharedCmd.RotaryPlatform.Azimuth.newBuilder()
        .setRotate(azimuthRotate)
        .build()
        
    val elevationMsg = JonSharedCmd.RotaryPlatform.Elevation.newBuilder()
        .setRotate(elevationRotate)
        .build()
        
    val axisMsg = JonSharedCmd.RotaryPlatform.Axis.newBuilder()
        .setAzimuth(azimuthMsg)
        .setElevation(elevationMsg)
        .build()
        
    val rotaryRoot = JonSharedCmd.RotaryPlatform.Root.newBuilder()
        .setAxis(axisMsg)
        .build()
        
    val root = JonSharedCmd.Root.newBuilder()
        .setProtocolVersion(1)
        .setRotary(rotaryRoot)
        .build()
        
    sendProtobufMessage(root)
}
```

Add handlers for new gesture-based commands:

```kotlin
private fun handleCommand(command: Map<String, Any>) {
    when (val action = command["action"] as? String) {
        "rotary-goto-ndc" -> {
            val params = command["params"] as? Map<*, *> ?: return
            val channel = parseChannel(params["channel"] as? String)
            val x = (params["x"] as? Number)?.toFloat() ?: return
            val y = (params["y"] as? Number)?.toFloat() ?: return
            
            sendRotaryGotoNDC(channel, x, y)
        }
        "cv-start-track-ndc" -> {
            val params = command["params"] as? Map<*, *> ?: return
            val channel = parseChannel(params["channel"] as? String)
            val x = (params["x"] as? Number)?.toFloat() ?: return
            val y = (params["y"] as? Number)?.toFloat() ?: return
            val frameTimestamp = params["frame-timestamp"] as? Long
            
            sendCVStartTrackNDC(channel, x, y, frameTimestamp)
        }
        "rotary-set-velocity" -> {
            val params = command["params"] as? Map<*, *> ?: return
            val azSpeed = (params["azimuth-speed"] as? Number)?.toFloat() ?: return
            val elSpeed = (params["elevation-speed"] as? Number)?.toFloat() ?: return
            val azDirStr = params["azimuth-direction"] as? String ?: "clockwise"
            val elDirStr = params["elevation-direction"] as? String ?: "clockwise"
            
            // Convert string directions to protobuf enums
            val azDir = when (azDirStr) {
                "counter-clockwise" -> JonGuiDataRotaryDirection.JON_GUI_DATA_ROTARY_DIRECTION_COUNTER_CLOCKWISE
                else -> JonGuiDataRotaryDirection.JON_GUI_DATA_ROTARY_DIRECTION_CLOCKWISE
            }
            val elDir = when (elDirStr) {
                "counter-clockwise" -> JonGuiDataRotaryDirection.JON_GUI_DATA_ROTARY_DIRECTION_COUNTER_CLOCKWISE
                else -> JonGuiDataRotaryDirection.JON_GUI_DATA_ROTARY_DIRECTION_CLOCKWISE
            }
            
            sendRotarySetVelocity(azSpeed, elSpeed, azDir, elDir)
        }
        "rotary-halt" -> {
            sendRotaryHalt()
        }
        // ... existing command handlers
    }
}
```

## Phase 5: Testing and Integration

### 5.1 Add Gesture Tests

**New File: `test/potatoclient/gestures/handler_test.clj`**

```clojure
(ns potatoclient.gestures.handler-test
  (:require [clojure.test :refer [deftest testing is]]
            [potatoclient.gestures.handler :as handler]
            [potatoclient.test-utils :as test-utils]))

(deftest test-calculate-rotation-speeds
  (testing "Dead zone filtering"
    (let [config {:dead-zone-radius 0.05
                  :ndc-threshold 0.5
                  :min-rotation-speed 0.0001
                  :max-rotation-speed 1.0
                  :curve-steepness 4.0}]
      (is (= [0.0 0.0] 
             (#'handler/calculate-rotation-speeds 0.01 0.01 config))))))

(deftest test-gesture-handling
  (testing "Tap gesture sends rotary-goto-ndc command"
    (test-utils/with-command-capture
      (handler/handle-tap-gesture
        {:ndc-x 0.5 :ndc-y -0.3 :stream-type "heat"})
      (is (= 1 (count @test-utils/*captured-commands*)))
      (let [cmd (first @test-utils/*captured-commands*)]
        (is (= "rotary-goto-ndc" (:action cmd)))
        (is (= 0.5 (get-in cmd [:params :x])))
        (is (= -0.3 (get-in cmd [:params :y])))))))
```

### 5.2 Manual Testing Plan

1. **Basic Gesture Recognition**
   - Single tap on video → Camera rotates to position
   - Double tap → CV tracking starts at position
   - Click and drag → Camera pans smoothly
   - Quick swipe → Gesture recognized (logged)

2. **Pan Gesture Performance**
   - Smooth rotation during pan
   - Proper speed scaling based on gesture distance
   - Dead zone prevents jitter
   - Stop command sent on release

3. **Integration Testing**
   - Gestures work on both heat and day streams
   - Commands reach server correctly
   - Frame timestamps included where needed
   - Proper NDC coordinate conversion

## Implementation Timeline

1. **Week 1**: Implement Kotlin gesture recognition
   - GestureRecognizer class
   - Integration with MouseEventHandler
   - Transit message generation

2. **Week 2**: Implement Clojure gesture handling
   - Gesture handler functions
   - Command mapping
   - Configuration loading

3. **Week 3**: Testing and refinement
   - Unit tests
   - Integration testing
   - Performance optimization
   - Configuration tuning

## Benefits of This Implementation

1. **Consistent with Web Frontend**: Same gesture behaviors across platforms
2. **Leverages Existing Infrastructure**: Uses Transit architecture and command system
3. **Modular Design**: Gesture recognition separate from event handling
4. **Configurable**: Thresholds and speeds can be tuned
5. **Extensible**: Easy to add new gesture types
6. **Performance**: Throttling and dead zones prevent overload

## Key Differences from Web Frontend

### 1. Single-Point vs Multi-Point
- Desktop uses mouse events (single point)
- No pinch zoom gestures (use mouse wheel instead)
- Simplified gesture state machine

### 2. Coordinate System
- Both use NDC (-1 to 1) for consistency
- Desktop needs aspect ratio adjustment for pan gestures
- Y-axis inversion handled in pixelToNDC conversion

### 3. Command Transport
- Desktop: Transit → Kotlin → Protobuf → WebSocket
- Web: TypeScript → Protobuf → WebSocket/WebTransport
- Same protobuf message structure on the wire

### 4. Configuration
- Desktop: EDN configuration files
- Web: JSON configuration files
- Same timing values and speed curves

## Critical Implementation Notes

### 1. Frame Synchronization
For CV tracking, always include frame timestamp:
- Get from `frameDataProvider.getFrameData()` in Kotlin
- Convert to Long for protobuf compatibility
- Critical for accurate tracking initialization

### 2. Pan Gesture Throttling
- Send commands every 120ms (matching web frontend)
- Use ScheduledExecutorService for consistent timing
- Cancel pending commands on gesture stop

### 3. Dead Zone Implementation
- Apply dead zone before calculating speeds
- Prevents jitter from small movements
- Configurable per zoom level

### 4. Aspect Ratio Adjustment
```kotlin
// Critical for consistent pan behavior across resolutions
val adjustedNdcDeltaX = ndcDeltaX * aspectRatio
```

### 5. Direction Calculation
- Azimuth: positive X = clockwise, negative = counter-clockwise
- Elevation: positive Y = up/clockwise, negative = down/counter-clockwise
- Must match server expectations

## Troubleshooting Guide

### Common Issues and Solutions

1. **Pan gestures not smooth**
   - Check UPDATE_INTERVAL is 120ms
   - Verify dead zone configuration
   - Ensure aspect ratio adjustment is applied
   - Check zoom level is being updated

2. **Double-tap not recognized**
   - Verify doubleTapThreshold (300ms)
   - Check doubleTapTolerance (10 pixels)
   - Ensure tap timestamps are being tracked

3. **Commands not reaching server**
   - Verify Transit message flow
   - Check CommandSubprocess is handling new actions
   - Ensure protobuf messages have correct structure
   - Verify WebSocket connection is active

4. **Wrong rotation direction**
   - Check NDC delta sign mapping
   - Verify direction enum conversion
   - Ensure Y-axis inversion in pixelToNDC

5. **Gesture conflicts**
   - Check moveThreshold prevents false pans
   - Verify gesture state transitions
   - Ensure proper cleanup on gesture end

### Debug Logging

Add debug logging at key points:

```kotlin
// In GestureRecognizer
logDebug("Gesture state: $currentState -> $newState")
logDebug("Pan delta: NDC($ndcDeltaX, $ndcDeltaY) px($deltaX, $deltaY)")

// In PanController  
logDebug("Speed config: zoom=$zoomLevel, max=${config.maxRotationSpeed}")
logDebug("Rotation: az=$azSpeed/$azDir, el=$elSpeed/$elDir")
```

```clojure
;; In gesture handler
(logging/log-debug "Gesture received" {:type gesture-type :ndc-x ndc-x :ndc-y ndc-y})
(logging/log-debug "Command sent" {:action (:action cmd) :params (:params cmd)})
```

## Future Enhancements

1. **Gesture Visualization**: Show gesture feedback in UI
2. **Custom Gestures**: User-definable gesture patterns
3. **Gesture Recording**: Record and replay gesture sequences
4. **Touch Support**: Add multi-touch gestures when needed
5. **Gesture Profiles**: Different configurations for different use cases
6. **Mouse Wheel Zoom**: Add zoom gestures via mouse wheel
7. **Gesture Macros**: Combine gestures into complex actions
8. **Haptic Feedback**: Vibration feedback for gesture recognition