package potatoclient.kotlin.transit.builders

import cmd.JonSharedCmd
import cmd.Lira.JonSharedCmdLira
import com.cognitect.transit.TransitFactory

/**
 * Builder for Lira commands
 * Generated from protobuf specs
 */
object LiraCommandBuilder {
    fun build(
        action: String,
        params: Map<*, *>,
    ): Result<JonSharedCmd.Root> {
        val liraMsg =
            when (action) {
                "lira-refine-target" -> buildRefineTarget(params)

                else -> return Result.failure(
                    IllegalArgumentException("Unknown Lira command: $action"),
                )
            }

        return liraMsg.map { lira ->
            JonSharedCmd.Root
                .newBuilder()
                .setProtocolVersion(1)
                .setLira(lira)
                .build()
        }
    }

    private fun buildRefineTarget(params: Map<*, *>): Result<JonSharedCmdLira.Root> {
        // Extract target parameters if provided
        val target = params[TransitFactory.keyword("target")] as? Map<*, *>

        val builder = JonSharedCmdLira.Root.newBuilder()
        val refineTargetBuilder = JonSharedCmdLira.Refine_target.newBuilder()

        if (target != null) {
            val targetBuilder = JonSharedCmdLira.JonGuiDataLiraTarget.newBuilder()

            // Set fields if present
            (target[TransitFactory.keyword("timestamp")] as? Long)?.let { targetBuilder.timestamp = it }
            (target[TransitFactory.keyword("target-longitude")] as? Double)?.let { targetBuilder.targetLongitude = it }
            (target[TransitFactory.keyword("target-latitude")] as? Double)?.let { targetBuilder.targetLatitude = it }
            (target[TransitFactory.keyword("target-altitude")] as? Double)?.let { targetBuilder.targetAltitude = it }
            (target[TransitFactory.keyword("target-azimuth")] as? Double)?.let { targetBuilder.targetAzimuth = it }
            (target[TransitFactory.keyword("target-elevation")] as? Double)?.let { targetBuilder.targetElevation = it }
            (target[TransitFactory.keyword("distance")] as? Double)?.let { targetBuilder.distance = it }
            (target[TransitFactory.keyword("uuid-part1")] as? Int)?.let { targetBuilder.uuidPart1 = it }
            (target[TransitFactory.keyword("uuid-part2")] as? Int)?.let { targetBuilder.uuidPart2 = it }
            (target[TransitFactory.keyword("uuid-part3")] as? Int)?.let { targetBuilder.uuidPart3 = it }
            (target[TransitFactory.keyword("uuid-part4")] as? Int)?.let { targetBuilder.uuidPart4 = it }

            refineTargetBuilder.target = targetBuilder.build()
        }

        return Result.success(
            builder.setRefineTarget(refineTargetBuilder.build()).build(),
        )
    }
}
