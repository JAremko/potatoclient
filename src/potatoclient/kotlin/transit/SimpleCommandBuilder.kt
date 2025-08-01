package potatoclient.kotlin.transit

import cmd.CV.JonSharedCmdCv
import cmd.DayCamera.JonSharedCmdDayCamera
import cmd.HeatCamera.JonSharedCmdHeatCamera
import cmd.JonSharedCmd
import cmd.RotaryPlatform.JonSharedCmdRotary
import ser.JonSharedDataTypes

/**
 * Command builder for Transit-based commands with proper error handling
 */
class SimpleCommandBuilder {
    /**
     * Build a command from action and params, returning Result for error handling
     */
    fun buildCommand(
        action: String,
        params: Map<*, *>? = null,
    ): Result<JonSharedCmd.Root> =
        try {
            when (action) {
                "ping" -> Result.success(buildPing())
                "rotary-halt" -> Result.success(buildRotaryHalt())
                "rotary-goto-ndc" -> buildRotaryGotoNDC(params)
                "rotary-set-velocity" -> buildRotarySetVelocity(params)
                "cv-start-track-ndc" -> buildCVStartTrackNDC(params)
                "heat-camera-next-zoom-table-pos" -> Result.success(buildHeatCameraNextZoomTablePos())
                "heat-camera-prev-zoom-table-pos" -> Result.success(buildHeatCameraPrevZoomTablePos())
                "day-camera-next-zoom-table-pos" -> Result.success(buildDayCameraNextZoomTablePos())
                "day-camera-prev-zoom-table-pos" -> Result.success(buildDayCameraPrevZoomTablePos())
                else -> Result.failure(
                    IllegalArgumentException("Unknown command action: $action")
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }

    fun buildPing(): JonSharedCmd.Root {
        // Build a simple ping command
        val pingCmd = JonSharedCmd.Ping.newBuilder().build()

        return JonSharedCmd.Root
            .newBuilder()
            .setPing(pingCmd)
            .build()
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

    private fun buildRotaryGotoNDC(params: Map<*, *>?): Result<JonSharedCmd.Root> {
        if (params == null) {
            return Result.failure(
                IllegalArgumentException("rotary-goto-ndc requires parameters")
            )
        }

        val channel = params["channel"] as? String
            ?: return Result.failure(
                IllegalArgumentException("rotary-goto-ndc missing required parameter: channel")
            )
        
        val x = (params["x"] as? Number)?.toFloat()
            ?: return Result.failure(
                IllegalArgumentException("rotary-goto-ndc missing required parameter: x")
            )
            
        val y = (params["y"] as? Number)?.toFloat()
            ?: return Result.failure(
                IllegalArgumentException("rotary-goto-ndc missing required parameter: y")
            )

        val rotateToNdc =
            JonSharedCmdRotary.RotateToNDC
                .newBuilder()
                .setChannel(parseChannel(channel))
                .setX(x)
                .setY(y)
                .build()

        val rotaryRoot =
            JonSharedCmdRotary.Root
                .newBuilder()
                .setRotateToNdc(rotateToNdc)
                .build()

        return Result.success(
            JonSharedCmd.Root
                .newBuilder()
                .setProtocolVersion(1)
                .setRotary(rotaryRoot)
                .build()
        )
    }

    private fun buildRotarySetVelocity(params: Map<*, *>?): Result<JonSharedCmd.Root> {
        if (params == null) {
            return Result.failure(
                IllegalArgumentException("rotary-set-velocity requires parameters")
            )
        }

        val azSpeed = (params["azimuth-speed"] as? Number)?.toFloat()
            ?: return Result.failure(
                IllegalArgumentException("rotary-set-velocity missing required parameter: azimuth-speed")
            )
            
        val elSpeed = (params["elevation-speed"] as? Number)?.toFloat()
            ?: return Result.failure(
                IllegalArgumentException("rotary-set-velocity missing required parameter: elevation-speed")
            )
            
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

        return Result.success(
            JonSharedCmd.Root
                .newBuilder()
                .setProtocolVersion(1)
                .setRotary(rotaryRoot)
                .build()
        )
    }

    private fun buildCVStartTrackNDC(params: Map<*, *>?): Result<JonSharedCmd.Root> {
        if (params == null) {
            return Result.failure(
                IllegalArgumentException("cv-start-track-ndc requires parameters")
            )
        }

        val channel = params["channel"] as? String
            ?: return Result.failure(
                IllegalArgumentException("cv-start-track-ndc missing required parameter: channel")
            )
            
        val x = (params["x"] as? Number)?.toFloat()
            ?: return Result.failure(
                IllegalArgumentException("cv-start-track-ndc missing required parameter: x")
            )
            
        val y = (params["y"] as? Number)?.toFloat()
            ?: return Result.failure(
                IllegalArgumentException("cv-start-track-ndc missing required parameter: y")
            )
            
        val frameTimestamp = params["frame-timestamp"] as? Long

        val builder =
            JonSharedCmdCv.StartTrackNDC
                .newBuilder()
                .setChannel(parseChannel(channel))
                .setX(x)
                .setY(y)

        // Add frame timestamp if available
        frameTimestamp?.let { builder.setFrameTime(it) }

        val cvRoot =
            JonSharedCmdCv.Root
                .newBuilder()
                .setStartTrackNdc(builder.build())
                .build()

        return Result.success(
            JonSharedCmd.Root
                .newBuilder()
                .setProtocolVersion(1)
                .setCv(cvRoot)
                .build()
        )
    }

    private fun parseChannel(channelStr: String?): JonSharedDataTypes.JonGuiDataVideoChannel =
        when (channelStr) {
            "heat" -> JonSharedDataTypes.JonGuiDataVideoChannel.JON_GUI_DATA_VIDEO_CHANNEL_HEAT
            "day" -> JonSharedDataTypes.JonGuiDataVideoChannel.JON_GUI_DATA_VIDEO_CHANNEL_DAY
            else -> JonSharedDataTypes.JonGuiDataVideoChannel.JON_GUI_DATA_VIDEO_CHANNEL_HEAT
        }

    // Zoom command builders
    private fun buildHeatCameraNextZoomTablePos(): JonSharedCmd.Root {
        val zoom =
            JonSharedCmdHeatCamera.Zoom
                .newBuilder()
                .setNextZoomTablePos(JonSharedCmdHeatCamera.NextZoomTablePos.newBuilder().build())
                .build()

        val heatRoot =
            JonSharedCmdHeatCamera.Root
                .newBuilder()
                .setZoom(zoom)
                .build()

        return JonSharedCmd.Root
            .newBuilder()
            .setProtocolVersion(1)
            .setHeatCamera(heatRoot)
            .build()
    }

    private fun buildHeatCameraPrevZoomTablePos(): JonSharedCmd.Root {
        val zoom =
            JonSharedCmdHeatCamera.Zoom
                .newBuilder()
                .setPrevZoomTablePos(JonSharedCmdHeatCamera.PrevZoomTablePos.newBuilder().build())
                .build()

        val heatRoot =
            JonSharedCmdHeatCamera.Root
                .newBuilder()
                .setZoom(zoom)
                .build()

        return JonSharedCmd.Root
            .newBuilder()
            .setProtocolVersion(1)
            .setHeatCamera(heatRoot)
            .build()
    }

    private fun buildDayCameraNextZoomTablePos(): JonSharedCmd.Root {
        val zoom =
            JonSharedCmdDayCamera.Zoom
                .newBuilder()
                .setNextZoomTablePos(JonSharedCmdDayCamera.NextZoomTablePos.newBuilder().build())
                .build()

        val dayRoot =
            JonSharedCmdDayCamera.Root
                .newBuilder()
                .setZoom(zoom)
                .build()

        return JonSharedCmd.Root
            .newBuilder()
            .setProtocolVersion(1)
            .setDayCamera(dayRoot)
            .build()
    }

    private fun buildDayCameraPrevZoomTablePos(): JonSharedCmd.Root {
        val zoom =
            JonSharedCmdDayCamera.Zoom
                .newBuilder()
                .setPrevZoomTablePos(JonSharedCmdDayCamera.PrevZoomTablePos.newBuilder().build())
                .build()

        val dayRoot =
            JonSharedCmdDayCamera.Root
                .newBuilder()
                .setZoom(zoom)
                .build()

        return JonSharedCmd.Root
            .newBuilder()
            .setProtocolVersion(1)
            .setDayCamera(dayRoot)
            .build()
    }
}