package potatoclient.kotlin.transit.builders

import cmd.JonSharedCmd
import cmd.RotaryPlatform.JonSharedCmdRotary
import com.cognitect.transit.TransitFactory

/**
 * Builder for RotaryPlatform commands
 * Generated from protobuf specs
 */
object RotaryPlatformCommandBuilder {
    
    fun build(action: String, params: Map<*, *>): Result<JonSharedCmd.Root> {
        val rotaryplatformMsg = when (action) {
            "rotaryplatform-rotate-to-gps" -> buildRotateToGps(params)
            "rotaryplatform-rotate-azimuth" -> buildRotateAzimuth(params)
            "rotaryplatform-rotate-elevation-to" -> buildRotateElevationTo(params)
            "rotaryplatform-scan-pause" -> buildScanPause(params)
            "rotaryplatform-rotate-to-ndc" -> buildRotateToNdc(params)
            "rotaryplatform-halt-elevation" -> buildHaltElevation(params)
            "rotaryplatform-scan-start" -> buildScanStart(params)
            "rotaryplatform-set-elevation-value" -> buildSetElevationValue(params)
            "rotaryplatform-elevation" -> buildElevation(params)
            "rotaryplatform-rotate-elevation-relative" -> buildRotateElevationRelative(params)
            "rotaryplatform-set-platform-azimuth" -> buildSetPlatformAzimuth(params)
            "rotaryplatform-scan-stop" -> buildScanStop(params)
            "rotaryplatform-halt-azimuth" -> buildHaltAzimuth(params)
            "rotaryplatform-start" -> buildStart(params)
            "rotaryplatform-azimuth" -> buildAzimuth(params)
            "rotaryplatform-stop" -> buildStop(params)
            "rotaryplatform-rotate-azimuth-relative" -> buildRotateAzimuthRelative(params)
            "rotaryplatform-set-origin-gps" -> buildSetOriginGps(params)
            "rotaryplatform-scan-next" -> buildScanNext(params)
            "rotaryplatform-set-platform-bank" -> buildSetPlatformBank(params)
            "rotaryplatform-get-meteo" -> buildGetMeteo(params)
            "rotaryplatform-set-use-rotary-as-compass" -> buildSetUseRotaryAsCompass(params)
            "rotaryplatform-rotate-azimuth-relative-set" -> buildRotateAzimuthRelativeSet(params)
            "rotaryplatform-scan-prev" -> buildScanPrev(params)
            "rotaryplatform-scan-add-node" -> buildScanAddNode(params)
            "rotaryplatform-set-platform-elevation" -> buildSetPlatformElevation(params)
            "rotaryplatform-rotate-elevation-relative-set" -> buildRotateElevationRelativeSet(params)
            "rotaryplatform-scan-select-node" -> buildScanSelectNode(params)
            "rotaryplatform-halt" -> buildHalt(params)
            "rotaryplatform-scan-delete-node" -> buildScanDeleteNode(params)
            "rotaryplatform-axis" -> buildAxis(params)
            "rotaryplatform-scan-unpause" -> buildScanUnpause(params)
            "rotaryplatform-rotate-elevation" -> buildRotateElevation(params)
            "rotaryplatform-rotate-azimuth-to" -> buildRotateAzimuthTo(params)
            "rotaryplatform-set-mode" -> buildSetMode(params)
            "rotaryplatform-set-azimuth-value" -> buildSetAzimuthValue(params)
            "rotaryplatform-scan-refresh-node-list" -> buildScanRefreshNodeList(params)
            "rotaryplatform-scan-update-node" -> buildScanUpdateNode(params)
            
            else -> return Result.failure(
                IllegalArgumentException("Unknown RotaryPlatform command: $action")
            )
        }
        
        return rotaryplatformMsg.map { rotaryplatform ->
            JonSharedCmd.Root.newBuilder()
                .setProtocolVersion(1)
                .setRotary(rotaryplatform)
                .build()
        }
    }
    
    private fun buildRotateToGps(params: Map<*, *>): Result<JonSharedCmdRotary.Root> = Result.success(
        JonSharedCmdRotary.Root.newBuilder()
            .setRotateToGps(JonSharedCmdRotary.RotateToGps.newBuilder().build())
            .build()
    )

    private fun buildRotateAzimuth(params: Map<*, *>): Result<JonSharedCmdRotary.Root> = Result.success(
        JonSharedCmdRotary.Root.newBuilder()
            .setRotateAzimuth(JonSharedCmdRotary.RotateAzimuth.newBuilder().build())
            .build()
    )

    private fun buildRotateElevationTo(params: Map<*, *>): Result<JonSharedCmdRotary.Root> = Result.success(
        JonSharedCmdRotary.Root.newBuilder()
            .setRotateElevationTo(JonSharedCmdRotary.RotateElevationTo.newBuilder().build())
            .build()
    )

    private fun buildScanPause(params: Map<*, *>): Result<JonSharedCmdRotary.Root> = Result.success(
        JonSharedCmdRotary.Root.newBuilder()
            .setScanPause(JonSharedCmdRotary.ScanPause.newBuilder().build())
            .build()
    )

    private fun buildRotateToNdc(params: Map<*, *>): Result<JonSharedCmdRotary.Root> = Result.success(
        JonSharedCmdRotary.Root.newBuilder()
            .setRotateToNdc(JonSharedCmdRotary.RotateToNdc.newBuilder().build())
            .build()
    )

    private fun buildHaltElevation(params: Map<*, *>): Result<JonSharedCmdRotary.Root> = Result.success(
        JonSharedCmdRotary.Root.newBuilder()
            .setHaltElevation(JonSharedCmdRotary.HaltElevation.newBuilder().build())
            .build()
    )

    private fun buildScanStart(params: Map<*, *>): Result<JonSharedCmdRotary.Root> = Result.success(
        JonSharedCmdRotary.Root.newBuilder()
            .setScanStart(JonSharedCmdRotary.ScanStart.newBuilder().build())
            .build()
    )

    private fun buildSetElevationValue(params: Map<*, *>): Result<JonSharedCmdRotary.Root> = Result.success(
        JonSharedCmdRotary.Root.newBuilder()
            .setSetElevationValue(JonSharedCmdRotary.SetElevationValue.newBuilder().build())
            .build()
    )

    private fun buildElevation(params: Map<*, *>): Result<JonSharedCmdRotary.Root> = Result.success(
        JonSharedCmdRotary.Root.newBuilder()
            .setElevation(JonSharedCmdRotary.Elevation.newBuilder().build())
            .build()
    )

    private fun buildRotateElevationRelative(params: Map<*, *>): Result<JonSharedCmdRotary.Root> = Result.success(
        JonSharedCmdRotary.Root.newBuilder()
            .setRotateElevationRelative(JonSharedCmdRotary.RotateElevationRelative.newBuilder().build())
            .build()
    )

    private fun buildSetPlatformAzimuth(params: Map<*, *>): Result<JonSharedCmdRotary.Root> = Result.success(
        JonSharedCmdRotary.Root.newBuilder()
            .setSetPlatformAzimuth(JonSharedCmdRotary.SetPlatformAzimuth.newBuilder().build())
            .build()
    )

    private fun buildScanStop(params: Map<*, *>): Result<JonSharedCmdRotary.Root> = Result.success(
        JonSharedCmdRotary.Root.newBuilder()
            .setScanStop(JonSharedCmdRotary.ScanStop.newBuilder().build())
            .build()
    )

    private fun buildHaltAzimuth(params: Map<*, *>): Result<JonSharedCmdRotary.Root> = Result.success(
        JonSharedCmdRotary.Root.newBuilder()
            .setHaltAzimuth(JonSharedCmdRotary.HaltAzimuth.newBuilder().build())
            .build()
    )

    private fun buildStart(params: Map<*, *>): Result<JonSharedCmdRotary.Root> = Result.success(
        JonSharedCmdRotary.Root.newBuilder()
            .setStart(JonSharedCmdRotary.Start.newBuilder().build())
            .build()
    )

    private fun buildAzimuth(params: Map<*, *>): Result<JonSharedCmdRotary.Root> = Result.success(
        JonSharedCmdRotary.Root.newBuilder()
            .setAzimuth(JonSharedCmdRotary.Azimuth.newBuilder().build())
            .build()
    )

    private fun buildStop(params: Map<*, *>): Result<JonSharedCmdRotary.Root> = Result.success(
        JonSharedCmdRotary.Root.newBuilder()
            .setStop(JonSharedCmdRotary.Stop.newBuilder().build())
            .build()
    )

    private fun buildRotateAzimuthRelative(params: Map<*, *>): Result<JonSharedCmdRotary.Root> = Result.success(
        JonSharedCmdRotary.Root.newBuilder()
            .setRotateAzimuthRelative(JonSharedCmdRotary.RotateAzimuthRelative.newBuilder().build())
            .build()
    )

    private fun buildSetOriginGps(params: Map<*, *>): Result<JonSharedCmdRotary.Root> = Result.success(
        JonSharedCmdRotary.Root.newBuilder()
            .setSetOriginGps(JonSharedCmdRotary.SetOriginGps.newBuilder().build())
            .build()
    )

    private fun buildScanNext(params: Map<*, *>): Result<JonSharedCmdRotary.Root> = Result.success(
        JonSharedCmdRotary.Root.newBuilder()
            .setScanNext(JonSharedCmdRotary.ScanNext.newBuilder().build())
            .build()
    )

    private fun buildSetPlatformBank(params: Map<*, *>): Result<JonSharedCmdRotary.Root> = Result.success(
        JonSharedCmdRotary.Root.newBuilder()
            .setSetPlatformBank(JonSharedCmdRotary.SetPlatformBank.newBuilder().build())
            .build()
    )

    private fun buildGetMeteo(params: Map<*, *>): Result<JonSharedCmdRotary.Root> = Result.success(
        JonSharedCmdRotary.Root.newBuilder()
            .setGetMeteo(JonSharedCmdRotary.GetMeteo.newBuilder().build())
            .build()
    )

    private fun buildSetUseRotaryAsCompass(params: Map<*, *>): Result<JonSharedCmdRotary.Root> = Result.success(
        JonSharedCmdRotary.Root.newBuilder()
            .setSetUseRotaryAsCompass(JonSharedCmdRotary.SetUseRotaryAsCompass.newBuilder().build())
            .build()
    )

    private fun buildRotateAzimuthRelativeSet(params: Map<*, *>): Result<JonSharedCmdRotary.Root> = Result.success(
        JonSharedCmdRotary.Root.newBuilder()
            .setRotateAzimuthRelativeSet(JonSharedCmdRotary.RotateAzimuthRelativeSet.newBuilder().build())
            .build()
    )

    private fun buildScanPrev(params: Map<*, *>): Result<JonSharedCmdRotary.Root> = Result.success(
        JonSharedCmdRotary.Root.newBuilder()
            .setScanPrev(JonSharedCmdRotary.ScanPrev.newBuilder().build())
            .build()
    )

    private fun buildScanAddNode(params: Map<*, *>): Result<JonSharedCmdRotary.Root> = Result.success(
        JonSharedCmdRotary.Root.newBuilder()
            .setScanAddNode(JonSharedCmdRotary.ScanAddNode.newBuilder().build())
            .build()
    )

    private fun buildSetPlatformElevation(params: Map<*, *>): Result<JonSharedCmdRotary.Root> = Result.success(
        JonSharedCmdRotary.Root.newBuilder()
            .setSetPlatformElevation(JonSharedCmdRotary.SetPlatformElevation.newBuilder().build())
            .build()
    )

    private fun buildRotateElevationRelativeSet(params: Map<*, *>): Result<JonSharedCmdRotary.Root> = Result.success(
        JonSharedCmdRotary.Root.newBuilder()
            .setRotateElevationRelativeSet(JonSharedCmdRotary.RotateElevationRelativeSet.newBuilder().build())
            .build()
    )

    private fun buildScanSelectNode(params: Map<*, *>): Result<JonSharedCmdRotary.Root> = Result.success(
        JonSharedCmdRotary.Root.newBuilder()
            .setScanSelectNode(JonSharedCmdRotary.ScanSelectNode.newBuilder().build())
            .build()
    )

    private fun buildHalt(params: Map<*, *>): Result<JonSharedCmdRotary.Root> = Result.success(
        JonSharedCmdRotary.Root.newBuilder()
            .setHalt(JonSharedCmdRotary.Halt.newBuilder().build())
            .build()
    )

    private fun buildScanDeleteNode(params: Map<*, *>): Result<JonSharedCmdRotary.Root> = Result.success(
        JonSharedCmdRotary.Root.newBuilder()
            .setScanDeleteNode(JonSharedCmdRotary.ScanDeleteNode.newBuilder().build())
            .build()
    )

    private fun buildAxis(params: Map<*, *>): Result<JonSharedCmdRotary.Root> = Result.success(
        JonSharedCmdRotary.Root.newBuilder()
            .setAxis(JonSharedCmdRotary.Axis.newBuilder().build())
            .build()
    )

    private fun buildScanUnpause(params: Map<*, *>): Result<JonSharedCmdRotary.Root> = Result.success(
        JonSharedCmdRotary.Root.newBuilder()
            .setScanUnpause(JonSharedCmdRotary.ScanUnpause.newBuilder().build())
            .build()
    )

    private fun buildRotateElevation(params: Map<*, *>): Result<JonSharedCmdRotary.Root> = Result.success(
        JonSharedCmdRotary.Root.newBuilder()
            .setRotateElevation(JonSharedCmdRotary.RotateElevation.newBuilder().build())
            .build()
    )

    private fun buildRotateAzimuthTo(params: Map<*, *>): Result<JonSharedCmdRotary.Root> = Result.success(
        JonSharedCmdRotary.Root.newBuilder()
            .setRotateAzimuthTo(JonSharedCmdRotary.RotateAzimuthTo.newBuilder().build())
            .build()
    )

    private fun buildSetMode(params: Map<*, *>): Result<JonSharedCmdRotary.Root> = Result.success(
        JonSharedCmdRotary.Root.newBuilder()
            .setSetMode(JonSharedCmdRotary.SetMode.newBuilder().build())
            .build()
    )

    private fun buildSetAzimuthValue(params: Map<*, *>): Result<JonSharedCmdRotary.Root> = Result.success(
        JonSharedCmdRotary.Root.newBuilder()
            .setSetAzimuthValue(JonSharedCmdRotary.SetAzimuthValue.newBuilder().build())
            .build()
    )

    private fun buildScanRefreshNodeList(params: Map<*, *>): Result<JonSharedCmdRotary.Root> = Result.success(
        JonSharedCmdRotary.Root.newBuilder()
            .setScanRefreshNodeList(JonSharedCmdRotary.ScanRefreshNodeList.newBuilder().build())
            .build()
    )

    private fun buildScanUpdateNode(params: Map<*, *>): Result<JonSharedCmdRotary.Root> = Result.success(
        JonSharedCmdRotary.Root.newBuilder()
            .setScanUpdateNode(JonSharedCmdRotary.ScanUpdateNode.newBuilder().build())
            .build()
    )
}
