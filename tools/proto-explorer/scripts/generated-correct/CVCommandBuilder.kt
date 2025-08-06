package potatoclient.kotlin.transit.builders

import cmd.JonSharedCmd
import cmd.CV.JonSharedCmdCv
import com.cognitect.transit.TransitFactory

/**
 * Builder for CV commands
 * Generated from protobuf specs
 */
object CVCommandBuilder {
    
    fun build(action: String, params: Map<*, *>): Result<JonSharedCmd.Root> {
        val cvMsg = when (action) {
            "cv-vampire-mode-enable" -> buildVampireModeEnable(params)
            "cv-vampire-mode-disable" -> buildVampireModeDisable(params)
            "cv-dump-stop" -> buildDumpStop(params)
            "cv-stabilization-mode-disable" -> buildStabilizationModeDisable(params)
            "cv-set-auto-focus" -> buildSetAutoFocus(params)
            "cv-start-track-ndc" -> buildStartTrackNdc(params)
            "cv-dump-start" -> buildDumpStart(params)
            "cv-stop-track" -> buildStopTrack(params)
            "cv-stabilization-mode-enable" -> buildStabilizationModeEnable(params)
            
            else -> return Result.failure(
                IllegalArgumentException("Unknown CV command: $action")
            )
        }
        
        return cvMsg.map { cv ->
            JonSharedCmd.Root.newBuilder()
                .setProtocolVersion(1)
                .setCv(cv)
                .build()
        }
    }
    
    private fun buildVampireModeEnable(params: Map<*, *>): Result<JonSharedCmdCv.Root> = Result.success(
        JonSharedCmdCv.Root.newBuilder()
            .setVampireModeEnable(JonSharedCmdCv.VampireModeEnable.newBuilder().build())
            .build()
    )

    private fun buildVampireModeDisable(params: Map<*, *>): Result<JonSharedCmdCv.Root> = Result.success(
        JonSharedCmdCv.Root.newBuilder()
            .setVampireModeDisable(JonSharedCmdCv.VampireModeDisable.newBuilder().build())
            .build()
    )

    private fun buildDumpStop(params: Map<*, *>): Result<JonSharedCmdCv.Root> = Result.success(
        JonSharedCmdCv.Root.newBuilder()
            .setDumpStop(JonSharedCmdCv.DumpStop.newBuilder().build())
            .build()
    )

    private fun buildStabilizationModeDisable(params: Map<*, *>): Result<JonSharedCmdCv.Root> = Result.success(
        JonSharedCmdCv.Root.newBuilder()
            .setStabilizationModeDisable(JonSharedCmdCv.StabilizationModeDisable.newBuilder().build())
            .build()
    )

    private fun buildSetAutoFocus(params: Map<*, *>): Result<JonSharedCmdCv.Root> = Result.success(
        JonSharedCmdCv.Root.newBuilder()
            .setSetAutoFocus(JonSharedCmdCv.SetAutoFocus.newBuilder().build())
            .build()
    )

    private fun buildStartTrackNdc(params: Map<*, *>): Result<JonSharedCmdCv.Root> = Result.success(
        JonSharedCmdCv.Root.newBuilder()
            .setStartTrackNdc(JonSharedCmdCv.StartTrackNdc.newBuilder().build())
            .build()
    )

    private fun buildDumpStart(params: Map<*, *>): Result<JonSharedCmdCv.Root> = Result.success(
        JonSharedCmdCv.Root.newBuilder()
            .setDumpStart(JonSharedCmdCv.DumpStart.newBuilder().build())
            .build()
    )

    private fun buildStopTrack(params: Map<*, *>): Result<JonSharedCmdCv.Root> = Result.success(
        JonSharedCmdCv.Root.newBuilder()
            .setStopTrack(JonSharedCmdCv.StopTrack.newBuilder().build())
            .build()
    )

    private fun buildStabilizationModeEnable(params: Map<*, *>): Result<JonSharedCmdCv.Root> = Result.success(
        JonSharedCmdCv.Root.newBuilder()
            .setStabilizationModeEnable(JonSharedCmdCv.StabilizationModeEnable.newBuilder().build())
            .build()
    )
}
