package potatoclient.kotlin.transit

import cmd.DayCamGlassHeater.JonSharedCmdDayCamGlassHeater
import cmd.JonSharedCmd
import com.cognitect.transit.TransitFactory

/**
 * Builder for Glass Heater commands
 */
object GlassHeaterCommandBuilder {
    
    fun build(action: String, params: Map<*, *>): Result<JonSharedCmd.Root> {
        val heaterMsg = when (action) {
            // Basic control
            "glass-heater-start" -> buildStart()
            "glass-heater-stop" -> buildStop()
            "glass-heater-turn-on" -> buildTurnOn()
            "glass-heater-turn-off" -> buildTurnOff()
            
            // Meteorological data
            "glass-heater-get-meteo" -> buildGetMeteo()
            
            else -> return Result.failure(
                IllegalArgumentException("Unknown glass heater command: $action")
            )
        }
        
        return heaterMsg.map { heater ->
            JonSharedCmd.Root.newBuilder()
                .setProtocolVersion(1)
                .setDayCamGlassHeater(heater)
                .build()
        }
    }
    
    // Basic control
    private fun buildStart(): Result<JonSharedCmdDayCamGlassHeater.Root> = Result.success(
        JonSharedCmdDayCamGlassHeater.Root.newBuilder()
            .setStart(JonSharedCmdDayCamGlassHeater.Start.newBuilder().build())
            .build()
    )
    
    private fun buildStop(): Result<JonSharedCmdDayCamGlassHeater.Root> = Result.success(
        JonSharedCmdDayCamGlassHeater.Root.newBuilder()
            .setStop(JonSharedCmdDayCamGlassHeater.Stop.newBuilder().build())
            .build()
    )
    
    private fun buildTurnOn(): Result<JonSharedCmdDayCamGlassHeater.Root> = Result.success(
        JonSharedCmdDayCamGlassHeater.Root.newBuilder()
            .setTurnOn(JonSharedCmdDayCamGlassHeater.TurnOn.newBuilder().build())
            .build()
    )
    
    private fun buildTurnOff(): Result<JonSharedCmdDayCamGlassHeater.Root> = Result.success(
        JonSharedCmdDayCamGlassHeater.Root.newBuilder()
            .setTurnOff(JonSharedCmdDayCamGlassHeater.TurnOff.newBuilder().build())
            .build()
    )
    
    // Meteorological data
    private fun buildGetMeteo(): Result<JonSharedCmdDayCamGlassHeater.Root> = Result.success(
        JonSharedCmdDayCamGlassHeater.Root.newBuilder()
            .setGetMeteo(JonSharedCmdDayCamGlassHeater.GetMeteo.newBuilder().build())
            .build()
    )
}