package potatoclient.kotlin.transit

import cmd.Gps.JonSharedCmdGps
import cmd.JonSharedCmd
import com.cognitect.transit.TransitFactory

/**
 * Builder for GPS commands
 */
object GPSCommandBuilder {
    
    fun build(action: String, params: Map<*, *>): Result<JonSharedCmd.Root> {
        val gpsMsg = when (action) {
            // Basic control
            "gps-start" -> buildStart()
            "gps-stop" -> buildStop()
            
            // Manual position
            "gps-set-manual-position" -> buildSetManualPosition(params)
            "gps-set-use-manual-position" -> buildSetUseManualPosition(params)
            
            // Meteorological data
            "gps-get-meteo" -> buildGetMeteo()
            
            else -> return Result.failure(
                IllegalArgumentException("Unknown GPS command: $action")
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
    
    private fun buildSetManualPosition(params: Map<*, *>): Result<JonSharedCmdGps.Root> {
        val latitude = getFloatParam(params, "latitude")
            ?: return Result.failure(IllegalArgumentException("Missing latitude parameter"))
        val longitude = getFloatParam(params, "longitude")
            ?: return Result.failure(IllegalArgumentException("Missing longitude parameter"))
        val altitude = getFloatParam(params, "altitude")
            ?: return Result.failure(IllegalArgumentException("Missing altitude parameter"))
        
        val setPosition = JonSharedCmdGps.SetManualPosition.newBuilder()
            .setLatitude(latitude)
            .setLongitude(longitude)
            .setAltitude(altitude)
            .build()
        
        return Result.success(
            JonSharedCmdGps.Root.newBuilder()
                .setSetManualPosition(setPosition)
                .build()
        )
    }
    
    private fun buildSetUseManualPosition(params: Map<*, *>): Result<JonSharedCmdGps.Root> {
        val flag = getBooleanParam(params, "flag")
            ?: return Result.failure(IllegalArgumentException("Missing flag parameter"))
        
        val setUseManual = JonSharedCmdGps.SetUseManualPosition.newBuilder()
            .setFlag(flag)
            .build()
        
        return Result.success(
            JonSharedCmdGps.Root.newBuilder()
                .setSetUseManualPosition(setUseManual)
                .build()
        )
    }
    
    private fun buildGetMeteo(): Result<JonSharedCmdGps.Root> = Result.success(
        JonSharedCmdGps.Root.newBuilder()
            .setGetMeteo(JonSharedCmdGps.GetMeteo.newBuilder().build())
            .build()
    )
    
    // Helper functions
    private fun getFloatParam(params: Map<*, *>, key: String): Float? {
        val value = params[key] ?: params[TransitFactory.keyword(key)]
        return (value as? Number)?.toFloat()
    }
    
    private fun getBooleanParam(params: Map<*, *>, key: String): Boolean? {
        val value = params[key] ?: params[TransitFactory.keyword(key)]
        return value as? Boolean
    }
}