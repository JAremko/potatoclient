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
            "gps-start" -> buildStart(params)
            "gps-stop" -> buildStop(params)
            "gps-get-meteo" -> buildGetMeteo(params)
            "gps-set-use-manual-position" -> buildSetUseManualPosition(params)
            "gps-set-manual-position" -> buildSetManualPosition(params)
            
            else -> return Result.failure(
                IllegalArgumentException("Unknown Gps command: $action")
            )
        }
        
        return gpsMsg.map { gps ->
            JonSharedCmd.Root.newBuilder()
                .setProtocolVersion(1)
                .setSetgps(gps)
                .build()
        }
    }
    
    private fun buildStart(params: Map<*, *>): Result<JonSharedCmdGps.Root> = Result.success(
        JonSharedCmdGps.Root.newBuilder()
            .setStart(JonSharedCmdGps.Start.newBuilder().build())
            .build()
    )

    private fun buildStop(params: Map<*, *>): Result<JonSharedCmdGps.Root> = Result.success(
        JonSharedCmdGps.Root.newBuilder()
            .setStop(JonSharedCmdGps.Stop.newBuilder().build())
            .build()
    )

    private fun buildGetMeteo(params: Map<*, *>): Result<JonSharedCmdGps.Root> = Result.success(
        JonSharedCmdGps.Root.newBuilder()
            .setGetMeteo(JonSharedCmdGps.GetMeteo.newBuilder().build())
            .build()
    )

    private fun buildSetUseManualPosition(params: Map<*, *>): Result<JonSharedCmdGps.Root> = Result.success(
        JonSharedCmdGps.Root.newBuilder()
            .setSetUseManualPosition(JonSharedCmdGps.SetUseManualPosition.newBuilder().build())
            .build()
    )

    private fun buildSetManualPosition(params: Map<*, *>): Result<JonSharedCmdGps.Root> = Result.success(
        JonSharedCmdGps.Root.newBuilder()
            .setSetManualPosition(JonSharedCmdGps.SetManualPosition.newBuilder().build())
            .build()
    )
}
