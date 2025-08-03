package potatoclient.kotlin.transit.builders

import cmd.JonSharedCmd
import cmd.OSD.JonSharedCmdOsd

/**
 * Builder for OSD commands
 * Generated from protobuf specs
 */
object OSDCommandBuilder {
    fun build(
        action: String,
        params: Map<*, *>,
    ): Result<JonSharedCmd.Root> {
        val osdMsg =
            when (action) {
                "osd-show-lrf-result-simplified-screen" -> buildShowLrfResultSimplifiedScreen(params)
                "osd-show-lrf-measure-screen" -> buildShowLrfMeasureScreen(params)
                "osd-disable-heat-osd" -> buildDisableHeatOsd(params)
                "osd-disable-day-osd" -> buildDisableDayOsd(params)
                "osd-show-lrf-result-screen" -> buildShowLrfResultScreen(params)
                "osd-enable-heat-osd" -> buildEnableHeatOsd(params)
                "osd-show-default-screen" -> buildShowDefaultScreen(params)
                "osd-enable-day-osd" -> buildEnableDayOsd(params)

                else -> return Result.failure(
                    IllegalArgumentException("Unknown OSD command: $action"),
                )
            }

        return osdMsg.map { osd ->
            JonSharedCmd.Root
                .newBuilder()
                .setProtocolVersion(1)
                .setOsd(osd)
                .build()
        }
    }

    private fun buildShowLrfResultSimplifiedScreen(params: Map<*, *>): Result<JonSharedCmdOsd.Root> =
        Result.success(
            JonSharedCmdOsd.Root
                .newBuilder()
                .setShowLrfResultSimplifiedScreen(JonSharedCmdOsd.ShowLrfResultSimplifiedScreen.newBuilder().build())
                .build(),
        )

    private fun buildShowLrfMeasureScreen(params: Map<*, *>): Result<JonSharedCmdOsd.Root> =
        Result.success(
            JonSharedCmdOsd.Root
                .newBuilder()
                .setShowLrfMeasureScreen(JonSharedCmdOsd.ShowLrfMeasureScreen.newBuilder().build())
                .build(),
        )

    private fun buildDisableHeatOsd(params: Map<*, *>): Result<JonSharedCmdOsd.Root> =
        Result.success(
            JonSharedCmdOsd.Root
                .newBuilder()
                .setDisableHeatOsd(JonSharedCmdOsd.DisableHeatOsd.newBuilder().build())
                .build(),
        )

    private fun buildDisableDayOsd(params: Map<*, *>): Result<JonSharedCmdOsd.Root> =
        Result.success(
            JonSharedCmdOsd.Root
                .newBuilder()
                .setDisableDayOsd(JonSharedCmdOsd.DisableDayOsd.newBuilder().build())
                .build(),
        )

    private fun buildShowLrfResultScreen(params: Map<*, *>): Result<JonSharedCmdOsd.Root> =
        Result.success(
            JonSharedCmdOsd.Root
                .newBuilder()
                .setShowLrfResultScreen(JonSharedCmdOsd.ShowLrfResultScreen.newBuilder().build())
                .build(),
        )

    private fun buildEnableHeatOsd(params: Map<*, *>): Result<JonSharedCmdOsd.Root> =
        Result.success(
            JonSharedCmdOsd.Root
                .newBuilder()
                .setEnableHeatOsd(JonSharedCmdOsd.EnableHeatOsd.newBuilder().build())
                .build(),
        )

    private fun buildShowDefaultScreen(params: Map<*, *>): Result<JonSharedCmdOsd.Root> =
        Result.success(
            JonSharedCmdOsd.Root
                .newBuilder()
                .setShowDefaultScreen(JonSharedCmdOsd.ShowDefaultScreen.newBuilder().build())
                .build(),
        )

    private fun buildEnableDayOsd(params: Map<*, *>): Result<JonSharedCmdOsd.Root> =
        Result.success(
            JonSharedCmdOsd.Root
                .newBuilder()
                .setEnableDayOsd(JonSharedCmdOsd.EnableDayOsd.newBuilder().build())
                .build(),
        )
}
