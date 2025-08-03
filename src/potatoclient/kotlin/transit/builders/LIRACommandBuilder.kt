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
            "lira-refine-target" -> buildRefineTarget(params)
            
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
    
    private fun buildRefineTarget(params: Map<*, *>): Result<JonSharedCmdLira.Root> {
        val target = params[TransitFactory.keyword("target")] as? Map<*, *>
            ?: return Result.failure(IllegalArgumentException("Target data not provided"))
        
        val targetBuilder = JonSharedCmdLira.JonGuiDataLiraTarget.newBuilder()
        
        // Extract target fields with Transit keyword keys
        (target[TransitFactory.keyword("timestamp")] as? Number)?.let { 
            targetBuilder.setTimestamp(it.toLong()) 
        }
        (target[TransitFactory.keyword("target-longitude")] as? Number)?.let { 
            targetBuilder.setTargetLongitude(it.toDouble()) 
        }
        (target[TransitFactory.keyword("target-latitude")] as? Number)?.let { 
            targetBuilder.setTargetLatitude(it.toDouble()) 
        }
        (target[TransitFactory.keyword("target-altitude")] as? Number)?.let { 
            targetBuilder.setTargetAltitude(it.toDouble()) 
        }
        (target[TransitFactory.keyword("target-azimuth")] as? Number)?.let { 
            targetBuilder.setTargetAzimuth(it.toDouble()) 
        }
        (target[TransitFactory.keyword("target-elevation")] as? Number)?.let { 
            targetBuilder.setTargetElevation(it.toDouble()) 
        }
        (target[TransitFactory.keyword("distance")] as? Number)?.let { 
            targetBuilder.setDistance(it.toDouble()) 
        }
        
        // UUID parts
        (target[TransitFactory.keyword("uuid-part1")] as? Number)?.let { 
            targetBuilder.setUuidPart1(it.toInt()) 
        }
        (target[TransitFactory.keyword("uuid-part2")] as? Number)?.let { 
            targetBuilder.setUuidPart2(it.toInt()) 
        }
        (target[TransitFactory.keyword("uuid-part3")] as? Number)?.let { 
            targetBuilder.setUuidPart3(it.toInt()) 
        }
        (target[TransitFactory.keyword("uuid-part4")] as? Number)?.let { 
            targetBuilder.setUuidPart4(it.toInt()) 
        }
        
        return Result.success(
            JonSharedCmdLira.Root.newBuilder()
                .setRefineTarget(
                    JonSharedCmdLira.Refine_target.newBuilder()
                        .setTarget(targetBuilder)
                        .build()
                )
                .build()
        )
    }
}