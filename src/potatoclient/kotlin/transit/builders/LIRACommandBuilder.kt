package potatoclient.kotlin.transit

import cmd.JonSharedCmd
import cmd.Lira.JonSharedCmdLira
import com.cognitect.transit.TransitFactory

/**
 * Builder for Laser Illuminator (LIRA) commands
 */
object LIRACommandBuilder {
    
    fun build(action: String, params: Map<*, *>): Result<JonSharedCmd.Root> {
        val liraMsg = when (action) {
            // Target refinement
            "lira-refine-target" -> buildRefineTarget()
            
            else -> return Result.failure(
                IllegalArgumentException("Unknown LIRA command: $action")
            )
        }
        
        return liraMsg.map { lira ->
            JonSharedCmd.Root.newBuilder()
                .setProtocolVersion(1)
                .setLira(lira)
                .build()
        }
    }
    
    private fun buildRefineTarget(): Result<JonSharedCmdLira.Root> = Result.success(
        JonSharedCmdLira.Root.newBuilder()
            .setRefineTarget(JonSharedCmdLira.Refine_target.newBuilder().build())
            .build()
    )
}