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
            "lrf-target-designator-off" -> buildTargetDesignatorOff()
            "lrf-target-designator-on-mode-b" -> buildTargetDesignatorOnModeB()
            "lrf-disable-fog-mode" -> buildDisableFogMode()
            "lrf-set-scan-mode" -> buildSetScanMode()
            "lrf-refine-off" -> buildRefineOff()
            "lrf-scan-off" -> buildScanOff()
            "lrf-refine-on" -> buildRefineOn()
            "lrf-start" -> buildStart()
            "lrf-measure" -> buildMeasure()
            "lrf-scan-on" -> buildScanOn()
            "lrf-stop" -> buildStop()
            "lrf-new-session" -> buildNewSession()
            "lrf-get-meteo" -> buildGetMeteo()
            "lrf-enable-fog-mode" -> buildEnableFogMode()
            "lrf-target-designator-on-mode-a" -> buildTargetDesignatorOnModeA()
            
            else -> return Result.failure(
                IllegalArgumentException("Unknown Lrf command: $action")
            )
        }
        
        return lrfMsg.map { lrf ->
            JonSharedCmd.Root.newBuilder()
                .setProtocolVersion(1)
                .setLrf(lrf)
                .build()
        }
    }
    
    private fun buildTargetDesignatorOff(): Result<JonSharedCmdLrf.Root> = Result.success(
        JonSharedCmdLrf.Root.newBuilder()
            .setTargetDesignatorOff(JonSharedCmdLrf.TargetDesignatorOff.newBuilder().build())
            .build()
    )

    private fun buildTargetDesignatorOnModeB(): Result<JonSharedCmdLrf.Root> = Result.success(
        JonSharedCmdLrf.Root.newBuilder()
            .setTargetDesignatorOnModeB(JonSharedCmdLrf.TargetDesignatorOnModeB.newBuilder().build())
            .build()
    )

    private fun buildDisableFogMode(): Result<JonSharedCmdLrf.Root> = Result.success(
        JonSharedCmdLrf.Root.newBuilder()
            .setDisableFogMode(JonSharedCmdLrf.DisableFogMode.newBuilder().build())
            .build()
    )

    private fun buildSetScanMode(): Result<JonSharedCmdLrf.Root> = Result.success(
        JonSharedCmdLrf.Root.newBuilder()
            .setSetScanMode(JonSharedCmdLrf.SetScanMode.newBuilder().build())
            .build()
    )

    private fun buildRefineOff(): Result<JonSharedCmdLrf.Root> = Result.success(
        JonSharedCmdLrf.Root.newBuilder()
            .setRefineOff(JonSharedCmdLrf.RefineOff.newBuilder().build())
            .build()
    )

    private fun buildScanOff(): Result<JonSharedCmdLrf.Root> = Result.success(
        JonSharedCmdLrf.Root.newBuilder()
            .setScanOff(JonSharedCmdLrf.ScanOff.newBuilder().build())
            .build()
    )

    private fun buildRefineOn(): Result<JonSharedCmdLrf.Root> = Result.success(
        JonSharedCmdLrf.Root.newBuilder()
            .setRefineOn(JonSharedCmdLrf.RefineOn.newBuilder().build())
            .build()
    )

    private fun buildStart(): Result<JonSharedCmdLrf.Root> = Result.success(
        JonSharedCmdLrf.Root.newBuilder()
            .setStart(JonSharedCmdLrf.Start.newBuilder().build())
            .build()
    )

    private fun buildMeasure(): Result<JonSharedCmdLrf.Root> = Result.success(
        JonSharedCmdLrf.Root.newBuilder()
            .setMeasure(JonSharedCmdLrf.Measure.newBuilder().build())
            .build()
    )

    private fun buildScanOn(): Result<JonSharedCmdLrf.Root> = Result.success(
        JonSharedCmdLrf.Root.newBuilder()
            .setScanOn(JonSharedCmdLrf.ScanOn.newBuilder().build())
            .build()
    )

    private fun buildStop(): Result<JonSharedCmdLrf.Root> = Result.success(
        JonSharedCmdLrf.Root.newBuilder()
            .setStop(JonSharedCmdLrf.Stop.newBuilder().build())
            .build()
    )

    private fun buildNewSession(): Result<JonSharedCmdLrf.Root> = Result.success(
        JonSharedCmdLrf.Root.newBuilder()
            .setNewSession(JonSharedCmdLrf.NewSession.newBuilder().build())
            .build()
    )

    private fun buildGetMeteo(): Result<JonSharedCmdLrf.Root> = Result.success(
        JonSharedCmdLrf.Root.newBuilder()
            .setGetMeteo(JonSharedCmdLrf.GetMeteo.newBuilder().build())
            .build()
    )

    private fun buildEnableFogMode(): Result<JonSharedCmdLrf.Root> = Result.success(
        JonSharedCmdLrf.Root.newBuilder()
            .setEnableFogMode(JonSharedCmdLrf.EnableFogMode.newBuilder().build())
            .build()
    )

    private fun buildTargetDesignatorOnModeA(): Result<JonSharedCmdLrf.Root> = Result.success(
        JonSharedCmdLrf.Root.newBuilder()
            .setTargetDesignatorOnModeA(JonSharedCmdLrf.TargetDesignatorOnModeA.newBuilder().build())
            .build()
    )
}
