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
            "rotaryplatform-rotate-to-gps" -> buildRotateToGps()
            "rotaryplatform-rotate-azimuth" -> buildRotateAzimuth()
            "rotaryplatform-rotate-elevation-to" -> buildRotateElevationTo()
            "rotaryplatform-scan-pause" -> buildScanPause()
            "rotaryplatform-rotate-to-ndc" -> buildRotateToNdc()
            "rotaryplatform-halt-elevation" -> buildHaltElevation()
            "rotaryplatform-scan-start" -> buildScanStart()
            "rotaryplatform-set-elevation-value" -> buildSetElevationValue()
            "rotaryplatform-elevation" -> buildElevation()
            "rotaryplatform-rotate-elevation-relative" -> buildRotateElevationRelative()
            "rotaryplatform-set-platform-azimuth" -> buildSetPlatformAzimuth()
            "rotaryplatform-scan-stop" -> buildScanStop()
            "rotaryplatform-halt-azimuth" -> buildHaltAzimuth()
            "rotaryplatform-start" -> buildStart()
            "rotaryplatform-azimuth" -> buildAzimuth()
            "rotaryplatform-stop" -> buildStop()
            "rotaryplatform-rotate-azimuth-relative" -> buildRotateAzimuthRelative()
            "rotaryplatform-set-origin-gps" -> buildSetOriginGps()
            "rotaryplatform-scan-next" -> buildScanNext()
            "rotaryplatform-set-platform-bank" -> buildSetPlatformBank()
            "rotaryplatform-get-meteo" -> buildGetMeteo()
            "rotaryplatform-set-use-rotary-as-compass" -> buildSetUseRotaryAsCompass()
            "rotaryplatform-rotate-azimuth-relative-set" -> buildRotateAzimuthRelativeSet()
            "rotaryplatform-scan-prev" -> buildScanPrev()
            "rotaryplatform-scan-add-node" -> buildScanAddNode()
            "rotaryplatform-set-platform-elevation" -> buildSetPlatformElevation()
            "rotaryplatform-rotate-elevation-relative-set" -> buildRotateElevationRelativeSet()
            "rotaryplatform-scan-select-node" -> buildScanSelectNode()
            "rotaryplatform-halt" -> buildHalt()
            "rotaryplatform-scan-delete-node" -> buildScanDeleteNode()
            "rotaryplatform-axis" -> buildAxis()
            "rotaryplatform-scan-unpause" -> buildScanUnpause()
            "rotaryplatform-rotate-elevation" -> buildRotateElevation()
            "rotaryplatform-rotate-azimuth-to" -> buildRotateAzimuthTo()
            "rotaryplatform-set-mode" -> buildSetMode()
            "rotaryplatform-set-azimuth-value" -> buildSetAzimuthValue()
            "rotaryplatform-scan-refresh-node-list" -> buildScanRefreshNodeList()
            "rotaryplatform-scan-update-node" -> buildScanUpdateNode()
            
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
    
    private fun buildRotateToGps(): Result<JonSharedCmdRotaryPlatform.Root> = Result.success(
        JonSharedCmdRotaryPlatform.Root.newBuilder()
            .setRotateToGps(JonSharedCmdRotaryPlatform.RotateToGps.newBuilder().build())
            .build()
    )

    private fun buildRotateAzimuth(): Result<JonSharedCmdRotaryPlatform.Root> = Result.success(
        JonSharedCmdRotaryPlatform.Root.newBuilder()
            .setRotateAzimuth(JonSharedCmdRotaryPlatform.RotateAzimuth.newBuilder().build())
            .build()
    )

    private fun buildRotateElevationTo(): Result<JonSharedCmdRotaryPlatform.Root> = Result.success(
        JonSharedCmdRotaryPlatform.Root.newBuilder()
            .setRotateElevationTo(JonSharedCmdRotaryPlatform.RotateElevationTo.newBuilder().build())
            .build()
    )

    private fun buildScanPause(): Result<JonSharedCmdRotaryPlatform.Root> = Result.success(
        JonSharedCmdRotaryPlatform.Root.newBuilder()
            .setScanPause(JonSharedCmdRotaryPlatform.ScanPause.newBuilder().build())
            .build()
    )

    private fun buildRotateToNdc(): Result<JonSharedCmdRotaryPlatform.Root> = Result.success(
        JonSharedCmdRotaryPlatform.Root.newBuilder()
            .setRotateToNdc(JonSharedCmdRotaryPlatform.RotateToNdc.newBuilder().build())
            .build()
    )

    private fun buildHaltElevation(): Result<JonSharedCmdRotaryPlatform.Root> = Result.success(
        JonSharedCmdRotaryPlatform.Root.newBuilder()
            .setHaltElevation(JonSharedCmdRotaryPlatform.HaltElevation.newBuilder().build())
            .build()
    )

    private fun buildScanStart(): Result<JonSharedCmdRotaryPlatform.Root> = Result.success(
        JonSharedCmdRotaryPlatform.Root.newBuilder()
            .setScanStart(JonSharedCmdRotaryPlatform.ScanStart.newBuilder().build())
            .build()
    )

    private fun buildSetElevationValue(): Result<JonSharedCmdRotaryPlatform.Root> = Result.success(
        JonSharedCmdRotaryPlatform.Root.newBuilder()
            .setSetElevationValue(JonSharedCmdRotaryPlatform.SetElevationValue.newBuilder().build())
            .build()
    )

    private fun buildElevation(): Result<JonSharedCmdRotaryPlatform.Root> = Result.success(
        JonSharedCmdRotaryPlatform.Root.newBuilder()
            .setElevation(JonSharedCmdRotaryPlatform.Elevation.newBuilder().build())
            .build()
    )

    private fun buildRotateElevationRelative(): Result<JonSharedCmdRotaryPlatform.Root> = Result.success(
        JonSharedCmdRotaryPlatform.Root.newBuilder()
            .setRotateElevationRelative(JonSharedCmdRotaryPlatform.RotateElevationRelative.newBuilder().build())
            .build()
    )

    private fun buildSetPlatformAzimuth(): Result<JonSharedCmdRotaryPlatform.Root> = Result.success(
        JonSharedCmdRotaryPlatform.Root.newBuilder()
            .setSetPlatformAzimuth(JonSharedCmdRotaryPlatform.SetPlatformAzimuth.newBuilder().build())
            .build()
    )

    private fun buildScanStop(): Result<JonSharedCmdRotaryPlatform.Root> = Result.success(
        JonSharedCmdRotaryPlatform.Root.newBuilder()
            .setScanStop(JonSharedCmdRotaryPlatform.ScanStop.newBuilder().build())
            .build()
    )

    private fun buildHaltAzimuth(): Result<JonSharedCmdRotaryPlatform.Root> = Result.success(
        JonSharedCmdRotaryPlatform.Root.newBuilder()
            .setHaltAzimuth(JonSharedCmdRotaryPlatform.HaltAzimuth.newBuilder().build())
            .build()
    )

    private fun buildStart(): Result<JonSharedCmdRotaryPlatform.Root> = Result.success(
        JonSharedCmdRotaryPlatform.Root.newBuilder()
            .setStart(JonSharedCmdRotaryPlatform.Start.newBuilder().build())
            .build()
    )

    private fun buildAzimuth(): Result<JonSharedCmdRotaryPlatform.Root> = Result.success(
        JonSharedCmdRotaryPlatform.Root.newBuilder()
            .setAzimuth(JonSharedCmdRotaryPlatform.Azimuth.newBuilder().build())
            .build()
    )

    private fun buildStop(): Result<JonSharedCmdRotaryPlatform.Root> = Result.success(
        JonSharedCmdRotaryPlatform.Root.newBuilder()
            .setStop(JonSharedCmdRotaryPlatform.Stop.newBuilder().build())
            .build()
    )

    private fun buildRotateAzimuthRelative(): Result<JonSharedCmdRotaryPlatform.Root> = Result.success(
        JonSharedCmdRotaryPlatform.Root.newBuilder()
            .setRotateAzimuthRelative(JonSharedCmdRotaryPlatform.RotateAzimuthRelative.newBuilder().build())
            .build()
    )

    private fun buildSetOriginGps(): Result<JonSharedCmdRotaryPlatform.Root> = Result.success(
        JonSharedCmdRotaryPlatform.Root.newBuilder()
            .setSetOriginGps(JonSharedCmdRotaryPlatform.SetOriginGps.newBuilder().build())
            .build()
    )

    private fun buildScanNext(): Result<JonSharedCmdRotaryPlatform.Root> = Result.success(
        JonSharedCmdRotaryPlatform.Root.newBuilder()
            .setScanNext(JonSharedCmdRotaryPlatform.ScanNext.newBuilder().build())
            .build()
    )

    private fun buildSetPlatformBank(): Result<JonSharedCmdRotaryPlatform.Root> = Result.success(
        JonSharedCmdRotaryPlatform.Root.newBuilder()
            .setSetPlatformBank(JonSharedCmdRotaryPlatform.SetPlatformBank.newBuilder().build())
            .build()
    )

    private fun buildGetMeteo(): Result<JonSharedCmdRotaryPlatform.Root> = Result.success(
        JonSharedCmdRotaryPlatform.Root.newBuilder()
            .setGetMeteo(JonSharedCmdRotaryPlatform.GetMeteo.newBuilder().build())
            .build()
    )

    private fun buildSetUseRotaryAsCompass(): Result<JonSharedCmdRotaryPlatform.Root> = Result.success(
        JonSharedCmdRotaryPlatform.Root.newBuilder()
            .setSetUseRotaryAsCompass(JonSharedCmdRotaryPlatform.SetUseRotaryAsCompass.newBuilder().build())
            .build()
    )

    private fun buildRotateAzimuthRelativeSet(): Result<JonSharedCmdRotaryPlatform.Root> = Result.success(
        JonSharedCmdRotaryPlatform.Root.newBuilder()
            .setRotateAzimuthRelativeSet(JonSharedCmdRotaryPlatform.RotateAzimuthRelativeSet.newBuilder().build())
            .build()
    )

    private fun buildScanPrev(): Result<JonSharedCmdRotaryPlatform.Root> = Result.success(
        JonSharedCmdRotaryPlatform.Root.newBuilder()
            .setScanPrev(JonSharedCmdRotaryPlatform.ScanPrev.newBuilder().build())
            .build()
    )

    private fun buildScanAddNode(): Result<JonSharedCmdRotaryPlatform.Root> = Result.success(
        JonSharedCmdRotaryPlatform.Root.newBuilder()
            .setScanAddNode(JonSharedCmdRotaryPlatform.ScanAddNode.newBuilder().build())
            .build()
    )

    private fun buildSetPlatformElevation(): Result<JonSharedCmdRotaryPlatform.Root> = Result.success(
        JonSharedCmdRotaryPlatform.Root.newBuilder()
            .setSetPlatformElevation(JonSharedCmdRotaryPlatform.SetPlatformElevation.newBuilder().build())
            .build()
    )

    private fun buildRotateElevationRelativeSet(): Result<JonSharedCmdRotaryPlatform.Root> = Result.success(
        JonSharedCmdRotaryPlatform.Root.newBuilder()
            .setRotateElevationRelativeSet(JonSharedCmdRotaryPlatform.RotateElevationRelativeSet.newBuilder().build())
            .build()
    )

    private fun buildScanSelectNode(): Result<JonSharedCmdRotaryPlatform.Root> = Result.success(
        JonSharedCmdRotaryPlatform.Root.newBuilder()
            .setScanSelectNode(JonSharedCmdRotaryPlatform.ScanSelectNode.newBuilder().build())
            .build()
    )

    private fun buildHalt(): Result<JonSharedCmdRotaryPlatform.Root> = Result.success(
        JonSharedCmdRotaryPlatform.Root.newBuilder()
            .setHalt(JonSharedCmdRotaryPlatform.Halt.newBuilder().build())
            .build()
    )

    private fun buildScanDeleteNode(): Result<JonSharedCmdRotaryPlatform.Root> = Result.success(
        JonSharedCmdRotaryPlatform.Root.newBuilder()
            .setScanDeleteNode(JonSharedCmdRotaryPlatform.ScanDeleteNode.newBuilder().build())
            .build()
    )

    private fun buildAxis(): Result<JonSharedCmdRotaryPlatform.Root> = Result.success(
        JonSharedCmdRotaryPlatform.Root.newBuilder()
            .setAxis(JonSharedCmdRotaryPlatform.Axis.newBuilder().build())
            .build()
    )

    private fun buildScanUnpause(): Result<JonSharedCmdRotaryPlatform.Root> = Result.success(
        JonSharedCmdRotaryPlatform.Root.newBuilder()
            .setScanUnpause(JonSharedCmdRotaryPlatform.ScanUnpause.newBuilder().build())
            .build()
    )

    private fun buildRotateElevation(): Result<JonSharedCmdRotaryPlatform.Root> = Result.success(
        JonSharedCmdRotaryPlatform.Root.newBuilder()
            .setRotateElevation(JonSharedCmdRotaryPlatform.RotateElevation.newBuilder().build())
            .build()
    )

    private fun buildRotateAzimuthTo(): Result<JonSharedCmdRotaryPlatform.Root> = Result.success(
        JonSharedCmdRotaryPlatform.Root.newBuilder()
            .setRotateAzimuthTo(JonSharedCmdRotaryPlatform.RotateAzimuthTo.newBuilder().build())
            .build()
    )

    private fun buildSetMode(): Result<JonSharedCmdRotaryPlatform.Root> = Result.success(
        JonSharedCmdRotaryPlatform.Root.newBuilder()
            .setSetMode(JonSharedCmdRotaryPlatform.SetMode.newBuilder().build())
            .build()
    )

    private fun buildSetAzimuthValue(): Result<JonSharedCmdRotaryPlatform.Root> = Result.success(
        JonSharedCmdRotaryPlatform.Root.newBuilder()
            .setSetAzimuthValue(JonSharedCmdRotaryPlatform.SetAzimuthValue.newBuilder().build())
            .build()
    )

    private fun buildScanRefreshNodeList(): Result<JonSharedCmdRotaryPlatform.Root> = Result.success(
        JonSharedCmdRotaryPlatform.Root.newBuilder()
            .setScanRefreshNodeList(JonSharedCmdRotaryPlatform.ScanRefreshNodeList.newBuilder().build())
            .build()
    )

    private fun buildScanUpdateNode(): Result<JonSharedCmdRotaryPlatform.Root> = Result.success(
        JonSharedCmdRotaryPlatform.Root.newBuilder()
            .setScanUpdateNode(JonSharedCmdRotaryPlatform.ScanUpdateNode.newBuilder().build())
            .build()
    )
}
