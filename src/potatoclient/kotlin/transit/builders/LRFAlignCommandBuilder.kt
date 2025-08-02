package potatoclient.kotlin.transit

import cmd.JonSharedCmd
import cmd.Lrf_calib.JonSharedCmdLrfAlign
import com.cognitect.transit.TransitFactory

/**
 * Builder for Laser Range Finder Alignment commands
 */
object LRFAlignCommandBuilder {
    
    fun build(action: String, params: Map<*, *>): Result<JonSharedCmd.Root> {
        val lrfAlignMsg = when (action) {
            // Basic control
            "lrf-align-start" -> buildStart()
            "lrf-align-stop" -> buildStop()
            
            // Alignment operations
            "lrf-align-save" -> buildSave()
            "lrf-align-reset" -> buildReset()
            
            // Offset adjustments
            "lrf-align-set-offset-x" -> buildSetOffsetX(params)
            "lrf-align-set-offset-y" -> buildSetOffsetY(params)
            
            else -> return Result.failure(
                IllegalArgumentException("Unknown LRF Align command: $action")
            )
        }
        
        return lrfAlignMsg.map { lrfAlign ->
            JonSharedCmd.Root.newBuilder()
                .setProtocolVersion(1)
                .setLrfAlign(lrfAlign)
                .build()
        }
    }
    
    private fun buildStart(): Result<JonSharedCmdLrfAlign.Root> = Result.success(
        JonSharedCmdLrfAlign.Root.newBuilder()
            .setStart(JonSharedCmdLrfAlign.Start.newBuilder().build())
            .build()
    )
    
    private fun buildStop(): Result<JonSharedCmdLrfAlign.Root> = Result.success(
        JonSharedCmdLrfAlign.Root.newBuilder()
            .setStop(JonSharedCmdLrfAlign.Stop.newBuilder().build())
            .build()
    )
    
    private fun buildSave(): Result<JonSharedCmdLrfAlign.Root> = Result.success(
        JonSharedCmdLrfAlign.Root.newBuilder()
            .setSave(JonSharedCmdLrfAlign.Save.newBuilder().build())
            .build()
    )
    
    private fun buildReset(): Result<JonSharedCmdLrfAlign.Root> = Result.success(
        JonSharedCmdLrfAlign.Root.newBuilder()
            .setReset(JonSharedCmdLrfAlign.Reset.newBuilder().build())
            .build()
    )
    
    private fun buildSetOffsetX(params: Map<*, *>): Result<JonSharedCmdLrfAlign.Root> {
        val value = getFloatParam(params, "value")
            ?: return Result.failure(IllegalArgumentException("Missing value parameter"))
        
        val setOffsetX = JonSharedCmdLrfAlign.SetOffsetX.newBuilder()
            .setValue(value)
            .build()
        
        return Result.success(
            JonSharedCmdLrfAlign.Root.newBuilder()
                .setSetOffsetX(setOffsetX)
                .build()
        )
    }
    
    private fun buildSetOffsetY(params: Map<*, *>): Result<JonSharedCmdLrfAlign.Root> {
        val value = getFloatParam(params, "value")
            ?: return Result.failure(IllegalArgumentException("Missing value parameter"))
        
        val setOffsetY = JonSharedCmdLrfAlign.SetOffsetY.newBuilder()
            .setValue(value)
            .build()
        
        return Result.success(
            JonSharedCmdLrfAlign.Root.newBuilder()
                .setSetOffsetY(setOffsetY)
                .build()
        )
    }
    
    // Helper functions
    private fun getFloatParam(params: Map<*, *>, key: String): Float? {
        val value = params[key] ?: params[TransitFactory.keyword(key)]
        return (value as? Number)?.toFloat()
    }
}