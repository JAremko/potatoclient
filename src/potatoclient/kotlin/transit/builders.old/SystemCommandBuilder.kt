package potatoclient.kotlin.transit.builders

import cmd.JonSharedCmd
import cmd.System.JonSharedCmdSystem

/**
 * Builder for System commands
 * Generated from protobuf specs
 */
object SystemCommandBuilder {
    fun build(
        action: String,
        params: Map<*, *>,
    ): Result<JonSharedCmd.Root> {
        val systemMsg =
            when (action) {
                "system-enable-geodesic-mode" -> buildEnableGeodesicMode(params)
                "system-unmark-rec-important" -> buildUnmarkRecImportant(params)
                "system-stop-rec" -> buildStopRec(params)
                "system-stop-a-ll" -> buildStopALl(params)
                "system-reboot" -> buildReboot(params)
                "system-start-rec" -> buildStartRec(params)
                "system-power-off" -> buildPowerOff(params)
                "system-set-localization" -> buildSetLocalization(params)
                "system-reset-configs" -> buildResetConfigs(params)
                "system-disable-geodesic-mode" -> buildDisableGeodesicMode(params)
                "system-enter-transport" -> buildEnterTransport(params)
                "system-mark-rec-important" -> buildMarkRecImportant(params)
                "system-start-a-ll" -> buildStartALl(params)

                else -> return Result.failure(
                    IllegalArgumentException("Unknown System command: $action"),
                )
            }

        return systemMsg.map { system ->
            JonSharedCmd.Root
                .newBuilder()
                .setProtocolVersion(1)
                .setSystem(system)
                .build()
        }
    }

    private fun buildEnableGeodesicMode(params: Map<*, *>): Result<JonSharedCmdSystem.Root> =
        Result.success(
            JonSharedCmdSystem.Root
                .newBuilder()
                .setEnableGeodesicMode(JonSharedCmdSystem.EnableGeodesicMode.newBuilder().build())
                .build(),
        )

    private fun buildUnmarkRecImportant(params: Map<*, *>): Result<JonSharedCmdSystem.Root> =
        Result.success(
            JonSharedCmdSystem.Root
                .newBuilder()
                .setUnmarkRecImportant(JonSharedCmdSystem.UnmarkRecImportant.newBuilder().build())
                .build(),
        )

    private fun buildStopRec(params: Map<*, *>): Result<JonSharedCmdSystem.Root> =
        Result.success(
            JonSharedCmdSystem.Root
                .newBuilder()
                .setStopRec(JonSharedCmdSystem.StopRec.newBuilder().build())
                .build(),
        )

    private fun buildStopALl(params: Map<*, *>): Result<JonSharedCmdSystem.Root> =
        Result.success(
            JonSharedCmdSystem.Root
                .newBuilder()
                .setStopAll(JonSharedCmdSystem.StopALl.newBuilder().build())
                .build(),
        )

    private fun buildReboot(params: Map<*, *>): Result<JonSharedCmdSystem.Root> =
        Result.success(
            JonSharedCmdSystem.Root
                .newBuilder()
                .setReboot(JonSharedCmdSystem.Reboot.newBuilder().build())
                .build(),
        )

    private fun buildStartRec(params: Map<*, *>): Result<JonSharedCmdSystem.Root> =
        Result.success(
            JonSharedCmdSystem.Root
                .newBuilder()
                .setStartRec(JonSharedCmdSystem.StartRec.newBuilder().build())
                .build(),
        )

    private fun buildPowerOff(params: Map<*, *>): Result<JonSharedCmdSystem.Root> =
        Result.success(
            JonSharedCmdSystem.Root
                .newBuilder()
                .setPowerOff(JonSharedCmdSystem.PowerOff.newBuilder().build())
                .build(),
        )

    private fun buildSetLocalization(params: Map<*, *>): Result<JonSharedCmdSystem.Root> =
        Result.success(
            JonSharedCmdSystem.Root
                .newBuilder()
                .setSetLocalization(JonSharedCmdSystem.SetLocalization.newBuilder().build())
                .build(),
        )

    private fun buildResetConfigs(params: Map<*, *>): Result<JonSharedCmdSystem.Root> =
        Result.success(
            JonSharedCmdSystem.Root
                .newBuilder()
                .setResetConfigs(JonSharedCmdSystem.ResetConfigs.newBuilder().build())
                .build(),
        )

    private fun buildDisableGeodesicMode(params: Map<*, *>): Result<JonSharedCmdSystem.Root> =
        Result.success(
            JonSharedCmdSystem.Root
                .newBuilder()
                .setDisableGeodesicMode(JonSharedCmdSystem.DisableGeodesicMode.newBuilder().build())
                .build(),
        )

    private fun buildEnterTransport(params: Map<*, *>): Result<JonSharedCmdSystem.Root> =
        Result.success(
            JonSharedCmdSystem.Root
                .newBuilder()
                .setEnterTransport(JonSharedCmdSystem.EnterTransport.newBuilder().build())
                .build(),
        )

    private fun buildMarkRecImportant(params: Map<*, *>): Result<JonSharedCmdSystem.Root> =
        Result.success(
            JonSharedCmdSystem.Root
                .newBuilder()
                .setMarkRecImportant(JonSharedCmdSystem.MarkRecImportant.newBuilder().build())
                .build(),
        )

    private fun buildStartALl(params: Map<*, *>): Result<JonSharedCmdSystem.Root> =
        Result.success(
            JonSharedCmdSystem.Root
                .newBuilder()
                .setStartAll(JonSharedCmdSystem.StartALl.newBuilder().build())
                .build(),
        )
}
