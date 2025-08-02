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
import com.cognitect.transit.TransitFactory
import ser.JonSharedDataTypes

/**
 * Simplified command handler that builds protobuf commands from Transit messages.
 * This is a replacement for ProtobufCommandHandlers that avoids Transit ReadHandler
 * complexity.
 */
object SimpleCommandHandlers {
    /**
     * Build a command from Transit message data
     */
    fun buildCommand(msgData: Map<*, *>): JonSharedCmd.Root? {
        val action = msgData[TransitFactory.keyword("action")] as? String
            ?: return null
        val params = msgData[TransitFactory.keyword("params")] as? Map<*, *>
        
        return buildCommand(action, params)
    }
    
    /**
     * Build a command from action and params
     */
    fun buildCommand(action: String, params: Map<*, *>?): JonSharedCmd.Root {
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
            "gps-start" -> buildGpsStart()
            "gps-stop" -> buildGpsStop()
            
            // Compass commands
            "compass-start" -> buildCompassStart()
            "compass-stop" -> buildCompassStop()
            
            // LRF commands  
            "lrf-measure" -> buildLrfMeasure()
            "lrf-start" -> buildLrfStart()
            "lrf-stop" -> buildLrfStop()
            
            else -> throw IllegalArgumentException("Unknown command action: $action")
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
        
        val channelEnum = when (channel) {
            "heat" -> JonSharedDataTypes.JonGuiDataVideoChannel.JON_GUI_DATA_VIDEO_CHANNEL_HEAT
            "day" -> JonSharedDataTypes.JonGuiDataVideoChannel.JON_GUI_DATA_VIDEO_CHANNEL_DAY
            else -> JonSharedDataTypes.JonGuiDataVideoChannel.JON_GUI_DATA_VIDEO_CHANNEL_HEAT
        }
        
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
        
        // Convert to enums
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
            "en", "english" -> JonSharedDataTypes.JonGuiDataSystemLocalizations.JON_GUI_DATA_SYSTEM_LOCALIZATION_EN
            "ua", "ukrainian" -> JonSharedDataTypes.JonGuiDataSystemLocalizations.JON_GUI_DATA_SYSTEM_LOCALIZATION_UA
            "cs", "czech" -> JonSharedDataTypes.JonGuiDataSystemLocalizations.JON_GUI_DATA_SYSTEM_LOCALIZATION_CS
            "ar", "arabic" -> JonSharedDataTypes.JonGuiDataSystemLocalizations.JON_GUI_DATA_SYSTEM_LOCALIZATION_AR
            else -> JonSharedDataTypes.JonGuiDataSystemLocalizations.JON_GUI_DATA_SYSTEM_LOCALIZATION_EN
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
    private fun buildGpsStart() = JonSharedCmd.Root.newBuilder()
        .setProtocolVersion(1)
        .setGps(
            JonSharedCmdGps.Root.newBuilder()
                .setStart(JonSharedCmdGps.Start.newBuilder().build())
                .build()
        )
        .build()
        
    private fun buildGpsStop() = JonSharedCmd.Root.newBuilder()
        .setProtocolVersion(1)
        .setGps(
            JonSharedCmdGps.Root.newBuilder()
                .setStop(JonSharedCmdGps.Stop.newBuilder().build())
                .build()
        )
        .build()
        
    // Compass commands
    private fun buildCompassStart() = JonSharedCmd.Root.newBuilder()
        .setProtocolVersion(1)
        .setCompass(
            JonSharedCmdCompass.Root.newBuilder()
                .setStart(JonSharedCmdCompass.Start.newBuilder().build())
                .build()
        )
        .build()
        
    private fun buildCompassStop() = JonSharedCmd.Root.newBuilder()
        .setProtocolVersion(1)
        .setCompass(
            JonSharedCmdCompass.Root.newBuilder()
                .setStop(JonSharedCmdCompass.Stop.newBuilder().build())
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
        
    private fun buildLrfStart() = JonSharedCmd.Root.newBuilder()
        .setProtocolVersion(1)
        .setLrf(
            JonSharedCmdLrf.Root.newBuilder()
                .setStart(JonSharedCmdLrf.Start.newBuilder().build())
                .build()
        )
        .build()
        
    private fun buildLrfStop() = JonSharedCmd.Root.newBuilder()
        .setProtocolVersion(1)
        .setLrf(
            JonSharedCmdLrf.Root.newBuilder()
                .setStop(JonSharedCmdLrf.Stop.newBuilder().build())
                .build()
        )
        .build()
}