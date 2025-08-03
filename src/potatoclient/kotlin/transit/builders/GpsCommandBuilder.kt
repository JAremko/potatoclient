package potatoclient.kotlin.transit.builders

import cmd.JonSharedCmd
import cmd.Gps.JonSharedCmdGps
import com.cognitect.transit.TransitFactory

/**
 * Builder for Gps commands
 * Generated from protobuf specs
 */
object GpsCommandBuilder {
    
    fun build(action: String, params: Map<*, *>): Result<JonSharedCmd.Root> {
        val gpsMsg = when (action) {
            "gps-start" -> buildStart()
            "gps-stop" -> buildStop()
            "gps-get-meteo" -> buildGetMeteo()
            "gps-set-use-manual-position" -> buildSetUseManualPosition()
            "gps-set-manual-position" -> buildSetManualPosition()
            
            else -> return Result.failure(
                IllegalArgumentException("Unknown Gps command: $action")
            )
        }
        
        return gpsMsg.map { gps ->
            JonSharedCmd.Root.newBuilder()
                .setProtocolVersion(1)
                .setGps(gps)
                .build()
        }
    }
    
    private fun buildStart(): Result<JonSharedCmdGps.Root> = Result.success(
        JonSharedCmdGps.Root.newBuilder()
            .setStart(JonSharedCmdGps.Start.newBuilder().build())
            .build()
    )

    private fun buildStop(): Result<JonSharedCmdGps.Root> = Result.success(
        JonSharedCmdGps.Root.newBuilder()
            .setStop(JonSharedCmdGps.Stop.newBuilder().build())
            .build()
    )

    private fun buildGetMeteo(): Result<JonSharedCmdGps.Root> = Result.success(
        JonSharedCmdGps.Root.newBuilder()
            .setGetMeteo(JonSharedCmdGps.GetMeteo.newBuilder().build())
            .build()
    )

    private fun buildSetUseManualPosition(): Result<JonSharedCmdGps.Root> = Result.success(
        JonSharedCmdGps.Root.newBuilder()
            .setSetUseManualPosition(JonSharedCmdGps.SetUseManualPosition.newBuilder().build())
            .build()
    )

    private fun buildSetManualPosition(): Result<JonSharedCmdGps.Root> = Result.success(
        JonSharedCmdGps.Root.newBuilder()
            .setSetManualPosition(JonSharedCmdGps.SetManualPosition.newBuilder().build())
            .build()
    )
}
