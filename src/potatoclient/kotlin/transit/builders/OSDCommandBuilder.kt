package potatoclient.kotlin.transit

import cmd.JonSharedCmd
import cmd.OSD.JonSharedCmdOsd
import com.cognitect.transit.TransitFactory

/**
 * Builder for On-Screen Display (OSD) commands
 */
object OSDCommandBuilder {
    
    fun build(action: String, params: Map<*, *>): Result<JonSharedCmd.Root> {
        val osdMsg = when (action) {
            // Screen display commands
            "osd-show-default-screen" -> buildShowDefaultScreen()
            "osd-show-lrf-measure-screen" -> buildShowLrfMeasureScreen()
            "osd-show-lrf-result-screen" -> buildShowLrfResultScreen()
            "osd-show-lrf-result-simplified-screen" -> buildShowLrfResultSimplifiedScreen()
            
            // Heat OSD commands
            "osd-enable-heat-osd" -> buildEnableHeatOsd()
            "osd-disable-heat-osd" -> buildDisableHeatOsd()
            
            // Day OSD commands
            "osd-enable-day-osd" -> buildEnableDayOsd()
            "osd-disable-day-osd" -> buildDisableDayOsd()
            
            else -> return Result.failure(
                IllegalArgumentException("Unknown OSD command: $action")
            )
        }
        
        return osdMsg.map { osd ->
            JonSharedCmd.Root.newBuilder()
                .setProtocolVersion(1)
                .setOsd(osd)
                .build()
        }
    }
    
    private fun buildShowDefaultScreen(): Result<JonSharedCmdOsd.Root> = Result.success(
        JonSharedCmdOsd.Root.newBuilder()
            .setShowDefaultScreen(JonSharedCmdOsd.ShowDefaultScreen.newBuilder().build())
            .build()
    )
    
    private fun buildShowLrfMeasureScreen(): Result<JonSharedCmdOsd.Root> = Result.success(
        JonSharedCmdOsd.Root.newBuilder()
            .setShowLrfMeasureScreen(JonSharedCmdOsd.ShowLRFMeasureScreen.newBuilder().build())
            .build()
    )
    
    private fun buildShowLrfResultScreen(): Result<JonSharedCmdOsd.Root> = Result.success(
        JonSharedCmdOsd.Root.newBuilder()
            .setShowLrfResultScreen(JonSharedCmdOsd.ShowLRFResultScreen.newBuilder().build())
            .build()
    )
    
    private fun buildShowLrfResultSimplifiedScreen(): Result<JonSharedCmdOsd.Root> = Result.success(
        JonSharedCmdOsd.Root.newBuilder()
            .setShowLrfResultSimplifiedScreen(JonSharedCmdOsd.ShowLRFResultSimplifiedScreen.newBuilder().build())
            .build()
    )
    
    private fun buildEnableHeatOsd(): Result<JonSharedCmdOsd.Root> = Result.success(
        JonSharedCmdOsd.Root.newBuilder()
            .setEnableHeatOsd(JonSharedCmdOsd.EnableHeatOSD.newBuilder().build())
            .build()
    )
    
    private fun buildDisableHeatOsd(): Result<JonSharedCmdOsd.Root> = Result.success(
        JonSharedCmdOsd.Root.newBuilder()
            .setDisableHeatOsd(JonSharedCmdOsd.DisableHeatOSD.newBuilder().build())
            .build()
    )
    
    private fun buildEnableDayOsd(): Result<JonSharedCmdOsd.Root> = Result.success(
        JonSharedCmdOsd.Root.newBuilder()
            .setEnableDayOsd(JonSharedCmdOsd.EnableDayOSD.newBuilder().build())
            .build()
    )
    
    private fun buildDisableDayOsd(): Result<JonSharedCmdOsd.Root> = Result.success(
        JonSharedCmdOsd.Root.newBuilder()
            .setDisableDayOsd(JonSharedCmdOsd.DisableDayOSD.newBuilder().build())
            .build()
    )
    
    // Helper functions
    private fun getIntParam(params: Map<*, *>, key: String): Int? {
        val value = params[key] ?: params[TransitFactory.keyword(key)]
        return (value as? Number)?.toInt()
    }
}