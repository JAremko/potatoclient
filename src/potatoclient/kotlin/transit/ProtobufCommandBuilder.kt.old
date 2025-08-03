package potatoclient.kotlin.transit

import cmd.JonSharedCmd
import com.cognitect.transit.TransitFactory
import potatoclient.kotlin.transit.builders.*

/**
 * Command builder that delegates to category-specific builders for protobuf construction.
 */
class ProtobufCommandBuilder {
    companion object {
        private val INSTANCE = ProtobufCommandBuilder()

        /**
         * Get the singleton instance
         */
        @JvmStatic
        fun getInstance(): ProtobufCommandBuilder = INSTANCE
    }

    /**
     * Build a command from Transit message data
     */
    fun buildCommand(msgData: Map<*, *>): Result<JonSharedCmd.Root> {
        val action =
            msgData[TransitFactory.keyword("action")] as? String
                ?: return Result.failure(IllegalArgumentException("Missing action in command message"))
        val params = msgData[TransitFactory.keyword("params")] as? Map<*, *>

        return buildCommand(action, params)
    }

    /**
     * Build a command from action and params
     */
    fun buildCommand(
        action: String,
        params: Map<*, *>?,
    ): Result<JonSharedCmd.Root> {
        val paramsMap = params ?: emptyMap<Any, Any>()

        // Delegate to appropriate builder based on command category
        return try {
            when {
                // Basic commands
                action == "ping" -> buildPing()
                action == "noop" -> buildNoop()
                action == "frozen" -> buildFrozen()

                // Rotary commands
                action.startsWith("rotary-") -> RotaryPlatformCommandBuilder.build(action, paramsMap)

                // CV commands
                action.startsWith("cv-") -> CVCommandBuilder.build(action, paramsMap)

                // System commands
                action.startsWith("system-") -> SystemCommandBuilder.build(action, paramsMap)

                // GPS commands
                action.startsWith("gps-") -> GpsCommandBuilder.build(action, paramsMap)

                // Compass commands
                action.startsWith("compass-") -> CompassCommandBuilder.build(action, paramsMap)

                // LRF commands
                action.startsWith("lrf-") -> LrfCommandBuilder.build(action, paramsMap)

                // Day camera commands
                action.startsWith("day-camera-") -> DayCameraCommandBuilder.build(action, paramsMap)

                // Heat camera commands
                action.startsWith("heat-camera-") -> HeatCameraCommandBuilder.build(action, paramsMap)

                // OSD commands
                action.startsWith("osd-") -> OSDCommandBuilder.build(action, paramsMap)

                // Glass heater commands
                action.startsWith("glass-heater-") -> DayCamGlassHeaterCommandBuilder.build(action, paramsMap)

                // LIRA commands
                action.startsWith("lira-") -> LiraCommandBuilder.build(action, paramsMap)

                else ->
                    Result.failure(
                        IllegalArgumentException("No builder available for command: $action"),
                    )
            }
        } catch (e: Exception) {
            Result.failure(CommandBuildException(action, e))
        }
    }

    // Basic command builders (these don't need separate classes)
    private fun buildPing(): Result<JonSharedCmd.Root> {
        val pingCmd = JonSharedCmd.Ping.newBuilder().build()
        return Result.success(
            JonSharedCmd.Root
                .newBuilder()
                .setPing(pingCmd)
                .build(),
        )
    }

    private fun buildNoop(): Result<JonSharedCmd.Root> {
        val noopCmd = JonSharedCmd.Noop.newBuilder().build()
        return Result.success(
            JonSharedCmd.Root
                .newBuilder()
                .setNoop(noopCmd)
                .build(),
        )
    }

    private fun buildFrozen(): Result<JonSharedCmd.Root> {
        val frozenCmd = JonSharedCmd.Frozen.newBuilder().build()
        return Result.success(
            JonSharedCmd.Root
                .newBuilder()
                .setFrozen(frozenCmd)
                .build(),
        )
    }
}

/**
 * Exception thrown when command building fails
 */
class CommandBuildException(
    val action: String,
    cause: Throwable,
) : RuntimeException("Failed to build command '$action': ${cause.message}", cause)
