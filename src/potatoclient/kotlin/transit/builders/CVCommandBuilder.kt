package potatoclient.kotlin.transit

import cmd.CV.JonSharedCmdCv
import cmd.JonSharedCmd
import com.cognitect.transit.TransitFactory
import ser.JonSharedDataTypes

/**
 * Builder for Computer Vision (CV) commands
 */
object CVCommandBuilder {
    
    fun build(action: String, params: Map<*, *>): Result<JonSharedCmd.Root> {
        val cvMsg = when (action) {
            // Auto focus
            "cv-set-auto-focus" -> buildSetAutoFocus(params)
            
            // Tracking commands
            "cv-start-track-ndc" -> buildStartTrackNDC(params)
            "cv-stop-track" -> buildStopTrack()
            
            // Vampire mode
            "cv-vampire-mode-enable" -> buildVampireModeEnable()
            "cv-vampire-mode-disable" -> buildVampireModeDisable()
            
            // Stabilization
            "cv-stabilization-mode-enable" -> buildStabilizationModeEnable()
            "cv-stabilization-mode-disable" -> buildStabilizationModeDisable()
            
            // Dump
            "cv-dump-start" -> buildDumpStart()
            "cv-dump-stop" -> buildDumpStop()
            
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
    
    private fun buildSetAutoFocus(params: Map<*, *>): Result<JonSharedCmdCv.Root> {
        val channel = getStringParam(params, "channel")
            ?: return Result.failure(IllegalArgumentException("Missing channel parameter"))
        val value = getBooleanParam(params, "value")
            ?: return Result.failure(IllegalArgumentException("Missing value parameter"))
        
        val setAutoFocus = JonSharedCmdCv.SetAutoFocus.newBuilder()
            .setChannel(parseChannel(channel))
            .setValue(value)
            .build()
        
        return Result.success(
            JonSharedCmdCv.Root.newBuilder()
                .setSetAutoFocus(setAutoFocus)
                .build()
        )
    }
    
    private fun buildStartTrackNDC(params: Map<*, *>): Result<JonSharedCmdCv.Root> {
        val channel = getStringParam(params, "channel")
            ?: return Result.failure(IllegalArgumentException("Missing channel parameter"))
        val x = getFloatParam(params, "x")
            ?: return Result.failure(IllegalArgumentException("Missing x parameter"))
        val y = getFloatParam(params, "y")
            ?: return Result.failure(IllegalArgumentException("Missing y parameter"))
        
        val frameTimestamp = getLongParam(params, "frame-timestamp")
        
        val builder = JonSharedCmdCv.StartTrackNDC.newBuilder()
            .setChannel(parseChannel(channel))
            .setX(x)
            .setY(y)
        
        // Add frame timestamp if available
        frameTimestamp?.let { builder.setFrameTime(it) }
        
        return Result.success(
            JonSharedCmdCv.Root.newBuilder()
                .setStartTrackNdc(builder.build())
                .build()
        )
    }
    
    private fun buildStopTrack(): Result<JonSharedCmdCv.Root> = Result.success(
        JonSharedCmdCv.Root.newBuilder()
            .setStopTrack(JonSharedCmdCv.StopTrack.newBuilder().build())
            .build()
    )
    
    private fun buildVampireModeEnable(): Result<JonSharedCmdCv.Root> = Result.success(
        JonSharedCmdCv.Root.newBuilder()
            .setVampireModeEnable(JonSharedCmdCv.VampireModeEnable.newBuilder().build())
            .build()
    )
    
    private fun buildVampireModeDisable(): Result<JonSharedCmdCv.Root> = Result.success(
        JonSharedCmdCv.Root.newBuilder()
            .setVampireModeDisable(JonSharedCmdCv.VampireModeDisable.newBuilder().build())
            .build()
    )
    
    private fun buildStabilizationModeEnable(): Result<JonSharedCmdCv.Root> = Result.success(
        JonSharedCmdCv.Root.newBuilder()
            .setStabilizationModeEnable(JonSharedCmdCv.StabilizationModeEnable.newBuilder().build())
            .build()
    )
    
    private fun buildStabilizationModeDisable(): Result<JonSharedCmdCv.Root> = Result.success(
        JonSharedCmdCv.Root.newBuilder()
            .setStabilizationModeDisable(JonSharedCmdCv.StabilizationModeDisable.newBuilder().build())
            .build()
    )
    
    private fun buildDumpStart(): Result<JonSharedCmdCv.Root> = Result.success(
        JonSharedCmdCv.Root.newBuilder()
            .setDumpStart(JonSharedCmdCv.DumpStart.newBuilder().build())
            .build()
    )
    
    private fun buildDumpStop(): Result<JonSharedCmdCv.Root> = Result.success(
        JonSharedCmdCv.Root.newBuilder()
            .setDumpStop(JonSharedCmdCv.DumpStop.newBuilder().build())
            .build()
    )
    
    // Helper functions
    private fun getStringParam(params: Map<*, *>, key: String): String? {
        return params[key] as? String ?: params[TransitFactory.keyword(key)] as? String
    }
    
    private fun getFloatParam(params: Map<*, *>, key: String): Float? {
        val value = params[key] ?: params[TransitFactory.keyword(key)]
        return (value as? Number)?.toFloat()
    }
    
    private fun getIntParam(params: Map<*, *>, key: String): Int? {
        val value = params[key] ?: params[TransitFactory.keyword(key)]
        return (value as? Number)?.toInt()
    }
    
    private fun getLongParam(params: Map<*, *>, key: String): Long? {
        val value = params[key] ?: params[TransitFactory.keyword(key)]
        return (value as? Number)?.toLong()
    }
    
    private fun parseChannel(channelStr: String): JonSharedDataTypes.JonGuiDataVideoChannel =
        when (channelStr.lowercase()) {
            "heat" -> JonSharedDataTypes.JonGuiDataVideoChannel.JON_GUI_DATA_VIDEO_CHANNEL_HEAT
            "day" -> JonSharedDataTypes.JonGuiDataVideoChannel.JON_GUI_DATA_VIDEO_CHANNEL_DAY
            else -> JonSharedDataTypes.JonGuiDataVideoChannel.JON_GUI_DATA_VIDEO_CHANNEL_HEAT
        }
    
    private fun getBooleanParam(params: Map<*, *>, key: String): Boolean? {
        val value = params[key] ?: params[TransitFactory.keyword(key)]
        return value as? Boolean
    }
}