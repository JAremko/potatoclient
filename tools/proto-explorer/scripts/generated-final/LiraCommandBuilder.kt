package potatoclient.kotlin.transit.builders

import cmd.JonSharedCmd
import cmd.Lira.JonSharedCmdLira
import com.cognitect.transit.TransitFactory

/**
 * Builder for Lira commands
 * Generated from protobuf specs
 */
object LiraCommandBuilder {
    
    fun build(action: String, params: Map<*, *>): Result<JonSharedCmd.Root> {
        val liraMsg = when (action) {
            "lira-refine_target" -> buildRefineTarget(params)
            "lira-jon-gui-data-lira-target" -> buildJonGuiDataLiraTarget(params)
            
            else -> return Result.failure(
                IllegalArgumentException("Unknown Lira command: $action")
            )
        }
        
        return liraMsg.map { lira ->
            JonSharedCmd.Root.newBuilder()
                .setProtocolVersion(1)
                .setLira(lira)
                .build()
        }
    }
    
    private fun buildRefineTarget(params: Map<*, *>): Result<JonSharedCmdLira.Root> = Result.success(
        JonSharedCmdLira.Root.newBuilder()
            .setRefineTarget(JonSharedCmdLira.Refine_target.newBuilder().build())
            .build()
    )

    private fun buildJonGuiDataLiraTarget(params: Map<*, *>): Result<JonSharedCmdLira.Root> = Result.success(
        JonSharedCmdLira.Root.newBuilder()
            .setJonGuiDataLiraTarget(JonSharedCmdLira.JonGuiDataLiraTarget.newBuilder().build())
            .build()
    )
}
