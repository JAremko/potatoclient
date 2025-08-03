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
            "compass-calibrate-cencel" -> buildCalibrateCencel()
            "compass-start" -> buildStart()
            "compass-calibrate-start-short" -> buildCalibrateStartShort()
            "compass-set-offset-angle-elevation" -> buildSetOffsetAngleElevation()
            "compass-stop" -> buildStop()
            "compass-calibrate-start-long" -> buildCalibrateStartLong()
            "compass-next" -> buildNext()
            "compass-calibrate-next" -> buildCalibrateNext()
            "compass-get-meteo" -> buildGetMeteo()
            "compass-set-use-rotary-position" -> buildSetUseRotaryPosition()
            "compass-set-magnetic-declination" -> buildSetMagneticDeclination()
            "compass-set-offset-angle-azimuth" -> buildSetOffsetAngleAzimuth()
            
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
    
    private fun buildCalibrateCencel(): Result<JonSharedCmdCompass.Root> = Result.success(
        JonSharedCmdCompass.Root.newBuilder()
            .setCalibrateCencel(JonSharedCmdCompass.CalibrateCencel.newBuilder().build())
            .build()
    )

    private fun buildStart(): Result<JonSharedCmdCompass.Root> = Result.success(
        JonSharedCmdCompass.Root.newBuilder()
            .setStart(JonSharedCmdCompass.Start.newBuilder().build())
            .build()
    )

    private fun buildCalibrateStartShort(): Result<JonSharedCmdCompass.Root> = Result.success(
        JonSharedCmdCompass.Root.newBuilder()
            .setCalibrateStartShort(JonSharedCmdCompass.CalibrateStartShort.newBuilder().build())
            .build()
    )

    private fun buildSetOffsetAngleElevation(): Result<JonSharedCmdCompass.Root> = Result.success(
        JonSharedCmdCompass.Root.newBuilder()
            .setSetOffsetAngleElevation(JonSharedCmdCompass.SetOffsetAngleElevation.newBuilder().build())
            .build()
    )

    private fun buildStop(): Result<JonSharedCmdCompass.Root> = Result.success(
        JonSharedCmdCompass.Root.newBuilder()
            .setStop(JonSharedCmdCompass.Stop.newBuilder().build())
            .build()
    )

    private fun buildCalibrateStartLong(): Result<JonSharedCmdCompass.Root> = Result.success(
        JonSharedCmdCompass.Root.newBuilder()
            .setCalibrateStartLong(JonSharedCmdCompass.CalibrateStartLong.newBuilder().build())
            .build()
    )

    private fun buildNext(): Result<JonSharedCmdCompass.Root> = Result.success(
        JonSharedCmdCompass.Root.newBuilder()
            .setNext(JonSharedCmdCompass.Next.newBuilder().build())
            .build()
    )

    private fun buildCalibrateNext(): Result<JonSharedCmdCompass.Root> = Result.success(
        JonSharedCmdCompass.Root.newBuilder()
            .setCalibrateNext(JonSharedCmdCompass.CalibrateNext.newBuilder().build())
            .build()
    )

    private fun buildGetMeteo(): Result<JonSharedCmdCompass.Root> = Result.success(
        JonSharedCmdCompass.Root.newBuilder()
            .setGetMeteo(JonSharedCmdCompass.GetMeteo.newBuilder().build())
            .build()
    )

    private fun buildSetUseRotaryPosition(): Result<JonSharedCmdCompass.Root> = Result.success(
        JonSharedCmdCompass.Root.newBuilder()
            .setSetUseRotaryPosition(JonSharedCmdCompass.SetUseRotaryPosition.newBuilder().build())
            .build()
    )

    private fun buildSetMagneticDeclination(): Result<JonSharedCmdCompass.Root> = Result.success(
        JonSharedCmdCompass.Root.newBuilder()
            .setSetMagneticDeclination(JonSharedCmdCompass.SetMagneticDeclination.newBuilder().build())
            .build()
    )

    private fun buildSetOffsetAngleAzimuth(): Result<JonSharedCmdCompass.Root> = Result.success(
        JonSharedCmdCompass.Root.newBuilder()
            .setSetOffsetAngleAzimuth(JonSharedCmdCompass.SetOffsetAngleAzimuth.newBuilder().build())
            .build()
    )
}
