package potatoclient.kotlin.transit.builders

import cmd.JonSharedCmd
import cmd.DayCamGlassHeater.JonSharedCmdDayCamGlassHeater
import com.cognitect.transit.TransitFactory

/**
 * Builder for DayCamGlassHeater commands
 * Generated from protobuf specs
 */
object DayCamGlassHeaterCommandBuilder {
    
    fun build(action: String, params: Map<*, *>): Result<JonSharedCmd.Root> {
        val daycamglassheaterMsg = when (action) {
            "daycamglassheater-start" -> buildStart()
            "daycamglassheater-stop" -> buildStop()
            "daycamglassheater-turn-on" -> buildTurnOn()
            "daycamglassheater-turn-off" -> buildTurnOff()
            "daycamglassheater-get-meteo" -> buildGetMeteo()
            
            else -> return Result.failure(
                IllegalArgumentException("Unknown DayCamGlassHeater command: $action")
            )
        }
        
        return daycamglassheaterMsg.map { daycamglassheater ->
            JonSharedCmd.Root.newBuilder()
                .setProtocolVersion(1)
                .setDayCamGlassHeater(daycamglassheater)
                .build()
        }
    }
    
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

    private fun buildGetMeteo(): Result<JonSharedCmdDayCamGlassHeater.Root> = Result.success(
        JonSharedCmdDayCamGlassHeater.Root.newBuilder()
            .setGetMeteo(JonSharedCmdDayCamGlassHeater.GetMeteo.newBuilder().build())
            .build()
    )
}
