package potatoclient.kotlin.transit

import cmd.CV.JonSharedCmdCv
import cmd.JonSharedCmd
import cmd.RotaryPlatform.JonSharedCmdRotary
import ser.JonSharedDataTypes

/**
 * Command builder for Transit-based commands
 */
class SimpleCommandBuilder {
    fun buildPing(): JonSharedCmd.Root {
        // Build a simple ping command
        val pingCmd = JonSharedCmd.Ping.newBuilder().build()

        return JonSharedCmd.Root
            .newBuilder()
            .setPing(pingCmd)
            .build()
    }

    fun buildCommand(
        action: String,
        params: Map<*, *>? = null,
    ): JonSharedCmd.Root =
        when (action) {
            "ping" -> buildPing()
            "rotary-halt" -> buildRotaryHalt()
            "rotary-goto-ndc" -> buildRotaryGotoNDC(params)
            "rotary-set-velocity" -> buildRotarySetVelocity(params)
            "cv-start-track-ndc" -> buildCVStartTrackNDC(params)
            else -> buildPing() // Default to ping for unknown commands
        }

    private fun buildRotaryHalt(): JonSharedCmd.Root {
        val haltMsg = JonSharedCmdRotary.Halt.newBuilder().build()

        val rotaryRoot =
            JonSharedCmdRotary.Root
                .newBuilder()
                .setHalt(haltMsg)
                .build()

        return JonSharedCmd.Root
            .newBuilder()
            .setProtocolVersion(1)
            .setRotary(rotaryRoot)
            .build()
    }

    private fun buildRotaryGotoNDC(params: Map<*, *>?): JonSharedCmd.Root {
        if (params == null) return buildPing()

        val channel = parseChannel(params["channel"] as? String)
        val x = (params["x"] as? Number)?.toFloat() ?: return buildPing()
        val y = (params["y"] as? Number)?.toFloat() ?: return buildPing()

        val rotateToNdc =
            JonSharedCmdRotary.RotateToNDC
                .newBuilder()
                .setChannel(channel)
                .setX(x)
                .setY(y)
                .build()

        val rotaryRoot =
            JonSharedCmdRotary.Root
                .newBuilder()
                .setRotateToNdc(rotateToNdc)
                .build()

        return JonSharedCmd.Root
            .newBuilder()
            .setProtocolVersion(1)
            .setRotary(rotaryRoot)
            .build()
    }

    private fun buildRotarySetVelocity(params: Map<*, *>?): JonSharedCmd.Root {
        if (params == null) return buildPing()

        val azSpeed = (params["azimuth-speed"] as? Number)?.toFloat() ?: return buildPing()
        val elSpeed = (params["elevation-speed"] as? Number)?.toFloat() ?: return buildPing()
        val azDirStr = params["azimuth-direction"] as? String ?: "clockwise"
        val elDirStr = params["elevation-direction"] as? String ?: "clockwise"

        // Convert string directions to protobuf enums
        val azDir =
            when (azDirStr) {
                "counter-clockwise" ->
                    JonSharedDataTypes.JonGuiDataRotaryDirection.JON_GUI_DATA_ROTARY_DIRECTION_COUNTER_CLOCKWISE
                else -> JonSharedDataTypes.JonGuiDataRotaryDirection.JON_GUI_DATA_ROTARY_DIRECTION_CLOCKWISE
            }
        val elDir =
            when (elDirStr) {
                "counter-clockwise" ->
                    JonSharedDataTypes.JonGuiDataRotaryDirection.JON_GUI_DATA_ROTARY_DIRECTION_COUNTER_CLOCKWISE
                else -> JonSharedDataTypes.JonGuiDataRotaryDirection.JON_GUI_DATA_ROTARY_DIRECTION_CLOCKWISE
            }

        // Create both axis commands
        val azimuthRotate =
            JonSharedCmdRotary.RotateAzimuth
                .newBuilder()
                .setSpeed(azSpeed)
                .setDirection(azDir)
                .build()

        val elevationRotate =
            JonSharedCmdRotary.RotateElevation
                .newBuilder()
                .setSpeed(elSpeed)
                .setDirection(elDir)
                .build()

        val azimuthMsg =
            JonSharedCmdRotary.Azimuth
                .newBuilder()
                .setRotate(azimuthRotate)
                .build()

        val elevationMsg =
            JonSharedCmdRotary.Elevation
                .newBuilder()
                .setRotate(elevationRotate)
                .build()

        val axisMsg =
            JonSharedCmdRotary.Axis
                .newBuilder()
                .setAzimuth(azimuthMsg)
                .setElevation(elevationMsg)
                .build()

        val rotaryRoot =
            JonSharedCmdRotary.Root
                .newBuilder()
                .setAxis(axisMsg)
                .build()

        return JonSharedCmd.Root
            .newBuilder()
            .setProtocolVersion(1)
            .setRotary(rotaryRoot)
            .build()
    }

    private fun buildCVStartTrackNDC(params: Map<*, *>?): JonSharedCmd.Root {
        if (params == null) return buildPing()

        val channel = parseChannel(params["channel"] as? String)
        val x = (params["x"] as? Number)?.toFloat() ?: return buildPing()
        val y = (params["y"] as? Number)?.toFloat() ?: return buildPing()
        val frameTimestamp = params["frame-timestamp"] as? Long

        val builder =
            JonSharedCmdCv.StartTrackNDC
                .newBuilder()
                .setChannel(channel)
                .setX(x)
                .setY(y)

        // Add frame timestamp if available
        frameTimestamp?.let { builder.setFrameTime(it) }

        val cvRoot =
            JonSharedCmdCv.Root
                .newBuilder()
                .setStartTrackNdc(builder.build())
                .build()

        return JonSharedCmd.Root
            .newBuilder()
            .setProtocolVersion(1)
            .setCv(cvRoot)
            .build()
    }

    private fun parseChannel(channelStr: String?): JonSharedDataTypes.JonGuiDataVideoChannel =
        when (channelStr) {
            "heat" -> JonSharedDataTypes.JonGuiDataVideoChannel.JON_GUI_DATA_VIDEO_CHANNEL_HEAT
            "day" -> JonSharedDataTypes.JonGuiDataVideoChannel.JON_GUI_DATA_VIDEO_CHANNEL_DAY
            else -> JonSharedDataTypes.JonGuiDataVideoChannel.JON_GUI_DATA_VIDEO_CHANNEL_HEAT
        }
}
