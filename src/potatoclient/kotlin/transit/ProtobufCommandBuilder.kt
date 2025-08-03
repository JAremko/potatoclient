package potatoclient.kotlin.transit

import cmd.JonSharedCmd
import com.cognitect.transit.TransitFactory
import potatoclient.transit.ActionRegistry
import potatoclient.kotlin.transit.builders.*

/**
 * Command builder that uses Action Registry for validation and delegates
 * to category-specific builders for protobuf construction.
 * 
 * This replaces the old SimpleCommandBuilder with a more maintainable,
 * registry-driven approach.
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
        val action = msgData[TransitFactory.keyword("action")] as? String
            ?: return Result.failure(IllegalArgumentException("Missing action in command message"))
        val params = msgData[TransitFactory.keyword("params")] as? Map<*, *>
        
        return buildCommand(action, params)
    }
    
    /**
     * Build a command from action and params with full validation
     */
    fun buildCommand(action: String, params: Map<*, *>?): Result<JonSharedCmd.Root> {
        // Validate action exists
        if (!ActionRegistry.isKnownAction(action)) {
            return Result.failure(UnknownCommandException(action))
        }
        
        // Validate required parameters
        val paramsMap = params ?: emptyMap<Any, Any>()
        if (!ActionRegistry.hasRequiredParams(action, paramsMap)) {
            val actionDef = ActionRegistry.getAction(action)
            val missingParams = getMissingParameters(actionDef, paramsMap)
            return Result.failure(MissingParametersException(action, missingParams))
        }
        
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
                
                // For now, delegate all others to SimpleCommandBuilder
                else -> SimpleCommandBuilder.getInstance().buildCommand(msgData)
            }
        } catch (e: Exception) {
            Result.failure(CommandBuildException(action, e))
        }
    }
    
    // Basic command builders (these don't need separate classes)
    private fun buildPing(): Result<JonSharedCmd.Root> {
        val pingCmd = JonSharedCmd.Ping.newBuilder().build()
        return Result.success(
            JonSharedCmd.Root.newBuilder()
                .setPing(pingCmd)
                .build()
        )
    }
    
    private fun buildNoop(): Result<JonSharedCmd.Root> {
        val noopCmd = JonSharedCmd.Noop.newBuilder().build()
        return Result.success(
            JonSharedCmd.Root.newBuilder()
                .setNoop(noopCmd)
                .build()
        )
    }
    
    private fun buildFrozen(): Result<JonSharedCmd.Root> {
        val frozenCmd = JonSharedCmd.Frozen.newBuilder().build()
        return Result.success(
            JonSharedCmd.Root.newBuilder()
                .setFrozen(frozenCmd)
                .build()
        )
    }
    
    /**
     * Get missing parameters by comparing required params with provided params
     */
    private fun getMissingParameters(
        actionDef: potatoclient.transit.ActionDefinition?,
        params: Map<*, *>
    ): Set<String> {
        if (actionDef == null) return emptySet()
        
        val missing = mutableSetOf<String>()
        for (required in actionDef.requiredParams) {
            val paramName = required.name
            // Check both string key and keyword key
            if (!params.containsKey(paramName) && 
                !params.containsKey(TransitFactory.keyword(paramName))) {
                missing.add(paramName)
            }
        }
        return missing
    }
}

/**
 * Exception thrown when an unknown command is requested
 */
class UnknownCommandException(val action: String) : 
    IllegalArgumentException("Unknown command action: $action")

/**
 * Exception thrown when required parameters are missing
 */
class MissingParametersException(val action: String, val missingParams: Set<String>) :
    IllegalArgumentException(
        "Command '$action' missing required parameters: ${missingParams.joinToString(", ")}"
    )

/**
 * Exception thrown when command building fails
 */
class CommandBuildException(val action: String, cause: Throwable) :
    RuntimeException("Failed to build command '$action': ${cause.message}", cause)