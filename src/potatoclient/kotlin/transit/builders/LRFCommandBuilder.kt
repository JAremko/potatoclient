package potatoclient.kotlin.transit

import cmd.JonSharedCmd
import cmd.Lrf.JonSharedCmdLrf
import com.cognitect.transit.TransitFactory
import ser.JonSharedDataTypes

/**
 * Builder for Laser Range Finder (LRF) commands
 */
object LRFCommandBuilder {
    
    fun build(action: String, params: Map<*, *>): Result<JonSharedCmd.Root> {
        val lrfMsg = when (action) {
            // Basic control
            "lrf-start" -> buildStart()
            "lrf-stop" -> buildStop()
            
            // Measurement
            "lrf-measure" -> buildMeasure()
            
            // Scanning
            "lrf-scan-on" -> buildScanOn()
            "lrf-scan-off" -> buildScanOff()
            
            // Refinement
            "lrf-refine-on" -> buildRefineOn()
            "lrf-refine-off" -> buildRefineOff()
            
            // Target designator
            "lrf-target-designator-off" -> buildTargetDesignatorOff()
            "lrf-target-designator-on-mode-a" -> buildTargetDesignatorOnModeA()
            "lrf-target-designator-on-mode-b" -> buildTargetDesignatorOnModeB()
            
            // Modes
            "lrf-enable-fog-mode" -> buildEnableFogMode()
            "lrf-disable-fog-mode" -> buildDisableFogMode()
            "lrf-set-scan-mode" -> buildSetScanMode(params)
            
            // Session management
            "lrf-new-session" -> buildNewSession()
            
            // Meteorological data
            "lrf-get-meteo" -> buildGetMeteo()
            
            else -> return Result.failure(
                IllegalArgumentException("Unknown LRF command: $action")
            )
        }
        
        return lrfMsg.map { lrf ->
            JonSharedCmd.Root.newBuilder()
                .setProtocolVersion(1)
                .setLrf(lrf)
                .build()
        }
    }
    
    private fun buildStart(): Result<JonSharedCmdLrf.Root> = Result.success(
        JonSharedCmdLrf.Root.newBuilder()
            .setStart(JonSharedCmdLrf.Start.newBuilder().build())
            .build()
    )
    
    private fun buildStop(): Result<JonSharedCmdLrf.Root> = Result.success(
        JonSharedCmdLrf.Root.newBuilder()
            .setStop(JonSharedCmdLrf.Stop.newBuilder().build())
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
    
    private fun buildRefineOff(): Result<JonSharedCmdLrf.Root> = Result.success(
        JonSharedCmdLrf.Root.newBuilder()
            .setRefineOff(JonSharedCmdLrf.RefineOff.newBuilder().build())
            .build()
    )
    
    private fun buildTargetDesignatorOff(): Result<JonSharedCmdLrf.Root> = Result.success(
        JonSharedCmdLrf.Root.newBuilder()
            .setTargetDesignatorOff(JonSharedCmdLrf.TargetDesignatorOff.newBuilder().build())
            .build()
    )
    
    private fun buildTargetDesignatorOnModeA(): Result<JonSharedCmdLrf.Root> = Result.success(
        JonSharedCmdLrf.Root.newBuilder()
            .setTargetDesignatorOnModeA(JonSharedCmdLrf.TargetDesignatorOnModeA.newBuilder().build())
            .build()
    )
    
    private fun buildTargetDesignatorOnModeB(): Result<JonSharedCmdLrf.Root> = Result.success(
        JonSharedCmdLrf.Root.newBuilder()
            .setTargetDesignatorOnModeB(JonSharedCmdLrf.TargetDesignatorOnModeB.newBuilder().build())
            .build()
    )
    
    private fun buildEnableFogMode(): Result<JonSharedCmdLrf.Root> = Result.success(
        JonSharedCmdLrf.Root.newBuilder()
            .setEnableFogMode(JonSharedCmdLrf.EnableFogMode.newBuilder().build())
            .build()
    )
    
    private fun buildDisableFogMode(): Result<JonSharedCmdLrf.Root> = Result.success(
        JonSharedCmdLrf.Root.newBuilder()
            .setDisableFogMode(JonSharedCmdLrf.DisableFogMode.newBuilder().build())
            .build()
    )
    
    private fun buildSetScanMode(params: Map<*, *>): Result<JonSharedCmdLrf.Root> {
        val mode = getStringParam(params, "mode")
            ?: return Result.failure(IllegalArgumentException("Missing mode parameter"))
        
        val scanMode = parseScanMode(mode)
        val setScanMode = JonSharedCmdLrf.SetScanMode.newBuilder()
            .setMode(scanMode)
            .build()
        
        return Result.success(
            JonSharedCmdLrf.Root.newBuilder()
                .setSetScanMode(setScanMode)
                .build()
        )
    }
    
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
    
    // Helper functions
    private fun getStringParam(params: Map<*, *>, key: String): String? {
        return params[key] as? String ?: params[TransitFactory.keyword(key)] as? String
    }
    
    private fun parseScanMode(modeStr: String): JonSharedDataTypes.JonGuiDataLrfScanModes =
        when (modeStr.lowercase()) {
            "1hz" -> JonSharedDataTypes.JonGuiDataLrfScanModes.JON_GUI_DATA_LRF_SCAN_MODE_1_HZ_CONTINUOUS
            "4hz" -> JonSharedDataTypes.JonGuiDataLrfScanModes.JON_GUI_DATA_LRF_SCAN_MODE_4_HZ_CONTINUOUS
            "10hz" -> JonSharedDataTypes.JonGuiDataLrfScanModes.JON_GUI_DATA_LRF_SCAN_MODE_10_HZ_CONTINUOUS
            "20hz" -> JonSharedDataTypes.JonGuiDataLrfScanModes.JON_GUI_DATA_LRF_SCAN_MODE_20_HZ_CONTINUOUS
            "100hz" -> JonSharedDataTypes.JonGuiDataLrfScanModes.JON_GUI_DATA_LRF_SCAN_MODE_100_HZ_CONTINUOUS
            "200hz" -> JonSharedDataTypes.JonGuiDataLrfScanModes.JON_GUI_DATA_LRF_SCAN_MODE_200_HZ_CONTINUOUS
            else -> JonSharedDataTypes.JonGuiDataLrfScanModes.JON_GUI_DATA_LRF_SCAN_MODE_1_HZ_CONTINUOUS
        }
}