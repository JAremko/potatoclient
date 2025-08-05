package potatoclient.kotlin.transit.builders

import cmd.JonSharedCmd
import cmd.CV.JonSharedCmdCV
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
                .setCV(cv)
                .build()
        }
    }
    
    private fun buildVampireModeEnable(params: Map<*, *>): Result<JonSharedCmdCV.Root> = Result.success(
        JonSharedCmdCV.Root.newBuilder()
            .setVampireModeEnable(JonSharedCmdCV.VampireModeEnable.newBuilder().build())
            .build()
    )

    private fun buildVampireModeDisable(params: Map<*, *>): Result<JonSharedCmdCV.Root> = Result.success(
        JonSharedCmdCV.Root.newBuilder()
            .setVampireModeDisable(JonSharedCmdCV.VampireModeDisable.newBuilder().build())
            .build()
    )

    private fun buildDumpStop(params: Map<*, *>): Result<JonSharedCmdCV.Root> = Result.success(
        JonSharedCmdCV.Root.newBuilder()
            .setDumpStop(JonSharedCmdCV.DumpStop.newBuilder().build())
            .build()
    )

    private fun buildStabilizationModeDisable(params: Map<*, *>): Result<JonSharedCmdCV.Root> = Result.success(
        JonSharedCmdCV.Root.newBuilder()
            .setStabilizationModeDisable(JonSharedCmdCV.StabilizationModeDisable.newBuilder().build())
            .build()
    )

    private fun buildSetAutoFocus(params: Map<*, *>): Result<JonSharedCmdCV.Root> = Result.success(
        JonSharedCmdCV.Root.newBuilder()
            .setSetAutoFocus(JonSharedCmdCV.SetAutoFocus.newBuilder().build())
            .build()
    )

    private fun buildStartTrackNdc(params: Map<*, *>): Result<JonSharedCmdCV.Root> = Result.success(
        JonSharedCmdCV.Root.newBuilder()
            .setStartTrackNdc(JonSharedCmdCV.StartTrackNdc.newBuilder().build())
            .build()
    )

    private fun buildDumpStart(params: Map<*, *>): Result<JonSharedCmdCV.Root> = Result.success(
        JonSharedCmdCV.Root.newBuilder()
            .setDumpStart(JonSharedCmdCV.DumpStart.newBuilder().build())
            .build()
    )

    private fun buildStopTrack(params: Map<*, *>): Result<JonSharedCmdCV.Root> = Result.success(
        JonSharedCmdCV.Root.newBuilder()
            .setStopTrack(JonSharedCmdCV.StopTrack.newBuilder().build())
            .build()
    )

    private fun buildStabilizationModeEnable(params: Map<*, *>): Result<JonSharedCmdCV.Root> = Result.success(
        JonSharedCmdCV.Root.newBuilder()
            .setStabilizationModeEnable(JonSharedCmdCV.StabilizationModeEnable.newBuilder().build())
            .build()
    )
}
