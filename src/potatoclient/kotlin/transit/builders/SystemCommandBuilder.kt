package potatoclient.kotlin.transit.builders

import cmd.JonSharedCmd
import cmd.System.JonSharedCmdSystem
import com.cognitect.transit.TransitFactory

/**
 * Builder for System commands
 * Generated from protobuf specs
 */
object SystemCommandBuilder {
    
    fun build(action: String, params: Map<*, *>): Result<JonSharedCmd.Root> {
        val systemMsg = when (action) {
            "system-enable-geodesic-mode" -> buildEnableGeodesicMode()
            "system-unmark-rec-important" -> buildUnmarkRecImportant()
            "system-stop-rec" -> buildStopRec()
            "system-stop-a-ll" -> buildStopALl()
            "system-reboot" -> buildReboot()
            "system-start-rec" -> buildStartRec()
            "system-power-off" -> buildPowerOff()
            "system-set-localization" -> buildSetLocalization()
            "system-reset-configs" -> buildResetConfigs()
            "system-disable-geodesic-mode" -> buildDisableGeodesicMode()
            "system-enter-transport" -> buildEnterTransport()
            "system-mark-rec-important" -> buildMarkRecImportant()
            "system-start-a-ll" -> buildStartALl()
            
            else -> return Result.failure(
                IllegalArgumentException("Unknown System command: $action")
            )
        }
        
        return systemMsg.map { system ->
            JonSharedCmd.Root.newBuilder()
                .setProtocolVersion(1)
                .setSystem(system)
                .build()
        }
    }
    
    private fun buildEnableGeodesicMode(): Result<JonSharedCmdSystem.Root> = Result.success(
        JonSharedCmdSystem.Root.newBuilder()
            .setEnableGeodesicMode(JonSharedCmdSystem.EnableGeodesicMode.newBuilder().build())
            .build()
    )

    private fun buildUnmarkRecImportant(): Result<JonSharedCmdSystem.Root> = Result.success(
        JonSharedCmdSystem.Root.newBuilder()
            .setUnmarkRecImportant(JonSharedCmdSystem.UnmarkRecImportant.newBuilder().build())
            .build()
    )

    private fun buildStopRec(): Result<JonSharedCmdSystem.Root> = Result.success(
        JonSharedCmdSystem.Root.newBuilder()
            .setStopRec(JonSharedCmdSystem.StopRec.newBuilder().build())
            .build()
    )

    private fun buildStopALl(): Result<JonSharedCmdSystem.Root> = Result.success(
        JonSharedCmdSystem.Root.newBuilder()
            .setStopALl(JonSharedCmdSystem.StopALl.newBuilder().build())
            .build()
    )

    private fun buildReboot(): Result<JonSharedCmdSystem.Root> = Result.success(
        JonSharedCmdSystem.Root.newBuilder()
            .setReboot(JonSharedCmdSystem.Reboot.newBuilder().build())
            .build()
    )

    private fun buildStartRec(): Result<JonSharedCmdSystem.Root> = Result.success(
        JonSharedCmdSystem.Root.newBuilder()
            .setStartRec(JonSharedCmdSystem.StartRec.newBuilder().build())
            .build()
    )

    private fun buildPowerOff(): Result<JonSharedCmdSystem.Root> = Result.success(
        JonSharedCmdSystem.Root.newBuilder()
            .setPowerOff(JonSharedCmdSystem.PowerOff.newBuilder().build())
            .build()
    )

    private fun buildSetLocalization(): Result<JonSharedCmdSystem.Root> = Result.success(
        JonSharedCmdSystem.Root.newBuilder()
            .setSetLocalization(JonSharedCmdSystem.SetLocalization.newBuilder().build())
            .build()
    )

    private fun buildResetConfigs(): Result<JonSharedCmdSystem.Root> = Result.success(
        JonSharedCmdSystem.Root.newBuilder()
            .setResetConfigs(JonSharedCmdSystem.ResetConfigs.newBuilder().build())
            .build()
    )

    private fun buildDisableGeodesicMode(): Result<JonSharedCmdSystem.Root> = Result.success(
        JonSharedCmdSystem.Root.newBuilder()
            .setDisableGeodesicMode(JonSharedCmdSystem.DisableGeodesicMode.newBuilder().build())
            .build()
    )

    private fun buildEnterTransport(): Result<JonSharedCmdSystem.Root> = Result.success(
        JonSharedCmdSystem.Root.newBuilder()
            .setEnterTransport(JonSharedCmdSystem.EnterTransport.newBuilder().build())
            .build()
    )

    private fun buildMarkRecImportant(): Result<JonSharedCmdSystem.Root> = Result.success(
        JonSharedCmdSystem.Root.newBuilder()
            .setMarkRecImportant(JonSharedCmdSystem.MarkRecImportant.newBuilder().build())
            .build()
    )

    private fun buildStartALl(): Result<JonSharedCmdSystem.Root> = Result.success(
        JonSharedCmdSystem.Root.newBuilder()
            .setStartALl(JonSharedCmdSystem.StartALl.newBuilder().build())
            .build()
    )
}
