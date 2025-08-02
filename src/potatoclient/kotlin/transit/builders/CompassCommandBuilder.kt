package potatoclient.kotlin.transit

import cmd.Compass.JonSharedCmdCompass
import cmd.JonSharedCmd
import com.cognitect.transit.TransitFactory

/**
 * Builder for compass commands
 */
object CompassCommandBuilder {
    
    fun build(action: String, params: Map<*, *>): Result<JonSharedCmd.Root> {
        val compassMsg = when (action) {
            // Basic control
            "compass-start" -> buildStart()
            "compass-stop" -> buildStop()
            
            // Configuration
            "compass-set-magnetic-declination" -> buildSetMagneticDeclination(params)
            "compass-set-offset-angle-azimuth" -> buildSetOffsetAngleAzimuth(params)
            "compass-set-offset-angle-elevation" -> buildSetOffsetAngleElevation(params)
            "compass-set-use-rotary-position" -> buildSetUseRotaryPosition(params)
            
            // Calibration
            "compass-calibrate-start-long" -> buildCalibrateStartLong()
            "compass-calibrate-start-short" -> buildCalibrateStartShort()
            "compass-calibrate-next" -> buildCalibrateNext()
            "compass-calibrate-cencel" -> buildCalibrateCencel()
            
            // Meteorological data
            "compass-get-meteo" -> buildGetMeteo()
            
            else -> return Result.failure(
                IllegalArgumentException("Unknown compass command: $action")
            )
        }
        
        return compassMsg.map { compass ->
            JonSharedCmd.Root.newBuilder()
                .setProtocolVersion(1)
                .setCompass(compass)
                .build()
        }
    }
    
    private fun buildStart(): Result<JonSharedCmdCompass.Root> = Result.success(
        JonSharedCmdCompass.Root.newBuilder()
            .setStart(JonSharedCmdCompass.Start.newBuilder().build())
            .build()
    )
    
    private fun buildStop(): Result<JonSharedCmdCompass.Root> = Result.success(
        JonSharedCmdCompass.Root.newBuilder()
            .setStop(JonSharedCmdCompass.Stop.newBuilder().build())
            .build()
    )
    
    private fun buildSetMagneticDeclination(params: Map<*, *>): Result<JonSharedCmdCompass.Root> {
        val value = getFloatParam(params, "value")
            ?: return Result.failure(IllegalArgumentException("Missing value parameter"))
        
        val setDeclination = JonSharedCmdCompass.SetMagneticDeclination.newBuilder()
            .setValue(value)
            .build()
        
        return Result.success(
            JonSharedCmdCompass.Root.newBuilder()
                .setSetMagneticDeclination(setDeclination)
                .build()
        )
    }
    
    private fun buildSetOffsetAngleAzimuth(params: Map<*, *>): Result<JonSharedCmdCompass.Root> {
        val value = getFloatParam(params, "value")
            ?: return Result.failure(IllegalArgumentException("Missing value parameter"))
        
        val setOffset = JonSharedCmdCompass.SetOffsetAngleAzimuth.newBuilder()
            .setValue(value)
            .build()
        
        return Result.success(
            JonSharedCmdCompass.Root.newBuilder()
                .setSetOffsetAngleAzimuth(setOffset)
                .build()
        )
    }
    
    private fun buildSetOffsetAngleElevation(params: Map<*, *>): Result<JonSharedCmdCompass.Root> {
        val value = getFloatParam(params, "value")
            ?: return Result.failure(IllegalArgumentException("Missing value parameter"))
        
        val setOffset = JonSharedCmdCompass.SetOffsetAngleElevation.newBuilder()
            .setValue(value)
            .build()
        
        return Result.success(
            JonSharedCmdCompass.Root.newBuilder()
                .setSetOffsetAngleElevation(setOffset)
                .build()
        )
    }
    
    private fun buildSetUseRotaryPosition(params: Map<*, *>): Result<JonSharedCmdCompass.Root> {
        val flag = getBooleanParam(params, "flag")
            ?: return Result.failure(IllegalArgumentException("Missing flag parameter"))
        
        val setUseRotary = JonSharedCmdCompass.SetUseRotaryPosition.newBuilder()
            .setFlag(flag)
            .build()
        
        return Result.success(
            JonSharedCmdCompass.Root.newBuilder()
                .setSetUseRotaryPosition(setUseRotary)
                .build()
        )
    }
    
    private fun buildCalibrateStartLong(): Result<JonSharedCmdCompass.Root> = Result.success(
        JonSharedCmdCompass.Root.newBuilder()
            .setStartCalibrateLong(JonSharedCmdCompass.CalibrateStartLong.newBuilder().build())
            .build()
    )
    
    private fun buildCalibrateStartShort(): Result<JonSharedCmdCompass.Root> = Result.success(
        JonSharedCmdCompass.Root.newBuilder()
            .setStartCalibrateShort(JonSharedCmdCompass.CalibrateStartShort.newBuilder().build())
            .build()
    )
    
    private fun buildCalibrateNext(): Result<JonSharedCmdCompass.Root> = Result.success(
        JonSharedCmdCompass.Root.newBuilder()
            .setCalibrateNext(JonSharedCmdCompass.CalibrateNext.newBuilder().build())
            .build()
    )
    
    private fun buildCalibrateCencel(): Result<JonSharedCmdCompass.Root> = Result.success(
        JonSharedCmdCompass.Root.newBuilder()
            .setCalibrateCencel(JonSharedCmdCompass.CalibrateCencel.newBuilder().build())
            .build()
    )
    
    private fun buildGetMeteo(): Result<JonSharedCmdCompass.Root> = Result.success(
        JonSharedCmdCompass.Root.newBuilder()
            .setGetMeteo(JonSharedCmdCompass.GetMeteo.newBuilder().build())
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