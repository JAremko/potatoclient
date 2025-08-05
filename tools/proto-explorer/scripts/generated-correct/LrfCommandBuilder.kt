package potatoclient.kotlin.transit.builders

import cmd.JonSharedCmd
import cmd.Lrf.JonSharedCmdLrf
import com.cognitect.transit.TransitFactory

/**
 * Builder for Lrf commands
 * Generated from protobuf specs
 */
object LrfCommandBuilder {
    
    fun build(action: String, params: Map<*, *>): Result<JonSharedCmd.Root> {
        val lrfMsg = when (action) {
            "lrf-target-designator-off" -> buildTargetDesignatorOff(params)
            "lrf-target-designator-on-mode-b" -> buildTargetDesignatorOnModeB(params)
            "lrf-disable-fog-mode" -> buildDisableFogMode(params)
            "lrf-set-scan-mode" -> buildSetScanMode(params)
            "lrf-refine-off" -> buildRefineOff(params)
            "lrf-scan-off" -> buildScanOff(params)
            "lrf-refine-on" -> buildRefineOn(params)
            "lrf-start" -> buildStart(params)
            "lrf-measure" -> buildMeasure(params)
            "lrf-scan-on" -> buildScanOn(params)
            "lrf-stop" -> buildStop(params)
            "lrf-new-session" -> buildNewSession(params)
            "lrf-get-meteo" -> buildGetMeteo(params)
            "lrf-enable-fog-mode" -> buildEnableFogMode(params)
            "lrf-target-designator-on-mode-a" -> buildTargetDesignatorOnModeA(params)
            
            else -> return Result.failure(
                IllegalArgumentException("Unknown Lrf command: $action")
            )
        }
        
        return lrfMsg.map { lrf ->
            JonSharedCmd.Root.newBuilder()
                .setProtocolVersion(1)
                .setSetlrf(lrf)
                .build()
        }
    }
    
    private fun buildTargetDesignatorOff(params: Map<*, *>): Result<JonSharedCmdLrf.Root> = Result.success(
        JonSharedCmdLrf.Root.newBuilder()
            .setTargetDesignatorOff(JonSharedCmdLrf.TargetDesignatorOff.newBuilder().build())
            .build()
    )

    private fun buildTargetDesignatorOnModeB(params: Map<*, *>): Result<JonSharedCmdLrf.Root> = Result.success(
        JonSharedCmdLrf.Root.newBuilder()
            .setTargetDesignatorOnModeB(JonSharedCmdLrf.TargetDesignatorOnModeB.newBuilder().build())
            .build()
    )

    private fun buildDisableFogMode(params: Map<*, *>): Result<JonSharedCmdLrf.Root> = Result.success(
        JonSharedCmdLrf.Root.newBuilder()
            .setDisableFogMode(JonSharedCmdLrf.DisableFogMode.newBuilder().build())
            .build()
    )

    private fun buildSetScanMode(params: Map<*, *>): Result<JonSharedCmdLrf.Root> = Result.success(
        JonSharedCmdLrf.Root.newBuilder()
            .setSetScanMode(JonSharedCmdLrf.SetScanMode.newBuilder().build())
            .build()
    )

    private fun buildRefineOff(params: Map<*, *>): Result<JonSharedCmdLrf.Root> = Result.success(
        JonSharedCmdLrf.Root.newBuilder()
            .setRefineOff(JonSharedCmdLrf.RefineOff.newBuilder().build())
            .build()
    )

    private fun buildScanOff(params: Map<*, *>): Result<JonSharedCmdLrf.Root> = Result.success(
        JonSharedCmdLrf.Root.newBuilder()
            .setScanOff(JonSharedCmdLrf.ScanOff.newBuilder().build())
            .build()
    )

    private fun buildRefineOn(params: Map<*, *>): Result<JonSharedCmdLrf.Root> = Result.success(
        JonSharedCmdLrf.Root.newBuilder()
            .setRefineOn(JonSharedCmdLrf.RefineOn.newBuilder().build())
            .build()
    )

    private fun buildStart(params: Map<*, *>): Result<JonSharedCmdLrf.Root> = Result.success(
        JonSharedCmdLrf.Root.newBuilder()
            .setStart(JonSharedCmdLrf.Start.newBuilder().build())
            .build()
    )

    private fun buildMeasure(params: Map<*, *>): Result<JonSharedCmdLrf.Root> = Result.success(
        JonSharedCmdLrf.Root.newBuilder()
            .setMeasure(JonSharedCmdLrf.Measure.newBuilder().build())
            .build()
    )

    private fun buildScanOn(params: Map<*, *>): Result<JonSharedCmdLrf.Root> = Result.success(
        JonSharedCmdLrf.Root.newBuilder()
            .setScanOn(JonSharedCmdLrf.ScanOn.newBuilder().build())
            .build()
    )

    private fun buildStop(params: Map<*, *>): Result<JonSharedCmdLrf.Root> = Result.success(
        JonSharedCmdLrf.Root.newBuilder()
            .setStop(JonSharedCmdLrf.Stop.newBuilder().build())
            .build()
    )

    private fun buildNewSession(params: Map<*, *>): Result<JonSharedCmdLrf.Root> = Result.success(
        JonSharedCmdLrf.Root.newBuilder()
            .setNewSession(JonSharedCmdLrf.NewSession.newBuilder().build())
            .build()
    )

    private fun buildGetMeteo(params: Map<*, *>): Result<JonSharedCmdLrf.Root> = Result.success(
        JonSharedCmdLrf.Root.newBuilder()
            .setGetMeteo(JonSharedCmdLrf.GetMeteo.newBuilder().build())
            .build()
    )

    private fun buildEnableFogMode(params: Map<*, *>): Result<JonSharedCmdLrf.Root> = Result.success(
        JonSharedCmdLrf.Root.newBuilder()
            .setEnableFogMode(JonSharedCmdLrf.EnableFogMode.newBuilder().build())
            .build()
    )

    private fun buildTargetDesignatorOnModeA(params: Map<*, *>): Result<JonSharedCmdLrf.Root> = Result.success(
        JonSharedCmdLrf.Root.newBuilder()
            .setTargetDesignatorOnModeA(JonSharedCmdLrf.TargetDesignatorOnModeA.newBuilder().build())
            .build()
    )
}
