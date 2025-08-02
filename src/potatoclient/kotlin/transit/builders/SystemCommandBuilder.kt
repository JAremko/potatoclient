package potatoclient.kotlin.transit

import cmd.JonSharedCmd
import cmd.System.JonSharedCmdSystem
import com.cognitect.transit.TransitFactory
import ser.JonSharedDataTypes

/**
 * Builder for system control commands
 */
object SystemCommandBuilder {
    
    fun build(action: String, params: Map<*, *>): Result<JonSharedCmd.Root> {
        val systemMsg = when (action) {
            // Power control
            "system-reboot" -> buildReboot()
            "system-power-off" -> buildPowerOff()
            
            // Configuration
            "system-reset-configs" -> buildResetConfigs()
            "system-set-localization" -> buildSetLocalization(params)
            
            // Process control
            "system-start-all" -> buildStartAll()
            "system-stop-all" -> buildStopAll()
            
            // Recording
            "system-start-recording" -> buildStartRecording()
            "system-stop-recording" -> buildStopRecording()
            "system-mark-rec-important" -> buildMarkRecImportant()
            "system-unmark-rec-important" -> buildUnmarkRecImportant()
            
            // Modes
            "system-enter-transport" -> buildEnterTransport()
            "system-enable-geodesic-mode" -> buildEnableGeodesicMode()
            "system-disable-geodesic-mode" -> buildDisableGeodesicMode()
            
            else -> return Result.failure(
                IllegalArgumentException("Unknown system command: $action")
            )
        }
        
        return systemMsg.map { system ->
            JonSharedCmd.Root.newBuilder()
                .setProtocolVersion(1)
                .setSystem(system)
                .build()
        }
    }
    
    // Power control
    private fun buildReboot(): Result<JonSharedCmdSystem.Root> = Result.success(
        JonSharedCmdSystem.Root.newBuilder()
            .setReboot(JonSharedCmdSystem.Reboot.newBuilder().build())
            .build()
    )
    
    private fun buildPowerOff(): Result<JonSharedCmdSystem.Root> = Result.success(
        JonSharedCmdSystem.Root.newBuilder()
            .setPowerOff(JonSharedCmdSystem.PowerOff.newBuilder().build())
            .build()
    )
    
    // Configuration
    private fun buildResetConfigs(): Result<JonSharedCmdSystem.Root> = Result.success(
        JonSharedCmdSystem.Root.newBuilder()
            .setResetConfigs(JonSharedCmdSystem.ResetConfigs.newBuilder().build())
            .build()
    )
    
    private fun buildSetLocalization(params: Map<*, *>): Result<JonSharedCmdSystem.Root> {
        val localization = getStringParam(params, "localization")
            ?: return Result.failure(IllegalArgumentException("Missing localization parameter"))
        
        val loc = parseLocalization(localization)
        val setLocalization = JonSharedCmdSystem.SetLocalization.newBuilder()
            .setLoc(loc)
            .build()
        
        return Result.success(
            JonSharedCmdSystem.Root.newBuilder()
                .setLocalization(setLocalization)
                .build()
        )
    }
    
    // Process control
    private fun buildStartAll(): Result<JonSharedCmdSystem.Root> = Result.success(
        JonSharedCmdSystem.Root.newBuilder()
            .setStartAll(JonSharedCmdSystem.StartALl.newBuilder().build())
            .build()
    )
    
    private fun buildStopAll(): Result<JonSharedCmdSystem.Root> = Result.success(
        JonSharedCmdSystem.Root.newBuilder()
            .setStopAll(JonSharedCmdSystem.StopALl.newBuilder().build())
            .build()
    )
    
    // Recording
    private fun buildStartRecording(): Result<JonSharedCmdSystem.Root> = Result.success(
        JonSharedCmdSystem.Root.newBuilder()
            .setStartRecording(JonSharedCmdSystem.StartRecording.newBuilder().build())
            .build()
    )
    
    private fun buildStopRecording(): Result<JonSharedCmdSystem.Root> = Result.success(
        JonSharedCmdSystem.Root.newBuilder()
            .setStopRecording(JonSharedCmdSystem.StopRecording.newBuilder().build())
            .build()
    )
    
    private fun buildMarkRecImportant(): Result<JonSharedCmdSystem.Root> = Result.success(
        JonSharedCmdSystem.Root.newBuilder()
            .setMarkRecImportant(JonSharedCmdSystem.MarkRecImportant.newBuilder().build())
            .build()
    )
    
    private fun buildUnmarkRecImportant(): Result<JonSharedCmdSystem.Root> = Result.success(
        JonSharedCmdSystem.Root.newBuilder()
            .setUnmarkRecImportant(JonSharedCmdSystem.UnmarkRecImportant.newBuilder().build())
            .build()
    )
    
    // Modes
    private fun buildEnterTransport(): Result<JonSharedCmdSystem.Root> = Result.success(
        JonSharedCmdSystem.Root.newBuilder()
            .setEnterTransport(JonSharedCmdSystem.EnterTransport.newBuilder().build())
            .build()
    )
    
    private fun buildEnableGeodesicMode(): Result<JonSharedCmdSystem.Root> = Result.success(
        JonSharedCmdSystem.Root.newBuilder()
            .setGeodesicModeEnable(JonSharedCmdSystem.EnableGeodesicMode.newBuilder().build())
            .build()
    )
    
    private fun buildDisableGeodesicMode(): Result<JonSharedCmdSystem.Root> = Result.success(
        JonSharedCmdSystem.Root.newBuilder()
            .setGeodesicModeDisable(JonSharedCmdSystem.DisableGeodesicMode.newBuilder().build())
            .build()
    )
    
    // Helper functions
    private fun getStringParam(params: Map<*, *>, key: String): String? {
        return params[key] as? String ?: params[TransitFactory.keyword(key)] as? String
    }
    
    private fun parseLocalization(locStr: String): JonSharedDataTypes.JonGuiDataSystemLocalizations =
        when (locStr.lowercase()) {
            "en", "english" -> JonSharedDataTypes.JonGuiDataSystemLocalizations.JON_GUI_DATA_SYSTEM_LOCALIZATION_EN
            "ua", "ukrainian" -> JonSharedDataTypes.JonGuiDataSystemLocalizations.JON_GUI_DATA_SYSTEM_LOCALIZATION_UA
            "cs", "czech" -> JonSharedDataTypes.JonGuiDataSystemLocalizations.JON_GUI_DATA_SYSTEM_LOCALIZATION_CS
            "ar", "arabic" -> JonSharedDataTypes.JonGuiDataSystemLocalizations.JON_GUI_DATA_SYSTEM_LOCALIZATION_AR
            else -> JonSharedDataTypes.JonGuiDataSystemLocalizations.JON_GUI_DATA_SYSTEM_LOCALIZATION_EN
        }
}