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
            "cv-vampire-mode-enable" -> buildVampireModeEnable()
            "cv-vampire-mode-disable" -> buildVampireModeDisable()
            "cv-dump-stop" -> buildDumpStop()
            "cv-stabilization-mode-disable" -> buildStabilizationModeDisable()
            "cv-set-auto-focus" -> buildSetAutoFocus()
            "cv-start-track-ndc" -> buildStartTrackNdc()
            "cv-dump-start" -> buildDumpStart()
            "cv-stop-track" -> buildStopTrack()
            "cv-stabilization-mode-enable" -> buildStabilizationModeEnable()
            
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
    
    private fun buildVampireModeEnable(): Result<JonSharedCmdCV.Root> = Result.success(
        JonSharedCmdCV.Root.newBuilder()
            .setVampireModeEnable(JonSharedCmdCV.VampireModeEnable.newBuilder().build())
            .build()
    )

    private fun buildVampireModeDisable(): Result<JonSharedCmdCV.Root> = Result.success(
        JonSharedCmdCV.Root.newBuilder()
            .setVampireModeDisable(JonSharedCmdCV.VampireModeDisable.newBuilder().build())
            .build()
    )

    private fun buildDumpStop(): Result<JonSharedCmdCV.Root> = Result.success(
        JonSharedCmdCV.Root.newBuilder()
            .setDumpStop(JonSharedCmdCV.DumpStop.newBuilder().build())
            .build()
    )

    private fun buildStabilizationModeDisable(): Result<JonSharedCmdCV.Root> = Result.success(
        JonSharedCmdCV.Root.newBuilder()
            .setStabilizationModeDisable(JonSharedCmdCV.StabilizationModeDisable.newBuilder().build())
            .build()
    )

    private fun buildSetAutoFocus(): Result<JonSharedCmdCV.Root> = Result.success(
        JonSharedCmdCV.Root.newBuilder()
            .setSetAutoFocus(JonSharedCmdCV.SetAutoFocus.newBuilder().build())
            .build()
    )

    private fun buildStartTrackNdc(): Result<JonSharedCmdCV.Root> = Result.success(
        JonSharedCmdCV.Root.newBuilder()
            .setStartTrackNdc(JonSharedCmdCV.StartTrackNdc.newBuilder().build())
            .build()
    )

    private fun buildDumpStart(): Result<JonSharedCmdCV.Root> = Result.success(
        JonSharedCmdCV.Root.newBuilder()
            .setDumpStart(JonSharedCmdCV.DumpStart.newBuilder().build())
            .build()
    )

    private fun buildStopTrack(): Result<JonSharedCmdCV.Root> = Result.success(
        JonSharedCmdCV.Root.newBuilder()
            .setStopTrack(JonSharedCmdCV.StopTrack.newBuilder().build())
            .build()
    )

    private fun buildStabilizationModeEnable(): Result<JonSharedCmdCV.Root> = Result.success(
        JonSharedCmdCV.Root.newBuilder()
            .setStabilizationModeEnable(JonSharedCmdCV.StabilizationModeEnable.newBuilder().build())
            .build()
    )
}
