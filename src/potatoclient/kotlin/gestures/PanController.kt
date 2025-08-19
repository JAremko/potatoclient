package potatoclient.kotlin.gestures

import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt

enum class StreamType {
    HEAT,
    DAY,
    ;

    fun toKeyword(): String = name.lowercase().replace("_", "-")
}

enum class RotaryDirection {
    CLOCKWISE,
    COUNTER_CLOCKWISE;
    
    fun toKeyword(): String = when (this) {
        CLOCKWISE -> "clockwise"
        COUNTER_CLOCKWISE -> "counter-clockwise"
    }
}

data class PanState(
    val azimuthSpeed: Double = 0.0,
    val elevationSpeed: Double = 0.0,
    val isInDeadZone: Boolean = true,
)

data class SpeedConfig(
    val maxRotationSpeed: Double = 1.0,
    val minRotationSpeed: Double = 0.0001,
    val ndcThreshold: Double = 0.5,
    val deadZoneRadius: Double = 0.05,
    val curveSteepness: Double = 4.0,
)

class PanController(
    private val onRotaryCommand: (
        azSpeed: Double,
        elSpeed: Double,
        azDir: RotaryDirection,
        elDir: RotaryDirection,
    ) -> Unit,
    private val onHaltCommand: () -> Unit,
    private val streamType: StreamType,
) {
    companion object {
        const val UPDATE_INTERVAL = 120L // ms, matching web frontend

        // Load configuration from resources
        fun loadSpeedConfigs(): Map<StreamType, List<SpeedConfig>> {
            // In production, load from resources/config/gestures.edn
            // For now, return defaults matching zoom_table_rotary_touch.json
            return mapOf(
                StreamType.HEAT to
                    listOf(
                        SpeedConfig(0.1, 0.0001, 0.5, 0.05, 4.0), // zoom 0
                        SpeedConfig(0.25, 0.0001, 0.5, 0.05, 4.0), // zoom 1
                        SpeedConfig(0.5, 0.0001, 0.5, 0.05, 4.0), // zoom 2
                        SpeedConfig(1.0, 0.0001, 0.5, 0.05, 4.0), // zoom 3+
                    ),
                StreamType.DAY to
                    listOf(
                        SpeedConfig(0.05, 0.0001, 0.5, 0.05, 4.0), // zoom 0
                        SpeedConfig(0.15, 0.0001, 0.5, 0.05, 4.0), // zoom 1
                        SpeedConfig(0.5, 0.0001, 0.5, 0.05, 4.0), // zoom 2
                        SpeedConfig(1.0, 0.0001, 0.5, 0.05, 4.0), // zoom 3+
                    ),
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

    fun updatePan(
        ndcDeltaX: Double,
        ndcDeltaY: Double,
        zoomLevel: Int,
    ) {
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

    private fun calculateRotationSpeeds(
        ndcDeltaX: Double,
        ndcDeltaY: Double,
    ): Pair<Double, Double> {
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
        val speed =
            speedConfig.minRotationSpeed +
                (speedConfig.maxRotationSpeed - speedConfig.minRotationSpeed) * curvedMagnitude

        return Pair(
            abs(normalizedDeltaX * speed).coerceAtLeast(speedConfig.minRotationSpeed),
            abs(normalizedDeltaY * speed).coerceAtLeast(speedConfig.minRotationSpeed),
        )
    }

    private fun startPeriodicUpdate() {
        updateTask?.cancel(false)
        updateTask =
            executor.scheduleAtFixedRate({
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
            val azDir =
                if (lastNdcDeltaX >= 0) {
                    RotaryDirection.CLOCKWISE
                } else {
                    RotaryDirection.COUNTER_CLOCKWISE
                }

            val elDir =
                if (lastNdcDeltaY >= 0) {
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
