package potatoclient.kotlin.transit.builders

import cmd.JonSharedCmd
import cmd.Compass.JonSharedCmdCompass
import com.cognitect.transit.TransitFactory

/**
 * Builder for Compass commands
 * Generated from protobuf specs
 */
object CompassCommandBuilder {
    
    fun build(action: String, params: Map<*, *>): Result<JonSharedCmd.Root> {
        val compassMsg = when (action) {
            "compass-calibrate-cencel" -> buildCalibrateCencel(params)
            "compass-start" -> buildStart(params)
            "compass-calibrate-start-short" -> buildCalibrateStartShort(params)
            "compass-set-offset-angle-elevation" -> buildSetOffsetAngleElevation(params)
            "compass-stop" -> buildStop(params)
            "compass-calibrate-start-long" -> buildCalibrateStartLong(params)
            "compass-next" -> buildNext(params)
            "compass-calibrate-next" -> buildCalibrateNext(params)
            "compass-get-meteo" -> buildGetMeteo(params)
            "compass-set-use-rotary-position" -> buildSetUseRotaryPosition(params)
            "compass-set-magnetic-declination" -> buildSetMagneticDeclination(params)
            "compass-set-offset-angle-azimuth" -> buildSetOffsetAngleAzimuth(params)
            
            else -> return Result.failure(
                IllegalArgumentException("Unknown Compass command: $action")
            )
        }
        
        return compassMsg.map { compass ->
            JonSharedCmd.Root.newBuilder()
                .setProtocolVersion(1)
                .setCompass(compass)
                .build()
        }
    }
    
    private fun buildCalibrateCencel(params: Map<*, *>): Result<JonSharedCmdCompass.Root> = Result.success(
        JonSharedCmdCompass.Root.newBuilder()
            .setCalibrateCencel(JonSharedCmdCompass.CalibrateCencel.newBuilder().build())
            .build()
    )

    private fun buildStart(params: Map<*, *>): Result<JonSharedCmdCompass.Root> = Result.success(
        JonSharedCmdCompass.Root.newBuilder()
            .setStart(JonSharedCmdCompass.Start.newBuilder().build())
            .build()
    )

    private fun buildCalibrateStartShort(params: Map<*, *>): Result<JonSharedCmdCompass.Root> = Result.success(
        JonSharedCmdCompass.Root.newBuilder()
            .setCalibrateStartShort(JonSharedCmdCompass.CalibrateStartShort.newBuilder().build())
            .build()
    )

    private fun buildSetOffsetAngleElevation(params: Map<*, *>): Result<JonSharedCmdCompass.Root> = Result.success(
        JonSharedCmdCompass.Root.newBuilder()
            .setSetOffsetAngleElevation(JonSharedCmdCompass.SetOffsetAngleElevation.newBuilder().build())
            .build()
    )

    private fun buildStop(params: Map<*, *>): Result<JonSharedCmdCompass.Root> = Result.success(
        JonSharedCmdCompass.Root.newBuilder()
            .setStop(JonSharedCmdCompass.Stop.newBuilder().build())
            .build()
    )

    private fun buildCalibrateStartLong(params: Map<*, *>): Result<JonSharedCmdCompass.Root> = Result.success(
        JonSharedCmdCompass.Root.newBuilder()
            .setCalibrateStartLong(JonSharedCmdCompass.CalibrateStartLong.newBuilder().build())
            .build()
    )

    private fun buildNext(params: Map<*, *>): Result<JonSharedCmdCompass.Root> = Result.success(
        JonSharedCmdCompass.Root.newBuilder()
            .setNext(JonSharedCmdCompass.Next.newBuilder().build())
            .build()
    )

    private fun buildCalibrateNext(params: Map<*, *>): Result<JonSharedCmdCompass.Root> = Result.success(
        JonSharedCmdCompass.Root.newBuilder()
            .setCalibrateNext(JonSharedCmdCompass.CalibrateNext.newBuilder().build())
            .build()
    )

    private fun buildGetMeteo(params: Map<*, *>): Result<JonSharedCmdCompass.Root> = Result.success(
        JonSharedCmdCompass.Root.newBuilder()
            .setGetMeteo(JonSharedCmdCompass.GetMeteo.newBuilder().build())
            .build()
    )

    private fun buildSetUseRotaryPosition(params: Map<*, *>): Result<JonSharedCmdCompass.Root> = Result.success(
        JonSharedCmdCompass.Root.newBuilder()
            .setSetUseRotaryPosition(JonSharedCmdCompass.SetUseRotaryPosition.newBuilder().build())
            .build()
    )

    private fun buildSetMagneticDeclination(params: Map<*, *>): Result<JonSharedCmdCompass.Root> = Result.success(
        JonSharedCmdCompass.Root.newBuilder()
            .setSetMagneticDeclination(JonSharedCmdCompass.SetMagneticDeclination.newBuilder().build())
            .build()
    )

    private fun buildSetOffsetAngleAzimuth(params: Map<*, *>): Result<JonSharedCmdCompass.Root> = Result.success(
        JonSharedCmdCompass.Root.newBuilder()
            .setSetOffsetAngleAzimuth(JonSharedCmdCompass.SetOffsetAngleAzimuth.newBuilder().build())
            .build()
    )
}
