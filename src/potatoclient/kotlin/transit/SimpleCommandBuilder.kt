package potatoclient.kotlin.transit

import cmd.CV.JonSharedCmdCv
import cmd.DayCamera.JonSharedCmdDayCamera
import cmd.HeatCamera.JonSharedCmdHeatCamera
import cmd.JonSharedCmd
import cmd.RotaryPlatform.JonSharedCmdRotary
import cmd.System.JonSharedCmdSystem
import cmd.Gps.JonSharedCmdGps
import cmd.Compass.JonSharedCmdCompass
import cmd.Lrf.JonSharedCmdLrf
// OSD commands not found in protobuf
import cmd.DayCamGlassHeater.JonSharedCmdDayCamGlassHeater
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
                // System commands
                "system-reboot" -> Result.success(buildSystemReboot())
                "system-power-off" -> Result.success(buildSystemPowerOff())
                "system-reset-configs" -> Result.success(buildSystemResetConfigs())
                "system-start-all" -> Result.success(buildSystemStartAll())
                "system-stop-all" -> Result.success(buildSystemStopAll())
                "system-mark-rec-important" -> Result.success(buildSystemMarkRecImportant())
                "system-unmark-rec-important" -> Result.success(buildSystemUnmarkRecImportant())
                "system-enter-transport" -> Result.success(buildSystemEnterTransport())
                "system-enable-geodesic-mode" -> Result.success(buildSystemEnableGeodesicMode())
                "system-disable-geodesic-mode" -> Result.success(buildSystemDisableGeodesicMode())
                "system-set-localization" -> buildSystemSetLocalization(params)
                // GPS commands
                "gps-start" -> Result.success(buildGpsStart())
                "gps-stop" -> Result.success(buildGpsStop())
                // Compass commands
                "compass-start" -> Result.success(buildCompassStart())
                "compass-stop" -> Result.success(buildCompassStop())
                // LRF commands
                "lrf-measure" -> Result.success(buildLrfMeasure())
                "lrf-start" -> Result.success(buildLrfStart())
                "lrf-stop" -> Result.success(buildLrfStop())
                // OSD commands removed - not in protobuf
                // Glass heater commands
                "glass-heater-turn-on" -> Result.success(buildGlassHeaterTurnOn())
                "glass-heater-turn-off" -> Result.success(buildGlassHeaterTurnOff())
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
    
    // System command builders
    private fun buildSystemReboot(): JonSharedCmd.Root {
        val systemRoot = JonSharedCmdSystem.Root
            .newBuilder()
            .setReboot(JonSharedCmdSystem.Reboot.newBuilder().build())
            .build()
            
        return JonSharedCmd.Root
            .newBuilder()
            .setProtocolVersion(1)
            .setSystem(systemRoot)
            .build()
    }
    
    private fun buildSystemPowerOff(): JonSharedCmd.Root {
        val systemRoot = JonSharedCmdSystem.Root
            .newBuilder()
            .setPowerOff(JonSharedCmdSystem.PowerOff.newBuilder().build())
            .build()
            
        return JonSharedCmd.Root
            .newBuilder()
            .setProtocolVersion(1)
            .setSystem(systemRoot)
            .build()
    }
    
    private fun buildSystemResetConfigs(): JonSharedCmd.Root {
        val systemRoot = JonSharedCmdSystem.Root
            .newBuilder()
            .setResetConfigs(JonSharedCmdSystem.ResetConfigs.newBuilder().build())
            .build()
            
        return JonSharedCmd.Root
            .newBuilder()
            .setProtocolVersion(1)
            .setSystem(systemRoot)
            .build()
    }
    
    private fun buildSystemStartAll(): JonSharedCmd.Root {
        val systemRoot = JonSharedCmdSystem.Root
            .newBuilder()
            .setStartAll(JonSharedCmdSystem.StartALl.newBuilder().build())
            .build()
            
        return JonSharedCmd.Root
            .newBuilder()
            .setProtocolVersion(1)
            .setSystem(systemRoot)
            .build()
    }
    
    private fun buildSystemStopAll(): JonSharedCmd.Root {
        val systemRoot = JonSharedCmdSystem.Root
            .newBuilder()
            .setStopAll(JonSharedCmdSystem.StopALl.newBuilder().build())
            .build()
            
        return JonSharedCmd.Root
            .newBuilder()
            .setProtocolVersion(1)
            .setSystem(systemRoot)
            .build()
    }
    
    private fun buildSystemMarkRecImportant(): JonSharedCmd.Root {
        val systemRoot = JonSharedCmdSystem.Root
            .newBuilder()
            .setMarkRecImportant(JonSharedCmdSystem.MarkRecImportant.newBuilder().build())
            .build()
            
        return JonSharedCmd.Root
            .newBuilder()
            .setProtocolVersion(1)
            .setSystem(systemRoot)
            .build()
    }
    
    private fun buildSystemUnmarkRecImportant(): JonSharedCmd.Root {
        val systemRoot = JonSharedCmdSystem.Root
            .newBuilder()
            .setUnmarkRecImportant(JonSharedCmdSystem.UnmarkRecImportant.newBuilder().build())
            .build()
            
        return JonSharedCmd.Root
            .newBuilder()
            .setProtocolVersion(1)
            .setSystem(systemRoot)
            .build()
    }
    
    private fun buildSystemEnterTransport(): JonSharedCmd.Root {
        val systemRoot = JonSharedCmdSystem.Root
            .newBuilder()
            .setEnterTransport(JonSharedCmdSystem.EnterTransport.newBuilder().build())
            .build()
            
        return JonSharedCmd.Root
            .newBuilder()
            .setProtocolVersion(1)
            .setSystem(systemRoot)
            .build()
    }
    
    private fun buildSystemEnableGeodesicMode(): JonSharedCmd.Root {
        val systemRoot = JonSharedCmdSystem.Root
            .newBuilder()
            .setGeodesicModeEnable(JonSharedCmdSystem.EnableGeodesicMode.newBuilder().build())
            .build()
            
        return JonSharedCmd.Root
            .newBuilder()
            .setProtocolVersion(1)
            .setSystem(systemRoot)
            .build()
    }
    
    private fun buildSystemDisableGeodesicMode(): JonSharedCmd.Root {
        val systemRoot = JonSharedCmdSystem.Root
            .newBuilder()
            .setGeodesicModeDisable(JonSharedCmdSystem.DisableGeodesicMode.newBuilder().build())
            .build()
            
        return JonSharedCmd.Root
            .newBuilder()
            .setProtocolVersion(1)
            .setSystem(systemRoot)
            .build()
    }
    
    private fun buildSystemSetLocalization(params: Map<*, *>?): Result<JonSharedCmd.Root> {
        if (params == null) {
            return Result.failure(
                IllegalArgumentException("system-set-localization requires parameters")
            )
        }
        
        val localization = params["localization"] as? String
            ?: return Result.failure(
                IllegalArgumentException("system-set-localization missing required parameter: localization")
            )
            
        val loc = parseLocalization(localization)
        
        val setLocalization = JonSharedCmdSystem.SetLocalization
            .newBuilder()
            .setLoc(loc)
            .build()
            
        val systemRoot = JonSharedCmdSystem.Root
            .newBuilder()
            .setLocalization(setLocalization)
            .build()
            
        return Result.success(
            JonSharedCmd.Root
                .newBuilder()
                .setProtocolVersion(1)
                .setSystem(systemRoot)
                .build()
        )
    }
    
    // GPS command builders
    private fun buildGpsStart(): JonSharedCmd.Root {
        val gpsRoot = JonSharedCmdGps.Root
            .newBuilder()
            .setStart(JonSharedCmdGps.Start.newBuilder().build())
            .build()
            
        return JonSharedCmd.Root
            .newBuilder()
            .setProtocolVersion(1)
            .setGps(gpsRoot)
            .build()
    }
    
    private fun buildGpsStop(): JonSharedCmd.Root {
        val gpsRoot = JonSharedCmdGps.Root
            .newBuilder()
            .setStop(JonSharedCmdGps.Stop.newBuilder().build())
            .build()
            
        return JonSharedCmd.Root
            .newBuilder()
            .setProtocolVersion(1)
            .setGps(gpsRoot)
            .build()
    }
    
    // Compass command builders
    private fun buildCompassStart(): JonSharedCmd.Root {
        val compassRoot = JonSharedCmdCompass.Root
            .newBuilder()
            .setStart(JonSharedCmdCompass.Start.newBuilder().build())
            .build()
            
        return JonSharedCmd.Root
            .newBuilder()
            .setProtocolVersion(1)
            .setCompass(compassRoot)
            .build()
    }
    
    private fun buildCompassStop(): JonSharedCmd.Root {
        val compassRoot = JonSharedCmdCompass.Root
            .newBuilder()
            .setStop(JonSharedCmdCompass.Stop.newBuilder().build())
            .build()
            
        return JonSharedCmd.Root
            .newBuilder()
            .setProtocolVersion(1)
            .setCompass(compassRoot)
            .build()
    }
    
    // LRF command builders
    private fun buildLrfMeasure(): JonSharedCmd.Root {
        val lrfRoot = JonSharedCmdLrf.Root
            .newBuilder()
            .setMeasure(JonSharedCmdLrf.Measure.newBuilder().build())
            .build()
            
        return JonSharedCmd.Root
            .newBuilder()
            .setProtocolVersion(1)
            .setLrf(lrfRoot)
            .build()
    }
    
    private fun buildLrfStart(): JonSharedCmd.Root {
        val lrfRoot = JonSharedCmdLrf.Root
            .newBuilder()
            .setStart(JonSharedCmdLrf.Start.newBuilder().build())
            .build()
            
        return JonSharedCmd.Root
            .newBuilder()
            .setProtocolVersion(1)
            .setLrf(lrfRoot)
            .build()
    }
    
    private fun buildLrfStop(): JonSharedCmd.Root {
        val lrfRoot = JonSharedCmdLrf.Root
            .newBuilder()
            .setStop(JonSharedCmdLrf.Stop.newBuilder().build())
            .build()
            
        return JonSharedCmd.Root
            .newBuilder()
            .setProtocolVersion(1)
            .setLrf(lrfRoot)
            .build()
    }
    
    // OSD commands removed - not found in actual protobuf
    
    // Glass heater command builders
    private fun buildGlassHeaterTurnOn(): JonSharedCmd.Root {
        val heaterRoot = JonSharedCmdDayCamGlassHeater.Root
            .newBuilder()
            .setTurnOn(JonSharedCmdDayCamGlassHeater.TurnOn.newBuilder().build())
            .build()
            
        return JonSharedCmd.Root
            .newBuilder()
            .setProtocolVersion(1)
            .setDayCamGlassHeater(heaterRoot)
            .build()
    }
    
    private fun buildGlassHeaterTurnOff(): JonSharedCmd.Root {
        val heaterRoot = JonSharedCmdDayCamGlassHeater.Root
            .newBuilder()
            .setTurnOff(JonSharedCmdDayCamGlassHeater.TurnOff.newBuilder().build())
            .build()
            
        return JonSharedCmd.Root
            .newBuilder()
            .setProtocolVersion(1)
            .setDayCamGlassHeater(heaterRoot)
            .build()
    }
    
    // Helper functions for parsing enums
    private fun parseLocalization(locStr: String): JonSharedDataTypes.JonGuiDataSystemLocalizations =
        when (locStr.lowercase()) {
            "en", "english" -> JonSharedDataTypes.JonGuiDataSystemLocalizations.JON_GUI_DATA_SYSTEM_LOCALIZATION_EN
            "ua", "ukrainian" -> JonSharedDataTypes.JonGuiDataSystemLocalizations.JON_GUI_DATA_SYSTEM_LOCALIZATION_UA
            "cs", "czech" -> JonSharedDataTypes.JonGuiDataSystemLocalizations.JON_GUI_DATA_SYSTEM_LOCALIZATION_CS
            "ar", "arabic" -> JonSharedDataTypes.JonGuiDataSystemLocalizations.JON_GUI_DATA_SYSTEM_LOCALIZATION_AR
            else -> JonSharedDataTypes.JonGuiDataSystemLocalizations.JON_GUI_DATA_SYSTEM_LOCALIZATION_EN
        }
}