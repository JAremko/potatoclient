package potatoclient.kotlin.transit.builders

import cmd.JonSharedCmd
import cmd.RotaryPlatform.JonSharedCmdRotaryPlatform
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
                .setRotaryPlatform(rotaryplatform)
                .build()
        }
    }
    
    private fun buildRotateToGps(params: Map<*, *>): Result<JonSharedCmdRotaryPlatform.Root> = Result.success(
        JonSharedCmdRotaryPlatform.Root.newBuilder()
            .setRotateToGps(JonSharedCmdRotaryPlatform.RotateToGps.newBuilder().build())
            .build()
    )

    private fun buildRotateAzimuth(params: Map<*, *>): Result<JonSharedCmdRotaryPlatform.Root> = Result.success(
        JonSharedCmdRotaryPlatform.Root.newBuilder()
            .setRotateAzimuth(JonSharedCmdRotaryPlatform.RotateAzimuth.newBuilder().build())
            .build()
    )

    private fun buildRotateElevationTo(params: Map<*, *>): Result<JonSharedCmdRotaryPlatform.Root> = Result.success(
        JonSharedCmdRotaryPlatform.Root.newBuilder()
            .setRotateElevationTo(JonSharedCmdRotaryPlatform.RotateElevationTo.newBuilder().build())
            .build()
    )

    private fun buildScanPause(params: Map<*, *>): Result<JonSharedCmdRotaryPlatform.Root> = Result.success(
        JonSharedCmdRotaryPlatform.Root.newBuilder()
            .setScanPause(JonSharedCmdRotaryPlatform.ScanPause.newBuilder().build())
            .build()
    )

    private fun buildRotateToNdc(params: Map<*, *>): Result<JonSharedCmdRotaryPlatform.Root> = Result.success(
        JonSharedCmdRotaryPlatform.Root.newBuilder()
            .setRotateToNdc(JonSharedCmdRotaryPlatform.RotateToNdc.newBuilder().build())
            .build()
    )

    private fun buildHaltElevation(params: Map<*, *>): Result<JonSharedCmdRotaryPlatform.Root> = Result.success(
        JonSharedCmdRotaryPlatform.Root.newBuilder()
            .setHaltElevation(JonSharedCmdRotaryPlatform.HaltElevation.newBuilder().build())
            .build()
    )

    private fun buildScanStart(params: Map<*, *>): Result<JonSharedCmdRotaryPlatform.Root> = Result.success(
        JonSharedCmdRotaryPlatform.Root.newBuilder()
            .setScanStart(JonSharedCmdRotaryPlatform.ScanStart.newBuilder().build())
            .build()
    )

    private fun buildSetElevationValue(params: Map<*, *>): Result<JonSharedCmdRotaryPlatform.Root> = Result.success(
        JonSharedCmdRotaryPlatform.Root.newBuilder()
            .setSetElevationValue(JonSharedCmdRotaryPlatform.SetElevationValue.newBuilder().build())
            .build()
    )

    private fun buildElevation(params: Map<*, *>): Result<JonSharedCmdRotaryPlatform.Root> = Result.success(
        JonSharedCmdRotaryPlatform.Root.newBuilder()
            .setElevation(JonSharedCmdRotaryPlatform.Elevation.newBuilder().build())
            .build()
    )

    private fun buildRotateElevationRelative(params: Map<*, *>): Result<JonSharedCmdRotaryPlatform.Root> = Result.success(
        JonSharedCmdRotaryPlatform.Root.newBuilder()
            .setRotateElevationRelative(JonSharedCmdRotaryPlatform.RotateElevationRelative.newBuilder().build())
            .build()
    )

    private fun buildSetPlatformAzimuth(params: Map<*, *>): Result<JonSharedCmdRotaryPlatform.Root> = Result.success(
        JonSharedCmdRotaryPlatform.Root.newBuilder()
            .setSetPlatformAzimuth(JonSharedCmdRotaryPlatform.SetPlatformAzimuth.newBuilder().build())
            .build()
    )

    private fun buildScanStop(params: Map<*, *>): Result<JonSharedCmdRotaryPlatform.Root> = Result.success(
        JonSharedCmdRotaryPlatform.Root.newBuilder()
            .setScanStop(JonSharedCmdRotaryPlatform.ScanStop.newBuilder().build())
            .build()
    )

    private fun buildHaltAzimuth(params: Map<*, *>): Result<JonSharedCmdRotaryPlatform.Root> = Result.success(
        JonSharedCmdRotaryPlatform.Root.newBuilder()
            .setHaltAzimuth(JonSharedCmdRotaryPlatform.HaltAzimuth.newBuilder().build())
            .build()
    )

    private fun buildStart(params: Map<*, *>): Result<JonSharedCmdRotaryPlatform.Root> = Result.success(
        JonSharedCmdRotaryPlatform.Root.newBuilder()
            .setStart(JonSharedCmdRotaryPlatform.Start.newBuilder().build())
            .build()
    )

    private fun buildAzimuth(params: Map<*, *>): Result<JonSharedCmdRotaryPlatform.Root> = Result.success(
        JonSharedCmdRotaryPlatform.Root.newBuilder()
            .setAzimuth(JonSharedCmdRotaryPlatform.Azimuth.newBuilder().build())
            .build()
    )

    private fun buildStop(params: Map<*, *>): Result<JonSharedCmdRotaryPlatform.Root> = Result.success(
        JonSharedCmdRotaryPlatform.Root.newBuilder()
            .setStop(JonSharedCmdRotaryPlatform.Stop.newBuilder().build())
            .build()
    )

    private fun buildRotateAzimuthRelative(params: Map<*, *>): Result<JonSharedCmdRotaryPlatform.Root> = Result.success(
        JonSharedCmdRotaryPlatform.Root.newBuilder()
            .setRotateAzimuthRelative(JonSharedCmdRotaryPlatform.RotateAzimuthRelative.newBuilder().build())
            .build()
    )

    private fun buildSetOriginGps(params: Map<*, *>): Result<JonSharedCmdRotaryPlatform.Root> = Result.success(
        JonSharedCmdRotaryPlatform.Root.newBuilder()
            .setSetOriginGps(JonSharedCmdRotaryPlatform.SetOriginGps.newBuilder().build())
            .build()
    )

    private fun buildScanNext(params: Map<*, *>): Result<JonSharedCmdRotaryPlatform.Root> = Result.success(
        JonSharedCmdRotaryPlatform.Root.newBuilder()
            .setScanNext(JonSharedCmdRotaryPlatform.ScanNext.newBuilder().build())
            .build()
    )

    private fun buildSetPlatformBank(params: Map<*, *>): Result<JonSharedCmdRotaryPlatform.Root> = Result.success(
        JonSharedCmdRotaryPlatform.Root.newBuilder()
            .setSetPlatformBank(JonSharedCmdRotaryPlatform.SetPlatformBank.newBuilder().build())
            .build()
    )

    private fun buildGetMeteo(params: Map<*, *>): Result<JonSharedCmdRotaryPlatform.Root> = Result.success(
        JonSharedCmdRotaryPlatform.Root.newBuilder()
            .setGetMeteo(JonSharedCmdRotaryPlatform.GetMeteo.newBuilder().build())
            .build()
    )

    private fun buildSetUseRotaryAsCompass(params: Map<*, *>): Result<JonSharedCmdRotaryPlatform.Root> = Result.success(
        JonSharedCmdRotaryPlatform.Root.newBuilder()
            .setSetUseRotaryAsCompass(JonSharedCmdRotaryPlatform.SetUseRotaryAsCompass.newBuilder().build())
            .build()
    )

    private fun buildRotateAzimuthRelativeSet(params: Map<*, *>): Result<JonSharedCmdRotaryPlatform.Root> = Result.success(
        JonSharedCmdRotaryPlatform.Root.newBuilder()
            .setRotateAzimuthRelativeSet(JonSharedCmdRotaryPlatform.RotateAzimuthRelativeSet.newBuilder().build())
            .build()
    )

    private fun buildScanPrev(params: Map<*, *>): Result<JonSharedCmdRotaryPlatform.Root> = Result.success(
        JonSharedCmdRotaryPlatform.Root.newBuilder()
            .setScanPrev(JonSharedCmdRotaryPlatform.ScanPrev.newBuilder().build())
            .build()
    )

    private fun buildScanAddNode(params: Map<*, *>): Result<JonSharedCmdRotaryPlatform.Root> = Result.success(
        JonSharedCmdRotaryPlatform.Root.newBuilder()
            .setScanAddNode(JonSharedCmdRotaryPlatform.ScanAddNode.newBuilder().build())
            .build()
    )

    private fun buildSetPlatformElevation(params: Map<*, *>): Result<JonSharedCmdRotaryPlatform.Root> = Result.success(
        JonSharedCmdRotaryPlatform.Root.newBuilder()
            .setSetPlatformElevation(JonSharedCmdRotaryPlatform.SetPlatformElevation.newBuilder().build())
            .build()
    )

    private fun buildRotateElevationRelativeSet(params: Map<*, *>): Result<JonSharedCmdRotaryPlatform.Root> = Result.success(
        JonSharedCmdRotaryPlatform.Root.newBuilder()
            .setRotateElevationRelativeSet(JonSharedCmdRotaryPlatform.RotateElevationRelativeSet.newBuilder().build())
            .build()
    )

    private fun buildScanSelectNode(params: Map<*, *>): Result<JonSharedCmdRotaryPlatform.Root> = Result.success(
        JonSharedCmdRotaryPlatform.Root.newBuilder()
            .setScanSelectNode(JonSharedCmdRotaryPlatform.ScanSelectNode.newBuilder().build())
            .build()
    )

    private fun buildHalt(params: Map<*, *>): Result<JonSharedCmdRotaryPlatform.Root> = Result.success(
        JonSharedCmdRotaryPlatform.Root.newBuilder()
            .setHalt(JonSharedCmdRotaryPlatform.Halt.newBuilder().build())
            .build()
    )

    private fun buildScanDeleteNode(params: Map<*, *>): Result<JonSharedCmdRotaryPlatform.Root> = Result.success(
        JonSharedCmdRotaryPlatform.Root.newBuilder()
            .setScanDeleteNode(JonSharedCmdRotaryPlatform.ScanDeleteNode.newBuilder().build())
            .build()
    )

    private fun buildAxis(params: Map<*, *>): Result<JonSharedCmdRotaryPlatform.Root> = Result.success(
        JonSharedCmdRotaryPlatform.Root.newBuilder()
            .setAxis(JonSharedCmdRotaryPlatform.Axis.newBuilder().build())
            .build()
    )

    private fun buildScanUnpause(params: Map<*, *>): Result<JonSharedCmdRotaryPlatform.Root> = Result.success(
        JonSharedCmdRotaryPlatform.Root.newBuilder()
            .setScanUnpause(JonSharedCmdRotaryPlatform.ScanUnpause.newBuilder().build())
            .build()
    )

    private fun buildRotateElevation(params: Map<*, *>): Result<JonSharedCmdRotaryPlatform.Root> = Result.success(
        JonSharedCmdRotaryPlatform.Root.newBuilder()
            .setRotateElevation(JonSharedCmdRotaryPlatform.RotateElevation.newBuilder().build())
            .build()
    )

    private fun buildRotateAzimuthTo(params: Map<*, *>): Result<JonSharedCmdRotaryPlatform.Root> = Result.success(
        JonSharedCmdRotaryPlatform.Root.newBuilder()
            .setRotateAzimuthTo(JonSharedCmdRotaryPlatform.RotateAzimuthTo.newBuilder().build())
            .build()
    )

    private fun buildSetMode(params: Map<*, *>): Result<JonSharedCmdRotaryPlatform.Root> = Result.success(
        JonSharedCmdRotaryPlatform.Root.newBuilder()
            .setSetMode(JonSharedCmdRotaryPlatform.SetMode.newBuilder().build())
            .build()
    )

    private fun buildSetAzimuthValue(params: Map<*, *>): Result<JonSharedCmdRotaryPlatform.Root> = Result.success(
        JonSharedCmdRotaryPlatform.Root.newBuilder()
            .setSetAzimuthValue(JonSharedCmdRotaryPlatform.SetAzimuthValue.newBuilder().build())
            .build()
    )

    private fun buildScanRefreshNodeList(params: Map<*, *>): Result<JonSharedCmdRotaryPlatform.Root> = Result.success(
        JonSharedCmdRotaryPlatform.Root.newBuilder()
            .setScanRefreshNodeList(JonSharedCmdRotaryPlatform.ScanRefreshNodeList.newBuilder().build())
            .build()
    )

    private fun buildScanUpdateNode(params: Map<*, *>): Result<JonSharedCmdRotaryPlatform.Root> = Result.success(
        JonSharedCmdRotaryPlatform.Root.newBuilder()
            .setScanUpdateNode(JonSharedCmdRotaryPlatform.ScanUpdateNode.newBuilder().build())
            .build()
    )
}
