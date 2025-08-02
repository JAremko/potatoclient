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
            // Visibility commands
            "osd-show" -> buildShow()
            "osd-hide" -> buildHide()
            
            // Brightness control
            "osd-set-brightness" -> buildSetBrightness(params)
            
            // Navigation
            "osd-next-page" -> buildNextPage()
            "osd-prev-page" -> buildPrevPage()
            "osd-set-page" -> buildSetPage(params)
            
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
    
    private fun buildShow(): Result<JonSharedCmdOsd.Root> = Result.success(
        JonSharedCmdOsd.Root.newBuilder()
            .setShow(JonSharedCmdOsd.Show.newBuilder().build())
            .build()
    )
    
    private fun buildHide(): Result<JonSharedCmdOsd.Root> = Result.success(
        JonSharedCmdOsd.Root.newBuilder()
            .setHide(JonSharedCmdOsd.Hide.newBuilder().build())
            .build()
    )
    
    private fun buildSetBrightness(params: Map<*, *>): Result<JonSharedCmdOsd.Root> {
        val value = getIntParam(params, "value")
            ?: return Result.failure(IllegalArgumentException("Missing value parameter"))
        
        val setBrightness = JonSharedCmdOsd.SetBrightness.newBuilder()
            .setValue(value)
            .build()
        
        return Result.success(
            JonSharedCmdOsd.Root.newBuilder()
                .setSetBrightness(setBrightness)
                .build()
        )
    }
    
    private fun buildNextPage(): Result<JonSharedCmdOsd.Root> = Result.success(
        JonSharedCmdOsd.Root.newBuilder()
            .setNextPage(JonSharedCmdOsd.NextPage.newBuilder().build())
            .build()
    )
    
    private fun buildPrevPage(): Result<JonSharedCmdOsd.Root> = Result.success(
        JonSharedCmdOsd.Root.newBuilder()
            .setPrevPage(JonSharedCmdOsd.PrevPage.newBuilder().build())
            .build()
    )
    
    private fun buildSetPage(params: Map<*, *>): Result<JonSharedCmdOsd.Root> {
        val value = getIntParam(params, "value")
            ?: return Result.failure(IllegalArgumentException("Missing value parameter"))
        
        val setPage = JonSharedCmdOsd.SetPage.newBuilder()
            .setValue(value)
            .build()
        
        return Result.success(
            JonSharedCmdOsd.Root.newBuilder()
                .setSetPage(setPage)
                .build()
        )
    }
    
    // Helper functions
    private fun getIntParam(params: Map<*, *>, key: String): Int? {
        val value = params[key] ?: params[TransitFactory.keyword(key)]
        return (value as? Number)?.toInt()
    }
}