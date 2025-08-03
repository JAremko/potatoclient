package potatoclient.kotlin.transit.builders

import cmd.JonSharedCmd
import cmd.OSD.JonSharedCmdOSD
import com.cognitect.transit.TransitFactory

/**
 * Builder for OSD commands
 * Generated from protobuf specs
 */
object OSDCommandBuilder {
    
    fun build(action: String, params: Map<*, *>): Result<JonSharedCmd.Root> {
        val osdMsg = when (action) {
            "osd-show-lrf-result-simplified-screen" -> buildShowLrfResultSimplifiedScreen()
            "osd-show-lrf-measure-screen" -> buildShowLrfMeasureScreen()
            "osd-disable-heat-osd" -> buildDisableHeatOsd()
            "osd-disable-day-osd" -> buildDisableDayOsd()
            "osd-show-lrf-result-screen" -> buildShowLrfResultScreen()
            "osd-enable-heat-osd" -> buildEnableHeatOsd()
            "osd-show-default-screen" -> buildShowDefaultScreen()
            "osd-enable-day-osd" -> buildEnableDayOsd()
            
            else -> return Result.failure(
                IllegalArgumentException("Unknown OSD command: $action")
            )
        }
        
        return osdMsg.map { osd ->
            JonSharedCmd.Root.newBuilder()
                .setProtocolVersion(1)
                .setOSD(osd)
                .build()
        }
    }
    
    private fun buildShowLrfResultSimplifiedScreen(): Result<JonSharedCmdOSD.Root> = Result.success(
        JonSharedCmdOSD.Root.newBuilder()
            .setShowLrfResultSimplifiedScreen(JonSharedCmdOSD.ShowLrfResultSimplifiedScreen.newBuilder().build())
            .build()
    )

    private fun buildShowLrfMeasureScreen(): Result<JonSharedCmdOSD.Root> = Result.success(
        JonSharedCmdOSD.Root.newBuilder()
            .setShowLrfMeasureScreen(JonSharedCmdOSD.ShowLrfMeasureScreen.newBuilder().build())
            .build()
    )

    private fun buildDisableHeatOsd(): Result<JonSharedCmdOSD.Root> = Result.success(
        JonSharedCmdOSD.Root.newBuilder()
            .setDisableHeatOsd(JonSharedCmdOSD.DisableHeatOsd.newBuilder().build())
            .build()
    )

    private fun buildDisableDayOsd(): Result<JonSharedCmdOSD.Root> = Result.success(
        JonSharedCmdOSD.Root.newBuilder()
            .setDisableDayOsd(JonSharedCmdOSD.DisableDayOsd.newBuilder().build())
            .build()
    )

    private fun buildShowLrfResultScreen(): Result<JonSharedCmdOSD.Root> = Result.success(
        JonSharedCmdOSD.Root.newBuilder()
            .setShowLrfResultScreen(JonSharedCmdOSD.ShowLrfResultScreen.newBuilder().build())
            .build()
    )

    private fun buildEnableHeatOsd(): Result<JonSharedCmdOSD.Root> = Result.success(
        JonSharedCmdOSD.Root.newBuilder()
            .setEnableHeatOsd(JonSharedCmdOSD.EnableHeatOsd.newBuilder().build())
            .build()
    )

    private fun buildShowDefaultScreen(): Result<JonSharedCmdOSD.Root> = Result.success(
        JonSharedCmdOSD.Root.newBuilder()
            .setShowDefaultScreen(JonSharedCmdOSD.ShowDefaultScreen.newBuilder().build())
            .build()
    )

    private fun buildEnableDayOsd(): Result<JonSharedCmdOSD.Root> = Result.success(
        JonSharedCmdOSD.Root.newBuilder()
            .setEnableDayOsd(JonSharedCmdOSD.EnableDayOsd.newBuilder().build())
            .build()
    )
}
