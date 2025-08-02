package potatoclient.kotlin.transit

import cmd.CV.JonSharedCmdCv
import cmd.DayCamera.JonSharedCmdDayCamera
import cmd.HeatCamera.JonSharedCmdHeatCamera
import cmd.JonSharedCmd
import cmd.RotaryPlatform.JonSharedCmdRotary
import cmd.System.JonSharedCmdSystem
import cmd.GPS.JonSharedCmdGps
import cmd.Compass.JonSharedCmdCompass
import cmd.LRF.JonSharedCmdLrf
import cmd.OSD.JonSharedCmdOsd
import cmd.DayCamGlassHeater.JonSharedCmdDayCamGlassHeater
import com.cognitect.transit.ReadHandler
import com.cognitect.transit.TransitFactory
import potatoclient.transit.EventType
import potatoclient.transit.MessageType
import ser.JonSharedDataTypes

/**
 * Transit read handlers for automatic command construction from Transit messages.
 * Following the clean architecture principle - no manual parsing, just handlers.
 */
object ProtobufCommandHandlers {
    /**
     * Create read handlers for command building
     * These handlers automatically convert Transit keywords to appropriate protobuf enums and values
     */
    fun createReadHandlers(): Map<String, ReadHandler<*, *>> {
        val handlers = mutableMapOf<String, ReadHandler<*, *>>()
        
        // Video channel enum handler
        handlers["video-channel"] = createEnumReadHandler { value ->
            when (value) {
                "heat" -> JonSharedDataTypes.JonGuiDataVideoChannel.JON_GUI_DATA_VIDEO_CHANNEL_HEAT
                "day" -> JonSharedDataTypes.JonGuiDataVideoChannel.JON_GUI_DATA_VIDEO_CHANNEL_DAY
                else -> JonSharedDataTypes.JonGuiDataVideoChannel.JON_GUI_DATA_VIDEO_CHANNEL_HEAT
            }
        }
        
        // Rotary direction enum handler
        handlers["rotary-direction"] = createEnumReadHandler { value ->
            when (value) {
                "clockwise" -> JonSharedDataTypes.JonGuiDataRotaryDirection.JON_GUI_DATA_ROTARY_DIRECTION_CLOCKWISE
                "counter-clockwise" -> JonSharedDataTypes.JonGuiDataRotaryDirection.JON_GUI_DATA_ROTARY_DIRECTION_COUNTER_CLOCKWISE
                else -> JonSharedDataTypes.JonGuiDataRotaryDirection.JON_GUI_DATA_ROTARY_DIRECTION_CLOCKWISE
            }
        }
        
        // System localization enum handler
        handlers["localization"] = createEnumReadHandler { value ->
            when (value) {
                "en", "english" -> JonSharedDataTypes.JonGuiDataSystemLocalizations.JON_GUI_DATA_SYSTEM_LOCALIZATIONS_EN
                "ua", "ukrainian" -> JonSharedDataTypes.JonGuiDataSystemLocalizations.JON_GUI_DATA_SYSTEM_LOCALIZATIONS_UA
                "cs", "czech" -> JonSharedDataTypes.JonGuiDataSystemLocalizations.JON_GUI_DATA_SYSTEM_LOCALIZATIONS_CS
                "ar", "arabic" -> JonSharedDataTypes.JonGuiDataSystemLocalizations.JON_GUI_DATA_SYSTEM_LOCALIZATIONS_AR
                "fr", "french" -> JonSharedDataTypes.JonGuiDataSystemLocalizations.JON_GUI_DATA_SYSTEM_LOCALIZATIONS_FR
                "de", "german" -> JonSharedDataTypes.JonGuiDataSystemLocalizations.JON_GUI_DATA_SYSTEM_LOCALIZATIONS_DE
                "es", "spanish" -> JonSharedDataTypes.JonGuiDataSystemLocalizations.JON_GUI_DATA_SYSTEM_LOCALIZATIONS_ES
                else -> JonSharedDataTypes.JonGuiDataSystemLocalizations.JON_GUI_DATA_SYSTEM_LOCALIZATIONS_EN
            }
        }
        
        // Glass heater mode enum handler
        handlers["glass-heater-mode"] = createEnumReadHandler { value ->
            when (value) {
                "auto" -> JonSharedDataTypes.JonGuiDataDayCamGlassHeaterMode.JON_GUI_DATA_DAY_CAM_GLASS_HEATER_MODE_AUTO
                "manual" -> JonSharedDataTypes.JonGuiDataDayCamGlassHeaterMode.JON_GUI_DATA_DAY_CAM_GLASS_HEATER_MODE_MANUAL
                "off" -> JonSharedDataTypes.JonGuiDataDayCamGlassHeaterMode.JON_GUI_DATA_DAY_CAM_GLASS_HEATER_MODE_OFF
                else -> JonSharedDataTypes.JonGuiDataDayCamGlassHeaterMode.JON_GUI_DATA_DAY_CAM_GLASS_HEATER_MODE_AUTO
            }
        }
        
        // Command message handler - builds JonSharedCmd.Root from Transit messages
        handlers["command"] = object : ReadHandler<Map<*, *>, JonSharedCmd.Root> {
            override fun fromRep(rep: Map<*, *>): JonSharedCmd.Root {
                val action = rep[TransitFactory.keyword("action")] as? String
                    ?: throw IllegalArgumentException("Command missing action")
                val params = rep[TransitFactory.keyword("params")] as? Map<*, *>
                
                return buildCommand(action, params)
            }
        }
        
        return handlers
    }
    
    /**
     * Build a command from action and params using the clean handler approach
     */
    private fun buildCommand(action: String, params: Map<*, *>?): JonSharedCmd.Root {
        return when (action) {
            // Core commands
            "ping" -> buildPing()
            
            // Rotary commands
            "rotary-halt" -> buildRotaryHalt()
            "rotary-goto-ndc" -> buildRotaryGotoNDC(params)
            "rotary-set-velocity" -> buildRotarySetVelocity(params)
            
            // CV commands
            "cv-start-track-ndc" -> buildCVStartTrackNDC(params)
            
            // Camera zoom commands
            "heat-camera-next-zoom-table-pos" -> buildHeatCameraNextZoomTablePos()
            "heat-camera-prev-zoom-table-pos" -> buildHeatCameraPrevZoomTablePos()
            "day-camera-next-zoom-table-pos" -> buildDayCameraNextZoomTablePos()
            "day-camera-prev-zoom-table-pos" -> buildDayCameraPrevZoomTablePos()
            
            // System commands
            "system-reboot" -> buildSystemReboot()
            "system-power-off" -> buildSystemPowerOff()
            "system-reset-configs" -> buildSystemResetConfigs()
            "system-start-all" -> buildSystemStartAll()
            "system-stop-all" -> buildSystemStopAll()
            "system-mark-rec-important" -> buildSystemMarkRecImportant()
            "system-unmark-rec-important" -> buildSystemUnmarkRecImportant()
            "system-enter-transport" -> buildSystemEnterTransport()
            "system-enable-geodesic-mode" -> buildSystemEnableGeodesicMode()
            "system-disable-geodesic-mode" -> buildSystemDisableGeodesicMode()
            "system-set-localization" -> buildSystemSetLocalization(params)
            
            // GPS commands
            "gps-enable" -> buildGpsEnable()
            "gps-disable" -> buildGpsDisable()
            "gps-reset" -> buildGpsReset()
            
            // Compass commands
            "compass-calibrate" -> buildCompassCalibrate()
            "compass-reset" -> buildCompassReset()
            
            // LRF commands
            "lrf-measure" -> buildLrfMeasure()
            "lrf-continuous-start" -> buildLrfContinuousStart()
            "lrf-continuous-stop" -> buildLrfContinuousStop()
            
            // OSD commands
            "osd-enable" -> buildOsdEnable()
            "osd-disable" -> buildOsdDisable()
            
            // Glass heater commands
            "glass-heater-enable" -> buildGlassHeaterEnable()
            "glass-heater-disable" -> buildGlassHeaterDisable()
            "glass-heater-set-mode" -> buildGlassHeaterSetMode(params)
            
            else -> throw IllegalArgumentException("Unknown command action: $action")
        }
    }
    
    // Helper to create enum read handlers
    private inline fun <reified T> createEnumReadHandler(
        crossinline parser: (String) -> T
    ): ReadHandler<String, T> {
        return object : ReadHandler<String, T> {
            override fun fromRep(rep: String): T = parser(rep)
        }
    }
    
    // Helper to get keyword values from Transit maps
    private fun Map<*, *>.getKeyword(key: String): Any? = this[TransitFactory.keyword(key)]
    private fun Map<*, *>.requireKeyword(key: String): Any = 
        getKeyword(key) ?: throw IllegalArgumentException("Missing required parameter: $key")
    
    // Command builders - Clean implementations without manual parsing
    
    private fun buildPing(): JonSharedCmd.Root {
        return JonSharedCmd.Root.newBuilder()
            .setPing(JonSharedCmd.Ping.newBuilder().build())
            .build()
    }
    
    private fun buildRotaryHalt(): JonSharedCmd.Root {
        val haltMsg = JonSharedCmdRotary.Halt.newBuilder().build()
        val rotaryRoot = JonSharedCmdRotary.Root.newBuilder()
            .setHalt(haltMsg)
            .build()
            
        return JonSharedCmd.Root.newBuilder()
            .setProtocolVersion(1)
            .setRotary(rotaryRoot)
            .build()
    }
    
    private fun buildRotaryGotoNDC(params: Map<*, *>?): JonSharedCmd.Root {
        requireNotNull(params) { "rotary-goto-ndc requires parameters" }
        
        val channel = params.requireKeyword("channel") as String
        val x = (params.requireKeyword("x") as Number).toFloat()
        val y = (params.requireKeyword("y") as Number).toFloat()
        
        // The channel will be automatically converted by the video-channel handler
        val channelEnum = JonSharedDataTypes.JonGuiDataVideoChannel.valueOf(
            "JON_GUI_DATA_VIDEO_CHANNEL_${channel.uppercase()}"
        )
        
        val rotateToNdc = JonSharedCmdRotary.RotateToNDC.newBuilder()
            .setChannel(channelEnum)
            .setX(x)
            .setY(y)
            .build()
            
        val rotaryRoot = JonSharedCmdRotary.Root.newBuilder()
            .setRotateToNdc(rotateToNdc)
            .build()
            
        return JonSharedCmd.Root.newBuilder()
            .setProtocolVersion(1)
            .setRotary(rotaryRoot)
            .build()
    }
    
    private fun buildRotarySetVelocity(params: Map<*, *>?): JonSharedCmd.Root {
        requireNotNull(params) { "rotary-set-velocity requires parameters" }
        
        val azSpeed = (params.requireKeyword("azimuth-speed") as Number).toFloat()
        val elSpeed = (params.requireKeyword("elevation-speed") as Number).toFloat()
        
        // Get directions with defaults
        val azDirStr = params.getKeyword("azimuth-direction") as? String ?: "clockwise"
        val elDirStr = params.getKeyword("elevation-direction") as? String ?: "clockwise"
        
        // Convert to enums - the handler will do the conversion
        val azDir = when (azDirStr) {
            "counter-clockwise" -> 
                JonSharedDataTypes.JonGuiDataRotaryDirection.JON_GUI_DATA_ROTARY_DIRECTION_COUNTER_CLOCKWISE
            else -> 
                JonSharedDataTypes.JonGuiDataRotaryDirection.JON_GUI_DATA_ROTARY_DIRECTION_CLOCKWISE
        }
        
        val elDir = when (elDirStr) {
            "counter-clockwise" -> 
                JonSharedDataTypes.JonGuiDataRotaryDirection.JON_GUI_DATA_ROTARY_DIRECTION_COUNTER_CLOCKWISE
            else -> 
                JonSharedDataTypes.JonGuiDataRotaryDirection.JON_GUI_DATA_ROTARY_DIRECTION_CLOCKWISE
        }
        
        // Build the command structure
        val azimuthRotate = JonSharedCmdRotary.RotateAzimuth.newBuilder()
            .setSpeed(azSpeed)
            .setDirection(azDir)
            .build()
            
        val elevationRotate = JonSharedCmdRotary.RotateElevation.newBuilder()
            .setSpeed(elSpeed)
            .setDirection(elDir)
            .build()
            
        val azimuthMsg = JonSharedCmdRotary.Azimuth.newBuilder()
            .setRotate(azimuthRotate)
            .build()
            
        val elevationMsg = JonSharedCmdRotary.Elevation.newBuilder()
            .setRotate(elevationRotate)
            .build()
            
        val axisMsg = JonSharedCmdRotary.Axis.newBuilder()
            .setAzimuth(azimuthMsg)
            .setElevation(elevationMsg)
            .build()
            
        val rotaryRoot = JonSharedCmdRotary.Root.newBuilder()
            .setAxis(axisMsg)
            .build()
            
        return JonSharedCmd.Root.newBuilder()
            .setProtocolVersion(1)
            .setRotary(rotaryRoot)
            .build()
    }
    
    private fun buildCVStartTrackNDC(params: Map<*, *>?): JonSharedCmd.Root {
        requireNotNull(params) { "cv-start-track-ndc requires parameters" }
        
        val channel = params.requireKeyword("channel") as String
        val x = (params.requireKeyword("x") as Number).toFloat()
        val y = (params.requireKeyword("y") as Number).toFloat()
        val frameTimestamp = params.getKeyword("frame-timestamp") as? Long
        
        val channelEnum = when (channel) {
            "heat" -> JonSharedDataTypes.JonGuiDataVideoChannel.JON_GUI_DATA_VIDEO_CHANNEL_HEAT
            "day" -> JonSharedDataTypes.JonGuiDataVideoChannel.JON_GUI_DATA_VIDEO_CHANNEL_DAY
            else -> JonSharedDataTypes.JonGuiDataVideoChannel.JON_GUI_DATA_VIDEO_CHANNEL_HEAT
        }
        
        val builder = JonSharedCmdCv.StartTrackNDC.newBuilder()
            .setChannel(channelEnum)
            .setX(x)
            .setY(y)
            
        frameTimestamp?.let { builder.setFrameTime(it) }
        
        val cvRoot = JonSharedCmdCv.Root.newBuilder()
            .setStartTrackNdc(builder.build())
            .build()
            
        return JonSharedCmd.Root.newBuilder()
            .setProtocolVersion(1)
            .setCv(cvRoot)
            .build()
    }
    
    // Camera zoom commands
    private fun buildHeatCameraNextZoomTablePos(): JonSharedCmd.Root {
        val zoom = JonSharedCmdHeatCamera.Zoom.newBuilder()
            .setNextZoomTablePos(JonSharedCmdHeatCamera.NextZoomTablePos.newBuilder().build())
            .build()
            
        val heatRoot = JonSharedCmdHeatCamera.Root.newBuilder()
            .setZoom(zoom)
            .build()
            
        return JonSharedCmd.Root.newBuilder()
            .setProtocolVersion(1)
            .setHeatCamera(heatRoot)
            .build()
    }
    
    private fun buildHeatCameraPrevZoomTablePos(): JonSharedCmd.Root {
        val zoom = JonSharedCmdHeatCamera.Zoom.newBuilder()
            .setPrevZoomTablePos(JonSharedCmdHeatCamera.PrevZoomTablePos.newBuilder().build())
            .build()
            
        val heatRoot = JonSharedCmdHeatCamera.Root.newBuilder()
            .setZoom(zoom)
            .build()
            
        return JonSharedCmd.Root.newBuilder()
            .setProtocolVersion(1)
            .setHeatCamera(heatRoot)
            .build()
    }
    
    private fun buildDayCameraNextZoomTablePos(): JonSharedCmd.Root {
        val zoom = JonSharedCmdDayCamera.Zoom.newBuilder()
            .setNextZoomTablePos(JonSharedCmdDayCamera.NextZoomTablePos.newBuilder().build())
            .build()
            
        val dayRoot = JonSharedCmdDayCamera.Root.newBuilder()
            .setZoom(zoom)
            .build()
            
        return JonSharedCmd.Root.newBuilder()
            .setProtocolVersion(1)
            .setDayCamera(dayRoot)
            .build()
    }
    
    private fun buildDayCameraPrevZoomTablePos(): JonSharedCmd.Root {
        val zoom = JonSharedCmdDayCamera.Zoom.newBuilder()
            .setPrevZoomTablePos(JonSharedCmdDayCamera.PrevZoomTablePos.newBuilder().build())
            .build()
            
        val dayRoot = JonSharedCmdDayCamera.Root.newBuilder()
            .setZoom(zoom)
            .build()
            
        return JonSharedCmd.Root.newBuilder()
            .setProtocolVersion(1)
            .setDayCamera(dayRoot)
            .build()
    }
    
    // System commands
    private fun buildSystemReboot() = JonSharedCmd.Root.newBuilder()
        .setProtocolVersion(1)
        .setSystem(
            JonSharedCmdSystem.Root.newBuilder()
                .setReboot(JonSharedCmdSystem.Reboot.newBuilder().build())
                .build()
        )
        .build()
        
    private fun buildSystemPowerOff() = JonSharedCmd.Root.newBuilder()
        .setProtocolVersion(1)
        .setSystem(
            JonSharedCmdSystem.Root.newBuilder()
                .setPowerOff(JonSharedCmdSystem.PowerOff.newBuilder().build())
                .build()
        )
        .build()
        
    private fun buildSystemResetConfigs() = JonSharedCmd.Root.newBuilder()
        .setProtocolVersion(1)
        .setSystem(
            JonSharedCmdSystem.Root.newBuilder()
                .setResetConfigs(JonSharedCmdSystem.ResetConfigs.newBuilder().build())
                .build()
        )
        .build()
        
    private fun buildSystemStartAll() = JonSharedCmd.Root.newBuilder()
        .setProtocolVersion(1)
        .setSystem(
            JonSharedCmdSystem.Root.newBuilder()
                .setStartAll(JonSharedCmdSystem.StartALl.newBuilder().build())
                .build()
        )
        .build()
        
    private fun buildSystemStopAll() = JonSharedCmd.Root.newBuilder()
        .setProtocolVersion(1)
        .setSystem(
            JonSharedCmdSystem.Root.newBuilder()
                .setStopAll(JonSharedCmdSystem.StopALl.newBuilder().build())
                .build()
        )
        .build()
        
    private fun buildSystemMarkRecImportant() = JonSharedCmd.Root.newBuilder()
        .setProtocolVersion(1)
        .setSystem(
            JonSharedCmdSystem.Root.newBuilder()
                .setMarkRecImportant(JonSharedCmdSystem.MarkRecImportant.newBuilder().build())
                .build()
        )
        .build()
        
    private fun buildSystemUnmarkRecImportant() = JonSharedCmd.Root.newBuilder()
        .setProtocolVersion(1)
        .setSystem(
            JonSharedCmdSystem.Root.newBuilder()
                .setUnmarkRecImportant(JonSharedCmdSystem.UnmarkRecImportant.newBuilder().build())
                .build()
        )
        .build()
        
    private fun buildSystemEnterTransport() = JonSharedCmd.Root.newBuilder()
        .setProtocolVersion(1)
        .setSystem(
            JonSharedCmdSystem.Root.newBuilder()
                .setEnterTransport(JonSharedCmdSystem.EnterTransport.newBuilder().build())
                .build()
        )
        .build()
        
    private fun buildSystemEnableGeodesicMode() = JonSharedCmd.Root.newBuilder()
        .setProtocolVersion(1)
        .setSystem(
            JonSharedCmdSystem.Root.newBuilder()
                .setGeodesicModeEnable(JonSharedCmdSystem.EnableGeodesicMode.newBuilder().build())
                .build()
        )
        .build()
        
    private fun buildSystemDisableGeodesicMode() = JonSharedCmd.Root.newBuilder()
        .setProtocolVersion(1)
        .setSystem(
            JonSharedCmdSystem.Root.newBuilder()
                .setGeodesicModeDisable(JonSharedCmdSystem.DisableGeodesicMode.newBuilder().build())
                .build()
        )
        .build()
        
    private fun buildSystemSetLocalization(params: Map<*, *>?): JonSharedCmd.Root {
        requireNotNull(params) { "system-set-localization requires parameters" }
        
        val localization = params.requireKeyword("localization") as String
        
        val loc = when (localization.lowercase()) {
            "en", "english" -> JonSharedDataTypes.JonGuiDataSystemLocalizations.JON_GUI_DATA_SYSTEM_LOCALIZATIONS_EN
            "ua", "ukrainian" -> JonSharedDataTypes.JonGuiDataSystemLocalizations.JON_GUI_DATA_SYSTEM_LOCALIZATIONS_UA
            "cs", "czech" -> JonSharedDataTypes.JonGuiDataSystemLocalizations.JON_GUI_DATA_SYSTEM_LOCALIZATIONS_CS
            "ar", "arabic" -> JonSharedDataTypes.JonGuiDataSystemLocalizations.JON_GUI_DATA_SYSTEM_LOCALIZATIONS_AR
            "fr", "french" -> JonSharedDataTypes.JonGuiDataSystemLocalizations.JON_GUI_DATA_SYSTEM_LOCALIZATIONS_FR
            "de", "german" -> JonSharedDataTypes.JonGuiDataSystemLocalizations.JON_GUI_DATA_SYSTEM_LOCALIZATIONS_DE
            "es", "spanish" -> JonSharedDataTypes.JonGuiDataSystemLocalizations.JON_GUI_DATA_SYSTEM_LOCALIZATIONS_ES
            else -> JonSharedDataTypes.JonGuiDataSystemLocalizations.JON_GUI_DATA_SYSTEM_LOCALIZATIONS_EN
        }
        
        return JonSharedCmd.Root.newBuilder()
            .setProtocolVersion(1)
            .setSystem(
                JonSharedCmdSystem.Root.newBuilder()
                    .setLocalization(
                        JonSharedCmdSystem.SetLocalization.newBuilder()
                            .setLoc(loc)
                            .build()
                    )
                    .build()
            )
            .build()
    }
    
    // GPS commands
    private fun buildGpsEnable() = JonSharedCmd.Root.newBuilder()
        .setProtocolVersion(1)
        .setGps(
            JonSharedCmdGps.Root.newBuilder()
                .setEnable(JonSharedCmdGps.Enable.newBuilder().build())
                .build()
        )
        .build()
        
    private fun buildGpsDisable() = JonSharedCmd.Root.newBuilder()
        .setProtocolVersion(1)
        .setGps(
            JonSharedCmdGps.Root.newBuilder()
                .setDisable(JonSharedCmdGps.Disable.newBuilder().build())
                .build()
        )
        .build()
        
    private fun buildGpsReset() = JonSharedCmd.Root.newBuilder()
        .setProtocolVersion(1)
        .setGps(
            JonSharedCmdGps.Root.newBuilder()
                .setReset(JonSharedCmdGps.Reset.newBuilder().build())
                .build()
        )
        .build()
        
    // Compass commands
    private fun buildCompassCalibrate() = JonSharedCmd.Root.newBuilder()
        .setProtocolVersion(1)
        .setCompass(
            JonSharedCmdCompass.Root.newBuilder()
                .setCalibrate(JonSharedCmdCompass.Calibrate.newBuilder().build())
                .build()
        )
        .build()
        
    private fun buildCompassReset() = JonSharedCmd.Root.newBuilder()
        .setProtocolVersion(1)
        .setCompass(
            JonSharedCmdCompass.Root.newBuilder()
                .setReset(JonSharedCmdCompass.Reset.newBuilder().build())
                .build()
        )
        .build()
        
    // LRF commands
    private fun buildLrfMeasure() = JonSharedCmd.Root.newBuilder()
        .setProtocolVersion(1)
        .setLrf(
            JonSharedCmdLrf.Root.newBuilder()
                .setMeasure(JonSharedCmdLrf.Measure.newBuilder().build())
                .build()
        )
        .build()
        
    private fun buildLrfContinuousStart() = JonSharedCmd.Root.newBuilder()
        .setProtocolVersion(1)
        .setLrf(
            JonSharedCmdLrf.Root.newBuilder()
                .setContinuousStart(JonSharedCmdLrf.ContinuousStart.newBuilder().build())
                .build()
        )
        .build()
        
    private fun buildLrfContinuousStop() = JonSharedCmd.Root.newBuilder()
        .setProtocolVersion(1)
        .setLrf(
            JonSharedCmdLrf.Root.newBuilder()
                .setContinuousStop(JonSharedCmdLrf.ContinuousStop.newBuilder().build())
                .build()
        )
        .build()
        
    // OSD commands
    private fun buildOsdEnable() = JonSharedCmd.Root.newBuilder()
        .setProtocolVersion(1)
        .setOsd(
            JonSharedCmdOsd.Root.newBuilder()
                .setEnableOsd(JonSharedCmdOsd.EnableOSD.newBuilder().build())
                .build()
        )
        .build()
        
    private fun buildOsdDisable() = JonSharedCmd.Root.newBuilder()
        .setProtocolVersion(1)
        .setOsd(
            JonSharedCmdOsd.Root.newBuilder()
                .setDisableOsd(JonSharedCmdOsd.DisableOSD.newBuilder().build())
                .build()
        )
        .build()
        
    // Glass heater commands
    private fun buildGlassHeaterEnable() = JonSharedCmd.Root.newBuilder()
        .setProtocolVersion(1)
        .setDayCamGlassHeater(
            JonSharedCmdDayCamGlassHeater.Root.newBuilder()
                .setEnable(JonSharedCmdDayCamGlassHeater.Enable.newBuilder().build())
                .build()
        )
        .build()
        
    private fun buildGlassHeaterDisable() = JonSharedCmd.Root.newBuilder()
        .setProtocolVersion(1)
        .setDayCamGlassHeater(
            JonSharedCmdDayCamGlassHeater.Root.newBuilder()
                .setDisable(JonSharedCmdDayCamGlassHeater.Disable.newBuilder().build())
                .build()
        )
        .build()
        
    private fun buildGlassHeaterSetMode(params: Map<*, *>?): JonSharedCmd.Root {
        requireNotNull(params) { "glass-heater-set-mode requires parameters" }
        
        val mode = params.requireKeyword("mode") as String
        
        val heaterMode = when (mode.lowercase()) {
            "auto" -> JonSharedDataTypes.JonGuiDataDayCamGlassHeaterMode.JON_GUI_DATA_DAY_CAM_GLASS_HEATER_MODE_AUTO
            "manual" -> JonSharedDataTypes.JonGuiDataDayCamGlassHeaterMode.JON_GUI_DATA_DAY_CAM_GLASS_HEATER_MODE_MANUAL
            "off" -> JonSharedDataTypes.JonGuiDataDayCamGlassHeaterMode.JON_GUI_DATA_DAY_CAM_GLASS_HEATER_MODE_OFF
            else -> JonSharedDataTypes.JonGuiDataDayCamGlassHeaterMode.JON_GUI_DATA_DAY_CAM_GLASS_HEATER_MODE_AUTO
        }
        
        return JonSharedCmd.Root.newBuilder()
            .setProtocolVersion(1)
            .setDayCamGlassHeater(
                JonSharedCmdDayCamGlassHeater.Root.newBuilder()
                    .setSetMode(
                        JonSharedCmdDayCamGlassHeater.SetMode.newBuilder()
                            .setMode(heaterMode)
                            .build()
                    )
                    .build()
            )
            .build()
    }
}